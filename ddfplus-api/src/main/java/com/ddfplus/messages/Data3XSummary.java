/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.messages;

import java.time.ZonedDateTime;

import com.ddfplus.codec.Codec;
import com.ddfplus.enums.QuoteType;
import com.ddfplus.util.DDFDate;

/**
 * The Class Data3XSummary.
 */
public class Data3XSummary extends AbstractMsgBaseMarket implements DdfMarketSummary {

    /** The _date. */
    public volatile Long millisCST = null;

    /** The _open. */
    public volatile Float _open = null;;

    /** The _high. */
    public volatile Float _high = null;

    /** The _low. */
    public volatile Float _low = null;

    /** The _close. */
    public volatile Float _close = null;

    /** The _volume. */
    public volatile Long _volume = null;

    /** The _open interest. */
    public volatile Long _openInterest = null;

    /**
     * Instantiates a new data3 x summary.
     * 
     * @param message
     *            the message
     */
    Data3XSummary(byte[] message) {
        super(message);
    }

    public float getClose() {
        return ((this._close == null) ? 0.0f : this._close);
    }

    // not used
    /**
     * Gets the date as millis.
     * 
     * @return the date as millis
     */
    long getDateAsMillis() {
        return ((this.millisCST == null) ? 0L : this.millisCST);
    }

    public float getHigh() {
        return ((this._high == null) ? 0.0f : this._high);
    }

    public float getLow() {
        return ((this._low == null) ? 0.0f : this._low);
    }

    public float getOpen() {
        return ((this._open == null) ? 0.0f : this._open);
    }

    public long getOpenInterest() {
        return ((this._openInterest == null) ? 0L : this._openInterest);
    }

    public long getVolume() {
        return ((this._volume == null) ? 0L : this._volume);
    }

    /**
     * Parses the.
     * 
     * @param ba
     *            the ba
     * @return the data3 x summary
     */
    public static Data3XSummary Parse(final byte[] ba) {

        Data3XSummary message = new Data3XSummary(ba);
        message._record = (char) ba[1];

        int pos = Codec.getIndexOf(ba, ',', 0);
        message._subrecord = (char) ba[pos + 1];
        message._symbol = new String(ba, 2, pos - 2);
        message._basecode = (char) ba[pos + 3];
        message._exchange = (char) ba[pos + 4];
        // 5, 6, 7 are reserved

        int month = Codec.parseIntValue(ba, pos + 8, 2);
        int date = Codec.parseIntValue(ba, pos + 11, 2);
        int year = Codec.parseIntValue(ba, pos + 14, 4);

        message.millisCST = ZonedDateTime.of(year, month, date, 0, 0, 0, 0, DDFDate._zoneChicago).toInstant()
                .toEpochMilli();

        pos = pos + 19;

        if (message._subrecord == 'C') {
            // end-of-day commodity prices
            int pos2 = Codec.getIndexOf(ba, ',', pos);
            message._open = Codec.parseDDFPriceValue(ba, pos, pos2 - pos, message._basecode);
            pos = pos2 + 1;

            pos2 = Codec.getIndexOf(ba, ',', pos);
            message._high = Codec.parseDDFPriceValue(ba, pos, pos2 - pos, message._basecode);
            pos = pos2 + 1;

            pos2 = Codec.getIndexOf(ba, ',', pos);
            message._low = Codec.parseDDFPriceValue(ba, pos, pos2 - pos, message._basecode);
            pos = pos2 + 1;

            message._close = Codec.parseDDFPriceValue(ba, pos, ba.length - pos - 1, message._basecode);

        } else if (message._subrecord == 'S') {
            // end-of-day stock and forex prices and volume
            int pos2 = Codec.getIndexOf(ba, ',', pos);
            message._open = Codec.parseDDFPriceValue(ba, pos, pos2 - pos, message._basecode);
            pos = pos2 + 1;

            pos2 = Codec.getIndexOf(ba, ',', pos);
            message._high = Codec.parseDDFPriceValue(ba, pos, pos2 - pos, message._basecode);
            pos = pos2 + 1;

            pos2 = Codec.getIndexOf(ba, ',', pos);
            message._low = Codec.parseDDFPriceValue(ba, pos, pos2 - pos, message._basecode);
            pos = pos2 + 1;

            pos2 = Codec.getIndexOf(ba, ',', pos);
            message._close = Codec.parseDDFPriceValue(ba, pos, pos2 - pos, message._basecode);
            pos = pos2 + 1;

            message._volume = (long) Codec.parseDDFIntValue(ba, pos, ba.length - pos - 1);

        } else if (message._subrecord == 'I') {
            // yesterday commodity ind vol & open int.
            int pos2 = Codec.getIndexOf(ba, ',', pos);
            message._volume = Codec.parseLongValue(ba, pos, pos2 - pos);
            pos = pos2 + 1;

            message._openInterest = Codec.parseLongValue(ba, pos, ba.length - pos - 1);

        } else if (message._subrecord == 'T') {
            // yesterday commodity composite vol & open int.
            int pos2 = Codec.getIndexOf(ba, ',', pos);
            message._volume = Codec.parseLongValue(ba, pos, pos2 - pos);
            pos = pos2 + 1;

            message._openInterest = Codec.parseLongValue(ba, pos, ba.length - pos - 1);
        }

        return message;
    }

    @Override
    public QuoteType getQuoteType() {
        return QuoteType.SUMMARY;
    }

}
