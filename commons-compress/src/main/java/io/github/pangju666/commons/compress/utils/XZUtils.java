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

package io.github.pangju666.commons.compress.utils;

import io.github.pangju666.commons.compress.io.resource.XZResource;
import io.github.pangju666.commons.compress.lang.CompressConstants;
import io.github.pangju666.commons.io.lang.IOConstants;
import io.github.pangju666.commons.io.resource.IOResource;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.IOUtils;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import org.apache.commons.io.input.UnsynchronizedBufferedInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.tukaani.xz.LZMA2Options;

import java.io.*;
import java.util.Objects;

/**
 * XZ 压缩/解压工具类。
 * <p>面向单文件或流数据的压缩格式（不包含归档目录结构）。基于 Apache Commons Compress 的 {@link XZCompressorInputStream} 与 {@link XZCompressorOutputStream} 实现。</p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li>单文件/流式压缩：适用于对单个文件或输入流进行压缩，输出为 {@code .xz}。</li>
 *   <li>多输入与输出：支持 {@link java.io.File} 与 {@link java.io.InputStream} 输入，输出到 {@link java.io.OutputStream} 或 {@link java.io.File}。</li>
 *   <li>格式校验：通过 Tika 进行 MIME 类型检测；文件/字节数组版本在调用前校验，输入流版本不预校验。</li>
 *   <li>性能优化：广泛使用缓冲与 {@link java.io.InputStream#transferTo(java.io.OutputStream)}。</li>
 *   <li>资源管理：采用 try-with-resources 自动释放内部创建的包装流。</li>
 * </ul>
 *
 * <h3>线程安全</h3>
 * <p>类无共享状态，方法均为静态；并发处理不同文件/流是安全的。对同一路径或同一输出目标并发写入可能产生冲突。</p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 压缩文件到 .xz
 * XZUtils.compress(new File("input.txt"), new File("input.txt.xz"));
 *
 * // 压缩输入流到输出流（当传入已构造的 XZCompressorOutputStream 时不关闭该对象）
 * try (InputStream in = new FileInputStream("input.txt");
 *      OutputStream out = new FileOutputStream("output.xz")) {
 *     XZUtils.compress(in, out);
 * }
 *
 * // 解压 .xz 文件到普通文件
 * XZUtils.uncompress(new File("input.txt.xz"), new File("output.txt"));
 *
 * // 解压 .xz 文件到输出流
 * try (OutputStream out = new FileOutputStream("output.txt")) {
 *     XZUtils.uncompress(new File("input.txt.xz"), out);
 * }
 *
 * // 使用 XZResource 解压
 * try (XZResource resource = new XZResource(new File("data.xz"))) {
 *     XZUtils.uncompress(resource, new File("output.txt"));
 * }
 * }</pre>
 *
 * @author pangju666
 * @see XZCompressorInputStream
 * @see XZCompressorOutputStream
 * @see XZResource
 * @since 1.0.0
 */
public class XZUtils {
	/**
	 * 受保护的构造函数，防止实例化。
	 */
	protected XZUtils() {
	}

	/**
	 * 检查指定文件是否为 XZ 格式。
	 * <p>基于 Tika 的 MIME 类型检测（{@link CompressConstants#XZ_MIME_TYPE}）。</p>
	 *
	 * @param file 待检测文件，必须存在且可读
	 * @return 当且仅当文件非空且检测为 XZ 时返回 {@code true}
	 * @throws NullPointerException 当 {@code file} 为 {@code null} 时抛出
	 * @throws IOException          当文件访问发生 I/O 异常时抛出
	 * @since 1.0.0
	 * @deprecated 请使用{@link XZResource} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static boolean isXZ(final File file) throws IOException {
		return FileUtils.isMimeType(file, CompressConstants.XZ_MIME_TYPE);
	}

	/**
	 * 检查字节数组内容是否为 XZ 格式。
	 * <p>基于 Tika 的 MIME 类型检测。</p>
	 *
	 * @param bytes 待检测的字节数组；为 {@code null} 或空数组将返回 {@code false}
	 * @return 当且仅当字节数组非空且检测为 XZ 时返回 {@code true}
	 * @since 1.0.0
	 * @deprecated 请使用{@link XZResource} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static boolean isXZ(final byte[] bytes) {
		return ArrayUtils.isNotEmpty(bytes) &&
			IOConstants.getDefaultTika().detect(bytes).equals(CompressConstants.XZ_MIME_TYPE);
	}

	/**
	 * 检查输入流内容是否为 XZ 格式。
	 * <p>基于 Tika 的 MIME 类型检测；不修改流状态。</p>
	 *
	 * @param inputStream 待检测的输入流，非空
	 * @return 当且仅当输入流非空且检测为 XZ 时返回 {@code true}
	 * @throws IOException 当流读取发生 I/O 错误时抛出
	 * @since 1.0.0
	 * @deprecated 请使用{@link XZResource} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static boolean isXZ(final InputStream inputStream) throws IOException {
		return Objects.nonNull(inputStream) &&
			IOConstants.getDefaultTika().detect(inputStream).equals(CompressConstants.XZ_MIME_TYPE);
	}

	/**
	 * 将输入流压缩为 XZ 并写入到输出流。
	 * <p>
	 * - 当 {@code outputStream} 已是 {@link XZCompressorOutputStream} 时，方法不会关闭该对象，仅调用 {@link XZCompressorOutputStream#finish()}。<br>
	 * - 当方法内部创建包装流（如 {@link BufferedOutputStream}、{@link XZCompressorOutputStream}）时，这些包装流会在方法结束时关闭，可能导致底层输出流被关闭。
	 * </p>
	 *
	 * @param inputStream  待压缩的输入流，非空
	 * @param outputStream 目标输出流，非空
	 * @throws NullPointerException 当 {@code inputStream} 或 {@code outputStream} 为 {@code null} 时抛出
	 * @throws IOException          当读取/写入发生 I/O 错误时抛出
	 * @since 1.0.0
	 */
	public static void compress(final InputStream inputStream, final OutputStream outputStream) throws IOException {
		compress(inputStream, outputStream, new LZMA2Options());
	}

	/**
	 * 将输入流压缩为 XZ 并写入到输出流（指定压缩选项）。
	 * <p>
	 * - 当 {@code outputStream} 已是 {@link XZCompressorOutputStream} 时，方法不会关闭该对象，仅调用 {@link XZCompressorOutputStream#finish()}。<br>
	 * - 当方法内部创建包装流（如 {@link BufferedOutputStream}、{@link XZCompressorOutputStream}）时，这些包装流会在方法结束时关闭，可能导致底层输出流被关闭。
	 * </p>
	 *
	 * @param inputStream  待压缩的输入流，非空
	 * @param outputStream 目标输出流，非空
	 * @param options      LZMA2 压缩选项，非空
	 * @throws NullPointerException 当 {@code inputStream}、{@code outputStream} 或 {@code options} 为 {@code null} 时抛出
	 * @throws IOException          当读取/写入发生 I/O 错误时抛出
	 * @since 2.1.0
	 */
	public static void compress(final InputStream inputStream, final OutputStream outputStream, final LZMA2Options options) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		Validate.notNull(options, "options 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");

		if (outputStream instanceof XZCompressorOutputStream compressorOutputStream) {
			if (inputStream instanceof BufferedInputStream || inputStream instanceof UnsynchronizedBufferedInputStream) {
				inputStream.transferTo(compressorOutputStream);
			} else {
				try (InputStream bufferedInputStream = IOUtils.unsynchronizedBuffer(inputStream)) {
					bufferedInputStream.transferTo(compressorOutputStream);
				}
			}
		} else {
			try (BufferedOutputStream bufferedOutputStream = IOUtils.buffer(outputStream);
			     XZCompressorOutputStream compressorOutputStream = XZCompressorOutputStream.builder()
					 .setLzma2Options(options)
					 .setOutputStream(bufferedOutputStream)
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
	 * <p>从 IOResource 读取数据并压缩为 XZ 格式写入输出流。方法会自动关闭资源打开的输入流和创建的输出流。</p>
	 * <p>使用默认 LZMA2 压缩选项。</p>
	 *
	 * @param resource     IOResource 对象，必须非 null
	 * @param outputStream 输出流，必须非 null
	 * @throws NullPointerException 当 {@code resource} 或 {@code outputStream} 为 null 时抛出
	 * @throws IOException          当读取资源或压缩过程中发生 I/O 错误时抛出
	 * @since 2.1.0
	 */
	public static void compress(final IOResource resource, final OutputStream outputStream) throws IOException {
		compress(resource, outputStream, new LZMA2Options());
	}


	/**
	 * 压缩 IOResource 到输出流（指定压缩选项）。
	 * <p>从 IOResource 读取数据并压缩为 XZ 格式写入输出流。方法会自动关闭资源打开的输入流和创建的输出流。</p>
	 *
	 * @param resource     IOResource 对象，必须非 null
	 * @param outputStream 输出流，必须非 null
	 * @param options      LZMA2 压缩选项，非空
	 * @throws NullPointerException 当 {@code resource}、{@code outputStream} 或 {@code options} 为 null 时抛出
	 * @throws IOException          当读取资源或压缩过程中发生 I/O 错误时抛出
	 * @since 2.1.0
	 */
	public static void compress(final IOResource resource, final OutputStream outputStream, final LZMA2Options options) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(options, "options 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (InputStream inputStream = resource.newBufferedInputStream();
		     BufferedOutputStream bufferedOutputStream = IOUtils.buffer(outputStream)) {
			compress(inputStream, bufferedOutputStream, options);
		}
	}

	/**
	 * 将文件内容压缩为 XZ 并写入到输出流。
	 * <p>
	 * - 当 {@code outputStream} 已是 {@link XZCompressorOutputStream} 时，方法不会关闭该对象，仅调用 {@link XZCompressorOutputStream#finish()}。<br>
	 * - 当方法内部创建包装流（如 {@link BufferedOutputStream}、{@link XZCompressorOutputStream}）时，这些包装流会在方法结束时关闭，可能导致底层输出流被关闭。
	 * </p>
	 *
	 * @param inputFile    待压缩的文件，必须存在且可读
	 * @param outputStream 目标输出流，非空
	 * @throws NullPointerException  当 {@code inputFile} 或 {@code outputStream} 为 {@code null} 时抛出
	 * @throws FileNotFoundException 当 {@code inputFile} 不存在时抛出
	 * @throws IOException           当读取/写入发生 I/O 错误时抛出
	 * @since 1.0.0
	 * @deprecated 请使用{@link #compress(IOResource, OutputStream)} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static void compress(File inputFile, OutputStream outputStream) throws IOException {
		FileUtils.checkFile(inputFile, "inputFile 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");

		if (outputStream instanceof XZCompressorOutputStream compressorOutputStream) {
			try (InputStream bufferedInputStream = FileUtils.openBufferedFileChannelInputStream(inputFile)) {
				bufferedInputStream.transferTo(compressorOutputStream);
			}
		} else if (outputStream instanceof BufferedOutputStream) {
			try (XZCompressorOutputStream compressorOutputStream = new XZCompressorOutputStream(outputStream);
			     InputStream bufferedInputStream = FileUtils.openBufferedFileChannelInputStream(inputFile)) {
				bufferedInputStream.transferTo(compressorOutputStream);
			}
		} else {
			try (OutputStream bufferedOutputStream = IOUtils.buffer(outputStream);
			     XZCompressorOutputStream compressorOutputStream = new XZCompressorOutputStream(bufferedOutputStream);
			     InputStream bufferedInputStream = FileUtils.openBufferedFileChannelInputStream(inputFile)) {
				bufferedInputStream.transferTo(compressorOutputStream);
			}
		}
	}

	/**
	 * 将文件内容压缩为 XZ 并写入到目标文件。
	 * <p>自动创建父目录；输出文件若已存在将被覆盖。</p>
	 *
	 * @param inputFile  待压缩的文件，必须存在且可读
	 * @param outputFile 目标 {@code .xz} 文件，非空
	 * @throws NullPointerException  当 {@code inputFile} 或 {@code outputFile} 为 {@code null} 时抛出
	 * @throws FileNotFoundException 当 {@code inputFile} 不存在时抛出
	 * @throws IOException           当读取/写入发生 I/O 错误时抛出
	 * @since 1.0.0
	 * @deprecated 请使用{@link #compress(IOResource, OutputStream)} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static void compress(File inputFile, File outputFile) throws IOException {
		FileUtils.checkFile(inputFile, "inputFile 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (BufferedOutputStream bufferedOutputStream = FileUtils.newBufferedOutputStream(outputFile);
		     XZCompressorOutputStream compressorOutputStream = new XZCompressorOutputStream(bufferedOutputStream);
		     InputStream bufferedInputStream = FileUtils.openBufferedFileChannelInputStream(inputFile)) {
			bufferedInputStream.transferTo(compressorOutputStream);
		}
	}

	/**
	 * 压缩输入流到文件。
	 * <p>将输入流的数据压缩为 XZ 格式并写入指定文件。会自动创建父目录并覆盖已存在文件。</p>
	 * <p>使用默认 LZMA2 压缩选项。</p>
	 *
	 * @param inputStream 输入流，必须非 null
	 * @param outputFile  输出文件，必须非 null
	 * @throws NullPointerException 当 {@code inputStream} 或 {@code outputFile} 为 null 时抛出
	 * @throws IOException          当文件写入或压缩过程中发生 I/O 错误时抛出
	 * @since 2.1.0
	 */
	public static void compress(final InputStream inputStream, final File outputFile) throws IOException {
		compress(inputStream, outputFile, new LZMA2Options());
	}

	/**
	 * 压缩输入流到文件（指定压缩选项）。
	 * <p>将输入流的数据压缩为 XZ 格式并写入指定文件。会自动创建父目录并覆盖已存在文件。</p>
	 *
	 * @param inputStream 输入流，必须非 null
	 * @param outputFile  输出文件，必须非 null
	 * @param options     LZMA2 压缩选项，非空
	 * @throws NullPointerException 当 {@code inputStream}、{@code outputFile} 或 {@code options} 为 null 时抛出
	 * @throws IOException          当文件写入或压缩过程中发生 I/O 错误时抛出
	 * @since 2.1.0
	 */
	public static void compress(final InputStream inputStream, final File outputFile, final LZMA2Options options) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		Validate.notNull(options, "options 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (BufferedOutputStream bufferedOutputStream = FileUtils.newBufferedOutputStream(outputFile)) {
			compress(inputStream, bufferedOutputStream, options);
		}
	}

	/**
	 * 压缩 IOResource 到文件。
	 * <p>从 IOResource 读取数据并压缩为 XZ 格式写入指定文件。会自动创建父目录并覆盖已存在文件。</p>
	 * <p>使用默认 LZMA2 压缩选项。</p>
	 *
	 * @param resource   IOResource 对象，必须非 null
	 * @param outputFile 输出文件，必须非 null
	 * @throws NullPointerException 当 {@code resource} 或 {@code outputFile} 为 null 时抛出
	 * @throws IOException          当读取资源或文件写入过程中发生 I/O 错误时抛出
	 * @since 2.1.0
	 */
	public static void compress(final IOResource resource, final File outputFile) throws IOException {
		compress(resource, outputFile, new LZMA2Options());
	}

	/**
	 * 压缩 IOResource 到文件（指定压缩选项）。
	 * <p>从 IOResource 读取数据并压缩为 XZ 格式写入指定文件。会自动创建父目录并覆盖已存在文件。</p>
	 *
	 * @param resource   IOResource 对象，必须非 null
	 * @param outputFile 输出文件，必须非 null
	 * @param options    LZMA2 压缩选项，非空
	 * @throws NullPointerException 当 {@code resource}、{@code outputFile} 或 {@code options} 为 null 时抛出
	 * @throws IOException          当读取资源或文件写入过程中发生 I/O 错误时抛出
	 * @since 2.1.0
	 */
	public static void compress(final IOResource resource, final File outputFile, final LZMA2Options options) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(options, "options 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (InputStream inputStream = resource.newBufferedInputStream();
		     BufferedOutputStream bufferedOutputStream = FileUtils.newBufferedOutputStream(outputFile)) {
			compress(inputStream, bufferedOutputStream, options);
		}
	}

	/**
	 * 从 {@code XZResource} 解压到输出流。
	 * <p>通过 XZResource 打开 XZ 压缩输入流，将解压后的数据写入输出流。</p>
	 *
	 * @param resource     XZResource 对象，必须非 null
	 * @param outputStream 输出流，必须非 null
	 * @throws NullPointerException 当 {@code resource} 或 {@code outputStream} 为 null 时抛出
	 * @throws IOException          当资源已关闭或解压过程中发生 I/O 错误时抛出
	 * @since 2.1.0
	 */
	public static void uncompress(final XZResource resource, final OutputStream outputStream) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (XZCompressorInputStream xzCompressorInputStream = resource.openXZCompressorInputStream();
		     BufferedOutputStream bufferedOutputStream = IOUtils.buffer(outputStream)) {
			xzCompressorInputStream.transferTo(bufferedOutputStream);
		}
	}

	/**
	 * 从 {@code XZResource} 解压到文件。
	 * <p>通过 XZResource 打开 XZ 压缩输入流，将解压后的数据写入指定文件。会自动创建父目录并覆盖已存在文件。</p>
	 *
	 * @param resource   XZResource 对象，必须非 null
	 * @param outputFile 输出文件，必须非 null
	 * @throws NullPointerException 当 {@code resource} 或 {@code outputFile} 为 null 时抛出
	 * @throws IOException          当资源已关闭、文件写入或解压过程中发生 I/O 错误时抛出
	 * @since 2.1.0
	 */
	public static void uncompress(final XZResource resource, final File outputFile) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (XZCompressorInputStream xzCompressorInputStream = resource.openXZCompressorInputStream();
		     BufferedOutputStream bufferedOutputStream = FileUtils.newBufferedOutputStream(outputFile)) {
			xzCompressorInputStream.transferTo(bufferedOutputStream);
		}
	}

	/**
	 * 从输入流解压 XZ 内容到输出流。
	 * <p>
	 * - 当 {@code inputStream} 已是 {@link XZCompressorInputStream} 时，直接读取并写入。<br>
	 * - 当方法内部创建包装流（如 {@link BufferedOutputStream}、{@link XZCompressorInputStream}）时，这些包装流会在方法结束时关闭，可能导致底层输出流被关闭。
	 * </p>
	 *
	 * @param inputStream  XZ 输入流或普通输入流，非空
	 * @param outputStream 目标输出流，非空
	 * @throws NullPointerException 当 {@code inputStream} 或 {@code outputStream} 为 {@code null} 时抛出
	 * @throws IOException          当读取/写入发生 I/O 错误时抛出
	 * @since 1.0.0
	 * @deprecated 请使用{@link #uncompress(XZResource, OutputStream)} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static void uncompress(InputStream inputStream, OutputStream outputStream) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");

		if (inputStream instanceof XZCompressorInputStream) {
			if (outputStream instanceof BufferedOutputStream || outputStream instanceof ByteArrayOutputStream) {
				inputStream.transferTo(outputStream);
			} else {
				try (BufferedOutputStream bufferedOutputStream = IOUtils.buffer(outputStream)) {
					inputStream.transferTo(bufferedOutputStream);
				}
			}
		} else if (inputStream instanceof BufferedInputStream || inputStream instanceof UnsynchronizedBufferedInputStream) {
			try (XZCompressorInputStream compressorInputStream = new XZCompressorInputStream(inputStream)) {
				if (outputStream instanceof BufferedOutputStream || outputStream instanceof ByteArrayOutputStream) {
					compressorInputStream.transferTo(outputStream);
				} else {
					try (BufferedOutputStream bufferedOutputStream = IOUtils.buffer(outputStream)) {
						compressorInputStream.transferTo(bufferedOutputStream);
					}
				}
			}
		} else {
			try (InputStream bufferedInputStream = IOUtils.unsynchronizedBuffer(inputStream);
			     XZCompressorInputStream compressorInputStream = new XZCompressorInputStream(bufferedInputStream)) {
				if (outputStream instanceof BufferedOutputStream || outputStream instanceof ByteArrayOutputStream) {
					compressorInputStream.transferTo(outputStream);
				} else {
					try (BufferedOutputStream bufferedOutputStream = IOUtils.buffer(outputStream)) {
						compressorInputStream.transferTo(bufferedOutputStream);
					}
				}
			}
		}
	}

	/**
	 * 解压 XZ 文件到输出流。
	 * <p>输入文件将进行 MIME 类型校验；方法可能关闭内部创建的包装流。</p>
	 *
	 * @param inputFile    XZ 文件，必须为有效 XZ 格式
	 * @param outputStream 目标输出流，非空
	 * @throws NullPointerException     当 {@code inputFile} 或 {@code outputStream} 为 {@code null} 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFile} 不是有效的 XZ 格式时抛出
	 * @throws IOException              当读取/写入发生 I/O 错误时抛出
	 * @since 1.0.0
	 * @deprecated 请使用{@link #uncompress(XZResource, OutputStream)} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static void uncompress(File inputFile, OutputStream outputStream) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.isTrue(isXZ(inputFile), "inputFile 不是xz压缩文件");

		try (InputStream bufferedInputStream = FileUtils.openBufferedFileChannelInputStream(inputFile);
		     XZCompressorInputStream compressorInputStream = new XZCompressorInputStream(bufferedInputStream)) {
			if (outputStream instanceof BufferedOutputStream) {
				compressorInputStream.transferTo(outputStream);
			} else {
				try (BufferedOutputStream bufferedOutputStream = IOUtils.buffer(outputStream)) {
					compressorInputStream.transferTo(bufferedOutputStream);
				}
			}
		}
	}

	/**
	 * 解压 XZ 文件到目标文件。
	 * <p>自动创建父目录；输出文件若已存在将被覆盖。</p>
	 *
	 * @param inputFile  XZ 输入文件，必须为有效 XZ 格式
	 * @param outputFile 普通输出文件，非空
	 * @throws NullPointerException     当 {@code inputFile} 或 {@code outputFile} 为 {@code null} 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFile} 不是有效的 XZ 格式时抛出
	 * @throws IOException              当读取/写入发生 I/O 错误时抛出
	 * @since 1.0.0
	 * @deprecated 请使用{@link #uncompress(XZResource, File)} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static void uncompress(File inputFile, File outputFile) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.isTrue(isXZ(inputFile), "inputFile 不是xz压缩文件");
		FileUtils.forceMkdirParent(outputFile);

		try (InputStream bufferedInputStream = FileUtils.openBufferedFileChannelInputStream(inputFile);
		     XZCompressorInputStream compressorInputStream = new XZCompressorInputStream(bufferedInputStream);
		     BufferedOutputStream bufferedOutputStream = FileUtils.newBufferedOutputStream(outputFile)) {
			compressorInputStream.transferTo(bufferedOutputStream);
		}
	}
}
