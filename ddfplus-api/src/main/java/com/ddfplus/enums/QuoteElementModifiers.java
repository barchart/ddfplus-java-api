/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.enums;

public enum QuoteElementModifiers {

	Last('0'), Ask('1'), Bid('2'), BidSize('<'), AskSize('='), TradeSize('>'), EtfSharesOutstanding('S'), EtfNetAssetValue(
			'N'), High52Week('H'), Low52('L'), CumulativeVolume('6');

	private char modifier;

	QuoteElementModifiers(char c) {
		modifier = c;
	}

	public char value() {
		return modifier;
	}

}
