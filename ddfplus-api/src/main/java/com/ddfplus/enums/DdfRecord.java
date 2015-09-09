/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.enums;

public enum DdfRecord {
	Timestamp('#'), RefreshOld('%'), RefreshXml('X'), Prices('2'), DepthEndOfDay('3'), ExchangeSpreads('5');

	private char record;

	DdfRecord(char c) {
		record = c;
	}

	public char value() {
		return record;
	}
}
