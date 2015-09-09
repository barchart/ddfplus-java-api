/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */

package com.ddfplus.messages;

import com.ddfplus.codec.Codec;
import com.ddfplus.enums.DdfMessageType;
import com.ddfplus.enums.QuoteType;

import static com.ddfplus.util.ParserHelper.filterNullChar;

/**
 * ddfplus record 2, subrecord 0
 * 
 * These are live, foreground messages. While these are becoming less and less
 * frequent, many exchanges still send down individual items - such as bid or
 * last. (Note that electronic systems send down bid, bid size, ask, ask size,
 * trade, trade size, etc. - all as one message. The would then be encapsulated
 * in a Message27, Message28, Message2A, etc.).
 * <P>
 * Message20 have one value - encoded as either a floating point or an integer.
 * You have to know what you are going for. For example, an element 0, modifier
 * 0 is a trade. Therefore it is a floating point value. An element 0, modifier
 * &lt; is a bid size, however, and hence is an integer value;
 */

public class Data20Parameter extends AbstractMsgBaseMarket implements DdfMarketParameter {

	/** The _element. */
	public volatile char _element = '\0';

	/** The _modifier. */
	public volatile char _modifier = '\0';

	/** The _value. */
	public volatile Number _value = null;

	/**
	 * Instantiates a new data20 parameter.
	 * 
	 * @param message
	 *            the message
	 */
	Data20Parameter(byte[] message) {
		super(message);
	}

	/**
	 * Gets the element.
	 * 
	 * @return The Element code
	 */

	public char getElement() {
		return _element;
	}

	/**
	 * Gets the modifier.
	 * 
	 * @return The Element Modifier code
	 */

	public char getModifier() {
		return _modifier;
	}

	/**
	 * Gets the value.
	 * 
	 * @return The value as an Object
	 */

	public Number getValue() {
		return _value;
	}

	/**
	 * Gets the value as float.
	 * 
	 * @return The value as a floating point
	 */

	public float getValueAsFloat() {
		return (_value == null) ? 0.0f : _value.floatValue();
	}

	/**
	 * Gets the value as integer.
	 * 
	 * @return The value as an integer
	 */

	public int getValueAsInteger() {
		return (_value == null) ? 0 : _value.intValue();
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

		int pos2 = Codec.getIndexOf(ba, ',', pos + 7);

		this._element = (char) ba[pos2 + 1];
		this._modifier = (char) ba[pos2 + 2];

		boolean isFloat = false;
		switch (this._element) {
		case '0':
			isFloat = true;
			break;
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case 'A':
		case 'D':
		case 'd':
		case 'E':
		case 'V':
			isFloat = true;
			break;
		default:
			isFloat = false;
			break;
		}

		if (isFloat)
			this._value = Codec.parseDDFPriceValue(ba, pos + 7, pos2 - pos - 7, this._basecode);
		else
			this._value = Codec.parseDDFIntValue(ba, pos + 7, pos2 - pos - 7);

		this._day = (char) ba[pos2 + 3];
		this._session = (char) ba[pos2 + 4];
		this.setMessageTimestamp(pos2 + 5);
	}

	/**
	 * Does the actual parsing of a Message20 object.
	 * 
	 * @param ba
	 *            the ba
	 * @return the data20 parameter
	 */

	public static Data20Parameter Parse(byte[] ba) {
		Data20Parameter msg = new Data20Parameter(ba);
		msg.parse(ba);
		return msg;
	}

	@Override
	protected void appendConcrete(StringBuilder text) {
		text.append(" element=");
		text.append(filterNullChar(_element));
		text.append(" modifier=");
		text.append(filterNullChar(_modifier));
		text.append(" value=");
		text.append(_value);
	}

	@Override
	public QuoteType getQuoteType() {
		switch (_element) {
		case '0':
			switch (_modifier) {
			case '0':
			case '1':
			case '2':
				return QuoteType.TICK;
			default:
				break;
			}
			break;
		case 'D':
			switch (_modifier) {
			case '0':
				return QuoteType.TICK;
			default:
				break;
			}
			break;
		}
		return QuoteType.UNKNOWN;
	}

	@Override
	public DdfMessageType getMessageType() {
		return DdfMessageType.Market_DDF_20;
	}

}
