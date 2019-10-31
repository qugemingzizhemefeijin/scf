package cg.zz.scf.protocol.sdp;

import cg.zz.scf.serializer.component.annotation.SCFMember;
import cg.zz.scf.serializer.component.annotation.SCFSerializable;

/**
 * 重置协议
 * @author chengang
 *
 */
@SCFSerializable(name="ResetProtocol")
public class ResetProtocol {
	
	/**
	 * 消息内容
	 */
	@SCFMember(sortId=1)
	private String msg;

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}
