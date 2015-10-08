package com.ddfplus.service.definition;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class DefinitionServiceImpl implements DefinitionService {

	// Every 12 hours
	private static final int DEFAULT_DEFINITION_REFRESH_INTERVAL_SEC = 60 * 60 * 12;
	private static final int NEAREST_MONTH = 0;
	private static final String BASE_URL = "http://extras.ddfplus.com/json/";
	private static final String FUTURES_URL = BASE_URL + "/futures/?root=";
	private static final String OPTIONS_URL = BASE_URL + "/options/?root=";

	private static final Logger logger = LoggerFactory.getLogger("DefinitionService");

	private final Map<String, FuturesRoot> futureRoots = new ConcurrentHashMap<String, FuturesRoot>();
	private final Map<String, OptionsRoot> optionsRoots = new ConcurrentHashMap<String, OptionsRoot>();
	private final Gson gson;
	private OkHttpClient httpClient;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private long refreshIntervalSec = DEFAULT_DEFINITION_REFRESH_INTERVAL_SEC;

	public DefinitionServiceImpl() {
		httpClient = new OkHttpClient();
		httpClient.setConnectTimeout(3, TimeUnit.SECONDS);
		httpClient.setReadTimeout(20, TimeUnit.SECONDS);
		gson = new GsonBuilder().create();
	}

	@Override
	public void init(Long intervalSec) {
		if (intervalSec != null) {
			this.refreshIntervalSec = intervalSec;
		}
		/*
		 * Start a refresh thread
		 */
		logger.info("Scheduling a symbol refresh every " + refreshIntervalSec + " seconds.");
		RefreshThread refreshThread = new RefreshThread();
		scheduler.scheduleAtFixedRate(refreshThread, refreshIntervalSec, refreshIntervalSec, TimeUnit.SECONDS);
	}

	@Override
	public String[] getAllFutureSymbols(String root) {
		String[] futures = new String[0];

		// Look up in cache first
		FuturesRoot futureRoot = futureRoots.get(root);
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
		FuturesRoot futureRoot = futureRoots.get(root);
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

		OptionsRoot optionsRoot = optionsRoots.get(root);
		if (optionsRoot == null) {
			optionsRoot = buildOptionsContractCache(root);
		}
		if (optionsRoot != null) {
			options = optionsRoot.getAllStrikeSymbols();
		}

		return options;
	}

	@Override
	public String[] getAllOptionsMonthYearSymbols(String root, String monthYear) {
		String[] options = new String[0];

		OptionsRoot optionsRoot = optionsRoots.get(root);
		if (optionsRoot == null) {
			optionsRoot = buildOptionsContractCache(root);
		}
		if (optionsRoot != null) {
			options = optionsRoot.getMonthYearStrikeSymbols(monthYear);
		}

		return options;
	}

	private FuturesRoot buildFutureContractCache(String root) {
		FuturesRoot futureRoot = null;
		// Look up from web service
		Request request = new Request.Builder().url(FUTURES_URL + root).build();
		Response response;
		try {
			response = httpClient.newCall(request).execute();
			if (response.isSuccessful()) {
				String json = response.body().string();
				if (logger.isDebugEnabled()) {
					logger.debug("< " + json);
				}
				futureRoot = processFutureRoot(json, root);
			}
		} catch (Exception e) {
			logger.error("Could not obtain futures definitions for: " + root + " error: " + e.getMessage());
		}
		return futureRoot;
	}

	FuturesRoot processFutureRoot(String json, String root) {
		FuturesRoot futureRoot = null;
		// A map is returned, so need collection type
		Type collectionType = new TypeToken<Map<String, FuturesRoot>>() {
		}.getType();
		Map<String, FuturesRoot> o = gson.fromJson(json, collectionType);
		if (o != null) {
			futureRoot = o.get(root);
		}
		if (futureRoot == null) {
			logger.error("Could not find futures for root: " + root);
			return futureRoot;
		}

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
		if (logger.isInfoEnabled()) {
			logger.info(futureRoot.toString());
		}

		futureRoots.put(root, futureRoot);

		return futureRoot;

	}

	private OptionsRoot buildOptionsContractCache(String root) {
		OptionsRoot optionsRoot = null;
		// Look up from web service
		Request request = new Request.Builder().url(OPTIONS_URL + root).build();
		Response response;
		try {
			response = httpClient.newCall(request).execute();
			if (response.isSuccessful()) {
				String json = response.body().string();
				if (logger.isDebugEnabled()) {
					logger.debug("< " + json);
				}
				optionsRoot = processOptionsRoot(json, root);
			}
		} catch (Exception e) {
			logger.error("Could not obtain futures definitions for: " + root + " error: " + e.getMessage());
		}
		return optionsRoot;
	}

	OptionsRoot processOptionsRoot(String json, String root) {
		OptionsRoot optionsRoot = null;
		// A map is returned, so need collection type
		Type collectionType = new TypeToken<Map<String, OptionsRoot>>() {
		}.getType();
		Map<String, OptionsRoot> o = gson.fromJson(json, collectionType);
		optionsRoot = o.get(root);
		if (optionsRoot != null) {
			optionsRoot.setRoot(root);
			optionsRoots.put(root, optionsRoot);
			if (logger.isInfoEnabled()) {
				logger.info(optionsRoot.toString());
			}
		} else {
			logger.error("Could not find options for root: " + root);
		}
		return optionsRoot;
	}

	public FuturesRoot getFuturesRoot(String root) {
		return futureRoots.get(root);
	}

	public OptionsRoot getOptionsRoot(String root) {
		return optionsRoots.get(root);
	}

	public long getRefreshPeriodSec() {
		return refreshIntervalSec;
	}

	public void setRefreshPeriodSec(long refreshPeriodSec) {
		this.refreshIntervalSec = refreshPeriodSec;
	}

	static class FuturesRoot {
		private String root;
		private String root_crb;
		private String description;
		private String exchange;
		private FutureContract[] contracts;
		private FutureContract nearestContract;
		// By Month Year
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
			StringBuilder sb = new StringBuilder("Futures: " + root + " desc: " + description);
			sb.append(" nearest: " + nearestContract);
			sb.append("\nsymbols: ");
			for (int i = 0; i < contractSymbols.size(); i++) {
				sb.append(i + "=" + contractSymbols.get(i) + ", ");
			}

			return sb.toString();
		}
	}

	/*
	 * By month and year
	 */
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

	/*
	 * Option Month Year Symbol --> Option Month Year Object
	 */
	static class OptionsRoot extends LinkedHashMap<String, OptionMonthYear> {

		private String root;

		public String getRoot() {
			return root;
		}

		public void setRoot(String root) {
			this.root = root;
		}

		public String[] getAllMonthYearKeys() {
			return keySet().toArray(new String[size()]);
		}

		/*
		 * Return strikes for all Month Years
		 */
		public String[] getAllStrikeSymbols() {
			List<String> symbols = new ArrayList<String>();
			String[] allMonthYearKeys = getAllMonthYearKeys();
			for (String key : allMonthYearKeys) {
				OptionMonthYear optionMonthYear = get(key);
				String[] strikes = optionMonthYear.getAllStrikeSymbols();
				symbols.addAll(Arrays.asList(strikes));
			}
			return symbols.toArray(new String[symbols.size()]);
		}

		/*
		 * Get strikes for a month and year.
		 */
		public String[] getMonthYearStrikeSymbols(String monthYear) {
			String[] ret = new String[0];

			OptionMonthYear optionMonthYear = get(monthYear);
			if (optionMonthYear != null) {
				ret = optionMonthYear.getAllStrikeSymbols();
			}

			return ret;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("Options: " + root + " totalMonthYears: " + size());
			int numStrikes = 0;
			String tmp = null;
			for (java.util.Map.Entry<String, OptionMonthYear> my : entrySet()) {
				numStrikes += my.getValue().getAllStrikeSymbols().length;
				tmp += my.getKey() + "=" + my.getValue().getAllStrikeSymbols().length + ", ";
			}
			sb.append(" totalStrikes: " + numStrikes + "\n");
			sb.append(tmp);
			return sb.toString();
		}

	}

	static class OptionMonthYear {
		// Key
		private String monthYear;

		private String expiration_date;
		private String underlying_future;
		private String days_to_expration;
		private String month;
		private String year;
		// Strikes by Option Symbol, i.e. CLX1000C --> Strike data
		private Map<String, OptionsStrike> strikes;

		public String[] getAllStrikeSymbols() {
			return strikes.keySet().toArray(new String[strikes.size()]);
		}

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

		public String getMonthYear() {
			return monthYear;
		}

		public void setMonthYear(String monthYear) {
			this.monthYear = monthYear;
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

	private class RefreshThread implements Runnable {

		@Override
		public void run() {
			logger.info("Running background symbol refresh..");
			for (String root : futureRoots.keySet()) {
				logger.info("Running symbol refresh for future root: " + root);
				buildFutureContractCache(root);
			}
			for (String root : optionsRoots.keySet()) {
				logger.info("Running symbol refresh for option root: " + root);
				buildOptionsContractCache(root);
			}

		}
	}

}
