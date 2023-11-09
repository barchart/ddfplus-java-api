/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * <p>
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */

package com.ddfplus.db;

import com.ddfplus.enums.MarketConditionType;
import com.ddfplus.messages.DdfMarketBase;
import com.ddfplus.util.DDFDate;
import com.ddfplus.util.ParserHelper;
import com.ddfplus.util.XMLNode;

import java.io.Serializable;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The Quote class encapsulates the complete quotation information for a given
 * Symbol. It contains Session objects which have the data. The Quote object
 * also contains formatting and other information.
 */
public class Quote implements Cloneable, Serializable {

    public static enum CacheAge { SevenDays, SixWeeks,SixWeeksPlus }

    public static final ZoneId ZONE_ID_CHICAGO = ZoneId.of("America/Chicago");
    // By defining the serialVersionUID we can keep Object Serialization
    // consistent.
    private static final long serialVersionUID = 9094149815858170620L;

    private Clock clock = Clock.system(ZONE_ID_CHICAGO);
    private volatile String _ddfExchange = "";
    private final SymbolInfo _symbolInfo;
    // Request symbol can be short symbol
    private String _requestSymbol = null;
    // Prices
    private volatile float _ask = 0.0f;
    private volatile int _askSize = 0;
    private volatile float _bid = 0.0f;
    private volatile int _bidSize = 0;

    // Sessions
    protected volatile Session _combinedSession = new Session(this);
    protected volatile Session _previousSession = new Session(this);
    protected volatile Session _zSession = null;
    private final List<Session> _sessions = new CopyOnWriteArrayList<Session>();

    private volatile char _flag = '\0';
    private volatile MarketConditionType _marketCondition = MarketConditionType.NORMAL;
    protected volatile long _lastUpdated = 0;

    // Original DDF Message
    private volatile DdfMarketBase _message = null;
    private volatile char _permission = '\0';
    // Openfeed Fields
    private long _seqNo;
    private long _marketId;
    private long _cacheTimeMs;
    private CacheAge _cacheAge;

    public Quote(SymbolInfo symbolInfo) {
        this._symbolInfo = symbolInfo;
    }

    /**
     * Clones the Quote object. This creates a copy of the Quote object (as
     * opposed to just a pointer reference. Useful if you implementing delay
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
        q._lastUpdated = this._lastUpdated;
        q._marketCondition = _marketCondition;

        q._message = _message;
        q._permission = _permission;

        q._combinedSession = (Session) _combinedSession.clone();
        q._previousSession = (Session) _previousSession.clone();
        if (_zSession != null) {
            q._zSession = (Session) _zSession.clone();
        }

        q._sessions.addAll(_sessions);
        q._requestSymbol = _requestSymbol;
        q._seqNo = _seqNo;
        q._marketId = _marketId;
        q._cacheTimeMs = _cacheTimeMs;
        q._cacheAge = _cacheAge;
        return q;

    }

    /**
     * Creates a session given a specific day and session code, stores it, and
     * returns it. Only creates the session if necessary otherwise returns the
     * current session.
     *
     * @param dayCode     Day Code
     * @param sessionCode Session Code
     * @return Session Current session object.
     */
    public Session createSession(char dayCode, char sessionCode) {
        Session session = this.getSession(dayCode, sessionCode);
        if (session == null) {
            session = new Session(this, DDFDate.fromDayCode(dayCode), sessionCode);

            if ((sessionCode == 'R') || (sessionCode == 'T'))
                _sessions.add(session);
        }

        return session;
    }

    /**
     * Creates a session given a specific day and session code, stores it, and
     * returns it. Only creates the session if necessary otherwise returns the
     * current session.
     *
     * @param date        date
     * @param sessionCode Session Code
     * @return Session Current session object.
     */
    public Session createSession(DDFDate date, char sessionCode) {
        char dayCode = date.getDayCode();

        Session session = this.getSession(dayCode, sessionCode);
        if (session == null) {
            session = new Session(this, date, sessionCode);

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
        return _lastUpdated;
    }

    /**
     * Gets the Market Condition for the symbol.
     *
     * @return Market Condition
     */

    public MarketConditionType getMarketCondition() {
        return _marketCondition;
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
     * Returns the 'Z' trading session for the Quote. The Z session includes
     * all trades, even the ones that do not update Last.
     *
     * @return <code>Session</code> The 'Z' session object.
     */
    public Session getZSession() {
        return _zSession;
    }

    /**
     * Returns the 'Z' trading session for the Quote. The Z session includes
     * all trades, even the ones that do not update Last.
     *
     * @return <code>Session</code> The 'Z' session object.
     */
    public Session createZSession() {
        if (_zSession == null) {
            _zSession = new Session(this);
        }
        return _zSession;
    }

    /**
     * Returns the specific session for a day and session.
     *
     * @param dayCode     Day Code
     * @param sessionCode Session Code
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
     * Sets the flag.
     *
     * @param value <code>char</code> The flag
     */

    public void setFlag(char value) {
        _flag = value;
    }


    public void setCombinedSession(Session session) {
        _combinedSession = session;
    }


    /**
     * Sets the Market Condition for the symbol.
     *
     * @param value <code>char</code> The Market Condition
     */

    public void setMarketCondition(MarketConditionType value) {
        _marketCondition = value;
    }

    /**
     * Sets the Message object to the quote. This allows the user app to pull
     * extra data and read deeper into the quote.
     *
     * @param message <code>Message</code> The Message object
     */

    protected void setMessage(DdfMarketBase message) {
        _message = message;
        if (_ddfExchange.length() < 1)
            _ddfExchange = "" + _message.getExchange();
    }


    /**
     * Sets the ddf exchange code.
     *
     * @param value <code>String</code> The ddf exchange
     */

    public void setDDFExchange(String value) {
        _ddfExchange = value;
    }


    public void setPermission(char c) {
        _permission = c;
    }


    public void setPreviousSession(Session session) {
        _previousSession = session;
    }

    /**
     * Last Updated in UTC, because this is used by PHP quote systems to compare
     * against db data, to determine which is fresher.
     */

    public void updateLastUpdated() {
        _lastUpdated = System.currentTimeMillis();
    }


    public void updateLastUpdated(long millis) {
        _lastUpdated = millis;
    }

    public String toJSONString() {
        return toJSONString(0, true, false);
    }

    public String toJSONString(int version, boolean displayBbo, boolean useEquityExtendedDecimals) {
        char baseCode = this._symbolInfo.getBaseCode();
        boolean opra = this._symbolInfo.getExchange().equals("OPRA") ? true : false;
        // US Exchange Equity
        boolean usEquity = false;
        if (useEquityExtendedDecimals) {
            String exchange = this._symbolInfo.getExchange();
            switch (exchange) {
                case "NYSE":
                case "NASDAQ":
                case "AMEX":
                case "OTC":
                    baseCode = 'C';
                    usEquity = true;
                    break;
                case "TSX":
                case "TSXV":
                case "TSX-V":
                    baseCode = 'B';
                    usEquity = true;
                    break;
            }
        } else {
            // TSX, Venture
            switch (this._symbolInfo.getExchange()) {
                case "TSX":
                case "TSXV":
                case "TSX-V":
                    usEquity = true;
                    break;
            }
        }

        Session session = this._combinedSession;
        Session session_t = this.getSession(this._combinedSession.getDayCode(), 'T');

        boolean useZSessionAsCurrentSession = isZSessionNewerThanCurrentSession();
        boolean useZSessionForCurrentSession = isZSessionForCurrentSession();
        Session previousSession = this._previousSession;

        if (useZSessionAsCurrentSession) {
            session = _zSession;
            previousSession = _combinedSession;
            session_t = this.getSession(this._zSession.getDayCode(), 'T');
        }

        StringBuilder sb = new StringBuilder("\"" + this._symbolInfo.getSymbol() + "\": { " + "\"symbol\": \""
                + this._symbolInfo.getSymbol() + "\""
                + (this._symbolInfo.getLongSymbol() != null ? (", \"longsymbol\": \"" + this._symbolInfo.getLongSymbol() + "\"") : "")
                + ", \"name\": \""
                + this._symbolInfo.getName().replaceAll("\"", "\\\\\"") + "\"" + ", \"exchange\": \""
                + this._symbolInfo.getExchange() + "\"" + ", \"basecode\": \"" + this._symbolInfo.getBaseCode() + "\""
                + ", \"pointvalue\": " + this._symbolInfo.getPointValue() + ", \"tickincrement\": "
                + this._symbolInfo.getTickIncrement() + ", \"ddfexchange\": "
                + (((this._ddfExchange == null) || (this._ddfExchange.length() == 0)) ? "null"
                : "\"" + this._ddfExchange + "\"")
        );

        if (session.getDay() != null) {
            sb.append(
                    ", \"day\": \"" + session.getDayCode() + "\", ");
            String dt = LocalDate.from(session.getDay().getDate()).format(DateTimeFormatter.ISO_DATE);
            sb.append("\"date\": \"" + dt + "\"");
        }

        if (!useZSessionAsCurrentSession) {
            sb.append(", \"flag\": " + ((this._flag != '\0') ? ("\"" + this._flag + "\"") : "null"));
        } else {
            sb.append(", \"flag\": \"p\"");
        }

        sb.append(", \"lastupdate\": " + (new DDFDate(_lastUpdated).toDDFString()));
        sb.append(", \"tzadjustment\": " + session.getTzAdjustment());

        if (displayBbo && !useZSessionAsCurrentSession) {
            sb.append(
                    ", \"bid\": "
                            + ((_bid == ParserHelper.DDFAPI_NOVALUE) ? "null"
                            : ParserHelper.float2string(_bid, baseCode,
                            ParserHelper.PURE_DECIMAL))
                            + ", \"bidsize\": " + ((_bidSize == ParserHelper.DDFAPI_NOVALUE) ? "null" : (usEquity ? _bidSize * 100 : _bidSize))
                            + ", \"ask\": "
                            + ((_ask == ParserHelper.DDFAPI_NOVALUE) ? "null"
                            : ParserHelper.float2string(_ask, baseCode,
                            ParserHelper.PURE_DECIMAL))
                            + ", \"asksize\": " + ((_askSize == ParserHelper.DDFAPI_NOVALUE) ? "null" : (usEquity ? _askSize * 100 : _askSize)));
            if (opra &&  _ask != ParserHelper.DDFAPI_NOVALUE) {
                float midpoint = calcMidPoint();
                sb.append(", \"midpoint\": " + ParserHelper.float2string(midpoint,'C', ParserHelper.PURE_DECIMAL));
            }
        }

        Float voloi = null;
        if (opra && session.getVolume() != ParserHelper.DDFAPI_NOVALUE && session.getOpenInterest() != ParserHelper.DDFAPI_NOVALUE) {
            voloi = calcVolOi(session);
        }
        if (!useZSessionAsCurrentSession) {
            sb.append(", " + "\"open\": "
                            + ((session.getOpen() == ParserHelper.DDFAPI_NOVALUE) ? "null"
                            : ParserHelper.float2string(session.getOpen(), baseCode,
                            ParserHelper.PURE_DECIMAL))
                            + ", \"high\": "
                            + ((session.getHigh() == ParserHelper.DDFAPI_NOVALUE) ? "null"
                            : ParserHelper.float2string(session.getHigh(), baseCode,
                            ParserHelper.PURE_DECIMAL))
                            + ", \"low\": "
                            + ((session.getLow() == ParserHelper.DDFAPI_NOVALUE) ? "null"
                            : ParserHelper.float2string(session.getLow(), baseCode,
                            ParserHelper.PURE_DECIMAL))
                            + ", \"last\": "
                            + ((session.getLast() == ParserHelper.DDFAPI_NOVALUE) ? "null"
                            : ParserHelper.float2string(session.getLast(), baseCode,
                            ParserHelper.PURE_DECIMAL))
                            + ", \"last2\": "
                            + ((session.getLast(1) == ParserHelper.DDFAPI_NOVALUE) ? "null"
                            : ParserHelper.float2string(session.getLast(1), baseCode,
                            ParserHelper.PURE_DECIMAL))
                            + ", \"last3\": "
                            + ((session.getLast(2) == ParserHelper.DDFAPI_NOVALUE) ? "null"
                            : ParserHelper.float2string(session.getLast(2), baseCode,
                            ParserHelper.PURE_DECIMAL))
                            + ", \"last_t\": "
                            + (((session_t != null) && (session_t.getLast() != ParserHelper.DDFAPI_NOVALUE)) ? ParserHelper
                            .float2string(session_t.getLast(), baseCode, ParserHelper.PURE_DECIMAL)
                            : "null")
                            + ", \"lastsize\": "
                            + ((session.getLastSize() == ParserHelper.DDFAPI_NOVALUE) ? "null" : (usEquity ? session.getLastSize() * 100 : session.getLastSize()))
                            + ", \"tradetimestamp\": " + session.getTradeTimestamp() + ", \"settlement\": "
                            + ((session.getSettlement() == ParserHelper.DDFAPI_NOVALUE) ? "null"
                            : ParserHelper.float2string(session.getSettlement(), baseCode,
                            ParserHelper.PURE_DECIMAL))
                            + ", \"previous\": "
                            + ((session.getPrevious() == ParserHelper.DDFAPI_NOVALUE) ? "null"
                            : ParserHelper.float2string(session.getPrevious(), baseCode,
                            ParserHelper.PURE_DECIMAL))
                            + ", \"volume\": "
                            + ((session.getVolume() == ParserHelper.DDFAPI_NOVALUE) ? "null" : session.getVolume())
                            + ", \"openinterest\": "
                            + ((session.getOpenInterest() == ParserHelper.DDFAPI_NOVALUE) ? "null"
                            : session.getOpenInterest())
                            + (voloi != null ? ", \"voloi\": " + voloi : "")
                            + ", \"numtrades\": " + session.getNumberOfTrades() + ", \"pricevolume\": " + ParserHelper.float2string(session.getPriceVolume(), 'A', ParserHelper.PURE_DECIMAL, false)
                            + ", \"timestamp\": " + session.getTimeInMillis()
                            + (useZSessionForCurrentSession ? (
                            ", \"last_z\": "
                                    + ((_zSession.getLast() == ParserHelper.DDFAPI_NOVALUE) ? "null"
                                    : ParserHelper.float2string(_zSession.getLast(), baseCode,
                                    ParserHelper.PURE_DECIMAL))
                                    + ", \"lastsize_z\": "
                                    + ((_zSession.getLastSize() == ParserHelper.DDFAPI_NOVALUE) ? "null" : (usEquity ? _zSession.getLastSize() * 100 : _zSession.getLastSize()))
                                    + ", \"tradetimestamp_z\": " + _zSession.getTradeTimestamp()
                    ) : "")
                            + (version == 1 && _seqNo > 0 ? ", \"seqno\": " + _seqNo : "")
                            + (version >= 1 && _marketId > 0 ? ", \"marketId\": " + _marketId : "")
            );
        } else {
            sb.append(", " + "\"open\": "
                    + ((session.getOpen() == ParserHelper.DDFAPI_NOVALUE) ? "null"
                    : ParserHelper.float2string(session.getOpen(), baseCode,
                    ParserHelper.PURE_DECIMAL))
                    + ", \"high\": "
                    + ((session.getHigh() == ParserHelper.DDFAPI_NOVALUE) ? "null"
                    : ParserHelper.float2string(session.getHigh(), baseCode,
                    ParserHelper.PURE_DECIMAL))
                    + ", \"low\": "
                    + ((session.getLow() == ParserHelper.DDFAPI_NOVALUE) ? "null"
                    : ParserHelper.float2string(session.getLow(), baseCode,
                    ParserHelper.PURE_DECIMAL))
                    + ", \"last_z\": "
                    + ((session.getLast() == ParserHelper.DDFAPI_NOVALUE) ? "null"
                    : ParserHelper.float2string(session.getLast(), baseCode,
                    ParserHelper.PURE_DECIMAL))
                    + ", \"last\": null"
                    + ", \"last2\": null"
                    + ", \"last3\": null"
                    + ", \"last_t\": "
                    + (((session_t != null) && (session_t.getLast() != ParserHelper.DDFAPI_NOVALUE)) ? ParserHelper
                    .float2string(session_t.getLast(), baseCode, ParserHelper.PURE_DECIMAL)
                    : "null")
                    + ", \"lastsize\": null"
                    + ", \"lastsize_z\": "
                    + ((session.getLastSize() == ParserHelper.DDFAPI_NOVALUE) ? "null" : (usEquity ? session.getLastSize() * 100 : session.getLastSize()))
                    + ", \"tradetimestamp\": null"
                    + ", \"tradetimestamp_z\": " + session.getTradeTimestamp()
                    + ", \"settlement\": "
                    + ((session.getSettlement() == ParserHelper.DDFAPI_NOVALUE) ? "null"
                    : ParserHelper.float2string(session.getSettlement(), baseCode,
                    ParserHelper.PURE_DECIMAL))
                    + ", \"previous\": "
                    + ((_combinedSession.getSettlement() == ParserHelper.DDFAPI_NOVALUE) ? "null"
                    : ParserHelper.float2string(_combinedSession.getSettlement(), baseCode,
                    ParserHelper.PURE_DECIMAL))
                    + ", \"volume\": "
                    + ((session.getVolume() == ParserHelper.DDFAPI_NOVALUE) ? "null" : session.getVolume())
                    + ", \"openinterest\": "
                    + ((session.getOpenInterest() == ParserHelper.DDFAPI_NOVALUE) ? "null"
                    : session.getOpenInterest())
                    + ", \"numtrades\": " + session.getNumberOfTrades() + ", \"pricevolume\": " + ParserHelper.float2string(session.getPriceVolume(), 'A', ParserHelper.PURE_DECIMAL, false)
                    + ", \"timestamp\": " + session.getTimeInMillis()
                    + (version == 1 && _seqNo > 0 ? ", \"seqno\": " + _seqNo : "")
                    + (version >= 1 && _marketId > 0 ? ", \"marketId\": " + _marketId : "")
            );
        }

        if ((session_t != null) && (session_t.getLast() != ParserHelper.DDFAPI_NOVALUE)) {
            boolean display = true;
            if (session_t.getTradeTimestamp() != 0) {
                ZonedDateTime tradeTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(session_t.getTradeTimestamp()), ZONE_ID_CHICAGO);
                ZonedDateTime now = ZonedDateTime.now(clock);
                ZonedDateTime currentSessionDate = session.getDay() != null ? session.getDay().getDate() : null;
                if (currentSessionDate != null && currentSessionDate.getDayOfYear() != tradeTime.getDayOfYear()) {
                    // T session is not for the current session
                    if (previousSession.getDay() != null && previousSession.getDay().getDate().getDayOfYear() == tradeTime.getDayOfYear()) {
                        // T session is from previous session
                        if (now.getHour() > 15) {
                            // after trading day don't show
                            display = false;
                        } else if (tradeTime.getHour() < 12) {
                            // during trading, only show if after 12 pm
                            display = false;
                        }
                    }
                } else if (now.getHour() >= 15) {
                    // For the current session, only show if trade after 12 pm
                    display = tradeTime.getHour() >= 12 ? true : false;
                }
            }
            if (display) {
                sb.append(", \"t_session\" : { ");
                sb.append("\"last\": " + ParserHelper.float2string(session_t.getLast(), baseCode, ParserHelper.PURE_DECIMAL));
                sb.append(", \"lastsize\": " + ((session_t.getLastSize() == ParserHelper.DDFAPI_NOVALUE) ? "null" : (usEquity ? session_t.getLastSize() * 100 : session_t.getLastSize())));
                sb.append(", \"tradetimestamp\": " + (session_t.getTradeTimestamp() == 0 ? null : session_t.getTradeTimestamp()));
                sb.append(", \"timestamp\": " + (session_t.getTimeInMillis() == 0 ? null : session_t.getTimeInMillis()));
                if (session_t.getNumberOfTrades() != 0) {
                    sb.append(", \"numtrades\": " + session_t.getNumberOfTrades());
                }
                if (session_t.getVolume() != ParserHelper.DDFAPI_NOVALUE) {
                    sb.append(", \"volume\": " + session_t.getVolume());
                }
                if (session_t.getPriceVolume() != ParserHelper.DDFAPI_NOVALUE) {
                    sb.append(", \"pricevolume\": " + ParserHelper.float2string(session_t.getPriceVolume(), 'A', ParserHelper.PURE_DECIMAL, false));
                }

                sb.append("}");
            }
        }

        sb.append(", " + "\"previous_session\": { ");
        sb.append("\"last\": " + ((previousSession.getLast() == ParserHelper.DDFAPI_NOVALUE) ? "null" : ParserHelper.float2string(previousSession.getLast(), baseCode, ParserHelper.PURE_DECIMAL)));
        sb.append(((previousSession.getOpen() == ParserHelper.DDFAPI_NOVALUE) ? "" : ",\"open\": " + ParserHelper.float2string(previousSession.getOpen(), baseCode, ParserHelper.PURE_DECIMAL)));
        sb.append(((previousSession.getHigh() == ParserHelper.DDFAPI_NOVALUE) ? "" : ",\"high\": " + ParserHelper.float2string(previousSession.getHigh(), baseCode, ParserHelper.PURE_DECIMAL)));
        sb.append(((previousSession.getLow() == ParserHelper.DDFAPI_NOVALUE) ? "" : ",\"low\": " + ParserHelper.float2string(previousSession.getLow(), baseCode, ParserHelper.PURE_DECIMAL)));
        sb.append(((previousSession.getPrevious() == ParserHelper.DDFAPI_NOVALUE) ? "" : ",\"previous\": " + ParserHelper.float2string(previousSession.getPrevious(), baseCode, ParserHelper.PURE_DECIMAL)));
        sb.append((previousSession.getVolume() == ParserHelper.DDFAPI_NOVALUE) ? "" : ",\"volume\": " + previousSession.getVolume());
        sb.append((previousSession.getOpenInterest() == ParserHelper.DDFAPI_NOVALUE) ? "" : ",\"openinterest\": " + previousSession.getOpenInterest());
        sb.append((previousSession.getSettlement() == ParserHelper.DDFAPI_NOVALUE) ? "" : ",\"settlement\": " + ParserHelper.float2string(previousSession.getSettlement(), baseCode, ParserHelper.PURE_DECIMAL));
        sb.append(",\"day\": " + ((previousSession.getDayCode() == '\0') ? "null" : "\"" + previousSession.getDayCode() + "\""));
        if (previousSession.getDay() != null) {
            String dt = LocalDate.from(previousSession.getDay().getDate()).format(DateTimeFormatter.ISO_DATE);
            sb.append(", \"date\": \"" + dt + "\"");
        } else {
            sb.append(", \"date\": \"null\"");
        }

        sb.append(" }");

        sb.append("}");
        return sb.toString();
    }

    private float calcVolOi(Session session) {
        return (float) session.getVolume() / (float) session.getOpenInterest();
    }

    // Use Z session if it is for today and newer than primary session.
    private boolean isZSessionNewerThanCurrentSession() {
        if (_zSession == null || _zSession.getDay() == null)
            return false;

        ZonedDateTime zSessionDate = _zSession.getDay().getDate();

        // If not for today, return false.
        if (!zSessionDate.toLocalDate().equals(LocalDate.now(zSessionDate.getZone())))
            return false;

        // Is zSession date after primary session?
        return _combinedSession.getDay() == null || zSessionDate.toLocalDate().isAfter(_combinedSession.getDay().getDate().toLocalDate());
    }

    // Use Z session if it is for today and newer than primary session.
    private boolean isZSessionForCurrentSession() {
        if (_zSession == null || _zSession.getDay() == null || _combinedSession == null || _combinedSession.getDay() == null)
            return false;

        // Is zSession date after primary session?
        return _zSession.getDay().getDate().toLocalDate().equals(_combinedSession.getDay().getDate().toLocalDate());
    }

    public XMLNode toXMLNode() {
        return toXMLNode(true);
    }


    /**
     * Converts the Quote object into an XMLNode, which can then be used to
     * bring the Quote into a textual form.
     *
     * @param showBidAsk Will add bid and ask in the Object
     * @return <code>XMLNode</code> The XMLNode representing this Quote.
     */

    public XMLNode toXMLNode(boolean showBidAsk) {
        boolean usEquity = false;
        String exchange = this._symbolInfo.getExchange();
        boolean opra = this._symbolInfo.getExchange().equals("OPRA") ? true : false;
        switch (exchange) {
            case "TSX":
            case "TSXV":
            case "TSX-V":
                usEquity = true;
                break;
        }

        XMLNode node = new XMLNode("QUOTE");
        node.setAttribute("symbol", _symbolInfo.getSymbol());
        if (_symbolInfo.getLongSymbol() != null) {
            node.setAttribute("longsymbol", _symbolInfo.getLongSymbol());
        }
        node.setAttribute("name", _symbolInfo.getName());
        node.setAttribute("exchange", _symbolInfo.getExchange());
        node.setAttribute("basecode", "" + _symbolInfo.getBaseCode());
        node.setAttribute("pointvalue", "" + _symbolInfo.getPointValue());
        node.setAttribute("tickincrement", "" + _symbolInfo.getTickIncrement());

        boolean useZSessionAsCurrentSession = isZSessionNewerThanCurrentSession();
        boolean useZSessionForCurrentSession = isZSessionForCurrentSession();

        if ((_ddfExchange != null) && (_ddfExchange.length() > 0))
            node.setAttribute("ddfexchange", _ddfExchange);

        if (!useZSessionAsCurrentSession) {
            if (_flag != '\0')
                node.setAttribute("flag", "" + _flag);
        } else {
            node.setAttribute("flag", "p");
        }

        if (_marketCondition != MarketConditionType.NORMAL)
            node.setAttribute("marketcondition", "" + _marketCondition.getCode());

        if (_lastUpdated > 0)
            node.setAttribute("lastupdate", (new DDFDate(_lastUpdated)).toDDFString());

        if (showBidAsk) {
            if (_bid != ParserHelper.DDFAPI_NOVALUE)
                node.setAttribute("bid", Integer.toString(ParserHelper.float2int(_symbolInfo.getUnitCode(), _bid)));
            if (_bidSize != ParserHelper.DDFAPI_NOVALUE)
                node.setAttribute("bidsize", "" + (usEquity ? _bidSize * 100 : _bidSize));
            if (_ask != ParserHelper.DDFAPI_NOVALUE)
                node.setAttribute("ask", Integer.toString(ParserHelper.float2int(_symbolInfo.getUnitCode(), _ask)));
            if (_askSize != ParserHelper.DDFAPI_NOVALUE)
                node.setAttribute("asksize", "" + (usEquity ? _askSize * 100 : _askSize));
            if (opra && _ask != ParserHelper.DDFAPI_NOVALUE) {
                float midpoint = calcMidPoint();
                node.setAttribute("midpoint", Integer.toString(ParserHelper.float2int(_symbolInfo.getUnitCode(),midpoint)));
            }
        }

        if (useZSessionAsCurrentSession) {
            XMLNode n1 = _zSession.toZSessionXMLNode();
            n1.setAttribute("id", "combined");
            node.addNode(n1);

            XMLNode n2 = _combinedSession.toXMLNode();
            n2.setAttribute("id", "previous");
            node.addNode(n2);
        } else {
            XMLNode n1 = _combinedSession.toXMLNode(useZSessionForCurrentSession ? _zSession : null);
            n1.setAttribute("id", "combined");
            node.addNode(n1);

            XMLNode n2 = _previousSession.toXMLNode();
            n2.setAttribute("id", "previous");
            node.addNode(n2);
        }

        for (Session session : _sessions) {
            XMLNode n = session.toXMLNode();
            n.setAttribute("id", "session_" + n.getAttribute("day") + "_" + n.getAttribute("session"));
            node.addNode(n);
        }

        return node;
    }

    private float calcMidPoint() {
        float midpoint = (_ask + _bid) / 2 ;
        return midpoint;
    }

    /**
     * Takes in an XMLNode object, and parses this into a Quote object.
     *
     * @param node XML Quote Object
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

        s = node.getAttribute("lastupdate");
        if ((s != null) && (s.length() > 0))
            qte._lastUpdated = DDFDate.fromDDFString(s).getMillisCST();


        for (XMLNode n : node.getAllNodes("SESSION")) {
            Session session = new Session(qte);
            session.fromXMLNode(n);

            if (n.getAttribute("id").equals("combined"))
                qte._combinedSession = session;
            else if (n.getAttribute("id").equals("previous"))
                qte._previousSession = session;
            else
                qte._sessions.add(session);
        }
        return qte;
    }

    public boolean isTick() {
        return _message.getQuoteType().isTick();
    }

    public boolean isRefresh() {
        return _message.getQuoteType().isRefresh();
    }

    public long getSeqNo() {
        return _seqNo;
    }

    public void setSeqNo(long _seqNo) {
        this._seqNo = _seqNo;
    }

    public String getRequestSymbol() {
        return _requestSymbol;
    }

    public void setRequestSymbol(String symbol) {
        this._requestSymbol = symbol;
    }

    public long getMarketId() {
        return _marketId;
    }

    public void setMarketId(long _marketId) {
        this._marketId = _marketId;
    }

    public long getCacheTimeMs() {
        return _cacheTimeMs;
    }

    public void setCacheTimeMs(long _cacheTimeMs) {
        this._cacheTimeMs = _cacheTimeMs;
    }

    public void setCacheAge(CacheAge age) {
        this._cacheAge = age;
    }
    public CacheAge getCacheAge() { return this._cacheAge; }
}
