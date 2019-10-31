package cg.zz.scf.server.core.conver;

import cg.zz.scf.protocol.sfp.enumeration.SerializeType;
import cg.zz.scf.protocol.sfp.v1.Protocol;
import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;

/**
 * 序列化解析工厂类
 * @author chengang
 *
 */
public class ConvertFacotry {
	
	private static ILog logger = LogFactory.getLogger(ConvertFacotry.class);
	
	/**
	 * json
	 */
	private static JsonConvert jsonConvert = new JsonConvert();

	/**
	 * java
	 */
	private static JavaConvert javaConvert = new JavaConvert();

	/**
	 * SCFBinary
	 */
	private static SCFBinaryConvert scfBinaryConvert = new SCFBinaryConvert();
	
	/**
	 * 根据协议获取指定的类型序列化解析器
	 * @param p - Protocol
	 * @return IConvert
	 */
	public static IConvert getConvert(Protocol p) {
		if (p.getSerializeType() == SerializeType.SCFBinary)
			return scfBinaryConvert;
		if (p.getSerializeType() == SerializeType.JAVABinary)
			return javaConvert;
		if (p.getSerializeType() == SerializeType.JSON)
			return jsonConvert;
		if (p.getSerializeType() == SerializeType.SCFBinaryV2) {
			return scfBinaryConvert;
		}

		logger.error("can't get IConvert not : json ,java, customBinary ");
		return null;
	}

}
