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

package io.github.pangju666.commons.crypto.encryption.text;

import io.github.pangju666.commons.crypto.encryption.binary.RSABinaryEncryptor;
import io.github.pangju666.commons.crypto.key.RSAKey;
import io.github.pangju666.commons.crypto.transformation.RSATransformation;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.exceptions.AlreadyInitializedException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.TextEncryptor;

import java.nio.charset.StandardCharsets;

/**
 * 基于RSA算法的文本加密解密器，支持Base64和十六进制编码格式
 * <p>
 * 该类是线程安全的
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 * @see TextEncryptor
 * @see RSABinaryEncryptor
 */
public final class RSATextEncryptor implements TextEncryptor {
	/**
	 * 底层二进制加密器实例
	 *
	 * @since 1.0.0
	 */
	private final RSABinaryEncryptor binaryEncryptor;

	/**
	 * 创建使用默认配置的加密器
	 *
	 * @since 1.0.0
	 */
	public RSATextEncryptor() {
		this.binaryEncryptor = new RSABinaryEncryptor();
	}

	/**
	 * 使用指定的二进制加密器创建实例
	 *
	 * @param binaryEncryptor 底层二进制加密器（非空）
	 * @since 1.0.0
	 */
	public RSATextEncryptor(RSABinaryEncryptor binaryEncryptor) {
		this.binaryEncryptor = binaryEncryptor;
	}

	/**
	 * 设置加密方案（如RSA/ECB/PKCS1Padding）
	 *
	 * @param transformation 加密方案
	 * @throws AlreadyInitializedException 如果加密器已经初始化后调用
	 * @since 1.0.0
	 */
	public synchronized void setTransformation(RSATransformation transformation) {
		this.binaryEncryptor.setTransformation(transformation);
	}

	/**
	 * 设置RSA密钥对
	 *
	 * @param key RSA密钥对（包含公钥和私钥）
	 * @throws AlreadyInitializedException 如果加密器已经初始化后调用
	 * @since 1.0.0
	 */
	public synchronized void setKey(RSAKey key) {
		this.binaryEncryptor.setKey(key);
	}

	/**
	 * 加密文本并返回Base64编码结果
	 *
	 * @param message 要加密的原始文本
	 * @return Base64编码的加密结果，空输入返回空字符串
	 * @since 1.0.0
	 */
	@Override
	public String encrypt(final String message) {
		if (StringUtils.isBlank(message)) {
			return StringUtils.EMPTY;
		}
		return Base64.encodeBase64String(
			binaryEncryptor.encrypt(message.getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * 解密Base64编码的加密文本
	 *
	 * @param encryptedMessage Base64编码的加密文本
	 * @return 解密后的原始文本，空输入返回空字符串
	 * @since 1.0.0
	 */
	@Override
	public String decrypt(final String encryptedMessage) {
		if (StringUtils.isBlank(encryptedMessage)) {
			return StringUtils.EMPTY;
		}
		return new String(binaryEncryptor.decrypt(
			Base64.decodeBase64(encryptedMessage)), StandardCharsets.UTF_8);
	}

	/**
	 * 加密文本并返回十六进制字符串形式的加密结果
	 *
	 * @param message 要加密的原始文本
	 * @return 十六进制编码的加密结果，空输入返回空字符串
	 * @since 1.0.0
	 */
	public String encryptToHexString(final String message) {
		if (StringUtils.isBlank(message)) {
			return StringUtils.EMPTY;
		}
		return Hex.encodeHexString(binaryEncryptor.encrypt(message.getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * 解密十六进制编码的加密文本
	 *
	 * @param encryptedMessage 十六进制编码的加密文本
	 * @return 解密后的原始文本，空输入返回空字符串
	 * @throws EncryptionOperationNotPossibleException 当十六进制解码失败或解密操作失败时抛出
	 * @since 1.0.0
	 */
	public String decryptFromHexString(final String encryptedMessage) {
		if (StringUtils.isBlank(encryptedMessage)) {
			return StringUtils.EMPTY;
		}
		try {
			return new String(binaryEncryptor.decrypt(Hex.decodeHex(encryptedMessage)),
				StandardCharsets.UTF_8);
		} catch (DecoderException e) {
			throw new EncryptionOperationNotPossibleException(e);
		}
	}
}