package io.github.pangju666.commons.codec.digest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.commons.CommonUtils;
import org.jasypt.digest.StringDigester;

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
 *        <li>执行签名（使用{@link #digest(String)}）或验证签名（使用{@link #matches(String, String)}）操作</li>
 *    </ol>
 * </p>
 * <br/>这个类是<i>线程安全的</i>
 *
 * @author pangju
 * @since 1.0.0
 */
public final class RSAStringDigester implements StringDigester {
	private final RSAByteDigester byteDigester;

	public RSAStringDigester() {
		this.byteDigester = new RSAByteDigester();
	}

	public RSAStringDigester(RSAByteDigester rsaByteDigester) {
		this.byteDigester = rsaByteDigester;
	}

	public void setPublicKey(byte[] publicKey) {
		byteDigester.setPublicKey(publicKey);
	}

	public void setPrivateKey(byte[] privateKey) {
		byteDigester.setPrivateKey(privateKey);
	}

	@Override
	public String digest(String message) {
		if (StringUtils.isBlank(message)) {
			return StringUtils.EMPTY;
		}
		return Base64.encodeBase64String(byteDigester.digest(message.getBytes(StandardCharsets.UTF_8)));
	}

	@Override
	public boolean matches(String message, String digest) {
		if (StringUtils.isBlank(message)) {
			return StringUtils.isBlank(digest);
		} else if (StringUtils.isBlank(digest)) {
			return false;
		}
		return byteDigester.matches(message.getBytes(StandardCharsets.UTF_8),
			Base64.decodeBase64(digest));
	}

	public String digestToHexadecimal(String message, boolean outputHex) {
		if (StringUtils.isBlank(message)) {
			return StringUtils.EMPTY;
		}
		return CommonUtils.toHexadecimal(byteDigester.digest(message.getBytes(StandardCharsets.UTF_8)));
	}

	public boolean matchesFromHexadecimal(String message, String digest, boolean inputHex) {
		if (StringUtils.isBlank(message)) {
			return StringUtils.isBlank(digest);
		} else if (StringUtils.isBlank(digest)) {
			return false;
		}
		return byteDigester.matches(message.getBytes(StandardCharsets.UTF_8),
			CommonUtils.fromHexadecimal(digest));
	}
}
