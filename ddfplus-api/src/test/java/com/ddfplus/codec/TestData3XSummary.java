package com.ddfplus.codec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ddfplus.messages.Data3XSummary;

public class TestData3XSummary {

    private static final Logger log = LoggerFactory.getLogger(TestData3XSummary.class);

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testMessage3X_S_Summary() {
        // 3,S
        final byte[] ba1 = "\u00013IBM,S\u0002AN>>,10/07/2009,12112,12285,12094,12278,5967600\u0003".getBytes();

        final Data3XSummary m1 = (Data3XSummary) Codec.parseMessage(ba1);

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

    @Test
    public void testMessage3X_C_Summary() {
        // 3,C
        final byte[] ba1 = "\u00013IBM,C\u0002AN>>,10/07/2009,12112,12285,12094,12278\u0003".getBytes();

        final Data3XSummary m1 = (Data3XSummary) Codec.parseMessage(ba1);

        log.info("m1 : {}", m1);

        assertEquals(m1._record, '3');
        assertEquals(m1._subrecord, 'C');
        assertEquals(m1._symbol, "IBM");
        assertEquals(m1._basecode, 'A');
        assertEquals(m1._exchange, 'N');

        assertEquals(m1._open, (Float) 121.12F);
        assertEquals(m1._high, (Float) 122.85F);
        assertEquals(m1._low, (Float) 120.94F);
        assertEquals(m1._close, (Float) 122.78F);

        assertNull(m1._volume);

    }

    @Test
    public void testMessage3X_I_Summary() {
        final byte[] ba1 = "\u00013IBM,I\u0002AN>>,10/07/2009,5967600,1000\u0003".getBytes();

        final Data3XSummary m1 = (Data3XSummary) Codec.parseMessage(ba1);

        log.info("m1 : {}", m1);

        assertEquals(m1._record, '3');
        assertEquals(m1._subrecord, 'I');
        assertEquals(m1._symbol, "IBM");
        assertEquals(m1._basecode, 'A');
        assertEquals(m1._exchange, 'N');

        assertNull(m1._open);
        assertNull(m1._high);
        assertNull(m1._low);
        assertNull(m1._close);

        assertEquals(m1._volume, Long.valueOf(5967600));
        assertEquals(m1._openInterest, Long.valueOf(1000));

    }

    @Test
    public void testMessage3X_T_Summary() {
        final byte[] ba1 = "\u00013IBM,T\u0002AN>>,10/07/2009,6967600,6000\u0003".getBytes();

        final Data3XSummary m1 = (Data3XSummary) Codec.parseMessage(ba1);

        log.info("m1 : {}", m1);

        assertEquals(m1._record, '3');
        assertEquals(m1._subrecord, 'T');
        assertEquals(m1._symbol, "IBM");
        assertEquals(m1._basecode, 'A');
        assertEquals(m1._exchange, 'N');

        assertNull(m1._open);
        assertNull(m1._high);
        assertNull(m1._low);
        assertNull(m1._close);

        assertEquals(m1._volume, Long.valueOf(6967600));
        assertEquals(m1._openInterest, Long.valueOf(6000));

    }

}
