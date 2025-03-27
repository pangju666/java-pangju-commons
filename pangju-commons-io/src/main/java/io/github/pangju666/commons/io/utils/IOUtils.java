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

package io.github.pangju666.commons.io.utils;

import io.github.pangju666.commons.io.lang.IOConstants;
import org.apache.commons.crypto.stream.CryptoInputStream;
import org.apache.commons.crypto.stream.CryptoOutputStream;
import org.apache.commons.crypto.stream.CtrCryptoInputStream;
import org.apache.commons.crypto.stream.CtrCryptoOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Properties;

/**
 * 增强型IO流操作工具类（继承自 {@link org.apache.commons.io.IOUtils}）
 * <p>提供基于Apache Commons Crypto的AES加解密能力扩展，主要特性：</p>
 *
 * <h3>核心功能模块：</h3>
 * <ul>
 *     <li><strong>AES加解密体系</strong> - 支持CBC/CTR两种加密模式</li>
 *     <li><strong>密码规范管理</strong> - 强制校验密钥长度（128/192/256位）</li>
 *     <li><strong>流式处理优化</strong> - 内存友好的大文件处理能力</li>
 *     <li><strong>线程安全实现</strong> - 所有方法无状态设计</li>
 * </ul>
 *
 * <h3>安全规范：</h3>
 * <ul>
 *     <li>密钥派生：直接使用密码字节（建议结合PBKDF2使用）</li>
 *     <li>IV生成：默认基于密码派生（可通过参数自定义）</li>
 *     <li>加密模式：CBC模式强制使用PKCS5填充，CTR模式无填充</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class IOUtils extends org.apache.commons.io.IOUtils {
	/**
	 * 默认加密配置属性集
	 * <p>包含以下配置：</p>
	 * <ul>
	 *     <li>密码提供者：使用JRE默认提供者</li>
	 *     <li>算法实现：优先选择Native实现（如果可用）</li>
	 *     <li>缓冲区大小：默认256KB</li>
	 * </ul>
	 *
	 * @since 1.0.0
	 */
	private static final Properties DEFAULT_PROPERTIES = new Properties();

	protected IOUtils() {
	}

	/**
	 * AES/CBC模式流加密（默认IV）
	 * <p>实现特性：</p>
	 * <ul>
	 *     <li>使用密码字节作为初始化向量（IV）</li>
	 *     <li>自动处理PKCS5填充</li>
	 *     <li>支持最大2GB流加密</li>
	 * </ul>
	 *
	 * @param inputStream  输入流（必须可读且未关闭）
	 * @param outputStream 输出流（必须可写且未关闭）
	 * @param password     加密密码（长度必须为16字节）
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>密码长度不符合规范</li>
	 *                         <li>流读写异常</li>
	 *                         <li>加密配置错误</li>
	 *                     </ul>
	 * @since 1.0.0
	 */
	public static void encrypt(InputStream inputStream, OutputStream outputStream, byte[] password) throws IOException {
		Validate.isTrue(ArrayUtils.getLength(password) == 16, "password 必须为16字节");
		encrypt(inputStream, outputStream, password, new IvParameterSpec(password),
			IOConstants.AES_CBC_PKCS5_PADDING);
	}

	/**
	 * AES/CBC模式流解密（默认IV）
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>必须使用与加密相同的密码和IV</li>
	 *     <li>输入流必须为加密后的原始数据</li>
	 *     <li>自动处理PKCS5填充移除</li>
	 * </ul>
	 *
	 * @param inputStream  加密输入流
	 * @param outputStream 解密输出流
	 * @param password     解密密码
	 * @throws IllegalArgumentException 当密码长度不符合要求时抛出
	 * @since 1.0.0
	 */
	public static void decrypt(InputStream inputStream, OutputStream outputStream, byte[] password) throws IOException {
		Validate.isTrue(ArrayUtils.getLength(password) == 16, "password 必须为16字节");
		decrypt(inputStream, outputStream, password, new IvParameterSpec(password),
			IOConstants.AES_CBC_PKCS5_PADDING);
	}

	/**
	 * AES/CBC模式流加密（使用指定IV）
	 * <p>使用指定的初始化向量(IV)进行加密操作</p>
	 *
	 * @param inputStream  输入数据流（必须可读）
	 * @param outputStream 输出加密流（必须可写）
	 * @param password     加密密钥（16/24/32字节）
	 * @param iv           16字节初始化向量
	 * @throws IOException 当发生以下情况时抛出：
	 *                     - 密钥或IV长度不符合规范
	 *                     - 流操作异常
	 *                     - 加密配置错误
	 * @since 1.0.0
	 */
	public static void encrypt(InputStream inputStream, OutputStream outputStream, byte[] password, byte[] iv) throws IOException {
		Validate.isTrue(ArrayUtils.getLength(iv) == 16, "iv必须为16字节");
		encrypt(inputStream, outputStream, password, new IvParameterSpec(iv),
			IOConstants.AES_CBC_PKCS5_PADDING);
	}

	/**
	 * AES/CBC模式流解密（使用指定IV）
	 * <p>需确保使用与加密相同的密码和IV</p>
	 *
	 * @param inputStream  加密数据流
	 * @param outputStream 解密输出流
	 * @param password     解密密钥
	 * @param iv           16字节初始化向量
	 * @throws IOException 当发生以下情况时抛出：
	 *                     - 密钥或IV长度不符合规范
	 *                     - 流操作异常
	 *                     - 解密验证失败
	 * @since 1.0.0
	 */
	public static void decrypt(InputStream inputStream, OutputStream outputStream, byte[] password, byte[] iv) throws IOException {
		Validate.isTrue(ArrayUtils.getLength(iv) == 16, "iv必须为16字节");
		decrypt(inputStream, outputStream, password, new IvParameterSpec(iv),
			IOConstants.AES_CBC_PKCS5_PADDING);
	}

	/**
	 * AES通用加密接口（自定义算法参数）
	 * <p>允许指定加密算法转换表达式和参数</p>
	 *
	 * @param inputStream            输入流
	 * @param outputStream           输出流
	 * @param password               密钥材料（16/24/32字节）
	 * @param algorithmParameterSpec 算法参数规范
	 * @param transformation         加密算法转换表达式（如AES/CBC/PKCS5Padding）
	 * @throws IOException 当发生以下情况时抛出：
	 *                     - 参数校验失败
	 *                     - 流操作异常
	 *                     - 算法不可用
	 * @since 1.0.0
	 */
	public static void encrypt(InputStream inputStream, OutputStream outputStream, byte[] password,
							   AlgorithmParameterSpec algorithmParameterSpec, String transformation) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");

		Validate.isTrue(IOConstants.AES_KEY_LENGTHS.contains(ArrayUtils.getLength(password)),
			"password长度必须为16,24,32");

		Validate.notNull(algorithmParameterSpec, "algorithmParameterSpec 不可为 null");
		Validate.notBlank(transformation, "transformation 不可为空");

		SecretKeySpec secretKey = new SecretKeySpec(password, IOConstants.AES_ALGORITHM);
		try (CryptoOutputStream cryptoOutputStream = new CryptoOutputStream(transformation, DEFAULT_PROPERTIES,
			outputStream, secretKey, algorithmParameterSpec)) {
			inputStream.transferTo(cryptoOutputStream);
		}
	}

	/**
	 * AES通用解密接口（自定义算法参数）
	 * <p>需确保参数与加密时完全一致</p>
	 *
	 * @param inputStream            输入加密流
	 * @param outputStream           解密输出流
	 * @param password               密钥材料
	 * @param algorithmParameterSpec 算法参数规范
	 * @param transformation         解密算法转换表达式
	 * @throws IOException 当发生以下情况时抛出：
	 *                     - 参数校验失败
	 *                     - 流操作异常
	 *                     - 解密验证失败
	 * @since 1.0.0
	 */
	public static void decrypt(InputStream inputStream, OutputStream outputStream, byte[] password,
							   AlgorithmParameterSpec algorithmParameterSpec, String transformation) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");

		Validate.isTrue(IOConstants.AES_KEY_LENGTHS.contains(ArrayUtils.getLength(password)),
			"password长度必须为16,24,32");

		Validate.notNull(algorithmParameterSpec, "algorithmParameterSpec 不可为 null");
		Validate.notBlank(transformation, "transformation 不可为空");

		SecretKeySpec secretKey = new SecretKeySpec(password, IOConstants.AES_ALGORITHM);
		try (CryptoInputStream cryptoInputStream = new CryptoInputStream(transformation, DEFAULT_PROPERTIES,
			inputStream, secretKey, algorithmParameterSpec)) {
			cryptoInputStream.transferTo(outputStream);
		}
	}

	/**
	 * CTR模式加密（默认IV）
	 * <p>使用密码字节作为默认IV的快速加密方法</p>
	 *
	 * @param inputStream  输入流
	 * @param outputStream 输出加密流
	 * @param password     16字节密钥
	 * @throws IOException 当发生以下情况时抛出：
	 *                     - 密钥长度不符合规范
	 *                     - 流操作异常
	 * @since 1.0.0
	 */
	public static void encryptByCtr(InputStream inputStream, OutputStream outputStream, byte[] password) throws IOException {
		Validate.isTrue(ArrayUtils.getLength(password) == 16, "password 必须为16字节");
		encryptByCtr(inputStream, outputStream, password, password);
	}

	/**
	 * CTR模式解密（默认IV）
	 * <p>需使用与加密相同的密码和IV</p>
	 *
	 * @param inputStream  加密数据流
	 * @param outputStream 解密输出流
	 * @param password     解密密钥
	 * @throws IOException 当发生以下情况时抛出：
	 *                     - 密钥长度不符合规范
	 *                     - 流操作异常
	 * @since 1.0.0
	 */
	public static void decryptByCtr(InputStream inputStream, OutputStream outputStream, byte[] password) throws IOException {
		Validate.isTrue(ArrayUtils.getLength(password) == 16, "password 必须为16字节");
		decryptByCtr(inputStream, outputStream, password, password);
	}

	/**
	 * CTR模式流加密（自定义IV）
	 * <p>CTR模式无填充，支持任意长度数据</p>
	 *
	 * @param inputStream  输入数据流
	 * @param outputStream 输出加密流
	 * @param password     16/24/32字节密钥
	 * @param iv           16字节初始化向量
	 * @throws IOException 当发生以下情况时抛出：
	 *                     - 参数校验失败
	 *                     - 流操作异常
	 * @since 1.0.0
	 */
	public static void encryptByCtr(InputStream inputStream, OutputStream outputStream, byte[] password, byte[] iv) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");

		Validate.isTrue(IOConstants.AES_KEY_LENGTHS.contains(ArrayUtils.getLength(password)),
			"password长度必须为16,24,32");
		Validate.isTrue(ArrayUtils.getLength(iv) == 16, "iv必须为16字节");

		try (CtrCryptoOutputStream cryptoInputStream = new CtrCryptoOutputStream(DEFAULT_PROPERTIES, outputStream,
			password, iv)) {
			inputStream.transferTo(cryptoInputStream);
		}
	}

	/**
	 * CTR模式流解密（自定义IV）
	 * <p>需确保IV与加密时完全一致</p>
	 *
	 * @param inputStream  加密数据流
	 * @param outputStream 解密输出流
	 * @param password     解密密钥
	 * @param iv           16字节初始化向量
	 * @throws IOException 当发生以下情况时抛出：
	 *                     - 参数校验失败
	 *                     - 流操作异常
	 * @since 1.0.0
	 */
	public static void decryptByCtr(InputStream inputStream, OutputStream outputStream, byte[] password, byte[] iv) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");

		Validate.isTrue(IOConstants.AES_KEY_LENGTHS.contains(ArrayUtils.getLength(password)),
			"password长度必须为16,24,32");
		Validate.isTrue(ArrayUtils.getLength(iv) == 16, "iv必须为16字节");

		try (CtrCryptoInputStream cryptoInputStream = new CtrCryptoInputStream(DEFAULT_PROPERTIES, inputStream,
			password, iv)) {
			cryptoInputStream.transferTo(outputStream);
		}
	}
}
