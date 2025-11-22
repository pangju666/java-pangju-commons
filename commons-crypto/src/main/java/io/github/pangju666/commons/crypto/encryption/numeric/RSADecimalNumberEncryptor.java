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

package io.github.pangju666.commons.crypto.encryption.numeric;

import io.github.pangju666.commons.crypto.encryption.binary.RSABinaryEncryptor;
import io.github.pangju666.commons.crypto.key.RSAKeyPair;
import io.github.pangju666.commons.crypto.transformation.RSATransformation;
import io.github.pangju666.commons.crypto.transformation.impl.RSAOEAPWithSHA256Transformation;
import io.github.pangju666.commons.crypto.transformation.impl.RSAPKCS1PaddingTransformation;
import org.apache.commons.lang3.Validate;
import org.jasypt.commons.CommonUtils;
import org.jasypt.exceptions.AlreadyInitializedException;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.numeric.DecimalNumberEncryptor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Objects;

/**
 * RSA 高精度浮点数加密解密器
 * <p>实现 {@link DecimalNumberEncryptor}，对 {@link BigDecimal} 执行非对称加/解密，按“密文 + 4 字节长度尾(MSB)”封装，保留原始标度。</p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li>非对称加密：公钥加密、私钥解密</li>
 *   <li>方案可插拔：默认 OAEPWithSHA-256，兼容 PKCS#1 v1.5</li>
 *   <li>精度保留：保留原始 scale，不损失数值精度</li>
 *   <li>数据封装：unscaledValue 存储“密文 + 4 字节长度尾(MSB)”</li>
 * </ul>
 *
 * <h3>使用说明</h3>
 * <ul>
 *   <li>默认不包含密钥；使用前需通过 {@code setPublicKey}/{@code setPrivateKey} 或 {@code setKeyPair} 配置</li>
 *   <li>惰性初始化：首次加/解密时计算并缓存分块大小</li>
 * </ul>
 *
 * <h3>典型应用场景</h3>
 * <ul>
 *   <li>金融系统金额加密</li>
 *   <li>科学计算数据保护</li>
 *   <li>数据库敏感浮点字段加密</li>
 *   <li>需要精确小数运算的安全协议</li>
 * </ul>
 *
 * <h3>技术实现</h3>
 * <ul>
 *   <li>加密：BigDecimal → 提取 scale/unscaledValue → RSA 加密 unscaledValue → 追加长度尾 → 构造新 BigDecimal</li>
 *   <li>解密：密文 BigDecimal → 解析打包 → RSA 解密 unscaledValue → 复原原始 BigDecimal（保留 scale）</li>
 * </ul>
 *
 * @author pangju666
 * @see DecimalNumberEncryptor
 * @see RSABinaryEncryptor
 * @see RSATransformation
 * @see RSAKeyPair
 * @since 1.0.0
 */
public final class RSADecimalNumberEncryptor implements DecimalNumberEncryptor {
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
	public RSADecimalNumberEncryptor() {
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
	public RSADecimalNumberEncryptor(final RSATransformation transformation) {
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
	public RSADecimalNumberEncryptor(final RSABinaryEncryptor binaryEncryptor) {
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
	 * <p>该方法会在 {@link #encrypt(BigDecimal)} 与 {@link #decrypt(BigDecimal)} 调用时自动触发。</p>
	 *
	 * @throws EncryptionInitializationException 当算法不支持或密钥规格解析失败时抛出
	 * @since 1.0.0
	 */
	public void initialize() {
		binaryEncryptor.initialize();
	}

	/**
	 * 加密 BigDecimal 数值
	 * <p>实现 {@link DecimalNumberEncryptor}，对无标度字节执行 RSA 公钥加密，并在末尾附加 4 字节长度尾。</p>
	 *
	 * <h3>数据格式</h3>
	 * <pre>
	 * +----------------+----------------+
	 * | RSA加密数据体   | 4字节长度尾(MSB) |
	 * +----------------+----------------+
	 * </pre>
	 *
	 * @param number 待加密数值；null 返回 null
	 * @return 加密后的 BigDecimal（scale 与原值一致；unscaledValue 为密文字节）
	 * @throws EncryptionInitializationException 当算法不支持或密钥规格解析失败时抛出
	 * @throws EncryptionOperationNotPossibleException 公钥未设置或加密失败
	 * @since 1.0.0
	 */
	@Override
	public BigDecimal encrypt(final BigDecimal number) {
		if (Objects.isNull(number)) {
			return null;
		}

		try {
			final int scale = number.scale();
			final BigInteger unscaledMessage = number.unscaledValue();
			final byte[] messageBytes = unscaledMessage.toByteArray();
			final byte[] encryptedMessage = this.binaryEncryptor.encrypt(messageBytes);
			final byte[] encryptedMessageLengthBytes = NumberUtils.byteArrayFromInt(encryptedMessage.length);
			final byte[] encryptionResult = CommonUtils.appendArrays(encryptedMessage, encryptedMessageLengthBytes);
			return new BigDecimal(new BigInteger(encryptionResult), scale);
		} catch (EncryptionInitializationException | EncryptionOperationNotPossibleException e) {
			throw e;
		} catch (Exception e) {
			throw new EncryptionOperationNotPossibleException();
		}
	}

	/**
	 * 解密 BigDecimal 数值
	 * <p>按“RSA加密数据体 + 4字节长度尾(MSB)”解析 unscaledValue，并用保留的 scale 还原原始数值。</p>
	 *
	 * <h3>验证</h3>
	 * <ul>
	 *   <li>长度尾与实际密文长度一致</li>
	 *   <li>私钥与加密公钥配对，方案一致</li>
	 *   <li>scale 为原始标度</li>
	 * </ul>
	 *
	 * @param encryptedNumber 由 {@link #encrypt(BigDecimal)} 生成的密文；null 返回 null
	 * @return 原始数值
	 * @throws EncryptionInitializationException 当算法不支持或密钥规格解析失败时抛出
	 * @throws EncryptionOperationNotPossibleException 输入格式非法、私钥未设置/不匹配或解密失败
	 * @since 1.0.0
	 */
	@Override
	public BigDecimal decrypt(final BigDecimal encryptedNumber) {
		if (Objects.isNull(encryptedNumber)) {
			return null;
		}

		try {
			int scale = encryptedNumber.scale();
			BigInteger unscaledEncryptedMessage = encryptedNumber.unscaledValue();
			byte[] encryptedMessageBytes = unscaledEncryptedMessage.toByteArray();
			encryptedMessageBytes = NumberUtils.processBigIntegerEncryptedByteArray(
				encryptedMessageBytes, encryptedNumber.signum());
			byte[] message = binaryEncryptor.decrypt(encryptedMessageBytes);
			return new BigDecimal(new BigInteger(message), scale);
		} catch (EncryptionInitializationException | EncryptionOperationNotPossibleException e) {
			throw e;
		} catch (Exception e) {
			throw new EncryptionOperationNotPossibleException();
		}
	}
}
