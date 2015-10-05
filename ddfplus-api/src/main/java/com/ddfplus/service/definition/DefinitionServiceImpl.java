package com.ddfplus.service.definition;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class DefinitionServiceImpl implements DefinitionService {

	private static final String BASE_URL = "http://extras.ddfplus.com/json/";
	private static final String FUTURES_URL = BASE_URL + "/futures/?root=";
	private static final String OPTIONS_URL = BASE_URL + "/options/?root=";
	// private static final String OPTIONS_MONTH_YEAR_URL = BASE_URL +
	// "/options/?root=";

	private static final Logger logger = LoggerFactory.getLogger(DefinitionServiceImpl.class);

	private final Map<String, FutureRoot> futureRoots = new ConcurrentHashMap<String, FutureRoot>();
	private final OkHttpClient httpClient;
	private final Gson gson;

	public DefinitionServiceImpl() {
		httpClient = new OkHttpClient();
		gson = new Gson();
	}

	@Override
	public String[] getAllFutureSymbols(String root) {
		String[] futures = new String[0];

		// Look up in cache first
		FutureRoot futureRoot = futureRoots.get(root);
		if (futureRoot != null) {
			return futureRoot.getContractSymbols();
		}

		// Look up from web service
		Request request = new Request.Builder().url(FUTURES_URL + root).build();
		Response response;
		try {
			response = httpClient.newCall(request).execute();
			if (response.isSuccessful()) {
				// A map is returned, so need collection type
				Type collectionType = new TypeToken<Map<String, FutureRoot>>() {
				}.getType();
				Map<String, FutureRoot> o = gson.fromJson(response.body().charStream(), collectionType);
				futureRoot = o.get(root);
				if (futureRoot != null) {
					processFutureRoot(root, futureRoot);

					futures = futureRoot.getContractSymbols();
				} else {
					logger.error("Could not find futures for root: " + root);
					return futures;
				}
			}
		} catch (Exception e) {
			logger.error("Could not obtain futures definitions for: " + root + " error: " + e.getMessage());
		}
		return futures;
	}

	@Override
	public String getFuturesMonthSymbol(String root, int month) {
		String ret = null;

		// Look up in cache first
		FutureRoot futureRoot = futureRoots.get(root);
		if (futureRoot == null) {
			futureRoot = buildFutureContractCache(root);
		}
		if (futureRoot != null) {
			return futureRoot.getSymbolByMonth(month);
		}

		return ret;
	}

	private FutureRoot buildFutureContractCache(String root) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getAllOptionsSymbols(String root) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getAllOptionsMonthYearSymbols(String root) {
		// TODO Auto-generated method stub
		return null;
	}

	void processFutureRoot(String root, FutureRoot futureRoot) {
		// Build all future symbols
		for (FutureContract c : futureRoot.getContracts()) {
			if (c.isnearest) {
				futureRoot.setNearestContract(c);
			}
			/*
			 * Jerq requires a single digit year in order to stream quotes.
			 */
			String s = root + c.getMonth() + c.getYear().substring((c.getYear().length() - 1));
			futureRoot.addContractSymbol(s);
		}
		// Store by root symbols
		futureRoots.put(root, futureRoot);
	}

	static class FutureRoot {
		private String root;
		private String root_crb;
		private String description;
		private String exchange;
		private FutureContract[] contracts;
		private FutureContract nearestContract;
		private List<String> contractSymbols = new ArrayList<String>();

		public String getRoot() {
			return root;
		}

		public String getSymbolByMonth(int month) {
			if (month > contractSymbols.size() || month < 0) {
				logger.error("Invalid contract, root: " + root + " month: " + month);
			}
			return contractSymbols.get(month);
		}

		public void addContractSymbol(String s) {
			contractSymbols.add(s);

		}

		public String[] getContractSymbols() {
			return contractSymbols.toArray(new String[0]);
		}

		public void setNearestContract(FutureContract c) {
			nearestContract = c;
		}

		public FutureContract getNearestContract() {
			return nearestContract;
		}

		public void setRoot(String root) {
			this.root = root;
		}

		public FutureContract[] getContracts() {
			return contracts;
		}
	}

	static class FutureContract {
		private String month;
		private String year;
		private boolean isnearest;

		public String getMonth() {
			return month;
		}

		public void setMonth(String month) {
			this.month = month;
		}

		public String getYear() {
			return year;
		}

		public void setYear(String year) {
			this.year = year;
		}

		public boolean isIsnearest() {
			return isnearest;
		}

		public void setIsnearest(boolean isnearest) {
			this.isnearest = isnearest;
		}

	}

}
