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
 * <p>基于Apache Commons Compress实现的7z格式压缩工具，主要特性：</p>
 *
 * <h3>核心功能：</h3>
 * <ul>
 *     <li><strong>高效压缩</strong> - 支持LZMA2等7z专有压缩算法</li>
 *     <li><strong>递归处理</strong> - 自动遍历目录结构进行压缩</li>
 *     <li><strong>格式校验</strong> - 自动检测7z文件有效性</li>
 *     <li><strong>大文件支持</strong> - 采用流式处理降低内存消耗</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class SevenZUtils {
	protected SevenZUtils() {
	}

	/**
	 * 检查指定文件是否为7z压缩格式
	 *
	 * @param file 待检查的文件对象，不可为null且需实际存在
	 * @return 当文件存在且检测为7z格式时返回true，否则false
	 * @throws IOException 当文件访问发生I/O异常时抛出
	 * @since 1.0.0
	 */
	public static boolean is7z(final File file) throws IOException {
		return FileUtils.isMimeType(file, CompressConstants.SEVEN_Z_MIME_TYPE);
	}

	/**
	 * 检查字节数组内容是否为7z压缩格式
	 *
	 * @param bytes 待检查的字节数组，不可为空数组或null
	 * @return 当数组非空且内容检测为7z格式时返回true，否则false
	 * @since 1.0.0
	 */
	public static boolean is7z(final byte[] bytes) {
		return ArrayUtils.isNotEmpty(bytes) &&
			IOConstants.getDefaultTika().detect(bytes).equals(CompressConstants.SEVEN_Z_MIME_TYPE);
	}

	/**
	 * 检查输入流内容是否为7z压缩格式
	 *
	 * @param inputStream 待检查的输入流对象，不可为null
	 * @return 当输入流非空且内容检测为7z格式时返回true，否则false
	 * @throws IOException 当流读取发生I/O异常时抛出
	 * @since 1.0.0
	 */
	public static boolean is7z(final InputStream inputStream) throws IOException {
		return Objects.nonNull(inputStream) &&
			IOConstants.getDefaultTika().detect(inputStream).equals(CompressConstants.SEVEN_Z_MIME_TYPE);
	}

	/**
	 * 解压缩7z文件到指定目录
	 *
	 * @param inputFile 要解压的7z文件，不可为null
	 * @param outputDir 解压输出目录，会自动创建不存在的目录
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输入文件不是有效的7z格式</li>
	 *                         <li>输出路径不是目录</li>
	 *                         <li>解压过程中发生I/O错误</li>
	 *                     </ul>
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
	 * @param sevenZFile 已打开的SevenZFile对象，不可为null
	 * @param outputDir  解压输出目录，会自动创建不存在的目录
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输出路径不是目录</li>
	 *                         <li>解压过程中发生I/O错误</li>
	 *                     </ul>
	 * @since 1.0.0
	 */
	public static void unCompress(final SevenZFile sevenZFile, final File outputDir) throws IOException {
		Validate.notNull(sevenZFile, "sevenZFile 不可为 null");
		Validate.notNull(outputDir, "outputDir 不可为 null");
		if (outputDir.exists() && !outputDir.isDirectory()) {
			throw new IllegalArgumentException(outputDir.getAbsolutePath() + " 不是一个目录路径");
		} else {
			FileUtils.forceMkdir(outputDir);
		}

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
	 * @param inputFile  要压缩的文件或目录（必须存在）
	 * @param outputFile 输出7z文件（自动覆盖已存在文件）
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输入文件不存在</li>
	 *                         <li>输出路径指向目录</li>
	 *                         <li>磁盘空间不足</li>
	 *                     </ul>
	 * @since 1.0.0
	 */
	public static void compress(final File inputFile, final File outputFile) throws IOException {
		Validate.notNull(outputFile, "outputFile 不可为 null");

		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile)) {
			compress(inputFile, sevenZOutputFile);
		}
	}

	/**
	 * 压缩文件/目录到SevenZOutputFile对象
	 *
	 * @param inputFile        要压缩的文件或目录，不可为null且必须存在
	 * @param sevenZOutputFile 已打开的SevenZOutputFile对象，不可为null
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输入文件不存在</li>
	 *                         <li>压缩过程中发生I/O错误</li>
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
	 * 压缩多个文件/目录到7z文件
	 *
	 * @param inputFiles 要压缩的文件/目录集合，可为null或空集合
	 * @param outputFile 输出7z文件，会自动覆盖已存在文件
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输出路径不是文件</li>
	 *                         <li>压缩过程中发生I/O错误</li>
	 *                     </ul>
	 * @since 1.0.0
	 */
	public static void compress(final Collection<File> inputFiles, final File outputFile) throws IOException {
		Validate.notNull(outputFile, "outputFile 不可为 null");
		if (outputFile.exists() && !outputFile.isFile()) {
			throw new IllegalArgumentException(outputFile.getAbsolutePath() + " 不是一个文件路径");
		}

		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile)) {
			compress(inputFiles, sevenZOutputFile);
		}
	}

	/**
	 * 压缩多个文件/目录到SevenZOutputFile对象
	 *
	 * @param inputFiles       要压缩的文件/目录集合，可为null或空集合
	 * @param sevenZOutputFile 已打开的SevenZOutputFile对象，不可为null
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>压缩过程中发生I/O错误</li>
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
	 * 递归添加目录到7z流
	 *
	 * @param inputFile  要添加的目录
	 * @param outputFile 7z输出流
	 * @param parent     父目录路径（用于构建相对路径）
	 * @throws IOException 当目录遍历失败或流写入失败时抛出
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
	 * 添加单个文件到7z流
	 *
	 * @param inputFile  要添加的文件
	 * @param outputFile 7z输出流
	 * @param parent     父目录路径（用于构建相对路径）
	 * @throws IOException 当文件读取失败或流写入失败时抛出
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