package cg.zz.scf.client.configuration.commmunication;

import java.nio.charset.Charset;

import javax.naming.directory.NoSuchAttributeException;

import org.w3c.dom.Node;

import cg.zz.scf.protocol.serializer.SerializeBase;
import cg.zz.scf.protocol.sfp.enumeration.CompressType;
import cg.zz.scf.protocol.sfp.enumeration.SerializeType;

/**
 * 协议信息
 * @author chengang
 *
 */
public class ProtocolProfile {
	
	private SerializeType serializerType;
	private SerializeBase serialize;
	public Charset Encoder;
	public byte serviceID;
	public CompressType compress;
	
	public ProtocolProfile(Node node) throws Exception {
		Node attrSer = node.getAttributes().getNamedItem("serialize");
		if (attrSer == null) {
			throw new ExceptionInInitializerError("Not find attrbuts:" + node.getNodeName() + "[@'serialize']");
		}
		String value = attrSer.getNodeValue().trim().toLowerCase();
		if (value.equalsIgnoreCase("binary"))
			this.serializerType = SerializeType.JAVABinary;
		else if (value.equalsIgnoreCase("json"))
			this.serializerType = SerializeType.JSON;
		else if (value.equalsIgnoreCase("xml"))
			this.serializerType = SerializeType.XML;
		else if (value.equalsIgnoreCase("scf"))
			this.serializerType = SerializeType.SCFBinary;
		else if (value.equalsIgnoreCase("scfv2"))
			this.serializerType = SerializeType.SCFBinaryV2;
		else {
			throw new NoSuchAttributeException("Protocol not supported " + value + "!");
		}
		
		this.serialize = SerializeBase.getInstance(this.serializerType);
		attrSer = node.getAttributes().getNamedItem("encoder");
		if (attrSer == null)
			this.Encoder = Charset.forName("UTF-8");
		else {
			this.Encoder = Charset.forName(attrSer.getNodeValue());
		}
		this.serialize.setEncoder(this.Encoder);
		this.serviceID = Byte.parseByte(node.getParentNode().getParentNode().getAttributes().getNamedItem("id").getNodeValue());
		this.compress = Enum.valueOf(CompressType.class, node.getAttributes().getNamedItem("compressType").getNodeValue());
	}
	
	public Charset getEncoder() {
		return this.Encoder;
	}

	public CompressType getCompress() {
		return this.compress;
	}

	public SerializeBase getSerializer() {
		return this.serialize;
	}

	public SerializeType getSerializerType() {
		return this.serializerType;
	}

	public byte getServiceID() {
		return this.serviceID;
	}

}
