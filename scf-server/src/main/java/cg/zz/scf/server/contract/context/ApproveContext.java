package cg.zz.scf.server.contract.context;

/**
 * 权限验证凭证
 * @author chengang
 *
 */
public class ApproveContext {
	
	/**
	 * 是否加密
	 */
	private boolean isRight = false;
	
	/**
	 * 权限数据
	 */
	private String data;

	public ApproveContext() {
		this.data = Global.getInstance().getServiceConfig().getString("scf.server.approve.securedata");
	}

	public boolean isRight() {
		return this.isRight;
	}

	public void setRight(boolean isRight) {
		this.isRight = isRight;
	}

	public String getData() {
		return this.data;
	}

	public void setData(String data) {
		this.data = data;
	}

}
