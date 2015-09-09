/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.api;

import java.util.Date;

/**
 * Timestamp/Beacon handler
 *
 */
public interface TimestampHandler {

	/**
	 * Callback for timestamp message.
	 * 
	 * @param ts
	 */
	void onTimestamp(Date ts);
}
