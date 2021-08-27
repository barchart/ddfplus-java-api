package com.ddfplus.util;

import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SymbolTest {
    @Test
    public void testGetSymbolType() {
        assertEquals(SymbolType.Rates, Symbol.getSymbolType("USTM3.RT"));
        assertEquals(SymbolType.Platts, Symbol.getSymbolType("AA.PT"));
    }

    @Test
    public void testFuturesShortSymbol() {
        // March 31
        assertEquals("NGH1", new Symbol("NGH31").getShortSymbol(2021, 8));

        // June 31
        assertEquals("NGM1", new Symbol("NGM31").getShortSymbol(2021, 8));

        // July 31
        assertEquals("NGN1", new Symbol("NGN31").getShortSymbol(2021, 8));

        // August 31
        assertEquals("NGO1", new Symbol("NGQ31").getShortSymbol(2021, 8));

        // Sept 31
        assertEquals("NGP1", new Symbol("NGU31").getShortSymbol(2021, 8));

        // March 41
        assertEquals("NGC1", new Symbol("NGH41").getShortSymbol(2021, 8));

        // June 41
        assertEquals("NGI1", new Symbol("NGM41").getShortSymbol(2021, 8));

        // July 41
        assertEquals("NGL1", new Symbol("NGN41").getShortSymbol(2021, 8));
    }
}
