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
 * 文件名处理工具类（继承自Apache Commons IO FilenameUtils）
 * <p>扩展功能包括：
 * <ul>
 *     <li>基于文件扩展名的MIME类型检测</li>
 *     <li>常见文件类型快速判断（图片/文本/视频等）</li>
 *     <li>精确/批量MIME类型匹配</li>
 * </ul>
 *
 * <p>实现特性：
 * <ul>
 *     <li>使用标准MimetypesFileTypeMap进行类型检测</li>
 *     <li>统一处理文件名大小写（自动转为小写）</li>
 *     <li>空安全校验（所有方法对空文件名直接返回false）</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 * @see org.apache.commons.io.FilenameUtils
 */
public class FilenameUtils extends org.apache.commons.io.FilenameUtils {
	/**
	 * MIME类型检测器（延迟初始化）
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
	 * @param fileName 文件名（包含扩展名）
	 * @return 小写格式的MIME类型字符串（如："image/png"）
	 * @throws IllegalArgumentException 当fileName为空时抛出
	 * @since 1.0.0
	 */
	public static String getMimeType(final String fileName) {
		Validate.notBlank(fileName, "fileName 不可为空");
		return MIME_TYPE_MAP.getContentType(getName(fileName).toLowerCase());
	}

	/**
	 * 判断是否为图片类型
	 *
	 * @param fileName 待检测文件名
	 * @return 当满足以下条件时返回true：
	 * <ul>
	 *     <li>文件名非空</li>
	 *     <li>MIME类型以 {@link IOConstants#IMAGE_MIME_TYPE_PREFIX} 开头</li>
	 * </ul>
	 * @since 1.0.0
	 */
	public static boolean isImageType(final String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return false;
		}
		return MIME_TYPE_MAP.getContentType(getName(fileName).toLowerCase())
			.startsWith(IOConstants.IMAGE_MIME_TYPE_PREFIX);
	}

	/**
	 * 判断是否为文本类型
	 *
	 * @param fileName 待检测文件名
	 * @return 当满足以下条件时返回true：
	 * <ul>
	 *     <li>文件名非空</li>
	 *     <li>MIME类型以 {@link IOConstants#TEXT_MIME_TYPE_PREFIX} 开头</li>
	 * </ul>
	 * @since 1.0.0
	 */
	public static boolean isTextType(final String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return false;
		}
		return MIME_TYPE_MAP.getContentType(getName(fileName).toLowerCase())
			.startsWith(IOConstants.TEXT_MIME_TYPE_PREFIX);
	}

	/**
	 * 判断是否为视频类型
	 *
	 * @param fileName 待检测文件名
	 * @return 当满足以下条件时返回true：
	 * <ul>
	 *     <li>文件名非空</li>
	 *     <li>MIME类型以 {@link IOConstants#VIDEO_MIME_TYPE_PREFIX} 开头</li>
	 * </ul>
	 * @since 1.0.0
	 */
	public static boolean isVideoType(final String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return false;
		}
		return MIME_TYPE_MAP.getContentType(getName(fileName).toLowerCase())
			.startsWith(IOConstants.VIDEO_MIME_TYPE_PREFIX);
	}

	/**
	 * 判断是否为音频类型
	 *
	 * @param fileName 待检测文件名
	 * @return 当满足以下条件时返回true：
	 * <ul>
	 *     <li>文件名非空</li>
	 *     <li>MIME类型以 {@link IOConstants#AUDIO_MIME_TYPE_PREFIX} 开头</li>
	 * </ul>
	 * @since 1.0.0
	 */
	public static boolean isAudioType(final String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return false;
		}
		return MIME_TYPE_MAP.getContentType(getName(fileName).toLowerCase())
			.startsWith(IOConstants.AUDIO_MIME_TYPE_PREFIX);
	}

	/**
	 * 判断是否为应用类型
	 *
	 * @param fileName 待检测文件名
	 * @return 当满足以下条件时返回true：
	 * <ul>
	 *     <li>文件名非空</li>
	 *     <li>MIME类型以 {@link IOConstants#APPLICATION_MIME_TYPE_PREFIX} 开头</li>
	 * </ul>
	 * @since 1.0.0
	 */
	public static boolean isApplicationType(final String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return false;
		}
		return MIME_TYPE_MAP.getContentType(getName(fileName).toLowerCase())
			.startsWith(IOConstants.APPLICATION_MIME_TYPE_PREFIX);
	}

	/**
	 * 精确匹配MIME类型
	 *
	 * @param fileName 待检测文件名
	 * @param mimeType 目标MIME类型（不区分大小写）
	 * @return 当满足以下条件时返回true：
	 * <ul>
	 *     <li>文件名非空</li>
	 *     <li>检测到的MIME类型与目标类型完全匹配（忽略大小写）</li>
	 * </ul>
	 * @throws IllegalArgumentException 当mimeType为空时抛出
	 * @since 1.0.0
	 */
	public static boolean isMimeType(final String fileName, final String mimeType) {
		Validate.notBlank(mimeType, "mimeType 不可为空");
		if (StringUtils.isBlank(fileName)) {
			return false;
		}
		String fileMimeType = MIME_TYPE_MAP.getContentType(getName(fileName).toLowerCase());
		return mimeType.equalsIgnoreCase(fileMimeType);
	}

	/**
	 * 批量匹配MIME类型
	 *
	 * @param fileName 待检测文件名
	 * @param mimeTypes 允许的MIME类型集合（不区分大小写）
	 * @return 当满足以下条件时返回true：
	 * <ul>
	 *     <li>文件名非空</li>
	 *     <li>mimeTypes参数非空</li>
	 *     <li>检测到的MIME类型与任一指定类型匹配</li>
	 * </ul>
	 * @since 1.0.0
	 */
	public static boolean isMimeType(final String fileName, final String... mimeTypes) {
		if (StringUtils.isBlank(fileName) || ArrayUtils.isEmpty(mimeTypes)) {
			return false;
		}
		String fileMimeType = MIME_TYPE_MAP.getContentType(getName(fileName).toLowerCase());
		return StringUtils.equalsAnyIgnoreCase(fileMimeType, mimeTypes);
	}

	public static boolean isAnyMimeType(final String fileName, final Collection<String> mimeTypes) {
		if (StringUtils.isBlank(fileName) || mimeTypes == null || mimeTypes.isEmpty()) {
			return false;
		}
		String fileMimeType = MIME_TYPE_MAP.getContentType(getName(fileName).toLowerCase());
		return mimeTypes.stream().anyMatch(mimeType -> StringUtils.equalsIgnoreCase(fileMimeType, mimeType));
	}
}