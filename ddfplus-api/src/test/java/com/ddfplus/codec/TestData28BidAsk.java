package com.ddfplus.codec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ddfplus.messages.Data28BidAsk;

public class TestData28BidAsk {

	private static final Logger log = LoggerFactory.getLogger(TestData28BidAsk.class);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	// 2,8
	final byte[] ba1 = "\u00012HOZ9,8\u0002CJ1020911,5,20919,1,SG\u0003".getBytes();

	@Test
	public void testMessage28BidAsk1() {

		final Data28BidAsk m1 = (Data28BidAsk) Codec.parseMessage(ba1);

		log.info("m1 : {}", m1);
		// log.info("m1 : {}", m1.toStringAscii());
		// log.info("m1 : {}", m1.toStringHex());
		// log.info("m1 : {}", m1.toStringAsciiHex());

		assertEquals(m1._record, '2');
		assertEquals(m1._subrecord, '8');
		assertEquals(m1._symbol, "HOZ9");
		assertEquals(m1._basecode, 'C');
		assertEquals(m1._exchange, 'J');

		assertEquals(m1._bid, (Float) 2.0911F);
		assertEquals(m1._bidSize, (Integer) 5);
		assertEquals(m1._ask, (Float) 2.0919F);
		assertEquals(m1._askSize, (Integer) 1);

		assertEquals(m1._day, 'S');
		assertEquals(m1._day, 'S');
		assertEquals(m1._session, 'G');

	}

	@Test
	public void testMessage28BidAsk_BidAskEmptyMissing() {

		// Bid =
		// Ask = 20919
		byte[] msg = "\u00012HOZ9,8\u0002CJ10,5,20919,1,SG\u0003".getBytes();
		Data28BidAsk m1 = (Data28BidAsk) Codec.parseMessage(msg);

		// Bid is missing/tmepy
		assertEquals(m1._record, '2');
		assertEquals(m1._subrecord, '8');
		assertNull("bid is null", m1._bid);
		assertNull("size is ignored, null", m1._bidSize);
		assertEquals(m1._ask, (Float) 2.0919F);
		assertEquals(m1._askSize, (Integer) 1);

		// Ask is empty
		msg = "\u00012HOZ9,8\u0002CJ1020911,5,,1,SG\u0003".getBytes();
		m1 = (Data28BidAsk) Codec.parseMessage(msg);

		log.info("m1 : {}", m1);

		assertEquals(m1._record, '2');
		assertEquals(m1._subrecord, '8');
		assertNull("ask is null", m1._ask);
		assertNull("size is ignored, null", m1._askSize);
	}

	@Test
	public void testMessage28BidAsk_BidAskDash() {

		// Bid = -
		// Ask = 20919
		byte[] msg = "\u00012HOZ9,8\u0002CJ10-,5,20919,1,SG\u0003".getBytes();
		Data28BidAsk m1 = (Data28BidAsk) Codec.parseMessage(msg);

		assertEquals(m1._record, '2');
		assertEquals(m1._subrecord, '8');

		assertNull("Should be null", m1._bid);
		assertNull(m1._bidSize);
		assertEquals(m1._ask, (Float) 2.0919F);
		assertEquals(m1._askSize, (Integer) 1);

		// Bid = 20911
		// Ask = -
		msg = "\u00012HOZ9,8\u0002CJ1020911,5,-,1,SG\u0003".getBytes();
		m1 = (Data28BidAsk) Codec.parseMessage(msg);

		assertEquals(m1._record, '2');
		assertEquals(m1._subrecord, '8');

		assertEquals(2.0911F, m1._bid, 0.0);
		assertEquals(5, m1._bidSize, 0.0);
		assertNull("Should be null", m1._ask);
		assertNull(m1._askSize);
	}

	@Test
	public void message28Corrupted() {

		byte[] msg = "\u00012COMM,8A".getBytes();

		Data28BidAsk m1 = (Data28BidAsk) Codec.parseMessage(msg);
		assertNull("should return null", m1);

	}

}
