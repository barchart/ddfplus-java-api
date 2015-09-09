/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.enums;

public enum DdfSubRecord {
	PriceElements('0'),
	//
	ExchangeGeneratedRefresh('1'),
	//
	DdfGeneratedUpdatePriceElementsRefresh('2'),
	//
	DdfGeneratedActiveSessionRefresh('3'),
	//
	DdfGeneratedPreviousSessionRefresh('4'),
	//
	Last('5'),
	//
	OpenHighLowLastOtherStatisticsRefresh('6'),
	//
	Trade('7'),
	//
	BidAsk('8'),
	//
	MarketCondition('9'),
	//
	BidAskTradeCumVolume('A'),
	//
	BookDepth('B'),
	//
	EndOfDayCommodityPrices('C'),
	//
	EndOfDayPreviousCommodityStatistics('I'),
	//
	EndOfDayPreviousCommodityCompositeStatistics('T'),
	//
	BidAskStocks('E'),
	//
	MarketSymbol('F'),
	//
	EndOfDayStockForexPrices('S'),
	//
	TradeStocks('Z');

	private char element;

	DdfSubRecord(char c) {
		element = c;
	}

	public char value() {
		return element;
	}
}
