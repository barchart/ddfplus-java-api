/**
 *
 * Copyright 2004 - 2015 Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */

package com.ddfplus.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ddfplus.util.DDFDate;
import com.ddfplus.util.ParserHelper;
import com.ddfplus.util.XMLNode;

/**
 * The CumulativeVolume class encapsulates all of the trades throughout the day,
 * and organizes a table of volume at prices. It is very much an expanded data
 * table, or hashtable. Essentially, prices serve as keys, and the total volume
 * traded at those prices are the values.
 */
public class CumulativeVolume {

	protected final String _symbol;
	protected volatile char _baseCode = '\0';
	protected volatile int _tickIncrement = 1;
	protected volatile float _last = 0.0f;
	protected volatile int _lastSize = 0;
	protected volatile int _lastCumulativeVolume = 0;
	protected volatile long _date = 0L;

	protected final Map<Float, Integer> _data;

	public CumulativeVolume(String symbol) {
		_symbol = symbol;
		_data = new ConcurrentHashMap<Float, Integer>();
	}

	/**
	 * Adds a trade to the cumulative volume internal hashtable.
	 * 
	 * @param price
	 *            The price at which the trade occurred.
	 * @param size
	 *            The trade size associated with the price.
	 */

	protected void addTrade(float price, int size) {
		Float f = new Float(price);
		Integer i = _data.get(f);
		int ival = 0;
		if (i != null)
			ival = i.intValue();
		ival += size;

		_data.put(f, ival);

		_lastSize = size;
		if (price == _last)
			_lastCumulativeVolume += _lastSize;
		else
			_lastCumulativeVolume = _lastSize;
		_last = price;
	}

	/**
	 * @return <B>String</B> The Symbol of the Quote.
	 */

	public String getSymbol() {
		return _symbol;
	}

	/**
	 * @return the base code for the associated the data.
	 */

	public char getBaseCode() {
		return _baseCode;
	}

	/**
	 * @return the <code>long</code> as time in milliseconds
	 */

	public long getDate() {
		return _date;
	}

	/**
	 * @return the last price.
	 */

	public float getLast() {
		return _last;
	}

	/**
	 * @return the last size
	 */

	public int getLastSize() {
		return _lastSize;
	}

	/**
	 * Returns the current cumulative volume at the last price. This is the
	 * total cumulative volume at the last price since the last price became the
	 * last price.
	 * 
	 * @return the current cumulative volume at the last price.
	 */

	public int getLastCumulativeVolume() {
		return _lastCumulativeVolume;
	}

	/**
	 * @return an <code>ArrayList</code> of the last prices.
	 */

	public List<Float> getPrices() {
		List<Float> l = new ArrayList<Float>(_data.keySet());
		Collections.sort(l);
		return l;
	}

	/**
	 * @return <code>Hashtable</code> the data
	 */

	public Map<Float, Integer> getData() {
		return _data;
	}

	/**
	 * This is a convenience method, passing through the date to DDFDate and
	 * returning the day code.
	 * 
	 * @return the Day Code.
	 */

	public char getDayCode() {
		return (new DDFDate(_date)).getDayCode();
	}

	/**
	 * Returns the tick increment of the symbol. This is different than the base
	 * code. For instance, while corn may trade in 1/8ths, corn only trades at
	 * 0/8, 2/8, 4/8, 6/8, e.g. the tick increment is 2. Another example is the
	 * E-Mini S&amp;P. While its base code is A, in that its values are always
	 * 1/100ths of a dollar, e.g. pennies, it trades in 50 cent increments.
	 * 
	 * @return The tick increment
	 */

	public int getTickIncrement() {
		return _tickIncrement;
	}

	protected void setDate(long date) {
		_date = date;
	}

	public String toString() { // Overrides Object
		String s = _symbol + ";" + _last + ";" + _lastSize + ";" + _lastCumulativeVolume + ";" + _data.toString();
		return s;
	}

	/**
	 * Converts this CumulativeVolume into an XMLNode for text serialization
	 * purposes.
	 * 
	 * @return XMLNode
	 */
	public XMLNode toXMLNode() {
		int uc = SymbolInfo.ddfuc2bb(_baseCode);

		XMLNode node = new XMLNode("CV");
		node.setAttribute("symbol", _symbol);
		node.setAttribute("basecode", "" + _baseCode);

		node.setAttribute("tickincrement", "" + _tickIncrement);
		node.setAttribute("last", Integer.toString(ParserHelper.float2int(uc, _last)));
		node.setAttribute("lastsize", "" + _lastSize);
		node.setAttribute("lastcvol", "" + _lastCumulativeVolume);

		if (_date > 0) {
			DDFDate d = new DDFDate(_date);
			node.setAttribute("date", d.toDDFString());
		}

		node.setAttribute("count", "" + _data.size());

		StringBuilder sb = new StringBuilder();
		for (Float f : _data.keySet()) {
			if (sb.length() > 0)
				sb.append(":");
			sb.append(ParserHelper.float2int(uc, f) + "," + _data.get(f));
		}

		if (sb.length() > 0)
			node.setAttribute("data", sb.toString());

		return node;
	}

	/**
	 * Deserializes the <code>XMLNode</code> node and returns the
	 * <code>CumualtiveVolume</code> object instance.
	 * 
	 * @param node
	 *            The <code>XMLNode</code> serialized node
	 * @return The deserialized <code>CumulativeVolume</code> object.
	 */

	public static final CumulativeVolume fromXMLNode(final XMLNode node) {

		if (!node.getName().equals("CV")) {
			return null;
		}

		final CumulativeVolume volume = new CumulativeVolume(node.getAttribute("symbol"));

		volume._baseCode = node.getAttribute("basecode").charAt(0);

		volume._tickIncrement = Integer.parseInt(node.getAttribute("tickincrement"));

		volume._last = ParserHelper.string2float(node.getAttribute("last"), volume._baseCode);

		volume._lastSize = Integer.parseInt(node.getAttribute("lastsize"));

		volume._lastCumulativeVolume = Integer.parseInt(node.getAttribute("lastcvol"));

		final String dateString = node.getAttribute("date");
		if (dateString != null) {
			volume._date = DDFDate.fromDDFString(dateString).getMillisCST();
		}

		final String dataString = node.getAttribute("data");
		if (dataString != null) {
			final String[] data = dataString.split(":");
			for (int i = 0; i < data.length; i++) {
				String[] pair = data[i].split(",");
				Float price = new Float(ParserHelper.string2float(pair[0], volume._baseCode));
				Integer quantity = new Integer(pair[1]);
				volume._data.put(price, quantity);
			}
		}

		return volume;

	}

}
