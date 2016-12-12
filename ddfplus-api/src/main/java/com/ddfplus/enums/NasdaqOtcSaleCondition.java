/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.enums;

/**
 * Spec version 14.4, Nov 2015
 *
 */
public enum NasdaqOtcSaleCondition {

	NO_SALE_CONDITION(' '),
	//
	REGULAR_SALE('@'),
	//
	ACQUISITON('A'),
	//
	BUNCHED_TRADE('B'),
	//
	CASH_SALE('C'),
	//
	DISTRIBUTION('D'),
	//
	PLACEHOLDER_FOR_FUTURE_USE('E'),
	//
	INTERMAKRET_SWEEP('F'),
	//
	BUNCHED_SOLD_TRADE('G'),
	//
	PRICE_VARIATION_TRADE('H'),
	//
	ODD_LOT_TRADE('I'),
	// AMEX
	RULE_155_TRADE('K'),
	//
	SOLD_LAST('L'),
	//
	MARKET_CENTER_OFFICIAL_CLOSE('M'),
	//
	NEXT_DAY('N'),
	//
	OPENING_PRINTS('O'),
	//
	PRIOR_REFERENCE_PRICE('P'),
	//
	MARKET_CENTER_OFFICIAL_OPEN('Q'),
	//
	SELLER('R'),
	//
	SPLIT_TRADE('S'),
	//
	FORM_T('T'),
	//
	EXTENDED_TRADING_HOURS('U'),
	//
	CONTINGENT_TRADE('V'),
	//
	AVERAGE_PRICE_TRADE('W'),
	//
	CROSS_TRADE('X'),
	//
	YELLOW_FLAG_REGULAR_TRADE('Y'),
	//
	SOLD('Z'),
	//
	STOPPED_STOCK('1'),
	//
	DERIVATIVELY_PRICED('4'),
	//
	RE_OPENING_PRINTS('5'),
	//
	CLOSING_PRINTS('6'),
	//
	QUALIFIED_CONTINGENT_TRADE('7'),
	//
	PLACEHOLDER_FOR_611_EXEMPT('8'),
	//
	CORRECTED_CONSOLIDATED_CLOSE('9'),;

	static final private NasdaqOtcSaleCondition[] ENUM_VALS = values();

	public static NasdaqOtcSaleCondition getByCode(char code) {
		for (final NasdaqOtcSaleCondition type : ENUM_VALS) {
			if (type._code == code) {
				return type;
			}
		}
		return NO_SALE_CONDITION;
	}

	private final char _code;

	NasdaqOtcSaleCondition(char code) {
		this._code = code;
	}

	public char getCode() {
		return _code;
	}

}
