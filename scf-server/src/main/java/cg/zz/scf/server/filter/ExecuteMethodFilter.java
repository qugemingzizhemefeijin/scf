package cg.zz.scf.server.filter;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cg.zz.scf.protocol.sdp.ExceptionProtocol;
import cg.zz.scf.protocol.sdp.RequestProtocol;
import cg.zz.scf.protocol.sfp.enumeration.PlatformType;
import cg.zz.scf.protocol.sfp.v1.Protocol;
import cg.zz.scf.protocol.utility.KeyValuePair;
import cg.zz.scf.server.contract.context.ExecFilterType;
import cg.zz.scf.server.contract.context.Global;
import cg.zz.scf.server.contract.context.SCFContext;
import cg.zz.scf.server.contract.context.SCFResponse;
import cg.zz.scf.server.contract.context.SecureContext;
import cg.zz.scf.server.contract.context.ServerType;
import cg.zz.scf.server.contract.filter.IFilter;
import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;
import cg.zz.scf.server.util.ExceptionHelper;

/**
 * 判断方法是否有权限调用
 * @author chengang
 *
 */
public class ExecuteMethodFilter implements IFilter {
	
	private static ILog logger = LogFactory.getLogger(ExecuteMethodFilter.class);

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public void filter(SCFContext context) throws Exception {
		Global global = Global.getInstance();
		Protocol p = context.getScfRequest().getProtocol();
		SCFResponse response = new SCFResponse();
		if (p.getPlatformType() == PlatformType.Java && context.getServerType() == ServerType.TCP) {
			//当前服务启动权限认证,并且当前channel通过校验，则进行方法校验
			SecureContext securecontext = global.getGlobalSecureContext(context.getChannel().getNettyChannel());
			if (global.getGlobalSecureIsRights()) {
				//当前服务启用权限认证,判断当前channel是否通过授权
				if (securecontext.isRights()) {
					RequestProtocol request = (RequestProtocol)p.getSdpEntity();
					if(request != null){
						StringBuffer buff = new StringBuffer(request.getLookup() + "." + request.getMethodName());
						buff.append("(");
						List<KeyValuePair> list = request.getParaKVList();
						if(list != null){
							int i=0;
							for(KeyValuePair k : list){
								if(k != null){
									if(i > 0){
										buff.append(",");
									}
									buff.append(k.getKey());
									++i;
								}
							}
						}
						buff.append(")");
						
						boolean bool = true;
						Map<String, List<String>> map = global.getSecureMap();
						if(map != null){
							Iterator<Map.Entry<String, List<String>>> iter = map.entrySet().iterator();
							while(iter.hasNext()){
								Map.Entry<String, List<String>> enty = iter.next();
								for(String str:enty.getValue()){
									if(str.equalsIgnoreCase(buff.toString())){
										bool = false;
										break;
									}
								}
							}
						}
						
						if(bool){
							logger.error("当前调用方法没有授权!");
							this.contextException(context, p, response, "当前调用方法没有授权!",global.getGlobalSecureIsRights(),securecontext.getDesKey().getBytes("utf-8"));
						}
					}
				} else {
					logger.error("当前连接没有通过权限认证!");
					this.contextException(context, p, response, "当前连接没有通过权限认证!");
				}
			}
		}
	}
	
	public void contextException(SCFContext context, Protocol protocol, SCFResponse response, String message, boolean bool, byte[] key) throws Exception {
		ExceptionProtocol ep = ExceptionHelper.createError(new Exception());
		ep.setErrorMsg(message);
		protocol.setSdpEntity(ep);
		response.setResponseBuffer(protocol.toBytes(bool, key));
		context.setScfResponse(response);
		setInvokeAndFilter(context);
	}
	
	public void contextException(SCFContext context, Protocol protocol, SCFResponse response, String message) throws Exception {
		ExceptionProtocol ep = ExceptionHelper.createError(new Exception());
		ep.setErrorMsg(message);
		protocol.setSdpEntity(ep);
		response.setResponseBuffer(protocol.toBytes());
		context.setScfResponse(response);
		setInvokeAndFilter(context);
	}
	
	public void setInvokeAndFilter(SCFContext context) {
		context.setExecFilter(ExecFilterType.None);
		context.setDoInvoke(false);
	}

}
