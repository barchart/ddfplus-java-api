package com.ddfplus.codec;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ddfplus.codec.Codec;
import com.ddfplus.messages.Data20Parameter;
import com.ddfplus.messages.Data28BidAsk;

import static org.junit.Assert.assertEquals;

public class TestData20Parameter {

	private static final Logger log = LoggerFactory
			.getLogger(TestData20Parameter.class);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	// 2,0
	final byte[] ba1 = "\u00012SF0,0\u00022B1010530,D0Q \u0003".getBytes();
	final byte[] ba2 = "\u00012MEZ900C,0\u00022G10100,10C \u0003".getBytes();
	

	@Test	
	public void testMessage20Parameter1() {
		System.out.println(Codec.parseMessage(ba1));

		final Data20Parameter m1 = (Data20Parameter) Codec
				.parseMessage(ba1);

		log.info("m1 : {}", m1);
		// log.info("m1 : {}", m1.toStringAscii());
		// log.info("m1 : {}", m1.toStringHex());
		// log.info("m1 : {}", m1.toStringAsciiHex());

		assertEquals(m1._record, '2');
		assertEquals(m1._subrecord, '0');
		assertEquals(m1._symbol, "SF0");
		assertEquals(m1._basecode, '2');

		assertEquals(m1._element, 'D');
		assertEquals(m1._modifier, '0');
		assertEquals(m1._value, 1053.0F);

	}

	@Test		
	public void testMessage28Parameter2() {
		System.out.println(Codec.parseMessage(ba2));
		final Data28BidAsk m1 = (Data28BidAsk) Codec
				.parseMessage(ba2);

		log.info("m1 : {}", m1);
		// log.info("m1 : {}", m1.toStringAscii());
		// log.info("m1 : {}", m1.toStringHex());
		// log.info("m1 : {}", m1.toStringAsciiHex());

		assertEquals(m1._record, '2');
		assertEquals(m1._subrecord, '8');
		assertEquals(m1._symbol, "MEZ900C");
		assertEquals(m1._basecode, '2');

		assertEquals(m1._ask, (Float) 10.0F);
	}
	
	
}
