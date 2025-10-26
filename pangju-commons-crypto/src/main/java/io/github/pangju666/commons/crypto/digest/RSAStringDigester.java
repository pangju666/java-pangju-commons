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

import io.github.pangju666.commons.crypto.enums.RsaSignatureAlgorithm;
import io.github.pangju666.commons.crypto.key.RSAKey;
import io.github.pangju666.commons.crypto.lang.CryptoConstants;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.digest.StringDigester;
import org.jasypt.exceptions.EncryptionInitializationException;

import java.nio.charset.StandardCharsets;

/**
 * RSA字符串签名处理器
 * <p>
 * 本类实现了基于RSA算法的字符串签名功能，提供以下核心能力：
 * <ul>
 *   <li><strong>字符串签名</strong> - 对字符串消息生成数字签名</li>
 *   <li><strong>签名验证</strong> - 验证字符串消息与签名的匹配性</li>
 *   <li><strong>多格式支持</strong> - 支持Base64和十六进制编码格式</li>
 *   <li><strong>线程安全</strong> - 所有操作都进行了同步控制</li>
 * </ul>
 *
 * <h3>典型使用场景：</h3>
 * <ul>
 *   <li>API请求参数签名验证</li>
 *   <li>配置文件完整性校验</li>
 *   <li>消息防篡改保护</li>
 * </ul>
 *
 * <h3>编码格式说明：</h3>
 * <ul>
 *   <li>{@link #digest(String)}/{@link #matches(String, String)} - 使用Base64编码</li>
 *   <li>{@link #digestToHexString(String)}/{@link #matchesFromHexString(String, String)} - 使用十六进制编码</li>
 * </ul>
 *
 * @author pangju666
 * @see StringDigester
 * @see RSAByteDigester
 * @see RSAKey
 * @since 1.0.0
 */
public final class RSAStringDigester implements StringDigester {
	/**
	 * 底层字节数组签名处理器
	 * <p>实际执行签名操作的核心组件，具有以下特性：</p>
	 * <ul>
	 *   <li>非null</li>
	 *   <li>线程安全</li>
	 *   <li>延迟初始化</li>
	 * </ul>
	 *
	 * @see RSAByteDigester
	 * @since 1.0.0
	 */
	private final RSAByteDigester byteDigester;

	/**
	 * 构造方法（使用默认配置）
	 * <p>创建使用默认密钥长度({@value CryptoConstants#RSA_DEFAULT_KEY_SIZE})和默认算法("SHA256withRSA")的实例</p>
	 *
	 * @see RSAByteDigester#RSAByteDigester()
	 * @since 1.0.0
	 */
	public RSAStringDigester() {
		this.byteDigester = new RSAByteDigester();
	}

	/**
	 * 构造方法（自定义算法，默认密钥长度）
	 * <p>使用默认密钥长度({@value CryptoConstants#RSA_DEFAULT_KEY_SIZE})和指定算法创建实例</p>
	 *
	 * @param algorithm 签名算法
	 * @throws NullPointerException 当algorithm为null时抛出
	 * @see CryptoConstants#RSA_DEFAULT_KEY_SIZE
	 * @since 1.0.0
	 */
	public RSAStringDigester(final RsaSignatureAlgorithm algorithm) {
		this.byteDigester = new RSAByteDigester(algorithm);
	}

	/**
	 * 构造方法（自定义密钥长度，默认算法）
	 * <p>使用指定密钥长度和默认算法("SHA256withRSA")创建实例</p>
	 *
	 * @param keySize RSA密钥位长度，推荐值：
	 *                <ul>
	 *                  <li>2048(商业应用最低要求)</li>
	 *                  <li>4096(高安全需求)</li>
	 *                </ul>
	 * @throws IllegalArgumentException 当keySize小于1024时抛出
	 * @since 1.0.0
	 */
	public RSAStringDigester(final int keySize) {
		this.byteDigester = new RSAByteDigester(keySize);
	}

	/**
	 * 构造方法（完全自定义配置）
	 * <p>使用指定密钥长度和算法创建实例</p>
	 *
	 * @param keySize   密钥位长度，最小1024
	 * @param algorithm 签名算法，非null
	 * @throws NullPointerException     当algorithm为null时抛出
	 * @throws IllegalArgumentException 当keySize小于1024时抛出
	 * @since 1.0.0
	 */
	public RSAStringDigester(final int keySize, final RsaSignatureAlgorithm algorithm) {
		this.byteDigester = new RSAByteDigester(keySize, algorithm);
	}

	/**
	 * 构造方法（使用现有密钥，默认算法）
	 * <p>使用预生成的RSA密钥对和默认算法("SHA256withRSA")创建实例</p>
	 *
	 * @param key RSA密钥对，必须满足：
	 *            <ul>
	 *              <li>非null</li>
	 *              <li>至少包含公钥或私钥</li>
	 *            </ul>
	 * @throws NullPointerException     当key为null时抛出
	 * @throws IllegalArgumentException 当key不包含任何密钥时抛出
	 * @since 1.0.0
	 */
	public RSAStringDigester(final RSAKey key) {
		this.byteDigester = new RSAByteDigester(key);
	}

	/**
	 * 构造方法（完全自定义密钥和算法）
	 * <p>使用预生成的RSA密钥对和指定算法创建实例</p>
	 *
	 * @param key       RSA密钥对，必须包含至少一个密钥
	 * @param algorithm 签名算法，非null
	 * @throws NullPointerException     当key或algorithm为null时抛出
	 * @throws IllegalArgumentException 当key不包含任何密钥时抛出
	 * @since 1.0.0
	 */
	public RSAStringDigester(final RSAKey key, final RsaSignatureAlgorithm algorithm) {
		this.byteDigester = new RSAByteDigester(key, algorithm);
	}

	/**
	 * 构造方法（自定义底层处理器）
	 * <p>使用指定的字节数组签名处理器创建实例</p>
	 *
	 * @param byteDigester 底层处理器，必须满足：
	 *                     <ul>
	 *                       <li>非null</li>
	 *                       <li>已配置有效密钥</li>
	 *                     </ul>
	 * @throws NullPointerException 当byteDigester为null时抛出
	 * @see RSAByteDigester
	 * @since 1.0.0
	 */
	public RSAStringDigester(final RSAByteDigester byteDigester) {
		this.byteDigester = byteDigester;
	}

	public RSAKey getKey() {
		return byteDigester.getKey();
	}

	/**
	 * 设置RSA密钥对
	 * <p>更新签名处理器使用的密钥对</p>
	 *
	 * @param key 新的密钥对，必须满足：
	 *            <ul>
	 *              <li>非null</li>
	 *              <li>至少包含公钥或私钥</li>
	 *            </ul>
	 * @throws NullPointerException 当key为null时抛出
	 * @throws IllegalArgumentException 当key不包含任何密钥时抛出
	 * @see RSAByteDigester#setKey(RSAKey)
	 * @since 1.0.0
	 */
	public void setKey(final RSAKey key) {
		byteDigester.setKey(key);
	}

	/**
	 * 设置签名算法
	 * <p>更新签名处理器使用的算法</p>
	 *
	 * @param algorithm 新的算法
	 * @throws NullPointerException 当algorithm为null时抛出
	 * @since 1.0.0
	 */
	public void setAlgorithm(final RsaSignatureAlgorithm algorithm) {
		byteDigester.setAlgorithm(algorithm);
	}

	/**
	 * 初始化签名组件
	 * <p>触发底层签名处理器的初始化操作，具有以下特性：</p>
	 * <ul>
	 *   <li>自动检测可用密钥</li>
	 *   <li>按需初始化签名/验证功能</li>
	 *   <li>线程安全</li>
	 * </ul>
	 * <p><strong>注意：</strong>此方法通常不需要显式调用，会在首次签名/验证时自动触发。</p>
	 *
	 * @throws EncryptionInitializationException 当以下情况发生时抛出：
	 *         <ul>
	 *           <li>未配置任何密钥</li>
	 *           <li>算法不支持</li>
	 *           <li>密钥无效</li>
	 *         </ul>
	 * @see RSAByteDigester#initialize()
	 * @since 1.0.0
	 */
	public void initialize() {
		byteDigester.initialize();
	}

	/**
	 * 生成Base64编码签名
	 * <p>对输入字符串生成Base64编码的数字签名</p>
	 *
	 * @param message 要签名的消息，空字符串返回空字符串
	 * @return Base64编码的签名结果，具有以下特性：
	 *         <ul>
	 *           <li>非null</li>
	 *           <li>空输入返回空字符串</li>
	 *         </ul>
	 * @see RSAByteDigester#digest(byte[])
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
	 * 验证Base64编码签名
	 * <p>验证消息与Base64编码签名的匹配性</p>
	 *
	 * @param message 原始消息
	 * @param digest  Base64编码的签名
	 * @return 验证结果，满足以下条件时返回true：
	 *         <ul>
	 *           <li>消息和签名都为空</li>
	 *           <li>签名与消息匹配</li>
	 *         </ul>
	 * @see RSAByteDigester#matches(byte[], byte[])
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
	 * 生成十六进制编码签名
	 * <p>对输入字符串生成十六进制编码的数字签名</p>
	 *
	 * @param message 要签名的消息，空字符串返回空字符串
	 * @return 十六进制编码的签名结果，具有以下特性：
	 *         <ul>
	 *           <li>非null</li>
	 *           <li>空输入返回空字符串</li>
	 *           <li>长度是原始签名的两倍</li>
	 *         </ul>
	 * @see RSAByteDigester#digest(byte[])
	 * @since 1.0.0
	 */
	public String digestToHexString(String message) {
		if (StringUtils.isBlank(message)) {
			return StringUtils.EMPTY;
		}
		return Hex.encodeHexString(byteDigester.digest(message.getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * 验证十六进制编码签名
	 * <p>验证消息与十六进制编码签名的匹配性</p>
	 *
	 * @param message 原始消息
	 * @param digest  十六进制编码的签名
	 * @return 验证结果
	 * @throws RuntimeException 当签名格式无效时抛出
	 * @see RSAByteDigester#matches(byte[], byte[])
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
