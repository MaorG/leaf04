package leaf04.utils;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ParserUtils {
	
	public static String getStringByName(Node node, String name) {
		List<Node> ret = ParserUtils.getNodesByName(node, name);
		if (ret.size() == 0)
			return null;
		return ret.get(0).getTextContent();
	}

	public static Integer getIntByName(Node node, String name) {
		List<Node> ret = ParserUtils.getNodesByName(node, name);
		if (ret.size() == 0)
			return null;
		return Integer.parseInt( ret.get(0).getTextContent());
	}

	public static Double getDoubleByName(Node node, String name) {
		List<Node> ret = ParserUtils.getNodesByName(node, name);
		if (ret.size() == 0)
			return null;
		return Double.parseDouble( ret.get(0).getTextContent());
	}
	
	public static List<Node> getNodesByTagName(Node node, String name) {
		
		
		NodeList nodeList = node.getChildNodes();
		List<Node> ret = new ArrayList<Node>();
		
		for (int i = 0; i < nodeList.getLength(); i++) {
		    switch (nodeList.item(i).getNodeType()) {
		    case Node.ELEMENT_NODE:

		        Element element = (Element) nodeList.item(i);
		        if (element.getNodeName().equalsIgnoreCase(name)) {
		        	ret.add(nodeList.item(i));
		        }
		    }
		}
		
		return ret;
		

	}
	
	public static List<Node> getNodesByName(Node node, String name) {
		
		
		NodeList nodeList = node.getChildNodes();
		List<Node> ret = new ArrayList<Node>();
		
		for (int i = 0; i < nodeList.getLength(); i++) {
		    switch (nodeList.item(i).getNodeType()) {
		    case Node.ELEMENT_NODE:

		        Element element = (Element) nodeList.item(i);
		        if (element.getAttribute("name").equalsIgnoreCase(name)) {
		        	ret.add(nodeList.item(i));
		        }
		    }
		}
		
		return ret;
		

	}

	public static Node getNodeByTagName(Node node, String name) {
		NodeList nodeList = node.getChildNodes();
		
		for (int i = 0; i < nodeList.getLength(); i++) {
		    switch (nodeList.item(i).getNodeType()) {
		    case Node.ELEMENT_NODE:

		        Element element = (Element) nodeList.item(i);
		        if (element.getNodeName().equalsIgnoreCase(name)) {
		        	return nodeList.item(i);
		        }
		    }
		}
		
		return null;
	}

}
