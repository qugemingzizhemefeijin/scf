package cg.zz.scf.server.secure;

import java.util.HashMap;
import java.util.Map;

import cg.zz.scf.secure.DESCoderHelper;

public class SecureKey {
	
	static RSACoderHelper rsaHelper = RSACoderHelper.getInstance();
	/**
	 * RSA密钥key
	 */
	Map<String,Object> map  = new HashMap<String,Object>();
	
	public SecureKey(){
		
	}
	
	public void initRSAkey() throws Exception{
		this.map = rsaHelper.initKey();
	}
	
	public String getRSAPrivateKeyAsNetFormat(String encodedPrivatekey){
		return rsaHelper.getRSAPrivateKeyAsNetFormat(encodedPrivatekey);
	}
	
	/**
	 * java 私钥转换为c#公钥
	 * @param encodedPrivatekey java私钥
	 * @return
	 */
	public String getRSAPublicKeyAsNetFormat(String encodedPrivatekey){
		return rsaHelper.getRSAPublicKeyAsNetFormat(encodedPrivatekey);
	}
	
	/**
	 * DES 加密
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptByDesKey(byte[] data, byte[] key) throws Exception{
		DESCoderHelper desHelper = DESCoderHelper.getInstance();
		return desHelper.encrypt(data, key);
	}
	
	/**
	 * DES 解密
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptByDesKey(byte[] data, byte[] key) throws Exception{
		DESCoderHelper desHelper = DESCoderHelper.getInstance();
		return desHelper.decrypt(data, key);
	}
	
	/**
	 * 公钥加密
	 * @param data原文
	 * @param key 公钥
	 * @return 密文byte[]
	 * @throws Exception
	 */
	public byte[] encryptByPublicKey(byte[] data,byte[] key) throws Exception{
		return rsaHelper.encryptByPublicKey(data, key);
	}
	
	/**
	 * 公钥加密
	 * @param data原文
	 * @param key 公钥
	 * @return 密文byte[]
	 * @throws Exception
	 */
	public String encryptByPublicKey(String data,String key) throws Exception{
		return rsaHelper.encryptByPublicKeyString(data, key);
	}
	
	/**
	 * 私钥解密
	 * @param data 密文
	 * @param key 私钥
	 * @return 原文byte[]
	 * @throws Exception
	 */
	public byte[] decryptByPrivateKey(byte[] data,byte[] key) throws Exception{
		return rsaHelper.decryptByPrivateKey(data, key);
	}
	
	/**
	 * 私钥解密
	 * @param data 密文
	 * @param key 私钥
	 * @return 原文byte[]
	 * @throws Exception
	 */
	public String decryptByPrivateKey(String data,String key) throws Exception{
		return rsaHelper.decryptByPrivateKey(data, key);
	}
	
	/**
	 * 获取公钥
	 * @return byte公钥
	 */
	public byte[] getPublicKey(){
		return (map == null)? null : rsaHelper.getPublicKey(map);
	}
	
	/**
	 * 获取私钥
	 * @return byte私钥
	 */
	public byte[] getPrivateKey(){
		return (map == null)? null : rsaHelper.getPrivateKey(map);
	}
	/**
	 * 获取公钥
	 * @return String公钥
	 */
	public String getStringPublicKey(){
		return (map == null)? null : rsaHelper.getStringPublicKey(map);
	}
	
	/**
	 * 获取私钥
	 * @return String私钥
	 */
	public String getStringPrivateKey(){
		return (map == null)? null : rsaHelper.getStringPrivateKey(map);
	}

}
