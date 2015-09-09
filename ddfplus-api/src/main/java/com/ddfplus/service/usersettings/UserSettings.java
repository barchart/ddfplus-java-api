/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.service.usersettings;

/**
 * Contains a users Barchart profile.
 * 
 */
public class UserSettings {

	private String userName;

	private String password;

	private String streamPrimaryServer;

	private String streamSecondaryServer;

	private String recoveryServer;

	private String wssServer;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getStreamPrimaryServer() {
		return streamPrimaryServer;
	}

	public void setStreamPrimaryServer(String streamPrimaryServer) {
		this.streamPrimaryServer = streamPrimaryServer;
	}

	public String getRecoveryServer() {
		return recoveryServer;
	}

	public void setRecoveryServer(String server) {
		this.recoveryServer = server;
	}

	@Override
	public String toString() {
		return "user: " + userName + " primary: " + streamPrimaryServer + " secondary: " + streamSecondaryServer
				+ " recovery: " + recoveryServer + " wss: " + wssServer;
	}

	public String getStreamSecondaryServer() {
		return this.streamSecondaryServer;
	}

	public void setStreamSecondaryServer(String s) {
		this.streamSecondaryServer = s;
	}

	public void setWSSServer(String s) {
		this.wssServer = s;

	}

	public String getWSSServer() {
		return this.wssServer;

	}

}
