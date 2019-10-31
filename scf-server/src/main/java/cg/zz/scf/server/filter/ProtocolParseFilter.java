package cg.zz.scf.server.filter;

import cg.zz.scf.protocol.sdp.ResetProtocol;
import cg.zz.scf.protocol.sfp.enumeration.PlatformType;
import cg.zz.scf.protocol.sfp.v1.Protocol;
import cg.zz.scf.server.contract.context.ExecFilterType;
import cg.zz.scf.server.contract.context.Global;
import cg.zz.scf.server.contract.context.SCFContext;
import cg.zz.scf.server.contract.context.SCFResponse;
import cg.zz.scf.server.contract.context.SecureContext;
import cg.zz.scf.server.contract.context.ServerStateType;
import cg.zz.scf.server.contract.context.ServerType;
import cg.zz.scf.server.contract.filter.IFilter;

/**
 * 协议转换过滤器
 * @author chengang
 *
 */
public class ProtocolParseFilter implements IFilter {

	@Override
	public int getPriority() {
		return 50;
	}

	@Override
	public void filter(SCFContext context) throws Exception {
		if (context.getServerType() == ServerType.TCP) {//只支持tcp协议
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
			
			Protocol protocol = Protocol.fromBytes(context.getScfRequest().getRequestBuffer(), global.getGlobalSecureIsRights(), desKeyByte);
			context.getScfRequest().setProtocol(protocol);
			
			//如果服务器状态正在重启，并且平台是java，则返回设置
			if (Global.getInstance().getServerState() == ServerStateType.Reboot && protocol.getPlatformType() == PlatformType.Java) {
				SCFResponse response = new SCFResponse();
				ResetProtocol rp = new ResetProtocol();
				rp.setMsg("This server is reboot!");
				protocol.setSdpEntity(rp);
			        response.setResponseBuffer(protocol.toBytes(global.getGlobalSecureIsRights(), desKeyByte));
			        context.setScfResponse(response);
			        context.setExecFilter(ExecFilterType.None);
			        context.setDoInvoke(false);
			}
		}
	}

}
