/**
 * Copyright 2004 - 2015 Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import com.ddfplus.api.ConnectionEvent;
import com.ddfplus.api.ConnectionEventType;

/**
 * DDF HTTP Stream Client.
 * 
 * HTTPSTREAM sends a web request that effectively long-polls and receives data
 * via chunked messages. So HTTPSTREAM actually streams DDF data. HTTP is an X
 * second refresh system.
 * 
 * 
 */
class IoChannelHTTPSTREAM extends IoChannel {

	private BufferedReader in = null;

	public IoChannelHTTPSTREAM(Connection connection, SymbolProvider symbolProvider) {
		super(connection);
		if (symbolProvider == null) {
			throw new IllegalArgumentException("Symbol Provider cannot be null");
		}
		this.symbolProvider = symbolProvider;
	}

	public void run() {

		isRunning = true;

		log.info("[INF] Started JerqSHTTPListener (HTTP Sreaming mode).");

		String symbols = symbolProvider.getSymbols();
		if (symbols == null || symbols.length() < 1) {
			return;
		}
		try {

			URL url = new URL("http://" + connection.primaryServer.getHostName() + "/stream/apstream.jsx?username="
					+ connection.username + "&password=" + connection.password + "&symbols=" + symbols + "&version="
					+ connection.version);

			log.info("HTTP URL: " + url);

			// Send request
			in = new BufferedReader(new InputStreamReader(url.openStream()));

			boolean isDone = false;

			boolean bDidEvent = false;

			while (isRunning && !isDone) {

				String line = in.readLine();

				if (line == null) {
					isDone = true;
				} else {
					connection.handleMessage(line);
					if (!bDidEvent) {
						connection.handleEvent(new ConnectionEvent(ConnectionEventType.CONNECTED));
						bDidEvent = true;
					}
				}
			}
		} catch (Exception e) {
			log.info("[SHTTP] Error streaming quotes - " + e);
			connection.handleEvent(new ConnectionEvent(ConnectionEventType.CONNECTION_FAILED));
		}

		connection.handleEvent(new ConnectionEvent(ConnectionEventType.DISCONNECTED));

		log.error("[INF] Stopped JerqSHTTPListener (HTTP Sreaming mode).");

		if (isRunning) {
			connection.restart();
		}

	}

	@Override
	public void disconnectAndShutdown() {
		try {
			isRunning = false;
			in.close();
		} catch (Exception e) {
			log.error("JerqHTTPListener.disconnect(): " + e);
		}
	}

	@Override
	protected void sendCommand(String cmd) {

	}
}
