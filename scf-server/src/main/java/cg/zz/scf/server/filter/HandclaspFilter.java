package cg.zz.scf.server.filter;

import cg.zz.scf.protocol.sdp.ExceptionProtocol;
import cg.zz.scf.protocol.sdp.HandclaspProtocol;
import cg.zz.scf.protocol.sfp.enumeration.PlatformType;
import cg.zz.scf.protocol.sfp.v1.Protocol;
import cg.zz.scf.server.contract.context.ExecFilterType;
import cg.zz.scf.server.contract.context.Global;
import cg.zz.scf.server.contract.context.SCFContext;
import cg.zz.scf.server.contract.context.SCFResponse;
import cg.zz.scf.server.contract.context.SecureContext;
import cg.zz.scf.server.contract.context.ServerType;
import cg.zz.scf.server.contract.filter.IFilter;
import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;
import cg.zz.scf.server.secure.SecureKey;
import cg.zz.scf.server.secure.StringUtils;
import cg.zz.scf.server.util.ExceptionHelper;

/**
 * 判断客户端和服务端是否启动了权限校验，并且将加密密钥相互传递
 * @author chengang
 *
 */
public class HandclaspFilter implements IFilter {
	
	private static ILog logger = LogFactory.getLogger(HandclaspFilter.class);

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public void filter(SCFContext context) throws Exception {
		Protocol protocol = context.getScfRequest().getProtocol();
		if (protocol.getPlatformType() == PlatformType.Java && context.getServerType() == ServerType.TCP) {
			SCFResponse response = new SCFResponse();
			Global global = Global.getInstance();
			//判断是否启动了权限验证
			if (global.getGlobalSecureIsRights()) {
				SecureContext sc = global.getGlobalSecureContext(context.getChannel().getNettyChannel());
				//判断当前channel是否通过认证
				if (!sc.isRights()) {
					//没有通过认证
					if ((protocol != null) && ((protocol.getSdpEntity() instanceof HandclaspProtocol))) {
						SecureKey sk = new SecureKey();
						HandclaspProtocol handclaspProtocol = (HandclaspProtocol)protocol.getSdpEntity();
						//接收 客户端公钥
						if ("1".equals(handclaspProtocol.getType())) {
							sk.initRSAkey();
							//客户端发送公钥数据
							String clientPublicKey = handclaspProtocol.getData();
							if(null == clientPublicKey || "".equals(clientPublicKey)){
								logger.warn("get client publicKey warn!");
							}
							//java 客户端
							if(protocol.getPlatformType() == PlatformType.Java){
								//服务器生成公/私钥,公钥传送给客户端
								sc.setServerPublicKey(sk.getStringPublicKey());
								sc.setServerPrivateKey(sk.getStringPrivateKey());
								sc.setClientPublicKey(clientPublicKey);
								handclaspProtocol.setData(sk.getStringPublicKey());//服务器端公钥
							}
							
							protocol.setSdpEntity(handclaspProtocol);
							response.setResponseBuffer(protocol.toBytes());
							context.setScfResponse(response);
							this.setInvokeAndFilter(context);
							logger.info("send server publieKey sucess!");
						} else if("2".equals(handclaspProtocol.getType())) {//接收权限文件
							//客户端加密授权文件
							String clientSecureInfo = handclaspProtocol.getData();
							if(null == clientSecureInfo || "".equals(clientSecureInfo)){
								logger.warn("get client secureKey warn!");
							}
							//授权文件客户端原文(服务器私钥解密)
							String sourceInfo = sk.decryptByPrivateKey(clientSecureInfo, sc.getServerPrivateKey());
							//校验授权文件是否相同
							//判断是否合法,如果合法服务器端生成DES密钥，通过客户端提供的公钥进行加密传送给客户端
							if(global.containsSecureMap(sourceInfo)){
								logger.info("secureKey is ok!");
								String desKey = StringUtils.getRandomNumAndStr(8);
								//设置当前channel属性
								sc.setDesKey(desKey);
								sc.setRights(true);
								handclaspProtocol.setData(sk.encryptByPublicKey(desKey, sc.getClientPublicKey()));
								protocol.setSdpEntity(handclaspProtocol);
								response.setResponseBuffer(protocol.toBytes());
								context.setScfResponse(response);
							} else {
								logger.error("It's bad secureKey!");
								contextException(context, protocol, response, "授权文件错误!");
							}
							this.setInvokeAndFilter(context);
						} else {
							logger.error("权限认证异常!");
							contextException(context, protocol, response, "权限认证 异常!");
						}
						
						response = null;
						sk = null;
						handclaspProtocol = null;
					}
				} else {
					logger.error("客户端没有启用权限认证!");
					contextException(context, protocol, response, "客户端没有启用权限认证!");
				}
			} else if (protocol != null && protocol.getSdpEntity() instanceof HandclaspProtocol) {
				logger.error("当前服务没有启用权限认证!");
				contextException(context, protocol, response, "当前服务没有启用权限认证!");
			}
		}
	}
	
	private void contextException(SCFContext context, Protocol protocol, SCFResponse response, String message) throws Exception {
		ExceptionProtocol ep = ExceptionHelper.createError(new Exception());
		ep.setErrorMsg(message);
		protocol.setSdpEntity(ep);
		response.setResponseBuffer(protocol.toBytes());
		context.setScfResponse(response);
		setInvokeAndFilter(context);
	}
	
	/**
	 * 设置是否可以被执行以及对应的执行过滤器的类型
	 * @param context - SCFContext
	 */
	public void setInvokeAndFilter(SCFContext context) {
		context.setExecFilter(ExecFilterType.None);
		context.setDoInvoke(false);
	}

}
