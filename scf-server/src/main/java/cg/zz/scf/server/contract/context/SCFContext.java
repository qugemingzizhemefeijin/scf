package cg.zz.scf.server.contract.context;

import cg.zz.scf.protocol.sdp.RequestProtocol;
import cg.zz.scf.server.contract.http.HttpContext;
import cg.zz.scf.server.contract.server.IServerHandler;

/**
 * scf request response 上下文环境
 * @author chengang
 *
 */
public class SCFContext {
	
	/**
	 * 是否需要监控，在MonitorRequestFilter中设置
	 */
	private boolean monitor;
	
	/**
	 * 性能观察对象
	 */
	private StopWatch stopWatch = new StopWatch();
	
	/**
	 * SCF Request
	 */
	private SCFRequest scfRequest = new SCFRequest();
	
	/**
	 * SCF Response
	 */
	private SCFResponse scfResponse;
	
	/**
	 * 服务类型
	 */
	private ServerType serverType;
	
	/**
	 * 错误
	 */
	private Throwable error;
	
	/**
	 * netty通道
	 */
	private SCFChannel channel;
	
	/**
	 * 服务器处理类
	 */
	private IServerHandler serverHandler;
	
	/**
	 * 是否可以被执行
	 */
	private boolean isDoInvoke = true;
	
	/**
	 * 是否是异步的
	 */
	private boolean isAsyn = false;
	
	/**
	 * 是否被删除
	 */
	private boolean isDel = false;
	
	/**
	 * sessionID
	 */
	private int sessionID;
	
	/**
	 * http上下文内容
	 */
	private HttpContext httpContext;
	
	private ExecFilterType execFilter = ExecFilterType.All;
	
	public SCFContext(){
		
	}
	
	public SCFContext(SCFChannel channel) {
		this.channel = channel;
	}
	
	public SCFContext(byte[] requestBuffer, SCFChannel channel, ServerType serverType, IServerHandler handler) throws Exception {
		this.scfRequest.setRequestBuffer(requestBuffer);
		this.channel = channel;
		this.serverType = serverType;
		this.serverHandler = handler;
	}
	
	/**
	 * 从ThreadLocal里获取上下文环境
	 * @return SCFContext
	 */
	public static SCFContext getFromThreadLocal() {
		return Global.getInstance().getThreadLocal().get();
	}
	
	/**
	 * 设置当前线程的上下文环境
	 * @param context - SCFContext
	 */
	public static void setThreadLocal(SCFContext context) {
		Global.getInstance().getThreadLocal().set(context);
	}
	
	/**
	 * 移除当前线程上下文环境
	 */
	public static void removeThreadLocal() {
		Global.getInstance().getThreadLocal().remove();
	}
	
	/**
	 * 获得当前线程的sessionID并且从当前线程变量中删除上下文环境
	 * @return int
	 */
	public static int getThreadLocalID() {
		ThreadLocal<SCFContext> threadLocal = Global.getInstance().getThreadLocal();
		
		SCFContext context = threadLocal.get();
		threadLocal.remove();
		
		return context.getSessionID();
	}
	
	public boolean isMonitor() {
		return monitor;
	}

	public void setMonitor(boolean monitor) {
		this.monitor = monitor;
	}
	
	public SCFRequest getScfRequest() {
		return this.scfRequest;
	}
	
	public void setScfRequest(SCFRequest scfRequest) {
		this.scfRequest = scfRequest;
		
		RequestProtocol r = (RequestProtocol)scfRequest.getProtocol().getSdpEntity();
		this.stopWatch.setLookup(r.getLookup());
		this.stopWatch.setMethodName(r.getMethodName());
	}
	
	public SCFResponse getScfResponse() {
		return this.scfResponse;
	}
	
	public void setScfResponse(SCFResponse scfResponse) {
		this.scfResponse = scfResponse;
	}
	
	public StopWatch getStopWatch() {
		return this.stopWatch;
	}
	
	public void setDoInvoke(boolean isDoInvoke) {
		this.isDoInvoke = isDoInvoke;
	}
	
	public boolean isDoInvoke() {
		return this.isDoInvoke;
	}
	
	public void setError(Throwable error) {
		this.error = error;
	}
	
	public Throwable getError() {
		return this.error;
	}
	
	public void setServerType(ServerType requestType) {
		this.serverType = requestType;
	}
	
	public ServerType getServerType() {
		return this.serverType;
	}
	
	public void setServerHandler(IServerHandler responseHandler) {
		this.serverHandler = responseHandler;
	}
	
	public IServerHandler getServerHandler() {
		return this.serverHandler;
	}
	
	public void setChannel(SCFChannel channel) {
		this.channel = channel;
	}
	
	public SCFChannel getChannel() {
		return this.channel;
	}
	
	public void setExecFilter(ExecFilterType execFilter) {
		this.execFilter = execFilter;
	}
	
	public ExecFilterType getExecFilter() {
		return this.execFilter;
	}
	
	public HttpContext getHttpContext() {
		return this.httpContext;
	}
	
	public void setHttpContext(HttpContext httpContext) {
		this.httpContext = httpContext;
	}
	
	public boolean isAsyn() {
		return this.isAsyn;
	}
	
	public void setAsyn(boolean isAsyn) {
		this.isAsyn = isAsyn;
	}
	
	public boolean isDel() {
		return this.isDel;
	}
	
	public void setDel(boolean isDel) {
		this.isDel = isDel;
	}

	/**
	 * 获得当前的sessionID
	 * @return int
	 */
	public int getSessionID() {
		return this.sessionID;
	}
	
	/**
	 * 设置sessionID
	 * @param sessionID - int
	 */
	public void setSessionID(int sessionID) {
		this.sessionID = sessionID;
	}

}
