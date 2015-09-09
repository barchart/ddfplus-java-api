package com.ddfplus.db;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ddfplus.db.CumulativeVolume;
import com.ddfplus.util.XMLNode;

public class TestCumulativeVolume {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	static final String VOLUME_1 = "<CV symbol=\"LEJ0\" basecode=\"B\" tickincrement=\"1\" last=\"95850\" lastsize=\"1\" lastcvol=\"14\" date=\"20100630120000\" count=\"24\" data=\"95950,12:95700,291:95775,86:95400,135:95725,37:95600,106:95925,29:95550,162:95525,94:95425,82:95500,166:95475,129:95675,154:95575,135:95900,45:95800,265:95375,114:95450,189:95850,66:95875,13:95750,61:95825,4:95650,504:95625,120\"/>";

	@Test
	public void testCumulativeVolume1() {

		XMLNode node = XMLNode.parse(VOLUME_1);

		CumulativeVolume volume = CumulativeVolume.fromXMLNode(node);

		assertEquals("LEJ0", volume._symbol);
		assertEquals('B', volume.getBaseCode());
		assertEquals(95.850F, volume._last, 0.01F);
		assertEquals(24, volume._data.size());

	}

	static final String VOLUME_2 = "<CV symbol=\"CLZ3\" basecode=\"A\" tickincrement=\"1\" last=\"8600\" lastsize=\"1\" lastcvol=\"20\" date=\"20100530120000\" count=\"0\"/>";

	@Test
	public void testCumulativeVolume2() {

		XMLNode node = XMLNode.parse(VOLUME_2);

		CumulativeVolume volume = CumulativeVolume.fromXMLNode(node);

		assertEquals("CLZ3", volume._symbol);
		assertEquals('A', volume.getBaseCode());
		assertEquals(86.00F, volume._last, 0.01F);
		assertEquals(0, volume._data.size());

	}

}
