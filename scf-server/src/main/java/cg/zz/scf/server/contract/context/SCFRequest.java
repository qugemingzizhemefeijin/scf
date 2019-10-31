package cg.zz.scf.server.contract.context;

import cg.zz.scf.protocol.sfp.v1.Protocol;

/**
 * SCF Request
 * @author chengang
 *
 */
public class SCFRequest {
	
	/**
	 * 协议对象
	 */
	private Protocol protocol;
	
	/**
	 * 读取的信息
	 */
	private byte[] requestBuffer;
	
	public SCFRequest() {
		
	}
	
	public SCFRequest(Protocol protocol, byte[] buf) {
		this.protocol = protocol;
		this.requestBuffer = buf;
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}

	public byte[] getRequestBuffer() {
		return requestBuffer;
	}

	public void setRequestBuffer(byte[] requestBuffer) {
		this.requestBuffer = requestBuffer;
	}

}
