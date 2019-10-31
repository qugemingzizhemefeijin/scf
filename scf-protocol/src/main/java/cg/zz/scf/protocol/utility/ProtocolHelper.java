package cg.zz.scf.protocol.utility;

import cg.zz.scf.protocol.sfp.v1.Protocol;

public class ProtocolHelper {
	
	/**
	 * 获得版本号
	 * @param buffer - byte[]
	 * @return int
	 */
	public static int getVersion(byte[] buffer) {
		return buffer[0];
	}
	
	/**
	 * 读取协议信息并且组装Protocol对象
	 * @param buffer - byte[]
	 * @return Object
	 * @throws Exception
	 */
	public static Object fromBytes(byte[] buffer) throws Exception {
		if (buffer != null && buffer.length > 0) {
			int version = buffer[0];
			if (version == 1) {
				return Protocol.fromBytes(buffer);
			}
		}

		throw new Exception("不完整的二进制流");
	}

	/**
	 * 检查头是否是指定的头
	 * @param buf - byte[]
	 * @return true or false
	 */
	public static boolean checkHeadDelimiter(byte[] buf) {
		if (buf.length == ProtocolConst.P_START_TAG.length) {
			for (int i = 0; i < buf.length; i++) {
				if (buf[i] != ProtocolConst.P_START_TAG[i]) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

}
