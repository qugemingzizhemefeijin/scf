package cg.zz.scf.client.utility.logger;

public interface ILog {
	
	public abstract void fine(String str);

	public abstract void config(String str);

	public abstract void info(String str);

	public abstract void warning(String str);

	public abstract void debug(String str);

	public abstract void debug(String str, Throwable th);

	public abstract void info(String str, Throwable th);

	public abstract void warn(String str);

	public abstract void warn(String str, Throwable th);

	public abstract void error(String str);

	public abstract void error(String str, Throwable th);

	public abstract void error(Throwable str);

	public abstract void fatal(String str);

	public abstract void fatal(String str, Throwable th);

}
