package com.ddfplus.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ddfplus.service.definition.DefinitionService;
import com.ddfplus.service.definition.DefinitionServiceImpl;

public class SymbolShortCutsImpl implements SymbolShortCuts {

	private static final char INDEX = '$';
	private static final String ALL_FUTURES = "^F";
	private static final String ALL_OPTIONS = "^O";
	private static final String ALL_OPTIONS_MONTH_YEAR = "^OM";
	private static final String FUTURE_MONTH_SHORTCUT = "*";

	private static final Logger log = LoggerFactory.getLogger(SymbolShortCutsImpl.class);

	private DefinitionService definitionService = new DefinitionServiceImpl();

	@Override
	public String[] checkForShortCutNotation(String symbol) {
		String[] ret = new String[] { symbol };
		if (symbol.charAt(0) == INDEX) {
			// index
			return ret;
		}

		String root = null;
		// All futures
		if (symbol.endsWith(ALL_FUTURES)) {
			root = symbol.substring(0, symbol.length() - ALL_FUTURES.length());
			String[] symbols = definitionService.getAllFutureSymbols(root);
			if (symbols.length > 0) {
				return symbols;
			}
		}

		if (symbol.endsWith(ALL_OPTIONS)) {
			root = symbol.substring(0, symbol.length() - ALL_OPTIONS.length());
			String[] symbols = definitionService.getAllOptionsSymbols(root);
			if (symbols.length > 0) {
				return symbols;
			}
		}

		if (symbol.endsWith(ALL_OPTIONS_MONTH_YEAR)) {
			root = symbol.substring(0, symbol.length() - ALL_OPTIONS_MONTH_YEAR.length());
			String[] symbols = definitionService.getAllOptionsMonthYearSymbols(root);
			if (symbols.length > 0) {
				return symbols;
			}
		}

		// Futures months: the "* notation” is <root>*<number>
		int i = symbol.indexOf(FUTURE_MONTH_SHORTCUT);
		if (i > 0) {
			String s = getMonthSymbol(symbol, i);
			if (s != null) {
				// TODO
			}
		}

		// No short cut just return the symbol
		return ret;
	}

	String getMonthSymbol(String symbol, int i) {
		String ret = null;
		try {
			int month = Integer.parseInt(symbol.substring(i));
			String root = symbol.substring(0, i);
			ret = definitionService.getFuturesMonthSymbol(root, month);

		} catch (NumberFormatException nfe) {
			log.error("Invalid month identifier: " + symbol);
		}
		return ret;
	}

}
