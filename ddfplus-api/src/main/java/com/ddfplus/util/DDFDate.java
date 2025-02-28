/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */

package com.ddfplus.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The DDFDate class encapsulates a date and provides some utility functions,
 * such as conversions to/from ddfplus day codes.
 */

public class DDFDate {
	public static final ZoneId _zoneChicago = ZoneId.of("America/Chicago");
	private static final DateTimeFormatter _formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
			.withZone(_zoneChicago);
	private static final DateTimeFormatter _formatterOhlc = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
			.withZone(_zoneChicago);
	private static final DateTimeFormatter _formatterYYYYMMDD = DateTimeFormatter.ofPattern("yyyy-MM-dd")
			.withZone(_zoneChicago);

	private final ZonedDateTime _zdt;
	private final char _dayCode;

	public DDFDate(ZonedDateTime zdt) {
		_zdt = zdt;
		_dayCode = DDFDate.convertNumberToDayCode(_zdt.getDayOfMonth());
	}

	/**
	 * @param millis
	 *            The time in milliseconds as a <code>long</code>.
	 */

	// XXX TIME!ZONE : assume millis are UTC
	public DDFDate(long millis) {
		_zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), _zoneChicago);
		_dayCode = DDFDate.convertNumberToDayCode(_zdt.getDayOfMonth());
	}



	/**
	 * *
	 * 
	 * @return The <code>ZonedDateTime</code> representing the date.
	 */

	public ZonedDateTime getDate() {
		return _zdt;
	}

	/**
	 * Returns the ddfplus day code for this object.
	 * 
	 * @return The ddfplus day code.
	 */

	public char getDayCode() {
		return this._dayCode;
	}

	/**
	 * Returns the milliseconds associated with this date.
	 * 
	 * @return The date value in milliseconds.
	 */

	public long getMillisCST() {
		return _zdt.withZoneSameInstant(_zoneChicago).toInstant().toEpochMilli();
	}

	/**
	 * Returns a String in the format YYYYMMDDHHNNSS
	 */

	public String toDDFString() {
		return _zdt.format(_formatter);
	}

	public String toYYYYMMDDString() {
		String dt = LocalDate.from(_zdt).format(_formatterYYYYMMDD);
		return dt;
	}

	@Override
	public String toString() {
		return Long.toString(_zdt.toInstant().toEpochMilli());
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
	 * @param daycode
	 *            The <code>char</code> ddfplus day code.
	 * @return The <code>DDFDate</code> object
	 */

	public static DDFDate fromDayCode(final char daycode) {
		int day = DDFDate.convertDayCodeToNumber(daycode);

		if ((day < 1) || (day > 31))
			return null;

		ZonedDateTime zdt = ZonedDateTime.now(_zoneChicago);

		boolean plusMonth = (day < zdt.getDayOfMonth() - 25);
		boolean minusMonth = (day > zdt.getDayOfMonth() + 5);

		if (plusMonth)
			zdt = zdt.plusMonths(1);
		else if (minusMonth)
			zdt = zdt.minusMonths(1);

		zdt = zdt.withDayOfMonth(day);

		return new DDFDate(zdt);
	}

	/**
	 * Parses a date in the format YYYYMMDDHHNNSS
	 * 
	 * @return <code>DDFDate</code> The DDFDate object
	 */

	public static DDFDate fromDDFString(String s) {
		try {
			ZonedDateTime zdt = ZonedDateTime.parse(s, _formatter);
			return new DDFDate(zdt);
		} catch (Exception e) {
			;
		}
		return null;
	}

	public static DDFDate fromYYYYYMMDDString(String s) {
		try {
			ZonedDateTime zdt = ZonedDateTime.parse(s, _formatterYYYYMMDD);
			return new DDFDate(zdt);
		} catch (Exception e) {
			;
		}
		return null;
	}

	public static DDFDate fromDDFStringOhlc(String s) {
		try {
			ZonedDateTime zdt = ZonedDateTime.parse(s, _formatterOhlc);
			return new DDFDate(zdt);
		} catch (Exception e) {
			;
		}
		return null;
	}
}
