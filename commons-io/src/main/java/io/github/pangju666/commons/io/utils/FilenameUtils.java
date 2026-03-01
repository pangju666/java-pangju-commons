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

import io.github.pangju666.commons.io.enums.FileType;
import io.github.pangju666.commons.io.lang.IOConstants;
import jakarta.activation.MimetypesFileTypeMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.Collection;

/**
 * 文件名及路径处理工具类（继承自 {@link org.apache.commons.io.FilenameUtils}）
 * <p>
 * 本工具类在Apache Commons IO原生功能基础上，扩展了以下核心能力：
 * </p>
 *
 * <h3>功能特性：</h3>
 * <ul>
 *     <li><b>MIME类型检测</b> - 基于文件扩展名的快速类型识别系统</li>
 *     <li><b>文件类型判断</b> - 支持图片/文本/视频/音频/应用等常见类型检测</li>
 *     <li><b>路径智能识别</b> - 精确区分目录路径与文件路径</li>
 *     <li><b>批量匹配机制</b> - 支持多MIME类型集合匹配校验</li>
 *     <li><b>文件名重构</b> - 提供全名替换、基名替换、扩展名替换等原子操作</li>
 * </ul>
 *
 * <h3>设计原则：</h3>
 * <ol>
 *     <li>所有方法均为静态方法，线程安全</li>
 *     <li>严格参数校验，避免NPE</li>
 *     <li>保持与Apache Commons IO一致的路径处理逻辑</li>
 *     <li>扩展功能与原生API风格统一</li>
 * </ol>
 *
 * <h3>典型用例：</h3>
 * <pre>{@code
 * // 获取MIME类型
 * String mime = FilenameUtils.getMimeType("document.pdf"); // "application/pdf"
 *
 * // 文件类型验证
 * boolean isValid = FilenameUtils.isAnyMimeType("image.jpg",
 *     "image/jpeg", "image/png"); // true
 *
 * // 文件名重构
 * String newPath = FilenameUtils.replaceExtension("data.old", "csv"); // "data.csv"
 * }</pre>
 *
 * @author pangju666
 * @since 1.0.0
 * @see org.apache.commons.io.FilenameUtils
 */
public class FilenameUtils extends org.apache.commons.io.FilenameUtils {
	/**
	 * MIME类型检测器实例
	 * <p>
	 * 使用{@link MimetypesFileTypeMap}实现，基于文件扩展名识别MIME类型。
	 * </p>
	 * <p>注意：此检测方式不验证文件实际内容。</p>
	 *
	 * @since 1.0.0
	 */
	protected static final MimetypesFileTypeMap MIME_TYPE_MAP = new MimetypesFileTypeMap();

	protected FilenameUtils() {
	}

	/**
	 * 获取文件MIME类型
	 * <p>
	 * 基于文件扩展名识别MIME类型，返回统一小写格式的结果。
	 * </p>
	 *
	 * <p><b>处理流程：</b></p>
	 * <ol>
	 *     <li>提取文件名中的扩展名部分</li>
	 *     <li>通过{@link #MIME_TYPE_MAP}查询对应MIME类型</li>
	 *     <li>返回小写格式的结果</li>
	 * </ol>
	 *
	 * @param filename 文件名（可包含路径），允许的格式：
	 *               <ul>
	 *                 <li>纯文件名（如："image.png"）</li>
	 *                 <li>完整路径（如："C:/docs/report.pdf"）</li>
	 *               </ul>
	 * @return 对应的MIME类型字符串（如："image/png"），当：
	 *         <ul>
	 *           <li>filename为空时返回null</li>
	 *           <li>无法识别的扩展名返回"application/octet-stream"</li>
	 *         </ul>
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
	 * <p>
	 * 通过检测文件扩展名对应的MIME类型前缀是否为"image/"来判断是否为图片文件。
	 * </p>
	 *
	 * <p><b>支持的图片格式：</b></p>
	 * <ul>
	 *     <li>常见格式：JPEG、PNG、GIF、WEBP</li>
	 *     <li>专业格式：TIFF、BMP、PSD</li>
	 *     <li>矢量格式：SVG</li>
	 * </ul>
	 *
	 * @param filename 待检测文件名，支持格式：
	 *               <ul>
	 *                 <li>纯文件名（如："photo.jpg"）</li>
	 *                 <li>完整路径（如："C:/images/avatar.png"）</li>
	 *               </ul>
	 * @return 检测结果，当满足以下条件时返回true：
	 *         <ul>
	 *           <li>文件名非空且有效</li>
	 *           <li>文件扩展名对应的MIME类型以"image/"开头</li>
	 *         </ul>
	 * @see #getMimeType(String)
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
	 * <p>
	 * 通过检测文件扩展名对应的MIME类型前缀是否为"text/"来判断是否为文本文件。
	 * </p>
	 *
	 * <p><b>支持的文本格式：</b></p>
	 * <ul>
	 *     <li>纯文本：TXT、CSV</li>
	 *     <li>标记语言：HTML、XML、JSON</li>
	 *     <li>代码文件：Java、Python、C++等源代码</li>
	 * </ul>
	 *
	 * @param filename 待检测文件名，支持格式：
	 *               <ul>
	 *                 <li>纯文件名（如："readme.txt"）</li>
	 *                 <li>完整路径（如："D:/docs/config.xml"）</li>
	 *               </ul>
	 * @return 检测结果，当满足以下条件时返回true：
	 *         <ul>
	 *           <li>文件名非空且有效</li>
	 *           <li>文件扩展名对应的MIME类型以"text/"开头</li>
	 *         </ul>
	 * @see #getMimeType(String)
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
	 * <p>
	 * 通过检测文件扩展名对应的MIME类型前缀是否为"model/"来判断是否为3D模型文件。
	 * </p>
	 *
	 * <p><b>支持的模型格式：</b></p>
	 * <ul>
	 *     <li>常见3D格式：STL、OBJ、FBX</li>
	 *     <li>CAD格式：STEP、IGES</li>
	 *     <li>游戏模型格式：GLTF、GLB</li>
	 * </ul>
	 *
	 * @param filename 待检测文件名，支持格式：
	 *               <ul>
	 *                 <li>纯文件名（如："model.stl"）</li>
	 *                 <li>完整路径（如："C:/models/car.obj"）</li>
	 *               </ul>
	 * @return 检测结果，当满足以下条件时返回true：
	 *         <ul>
	 *           <li>文件名非空且有效</li>
	 *           <li>文件扩展名对应的MIME类型以"model/"开头</li>
	 *         </ul>
	 * @see #getMimeType(String)
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
	 * <p>
	 * 通过检测文件扩展名对应的MIME类型前缀是否为"video/"来判断是否为视频文件。
	 * </p>
	 *
	 * <p><b>支持的视频格式：</b></p>
	 * <ul>
	 *     <li>常见格式：MP4、AVI、MKV、MOV</li>
	 *     <li>流媒体格式：M3U8、MPEG-TS</li>
	 *     <li>专业格式：ProRes、DNxHD</li>
	 * </ul>
	 *
	 * @param filename 待检测文件名，支持格式：
	 *               <ul>
	 *                 <li>纯文件名（如："movie.mp4"）</li>
	 *                 <li>完整路径（如："D:/videos/trailer.mov"）</li>
	 *               </ul>
	 * @return 检测结果，当满足以下条件时返回true：
	 *         <ul>
	 *           <li>文件名非空且有效</li>
	 *           <li>文件扩展名对应的MIME类型以"video/"开头</li>
	 *         </ul>
	 * @see #getMimeType(String)
	 * @since 1.0.0
	 */
	public static boolean isVideoType(final String filename) {
		if (StringUtils.isBlank(filename)) {
			return false;
		}
		String mimeType = MIME_TYPE_MAP.getContentType(getName(filename).toLowerCase());
		return FileType.VIDEO.getTypes().contains(mimeType) || mimeType.startsWith(IOConstants.VIDEO_MIME_TYPE_PREFIX);
	}

	/**
	 * 判断是否为音频类型
	 * <p>
	 * 通过检测文件扩展名对应的MIME类型前缀是否为"audio/"来判断是否为音频文件。
	 * </p>
	 *
	 * <p><b>支持的音频格式：</b></p>
	 * <ul>
	 *     <li>常见格式：MP3、WAV、AAC、FLAC</li>
	 *     <li>无损格式：ALAC、APE</li>
	 *     <li>专业格式：MIDI、DSD</li>
	 * </ul>
	 *
	 * @param filename 待检测文件名，支持格式：
	 *               <ul>
	 *                 <li>纯文件名（如："song.mp3"）</li>
	 *                 <li>完整路径（如："C:/music/recording.wav"）</li>
	 *               </ul>
	 * @return 检测结果，当满足以下条件时返回true：
	 *         <ul>
	 *           <li>文件名非空且有效</li>
	 *           <li>文件扩展名对应的MIME类型以"audio/"开头</li>
	 *         </ul>
	 * @see #getMimeType(String)
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
	 * 判断是否为指定类型
	 * <p>
	 * 严格匹配文件扩展名对应的MIME类型与指定类型是否一致（不区分大小写）。
	 * </p>
	 *
	 * <p><b>典型用例：</b></p>
	 * <pre>{@code
	 * // 检查是否为PNG图片
	 * boolean isPng = FilenameUtils.isMimeType("image.png", "image/png");
	 * }</pre>
	 *
	 * @param filename 待检测文件名，支持格式：
	 *               <ul>
	 *                 <li>纯文件名（如："data.json"）</li>
	 *                 <li>完整路径（如："C:/data/report.pdf"）</li>
	 *               </ul>
	 * @param mimeType 目标MIME类型（如："application/pdf"），要求：
	 *               <ul>
	 *                 <li>非空字符串</li>
	 *                 <li>标准MIME类型格式</li>
	 *               </ul>
	 * @return 匹配结果，当满足以下条件时返回true：
	 *         <ul>
	 *           <li>文件名和mimeType参数均非空</li>
	 *           <li>文件扩展名对应的MIME类型与目标类型完全匹配（忽略大小写）</li>
	 *         </ul>
	 * @see #getMimeType(String)
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
	 * 判断是否为任一类型
	 * <p>
	 * 检查文件扩展名对应的MIME类型是否匹配给定的任意一个MIME类型（不区分大小写）。
	 * </p>
	 *
	 * <p><b>典型用例：</b></p>
	 * <pre>{@code
	 * // 检查是否为图片或PDF
	 * boolean isValid = FilenameUtils.isAnyMimeType("file.pdf",
	 *     "image/jpeg", "image/png", "application/pdf");
	 * }</pre>
	 *
	 * @param filename  待检测文件名，支持格式：
	 *                <ul>
	 *                  <li>纯文件名（如："data.json"）</li>
	 *                  <li>完整路径（如："C:/data/report.pdf"）</li>
	 *                </ul>
	 * @param mimeTypes 允许的MIME类型数组，要求：
	 *                <ul>
	 *                  <li>非空数组</li>
	 *                  <li>每个元素为标准MIME类型格式</li>
	 *                </ul>
	 * @return 匹配结果，当满足以下条件时返回true：
	 *         <ul>
	 *           <li>文件名和mimeTypes参数均非空</li>
	 *           <li>文件扩展名对应的MIME类型与任一目标类型匹配（忽略大小写）</li>
	 *         </ul>
	 * @see #getMimeType(String)
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
	 * 判断是否为任一类型（集合版本）
	 * <p>
	 * 检查文件扩展名对应的MIME类型是否匹配集合中的任意一个MIME类型（不区分大小写）。
	 * </p>
	 *
	 * <p><b>与数组版本的区别：</b></p>
	 * <ul>
	 *     <li>支持动态生成的MIME类型集合</li>
	 *     <li>适用于预先定义的允许类型列表</li>
	 * </ul>
	 *
	 * @param filename  待检测文件名，支持格式：
	 *                <ul>
	 *                  <li>纯文件名（如："data.json"）</li>
	 *                  <li>完整路径（如："C:/data/report.pdf"）</li>
	 *                </ul>
	 * @param mimeTypes 允许的MIME类型集合，要求：
	 *                <ul>
	 *                  <li>非空集合</li>
	 *                  <li>每个元素为标准MIME类型格式</li>
	 *                </ul>
	 * @return 匹配结果，当满足以下条件时返回true：
	 *         <ul>
	 *           <li>文件名和mimeTypes参数均非空</li>
	 *           <li>文件扩展名对应的MIME类型与集合中任一目标类型匹配（忽略大小写）</li>
	 *         </ul>
	 * @see #getMimeType(String)
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
	 * <p>
	 * 该方法将原始文件名的主体和扩展名整体替换为新名称，适用于需要同时修改文件名和扩展名的场景。
	 * </p>
	 *
	 * <p><b>特性说明：</b></p>
	 * <ul>
	 *     <li>保留原始路径结构</li>
	 *     <li>支持无扩展名的新文件名</li>
	 *     <li>自动处理路径分隔符</li>
	 * </ul>
	 *
	 * <p><b>处理规则：</b></p>
	 * <ol>
	 *     <li>提取原始文件路径部分</li>
	 *     <li>完全替换文件名部分（包括扩展名）</li>
	 *     <li>拼接新路径</li>
	 * </ol>
	 *
	 * @param filename    原始文件路径或文件名，支持格式：
	 *                  <ul>
	 *                    <li>纯文件名（如："data.txt"）</li>
	 *                    <li>完整路径（如："C:/docs/report.pdf"）</li>
	 *                  </ul>
	 * @param newFilename 新的完整文件名，处理规则：
	 *                  <ul>
	 *                    <li>可包含扩展名（如："backup.zip"）</li>
	 *                    <li>可不含扩展名（如："config"）</li>
	 *                  </ul>
	 * @return 处理后的完整路径，示例：
	 *         <ul>
	 *           <li>输入 ("data.csv", "backup") → "backup"</li>
	 *           <li>输入 ("photo.jpg", "vacation.png") → "vacation.png"</li>
	 *           <li>输入 ("/var/log/app.log", "error_2023.log") → "/var/log/error_2023.log"</li>
	 *         </ul>
	 * @throws IllegalArgumentException 当以下情况时抛出：
	 *                                  <ul>
	 *                                    <li>newFilename参数为空</li>
	 *                                    <li>filename参数不包含有效文件名（如：纯目录路径）</li>
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
	 * <p>
	 * 该方法仅替换文件名的主体部分，保留原始扩展名和路径结构。
	 * </p>
	 *
	 * <p><b>与rename方法的区别：</b></p>
	 * <ul>
	 *     <li>仅修改文件名主体部分</li>
	 *     <li>自动保留原始扩展名</li>
	 *     <li>适用于需要保持文件类型不变的场景</li>
	 * </ul>
	 *
	 * @param filename    原始文件名（包含路径），要求：
	 *                  <ul>
	 *                    <li>非空字符串</li>
	 *                    <li>包含有效文件名（不能是纯目录路径）</li>
	 *                  </ul>
	 * @param newBaseName 新基名，处理规则：
	 *                  <ul>
	 *                    <li>不应包含扩展名分隔符</li>
	 *                    <li>不应包含路径分隔符</li>
	 *                  </ul>
	 * @return 包含路径的新文件名，示例：
	 *         <ul>
	 *           <li>输入 ("file.txt", "new") → "new.txt"</li>
	 *           <li>输入 ("/path/to/old.jpg", "photo") → "/path/to/photo.jpg"</li>
	 *           <li>输入 ("config.bak", "settings") → "settings.bak"</li>
	 *         </ul>
	 * @throws IllegalArgumentException 当以下情况时抛出：
	 *                                  <ul>
	 *                                    <li>newBaseName为空</li>
	 *                                    <li>filename不包含有效文件名（如：纯目录路径）</li>
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
	 * <p>
	 * 智能处理文件扩展名替换，支持多种输入格式和边界情况。
	 * </p>
	 *
	 * <p><b>特性说明：</b></p>
	 * <ul>
	 *     <li>自动处理扩展名分隔符（输入"pdf"或".pdf"均可）</li>
	 *     <li>支持移除扩展名（当newExtension为空时）</li>
	 *     <li>保留原始文件路径结构</li>
	 * </ul>
	 *
	 * <p><b>处理规则：</b></p>
	 * <ol>
	 *     <li>移除原始文件扩展名（如果存在）</li>
	 *     <li>规范化新扩展名格式（自动添加分隔符）</li>
	 *     <li>拼接新文件名</li>
	 * </ol>
	 *
	 * @param filename     原始文件名，要求：
	 *                   <ul>
	 *                     <li>非空字符串</li>
	 *                     <li>包含有效文件名（不能是纯目录路径）</li>
	 *                   </ul>
	 * @param newExtension 新扩展名，处理规则：
	 *                   <ul>
	 *                     <li>可为空（表示移除扩展名）</li>
	 *                     <li>自动处理分隔符（输入"pdf"或".pdf"效果相同）</li>
	 *                   </ul>
	 * @return 处理后的完整文件名，示例：
	 *         <ul>
	 *           <li>输入 ("file.txt", "csv") → "file.csv"</li>
	 *           <li>输入 ("/path/to/data.old", "json") → "/path/to/data.json"</li>
	 *           <li>输入 ("config", ".xml") → "config.xml"</li>
	 *         </ul>
	 * @throws IllegalArgumentException 当filename不包含有效文件名时抛出
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
	 * <p>
	 * 通过分析路径结构，精确判断是否为有效的目录路径（而非文件路径）。
	 * </p>
	 *
	 * <p><b>识别规则：</b></p>
	 * <ul>
	 *     <li>路径非空且有效</li>
	 *     <li>路径以文件分隔符结尾（系统自适应）</li>
	 *     <li>通过{@link org.apache.commons.io.FilenameUtils#getName(String)}获取的文件名为空</li>
	 * </ul>
	 *
	 * <p><b>与isFilePath方法的区别：</b></p>
	 * <ul>
	 *     <li>本方法专门用于识别目录结构</li>
	 *     <li>两者结果互斥（同一路径不会同时返回true）</li>
	 * </ul>
	 *
	 * @param directoryPath 待检测的路径字符串，支持格式：
	 *                    <ul>
	 *                      <li>Unix风格路径（/home/user/）</li>
	 *                      <li>Windows风格路径（C:\logs\）</li>
	 *                      <li>相对路径（../docs/）</li>
	 *                    </ul>
	 * @return 当路径符合目录特征时返回true，特别情况：
	 *         <ul>
	 *           <li>空路径返回false</li>
	 *           <li>文件路径返回false</li>
	 *           <li>无效路径返回false</li>
	 *         </ul>
	 * @see #isFilePath(String)
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
	 * <p>
	 * 通过分析路径结构，判断是否为有效的文件路径（而非目录路径）。
	 * </p>
	 *
	 * <p><b>识别规则：</b></p>
	 * <ul>
	 *     <li>路径不以文件分隔符结尾</li>
	 *     <li>通过{@link #getName(String)}获取的文件名非空</li>
	 *     <li>符合操作系统路径规范</li>
	 * </ul>
	 *
	 * @param filePath 待检测的路径字符串，支持格式：
	 *               <ul>
	 *                 <li>Unix风格路径（/home/user/file.txt）</li>
	 *                 <li>Windows风格路径（C:\Documents\data.csv）</li>
	 *                 <li>相对路径（../docs/readme.md）</li>
	 *               </ul>
	 * @return 当路径符合文件特征时返回true，特别情况：
	 *         <ul>
	 *           <li>空路径返回false</li>
	 *           <li>纯目录路径返回false</li>
	 *         </ul>
	 * @see #isDirectoryPath(String)
	 * @since 1.0.0
	 */
	public static boolean isFilePath(final String filePath) {
		if (StringUtils.isBlank(filePath)) {
			return false;
		}
		return StringUtils.isNotBlank(getName(filePath));
	}
}