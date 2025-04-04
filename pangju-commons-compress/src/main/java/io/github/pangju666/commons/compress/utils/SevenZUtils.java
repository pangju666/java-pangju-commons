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
import org.apache.commons.io.input.UnsynchronizedBufferedInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * 7z压缩解压工具类
 * <p>基于Apache Commons Compress实现的7z格式压缩工具，支持LZMA2等高效压缩算法。</p>
 *
 * <h3>核心功能：</h3>
 * <ul>
 *     <li><strong>高效压缩</strong> - 支持LZMA2、BZip2等7z专有压缩算法，提供高压缩率</li>
 *     <li><strong>递归处理</strong> - 自动遍历目录结构进行压缩，保持原始文件结构</li>
 *     <li><strong>格式校验</strong> - 通过文件魔数和Tika检测确保7z文件有效性</li>
 *     <li><strong>大文件支持</strong> - 采用流式处理降低内存消耗，支持GB级文件处理</li>
 *     <li><strong>线程安全</strong> - 所有方法均为静态方法，可安全用于多线程环境</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 压缩单个文件
 * SevenZUtils.compress(new File("input.txt"), new File("output.7z"));
 *
 * // 解压缩文件
 * SevenZUtils.unCompress(new File("archive.7z"), new File("outputDir"));
 * }</pre>
 *
 * @author pangju666
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
	 * @throws IOException 当文件访问发生I/O异常时抛出
	 * @throws SecurityException 当没有文件读取权限时抛出
	 * @see FileUtils#isMimeType(File, String)
	 * @since 1.0.0
	 */
	public static boolean is7z(final File file) throws IOException {
		return FileUtils.isMimeType(file, CompressConstants.SEVEN_Z_MIME_TYPE);
	}

	/**
	 * 检查字节数组内容是否为有效的7z压缩格式
	 * <p>通过检测字节数组的魔数(Magic Number)和Tika内容分析判断是否为7z格式</p>
	 *
	 * @param bytes 待检测的字节数组，不可为null或空数组
	 * @return 当且仅当满足以下条件时返回true：
	 *         <ul>
	 *             <li>字节数组非null且非空</li>
	 *             <li>内容检测为7z格式(魔数为"7z"开头)</li>
	 *         </ul>
	 * @throws IllegalArgumentException 当bytes为空数组时抛出
	 * @see IOConstants#getDefaultTika()
	 * @see CompressConstants#SEVEN_Z_MIME_TYPE
	 * @since 1.0.0
	 */
	public static boolean is7z(final byte[] bytes) {
		return ArrayUtils.isNotEmpty(bytes) &&
			IOConstants.getDefaultTika().detect(bytes).equals(CompressConstants.SEVEN_Z_MIME_TYPE);
	}

	/**
	 * 检查输入流内容是否为有效的7z压缩格式
	 * <p>通过检测输入流的魔数(Magic Number)和Tika内容分析判断是否为7z格式</p>
	 *
	 * @param inputStream 待检测的输入流，必须支持mark/reset操作以便格式检测
	 * @return 当且仅当满足以下条件时返回true：
	 *         <ul>
	 *             <li>输入流非null</li>
	 *             <li>内容检测为7z格式(魔数为"7z"开头)</li>
	 *         </ul>
	 * @throws NullPointerException 当inputStream为null时抛出
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>流读取发生I/O错误</li>
	 *                         <li>流不支持mark/reset操作</li>
	 *                     </ul>
	 * @see IOConstants#getDefaultTika()
	 * @see CompressConstants#SEVEN_Z_MIME_TYPE
	 * @since 1.0.0
	 */
	public static boolean is7z(final InputStream inputStream) throws IOException {
		return Objects.nonNull(inputStream) &&
			IOConstants.getDefaultTika().detect(inputStream).equals(CompressConstants.SEVEN_Z_MIME_TYPE);
	}

	/**
	 * 解压缩7z文件到指定目录
	 *
	 * @param inputFile 要解压的7z文件，必须存在且可读
	 * @param outputDir 解压输出目录，会自动创建不存在的目录结构
	 * @throws NullPointerException 当inputFile或outputDir为null时抛出
	 * @throws IllegalArgumentException 当出现以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>inputFile不是有效的7z格式</li>
	 *                                      <li>outputDir存在但不是目录</li>
	 *                                  </ul>
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输入文件不存在或不可读</li>
	 *                         <li>输出目录不可写</li>
	 *                         <li>解压过程中发生I/O错误</li>
	 *                     </ul>
	 * @throws SecurityException 当没有文件系统操作权限时抛出
	 * @see #unCompress(SevenZFile, File)
	 * @since 1.0.0
	 */
	public static void unCompress(final File inputFile, final File outputDir) throws IOException {
		Validate.notNull(inputFile, "inputFile 不可为 null");

		String mimeType = FileUtils.getMimeType(inputFile);
		if (!CompressConstants.SEVEN_Z_MIME_TYPE.equals(mimeType)) {
			throw new IllegalArgumentException(inputFile.getAbsolutePath() + "不是7z类型文件");
		}
		try (SevenZFile sevenZFile = SevenZFile.builder().setFile(inputFile).get()) {
			unCompress(sevenZFile, outputDir);
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
	 * @throws SecurityException 当没有文件系统操作权限时抛出
	 * @see #unCompress(File, File)
	 * @since 1.0.0
	 */
	public static void unCompress(final SevenZFile sevenZFile, final File outputDir) throws IOException {
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
	 * 压缩文件/目录到7z文件
	 *
	 * @param inputFile 要压缩的文件或目录，必须存在且可读
	 * @param outputFile 输出7z文件路径，会自动覆盖已存在文件
	 * @throws NullPointerException 当inputFile或outputFile为null时抛出
	 * @throws IllegalArgumentException 当出现以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>inputFile不存在</li>
	 *                                      <li>outputFile存在但不是文件</li>
	 *                                  </ul>
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输入文件不可读</li>
	 *                         <li>输出路径不可写</li>
	 *                         <li>磁盘空间不足</li>
	 *                         <li>压缩过程中发生I/O错误</li>
	 *                     </ul>
	 * @throws SecurityException 当没有文件系统操作权限时抛出
	 * @see #compress(File, SevenZOutputFile)
	 * @since 1.0.0
	 */
	public static void compress(final File inputFile, final File outputFile) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
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
	 * @throws SecurityException 当没有文件系统操作权限时抛出
	 * @see #compress(File, File)
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
	 * 压缩多个文件/目录到7z文件
	 *
	 * @param inputFiles 要压缩的文件/目录集合，可为null或空集合（此时创建空压缩包）
	 * @param outputFile 输出7z文件路径，会自动覆盖已存在文件
	 * @throws NullPointerException 当outputFile为null时抛出
	 * @throws IllegalArgumentException 当出现以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>outputFile存在但不是文件</li>
	 *                                      <li>集合中存在不存在的文件</li>
	 *                                  </ul>
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输入文件不可读</li>
	 *                         <li>输出路径不可写</li>
	 *                         <li>磁盘空间不足</li>
	 *                         <li>压缩过程中发生I/O错误</li>
	 *                     </ul>
	 * @throws SecurityException 当没有文件系统操作权限时抛出
	 * @see #compress(Collection, SevenZOutputFile)
	 * @since 1.0.0
	 */
	public static void compress(final Collection<File> inputFiles, final File outputFile) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
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
	 * @throws SecurityException 当没有文件系统操作权限时抛出
	 * @see #compress(Collection, File)
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
	 * @throws SecurityException 当没有文件系统操作权限时抛出
	 * @see #addFile(File, SevenZOutputFile, String)
	 * @since 1.0.0
	 */
	protected static void addDir(final File inputFile, final SevenZOutputFile outputFile, final String parent) throws IOException {
		String archiveEntryName = StringUtils.isNotBlank(parent) ?
			parent + CompressConstants.PATH_SEPARATOR + inputFile.getName() : inputFile.getName();
		SevenZArchiveEntry archiveEntry = outputFile.createArchiveEntry(inputFile, archiveEntryName + CompressConstants.PATH_SEPARATOR);
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
	 * @throws SecurityException 当没有文件系统操作权限时抛出
	 * @see #addDir(File, SevenZOutputFile, String)
	 * @since 1.0.0
	 */
	protected static void addFile(final File inputFile, final SevenZOutputFile outputFile, final String parent) throws IOException {
		try (UnsynchronizedBufferedInputStream inputStream = FileUtils.openUnsynchronizedBufferedInputStream(inputFile)) {
			String archiveEntryName = StringUtils.isNotBlank(parent) ?
				parent + CompressConstants.PATH_SEPARATOR + inputFile.getName() : inputFile.getName();
			SevenZArchiveEntry archiveEntry = outputFile.createArchiveEntry(inputFile, archiveEntryName);
			outputFile.putArchiveEntry(archiveEntry);
			outputFile.write(inputStream);
			outputFile.closeArchiveEntry();
		}
	}
}