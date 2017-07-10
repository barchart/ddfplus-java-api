package com.ddfplus.service.definition;

/*
 * Lookups instrument metadata from Barchart's: http://extras.ddfplus.com
 */
public interface DefinitionService {

	/**
	 * Will start a background refresh of the symbol cache.
	 * 
	 * @param intervalSec
	 *            Interval in seconds.
	 */
	void init(Long intervalSec);

	String[] getAllFutureSymbols(String root);

	String getFuturesMonthSymbol(String root, int month);

	String[] getAllOptionsSymbols(String root);

	String[] getAllOptionsMonthYearSymbols(String root, String monthYear);

	/**
	 * Get a symbol's exchange code.
	 * 
	 * @param symbol
	 *            Exchange Symbol
	 * @return Barchart Exchange Code or null if not known at the time
	 */
	String getExchange(String symbol);

	/**
	 * Returns all symbols for an exchange.
	 * 
	 * @param exchangeCode
	 *            Barchart Exchange Code
	 * @return Array of symbols for this exchange.
	 */
	String[] getExchangeSymbols(String exchangeCode);

}
