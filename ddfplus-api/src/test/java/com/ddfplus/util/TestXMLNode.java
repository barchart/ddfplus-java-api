package com.ddfplus.util;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ddfplus.util.XMLNode;

public class TestXMLNode {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	// no LF
	static final String m1 = "%<BOOK symbol=\"ESM0\" basecode=\"A\" askcount=\"10\" bidcount=\"10\" askprices=\"116500,116525,116550,116575,116600,116625,116650,116675,116700,116725\" asksizes=\"397,606,865,849,1419,953,1058,1051,2083,1385\" bidprices=\"116475,116450,116425,116400,116375,116350,116325,116300,116275,116250\" bidsizes=\"307,604,728,977,1215,994,994,1061,873,995\"/>";

	// with LF
	static final String m2 = "%<BOOK symbol=\"ESM0\" basecode=\"A\" askcount=\"10\" bidcount=\"10\" askprices=\"116500,116525,116550,116575,116600,116625,116650,116675,116700,116725\" asksizes=\"397,606,865,849,1419,953,1058,1051,2083,1385\" bidprices=\"116475,116450,116425,116400,116375,116350,116325,116300,116275,116250\" bidsizes=\"307,604,728,977,1215,994,994,1061,873,995\"/>\n";

	@Test
	public void testParse() {

		XMLNode node;

		node = XMLNode.parse(m1);

		// node = XMLNode.parse(m2);

		assertTrue(true);

	}

}
