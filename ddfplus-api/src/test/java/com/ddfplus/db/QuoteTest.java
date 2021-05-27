package com.ddfplus.db;

import com.ddfplus.util.DDFDate;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.time.ZonedDateTime;

import static org.junit.Assert.*;

public class QuoteTest {

	private Quote quote;

	private SymbolInfo symbolInfo;

	@Before
	public void setUp() throws Exception {

	}


	@Test
	public void testJson() {
		symbolInfo = new SymbolInfo("HGEN", "MEZ900C", "G", '2', null, 1);
		quote = new Quote(symbolInfo);
		// Current Session
		quote.getCombinedSession().setDayCode('1');
		// T Session
		Session t = quote.createSession('1', 'T');
		t.setLast(100);
		String json = quote.toJSONString();
		assertNotNull(json);
		System.out.println(json);
		JSONObject obj = new JSONObject("{" + json + "}");
		JSONObject jsonObject = obj.getJSONObject("HGEN");
		JSONObject t_session = jsonObject.getJSONObject("t_session");
		assertTrue(t_session != null);
		assertEquals(100, t_session.getInt("last"));
	}

	@Test
	public void testJsonZSession() {
		symbolInfo = new SymbolInfo("HGEN", "MEZ900C", "G", '2', null, 1);
		quote = new Quote(symbolInfo);
		// Current Session
		quote.getCombinedSession().setDayCode(DDFDate.fromDDFString("20210501000000"));
		// Z Session
		Session z = quote.createZSession();
		z.setLast(100);
		z.setDayCode(new DDFDate(ZonedDateTime.now()));
		String json = quote.toJSONString();
		assertNotNull(json);
		System.out.println(json);
		JSONObject obj = new JSONObject("{" + json + "}");
		JSONObject jsonObject = obj.getJSONObject("HGEN");
		assertEquals(100, jsonObject.getInt("last_z"));
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
