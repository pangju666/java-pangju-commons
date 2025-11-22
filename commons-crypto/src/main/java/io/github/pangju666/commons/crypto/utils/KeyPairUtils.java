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
 * 密钥对工具类，提供密钥对生成、密钥编解码及密钥对象转换功能。
 * <p>
 * 封装 Java 安全体系中的密钥操作，提供线程安全的静态方法来生成、解析和转换密钥对。
 * 支持多种非对称加密算法（如 RSA、DSA、EC 等）的密钥操作。
 * </p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li><b>对象缓存</b>：缓存 KeyFactory 与 KeyPairGenerator 实例以降低创建开销</li>
 *   <li><b>多格式支持</b>：支持原始字节与 Base64 字符串等多种密钥格式</li>
 *   <li><b>标准兼容</b>：遵循 PKCS#8（私钥）与 X.509（公钥）标准</li>
 *   <li><b>并发安全</b>：使用并发映射缓存，方法可在多线程环境下正确工作</li>
 * </ul>
 *
 * <h3>重要说明</h3>
 * <ul>
 *   <li>默认密钥长度与行为由具体 JCA Provider 决定，建议显式指定 keySize</li>
 *   <li>加密的 PKCS#8 私钥需先解密后再解析（本类不负责解密）</li>
 * </ul>
 *
 * @author pangju666
 * @see java.security.KeyPair
 * @see java.security.PublicKey
 * @see java.security.PrivateKey
 * @since 1.0.0
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
	 * 获取指定算法的 KeyFactory（带缓存）
	 * <p>若缓存中不存在则创建并加入缓存。</p>
	 *
	 * <h3>并发行为</h3>
	 * <ul>
	 *   <li>使用 ConcurrentHashMap 保证检索与更新的线程安全</li>
	 *   <li>可能发生并发创建，但最终缓存中的实例可正确工作</li>
	 * </ul>
	 *
	 * @param algorithm 加密算法名称（如 "RSA"、"DSA"、"EC"），不可为空
	 * @return 对应算法的 KeyFactory 实例
	 * @throws NoSuchAlgorithmException 当指定算法不可用时抛出
	 * @throws IllegalArgumentException 当 algorithm 为空时抛出
	 * @since 1.0.0
	 */
	public static KeyFactory getKeyFactory(final String algorithm) throws NoSuchAlgorithmException {
		Validate.notBlank(algorithm, "algorithm不可为空");
		KeyFactory keyFactory = KEY_FACTORY_MAP.get(algorithm);
		if (Objects.nonNull(keyFactory)) {
			return keyFactory;
		}
		keyFactory = KeyFactory.getInstance(algorithm);
		KEY_FACTORY_MAP.putIfAbsent(algorithm, keyFactory);
		return keyFactory;
	}

	/**
	 * 生成指定算法的密钥对（使用 Provider 默认参数）
	 * <p>密钥长度与其他初始化参数由 Provider 决定，可能因实现不同而差异。</p>
	 *
	 * <h3>建议</h3>
	 * <ul>
	 *   <li>为安全性与可预期性，推荐使用显式 keySize 的重载</li>
	 *   <li>RSA 场景建议 ≥ 2048 位；EC 场景建议使用安全曲线与合适参数</li>
	 * </ul>
	 *
	 * @param algorithm 加密算法名称（如 "RSA"、"DSA"、"EC"），不可为空
	 * @return 新生成的密钥对（不返回 null）
	 * @throws NoSuchAlgorithmException 当指定算法不可用时抛出
	 * @throws IllegalArgumentException 当 algorithm 为空时抛出
	 * @since 1.0.0
	 */
	public static KeyPair generateKeyPair(final String algorithm) throws NoSuchAlgorithmException {
		Validate.notBlank(algorithm, "algorithm不可为空");
		KeyPairGenerator generator = KEY_PAIR_GENERATOR_MAP.get(algorithm);
		if (Objects.isNull(generator)) {
			generator = KeyPairGenerator.getInstance(algorithm);
			KEY_PAIR_GENERATOR_MAP.putIfAbsent(algorithm, generator);
		}
		return generator.generateKeyPair();
	}

	/**
	 * 生成指定算法与密钥长度的密钥对
	 * <p>使用显式 keySize 初始化生成器。</p>
	 *
	 * <h3>密钥长度建议</h3>
	 * <table border="1">
	 *   <tr><th>算法</th><th>最小长度</th><th>推荐长度</th><th>最大长度</th></tr>
	 *   <tr><td>RSA</td><td>1024</td><td>2048</td><td>4096</td></tr>
	 *   <tr><td>DSA</td><td>1024</td><td>2048</td><td>3072</td></tr>
	 *   <tr><td>EC</td><td>224</td><td>256</td><td>571</td></tr>
	 * </table>
	 *
	 * @param algorithm 加密算法名称（如 "RSA"、"DSA"、"EC"），不可为空
	 * @param keySize 密钥长度（单位：bit），需符合算法与 Provider 要求
	 * @return 新生成的密钥对
	 * @throws NoSuchAlgorithmException 当指定算法不可用时抛出
	 * @throws IllegalArgumentException 当参数为空或 keySize 非法时抛出
	 * @since 1.0.0
	 */
	public static KeyPair generateKeyPair(final String algorithm, final int keySize) throws NoSuchAlgorithmException {
		Validate.notBlank(algorithm, "algorithm不可为空");
		String mapKey = algorithm + "-" + keySize;
		KeyPairGenerator generator = KEY_PAIR_GENERATOR_MAP.get(mapKey);
		if (Objects.isNull(generator)) {
			generator = KeyPairGenerator.getInstance(algorithm);
			generator.initialize(keySize);
			KEY_PAIR_GENERATOR_MAP.putIfAbsent(mapKey, generator);
		}
		return generator.generateKeyPair();
	}

	/**
	 * 生成指定算法、密钥长度与随机源的密钥对
	 * <p>使用自定义 SecureRandom 初始化生成器。</p>
	 *
	 * <h3>随机源选择</h3>
	 * <ul>
	 *   <li>强随机源：{@code SecureRandom.getInstanceStrong()}</li>
	 *   <li>通用性能：{@code new SecureRandom()}</li>
	 *   <li>测试用途：固定种子 {@code SecureRandom}</li>
	 * </ul>
	 *
	 * @param algorithm 加密算法名称（如 "RSA"、"DSA"、"EC"），不可为空
	 * @param keySize 密钥长度（单位：bit），需符合算法与 Provider 要求
	 * @param secureRandom 安全随机数生成器，不可为 null
	 * @return 新生成的密钥对（不返回 null）
	 * @throws NoSuchAlgorithmException 当指定算法不可用时抛出
	 * @throws IllegalArgumentException 当任何参数为空或无效时抛出
	 * @since 1.0.0
	 */
	public static KeyPair generateKeyPair(final String algorithm, final int keySize, final SecureRandom secureRandom) throws NoSuchAlgorithmException {
		Validate.notBlank(algorithm, "algorithm不可为空");
		KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithm);
		generator.initialize(keySize, secureRandom);
		return generator.generateKeyPair();
	}

	/**
	 * 从 PKCS#8 Base64 字符串解析未加密私钥
	 * <p>支持标准 PEM（可带/不带头尾标记）；若为加密的 PKCS#8（如 {@code -----BEGIN ENCRYPTED PRIVATE KEY-----}），请先解密。</p>
	 *
	 * <h3>输入格式示例</h3>
	 * <pre>
	 * -----BEGIN PRIVATE KEY-----
	 * MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEA...
	 * -----END PRIVATE KEY-----
	 * </pre>
	 *
	 * @param algorithm 密钥算法名称（如 "RSA"、"DSA"、"EC"），不可为空
	 * @param pkcs8Key Base64 编码的未加密 PKCS#8 私钥，允许：
	 *                 <ul>
	 *                   <li>PEM（带 BEGIN/END 标记）</li>
	 *                   <li>纯 Base64 字符串</li>
	 *                   <li>null 或空字符串（返回 null）</li>
	 *                 </ul>
	 * @return 解析得到的 PrivateKey，或 null（当输入为空）
	 * @throws InvalidKeySpecException 当密钥数据不符合未加密 PKCS#8 格式时抛出
	 * @throws NoSuchAlgorithmException 当指定算法不被支持时抛出
	 * @throws IllegalArgumentException 当 algorithm 为空时抛出
	 * @since 1.0.0
	 */
	public static PrivateKey getPrivateKeyFromPKCS8Base64String(final String algorithm, final String pkcs8Key) throws InvalidKeySpecException, NoSuchAlgorithmException {
		if (StringUtils.isBlank(pkcs8Key)) {
			return null;
		}
		String encodedKey = pkcs8Key
			.replace("-----BEGIN PRIVATE KEY-----", StringUtils.EMPTY)
			.replace("-----END PRIVATE KEY-----", StringUtils.EMPTY)
			.replaceAll("\\s", StringUtils.EMPTY);
		return getPrivateKeyFromPKCS8EncodedKey(algorithm, Base64.decodeBase64(encodedKey));
	}

	/**
	 * 从 PKCS#8 原始字节解析未加密私钥
	 * <p>仅支持未加密的 PKCS#8 {@code PrivateKeyInfo}；加密的 PKCS#8（{@code EncryptedPrivateKeyInfo}）需先解密。</p>
	 *
	 * @param algorithm 密钥算法名称（如 "RSA"、"DSA"、"EC"），不可为空
	 * @param encodedKey 未加密 PKCS#8 原始字节，必须非空
	 * @return 解析得到的 PrivateKey
	 * @throws InvalidKeySpecException 当密钥规格与算法不匹配时抛出
	 * @throws NoSuchAlgorithmException 当指定算法不被支持时抛出
	 * @throws IllegalArgumentException 当参数无效时抛出
	 * @see java.security.spec.PKCS8EncodedKeySpec
	 * @since 1.0.0
	 */
	public static PrivateKey getPrivateKeyFromPKCS8EncodedKey(final String algorithm, final byte[] encodedKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
		Validate.isTrue(ArrayUtils.isNotEmpty(encodedKey), "encodedKey 不可为空");

		KeySpec keySpec = new PKCS8EncodedKeySpec(encodedKey);
		return getKeyFactory(algorithm).generatePrivate(keySpec);
	}

	/**
	 * 从 X.509 Base64 字符串解析公钥
	 * <p>支持标准 PEM（可带/不带头尾标记）。</p>
	 *
	 * <h3>输入格式示例</h3>
	 * <pre>
	 * -----BEGIN PUBLIC KEY-----
	 * MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAOsaqNb3Rbl1Kr14Y...
	 * -----END PUBLIC KEY-----
	 * </pre>
	 *
	 * @param algorithm 密钥算法名称（如 "RSA"、"DSA"、"EC"），不可为空
	 * @param x509Key Base64 编码的 X.509 公钥，允许：
	 *                <ul>
	 *                  <li>PEM（带 BEGIN/END 标记）</li>
	 *                  <li>纯 Base64 字符串</li>
	 *                  <li>null 或空字符串（返回 null）</li>
	 *                </ul>
	 * @return 解析得到的 PublicKey，或 null（当输入为空）
	 * @throws InvalidKeySpecException 当密钥数据格式不正确时抛出
	 * @throws NoSuchAlgorithmException 当指定算法不被支持时抛出
	 * @throws IllegalArgumentException 当 algorithm 为空时抛出
	 * @since 1.0.0
	 */
	public static PublicKey getPublicKeyFromX509Base64String(final String algorithm, final String x509Key) throws InvalidKeySpecException, NoSuchAlgorithmException {
		if (StringUtils.isBlank(x509Key)) {
			return null;
		}
		String encodedKey = x509Key
			.replace("-----BEGIN PUBLIC KEY-----", StringUtils.EMPTY)
			.replace("-----END PUBLIC KEY-----", StringUtils.EMPTY)
			.replaceAll("\\s", StringUtils.EMPTY);
		return getPublicKeyFromX509EncodedKey(algorithm, Base64.decodeBase64(encodedKey));
	}

	/**
	 * 从 X.509 原始字节解析公钥
	 * <p>使用 {@code X509EncodedKeySpec} 解析 SubjectPublicKeyInfo 结构。</p>
	 *
	 * @param algorithm 密钥算法名称（如 "RSA"、"DSA"、"EC"），不可为空
	 * @param encodedKey X.509 原始字节，必须非空
	 * @return 解析得到的 PublicKey
	 * @throws InvalidKeySpecException 当密钥规格与算法不匹配时抛出
	 * @throws NoSuchAlgorithmException 当指定算法不被支持时抛出
	 * @throws IllegalArgumentException 当参数无效时抛出
	 * @see java.security.spec.X509EncodedKeySpec
	 * @since 1.0.0
	 */
	public static PublicKey getPublicKeyFromX509EncodedKey(final String algorithm, final byte[] encodedKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
		Validate.isTrue(ArrayUtils.isNotEmpty(encodedKey), "encodedKey 不可为空");

		KeySpec keySpec = new X509EncodedKeySpec(encodedKey);
		return getKeyFactory(algorithm).generatePublic(keySpec);
	}
}