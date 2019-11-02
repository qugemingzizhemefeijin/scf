package cg.zz.scf.protocol.sfp.enumeration;

import cg.zz.scf.protocol.sdp.ExceptionProtocol;
import cg.zz.scf.protocol.sdp.HandclaspProtocol;
import cg.zz.scf.protocol.sdp.RequestProtocol;
import cg.zz.scf.protocol.sdp.ResetProtocol;
import cg.zz.scf.protocol.sdp.ResponseProtocol;

/**
 * 会话描述协议类型
 * @author chengang
 *
 */
public enum SDPType {
	
	/**
	 * 请求响应
	 */
	Response(1),
	
	/**
	 * 请求
	 */
	Request(2),
	
	/**
	 * 异常
	 */
	Exception(3),
	
	/**
	 * ????
	 */
	Config(4),
	
	/**
	 * Handclasp:DES密钥交换消息
	 */
	Handclasp(5),
	
	/**
	 * 服务重启协议
	 */
	Reset(6),
	
	/**
	 * 看样子是交换加密key，但是此功能在服务器未实现
	 */
	StringKey(7);
	
	private final int num;
	
	private SDPType(int num) {
		this.num = num;
	}
	
	public int getNum() {
		return num;
	}
	
	/**
	 * 根据会话协议类型码获取枚举
	 * @param num - int
	 * @return SDPType
	 * @throws Exception
	 */
	public static SDPType getSDPType(int num) throws Exception {
		for (SDPType type : SDPType.values()) {
			if (type.getNum() == num) {
				return type;
			}
		}
		throw new Exception("末知的SDP:" + num);
	}
	
	/**
	 * 根据Class类型获取枚举
	 * @param clazz - Class
	 * @return SDPType
	 * @throws Exception
	 */
	public static SDPType getSDPType(Class<?> clazz) throws Exception {
		if(clazz == RequestProtocol.class){
			return SDPType.Request;
		} else if(clazz == ResponseProtocol.class){
			return SDPType.Response;
		} else if(clazz == ExceptionProtocol.class){
			return SDPType.Exception;
		} else if(clazz == HandclaspProtocol.class){
			return SDPType.Handclasp;
		} else if(clazz == ResetProtocol.class){
			return SDPType.Reset;
		} else if (clazz == String.class) {
			return StringKey;
		}
		throw new Exception("末知的SDP:" + clazz.getName());
	}
	
	/**
	 * 根据SDPType枚举来获取对应协议对象
	 * @param type - SDPType
	 * @return Class
	 * @throws Exception
	 */
	public static Class<?> getSDPClass(SDPType type) throws Exception {
		if(type == SDPType.Request){
			return RequestProtocol.class;
		} else if(type == SDPType.Response) {
			return ResponseProtocol.class;
		} else if(type == SDPType.Exception){
			return ExceptionProtocol.class;
		} else if(type == SDPType.Handclasp){
			return HandclaspProtocol.class;
		} else if(type == SDPType.Reset){
			return ResetProtocol.class;
		} else if (type == StringKey) {
			return String.class;
		}
		throw new Exception("末知的SDP:" + type);
	}
	
	/**
	 * 根据Protocol对象获取对应的协议枚举
	 * @param obj - Object
	 * @return
	 */
	public static SDPType getSDPType(Object obj) {
		if(obj instanceof RequestProtocol) {
	    		return SDPType.Request;
	    	} else if(obj instanceof ResponseProtocol) {
	    		return SDPType.Response;
	    	} else if(obj instanceof HandclaspProtocol) {
	    		return SDPType.Handclasp;
	    	} else if(obj instanceof ResetProtocol){
	    		return SDPType.Reset;
	    	} else if ((obj instanceof String)) {
	    		return StringKey;
	    	} else {
	    		return SDPType.Exception;
	    	}
	}

}
