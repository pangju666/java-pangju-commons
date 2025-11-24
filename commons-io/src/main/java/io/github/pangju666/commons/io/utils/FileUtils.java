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
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.input.BufferedFileChannelInputStream;
import org.apache.commons.io.input.MemoryMappedFileInputStream;
import org.apache.commons.io.input.UnsynchronizedBufferedInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tika.metadata.Metadata;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 增强型文件工具类
 * <p>扩展自 {@link org.apache.commons.io.FileUtils}，在通用文件操作基础上强化性能与可用性。</p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li>高性能读取：提供内存映射、缓冲通道、非同步缓冲等多种输入流</li>
 *   <li>内容类型检测：独立于扩展名识别大量 MIME 类型</li>
 *   <li>元数据解析：集成 Apache Tika 进行元数据提取</li>
 *   <li>健壮删除：增强删除策略，可处理被占用文件</li>
 *   <li>文件加解密：提供 AES/CBC 与 AES/CTR 文件加/解密便捷方法（委托 {@link IOUtils}，流式处理）</li>
 *   <li>快速摘要：基于 xxHash64 的三段采样文件摘要</li>
 * </ul>
 *
 * <h3>加解密说明</h3>
 * <ul>
 *   <li>密钥长度：16/24/32 字节（128/192/256 位）</li>
 *   <li>IV 长度：16 字节；解密需与加密一致</li>
 *   <li>错误处理：CBC 模式填充验证失败或配置错误将抛出 {@code IOException}</li>
 * </ul>
 *
 * <h3>设计原则</h3>
 * <ul>
 *   <li>严格参数校验，避免非法输入</li>
 *   <li>针对大文件场景优化性能</li>
 * </ul>
 *
 * <h3>使用建议</h3>
 * <ul>
 *   <li>不关注线程安全：优先 {@link #openUnsynchronizedBufferedInputStream(File)}</li>
 *   <li>常规大小文件：优先 {@link #openInputStream(File)}</li>
 *   <li>不敏感于内存占用且需极致性能：考虑 {@link #openMemoryMappedFileInputStream(File)}</li>
 *   <li>其他场景：使用 {@link #openBufferedFileChannelInputStream(File)}</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class FileUtils extends org.apache.commons.io.FileUtils {
	protected static final int MB_1 = 1024 * 1024;
	protected static final int MB_4 = 4 * MB_1;
	protected static final int MB_16 = 16 * MB_1;
	protected static final int MB_32 = 32 * MB_1;
	protected static final int MB_64 = 64 * MB_1;
	protected static final int MB_100 = 100 * MB_1;
	protected static final long GB_1 = 1024 * MB_1;
	protected static final long GB_10 = 10 * GB_1;

	/**
	 * 64 位 xxHash 函数
	 * <p>用于快速计算文件摘要，兼顾性能与较低碰撞率。</p>
	 *
	 * @since 1.0.0
	 */
	protected static final LongHashFunction HASH_FUNC = LongHashFunction.xx();
	/**
	 * 采样字节数
	 * <p>分别从文件头/中/尾各读取该大小的字节用于摘要计算。</p>
	 *
	 * @since 1.0.0
	 */
	protected static final int SAMPLE_SIZE = 64;
	/**
	 * 空文件摘要固定值
	 * <p>当文件大小为 0 时直接返回该 16 位十六进制字符串。</p>
	 *
	 * @since 1.0.0
	 */
	protected static final String EMPTY_FILE_DIGEST = "0000000000000000";
	/**
	 * 摘要输出格式
	 * <p>使用 16 位十六进制、左侧 0 填充（`%016x`）。</p>
	 *
	 * @since 1.0.0
	 */
	protected static final String FILE_DIGEST_FORMAT = "%016x";

	protected FileUtils() {
	}

	/**
	 * 计算文件摘要
	 * <p>基于文件大小与三段采样（头/中/尾各 {@link #SAMPLE_SIZE} 字节）组合后使用 xxHash64 计算，输出 16 位十六进制字符串。</p>
	 *
	 * <h3>处理规则</h3>
	 * <ul>
	 *   <li>空文件返回固定值 {@link #EMPTY_FILE_DIGEST}</li>
	 *   <li>中段位置在避开头尾的区间居中选取</li>
	 * </ul>
	 *
	 * @param file 目标文件，不能为空
	 * @return 16 位十六进制摘要字符串（左侧 0 填充）
	 * @throws IOException 读写通道异常
	 * @since 1.0.0
	 */
	public static String computeDigest(final File file) throws IOException {
		checkFile(file, "file不可为 null");

		long fileSize = file.length();
		if (fileSize == 0) {
			return EMPTY_FILE_DIGEST;
		}

		byte[] head = new byte[SAMPLE_SIZE];
		byte[] mid = new byte[SAMPLE_SIZE];
		byte[] tail = new byte[SAMPLE_SIZE];

		try (RandomAccessFile raf = new RandomAccessFile(file, "r");
			 FileChannel channel = raf.getChannel()) {

			// 读取开头
			readFully(channel, 0, head);

			// 读取结尾
			long tailPos = Math.max(0, fileSize - SAMPLE_SIZE);
			readFully(channel, tailPos, tail);

			// 读取中间（避开头尾）
			long midPos;
			if (fileSize <= 2L * SAMPLE_SIZE) {
				// 文件太小，中间与头重叠
				midPos = 0;
			} else {
				// 在 [SAMPLE_SIZE, fileSize - SAMPLE_SIZE) 区间居中
				midPos = SAMPLE_SIZE + (fileSize - 2L * SAMPLE_SIZE) / 2;
			}
			readFully(channel, midPos, mid);
		}

		// 合并：[fileSize][head][mid][tail]
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES + 3 * SAMPLE_SIZE);
		buffer.putLong(fileSize);
		buffer.put(head);
		buffer.put(mid);
		buffer.put(tail);

		long hash = HASH_FUNC.hashBytes(buffer.array());
		return String.format(FILE_DIGEST_FORMAT, hash); // 16 位 0 补齐
	}

	/**
	 * 从指定位置读取固定长度字节
	 * <p>循环读取至缓冲区填满或到达 EOF，允许部分读取。</p>
	 *
	 * @param channel  文件通道
	 * @param position 起始读取位置（字节偏移）
	 * @param buffer   目标缓冲区（长度即期望读取量）
	 * @throws IOException 通道读写异常
	 * @since 1.0.0
	 */
	protected static void readFully(final FileChannel channel, final long position, final byte[] buffer) throws IOException {
		ByteBuffer bb = ByteBuffer.wrap(buffer);
		int totalRead = 0;
		while (totalRead < buffer.length) {
			channel.position(position + totalRead);
			int read = channel.read(bb);
			if (read <= 0) {
				break;
			}
			totalRead += read;
		}
	}

	/**
	 * 根据文件大小计算最佳缓冲区大小
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
	 * @param file 目标文件（必须存在且可读）
	 * @return 优化的缓冲区大小（单位：字节）
	 * @throws IOException 当文件不存在或无法读取大小时抛出
	 * @see IOUtils#getBufferSize(long)
	 * @since 1.0.0
	 */
	public static int getBufferSize(final File file) throws IOException {
		checkFile(file, "file不可为 null");

		return IOUtils.getBufferSize(file.length());
	}

	/**
	 * 根据文件大小计算滑动窗口缓冲区大小
	 * <p>分块策略：</p>
	 * <ul>
	 *     <li>小文件(100MB以下)：4MB</li>
	 *     <li>中等文件(100MB~1GB)：16MB</li>
	 *     <li>大文件(1GB~10GB)：32MB</li>
	 *     <li>超大文件(10GB以上)：64MB</li>
	 * </ul>
	 *
	 * <p>设计考虑：</p>
	 * <ul>
	 *     <li>平衡内存使用和IO效率</li>
	 *     <li>减少系统调用次数</li>
	 *     <li>适配不同存储介质特性</li>
	 * </ul>
	 *
	 * @param file 目标文件（必须存在且可读）
	 * @return 优化的滑动窗口缓冲区大小（单位：字节）
	 * @throws IOException 当文件不存在或无法读取大小时抛出
	 * @since 1.0.0
	 */
	public static int getSlidingBufferSize(final File file) throws IOException {
		checkFile(file, "file不可为 null");

		long fileSize = file.length();
		if (fileSize < MB_100) { // < 100MB
			return MB_4;
		} else if (fileSize < GB_1) { // < 1GB
			return MB_16;
		} else if (fileSize < GB_10) { // < 10GB
			return MB_32;
		} else {
			return MB_64;
		}
	}

	/**
	 * 打开非同步缓冲输入流（自动计算缓冲区大小）
	 * <p>核心特性：</p>
	 * <ul>
	 *     <li>线程安全的非阻塞IO操作</li>
	 *     <li>自动根据文件大小优化缓冲区</li>
	 *     <li>内置文件存在性校验</li>
	 * </ul>
	 *
	 * @param file 要读取的文件对象（必须存在且可读）
	 * @return 配置好的非同步缓冲输入流实例
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>文件不存在或不可读</li>
	 *                         <li>文件被其他进程独占锁定</li>
	 *                     </ul>
	 * @see UnsynchronizedBufferedInputStream.Builder
	 * @since 1.0.0
	 */
	public static UnsynchronizedBufferedInputStream openUnsynchronizedBufferedInputStream(final File file) throws IOException {
		checkFile(file, "file不可为 null");

		return new UnsynchronizedBufferedInputStream.Builder()
			.setBufferSize(IOUtils.getBufferSize(file.length()))
			.setFile(file)
			.get();
	}

	/**
	 * 打开缓冲文件通道输入流（自动计算缓冲区大小）
	 * <p>性能特点：</p>
	 * <ul>
	 *     <li>基于NIO FileChannel实现高性能读取</li>
	 *     <li>自动根据文件大小优化缓冲区</li>
	 *     <li>适合顺序读取大文件场景</li>
	 * </ul>
	 *
	 * @param file 要读取的文件对象（必须存在且可读）
	 * @return 配置好的缓冲文件通道输入流
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>文件不存在或不可读</li>
	 *                         <li>文件被其他进程独占锁定</li>
	 *                     </ul>
	 * @see BufferedFileChannelInputStream.Builder
	 * @since 1.0.0
	 */
	public static BufferedFileChannelInputStream openBufferedFileChannelInputStream(final File file) throws IOException {
		checkFile(file, "file不可为 null");

		return BufferedFileChannelInputStream
			.builder()
			.setFile(file)
			.setBufferSize(IOUtils.getBufferSize(file.length()))
			.get();
	}

	/**
	 * 打开内存映射文件输入流（自动计算滑动窗口大小）
	 * <p>技术优势：</p>
	 * <ul>
	 *     <li>零拷贝技术减少内存复制开销</li>
	 *     <li>自动根据文件大小优化映射区域</li>
	 *     <li>特别适合大文件随机访问</li>
	 * </ul>
	 *
	 * <p>性能建议：</p>
	 * <ul>
	 *     <li>频繁小数据读取建议使用缓冲流包装</li>
	 *     <li>映射区域大小应与文件系统块大小对齐</li>
	 *     <li>超大文件建议使用分块映射</li>
	 * </ul>
	 *
	 * @param file 要映射的文件对象（必须存在且可读）
	 * @return 配置好的内存映射输入流
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>文件不存在或不可读</li>
	 *                         <li>内存映射失败（地址空间不足）</li>
	 *                     </ul>
	 * @see #getSlidingBufferSize(File)
	 * @see MemoryMappedFileInputStream.Builder
	 * @since 1.0.0
	 */
	public static MemoryMappedFileInputStream openMemoryMappedFileInputStream(final File file) throws IOException {
		int chunkSize = getSlidingBufferSize(file);
		return MemoryMappedFileInputStream
			.builder()
			.setFile(file)
			.setBufferSize(chunkSize)
			.get();
	}

	/**
	 * 使用 AES/CBC/PKCS5Padding 模式加密文件
	 * <p><strong>特性：</strong></p>
	 * <ul>
	 *     <li>使用 PKCS5Padding 填充，兼容任意长度数据</li>
	 *     <li>流式处理，适合大文件</li>
	 * </ul>
	 *
	 * @param inputFile  待加密源文件（必须存在且可读）
	 * @param outputFile 加密输出文件（自动创建父目录）
	 * @param key        加密密钥（16/24/32 字节）
	 * @param iv         初始化向量（16 字节，解密时必须与加密一致）
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输入文件不存在或不可读</li>
	 *                                      <li>输出路径不可写或创建失败</li>
	 *                                      <li>文件 IO 操作失败</li>
	 *                                  </ul>
	 * @throws IllegalArgumentException 当密钥长度不是 16/24/32 字节或 IV 长度不是 16 字节时
	 * @see IOUtils#encrypt(InputStream, OutputStream, byte[], byte[])
	 * @since 1.0.0
	 */
	public static void encryptFile(final File inputFile, final File outputFile, final byte[] key, final byte[] iv) throws IOException {
		checkFile(inputFile, "inputFile 不可为 null");
		checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (OutputStream outputStream = openOutputStream(outputFile);
			 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream, IOUtils.DEFAULT_BUFFER_SIZE);
			 UnsynchronizedBufferedInputStream bufferedInputStream = openUnsynchronizedBufferedInputStream(inputFile)) {
			IOUtils.encrypt(bufferedInputStream, bufferedOutputStream, key, iv);
		}
	}

	/**
	 * 使用 AES/CBC/PKCS5Padding 模式加密文件（自定义缓冲区）
	 * <p>加密使用的 IV 必须在解密时保持完全一致。</p>
	 *
	 * @param inputFile  待加密源文件（必须存在且可读）
	 * @param outputFile 加密输出文件（自动创建父目录）
	 * @param key        加密密钥（16/24/32 字节）
	 * @param iv         初始化向量（16 字节）
	 * @param bufferSize 处理缓冲区大小（正数，建议参考 {@link IOUtils#DEFAULT_BUFFER_SIZE}）
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输入文件不存在或不可读</li>
	 *                                      <li>输出路径不可写或创建失败</li>
	 *                                      <li>文件 IO 操作失败</li>
	 *                                  </ul>
	 * @throws IllegalArgumentException 当密钥长度不是 16/24/32 字节或 IV 长度不是 16 字节时
	 * @see IOUtils#encrypt(InputStream, OutputStream, byte[], byte[], int)
	 * @since 1.0.0
	 */
	public static void encryptFile(final File inputFile, final File outputFile, final byte[] key, final byte[] iv,
								   final int bufferSize) throws IOException {
		checkFile(inputFile, "inputFile 不可为 null");
		checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (OutputStream outputStream = openOutputStream(outputFile);
			 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream, bufferSize);
			 UnsynchronizedBufferedInputStream bufferedInputStream = openUnsynchronizedBufferedInputStream(inputFile)) {
			IOUtils.encrypt(bufferedInputStream, bufferedOutputStream, key, iv, bufferSize);
		}
	}

	/**
	 * 使用 AES/CBC/PKCS5Padding 模式解密文件
	 * <p><strong>要求：</strong></p>
	 * <ul>
	 *     <li>解密密钥与加密时一致</li>
	 *     <li>IV 与加密时一致</li>
	 *     <li>加密文件必须完整未修改，否则可能出现填充验证失败</li>
	 * </ul>
	 *
	 * @param inputFile  加密文件（必须存在且可读）
	 * @param outputFile 解密输出文件（自动创建父目录）
	 * @param key        解密密钥（16/24/32 字节，与加密时一致）
	 * @param iv         初始化向量（16 字节，与加密时一致）
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输入文件不存在或不可读</li>
	 *                                      <li>输出路径不可写或创建失败</li>
	 *                                      <li>文件 IO 操作失败</li>
	 *                                      <li>填充验证失败（可能文件被篡改）</li>
	 *                                  </ul>
	 * @throws IllegalArgumentException 当密钥长度不是 16/24/32 字节或 IV 长度不是 16 字节时
	 * @see IOUtils#decrypt(InputStream, OutputStream, byte[], byte[])
	 * @since 1.0.0
	 */
	public static void decryptFile(final File inputFile, final File outputFile, final byte[] key, final byte[] iv) throws IOException {
		checkFile(inputFile, "inputFile 不可为 null");
		checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (OutputStream outputStream = openOutputStream(outputFile);
			 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream, IOUtils.DEFAULT_BUFFER_SIZE);
			 UnsynchronizedBufferedInputStream bufferedInputStream = openUnsynchronizedBufferedInputStream(inputFile)) {
			IOUtils.decrypt(bufferedInputStream, bufferedOutputStream, key, iv);
		}
	}

	/**
	 * 使用 AES/CBC/PKCS5Padding 模式解密文件（自定义缓冲区）
	 * <p>解密所用的密钥与 IV 必须与加密时完全一致。</p>
	 *
	 * @param inputFile  加密文件（必须存在且可读）
	 * @param outputFile 解密输出文件（自动创建父目录）
	 * @param key        解密密钥（16/24/32 字节，与加密时一致）
	 * @param iv         初始化向量（16 字节，与加密时一致）
	 * @param bufferSize 处理缓冲区大小（正数，建议参考 {@link IOUtils#DEFAULT_BUFFER_SIZE}）
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输入文件不存在或不可读</li>
	 *                                      <li>输出路径不可写或创建失败</li>
	 *                                      <li>文件 IO 操作失败</li>
	 *                                      <li>填充验证失败（可能文件被篡改）</li>
	 *                                  </ul>
	 * @throws IllegalArgumentException 当密钥长度不是 16/24/32 字节或 IV 长度不是 16 字节时
	 * @see IOUtils#decrypt(InputStream, OutputStream, byte[], byte[], int)
	 * @since 1.0.0
	 */
	public static void decryptFile(final File inputFile, final File outputFile, final byte[] key, final byte[] iv,
								   final int bufferSize) throws IOException {
		checkFile(inputFile, "inputFile 不可为 null");
		checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (OutputStream outputStream = openOutputStream(outputFile);
			 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream, bufferSize);
			 UnsynchronizedBufferedInputStream bufferedInputStream = openUnsynchronizedBufferedInputStream(inputFile)) {
			IOUtils.decrypt(bufferedInputStream, bufferedOutputStream, key, iv, bufferSize);
		}
	}

	/**
	 * 使用 AES/CTR 模式加密文件
	 * <p><strong>技术特性：</strong></p>
	 * <ul>
	 *     <li>无填充要求，支持任意长度数据</li>
	 *     <li>计数器模式支持并行处理</li>
	 *     <li>流式处理，适合大文件</li>
	 * </ul>
	 *
	 * @param inputFile  待加密源文件（必须存在且可读）
	 * @param outputFile 加密输出文件（自动创建父目录）
	 * @param key        加密密钥（16/24/32 字节）
	 * @param iv         初始化向量（16 字节，解密时必须与加密一致）
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输入文件不存在或不可读</li>
	 *                                      <li>输出路径不可写或创建失败</li>
	 *                                      <li>文件 IO 操作失败</li>
	 *                                  </ul>
	 * @throws IllegalArgumentException 当密钥长度不是 16/24/32 字节或 IV 长度不是 16 字节时
	 * @see IOUtils#encryptByCtr(InputStream, OutputStream, byte[], byte[])
	 * @since 1.0.0
	 */
	public static void encryptFileByCtr(final File inputFile, final File outputFile, final byte[] key, final byte[] iv) throws IOException {
		checkFile(inputFile, "inputFile 不可为 null");
		checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (OutputStream outputStream = openOutputStream(outputFile);
			 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream, IOUtils.DEFAULT_BUFFER_SIZE);
			 UnsynchronizedBufferedInputStream bufferedInputStream = openUnsynchronizedBufferedInputStream(inputFile)) {
			IOUtils.encryptByCtr(bufferedInputStream, bufferedOutputStream, key, iv);
		}
	}

	/**
	 * 使用 AES/CTR 模式加密文件（自定义缓冲区）
	 * <p>加密使用的 IV 必须在解密时保持完全一致。</p>
	 *
	 * @param inputFile  待加密源文件（必须存在且可读）
	 * @param outputFile 加密输出文件（自动创建父目录）
	 * @param key        加密密钥（16/24/32 字节）
	 * @param iv         初始化向量（16 字节）
	 * @param bufferSize 处理缓冲区大小（正数，建议参考 {@link IOUtils#DEFAULT_BUFFER_SIZE}）
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输入文件不存在或不可读</li>
	 *                                      <li>输出路径不可写或创建失败</li>
	 *                                      <li>文件 IO 操作失败</li>
	 *                                  </ul>
	 * @throws IllegalArgumentException 当密钥长度不是 16/24/32 字节或 IV 长度不是 16 字节时
	 * @see IOUtils#encryptByCtr(InputStream, OutputStream, byte[], byte[], int)
	 * @since 1.0.0
	 */
	public static void encryptFileByCtr(final File inputFile, final File outputFile, final byte[] key, final byte[] iv,
										final int bufferSize) throws IOException {
		checkFile(inputFile, "inputFile 不可为 null");
		checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (OutputStream outputStream = openOutputStream(outputFile);
			 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream, bufferSize);
			 UnsynchronizedBufferedInputStream bufferedInputStream = openUnsynchronizedBufferedInputStream(inputFile)) {
			IOUtils.encryptByCtr(bufferedInputStream, bufferedOutputStream, key, iv, bufferSize);
		}
	}

	/**
	 * AES/CTR 模式文件解密
	 * <p><strong>技术特性：</strong></p>
	 * <ul>
	 *     <li>无填充要求，支持任意长度数据</li>
	 *     <li>计数器模式支持并行处理</li>
	 *     <li>流式处理，适合大文件</li>
	 * </ul>
	 *
	 * @param inputFile  加密文件（必须存在且可读）
	 * @param outputFile 解密输出文件（自动创建父目录）
	 * @param key        解密密钥（16/24/32 字节，与加密时一致）
	 * @param iv         初始化向量（16 字节，与加密时一致）
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输入文件不存在或不可读</li>
	 *                                      <li>输出路径不可写或创建失败</li>
	 *                                      <li>文件 IO 操作失败</li>
	 *                                  </ul>
	 * @throws IllegalArgumentException 当密钥长度不是 16/24/32 字节或 IV 长度不是 16 字节时
	 * @see IOUtils#decryptByCtr(InputStream, OutputStream, byte[], byte[])
	 * @since 1.0.0
	 */
	public static void decryptFileByCtr(final File inputFile, final File outputFile, final byte[] key, final byte[] iv) throws IOException {
		checkFile(inputFile, "inputFile 不可为 null");
		checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (OutputStream outputStream = openOutputStream(outputFile);
			 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream, IOUtils.DEFAULT_BUFFER_SIZE);
			 UnsynchronizedBufferedInputStream bufferedInputStream = openUnsynchronizedBufferedInputStream(inputFile)) {
			IOUtils.decryptByCtr(bufferedInputStream, bufferedOutputStream, key, iv);
		}
	}

	/**
	 * AES/CTR 模式文件解密（自定义缓冲区）
	 * <p>解密所用的密钥与 IV 必须与加密时完全一致。</p>
	 *
	 * @param inputFile  加密文件（必须存在且可读）
	 * @param outputFile 解密输出文件（自动创建父目录）
	 * @param key        解密密钥（16/24/32 字节，与加密时一致）
	 * @param iv         初始化向量（16 字节，与加密时一致）
	 * @param bufferSize 处理缓冲区大小（正数，建议参考 {@link IOUtils#DEFAULT_BUFFER_SIZE}）
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输入文件不存在或不可读</li>
	 *                                      <li>输出路径不可写或创建失败</li>
	 *                                      <li>文件 IO 操作失败</li>
	 *                                  </ul>
	 * @throws IllegalArgumentException 当密钥长度不是 16/24/32 字节或 IV 长度不是 16 字节时
	 * @see IOUtils#decryptByCtr(InputStream, OutputStream, byte[], byte[], int)
	 * @since 1.0.0
	 */
	public static void decryptFileByCtr(final File inputFile, final File outputFile, final byte[] key, final byte[] iv,
										final int bufferSize) throws IOException {
		checkFile(inputFile, "inputFile 不可为 null");
		checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (OutputStream outputStream = openOutputStream(outputFile);
			 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream, bufferSize);
			 UnsynchronizedBufferedInputStream bufferedInputStream = openUnsynchronizedBufferedInputStream(inputFile)) {
			IOUtils.decryptByCtr(bufferedInputStream, bufferedOutputStream, key, iv, bufferSize);
		}
	}

	/**
	 * 强制删除文件或目录（如果存在）
	 * <p><strong>功能特性：</strong></p>
	 * <ul>
	 *     <li>自动清除只读属性，确保删除成功</li>
	 *     <li>支持删除被其他进程锁定的文件（Windows系统）</li>
	 *     <li>递归删除目录及其所有内容（当参数为目录时）</li>
	 *     <li>静默处理不存在的文件</li>
	 * </ul>
	 *
	 * <p><strong>实现原理：</strong></p>
	 * <ol>
	 *     <li>检查文件是否存在</li>
	 *     <li>调用{@link #forceDelete(File)}执行强制删除</li>
	 * </ol>
	 *
	 * @param file 待删除的文件或目录（可为null）
	 * @throws IOException 当文件存在但无法删除时抛出，包括：
	 *                     <ul>
	 *                         <li>文件被系统锁定</li>
	 *                         <li>磁盘I/O错误</li>
	 *                         <li>权限不足</li>
	 *                     </ul>
	 * @see #forceDelete(File)
	 * @since 1.0.0
	 */
	public static void forceDeleteIfExist(final File file) throws IOException {
		if (exist(file)) {
			forceDelete(file);
		}
	}

	/**
	 * 条件删除文件（如果存在）
	 * <p><strong>与{@link #forceDeleteIfExist}的主要区别：</strong></p>
	 * <ul>
	 *     <li>不强制清除只读属性</li>
	 *     <li>不处理被锁定的文件</li>
	 *     <li>不递归删除目录内容</li>
	 *     <li>可能抛出SecurityException而非IOException</li>
	 * </ul>
	 *
	 * <p><strong>适用场景：</strong></p>
	 * <ul>
	 *     <li>需要更严格的权限控制时</li>
	 *     <li>仅需删除单个文件时</li>
	 *     <li>不需要处理特殊锁定状态时</li>
	 * </ul>
	 *
	 * @param file 待删除的文件（可为null）
	 * @throws SecurityException 当安全管理器拒绝删除操作时抛出
	 * @throws IOException       当发生I/O错误时抛出
	 * @see File#delete()
	 * @since 1.0.0
	 */
	public static void deleteIfExist(final File file) throws IOException {
		if (exist(file)) {
			delete(file);
		}
	}

	/**
	 * 检查文件或目录是否存在
	 * <p><strong>特性说明：</strong></p>
	 * <ul>
	 *     <li>安全处理null输入，返回false</li>
	 *     <li>同时适用于文件和目录检查</li>
	 *     <li>底层调用{@link File#exists()}方法</li>
	 * </ul>
	 *
	 * @param file 待检查的文件对象（可为null）
	 * @return 当文件非null且存在时返回true
	 * @see File#exists()
	 * @since 1.0.0
	 */
	public static boolean exist(final File file) {
		return Objects.nonNull(file) && file.exists();
	}

	/**
	 * 检查文件或目录是否不存在
	 * <p><strong>与{@link #exist}的关系：</strong></p>
	 * <ul>
	 *     <li>逻辑上等价于!exist(file)</li>
	 *     <li>提供更语义化的方法名</li>
	 *     <li>同样安全处理null输入</li>
	 * </ul>
	 *
	 * @param file 待检查的文件对象（可为null）
	 * @return 当文件为null或不存在时返回true
	 * @see #exist(File)
	 * @since 1.0.0
	 */
	public static boolean notExist(final File file) {
		return !exist(file);
	}

	/**
	 * 检查常规文件是否存在
	 * <p><strong>功能特性：</strong></p>
	 * <ul>
	 *     <li>严格验证文件类型为常规文件（非目录）</li>
	 *     <li>安全处理null输入，返回false</li>
	 *     <li>底层调用{@link File#isFile()}方法</li>
	 * </ul>
	 *
	 * <p><strong>与{@link #exist}的区别：</strong></p>
	 * <ol>
	 *     <li>额外验证文件类型为常规文件</li>
	 *     <li>排除目录</li>
	 *     <li>适用于需要严格文件验证的场景</li>
	 * </ol>
	 *
	 * @param file 待检查的文件对象（可为null）
	 * @return 当文件非null、存在且是常规文件时返回true
	 * @see File#isFile()
	 * @see #exist(File)
	 * @since 1.0.0
	 */
	public static boolean existFile(final File file) {
		return Objects.nonNull(file) && file.exists() && file.isFile();
	}

	/**
	 * 检查常规文件是否不存在
	 * <p><strong>功能特性：</strong></p>
	 * <ul>
	 *     <li>逻辑上等价于!existFile(file)</li>
	 *     <li>提供更语义化的方法名</li>
	 *     <li>同样安全处理null输入</li>
	 * </ul>
	 *
	 * <p><strong>适用场景：</strong></p>
	 * <ol>
	 *     <li>需要创建新文件前的验证</li>
	 *     <li>避免覆盖现有文件</li>
	 *     <li>排除目录存在的干扰</li>
	 *     <li>文件操作前的安全检查</li>
	 * </ol>
	 *
	 * @param file 待检查的文件对象（可为null）
	 * @return 当文件为null、不存在或不是常规文件时返回true
	 * @see #existFile(File)
	 * @since 1.0.0
	 */
	public static boolean notExistFile(final File file) {
		return !existFile(file);
	}

	/**
	 * 解析文件内容元数据
	 * <p><strong>功能特性：</strong></p>
	 * <ul>
	 *     <li>基于Apache Tika实现，支持1000+种文件格式</li>
	 *     <li>深度解析文件内容而非仅依赖文件头</li>
	 *     <li>自动处理编码和压缩格式</li>
	 * </ul>
	 *
	 * <p><strong>支持格式示例：</strong></p>
	 * <table border="1">
	 *     <tr><th>类型</th><th>示例格式</th><th>可提取元数据</th></tr>
	 *     <tr><td>文档类</td><td>PDF/DOCX/XLSX/PPTX</td><td>作者、页数、创建时间、修改时间</td></tr>
	 *     <tr><td>多媒体</td><td>MP3/MP4/JPEG/PNG</td><td>专辑、时长、分辨率、拍摄参数</td></tr>
	 *     <tr><td>压缩文件</td><td>ZIP/RAR/7Z</td><td>条目数、压缩方法、注释</td></tr>
	 *     <tr><td>其他</td><td>HTML/XML/JSON</td><td>编码、字符集、DOCTYPE</td></tr>
	 * </table>
	 *
	 * <p><strong>性能提示：</strong></p>
	 * <ul>
	 *     <li>大文件解析会有内存和CPU开销</li>
	 *     <li>结果已按元数据类型分组</li>
	 *     <li>建议对结果进行缓存</li>
	 * </ul>
	 *
	 * @param file 目标文件（必须存在且可读）
	 * @return 包含所有元数据的不可修改键值对集合，Key为标准元数据类型（如"Content-Type"、"Author"等）
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>文件格式不支持</li>
	 *                         <li>文件损坏或加密</li>
	 *                         <li>权限不足无法读取</li>
	 *                     </ul>
	 * @see Metadata
	 * @see org.apache.tika.Tika
	 * @since 1.0.0
	 */
	public static Map<String, String> parseMetaData(final File file) throws IOException {
		checkFile(file, "file 不可为 null");
		Metadata metadata = new Metadata();
		try (Reader reader = IOConstants.getDefaultTika().parse(file, metadata)) {
			return Arrays.stream(metadata.names())
				.map(name -> Pair.of(name, metadata.get(name)))
				.collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
		}
	}

	/**
	 * 获取文件真实MIME类型
	 * <p><strong>技术实现：</strong></p>
	 * <ul>
	 *     <li>基于Apache Tika内容检测引擎</li>
	 *     <li>通过文件魔数(Magic Number)识别格式</li>
	 *     <li>支持300+种常见文件格式</li>
	 * </ul>
	 *
	 * <p><strong>与文件扩展名的区别：</strong></p>
	 * <ol>
	 *     <li>不依赖文件扩展名，防止伪造</li>
	 *     <li>能识别无扩展名文件</li>
	 *     <li>可检测被修改扩展名的文件</li>
	 * </ol>
	 *
	 * @param file 目标文件（必须存在且可读）
	 * @return 标准MIME类型字符串（如："application/pdf"），遵循IANA标准
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>文件不存在或不可读</li>
	 *                         <li>文件格式无法识别</li>
	 *                         <li>磁盘I/O错误</li>
	 *                     </ul>
	 * @see org.apache.tika.Tika#detect(File)
	 * @since 1.0.0
	 */
	public static String getMimeType(final File file) throws IOException {
		checkFile(file, "file 不可为 null");
		return IOConstants.getDefaultTika().detect(file);
	}

	/**
	 * 检测是否为图片文件
	 * <p>支持格式：JPEG/PNG/GIF/BMP/WebP等50+种格式</p>
	 *
	 * @param file 待检测文件
	 * @return 当文件内容被识别为图片类型时返回true
	 * @throws IOException 当文件读取失败时抛出
	 * @since 1.0.0
	 */
	public static boolean isImageType(final File file) throws IOException {
		return getMimeType(file).startsWith(IOConstants.IMAGE_MIME_TYPE_PREFIX);
	}

	/**
	 * 检测是否为文本文件
	 * <p>支持格式：TXT/CSV/XML/JSON/Markdown等30+种文本格式</p>
	 *
	 * @param file 待检测文件
	 * @return 当文件内容被识别为文本类型时返回true
	 * @throws IOException 当文件无法读取时抛出
	 * @since 1.0.0
	 */
	public static boolean isTextType(final File file) throws IOException {
		return getMimeType(file).startsWith(IOConstants.TEXT_MIME_TYPE_PREFIX);
	}

	/**
	 * 检测是否为模型文件
	 * <p>支持格式：obj/stl/fbx/glb等模型格式</p>
	 *
	 * @param file 待检测文件
	 * @return 当文件内容被识别为模型类型时返回true
	 * @throws IOException 当文件无法读取时抛出
	 * @since 1.0.0
	 */
	public static boolean isModelType(final File file) throws IOException {
		return getMimeType(file).startsWith(IOConstants.MODEL_MIME_TYPE_PREFIX);
	}

	/**
	 * 检测是否为视频文件
	 * <p>支持格式：MP4/AVI/MOV/MKV等20+种主流视频格式</p>
	 *
	 * @param file 待检测文件
	 * @return 当文件内容被识别为视频类型时返回true
	 * @throws IOException 当文件损坏或无法解析时抛出
	 * @since 1.0.0
	 */
	public static boolean isVideoType(final File file) throws IOException {
		return getMimeType(file).startsWith(IOConstants.VIDEO_MIME_TYPE_PREFIX);
	}

	/**
	 * 检测是否为音频文件
	 * <p>支持格式：MP3/WAV/FLAC/AAC等15+种音频格式</p>
	 *
	 * @param file 待检测文件
	 * @return 当文件内容被识别为音频类型时返回true
	 * @throws IOException 当文件格式不支持时抛出
	 * @since 1.0.0
	 */
	public static boolean isAudioType(final File file) throws IOException {
		return getMimeType(file).startsWith(IOConstants.AUDIO_MIME_TYPE_PREFIX);
	}

	/**
	 * 判断文件是否为指定类型
	 * <p>性能提示：</p>
	 * <ul>
	 *     <li>内部使用Tika进行内容检测</li>
	 *     <li>大文件检测会有性能开销</li>
	 *     <li>结果缓存可提升重复检测效率</li>
	 * </ul>
	 *
	 * @param file     目标文件（必须存在）
	 * @param mimeType 预期MIME类型
	 * @return 类型匹配返回true
	 * @throws IOException 当文件读取失败时抛出
	 * @since 1.0.0
	 */
	public static boolean isMimeType(final File file, final String mimeType) throws IOException {
		if (StringUtils.isBlank(mimeType)) {
			return false;
		}
		return getMimeType(file).equalsIgnoreCase(mimeType);
	}

	/**
	 * 判断文件是否为任一类型
	 * <p><strong>功能特性：</strong></p>
	 * <ul>
	 *     <li>支持可变参数形式传入多个MIME类型</li>
	 *     <li>自动忽略空数组或null元素</li>
	 *     <li>大小写敏感比较（遵循IANA标准）</li>
	 *     <li>基于Tika内容检测，不受文件扩展名影响</li>
	 * </ul>
	 *
	 * <p><strong>性能提示：</strong></p>
	 * <ul>
	 *     <li>内部仅执行一次MIME类型检测</li>
	 *     <li>适合少量固定类型的匹配场景</li>
	 *     <li>如需频繁匹配大量类型，建议使用集合版本</li>
	 * </ul>
	 *
	 * @param file      目标文件（必须存在且可读）
	 * @param mimeTypes 待匹配的MIME类型数组（可空）
	 * @return 当文件MIME类型匹配数组中任意元素时返回true
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>文件不存在或不可读</li>
	 *                         <li>磁盘I/O错误</li>
	 *                     </ul>
	 * @see #getMimeType(File)
	 * @see #isAnyMimeType(File, Collection)
	 * @since 1.0.0
	 */
	public static boolean isAnyMimeType(final File file, final String... mimeTypes) throws IOException {
		if (ArrayUtils.isEmpty(mimeTypes)) {
			return false;
		}
		return StringUtils.equalsAny(getMimeType(file), mimeTypes);
	}

	/**
	 * 判断文件是否为任一类型
	 * <p><strong>功能特性：</strong></p>
	 * <ul>
	 *     <li>支持动态变化的MIME类型集合</li>
	 *     <li>适用于大量预定义类型的匹配场景</li>
	 *     <li>自动处理null和空集合输入</li>
	 *     <li>大小写敏感比较（遵循IANA标准）</li>
	 * </ul>
	 *
	 * <p><strong>与数组版本的区别：</strong></p>
	 * <ol>
	 *     <li>更适合运行时动态变化的类型集合</li>
	 *     <li>避免数组创建开销</li>
	 *     <li>支持更灵活的类型过滤逻辑</li>
	 * </ol>
	 *
	 * @param file      目标文件（必须存在且可读）
	 * @param mimeTypes 待匹配的MIME类型集合（可空）
	 * @return 当文件MIME类型匹配集合中任意元素时返回true
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>文件不存在或不可读</li>
	 *                         <li>磁盘I/O错误</li>
	 *                     </ul>
	 * @see #getMimeType(File)
	 * @see #isAnyMimeType(File, String...)
	 * @since 1.0.0
	 */
	public static boolean isAnyMimeType(final File file, final Collection<String> mimeTypes) throws IOException {
		if (mimeTypes == null || mimeTypes.isEmpty()) {
			return false;
		}
		String fileMimeType = getMimeType(file);
		return mimeTypes.stream().anyMatch(mimeType -> StringUtils.equals(fileMimeType, mimeType));
	}

	/**
	 * 安全重命名文件或目录
	 * <p><strong>功能特性：</strong></p>
	 * <ul>
	 *     <li>支持文件和目录重命名</li>
	 *     <li>自动处理路径分隔符，确保跨平台兼容</li>
	 *     <li>严格校验目标文件是否存在，避免覆盖</li>
	 *     <li>原子性操作保证数据一致性</li>
	 * </ul>
	 *
	 * <p><strong>注意事项：</strong></p>
	 * <ol>
	 *     <li>源文件和目标文件必须在同一文件系统</li>
	 *     <li>对于目录，仅修改目录名而不影响其内容</li>
	 *     <li>Windows系统下可能需要管理员权限</li>
	 * </ol>
	 *
	 * @param file        源文件或目录（必须存在）
	 * @param newFilename 新名称
	 * @return 重命名后的文件对象
	 * @throws FileExistsException 当目标文件已存在时抛出
	 * @throws IOException         当发生以下情况时抛出：
	 *                             <ul>
	 *                                 <li>文件系统权限不足</li>
	 *                                 <li>跨文件系统移动</li>
	 *                                 <li>磁盘I/O错误</li>
	 *                             </ul>
	 * @see File#renameTo(File)
	 * @since 1.0.0
	 */
	public static File rename(final File file, String newFilename) throws IOException {
		check(file, "file 不可为 null");
		if (file.isFile()) {
			newFilename = FilenameUtils.getName(newFilename);
			Validate.notBlank(newFilename, "newFilename 必须为文件名");
		}
		File destFile = new File(file.getParent(), newFilename);
		if (destFile.exists()) {
			throw new FileExistsException(file);
		}
		if (!file.renameTo(destFile)) {
			throw new IOException("重命名源文件 '" + file + "' 失败 '");
		}
		return destFile;
	}

	/**
	 * 替换文件基名（保留扩展名）
	 * <p>功能特性：</p>
	 * <ul>
	 *     <li>自动保留原始文件扩展名</li>
	 *     <li>支持路径中包含的文件名处理</li>
	 *     <li>严格参数校验确保安全性</li>
	 * </ul>
	 *
	 * <p><b>处理流程：</b></p>
	 * <ol>
	 *     <li>验证输入文件有效性</li>
	 *     <li>提取原始文件扩展名</li>
	 *     <li>构建新文件名路径</li>
	 * </ol>
	 *
	 * @param file        源文件（必须为存在的常规文件）
	 * @param newBaseName 新基名（不允许包含路径分隔符或扩展名分隔符）
	 * @return 包含新基名和原扩展名的文件对象
	 * @throws IllegalArgumentException 当以下情况时抛出：
	 *                                  <ul>
	 *                                    <li>file参数为null</li>
	 *                                    <li>file不存在或不是文件</li>
	 *                                    <li>newBaseName为空或包含非法字符</li>
	 *                                  </ul>
	 * @since 1.0.0
	 */
	public static File replaceBaseName(final File file, final String newBaseName) throws IOException {
		checkFile(file, "file 不可为 null");
		String newFilePath = FilenameUtils.replaceBaseName(file.getAbsolutePath(), newBaseName);
		File destFile = new File(newFilePath);
		if (destFile.exists()) {
			throw new FileExistsException(file);
		}
		if (!file.renameTo(destFile)) {
			throw new IOException("修改源文件 '" + file + "' 文件名失败 '");
		}
		return destFile;
	}

	/**
	 * 替换文件扩展名
	 * <p>与{@link #replaceBaseName}的区别：</p>
	 * <ul>
	 *     <li>保留文件名主体部分</li>
	 *     <li>支持完全移除扩展名</li>
	 *     <li>自动处理扩展名分隔符</li>
	 * </ul>
	 *
	 * @param file         源文件（必须为存在的常规文件）
	 * @param newExtension 新扩展名（可空，表示移除扩展名）
	 * @return 包含原基名和新扩展名的文件对象
	 * @throws IllegalArgumentException 当以下情况时抛出：
	 *                                  <ul>
	 *                                    <li>file参数为null</li>
	 *                                    <li>file不存在或不是文件</li>
	 *                                  </ul>
	 * @since 1.0.0
	 */
	public static File replaceExtension(final File file, final String newExtension) throws IOException {
		checkFile(file, "file 不可为 null");
		String newFilePath = FilenameUtils.replaceExtension(file.getAbsolutePath(), newExtension);
		File destFile = new File(newFilePath);
		if (destFile.exists()) {
			throw new FileExistsException(file);
		}
		if (!file.renameTo(destFile)) {
			throw new IOException("修改源文件 '" + file + "' 拓展名失败 '");
		}
		return destFile;
	}

	/**
	 * 基础文件校验（存在性检查）
	 * <p><strong>功能特性：</strong></p>
	 * <ul>
	 *     <li>严格校验文件非null</li>
	 *     <li>验证文件/目录存在性</li>
	 *     <li>统一异常处理机制</li>
	 * </ul>
	 *
	 * @param file    待校验的文件对象
	 * @param message 校验失败时的异常消息
	 * @throws IOException          当文件不存在时抛出FileNotFoundException
	 * @throws NullPointerException 当file为null时抛出
	 * @see File#exists()
	 * @since 1.0.0
	 */
	public static void check(final File file, final String message) throws IOException {
		Objects.requireNonNull(file, message);
		if (!file.exists()) {
			throw new FileNotFoundException(file.getAbsolutePath());
		}
	}

	/**
	 * 常规文件校验（类型检查）
	 * <p><strong>与{@link #check}的区别：</strong></p>
	 * <ul>
	 *     <li>额外验证文件类型为常规文件</li>
	 *     <li>排除目录类型</li>
	 *     <li>适用于需要严格文件验证的场景</li>
	 * </ul>
	 *
	 * @param file    待校验的文件对象
	 * @param message 校验失败时的异常消息
	 * @throws IOException              当文件不存在时抛出
	 * @throws IllegalArgumentException 当路径指向目录时抛出
	 * @see #check(File, String)
	 * @see File#isFile()
	 * @since 1.0.0
	 */
	public static void checkFile(final File file, final String message) throws IOException {
		check(file, message);
		if (file.isDirectory()) {
			throw new IllegalArgumentException(file.getAbsolutePath() + " 不是一个文件路径");
		}
	}

	/**
	 * 条件文件校验（存在时才验证类型）
	 * <p><strong>功能特性：</strong></p>
	 * <ul>
	 *     <li>仅当文件存在时才进行类型校验</li>
	 *     <li>安全处理null输入</li>
	 *     <li>适用于创建新文件前的校验场景</li>
	 * </ul>
	 *
	 * @param file    待校验的文件对象
	 * @param message 校验失败时的异常消息
	 * @throws NullPointerException     当file为null时抛出
	 * @throws IllegalArgumentException 当路径存在且指向目录时抛出
	 * @see #checkFile(File, String)
	 * @since 1.0.0
	 */
	public static void checkFileIfExist(final File file, final String message) {
		if (Objects.isNull(file)) {
			throw new NullPointerException(message);
		}
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IllegalArgumentException(file.getAbsolutePath() + " 不是一个文件路径");
			}
		}
	}
}