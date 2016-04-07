/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.enums;

public enum MarketConditionType {

	NORMAL('\0'), //

	TRADING_HALT('A'), //

	TRADING_RESUMTPION('B'), //

	QUOTATION_RESUMPTION('C'), //

	END_FAST_MARKET('E'), //

	FAST_MARKET('F'), //

	LATE_MARKET('L'), //

	END_LATE_MARKET('M'), //

	POST_SESSION('P'), //

	END_POST_SESSION('Q'), //

	OPENING_DELAY('1'), //

	NO_OPEN_RESUME('4'),
	//
	NoRegSHOShortSaleRestriction('0'),
	//
	RegSHOShortSalePriceRestrictionInEffect('1'),
	//
	RegSHOShortSalePriceRestrictionRemainsInEffect('2');

	static final private MarketConditionType[] ENUM_VALS = values();

	public static MarketConditionType getByCode(final char code) {

		for (final MarketConditionType type : ENUM_VALS) {
			if (type._code == code) {
				return type;
			}
		}

		return NORMAL;

	}

	private final char _code;

	MarketConditionType(final char code) {
		this._code = code;
	}

	public char getCode() {
		return _code;
	}

}
