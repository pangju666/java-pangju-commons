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

import org.apache.commons.crypto.stream.CryptoInputStream;
import org.apache.commons.crypto.stream.CryptoOutputStream;
import org.apache.commons.crypto.stream.CtrCryptoInputStream;
import org.apache.commons.crypto.stream.CtrCryptoOutputStream;
import org.apache.commons.crypto.utils.AES;
import org.apache.commons.io.input.UnsynchronizedBufferedInputStream;
import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

/**
 * 增强型IO流操作工具类（继承自 {@link org.apache.commons.io.IOUtils}）
 * <p>提供基于Apache Commons Crypto的AES加解密能力扩展，主要特性：</p>
 *
 * <h3>核心功能模块：</h3>
 * <ul>
 *     <li><strong>AES加解密体系</strong> - 支持CBC/CTR两种加密模式</li>
 *     <li><strong>密码规范管理</strong> - 强制校验密钥长度（128/192/256位）</li>
 *     <li><strong>流式处理优化</strong> - 内存友好的大文件处理能力</li>
 *     <li><strong>非同步流支持</strong> - 提供非线程安全的缓冲流实现</li>
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
	protected static final Properties DEFAULT_PROPERTIES = new Properties();
	/**
	 * 合法的AES密钥长度集合（单位：字节）
	 *
	 * <p>包含16(128位)、24(192位)、32(256位)三种标准长度</p>
	 *
	 * @since 1.0.0
	 */
	protected static final Set<Integer> AES_KEY_LENGTHS = Set.of(16, 24, 32);

	protected static final int KB_4 = 4 * 1024;
	protected static final int KB_8 = 8 * 1024;
	protected static final int KB_32 = 32 * 1024;
	protected static final int KB_64 = 64 * 1024;
	protected static final int KB_128 = 128 * 1024;
	protected static final int KB_256 = 256 * 1024;
	protected static final int MB_1 = 1024 * 1024;
	protected static final int MB_10 = 10 * MB_1;
	protected static final int MB_100 = 100 * MB_1;
	protected static final int GB_1 = 1024 * MB_1;

	protected IOUtils() {
	}

	/**
	 * 根据总大小获取推荐的缓冲区大小
	 * <p>缓冲区大小策略如下：</p>
	 * <table border="1">
	 *     <tr><th>文件大小范围</th><th>缓冲区大小</th></tr>
	 *      <tr><td>&lt;256KB</td><td>4KB</td></tr>
	 *     <tr><td>256KB~1MB</td><td>8KB</td></tr>
	 *     <tr><td>1MB~10MB</td><td>32KB</td></tr>
	 *     <tr><td>10MB~100MB</td><td>64KB</td></tr>
	 *     <tr><td>100MB~1GB</td><td>128KB</td></tr>
	 *     <tr><td>&ge;1GB</td><td>256KB</td></tr>
	 * </table>
	 *
	 * @param totalSize 文件总大小（单位：字节）
	 * @return 推荐的缓冲区大小（单位：字节）
	 * @throws IllegalArgumentException 如果totalSize小于0时抛出
	 * @since 1.0.0
	 */
	public static int getBufferSize(final long totalSize) {
		Validate.isTrue(totalSize >= 0, "totalSize 必须大于等于0");

		if (totalSize < KB_256) { // < 256KB
			return KB_4;
		} else if (totalSize < MB_1) { // 256KB-1MB
			return KB_8;
		} else if (totalSize < MB_10) { // 1MB~10MB
			return KB_32;
		} else if (totalSize < MB_100) { // 10MB~100MB
			return KB_64;
		} else if (totalSize < GB_1) { // 100MB~1GB
			return KB_128;
		} else {
			return KB_256;
		}
	}

	/**
	 * 创建非同步缓冲输入流（使用默认缓冲区大小）
	 * <p>默认缓冲区大小为 {@link IOUtils#DEFAULT_BUFFER_SIZE}</p>
	 *
	 * @param inputStream 原始输入流（必须非null）
	 * @return 包装后的缓冲输入流
	 * @throws IOException 当流初始化失败时抛出
	 * @throws NullPointerException 当inputStream为null时抛出
	 * @see #unsynchronizedBuffer(InputStream, int)
	 * @since 1.0.0
	 */
	public static UnsynchronizedBufferedInputStream unsynchronizedBuffer(final InputStream inputStream) throws IOException {
		return unsynchronizedBuffer(inputStream, IOUtils.DEFAULT_BUFFER_SIZE);
	}

	/**
	 * 创建非同步缓冲输入流（自定义缓冲区大小）
	 * <p>特性说明：</p>
	 * <ul>
	 *     <li>非线程安全实现，适合单线程使用</li>
	 *     <li>不进行同步操作，性能优于同步缓冲流</li>
	 *     <li>如果输入流已经是非同步缓冲流则直接返回</li>
	 *     <li>缓冲区大小应根据数据量合理设置</li>
	 * </ul>
	 *
	 * @param inputStream 原始输入流（必须非null）
	 * @param bufferSize 缓冲区大小（单位：字节，必须大于0）
	 * @return 包装后的缓冲输入流
	 * @throws IOException 当流初始化失败时抛出
	 * @throws NullPointerException 当inputStream为null时抛出
	 * @throws IllegalArgumentException 当bufferSize小于等于0时抛出
	 * @see #getBufferSize(long)
	 * @since 1.0.0
	 */
	public static UnsynchronizedBufferedInputStream unsynchronizedBuffer(final InputStream inputStream,
																		 final int bufferSize) throws IOException {
		Objects.requireNonNull(inputStream, "inputStream");
		Validate.isTrue(bufferSize > 0, "bufferSize 必须大于0");

		if (inputStream instanceof UnsynchronizedBufferedInputStream) {
			return (UnsynchronizedBufferedInputStream) inputStream;
		}
		return new UnsynchronizedBufferedInputStream.Builder()
			.setBufferSize(bufferSize)
			.setInputStream(inputStream)
			.get();
	}

	/**
	 * 创建非同步字节数组输入流
	 * <p>特性说明：</p>
	 * <ul>
	 *     <li>线程不安全，适合单线程使用</li>
	 *     <li>支持空数组输入（自动转换为空流）</li>
	 *     <li>可重复读取数据</li>
	 * </ul>
	 *
	 * @param bytes 原始字节数组（允许为null，将视为空数组）
	 * @return 基于字节数组的输入流
	 * @throws IOException 当流初始化失败时抛出
	 * @since 1.0.0
	 */
	public static UnsynchronizedByteArrayInputStream toUnsynchronizedByteArrayInputStream(final byte[] bytes) throws IOException {
		return UnsynchronizedByteArrayInputStream.builder()
			.setByteArray(ArrayUtils.nullToEmpty(bytes))
			.setOffset(0)
			.setLength(ArrayUtils.getLength(bytes))
			.get();
	}

	/**
	 * 创建非同步字节数组输出流（指定初始缓冲区大小）
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>缓冲区大小必须大于0</li>
	 *     <li>初始容量会影响性能，建议根据预期数据量设置</li>
	 *     <li>非线程安全实现</li>
	 * </ul>
	 *
	 * @param bufferSize 初始缓冲区大小（单位：字节）
	 * @return 新的字节数组输出流
	 * @throws IllegalArgumentException 当bufferSize小于等于0时抛出
	 * @see #getBufferSize(long)
	 * @since 1.0.0
	 */
	public static UnsynchronizedByteArrayOutputStream toUnsynchronizedByteArrayOutputStream(final int bufferSize) {
		Validate.isTrue(bufferSize > 0, "bufferSize 必须大于0");

		return UnsynchronizedByteArrayOutputStream.builder()
			.setBufferSize(bufferSize)
			.get();
	}

	/**
	 * 创建非同步字节数组输出流并写入输入流数据（使用默认缓冲区大小）
	 * <p>特性说明：</p>
	 * <ul>
	 *     <li>使用默认缓冲区大小 {@link IOUtils#DEFAULT_BUFFER_SIZE}</li>
	 *     <li>自动读取输入流数据并写入输出流</li>
	 *     <li>非线程安全实现，适合单线程使用</li>
	 * </ul>
	 *
	 * @param inputStream 输入流（必须非null）
	 * @return 包含输入流数据的字节数组输出流
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>流读取/写入失败</li>
	 *                         <li>内存不足</li>
	 *                     </ul>
	 * @throws NullPointerException 当inputStream为null时抛出
	 * @see #toUnsynchronizedByteArrayOutputStream(InputStream, int)
	 * @since 1.0.0
	 */
	public static UnsynchronizedByteArrayOutputStream toUnsynchronizedByteArrayOutputStream(final InputStream inputStream) throws IOException {
		Objects.requireNonNull(inputStream, "inputStream");

		UnsynchronizedByteArrayOutputStream outputStream = toUnsynchronizedByteArrayOutputStream(IOUtils.DEFAULT_BUFFER_SIZE);
		outputStream.write(inputStream);
		return outputStream;
	}

	/**
	 * 创建非同步字节数组输出流并写入输入流数据（自定义缓冲区大小）
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>缓冲区大小应根据预期数据量合理设置</li>
	 *     <li>大文件处理建议使用 {@link #getBufferSize(long)} 计算合适大小</li>
	 *     <li>非线程安全实现</li>
	 * </ul>
	 *
	 * @param inputStream 输入流（必须非null）
	 * @param bufferSize 初始缓冲区大小（单位：字节，必须大于0）
	 * @return 包含输入流数据的字节数组输出流
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>流读取/写入失败</li>
	 *                         <li>内存不足</li>
	 *                     </ul>
	 * @throws NullPointerException 当inputStream为null时抛出
	 * @throws IllegalArgumentException 当bufferSize小于等于0时抛出
	 * @see #getBufferSize(long)
	 * @since 1.0.0
	 */
	public static UnsynchronizedByteArrayOutputStream toUnsynchronizedByteArrayOutputStream(final InputStream inputStream,
																							final int bufferSize) throws IOException {
		Objects.requireNonNull(inputStream, "inputStream");

		UnsynchronizedByteArrayOutputStream outputStream = toUnsynchronizedByteArrayOutputStream(bufferSize);
		outputStream.write(inputStream);
		return outputStream;
	}

	/**
	 * AES/CBC/PKCS5Padding模式流加密（默认IV）
	 * <p>实现特性：</p>
	 * <ul>
	 *     <li>使用密码字节作为初始化向量（IV）</li>
	 *     <li>自动处理PKCS5填充</li>
	 *     <li>支持最大2GB流加密</li>
	 * </ul>
	 *
	 * @param inputStream  输入流（必须可读且未关闭）
	 * @param outputStream 输出流（必须可写且未关闭）
	 * @param key     加密密码（长度必须为16字节）
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>密码长度不符合规范</li>
	 *                         <li>流读写异常</li>
	 *                         <li>加密配置错误</li>
	 *                     </ul>
	 * @see #encrypt(InputStream, OutputStream, Key, AlgorithmParameterSpec, String)
	 * @since 1.0.0
	 */
	public static void encrypt(final InputStream inputStream, final OutputStream outputStream, final byte[] key) throws IOException {
		Validate.isTrue(ArrayUtils.getLength(key) == 16, "key 必须为16字节");

		SecretKeySpec secretKey = new SecretKeySpec(key, AES.ALGORITHM);
		encrypt(inputStream, outputStream, secretKey, new IvParameterSpec(key),
			AES.CBC_PKCS5_PADDING);
	}

	/**
	 * AES/CBC/PKCS5Padding模式流解密（默认IV）
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>必须使用与加密相同的密码和IV</li>
	 *     <li>输入流必须为加密后的原始数据</li>
	 *     <li>自动处理PKCS5填充移除</li>
	 * </ul>
	 *
	 * @param inputStream  加密输入流
	 * @param outputStream 解密输出流
	 * @param key     解密密码（长度必须为16字节）
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>密码长度不符合规范</li>
	 *                         <li>流读写异常</li>
	 *                         <li>解密配置错误</li>
	 *                     </ul>
	 * @see #decrypt(InputStream, OutputStream, Key, AlgorithmParameterSpec, String)
	 * @since 1.0.0
	 */
	public static void decrypt(final InputStream inputStream, final OutputStream outputStream, final byte[] key) throws IOException {
		Validate.isTrue(ArrayUtils.getLength(key) == 16, "key 必须为16字节");

		SecretKeySpec secretKey = new SecretKeySpec(key, AES.ALGORITHM);
		decrypt(inputStream, outputStream, secretKey, new IvParameterSpec(key),
			AES.CBC_PKCS5_PADDING);
	}

	/**
	 * AES/CBC/PKCS5Padding模式流加密（自定义IV）
	 * <p>实现特性：</p>
	 * <ul>
	 *     <li>支持自定义16字节初始化向量</li>
	 *     <li>自动处理PKCS5填充</li>
	 *     <li>支持16/24/32字节密钥</li>
	 * </ul>
	 *
	 * @param inputStream  输入流（必须可读且未关闭）
	 * @param outputStream 输出流（必须可写且未关闭）
	 * @param key     加密密码（长度必须为16/24/32字节）
	 * @param iv           16字节初始化向量
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>密码或IV长度不符合规范</li>
	 *                         <li>流读写异常</li>
	 *                         <li>加密配置错误</li>
	 *                     </ul>
	 * @see #encrypt(InputStream, OutputStream, Key, AlgorithmParameterSpec, String)
	 * @since 1.0.0
	 */
	public static void encrypt(final InputStream inputStream, final OutputStream outputStream, final byte[] key,
							   final byte[] iv) throws IOException {
		Validate.isTrue(AES_KEY_LENGTHS.contains(ArrayUtils.getLength(key)), "key长度必须为16,24,32");
		Validate.isTrue(ArrayUtils.getLength(iv) == 16, "iv必须为16字节");

		SecretKeySpec secretKey = new SecretKeySpec(key, AES.ALGORITHM);
		encrypt(inputStream, outputStream, secretKey, new IvParameterSpec(iv),
			AES.CBC_PKCS5_PADDING);
	}

	/**
	 * AES/CBC/PKCS5Padding模式流解密（自定义IV）
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>必须使用与加密相同的密码和IV</li>
	 *     <li>IV必须为16字节</li>
	 *     <li>输入流必须为加密后的原始数据</li>
	 * </ul>
	 *
	 * @param inputStream  加密输入流
	 * @param outputStream 解密输出流
	 * @param key     解密密码（长度必须为16/24/32字节）
	 * @param iv           16字节初始化向量
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>密码或IV长度不符合规范</li>
	 *                         <li>流读写异常</li>
	 *                         <li>解密配置错误</li>
	 *                     </ul>
	 * @see #decrypt(InputStream, OutputStream, Key, AlgorithmParameterSpec, String)
	 * @since 1.0.0
	 */
	public static void decrypt(final InputStream inputStream, final OutputStream outputStream, final byte[] key,
							   final byte[] iv) throws IOException {
		Validate.isTrue(AES_KEY_LENGTHS.contains(ArrayUtils.getLength(key)), "key长度必须为16,24,32");
		Validate.isTrue(ArrayUtils.getLength(iv) == 16, "iv必须为16字节");

		SecretKeySpec secretKey = new SecretKeySpec(key, AES.ALGORITHM);
		decrypt(inputStream, outputStream, secretKey, new IvParameterSpec(iv),
			AES.CBC_PKCS5_PADDING);
	}

	/**
	 * 通用加密方法（使用Key对象）
	 * <p>核心特性：</p>
	 * <ul>
	 *     <li>支持任意类型的加密密钥（Key对象）</li>
	 *     <li>可自定义算法参数和转换模式</li>
	 *     <li>自动资源管理（使用try-with-resources）</li>
	 * </ul>
	 *
	 * @param inputStream 原始输入流（必须非null且未关闭）
	 * @param outputStream 加密输出流（必须非null且未关闭）
	 * @param key 加密密钥对象（必须非null）
	 * @param algorithmParameterSpec 算法参数规范（如{@link IvParameterSpec}）
	 * @param transformation 加密算法转换名称（如"AES/CBC/PKCS5Padding"）
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>参数校验失败</li>
	 *                         <li>流操作异常</li>
	 *                         <li>加密配置错误</li>
	 *                     </ul>
	 * @since 1.0.0
	 */
	public static void encrypt(final InputStream inputStream, final OutputStream outputStream, final Key key,
							   final AlgorithmParameterSpec algorithmParameterSpec, final String transformation) throws IOException {
		validateArgs(inputStream, outputStream, key, algorithmParameterSpec, transformation);

		try (CryptoOutputStream cryptoOutputStream = new CryptoOutputStream(transformation, DEFAULT_PROPERTIES,
			outputStream, key, algorithmParameterSpec)) {
			inputStream.transferTo(cryptoOutputStream);
		}
	}

	/**
	 * 通用解密方法（使用Key对象）
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>必须使用与加密相同的密钥对象</li>
	 *     <li>算法参数和转换模式需与加密时一致</li>
	 *     <li>输入流必须为加密后的原始数据</li>
	 * </ul>
	 *
	 * @param inputStream 加密输入流（必须非null且未关闭）
	 * @param outputStream 解密输出流（必须非null且未关闭）
	 * @param key 解密密钥对象（必须非null）
	 * @param algorithmParameterSpec 算法参数规范（需与加密时一致）
	 * @param transformation 解密算法转换名称（需与加密时一致）
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>参数校验失败</li>
	 *                         <li>流操作异常</li>
	 *                         <li>解密配置错误</li>
	 *                     </ul>
	 * @since 1.0.0
	 */
	public static void decrypt(final InputStream inputStream, final OutputStream outputStream, final Key key,
							   final AlgorithmParameterSpec algorithmParameterSpec, final String transformation) throws IOException {
		validateArgs(inputStream, outputStream, key, algorithmParameterSpec, transformation);

		try (CryptoInputStream cryptoInputStream = new CryptoInputStream(transformation, DEFAULT_PROPERTIES,
			inputStream, key, algorithmParameterSpec)) {
			cryptoInputStream.transferTo(outputStream);
		}
	}

	/**
	 * 使用AES/CTR模式加密（默认IV）
	 * <p>实现特性：</p>
	 * <ul>
	 *     <li>使用密码字节作为初始化向量（IV）</li>
	 *     <li>CTR模式无填充，支持任意长度数据</li>
	 *     <li>高性能流式加密处理</li>
	 * </ul>
	 *
	 * @param inputStream  原始输入流（必须非null且未关闭）
	 * @param outputStream 加密输出流（必须非null且未关闭）
	 * @param key     16字节加密密钥
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>密钥长度不符合16字节规范</li>
	 *                         <li>流读写异常</li>
	 *                         <li>加密配置错误</li>
	 *                     </ul>
	 * @see #encryptByCtr(InputStream, OutputStream, byte[], byte[])
	 * @since 1.0.0
	 */
	public static void encryptByCtr(final InputStream inputStream, final OutputStream outputStream, final byte[] key) throws IOException {
		Validate.isTrue(ArrayUtils.getLength(key) == 16, "key 必须为16字节");

		encryptByCtr(inputStream, outputStream, key, key);
	}

	/**
	 * 使用AES/CTR模式解密（默认IV）
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>必须使用与加密相同的16字节密钥</li>
	 *     <li>IV会自动使用密钥字节</li>
	 *     <li>输入流必须为CTR模式加密的原始数据</li>
	 * </ul>
	 *
	 * @param inputStream  CTR加密输入流（必须非null且未关闭）
	 * @param outputStream 解密输出流（必须非null且未关闭）
	 * @param key     16字节解密密钥（需与加密时一致）
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>密钥长度不符合16字节规范</li>
	 *                         <li>流读写异常</li>
	 *                         <li>解密配置错误</li>
	 *                     </ul>
	 * @see #decryptByCtr(InputStream, OutputStream, byte[], byte[])
	 * @since 1.0.0
	 */
	public static void decryptByCtr(final InputStream inputStream, final OutputStream outputStream, final byte[] key) throws IOException {
		Validate.isTrue(ArrayUtils.getLength(key) == 16, "key 必须为16字节");

		decryptByCtr(inputStream, outputStream, key, key);
	}

	/**
	 * CTR模式流加密（自定义IV）
	 * <p>实现特性：</p>
	 * <ul>
	 *     <li>支持16/24/32字节密钥（128/192/256位）</li>
	 *     <li>必须提供16字节初始化向量（IV）</li>
	 *     <li>CTR模式无填充，支持任意长度数据</li>
	 *     <li>高性能流式加密处理</li>
	 * </ul>
	 *
	 * @param inputStream  原始输入流（必须非null且未关闭）
	 * @param outputStream 加密输出流（必须非null且未关闭）
	 * @param key     加密密钥（16/24/32字节）
	 * @param iv           16字节初始化向量（必须非null）
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>密钥或IV长度不符合规范</li>
	 *                         <li>流读写异常</li>
	 *                         <li>加密配置错误</li>
	 *                     </ul>
	 * @see #encryptByCtr(InputStream, OutputStream, byte[])
	 * @since 1.0.0
	 */
	public static void encryptByCtr(final InputStream inputStream, final OutputStream outputStream,
									final byte[] key, final byte[] iv) throws IOException {
		validateArgs(inputStream, outputStream, key, iv);

		try (CtrCryptoOutputStream cryptoInputStream = new CtrCryptoOutputStream(DEFAULT_PROPERTIES, outputStream,
			key, iv)) {
			inputStream.transferTo(cryptoInputStream);
		}
	}

	/**
	 * CTR模式流解密（自定义IV）
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>必须使用与加密相同的密钥和IV</li>
	 *     <li>IV必须为16字节</li>
	 *     <li>输入流必须为CTR模式加密的原始数据</li>
	 * </ul>
	 *
	 * @param inputStream  CTR加密输入流（必须非null且未关闭）
	 * @param outputStream 解密输出流（必须非null且未关闭）
	 * @param key     解密密钥（16/24/32字节，需与加密时一致）
	 * @param iv           16字节初始化向量（需与加密时一致）
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>密钥或IV长度不符合规范</li>
	 *                         <li>流读写异常</li>
	 *                         <li>解密配置错误</li>
	 *                     </ul>
	 * @see #decryptByCtr(InputStream, OutputStream, byte[])
	 * @since 1.0.0
	 */
	public static void decryptByCtr(final InputStream inputStream, final OutputStream outputStream,
									final byte[] key, final byte[] iv) throws IOException {
		validateArgs(inputStream, outputStream, key, iv);

		try (CtrCryptoInputStream cryptoInputStream = new CtrCryptoInputStream(DEFAULT_PROPERTIES, inputStream,
			key, iv)) {
			cryptoInputStream.transferTo(outputStream);
		}
	}

	/**
	 * 校验AES加解密参数
	 * <p>校验规则：</p>
	 * <ul>
	 *     <li>输入/输出流必须非null</li>
	 *     <li>密码长度必须为16/24/32字节</li>
	 *     <li>初始化向量必须为16字节</li>
	 * </ul>
	 *
	 * @param inputStream  输入流（必须非null）
	 * @param outputStream 输出流（必须非null）
	 * @param key     加密密码字节数组（必须16/24/32字节）
	 * @param iv           初始化向量字节数组（必须16字节）
	 * @throws NullPointerException     当inputStream/outputStream为null时抛出
	 * @throws IllegalArgumentException 当key或iv长度不符合要求时抛出
	 * @since 1.0.0
	 */
	protected static void validateArgs(final InputStream inputStream, final OutputStream outputStream,
									   final byte[] key, final byte[] iv) {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");

		Validate.isTrue(AES_KEY_LENGTHS.contains(ArrayUtils.getLength(key)),
			"key长度必须为16,24,32");
		Validate.isTrue(ArrayUtils.getLength(iv) == 16, "iv必须为16字节");
	}

	/**
	 * 校验通用加解密参数（Key对象形式）
	 * <p>校验规则：</p>
	 * <ul>
	 *     <li>输入/输出流必须非null</li>
	 *     <li>密钥对象必须非null</li>
	 *     <li>算法参数规范必须非null</li>
	 *     <li>转换名称必须非空</li>
	 * </ul>
	 *
	 * @param inputStream            输入流（必须非null）
	 * @param outputStream           输出流（必须非null）
	 * @param key                    加密密钥对象（必须非null）
	 * @param algorithmParameterSpec 算法参数规范（必须非null）
	 * @param transformation         算法转换名称（必须非空）
	 * @throws NullPointerException     当任何非null参数为null时抛出
	 * @throws IllegalArgumentException 当transformation为空时抛出
	 * @since 1.0.0
	 */
	protected static void validateArgs(final InputStream inputStream, final OutputStream outputStream, final Key key,
									   final AlgorithmParameterSpec algorithmParameterSpec, final String transformation) {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");

		Validate.notNull(key, "key 不可为 null");
		Validate.notNull(algorithmParameterSpec, "algorithmParameterSpec 不可为 null");
		Validate.notBlank(transformation, "transformation 不可为空");
	}
}
