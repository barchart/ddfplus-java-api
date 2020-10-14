package com.ddfplus.db;

import com.ddfplus.db.MarketEvent.MarketEventType;
import com.ddfplus.messages.CtrlTimestamp;
import com.ddfplus.messages.DdfMarketBase;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class QuoteTest {

	private Quote quote;

	private SymbolInfo symbolInfo;

	@Before
	public void setUp() throws Exception {

	}


	@Test
	public void toJson() {

		symbolInfo = new SymbolInfo("HGEN", "MEZ900C", "G", '2', null, 1);
		quote = new Quote(symbolInfo);
		// Current Session
		quote.getCombinedSession().setDayCode('1');
		// T Session
		Session t = quote.createSession('1', 'T');
		t.setLast(100);
		String json = quote.toJSONString();
		assertNotNull(json);
//		System.out.println(json);
	}

	@Test
	public void toXml() {

		symbolInfo = new SymbolInfo("HGEN", "MEZ900C", "G", '2', null, 1);
		quote = new Quote(symbolInfo);
		// Current Session
		quote.getCombinedSession().setDayCode('1');
		// T Session
		Session t = quote.createSession('1', 'T');
		t.setLast(100);
		String xml = quote.toXMLNode(true).toXMLString();
		assertNotNull(xml);
//		System.out.println(xml);
	}



}
