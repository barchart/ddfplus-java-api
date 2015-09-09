/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageStoreImpl implements MessageStore {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private DataInputStream in;
	private DataOutputStream out;
	private String filePath;

	@Override
	public void open(String filePath, boolean read) {
		this.filePath = filePath;
		try {
			log.info("Opening up message store: " + filePath);
			if (read) {
				FileInputStream fis = new FileInputStream(filePath);
				in = new DataInputStream(fis);
			} else {
				FileOutputStream fos = new FileOutputStream(filePath);
				out = new DataOutputStream(fos);
			}
		} catch (FileNotFoundException e) {
			log.error("Could not create message store: " + e);
		}
	}

	@Override
	public void open(String path, String fileName, boolean read) {
		open(path + "/" + fileName, read);
	}

	@Override
	public void close() {
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
			}
		}
	}

	@Override
	public void storeMessage(byte[] bytes) {
		try {
			out.writeInt(bytes.length);
			out.write(bytes);
		} catch (IOException e) {
			log.error("Could not write to message store: " + e);
		}
	}

	@Override
	public void readMessages(ReaderCallback cb) {
		if (in == null) {
			log.error("Message store was not opened");
			return;
		}
		if (cb == null) {
			log.error("Reader callback cannot be null");
			return;
		}
		byte[] buf = new byte[1024];
		try {
			while (in.available() > 0) {
				int len = in.readInt();
				if (buf.length < len) {
					buf = new byte[len];
				}
				in.read(buf, 0, len);
				cb.onMessage(buf, len);
			}
		} catch (IOException e) {
			log.error("Read error on: " + filePath + " error: " + e.getMessage());
		}

	}
}
