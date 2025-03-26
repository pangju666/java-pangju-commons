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
import org.jasypt.util.numeric.IntegerNumberEncryptor;

import java.math.BigInteger;
import java.util.Objects;

/**
 * RSA算法整数加密器（公钥加密，私钥解密），提供大整数安全加解密能力
 * <p>
 * 本类通过将BigInteger转换为字节数组进行加密，保留原始数值的数学特性，
 * 适用于需要精确加密/解密整数数值的场景，如金融交易、密码学协议等。
 * </p>
 *
 * <p><b>线程安全实现：</b>
 * <ul>
 *   <li>配置方法(setKey/setTransformation)使用synchronized保证原子性</li>
 *   <li>底层加密器实例(final修饰)初始化后不可变</li>
 *   <li>加密/解密操作本身无状态，支持并发调用</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 * @see IntegerNumberEncryptor
 * @see RSABinaryEncryptor
 * @see RSATransformation
 * @see RSAKey
 */
public final class RSAIntegerNumberEncryptor implements IntegerNumberEncryptor {
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
	public RSAIntegerNumberEncryptor() {
		this.binaryEncryptor = new RSABinaryEncryptor();
	}

	/**
	 * 构造方法（使用默认密钥长度和指定加密方案）
	 *
	 * @param transformation 加密方案
	 * @since 1.0.0
	 */
	public RSAIntegerNumberEncryptor(final RSATransformation transformation) {
		this.binaryEncryptor = new RSABinaryEncryptor(transformation);
	}

	/**
	 * 构造方法（使用指定密钥长度和默认加密方案）
	 *
	 * @param keySize RSA 密钥长度（单位：bit）
	 * @since 1.0.0
	 */
	public RSAIntegerNumberEncryptor(final int keySize) {
		this.binaryEncryptor = new RSABinaryEncryptor(keySize);
	}

	/**
	 * 构造方法（使用指定密钥长度和加密方案）
	 *
	 * @param keySize        密钥长度（单位：bit）
	 * @param transformation 加密方案
	 * @since 1.0.0
	 */
	public RSAIntegerNumberEncryptor(final int keySize, final RSATransformation transformation) {
		this.binaryEncryptor = new RSABinaryEncryptor(keySize, transformation);
	}

	/**
	 * 构造方法（使用已有密钥和默认加密方案）
	 *
	 * @param key 预生成的 RSA 密钥对
	 *
	 * @since 1.0.0
	 */
	public RSAIntegerNumberEncryptor(final RSAKey key) {
		this.binaryEncryptor = new RSABinaryEncryptor(key);
	}

	/**
	 * 构造方法（使用已有密钥和指定加密方案）
	 *
	 * @param key            预生成的 RSA 密钥对
	 * @param transformation 加密方案
	 * @since 1.0.0
	 */
	public RSAIntegerNumberEncryptor(final RSAKey key, final RSATransformation transformation) {
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
	public RSAIntegerNumberEncryptor(final RSABinaryEncryptor binaryEncryptor) {
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
	 * 加密BigInteger整数
	 * <p>
	 * 加密流程：
	 * <ol>
	 *   <li>将BigInteger转换为二进制补码格式字节数组</li>
	 *   <li>使用RSA公钥加密字节数据</li>
	 *   <li>在加密结果前附加4字节长度信息</li>
	 *   <li>组合生成新的BigInteger</li>
	 * </ol>
	 * </p>
	 *
	 * @param number 待加密整数，允许为null
	 * @return 加密后的BigInteger对象，具有以下特征：
	 *         <ul>
	 *           <li>符号位表示加密数据的符号</li>
	 *           <li>数值部分为加密后的二进制数据</li>
	 *           <li>null输入返回null</li>
	 *         </ul>
	 * @throws EncryptionInitializationException 当：
	 *         <ul>
	 *           <li>未设置公钥</li>
	 *           <li>加密方案未配置</li>
	 *         </ul>
	 * @throws EncryptionOperationNotPossibleException 当：
	 *         <ul>
	 *           <li>输入数据超过RSA最大加密长度</li>
	 *           <li>加密过程发生错误</li>
	 *         </ul>
	 * @since 1.0.0
	 */
	@Override
	public BigInteger encrypt(final BigInteger number) {
		if (Objects.isNull(number)) {
			return null;
		}
		final byte[] messageBytes = number.toByteArray();
		final byte[] encryptedMessage = this.binaryEncryptor.encrypt(messageBytes);
		final byte[] encryptedMessageLengthBytes = NumberUtils.byteArrayFromInt(encryptedMessage.length);
		final byte[] encryptionResult = CommonUtils.appendArrays(encryptedMessage, encryptedMessageLengthBytes);
		return new BigInteger(encryptionResult);
	}

	/**
	 * 解密BigInteger密文
	 * <p>
	 * 解密流程：
	 * <ol>
	 *   <li>从加密BigInteger中提取二进制补码字节数组</li>
	 *   <li>解析前4字节获取加密数据长度</li>
	 *   <li>使用RSA私钥解密数据部分</li>
	 *   <li>重构原始BigInteger</li>
	 * </ol>
	 * </p>
	 *
	 * @param encryptedNumber 加密后的BigInteger，允许为null
	 * @return 解密后的原始整数，保证：
	 *         <ul>
	 *           <li>数值与加密前完全一致</li>
	 *           <li>符号位保留原始状态</li>
	 *           <li>null输入返回null</li>
	 *         </ul>
	 * @throws EncryptionInitializationException 当：
	 *         <ul>
	 *           <li>未设置私钥</li>
	 *           <li>加密方案不匹配</li>
	 *         </ul>
	 * @throws EncryptionOperationNotPossibleException 当：
	 *         <ul>
	 *           <li>输入数据格式错误</li>
	 *           <li>解密失败（如密钥不匹配）</li>
	 *         </ul>
	 * @since 1.0.0
	 */
	@Override
	public BigInteger decrypt(final BigInteger encryptedNumber) {
		if (Objects.isNull(encryptedNumber)) {
			return null;
		}
		byte[] encryptedMessageBytes = encryptedNumber.toByteArray();
		encryptedMessageBytes = NumberUtils.processBigIntegerEncryptedByteArray(
			encryptedMessageBytes, encryptedNumber.signum());
		byte[] message = binaryEncryptor.decrypt(encryptedMessageBytes);
		return new BigInteger(message);
	}
}
