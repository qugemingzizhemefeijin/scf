package cg.zz.scf.client.proxy.builder;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cg.zz.scf.client.communication.socket.ThreadRenameFactory;
import cg.zz.scf.client.utility.helper.SystemUtils;

/**
 * 异步消息接收到服务器返回的时候的处理连接池
 * @author chengang
 *
 */
public class CallBackExecutor {
	
	static final ThreadPoolExecutor callBackExe = new ThreadPoolExecutor(SystemUtils.getSystemThreadCount(),SystemUtils.getSystemThreadCount(),
		1500L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
		new ThreadRenameFactory("CallBackExecutor-Thread"));

}
