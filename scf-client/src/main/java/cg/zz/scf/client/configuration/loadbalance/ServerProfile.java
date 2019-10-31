package cg.zz.scf.client.configuration.loadbalance;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import cg.zz.scf.client.SCFConst;
import cg.zz.scf.client.utility.helper.TimeSpanHelper;

/**
 * 服务配置
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
	 * 比重率
	 */
	private float weithtRate;
	
	public ServerProfile(Node node){
		NamedNodeMap attributes = node.getAttributes();
		this.name = attributes.getNamedItem("name").getNodeValue();
		this.host = attributes.getNamedItem("host").getNodeValue();
		this.port = Integer.parseInt(attributes.getNamedItem("port").getNodeValue());
		Node atribute = attributes.getNamedItem("weithtRate");
		if (atribute != null)
			this.weithtRate = Float.parseFloat(atribute.getNodeValue().toString());
		else {
			this.weithtRate = 1.0F;
		}
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
