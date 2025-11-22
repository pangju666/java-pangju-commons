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

import io.github.pangju666.commons.crypto.key.RSAKeyPair;
import io.github.pangju666.commons.crypto.lang.CryptoConstants;
import io.github.pangju666.commons.crypto.transformation.RSATransformation;
import io.github.pangju666.commons.crypto.transformation.impl.RSAOEAPWithSHA256Transformation;
import io.github.pangju666.commons.crypto.transformation.impl.RSAPKCS1PaddingTransformation;
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
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Objects;

/**
 * RSA 二进制数据加密解密器
 * <p>基于 RSA 的二进制数据加/解密，支持分段处理与可插拔填充方案。</p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li>分段处理：自动对超出单块大小的数据进行分块加/解密</li>
 *   <li>非对称密钥：公钥加密、私钥解密</li>
 *   <li>方案可插拔：默认 OAEPWithSHA-256，支持自定义（如 PKCS#1 v1.5）</li>
 *   <li>并发安全：关键初始化与配置变更均做同步控制</li>
 * </ul>
 *
 * <h3>密钥管理</h3>
 * <ul>
 *   <li>默认不包含密钥；使用前需通过 {@code setPublicKey}/{@code setPrivateKey} 或 {@code setKeyPair} 配置</li>
 *   <li>初始化后不可修改密钥与加密方案</li>
 * </ul>
 *
 * <h3>安全与限制</h3>
 * <ul>
 *   <li>优先使用 OAEP；不建议在新场景使用过时的 PKCS#1 v1.5</li>
 *   <li>大数据建议采用“RSA + 对称加密”的混合方案</li>
 *   <li>分块逻辑为工程兼容处理，不改变 RSA 单块大小上限</li>
 * </ul>
 *
 * <h3>初始化行为</h3>
 * <ul>
 *   <li>惰性初始化且幂等；首次加/解密时计算并缓存分块大小</li>
 * </ul>
 *
 * @see RSATransformation
 * @see RSAKeyPair
 * @see BinaryEncryptor
 * @author pangju666
 * @since 1.0.0
 */
public final class RSABinaryEncryptor implements BinaryEncryptor {
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
	 * RSA 加密方案
	 * <p>定义算法与填充模式，影响 Cipher 名称与分块大小计算：</p>
	 * <ul>
	 *   <li>默认 OAEPWithSHA-256</li>
	 *   <li>初始化后不可更改</li>
	 *   <li>决定加/解密分块大小</li>
	 * </ul>
	 *
	 * @see RSATransformation
	 * @since 1.0.0
	 */
	private final RSATransformation transformation;
	/**
	 * 当前 RSA 公钥
	 * <p>用于加密与计算加密分块大小；可能为 null。</p
	 *
	 * @since 1.0.0
	 */
	private RSAPublicKey publicKey;

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
	 * 当前 RSA 私钥
	 * <p>用于解密与计算解密分块大小；可能为 null。</p>
	 *
	 * @since 1.0.0
	 */
	private RSAPrivateKey privateKey;

	/**
	 * 构建使用默认配置的加密器实例
	 * <p>
	 * 默认仅配置加密方案为 {@link RSAOEAPWithSHA256Transformation}，不设置密钥。
	 * 使用前需通过 {@link #setPublicKey(RSAPublicKey)} / {@link #setPrivateKey(RSAPrivateKey)} 或 {@link #setKeyPair(RSAKeyPair)} 配置密钥。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	public RSABinaryEncryptor() {
		this.transformation = new RSAOEAPWithSHA256Transformation();
	}

	/**
	 * 使用指定加密方案构建加密器实例（不包含默认密钥）
	 * <p>
	 * 该构造方法仅设置加密方案（算法/填充），不生成或设置任何密钥；
	 * 使用前需通过 {@link #setPublicKey(RSAPublicKey)} / {@link #setPrivateKey(RSAPrivateKey)} 或 {@link #setKeyPair(RSAKeyPair)} 配置密钥。
	 * </p>
	 *
	 * <h3>方案选择建议</h3>
	 * <ul>
	 *   <li>高安全性：{@link RSAOEAPWithSHA256Transformation}</li>
	 *   <li>兼容性：{@link RSAPKCS1PaddingTransformation}</li>
	 * </ul>
	 *
	 * @param transformation 加密方案，不可为 null
	 * @throws NullPointerException 当传入 null 参数时抛出
	 * @see RSATransformation
	 * @since 1.0.0
	 */
	public RSABinaryEncryptor(final RSATransformation transformation) {
		Validate.notNull(transformation, "transformation 不可为 null");
		this.transformation = transformation;
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
	 * 初始化加密组件
	 * <p>根据当前配置计算并缓存分块大小（加密/解密），自动检测可用密钥：</p>
	 * <ul>
	 *   <li>存在公钥：初始化加密分块大小</li>
	 *   <li>存在私钥：初始化解密分块大小</li>
	 * </ul>
	 * <p><strong>说明：</strong>该方法为惰性执行且幂等；当未设置任何密钥时不会抛出异常，分块大小保持未初始化状态。</p>
	 * <p>该方法会在 {@link #encrypt(byte[])} 与 {@link #decrypt(byte[])} 调用时自动触发。</p>
	 *
	 * @throws EncryptionInitializationException 当算法不支持或密钥规格解析失败时抛出
	 * @since 1.0.0
	 */
	public synchronized void initialize() {
		if (!initialized) {
			try {
				if (Objects.nonNull(publicKey)) {
					RSAPublicKeySpec publicKeySpec = KeyPairUtils.getKeyFactory(CryptoConstants.RSA_ALGORITHM)
						.getKeySpec(publicKey, RSAPublicKeySpec.class);
					this.encryptBlockSize = this.transformation.getEncryptBlockSize(publicKeySpec);
				}

				if (Objects.nonNull(privateKey)) {
					RSAPrivateKeySpec privateKeySpec = KeyPairUtils.getKeyFactory(CryptoConstants.RSA_ALGORITHM)
						.getKeySpec(privateKey, RSAPrivateKeySpec.class);
					this.decryptBlockSize = this.transformation.getDecryptBlockSize(privateKeySpec);
				}
			} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
				throw new EncryptionInitializationException(e);
			}
			initialized = true;
		}
	}

	/**
	 * 加密二进制数据
	 * <p>使用公钥对原始数据进行加密，自动处理大数据分块：</p>
	 * <ol>
	 *   <li>检查输入数据（支持 null/空数组）</li>
	 *   <li>惰性初始化（如需要）</li>
	 *   <li>按分块大小迭代加密</li>
	 *   <li>合并并返回结果</li>
	 * </ol>
	 *
	 * @param binary 要加密的数据，允许为 null 或空数组（返回空数组）
	 * @return 加密后的数据（非 null；空输入返回空数组）
	 * @throws EncryptionInitializationException 当算法不支持或密钥规格解析失败时抛出
	 * @throws EncryptionOperationNotPossibleException 未设置公钥或加密过程出现异常时抛出
	 * @apiNote 若需提升大数据性能，建议配合对称加密采用混合方案
	 * @since 1.0.0
	 */
	public byte[] encrypt(final byte[] binary) {
		if (ArrayUtils.isEmpty(binary)) {
			return ArrayUtils.EMPTY_BYTE_ARRAY;
		}
		if (Objects.isNull(publicKey)) {
			throw new EncryptionOperationNotPossibleException("未设置公钥");
		}
		if (!initialized) {
			initialize();
		}

		try {
			Cipher cipher = Cipher.getInstance(this.transformation.getName());
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return doFinal(cipher, binary, this.encryptBlockSize);
		} catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchAlgorithmException |
				 NoSuchPaddingException e) {
			throw new EncryptionOperationNotPossibleException(e);
		}
	}

	/**
	 * 解密二进制数据
	 * <p>使用私钥对加密数据进行解密，自动处理大数据分块：</p>
	 * <ol>
	 *   <li>检查输入数据（支持 null/空数组）</li>
	 *   <li>惰性初始化（如需要）</li>
	 *   <li>按分块大小迭代解密</li>
	 *   <li>合并并返回原文</li>
	 * </ol>
	 *
	 * @param encryptedBinary 要解密的数据，允许为 null 或空数组（返回空数组）
	 * @return 解密后的原始数据（非 null；空输入返回空数组）
	 * @throws EncryptionInitializationException 当算法不支持或密钥规格解析失败时抛出
	 * @throws EncryptionOperationNotPossibleException 未设置私钥或解密过程出现异常时抛出
	 * @apiNote 若需提升大数据性能，建议配合对称加密采用混合方案
	 * @since 1.0.0
	 */
	public byte[] decrypt(final byte[] encryptedBinary) {
		if (ArrayUtils.isEmpty(encryptedBinary)) {
			return ArrayUtils.EMPTY_BYTE_ARRAY;
		}
		if (Objects.isNull(privateKey)) {
			throw new EncryptionOperationNotPossibleException("未设置私钥");
		}
		if (!initialized) {
			initialize();
		}

		try {
			Cipher cipher = Cipher.getInstance(this.transformation.getName());
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			return doFinal(cipher, encryptedBinary, this.decryptBlockSize);
		} catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchAlgorithmException |
				 NoSuchPaddingException e) {
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
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		while (inputLength - offsetLength > 0) {
			byte[] bytes;
			if (inputLength - offsetLength > size) {
				bytes = cipher.doFinal(input, offsetLength, size);
			} else {
				bytes = cipher.doFinal(input, offsetLength, inputLength - offsetLength);
			}
			try {
				outputStream.write(bytes);
			} catch (IOException ignored) {
			}
			offsetLength += size;
		}
		return outputStream.toByteArray();
	}
}