/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */

package com.ddfplus.net;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ddfplus.api.ConnectionEvent;
import com.ddfplus.api.ConnectionEventType;
import com.ddfplus.enums.ConnectionType;

/**
 * The Connection object manages and maintains a persistent connection to the
 * ddfplus data servers. This is a smart networking API that lets your
 * application connect to our servers. More importantly, once you implement this
 * api, your application can easily switch connectivity modes without recoding.
 */

public class Connection {

	private static final int READER_THREAD_WAIT_MS = 1000;

	private static final long RESTART_INTERVAL_MS = 10000;

	private static final Logger log = LoggerFactory.getLogger(Connection.class);

	protected InetAddress primaryServer;

	protected InetAddress secondaryServer;

	protected int port;

	protected InetAddress intf;

	protected String password = "";

	protected String username = "";

	protected int version = 1;

	private ConnectionType type = ConnectionType.TCP;

	private IoChannel channel;

	private boolean bDoStop = false;

	private String _id = "";

	private final List<ConnectionHandler> handlerList = new CopyOnWriteArrayList<ConnectionHandler>();

	private long _refreshRate = 5000L;

	private volatile long _totalDDFMessageCount = 0L;

	private XQueue _queue = null;

	private boolean _useQueue = false;

	/*
	 * Provide for implementations that do not support dynamic symbol
	 * registration.
	 */
	private SymbolProvider symbolProvider;

	/**
	 * This is the primary (full) constructor. If your application is connecting
	 * to a data server over the Internet, you should use one of the simplified
	 * constructors. For this constructor, depending on the mode, some items may
	 * be supplied as null.
	 */
	public Connection(ConnectionType type, String username, String password, InetAddress primaryServer, int port,
			InetAddress intf, SymbolProvider symbolProvider, InetAddress secondaryServer) {

		this.username = username;
		this.password = password;
		this.type = type;
		this.primaryServer = primaryServer;
		this.port = port;
		this.intf = intf;
		this.symbolProvider = symbolProvider;
		this.secondaryServer = secondaryServer;

	}

	/**
	 * Use this simplified constructor for Internet (client/server) access.
	 * Supply a valid mode (e.g. INET_UDP, INET_TCP, INET_SHTTP, or INET_HTTP),
	 * a username, a password, and the server you wish to connect to.
	 * <P>
	 * The API will assume the following default ddfplus ports:<BR>
	 * UDP: 7600<BR>
	 * TCP: 7500<BR>
	 * HTTP and Streaming HTTP: 80
	 * 
	 * @param mode
	 *            Connection Channel Mode, @see {@link ConnectionType}
	 * @param username
	 *            The username
	 * @param password
	 *            The password
	 * @param server
	 *            The server as <code>String</code>. A valid ICANN name or a
	 *            dotted IP address.
	 * @throws IllegalArgumentException
	 * @throws UnknownHostException
	 */
	public Connection(ConnectionType mode, String username, String password, String server)
			throws IllegalArgumentException, UnknownHostException {

		this.username = username;
		this.password = password;

		this.primaryServer = InetAddress.getByName(server);

		this.type = mode;

	}

	/**
	 * Use this simplified constructor for one-way server connections where
	 * ddfplus pushes data to your server. In this case, the mode should be
	 * SAT_UDP or SAT_TCP, and the address and port should be the local address
	 * and port on your server. The interface argument may be left null if you
	 * are not using a multicast socket.
	 * 
	 * @throws IllegalArgumentException
	 */
	public Connection(ConnectionType mode, InetAddress addr, int port, InetAddress intf)
			throws IllegalArgumentException {

		switch (mode) {

		case LISTEN_UDP:
		case LISTEN_TCP:

			this.type = mode;

			this.primaryServer = addr;
			this.port = port;
			this.intf = intf;

			break;

		default:
			throw new IllegalArgumentException("The constructor requires a one-way type mode:"
					+ ConnectionType.LISTEN_TCP + " or " + ConnectionType.LISTEN_UDP);
		}

	}

	/**
	 * Adds your application listener to the Connection object. Once added, your
	 * application will receive all live messages through this handler.
	 * 
	 * @param handler
	 *            Connection Handler
	 */
	public void registerHandler(final ConnectionHandler handler) {
		((CopyOnWriteArrayList<ConnectionHandler>) handlerList).addIfAbsent(handler);
	}

	/**
	 * Removes the connection handler
	 * 
	 * @param handler
	 */
	public void unregisterHandler(final ConnectionHandler handler) {
		handlerList.remove(handler);
	}

	/**
	 * Sends quote subscription for symbol which includes updates, refresh, and
	 * volume
	 * 
	 * @param symbol
	 *            Symbol
	 */
	public void subscribeQuote(String symbol) {
		Cmd cmd = new Cmd("GO", symbol, "SsV");
		channel.enqueueCommand(cmd);
	}

	/**
	 * Unsubscribe for a quote subscription.
	 * 
	 * @param symbol
	 */
	public void unsubscribeQuote(String symbol) {
		Cmd cmd = new Cmd("STOP", symbol, "Ss");
		channel.enqueueCommand(cmd);
	}

	/**
	 * Sends quote subscription for snapshot/refresh only
	 * 
	 * @param symbol
	 *            Symbol
	 */
	public void subscribeQuoteSnapshot(String symbol) {
		Cmd cmd = new Cmd("GO", symbol, "s");
		channel.enqueueCommand(cmd);
	}

	public void unsubscribeQuoteSnapshot(String symbol) {
		Cmd cmd = new Cmd("STOP", symbol, "s");
		channel.enqueueCommand(cmd);
	}

	/**
	 * Subscribe for book/depth.
	 * 
	 * @param symbol
	 *            Symbol
	 */
	public void subscribeDepth(String symbol) {
		Cmd cmd = new Cmd("GO", symbol, "Bb");
		channel.enqueueCommand(cmd);
	}

	/**
	 * Subscribe for book/depth.
	 * 
	 * @param symbol
	 *            Symbol
	 */
	public void unsubscribeDepth(String symbol) {
		Cmd cmd = new Cmd("STOP", symbol, "Bb");
		channel.enqueueCommand(cmd);
	}

	/**
	 * Subscribe for a "push" subscription on the whole exchange.
	 * 
	 * @param exchangeCode
	 *            DDF Exchange Code
	 */
	public void subscribeExchange(String exchangeCode) {
		Cmd cmd = new Cmd("STR", exchangeCode);
		channel.enqueueCommand(cmd);
	}

	public void subscribeMinuteBar(String symbol) {
		Cmd cmd = new Cmd("GO", symbol, "O");
		channel.enqueueCommand(cmd);
	}

	public void unsubscribeMinuteBar(String symbol) {
		Cmd cmd = new Cmd("STOP", symbol, "O");
		channel.enqueueCommand(cmd);
	}

	/**
	 * Starts the data stream to the server.
	 */
	public synchronized void startDataStream() {

		// Ensure a clean start.
		stopDataStream();

		if (handlerList.isEmpty()) {
			log.warn("ConnectionHandlers are empty, not starting connection.");
			return;
		}

		if (channel == null) {

			// Select the correct reader implementation based on connection type
			switch (type) {
			case UDP:
				// client
				channel = new IoChannelUDP(this, symbolProvider);
				break;
			case TCP:
				// client
				channel = new IoChannelTCP(this);
				break;
			case HTTP:
				// Client (One shot Refresh only)
				channel = new IoChannelHTTP(this, symbolProvider);
				break;
			case HTTPSTREAM:
				// client (DDF Stream)
				channel = new IoChannelHTTPSTREAM(this, symbolProvider);
				break;
			case LISTEN_UDP:
				// server
				channel = new IoChannelListenUDP(this);
				break;
			case LISTEN_TCP:
				// server
				channel = new IoChannelListenTCP(this);
				break;
			case WS:
			case WSS:
				// Web socket client
				channel = new IoChannelWSS(this);
				break;
			default:
				// client
				channel = new IoChannelTCP(this);
				break;
			}

			// Start the channel
			channel.start();

		}

	}

	/**
	 * Stops the data stream and closes all network connections.
	 */
	public void stopDataStream() {

		bDoStop = true;

		if (channel != null) {
			try {
				channel.disconnectAndShutdown();
				channel.join(READER_THREAD_WAIT_MS);
			} catch (InterruptedException ie) {
				log.error("Connection.stopDataStream(): " + ie);
			}
		}
		channel = null;
	}

	/**
	 * Returns id for this connection.
	 * 
	 * @return Unique Id for the connection.
	 */
	public String getId() {
		return _id;
	}

	public void setId(String value) {
		_id = value;
	}

	/**
	 * Returns the largest size of the message queue has been.
	 * 
	 * @return <code>int</code> queue size.
	 */

	public int getMaxQueueSize() {

		int size = 0;

		if (channel != null) {
			size = channel.getMaxQueueSize();
		}

		return size;

	}

	/**
	 * Returns the size of the current message queue.
	 * 
	 * @return <code>int</code> queue size.
	 */

	public int getQueueSize() {

		int size = 0;

		if (channel != null) {
			size = channel.getQueueSize();
		}

		return size;

	}

	/**
	 * Returns the total number of DDFPlus messages this Connection object has
	 * passed.
	 * 
	 * @return Total number of messages.
	 */
	public long getTotalDDFMessageCount() {
		return _totalDDFMessageCount;
	}

	/**
	 * Returns if the Connection object is using an internal queue for the data
	 * messages.
	 */

	public boolean getUseQueue() {
		return _useQueue;
	}

	/**
	 * Sets/Unsets the the use of the internal queue in the Connection object.
	 * 
	 * @param value
	 *            <code>boolean</code>Use/Not Use a queue
	 */

	public void setUseQueue(boolean value) {
		_useQueue = value;
		if (_useQueue) {
			_queue = new XQueue(this);
		}

	}

	public void newQueueMessage(byte[] array) {

		pumpMessage(array);

	}

	/**
	 * Gets the JERQ/P version. Version 1 is the default. Some newer JERQ
	 * servers work on version 1 or version 2.
	 */

	public int getVersion() {
		return version;
	}

	/**
	 * Sets the JERQ/P version. Version 1 is the default. Some newer JERQ
	 * servers work on version 1 or version 2.
	 */

	public void setVersion(int version) {
		this.version = version;
	}

	public void setThreadPriority(int priority) {

		if (channel == null) {
			log.error("Connection.setPriority(): listener == null");
		} else {
			if (priority < Thread.MIN_PRIORITY || priority > Thread.MAX_PRIORITY) {
				log.error("Connection.setPriority(): priority is out of range");
			} else {
				channel.setPriority(priority);
			}
		}

	}

	public int getThreadPriority() {

		if (channel == null) {
			log.error("Connection.getPriority(): listener == null");
			return -1;
		} else {
			return channel.getPriority();
		}

	}

	public String getThreadName() {
		if (channel == null) {
			log.error("Connection.getThreadName(): listener == null");
			return null;
		} else {
			return channel.getName();
		}
	}

	public void setThreadName(String name) {
		if (channel == null) {
			log.error("Connection.setThreadName(): listener == null");
		} else {
			channel.setName(name);
		}
	}

	public ConnectionType getConnectionType() {
		return type;
	}

	public int getPort() {
		return port;
	}

	/**
	 * Broadcasts a connection event id to each listener.
	 * 
	 * @param event
	 *            An id identifying the event.
	 */
	protected void handleEvent(final ConnectionEvent event) {

		for (final ConnectionHandler handler : handlerList) {

			try {
				handler.onConnectionEvent(event);
			} catch (Exception e) {
				log.error("Connection.handleEvent(" + event + ")", e);
			}

		}

	}

	protected void handleMessage(final String message) {

		// Optimization, use byte array from string
		final byte[] array = new byte[message.length()];

		for (int i = 0; i < array.length; i++) {
			array[i] = (byte) message.charAt(i);
		}

		if (_useQueue) {
			_queue.add(array);
		} else {
			pumpMessage(array);
		}

	}

	/**
	 * Returns the number of milliseconds to wait before issuing another refresh
	 * request.
	 */

	protected long getRefreshRate() {
		return _refreshRate;
	}

	protected void restart() {

		if (!bDoStop) {
			return;
		}

		try {
			Thread.sleep(RESTART_INTERVAL_MS);
			startDataStream();
		} catch (Exception e) {
			log.error("Connection.restart(): " + e);
			restart();
		}

	}

	private void pumpMessage(byte[] array) {

		if ((array == null) || (array.length < 1)) {
			return;
		}

		_totalDDFMessageCount++;

		if ((char) array[0] == 'C') {

			String message = new String(array);

			if (message.startsWith("CLockout")) {
				stopDataStream();
				handleEvent(new ConnectionEvent(ConnectionEventType.USER_LOCKOUT));
				return;
			}

		}

		// Call each handler
		for (ConnectionHandler handler : handlerList) {
			try {
				handler.onMessage(array);
			} catch (Exception e) {
				log.error("Connection.pumpMesage(" + new String(array) + "): " + e);
			}
		}
	}

}
