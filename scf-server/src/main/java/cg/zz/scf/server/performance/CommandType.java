package cg.zz.scf.server.performance;

/**
 * 命令枚举
 * @author chengang
 *
 */
public enum CommandType {
	
	Exec,

	Time,

	Count,

	CRLF,

	Control,

	Help,

	Quit,

	/**
	 * 不合法的命令
	 */
	Illegal,

	JVM,

	/**
	 * clear清理监控信息命令
	 */
	Clear;

}
