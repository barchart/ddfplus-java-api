package com.ddfplus.codec;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ddfplus.codec.Codec;
import com.ddfplus.messages.Data27Trade;

import static org.junit.Assert.assertEquals;

public class TestData27Trade {

	private static final Logger log = LoggerFactory
			.getLogger(TestData27Trade.class);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	// 2,8
	final byte[] ba1 = "\u00012HOZ9,7\u0002CJ1021371,5,SG\u0003".getBytes();
	
//	final byte[] ba1 = "\u00012BRK.A,7\u0002CN152440610001,1,5@\u0003".getBytes();
	
	

	@Test
	public void testMessage27Trade() {

		final Data27Trade m1 = (Data27Trade) Codec
				.parseMessage(ba1);

		log.info("t: {}", m1.getTradePrice());
		log.info("m1 : {}", m1);
		// log.info("m1 : {}", m1.toStringAscii());
		// log.info("m1 : {}", m1.toStringHex());
		// log.info("m1 : {}", m1.toStringAsciiHex());

		assertEquals(m1._record, '2');
		assertEquals(m1._subrecord, '7');
		assertEquals(m1._symbol, "HOZ9");
		assertEquals(m1._basecode, 'C');
		assertEquals(m1._exchange, 'J');

		assertEquals(m1._tradePrice, (Float) 2.1371F);
		assertEquals(m1._tradeSize, (Integer) 5);

		assertEquals(m1._day, 'S');
		assertEquals(m1._session, 'G');

	}

}
