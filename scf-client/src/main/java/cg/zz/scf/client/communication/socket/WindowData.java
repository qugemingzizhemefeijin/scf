package cg.zz.scf.client.communication.socket;

import cg.zz.scf.client.proxy.builder.ReceiveHandler;
import cg.zz.scf.client.utility.AutoResetEvent;

public class WindowData {
	
	AutoResetEvent _event;
	byte[] _data;
	private byte flag = 0;
	private Exception exception;
	private long timestamp;
	private ReceiveHandler receiveHandler;
	private CSocket csocket;
	private byte[] sendData;
	private int sessionId;
	
	public WindowData(AutoResetEvent event) {
		this._event = event;
	}

	public WindowData(ReceiveHandler receiveHandler, CSocket csocket) {
		this.flag = 1;
		this.receiveHandler = receiveHandler;
		this.csocket = csocket;
	}

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
