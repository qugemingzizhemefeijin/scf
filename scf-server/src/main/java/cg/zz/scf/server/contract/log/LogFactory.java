package cg.zz.scf.server.contract.log;

/**
 * 日志工厂
 * @author chengang
 *
 */
public final class LogFactory {
	
	/**
	 * 获得日志工具对象
	 * @param cls - Class<?>
	 * @return ILog
	 */
	public static ILog getLogger(Class<?> cls) {
		return new Log4jLogger(cls);
	}

}
