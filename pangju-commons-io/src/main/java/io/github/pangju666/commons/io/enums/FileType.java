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

import java.util.Collections;
import java.util.Set;

/**
 * 文件类型枚举类，定义常见文件类型的分类标准
 * <p>
 * 每个枚举实例包含以下属性：
 * <ul>
 *     <li>label - 文件类型中文标签</li>
 *     <li>types - 具体MIME类型集合（当typePrefix为null时用于精确匹配）</li>
 *     <li>typePrefix - MIME类型前缀（用于前缀匹配，如"image/"匹配所有图片类型）</li>
 * </ul>
 * <p>
 * 使用说明：
 * <ol>
 *     <li>当typePrefix不为空时，通过MIME类型前缀匹配文件类型（如"image/png"会匹配IMAGE类型）</li>
 *     <li>当typePrefix为空时，需要严格匹配types集合中的具体MIME类型（如COMPRESS类型需要匹配具体的压缩格式类型）</li>
 * </ol>
 *
 * @author pangju666
 * @since 1.0.0
 */
public enum FileType {
	/**
	 * 图片类型（通过MIME类型前缀 image/ 匹配）
	 *
	 * @since 1.0.0
	 */
	IMAGE("图片", Collections.emptySet(), "image/"),
	/**
	 * 文本类型（通过MIME类型前缀 text/ 匹配）
	 *
	 * @since 1.0.0
	 */
	TEXT("文本", Collections.emptySet(), "text/"),
	/**
	 * 音频类型（通过MIME类型前缀 audio/ 匹配）
	 *
	 * @since 1.0.0
	 */
	AUDIO("音频", Collections.emptySet(), "audio/"),
	/**
	 * 模型类型（通过MIME类型前缀 model/ 匹配）
	 *
	 * @since 1.0.0
	 */
	MODEL("模型", Collections.emptySet(), "model/"),
	/**
	 * 视频类型（通过MIME类型前缀 video/ 匹配）
	 *
	 * @since 1.0.0
	 */
	VIDEO("视频", Collections.singleton("application/vnd.apple.mpegurl"), "video/"),
	/**
	 * 压缩包类型（通过具体MIME类型匹配）
	 *
	 * @since 1.0.0
	 */
	COMPRESS("压缩包", Set.of(
		"application/x-tar", "application/x-gzip", "application/x-bzip", "application/x-bzip2",
		"application/zip", "application/x-uc2-compressed", "application/x-rar-compressed",
		"application/x-ace-compressed", "application/x-7z-compressed", "application/vnd.ms-cab-compressed"
	), null),
	/**
	 * 文档类型（通过具体MIME类型匹配）
	 *
	 * @since 1.0.0
	 */
	DOCUMENT("文档", Set.of("application/pdf",
		"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel",
		"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword",
		"application/vnd.openxmlformats-officedocument.presentationml.presentation", "application/vnd.ms-powerpoint"
	), null);

	/**
	 * 文件类型中文标签（如：图片、文本等）
	 *
	 * @since 1.0.0
	 */
	private final String label;
	/**
	 * 具体MIME类型集合（当typePrefix为null时用于精确匹配）
	 *
	 * @since 1.0.0
	 */
	private final Set<String> types;
	/**
	 * MIME类型前缀（用于前缀匹配，如"image/"匹配所有图片类型）
	 *
	 * @since 1.0.0
	 */
	private final String typePrefix;

	/**
	 * 构造文件类型枚举
	 *
	 * @param label      中文类型标签
	 * @param types      具体MIME类型集合（当typePrefix为null时必须指定）
	 * @param typePrefix MIME类型前缀（当types为空时必须指定）
	 * @since 1.0.0
	 */
	FileType(String label, Set<String> types, String typePrefix) {
		this.label = label;
		this.types = types;
		this.typePrefix = typePrefix;
	}

	/**
	 * 获取文件类型中文标签
	 *
	 * @return 如："图片"、"文本"等
	 * @since 1.0.0
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * 获取具体MIME类型集合
	 *
	 * @return 不可修改的MIME类型集合（当typePrefix为null时用于精确匹配）
	 * @since 1.0.0
	 */
	public Set<String> getTypes() {
		return types;
	}

	/**
	 * 获取MIME类型前缀
	 *
	 * @return 类型前缀字符串（如"image/"，当types为空集合时用于前缀匹配）
	 * @since 1.0.0
	 */
	public String getTypePrefix() {
		return typePrefix;
	}
}
