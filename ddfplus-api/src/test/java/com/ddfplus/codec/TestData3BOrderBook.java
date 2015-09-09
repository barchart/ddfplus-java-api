package com.ddfplus.codec;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ddfplus.codec.Codec;
import com.ddfplus.messages.Data3BOrderBook;

import static org.junit.Assert.assertEquals;

public class TestData3BOrderBook {

	private static final Logger log = LoggerFactory
			.getLogger(TestData3BOrderBook.class);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	// 3,B
	final byte[] ba1 = "\u00013XIZ9,B\u00028X55,63795K25,63790L5,63780M1000,63775N35,63765O5,63800J20,63815I5,63820H10,63825G5,63830F7\u0003"
			.getBytes();

	@Test
	public void testMessage3BOrderBook1() {

		final Data3BOrderBook m1 = (Data3BOrderBook) Codec
				.parseMessage(ba1);

		log.info("m1 : {}", m1);
		// log.info("m1 : {}", m1.toStringAscii());
		// log.info("m1 : {}", m1.toStringHex());
		// log.info("m1 : {}", m1.toStringAsciiHex());

		assertEquals(m1._record, '3');
		assertEquals(m1._subrecord, 'B');
		assertEquals(m1._symbol, "XIZ9");
		assertEquals(m1._basecode, '8');

		assertEquals(m1._bidCount, 5);

		final float[] bidPrices = m1._bidPrices;
		final int[] bidSizes = m1._bidSizes;

		assertEquals(bidPrices[0], 63795, 0);
		assertEquals(bidSizes[0], 25);
		assertEquals(bidPrices[1], 63790, 0);
		assertEquals(bidSizes[1], 5);
		assertEquals(bidPrices[2], 63780, 0);
		assertEquals(bidSizes[2], 1000);
		assertEquals(bidPrices[3], 63775, 0);
		assertEquals(bidSizes[3], 35);
		assertEquals(bidPrices[4], 63765, 0);
		assertEquals(bidSizes[4], 5);

		assertEquals(m1._askCount, 5);

		final float[] askPrices = m1._askPrices;
		final int[] askSizes = m1._askSizes;

		assertEquals(askPrices[0], 63800, 0);
		assertEquals(askSizes[0], 20);
		assertEquals(askPrices[1], 63815, 0);
		assertEquals(askSizes[1], 5);
		assertEquals(askPrices[2], 63820, 0);
		assertEquals(askSizes[2], 10);
		assertEquals(askPrices[3], 63825, 0);
		assertEquals(askSizes[3], 5);
		assertEquals(askPrices[4], 63830, 0);
		assertEquals(askSizes[4], 7);

		// assertEquals(m1._day, 'H');

	}

	// ddf parser can not handle LF at the end
	final byte[] ba2 = "3WIM0,BAL55,7144K4,7143L5,7142M3,7141N17,7140O7,7147J4,7148I4,7149H4,7150G4,7151F18"
			.getBytes();

	@Test
	public void testMessage3BOrderBook2() {

		final Data3BOrderBook m2 = (Data3BOrderBook) Codec
				.parseMessage(ba2);

		log.info("m2 : {}", m2);

		assertEquals(m2._bidCount, 5);

		final float[] bidPrices = m2._bidPrices;
		final int[] bidSizes = m2._bidSizes;

		assertEquals(bidPrices[0], 71.44, 0.0001);
		assertEquals(bidSizes[0], 4);
		assertEquals(bidPrices[1], 71.43, 0.0001);
		assertEquals(bidSizes[1], 5);
		assertEquals(bidPrices[2], 71.42, 0.0001);
		assertEquals(bidSizes[2], 3);
		assertEquals(bidPrices[3], 71.41, 0.0001);
		assertEquals(bidSizes[3], 17);
		assertEquals(bidPrices[4], 71.40, 0.0001);
		assertEquals(bidSizes[4], 7);

		assertEquals(m2._askCount, 5);

		final float[] askPrices = m2._askPrices;
		final int[] askSizes = m2._askSizes;

		assertEquals(askPrices[0], 71.47, 0.0001);
		assertEquals(askSizes[0], 4);
		assertEquals(askPrices[1], 71.48, 0.0001);
		assertEquals(askSizes[1], 4);
		assertEquals(askPrices[2], 71.49, 0.0001);
		assertEquals(askSizes[2], 4);
		assertEquals(askPrices[3], 71.50, 0.0001);
		assertEquals(askSizes[3], 4);
		assertEquals(askPrices[4], 71.51, 0.0001);
		assertEquals(askSizes[4], 18);

	}

}
