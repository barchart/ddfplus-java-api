package com.ddfplus.db;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SessionTest {

    private Session session;
    private Quote quote;

    @Before
    public void setUp() throws Exception {
        quote = new Quote(new SymbolInfo("IBM", "IBM", "NYSE", 1, 1F, 1));
        session = new Session(quote);
    }

    @Test
    public void clearLasts() {
        session.setLast(1.0F);
        session.setLast(1.1F);
        session.setLast(1.2F);
        assertEquals(1.2f,session.getLast(),0.0);
        session.clearLasts();
        assertEquals(0.0f,session.getLast(),0.0);
    }


}
