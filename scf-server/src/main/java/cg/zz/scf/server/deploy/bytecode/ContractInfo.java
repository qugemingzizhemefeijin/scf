package cg.zz.scf.server.deploy.bytecode;

import java.util.List;

/**
 * 接口信息类
 * @author chengang
 *
 */
public class ContractInfo {
	
	/**
	 * sessionBeanList里面存储的是接口所有的类元信息
	 */
	private List<SessionBean> sessionBeanList;
	
	public ContractInfo() {
		
	}
	
	public ContractInfo(List<SessionBean> sessionBeanList) {
		this.sessionBeanList = sessionBeanList;
	}

	public List<SessionBean> getSessionBeanList() {
		return sessionBeanList;
	}

	public void setSessionBeanList(List<SessionBean> sessionBeanList) {
		this.sessionBeanList = sessionBeanList;
	}

}
