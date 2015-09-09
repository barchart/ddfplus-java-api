/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.db;

import com.ddfplus.messages.DdfMarketDepth;
import com.ddfplus.util.ParserHelper;
import com.ddfplus.util.XMLNode;

/**
 * The BookQuote encapsulates the market depth for a given symbol. The market
 * depth (or "book") is the currently available set of bids, bid sizes, asks,
 * and ask sizes at prices above and below the market. Note that the bid and ask
 * in quote object is known as the "top of the book." Another term for the top
 * of the book is "Level One", whereas the entire market depth (this object) is
 * commonly referred to as "Level Two".
 */
public class BookQuote implements java.io.Serializable {
	static final long serialVersionUID = 8023594349583363700L;

	protected final String symbol;

	protected volatile int askcount = 0;
	protected volatile int bidcount = 0;

	protected volatile float[] askprices = new float[10];
	protected volatile int[] asksizes = new int[10];
	protected volatile float[] bidprices = new float[10];
	protected volatile int[] bidsizes = new int[10];

	protected volatile char basecode = '\0';

	protected volatile long timestamp = 0L;

	public BookQuote(String symbol) {
		this.symbol = symbol;
	}

	/**
	 * Returns the number of ask prices / sizes available in the book.
	 * 
	 * @return Number of Ask Sizes
	 */
	public int getAskCount() {
		return askcount;
	}

	/**
	 * Returns a two indexed Object array, where the first entry is an array of
	 * floating point values representing the ask prices, and the second entry
	 * is an array of integer values representing the sizes of the asking
	 * prices.
	 * 
	 * @return array of prices and sizes
	 */
	public Object[] getAskData() {
		Object[] o = new Object[2];
		o[0] = askprices;
		o[1] = asksizes;
		return o;
	}

	/**
	 * Returns the number of bid prices / sizes available in the book.
	 * 
	 * @return Number of bid prices/sizes
	 */
	public int getBidCount() {
		return bidcount;
	}

	/**
	 * Returns a two indexed Object array, where the first entry is an array of
	 * floating point values representing the bid prices, and the second entry
	 * is an array of integer values representing the sizes of the bid prices.
	 * 
	 * @return array of prices and sizes
	 */
	public Object[] getBidData() {
		Object[] o = new Object[2];
		o[0] = bidprices;
		o[1] = bidsizes;
		return o;
	}

	/**
	 * Returns the symbol of the BookQuote.
	 * 
	 * @return symbol
	 */
	public String getSymbol() {
		return symbol;
	}

	/**
	 * Returns the Base Code of the underlying quote object.
	 * 
	 * @return base code
	 */
	public char getBaseCode() {
		return basecode;
	}

	/**
	 * Sets the Base Code od the underlying data. Note that this method is
	 * rarely called by end-user applications, and is a public method for the
	 * ddf api purposes only.
	 * 
	 * 
	 * @param c
	 *            Base Code
	 */
	public void setBaseCode(char c) {
		if (c != '?')
			basecode = c;
	}

	/**
	 * Timestamp in ms of when message is received.
	 * 
	 * @return <B>long</B> The timestamp of the message in milliseconds.
	 */
	public long getTimeInMillis() {
		return timestamp;
	}

	/**
	 * Converts this BookQuote into an XMLNode for text serialization purposes.
	 * 
	 * @return XMLNode
	 */
	public XMLNode toXMLNode() {
		XMLNode node = new XMLNode("BOOK");
		node.setAttribute("symbol", symbol);
		node.setAttribute("basecode", "" + basecode);

		node.setAttribute("askcount", "" + askcount);
		node.setAttribute("bidcount", "" + bidcount);

		int uc = SymbolInfo.ddfuc2bb(basecode);

		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();

		for (int i = 0; i < askcount; i++) {
			sb1.append("," + Integer.toString(ParserHelper.float2int(uc, askprices[i])));
			sb2.append("," + "" + asksizes[i]);
		}

		if (sb1.length() > 0)
			node.setAttribute("askprices", sb1.substring(1));

		if (sb2.length() > 0)
			node.setAttribute("asksizes", sb2.substring(1));

		sb1 = new StringBuilder();
		sb2 = new StringBuilder();

		for (int i = 0; i < bidcount; i++) {
			sb1.append("," + Integer.toString(ParserHelper.float2int(uc, bidprices[i])));
			sb2.append("," + "" + bidsizes[i]);
		}

		if (sb1.length() > 0)
			node.setAttribute("bidprices", sb1.substring(1));

		if (sb2.length() > 0)
			node.setAttribute("bidsizes", sb2.substring(1));

		return node;
	}

	/**
	 * Takes in a Message3B object and creates a new BookQuote object from it.
	 * 
	 * @param message
	 *            <code>Message3B</code> the ddf message
	 * 
	 * @return BookQuote
	 */
	public static final BookQuote FromDDFMessage(final DdfMarketDepth message) {

		final BookQuote bq = new BookQuote(message.getSymbol());
		bq.basecode = message.getBaseCode();
		bq.askcount = message.getAskCount();
		bq.bidcount = message.getBidCount();

		System.arraycopy(message.getAskPrices(), 0, bq.askprices, 0, bq.askcount);
		System.arraycopy(message.getAskSizes(), 0, bq.asksizes, 0, bq.askcount);
		System.arraycopy(message.getBidPrices(), 0, bq.bidprices, 0, bq.bidcount);
		System.arraycopy(message.getBidSizes(), 0, bq.bidsizes, 0, bq.bidcount);

		return bq;

	}

	/**
	 * Takes in an XMLNode object, and parses this into a BookQuote object.
	 * 
	 * @param node
	 *            <code>XMLNode</code> an XMLNode of type "Book"
	 * 
	 * @return BookQuote
	 */
	public static BookQuote fromXMLNode(final XMLNode node) {

		final BookQuote bq = new BookQuote(node.getAttribute("symbol"));

		bq.basecode = node.getAttribute("basecode").charAt(0);
		bq.askcount = Integer.parseInt(node.getAttribute("askcount"));
		bq.bidcount = Integer.parseInt(node.getAttribute("bidcount"));

		String s = node.getAttribute("askprices");
		if (s != null) {
			String[] sa = s.split(",");
			for (int i = 0; i < sa.length; i++) {
				if (sa[i].length() > 0) {
					bq.askprices[i] = ParserHelper.string2float(sa[i], bq.basecode);
				}
			}
		}

		s = node.getAttribute("asksizes");
		if (s != null) {
			String[] sa = s.split(",");
			for (int i = 0; i < sa.length; i++) {
				if (sa[i].length() > 0) {
					bq.asksizes[i] = Integer.parseInt(sa[i]);
				}
			}
		}

		s = node.getAttribute("bidprices");
		if (s != null) {
			String[] sa = s.split(",");
			for (int i = 0; i < sa.length; i++) {
				if (sa[i].length() > 0) {
					bq.bidprices[i] = ParserHelper.string2float(sa[i], bq.basecode);
				}
			}
		}

		s = node.getAttribute("bidsizes");
		if (s != null) {
			String[] sa = s.split(",");
			for (int i = 0; i < sa.length; i++) {
				if (sa[i].length() > 0) {
					bq.bidsizes[i] = Integer.parseInt(sa[i]);
				}
			}
		}

		return bq;

	}

}
