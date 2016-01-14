/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.db;

import com.ddfplus.messages.DdfMarketBase;

public class MarketEvent {
	public enum MarketEventType {
		PreOpen, Open, High, Low, Close, PreSettlement, Settlement, TradingHalt, TradingResumption;
	}

	private MarketEventType type;
	private float open;
	private float high;
	private float low;
	private float close;
	private float preSettlement;
	private float settlement;
	private String symbol;
	private DdfMarketBase ddfMessage;

	public MarketEvent(MarketEventType eventType) {
		this.type = eventType;
	}

	public MarketEventType getEventType() {
		return this.type;
	}

	public void setEventType(MarketEventType type) {
		this.type = type;
	}

	public float getOpen() {
		return this.open;
	}

	public void setOpen(float f) {
		this.open = f;
	}

	public float getClose() {
		return this.close;
	}

	public void setClose(float f) {
		this.close = f;
	}

	public float getSettlement() {
		return this.settlement;
	}

	public void setSettlement(float f) {
		this.settlement = f;
	}

	public float getPreSettlement() {
		return this.preSettlement;
	}

	public void setPreSettlement(float f) {
		this.preSettlement = f;

	}

	public float getHigh() {
		return this.high;
	}

	public void setHigh(float f) {
		this.high = f;

	}

	public float getLow() {
		return this.low;
	}

	public void setLow(float f) {
		this.low = f;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("MarketEvent(" + ddfMessage.getRecord() + ddfMessage.getSubRecord() + "): ");
		sb.append("type: " + type + " ");
		switch (type) {
		case PreOpen:
			break;
		case Open:
			break;
		case High:
			sb.append("high: " + high);
			break;
		case Low:
			sb.append("low: " + low);
			break;
		case Close:
			sb.append("close: " + close);
			break;
		case PreSettlement:
			sb.append("preSettlement: " + preSettlement);
			break;
		case Settlement:
			sb.append("settlement: " + settlement);
			break;
		default:
			break;
		}
		if (symbol != null) {
			sb.append(" symbol: " + symbol);
		}
		if (ddfMessage != null) {
			sb.append(" DDF: " + ddfMessage);
		}
		return sb.toString();
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public void setDdfMessage(DdfMarketBase msg) {
		this.ddfMessage = msg;
	}

	public DdfMarketBase getDdfMessage() {
		return this.ddfMessage;
	}
}
