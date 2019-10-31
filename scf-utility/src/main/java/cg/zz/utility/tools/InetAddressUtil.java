package cg.zz.utility.tools;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * 获取本机IP的工具类
 * @author chengang
 *
 */
public final class InetAddressUtil {
	
	/**
	 * 本机IP
	 */
	private static String localIP;

	/**
	 * 获得本机名称
	 * @return String
	 * @throws UnknownHostException
	 */
	public static String getLocalHostName() throws UnknownHostException {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException uhe) {
			String host = uhe.getMessage();
			if (host != null) {
				int colon = host.indexOf(':');
				if (colon > 0)
					return host.substring(0, colon);
			}
			throw uhe;
		}
	}

	/**
	 * 获得注册生成器key
	 * @param subject - String
	 * @return String
	 * @throws UnknownHostException
	 */
	public static String generatorRegistryKey(String subject) throws UnknownHostException {
		return localIP + ":" + subject;
	}

	/**
	 * 获得本机IP
	 * @return String
	 * @throws UnknownHostException
	 */
	public static String getHostAddress() throws UnknownHostException {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException uhe) {
			String host = uhe.getMessage();
			if (host != null) {
				int colon = host.indexOf(':');
				if (colon > 0)
					return host.substring(0, colon);
			}
			throw uhe;
		}
	}

	/**
	 * 获得IP地址的hashCode
	 * @return int
	 * @throws UnknownHostException
	 */
	public static int getHashCode() throws UnknownHostException {
		return String.valueOf(localIP).hashCode();
	}

	/**
	 * 获得机器的IP地址，如果未获取到则返回127.0.0.1
	 * @return String
	 */
	public static String getIpMixed() {
		Enumeration<NetworkInterface> netInterfaces = null;
		try {
			netInterfaces = NetworkInterface.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();
				Enumeration<InetAddress> ips = ni.getInetAddresses();
				if (!"eth0".equals(ni.getName()))
					if (!"eth1".equals(ni.getName()))
						continue;
					else
						while (ips.hasMoreElements()) {
							String strIp = ((InetAddress) ips.nextElement()).getHostAddress();
							if (strIp.split(Pattern.quote(".")).length > 3) {
								return strIp;
							}
						}
			}

			System.err.println("This application get default ip is :127.0.0.1");
			return "127.0.0.1";
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("This application get default ip :127.0.0.1");
		}
		return "127.0.0.1";
	}

}
