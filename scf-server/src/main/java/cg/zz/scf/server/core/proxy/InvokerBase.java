package cg.zz.scf.server.core.proxy;

import cg.zz.scf.protocol.sdp.RequestProtocol;
import cg.zz.scf.protocol.sdp.ResponseProtocol;
import cg.zz.scf.protocol.sfp.v1.Protocol;
import cg.zz.scf.protocol.utility.KeyValuePair;
import cg.zz.scf.server.contract.context.Global;
import cg.zz.scf.server.contract.context.IProxyStub;
import cg.zz.scf.server.contract.context.SCFContext;
import cg.zz.scf.server.contract.context.SCFResponse;
import cg.zz.scf.server.contract.context.StopWatch;
import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;
import cg.zz.scf.server.performance.monitorweb.FrameExCount;
import cg.zz.scf.server.util.ErrorState;
import cg.zz.scf.server.util.ExceptionHelper;
import cg.zz.scf.server.util.ServiceFrameException;
import cg.zz.scf.server.util.SystemUtils;

/**
 * 调用者抽象类
 * @author chengang
 *
 */
public abstract class InvokerBase implements IInvokerHandle {
	
	private static final ILog logger = LogFactory.getLogger(InvokerBase.class);

	/**
	 * 调用
	 * @param context
	 */
	void doInvoke(SCFContext context) {
		logger.debug("------------------------------ begin request-----------------------------");
		
		StringBuffer sbInvokerMsg = new StringBuffer();
		StringBuffer sbIsAsynMsg = new StringBuffer();
		StopWatch sw = context.getStopWatch();
		
		Object response = null;
		Protocol protocol = null;
		
		try {
			protocol = context.getScfRequest().getProtocol();
			RequestProtocol request = (RequestProtocol)protocol.getSdpEntity();
			
			sbInvokerMsg.append("protocol version:");
			sbInvokerMsg.append(protocol.getVersion());
			sbInvokerMsg.append("\nfromIP:");
			sbInvokerMsg.append(context.getChannel().getRemoteIP());
			sbInvokerMsg.append("\nlookUP:");
			sbInvokerMsg.append(request.getLookup());
			sbIsAsynMsg.append(request.getLookup());
			sbInvokerMsg.append("\nmethodName:");
			sbInvokerMsg.append(request.getMethodName());
			sbIsAsynMsg.append(request.getMethodName());
			sbInvokerMsg.append("\nparams:");
			
			if (request.getParaKVList() != null) {
				for (KeyValuePair kv : request.getParaKVList()) {
					if (kv != null) {
						sbInvokerMsg.append("\n--key:");
						sbInvokerMsg.append(kv.getKey());
						sbIsAsynMsg.append(kv.getKey());
						sbInvokerMsg.append("\n--value:");
						sbInvokerMsg.append(kv.getValue());
					} else {
						logger.error("KeyValuePair is null  Lookup:" + request.getLookup() + "--MethodName:" + request.getMethodName());
					}
				}
			}
			
			logger.debug(sbInvokerMsg.toString());
			logger.debug(sbIsAsynMsg.toString());
			logger.debug("begin get proxy factory");
			
			IProxyStub localProxy = Global.getInstance().getProxyFactory().getProxy(request.getLookup());
			logger.debug("proxyFactory.getProxy finish");
			
			if (localProxy == null) {
				ServiceFrameException sfe = new ServiceFrameException(
					"method:ProxyHandle.invoke--msg:" + request.getLookup() + "." + request.getMethodName() + " not found",
					context.getChannel()	.getRemoteIP(),
					context.getChannel().getLocalIP(),
					request,
					ErrorState.NotFoundServiceException,
					null);
				
				response = ExceptionHelper.createError(sfe);
				logger.error("localProxy is null", sfe);
			} else {
				logger.debug("begin localProxy.invoke");
				String swInvoderKey = "InvokeRealService_" + request.getLookup() + "." + request.getMethodName();
				//添加一个监控的计时器
				sw.startNew(swInvoderKey, sbInvokerMsg.toString());
				sw.setFromIP(context.getChannel().getRemoteIP());
			        sw.setLocalIP(context.getChannel().getLocalIP());
			        SCFContext.setThreadLocal(context);
			        
			        //如果可以被异步调用，则放到到异步返回的容器中
			        if (AsynBack.asynMap.containsKey(sbIsAsynMsg.toString())) {
			        	int sessionid = SystemUtils.createSessionId();
			                context.setAsyn(true);
			                context.setSessionID(sessionid);
			                AsynBack.contextMap.put(Integer.valueOf(sessionid), context);
			                AsynBack.callBackUtil.offer(new WData(sessionid, System.currentTimeMillis()));
			        }
			        
			        SCFResponse scfResponse = localProxy.invoke(context);
			        
			        //如果是异步的则直接返回
			        if (context.isAsyn()) return;
			        
			        sw.stop(swInvoderKey);
			        
			        logger.debug("end localProxy.invoke");
			        context.setScfResponse(scfResponse);
			        response = createResponse(scfResponse);
			        logger.debug("localProxy.invoke finish");
			}
		} catch (ServiceFrameException sfe) {
			logger.error("ServiceFrameException when invoke service fromIP:" + context.getChannel().getRemoteIP() + "  toIP:" + context.getChannel().getLocalIP(), sfe);
			response = ExceptionHelper.createError(sfe);
			context.setError(sfe);
			
			FrameExCount.messageRecv();
			SCFContext.removeThreadLocal();
		} catch (Throwable e) {
			logger.error("Exception when invoke service fromIP:" + context.getChannel().getRemoteIP() + "  toIP:" + context.getChannel().getLocalIP(), e);
			response = ExceptionHelper.createError(e);
			context.setError(e);
			SCFContext.removeThreadLocal();
		}
		
		protocol.setSdpEntity(response);
		logger.debug("---------------------------------- end --------------------------------");
	}
	
	/**
	 * 输出的协议
	 * @param scfResponse - SCFResponse
	 * @return ResponseProtocol
	 */
	ResponseProtocol createResponse(SCFResponse scfResponse) {
		if (scfResponse.getOutParaList() != null && scfResponse.getOutParaList().size() > 0) {
			int outParaSize = scfResponse.getOutParaList().size();
			Object[] objArray = new Object[outParaSize];
			for (int i = 0; i < outParaSize; i++) {
				objArray[i] = scfResponse.getOutParaList().get(i).getOutPara();
			}
			return new ResponseProtocol(scfResponse.getReturnValue(), objArray);
		}
		return new ResponseProtocol(scfResponse.getReturnValue(), null);
	}

}
