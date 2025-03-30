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
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.input.UnsynchronizedBufferedInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

/**
 * ZIP压缩解压工具类
 * <p>提供基于Apache Commons Compress的增强功能，主要特性：</p>
 *
 * <h3>核心功能：</h3>
 * <ul>
 *     <li><strong>多输入源支持</strong> - 支持文件、输入流、ZipFile对象等多种压缩源</li>
 *     <li><strong>递归压缩</strong> - 自动处理目录结构的递归压缩</li>
 *     <li><strong>类型安全校验</strong> - 自动检测ZIP文件格式有效性</li>
 *     <li><strong>大文件优化</strong> - 使用缓冲通道流提升大文件处理性能</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class ZipUtils {
	protected ZipUtils() {
	}

	/**
	 * 检查指定文件是否为ZIP格式
	 *
	 * @param file 待检测的File对象，需确保文件存在且可读
	 * @return boolean 当且仅当文件存在且检测为ZIP格式时返回true
	 * @throws IOException 当文件访问异常时抛出
	 * @since 1.0.0
	 */
	public static boolean isZip(final File file) throws IOException {
		return FileUtils.exist(file, true) &&
			IOConstants.getDefaultTika().detect(file).equals(CompressConstants.ZIP_MIME_TYPE);
	}

	/**
	 * 检查字节数组是否为ZIP格式数据
	 *
	 * @param bytes 待检测的字节数组，非空数组才会进行格式检测
	 * @return boolean 当且仅当数组非空且检测为ZIP格式时返回true
	 * @since 1.0.0
	 */
	public static boolean isZip(final byte[] bytes) {
		return ArrayUtils.isNotEmpty(bytes) &&
			IOConstants.getDefaultTika().detect(bytes).equals(CompressConstants.ZIP_MIME_TYPE);
	}

	/**
	 * 检查输入流是否为ZIP格式数据
	 *
	 * @param inputStream 待检测的输入流，需确保流可读取且未关闭
	 * @return boolean 当且仅当流非空且检测为ZIP格式时返回true
	 * @throws IOException 当流读取异常时抛出
	 * @since 1.0.0
	 */
	public static boolean isZip(final InputStream inputStream) throws IOException {
		return Objects.nonNull(inputStream) &&
			IOConstants.getDefaultTika().detect(inputStream).equals(CompressConstants.ZIP_MIME_TYPE);
	}

	/**
	 * 解压ZIP文件到指定目录
	 *
	 * @param inputFile ZIP文件（必须存在且为有效ZIP文件）
	 * @param outputDir 输出目录（自动创建）
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输入文件不是ZIP格式</li>
	 *                         <li>输出路径不是目录</li>
	 *                         <li>ZIP文件损坏</li>
	 *                     </ul>
	 * @since 1.0.0
	 */
	public static void unCompress(final File inputFile, final File outputDir) throws IOException {
		Validate.notNull(inputFile, "inputFile 不可为 null");

		String mimeType = FileUtils.getMimeType(inputFile);
		if (!CompressConstants.ZIP_MIME_TYPE.equals(mimeType)) {
			throw new IOException(inputFile.getAbsolutePath() + "不是zip类型文件");
		}
		try (ZipFile zipFile = ZipFile.builder().setFile(inputFile).get()) {
			unCompress(zipFile, outputDir);
		}
	}

	/**
	 * 从字节数组解压ZIP内容
	 *
	 * @param bytes     字节数组
	 * @param outputDir 输出目录（自动创建）
	 * @throws IOException 内容不是有效ZIP格式时抛出
	 * @since 1.0.0
	 */
	public static void unCompress(final byte[] bytes, final File outputDir) throws IOException {
		if (ArrayUtils.isNotEmpty(bytes)) {
			String mimeType = IOConstants.getDefaultTika().detect(bytes);
			if (!CompressConstants.ZIP_MIME_TYPE.equals(mimeType)) {
				throw new IOException("不是zip类型文件");
			}
			unCompress(IOUtils.toUnsynchronizedByteArrayInputStream(bytes), outputDir);
		}
	}

	/**
	 * 从输入流解压ZIP内容
	 *
	 * @param inputStream ZIP输入流
	 * @param outputDir   输出目录（自动创建）
	 * @throws IOException 流内容不是有效ZIP格式时抛出
	 * @throws NullPointerException inputStream为null时抛出
	 * @since 1.0.0
	 */
	public static void unCompress(final InputStream inputStream, final File outputDir) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		if (inputStream instanceof ZipArchiveInputStream zipArchiveInputStream) {
			unCompress(zipArchiveInputStream, outputDir);
		} else {
			try (ZipArchiveInputStream archiveInputStream = new ZipArchiveInputStream(inputStream)) {
				unCompress(archiveInputStream, outputDir);
			}
		}
	}

	/**
	 * 从ZipFile对象解压到目录
	 *
	 * @param zipFile   已打开的ZipFile实例
	 * @param outputDir 解压目标目录（自动创建）
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>zipFile为null</li>
	 *                         <li>目录条目创建失败</li>
	 *                         <li>文件写入权限不足</li>
	 *                     </ul>
	 * @since 1.0.0
	 */
	public static void unCompress(final ZipFile zipFile, final File outputDir) throws IOException {
		Validate.notNull(zipFile, "zipFile 不可为 null");
		Validate.notNull(outputDir, "outputDir 不可为 null");
		if (outputDir.exists() && !outputDir.isDirectory()) {
			throw new IOException(outputDir.getAbsolutePath() + " 不是一个目录路径");
		} else {
			FileUtils.forceMkdir(outputDir);
		}

		Iterator<ZipArchiveEntry> iterator = zipFile.getEntries().asIterator();
		while (iterator.hasNext()) {
			ZipArchiveEntry zipEntry = iterator.next();
			File file = new File(outputDir, zipEntry.getName());
			if (zipEntry.isDirectory()) {
				if (!file.exists()) {
					FileUtils.forceMkdir(file);
				}
			} else {
				try (FileOutputStream fileOutputStream = FileUtils.openOutputStream(file);
					 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
					 InputStream inputStream = zipFile.getInputStream(zipEntry)) {
					inputStream.transferTo(bufferedOutputStream);
				}
			}
		}
	}

	/**
	 * 从ZIP输入流解压到目录
	 *
	 * @param archiveInputStream ZIP归档输入流
	 * @param outputDir          解压目标目录（自动创建）
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输入流为null</li>
	 *                         <li>流内容已损坏</li>
	 *                         <li>目录结构创建失败</li>
	 *                     </ul>
	 * @since 1.0.0
	 */
	public static void unCompress(final ZipArchiveInputStream archiveInputStream, final File outputDir) throws IOException {
		Validate.notNull(archiveInputStream, "archiveInputStream 不可为 null");
		Validate.notNull(outputDir, "outputDir 不可为 null");
		if (outputDir.exists() && !outputDir.isDirectory()) {
			throw new IOException(outputDir.getAbsolutePath() + " 不是一个目录路径");
		} else {
			FileUtils.forceMkdir(outputDir);
		}

		ZipArchiveEntry zipEntry = archiveInputStream.getNextEntry();
		while (Objects.nonNull(zipEntry)) {
			File file = new File(outputDir, zipEntry.getName());
			if (zipEntry.isDirectory()) {
				if (!file.exists()) {
					FileUtils.forceMkdir(file);
				}
			} else {
				try (FileOutputStream fileOutputStream = FileUtils.openOutputStream(file);
					 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {
					archiveInputStream.transferTo(bufferedOutputStream);
				}
			}
			zipEntry = archiveInputStream.getNextEntry();
		}
	}

	/**
	 * 压缩文件/目录到ZIP文件
	 *
	 * @param inputFile  要压缩的文件或目录（必须存在）
	 * @param outputFile 输出ZIP文件（自动创建父目录）
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输入文件不存在</li>
	 *                         <li>输出路径是目录</li>
	 *                     </ul>
	 * @since 1.0.0
	 */
	public static void compress(final File inputFile, final File outputFile) throws IOException {
		Validate.notNull(outputFile, "outputFile 不可为 null");
		if (outputFile.exists() && !outputFile.isFile()) {
			throw new IOException(outputFile.getAbsolutePath() + " 不是一个文件路径");
		}

		try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputFile)) {
			compress(inputFile, zipArchiveOutputStream);
		}
	}

	/**
	 * 压缩文件/目录到输出流
	 *
	 * @param inputFile    要压缩的文件或目录
	 * @param outputStream 输出流（不自动关闭）
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输出流为null</li>
	 *                         <li>流写入失败</li>
	 *                         <li>文件读取权限不足</li>
	 *                     </ul>
	 * @since 1.0.0
	 */
	public static void compress(final File inputFile, final OutputStream outputStream) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		if (outputStream instanceof ZipArchiveOutputStream zipArchiveOutputStream) {
			compress(inputFile, zipArchiveOutputStream);
		} else {
			try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputStream)) {
				compress(inputFile, zipArchiveOutputStream);
			}
		}
	}

	/**
	 * 压缩文件/目录到ZIP输出流
	 *
	 * @param inputFile              要压缩的文件或目录
	 * @param zipArchiveOutputStream ZIP输出流（必须已打开）
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输入文件不存在</li>
	 *                         <li>输出流已关闭</li>
	 *                         <li>目录遍历失败</li>
	 *                     </ul>
	 * @since 1.0.0
	 */
	public static void compress(final File inputFile, final ZipArchiveOutputStream zipArchiveOutputStream) throws IOException {
		Validate.notNull(zipArchiveOutputStream, "zipArchiveOutputStream 不可为 null");
		Validate.notNull(inputFile, "inputFile 不可为 null");
		if (!inputFile.exists()) {
			throw new FileNotFoundException(inputFile.getAbsolutePath());
		}

		if (inputFile.isDirectory()) {
			addDir(inputFile, zipArchiveOutputStream, null);
		} else {
			addFile(inputFile, zipArchiveOutputStream, null);
		}
		zipArchiveOutputStream.finish();
	}

	/**
	 * 批量压缩文件
	 *
	 * @param inputFiles 要压缩的文件集合（自动过滤null和不存在的文件）
	 * @param outputFile 输出ZIP文件
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输出文件路径无效</li>
	 *                         <li>所有输入文件均无效</li>
	 *                     </ul>
	 * @since 1.0.0
	 */
	public static void compress(final Collection<File> inputFiles, final File outputFile) throws IOException {
		Validate.notNull(outputFile, "outputFile 不可为 null");
		if (outputFile.exists() && !outputFile.isFile()) {
			throw new IOException(outputFile.getAbsolutePath() + " 不是一个文件路径");
		}

		try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputFile)) {
			compress(inputFiles, zipArchiveOutputStream);
		}
	}

	/**
	 * 批量压缩文件到输出流
	 *
	 * @param inputFiles   要压缩的文件集合（自动过滤null和不存在的文件）
	 * @param outputStream 输出流（不自动关闭）
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输出流为null</li>
	 *                         <li>流写入失败</li>
	 *                         <li>输入文件冲突</li>
	 *                     </ul>
	 * @since 1.0.0
	 */
	public static void compress(final Collection<File> inputFiles, final OutputStream outputStream) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		if (outputStream instanceof ZipArchiveOutputStream zipArchiveOutputStream) {
			compress(inputFiles, zipArchiveOutputStream);
		} else {
			try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputStream)) {
				compress(inputFiles, zipArchiveOutputStream);
			}
		}
	}

	/**
	 * 批量压缩文件到ZIP输出流
	 *
	 * @param inputFiles             要压缩的文件集合（自动过滤null和不存在的文件）
	 * @param zipArchiveOutputStream ZIP输出流（必须已打开）
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输出流为null</li>
	 *                         <li>文件添加失败</li>
	 *                         <li>流已关闭</li>
	 *                     </ul>
	 * @since 1.0.0
	 */
	public static void compress(Collection<File> inputFiles, final ZipArchiveOutputStream zipArchiveOutputStream) throws IOException {
		Validate.notNull(zipArchiveOutputStream, "zipArchiveOutputStream 不可为 null");

		inputFiles = Objects.isNull(inputFiles) ? Collections.emptyList() : inputFiles;
		for (File file : inputFiles) {
			if (FileUtils.exist(file)) {
				if (file.isDirectory()) {
					addDir(file, zipArchiveOutputStream, null);
				} else {
					addFile(file, zipArchiveOutputStream, null);
				}
			}
		}
		zipArchiveOutputStream.finish();
	}

	/**
	 * 递归添加目录到ZIP流
	 *
	 * @param inputDir               要添加的目录
	 * @param zipArchiveOutputStream ZIP输出流
	 * @param parent                 父目录路径（用于构建相对路径）
	 * @throws IOException 当目录读取失败时抛出
	 * @since 1.0.0
	 */
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
	 * 添加单个文件到ZIP流（内部方法）
	 *
	 * @param inputFile              要添加的文件
	 * @param zipArchiveOutputStream ZIP输出流
	 * @param parent                 父目录路径（用于构建相对路径）
	 * @throws IOException 当文件读取失败时抛出
	 * @since 1.0.0
	 */
	protected static void addFile(File inputFile, ZipArchiveOutputStream zipArchiveOutputStream, String parent) throws IOException {
		try (UnsynchronizedBufferedInputStream fileChannelInputStream = FileUtils.openUnsynchronizedBufferedInputStream(inputFile)) {
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
			fileChannelInputStream.transferTo(zipArchiveOutputStream);
			zipArchiveOutputStream.closeArchiveEntry();
		}
	}
}