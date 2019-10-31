package cg.zz.scf.server.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * UDP客户端
 * @author chengang
 *
 */
public class UDPClient {
	
	private String encode;
	private DatagramSocket sock = null;
	
	private InetSocketAddress addr = null;
	
	public static UDPClient getInstrance(String ip, int port, String encode) throws SocketException {
		UDPClient client = new UDPClient();
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
	
	public void send(String msg) throws IOException {
		byte[] buf = msg.getBytes(this.encode);
		send(buf);
	}

	public void send(byte[] buf) throws IOException {
		DatagramPacket dp = new DatagramPacket(buf, buf.length, this.addr);
		this.sock.send(dp);
	}

}
