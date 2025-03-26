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

package io.github.pangju666.commons.crypto.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 密钥对工具类，提供密钥对生成、密钥编解码及密钥对象转换功能
 * <p>支持多种非对称加密算法密钥对的生成和解析操作</p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class KeyPairUtils {
	/**
	 * KeyFactory对象缓存池（按算法名称缓存，初始容量3）
	 * <p>用于提高密钥工厂对象的获取效率</p>
	 *
	 * @since 1.0.0
	 */
	protected static final Map<String, KeyFactory> KEY_FACTORY_MAP = new ConcurrentHashMap<>(3);
	/**
	 * KeyPairGenerator对象缓存池（按算法名称缓存，初始容量3）
	 * <p>用于缓存密钥对生成器配置参数，避免重复初始化</p>
	 *
	 * @since 1.0.0
	 */
	protected static final Map<String, KeyPairGenerator> KEY_PAIR_GENERATOR_MAP = new ConcurrentHashMap<>(3);

	protected KeyPairUtils() {
	}

	/**
	 * 获取指定算法的密钥工厂（带缓存机制）
	 *
	 * @param algorithm 加密算法名称（如RSA/DSA）
	 * @return 对应算法的密钥工厂实例
	 * @throws NoSuchAlgorithmException 当指定的算法不可用时抛出
	 * @see KeyFactory
	 * @since 1.0.0
	 */
	public static KeyFactory getKeyFactory(final String algorithm) throws NoSuchAlgorithmException {
		Validate.notBlank(algorithm, "algorithm不可为空");
		if (KEY_FACTORY_MAP.containsKey(algorithm)) {
			return KEY_FACTORY_MAP.get(algorithm);
		}
		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
		KEY_FACTORY_MAP.put(algorithm, keyFactory);
		return keyFactory;
	}

	/**
	 * 生成指定算法的密钥对（使用默认参数）
	 *
	 * @param algorithm 加密算法名称（如RSA/DSA）
	 * @return 新生成的密钥对，可能返回null（当生成器初始化失败时）
	 * @throws NoSuchAlgorithmException 当指定的算法不可用时抛出
	 * @see KeyPairGenerator
	 * @since 1.0.0
	 */
	public static KeyPair generateKeyPair(final String algorithm) throws NoSuchAlgorithmException {
		Validate.notBlank(algorithm, "algorithm不可为空");
		KeyPairGenerator generator;
		if (KEY_PAIR_GENERATOR_MAP.containsKey(algorithm)) {
			generator = KEY_PAIR_GENERATOR_MAP.get(algorithm);
		} else {
			generator = KeyPairGenerator.getInstance(algorithm);
			KEY_PAIR_GENERATOR_MAP.put(algorithm, generator);
		}
		if (Objects.isNull(generator)) {
			return null;
		}
		return generator.generateKeyPair();
	}

	/**
	 * 生成指定算法和密钥长度的密钥对
	 *
	 * @param algorithm 加密算法名称
	 * @param keySize   密钥长度（单位：bit）
	 * @return 新生成的密钥对，可能返回null（当生成器初始化失败时）
	 * @throws NoSuchAlgorithmException 当指定的算法不可用时抛出
	 * @see KeyPairGenerator
	 * @since 1.0.0
	 */
	public static KeyPair generateKeyPair(final String algorithm, final int keySize) throws NoSuchAlgorithmException {
		Validate.notBlank(algorithm, "algorithm不可为空");
		String mapKey = algorithm + "-" + keySize;
		KeyPairGenerator generator;
		if (KEY_PAIR_GENERATOR_MAP.containsKey(mapKey)) {
			generator = KEY_PAIR_GENERATOR_MAP.get(mapKey);
		} else {
			generator = KeyPairGenerator.getInstance(algorithm);
			generator.initialize(keySize);
			KEY_PAIR_GENERATOR_MAP.put(mapKey, generator);
		}
		if (Objects.isNull(generator)) {
			return null;
		}
		return generator.generateKeyPair();
	}

	/**
	 * 生成指定算法、密钥长度和随机数源的密钥对
	 *
	 * @param algorithm    加密算法名称
	 * @param keySize      密钥长度（单位：bit）
	 * @param secureRandom 安全随机数生成器
	 * @return 新生成的密钥对，可能返回null（当生成器初始化失败时）
	 * @throws NoSuchAlgorithmException 当指定的算法不可用时抛出
	 * @see KeyPairGenerator
	 * @since 1.0.0
	 */
	public static KeyPair generateKeyPair(final String algorithm, final int keySize, final SecureRandom secureRandom) throws NoSuchAlgorithmException {
		Validate.notBlank(algorithm, "algorithm不可为空");
		KeyPairGenerator generator;
		if (KEY_PAIR_GENERATOR_MAP.containsKey(algorithm)) {
			generator = KEY_PAIR_GENERATOR_MAP.get(algorithm);
		} else {
			generator = KeyPairGenerator.getInstance(algorithm);
			generator.initialize(keySize, secureRandom);
			KEY_PAIR_GENERATOR_MAP.put(algorithm, generator);
		}
		if (Objects.isNull(generator)) {
			return null;
		}
		return generator.generateKeyPair();
	}

	/**
	 * 从PKCS#8格式的Base64编码字符串中解析私钥
	 *
	 * @param algorithm  密钥算法名称（如RSA），用于确定密钥工厂
	 * @param encodedKey Base64编码的PKCS#8规范私钥数据，允许为null或空字符串
	 * @return 解析成功的PrivateKey对象，当输入为空时返回null
	 * @throws InvalidKeySpecException  当密钥规格与算法不匹配时抛出
	 * @throws NoSuchAlgorithmException 当指定算法不被支持时抛出
	 * @see PKCS8EncodedKeySpec
	 * @since 1.0.0
	 */
	public static PrivateKey getPrivateKeyFromPKCS8Base64String(final String algorithm, final String encodedKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
		if (StringUtils.isBlank(encodedKey)) {
			return null;
		}
		return getPrivateKeyFromPKCS8RawBytes(algorithm, Base64.decodeBase64(encodedKey));
	}

	/**
	 * 从PKCS#8格式的原始字节数组中解析私钥
	 *
	 * @param algorithm  密钥算法名称（如RSA），用于确定密钥工厂
	 * @param encodedKey PKCS#8规范原始字节数据，允许为null或空数组
	 * @return 解析成功的PrivateKey对象，当输入为空时返回null
	 * @throws InvalidKeySpecException  当密钥规格与算法不匹配时抛出
	 * @throws NoSuchAlgorithmException 当指定算法不被支持时抛出
	 * @see PKCS8EncodedKeySpec
	 * @since 1.0.0
	 */
	public static PrivateKey getPrivateKeyFromPKCS8RawBytes(final String algorithm, final byte[] encodedKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
		if (ArrayUtils.isEmpty(encodedKey)) {
			return null;
		}
		KeySpec keySpec = new PKCS8EncodedKeySpec(encodedKey);
		return getKeyFactory(algorithm).generatePrivate(keySpec);
	}

	/**
	 * 从X.509格式的Base64编码字符串中解析公钥
	 *
	 * @param algorithm  密钥算法名称（如RSA），用于确定密钥工厂
	 * @param encodedKey Base64编码的X.509规范公钥数据，允许为null或空字符串
	 * @return 解析成功的PublicKey对象，当输入为空时返回null
	 * @throws InvalidKeySpecException  当密钥规格与算法不匹配时抛出
	 * @throws NoSuchAlgorithmException 当指定算法不被支持时抛出
	 * @see X509EncodedKeySpec
	 * @since 1.0.0
	 */
	public static PublicKey getPublicKeyFromX509Base64String(final String algorithm, final String encodedKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
		if (StringUtils.isBlank(encodedKey)) {
			return null;
		}
		return getPublicKeyFromX509RawBytes(algorithm, Base64.decodeBase64(encodedKey));
	}

	/**
	 * 从X.509格式的原始字节数组中解析公钥
	 *
	 * @param algorithm  密钥算法名称（如RSA），用于确定密钥工厂
	 * @param encodedKey X.509规范原始字节数据，允许为null或空数组
	 * @return 解析成功的PublicKey对象，当输入为空时返回null
	 * @throws InvalidKeySpecException  当密钥规格与算法不匹配时抛出
	 * @throws NoSuchAlgorithmException 当指定算法不被支持时抛出
	 * @see X509EncodedKeySpec
	 * @since 1.0.0
	 */
	public static PublicKey getPublicKeyFromX509RawBytes(final String algorithm, final byte[] encodedKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
		if (ArrayUtils.isEmpty(encodedKey)) {
			return null;
		}
		KeySpec keySpec = new X509EncodedKeySpec(encodedKey);
		return getKeyFactory(algorithm).generatePublic(keySpec);
	}
}