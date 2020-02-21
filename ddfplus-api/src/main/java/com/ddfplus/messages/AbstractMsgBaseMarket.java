/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.messages;

import static com.ddfplus.util.ParserHelper.filterNullChar;
import static com.ddfplus.util.ParserHelper.toAsciiString;
import static com.ddfplus.util.ParserHelper.toAsciiString2;
import static com.ddfplus.util.ParserHelper.toHexString;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ddfplus.enums.QuoteType;
import com.ddfplus.util.ASCII;
import com.ddfplus.util.DDFDate;

/**
 * The Message base class encapsulates a ddf plus message. It is generally
 * subclassed by one of the many Message???.class files in this package. See
 * those files for more details about message specific information.
 * <P>
 * <B>As with all of the API documentation, it is <I>very</I> helpful to
 * cross-reference the ddfplus data feed documentation.</B>
 */

public abstract class AbstractMsgBaseMarket extends AbstractMsgBase implements DdfMarketBase {

	/*
	 * Used for the timestamp encoding to make it more printable.
	 */
	public static final int ASCII_SHIFT = 0x40;

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(AbstractMsgBaseMarket.class);

	/** The _basecode. */
	public volatile char _basecode = ASCII.NUL;

	/** The _day. */
	public volatile char _day = ASCII.NUL;

	/** The _delay. */
	public volatile int _delay = 0;

	/** The _etxpos. */
	public volatile int _etxpos = -1; // The ETX Position

	/** The _exchange. */
	public volatile char _exchange = ASCII.NUL;

	/** The _record. */
	public volatile char _record = ASCII.NUL;

	/** The _session. */
	public volatile char _session = ASCII.NUL;

	/** The _spread legs. */
	public volatile String[] _spreadLegs = null;

	/** The _spread type. */
	public volatile String _spreadType = null;

	/** The _subrecord. */
	public volatile char _subrecord = ASCII.NUL;

	/** The _symbol. */
	public volatile String _symbol = ASCII.STRING_EMPTY;

	/** The _timestamp. */
	// public volatile DateTime dateTime = null;

	/**
	 * Instantiates a new msg base market.
	 * 
	 * @param message
	 *            the message
	 */
	protected AbstractMsgBaseMarket(byte[] message) {
		super(message);
	}

	public char getBaseCode() {
		return _basecode;
	}

	public char getDay() {
		return _day;
	}

	public int getDelay() {
		return _delay;
	}

	public int getPositionETX() {
		return _etxpos;
	}

	public char getExchange() {
		return _exchange;
	}

	public char getRecord() {
		return _record;
	}

	public char getSession() {
		return _session;
	}

	public char getSubRecord() {
		return _subrecord;
	}

	public String getSymbol() {
		return _symbol;
	}

	/**
	 * Sets the basecode for the message.
	 * 
	 * @param c
	 *            the new base code
	 */

	protected void setBaseCode(char c) {
		_basecode = c;
	}

	/**
	 * Sets the message timestamp.
	 * 
	 * @param etxpos
	 *            the new message timestamp
	 */
	public void setMessageTimestamp(int etxpos) {

		if ((_message == null) || (_message.length < etxpos + 1))
			return;

		_etxpos = etxpos;

		/*
		 * DDF Time can be 7 or 9 bytes.
		 * 
		 * Some exchanges are not sending the ms, so the DDF timestamp is only 7
		 * bytes
		 */
		if ((_message.length == etxpos + 10 || _message.length == etxpos + 8) && (_message[etxpos + 1] == 20)) {
			int year = (_message[etxpos + 1] * 100) + _message[etxpos + 2] - ASCII_SHIFT;
			int month = _message[etxpos + 3] - ASCII_SHIFT - 1;
			int date = _message[etxpos + 4] - ASCII_SHIFT;
			int hour = _message[etxpos + 5] - ASCII_SHIFT;
			int minute = _message[etxpos + 6] - ASCII_SHIFT;
			int second = _message[etxpos + 7] - ASCII_SHIFT;

			int ms = 0;
			if (_message.length == etxpos + 10) {
				ms = (0xFF & _message[etxpos + 8]) + ((0xFF & _message[etxpos + 9]) << 8);
				// Validation check, some feeds can have incorrect data here.
				if (ms < 0 || ms > 999) {
					ms = 0;
				}
			}

			/*
			 * DDF suffix time stamp comes with hard coded time zone of CST
			 * 
			 * TODO Support switching on futures vs stocks
			 */
			this.localDateTime = LocalDateTime.of(year, month + 1, date, hour, minute, second, ms * 1_000_000);
			this.millisCST = ZonedDateTime
					.of(this.localDateTime, DDFDate._zoneChicago).toInstant()
					.toEpochMilli();
		}
	}

	@Override
	public String toString() {

		StringBuilder text = new StringBuilder(128);

		text.append(" record=");
		text.append(filterNullChar(_record));
		text.append(" subrec=");
		text.append(filterNullChar(_subrecord));
		text.append(" exchcode=");
		text.append(filterNullChar(_exchange));

		if (_symbol != ASCII.STRING_EMPTY) {
			text.append(" sym=");
			text.append(_symbol);
		}

		if (_spreadLegs != null) {
			text.append(" legs=");
			text.append(_spreadLegs);
		}

		text.append(" basecode=");
		text.append(filterNullChar(_basecode));

		text.append(" day=");
		text.append(filterNullChar(_day));

		appendConcrete(text);

		// Dump the full message
		text.append(" [" + toStringAscii() + "]");

		return text.toString();

	}

	//

	/**
	 * To string hex.
	 * 
	 * @return the string
	 */
	public String toStringHex() {
		return toHexString(_message);
	}

	/**
	 * To string ascii.
	 * 
	 * @return the string
	 */
	public String toStringAscii() {
		return toAsciiString(_message);
	}

	/**
	 * To string ascii hex.
	 * 
	 * @return the string
	 */
	public String toStringAsciiHex() {
		return //
		"\n\t" + toAsciiString2(_message) + //
				"\n\t" + toHexString(_message) + //
				"";
	}

	@Override
	public QuoteType getQuoteType() {
		return QuoteType.UNKNOWN;
	}

}
