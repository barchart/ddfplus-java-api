package com.ddfplus.service.definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.ddfplus.service.definition.DefinitionServiceImpl.FuturesRoot;
import com.ddfplus.service.definition.DefinitionServiceImpl.OptionsRoot;

@RunWith(MockitoJUnitRunner.class)
public class DefinitionServiceTest {

	private DefinitionServiceImpl definitionService;
	private String root;
	private String json;

	@Before
	public void setUp() throws Exception {
		definitionService = new DefinitionServiceImpl();
	}

	@Test
	public void processFutureRootBadSymbol() throws Exception {
		json = getJson("src/test/resources/futures_root.json");
		root = "BAD";
		FuturesRoot futureRoot = definitionService.processFutureRoot(json, root);
		assertNull(futureRoot);
	}

	@Test
	public void processFutureRoot() throws Exception {
		json = getJson("src/test/resources/futures_root.json");
		root = "CL";
		FuturesRoot futureRoot = definitionService.processFutureRoot(json, root);
		assertNotNull(futureRoot);
		assertEquals(futureRoot, definitionService.getFuturesRoot(root));
		assertEquals(root, futureRoot.getRoot());
		assertEquals("CLX5", futureRoot.getNearestContract().getSymbol());
		String[] contractSymbols = futureRoot.getContractSymbols();
		assertEquals(69, contractSymbols.length);
		assertEquals("CLX5", contractSymbols[0]);
		assertEquals("CLZ5", contractSymbols[1]);

	}

	@Test
	public void processOptionsRootBadSymbol() throws Exception {
		json = getJson("src/test/resources/options_root.json");
		root = "BAD";
		OptionsRoot optionsRoot = definitionService.processOptionsRoot(json, root);
		assertNull(optionsRoot);
	}

	@Test
	public void processOptionsRoot() throws Exception {
		json = getJson("src/test/resources/options_root.json");
		root = "CL";
		OptionsRoot optionsRoot = definitionService.processOptionsRoot(json, root);
		assertNotNull(optionsRoot);
		assertEquals(optionsRoot, definitionService.getOptionsRoot(root));
		assertEquals(root, optionsRoot.getRoot());
		assertEquals(2094, optionsRoot.getAllStrikeSymbols().length);
		String[] monthYearStrikeSymbols = optionsRoot.getMonthYearStrikeSymbols("X2015");
		assertEquals(251, monthYearStrikeSymbols.length);
	}

	private String getJson(String path) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(path));
		StringBuilder sb = new StringBuilder();
		String l = null;
		while ((l = in.readLine()) != null) {
			sb.append(l);
		}
		return sb.toString();
	}

}
