/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ddfplus.db.SymbolInfo;
import com.ddfplus.messages.AbstractMsgBaseMarket;
import com.ddfplus.messages.CtrlTimestamp;
import com.ddfplus.messages.Data20Parameter;
import com.ddfplus.messages.Data21Refresh;
import com.ddfplus.messages.Data27Trade;
import com.ddfplus.messages.Data28BidAsk;
import com.ddfplus.messages.Data29Condition;
import com.ddfplus.messages.Data2ZTrade;
import com.ddfplus.messages.Data3BOrderBook;
import com.ddfplus.messages.Data3XSummary;
import com.ddfplus.messages.DataRefreshXML;
import com.ddfplus.messages.DdfMarketBase;

/**
 * DDF Message Codec
 */
public class Codec {

	/** The Constant LOG. */
	private static final Logger log = LoggerFactory.getLogger(Codec.class);

	/**
	 * Instantiates a new message provider.
	 */
	private Codec() {
		// Singleton
	}

	/**
	 * Gets the index of.
	 * 
	 * @param ba
	 *            the ba
	 * @param item
	 *            the item
	 * @param start
	 *            the start
	 * @return the index of
	 */
	public static int getIndexOf(byte[] ba, char item, int start) {
		byte b = (byte) item;
		for (int i = start; i < ba.length; i++) {
			if (ba[i] == b)
				return i;
		}
		return -1;
	}

	/*
	 * 
	 */
	/**
	 * Parses the ddf int value.
	 * 
	 * @param ba
	 *            the ba
	 * @param start
	 *            the start
	 * @param length
	 *            the length
	 * @return the int
	 */
	public static int parseDDFIntValue(byte[] ba, int start, int length) {
		return (length == 0) ? 0 : Codec.parseIntValue(ba, start, length);
	}

	/**
	 * Parses the ddf price value.
	 * 
	 * @param ba
	 *            the ba
	 * @param start
	 *            the start
	 * @param length
	 *            the length
	 * @param unitcode
	 *            the unitcode
	 * @return the float
	 * @throws NumberFormatException
	 *             the number format exception
	 */
	public static float parseDDFPriceValue(byte[] ba, int start, int length, char unitcode)
			throws NumberFormatException {
		return Codec.parseDDFPriceValue(ba, start, length, SymbolInfo.ddfuc2bb(unitcode));
	}

	/**
	 * Converts a string representation of the data to a floating point value,
	 * based on the unitcode.
	 * 
	 * @param ba
	 *            the ba
	 * @param start
	 *            the start
	 * @param length
	 *            the length
	 * @param basecode
	 *            the basecode
	 * @return the float
	 * @throws NumberFormatException
	 *             the number format exception
	 */

	public static float parseDDFPriceValue(byte[] ba, int start, int length, int basecode)
			throws NumberFormatException {

		if (start + length > ba.length)
			throw new NumberFormatException(
					"Index Out of Bounds array.length=" + ba.length + ", start=" + start + ", length=" + length);

		if (length == 0)
			return 0;

		if (((char) ba[start] == '-') && (length == 1))
			return 0;


		if (basecode >= 0) {
			long lval = Codec.parseLongValue(ba, start, length);
			double d = (double) ((double) lval / (double) Math.pow(10, basecode));
			return (float) d;
		}
		else {
			float f = 0;
			int ival = Codec.parseIntValue(ba, start, length);
			switch (basecode) {
			case -1:
				f = (ival / 10) + ((float) (ival % 10) / 8);
				break;
			case -2:
				f = (ival / 100) + ((float) (ival % 100) / 16);
				break;
			case -3:
				f = (ival / 100) + ((float) (ival % 100) / 32);
				break;
			case -4:
				f = (ival / 100) + ((float) (ival % 100) / 64);
				break;
			case -5:
				f = (ival / 1000) + ((float) (ival % 1000) / 128);
				break;
			case -6:
				f = (ival / 1000) + ((float) (ival % 1000) / 256);
				break;
			}
			return f;
		}
	}

	/**
	 * Parses the int value.
	 * 
	 * @param ba
	 *            the ba
	 * @param start
	 *            the start
	 * @param length
	 *            the length
	 * @return the int
	 * @throws NumberFormatException
	 *             the number format exception
	 */
	public static int parseIntValue(byte[] ba, int start, int length) throws NumberFormatException {
		if (start + length > ba.length)
			throw new NumberFormatException(
					"Index Out of Bounds array.length=" + ba.length + ", start=" + start + ", length=" + length);

		int mult = 1;
		if ((char) ba[start] == '-') {
			mult = -1;
			start++;
			length--;
		}

		// [2, 0, 0, 9]
		int value = 0;
		int base = 1;
		for (int i = start + length - 1; i >= start; i--) {
			value += base * ((int) ba[i] - 48);
			base *= 10;
		}

		return value * mult;
	}

	/**
	 * Parses the long value.
	 * 
	 * @param ba
	 *            the ba
	 * @param start
	 *            the start
	 * @param length
	 *            the length
	 * @return the long
	 */
	public static long parseLongValue(byte[] ba, int start, int length) {
		long mult = 1;
		if ((char) ba[start] == '-') {
			mult = -1;
			start++;
			length--;
		}
		
		// [2, 0, 0, 4]
		long value = 0L;
		int base = 1;
		for (int i = start + length - 1; i >= start; i--) {
			value += base * ((long) ba[i] - 48);
			base *= 10;
		}

		return value * mult;
	}

	/**
	 * Parses a string message into a Message object.
	 * 
	 * @param array
	 *            the array
	 * @return the base market message
	 */

	public static DdfMarketBase parseMessage(byte[] array) {

		final byte[] baOriginal = array;

		boolean showErrorMessage = true;

		AbstractMsgBaseMarket msg = null;

		try {
			switch (array[0]) {
			case 37: // '%'
				/* JERQ initiating / refresh message */
				String message = new String(array);
				if (message.charAt(1) == '<') {
					// ok
				} else if (message.charAt(1) == '%') {
					/*
					 * Compensate for a server bug that sends two % for HTTP
					 * sessions
					 */
					message = message.substring(1);
				} else {
					log.error("format error; message={}", message);
					break;
				}
				msg = DataRefreshXML.Parse(message);
				break;

			case 1: // SOH
				// ddfplus message
				char record = (char) array[1];
				String spreadType = null;
				String[] spreadLegs = null;

				if (record == 'S') {
					Object[] o = Codec.stripSpreadPreamble(array);
					record = '2';
					array = (byte[]) o[5];
					spreadType = (String) o[1];
					spreadLegs = (String[]) o[3];
				}

				switch (record) { // Record
				case '#':
					// Timestamp
					msg = CtrlTimestamp.Parse(array);
					break;
				case '!':
					/*
					 * Timestamp, {soh}!yyyymmddss{etx}
					 * 
					 * Ignore.
					 */
					showErrorMessage = false;
					break;
				case 'C':
				case 'S':
				case '2': {
					int pos = getIndexOf(array, ',', 0);
					char subrecord = (char) array[pos + 1];
					
					switch (subrecord) { // Subrecord
					case '0': {
						String symbol = Codec.parseStringValue(array, 2, pos - 2);
						char baseCode = (char) array[pos + 3];
						char exchange = (char) array[pos + 4];
						int delay = Codec.parseIntValue(array, pos + 5, 2);

						int pos2 = Codec.getIndexOf(array, ',', pos + 7);
						char element = (char) array[pos2 + 1];
						char modifier = (char) array[pos2 + 2];

						switch (element) {
						case '1': // Ask
							msg = new Data28BidAsk(array);
							((Data28BidAsk) msg)._record = '2';
							((Data28BidAsk) msg)._subrecord = '8';
							((Data28BidAsk) msg)._ask = Codec.parseDDFPriceValue(array, pos + 7, pos2 - pos - 7,
									baseCode);
							break;
						case '2': // Bid
							msg = new Data28BidAsk(array);
							((Data28BidAsk) msg)._record = '2';
							((Data28BidAsk) msg)._subrecord = '8';
							((Data28BidAsk) msg)._bid = Codec.parseDDFPriceValue(array, pos + 7, pos2 - pos - 7,
									baseCode);
							break;
						default:
							msg = Data20Parameter.Parse(array);
							break;
						}

						msg._symbol = symbol;
						msg._basecode = baseCode;
						msg._exchange = exchange;
						msg._delay = delay;

						msg._day = (char) array[pos2 + 3];
						msg._session = (char) array[pos2 + 4];
						msg.setMessageTimestamp(pos2 + 5);

						break;
					}
					case '1':
					case '2':
					case '3':
					case '4':
					case '6':
						msg = Data21Refresh.Parse(array);
						break;
					case '5':
						showErrorMessage = false;
						break;
					case '7':
						msg = Data27Trade.Parse(array);
						break;
					case 'Z':
						msg = Data2ZTrade.Parse(array);
						break;
					case '8':
						msg = Data28BidAsk.Parse(array);
						break;
					case '9':
						msg = Data29Condition.Parse(array);
						break;
					case 'F':
						showErrorMessage = false;
						break;
					} // end of sub record

					if ((msg != null) && (spreadType != null)) {
						msg._spreadType = spreadType;
						msg._spreadLegs = spreadLegs;
//						msg._message = baOriginal;
						// We may need re-set the etxpos
						if (msg._etxpos > -1) {
							msg._etxpos += (baOriginal.length - array.length);
						}
					}
					break;
				}
				case '3': {
					int pos = getIndexOf(array, ',', 0);
					switch ((char) array[pos + 1]) { // Subrecord
					case 'B':
						msg = Data3BOrderBook.Parse(array);
						break;
					case 'C':
						msg = Data3XSummary.Parse(array);
						break;
					case 'D':
						showErrorMessage = false;
						break;
					case 'I':
						showErrorMessage = false;
						break;
					case 'R':
						showErrorMessage = false;
						break;
					case 'S':
						msg = Data3XSummary.Parse(array);
						break;
					case 'T':
					case 'V':
					case 'W':
						showErrorMessage = false;
						break;
					}

					break;
				}
				}
			}
		} catch (Exception e) {
			log.error("Parse failed on message: " + new String(array) + " error: ", e);
		}

		if ((msg == null) && (showErrorMessage)) {
			log.error("Could not parse message: " + new String(array));
		}

		return msg;
	}

	/**
	 * Parses the string value.
	 * 
	 * @param ba
	 *            the ba
	 * @param start
	 *            the start
	 * @param length
	 *            the length
	 * @return the string
	 */
	public static String parseStringValue(byte[] ba, int start, int length) {
		char[] ca = new char[length];
		for (int i = 0; i < length; i++) {
			ca[i] = (char) ba[i + start];
		}
		return new String(ca);
	}

	/*
	 * This is still under construction.<br /> 0 - char subrecord<br /> 1 -
	 * (String) Spread Type<br /> 2 - (Integer) Number of Legs<br /> 3 -
	 * (String[]) Legs<br /> 4 - Parseable Message<br />
	 * 
	 * @param ba the ba
	 * 
	 * @return the object[]
	 * 
	 * @return
	 */
	protected static Object[] stripSpreadPreamble(byte[] ba) {
		// _SCLN9,8_AJ10SP2CLV9,-232,-,,,GG_
		// 0123456789a123456789b123456789d123456789e

		int pos = Codec.getIndexOf(ba, ',', 0);

		String symbol = Codec.parseStringValue(ba, 2, pos - 2);
		char subrecord = (char) ba[pos + 1];

		int spStart = pos + 7;
		String spreadType = Codec.parseStringValue(ba, pos + 7, 2);
		int numberOfLegs = Codec.parseIntValue(ba, pos + 9, 1);
		String[] legs = new String[numberOfLegs];
		legs[0] = symbol;

		pos += 10;
		for (int i = 1; i < numberOfLegs; i++) {
			int pos2 = Codec.getIndexOf(ba, ',', pos);
			legs[i] = Codec.parseStringValue(ba, pos, pos2 - pos);
			pos = pos2 + 1;
		}

		int start2 = pos;
		switch (subrecord) {
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
			// N.B. Keep the comma
			start2--;
			break;
		}

		int length = spStart + (ba.length - start2);
		byte[] ba2 = new byte[length];

		System.arraycopy(ba, 0, ba2, 0, spStart);
		System.arraycopy(ba, start2, ba2, spStart, ba.length - start2);

		StringBuilder sb = new StringBuilder("_S_" + spreadType);
		for (String s : legs) {
			sb.append("_" + s);
		}

		byte[] ba3 = new byte[ba2.length - symbol.length() + sb.length()];
		ba3[0] = 1;
		ba3[1] = 50;

		for (int i = 0; i < sb.length(); i++) {
			ba3[2 + i] = (byte) sb.charAt(i);
		}

		System.arraycopy(ba2, 2 + symbol.length(), ba3, 2 + sb.length(), ba3.length - (2 + sb.length()));

		return new Object[] { subrecord, spreadType, numberOfLegs, legs, ba2, ba3 };
	}

}
