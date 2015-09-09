/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.api;

import com.ddfplus.messages.DdfMessageBase;

/**
 * DDF Message handler
 * 
 *
 */
public interface FeedHandler {

	/**
	 * Received DDF Message.
	 * 
	 * @param msg
	 *            DDF Message
	 */
	void onMessage(DdfMessageBase msg);
}
