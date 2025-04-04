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
 * <p>提供基于Apache Commons Compress的增强功能，实现ZIP格式的高效压缩与解压缩操作。</p>
 *
 * <h3>核心特性：</h3>
 * <ul>
 *     <li><strong>多输入源支持</strong> - 支持文件、字节数组、输入流、ZipFile对象等多种压缩源</li>
 *     <li><strong>递归压缩</strong> - 自动处理目录结构的递归压缩，保持原始文件层级关系</li>
 *     <li><strong>格式安全校验</strong> - 通过Tika检测和魔数验证确保ZIP文件格式有效性</li>
 *     <li><strong>大文件优化</strong> - 采用缓冲通道流(Buffered Stream)提升大文件处理性能</li>
 *     <li><strong>资源管理</strong> - 自动管理文件资源，防止资源泄漏</li>
 * </ul>
 *
 * <h3>线程安全说明：</h3>
 * <p>本工具类所有方法均为静态方法，可安全用于多线程环境。</p>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 压缩单个文件
 * ZipUtils.compress(new File("input.txt"), new File("output.zip"));
 *
 * // 解压缩文件
 * ZipUtils.unCompress(new File("archive.zip"), new File("outputDir"));
 * }</pre>
 *
 * @author pangju666
 * @see org.apache.commons.compress.archivers.zip.ZipFile
 * @see org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
 * @see org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
 * @since 1.0.0
 */
public class ZipUtils {
	protected ZipUtils() {
	}

	/**
	 * 检查指定文件是否为有效的ZIP格式
	 * <p>通过检测文件魔数(Magic Number)和Tika内容分析判断是否为ZIP格式</p>
	 *
	 * @param file 待检测的文件对象，必须存在且可读
	 * @return 当且仅当满足以下条件时返回true：
	 *         <ul>
	 *             <li>文件非null且存在</li>
	 *             <li>文件可读</li>
	 *             <li>内容检测为ZIP格式(魔数为"PK"开头)</li>
	 *         </ul>
	 * @throws NullPointerException 当file参数为null时抛出
	 * @throws IOException 当文件访问发生I/O异常时抛出
	 * @throws SecurityException 当没有文件读取权限时抛出
	 * @see FileUtils#isMimeType(File, String)
	 * @see CompressConstants#ZIP_MIME_TYPE
	 * @since 1.0.0
	 */
	public static boolean isZip(final File file) throws IOException {
		return FileUtils.isMimeType(file, CompressConstants.ZIP_MIME_TYPE);
	}

	/**
	 * 检查字节数组内容是否为有效的ZIP格式
	 * <p>通过检测字节数组的魔数(Magic Number)和Tika内容分析判断是否为ZIP格式</p>
	 *
	 * @param bytes 待检测的字节数组，不可为null或空数组
	 * @return 当且仅当满足以下条件时返回true：
	 *         <ul>
	 *             <li>字节数组非null且非空</li>
	 *             <li>内容检测为ZIP格式(魔数为"PK"开头)</li>
	 *         </ul>
	 * @throws IllegalArgumentException 当bytes为空数组时抛出
	 * @see IOConstants#getDefaultTika()
	 * @see CompressConstants#ZIP_MIME_TYPE
	 * @since 1.0.0
	 */
	public static boolean isZip(final byte[] bytes) {
		return ArrayUtils.isNotEmpty(bytes) &&
			IOConstants.getDefaultTika().detect(bytes).equals(CompressConstants.ZIP_MIME_TYPE);
	}

	/**
	 * 检查输入流内容是否为有效的ZIP格式
	 * <p>通过检测输入流的魔数(Magic Number)和Tika内容分析判断是否为ZIP格式</p>
	 *
	 * @param inputStream 待检测的输入流，必须支持mark/reset操作以便格式检测
	 * @return 当且仅当满足以下条件时返回true：
	 *         <ul>
	 *             <li>输入流非null</li>
	 *             <li>内容检测为ZIP格式(魔数为"PK"开头)</li>
	 *         </ul>
	 * @throws NullPointerException 当inputStream为null时抛出
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>流读取发生I/O错误</li>
	 *                         <li>流不支持mark/reset操作</li>
	 *                     </ul>
	 * @see IOConstants#getDefaultTika()
	 * @see CompressConstants#ZIP_MIME_TYPE
	 * @since 1.0.0
	 */
	public static boolean isZip(final InputStream inputStream) throws IOException {
		return Objects.nonNull(inputStream) &&
			IOConstants.getDefaultTika().detect(inputStream).equals(CompressConstants.ZIP_MIME_TYPE);
	}

	/**
	 * 解压缩ZIP文件到指定目录
	 * <p>将ZIP格式文件解压到指定目录，自动创建不存在的目录结构并保持原始文件层级关系</p>
	 *
	 * @param inputFile 要解压的ZIP文件，必须满足以下条件：
	 *                  <ul>
	 *                      <li>非null</li>
	 *                      <li>存在且可读</li>
	 *                      <li>内容为有效的ZIP格式(通过Tika检测)</li>
	 *                  </ul>
	 * @param outputDir 解压目标目录，满足以下条件：
	 *                  <ul>
	 *                      <li>非null</li>
	 *                      <li>如果已存在必须是目录</li>
	 *                      <li>自动创建不存在的父目录</li>
	 *                  </ul>
	 * @throws NullPointerException 当inputFile或outputDir为null时抛出
	 * @throws IllegalArgumentException 当出现以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>inputFile不是有效的ZIP格式文件</li>
	 *                                      <li>outputDir存在但不是目录</li>
	 *                                  </ul>
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输入文件不可读</li>
	 *                         <li>输出目录不可写</li>
	 *                         <li>解压过程中发生I/O错误</li>
	 *                         <li>磁盘空间不足</li>
	 *                     </ul>
	 * @throws SecurityException 当没有文件系统操作权限时抛出
	 * @see #isZip(File)
	 * @see #unCompress(ZipFile, File)
	 * @since 1.0.0
	 */
	public static void unCompress(final File inputFile, final File outputDir) throws IOException {
		Validate.notNull(inputFile, "inputFile 不可为 null");

		String mimeType = FileUtils.getMimeType(inputFile);
		if (!CompressConstants.ZIP_MIME_TYPE.equals(mimeType)) {
			throw new IllegalArgumentException(inputFile.getAbsolutePath() + "不是zip类型文件");
		}
		try (ZipFile zipFile = ZipFile.builder().setFile(inputFile).get()) {
			unCompress(zipFile, outputDir);
		}
	}

	/**
	 * 从字节数组解压ZIP内容
	 * <p>将ZIP格式的字节数组解压到指定目录，自动创建不存在的目录结构并保持原始文件层级关系</p>
	 *
	 * @param bytes     要解压的ZIP字节数组，必须满足以下条件：
	 *                  <ul>
	 *                      <li>非null且非空</li>
	 *                      <li>内容为有效的ZIP格式(通过Tika检测)</li>
	 *                  </ul>
	 * @param outputDir 解压目标目录，满足以下条件：
	 *                  <ul>
	 *                      <li>非null</li>
	 *                      <li>如果已存在必须是目录</li>
	 *                      <li>自动创建不存在的父目录</li>
	 *                  </ul>
	 * @throws NullPointerException 当bytes或outputDir为null时抛出
	 * @throws IllegalArgumentException 当出现以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>bytes为空数组</li>
	 *                                      <li>bytes不是有效的ZIP格式</li>
	 *                                      <li>outputDir存在但不是目录</li>
	 *                                  </ul>
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输出目录不可写</li>
	 *                         <li>解压过程中发生I/O错误</li>
	 *                         <li>磁盘空间不足</li>
	 *                     </ul>
	 * @throws SecurityException 当没有文件系统操作权限时抛出
	 * @see #isZip(byte[])
	 * @see #unCompress(InputStream, File)
	 * @since 1.0.0
	 */
	public static void unCompress(final byte[] bytes, final File outputDir) throws IOException {
		Validate.isTrue(ArrayUtils.isNotEmpty(bytes), "bytes 不可为空");

		String mimeType = IOConstants.getDefaultTika().detect(bytes);
		if (!CompressConstants.ZIP_MIME_TYPE.equals(mimeType)) {
			throw new IllegalArgumentException("不是zip类型文件");
		}
		try (ZipArchiveInputStream zipArchiveInputStream = new ZipArchiveInputStream(IOUtils.toUnsynchronizedByteArrayInputStream(bytes))) {
			unCompress(zipArchiveInputStream, outputDir);
		}
	}

	/**
	 * 从输入流解压ZIP内容
	 *
	 * @param inputStream ZIP格式输入流，必须支持mark/reset操作以便格式检测
	 * @param outputDir   解压目标目录，会自动创建不存在的目录结构
	 * @throws NullPointerException 当inputStream或outputDir为null时抛出
	 * @throws IllegalArgumentException 当出现以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>outputDir存在但不是目录</li>
	 *                                      <li>输入流内容不是有效ZIP格式</li>
	 *                                  </ul>
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输入流不可读或已关闭</li>
	 *                         <li>输出目录不可写</li>
	 *                         <li>解压过程中发生I/O错误</li>
	 *                         <li>磁盘空间不足</li>
	 *                     </ul>
	 * @throws SecurityException 当没有文件系统操作权限时抛出
	 * @see #unCompress(byte[], File)
	 * @see #unCompress(File, File)
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
	 * 从ZipFile对象解压缩到指定目录
	 *
	 * @param zipFile   已初始化的ZipFile对象，必须处于可读取状态且不为null
	 * @param outputDir 解压目标目录，会自动创建不存在的目录结构
	 * @throws NullPointerException 当zipFile或outputDir为null时抛出
	 * @throws IllegalArgumentException 当出现以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>outputDir存在但不是目录</li>
	 *                                  </ul>
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>zipFile已关闭或不可读</li>
	 *                         <li>输出目录不可写</li>
	 *                         <li>解压过程中发生I/O错误</li>
	 *                         <li>磁盘空间不足</li>
	 *                     </ul>
	 * @throws SecurityException 当没有文件系统操作权限时抛出
	 * @see #unCompress(InputStream, File)
	 * @since 1.0.0
	 */
	public static void unCompress(final ZipFile zipFile, final File outputDir) throws IOException {
		Validate.notNull(zipFile, "zipFile 不可为 null");
		FileUtils.forceMkdir(outputDir);

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
	 * 从ZipArchiveInputStream解压缩到指定目录
	 *
	 * @param archiveInputStream 已初始化的ZIP输入流，必须处于可读取状态且不为null
	 * @param outputDir          解压目标目录，会自动创建不存在的目录结构
	 * @throws NullPointerException 当archiveInputStream或outputDir为null时抛出
	 * @throws IllegalArgumentException 当出现以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>outputDir存在但不是目录</li>
	 *                                  </ul>
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输入流已关闭或不可读</li>
	 *                         <li>输出目录不可写</li>
	 *                         <li>解压过程中发生I/O错误</li>
	 *                         <li>磁盘空间不足</li>
	 *                     </ul>
	 * @throws SecurityException 当没有文件系统操作权限时抛出
	 * @see #unCompress(ZipFile, File)
	 * @since 1.0.0
	 */
	public static void unCompress(final ZipArchiveInputStream archiveInputStream, final File outputDir) throws IOException {
		Validate.notNull(archiveInputStream, "archiveInputStream 不可为 null");
		FileUtils.forceMkdir(outputDir);

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
	 * <p>将单个文件或目录(递归包含子目录)压缩为ZIP格式文件</p>
	 *
	 * @param inputFile  要压缩的文件或目录，必须存在且可读
	 * @param outputFile 输出ZIP文件路径，会自动创建父目录并覆盖已存在文件
	 * @throws NullPointerException 当inputFile或outputFile为null时抛出
	 * @throws IllegalArgumentException 当出现以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>inputFile不存在</li>
	 *                                      <li>outputFile存在但不是文件</li>
	 *                                  </ul>
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输入文件不可读</li>
	 *                         <li>输出文件不可写</li>
	 *                         <li>压缩过程中发生I/O错误</li>
	 *                         <li>磁盘空间不足</li>
	 *                     </ul>
	 * @throws SecurityException 当没有文件系统操作权限时抛出
	 * @see #compress(Collection, File)
	 * @see #compress(File, OutputStream)
	 * @since 1.0.0
	 */
	public static void compress(final File inputFile, final File outputFile) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputFile)) {
			compress(inputFile, zipArchiveOutputStream);
		}
	}

	/**
	 * 压缩文件/目录到输出流
	 * <p>将单个文件或目录(递归包含子目录)压缩为ZIP格式并写入输出流</p>
	 *
	 * @param inputFile    要压缩的文件或目录，必须存在且可读
	 * @param outputStream 输出流对象，必须可写且不为null(方法不会自动关闭此流)
	 * @throws NullPointerException 当inputFile或outputStream为null时抛出
	 * @throws IllegalArgumentException 当inputFile不存在时抛出
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输入文件不可读</li>
	 *                         <li>输出流不可写</li>
	 *                         <li>压缩过程中发生I/O错误</li>
	 *                     </ul>
	 * @throws SecurityException 当没有文件系统操作权限时抛出
	 * @see #compress(File, File)
	 * @see #compress(File, ZipArchiveOutputStream)
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
	 * 压缩文件/目录到ZipArchiveOutputStream
	 * <p>将单个文件或目录(递归包含子目录)添加到已初始化的ZIP输出流</p>
	 *
	 * @param inputFile              要压缩的文件或目录，必须存在且可读
	 * @param zipArchiveOutputStream 已初始化的ZIP输出流，必须可写且不为null
	 * @throws NullPointerException 当inputFile或zipArchiveOutputStream为null时抛出
	 * @throws IllegalArgumentException 当inputFile不存在时抛出
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输入文件不可读</li>
	 *                         <li>输出流不可写或已关闭</li>
	 *                         <li>压缩过程中发生I/O错误</li>
	 *                     </ul>
	 * @throws SecurityException 当没有文件系统操作权限时抛出
	 * @see #compress(File, OutputStream)
	 * @since 1.0.0
	 */
	public static void compress(final File inputFile, final ZipArchiveOutputStream zipArchiveOutputStream) throws IOException {
		Validate.notNull(zipArchiveOutputStream, "zipArchiveOutputStream 不可为 null");
		FileUtils.check(inputFile, "inputFile 不可为 null");

		if (inputFile.isDirectory()) {
			addDir(inputFile, zipArchiveOutputStream, null);
		} else {
			addFile(inputFile, zipArchiveOutputStream, null);
		}
		zipArchiveOutputStream.finish();
	}

	/**
	 * 批量压缩文件/目录到ZIP文件
	 * <p>将多个文件或目录(递归包含子目录)压缩为单个ZIP格式文件</p>
	 *
	 * @param inputFiles 要压缩的文件/目录集合，集合可为null或空(此时创建空ZIP文件)
	 * @param outputFile 输出ZIP文件路径，会自动创建父目录并覆盖已存在文件
	 * @throws NullPointerException 当outputFile为null时抛出
	 * @throws IllegalArgumentException 当出现以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>outputFile存在但不是文件</li>
	 *                                  </ul>
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输出文件不可写</li>
	 *                         <li>压缩过程中发生I/O错误</li>
	 *                         <li>磁盘空间不足</li>
	 *                     </ul>
	 * @throws SecurityException 当没有文件系统操作权限时抛出
	 * @see #compress(File, File)
	 * @see #compress(Collection, OutputStream)
	 * @since 1.0.0
	 */
	public static void compress(final Collection<File> inputFiles, final File outputFile) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputFile)) {
			compress(inputFiles, zipArchiveOutputStream);
		}
	}

	/**
	 * 批量压缩文件到输出流
	 * <p>将多个文件或目录(递归包含子目录)压缩为ZIP格式并写入输出流</p>
	 *
	 * @param inputFiles   要压缩的文件集合，自动过滤null和不存在的文件
	 * @param outputStream 输出流对象，必须可写且不为null(方法不会自动关闭此流)
	 * @throws NullPointerException 当outputStream为null时抛出
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输出流不可写</li>
	 *                         <li>压缩过程中发生I/O错误</li>
	 *                         <li>输入文件冲突(同名文件)</li>
	 *                     </ul>
	 * @throws SecurityException 当没有文件系统操作权限时抛出
	 * @see #compress(File, OutputStream)
	 * @see #compress(Collection, ZipArchiveOutputStream)
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
	 * <p>将多个文件或目录(递归包含子目录)添加到已初始化的ZIP输出流</p>
	 *
	 * @param inputFiles             要压缩的文件集合，自动过滤null和不存在的文件
	 * @param zipArchiveOutputStream 已初始化的ZIP输出流，必须可写且不为null
	 * @throws NullPointerException 当zipArchiveOutputStream为null时抛出
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>输出流不可写或已关闭</li>
	 *                         <li>压缩过程中发生I/O错误</li>
	 *                         <li>输入文件冲突(同名文件)</li>
	 *                     </ul>
	 * @throws SecurityException 当没有文件系统操作权限时抛出
	 * @see #compress(Collection, OutputStream)
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
	 * <p>将目录及其所有子目录和文件递归添加到ZIP输出流中，保持原始目录结构</p>
	 *
	 * @param inputDir               要添加的目录，必须存在且可读
	 * @param zipArchiveOutputStream ZIP输出流对象，必须已初始化且可写
	 * @param parent                 父目录相对路径(用于构建ZIP条目路径)，可为null
	 * @throws NullPointerException 当inputDir或zipArchiveOutputStream为null时抛出
	 * @throws IllegalArgumentException 当inputDir不存在或不是目录时抛出
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>目录不可读</li>
	 *                         <li>输出流不可写或已关闭</li>
	 *                         <li>添加过程中发生I/O错误</li>
	 *                     </ul>
	 * @throws SecurityException 当没有文件系统操作权限时抛出
	 * @see #addFile(File, ZipArchiveOutputStream, String)
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
	 * 添加单个文件到ZIP流
	 * <p>将单个文件添加到ZIP输出流中，可指定父目录路径</p>
	 *
	 * @param inputFile              要添加的文件，必须存在且可读
	 * @param zipArchiveOutputStream ZIP输出流对象，必须已初始化且可写
	 * @param parent                 父目录相对路径(用于构建ZIP条目路径)，可为null
	 * @throws NullPointerException 当inputFile或zipArchiveOutputStream为null时抛出
	 * @throws IllegalArgumentException 当inputFile不存在或不是文件时抛出
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>文件不可读</li>
	 *                         <li>输出流不可写或已关闭</li>
	 *                         <li>添加过程中发生I/O错误</li>
	 *                     </ul>
	 * @throws SecurityException 当没有文件系统操作权限时抛出
	 * @see #addDir(File, ZipArchiveOutputStream, String)
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