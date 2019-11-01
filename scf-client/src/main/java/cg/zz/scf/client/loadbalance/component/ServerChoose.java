package cg.zz.scf.client.loadbalance.component;

/**
 * 记录接口和方法参数等与多少个服务相关联。在ServiceProxy.setServer方法中会创建
 * @author chengang
 *
 */
public class ServerChoose {
	
	/**
	 * 服务数量
	 */
	private int serviceCount;
	
	/**
	 * 服务名称集合
	 */
	private String[] serverName;

	/**
	 * 构造ServerChoose对象，设置服务集合
	 * @param serviceCount - 服务数量
	 * @param serverName - 服务集合
	 */
	public ServerChoose(int serviceCount, String[] serverName) {
		//这个数量直接为serverName.length即可，跟本不需要传入
		this.serverName = serverName;
		this.serviceCount = serviceCount;
	}

	/**
	 * 获取服务数量
	 * @return int
	 */
	public int getServiceCount() {
		return this.serviceCount;
	}

	/**
	 * 设置服务数量
	 * @param serviceCount - int
	 */
	public void setServiceCount(int serviceCount) {
		this.serviceCount = serviceCount;
	}

	/**
	 * 获取服务集合
	 * @return String[]
	 */
	public String[] getServerName() {
		return this.serverName;
	}

	/**
	 * 设置服务集合
	 * @param serverName - String[]
	 */
	public void setServerName(String[] serverName) {
		this.serverName = serverName;
	}

}
