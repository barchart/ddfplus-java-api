package com.ddfplus.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SymbolTest {
    @Test
    public void testGetSymbolType() {
        assertEquals(SymbolType.Rates, Symbol.getSymbolType("USTM3.RT"));
        assertEquals(SymbolType.Platts, Symbol.getSymbolType("AA.PT"));
    }
}
