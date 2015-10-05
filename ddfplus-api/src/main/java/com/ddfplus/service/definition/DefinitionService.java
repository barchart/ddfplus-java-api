package com.ddfplus.service.definition;

public interface DefinitionService {

	String[] getAllFutureSymbols(String root);

	String getFuturesMonthSymbol(String root, int month);

	String[] getAllOptionsSymbols(String root);

	String[] getAllOptionsMonthYearSymbols(String root);

}
