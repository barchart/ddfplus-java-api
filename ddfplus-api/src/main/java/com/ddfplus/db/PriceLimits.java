package com.ddfplus.db;

public class PriceLimits {
    private int tradeDate;
    private long transactionTime;
    private float upperPriceLimit;
    private float lowerPriceLimit;

    public int getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(int tradeDate) {
        this.tradeDate = tradeDate;
    }

    public long getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(long transactionTime) {
        this.transactionTime = transactionTime;
    }

    public float getUpperPriceLimit() {
        return upperPriceLimit;
    }

    public void setUpperPriceLimit(float upperPriceLimit) {
        this.upperPriceLimit = upperPriceLimit;
    }

    public float getLowerPriceLimit() {
        return lowerPriceLimit;
    }

    public void setLowerPriceLimit(float lowerPriceLimit) {
        this.lowerPriceLimit = lowerPriceLimit;
    }
}
