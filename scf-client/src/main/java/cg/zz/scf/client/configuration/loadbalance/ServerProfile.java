package cg.zz.scf.client.configuration.loadbalance;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import cg.zz.scf.client.SCFConst;
import cg.zz.scf.client.utility.helper.TimeSpanHelper;

/**
 * 服务地址，端口等配置信息属性
 *
 */
public class ServerProfile {
	
	/**
	 * 服务名称
	 */
	private String name;
	
	/**
	 * 服务IP
	 */
	private String host;
	
	/**
	 * 服务端口
	 */
	private int port;
	
	/**
	 * 僵死的超时时间
	 */
	private int deadTimeout;
	
	/**
	 * 服务权重，如果没有配置，则默认为1
	 */
	private float weithtRate;
	
	/**
	 * 解析&lt;add name="enterprise" host="127.0.0.1" port="19000" maxCurrentUser="100" /&gt;XML节点并构造ServerProfile对象
	 * @param node - Node
	 */
	public ServerProfile(Node node){
		NamedNodeMap attributes = node.getAttributes();
		//服务器名称[此版本名字根本没啥用]
		this.name = attributes.getNamedItem("name").getNodeValue();
		//IP地址
		this.host = attributes.getNamedItem("host").getNodeValue();
		//端口
		this.port = Integer.parseInt(attributes.getNamedItem("port").getNodeValue());
		//权重默认为1
		Node atribute = attributes.getNamedItem("weithtRate");
		if (atribute != null)
			this.weithtRate = Float.parseFloat(atribute.getNodeValue().toString());
		else {
			this.weithtRate = 1.0F;
		}
		//服务器挂后心跳检测间隔时间，没有配置则为60秒。这个deadTimeout是配置在Server节点上的。
		atribute = node.getParentNode().getAttributes().getNamedItem("deadTimeout");
		if (atribute != null) {
			//设置最小值为30s
			int dtime = TimeSpanHelper.getIntFromTimeSpan(atribute.getNodeValue().toString());
			if (dtime < 30000) {
				dtime = 30000;
			}
			this.deadTimeout = dtime;
		} else {
			this.deadTimeout = SCFConst.DEFAULT_DEAD_TIMEOUT;
		}
	}

	public String getName() {
		return name;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public int getDeadTimeout() {
		return deadTimeout;
	}

	public float getWeithtRate() {
		return weithtRate;
	}

}
