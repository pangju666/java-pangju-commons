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
import net.openhft.hashing.LongHashFunction;
import org.apache.commons.crypto.stream.CryptoInputStream;
import org.apache.commons.crypto.stream.CryptoOutputStream;
import org.apache.commons.crypto.stream.CtrCryptoInputStream;
import org.apache.commons.crypto.stream.CtrCryptoOutputStream;
import org.apache.commons.crypto.utils.AES;
import org.apache.commons.io.input.UnsynchronizedBufferedInputStream;
import org.apache.commons.io.input.UnsynchronizedBufferedReader;
import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;

/**
 * 增强型IO流操作工具类（继承自 {@link org.apache.commons.io.IOUtils}）
 * <p>提供基于Apache Commons Crypto的AES加解密能力扩展及流处理优化，主要特性：</p>
 *
 * <h3>核心功能模块：</h3>
 * <ul>
 *     <li><strong>AES加解密体系</strong> - 支持CBC/CTR两种加密模式，支持自定义缓冲区大小</li>
 *     <li><strong>密码规范管理</strong> - 强制校验密钥长度（128/192/256位）和IV长度（16字节）</li>
 *     <li><strong>流式处理优化</strong> - 内存友好的大文件处理能力</li>
 *     <li><strong>非同步流支持</strong> - 提供非线程安全的缓冲流实现，性能优于同步版本</li>
 *     <li><strong>流摘要计算</strong> - 基于三段采样策略的高效摘要计算，适合大文件</li>
 *     <li><strong>缓冲区大小计算</strong> - 根据文件大小自动推荐合适的缓冲区大小</li>
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

	/**
	 * 4KB常量
	 *
	 * @since 1.0.0
	 */
	protected static final int KB_4 = 4 * 1024;
	/**
	 * 8KB常量
	 *
	 * @since 1.0.0
	 */
	protected static final int KB_8 = 8 * 1024;
	/**
	 * 32KB常量
	 *
	 * @since 1.0.0
	 */
	protected static final int KB_32 = 32 * 1024;
	/**
	 * 64KB常量
	 *
	 * @since 1.0.0
	 */
	protected static final int KB_64 = 64 * 1024;
	/**
	 * 128KB常量
	 *
	 * @since 1.0.0
	 */
	protected static final int KB_128 = 128 * 1024;
	/**
	 * 256KB常量
	 *
	 * @since 1.0.0
	 */
	protected static final int KB_256 = 256 * 1024;
	/**
	 * 1MB常量
	 *
	 * @since 1.0.0
	 */
	protected static final int MB_1 = 1024 * 1024;
	/**
	 * 10MB常量
	 *
	 * @since 1.0.0
	 */
	protected static final int MB_10 = 10 * MB_1;
	/**
	 * 100MB常量
	 *
	 * @since 1.0.0
	 */
	protected static final int MB_100 = 100 * MB_1;
	/**
	 * 1GB常量
	 *
	 * @since 1.0.0
	 */
	protected static final int GB_1 = 1024 * MB_1;

	static {
		DEFAULT_PROPERTIES.put(CryptoInputStream.STREAM_BUFFER_SIZE_KEY, IOUtils.DEFAULT_BUFFER_SIZE);
	}

	protected IOUtils() {
	}

	/**
	 * 计算输入流的摘要（使用默认采样大小和哈希函数）
	 * <p>实现特性：</p>
	 * <ul>
	 *     <li>使用默认采样大小 {@link IOConstants#DEFAULT_SAMPLE_SIZE}</li>
	 *     <li>使用默认哈希函数 {@link IOConstants#DEFAULT_HASH_FUNC}</li>
	 *     <li>采用三段采样策略：头部、中部、尾部</li>
	 *     <li>支持已知和未知流长度</li>
	 * </ul>
	 *
	 * <p>采样策略说明：</p>
	 * <ul>
	 *     <li>流长度小于等于3倍采样大小时：读取全部数据</li>
	 *     <li>流长度大于3倍采样大小时：读取头部、中部、尾部各采样大小的数据</li>
	 *     <li>未知流长度时：一次性读取全部数据（适合小流）</li>
	 * </ul>
	 *
	 * @param inputStream  输入流（必须非null）
	 * @param streamLength 流长度（字节），-1表示未知长度
	 * @return 格式化的摘要字符串
	 * @throws IOException              当流读取失败时抛出
	 * @throws NullPointerException     当inputStream为null时抛出
	 * @throws IllegalArgumentException 当streamLength小于-1时抛出
	 * @see #computeDigest(InputStream, long, int)
	 * @see #computeDigest(InputStream, long, int, LongHashFunction)
	 * @since 1.1.0
	 */
	public static String computeDigest(final InputStream inputStream, final long streamLength) throws IOException {
		return computeDigest(inputStream, streamLength, IOConstants.DEFAULT_SAMPLE_SIZE,
			IOConstants.DEFAULT_HASH_FUNC);
	}

	/**
	 * 计算输入流的摘要（使用自定义采样大小和默认哈希函数）
	 * <p>实现特性：</p>
	 * <ul>
	 *     <li>使用自定义采样大小</li>
	 *     <li>使用默认哈希函数 {@link IOConstants#DEFAULT_HASH_FUNC}</li>
	 *     <li>采用三段采样策略：头部、中部、尾部</li>
	 *     <li>支持已知和未知流长度</li>
	 * </ul>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>采样大小应根据数据特征合理设置</li>
	 *     <li>采样大小越大，摘要计算越准确但性能越低</li>
	 *     <li>未知流长度时会一次性读取全部数据，大流建议传入准确长度</li>
	 * </ul>
	 *
	 * @param inputStream  输入流（必须非null）
	 * @param streamLength 流长度（字节），-1表示未知长度
	 * @param sampleSize   采样大小（字节，必须大于0）
	 * @return 格式化的摘要字符串
	 * @throws IOException              当流读取失败时抛出
	 * @throws NullPointerException     当inputStream为null时抛出
	 * @throws IllegalArgumentException 当streamLength小于-1或sampleSize小于等于0时抛出
	 * @see #computeDigest(InputStream, long)
	 * @see #computeDigest(InputStream, long, int, LongHashFunction)
	 * @since 1.1.0
	 */
	public static String computeDigest(final InputStream inputStream, final long streamLength, final int sampleSize) throws IOException {
		return computeDigest(inputStream, streamLength, sampleSize, IOConstants.DEFAULT_HASH_FUNC);
	}

	/**
	 * 计算输入流的摘要（使用自定义采样大小和哈希函数）
	 * <p>实现特性：</p>
	 * <ul>
	 *     <li>使用自定义采样大小和哈希函数</li>
	 *     <li>采用三段采样策略：头部、中部、尾部</li>
	 *     <li>支持已知和未知流长度</li>
	 *     <li>内存友好，适合大文件处理</li>
	 * </ul>
	 *
	 * <p>采样策略说明：</p>
	 * <ul>
	 *     <li>流长度为0时：返回空摘要 {@link IOConstants#EMPTY_DIGEST}</li>
	 *     <li>流长度小于等于3倍采样大小时：读取全部数据</li>
	 *     <li>流长度大于3倍采样大小时：读取头部、中部、尾部各采样大小的数据</li>
	 *     <li>未知流长度（-1）时：一次性读取全部数据（适合小流，大流建议传入准确长度）</li>
	 * </ul>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>采样大小应根据数据特征合理设置</li>
	 *     <li>采样大小越大，摘要计算越准确但性能越低</li>
	 *     <li>未知流长度时会一次性读取全部数据，大流建议传入准确长度避免OOM</li>
	 *     <li>摘要包含流长度信息，确保相同内容不同长度的流摘要不同</li>
	 * </ul>
	 *
	 * @param inputStream  输入流（必须非null）
	 * @param streamLength 流长度（字节），-1表示未知长度
	 * @param sampleSize   采样大小（字节，必须大于0）
	 * @param hashFunc     哈希函数（必须非null）
	 * @return 格式化的摘要字符串
	 * @throws IOException              当流读取失败时抛出
	 * @throws NullPointerException     当inputStream或hashFunc为null时抛出
	 * @throws IllegalArgumentException 当streamLength小于-1、sampleSize小于等于0时抛出
	 * @see #computeDigest(InputStream, long)
	 * @see #computeDigest(InputStream, long, int)
	 * @since 1.1.0
	 */
	public static String computeDigest(final InputStream inputStream, final long streamLength, final int sampleSize,
	                                   final LongHashFunction hashFunc) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		Validate.notNull(hashFunc, "hashFunc 不可为 null");
		Validate.isTrue(sampleSize > 0, "sampleSize 必须大于0");
		Validate.isTrue(streamLength >= -1, "streamLength 必须大于等于 -1");

		int totalSampleSize = 3 * sampleSize;
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES + totalSampleSize);
		buffer.putLong(streamLength);

		if (streamLength == -1) {
			// 未知流长度：一次性全读，适合小流；大流建议传入准确长度避免OOM
			UnsynchronizedByteArrayOutputStream outputStream = toUnsynchronizedByteArrayOutputStream(inputStream);
			byte[] bytes = outputStream.toByteArray();
			int bytesLength = bytes.length;

			if (bytesLength == 0) {
				return IOConstants.EMPTY_DIGEST;
			}

			if (bytesLength <= totalSampleSize) {
				buffer.put(bytes);
			} else {
				byte[] head = Arrays.copyOfRange(bytes, 0, sampleSize);

				int midOffset = sampleSize + ((bytesLength - totalSampleSize) / 2);
				byte[] mid = Arrays.copyOfRange(bytes, midOffset, midOffset + sampleSize);

				int tailOffset = bytesLength - sampleSize;
				byte[] tail = Arrays.copyOfRange(bytes, tailOffset, tailOffset + sampleSize);

				buffer.put(head).put(mid).put(tail);
			}
		} else {
			if (streamLength == 0) {
				return IOConstants.EMPTY_DIGEST;
			}

			if (streamLength <= totalSampleSize) {
				byte[] all = inputStream.readNBytes((int) streamLength);
				buffer.put(all);
			} else {
				byte[] head = new byte[sampleSize];
				inputStream.readNBytes(head, 0, head.length);

				byte[] mid = new byte[sampleSize];
				long midStart = sampleSize + (streamLength - totalSampleSize) / 2;
				skip(inputStream, midStart - sampleSize);
				inputStream.readNBytes(mid, 0, mid.length);

				byte[] tail = new byte[sampleSize];
				long tailStart = streamLength - sampleSize;
				skip(inputStream, tailStart - midStart - sampleSize);
				inputStream.readNBytes(tail, 0, tail.length);

				buffer.put(head).put(mid).put(tail);
			}
		}

		buffer.flip();
		byte[] validData = Arrays.copyOf(buffer.array(), buffer.remaining());
		long hash = hashFunc.hashBytes(validData);
		return String.format(IOConstants.DIGEST_FORMAT, hash);
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
	 * 创建非同步缓冲读取器（使用默认缓冲区大小）
	 * <p>默认缓冲区大小为 {@link IOUtils#DEFAULT_BUFFER_SIZE}</p>
	 *
	 * @param reader 原始读取器（必须非null）
	 * @return 包装后的缓冲读取器
	 * @throws NullPointerException 当reader为null时抛出
	 * @see #unsynchronizedBuffer(Reader, int)
	 * @since 1.1.0
	 */
	public static UnsynchronizedBufferedReader unsynchronizedBuffer(final Reader reader) {
		return unsynchronizedBuffer(reader, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * 创建非同步缓冲读取器（自定义缓冲区大小）
	 * <p>特性说明：</p>
	 * <ul>
	 *     <li>非线程安全实现，适合单线程使用</li>
	 *     <li>不进行同步操作，性能优于同步缓冲流</li>
	 *     <li>如果读取器已经是非同步缓冲流则直接返回</li>
	 *     <li>缓冲区大小应根据数据量合理设置</li>
	 * </ul>
	 *
	 * @param reader     原始读取器（必须非null）
	 * @param bufferSize 缓冲区大小（单位：字符，必须大于0）
	 * @return 包装后的缓冲读取器
	 * @throws NullPointerException     当reader为null时抛出
	 * @throws IllegalArgumentException 当bufferSize小于等于0时抛出
	 * @see #getBufferSize(long)
	 * @since 1.1.0
	 */
	public static UnsynchronizedBufferedReader unsynchronizedBuffer(final Reader reader, final int bufferSize) {
		Validate.notNull(reader, "reader 不可为 null");
		Validate.isTrue(bufferSize > 0, "bufferSize 必须大于0");

		if (reader instanceof UnsynchronizedBufferedReader) {
			return (UnsynchronizedBufferedReader) reader;
		}
		return new UnsynchronizedBufferedReader(reader, bufferSize);
	}

	/**
	 * 创建非同步缓冲输入流（使用默认缓冲区大小）
	 * <p>默认缓冲区大小为 {@link IOUtils#DEFAULT_BUFFER_SIZE}</p>
	 *
	 * @param inputStream 原始输入流（必须非null）
	 * @return 包装后的缓冲输入流
	 * @throws IOException          当流初始化失败时抛出
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
	 * @param bufferSize  缓冲区大小（单位：字节，必须大于0）
	 * @return 包装后的缓冲输入流
	 * @throws IOException              当流初始化失败时抛出
	 * @throws NullPointerException     当inputStream为null时抛出
	 * @throws IllegalArgumentException 当bufferSize小于等于0时抛出
	 * @see #getBufferSize(long)
	 * @since 1.0.0
	 */
	public static UnsynchronizedBufferedInputStream unsynchronizedBuffer(final InputStream inputStream,
	                                                                     final int bufferSize) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
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
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>流读取/写入失败</li>
	 *                                  <li>内存不足</li>
	 *                              </ul>
	 * @throws NullPointerException 当inputStream为null时抛出
	 * @see #toUnsynchronizedByteArrayOutputStream(InputStream, int)
	 * @since 1.0.0
	 */
	public static UnsynchronizedByteArrayOutputStream toUnsynchronizedByteArrayOutputStream(final InputStream inputStream) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

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
	 * @param bufferSize  初始缓冲区大小（单位：字节，必须大于0）
	 * @return 包含输入流数据的字节数组输出流
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>流读取/写入失败</li>
	 *                                      <li>内存不足</li>
	 *                                  </ul>
	 * @throws NullPointerException     当inputStream为null时抛出
	 * @throws IllegalArgumentException 当bufferSize小于等于0时抛出
	 * @see #getBufferSize(long)
	 * @since 1.0.0
	 */
	public static UnsynchronizedByteArrayOutputStream toUnsynchronizedByteArrayOutputStream(final InputStream inputStream,
	                                                                                        final int bufferSize) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		UnsynchronizedByteArrayOutputStream outputStream = toUnsynchronizedByteArrayOutputStream(bufferSize);
		outputStream.write(inputStream);
		return outputStream;
	}

	/**
	 * AES/CBC/PKCS5Padding 模式流加密
	 * <p>实现特性：</p>
	 * <ul>
	 *   <li>使用调用方提供的 16 字节初始化向量（IV）</li>
	 *   <li>自动处理 PKCS5 填充</li>
	 *   <li>支持 16/24/32 字节密钥（128/192/256 位）</li>
	 *   <li>采用默认缓冲区（{@link IOUtils#DEFAULT_BUFFER_SIZE}）进行高性能流式加密</li>
	 * </ul>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *   <li>IV 长度必须为 16 字节</li>
	 *   <li>解密时必须使用与加密完全一致的密钥与 IV</li>
	 * </ul>
	 *
	 * @param inputStream  输入流（必须可读且未关闭）
	 * @param outputStream 输出流（必须可写且未关闭）
	 * @param key          加密密钥（长度必须为 16/24/32 字节）
	 * @param iv           初始化向量（16 字节）
	 * @throws IOException              流读写异常或加密配置错误
	 * @throws NullPointerException     当输入或输出流为 null
	 * @throws IllegalArgumentException 当密钥或 IV 长度不符合规范
	 * @since 1.0.0
	 */
	public static void encrypt(final InputStream inputStream, final OutputStream outputStream, final byte[] key,
	                           final byte[] iv) throws IOException {
		validateArgs(inputStream, outputStream, key, iv);

		Key secretKey = new SecretKeySpec(key, AES.ALGORITHM);
		AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(iv);
		try (CryptoOutputStream cryptoOutputStream = new CryptoOutputStream(AES.CBC_PKCS5_PADDING,
			DEFAULT_PROPERTIES, outputStream, secretKey, ivParameterSpec)) {
			inputStream.transferTo(cryptoOutputStream);
		}
	}

	/**
	 * AES/CBC/PKCS5Padding 模式流解密
	 * <p>注意事项：</p>
	 * <ul>
	 *   <li>必须使用与加密完全一致的密钥与 IV</li>
	 *   <li>输入流必须为 CBC/PKCS5Padding 模式加密后的原始数据</li>
	 *   <li>采用默认缓冲区（{@link IOUtils#DEFAULT_BUFFER_SIZE}）进行高性能流式加密</li>
	 * </ul>
	 *
	 * @param inputStream  加密输入流（必须可读且未关闭）
	 * @param outputStream 解密输出流（必须可写且未关闭）
	 * @param key          解密密钥（长度必须为 16/24/32 字节，需与加密一致）
	 * @param iv           初始化向量（16 字节，需与加密一致）
	 * @throws IOException              流读写异常或解密配置错误
	 * @throws NullPointerException     当输入或输出流为 null
	 * @throws IllegalArgumentException 当密钥或 IV 长度不符合规范
	 * @since 1.0.0
	 */
	public static void decrypt(final InputStream inputStream, final OutputStream outputStream, final byte[] key,
	                           final byte[] iv) throws IOException {
		validateArgs(inputStream, outputStream, key, iv);

		Key secretKey = new SecretKeySpec(key, AES.ALGORITHM);
		AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(iv);
		try (CryptoInputStream cryptoInputStream = new CryptoInputStream(AES.CBC_PKCS5_PADDING,
			DEFAULT_PROPERTIES, inputStream, secretKey, ivParameterSpec)) {
			cryptoInputStream.transferTo(outputStream);
		}
	}

	/**
	 * AES/CBC/PKCS5Padding 模式流加密（自定义缓冲区）
	 * <p>实现特性：</p>
	 * <ul>
	 *   <li>使用调用方提供的 16 字节 IV 与 16/24/32 字节密钥</li>
	 *   <li>自动处理 PKCS5 填充</li>
	 *   <li>通过 {@code bufferSize} 设置流缓冲区大小</li>
	 * </ul>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *   <li>合理选择缓冲区大小：过小影响性能，过大增加内存占用</li>
	 *   <li>解密需要使用相同的密钥与 IV</li>
	 * </ul>
	 *
	 * @param inputStream  输入流（必须可读且未关闭）
	 * @param outputStream 输出流（必须可写且未关闭）
	 * @param key          加密密钥（长度必须为 16/24/32 字节）
	 * @param iv           初始化向量（16 字节）
	 * @param bufferSize   流缓冲区大小（字节）
	 * @throws IOException              流读写异常或加密配置错误
	 * @throws NullPointerException     当输入或输出流为 null
	 * @throws IllegalArgumentException 当密钥或 IV 长度不符合规范
	 * @since 1.0.0
	 */
	public static void encrypt(final InputStream inputStream, final OutputStream outputStream, final byte[] key,
	                           final byte[] iv, final int bufferSize) throws IOException {
		validateArgs(inputStream, outputStream, key, iv);

		Properties properties = new Properties();
		properties.put(CryptoInputStream.STREAM_BUFFER_SIZE_KEY, bufferSize);

		Key secretKey = new SecretKeySpec(key, AES.ALGORITHM);
		AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(iv);
		try (CryptoOutputStream cryptoOutputStream = new CryptoOutputStream(AES.CBC_PKCS5_PADDING,
			properties, outputStream, secretKey, ivParameterSpec)) {
			inputStream.transferTo(cryptoOutputStream);
		}
	}

	/**
	 * AES/CBC/PKCS5Padding 模式流解密（自定义缓冲区）
	 * <p>注意事项：</p>
	 * <ul>
	 *   <li>必须使用与加密一致的密钥与 IV</li>
	 *   <li>输入流必须为 CBC/PKCS5Padding 模式加密后的原始数据</li>
	 *   <li>通过 {@code bufferSize} 设置流缓冲区大小</li>
	 * </ul>
	 *
	 * @param inputStream  加密输入流（必须可读且未关闭）
	 * @param outputStream 解密输出流（必须可写且未关闭）
	 * @param key          解密密钥（长度必须为 16/24/32 字节，需与加密一致）
	 * @param iv           初始化向量（16 字节，需与加密一致）
	 * @param bufferSize   流缓冲区大小（字节）
	 * @throws IOException              流读写异常或解密配置错误
	 * @throws NullPointerException     当输入或输出流为 null
	 * @throws IllegalArgumentException 当密钥或 IV 长度不符合规范
	 * @since 1.0.0
	 */
	public static void decrypt(final InputStream inputStream, final OutputStream outputStream, final byte[] key,
	                           final byte[] iv, final int bufferSize) throws IOException {
		validateArgs(inputStream, outputStream, key, iv);

		Properties properties = new Properties();
		properties.put(CryptoInputStream.STREAM_BUFFER_SIZE_KEY, bufferSize);

		Key secretKey = new SecretKeySpec(key, AES.ALGORITHM);
		AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(iv);
		try (CryptoInputStream cryptoInputStream = new CryptoInputStream(AES.CBC_PKCS5_PADDING,
			properties, inputStream, secretKey, ivParameterSpec)) {
			cryptoInputStream.transferTo(outputStream);
		}
	}

	/**
	 * 使用 AES/CTR 模式加密
	 * <p>实现特性：</p>
	 * <ul>
	 *   <li>使用调用方提供的 16 字节初始化向量（IV）</li>
	 *   <li>支持 16/24/32 字节密钥（128/192/256 位）</li>
	 *   <li>CTR 模式无填充，适合任意长度数据</li>
	 *   <li>采用默认缓冲区（{@link IOUtils#DEFAULT_BUFFER_SIZE}）进行高性能流式加密</li>
	 * </ul>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *   <li>IV 长度必须为 16 字节</li>
	 *   <li>解密时必须使用与加密完全一致的密钥与 IV</li>
	 * </ul>
	 *
	 * @param inputStream  原始输入流（必须非 null 且未关闭）
	 * @param outputStream 加密输出流（必须非 null 且未关闭）
	 * @param key          加密密钥（16/24/32 字节）
	 * @param iv           初始化向量（16 字节）
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                       <li>密钥或 IV 长度不符合规范</li>
	 *                       <li>流读写异常</li>
	 *                       <li>加密配置错误</li>
	 *                     </ul>
	 * @since 1.0.0
	 */
	public static void encryptByCtr(final InputStream inputStream, final OutputStream outputStream, final byte[] key,
	                                final byte[] iv) throws IOException {
		validateArgs(inputStream, outputStream, key, iv);

		try (CtrCryptoOutputStream cryptoInputStream = new CtrCryptoOutputStream(DEFAULT_PROPERTIES, outputStream, key, iv)) {
			inputStream.transferTo(cryptoInputStream);
		}
	}

	/**
	 * 使用 AES/CTR 模式解密
	 * <p>注意事项：</p>
	 * <ul>
	 *   <li>必须使用与加密完全一致的密钥与 IV</li>
	 *   <li>输入流必须为 CTR 模式加密的原始数据</li>
	 *   <li>采用默认缓冲区（{@link IOUtils#DEFAULT_BUFFER_SIZE}）进行高性能流式加密</li>
	 * </ul>
	 *
	 * @param inputStream  CTR 加密输入流（必须非 null 且未关闭）
	 * @param outputStream 解密输出流（必须非 null 且未关闭）
	 * @param key          解密密钥（16/24/32 字节，需与加密一致）
	 * @param iv           初始化向量（16 字节，需与加密一致）
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                       <li>密钥或 IV 长度不符合规范</li>
	 *                       <li>流读写异常</li>
	 *                       <li>解密配置错误</li>
	 *                     </ul>
	 * @since 1.0.0
	 */
	public static void decryptByCtr(final InputStream inputStream, final OutputStream outputStream, final byte[] key,
	                                final byte[] iv) throws IOException {
		validateArgs(inputStream, outputStream, key, iv);

		try (CtrCryptoInputStream cryptoInputStream = new CtrCryptoInputStream(DEFAULT_PROPERTIES, inputStream, key, iv)) {
			cryptoInputStream.transferTo(outputStream);
		}
	}

	/**
	 * CTR 模式流加密（自定义缓冲区）
	 * <p>实现特性：</p>
	 * <ul>
	 *   <li>支持 16/24/32 字节密钥（128/192/256 位）</li>
	 *   <li>必须提供 16 字节初始化向量（IV）</li>
	 *   <li>CTR 模式无填充，支持任意长度数据</li>
	 *   <li>通过 {@code bufferSize} 设置流缓冲区大小</li>
	 * </ul>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *   <li>根据数据规模选择合理缓冲区，过小影响性能，过大增加内存占用</li>
	 *   <li>解密需要使用相同的密钥与 IV</li>
	 * </ul>
	 *
	 * @param inputStream  原始输入流（必须非 null 且未关闭）
	 * @param outputStream 加密输出流（必须非 null 且未关闭）
	 * @param key          加密密钥（16/24/32 字节）
	 * @param iv           初始化向量（16 字节）
	 * @param bufferSize   流缓冲区大小（字节）
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                       <li>密钥或 IV 长度不符合规范</li>
	 *                       <li>流读写异常</li>
	 *                       <li>加密配置错误</li>
	 *                     </ul>
	 * @since 1.0.0
	 */
	public static void encryptByCtr(final InputStream inputStream, final OutputStream outputStream,
	                                final byte[] key, final byte[] iv, final int bufferSize) throws IOException {
		validateArgs(inputStream, outputStream, key, iv);

		Properties properties = new Properties();
		properties.put(CryptoInputStream.STREAM_BUFFER_SIZE_KEY, bufferSize);

		try (CtrCryptoOutputStream cryptoInputStream = new CtrCryptoOutputStream(properties, outputStream, key, iv)) {
			inputStream.transferTo(cryptoInputStream);
		}
	}

	/**
	 * CTR 模式流解密（自定义缓冲区）
	 * <p>注意事项：</p>
	 * <ul>
	 *   <li>必须使用与加密一致的密钥与 IV</li>
	 *   <li>通过 {@code bufferSize} 设置流缓冲区大小</li>
	 *   <li>输入流必须为 CTR 模式加密的原始数据</li>
	 * </ul>
	 *
	 * @param inputStream  CTR 加密输入流（必须非 null 且未关闭）
	 * @param outputStream 解密输出流（必须非 null 且未关闭）
	 * @param key          解密密钥（16/24/32 字节，需与加密一致）
	 * @param iv           初始化向量（16 字节，需与加密一致）
	 * @param bufferSize   流缓冲区大小（字节）
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                       <li>密钥或 IV 长度不符合规范</li>
	 *                       <li>流读写异常</li>
	 *                       <li>解密配置错误</li>
	 *                     </ul>
	 * @since 1.0.0
	 */
	public static void decryptByCtr(final InputStream inputStream, final OutputStream outputStream,
	                                final byte[] key, final byte[] iv, final int bufferSize) throws IOException {
		validateArgs(inputStream, outputStream, key, iv);

		Properties properties = new Properties();
		properties.put(CryptoInputStream.STREAM_BUFFER_SIZE_KEY, bufferSize);

		try (CtrCryptoInputStream cryptoInputStream = new CtrCryptoInputStream(properties, inputStream, key, iv)) {
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
	 * @param key          加密密码字节数组（必须16/24/32字节）
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
}
