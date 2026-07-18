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

import io.github.pangju666.commons.ffmpeg.enums.FrameType;
import io.github.pangju666.commons.ffmpeg.utils.FFmpegUtils;
import io.github.pangju666.commons.io.exception.UnsupportedResourceException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.bytedeco.ffmpeg.avcodec.AVCodec;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 媒体输出选项抽象基类，定义音视频媒体的通用属性和配置规范
 * <p>
 * 该类作为音频（{@link AudioOutputOption}）、视频（{@link VideoOutputOption}）等具体媒体类型的父类，
 * 封装了所有媒体文件的通用特征：格式、编码器信息、元数据等，
 * 同时提供统一的配置方法来设置 FFmpeg 录制器。
 * </p>
 * <h3>核心特性</h3>
 * <ul>
 *     <li>基于 JavaCV/FFmpeg 自动验证格式和编码器支持</li>
 *     <li>抽象基类设计，便于扩展新的媒体类型</li>
 *     <li>支持从 FFmpeg AVCodec 对象设置编码器信息</li>
 *     <li>支持元数据管理和剥离元数据</li>
 *     <li>提供抽象方法用于配置录制器</li>
 * </ul>
 * <h3>通用属性</h3>
 * <ul>
 *     <li>{@link #format} - 媒体容器格式（如 mp3、mp4、wav、flac）</li>
 *     <li>{@link #metadata} - 媒体元数据（如标题、艺术家、专辑）</li>
 *     <li>{@link #codecId} - FFmpeg 编码器 ID（如 AV_CODEC_ID_H264）</li>
 *     <li>{@link #stripMetadata} - 是否剥离元数据</li>
 * </ul>
 *
 * @author pangju666
 * @see AudioOutputOption
 * @see VideoOutputOption
 * @see FFmpegFrameRecorder
 * @see AVCodec
 * @since 1.1.0
 */
public abstract class OutputOption {
	/**
	 * 媒体文件格式（如mp3、mp4、wav、flv等）
	 * <p>格式名称与FFmpeg原生格式标识保持一致，用于指定输出容器格式</p>
	 * <p>常见格式：mp3、wav、flac、aac（音频）；mp4、webm、mkv、avi（视频）</p>
	 *
	 * @since 1.1.0
	 */
	protected String format;

	/**
	 * 媒体元数据（如标题、作者、时长、比特率等键值对）
	 * <p>用于设置输出媒体的元数据信息，键名与FFmpeg支持的元数据字段一致</p>
	 * <p>常见元数据字段：title（标题）、artist（艺术家）、album（专辑）</p>
	 *
	 * @since 1.1.0
	 */
	protected Map<String, String> metadata = new HashMap<>();

	/**
	 * 编码器ID（对应FFmpeg的AVCodecID枚举值）
	 * <p>数值型ID可直接用于FFmpeg底层API调用，比名称更精准</p>
	 * <p>默认值为 AV_CODEC_ID_NONE</p>
	 * <p>常见编码器ID：AV_CODEC_ID_MP3、AV_CODEC_ID_AAC、AV_CODEC_ID_H264、AV_CODEC_ID_H265</p>
	 *
	 * @since 1.1.0
	 */
	protected int codecId = avcodec.AV_CODEC_ID_NONE;

	/**
	 * 是否剥离元数据
	 * <p>如果为 true，输出时不包含任何元数据；如果为 false，保留元数据</p>
	 * <p>默认值为 false</p>
	 *
	 * @since 1.1.0
	 */
	protected boolean stripMetadata = false;

	/**
	 * 默认构造函数
	 *
	 * @since 1.1.0
	 */
	protected OutputOption() {
	}

	/**
	 * 根据格式构造输出选项
	 *
	 * @param format 媒体格式，不可为空
	 * @throws IllegalArgumentException     当 format 为空时抛出
	 * @throws UnsupportedResourceException 当格式不被支持时抛出
	 * @since 1.1.0
	 */
	protected OutputOption(String format) {
		Validate.notBlank(format, "format 不可为空");

		setFormat(format);
	}

	/**
	 * 从 AVCodec 对象设置编码器信息
	 * <p>直接从 FFmpeg 的 AVCodec 对象中提取编码器 ID 和名称，是设置编码器信息的推荐方式</p>
	 *
	 * @param codec FFmpeg 的 AVCodec 对象，为 null 时忽略
	 * @since 1.1.0
	 */
	public void setCodec(AVCodec codec) {
		if (Objects.nonNull(codec)) {
			this.codecId = codec.id();
		}
	}

	/**
	 * 获取媒体格式
	 *
	 * @return 媒体格式
	 * @since 1.1.0
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * 设置媒体格式
	 * <p>
	 * 如果格式不在支持列表中，会通过 FFmpeg 验证其有效性。
	 * 空值会被忽略。
	 * </p>
	 *
	 * @param format 媒体格式
	 * @throws UnsupportedResourceException 当格式不被支持时抛出
	 * @since 1.1.0
	 */
	public void setFormat(String format) {
		if (StringUtils.isBlank(format)) {
			return;
		}

		String validFormat = FFmpegUtils.parseFormat(format);
		if (!FFmpegUtils.isSupportedMuxer(validFormat)) {
			throw new UnsupportedResourceException("不支持使用封装容器，名称：" + validFormat + " 输出");
		} else {
			this.format = validFormat;
		}
	}

	/**
	 * 获取媒体元数据
	 *
	 * @return 元数据键值对的副本
	 * @since 1.1.0
	 */
	public Map<String, String> getMetadata() {
		return metadata;
	}

	/**
	 * 设置媒体元数据
	 * <p>传入的元数据会直接用于设置媒体的元数据信息。</p>
	 * <p>null 值会被忽略。</p>
	 *
	 * @param metadata 元数据键值对
	 * @since 1.1.0
	 */
	public void setMetadata(Map<String, String> metadata) {
		if (Objects.nonNull(metadata)) {
			this.metadata = new HashMap<>(metadata);
		}
	}

	/**
	 * 获取编码器 ID
	 *
	 * @return 编码器 ID
	 * @since 1.1.0
	 */
	public int getCodecId() {
		return codecId;
	}

	/**
	 * 设置编码器 ID
	 * <p>
	 * 如果编码器 ID 不在支持列表中，会通过 FFmpeg 验证其有效性。
	 * 设置为 AV_CODEC_ID_NONE 时表示无编码器。
	 * </p>
	 *
	 * @param codecId 编码器 ID
	 * @throws UnsupportedResourceException 当编码器不被支持时抛出
	 * @since 1.1.0
	 */
	public void setCodecId(int codecId) {
		if (codecId == avcodec.AV_CODEC_ID_NONE) {
			this.codecId = avcodec.AV_CODEC_ID_NONE;
			return;
		}

		if (!FFmpegUtils.isSupportedEncoder(codecId)) {
			throw new UnsupportedResourceException("不支持使用编码器，ID：" + codecId + " 输出");
		} else {
			this.codecId = codecId;
		}
	}

	/**
	 * 获取是否剥离元数据
	 *
	 * @return true 表示剥离元数据，false 表示保留元数据
	 * @since 1.1.0
	 */
	public boolean isStripMetadata() {
		return stripMetadata;
	}

	/**
	 * 设置是否剥离元数据
	 *
	 * @param stripMetadata true 表示剥离元数据，false 表示保留元数据
	 * @since 1.1.0
	 */
	public void setStripMetadata(boolean stripMetadata) {
		this.stripMetadata = stripMetadata;
	}

	/**
	 * 配置 FFmpeg 帧录制器
	 * <p>
	 * 抽象方法，由子类实现具体的录制器配置逻辑。
	 * </p>
	 *
	 * @param recorder  FFmpeg 帧录制器
	 * @param frameType 帧类型
	 * @since 1.1.0
	 */
	public abstract void configure(FFmpegFrameRecorder recorder, FrameType frameType);
}
