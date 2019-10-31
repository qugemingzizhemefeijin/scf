package cg.zz.scf.server.performance.monitorweb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import cg.zz.scf.protocol.utility.ByteConverter;
import cg.zz.scf.server.performance.exception.SerializeException;

/**
 * web监控的一个通信协议
 * @author chengang
 *
 */
public class MonitorProtocol {
	
	public static final int HEADER_LENGTH = 9;
	private byte version = 1;
	private int totalLen;
	private short type;
	private short exType;
	private byte[] body;
	
	public MonitorProtocol() {}
	
	public MonitorProtocol(short type) {
		this.type = type;
	}
	
	public MonitorProtocol(short type, short exType) {
		this.type = type;
		this.exType = exType;
		this.totalLen = MPStruct.getHeadLength();
	}
	
	public byte[] dataCreate(byte[] recv) throws SerializeException {
		ByteArrayOutputStream stream = null;
		try {
			stream = new ByteArrayOutputStream();
			 this.body = recv;
			 if (this.body != null) {
				 stream.write(ByteConverter.intToBytesBigEndian(HEADER_LENGTH + this.body.length));
			 } else {
				 stream.write(ByteConverter.intToBytesBigEndian(HEADER_LENGTH));
			 }
			 stream.write(this.version);
			 stream.write(ByteConverter.shortToBytesBigEndian(this.type));
			 stream.write(ByteConverter.shortToBytesBigEndian(this.exType));
			 
			 if (this.body != null) {
				 stream.write(this.body);
			 }
			 
			 return stream.toByteArray();
		} catch (Exception e) {
			throw new SerializeException(e);
		} finally {
			if (stream != null) {
				try {stream.close();} catch (IOException e) {throw new SerializeException(e);}
			}
		}
	}
	
	public static MonitorProtocol fromBytes(byte[] buf) throws Exception {
		int index = 0;
		
		int totalLen = ByteConverter.bytesToIntBigEndian(buf, index);
		index += 4;
		
		byte version = buf[index];
		index++;
		
		short type = ByteConverter.bytesToShortBigEndian(buf, index);
		index += 2;
		
		short exType = ByteConverter.bytesToShortBigEndian(buf, index);
		index += 2;
		
		byte[] body = new byte[totalLen - HEADER_LENGTH];
		
		if (body.length > 0) {
			System.arraycopy(buf, index, body, 0, totalLen - HEADER_LENGTH);
		}
		
		MonitorProtocol mp = new MonitorProtocol();
		mp.setVersion(version);
		mp.setType(type);
		mp.setExType(exType);
		mp.setBody(body);
		return mp;
	}

	public byte getVersion() {
		return version;
	}

	public void setVersion(byte version) {
		this.version = version;
	}

	public int getTotalLen() {
		return totalLen;
	}

	public void setTotalLen(int totalLen) {
		this.totalLen = totalLen;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}

	public short getExType() {
		return exType;
	}

	public void setExType(short exType) {
		this.exType = exType;
	}

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}

}
