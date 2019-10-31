package cg.zz.scf.server.contract.context;

/**
 * 密钥Context
 * @author chengang
 *
 */
public class SecureContext {
	
	/**
	 * DES密钥
	 */
	private String desKey;
	
	/**
	 * 服务器端RSA公钥
	 */
	private String serverPublicKey;
	
	/**
	 * 服务器端RSA私钥
	 */
	private String serverPrivateKey;
	
	/**
	 * 客户端RSA公钥
	 */
	private String clientPublicKey;
	
	/**
	 * 客户端RSA私钥
	 */
	private String clientPrivateKey;
	
	/**
	 * 判断请求是否通过了验证
	 */
	private boolean isRights = false;

	public boolean isRights() {
		return this.isRights;
	}

	public void setRights(boolean isRights) {
		this.isRights = isRights;
	}

	public String getDesKey() {
		return this.desKey;
	}

	public void setDesKey(String desKey) {
		this.desKey = desKey;
	}

	public String getServerPublicKey() {
		return this.serverPublicKey;
	}

	public void setServerPublicKey(String serverPublicKey) {
		this.serverPublicKey = serverPublicKey;
	}

	public String getServerPrivateKey() {
		return this.serverPrivateKey;
	}

	public void setServerPrivateKey(String serverPrivateKey) {
		this.serverPrivateKey = serverPrivateKey;
	}

	public String getClientPublicKey() {
		return this.clientPublicKey;
	}

	public void setClientPublicKey(String clientPublicKey) {
		this.clientPublicKey = clientPublicKey;
	}

	public String getClientPrivateKey() {
		return this.clientPrivateKey;
	}

	public void setClientPrivateKey(String clientPrivateKey) {
		this.clientPrivateKey = clientPrivateKey;
	}

}
