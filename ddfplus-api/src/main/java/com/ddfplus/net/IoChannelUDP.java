/**
 * 
 * Copyright 2004 - 2015 Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.StringTokenizer;

import com.ddfplus.api.ConnectionEvent;
import com.ddfplus.api.ConnectionEventType;

/**
 * DDF UDP Client.
 * 
 */
class IoChannelUDP extends IoChannel {

	private volatile boolean bDoStop = false;

	volatile DatagramSocket socket = null;

	public IoChannelUDP(Connection connection, SymbolProvider symbolProvider) {
		super(connection);
		if (symbolProvider == null) {
			throw new IllegalArgumentException("Symbol Provider cannot be null");
		}
		this.symbolProvider = symbolProvider;
	}

	public void run() {

		log.info("[INF] Started JerqUDPListener (UDP Streaming Mode) to " + connection.primaryServer + ":"
				+ connection.port + ".");

		while (!bDoStop) {

			try {

				socket = new DatagramSocket();

				socket.setSoTimeout(createReadTimeoutMs());

				/*
				 * Build Connection UDP Packet (header)
				 */
				String header = connection.username + ":" + connection.password + ":";

				if (connection.version > 1) {
					header += connection.version + ":";
				}

				/*
				 * Subscription symbols
				 */
				String symbols = symbolProvider.getSymbols();
				if (symbols == null || symbols.length() < 1) {
					return;
				}
				if (symbols.indexOf(";") > 0) {
					symbols = symbols.replace(';', ',');
					header += "X";
				} else {
					header += "G";
				}

				// Final command.
				String cmd = header + symbols;
				byte[] ba = cmd.getBytes();

				log.info("Sending UDP Command: " + cmd);

				// Send the UDP command
				DatagramPacket out_packet = new DatagramPacket(ba, ba.length, connection.primaryServer, connection.port);
				socket.send(out_packet);
				isRunning = true;

				boolean bDidEvent = false;

				// Listen for DDF UDP stream
				while (isRunning) {
					try {
						DatagramPacket in_packet = new DatagramPacket(new byte[1460], 1460);
						socket.receive(in_packet);
						StringTokenizer st = new StringTokenizer(new String(in_packet.getData(), 0,
								in_packet.getLength()), "\n");

						// Pass to message handlers
						while (st.hasMoreTokens()) {
							connection.handleMessage(st.nextToken());
						}

						if (!bDidEvent) {
							connection.handleEvent(new ConnectionEvent(ConnectionEventType.CONNECTED));
							bDidEvent = true;
						}
					} catch (SocketException se) {
						log.error("JerqUDPListener.run(recv): " + se);
						isRunning = false;
					} catch (IOException ioe) {
						log.error("JerqUDPListener.run(recv io): " + ioe);
						isRunning = false;
					}
				}
			} catch (IOException ioe) {
				log.error("JerqUDPListener.run(startup): " + ioe);
			}
		}

		connection.handleEvent(new ConnectionEvent(ConnectionEventType.DISCONNECTED));

		log.info("[INF] Stopped JerqUDPListener (UDP Streaming Mode) to " + connection.primaryServer + ".");
	}

	@Override
	public void disconnectAndShutdown() {
		try {
			bDoStop = true;
			if (socket != null) {
				String cmd = connection.username + ":" + connection.password + ":G";
				byte[] ba = cmd.getBytes();
				DatagramPacket out_packet = new DatagramPacket(ba, ba.length, connection.primaryServer, connection.port);
				socket.send(out_packet);
				socket.close();
			}
		} catch (Exception e) {
			log.error("JerqUDPListener.disconnect(): " + e);
		}
	}

	@Override
	protected void sendCommand(String cmd) {

	}
}
