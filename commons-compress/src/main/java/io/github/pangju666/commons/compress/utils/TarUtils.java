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
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.apache.commons.io.input.UnsynchronizedBufferedInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * TAR 压缩/解压工具类。
 * <p>基于 Apache Commons Compress 提供 TAR 的压缩与解压能力。</p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li>多输入源：支持文件、字节数组、输入流、{@code TarFile}。</li>
 *   <li>目录递归：保持原始目录层级进行压缩与解压。</li>
 *   <li>格式校验：通过 Tika 的 MIME 类型检测判断 TAR 格式（文件/字节数组在调用前校验；流式读取不预校验，若非 TAR 会在读取过程中失败）。</li>
 *   <li>性能优化：广泛使用缓冲流与流式传输，适合大文件。</li>
 *   <li>资源管理：使用 try-with-resources 自动释放资源。</li>
 * </ul>
 *
 * <h3>线程安全</h3>
 * <p>类本身无共享状态，方法均为静态；并发处理不同文件/目录是安全的。若对同一路径/同一输出文件并发写入，可能发生冲突或覆盖。</p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 1) 压缩单个文件到 .tar
 * TarUtils.compress(new File("input.txt"), new File("archive.tar"));
 *
 * // 2) 压缩目录到输出流（不会自动关闭传入流）
 * try (FileOutputStream fos = new FileOutputStream("archive.tar");
 *      TarArchiveOutputStream taos = new TarArchiveOutputStream(fos)) {
 *     TarUtils.compress(new File("inputDir"), taos);
 * }
 *
 * // 3) 解压 TAR 文件到目录
 * TarUtils.uncompress(new File("archive.tar"), new File("outputDir"));
 *
 * // 4) 解压字节数组（字节数组会先进行 MIME 类型校验）
 * byte[] bytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("archive.tar"));
 * TarUtils.uncompress(bytes, new File("outputDir"));
 *
 * // 5) 解压输入流（不预校验格式，读取失败以 I/O 异常体现）
 * try (InputStream in = new FileInputStream("archive.tar")) {
 *     TarUtils.uncompress(in, new File("outputDir"));
 * }
 *
 * // 6) 使用 TarFile 解压
 * try (TarFile tarFile = new TarFile(new File("archive.tar"))) {
 *     TarUtils.uncompress(tarFile, new File("outputDir"));
 * }
 * }</pre>
 *
 * @author pangju666
 * @see org.apache.commons.compress.archivers.tar.TarFile
 * @see org.apache.commons.compress.archivers.tar.TarArchiveInputStream
 * @see org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
 * @since 1.0.0
 */
public class TarUtils {
	protected TarUtils() {
	}

	/**
	 * 检查指定文件是否为 TAR 格式。
	 * <p>基于 Tika 的 MIME 类型检测。</p>
	 *
	 * @param file 待检测文件，必须存在且可读
	 * @return 当且仅当文件非空且检测为 {@code application/x-tar} 时返回 {@code true}
	 * @throws NullPointerException 当 {@code file} 为 {@code null} 时抛出
	 * @throws IOException          当文件访问发生 I/O 异常时抛出
	 * @throws SecurityException    当没有文件读取权限时抛出
	 * @since 1.0.0
	 */
	public static boolean isTar(final File file) throws IOException {
		return FileUtils.isMimeType(file, CompressConstants.TAR_MIME_TYPE);
	}

	/**
	 * 检查字节数组内容是否为 TAR 格式。
	 * <p>基于 Tika 的 MIME 类型检测。</p>
	 *
	 * @param bytes 待检测的字节数组；为 {@code null} 或空数组将返回 {@code false}
	 * @return 当且仅当字节数组非空且检测为 {@code application/x-tar} 时返回 {@code true}
	 * @since 1.0.0
	 */
	public static boolean isTar(final byte[] bytes) {
		return ArrayUtils.isNotEmpty(bytes) &&
			IOConstants.getDefaultTika().detect(bytes).equals(CompressConstants.TAR_MIME_TYPE);
	}

	/**
	 * 检查输入流内容是否为 TAR 格式。
	 * <p>基于 Tika 的 MIME 类型检测。</p>
	 *
	 * @param inputStream 待检测的输入流；为 {@code null} 时返回 {@code false}
	 * @return 当且仅当输入流非空且检测为 {@code application/x-tar} 时返回 {@code true}
	 * @throws IOException 当流读取发生 I/O 错误时抛出
	 * @since 1.0.0
	 */
	public static boolean isTar(final InputStream inputStream) throws IOException {
		return Objects.nonNull(inputStream) &&
			IOConstants.getDefaultTika().detect(inputStream).equals(CompressConstants.TAR_MIME_TYPE);
	}

	/**
	 * 解压 TAR 文件到指定目录。
	 * <p>将 TAR 格式文件解压到目标目录，自动创建不存在的目录结构并保持原始层级。</p>
	 *
	 * @param inputFile 要解压的 TAR 文件，必须存在且可读且为有效 TAR 格式（通过 Tika 检测）
	 * @param outputDir 解压目标目录；若不存在则自动创建父目录
	 * @throws NullPointerException     当 {@code inputFile} 或 {@code outputDir} 为 {@code null} 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFile} 不是 TAR 格式时抛出
	 * @throws IOException              当输入文件不可读、输出目录不可写、解压过程中发生 I/O 错误或磁盘空间不足时抛出
	 * @since 1.0.0
	 */
	public static void uncompress(final File inputFile, final File outputDir) throws IOException {
		Validate.notNull(inputFile, "inputFile 不可为 null");

		String mimeType = FileUtils.getMimeType(inputFile);
		if (!CompressConstants.TAR_MIME_TYPE.equals(mimeType)) {
			throw new IllegalArgumentException(inputFile.getAbsolutePath() + "不是tar类型文件");
		}
		try (InputStream inputStream = FileUtils.openUnsynchronizedBufferedInputStream(inputFile);
			 TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(inputStream)) {
			uncompress(tarArchiveInputStream, outputDir);
		}
	}

	/**
	 * 从字节数组解压 TAR 内容到指定目录。
	 *
	 * @param bytes     要解压的 TAR 字节数组，必须非空且为有效 TAR 格式（通过 Tika 检测）
	 * @param outputDir 解压目标目录；若不存在则自动创建父目录
	 * @throws IllegalArgumentException 当 {@code bytes} 为空数组或内容不是 TAR 格式时抛出
	 * @throws NullPointerException     当 {@code outputDir} 为 {@code null} 时抛出
	 * @throws IOException              当输出目录不可写、解压过程中发生 I/O 错误或磁盘空间不足时抛出
	 * @since 1.0.0
	 */
	public static void uncompress(final byte[] bytes, final File outputDir) throws IOException {
		Validate.isTrue(ArrayUtils.isNotEmpty(bytes), "bytes 不可为空");

		String mimeType = IOConstants.getDefaultTika().detect(bytes);
		if (!CompressConstants.TAR_MIME_TYPE.equals(mimeType)) {
			throw new IllegalArgumentException("不是tar类型文件");
		}
		try (InputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
			 TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(inputStream)) {
			uncompress(tarArchiveInputStream, outputDir);
		}
	}

	/**
	 * 从输入流解压 TAR 内容到指定目录。
	 * <p>若输入流不是 {@code TarArchiveInputStream}，则自动包装为 TAR 输入流；不预校验格式，非 TAR 内容会在读取过程中以 I/O 错误体现。</p>
	 *
	 * @param inputStream TAR 输入源，非空
	 * @param outputDir   解压目标目录；若不存在则自动创建父目录
	 * @throws NullPointerException 当 {@code inputStream} 或 {@code outputDir} 为 {@code null} 时抛出
	 * @throws IOException          当输入流不可读或已关闭、输出目录不可写、解压过程中发生 I/O 错误或磁盘空间不足时抛出
	 * @since 1.0.0
	 */
	public static void uncompress(final InputStream inputStream, final File outputDir) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		if (inputStream instanceof TarArchiveInputStream) {
			uncompress((TarArchiveInputStream) inputStream, outputDir);
		} else {
			if (inputStream instanceof BufferedInputStream || inputStream instanceof UnsynchronizedBufferedInputStream) {
				try (TarArchiveInputStream archiveInputStream = new TarArchiveInputStream(inputStream)) {
					uncompress(archiveInputStream, outputDir);
				}
			} else {
				try (InputStream bufferedInputStream = IOUtils.unsynchronizedBuffer(inputStream);
					 TarArchiveInputStream archiveInputStream = new TarArchiveInputStream(bufferedInputStream)) {
					uncompress(archiveInputStream, outputDir);
				}
			}
		}
	}

	/**
	 * 从 {@code TarFile} 解压到指定目录。
	 *
	 * @param tarFile   已初始化的 {@code TarFile}，必须可读且非空
	 * @param outputDir 解压目标目录；若不存在则自动创建父目录
	 * @throws NullPointerException 当 {@code tarFile} 或 {@code outputDir} 为 {@code null} 时抛出
	 * @throws IOException          当 {@code tarFile} 已关闭或不可读、输出目录不可写、解压过程中发生 I/O 错误或磁盘空间不足时抛出
	 * @since 1.0.0
	 */
	public static void uncompress(final TarFile tarFile, final File outputDir) throws IOException {
		Validate.notNull(tarFile, "tarFile 不可为 null");
		FileUtils.forceMkdir(outputDir);

		for (TarArchiveEntry tarEntry : tarFile.getEntries()) {
			File file = new File(outputDir, tarEntry.getName());
			if (tarEntry.isDirectory()) {
				if (!file.exists()) {
					FileUtils.forceMkdir(file);
				}
			} else {
				FileUtils.forceMkdir(file.getParentFile());
				try (FileOutputStream fileOutputStream = FileUtils.openOutputStream(file);
					 BufferedOutputStream bufferedOutputStream = IOUtils.buffer(fileOutputStream);
					 InputStream inputStream = tarFile.getInputStream(tarEntry)) {
					inputStream.transferTo(bufferedOutputStream);
				}
			}
		}
	}

	/**
	 * 从 {@code TarArchiveInputStream} 解压到指定目录。
	 *
	 * @param tarArchiveInputStream TAR 输入流，必须可读且非空
	 * @param outputDir             解压目标目录；若不存在则自动创建父目录
	 * @throws NullPointerException 当 {@code tarArchiveInputStream} 或 {@code outputDir} 为 {@code null} 时抛出
	 * @throws IOException          当输入流已关闭或不可读、输出目录不可写、解压过程中发生 I/O 错误或磁盘空间不足时抛出
	 * @since 1.0.0
	 */
	public static void uncompress(final TarArchiveInputStream tarArchiveInputStream, final File outputDir) throws IOException {
		Validate.notNull(tarArchiveInputStream, "tarArchiveInputStream 不可为 null");
		FileUtils.forceMkdir(outputDir);

		TarArchiveEntry tarEntry = tarArchiveInputStream.getNextEntry();
		while (Objects.nonNull(tarEntry)) {
			File file = new File(outputDir, tarEntry.getName());
			if (tarEntry.isDirectory()) {
				if (!file.exists()) {
					FileUtils.forceMkdir(file);
				}
			} else {
				FileUtils.forceMkdir(file.getParentFile());
				try (FileOutputStream fileOutputStream = FileUtils.openOutputStream(file);
					 BufferedOutputStream bufferedOutputStream = IOUtils.buffer(fileOutputStream)) {
					tarArchiveInputStream.transferTo(bufferedOutputStream);
				}
			}
			tarEntry = tarArchiveInputStream.getNextEntry();
		}
	}

	/**
	 * 压缩文件/目录到 TAR 文件。
	 * <p>将单个文件或目录（递归包含子目录）压缩为 TAR 格式文件。</p>
	 *
	 * @param inputFile  要压缩的文件或目录，必须存在且可读
	 * @param outputFile 输出 TAR 文件路径，会自动创建父目录并覆盖已存在文件
	 * @throws NullPointerException 当 {@code inputFile} 或 {@code outputFile} 为 {@code null} 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读（例如抛出 {@code FileNotFoundException}）</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误或磁盘空间不足</li>
	 *                              </ul>
	 * @since 1.0.0
	 */
	public static void compress(final File inputFile, final File outputFile) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		FileUtils.forceMkdirParent(outputFile);

		try (FileOutputStream outputStream = FileUtils.openOutputStream(outputFile);
			 BufferedOutputStream bufferedOutputStream = IOUtils.buffer(outputStream);
			 TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(bufferedOutputStream)) {
			compress(inputFile, tarArchiveOutputStream);
		}
	}

	/**
	 * 压缩文件/目录到输出流。
	 * <p>将单个文件或目录（递归包含子目录）压缩为 TAR 格式并写入输出流。</p>
	 *
	 * @param inputFile    要压缩的文件或目录，必须存在且可读
	 * @param outputStream 输出流对象，必须可写且非空（方法不会自动关闭此流）
	 * @throws NullPointerException 当 {@code inputFile} 或 {@code outputStream} 为 {@code null} 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读（例如抛出 {@code FileNotFoundException}）</li>
	 *                                  <li>输出流不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                              </ul>
	 * @since 1.0.0
	 */
	public static void compress(final File inputFile, final OutputStream outputStream) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		if (outputStream instanceof TarArchiveOutputStream) {
			compress(inputFile, (TarArchiveOutputStream) outputStream);
		} else {
			try (BufferedOutputStream bufferedOutputStream = IOUtils.buffer(outputStream);
				 TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(bufferedOutputStream)) {
				compress(inputFile, tarArchiveOutputStream);
			}
		}
	}

	/**
	 * 压缩文件/目录到 {@code TarArchiveOutputStream}。
	 * <p>将单个文件或目录（递归包含子目录）添加到已初始化的 TAR 输出流。</p>
	 *
	 * @param inputFile              要压缩的文件或目录，必须存在且可读
	 * @param tarArchiveOutputStream 已初始化的 TAR 输出流，必须可写且非空
	 * @throws NullPointerException 当 {@code inputFile} 或 {@code tarArchiveOutputStream} 为 {@code null} 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输入文件不存在或不可读（例如抛出 {@code FileNotFoundException}）</li>
	 *                                  <li>输出流不可写或已关闭</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                              </ul>
	 * @since 1.0.0
	 */
	public static void compress(final File inputFile, final TarArchiveOutputStream tarArchiveOutputStream) throws IOException {
		Validate.notNull(tarArchiveOutputStream, "tarArchiveOutputStream 不可为 null");
		FileUtils.check(inputFile, "inputFile 不可为 null");

		if (inputFile.isDirectory()) {
			addDir(inputFile, tarArchiveOutputStream, null);
		} else {
			addFile(inputFile, tarArchiveOutputStream, null);
		}
		tarArchiveOutputStream.finish();
	}

	/**
	 * 批量压缩文件/目录到 TAR 文件。
	 * <p>将多个文件或目录（递归包含子目录）压缩为单个 TAR 格式文件。</p>
	 *
	 * @param inputFiles 要压缩的文件/目录集合，可为 {@code null} 或空集合（此时创建空压缩包）
	 * @param outputFile 输出 TAR 文件路径，会自动创建父目录并覆盖已存在文件
	 * @throws NullPointerException 当 {@code outputFile} 为 {@code null} 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>集合中存在不存在或不可读的文件（例如抛出 {@code FileNotFoundException}）</li>
	 *                                  <li>输出文件不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误或磁盘空间不足</li>
	 *                              </ul>
	 * @since 1.0.0
	 */
	public static void compress(final Collection<File> inputFiles, final File outputFile) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		FileUtils.forceMkdirParent(outputFile);

		try (FileOutputStream outputStream = FileUtils.openOutputStream(outputFile);
			 BufferedOutputStream bufferedOutputStream = IOUtils.buffer(outputStream);
			 TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(bufferedOutputStream)) {
			compress(inputFiles, tarArchiveOutputStream);
		}
	}

	/**
	 * 批量压缩文件/目录到输出流。
	 * <p>将多个文件或目录（递归包含子目录）压缩为 TAR 格式并写入输出流。</p>
	 *
	 * @param inputFiles   要压缩的文件集合，自动过滤 {@code null} 和不存在的文件（不存在将抛异常）
	 * @param outputStream 输出流对象，必须可写且非空（方法不会自动关闭此流）
	 * @throws NullPointerException 当 {@code outputStream} 为 {@code null} 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>输出流不可写</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                              </ul>
	 * @since 1.0.0
	 */
	public static void compress(final Collection<File> inputFiles, final OutputStream outputStream) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		if (outputStream instanceof TarArchiveOutputStream) {
			compress(inputFiles, (TarArchiveOutputStream) outputStream);
		} else {
			try (BufferedOutputStream bufferedOutputStream = IOUtils.buffer(outputStream);
				 TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(bufferedOutputStream)) {
				compress(inputFiles, tarArchiveOutputStream);
			}
		}
	}

	/**
	 * 批量压缩文件/目录到 TAR 输出流。
	 * <p>将多个文件或目录（递归包含子目录）添加到已初始化的 TAR 输出流。</p>
	 *
	 * @param inputFiles             要压缩的文件/目录集合，可为 {@code null}（此时不添加任何内容）
	 * @param tarArchiveOutputStream 已初始化的 TAR 输出流，必须可写且非空
	 * @throws NullPointerException 当 {@code tarArchiveOutputStream} 为 {@code null} 时抛出
	 * @throws IOException          当发生以下情况时抛出：
	 *                              <ul>
	 *                                  <li>部分输入文件不存在或不可读（例如抛出 {@code FileNotFoundException}）</li>
	 *                                  <li>输出流不可写或已关闭</li>
	 *                                  <li>压缩过程中发生 I/O 错误</li>
	 *                              </ul>
	 * @since 1.0.0
	 */
	public static void compress(Collection<File> inputFiles, final TarArchiveOutputStream tarArchiveOutputStream) throws IOException {
		Validate.notNull(tarArchiveOutputStream, "tarArchiveOutputStream 不可为 null");
		inputFiles = Objects.isNull(inputFiles) ? Collections.emptyList() : inputFiles;
		for (File file : inputFiles) {
			if (FileUtils.notExist(file)) {
				throw new FileNotFoundException(file.getAbsolutePath());
			}
			if (file.isDirectory()) {
				addDir(file, tarArchiveOutputStream, null);
			} else {
				addFile(file, tarArchiveOutputStream, null);
			}
		}
		tarArchiveOutputStream.finish();
	}

	/**
	 * 将目录作为条目加入 TAR 输出流，并递归处理其子项。
	 * <p>
	 * 条目名的构造使用归档内的路径分隔符 {@link CompressConstants#PATH_SEPARATOR}：
	 * - 顶层目录：使用 {@code inputDir.getName() + PATH_SEPARATOR}
	 * - 有父前缀且父前缀以分隔符结尾：{@code parent + inputDir.getName() + PATH_SEPARATOR}
	 * - 有父前缀但不以分隔符结尾：{@code parent + PATH_SEPARATOR + inputDir.getName() + PATH_SEPARATOR}
	 * 目录条目统一以分隔符结尾以表示目录。
	 * </p>
	 * 目录本身作为空条目写入，随后枚举并递归添加其子目录与文件；空目录也会被记录为条目。
	 *
	 * @param inputDir               要写入的目录
	 * @param tarArchiveOutputStream 目标 TAR 输出流
	 * @param parent                 归档内的父路径前缀，空或空白表示顶层
	 * @throws IOException 写入目录条目或递归处理子项时发生的 I/O 异常
	 * @since 1.0.0
	 */
	protected static void addDir(File inputDir, TarArchiveOutputStream tarArchiveOutputStream, String parent) throws IOException {
		String entryName = inputDir.getName();
		if (StringUtils.isNotBlank(parent)) {
			if (parent.endsWith(CompressConstants.PATH_SEPARATOR)) {
				entryName = parent + inputDir.getName() + CompressConstants.PATH_SEPARATOR;
			} else {
				entryName = parent + CompressConstants.PATH_SEPARATOR + inputDir.getName() + CompressConstants.PATH_SEPARATOR;
			}
		}
		TarArchiveEntry archiveEntry = new TarArchiveEntry(inputDir, entryName);
		tarArchiveOutputStream.putArchiveEntry(archiveEntry);
		tarArchiveOutputStream.closeArchiveEntry();

		File[] childFiles = ArrayUtils.nullToEmpty(inputDir.listFiles(), File[].class);
		for (File childFile : childFiles) {
			if (childFile.isDirectory()) {
				addDir(childFile, tarArchiveOutputStream, entryName);
			} else {
				addFile(childFile, tarArchiveOutputStream, entryName);
			}
		}
	}

	/**
	 * 将文件作为条目加入 TAR 输出流并写入其内容。
	 * <p>
	 * 条目名的构造使用归档内的路径分隔符 {@link CompressConstants#PATH_SEPARATOR}：
	 * - 顶层文件：使用 {@code inputFile.getName()}
	 * - 有父前缀且父前缀以分隔符结尾：{@code parent + inputFile.getName()}
	 * - 有父前缀但不以分隔符结尾：{@code parent + PATH_SEPARATOR + inputFile.getName()}
	 * </p>
	 * 输入流采用 try-with-resources 自动关闭；条目内容通过 {@link java.io.InputStream#transferTo(java.io.OutputStream)} 写入。
	 *
	 * @param inputFile              要写入的文件
	 * @param tarArchiveOutputStream 目标 TAR 输出流
	 * @param parent                 归档内的父路径前缀，空或空白表示顶层
	 * @throws IOException 打开文件或写入条目内容时发生的 I/O 异常（包括 {@link java.io.FileNotFoundException}）
	 * @since 1.0.0
	 */
	protected static void addFile(File inputFile, TarArchiveOutputStream tarArchiveOutputStream, String parent) throws IOException {
		try (InputStream inputStream = FileUtils.openUnsynchronizedBufferedInputStream(inputFile)) {
			String entryName = inputFile.getName();
			if (StringUtils.isNotBlank(parent)) {
				if (parent.endsWith(CompressConstants.PATH_SEPARATOR)) {
					entryName = parent + inputFile.getName();
				} else {
					entryName = parent + CompressConstants.PATH_SEPARATOR + inputFile.getName();
				}
			}
			TarArchiveEntry archiveEntry = new TarArchiveEntry(inputFile, entryName);
			tarArchiveOutputStream.putArchiveEntry(archiveEntry);
			inputStream.transferTo(tarArchiveOutputStream);
			tarArchiveOutputStream.closeArchiveEntry();
		}
	}
}