/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */

package com.ddfplus.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * The DDFDate class encapsulates a date and provides some utility functions,
 * such as conversions to/from ddfplus day codes.
 */

public class DDFDate {

	// default; futures
	public static final DateTimeZone TIME_ZONE_CHICAGO = DateTimeZone.forID("America/Chicago");

	// equities, indexes
	public static final DateTimeZone TIME_ZONE_NEWYORK = DateTimeZone.forID("America/New_York");

	// XXX TIME!ZONE no time zone info
	private static final DateTimeFormatter _formatter = DateTimeFormat.forPattern("YYYYMMddHHmmss").withZone(
			TIME_ZONE_CHICAGO);

	private final DateTime _dateTime;

	public DDFDate(DateTime dt) {
		_dateTime = dt;
	}

	/**
	 * @param millis
	 *            The time in milliseconds as a <code>long</code>.
	 */

	// XXX TIME!ZONE : assume millis are UTC
	public DDFDate(long millis) {
		_dateTime = new DateTime(millis, TIME_ZONE_CHICAGO);
	}

	/**
	 * Returns the ddfplus day code for this object.
	 * 
	 * @return The ddfplus day code.
	 */

	public char getDayCode() {
		int day = _dateTime.getDayOfMonth();
		return DDFDate.convertNumberToDayCode(day);
	}

	/**
	 * Returns the milliseconds associated with this date.
	 * 
	 * @return The date value in milliseconds.
	 */
	// XXX TIME!ZONE
	public long getMillisCST() {
		return millisCST(_dateTime);
	}

	/**
	 * Returns a String in the format YYYYMMDDHHNNSS
	 */

	public String toDDFString() {
		return _formatter.print(_dateTime);
	}

	@Override
	public String toString() {
		return ((new Date(_dateTime.getMillis())).toString());
	}

	/**
	 * Converts a ddfplus <code>char</code> day code into a numeric value.
	 * ddfplus day codes are characters '1' .. '9', '0', 'A' .. 'U' representing
	 * the numbers 1 .. 31, in order.
	 * 
	 * @param daycode
	 *            A <code>char</code> value '1' .. '9', '0', 'A' .. 'U'
	 * @return The <code>int</code> corresponding value.
	 */

	public static int convertDayCodeToNumber(char daycode) {
		if (daycode == '\0')
			return 0;
		
		if ((daycode >= '1') && (daycode <= '9'))
			return (daycode - '0');
		else if (daycode == '0')
			return 10;
		else
			return ((daycode - 'A') + 11);
	}

	/**
	 * Converts a numeric day value to a ddfplus <code>char</code> day code.
	 * 
	 * @param value
	 *            A calendar value, e.g. 1 .. 31
	 * @return <code>char</code> The day code, e.g. 1 .. 9, 0, A .. U
	 */

	public static char convertNumberToDayCode(int value) {
		if ((value >= 1) && (value <= 9))
			return (char) ('1' + value - 1);
		else if (value == 10)
			return '0';
		else
			return (char) ('A' + value - 11);
	}

	/**
	 * Creates a new <code>DDFDate</code> object with the current time as the
	 * reference point.
	 * 
	 * @see #fromDayCode(char, long)
	 * 
	 * @param daycode
	 *            The <code>char</code> ddfplus day code.
	 * @return The <code>DDFDate</code> object
	 */

	// XXX TIME!ZONE millis UTC -> CST
	public static DDFDate fromDayCode(final char daycode) {

		long millis = millisCST(new DateTime(TIME_ZONE_CHICAGO));

		return fromDayCode(daycode, millis);

	}

	/**
	 * Creates a new <code>DDFDate</code> object with the given reference point.
	 * The method rolls the underlying calendar value forward, so that if the
	 * reference point is the 31st of March, and the daycode passed in is a '1',
	 * then the result will be April 1st.
	 * 
	 * @param daycode
	 *            The <code>char</code> ddfplus day code
	 * @param reference
	 *            The reference date passed in milliseconds as a
	 *            <code>long</code>.
	 */

	public static DDFDate fromDayCode(char daycode, long reference) {

		MutableDateTime dt = new MutableDateTime(reference);

		int day = convertDayCodeToNumber(daycode);
		int cday = dt.getDayOfMonth();

		dt.setDayOfMonth(day);

		if (day > cday)
			dt.addMonths(1);

		return new DDFDate(dt.getMillis());

	}

	/**
	 * Parses a date in the format YYYYMMDDHHNNSS
	 * 
	 * @return <code>DDFDate</code> The DDFDate object
	 */

	public static DDFDate fromDDFString(String s) {
		try {
			long millis = _formatter.parseMillis(s);
			return new DDFDate(millis);
		} catch (Exception e) {
			;
		}
		return null;
	}

	/**
	 * hack to force seemingly UTC time stamps look like Chicago time
	 **/
	public static final long millisCST(final DateTime dateTime) {
		final long millisUTC = dateTime.getMillis();
		final long offsetCST = TIME_ZONE_CHICAGO.getOffset(dateTime);
		final long offsetLOC = DateTimeZone.getDefault().getOffset(dateTime);
		return millisUTC + offsetCST - offsetLOC;
	}

	
	
	public static final LocalDate getLocalDateFromDayCode(char dayCode) {
		// Get today, in Chicago
		LocalDate chicago = LocalDate.now(ZoneId.of("America/Chicago"));
		int year = chicago.getYear();
		int month = chicago.getMonth().getValue(); // 1 - 12
		int day = chicago.getDayOfMonth();

		
		// Determine Day from DayCode
		int daynum = convertDayCodeToNumber(dayCode);
		
		// If the day code is greater than today pkus a few days, it must have been for last month ;)
		if (daynum > (day + 5)) {
			month--;
			if (month == 0) {
				year--;
				month = 12;
			}
		}
		
		if (daynum == 0)
			daynum = 1;
		
		LocalDate date;
		
		try {
			date = LocalDate.of(year, month, daynum);
		}
		catch (Exception e) {
			date = chicago;
		}

		return date;
	}
}
