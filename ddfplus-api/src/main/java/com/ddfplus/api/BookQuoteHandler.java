/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.api;

import com.ddfplus.db.BookQuote;

/**
 * Handler for Book/Depth messages.
 *
 */
public interface BookQuoteHandler {

	/**
	 * Callback for book/depth message.
	 * 
	 * @param bookQuote
	 *            Depth Book
	 */
	void onBookQuote(BookQuote bookQuote);
}
