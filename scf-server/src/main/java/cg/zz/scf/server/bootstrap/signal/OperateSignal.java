package cg.zz.scf.server.bootstrap.signal;

import sun.misc.Signal;
import sun.misc.SignalHandler;
import cg.zz.scf.server.contract.context.Global;
import cg.zz.scf.server.contract.context.ServerStateType;
import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;

/**
 * 在Java窗口程序中按ctrl+c会强行中止Java程序。点击窗口关闭按钮也会强行中止程序。 
 * Runtime.getRuntime().addShutdownHook(hooker)允许注册一个线程，在System.exit()之后、 finalize被调用之前执行它。 
 * 根据文档，如果有多个程序注册了hooker, 它们会被同时启用，顺序不被保证。比如你想在关闭前透过Jms发送消息，
 * 很可能JMS服务已经关闭。 Google告诉我们，还有别的好办法，就是利用Sun的独家秘技，直接拦截系统信号。
 * 注意，在非Sun的jvm上无效。
 * @author Administrator
 *
 */
@SuppressWarnings("restriction")
public class OperateSignal implements SignalHandler {
	
	private static ILog logger = LogFactory.getLogger(OperateSignal.class);

	@Override
	public void handle(Signal arg0) {
		Global.getInstance().setServerState(ServerStateType.Reboot);
		logger.info(Global.getInstance().getServiceConfig().getString("scf.service.name") + " Server state is " + Global.getInstance().getServerState());
		logger.info(Global.getInstance().getServiceConfig().getString("scf.service.name") + " Server will reboot!");
	}

}
