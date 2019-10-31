package cg.zz.scf.server.secure;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

/**
 * 加密辅助类
 * @author chengang
 *
 */
@SuppressWarnings("unused")
public class DESCoderHelper {

	public static final String KEY_ALGORITHM = "DES";
	public static final String CIPHER_ALGORITHM_ECB = "DES/ECB/PKCS5Padding";
	public static final String CIPHER_ALGORITHM_CBC = "DES/CBC/PKCS5Padding";

	public static DESCoderHelper getInstance() {
		return new DESCoderHelper();
	}

	private Key toKey(byte[] key) throws Exception {
		DESKeySpec dks = new DESKeySpec(key);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");

		SecretKey secretKey = skf.generateSecret(dks);
		return secretKey;
	}

	private byte[] initkey() throws Exception {
		KeyGenerator kg = KeyGenerator.getInstance("DES");
		kg.init(56);
		SecretKey secretKey = kg.generateKey();
		return secretKey.getEncoded();
	}

	public String initkeyString() throws Exception {
		return Base64.encodeBase64String(initkey());
	}

	public String initkeyString(String key) throws Exception {
		return Base64.encodeBase64String(initkey(key));
	}

	public byte[] initkey(String key) throws Exception {
		if ((key == null) || (key.getBytes().length < 8)) {
			throw new Exception("key不合法, 长度必须大于8个字节!");
		}
		byte[] bufKey = key.getBytes("UTF-8");
		DESKeySpec dks = new DESKeySpec(bufKey);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey securekey = keyFactory.generateSecret(dks);
		return securekey.getEncoded();
	}

	public byte[] encrypt(byte[] data, byte[] key) throws Exception {
		Key k = toKey(key);
		Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
		cipher.init(1, k);
		return cipher.doFinal(data);
	}

	public String encryptString(byte[] data, byte[] key) throws Exception {
		return Base64.encodeBase64String(encrypt(data, key));
	}

	public String encryptString(String data, String key) throws Exception {
		return encryptString(data.getBytes("UTF-8"), key.getBytes("UTF-8"));
	}

	public byte[] decrypt(byte[] data, byte[] key) throws Exception {
		Key k = toKey(key);
		Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
		cipher.init(2, k);
		return cipher.doFinal(data);
	}

	private String decrypt(String data, String key) throws Exception {
		return new String(decrypt(Base64.decodeBase64(data), key.getBytes("UTF-8")));
	}

	public byte[] encryptNetByte(String data, String key) throws Exception {
		keyLength(key);
		return encryptNet(data.getBytes("UTF-8"), key.getBytes("UTF-8"));
	}

	public String encryptNetString(String data, String key) throws Exception {
		keyLength(key);
		return Base64.encodeBase64String(encryptNetByte(data, key));
	}

	private byte[] encryptNet(byte[] data, byte[] key) throws Exception {
		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		DESKeySpec desKeySpec = new DESKeySpec(key);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
		IvParameterSpec iv = new IvParameterSpec(key);
		cipher.init(1, secretKey, iv);
		return cipher.doFinal(data);
	}

	private byte[] decryptNet(byte[] data, byte[] key) throws Exception {
		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		DESKeySpec desKeySpec = new DESKeySpec(key);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
		IvParameterSpec iv = new IvParameterSpec(key);
		cipher.init(2, secretKey, iv);
		return cipher.doFinal(data);
	}

	public String decryptNetString(String data, String key) throws Exception {
		keyLength(key);
		return new String(decryptNet(Base64.decodeBase64(data), key.getBytes("UTF-8")));
	}

	public String decryptNetString(byte[] data, byte[] key) throws Exception {
		keyLength(key);
		return new String(decryptNet(data, key));
	}

	private void keyLength(String key) throws Exception {
		keyLength(key.getBytes("UTF-8"));
	}

	private void keyLength(byte[] key) throws Exception {
		if (key.length != 8)
			throw new Exception("key length must be 8 bytes long!");
	}

	public static void main(String[] args) throws Exception {
		DESCoderHelper descode = getInstance();
		String key = descode.initkeyString();
		System.out.println("密钥:" + key);
		System.out.println(descode.encryptNetString("58同城一个神奇的网站  very good! oh haha $&**&", "tsyg=af$"));
	}

}
