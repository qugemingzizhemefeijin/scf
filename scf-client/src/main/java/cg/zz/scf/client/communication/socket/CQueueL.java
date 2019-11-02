package cg.zz.scf.client.communication.socket;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 队列，此队列维持了两个列表。
 * 其中一个是其本身的队列，另一个是一个额外的队列，此值可能会大于等于本身队列的长度
 *
 */
public class CQueueL extends LinkedBlockingQueue<CSocket> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4286566925351152261L;
	
	/**
	 * 元素的最大空闲时间
	 */
	private int _duration;
	
	/**
	 * 最少的空闲元素
	 */
	private int _minConn;
	
	/**
	 * 锁
	 */
	private final Object shrinkLockHelper = new Object();
	
	/**
	 * 元素
	 */
	private CopyOnWriteArrayList<CSocket> _AllSocket = new CopyOnWriteArrayList<>();
	
	/**
	 * 上次检查时间
	 */
	private long lastCheckTime = System.currentTimeMillis();
	
	/**
	 * 空闲数量
	 */
	private int freeCount = -1;
	
	/**
	 * 当前数量
	 */
	private int shrinkCount = 0;
	
	public CQueueL(int _duration, int _minConn) {
		this._duration = _duration;
		this._minConn = _minConn;
	}
	
	/**
	 * 如果可能，在队列尾部插入指定的元素，如果队列已满则立即返回。
	 * @param element - 要添加的元素
	 * @return CSocket
	 */
	public CSocket enqueue(CSocket element) {
		offer(element);
		return element;
	}
	
	/**
	 * 检索并移除此队列的头，如果此队列为空，则返回 null。
	 * @return CSocket
	 */
	public CSocket dequeue() {
		return poll();
	}
	
	/**
	 * 检索并移除此队列的头部，如果此队列中没有任何元素，则等待指定等待的时间（如果有必要）。
	 * @param time - 等待时间
	 * @return CSocket
	 * @throws InterruptedException
	 */
	public CSocket dequeue(long time) throws InterruptedException {
		return poll(time, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * 注册一个CSocket
	 * @param socket - CSocket
	 */
	public void register(CSocket socket) {
		this._AllSocket.add(socket);
	}
	
	/**
	 * 移除一个连接
	 * @param socket - CSocket
	 * @return boolean
	 */
	public synchronized boolean remove(CSocket socket) {
		this._AllSocket.remove(socket);
		return super.remove(socket);
	}
	
	/**
	 * 获得当前所有连接的数量（包括正在被使用着的）
	 * @return int
	 */
	public int getTotal() {
		return this._AllSocket.size();
	}
	
	/**
	 * 获得所有的连接列表
	 * @return List<CSocket>
	 */
	public List<CSocket> getAllSocket() {
		return this._AllSocket;
	}
	
	/**
	 * 检查当前连接池可被回收的数量，如果可被回收的数量>0，则返回true。否则更新可被回收的数量，并刷新检查时间。
	 * @return boolean
	 */
	public boolean shrink() {
		synchronized (this.shrinkLockHelper) {
			if (this.shrinkCount > 0) {
				this.shrinkCount -= 1;
				return true;
			}
			if (System.currentTimeMillis() - this.lastCheckTime > this._duration) {
				this.lastCheckTime = System.currentTimeMillis();
				boolean b = (this.freeCount > 0 && getTotal() > this._minConn);
				if (b) {
					this.shrinkCount = Math.min(getTotal() - this._minConn, this.freeCount);
					if (this.shrinkCount < 0) {
						this.shrinkCount = 0;
					}
				}
				return false;
			}
			int currFreeCount = size();
			if (currFreeCount < this.freeCount || (this.freeCount < 0)) {
				this.freeCount = currFreeCount;
			}
			return false;
		}
	}

}
