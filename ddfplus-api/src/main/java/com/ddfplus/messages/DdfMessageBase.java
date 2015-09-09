/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.messages;

import com.ddfplus.enums.DdfMessageType;

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
	 * Gets the message type.
	 * 
	 * @return the type
	 */
	DdfMessageType getMessageType();

}
