/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.api;

import com.ddfplus.db.MarketEvent;

/**
 * Market Event message handler
 * 
 */
public interface MarketEventHandler {

	/**
	 * Callback for Market Events.
	 * 
	 * @param event
	 *            Market Event
	 */
	void onEvent(MarketEvent event);
}
