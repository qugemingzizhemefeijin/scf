package cg.zz.scf.client.communication.socket;

import java.io.IOException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import cg.zz.scf.client.utility.logger.ILog;
import cg.zz.scf.client.utility.logger.LogFactory;

/**
 * 工作者
 * @author chengang
 *
 */
public class Worker implements Runnable {
	
	private static ILog logger = LogFactory.getLogger(Worker.class);
	
	private List<CSocket> sockets = new ArrayList<>();
	
	/**
	 * 控制开关
	 */
	private static boolean control = true;
	
	private Selector selector;
	
	/**
	 * 锁
	 */
	private final Object locker = new Object();
	
	public Worker() throws IOException {
		this.selector = Selector.open();
	}
	
	/**
	 * 注册一个通道
	 * @param csocket - CSocket
	 * @throws IOException
	 */
	public void register(CSocket csocket) throws IOException {
		if (csocket.connecting()) {
			synchronized (this.locker) {
				this.sockets.add(csocket);
			}
			this.selector.wakeup();
		} else {
			throw new IOException("channel is not open when register selector");
		}
	}

	@Override
	public void run() {
		while (isControl()) {
			CSocket nioChannel = null;
			try {
				this.selector.select();
				if (this.sockets.size() > 0) {
					synchronized (this.locker) {
						for (CSocket channel : this.sockets) {
							channel.getChannle().register(this.selector, SelectionKey.OP_READ, channel);
						}
						this.sockets.clear();
					}
				}
				Set<SelectionKey> selectedKeys = this.selector.selectedKeys();
				Iterator<SelectionKey> it = selectedKeys.iterator();
				while (it.hasNext()) {
					SelectionKey key = it.next();
					if (!key.isValid() || (key.readyOps() & 0x1) != 1) continue;
					nioChannel = (CSocket)key.attachment();
					nioChannel.frameHandle();
				}
				selectedKeys.clear();
			} catch (IOException e) {
				if (nioChannel != null) {
					nioChannel.closeAndDisponse();
				}
				logger.error("receive data error", e);
			} catch (NotYetConnectedException e) {
				if (nioChannel != null) {
					nioChannel.closeAndDisponse();
				}
				logger.error("receive data error", e);
			} catch (InterruptedException e) {
				logger.error("receive data error", e);
			} catch (Throwable t) {
				logger.error("receive data error", t);
			}
		}
	}
	
	protected static boolean isControl() {
		return control;
	}

	protected static void setControl(boolean control) {
		Worker.control = control;
	}

}
