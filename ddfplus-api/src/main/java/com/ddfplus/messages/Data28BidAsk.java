/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */

package com.ddfplus.messages;

import com.ddfplus.codec.Codec;
import com.ddfplus.enums.QuoteType;

/**
 * ddfplus record 2, subrecord 8
 * 
 * These are top of book (bid, bid size, ask, ask size) messages.
 */

public class Data28BidAsk extends AbstractMsgBaseMarket implements DdfMarketBidAsk {

	/** The _ask. */
	public volatile Float _ask = null;

	/** The _ask size. */
	public volatile Integer _askSize = null;

	/** The _bid. */
	public volatile Float _bid = null;

	/** The _bid size. */
	public volatile Integer _bidSize = null;

	/**
	 * Instantiates a new data28 bid ask.
	 * 
	 * @param message
	 *            the message
	 */
	public Data28BidAsk(byte[] message) {
		super(message);
	}

	/**
	 * The Ask (Offer) price.
	 * 
	 * @return The Ask price (Offer price)
	 */

	public Float getAskPrice() {
		return _ask;
	}

	/**
	 * The Size of the Ask.
	 * 
	 * @return The Size of the Ask.
	 */

	public Integer getAskSize() {
		return _askSize;
	}

	/**
	 * The Bid price.
	 * 
	 * @return The Bid price.
	 */

	public Float getBidPrice() {
		return _bid;
	}

	/**
	 * The Size of the Bid.
	 * 
	 * @return The Size of the Bid.
	 */

	public Integer getBidSize() {
		return _bidSize;
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
		this._exchange = (char) ba[pos + 4];
		this._delay = Codec.parseIntValue(ba, pos + 5, 2);
		this._record = (char) ba[1];
		this._subrecord = (char) ba[pos + 1];

		int pos2 = Codec.getIndexOf(ba, ',', pos + 7);
		if (pos2 > pos + 7)
			this._bid = Codec.parseDDFPriceValue(ba, pos + 7, pos2 - pos - 7, this._basecode);

		pos = Codec.getIndexOf(ba, ',', pos2 + 1);
		if (this._bid != null)
			this._bidSize = Codec.parseDDFIntValue(ba, pos2 + 1, pos - pos2 - 1);

		pos2 = Codec.getIndexOf(ba, ',', pos + 1);
		if (pos2 > pos + 1)
			this._ask = Codec.parseDDFPriceValue(ba, pos + 1, pos2 - pos - 1, this._basecode);

		pos = Codec.getIndexOf(ba, ',', pos2 + 1);
		if (this._ask != null)
			this._askSize = Codec.parseDDFIntValue(ba, pos2 + 1, pos - pos2 - 1);

		pos = Codec.getIndexOf(ba, '\u0003', pos);
		this._day = (char) ba[pos - 2];
		this._session = (char) ba[pos - 1];

		this.setMessageTimestamp(pos);
	}

	/**
	 * Parses the.
	 * 
	 * @param ba
	 *            the ba
	 * @return the data28 bid ask
	 */
	public static Data28BidAsk Parse(byte[] ba) {
		Data28BidAsk msg = new Data28BidAsk(ba);
		msg.parse(ba);
		return msg;
	}

	public QuoteType getQuoteType() {
		return QuoteType.UNKNOWN;
	}

}
