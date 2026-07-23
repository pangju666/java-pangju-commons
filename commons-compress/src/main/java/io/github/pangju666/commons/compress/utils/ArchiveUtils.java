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

import io.github.pangju666.commons.compress.lang.CompressConstants;
import io.github.pangju666.commons.io.utils.FileUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 通用压缩文件工具类。
 * <p>提供基于 Apache Commons Compress 的通用压缩/解压能力，支持多种压缩格式（如 zip、tar、7z 等）。</p>
 * <p>此类是 ZipUtils 和 TarUtils 的底层实现，通过泛型和函数式接口提供格式无关的压缩/解压操作。</p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li><strong>通用性</strong>：通过泛型支持多种压缩格式，不依赖特定实现。</li>
 *   <li><strong>目录递归</strong>：保持原始目录层级结构进行压缩。</li>
 *   <li><strong>流式处理</strong>：支持基于流和迭代器的解压操作，适合大文件处理。</li>
 *   <li><strong>资源管理</strong>：使用 try-with-resources 自动释放资源。</li>
 *   <li><strong>条目处理</strong>：支持通过 Consumer 对每个归档条目进行自定义处理。</li>
 * </ul>
 *
 * <h3>线程安全</h3>
 * <p>类本身无共享状态，方法均为静态；并发处理不同文件/目录是安全的。对同一路径或同一输出文件并发写入可能发生冲突或覆盖。</p>
 *
 * <h3>使用示例</h3>
 * <p>此类通常不直接使用，而是通过 ZipUtils、TarUtils 等特定格式的工具类调用。以下为底层使用示例：</p>
 * <pre>{@code
 * // 1) 使用 ArchiveInputStream 解压
 * try (TarArchiveInputStream tais = new TarArchiveInputStream(inputStream)) {
 *     ArchiveUtils.extract(tais, new File("outputDir"));
 * }
 *
 * // 2) 使用迭代器和提取器解压（适用于 7z 等格式）
 * try (SevenZFile zf = SevenZFile.builder().setFile(file).get()) {
 *     ArchiveUtils.extract(zf.getEntries().iterator(), new File("outputDir"), zf::getInputStream);
 * }
 *
 * // 3) 压缩单个文件（底层调用）
 * try (TarArchiveOutputStream taos = new TarArchiveOutputStream(outputStream)) {
 *     ArchiveUtils.archive(new File("input.txt"), taos, entry -> {
 *         // 自定义条目处理逻辑
 *         System.out.println("Adding: " + entry.getName());
 *     });
 * }
 *
 * // 4) 批量压缩多个文件（底层调用）
 * try (ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(outputStream)) {
 *     List<File> files = List.of(new File("a.txt"), new File("b.txt"));
 *     ArchiveUtils.archive(files, zaos, entry -> {
 *         // 自定义条目处理逻辑
 *         entry.setComment("Compressed by ArchiveUtils");
 *     });
 * }
 * }</pre>
 *
 * @author pangju666
 * @see ArchiveEntry
 * @see ArchiveInputStream
 * @see ArchiveOutputStream
 * @see ZipUtils
 * @see TarUtils
 * @since 2.1.0
 */
public class ArchiveUtils {
	/**
	 * 受保护的构造函数，防止实例化。
	 */
	protected ArchiveUtils() {
	}

	/**
	 * 使用迭代器和提取器解压缩压缩文件到指定目录。
	 * <p>通过迭代器遍历压缩条目，使用提取器获取每个条目的输入流，将内容解压到指定目录。</p>
	 *
	 * @param <T>            压缩条目类型，必须继承自 {@link ArchiveEntry}
	 * @param archiveEntries 压缩条目迭代器，必须非 null
	 * @param outputDir      解压目标目录，会自动创建不存在的目录结构
	 * @param extractor      压缩条目提取器，用于从条目获取输入流，必须非 null
	 * @throws NullPointerException 当 {@code archiveEntries}、{@code outputDir} 或 {@code extractor} 为 null 时抛出
	 * @throws IOException          当输出目录不可写、解压过程中发生 I/O 错误或磁盘空间不足时抛出
	 * @since 2.1.0
	 */
	static <T extends ArchiveEntry> void extract(final Iterator<T> archiveEntries, final File outputDir,
	                                                final ArchiveEntryExtractor<T> extractor) throws IOException {
		Validate.notNull(archiveEntries, "archiveEntries 不可为 null");
		FileUtils.checkDirIfExist(outputDir, "outputDir 不可为 null");
		Validate.notNull(extractor, "extractor 不可为 null");

		FileUtils.forceMkdir(outputDir);

		while (archiveEntries.hasNext()) {
			T archiveEntry = archiveEntries.next();
			File archiveEntryFile = new File(outputDir, archiveEntry.getName());

			if (archiveEntry.isDirectory()) {
				if (!archiveEntryFile.exists()) {
					FileUtils.forceMkdir(archiveEntryFile);
				}
			} else {
				FileUtils.forceMkdir(archiveEntryFile.getParentFile());

				try (BufferedOutputStream bufferedOutputStream = FileUtils.newBufferedOutputStream(archiveEntryFile);
				     InputStream inputStream = extractor.extractor(archiveEntry)) {
					inputStream.transferTo(bufferedOutputStream);
				}
			}
		}
	}

	/**
	 * 使用压缩输入流解压缩到指定目录。
	 * <p>通过压缩输入流读取压缩条目，将内容解压到指定目录，自动创建不存在的目录结构并保持原始文件层级关系。</p>
	 *
	 * @param <T>                压缩条目类型，必须继承自 {@link ArchiveEntry}
	 * @param archiveInputStream 压缩输入流，必须非 null
	 * @param outputDir          解压目标目录，会自动创建不存在的目录结构
	 * @throws NullPointerException 当 {@code archiveInputStream} 或 {@code outputDir} 为 null 时抛出
	 * @throws IOException          当输出目录不可写、解压过程中发生 I/O 错误或磁盘空间不足时抛出
	 * @since 2.1.0
	 */
	public static <T extends ArchiveEntry> void extract(final ArchiveInputStream<T> archiveInputStream,
	                                                       final File outputDir) throws IOException {
		Validate.notNull(archiveInputStream, "archiveInputStream 不可为 null");
		FileUtils.checkDirIfExist(outputDir, "outputDir 不可为 null");

		FileUtils.forceMkdir(outputDir);

		T archiveEntry = archiveInputStream.getNextEntry();
		while (Objects.nonNull(archiveEntry)) {
			File file = new File(outputDir, archiveEntry.getName());

			if (archiveEntry.isDirectory()) {
				if (!file.exists()) {
					FileUtils.forceMkdir(file);
				}
			} else {
				FileUtils.forceMkdir(file.getParentFile());

				try (BufferedOutputStream bufferedOutputStream = FileUtils.newBufferedOutputStream(file)) {
					archiveInputStream.transferTo(bufferedOutputStream);
				}
			}

			archiveEntry = archiveInputStream.getNextEntry();
		}
	}

	/**
	 * 压缩单个文件或目录到归档输出流。
	 * <p>将文件或目录（递归包含子目录）压缩到指定的归档输出流中，通过 Consumer 对每个归档条目进行自定义处理。</p>
	 *
	 * @param <T>                  归档条目类型，必须继承自 {@link ArchiveEntry}
	 * @param inputFile            要压缩的文件或目录，必须存在且可读
	 * @param archiveOutputStream  归档输出流，必须非 null
	 * @param archiveEntryConsumer 归档条目处理器，可为 null
	 * @throws NullPointerException     当 {@code archiveOutputStream} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFile} 为 null 或不存在时抛出
	 * @throws IOException              当文件读取失败或写入输出流失败时抛出
	 * @since 2.1.0
	 */
	public static <T extends ArchiveEntry> void archive(final File inputFile,
	                                                     final ArchiveOutputStream<T> archiveOutputStream,
	                                                     final Consumer<T> archiveEntryConsumer) throws IOException {
		Validate.notNull(archiveOutputStream, "archiveOutputStream 不可为 null");
		FileUtils.check(inputFile, "inputFile 不可为 null");

		if (inputFile.isDirectory()) {
			addDir(inputFile, archiveOutputStream, null, archiveEntryConsumer);
		} else {
			addFile(inputFile, archiveOutputStream, null, archiveEntryConsumer);
		}
	}

	/**
	 * 批量压缩文件或目录到归档输出流。
	 * <p>将多个文件或目录（递归包含子目录）压缩到指定的归档输出流中，通过 Consumer 对每个归档条目进行自定义处理。</p>
	 *
	 * @param <T>                  归档条目类型，必须继承自 {@link ArchiveEntry}
	 * @param inputFiles           要压缩的文件/目录集合，必须非空且所有文件必须存在
	 * @param archiveOutputStream  归档输出流，必须非 null
	 * @param archiveEntryConsumer 归档条目处理器，可为 null
	 * @throws NullPointerException     当 {@code archiveOutputStream} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code inputFiles} 为空或包含 null 或不存在的文件/目录时抛出
	 * @throws IOException              当文件读取失败或写入输出流失败时抛出
	 * @since 2.1.0
	 */
	public static <T extends ArchiveEntry> void archive(final Collection<File> inputFiles,
	                                                     final ArchiveOutputStream<T> archiveOutputStream,
	                                                     final Consumer<T> archiveEntryConsumer) throws IOException {
		Validate.notNull(archiveOutputStream, "archiveOutputStream 不可为 null");
		Validate.notEmpty(inputFiles, "inputFiles 不可为空");
		Validate.isTrue(inputFiles.stream().allMatch(FileUtils::exist),
			"inputFiles 中存在为 null 或不存在的文件/目录");

		for (File file : inputFiles) {
			if (file.isDirectory()) {
				addDir(file, archiveOutputStream, null, archiveEntryConsumer);
			} else {
				addFile(file, archiveOutputStream, null, archiveEntryConsumer);
			}
		}
	}

	/**
	 * 递归添加目录到归档输出流。
	 * <p>
	 * 将目录及其所有子目录和文件递归添加到归档输出流中，保持原始目录结构。
	 * 条目名的构造使用归档内的路径分隔符 {@link CompressConstants#PATH_SEPARATOR}。
	 * </p>
	 *
	 * @param <T>                  归档条目类型，必须继承自 {@link ArchiveEntry}
	 * @param inputDir             要添加的目录，必须可读
	 * @param archiveOutputStream  归档输出流
	 * @param parent               归档内的父路径前缀，空或空白表示顶层
	 * @param archiveEntryConsumer 归档条目处理器，可为 null
	 * @throws IOException 当写入目录条目或递归处理子项时发生 I/O 异常
	 * @since 2.1.0
	 */
	private static <T extends ArchiveEntry> void addDir(final File inputDir, final ArchiveOutputStream<T> archiveOutputStream,
	                                                    final String parent, final Consumer<T> archiveEntryConsumer) throws IOException {
		String entryName = inputDir.getName();
		if (StringUtils.isNotBlank(parent)) {
			if (parent.endsWith(CompressConstants.PATH_SEPARATOR)) {
				entryName = parent + inputDir.getName() + CompressConstants.PATH_SEPARATOR;
			} else {
				entryName = parent + CompressConstants.PATH_SEPARATOR + inputDir.getName() + CompressConstants.PATH_SEPARATOR;
			}
		}
		T archiveEntry = archiveOutputStream.createArchiveEntry(inputDir, entryName);
		if (Objects.nonNull(archiveEntryConsumer)) {
			archiveEntryConsumer.accept(archiveEntry);
		}
		archiveOutputStream.putArchiveEntry(archiveEntry);
		archiveOutputStream.closeArchiveEntry();

		File[] childFiles = ArrayUtils.nullToEmpty(inputDir.listFiles(), File[].class);
		for (File childFile : childFiles) {
			if (childFile.isDirectory()) {
				addDir(childFile, archiveOutputStream, entryName, archiveEntryConsumer);
			} else {
				addFile(childFile, archiveOutputStream, entryName, archiveEntryConsumer);
			}
		}
	}

	/**
	 * 添加文件到归档输出流并写入其内容。
	 * <p>
	 * 条目名的构造使用归档内的路径分隔符 {@link CompressConstants#PATH_SEPARATOR}。
	 * 输入流采用 try-with-resources 自动关闭；条目内容通过 {@link InputStream#transferTo(OutputStream)} 写入。
	 * </p>
	 *
	 * @param <T>                  归档条目类型，必须继承自 {@link ArchiveEntry}
	 * @param inputFile            要添加的文件，必须可读
	 * @param archiveOutputStream  归档输出流
	 * @param parent               归档内的父路径前缀，空或空白表示顶层
	 * @param archiveEntryConsumer 归档条目处理器，可为 null
	 * @throws IOException 当打开文件或写入条目内容时发生 I/O 异常
	 * @since 2.1.0
	 */
	private static <T extends ArchiveEntry> void addFile(final File inputFile, final ArchiveOutputStream<T> archiveOutputStream,
	                                                     final String parent, final Consumer<T> archiveEntryConsumer) throws IOException {
		try (InputStream inputStream = FileUtils.openBufferedFileChannelInputStream(inputFile)) {
			String entryName = inputFile.getName();
			if (StringUtils.isNotBlank(parent)) {
				if (parent.endsWith(CompressConstants.PATH_SEPARATOR)) {
					entryName = parent + inputFile.getName();
				} else {
					entryName = parent + CompressConstants.PATH_SEPARATOR + inputFile.getName();
				}
			}
			T archiveEntry = archiveOutputStream.createArchiveEntry(inputFile, entryName);
			if (Objects.nonNull(archiveEntryConsumer)) {
				archiveEntryConsumer.accept(archiveEntry);
			}
			archiveOutputStream.putArchiveEntry(archiveEntry);
			inputStream.transferTo(archiveOutputStream);
			archiveOutputStream.closeArchiveEntry();
		}
	}

	/**
	 * 压缩条目提取器函数式接口。
	 * <p>用于从压缩条目中提取输入流，以便进行解压操作。</p>
	 *
	 * <h3>使用示例</h3>
	 * <pre>{@code
	 * // 使用 SevenZFile 作为提取器
	 * try (SevenZFile zf = SevenZFile.builder().setFile(file).get()) {
	 *     ArchiveUtils.uncompress(zf.getEntries().iterator(), outputDir, zf::getInputStream);
	 * }
	 * }</pre>
	 *
	 * @param <T> 压缩条目类型，必须继承自 {@link ArchiveEntry}
	 * @since 2.1.0
	 */
	@FunctionalInterface
	interface ArchiveEntryExtractor<T extends ArchiveEntry> {
		/**
		 * 从压缩条目提取输入流。
		 *
		 * @param entry 压缩条目对象
		 * @return 条目内容的输入流
		 * @throws IOException 当提取输入流失败时抛出
		 * @since 2.1.0
		 */
		InputStream extractor(T entry) throws IOException;
	}
}
