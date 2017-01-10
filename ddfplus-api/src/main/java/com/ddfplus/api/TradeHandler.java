/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.api;

import com.ddfplus.messages.DdfMarketTrade;

/**
 * Trade message handler.
 *
 */
public interface TradeHandler {

	/**
	 * Callback for Trades
	 * 
	 * @param trade
	 *            Trade
	 */
	void onTrade(DdfMarketTrade trade);
}
