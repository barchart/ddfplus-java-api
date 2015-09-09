/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.messages;

/**
 */
public interface DdfControlResponse extends DdfMessageBase {

	/**
	 * Gets the code.
	 * 
	 * @return the code
	 */
	char getCode();

	/**
	 * Gets the comment.
	 * 
	 * @return the comment
	 */
	String getComment();

}
