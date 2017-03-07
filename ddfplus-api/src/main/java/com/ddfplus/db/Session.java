/**
 * Copyright 2004 - 2015 Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */

package com.ddfplus.db;

import java.text.NumberFormat;

import com.ddfplus.util.DDFDate;
import com.ddfplus.util.ParserHelper;
import com.ddfplus.util.XMLNode;

/**
 * Holds session statistics for a symbol.
 * 
 * The Session object encapsulates the session data for a given quote object.
 * This data is what you would expect to see for a live, intraday update, e.g.
 * open, high, low, last, etc. However, since there can be multiple sessions in
 * a day, the Session object is distinct from the Quote object.
 * <P>
 * 
 * Holds
 * 
 * @see com.ddfplus.db.Quote
 */
public class Session implements java.lang.Cloneable, java.io.Serializable {

	// Used for Object Serialization
	static final long serialVersionUID = 2144658854597154180L;

	private static final NumberFormat _numberFormatInstance = NumberFormat.getNumberInstance();
	static {
		_numberFormatInstance.setGroupingUsed(false);
		_numberFormatInstance.setMaximumFractionDigits(2);
		_numberFormatInstance.setMinimumFractionDigits(2);
	}

	protected volatile float _close = 0.0f;
	protected volatile float _close2 = 0.0f;
	private DDFDate _day = null;
	
	protected volatile float _high = 0.0f;
	// protected volatile float _last = 0.0f;
	protected volatile float _low = 0.0f;
	protected volatile float _open = 0.0f;
	protected volatile float _open2 = 0.0f;
	protected volatile int _openInterest = 0;

	private final Quote _parentQuote;
	private volatile float _previous = 0.0f;
	protected volatile char _session;
	protected volatile float _settlement = 0.0f;
	protected volatile long _timestamp = 0L;
	protected volatile int _tradeSize = 0;
	protected volatile long _tradeTimestamp = 0L;
	protected volatile long _volume = 0L;
	protected volatile long _numTrades = 0L;
	protected volatile double _priceVolume = 0.0;
	protected volatile float _vwap = 0.0f;

	// Default Constructor
	public Session(Quote parent) {
		this(parent, '\0', '\0');
	}

	public Session(Quote parent, char dayCode, char sessionCode) {
		this._parentQuote = parent;
		this._day = DDFDate.fromDayCode(dayCode);
		this._session = sessionCode;		
	}

	@Override
	public Object clone() { // Implements Cloneable

		Session s = new Session(_parentQuote, ((this._day == null) ? '\0' : this._day.getDayCode()), this._session);

		s._close = _close;
		s._close2 = _close2;
		s._high = _high;

		s.lastArray = lastArray.clone();
		s._settlement = _settlement;

		s._low = _low;
		s._open = _open;
		s._open2 = _open2;
		s._openInterest = _openInterest;
		s._previous = _previous;
		s._timestamp = _timestamp;
		s._tradeSize = _tradeSize;
		s._tradeTimestamp = _tradeTimestamp;
		s._volume = _volume;
		s._numTrades = _numTrades;
		s._priceVolume = _priceVolume;
		s._vwap = _vwap;

		return s;
	}

	/**
	 * @return The Closing price for the session. Will return 0 if the session
	 *         is not closed. The getLast() method will also return the closing
	 *         value if the session is closed.
	 */

	public float getClose() {
		return _close;
	}

	
	public void setClose(float v) {
		this._close = v;
	}

	/**
	 * @return The Close2 price for the session. On some pit-traded commodities,
	 *         the exchange reports multiple closing prices, generally the last
	 *         two trades before or at the closing bell.
	 */

	public float getClose2() {
		return _close2;
	}

	public void setClose2(float v) {
		this._close2 = v;
	}

	/**
	 * @return <B>long</B> The timestamp of the last update to the data in
	 *         milliseconds.
	 */

	public long getTimeInMillis() {
		return _timestamp;
	}

	
	public DDFDate getDay() {
		return this._day;
	}
	
	/**
	 * @return <B>char</B> The Day Code
	 */

	public char getDayCode() {
		if (_day == null)
			return '\0';
		
		return _day.getDayCode();
	}

	
	public void setDayCode(DDFDate day) {
		this._day = day;
	}
	
	public void setDayCode(char code) {
		this._day = DDFDate.fromDayCode(code);
	}

	/**
	 * @return The High price.
	 */

	public float getHigh() {
		return _high;
	}

	/**
	 * @return The Last price.
	 */

	public float getLast() {
		return lastArray[0];
	}

	public float getLast(final int index) {
		switch (index) {
		case 0:
		case 1:
		case 2:
			return lastArray[index];
		default:
			return 0.0f;
		}
	}

	/**
	 * Returns the last trade size, or the size associated with the last last.
	 * 
	 * @return <code>int</code> The trade size
	 */

	public int getLastSize() {
		return _tradeSize;
	}

	/**
	 * @return The Low price for the session.
	 */

	public float getLow() {
		return _low;
	}

	public long getNumberOfTrades() {
		return this._numTrades;
	}

	/**
	 * @return The Open price for the session.
	 */

	public float getOpen() {
		return _open;
	}

	/**
	 * @return The Open2 price for the session. On some pit-traded commodities,
	 *         the exchange reports both the first trade and the second trade as
	 *         open and open2. This only applies to certain pit-traded
	 *         commodities, namely the grains on the CBOT.
	 */

	public float getOpen2() {
		return _open2;
	}

	public void setOpen2(float v) {
		this._open2 = v;
	}

	/**
	 * @return The Open Interest
	 */

	public int getOpenInterest() {
		return _openInterest;
	}

	public void setOpenInterest(int v) {
		_openInterest = v;
	}

	/**
	 * @return the Previous session's close/settle.
	 */

	public float getPrevious() {
		return _previous;
	}

	public double getPriceVolume() {
		return this._priceVolume;
	}

	public float getSettlement() {
		return this._settlement;
	}

	public void setSettlement(float f) {
		_settlement = f;
	}

	/**
	 * Returns the timestamp (in millis) of the last trade.
	 * 
	 * @return <code>long</code> The timestamp (in millis)
	 */

	public long getTradeTimestamp() {
		return _tradeTimestamp;
	}

	public void setTradeTimestamp(long ts) {
		this._tradeTimestamp = ts;
	}

	/**
	 * Returns the Volume Weighted Average Price
	 * 
	 * @return <code>double</code> The Volume Weighted Average Price
	 */

	public float getVWAP() {
		return this._vwap;
	}

	/**
	 * Sets the last price. Used internally.
	 */

	static final int LAST_LIMIT = 3;

	private volatile float[] lastArray = new float[LAST_LIMIT];

	public void setLast(final float last) {
		lastArray[2] = lastArray[1];
		lastArray[1] = lastArray[0];
		lastArray[0] = last;

		// System.err.println(" LA0=" + lastArray[0] + " LA1=" + lastArray[1]
		// + " LA2=" + lastArray[2]);
	}

	public void setLastSize(int value) {
		_tradeSize = value;
	}

	/**
	 * @return <B>char</B> The Session Code
	 */

	public char getSessionCode() {
		return _session;
	}

	public void setSessionCode(char code) {
		this._session = code;
	}

	/**
	 * @return The cumulative volume for the session.
	 */

	public long getVolume() {
		return _volume;
	}

	/**
	 * Deserializes the object from an XMLNode object.
	 * 
	 * @param node
	 *            The <code>XMLNode</code> containing the serialized
	 *            <code>Session</code>.
	 */

	public void fromXMLNode(XMLNode node) {

		String s = node.getAttribute("day");
		if ((s != null) && (s.length() > 0))
			_day = DDFDate.fromDayCode(s.charAt(0));

		s = node.getAttribute("session");
		if ((s != null) && (s.length() > 0))
			_session = s.charAt(0);

		s = node.getAttribute("timestamp");
		if (s != null)
			_timestamp = DDFDate.fromDDFString(s).getMillisCST();

		s = node.getAttribute("tradetime");
		if (s != null)
			_tradeTimestamp = DDFDate.fromDDFString(s).getMillisCST();

		s = node.getAttribute("open");
		if (s != null)
			_open = ParserHelper.string2float(s, _parentQuote.getSymbolInfo().getBaseCode());

		s = node.getAttribute("open2");
		if (s != null)
			_open2 = ParserHelper.string2float(s, _parentQuote.getSymbolInfo().getBaseCode());

		s = node.getAttribute("high");
		if (s != null)
			_high = ParserHelper.string2float(s, _parentQuote.getSymbolInfo().getBaseCode());

		s = node.getAttribute("low");
		if (s != null)
			_low = ParserHelper.string2float(s, _parentQuote.getSymbolInfo().getBaseCode());

		s = node.getAttribute("last");
		if (s != null)
			setLast(ParserHelper.string2float(s, _parentQuote.getSymbolInfo().getBaseCode()));

		s = node.getAttribute("close");
		if (s != null)
			_close = ParserHelper.string2float(s, _parentQuote.getSymbolInfo().getBaseCode());

		s = node.getAttribute("close2");
		if (s != null)
			_close2 = ParserHelper.string2float(s, _parentQuote.getSymbolInfo().getBaseCode());

		s = node.getAttribute("previous");
		if (s != null)
			_previous = ParserHelper.string2float(s, _parentQuote.getSymbolInfo().getBaseCode());

		s = node.getAttribute("settlement");
		if (s != null)
			_settlement = ParserHelper.string2float(s, _parentQuote.getSymbolInfo().getBaseCode());

		s = node.getAttribute("tradesize");
		if (s != null)
			_tradeSize = ParserHelper.string2int(s);

		s = node.getAttribute("openinterest");
		if (s != null)
			_openInterest = ParserHelper.string2int(s);

		s = node.getAttribute("volume");
		if (s != null)
			_volume = ParserHelper.string2int(s);

		s = node.getAttribute("numtrades");
		if (s != null)
			_numTrades = ParserHelper.string2int(s);

		s = node.getAttribute("pricevolume");
		try {
			if (s != null)
				_priceVolume = Double.parseDouble(s);
		} catch (Exception e) {
			;
		}

		s = node.getAttribute("vwap");
		try {
			if ((s != null) && (s.length() > 0))
				this._vwap = ParserHelper.string2float(s, _parentQuote.getSymbolInfo().getBaseCode());
		} catch (Exception e) {
			;
		}

	}

	public void setHigh(float value) {
		_high = value;
	}

	public void setLow(float value) {
		_low = value;
	}

	public void setNumberOfTrades(long value) {
		this._numTrades = value;
	}

	public void setOpen(float value) {
		_open = value;
	}

	public void setPrevious(float value) {
		_previous = value;
	}

	public void setPriceVolume(double value) {
		this._priceVolume = value;
	}

	public void setTimeInMillis(long value) {
		this._timestamp = value;
	}

	public void setVolume(long value) {
		_volume = value;
	}

	public void setVWAP(float value) {
		this._vwap = value;
	}

	/*
	 * Serializes the <code>Session</code> into an <code>XMLNode</code> object.
	 */
	public XMLNode toXMLNode() {
		int uc = _parentQuote.getSymbolInfo().getUnitCode();

		XMLNode node = new XMLNode("SESSION");

		if (_day != null)
			node.setAttribute("day", "" + _day.getDayCode());

		if (_session != '\0')
			node.setAttribute("session", "" + _session);

		if (_timestamp > 0) {
			DDFDate d = new DDFDate(_timestamp);
			node.setAttribute("timestamp", d.toDDFString());
		}

		if (_open != ParserHelper.DDFAPI_NOVALUE)
			node.setAttribute("open", Integer.toString(ParserHelper.float2int(uc, _open)));

		if (_open2 != ParserHelper.DDFAPI_NOVALUE)
			node.setAttribute("open2", Integer.toString(ParserHelper.float2int(uc, _open2)));

		if (_high != ParserHelper.DDFAPI_NOVALUE)
			node.setAttribute("high", Integer.toString(ParserHelper.float2int(uc, _high)));

		if (_low != ParserHelper.DDFAPI_NOVALUE)
			node.setAttribute("low", Integer.toString(ParserHelper.float2int(uc, _low)));

		float f = getLast();
		if (f != ParserHelper.DDFAPI_NOVALUE)
			node.setAttribute("last", Integer.toString(ParserHelper.float2int(uc, f)));

		if (_close != ParserHelper.DDFAPI_NOVALUE)
			node.setAttribute("close", Integer.toString(ParserHelper.float2int(uc, _close)));

		if (_close2 != ParserHelper.DDFAPI_NOVALUE)
			node.setAttribute("close2", Integer.toString(ParserHelper.float2int(uc, _close2)));

		if (_previous != ParserHelper.DDFAPI_NOVALUE)
			node.setAttribute("previous", Integer.toString(ParserHelper.float2int(uc, _previous)));

		if (_settlement != ParserHelper.DDFAPI_NOVALUE)
			node.setAttribute("settlement", Integer.toString(ParserHelper.float2int(uc, _settlement)));

		if (_tradeSize != ParserHelper.DDFAPI_NOVALUE)
			node.setAttribute("tradesize", "" + _tradeSize);

		if (_openInterest != ParserHelper.DDFAPI_NOVALUE)
			node.setAttribute("openinterest", "" + _openInterest);

		if (_volume != ParserHelper.DDFAPI_NOVALUE)
			node.setAttribute("volume", "" + _volume);

		if (_numTrades > 0)
			node.setAttribute("numtrades", "" + _numTrades);

		if (_priceVolume != ParserHelper.DDFAPI_NOVALUE)
			node.setAttribute("pricevolume", "" + _numberFormatInstance.format(_priceVolume));

		if (_vwap != ParserHelper.DDFAPI_NOVALUE)
			node.setAttribute("vwap", Integer.toString(ParserHelper.float2int(uc, _vwap)));

		if (_tradeTimestamp > 0L) {
			DDFDate d = new DDFDate(_tradeTimestamp);
			node.setAttribute("tradetime", d.toDDFString());
		}

		StringBuilder ticks = new StringBuilder();
		for (int i = 0; i < lastArray.length - 1; i++) {
			if (lastArray[i + 1] == 0.0f)
				break;
			if (lastArray[i] > lastArray[i + 1])
				ticks.append("+");
			else if (lastArray[i] < lastArray[i + 1])
				ticks.append("-");
			else
				ticks.append(".");
		}
		if (ticks.length() > 0)
			node.setAttribute("ticks", ticks.toString());

		return node;
	}
}
