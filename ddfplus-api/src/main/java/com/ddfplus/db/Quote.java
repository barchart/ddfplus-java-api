/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */

package com.ddfplus.db;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.ddfplus.enums.MarketConditionType;
import com.ddfplus.messages.DdfMarketBase;
import com.ddfplus.util.DDFDate;
import com.ddfplus.util.ParserHelper;
import com.ddfplus.util.XMLNode;

/**
 * The Quote class encapsulates the complete quotation information for a given
 * Symbol. It contains Session objects which have the data. The Quote object
 * also contains formatting and other information.
 */
public class Quote implements Cloneable, Serializable {

	// By defining the serialVersionUID we can keep Object Serialization
	// consistent.
	private static final long serialVersionUID = 9094149815858170620L;

	private volatile String _ddfExchange = "";
	private final SymbolInfo _symbolInfo;
	// Prices
	private volatile float _ask = 0.0f;
	private volatile int _askSize = 0;
	private volatile float _bid = 0.0f;
	private volatile int _bidSize = 0;

	// Sessions
	protected volatile Session _combinedSession = new Session(this);
	protected volatile Session _previousSession = new Session(this);
	protected volatile Session _electronicSession = new Session(this);
	private final List<Session> _sessions = new CopyOnWriteArrayList<Session>();

	private volatile char _flag = '\0';
	private volatile MarketConditionType _marketCondition = MarketConditionType.NORMAL;
	protected volatile DateTime _lastUpdated = null;

	// Original DDF Message
	private volatile DdfMarketBase _message = null;
	private volatile char _permission = '\0';

	public Quote(SymbolInfo symbolInfo) {
		this._symbolInfo = symbolInfo;
	}

	/**
	 * Clones the Quote object. This creates a copy of the Quote object (as
	 * opposed to just a ponter reference. Useful if you implementing delay
	 * queues, or anything that requires a second physical copy of the Quote.
	 */
	@Override
	public Object clone() {

		Quote q = new Quote(_symbolInfo);

		q._ask = _ask;
		q._askSize = _askSize;
		q._bid = _bid;
		q._bidSize = _bidSize;
		q._ddfExchange = _ddfExchange;
		q._flag = _flag;
		q._marketCondition = _marketCondition;

		q._message = _message;
		q._permission = _permission;

		q._combinedSession = (Session) _combinedSession.clone();
		q._electronicSession = (Session) _electronicSession.clone();
		q._previousSession = (Session) _previousSession.clone();

		q._sessions.addAll(_sessions);

		return q;

	}

	/**
	 * Creates a session given a specific day and session code, stores it, and
	 * returns it. Only creates the session if necessary otherwise returns the
	 * current session.
	 * 
	 * @param dayCode
	 *            Day Code
	 * @param sessionCode
	 *            Session Code
	 * 
	 * @return Session Current session object.
	 */
	public Session createSession(char dayCode, char sessionCode) {
		Session session = this.getSession(dayCode, sessionCode);
		if (session == null) {
			session = new Session(this, dayCode, sessionCode);

			if ((sessionCode == 'R') || (sessionCode == 'T'))
				_sessions.add(session);
		}

		return session;
	}

	/**
	 * Returns the best Ask (offer) price.
	 * 
	 * @return <code>float</code> The Ask price
	 */

	public float getAsk() {
		return _ask;
	}

	/**
	 * Returns the size of the best Ask (Offer).
	 * 
	 * @return <code>int</code> The Ask size
	 */

	public int getAskSize() {
		return _askSize;
	}

	/**
	 * Returns the best bid price.
	 * 
	 * @return The Bid price
	 */
	public float getBid() {
		return _bid;
	}

	/**
	 * Returns the size of the best bid.
	 * 
	 * @return <code>int</code> The bid size
	 */

	public int getBidSize() {
		return _bidSize;
	}

	/**
	 * Returns the change, i.e. the difference between the last price and the
	 * previous day's last (or settling) price.
	 * 
	 * @return <code>float</code> The change
	 */

	public float getChange() {
		float last = this._combinedSession.getLast();
		if (last == 0.0f)
			return 0.0f;

		float prev = _previousSession.getLast();
		if (prev == 0.0f)
			return 0.0f;

		return last - prev;
	}

	/**
	 * Returns the combined trading session for the Quote. The combined session
	 * is the standard session used, and includes both electronic and pit
	 * session, if applicable. For equities, there is only one session.
	 * 
	 * @return <code>Session</code> The combined session object.
	 */

	public Session getCombinedSession() {
		return _combinedSession;
	}

	/**
	 * Returns the ddf exchange code that the Quote is on.
	 * 
	 * @return <code>String</code> - The ddf exchange code.
	 */

	public String getDDFExchange() {
		return _ddfExchange;
	}

	/**
	 * Returns the electronic trading session for the Quote. The electronic
	 * session has different interpretaions, based on the exchange. Some
	 * exchanges do not offer electronic data.
	 * 
	 * @return <code>Session</code> The electronic session object.
	 * @deprecated
	 */

	@Deprecated
	public Session getElectronicSession() {
		return _electronicSession;
	}

	/**
	 * Returns the flag (if any) associated with the quote. Flags could be 'c'
	 * for close, 's' for settle.
	 * 
	 * @return <code>char</code> - the flag
	 */
	public char getFlag() {
		return _flag;
	}

	/**
	 * Last time the Quote was updated in ms.
	 * 
	 * @return update time in ms.
	 */
	public long getLastUpdated() {
		return (_lastUpdated == null) ? 0 : _lastUpdated.getMillis();
	}

	public void setLastUpdated(DateTime dt) {
		_lastUpdated = dt;
	}

	/**
	 * Returns the last Message object that influenced this Quote.
	 * 
	 * @return <code>Message</code> The original Message object
	 */

	public DdfMarketBase getMessage() {
		return _message;
	}

	/**
	 * Return the permission of the quote. One of 'R', 'S', 'I'.
	 * 
	 * @return <code>char</code> The mode/permission of the quote.
	 */

	public char getPermission() {
		return _permission;
	}

	/**
	 * Returns the previous trading session for the Quote. The previous session
	 * is used to calculate changes, and to see the data for "yesterday."
	 * 
	 * @return <code>Session</code> The previous session object.
	 */

	public Session getPreviousSession() {
		return _previousSession;
	}

	/**
	 * Returns the specific session for a day and session.
	 * 
	 * @param dayCode
	 *            Day Code
	 * @param sessionCode
	 *            Session Code
	 * 
	 * @return Session
	 */
	public Session getSession(char dayCode, char sessionCode) {
		for (Session session : _sessions) {
			if ((session.getDayCode() == dayCode) && (session.getSessionCode() == sessionCode))
				return session;
		}

		return null;
	}

	/**
	 * Returns the symbol of the Quote object, e.g. IBM or ZNZ7
	 * 
	 * @return <B>String</B> The symbol
	 */

	public SymbolInfo getSymbolInfo() {
		return _symbolInfo;
	}

	public void setAsk(float value) {
		_ask = value;
	}

	public void setAskSize(int value) {
		_askSize = value;
	}

	public void setBid(float value) {
		_bid = value;
	}

	public void setBidSize(int value) {
		_bidSize = value;
	}

	/**
	 * Sets the ddf exchange code.
	 * 
	 * @param value
	 *            <code>String</code> The ddf exchange
	 */

	public void setDDFExchange(String value) {
		_ddfExchange = value;
	}

	/**
	 * Sets the flag.
	 * 
	 * @param value
	 *            <code>char</code> The flag
	 */

	public void setFlag(char value) {
		_flag = value;
	}

	/**
	 * Sets the Market Condition for the symbol.
	 * 
	 * @param value
	 *            <code>char</code> The Market Condition
	 */
	public void setMarketCondition(MarketConditionType value) {
		_marketCondition = value;
	}

	/**
	 * Sets the Message object to the quote. This allows the user app to pull
	 * extra data and read deeper into the quote.
	 * 
	 * @param message
	 *            <code>Message</code> The Message object
	 */

	protected void setMessage(DdfMarketBase message) {
		_message = message;
		if (_ddfExchange.length() < 1)
			_ddfExchange = "" + _message.getExchange();
	}

	public void setPermission(char c) {
		_permission = c;
	}

	public void setCombinedSession(Session session) {
		_combinedSession = session;
	}

	public void setPreviousSession(Session session) {
		_previousSession = session;
	}

	/**
	 * Last Updated in UTC, because this is used by PHP quote systems to compare
	 * against db data, to determine which is fresher.
	 */

	public void updateLastUpdated() {
		// XXX TIME!ZONE
		_lastUpdated = new DateTime(DateTimeZone.UTC);
	}

	public String toJSONString() {
		StringBuilder sb = new StringBuilder("\""
				+ this._symbolInfo.getSymbol()
				+ "\": { "
				+ "\"symbol\": \""
				+ this._symbolInfo.getSymbol()
				+ "\""
				+ ", \"name\": \""
				+ this._symbolInfo.getName().replaceAll("\"", "\\\\\"")
				+ "\""
				+ ", \"exchange\": \""
				+ this._symbolInfo.getExchange()
				+ "\""
				+ ", \"basecode\": \""
				+ this._symbolInfo.getBaseCode()
				+ "\""
				+ ", \"pointvalue\": "
				+ this._symbolInfo.getPointValue()
				+ ", \"tickincrement\": "
				+ this._symbolInfo.getTickIncrement()
				+ ", \"ddfexchange\": "
				+ (((this._ddfExchange == null) || (this._ddfExchange.length() == 0)) ? "null" : "\""
						+ this._ddfExchange + "\"") + ", \"flag\": "
				+ ((this._flag != '\0') ? ("\"" + this._flag + "\"") : "null") + ", \"lastupdate\": "
				+ ((this._lastUpdated != null) ? (new DDFDate(_lastUpdated)).toDDFString() : "0"));

		sb.append(", \"bid\": "
				+ ((_bid == ParserHelper.DDFAPI_NOVALUE) ? "null" : ParserHelper.float2string(_bid,
						this._symbolInfo.getBaseCode(), ParserHelper.PURE_DECIMAL))
				+ ", \"bidsize\": "
				+ ((_bidSize == ParserHelper.DDFAPI_NOVALUE) ? "null" : _bidSize * 100)
				+ ", \"ask\": "
				+ ((_ask == ParserHelper.DDFAPI_NOVALUE) ? "null" : ParserHelper.float2string(_ask,
						this._symbolInfo.getBaseCode(), ParserHelper.PURE_DECIMAL)) + ", \"asksize\": "
				+ ((_askSize == ParserHelper.DDFAPI_NOVALUE) ? "null" : _askSize * 100));

		Session session = this._combinedSession;
		Session session_t = this.getSession(this._combinedSession.getDayCode(), 'T');

		sb.append(", " + "\"open\": "
				+ ((session.getOpen() == ParserHelper.DDFAPI_NOVALUE) ? "null" : ParserHelper.float2string(
						session.getOpen(), this._symbolInfo.getBaseCode(), ParserHelper.PURE_DECIMAL))
				+ ", \"high\": "
				+ ((session.getHigh() == ParserHelper.DDFAPI_NOVALUE) ? "null" : ParserHelper.float2string(
						session.getHigh(), this._symbolInfo.getBaseCode(), ParserHelper.PURE_DECIMAL))
				+ ", \"low\": "
				+ ((session.getLow() == ParserHelper.DDFAPI_NOVALUE) ? "null" : ParserHelper.float2string(
						session.getLow(), this._symbolInfo.getBaseCode(), ParserHelper.PURE_DECIMAL))
				+ ", \"last\": "
				+ ((session.getLast() == ParserHelper.DDFAPI_NOVALUE) ? "null" : ParserHelper.float2string(
						session.getLast(), this._symbolInfo.getBaseCode(), ParserHelper.PURE_DECIMAL))
				+ ", \"last2\": "
				+ ((session.getLast(1) == ParserHelper.DDFAPI_NOVALUE) ? "null" : ParserHelper.float2string(
						session.getLast(1), this._symbolInfo.getBaseCode(), ParserHelper.PURE_DECIMAL))
				+ ", \"last3\": "
				+ ((session.getLast(2) == ParserHelper.DDFAPI_NOVALUE) ? "null" : ParserHelper.float2string(
						session.getLast(2), this._symbolInfo.getBaseCode(), ParserHelper.PURE_DECIMAL))
				+ ", \"last_t\": "
				+ (((session_t != null) && (session_t.getLast() != ParserHelper.DDFAPI_NOVALUE)) ? ParserHelper
						.float2string(session_t.getLast(), this._symbolInfo.getBaseCode(), ParserHelper.PURE_DECIMAL)
						: "null")
				+ ", \"lastsize\": "
				+ ((session.getLastSize() == ParserHelper.DDFAPI_NOVALUE) ? "null" : session.getLastSize())
				+ ", \"tradetimestamp\": "
				+ session.getTradeTimestamp()
				+ ", \"settlement\": "
				+ ((session.getSettlement() == ParserHelper.DDFAPI_NOVALUE) ? "null" : ParserHelper.float2string(
						session.getSettlement(), this._symbolInfo.getBaseCode(), ParserHelper.PURE_DECIMAL))
				+ ", \"previous\": "
				+ ((session.getPrevious() == ParserHelper.DDFAPI_NOVALUE) ? "null" : ParserHelper.float2string(
						session.getPrevious(), this._symbolInfo.getBaseCode(), ParserHelper.PURE_DECIMAL))
				+ ", \"volume\": "
				+ ((session.getVolume() == ParserHelper.DDFAPI_NOVALUE) ? "null" : session.getVolume())
				+ ", \"openinterest\": "
				+ ((this._previousSession.getOpenInterest() == ParserHelper.DDFAPI_NOVALUE) ? "null"
						: this._previousSession.getOpenInterest()) + ", \"numtrades\": " + session.getNumberOfTrades()
				+ ", \"pricevolume\": " + session.getPriceVolume());

		sb.append(" }");
		return sb.toString();
	}

	public XMLNode toXMLNode() {
		return toXMLNode(true);
	}

	/**
	 * Converts the Quote object into an XMLNode, which can then be used to
	 * bring the Quote into a textual form.
	 * 
	 * @param showBidAsk
	 *            Will add bid and ask in the Object
	 * @return <code>XMLNode</code> The XMLNode representing this Quote.
	 */
	public XMLNode toXMLNode(boolean showBidAsk) {
		XMLNode node = new XMLNode("QUOTE");
		node.setAttribute("symbol", _symbolInfo.getSymbol());
		node.setAttribute("name", _symbolInfo.getName());
		node.setAttribute("exchange", _symbolInfo.getExchange());
		node.setAttribute("basecode", "" + _symbolInfo.getBaseCode());
		node.setAttribute("pointvalue", "" + _symbolInfo.getPointValue());
		node.setAttribute("tickincrement", "" + _symbolInfo.getTickIncrement());

		if ((_ddfExchange != null) && (_ddfExchange.length() > 0))
			node.setAttribute("ddfexchange", _ddfExchange);

		if (_flag != '\0')
			node.setAttribute("flag", "" + _flag);

		if (_marketCondition != MarketConditionType.NORMAL)
			node.setAttribute("marketcondition", "" + _marketCondition.getCode());

		if (_lastUpdated != null) {
			DDFDate d = new DDFDate(_lastUpdated);
			node.setAttribute("lastupdate", d.toDDFString());
		}

		if (showBidAsk) {
			if (_bid != ParserHelper.DDFAPI_NOVALUE)
				node.setAttribute("bid", Integer.toString(ParserHelper.float2int(_symbolInfo.getUnitCode(), _bid)));
			if (_bidSize != ParserHelper.DDFAPI_NOVALUE)
				node.setAttribute("bidsize", "" + _bidSize);
			if (_ask != ParserHelper.DDFAPI_NOVALUE)
				node.setAttribute("ask", Integer.toString(ParserHelper.float2int(_symbolInfo.getUnitCode(), _ask)));
			if (_askSize != ParserHelper.DDFAPI_NOVALUE)
				node.setAttribute("asksize", "" + _askSize);
		}

		XMLNode n1 = _combinedSession.toXMLNode();
		n1.setAttribute("id", "combined");
		node.addNode(n1);

		XMLNode n2 = _previousSession.toXMLNode();
		n2.setAttribute("id", "previous");
		node.addNode(n2);

		for (Session session : _sessions) {
			XMLNode n = session.toXMLNode();
			n.setAttribute("id", "session_" + n.getAttribute("day") + "_" + n.getAttribute("session"));
			node.addNode(n);
		}

		return node;
	}

	/**
	 * Takes in an XMLNode object, and parses this into a Quote object.
	 * 
	 * @param node
	 *            XML Quote Object
	 * @return Quote
	 */
	public static Quote fromXMLNode(XMLNode node) {

		if (!node.getName().equals("QUOTE"))
			return null;

		Quote qte = new Quote(new SymbolInfo(node.getAttribute("symbol"), node.getAttribute("name"),
				node.getAttribute("exchange"), node.getAttribute("basecode").charAt(0),
				((node.getAttribute("pointvalue") != null) ? Float.parseFloat(node.getAttribute("pointvalue")) : 1.0f),
				((node.getAttribute("tickincrement") != null) ? Integer.parseInt(node.getAttribute("tickincrement"))
						: 1)));

		String s = node.getAttribute("ddfexchange");
		if (s != null)
			qte._ddfExchange = node.getAttribute("ddfexchange");

		s = node.getAttribute("flag");
		if ((s != null) && (s.length() > 0))
			qte._flag = s.charAt(0);

		s = node.getAttribute("marketcondition");
		if ((s != null) && (s.length() > 0))
			qte._marketCondition = MarketConditionType.getByCode(s.charAt(0));

		s = node.getAttribute("bid");
		if (s != null)
			qte._bid = ParserHelper.string2float(s, qte.getSymbolInfo().getBaseCode());

		s = node.getAttribute("bidsize");
		if (s != null)
			qte._bidSize = ParserHelper.string2int(s);

		s = node.getAttribute("ask");
		if (s != null)
			qte._ask = ParserHelper.string2float(s, qte.getSymbolInfo().getBaseCode());

		s = node.getAttribute("asksize");
		if (s != null)
			qte._askSize = ParserHelper.string2int(s);

		s = node.getAttribute("mode");
		if (s != null)
			qte._permission = s.charAt(0);

		for (XMLNode n : node.getAllNodes("SESSION")) {
			Session session = new Session(qte);
			session.fromXMLNode(n);

			qte._sessions.add(session);

			if (n.getAttribute("id").equals("combined"))
				qte._combinedSession = session;
			else if (n.getAttribute("id").equals("previous"))
				qte._previousSession = session;
		}
		return qte;
	}

	public boolean isTick() {
		return _message.getQuoteType().isTick();
	}

	public boolean isRefresh() {
		return _message.getQuoteType().isRefresh();
	}

}
