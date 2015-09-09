/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.net;

public final class NetConstants {

	public static final String DEFAULT_DDF_SERVER = "qs01.ddfplus.com";

	public static final int JERQ_VERSION_DEFAULT = 4;

	public static final int UDP_RECV_PACKET_SIZE = 2 * 1024;

	public static final int UDP_RECV_BUFFER_SIZE = 2048 * 1024;

	public static final int UDP_RECV_TIMEOUT = 30 * 1000;

	public static final int TCP_RECV_BUFFER_SIZE = 128 * 1024;

	public static final int TCP_RECV_TIMEOUT = 30 * 1000;

	private NetConstants() {
	}
}
