package cg.zz.scf.client.proxy.builder;

import cg.zz.scf.client.loadbalance.Server;
import cg.zz.scf.client.loadbalance.component.ServerState;
import cg.zz.scf.protocol.exception.ThrowErrorHelper;
import cg.zz.scf.protocol.sdp.ExceptionProtocol;
import cg.zz.scf.protocol.sdp.ResponseProtocol;
import cg.zz.scf.protocol.sfp.enumeration.SDPType;
import cg.zz.scf.protocol.sfp.v1.Protocol;

/**
 * 数据接收处理器
 * @author chengang
 *
 */
public abstract class ReceiveHandler {
	
	final CallBackExecutor callBack = CallBackHelper.getInstance();
	private Server server;
	
	public void setServer(Server server) {
		this.server = server;
	}
	
	public void notify(final byte[] buffer) throws Exception {
		CallBackExecutor.callBackExe.execute(new Runnable() {
			public void run() {
				try {
					InvokeResult<Object> result = null;
					Protocol receiveP = Protocol.fromBytes(buffer);
					if (ReceiveHandler.this.server.getState() == ServerState.Testing) {
						ReceiveHandler.this.server.relive();
					}
					if (receiveP == null) {
						throw new Exception("userdatatype error!");
					}
					
					if (receiveP.getSdpType() == SDPType.Response) {
						ResponseProtocol rp = (ResponseProtocol) receiveP.getSdpEntity();
						result = new InvokeResult<Object>(rp.getResult(), rp.getOutpara());
					} else if (receiveP.getSdpType() == SDPType.Exception) {
						ExceptionProtocol ep = (ExceptionProtocol) receiveP.getSdpEntity();
						result = new InvokeResult<Object>(ThrowErrorHelper.throwServiceError(ep.getErrorCode(),ep.getErrorMsg()), null);
					} else if (receiveP.getSdpType() == SDPType.Reset) {
						try {
							ReceiveHandler.this.server.createReboot();
							return;
						} catch (Throwable e) {
							e.printStackTrace();
						}
					} else {
						result = new InvokeResult<Object>(new Exception("userdatatype error!"), null);
					}
					
					if (result.getResult() != null) {
						ReceiveHandler.this.callBack(result.getResult());
					}
				} catch (Exception e) {
					e.printStackTrace();
					try {
						ReceiveHandler.this.callBack(new InvokeResult<Object>(new Exception(e.getMessage()), null));
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});
	}
	
	/**
	 * 回调函数
	 * @param paramObject - 参数
	 */
	public abstract void callBack(Object paramObject);

}
