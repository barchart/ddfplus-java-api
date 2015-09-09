/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.net;

import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 */
class XQueue implements Runnable {

	/**
	 */
	private Connection _connection = null;
	private final LinkedBlockingQueue<byte[]> _queue;

	public XQueue(Connection connection) {
		_connection = connection;
		_queue = new LinkedBlockingQueue<byte[]>();
		Thread t = new Thread(this);
		t.start();
	}

	public void add(byte[] item) {
		try {
			_queue.put(item);
		} catch (InterruptedException ie) {
			;
		}
	}

	public int getSize() {
		return _queue.size();
	}

	@Override
	public void run() {
		while (true) {
			try {
				byte[] buffer = _queue.take();
				_connection.newQueueMessage(buffer);
			} catch (InterruptedException ie) {
				;
			}
		}
	}
}
