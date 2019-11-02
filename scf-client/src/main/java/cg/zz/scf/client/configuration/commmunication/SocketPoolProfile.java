package cg.zz.scf.client.configuration.commmunication;

import org.w3c.dom.Node;

import cg.zz.scf.client.SCFConst;
import cg.zz.scf.client.utility.helper.TimeSpanHelper;

/**
 * socket连接池配置信息<br/>
 * <br/>
 * bufferSize:网络传输流缓存大小<br/>
 * minPoolSize:连接池中最小连接数量<br/>
 * maxPoolSize:连接池中最大连接数量<br/>
 * nagle:是否启用Nagle算法<br/>
 * autoShrink:是否启动连接自动回收<br/>
 * sendTimeout:发送超时<br/>
 * receiveTimeout:接收超时<br/>
 * waitTimeout:当连接池中没有连接时需要等待释放连接的时间<br/>
 * 
 * &lt;SocketPool bufferSize="4096" minPoolSize="3" maxPoolSize="100" nagle="true" autoShrink="00:00:20" sendTimeout="00:00:05" receiveTimeout="00:00:10" waitTimeout="00:00:03" /&gt;
 *
 */
public class SocketPoolProfile {
	
	/**
	 * 最小连接池大小
	 */
	private int minPoolSize;
	
	/**
	 * 最大连接池大小
	 */
	private int maxPoolSize;
	
	/**
	 * 发送数据超时时间
	 */
	private int sendTimeout;
	
	/**
	 * 接收数据超时时间
	 */
	private int receiveTimeout;
	
	/**
	 * 等待超时时间
	 */
	private int waitTimeout;
	
	/**
	 * 是否启用Nagle算法
	 */
	private boolean nagle;
	
	/**
	 * 连接自动回收的时间
	 */
	private int shrinkInterval;
	
	/**
	 * 缓冲区大小
	 */
	private int bufferSize;
	
	/**
	 * 连接超时时间
	 */
	private int connectionTimeout = 3000;
	
	/**
	 * 最大的包大小
	 */
	private int maxPakageSize;
	
	/**
	 * 当前服务是受保护的，默认true。如果为true，socket读取数据超时时间是动态变化的（负载越大超时时间越小），否则是一个恒定值。
	 */
	private boolean _protected;
	
	/**
	 * 请求重试的超时时间
	 */
	private int reconnectTime = 0;
	
	/**
	 * 接收缓冲大小
	 */
	private int recvBufferSize;
	
	/**
	 * 发送的缓冲大小
	 */
	private int sendBufferSize;
	
	public SocketPoolProfile(Node node) {
		this.minPoolSize = Integer.parseInt(node.getAttributes().getNamedItem("minPoolSize").getNodeValue());
		this.maxPoolSize = Integer.parseInt(node.getAttributes().getNamedItem("maxPoolSize").getNodeValue());
		this.sendTimeout = TimeSpanHelper.getIntFromTimeSpan(node.getAttributes().getNamedItem("sendTimeout").getNodeValue().toString());

		this.receiveTimeout = TimeSpanHelper.getIntFromTimeMsSpan(node.getAttributes().getNamedItem("receiveTimeout").getNodeValue().toString());
		this.waitTimeout = TimeSpanHelper.getIntFromTimeSpan(node.getAttributes().getNamedItem("waitTimeout").getNodeValue().toString());
		this.nagle = Boolean.parseBoolean(node.getAttributes().getNamedItem("nagle").getNodeValue().toString());
		this.shrinkInterval = TimeSpanHelper.getIntFromTimeSpan(node.getAttributes().getNamedItem("autoShrink").getNodeValue().toString());
		this.bufferSize = Integer.parseInt(node.getAttributes().getNamedItem("bufferSize").getNodeValue());
		if (this.bufferSize < SCFConst.DEFAULT_BUFFER_SIZE) {
			this.bufferSize = SCFConst.DEFAULT_BUFFER_SIZE;
		}
		Node nProtected = node.getAttributes().getNamedItem("protected");
		if (nProtected == null)
			this._protected = true;
		else {
			this._protected = Boolean.parseBoolean(nProtected.getNodeValue());
		}
		Node nPackage = node.getAttributes().getNamedItem("maxPakageSize");
		if (nPackage == null)
			this.maxPakageSize = SCFConst.DEFAULT_MAX_PAKAGE_SIZE;
		else {
			this.maxPakageSize = Integer.parseInt(nPackage.getNodeValue());
		}

		Node nReconnectTime = node.getAttributes().getNamedItem("reconnectTime");
		if (nReconnectTime != null) {
			this.reconnectTime = Integer.parseInt(nReconnectTime.getNodeValue());
			if (this.reconnectTime < 0) {
				this.reconnectTime = 0;
			}
		}

		this.recvBufferSize = 1024 * 1024 * 8;
		this.sendBufferSize = 1024 * 1024 * 8;
	}

	public int getMinPoolSize() {
		return minPoolSize;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public int getSendTimeout() {
		return sendTimeout;
	}

	public int getReceiveTimeout() {
		return receiveTimeout;
	}

	public int getWaitTimeout() {
		return waitTimeout;
	}

	public boolean isNagle() {
		return nagle;
	}
	
	/**
	 * 是否自动收缩
	 * @return boolean
	 */
	public boolean AutoShrink() {
		return this.shrinkInterval > 0;
	}

	public int getShrinkInterval() {
		return shrinkInterval;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public int getMaxPakageSize() {
		return maxPakageSize;
	}

	public boolean isProtected() {
		return _protected;
	}

	public int getReconnectTime() {
		return reconnectTime;
	}

	public int getRecvBufferSize() {
		return recvBufferSize;
	}

	public int getSendBufferSize() {
		return sendBufferSize;
	}

	public void setRecvBufferSize(int recvBufferSize) {
		this.recvBufferSize = recvBufferSize;
	}

	public void setSendBufferSize(int sendBufferSize) {
		this.sendBufferSize = sendBufferSize;
	}

}
