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

package io.github.pangju666.commons.media.model;

import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.IOUtils;
import org.apache.commons.lang3.Validate;
import org.bytedeco.ffmpeg.avcodec.AVCodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * 媒体文件抽象基类，定义音视频媒体的通用属性和构建器规范
 * <p>
 * 该类作为音频（Audio）、视频（Video）等具体媒体类型的父类，封装了所有媒体文件的通用特征：
 * 格式、元数据、编码器信息等，同时提供统一的构建器模式来解析和构建媒体对象。
 * </p>
 * <h3>核心特性</h3>
 * <ul>
 *     <li>基于 JavaCV/FFmpeg 自动解析媒体元数据</li>
 *     <li>支持多种来源解析方式（File、byte[]、InputStream、FFmpegFrameGrabber）</li>
 *     <li>采用 Fluent Builder 模式，支持链式调用</li>
 *     <li>抽象基类设计，便于扩展新的媒体类型</li>
 *     <li>不可变对象设计，所有属性为 final，确保线程安全</li>
 *     <li>支持从现有媒体对象克隆并修改</li>
 *     <li>支持从 FFmpeg AVCodec 对象设置编码器信息</li>
 * </ul>
 *
 * @author pangju666
 * @see Audio
 * @see Video
 * @see org.bytedeco.javacv.FFmpegFrameGrabber
 * @see org.bytedeco.ffmpeg.avcodec.AVCodec
 * @since 1.1.0
 */
public abstract class Media {
	/**
	 * 媒体文件格式（如mp3、mp4、wav、flv等）
	 * <p>格式名称与FFmpeg原生格式标识保持一致，便于后续编解码操作</p>
	 * <p>该值通过 {@link FFmpegFrameGrabber#getFormat()} 获取或手动设置</p>
	 *
	 * @since 1.1.0
	 */
	protected final String format;

	/**
	 * 媒体元数据（如标题、作者、时长、比特率等键值对）
	 * <p>元数据直接从FFmpegFrameGrabber解析而来，键名与FFmpeg输出的元数据字段一致</p>
	 * <p>该值始终为不可修改的 {@link Collections#unmodifiableMap(Map)}，防止外部篡改</p>
	 *
	 * @since 1.1.0
	 */
	protected final Map<String, String> metadata;

	/**
	 * 编码器名称（如mp3、aac、h264、h265等）
	 * <p>编码器名称与FFmpeg编码器标识保持一致，用于指定编解码算法</p>
	 * <p>该值通过 {@link FFmpegFrameGrabber#getAudioCodecName()} 或 {@link FFmpegFrameGrabber#getVideoCodecName()} 获取</p>
	 *
	 * @since 1.1.0
	 */
	protected final String codecName;

	/**
	 * 编码器ID（对应FFmpeg的AVCodecID枚举值）
	 * <p>数值型ID可直接用于FFmpeg底层API调用，比名称更精准</p>
	 * <p>该值通过 {@link FFmpegFrameGrabber#getAudioCodec()} 或 {@link FFmpegFrameGrabber#getVideoCodec()} 获取，默认值为 0</p>
	 *
	 * @since 1.1.0
	 */
	protected final int codecId;

	/**
	 * 受保护的构造函数
	 * <p>
	 * 仅用于子类继承和构建器模式内部实例化，
	 * 不对外暴露直接构造能力，确保实例化过程的一致性和安全性。
	 * </p>
	 *
	 * @param format    媒体格式
	 * @param codecName 编码器名称
	 * @param metadata  媒体元数据
	 * @param codecId   编码器ID
	 * @since 1.1.0
	 */
	protected Media(String format, String codecName, Map<String, String> metadata, int codecId) {
		this.format = format;
		this.codecName = codecName;
		this.metadata = Collections.unmodifiableMap(metadata);
		this.codecId = codecId;
	}

	/**
	 * 获取媒体格式
	 *
	 * @return 格式名称（如mp3、wav），未解析时返回null
	 * @since 1.1.0
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * 获取媒体元数据（不可修改）
	 * <p>返回的是不可修改的Map，防止外部篡改元数据内容</p>
	 *
	 * @return 元数据键值对集合，空集合表示无元数据
	 * @since 1.1.0
	 */
	public Map<String, String> getMetadata() {
		return metadata;
	}

	/**
	 * 获取编码器名称
	 *
	 * @return 编码器名称（如mp3、aac），未解析时返回null
	 * @since 1.1.0
	 */
	public String getCodecName() {
		return codecName;
	}

	/**
	 * 获取编码器ID
	 * <p>默认值为0，表示未解析或不支持的编码器类型</p>
	 *
	 * @return FFmpeg定义的编码器ID，默认0
	 * @since 1.1.0
	 */
	public int getCodecId() {
		return codecId;
	}

	/**
	 * 媒体对象构建器抽象类，定义通用的构建逻辑
	 * <p>
	 * 采用泛型自限定模式（Fluent Builder）实现链式调用，同时为不同媒体类型（Audio/Video）
	 * 提供统一的解析入口（文件、字节数组、输入流、FFmpegFrameGrabber）。
	 * </p>
	 * <h3>泛型说明</h3>
	 * <ul>
	 *     <li><b>T</b>：构建器自身类型（用于链式调用，如Audio.Builder/Video.Builder）</li>
	 *     <li><b>V</b>：媒体对象类型（Audio/Video等具体媒体实现类）</li>
	 * </ul>
	 * <h3>创建构建器的两种方式</h3>
	 * <ul>
	 *     <li><b>空构建器</b>：使用 {@code new Xxx.Builder()} 创建空白构建器，用于全新构建</li>
	 *     <li><b>复制构建器</b>：（子类通常会提供静态工厂方法）基于现有媒体对象创建，用于修改现有对象</li>
	 * </ul>
	 *
	 * @param <T> 构建器自身类型（用于链式调用）
	 * @param <V> 媒体对象类型（Audio/Video等具体实现类）
	 * @author pangju666
	 * @since 1.1.0
	 */
	public static abstract class Builder<T extends Builder<T, V>, V extends Media> {
		/**
		 * 媒体文件格式（如mp3、mp4、wav、flv等）
		 * <p>格式名称与FFmpeg原生格式标识保持一致，便于后续编解码操作
		 * <p>该值通过 {@link FFmpegFrameGrabber#getFormat()} 获取或手动设置
		 *
		 * @since 1.1.0
		 */
		protected String format;

		/**
		 * 媒体元数据（如标题、作者、时长、比特率等键值对）
		 * <p>元数据直接从FFmpegFrameGrabber解析而来，键名与FFmpeg输出的元数据字段一致
		 *
		 * @since 1.1.0
		 */
		protected Map<String, String> metadata;

		/**
		 * 编码器名称（如mp3、aac、h264、h265等）
		 * <p>编码器名称与FFmpeg编码器标识保持一致，用于指定编解码算法
		 * <p>该值通过 {@link FFmpegFrameGrabber#getAudioCodecName()} 或 {@link FFmpegFrameGrabber#getVideoCodecName()} 获取
		 *
		 * @since 1.1.0
		 */
		protected String codecName;

		/**
		 * 编码器ID（对应FFmpeg的AVCodecID枚举值）
		 * <p>数值型ID可直接用于FFmpeg底层API调用，比名称更精准
		 * <p>该值通过 {@link FFmpegFrameGrabber#getAudioCodec()} 或 {@link FFmpegFrameGrabber#getVideoCodec()} 获取，默认值为 0
		 *
		 * @since 1.1.0
		 */
		protected int codecId;

		/**
		 * 受保护的构造函数
		 * <p>
		 * 仅用于子类继承，初始化构建器的默认值。
		 * </p>
		 *
		 * @since 1.1.0
		 */
		protected Builder() {
			initDefaultValue();
		}

		/**
		 * 基于已有媒体对象创建构建器的受保护构造函数
		 * <p>
		 * 仅用于子类继承，从现有媒体对象中复制所有通用属性到新的构建器中，
		 * 方便基于现有媒体对象进行修改或扩展。
		 * </p>
		 *
		 * @param media 源媒体对象，<b>不可为 null</b>
		 * @throws IllegalArgumentException 当 media 为 null 时抛出
		 * @since 1.1.0
		 */
		protected Builder(V media) {
			Validate.notNull(media, "media 不可为 null");

			this.format = media.getFormat();
			this.codecId = media.getCodecId();
			this.codecName = media.getCodecName();
			this.metadata = media.getMetadata();
		}

		/**
		 * 从 FFmpegFrameGrabber 解析媒体信息并填充
		 * <p>如果抓取器尚未启动（FormatContext 为空或 null），会自动启动抓取器，
		 * 然后从抓取器中读取媒体格式和元数据信息。
		 * </p>
		 *
		 * @param grabber FFmpeg 帧抓取器，不可为 null
		 * @return 构建器自身，用于链式调用
		 * @throws FFmpegFrameGrabber.Exception 当抓取器启动或读取媒体信息失败时抛出
		 * @throws IllegalArgumentException 当 grabber 为 null 时抛出
		 * @since 1.1.0
		 */
		public T parse(FFmpegFrameGrabber grabber) throws FFmpegFrameGrabber.Exception {
			Validate.notNull(grabber, "grabber 不可为 null");

			if (Objects.isNull(grabber.getFormatContext()) || grabber.getFormatContext().isNull()) {
				grabber.start();
			}
			this.format = grabber.getFormat();
			this.metadata = grabber.getMetadata();
			return self();
		}

		/**
		 * 从文件解析媒体信息并填充
		 * <p>内部会自动创建FFmpegFrameGrabber并启动，解析完成后自动关闭资源</p>
		 *
		 * @param file 媒体文件，不可为null且需存在（文件路径需合法）
		 * @return 构建器自身，用于链式调用
		 * @throws IOException              当文件不存在、无读取权限或FFmpeg解析失败时抛出
		 * @throws IllegalArgumentException 当file为null或不是有效文件时抛出
		 * @since 1.1.0
		 */
		public T parse(File file) throws IOException {
			FileUtils.checkFile(file, "file 不可为 null");

			try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file)) {
				grabber.start();
				parse(grabber);
				return self();
			}
		}

		/**
		 * 从字节数组解析媒体信息并填充
		 * <p>字节数组需为完整的媒体文件数据，内部会转换为输入流并自动管理资源</p>
		 *
		 * @param bytes 媒体文件字节数组，不可为null（空数组会导致解析失败）
		 * @return 构建器自身，用于链式调用
		 * @throws IOException              当FFmpeg无法解析字节数组中的媒体格式时抛出
		 * @throws IllegalArgumentException 当bytes为null时抛出
		 * @since 1.1.0
		 */
		public T parse(byte[] bytes) throws IOException {
			Validate.notNull(bytes, "bytes 不可为 null");

			try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(IOUtils.toUnsynchronizedByteArrayInputStream(bytes))) {
				grabber.start();
				parse(grabber);
				return self();
			}
		}

		/**
		 * 从输入流解析媒体信息并填充
		 * <p>输入流会被内部创建的FFmpegFrameGrabber完全消费，解析完成后自动关闭</p>
		 *
		 * @param inputStream 媒体文件输入流，不可为null
		 * @return 构建器自身，用于链式调用
		 * @throws IOException              当流读取异常、FFmpeg解析失败或流关闭失败时抛出
		 * @throws IllegalArgumentException 当inputStream为null时抛出
		 * @since 1.1.0
		 */
		public T parse(InputStream inputStream) throws IOException {
			Validate.notNull(inputStream, "媒体输入流不可为 null");

			try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputStream)) {
				grabber.start();
				parse(grabber);
				return self();
			}
		}

		/**
		 * 设置媒体格式
		 * <p>传入的格式会自动转换为小写，与FFmpeg保持一致</p>
		 *
		 * @param format 格式名称（如mp3、wav），不可为空白
		 * @return 构建器自身，用于链式调用
		 * @throws IllegalArgumentException 当format为null或空白时抛出
		 * @since 1.1.0
		 */
		public T format(String format) {
			Validate.notBlank(format, "format 不可为空");

			this.format = format.toLowerCase();
			return self();
		}

		/**
		 * 设置媒体元数据
		 * <p>传入的元数据会直接用于设置媒体的元数据信息。
		 * </p>
		 *
		 * @param metadata 元数据键值对，不可为 null
		 * @return 构建器自身，用于链式调用
		 * @throws IllegalArgumentException 当 metadata 为 null 时抛出
		 * @since 1.1.0
		 */
		public T metadata(Map<String, String> metadata) {
			Validate.notNull(metadata, "metadata 不可为 null");

			this.metadata = metadata;
			return self();
		}

		/**
		 * 从 AVCodec 对象设置编码器信息
		 * <p>直接从 FFmpeg 的 AVCodec 对象中提取编码器 ID 和名称，是设置编码器信息的推荐方式</p>
		 *
		 * @param codec FFmpeg 的 AVCodec 对象，<b>不可为 null</b>
		 * @return 构建器自身，用于链式调用
		 * @throws IllegalArgumentException 当 codec 为 null 时抛出
		 * @since 1.1.0
		 */
		public T codec(AVCodec codec) {
			Validate.notNull(codec, "avCodec 不可为 null");

			this.codecId = codec.id();
			this.codecName = codec.name().getString();
			return self();
		}

		/**
		 * 仅设置编码器ID
		 * <p>适用于仅需ID无需名称的场景，编码器名称保持当前值</p>
		 *
		 * @param codecId 编码器ID（FFmpeg定义的AVCodecID数值），不可小于0
		 * @return 构建器自身，用于链式调用
		 * @throws IllegalArgumentException 当codecId小于0时抛出
		 * @since 1.1.0
		 */
		public T codecId(int codecId) {
			Validate.isTrue(codecId >= 0, "codecId 不可小于0");

			this.codecId = codecId;
			return self();
		}

		/**
		 * 重置构建器为初始状态
		 * <p>清空所有已设置的属性，恢复到构建器创建时的初始状态</p>
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public T reset() {
			initDefaultValue();
			return self();
		}

		/**
		 * 构建媒体对象
		 * <p>返回最终构建完成的媒体对象，后续构建器的修改不会影响该对象</p>
		 *
		 * @return 构建完成的媒体对象（Audio/Video等具体实现类）
		 * @since 1.1.0
		 */
		public abstract V build();

		/**
		 * 获取构建器自身实例（用于泛型链式调用）
		 * <p>泛型自限定的核心方法，子类无需重写，自动返回自身类型</p>
		 *
		 * @return 构建器自身
		 * @since 1.1.0
		 */
		@SuppressWarnings("unchecked")
		protected T self() {
			return (T) this;
		}

		/**
		 * 初始化媒体对象默认值
		 * <p>将所有属性重置为初始状态</p>
		 *
		 * @since 1.1.0
		 */
		protected void initDefaultValue() {
			this.format = null;
			this.metadata = Collections.emptyMap();
			this.codecId = 0;
			this.codecName = null;
		}
	}
}
