package io.github.pangju666.commons.codec.encryption.text;

import io.github.pangju666.commons.codec.encryption.binary.RSABinaryEncryptor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.commons.CommonUtils;
import org.jasypt.util.text.TextEncryptor;

import java.nio.charset.StandardCharsets;

/**
 * RSA算法文本加密器（公钥加密，私钥解密）
 * <p>默认以BASE64格式返回结果</p>
 * <p>
 * 使用步骤：
 *    <ol>
 *        <li>创建一个实例（使用new）</li>
 *        <li>设置公钥（使用{@link #setPublicKey(byte[])}）<b>提示：</b>如果只需要解密可省略该操作</li>
 *        <li>设置私钥（使用{@link #setPrivateKey(byte[])}）<b>提示：</b>如果只需要加密可省略该操作</li>
 *        <li>执行加密（使用{@link #encrypt(String)}）或解密（使用{@link #decrypt(String)}）操作</li>
 *    </ol>
 * </p>
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

	public RSATextEncryptor(RSABinaryEncryptor rsaBinaryEncryptor) {
		this.binaryEncryptor = rsaBinaryEncryptor;
	}

	public void setPublicKey(final byte[] publicKey) {
		binaryEncryptor.setPublicKey(publicKey);
	}

	public void setPrivateKey(final byte[] privateKey) {
		binaryEncryptor.setPrivateKey(privateKey);
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
