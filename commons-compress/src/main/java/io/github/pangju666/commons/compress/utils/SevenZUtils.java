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

import io.github.pangju666.commons.compress.io.resource.SevenZResource;
import io.github.pangju666.commons.compress.lang.CompressConstants;
import io.github.pangju666.commons.io.lang.IOConstants;
import io.github.pangju666.commons.io.utils.FileUtils;
import org.apache.commons.compress.archivers.sevenz.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SeekableByteChannel;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 7z 压缩/解压工具类。
 * <p>基于 Apache Commons Compress 提供 7z 的压缩与解压能力。</p>
 *
 * <h3>核心特性</h3>
 * <ul>
 * <li><strong>多输入源</strong>：支持 {@code SevenZFile} 和 {@code SevenZResource}。</li>
 * <li><strong>多输出目标</strong>：支持文件、{@code SeekableByteChannel}、{@code SevenZOutputFile}。</li>
 * <li><strong>目录递归</strong>：保持原始目录层级结构进行压缩。</li>
 * <li><strong>加密支持</strong>：支持创建和读取加密的 7z 文件。</li>
 * <li><strong>性能优化</strong>：流式传输与缓冲处理，适合大文件。</li>
 * <li><strong>资源管理</strong>：使用 try-with-resources 自动释放资源。</li>
 * </ul>
 *
 * <h3>线程安全</h3>
 * <p>类本身无共享状态，方法均为静态；并发处理不同文件/目录是安全的。对同一路径或同一输出文件并发写入可能发生冲突或覆盖。</p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 1) 压缩单个文件到 .7z
 * SevenZUtils.archive(new File("input.txt"), new File("archive.7z"));
 *
 * // 2) 压缩加密的 7z 文件
 * SevenZUtils.archive(new File("input.txt"), new File("encrypted.7z"), "password");
 *
 * // 3) 压缩目录到 SevenZOutputFile（不会自动关闭传入对象）
 * try (SevenZOutputFile szf = new SevenZOutputFile(new File("archive.7z"))) {
 *     SevenZUtils.archive(new File("inputDir"), szf);
 * }
 *
 * // 4) 批量压缩多个文件/目录到 .7z
 * List<File> inputs = List.of(new File("a.txt"), new File("b"), new File("c"));
 * SevenZUtils.archive(inputs, new File("batch.7z"));
 *
 * // 5) 压缩到 SeekableByteChannel
 * try (SeekableByteChannel channel = FileChannel.open(new File("archive.7z").toPath(),
 *         StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
 *     SevenZUtils.archive(new File("input.txt"), channel);
 * }
 *
 * // 6) 压缩到加密的 SeekableByteChannel
 * try (SeekableByteChannel channel = FileChannel.open(new File("encrypted.7z").toPath(),
 *         StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
 *     SevenZUtils.archive(new File("input.txt"), channel, "password");
 * }
 *
 * // 7) 使用 SevenZFile 解压（适合随机访问和大文件）
 * try (SevenZFile zf = SevenZFile.builder().setFile(new File("archive.7z")).get()) {
 *     SevenZUtils.extract(zf, new File("outputDir"));
 * }
 *
 * // 8) 使用 SevenZResource 解压
 * try (SevenZResource resource = new SevenZResource(new File("archive.7z"))) {
 *     SevenZUtils.extract(resource, new File("outputDir"));
 * }
 *
 * }</pre>
 *
 * @author pangju666
 * @see SevenZFile
 * @see SevenZOutputFile
 * @see SevenZResource
 * @since 1.0.0
 */
public class SevenZUtils {
	protected SevenZUtils() {
	}

	/**
	 * 检查指定文件是否为有效的7z压缩格式
	 *
	 * @param file 待检查的文件对象
	 * @return 当且仅当文件存在且检测为7z格式时返回true
	 * @throws NullPointerException 当file参数为null时抛出
	 * @throws IOException          当文件访问发生I/O异常时抛出
	 * @since 1.0.0
	 * @deprecated 请使用{@link SevenZResource} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static boolean is7z(final File file) throws IOException {
		return FileUtils.isMimeType(file, CompressConstants.SEVEN_Z_MIME_TYPE);
	}

	/**
	 * 检查字节数组内容是否为有效的 7z 格式。
	 * <p>基于 Tika 的 MIME 类型检测。</p>
	 *
	 * @param bytes 待检测的字节数组；为 {@code null} 或空数组将返回 {@code false}
	 * @return 当且仅当字节数组非空且检测为 {@code application/x-7z-archiveed} 时返回 {@code true}
	 * @since 1.0.0
	 * @deprecated 请使用{@link SevenZResource} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static boolean is7z(final byte[] bytes) {
		return ArrayUtils.isNotEmpty(bytes) &&
			IOConstants.getDefaultTika().detect(bytes).equals(CompressConstants.SEVEN_Z_MIME_TYPE);
	}

	/**
	 * 检查输入流内容是否为有效的 7z 格式。
	 * <p>基于 Tika 的 MIME 类型检测。</p>
	 *
	 * @param inputStream 待检测的输入流，非空
	 * @return 当且仅当输入流非空且检测为 {@code application/x-7z-archiveed} 时返回 {@code true}
	 * @throws NullPointerException 当 {@code inputStream} 为 {@code null} 时抛出
	 * @throws IOException          当流读取发生 I/O 错误时抛出
	 * @since 1.0.0
	 * @deprecated 请使用{@link SevenZResource} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static boolean is7z(final InputStream inputStream) throws IOException {
		return Objects.nonNull(inputStream) &&
			IOConstants.getDefaultTika().detect(inputStream).equals(CompressConstants.SEVEN_Z_MIME_TYPE);
	}

	/**
	 * 解压缩 7z 文件到指定目录。
	 * <p>将 7z 格式文件解压到指定目录，自动创建不存在的目录结构并保持原始文件层级关系。</p>
	 *
	 * @param inputFile 要解压的 7z 文件，必须存在且可读且为有效 7z 格式（通过 Tika 检测）
	 * @param outputDir 解压目标目录；若不存在则自动创建父目录
	 * @throws NullPointerException     当 {@code inputFile} 或 {@code outputDir} 为 {@code null} 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFile} 不是有效的 7z 格式或 {@code outputDir} 存在但不是目录时抛出
	 * @throws IOException              当输入文件不可读、输出目录不可写、解压过程中发生 I/O 错误或磁盘空间不足时抛出
	 * @since 1.0.0
	 * @deprecated 请使用{@link #extract(SevenZResource, File)} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static void uncompress(final File inputFile, final File outputDir) throws IOException {
		Validate.notNull(inputFile, "inputFile 不可为 null");
		Validate.isTrue(is7z(inputFile), "inputFile 不是7z压缩文件");

		try (SevenZFile sevenZFile = SevenZFile.builder().setFile(inputFile).get()) {
			extract(sevenZFile, outputDir);
		}
	}

	/**
	 * 从 {@code SevenZFile} 对象解压缩到指定目录。
	 * <p>使用已初始化的 SevenZFile 对象将内容解压到指定目录，自动创建不存在的目录结构并保持原始文件层级关系。</p>
	 *
	 * @param sevenZFile 已初始化的 SevenZFile 对象，必须处于可读取状态且不为 null
	 * @param outputDir  解压目标目录，会自动创建不存在的目录结构
	 * @throws NullPointerException     当 {@code sevenZFile} 或 {@code outputDir} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code outputDir} 存在但不是目录时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>sevenZFile 已关闭或不可读</li>
	 *                                  <li>输出目录不可写</li>
	 *                                  <li>解压过程中发生 I/O 错误</li>
	 *                                  <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 1.0.0
	 * @deprecated 请使用{@link #extract(SevenZResource, File)} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static void uncompress(final SevenZFile sevenZFile, final File outputDir) throws IOException {
		extract(sevenZFile, outputDir);
	}

	/**
	 * 压缩文件/目录到 7z 文件。
	 * <p>将单个文件或目录（递归包含子目录）压缩为 7z 格式文件。</p>
	 *
	 * @param inputFile  要压缩的文件或目录，必须存在且可读
	 * @param outputFile 输出 7z 文件路径，会自动创建父目录并覆盖已存在文件
	 * @throws NullPointerException     当 {@code inputFile} 或 {@code outputFile} 为 {@code null} 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFile} 不存在时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>输入文件不可读</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误或磁盘空间不足</li>
	 *                                  </ul>
	 * @since 1.0.0
	 * @deprecated 请使用{@link #archive(File, File)} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static void compress(final File inputFile, final File outputFile) throws IOException {
		archive(inputFile, outputFile);
	}

	/**
	 * 压缩文件/目录到 SevenZOutputFile 对象。
	 * <p>将单个文件或目录（递归包含子目录）压缩到已初始化的 SevenZOutputFile 对象中。</p>
	 *
	 * @param inputFile        要压缩的文件或目录，必须存在且可读
	 * @param sevenZOutputFile 已初始化的 SevenZOutputFile 对象，必须处于可写入状态且不为 null
	 * @throws NullPointerException     当 {@code inputFile} 或 {@code sevenZOutputFile} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFile} 不存在时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>输入文件不可读</li>
	 *                                  <li>sevenZOutputFile 已关闭或不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 1.0.0
	 * @deprecated 请使用{@link #archive(File, SevenZOutputFile)} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static void compress(final File inputFile, final SevenZOutputFile sevenZOutputFile) throws IOException {
		archive(inputFile, sevenZOutputFile);
	}

	/**
	 * 压缩多个文件/目录到 7z 文件。
	 * <p>将多个文件或目录（递归包含子目录）压缩为单个 7z 格式文件。</p>
	 *
	 * @param inputFiles 要压缩的文件/目录集合，必须非空且所有元素必须存在
	 * @param outputFile 输出 7z 文件路径，会自动创建父目录并覆盖已存在文件
	 * @throws NullPointerException     当 {@code inputFiles} 或 {@code outputFile} 为 {@code null} 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空集合或集合中存在不存在的文件时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>集合中存在不可读的文件</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误或磁盘空间不足</li>
	 *                                  </ul>
	 * @since 1.0.0
	 * @deprecated 请使用{@link #archive(Collection, File)} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static void compress(final Collection<File> inputFiles, final File outputFile) throws IOException {
		archive(inputFiles, outputFile);
	}

	/**
	 * 压缩多个文件/目录到 SevenZOutputFile 对象。
	 * <p>将多个文件或目录（递归包含子目录）压缩到已初始化的 SevenZOutputFile 对象中。</p>
	 *
	 * @param inputFiles       要压缩的文件/目录集合，必须非空且所有元素必须存在
	 * @param sevenZOutputFile 已初始化的 SevenZOutputFile 对象，必须处于可写入状态且不为 null
	 * @throws NullPointerException     当 {@code inputFiles} 或 {@code sevenZOutputFile} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空集合或集合中存在不存在的文件时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>集合中存在不可读的文件</li>
	 *                                  <li>sevenZOutputFile 已关闭或不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  </ul>
	 * @since 1.0.0
	 * @deprecated 请使用{@link #archive(Collection, SevenZOutputFile)} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static void compress(final Collection<File> inputFiles, final SevenZOutputFile sevenZOutputFile) throws IOException {
		archive(inputFiles, sevenZOutputFile);
	}

	/**
	 * 从 {@code SevenZResource} 对象解压缩到指定目录。
	 * <p>通过 SevenZResource 打开 SevenZFile 并将内容解压到指定目录，自动创建不存在的目录结构并保持原始文件层级关系。</p>
	 *
	 * @param resource  7z 资源对象，必须非 null
	 * @param outputDir 解压目标目录，会自动创建不存在的目录结构
	 * @throws NullPointerException 当 {@code resource} 或 {@code outputDir} 为 null 时抛出
	 * @throws IOException          当资源已关闭、输出目录不可写、解压过程中发生 I/O 错误或磁盘空间不足时抛出
	 * @since 2.1.0
	 */
	public static void extract(final SevenZResource resource, final File outputDir) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");

		try (SevenZFile sevenZFile = resource.openSevenZFile()) {
			extract(sevenZFile, outputDir);
		}
	}

	/**
	 * 从 {@code SevenZFile} 对象解压缩到指定目录。
	 * <p>使用已初始化的 SevenZFile 对象将内容解压到指定目录，自动创建不存在的目录结构并保持原始文件层级关系。</p>
	 *
	 * @param sevenZFile 已初始化的 SevenZFile 对象，必须处于可读取状态且不为 null
	 * @param outputDir  解压目标目录，会自动创建不存在的目录结构
	 * @throws NullPointerException     当 {@code sevenZFile} 或 {@code outputDir} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code outputDir} 存在但不是目录时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>sevenZFile 已关闭或不可读</li>
	 *                                  <li>输出目录不可写</li>
	 *                                  <li>解压过程中发生 I/O 错误</li>
	 *                                  <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 2.1.0
	 */
	public static void extract(final SevenZFile sevenZFile, final File outputDir) throws IOException {
		Validate.notNull(sevenZFile, "sevenZFile 不可为 null");

		ArchiveUtils.extract(sevenZFile.getEntries().iterator(), outputDir, sevenZFile::getInputStream);
	}

	/**
	 * 压缩文件/目录到 7z 文件。
	 * <p>将单个文件或目录（递归包含子目录）压缩为 7z 格式文件。</p>
	 *
	 * @param inputFile  要压缩的文件或目录，必须存在且可读
	 * @param outputFile 输出 7z 文件路径，会自动创建父目录并覆盖已存在文件
	 * @throws NullPointerException     当 {@code inputFile} 或 {@code outputFile} 为 {@code null} 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFile} 不存在时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>输入文件不可读</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误或磁盘空间不足</li>
	 *                                  </ul>
	 * @since 2.1.0
	 */
	public static void archive(final File inputFile, final File outputFile) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile)) {
			archive(inputFile, sevenZOutputFile);
		}
	}

	/**
	 * 压缩文件/目录到 7z 文件，指定压缩方法。
	 * <p>将单个文件或目录（递归包含子目录）压缩为 7z 格式文件，使用指定的压缩方法。</p>
	 *
	 * @param inputFile  要压缩的文件或目录，必须存在且可读
	 * @param outputFile 输出 7z 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param method     压缩方法，必须非 null
	 * @throws NullPointerException     当 {@code inputFile}、{@code outputFile} 或 {@code method} 为 {@code null} 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFile} 不存在时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>输入文件不可读</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误或磁盘空间不足</li>
	 *                                  </ul>
	 * @apiNote 目前仅支持 {@link SevenZMethod#COPY}、{@link SevenZMethod#LZMA2}、{@link SevenZMethod#BZIP2} 和 {@link SevenZMethod#DEFLATE}。
	 * @since 2.1.0
	 */
	public static void archive(final File inputFile, final File outputFile, final SevenZMethod method) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notNull(method, "method 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile)) {
			SevenZMethodConfiguration configuration = new SevenZMethodConfiguration(method);
			archive(inputFile, sevenZOutputFile,
				archiveEntry -> archiveEntry.setContentMethods(configuration));
		}
	}

	/**
	 * 压缩文件/目录到 7z 文件，自定义压缩条目配置。
	 * <p>将单个文件或目录（递归包含子目录）压缩为 7z 格式文件，通过 Consumer 自定义压缩条目配置。</p>
	 *
	 * @param inputFile            要压缩的文件或目录，必须存在且可读
	 * @param outputFile           输出 7z 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param archiveEntryConsumer 压缩条目配置函数，可为 null
	 * @throws NullPointerException     当 {@code inputFile} 或 {@code outputFile} 为 {@code null} 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFile} 不存在时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>输入文件不可读</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误或磁盘空间不足</li>
	 *                                  </ul>
	 * @since 2.1.0
	 */
	public static void archive(final File inputFile, final File outputFile,
	                            final Consumer<SevenZArchiveEntry> archiveEntryConsumer) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile)) {
			archive(inputFile, sevenZOutputFile, archiveEntryConsumer);
		}
	}

	/**
	 * 压缩文件/目录到加密的 7z 文件。
	 * <p>将单个文件或目录（递归包含子目录）压缩为加密的 7z 格式文件。</p>
	 *
	 * @param inputFile  要压缩的文件或目录，必须存在且可读
	 * @param outputFile 输出 7z 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param password   7z 文件密码，必须非空且非空白字符串
	 * @throws NullPointerException     当 {@code inputFile} 或 {@code outputFile} 为 {@code null} 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFile} 不存在或 {@code password} 为空时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>输入文件不可读</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误或磁盘空间不足</li>
	 *                                  </ul>
	 * @since 2.1.0
	 */
	public static void archive(final File inputFile, final File outputFile, final String password) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notBlank(password, "password 不可为空");

		FileUtils.forceMkdirParent(outputFile);

		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile, password.toCharArray())) {
			archive(inputFile, sevenZOutputFile);
		}
	}

	/**
	 * 压缩文件/目录到加密的 7z 文件，指定压缩方法。
	 * <p>将单个文件或目录（递归包含子目录）压缩为加密的 7z 格式文件，使用指定的压缩方法。</p>
	 *
	 * @param inputFile  要压缩的文件或目录，必须存在且可读
	 * @param outputFile 输出 7z 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param password   7z 文件密码，必须非空且非空白字符串
	 * @param method     压缩方法，必须非 null
	 * @throws NullPointerException     当 {@code inputFile}、{@code outputFile}、{@code password} 或 {@code method} 为 {@code null} 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFile} 不存在或 {@code password} 为空时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>输入文件不可读</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误或磁盘空间不足</li>
	 *                                  </ul>
	 * @apiNote 目前仅支持 {@link SevenZMethod#COPY}、{@link SevenZMethod#LZMA2}、{@link SevenZMethod#BZIP2} 和 {@link SevenZMethod#DEFLATE}。
	 * @since 2.1.0
	 */
	public static void archive(final File inputFile, final File outputFile, final String password, final SevenZMethod method) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notBlank(password, "password 不可为空");
		Validate.notNull(method, "method 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile, password.toCharArray())) {
			SevenZMethodConfiguration configuration = new SevenZMethodConfiguration(method);
			archive(inputFile, sevenZOutputFile,
				archiveEntry -> archiveEntry.setContentMethods(configuration));
		}
	}

	/**
	 * 压缩文件/目录到加密的 7z 文件，自定义压缩条目配置。
	 * <p>将单个文件或目录（递归包含子目录）压缩为加密的 7z 格式文件，通过 Consumer 自定义压缩条目配置。</p>
	 *
	 * @param inputFile            要压缩的文件或目录，必须存在且可读
	 * @param outputFile           输出 7z 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param password             7z 文件密码，必须非空且非空白字符串
	 * @param archiveEntryConsumer 压缩条目配置函数，可为 null
	 * @throws NullPointerException     当 {@code inputFile}、{@code outputFile} 或 {@code password} 为 {@code null} 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFile} 不存在或 {@code password} 为空时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>输入文件不可读</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误或磁盘空间不足</li>
	 *                                  </ul>
	 * @since 2.1.0
	 */
	public static void archive(final File inputFile, final File outputFile, final String password,
	                            final Consumer<SevenZArchiveEntry> archiveEntryConsumer) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notBlank(password, "password 不可为空");

		FileUtils.forceMkdirParent(outputFile);

		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile, password.toCharArray())) {
			archive(inputFile, sevenZOutputFile, archiveEntryConsumer);
		}
	}

	/**
	 * 压缩文件/目录到可定位字节通道。
	 * <p>将单个文件或目录（递归包含子目录）压缩到可定位字节通道中。</p>
	 *
	 * @param inputFile     要压缩的文件或目录，必须存在且可读
	 * @param outputChannel 可定位字节通道，必须非 null
	 * @throws NullPointerException     当 {@code inputFile} 或 {@code outputChannel} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFile} 不存在时抛出
	 * @throws IOException              当输入文件不可读、输出通道不可写、压缩过程中发生 I/O 错误或磁盘空间不足时抛出
	 * @since 2.1.0
	 */
	public static void archive(final File inputFile, final SeekableByteChannel outputChannel) throws IOException {
		Validate.notNull(outputChannel, "outputChannel 不可为 null");

		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputChannel)) {
			archive(inputFile, sevenZOutputFile);
		}
	}

	/**
	 * 压缩文件/目录到可定位字节通道，指定压缩方法。
	 * <p>将单个文件或目录（递归包含子目录）压缩到可定位字节通道中，使用指定的压缩方法。</p>
	 *
	 * @param inputFile     要压缩的文件或目录，必须存在且可读
	 * @param outputChannel 可定位字节通道，必须非 null
	 * @param method        压缩方法，必须非 null
	 * @throws NullPointerException     当 {@code inputFile}、{@code outputChannel} 或 {@code method} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFile} 不存在时抛出
	 * @throws IOException              当输入文件不可读、输出通道不可写、压缩过程中发生 I/O 错误或磁盘空间不足时抛出
	 * @apiNote 目前仅支持 {@link SevenZMethod#COPY}、{@link SevenZMethod#LZMA2}、{@link SevenZMethod#BZIP2} 和 {@link SevenZMethod#DEFLATE}。
	 * @since 2.1.0
	 */
	public static void archive(final File inputFile, final SeekableByteChannel outputChannel, final SevenZMethod method) throws IOException {
		Validate.notNull(outputChannel, "outputChannel 不可为 null");
		Validate.notNull(method, "method 不可为 null");

		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputChannel)) {
			SevenZMethodConfiguration configuration = new SevenZMethodConfiguration(method);
			archive(inputFile, sevenZOutputFile,
				archiveEntry -> archiveEntry.setContentMethods(configuration));
		}
	}

	/**
	 * 压缩文件/目录到可定位字节通道，自定义压缩条目配置。
	 * <p>将单个文件或目录（递归包含子目录）压缩到可定位字节通道中。</p>
	 *
	 * @param inputFile            要压缩的文件或目录，必须存在且可读
	 * @param outputChannel        可定位字节通道，必须非 null
	 * @param archiveEntryConsumer 压缩条目配置函数，可为 null
	 * @throws NullPointerException     当 {@code inputFile} 或 {@code outputChannel} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFile} 不存在时抛出
	 * @throws IOException              当输入文件不可读、输出通道不可写、压缩过程中发生 I/O 错误或磁盘空间不足时抛出
	 * @since 2.1.0
	 */
	public static void archive(final File inputFile, final SeekableByteChannel outputChannel,
	                           final Consumer<SevenZArchiveEntry> archiveEntryConsumer) throws IOException {
		Validate.notNull(outputChannel, "outputChannel 不可为 null");

		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputChannel)) {
			archive(inputFile, sevenZOutputFile, archiveEntryConsumer);
		}
	}

	/**
	 * 压缩文件/目录到加密的可定位字节通道。
	 * <p>将单个文件或目录（递归包含子目录）压缩为加密的 7z 格式并写入可定位字节通道。</p>
	 *
	 * @param inputFile     要压缩的文件或目录，必须存在且可读
	 * @param outputChannel 可定位字节通道，必须非 null
	 * @param password      7z 文件密码，必须非空且非空白字符串
	 * @throws NullPointerException     当 {@code inputFile}、{@code outputChannel} 或 {@code password} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFile} 不存在或 {@code password} 为空时抛出
	 * @throws IOException              当输入文件不可读、输出通道不可写、压缩过程中发生 I/O 错误或磁盘空间不足时抛出
	 * @since 2.1.0
	 */
	public static void archive(final File inputFile, final SeekableByteChannel outputChannel, final String password) throws IOException {
		Validate.notNull(outputChannel, "outputChannel 不可为 null");
		Validate.notBlank(password, "password 不可为空");

		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputChannel, password.toCharArray())) {
			archive(inputFile, sevenZOutputFile);
		}
	}

	/**
	 * 压缩文件/目录到加密的可定位字节通道，指定压缩方法。
	 * <p>将单个文件或目录（递归包含子目录）压缩为加密的 7z 格式并写入可定位字节通道，使用指定的压缩方法。</p>
	 *
	 * @param inputFile     要压缩的文件或目录，必须存在且可读
	 * @param outputChannel 可定位字节通道，必须非 null
	 * @param password      7z 文件密码，必须非空且非空白字符串
	 * @param method        压缩方法，必须非 null
	 * @throws NullPointerException     当 {@code inputFile}、{@code outputChannel}、{@code password} 或 {@code method} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFile} 不存在或 {@code password} 为空时抛出
	 * @throws IOException              当输入文件不可读、输出通道不可写、压缩过程中发生 I/O 错误或磁盘空间不足时抛出
	 * @apiNote 目前仅支持 {@link SevenZMethod#COPY}、{@link SevenZMethod#LZMA2}、{@link SevenZMethod#BZIP2} 和 {@link SevenZMethod#DEFLATE}。
	 * @since 2.1.0
	 */
	public static void archive(final File inputFile, final SeekableByteChannel outputChannel, final String password,
	                            final SevenZMethod method) throws IOException {
		Validate.notNull(outputChannel, "outputChannel 不可为 null");
		Validate.notBlank(password, "password 不可为空");
		Validate.notNull(method, "method 不可为 null");

		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputChannel, password.toCharArray())) {
			SevenZMethodConfiguration configuration = new SevenZMethodConfiguration(method);
			archive(inputFile, sevenZOutputFile,
				archiveEntry -> archiveEntry.setContentMethods(configuration));
		}
	}

	/**
	 * 压缩文件/目录到加密的可定位字节通道，自定义压缩条目配置。
	 * <p>将单个文件或目录（递归包含子目录）压缩为加密的 7z 格式并写入可定位字节通道。</p>
	 *
	 * @param inputFile            要压缩的文件或目录，必须存在且可读
	 * @param outputChannel        可定位字节通道，必须非 null
	 * @param password             7z 文件密码，必须非空且非空白字符串
	 * @param archiveEntryConsumer 压缩条目配置函数，可为 null
	 * @throws NullPointerException     当 {@code inputFile}、{@code outputChannel} 或 {@code password} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFile} 不存在或 {@code password} 为空时抛出
	 * @throws IOException              当输入文件不可读、输出通道不可写、压缩过程中发生 I/O 错误或磁盘空间不足时抛出
	 * @since 2.1.0
	 */
	public static void archive(final File inputFile, final SeekableByteChannel outputChannel, final String password,
	                            final Consumer<SevenZArchiveEntry> archiveEntryConsumer) throws IOException {
		Validate.notNull(outputChannel, "outputChannel 不可为 null");
		Validate.notBlank(password, "password 不可为空");

		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputChannel, password.toCharArray())) {
			archive(inputFile, sevenZOutputFile, archiveEntryConsumer);
		}
	}

	/**
	 * 压缩文件/目录到 SevenZOutputFile 对象。
	 * <p>将单个文件或目录（递归包含子目录）压缩到已初始化的 SevenZOutputFile 对象中。</p>
	 *
	 * @param inputFile        要压缩的文件或目录，必须存在且可读
	 * @param sevenZOutputFile 已初始化的 SevenZOutputFile 对象，必须处于可写入状态且不为 null
	 * @throws NullPointerException     当 {@code inputFile} 或 {@code sevenZOutputFile} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFile} 不存在时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>输入文件不可读</li>
	 *                                  <li>sevenZOutputFile 已关闭或不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 2.1.0
	 */
	public static void archive(final File inputFile, final SevenZOutputFile sevenZOutputFile) throws IOException {
		FileUtils.check(inputFile, "inputFile 不可为 null");
		Validate.notNull(sevenZOutputFile, "sevenZOutputFile 不可为 null");

		archive(inputFile, sevenZOutputFile, (Consumer<SevenZArchiveEntry>) null);
	}

	/**
	 * 压缩文件/目录到 SevenZOutputFile 对象，指定压缩方法。
	 * <p>将单个文件或目录（递归包含子目录）压缩到已初始化的 SevenZOutputFile 对象中，使用指定的压缩方法。</p>
	 *
	 * @param inputFile        要压缩的文件或目录，必须存在且可读
	 * @param sevenZOutputFile 已初始化的 SevenZOutputFile 对象，必须处于可写入状态且不为 null
	 * @param method           压缩方法，必须非 null
	 * @throws NullPointerException     当 {@code inputFile}、{@code sevenZOutputFile} 或 {@code method} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFile} 不存在时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>输入文件不可读</li>
	 *                                  <li>sevenZOutputFile 已关闭或不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @apiNote 目前仅支持 {@link SevenZMethod#COPY}、{@link SevenZMethod#LZMA2}、{@link SevenZMethod#BZIP2} 和 {@link SevenZMethod#DEFLATE}。
	 * @since 2.1.0
	 */
	public static void archive(final File inputFile, final SevenZOutputFile sevenZOutputFile, final SevenZMethod method) throws IOException {
		FileUtils.check(inputFile, "inputFile 不可为 null");
		Validate.notNull(sevenZOutputFile, "sevenZOutputFile 不可为 null");
		Validate.notNull(method, "method 不可为 null");

		SevenZMethodConfiguration configuration = new SevenZMethodConfiguration(method);
		archive(inputFile, sevenZOutputFile,
			archiveEntry -> archiveEntry.setContentMethods(configuration));
	}

	/**
	 * 压缩文件/目录到 SevenZOutputFile 对象，自定义压缩条目配置。
	 * <p>将单个文件或目录（递归包含子目录）压缩到已初始化的 SevenZOutputFile 对象中。</p>
	 *
	 * @param inputFile            要压缩的文件或目录，必须存在且可读
	 * @param sevenZOutputFile     已初始化的 SevenZOutputFile 对象，必须处于可写入状态且不为 null
	 * @param archiveEntryConsumer 压缩条目配置函数，可为 null
	 * @throws NullPointerException     当 {@code inputFile} 或 {@code sevenZOutputFile} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFile} 不存在时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>输入文件不可读</li>
	 *                                  <li>sevenZOutputFile 已关闭或不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  <li>磁盘空间不足</li>
	 *                                  </ul>
	 * @since 2.1.0
	 */
	public static void archive(final File inputFile, final SevenZOutputFile sevenZOutputFile,
	                            final Consumer<SevenZArchiveEntry> archiveEntryConsumer) throws IOException {
		FileUtils.check(inputFile, "inputFile 不可为 null");
		Validate.notNull(sevenZOutputFile, "sevenZOutputFile 不可为 null");

		if (inputFile.isDirectory()) {
			addDir(inputFile, sevenZOutputFile, null, archiveEntryConsumer);
		} else {
			addFile(inputFile, sevenZOutputFile, null, archiveEntryConsumer);
		}
	}

	/**
	 * 压缩多个文件/目录到 7z 文件。
	 * <p>将多个文件或目录（递归包含子目录）压缩为单个 7z 格式文件。</p>
	 *
	 * @param inputFiles 要压缩的文件/目录集合，必须非空且所有元素必须存在
	 * @param outputFile 输出 7z 文件路径，会自动创建父目录并覆盖已存在文件
	 * @throws NullPointerException     当 {@code inputFiles} 或 {@code outputFile} 为 {@code null} 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空集合或集合中存在不存在的文件时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>集合中存在不可读的文件</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误或磁盘空间不足</li>
	 *                                  </ul>
	 * @since 2.1.0
	 */
	public static void archive(final Collection<File> inputFiles, final File outputFile) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile)) {
			archive(inputFiles, sevenZOutputFile);
		}
	}

	/**
	 * 压缩多个文件/目录到 7z 文件，指定压缩方法。
	 * <p>将多个文件或目录（递归包含子目录）压缩为单个 7z 格式文件，使用指定的压缩方法。</p>
	 *
	 * @param inputFiles 要压缩的文件/目录集合，必须非空且所有元素必须存在
	 * @param outputFile 输出 7z 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param method     压缩方法，必须非 null
	 * @throws NullPointerException     当 {@code inputFiles}、{@code outputFile} 或 {@code method} 为 {@code null} 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空集合或集合中存在不存在的文件时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>集合中存在不可读的文件</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误或磁盘空间不足</li>
	 *                                  </ul>
	 * @apiNote 目前仅支持 {@link SevenZMethod#COPY}、{@link SevenZMethod#LZMA2}、{@link SevenZMethod#BZIP2} 和 {@link SevenZMethod#DEFLATE}。
	 * @since 2.1.0
	 */
	public static void archive(final Collection<File> inputFiles, final File outputFile, final SevenZMethod method) throws IOException {
		Validate.notNull(method, "method 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		SevenZMethodConfiguration configuration = new SevenZMethodConfiguration(method);
		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile)) {
			archive(inputFiles, sevenZOutputFile,
				archiveEntry -> archiveEntry.setContentMethods(configuration));
		}
	}

	/**
	 * 压缩多个文件/目录到 7z 文件，自定义压缩条目配置。
	 * <p>将多个文件或目录（递归包含子目录）压缩为单个 7z 格式文件，通过 Consumer 自定义压缩条目配置。</p>
	 *
	 * @param inputFiles           要压缩的文件/目录集合，必须非空且所有元素必须存在
	 * @param outputFile           输出 7z 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param archiveEntryConsumer 压缩条目配置函数，可为 null
	 * @throws NullPointerException     当 {@code inputFiles} 或 {@code outputFile} 为 {@code null} 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空集合或集合中存在不存在的文件时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>集合中存在不可读的文件</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误或磁盘空间不足</li>
	 *                                  </ul>
	 * @since 2.1.0
	 */
	public static void archive(final Collection<File> inputFiles, final File outputFile,
	                            final Consumer<SevenZArchiveEntry> archiveEntryConsumer) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile)) {
			archive(inputFiles, sevenZOutputFile, archiveEntryConsumer);
		}
	}

	/**
	 * 压缩多个文件/目录到加密的 7z 文件。
	 * <p>将多个文件或目录（递归包含子目录）压缩为单个加密的 7z 格式文件。</p>
	 *
	 * @param inputFiles 要压缩的文件/目录集合，必须非空且所有元素必须存在
	 * @param outputFile 输出 7z 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param password   7z 文件密码，必须非空且非空白字符串
	 * @throws NullPointerException     当 {@code inputFiles} 或 {@code outputFile} 为 {@code null} 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空集合、集合中存在不存在的文件或 {@code password} 为空时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>集合中存在不可读的文件</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误或磁盘空间不足</li>
	 *                                  </ul>
	 * @since 2.1.0
	 */
	public static void archive(final Collection<File> inputFiles, final File outputFile, final String password) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notBlank(password, "password 不可为空");

		FileUtils.forceMkdirParent(outputFile);

		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile, password.toCharArray())) {
			archive(inputFiles, sevenZOutputFile);
		}
	}

	/**
	 * 压缩多个文件/目录到加密的 7z 文件，指定压缩方法。
	 * <p>将多个文件或目录（递归包含子目录）压缩为单个加密的 7z 格式文件，使用指定的压缩方法。</p>
	 *
	 * @param inputFiles 要压缩的文件/目录集合，必须非空且所有元素必须存在
	 * @param outputFile 输出 7z 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param password   7z 文件密码，必须非空且非空白字符串
	 * @param method     压缩方法，必须非 null
	 * @throws NullPointerException     当 {@code inputFiles}、{@code outputFile}、{@code password} 或 {@code method} 为 {@code null} 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空集合、集合中存在不存在的文件或 {@code password} 为空时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>集合中存在不可读的文件</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误或磁盘空间不足</li>
	 *                                  </ul>
	 * @apiNote 目前仅支持 {@link SevenZMethod#COPY}、{@link SevenZMethod#LZMA2}、{@link SevenZMethod#BZIP2} 和 {@link SevenZMethod#DEFLATE}。
	 * @since 2.1.0
	 */
	public static void archive(final Collection<File> inputFiles, final File outputFile, final String password,
	                            final SevenZMethod method) throws IOException {
		Validate.notNull(method, "method 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notBlank(password, "password 不可为空");

		FileUtils.forceMkdirParent(outputFile);

		SevenZMethodConfiguration configuration = new SevenZMethodConfiguration(method);
		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile, password.toCharArray())) {
			archive(inputFiles, sevenZOutputFile,
				archiveEntry -> archiveEntry.setContentMethods(configuration));
		}
	}

	/**
	 * 压缩多个文件/目录到加密的 7z 文件，自定义压缩条目配置。
	 * <p>将多个文件或目录（递归包含子目录）压缩为单个加密的 7z 格式文件，通过 Consumer 自定义压缩条目配置。</p>
	 *
	 * @param inputFiles           要压缩的文件/目录集合，必须非空且所有元素必须存在
	 * @param outputFile           输出 7z 文件路径，会自动创建父目录并覆盖已存在文件
	 * @param password             7z 文件密码，必须非空且非空白字符串
	 * @param archiveEntryConsumer 压缩条目配置函数，可为 null
	 * @throws NullPointerException     当 {@code inputFiles} 或 {@code outputFile} 为 {@code null} 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空集合、集合中存在不存在的文件或 {@code password} 为空时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>集合中存在不可读的文件</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误或磁盘空间不足</li>
	 *                                  </ul>
	 * @since 2.1.0
	 */
	public static void archive(final Collection<File> inputFiles, final File outputFile, final String password,
	                            final Consumer<SevenZArchiveEntry> archiveEntryConsumer) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notBlank(password, "password 不可为空");

		FileUtils.forceMkdirParent(outputFile);

		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile, password.toCharArray())) {
			archive(inputFiles, sevenZOutputFile, archiveEntryConsumer);
		}
	}

	/**
	 * 压缩多个文件/目录到可定位字节通道。
	 * <p>将多个文件或目录（递归包含子目录）压缩到可定位字节通道中。</p>
	 *
	 * @param inputFiles    要压缩的文件/目录集合，必须非空且所有元素必须存在
	 * @param outputChannel 可定位字节通道，必须非 null
	 * @throws NullPointerException     当 {@code inputFiles} 或 {@code outputChannel} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空集合或集合中存在不存在的文件时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>集合中存在不可读的文件</li>
	 *                                  <li>输出通道不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  </ul>
	 * @since 2.1.0
	 */
	public static void archive(final Collection<File> inputFiles, final SeekableByteChannel outputChannel) throws IOException {
		Validate.notNull(outputChannel, "outputChannel 不可为 null");

		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputChannel)) {
			archive(inputFiles, sevenZOutputFile);
		}
	}

	/**
	 * 压缩多个文件/目录到可定位字节通道，指定压缩方法。
	 * <p>将多个文件或目录（递归包含子目录）压缩到可定位字节通道中，使用指定的压缩方法。</p>
	 *
	 * @param inputFiles    要压缩的文件/目录集合，必须非空且所有元素必须存在
	 * @param outputChannel 可定位字节通道，必须非 null
	 * @param method        压缩方法，必须非 null
	 * @throws NullPointerException     当 {@code inputFiles}、{@code outputChannel} 或 {@code method} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空集合或集合中存在不存在的文件时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>集合中存在不可读的文件</li>
	 *                                  <li>输出通道不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  </ul>
	 * @apiNote 目前仅支持 {@link SevenZMethod#COPY}、{@link SevenZMethod#LZMA2}、{@link SevenZMethod#BZIP2} 和 {@link SevenZMethod#DEFLATE}。
	 * @since 2.1.0
	 */
	public static void archive(final Collection<File> inputFiles, final SeekableByteChannel outputChannel,
	                            final SevenZMethod method) throws IOException {
		Validate.notNull(method, "method 不可为 null");
		Validate.notNull(outputChannel, "outputChannel 不可为 null");

		SevenZMethodConfiguration configuration = new SevenZMethodConfiguration(method);
		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputChannel)) {
			archive(inputFiles, sevenZOutputFile,
				archiveEntry -> archiveEntry.setContentMethods(configuration));
		}
	}

	/**
	 * 压缩多个文件/目录到可定位字节通道，自定义压缩条目配置。
	 * <p>将多个文件或目录（递归包含子目录）压缩到可定位字节通道中。</p>
	 *
	 * @param inputFiles           要压缩的文件/目录集合，必须非空且所有元素必须存在
	 * @param outputChannel        可定位字节通道，必须非 null
	 * @param archiveEntryConsumer 压缩条目配置函数，可为 null
	 * @throws NullPointerException     当 {@code inputFiles} 或 {@code outputChannel} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空集合或集合中存在不存在的文件时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>集合中存在不可读的文件</li>
	 *                                  <li>输出通道不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  </ul>
	 * @since 2.1.0
	 */
	public static void archive(final Collection<File> inputFiles, final SeekableByteChannel outputChannel,
	                           final Consumer<SevenZArchiveEntry> archiveEntryConsumer) throws IOException {
		Validate.notNull(outputChannel, "outputChannel 不可为 null");

		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputChannel)) {
			archive(inputFiles, sevenZOutputFile, archiveEntryConsumer);
		}
	}

	/**
	 * 压缩多个文件/目录到加密的可定位字节通道。
	 * <p>将多个文件或目录（递归包含子目录）压缩为加密的 7z 格式并写入可定位字节通道。</p>
	 *
	 * @param inputFiles    要压缩的文件/目录集合，必须非空且所有元素必须存在
	 * @param outputChannel 可定位字节通道，必须非 null
	 * @param password      7z 文件密码，必须非空且非空白字符串
	 * @throws NullPointerException     当 {@code inputFiles} 或 {@code outputChannel} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空集合、集合中存在不存在的文件或 {@code password} 为空时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>集合中存在不可读的文件</li>
	 *                                  <li>输出通道不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  </ul>
	 * @since 2.1.0
	 */
	public static void archive(final Collection<File> inputFiles, final SeekableByteChannel outputChannel,
	                            final String password) throws IOException {
		Validate.notNull(outputChannel, "outputChannel 不可为 null");
		Validate.notBlank(password, "password 不可为空");

		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputChannel, password.toCharArray())) {
			archive(inputFiles, sevenZOutputFile);
		}
	}

	/**
	 * 压缩多个文件/目录到加密的可定位字节通道，指定压缩方法。
	 * <p>将多个文件或目录（递归包含子目录）压缩为加密的 7z 格式并写入可定位字节通道，使用指定的压缩方法。</p>
	 *
	 * @param inputFiles    要压缩的文件/目录集合，必须非空且所有元素必须存在
	 * @param outputChannel 可定位字节通道，必须非 null
	 * @param password      7z 文件密码，必须非空且非空白字符串
	 * @param method        压缩方法，必须非 null
	 * @throws NullPointerException     当 {@code inputFiles}、{@code outputChannel}、{@code password} 或 {@code method} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空集合、集合中存在不存在的文件或 {@code password} 为空时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>集合中存在不可读的文件</li>
	 *                                  <li>输出通道不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  </ul>
	 * @apiNote 目前仅支持 {@link SevenZMethod#COPY}、{@link SevenZMethod#LZMA2}、{@link SevenZMethod#BZIP2} 和 {@link SevenZMethod#DEFLATE}。
	 * @since 2.1.0
	 */
	public static void archive(final Collection<File> inputFiles, final SeekableByteChannel outputChannel,
	                            final String password, final SevenZMethod method) throws IOException {
		Validate.notNull(method, "method 不可为 null");
		Validate.notBlank(password, "password 不可为空");

		SevenZMethodConfiguration configuration = new SevenZMethodConfiguration(method);
		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputChannel, password.toCharArray())) {
			archive(inputFiles, sevenZOutputFile,
				archiveEntry -> archiveEntry.setContentMethods(configuration));
		}
	}

	/**
	 * 压缩多个文件/目录到加密的可定位字节通道，自定义压缩条目配置。
	 * <p>将多个文件或目录（递归包含子目录）压缩为加密的 7z 格式并写入可定位字节通道。</p>
	 *
	 * @param inputFiles           要压缩的文件/目录集合，必须非空且所有元素必须存在
	 * @param outputChannel        可定位字节通道，必须非 null
	 * @param password             7z 文件密码，必须非空且非空白字符串
	 * @param archiveEntryConsumer 压缩条目配置函数，可为 null
	 * @throws NullPointerException     当 {@code inputFiles} 或 {@code outputChannel} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空集合、集合中存在不存在的文件或 {@code password} 为空时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>集合中存在不可读的文件</li>
	 *                                  <li>输出通道不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  </ul>
	 * @since 2.1.0
	 */
	public static void archive(final Collection<File> inputFiles, final SeekableByteChannel outputChannel,
	                            final String password, final Consumer<SevenZArchiveEntry> archiveEntryConsumer) throws IOException {
		Validate.notNull(outputChannel, "outputChannel 不可为 null");
		Validate.notBlank(password, "password 不可为空");

		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputChannel, password.toCharArray())) {
			archive(inputFiles, sevenZOutputFile, archiveEntryConsumer);
		}
	}

	/**
	 * 压缩多个文件/目录到 SevenZOutputFile 对象。
	 * <p>将多个文件或目录（递归包含子目录）压缩到已初始化的 SevenZOutputFile 对象中。</p>
	 *
	 * @param inputFiles       要压缩的文件/目录集合，必须非空且所有元素必须存在
	 * @param sevenZOutputFile 已初始化的 SevenZOutputFile 对象，必须处于可写入状态且不为 null
	 * @throws NullPointerException     当 {@code inputFiles} 或 {@code sevenZOutputFile} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空集合或集合中存在不存在的文件时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>集合中存在不可读的文件</li>
	 *                                  <li>sevenZOutputFile 已关闭或不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  </ul>
	 * @since 2.1.0
	 */
	public static void archive(final Collection<File> inputFiles, final SevenZOutputFile sevenZOutputFile) throws IOException {
		archive(inputFiles, sevenZOutputFile, (Consumer<SevenZArchiveEntry>) null);
	}

	/**
	 * 压缩多个文件/目录到 SevenZOutputFile 对象，指定压缩方法。
	 * <p>将多个文件或目录（递归包含子目录）压缩到已初始化的 SevenZOutputFile 对象中，使用指定的压缩方法。</p>
	 *
	 * @param inputFiles       要压缩的文件/目录集合，必须非空且所有元素必须存在
	 * @param sevenZOutputFile 已初始化的 SevenZOutputFile 对象，必须处于可写入状态且不为 null
	 * @param method           压缩方法，必须非 null
	 * @throws NullPointerException     当 {@code inputFiles}、{@code sevenZOutputFile} 或 {@code method} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空集合或集合中存在不存在的文件时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>集合中存在不可读的文件</li>
	 *                                  <li>sevenZOutputFile 已关闭或不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  </ul>
	 * @apiNote 目前仅支持 {@link SevenZMethod#COPY}、{@link SevenZMethod#LZMA2}、{@link SevenZMethod#BZIP2} 和 {@link SevenZMethod#DEFLATE}。
	 * @since 2.1.0
	 */
	public static void archive(final Collection<File> inputFiles, final SevenZOutputFile sevenZOutputFile,
	                           final SevenZMethod method) throws IOException {
		Validate.notNull(method, "method 不可为 null");

		SevenZMethodConfiguration configuration = new SevenZMethodConfiguration(method);
		archive(inputFiles, sevenZOutputFile,
			archiveEntry -> archiveEntry.setContentMethods(configuration));
	}

	/**
	 * 压缩多个文件/目录到 SevenZOutputFile 对象，自定义压缩条目配置。
	 * <p>将多个文件或目录（递归包含子目录）压缩到已初始化的 SevenZOutputFile 对象中。</p>
	 *
	 * @param inputFiles           要压缩的文件/目录集合，必须非空且所有元素必须存在
	 * @param sevenZOutputFile     已初始化的 SevenZOutputFile 对象，必须处于可写入状态且不为 null
	 * @param archiveEntryConsumer 压缩条目配置函数，可为 null
	 * @throws NullPointerException     当 {@code inputFiles} 或 {@code sevenZOutputFile} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空集合或集合中存在不存在的文件时抛出
	 * @throws IOException              当发生以下情况时抛出：
	 *                                  <ul>
	 *                                  <li>集合中存在不可读的文件</li>
	 *                                  <li>sevenZOutputFile 已关闭或不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  </ul>
	 * @since 2.1.0
	 */
	public static void archive(final Collection<File> inputFiles, final SevenZOutputFile sevenZOutputFile,
	                            final Consumer<SevenZArchiveEntry> archiveEntryConsumer) throws IOException {
		Validate.notNull(sevenZOutputFile, "sevenZOutputFile 不可为 null");
		Validate.notEmpty(inputFiles, "inputFiles 不可为空");
		Validate.isTrue(inputFiles.stream().allMatch(FileUtils::exist),
			"inputFiles 中存在为 null 或 不存在的文件");

		for (File file : inputFiles) {
			if (file.isDirectory()) {
				addDir(file, sevenZOutputFile, null, archiveEntryConsumer);
			} else {
				addFile(file, sevenZOutputFile, null, archiveEntryConsumer);
			}
		}
	}

	/**
	 * 递归添加目录及其子内容到 7z 输出流。
	 * <p>
	 * 该方法会先创建当前目录的归档条目（包含尾部路径分隔符），
	 * 然后递归遍历目录下的所有子文件和子目录。
	 * 支持通过 {@code archiveEntryConsumer} 在条目写入前自定义属性（如压缩方法）。
	 * </p>
	 *
	 * @param inputDir             要添加的源目录，必须存在且可读
	 * @param outputFile           7z 输出文件对象，必须处于可写入状态
	 * @param parent               父级归档路径前缀；若为 {@code null} 或空白，表示当前目录为根目录
	 * @param archiveEntryConsumer 条目配置回调函数，在 {@code putArchiveEntry} 之前调用；可为 {@code null}
	 * @throws IOException 当目录不可读、创建条目失败或写入过程中发生 I/O 错误时抛出
	 * @since 2.1.0
	 */
	private static void addDir(final File inputDir, final SevenZOutputFile outputFile, final String parent,
	                           final Consumer<SevenZArchiveEntry> archiveEntryConsumer) throws IOException {
		String archiveEntryName = StringUtils.isNotBlank(parent) ?
			parent + CompressConstants.PATH_SEPARATOR + inputDir.getName() : inputDir.getName();
		SevenZArchiveEntry archiveEntry = outputFile.createArchiveEntry(inputDir, archiveEntryName +
			CompressConstants.PATH_SEPARATOR);
		if (Objects.nonNull(archiveEntryConsumer)) {
			archiveEntryConsumer.accept(archiveEntry);
		}
		archiveEntry.setDirectory(true);
		outputFile.putArchiveEntry(archiveEntry);
		outputFile.closeArchiveEntry();

		File[] childFiles = ArrayUtils.nullToEmpty(inputDir.listFiles(), File[].class);
		if (ArrayUtils.isNotEmpty(childFiles)) {
			String childParent = inputDir.getName();
			if (Objects.nonNull(parent)) {
				childParent = parent + CompressConstants.PATH_SEPARATOR + inputDir.getName();
			}
			for (File childFile : childFiles) {
				if (childFile.isDirectory()) {
					addDir(childFile, outputFile, childParent, archiveEntryConsumer);
				} else {
					addFile(childFile, outputFile, childParent, archiveEntryConsumer);
				}
			}
		}
	}

	/**
	 * 添加单个文件到 7z 输出流。
	 * <p>
	 * 使用缓冲输入流读取文件内容并写入归档。
	 * 支持通过 {@code archiveEntryConsumer} 在条目写入前自定义属性。
	 * </p>
	 *
	 * @param inputFile            要添加的源文件，必须存在且可读
	 * @param outputFile           7z 输出文件对象，必须处于可写入状态
	 * @param parent               父级归档路径前缀；若为 {@code null} 或空白，表示当前文件为根文件
	 * @param archiveEntryConsumer 条目配置回调函数，在 {@code putArchiveEntry} 之前调用；可为 {@code null}
	 * @throws IOException 当文件不可读、创建条目失败或写入过程中发生 I/O 错误时抛出
	 * @since 2.1.0
	 */
	private static void addFile(final File inputFile, final SevenZOutputFile outputFile, final String parent,
	                            final Consumer<SevenZArchiveEntry> archiveEntryConsumer) throws IOException {
		try (InputStream inputStream = FileUtils.openBufferedFileChannelInputStream(inputFile)) {
			String archiveEntryName = StringUtils.isNotBlank(parent) ? parent + CompressConstants.PATH_SEPARATOR +
				inputFile.getName() : inputFile.getName();
			SevenZArchiveEntry archiveEntry = outputFile.createArchiveEntry(inputFile, archiveEntryName);
			if (Objects.nonNull(archiveEntryConsumer)) {
				archiveEntryConsumer.accept(archiveEntry);
			}
			outputFile.putArchiveEntry(archiveEntry);
			outputFile.write(inputStream);
			outputFile.closeArchiveEntry();
		}
	}

	/**
	 * 递归添加目录到 7z 压缩流。
	 * <p>将目录及其所有子目录和文件递归添加到压缩流中，保持原始目录层级结构。</p>
	 *
	 * @param inputFile  要添加的目录，必须存在且可读
	 * @param outputFile 已初始化的 SevenZOutputFile 对象，必须处于可写入状态
	 * @param parent     父目录相对路径（用于构建压缩包内路径），可为 null
	 * @throws NullPointerException 当 {@code inputFile} 或 {@code outputFile} 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>目录不可读</li>
	 *                                  <li>outputFile 已关闭或不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  <li>磁盘空间不足</li>
	 *                              </ul>
	 * @since 1.0.0
	 * @deprecated
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	protected static void addDir(final File inputFile, final SevenZOutputFile outputFile, final String parent) throws IOException {
		String archiveEntryName = StringUtils.isNotBlank(parent) ?
			parent + CompressConstants.PATH_SEPARATOR + inputFile.getName() : inputFile.getName();
		SevenZArchiveEntry archiveEntry = outputFile.createArchiveEntry(inputFile, archiveEntryName +
			CompressConstants.PATH_SEPARATOR);
		archiveEntry.setDirectory(true);
		outputFile.putArchiveEntry(archiveEntry);
		outputFile.closeArchiveEntry();

		File[] childFiles = ArrayUtils.nullToEmpty(inputFile.listFiles(), File[].class);
		if (ArrayUtils.isNotEmpty(childFiles)) {
			String childParent = inputFile.getName();
			if (Objects.nonNull(parent)) {
				childParent = parent + CompressConstants.PATH_SEPARATOR + inputFile.getName();
			}
			for (File childFile : childFiles) {
				if (childFile.isDirectory()) {
					addDir(childFile, outputFile, childParent);
				} else {
					addFile(childFile, outputFile, childParent);
				}
			}
		}
	}

	/**
	 * 添加单个文件到 7z 压缩流。
	 * <p>将单个文件添加到压缩流中，可指定父目录路径以构建压缩包内的层级结构。</p>
	 *
	 * @param inputFile  要添加的文件，必须存在且可读
	 * @param outputFile 已初始化的 SevenZOutputFile 对象，必须处于可写入状态
	 * @param parent     父目录相对路径（用于构建压缩包内路径），可为 null
	 * @throws NullPointerException 当 {@code inputFile} 或 {@code outputFile} 为 null 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>文件不可读</li>
	 *                                  <li>outputFile 已关闭或不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                                  <li>磁盘空间不足</li>
	 *                              </ul>
	 * @since 1.0.0
	 * @deprecated
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	protected static void addFile(final File inputFile, final SevenZOutputFile outputFile, final String parent) throws IOException {
		try (InputStream inputStream = FileUtils.openBufferedFileChannelInputStream(inputFile)) {
			String archiveEntryName = StringUtils.isNotBlank(parent) ? parent + CompressConstants.PATH_SEPARATOR +
				inputFile.getName() : inputFile.getName();
			SevenZArchiveEntry archiveEntry = outputFile.createArchiveEntry(inputFile, archiveEntryName);
			outputFile.putArchiveEntry(archiveEntry);
			outputFile.write(inputStream);
			outputFile.closeArchiveEntry();
		}
	}
}