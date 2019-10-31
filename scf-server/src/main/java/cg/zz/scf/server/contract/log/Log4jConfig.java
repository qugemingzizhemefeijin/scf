package cg.zz.scf.server.contract.log;

import org.apache.log4j.xml.DOMConfigurator;

/**
 * 加载log4j配置文件
 * @author chengang
 *
 */
public class Log4jConfig {
	
	/**
	 * 加载配置文件
	 * @param configFilePath - 配置文件路径
	 */
	public static void configure(String configFilePath) {
		DOMConfigurator.configure(configFilePath);
	}

}
