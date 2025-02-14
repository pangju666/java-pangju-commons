package io.github.pangju666.commons.codec.key;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.jasypt.commons.CommonUtils;

import java.util.Objects;

public class RSAKey {
	protected byte[] publicKey;
	protected byte[] privateKey;

	public static RSAKey of(byte[] publicKey, byte[] privateKey) {
		RSAKey key = new RSAKey();
		key.setPublicKey(publicKey);
		key.setPrivateKey(privateKey);
		return key;
	}

	public static RSAKey fromBase64(String publicKey, String privateKey) {
		RSAKey key = new RSAKey();
		key.setPublicKeyFromBase64(publicKey);
		key.setPrivateKeyFromBase64(privateKey);
		return key;
	}

	public static RSAKey fromHex(String publicKey, String privateKey) {
		RSAKey key = new RSAKey();
		key.setPublicKeyFromHex(publicKey);
		key.setPrivateKeyFromHex(privateKey);
		return key;
	}

	public byte[] getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(byte[] publicKey) {
		Validate.isTrue(ArrayUtils.isNotEmpty(publicKey), "公钥不可为空");
		if (Objects.nonNull(this.publicKey)) {
			cleanPublicKey();
		}
		this.publicKey = new byte[publicKey.length];
		System.arraycopy(publicKey, 0, this.publicKey, 0, publicKey.length);
	}

	public byte[] getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(byte[] privateKey) {
		Validate.isTrue(ArrayUtils.isNotEmpty(privateKey), "私钥不可为空");
		if (Objects.nonNull(this.privateKey)) {
			cleanPrivateKey();
		}
		this.privateKey = new byte[privateKey.length];
		System.arraycopy(privateKey, 0, this.privateKey, 0, privateKey.length);
	}

	public void setPublicKeyFromHex(String privateKey) {
		setPublicKey(CommonUtils.fromHexadecimal(privateKey));
	}

	public void setPublicKeyFromBase64(String privateKey) {
		setPublicKey(Base64.decodeBase64(privateKey));
	}

	protected void cleanPublicKey() {
		if (Objects.nonNull(this.publicKey)) {
			byte[] key = this.publicKey;
			synchronized (key) {
				final int keyLength = key.length;
				for (int i = 0; i < keyLength; i++) {
					key[i] = (byte) 0;
				}
			}
		}
	}

	protected void cleanPrivateKey() {
		if (Objects.nonNull(this.privateKey)) {
			byte[] key = this.privateKey;
			synchronized (key) {
				final int keyLength = key.length;
				for (int i = 0; i < keyLength; i++) {
					key[i] = (byte) 0;
				}
			}
		}
	}

	public void setPrivateKeyFromHex(String privateKey) {
		setPrivateKey(CommonUtils.fromHexadecimal(privateKey));
	}

	public void setPrivateKeyFromBase64(String privateKey) {
		setPrivateKey(Base64.decodeBase64(privateKey));
	}
}