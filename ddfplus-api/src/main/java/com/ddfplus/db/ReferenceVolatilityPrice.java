package com.ddfplus.db;

public class ReferenceVolatilityPrice {
    private int tradeDate;
    private int atm;
    private String surfaceDomain;
    private long volatility;
    private long premium;
    private long delta;

    public int getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(int tradeDate) {
        this.tradeDate = tradeDate;
    }

    public int getAtm() {
        return atm;
    }

    public void setAtm(int atm) {
        this.atm = atm;
    }

    public String getSurfaceDomain() {
        return surfaceDomain;
    }

    public void setSurfaceDomain(String surfaceDomain) {
        this.surfaceDomain = surfaceDomain;
    }

    public long getVolatility() {
        return volatility;
    }

    public void setVolatility(long volatility) {
        this.volatility = volatility;
    }

    public long getPremium() {
        return premium;
    }

    public void setPremium(long premium) {
        this.premium = premium;
    }

    public long getDelta() {
        return delta;
    }

    public void setDelta(long delta) {
        this.delta = delta;
    }
}
