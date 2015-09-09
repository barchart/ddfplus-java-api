/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.net;

/**
 * Provides symbol subscriptions to communications implementations that do not
 * support dynamic subscriptions.
 */
public interface SymbolProvider {

	/**
	 * Returns subscription symbols.
	 * 
	 * @return Comma separated symbols.
	 */
	String getSymbols();

	/**
	 * Set the subscription symbols.
	 * 
	 * @param symbols
	 *            Subscription Symbols
	 */
	void setSymbols(String symbols);
}
