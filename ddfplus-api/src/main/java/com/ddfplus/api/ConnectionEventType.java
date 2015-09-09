/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.api;

/**
 * Connection Events Type
 *
 */
public enum ConnectionEventType {

	/**
	 * Signals a connected event.
	 */
	CONNECTED(1), //

	/**
	 * Signals a disconnected event.
	 */
	DISCONNECTED(2), //

	/**
	 * Signals a connection failed event.
	 */
	CONNECTION_FAILED(3), //

	/**
	 * Signals a user-lockout event.
	 */
	USER_LOCKOUT(4), //

	/**
	 * Signals a successful login.
	 */
	LOGIN_SUCCESS(5), //

	/**
	 * Signals a failed login.
	 */
	LOGIN_FAILED(5), //

	;

	final int code;

	private ConnectionEventType(final int code) {
		this.code = code;
	}

}
