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
import io.github.pangju666.commons.crypto.key.RSAKey;
import io.github.pangju666.commons.crypto.transformation.RSATransformation;
import org.apache.commons.lang3.Validate;
import org.jasypt.commons.CommonUtils;
import org.jasypt.exceptions.AlreadyInitializedException;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.numeric.DecimalNumberEncryptor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/**
 * RSA算法浮点数加密器（公钥加密，私钥解密），提供BigDecimal类型数值的安全加解密能力
 * <p>
 * 本类通过分离BigDecimal的标度(scale)和无标度值(unscaled value)实现浮点数加密，
 * 加密过程保留原始数值的小数位数信息，确保解密后能完全还原原始精度。
 * </p>
 *
 * <p><b>线程安全：</b>
 * <ul>
 *   <li>通过方法级同步控制保证配置操作的原子性</li>
 *   <li>底层加密器实例(final修饰)初始化后不可变</li>
 *   <li>加密/解密操作本身无状态，可并发执行</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 * @see DecimalNumberEncryptor
 * @see RSABinaryEncryptor
 * @see RSATransformation
 * @see RSAKey
 */
public final class RSADecimalNumberEncryptor implements DecimalNumberEncryptor {
	/**
	 * 核心RSA二进制加密处理器，执行底层加密运算
	 * <p>
	 * 设计特性：
	 * <ul>
	 *   <li>通过构造函数注入，生命周期与宿主对象一致</li>
	 *   <li>配置变更通过代理模式实现</li>
	 *   <li>实际加密操作委托给此实例</li>
	 * </ul>
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private final RSABinaryEncryptor binaryEncryptor;

	/**
	 * 构建使用默认安全配置的加密器
	 * <p>
	 * 默认安全参数：
	 * <ul>
	 *   <li>密钥强度：2048位RSA密钥</li>
	 *   <li>加密方案：RSA/ECB/OAEPWithSHA-256AndMGF1Padding</li>
	 *   <li>字符编码：UTF-8</li>
	 *   <li>填充方式：OAEP最优非对称加密填充</li>
	 * </ul>
	 * </p>
	 *
	 * @throws EncryptionInitializationException 当密钥生成失败时抛出
	 * @since 1.0.0
	 */
	public RSADecimalNumberEncryptor() {
		this.binaryEncryptor = new RSABinaryEncryptor();
	}

	/**
	 * 构造方法（使用默认密钥长度和指定加密方案）
	 *
	 * @param transformation 加密方案
	 * @since 1.0.0
	 */
	public RSADecimalNumberEncryptor(final RSATransformation transformation) {
		this.binaryEncryptor = new RSABinaryEncryptor(transformation);
	}

	/**
	 * 构造方法（使用指定密钥长度和默认加密方案）
	 *
	 * @param keySize RSA 密钥长度（单位：bit）
	 * @since 1.0.0
	 */
	public RSADecimalNumberEncryptor(final int keySize) {
		this.binaryEncryptor = new RSABinaryEncryptor(keySize);
	}

	/**
	 * 构造方法（使用指定密钥长度和加密方案）
	 *
	 * @param keySize        密钥长度（单位：bit）
	 * @param transformation 加密方案
	 * @since 1.0.0
	 */
	public RSADecimalNumberEncryptor(final int keySize, final RSATransformation transformation) {
		this.binaryEncryptor = new RSABinaryEncryptor(keySize, transformation);
	}

	/**
	 * 构造方法（使用已有密钥和默认加密方案）
	 *
	 * @param key 预生成的 RSA 密钥对
	 *
	 * @since 1.0.0
	 */
	public RSADecimalNumberEncryptor(final RSAKey key) {
		this.binaryEncryptor = new RSABinaryEncryptor(key);
	}

	/**
	 * 构造方法（使用已有密钥和指定加密方案）
	 *
	 * @param key            预生成的 RSA 密钥对
	 * @param transformation 加密方案
	 * @since 1.0.0
	 */
	public RSADecimalNumberEncryptor(final RSAKey key, final RSATransformation transformation) {
		this.binaryEncryptor = new RSABinaryEncryptor(key, transformation);
	}

	/**
	 * 使用预配置的加密器构建实例
	 * <p>
	 * 适用场景：
	 * <ul>
	 *   <li>复用已有加密器配置</li>
	 *   <li>需要自定义生命周期管理</li>
	 *   <li>实现加密器热切换</li>
	 * </ul>
	 * </p>
	 *
	 * @param binaryEncryptor 预初始化的加密器实例，需满足：
	 *                        <ul>
	 *                          <li>已完成密钥配置</li>
	 *                          <li>已设置有效加密方案</li>
	 *                        </ul>
	 * @throws NullPointerException 当传入null参数时抛出
	 * @since 1.0.0
	 */
	public RSADecimalNumberEncryptor(final RSABinaryEncryptor binaryEncryptor) {
		Validate.notNull(binaryEncryptor, "binaryEncryptor 不能为 null");
		this.binaryEncryptor = binaryEncryptor;
	}

	/**
	 * 配置加密算法方案
	 * <p>
	 * 重要约束：
	 * <ul>
	 *   <li>必须在首次加密操作前调用</li>
	 *   <li>修改方案会导致已有加密数据不可解密</li>
	 *   <li>不同方案的密钥可能不兼容</li>
	 * </ul>
	 * </p>
	 *
	 * @param transformation 加密方案枚举实例，支持：
	 *                       <ul>
	 *                         <li>PKCS1v1.5填充方案</li>
	 *                         <li>OAEP填充方案（推荐）</li>
	 *                       </ul>
	 * @throws AlreadyInitializedException 若已执行过加密操作后调用
	 * @throws NullPointerException        当传入null参数时抛出
	 * @since 1.0.0
	 */
	public void setTransformation(final RSATransformation transformation) {
		this.binaryEncryptor.setTransformation(transformation);
	}

	/**
	 * 设置 RSA 密钥（必须在初始化前调用）
	 *
	 * @param key 新的密钥对
	 * @throws AlreadyInitializedException 如果已经初始化后调用
	 * @since 1.0.0
	 */
	public void setKey(final RSAKey key) {
		this.binaryEncryptor.setKey(key);
	}

	/**
	 * 加密BigDecimal数值
	 * <p>
	 * 加密流程：
	 * <ol>
	 *   <li>分离数值的标度(scale)和无标度值(unscaledValue)</li>
	 *   <li>将无标度值转换为字节数组</li>
	 *   <li>使用RSA公钥加密字节数组</li>
	 *   <li>将加密结果与原始数据长度组合生成新的无标度值</li>
	 *   <li>返回包含新无标度值和原始标度的BigDecimal</li>
	 * </ol>
	 * </p>
	 *
	 * @param number 待加密的BigDecimal数值，允许为null
	 * @return 加密后的BigDecimal对象，具有以下特征：
	 *         <ul>
	 *           <li>保持原始数值的标度(scale)</li>
	 *           <li>无标度值为加密后的密文数据</li>
	 *           <li>null输入返回null</li>
	 *         </ul>
	 * @throws EncryptionInitializationException 如果未正确初始化密钥或算法
	 * @throws EncryptionOperationNotPossibleException 当：
	 *         <ul>
	 *           <li>输入数据长度超过RSA算法限制</li>
	 *           <li>加密过程出现不可恢复错误</li>
	 *         </ul>
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
	 * 解密BigDecimal数值
	 * <p>
	 * 解密流程：
	 * <ol>
	 *   <li>从加密数值中提取标度和加密后的无标度值</li>
	 *   <li>处理字节数组的符号位和补码格式</li>
	 *   <li>使用RSA私钥解密字节数组</li>
	 *   <li>组合解密结果和原始标度重建BigDecimal</li>
	 * </ol>
	 * </p>
	 *
	 * @param encryptedNumber 加密后的BigDecimal数值，允许为null
	 * @return 解密后的原始数值，具有以下特征：
	 *         <ul>
	 *           <li>与加密前完全相同的标度</li>
	 *           <li>精确还原原始无标度值</li>
	 *           <li>null输入返回null</li>
	 *         </ul>
	 * @throws EncryptionInitializationException 如果未正确初始化密钥或算法
	 * @throws EncryptionOperationNotPossibleException 当：
	 *         <ul>
	 *           <li>输入数据格式不符合加密规范</li>
	 *           <li>解密失败（如密钥不匹配）</li>
	 *           <li>数据完整性受损</li>
	 *         </ul>
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
