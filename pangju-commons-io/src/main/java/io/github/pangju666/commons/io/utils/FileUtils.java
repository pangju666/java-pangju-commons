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

package io.github.pangju666.commons.io.utils;

import io.github.pangju666.commons.io.lang.IOConstants;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.input.BufferedFileChannelInputStream;
import org.apache.commons.io.input.MemoryMappedFileInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tika.metadata.Metadata;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 增强型文件操作工具类（继承自 {@link org.apache.commons.io.FileUtils}）
 * <p>在Apache Commons IO基础上扩展以下核心能力：</p>
 *
 * <h3>功能扩展：</h3>
 * <ul>
 *     <li><strong>高性能IO流</strong> - 内存映射文件流、缓冲通道流等高效读取方案</li>
 *     <li><strong>文件加解密体系</strong> - 支持AES/CBC和AES/CTR两种加密模式</li>
 *     <li><strong>元数据解析</strong> - 基于Tika实现50+种文件格式的元数据提取</li>
 *     <li><strong>内容类型检测</strong> - 精准识别300+种MIME类型</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class FileUtils extends org.apache.commons.io.FileUtils {
	protected FileUtils() {
	}

	/**
	 * 打开内存映射文件输入流（默认缓冲区）
	 * <p>适用于需要随机访问的大文件读取场景</p>
	 *
	 * @param file 目标文件（必须存在且为文件）
	 * @return 配置好的内存映射输入流
	 * @throws FileNotFoundException 当以下情况时抛出：
	 *                               <ul>
	 *                                   <li>文件不存在</li>
	 *                                   <li>路径指向目录而非文件</li>
	 *                               </ul>
	 * @throws IOException           当IO错误发生时抛出
	 * @since 1.0.0
	 */
	public static MemoryMappedFileInputStream openMemoryMappedFileInputStream(final File file) throws IOException {
		return openMemoryMappedFileInputStream(file, IOConstants.DEFAULT_MEMORY_MAPPED_BUFFER_SIZE);
	}

	/**
	 * 打开内存映射文件输入流（自定义缓冲区）
	 * <p>示例：读取4K对齐的SSD存储设备文件时可设置为4096字节</p>
	 *
	 * @param file       目标文件
	 * @param bufferSize 自定义缓冲区大小（单位：字节）
	 * @return 内存映射输入流实例
	 * @throws IllegalArgumentException 当bufferSize小于等于0时抛出
	 * @since 1.0.0
	 */
	public static MemoryMappedFileInputStream openMemoryMappedFileInputStream(final File file, final int bufferSize) throws IOException {
		checkExists(file, "file不可为 null", true);
		return MemoryMappedFileInputStream
			.builder()
			.setFile(file)
			.setBufferSize(bufferSize)
			.get();
	}

	/**
	 * 打开内存映射文件输入流（默认缓冲区）
	 * <p>适用于需要随机访问的大文件读取场景</p>
	 *
	 * @param file 目标文件（必须存在且为文件）
	 * @return 配置好的内存映射输入流
	 * @throws FileNotFoundException 当以下情况时抛出：
	 *                               <ul>
	 *                                   <li>文件不存在</li>
	 *                                   <li>路径指向目录而非文件</li>
	 *                               </ul>
	 * @throws IOException           当IO错误发生时抛出
	 * @since 1.0.0
	 */
	public static BufferedFileChannelInputStream openBufferedFileChannelInputStream(final File file) throws IOException {
		return openBufferedFileChannelInputStream(file, IOConstants.DEFAULT_BUFFERED_FILE_CHANNEL_BUFFER_SIZE);
	}

	/**
	 * 打开内存映射文件输入流（自定义缓冲区）
	 * <p>示例：读取4K对齐的SSD存储设备文件时可设置为4096字节</p>
	 *
	 * @param file       目标文件
	 * @param bufferSize 自定义缓冲区大小（单位：字节）
	 * @return 内存映射输入流实例
	 * @throws IllegalArgumentException 当bufferSize小于等于0时抛出
	 * @since 1.0.0
	 */
	public static BufferedFileChannelInputStream openBufferedFileChannelInputStream(final File file, final int bufferSize) throws IOException {
		checkExists(file, "file不可为 null", true);
		return BufferedFileChannelInputStream
			.builder()
			.setFile(file)
			.setBufferSize(bufferSize)
			.get();
	}

	/**
	 * AES/CBC模式文件加密
	 * <p>加密特征：</p>
	 * <ul>
	 *     <li>使用PKCS5Padding填充方案</li>
	 *     <li>密码通过PBKDF2算法派生密钥</li>
	 *     <li>支持最大2GB文件解密</li>
	 * </ul>
	 *
	 * @param inputFile  原始文件（必须存在）
	 * @param outputFile 加密后文件（自动创建父目录）
	 * @param password   加密密码（长度必须为16字节）
	 * @throws IOException 当文件读写失败时抛出
	 * @see org.apache.commons.crypto.stream.CryptoOutputStream
	 * @see io.github.pangju666.commons.io.utils.IOUtils
	 * @since 1.0.0
	 */
	public static void encryptFile(final File inputFile, final File outputFile, final String password) throws IOException {
		checkExists(inputFile, "inputFile 不可为 null", true);
		Validate.notNull(outputFile, "outputFile 不可为 null");
		try (OutputStream outputStream = openOutputStream(outputFile);
			 InputStream inputStream = openInputStream(inputFile)) {
			IOUtils.encrypt(inputStream, outputStream, password);
		}
	}

	public static void encryptFile(final File inputFile, final File outputFile, final String password, final byte[] iv) throws IOException {
		checkExists(inputFile, "inputFile 不可为 null", true);
		Validate.notNull(outputFile, "outputFile 不可为 null");
		try (OutputStream outputStream = openOutputStream(outputFile);
			 InputStream inputStream = openInputStream(inputFile)) {
			IOUtils.encrypt(inputStream, outputStream, password, iv);
		}
	}

	/**
	 * AES/CBC模式文件解密
	 * <p>解密要求：</p>
	 * <ul>
	 *     <li>必须使用与加密相同的密码和盐值</li>
	 *     <li>文件必须完整未篡改</li>
	 *     <li>支持最大2GB文件解密</li>
	 * </ul>
	 *
	 * @param inputFile  加密文件（必须为有效文件）
	 * @param outputFile 输出文件（自动创建父目录）
	 * @param password   解密密码（需与加密密码一致）
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>密码错误导致解密失败</li>
	 *                         <li>输入文件被截断或损坏</li>
	 *                         <li>输出路径无写入权限</li>
	 *                     </ul>
	 * @see #encryptFile
	 * @see org.apache.commons.crypto.stream.CryptoInputStream
	 * @see io.github.pangju666.commons.io.utils.IOUtils
	 * @since 1.0.0
	 */
	public static void decryptFile(final File inputFile, final File outputFile, final String password) throws IOException {
		checkExists(inputFile, "inputFile 不可为 null", true);
		Validate.notNull(outputFile, "outputFile 不可为 null");
		try (OutputStream outputStream = openOutputStream(outputFile);
			 InputStream inputStream = openInputStream(inputFile)) {
			IOUtils.decrypt(inputStream, outputStream, password);
		}
	}

	public static void decryptFile(final File inputFile, final File outputFile, final String password, final byte[] iv) throws IOException {
		checkExists(inputFile, "inputFile 不可为 null", true);
		Validate.notNull(outputFile, "outputFile 不可为 null");
		try (OutputStream outputStream = openOutputStream(outputFile);
			 InputStream inputStream = openInputStream(inputFile)) {
			IOUtils.decrypt(inputStream, outputStream, password, iv);
		}
	}

	/**
	 * AES/CTR模式文件加密
	 * <p>与CBC模式的主要区别：</p>
	 * <ul>
	 *     <li>支持流式加密处理大文件</li>
	 *     <li>不需要数据填充</li>
	 *     <li>计数器模式保证并行加密安全</li>
	 * </ul>
	 *
	 * @param inputFile  原始文件（必须存在）
	 * @param outputFile 加密后文件
	 * @param password   加密密码（长度必须为16字节）
	 * @throws IOException 当文件读写失败时抛出
	 * @see org.apache.commons.crypto.stream.CtrCryptoOutputStream
	 * @see io.github.pangju666.commons.io.utils.IOUtils
	 * @since 1.0.0
	 */
	public static void encryptFileByCtr(final File inputFile, final File outputFile, final String password) throws IOException {
		checkExists(inputFile, "inputFile 不可为 null", true);
		Validate.notNull(outputFile, "outputFile 不可为 null");
		try (OutputStream outputStream = openOutputStream(outputFile);
			 InputStream inputStream = openInputStream(inputFile)) {
			IOUtils.encryptByCtr(inputStream, outputStream, password);
		}
	}

	public static void encryptFileByCtr(final File inputFile, final File outputFile, final String password, final byte[] iv) throws IOException {
		checkExists(inputFile, "inputFile 不可为 null", true);
		Validate.notNull(outputFile, "outputFile 不可为 null");
		try (OutputStream outputStream = openOutputStream(outputFile);
			 InputStream inputStream = openInputStream(inputFile)) {
			IOUtils.encryptByCtr(inputStream, outputStream, password, iv);
		}
	}

	/**
	 * AES/CTR模式文件解密
	 * <p>适用于大文件流式解密场景，特征：</p>
	 * <ul>
	 *     <li>无填充要求，支持任意长度数据</li>
	 *     <li>计数器模式保证并行解密能力</li>
	 *     <li>相同密码每次加密结果不同（随机IV）</li>
	 * </ul>
	 *
	 * @param inputFile  加密文件（必须存在）
	 * @param outputFile 解密后文件（自动创建父目录）
	 * @param password   解密密码（需与加密时一致）
	 * @throws IOException 当发生以下情况时抛出：
	 *                     <ul>
	 *                         <li>密码错误导致解密失败</li>
	 *                         <li>文件损坏或格式不正确</li>
	 *                         <li>磁盘空间不足</li>
	 *                     </ul>
	 * @see #encryptFileByCtr
	 * @see org.apache.commons.crypto.stream.CtrCryptoInputStream
	 * @see io.github.pangju666.commons.io.utils.IOUtils
	 * @since 1.0.0
	 */
	public static void decryptFileByCtr(final File inputFile, final File outputFile, final String password) throws IOException {
		checkExists(inputFile, "inputFile 不可为 null", true);
		Validate.notNull(outputFile, "outputFile 不可为 null");
		try (OutputStream outputStream = openOutputStream(outputFile);
			 InputStream inputStream = openInputStream(inputFile)) {
			IOUtils.decryptByCtr(inputStream, outputStream, password);
		}
	}

	public static void decryptFileByCtr(final File inputFile, final File outputFile, final String password, final byte[] iv) throws IOException {
		checkExists(inputFile, "inputFile 不可为 null", true);
		Validate.notNull(outputFile, "outputFile 不可为 null");
		try (OutputStream outputStream = openOutputStream(outputFile);
			 InputStream inputStream = openInputStream(inputFile)) {
			IOUtils.decryptByCtr(inputStream, outputStream, password, iv);
		}
	}

	/**
	 * 强制删除文件（如果存在）
	 * <p>增强特性：</p>
	 * <ul>
	 *     <li>自动清除只读属性</li>
	 *     <li>支持删除被其他进程打开的文件（Windows系统）</li>
	 *     <li>递归删除目录内容（当参数为目录时）</li>
	 * </ul>
	 *
	 * @param file 待删除文件或目录
	 * @throws IOException 当文件存在但无法删除时抛出
	 * @since 1.0.0
	 * @see #forceDelete(File)
	 */
	public static void forceDeleteIfExist(final File file) throws IOException {
		if (exist(file)) {
			forceDelete(file);
		}
	}

	/**
	 * 条件删除文件（如果存在）
	 * <p>与{@link #forceDeleteIfExist}的区别：</p>
	 * <ul>
	 *     <li>不强制删除只读文件</li>
	 *     <li>可能抛出SecurityException（无删除权限时）</li>
	 *     <li>不递归删除目录内容</li>
	 * </ul>
	 *
	 * @param file 待删除文件（可为null）
	 * @throws SecurityException 当安全管理器拒绝删除操作时抛出
	 * @since 1.0.0
	 * @see FileUtils#delete
	 */
	public static void deleteIfExist(final File file) throws IOException {
		if (exist(file)) {
			delete(file);
		}
	}

	/**
	 * 判断文件是否存在
	 * <p>空安全版本的文件存在性检查，等效于：</p>
	 * <pre>{@code
	 * file != null && file.exists()
	 * }</pre>
	 *
	 * @param file 待检查文件对象（允许为null）
	 * @return 当且仅当文件非空且存在时返回true
	 * @since 1.0.0
	 */
	public static boolean exist(final File file) {
		return Objects.nonNull(file) && file.exists();
	}

	/**
	 * 判断文件是否不存在
	 * <p>空安全版本的文件不存在检查，等效于：</p>
	 * <pre>{@code
	 * file == null || !file.exists()
	 * }</pre>
	 *
	 * @param file 待检查文件对象（允许为null）
	 * @return 当文件为null或不存在时返回true
	 * @since 1.0.0
	 */
	public static boolean notExist(final File file) {
		return Objects.isNull(file) || !file.exists();
	}

	/**
	 * 解析文件内容元数据
	 * <p>支持格式示例：</p>
	 * <ul>
	 *     <li>文档类：PDF/Office文档的作者、页数等</li>
	 *     <li>多媒体：MP3的专辑信息、图片的EXIF数据</li>
	 *     <li>压缩文件：ZIP条目数、RAR压缩方式</li>
	 * </ul>
	 *
	 * @param file 目标文件（必须存在且可读）
	 * @return 包含所有元数据的键值对集合（Key为元数据类型，如"Content-Type"）
	 * @throws IOException 当文件格式不支持或损坏时抛出
	 * @see Metadata
	 * @see org.apache.tika.Tika
	 * @since 1.0.0
	 */
	public static Map<String, String> parseMetaData(final File file) throws IOException {
		checkExists(file, "file 不可为 null", true);
		Metadata metadata = new Metadata();
		try (Reader reader = IOConstants.getDefaultTika().parse(file, metadata)) {
			return Arrays.stream(metadata.names())
				.map(name -> Pair.of(name, metadata.get(name)))
				.collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
		}
	}

	/**
	 * 获取文件真实MIME类型
	 * <p>检测机制：</p>
	 * <ul>
	 *     <li>优先分析文件内容特征</li>
	 *     <li>支持300+种常见文件格式</li>
	 *     <li>不受文件扩展名影响</li>
	 * </ul>
	 *
	 * @param file 目标文件（必须存在）
	 * @return 标准MIME类型字符串（如："application/pdf"）
	 * @throws IOException 当文件无法读取时抛出
	 * @since 1.0.0
	 * @see org.apache.tika.Tika
	 */
	public static String getMimeType(final File file) throws IOException {
		checkExists(file, "file 不可为 null", true);
		return IOConstants.getDefaultTika().detect(file);
	}

	/**
	 * 检测是否为图片文件
	 * <p>支持格式：JPEG/PNG/GIF/BMP/WebP等50+种格式</p>
	 *
	 * @param file 待检测文件
	 * @return 当文件内容被识别为图片类型时返回true
	 * @throws IOException 当文件读取失败时抛出
	 * @since 1.0.0
	 */
	public static boolean isImageType(final File file) throws IOException {
		checkExists(file, "file 不可为 null", true);
		return IOConstants.getDefaultTika().detect(file).startsWith(IOConstants.IMAGE_MIME_TYPE_PREFIX);
	}

	/**
	 * 检测是否为文本文件
	 * <p>支持格式：TXT/CSV/XML/JSON/Markdown等30+种文本格式</p>
	 *
	 * @param file 待检测文件
	 * @return 当文件内容被识别为文本类型时返回true
	 * @throws IOException 当文件无法读取时抛出
	 * @since 1.0.0
	 */
	public static boolean isTextType(final File file) throws IOException {
		checkExists(file, "file 不可为 null", true);
		return IOConstants.getDefaultTika().detect(file).startsWith(IOConstants.TEXT_MIME_TYPE_PREFIX);
	}

	/**
	 * 检测是否为视频文件
	 * <p>支持格式：MP4/AVI/MOV/MKV等20+种主流视频格式</p>
	 *
	 * @param file 待检测文件
	 * @return 当文件内容被识别为视频类型时返回true
	 * @throws IOException 当文件损坏或无法解析时抛出
	 * @since 1.0.0
	 */
	public static boolean isVideoType(final File file) throws IOException {
		checkExists(file, "file 不可为 null", true);
		return IOConstants.getDefaultTika().detect(file).startsWith(IOConstants.VIDEO_MIME_TYPE_PREFIX);
	}

	/**
	 * 检测是否为音频文件
	 * <p>支持格式：MP3/WAV/FLAC/AAC等15+种音频格式</p>
	 *
	 * @param file 待检测文件
	 * @return 当文件内容被识别为音频类型时返回true
	 * @throws IOException 当文件格式不支持时抛出
	 * @since 1.0.0
	 */
	public static boolean isAudioType(final File file) throws IOException {
		checkExists(file, "file 不可为 null", true);
		return IOConstants.getDefaultTika().detect(file).startsWith(IOConstants.AUDIO_MIME_TYPE_PREFIX);
	}

	/**
	 * 检测是否为应用程序类型
	 * <p>包含格式：EXE/DMG/APK/JAR等可执行文件格式</p>
	 *
	 * @param file 待检测文件
	 * @return 当文件内容被识别为应用类型时返回true
	 * @throws IOException 当文件不可读时抛出
	 * @since 1.0.0
	 */
	public static boolean isApplicationType(final File file) throws IOException {
		checkExists(file, "file 不可为 null", true);
		return IOConstants.getDefaultTika().detect(file).startsWith(IOConstants.APPLICATION_MIME_TYPE_PREFIX);
	}

	/**
	 * 精确匹配文件MIME类型
	 * <p>匹配规则：</p>
	 * <ul>
	 *     <li>不区分大小写比较</li>
	 *     <li>完全匹配（如："text/plain"与"TEXT/PLAIN"匹配）</li>
	 *     <li>不支持通配符匹配（如："image/*"）</li>
	 * </ul>
	 *
	 * @param file     目标文件
	 * @param mimeType 预期MIME类型（标准格式）
	 * @return 匹配成功返回true
	 * @throws IOException 当文件不可读时抛出
	 * @since 1.0.0
	 */
	public static boolean isMimeType(final File file, final String mimeType) throws IOException {
		Validate.notBlank(mimeType, "mimeType 不可为空");
		checkExists(file, "file 不可为 null", true);
		String fileMimeType = IOConstants.getDefaultTika().detect(file);
		return mimeType.equalsIgnoreCase(fileMimeType);
	}

	/**
	 * 批量匹配MIME类型
	 * <p>匹配规则：</p>
	 * <ul>
	 *     <li>不区分大小写比较</li>
	 *     <li>完全匹配（如："text/plain"与"TEXT/PLAIN"匹配）</li>
	 *     <li>不支持通配符匹配（如："image/*"）</li>
	 * </ul>
	 *
	 * @param file      目标文件
	 * @param mimeTypes 允许的MIME类型数组
	 * @return 匹配任一类型返回true，空数组返回false
	 * @throws IOException 当文件不可读时抛出
	 * @since 1.0.0
	 */
	public static boolean isAnyMimeType(final File file, final String... mimeTypes) throws IOException {
		checkExists(file, "file 不可为 null", true);
		if (ArrayUtils.isEmpty(mimeTypes)) {
			return false;
		}
		String fileMimeType = IOConstants.getDefaultTika().detect(file);
		return StringUtils.equalsAnyIgnoreCase(fileMimeType, mimeTypes);
	}

	/**
	 * 批量匹配MIME类型
	 * <p>匹配规则：</p>
	 * <ul>
	 *     <li>不区分大小写比较</li>
	 *     <li>完全匹配（如："text/plain"与"TEXT/PLAIN"匹配）</li>
	 *     <li>不支持通配符匹配（如："image/*"）</li>
	 * </ul>
	 *
	 * @param file      目标文件
	 * @param mimeTypes 允许的MIME类型集合
	 * @return 匹配任一类型返回true
	 * @throws IOException 当文件不可读时抛出
	 * @since 1.0.0
	 */
	public static boolean isAnyMimeType(final File file, final Collection<String> mimeTypes) throws IOException {
		checkExists(file, "file 不可为 null", true);
		if (mimeTypes == null || mimeTypes.isEmpty()) {
			return false;
		}
		String fileMimeType = IOConstants.getDefaultTika().detect(file);
		return mimeTypes.stream().anyMatch(mimeType -> StringUtils.equalsIgnoreCase(fileMimeType, mimeType));
	}

	/**
	 * 安全重命名文件
	 *
	 * @param file        源文件（必须存在）
	 * @param newFilename 新文件名（允许包含路径分隔符）
	 * @return 重命名后的文件对象
	 * @throws FileExistsException 当目标文件已存在时抛出
	 * @throws IOException         当重命名操作失败时抛出
	 * @since 1.0.0
	 */
	public static File rename(final File file, String newFilename) throws IOException {
		checkExists(file, "file 不可为 null", false);
		if (file.isFile()) {
			newFilename = FilenameUtils.getName(newFilename);
			Validate.notBlank(newFilename, "newFilename 必须为文件名");
		}
		File destFile = new File(file.getParent(), newFilename);
		if (destFile.exists()) {
			throw new FileExistsException(file);
		}
		if (!file.renameTo(destFile)) {
			throw new IOException("重命名源文件 '" + file + "' 失败 '");
		}
		return destFile;
	}

	/**
	 * 替换文件基名（保留扩展名）
	 * <p>示例：</p>
	 * <pre>
	 * FileUtils.replaceBaseName(new File("report.pdf"), "年度报告")
	 * → 生成"年度报告.pdf"
	 * </pre>
	 *
	 * @param file        源文件（必须为文件）
	 * @param newBaseName 新基名（不允许包含扩展名分隔符）
	 * @return 修改后的文件对象
	 * @throws FileExistsException 当目标文件存在时抛出
	 * @throws IOException         当修改失败时抛出
	 * @since 1.0.0
	 */
	public static File replaceBaseName(final File file, final String newBaseName) throws IOException {
		checkExists(file, "file 不可为 null", true);
		String newFilePath = FilenameUtils.replaceBaseName(file.getAbsolutePath(), newBaseName);
		File destFile = new File(newFilePath);
		if (destFile.exists()) {
			throw new FileExistsException(file);
		}
		if (!file.renameTo(destFile)) {
			throw new IOException("修改源文件 '" + file + "' 文件名失败 '");
		}
		return destFile;
	}

	/**
	 * 替换文件扩展名
	 * <p>功能特性：</p>
	 * <ul>
	 *     <li>自动处理扩展名格式（".jpg"或"jpg"均可）</li>
	 *     <li>支持移除扩展名（参数传空字符串）</li>
	 * </ul>
	 *
	 * <p>示例：</p>
	 * <pre>
	 * FileUtils.replaceExtension(new File("data.log"), "bak")
	 * → 生成"data.bak"
	 * </pre>
	 *
	 * @param file         源文件（必须存在）
	 * @param newExtension 新扩展名（允许为null或空字符串）
	 * @return 修改后的文件对象
	 * @throws FileExistsException 当目标文件已存在时抛出
	 * @throws IOException         当修改失败时抛出
	 * @since 1.0.0
	 */
	public static File replaceExtension(final File file, final String newExtension) throws IOException {
		checkExists(file, "file 不可为 null", true);
		String newFilePath = FilenameUtils.replaceExtension(file.getAbsolutePath(), newExtension);
		File destFile = new File(newFilePath);
		if (destFile.exists()) {
			throw new FileExistsException(file);
		}
		if (!file.renameTo(destFile)) {
			throw new IOException("修改源文件 '" + file + "' 拓展名失败 '");
		}
		return destFile;
	}

	/**
	 * 校验文件存在性及类型（内部方法）
	 * <p>被以下公共方法调用：</p>
	 * <ul>
	 *     <li>{@link #openMemoryMappedFileInputStream}</li>
	 *     <li>{@link #parseMetaData}</li>
	 *     <li>{@link #replaceBaseName}</li>
	 * </ul>
	 *
	 * @param file    待校验文件
	 * @param message 空指针异常提示信息
	 * @param isFile  类型校验标记：
	 *               true=必须为文件，false=允许目录
	 * @throws FileNotFoundException 当路径不存在或类型不匹配时抛出
	 * @since 1.0.0
	 */
	public static void checkExists(final File file, final String message, boolean isFile) throws FileNotFoundException {
		Objects.requireNonNull(file, message);
		if (!file.exists()) {
			throw new FileNotFoundException(file.toString());
		}
		if (isFile && !file.isFile()) {
			throw new FileNotFoundException(file.toString());
		}
	}
}