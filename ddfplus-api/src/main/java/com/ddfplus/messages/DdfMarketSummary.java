/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.messages;

/**
 * 
 */
public interface DdfMarketSummary extends DdfMarketBase {

	/**
	 * Gets the close.
	 * 
	 * @return the close
	 */
	float getClose();

	/**
	 * Gets the high.
	 * 
	 * @return the high
	 */
	float getHigh();

	/**
	 * Gets the low.
	 * 
	 * @return the low
	 */
	float getLow();

	/**
	 * Gets the open.
	 * 
	 * @return the open
	 */
	float getOpen();

	/**
	 * Gets the open interest.
	 * 
	 * @return the open interest
	 */
	long getOpenInterest();

	/**
	 * Gets the volume.
	 * 
	 * @return the volume
	 */
	long getVolume();

}