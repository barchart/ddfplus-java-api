/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.util;

public interface MessageStore {

	void open(String filePath, boolean read);

	void open(String path, String fileName, boolean read);

	void close();

	void storeMessage(byte[] bytes);

	void readMessages(ReaderCallback cb);

	public interface ReaderCallback {
		void onMessage(byte[] bytes, int len);
	}
}
