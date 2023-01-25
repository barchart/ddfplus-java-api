package com.ddfplus.util;

import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SymbolTest {
    @Test
    public void testGetSymbolType() {
        assertEquals(SymbolType.Equity_US, Symbol.getSymbolType("AAPL"));
        assertEquals(SymbolType.Rates, Symbol.getSymbolType("USTM3.RT"));
        assertEquals(SymbolType.Platts, Symbol.getSymbolType("AA.PT"));
        assertEquals(SymbolType.Future_Option, Symbol.getSymbolType("CLG3|1300C"));
        assertEquals(SymbolType.Equity_Option, Symbol.getSymbolType("TSLA|20221014|232.50C"));
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

    @Test
    public void testOptionsShortSymbol() {
        // March 31
        assertEquals("NGC1300C", new Symbol("NGC1300C", 2022, 9).getShortSymbol(2022, 9));
        assertEquals("CLG1300D", new Symbol("CLG1300D", 2022, 9).getShortSymbol(2022, 9));

        assertEquals("CLG1300D", new Symbol("CLG3|1300C", 2022, 9).getShortSymbol(2022, 9));
        assertEquals("CLG1300D", new Symbol("CLG23|1300C", 2022, 9).getShortSymbol(2022, 9));

        // March 31
        assertEquals("NGH1300C", new Symbol("NGH31|1300C", 2021, 8).getShortSymbol(2021, 8));

        assertEquals("NGH1300C", new Symbol("NGH1|1300C", 2021, 8).getShortSymbol(2021, 8));

        // June 31
        assertEquals("NGM1300C", new Symbol("NGM31|1300C", 2021, 8).getShortSymbol(2021, 8));

        // July 31
        assertEquals("NGN1300C", new Symbol("NGN31|1300C", 2021, 8).getShortSymbol(2021, 8));

        // August 31
        assertEquals("NGO1300C", new Symbol("NGQ31|1300C", 2021, 8).getShortSymbol(2021, 8));

        // Sept 31
        assertEquals("NGP1300C", new Symbol("NGU31|1300C", 2021, 8).getShortSymbol(2021, 8));

        // March 41
        assertEquals("NGC1300C", new Symbol("NGH41|1300C", 2021, 8).getShortSymbol(2021, 8));

        // June 41
        assertEquals("NGI1300C", new Symbol("NGM41|1300C", 2021, 8).getShortSymbol(2021, 8));

        // July 41
        assertEquals("NGL1300C", new Symbol("NGN41|1300C", 2021, 8).getShortSymbol(2021, 8));

        assertEquals("CLT1165P", new Symbol("CLZ32|1165P", 2022, 9).getShortSymbol(2022, 9));
        assertEquals("CLT1165P", new Symbol("CLT1165P", 2022, 9).getShortSymbol(2022, 9));

        assertEquals("CLV1025Q", new Symbol("CLV23|1025P", 2022, 9).getShortSymbol(2022, 9));
        assertEquals("CLV1025Q", new Symbol("CLV1025Q", 2022, 9).getShortSymbol(2022, 9));

        assertEquals("MDUV2|3200P", new Symbol("MDUV2|3200P", 2022, 9).getShortSymbol(2022, 9));
        assertEquals("MMCF3|9650C", new Symbol("MMCF3|9650C", 2022, 9).getShortSymbol(2022, 9));
    }
}
