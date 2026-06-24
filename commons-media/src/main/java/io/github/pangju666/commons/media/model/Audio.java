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
import java.util.Map;

/**
 * 音频媒体对象，封装了音频文件的核心信息
 * <p>
 * 该类继承自 {@link Media}，专门用于表示音频文件，包含音频特有的属性如采样率、声道数、比特率等。
 * 采用不可变对象设计，所有属性为 final，确保线程安全。
 * </p>
 * <h3>核心特性</h3>
 * <ul>
 *     <li>支持解析自多种来源（File/byte[]/InputStream/FFmpegFrameGrabber）</li>
 *     <li>不可变对象设计，线程安全</li>
 *     <li>提供 Fluent Builder 模式，支持链式调用</li>
 *     <li>内置音频格式判断（单声道/立体声等）</li>
 *     <li>提供默认值，便于快速创建标准音频配置</li>
 * </ul>
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 从文件解析音频
 * Audio audio = Audio.builder()
 *     .parse(new File("music.wav"))
 *     .build();
 *
 * // 手动构建标准音频
 * Audio standardAudio = Audio.builder()
 *     .format("mp3")
 *     .stereo()
 *     .sampleRate(44100)
 *     .bitrate(128000)
 *     .build();
 *
 * // 基于现有音频修改
 * Audio modifiedAudio = Audio.builder(existingAudio)
 *     .bitrate(320000)
 *     .build();
 * }</pre>
 *
 * @author pangju666
 * @see Media
 * @see Audio.Builder
 * @since 1.1.0
 */
public class Audio extends Media {
	public static final Audio WAV = Audio.builder()
		.wav()
		.build();

	public static final Audio FLAC = Audio.builder()
		.flac()
		.build();

	public static final Audio MP3 = Audio.builder()
		.mp3()
		.bitrate(128_000)
		.build();

	public static final Audio MP3_HIGH = Audio.builder()
		.mp3()
		.bitrate(192_000)
		.build();

	public static final Audio OPUS = Audio.builder()
		.opus()
		.bitrate(96_000)
		.build();

	public static final Audio OPUS_HIGH = Audio.builder()
		.opus()
		.bitrate(192_000)
		.build();

	public static final Audio AAC = Audio.builder()
		.aac()
		.bitrate(128_000)
		.build();

	public static final Audio AAC_HIGH = Audio.builder()
		.aac()
		.bitrate(256_000)
		.build();

	/**
	 * 音频时长
	 *
	 * @since 1.1.0
	 */
	protected final Duration duration;

	/**
	 * 采样率（Hz），如 44100、48000 等
	 *
	 * @since 1.1.0
	 */
	protected final int sampleRate;

	/**
	 * 声道数，1 为单声道，2 为立体声，大于 2 为多声道
	 *
	 * @since 1.1.0
	 */
	protected final int channels;

	/**
	 * 比特率（bps），表示音频数据传输速率
	 *
	 * @since 1.1.0
	 */
	protected final int bitrate;

	/**
	 * 受保护的构造函数，仅用于 Builder 内部实例化
	 *
	 * @param format     音频格式
	 * @param codecName  编码器名称
	 * @param metadata   音频元数据
	 * @param codecId    编码器ID
	 * @param duration   音频时长
	 * @param sampleRate 采样率
	 * @param channels   声道数
	 * @param bitrate    比特率
	 * @since 1.1.0
	 */
	protected Audio(String format, String codecName, Map<String, String> metadata, int codecId, Duration duration,
	                int sampleRate, int channels, int bitrate) {
		super(format, codecName, metadata, codecId);
		this.duration = duration;
		this.sampleRate = sampleRate;
		this.channels = channels;
		this.bitrate = bitrate;
	}

	public static Audio.Builder builder() {
		return new Audio.Builder();
	}

	/**
	 * 基于现有音频对象创建构建器
	 * <p>新构建器会复制现有音频的所有属性，方便进行修改</p>
	 *
	 * @param audio 源音频对象，不可为 null
	 * @return 基于现有音频的 Audio.Builder 实例
	 * @throws IllegalArgumentException 当 audio 为 null 时抛出
	 * @since 1.1.0
	 */
	public static Audio.Builder builder(Audio audio) {
		return new Audio.Builder(audio);
	}

	public static Audio.Builder builder(FFmpegFrameGrabber grabber) throws FFmpegFrameGrabber.Exception {
		return new Audio.Builder().parse(grabber);
	}

	public static Audio.Builder builder(File file) throws IOException {
		FileUtils.checkFile(file, "file 不可为 null");

		try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file)) {
			grabber.start();
			return new Audio.Builder().parse(grabber);
		}
	}

	public static Audio.Builder builder(byte[] bytes) throws IOException {
		Validate.notNull(bytes, "bytes 不可为 null");

		try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(IOUtils.toUnsynchronizedByteArrayInputStream(bytes))) {
			grabber.start();
			return new Audio.Builder().parse(grabber);
		}
	}

	public static Audio.Builder builder(InputStream inputStream) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputStream)) {
			grabber.start();
			return new Audio.Builder().parse(grabber);
		}
	}

	/**
	 * 获取音频时长
	 *
	 * @return 音频时长，可能为 null
	 * @since 1.1.0
	 */
	public Duration getDuration() {
		return duration;
	}

	/**
	 * 获取采样率（Hz）
	 *
	 * @return 采样率，如 44100、48000
	 * @since 1.1.0
	 */
	public int getSampleRate() {
		return sampleRate;
	}

	/**
	 * 获取声道数
	 *
	 * @return 声道数，1 为单声道，2 为立体声
	 * @since 1.1.0
	 */
	public int getChannels() {
		return channels;
	}

	/**
	 * 获取比特率（bps）
	 *
	 * @return 比特率，如 64000（64kbps）
	 * @since 1.1.0
	 */
	public int getBitrate() {
		return bitrate;
	}

	/**
	 * 判断是否为单声道音频
	 *
	 * @return true 表示单声道，false 表示非单声道
	 * @since 1.1.0
	 */
	public boolean isMono() {
		return this.channels == 1;
	}

	/**
	 * 判断是否为立体声音频
	 *
	 * @return true 表示立体声，false 表示非立体声
	 * @since 1.1.0
	 */
	public boolean isStereo() {
		return this.channels == 2;
	}

	/**
	 * 判断是否有时长信息
	 * <p>检查 duration 是否不为 null 且不为零时长</p>
	 *
	 * @return true 表示有有效时长，false 表示无有效时长
	 * @since 1.1.0
	 */
	public boolean hasDuration() {
		return duration != null && !duration.isZero();
	}

	@Override
	public void initRecoder(FFmpegFrameRecorder recorder) {
		super.initRecoder(recorder);

		recorder.setSampleRate(this.sampleRate);
		recorder.setAudioCodec(this.codecId);
		recorder.setAudioCodecName(this.codecName);
		recorder.setAudioBitrate(this.bitrate);
		recorder.setAudioChannels(this.channels);
		recorder.setAudioMetadata(this.metadata);
	}

	/**
	 * Audio 对象的构建器，提供 Fluent API 链式调用
	 * <p>
	 * 继承自 {@link Media.Builder}，增加了音频特有的属性设置方法。
	 * </p>
	 * <h3>使用方式</h3>
	 * <ul>
	 *     <li><b>新建音频对象</b>：使用 {@link #Builder()} 创建空白构建器，设置属性后调用 {@link #build()}</li>
	 *     <li><b>修改现有音频</b>：使用 {@link #Builder(Audio)} 基于现有音频创建构建器，修改属性后调用 {@link #build()}</li>
	 * </ul>
	 * <h3>主要方法</h3>
	 * <ul>
	 *     <li>{@link #mono()} / {@link #stereo()}：快速设置声道数</li>
	 *     <li>{@link #sampleRate(int)}：设置采样率</li>
	 *     <li>{@link #bitrate(int)}：设置比特率</li>
	 *     <li>{@link #duration(Duration)}：设置时长</li>
	 *     <li>{@link #channels(int)}：设置声道数</li>
	 * </ul>
	 * <h3>使用示例</h3>
	 * <pre>{@code
	 * // 新建标准音频
	 * Audio audio = new Audio.Builder()
	 *     .format("mp3")
	 *     .stereo()
	 *     .sampleRate(44100)
	 *     .bitrate(128000)
	 *     .build();
	 *
	 * // 从文件解析
	 * Audio parsed = new Audio.Builder()
	 *     .parse(new File("music.wav"))
	 *     .build();
	 *
	 * // 修改现有音频
	 * Audio modified = new Audio.Builder(existingAudio)
	 *     .bitrate(320000)
	 *     .build();
	 * }</pre>
	 *
	 * @author pangju666
	 * @see Media.Builder
	 * @see Audio
	 * @since 1.1.0
	 */
	public static class Builder extends Media.Builder<Audio.Builder, Audio> {
		/**
		 * 音频时长
		 *
		 * @since 1.1.0
		 */
		protected Duration duration;

		/**
		 * 采样率（Hz）
		 *
		 * @since 1.1.0
		 */
		protected int sampleRate;

		/**
		 * 声道数
		 *
		 * @since 1.1.0
		 */
		protected int channels;

		/**
		 * 比特率（bps）
		 *
		 * @since 1.1.0
		 */
		protected int bitrate;

		public Builder() {
			super();
			this.duration = Duration.ZERO;
			this.sampleRate = MediaConstants.AUDIO_STANDARD_SAMPLE_RATE;
			this.channels = MediaConstants.DEFAULT_AUDIO_CHANNELS;
			this.bitrate = 0;
		}

		/**
		 * 基于现有音频对象创建构建器
		 * <p>新构建器会复制现有音频的所有属性，方便进行修改
		 *
		 * @param audio 源音频对象，不可为 null
		 * @throws IllegalArgumentException 当 audio 为 null 时抛出
		 * @since 1.1.0
		 */
		public Builder(Audio audio) {
			super(audio);
			this.duration = Duration.ofNanos(audio.getDuration().toNanos());
			this.sampleRate = audio.sampleRate;
			this.channels = audio.channels;
			this.bitrate = audio.bitrate;
		}

		public Builder wav() {
			this.format = MediaConstants.AUDIO_WAV_FORMAT;
			this.codecId = avcodec.AV_CODEC_ID_PCM_S16LE;
			return this;
		}

		public Builder flac() {
			this.format = MediaConstants.AUDIO_FLAC_FORMAT;
			this.codecId = avcodec.AV_CODEC_ID_FLAC;
			return this;
		}

		public Builder mp3() {
			this.format = MediaConstants.AUDIO_MP3_FORMAT;
			this.codecId = avcodec.AV_CODEC_ID_MP3;
			return this;
		}

		public Builder opus() {
			this.format = MediaConstants.AUDIO_OPUS_FORMAT;
			this.codecId = avcodec.AV_CODEC_ID_OPUS;
			return this;
		}

		public Builder aac() {
			this.format = MediaConstants.AUDIO_AAC_FORMAT;
			this.codecId = avcodec.AV_CODEC_ID_AAC;
			return this;
		}

		/**
		 * 设置为单声道
		 * <p>快捷方法，等效于调用 channels(1)</p>
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public Builder mono() {
			return channels(1);
		}

		/**
		 * 设置为立体声
		 * <p>快捷方法，等效于调用 channels(2)</p>
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public Builder stereo() {
			return channels(2);
		}

		/**
		 * 设置音频时长
		 *
		 * @param duration 音频时长，不可为 null 且必须为正数
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
		 * 设置采样率
		 *
		 * @param sampleRate 采样率（Hz），必须大于 0
		 * @return 构建器自身，用于链式调用
		 * @throws IllegalArgumentException 当 sampleRate 小于等于 0 时抛出
		 * @since 1.1.0
		 */
		public Builder sampleRate(int sampleRate) {
			Validate.isTrue(sampleRate > 0, "sampleRate 必须大于 0");
			this.sampleRate = sampleRate;
			return this;
		}

		/**
		 * 设置声道数
		 *
		 * @param channels 声道数，必须大于 0
		 * @return 构建器自身，用于链式调用
		 * @throws IllegalArgumentException 当 channels 小于等于 0 时抛出
		 * @since 1.1.0
		 */
		public Builder channels(int channels) {
			Validate.isTrue(channels > 0, "channels 必须大于 0");
			this.channels = channels;
			return this;
		}

		/**
		 * 设置比特率
		 *
		 * @param bitrate 比特率（bps），必须大于 0
		 * @return 构建器自身，用于链式调用
		 * @throws IllegalArgumentException 当 bitrate 小于等于 0 时抛出
		 * @since 1.1.0
		 */
		public Builder bitrate(int bitrate) {
			Validate.isTrue(bitrate >= 0, "bitrate 必须为非负数");
			this.bitrate = bitrate;
			return this;
		}

		/**
		 * 构建最终的 Audio 对象
		 *
		 * @return 构建完成的 Audio 对象
		 * @since 1.1.0
		 */
		@Override
		public Audio build() {
			return new Audio(this.format, this.codecName, this.metadata, this.codecId, this.duration, this.sampleRate,
				this.channels, this.bitrate);
		}

		/**
		 * 从 FFmpegFrameGrabber 解析音频信息
		 * <p>如果 grabber 包含音频流，则解析音频特有属性</p>
		 *
		 * @param grabber FFmpeg 帧抓取器，不可为 null
		 * @return 构建器自身，用于链式调用
		 * @throws IllegalArgumentException 当 grabber 为 null 时抛出
		 * @since 1.1.0
		 */
		@Override
		protected Builder parse(FFmpegFrameGrabber grabber) throws FFmpegFrameGrabber.Exception {
			Validate.notNull(grabber, "grabber 不可为 null");

			super.parse(grabber);
			if (grabber.hasAudio()) {
				this.metadata = Collections.unmodifiableMap(grabber.getAudioMetadata());
				this.codecName = grabber.getAudioCodecName();
				this.codecId = grabber.getAudioCodec();
				// 微秒 → 纳秒：1 微秒 = 1000 纳秒
				this.duration = Duration.ofNanos(grabber.getLengthInTime() * 1000);
				this.sampleRate = grabber.getSampleRate();
				this.channels = grabber.getAudioChannels();
				this.bitrate = grabber.getAudioBitrate();
			}

			return this;
		}
	}
}
