/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.net;

public class Cmd {
	private String cmd;
	private String symbol;
	private String suffix;

	public Cmd(String cmd) {
		this.cmd = cmd;
	}

	public Cmd(String cmd, String symbol) {
		this(cmd, symbol, null);
	}

	public Cmd(String cmd, String symbol, String suffix) {
		this(cmd);
		this.symbol = symbol;
		this.suffix = suffix;
	}

	public String getCmd() {
		return cmd;
	}

	public String getSuffix() {
		return suffix;
	}

	public String getSymbol() {
		return symbol;
	}

	@Override
	public String toString() {
		return cmd + " " + symbol + " " + suffix;
	}
}
