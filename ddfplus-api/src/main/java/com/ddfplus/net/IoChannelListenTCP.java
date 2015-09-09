/**
 * Copyright 2004 - 2015 Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.net;

import java.io.BufferedInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ddfplus.util.ASCII;

/**
 * DDF TCP Listen server.
 * 
 * Listens for in bound TCP DDF connections from the Barchart servers and
 * callback message handlers.
 * 
 *
 */
class IoChannelListenTCP extends IoChannel {

	/** XXX remember to allocate kernel buffers */
	private static final int SO_BUF_SIZE = 8 * 1024 * 1024;

	/** XXX if no time stamp in 20 seconds, force disconnect */
	static final int CLIENT_TIMEOUT = 20 * 1000;

	/** maximum ddf message size */
	static final int PACKET_BUFFER = 64 * 1024;

	/** ddf time stamp */
	static final int TIME_STAMP_SIZE = 9;

	/** end of stream indicator */
	static final int EOF = -1;

	//

	private static final Logger log = LoggerFactory.getLogger(IoChannelListenTCP.class);

	private final XQueueTCP queueThread;

	public IoChannelListenTCP(final Connection connection) {

		super(connection);

		this.queueThread = new XQueueTCP(this);

	}

	@Override
	public int getMaxQueueSize() {
		return queueThread.getMaxSize();
	}

	@Override
	public int getQueueSize() {
		return queueThread.getSize();
	}

	static void delay(long millis) {
		try {
			Thread.sleep(1 * 1000);
		} catch (InterruptedException e) {
			log.debug("terminated");
		}
	}

	@Override
	public void run() {
		/*
		 * Since this class is used for servers (not clients), there isn't a
		 * call to disconnect. Hence ... while(true)
		 */
		while (true) {
			try {
				runCore();
			} catch (Throwable e) {
				log.error("listener run core failed", e);
				delay(1 * 1000);
			}
		}
	}

	private void runCore() throws Exception {

		log.info("listener start; local=" + //
				connection.primaryServer + ":" + connection.port);

		// local connection listener
		final ServerSocket server = new ServerSocket(connection.port, 0, connection.primaryServer);

		// incoming remote connection
		Socket client = null;

		try {

			client = server.accept();

			log.info("listener accept; remote=" + //
					client.getInetAddress() + ":" + client.getPort());

			client.setSoTimeout(CLIENT_TIMEOUT);
			log.info("listener inactivity timeout : {}", CLIENT_TIMEOUT);

			final int request = SO_BUF_SIZE;
			client.setReceiveBufferSize(request);
			final int actual = client.getReceiveBufferSize();
			if (actual >= request) {
				log.info("listener allocated receive buffer : {}", actual);
			} else {
				log.error("listener failed to allocate buffer; " + "request: {}  actual:{} ;", request, actual);
			}

			final BufferedInputStream input = new BufferedInputStream(client.getInputStream());

			final Machine machine = new Machine(input, connection);

			log.info("MACHINE INIT");

			machine.run();

			log.info("MACHINE DONE");

			log.info("listener finished; remote=" + //
					client.getInetAddress() + ":" + client.getPort());

		} catch (Throwable e) {
			log.error("listener client session failed", e);
		} finally {
			if (client != null) {
				try {
					client.close();
				} catch (Exception e) {
					log.error("unexpected", e);
				}
			}
		}

		if (server != null) {
			try {
				server.close();
			} catch (Exception e) {
				log.error("unexpected", e);
			}
		}

	}

	/** state of ddf stream decoder */
	private enum State {

		/** initialize */
		S0_INIT, //

		/** received ddf message start */
		S1_DDF_SOH, //

		/** received ddf message finish */
		S2_DDF_ETX, //

		/** received "century" i.e. time stamp marker */
		S3_DDF_DC4, //

		/** decoder logic error; should not happen */
		S4_ERROR, //

	}

	/** thread safe, used in single thread */
	private class Machine {

		private final ByteBuffer buffer = ByteBuffer.allocate(PACKET_BUFFER);

		private final byte[] array = buffer.array();

		private final BufferedInputStream input;

		@SuppressWarnings("unused")
		private final Connection connector;

		Machine(final BufferedInputStream input, final Connection connector) {
			this.input = input;
			this.connector = connector;

		}

		// send received message array
		void fire() {

			final int start = 0;
			final int finish = buffer.position();
			final int length = finish - start;

			/* queue-less */

			// final String message = new String(array, start, finish,
			// ASCII.ASCII_CHARSET);
			// connector.handleMessage(message);

			/* uses queue */

			final byte[] message = new byte[length];
			System.arraycopy(array, start, message, start, length);
			queueThread.add(message);

		}

		/** end of time stamp counter */
		private int ender;

		/** main loop; will exit only on EOF */
		void run() throws Exception {

			State state = State.S0_INIT;

			STREAM: while (true) {

				final int result = input.read();

				if (result == EOF) {
					log.warn("received end of stream; machine will exit now");
					break STREAM;
				}

				final byte alpha = (byte) result;

				switch (state) {

				case S0_INIT:
					switch (alpha) {
					case ASCII.SOH:
						/* new message start */
						buffer.clear();
						buffer.put(alpha);
						state = State.S1_DDF_SOH;
						continue STREAM;
					default:
						/* skip all till find SOH */
						continue STREAM;
					}

				case S1_DDF_SOH:
					switch (alpha) {
					case ASCII.ETX:
						/* found end of this message */
						state = State.S2_DDF_ETX;
						buffer.put(alpha);
						continue STREAM;
					default:
						/* keep reading till find ETX */
						buffer.put(alpha);
						continue STREAM;
					}

				case S2_DDF_ETX:
					switch (alpha) {
					case ASCII.SOH:
						/* next message start; end-to-end, no time stamp */
						fire();
						buffer.clear();
						buffer.put(alpha);
						state = State.S1_DDF_SOH;
						continue STREAM;
					case ASCII.DC4:
						/* this message has stamp suffix; read fixed size */
						buffer.put(alpha);
						ender = 1;
						state = State.S3_DDF_DC4;
						continue STREAM;
					default:
						log.error("machine reset: wrong alpha : {}", alpha);
						state = State.S0_INIT;
						continue STREAM;
					}

				case S3_DDF_DC4:
					/* read fixed size time stamp */
					buffer.put(alpha);
					ender++;
					if (ender == TIME_STAMP_SIZE) {
						fire();
						state = State.S0_INIT;
					}
					continue STREAM;

				default:
					log.error("machine reset: wrong state : {}", state);
					state = State.S0_INIT;
					continue STREAM;
				}

			}

		}

	}

	@Override
	public void disconnectAndShutdown() {
		log.info("listener disconnect");
	}

	@Override
	protected void sendCommand(String cmd) {

	}

}
