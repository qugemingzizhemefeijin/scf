package cg.zz.scf.client.utility.logger;

public final class LogFactory {
	
	public static ILog getLogger(Class<?> clazz) {
		return new FileLog(clazz);
	}

}
