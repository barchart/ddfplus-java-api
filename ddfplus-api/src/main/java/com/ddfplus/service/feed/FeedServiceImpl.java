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
import com.ddfplus.util.XMLNode;
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
					XMLNode n = XMLNode.fromElement((Element)node);
					Quote q = Quote.fromXMLNode(n);

					// Update the Cache
					datamaster.putQuote(q);
					
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
