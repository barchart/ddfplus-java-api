/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.enums;

public enum QuoteElement {
	Trade('0'), Ask('1'), Bid('2'), Close('3'), Close2('4'), High('5'), Low('6'), Volume('7'), Open('A'), Open2('B'), OpenInterest(
			'C'), Settlement('D'), SettlementDuringMarketTrading('d'), Previous('E'), ETFInformationalMessage('F'), HighLow52Week(
			'S'), CancelledTradeMessage('X'), VWAP('V');

	private char element;

	QuoteElement(char c) {
		element = c;
	}

	public char value() {
		return element;
	}
}
