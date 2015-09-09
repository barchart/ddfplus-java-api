/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.enums;

public enum ConnectionType {

	/**
	 * Mode constant for UDP connections. The JerqClient will assume the default
	 * ddfplus UDP port of 7600. UDP connections are streaming connections. In
	 * general, most corporate client firewalls will block UDP connections.
	 */
	/**
	 * Internet based (client/server) UDP connectivity.
	 */
	UDP(1, 7600), //

	/**
	 * Mode constant for TCP connections. The JerqClient will assume the default
	 * ddfplus TCP port of 7500. TCP connections are streaming connections.
	 * Depending on the client's network configuration, these connections may be
	 * blocked by networks with NAT (Network Address Translation) and / or
	 * firewalls.
	 */
	/**
	 * Internet based (client/server) TCP connectivity.
	 */
	TCP(2, 7500), //

	/**
	 * Mode constant for HTTP refreshing connections. The JerqClient will assume
	 * the default ddfplus HTTP port of 80. In general, HTTP refreshing
	 * connections will not get blocked by firewalls, but they do not provide
	 * streaming data.
	 */
	/**
	 * Internet / Web based (client/server) HTTP connectivity. Note that this
	 * method is a last-resort, since it makes refreshing requests to the web
	 * services, instead of streaming message for message data.
	 */
	HTTP(3, 80), //

	/**
	 * Mode constant for streaming data over HTTP. The JerqClient will assume
	 * the default ddfplus HTTP port of 80. In general, the streaming over HTTP
	 * method works with most firewalls and NAT proxies. However, some proxies
	 * ignore the server's request to not buffer connection data, and as such,
	 * this method may not function properly.
	 */
	/**
	 * Internet / Web based (client/server) streaming HTTP connectivity. This
	 * method is somewhere between INET_TCP and the INET_HTTP in that it
	 * attempts to stream data over the web port (80) using multipart mixed
	 * content. While this works for most firewalls and proxies, some proxies
	 * ignore the requests and respomses sent by the server, and cache data
	 * before releasing it to the client. This produces the effect that no
	 * messages will come in for minutes, until a huge burst appears.
	 */
	HTTPSTREAM(4, 80), //

	/**
	 * One way (could be Internet driven) broadcast-only traffic to the client
	 * over UDP. This is for server applications directly linked to the ddf plus
	 * network.
	 */
	LISTEN_UDP(11, 7600), //

	/**
	 * One way (could be Internet driven) broadcast-only traffic to the client
	 * over TCP. This is for server applications directly linked to the ddf plus
	 * network.
	 */
	LISTEN_TCP(12, 80),

	/**
	 * Web Socket Support.
	 */
	WSS(13, 80);

	public final int code;

	public final int port;

	private ConnectionType(final int code, final int port) {
		this.code = code;
		this.port = port;
	}

	/** NOTE: will return default (TCP) if no match */
	public final static ConnectionType of(final String typeName) {
		for (final ConnectionType known : values()) {
			if (known.name().equalsIgnoreCase(typeName)) {
				return known;
			}
		}
		return TCP;
	}

}
