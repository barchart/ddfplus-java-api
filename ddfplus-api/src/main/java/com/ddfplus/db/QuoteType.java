/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.db;

public enum QuoteType {

	UNKNOWN,

	/** DDF Timestamp */
	DATE,
	/**
	 * Trade
	 */
	TRADE,
	/**
	 * Book/Depth
	 */
	BOOK,
	/** Cumulative Volume */
	VOLUME,
}
