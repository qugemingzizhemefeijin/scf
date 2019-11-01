package cg.zz.scf.client.loadbalance.component;

/**
 * 远程服务器状态
 *
 */
public enum ServerState {
	
	/**
	 * 死亡
	 */
	Dead,
	
	/**
	 * 正常
	 */
	Normal,
	
	/**
	 * 忙碌
	 */
	Busy,
	
	/**
	 * 不可用
	 */
	Disable,
	
	/**
	 * 重启中
	 */
	Reboot,
	
	/**
	 * 测试中
	 */
	Testing;

}
