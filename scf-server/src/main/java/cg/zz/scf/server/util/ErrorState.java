package cg.zz.scf.server.util;

/**
 * 错误枚举
 * @author chengang
 *
 */
public enum ErrorState {
	
	/**
	 * 数据库错误
	 */
	DBException(1), 
	
	/**
	 * 网络错误
	 */
	NetException(2), 
	
	/**
	 * 超时
	 */
	TimoutException(3), 
	
	/**
	 * 协议错误
	 */
	ProtocolException(4),
	
	/**
	 * 消息体json格式错误
	 */
	JsonException(5),
	
	/**
	 * 方法调用参数错误
	 */
	ParaException(6),
	
	/**
	 * 找不到方法
	 */
	NotFoundMethodException(7),
	
	/**
	 * 找不到服务
	 */
	NotFoundServiceException(8),
	
	/**
	 * JSON序列化错误
	 */
	JSONSerializeException(9),
	
	/**
	 * 服务错误
	 */
	ServiceException(10),
	
	/**
	 * 数据溢出错误
	 */
	DataOverFlowException(11),
	
	/**
	 * 其它错误
	 */
	OtherException(99);
	
	private final int stateNum;
	
	private ErrorState(int stateNum) {
		this.stateNum = stateNum;
	}

	public int getStateNum() {
		return stateNum;
	}

}
