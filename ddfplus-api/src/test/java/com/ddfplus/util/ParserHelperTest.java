package com.ddfplus.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ParserHelperTest {

    @Test
    public void float2int_round_1() {
        int v = ParserHelper.float2int(2,4.63f);
        assertEquals(463,v);
        v = ParserHelper.float2int(2,4.635f);
        assertEquals(464,v);
    }

    @Test
    public void float2int_round_2() {
        int v = ParserHelper.float2int(2,179.0044f);
        assertEquals(17900,v);
        v = ParserHelper.float2int(2, 179.0050f);
        assertEquals(17901,v);
        v = ParserHelper.float2int(2,179.0051f);
        assertEquals(17901,v);
    }

    @Test
    public void float2int_round_3() {
        // ZNM21
        int v = ParserHelper.float2int(-4,132.1875f);
        assertEquals(13212,v);
    }

    @Test
    public void float2string() {
        String v = ParserHelper.float2string(4.63f,'A',ParserHelper.PURE_DECIMAL);
        assertEquals("4.63",v);
        v = ParserHelper.float2string(4.635f,'A',ParserHelper.PURE_DECIMAL);
        assertEquals("4.64",v);
        v = ParserHelper.float2string(4.633f,'A',ParserHelper.PURE_DECIMAL);
        assertEquals("4.63",v);
        v = ParserHelper.float2string(4.636f,'A',ParserHelper.PURE_DECIMAL);
        assertEquals("4.64",v);

        v = ParserHelper.float2string(6.0300f,'C',ParserHelper.PURE_DECIMAL);
        assertEquals("6.0300",v);

        v = ParserHelper.float2string(6.03f,'C',ParserHelper.PURE_DECIMAL);
        assertEquals("6.0300",v);

    }

}
