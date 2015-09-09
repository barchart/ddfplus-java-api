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
 * DDF HTTP refresh client.
 * 
 * Sends an HTTP request to the DDF Server and receives refresh/snapshot
 * response once.
 *
 */
class IoChannelHTTP extends IoChannel {

	private boolean _stop = false;

	public IoChannelHTTP(Connection connection, SymbolProvider symbolProvider) {
		super(connection);
		if (symbolProvider == null) {
			throw new IllegalArgumentException("Symbol Provider cannot be null");
		}
		this.symbolProvider = symbolProvider;
	}

	public void run() {

		isRunning = true;

		log.info("[INF] Started JerqHTTPListener (HTTP refresh mode).");

		String symbols = symbolProvider.getSymbols();
		if (symbols.length() > 0) {

			String url = "http://" + connection.primaryServer.getHostName() + "/stream/apquote.jsx?username="
					+ connection.username + "&password=" + connection.password + "&version=" + connection.version
					+ "&symbols=" + symbols;

			log.info("HTTP URL: " + url);

			while (!_stop) {

				try {

					boolean bDidEvent = false;

					URL u = new URL(url);

					// Send request
					BufferedReader in = new BufferedReader(new InputStreamReader(u.openStream()));

					boolean done = false;

					while (!done) {

						String line = in.readLine();

						if (line == null) {
							done = true;
						} else {
							connection.handleMessage("%" + line);
							if (!bDidEvent) {
								connection.handleEvent(new ConnectionEvent(ConnectionEventType.CONNECTED));
								bDidEvent = true;
							}
						}

					}

					in.close();

					sleep(connection.getRefreshRate());

				} catch (Exception e) {
					log.error("JerqHTTPListener.run(): " + e);
					connection.handleEvent(new ConnectionEvent(ConnectionEventType.CONNECTION_FAILED));
				}
			}
		}

		connection.handleEvent(new ConnectionEvent(ConnectionEventType.DISCONNECTED));

		isRunning = false;

	}

	@Override
	public void disconnectAndShutdown() {
		_stop = true;
	}

	@Override
	protected void sendCommand(String cmd) {

	}

}
