/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.net;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ddfplus.api.ConnectionEvent;
import com.ddfplus.api.ConnectionEventType;

/**
 * Base class for communications channels.
 */
abstract class IoChannel extends Thread {

	protected static final long CONNECTION_TIMEOUT_SEC = 60;

	protected static final int RECONNECTION_INTERVAL_MS = 3000;

	private static final int CMD_INITIAL_DELAY_MS = 100;

	private static final long CMD_TIMEOUT_MS = 250;

	enum ConnectionState {
		NotConnected, Connecting, Connected, LoggedIn;
	}

	private static volatile int s_InstanceId = 0;

	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected final Connection connection;

	protected boolean isRunning = false;

	protected ConnectionState connState = ConnectionState.NotConnected;

	protected SymbolProvider symbolProvider;

	private BlockingQueue<Cmd> commandQ = new LinkedBlockingQueue<Cmd>();

	private ScheduledExecutorService es = Executors.newScheduledThreadPool(1);

	private CmdThread cmdThread;

	public IoChannel(final Connection connection) {
		super("ConnectionListenerThread#" + ++s_InstanceId + " for " + connection.getId());
		this.connection = connection;
		cmdThread = new CmdThread(commandQ);
		es.scheduleAtFixedRate(cmdThread, CMD_INITIAL_DELAY_MS, CMD_TIMEOUT_MS, TimeUnit.MILLISECONDS);
	}

	/**
	 * Sends a command to the Jerq Server using the communications specific
	 * protocol
	 * 
	 * @param cmd
	 *            Raw Jerk command
	 */
	protected abstract void sendCommand(String cmd);

	public abstract void disconnectAndShutdown();

	/**
	 * Sends a command to the Jerk Server.
	 * 
	 * @param cmd
	 *            Raw Jerk Command
	 */
	public final void enqueueCommand(String cmd) {
		Cmd c = new Cmd(cmd);
		enqueueCommand(c);
	}

	/**
	 * Sends a command to the Jerk Server.
	 * 
	 * @param cmd
	 *            Command Object
	 */
	public final void enqueueCommand(Cmd cmd) {
		commandQ.offer(cmd);
	}

	public int createReadTimeout() {
		return (25000 + ((int) (Math.random() * 10) * 1000));
	}

	public int getMaxQueueSize() {
		return -1;
	}

	public int getQueueSize() {
		return -1;
	}

	protected void stopCommandThread() {
		es.shutdownNow();
	}

	protected void distributeMessage(final byte[] array) {

		if (array == null || array.length < 1) {
			return;
		}

		boolean isStart = true;

		int pos1 = 0;
		int pos2 = 0;

		for (pos1 = 0; pos1 < array.length; pos1++) {
			char c = (char) array[pos1];

			if (isStart) {
				if (pos1 > pos2) {
					byte[] ba = new byte[pos1 - pos2];
					System.arraycopy(array, pos2, ba, 0, ba.length);
					connection.newQueueMessage(ba);
					pos2 = pos1;
				}

				if (c == '\u0001')
					isStart = false;
			} else {
				if (c == '\u0003') {
					if ((array.length > pos1 + 9) && (array[pos1 + 1] == 20)) {
						pos1 += 9;
					}
					isStart = true;
				}
			}
		}

		if (pos1 > pos2) {

			byte[] ba = new byte[pos1 - pos2];

			System.arraycopy(array, pos2, ba, 0, ba.length);

			connection.newQueueMessage(ba);

			pos2 = pos1;

		}
	}

	protected Connection getConnection() {
		return this.connection;
	}

	protected void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (Exception ignore) {
		}
	}

	protected void resendSubscriptionsOnReconnection() {
		log.info("Resending subscriptons due to a reconnection.");
		cmdThread.resendSubscriptionRequests();
	}

	protected ConnectionEvent makeConnectionEvent(ConnectionEventType type, boolean reconnection) {
		return new ConnectionEvent(type, reconnection);
	}

	private class CmdThread implements Runnable {

		private static final int COMMAND_AGGREGATION_COUNT = 250;
		private BlockingQueue<Cmd> q;
		private StringBuilder sendBuf = new StringBuilder();
		private StringBuilder goBuf = new StringBuilder();
		private int numGo = 0;
		private StringBuilder stopBuf = new StringBuilder();
		private int numStop = 0;
		// Used for reconnection
		private List<String> subscriptionCmdHistory = new ArrayList<String>();

		public CmdThread(BlockingQueue<Cmd> q) {
			this.q = q;
		}

		@Override
		public void run() {
			// Used to aggregate the subscription commands.
			initGoBuf();
			initStopBuf();
			while (q.peek() != null) {
				sendBuf.setLength(0);
				try {
					Cmd cmd = q.poll();
					if (cmd != null) {

						// Must be logged in
						if (!cmd.getCmd().startsWith("LOGIN") && connState != ConnectionState.LoggedIn) {
							log.warn("Not logined in, ignoring command: " + cmd);
							continue;
						}
						// Admin commands
						if (cmd.getCmd().startsWith("LOGIN") || cmd.getCmd().startsWith("VERSION")
								|| cmd.getCmd().startsWith("LOGOFF") || cmd.getCmd().startsWith("LOGOUT")) {
							sendBuf.append(cmd.getCmd());
							send(sendBuf.toString());
						}
						// Start Subscription
						else if (cmd.getCmd().equals("GO")) {
							numGo++;
							if (numGo > 1) {
								goBuf.append(',');
							}
							goBuf.append(cmd.getSymbol() + "=" + cmd.getSuffix());
						}
						// Stop subscription
						else if (cmd.getCmd().equals("STOP")) {
							numStop++;
							if (numStop > 1) {
								stopBuf.append(',');
							}
							stopBuf.append(cmd.getSymbol() + "=" + cmd.getSuffix());
						}
						// STREAM
						else if (cmd.getCmd().equals("STR")) {
							sendBuf.append(cmd.getCmd() + " L " + cmd.getSymbol() + ";");
							send(sendBuf.toString());
							subscriptionCmdHistory.add(sendBuf.toString());
						}

						if (numGo >= COMMAND_AGGREGATION_COUNT) {
							send(goBuf.toString());
							subscriptionCmdHistory.add(goBuf.toString());
							initGoBuf();
						}
						if (numStop >= COMMAND_AGGREGATION_COUNT) {
							send(stopBuf.toString());
							initStopBuf();
						}
					}
				} catch (Exception e) {
					log.error("Could not send command: " + e.getMessage());
				}
			}

			/*
			 * We did not have symbols > the aggregation count, so send what we
			 * have.
			 */
			if (numGo > 0) {
				send(goBuf.toString());
				subscriptionCmdHistory.add(goBuf.toString());
				initGoBuf();
			}
			if (numStop > 0) {
				send(stopBuf.toString());
				initStopBuf();
			}

		}

		/*
		 * Resends subscription requests on reconnection.
		 */
		public void resendSubscriptionRequests() {
			for (String cmd : subscriptionCmdHistory) {
				send(cmd);
			}
		}

		private void initStopBuf() {
			stopBuf.setLength(0);
			stopBuf.append("STOP ");
			numStop = 0;
		}

		private void initGoBuf() {
			goBuf.setLength(0);
			goBuf.append("GO ");
			numGo = 0;
		}

		private void send(String cmd) {
			// Call communications channel specific send method.
			log.info("> " + cmd);
			sendCommand(cmd);

		}
	}

}
