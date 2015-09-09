/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.db;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ddfplus.codec.Codec;
import com.ddfplus.db.MarketEvent.MarketEventType;
import com.ddfplus.enums.DdfRecord;
import com.ddfplus.enums.DdfSessionCode;
import com.ddfplus.enums.DdfSubRecord;
import com.ddfplus.enums.QuoteElement;
import com.ddfplus.enums.QuoteElementModifiers;
import com.ddfplus.messages.DdfTimestamp;
import com.ddfplus.messages.DdfMarketBase;
import com.ddfplus.messages.DdfMarketBidAsk;
import com.ddfplus.messages.DdfMarketCondition;
import com.ddfplus.messages.DdfMarketDepth;
import com.ddfplus.messages.DdfMarketParameter;
import com.ddfplus.messages.DdfMarketRefresh;
import com.ddfplus.messages.DdfMarketRefreshXML;
import com.ddfplus.messages.DdfMarketTrade;
import com.ddfplus.service.feed.FeedService;
import com.ddfplus.util.DDFDate;
import com.ddfplus.util.ParserHelper;
import com.ddfplus.util.XMLNode;

/**
 * The DataMaster class controls the "mini-database" system that comes with the
 * ddf api. This class makes heavy use of the ddfplus.codec package, and its
 * central method -- processMessage() -- takes in Message subclasses, and
 * maintains a localized (in memory database). This method also returns the
 * object that was created, be it a Quote, BookQuote, or Date object.
 */
public class DataMaster {

	private static final Logger log = LoggerFactory.getLogger(DataMaster.class);

	private final Map<String, BookQuote> bookMap;

	private final Map<String, CumulativeVolume> _cumulativeVolumeTable;

	private final Map<String, Quote> quoteMap;

	private volatile long millisCST = 0L; // If single cloud source

	private final MasterType _type;

	/**
	 * Will be used to request a snapshot refresh for a symbol. Used for "stream
	 * listen <symbol>" commands which do not provide symbol statistics
	 * (hi,low,etc..).
	 */
	private FeedService feedService;

	/**
	 * Constructor which takes in the realtime argument as a boolean.
	 * 
	 * @param type
	 *            An <code>int</code> to denote if the <code>DataMaster</code>
	 *            object is realtime, delayed or, end of day.
	 */
	public DataMaster(MasterType type) {
		_type = type;
		bookMap = new ConcurrentHashMap<String, BookQuote>();
		_cumulativeVolumeTable = new ConcurrentHashMap<String, CumulativeVolume>();
		quoteMap = new ConcurrentHashMap<String, Quote>();
	}

	/**
	 * Processes the feed message.
	 * 
	 * @param message
	 *            raw byte array
	 * @return FeedEvent
	 */
	public FeedEvent processMessage(byte[] message) {
		if ((message == null) || (message.length < 2))
			return null;

		DdfMarketBase msg = Codec.parseMessage(message);

		if (msg == null) {
			log.error("DataMaster.processMessage(" + new String(message) + ") failed.");
			return null;
		}

		return processMessage(msg);
	}

	/**
	 * Processes a Message object. The processed data is then stored in the
	 * internal cache, and can be retrieved via the getQuote() and
	 * getBookQuote() methods.
	 * 
	 * @see #getQuote(String)
	 * @see #getBookQuote(String)
	 * 
	 * @param msg
	 *            DDF Message
	 * 
	 * @return FeedEvent
	 */
	public FeedEvent processMessage(DdfMarketBase msg) {
		if (msg == null) {
			// sanity check
			return null;
		}

		if (_type == MasterType.EndOfDay) {
			// Do not process live messages if EOD cache.
			return null;
		}

		if (log.isDebugEnabled()) {
			log.debug("processMessage: " + msg);
		}

		// Can contain Quotes, Book, and a series of Market Events
		FeedEvent fe = new FeedEvent();
		// Save RAW DDF Message
		fe.setDdfMessage(msg);

		// //////////////////////////////////////////////////////////
		// //////////////////////////////////////////////////////////
		// Process based on Record Type (Message Type)
		// ///////////////////////////////////////////////////////////
		// ///////////////////////////////////////////////////////////
		if (msg.getRecord() == DdfRecord.Timestamp.value()) {
			// /////////////////////////////
			// TimeStamp Beacon
			// TIME!ZONE
			// ///////////////////////////
			millisCST = ((DdfTimestamp) msg).getMillisCST();
			Date d = new Date(millisCST);
			fe.setDate(d);
			return fe;

		} else if (msg.getRecord() == DdfRecord.RefreshOld.value()) {
			// /////////////////////////////////////////////////////////
			// record = % - Older Refresh Message, not Used
			// /////////////////////////////////////////////////////////
		} else if (msg.getRecord() == DdfRecord.RefreshXml.value()) {
			// /////////////////////////////////////////////////////////
			// record = X - Market Data Refresh Messages from Jerq, in XML
			// format
			// ///////////////////////////////////////////////////////////
			recordX_marketRefresh(msg, fe);
			return fe;

		} else if (msg.getRecord() == DdfRecord.Prices.value()) {
			// /////////////////////////////////////////////////////////
			// record = 2 live prices
			// ///////////////////////////////////////////////////////////
			final Quote quote = getQuote(msg.getSymbol());
			if (quote == null) {
				/*
				 * Initial Quote refresh not received yet, get snapshot refresh
				 * from the web service. Until we receive the snapshot we will
				 * not process any updates.
				 * 
				 * ISSUE: Until the snapshot is processed we will drop updates
				 * and the snapshot exchange callback will be old as compared to
				 * the most recent updates that where not processed. How to fix,
				 * queue the updates.
				 */
				if (feedService != null) {
					// Schedule the refresh
					feedService.scheduleQuoteRefresh(msg.getSymbol());
				}
				return fe;
			}
			// Process record 2
			record2_liveprices(msg, quote, fe);
			fe.setQuote(quote);
			return fe;

		} else if (msg.getRecord() == DdfRecord.DepthEndOfDay.value()) {
			// ////////////////////////////////////////////
			// record 3 Market Depth, End of Day
			// ///////////////////////////////////////////
			BookQuote b = record3_book_eod(msg);
			fe.setBook(b);
			return fe;
		}

		log.warn("Unrecognized DDF Message: " + msg);
		return fe;
	}

	/**
	 * Retrieves the active last BookQuote object for the given symbol. This
	 * method will return <B>null</B> if no BookQuote is found for the symbol.
	 * 
	 * @param symbol
	 *            Symbol
	 * @return <B>BookQuote</B>
	 */

	public BookQuote getBookQuote(String symbol) {

		if (symbol == null) {
			return null;
		}

		BookQuote bookQoute = bookMap.get(symbol);

		return bookQoute;

	}

	/**
	 * Retrieves a <code>CumulativeVolume</code> object for the given symbol.
	 * This method will return <code>null</code> if no CumulativeVolume object
	 * is found.
	 * 
	 * @param symbol
	 *            The <code>String</code> symbol to retrieve.
	 * @return The <code>CumulativeVolume</code> object containing the
	 *         cumulative volume data.
	 */

	public CumulativeVolume getCumulativeVolume(String symbol) {
		CumulativeVolume cv = _cumulativeVolumeTable.get(symbol);
		return cv;
	}

	/**
	 * Retrieves the active Quote object for the given symbol. This method will
	 * return <B>null</B> if no Quote object is found for the symbol.
	 * 
	 * @param symbol
	 *            Symbol
	 * @return <B>Quote</B>
	 */
	public Quote getQuote(final String symbol) {

		if (symbol == null) {
			return null;
		}

		Quote quote = quoteMap.get(symbol);

		return quote;

	}

	/**
	 * Returns sizes of caches.
	 * 
	 * @return cache sizes of quote,book,culativeVolume in this order.
	 */
	public int[] getSizes() {
		return new int[] { quoteMap.size(), bookMap.size(), _cumulativeVolumeTable.size() };
	}

	/**
	 * Returns the latest timestamp processed by the DataMaster object.
	 * 
	 * @return <code>long</code> The timestamp in milliseconds
	 */

	public long getMillisCST() {
		return millisCST;
	}

	/**
	 * Adds/Replaces a Quote in the internal table.
	 * 
	 * @param q
	 *            Quote
	 */
	public void putQuote(Quote q) {
		quoteMap.put(q.getSymbolInfo().getSymbol(), q);
	}

	/**
	 * Adds/Replaces a BookQuote in the internal table.
	 * 
	 * @param q
	 *            Quote
	 */
	public void putBookQuote(BookQuote q) {
		bookMap.put(q.getSymbol(), q);
	}

	/**
	 * Adds/Replaces a CumulativeVolume entry in the internal table.
	 * 
	 * @param cv
	 *            Cumm Volume
	 */
	public void putCumulativeVolume(CumulativeVolume cv) {
		_cumulativeVolumeTable.put(cv.getSymbol(), cv);
	}

	public MasterType getMasterType() {
		return _type;
	}

	/**
	 * Clears all of the internal data tables. Used to reset the
	 * <code>DataMaster</code> object to an initial set.
	 */
	public void clearDataCache() {
		_cumulativeVolumeTable.clear();
		bookMap.clear();
		quoteMap.clear();
		millisCST = 0L;
	}

	public void setFeedService(FeedService feedService) {
		this.feedService = feedService;
	}

	void recordX_marketRefresh(DdfMarketBase msg, FeedEvent fe) {
		DdfMarketRefreshXML message = (DdfMarketRefreshXML) msg;
		XMLNode node = message.getXMLNode();
		if (node.getName().equals("QUOTE")) {
			Quote quote = handleRefreshQuote(msg, node);
			fe.setQuote(quote);
		} else if (node.getName().equals("BOOK")) {
			BookQuote bookQuote = handleBookQuoteRefresh(node);
			fe.setBook(bookQuote);
		} else if (node.getName().equals("CV")) {
			CumulativeVolume volume = handleRefreshCumlativeVolume(node);
			fe.setCumVolume(volume);
		} else {
			log.error("wrong refresh message type: {}", node.getName());
		}
	}

	void record2_liveprices(DdfMarketBase msg, Quote quote, FeedEvent fe) {
		// /////////////////////////////////////////////////////////
		// record = 2 Exchange live quote messages
		// ///////////////////////////////////////////////////////////

		// ///////////////////////////
		// Update cached quote
		// ///////////////////////////
		quote.updateLastUpdated();
		// Save Original DDF message
		quote.setMessage(msg);

		/*
		 * Session Logic
		 */
		boolean bDoNotSetFlag = false;
		// Running cumulative Volume
		CumulativeVolume cv = getCumulativeVolume(msg.getSymbol());

		Session pCombinedSession = null;
		Session pElectronicSession = null;
		Session pPreviousSession = null;

		/*
		 * Set current session to the message's "day", probably first message of
		 * the day.
		 */
		if (quote._combinedSession._day == '\0') {
			quote._combinedSession._day = msg.getDay();
		}

		int day1_dayFromCurrentQuoteSession = DDFDate.convertDayCodeToNumber(quote._combinedSession._day);
		int day2_dayFromMessage = DDFDate.convertDayCodeToNumber(msg.getDay());

		if (day1_dayFromCurrentQuoteSession == day2_dayFromMessage) {
			/*
			 * The message is for the current session.
			 */
			pCombinedSession = quote._combinedSession;
			pPreviousSession = quote._previousSession;
			pElectronicSession = quote._electronicSession;
		} else if (msg.getDay() == quote._previousSession._day) {
			/*
			 * Message is for the previous session
			 */
			pCombinedSession = quote._previousSession;
			pPreviousSession = new Session(quote);
			pElectronicSession = new Session(quote);

			/* Don't set any flags, since we're in "yesterday" */
			bDoNotSetFlag = true;
			log.info("Previous session: " + msg);

		} else if ((day2_dayFromMessage > day1_dayFromCurrentQuoteSession)
				|| ((day1_dayFromCurrentQuoteSession - day2_dayFromMessage) > 5)) {
			/*
			 * Message is for a new session/day, so we have changed sessions.
			 */

			/*
			 * Only roll if the combined session has a valid last. The previous
			 * day could have a been a holiday or there was no trades for that
			 * day.
			 */
			if (quote._combinedSession.getLast() == 0.0f) {
				log.info("New session, last == 0: " + msg);
				pCombinedSession = quote._combinedSession;
				pPreviousSession = quote._previousSession;
				pElectronicSession = quote._electronicSession;
			} else {
				// Current Session has a last price, start a new session
				log.info("New session, last > 0: " + msg);

				Session pPrevious = quote._previousSession;
				quote._previousSession = quote._combinedSession;
				/*
				 * A 'T' session is for pre and post equities. These trades
				 * normally do not affect the statistics (hi, low, etc..)
				 */
				quote._electronicSession = new Session(quote, msg.getDay(), 'T');
				// Create new current session
				quote._combinedSession = new Session(quote, msg.getDay(), msg.getSession());
				quote.setFlag('p');

				// Add MarketEvent.PreOpen
				MarketEvent me = addMarketEvent(fe, msg, MarketEventType.PreOpen, quote.getSymbolInfo().getSymbol());
				if (log.isDebugEnabled()) {
					log.debug(me.toString());
				}

				quote._combinedSession._open = 0.0f;
				quote._combinedSession._high = 0.0f;
				quote._combinedSession._low = 0.0f;
				quote._combinedSession.setPrevious(quote._previousSession.getLast());

				if (quote._previousSession.getOpenInterest() == 0) {
					quote._previousSession._openInterest = pPrevious._openInterest;
				}
				pCombinedSession = quote._combinedSession;
				pPreviousSession = quote._previousSession;
				pElectronicSession = quote._electronicSession;

				if (cv != null) {
					// It is a new session clear the cumulative volume for this
					// session
					DDFDate d = DDFDate.fromDayCode(msg.getDay(), cv.getDate());
					cv.setDate(d.getMillisCST());
					cv.getData().clear();
				}
			}
		} else {
			// Should never happen
			return;
		}

		/*
		 * We should update the Quote's Session session value with the inbound
		 * message. Otherwise, we get stuck on something like a Form-T
		 */
		pCombinedSession._session = msg.getSession();

		Session session = quote.createSession(msg.getDay(), msg.getSession());

		if (session == pCombinedSession) {
			session = null;
		}

		if ((session != null) && (session.getPrevious() == 0.0f)) {
			session.setPrevious(pCombinedSession.getPrevious());
		}

		// //////////////////////////////////////////////////////////
		// Process based on record type and subrecord type
		// ///////////////////////////////////////////////////////////
		if (msg instanceof DdfMarketParameter) {
			// ////////////////////////////////////////////
			// record 2, subrecord 0, Price Elements
			// ///////////////////////////////////////////
			record2_subrecord0(msg, fe, quote, bDoNotSetFlag, pCombinedSession, pPreviousSession, session);

		} else if (msg instanceof DdfMarketRefresh) {
			// ////////////////////////////////////////////
			// record 2, subrecord 1,2,3,4,6
			// ///////////////////////////////////////////
			record2_subrecord12346(msg, fe, quote, bDoNotSetFlag, pCombinedSession, pPreviousSession, session);

		} else if ((msg instanceof DdfMarketTrade) && (msg.getSession() == 'T')) {
			// ////////////////////////////////////////////
			// record 2, subrecord 7,T
			// Electronic (Form-T) Trade
			// ///////////////////////////////////////////
			record2_subrecord7T(msg, pCombinedSession, pElectronicSession, session);

		} else if (msg instanceof DdfMarketTrade) {
			// ////////////////////////////////////////////
			// record 2, subrecord 7,Z
			// ///////////////////////////////////////////
			record2_subrecord7Z(msg, quote, bDoNotSetFlag, cv, pCombinedSession, session);

		} else if (msg instanceof DdfMarketBidAsk) {
			// ////////////////////////////////////////////
			// record 2, subrecord 8
			// ///////////////////////////////////////////
			record2_subrecord8(msg, quote, pCombinedSession, session);

		} else if (msg instanceof DdfMarketCondition) {
			// ////////////////////////////////////////////
			// record 2, subrecord 9 Market Condition/Trading Status
			// ///////////////////////////////////////////
			quote.setMarketCondition(((DdfMarketCondition) msg).getMarketCondition());
		}
	}

	void record2_subrecord0(DdfMarketBase msg, FeedEvent fe, final Quote quote, boolean bDoNotSetFlag,
			Session pCombinedSession, Session pPreviousSession, Session session) {
		final DdfMarketParameter msg20 = (DdfMarketParameter) msg;

		boolean bUpdateTimestamp = true;

		final char element = msg20.getElement();
		final char modifier = msg20.getModifier();

		final float f = msg20.getValueAsFloat();
		final int i = msg20.getValueAsInteger();

		if ((msg20.getValueAsFloat() == 0.0f) && (msg20.getValueAsInteger() == 0)) {
			// No data
			return;
		}

		if ((element == QuoteElement.Trade.value())
				&& ((modifier == QuoteElementModifiers.Last.value()) || (modifier == QuoteElementModifiers.Ask.value()) || (modifier == QuoteElementModifiers.Bid
						.value()))) {
			// Trade - Last
			if (session != null) {
				session.setLast(f);
			}
			pCombinedSession.setLast(f);

			if (modifier == QuoteElementModifiers.Last.value()) {
				if (session != null)
					session._tradeTimestamp = determineTimestamp(msg);
				pCombinedSession._tradeTimestamp = determineTimestamp(msg);

				if (!bDoNotSetFlag)
					quote.setFlag('\0');
			}
		} else if ((element == QuoteElement.Trade.value()) && (modifier == QuoteElementModifiers.BidSize.value())) {
			// Bid Size
			quote.setBidSize(i);
			if (_type == MasterType.Realtime)
				bUpdateTimestamp = false;
		} else if ((element == QuoteElement.Trade.value()) && (modifier == QuoteElementModifiers.AskSize.value())) {
			// Ask Size
			quote.setAskSize(i);
			if (_type == MasterType.Realtime)
				bUpdateTimestamp = false;
		} else if ((element == QuoteElement.Trade.value()) && (modifier == QuoteElementModifiers.TradeSize.value())) {
			// Trade Size
			if (session != null) {
				session._tradeSize = i;
				session._volume += i;
			}

			pCombinedSession._tradeSize = i;
			pCombinedSession._volume += i;
		} else if (element == QuoteElement.Ask.value()) {
			// Ask
			if (f == 0.0f) {
				quote.setAsk(0.0f);
				quote.setAskSize(0);
			} else
				quote.setAsk(f);

			if (_type != MasterType.Realtime)
				bUpdateTimestamp = false;
		} else if (element == QuoteElement.Bid.value()) {
			// Bid
			if (f == 0.0f) {
				quote.setBid(0.0f);
				quote.setBidSize(0);
			} else
				quote.setBid(f);

			if (_type != MasterType.Realtime)
				bUpdateTimestamp = false;
		} else if (element == QuoteElement.Close.value()) {
			// Closing Message
			if (session != null) {
				session.setLast(f);
				session._close = f;
			}

			pCombinedSession.setLast(f);
			pCombinedSession._close = f;
			if (!bDoNotSetFlag)
				quote.setFlag('c');
			if (modifier == QuoteElementModifiers.Ask.value())
				quote.setAsk(f);
			else if (modifier == QuoteElementModifiers.Bid.value())
				quote.setBid(f);

			// MarketEvent.Close
			MarketEvent me = addMarketEvent(fe, msg, MarketEventType.Close, quote.getSymbolInfo().getSymbol());
			me.setClose(f);
			if (log.isDebugEnabled()) {
				log.debug(me.toString());
			}

		} else if (element == QuoteElement.Close2.value()) {
			// Closing Message
			if (!bDoNotSetFlag)
				quote.setFlag('c');

			if (session != null) {
				session.setLast(f);
				session._close2 = f;
			}

			pCombinedSession.setLast(f);
			pCombinedSession._close2 = f;

			if (modifier == QuoteElementModifiers.Ask.value())
				quote.setAsk(f);
			else if (modifier == QuoteElementModifiers.Bid.value())
				quote.setBid(f);

		} else if ((element == QuoteElement.OpenInterest.value()) && (modifier == QuoteElementModifiers.Ask.value())) { // Open
																														// Interest
			pPreviousSession._openInterest = i;
			bUpdateTimestamp = false;

		} else if ((element == QuoteElement.Settlement.value()) && (modifier == QuoteElementModifiers.Last.value())) { // Settlement
			if (!bDoNotSetFlag)
				quote.setFlag('s');
			if (session != null) {
				session.setLast(f);
				session._settlement = f;
			}

			pCombinedSession.setLast(f);
			pCombinedSession._settlement = f;

			// MarketEvent.Settlement
			MarketEvent me = addMarketEvent(fe, msg, MarketEventType.Settlement, quote.getSymbolInfo().getSymbol());
			me.setSettlement(f);
			if (log.isDebugEnabled()) {
				log.debug(me.toString());
			}

		} else if ((element == QuoteElement.SettlementDuringMarketTrading.value())
				&& (modifier == QuoteElementModifiers.Last.value())) {
			// Pre-Settlement
			if (session != null)
				session._settlement = f;

			pCombinedSession._settlement = f;

			// MarketEvent.PreSettlement
			MarketEvent me = addMarketEvent(fe, msg, MarketEventType.PreSettlement, quote.getSymbolInfo().getSymbol());
			me.setPreSettlement(f);
			if (log.isDebugEnabled()) {
				log.debug(me.toString());
			}

		} else if ((element == QuoteElement.VWAP.value()) && (modifier == QuoteElementModifiers.Last.value())) { // VWAP
			if (session != null)
				session._vwap = f;

			pCombinedSession._vwap = f;

		} else if (element == QuoteElement.High.value()) {
			// High
			if (session != null)
				session._high = f;

			pCombinedSession._high = f;

			// MarketEvent.High
			MarketEvent me = addMarketEvent(fe, msg, MarketEventType.High, quote.getSymbolInfo().getSymbol());
			me.setHigh(f);
			if (log.isDebugEnabled()) {
				log.debug(me.toString());
			}

		} else if (element == QuoteElement.Low.value()) {
			// Low
			if (session != null)
				session._low = f;

			pCombinedSession._low = f;

			// MarketEvent.Low
			MarketEvent me = addMarketEvent(fe, msg, MarketEventType.Low, quote.getSymbolInfo().getSymbol());
			me.setLow(f);
			if (log.isDebugEnabled()) {
				log.debug(me.toString());
			}

		} else if ((element == QuoteElement.Volume.value()) && (modifier == QuoteElementModifiers.Ask.value())) {
			// Yesterday's Volume
			pPreviousSession._volume = i;
			bUpdateTimestamp = false;

		} else if ((element == QuoteElement.Volume.value())
				&& (modifier == QuoteElementModifiers.CumulativeVolume.value())) {
			// Cumulative Volume
			if (session != null)
				session._volume = i;

			pCombinedSession._volume = i;

		} else if (element == QuoteElement.Open.value()) {
			// Opening of the Market

			if (!bDoNotSetFlag)
				quote.setFlag('\0');

			if (pCombinedSession._open == 0.0f) {
				// Not set before
				if (session != null) {
					session.setLast(f);
					session._open = f;
					session._high = f;
					session._low = f;
				}

				pCombinedSession.setLast(f);
				pCombinedSession._open = f;
				pCombinedSession._high = f;
				pCombinedSession._low = f;

				// MarketEvent.Settlement
				MarketEvent me = addMarketEvent(fe, msg, MarketEventType.Open, quote.getSymbolInfo().getSymbol());
				me.setOpen(f);
				if (log.isDebugEnabled()) {
					log.debug(me.toString());
				}

			}
		} else if (element == QuoteElement.Previous.value()) {
			// Previous

			pPreviousSession.setLast(f);
			pPreviousSession._settlement = f;

			if (session != null)
				session.setPrevious(f);

			pCombinedSession.setPrevious(f);

			if (_type == MasterType.Delayed) {
				Calendar c = java.util.Calendar.getInstance();
				c.setTime(new Date(millisCST));
				c.add(Calendar.MINUTE, (-1 * quote.getMessage().getDelay()));
				pPreviousSession._timestamp = c.getTimeInMillis();
			} else
				pPreviousSession._timestamp = millisCST;

			bUpdateTimestamp = false;
		}

		if (bUpdateTimestamp) {
			if (session != null)
				session._timestamp = determineTimestamp(msg);
			pCombinedSession._timestamp = determineTimestamp(msg);
		}
	}

	void record2_subrecord12346(DdfMarketBase msg, FeedEvent fe, final Quote quote, boolean bDoNotSetFlag,
			Session pCombinedSession, Session pPreviousSession, Session session) {

		boolean bProcessCombined = true;
		boolean clearPreFlag = false;

		if ((msg.getSubRecord() == DdfSubRecord.ExchangeGeneratedRefresh.value())
				|| (msg.getSubRecord() == DdfSubRecord.DdfGeneratedUpdatePriceElementsRefresh.value())
				|| (msg.getSubRecord() == DdfSubRecord.DdfGeneratedActiveSessionRefresh.value())) {

			if ((msg.getSession() == DdfSessionCode.CmeGlobexPitSession.value())) {
				/*
				 * The barchart system has a connection to the old CME ITC Pit
				 * session electronic feed, so if from a Pit Session we do not
				 * want to process based on the logic below.
				 * 
				 * These are refreshes for the day session. Since the API does
				 * not handle day vs night sessions, these refreshes can wipe
				 * out good data. So we'll squash them here.
				 */
				bProcessCombined = false;
			}
		}

		DdfMarketRefresh msg21 = (DdfMarketRefresh) msg;

		Float f = null;
		Long l = null;

		if (bProcessCombined) {
			f = msg21.getAsk();
			if (f != null) {
				quote.setAsk(f);
				if (f == 0.0f)
					quote.setAskSize(0);
			}

			f = msg21.getBid();
			if (f != null) {
				quote.setBid(f);
				if (f == 0.0f)
					quote.setBidSize(0);
			}
		}

		f = msg21.getLast();
		if (f != null) {
			if (session != null)
				session.setLast(f);
			if (bProcessCombined) {
				pCombinedSession.setLast(f);
				clearPreFlag = true;
			}
		}

		f = msg21.getClose();
		if (f != null) {
			if (session != null) {
				session._close = f;
				session.setLast(f);
			}
			if (bProcessCombined) {
				if (!bDoNotSetFlag)
					quote.setFlag('c');

				pCombinedSession._close = f;
				pCombinedSession.setLast(f);
				clearPreFlag = true;

				// MarketEvent.Close
				MarketEvent me = addMarketEvent(fe, msg, MarketEventType.Close, quote.getSymbolInfo().getSymbol());
				me.setClose(f);
				if (log.isDebugEnabled()) {
					log.debug(me.toString());
				}
			}
		} else if (msg.getSubRecord() == DdfSubRecord.DdfGeneratedUpdatePriceElementsRefresh.value()) {
			// subrecord == 2, Update Price elements
			if (bProcessCombined) {
				if (!bDoNotSetFlag)
					quote.setFlag('\0');
			}
		}

		f = msg21.getClose2();
		if (f != null) {
			if (session != null)
				session._close2 = f;

			if (bProcessCombined)
				pCombinedSession._close2 = f;
		}

		f = msg21.getSettle();
		if (f != null) {
			if (f == 0.0f) {
				// zero settlement price
				if (session != null)
					session._settlement = f;

				pCombinedSession._settlement = f;

				if (!bDoNotSetFlag)
					quote.setFlag('\0');
			} else {
				if (session != null) {
					session.setLast(f);
					session._settlement = f;
				}

				if (bProcessCombined) {
					if (!bDoNotSetFlag) {
						quote.setFlag('s');
					}
					pCombinedSession.setLast(f);
					pCombinedSession._settlement = f;
					clearPreFlag = true;
					// MarketEvent.Settlement
					MarketEvent me = addMarketEvent(fe, msg, MarketEventType.Settlement, quote.getSymbolInfo()
							.getSymbol());
					me.setSettlement(f);
					if (log.isDebugEnabled()) {
						log.debug(me.toString());
					}
				}
			}
		}

		f = msg21.getHigh();
		if (f != null) {
			if (session != null)
				session._high = f;

			if (bProcessCombined) {
				pCombinedSession._high = f;
				clearPreFlag = true;
			}
			// MarketEvent.High
			MarketEvent me = addMarketEvent(fe, msg, MarketEventType.High, quote.getSymbolInfo().getSymbol());
			me.setHigh(f);
			if (log.isDebugEnabled()) {
				log.debug(me.toString());
			}
		}

		f = msg21.getLow();
		if (f != null) {
			if (session != null)
				session._low = f;

			if (bProcessCombined) {
				pCombinedSession._low = f;
				clearPreFlag = true;

				// MarketEvent.Low
				MarketEvent me = addMarketEvent(fe, msg, MarketEventType.Low, quote.getSymbolInfo().getSymbol());
				me.setLow(f);
				if (log.isDebugEnabled()) {
					log.debug(me.toString());
				}
			}
		}

		f = msg21.getOpen();
		if (f != null) {
			if (session != null)
				session._open = f;

			if (bProcessCombined) {
				pCombinedSession._open = f;
				clearPreFlag = true;

				// MarketEvent.Open
				MarketEvent me = addMarketEvent(fe, msg, MarketEventType.Open, quote.getSymbolInfo().getSymbol());
				me.setOpen(f);
				if (log.isDebugEnabled()) {
					log.debug(me.toString());
				}
			}
		}

		f = msg21.getOpen2();
		if (f != null) {
			if (session != null)
				session._open2 = f;

			if (bProcessCombined)
				pCombinedSession._open2 = f;
		}

		l = msg21.getPreviousVolume();
		if (l != null) {
			if (bProcessCombined)
				pPreviousSession._volume = l;
		}

		l = msg21.getOpenInterest();
		if (l != null) {
			if (bProcessCombined)
				pPreviousSession._openInterest = l.intValue();
		}

		l = msg21.getVolume();
		if ((l != null) && (l > pCombinedSession._volume)) {
			if (session != null)
				session._volume = l;

			if (bProcessCombined)
				pCombinedSession._volume = l;
		}

		f = msg21.getPrevious();
		if (f != null) {
			if (session != null)
				session.setPrevious(f);

			if (bProcessCombined) {
				pPreviousSession.setLast(f);
				pCombinedSession.setPrevious(f);
			}
		}

		switch (msg.getSubRecord()) {
		case '1': // Exchange generated refresh
			if (session != null)
				session._timestamp = determineTimestamp(msg);
			if (bProcessCombined)
				pCombinedSession._timestamp = determineTimestamp(msg);
			break;
		case '6': // open, statistics refresh
			if (session != null) {
				session._timestamp = determineTimestamp(msg);
				session._tradeTimestamp = session._timestamp;
			}
			if (bProcessCombined) {
				pCombinedSession._timestamp = determineTimestamp(msg);
				pCombinedSession._tradeTimestamp = pCombinedSession._timestamp;
			}
			break;
		}

		if ((quote.getFlag() == 'p') && (clearPreFlag) && (!bDoNotSetFlag))
			quote.setFlag('\0');
	}

	void record2_subrecord7T(DdfMarketBase msg, Session pCombinedSession, Session pElectronicSession, Session session) {
		// Electronic (Form-T) Trade
		if (session != null) {
			session.setLast(((DdfMarketTrade) msg).getTradePrice());
			session._tradeSize = ((DdfMarketTrade) msg).getTradeSize();
			session._volume += ((DdfMarketTrade) msg).getTradeSize();
			session._numTrades++;
			session._priceVolume += ((DdfMarketTrade) msg).getTradePrice() * ((DdfMarketTrade) msg).getTradeSize();
		}

		pElectronicSession.setLast(((DdfMarketTrade) msg).getTradePrice());
		pElectronicSession._tradeSize = ((DdfMarketTrade) msg).getTradeSize();
		pElectronicSession._volume += ((DdfMarketTrade) msg).getTradeSize();
		pElectronicSession._numTrades++;
		pElectronicSession._priceVolume += ((DdfMarketTrade) msg).getTradePrice()
				* ((DdfMarketTrade) msg).getTradeSize();

		pCombinedSession._volume += ((DdfMarketTrade) msg).getTradeSize();

		if (msg.getMillisCST() > 0) {
			if (session != null)
				session._tradeTimestamp = msg.getMillisCST();
			pElectronicSession._tradeTimestamp = msg.getMillisCST();
		} else {
			if (session != null)
				session._tradeTimestamp = determineTimestamp(msg);

			pElectronicSession._tradeTimestamp = determineTimestamp(msg);
		}

		if (session != null)
			session._timestamp = pElectronicSession._tradeTimestamp;

		pElectronicSession._timestamp = pElectronicSession._tradeTimestamp;
	}

	void record2_subrecord7Z(DdfMarketBase msg, final Quote quote, boolean bDoNotSetFlag, CumulativeVolume cv,
			Session pCombinedSession, Session session) {
		if (msg.getSubRecord() == 'Z') {
			if (session != null)
				session._volume += ((DdfMarketTrade) msg).getTradeSize();
			pCombinedSession._volume += ((DdfMarketTrade) msg).getTradeSize();
		} else {
			if (session != null) {
				session.setLast(((DdfMarketTrade) msg).getTradePrice());
				session._tradeSize = ((DdfMarketTrade) msg).getTradeSize();
				session._volume += ((DdfMarketTrade) msg).getTradeSize();
				session._numTrades++;
				session._priceVolume += ((DdfMarketTrade) msg).getTradePrice() * ((DdfMarketTrade) msg).getTradeSize();
			}

			pCombinedSession.setLast(((DdfMarketTrade) msg).getTradePrice());
			pCombinedSession._tradeSize = ((DdfMarketTrade) msg).getTradeSize();
			pCombinedSession._volume += ((DdfMarketTrade) msg).getTradeSize();
			pCombinedSession._numTrades++;
			pCombinedSession._priceVolume += ((DdfMarketTrade) msg).getTradePrice()
					* ((DdfMarketTrade) msg).getTradeSize();

			if (msg.getMillisCST() > 0) {
				if (session != null)
					session._tradeTimestamp = msg.getMillisCST();

				pCombinedSession._tradeTimestamp = msg.getMillisCST();
			} else {
				if (session != null)
					session._tradeTimestamp = determineTimestamp(msg);

				pCombinedSession._tradeTimestamp = determineTimestamp(msg);
			}

			if (session != null)
				session._timestamp = pCombinedSession._tradeTimestamp;

			pCombinedSession._timestamp = pCombinedSession._tradeTimestamp;

			if (cv != null)
				cv.addTrade(((DdfMarketTrade) msg).getTradePrice(), ((DdfMarketTrade) msg).getTradeSize());

			if (!bDoNotSetFlag)
				quote.setFlag('\0');
		}
	}

	void record2_subrecord8(DdfMarketBase msg, final Quote quote, Session pCombinedSession, Session session) {
		DdfMarketBidAsk m = (DdfMarketBidAsk) msg;
		Float f = m.getAskPrice();
		if (f != null) {
			quote.setAsk(f);
			if (f == 0.0f)
				quote.setAskSize(0);
			else {
				Integer i = m.getAskSize();
				if (i != null)
					quote.setAskSize(i);
			}
		}

		f = m.getBidPrice();
		if (f != null) {
			quote.setBid(f);
			if (f == 0.0f)
				quote.setBidSize(0);
			else {
				Integer i = m.getBidSize();
				if (i != null)
					quote.setBidSize(i);
			}
		}

		if (_type == MasterType.Realtime) {
			if (session != null)
				session._timestamp = determineTimestamp(msg);
			pCombinedSession._timestamp = determineTimestamp(msg);
		}
	}

	BookQuote record3_book_eod(DdfMarketBase msg) {
		// Market Depth Messages
		if (msg.getSubRecord() == DdfSubRecord.BookDepth.value()) {
			BookQuote bookQuote = getBookQuote(msg.getSymbol());
			if (bookQuote == null) {
				bookQuote = BookQuote.FromDDFMessage((DdfMarketDepth) msg);
			} else {
				bookQuote.setBaseCode(msg.getBaseCode());

				bookQuote.askcount = ((DdfMarketDepth) msg).getAskCount();
				bookQuote.bidcount = ((DdfMarketDepth) msg).getBidCount();

				float fa[] = ((DdfMarketDepth) msg).getAskPrices();
				int ia[] = ((DdfMarketDepth) msg).getAskSizes();
				for (int i = 0; i < bookQuote.askcount; i++) {
					if (fa[i] != ParserHelper.DDFAPI_NOVALUE)
						bookQuote.askprices[i] = fa[i];
					if (ia[i] != ParserHelper.DDFAPI_NOVALUE)
						bookQuote.asksizes[i] = ia[i];
				}

				fa = ((DdfMarketDepth) msg).getBidPrices();
				ia = ((DdfMarketDepth) msg).getBidSizes();

				for (int i = 0; i < bookQuote.bidcount; i++) {
					if (fa[i] != ParserHelper.DDFAPI_NOVALUE)
						bookQuote.bidprices[i] = fa[i];
					if (ia[i] != ParserHelper.DDFAPI_NOVALUE)
						bookQuote.bidsizes[i] = ia[i];
				}
			}

			if (bookQuote != null) {
				bookMap.put(bookQuote.getSymbol(), bookQuote);
			}

			return bookQuote;
		}
		return null;
	}

	private MarketEvent addMarketEvent(FeedEvent fe, DdfMarketBase msg, MarketEventType type, String symbol) {
		MarketEvent me = new MarketEvent(type);
		me.setDdfMessage(msg);
		me.setSymbol(symbol);
		fe.addMarketEvent(me);
		return me;
	}

	private CumulativeVolume handleRefreshCumlativeVolume(XMLNode node) {
		CumulativeVolume volume = CumulativeVolume.fromXMLNode(node);
		if (volume != null) {
			_cumulativeVolumeTable.put(volume.getSymbol(), volume);
		}
		return volume;
	}

	private BookQuote handleBookQuoteRefresh(XMLNode node) {
		BookQuote bookQuote = BookQuote.fromXMLNode(node);
		if (bookQuote != null) {
			bookMap.put(bookQuote.getSymbol(), bookQuote);
		}
		return bookQuote;
	}

	private Quote handleRefreshQuote(DdfMarketBase msg, XMLNode node) {
		// makes new
		Quote quote = Quote.fromXMLNode(node);
		if (quote != null) {
			quoteMap.put(quote.getSymbolInfo().getSymbol(), quote);
			quote.setMessage(msg);
		}
		return quote;
	}

	/**
	 * Determines the proper timestamp to place on the Quote. If the entry is an
	 * equity, we need to offset to NY time, if the entry is delayed, we need to
	 * offset to the delay time.
	 */

	private long determineTimestamp(DdfMarketBase m) {

		long millis = millisCST;

		int type = getSymbolType(m.getSymbol());

		if ((type > 200) && (type < 300)) {
			// Equity, add 1 Hour
			millis += 60 * 60 * 1000;
		}

		if (_type == MasterType.Delayed) {
			millis -= m.getDelay() * 60 * 1000;
		}

		return millis;
	}

	/**
	 * A fast, quick way to determine if the symbol represents a future, future
	 * option, or equity.
	 */

	private int getSymbolType(String symbol) {
		if (symbol.length() > 0) {
			boolean hasNumbers = false;
			char[] ca = symbol.toCharArray();

			if (ca[0] == '$')
				return 202;
			if (ca[0] == '^')
				return 501;

			for (int i = 0; i < ca.length; i++) {
				if (Character.isDigit(ca[i])) {
					hasNumbers = true;
					break;
				}
			}
			if (!hasNumbers)
				return 201;
			else {
				if (Character.isDigit(ca[ca.length - 1]))
					return 101;
				else
					return 102;
			}
		} else
			return -1;
	}

}
