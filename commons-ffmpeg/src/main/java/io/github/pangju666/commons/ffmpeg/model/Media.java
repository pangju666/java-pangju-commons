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

package io.github.pangju666.commons.ffmpeg.model;

import io.github.pangju666.commons.io.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * 媒体文件抽象基类，定义音视频媒体的通用属性和构建器规范
 *
 * @author pangju666
 * @since 1.1.0
 */
public abstract class Media {
	/**
	 * 媒体文件格式（如mp3、mp4、wav等）
	 *
	 * @since 1.1.0
	 */
	protected String format;
	/**
	 * 媒体元数据（如标题、作者、时长描述等键值对）
	 *
	 * @since 1.1.0
	 */
	protected Map<String, String> metadata = Collections.emptyMap();
	/**
	 * 编码器名称（如mp3、aac、h264等）
	 *
	 * @since 1.1.0
	 */
	protected String codecName;
	/**
	 * 编码器ID（对应FFmpeg的编码器标识）
	 *
	 * @since 1.1.0
	 */
	protected int codecId;

	protected Media() {
	}

	/**
	 * 获取媒体格式
	 *
	 * @return 格式名称（如mp3、wav），可为null
	 * @since 1.1.0
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * 获取媒体元数据（不可修改）
	 *
	 * @return 元数据键值对集合，空集合表示无元数据
	 * @since 1.1.0
	 */
	public Map<String, String> getMetadata() {
		return Collections.unmodifiableMap(metadata);
	}

	/**
	 * 获取编码器名称
	 *
	 * @return 编码器名称（如mp3、aac），可为null
	 * @since 1.1.0
	 */
	public String getCodecName() {
		return codecName;
	}

	/**
	 * 获取编码器ID
	 *
	 * @return FFmpeg定义的编码器ID，默认0
	 * @since 1.1.0
	 */
	public int getCodecId() {
		return codecId;
	}

	/**
	 * 媒体对象构建器抽象类，定义通用的构建逻辑
	 *
	 * @param <T> 构建器自身类型（用于链式调用）
	 * @param <V> 媒体对象类型（Audio/Video等）
	 * @author pangju666
	 * @since 1.1.0
	 */
	public static class Builder<T extends Builder<T, V>, V extends Media> {
		/**
		 * 待构建的媒体对象
		 *
		 * @since 1.1.0
		 */
		protected final V media;

		/**
		 * 构建器构造器
		 *
		 * @param media 待构建的媒体对象实例，不可为null
		 * @throws IllegalArgumentException 当media为null时抛出
		 * @since 1.1.0
		 */
		protected Builder(V media) {
			Validate.notNull(media, "media 不可为 null");

			this.media = media;
		}

		/**
		 * 从FFmpegFrameGrabber解析媒体信息并填充到构建器
		 *
		 * @param grabber FFmpeg帧抓取器（已启动）
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public T parse(FFmpegFrameGrabber grabber) {
			init(grabber);
			return self();
		}

		/**
		 * 从文件解析媒体信息并填充到构建器
		 *
		 * @param file 媒体文件，不可为null且需存在
		 * @return 构建器自身，用于链式调用
		 * @throws IOException 文件读取失败、FFmpeg解析失败时抛出
		 * @since 1.1.0
		 */
		public T parse(File file) throws IOException {
			try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file)) {
				grabber.start();
				init(grabber);
				return self();
			}
		}

		/**
		 * 从字节数组解析媒体信息并填充到构建器
		 *
		 * @param bytes 媒体文件字节数组，不可为null
		 * @return 构建器自身，用于链式调用
		 * @throws IOException FFmpeg解析字节数组失败时抛出
		 * @since 1.1.0
		 */
		public T parse(byte[] bytes) throws IOException {
			try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(IOUtils.toUnsynchronizedByteArrayInputStream(bytes))) {
				grabber.start();
				init(grabber);
				return self();
			}
		}

		/**
		 * 从输入流解析媒体信息并填充到构建器
		 * <p>注意：输入流会被该方法关闭，若需复用请自行处理流复制</p>
		 *
		 * @param inputStream 媒体文件输入流，不可为null
		 * @return 构建器自身，用于链式调用
		 * @throws IOException FFmpeg解析输入流失败、流读取异常时抛出
		 * @since 1.1.0
		 */
		public T parse(InputStream inputStream) throws IOException {
			try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputStream)) {
				grabber.start();
				init(grabber);
				return self();
			}
		}

		/**
		 * 设置媒体格式
		 *
		 * @param format 格式名称（如mp3、wav），空值不生效
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public T format(String format) {
			if (StringUtils.isNotBlank(format)) {
				media.format = format;
			}
			return self();
		}

		/**
		 * 设置媒体元数据
		 *
		 * @param metadata 元数据键值对，null/空值会重置为空集合
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public T metadata(Map<String, String> metadata) {
			if (Objects.nonNull(metadata) && !metadata.isEmpty()) {
				media.metadata = Collections.unmodifiableMap(metadata);
			} else {
				media.metadata = Collections.emptyMap();
			}
			return self();
		}

		/**
		 * 设置编码器信息
		 *
		 * @param codecId   编码器ID（FFmpeg定义）
		 * @param codecName 编码器名称（如mp3、aac）
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public T codec(int codecId, String codecName) {
			media.codecId = codecId;
			media.codecName = codecName;
			return self();
		}

		/**
		 * 仅设置编码器名称
		 *
		 * @param codecName 编码器名称（如mp3、aac），可为null
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public T codecName(String codecName) {
			media.codecName = codecName;
			return self();
		}

		/**
		 * 重置构建器为初始状态
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public T reset() {
			media.format = null;
			media.metadata = Collections.emptyMap();
			media.codecId = 0;
			media.codecName = null;
			return self();
		}

		/**
		 * 构建媒体对象
		 *
		 * @return 构建完成的媒体对象（Audio/Video等）
		 * @since 1.1.0
		 */
		public V build() {
			return media;
		}

		/**
		 * 获取构建器自身实例（用于泛型链式调用）
		 *
		 * @return 构建器自身
		 * @since 1.1.0
		 */
		@SuppressWarnings("unchecked")
		protected T self() {
			return (T) this;
		}

		/**
		 * 初始化媒体对象通用属性（从FFmpegFrameGrabber）
		 * <p>子类可重写此方法扩展属性初始化逻辑</p>
		 *
		 * @param grabber FFmpeg帧抓取器（已启动）
		 * @since 1.1.0
		 */
		protected void init(FFmpegFrameGrabber grabber) {
			media.format = grabber.getFormat();
			media.metadata = Collections.unmodifiableMap(grabber.getMetadata());
		}
	}
}
