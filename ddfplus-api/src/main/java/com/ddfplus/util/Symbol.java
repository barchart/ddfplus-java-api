/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */

package com.ddfplus.util;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class Symbol {

	// XXX TIME!ZONE
	public static final int _currentYear = new DateTime().getYear();
	public static final int _currentMonth = new DateTime().getMonthOfYear();

	private static char[] _extendedFuturesMonths = new char[] { 'A', 'B', 'C', 'D', 'E', 'I', 'L', 'O', 'P', 'R', 'S',
			'T' };

	private static char[] _futuresMonths = new char[] { 'F', 'G', 'H', 'J', 'K', 'M', 'N', 'Q', 'U', 'V', 'X', 'Z' };

	public static int calculateYear(String value) {
		int fullYear = 0;

		int year = 0;
		try {
			year = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			;
		}

		if (year < 0)
			year = 0;

		if (year > 1000)
			fullYear = year;
		else {
			if (year < 10) {
				fullYear = ((_currentYear / 10) * 10) + year;

				if (fullYear < _currentYear)
					fullYear += 10;
			} else if (year < 100) {
				fullYear = ((_currentYear / 100) * 100) + year;
			}
		}

		if (fullYear < 1900)
			fullYear = 1900;

		return fullYear;
	}

	public static char getExtendedFuturesMonthCode(int month) {
		return _extendedFuturesMonths[month - 1];
	}

	public static char getFuturesMonthCode(int month) {
		return _futuresMonths[month - 1];
	}

	public static int getMonthFromExtendedFuturesCode(char c) {
		for (int i = 0; i < _extendedFuturesMonths.length; i++) {
			if (c == _extendedFuturesMonths[i])
				return (i + 1);
		}

		return 0;
	}

	public static int getMonthFromFuturesCode(char c) {
		for (int i = 0; i < _futuresMonths.length; i++) {
			if (c == _futuresMonths[i])
				return (i + 1);
		}

		return 0;
	}

	public static SymbolType getSymbolType(String symbol) {
		if (symbol.startsWith("_S_"))
			return SymbolType.Future_Spread;
		else if (symbol.startsWith("_U_"))
			return SymbolType.Future_Option_UserDefinedSpread;
		else if (symbol.startsWith("_"))
			return SymbolType.Test;
		else if (symbol.length() > 0) {
			if (symbol.endsWith(".CF"))
				return SymbolType.Fund_CAN;

			boolean hasNumbers = false;
			char[] ca = symbol.toCharArray();

			if (ca[0] == '$')
				return SymbolType.Index;
			else if (ca[0] == '^')
				return SymbolType.Forex;
			else if ((symbol.length() == 8) && (ca[6] == '.'))
				return SymbolType.Forex;
			else if (ca[0] == '-')
				return SymbolType.Equity_US; // Actually a Sector.
			else if (symbol.endsWith(".AX"))
				return SymbolType.Equity_AX;
			else if ((symbol.endsWith(".BB")) || (symbol.endsWith(".FP")) || (symbol.endsWith(".NA"))|| (symbol.endsWith(".PL")))
				return SymbolType.Equity_EU;
			else if ((symbol.endsWith(".EB")) || (symbol.endsWith(".IX")) || (symbol.endsWith(".DX")))
				return SymbolType.Equity_EU;
			else if ((symbol.endsWith(".EI")))
				return SymbolType.Index_EU;
			else if ((symbol.endsWith(".TO")) || (symbol.endsWith(".VN")) || (symbol.endsWith(".CN")))
				return SymbolType.Equity_CAN;
			else if ((symbol.endsWith(".LN")) || (symbol.endsWith(".LS")))
				return SymbolType.Equity_LSE;
			else if (symbol.endsWith(".NS"))
				return SymbolType.Equity_NSE;
			else if (symbol.endsWith(".CM"))
				return SymbolType.GrainBid;
			else if (symbol.endsWith(".CS"))
				return SymbolType.CommodityStats;
			else if (symbol.endsWith(".C3"))
				return SymbolType.Commodity3;
			else if (symbol.endsWith(".CP"))
				return SymbolType.CommodityPriceNetwork;
			else if (symbol.endsWith(".RT"))
				return SymbolType.Rates;
			else if (symbol.endsWith(".PT"))
				return SymbolType.Platts;

			for (int i = 0; i < ca.length; i++) {
				if (Character.isDigit(ca[i])) {
					hasNumbers = true;
					break;
				}
			}

			// If the symbol is too short, or the month code is a digit
			if (hasNumbers) {
				if (symbol.length() < 3)
					hasNumbers = false;
			}

			if (!hasNumbers) {
				if ((symbol.length() == 5) && (symbol.endsWith("X"))) {
					for (char c : ca) {
						if ((c == '-') || (c == '.'))
							return SymbolType.Equity_US;
					}
					return SymbolType.Fund;
				} else
					return SymbolType.Equity_US;
			} else {
				if (Character.isDigit(ca[ca.length - 1]))
					return SymbolType.Future;
				else if (symbol.length() >= 4 && symbol.charAt(symbol.length() - 4) == '.') {
					return SymbolType.Equity_Option;
				}
				else {
					return SymbolType.Future_Option;
				}
			}
		} else
			return SymbolType.Unknown;
	}

	public static boolean isCashSymbol(String symbol) {
		return symbol.endsWith("Y0") || symbol.endsWith("Y00");
	}

	public static String getShortCashSymbol(String symbol) {
		if(symbol.endsWith("Y00")) {
			return symbol.substring(0,symbol.length() - 1);
		}
		return symbol;
	}

	private static String[] splitSymbol(String symbol, int currentYear, int currentMonth) {
		SymbolType type = getSymbolType(symbol);
		return splitSymbol(symbol, type, currentYear, currentMonth);
	}

	private static String[] splitSymbol(String symbol, SymbolType type, int currentYear, int currentMonth) {
		String[] parts = null;
		switch (type) {
		case Future:
			parts = new String[3]; // Commodity Code, Month, Year
			if (symbol.length() == 6) {
				parts[0] = symbol.substring(0, 3);
				parts[1] = symbol.substring(3, 4);
				parts[2] = symbol.substring(4, 6);
			} else {
				parts[0] = parts[1] = parts[2] = "";
				boolean year = true;

				for (int i = symbol.length(); i > 0; i--) {
					char c = symbol.charAt(i - 1);
					if (year) {
						if (Character.isDigit(c))
							parts[2] = c + parts[2];
						else {
							parts[1] = "" + c;
							year = false;
						}
					} else
						parts[0] = "" + c + parts[0];
				}
				parts[0] = parts[0].trim();
				parts[2] = parts[2].trim();
			}
			return parts;
		case Future_Option:
			String[] sa = symbol.split("\\|");

			if (sa.length == 2) {
				parts = new String[5];
				String[] sb = splitSymbol(sa[0], currentYear, currentMonth);

				parts[0] = sb[0];
				parts[1] = sb[1];
				parts[2] = sa[1].substring(0, sa[1].length() - 1);
				char callPutIndicator = sa[1].substring(sa[1].length() - 1).charAt(0);
				int year = callPutIndicator >= 'P' ? currentYear + (callPutIndicator - 'P') : currentYear + (callPutIndicator - 'C');
				int month = Symbol.getMonthFromExtendedFuturesCode(parts[1].charAt(0));
				if (month == 0) {
					month = Symbol.getMonthFromFuturesCode(parts[1].charAt(0));
				}

				if (sb.length > 2) {
					year += Integer.parseInt(sb[2]) - currentYear % (int)Math.pow(10, sb[2].length());
				}

				if (year == currentYear && month < currentMonth) {
					year += 10;
				}

				parts[3] = Integer.toString(year);
				parts[4] = callPutIndicator >= 'P' ? "P" : "C";
				return parts;
			} else {
				parts = new String[5];
				char callPutIndicator = symbol.substring(symbol.length() - 1).charAt(0);
				parts[3] = Integer.toString(callPutIndicator >= 'P' ? currentYear + (callPutIndicator - 'P') : currentYear + (callPutIndicator - 'C'));
				parts[4] = callPutIndicator >= 'P' ? "P" : "C";

				parts[2] = "";
				int pos = symbol.length() - 2;
				while (pos > 0) {
					if (Character.isDigit(symbol.charAt(pos)))
						parts[2] = symbol.charAt(pos) + parts[2];
					else
						break;
					pos--;
				}

				parts[1] = "" + Character.toUpperCase(symbol.charAt(pos));
				parts[0] = symbol.substring(0, pos).toUpperCase();
				return parts;
			}
		default:
			return null;
		}
	}

	private final String _symbol;
	private final String _commodityCode;
	private final char _month;
	private final OptionType _optionType;
	private final String _strike;
	private final SymbolType _type;
	private final int _year;

	private final String _spreadType;
	private final List<Symbol> _spreadLegs;

	private String [] futureParts;

	public Symbol(String symbol) {
		this(symbol, _currentYear, _currentMonth);
	}
	public Symbol(String symbol, int currentYear, int currentMonth) {
		this._symbol = symbol;
		this._type = Symbol.getSymbolType(symbol);

		switch (this._type) {
		case Future: {
			String[] sa = Symbol.splitSymbol(symbol, currentYear, currentMonth);
			futureParts = sa;
			this._commodityCode = sa[0];
			this._month = (sa[1].length() > 0) ? sa[1].charAt(0) : '\0';
			this._year = Symbol.calculateYear(sa[2]);
			this._optionType = null;
			this._strike = null;
			this._spreadType = null;
			this._spreadLegs = null;
			break;
		}
		case Future_Option: {
			String[] sa = Symbol.splitSymbol(symbol, currentYear, currentMonth);
			this._commodityCode = sa[0];
			this._month = (sa[1].length() > 0) ? sa[1].charAt(0) : '\0';
			this._strike = sa[2];

			_year = Integer.parseInt(sa[3]);
			this._optionType = sa[4].equals("P") ? OptionType.Put : OptionType.Call;
			this._spreadType = null;
			this._spreadLegs = null;
			break;
		}
		case Future_Spread: {
			String[] sa = symbol.split("_");
			String spreadType = null;
			List<Symbol> legs = new ArrayList<Symbol>();
			if (sa.length > 3) {
				int count = 0;
				for (String s2 : sa) {
					s2 = s2.trim();
					if (count < 2) {
						;
					} else if (count == 2)
						spreadType = s2;
					else if (s2.length() > 0)
						legs.add(new Symbol(s2));
					count++;
				}
			}

			if (spreadType != null) {
				this._spreadType = spreadType;
				this._spreadLegs = legs;

				Symbol s = legs.get(0);
				this._commodityCode = s.getCommodityCode();
				this._month = s.getMonth();
				this._year = s.getYear();
			} else {
				this._spreadType = null;
				this._spreadLegs = null;
				this._commodityCode = null;
				this._month = '\0';
				this._year = 0;
			}

			this._optionType = null;
			this._strike = null;
			break;
		}
		default:
			this._commodityCode = null;
			this._month = '\0';
			this._optionType = OptionType.None;
			this._strike = null;
			this._year = 0;
			this._spreadType = null;
			this._spreadLegs = null;
			break;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Symbol) {
			Symbol s = (Symbol) o;
			switch (s._type) {
			case Future:
				return ((this._commodityCode.equals(s._commodityCode)) && (this._month == s._month) && (this._year == s._year));
			case Future_Option:
				return ((this._commodityCode.equals(s._commodityCode)) && (this._month == s._month)
						&& (this._year == s._year) && (this._strike.equals(s._strike)) && (this._optionType == s._optionType));
			default:
				return ((this._symbol.equals(s._symbol)) && (this._type == s._type));
			}
		}

		return false;
	}

	public String getCommodityCode() {
		return this._commodityCode;
	}

	public char getMonth() {
		return this._month;
	}

	public OptionType getOptionType() {
		return _optionType;
	}

	public String getNormalizedSymbol() {
		switch (this._type) {
		case Future:
			StringBuilder sb = new StringBuilder(this._commodityCode);
			sb.append(this._month);
			sb.append(Integer.toString(_year).substring(2, 4));
			return sb.toString();
		default:
			return this._symbol;
		}
	}

	public String getShortSymbol() {
		return getShortSymbol(_currentYear, _currentMonth);
	}

	public String getShortSymbol(int currentYear, int currentMonth) {
		 if(isCashSymbol(_symbol)) {
			return getShortCashSymbol(_symbol);
		}

		switch (this._type) {
		case Future: {
			char c_mo = _month;

			int mon = Symbol.getMonthFromFuturesCode(_month);
			if (mon == 0)
				return _symbol;

			final int switchYear = currentYear + 10;
			if (_year >= switchYear) {
				if (_year == switchYear) {
					if (mon >= currentMonth) {
						c_mo = Symbol.getExtendedFuturesMonthCode(mon);
					}
				} else {
					c_mo = Symbol.getExtendedFuturesMonthCode(mon);
				}
			}

			return _commodityCode + c_mo + Integer.toString(_year).substring(3, 4);
		}
		case Future_Option: {
				char c_mo = _month;

				int mon = Symbol.getMonthFromFuturesCode(_month);
				if (mon == 0)
					return _symbol;

				final int switchYear = currentYear + 10;
				if (_year >= switchYear) {
					if (_year == switchYear) {
						if (mon >= currentMonth) {
							c_mo = Symbol.getExtendedFuturesMonthCode(mon);
						}
					} else {
						c_mo = Symbol.getExtendedFuturesMonthCode(mon);
					}
				}

				if (_commodityCode.length() <=2) {
					int yearDelta = (_year - currentYear) % 10;

					return _commodityCode + c_mo + _strike
							+ (_optionType == OptionType.Call ? (char) ('C' + yearDelta) : (char) ('P' + yearDelta));
				}
				else {
					return _commodityCode + c_mo + (_year % 10) + "|" + _strike
							+ (_optionType == OptionType.Call ? 'C' : 'P');
				}
			}

			default:
			return this._symbol;
		}
	}

	public List<Symbol> getSpreadLegs() {
		return this._spreadLegs;
	}

	public String getSpreadType() {
		return this._spreadType;
	}

	public String getStrike() {
		return _strike;
	}

	public String getSymbol() {
		return _symbol;
	}

	public SymbolType getSymbolType() {
		return this._type;
	}

	public int getYear() {
		return this._year;
	}

	public boolean isFutureYearEndMatch() {
		if(this._type != SymbolType.Future) {
			return false;
		}
		try {
			String calculatedYear = Integer.toString(this._year).substring(2);
			return this.futureParts[2].equalsIgnoreCase(calculatedYear);
		} catch(Exception e) {
			return false;
		}
	}

}
