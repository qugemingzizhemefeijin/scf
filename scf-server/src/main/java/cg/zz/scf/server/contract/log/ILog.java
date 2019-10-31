package cg.zz.scf.server.contract.log;

/**
 * 日志接口
 * @author chengang
 *
 */
public abstract interface ILog {
	
	/**
	 * 输出好的提示信息
	 * @param message - String
	 */
	public abstract void fine(String message);
	
	/**
	 * 输出配置信息
	 * @param message - String
	 */
	public abstract void config(String message);
	
	/**
	 * 输出信息
	 * @param message - String
	 */
	public abstract void info(String message);
	
	/**
	 * 输出警告
	 * @param message - String
	 */
	public abstract void warning(String message);
	
	/**
	 * 输出调试信息
	 * @param message - String
	 */
	public abstract void debug(String message);
	
	/**
	 * 输出调试信息
	 * @param message - String
	 * @param t - Throwable
	 */
	public abstract void debug(String message, Throwable t);
	
	/**
	 * 输出信息
	 * @param message - String
	 * @param t - Throwable
	 */
	public abstract void info(String message, Throwable t);
	
	/**
	 * 输出警告提示
	 * @param message - String
	 */
	public abstract void warn(String message);
	
	/**
	 * 输出警告提示
	 * @param message - String
	 * @param t - Throwable
	 */
	public abstract void warn(String message, Throwable t);
	
	/**
	 * 输出导致程序出错的日志
	 * @param message - String
	 */
	public abstract void error(String message);
	
	/**
	 * 输出导致程序出错的日志
	 * @param message - String
	 * @param t - Throwable
	 */
	public abstract void error(String message, Throwable t);
	
	/**
	 * 输出导致程序出错的日志
	 * @param t - Throwable
	 */
	public abstract void error(Throwable t);
	
	/**
	 * 输出导致程序退出的错误日志
	 * @param message - String
	 */
	public abstract void fatal(String message);
	
	/**
	 * 输出导致程序退出的错误日志
	 * @param message - String
	 * @param t - Throwable
	 */
	public abstract void fatal(String message, Throwable t);

}
