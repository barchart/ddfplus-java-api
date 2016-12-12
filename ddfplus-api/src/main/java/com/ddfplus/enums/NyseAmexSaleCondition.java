/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.enums;

/**
 * Spec Version 80, Sept 15, 2016
 *
 */
public enum NyseAmexSaleCondition {

	NO_SALE_CONDITION(' '),
	//
	REGULAR_TRADE('@'),
	//
	AVERAGE_PRICE_TRADE('B'),
	//
	CASH_TRADE('C'),
	//
	AUTOMATIC_EXECUTION('E'),
	//
	INTERMAKRET_SWEEP_ORDER('F'),
	//
	PRICE_VARIATION_TRADE('H'),
	//
	ODD_LOT_TRADE('I'),
	//
	RULE_127_OR_RULE_155('K'),
	//
	SOLD_LAST('L'),
	//
	MARKET_CENTER_OFFICIAL_CLOSE('M'),
	//
	NEXT_DAY_TRADE('N'),
	//
	MARKET_CENTER_OPENING_TRADE('O'),
	//
	PRIOR_REFERENCE_PRICE('P'),
	//
	MARKET_CENTER_OFFICIAL_OPEN('Q'),
	//
	SELLER('R'),
	//
	EXTENDED_HOURS_TRADE('T'),
	//
	EXTENDED_HOURS_SOLD('U'),
	//
	CONTINGENT_TRADE('V'),
	//
	CROSS_TRADE('X'),
	//
	SOLD('Z'),
	//
	DERIVATELY_PRICED('4'),
	//
	MARKET_CENTER_REOPENING_TRADE('5'),
	//
	MARKET_CENTER_CLOSING_TRADE('6'),
	//
	QUALIFIED_CONTINGENT_TRADE('7'),
	//
	RESERVED('8'),
	//
	CORRECTED_CONSOLIDATED_CLOSE_PRICE('9'),;

	static final private NyseAmexSaleCondition[] ENUM_VALS = values();

	public static NyseAmexSaleCondition getByCode(char code) {
		for (final NyseAmexSaleCondition type : ENUM_VALS) {
			if (type._code == code) {
				return type;
			}
		}
		return NO_SALE_CONDITION;
	}

	private final char _code;

	NyseAmexSaleCondition(char code) {
		this._code = code;
	}

	public char getCode() {
		return _code;
	}

}
