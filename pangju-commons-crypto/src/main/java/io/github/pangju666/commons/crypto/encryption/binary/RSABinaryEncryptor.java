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

package io.github.pangju666.commons.crypto.encryption.binary;

import io.github.pangju666.commons.crypto.key.RSAKey;
import io.github.pangju666.commons.crypto.lang.CryptoConstants;
import io.github.pangju666.commons.crypto.transformation.RSATransformation;
import io.github.pangju666.commons.crypto.transformation.impl.RSAOEAPWithSHA256Transformation;
import io.github.pangju666.commons.crypto.utils.KeyPairUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.jasypt.exceptions.AlreadyInitializedException;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.binary.BinaryEncryptor;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Objects;

/**
 * RSA 二进制数据加密解密器
 * <p>
 * 本类实现了基于RSA算法的二进制数据加密/解密功能，提供以下核心能力：
 * <ul>
 *   <li><strong>分段加密/解密</strong> - 自动处理大数据的分块操作</li>
 *   <li><strong>多密钥支持</strong> - 支持公钥加密/私钥解密</li>
 *   <li><strong>算法扩展</strong> - 支持多种填充模式(如OAEPWithSHA-256)</li>
 *   <li><strong>线程安全</strong> - 所有关键操作都进行了同步控制</li>
 * </ul>
 *
 * <h3>典型使用场景：</h3>
 * <ul>
 *   <li>敏感数据加密存储</li>
 *   <li>安全通信数据传输</li>
 *   <li>数字信封实现</li>
 * </ul>
 *
 * <h3>线程安全说明：</h3>
 * <p>本类所有公共方法均已实现线程安全，但需要注意：</p>
 * <ul>
 *   <li>初始化后不可修改密钥和加密方案</li>
 *   <li>加密/解密操作会自动触发初始化</li>
 * </ul>
 *
 * @see RSATransformation
 * @see RSAKey
 * @see BinaryEncryptor
 * @author pangju666
 * @since 1.0.0
 */
public final class RSABinaryEncryptor implements BinaryEncryptor {
	/**
	 * 解密密码器实例（延迟初始化）
	 * <p>用于解密操作的{@link Cipher}实例，具有以下特性：</p>
	 * <ul>
	 *   <li>使用私钥初始化</li>
	 *   <li>线程安全（通过外部同步控制）</li>
	 *   <li>按需初始化</li>
	 * </ul>
	 *
	 * @see #initialize()
	 * @since 1.0.0
	 */
	private Cipher decryptCipher;

	/**
	 * 加密密码器实例（延迟初始化）
	 * <p>用于加密操作的{@link Cipher}实例，具有以下特性：</p>
	 * <ul>
	 *   <li>使用公钥初始化</li>
	 *   <li>线程安全（通过外部同步控制）</li>
	 *   <li>按需初始化</li>
	 * </ul>
	 *
	 * @see #initialize()
	 * @since 1.0.0
	 */
	private Cipher encryptCipher;

	/**
	 * 加密分块大小（字节）
	 * <p>根据公钥和加密方案计算得出，表示：</p>
	 * <ul>
	 *   <li>单次加密操作的最大数据量</li>
	 *   <li>初始化后不可修改</li>
	 * </ul>
	 *
	 * @see RSATransformation#getEncryptBlockSize(RSAPublicKeySpec)
	 * @since 1.0.0
	 */
	private int encryptBlockSize;

	/**
	 * 解密分块大小（字节）
	 * <p>根据私钥和加密方案计算得出，表示：</p>
	 * <ul>
	 *   <li>单次解密操作的最大数据量</li>
	 *   <li>初始化后不可修改</li>
	 * </ul>
	 *
	 * @see RSATransformation#getDecryptBlockSize(RSAPrivateKeySpec)
	 * @since 1.0.0
	 */
	private int decryptBlockSize;

	/**
	 * RSA密钥对容器
	 * <p>存储用于加密/解密的非对称密钥对，必须满足：</p>
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
	 * <p>表示加密组件是否已完成初始化，具有以下特性：</p>
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
	 * RSA加密方案
	 * <p>定义加密算法和填充模式，具有以下特性：</p>
	 * <ul>
	 *   <li>默认使用OAEPWithSHA-256填充</li>
	 *   <li>初始化后不可更改</li>
	 *   <li>决定分块大小计算方式</li>
	 * </ul>
	 *
	 * @see RSATransformation
	 * @since 1.0.0
	 */
	private RSATransformation transformation = new RSAOEAPWithSHA256Transformation();

	/**
	 * 构造方法（使用默认密钥长度和加密方案）
	 * <p>创建使用默认密钥长度({@value CryptoConstants#RSA_DEFAULT_KEY_SIZE})和默认加密方案的实例</p>
	 *
	 * @see CryptoConstants#RSA_DEFAULT_KEY_SIZE
	 * @since 1.0.0
	 */
	public RSABinaryEncryptor() {
		this.key = RSAKey.random(CryptoConstants.RSA_DEFAULT_KEY_SIZE);
	}

	/**
	 * 构造方法（使用默认密钥长度和指定加密方案）
	 * <p>创建使用默认密钥长度({@value CryptoConstants#RSA_DEFAULT_KEY_SIZE})和自定义加密方案的实例</p>
	 *
	 * @param transformation 加密方案，必须满足：
	 *                       <ul>
	 *                         <li>非null</li>
	 *                         <li>有效的RSA转换方案</li>
	 *                       </ul>
	 * @throws NullPointerException 当transformation为null时抛出
	 * @see RSATransformation
	 * @since 1.0.0
	 */
	public RSABinaryEncryptor(final RSATransformation transformation) {
		this.key = RSAKey.random(CryptoConstants.RSA_DEFAULT_KEY_SIZE);
		this.transformation = transformation;
	}

	/**
	 * 构造方法（使用指定密钥长度和默认加密方案）
	 * <p>创建使用自定义密钥长度和默认加密方案的实例</p>
	 *
	 * @param keySize 密钥长度(bit)，必须满足：
	 *                <ul>
	 *                  <li>≥1024</li>
	 *                  <li≤8192</li>
	 *                </ul>
	 * @throws IllegalArgumentException 当keySize不满足要求时抛出
	 * @since 1.0.0
	 */
	public RSABinaryEncryptor(final int keySize) {
		this.key = RSAKey.random(keySize);
	}

	/**
	 * 构造方法（使用指定密钥长度和加密方案）
	 * <p>创建完全自定义配置的实例</p>
	 *
	 * @param keySize 密钥长度(bit)，必须≥1024
	 * @param transformation 加密方案，非null
	 * @throws NullPointerException 当transformation为null时抛出
	 * @throws IllegalArgumentException 当keySize不满足要求时抛出
	 * @since 1.0.0
	 */
	public RSABinaryEncryptor(final int keySize, final RSATransformation transformation) {
		this.key = RSAKey.random(keySize);
		this.transformation = transformation;
	}

	/**
	 * 构造方法（使用已有密钥和默认加密方案）
	 * <p>使用预生成的RSA密钥对创建实例</p>
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
	public RSABinaryEncryptor(final RSAKey key) {
		this.key = key;
	}

	/**
	 * 构造方法（使用已有密钥和指定加密方案）
	 * <p>使用预生成的RSA密钥对和自定义加密方案创建实例</p>
	 *
	 * @param key RSA密钥对，必须包含至少一个密钥
	 * @param transformation 加密方案，非null
	 * @throws NullPointerException 当key或transformation为null时抛出
	 * @throws IllegalArgumentException 当key不包含任何密钥时抛出
	 * @since 1.0.0
	 */
	public RSABinaryEncryptor(final RSAKey key, final RSATransformation transformation) {
		this.key = key;
		this.transformation = transformation;
	}

	/**
	 * 设置加密方案（初始化前有效）
	 * <p>在实例初始化前设置新的加密方案，用于替换默认方案。</p>
	 *
	 * @param transformation 新的加密方案，必须满足：
	 *                       <ul>
	 *                         <li>非null</li>
	 *                         <li>有效的RSA转换方案</li>
	 *                       </ul>
	 * @throws NullPointerException 当transformation为null时抛出
	 * @throws AlreadyInitializedException 已初始化后调用时抛出
	 * @see RSATransformation
	 * @since 1.0.0
	 */
	public synchronized void setTransformation(final RSATransformation transformation) {
		Validate.notNull(transformation, "transformation 不可为 null");
		if (initialized) {
			throw new AlreadyInitializedException();
		}
		this.transformation = transformation;
	}

	/**
	 * 设置RSA密钥对（初始化前有效）
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
	 * 初始化加密组件
	 * <p>根据当前配置初始化加密/解密处理器，此方法会自动检测可用密钥：</p>
	 * <ul>
	 *   <li>存在公钥：初始化加密功能</li>
	 *   <li>存在私钥：初始化解密功能</li>
	 * </ul>
	 * <p><strong>注意：</strong>此方法会自动被{@link #encrypt(byte[])}和{@link #decrypt(byte[])}调用，通常不需要手动调用。</p>
	 *
	 * @throws EncryptionInitializationException 当以下情况发生时抛出：
	 *         <ul>
	 *             <li>未配置任何密钥</li>
	 *             <li>算法不支持</li>
	 *             <li>密钥无效</li>
	 *         </ul>
	 * @see #encrypt(byte[])
	 * @see #decrypt(byte[])
	 * @since 1.0.0
	 */
	public synchronized void initialize() {
		if (!initialized) {
			try {
				if (Objects.nonNull(key.publicKey())) {
					PublicKey publicKey = key.publicKey();
					RSAPublicKeySpec publicKeySpec = KeyPairUtils.getKeyFactory(CryptoConstants.RSA_ALGORITHM)
						.getKeySpec(publicKey, RSAPublicKeySpec.class);
					this.encryptBlockSize = this.transformation.getEncryptBlockSize(publicKeySpec);
					this.encryptCipher = Cipher.getInstance(this.transformation.getName());
					this.encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
				}

				if (Objects.nonNull(key.privateKey())) {
					PrivateKey privateKey = key.privateKey();
					RSAPrivateKeySpec privateKeySpec = KeyPairUtils.getKeyFactory(CryptoConstants.RSA_ALGORITHM)
						.getKeySpec(privateKey, RSAPrivateKeySpec.class);
					this.decryptBlockSize = this.transformation.getDecryptBlockSize(privateKeySpec);
					this.decryptCipher = Cipher.getInstance(this.transformation.getName());
					this.decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
				}
			} catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
					 InvalidKeySpecException e) {
				throw new EncryptionInitializationException(e);
			}
			initialized = true;
		}
	}

	/**
	 * 加密二进制数据
	 * <p>使用公钥加密原始数据，自动处理大数据分块，流程包括：</p>
	 * <ol>
	 *   <li>检查输入数据</li>
	 *   <li>自动初始化(如需要)</li>
	 *   <li>分块加密数据</li>
	 *   <li>返回加密结果</li>
	 * </ol>
	 *
	 * @param binary 要加密的数据，允许为空数组(返回空数组)
	 * @return 加密后的数据，具有以下特性：
	 *         <ul>
	 *           <li>非null</li>
	 *           <li>空输入返回空数组</li>
	 *         </ul>
	 * @throws EncryptionOperationNotPossibleException 当以下情况发生时抛出：
	 *         <ul>
	 *           <li>未设置公钥</li>
	 *           <li>加密过程出现异常</li>
	 *         </ul>
	 * @since 1.0.0
	 */
	public byte[] encrypt(final byte[] binary) {
		if (ArrayUtils.isEmpty(binary)) {
			return ArrayUtils.EMPTY_BYTE_ARRAY;
		}
		if (Objects.isNull(key.publicKey())) {
			throw new EncryptionOperationNotPossibleException("未设置公钥");
		}
		if (!initialized) {
			initialize();
		}
		try {
			return doFinal(this.encryptCipher, binary, this.encryptBlockSize);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new EncryptionOperationNotPossibleException(e);
		}
	}

	/**
	 * 解密二进制数据
	 * <p>使用私钥解密数据，自动处理大数据分块，流程包括：</p>
	 * <ol>
	 *   <li>检查输入数据</li>
	 *   <li>自动初始化(如需要)</li>
	 *   <li>分块解密数据</li>
	 *   <li>返回解密结果</li>
	 * </ol>
	 *
	 * @param encryptedBinary 要解密的数据，允许为空数组(返回空数组)
	 * @return 解密后的原始数据，具有以下特性：
	 *         <ul>
	 *           <li>非null</li>
	 *           <li>空输入返回空数组</li>
	 *         </ul>
	 * @throws EncryptionOperationNotPossibleException 当以下情况发生时抛出：
	 *         <ul>
	 *           <li>未设置私钥</li>
	 *           <li>解密过程出现异常</li>
	 *         </ul>
	 * @since 1.0.0
	 */
	public byte[] decrypt(final byte[] encryptedBinary) {
		if (ArrayUtils.isEmpty(encryptedBinary)) {
			return ArrayUtils.EMPTY_BYTE_ARRAY;
		}
		if (Objects.isNull(key.privateKey())) {
			throw new EncryptionOperationNotPossibleException("未设置私钥");
		}
		if (!initialized) {
			initialize();
		}
		try {
			return doFinal(this.decryptCipher, encryptedBinary, this.decryptBlockSize);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new EncryptionOperationNotPossibleException(e);
		}
	}

	/**
	 * 执行分段加密/解密操作
	 * <p>内部方法，处理大数据的分块加密/解密，流程包括：</p>
	 * <ol>
	 *   <li>检查输入数据长度</li>
	 *   <li>分块处理数据</li>
	 *   <li>合并处理结果</li>
	 * </ol>
	 *
	 * @param cipher 已初始化的密码器实例
	 * @param input  要处理的数据
	 * @param size   单次处理的最大字节数
	 * @return 处理后的完整数据
	 * @throws IllegalBlockSizeException 当数据块大小不符合要求时抛出
	 * @throws BadPaddingException 当填充错误时抛出
	 * @since 1.0.0
	 */
	private byte[] doFinal(final Cipher cipher, final byte[] input, final int size) throws IllegalBlockSizeException, BadPaddingException {
		if (input.length <= size) {
			return cipher.doFinal(input);
		}
		int inputLength = input.length;
		int offsetLength = 0;
		int i = 0;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		while (inputLength - offsetLength > 0) {
			byte[] bytes;
			if (inputLength - offsetLength > size) {
				bytes = cipher.doFinal(input, offsetLength, size);
			} else {
				bytes = cipher.doFinal(input, offsetLength, inputLength - offsetLength);
			}
			outputStream.writeBytes(bytes);
			++i;
			offsetLength = size * i;
		}
		return outputStream.toByteArray();
	}
}