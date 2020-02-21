package com.ddfplus.codec;

import com.ddfplus.messages.Data27Trade;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static com.ddfplus.messages.AbstractMsgBaseMarket.ASCII_SHIFT;
import static org.junit.Assert.assertEquals;

public class TestTimestampParsing {

	private static final Logger log = LoggerFactory
			.getLogger(TestTimestampParsing.class);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTimestampParsingNoSecond() {

		int year = 2021;
		int month = 2;
		int day = 10;
		int hour = 16;
		int minute = 30;
		int second = 1;
		int ms = 123;

		byte[] msg = "\u00012HOZ9,7\u0002CJ1021371,5,SG\u0003".getBytes();
		byte []ts = new byte[9];

		ts[0] = (byte) (year / 100);
		ts[1] = (byte) (ASCII_SHIFT + year % 100);
		ts[2] = (byte) (ASCII_SHIFT + month);
		ts[3] = (byte) (ASCII_SHIFT + day);
		ts[4] = (byte) (ASCII_SHIFT + hour);
		ts[5] = (byte) (ASCII_SHIFT + minute);
		ts[6] = (byte) (ASCII_SHIFT + second);

		ts[7] = (byte) (ms & 0xFF);
		ts[8] = (byte) ((ms >> 8) & 0xFF);

		final Data27Trade m1 = (Data27Trade) Codec
				.parseMessage((new String(msg) + new String(ts)).getBytes());

		assertEquals(m1._day, 'S');
		assertEquals(m1._session, 'G');

		LocalDateTime localDateTime = m1.getLocalDateTime();

		assertEquals(year, localDateTime.getYear());
		assertEquals(month, localDateTime.getMonth().getValue());
		assertEquals(day, localDateTime.getDayOfMonth());
		assertEquals(hour, localDateTime.getHour());
		assertEquals(minute, localDateTime.getMinute());
		assertEquals(second, localDateTime.getSecond());
		assertEquals(TimeUnit.MILLISECONDS.toNanos(ms), localDateTime.getNano());
	}
}
