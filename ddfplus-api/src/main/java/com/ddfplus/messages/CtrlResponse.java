/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.messages;

/**
 */
public class CtrlResponse extends AbstractMsgBase implements DdfControlResponse {

	public CtrlResponse(byte[] message) {
		super(message);
	}

	/** The response code. */
	volatile char responseCode;

	/** The response comment. */
	volatile String responseComment;

	@Override
	public char getCode() {
		return responseCode;
	}

	@Override
	public String getComment() {
		return responseComment;
	}

}
