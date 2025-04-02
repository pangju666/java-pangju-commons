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
import jakarta.activation.MimetypesFileTypeMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.Collection;

/**
 * 文件名及路径处理工具类（继承自 {@link org.apache.commons.io.FilenameUtils}）
 * <p>在 Apache Commons IO 原生功能基础上扩展以下核心能力：</p>
 *
 * <h3>功能扩展：</h3>
 * <ul>
 *     <li><strong>MIME类型检测</strong> - 基于文件扩展名的快速类型识别</li>
 *     <li><strong>文件类型判断</strong> - 支持图片/文本/视频/音频/应用等常见类型检测</li>
 *     <li><strong>智能路径识别</strong> - 区分目录路径与文件路径的验证方法</li>
 *     <li><strong>批量匹配机制</strong> - 支持多MIME类型集合匹配校验</li>
 *     <li><strong>文件名重构工具</strong> - 提供全名替换、基名替换、扩展名替换等原子操作</li>
 * </ul>
 *
 * <h3>典型用例：</h3>
 * <pre>{@code
 * // MIME类型检测
 * String mime = FilenameUtils.getMimeType("image.png"); // => "image/png"
 *
 * // 文件类型判断
 * boolean isVideo = FilenameUtils.isVideoType("movie.mp4"); // => true
 *
 * // 路径类型识别
 * boolean isDir = FilenameUtils.isDirectoryPath("/var/log/"); // => true
 *
 * // 文件名重构
 * String newPath = FilenameUtils.replaceBaseName("data.txt", "backup"); // => "backup.txt"
 * }</pre>
 *
 * @author pangju666
 * @since 1.0.0
 * @see org.apache.commons.io.FilenameUtils
 */
public class FilenameUtils extends org.apache.commons.io.FilenameUtils {
	/**
	 * MIME类型检测器
	 * <p>用于根据文件扩展名获取对应的MIME类型</p>
	 *
	 * @since 1.0.0
	 */
	protected static final MimetypesFileTypeMap MIME_TYPE_MAP = new MimetypesFileTypeMap();

	protected FilenameUtils() {
	}

	/**
	 * 获取文件MIME类型
	 * <p>注意：检测结果基于文件扩展名而非实际内容</p>
	 *
	 * @param filename 文件名（包含扩展名）
	 * @return 小写格式的MIME类型字符串（如："image/png"），文件名为空字符串时返回null
	 * @since 1.0.0
	 */
	public static String getMimeType(final String filename) {
		if (StringUtils.isBlank(filename)) {
			return null;
		}
		return MIME_TYPE_MAP.getContentType(getName(filename).toLowerCase());
	}

	/**
	 * 判断是否为图片类型
	 *
	 * @param filename 待检测文件名
	 * @return 当满足以下条件时返回true：
	 * <ul>
	 *     <li>文件名非空</li>
	 *     <li>MIME类型以 {@link IOConstants#IMAGE_MIME_TYPE_PREFIX} 开头</li>
	 * </ul>
	 * @since 1.0.0
	 */
	public static boolean isImageType(final String filename) {
		if (StringUtils.isBlank(filename)) {
			return false;
		}
		return MIME_TYPE_MAP.getContentType(getName(filename).toLowerCase())
			.startsWith(IOConstants.IMAGE_MIME_TYPE_PREFIX);
	}

	/**
	 * 判断是否为文本类型
	 *
	 * @param filename 待检测文件名
	 * @return 当满足以下条件时返回true：
	 * <ul>
	 *     <li>文件名非空</li>
	 *     <li>MIME类型以 {@link IOConstants#TEXT_MIME_TYPE_PREFIX} 开头</li>
	 * </ul>
	 * @since 1.0.0
	 */
	public static boolean isTextType(final String filename) {
		if (StringUtils.isBlank(filename)) {
			return false;
		}
		return MIME_TYPE_MAP.getContentType(getName(filename).toLowerCase())
			.startsWith(IOConstants.TEXT_MIME_TYPE_PREFIX);
	}

	/**
	 * 判断是否为模型类型
	 *
	 * @param filename 待检测文件名
	 * @return 当满足以下条件时返回true：
	 * <ul>
	 *     <li>文件名非空</li>
	 *     <li>MIME类型以 {@link IOConstants#MODEL_MIME_TYPE_PREFIX} 开头</li>
	 * </ul>
	 * @since 1.0.0
	 */
	public static boolean isModelType(final String filename) {
		if (StringUtils.isBlank(filename)) {
			return false;
		}
		return MIME_TYPE_MAP.getContentType(getName(filename).toLowerCase())
			.startsWith(IOConstants.MODEL_MIME_TYPE_PREFIX);
	}

	/**
	 * 判断是否为视频类型
	 *
	 * @param filename 待检测文件名
	 * @return 当满足以下条件时返回true：
	 * <ul>
	 *     <li>文件名非空</li>
	 *     <li>MIME类型以 {@link IOConstants#VIDEO_MIME_TYPE_PREFIX} 开头</li>
	 * </ul>
	 * @since 1.0.0
	 */
	public static boolean isVideoType(final String filename) {
		if (StringUtils.isBlank(filename)) {
			return false;
		}
		return MIME_TYPE_MAP.getContentType(getName(filename).toLowerCase())
			.startsWith(IOConstants.VIDEO_MIME_TYPE_PREFIX);
	}

	/**
	 * 判断是否为音频类型
	 *
	 * @param filename 待检测文件名
	 * @return 当满足以下条件时返回true：
	 * <ul>
	 *     <li>文件名非空</li>
	 *     <li>MIME类型以 {@link IOConstants#AUDIO_MIME_TYPE_PREFIX} 开头</li>
	 * </ul>
	 * @since 1.0.0
	 */
	public static boolean isAudioType(final String filename) {
		if (StringUtils.isBlank(filename)) {
			return false;
		}
		return MIME_TYPE_MAP.getContentType(getName(filename).toLowerCase())
			.startsWith(IOConstants.AUDIO_MIME_TYPE_PREFIX);
	}

	/**
	 * 判断是否为应用类型
	 *
	 * @param filename 待检测文件名
	 * @return 当满足以下条件时返回true：
	 * <ul>
	 *     <li>文件名非空</li>
	 *     <li>MIME类型以 {@link IOConstants#APPLICATION_MIME_TYPE_PREFIX} 开头</li>
	 * </ul>
	 * @since 1.0.0
	 */
	public static boolean isApplicationType(final String filename) {
		if (StringUtils.isBlank(filename)) {
			return false;
		}
		return MIME_TYPE_MAP.getContentType(getName(filename).toLowerCase())
			.startsWith(IOConstants.APPLICATION_MIME_TYPE_PREFIX);
	}

	/**
	 * 精确匹配MIME类型
	 *
	 * @param filename 待检测文件名
	 * @param mimeType 目标MIME类型（不区分大小写，为空字符串则返回null）
	 * @return 当满足以下条件时返回true：
	 * <ul>
	 *     <li>文件名非空</li>
	 *     <li>检测到的MIME类型与目标类型完全匹配（忽略大小写）</li>
	 * </ul>
	 * @since 1.0.0
	 */
	public static boolean isMimeType(final String filename, final String mimeType) {
		if (StringUtils.isAnyBlank(filename, mimeType)) {
			return false;
		}
		String fileMimeType = MIME_TYPE_MAP.getContentType(getName(filename).toLowerCase());
		return mimeType.equalsIgnoreCase(fileMimeType);
	}

	/**
	 * 批量匹配MIME类型
	 *
	 * @param filename 待检测文件名
	 * @param mimeTypes 允许的MIME类型集合（不区分大小写）
	 * @return 当满足以下条件时返回true：
	 * <ul>
	 *     <li>文件名非空</li>
	 *     <li>mimeTypes参数非空</li>
	 *     <li>检测到的MIME类型与任一指定类型匹配</li>
	 * </ul>
	 * @since 1.0.0
	 */
	public static boolean isAnyMimeType(final String filename, final String... mimeTypes) {
		if (StringUtils.isBlank(filename) || ArrayUtils.isEmpty(mimeTypes)) {
			return false;
		}
		String fileMimeType = MIME_TYPE_MAP.getContentType(getName(filename).toLowerCase());
		return StringUtils.equalsAnyIgnoreCase(fileMimeType, mimeTypes);
	}

	/**
	 * 批量匹配MIME类型（集合版本）
	 *
	 * @param filename  待检测文件名
	 * @param mimeTypes 允许的MIME类型集合（不区分大小写）
	 * @return 当满足以下条件时返回true：
	 * <ul>
	 *     <li>文件名非空</li>
	 *     <li>mimeTypes集合非空</li>
	 *     <li>检测到的MIME类型与集合中任一类型匹配</li>
	 * </ul>
	 * @since 1.0.0
	 */
	public static boolean isAnyMimeType(final String filename, final Collection<String> mimeTypes) {
		if (StringUtils.isBlank(filename) || mimeTypes == null || mimeTypes.isEmpty()) {
			return false;
		}
		String fileMimeType = MIME_TYPE_MAP.getContentType(getName(filename).toLowerCase());
		return mimeTypes.stream().anyMatch(mimeType -> StringUtils.equalsIgnoreCase(fileMimeType, mimeType));
	}

	/**
	 * 完全替换文件名（包含名称和扩展名）
	 * <p>该方法将原始文件名的主体和扩展名整体替换为新名称，适用于需要同时修改文件名和扩展名的场景</p>
	 *
	 * @param filename    原始文件路径或文件名（示例："/path/to/oldfile.txt" 或 "oldfile.txt"）
	 * @param newFilename 新的完整文件名（可包含扩展名，示例："newfile" 或 "newfile.md"）
	 * @return 包含原始路径的新文件名，示例：
	 * <ul>
	 *     <li>输入 ("data.csv", "backup") → "backup"</li>
	 *     <li>输入 ("photo.jpg", "vacation.png") → "vacation.png"</li>
	 *     <li>输入 "/var/log/app.log" + "error_2023.log" → "/var/log/error_2023.log"</li>
	 * </ul>
	 * @throws IllegalArgumentException 当以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>newFilename参数为空</li>
	 *                                      <li>filename参数不包含有效文件名（如：纯目录路径）</li>
	 *                                  </ul>
	 * @since 1.0.0
	 */
	public static String rename(final String filename, final String newFilename) {
		Validate.notBlank(newFilename, "newFilename 不可为空");
		String oldName = getName(filename);
		Validate.notBlank(oldName, "filename 必须为文件名或文件路径");
		return getFullPath(filename) + newFilename;
	}

	/**
	 * 替换文件基名（保留扩展名和路径）
	 * <p>示例：
	 * <ul>
	 *     <li>输入 ("file.txt", "new") → "new.txt"</li>
	 *     <li>输入 "/path/to/old.jpg" + "photo" → "/path/to/photo.jpg"</li>
	 * </ul>
	 *
	 * @param filename    原始文件名（包含路径）
	 * @param newBaseName 新基名（不含扩展名）
	 * @return 包含路径的新文件名
	 * @throws IllegalArgumentException 当以下情况时抛出：
	 *                                  <ul>
	 *                                      <li>newBaseName为空</li>
	 *                                      <li>filename不包含有效文件名（如：纯目录路径）</li>
	 *                                  </ul>
	 * @since 1.0.0
	 */
	public static String replaceBaseName(final String filename, final String newBaseName) {
		Validate.notBlank(newBaseName, "newName 不可为空");
		String oldName = getName(filename);
		Validate.notBlank(oldName, "filename 必须为文件名或文件路径");

		String extension = getExtension(oldName);
		return getFullPath(filename) + newBaseName + (StringUtils.isNotEmpty(extension) ?
			EXTENSION_SEPARATOR + extension : StringUtils.EMPTY);
	}

	/**
	 * 替换文件扩展名
	 * <ul>
	 *     <li>支持移除扩展名（当newExtension为空时）</li>
	 *     <li>自动处理无扩展名文件</li>
	 *     <li>增强路径安全性处理</li>
	 * </ul>
	 *
	 * @param filename     原始文件名
	 * @param newExtension 新扩展名（自动处理格式，输入"pdf"、".pdf"、null 均有效）
	 * @return 处理后的完整文件名，规则如下：
	 * <ul>
	 *     <li>当filename为空时返回空字符串</li>
	 *     <li>当newExtension为空时返回无扩展名文件</li>
	 *     <li>自动添加扩展名分隔符</li>
	 * </ul>
	 * @throws IllegalArgumentException 当以下情况时抛出：
	 *                                  <ul>
	 *                                  	<li>filename不包含有效文件名（如：纯目录路径）</li>
	 *                                  </ul>
	 * @since 1.0.0
	 */
	public static String replaceExtension(final String filename, String newExtension) {
		newExtension = StringUtils.defaultIfBlank(newExtension, StringUtils.EMPTY);
		String oldName = getBaseName(filename);
		Validate.notBlank(oldName, "filename 必须为文件名或文件路径");
		if (StringUtils.isEmpty(newExtension)) {
			return removeExtension(filename);
		}
		return removeExtension(filename) + (newExtension.startsWith(EXTENSION_SEPARATOR_STR) ? newExtension :
			EXTENSION_SEPARATOR_STR + newExtension);
	}

	/**
	 * 判断路径是否为目录路径
	 * <p>当路径满足以下条件时返回 true：
	 * <ul>
	 *     <li>路径非空</li>
	 *     <li>路径结尾为文件分隔符（即不包含文件名部分）</li>
	 *     <li>通过 {@link org.apache.commons.io.FilenameUtils#getName(String)} 获取的文件名为空</li>
	 * </ul>
	 *
	 * @param directoryPath 待检测的路径字符串
	 * @return 当路径表示目录结构时返回 true（示例：
	 * <ul>
	 *     <li>输入 "C:\\logs\\" → true</li>
	 *     <li>输入 "/var/log/" → true</li>
	 *     <li>输入 "file.txt" → false</li>
	 * </ul>）
	 * @since 1.0.0
	 */
	public static boolean isDirectoryPath(final String directoryPath) {
		if (StringUtils.isBlank(directoryPath)) {
			return false;
		}
		return StringUtils.isBlank(getName(directoryPath));
	}

	/**
	 * 判断路径是否为文件路径
	 * <p>当路径满足以下条件时返回 true：
	 * <ul>
	 *     <li>路径非空</li>
	 *     <li>路径包含有效的文件名部分</li>
	 *     <li>通过 {@link org.apache.commons.io.FilenameUtils#getName(String)} 获取的文件名非空</li>
	 * </ul>
	 *
	 * @param filePath 待检测的路径字符串
	 * @return 当路径表示文件路径时返回 true（示例：
	 * <ul>
	 *     <li>输入 "document.pdf" → true</li>
	 *     <li>输入 "/tmp/data.csv" → true</li>
	 *     <li>输入 "C:\\logs\\" → false</li>
	 * </ul>
	 * @since 1.0.0
	 */
	public static boolean isFilePath(final String filePath) {
		if (StringUtils.isBlank(filePath)) {
			return false;
		}
		return StringUtils.isNotBlank(getName(filePath));
	}
}