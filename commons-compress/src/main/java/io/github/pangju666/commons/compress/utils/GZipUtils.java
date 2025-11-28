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

import io.github.pangju666.commons.compress.lang.CompressConstants;
import io.github.pangju666.commons.io.lang.IOConstants;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.IOUtils;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.input.UnsynchronizedBufferedInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

import java.io.*;
import java.util.Objects;

/**
 * GZIP 压缩/解压工具类。
 * <p>面向单文件或流数据的压缩格式（不包含归档目录结构）。基于 Apache Commons Compress 的 {@link GzipCompressorInputStream} 与 {@link GzipCompressorOutputStream} 实现。</p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li>单文件/流式压缩：适用于对单个文件或输入流进行压缩，输出为 {@code .gz}。</li>
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
 * // 压缩文件到 .gz
 * GZipUtils.compress(new File("input.txt"), new File("input.txt.gz"));
 *
 * // 压缩输入流到输出流（当传入已构造的 GzipCompressorOutputStream 时不关闭该对象）
 * try (InputStream in = new FileInputStream("input.txt");
 *      OutputStream out = new FileOutputStream("output.gz")) {
 *     GZipUtils.compress(in, out);
 * }
 *
 * // 解压 .gz 文件到普通文件
 * GZipUtils.uncompress(new File("input.txt.gz"), new File("output.txt"));
 *
 * // 解压 .gz 文件到输出流
 * try (OutputStream out = new FileOutputStream("output.txt")) {
 *     GZipUtils.uncompress(new File("input.txt.gz"), out);
 * }
 *
 * // 格式检测
 * boolean ok = GZipUtils.isGZip(new File("input.txt.gz"));
 * }</pre>
 *
 * @author pangju666
 * @see GzipCompressorInputStream
 * @see GzipCompressorOutputStream
 * @since 1.0.0
 */
public class GZipUtils {
	protected GZipUtils() {
	}

	/**
	 * 检查指定文件是否为 GZIP 格式。
	 * <p>基于 Tika 的 MIME 类型检测（{@link CompressConstants#GZIP_TYPE}）。</p>
	 *
	 * @param file 待检测文件，必须存在且可读
	 * @return 当且仅当文件非空且检测为 GZIP 时返回 {@code true}
	 * @throws NullPointerException 当 {@code file} 为 {@code null} 时抛出
	 * @throws IOException          当文件访问发生 I/O 异常时抛出
	 * @since 1.0.0
	 */
	public static boolean isGZip(final File file) throws IOException {
		return FileUtils.isMimeType(file, CompressConstants.GZIP_TYPE);
	}

	/**
	 * 检查字节数组内容是否为 GZIP 格式。
	 * <p>基于 Tika 的 MIME 类型检测。</p>
	 *
	 * @param bytes 待检测的字节数组；为 {@code null} 或空数组将返回 {@code false}
	 * @return 当且仅当字节数组非空且检测为 GZIP 时返回 {@code true}
	 * @since 1.0.0
	 */
	public static boolean isGZip(final byte[] bytes) {
		return ArrayUtils.isNotEmpty(bytes) &&
			IOConstants.getDefaultTika().detect(bytes).equals(CompressConstants.GZIP_TYPE);
	}

	/**
	 * 检查输入流内容是否为 GZIP 格式。
	 * <p>基于 Tika 的 MIME 类型检测；不修改流状态。</p>
	 *
	 * @param inputStream 待检测的输入流，非空
	 * @return 当且仅当输入流非空且检测为 GZIP 时返回 {@code true}
	 * @throws IOException 当流读取发生 I/O 错误时抛出
	 * @since 1.0.0
	 */
	public static boolean isGZip(final InputStream inputStream) throws IOException {
		return Objects.nonNull(inputStream) &&
			IOConstants.getDefaultTika().detect(inputStream).equals(CompressConstants.GZIP_TYPE);
	}

	/**
	 * 将输入流压缩为 GZIP 并写入到输出流。
	 * <p>
	 * - 当 {@code outputStream} 已是 {@link GzipCompressorOutputStream} 时，方法不会关闭该对象，仅调用 {@link GzipCompressorOutputStream#finish()}。<br>
	 * - 当方法内部创建包装流（如 {@link BufferedOutputStream}、{@link GzipCompressorOutputStream}）时，这些包装流会在方法结束时关闭，可能导致底层输出流被关闭。
	 * </p>
	 *
	 * @param inputStream  待压缩的输入流，非空
	 * @param outputStream 目标输出流，非空
	 * @throws NullPointerException 当 {@code inputStream} 或 {@code outputStream} 为 {@code null} 时抛出
	 * @throws IOException          当读取/写入发生 I/O 错误时抛出
	 * @since 1.0.0
	 */
	public static void compress(InputStream inputStream, OutputStream outputStream) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");

		if (outputStream instanceof GzipCompressorOutputStream) {
			GzipCompressorOutputStream compressorOutputStream = (GzipCompressorOutputStream) outputStream;
			if (inputStream instanceof BufferedInputStream ||
				inputStream instanceof UnsynchronizedBufferedInputStream) {
				inputStream.transferTo(compressorOutputStream);
			} else {
				try (InputStream bufferedInputStream = IOUtils.unsynchronizedBuffer(inputStream)) {
					bufferedInputStream.transferTo(compressorOutputStream);
				}
			}
			compressorOutputStream.finish();
		} else if (outputStream instanceof BufferedOutputStream) {
			try (GzipCompressorOutputStream compressorOutputStream = new GzipCompressorOutputStream(outputStream)) {
				if (inputStream instanceof BufferedInputStream ||
					inputStream instanceof UnsynchronizedBufferedInputStream) {
					inputStream.transferTo(compressorOutputStream);
				} else {
					try (InputStream bufferedInputStream = IOUtils.unsynchronizedBuffer(inputStream)) {
						bufferedInputStream.transferTo(compressorOutputStream);
					}
				}
				compressorOutputStream.finish();
			}
		} else {
			try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
				 GzipCompressorOutputStream compressorOutputStream = new GzipCompressorOutputStream(bufferedOutputStream)) {
				if (inputStream instanceof BufferedInputStream ||
					inputStream instanceof UnsynchronizedBufferedInputStream) {
					inputStream.transferTo(compressorOutputStream);
				} else {
					try (InputStream bufferedInputStream = IOUtils.unsynchronizedBuffer(inputStream)) {
						bufferedInputStream.transferTo(compressorOutputStream);
					}
				}
				compressorOutputStream.finish();
			}
		}
	}

	/**
	 * 将文件内容压缩为 GZIP 并写入到输出流。
	 * <p>
	 * - 当 {@code outputStream} 已是 {@link GzipCompressorOutputStream} 时，方法不会关闭该对象，仅调用 {@link GzipCompressorOutputStream#finish()}。<br>
	 * - 当方法内部创建包装流（如 {@link BufferedOutputStream}、{@link GzipCompressorOutputStream}）时，这些包装流会在方法结束时关闭，可能导致底层输出流被关闭。
	 * </p>
	 *
	 * @param inputFile    待压缩的文件，必须存在且可读
	 * @param outputStream 目标输出流，非空
	 * @throws NullPointerException  当 {@code inputFile} 或 {@code outputStream} 为 {@code null} 时抛出
	 * @throws FileNotFoundException 当 {@code inputFile} 不存在时抛出
	 * @throws IOException           当读取/写入发生 I/O 错误时抛出
	 * @since 1.0.0
	 */
	public static void compress(File inputFile, OutputStream outputStream) throws IOException {
		FileUtils.checkFile(inputFile, "inputFile 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");

		if (outputStream instanceof GzipCompressorOutputStream) {
			GzipCompressorOutputStream compressorOutputStream = (GzipCompressorOutputStream) outputStream;
			try (InputStream bufferedInputStream = FileUtils.openUnsynchronizedBufferedInputStream(inputFile)) {
				bufferedInputStream.transferTo(compressorOutputStream);
			}
			compressorOutputStream.finish();
		} else if (outputStream instanceof BufferedOutputStream) {
			try (GzipCompressorOutputStream compressorOutputStream = new GzipCompressorOutputStream(outputStream);
				 InputStream bufferedInputStream = FileUtils.openUnsynchronizedBufferedInputStream(inputFile)) {
				bufferedInputStream.transferTo(compressorOutputStream);
				compressorOutputStream.finish();
			}
		} else {
			try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
				 GzipCompressorOutputStream compressorOutputStream = new GzipCompressorOutputStream(bufferedOutputStream);
				 InputStream bufferedInputStream = FileUtils.openUnsynchronizedBufferedInputStream(inputFile)) {
				bufferedInputStream.transferTo(compressorOutputStream);
				compressorOutputStream.finish();
			}
		}
	}

	/**
	 * 将文件内容压缩为 GZIP 并写入到目标文件。
	 * <p>自动创建父目录；输出文件若已存在将被覆盖。</p>
	 *
	 * @param inputFile  待压缩的文件，必须存在且可读
	 * @param outputFile 目标 {@code .gz} 文件，非空
	 * @throws NullPointerException  当 {@code inputFile} 或 {@code outputFile} 为 {@code null} 时抛出
	 * @throws FileNotFoundException 当 {@code inputFile} 不存在时抛出
	 * @throws IOException           当读取/写入发生 I/O 错误时抛出
	 * @since 1.0.0
	 */
	public static void compress(File inputFile, File outputFile) throws IOException {
		FileUtils.checkFile(inputFile, "inputFile 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		FileUtils.forceMkdirParent(outputFile);

		try (FileOutputStream outputStream = new FileOutputStream(outputFile);
			 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
			 GzipCompressorOutputStream compressorOutputStream = new GzipCompressorOutputStream(bufferedOutputStream);
			 InputStream bufferedInputStream = FileUtils.openUnsynchronizedBufferedInputStream(inputFile)) {
			bufferedInputStream.transferTo(compressorOutputStream);
			compressorOutputStream.finish();
		}
	}

	/**
	 * 从输入流解压 GZIP 内容到输出流。
	 * <p>
	 * - 当 {@code inputStream} 已是 {@link GzipCompressorInputStream} 时，直接读取并写入。<br>
	 * - 当方法内部创建包装流（如 {@link BufferedOutputStream}、{@link GzipCompressorInputStream}）时，这些包装流会在方法结束时关闭，可能导致底层输出流被关闭。
	 * </p>
	 *
	 * @param inputStream  GZIP 输入流或普通输入流，非空
	 * @param outputStream 目标输出流，非空
	 * @throws NullPointerException 当 {@code inputStream} 或 {@code outputStream} 为 {@code null} 时抛出
	 * @throws IOException          当读取/写入发生 I/O 错误时抛出
	 * @since 1.0.0
	 */
	public static void uncompress(InputStream inputStream, OutputStream outputStream) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");

		if (inputStream instanceof GzipCompressorInputStream) {
			GzipCompressorInputStream compressorInputStream = (GzipCompressorInputStream) inputStream;
			if (outputStream instanceof BufferedOutputStream) {
				BufferedOutputStream bufferedOutputStream = (BufferedOutputStream) outputStream;
				compressorInputStream.transferTo(bufferedOutputStream);
			} else {
				try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);) {
					compressorInputStream.transferTo(bufferedOutputStream);
				}
			}
		} else if (inputStream instanceof BufferedInputStream || inputStream instanceof UnsynchronizedBufferedInputStream) {
			try (GzipCompressorInputStream compressorInputStream = new GzipCompressorInputStream(inputStream)) {
				if (outputStream instanceof BufferedOutputStream) {
					BufferedOutputStream bufferedOutputStream = (BufferedOutputStream) outputStream;
					compressorInputStream.transferTo(bufferedOutputStream);
				} else {
					try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);) {
						compressorInputStream.transferTo(bufferedOutputStream);
					}
				}
			}
		} else {
			try (InputStream bufferedInputStream = IOUtils.unsynchronizedBuffer(inputStream);
				 GzipCompressorInputStream compressorInputStream = new GzipCompressorInputStream(bufferedInputStream)) {
				if (outputStream instanceof BufferedOutputStream) {
					BufferedOutputStream bufferedOutputStream = (BufferedOutputStream) outputStream;
					compressorInputStream.transferTo(bufferedOutputStream);
				} else {
					try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);) {
						compressorInputStream.transferTo(bufferedOutputStream);
					}
				}
			}
		}
	}

	/**
	 * 解压 GZIP 文件到输出流。
	 * <p>输入文件将进行 MIME 类型校验；方法可能关闭内部创建的包装流。</p>
	 *
	 * @param inputFile    GZIP 文件，必须为有效 GZIP 格式
	 * @param outputStream 目标输出流，非空
	 * @throws NullPointerException     当 {@code inputFile} 或 {@code outputStream} 为 {@code null} 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFile} 不是有效的 GZIP 格式时抛出
	 * @throws IOException              当读取/写入发生 I/O 错误时抛出
	 * @since 1.0.0
	 */
	public static void uncompress(File inputFile, OutputStream outputStream) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		checkInputFile(inputFile);

		try (InputStream bufferedInputStream = FileUtils.openUnsynchronizedBufferedInputStream(inputFile);
			 GzipCompressorInputStream compressorInputStream = new GzipCompressorInputStream(bufferedInputStream)) {
			if (outputStream instanceof BufferedOutputStream) {
				BufferedOutputStream bufferedOutputStream = (BufferedOutputStream) outputStream;
				compressorInputStream.transferTo(bufferedOutputStream);
			} else {
				try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);) {
					compressorInputStream.transferTo(bufferedOutputStream);
				}
			}
		}
	}

	/**
	 * 解压 GZIP 文件到目标文件。
	 * <p>自动创建父目录；输出文件若已存在将被覆盖。</p>
	 *
	 * @param inputFile  GZIP 输入文件，必须为有效 GZIP 格式
	 * @param outputFile 普通输出文件，非空
	 * @throws NullPointerException     当 {@code inputFile} 或 {@code outputFile} 为 {@code null} 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFile} 不是有效的 GZIP 格式时抛出
	 * @throws IOException              当读取/写入发生 I/O 错误时抛出
	 * @since 1.0.0
	 */
	public static void uncompress(File inputFile, File outputFile) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		checkInputFile(inputFile);
		FileUtils.forceMkdirParent(outputFile);

		try (InputStream bufferedInputStream = FileUtils.openUnsynchronizedBufferedInputStream(inputFile);
			 GzipCompressorInputStream compressorInputStream = new GzipCompressorInputStream(bufferedInputStream);
			 OutputStream outputStream = FileUtils.openOutputStream(outputFile);
			 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)) {
			compressorInputStream.transferTo(bufferedOutputStream);
		}
	}

	/**
	 * 校验输入文件是否存在且为 GZIP 格式。
	 * <p>通过 Tika 的 MIME 类型检测；当格式不匹配时抛出 {@link IllegalArgumentException}。</p>
	 *
	 * @param inputFile 待校验的输入文件，非空
	 * @throws NullPointerException     当 {@code inputFile} 为 {@code null} 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFile} 不是有效的 GZIP 格式时抛出
	 * @throws IOException              当文件访问发生 I/O 异常时抛出
	 * @since 1.0.0
	 */
	protected static void checkInputFile(File inputFile) throws IOException {
		FileUtils.checkFile(inputFile, "inputFile 不可为 null");
		if (!FileUtils.isMimeType(inputFile, CompressConstants.GZIP_TYPE)) {
			throw new IllegalArgumentException(inputFile.getAbsolutePath() + "不是gz类型文件");
		}
	}
}
