/*
 *   Copyright 2026 pangju666
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

package io.github.pangju666.commons.ffmpeg.builder;

import io.github.pangju666.commons.ffmpeg.model.OutputOption;
import org.apache.commons.lang3.StringUtils;
import org.bytedeco.ffmpeg.avcodec.AVCodec;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 媒体输出选项构建器抽象基类
 * <p>
 * 提供流式 API 用于构建 {@link OutputOption} 实例，支持链式调用。
 * 封装了音频和视频输出选项的通用配置方法，包括编码器设置、元数据管理等。
 * </p>
 * <h3>核心特性</h3>
 * <ul>
 *     <li>支持设置编码器（通过 AVCodec 对象或编码器 ID）</li>
 *     <li>支持添加单个或批量元数据</li>
 *     <li>支持剥离元数据</li>
 *     <li>提供自引用返回以支持链式调用</li>
 * </ul>
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 设置编码器
 * builder.codecId(avcodec.AV_CODEC_ID_H264);
 *
 * // 添加元数据
 * builder.addMetadata("title", "My Video")
 *        .addMetadata("author", "pangju666");
 *
 * // 批量添加元数据
 * Map<String, String> metadata = new HashMap<>();
 * metadata.put("title", "My Video");
 * metadata.put("author", "pangju666");
 * builder.addMetadata(metadata);
 *
 * // 剥离元数据
 * builder.stripMetadata();
 *
 * // 构建最终选项
 * OutputOption option = builder.build();
 * }</pre>
 *
 * @param <T> 构建器自身类型，用于链式调用
 * @param <V> 输出选项类型
 * @author pangju666
 * @see OutputOption
 * @since 1.1.0
 */
public abstract class OutputOptionBuilder<T extends OutputOptionBuilder<T, V>, V extends OutputOption> {
	/**
	 * 内部维护的输出选项实例
	 *
	 * @since 1.1.0
	 */
	protected final V outputOption;

	/**
	 * 构造函数
	 *
	 * @param outputOption 输出选项实例
	 * @since 1.1.0
	 */
	protected OutputOptionBuilder(V outputOption) {
		this.outputOption = outputOption;
	}

	/**
	 * 设置编码器
	 *
	 * @param codec FFmpeg 的 AVCodec 对象
	 * @return 构建器自身，用于链式调用
	 * @since 1.1.0
	 */
	public T codec(AVCodec codec) {
		this.outputOption.setCodec(codec);

		return self();
	}

	/**
	 * 设置编码器 ID
	 *
	 * @param codecId 编码器 ID
	 * @return 构建器自身，用于链式调用
	 * @since 1.1.0
	 */
	public T codecId(int codecId) {
		this.outputOption.setCodecId(codecId);

		return self();
	}

	/**
	 * 添加单个元数据
	 * <p>如果 key 为空或 value 为 null，则跳过该条目</p>
	 *
	 * @param key   元数据键
	 * @param value 元数据值
	 * @return 构建器自身，用于链式调用
	 * @since 1.1.0
	 */
	public T addMetadata(String key, String value) {
		if (StringUtils.isNotBlank(key) && Objects.nonNull(value)) {
			outputOption.getMetadata().put(key, value);
		}

		return self();
	}

	/**
	 * 批量添加元数据
	 * <p>会过滤掉 key 为空或 value 为 null 的条目</p>
	 *
	 * @param metadata 元数据映射
	 * @return 构建器自身，用于链式调用
	 * @since 1.1.0
	 */
	public T addMetadata(Map<String, String> metadata) {
		if (Objects.nonNull(metadata) && !metadata.isEmpty()) {
			Map<String, String> validMetadata = metadata.entrySet()
				.stream()
				.filter(entry -> StringUtils.isNotBlank(entry.getKey()) &&
					Objects.nonNull(entry.getValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			outputOption.getMetadata().putAll(validMetadata);
		}

		return self();
	}

	/**
	 * 设置为剥离元数据模式
	 * <p>输出时将不包含任何元数据</p>
	 *
	 * @return 构建器自身，用于链式调用
	 * @since 1.1.0
	 */
	public T stripMetadata() {
		this.outputOption.setStripMetadata(true);

		return self();
	}

	/**
	 * 设置是否剥离元数据
	 *
	 * @param stripMetadata 是否剥离元数据
	 * @return 构建器自身，用于链式调用
	 * @since 1.1.0
	 */
	public T stripMetadata(boolean stripMetadata) {
		this.outputOption.setStripMetadata(stripMetadata);

		return self();
	}

	/**
	 * 构建输出选项实例
	 *
	 * @return 输出选项实例
	 * @since 1.1.0
	 */
	public V build() {
		return this.outputOption;
	}

	/**
	 * 返回构建器自身
	 * <p>用于子类实现链式调用</p>
	 *
	 * @return 构建器自身
	 * @since 1.1.0
	 */
	@SuppressWarnings("unchecked")
	public T self() {
		return (T) this;
	}
}
