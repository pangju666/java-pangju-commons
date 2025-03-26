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
import io.github.pangju666.commons.crypto.lang.CryptoConstants;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.jasypt.digest.ByteDigester;
import org.jasypt.exceptions.AlreadyInitializedException;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

import java.security.*;
import java.util.Objects;

/**
 * RSA 数字签名处理器，提供基于非对称密钥的签名生成与验证功能
 * <p>
 * 本类实现了 {@link ByteDigester} 接口，支持以下特性：
 * <ul>
 *   <li>使用私钥生成数字签名</li>
 *   <li>使用公钥验证签名有效性</li>
 *   <li>支持自定义签名算法（比如：SHA256withRSA、SHA384withRSA、SHA512withRSA）</li>
 *   <li>线程安全初始化机制</li>
 * </ul>
 *
 * @author pangju666
 * @see ByteDigester
 * @see RSAKey
 * @since 1.0.0
 */
public final class RSAByteDigester implements ByteDigester {
	/**
	 * 验证处理器（延迟初始化）
	 *
	 * @since 1.0.0
	 */
	private Signature verifySignature;
	/**
	 * 签名处理器（延迟初始化）
	 *
	 * @since 1.0.0
	 */
	private Signature signature;
	/**
	 * RSA 密钥对容器（必须包含至少一个密钥）
	 *
	 * @since 1.0.0
	 */
	private RSAKey key;
	/**
	 * 初始化状态标识（true 表示已完成初始化）
	 *
	 * @since 1.0.0
	 */
	private boolean initialized = false;
	/**
	 * 签名算法名称（遵循 JCA 标准命名规范）
	 * <p>默认值："SHA256withRSA"</p>
	 *
	 * @since 1.0.0
	 */
	private String algorithm = "SHA256withRSA";

	/**
	 * 构造方法（使用默认密钥长度和算法）
	 *
	 * @since 1.0.0
	 */
	public RSAByteDigester() {
		this.key = RSAKey.random(CryptoConstants.RSA_DEFAULT_KEY_SIZE);
	}

	/**
	 * 构造方法（自定义算法，默认密钥长度）
	 *
	 * @param algorithm 签名算法名称（如：SHA256withRSA）
	 * @since 1.0.0
	 */
	public RSAByteDigester(final String algorithm) {
		this.key = RSAKey.random(CryptoConstants.RSA_DEFAULT_KEY_SIZE);
		this.algorithm = algorithm;
	}

	/**
	 * 构造方法（自定义密钥长度，默认算法）
	 *
	 * @param keySize RSA 密钥位长度（推荐值：2048/4096）
	 * @since 1.0.0
	 */
	public RSAByteDigester(final int keySize) {
		this.key = RSAKey.random(keySize);
	}

	/**
	 * 构造方法（完全自定义配置）
	 *
	 * @param keySize   密钥位长度
	 * @param algorithm 签名算法名称
	 * @since 1.0.0
	 */
	public RSAByteDigester(final int keySize, final String algorithm) {
		this.key = RSAKey.random(keySize);
		this.algorithm = algorithm;
	}

	/**
	 * 构造方法（使用现有密钥，默认算法）
	 *
	 * @param key 预生成的 RSA 密钥对
	 * @since 1.0.0
	 */
	public RSAByteDigester(final RSAKey key) {
		this.key = key;
	}

	/**
	 * 构造方法（完全自定义密钥和算法）
	 *
	 * @param key       预生成的 RSA 密钥对
	 * @param algorithm 签名算法名称
	 * @since 1.0.0
	 */
	public RSAByteDigester(final RSAKey key, final String algorithm) {
		this.key = key;
		this.algorithm = algorithm;
	}

	/**
	 * 设置 RSA 密钥对（初始化前有效）
	 *
	 * @param key 新的密钥容器
	 * @throws AlreadyInitializedException 已初始化后调用时抛出
	 * @since 1.0.0
	 */
	public synchronized void setKey(final RSAKey key) {
		Validate.notNull(key, "key 不可为 null");
		if (initialized) {
			throw new AlreadyInitializedException();
		}
		this.key = key;
	}

	/**
	 * 设置签名算法（初始化前有效）
	 *
	 * @param algorithm 新的算法名称
	 * @throws AlreadyInitializedException 已初始化后调用时抛出
	 * @since 1.0.0
	 */
	public synchronized void setAlgorithm(final String algorithm) {
		Validate.notNull(algorithm, "algorithm 不可为 null");
		if (initialized) {
			throw new AlreadyInitializedException();
		}
		this.algorithm = algorithm;
	}

	/**
	 * 初始化签名组件
	 * <p>
	 * 根据当前配置初始化签名处理器，自动检测可用密钥：
	 * <ul>
	 *   <li>存在私钥：初始化签名功能</li>
	 *   <li>存在公钥：初始化验证功能</li>
	 * </ul>
	 *
	 * @throws EncryptionInitializationException 初始化失败时抛出
	 * @since 1.0.0
	 */
	public synchronized void initialize() {
		if (!initialized) {
			try {
				if (Objects.nonNull(key.publicKey())) {
					PublicKey publicKey = key.publicKey();
					this.verifySignature = Signature.getInstance(this.algorithm);
					this.verifySignature.initVerify(publicKey);
				}

				if (Objects.nonNull(key.privateKey())) {
					PrivateKey privateKey = key.privateKey();
					this.signature = Signature.getInstance(this.algorithm);
					this.signature.initSign(privateKey);
				}
			} catch (NoSuchAlgorithmException | InvalidKeyException e) {
				throw new EncryptionInitializationException(e);
			}
			initialized = true;
		}
	}

	/**
	 * 生成数据签名
	 *
	 * @param message 原始数据字节数组
	 * @return 数字签名字节数组
	 * @throws EncryptionOperationNotPossibleException 当：
	 *         1. 未配置私钥
	 *         2. 签名操作失败
	 * @since 1.0.0
	 */
	@Override
	public byte[] digest(byte[] message) {
		if (ArrayUtils.isEmpty(message)) {
			return ArrayUtils.EMPTY_BYTE_ARRAY;
		}
		if (Objects.isNull(key.privateKey())) {
			throw new EncryptionInitializationException("未设置私钥");
		}
		if (!initialized) {
			initialize();
		}
		try {
			signature.update(message);
			return signature.sign();
		} catch (SignatureException e) {
			throw new EncryptionOperationNotPossibleException(e);
		}
	}

	/**
	 * 验证签名有效性
	 *
	 * @param message 原始数据字节数组
	 * @param digest  待验证签名字节数组
	 * @return 验证结果（true=有效）
	 * @throws EncryptionOperationNotPossibleException 当：
	 *         1. 未配置公钥
	 *         2. 验证操作失败
	 * @since 1.0.0
	 */
	@Override
	public boolean matches(byte[] message, byte[] digest) {
		if (ArrayUtils.isEmpty(message)) {
			return ArrayUtils.isEmpty(digest);
		} else if (ArrayUtils.isEmpty(digest)) {
			return false;
		}

		if (Objects.isNull(key.publicKey())) {
			throw new EncryptionInitializationException("未设置公钥");
		}
		if (!initialized) {
			initialize();
		}
		try {
			verifySignature.update(message);
			return verifySignature.verify(digest);
		} catch (SignatureException e) {
			throw new EncryptionOperationNotPossibleException(e);
		}
	}
}
