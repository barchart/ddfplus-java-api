package com.ddfplus.db;

public class Vwap {
    private long transactionTime;
    private int tradeDate;
    private float vwap;

    public long getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(long transactionTime) {
        this.transactionTime = transactionTime;
    }

    public int getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(int tradeDate) {
        this.tradeDate = tradeDate;
    }

    public float getVwap() {
        return vwap;
    }

    public void setVwap(float vwap) {
        this.vwap = vwap;
    }
}
