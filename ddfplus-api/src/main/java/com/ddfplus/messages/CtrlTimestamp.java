/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */

package com.ddfplus.messages;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ddfplus.codec.Codec;
import com.ddfplus.enums.DdfMessageType;
import com.ddfplus.util.DDFDate;

/**
 * ddfplus record # timestamps
 * 
 * This message has only one value, the time in milliseconds.
 */

public class CtrlTimestamp extends AbstractMsgBaseMarket implements DdfTimestamp {

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(CtrlTimestamp.class);

	/** The _date time. */
	protected volatile DateTime _dateTime = null;

	/** The _year. */
	protected volatile int _year = 0;

	/** The _month. */
	protected volatile int _month = 0;

	/** The _date. */
	protected volatile int _date = 0;

	/** The _hour. */
	protected volatile int _hour = 0;

	/** The _minute. */
	protected volatile int _minute = 0;

	/** The _second. */
	protected volatile int _second = 0;

	/**
	 * Instantiates a new ctrl timestamp.
	 * 
	 * @param message
	 *            the message
	 */
	CtrlTimestamp(byte[] message) {
		super(message);
	}

	public DateTime getDateTime() {
		return _dateTime;
	}

	/**
	 * Returns the time data in parts, as an <code>int[]</code>.<BR>
	 * 
	 * <p>
	 * int[0] = year<BR>
	 * int[1] = month<BR>
	 * int[2] = day<BR>
	 * int[3] = hour<BR>
	 * int[4] = minute<BR>
	 * int[5] = second<BR>
	 * </p>
	 * 
	 * @return array of time data
	 */

	public int[] getTimeInParts() {
		return new int[] { _year, _month, _date, _hour, _minute, _second };
	}

	/**
	 * Parses a byte[] in the format of a ddf timestamp to a MessageTimestamp
	 * object.
	 * 
	 * @param array
	 *            the array
	 * @return the ctrl timestamp
	 */

	public static CtrlTimestamp Parse(final byte[] array) {

		CtrlTimestamp message = new CtrlTimestamp(array);

		// legacy values
		message._record = '#';
		message._subrecord = '\0';

		try {

			message._year = Codec.parseIntValue(array, 2, 4);
			message._month = Codec.parseIntValue(array, 6, 2) - 1;
			message._date = Codec.parseIntValue(array, 8, 2);
			message._hour = Codec.parseIntValue(array, 10, 2);
			message._minute = Codec.parseIntValue(array, 12, 2);
			message._second = Codec.parseIntValue(array, 14, 2);

			// XXX TIME!ZONE
			// DDF control time stamp message has hard coded time zone of CST
			final DateTime dateTime = new DateTime(message._year, message._month + 1, message._date, message._hour,
					message._minute, message._second, 0, DDFDate.TIME_ZONE_CHICAGO);

			message._dateTime = dateTime;

			// XXX
			message.millisCST = DDFDate.millisCST(dateTime);

			return message;

		} catch (Exception e) {
			LOG.error("Failed to parse time: " + new String(array) + ". ", e);
		}

		return null;
	}

	@Override
	public String toString() {
		return " TimeStamp : " + _dateTime;
	}

	@Override
	public DdfMessageType getMessageType() {
		return DdfMessageType.Timestamp;
	}

}
