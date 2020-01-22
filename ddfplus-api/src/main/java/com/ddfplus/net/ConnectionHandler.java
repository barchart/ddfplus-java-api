/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */

package com.ddfplus.net;

import com.ddfplus.api.ConnectionEvent;

/**
 * The ConnectionListener is the primary interface for receiving raw data
 * directly from the Connection object.
 * 
 * @see com.ddfplus.net.Connection
 */

public interface ConnectionHandler {

	/**
	 * These are events from the Connection object regarding the status of the
	 * connection.
	 */
	public void onConnectionEvent(ConnectionEvent event);

	/**
	 * This method is called whenever new data arrives, passing the original
	 * ddfplus message. Your application should then process this message, either
	 * using the <B>ddf.parser</B> package or using your own equivalent.
	 * <P>
	 * For more information on what exactly is contained in ddfplus
	 * messages, please refer to the ddfplus feed specifications.
	 *
	 * @see com.ddfplus.codec
	 */
	public void onMessage(byte[] array);
}
