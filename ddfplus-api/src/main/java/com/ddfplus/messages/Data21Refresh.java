/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */

package com.ddfplus.messages;

import com.ddfplus.codec.Codec;
import com.ddfplus.db.SymbolInfo;
import com.ddfplus.enums.QuoteType;

/**
 * ddfplus record 2, subrecord 1, 2, 3, 4, or 6
 * 
 * These are refresh messages. This class applies to Record Type 2, Subrecords
 * 1, 2, 3, 4, and 6. Each of these messages is essntially the same, with the
 * difference being only in interpretation.
 * <P>
 * <ul>
 * <LI>Subrecord 1: Exchange genreated, live foreground refresh
 * <LI>Subrecord 2: ddfplus genreated, live foregrounfd refresh
 * <LI>Subrecord 3: ddfplus generated, background refresh
 * <LI>Subrecord 4: ddfplus generated, background refresh for previous session
 * <LI>Subrecord 6: Live foregroud quote message. Some items are quoted as ohlc
 * </ul>
 * refresh messages. So these are essentially quotes.
 */
public class Data21Refresh extends AbstractMsgBaseMarket implements DdfMarketRefresh {

	/** The _ask. */
	public volatile Float _ask = null;

	/** The _bid. */
	public volatile Float _bid = null;

	/** The _close. */
	public volatile Float _close = null;

	/** The _close2. */
	public volatile Float _close2 = null;

	/** The _high. */
	public volatile Float _high = null;

	/** The _last. */
	public volatile Float _last = null;

	/** The _low. */
	public volatile Float _low = null;

	/** The _open. */
	public volatile Float _open = null;

	/** The _open2. */
	public volatile Float _open2 = null;

	/** The _open interest. */
	public volatile Long _openInterest = null;

	/** The _previous. */
	public volatile Float _previous = null;

	/** The _previous volume. */
	public volatile Long _previousVolume = null;

	/** The _settle. */
	public volatile Float _settle = null;

	/** The _volume. */
	public volatile Long _volume = null;

	/**
	 * Instantiates a new data21 refresh.
	 * 
	 * @param message
	 *            the message
	 */
	Data21Refresh(byte[] message) {
		super(message);
	}

	/**
	 * The Ask (Offer, Sell) price.
	 * 
	 * @return the ask
	 */

	public Float getAsk() {
		return _ask;
	}

	/**
	 * The Bid (Buy) price.
	 * 
	 * @return the bid
	 */

	public Float getBid() {
		return _bid;
	}

	/**
	 * The closing price.
	 * 
	 * @return the close
	 */

	public Float getClose() {
		return _close;
	}

	/**
	 * The second of the closing range price.
	 * 
	 * @return the close2
	 */

	public Float getClose2() {
		return _close2;
	}

	/**
	 * The High price.
	 * 
	 * @return the high
	 */

	public Float getHigh() {
		return _high;
	}

	/**
	 * The Last price.
	 * 
	 * @return the last
	 */

	public Float getLast() {
		return _last;
	}

	/**
	 * The Low price.
	 * 
	 * @return the low
	 */

	public Float getLow() {
		return _low;
	}

	/**
	 * The Open price.
	 * 
	 * @return the open
	 */

	public Float getOpen() {
		return _open;
	}

	/**
	 * The second of an opening range price.
	 * 
	 * @return the open2
	 */

	public Float getOpen2() {
		return _open2;
	}

	/**
	 * The Open Interest.
	 * 
	 * @return the open interest
	 */

	public Long getOpenInterest() {
		return _openInterest;
	}

	/**
	 * The Previous Settle.
	 * 
	 * @return the previous
	 */

	public Float getPrevious() {
		return _previous;
	}

	/**
	 * The Volume for the Previous Session.
	 * 
	 * @return the previous volume
	 */

	public Long getPreviousVolume() {
		return _previousVolume;
	}

	/**
	 * The settlement price.
	 * 
	 * @return the settle
	 */

	public Float getSettle() {
		return _settle;
	}

	/**
	 * The Volume for the Current Session.
	 * 
	 * @return the volume
	 */

	public Long getVolume() {
		return _volume;
	}

	/**
	 * Parses the.
	 * 
	 * @param ba
	 *            the ba
	 */
	protected void parse(final byte[] ba) {

		int pos = Codec.getIndexOf(ba, ',', 0);

		this._symbol = new String(ba, 2, pos - 2);
		this.setBaseCode((char) ba[pos + 3]);

		int unitCode = SymbolInfo.ddfuc2bb((char) ba[pos + 3]);

		this._exchange = (char) ba[pos + 4];
		this._delay = Codec.parseIntValue(ba, pos + 5, 2);
		this._record = (char) ba[1];
		this._subrecord = (char) ba[pos + 1];

		int pos2 = pos + 8;

		int idx = -1;
		while (idx++ < 15) {
			if (idx == 14) {
				this._day = (char) ba[pos + 1];
				this._session = (char) ba[pos + 2];
				break;
			}

			pos = Codec.getIndexOf(ba, ',', pos2);

			Float fvalue = null;
			Long lvalue = null;

			if (pos > pos2) {
				if ((pos - pos2 == 1) && ((char) ba[pos2] == '-'))
					fvalue = 0.0f;
				else if (idx <= 10)
					fvalue = Codec.parseDDFPriceValue(ba, pos2, pos - pos2, unitCode);

				else
					lvalue = Codec.parseLongValue(ba, pos2, pos - pos2);
			}
			pos2 = pos + 1;

			switch (idx) {
			case 0:
				this._open = fvalue;
				break;
			case 1:
				this._high = fvalue;
				break;
			case 2:
				this._low = fvalue;
				break;
			case 3:
				this._last = fvalue;
				break;
			case 4:
				this._bid = fvalue;
				break;
			case 5:
				this._ask = fvalue;
				break;
			case 6:
				this._open2 = fvalue;
				break;
			case 7:
				this._previous = fvalue;
				break;
			case 8:
				this._close = fvalue;
				break;
			case 9:
				this._close2 = fvalue;
				break;
			case 10:
				this._settle = fvalue;
				break;
			case 11:
				this._previousVolume = lvalue;
				break;
			case 12:
				this._openInterest = lvalue;
				break;
			case 13:
				this._volume = lvalue;
				break;
			}
		}

		pos = Codec.getIndexOf(ba, '\u0003', pos2);
		if ((pos > 0) && ((ba.length - 10) == pos))
			this.setMessageTimestamp(pos);
	}

	/**
	 * Parses the.
	 * 
	 * @param ba
	 *            the ba
	 * @return the data21 refresh
	 */
	public static Data21Refresh Parse(byte[] ba) {
		Data21Refresh msg = new Data21Refresh(ba);
		msg.parse(ba);
		return msg;
	}

	public QuoteType getQuoteType() {
		switch (_subrecord) {
		case '1':
		case '2':
		case '3':
			return QuoteType.REFRESH;
		case '6':
			return QuoteType.TICK;
		default:
			break;
		}
		return QuoteType.UNKNOWN;
	}

}
