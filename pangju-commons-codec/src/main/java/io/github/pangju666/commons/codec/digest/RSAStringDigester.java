package io.github.pangju666.commons.codec.digest;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.digest.StringDigester;
import org.jasypt.exceptions.AlreadyInitializedException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

import java.nio.charset.StandardCharsets;

/**
 * RSA算法文本消息签名器（私钥签名，公钥验证签名）
 * <p>默认以BASE64格式返回结果</p>
 * <p>
 * 使用步骤：
 *    <ol>
 *        <li>创建一个实例（使用new）</li>
 *        <li>设置公钥（使用{@link #setPublicKey(byte[])}）<b>提示：</b>如果只需要签名可省略该操作</li>
 *        <li>设置私钥（使用{@link #setPrivateKey(byte[])}）<b>提示：</b>如果只需要验证签名可省略该操作</li>
 *        <li>初始化（使用{@link #initialize()}）<b>提示：</b>一旦加密器初始化，尝试更改密钥将导致抛出{@link AlreadyInitializedException}</li>
 *        <li>执行签名（使用{@link #digest(String)}）或验证签名（使用{@link #matches(String, String)}）操作</li>
 *    </ol>
 * </p>
 * <br/>这个类是<i>线程安全的</i>
 *
 * @author pangju
 * @since 1.0.0
 */
public final class RSAStringDigester implements StringDigester {
	private final RSAByteDigester byteDigester = new RSAByteDigester();

	public void setPublicKey(byte[] publicKey) {
		byteDigester.setPublicKey(publicKey);
	}

	public void setPrivateKey(byte[] privateKey) {
		byteDigester.setPrivateKey(privateKey);
	}

	public void initialize() {
		byteDigester.initialize();
	}

	@Override
	public String digest(String message) {
		return digest(message, false);
	}

	@Override
	public boolean matches(String message, String digest) {
		return matches(message, digest, false);
	}

	public String digest(String message, boolean outputHex) {
		if (StringUtils.isBlank(message)) {
			return StringUtils.EMPTY;
		}
		if (!outputHex) {
			return Base64.encodeBase64String(byteDigester.digest(message.getBytes(StandardCharsets.UTF_8)));
		}
		return Hex.encodeHexString(byteDigester.digest(message.getBytes(StandardCharsets.UTF_8)));
	}

	public boolean matches(String message, String digest, boolean inputHex) {
		if (StringUtils.isBlank(message)) {
			return StringUtils.isBlank(digest);
		} else if (StringUtils.isBlank(digest)) {
			return false;
		}
		if (!inputHex) {
			return byteDigester.matches(message.getBytes(StandardCharsets.UTF_8), Base64.decodeBase64(digest));
		}
		try {
			return byteDigester.matches(message.getBytes(StandardCharsets.UTF_8), Hex.decodeHex(digest));
		} catch (DecoderException e) {
			throw new EncryptionOperationNotPossibleException(e);
		}
	}
}
