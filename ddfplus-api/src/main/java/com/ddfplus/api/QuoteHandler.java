/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.api;

import com.ddfplus.db.Quote;

/**
 * Quote message handler.
 *
 */
public interface QuoteHandler {

	/**
	 * Callback for Quote
	 * 
	 * @param quote
	 */
	void onQuote(Quote quote);

	/**
	 * Callback for Quote.
	 * @param quote
	 * @param refreshMessage True if the quote is from a snapshot or refresh message.
	 */
	void onQuote(Quote quote,boolean refreshMessage);

	/**
	 * If set will request a Snapshot Quote only.
	 *
	 * @return true if request Snapshot Quote
	 */
	boolean isSnapshotRequest();

}
