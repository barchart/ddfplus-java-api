package com.ddfplus.net;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ddfplus.service.definition.DefinitionService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;

@RunWith(MockitoJUnitRunner.class)
public class SymbolShortCutsTest {

	private SymbolShortCutsImpl shortCuts;

	@Mock
	private DefinitionService definitionService;

	@Before
	public void setUp() throws Exception {
		shortCuts = new SymbolShortCutsImpl(definitionService);
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
		assertEquals("RBX5", symbols[0]);
		symbols = shortCuts.resolveShortCutSymbols("ABC");
		assertEquals(1, symbols.length);
		assertEquals("ABC", symbols[0]);
	}

	@Test
	public void badShortCutFormat() throws Exception {

		String[] symbols = shortCuts.resolveShortCutSymbols("CL^MMYYYY");
		assertEquals("Could not create shortcut so just pass on original", "CL^MMYYYY", symbols[0]);
	}

	@Test
	public void convertToDdfFeedSymbol() throws Exception {

		String ddfSymbol = shortCuts.convertToDdfFeedSymbol("RBX15");
		assertEquals("RBX5", ddfSymbol);
		ddfSymbol = shortCuts.convertToDdfFeedSymbol("RBX5");
		assertEquals("RBX5", ddfSymbol);
		ddfSymbol = shortCuts.convertToDdfFeedSymbol("RBX2015");
		assertEquals("RBX5", ddfSymbol);
		ddfSymbol = shortCuts.convertToDdfFeedSymbol("RBXZ5");
		assertEquals("RBXZ5", ddfSymbol);
		ddfSymbol = shortCuts.convertToDdfFeedSymbol("ABC");
		assertEquals("ABC", ddfSymbol);
		ddfSymbol = shortCuts.convertToDdfFeedSymbol("ABCM9998");
		assertEquals("ABCM8", ddfSymbol);
	}

	@Test
	public void convertToDdfFeedSymbolFarOutMonths() throws Exception {

		String ddfSymbol = shortCuts.convertToDdfFeedSymbol("RBX25");
		assertEquals("Only applies to NG", "RBX5", ddfSymbol);
		ddfSymbol = shortCuts.convertToDdfFeedSymbol("NGV25");
		assertEquals("NGR5", ddfSymbol);
		ddfSymbol = shortCuts.convertToDdfFeedSymbol("NGK26");
		assertEquals("NGE6", ddfSymbol);
	}

	@Test
	public void allFutures() throws Exception {

		String[] convertedSymbols = new String[] { "CLX5", "CLZ5" };
		when(definitionService.getAllFutureSymbols(anyString())).thenReturn(convertedSymbols);
		String[] symbols = shortCuts.resolveShortCutSymbols("CL^F");
		assertEquals(2, symbols.length);
		assertEquals("CLX5", symbols[0]);
		assertEquals("CLZ5", symbols[1]);

		convertedSymbols = new String[0];
		when(definitionService.getAllFutureSymbols(anyString())).thenReturn(convertedSymbols);
		symbols = shortCuts.resolveShortCutSymbols("BAD^F");
		assertEquals("empty array if bad symbol", 0, symbols.length);

		verify(definitionService, times(2)).getAllFutureSymbols(anyString());
	}

	@Test
	public void futuresMonthShortCut() throws Exception {

		String convertedSymbols = "CLZ5";
		when(definitionService.getFuturesMonthSymbol(anyString(), anyInt())).thenReturn(convertedSymbols);
		String[] symbols = shortCuts.resolveShortCutSymbols("CL*1");
		assertEquals(1, symbols.length);
		assertEquals("CLZ5", symbols[0]);

		when(definitionService.getFuturesMonthSymbol(anyString(), anyInt())).thenReturn(null);
		symbols = shortCuts.resolveShortCutSymbols("BAD*1");
		assertEquals(0, symbols.length);

		verify(definitionService, times(2)).getFuturesMonthSymbol(anyString(), anyInt());
	}

	@Test
	public void futuresMonthShortCutBadValue() throws Exception {

		String[] symbols = shortCuts.resolveShortCutSymbols("CL*a");
		assertEquals(0, symbols.length);

	}

	@Test
	public void allOptions() throws Exception {
		String[] convertedSymbols = new String[] { "CLX1000C", "CLX1000P" };
		when(definitionService.getAllOptionsSymbols(anyString())).thenReturn(convertedSymbols);

		String[] symbols = shortCuts.resolveShortCutSymbols("CL^O");
		assertEquals(2, symbols.length);
		assertEquals("CLX1000C", symbols[0]);
		assertEquals("CLX1000P", symbols[1]);

		convertedSymbols = new String[0];
		when(definitionService.getAllOptionsSymbols(anyString())).thenReturn(convertedSymbols);
		symbols = shortCuts.resolveShortCutSymbols("CL^O");
		assertEquals("empty array if bad symbol", 0, symbols.length);

		verify(definitionService, times(2)).getAllOptionsSymbols(anyString());
	}

	@Test
	public void allOptionsMonthYear() throws Exception {
		String[] convertedSymbols = new String[] { "CLX1000C", "CLX1000P" };
		when(definitionService.getAllOptionsMonthYearSymbols(anyString(), anyString())).thenReturn(convertedSymbols);

		String[] symbols = shortCuts.resolveShortCutSymbols("CL^X2015^OM");
		assertEquals(2, symbols.length);
		assertEquals("CLX1000C", symbols[0]);
		assertEquals("CLX1000P", symbols[1]);

		convertedSymbols = new String[0];
		when(definitionService.getAllOptionsMonthYearSymbols(anyString(), anyString())).thenReturn(convertedSymbols);
		symbols = shortCuts.resolveShortCutSymbols("BAD^MYYYY^OM");
		assertEquals("empty array if bad symbol", 0, symbols.length);

		verify(definitionService, times(2)).getAllOptionsMonthYearSymbols(anyString(), anyString());
	}
}
