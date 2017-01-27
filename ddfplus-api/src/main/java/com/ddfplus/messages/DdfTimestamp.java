/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.messages;

/**
 * DDF Timestamp
 */
public interface DdfTimestamp extends DdfMessageBase {

	/**
	 * Returns the time data in parts, as an <code>int[]</code>.<BR>
	 * <p>
	 * int[0] = year<BR>
	 * int[1] = month<BR>
	 * int[2] = day<BR>
	 * int[3] = hour<BR>
	 * int[4] = minute<BR>
	 * int[5] = second<BR>
	 * </p>
	 * 
	 * @return array of time data
	 */

	int[] getTimeInParts();

}