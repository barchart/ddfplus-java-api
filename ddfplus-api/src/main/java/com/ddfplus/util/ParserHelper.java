/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.util;

import java.awt.event.KeyEvent;
import java.text.NumberFormat;

import com.ddfplus.db.SymbolInfo;

/**
 * The MessageParser class is the central class in the ddf api parsing system.
 * This class contains one key static method: ParseMesasge. This in turn returns
 * a fully parsed Message (subclass) from a given input ddfplus text message.
 */
public final class ParserHelper {

//	private static final Logger LOG = LoggerFactory.getLogger(ParserHelper.class);

	/**
	 * Default Value when there is no data. This is not the same as zero (0),
	 * since zero <I>can</I> be a valid value. This constant is an unlikely
	 * number to appear, and therefore occurs if no value exists.
	 */
	public static final int DDFAPI_NOVALUE = 0;

	/**
	 * Specifies Decimal notation.
	 * 
	 * @see #float2string
	 */
	public static final int DECIMAL = 0;

	public static final int PURE_DECIMAL = 3;

	/**
	 * Specifies Dash notation.
	 * 
	 * @see #float2string
	 */
	public static final int DASH = 1;

	/**
	 * Specifies Integer notation.
	 * 
	 * @see #float2string
	 */
	public static final int INTEGER = 2;

	private static final int[] units_f1 = { 1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000 };
	private static final int[] units_f2 = { 8, 16, 32, 64, 128, 256, 512 };
	private static final int[] units_f3 = { 8, 16, 32, 64, 128, 256, 512 };
	private static final int[] units_f4 = { 10, 100, 100, 100, 1000, 1000, 1000 };

	/**
	 * Converts a floating point value into the same String format that the JERQ
	 * servers use to transmit data. The nuance difference is in the INTEGER
	 * value of certain items, such as the 10-Year note, which is transmitted in
	 * 64ths, even though it is displayed in 32nds and halves.
	 */
	public static int float2int(int basecode, float value) {

		float c = value;
		float rf = (float) 0.25;

		if (c < 0)
			rf = (float) -0.25;

		byte tmp = (byte) basecode;

		// The nice thing about futures units > 0 is that
		// they are all decimal, and you don't have to do
		// much with them.
		if (tmp >= 0) {
			float d = c * units_f1[tmp] + rf;
			return (int) d;
		}

		// Now the pain in the ass stuff
		int i = 0 - basecode - 1;
		int ii = units_f3[i];
		int jj = units_f4[i];

		float dd = ((float) 1.0 / units_f2[i]);
		float t = roundFloat(c, dd);
		int n = (int) t;
		int j = (int) ((t - n) * ii + rf);
		if (j >= ii)
			j = jj;

		return (n * jj + j);
	}

	/**
	 * Converts a floating point value into a String that's suitable for client
	 * display.
	 * <P>
	 * 
	 * @param f
	 *            Floating point value
	 * @param basecode
	 *            Base Code (a.k.a. unit code)
	 * @param target
	 *            The Target Format
	 *            <P>
	 *            <B>Target is one of the following constants:</B><BR>
	 *            <ul>
	 *            <LI>#DECIMAL - Decimal Notation
	 *            <LI>#DASH - Dash Notation
	 *            <LI>#INTEGER - Integer (like dash but no dashes) notation
	 *            </ul>
	 */
	public static final String float2string(float f, char basecode, int target) {
		return float2string(f, basecode, target, true);
	}

	private final static NumberFormat NF = NumberFormat.getInstance();

	/*
	 * Convert a float to a DDF string.
	 * 
	 * TODO Possible optimizations here.
	 */
	public synchronized static final String float2string(double value, final char basecode, final int target,
			final boolean special64) {

		final int unit = SymbolInfo.ddfuc2bb(basecode);

		if (unit >= 0) {
			if (target == PURE_DECIMAL)
				NF.setGroupingUsed(false);
			else
				NF.setGroupingUsed(true);

			NF.setMinimumFractionDigits(unit);
			NF.setMaximumFractionDigits(unit);

			String s = NF.format(value);

			if (target == INTEGER) {
				char ca[] = s.toCharArray();
				int st = 0;

				s = "";

				for (int i = 0; i < ca.length; i++) {
					if ((i == 0) && (ca[i] == '+') || (ca[i] == '-')) // A
						// leading
						// sign
						st++;
					else if ((ca[i] == '0') || (ca[i] == '.')) // Strip all
						// zeros from
						// the left
						st++;
					else if (Character.isDigit(ca[i]))
						break;
				}

				for (int i = st; i < ca.length; i++) {
					if (Character.isDigit(ca[i]))
						s = s + ca[i];
				}

				if ((value == 0.0f) && (s.length() < 1))
					s = "0";

				if (value < 0)
					s = "-" + s;

				return s;
			} else
				return s;
		}

		if ((target == DECIMAL) || (target == PURE_DECIMAL)) {
			if (target == PURE_DECIMAL)
				NF.setGroupingUsed(false);
			else
				NF.setGroupingUsed(true);

			switch (unit) {
			case -1:
				NF.setMinimumFractionDigits(3);
				NF.setMaximumFractionDigits(3);
				break;
			case -2:
				NF.setMinimumFractionDigits(4);
				NF.setMaximumFractionDigits(4);
				break;
			case -3:
				NF.setMinimumFractionDigits(5);
				NF.setMaximumFractionDigits(5);
				break;
			case -4:
				NF.setMinimumFractionDigits(6);
				NF.setMaximumFractionDigits(6);
				break;
			case -5:
				NF.setMinimumFractionDigits(7);
				NF.setMaximumFractionDigits(7);
				break;
			case -6:
				NF.setMinimumFractionDigits(8);
				NF.setMaximumFractionDigits(8);
				break;
			}

			return (NF.format(value));
		}

		int sign = 1;

		if (value < 0.0f)
			sign = -1;

		value = Math.abs(value);

		int iWhole = (int) value;

		String sw = "" + iWhole;

		if (sign == -1)
			sw = "-" + sw;

		String sf = "";

		double div = value - (int) value;
		int digits = 0;

		if (unit == -1) {
			div = div * 8;
			digits = 1;
		} else if (unit == -2) {
			div = div * 16;
			digits = 2;
		} else if (unit == -3) {
			div = div * 32;
			digits = 2;
		} else if (unit == -4) {
			if (special64) {
				// 64ths --> 32nds and halves
				div = div * 320;
				digits = 3;
			} else {
				div = div * 64;
				digits = 2;
			}
		} else if (unit == -5) {
			if (special64) {
				div = div * 320;
				digits = 3;
			} else {
				div = div * 128;
				digits = 3;
			}
		} else if (unit == -6) {
			div = div * 256;
			digits = 3;
		} else {
			div = 0;
			digits = 1;
		}

		sf = "00000000" + (int) div;
		sf = sf.substring(sf.length() - digits, sf.length());

		if (target == INTEGER) {
			if (iWhole != 0)
				return (sw + sf);
			else {
				char c = sw.charAt(0);
				if (c == '-')
					return c + sf;
				else
					return sf;
			}
		} else
			return sw + "-" + sf;
	}

	/**
	 * Specialized method used to round a float to specified number of digits.
	 * Uses casts to eliminate floating point irregularities.
	 */

	public static float roundFloat(float r, float rinc) {
		float t = rinc;
		if (r < 0)
			t = -rinc;
		t = (r + t / (float) 2.0) / rinc;
		int i = (int) t;
		float d = rinc * i;
		return d;
	}

	/**
	 * Converts a string representation of the data to a floating point value,
	 * based on the unitcode.
	 */

	public static float string2float(String value, char unitcode) throws NumberFormatException {
		return string2float(value, SymbolInfo.ddfuc2bb(unitcode));
	}

	/**
	 * Converts a string representation of the data to a floating point value,
	 * based on the unitcode.
	 */

	public static float string2float(String value, int basecode) throws NumberFormatException {
		if (value.length() == 0)
			return DDFAPI_NOVALUE;

		if (value.equals("-"))
			return 0;

		int ival = Integer.parseInt(value);
		float f = 0;

		if (basecode >= 0) {
			f = (ival / (float) Math.pow(10, basecode));
		} else {
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
		}

		return f;
	}

	/**
	 * A unified way of converting string values to integer values.
	 */

	public static int string2int(String value) {
		int i = DDFAPI_NOVALUE;
		if (value.length() == 0)
			return DDFAPI_NOVALUE;

		try {
			i = Integer.parseInt(value);
		} catch (Exception e) {
			;
		}

		return i;
	}

	public static boolean isPrintableChar(final char c) {
		final Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
		return (!Character.isISOControl(c)) && c != KeyEvent.CHAR_UNDEFINED && block != null
				&& block != Character.UnicodeBlock.SPECIALS;
	}

	public final static char filterNullChar(final char in) {
		if (in == ASCII.NUL) {
			return '?';
		} else {
			return in;
		}
	}

	public final static String toAsciiString(final byte bytes[]) {
		int size = bytes.length;
		char[] charArray = new char[size];
		for (int k = 0; k < size; k++) {
			char c = (char) bytes[k];
			if (isPrintableChar(c)) {
				charArray[k] = c;
			} else {
				charArray[k] = ASCII.SPACE;
			}
		}
		return new String(charArray);
	}

	public final static String toAsciiString2(final byte bytes[]) {
		int size = bytes.length;
		char[] charArray = new char[size * 2];
		int m = 0;
		for (int k = 0; k < size; k++) {
			char c = (char) bytes[k];
			charArray[m++] = ASCII.SPACE;
			if (isPrintableChar(c)) {
				charArray[m++] = c;
			} else {
				charArray[m++] = ASCII.SPACE;
			}
		}
		return new String(charArray);
	}

	public final static String toHexString(final byte bytes[]) {
		if (bytes == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		int size = bytes.length;
		for (int k = 0; k < size; k++) {
			byte high = (byte) ((bytes[k] & 0xf0) >> 4);
			byte low = (byte) (bytes[k] & 0x0f);
			sb.append(nibble2char(high));
			sb.append(nibble2char(low));
		}
		return sb.toString();
	}

	private final static char nibble2char(final byte b) {
		byte nibble = (byte) (b & 0x0f);
		if (nibble < 10) {
			return (char) ('0' + nibble);
		}
		return (char) ('a' + nibble - 10);
	}
}
