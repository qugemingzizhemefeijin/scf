package cg.zz.scf.client.utility.logger;

public interface ILog {
	
	public abstract void fine(String paramString);

	public abstract void config(String paramString);

	public abstract void info(String paramString);

	public abstract void warning(String paramString);

	public abstract void debug(String paramString);

	public abstract void debug(String paramString, Throwable paramThrowable);

	public abstract void info(String paramString, Throwable paramThrowable);

	public abstract void warn(String paramString);

	public abstract void warn(String paramString, Throwable paramThrowable);

	public abstract void error(String paramString);

	public abstract void error(String paramString, Throwable paramThrowable);

	public abstract void error(Throwable paramThrowable);

	public abstract void fatal(String paramString);

	public abstract void fatal(String paramString, Throwable paramThrowable);

}
