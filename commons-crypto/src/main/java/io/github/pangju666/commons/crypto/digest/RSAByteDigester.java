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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.jasypt.digest.ByteDigester;
import org.jasypt.exceptions.AlreadyInitializedException;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Objects;

/**
 * RSA 数字签名处理器
 * <p>基于 RSA 的签名生成与验证，支持算法选择与惰性初始化。</p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li>签名生成：私钥签名</li>
 *   <li>签名验证：公钥验签</li>
 *   <li>算法可选：支持 JCA 标准算法，默认 {@code SHA256withRSA}</li>
 *   <li>并发安全：关键配置与初始化同步控制</li>
 * </ul>
 *
 * <h3>密钥管理</h3>
 * <ul>
 *   <li>默认不包含密钥；使用前需通过 {@code setPublicKey}/{@code setPrivateKey} 或 {@code setKeyPair} 配置</li>
 *   <li>初始化后不可修改密钥</li>
 * </ul>
 *
 * <h3>初始化行为</h3>
 * <ul>
 *   <li>惰性初始化且幂等；按需设置初始化标识</li>
 * </ul>
 *
 * <h3>典型使用场景</h3>
 * <ul>
 *   <li>软件发布包签名验证</li>
 *   <li>API 请求身份认证</li>
 *   <li>重要数据完整性保护</li>
 * </ul>
 *
 * @author pangju666
 * @see ByteDigester
 * @see RSAKeyPair
 * @see RSASignatureAlgorithm
 * @see <a href="https://docs.oracle.com/en/java/javase/11/docs/specs/security/standard-names.html">JCA 签名算法标准名称</a>
 * @since 1.0.0
 */
public final class RSAByteDigester implements ByteDigester {
	/**
	 * 当前 RSA 签名算法
	 * <p>决定底层 {@code Signature} 的算法名称，用于签名与验签。</p>
	 *
	 * @since 1.0.0
	 */
	private final RSASignatureAlgorithm algorithm;
	/**
	 * 当前 RSA 公钥
	 * <p>用于验证签名；可能为 null。</p>
	 *
	 * @since 1.0.0
	 */
	private RSAPublicKey publicKey;
	/**
	 * 当前 RSA 私钥
	 * <p>用于生成签名；可能为 null。</p>
	 *
	 * @since 1.0.0
	 */
	private RSAPrivateKey privateKey;
	/**
	 * 初始化状态标识
	 * <p>表示摘要组件是否已完成初始化，具有以下特性：</p>
	 * <ul>
	 *   <li>true：已完成初始化</li>
	 *   <li>false：未初始化</li>
	 * </ul>
	 *
	 * @see #initialize()
	 * @since 1.0.0
	 */
	private boolean initialized = false;

	/**
	 * 使用默认签名算法构建实例
	 * <p>默认算法：{@link RSASignatureAlgorithm#SHA256_WITH_RSA}。</p>
	 *
	 * @since 1.0.0
	 */
	public RSAByteDigester() {
		this.algorithm = RSASignatureAlgorithm.SHA256_WITH_RSA;
	}

	/**
	 * 使用指定签名算法构建实例
	 *
	 * @param algorithm 签名算法，不能为 null
	 * @throws NullPointerException 当 {@code algorithm} 为 null
	 * @since 1.0.0
	 */
	public RSAByteDigester(final RSASignatureAlgorithm algorithm) {
		Validate.notNull(algorithm, "algorithm 不可为 null");
		this.algorithm = algorithm;
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
	public synchronized void setKeyPair(final RSAKeyPair keyPair) {
		if (initialized) {
			throw new AlreadyInitializedException();
		}
		if (Objects.nonNull(keyPair)) {
			this.privateKey = keyPair.getPrivateKey();
			this.publicKey = keyPair.getPublicKey();
		} else {
			this.privateKey = null;
			this.publicKey = null;
		}
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
	public synchronized void setPrivateKey(final RSAPrivateKey privateKey) {
		if (initialized) {
			throw new AlreadyInitializedException();
		}
		this.privateKey = privateKey;
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
	public synchronized void setPublicKey(final RSAPublicKey publicKey) {
		if (initialized) {
			throw new AlreadyInitializedException();
		}
		this.publicKey = publicKey;
	}

	/**
	 * 初始化摘要组件
	 * <p><strong>说明：</strong>该方法为惰性执行且幂等；当未设置任何密钥时不会抛出异常。</p>
	 * <p>该方法会在 {@link #digest(byte[])} 与 {@link #matches(byte[], byte[])} 调用时自动触发。</p>
	 *
	 * @since 1.0.0
	 */
	public synchronized void initialize() {
		if (!initialized) {
			initialized = true;
		}
	}

	/**
	 * 计算签名摘要
	 * <p>使用私钥与当前算法对输入字节生成签名。</p>
	 *
	 * <h3>处理流程</h3>
	 * <ol>
	 *   <li>空输入返回空数组</li>
	 *   <li>校验私钥</li>
	 *   <li>惰性初始化</li>
	 *   <li>生成签名并返回</li>
	 * </ol>
	 *
	 * @param message 待签名数据；null 或空返回空数组
	 * @return 签名字节；非 null
	 * @throws EncryptionInitializationException 未设置私钥
	 * @throws EncryptionOperationNotPossibleException 签名失败或算法不可用
	 * @since 1.0.0
	 */
	@Override
	public byte[] digest(byte[] message) {
		if (ArrayUtils.isEmpty(message)) {
			return ArrayUtils.EMPTY_BYTE_ARRAY;
		}
		if (Objects.isNull(privateKey)) {
			throw new EncryptionInitializationException("未设置私钥");
		}
		if (!initialized) {
			initialize();
		}

		try {
			Signature signature = Signature.getInstance(this.algorithm.getAlgorithm());
			signature.initSign(privateKey);
			signature.update(message);
			return signature.sign();
		} catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException e) {
			throw new EncryptionOperationNotPossibleException(e);
		}
	}

	/**
	 * 验证签名是否匹配
	 * <p>使用公钥与当前算法验证签名字节。</p>
	 *
	 * <h3>处理规则</h3>
	 * <ul>
	 *   <li>消息为空：当且仅当签名字节也为空时返回 true</li>
	 *   <li>签名字节为空：返回 false</li>
	 * </ul>
	 *
	 * @param message 原始消息字节
	 * @param digest 待验证的签名字节
	 * @return 是否匹配
	 * @throws EncryptionInitializationException 未设置公钥
	 * @throws EncryptionOperationNotPossibleException 验证失败或算法不可用
	 * @since 1.0.0
	 */
	@Override
	public boolean matches(byte[] message, byte[] digest) {
		if (ArrayUtils.isEmpty(message)) {
			return ArrayUtils.isEmpty(digest);
		} else if (ArrayUtils.isEmpty(digest)) {
			return false;
		}
		if (Objects.isNull(publicKey)) {
			throw new EncryptionInitializationException("未设置公钥");
		}
		if (!initialized) {
			initialize();
		}

		try {
			Signature signature = Signature.getInstance(this.algorithm.getAlgorithm());
			signature.initVerify(publicKey);
			signature.update(message);
			return signature.verify(digest);
		} catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException e) {
			throw new EncryptionOperationNotPossibleException(e);
		}
	}
}
