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

import org.apache.commons.lang3.Validate;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * 视频媒体模型
 * <p>继承自 {@link Media} 媒体基类，专门封装视频专属属性：时长、帧率、分辨率、码率、音频信息；
 * 基于 JavaCV + FFmpeg 实现媒体信息解析，内置链式 Builder 构建器，
 * 统一视频对象创建、属性拷贝、媒体解析能力。</p>
 * <h3>核心特性</h3>
 * <ul>
 *   <li>支持从多种来源解析视频信息（File、byte[]、InputStream、FFmpegFrameGrabber）</li>
 *   <li>封装视频特有属性：分辨率、帧率、码率、时长</li>
 *   <li>内置音频轨道信息，支持音视频一体化处理</li>
 *   <li>不可变对象设计，线程安全</li>
 *   <li>提供 Fluent Builder 模式，支持链式调用</li>
 * </ul>
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 从文件解析视频
 * Video video = Video.builder()
 *     .parse(new File("video.mp4"))
 *     .build();
 *
 * // 手动构建视频
 * Video customVideo = Video.builder()
 *     .format("mp4")
 *     .resolution(1920, 1080)
 *     .frameRate(30)
 *     .bitrate(2000000)
 *     .build();
 *
 * // 基于现有视频修改
 * Video modifiedVideo = Video.builder(existingVideo)
 *     .bitrate(4000000)
 *     .build();
 * }</pre>
 *
 * @author pangju666
 * @see Media
 * @see Audio
 * @see FFmpegFrameGrabber
 * @since 1.1.0
 */
public class Video extends Media {
	/**
	 * 默认视频封装格式：AVI
	 * <p>通用容器格式，广泛用于视频编辑、转码中间载体</p>
	 *
	 * @since 1.1.0
	 */
	public static final String DEFAULT_FORMAT = "avi";

	/**
	 * 默认视频帧率：30 fps
	 * <p>标准视频帧率，适合大多数场景</p>
	 *
	 * @since 1.1.0
	 */
	public static final int DEFAULT_FRAME_RATE = 30;

	/**
	 * 默认视频码率：400000 bps（400kbps）
	 * <p>适合低清晰度视频；高清视频建议使用 2000000bps 及以上</p>
	 *
	 * @since 1.1.0
	 */
	public static final int DEFAULT_BITRATE = 400000;

	/**
	 * 视频总播放时长
	 * <p>null / {@link Duration#ZERO} 代表无有效时长（如实时流、直播流）</p>
	 *
	 * @since 1.1.0
	 */
	protected Duration duration;

	/**
	 * 视频帧率，单位 fps（帧/秒）
	 * <p>常见取值：24、25、30、60，数值越高画面越流畅、体积越大</p>
	 *
	 * @since 1.1.0
	 */
	protected double frameRate;

	/**
	 * 视频宽度，单位像素
	 * <p>常见分辨率：1920（1080p）、1280（720p）、3840（4K）</p>
	 *
	 * @since 1.1.0
	 */
	protected int width;

	/**
	 * 视频高度，单位像素
	 * <p>常见分辨率：1080（1080p）、720（720p）、2160（4K）</p>
	 *
	 * @since 1.1.0
	 */
	protected int height;

	/**
	 * 视频码率，单位 bps
	 * <p>码率越高画质越好、体积越大，需根据分辨率和帧率合理设置</p>
	 *
	 * @since 1.1.0
	 */
	protected int bitrate;

	/**
	 * 视频中的音频信息
	 * <p>null 表示视频无音频轨道</p>
	 *
	 * @since 1.1.0
	 */
	protected Audio audio;

	/**
	 * 私有构造函数，仅通过 Builder 调用
	 *
	 * @param format    视频容器格式
	 * @param codecName 视频编码名称
	 * @param metadata  元数据映射
	 * @param codecId   视频编码ID
	 * @param duration  视频时长
	 * @param frameRate 视频帧率
	 * @param width     视频宽度
	 * @param height    视频高度
	 * @param bitrate   视频码率
	 * @param audio     音频信息
	 * @since 1.1.0
	 */
	protected Video(String format, String codecName, Map<String, String> metadata, int codecId, Duration duration,
	                double frameRate, int width, int height, int bitrate, Audio audio) {
		super(format, codecName, metadata, codecId);
		this.duration = duration;
		this.frameRate = frameRate;
		this.width = width;
		this.height = height;
		this.bitrate = bitrate;
		this.audio = audio;
	}

	/**
	 * 创建视频构建器，使用全局默认参数初始化对象
	 *
	 * @return 视频链式构建器实例
	 * @since 1.1.0
	 */
	public static Video.Builder builder() {
		return new Video.Builder();
	}

	/**
	 * 基于已有视频对象，复制全部属性并创建新构建器
	 *
	 * @param video 源视频对象，<b>不可为 null</b>
	 * @return 视频链式构建器实例
	 * @throws IllegalArgumentException 入参为 null 时抛出
	 * @since 1.1.0
	 */
	public static Video.Builder builder(Video video) {
		return new Video.Builder(video);
	}

	/**
	 * 获取视频总时长
	 *
	 * @return 时长对象；null 表示未解析到时长
	 * @since 1.1.0
	 */
	public Duration getDuration() {
		return duration;
	}

	/**
	 * 获取视频帧率
	 *
	 * @return 帧率（fps）
	 * @since 1.1.0
	 */
	public double getFrameRate() {
		return frameRate;
	}

	/**
	 * 获取视频宽度
	 *
	 * @return 宽度（像素）
	 * @since 1.1.0
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * 获取视频高度
	 *
	 * @return 高度（像素）
	 * @since 1.1.0
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * 获取视频码率
	 *
	 * @return 码率（bps）
	 * @since 1.1.0
	 */
	public int getBitrate() {
		return bitrate;
	}

	/**
	 * 获取视频中的音频信息
	 *
	 * @return 音频对象；null 表示无音频轨道
	 * @since 1.1.0
	 */
	public Audio getAudio() {
		return audio;
	}

	/**
	 * 获取视频总秒数
	 *
	 * @return 总时长（秒）；无有效时长返回 0
	 * @since 1.1.0
	 */
	public long getTotalSeconds() {
		if (Objects.isNull(duration)) {
			return 0;
		}
		return duration.toSeconds();
	}

	/**
	 * 判断视频是否包含音频轨道
	 *
	 * @return true = 有音频，false = 无音频
	 * @since 1.1.0
	 */
	public boolean hasAudio() {
		return Objects.nonNull(audio);
	}

	/**
	 * 判断是否存在有效播放时长
	 *
	 * @return true = 非空且时长不为 0；false = 无有效时长
	 * @since 1.1.0
	 */
	public boolean hasDuration() {
		return duration != null && !duration.isZero();
	}

	/**
	 * 视频链式构建器
	 * <p>继承媒体通用构建器，扩展视频专属属性配置、媒体解析能力；
	 * 支持默认初始化、对象拷贝、从 FFmpeg 抓取器自动解析视频信息。</p>
	 * <h3>主要功能</h3>
	 * <ul>
	 *   <li>设置视频分辨率：{@link #resolution(int, int)}</li>
	 *   <li>设置视频帧率：{@link #frameRate(double)}</li>
	 *   <li>设置视频码率：{@link #bitrate(int)}</li>
	 *   <li>设置视频时长：{@link #duration(Duration)}</li>
	 *   <li>设置音频信息：{@link #audio(Audio)}</li>
	 *   <li>解析媒体信息：{@link #parse(FFmpegFrameGrabber)}</li>
	 * </ul>
	 * <h3>使用示例</h3>
	 * <pre>{@code
	 * // 方式1：从文件解析
	 * Video video = Video.builder()
	 *     .parse(new File("video.mp4"))
	 *     .build();
	 *
	 * // 方式2：手动构建
	 * Video custom = Video.builder()
	 *     .format("mp4")
	 *     .resolution(1920, 1080)
	 *     .frameRate(30)
	 *     .bitrate(2000000)
	 *     .build();
	 * }</pre>
	 *
	 * @author pangju666
	 * @since 1.1.0
	 */
	public static class Builder extends Media.Builder<Video.Builder, Video> {
		/**
		 * 视频总播放时长
		 * <p>null / {@link Duration#ZERO} 代表无有效时长（如实时流、直播流）</p>
		 *
		 * @since 1.1.0
		 */
		protected Duration duration;

		/**
		 * 视频帧率，单位 fps（帧/秒）
		 * <p>常见取值：24、25、30、60，数值越高画面越流畅、体积越大</p>
		 *
		 * @since 1.1.0
		 */
		protected double frameRate;

		/**
		 * 视频宽度，单位像素
		 * <p>常见分辨率：1920（1080p）、1280（720p）、3840（4K）</p>
		 *
		 * @since 1.1.0
		 */
		protected int width;

		/**
		 * 视频高度，单位像素
		 * <p>常见分辨率：1080（1080p）、720（720p）、2160（4K）</p>
		 *
		 * @since 1.1.0
		 */
		protected int height;

		/**
		 * 视频码率，单位 bps
		 * <p>码率越高画质越好、体积越大，需根据分辨率和帧率合理设置</p>
		 *
		 * @since 1.1.0
		 */
		protected int bitrate;

		/**
		 * 视频中的音频信息
		 * <p>null 表示视频无音频轨道</p>
		 *
		 * @since 1.1.0
		 */
		protected Audio audio;

		/**
		 * 默认构造函数，使用默认参数初始化
		 *
		 * @since 1.1.0
		 */
		public Builder() {
			super();
		}

		/**
		 * 复制构造函数，基于现有视频对象创建
		 *
		 * @param video 源视频对象，不可为 null
		 * @since 1.1.0
		 */
		public Builder(Video video) {
			super(video);
			this.duration = Duration.ofNanos(video.getDuration().toNanos());
			this.frameRate = video.frameRate;
			this.width = video.width;
			this.height = video.height;
			this.bitrate = video.bitrate;
			this.audio = video.audio;
		}

		/**
		 * 设置视频时长
		 *
		 * @param duration 视频时长，不可为 null 且必须为正数
		 * @return 构建器自身，用于链式调用
		 * @throws IllegalArgumentException 当 duration 为 null 或为负数时抛出
		 * @since 1.1.0
		 */
		public Video.Builder duration(Duration duration) {
			Validate.notNull(duration, "duration 不可为 null");
			Validate.isTrue(!duration.isNegative() && !duration.isZero(), "duration 必须为正数");

			this.duration = duration;
			return this;
		}

		/**
		 * 设置视频帧率
		 *
		 * @param frameRate 帧率（fps），<b>必须大于 0</b>
		 * @return 当前构建器，支持链式调用
		 * @throws IllegalArgumentException 帧率小于等于 0 时抛出
		 * @since 1.1.0
		 */
		public Builder frameRate(double frameRate) {
			Validate.isTrue(frameRate > 0, "frameRate 必须大于 0");
			this.frameRate = frameRate;
			return this;
		}

		/**
		 * 设置视频分辨率
		 *
		 * @param width  视频宽度，必须大于 0
		 * @param height 视频高度，必须大于 0
		 * @return 当前构建器，支持链式调用
		 * @throws IllegalArgumentException 当 width 或 height 小于等于 0 时抛出
		 * @since 1.1.0
		 */
		public Builder resolution(int width, int height) {
			Validate.isTrue(width > 0, "width 必须大于 0");
			Validate.isTrue(height > 0, "height 必须大于 0");
			this.height = height;
			this.width = width;
			return this;
		}

		/**
		 * 设置视频码率
		 *
		 * @param bitrate 码率（bps），<b>必须大于 0</b>
		 * @return 当前构建器，支持链式调用
		 * @throws IllegalArgumentException 码率小于等于 0 时抛出
		 * @since 1.1.0
		 */
		public Builder bitrate(int bitrate) {
			Validate.isTrue(bitrate > 0, "bitrate 必须大于 0");
			this.bitrate = bitrate;
			return this;
		}

		/**
		 * 设置视频中的音频信息
		 *
		 * @param audio 音频对象，不可为 null
		 * @return 当前构建器，支持链式调用
		 * @throws IllegalArgumentException 当 audio 为 null 时抛出
		 * @since 1.1.0
		 */
		public Builder audio(Audio audio) {
			Validate.notNull(audio, "audio 不可为 null");
			this.audio = Audio.builder(audio).build();
			return this;
		}

		/**
		 * 构建最终的 Video 对象
		 *
		 * @return 构建完成的 Video 对象
		 * @since 1.1.0
		 */
		@Override
		public Video build() {
			return new Video(this.format, this.codecName, this.metadata, this.codecId, this.duration, this.frameRate,
				this.width, this.height, this.bitrate, this.audio);
		}

		/**
		 * 从 FFmpeg 帧抓取器解析视频信息
		 * <p>将解析视频时长、帧率、分辨率、码率，以及可能存在的音频轨道信息</p>
		 *
		 * @param grabber FFmpeg 帧抓取器，不可为 null
		 * @return 构建器自身，用于链式调用
		 * @throws IllegalArgumentException 当 grabber 为 null 时抛出
		 * @since 1.1.0
		 */
		@Override
		public Video.Builder parse(FFmpegFrameGrabber grabber) throws FFmpegFrameGrabber.Exception {
			Validate.notNull(grabber, "grabber 不可为 null");

			super.parse(grabber);
			if (grabber.hasVideo()) {
				// 微秒 → 纳秒：1 微秒 = 1000 纳秒
				this.duration = Duration.ofNanos(grabber.getLengthInTime() * 1000);
				this.frameRate = grabber.getVideoFrameRate();
				this.width = grabber.getImageWidth();
				this.height = grabber.getImageHeight();
				this.frameRate = grabber.getVideoFrameRate();
				this.bitrate = grabber.getVideoBitrate();
			}
			if (grabber.hasAudio()) {
				this.audio = Audio.builder().parse(grabber).build();
			}

			return this;
		}

		/**
		 * 重置为全局默认视频参数
		 * <p>使用 Video 类中定义的默认值（AVI 格式、30fps、400kbps 等）</p>
		 *
		 * @since 1.1.0
		 */
		@Override
		protected void initDefaultValue() {
			super.initDefaultValue();
			this.format = DEFAULT_FORMAT;
			this.width = 0;
			this.height = 0;
			this.frameRate = DEFAULT_FRAME_RATE;
			this.bitrate = DEFAULT_BITRATE;
			this.duration = Duration.ZERO;
		}
	}
}
