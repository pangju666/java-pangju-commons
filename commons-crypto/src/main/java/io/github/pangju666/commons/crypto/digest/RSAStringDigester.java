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

import io.github.pangju666.commons.crypto.enums.RSASignatureAlgorithm;
import io.github.pangju666.commons.crypto.key.RSAKeyPair;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jasypt.digest.StringDigester;
import org.jasypt.exceptions.AlreadyInitializedException;
import org.jasypt.exceptions.EncryptionInitializationException;

import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * RSA 字符串签名处理器
 * <p>基于 RSA 的字符串签名与验证，仅支持标准 Base64 编码输出。</p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li>签名生成：私钥对字符串签名</li>
 *   <li>签名验证：公钥验签</li>
 *   <li>编码规范：RFC 4648 标准 Base64（包含填充）</li>
 *   <li>并发安全：关键配置与初始化同步控制</li>
 * </ul>
 *
 * <h3>密钥管理</h3>
 * <ul>
 *   <li>默认不包含密钥；使用前需通过 {@code setPublicKey}/{@code setPrivateKey} 或 {@code setKeyPair} 配置</li>
 * </ul>
 *
 * <h3>初始化行为</h3>
 * <ul>
 *   <li>惰性初始化且幂等；按需触发</li>
 * </ul>
 *
 * <h3>典型使用场景</h3>
 * <ul>
 *   <li>API 请求参数签名验证</li>
 *   <li>配置文件完整性校验</li>
 *   <li>消息防篡改保护</li>
 * </ul>
 *
 * @author pangju666
 * @see StringDigester
 * @see RSAByteDigester
 * @see RSAKeyPair
 * @since 1.0.0
 */
public final class RSAStringDigester implements StringDigester {
	/**
	 * 底层字节签名处理器
	 * <p>负责执行实际的签名/验签逻辑；引用为 final，惰性初始化。</p>
	 *
	 * @see RSAByteDigester
	 * @since 1.0.0
	 */
	private final RSAByteDigester byteDigester;

	/**
	 * 使用默认签名算法构建实例
	 * <p>默认算法：SHA256withRSA。</p>
	 *
	 * @since 1.0.0
	 */
	public RSAStringDigester() {
		this.byteDigester = new RSAByteDigester();
	}

	/**
	 * 使用指定签名算法构建实例
	 *
	 * @param algorithm 签名算法，不能为 null
	 * @throws NullPointerException 当 {@code algorithm} 为 null
	 * @since 1.0.0
	 */
	public RSAStringDigester(final RSASignatureAlgorithm algorithm) {
		this.byteDigester = new RSAByteDigester(algorithm);
	}

	/**
	 * 使用预配置的字节签名处理器构建实例
	 * <p>适用于复用已有密钥与算法配置的场景。</p>
	 *
	 * @param byteDigester 预配置的字节签名处理器，不能为 null
	 * @throws NullPointerException 当 {@code byteDigester} 为 null
	 * @since 1.0.0
	 */
	public RSAStringDigester(final RSAByteDigester byteDigester) {
		Validate.notNull(byteDigester, "byteDigester 不能为 null");
		this.byteDigester = byteDigester;
	}

	/**
	 * 设置 RSA 密钥容器（初始化前有效）
	 * <p>已初始化后调用将抛出 {@link AlreadyInitializedException}。</p>
	 * <p>允许传入 null；为 null 时将清除当前公钥与私钥。</p>
	 *
	 * @param keyPair 新的密钥容器，允许为 null（null 将清除当前密钥）
	 * @throws AlreadyInitializedException 当已初始化后调用时抛出
	 * @since 1.0.0
	 */
	public void setKeyPair(final RSAKeyPair keyPair) {
		this.byteDigester.setKeyPair(keyPair);
	}

	/**
	 * 设置 RSA 私钥（初始化前有效）
	 * <p>已初始化后调用将抛出 {@link AlreadyInitializedException}。</p>
	 * <p>允许传入 null；为 null 时将清除当前私钥。</p>
	 *
	 * @param privateKey 私钥，允许为 null（null 将清除当前私钥）
	 * @throws AlreadyInitializedException 当已初始化后调用时抛出
	 * @since 1.0.0
	 */
	public void setPrivateKey(final RSAPrivateKey privateKey) {
		this.byteDigester.setPrivateKey(privateKey);
	}

	/**
	 * 设置 RSA 公钥（初始化前有效）
	 * <p>已初始化后调用将抛出 {@link AlreadyInitializedException}。</p>
	 * <p>允许传入 null；为 null 时将清除当前公钥。</p>
	 *
	 * @param publicKey 公钥，允许为 null（null 将清除当前公钥）
	 * @throws AlreadyInitializedException 当已初始化后调用时抛出
	 * @since 1.0.0
	 */
	public void setPublicKey(final RSAPublicKey publicKey) {
		this.byteDigester.setPublicKey(publicKey);
	}

	/**
	 * 初始化摘要组件
	 * <p><strong>说明：</strong>该方法为惰性执行且幂等；当未设置任何密钥时不会抛出异常。</p>
	 * <p>该方法会在 {@link #digest(String)} 与 {@link #matches(String, String)} 调用时自动触发。</p>
	 *
	 * @since 1.0.0
	 */
	public void initialize() {
		byteDigester.initialize();
	}

	/**
	 * 生成 Base64 编码签名
	 * <p>使用私钥与当前算法对字符串进行签名，并以标准 Base64 编码输出。</p>
	 *
	 * <h3>输出规范</h3>
	 * <ul>
	 *   <li>RFC 4648 标准 Base64（包含填充）</li>
	 *   <li>空输入返回空字符串</li>
	 * </ul>
	 *
	 * @param message 原始消息，null 或空返回空字符串
	 * @return Base64 编码的签名
	 * @throws EncryptionInitializationException 未设置私钥
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
	 * 验证 Base64 编码签名
	 * <p>使用公钥与当前算法验证字符串与签名是否匹配。</p>
	 *
	 * <h3>输入要求</h3>
	 * <ul>
	 *   <li>消息为空：当且仅当签名也为空时返回 true</li>
	 *   <li>签名为空：返回 false</li>
	 * </ul>
	 *
	 * @param message 原始消息
	 * @param digest Base64 编码的签名
	 * @return 是否匹配
	 * @throws EncryptionInitializationException 未设置公钥
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
}
