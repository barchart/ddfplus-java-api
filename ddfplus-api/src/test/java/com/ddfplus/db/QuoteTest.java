package com.ddfplus.db;

import com.ddfplus.util.DDFDate;
import com.ddfplus.util.XMLNode;
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
        assertNotNull(t_session);
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
	}

	@Test
	public void toXML_VN() {
		symbolInfo = new SymbolInfo("RCK.VN", "rck", "TSX-V", 'B', null, 1);
		quote = new Quote(symbolInfo);
		quote.setBid(5.31f);
		quote.setBidSize(1);
		quote.setAsk(5.5f);
		quote.setAskSize(2);
		XMLNode xml = quote.toXMLNode(true);
		assertEquals("5310",xml.getAttribute("bid"));
		assertEquals("1",xml.getAttribute("bidsize"));
		assertEquals("5500",xml.getAttribute("ask"));
		assertEquals("2",xml.getAttribute("asksize"));
	}

	@Test
	public void toJson_VN() {
		symbolInfo = new SymbolInfo("RCK.VN", "rck", "TSX-V", 'B', null, 1);
		quote = new Quote(symbolInfo);
		quote.setBid(5.31f);
		quote.setBidSize(1);
		quote.setAsk(5.5f);
		quote.setAskSize(2);
		String json = quote.toJSONString();
		assertNotNull(json);
		JSONObject obj = new JSONObject("{" + json + "}");
		JSONObject jsonObject = obj.getJSONObject("RCK.VN");
		float bid = jsonObject.getFloat("bid");
		assertEquals(5.310,bid,0.01);
		assertEquals(1,jsonObject.getInt("bidsize"));
		assertEquals(5.50,jsonObject.getFloat("ask"),0.01);
		assertEquals(2,jsonObject.getInt("asksize"));
	}

	@Test
	public void cloneTest() {
		symbolInfo = new SymbolInfo("RCK.VN", "rck", "TSX-V", 'B', null, 1);
		quote = new Quote(symbolInfo);
		quote.setBid(5.31f);
		quote.setBidSize(1);
		quote.setAsk(5.5f);
		quote.setAskSize(2);

		Quote c = (Quote) quote.clone();
		assertNotEquals(quote.getCombinedSession().getParentQuote(),c.getCombinedSession().getParentQuote());
	}

}
