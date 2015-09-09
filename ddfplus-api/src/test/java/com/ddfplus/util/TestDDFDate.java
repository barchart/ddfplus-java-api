package com.ddfplus.util;

import java.util.Date;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestDDFDate {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDDFDate() {

		// NY
		DateTime dateTimeNY = new DateTime("2010-06-03T14:32:49.318-04:00");
		System.err.println("" + dateTimeNY);
		System.err.println("" + dateTimeNY.getMillis());

		DateTime dateTimeCH1 = dateTimeNY.withZone(DDFDate.TIME_ZONE_CHICAGO);
		System.err.println("" + dateTimeCH1);
		System.err.println("" + dateTimeCH1.getMillis());

		DateTime dateTimeCH2 = dateTimeNY.withZoneRetainFields(DDFDate.TIME_ZONE_CHICAGO);
		System.err.println("" + dateTimeCH2);
		System.err.println("" + dateTimeCH2.getMillis());

		System.err.println("" + (dateTimeCH2.getMillis() - dateTimeCH1.getMillis()));

		DateTime dateTimeCH3 = new DateTime("2010-06-03T14:32:49.318-05:00");

		long offsetCH3 = DDFDate.TIME_ZONE_CHICAGO.getOffset(dateTimeCH3);

		System.err.println("offsetCH3=" + offsetCH3);

		long millisCH3 = dateTimeCH3.getMillis() + offsetCH3;

		System.err.println("millisCH3=" + millisCH3);

		Date dateCH3 = new Date(dateTimeCH3.getMillis());

		System.err.println("dateTimeCH3=" + dateTimeCH3);
		System.err.println("dateCH3=" + dateCH3);
		System.err.println("dateTimeCH3.millis=" + dateTimeCH3.getMillis());
		System.err.println("dateCH3.millis=" + dateCH3.getTime());
		System.err.println("CH3 diff millis=" + (dateTimeCH3.getMillis() - dateCH3.getTime()));

		assertTrue(true);

	}

}
