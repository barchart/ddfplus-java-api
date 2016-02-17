/**
 * Copyright 2004 - 2015 Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.net;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.container.jdk.client.JdkClientContainer;

import com.ddfplus.api.ConnectionEventType;
import com.ddfplus.enums.ConnectionType;
import com.ddfplus.util.ASCII;

/**
 * DDF Web Socket Client using the Tyrus library.
 * 
 */
class IoChannelWSS extends IoChannel {

	private static int READ_BUF_SIZE = 2000000;

	private static final String WS_PREFIX = "ws://";

	private static final String WSS_PREFIX = "wss://";

	private static final String WSS_POSTFIX = "/jerq";

	private AtomicBoolean running = new AtomicBoolean(true);

	private CountDownLatch connectionLatch;

	private CountDownLatch sessionFinishedLatch;

	private JerqWssEndPoint endpoint;

	private URI uri;

	private ClientManager clientManager;

	private ClientEndpointConfig clientEndpointConfig;

	private WssMessageTextStreamHandler messageHandler;

	private boolean reconnection;

	public IoChannelWSS(Connection connection, SymbolProvider symbolProvider) {
		super(connection);
		if (symbolProvider == null) {
			throw new IllegalArgumentException("Symbol Provider cannot be null");
		}
		this.symbolProvider = symbolProvider;

		// Build Web Service URL
		String addr = null;
		if (connection.getConnectionType() == ConnectionType.WS) {
			addr = WS_PREFIX + connection.primaryServer.getHostName() + ":" + connection.getPort() + WSS_POSTFIX;
		} else {
			addr = WSS_PREFIX + connection.primaryServer.getHostName() + WSS_POSTFIX;
		}
		try {
			uri = new URI(addr);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Could not create Web Socket URI: " + e.getMessage());
		}

	}

	@Override
	public void run() {

		log.info("Started Jerq Web Socket Client to: " + connection.primaryServer);

		while (running.get()) {
			try {
				sessionFinishedLatch = new CountDownLatch(1);
				boolean ret = connectToServer();
				if (ret == false || connState == ConnectionState.NotConnected) {

					// Connect failed, retry
					log.warn("WebSocket connection issue, retrying in: " + RECONNECTION_INTERVAL_SEC + " seconds.");
					sleep(RECONNECTION_INTERVAL_SEC * 1000);
					continue;
				}
				// Connected, wait until session is closed.
				sessionFinishedLatch.await();
				log.info("Web socket session has closed.");
				if (reconnection) {
					log.warn("Awaiting " + RECONNECTION_INTERVAL_SEC + " seconds before reconnection.");
					sleep(RECONNECTION_INTERVAL_SEC * 1000);
				}

			} catch (Exception e) {
				log.error("WebSocket error runState: " + running.get() + " error: ", e);
			}
		}

		if (clientManager != null) {
			clientManager.shutdown();
			clientManager = null;
		}

		log.info("Stopped Jerq Web Socket Client to " + connection.primaryServer);

	}

	@Override
	public void disconnectAndShutdown() {
		if (clientManager != null) {
			clientManager.shutdown();
			clientManager = null;
		}
		running.set(false);
		sessionFinishedLatch.countDown();
	}

	@Override
	protected void sendCommand(String cmd) {
		endpoint.sendCmd(cmd);
	}

	/*
	 * Initializes web socket and attempts a connection.
	 */
	private boolean connectToServer()
			throws URISyntaxException, DeploymentException, IOException, InterruptedException {

		/*
		 * Create WS client using built in Java Asynchronous Channel API (Not
		 * Grizzly container). Just call createClient() if you want the default
		 * Grizzly implementation.
		 */
		clientManager = ClientManager.createClient(JdkClientContainer.class.getName());

		int idleTimeOutMs = createReadTimeoutMs();
		clientManager.setDefaultMaxSessionIdleTimeout(idleTimeOutMs);

		// // Note: Did not work
		// client.getProperties().put("org.glassfish.tyrus.server.tracingType",
		// "ALL");
		// client.getProperties().put("org.glassfish.tyrus.server.tracingThreshold",
		// "TRACE");
		// client.getProperties().put("org.glassfish.tyrus.incomingBufferSize",
		// 6000000);
		if (log.isDebugEnabled()) {
			clientManager.getProperties().put(ClientProperties.LOG_HTTP_UPGRADE, true);
		}

		clientEndpointConfig = ClientEndpointConfig.Builder.create().build();

		messageHandler = new WssMessageTextStreamHandler();

		endpoint = new JerqWssEndPoint(messageHandler);

		connectionLatch = new CountDownLatch(1);

		// Attempt Connection
		log.info("Starting connection to: " + uri);
		try {
			Session session = clientManager.connectToServer(endpoint, clientEndpointConfig, uri);
			// Log Config
			log.info("Endpoint: sendTimeout: " + clientManager.getDefaultAsyncSendTimeout() + " binaryBufSize: "
					+ clientManager.getDefaultMaxBinaryMessageBufferSize() + " sessionIdleTimeOutMs: "
					+ clientManager.getDefaultMaxSessionIdleTimeout() + " textBufSize: "
					+ clientManager.getDefaultMaxTextMessageBufferSize());

		} catch (javax.websocket.DeploymentException e) {
			log.error("Deployment Exception: Could not connect to: " + uri + " reason: " + e.getMessage());
			return false;
		} catch (Exception e) {
			log.error("Exception: Could not connect to: " + uri + " reason: " + e.getMessage());
			return false;
		}

		boolean ret = connectionLatch.await(CONNECTION_TIMEOUT_SEC, TimeUnit.SECONDS);
		return ret;
	}

	private void closeSession() {
		clientManager.shutdown();
		connState = ConnectionState.NotConnected;
		if (running.get()) {
			/*
			 * We are not shutting down, so reconnect.
			 */
			reconnection = true;
		}
		sessionFinishedLatch.countDown();
	}

	private class JerqWssEndPoint extends Endpoint {

		private WssMessageTextStreamHandler handler;
		private Session session;

		public JerqWssEndPoint(WssMessageTextStreamHandler messageHandler) {
			this.handler = messageHandler;
		}

		@Override
		public void onOpen(Session session, EndpointConfig config) {
			this.session = session;
			log.info("Endpoint connected to: " + uri);

			session.addMessageHandler(handler);
			connState = ConnectionState.Connecting;

		}

		@Override
		public void onClose(Session session, CloseReason closeReason) {
			log.warn("Endpoint close, reason: " + closeReason);
			// clean up
			closeSession();
		}

		@Override
		public void onError(Session session, Throwable thr) {
			log.error("Endpoint error: " + session, thr);
			closeSession();
		}

		public void sendCmd(String cmd) {
			if (session != null) {
				try {
					session.getBasicRemote().sendText(cmd);
				} catch (IOException e) {
					log.error("Cannot send command: " + cmd, e);
				}
			} else {
				log.warn("Cannot send command, session is closed: " + cmd);
			}
		}
	}

	private class WssMessageTextStreamHandler implements MessageHandler.Whole<Reader> {

		private char[] buf = new char[READ_BUF_SIZE];
		private StringBuilder bufPartial = new StringBuilder(100);
		private int partialCount;

		@Override
		public void onMessage(Reader reader) {

			if (log.isDebugEnabled()) {
				log.debug("+++++++++++++++++++ onMessage ++++++++++++++++++++++++++++++++++++++++");
			}
			// clear
			int index = -1;
			String packet = null;
			try {

				while (reader.ready()) {
					int c = reader.read();
					// We have data, increase index
					index++;

					// End of stream;
					if (c == -1) {
						if (index <= 0) {
							// no data, just return
							break;
						}
						packet = new String(buf, 0, index);
						if (log.isDebugEnabled()) {
							log.debug("<BUFEND " + packet);
						}
						// Normal packet
						boolean haveFullPacket = checkFraming(packet);
						if (haveFullPacket) {
							handlePacket(packet);
						} else {
							handePartialPacket(packet);
						}
						break;
					}

					if (c == ASCII.LF) {
						// we have a packet delimiter, do not take LF
						packet = new String(buf, 0, index);
						if (log.isDebugEnabled()) {
							log.debug("<BUF " + packet);
						}
						boolean haveFullPacket = checkFraming(packet);
						if (haveFullPacket) {
							handlePacket(packet);
						} else {
							handePartialPacket(packet);
						}
						// Reset
						index = -1;
					} else {
						buf[index] = (char) c;
					}

				}

			} catch (IOException e1) {
				log.error("WSS read error: ", e1);
			}
			if (log.isDebugEnabled()) {
				log.debug("++++++ Leaving onMessage ++++++++++++++++++++++++++++++++++++++++");
			}
		}

		private void handePartialPacket(String packet) {
			// handle partial packet
			if (log.isDebugEnabled()) {
				log.debug("<PARTIAL buf: " + bufPartial.toString() + " pkt: " + packet);
			}
			bufPartial.append(packet);
			partialCount++;
			// Do we have a full message?
			if (partialCount == 2) {
				if (checkFraming(bufPartial.toString())) {
					if (log.isDebugEnabled()) {
						log.debug("<PARTIAL DONE" + bufPartial.toString());
					}
					handlePacket(bufPartial.toString());
				}
				partialCount = 0;
				bufPartial.setLength(0);
			}
		}

		private int hasSOH(char[] buf) {
			for (int i = 0; i < buf.length; i++) {
				if (buf[i] == ASCII.SOH) {
					return i;
				}
			}
			return -1;
		}

		private int hasEtx(char[] buf) {
			for (int i = 0; i < buf.length; i++) {
				if (buf[i] == ASCII.ETX) {
					return i;
				}
			}
			return -1;
		}

		/*
		 * Returns true if full packet found
		 */
		private boolean checkFraming(String packet) {
			if (connState != ConnectionState.LoggedIn) {
				// Only data messages are checked for framing
				return true;
			}
			if (buf[0] == JerqProtocol.JERQ_ERROR_START_BYTE || buf[0] == JerqProtocol.JERQ_INFO_START_BYTE) {
				// INFO and ERROR messages do not have framing
				return true;
			}
			// When logged in we receive data messages
			char[] data = packet.toCharArray();
			boolean startRefresh = packet.contains("<QUOTE ");
			boolean endEndRefresh = packet.contains("</QUOTE>");
			if (startRefresh || endEndRefresh) {
				// We are in a refresh packet
				if (startRefresh && endEndRefresh) {
					return true;
				}
				return false;
			} else {
				// DDF Message
				int soh = hasSOH(data);
				int etx = hasEtx(data);
				if (soh < 0 || etx < 0) {
					return false;
				}
				if (soh > etx) {
					return false;
				}
				// packet has SOH and ETX
				return true;
			}
		}

		private void handlePacket(String msgString) {
			try {
				if (msgString.length() == 0) {
					return;
				}
				if (log.isDebugEnabled()) {
					log.debug("handlePacket: " + msgString);
				}

				if (connState == ConnectionState.Connecting) {
					handleConnectionHeader(msgString);
				} else if (connState == ConnectionState.Connected) {
					handleLogon(msgString);
				} else if (connState == ConnectionState.LoggedIn) {
					/*
					 * We have a data packet which can be one of the following:
					 * 
					 * DDF, Refresh Message, only sent when using the push
					 * command (GO).
					 */

					if (msgString.startsWith(JerqProtocol.JERQ_INFO_START)) {
						// Log and continue
						log.info("SERVER INFO: " + msgString);
					} else if (msgString.startsWith(JerqProtocol.JERQ_ERROR_START)) {
						// Log and continue
						log.warn("SERVER ERROR: " + msgString);
					} else if (msgString.startsWith(JerqProtocol.JERQ_STOPPED_STREAM)) {
						log.error("SERVER ERROR " + msgString);
						// Close the session
						closeSession();
					} else {
						/*
						 * Data Packet
						 */
						byte[] bytes = msgString.getBytes();
						if (bytes[0] == JerqProtocol.JERQ_REFRESH_MESSAGE) {
							/*
							 * Jerq Refresh message, format: %<QUOTE>
							 * ...</QUOTE>
							 */
							connection.handleMessage(msgString);
						} else {

							/*
							 * DDF message, framed by <SOH> .... <ETX>
							 */
							if (msgString.length() < 2) {
								// must be > 2 chars for a valid DDF message
								return;
							}
							connection.handleMessage(msgString);
						}
					}

				}
			} catch (Exception e) {
				log.error("Could not process message: " + msgString + " error: ", e);
			}
		}

		private void handleConnectionHeader(String message) {
			if (message.endsWith(JerqProtocol.JERQ_HEADER_END)) {
				/*
				 * We have a valid header, Send Login Command
				 */
				connState = ConnectionState.Connected;
				connection.handleEvent(makeConnectionEvent(ConnectionEventType.CONNECTED, reconnection));
				String command = "LOGIN " + connection.username + ":" + connection.password + " VERSION="
						+ connection.getVersion();
				enqueueCommand(command);
			}
		}

		private void handleLogon(String message) {
			if (message.startsWith(JerqProtocol.JERQ_ERROR_START)) {
				// Login failed
				connState = ConnectionState.NotConnected;
				log.error("Jerq WS Client, the server reported an invalid login attempt: " + message);
				connection.handleEvent(makeConnectionEvent(ConnectionEventType.LOGIN_FAILED, reconnection));
			} else if (message.startsWith(JerqProtocol.JERQ_SUCCESSFUL_LOGIN)) {
				// Successful login
				log.info("Sucessful login for: " + connection.username);
				connState = ConnectionState.LoggedIn;
				connection.handleEvent(makeConnectionEvent(ConnectionEventType.LOGIN_SUCCESS, reconnection));
				// On reconnection only
				if (reconnection) {
					resendSubscriptionsOnReconnection();
					reconnection = false;
				}
			}
			connectionLatch.countDown();
		}

	}

}
