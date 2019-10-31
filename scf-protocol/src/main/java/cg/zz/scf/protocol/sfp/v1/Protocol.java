package cg.zz.scf.protocol.sfp.v1;

import cg.zz.scf.protocol.compress.CompressBase;
import cg.zz.scf.protocol.serializer.SerializeBase;
import cg.zz.scf.protocol.sfp.enumeration.CompressType;
import cg.zz.scf.protocol.sfp.enumeration.PlatformType;
import cg.zz.scf.protocol.sfp.enumeration.SDPType;
import cg.zz.scf.protocol.sfp.enumeration.SerializeType;
import cg.zz.scf.protocol.utility.ByteConverter;
import cg.zz.scf.secure.DESCoderHelper;

/**
 * 版本一协义定义
 * 1byte(版本号) | 4byte(协义总长度) | 4byte(序列号) | 1byte(服务编号) | 1byte(消息体类型) | 1byte 所采用的压缩算法 | 1byte 序列化规则 | 1byte 平台(.net java ...) | n byte消息体 | 5byte(分界符)
 * 	0			1~4			5~8              	9                   10                   11                      12                     13
 * 消息头总长度:14byte
 * 
 *  协义总长度 = 消息头总长度 + 消息体总长度 (不包括分界符)
 *  
 *  尾分界符: 9, 11, 13, 17, 18
 *  
 *  版本号从ASCII > 48 开始标识
 *  
 * @author chengang
 *
 */
public class Protocol {
	
	/**
	 * 协议版本号
	 */
	public static final byte VERSION = 1;
	
	/**
	 * 协义头14个byte
	 */
	private final static int HEAD_STACK_LENGTH = 14;
	
	/**
	 * 消息总长度
	 */
	private int totalLen;
	
	/**
	 * sessionID
	 */
	private int sessionID;
	
	/**
	 * 服务ID
	 */
	private byte serviceID;
	
	/**
	 * sdp类型
	 */
	private SDPType sdpType;
	
	/**
	 * 压缩算法类型
	 */
	private CompressType compressType = CompressType.UnCompress;
	
	/**
	 * 序列化类型
	 */
	private SerializeType serializeType = SerializeType.SCFBinary;
	
	/**
	 * 平台类型
	 */
	private PlatformType platformType = PlatformType.Java;
	
	/**
	 * 数据内容
	 */
	private byte[] userData;
	
	/**
	 * 会话描述对象
	 */
	private Object sdpEntity;
	
	/**
	 * 默认构造函数
	 */
	public Protocol() {
		
	}
	
	public Protocol(int sessionId, byte serviceId, SDPType sdpType, CompressType compressType, SerializeType serializeType, PlatformType platformType, Object sdpEntity) {
		this.sdpEntity = sdpEntity;
		this.sessionID = sessionId;
		this.serviceID = serviceId;
		this.sdpType = sdpType;
		this.compressType = compressType;
		this.serializeType = serializeType;
		this.platformType = platformType;
	}
	
	public Protocol(int sessionId, byte serviceId, SDPType sdpType, CompressType compressType, SerializeType serializeType, PlatformType platformType, byte[] userData) {
		this.userData = userData;
		this.sessionID = sessionId;
		this.serviceID = serviceId;
		this.sdpType = sdpType;
		this.compressType = compressType;
		this.serializeType = serializeType;
		this.platformType = platformType;
	}
	
	public Protocol(int sessionId, byte serviceId, SDPType sdpType, Object sdpEntity) {
		this.sdpEntity = sdpEntity;
		this.sessionID = sessionId;
		this.sdpType = sdpType;
		this.serviceID = serviceId;
	}
	
	/**
	 * 组装返回协议内容
	 * @return byte[]
	 * @throws Exception
	 */
	public byte[] toBytes() throws Exception {
		int startIndex = 0;
		SerializeBase serialize = SerializeBase.getInstance(getSerializeType());
		CompressBase compress = CompressBase.getInstance(getCompressType());
		
		this.sdpType = SDPType.getSDPType(this.sdpEntity);
		byte[] sdpData = serialize.serialize(this.sdpEntity);
		
		sdpData = compress.zip(sdpData);
		int protocolLen = HEAD_STACK_LENGTH + sdpData.length;
		this.setTotalLen(protocolLen);
		
		byte[] data = new byte[protocolLen];
	        data[0] = Protocol.VERSION;
	        
	        startIndex += SFPStruct.Version;
	        System.arraycopy(ByteConverter.intToBytesLittleEndian(this.getTotalLen()), 0, data, startIndex, SFPStruct.TotalLen);
	        
	        startIndex += SFPStruct.TotalLen;
	        System.arraycopy(ByteConverter.intToBytesLittleEndian(this.getSessionID()), 0, data, startIndex, SFPStruct.SessionId);
	        
	        startIndex += SFPStruct.SessionId;
	        data[startIndex] = this.getServiceID();
	        
	        startIndex += SFPStruct.ServerId;
	        data[startIndex] = (byte) this.getSdpType().getNum();

	        startIndex += SFPStruct.SDPType;
	        data[startIndex] = (byte) this.getCompressType().getNum();

	        startIndex += SFPStruct.CompressType;
	        data[startIndex] = (byte) this.getSerializeType().getNum();

	        startIndex += SFPStruct.SerializeType;
	        data[startIndex] = (byte) this.getPlatformType().getNum();

	        startIndex += SFPStruct.Platform;
	        System.arraycopy(sdpData, 0, data, startIndex, protocolLen - startIndex);
	        
	        return data;
	}
	
	/**
	 * 读取协议信息并且组装Protocol对象
	 * @param data - 协议内容
	 * @return Protocol
	 * @throws Exception
	 */
	public static Protocol fromBytes(byte[] data) throws Exception {
		Protocol p = new Protocol();
		int startIndex = 0;
		if(data[startIndex] != Protocol.VERSION) {
	        	throw new Exception("协义版本错误");
	        }
		
		startIndex += SFPStruct.Version;//1
	        byte[] totalLengthByte = new byte[SFPStruct.TotalLen];
	        for (int i = 0; i < SFPStruct.TotalLen; i++) {
	            totalLengthByte[i] = data[startIndex + i];
	        }
	        p.setTotalLen(ByteConverter.bytesToIntLittleEndian(totalLengthByte));

	        startIndex += SFPStruct.TotalLen;//5
	        byte[] sessionIDByte = new byte[SFPStruct.SessionId];
	        for (int i = 0; i < SFPStruct.SessionId; i++) {
	            sessionIDByte[i] = data[startIndex + i];
	        }
	        p.setSessionID(ByteConverter.bytesToIntLittleEndian(sessionIDByte));

	        startIndex += SFPStruct.SessionId;//9
	        p.setServiceID(data[startIndex]);

	        startIndex += SFPStruct.ServerId;//10
	        p.setSdpType(SDPType.getSDPType(data[startIndex]));

	        startIndex += SFPStruct.SDPType;//11
	        CompressType ct = CompressType.getCompressType(data[startIndex]);
	        p.setCompressType(ct);

	        startIndex += SFPStruct.CompressType;//12
	        SerializeType st = SerializeType.getSerializeType(data[startIndex]);
	        p.setSerializeType(st);

	        startIndex += SFPStruct.SerializeType;//13
	        p.setPlatformType(PlatformType.getPlatformType(data[startIndex]));

	        startIndex += SFPStruct.Platform;//14

	        byte[] sdpData = new byte[data.length - startIndex];
	        System.arraycopy(data, startIndex, sdpData, 0, data.length - startIndex);
	        sdpData = CompressBase.getInstance(ct).unzip(sdpData);
	        p.setUserData(sdpData);
	        
	        SerializeBase serialize = SerializeBase.getInstance(st);
	        p.setSdpEntity(serialize.deserialize(sdpData, SDPType.getSDPClass(p.getSdpType())));
	        return p;
	}
	
	/**
	 * 加密重载方法
	 * @param rights - 是否启用权限认证
	 * @param desKey - DES密钥
	 * @return byte[]
	 * @throws Exception
	 */
	public byte[] toBytes(boolean rights, byte[] desKey) throws Exception {
		int startIndex = 0;
		SerializeBase serialize = SerializeBase.getInstance(this.getSerializeType());
	        CompressBase compress = CompressBase.getInstance(this.getCompressType());
	        
	        this.sdpType = SDPType.getSDPType(this.sdpEntity);
	        byte[] sdpData = serialize.serialize(this.sdpEntity);
	        
	        //数据加密
	        if(this.getSdpType().getNum() != SDPType.Handclasp.getNum() && rights && desKey != null){
	        	sdpData = DESCoderHelper.getInstance().encrypt(sdpData, desKey);//DES加密数据
	        }
	        
	        sdpData = compress.zip(sdpData);
	        int protocolLen = HEAD_STACK_LENGTH + sdpData.length;
	        this.setTotalLen(protocolLen);
	        
	        byte[] data = new byte[protocolLen];
	        data[0] = Protocol.VERSION;

	        startIndex += SFPStruct.Version;
	        System.arraycopy(ByteConverter.intToBytesLittleEndian(this.getTotalLen()), 0, data, startIndex, SFPStruct.TotalLen);

	        startIndex += SFPStruct.TotalLen;
	        System.arraycopy(ByteConverter.intToBytesLittleEndian(this.getSessionID()), 0, data, startIndex, SFPStruct.SessionId);

	        startIndex += SFPStruct.SessionId;
	        data[startIndex] = this.getServiceID();

	        startIndex += SFPStruct.ServerId;
	        data[startIndex] = (byte) this.getSdpType().getNum();

	        startIndex += SFPStruct.SDPType;
	        data[startIndex] = (byte) this.getCompressType().getNum();

	        startIndex += SFPStruct.CompressType;
	        data[startIndex] = (byte) this.getSerializeType().getNum();

	        startIndex += SFPStruct.SerializeType;
	        data[startIndex] = (byte) this.getPlatformType().getNum();

	        startIndex += SFPStruct.Platform;
	        System.arraycopy(sdpData, 0, data, startIndex, protocolLen - startIndex);

	        return data;
	}
	
	/**
	 * 解密重载方法
	 * @param data - 密文
	 * @param rights - 是否启用权限认证
	 * @param desKey - DES密钥
	 * @return Protocol
	 * @throws Exception
	 */
	public static Protocol fromBytes(byte[] data, boolean rights, byte[] desKey) throws Exception {
		Protocol p = new Protocol();
		int startIndex = 0;
	        if(data[startIndex] != Protocol.VERSION) {
	        	throw new Exception("协义版本错误");
	        }
	        
	        startIndex += SFPStruct.Version;//1
	        byte[] totalLengthByte = new byte[SFPStruct.TotalLen];
	        for (int i = 0; i < SFPStruct.TotalLen; i++) {
	            totalLengthByte[i] = data[startIndex + i];
	        }
	        p.setTotalLen(ByteConverter.bytesToIntLittleEndian(totalLengthByte));

	        startIndex += SFPStruct.TotalLen;//5
	        byte[] sessionIDByte = new byte[SFPStruct.SessionId];
	        for (int i = 0; i < SFPStruct.SessionId; i++) {
	            sessionIDByte[i] = data[startIndex + i];
	        }
	        p.setSessionID(ByteConverter.bytesToIntLittleEndian(sessionIDByte));

	        startIndex += SFPStruct.SessionId;//9
	        p.setServiceID(data[startIndex]);

	        startIndex += SFPStruct.ServerId;//10
	        p.setSdpType(SDPType.getSDPType(data[startIndex]));

	        startIndex += SFPStruct.SDPType;
	        CompressType ct = CompressType.getCompressType(data[startIndex]);
	        p.setCompressType(ct);

	        startIndex += SFPStruct.CompressType;
	        SerializeType st = SerializeType.getSerializeType(data[startIndex]);
	        p.setSerializeType(st);

	        startIndex += SFPStruct.SerializeType;
	        p.setPlatformType(PlatformType.getPlatformType(data[startIndex]));

	        startIndex += SFPStruct.Platform;

	        byte[] sdpData = new byte[data.length - startIndex];
	        System.arraycopy(data, startIndex, sdpData, 0, data.length - startIndex);
	        sdpData = CompressBase.getInstance(ct).unzip(sdpData);
	        
	        //数据解密
	        if(p.getSdpType().getNum() != SDPType.Handclasp.getNum() && rights && desKey !=null){
	        	sdpData = DESCoderHelper.getInstance().decrypt(sdpData, desKey);//DES解密数据
	        }
	        
	        p.setUserData(sdpData);
	        SerializeBase serialize = SerializeBase.getInstance(st);        
	        p.setSdpEntity(serialize.deserialize(sdpData, SDPType.getSDPClass(p.getSdpType())));
	        return p;
	}
	
	public int getVersion() {
		return VERSION;
	}

	public int getTotalLen() {
		return totalLen;
	}

	public void setTotalLen(int totalLen) {
		this.totalLen = totalLen;
	}

	public int getSessionID() {
		return sessionID;
	}

	public void setSessionID(int sessionID) {
		this.sessionID = sessionID;
	}

	public byte getServiceID() {
		return serviceID;
	}

	public void setServiceID(byte serviceID) {
		this.serviceID = serviceID;
	}

	public SDPType getSdpType() {
		return sdpType;
	}

	public void setSdpType(SDPType sdpType) {
		this.sdpType = sdpType;
	}

	public CompressType getCompressType() {
		return compressType;
	}

	public void setCompressType(CompressType compressType) {
		this.compressType = compressType;
	}

	public SerializeType getSerializeType() {
		return serializeType;
	}

	public void setSerializeType(SerializeType serializeType) {
		this.serializeType = serializeType;
	}

	public PlatformType getPlatformType() {
		return platformType;
	}

	public void setPlatformType(PlatformType platformType) {
		this.platformType = platformType;
	}

	public byte[] getUserData() {
		return userData;
	}

	public void setUserData(byte[] userData) {
		this.userData = userData;
	}

	public Object getSdpEntity() {
		return sdpEntity;
	}

	public void setSdpEntity(Object sdpEntity) {
		this.sdpEntity = sdpEntity;
	}

}
