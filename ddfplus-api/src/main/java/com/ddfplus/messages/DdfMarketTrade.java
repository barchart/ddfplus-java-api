/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.messages;

/**
 * DDF Market Data Trade
 */
public interface DdfMarketTrade extends DdfMarketBase {

	/**
	 * Gets the trade price.
	 * 
	 * @return The Trade price.
	 */

	float getTradePrice();

	/**
	 * Gets the trade size.
	 * 
	 * @return The size of the trade.
	 */

	int getTradeSize();

}