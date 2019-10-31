package cg.zz.scf.server.performance.monitorweb;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * 监控UDP客户端
 * @author chengang
 *
 */
@SuppressWarnings("unused")
public class MonitorUDPClient {
	
	private String encode;
	private DatagramSocket sock = null;
	
	private InetSocketAddress addr = null;
	
	 public static MonitorUDPClient getInstrance(String ip, int port, String encode) throws SocketException {
		 MonitorUDPClient client = new MonitorUDPClient();
		 client.encode = encode;
		 client.sock = new DatagramSocket();
		 client.addr = new InetSocketAddress(ip, port);
		 return client;
	 }
	 
	 public void close() {
		 this.sock.close();
	 }
	 
	 public void send(String msg, String encoding) throws Exception {
		 byte[] buf = msg.getBytes(encoding);
		 send(buf);
	 }
	 
	 public void send(String msg) throws Exception {
		 byte[] buf = msg.getBytes("utf-8");
		 send(buf);
	 }
	 
	 public void send(byte[] buf) throws IOException {
		 DatagramPacket dp = new DatagramPacket(buf, buf.length, this.addr);
		 this.sock.send(dp);
	 }

}
