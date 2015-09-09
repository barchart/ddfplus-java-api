/**
 * Copyright 2004 - 2015 Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.net;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DDF UDP Listen Server.
 * 
 * Receives DDF UDP packets and calls the message handlers.
 */
class IoChannelListenUDP extends IoChannel {

	final static Logger log = LoggerFactory.getLogger(IoChannelListenUDP.class);

	/**
	 */
	private final XQueueUDP queueThread;

	public IoChannelListenUDP(Connection connection) {
		super(connection);
		queueThread = new XQueueUDP(this);
	}

	@Override
	public int getMaxQueueSize() { // Overrides Connection Listener
		return queueThread.getMaxSize();
	}

	@Override
	public int getQueueSize() { // Overrides Connection Listener
		return queueThread.getSize();
	}

	@Override
	public void run() {

		final byte[] array = new byte[NetConstants.UDP_RECV_PACKET_SIZE];

		final DatagramPacket packet = new DatagramPacket(array, NetConstants.UDP_RECV_PACKET_SIZE);

		DatagramSocket socket = null;

		/* use socket loop to guard against intermittent hardware disconnect */

		socketLoop: while (true) {
			try {

				socket = makeSocket();

				log.info("made socket; " + connection.primaryServer + ":" + connection.port);

				/*
				 * Since this class is used for servers (not clients), there
				 * isn't a call to disconnect. Hence ... while(true)
				 */

				while (true) {

					packet.setData(array);

					// Block here waiting for UDP packets
					socket.receive(packet);

					byte[] ba = new byte[packet.getLength()];

					System.arraycopy(array, 0, ba, 0, ba.length);

					queueThread.add(ba);

				}
			} catch (Exception e) {
				log.error("receive failed; " + connection.primaryServer + ":" + connection.port);
				log.error("", e);
			}

			try {
				// clean up
				if (socket != null) {
					socket.close();
				}
			} catch (Exception e) {
				log.error("close failed", e);
			}

			try {
				// do not hang app by spinning
				sleep(1 * 1000);
			} catch (Exception e) {
				log.error("unexpected", e);
			}

			continue socketLoop;

		}

	}

	@Override
	public void disconnectAndShutdown() {
		;
	}

	@Override
	protected void sendCommand(String cmd) {
		// not required, this is a server
	}

	private DatagramSocket makeSocket() throws Exception {

		DatagramSocket socket;

		if (connection.primaryServer.isMulticastAddress()) {

			socket = new MulticastSocket(connection.port);

			final MulticastSocket socketMC = (MulticastSocket) socket;

			if (connection.intf != null) {
				socketMC.setInterface(connection.intf);
			}

			socketMC.joinGroup(connection.primaryServer);

		} else {

			socket = new DatagramSocket(null);

			socket.setReuseAddress(true);

			InetSocketAddress socketAddress = new InetSocketAddress(connection.primaryServer, connection.port);

			socket.bind(socketAddress);

		}

		socket.setReceiveBufferSize(NetConstants.UDP_RECV_BUFFER_SIZE);

		log.error("DDFUDPListener getReceiveBufferSize(): " + socket.getReceiveBufferSize());

		return socket;

	}

}
