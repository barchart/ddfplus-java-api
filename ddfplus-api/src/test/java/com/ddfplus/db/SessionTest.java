package com.ddfplus.db;

import com.ddfplus.util.DDFDate;
import com.ddfplus.util.XMLNode;
import org.junit.Before;
import org.junit.Test;

import java.time.ZonedDateTime;

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

    @Test
    public void testDayTimestamp_TimestampAndDayNoDifference() {
        // Set timestamp, 2022-04-19T17:23:37.704-05:00[America/Chicago]
        long tsMs = 1650407017704L;
        session.setTimeInMillis(tsMs);
        // Set day to same day of 4/19/2022 17:23:37
        ZonedDateTime dateZdt = ZonedDateTime.of(2022,4,19,17,23,37,0,DDFDate._zoneChicago);
        session.setDayCode(new DDFDate(dateZdt));

        XMLNode xmlNode = session.toXMLNode();
        assertEquals("20220419172337",xmlNode.getAttribute("timestamp"));
        assertEquals("I",xmlNode.getAttribute("day"));
    }

    @Test
    public void testDayTimestamp_DayIsNewerThanTimestamp() {
        // Set timestamp, 2022-04-19T17:23:37.704-05:00[America/Chicago]
        long tsMs = 1650407017704L;
        session.setTimeInMillis(tsMs);
        // Set day to Next Day 2022-04-20T00:00-05:00[America/Chicago]
        ZonedDateTime dateZdt = ZonedDateTime.of(2022,4,20,0,0,0,0,DDFDate._zoneChicago);
        session.setDayCode(new DDFDate(dateZdt));

        XMLNode xmlNode = session.toXMLNode();
        assertEquals("20220419172337",xmlNode.getAttribute("timestamp"));
        assertEquals("J",xmlNode.getAttribute("day"));
    }


}
