/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.api.examples;

import com.ddfplus.util.MessageStore;
import com.ddfplus.util.MessageStoreImpl;

public class MessageStoreReader {

	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Usage: MessageStoreReader fileName");
			System.exit(0);
		}
		String dataFile = args[0];
		MessageStore store = new MessageStoreImpl();
		store.open(dataFile, true);
		store.readMessages(new ReaderCallback());
	}

	private static class ReaderCallback implements MessageStore.ReaderCallback {

		@Override
		public void onMessage(byte[] bytes, int len) {
			String msg = new String(bytes, 0, len);
			System.out.println(msg);

		}

	}
}
