/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.messages;

/**
 * record 2, subrec 0
 *
 */
public interface DdfMarketParameter extends DdfMarketBase {

	/**
	 * Gets the element.
	 * 
	 * @return The Element code
	 */

	char getElement();

	/**
	 * Gets the modifier.
	 * 
	 * @return The Element Modifier code
	 */

	char getModifier();

	/**
	 * Gets the value.
	 * 
	 * @return The value as an Object
	 */

	Number getValue();

	/**
	 * Gets the value as float.
	 * 
	 * @return The value as a floating point
	 */

	float getValueAsFloat();

	/**
	 * Gets the value as integer.
	 * 
	 * @return The value as an integer
	 */

	int getValueAsInteger();

}