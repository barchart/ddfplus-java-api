/**
 *
 * Copyright 2004 - 2015 Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */

package com.ddfplus.db;

import com.ddfplus.util.DDFDate;
import com.ddfplus.util.ParserHelper;
import com.ddfplus.util.XMLNode;

/**
 * OHLC Minute Bar
 */
public class Ohlc {

	private final String _symbol;
	private char _day;
	private int _interval = 0;
	private long _time = 0L;
	private char _baseCode = '\0';
	// stats
	private float _open = 0.0f;
	private float _high = 0.0f;
	private float _low = 0.0f;
	private float _close = 0.0f;
	private int _volume = 0;

	public Ohlc(String symbol) {
		_symbol = symbol;
	}

	public String getSymbol() {
		return _symbol;
	}

	public char getDay() {
		return _day;
	}

	public int getInterval() {
		return _interval;
	}

	public long getTime() {
		return _time;
	}

	public char getBaseCode() {
		return _baseCode;
	}

	public float getOpen() {
		return _open;
	}

	public float getHigh() {
		return _high;
	}

	public float getLow() {
		return _low;
	}

	public float getClose() {
		return _close;
	}

	public int getVolume() {
		return _volume;
	}

	public void setDay(char _day) {
		this._day = _day;
	}

	public void setInterval(int _interval) {
		this._interval = _interval;
	}

	public void setTime(long _time) {
		this._time = _time;
	}

	public void setBaseCode(char _baseCode) {
		this._baseCode = _baseCode;
	}

	public void setOpen(float _open) {
		this._open = _open;
	}

	public void setHigh(float _high) {
		this._high = _high;
	}

	public void setLow(float _low) {
		this._low = _low;
	}

	public void setClose(float _close) {
		this._close = _close;
	}

	public void setVolume(int _volume) {
		this._volume = _volume;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(_symbol + ";");
		sb.append("day=" + _day + ";");
		sb.append("time=" + new DDFDate(_time).toDDFString() + ";");
		sb.append("baseCode=" + _baseCode + ";");
		sb.append("open=" + _open + ";");
		sb.append("high=" + _high + ";");
		sb.append("low=" + _low + ";");
		sb.append("close=" + _close + ";");
		sb.append("volume=" + _volume + ";");
		return sb.toString();
	}

	/**
	 * T Deserializes the <code>XMLNode</code> node and returns the
	 * <code>Ohlc</code> object instance.
	 * 
	 * @param node
	 *            The <code>XMLNode</code> serialized node
	 * @return The deserialized <code>Ohlc</code> object.
	 */

	public static final Ohlc fromXMLNode(final XMLNode node) {

		if (!node.getName().equals("ohlc")) {
			return null;
		}

		final Ohlc ohlc = new Ohlc(node.getAttribute("symbol"));

		ohlc._day = node.getAttribute("day").charAt(0);
		ohlc._baseCode = node.getAttribute("basecode").charAt(0);
		ohlc._interval = Integer.parseInt(node.getAttribute("interval"));
		String v = node.getAttribute("time");
		if (v != null && v.length() > 0) {
			ohlc._time = DDFDate.fromDDFStringOhlc(v).getMillisCST();
		}

		// stats
		ohlc._open = ParserHelper.string2float(node.getAttribute("open"), ohlc._baseCode);
		ohlc._high = ParserHelper.string2float(node.getAttribute("high"), ohlc._baseCode);
		ohlc._low = ParserHelper.string2float(node.getAttribute("low"), ohlc._baseCode);
		ohlc._close = ParserHelper.string2float(node.getAttribute("close"), ohlc._baseCode);
		ohlc._volume = Integer.parseInt(node.getAttribute("volume"));
		return ohlc;

	}

}
