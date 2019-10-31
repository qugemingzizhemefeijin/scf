package cg.zz.scf.server.secure;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class DESHelper {
	
	private Cipher encryptCipher = null;
	private Cipher decryptCipher = null;

	/**
	 * 获取实例
	 * 
	 * @param key 密钥，长度必须是8的倍数
	 * @return
	 * @throws Exception
	 */
	public static DESHelper getInstance(String key) throws Exception {
		return new DESHelper(key);
	}

	/**
	 * 
	 * @param key 密钥，长度必须是8的倍数
	 * @throws Exception
	 */
	private DESHelper(String key) throws Exception {
		if (key == null || key.length() % 8 != 0) {
			throw new Exception("key不合法, 长度必须是8的倍数");
		}
		byte[] bufKey = key.getBytes("utf-8");

		{
			SecureRandom sr = new SecureRandom();
			DESKeySpec dks = new DESKeySpec(bufKey);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey securekey = keyFactory.generateSecret(dks);
			encryptCipher = Cipher.getInstance("DES");
			encryptCipher.init(Cipher.ENCRYPT_MODE, securekey, sr);
		}

		{
			SecureRandom sr = new SecureRandom();
			DESKeySpec dks = new DESKeySpec(bufKey);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey securekey = keyFactory.generateSecret(dks);
			decryptCipher = Cipher.getInstance("DES");
			decryptCipher.init(Cipher.DECRYPT_MODE, securekey, sr);
		}
	}

	/**
	 * 加密
	 * 
	 * @param src 原文
	 * @return 密文
	 * @throws Exception
	 */
	public byte[] encrypt(byte[] src) throws Exception {
		return encryptCipher.doFinal(src);
	}

	/**
	 * 解密
	 * 
	 * @param dest 密文
	 * @return 原文
	 * @throws Exception
	 */
	public byte[] decrypt(byte[] dest) throws Exception {
		return decryptCipher.doFinal(dest);
	}

}
