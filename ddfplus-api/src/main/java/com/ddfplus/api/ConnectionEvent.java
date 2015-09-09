/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.api;

/**
 * Connection Event.
 * 
 * @see ConnectionEventType
 */
public class ConnectionEvent {

	private ConnectionEventType type;

	private boolean isReconnect;

	public ConnectionEvent(ConnectionEventType type) {
		this.type = type;
	}

	public ConnectionEvent(ConnectionEventType type, boolean reconnection) {
		this(type);
		isReconnect = reconnection;
	}

	public ConnectionEventType getType() {
		return type;
	}

	public void setType(ConnectionEventType type) {
		this.type = type;
	}

	public boolean isReconnect() {
		return isReconnect;
	}

	public void setReconnect(boolean isReconnect) {
		this.isReconnect = isReconnect;
	}

	@Override
	public String toString() {
		return "Connection Event: " + type + " reconnect: " + isReconnect;
	}
}
