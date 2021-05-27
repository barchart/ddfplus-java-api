/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */

package com.ddfplus.net;

import com.ddfplus.api.*;
import com.ddfplus.db.*;
import com.ddfplus.enums.ConnectionType;
import com.ddfplus.messages.DdfMarketBase;
import com.ddfplus.messages.DdfMarketTrade;
import com.ddfplus.service.definition.DefinitionService;
import com.ddfplus.service.definition.DefinitionServiceImpl;
import com.ddfplus.service.feed.FeedService;
import com.ddfplus.service.feed.FeedServiceImpl;
import com.ddfplus.service.usersettings.UserSettings;
import com.ddfplus.service.usersettings.UserSettingsService;
import com.ddfplus.service.usersettings.UserSettingsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

/**
 * DDF Client API.
 * 
 * To use:
 * 
 * <ol>
 * <li>Instantiate with proper connection variables.
 * <li>Call the init() method.
 * <li>Call the connect() method.
 * <li>Add subscriptions for quote and book quotes as required.
 * </ol>
 */
public class DdfClientImpl implements DdfClient {

	private static final Logger log = LoggerFactory.getLogger(DdfClientImpl.class);

	// Member variables are static, as the intention is keep one active
	// DdfClient per VM or ClassLoader instance.
	private static Connection connection;

	private static final DataMaster dataMaster = new DataMaster(MasterType.Realtime);

	private static final CopyOnWriteArrayList<ConnectionEventHandler> adminHandlers = new CopyOnWriteArrayList<>();

	// Raw DDF message handlers
	private static final CopyOnWriteArrayList<FeedHandler> feedHandlers = new CopyOnWriteArrayList<>();

	// Market Event Handlers
	private static final CopyOnWriteArrayList<MarketEventHandler> marketEventHandlers = new CopyOnWriteArrayList<>();

	// Timestamp Handlers
	private static final CopyOnWriteArrayList<TimestampHandler> timestampHandlers = new CopyOnWriteArrayList<>();

	// Quote/Market Update
	private static final Map<String, CopyOnWriteArrayList<QuoteHandler>> quoteHandlers = new ConcurrentHashMap<>();

	// Quote/Market Update by Exchange (ExchangeCode ==> Handler)
	private static final Map<String, QuoteHandler> quoteExchangeHandlers = new ConcurrentHashMap<>();

	// Trades by Exchange (ExchangeCode ==> Handler)
	private static final Map<String, TradeHandler> tradeExchangeHandlers = new ConcurrentHashMap<>();

	// Market Depth/Book Quote
	private static final Map<String, CopyOnWriteArrayList<BookQuoteHandler>> bookQuoteHandlers = new ConcurrentHashMap<>();

	// OHLC handlers
	private static final Map<String, MinuteBarHandler> minuteBarHandlers = new ConcurrentHashMap<>();
	// Exchange to OHLC exchange handler
	private static final Map<String, MinuteBarExchangeHandler> minuteBarExchangeHandlers = new ConcurrentHashMap<>();

	private static int instanceId = 0;

	// Client Configuration
	private ClientConfig config;

	// Connection parameters
	private String host;
	private String secondaryHost;
	private final ConnectionType type;
	// will bind to all interfaces if null
	private String bindInterface = null;
	private int jerqVersion = NetConstants.JERQ_VERSION_DEFAULT;
	// Jerq login credentials
	private final String username;
	private final String password;
	private UserSettings userSettings;
	// Jerq Snapshot credentials
	private String snapshotUserName;
	private String snapshotPassword;
	private UserSettings snapshotUserSettings;

	private UserSettingsService userSettingsService = new UserSettingsServiceImpl();
	// Snapshot via web service
	private FeedService feedService;
	/*
	 * TOOD Might be removed. Symbol Provider
	 */
	private SymbolProvider symbolProvider;

	private SymbolShortCuts symbolShortCuts;
	private DefinitionService definitionService;
	private final ScheduledExecutorService unknownSymbolScheduler = Executors.newScheduledThreadPool(1);

	public DdfClientImpl(ClientConfig config) {
		this(config, new SymbolProviderImpl());
	}

	public DdfClientImpl(ClientConfig config, SymbolProvider symbolProvider) {
		this.config = config;
		this.username = config.getUserName();
		this.password = config.getPassword();
		if (username == null || password == null) {
			throw new IllegalArgumentException("username and password have to be set");
		}
		this.type = config.getConnectionType();
		this.bindInterface = config.getBindInterface();
		this.host = config.getPrimaryServer();
		this.secondaryHost = config.getSecondaryServer();
		this.symbolProvider = symbolProvider;

		// Definition Service
		definitionService = new DefinitionServiceImpl();
		definitionService.init(config.getDefinitionRefreshIntervalSec());

		symbolShortCuts = new SymbolShortCutsImpl(definitionService);
	}

	@Override
	public void init() {
		if (this.host == null) {
			// Look up via user settings
			log.info("Looking up user settings for: " + this.username);
			userSettings = userSettingsService.getUserSettings(this.username, this.password);
			this.host = userSettings.getStreamPrimaryServer();
			if (this.host == null) {
				throw new IllegalStateException("Could not determine DDF server for user: " + username);
			}
		} else {
			userSettings = new UserSettings();
			userSettings.setUserName(this.username);
			userSettings.setPassword(this.password);
			userSettings.setStreamPrimaryServer(this.host);
			userSettings.setStreamSecondaryServer(this.secondaryHost);
		}

		/*
		 * For remote snapshot/refresh refreshes when using the "push"/STREAM
		 * LISTEN commands.
		 */
		if (snapshotUserName != null && snapshotPassword != null) {
			// Look up via user settings
			log.info("Looking up snaphot/refresh user settings for: " + snapshotUserName);
			snapshotUserSettings = userSettingsService.getUserSettings(snapshotUserName, snapshotPassword);
			if (snapshotUserSettings.getStreamPrimaryServer() == null) {
				log.warn("Could not determine Snapshot DDF server for user: " + snapshotUserName
						+ " will not have snapshots for push quotes.");
			}
			/*
			 * Add Feed Service
			 */
			feedService = new FeedServiceImpl(dataMaster, snapshotUserSettings, quoteExchangeHandlers);
			dataMaster.setFeedService(feedService);
		}

		/*
		 * Start a background task to subscribe to unknown symbols (Symbols
		 * which do not have quotes), in order to see if the symbol has been
		 * added to the back end system.
		 */
		log.info("Scheduling an unknown symbol lookup " + config.getUnknownSymbolInterval() + " every seconds.");
		UnknownSymbolThread unknownSymbolThread = new UnknownSymbolThread();
		unknownSymbolScheduler.scheduleAtFixedRate(unknownSymbolThread, config.getUnknownSymbolDeplay(),
				config.getUnknownSymbolInterval(), TimeUnit.SECONDS);
	}

	@Override
	public void setSnapshotLogin(String username, String password) {
		snapshotUserName = username;
		snapshotPassword = password;
		if (snapshotUserName == null || snapshotPassword == null) {
			throw new IllegalArgumentException("snapshot username and password have to be set");
		}
	}

	@Override
	public void connect() throws Exception {

		synchronized (dataMaster) {

			log.info("Trying to connect..");

			if (connection != null) {
				log.error("connection != null");
				return;
			}

			InetAddress ddfServer = InetAddress.getByName(userSettings.getStreamPrimaryServer());
			InetAddress secondaryServer = InetAddress.getByName(userSettings.getStreamSecondaryServer());
			if (type == ConnectionType.WS || type == ConnectionType.WSS) {
				ddfServer = InetAddress.getByName(userSettings.getWssServer());
			}

			InetAddress intf = InetAddress.getByName(this.bindInterface);
			connection = new Connection(//
					this.type, //
					this.username, //
					this.password, //
					ddfServer, //
					config.getServerPort() != null ? config.getServerPort() : type.port, //
					intf, //
					symbolProvider, //
					secondaryServer);

			// Sets the JERQ/P version
			connection.setVersion(jerqVersion);

			DdfClientConnectionHandler handler = new DdfClientConnectionHandler();
			connection.registerHandler(handler);

			log.info("Starting DdfClient#" + ++instanceId + " Version = " + connection.getVersion());

			// Start the connection
			connection.startDataStream();
		}

	}

	@Override
	public void disconnect() {
		if (connection != null) {
			log.info("Stopping DdfClient#" + ++instanceId + " Version = " + connection.getVersion());
			connection.stopDataStream();
			connection = null;
			// Clear handlers
			adminHandlers.clear();
			feedHandlers.clear();
			marketEventHandlers.clear();
			timestampHandlers.clear();
			quoteHandlers.clear();
			quoteExchangeHandlers.clear();
			tradeExchangeHandlers.clear();
			bookQuoteHandlers.clear();
			minuteBarHandlers.clear();
			minuteBarExchangeHandlers.clear();
		}
	}

	@Override
	public void addConnectionEventHandler(ConnectionEventHandler handler) {
		adminHandlers.addIfAbsent(handler);
	}

	@Override
	public void removeConnectionEventHandler(ConnectionEventHandler handler) {
		adminHandlers.remove(handler);
	}

	@Override
	public void addFeedHandler(FeedHandler handler) {
		feedHandlers.addIfAbsent(handler);
	}

	@Override
	public void removeFeedHandler(FeedHandler handler) {
		feedHandlers.remove(handler);
	}

	@Override
	public void addMarketEventHandler(MarketEventHandler handler) {
		marketEventHandlers.addIfAbsent(handler);
	}

	@Override
	public void removeMarketEventHandler(MarketEventHandler handler) {
		marketEventHandlers.remove(handler);
	}

	@Override
	public void addTimestampHandler(TimestampHandler handler) {
		timestampHandlers.addIfAbsent(handler);
	}

	@Override
	public void removeTimestampHandler(TimestampHandler handler) {
		timestampHandlers.remove(handler);
	}

	@Override
	public void addQuoteHandler(String symbol, QuoteHandler handler) {

		String[] realSymbols = symbolShortCuts.resolveShortCutSymbols(symbol.trim());
		if (realSymbols.length == 0) {
			log.error("Invalid Symbol: " + symbol + " ignoring.");
			return;
		}
		for (String s : realSymbols) {
			synchronized (quoteHandlers) {
				CopyOnWriteArrayList<QuoteHandler> l = quoteHandlers.get(s);
				if (l == null) {
					// No subscription
					l = new CopyOnWriteArrayList<>();
					quoteHandlers.put(s, l);
					l.add(handler);
					// Initial Subscription
					subscribeQuote(s);
				} else {
					// We have a subscription
					boolean added = l.addIfAbsent(handler);
					if (added) {
						sendQuoteFromCache(s, handler);
					}
				}
			}
		}
	}

	@Override
	public void removeQuoteHandler(String symbol, QuoteHandler handler) {
		synchronized (quoteHandlers) {
			CopyOnWriteArrayList<QuoteHandler> l = quoteHandlers.get(symbol);
			if (l == null) {
				return;
			}
			l.remove(handler);
			if (l.size() == 0) {
				unsubscribeQuote(symbol);
			}
		}
	}

	@Override
	public void addBookQuoteHandler(String symbol, BookQuoteHandler handler) {
		synchronized (bookQuoteHandlers) {
			CopyOnWriteArrayList<BookQuoteHandler> l = bookQuoteHandlers.get(symbol);
			if (l == null) {
				// No subscription
				l = new CopyOnWriteArrayList<>();
				bookQuoteHandlers.put(symbol, l);
				l.add(handler);
				// Initial Subscription
				subscribeDepth(symbol);
			} else {
				// We have a subscription
				boolean added = l.addIfAbsent(handler);
				if (added) {
					sendDepthFromCache(symbol, handler);
				}
			}
		}
	}

	@Override
	public void removeBookQuoteHandler(String symbol, BookQuoteHandler handler) {
		synchronized (bookQuoteHandlers) {
			CopyOnWriteArrayList<BookQuoteHandler> l = bookQuoteHandlers.get(symbol);
			if (l == null) {
				return;
			}
			l.remove(handler);
			if (l.size() == 0) {
				unsubscribeDepth(symbol);
			}
		}
	}

	@Override
	public void addQuoteExchangeHandler(String exchangeCode, QuoteHandler handler) {
		synchronized (quoteExchangeHandlers) {

			QuoteHandler h = quoteExchangeHandlers.get(exchangeCode);
			if (h == null) {
				// No subscription
				quoteExchangeHandlers.put(exchangeCode, handler);
				// Initial Stream Subscription
				if (!tradeExchangeHandlers.containsKey(exchangeCode)) {
					// Only subscribe once
					subscribeExchange(exchangeCode);
				}
			} else {
				/*
				 * We already have a subscription and only one Exchange handler
				 * is allowed.
				 */
				log.warn(
						"An exchange quote subscription was already active, only 1 handler is allowed per exchange, exchange: "
								+ exchangeCode);
			}
		}
	}

	@Override
	public void removeQuoteExchangeHandler(String exchangeCode) {
		synchronized (quoteExchangeHandlers) {
			quoteExchangeHandlers.remove(exchangeCode);
		}
	}

	@Override
	public void addTradeExchangeHandler(String exchangeCode, TradeHandler handler) {
		synchronized (tradeExchangeHandlers) {

			TradeHandler h = tradeExchangeHandlers.get(exchangeCode);
			if (h == null) {
				// No subscription
				tradeExchangeHandlers.put(exchangeCode, handler);
				// Initial Stream Subscription
				if (!quoteExchangeHandlers.containsKey(exchangeCode)) {
					// only subscribe once
					subscribeExchange(exchangeCode);
				}
			} else {
				/*
				 * We already have a subscription and only one Exchange handler
				 * is allowed.
				 */
				log.warn(
						"An exchange trade subscription was already active, only 1 handler is allowed per exchange, exchange: "
								+ exchangeCode);
			}
		}

	}

	@Override
	public void removeTradeExchangeHandler(String exchangeCode) {
		synchronized (tradeExchangeHandlers) {
			tradeExchangeHandlers.remove(exchangeCode);
		}
	}

	@Override
	public void addMinuteBarHandler(String symbol, MinuteBarHandler handler) {
		synchronized (minuteBarHandlers) {

			MinuteBarHandler h = minuteBarHandlers.get(symbol);
			if (h == null) {
				// No subscription
				minuteBarHandlers.put(symbol, handler);
				// Initial Subscription
				subscribeMinuteBar(symbol);
			} else {
				log.warn("A minute bar subscription was already active, only 1 handler is allowed per symbol, symbol: "
						+ symbol);
			}
		}

	}

	@Override
	public void removeMinuteBarHandler(String symbol) {
		synchronized (minuteBarHandlers) {
			minuteBarHandlers.remove(symbol);
			unSubscribeMinuteBar(symbol);
		}

	}

	@Override
	public void addMinuteBarExchangeHandler(String exchange, MinuteBarExchangeHandler handler) {
		synchronized (minuteBarExchangeHandlers) {

			MinuteBarExchangeHandler h = minuteBarExchangeHandlers.get(exchange);
			if (h == null) {
				// No subscription
				minuteBarExchangeHandlers.put(exchange, handler);
				// Initial Subscription
				subscribeMinuteBarExchange(exchange);
			} else {
				log.warn(
						"A minute bar subscription was already active, only 1 handler is allowed per exchange, exchange: "
								+ exchange);
			}
		}

	}

	@Override
	public void removeMinuteBarExchangeHandler(String exch) {
		synchronized (minuteBarExchangeHandlers) {
			minuteBarExchangeHandlers.remove(exch);
			unSubscribeMinuteBarExchange(exch);
		}

	}

	@Override
	public Quote getQuote(String symbol) {
		return dataMaster.getQuote(symbol);
	}

	@Override
	public BookQuote getBookQuote(String symbol) {
		return dataMaster.getBookQuote(symbol);
	}

	@Override
	public CumulativeVolume getCumulativeVolume(String symbol) {
		return dataMaster.getCumulativeVolume(symbol);
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public int getJerqVersion() {
		return jerqVersion;
	}

	public void setJerqVersion(int jerqVersion) {
		this.jerqVersion = jerqVersion;
	}

	private void subscribeDepth(String symbol) {
		connection.subscribeDepth(symbol);
		// For tracking unknown symbols
		dataMaster.addSubscribedSymbol(symbol);
	}

	private void unsubscribeDepth(String symbol) {
		connection.unsubscribeDepth(symbol);
		dataMaster.removeSubscribedSymbol(symbol);
	}

	private void sendDepthFromCache(String symbol, BookQuoteHandler handler) {
		BookQuote depth = dataMaster.getBookQuote(symbol);
		if (depth != null) {
			handler.onBookQuote(depth);
		}

	}

	private void subscribeQuote(String symbol) {
		connection.subscribeQuote(symbol);
		// For tracking unknown symbols
		dataMaster.addSubscribedSymbol(symbol);
	}

	private void unsubscribeQuote(String symbol) {
		connection.unsubscribeQuote(symbol);
		dataMaster.removeSubscribedSymbol(symbol);
	}

	private void subscribeExchange(String exchangeCode) {
		connection.subscribeExchange(exchangeCode);

	}

	private void subscribeMinuteBar(String symbol) {
		connection.subscribeMinuteBar(symbol);
	}

	private void unSubscribeMinuteBar(String symbol) {
		connection.unsubscribeMinuteBar(symbol);
	}

	private void subscribeMinuteBarExchange(String exchange) {
		String[] syms = definitionService.getExchangeSymbols(exchange);
		for (String s : syms) {
			subscribeMinuteBar(s);
		}
	}

	private void unSubscribeMinuteBarExchange(String exchange) {
		String[] syms = definitionService.getExchangeSymbols(exchange);
		for (String s : syms) {
			unSubscribeMinuteBar(s);
		}
	}

	private void sendQuoteFromCache(String symbol, QuoteHandler handler) {
		Quote quote = dataMaster.getQuote(symbol);
		if (quote != null) {
			handler.onQuote(quote);
		}
	}

	/**
	 * Connection Handler
	 */
	private class DdfClientConnectionHandler implements ConnectionHandler {

		private boolean wireStats = false;
		private WireStats stats;

		public DdfClientConnectionHandler() {
			if(wireStats) {
				stats = new WireStats();
				Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::logStats,1,1,TimeUnit.SECONDS);
			}
		}

		private void logStats() {
			log.info("{}",this.stats);
			this.stats.reset();
		}


		@Override
		public void onConnectionEvent(ConnectionEvent event) {
			for (ConnectionEventHandler listener : adminHandlers) {
				listener.onEvent(event);
			}
		}

		@Override
		public void onMessage(byte[] array) {

			if(wireStats) {
				stats.update(array.length);
			}

			// Decode and update caches
			FeedEvent fe = dataMaster.processMessage(array);

			if (fe == null) {
				// decode failure
				return;
			}

			// RAW DDF Handlers
			if (fe.isDdfMessage()) {
				for (FeedHandler h : feedHandlers) {
					try {
						h.onMessage(fe.getDdfMessage());
					} catch (Exception e) {
						log.error("DdfClient.onMessage(" + array + ") failed on onMessage. " + e);
					}
				}
			}

			/*
			 * Call any exchange Trade handlers, which works with the raw
			 * message only.
			 */
			if (fe.isTrade()) {
				DdfMarketTrade trade = fe.getTrade();
				char ddfExchange = trade.getExchange();
				TradeHandler tradeExchangeHandler = tradeExchangeHandlers.get(Character.toString(ddfExchange));
				if (tradeExchangeHandler != null) {
					try {
						tradeExchangeHandler.onTrade(trade);
					} catch (Exception e) {
						log.error("exchangeTrade(" + array + ") failed on onMessage. " + e);
					}
				}
			}

			/*
			 * Quote is updated for all DDF record 2 messages.
			 * 
			 */
			if (fe.isQuote()) {

				Quote q = fe.getQuote();
				String symbol = q.getSymbolInfo().getSymbol();

				// Quote Handlers for pull (GO command)
				CopyOnWriteArrayList<QuoteHandler> handlers = quoteHandlers.get(symbol);
				if (handlers != null) {
					for (QuoteHandler h : handlers) {
						try {
							h.onQuote(q);
						} catch (Exception e) {
							log.error("quote(" + array + ") failed on onMessage. " + e);
						}
					}
				} else {
					log.debug("Quote handler not found for symbol: {} msg: {}", q.getSymbolInfo().getSymbol(),
							q.getMessage());
				}

				// Quote Exchange Handler for push (STREAM LISTEN command)
				String ddfExchange = q.getDDFExchange();
				QuoteHandler eh = quoteExchangeHandlers.get(ddfExchange);
				if (eh != null) {
					try {
						eh.onQuote(q);
					} catch (Exception e) {
						log.error("exchangeQuote(" + array + ") failed on onMessage. " + e);
					}
				}

				// Trade Exchange Handler for push (STREAM LISTEN command)
				TradeHandler tradeExchangeHandler = tradeExchangeHandlers.get(ddfExchange);
				if (tradeExchangeHandler != null) {
					try {
						DdfMarketBase ddf = q.getMessage();
						if (ddf instanceof DdfMarketTrade) {
							DdfMarketTrade trade = (DdfMarketTrade) ddf;
							char sessionCondition = trade.getSession();
							tradeExchangeHandler.onTrade(trade);
						}
					} catch (Exception e) {
						log.error("exchangeTrade(" + array + ") failed on onMessage. " + e);
					}
				}

			}

			// Book/Depth
			if (fe.isBookQuote()) {
				BookQuote bq = fe.getBook();
				CopyOnWriteArrayList<BookQuoteHandler> handlers = bookQuoteHandlers.get(bq.getSymbol());
				if (handlers != null) {
					for (BookQuoteHandler h : handlers) {
						try {
							h.onBookQuote(bq);
						} catch (Exception e) {
							log.error("bookQuote(" + array + ") failed on onMessage. " + e);
						}
					}
				}
			}

			// Timestamp
			if (fe.isTimestamp()) {
				Date ts = fe.getTimestamp();
				for (TimestampHandler h : timestampHandlers) {
					try {
						h.onTimestamp(ts);
					} catch (Exception e) {
						log.error("timestamp(" + array + ") failed on onMessage. " + e);
					}
				}
			}

			// Cumulative Volume
			if (fe.isCumVolume()) {
				// Do nothing
			}

			// Market Events
			if (fe.isMarketEvents()) {
				for (MarketEventHandler h : marketEventHandlers) {
					try {
						for (MarketEvent e : fe.getMarketEvents()) {
							h.onEvent(e);
						}
					} catch (Exception e) {
						log.error("marketEvent(" + array + ") failed on onMessage. " + e);
					}
				}
			}

			// OHLC
			if (fe.isOhlc()) {
				Ohlc ohlc = fe.getOhlc();
				// By exchange
				String exchange = definitionService.getExchange(ohlc.getSymbol());
				if (exchange != null) {
					MinuteBarExchangeHandler h = minuteBarExchangeHandlers.get(exchange);
					if (h != null) {
						try {
							ohlc.setExchange(exchange);
							h.onOhlc(ohlc);
						} catch (Exception e) {
							log.error("minuteBarExchange(" + array + ") failed on onOhlc. " + e);
						}
					}
				}
				// By Symbol
				MinuteBarHandler mh = minuteBarHandlers.get(ohlc.getSymbol());
				if (mh != null) {
					try {
						mh.onOhlc(ohlc);
					} catch (Exception e) {
						log.error("minuteBar(" + array + ") failed on onOhlc. " + e);
					}
				}
			}
		}

	}

	private class UnknownSymbolThread implements Runnable {

		@Override
		public void run() {
			if (log.isDebugEnabled()) {
				log.debug("Running unknown symbol thread.");
			}
			for (String s : dataMaster.getUnknownSymbols()) {
				/*
				 * Subscribe for snapshot only, which will return the refresh if
				 * the symbol is known.
				 */
				connection.subscribeQuoteSnapshot(s);
			}
		}
	}

}
