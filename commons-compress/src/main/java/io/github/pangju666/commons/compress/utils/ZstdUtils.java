/*
 *   Copyright 2026 pangju666
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

package io.github.pangju666.commons.compress.utils;

import io.github.pangju666.commons.compress.io.resource.ZstdResource;
import io.github.pangju666.commons.io.resource.IOResource;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.IOUtils;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorInputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdConstants;
import org.apache.commons.io.input.UnsynchronizedBufferedInputStream;
import org.apache.commons.lang3.Validate;

import java.io.*;

/**
 * Zstandard (Zstd) 压缩/解压工具类。
 * <p>面向单文件或流数据的压缩格式（不包含归档目录结构）。基于 Apache Commons Compress 的 {@link ZstdCompressorInputStream} 与 {@link ZstdCompressorOutputStream} 实现。</p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li>单文件/流式压缩：适用于对单个文件或输入流进行压缩，输出为 {@code .zst}。</li>
 *   <li>多输入与输出：支持 {@link File} 与 {@link InputStream} 输入，输出到 {@link OutputStream} 或 {@link File}。</li>
 *   <li>格式校验：通过 Tika 进行 MIME 类型检测；文件/字节数组版本在调用前校验，输入流版本不预校验。</li>
 *   <li>性能优化：广泛使用缓冲与 {@link InputStream#transferTo(OutputStream)}。</li>
 *   <li>资源管理：采用 try-with-resources 自动释放内部创建的包装流。</li>
 * </ul>
 *
 * <h3>线程安全</h3>
 * <p>类无共享状态，方法均为静态；并发处理不同文件/流是安全的。对同一路径或同一输出目标并发写入可能产生冲突。</p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 压缩文件到 .zst
 * ZstdUtils.compress(new File("input.txt"), new File("input.txt.zst"));
 *
 * // 压缩输入流到输出流（当传入已构造的 ZstdCompressorOutputStream 时不关闭该对象）
 * try (InputStream in = new FileInputStream("input.txt");
 *      OutputStream out = new FileOutputStream("output.zst")) {
 *     ZstdUtils.compress(in, out);
 * }
 *
 * // 解压 .zst 文件到普通文件
 * ZstdUtils.uncompress(new File("input.txt.zst"), new File("output.txt"));
 *
 * // 解压 .zst 文件到输出流
 * try (OutputStream out = new FileOutputStream("output.txt")) {
 *     ZstdUtils.uncompress(new File("input.txt.zst"), out);
 * }
 *
 * // 使用 ZstdResource 解压
 * try (ZstdResource resource = new ZstdResource(new File("data.zst"))) {
 *     ZstdUtils.uncompress(resource, new File("output.txt"));
 * }
 * }</pre>
 *
 * @author pangju666
 * @see ZstdCompressorInputStream
 * @see ZstdCompressorOutputStream
 * @see ZstdResource
 * @since 1.1.0
 */
public class ZstdUtils {
	/**
	 * 受保护的构造函数，防止实例化。
	 */
	protected ZstdUtils() {
	}

	/**
	 * 压缩输入流到输出流。
	 * <p>将输入流的数据压缩为 Zstd 格式并写入输出流。如果传入的输出流是 {@code ZstdCompressorOutputStream}，将直接使用它；否则会创建新的 {@code ZstdCompressorOutputStream}。</p>
	 * <p>方法会自动处理输入流的缓冲，如果输入流已经是缓冲流则直接使用，否则会创建缓冲流。</p>
	 * <p>使用默认压缩级别 {@link ZstdConstants#ZSTD_CLEVEL_DEFAULT}。</p>
	 *
	 * @param inputStream  输入流，必须非 null
	 * @param outputStream 输出流，必须非 null（如果传入 {@code ZstdCompressorOutputStream} 则不会关闭它）
	 * @throws NullPointerException 当 {@code inputStream} 或 {@code outputStream} 为 null 时抛出
	 * @throws IOException          当压缩过程中发生 I/O 错误时抛出
	 * @since 1.1.0
	 */
	public static void compress(final InputStream inputStream, final OutputStream outputStream) throws IOException {
		compress(inputStream, outputStream, ZstdConstants.ZSTD_CLEVEL_DEFAULT);
	}

	/**
	 * 压缩输入流到输出流（指定压缩级别）。
	 * <p>将输入流的数据压缩为 Zstd 格式并写入输出流。如果传入的输出流是 {@code ZstdCompressorOutputStream}，将直接使用它；否则会创建新的 {@code ZstdCompressorOutputStream}。</p>
	 * <p>方法会自动处理输入流的缓冲，如果输入流已经是缓冲流则直接使用，否则会创建缓冲流。</p>
	 *
	 * @param inputStream  输入流，必须非 null
	 * @param outputStream 输出流，必须非 null（如果传入 {@code ZstdCompressorOutputStream} 则不会关闭它）
	 * @param level        压缩级别，范围为 {@link ZstdConstants#ZSTD_CLEVEL_MIN} 到 {@link ZstdConstants#ZSTD_CLEVEL_MAX}
	 * @throws NullPointerException 当 {@code inputStream} 或 {@code outputStream} 为 null 时抛出
	 * @throws IOException          当压缩过程中发生 I/O 错误时抛出
	 * @since 1.1.0
	 */
	public static void compress(final InputStream inputStream, final OutputStream outputStream, final int level) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");

		if (outputStream instanceof ZstdCompressorOutputStream) {
			if (inputStream instanceof BufferedInputStream || inputStream instanceof UnsynchronizedBufferedInputStream) {
				inputStream.transferTo(outputStream);
			} else {
				try (InputStream bufferedInputStream = IOUtils.unsynchronizedBuffer(inputStream)) {
					bufferedInputStream.transferTo(outputStream);
				}
			}
		} else {
			try (BufferedOutputStream bufferedOutputStream = IOUtils.buffer(outputStream);
			     ZstdCompressorOutputStream compressorOutputStream = ZstdCompressorOutputStream.builder()
					 .setOutputStream(bufferedOutputStream)
					 .setLevel(level)
					 .get()) {
				if (inputStream instanceof BufferedInputStream || inputStream instanceof UnsynchronizedBufferedInputStream) {
					inputStream.transferTo(compressorOutputStream);
				} else {
					try (InputStream bufferedInputStream = IOUtils.unsynchronizedBuffer(inputStream)) {
						bufferedInputStream.transferTo(compressorOutputStream);
					}
				}
			}
		}
	}

	/**
	 * 压缩 IOResource 到输出流。
	 * <p>从 IOResource 读取数据并压缩为 Zstd 格式写入输出流。方法会自动关闭资源打开的输入流和创建的输出流。</p>
	 * <p>使用默认压缩级别 {@link ZstdConstants#ZSTD_CLEVEL_DEFAULT}。</p>
	 *
	 * @param resource     IOResource 对象，必须非 null
	 * @param outputStream 输出流，必须非 null
	 * @throws NullPointerException 当 {@code resource} 或 {@code outputStream} 为 null 时抛出
	 * @throws IOException          当读取资源或压缩过程中发生 I/O 错误时抛出
	 * @since 1.1.0
	 */
	public static void compress(final IOResource resource, final OutputStream outputStream) throws IOException {
		compress(resource, outputStream, ZstdConstants.ZSTD_CLEVEL_DEFAULT);
	}

	/**
	 * 压缩 IOResource 到输出流（指定压缩级别）。
	 * <p>从 IOResource 读取数据并压缩为 Zstd 格式写入输出流。方法会自动关闭资源打开的输入流和创建的输出流。</p>
	 *
	 * @param resource     IOResource 对象，必须非 null
	 * @param outputStream 输出流，必须非 null
	 * @param level        压缩级别，范围为 {@link ZstdConstants#ZSTD_CLEVEL_MIN} 到 {@link ZstdConstants#ZSTD_CLEVEL_MAX}
	 * @throws NullPointerException 当 {@code resource} 或 {@code outputStream} 为 null 时抛出
	 * @throws IOException          当读取资源或压缩过程中发生 I/O 错误时抛出
	 * @since 1.1.0
	 */
	public static void compress(final IOResource resource, final OutputStream outputStream, final int level) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (InputStream inputStream = resource.newBufferedInputStream()) {
			compress(inputStream, outputStream, level);
		}
	}

	/**
	 * 压缩输入流到文件。
	 * <p>将输入流的数据压缩为 Zstd 格式并写入指定文件。会自动创建父目录并覆盖已存在文件。</p>
	 * <p>使用默认压缩级别 {@link ZstdConstants#ZSTD_CLEVEL_DEFAULT}。</p>
	 *
	 * @param inputStream 输入流，必须非 null
	 * @param outputFile  输出文件，必须非 null
	 * @throws NullPointerException 当 {@code inputStream} 或 {@code outputFile} 为 null 时抛出
	 * @throws IOException          当文件写入或压缩过程中发生 I/O 错误时抛出
	 * @since 1.1.0
	 */
	public static void compress(final InputStream inputStream, final File outputFile) throws IOException {
		compress(inputStream, outputFile, ZstdConstants.ZSTD_CLEVEL_DEFAULT);
	}

	/**
	 * 压缩输入流到文件（指定压缩级别）。
	 * <p>将输入流的数据压缩为 Zstd 格式并写入指定文件。会自动创建父目录并覆盖已存在文件。</p>
	 *
	 * @param inputStream 输入流，必须非 null
	 * @param outputFile  输出文件，必须非 null
	 * @param level       压缩级别，范围为 {@link ZstdConstants#ZSTD_CLEVEL_MIN} 到 {@link ZstdConstants#ZSTD_CLEVEL_MAX}
	 * @throws NullPointerException 当 {@code inputStream} 或 {@code outputFile} 为 null 时抛出
	 * @throws IOException          当文件写入或压缩过程中发生 I/O 错误时抛出
	 * @since 1.1.0
	 */
	public static void compress(final InputStream inputStream, final File outputFile, final int level) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (BufferedOutputStream bufferedOutputStream = FileUtils.newBufferedOutputStream(outputFile)) {
			compress(inputStream, bufferedOutputStream, level);
		}
	}

	/**
	 * 压缩 IOResource 到文件。
	 * <p>从 IOResource 读取数据并压缩为 Zstd 格式写入指定文件。会自动创建父目录并覆盖已存在文件。</p>
	 * <p>使用默认压缩级别 {@link ZstdConstants#ZSTD_CLEVEL_DEFAULT}。</p>
	 *
	 * @param resource   IOResource 对象，必须非 null
	 * @param outputFile 输出文件，必须非 null
	 * @throws NullPointerException 当 {@code resource} 或 {@code outputFile} 为 null 时抛出
	 * @throws IOException          当读取资源或文件写入过程中发生 I/O 错误时抛出
	 * @since 1.1.0
	 */
	public static void compress(final IOResource resource, final File outputFile) throws IOException {
		compress(resource, outputFile, ZstdConstants.ZSTD_CLEVEL_DEFAULT);
	}

	/**
	 * 压缩 IOResource 到文件（指定压缩级别）。
	 * <p>从 IOResource 读取数据并压缩为 Zstd 格式写入指定文件。会自动创建父目录并覆盖已存在文件。</p>
	 *
	 * @param resource   IOResource 对象，必须非 null
	 * @param outputFile 输出文件，必须非 null
	 * @param level      压缩级别，范围为 {@link ZstdConstants#ZSTD_CLEVEL_MIN} 到 {@link ZstdConstants#ZSTD_CLEVEL_MAX}
	 * @throws NullPointerException 当 {@code resource} 或 {@code outputFile} 为 null 时抛出
	 * @throws IOException          当读取资源或文件写入过程中发生 I/O 错误时抛出
	 * @since 1.1.0
	 */
	public static void compress(final IOResource resource, final File outputFile, final int level) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (InputStream inputStream = resource.newBufferedInputStream();
		     BufferedOutputStream bufferedOutputStream = FileUtils.newBufferedOutputStream(outputFile)) {
			compress(inputStream, bufferedOutputStream, level);
		}
	}

	/**
	 * 从 {@code ZstdResource} 解压到输出流。
	 * <p>通过 ZstdResource 打开 Zstd 压缩输入流，将解压后的数据写入输出流。</p>
	 *
	 * @param resource     ZstdResource 对象，必须非 null
	 * @param outputStream 输出流，必须非 null
	 * @throws NullPointerException 当 {@code resource} 或 {@code outputStream} 为 null 时抛出
	 * @throws IOException          当资源已关闭或解压过程中发生 I/O 错误时抛出
	 * @since 1.1.0
	 */
	public static void uncompress(final ZstdResource resource, final OutputStream outputStream) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (ZstdCompressorInputStream zstdCompressorInputStream = resource.openZstdCompressorInputStream();
		     BufferedOutputStream bufferedOutputStream = IOUtils.buffer(outputStream)) {
			zstdCompressorInputStream.transferTo(bufferedOutputStream);
		}
	}

	/**
	 * 从 {@code ZstdResource} 解压到文件。
	 * <p>通过 ZstdResource 打开 Zstd 压缩输入流，将解压后的数据写入指定文件。会自动创建父目录并覆盖已存在文件。</p>
	 *
	 * @param resource   ZstdResource 对象，必须非 null
	 * @param outputFile 输出文件，必须非 null
	 * @throws NullPointerException 当 {@code resource} 或 {@code outputFile} 为 null 时抛出
	 * @throws IOException          当资源已关闭、文件写入或解压过程中发生 I/O 错误时抛出
	 * @since 1.1.0
	 */
	public static void uncompress(final ZstdResource resource, final File outputFile) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (ZstdCompressorInputStream zstdCompressorInputStream = resource.openZstdCompressorInputStream();
		     BufferedOutputStream bufferedOutputStream = FileUtils.newBufferedOutputStream(outputFile)) {
			zstdCompressorInputStream.transferTo(bufferedOutputStream);
		}
	}
}

