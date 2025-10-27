/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * <p>
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.db;

import com.ddfplus.util.XMLNode;

/**
 * Symbol definition parameters
 *
 */
public class SymbolInfo {

    private char _baseCode;
    private final String _exchange;
    private final String _name;
    private final String _symbol;
    private final float _pointValue;
    private final int _tickIncrement;
    private final int _unitCode;

    private String _longSymbol;
    private String _requestSymbol;
    private String _updatedAt;

    public SymbolInfo(String symbol, String name, String exchange, int unitCode, Float pointValue, int tickIncrement) {
        this(symbol, name, exchange, convertUnitCodeInt2DDF(unitCode), pointValue, tickIncrement);
    }

    public SymbolInfo(String symbol, String name, String exchange, char baseCode, Float pointValue, int tickIncrement) {
        this._symbol = symbol;
        this._name = name;
        this._exchange = exchange;
        this._baseCode = baseCode;
        this._unitCode = ddfuc2bb(baseCode);
        this._pointValue = (pointValue == null) ? 1.0f : pointValue;
        this._tickIncrement = tickIncrement;
    }

    public SymbolInfo clone(String symbol) {
        SymbolInfo si = new SymbolInfo(symbol, this._name, this._exchange, this._baseCode,this._pointValue, this._tickIncrement);
        return si;
    }

    @Override
    public String toString() {
        return "{sym: "+_symbol + " exch: " + _exchange +"  bc: "+_baseCode + " lngSym "+_longSymbol + " reqSym: "+_requestSymbol + "}";
    }

    /**
     * The base code (unit code), tells you how to format the underlying
     * floating point data.
     *
     * @return <code>char</code> The base code.
     */

    public char getBaseCode() {
        return _baseCode;
    }

    public void setBaseCode(char baseCode) {
        this._baseCode = baseCode;
    }

    public String getExchange() {
        return this._exchange;
    }

    public String getUpdatedAt() {
        return _updatedAt;
    }

    public void setUpdatedAt(String _updatedAt) {
        this._updatedAt = _updatedAt;
    }

    /**
     * Return the name of the object. For instance, IBM would return
     * "International Business Machines". Note that this is available only if
     * the connecting JERQ server reports a name on the initial refresh of the
     * data. Any database bound controlling application would be responsible for
     * setting this value.
     *
     * @return <code>String</code> - the name of the Quote
     */

    public String getName() {
        return XMLNode.htmlDecode(_name);
    }

    /*
     * Returns the value of one point in dollar terms. For instance, for stocks,
     * it's always $1.00 as stocks are quoted in $'s. But eMini S&P futures are
     * $50 as one point is equal to $50 of the underlying contract.
     *
     * @return Point value
     */
    public float getPointValue() {
        return _pointValue;
    }

    public String getSymbol() {
        return _symbol;
    }

    public int getTickIncrement() {
        return _tickIncrement;
    }

    public int getUnitCode() {
        return _unitCode;
    }

    public static char convertUnitCodeInt2DDF(int code) {
        switch (code) {
            case -1:
                return '2';
            case -2:
                return '3';
            case -3:
                return '4';
            case -4:
                return '5';
            case -5:
                return '6';
            case -6:
                return '7';
            case 0:
                return '8';
            case 1:
                return '9';
            case 2:
                return 'A';
            case 3:
                return 'B';
            case 4:
                return 'C';
            case 5:
                return 'D';
            case 6:
                return 'E';
            case 7:
                return 'F';
            default:
                return 'A';
        }
    }

    public static int convertBaseCode2Multiplier(char baseCode) {
        switch (baseCode) {
            case '2':
                return 8;
            case '3':
                return 16;
            case '4':
                return 32;
            case '5':
                return 64;
            case '6':
                return 128;
            case '7':
                return 256;
            case '8':
                return 1;
            case '9':
                return 10;
            case 'A':
                return 100;
            case 'B':
                return 1000;
            case 'C':
                return 10000;
            case 'D':
                return 100000;
            case 'E':
                return 1000000;
            case 'F':
                return 10000000;
            default:
                return 1;
        }
    }

    /**
     * Converts a ddfplus base code into an alternate format. The alternative
     * format has some benefits for calculating floating point values.
     *
     * @param c
     *            Base Code
     *
     * @return alternative format
     */
    public static int ddfuc2bb(char c) {
        /**
         * Base Codes
         *
         * 2 = 1/8 = one digit fraction, range is 0 - 7
         *
         * 3 = 1/16 = two digit fraction, range is 0 - 15
         *
         * 4 = 1/32 = two digit fraction, range is 0 - 31
         *
         * 5 = 1/64 = two digit fraction, range is 0 - 63
         *
         * 6 = 1/128 = three digit fraction, range is 0 - 127
         *
         * 7 = 1/256 = three digit fraction, range is 0 - 255
         *
         * 8 = 0 = no decimal places, a whole number
         *
         * 9 = 0.1 = one decimal place
         *
         * A = 0.01 = two decimal places
         *
         * B = 0.001 = three decimal places
         *
         * C = 0.0001 = four decimal places
         *
         * D = 0.00001 = five decimal places
         *
         * E = 0.000001 = six decimal places
         *
         * = unchanged = transmitted by DDFplus with size only data
         */

        if (c == '*')
            return 0;

        int ival = c;

        // Get A-E ==> int
        if (ival >= 65)
            ival -= 7;

        if (ival >= 56) // 8 - E, Returns n * .1
            return (ival - 56);

        switch (c) {
            case '2':
                return -1;
            case '3':
                return -2;
            case '4':
                return -3;
            case '5':
                return -4;
            case '6':
                return -5;
            case '7':
                return -6;
        }
        return 0;
    }

    public String getLongSymbol() {
        return _longSymbol;
    }

    public void setLongSymbol(String _longSymbol) {
        this._longSymbol = _longSymbol;
    }

    public String getRequestSymbol() {
        return _requestSymbol;
    }

    public void setRequestSymbol(String _requestSymbol) {
        this._requestSymbol = _requestSymbol;
    }
}
