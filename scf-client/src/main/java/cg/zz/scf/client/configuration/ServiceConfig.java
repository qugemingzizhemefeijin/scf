package cg.zz.scf.client.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cg.zz.scf.client.SCFConst;
import cg.zz.scf.client.configuration.commmunication.ProtocolProfile;
import cg.zz.scf.client.configuration.commmunication.SocketPoolProfile;
import cg.zz.scf.client.configuration.loadbalance.ServerProfile;
import cg.zz.scf.client.configuration.secure.ApproveProfile;
import cg.zz.scf.client.configuration.secure.KeyProfile;
import cg.zz.scf.client.utility.helper.XMLHelper;

/**
 * 服务配置信息
 * @author chengang
 *
 */
public class ServiceConfig {
	
	/**
	 * 服务名称
	 */
	private String servicename;
	
	/**
	 * 服务ID
	 */
	private int serviceid;
	
	/**
	 * 连接池配置信息
	 */
	private SocketPoolProfile SocketPool;
	
	/**
	 * 协议配置信息
	 */
	private ProtocolProfile protocol;
	
	/**
	 * 服务包含的服务端地址信息
	 */
	private List<ServerProfile> servers;
	
	/**
	 * DES公私钥配置
	 */
	private KeyProfile SecureKey;
	
	/**
	 * 字符密码配置，校验&lt;Approve&gt;中&lt;keyinfo&gt;的&lt;key&gt;字段中info属性跟服务端是否一致
	 */
	private ApproveProfile approveKey;
	
	public static ServiceConfig GetConfig(String serviceName) throws Exception {
		File f = new File(SCFConst.CONFIG_PATH);
		if (!f.exists()) {
			throw new Exception("scf.config not fond:" + SCFConst.CONFIG_PATH);
		}
		
		Element xmlDoc = XMLHelper.GetXmlDoc(SCFConst.CONFIG_PATH);
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		Node serviceNode = (Node) xpath.evaluate("//Service[@name='" + serviceName + "']", xmlDoc,XPathConstants.NODE);
		if (serviceNode == null) {
			printExceprion(0, serviceName);
		}
		
		ServiceConfig config = new ServiceConfig();
		//服务名称和ID
		config.servicename = serviceNode.getAttributes().getNamedItem("name").getNodeValue();
		Node idNode = serviceNode.getAttributes().getNamedItem("id");
		if (idNode == null) {
			printExceprion(4, serviceName);
		}
		config.serviceid = Integer.parseInt(idNode.getNodeValue());
		//连接池
		Node xnSocketPool = (Node) xpath.evaluate("Commmunication/SocketPool", serviceNode, XPathConstants.NODE);
		if (xnSocketPool == null) {
			printExceprion(1, serviceName);
		}
		config.SocketPool = new SocketPoolProfile(xnSocketPool);
		//协议
		Node xnProtocol = (Node) xpath.evaluate("Commmunication/Protocol", serviceNode, XPathConstants.NODE);
		if (xnProtocol == null) {
			printExceprion(2, serviceName);
		}
		config.protocol = new ProtocolProfile(xnProtocol);
		
		//DES密钥
		Node xnKey = (Node) xpath.evaluate("Secure/Key", serviceNode, XPathConstants.NODE);
		config.SecureKey = new KeyProfile(xnKey);

		//字符密钥
		Node apKey = (Node) xpath.evaluate("Approve/key", serviceNode, XPathConstants.NODE);
		config.approveKey = new ApproveProfile(apKey);

		//服务地址信息
		NodeList xnServers = (NodeList) xpath.evaluate("Loadbalance/Server/add", serviceNode,XPathConstants.NODESET);
		if ((xnServers == null) || (xnServers.getLength() == 0)) {
			printExceprion(3, serviceName);
		}

		List<ServerProfile> servers = new ArrayList<>();
		for (int i = 0; i < xnServers.getLength(); i++) {
			servers.add(new ServerProfile(xnServers.item(i)));
		}
		config.servers = servers;
		config.servicename = serviceName;
		return config;
	}
	
	/**
	 * 获得所有的服务名称
	 * @return List<String>
	 * @throws Exception
	 */
	public static List<String> getServiceName() throws Exception {
		File f = new File(SCFConst.CONFIG_PATH);
		if (!f.exists()) {
			throw new Exception("scf.config not fond:" + SCFConst.CONFIG_PATH);
		}

		Element xmlDoc = XMLHelper.GetXmlDoc(SCFConst.CONFIG_PATH);
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		NodeList nodeList = (NodeList) xpath.evaluate("//Service", xmlDoc, XPathConstants.NODESET);
		List<String> services = new ArrayList<>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			services.add(nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue());
		}
		return services;
	}
	
	/**
	 * 获得服务的节点列表
	 * @param serviceName - 服务名称
	 * @return List<String>
	 * @throws Exception
	 */
	public static List<String> getServerNode(String serviceName) throws Exception {
		File f = new File(SCFConst.CONFIG_PATH);
		if (!f.exists()) {
			throw new Exception("scf.config not fond:" + SCFConst.CONFIG_PATH);
		}
		
		Element xmlDoc = XMLHelper.GetXmlDoc(SCFConst.CONFIG_PATH);
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		Node serviceNode = (Node) xpath.evaluate("//Service[@name='" + serviceName + "']", xmlDoc, XPathConstants.NODE);
		if (serviceNode == null) {
			printExceprion(0, serviceName);
		}
		NodeList xnServers = (NodeList) xpath.evaluate("Loadbalance/Server/add", serviceNode, XPathConstants.NODESET);
		if ((xnServers == null) || (xnServers.getLength() == 0)) {
			printExceprion(3, serviceName);
		}

		List<String> servers = new ArrayList<>();
		for (int i = 0; i < xnServers.getLength(); i++) {
			servers.add(xnServers.item(i).getAttributes().getNamedItem("name").getNodeValue());
		}
		return servers;
	}
	
	/**
	 * 抛出对应的异常信息
	 * @param i - 信息类型
	 * @param serviceName - 服务名称
	 * @throws Exception
	 */
	private static void printExceprion(int i, String serviceName) throws Exception {
		switch (i) {
			case 0:
				throw new Exception("scf.config中没有发现" + serviceName + "服务节点!");
			case 1:
				throw new Exception("scf.config服务节点" + serviceName + "没有发现Commmunication/SocketPool配置!");
			case 2:
				throw new Exception("scf.config服务节点" + serviceName + "没有发现Commmunication/Protocol配置!");
			case 3:
				throw new Exception("scf.config服务节点" + serviceName + "没有发现Loadbalance/Server/add配置!");
			case 4:
				throw new Exception("scf.config服务节点" + serviceName + "没有发现Service/id配置!");
		}
	}
	
	public KeyProfile getSecureKey() {
		return this.SecureKey;
	}

	public void setSecureKey(KeyProfile secureKey) {
		this.SecureKey = secureKey;
	}

	public ApproveProfile getApproveKey() {
		return this.approveKey;
	}

	public void setApproveKey(ApproveProfile approveKey) {
		this.approveKey = approveKey;
	}

	public SocketPoolProfile getSocketPool() {
		return this.SocketPool;
	}

	public ProtocolProfile getProtocol() {
		return this.protocol;
	}

	public List<ServerProfile> getServers() {
		return this.servers;
	}

	public int getServiceid() {
		return this.serviceid;
	}

	public String getServicename() {
		return this.servicename;
	}

}
