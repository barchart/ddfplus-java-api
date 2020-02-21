/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.messages;

import com.ddfplus.enums.DdfMessageType;

import java.time.LocalDateTime;

/**
 * Raw DDF Message
 */
public interface DdfMessageBase {

	/**
	 * Gets the bytes.
	 * 
	 * @return the bytes
	 */
	byte[] getBytes();

	/**
	 * Returns the time stamp of the message, if available. milliseconds
	 * 
	 * @return The time stamp
	 */
	long getMillisCST();

	long getMillisUTC();

	/**
	 * Returns the local date time of the message. Will be represented in the exchange time zone.
	 *
	 * @return The local date time.
	 */
	LocalDateTime getLocalDateTime();

	/**
	 * Gets the message type.
	 * 
	 * @return the type
	 */
	DdfMessageType getMessageType();

}
