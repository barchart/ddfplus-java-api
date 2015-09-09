package com.ddfplus.codec;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ddfplus.codec.Codec;
import com.ddfplus.messages.Data21Refresh;

import static org.junit.Assert.assertEquals;

public class TestData21Refresh {

	private static final Logger log = LoggerFactory
			.getLogger(TestData21Refresh.class);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	// 2,1
	final byte[] ba1 = "\u00012SF0,1\u00022B10,,,,,-,-,,,,,,,,,Q \u0003"
			.getBytes();

	@Test
	public void testMessage21Refresh1() {

		final Data21Refresh m1 = (Data21Refresh) Codec
				.parseMessage(ba1);

		log.info("m1 : {}", m1);
		// log.info("m1 : {}", m1.toStringAscii());
		// log.info("m1 : {}", m1.toStringHex());
		// log.info("m1 : {}", m1.toStringAsciiHex());

		assertEquals(m1._record, '2');
		assertEquals(m1._subrecord, '1');
		assertEquals(m1._symbol, "SF0");
		assertEquals(m1._basecode, '2');
		assertEquals(m1._exchange, 'B');

		assertEquals(m1._open, (Float) null);
		assertEquals(m1._high, (Float) null);
		assertEquals(m1._low, (Float) null);
		assertEquals(m1._last, (Float) null);

		assertEquals(m1._bid, (Float) 0.0F);
		assertEquals(m1._ask, (Float) 0.0F);

		assertEquals(m1._open2, (Float) null);
		assertEquals(m1._previous, (Float) null);
		assertEquals(m1._close, (Float) null);
		assertEquals(m1._close2, (Float) null);
		assertEquals(m1._settle, (Float) null);
		assertEquals(m1._previousVolume, (Long) null);
		assertEquals(m1._openInterest, (Long) null);
		assertEquals(m1._volume, (Long) null);

		assertEquals(m1._day, 'Q');

	}

	// 2,2
	final byte[] ba2 = "\u00012HIG,2\u0002AN15,2445,2604,2404,2582,,,,2481,,,2582,,,12196949,3 \u0003"
			.getBytes();

	@Test
	public void testMessage21Refresh2() {

		final Data21Refresh m2 = (Data21Refresh) Codec
				.parseMessage(ba2);

		log.info("m1 : {}", m2);
		// log.info("m1 : {}", m2.toStringAscii());
		// log.info("m1 : {}", m2.toStringHex());
		// log.info("m1 : {}", m2.toStringAsciiHex());

		assertEquals(m2._record, '2');
		assertEquals(m2._subrecord, '2');
		assertEquals(m2._symbol, "HIG");
		assertEquals(m2._basecode, 'A');
		assertEquals(m2._exchange, 'N');

		// log.info("m2._open : {}", m2._open);

		assertEquals(m2._open, (Float) 24.45F);
		assertEquals(m2._high, (Float) 26.04F);
		assertEquals(m2._low, (Float) 24.04F);
		assertEquals(m2._last, (Float) 25.82F);

		assertEquals(m2._bid, (Float) null);
		assertEquals(m2._ask, (Float) null);

		assertEquals(m2._open2, (Float) null);
		assertEquals(m2._previous, (Float) 24.81F);
		assertEquals(m2._close, (Float) null);
		assertEquals(m2._close2, (Float) null);
		assertEquals(m2._settle, (Float) 25.82F);
		assertEquals(m2._previousVolume, (Long) null);
		assertEquals(m2._openInterest, (Long) null);
		assertEquals(m2._volume, (Long) 12196949L);

		assertEquals(m2._day, '3');

	}

	// 2,6
	final byte[] ba3 = "\u00012$DJUBSSO,6\u0002Ao10,5515,5642,5501,5631,,,,,,,,,,,S \u0003"
			.getBytes();

	@Test
	public void testMessage21Refresh3() {

		final Data21Refresh m3 = (Data21Refresh) Codec
				.parseMessage(ba3);

		log.info("m1 : {}", m3);
		// log.info("m1 : {}", m3.toStringAscii());
		// log.info("m1 : {}", m3.toStringHex());
		// log.info("m1 : {}", m3.toStringAsciiHex());

		assertEquals(m3._record, '2');
		assertEquals(m3._subrecord, '6');
		assertEquals(m3._symbol, "$DJUBSSO");
		assertEquals(m3._basecode, 'A');
		assertEquals(m3._exchange, 'o');

		// log.info("m2._open : {}", m2._open);

		assertEquals(m3._open, (Float) 55.15F);
		assertEquals(m3._high, (Float) 56.42F);
		assertEquals(m3._low, (Float) 55.01F);
		assertEquals(m3._last, (Float) 56.31F);

		assertEquals(m3._bid, (Float) null);
		assertEquals(m3._ask, (Float) null);

		assertEquals(m3._open2, (Float) null);
		assertEquals(m3._previous, (Float) null);
		assertEquals(m3._close, (Float) null);
		assertEquals(m3._close2, (Float) null);
		assertEquals(m3._settle, (Float) null);
		assertEquals(m3._previousVolume, (Long) null);
		assertEquals(m3._openInterest, (Long) null);
		assertEquals(m3._volume, (Long) null);

		assertEquals(m3._day, 'S');

	}

}
