package cg.zz.scf.client;

public class SCFConst {
	
	/**
	 * 配置文件路径
	 */
	public static String CONFIG_PATH = SCFInit.DEFAULT_CONFIG_PATH;
	
	/**
	 * MAX_SESSIONID
	 */
	public static final long MAX_SESSIONID = 1024 * 1024 * 1024;
	
	/**
	 * 最大线程数
	 */
	public static final int DEFAULT_MAX_THREAD_COUNT = 2000;
	
	/**
	 * 最大并发用户数
	 */
	public static final int DEFAULT_MAX_CURRENT_USER_COUNT = 2000;
	
	/**
	 * Socket的发送缓存,1MB
	 */
	public static final int DEFAULT_MAX_PAKAGE_SIZE = 1024 * 1024;
	
	/**
	 * Socket的接收缓冲,10KB
	 */
	public static final int DEFAULT_BUFFER_SIZE = 10 * 1024;
	
	/**
	 * 服务的僵死超时时间 60秒
	 */
	public static final int DEFAULT_DEAD_TIMEOUT = 60000;
	
	/**
	 * 是否需要保护
	 */
	public static final boolean DEFAULT_PROTECTED = true;
	
	/**
	 * socket连接超时时间，毫秒
	 */
	public static final int SOCKET_CONNECT_TIMEOUT = 2000;
	
	/**
	 * 版本
	 */
	public static final String VERSION_FLAG = Version.ID;

}
