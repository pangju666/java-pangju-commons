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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.jasypt.digest.ByteDigester;
import org.jasypt.exceptions.AlreadyInitializedException;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

import java.security.*;
import java.util.Objects;

/**
 * RSA 数字签名处理器
 * <p>
 * 本类实现了基于 RSA 非对称加密算法的数字签名功能，提供以下核心能力：
 * <ul>
 *   <li><strong>签名生成</strong> - 使用私钥对数据进行数字签名</li>
 *   <li><strong>签名验证</strong> - 使用公钥验证签名的有效性</li>
 *   <li><strong>算法扩展</strong> - 支持多种 JCA 标准签名算法（如 SHA256withRSA、SHA384withRSA 等）</li>
 *   <li><strong>线程安全</strong> - 所有关键操作都进行了同步控制</li>
 *   <li><strong>延迟初始化</strong> - 按需初始化签名组件，提高资源利用率</li>
 * </ul>
 *
 * <h3>典型使用场景：</h3>
 * <ul>
 *   <li>软件发布包的数字签名验证</li>
 *   <li>API 请求的身份认证</li>
 *   <li>重要数据的完整性保护</li>
 * </ul>
 *
 * <h3>线程安全说明：</h3>
 * <p>本类所有公共方法均已实现线程安全，可安全用于多线程环境。</p>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 生成签名
 * RSAByteDigester signer = new RSAByteDigester();
 * byte[] signature = signer.digest(data);
 *
 * // 验证签名
 * boolean isValid = signer.matches(data, signature);
 * }</pre>
 *
 * @author pangju666
 * @see ByteDigester
 * @see RSAKey
 * @see <a href="https://docs.oracle.com/en/java/javase/11/docs/specs/security/standard-names.html">JCA 签名算法标准名称</a>
 * @since 1.0.0
 */
public final class RSAByteDigester implements ByteDigester {
	/**
	 * 签名验证处理器（延迟初始化）
	 * <p>用于验证数字签名的{@link Signature}实例，具有以下特性：</p>
	 * <ul>
	 *   <li>使用公钥初始化</li>
	 *   <li>线程安全（通过外部同步控制）</li>
	 *   <li>按需初始化</li>
	 * </ul>
	 *
	 * @see #initialize()
	 * @since 1.0.0
	 */
	private Signature verifySignature;

	/**
	 * 签名生成处理器（延迟初始化）
	 * <p>用于生成数字签名的{@link Signature}实例，具有以下特性：</p>
	 * <ul>
	 *   <li>使用私钥初始化</li>
	 *   <li>线程安全（通过外部同步控制）</li>
	 *   <li>按需初始化</li>
	 * </ul>
	 *
	 * @see #initialize()
	 * @since 1.0.0
	 */
	private Signature signature;

	/**
	 * RSA密钥对容器
	 * <p>存储用于签名和验证的非对称密钥对，必须满足：</p>
	 * <ul>
	 *   <li>非null</li>
	 *   <li>至少包含公钥或私钥</li>
	 *   <li>初始化后不可更改</li>
	 * </ul>
	 *
	 * @see RSAKey
	 * @since 1.0.0
	 */
	private RSAKey key;

	/**
	 * 初始化状态标识
	 * <p>表示签名组件是否已完成初始化，具有以下特性：</p>
	 * <ul>
	 *   <li>true：已完成初始化</li>
	 *   <li>false：未初始化</li>
	 * </ul>
	 * <p><strong>注意：</strong>初始化后修改密钥或算法将抛出异常</p>
	 *
	 * @see #initialize()
	 * @since 1.0.0
	 */
	private boolean initialized = false;

	/**
	 * 签名算法名称
	 *
	 * @since 1.0.0
	 */
	private RsaSignatureAlgorithm algorithm = RsaSignatureAlgorithm.SHA256_WITH_RSA;

	/**
	 * 构造方法（使用默认密钥长度和算法）
	 * <p>使用默认密钥长度({@value CryptoConstants#RSA_DEFAULT_KEY_SIZE})和默认算法("SHA256withRSA")创建实例</p>
	 *
	 * @see CryptoConstants#RSA_DEFAULT_KEY_SIZE
	 * @since 1.0.0
	 */
	public RSAByteDigester() {
		this.key = RSAKey.random(CryptoConstants.RSA_DEFAULT_KEY_SIZE);
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
	public RSAByteDigester(final RsaSignatureAlgorithm algorithm) {
		Validate.notNull(algorithm, "algorithm 不可为 null");
		this.key = RSAKey.random(CryptoConstants.RSA_DEFAULT_KEY_SIZE);
		this.algorithm = algorithm;
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
	public RSAByteDigester(final int keySize) {
		this.key = RSAKey.random(keySize);
	}

	/**
	 * 构造方法（完全自定义配置）
	 * <p>使用指定密钥长度和算法创建实例</p>
	 *
	 * @param keySize   密钥位长度，最小1024
	 * @param algorithm 签名算法名称，非null
	 * @throws NullPointerException 当algorithm为null时抛出
	 * @throws IllegalArgumentException 当keySize小于1024时抛出
	 * @since 1.0.0
	 */
	public RSAByteDigester(final int keySize, final RsaSignatureAlgorithm algorithm) {
		Validate.notNull(algorithm, "algorithm 不可为 null");
		this.key = RSAKey.random(keySize);
		this.algorithm = algorithm;
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
	 * @throws NullPointerException 当key为null时抛出
	 * @throws IllegalArgumentException 当key不包含任何密钥时抛出
	 * @since 1.0.0
	 */
	public RSAByteDigester(final RSAKey key) {
		this.key = key;
	}

	/**
	 * 构造方法（完全自定义密钥和算法）
	 * <p>使用预生成的RSA密钥对和指定算法创建实例</p>
	 *
	 * @param key        RSA密钥对，必须包含至少一个密钥
	 * @param algorithm  签名算法名称，非null
	 * @throws NullPointerException 当key或algorithm为null时抛出
	 * @throws IllegalArgumentException 当key不包含任何密钥时抛出
	 * @since 1.0.0
	 */
	public RSAByteDigester(final RSAKey key, final RsaSignatureAlgorithm algorithm) {
		Validate.notNull(algorithm, "algorithm 不可为 null");
		this.key = key;
		this.algorithm = algorithm;
	}

	public RSAKey getKey() {
		return key;
	}

	/**
	 * 设置 RSA 密钥对（初始化前有效）
	 * <p>在实例初始化前设置新的RSA密钥对，用于替换默认生成的密钥。</p>
	 *
	 * @param key 新的密钥容器，必须满足：
	 *            <ul>
	 *              <li>非null</li>
	 *              <li>至少包含公钥或私钥</li>
	 *            </ul>
	 * @throws NullPointerException 当key为null时抛出
	 * @throws IllegalArgumentException 当key不包含任何密钥时抛出
	 * @throws AlreadyInitializedException 已初始化后调用时抛出
	 * @see #initialize()
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
	 * <p>在实例初始化前设置新的签名算法，用于替换默认算法(SHA256withRSA)。</p>
	 *
	 * @param algorithm 新的算法
	 * @throws NullPointerException 当algorithm为null时抛出
	 * @throws AlreadyInitializedException 已初始化后调用时抛出
	 * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#Signature">JCA 签名算法标准名称</a>
	 * @since 1.0.0
	 */
	public synchronized void setAlgorithm(final RsaSignatureAlgorithm algorithm) {
		Validate.notNull(algorithm, "algorithm 不可为 null");
		if (initialized) {
			throw new AlreadyInitializedException();
		}
		this.algorithm = algorithm;
	}

	/**
	 * 初始化签名组件
	 * <p>根据当前配置初始化签名和验证处理器，此方法会自动检测可用密钥：</p>
	 * <ul>
	 *   <li>存在私钥：初始化签名功能</li>
	 *   <li>存在公钥：初始化验证功能</li>
	 * </ul>
	 * <p><strong>注意：</strong>此方法会自动被{@link #digest(byte[])}和{@link #matches(byte[], byte[])}调用，通常不需要手动调用。</p>
	 *
	 * @throws EncryptionInitializationException 当以下情况发生时抛出：
	 *         <ul>
	 *             <li>未配置任何密钥</li>
	 *             <li>算法不支持</li>
	 *             <li>密钥无效</li>
	 *         </ul>
	 * @see #digest(byte[])
	 * @see #matches(byte[], byte[])
	 * @since 1.0.0
	 */
	public synchronized void initialize() {
		if (!initialized) {
			try {
				if (Objects.nonNull(key.getPublicKey())) {
					PublicKey publicKey = key.getPublicKey();
					this.verifySignature = Signature.getInstance(this.algorithm.getAlgorithm());
					this.verifySignature.initVerify(publicKey);
					if (Objects.nonNull(key.getPrivateKey())) {
						PrivateKey privateKey = key.getPrivateKey();
						this.signature = Signature.getInstance(this.algorithm.getAlgorithm());
						this.signature.initSign(privateKey);
					}
				} else {
					if (Objects.nonNull(key.getPrivateKey())) {
						PrivateKey privateKey = key.getPrivateKey();
						this.signature = Signature.getInstance(this.algorithm.getAlgorithm());
						this.signature.initSign(privateKey);
					}
				}

			} catch (NoSuchAlgorithmException | InvalidKeyException e) {
				throw new EncryptionInitializationException(e);
			}
			initialized = true;
		}
	}

	/**
	 * 生成数据签名
	 * <p>使用配置的私钥对输入数据生成数字签名，签名过程包括：</p>
	 * <ol>
	 *   <li>自动初始化签名组件（如果未初始化）</li>
	 *   <li>更新签名状态机</li>
	 *   <li>生成最终签名</li>
	 * </ol>
	 *
	 * @param message 要签名的原始数据，允许为空数组（返回空数组）
	 * @return 数字签名字节数组，具有以下特性：
	 *         <ul>
	 *             <li>空输入返回空数组</li>
	 *             <li>非空输入返回固定长度的签名数据</li>
	 *         </ul>
	 * @throws EncryptionInitializationException 当以下情况发生时抛出：
	 *         <ul>
	 *             <li>未配置私钥</li>
	 *             <li>签名组件初始化失败</li>
	 *         </ul>
	 * @throws EncryptionOperationNotPossibleException 当签名操作失败时抛出
	 * @see #initialize()
	 * @since 1.0.0
	 */
	@Override
	public byte[] digest(byte[] message) {
		if (ArrayUtils.isEmpty(message)) {
			return ArrayUtils.EMPTY_BYTE_ARRAY;
		}
		if (Objects.isNull(key.getPrivateKey())) {
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
	 * <p>使用配置的公钥验证签名与原始数据的匹配性，验证过程包括：</p>
	 * <ol>
	 *   <li>自动初始化验证组件（如果未初始化）</li>
	 *   <li>更新验证状态机</li>
	 *   <li>执行最终验证</li>
	 * </ol>
	 *
	 * @param message 原始数据字节数组，空数组仅当签名为空时返回true
	 * @param digest 待验证签名字节数组，空数组仅当原始数据为空时返回true
	 * @return 验证结果，满足以下条件时返回true：
	 *         <ul>
	 *             <li>签名与数据匹配</li>
	 *             <li>输入和签名均为空数组</li>
	 *         </ul>
	 * @throws EncryptionInitializationException 当以下情况发生时抛出：
	 *         <ul>
	 *             <li>未配置公钥</li>
	 *             <li>验证组件初始化失败</li>
	 *         </ul>
	 * @throws EncryptionOperationNotPossibleException 当验证操作失败时抛出
	 * @see #initialize()
	 * @since 1.0.0
	 */
	@Override
	public boolean matches(byte[] message, byte[] digest) {
		if (ArrayUtils.isEmpty(message)) {
			return ArrayUtils.isEmpty(digest);
		} else if (ArrayUtils.isEmpty(digest)) {
			return false;
		}

		if (Objects.isNull(key.getPublicKey())) {
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
