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
	public void emptySymbol() throws Exception {

		String[] symbols = shortCuts.resolveShortCutSymbols(null);
		assertEquals(0, symbols.length);
		symbols = shortCuts.resolveShortCutSymbols("");
		assertEquals(0, symbols.length);
	}

	@Test
	public void indexSymbol() throws Exception {

		String[] symbols = shortCuts.resolveShortCutSymbols("$ADDT");
		assertEquals(1, symbols.length);
		assertEquals("$ADDT" + "", symbols[0]);
	}

	@Test
	public void noShortCut() throws Exception {

		String[] symbols = shortCuts.resolveShortCutSymbols("RBX15");
		assertEquals(1, symbols.length);
		assertEquals("RBX15", symbols[0]);
	}

	@Test
	@Ignore
	public void allFutures() throws Exception {

		String[] symbols = shortCuts.resolveShortCutSymbols("CL^F");
		assertEquals(69, symbols.length);
		assertEquals("CLX15", symbols[0]);
	}

	@Test
	@Ignore
	public void futuresMonth() throws Exception {

		String[] symbols = shortCuts.resolveShortCutSymbols("CL*0");
		assertEquals(1, symbols.length);
		assertEquals("CLX5", symbols[0]);
		symbols = shortCuts.resolveShortCutSymbols("CL*1");
		assertEquals(1, symbols.length);
		assertEquals("CLZ5", symbols[0]);
	}

	@Test
	@Ignore
	public void allOptions() throws Exception {

		String[] symbols = shortCuts.resolveShortCutSymbols("CL^O");
		assertEquals(1, symbols.length);
		assertEquals("RBX15", symbols[0]);
	}

	@Test
	@Ignore
	public void allOptionsMonthYear() throws Exception {

		String[] symbols = shortCuts.resolveShortCutSymbols("CL^X2015^OM");
		assertEquals(1, symbols.length);
		assertEquals("RBX15", symbols[0]);
	}
}
