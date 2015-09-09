/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */

package com.ddfplus.messages;

import com.ddfplus.codec.Codec;
import com.ddfplus.enums.QuoteType;

/**
 * ddfplus record 3, subrecord B.
 * 
 * These are Market Depth or "Book" messages. Market Depth is a series of bids
 * and asks with corresponding sizes. Normally, bids and asks are the "best" bid
 * and "best" ask. This is also known as the "top of the book". The book itself
 * also shows the second best bid, the second best ask, the third best, etc. -
 * <B>if</B> available. This is commonly referred to as the "order book", or
 * just "book".
 * <P>
 * The entries in this message are ordered best downward, with best being in
 * array index 0. The indexes of the ask prices correspond with the indexed of
 * the ask sizes, and the indexes of the bid prices correspond with the indexes
 * of the bid sizes.
 */

public class Data3BOrderBook extends AbstractMsgBaseMarket implements DdfMarketDepth {

	/** The _ask count. */
	public volatile int _askCount = 0;

	/** The _ask prices. */
	public final float[] _askPrices = new float[10];

	/** The _ask sizes. */
	public final int[] _askSizes = new int[10];

	/** The _bid count. */
	public volatile int _bidCount = 0;

	/** The _bid prices. */
	public final float[] _bidPrices = new float[10];

	/** The _bid sizes. */
	public final int[] _bidSizes = new int[10];

	/**
	 * Instantiates a new data3 b order book.
	 * 
	 * @param message
	 *            the message
	 */
	Data3BOrderBook(byte[] message) {
		super(message);
	}

	/**
	 * Gets the ask count.
	 * 
	 * @return the number of ask entries in the book.
	 */

	public int getAskCount() {
		return _askCount;
	}

	/**
	 * Gets the bid count.
	 * 
	 * @return the number of bid (offer) entries in the book.
	 */

	public int getBidCount() {
		return _bidCount;
	}

	/**
	 * Returns the <code>float[]</code> array of ask prices. Note that this
	 * array always has a fixed length. Use the #getAskCount() method to
	 * determine which entries are valid.
	 * 
	 * @return the indexed array of ask prices
	 */

	public float[] getAskPrices() {
		return _askPrices;
	}

	/**
	 * Returns the <code>int[]</code> array of ask sizes. Note that this array
	 * always has a fixed length. Use the #getAskCount() method to determine
	 * which entries are valid.
	 * 
	 * @return the indexed array of ask sizes
	 */

	public int[] getAskSizes() {
		return _askSizes;
	}

	/**
	 * Returns the <code>float[]</code> array of bid prices. Note that this
	 * array always has a fixed length. Use the #getBidCount() method to
	 * determine which entries are valid.
	 * 
	 * @return the indexed array of bid prices
	 */

	public float[] getBidPrices() {
		return _bidPrices;
	}

	/**
	 * Returns the <code>int[]</code> array of bid sizes. Note that this array
	 * always has a fixed length. Use the #getBidCount() method to determine
	 * which entries are valid.
	 * 
	 * @return the indexed array of ask prices
	 */

	public int[] getBidSizes() {
		return _bidSizes;
	}

	/**
	 * Parses the.
	 * 
	 * @param ba
	 *            the ba
	 * @return the data3 b order book
	 */
	public static Data3BOrderBook Parse(byte[] ba) {
		// <soh>3CLZ6,B<stx>AZ33,9517K1,9514L1,9530M1,9615J1,9618I1,9609H1<etx>

		Data3BOrderBook msg = new Data3BOrderBook(ba);
		int pos = Codec.getIndexOf(ba, ',', 0);

		msg._symbol = Codec.parseStringValue(ba, 2, pos - 2);
		msg.setBaseCode((char) ba[pos + 3]);
		msg._exchange = (char) ba[pos + 4];
		msg._record = (char) ba[1];
		msg._subrecord = (char) ba[pos + 1];

		if ((char) ba[pos + 5] == 'A')
			msg._bidCount = 10;
		else
			msg._bidCount = ((int) ba[pos + 5]) - 48;

		if ((char) ba[pos + 6] == 'A')
			msg._askCount = 10;
		else
			msg._askCount = ((int) ba[pos + 6]) - 48;

		if (ba.length < pos + 9)
			return msg;

		pos = pos + 8;
		boolean more = true;
		while (more) {
			int pos2 = Codec.getIndexOf(ba, ',', pos);
			if (pos2 == -1) {
				pos2 = ba.length - 1;
				more = false;
			}

			int xx = 0;
			for (xx = pos; xx < pos2; xx++) {
				if ((ba[xx] >= 75) && (ba[xx] <= 90)) {
					// Bids are denoted positionally K .. T
					int i = ba[xx] - 75;
					msg._bidPrices[i] = Codec.parseDDFPriceValue(ba, pos, xx - pos, msg._basecode);
					msg._bidSizes[i] = Codec.parseIntValue(ba, xx + 1, pos2 - xx - 1);
				} else if ((ba[xx] >= 65) && (ba[xx] <= 74)) {
					int i = 9 - (ba[xx] - 65);
					msg._askPrices[i] = Codec.parseDDFPriceValue(ba, pos, xx - pos, msg._basecode);
					msg._askSizes[i] = Codec.parseIntValue(ba, xx + 1, pos2 - xx - 1);
				}
			}

			pos = pos2 + 1;
		}

		return msg;
	}

	public QuoteType getQuoteType() {
		return QuoteType.BOOK;
	}

}
