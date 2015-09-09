/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */

package com.ddfplus.messages;

import com.ddfplus.enums.QuoteType;
import com.ddfplus.util.XMLNode;

/**
 * JERQ server refresh version 2 message.
 * 
 * When attaching to a ddfplus JERQ server in one of the INET_*** modes, and
 * requesting a series of symbols (not exchanges), the server starts the
 * transaction by delivering a series of refresh messages to build and populate
 * the client-side active database.
 * <P>
 * This message is an XML-like message. Unlike other subclases of Message, this
 * class contains little data, but access to the XMLNode object that contains
 * all of the data.
 */

public class DataRefreshXML extends AbstractMsgBaseMarket implements DdfMarketRefreshXML {

	volatile XMLNode _XMLNode = null;

	/**
	 * Instantiates a new data refresh xml.
	 * 
	 * @param ba
	 *            the ba
	 */
	DataRefreshXML(byte[] ba) {
		super(ba);
	}

	public XMLNode getXMLNode() {
		return _XMLNode;
	}

	/**
	 * Parses the.
	 * 
	 * @param text
	 *            the text
	 * @return the data refresh xml
	 */
	public static DataRefreshXML Parse(String text) {

		DataRefreshXML msg = new DataRefreshXML(text.getBytes());

		if (text.charAt(0) == '%') {
			text = text.substring(1);
		}

		XMLNode node = XMLNode.parse(text);
		msg._record = 'X';
		msg._symbol = node.getAttribute("symbol");
		msg.setBaseCode(node.getAttribute("basecode").charAt(0));
		msg._XMLNode = node;

		return msg;

	}

	protected void appendConcrete(StringBuilder text) {
		text.append(_XMLNode.toString());
	}

	public QuoteType getQuoteType() {
		return QuoteType.REFRESH;
	}

}
