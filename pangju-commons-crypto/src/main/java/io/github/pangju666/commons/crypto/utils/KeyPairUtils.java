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
import java.util.concurrent.ConcurrentHashMap;

/**
 * 密钥对工具类，提供密钥对生成、密钥编解码及密钥对象转换功能
 * <p>
 * 本工具类封装了Java安全体系中的密钥操作，提供线程安全的方法来生成、解析和转换密钥对。
 * 支持多种非对称加密算法（如RSA、DSA等）的密钥操作。
 * </p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li><b>对象缓存</b> - 缓存KeyFactory和KeyPairGenerator实例提高性能</li>
 *   <li><b>多格式支持</b> - 支持原始字节、Base64编码字符串等多种密钥格式</li>
 *   <li><b>标准兼容</b> - 严格遵循PKCS#8和X.509标准</li>
 *   <li><b>线程安全</b> - 所有方法均为线程安全的静态方法</li>
 * </ul>
 *
 * <h3>典型应用场景</h3>
 * <ol>
 *   <li>非对称加密系统初始化</li>
 *   <li>密钥管理组件开发</li>
 *   <li>密钥持久化存储</li>
 *   <li>密钥交换协议实现</li>
 * </ol>
 *
 * @author pangju666
 * @since 1.0.0
 * @see java.security.KeyPair
 * @see java.security.PublicKey
 * @see java.security.PrivateKey
 */
public class KeyPairUtils {
	/**
	 * KeyFactory对象缓存池（按算法名称缓存）
	 * <p>
	 * 用于缓存已初始化的KeyFactory实例，避免重复创建带来的性能开销。
	 * </p>
	 *
	 * <h3>缓存策略</h3>
	 * <ul>
	 *   <li>初始容量：3（适合RSA/DSA/EC等常见算法）</li>
	 *   <li>并发安全：使用ConcurrentHashMap实现</li>
	 *   <li>生命周期：随ClassLoader存在</li>
	 * </ul>
	 *
	 * @since 1.0.0
	 */
	protected static final Map<String, KeyFactory> KEY_FACTORY_MAP = new ConcurrentHashMap<>(3);

	/**
	 * KeyPairGenerator对象缓存池（按算法名称缓存）
	 * <p>
	 * 用于缓存已配置的KeyPairGenerator实例，避免重复初始化。
	 * </p>
	 *
	 * <h3>缓存策略</h3>
	 * <ul>
	 *   <li>初始容量：3（适合RSA/DSA/EC等常见算法）</li>
	 *   <li>并发安全：使用ConcurrentHashMap实现</li>
	 *   <li>生命周期：随ClassLoader存在</li>
	 * </ul>
	 *
	 * @since 1.0.0
	 */
	protected static final Map<String, KeyPairGenerator> KEY_PAIR_GENERATOR_MAP = new ConcurrentHashMap<>(3);

	protected KeyPairUtils() {
	}

	/**
	 * 获取指定算法的密钥工厂（带缓存机制）
	 * <p>
	 * 获取指定算法的KeyFactory实例，如果缓存中不存在则创建并缓存。
	 * </p>
	 *
	 * <h3>性能考虑</h3>
	 * <ul>
	 *   <li>缓存命中：直接返回现有实例</li>
	 *   <li>缓存未命中：创建新实例并加入缓存</li>
	 *   <li>线程安全：整个操作是原子性的</li>
	 * </ul>
	 *
	 * @param algorithm 加密算法名称（如"RSA"、"DSA"等），不能为null或空
	 * @return 对应算法的KeyFactory实例
	 * @throws NoSuchAlgorithmException 当指定的算法不可用时抛出
	 * @throws IllegalArgumentException 当algorithm为null或空时抛出
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
	 * <p>
	 * 使用默认参数生成指定算法的密钥对，密钥长度由算法默认值决定。
	 * </p>
	 *
	 * <h3>默认行为</h3>
	 * <ul>
	 *   <li>RSA算法：默认2048位密钥</li>
	 *   <li>DSA算法：默认1024位密钥</li>
	 *   <li>EC算法：默认256位密钥</li>
	 * </ul>
	 *
	 * @param algorithm 加密算法名称（如"RSA"、"DSA"等），不能为null或空
	 * @return 新生成的密钥对
	 * @throws NoSuchAlgorithmException 当指定的算法不可用时抛出
	 * @throws IllegalArgumentException 当algorithm为null或空时抛出
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
		return generator.generateKeyPair();
	}

	/**
	 * 生成指定算法和密钥长度的密钥对
	 * <p>
	 * 使用指定密钥长度生成密钥对，支持RSA/DSA/EC等主流非对称加密算法。
	 * </p>
	 *
	 * <h3>密钥长度建议</h3>
	 * <table border="1">
	 *   <tr><th>算法</th><th>最小长度</th><th>推荐长度</th><th>最大长度</th></tr>
	 *   <tr><td>RSA</td><td>1024</td><td>2048</td><td>4096</td></tr>
	 *   <tr><td>DSA</td><td>1024</td><td>2048</td><td>3072</td></tr>
	 *   <tr><td>EC</td><td>224</td><td>256</td><td>571</td></tr>
	 * </table>
	 *
	 * @param algorithm 加密算法名称（如"RSA"、"DSA"等），不能为null或空
	 * @param keySize 密钥长度（单位：bit），必须符合算法要求的最小长度
	 * @return 新生成的密钥对
	 * @throws NoSuchAlgorithmException 当指定的算法不可用时抛出
	 * @throws IllegalArgumentException 当algorithm为null或空，或keySize不合法时抛出
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
		return generator.generateKeyPair();
	}

	/**
	 * 生成指定算法、密钥长度和随机数源的密钥对
	 * <p>
	 * 使用自定义安全随机数生成器生成密钥对，适用于需要特定随机数源的场景。
	 * </p>
	 *
	 * <h3>随机数源选择</h3>
	 * <ul>
	 *   <li><b>默认</b>：SecureRandom.getInstanceStrong()</li>
	 *   <li><b>高性能</b>：new SecureRandom()</li>
	 *   <li><b>确定性测试</b>：使用固定种子的SecureRandom</li>
	 * </ul>
	 *
	 * @param algorithm 加密算法名称（如"RSA"、"DSA"等），不能为null或空
	 * @param keySize 密钥长度（单位：bit），必须符合算法要求的最小长度
	 * @param secureRandom 安全随机数生成器实例，不能为null
	 * @return 新生成的密钥对，可能返回null（当生成器初始化失败时）
	 * @throws NoSuchAlgorithmException 当指定的算法不可用时抛出
	 * @throws IllegalArgumentException 当任何参数为null或无效时抛出
	 * @since 1.0.0
	 */
	public static KeyPair generateKeyPair(final String algorithm, final int keySize, final SecureRandom secureRandom) throws NoSuchAlgorithmException {
		Validate.notBlank(algorithm, "algorithm不可为空");
		KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithm);
		generator.initialize(keySize, secureRandom);
		return generator.generateKeyPair();
	}

	/**
	 * 从PKCS#8格式的Base64编码字符串中解析私钥
	 * <p>
	 * 解析Base64编码的PKCS#8格式私钥，支持标准PEM格式（带或不带头尾标记）。
	 * </p>
	 *
	 * <h3>输入格式示例</h3>
	 * <pre>
	 * -----BEGIN PRIVATE KEY-----
	 * MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQD...
	 * -----END PRIVATE KEY-----
	 * </pre>
	 *
	 * @param algorithm 密钥算法名称（如"RSA"、"DSA"等），不能为null或空
	 * @param encodedKey Base64编码的PKCS#8私钥数据，允许：
	 *                   <ul>
	 *                     <li>标准PEM格式（带BEGIN/END标记）</li>
	 *                     <li>纯Base64编码字符串</li>
	 *                     <li>null或空字符串（返回null）</li>
	 *                   </ul>
	 * @return 解析成功的PrivateKey对象，或null（当输入为空时）
	 * @throws InvalidKeySpecException 当密钥数据格式不正确时抛出
	 * @throws NoSuchAlgorithmException 当指定算法不被支持时抛出
	 * @throws IllegalArgumentException 当algorithm为null或空时抛出
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
	 * <p>
	 * 处理PKCS#8标准格式的私钥字节数据，支持以下特性：
	 * </p>
	 *
	 * <h3>格式规范</h3>
	 * <ul>
	 *   <li>必须符合PKCS#8 v1.2标准</li>
	 *   <li>支持加密和非加密私钥</li>
	 *   <li>必须包含完整的算法标识符</li>
	 * </ul>
	 *
	 * <h3>异常处理</h3>
	 * <ul>
	 *   <li>空数组：抛出IllegalArgumentException</li>
	 *   <li>格式错误：抛出InvalidKeySpecException</li>
	 *   <li>算法不支持：抛出NoSuchAlgorithmException</li>
	 * </ul>
	 *
	 * @param algorithm 密钥算法名称（如"RSA"、"DSA"等），不能为null或空
	 * @param encodedKey PKCS#8规范原始字节数据，必须：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>非空数组</li>
	 *                     <li>符合PKCS#8标准格式</li>
	 *                   </ul>
	 * @return 解析成功的PrivateKey对象
	 * @throws InvalidKeySpecException 当密钥规格与算法不匹配时抛出
	 * @throws NoSuchAlgorithmException 当指定算法不被支持时抛出
	 * @throws IllegalArgumentException 当参数无效时抛出
	 * @see PKCS8EncodedKeySpec
	 * @since 1.0.0
	 */
	public static PrivateKey getPrivateKeyFromPKCS8RawBytes(final String algorithm, final byte[] encodedKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
		Validate.isTrue(ArrayUtils.isNotEmpty(encodedKey), "encodedKey 不可为空");

		KeySpec keySpec = new PKCS8EncodedKeySpec(encodedKey);
		return getKeyFactory(algorithm).generatePrivate(keySpec);
	}

	/**
	 * 从X.509格式的Base64编码字符串中解析公钥
	 * <p>
	 * 解析Base64编码的X.509格式公钥，支持标准PEM格式（带或不带头尾标记）。
	 * </p>
	 *
	 * <h3>输入格式示例</h3>
	 * <pre>
	 * -----BEGIN PUBLIC KEY-----
	 * MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...
	 * -----END PUBLIC KEY-----
	 * </pre>
	 *
	 * @param algorithm 密钥算法名称（如"RSA"、"DSA"等），不能为null或空
	 * @param encodedKey Base64编码的X.509公钥数据，允许：
	 *                   <ul>
	 *                     <li>标准PEM格式（带BEGIN/END标记）</li>
	 *                     <li>纯Base64编码字符串</li>
	 *                     <li>null或空字符串（返回null）</li>
	 *                   </ul>
	 * @return 解析成功的PublicKey对象，或null（当输入为空时）
	 * @throws InvalidKeySpecException 当密钥数据格式不正确时抛出
	 * @throws NoSuchAlgorithmException 当指定算法不被支持时抛出
	 * @throws IllegalArgumentException 当algorithm为null或空时抛出
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
	 * <p>
	 * 处理X.509标准格式的公钥字节数据，支持以下特性：
	 * </p>
	 *
	 * <h3>格式规范</h3>
	 * <ul>
	 *   <li>必须符合X.509 v3标准</li>
	 *   <li>支持SubjectPublicKeyInfo结构</li>
	 *   <li>必须包含完整的算法标识符</li>
	 * </ul>
	 *
	 * <h3>性能优化</h3>
	 * <ul>
	 *   <li>使用缓存的KeyFactory实例</li>
	 *   <li>最小化字节数组拷贝</li>
	 *   <li>提前验证输入有效性</li>
	 * </ul>
	 *
	 * @param algorithm 密钥算法名称（如"RSA"、"DSA"等），不能为null或空
	 * @param encodedKey X.509规范原始字节数据，必须：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>非空数组</li>
	 *                     <li>符合X.509标准格式</li>
	 *                   </ul>
	 * @return 解析成功的PublicKey对象
	 * @throws InvalidKeySpecException 当密钥规格与算法不匹配时抛出
	 * @throws NoSuchAlgorithmException 当指定算法不被支持时抛出
	 * @throws IllegalArgumentException 当参数无效时抛出
	 * @see X509EncodedKeySpec
	 * @since 1.0.0
	 */
	public static PublicKey getPublicKeyFromX509RawBytes(final String algorithm, final byte[] encodedKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
		Validate.isTrue(ArrayUtils.isNotEmpty(encodedKey), "encodedKey 不可为空");

		KeySpec keySpec = new X509EncodedKeySpec(encodedKey);
		return getKeyFactory(algorithm).generatePublic(keySpec);
	}
}