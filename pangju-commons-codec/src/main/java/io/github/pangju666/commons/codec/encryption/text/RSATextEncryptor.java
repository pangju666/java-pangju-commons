package io.github.pangju666.commons.codec.encryption.text;

import io.github.pangju666.commons.codec.encryption.binary.RSABinaryEncryptor;
import io.github.pangju666.commons.codec.key.RSAKey;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.commons.CommonUtils;
import org.jasypt.util.text.TextEncryptor;

import java.nio.charset.StandardCharsets;

/**
 * RSA算法文本加密器（公钥加密，私钥解密）
 * <br/>这个类是<i>线程安全的</i>
 *
 * @author pangju
 * @since 1.0.0
 */
public final class RSATextEncryptor implements TextEncryptor {
	private final RSABinaryEncryptor binaryEncryptor;

	public RSATextEncryptor() {
		this.binaryEncryptor = new RSABinaryEncryptor();
	}

	public RSATextEncryptor(RSAKey key) {
		this.binaryEncryptor = new RSABinaryEncryptor(key);
	}

	public void setKey(RSAKey key) {
		binaryEncryptor.setKey(key);
	}

	public void setAlgorithm(String algorithm) {
		binaryEncryptor.setAlgorithm(algorithm);
	}

	@Override
	public String encrypt(final String message) {
		if (StringUtils.isBlank(message)) {
			return StringUtils.EMPTY;
		}
		return Base64.encodeBase64String(
			binaryEncryptor.encrypt(message.getBytes(StandardCharsets.UTF_8)));
	}

	@Override
	public String decrypt(final String encryptedMessage) {
		if (StringUtils.isBlank(encryptedMessage)) {
			return StringUtils.EMPTY;
		}
		return new String(binaryEncryptor.decrypt(
			Base64.decodeBase64(encryptedMessage)), StandardCharsets.UTF_8);
	}

	public String encryptToHexadecimal(final String message) {
		if (StringUtils.isBlank(message)) {
			return StringUtils.EMPTY;
		}
		return CommonUtils.toHexadecimal(binaryEncryptor.encrypt(message.getBytes(StandardCharsets.UTF_8)));
	}

	public String decryptFromHexadecimal(final String encryptedMessage) {
		if (StringUtils.isBlank(encryptedMessage)) {
			return StringUtils.EMPTY;
		}
		return new String(binaryEncryptor.decrypt(
			CommonUtils.fromHexadecimal(encryptedMessage)), StandardCharsets.UTF_8);
	}
}