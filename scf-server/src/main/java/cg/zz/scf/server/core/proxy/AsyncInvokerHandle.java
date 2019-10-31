package cg.zz.scf.server.core.proxy;

import cg.zz.scf.protocol.sfp.v1.Protocol;
import cg.zz.scf.server.contract.context.ExecFilterType;
import cg.zz.scf.server.contract.context.Global;
import cg.zz.scf.server.contract.context.SCFContext;
import cg.zz.scf.server.contract.context.SCFResponse;
import cg.zz.scf.server.contract.context.SecureContext;
import cg.zz.scf.server.contract.context.ServerType;
import cg.zz.scf.server.contract.filter.IFilter;
import cg.zz.scf.server.contract.http.HttpThreadLocal;
import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;
import cg.zz.scf.server.performance.monitorweb.AbandonCount;
import cg.zz.scf.server.util.ExceptionHelper;
import cg.zz.utility.async.AsyncInvoker;
import cg.zz.utility.async.IAsyncHandler;

/**
 * 异步调用操作类
 * @author chengang
 *
 */
public class AsyncInvokerHandle extends InvokerBase {
	
	private static final ILog logger = LogFactory.getLogger(AsyncInvokerHandle.class);
	
	private AsyncInvoker asyncInvoker;
	private HttpThreadLocal httpThreadLocal;
	private int taskTimeOut = 1000;
	private int inQueueTime = -1;
	
	public AsyncInvokerHandle(){
		try {
			this.httpThreadLocal = HttpThreadLocal.getInstance();
			//工作线程数量
			int workerCount = Global.getInstance().getServiceConfig().getInt("scf.async.worker.count");
			if (workerCount > 0) {
				this.asyncInvoker = AsyncInvoker.getInstance(workerCount, false, "Scf Async worker");
			} else {
				this.asyncInvoker = AsyncInvoker.getInstance();
			}
			
			//tcp任务超时时间
			String sTaskTimeOut = Global.getInstance().getServiceConfig().getString("scf.server.tcp.task.timeout");
			if (sTaskTimeOut != null && !"".equals(sTaskTimeOut)) {
				this.taskTimeOut = Integer.parseInt(sTaskTimeOut);
			}
			
			String sInQueueTime = Global.getInstance().getServiceConfig().getString("scf.server.tcp.task.inqueue");
			if (sInQueueTime != null && !"".equals(sInQueueTime)) {
				this.inQueueTime = Integer.parseInt(sInQueueTime);
			}
			logger.info("async worker count:" + workerCount);
		} catch (Exception e) {
			logger.error("init AsyncInvokerHandle error", e);
		}
	}

	@Override
	public void invoke(final SCFContext context) throws Exception {
		logger.debug("-------------------begin async invoke-------------------");

		this.asyncInvoker.run(this.taskTimeOut, this.inQueueTime, new IAsyncHandler() {

			@Override
			public Object run() throws Throwable {
				AsyncInvokerHandle.logger.debug("begin request filter");
				
				// request filter
				for(IFilter f : Global.getInstance().getGlobalRequestFilterList()) {
					if(context.getExecFilter() == ExecFilterType.All || context.getExecFilter() == ExecFilterType.RequestOnly) {
						f.filter(context);
					}
				}
				
				if(context.isDoInvoke()) {
					if(context.getServerType() == ServerType.HTTP){
						httpThreadLocal.set(context.getHttpContext());
					}
					logger.debug("sessionId : " + context.getSessionID());
					doInvoke(context);
				}
				
				logger.debug("begin response filter");
				// response filter
				for(IFilter f : Global.getInstance().getGlobalResponseFilterList()) {
					if(context.getExecFilter() == ExecFilterType.All || context.getExecFilter() == ExecFilterType.ResponseOnly) {
						f.filter(context);
					}
				}
				
				return context;
			}

			@Override
			public void messageReceived(Object obj) {
				try {
					if(context.getServerType() == ServerType.HTTP){
						httpThreadLocal.remove();
					}
					
					if(obj != null) {
						SCFContext ctx = (SCFContext)obj;
						ctx.getServerHandler().writeResponse(ctx);
					} else {
						logger.error("context is null!");
					}
				} finally {
					SCFContext.removeThreadLocal();
				}
			}

			@Override
			public void exceptionCaught(Throwable e) {
				try {
					if(context.getServerType() == ServerType.HTTP){
						httpThreadLocal.remove();
					}
					
					if(context.getScfResponse() == null){
						SCFResponse respone = new SCFResponse();
						context.setScfResponse(respone);
					}
					
					if (e.getMessage().indexOf("timeout") > 0) {
						try {
							AbandonCount.messageRecv();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					
					byte[] desKeyByte = (byte[]) null;
					String desKeyStr = null;
					boolean bool = false;
					
					Global global = Global.getInstance();
					if (global != null) {
						//判断当前服务启用权限认证
						if (global.getGlobalSecureIsRights()) {
							SecureContext securecontext = global.getGlobalSecureContext(context.getChannel().getNettyChannel());
							bool = securecontext.isRights();
							if (bool) {
								desKeyStr = securecontext.getDesKey();
							}
						}
					}
					
					if (desKeyStr != null) {
						desKeyByte = desKeyStr.getBytes("utf-8");
					}
					
					Protocol protocol = context.getScfRequest().getProtocol();
					if (protocol == null) {
						protocol = Protocol.fromBytes(context.getScfRequest().getRequestBuffer(), global.getGlobalSecureIsRights(),desKeyByte);
						context.getScfRequest().setProtocol(protocol);
					}
					protocol.setSdpEntity(ExceptionHelper.createError(e));
					context.getScfResponse().setResponseBuffer(protocol.toBytes(Global.getInstance().getGlobalSecureIsRights(), desKeyByte));
				} catch (Exception ex) {
					context.getScfResponse().setResponseBuffer(new byte[1]);
					AsyncInvokerHandle.logger.error("AsyncInvokerHandle invoke-exceptionCaught error", ex);
				} finally {
					SCFContext.removeThreadLocal();
					AsyncInvokerHandle.logger.error("AsynBack.contextMap.remove " + context.getSessionID());
				        AsynBack.contextMap.remove(Integer.valueOf(context.getSessionID()));
				}
				
				logger.info("返回数据!!!!!!!!!!!!!!!!!!!!!!!!!!");
				context.getServerHandler().writeResponse(context);

			        AsyncInvokerHandle.logger.error("AsyncInvokerHandle invoke error", e);
			}
			
		});
	}

}
