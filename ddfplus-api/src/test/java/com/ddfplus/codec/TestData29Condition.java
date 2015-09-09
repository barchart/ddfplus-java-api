package com.ddfplus.codec;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ddfplus.codec.Codec;
import com.ddfplus.enums.MarketConditionType;
import com.ddfplus.messages.Data29Condition;

import static org.junit.Assert.assertEquals;

public class TestData29Condition {

	private static final Logger log = LoggerFactory
			.getLogger(TestData29Condition.class);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	// 2,9 byte[]
	final byte[] ba1 = "\u00012FTNT,9\u0002*Q15A,  H \u0003".getBytes();

	@Test
	public void testMessage29Condition1() {

		final Data29Condition m1 = (Data29Condition) Codec
				.parseMessage(ba1);

		log.info("m1 : {}", m1);
		// log.info("m1 : {}", m1.toStringAscii());
		// log.info("m1 : {}", m1.toStringHex());
		// log.info("m1 : {}", m1.toStringAsciiHex());

		assertEquals(m1._record, '2');
		assertEquals(m1._subrecord, '9');
		assertEquals(m1._symbol, "FTNT");
		assertEquals(m1._basecode, '*');

		assertEquals(m1._marketCondition, MarketConditionType.TRADING_HALT);

		assertEquals(m1._day, 'H');

	}

}
