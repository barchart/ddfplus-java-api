/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.net;

public class SymbolProviderImpl implements SymbolProvider {

	private String symbols;

	@Override
	public String getSymbols() {
		return symbols;
	}

	@Override
	public void setSymbols(String symgbols) {
		this.symbols = symgbols;

	}

}
