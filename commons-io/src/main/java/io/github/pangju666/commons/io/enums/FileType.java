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

package io.github.pangju666.commons.io.enums;

import io.github.pangju666.commons.io.lang.IOConstants;

import java.util.Collections;
import java.util.Set;

/**
 * 文件类型枚举类，提供常见文件类型的分类标准
 * <p>
 * 本枚举定义了基于MIME类型的文件分类系统，支持两种匹配方式：
 * <ol>
 *     <li><b>前缀匹配</b> - 通过typePrefix匹配某一类文件（如"image/"匹配所有图片）</li>
 *     <li><b>精确匹配</b> - 通过types集合匹配特定文件类型（如压缩包的各种具体格式）</li>
 * </ol>
 * <p>
 * <b>典型使用场景：</b>
 * <ul>
 *     <li>文件上传时的类型校验</li>
 *     <li>文件分类管理</li>
 *     <li>根据类型显示不同图标</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public enum FileType {
	/**
	 * 图片类型，匹配所有MIME类型以"image/"开头的文件
	 * <p>常见格式：JPEG、PNG、GIF、WEBP等</p>
	 *
	 * @since 1.0.0
	 */
	IMAGE("图片", Collections.emptySet(), IOConstants.IMAGE_MIME_TYPE_PREFIX),
	/**
	 * 文本类型，匹配所有MIME类型以"text/"开头的文件
	 * <p>常见格式：TXT、CSV、HTML、XML等</p>
	 *
	 * @since 1.0.0
	 */
	TEXT("文本", Collections.emptySet(), IOConstants.TEXT_MIME_TYPE_PREFIX),
	/**
	 * 音频类型，匹配所有MIME类型以"audio/"开头的文件
	 * <p>常见格式：MP3、WAV、AAC、OGG等</p>
	 *
	 * @since 1.0.0
	 */
	AUDIO("音频", Collections.emptySet(), IOConstants.AUDIO_MIME_TYPE_PREFIX),
	/**
	 * 3D模型类型，匹配所有MIME类型以"model/"开头的文件
	 * <p>常见格式：STL、OBJ、FBX等</p>
	 *
	 * @since 1.0.0
	 */
	MODEL("模型", Collections.emptySet(), IOConstants.MODEL_MIME_TYPE_PREFIX),
	/**
	 * 视频类型，主要匹配MIME类型以"video/"开头的文件
	 * <p>包含特殊格式：HLS流媒体(application/vnd.apple.mpegurl)</p>
	 * <p>常见格式：MP4、AVI、MKV、MOV等</p>
	 *
	 * @since 1.0.0
	 */
	VIDEO("视频", Collections.singleton("application/vnd.apple.mpegurl"), IOConstants.VIDEO_MIME_TYPE_PREFIX),
	/**
	 * 压缩包类型，通过具体MIME类型精确匹配
	 * <p>支持的压缩格式：TAR、GZIP、BZIP2、ZIP、RAR、7Z、CAB等</p>
	 *
	 * @since 1.0.0
	 */
	COMPRESS("压缩包", Set.of(
		"application/x-tar", "application/x-gzip", "application/x-bzip", "application/x-bzip2",
		"application/zip", "application/x-uc2-compressed", "application/x-rar-compressed",
		"application/x-ace-compressed", "application/x-7z-compressed", "application/vnd.ms-cab-compressed"
	), null),
	/**
	 * 办公文档类型，通过具体MIME类型精确匹配
	 * <p>支持的文档格式：PDF、Excel(XLS/XLSX)、Word(DOC/DOCX)、PPT(PPT/PPTX)</p>
	 *
	 * @since 1.0.0
	 */
	DOCUMENT("文档", Set.of("application/pdf",
		"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel",
		"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword",
		"application/vnd.openxmlformats-officedocument.presentationml.presentation", "application/vnd.ms-powerpoint"
	), null);

	/**
	 * 文件类型的中文显示名称
	 * <p>用于用户界面展示，如"图片"、"文本"等友好名称</p>
	 *
	 * @since 1.0.0
	 */
	private final String label;
	/**
	 * 该类型对应的具体MIME类型集合
	 * <p>当typePrefix为null时，必须通过此集合中的值进行精确匹配</p>
	 *
	 * @since 1.0.0
	 */
	private final Set<String> types;
	/**
	 * MIME类型的前缀字符串
	 * <p>当不为null时，表示可以通过此前缀匹配一类文件（如"image/"）</p>
	 * <p>当为null时，必须使用types集合进行精确匹配</p>
	 *
	 * @since 1.0.0
	 */
	private final String typePrefix;

	/**
	 * 构造文件类型枚举实例
	 *
	 * @param label 类型中文名称，不可为空
	 * @param types 具体MIME类型集合，当typePrefix为null时不可为空集合
	 * @param typePrefix MIME类型前缀，当types为空集合时不可为null
	 * @throws IllegalArgumentException 当label为空，或types和typePrefix同时为null时抛出
	 * @since 1.0.0
	 */
	FileType(String label, Set<String> types, String typePrefix) {
		this.label = label;
		this.types = types;
		this.typePrefix = typePrefix;
	}

	/**
	 * 获取文件类型的中文标签
	 *
	 * @return 文件类型的友好显示名称，如"图片"、"文本"等
	 * @since 1.0.0
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * 获取该类型对应的具体MIME类型集合
	 * <p>返回的集合是不可修改的</p>
	 *
	 * @return 该类型支持的具体MIME类型集合
	 * @since 1.0.0
	 */
	public Set<String> getTypes() {
		return types;
	}

	/**
	 * 获取MIME类型的前缀匹配字符串
	 *
	 * @return 类型前缀字符串，如"image/"；可能为null表示需要精确匹配
	 * @since 1.0.0
	 */
	public String getTypePrefix() {
		return typePrefix;
	}
}
