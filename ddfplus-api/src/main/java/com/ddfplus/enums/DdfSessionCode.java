/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.enums;

public enum DdfSessionCode {
	None(' '), CmeGlobex('G'), CmeGlobexPitSession('R');

	private char code;

	DdfSessionCode(char c) {
		code = c;
	}

	public char value() {
		return code;
	}
}
