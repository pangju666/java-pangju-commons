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
 * RSA密钥对不可变容器类，提供多种密钥生成和解析方式
 * <p>
 * 本类封装了RSA算法所需的公钥和私钥，支持从多种格式创建密钥对实例，
 * 并确保密钥对象的线程安全性和不可变性。
 * </p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li><b>不可变设计</b> - 所有字段均为final，确保线程安全</li>
 *   <li><b>多格式支持</b> - 支持随机生成、密钥对转换、原始字节和Base64解析</li>
 *   <li><b>空值安全</b> - 允许公钥或私钥单独为null</li>
 *   <li><b>标准兼容</b> - 严格遵循X.509和PKCS#8标准</li>
 * </ul>
 *
 * <h3>典型应用场景</h3>
 * <ol>
 *   <li>非对称加密系统初始化</li>
 *   <li>密钥管理组件</li>
 *   <li>密钥持久化存储</li>
 *   <li>密钥交换协议</li>
 * </ol>
 *
 * @author pangju666
 * @since 1.0.0
 * @see java.security.PublicKey
 * @see java.security.PrivateKey
 * @see java.security.KeyPair
 */
public class RSAKey {
	/**
	 * RSA公钥对象，符合X.509标准，可能为null（仅解密场景）
	 *
	 * @since 1.0.0
	 */
	private final PublicKey publicKey;
	/**
	 * RSA私钥对象，符合PKCS#8标准，可能为null（仅加密场景）
	 *
	 * @since 1.0.0
	 */
	private final PrivateKey privateKey;

	/**
	 * 构造方法
	 *
	 * @since 1.0.0
	 */
	public RSAKey(PublicKey publicKey, PrivateKey privateKey) {
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	/**
	 * 生成默认长度(2048位)的随机RSA密钥对
	 * <p>
	 * 使用安全随机数生成器创建符合标准的密钥对。
	 * </p>
	 *
	 * <h3>实现细节</h3>
	 * <ul>
	 *   <li>默认密钥长度：2048位（平衡安全与性能）</li>
	 *   <li>使用平台默认的安全随机数生成器</li>
	 *   <li>自动处理密钥规格转换</li>
	 * </ul>
	 *
	 * @return 包含完整公钥和私钥的RSAKey实例
	 * @throws RuntimeException 当底层安全提供程序异常时（理论上不会发生）
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
	 * 生成指定长度的随机RSA密钥对
	 * <p>
	 * 支持1024/2048/4096位三种标准密钥长度。
	 * </p>
	 *
	 * <h3>安全建议</h3>
	 * <ul>
	 *   <li><b>生产环境</b>：推荐2048位或更长</li>
	 *   <li><b>测试环境</b>：可使用1024位提高性能</li>
	 *   <li><b>高安全需求</b>：建议4096位</li>
	 * </ul>
	 *
	 * @param keySize 密钥长度（单位：bit），有效值：1024/2048/4096
	 * @return 包含完整公钥和私钥的RSAKey实例
	 * @throws IllegalArgumentException 当密钥长度不符合要求时
	 * @throws RuntimeException 当底层安全提供程序异常时（理论上不会发生）
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
	 * 从现有KeyPair构建RSAKey实例
	 * <p>
	 * 转换标准KeyPair对象为类型安全的RSAKey实例。
	 * </p>
	 *
	 * <h3>转换规则</h3>
	 * <ul>
	 *   <li>KeyPair中的公钥必须为RSAPublicKey实例</li>
	 *   <li>KeyPair中的私钥必须为RSAPrivateKey实例</li>
	 *   <li>允许公钥或私钥为null（但不推荐）</li>
	 * </ul>
	 *
	 * @param keyPair 标准密钥对对象，不能为null
	 * @return 包含对应公钥和私钥的RSAKey实例
	 * @throws NullPointerException 当keyPair为null时
	 * @since 1.0.0
	 */
	public static RSAKey fromKeyPair(final KeyPair keyPair) {
		Validate.notNull(keyPair, "keyPair不可为 null");
		return new RSAKey(keyPair.getPublic(), keyPair.getPrivate());
	}

	/**
	 * 从原始字节数组构建RSAKey
	 * <p>
	 * 支持从X.509格式的公钥和PKCS#8格式的私钥字节数组重建密钥对。
	 * </p>
	 *
	 * <h3>格式要求</h3>
	 * <table border="1">
	 *   <tr><th>密钥类型</th><th>编码格式</th><th>标准</th></tr>
	 *   <tr><td>公钥</td><td>X.509</td><td>RFC 5280</td></tr>
	 *   <tr><td>私钥</td><td>PKCS#8</td><td>RFC 5208</td></tr>
	 * </table>
	 *
	 * @param publicKey X.509格式的公钥字节数组，可为null或空数组
	 * @param privateKey PKCS#8格式的私钥字节数组，可为null或空数组
	 * @return 包含解析后密钥的RSAKey实例
	 * @throws InvalidKeySpecException 当字节数组不符合密钥规范时
	 * @since 1.0.0
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
	 * 从Base64编码字符串构建RSAKey
	 * <p>
	 * 支持从Base64编码的X.509公钥和PKCS#8私钥字符串重建密钥对。
	 * </p>
	 *
	 * <h3>字符串格式</h3>
	 * <ul>
	 *   <li>标准Base64编码（非URL安全的）</li>
	 *   <li>可包含换行符（自动过滤）</li>
	 *   <li>空字符串或null视为对应密钥不存在</li>
	 * </ul>
	 *
	 * @param publicKey Base64编码的X.509公钥字符串，可为null或空
	 * @param privateKey Base64编码的PKCS#8私钥字符串，可为null或空
	 * @return 包含解析后密钥的RSAKey实例
	 * @throws InvalidKeySpecException 当字符串不符合密钥规范时
	 * @since 1.0.0
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