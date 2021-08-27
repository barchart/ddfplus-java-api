/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ddfplus.messages.DdfMarketTrade;
import com.ddfplus.messages.DdfMessageBase;

public class FeedEvent {

	private Date timestamp;
	private DdfMessageBase ddfMessage;
	private Quote quote;
	private BookQuote bookQuote;
	private CumulativeVolume cumVolume;
	private List<MarketEvent> marketEvents;
	private Ohlc ohlc;
	private DdfMarketTrade trade;
	private boolean refreshMessage;

	public boolean isDdfMessage() {
		return ddfMessage != null;
	}

	public DdfMessageBase getDdfMessage() {
		return ddfMessage;
	}

	public void setDdfMessage(DdfMessageBase ddfMessage) {
		this.ddfMessage = ddfMessage;
	}

	public boolean isTimestamp() {
		return timestamp != null;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setDate(Date date) {
		this.timestamp = date;
	}

	public boolean isQuote() {
		return quote != null;
	}

	public Quote getQuote() {
		return quote;
	}

	public void setQuote(Quote quote) {
		this.quote = quote;
	}

	public boolean isBookQuote() {
		return bookQuote != null;
	}

	public BookQuote getBook() {
		return bookQuote;
	}

	public void setBook(BookQuote book) {
		this.bookQuote = book;
	}

	public boolean isMarketEvents() {
		return marketEvents != null && marketEvents.size() > 0;
	}

	public List<MarketEvent> getMarketEvents() {
		return marketEvents;
	}

	public void addMarketEvent(MarketEvent event) {
		if (marketEvents == null) {
			marketEvents = new ArrayList<>();
		}
		marketEvents.add(event);
	}

	public void setMarketEvents(List<MarketEvent> marketEvent) {
		this.marketEvents = marketEvent;
	}

	public boolean isCumVolume() {
		return cumVolume != null;
	}

	public CumulativeVolume getCumVolume() {
		return cumVolume;
	}

	public void setCumVolume(CumulativeVolume volume) {
		this.cumVolume = volume;
	}

	public boolean isOhlc() {
		return ohlc != null;
	}

	public Ohlc getOhlc() {
		return this.ohlc;
	}

	public void setOhlc(Ohlc ohlc) {
		this.ohlc = ohlc;

	}

	public boolean isTrade() {
		return trade != null;
	}

	public DdfMarketTrade getTrade() {
		return trade;
	}

	public void setTrade(DdfMarketTrade ddfMessage) {
		this.trade = ddfMessage;
	}

    public void setRefreshMessage(boolean b) {
		this.refreshMessage = b;
    }
    public boolean isRefreshMessage() { return this.refreshMessage; }
}
