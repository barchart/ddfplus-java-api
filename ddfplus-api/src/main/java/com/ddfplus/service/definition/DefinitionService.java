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

}
