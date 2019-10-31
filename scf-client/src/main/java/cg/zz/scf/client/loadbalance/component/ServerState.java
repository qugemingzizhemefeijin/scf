package cg.zz.scf.client.loadbalance.component;

/**
 * 服务状态
 *
 */
public enum ServerState {
	
	Dead,
	Normal,
	Busy,
	Disable,
	Reboot,
	Testing;

}
