package cg.zz.scf.server.filter;

import cg.zz.scf.protocol.sfp.v1.Protocol;
import cg.zz.scf.server.contract.context.Global;
import cg.zz.scf.server.contract.context.SCFContext;
import cg.zz.scf.server.contract.context.SCFResponse;
import cg.zz.scf.server.contract.context.SecureContext;
import cg.zz.scf.server.contract.filter.IFilter;
import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;

/**
 * 返回协议的创建，如果需要加密传输则自动进行加密
 * @author chengang
 *
 */
public class ProtocolCreateFilter implements IFilter {
	
	private static ILog logger = LogFactory.getLogger(ExecuteMethodFilter.class);

	@Override
	public void filter(SCFContext context) throws Exception {
		try {
			Protocol protocol = context.getScfRequest().getProtocol();
			byte[] desKeyByte = null;
		        String desKeyStr = null;
		        boolean bool = false;
		        
		        Global global = Global.getInstance();
			if(global != null){
				//判断当前服务启用权限认证
				if(global.getGlobalSecureIsRights()){
					SecureContext securecontext = global.getGlobalSecureContext(context.getChannel().getNettyChannel());
					bool = securecontext.isRights();
					if(bool){
						desKeyStr = securecontext.getDesKey();
					}
				}
			}
			
			if(desKeyStr != null){
				desKeyByte = desKeyStr.getBytes("utf-8");
			}
			
			if (context.getScfResponse() == null) {
				SCFResponse respone = new SCFResponse();
				context.setScfResponse(respone);
			}
			context.getScfResponse().setResponseBuffer(protocol.toBytes(Global.getInstance().getGlobalSecureIsRights(), desKeyByte));
		} catch (Exception ex) {
			System.out.println(context);
			logger.error("Server ProtocolCreateFilter error!", ex);
		}
	}
	
	@Override
	public int getPriority() {
		return 50;
	}

}
