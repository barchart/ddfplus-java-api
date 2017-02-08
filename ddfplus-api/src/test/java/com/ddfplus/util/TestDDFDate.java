package com.ddfplus.util;

import java.time.ZonedDateTime;

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
		ZonedDateTime zdtNY = ZonedDateTime.parse("2010-06-03T14:32:49.318-04:00");
		ZonedDateTime zdtCH1 = zdtNY.withZoneSameInstant(DDFDate._zoneChicago);
		System.err.println("" + (zdtNY.toInstant().toEpochMilli() - zdtCH1.toInstant().toEpochMilli()));

		assertTrue(true);

	}

}
