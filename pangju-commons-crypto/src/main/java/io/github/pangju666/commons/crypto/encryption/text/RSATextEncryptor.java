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
import org.apache.commons.lang3.Validate;
import org.jasypt.exceptions.AlreadyInitializedException;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.TextEncryptor;

import java.nio.charset.StandardCharsets;

/**
 * 基于RSA算法的文本加密解密器，提供安全的非对称文本加解密能力
 * <p>
 * 本类针对字符串数据提供两种编码格式支持：
 * <ul>
 *   <li><b>Base64编码</b> - 适用于网络传输和紧凑存储，编码后数据体积较小</li>
 *   <li><b>十六进制编码</b> - 便于人工阅读调试，但数据体积膨胀率较高</li>
 * </ul>
 * 采用标准的公钥加密/私钥解密模式，保证端到端数据安全。
 * </p>
 *
 * <p><b>线程安全保证：</b>
 * <ul>
 *   <li>配置方法通过synchronized实现原子性操作</li>
 *   <li>核心加密器实例(final修饰)初始化后不可变</li>
 *   <li>加解密操作无状态且幂等</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 * @see TextEncryptor
 * @see RSABinaryEncryptor
 * @see RSATransformation
 * @see RSAKey
 */
public final class RSATextEncryptor implements TextEncryptor {
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
	public RSATextEncryptor() {
		this.binaryEncryptor = new RSABinaryEncryptor();
	}

	/**
	 * 构造方法（使用默认密钥长度和指定加密方案）
	 *
	 * @param transformation 加密方案
	 * @since 1.0.0
	 */
	public RSATextEncryptor(final RSATransformation transformation) {
		this.binaryEncryptor = new RSABinaryEncryptor(transformation);
	}

	/**
	 * 构造方法（使用指定密钥长度和默认加密方案）
	 *
	 * @param keySize RSA 密钥长度（单位：bit）
	 * @since 1.0.0
	 */
	public RSATextEncryptor(final int keySize) {
		this.binaryEncryptor = new RSABinaryEncryptor(keySize);
	}

	/**
	 * 构造方法（使用指定密钥长度和加密方案）
	 *
	 * @param keySize        密钥长度（单位：bit）
	 * @param transformation 加密方案
	 * @since 1.0.0
	 */
	public RSATextEncryptor(final int keySize, final RSATransformation transformation) {
		this.binaryEncryptor = new RSABinaryEncryptor(keySize, transformation);
	}

	/**
	 * 构造方法（使用已有密钥和默认加密方案）
	 *
	 * @param key 预生成的 RSA 密钥对
	 * @since 1.0.0
	 */
	public RSATextEncryptor(final RSAKey key) {
		this.binaryEncryptor = new RSABinaryEncryptor(key);
	}

	/**
	 * 构造方法（使用已有密钥和指定加密方案）
	 *
	 * @param key            预生成的 RSA 密钥对
	 * @param transformation 加密方案
	 * @since 1.0.0
	 */
	public RSATextEncryptor(final RSAKey key, final RSATransformation transformation) {
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
	public RSATextEncryptor(final RSABinaryEncryptor binaryEncryptor) {
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
	 * @throws NullPointerException 当传入null参数时抛出
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
	 *
	 * @since 1.0.0
	 */
	public void setKey(final RSAKey key) {
		this.binaryEncryptor.setKey(key);
	}

	/**
	 * Base64格式加密文本
	 * <p>
	 * 处理流程：
	 * <ol>
	 *   <li>输入有效性校验（空值处理）</li>
	 *   <li>UTF-8编码转换</li>
	 *   <li>RSA公钥加密原始字节</li>
	 *   <li>Base64 URL安全编码</li>
	 * </ol>
	 * 输出特征：
	 * <ul>
	 *   <li>不使用填充字符（符合RFC 4648）</li>
	 *   <li>编码结果不包含换行符</li>
	 * </ul>
	 * </p>
	 *
	 * @param message 待加密文本，允许：
	 *                <ul>
	 *                  <li>常规字符串（1-190字节，受密钥长度限制）</li>
	 *                  <li>空字符串（返回空）</li>
	 *                  <li>null（返回空）</li>
	 *                </ul>
	 * @return Base64编码的加密结果字符串
	 * @throws EncryptionOperationNotPossibleException 当：
	 *         <ul>
	 *           <li>明文长度超过RSA算法限制</li>
	 *           <li>公钥未正确初始化</li>
	 *           <li>加密过程发生不可恢复错误</li>
	 *         </ul>
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
	 * <p>
	 * 重要约束：
	 * <ul>
	 *   <li>必须使用与加密时相同的私钥</li>
	 *   <li>Base64编码格式需与加密输出一致</li>
	 * </ul>
	 * 异常处理：
	 * <ul>
	 *   <li>自动处理URL安全的Base64编码</li>
	 *   <li>兼容标准Base64编码输入</li>
	 * </ul>
	 * </p>
	 *
	 * @param encryptedMessage Base64格式的加密字符串，允许：
	 *                         <ul>
	 *                           <li>标准密文字符串</li>
	 *                           <li>空字符串（返回空）</li>
	 *                           <li>null（返回空）</li>
	 *                         </ul>
	 * @return 原始明文字符串，保证：
	 *         <ul>
	 *           <li>字符编码与加密前一致（UTF-8）</li>
	 *           <li>大小写敏感</li>
	 *           <li>空白字符保留</li>
	 *         </ul>
	 * @throws EncryptionOperationNotPossibleException 当：
	 *         <ul>
	 *           <li>私钥不匹配或未设置</li>
	 *           <li>输入格式非法</li>
	 *           <li>解密过程错误</li>
	 *         </ul>
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
	 * 十六进制格式加密文本
	 * <p>
	 * 与Base64加密的区别：
	 * <ul>
	 *   <li>输出为小写十六进制字符串</li>
	 *   <li>数据体积增加100%</li>
	 *   <li>适合日志记录和调试输出</li>
	 * </ul>
	 * 典型用例：
	 * <ul>
	 *   <li>需要人工核对加密数据的场景</li>
	 *   <li>兼容不支持Base64的旧系统</li>
	 * </ul>
	 * </p>
	 *
	 * @param message 待加密文本，处理规则同{@link #encrypt(String)}
	 * @return 十六进制小写字符串
	 * @see #encrypt(String)
	 * @since 1.0.0
	 */
	public String encryptToHexString(final String message) {
		if (StringUtils.isBlank(message)) {
			return StringUtils.EMPTY;
		}
		return Hex.encodeHexString(binaryEncryptor.encrypt(message.getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * 解密十六进制格式加密文本
	 * <p>
	 * 输入容错处理：
	 * <ul>
	 *   <li>自动过滤非十六进制字符（0-9,a-f,A-F）</li>
	 *   <li>支持包含分隔符的格式（如"48-65-6c"）</li>
	 *   <li>奇数字符自动前补零处理</li>
	 * </ul>
	 * 安全警告：
	 * <ul>
	 *   <li>不验证输入数据的完整性</li>
	 *   <li>可能丢失原始数据的前导零</li>
	 * </ul>
	 * </p>
	 *
	 * @param encryptedMessage 十六进制格式字符串，允许：
	 *                         <ul>
	 *                           <li>连续字符串（推荐）</li>
	 *                           <li>带分隔符形式（自动过滤）</li>
	 *                           <li>大小写混合</li>
	 *                         </ul>
	 * @return 解密后的原始字符串
	 * @throws EncryptionOperationNotPossibleException 当：
	 *         <ul>
	 *           <li>包含非法十六进制字符</li>
	 *           <li>解密过程失败</li>
	 *         </ul>
	 * @see #decrypt(String)
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