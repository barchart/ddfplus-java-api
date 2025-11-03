package com.ddfplus.db;

public class ReferenceVolatilityPrice implements Cloneable{
    private int tradeDate;
    private int atm;
    private String surfaceDomain;
    private float volatility;
    private float premium;
    private float delta;

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

    public float getVolatility() {
        return volatility;
    }

    public void setVolatility(float volatility) {
        this.volatility = volatility;
    }

    public float getPremium() {
        return premium;
    }

    public void setPremium(float premium) {
        this.premium = premium;
    }

    public float getDelta() {
        return delta;
    }

    public void setDelta(float delta) {
        this.delta = delta;
    }

    @Override
    public ReferenceVolatilityPrice clone() {
        try {
            ReferenceVolatilityPrice clone = (ReferenceVolatilityPrice) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
