/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.messages;

import com.ddfplus.enums.DdfMessageType;

abstract class AbstractMsgBase implements DdfMessageBase {

	/** The original ddf message. */
	public volatile byte[] _message;

	/** The _time in millis. */
	// http://www.timeanddate.com/library/abbreviations/timezones/na/cst.html
	volatile long millisCST = 0L;

	AbstractMsgBase(byte[] message) {
		_message = message;
	}

	@Override
	public DdfMessageType getMessageType() {
		return DdfMessageType.Unknown;
	}

	@Override
	public byte[] getBytes() {
		return _message;
	}

	@Override
	public long getMillisCST() {
		return this.millisCST;
	}

	@Override
	public long getMillisUTC() {
		return this.millisCST;
	}

	/**
	 * Append concrete for toString() method.
	 * 
	 * @param text
	 *            the text
	 */
	protected void appendConcrete(StringBuilder text) {

	}

}
