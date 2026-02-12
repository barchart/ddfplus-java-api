/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.util;

public enum SymbolType {
	/**
	 */
	Unknown,
	/**
	 * US Equity, NYSE, NASDAQ, etc
	 */
	Equity_US,
	/**
	 * Australian Equity.
	 */
	Equity_AX, Equity_CAN,
	/**
	 * London Stock Exchange Equity.
	 */
	Equity_LSE,
	/**
	 * National Stock Exchange of India.
	 */
	Equity_NSE,
	/**
	 * EU Equity.
	 */
	Equity_EU,
	Fund,
	/**
	 * Canadian Funds.
	 */
	Fund_CAN,
	Future,
	Future_Spread,
	Equity_Option,
	/**
	 * Future Options.
	 */
	Future_Option,
	/**
	 * Forex.
	 */
	Forex,
	/**
	 * Indexes.
	 */
	Index,
	/**
	 * EU Indexes.
	 */
	Index_EU,
	Test,
	GrainBid,
	AgIndex,
	CommodityStats,
	Commodity3,
	CommodityPriceNetwork,
	Rates,
	Platts,
	/**
	 * Globex user defined spread on Future Options
	 */
	Future_Option_UserDefinedSpread,
	Economic_Indicators,
	Baltic,
	YieldBP,
	Equity_Brazil;

}
