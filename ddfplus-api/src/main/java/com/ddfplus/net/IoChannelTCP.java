/**
 * Copyright 2004 - 2015 Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.ddfplus.api.ConnectionEventType;

/**
 * DDF TCP Client.
 * 
 * Connects via TCP to the DDF Servers.
 *
 */
class IoChannelTCP extends IoChannel {

	private static int _nextId = 1;
	private int _id = 0;
	private Socket socket = null;
	private BufferedReader in = null;
	private PrintWriter out = null;
	private AtomicBoolean bDoStop = new AtomicBoolean(false);
	private boolean reconnection;
	private InetAddress currentServerAddress;
	private InetAddress backupServerAddress;
	private String logPrefix;

	public IoChannelTCP(Connection connection) {
		super(connection);
		_id = _nextId++;
		logPrefix = "[INF " + _id + "] ";
	}

	public void run() {

		currentServerAddress = connection.primaryServer;
		backupServerAddress = connection.secondaryServer;

		log.info("[INF " + _id + "] Started JerqTCPListener (TCP Streaming mode) to " + currentServerAddress);

		while (!bDoStop.get()) {

			if (connectToServer()) {

				isRunning = true;

				try {
					// Block here waiting for messages
					while (isRunning) {

						String line = null;
						try {
							line = in.readLine();
						} catch (SocketTimeoutException ste) {
							log.debug("[DEBUG " + _id + "] read timeout");
							continue;
						}

						if (line == null) {
							isRunning = false;
						} else if (line.startsWith(JerqProtocol.JERQ_STOPPED_STREAM)) {
							isRunning = false;
						} else if (line.startsWith(JerqProtocol.JERQ_INFO_START)) {
							;
						} else {
							connection.handleMessage(line);
						}
					} // end read loop

				} catch (SocketException se) {
					log.error("[ERR " + _id + "] JerqTCPListener.run(): communications error - " + se);
				} catch (Exception e) {
					log.error("[ERR " + _id + "] JerqTCPListener.run(): error streaming quotes - " + e);
				}

				// Some type of socket error, fall through to re-connection
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException io) {
						log.error(logPrefix + " Socket close issue: " + io);
					}
					socket = null;
				}
			}

			// We are disconnected
			connection.handleEvent(makeConnectionEvent(ConnectionEventType.DISCONNECTED, reconnection));

			log.warn("[INF " + _id + "] Disconnected from " + currentServerAddress + ":" + connection.port);

			if (!bDoStop.get()) {
				// We are not stopping so this is a re-connection.
				try {
					reconnection = true;
					int reconnectionMs = (RECONNECTION_INTERVAL_SEC * 1000) + ((int) (Math.random() * 10)) * 500;
					log.info("Will attempt reconnection in " + reconnectionMs + " ms");
					Thread.sleep(reconnectionMs);
				} catch (Exception e) {
					log.error(logPrefix + " reconnection issue: " + e.getMessage());
				}
			}

		}

		log.info("[INF " + _id + "] Stopped JerqTCPListener (TCP Streaming mode) to " + currentServerAddress);
	}

	@Override
	protected void sendCommand(String cmd) {
		if (out != null) {
			out.print(cmd + "\n");
			out.flush();
		}
	}

	@Override
	public void disconnectAndShutdown() {
		bDoStop.set(true);
		isRunning = false;
		disconnectFromServer();
	}

	private boolean connectToServer() {

		boolean isSuccess = false;

		synchronized (connection) {

			if (socket != null) {
				log.warn("connectToServer called twice, returning.");
				return false;
			}

			try {

				if (reconnection && backupServerAddress != null) {
					// Swap
					InetAddress temp = currentServerAddress;
					currentServerAddress = backupServerAddress;
					backupServerAddress = temp;
				}

				log.warn("[INF " + _id + "] Connecting via TCP to " + currentServerAddress + ":" + connection.port);

				socket = new Socket(currentServerAddress, connection.port);
				socket.setSoTimeout(createReadTimeout());
				socket.setTcpNoDelay(true);

				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				log.info("[INF " + _id + " tid: " + Thread.currentThread().getId() + "] Connected to "
						+ currentServerAddress + ":" + connection.port + " localAddr: " + socket.getLocalAddress() + ":"
						+ socket.getLocalPort());

				connState = ConnectionState.Connected;
				connection.handleEvent(makeConnectionEvent(ConnectionEventType.CONNECTED, reconnection));

				boolean isDone = false;

				/*
				 * Get passed the Jerq Header:
				 * 
				 * 'Welcome to the Jerq Server Connected to Streaming Quotes
				 * (using JERQP) +++"
				 */
				while (!isDone) {
					String line = in.readLine();
					if (line == null) {
						isDone = true;
					} else if (line.startsWith("+++")) {
						isDone = true;
					}
				}

				out = new PrintWriter(socket.getOutputStream());

				// Send Login Command
				String command = "LOGIN " + connection.username + ":" + connection.password + " VERSION="
						+ connection.getVersion();
				enqueueCommand(command);

				String line = in.readLine();

				log.info("line={}", line);

				if ((line == null) || (line.startsWith(JerqProtocol.JERQ_ERROR_START))) {
					// Login failed
					connection.handleEvent(makeConnectionEvent(ConnectionEventType.LOGIN_FAILED, reconnection));
					log.error("JerqTCPListener[" + _id + "].run(): The server reported an invalid login attempt ("
							+ command + "): " + line);
				} else {
					// Successful login
					connState = ConnectionState.LoggedIn;
					connection.handleEvent(makeConnectionEvent(ConnectionEventType.LOGIN_SUCCESS, reconnection));
					isSuccess = true;
					// On reconnection only
					if (reconnection) {
						resendSubscriptionsOnReconnection();
					}
				}
			} catch (IOException ioe) {
				log.error("JerqTCPListener[" + _id + "].run(): There was an error during connection: " + ioe);
				connection.handleEvent(makeConnectionEvent(ConnectionEventType.CONNECTION_FAILED, reconnection));
			} catch (Exception e) {
				log.error("JerqTCPListener[" + _id + "].run(): There was an uncaught exception during connection: ", e);
				connection.handleEvent(makeConnectionEvent(ConnectionEventType.CONNECTION_FAILED, reconnection));
			}

			if (!isSuccess) {
				if (in != null) {
					try {
						in.close();
					} catch (Exception e) {
						;
					}
				}
				in = null;
				if (out != null) {
					try {
						out.close();
					} catch (Exception e) {
						;
					}
				}
				out = null;
				if (socket != null) {
					try {
						socket.close();
					} catch (Exception e) {
						;
					}
				}
				socket = null;
				connState = ConnectionState.NotConnected;
			}
		}

		return isSuccess;
	}

	private void disconnectFromServer() {

		synchronized (connection) {
			log.warn("[INF " + _id + "] JerqTCPListener Closing");
			try {
				// Call directly here, do not need to go through the queue.
				sendCommand("LOGOFF");

			} catch (Exception e) {
				log.error("JerqTCPListener[" + _id + "].disconnectFromServer(): " + e);
			}

			stopCommandThread();

			connection.handleEvent(makeConnectionEvent(ConnectionEventType.DISCONNECTED, reconnection));

			if (socket != null) {
				try {
					socket.close();
				} catch (IOException ignore) {
				}
				socket = null;
			}
			log.info("[INF " + _id + "] JerqTCPListener Closed.");
		}
	}

}
