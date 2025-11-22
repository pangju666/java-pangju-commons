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
import io.github.pangju666.commons.crypto.key.RSAKeyPair;
import io.github.pangju666.commons.crypto.transformation.RSATransformation;
import io.github.pangju666.commons.crypto.transformation.impl.RSAOEAPWithSHA256Transformation;
import io.github.pangju666.commons.crypto.transformation.impl.RSAPKCS1PaddingTransformation;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jasypt.exceptions.AlreadyInitializedException;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.TextEncryptor;

import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * RSA 文本加密解密器
 * <p>实现 {@link TextEncryptor}，提供基于 RSA 的字符串加/解密能力，输出标准 Base64 编码密文。</p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li>非对称加密：公钥加密，私钥解密</li>
 *   <li>方案可插拔：支持 OAEPWithSHA-256（推荐）与 PKCS#1 v1.5（兼容）</li>
 *   <li>编码支持：标准 Base64</li>
 *   <li>编码一致性：强制使用 UTF-8</li>
 * </ul>
 *
 * <h3>使用说明</h3>
 * <ul>
 *   <li>默认不包含密钥；使用前需通过 {@code setPublicKey}/{@code setPrivateKey} 或 {@code setKeyPair} 配置密钥</li>
 *   <li>惰性初始化：首次加/解密时计算并缓存分块大小</li>
 * </ul>
 *
 * <h3>典型用例</h3>
 * <ul>
 *   <li>敏感配置加密存储</li>
 *   <li>API 通信端到端加密</li>
 *   <li>数据库字段级加密</li>
 *   <li>日志敏感信息脱敏</li>
 * </ul>
 *
 * <h3>安全与性能建议</h3>
 * <ul>
 *   <li>大文本采用“RSA + 对称加密”的混合方案</li>
 *   <li>优先使用 OAEP；避免在新场景使用过时的 PKCS#1 v1.5</li>
 *   <li>RSA 单块大小受密钥长度与填充影响</li>
 * </ul>
 *
 * <h3>线程安全</h3>
 * <ul>
 *   <li>不可变引用：内部加密器为 final</li>
 *   <li>同步配置：变更密钥时使用同步保证原子性</li>
 *   <li>无共享状态：加/解密操作幂等</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 * @see TextEncryptor
 * @see RSABinaryEncryptor
 * @see RSATransformation
 * @see RSAKeyPair
 */
public final class RSATextEncryptor implements TextEncryptor {
	/**
	 * 核心 RSA 二进制加密处理器
	 * <p>承载底层加/解密与分块逻辑，随宿主对象生命周期存在。</p>
	 * <ul>
	 *   <li>不可变引用：字段为 final，不可在运行期替换实例</li>
	 *   <li>委托模式：加/解密委托给该处理器</li>
	 *   <li>惰性初始化：首次加/解密时计算并缓存分块大小</li>
	 * </ul>
	 *
	 * @since 1.0.0
	 */
	private final RSABinaryEncryptor binaryEncryptor;

	/**
	 * 构建使用默认配置的加密器实例
	 * <p>
	 * 默认仅配置加密方案为 {@link RSAOEAPWithSHA256Transformation}，不设置密钥。
	 * 使用前需通过 {@link #setPublicKey(RSAPublicKey)} / {@link #setPrivateKey(RSAPrivateKey)} 或 {@link #setKeyPair(RSAKeyPair)} 配置密钥。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	public RSATextEncryptor() {
		this.binaryEncryptor = new RSABinaryEncryptor();
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
	public RSATextEncryptor(final RSATransformation transformation) {
		this.binaryEncryptor = new RSABinaryEncryptor(transformation);
	}

	/**
	 * 使用预配置的二进制加密器构建实例
	 * <p>适用于复用已有密钥与加密方案的场景，避免重复配置。</p>
	 *
	 * <h3>说明</h3>
	 * <ul>
	 *   <li>加密器可未初始化；初始化在首次加/解密时惰性触发</li>
	 *   <li>密钥与加密方案应按需配置</li>
	 * </ul>
	 *
	 * @param binaryEncryptor 二进制加密器实例，必须非 null
	 * @throws NullPointerException 当加密器为 null 时抛出
	 * @see RSABinaryEncryptor
	 * @since 1.0.0
	 */
	public RSATextEncryptor(final RSABinaryEncryptor binaryEncryptor) {
		Validate.notNull(binaryEncryptor, "binaryEncryptor 不能为 null");
		this.binaryEncryptor = binaryEncryptor;
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
	public void setKeyPair(final RSAKeyPair keyPair) {
		this.binaryEncryptor.setKeyPair(keyPair);
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
	public void setPrivateKey(final RSAPrivateKey privateKey) {
		this.binaryEncryptor.setPrivateKey(privateKey);
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
	public void setPublicKey(final RSAPublicKey publicKey) {
		this.binaryEncryptor.setPublicKey(publicKey);
	}

	/**
	 * 初始化加密组件
	 * <p>根据当前配置计算并缓存分块大小（加密/解密），自动检测可用密钥：</p>
	 * <ul>
	 *   <li>存在公钥：初始化加密分块大小</li>
	 *   <li>存在私钥：初始化解密分块大小</li>
	 * </ul>
	 * <p><strong>说明：</strong>该方法为惰性执行且幂等；当未设置任何密钥时不会抛出异常，分块大小保持未初始化状态。</p>
	 * <p>该方法会在 {@link #encrypt(String)} 与 {@link #decrypt(String)} 调用时自动触发。</p>
	 *
	 * @throws EncryptionInitializationException 当算法不支持或密钥规格解析失败时抛出
	 * @since 1.0.0
	 */
	public void initialize() {
		binaryEncryptor.initialize();
	}

	/**
	 * Base64格式加密文本
	 * <p>实现 {@link TextEncryptor}，输出标准 Base64 编码的密文。</p>
	 *
	 * <h3>处理流程</h3>
	 * <ol>
	 *   <li>输入有效性校验（空值→空字符串）</li>
	 *   <li>UTF-8 编码</li>
	 *   <li>RSA 公钥分段加密字节</li>
	 *   <li>标准 Base64 编码</li>
	 * </ol>
	 *
	 * <h3>输出规范</h3>
	 * <ul>
	 *   <li>RFC 4648 标准 Base64（包含填充，且不换行）</li>
	 * </ul>
	 *
	 * @param message 待加密文本，null 或空字符串返回空字符串
	 * @return Base64 编码的密文
	 * @throws EncryptionInitializationException 当算法不支持或密钥规格解析失败时抛出
	 * @throws EncryptionOperationNotPossibleException 公钥未设置或加密失败
	 * @apiNote 对于大文本，建议采用“RSA + 对称加密”混合方案以提升性能与安全性
	 * @see Base64#encodeBase64String(byte[])
	 * @since 1.0.0
	 */
	@Override
	public String encrypt(final String message) {
		if (StringUtils.isBlank(message)) {
			return StringUtils.EMPTY;
		}
		return Base64.encodeBase64String(binaryEncryptor.encrypt(message.getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * 解密 Base64 编码的密文
	 * <p>处理标准 Base64 格式的加密输入。</p>
	 *
	 * <h3>解密要求</h3>
	 * <ul>
	 *   <li>私钥与加密时匹配</li>
	 *   <li>加密方案一致</li>
	 * </ul>
	 *
	 * @param encryptedMessage Base64 格式字符串，null 或空返回空字符串
	 * @return 原始明文（UTF-8）
	 * @throws EncryptionInitializationException 当算法不支持或密钥规格解析失败时抛出
	 * @throws EncryptionOperationNotPossibleException 私钥未设置、输入非法或解密失败
	
	 * @see Base64#decodeBase64(String)
	 * @since 1.0.0
	 */
	@Override
	public String decrypt(final String encryptedMessage) {
		if (StringUtils.isBlank(encryptedMessage)) {
			return StringUtils.EMPTY;
		}
		return new String(binaryEncryptor.decrypt(Base64.decodeBase64(encryptedMessage)),
			StandardCharsets.UTF_8);
	}
}