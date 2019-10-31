package cg.zz.scf.server.contract.log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

/**
 * 将普通输出重新定向到log4j中
 * @author chengang
 *
 */
public class SystemPrintStream extends PrintStream {
	
	/**
	 * 日志工具
	 */
	private ILog logger = LogFactory.getLogger(SystemPrintStream.class);
	
	/**
	 * System.out封装类
	 */
	private static PrintStream outInstance = new SystemPrintStream(System.out);
	
	/**
	 * System.err封装类
	 */
	private static PrintStream errInstance = new SystemPrintStream(System.err);

	/**
	 * 构造函数
	 * @param out - OutputStream
	 */
	public SystemPrintStream(OutputStream out) {
		super(out);
	}
	
	/**
	 * 将控制台输出重新封装，利用log4j输出日志
	 */
	public static void redirectToLog4j() {
		System.setOut(outInstance);
		System.setErr(errInstance);
	}
	
	public PrintStream append(char c) {
		return this;
	}
	
	public PrintStream append(CharSequence csq, int start, int end) {
		return this;
	}
	
	public PrintStream append(CharSequence csq) {
		return this;
	}
	
	public boolean checkError() {
		return false;
	}
	
	protected void clearError() {
		
	}
	
	public void close() {
		
	}
	
	public void flush() {
		
	}
	
	public PrintStream format(Locale l, String format, Object... args) {
		return this;
	}
	
	public PrintStream format(String format, Object... args) {
		return this;
	}
	
	public void print(boolean b) {
		println(b);
	}
	
	public void print(char c) {
		println(c);
	}
	
	public void print(char[] s) {
		println(s);
	}
	
	public void print(double d) {
		println(d);
	}
	
	public void print(float f) {
		println(f);
	}
	
	public void print(int i) {
		println(i);
	}
	
	public void print(long l) {
		println(l);
	}
	
	public void print(Object obj) {
		println(obj);
	}
	
	public void print(String s) {
		println(s);
	}
	
	public PrintStream printf(Locale l, String format, Object... args) {
		return this;
	}
	
	public PrintStream printf(String format, Object... args) {
		return this;
	}
	
	public void println() {
		
	}
	
	public void println(boolean x) {
		if (this == errInstance) {
			this.logger.error(String.valueOf(x));
		} else {
			this.logger.info(String.valueOf(x));
		}
	}
	
	public void println(char x) {
		if (this == errInstance) {
			this.logger.error(String.valueOf(x));
		} else {
			this.logger.info(String.valueOf(x));
		}
	}
	
	public void println(char[] x) {
		if (this == errInstance) {
			this.logger.error(x == null ? null : new String(x));
		} else {
			this.logger.info(x == null ? null : new String(x));
		}
	}
	
	public void println(double x) {
		if (this == errInstance) {
			this.logger.error(String.valueOf(x));
		} else {
			this.logger.info(String.valueOf(x));
		}
	}
	
	public void println(float x) {
		if (this == errInstance) {
			this.logger.error(String.valueOf(x));
		} else {
			this.logger.info(String.valueOf(x));
		}
	}
	
	public void println(int x) {
		if (this == errInstance) {
			this.logger.error(String.valueOf(x));
		} else {
			this.logger.info(String.valueOf(x));
		}
	}
	
	public void println(long x) {
		if (this == errInstance) {
			this.logger.error(String.valueOf(x));
		} else {
			this.logger.info(String.valueOf(x));
		}
	}
	
	public void println(Object x) {
		if (this == errInstance) {
			this.logger.error(String.valueOf(x));
		} else {
			this.logger.info(String.valueOf(x));
		}
	}
	
	public void println(String x) {
		if (this == errInstance) {
			this.logger.error(x);
		} else {
			this.logger.info(x);
		}
	}
	
	protected void setError() {
		
	}
	
	public void write(byte[] buf, int off, int len) {
		
	}
	
	public void write(int b) {
		
	}
	
	public void write(byte[] b) throws IOException {
		
	}

}
