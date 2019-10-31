package cg.zz.scf.server.contract.context;

import java.util.List;

import cg.zz.scf.server.contract.entity.Out;

/**
 * SCF Response
 * @author chengang
 *
 */
public class SCFResponse {
	
	/**
	 * 返回值
	 */
	private Object returnValue;

	/**
	 * 输出对象集合
	 */
	private List<Out<?>> outParaList;
	
	/**
	 * 输出字节数组
	 */
	private byte[] responseBuffer;
	
	public SCFResponse() {
		
	}
	
	public SCFResponse(String rv, List<Out<?>> op) {
		setValue(rv , op);
	}
	
	public SCFResponse(int rv, List<Out<?>> op) {
		setValue(Integer.valueOf(rv), op);
	}
	
	public SCFResponse(Integer rv, List<Out<?>> op) {
		setValue(rv, op);
	}
	
	public SCFResponse(Long rv, List<Out<?>> op) {
		setValue(rv, op);
	}
	
	public SCFResponse(long rv, List<Out<?>> op) {
		setValue(Long.valueOf(rv), op);
	}
	
	public SCFResponse(short rv, List<Out<?>> op) {
		setValue(Short.valueOf(rv), op);
	}
	
	public SCFResponse(Short rv, List<Out<?>> op) {
		setValue(rv, op);
	}
	
	public SCFResponse(float rv, List<Out<?>> op) {
		setValue(Float.valueOf(rv), op);
	}
	
	public SCFResponse(Float rv, List<Out<?>> op) {
		setValue(rv, op);
	}
	
	public SCFResponse(boolean rv, List<Out<?>> op) {
		setValue(Boolean.valueOf(rv), op);
	}
	
	public SCFResponse(Boolean rv, List<Out<?>> op) {
		setValue(rv, op);
	}
	
	public SCFResponse(double rv, List<Out<?>> op) {
		setValue(Double.valueOf(rv), op);
	}
	
	public SCFResponse(Double rv, List<Out<?>> op) {
		setValue(rv, op);
	}
	
	public SCFResponse(char rv, List<Out<?>> op) {
		setValue(Character.valueOf(rv), op);
	}
	
	public SCFResponse(Character rv, List<Out<?>> op) {
		setValue(rv, op);
	}
	
	public SCFResponse(byte rv, List<Out<?>> op) {
		setValue(Byte.valueOf(rv), op);
	}
	
	public SCFResponse(Object rv, List<Out<?>> op) {
		setValue(rv, op);
	}
	
	public SCFResponse(Byte rv, List<Out<?>> op) {
		setValue(rv, op);
	}
	
	/**
	 * 设置初始值
	 * @param rv - 返回值
	 * @param op - 输出对象列表
	 */
	public void setValue(Object rv, List<Out<?>> op) {
		this.outParaList = op;
		this.returnValue = rv;
	}

	public Object getReturnValue() {
		return returnValue;
	}

	public void setReturnValue(Object returnValue) {
		this.returnValue = returnValue;
	}

	public List<Out<?>> getOutParaList() {
		return outParaList;
	}

	public void setOutParaList(List<Out<?>> outParaList) {
		this.outParaList = outParaList;
	}

	public byte[] getResponseBuffer() {
		return responseBuffer;
	}

	public void setResponseBuffer(byte[] responseBuffer) {
		this.responseBuffer = responseBuffer;
	}

}
