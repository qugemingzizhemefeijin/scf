package cg.zz.scf.server.core.proxy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.handler.timeout.TimeoutException;

import cg.zz.scf.protocol.sdp.ResponseProtocol;
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
 * 异步返回
 * @author chengang
 *
 */
public class AsynBack {
	
	private static final ILog logger = LogFactory.getLogger(AsynBack.class);
	
	private static AsynBack asyn = null;
	
	private static int taskTimeOut = 1000;
	private static int inQueueTime = -1;
	private static HttpThreadLocal httpThreadLocal;
	public static Map<String, Integer> asynMap = new ConcurrentHashMap<String , Integer>();
	public static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();//CPU数量
	private static AsyncInvoker asyncInvoker = AsyncInvoker.getInstance(THREAD_COUNT, false, "Back Async Worker");
	public static Map<Integer, SCFContext> contextMap = new ConcurrentHashMap<Integer , SCFContext>();
	public static final CallBackUtil callBackUtil = new CallBackUtil();
	
	static {
		try {
			httpThreadLocal = HttpThreadLocal.getInstance();
			String sTaskTimeOut = Global.getInstance().getServiceConfig().getString("back.task.timeout");
			if (sTaskTimeOut != null && !"".equals(sTaskTimeOut)) {
				taskTimeOut = Integer.parseInt(sTaskTimeOut);
			}
			
			String sInQueueTime = Global.getInstance().getServiceConfig().getString("scf.server.task.asyn.inqueue");
			if (sInQueueTime != null && !"".equals(sInQueueTime)) {
				inQueueTime = Integer.parseInt(sInQueueTime);
			}
			logger.info("back async worker count:" + THREAD_COUNT);
		} catch (Exception e) {
			logger.error("init AsyncInvokerHandle error", e);
		}
	}
	
	public static AsynBack getAsynBack() {
		return asyn != null ? asyn : new AsynBack();
	}
	
	/**
	 * 异步调用发送数据
	 * @param key - String
	 * @param obj - Object
	 */
	public static void send(int key, final Object obj) {
		//异步调用请求上下文
		final SCFContext context = contextMap.get(Integer.valueOf(key));
		if (context == null) {
			return;
		}
		//将此请求设置为删除状态
		synchronized (context) {
			if ((context == null) || (context.isDel())) {
				return;
			}
			context.setDel(true);
		}
		
		asyncInvoker.run(taskTimeOut, inQueueTime, new IAsyncHandler() {

			@Override
			public Object run() throws Throwable {
				if (obj instanceof Exception) {
					exceptionCaught((Throwable)obj);
					return null;
				}
				
				Protocol protocol = context.getScfRequest().getProtocol();
			        SCFResponse response = new SCFResponse(obj, null);
			        
			        protocol.setSdpEntity(new ResponseProtocol(response.getReturnValue(), null));
			        
			        for (IFilter f : Global.getInstance().getGlobalResponseFilterList()) {
			        	if (context.getExecFilter() == ExecFilterType.All || context.getExecFilter() == ExecFilterType.ResponseOnly) {
			        		f.filter(context);
			        	}
			        }
			        return context;
			}

			@Override
			public void messageReceived(Object obj) {
				if (obj != null) {
					SCFContext ctx = (SCFContext) obj;
					if (ctx.isAsyn()) {
						ctx.getServerHandler().writeResponse(ctx);
					} else
						AsynBack.logger.error("The Method is Synchronized!");
				}
			}

			@Override
			public void exceptionCaught(Throwable e) {
				try {
					if (context != null || context.getServerType() == ServerType.HTTP) {
						AsynBack.httpThreadLocal.remove();
					}

					if (context.getScfResponse() == null) {
						SCFResponse respone = new SCFResponse();
						context.setScfResponse(respone);
					}

					if ((e instanceof TimeoutException)) {
						try {
							AbandonCount.messageRecv();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					
					byte[] desKeyByte = null;
					String desKeyStr = null;
					boolean bool = false;
					
					Global global = Global.getInstance();
					if (global != null) {
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
					AsynBack.logger.error("AsyncInvokerHandle invoke-exceptionCaught error", ex);
				} finally {
					context.getServerHandler().writeResponse(context);
					AsynBack.logger.error("AsyncInvokerHandle invoke error", e);
				}
			}
			
		});
	}

}
