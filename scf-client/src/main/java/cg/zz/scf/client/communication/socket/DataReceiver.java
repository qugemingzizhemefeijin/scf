package cg.zz.scf.client.communication.socket;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 数据接受者
 * @author chengang
 *
 */
public class DataReceiver {
	
	private static DataReceiver _DataReceiver = null;
	private static final Object LockHelper = new Object();
	private final ReentrantLock lock = new ReentrantLock();
	private Worker worker = null;
	
	private DataReceiver() throws IOException {
		this.worker = new Worker();
		
		//启动一个IO线程，负责写入和接收消息
		Thread thread = new Thread(this.worker);
		thread.setName("DataReceiver-thread");
		thread.setDaemon(true);
		thread.start();
	}
	
	public static DataReceiver instance() throws ClosedChannelException, IOException {
		if (_DataReceiver == null) {
			synchronized (LockHelper) {
				if (_DataReceiver == null) {
					_DataReceiver = new DataReceiver();
				}
			}
		}
		return _DataReceiver;
	}
	
	/**
	 * 像工作线程注册一个通道
	 * @param socket - CSocket
	 * @throws ClosedChannelException
	 * @throws IOException
	 */
	public void RegSocketChannel(CSocket socket) throws ClosedChannelException, IOException {
		this.lock.lock();
		try {
			this.worker.register(socket);
		} finally {
			this.lock.unlock();
		}
	}
	
	public void UnRegSocketChannel(CSocket socket) {
		
	}

	/**
	 * 关闭数据接收线程
	 */
	public static void closeRecv() {
		Worker.setControl(false);
	}

}
