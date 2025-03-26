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
 * RSA 二进制数据加密解密器，支持分段加密和解密操作。
 * <p>
 * 本类实现了 {@link BinaryEncryptor} 接口，使用 RSA 算法对二进制数据进行加密/解密操作，
 * 支持使用不同填充模式和算法配置。默认使用 OAEPWithSHA-256 填充模式。
 * <p>
 * 该类是线程安全的，但需要注意：
 * <ul>
 *   <li>初始化后（调用 {@link #initialize()}）不可再修改密钥和加密方案</li>
 *   <li>加密/解密操作会自动触发初始化（如果尚未初始化）</li>
 * </ul>
 *
 * @see RSATransformation
 * @see RSAKey
 * @author pangju666
 * @since 1.0.0
 */
public final class RSABinaryEncryptor implements BinaryEncryptor {
	/**
	 * 解密密码器实例（延迟初始化）
	 *
	 * @since 1.0.0
	 */
	private Cipher decryptCipher;
	/**
	 * 加密密码器实例（延迟初始化）
	 *
	 * @since 1.0.0
	 */
	private Cipher encryptCipher;
	/**
	 * 加密时每个分块的最大字节数（根据公钥和加密方案计算得出）
	 *
	 * @since 1.0.0
	 */
	private int encryptBlockSize;
	/**
	 * 解密时每个分块的最大字节数（根据私钥和加密方案计算得出）
	 *
	 * @since 1.0.0
	 */
	private int decryptBlockSize;
	/**
	 * RSA 密钥对（可包含公钥/私钥/两者）
	 *
	 * @since 1.0.0
	 */
	private RSAKey key;
	/**
	 * 是否已初始化的标志位
	 *
	 * @since 1.0.0
	 */
	private boolean initialized = false;
	/**
	 * RSA 加密方案（算法/填充模式配置），默认使用 RSA/ECB/OAEPWithSHA-256AndMGF1Padding
	 *
	 * @since 1.0.0
	 */
	private RSATransformation transformation = new RSAOEAPWithSHA256Transformation();

	/**
	 * 构造方法（使用默认密钥长度和加密方案）
	 *
	 * @since 1.0.0
	 */
	public RSABinaryEncryptor() {
		this.key = RSAKey.random(CryptoConstants.RSA_DEFAULT_KEY_SIZE);
	}

	/**
	 * 构造方法（使用默认密钥长度和指定加密方案）
	 *
	 * @param transformation 加密方案
	 * @since 1.0.0
	 */
	public RSABinaryEncryptor(RSATransformation transformation) {
		this.key = RSAKey.random(CryptoConstants.RSA_DEFAULT_KEY_SIZE);
		this.transformation = transformation;
	}

	/**
	 * 构造方法（使用指定密钥长度和默认加密方案）
	 *
	 * @param keySize RSA 密钥长度（单位：bit）
	 * @since 1.0.0
	 */
	public RSABinaryEncryptor(int keySize) {
		this.key = RSAKey.random(keySize);
	}

	/**
	 * 构造方法（使用指定密钥长度和加密方案）
	 *
	 * @param keySize        密钥长度（单位：bit）
	 * @param transformation 加密方案
	 * @since 1.0.0
	 */
	public RSABinaryEncryptor(int keySize, RSATransformation transformation) {
		this.key = RSAKey.random(keySize);
		this.transformation = transformation;
	}

	/**
	 * 构造方法（使用已有密钥和默认加密方案）
	 *
	 * @param key 预生成的 RSA 密钥对
	 *
	 * @since 1.0.0
	 */
	public RSABinaryEncryptor(RSAKey key) {
		this.key = key;
	}

	/**
	 * 构造方法（使用已有密钥和指定加密方案）
	 *
	 * @param key            预生成的 RSA 密钥对
	 * @param transformation 加密方案
	 * @since 1.0.0
	 */
	public RSABinaryEncryptor(RSAKey key, RSATransformation transformation) {
		this.key = key;
		this.transformation = transformation;
	}

	/**
	 * 设置加密方案（必须在初始化前调用）
	 *
	 * @param transformation 新的加密方案
	 * @throws AlreadyInitializedException 如果已经初始化后调用
	 * @since 1.0.0
	 */
	public synchronized void setTransformation(RSATransformation transformation) {
		Validate.notNull(transformation, "transformation 不可为 null");
		if (initialized) {
			throw new AlreadyInitializedException();
		}
		this.transformation = transformation;
	}

	/**
	 * 设置 RSA 密钥（必须在初始化前调用）
	 *
	 * @param key 新的密钥对
	 * @throws AlreadyInitializedException 如果已经初始化后调用
	 *
	 * @since 1.0.0
	 */
	public synchronized void setKey(RSAKey key) {
		Validate.notNull(key, "key 不可为 null");
		if (initialized) {
			throw new AlreadyInitializedException();
		}
		this.key = key;
	}

	/**
	 * 初始化加密/解密组件
	 * <p>
	 * 根据当前配置的密钥和加密方案初始化 Cipher 实例，
	 * 自动计算加密/解密分块大小
	 *
	 * @throws EncryptionInitializationException 如果初始化过程中出现密钥相关异常
	 *
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
	 *
	 * @param binary 要加密的原始二进制数据
	 * @return 加密后的字节数组
	 * @throws EncryptionOperationNotPossibleException 如果：<br>
	 *         1. 未设置公钥<br>
	 *         2. 加密过程中出现密码学异常
	 *
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
	 *
	 * @param encryptedBinary 要解密的二进制数据
	 * @return 解密后的原始字节数组
	 * @throws EncryptionOperationNotPossibleException 如果：<br>
	 *         1. 未设置私钥<br>
	 *         2. 解密过程中出现密码学异常
	 *
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
	 *
	 * @param cipher 密码器实例（加密/解密模式已初始化）
	 * @param input  输入数据
	 * @param size   每个分块的最大字节数
	 * @return 处理后的完整字节数组
	 * @throws IllegalBlockSizeException 数据块大小不符合要求
	 * @throws BadPaddingException       填充错误
	 *
	 * @since 1.0.0
	 */
	private byte[] doFinal(final Cipher cipher, final byte[] input, final int size)
		throws IllegalBlockSizeException, BadPaddingException {
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