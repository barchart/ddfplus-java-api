/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.messages;

/**
 * DDF Market Data BBO
 */
public interface DdfMarketBidAsk extends DdfMarketBase {

	/**
	 * The Ask (Offer) price.
	 * 
	 * @return The Ask price (Offer price)
	 */

	Float getAskPrice();

	/**
	 * The Size of the Ask.
	 * 
	 * @return The Size of the Ask.
	 */

	Integer getAskSize();

	/**
	 * The Bid price.
	 * 
	 * @return The Bid price.
	 */

	Float getBidPrice();

	/**
	 * The Size of the Bid.
	 * 
	 * @return The Size of the Bid.
	 */

	Integer getBidSize();

}