/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.api.examples;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ddfplus.api.ConnectionEvent;
import com.ddfplus.codec.Codec;
import com.ddfplus.db.DataMaster;
import com.ddfplus.db.FeedEvent;
import com.ddfplus.db.MarketEvent;
import com.ddfplus.db.MasterType;
import com.ddfplus.enums.ConnectionType;
import com.ddfplus.messages.DdfMarketBase;
import com.ddfplus.net.Connection;
import com.ddfplus.net.ConnectionHandler;
import com.ddfplus.service.feed.FeedService;
import com.ddfplus.service.feed.FeedServiceImpl;
import com.ddfplus.service.usersettings.UserSettings;
import com.ddfplus.service.usersettings.UserSettingsService;
import com.ddfplus.service.usersettings.UserSettingsServiceImpl;

/**
 * The Server application is meant as a simple example on how to create a server
 * application that receives data from the ddfplus data servers via TCP or UDP.
 * The Barchart servers will connect to this server and send DDF messages.
 * 
 * This class implements ConnectionHandler, which is the primary way of
 * receiving ddfplus messages. Once you receive the message, your application
 * must parse and process the message. There are a set of parser classes in the
 * ddf API which convert the ddfplus messages into a Java model, where you have
 * access to real floating point values for prices and ints for sizes, for
 * example.
 * 
 * @see com.ddfplus.codec
 * 
 *      This example also uses the DataMaster class, part of the ddf.db package.
 *      This packages returns an even more well-formed object. However, please
 *      bear in mind that the nature of the server application does not allow
 *      for a "refresh" or "re-play" of symbols. Therefore, upon application
 *      startup, you will need to synchronize YOUR database to the DataMaster
 *      object, if you wish to use it.
 */
public class ServerListenExample implements ConnectionHandler {

	private static final Logger log = LoggerFactory.getLogger(ServerListenExample.class);
	// See ddf.db.DataMaster for more info.
	private final DataMaster dataMaster = new DataMaster(MasterType.Realtime);
	private Connection connection = null;
	private final AtomicLong messageCount = new AtomicLong(0);
	// Snapshot/Refresh via web service
	private FeedService feedService;
	private UserSettingsService userSettingsService = new UserSettingsServiceImpl();

	public static void main(String[] args) {

		ConnectionType connType;

		InetAddress addr = null;
		int port = 0;
		InetAddress intf = null;

		if (args.length < 3) {
			printHelp();
			System.exit(1);
		}

		String typeString = args[0].trim().toUpperCase();

		connType = ConnectionType.of(typeString);

		switch (connType) {
		case LISTEN_TCP:
		case LISTEN_UDP:
			break;
		default:
			printHelp();
			System.exit(1);
			break;
		}

		try {
			addr = InetAddress.getByName(args[1]);
			if (addr.isMulticastAddress()) {
				if (args.length < 4) {
					printHelp();
					System.exit(1);
				}
				intf = InetAddress.getByName(args[3]);
			}
		} catch (Exception e) {
			printHelp();
			System.exit(1);
		}

		try {
			port = Integer.parseInt(args[2]);
		} catch (Exception e) {
			printHelp();
			System.exit(1);
		}

		String snapshotUser = null;
		String snapshotPassword = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-su") && i + 1 < args.length) {
				snapshotUser = args[i + 1];
				i++;
			}
			if (args[i].equals("-sp") && i + 1 < args.length) {
				snapshotPassword = args[i + 1];
				i++;
			}
		}

		// If all of the inputs are good, start the server.
		ServerListenExample server = new ServerListenExample(connType, addr, port, intf, snapshotUser,
				snapshotPassword);

		ShutdownHook shutdownThread = new ShutdownHook(server);
		Runtime.getRuntime().addShutdownHook(shutdownThread);

		server.start();

	}

	public static void printHelp() {

		final StringBuilder text = new StringBuilder(1024);

		text.append("" + "Loads and runs the sample ddfplus server application.\n" + "Usage: java "
				+ ServerListenExample.class.getCanonicalName()
				+ " LISTEN_TCP|LISTEN_UDP address port [interface] [-su user] [-sp password]\n"
				+ "  LISTEN_TCP|LISTEN_UDP\tSpecify whether the inbound packets are TCP or UDP\n"
				+ "  address  \tSpecify the Local Address to bind to. Use 0.0.0.0 for any.\n"
				+ "  port     \tSpecify the port to bind to.\n"
				+ "  interface\t(Optional) Only if address is a multicast address.");
		text.append("\n  -su user\tSnapshot User Name");
		text.append("\n  -sp password\tSnapshot Password");
		log.info(text.toString());
	}

	/**
	 * Simple DDF Server receiving DDF messages and displaying to standard out.
	 * 
	 * @param connectionTupe
	 *            ConnectionType
	 * 
	 * @param address
	 *            <code>InetAddress</code> The address that we bind to
	 * @param port
	 *            <code>int</code> The port that we bind to
	 * @param intf
	 *            <code>InetAddress</code> The interface that we bind to for
	 *            multicast.
	 * @param snapshotPassword
	 *            Snapshot/Refresh User Name
	 * @param snapshotUser
	 *            Snapshot/Refresh Password
	 */
	public ServerListenExample(ConnectionType connectionTupe, InetAddress address, int port, InetAddress intf,
			String snapshotUser, String snapshotPassword) {

		connection = new Connection(connectionTupe, address, port, intf);

		connection.registerHandler(this);

		if (snapshotUser != null && snapshotPassword != null) {
			/*
			 * Provides refresh of statistics (hi, low, etc..) and enables Quote
			 * objects to be returned in the FeedEvent.
			 */
			// Look up via user settings
			UserSettings snapshotUserSettings = userSettingsService.getUserSettings(snapshotUser, snapshotPassword);
			if (snapshotUserSettings.getStreamPrimaryServer() == null) {
				log.warn("Could not determine Snapshot/Refresh DDF server for user: " + snapshotUser
						+ " will not have snapshots/refresh for quotes.");
			}
			// Add Feed Service to cache
			feedService = new FeedServiceImpl(dataMaster, snapshotUserSettings);
			dataMaster.setFeedService(feedService);
			log.info("Activating Snapshot/Refresh for all symbols.");
		}

	}

	public void start() {
		// Will receive DDF Messages and callback the Connection Handler
		connection.startDataStream();
	}

	public void shutdown() {
		if (connection != null) {
			connection.stopDataStream();
		}
	}

	// -------------- ConnectionHandler ----------------------------
	@Override
	public void onConnectionEvent(final ConnectionEvent event) {
		log.info("Con Event: ", event);
	}

	@Override
	public void onMessage(final byte[] array) {

		/*
		 * Decode message
		 */
		DdfMarketBase ddfMessage = Codec.parseMessage(array);

		/*
		 * Process the message, will update internal cache and return client API
		 * objects
		 * 
		 * 
		 * SPECIAL NOTE: In server mode, the ddfplus data feed sends down all
		 * messages as they come in, but there is no "re-play" of past messages.
		 * While this in-memory database is sufficient as a stand-alone entity,
		 * your final solution will have to bind DataMaster to your database.
		 * Otherwise, the results from processMessage will be incorrect after an
		 * application restart.
		 */
		FeedEvent fe = dataMaster.processMessage(ddfMessage);

		if (fe != null) {
			if (fe.isTimestamp()) {
				log.info("< TS:" + fe.getTimestamp());
			}
			if (fe.isDdfMessage()) {
				log.info("< DDF:" + fe.getDdfMessage());
			}
			if (fe.isQuote()) {
				log.info("< " + fe.getQuote().toXMLNode().toXMLString());
			}
			if (fe.isBookQuote()) {
				log.info("< BOOK:" + fe.getBook().toXMLNode().toXMLString());
			}
			if (fe.isMarketEvents()) {
				for (MarketEvent me : fe.getMarketEvents()) {
					log.info("< MARKET EVENT:" + me);
				}
			}
		}

		final long count = messageCount.getAndIncrement();

		if (count % 10000 == 0) {
			log.info("message count: ", count);
		}

	}

	private static class ShutdownHook extends Thread {

		private ServerListenExample app;

		public ShutdownHook(ServerListenExample app) {
			this.app = app;
		}

		@Override
		public void run() {
			app.shutdown();
		}

	}

}