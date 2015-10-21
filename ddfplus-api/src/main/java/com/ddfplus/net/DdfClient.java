/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.net;

import com.ddfplus.api.BookQuoteHandler;
import com.ddfplus.api.ConnectionEventHandler;
import com.ddfplus.api.FeedHandler;
import com.ddfplus.api.MarketEventHandler;
import com.ddfplus.api.QuoteHandler;
import com.ddfplus.api.TimestampHandler;
import com.ddfplus.db.BookQuote;
import com.ddfplus.db.CumulativeVolume;
import com.ddfplus.db.MarketEvent;
import com.ddfplus.db.Quote;

/**
 * DDF Client API. The DDF client can operate in one of two modes:
 * 
 * <ol>
 * <li>Pull By Symbol
 * <li>Pull By Exchange
 * </ol>
 * 
 */
public interface DdfClient {

	void init();

	/**
	 * Sets the credentials for the refresh/snapshot web service login. Only
	 * used when the API is used in pull by exchange mode where all symbols for
	 * an exchange are requested.
	 *
	 * @see addQuoteExchangeHandler
	 * 
	 * @param username
	 *            Refresh/Snapshot web service login
	 * @param password
	 *            Password
	 */
	void setSnapshotLogin(String username, String password);

	void connect() throws Exception;

	void disconnect();

	/**
	 * Handler for Connection Events
	 * 
	 * @param handler
	 */
	void addConnectionEventHandler(ConnectionEventHandler handler);

	void removeConnectionEventHandler(ConnectionEventHandler handler);

	/**
	 * DDF timestamp message handler.
	 * 
	 * @param handler
	 */
	void addTimestampHandler(TimestampHandler handler);

	void removeTimestampHandler(TimestampHandler handler);

	/**
	 * DDF message handler, called back for all received DDF messages.
	 * 
	 * @param handler
	 */
	void addFeedHandler(FeedHandler handler);

	void removeFeedHandler(FeedHandler handler);

	/**
	 * Market Event Handler
	 * 
	 * @param handler
	 * 
	 * @see MarketEvent
	 */
	void addMarketEventHandler(MarketEventHandler handler);

	void removeMarketEventHandler(MarketEventHandler handler);

	/**
	 * Quote/BBO handler
	 * 
	 */
	void addQuoteHandler(String symbol, QuoteHandler handler);

	void removeQuoteHandler(String symbol, QuoteHandler handler);

	/**
	 * Market Depth Handler
	 */
	void addBookQuoteHandler(String symbol, BookQuoteHandler handler);

	void removeBookQuoteHandler(String symbol, BookQuoteHandler handler);

	/**
	 * Add a quote handler for all symbols at the exchange. This is the callback
	 * for the pull by exchange mode.
	 * 
	 * @param exchangeCode
	 *            DDF Exchange Code
	 * 
	 * @param handler
	 *            Quote Handler for all symbols
	 */
	void addQuoteExchangeHandler(String exchangeCode, QuoteHandler handler);

	/**
	 * Removes the quote handler for the exchange.
	 * 
	 * @param exchangeCode
	 */
	void removeQuoteExchangeHandler(String exchangeCode);

	/**
	 * Retrieves a Quote from the embedded DataManager object.
	 * 
	 * @param symbol
	 *            The <code>String</code> symbol to retrieve
	 * @return quote Symbol Quote
	 */
	Quote getQuote(String symbol);

	/**
	 * Retrieves a BookQuote(Depth) from the embedded DataManager object.
	 * 
	 * 
	 * @param symbol
	 *            The <code>String</code> symbol to retrieve
	 * @return book Symbol Book/Depth
	 */
	BookQuote getBookQuote(String symbol);

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
	CumulativeVolume getCumulativeVolume(String symbol);

}