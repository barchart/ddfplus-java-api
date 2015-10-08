package com.ddfplus.net;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private static final Pattern DDF_SYMBOL_PATTERN = Pattern.compile("(\\D+)(\\D)(\\d+)");

	private static final Logger log = LoggerFactory.getLogger("SymbolShortCuts");

	private DefinitionService definitionService = new DefinitionServiceImpl();

	private Map<Character, Character> farOutMonths = new HashMap<Character, Character>();

	public SymbolShortCutsImpl() {
		farOutMonths.put('F', 'A');
		farOutMonths.put('G', 'B');
		farOutMonths.put('H', 'C');
		farOutMonths.put('J', 'D');
		farOutMonths.put('K', 'E');
		farOutMonths.put('M', 'I');
		farOutMonths.put('N', 'L');
		farOutMonths.put('Q', 'O');
		farOutMonths.put('U', 'P');
		farOutMonths.put('V', 'R');
		farOutMonths.put('X', 'S');
		farOutMonths.put('Z', 'T');
	}

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

		// Convert symbols to DDF feed symbol if required.
		String ddfFeedSymbol = convertToDdfFeedSymbol(symbol);
		if (ddfFeedSymbol != null) {
			return new String[] { ddfFeedSymbol };
		}

		// No short cut just return the symbol
		return new String[] { symbol };
	}

	String convertToDdfFeedSymbol(String symbol) {
		Matcher m = DDF_SYMBOL_PATTERN.matcher(symbol);
		if (m.matches()) {
			String s = m.group(1);
			char month = m.group(2).charAt(0);
			String yearString = m.group(3);
			int year = Integer.parseInt(yearString);
			int rawYear = year % 2000;
			/**
			 * <pre>
			 * For Futures contracts, currently only Natural Gas (NG), that
			 * trade out more than 10-years DDF will use alternate contract
			 * months
			 * 
			 * 'A','B','C','D','E','I','L','O','P','R','S','T' = 'Jan' ... 'Dec'
			 * 
			 * For example using 2015 as the base year
			 * NGV25 - Natural Gas Oct(V) 2015 
			 * gt  0.0 - Natural Gas Oct(R) 2025
			 * </pre>
			 */
			if (s.equals("NG") && rawYear >= 10) {
				Character newMonth = farOutMonths.get(month);
				if (newMonth != null) {
					month = newMonth;
				} else {
					log.error("Lookup for far out month for symbol: " + symbol + " failed.");
				}
			}
			// reduce the year to 1 digit, assumes the current year
			String ddfSymbol = s + month + yearString.charAt(yearString.length() - 1);
			return ddfSymbol;
		}
		return symbol;
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

	void setDefinitionService(DefinitionService definitionService) {
		this.definitionService = definitionService;
	}

}
