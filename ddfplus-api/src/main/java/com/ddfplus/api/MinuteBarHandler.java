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
 * Minute Bar Handler.
 * 
 * Will be called back when open,high,low,close message is received.
 * 
 * %<ohlc symbol="GOOG" day="3" interval="1" time="2017-05-30 14:40" basecode=
 * "A" open="97478" high="97501" low="97478" close="97496" volume="1910" />
 *
 */
public interface MinuteBarHandler {

	/**
	 * Received Message.
	 * 
	 * @param ohlc
	 *            DDF Message
	 */
	void onOhlc(Ohlc ohlc);
}
