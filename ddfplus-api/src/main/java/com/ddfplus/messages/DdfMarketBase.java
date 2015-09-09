/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.messages;

import com.ddfplus.enums.QuoteType;

/**
 * DDF Market Data Message
 */
public interface DdfMarketBase extends DdfMessageBase {

	/**
	 * The Base code, or unit code, tells you how to format the floating point
	 * value. Further, it is used to create the floating point value from a
	 * string or integer representation.
	 * 
	 * @return char The base code
	 */

	char getBaseCode();

	/**
	 * The Day code tells you what day the data is for. Since futures, and
	 * eventually all instruments can trade for a different day than the
	 * calendar shows, this is needed to properly render the data. For example,
	 * the E-Mini S&amp;P stops trading for Tuesday at 3:15 on Tuesday. Then, at
	 * 3:30 pm it starts trading again, but the new data is for Wednesday!
	 * 
	 * @return The day code
	 */
	char getDay();

	/**
	 * The delay is the time that the message *should* be delayed, <I>if</I> the
	 * message is realtime, <B>or</B> the time that the message is delayed. This
	 * depends on the feed that is being received from ddfplus.
	 * 
	 * @return the delay
	 */

	int getDelay();

	/**
	 * Gets the position etx.
	 * 
	 * @return the position etx
	 */
	int getPositionETX();

	/**
	 * The exchange code identifies the originating exchange for the quote.
	 * 
	 * @return The exchange code.
	 */

	char getExchange();

	/**
	 * Each ddfplus message has a record and a subrecord type. This tells you
	 * how to process the message into a meaningful quote.
	 * 
	 * @return The record type.
	 */

	char getRecord();

	/**
	 * The session is an indicator if the quote is originated from the pit or
	 * electronically. These vary from exchange to exchange, and some exchanges
	 * don't have a session indicator.
	 * 
	 * @return The session code.
	 */

	char getSession();

	/**
	 * Along with the record type, the subrecord tells you how to process the
	 * message into a meaningful quote.
	 * 
	 * @return The subrecord type.
	 */
	char getSubRecord();

	/**
	 * The symbol for which the message is generated.
	 * 
	 * @return The symbol.
	 */

	String getSymbol();

	/**
	 * Tells how this message will affect quote object.
	 * 
	 * @return the quote type
	 * @return Type of Quote
	 */
	QuoteType getQuoteType();

}