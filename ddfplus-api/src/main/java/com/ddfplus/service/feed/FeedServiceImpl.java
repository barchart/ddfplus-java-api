/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.service.feed;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ddfplus.api.QuoteHandler;
import com.ddfplus.db.DataMaster;
import com.ddfplus.db.Quote;
import com.ddfplus.db.Session;
import com.ddfplus.db.SymbolInfo;
import com.ddfplus.enums.MarketConditionType;
import com.ddfplus.service.usersettings.UserSettings;
import com.ddfplus.util.DDFDate;
import com.ddfplus.util.ParserHelper;
import com.ddfplus.util.XmlUtil;

/**
 * Feed Service Implementation
 *
 */
public class FeedServiceImpl implements FeedService {

	private static final int REQUEST_THREAD_INITIAL_DELAY_MS = 100;

	private static final long REQUEST_THREAD_TIMEOUT_MS = 500;

	private static final int REQUEST_MAX_SYMBOLS = 200;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final UserSettings userSettings;

	private final String baseUrl;

	private DataMaster datamaster;

	private BlockingQueue<FeedCmd> requestQ = new LinkedBlockingQueue<FeedCmd>(100);

	private ScheduledExecutorService es = Executors.newScheduledThreadPool(1);

	private RequestThread requestThread;

	private Map<String, QuoteHandler> quoteExchangehandlers;

	// for testing only
	private String queryUrl;

	public FeedServiceImpl(DataMaster datamaster, UserSettings userSettings) {
		this(datamaster, userSettings, null);
	}

	public FeedServiceImpl(DataMaster datamaster, UserSettings userSettings,
			Map<String, QuoteHandler> quoteExchangehandlers) {
		this.datamaster = datamaster;
		this.userSettings = userSettings;
		this.quoteExchangehandlers = quoteExchangehandlers;
		this.baseUrl = buildBaseUrl(this.userSettings);
		requestThread = new RequestThread(requestQ);
		es.scheduleAtFixedRate(requestThread, REQUEST_THREAD_INITIAL_DELAY_MS, REQUEST_THREAD_TIMEOUT_MS,
				TimeUnit.MILLISECONDS);
		log.info("Snapshot/Refresh is enabled for user: " + userSettings.getUserName());
	}

	@Override
	public void scheduleQuoteRefresh(String symbol) {
		FeedCmd cmd = new FeedCmd(CMDS.QuoteRefresh, symbol);
		requestQ.offer(cmd);
	}

	/*
	 * Queries web service for snapshot refresh, parses it, and updates the
	 * caches.
	 */
	void getQuotes(List<String> symbolList) {
		// Get Quote Refresh(s)
		String query = buildQueryString(symbolList);
		if (log.isDebugEnabled()) {
			log.debug("Sending refresh request: " + query);
		}
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(query);

			if (log.isDebugEnabled()) {
				log.debug(XmlUtil.printDocument(doc));
			}

			Element root = doc.getDocumentElement();
			NodeList list = root.getElementsByTagName("QUOTE");
			for (int i = 0; i < list.getLength(); i++) {
				try {
					Node node = list.item(i);
					Quote q = processQuote(node);
					/*
					 * We have the snapshot now, callback the handler
					 */
					if (quoteExchangehandlers != null) {
						String ddfExchange = q.getDDFExchange();
						QuoteHandler eh = quoteExchangehandlers.get(ddfExchange);
						if (eh != null) {
							eh.onQuote(q);
						}
					}
				} catch (Exception e2) {
					log.error("Parsing Quote Error: " + e2.getMessage());
				}
			}

			// Not supported since DDF provides the whole book.
			list = root.getElementsByTagName("BOOK");

			// Not supported yet, do we need it?
			list = root.getElementsByTagName("CV");
			for (int i = 0; i < list.getLength(); i++) {
				try {
					Node node = list.item(i);
					processCV(node);
				} catch (Exception e2) {
					log.error("CV Parsing Error: " + e2.getMessage());
				}
			}

		} catch (ParserConfigurationException e) {
			log.error("Parser configuration on snapshot/refresh request: " + e.getMessage());
		} catch (SAXException e) {
			log.error("Could not parse snapshot/refresh request: " + e.getMessage());
		} catch (IOException e) {
			log.error("IO issue with snaphost/refresh request: " + e.getMessage());
		}
	}

	private void processCV(Node node) {

	}

	private void processBook(Node node) {

	}

	private Quote processQuote(Node node) {
		Element element = (Element) node;
		SymbolInfo symbolInfo = new SymbolInfo(element.getAttribute("symbol"), //
				element.getAttribute("name"), //
				element.getAttribute("exchange"), //
				element.getAttribute("basecode").charAt(0), //
				((element.getAttribute("pointvalue").length() > 0)
						? Float.parseFloat(element.getAttribute("pointvalue")) : 1.0f),
				((element.getAttribute("tickincrement").length() > 0)
						? Integer.parseInt(element.getAttribute("tickincrement")) : 1));

		Quote q = new Quote(symbolInfo);

		String s = element.getAttribute("ddfexchange");
		if (s.length() > 0)
			q.setDDFExchange(s);

		s = element.getAttribute("flag");
		if (s.length() > 0)
			q.setFlag(s.charAt(0));

		s = element.getAttribute("marketcondition");
		if ((s != null) && (s.length() > 0))
			q.setMarketCondition(MarketConditionType.getByCode(s.charAt(0)));

		s = element.getAttribute("lastupdate");
		if (s.length() > 0)
			q.setLastUpdated(new DateTime(DDFDate.fromDDFString(s).getMillisCST()));

		s = element.getAttribute("bid");
		if (s.length() > 0)
			q.setBid(ParserHelper.string2float(s, q.getSymbolInfo().getBaseCode()));

		s = element.getAttribute("bidsize");
		if (s.length() > 0)
			q.setBidSize(ParserHelper.string2int(s));

		s = element.getAttribute("ask");
		if (s.length() > 0)
			q.setAsk(ParserHelper.string2float(s, q.getSymbolInfo().getBaseCode()));

		s = element.getAttribute("asksize");
		if (s.length() > 0)
			q.setAskSize(ParserHelper.string2int(s));

		s = element.getAttribute("mode");
		if (s.length() > 0)
			q.setPermission(s.charAt(0));

		// Sessions
		NodeList sessions = element.getElementsByTagName("SESSION");
		for (int i = 0; i < sessions.getLength(); i++) {
			Element el = (Element) sessions.item(i);
			Session session = new Session(q);
			parseSession(session, q, el);
			if (el.getAttribute("id").equals("combined")) {
				q.setCombinedSession(session);
			} else if (el.getAttribute("id").equals("previous")) {
				q.setPreviousSession(session);
			}
		}

		// Update the Cache
		datamaster.putQuote(q);

		return q;
	}

	private void parseSession(Session session, Quote parent, Element element) {
		String s = element.getAttribute("day");
		if (s.length() > 0)
			session.setDayCode(s.charAt(0));

		s = element.getAttribute("session");
		if (s.length() > 0)
			session.setSessionCode(s.charAt(0));

		s = element.getAttribute("timestamp");
		if (s.length() > 0)
			session.setTimeInMillis(DDFDate.fromDDFString(s).getMillisCST());

		s = element.getAttribute("tradetime");
		if (s.length() > 0)
			session.setTradeTimestamp(DDFDate.fromDDFString(s).getMillisCST());

		s = element.getAttribute("open");
		if (s.length() > 0)
			session.setOpen(ParserHelper.string2float(s, parent.getSymbolInfo().getBaseCode()));

		s = element.getAttribute("open2");
		if (s.length() > 0)
			session.setOpen2(ParserHelper.string2float(s, parent.getSymbolInfo().getBaseCode()));

		s = element.getAttribute("high");
		if (s.length() > 0)
			session.setHigh(ParserHelper.string2float(s, parent.getSymbolInfo().getBaseCode()));

		s = element.getAttribute("low");
		if (s.length() > 0)
			session.setLow(ParserHelper.string2float(s, parent.getSymbolInfo().getBaseCode()));

		s = element.getAttribute("last");
		if (s.length() > 0)
			session.setLast(ParserHelper.string2float(s, parent.getSymbolInfo().getBaseCode()));

		s = element.getAttribute("close");
		if (s.length() > 0)
			session.setClose(ParserHelper.string2float(s, parent.getSymbolInfo().getBaseCode()));

		s = element.getAttribute("close2");
		if (s.length() > 0)
			session.setClose2(ParserHelper.string2float(s, parent.getSymbolInfo().getBaseCode()));

		s = element.getAttribute("previous");
		if (s.length() > 0)
			session.setPrevious(ParserHelper.string2float(s, parent.getSymbolInfo().getBaseCode()));

		s = element.getAttribute("settlement");
		if (s.length() > 0)
			session.setSettlement(ParserHelper.string2float(s, parent.getSymbolInfo().getBaseCode()));

		s = element.getAttribute("tradesize");
		if (s.length() > 0)
			session.setLastSize(ParserHelper.string2int(s));

		s = element.getAttribute("openinterest");
		if (s.length() > 0)
			session.setOpenInterest(ParserHelper.string2int(s));

		s = element.getAttribute("volume");
		if (s.length() > 0)
			session.setVolume(ParserHelper.string2int(s));

		s = element.getAttribute("numtrades");
		if (s != null)
			session.setNumberOfTrades(ParserHelper.string2int(s));

		s = element.getAttribute("pricevolume");
		try {
			if (s != null)
				session.setPriceVolume(Double.parseDouble(s));
		} catch (Exception e) {
			;
		}

		s = element.getAttribute("vwap");
		try {
			if ((s != null) && (s.length() > 0))
				session.setVWAP(ParserHelper.string2float(s, parent.getSymbolInfo().getBaseCode()));
		} catch (Exception e) {
			;
		}

	}

	private String buildQueryString(List<String> symbols) {
		// for testing only
		if (queryUrl != null) {
			return queryUrl;
		}
		StringBuilder sb = new StringBuilder(baseUrl);

		for (int i = 0; i < symbols.size(); i++) {
			try {
				String s = symbols.get(i);
				// kill whitespace
				s = s.replaceAll("\\s+", "");
				if (i == 0) {
					sb.append(URLEncoder.encode(s, "UTF-8"));
				} else {
					sb.append(",").append(URLEncoder.encode(s, "UTF-8"));
				}
			} catch (UnsupportedEncodingException e) {
				log.error("URL encode error: ", e);
			}
		}
		return sb.toString();
	}

	private String buildBaseUrl(UserSettings us) {
		String s = us.getStreamPrimaryServer();
		return "http://" + s + "/stream/quotes.jsx?" + "username=" + us.getUserName() + "&" + "password="
				+ us.getPassword() + "&symbols=";
	}

	private static class FeedCmd {
		private CMDS cmd;
		private String symbol;

		public FeedCmd(CMDS cmd, String symbol) {
			this.cmd = cmd;
			this.symbol = symbol;
		}

		public CMDS getCmd() {
			return cmd;
		}

		public void setCmd(CMDS cmd) {
			this.cmd = cmd;
		}

		public String getSymbol() {
			return symbol;
		}

		public void setSymbol(String symbols) {
			this.symbol = symbols;
		}
	}

	// for testing
	void setQueryUrl(String url) {
		this.queryUrl = url;
	}

	private class RequestThread implements Runnable {

		private BlockingQueue<FeedCmd> cmdQ;
		private List<String> symbols = new ArrayList<String>();

		public RequestThread(BlockingQueue<FeedCmd> q) {
			cmdQ = requestQ;
		}

		@Override
		public void run() {
			symbols.clear();
			while (cmdQ.peek() != null) {
				try {
					FeedCmd cmd = cmdQ.poll();
					if (cmd != null) {
						switch (cmd.getCmd()) {
						case QuoteRefresh:
							if (symbols.size() < REQUEST_MAX_SYMBOLS) {
								symbols.add(cmd.getSymbol());
							} else {
								// Send it
								getQuotes(symbols);
								symbols.clear();
							}
							break;
						default:
						}
					}
				} catch (Exception e) {
					log.error("Feed request error: " + e.getMessage());
				}
			}
			// Q is empty now, send any remaining requests
			if (symbols.size() > 0) {
				getQuotes(symbols);
			}
		}
	}

	private enum CMDS {
		QuoteRefresh;
	}
}
