/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.messages;

import com.ddfplus.enums.QuoteType;

import static com.ddfplus.codec.Codec.getIndexOf;
import static com.ddfplus.codec.Codec.parseDDFIntValue;
import static com.ddfplus.codec.Codec.parseDDFPriceValue;
import static com.ddfplus.codec.Codec.parseIntValue;
import static com.ddfplus.codec.Codec.parseStringValue;

/**
 * ddfplus record 2, subrecord 7
 * 
 * These are live trade messages, which have two data points: trade and trade
 * size.
 */
public class Data27Trade extends AbstractMsgBaseMarket implements DdfMarketTrade {

	/** The _trade. */
	public volatile Float _tradePrice = null;

	/** The _trade size. */
	public volatile Integer _tradeSize = null;

	/**
	 * Instantiates a new data27 trade.
	 * 
	 * @param message
	 *            the message
	 */
	public Data27Trade(byte[] message) {
		super(message);
	}

	/**
	 * Gets the trade price. @Override public void accept(final
	 * MarketMessageVisitor visitor) { visitor.visit(this); }
	 * 
	 * 
	 * @return The Trade price.
	 */

	public float getTradePrice() {
		return (_tradePrice == null) ? 0.0f : _tradePrice;
	}

	/**
	 * Gets the trade size.
	 * 
	 * @return The size of the trade.
	 */

	public int getTradeSize() {
		return (_tradeSize == null) ? 0 : _tradeSize;
	}

	/**
	 * Parses the.
	 * 
	 * @param ba
	 *            the ba
	 */
	protected void parse(final byte[] ba) {

		int pos = getIndexOf(ba, ',', 0);

		this._symbol = parseStringValue(ba, 2, pos - 2);
		this.setBaseCode((char) ba[pos + 3]);
		this._exchange = (char) ba[pos + 4];
		this._delay = parseIntValue(ba, pos + 5, 2);
		this._record = (char) ba[1];
		this._subrecord = (char) ba[pos + 1];

		int pos2 = getIndexOf(ba, ',', pos + 7);
		this._tradePrice = parseDDFPriceValue(ba, pos + 7, pos2 - pos - 7, this._basecode);

		pos = getIndexOf(ba, ',', pos2 + 1);
		this._tradeSize = parseDDFIntValue(ba, pos2 + 1, pos - pos2 - 1);

		pos = getIndexOf(ba, '\u0003', pos);
		this._day = (char) ba[pos - 2];
		this._session = (char) ba[pos - 1];

		this.setMessageTimestamp(pos);

	}

	/**
	 * Parses the.
	 * 
	 * @param ba
	 *            the ba
	 * @return the data27 trade
	 */
	public static Data27Trade Parse(byte[] ba) {
		Data27Trade msg = new Data27Trade(ba);
		msg.parse(ba);
		return msg;
	}

	public QuoteType getQuoteType() {
		return QuoteType.TICK;
	}

}
