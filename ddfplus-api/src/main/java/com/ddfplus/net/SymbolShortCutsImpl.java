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
	private static final String CTRL = "^";

	private static final Logger log = LoggerFactory.getLogger("SymbolShortCuts");

	private DefinitionService definitionService = new DefinitionServiceImpl();

	@Override
	public String[] resolveShortCutSymbols(String symbol) {
		String[] noSymbol = new String[0];
		if (symbol == null || symbol.isEmpty()) {
			return noSymbol;
		}

		if (symbol.charAt(0) == INDEX) {
			// index
			return new String[] { symbol };
		}

		String root = null;
		// All futures
		if (symbol.endsWith(ALL_FUTURES)) {
			root = symbol.substring(0, symbol.length() - ALL_FUTURES.length());
			String[] symbols = definitionService.getAllFutureSymbols(root);
			if (symbols.length > 0) {
				return symbols;
			}
			return noSymbol;
		}

		// Futures months: the "* notation” is <root>*<number>
		int i = symbol.indexOf(FUTURE_MONTH_SHORTCUT);
		if (i > 0) {
			String s = getMonthSymbol(symbol, i);
			if (s != null) {
				return new String[] { s };
			}
			return noSymbol;
		}

		if (symbol.endsWith(ALL_OPTIONS)) {
			root = symbol.substring(0, symbol.length() - ALL_OPTIONS.length());
			String[] symbols = definitionService.getAllOptionsSymbols(root);
			if (symbols.length > 0) {
				return symbols;
			}
			return noSymbol;
		}

		if (symbol.endsWith(ALL_OPTIONS_MONTH_YEAR)) {
			// Example: ZC^Z2015^OM
			String monthYear = null;
			int first = symbol.indexOf(CTRL);
			if (first > 0) {
				root = symbol.substring(0, first);
			}
			int last = symbol.lastIndexOf(CTRL);
			if (last > 0) {
				monthYear = symbol.substring(first + 1, last);
			}
			if (root != null && monthYear != null) {
				String[] symbols = definitionService.getAllOptionsMonthYearSymbols(root, monthYear);
				if (symbols.length > 0) {
					return symbols;
				}
			}
			return noSymbol;
		}

		// No short cut just return the symbol
		return new String[] { symbol };
	}

	String getMonthSymbol(String symbol, int i) {
		String ret = null;
		try {
			int month = Integer.parseInt(symbol.substring(i + 1));
			String root = symbol.substring(0, i);
			ret = definitionService.getFuturesMonthSymbol(root, month);

		} catch (NumberFormatException nfe) {
			log.error("Invalid month identifier on symbol: " + symbol + " streaming not active.");
		}
		return ret;
	}

}
