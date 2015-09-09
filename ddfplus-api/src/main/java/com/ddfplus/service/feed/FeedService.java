/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.service.feed;

/**
 * Provides the following feed services:
 * 
 * <ul>
 * <li>Background snapshot/refresh, used when push mode is active
 * </ul>
 *
 */
public interface FeedService {

	void scheduleQuoteRefresh(String symbol);
}
