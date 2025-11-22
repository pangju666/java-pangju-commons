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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

/**
 * RSA 密钥对不可变容器
 * <p>
 * 封装 RSA 公钥/私钥的不可变对象，支持随机生成、现有 KeyPair 转换、
 * 原始字节与 Base64 解析等多种来源，确保线程安全与类型一致性。
 * </p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li><b>不可变设计</b>：所有字段均为 final</li>
 *   <li><b>多来源支持</b>：随机生成、KeyPair、X.509/PKCS#8 字节与 Base64</li>
 *   <li><b>类型约束</b>：公钥为 {@link java.security.interfaces.RSAPublicKey}，私钥为 {@link java.security.interfaces.RSAPrivateKey}</li>
 *   <li><b>空值策略</b>：容器可持有任一为空；但构造与工厂方法要求至少提供其一</li>
 *   <li><b>标准兼容</b>：遵循 X.509（公钥 SubjectPublicKeyInfo）与 PKCS#8（未加密私钥）</li>
 * </ul>
 *
 * <h3>使用说明</h3>
 * <ul>
 *   <li>默认密钥长度取决于 Provider；需固定长度请使用显式 {@code keySize}</li>
 *   <li>若私钥为加密的 PKCS#8（EncryptedPrivateKeyInfo），需先解密后再解析</li>
 * </ul>
 *
 * @author pangju666
 * @see java.security.PublicKey
 * @see java.security.PrivateKey
 * @see java.security.KeyPair
 * @since 1.0.0
 */
public class RSAKeyPair {
	/**
	 * RSA公钥对象，符合X.509标准，可能为null（仅解密场景）
	 *
	 * @since 1.0.0
	 */
	private final RSAPublicKey publicKey;
	/**
	 * RSA私钥对象，符合PKCS#8标准，可能为null（仅加密场景）
	 *
	 * @since 1.0.0
	 */
	private final RSAPrivateKey privateKey;

	/**
	 * 构造方法
	 * <p>至少需要提供公钥或私钥中的任意一个，允许另一个为 null。</p>
	 *
	 * @param publicKey  RSA 公钥，可为 null
	 * @param privateKey RSA 私钥，可为 null
	 * @throws IllegalArgumentException 当同时未提供公钥与私钥时抛出
	 * @since 1.0.0
	 */
	public RSAKeyPair(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
		Validate.isTrue(ObjectUtils.anyNotNull(privateKey, publicKey), "至少需要设置公钥或私钥");

		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	/**
	 * 生成随机 RSA 密钥对（使用 Provider 默认参数）
	 * <p>
	 * 使用平台 Provider 的默认参数生成密钥对；实际长度可能因实现不同而差异。
	 * 如需固定长度，请使用 {@link #random(int)}。
	 * </p>
	 *
	 * @return 包含完整公钥与私钥的 RSAKeyPair 实例
	 * @since 1.0.0
	 */
	public static RSAKeyPair random() {
		try {
			KeyPair keyPair = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM);
			// RSA算法生成的KeyPair不会为null，因为Java 平台的每个实现都必须支持RSA算法
			return new RSAKeyPair((RSAPublicKey) keyPair.getPublic(), (RSAPrivateKey) keyPair.getPrivate());
		} catch (NoSuchAlgorithmException e) {
			// 正常不会抛出，因为Java 平台的每个实现都必须支持RSA算法
			throw ExceptionUtils.asRuntimeException(e);
		}
	}

	/**
	 * 生成指定长度的随机 RSA 密钥对
	 * <p>支持 1024/2048/4096 三种标准长度。</p>
	 *
	 * <h3>安全建议</h3>
	 * <ul>
	 *   <li>生产环境：推荐 ≥ 2048 位</li>
	 *   <li>测试环境：1024 位可提高性能</li>
	 *   <li>高安全需求：建议 4096 位</li>
	 * </ul>
	 *
	 * @param keySize 密钥长度（bit），有效值：1024/2048/4096
	 * @return 包含完整公钥与私钥的 RSAKeyPair 实例
	 * @throws IllegalArgumentException 当 {@code keySize} 不在允许集合中时抛出
	 * @since 1.0.0
	 */
	public static RSAKeyPair random(final int keySize) {
		Validate.isTrue(CryptoConstants.RSA_KEY_SIZE_SET.contains(keySize), "keySize 必须为 1024/2048/4096");
		try {
			KeyPair keyPair = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, keySize);
			// RSA算法生成的KeyPair不会为null，因为Java 平台的每个实现都必须支持RSA算法
			return new RSAKeyPair((RSAPublicKey) keyPair.getPublic(), (RSAPrivateKey) keyPair.getPrivate());
		} catch (NoSuchAlgorithmException e) {
			// 正常不会抛出，因为Java 平台的每个实现都必须支持RSA算法
			throw ExceptionUtils.asRuntimeException(e);
		}
	}

	/**
	 * 从现有 KeyPair 构建 RSAKey
	 * <p>将标准 {@link java.security.KeyPair} 转换为类型安全的 RSAKey。</p>
	 *
	 * <h3>转换要求</h3>
	 * <ul>
	 *   <li>公钥必须为 {@link java.security.interfaces.RSAPublicKey}</li>
	 *   <li>私钥必须为 {@link java.security.interfaces.RSAPrivateKey}</li>
	 * </ul>
	 *
	 * @param keyPair 标准密钥对对象，不能为 null
	 * @return 包含对应公钥与私钥的 RSAKeyPair 实例
	 * @throws IllegalArgumentException 当任一键为 null 或非 RSA 类型时抛出
	 * @since 1.0.0
	 */
	public static RSAKeyPair fromKeyPair(final KeyPair keyPair) {
		Validate.notNull(keyPair, "keyPair不可为 null");

		PublicKey publicKey = keyPair.getPublic();
		PrivateKey privateKey = keyPair.getPrivate();
		if (publicKey instanceof RSAPublicKey && privateKey instanceof RSAPrivateKey) {
			return new RSAKeyPair((RSAPublicKey) publicKey, (RSAPrivateKey) privateKey);
		}
		throw new IllegalArgumentException("必须为RSA类型的密钥对");
	}

	/**
	 * 从原始字节数组构建 RSAKey
	 * <p>解析 X.509（公钥）与未加密 PKCS#8（私钥）字节数组，重建密钥。</p>
	 *
	 * <h3>格式要求</h3>
	 * <table border="1">
	 *   <tr><th>密钥类型</th><th>编码格式</th><th>标准</th></tr>
	 *   <tr><td>公钥</td><td>X.509 SubjectPublicKeyInfo</td><td>RFC 5280</td></tr>
	 *   <tr><td>私钥</td><td>PKCS#8（未加密）</td><td>RFC 5208</td></tr>
	 * </table>
	 *
	 * @param publicKeyBytes  X.509 公钥字节数组，可为空（为空时不解析公钥）
	 * @param privateKeyBytes 未加密 PKCS#8 私钥字节数组，可为空（为空时不解析私钥）
	 * @return 包含解析后密钥的 RSAKeyPair 实例（至少需提供其一，否则触发构造校验异常）
	 * @throws InvalidKeySpecException  当字节数组不符合密钥规范时抛出
	 * @throws IllegalArgumentException 当解析结果非 RSA 类型或最终未提供任一密钥时抛出
	 * @since 1.0.0
	 */
	public static RSAKeyPair fromBytes(final byte[] publicKeyBytes, final byte[] privateKeyBytes) throws InvalidKeySpecException {
		try {
			RSAPublicKey rsaPublicKey = null;
			if (ArrayUtils.isNotEmpty(publicKeyBytes)) {
				PublicKey publicKey = KeyPairUtils.getPublicKeyFromX509EncodedKey(CryptoConstants.RSA_ALGORITHM,
					publicKeyBytes);
				if (publicKey instanceof RSAPublicKey) {
					rsaPublicKey = (RSAPublicKey) publicKey;
				} else {
					throw new IllegalArgumentException("必须为RSA类型的密钥对");
				}
			}

			RSAPrivateKey rsaPrivateKey = null;
			if (ArrayUtils.isNotEmpty(publicKeyBytes)) {
				PrivateKey privateKey = KeyPairUtils.getPrivateKeyFromPKCS8EncodedKey(CryptoConstants.RSA_ALGORITHM,
					privateKeyBytes);
				if (privateKey instanceof RSAPrivateKey) {
					rsaPrivateKey = (RSAPrivateKey) privateKey;
				} else {
					throw new IllegalArgumentException("必须为RSA类型的密钥对");
				}
			}

			return new RSAKeyPair(rsaPublicKey, rsaPrivateKey);
		} catch (NoSuchAlgorithmException e) {
			// 正常不会抛出，因为Java 平台的每个实现都必须支持RSA算法
			throw ExceptionUtils.asRuntimeException(e);
		}
	}

	/**
	 * 从 Base64 字符串构建 RSAKey
	 * <p>解析 Base64 编码的 X.509 公钥与未加密 PKCS#8 私钥字符串，重建密钥。</p>
	 *
	 * <h3>字符串格式</h3>
	 * <ul>
	 *   <li>标准 Base64（非 URL 安全）</li>
	 *   <li>可含换行与空白（自动过滤）</li>
	 *   <li>任一输入为空或无效将解析为 null，最终至少需提供其一</li>
	 * </ul>
	 *
	 * @param publicKeyString  Base64 X.509 公钥字符串，可为空（为空时不解析公钥）
	 * @param privateKeyString Base64 未加密 PKCS#8 私钥字符串，可为空（为空时不解析私钥）
	 * @return 包含解析后密钥的 RSAKeyPair 实例（至少需提供其一，否则触发构造校验异常）
	 * @throws InvalidKeySpecException  当字符串不符合密钥规范时抛出
	 * @throws IllegalArgumentException 当解析结果非 RSA 类型或最终未提供任一密钥时抛出
	 * @since 1.0.0
	 */
	public static RSAKeyPair fromBase64String(final String publicKeyString, final String privateKeyString) throws InvalidKeySpecException {
		try {
			RSAPublicKey rsaPublicKey = null;
			if (StringUtils.isNotBlank(publicKeyString)) {
				PublicKey publicKey = KeyPairUtils.getPublicKeyFromX509Base64String(CryptoConstants.RSA_ALGORITHM,
					publicKeyString);
				if (publicKey instanceof RSAPublicKey) {
					rsaPublicKey = (RSAPublicKey) publicKey;
				} else {
					throw new IllegalArgumentException("必须为RSA类型的密钥对");
				}
			}

			RSAPrivateKey rsaPrivateKey = null;
			if (StringUtils.isNotBlank(privateKeyString)) {
				PrivateKey privateKey = KeyPairUtils.getPrivateKeyFromPKCS8Base64String(CryptoConstants.RSA_ALGORITHM,
					privateKeyString);
				if (privateKey instanceof RSAPrivateKey) {
					rsaPrivateKey = (RSAPrivateKey) privateKey;
				} else {
					throw new IllegalArgumentException("必须为RSA类型的密钥对");
				}
			}

			return new RSAKeyPair(rsaPublicKey, rsaPrivateKey);
		} catch (NoSuchAlgorithmException e) {
			// 正常不会抛出，因为Java 平台的每个实现都必须支持RSA算法
			throw ExceptionUtils.asRuntimeException(e);
		}
	}

	/**
	 * 获取当前 RSA 公钥
	 *
	 * @return 当前公钥，可能为 null
	 * @since 1.0.0
	 */
	public RSAPublicKey getPublicKey() {
		return publicKey;
	}

	/**
	 * 获取当前 RSA 私钥
	 *
	 * @return 当前私钥，可能为 null
	 * @since 1.0.0
	 */
	public RSAPrivateKey getPrivateKey() {
		return privateKey;
	}
}