/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.enums;

public enum QuoteType {

	UNKNOWN, //

	TICK, //

	REFRESH, //

	SUMMARY, //

	BOOK, //

	TIME, //

	;

	public final boolean isTick() {
		return this == TICK;
	}

	public final boolean isRefresh() {
		return this == REFRESH;
	}

}
