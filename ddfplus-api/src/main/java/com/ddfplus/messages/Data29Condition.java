/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */

package com.ddfplus.messages;

import com.ddfplus.codec.Codec;
import com.ddfplus.enums.MarketConditionType;
import com.ddfplus.enums.QuoteType;

/**
 * <b>Message29</b> encapsulates ddfplus messages with record 2, subrecord 9.
 * These are market condition messages, which only have one data point: The
 * market condition
 */

public class Data29Condition extends AbstractMsgBaseMarket implements DdfMarketCondition {

	public volatile MarketConditionType _marketCondition = null;

	/**
	 * Instantiates a new data29 condition.
	 * 
	 * @param message
	 *            the message
	 */
	Data29Condition(byte[] message) {
		super(message);
	}

	/**
	 * Gets the market condition.
	 * 
	 * @return The Market Condition.
	 */

	public MarketConditionType getMarketCondition() {
		return _marketCondition;
	}

	/**
	 * Parses the.
	 * 
	 * @param ba
	 *            the ba
	 */
	protected void parse(final byte[] ba) {
		int pos = Codec.getIndexOf(ba, ',', 0);

		this._symbol = Codec.parseStringValue(ba, 2, pos - 2);
		this.setBaseCode((char) ba[pos + 3]);
		this._exchange = (char) ba[pos + 4];
		this._delay = Codec.parseIntValue(ba, pos + 5, 2);
		this._record = (char) ba[1];
		this._subrecord = (char) ba[pos + 1];

		this._marketCondition = MarketConditionType.getByCode((char) ba[pos + 7]);
		this._day = (char) ba[pos + 11];
		this._session = (char) ba[pos + 12];

		this.setMessageTimestamp(pos + 13);
	}

	/**
	 * Parses the.
	 * 
	 * @param ba
	 *            the ba
	 * @return the data29 condition
	 */
	public static Data29Condition Parse(byte[] ba) {
		Data29Condition msg = new Data29Condition(ba);
		msg.parse(ba);
		return msg;
	}

	public QuoteType getQuoteType() {
		return QuoteType.UNKNOWN;
	}

}
