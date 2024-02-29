package com.ddfplus.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class SymbolTest {
    @Test
    public void testGetSymbolType() {
        assertEquals(SymbolType.Equity_US, Symbol.getSymbolType("AAPL"));
        assertEquals(SymbolType.Rates, Symbol.getSymbolType("USTM3.RT"));
        assertEquals(SymbolType.Platts, Symbol.getSymbolType("AA.PT"));
        assertEquals(SymbolType.Future_Option, Symbol.getSymbolType("CLG3|1300C"));
        assertEquals(SymbolType.Index, Symbol.getSymbolType("$SPX"));
    }

    @Test
    public void testEquity() {
        assertEquals(SymbolType.Equity_US, Symbol.getSymbolType("IBM"));
        assertEquals(SymbolType.Equity_US, Symbol.getSymbolType("-AA"));
        assertEquals(SymbolType.Fund, Symbol.getSymbolType("AAAAX"));
    }

    @Test
    public void testEquityOptionSymbolType() {
        assertEquals(SymbolType.Equity_Option, Symbol.getSymbolType("TSLA|20221014|232.50C"));
        assertEquals(SymbolType.Equity_Option, Symbol.getSymbolType("$SPX|20221014|232.50C"));
        assertEquals(SymbolType.Equity_Option, Symbol.getSymbolType("$SPX|20221014|232.50WC"));
        assertEquals(SymbolType.Equity_Option, Symbol.getSymbolType("$SPX|20221014|232.50WP"));
        assertEquals(SymbolType.Equity_Option, Symbol.getSymbolType("XYZ|20221014|232.50WC"));
        assertEquals(SymbolType.Equity_Option, Symbol.getSymbolType("XYZ|20221014|232.50WP"));
        assertEquals(SymbolType.Equity_Option, Symbol.getSymbolType("$HDX|20221014|232.50PC"));
        assertEquals(SymbolType.Equity_Option, Symbol.getSymbolType("$HDX|20221014|232.50PP"));
    }

    @Test
    public void testFuturesSymbolType() {
        assertEquals(SymbolType.Future, Symbol.getSymbolType("ESM3"));
        assertEquals(SymbolType.Future, Symbol.getSymbolType("ESM33"));
        assertEquals(SymbolType.Future_Option, Symbol.getSymbolType("ESM3|4160C"));
        assertEquals(SymbolType.Future_Option, Symbol.getSymbolType("ZCN3|535C"));
        assertEquals(SymbolType.Future_Option, Symbol.getSymbolType("ZCN23|535C"));
        assertEquals(SymbolType.Future_Option, Symbol.getSymbolType("KFX2600P"));
        assertEquals(SymbolType.Future_Spread, Symbol.getSymbolType("_S_SP_ZCN3_ZCU3"));
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

    @Test
    public void testCashShortSymbol() {
        assertEquals("LZYY0", new Symbol("LZYY0").getShortSymbol());
        assertEquals("LZYY0", new Symbol("LZYY00").getShortSymbol());
    }

    @Test
    public void testFutureSpreadShortSymbol() {
        assertEquals("Its already short","_S_SP_ZSN3_ZSX3", new Symbol("_S_SP_ZSN3_ZSX3").getShortSymbol());
        assertEquals("Long -> Short","_S_SP_ZSN3_ZSX3", new Symbol("_S_SP_ZSN23_ZSX23").getShortSymbol());
    }

    @Test
    public void testUDSSymbol() {
        Symbol s = new Symbol("_U_VT_1@ZBZ3|11600C_-1@ZBZ3|11650C");
        assertEquals(SymbolType.Future_Option_UserDefinedSpread,  s.getSymbolType());
        assertEquals("ZB",  s.getCommodityCode());
        assertEquals(2,  s.getSpreadLegs().size());
        assertEquals("ZBZ3|11600C",  s.getSpreadLegs().get(0).getSymbol());
        assertEquals("ZBZ3|11650C",  s.getSpreadLegs().get(1).getSymbol());
    }

    @Test
    public void testIsExpiredFuture() {
        Symbol s = new Symbol("CTZ14");
        assertTrue("2014",s.isExpired());
        s = new Symbol("CTZ34");
        assertFalse(s.isExpired());
    }

    @Test
    public void testIsExpiredFutureSpread() {
        Symbol s = new Symbol("_S_SP_SDN4_SDU5");
        assertFalse(s.isExpired());
        s = new Symbol("_S_SP_SDN14_SDU15");
        assertTrue("2014",s.isExpired());
        s = new Symbol("_S_SP_ZDV4_ZDZ5");
        assertFalse(s.isExpired());
    }

    @Test
    public void testIsExpiredEquityOption() {
        Symbol s = new Symbol("SPY|20240228|423.00C");
        assertFalse(s.isExpired());
        s = new Symbol("SPY   240228C00423000");
        assertFalse(s.isExpired());
    }

    @Test
    public void testFutureOption() {
        Symbol s = new Symbol("CCM670P");
        assertFalse(s.isExpired());
        s = new Symbol("KK7H12");
        assertTrue("2012",s.isExpired());
    }


}
