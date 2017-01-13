package com.ddfplus.service.feed;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Before;
import org.junit.Test;

import com.ddfplus.api.QuoteHandler;
import com.ddfplus.db.DataMaster;
import com.ddfplus.db.MasterType;
import com.ddfplus.db.Quote;
import com.ddfplus.db.Session;
import com.ddfplus.service.usersettings.UserSettings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FeedServiceTest {

	private DataMaster dataMaster;
	private FeedServiceImpl service;
	private UserSettings userSettings;
	private Map<String, QuoteHandler> quoteExchangeHandlers;

	@Before
	public void setUp() throws Exception {
		dataMaster = new DataMaster(MasterType.Realtime);
		userSettings = new UserSettings();
		userSettings.setUserName("custom");
		userSettings.setPassword("elvis");
		userSettings.setStreamPrimaryServer("qs02.aws.ddfplus.com");

		quoteExchangeHandlers = new ConcurrentHashMap<String, QuoteHandler>();
		service = new FeedServiceImpl(dataMaster, userSettings, quoteExchangeHandlers);
	}

	@Test
	public void getQuotes() {
		service.setQueryUrl("src/test/resources/quote_refresh_cme.xml");
		List<String> s = new ArrayList<String>();
		s.add("IBM");
		service.getQuotes(s);

		// quote 1
		Quote quote = dataMaster.getQuote("ESZ2125C");		
		assertNotNull(quote);
		assertEquals("M", quote.getDDFExchange());
		assertEquals("ESZ2125C", quote.getSymbolInfo().getSymbol());
		// sessions
		Session previousSession = quote.getPreviousSession();
		Session session = quote.getCombinedSession();
		assertTrue(previousSession != session);

		assertEquals(45.75, session.getPrevious(), 0.0);
		assertEquals('3', session.getDayCode());

		// Quote 2
		Quote quote2 = dataMaster.getQuote("HEQ780C");
		assertNotNull(quote2);
		session = quote2.getCombinedSession();
		assertEquals(2.850, session.getHigh(), 0.001);
		assertEquals(2.750, session.getLast(), 0.001);
		assertEquals(2.750, session.getLow(), 0.001);
		assertEquals(2.850, session.getOpen(), 0.001);
		assertEquals(2.475, session.getPrevious(), 0.001);
		assertEquals(2, session.getLastSize(), 0.001);
		assertEquals(102, session.getVolume(), 0.001);
		// previous
		previousSession = quote2.getPreviousSession();
		assertEquals(2.475, previousSession.getLast(), 0.001);
		assertEquals(1965, previousSession.getOpenInterest(), 0.001);
		assertEquals(1.300, previousSession.getPrevious(), 0.001);
		assertEquals(2.475, previousSession.getSettlement(), 0.001);
		assertEquals(202, previousSession.getVolume(), 0.001);

		// Quote 3
		Quote quote3 = dataMaster.getQuote("J6U810P");
		assertNotNull(quote3);

	}
}
