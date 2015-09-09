/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.util;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The XMLNode class is intended to be a simple XML parser for single line
 * XML-like entries. It is NOT intended to be a full featured XML parser. For
 * that, one should use the XML classes found in javax.xml.
 * 
 * This class was written to be used by the various ddf.db classes for textaul
 * serialization of the data.
 */

public class XMLNode {

	private final Map<String, String> m_Attributes;
	private final List<XMLNode> m_SubNodes;
	private final String m_Name;

	public XMLNode(String name) {
		this.m_Name = name;
		this.m_Attributes = new LinkedHashMap<String, String>();
		this.m_SubNodes = new LinkedList<XMLNode>();
	}

	/**
	 * Adds an XML node to the list of XMLNodes. Each XMLNode can contain
	 * attributes and "sub-nodes", which in turn can also contain attributes and
	 * sub-nodes, etc.
	 * 
	 * @param node
	 *            <code>XMLNode</code>
	 */

	public void addNode(XMLNode node) {
		m_SubNodes.add(node);
	}

	/**
	 * Retrieves the name of the node. The name is XML element name.
	 */

	public String getName() {
		return m_Name;
	}

	/**
	 * Retrieves all of the sub nodes associated with this node with a specified
	 * element name.
	 * 
	 * @param name
	 *            <code>String</code>
	 */

	public List<XMLNode> getAllNodes(String name) {

		List<XMLNode> list = new LinkedList<XMLNode>();

		for (XMLNode n : m_SubNodes) {
			if (n.getName().equals(name)) {
				list.add(n);
			}

		}

		return list;

	}

	/**
	 * Retrieves the value of a specified attribute. This method will return
	 * null if the attribute was not provided.
	 * 
	 * @param name
	 *            - The name of the attribute
	 */

	public String getAttribute(String name) {
		return m_Attributes.get(name);
	}

	/**
	 * Sets the value of an attribute.
	 */

	public void setAttribute(String name, String value) {
		m_Attributes.put(name, value);
	}

	/**
	 * Converts this XMLNode to an XML String.
	 */

	public String toXMLString() {

		StringBuilder text = new StringBuilder();

		text.append("<" + m_Name);

		for (String key : m_Attributes.keySet()) {

			String value = m_Attributes.get(key);

			if (value == null) {

				text.append(" " + key);

			} else {

				text.append(" " + key + "=\"" + ConvertTextToXml(value) + "\"");

			}

		}

		if (m_SubNodes.size() > 0) {

			text.append(">");

			for (XMLNode n : m_SubNodes) {
				text.append(n.toXMLString());
			}

			text.append("</" + m_Name + ">");

		} else {

			text.append("/>");

		}

		return text.toString();

	}

	/*
	 * Converts the text to be XML complaint, e.g. " becomes &quot; and &
	 * becomes &amp;
	 */
	public static String ConvertTextToXml(String s) {
		if (s.indexOf("&") > -1)
			s = s.replaceAll("\\&", "&amp;");
		if (s.indexOf("\"") > -1)
			s = s.replaceAll("\\\"", "&quot;");
		return s;
	}

	public static String htmlDecode(String s) {
		s = s.replaceAll("\\&amp;", "&");
		s = s.replaceAll("\\&quot;", "\"");
		s = s.replaceAll("\\&nbsp;", " ");
		return s;
	}

	/**
	 * Parses an individual tag for the name and attributes. This will not parse
	 * any interior element data.
	 */

	private static XMLNode parseTag(String tag, XMLNode node) {
		char[] ca = tag.toCharArray();
		int pos = 0;
		for (int i = 0; i < ca.length; i++) {
			if ((node == null) && ((ca[i] == ' ') || (i == ca.length - 1))) {
				node = new XMLNode(new String(ca, 0, i + 1).trim());
				pos = i + 1;
			} else if (((ca[i] == ' ') && ca[i - 1] == '"') || (i == ca.length - 1)) {
				String attrib = new String(ca, pos, i - pos + 1).trim();
				int pos2 = attrib.indexOf('=');
				if (pos2 > -1) {
					String key = attrib.substring(0, pos2);
					String val = attrib.substring(pos2 + 1, attrib.length());
					if (val.startsWith("\""))
						val = val.substring(1, val.length());
					if (val.endsWith("\""))
						val = val.substring(0, val.length() - 1);
					node.m_Attributes.put(key, val);
				} else
					node.m_Attributes.put(attrib, null);

				pos = i + 1;
			}
		}

		return node;
	}

	/**
	 * Parses an XML string into an XMLNode. This method is recursive, and will
	 * create any subnodes that may be included in the input.
	 */

	public static XMLNode parse(final String xml) {

		String s1 = "";

		String s2 = "";

		if (xml.endsWith("/>")) {
			s1 = xml.substring(1, xml.length() - 2);
		} else {
			int pos = xml.indexOf(">");

			s1 = xml.substring(1, pos);
			s2 = xml.substring(pos + 1).trim();
			pos = s2.lastIndexOf("</");
			s2 = s2.substring(0, pos);
		}

		XMLNode node = parseTag(s1.trim(), null);

		char[] ca = s2.toCharArray();
		int iLevel = 0;

		StringBuilder tmp = new StringBuilder();
		boolean b = false;
		for (int i = 0; i < ca.length; i++) {
			tmp.append(ca[i]);
			if ((ca[i] == '<') && (ca[i + 1] != '/'))
				iLevel++;
			else if ((ca[i] == '<') && (ca[i + 1] == '/')) {
				b = true;
			} else if (ca[i] == '>') {
				if (ca[i - 1] == '/')
					iLevel--;
				else if (b) {
					b = false;
					iLevel--;
				}

				if (iLevel == 0) {
					node.m_SubNodes.add(XMLNode.parse(tmp.toString()));
					tmp = new StringBuilder();
				}
			}
		}

		return node;

	}

	@Override
	public String toString() {
		return toXMLString();
	}

}
