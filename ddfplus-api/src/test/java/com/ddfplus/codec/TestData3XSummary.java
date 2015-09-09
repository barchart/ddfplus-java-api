package com.ddfplus.codec;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ddfplus.codec.Codec;
import com.ddfplus.messages.Data3XSummary;

import static org.junit.Assert.assertEquals;

public class TestData3XSummary {

	private static final Logger log = LoggerFactory
			.getLogger(TestData3XSummary.class);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	// 3,S
	final byte[] ba1 = "\u00013IBM,S\u0002AN>>,10/07/2009,12112,12285,12094,12278,5967600\u0003"
			.getBytes();

	@Test
	public void testMessage3XSummary() {

		final Data3XSummary m1 = (Data3XSummary) Codec
				.parseMessage(ba1);

		log.info("m1 : {}", m1);
		// log.info("m1 : {}", m1.toStringAscii());
		// log.info("m1 : {}", m1.toStringHex());
		// log.info("m1 : {}", m1.toStringAsciiHex());

		assertEquals(m1._record, '3');
		assertEquals(m1._subrecord, 'S');
		assertEquals(m1._symbol, "IBM");
		assertEquals(m1._basecode, 'A');
		assertEquals(m1._exchange, 'N');

		assertEquals(m1._open, (Float) 121.12F);
		assertEquals(m1._high, (Float) 122.85F);
		assertEquals(m1._low, (Float) 120.94F);
		assertEquals(m1._close, (Float) 122.78F);

		assertEquals(m1._volume, (Long) 5967600L);

	}

}
