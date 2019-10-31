package cg.zz.scf.protocol.utility;

/**
 * 协议常量
 * @author chengang
 *
 */
public final class ProtocolConst {
	
	/**
	 * 开头信息
	 */
	public static final byte[] P_START_TAG = { 18, 17, 13, 10, 9 };
	
	/**
	 * 结束信息
	 */
	public static final byte[] P_END_TAG = { 9, 10, 13, 17, 18 };

}
