/*
 *   Copyright 2025 pangju666
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.pangju666.commons.crypto.digest;

import io.github.pangju666.commons.crypto.key.RSAKey;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.digest.StringDigester;

import java.nio.charset.StandardCharsets;

/**
 * 基于RSA算法的字符串摘要处理器，用于执行消息签名和验证操作
 * <p>
 * 该类是线程安全的
 * </p>
 *
 * @author pangju666
 * @see StringDigester
 * @see RSAByteDigester
 * @since 1.0.0
 */
public final class RSAStringDigester implements StringDigester {
	/**
	 * 底层字节数组摘要处理器实例
	 *
	 * @since 1.0.0
	 */
	private final RSAByteDigester byteDigester;

	/**
	 * 创建使用默认配置的摘要处理器
	 */
	public RSAStringDigester() {
		this.byteDigester = new RSAByteDigester();
	}

	/**
	 * 使用指定的字节数组摘要处理器创建实例
	 *
	 * @param byteDigester 底层字节数组摘要处理器（非空）
	 */
	public RSAStringDigester(RSAByteDigester byteDigester) {
		this.byteDigester = byteDigester;
	}

	/**
	 * 设置RSA密钥对
	 *
	 * @param key RSA密钥对（包含公钥和私钥）
	 */
	public void setKey(RSAKey key) {
		byteDigester.setKey(key);
	}

	/**
	 * 设置签名算法（如SHA256withRSA）
	 *
	 * @param algorithm 要使用的签名算法名称
	 */
	public void setAlgorithm(String algorithm) {
		byteDigester.setAlgorithm(algorithm);
	}

	/**
	 * 对消息进行签名，返回Base64编码的签名结果
	 *
	 * @param message 要签名的原始消息
	 * @return Base64编码的签名结果，空消息返回空字符串
	 * @since 1.0.0
	 */
	@Override
	public String digest(String message) {
		if (StringUtils.isBlank(message)) {
			return StringUtils.EMPTY;
		}
		return Base64.encodeBase64String(byteDigester.digest(message.getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * 验证消息与Base64编码的签名是否匹配
	 *
	 * @param message 原始消息
	 * @param digest  Base64编码的待验证签名
	 * @return 验证结果（true=匹配，false=不匹配）
	 * @since 1.0.0
	 */
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

	/**
	 * 对消息进行签名，返回十六进制字符串形式的签名结果
	 *
	 * @param message 要签名的原始消息
	 * @return 十六进制编码的签名结果，空消息返回空字符串
	 * @since 1.0.0
	 */
	public String digestToHexString(String message) {
		if (StringUtils.isBlank(message)) {
			return StringUtils.EMPTY;
		}
		return Hex.encodeHexString(byteDigester.digest(message.getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * 验证消息与十六进制编码的签名是否匹配
	 *
	 * @param message 原始消息
	 * @param digest  十六进制编码的待验证签名
	 * @return 验证结果（true=匹配，false=不匹配）
	 * @throws RuntimeException 当十六进制解码失败时抛出
	 * @since 1.0.0
	 */
	public boolean matchesFromHexString(String message, String digest) {
		if (StringUtils.isBlank(message)) {
			return StringUtils.isBlank(digest);
		} else if (StringUtils.isBlank(digest)) {
			return false;
		}
		try {
			return byteDigester.matches(message.getBytes(StandardCharsets.UTF_8),
				Hex.decodeHex(digest));
		} catch (DecoderException e) {
			throw new RuntimeException(e);
		}
	}
}
