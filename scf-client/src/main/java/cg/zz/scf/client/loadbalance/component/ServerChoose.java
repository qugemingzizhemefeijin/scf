package cg.zz.scf.client.loadbalance.component;

/**
 * 记录服务的名字和服务的总计数
 * @author chengang
 *
 */
public class ServerChoose {
	
	private int serviceCount;
	private String[] serverName;

	public ServerChoose(int serviceCount, String[] serverName) {
		this.serverName = serverName;
		this.serviceCount = serviceCount;
	}

	public int getServiceCount() {
		return this.serviceCount;
	}

	public void setServiceCount(int serviceCount) {
		this.serviceCount = serviceCount;
	}

	public String[] getServerName() {
		return this.serverName;
	}

	public void setServerName(String[] serverName) {
		this.serverName = serverName;
	}

}
