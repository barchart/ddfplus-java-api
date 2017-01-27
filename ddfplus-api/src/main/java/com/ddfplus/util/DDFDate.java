/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */

package com.ddfplus.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * The DDFDate class encapsulates a date and provides some utility functions,
 * such as conversions to/from ddfplus day codes.
 */

public class DDFDate {
	public static final DateTimeZone TIME_ZONE_CHICAGO = DateTimeZone.forID("America/Chicago");
	
	public static final ZoneId _zoneChicago = ZoneId.of("America/Chicago");
	private static final DateTimeFormatter _formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(_zoneChicago);
	
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
	
	
	/**	 * 
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
	// XXX TIME!ZONE
	public long getMillisCST() {
		return _zdt.withZoneSameInstant(_zoneChicago).toInstant().toEpochMilli();
	}

	/**
	 * Returns a String in the format YYYYMMDDHHNNSS
	 */

	public String toDDFString() {
		return _zdt.format(_formatter);
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

	// XXX TIME!ZONE millis UTC -> CST
	public static DDFDate fromDayCode(final char daycode) {
		int day = DDFDate.convertDayCodeToNumber(daycode);
		
		if ((day < 1) || (day > 31))
			return null;
		
		ZonedDateTime zdt1 = ZonedDateTime.now(_zoneChicago);		
		ZonedDateTime zdt2 = ZonedDateTime.of(zdt1.getYear(), zdt1.getMonthValue(), day, 0, 0, 0, 0, _zoneChicago);
			
		if (day < zdt1.getDayOfMonth() - 25) // next month
			zdt2 = zdt2.plusMonths(1);
		else if (day > zdt1.getDayOfMonth() + 5) // last month
			zdt2 = zdt2.minusMonths(1);
		
		return new DDFDate(zdt2);
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

	/**
	 * hack to force seemingly UTC time stamps look like Chicago time
	 **/
	public static final long millisCST(final DateTime dateTime) {
		final long millisUTC = dateTime.getMillis();
		final long offsetCST = TIME_ZONE_CHICAGO.getOffset(dateTime);
		final long offsetLOC = DateTimeZone.getDefault().getOffset(dateTime);
		return millisUTC + offsetCST - offsetLOC;
	}
}
