package cg.zz.scf.server.contract.log;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Log4日志记录工具
 * @author chengang
 *
 */
public final class Log4jLogger implements ILog {
	
	private transient Logger logger = null;
	
	private static final String FQCN = Log4jLogger.class.getName();
	
	/**
	 * 构造函数
	 * @param cls - Class<?>
	 */
	public Log4jLogger(Class<?> cls) {
		this.logger = Logger.getLogger(cls);
	}
	
	/**
	 * 格式化输出日志
	 * @param msg - String
	 * @return String
	 */
	private String getLogMsg(String msg) {
		StringBuilder buf = new StringBuilder();
		buf.append(msg);
		
		//TODO 此处需要
		
		return buf.toString();
	}

	@Override
	public void fine(String message) {
		this.logger.log(FQCN, Level.DEBUG, getLogMsg(message), null);
	}

	@Override
	public void config(String message) {
		this.logger.log(FQCN, Level.DEBUG, getLogMsg(message), null);
	}

	@Override
	public void info(String message) {
		this.logger.log(FQCN, Level.INFO, getLogMsg(message), null);
	}

	@Override
	public void warning(String message) {
		this.logger.log(FQCN, Level.WARN, getLogMsg(message), null);
	}

	@Override
	public void debug(String message) {
		this.logger.log(FQCN, Level.DEBUG, getLogMsg(message), null);
	}

	@Override
	public void debug(String message, Throwable t) {
		this.logger.log(FQCN, Level.DEBUG, getLogMsg(message), t);
	}

	@Override
	public void info(String message, Throwable t) {
		this.logger.log(FQCN, Level.INFO, getLogMsg(message), t);
	}

	@Override
	public void warn(String message) {
		this.logger.log(FQCN, Level.WARN, getLogMsg(message), null);
	}

	@Override
	public void warn(String message, Throwable t) {
		this.logger.log(FQCN, Level.WARN, getLogMsg(message), t);
	}

	@Override
	public void error(String message) {
		this.logger.log(FQCN, Level.ERROR, getLogMsg(message), null);
	}

	@Override
	public void error(String message, Throwable t) {
		this.logger.log(FQCN, Level.ERROR, getLogMsg(message), t);
	}

	@Override
	public void error(Throwable t) {
		this.logger.log(FQCN, Level.ERROR, getLogMsg(""), t);
	}

	@Override
	public void fatal(String message) {
		this.logger.log(FQCN, Level.FATAL, getLogMsg(message), null);
	}

	@Override
	public void fatal(String message, Throwable t) {
		this.logger.log(FQCN, Level.FATAL, getLogMsg(message), t);
	}

}
