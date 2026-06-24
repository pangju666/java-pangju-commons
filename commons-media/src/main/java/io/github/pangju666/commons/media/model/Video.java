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
import io.github.pangju666.commons.media.lang.MediaConstants;
import org.apache.commons.lang3.Validate;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
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
	public static final Audio AAC_480P = Audio.builder()
		.aac()
		.bitrate(96_000)
		.build();

	public static final Audio AAC_720P = Audio.builder()
		.aac().bitrate(128_000)
		.build();

	public static final Audio AAC_1080P = Audio.builder()
		.aac()
		.sampleRate(MediaConstants.VIDEO_STANDARD_SAMPLE_RATE)
		.bitrate(192_000)
		.build();

	public static final Audio AAC_2K = Audio.builder()
		.aac()
		.sampleRate(MediaConstants.VIDEO_STANDARD_SAMPLE_RATE)
		.bitrate(256_000)
		.build();

	public static final Audio OPUS_480P = Audio.builder()
		.opus()
		.bitrate(80_000)
		.build();

	public static final Audio OPUS_720P = Audio.builder()
		.opus()
		.bitrate(96_000)
		.build();

	public static final Audio OPUS_1080P = Audio.builder()
		.opus()
		.sampleRate(MediaConstants.VIDEO_STANDARD_SAMPLE_RATE)
		.bitrate(128_000)
		.build();

	public static final Audio OPUS_2K = Audio.builder()
		.opus()
		.sampleRate(MediaConstants.VIDEO_STANDARD_SAMPLE_RATE)
		.bitrate(192_000)
		.build();

	public static final Audio FLAC = Audio.builder()
		.flac()
		.sampleRate(MediaConstants.VIDEO_STANDARD_SAMPLE_RATE)
		.build();

	public static final Video MP4_480P = Video.builder()
		.mp4WithH264()
		.resolution480p()
		.bitrate(1500_000)
		.audio(AAC_480P)
		.build();

	public static final Video MP4_720P = Video.builder()
		.mp4WithH264()
		.resolution720p()
		.bitrate(3000_000)
		.audio(AAC_720P)
		.build();

	public static final Video MP4_1080P = Video.builder()
		.mp4WithH264()
		.resolution1080p()
		.bitrate(6000_000)
		.audio(AAC_1080P)
		.build();

	public static final Video MP4_2K = Video.builder()
		.mp4WithH264()
		.resolution2k()
		.bitrate(12000_000)
		.audio(AAC_2K)
		.build();

	public static final Video MP4_480P_VERTICAL = Video.builder()
		.mp4WithH264()
		.resolution480pVertical()
		.bitrate(1500_000)
		.audio(AAC_480P)
		.build();

	public static final Video MP4_720P_VERTICAL = Video.builder()
		.mp4WithH264()
		.resolution720pVertical()
		.bitrate(3000_000)
		.audio(AAC_720P)
		.build();

	public static final Video MP4_1080P_VERTICAL = Video.builder()
		.mp4WithH264()
		.resolution1080pVertical()
		.bitrate(6000_000)
		.audio(AAC_1080P)
		.build();

	public static final Video MP4_2K_VERTICAL = Video.builder()
		.mp4WithH264()
		.resolution2kVertical()
		.bitrate(12000_000)
		.audio(AAC_2K)
		.build();

	public static final Video MP4_480P_LOW = Video.builder()
		.mp4WithH264()
		.resolution480p()
		.bitrate(1000_000)
		.audio(AAC_480P)
		.build();

	public static final Video MP4_720P_LOW = Video.builder()
		.mp4WithH264()
		.resolution720p()
		.bitrate(2000_000)
		.audio(AAC_720P)
		.build();

	public static final Video MP4_1080P_LOW = Video.builder()
		.mp4WithH264()
		.resolution1080p()
		.bitrate(4000_000)
		.audio(AAC_1080P)
		.build();

	public static final Video MP4_2K_LOW = Video.builder()
		.mp4WithH264()
		.resolution2k()
		.bitrate(8000_000)
		.audio(AAC_2K)
		.build();

	public static final Video MP4_480P_LOW_VERTICAL = Video.builder()
		.mp4WithH264()
		.resolution480pVertical()
		.bitrate(1000_000)
		.audio(AAC_480P)
		.build();

	public static final Video MP4_720P_LOW_VERTICAL = Video.builder()
		.mp4WithH264()
		.resolution720pVertical()
		.bitrate(2000_000)
		.audio(AAC_720P)
		.build();

	public static final Video MP4_1080P_LOW_VERTICAL = Video.builder()
		.mp4WithH264()
		.resolution1080pVertical()
		.bitrate(4000_000)
		.audio(AAC_1080P)
		.build();

	public static final Video MP4_2K_LOW_VERTICAL = Video.builder()
		.mp4WithH264()
		.resolution2kVertical()
		.bitrate(8000_000)
		.audio(AAC_2K)
		.build();

	public static final Video MP4_480P_HIGH = Video.builder()
		.mp4WithH264()
		.resolution480p()
		.bitrate(2000_000)
		.audio(AAC_480P)
		.build();

	public static final Video MP4_720P_HIGH = Video.builder()
		.mp4WithH264()
		.resolution720p()
		.bitrate(4000_000)
		.audio(AAC_720P)
		.build();

	public static final Video MP4_1080P_HIGH = Video.builder()
		.mp4WithH264()
		.resolution1080p()
		.bitrate(8000_000)
		.audio(AAC_1080P)
		.build();

	public static final Video MP4_2K_HIGH = Video.builder()
		.mp4WithH264()
		.resolution2k()
		.bitrate(16000_000)
		.audio(AAC_2K)
		.build();

	public static final Video MP4_480P_HIGH_VERTICAL = Video.builder()
		.mp4WithH264()
		.resolution480pVertical()
		.bitrate(2000_000)
		.audio(AAC_480P)
		.build();

	public static final Video MP4_720P_HIGH_VERTICAL = Video.builder()
		.mp4WithH264()
		.resolution720pVertical()
		.bitrate(4000_000)
		.audio(AAC_720P)
		.build();

	public static final Video MP4_1080P_HIGH_VERTICAL = Video.builder()
		.mp4WithH264()
		.resolution1080pVertical()
		.bitrate(8000_000)
		.audio(AAC_1080P)
		.build();

	public static final Video MP4_2K_HIGH_VERTICAL = Video.builder()
		.mp4WithH264()
		.resolution2kVertical()
		.bitrate(16000_000)
		.audio(AAC_2K)
		.build();

	public static final Video WEBM_480P = Video.builder()
		.webmWithVP9()
		.resolution480p()
		.bitrate(1300_000)
		.audio(OPUS_480P)
		.build();

	public static final Video WEBM_720P = Video.builder()
		.webmWithVP9()
		.resolution720p()
		.bitrate(2600_000)
		.audio(OPUS_720P)
		.build();

	public static final Video WEBM_1080P = Video.builder()
		.webmWithVP9()
		.resolution1080p()
		.bitrate(5200_000)
		.audio(OPUS_1080P)
		.build();

	public static final Video WEBM_2K = Video.builder()
		.webmWithVP9()
		.resolution2k()
		.bitrate(10500_000)
		.audio(OPUS_2K)
		.build();

	public static final Video WEBM_480P_VERTICAL = Video.builder()
		.webmWithVP9()
		.resolution480pVertical()
		.bitrate(1300_000)
		.audio(OPUS_480P)
		.build();

	public static final Video WEBM_720P_VERTICAL = Video.builder()
		.webmWithVP9()
		.resolution720pVertical()
		.bitrate(2600_000)
		.audio(OPUS_720P)
		.build();

	public static final Video WEBM_1080P_VERTICAL = Video.builder()
		.webmWithVP9()
		.resolution1080pVertical()
		.bitrate(5200_000)
		.audio(OPUS_1080P)
		.build();

	public static final Video WEBM_2K_VERTICAL = Video.builder()
		.webmWithVP9()
		.resolution2kVertical()
		.bitrate(10500_000)
		.audio(OPUS_2K)
		.build();

	public static final Video WEBM_480P_LOW = Video.builder()
		.webmWithVP9()
		.resolution480p()
		.bitrate(750_000)
		.audio(OPUS_480P)
		.build();

	public static final Video WEBM_720P_LOW = Video.builder()
		.webmWithVP9()
		.resolution720p()
		.bitrate(1500_000)
		.audio(OPUS_720P)
		.build();

	public static final Video WEBM_1080P_LOW = Video.builder()
		.webmWithVP9()
		.resolution1080p()
		.bitrate(3000_000)
		.audio(OPUS_1080P)
		.build();

	public static final Video WEBM_2K_LOW = Video.builder()
		.webmWithVP9()
		.resolution2k()
		.bitrate(6000_000)
		.audio(OPUS_2K)
		.build();

	public static final Video WEBM_480P_LOW_VERTICAL = Video.builder()
		.webmWithVP9()
		.resolution480pVertical()
		.bitrate(750_000)
		.audio(OPUS_480P)
		.build();

	public static final Video WEBM_720P_LOW_VERTICAL = Video.builder()
		.webmWithVP9()
		.resolution720pVertical()
		.bitrate(1500_000)
		.audio(OPUS_720P)
		.build();

	public static final Video WEBM_1080P_LOW_VERTICAL = Video.builder()
		.webmWithVP9()
		.resolution1080pVertical()
		.bitrate(3000_000)
		.audio(OPUS_1080P)
		.build();

	public static final Video WEBM_2K_LOW_VERTICAL = Video.builder()
		.webmWithVP9()
		.resolution2kVertical()
		.bitrate(6000_000)
		.audio(OPUS_2K)
		.build();

	public static final Video WEBM_480P_HIGH = Video.builder()
		.webmWithVP9()
		.resolution480p()
		.bitrate(1800_000)
		.audio(OPUS_480P)
		.build();

	public static final Video WEBM_720P_HIGH = Video.builder()
		.webmWithVP9()
		.resolution720p()
		.bitrate(3500_000)
		.audio(OPUS_720P)
		.build();

	public static final Video WEBM_1080P_HIGH = Video.builder()
		.webmWithVP9()
		.resolution1080p()
		.bitrate(7000_000)
		.audio(OPUS_1080P)
		.build();

	public static final Video WEBM_2K_HIGH = Video.builder()
		.webmWithVP9()
		.resolution2k()
		.bitrate(14000_000)
		.audio(OPUS_2K)
		.build();

	public static final Video WEBM_480P_HIGH_VERTICAL = Video.builder()
		.webmWithVP9()
		.resolution480pVertical()
		.bitrate(1800_000)
		.audio(OPUS_480P)
		.build();

	public static final Video WEBM_720P_HIGH_VERTICAL = Video.builder()
		.webmWithVP9()
		.resolution720pVertical()
		.bitrate(3500_000)
		.audio(OPUS_720P)
		.build();

	public static final Video WEBM_1080P_HIGH_VERTICAL = Video.builder()
		.webmWithVP9()
		.resolution1080pVertical()
		.bitrate(7000_000)
		.audio(OPUS_1080P)
		.build();

	public static final Video WEBM_2K_HIGH_VERTICAL = Video.builder()
		.webmWithVP9()
		.resolution2kVertical()
		.bitrate(14000_000)
		.audio(OPUS_2K)
		.build();

	public static final Video MKV_480P = Video.builder()
		.mkvWithH264()
		.resolution480p()
		.bitrate(900_000)
		.audio(AAC_480P)
		.build();

	public static final Video MKV_720P = Video.builder()
		.mkvWithH264()
		.resolution720p()
		.bitrate(1800_000)
		.audio(Audio.FLAC)
		.build();

	public static final Video MKV_1080P = Video.builder()
		.mkvWithH264()
		.resolution1080p()
		.bitrate(3600_000)
		.audio(Audio.FLAC)
		.build();

	public static final Video MKV_2K = Video.builder()
		.mkvWithH264()
		.resolution2k()
		.bitrate(7200_000)
		.audio(AAC_2K)
		.build();

	public static final Video MKV_480P_VERTICAL = Video.builder()
		.mkvWithH264()
		.resolution480pVertical()
		.bitrate(900_000)
		.audio(Audio.FLAC)
		.build();

	public static final Video MKV_720P_VERTICAL = Video.builder()
		.mkvWithH264()
		.resolution720pVertical()
		.bitrate(1800_000)
		.audio(Audio.FLAC)
		.build();

	public static final Video MKV_1080P_VERTICAL = Video.builder()
		.mkvWithH264()
		.resolution1080pVertical()
		.bitrate(3600_000)
		.audio(FLAC)
		.build();

	public static final Video MKV_2K_VERTICAL = Video.builder()
		.mkvWithH264()
		.resolution2kVertical()
		.bitrate(7200_000)
		.audio(FLAC)
		.build();

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

	public static Video.Builder builder(FFmpegFrameGrabber grabber) throws FFmpegFrameGrabber.Exception {
		return new Video.Builder().parse(grabber);
	}

	public static Video.Builder builder(File file) throws IOException {
		FileUtils.checkFile(file, "file 不可为 null");

		try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file)) {
			grabber.start();
			return new Video.Builder().parse(grabber);
		}
	}

	public static Video.Builder builder(byte[] bytes) throws IOException {
		Validate.notNull(bytes, "bytes 不可为 null");

		try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(IOUtils.toUnsynchronizedByteArrayInputStream(bytes))) {
			grabber.start();
			return new Video.Builder().parse(grabber);
		}
	}

	public static Video.Builder builder(InputStream inputStream) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputStream)) {
			grabber.start();
			return new Video.Builder().parse(grabber);
		}
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

	public boolean isVertical() {
		return height > width;
	}

	public boolean isSquare() {
		return width == height;
	}

	@Override
	public void initRecoder(FFmpegFrameRecorder recorder) {
		super.initRecoder(recorder);

		recorder.setFrameRate(this.frameRate);
		recorder.setVideoBitrate(this.bitrate);
		recorder.setVideoCodec(this.codecId);
		recorder.setVideoCodecName(this.codecName);
		recorder.setImageWidth(this.width);
		recorder.setImageHeight(this.height);
		recorder.setVideoMetadata(new HashMap<>(this.metadata));

		if (Objects.nonNull(this.audio)) {
			recorder.setSampleRate(this.audio.sampleRate);
			recorder.setAudioCodec(this.audio.codecId);
			recorder.setAudioCodecName(this.audio.codecName);
			recorder.setAudioBitrate(this.audio.bitrate);
			recorder.setAudioChannels(this.audio.channels);
			recorder.setAudioMetadata(new HashMap<>(this.audio.metadata));
		}
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
			this.duration = Duration.ZERO;
			this.frameRate = MediaConstants.DEFAULT_VIDEO_FRAME_RATE;
			this.width = 0;
			this.height = 0;
			this.bitrate = 0;
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

		public Builder mp4WithH264() {
			this.format = MediaConstants.VIDEO_MP4_FORMAT;
			this.codecId = avcodec.AV_CODEC_ID_H264;
			return this;
		}

		public Builder webmWithVP9() {
			this.format = MediaConstants.VIDEO_WEBM_FORMAT;
			this.codecId = avcodec.AV_CODEC_ID_VP9;
			return this;
		}

		public Builder mkvWithH264() {
			this.format = MediaConstants.VIDEO_MKV_FORMAT;
			this.codecId = avcodec.AV_CODEC_ID_H264;
			return this;
		}

		public Builder resolution2k() {
			return resolution(2560, 1440);
		}

		public Builder resolution1080p() {
			return resolution(1920, 1080);
		}

		public Builder resolution720p() {
			return resolution(1280, 720);
		}

		public Builder resolution480p() {
			return resolution(640, 480);
		}

		public Builder resolution2kVertical() {
			return resolution(1440, 2560);
		}

		public Builder resolution1080pVertical() {
			return resolution(1080, 1920);
		}

		public Builder resolution720pVertical() {
			return resolution(720, 1280);
		}

		public Builder resolution480pVertical() {
			return resolution(480, 640);
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
		 * 设置视频时长
		 *
		 * @param duration 视频时长，不可为 null 且必须为正数
		 * @return 构建器自身，用于链式调用
		 * @throws IllegalArgumentException 当 duration 为 null 或为负数时抛出
		 * @since 1.1.0
		 */
		public Builder duration(Duration duration) {
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
		 * 设置视频码率
		 *
		 * @param bitrate 码率（bps），<b>必须大于 0</b>
		 * @return 当前构建器，支持链式调用
		 * @throws IllegalArgumentException 码率小于等于 0 时抛出
		 * @since 1.1.0
		 */
		public Builder bitrate(int bitrate) {
			Validate.isTrue(bitrate >= 0, "bitrate 必须为非负数");
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
				this.metadata = Collections.unmodifiableMap(grabber.getVideoMetadata());
				this.codecName = grabber.getVideoCodecName();
				this.codecId = grabber.getVideoCodec();
				// 微秒 → 纳秒：1 微秒 = 1000 纳秒
				this.duration = Duration.ofNanos(grabber.getLengthInTime() * 1000);
				this.frameRate = grabber.getVideoFrameRate();
				this.bitrate = grabber.getVideoBitrate();
				this.width = grabber.getImageWidth();
				this.height = grabber.getImageHeight();
			}
			if (grabber.hasAudio()) {
				this.audio = Audio.builder(grabber).build();
			}

			return this;
		}
	}
}
