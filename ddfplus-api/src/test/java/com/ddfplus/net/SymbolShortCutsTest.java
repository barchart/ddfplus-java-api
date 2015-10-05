package com.ddfplus.net;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class SymbolShortCutsTest {

	private SymbolShortCutsImpl shortCuts;

	@Before
	public void setUp() throws Exception {
		shortCuts = new SymbolShortCutsImpl();
	}

	@Test
	public void indexSymbol() throws Exception {

		String[] symbols = shortCuts.checkForShortCutNotation("$ADDT");
		assertEquals(1, symbols.length);
		assertEquals("$ADDT" + "", symbols[0]);
	}

	@Test
	public void noShortCut() throws Exception {

		String[] symbols = shortCuts.checkForShortCutNotation("RBX15");
		assertEquals(1, symbols.length);
		assertEquals("RBX15", symbols[0]);
	}

	@Test
	@Ignore
	public void allFutures() throws Exception {

		String[] symbols = shortCuts.checkForShortCutNotation("CL^F");
		assertEquals(69, symbols.length);
		assertEquals("CLX15", symbols[0]);
	}

	@Test
	@Ignore
	public void allOptions() throws Exception {

		String[] symbols = shortCuts.checkForShortCutNotation("CL^O");
		assertEquals(1, symbols.length);
		assertEquals("RBX15", symbols[0]);
	}

	@Test
	@Ignore
	public void allOptionsMonthYear() throws Exception {

		String[] symbols = shortCuts.checkForShortCutNotation("CL^Z2011^OM");
		assertEquals(1, symbols.length);
		assertEquals("RBX15", symbols[0]);
	}
}
