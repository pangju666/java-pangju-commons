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

package io.github.pangju666.commons.crypto.key;

import io.github.pangju666.commons.crypto.lang.CryptoConstants;
import io.github.pangju666.commons.crypto.utils.KeyPairUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

/**
 * RSA 密钥对容器类
 * <p>封装 RSA 公钥和私钥的不可变对象，提供多种密钥对生成和解析方式</p>
 *
 * @param publicKey  公钥对象（可为 null）
 * @param privateKey 私钥对象（可为 null）
 * @author pangju666
 * @since 1.0.0
 */
public record RSAKey(PublicKey publicKey, PrivateKey privateKey) {
	/**
	 * 生成默认长度的随机 RSA 密钥对
	 *
	 * @return 包含公钥和私钥的 RSAKey 对象
	 * @since 1.0.0
	 */
	public static RSAKey random() {
		try {
			KeyPair keyPair = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM);
			// RSA算法生成的KeyPair不会为null，因为Java 平台的每个实现都必须支持RSA算法
			return new RSAKey(keyPair.getPublic(), keyPair.getPrivate());
		} catch (NoSuchAlgorithmException e) {
			// 正常不会抛出，因为Java 平台的每个实现都必须支持RSA算法
			throw ExceptionUtils.asRuntimeException(e);
		}
	}

	/**
	 * 生成指定长度的随机 RSA 密钥对
	 *
	 * @param keySize 密钥长度（只能为 1024/2048/4096）
	 * @return 包含公钥和私钥的 RSAKey 对象
	 * @since 1.0.0
	 */
	public static RSAKey random(final int keySize) {
		Validate.isTrue(CryptoConstants.RSA_KEY_SIZE_SET.contains(keySize), "keySize 必须为 1024/2048/4096");
		try {
			KeyPair keyPair = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, keySize);
			// RSA算法生成的KeyPair不会为null，因为Java 平台的每个实现都必须支持RSA算法
			return new RSAKey(keyPair.getPublic(), keyPair.getPrivate());
		} catch (NoSuchAlgorithmException e) {
			// 正常不会抛出，因为Java 平台的每个实现都必须支持RSA算法
			throw ExceptionUtils.asRuntimeException(e);
		}
	}

	/**
	 * 从现有密钥对构建 RSAKey
	 *
	 * @param keyPair 密钥对对象
	 * @return 包含公钥和私钥的 RSAKey 对象
	 * @since 1.0.0
	 */
	public static RSAKey fromKeyPair(final KeyPair keyPair) {
		Validate.notNull(keyPair, "keyPair不可为空");
		return new RSAKey(keyPair.getPublic(), keyPair.getPrivate());
	}

	/**
	 * 从原始字节数组构建 RSAKey
	 *
	 * @param publicKey  公钥原始字节数组（必须符合X.509 标准)
	 * @param privateKey 私钥原始字节数组（必须符合PKCS #8 标准）
	 * @return 解析后的 RSAKey 对象（任一参数为空数组时对应密钥为 null）
	 * @throws InvalidKeySpecException  当密钥规格无效时抛出
	 * @since 1.0.0
	 * @see java.security.spec.X509EncodedKeySpec
	 * @see java.security.spec.PKCS8EncodedKeySpec
	 */
	public static RSAKey fromRawBytes(final byte[] publicKey, final byte[] privateKey) throws InvalidKeySpecException {
		try {
			return new RSAKey(KeyPairUtils.getPublicKeyFromX509RawBytes(CryptoConstants.RSA_ALGORITHM, publicKey),
				KeyPairUtils.getPrivateKeyFromPKCS8RawBytes(CryptoConstants.RSA_ALGORITHM, privateKey));
		} catch (NoSuchAlgorithmException e) {
			// 正常不会抛出，因为Java 平台的每个实现都必须支持RSA算法
			throw ExceptionUtils.asRuntimeException(e);
		}
	}

	/**
	 * 从 Base64 编码字符串构建 RSAKey
	 *
	 * @param publicKey  Base64 编码的公钥字符串（必须符合X.509 标准)
	 * @param privateKey Base64 编码的私钥字符串（必须符合PKCS #8 标准）
	 * @return 解析后的 RSAKey 对象（任一参数为空字符串时对应密钥为 null）
	 * @throws InvalidKeySpecException  当密钥规格无效时抛出
	 * @since 1.0.0
	 * @see java.security.spec.X509EncodedKeySpec
	 * @see java.security.spec.PKCS8EncodedKeySpec
	 */
	public static RSAKey fromBase64String(final String publicKey, final String privateKey) throws InvalidKeySpecException {
		try {
			return new RSAKey(KeyPairUtils.getPublicKeyFromX509Base64String(CryptoConstants.RSA_ALGORITHM, publicKey),
				KeyPairUtils.getPrivateKeyFromPKCS8Base64String(CryptoConstants.RSA_ALGORITHM, privateKey));
		} catch (NoSuchAlgorithmException e) {
			// 正常不会抛出，因为Java 平台的每个实现都必须支持RSA算法
			throw ExceptionUtils.asRuntimeException(e);
		}
	}
}