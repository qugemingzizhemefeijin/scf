package cg.zz.scf.server.util;

import java.util.regex.Pattern;

import cg.zz.scf.server.contract.context.Global;

/**
 * IP Table 工具类
 * @author chengang
 *
 */
public class IPTable {
	
	/**
	 * 允许IP验证规则
	 */
	private static Pattern allowPattern;
	
	/**
	 * 禁止IP验证规则
	 */
	private static Pattern forbidPattern;

	static {
		init();
	}

	public static void init() {
		String allowIP = Global.getInstance().getServiceConfig().getString("scf.iptable.allow.iplist");
		String forbidIP = Global.getInstance().getServiceConfig().getString("scf.iptable.forbid.iplist");
		allowIP = allowIP.replaceAll("\\.", "\\\\.").replaceAll(",", "|").replaceAll("\\*", "\\.\\*");

		forbidIP = forbidIP.replaceAll("\\.", "\\\\.").replaceAll(",", "|").replaceAll("\\*", "\\.\\*");

		if (allowIP != null && !allowIP.equalsIgnoreCase("")) {
			allowPattern = Pattern.compile(allowIP);
		} else {
			allowPattern = null; // for unit test
		}
		if (forbidIP != null && !forbidIP.equalsIgnoreCase("")) {
			forbidPattern = Pattern.compile(forbidIP);
		} else {
			forbidPattern = null; // for unit test
		}
	}

	/**
	 * 检查是否是允许的IP访问
	 * @param ip - String
	 * @return boolean
	 */
	public static boolean isAllow(String ip) {
		if (ip != null && !ip.equalsIgnoreCase("")) {
			boolean allowMatch = true;
			boolean forbidMatch = false;

			if (allowPattern != null) {
				allowMatch = allowPattern.matcher(ip).find();
			}
			if (forbidPattern != null) {
				forbidMatch = forbidPattern.matcher(ip).find();
			}

			return (allowMatch && !forbidMatch);
		}

		return false;
	}

	/**
	 * IP地址格式化
	 * @param ip - String
	 * @return String
	 */
	public static String formatIP(String ip) {
		ip = ip.replaceAll("/", "");
		ip = ip.substring(0, ip.lastIndexOf(":"));
		return ip;
	}

}
