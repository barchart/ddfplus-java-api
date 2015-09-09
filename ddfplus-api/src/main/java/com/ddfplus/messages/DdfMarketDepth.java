/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.messages;

/**
 * DDF Market Depth
 */
public interface DdfMarketDepth extends DdfMarketBase {

	/**
	 * Gets the ask count.
	 * 
	 * @return the number of ask entries in the book.
	 */

	int getAskCount();

	/**
	 * Gets the bid count.
	 * 
	 * @return the number of bid (offer) entries in the book.
	 */

	int getBidCount();

	/**
	 * Returns the <code>float[]</code> array of ask prices. Note that this
	 * array always has a fixed length. Use the #getAskCount() method to
	 * determine which entries are valid.
	 * 
	 * @return the indexed array of ask prices
	 */

	float[] getAskPrices();

	/**
	 * Returns the <code>int[]</code> array of ask sizes. Note that this array
	 * always has a fixed length. Use the #getAskCount() method to determine
	 * which entries are valid.
	 * 
	 * @return the indexed array of ask sizes
	 */

	int[] getAskSizes();

	/**
	 * Returns the <code>float[]</code> array of bid prices. Note that this
	 * array always has a fixed length. Use the #getBidCount() method to
	 * determine which entries are valid.
	 * 
	 * @return the indexed array of bid prices
	 */

	float[] getBidPrices();

	/**
	 * Returns the <code>int[]</code> array of bid sizes. Note that this array
	 * always has a fixed length. Use the #getBidCount() method to determine
	 * which entries are valid.
	 * 
	 * @return the indexed array of ask prices
	 */

	int[] getBidSizes();

}