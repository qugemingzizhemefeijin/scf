package cg.zz.scf.server.contract.context;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 服务的配置信息
 * @author chengang
 *
 */
public class ServiceConfig {
	
	private static Map<String, String> property = null;
	
	private ServiceConfig() {
		property = new HashMap<String , String>();
	}
	
	public void set(String key, String value) {
		property.put(key, value);
	}
	
	public String getString(String name) {
		return property.get(name);
	}
	
	public int getInt(String name) throws Exception {
		String value = property.get(name);
		if(value == null || value.equals("")) return 0;
		return Integer.parseInt(value);
	}
	
	public boolean getBoolean(String name) throws Exception {
		String value = property.get(name);
		if(value == null || value.equals("")) {
			throw new Exception("the property (" + name + ") is null");
		}
		return Boolean.parseBoolean(value);
	}
	
	public List<String> getList(String name, String split) {
		String value = property.get(name);
		if(value == null || value.equals("")) return null;
		
		List<String> list = new ArrayList<String>();
		String[] values = value.split(split);
		
		if(value != null && value.length() > 0) {
			for (String v : values) {
				list.add(v);
			}
		}
		
		return list;
	}
	
	public static ServiceConfig getServiceConfig(String[] paths) throws Exception {
		ServiceConfig instance = new ServiceConfig();
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression exprProperty = xpath.compile("//configuration/property");
		XPathExpression exprName = xpath.compile("name");
		XPathExpression exprValue = xpath.compile("value");
		
		String[] arrayOfString = paths;
		for (int i = 0; i < paths.length; i++) {
			String p = arrayOfString[i];
			
			File fServiceConfig = new File(p);
			if (!fServiceConfig.exists()) continue;
			
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true);
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			Document doc = builder.parse(p);
			
			NodeList propertyNodes = (NodeList)exprProperty.evaluate(doc, XPathConstants.NODESET);
			for (int j = 0; j < propertyNodes.getLength(); j++) {
				Node node = propertyNodes.item(j);
				Node nameNode = (Node)exprName.evaluate(node, XPathConstants.NODE);
				Node valueNode = (Node)exprValue.evaluate(node, XPathConstants.NODE);
				
				Node append = valueNode.getAttributes().getNamedItem("append");
				if (append != null && append.getNodeValue() != null && append.getNodeValue().equalsIgnoreCase("true")) {
					String key = nameNode.getTextContent();
					String value = property.get(nameNode.getTextContent());
					if (value != null) value = value + "," + valueNode.getTextContent();
					else value = valueNode.getTextContent();
					
					property.put(key, value);
				} else {
					property.put(nameNode.getTextContent(), valueNode.getTextContent());
				}
			}
		}
		
		return instance;
	}
	
	public static void getSecureConfig(String[] paths) throws Exception {
		Global global = Global.getInstance();
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression exprProperty = xpath.compile("//secure");
		XPathExpression exprName = xpath.compile("key");
		XPathExpression exprValue = xpath.compile("method");

		String[] arrayOfString = paths;
		int j = paths.length;
		for (int i = 0; i < j; i++) {
			String p = arrayOfString[i];
			File fServiceConfig = new File(p);
			if (!fServiceConfig.exists()) {
				continue;
			}
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true);
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			Document doc = builder.parse(p);

			NodeList propertyNodes = (NodeList) exprProperty.evaluate(doc, XPathConstants.NODESET);
			for (int x = 0; x < propertyNodes.getLength(); x++) {
				Node node = propertyNodes.item(x);
				Node nameNode = (Node) exprName.evaluate(node, XPathConstants.NODE);
				Node valueNode = (Node) exprValue.evaluate(node, XPathConstants.NODE);

				List<String> list = new ArrayList<String>();
				if (valueNode.getTextContent() != null) {
					list = Arrays.asList(valueNode.getTextContent().split(":"));
				}

				if (nameNode.getTextContent() != null) {
					global.addSecureMap(nameNode.getTextContent(), list);
				}

				list = null;
			}
		}
	}

}
