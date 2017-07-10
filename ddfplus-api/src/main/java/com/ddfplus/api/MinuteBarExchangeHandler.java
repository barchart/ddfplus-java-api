/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.api;

import com.ddfplus.db.Ohlc;

/**
 * 
 * Minute Bar Exchange Handler.
 * 
 */
public interface MinuteBarExchangeHandler {

	/**
	 * Exchange code for minute bars.
	 * 
	 * @return exchangeCode Barchart Exchange Code
	 */
	String getExchange();

	/**
	 * Received Message.
	 * 
	 * @param ohlc
	 *            DDF Message
	 */
	void onOhlc(Ohlc ohlc);
}
