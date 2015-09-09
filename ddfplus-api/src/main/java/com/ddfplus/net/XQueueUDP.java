/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.net;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
class XQueueUDP extends Thread {

	private static final Logger log = LoggerFactory.getLogger(XQueueUDP.class);

	private volatile int _maxSize = 0;
	private final BlockingQueue<byte[]> messageQueue;

	/**
	 */
	private final IoChannelListenUDP reader;

	public XQueueUDP(final IoChannelListenUDP reader) {
		super("XQueue for " + reader.getConnection().getId());
		this.reader = reader;
		messageQueue = new LinkedBlockingQueue<byte[]>();
		this.start();
	}

	public void add(byte[] message) {
		try {
			messageQueue.put(message);
			_maxSize = Math.max(_maxSize, messageQueue.size());
		} catch (Exception e) {
			log.error("", e);
		}
	}

	public int getMaxSize() {
		return _maxSize;
	}

	public int getSize() {
		return messageQueue.size();
	}

	@Override
	public void run() {

		final int priority = 10;
		this.setPriority(priority);

		final String name = this.getName();
		log.info("name={} priority={}", name, priority);

		while (true) {
			try {

				byte[] buffer = messageQueue.take();

				/* will also do message split */
				reader.distributeMessage(buffer);

			} catch (InterruptedException ie) {
				;
			}
		}

	}

}
