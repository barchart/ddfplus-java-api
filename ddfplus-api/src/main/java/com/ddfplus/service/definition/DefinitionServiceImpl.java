package com.ddfplus.service.definition;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class DefinitionServiceImpl implements DefinitionService {

	private static final int NEAREST_MONTH = 0;
	private static final String BASE_URL = "http://extras.ddfplus.com/json/";
	private static final String FUTURES_URL = BASE_URL + "/futures/?root=";
	private static final String OPTIONS_URL = BASE_URL + "/options/?root=";

	private static final Logger logger = LoggerFactory.getLogger(DefinitionServiceImpl.class);

	private final Map<String, FutureRoot> futureRoots = new ConcurrentHashMap<String, FutureRoot>();
	private final Map<String, OptionsRoot> optionsRoots = new ConcurrentHashMap<String, OptionsRoot>();
	private final OkHttpClient httpClient;
	private final Gson gson;

	public DefinitionServiceImpl() {
		httpClient = new OkHttpClient();
		gson = new GsonBuilder().create();
	}

	@Override
	public String[] getAllFutureSymbols(String root) {
		String[] futures = new String[0];

		// Look up in cache first
		FutureRoot futureRoot = futureRoots.get(root);
		if (futureRoot == null) {
			futureRoot = buildFutureContractCache(root);
		}
		if (futureRoot != null) {
			futures = futureRoot.getContractSymbols();
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
			if (month == NEAREST_MONTH) {
				// nearest month
				return futureRoot.getNearestContract().getSymbol();
			}
			return futureRoot.getSymbolByMonth(month);
		}
		return ret;
	}

	@Override
	public String[] getAllOptionsSymbols(String root) {
		String[] options = new String[0];
		// TODO

		OptionsRoot optionsRoot = optionsRoots.get(root);
		if (optionsRoot == null) {
			optionsRoot = buildOptionsContractCache(root);
		}
		if (optionsRoot != null) {
			// futures = futureRoot.getContractSymbols();
		}

		return options;
	}

	@Override
	public String[] getAllOptionsMonthYearSymbols(String root) {
		// TODO
		String[] options = new String[0];
		return options;
	}

	void processFutureRoot(String root, FutureRoot futureRoot) {
		// Build all future symbols
		for (FutureContract contract : futureRoot.getContracts()) {
			if (contract.isnearest) {
				futureRoot.setNearestContract(contract);
			}
			/*
			 * Build DDF contract symbol
			 * 
			 * Jerq requires a single digit year in order to stream quotes.
			 */
			String s = root + contract.getMonth() + contract.getYear().substring((contract.getYear().length() - 1));
			contract.setSymbol(s);
			// Save full list of symbols
			futureRoot.addContractSymbol(s);
		}
		// Store by root symbol
		logger.info(futureRoot.toString());

		futureRoots.put(root, futureRoot);
	}

	private FutureRoot buildFutureContractCache(String root) {
		FutureRoot futureRoot = null;
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
				} else {
					logger.error("Could not find futures for root: " + root);
				}
			}
		} catch (Exception e) {
			logger.error("Could not obtain futures definitions for: " + root + " error: " + e.getMessage());
		}
		return futureRoot;
	}

	// TODO wip
	private OptionsRoot buildOptionsContractCache(String root) {
		OptionsRoot optionsRoot = null;
		// Look up from web service
		Request request = new Request.Builder().url(OPTIONS_URL + root).build();
		Response response;
		try {
			response = httpClient.newCall(request).execute();
			if (response.isSuccessful()) {
				// A map is returned, so need collection type
				Type collectionType = new TypeToken<Map<String, OptionsRoot>>() {
				}.getType();
				Map<String, OptionsRoot> o = gson.fromJson(response.body().charStream(), collectionType);
				optionsRoot = o.get(root);
				if (optionsRoot != null) {
					Option option = optionsRoot.get("X2015");

					// TODO
					processOptionsRoot(root, optionsRoot);
				} else {
					logger.error("Could not find options for root: " + root);
				}
			}
		} catch (Exception e) {
			logger.error("Could not obtain futures definitions for: " + root + " error: " + e.getMessage());
		}
		return optionsRoot;
	}

	private void processOptionsRoot(String root, OptionsRoot optionsRoot) {
		// TODO Auto-generated method stub

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
			if (month >= contractSymbols.size() || month < 0) {
				StringBuilder sb = new StringBuilder(
						"Invalid contract month: " + month + " root: " + root + " valid months: ");
				for (int i = 0; i < contractSymbols.size(); i++) {
					sb.append(i + "=" + contractSymbols.get(i) + ", ");
				}
				logger.error(sb.toString());
				return null;
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

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("Future: " + root + " desc: " + description);
			sb.append(" nearest: " + nearestContract);
			sb.append("\nsymbols: ");
			for (int i = 0; i < contractSymbols.size(); i++) {
				sb.append(i + "=" + contractSymbols.get(i) + ", ");
			}

			return sb.toString();
		}
	}

	static class FutureContract {
		private String month;
		private String year;
		private boolean isnearest;
		private String symbol;

		public String getMonth() {
			return month;
		}

		public String getSymbol() {
			return symbol;
		}

		public void setSymbol(String s) {
			symbol = s;
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

		@Override
		public String toString() {
			return symbol + " mon: " + month + " year: " + year + " nearest:" + isnearest;
		}
	}

	static class OptionsRoot extends HashMap<String, Option> {
	}

	static class Option {
		private String expiration_date;
		private String underlying_future;
		private String days_to_expration;
		private String month;
		private String year;
		private Map<String, OptionsStrike> strikes;

		public Map<String, OptionsStrike> getStrikes() {
			return strikes;
		}

		public String getExpiration_date() {
			return expiration_date;
		}

		public void setExpiration_date(String expiration_date) {
			this.expiration_date = expiration_date;
		}

		public String getUnderlying_future() {
			return underlying_future;
		}

		public void setUnderlying_future(String underlying_future) {
			this.underlying_future = underlying_future;
		}

		public String getDays_to_expration() {
			return days_to_expration;
		}

		public void setDays_to_expration(String days_to_expration) {
			this.days_to_expration = days_to_expration;
		}

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

	}

	static class OptionsStrike {
		private String strike;
		private String type;

		public String getStrike() {
			return strike;
		}

		public void setStrike(String strike) {
			this.strike = strike;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
	}

}
