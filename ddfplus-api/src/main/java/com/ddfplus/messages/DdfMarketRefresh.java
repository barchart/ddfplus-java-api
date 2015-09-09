/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.messages;

import com.ddfplus.enums.QuoteType;

/**
 * Price and statistics market data refresh
 */
public interface DdfMarketRefresh extends DdfMarketBase {

	/**
	 * The Ask (Offer, Sell) price.
	 * 
	 * @return the ask
	 */

	Float getAsk();

	/**
	 * The Bid (Buy) price.
	 * 
	 * @return the bid
	 */

	Float getBid();

	/**
	 * The closing price.
	 * 
	 * @return the close
	 */

	Float getClose();

	/**
	 * The second of the closing range price.
	 * 
	 * @return the close2
	 */

	Float getClose2();

	/**
	 * The High price.
	 * 
	 * @return the high
	 */

	Float getHigh();

	/**
	 * The Last price.
	 * 
	 * @return the last
	 */

	Float getLast();

	/**
	 * The Low price.
	 * 
	 * @return the low
	 */

	Float getLow();

	/**
	 * The Open price.
	 * 
	 * @return the open
	 */

	Float getOpen();

	/**
	 * The second of an opening range price.
	 * 
	 * @return the open2
	 */

	Float getOpen2();

	/**
	 * The Open Interest.
	 * 
	 * @return the open interest
	 */

	Long getOpenInterest();

	/**
	 * The Previous Settle.
	 * 
	 * @return the previous
	 */

	Float getPrevious();

	/**
	 * The Volume for the Previous Session.
	 * 
	 * @return the previous volume
	 */

	Long getPreviousVolume();

	/**
	 * The settlement price.
	 * 
	 * @return the settle
	 */

	Float getSettle();

	/**
	 * The Volume for the Current Session.
	 * 
	 * @return the volume
	 */

	Long getVolume();

	/**
	 * 
	 */
	QuoteType getQuoteType();

}