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
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * 7z 压缩/解压工具类。
 * <p>基于 Apache Commons Compress 提供 7z 的压缩与解压能力。</p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li><strong>多输入源</strong>：支持文件、字节数组、{@code SevenZFile}。</li>
 *   <li><strong>目录递归</strong>：保持原始目录层级结构进行压缩。</li>
 *   <li><strong>格式校验</strong>：通过 Tika 的 MIME 类型检测判断 7z 格式（文件版本在调用前校验；流式读取时错误体现在 I/O 失败）。</li>
 *   <li><strong>性能优化</strong>：流式传输与缓冲处理，适合大文件。</li>
 *   <li><strong>资源管理</strong>：使用 try-with-resources 自动释放资源。</li>
 * </ul>
 *
 * <h3>线程安全</h3>
 * <p>类本身无共享状态，方法均为静态；并发处理不同文件/目录是安全的。对同一路径或同一输出文件并发写入可能发生冲突或覆盖。</p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 1) 压缩单个文件到 .7z
 * SevenZUtils.compress(new File("input.txt"), new File("archive.7z"));
 *
 * // 2) 压缩目录到 SevenZOutputFile（不会自动关闭传入对象）
 * try (org.apache.commons.compress.archivers.sevenz.SevenZOutputFile szf =
 *          new org.apache.commons.compress.archivers.sevenz.SevenZOutputFile(new File("archive.7z"))) {
 *     SevenZUtils.compress(new File("inputDir"), szf);
 * }
 *
 * // 3) 批量压缩多个文件/目录到 .7z
 * java.util.List<File> inputs = java.util.List.of(new File("a.txt"), new File("b"), new File("c"));
 * SevenZUtils.compress(inputs, new File("batch.7z"));
 *
 * // 4) 解压 7z 文件到目录（文件版本会先进行 MIME 类型校验）
 * SevenZUtils.uncompress(new File("archive.7z"), new File("outputDir"));
 *
 * // 5) 使用 SevenZFile 解压（适合随机访问和大文件）
 * try (org.apache.commons.compress.archivers.sevenz.SevenZFile zf =
 *          org.apache.commons.compress.archivers.sevenz.SevenZFile.builder().setFile(new File("archive.7z")).get()) {
 *     SevenZUtils.uncompress(zf, new File("outputDir"));
 * }
 * }</pre>
 *
 * @author pangju666
 * @since 1.0.0
 * @see SevenZFile
 * @see SevenZOutputFile
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
	 * @throws IOException 当文件访问发生I/O异常时抛出
	 * @since 1.0.0
	 */
	public static boolean is7z(final File file) throws IOException {
		return FileUtils.isMimeType(file, CompressConstants.SEVEN_Z_MIME_TYPE);
	}

	/**
	 * 检查字节数组内容是否为有效的 7z 格式。
	 * <p>基于 Tika 的 MIME 类型检测。</p>
	 *
	 * @param bytes 待检测的字节数组；为 {@code null} 或空数组将返回 {@code false}
	 * @return 当且仅当字节数组非空且检测为 {@code application/x-7z-compressed} 时返回 {@code true}
	 * @since 1.0.0
	 */
	public static boolean is7z(final byte[] bytes) {
		return ArrayUtils.isNotEmpty(bytes) &&
			IOConstants.getDefaultTika().detect(bytes).equals(CompressConstants.SEVEN_Z_MIME_TYPE);
	}

	/**
	 * 检查输入流内容是否为有效的 7z 格式。
	 * <p>基于 Tika 的 MIME 类型检测。</p>
	 *
	 * @param inputStream 待检测的输入流，非空
	 * @return 当且仅当输入流非空且检测为 {@code application/x-7z-compressed} 时返回 {@code true}
	 * @throws NullPointerException 当 {@code inputStream} 为 {@code null} 时抛出
	 * @throws IOException          当流读取发生 I/O 错误时抛出
	 * @since 1.0.0
	 */
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
	 */
	public static void uncompress(final File inputFile, final File outputDir) throws IOException {
		Validate.notNull(inputFile, "inputFile 不可为 null");

		String mimeType = FileUtils.getMimeType(inputFile);
		if (!CompressConstants.SEVEN_Z_MIME_TYPE.equals(mimeType)) {
			throw new IllegalArgumentException(inputFile.getAbsolutePath() + "不是7z类型文件");
		}
		try (SevenZFile sevenZFile = SevenZFile.builder().setFile(inputFile).get()) {
			uncompress(sevenZFile, outputDir);
		}
	}

	/**
	 * 从SevenZFile对象解压缩到指定目录
	 *
	 * @param sevenZFile 已初始化的SevenZFile对象，必须处于可读取状态且不为null
	 * @param outputDir 解压目标目录，会自动创建不存在的目录结构
	 * @throws NullPointerException 当sevenZFile或outputDir为null时抛出
	 * @throws IllegalArgumentException 当出现以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>outputDir存在但不是目录</li>
	 *                                  </ul>
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>sevenZFile已关闭或不可读</li>
	 *                         <li>输出目录不可写</li>
	 *                         <li>解压过程中发生I/O错误</li>
	 *                         <li>磁盘空间不足</li>
	 *                     </ul>
	 * @since 1.0.0
	 */
	public static void uncompress(final SevenZFile sevenZFile, final File outputDir) throws IOException {
		Validate.notNull(sevenZFile, "sevenZFile 不可为 null");
		FileUtils.forceMkdir(outputDir);

		SevenZArchiveEntry archiveEntry = sevenZFile.getNextEntry();
		while (Objects.nonNull(archiveEntry)) {
			File file = new File(outputDir, archiveEntry.getName());
			if (archiveEntry.isDirectory()) {
				if (!file.exists()) {
					FileUtils.forceMkdir(file);
				}
			} else {
				try (InputStream inputStream = sevenZFile.getInputStream(archiveEntry);
					 FileOutputStream fileOutputStream = FileUtils.openOutputStream(file);
					 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {
					inputStream.transferTo(bufferedOutputStream);
				}
			}
			archiveEntry = sevenZFile.getNextEntry();
		}
	}

	/**
	 * 压缩文件/目录到 7z 文件。
	 * <p>将单个文件或目录（递归包含子目录）压缩为 7z 格式文件。</p>
	 *
	 * @param inputFile  要压缩的文件或目录，必须存在且可读
	 * @param outputFile 输出 7z 文件路径，会自动创建父目录并覆盖已存在文件
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

		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile)) {
			compress(inputFile, sevenZOutputFile);
		}
	}

	/**
	 * 压缩文件/目录到SevenZOutputFile对象
	 *
	 * @param inputFile 要压缩的文件或目录，必须存在且可读
	 * @param sevenZOutputFile 已初始化的SevenZOutputFile对象，必须处于可写入状态且不为null
	 * @throws NullPointerException 当inputFile或sevenZOutputFile为null时抛出
	 * @throws FileNotFoundException 当inputFile不存在时抛出
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输入文件不可读</li>
	 *                         <li>sevenZOutputFile已关闭或不可写</li>
	 *                         <li>压缩过程中发生I/O错误</li>
	 *                         <li>磁盘空间不足</li>
	 *                     </ul>
	 * @since 1.0.0
	 */
	public static void compress(final File inputFile, final SevenZOutputFile sevenZOutputFile) throws IOException {
		Validate.notNull(inputFile, "inputFile 不可为 null");
		Validate.notNull(sevenZOutputFile, "sevenZOutputFile 不可为 null");
		if (!inputFile.exists()) {
			throw new FileNotFoundException(inputFile.getAbsolutePath());
		}

		if (inputFile.isDirectory()) {
			addDir(inputFile, sevenZOutputFile, null);
		} else {
			addFile(inputFile, sevenZOutputFile, null);
		}
		sevenZOutputFile.finish();
	}

	/**
	 * 压缩多个文件/目录到 7z 文件。
	 * <p>将多个文件或目录（递归包含子目录）压缩为单个 7z 格式文件。</p>
	 *
	 * @param inputFiles 要压缩的文件/目录集合，可为 {@code null} 或空集合（此时创建空压缩包）
	 * @param outputFile 输出 7z 文件路径，会自动创建父目录并覆盖已存在文件
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

		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile)) {
			compress(inputFiles, sevenZOutputFile);
		}
	}

	/**
	 * 压缩多个文件/目录到SevenZOutputFile对象
	 *
	 * @param inputFiles 要压缩的文件/目录集合，可为null或空集合（此时不添加任何内容）
	 * @param sevenZOutputFile 已初始化的SevenZOutputFile对象，必须处于可写入状态且不为null
	 * @throws NullPointerException 当sevenZOutputFile为null时抛出
	 * @throws FileNotFoundException 当集合中存在不存在的文件时抛出
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输入文件不可读</li>
	 *                         <li>sevenZOutputFile已关闭或不可写</li>
	 *                         <li>压缩过程中发生I/O错误</li>
	 *                         <li>磁盘空间不足</li>
	 *                     </ul>
	 * @since 1.0.0
	 */
	public static void compress(Collection<File> inputFiles, final SevenZOutputFile sevenZOutputFile) throws IOException {
		Validate.notNull(sevenZOutputFile, "sevenZOutputFile 不可为 null");

		inputFiles = Objects.isNull(inputFiles) ? Collections.emptyList() : inputFiles;
		for (File file : inputFiles) {
			if (FileUtils.notExist(file)) {
				throw new FileNotFoundException(file.getAbsolutePath());
			}
			if (file.isDirectory()) {
				addDir(file, sevenZOutputFile, null);
			} else {
				addFile(file, sevenZOutputFile, null);
			}
		}
		sevenZOutputFile.finish();
	}

	/**
	 * 递归添加目录到7z压缩流
	 *
	 * @param inputFile 要添加的目录，必须存在且可读
	 * @param outputFile 已初始化的SevenZOutputFile对象，必须处于可写入状态
	 * @param parent 父目录相对路径（用于构建压缩包内路径），可为null
	 * @throws NullPointerException 当inputFile或outputFile为null时抛出
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>目录不可读</li>
	 *                         <li>outputFile已关闭或不可写</li>
	 *                         <li>压缩过程中发生I/O错误</li>
	 *                         <li>磁盘空间不足</li>
	 *                     </ul>
	 * @since 1.0.0
	 */
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
	 * 添加单个文件到7z压缩流
	 *
	 * @param inputFile 要添加的文件，必须存在且可读
	 * @param outputFile 已初始化的SevenZOutputFile对象，必须处于可写入状态
	 * @param parent 父目录相对路径（用于构建压缩包内路径），可为null
	 * @throws NullPointerException 当inputFile或outputFile为null时抛出
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>文件不可读</li>
	 *                         <li>outputFile已关闭或不可写</li>
	 *                         <li>压缩过程中发生I/O错误</li>
	 *                         <li>磁盘空间不足</li>
	 *                     </ul>
	 * @since 1.0.0
	 */
	protected static void addFile(final File inputFile, final SevenZOutputFile outputFile, final String parent) throws IOException {
		try (InputStream inputStream = FileUtils.openUnsynchronizedBufferedInputStream(inputFile)) {
			String archiveEntryName = StringUtils.isNotBlank(parent) ? parent + CompressConstants.PATH_SEPARATOR +
				inputFile.getName() : inputFile.getName();
			SevenZArchiveEntry archiveEntry = outputFile.createArchiveEntry(inputFile, archiveEntryName);
			outputFile.putArchiveEntry(archiveEntry);
			outputFile.write(inputStream);
			outputFile.closeArchiveEntry();
		}
	}
}