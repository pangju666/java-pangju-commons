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

import io.github.pangju666.commons.compress.io.resource.ZipResource;
import io.github.pangju666.commons.compress.lang.CompressConstants;
import io.github.pangju666.commons.io.lang.IOConstants;
import io.github.pangju666.commons.io.model.DataSize;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.IOUtils;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.input.UnsynchronizedBufferedInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.*;
import java.nio.channels.SeekableByteChannel;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.zip.Deflater;

/**
 * ZIP压缩/解压工具类。
 * <p>基于 Apache Commons Compress 和 Zip4j 提供 ZIP 的高效压缩与解压能力。</p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li><strong>多输入源</strong>：支持文件、字节数组、输入流、{@code ZipFile}、{@code ZipResource}。</li>
 *   <li><strong>目录递归</strong>：保持原始目录层级结构进行压缩。</li>
 *   <li><strong>加密支持</strong>：支持创建和解压加密的 ZIP 文件。</li>
 *   <li><strong>性能优化</strong>：广泛使用缓冲流与流式传输，适合大文件。</li>
 *   <li><strong>资源管理</strong>：使用 try-with-resources 自动释放资源。</li>
 *   <li><strong>分片压缩</strong>：支持将大文件压缩为分片 ZIP 文件，便于传输和存储。</li>
 * </ul>
 *
 * <h3>线程安全</h3>
 * <p>类本身无共享状态，方法均为静态；并发处理不同文件/目录是安全的。若对同一路径/同一输出文件并发写入，可能发生冲突或覆盖。</p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 1) 压缩单个文件到 .zip
 * ZipUtils.archive(new File("input.txt"), new File("archive.zip"));
 *
 * // 2) 压缩目录到输出流（不会自动关闭传入流）
 * try (FileOutputStream fos = new FileOutputStream("archive.zip")) {
 *     ZipUtils.archive(new File("inputDir"), fos);
 * }
 *
 * // 3) 批量压缩多个文件/目录到 .zip
 * List<File> inputs = List.of(new File("a.txt"), new File("b"), new File("c"));
 * ZipUtils.archive(inputs, new File("batch.zip"));
 *
 * // 4) 使用 ZipResource 解压 ZIP 文件到目录
 * try (ZipResource resource = new ZipResource(new File("archive.zip"))) {
 *     ZipUtils.extract(resource, new File("outputDir"));
 * }
 *
 * // 5) 解压输入流
 * try (InputStream in = new FileInputStream("archive.zip");
 * 		ZipArchiveInputStream zipArchiveInputStream = new ZipArchiveInputStream(in)) {
 *     ZipUtils.extract(zipArchiveInputStream, new File("outputDir"));
 * }
 *
 * // 6) 使用 ZipFile 解压（适合随机访问和大文件）
 * try (ZipFile zf = new ZipFile(new File("archive.zip"))) {
 *     ZipUtils.extract(zf, new File("outputDir"));
 * }
 *
 * // 7) 解压加密的 ZIP 文件
 * ZipUtils.extract(new File("encrypted.zip"), "password", new File("outputDir"));
 *
 * // 8) 创建加密的 ZIP 文件
 * ZipUtils.archive(new File("input.txt"), new File("encrypted.zip"), "password");
 *
 * // 9) 分片压缩文件（使用默认分片大小）
 * ZipUtils.archiveSplit(new File("largefile.txt"), new File("split.zip"));
 *
 * // 10) 分片压缩文件（指定分片大小）
 * ZipUtils.archiveSplit(new File("largefile.txt"), new File("split.zip"), DataSize.ofMegabytes(10).toBytes());
 *
 * // 11) 分片压缩加密 ZIP 文件
 * ZipUtils.archiveSplit(new File("largefile.txt"), new File("encrypted-split.zip"), "password");
 * }</pre>
 *
 * @author pangju666
 * @see ZipFile
 * @see ZipArchiveInputStream
 * @see ZipArchiveOutputStream
 * @see net.lingala.zip4j.ZipFile
 * @see ZipResource
 * @since 1.0.0
 */
public class ZipUtils {
	/**
	 * 默认分片大小（2GB）。
	 * <p>用于分片压缩时的默认分片大小。</p>
	 *
	 * @since 1.1.0
	 */
	protected static final long DEFAULT_SPLIT_SIZE = DataSize.ofGigabytes(2).toBytes();

	/**
	 * 最小分片大小（64KB）。
	 * <p>分片压缩时的最小分片大小限制。</p>
	 *
	 * @since 1.1.0
	 */
	protected static final long MIN_SPLIT_SIZE = DataSize.ofKilobytes(64).toBytes();
	/**
	 * 最大分片大小（4GB）。
	 * <p>分片压缩时的最大分片大小限制。</p>
	 *
	 * @since 1.1.0
	 */
	protected static final long MAX_SPLIT_SIZE = DataSize.ofGigabytes(4).toBytes();

	/**
	 * 受保护的构造函数，防止实例化。
	 */
	protected ZipUtils() {
	}

	/**
	 * 检查指定文件是否为有效的 ZIP 格式。
	 * <p>通过检测文件魔数（Magic Number）和 Tika 内容分析判断是否为 ZIP 格式。</p>
	 *
	 * @param file 待检测的文件对象，必须存在且可读
	 * @return 如果是 ZIP 格式返回 true，否则返回 false
	 * @throws NullPointerException 当 file 参数为 null 时抛出
	 * @throws IOException          当读取文件失败时抛出
	 * @deprecated 请使用 {@link ZipResource} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static boolean isZip(final File file) throws IOException {
		return FileUtils.isMimeType(file, CompressConstants.ZIP_MIME_TYPE);
	}

	/**
	 * 检查字节数组内容是否为有效的 ZIP 格式。
	 * <p>通过检测字节数组的魔数（Magic Number）和 Tika 内容分析判断是否为 ZIP 格式。</p>
	 *
	 * @param bytes 待检测的字节数组；为 null 或空数组将返回 false
	 * @return 如果是 ZIP 格式返回 true，否则返回 false
	 * @deprecated 请使用 {@link ZipResource} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static boolean isZip(final byte[] bytes) {
		return ArrayUtils.isNotEmpty(bytes) &&
			IOConstants.getDefaultTika().detect(bytes).equals(CompressConstants.ZIP_MIME_TYPE);
	}

	/**
	 * 检查输入流内容是否为有效的 ZIP 格式。
	 * <p>通过检测输入流的魔数（Magic Number）和 Tika 内容分析判断是否为 ZIP 格式。</p>
	 *
	 * @param inputStream 待检测的输入流，非空
	 * @return 如果是 ZIP 格式返回 true，否则返回 false
	 * @throws NullPointerException 当 inputStream 为 null 时抛出
	 * @throws IOException          当流读取发生 I/O 错误时抛出
	 * @deprecated 请使用 {@link ZipResource} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static boolean isZip(final InputStream inputStream) throws IOException {
		return Objects.nonNull(inputStream) &&
			IOConstants.getDefaultTika().detect(inputStream).equals(CompressConstants.ZIP_MIME_TYPE);
	}

	/**
	 * 解压缩 ZIP 文件到指定目录。
	 * <p>将 ZIP 格式文件解压到指定目录，自动创建不存在的目录结构并保持原始文件层级关系。</p>
	 *
	 * @param inputFile 要解压的 ZIP 文件，必须存在且可读
	 * @param outputDir 解压目标目录，如果已存在必须是目录
	 * @throws NullPointerException     当 inputFile 或 outputDir 为 null 时抛出
	 * @throws IllegalArgumentException 当 inputFile 不是有效的 ZIP 格式文件或 outputDir 存在但不是目录时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输入文件不可读</li>
	 *                                      <li>输出目录不可写</li>
	 *                                      <li>解压过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @deprecated 请使用 {@link #extract(ZipResource, File)} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static void uncompress(final File inputFile, final File outputDir) throws IOException {
		Validate.notNull(inputFile, "inputFile 不可为 null");
		Validate.isTrue(isZip(inputFile), "inputFile 不是zip压缩文件");

		try (InputStream inputStream = FileUtils.openBufferedFileChannelInputStream(inputFile);
		     ZipArchiveInputStream zipArchiveInputStream = new ZipArchiveInputStream(inputStream)) {
			ArchiveUtils.extract(zipArchiveInputStream, outputDir);
		}
	}

	/**
	 * 从字节数组解压 ZIP 内容。
	 * <p>将 ZIP 格式的字节数组解压到指定目录，自动创建不存在的目录结构并保持原始文件层级关系。</p>
	 *
	 * @param bytes     要解压的 ZIP 字节数组，必须非空且为有效的 ZIP 格式
	 * @param outputDir 解压目标目录，如果已存在必须是目录
	 * @throws NullPointerException     当 bytes 或 outputDir 为 null 时抛出
	 * @throws IllegalArgumentException 当 bytes 为空数组、不是有效的 ZIP 格式或 outputDir 存在但不是目录时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输出目录不可写</li>
	 *                                      <li>解压过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @deprecated 请使用 {@link #extract(ZipResource, File)} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static void uncompress(final byte[] bytes, final File outputDir) throws IOException {
		Validate.isTrue(ArrayUtils.isNotEmpty(bytes), "bytes 不可为空");
		Validate.isTrue(isZip(bytes), "bytes 不是zip压缩文件数据");

		try (InputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
		     ZipArchiveInputStream zipArchiveInputStream = new ZipArchiveInputStream(inputStream)) {
			ArchiveUtils.extract(zipArchiveInputStream, outputDir);
		}
	}

	/**
	 * 从输入流解压ZIP内容
	 *
	 * @param inputStream ZIP格式输入流，非空
	 * @param outputDir   解压目标目录，会自动创建不存在的目录结构
	 * @throws NullPointerException     当inputStream或outputDir为null时抛出
	 * @throws IllegalArgumentException 当出现以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>outputDir存在但不是目录</li>
	 *                                  </ul>
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输入流不可读或已关闭</li>
	 *                                      <li>输出目录不可写</li>
	 *                                      <li>解压过程中发生I/O错误（包括输入内容非ZIP导致的读取失败）</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 1.0.0
	 * @deprecated 请使用{@link #extract(ZipArchiveInputStream, File)} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static void uncompress(final InputStream inputStream, final File outputDir) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		if (inputStream instanceof ZipArchiveInputStream) {
			extract((ZipArchiveInputStream) inputStream, outputDir);
		} else {
			if (inputStream instanceof BufferedInputStream || inputStream instanceof UnsynchronizedBufferedInputStream) {
				try (ZipArchiveInputStream archiveInputStream = new ZipArchiveInputStream(inputStream)) {
					extract(archiveInputStream, outputDir);
				}
			} else {
				try (InputStream bufferedInputStream = IOUtils.unsynchronizedBuffer(inputStream);
				     ZipArchiveInputStream archiveInputStream = new ZipArchiveInputStream(bufferedInputStream)) {
					extract(archiveInputStream, outputDir);
				}
			}
		}
	}

	/**
	 * 从 {@code ZipArchiveInputStream} 解压到指定目录。
	 * <p>将 ZIP 归档输入流的内容解压到指定目录，自动创建不存在的目录结构并保持原始文件层级关系。</p>
	 *
	 * @param zipArchiveInputStream ZIP 归档输入流，必须非 null
	 * @param outputDir             解压目标目录，会自动创建不存在的目录结构
	 * @throws NullPointerException     当 zipArchiveInputStream 或 outputDir 为 null 时抛出
	 * @throws IllegalArgumentException 当 outputDir 存在但不是目录时抛出
	 * @throws IOException              当输入流不可读、输出目录不可写、解压过程中发生 I/O 错误或磁盘空间不足时抛出
	 * @since 1.0.0
	 * @deprecated 请使用{@link #extract(ZipArchiveInputStream, File)} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static void uncompress(final ZipArchiveInputStream zipArchiveInputStream, final File outputDir) throws IOException {
		extract(zipArchiveInputStream, outputDir);
	}

	/**
	 * 从 {@code ZipFile} 对象解压缩到指定目录。
	 * <p>使用已初始化的 ZipFile 对象将内容解压到指定目录，自动创建不存在的目录结构并保持原始文件层级关系。</p>
	 *
	 * @param zipFile   已初始化的 ZipFile 对象，必须处于可读取状态且不为 null
	 * @param outputDir 解压目标目录，会自动创建不存在的目录结构
	 * @throws NullPointerException     当 {@code zipFile} 或 {@code outputDir} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code outputDir} 存在但不是目录时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>zipFile 已关闭或不可读</li>
	 *                                      <li>输出目录不可写</li>
	 *                                      <li>解压过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 1.0.0
	 * @deprecated 请使用{@link #extract(ZipFile, File)} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static void uncompress(final ZipFile zipFile, final File outputDir) throws IOException {
		extract(zipFile, outputDir);
	}

	/**
	 * 压缩文件/目录到 ZIP 文件。
	 * <p>将单个文件或目录（递归包含子目录）压缩为 ZIP 格式文件。</p>
	 *
	 * @param inputFile  要压缩的文件或目录，必须存在且可读
	 * @param outputFile 输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @throws NullPointerException 当 {@code inputFile} 或 {@code outputFile} 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读（例如抛出 {@code FileNotFoundException}）</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  <li>磁盘空间不足</li>
	 *                              </ul>
	 * @since 1.0.0
	 * @deprecated 请使用{@link #archive(File, File)} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static void compress(final File inputFile, final File outputFile) throws IOException {
		archive(inputFile, outputFile);
	}

	/**
	 * 压缩文件/目录到输出流。
	 * <p>将单个文件或目录（递归包含子目录）压缩为 ZIP 格式并写入输出流。</p>
	 *
	 * @param inputFile    要压缩的文件或目录，必须存在且可读
	 * @param outputStream 输出流对象，必须可写且不为 null（方法不会自动关闭此流）
	 * @throws NullPointerException 当 {@code outputStream} 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读（例如抛出 {@code FileNotFoundException}）</li>
	 *                                  <li>输出流不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                              </ul>
	 * @since 1.0.0
	 * @deprecated 请使用{@link #archive(File, OutputStream)} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static void compress(final File inputFile, final OutputStream outputStream) throws IOException {
		archive(inputFile, outputStream);
	}

	/**
	 * 批量压缩文件/目录到ZIP文件
	 * <p>将多个文件或目录(递归包含子目录)压缩为单个ZIP格式文件</p>
	 *
	 * @param inputFiles 要压缩的文件集合，必须非空且所有文件必须存在
	 * @param outputFile 输出ZIP文件路径，会自动创建父目录并覆盖已存在文件
	 * @throws NullPointerException     当outputFile为null时抛出
	 * @throws IllegalArgumentException 当出现以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>outputFile存在但不是文件</li>
	 *                                  </ul>
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输出文件不可写</li>
	 *                                      <li>压缩过程中发生I/O错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 1.0.0
	 * @deprecated 请使用{@link #archive(Collection, File)} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static void compress(final Collection<File> inputFiles, final File outputFile) throws IOException {
		archive(inputFiles, outputFile);
	}

	/**
	 * 批量压缩文件/目录到输出流。
	 * <p>将多个文件或目录（递归包含子目录）压缩为 TAR 格式并写入输出流。</p>
	 *
	 * @param inputFiles   要压缩的文件集合，必须非空且所有文件必须存在
	 * @param outputStream 输出流对象，必须可写且非空（方法不会自动关闭此流）
	 * @throws NullPointerException     当 {@code outputStream} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空或包含 null 或不存在的文件时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输出流不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                  </ul>
	 * @since 1.0.0
	 * @deprecated 请使用{@link #archive(Collection, OutputStream)} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static void compress(final Collection<File> inputFiles, final OutputStream outputStream) throws IOException {
		archive(inputFiles, outputStream);
	}

	/**
	 * 从 {@code ZipResource} 对象解压缩到指定目录。
	 * <p>通过 ZipResource 打开 ZipFile 并将内容解压到指定目录，自动创建不存在的目录结构并保持原始文件层级关系。</p>
	 *
	 * @param resource  ZIP 资源对象，必须非 null
	 * @param outputDir 解压目标目录，会自动创建不存在的目录结构
	 * @throws NullPointerException 当 {@code resource} 或 {@code outputDir} 为 null 时抛出
	 * @throws IOException          当资源已关闭、输出目录不可写、解压过程中发生 I/O 错误或磁盘空间不足时抛出
	 * @since 1.1.0
	 */
	public static void extract(final ZipResource resource, final File outputDir) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");

		try (ZipFile zipFile = resource.openZipFile()) {
			extract(zipFile, outputDir);
		}
	}

	/**
	 * 从 {@code ZipResource} 对象解压缩到指定目录，可选择忽略本地文件头。
	 * <p>通过 ZipResource 打开 ZipFile 并将内容解压到指定目录，自动创建不存在的目录结构并保持原始文件层级关系。</p>
	 *
	 * @param resource              ZIP 资源对象，必须非 null
	 * @param outputDir             解压目标目录，会自动创建不存在的目录结构
	 * @param ignoreLocalFileHeader 是否忽略本地文件头，某些损坏的 ZIP 文件可能需要设置为 true
	 * @throws NullPointerException 当 {@code resource} 或 {@code outputDir} 为 null 时抛出
	 * @throws IOException          当资源已关闭、输出目录不可写、解压过程中发生 I/O 错误或磁盘空间不足时抛出
	 * @since 1.1.0
	 */
	public static void extract(final ZipResource resource, final File outputDir, final boolean ignoreLocalFileHeader) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");

		try (ZipFile zipFile = resource.openZipFile(ignoreLocalFileHeader)) {
			extract(zipFile, outputDir);
		}
	}

	/**
	 * 从 {@code ZipArchiveInputStream} 解压到指定目录。
	 * <p>将 ZIP 归档输入流的内容解压到指定目录，自动创建不存在的目录结构并保持原始文件层级关系。</p>
	 *
	 * @param zipArchiveInputStream ZIP 归档输入流，必须非 null
	 * @param outputDir             解压目标目录，会自动创建不存在的目录结构
	 * @throws NullPointerException     当 zipArchiveInputStream 或 outputDir 为 null 时抛出
	 * @throws IllegalArgumentException 当 outputDir 存在但不是目录时抛出
	 * @throws IOException              当输入流不可读、输出目录不可写、解压过程中发生 I/O 错误或磁盘空间不足时抛出
	 * @since 1.1.0
	 */
	public static void extract(final ZipArchiveInputStream zipArchiveInputStream, final File outputDir) throws IOException {
		ArchiveUtils.extract(zipArchiveInputStream, outputDir);
	}

	/**
	 * 从 {@code ZipFile} 对象解压缩到指定目录。
	 * <p>使用已初始化的 ZipFile 对象将内容解压到指定目录，自动创建不存在的目录结构并保持原始文件层级关系。</p>
	 *
	 * @param zipFile   已初始化的 ZipFile 对象，必须处于可读取状态且不为 null
	 * @param outputDir 解压目标目录，会自动创建不存在的目录结构
	 * @throws NullPointerException     当 {@code zipFile} 或 {@code outputDir} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code outputDir} 存在但不是目录时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>zipFile 已关闭或不可读</li>
	 *                                      <li>输出目录不可写</li>
	 *                                      <li>解压过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 1.1.0
	 */
	public static void extract(final ZipFile zipFile, final File outputDir) throws IOException {
		Validate.notNull(zipFile, "zipFile 不可为 null");

		ArchiveUtils.extract(zipFile.getEntries().asIterator(), outputDir, zipFile::getInputStream);
	}

	/**
	 * 从 {@code ZipResource} 对象解压缩加密的 ZIP 文件到指定目录。
	 * <p>通过 ZipResource 打开加密的 ZIP 文件并将内容解压到指定目录，自动创建不存在的目录结构并保持原始文件层级关系。</p>
	 *
	 * @param resource  ZIP 资源对象，必须非 null
	 * @param outputDir 解压目标目录，会自动创建不存在的目录结构
	 * @param password  ZIP 文件密码，必须非空
	 * @throws NullPointerException     当 resource、password 或 outputDir 为 null 时抛出
	 * @throws IllegalArgumentException 当 password 为空时抛出
	 * @throws IOException              当资源已关闭、输出目录不可写、解压过程中发生 I/O 错误或磁盘空间不足时抛出
	 * @since 1.1.0
	 */
	public static void extract(final ZipResource resource, final File outputDir, final String password) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notBlank(password, "password 不可为空");
		FileUtils.checkDirIfExist(outputDir, "outputDir 不可为 null");

		FileUtils.forceMkdir(outputDir);

		try (net.lingala.zip4j.ZipFile zipFile = resource.openZipFile(password)) {
			zipFile.extractAll(outputDir.getAbsolutePath());
		}
	}

	/**
	 * 压缩文件/目录到 ZIP 文件。
	 * <p>将单个文件或目录（递归包含子目录）压缩为 ZIP 格式文件。</p>
	 *
	 * @param inputFile  要压缩的文件或目录，必须存在且可读
	 * @param outputFile 输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @throws NullPointerException 当 {@code inputFile} 或 {@code outputFile} 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读（例如抛出 {@code FileNotFoundException}）</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  <li>磁盘空间不足</li>
	 *                              </ul>
	 * @since 1.1.0
	 */
	public static void archive(final File inputFile, final File outputFile) throws IOException {
		archive(inputFile, outputFile, Deflater.DEFAULT_COMPRESSION, null);
	}

	/**
	 * 压缩文件/目录到 ZIP 文件，指定压缩级别。
	 * <p>将单个文件或目录（递归包含子目录）压缩为 ZIP 格式文件，使用指定的压缩级别。</p>
	 *
	 * @param inputFile  要压缩的文件或目录，必须存在且可读
	 * @param outputFile 输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param level      压缩级别（0-9），0 表示无压缩，9 表示最高压缩
	 * @throws NullPointerException 当 {@code inputFile} 或 {@code outputFile} 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  <li>磁盘空间不足</li>
	 *                              </ul>
	 * @see Deflater#DEFAULT_COMPRESSION
	 * @see Deflater#BEST_COMPRESSION
	 * @see Deflater#BEST_SPEED
	 * @see Deflater#NO_COMPRESSION
	 * @since 1.1.0
	 */
	public static void archive(final File inputFile, final File outputFile, final int level) throws IOException {
		archive(inputFile, outputFile, level, null);
	}

	/**
	 * 压缩文件/目录到 ZIP 文件，支持自定义 ZIP 条目处理器。
	 * <p>将单个文件或目录（递归包含子目录）压缩为 ZIP 格式文件，并通过 Consumer 对每个 ZIP 条目进行自定义处理。</p>
	 *
	 * @param inputFile     要压缩的文件或目录，必须存在且可读
	 * @param outputFile    输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param entryConsumer ZIP 条目处理器，可为 null
	 * @throws NullPointerException 当 {@code inputFile} 或 {@code outputFile} 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  <li>磁盘空间不足</li>
	 *                              </ul>
	 * @since 1.1.0
	 */
	public static void archive(final File inputFile, final File outputFile,
	                            final Consumer<ZipArchiveEntry> entryConsumer) throws IOException {
		archive(inputFile, outputFile, Deflater.DEFAULT_COMPRESSION, entryConsumer);
	}

	/**
	 * 压缩文件/目录到 ZIP 文件，指定压缩级别和自定义 ZIP 条目处理器。
	 * <p>将单个文件或目录（递归包含子目录）压缩为 ZIP 格式文件，使用指定的压缩级别，并通过 Consumer 对每个 ZIP 条目进行自定义处理。</p>
	 *
	 * @param inputFile     要压缩的文件或目录，必须存在且可读
	 * @param outputFile    输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param level         压缩级别（0-9），0 表示无压缩，9 表示最高压缩
	 * @param entryConsumer ZIP 条目处理器，可为 null
	 * @throws NullPointerException 当 {@code inputFile} 或 {@code outputFile} 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  <li>磁盘空间不足</li>
	 *                              </ul>
	 * @see Deflater#DEFAULT_COMPRESSION
	 * @see Deflater#BEST_COMPRESSION
	 * @see Deflater#BEST_SPEED
	 * @see Deflater#NO_COMPRESSION
	 * @since 1.1.0
	 */
	public static void archive(final File inputFile, final File outputFile, final int level,
	                            final Consumer<ZipArchiveEntry> entryConsumer) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (BufferedOutputStream bufferedOutputStream = FileUtils.newBufferedOutputStream(outputFile);
		     ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(bufferedOutputStream)) {
			zipArchiveOutputStream.setLevel(level);
			ArchiveUtils.archive(inputFile, zipArchiveOutputStream, entryConsumer);
		}
	}

	/**
	 * 压缩文件/目录到输出流。
	 * <p>将单个文件或目录（递归包含子目录）压缩为 ZIP 格式并写入输出流。</p>
	 *
	 * @param inputFile    要压缩的文件或目录，必须存在且可读
	 * @param outputStream 输出流对象，必须可写且不为 null（方法不会自动关闭此流）
	 * @throws NullPointerException 当 {@code outputStream} 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读（例如抛出 {@code FileNotFoundException}）</li>
	 *                                  <li>输出流不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                              </ul>
	 * @since 1.1.0
	 */
	public static void archive(final File inputFile, final OutputStream outputStream) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		if (outputStream instanceof ZipArchiveOutputStream) {
			ArchiveUtils.archive(inputFile, (ZipArchiveOutputStream) outputStream,
				null);
		} else {
			try (BufferedOutputStream bufferedOutputStream = IOUtils.buffer(outputStream);
			     ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(bufferedOutputStream)) {
				ArchiveUtils.archive(inputFile, zipArchiveOutputStream, null);
			}
		}
	}

	/**
	 * 压缩文件/目录到输出流，指定压缩级别。
	 * <p>将单个文件或目录（递归包含子目录）压缩为 ZIP 格式并写入输出流，使用指定的压缩级别。</p>
	 *
	 * @param inputFile    要压缩的文件或目录，必须存在且可读
	 * @param outputStream 输出流对象，必须可写且不为 null（方法不会自动关闭此流）
	 * @param level        压缩级别（0-9），0 表示无压缩，9 表示最高压缩
	 * @throws NullPointerException 当 {@code inputFile} 或 {@code outputStream} 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读</li>
	 *                                  <li>输出流不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                              </ul>
	 * @see Deflater#DEFAULT_COMPRESSION
	 * @see Deflater#BEST_COMPRESSION
	 * @see Deflater#BEST_SPEED
	 * @see Deflater#NO_COMPRESSION
	 * @since 1.1.0
	 */
	public static void archive(final File inputFile, final OutputStream outputStream, final int level) throws IOException {
		archive(inputFile, outputStream, level, null);
	}

	/**
	 * 压缩文件/目录到输出流，支持自定义 ZIP 条目处理器。
	 * <p>将单个文件或目录（递归包含子目录）压缩为 ZIP 格式并写入输出流，通过 Consumer 对每个 ZIP 条目进行自定义处理。</p>
	 *
	 * @param inputFile     要压缩的文件或目录，必须存在且可读
	 * @param outputStream  输出流对象，必须可写且不为 null（方法不会自动关闭此流）
	 * @param entryConsumer ZIP 条目处理器，可为 null
	 * @throws NullPointerException 当 {@code outputStream} 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读</li>
	 *                                  <li>输出流不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                              </ul>
	 * @since 1.1.0
	 */
	public static void archive(final File inputFile, final OutputStream outputStream,
	                           final Consumer<ZipArchiveEntry> entryConsumer) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		if (outputStream instanceof ZipArchiveOutputStream) {
			ArchiveUtils.archive(inputFile, (ZipArchiveOutputStream) outputStream, entryConsumer);
		} else {
			try (BufferedOutputStream bufferedOutputStream = IOUtils.buffer(outputStream);
			     ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(bufferedOutputStream)) {
				ArchiveUtils.archive(inputFile, zipArchiveOutputStream, entryConsumer);
			}
		}
	}

	/**
	 * 压缩文件/目录到输出流，指定压缩级别和自定义 ZIP 条目处理器。
	 * <p>将单个文件或目录（递归包含子目录）压缩为 ZIP 格式并写入输出流，使用指定的压缩级别，并通过 Consumer 对每个 ZIP 条目进行自定义处理。</p>
	 *
	 * @param inputFile     要压缩的文件或目录，必须存在且可读
	 * @param outputStream  输出流对象，必须可写且不为 null（方法不会自动关闭此流）
	 * @param level         压缩级别（0-9），0 表示无压缩，9 表示最高压缩
	 * @param entryConsumer ZIP 条目处理器，可为 null
	 * @throws NullPointerException 当 {@code inputFile} 或 {@code outputStream} 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读</li>
	 *                                  <li>输出流不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                              </ul>
	 * @see Deflater#DEFAULT_COMPRESSION
	 * @see Deflater#BEST_COMPRESSION
	 * @see Deflater#BEST_SPEED
	 * @see Deflater#NO_COMPRESSION
	 * @since 1.1.0
	 */
	public static void archive(final File inputFile, final OutputStream outputStream, final int level,
	                            final Consumer<ZipArchiveEntry> entryConsumer) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		if (outputStream instanceof ZipArchiveOutputStream) {
			ZipArchiveOutputStream zipArchiveOutputStream = (ZipArchiveOutputStream) outputStream;
			zipArchiveOutputStream.setLevel(level);
			ArchiveUtils.archive(inputFile, zipArchiveOutputStream, entryConsumer);
		} else {
			try (BufferedOutputStream bufferedOutputStream = IOUtils.buffer(outputStream);
			     ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(bufferedOutputStream)) {
				zipArchiveOutputStream.setLevel(level);
				ArchiveUtils.archive(inputFile, zipArchiveOutputStream, entryConsumer);
			}
		}
	}

	/**
	 * 压缩文件/目录到可寻址字节通道。
	 * <p>将单个文件或目录（递归包含子目录）压缩为 ZIP 格式并写入可寻址字节通道。</p>
	 *
	 * @param inputFile     要压缩的文件或目录，必须存在且可读
	 * @param outputChannel 可寻址字节通道，必须可写且不为 null
	 * @throws NullPointerException 当 {@code inputFile} 或 {@code outputChannel} 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读</li>
	 *                                  <li>输出通道不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                              </ul>
	 * @since 1.1.0
	 */
	public static void archive(final File inputFile, final SeekableByteChannel outputChannel) throws IOException {
		archive(inputFile, outputChannel, Deflater.DEFAULT_COMPRESSION, null);
	}

	/**
	 * 压缩文件/目录到可寻址字节通道，指定压缩级别。
	 * <p>将单个文件或目录（递归包含子目录）压缩为 ZIP 格式并写入可寻址字节通道，使用指定的压缩级别。</p>
	 *
	 * @param inputFile     要压缩的文件或目录，必须存在且可读
	 * @param outputChannel 可寻址字节通道，必须可写且不为 null
	 * @param level         压缩级别（0-9），0 表示无压缩，9 表示最高压缩
	 * @throws NullPointerException 当 {@code inputFile} 或 {@code outputChannel} 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读</li>
	 *                                  <li>输出通道不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                              </ul>
	 * @see Deflater#DEFAULT_COMPRESSION
	 * @see Deflater#BEST_COMPRESSION
	 * @see Deflater#BEST_SPEED
	 * @see Deflater#NO_COMPRESSION
	 * @since 1.1.0
	 */
	public static void archive(final File inputFile, final SeekableByteChannel outputChannel, final int level) throws IOException {
		archive(inputFile, outputChannel, level, null);
	}

	/**
	 * 压缩文件/目录到可寻址字节通道，支持自定义 ZIP 条目处理器。
	 * <p>将单个文件或目录（递归包含子目录）压缩为 ZIP 格式并写入可寻址字节通道，通过 Consumer 对每个 ZIP 条目进行自定义处理。</p>
	 *
	 * @param inputFile     要压缩的文件或目录，必须存在且可读
	 * @param outputChannel 可寻址字节通道，必须可写且不为 null
	 * @param entryConsumer ZIP 条目处理器，可为 null
	 * @throws NullPointerException 当 {@code inputFile} 或 {@code outputChannel} 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读</li>
	 *                                  <li>输出通道不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                              </ul>
	 * @since 1.1.0
	 */
	public static void archive(final File inputFile, final SeekableByteChannel outputChannel,
	                            final Consumer<ZipArchiveEntry> entryConsumer) throws IOException {
		archive(inputFile, outputChannel, Deflater.DEFAULT_COMPRESSION, entryConsumer);
	}

	/**
	 * 压缩文件/目录到可寻址字节通道，指定压缩级别和自定义 ZIP 条目处理器。
	 * <p>将单个文件或目录（递归包含子目录）压缩为 ZIP 格式并写入可寻址字节通道，使用指定的压缩级别，并通过 Consumer 对每个 ZIP 条目进行自定义处理。</p>
	 *
	 * @param inputFile     要压缩的文件或目录，必须存在且可读
	 * @param outputChannel 可寻址字节通道，必须可写且不为 null
	 * @param level         压缩级别（0-9），0 表示无压缩，9 表示最高压缩
	 * @param entryConsumer ZIP 条目处理器，可为 null
	 * @throws NullPointerException 当 {@code inputFile} 或 {@code outputChannel} 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读</li>
	 *                                  <li>输出通道不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                              </ul>
	 * @see Deflater#DEFAULT_COMPRESSION
	 * @see Deflater#BEST_COMPRESSION
	 * @see Deflater#BEST_SPEED
	 * @see Deflater#NO_COMPRESSION
	 * @since 1.1.0
	 */
	public static void archive(final File inputFile, final SeekableByteChannel outputChannel, final int level,
	                           final Consumer<ZipArchiveEntry> entryConsumer) throws IOException {
		Validate.notNull(outputChannel, "outputChannel 不可为 null");

		try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputChannel)) {
			zipArchiveOutputStream.setLevel(level);
			ArchiveUtils.archive(inputFile, zipArchiveOutputStream, entryConsumer);
		}
	}

	/**
	 * 批量压缩文件/目录到ZIP文件
	 * <p>将多个文件或目录(递归包含子目录)压缩为单个ZIP格式文件</p>
	 *
	 * @param inputFiles 要压缩的文件集合，必须非空且所有文件必须存在
	 * @param outputFile 输出ZIP文件路径，会自动创建父目录并覆盖已存在文件
	 * @throws NullPointerException     当outputFile为null时抛出
	 * @throws IllegalArgumentException 当出现以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>outputFile存在但不是文件</li>
	 *                                  </ul>
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输出文件不可写</li>
	 *                                      <li>压缩过程中发生I/O错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 1.1.0
	 */
	public static void archive(final Collection<File> inputFiles, final File outputFile) throws IOException {
		archive(inputFiles, outputFile, Deflater.DEFAULT_COMPRESSION, null);
	}

	/**
	 * 批量压缩文件/目录到 ZIP 文件，指定压缩级别。
	 * <p>将多个文件或目录（递归包含子目录）压缩为单个 ZIP 格式文件，使用指定的压缩级别。</p>
	 *
	 * @param inputFiles 要压缩的文件集合，必须非空且所有文件必须存在
	 * @param outputFile 输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param level      压缩级别（0-9），0 表示无压缩，9 表示最高压缩
	 * @throws NullPointerException     当 {@code outputFile} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code outputFile} 存在但不是文件时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输出文件不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @see Deflater#DEFAULT_COMPRESSION
	 * @see Deflater#BEST_COMPRESSION
	 * @see Deflater#BEST_SPEED
	 * @see Deflater#NO_COMPRESSION
	 * @since 1.1.0
	 */
	public static void archive(final Collection<File> inputFiles, final File outputFile, final int level) throws IOException {
		archive(inputFiles, outputFile, level, null);
	}

	/**
	 * 批量压缩文件/目录到 ZIP 文件，支持自定义 ZIP 条目处理器。
	 * <p>将多个文件或目录（递归包含子目录）压缩为单个 ZIP 格式文件，通过 Consumer 对每个 ZIP 条目进行自定义处理。</p>
	 *
	 * @param inputFiles    要压缩的文件集合，必须非空且所有文件必须存在
	 * @param outputFile    输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param entryConsumer ZIP 条目处理器，可为 null
	 * @throws NullPointerException     当 {@code outputFile} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code outputFile} 存在但不是文件时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输出文件不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 1.1.0
	 */
	public static void archive(final Collection<File> inputFiles, final File outputFile,
	                            final Consumer<ZipArchiveEntry> entryConsumer) throws IOException {
		archive(inputFiles, outputFile, Deflater.DEFAULT_COMPRESSION, entryConsumer);
	}

	/**
	 * 批量压缩文件/目录到 ZIP 文件，指定压缩级别和自定义 ZIP 条目处理器。
	 * <p>将多个文件或目录（递归包含子目录）压缩为单个 ZIP 格式文件，使用指定的压缩级别，并通过 Consumer 对每个 ZIP 条目进行自定义处理。</p>
	 *
	 * @param inputFiles    要压缩的文件集合，必须非空且所有文件必须存在
	 * @param outputFile    输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param level         压缩级别（0-9），0 表示无压缩，9 表示最高压缩
	 * @param entryConsumer ZIP 条目处理器，可为 null
	 * @throws NullPointerException     当 {@code outputFile} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code outputFile} 存在但不是文件时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输出文件不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @see Deflater#DEFAULT_COMPRESSION
	 * @see Deflater#BEST_COMPRESSION
	 * @see Deflater#BEST_SPEED
	 * @see Deflater#NO_COMPRESSION
	 * @since 1.1.0
	 */
	public static void archive(final Collection<File> inputFiles, final File outputFile, final int level,
	                            final Consumer<ZipArchiveEntry> entryConsumer) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (BufferedOutputStream bufferedOutputStream = FileUtils.newBufferedOutputStream(outputFile);
		     ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(bufferedOutputStream)) {
			zipArchiveOutputStream.setLevel(level);
			ArchiveUtils.archive(inputFiles, zipArchiveOutputStream, entryConsumer);
		}
	}

	/**
	 * 批量压缩文件到输出流。
	 * <p>将多个文件或目录（递归包含子目录）压缩为 ZIP 格式并写入输出流。</p>
	 *
	 * @param inputFiles   要压缩的文件集合，必须非空且所有文件必须存在
	 * @param outputStream 输出流对象，必须可写且不为 null（方法不会自动关闭此流）
	 * @throws NullPointerException     当 {@code outputStream} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空或包含 null 或不存在的文件时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输出流不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                  </ul>
	 * @since 1.1.0
	 */
	public static void archive(final Collection<File> inputFiles, final OutputStream outputStream) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		if (outputStream instanceof ZipArchiveOutputStream) {
			ArchiveUtils.archive(inputFiles, (ZipArchiveOutputStream) outputStream,
				null);
		} else {
			try (BufferedOutputStream bufferedOutputStream = IOUtils.buffer(outputStream);
			     ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(bufferedOutputStream)) {
				ArchiveUtils.archive(inputFiles, zipArchiveOutputStream, null);
			}
		}
	}

	/**
	 * 批量压缩文件到输出流，指定压缩级别。
	 * <p>将多个文件或目录（递归包含子目录）压缩为 ZIP 格式并写入输出流，使用指定的压缩级别。</p>
	 *
	 * @param inputFiles   要压缩的文件集合，必须非空且所有文件必须存在
	 * @param outputStream 输出流对象，必须可写且不为 null（方法不会自动关闭此流）
	 * @param level        压缩级别（0-9），0 表示无压缩，9 表示最高压缩
	 * @throws NullPointerException     当 {@code outputStream} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空或包含 null 或不存在的文件时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输出流不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                  </ul>
	 * @see Deflater#DEFAULT_COMPRESSION
	 * @see Deflater#BEST_COMPRESSION
	 * @see Deflater#BEST_SPEED
	 * @see Deflater#NO_COMPRESSION
	 * @since 1.1.0
	 */
	public static void archive(final Collection<File> inputFiles, final OutputStream outputStream, final int level) throws IOException {
		archive(inputFiles, outputStream, level, null);
	}

	/**
	 * 批量压缩文件到输出流，支持自定义 ZIP 条目处理器。
	 * <p>将多个文件或目录（递归包含子目录）压缩为 ZIP 格式并写入输出流，通过 Consumer 对每个 ZIP 条目进行自定义处理。</p>
	 *
	 * @param inputFiles    要压缩的文件集合，必须非空且所有文件必须存在
	 * @param outputStream  输出流对象，必须可写且不为 null（方法不会自动关闭此流）
	 * @param entryConsumer ZIP 条目处理器，可为 null
	 * @throws NullPointerException     当 {@code outputStream} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空或包含 null 或不存在的文件时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输出流不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                  </ul>
	 * @since 1.1.0
	 */
	public static void archive(final Collection<File> inputFiles, final OutputStream outputStream,
	                           final Consumer<ZipArchiveEntry> entryConsumer) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		if (outputStream instanceof ZipArchiveOutputStream) {
			ArchiveUtils.archive(inputFiles, (ZipArchiveOutputStream) outputStream, entryConsumer);
		} else {
			try (BufferedOutputStream bufferedOutputStream = IOUtils.buffer(outputStream);
			     ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(bufferedOutputStream)) {
				ArchiveUtils.archive(inputFiles, zipArchiveOutputStream, entryConsumer);
			}
		}
	}

	/**
	 * 批量压缩文件到输出流，指定压缩级别和自定义 ZIP 条目处理器。
	 * <p>将多个文件或目录（递归包含子目录）压缩为 ZIP 格式并写入输出流，使用指定的压缩级别，并通过 Consumer 对每个 ZIP 条目进行自定义处理。</p>
	 *
	 * @param inputFiles    要压缩的文件集合，必须非空且所有文件必须存在
	 * @param outputStream  输出流对象，必须可写且不为 null（方法不会自动关闭此流）
	 * @param level         压缩级别（0-9），0 表示无压缩，9 表示最高压缩
	 * @param entryConsumer ZIP 条目处理器，可为 null
	 * @throws NullPointerException     当 {@code outputStream} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空或包含 null 或不存在的文件时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输出流不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                  </ul>
	 * @see Deflater#DEFAULT_COMPRESSION
	 * @see Deflater#BEST_COMPRESSION
	 * @see Deflater#BEST_SPEED
	 * @see Deflater#NO_COMPRESSION
	 * @since 1.1.0
	 */
	public static void archive(final Collection<File> inputFiles, final OutputStream outputStream, final int level,
	                            final Consumer<ZipArchiveEntry> entryConsumer) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		if (outputStream instanceof ZipArchiveOutputStream) {
			ZipArchiveOutputStream zipArchiveOutputStream = (ZipArchiveOutputStream) outputStream;
			zipArchiveOutputStream.setLevel(level);
			ArchiveUtils.archive(inputFiles, zipArchiveOutputStream, entryConsumer);
		} else {
			try (BufferedOutputStream bufferedOutputStream = IOUtils.buffer(outputStream);
			     ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(bufferedOutputStream)) {
				zipArchiveOutputStream.setLevel(level);
				ArchiveUtils.archive(inputFiles, zipArchiveOutputStream, entryConsumer);
			}
		}
	}

	/**
	 * 批量压缩文件到可寻址字节通道。
	 * <p>将多个文件或目录（递归包含子目录）压缩为 ZIP 格式并写入可寻址字节通道。</p>
	 *
	 * @param inputFiles    要压缩的文件集合，必须非空且所有文件必须存在
	 * @param outputChannel 可寻址字节通道，必须可写且不为 null
	 * @throws NullPointerException     当 {@code outputChannel} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空或包含 null 或不存在的文件时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输出通道不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                  </ul>
	 * @since 1.1.0
	 */
	public static void archive(final Collection<File> inputFiles, final SeekableByteChannel outputChannel) throws IOException {
		archive(inputFiles, outputChannel, Deflater.DEFAULT_COMPRESSION, null);
	}

	/**
	 * 批量压缩文件到可寻址字节通道，指定压缩级别。
	 * <p>将多个文件或目录（递归包含子目录）压缩为 ZIP 格式并写入可寻址字节通道，使用指定的压缩级别。</p>
	 *
	 * @param inputFiles    要压缩的文件集合，必须非空且所有文件必须存在
	 * @param outputChannel 可寻址字节通道，必须可写且不为 null
	 * @param level         压缩级别（0-9），0 表示无压缩，9 表示最高压缩
	 * @throws NullPointerException     当 {@code outputChannel} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空或包含 null 或不存在的文件时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输出通道不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                  </ul>
	 * @see Deflater#DEFAULT_COMPRESSION
	 * @see Deflater#BEST_COMPRESSION
	 * @see Deflater#BEST_SPEED
	 * @see Deflater#NO_COMPRESSION
	 * @since 1.1.0
	 */
	public static void archive(final Collection<File> inputFiles, final SeekableByteChannel outputChannel, final int level) throws IOException {
		archive(inputFiles, outputChannel, level, null);
	}

	/**
	 * 批量压缩文件到可寻址字节通道，支持自定义 ZIP 条目处理器。
	 * <p>将多个文件或目录（递归包含子目录）压缩为 ZIP 格式并写入可寻址字节通道，通过 Consumer 对每个 ZIP 条目进行自定义处理。</p>
	 *
	 * @param inputFiles    要压缩的文件集合，必须非空且所有文件必须存在
	 * @param outputChannel 可寻址字节通道，必须可写且不为 null
	 * @param entryConsumer ZIP 条目处理器，可为 null
	 * @throws NullPointerException     当 {@code outputChannel} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空或包含 null 或不存在的文件时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输出通道不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                  </ul>
	 * @since 1.1.0
	 */
	public static void archive(final Collection<File> inputFiles, final SeekableByteChannel outputChannel,
	                            final Consumer<ZipArchiveEntry> entryConsumer) throws IOException {
		archive(inputFiles, outputChannel, Deflater.DEFAULT_COMPRESSION, entryConsumer);
	}

	/**
	 * 批量压缩文件到可寻址字节通道，指定压缩级别和自定义 ZIP 条目处理器。
	 * <p>将多个文件或目录（递归包含子目录）压缩为 ZIP 格式并写入可寻址字节通道，使用指定的压缩级别，并通过 Consumer 对每个 ZIP 条目进行自定义处理。</p>
	 *
	 * @param inputFiles    要压缩的文件集合，必须非空且所有文件必须存在
	 * @param outputChannel 可寻址字节通道，必须可写且不为 null
	 * @param level         压缩级别（0-9），0 表示无压缩，9 表示最高压缩
	 * @param entryConsumer ZIP 条目处理器，可为 null
	 * @throws NullPointerException     当 {@code outputChannel} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空或包含 null 或不存在的文件时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输出通道不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                  </ul>
	 * @see Deflater#DEFAULT_COMPRESSION
	 * @see Deflater#BEST_COMPRESSION
	 * @see Deflater#BEST_SPEED
	 * @see Deflater#NO_COMPRESSION
	 * @since 1.1.0
	 */
	public static void archive(final Collection<File> inputFiles, final SeekableByteChannel outputChannel,
	                            final int level, final Consumer<ZipArchiveEntry> entryConsumer) throws IOException {
		Validate.notNull(outputChannel, "outputChannel 不可为 null");

		try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputChannel)) {
			zipArchiveOutputStream.setLevel(level);
			ArchiveUtils.archive(inputFiles, zipArchiveOutputStream, entryConsumer);
		}
	}

	/**
	 * 压缩文件/目录到 ZIP 文件，使用自定义 ZIP 参数。
	 * <p>将单个文件或目录（递归包含子目录）压缩为 ZIP 格式文件，使用指定的 ZipParameters 进行自定义配置。</p>
	 *
	 * @param inputFile  要压缩的文件或目录，必须存在且可读
	 * @param outputFile 输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param parameters ZIP 参数配置，必须非 null
	 * @throws NullPointerException 当 {@code inputFile}、{@code outputFile} 或 {@code parameters} 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  <li>磁盘空间不足</li>
	 *                              </ul>
	 * @since 1.1.0
	 */
	public static void archive(final File inputFile, final File outputFile, final ZipParameters parameters) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		FileUtils.check(inputFile, "inputFile 不可为 null");
		Validate.notNull(parameters, "parameters 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		if (outputFile.exists()) {
			FileUtils.forceDelete(outputFile);
		}

		try (net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(outputFile)) {
			if (inputFile.isDirectory()) {
				zipFile.addFolder(inputFile, parameters);
			} else {
				zipFile.addFile(inputFile, parameters);
			}
		}
	}

	/**
	 * 批量压缩文件/目录到 ZIP 文件，使用自定义 ZIP 参数。
	 * <p>将多个文件或目录（递归包含子目录）压缩为单个 ZIP 格式文件，使用指定的 ZipParameters 进行自定义配置。</p>
	 *
	 * @param inputFiles 要压缩的文件/目录列表，必须非空且非 null
	 * @param outputFile 输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param parameters ZIP 参数配置，必须非 null
	 * @throws NullPointerException     当 {@code inputFiles}、{@code outputFile} 或 {@code parameters} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空列表时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输入文件不存在或不可读</li>
	 *                                      <li>输出文件不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 1.1.0
	 */
	public static void archive(final List<File> inputFiles, final File outputFile, final ZipParameters parameters) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notEmpty(inputFiles, "inputFiles 不可为空");
		Validate.notNull(parameters, "parameters 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		if (outputFile.exists()) {
			FileUtils.forceDelete(outputFile);
		}

		try (net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(outputFile)) {
			for (File inputFile : inputFiles) {
				FileUtils.check(inputFile, "inputFile 不可为 null");

				if (inputFile.isDirectory()) {
					zipFile.addFolder(inputFile, parameters);
				} else {
					zipFile.addFile(inputFile, parameters);
				}
			}
		}
	}

	/**
	 * 压缩文件/目录到加密的 ZIP 文件。
	 * <p>将单个文件或目录（递归包含子目录）压缩为加密的 ZIP 格式文件，使用默认加密参数。</p>
	 *
	 * @param inputFile  要压缩的文件或目录，必须存在且可读
	 * @param outputFile 输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param password   ZIP 文件密码，必须非空
	 * @throws NullPointerException     当 {@code inputFile}、{@code outputFile} 或 {@code password} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code password} 为空时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输入文件不存在或不可读</li>
	 *                                      <li>输出文件不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 1.1.0
	 */
	public static void archive(final File inputFile, final File outputFile, final String password) throws IOException {
		archive(inputFile, outputFile, password, new ZipParameters());
	}

	/**
	 * 批量压缩文件/目录到加密的 ZIP 文件。
	 * <p>将多个文件或目录（递归包含子目录）压缩为加密的 ZIP 格式文件，使用默认加密参数。</p>
	 *
	 * @param inputFiles 要压缩的文件/目录列表，必须非空且非 null
	 * @param outputFile 输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param password   ZIP 文件密码，必须非空
	 * @throws NullPointerException     当 {@code inputFiles}、{@code outputFile} 或 {@code password} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空列表或 {@code password} 为空时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输入文件不存在或不可读</li>
	 *                                      <li>输出文件不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 1.1.0
	 */
	public static void archive(final List<File> inputFiles, final File outputFile, final String password) throws IOException {
		archive(inputFiles, outputFile, password, new ZipParameters());
	}

	/**
	 * 压缩文件/目录到加密的 ZIP 文件，使用自定义 ZIP 参数。
	 * <p>将单个文件或目录（递归包含子目录）压缩为加密的 ZIP 格式文件，使用指定的密码和 ZipParameters 进行自定义配置。</p>
	 *
	 * @param inputFile  要压缩的文件或目录，必须存在且可读
	 * @param outputFile 输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param password   ZIP 文件密码，必须非空
	 * @param parameters ZIP 参数配置，必须非 null（会自动设置加密标志和默认加密方法）
	 * @throws NullPointerException     当 {@code inputFile}、{@code outputFile}、{@code password} 或 {@code parameters} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code password} 为空时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输入文件不存在或不可读</li>
	 *                                      <li>输出文件不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 1.1.0
	 */
	public static void archive(final File inputFile, final File outputFile, final String password,
	                            final ZipParameters parameters) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		FileUtils.check(inputFile, "inputFile 不可为 null");
		Validate.notBlank(password, "password 不可为空");
		Validate.notNull(parameters, "parameters 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		if (outputFile.exists()) {
			FileUtils.forceDelete(outputFile);
		}

		parameters.setEncryptFiles(true);
		if (Objects.isNull(parameters.getEncryptionMethod()) || parameters.getEncryptionMethod() == EncryptionMethod.NONE) {
			parameters.setEncryptionMethod(EncryptionMethod.AES);
		}

		try (net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(outputFile, password.toCharArray())) {
			if (inputFile.isDirectory()) {
				zipFile.addFolder(inputFile, parameters);
			} else {
				zipFile.addFile(inputFile, parameters);
			}
		}
	}

	/**
	 * 批量压缩文件/目录到加密的 ZIP 文件，使用自定义 ZIP 参数。
	 * <p>将多个文件或目录（递归包含子目录）压缩为加密的 ZIP 格式文件，使用指定的密码和 ZipParameters 进行自定义配置。</p>
	 *
	 * @param inputFiles 要压缩的文件/目录列表，必须非空且非 null
	 * @param outputFile 输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param password   ZIP 文件密码，必须非空
	 * @param parameters ZIP 参数配置，必须非 null（会自动设置加密标志和默认加密方法）
	 * @throws NullPointerException     当 {@code inputFiles}、{@code outputFile}、{@code password} 或 {@code parameters} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空列表或 {@code password} 为空时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输入文件不存在或不可读</li>
	 *                                      <li>输出文件不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 1.1.0
	 */
	public static void archive(final List<File> inputFiles, final File outputFile, final String password,
	                            final ZipParameters parameters) throws IOException {
		Validate.notBlank(password, "password 不可为空");

		parameters.setEncryptFiles(true);
		if (Objects.isNull(parameters.getEncryptionMethod()) || parameters.getEncryptionMethod() == EncryptionMethod.NONE) {
			parameters.setEncryptionMethod(EncryptionMethod.AES);
		}

		try (net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(outputFile, password.toCharArray())) {
			for (File inputFile : inputFiles) {
				FileUtils.check(inputFile, "inputFile 不可为 null");

				if (inputFile.isDirectory()) {
					zipFile.addFolder(inputFile, parameters);
				} else {
					zipFile.addFile(inputFile, parameters);
				}
			}
		}
	}

	/**
	 * 分片压缩文件/目录到 ZIP 文件。
	 * <p>将单个文件或目录（递归包含子目录）压缩为分片 ZIP 格式文件，使用默认分片大小。</p>
	 *
	 * @param inputFile  要压缩的文件或目录，必须存在且可读
	 * @param outputFile 输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @throws NullPointerException 当 {@code inputFile} 或 {@code outputFile} 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  <li>磁盘空间不足</li>
	 *                              </ul>
	 * @since 1.1.0
	 */
	public static void archiveSplit(final File inputFile, final File outputFile) throws IOException {
		archiveSplit(inputFile, outputFile, DEFAULT_SPLIT_SIZE, Deflater.DEFAULT_COMPRESSION,
			null);
	}

	/**
	 * 分片压缩文件/目录到 ZIP 文件，指定压缩级别。
	 * <p>将单个文件或目录（递归包含子目录）压缩为分片 ZIP 格式文件，使用默认分片大小和指定的压缩级别。</p>
	 *
	 * @param inputFile  要压缩的文件或目录，必须存在且可读
	 * @param outputFile 输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param level      压缩级别（0-9），0 表示无压缩，9 表示最高压缩
	 * @throws NullPointerException 当 {@code inputFile} 或 {@code outputFile} 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  <li>磁盘空间不足</li>
	 *                              </ul>
	 * @see Deflater#DEFAULT_COMPRESSION
	 * @see Deflater#BEST_COMPRESSION
	 * @see Deflater#BEST_SPEED
	 * @see Deflater#NO_COMPRESSION
	 * @since 1.1.0
	 */
	public static void archiveSplit(final File inputFile, final File outputFile, final int level) throws IOException {
		archiveSplit(inputFile, outputFile, DEFAULT_SPLIT_SIZE, level, null);
	}

	/**
	 * 分片压缩文件/目录到 ZIP 文件，支持自定义 ZIP 条目处理器。
	 * <p>将单个文件或目录（递归包含子目录）压缩为分片 ZIP 格式文件，使用默认分片大小，并通过 Consumer 对每个 ZIP 条目进行自定义处理。</p>
	 *
	 * @param inputFile     要压缩的文件或目录，必须存在且可读
	 * @param outputFile    输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param entryConsumer ZIP 条目处理器，可为 null
	 * @throws NullPointerException 当 {@code inputFile} 或 {@code outputFile} 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  <li>磁盘空间不足</li>
	 *                              </ul>
	 * @since 1.1.0
	 */
	public static void archiveSplit(final File inputFile, final File outputFile,
	                                 final Consumer<ZipArchiveEntry> entryConsumer) throws IOException {
		archiveSplit(inputFile, outputFile, DEFAULT_SPLIT_SIZE, Deflater.DEFAULT_COMPRESSION,
			entryConsumer);
	}

	/**
	 * 分片压缩文件/目录到 ZIP 文件，指定压缩级别和自定义 ZIP 条目处理器。
	 * <p>将单个文件或目录（递归包含子目录）压缩为分片 ZIP 格式文件，使用默认分片大小和指定的压缩级别，并通过 Consumer 对每个 ZIP 条目进行自定义处理。</p>
	 *
	 * @param inputFile     要压缩的文件或目录，必须存在且可读
	 * @param outputFile    输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param level         压缩级别（0-9），0 表示无压缩，9 表示最高压缩
	 * @param entryConsumer ZIP 条目处理器，可为 null
	 * @throws NullPointerException 当 {@code inputFile} 或 {@code outputFile} 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  <li>磁盘空间不足</li>
	 *                              </ul>
	 * @see Deflater#DEFAULT_COMPRESSION
	 * @see Deflater#BEST_COMPRESSION
	 * @see Deflater#BEST_SPEED
	 * @see Deflater#NO_COMPRESSION
	 * @since 1.1.0
	 */
	public static void archiveSplit(final File inputFile, final File outputFile, final int level,
	                                 final Consumer<ZipArchiveEntry> entryConsumer) throws IOException {
		archiveSplit(inputFile, outputFile, DEFAULT_SPLIT_SIZE, level, entryConsumer);
	}

	/**
	 * 分片压缩文件/目录到 ZIP 文件，指定分片大小。
	 * <p>将单个文件或目录（递归包含子目录）压缩为分片 ZIP 格式文件，使用指定的分片大小。</p>
	 *
	 * @param inputFile    要压缩的文件或目录，必须存在且可读
	 * @param outputFile   输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param zipSplitSize 分片大小（字节），会被限制在最小值和最大值之间
	 * @throws NullPointerException 当 {@code inputFile} 或 {@code outputFile} 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  <li>磁盘空间不足</li>
	 *                              </ul>
	 * @since 1.1.0
	 */
	public static void archiveSplit(final File inputFile, final File outputFile, final long zipSplitSize) throws IOException {
		archiveSplit(inputFile, outputFile, zipSplitSize, Deflater.DEFAULT_COMPRESSION, null);
	}

	/**
	 * 分片压缩文件/目录到 ZIP 文件，指定分片大小和压缩级别。
	 * <p>将单个文件或目录（递归包含子目录）压缩为分片 ZIP 格式文件，使用指定的分片大小和压缩级别。</p>
	 *
	 * @param inputFile    要压缩的文件或目录，必须存在且可读
	 * @param outputFile   输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param zipSplitSize 分片大小（字节），会被限制在最小值和最大值之间
	 * @param level        压缩级别（0-9），0 表示无压缩，9 表示最高压缩
	 * @throws NullPointerException 当 {@code inputFile} 或 {@code outputFile} 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  <li>磁盘空间不足</li>
	 *                              </ul>
	 * @see Deflater#DEFAULT_COMPRESSION
	 * @see Deflater#BEST_COMPRESSION
	 * @see Deflater#BEST_SPEED
	 * @see Deflater#NO_COMPRESSION
	 * @since 1.1.0
	 */
	public static void archiveSplit(final File inputFile, final File outputFile, final long zipSplitSize, final int level) throws IOException {
		archiveSplit(inputFile, outputFile, zipSplitSize, level, null);
	}

	/**
	 * 分片压缩文件/目录到 ZIP 文件，指定分片大小和自定义 ZIP 条目处理器。
	 * <p>将单个文件或目录（递归包含子目录）压缩为分片 ZIP 格式文件，使用指定的分片大小，并通过 Consumer 对每个 ZIP 条目进行自定义处理。</p>
	 *
	 * @param inputFile     要压缩的文件或目录，必须存在且可读
	 * @param outputFile    输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param zipSplitSize  分片大小（字节），会被限制在最小值和最大值之间
	 * @param entryConsumer ZIP 条目处理器，可为 null
	 * @throws NullPointerException 当 {@code inputFile} 或 {@code outputFile} 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  <li>磁盘空间不足</li>
	 *                              </ul>
	 * @since 1.1.0
	 */
	public static void archiveSplit(final File inputFile, final File outputFile, final long zipSplitSize,
	                                 final Consumer<ZipArchiveEntry> entryConsumer) throws IOException {
		archiveSplit(inputFile, outputFile, zipSplitSize, Deflater.DEFAULT_COMPRESSION, entryConsumer);
	}

	/**
	 * 分片压缩文件/目录到 ZIP 文件，指定分片大小、压缩级别和自定义 ZIP 条目处理器。
	 * <p>将单个文件或目录（递归包含子目录）压缩为分片 ZIP 格式文件，使用指定的分片大小和压缩级别，并通过 Consumer 对每个 ZIP 条目进行自定义处理。</p>
	 *
	 * @param inputFile     要压缩的文件或目录，必须存在且可读
	 * @param outputFile    输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param zipSplitSize  分片大小（字节），会被限制在最小值和最大值之间
	 * @param level         压缩级别（0-9），0 表示无压缩，9 表示最高压缩
	 * @param entryConsumer ZIP 条目处理器，可为 null
	 * @throws NullPointerException 当 {@code inputFile} 或 {@code outputFile} 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  <li>磁盘空间不足</li>
	 *                              </ul>
	 * @see Deflater#DEFAULT_COMPRESSION
	 * @see Deflater#BEST_COMPRESSION
	 * @see Deflater#BEST_SPEED
	 * @see Deflater#NO_COMPRESSION
	 * @since 1.1.0
	 */
	public static void archiveSplit(final File inputFile, final File outputFile, final long zipSplitSize,
	                                 final int level, final Consumer<ZipArchiveEntry> entryConsumer) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputFile,
			Math.min(Math.max(zipSplitSize, MIN_SPLIT_SIZE), MAX_SPLIT_SIZE))) {
			zipArchiveOutputStream.setLevel(level);
			ArchiveUtils.archive(inputFile, zipArchiveOutputStream, entryConsumer);
		}
	}

	/**
	 * 批量分片压缩文件/目录到 ZIP 文件。
	 * <p>将多个文件或目录（递归包含子目录）压缩为分片 ZIP 格式文件，使用默认分片大小。</p>
	 *
	 * @param inputFiles 要压缩的文件集合，必须非空且所有文件必须存在
	 * @param outputFile 输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @throws NullPointerException     当 {@code outputFile} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输出文件不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 1.1.0
	 */
	public static void archiveSplit(final Collection<File> inputFiles, final File outputFile) throws IOException {
		archiveSplit(inputFiles, outputFile, DEFAULT_SPLIT_SIZE, Deflater.DEFAULT_COMPRESSION,
			null);
	}

	/**
	 * 批量分片压缩文件/目录到 ZIP 文件，指定压缩级别。
	 * <p>将多个文件或目录（递归包含子目录）压缩为分片 ZIP 格式文件，使用默认分片大小和指定的压缩级别。</p>
	 *
	 * @param inputFiles 要压缩的文件集合，必须非空且所有文件必须存在
	 * @param outputFile 输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param level      压缩级别（0-9），0 表示无压缩，9 表示最高压缩
	 * @throws NullPointerException     当 {@code outputFile} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输出文件不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @see Deflater#DEFAULT_COMPRESSION
	 * @see Deflater#BEST_COMPRESSION
	 * @see Deflater#BEST_SPEED
	 * @see Deflater#NO_COMPRESSION
	 * @since 1.1.0
	 */
	public static void archiveSplit(final Collection<File> inputFiles, final File outputFile, final int level) throws IOException {
		archiveSplit(inputFiles, outputFile, DEFAULT_SPLIT_SIZE, level, null);
	}

	/**
	 * 批量分片压缩文件/目录到 ZIP 文件，支持自定义 ZIP 条目处理器。
	 * <p>将多个文件或目录（递归包含子目录）压缩为分片 ZIP 格式文件，使用默认分片大小，并通过 Consumer 对每个 ZIP 条目进行自定义处理。</p>
	 *
	 * @param inputFiles    要压缩的文件集合，必须非空且所有文件必须存在
	 * @param outputFile    输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param entryConsumer ZIP 条目处理器，可为 null
	 * @throws NullPointerException     当 {@code outputFile} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输出文件不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 1.1.0
	 */
	public static void archiveSplit(final Collection<File> inputFiles, final File outputFile,
	                                 final Consumer<ZipArchiveEntry> entryConsumer) throws IOException {
		archiveSplit(inputFiles, outputFile, DEFAULT_SPLIT_SIZE, Deflater.DEFAULT_COMPRESSION,
			entryConsumer);
	}

	/**
	 * 批量分片压缩文件/目录到 ZIP 文件，指定压缩级别和自定义 ZIP 条目处理器。
	 * <p>将多个文件或目录（递归包含子目录）压缩为分片 ZIP 格式文件，使用默认分片大小和指定的压缩级别，并通过 Consumer 对每个 ZIP 条目进行自定义处理。</p>
	 *
	 * @param inputFiles    要压缩的文件集合，必须非空且所有文件必须存在
	 * @param outputFile    输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param level         压缩级别（0-9），0 表示无压缩，9 表示最高压缩
	 * @param entryConsumer ZIP 条目处理器，可为 null
	 * @throws NullPointerException     当 {@code outputFile} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输出文件不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @see Deflater#DEFAULT_COMPRESSION
	 * @see Deflater#BEST_COMPRESSION
	 * @see Deflater#BEST_SPEED
	 * @see Deflater#NO_COMPRESSION
	 * @since 1.1.0
	 */
	public static void archiveSplit(final Collection<File> inputFiles, final File outputFile, final int level,
	                                 final Consumer<ZipArchiveEntry> entryConsumer) throws IOException {
		archiveSplit(inputFiles, outputFile, DEFAULT_SPLIT_SIZE, level, entryConsumer);
	}

	/**
	 * 批量分片压缩文件/目录到 ZIP 文件，指定分片大小。
	 * <p>将多个文件或目录（递归包含子目录）压缩为分片 ZIP 格式文件，使用指定的分片大小。</p>
	 *
	 * @param inputFiles   要压缩的文件集合，必须非空且所有文件必须存在
	 * @param outputFile   输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param zipSplitSize 分片大小（字节），会被限制在最小值和最大值之间
	 * @throws NullPointerException     当 {@code outputFile} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输出文件不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 1.1.0
	 */
	public static void archiveSplit(final Collection<File> inputFiles, final File outputFile, final long zipSplitSize) throws IOException {
		archiveSplit(inputFiles, outputFile, zipSplitSize, Deflater.DEFAULT_COMPRESSION, null);
	}

	/**
	 * 批量分片压缩文件/目录到 ZIP 文件，指定分片大小和压缩级别。
	 * <p>将多个文件或目录（递归包含子目录）压缩为分片 ZIP 格式文件，使用指定的分片大小和压缩级别。</p>
	 *
	 * @param inputFiles   要压缩的文件集合，必须非空且所有文件必须存在
	 * @param outputFile   输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param zipSplitSize 分片大小（字节），会被限制在最小值和最大值之间
	 * @param level        压缩级别（0-9），0 表示无压缩，9 表示最高压缩
	 * @throws NullPointerException     当 {@code outputFile} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输出文件不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @see Deflater#DEFAULT_COMPRESSION
	 * @see Deflater#BEST_COMPRESSION
	 * @see Deflater#BEST_SPEED
	 * @see Deflater#NO_COMPRESSION
	 * @since 1.1.0
	 */
	public static void archiveSplit(final Collection<File> inputFiles, final File outputFile, final long zipSplitSize,
	                                 final int level) throws IOException {
		archiveSplit(inputFiles, outputFile, zipSplitSize, level, null);
	}

	/**
	 * 批量分片压缩文件/目录到 ZIP 文件，指定分片大小和自定义 ZIP 条目处理器。
	 * <p>将多个文件或目录（递归包含子目录）压缩为分片 ZIP 格式文件，使用指定的分片大小，并通过 Consumer 对每个 ZIP 条目进行自定义处理。</p>
	 *
	 * @param inputFiles    要压缩的文件集合，必须非空且所有文件必须存在
	 * @param outputFile    输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param zipSplitSize  分片大小（字节），会被限制在最小值和最大值之间
	 * @param entryConsumer ZIP 条目处理器，可为 null
	 * @throws NullPointerException     当 {@code outputFile} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输出文件不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 1.1.0
	 */
	public static void archiveSplit(final Collection<File> inputFiles, final File outputFile, final long zipSplitSize,
	                                 final Consumer<ZipArchiveEntry> entryConsumer) throws IOException {
		archiveSplit(inputFiles, outputFile, zipSplitSize, Deflater.DEFAULT_COMPRESSION, entryConsumer);
	}

	/**
	 * 批量分片压缩文件/目录到 ZIP 文件，指定分片大小、压缩级别和自定义 ZIP 条目处理器。
	 * <p>将多个文件或目录（递归包含子目录）压缩为分片 ZIP 格式文件，使用指定的分片大小和压缩级别，并通过 Consumer 对每个 ZIP 条目进行自定义处理。</p>
	 *
	 * @param inputFiles    要压缩的文件集合，必须非空且所有文件必须存在
	 * @param outputFile    输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param zipSplitSize  分片大小（字节），会被限制在最小值和最大值之间
	 * @param level         压缩级别（0-9），0 表示无压缩，9 表示最高压缩
	 * @param entryConsumer ZIP 条目处理器，可为 null
	 * @throws NullPointerException     当 {@code outputFile} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输出文件不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @see Deflater#DEFAULT_COMPRESSION
	 * @see Deflater#BEST_COMPRESSION
	 * @see Deflater#BEST_SPEED
	 * @see Deflater#NO_COMPRESSION
	 * @since 1.1.0
	 */
	public static void archiveSplit(final Collection<File> inputFiles, final File outputFile, final long zipSplitSize,
	                                 final int level, final Consumer<ZipArchiveEntry> entryConsumer) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notEmpty(inputFiles, "inputFiles 不可为空");

		FileUtils.forceMkdirParent(outputFile);

		try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputFile,
			Math.min(Math.max(zipSplitSize, MIN_SPLIT_SIZE), MAX_SPLIT_SIZE))) {
			zipArchiveOutputStream.setLevel(level);
			ArchiveUtils.archive(inputFiles, zipArchiveOutputStream, entryConsumer);
		}
	}

	/**
	 * 批量分片压缩文件到 ZIP 文件，使用自定义 ZIP 参数。
	 * <p>将多个文件（仅文件，不支持目录）压缩为分片 ZIP 格式文件，使用默认分片大小和指定的 ZipParameters 进行自定义配置。</p>
	 *
	 * @param inputFiles 要压缩的文件列表，必须非空且所有文件必须存在
	 * @param outputFile 输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param parameters ZIP 参数配置，必须非 null
	 * @throws NullPointerException     当 {@code inputFiles}、{@code outputFile} 或 {@code parameters} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空列表或包含不存在的文件时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输入文件不存在或不可读</li>
	 *                                      <li>输出文件不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 1.1.0
	 */
	public static void archiveSplit(final List<File> inputFiles, final File outputFile, final ZipParameters parameters) throws IOException {
		archiveSplit(inputFiles, outputFile, DEFAULT_SPLIT_SIZE, parameters);
	}

	/**
	 * 批量分片压缩文件到 ZIP 文件，指定分片大小和自定义 ZIP 参数。
	 * <p>将多个文件（仅文件，不支持目录）压缩为分片 ZIP 格式文件，使用指定的分片大小和 ZipParameters 进行自定义配置。</p>
	 *
	 * @param inputFiles  要压缩的文件列表，必须非空且所有文件必须存在
	 * @param outputFile  输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param splitLength 分片长度（字节），会被限制在最小值之上
	 * @param parameters  ZIP 参数配置，必须非 null
	 * @throws NullPointerException     当 {@code inputFiles}、{@code outputFile} 或 {@code parameters} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空列表或包含不存在的文件时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输入文件不存在或不可读</li>
	 *                                      <li>输出文件不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 1.1.0
	 */
	public static void archiveSplit(final List<File> inputFiles, final File outputFile, final long splitLength,
	                                 final ZipParameters parameters) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notEmpty(inputFiles, "inputFiles 不可为空");
		Validate.notNull(parameters, "parameters 不可为 null");
		Validate.isTrue(inputFiles.stream().allMatch(FileUtils::existFile),
			"inputFiles 中存在为 null 或不存在的文件");

		FileUtils.forceMkdirParent(outputFile);

		if (outputFile.exists()) {
			FileUtils.forceDelete(outputFile);
		}

		try (net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(outputFile)) {
			zipFile.createSplitZipFile(inputFiles, parameters, true,
				Math.max(MIN_SPLIT_SIZE, splitLength));
		}
	}


	/**
	 * 批量分片压缩文件到加密 ZIP 文件。
	 * <p>将多个文件（仅文件，不支持目录）压缩为加密的分片 ZIP 格式文件，使用默认分片大小和默认加密参数。</p>
	 *
	 * @param inputFiles 要压缩的文件列表，必须非空且所有文件必须存在
	 * @param outputFile 输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param password   ZIP 文件密码，必须非空
	 * @throws NullPointerException     当 {@code inputFiles}、{@code outputFile} 或 {@code password} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空列表或 {@code password} 为空时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输入文件不存在或不可读</li>
	 *                                      <li>输出文件不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 1.1.0
	 */
	public static void archiveSplit(final List<File> inputFiles, final File outputFile, final String password) throws IOException {
		archiveSplit(inputFiles, outputFile, password, DEFAULT_SPLIT_SIZE, new ZipParameters());
	}

	/**
	 * 批量分片压缩文件到加密 ZIP 文件，指定分片长度。
	 * <p>将多个文件（仅文件，不支持目录）压缩为加密的分片 ZIP 格式文件，使用指定的分片长度和默认加密参数。</p>
	 *
	 * @param inputFiles  要压缩的文件列表，必须非空且所有文件必须存在
	 * @param outputFile  输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param password    ZIP 文件密码，必须非空
	 * @param splitLength 分片长度（字节），会被限制在最小值之上
	 * @throws NullPointerException     当 {@code inputFiles}、{@code outputFile} 或 {@code password} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空列表或 {@code password} 为空时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输入文件不存在或不可读</li>
	 *                                      <li>输出文件不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 1.1.0
	 */
	public static void archiveSplit(final List<File> inputFiles, final File outputFile, final String password,
	                                 final long splitLength) throws IOException {
		archiveSplit(inputFiles, outputFile, password, splitLength, new ZipParameters());
	}

	/**
	 * 批量分片压缩文件到加密 ZIP 文件，指定分片长度和自定义 ZIP 参数。
	 * <p>将多个文件（仅文件，不支持目录）压缩为加密的分片 ZIP 格式文件，使用指定的分片长度和 ZipParameters 进行自定义配置。</p>
	 *
	 * @param inputFiles  要压缩的文件列表，必须非空且所有文件必须存在
	 * @param outputFile  输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param password    ZIP 文件密码，必须非空
	 * @param splitLength 分片长度（字节），会被限制在最小值之上
	 * @param parameters  ZIP 参数配置，必须非 null（会自动设置加密标志和默认加密方法）
	 * @throws NullPointerException     当 {@code inputFiles}、{@code outputFile}、{@code password} 或 {@code parameters} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空列表或 {@code password} 为空时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输入文件不存在或不可读</li>
	 *                                      <li>输出文件不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 1.1.0
	 */
	public static void archiveSplit(final List<File> inputFiles, final File outputFile, final String password,
	                                 final long splitLength, final ZipParameters parameters) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notBlank(password, "password 不可为空");
		Validate.notEmpty(inputFiles, "inputFiles 不可为空");
		Validate.notNull(parameters, "parameters 不可为 null");
		Validate.isTrue(inputFiles.stream().allMatch(FileUtils::existFile),
			"inputFiles 中存在为 null 或不存在的文件");

		FileUtils.forceMkdirParent(outputFile);

		if (outputFile.exists()) {
			FileUtils.forceDelete(outputFile);
		}

		parameters.setEncryptFiles(true);
		if (Objects.isNull(parameters.getEncryptionMethod()) || parameters.getEncryptionMethod() == EncryptionMethod.NONE) {
			parameters.setEncryptionMethod(EncryptionMethod.AES);
		}

		try (net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(outputFile, password.toCharArray())) {
			zipFile.createSplitZipFile(inputFiles, parameters, true,
				Math.max(MIN_SPLIT_SIZE, splitLength));
		}
	}

	/**
	 * 分片压缩文件/目录到 ZIP 文件，使用自定义 ZIP 参数。
	 * <p>将单个文件或目录（递归包含子目录）压缩为分片 ZIP 格式文件，使用默认分片大小和指定的 ZipParameters 进行自定义配置。</p>
	 *
	 * @param inputFile  要压缩的文件或目录，必须存在且可读
	 * @param outputFile 输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param parameters ZIP 参数配置，必须非 null
	 * @throws NullPointerException 当 {@code inputFile}、{@code outputFile} 或 {@code parameters} 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  <li>磁盘空间不足</li>
	 *                              </ul>
	 * @since 1.1.0
	 */
	public static void archiveSplit(final File inputFile, final File outputFile, final ZipParameters parameters) throws IOException {
		archiveSplit(inputFile, outputFile, DEFAULT_SPLIT_SIZE, parameters);
	}

	/**
	 * 分片压缩文件/目录到 ZIP 文件，指定分片长度和自定义 ZIP 参数。
	 * <p>将单个文件或目录（递归包含子目录）压缩为分片 ZIP 格式文件，使用指定的分片长度和 ZipParameters 进行自定义配置。</p>
	 *
	 * @param inputFile   要压缩的文件或目录，必须存在且可读
	 * @param outputFile  输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param splitLength 分片长度（字节），会被限制在最小值之上
	 * @param parameters  ZIP 参数配置，必须非 null
	 * @throws NullPointerException 当 {@code inputFile}、{@code outputFile} 或 {@code parameters} 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  <li>磁盘空间不足</li>
	 *                              </ul>
	 * @since 1.1.0
	 */
	public static void archiveSplit(final File inputFile, final File outputFile, final long splitLength,
	                                 final ZipParameters parameters) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		FileUtils.check(inputFile, "inputFile 不可为 null");
		Validate.notNull(parameters, "parameters 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		if (outputFile.exists()) {
			FileUtils.forceDelete(outputFile);
		}

		try (net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(outputFile)) {
			if (inputFile.isDirectory()) {
				zipFile.createSplitZipFileFromFolder(inputFile, parameters, true,
					Math.max(MIN_SPLIT_SIZE, splitLength));
			} else {
				zipFile.createSplitZipFile(List.of(inputFile), parameters, true,
					Math.max(MIN_SPLIT_SIZE, splitLength));
			}
		}
	}

	/**
	 * 分片压缩文件/目录到加密 ZIP 文件。
	 * <p>将单个文件或目录（递归包含子目录）压缩为加密的分片 ZIP 格式文件，使用默认分片大小和默认加密参数。</p>
	 *
	 * @param inputFile  要压缩的文件或目录，必须存在且可读
	 * @param outputFile 输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param password   ZIP 文件密码，必须非空
	 * @throws NullPointerException     当 {@code inputFile}、{@code outputFile} 或 {@code password} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code password} 为空时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输入文件不存在或不可读</li>
	 *                                      <li>输出文件不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 1.1.0
	 */
	public static void archiveSplit(final File inputFile, final File outputFile, final String password) throws IOException {
		archiveSplit(inputFile, outputFile, password, DEFAULT_SPLIT_SIZE, new ZipParameters());
	}

	/**
	 * 分片压缩文件/目录到加密 ZIP 文件，指定分片长度。
	 * <p>将单个文件或目录（递归包含子目录）压缩为加密的分片 ZIP 格式文件，使用指定的分片长度和默认加密参数。</p>
	 *
	 * @param inputFile   要压缩的文件或目录，必须存在且可读
	 * @param outputFile  输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param password    ZIP 文件密码，必须非空
	 * @param splitLength 分片长度（字节），会被限制在最小值之上
	 * @throws NullPointerException     当 {@code inputFile}、{@code outputFile} 或 {@code password} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code password} 为空时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输入文件不存在或不可读</li>
	 *                                      <li>输出文件不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 1.1.0
	 */
	public static void archiveSplit(final File inputFile, final File outputFile, final String password,
	                                 final long splitLength) throws IOException {
		archiveSplit(inputFile, outputFile, password, splitLength, new ZipParameters());
	}

	/**
	 * 分片压缩文件/目录到加密 ZIP 文件，指定分片长度和自定义 ZIP 参数。
	 * <p>将单个文件或目录（递归包含子目录）压缩为加密的分片 ZIP 格式文件，使用指定的分片长度和 ZipParameters 进行自定义配置。</p>
	 *
	 * @param inputFile   要压缩的文件或目录，必须存在且可读
	 * @param outputFile  输出 ZIP 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param password    ZIP 文件密码，必须非空
	 * @param splitLength 分片长度（字节），会被限制在最小值之上
	 * @param parameters  ZIP 参数配置，必须非 null（会自动设置加密标志和默认加密方法）
	 * @throws NullPointerException     当 {@code inputFile}、{@code outputFile}、{@code password} 或 {@code parameters} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code password} 为空时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>输入文件不存在或不可读</li>
	 *                                      <li>输出文件不可写</li>
	 *                                      <li>压缩过程中发生 I/O 错误</li>
	 *                                      <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 1.1.0
	 */
	public static void archiveSplit(final File inputFile, final File outputFile, final String password,
	                                 final long splitLength, final ZipParameters parameters) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		FileUtils.check(inputFile, "inputFile 不可为 null");
		Validate.notBlank(password, "password 不可为空");
		Validate.notNull(parameters, "parameters 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		if (outputFile.exists()) {
			FileUtils.forceDelete(outputFile);
		}

		parameters.setEncryptFiles(true);
		if (Objects.isNull(parameters.getEncryptionMethod()) || parameters.getEncryptionMethod() == EncryptionMethod.NONE) {
			parameters.setEncryptionMethod(EncryptionMethod.AES);
		}

		try (net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(outputFile, password.toCharArray())) {
			if (inputFile.isDirectory()) {
				zipFile.createSplitZipFileFromFolder(inputFile, parameters, true,
					Math.max(MIN_SPLIT_SIZE, splitLength));
			} else {
				zipFile.createSplitZipFile(List.of(inputFile), parameters, true,
					Math.max(MIN_SPLIT_SIZE, splitLength));
			}
		}
	}

	/**
	 * 递归添加目录到 ZIP 流。
	 * <p>将目录及其所有子目录和文件递归添加到 ZIP 输出流中，保持原始目录结构。</p>
	 *
	 * @param inputDir               要添加的目录，需可读
	 * @param zipArchiveOutputStream ZIP 输出流对象，必须已初始化且可写
	 * @param parent                 父目录相对路径（用于构建 ZIP 条目路径），可为 null
	 * @throws NullPointerException 当 inputDir 或 zipArchiveOutputStream 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>目录不可读</li>
	 *                                  <li>输出流不可写或已关闭</li>
	 *                                  <li>添加过程中发生 I/O 错误</li>
	 *                              </ul>
	 * @since 1.0.0
	 * @deprecated 请使用 {@link ArchiveUtils#archive(File, ArchiveOutputStream, Consumer)}
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	protected static void addDir(File inputDir, ZipArchiveOutputStream zipArchiveOutputStream, String parent) throws IOException {
		String entryName = inputDir.getName();
		if (StringUtils.isNotBlank(parent)) {
			if (parent.endsWith(CompressConstants.PATH_SEPARATOR)) {
				entryName = parent + inputDir.getName() + CompressConstants.PATH_SEPARATOR;
			} else {
				entryName = parent + CompressConstants.PATH_SEPARATOR + inputDir.getName() + CompressConstants.PATH_SEPARATOR;
			}
		}
		ZipArchiveEntry archiveEntry = new ZipArchiveEntry(inputDir, entryName);
		zipArchiveOutputStream.putArchiveEntry(archiveEntry);
		zipArchiveOutputStream.closeArchiveEntry();

		File[] childFiles = ArrayUtils.nullToEmpty(inputDir.listFiles(), File[].class);
		for (File childFile : childFiles) {
			if (childFile.isDirectory()) {
				addDir(childFile, zipArchiveOutputStream, entryName);
			} else {
				addFile(childFile, zipArchiveOutputStream, entryName);
			}
		}
	}

	/**
	 * 添加单个文件到 ZIP 流。
	 * <p>将单个文件添加到 ZIP 输出流中，可指定父目录路径。</p>
	 *
	 * @param inputFile              要添加的文件，必须存在且可读
	 * @param zipArchiveOutputStream ZIP 输出流对象，必须已初始化且可写
	 * @param parent                 父目录相对路径（用于构建 ZIP 条目路径），可为 null
	 * @throws NullPointerException 当 inputFile 或 zipArchiveOutputStream 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>文件不存在或不可读（例如抛出 {@code FileNotFoundException}）</li>
	 *                                  <li>输出流不可写或已关闭</li>
	 *                                  <li>添加过程中发生 I/O 错误</li>
	 *                              </ul>
	 * @since 1.0.0
	 * @deprecated 请使用 {@link ArchiveUtils#archive(File, ArchiveOutputStream, Consumer)}
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	protected static void addFile(File inputFile, ZipArchiveOutputStream zipArchiveOutputStream, String parent) throws IOException {
		try (InputStream inputStream = FileUtils.openBufferedFileChannelInputStream(inputFile)) {
			String entryName = inputFile.getName();
			if (StringUtils.isNotBlank(parent)) {
				if (parent.endsWith(CompressConstants.PATH_SEPARATOR)) {
					entryName = parent + inputFile.getName();
				} else {
					entryName = parent + CompressConstants.PATH_SEPARATOR + inputFile.getName();
				}
			}
			ZipArchiveEntry archiveEntry = new ZipArchiveEntry(inputFile, entryName);
			zipArchiveOutputStream.putArchiveEntry(archiveEntry);
			inputStream.transferTo(zipArchiveOutputStream);
			zipArchiveOutputStream.closeArchiveEntry();
		}
	}
}