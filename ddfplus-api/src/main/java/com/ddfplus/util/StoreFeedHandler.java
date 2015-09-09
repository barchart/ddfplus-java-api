/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.util;

import com.ddfplus.api.FeedHandler;
import com.ddfplus.enums.DdfMessageType;
import com.ddfplus.messages.DdfMessageBase;

public class StoreFeedHandler implements FeedHandler {

	private MessageStore store;

	public StoreFeedHandler(MessageStore store) {
		this.store = store;
	}

	@Override
	public void onMessage(DdfMessageBase msg) {
		if (msg.getMessageType() == DdfMessageType.Timestamp) {
			return;
		}
		store.storeMessage(msg.getBytes());
	}
}
