/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.service.usersettings;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class UserSettingsServiceImpl implements UserSettingsService {

	public static final String DDF_CENTRAL = "http://www.ddfplus.com";

	private final Logger log = LoggerFactory.getLogger(getClass());

	// for testing
	private String queryUrl;

	@Override
	public UserSettings getUserSettings(String userName, String password) {
		UserSettings userSettings = new UserSettings();
		userSettings.setUserName(userName);
		userSettings.setPassword(password);

		// Servers
		String url = getURL(userName, password);
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(url);
			Element userSettingsElement = doc.getDocumentElement();
			// Find servers node
			NodeList childrenList = userSettingsElement.getChildNodes();
			Node serversNode = null;
			if (childrenList != null) {
				for (int i = 0; i < childrenList.getLength(); i++) {
					Node child = childrenList.item(i);
					if (child.getNodeName().equals("servers")) {
						serversNode = child;
						break;
					}
				}
			}
			if (serversNode != null) {
				NodeList serverList = serversNode.getChildNodes();
				if (serverList != null) {
					for (int i = 0; i < serverList.getLength(); i++) {
						Node server = serverList.item(i);
						// We are at usersettings.servers.server
						NamedNodeMap attributes = server.getAttributes();
						if (attributes != null) {
							Node type = attributes.getNamedItem("type");
							if (type.getTextContent().equals("stream")) {
								// We found the stream server
								Node n = attributes.getNamedItem("primary");
								if (n != null) {
									userSettings.setStreamPrimaryServer(n.getTextContent());
								}
								n = attributes.getNamedItem("secondary");
								if (n != null) {
									userSettings.setStreamSecondaryServer(n.getTextContent());
								}
								n = attributes.getNamedItem("recovery");
								if (n != null) {
									userSettings.setRecoveryServer(n.getTextContent());
								}
								n = attributes.getNamedItem("wss");
								if (n != null) {
									userSettings.setWssServer(n.getTextContent());
								}
							}
						}
					}
				}
			}

			log.info("User settings: " + userSettings);

		} catch (ParserConfigurationException e) {
			log.error("User settings parse configuration: " + e.getMessage());
		} catch (SAXException e) {
			log.error("User settings parse: " + e.getMessage());
		} catch (IOException e) {
			log.error("User settings IO issue: " + e.getMessage());
		}

		return userSettings;
	}

	// for testing
	void setQueryUrl(String url) {
		this.queryUrl = url;
	}

	private String getURL(String userName, String password) {
		// for testing
		if (queryUrl != null) {
			return queryUrl;
		}
		return DDF_CENTRAL + "/" + "getUserSettings.php" + "?" + //
				"username=" + userName + "&" + "password=" + password;
	}

}
