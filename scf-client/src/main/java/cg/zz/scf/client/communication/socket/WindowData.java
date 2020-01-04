package cg.zz.scf.client.communication.socket;

import cg.zz.scf.client.proxy.builder.ReceiveHandler;
import cg.zz.scf.client.utility.AutoResetEvent;

/**
 * 此类是用于控制同步方法的线程阻塞和唤醒或者异步方法的回调执行。
 * 在Servier.request方法中socket.send方法前，会创建此类并注册到此socket中的WindowData中，
 * 在socket.receive中会根据sessionID获取到此类，调用AutoResetEvent.waitOne方法来阻塞当前线程指定时间。
 * 如果该Socket获取到数据返回，则会在Worker中调用frameHandle方法并解锁线程
 * @author chengang
 *
 */
public class WindowData {
	
	/**
	 * 同步调用需要用到的控制线程阻塞和恢复的类，里面实际就是用到了CountDownLatch类来控制的
	 */
	AutoResetEvent _event;
	
	/**
	 * 接收都的数据
	 */
	byte[] _data;
	
	/**
	 * 0同步消息，1异步消息
	 */
	private byte flag = 0;
	
	/**
	 * 异常信息
	 */
	private Exception exception;
	
	/**
	 * 时间戳
	 */
	private long timestamp;
	
	/**
	 * 异步回调处理对象
	 */
	private ReceiveHandler receiveHandler;
	
	/**
	 * 异步回调需要用到的CSocket对象
	 */
	private CSocket csocket;
	
	/**
	 * 异步方法发送的数据
	 */
	private byte[] sendData;
	
	/**
	 * 消息ID
	 */
	private int sessionId;
	
	/**
	 * 同步消息专用的构造函数
	 * @param event - AutoResetEvent
	 */
	public WindowData(AutoResetEvent event) {
		this._event = event;
	}

	/**
	 * 异步消息的构造函数（这个好像没有用）
	 * @param receiveHandler - 异步处理回调
	 * @param csocket - CSocket
	 */
	public WindowData(ReceiveHandler receiveHandler, CSocket csocket) {
		this.flag = 1;
		this.receiveHandler = receiveHandler;
		this.csocket = csocket;
	}

	/**
	 * 异步消息专用的构造函数
	 * @param receiveHandler - 异步处理回调
	 * @param csocket - CSocket
	 * @param sendData - 发送的消息内容
	 * @param sessionId - 消息ID
	 */
	public WindowData(ReceiveHandler receiveHandler, CSocket csocket, byte[] sendData, int sessionId) {
		this.flag = 1;
		this.receiveHandler = receiveHandler;
		this.csocket = csocket;
		this.sendData = sendData;
		this.sessionId = sessionId;
		this.timestamp = System.currentTimeMillis();
	}

	public AutoResetEvent getEvent() {
		return this._event;
	}

	public byte[] getData() {
		return this._data;
	}

	public void setData(byte[] data) {
		this._data = data;
	}

	public byte getFlag() {
		return this.flag;
	}

	public void setFlag(byte flag) {
		this.flag = flag;
	}

	public Exception getException() {
		return this.exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public ReceiveHandler getReceiveHandler() {
		return this.receiveHandler;
	}

	public void setReceiveHandler(ReceiveHandler receiveHandler) {
		this.receiveHandler = receiveHandler;
	}

	public CSocket getCsocket() {
		return this.csocket;
	}

	public void setCsocket(CSocket csocket) {
		this.csocket = csocket;
	}

	public byte[] getSendData() {
		return this.sendData;
	}

	public void setSendData(byte[] sendData) {
		this.sendData = sendData;
	}

	public int getSessionId() {
		return this.sessionId;
	}

	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}

}
