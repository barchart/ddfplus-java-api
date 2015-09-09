/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.api;

/**
 * Connection Event handler.
 * 
 */
public interface ConnectionEventHandler {

	/**
	 * These are events from the Connection object regarding the status of the
	 * connection.
	 * 
	 * @param event
	 *            Connection Event
	 */
	public void onEvent(ConnectionEvent event);

}
