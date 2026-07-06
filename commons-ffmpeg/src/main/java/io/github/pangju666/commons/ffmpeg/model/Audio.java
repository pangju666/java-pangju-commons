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

import io.github.pangju666.commons.ffmpeg.lang.FFmpegConstants;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.IOUtils;
import org.apache.commons.lang3.Validate;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;

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
 *     <li>支持解析自多种来源（{@link File}/byte[]/{@link InputStream}/{@link FFmpegFrameGrabber}）</li>
 *     <li>不可变对象设计，线程安全</li>
 *     <li>提供 Fluent Builder 模式，支持链式调用</li>
 *     <li>内置音频格式判断（单声道/立体声等）</li>
 *     <li>提供预定义标准音频配置常量（{@link #WAV}、{@link #FLAC}、{@link #MP3}、{@link #MP3_HIGH}、
 *     {@link #OPUS}、{@link #OPUS_HIGH}、{@link #AAC}、{@link #AAC_HIGH}）</li>
 * </ul>
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 从文件解析音频
 * Audio audio = Audio.builder(new File("music.wav")).build();
 *
 * // 使用预定义配置
 * Audio standardAudio = Audio.MP3;
 *
 * // 手动构建标准音频
 * Audio customAudio = Audio.builder()
 *     .mp3()
 *     .stereo()
 *     .cd()
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
 * @since 2.1.0
 */
public class Audio extends Media {
	/**
	 * 标准 WAV 音频配置
	 * <p>使用 PCM 16-bit LE 编码，默认采样率 44100Hz，立体声</p>
	 *
	 * @since 2.1.0
	 */
	public static final Audio WAV = Audio.builder()
		.wav()
		.build();

	/**
	 * 标准 FLAC 音频配置
	 * <p>无损压缩格式，默认采样率 44100Hz，立体声</p>
	 *
	 * @since 2.1.0
	 */
	public static final Audio FLAC = Audio.builder()
		.flac()
		.build();

	/**
	 * 标准 MP3 音频配置（128kbps）
	 * <p>适用于一般音质需求，采样率 44100Hz，立体声</p>
	 *
	 * @since 2.1.0
	 */
	public static final Audio MP3 = Audio.builder()
		.mp3()
		.bitrate(128_000)
		.build();

	/**
	 * 高音质 MP3 音频配置（192kbps）
	 * <p>适用于较高音质需求，采样率 44100Hz，立体声</p>
	 *
	 * @since 2.1.0
	 */
	public static final Audio MP3_HIGH = Audio.builder()
		.mp3()
		.bitrate(192_000)
		.build();

	/**
	 * 标准 OPUS 音频配置（96kbps）
	 * <p>适用于网络音频传输，采样率 44100Hz，立体声</p>
	 *
	 * @since 2.1.0
	 */
	public static final Audio OPUS = Audio.builder()
		.opus()
		.bitrate(96_000)
		.build();

	/**
	 * 高音质 OPUS 音频配置（192kbps）
	 * <p>适用于高音质网络传输，采样率 44100Hz，立体声</p>
	 *
	 * @since 2.1.0
	 */
	public static final Audio OPUS_HIGH = Audio.builder()
		.opus()
		.bitrate(192_000)
		.build();

	/**
	 * 标准 AAC 音频配置（128kbps）
	 * <p>适用于一般音质需求，采样率 44100Hz，立体声</p>
	 *
	 * @since 2.1.0
	 */
	public static final Audio AAC = Audio.builder()
		.aac()
		.bitrate(128_000)
		.build();

	/**
	 * 高音质 AAC 音频配置（256kbps）
	 * <p>适用于较高音质需求，采样率 44100Hz，立体声</p>
	 *
	 * @since 2.1.0
	 */
	public static final Audio AAC_HIGH = Audio.builder()
		.aac()
		.bitrate(256_000)
		.build();

	/**
	 * 音频时长
	 *
	 * @since 2.1.0
	 */
	protected final Duration duration;

	/**
	 * 采样率（Hz），如 44100、48000 等
	 *
	 * @since 2.1.0
	 */
	protected final int sampleRate;

	/**
	 * 声道数，1 为单声道，2 为立体声，大于 2 为多声道
	 *
	 * @since 2.1.0
	 */
	protected final int channels;

	/**
	 * 比特率（bps），表示音频数据传输速率
	 *
	 * @since 2.1.0
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
	 * @since 2.1.0
	 */
	protected Audio(String format, String codecName, Map<String, String> metadata, int codecId, Duration duration,
	                int sampleRate, int channels, int bitrate) {
		super(format, codecName, metadata, codecId);
		this.duration = duration;
		this.sampleRate = sampleRate;
		this.channels = channels;
		this.bitrate = bitrate;
	}

	/**
	 * 创建新的音频构建器
	 *
	 * @return 空的 Audio.Builder 实例
	 * @since 2.1.0
	 */
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
	 * @since 2.1.0
	 */
	public static Audio.Builder builder(Audio audio) {
		return new Audio.Builder(audio);
	}

	/**
	 * 从 {@link FFmpegFrameGrabber} 创建构建器
	 * <p>会自动解析 grabber 中的音频信息</p>
	 *
	 * @param grabber FFmpeg 帧抓取器，不可为 null
	 * @return 已解析的 Audio.Builder 实例
	 * @throws IllegalArgumentException     当 grabber 为 null 时抛出
	 * @throws FFmpegFrameGrabber.Exception 当解析失败时抛出
	 * @since 2.1.0
	 */
	public static Audio.Builder builder(FFmpegFrameGrabber grabber) throws FFmpegFrameGrabber.Exception {
		return new Audio.Builder().parse(grabber);
	}

	/**
	 * 从文件创建构建器
	 * <p>会自动解析文件中的音频信息</p>
	 *
	 * @param file 音频文件，不可为 null 且必须是有效文件
	 * @return 已解析的 Audio.Builder 实例
	 * @throws IllegalArgumentException 当 file 为 null 或无效时抛出
	 * @throws IOException              当文件读取失败时抛出
	 * @since 2.1.0
	 */
	public static Audio.Builder builder(File file) throws IOException {
		FileUtils.checkFile(file, "file 不可为 null");

		try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file)) {
			grabber.start();
			return new Audio.Builder().parse(grabber);
		}
	}

	/**
	 * 从字节数组创建构建器
	 * <p>会自动解析字节数组中的音频信息</p>
	 *
	 * @param bytes 音频字节数组，不可为 null
	 * @return 已解析的 Audio.Builder 实例
	 * @throws IllegalArgumentException 当 bytes 为 null 时抛出
	 * @throws IOException              当解析失败时抛出
	 * @since 2.1.0
	 */
	public static Audio.Builder builder(byte[] bytes) throws IOException {
		Validate.notNull(bytes, "bytes 不可为 null");

		try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(IOUtils.toUnsynchronizedByteArrayInputStream(bytes))) {
			grabber.start();
			return new Audio.Builder().parse(grabber);
		}
	}

	/**
	 * 从输入流创建构建器
	 * <p>会自动解析输入流中的音频信息</p>
	 *
	 * @param inputStream 音频输入流，不可为 null
	 * @return 已解析的 Audio.Builder 实例
	 * @throws IllegalArgumentException 当 inputStream 为 null 时抛出
	 * @throws IOException              当读取失败时抛出
	 * @since 2.1.0
	 */
	public static Audio.Builder builder(InputStream inputStream) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputStream)) {
			grabber.start();
			return new Audio.Builder().parse(grabber);
		}
	}

	/**
	 * 从 {@link FFmpegFrameGrabber} 解析音频对象
	 * <p>会自动解析 grabber 中的音频信息并构建 Audio 对象
	 *
	 * @param grabber FFmpeg 帧抓取器，不可为 null
	 * @return 解析得到的 Audio 对象
	 * @throws IllegalArgumentException     当 grabber 为 null 时抛出
	 * @throws FFmpegFrameGrabber.Exception 当解析失败时抛出
	 * @since 2.1.0
	 */
	public static Audio parse(FFmpegFrameGrabber grabber) throws FFmpegFrameGrabber.Exception {
		return builder(grabber).build();
	}

	/**
	 * 从文件解析音频对象
	 * <p>会自动解析文件中的音频信息并构建 Audio 对象
	 *
	 * @param file 音频文件，不可为 null 且必须是有效文件
	 * @return 解析得到的 Audio 对象
	 * @throws IllegalArgumentException 当 file 为 null 或无效时抛出
	 * @throws IOException              当文件读取失败时抛出
	 * @since 2.1.0
	 */
	public static Audio parse(File file) throws IOException {
		return builder(file).build();
	}

	/**
	 * 从字节数组解析音频对象
	 * <p>会自动解析字节数组中的音频信息并构建 Audio 对象
	 *
	 * @param bytes 音频字节数组，不可为 null
	 * @return 解析得到的 Audio 对象
	 * @throws IllegalArgumentException 当 bytes 为 null 时抛出
	 * @throws IOException              当解析失败时抛出
	 * @since 2.1.0
	 */
	public static Audio parse(byte[] bytes) throws IOException {
		return builder(bytes).build();
	}

	/**
	 * 从输入流解析音频对象
	 * <p>会自动解析输入流中的音频信息并构建 Audio 对象
	 *
	 * @param inputStream 音频输入流，不可为 null
	 * @return 解析得到的 Audio 对象
	 * @throws IllegalArgumentException 当 inputStream 为 null 时抛出
	 * @throws IOException              当读取失败时抛出
	 * @since 2.1.0
	 */
	public static Audio parse(InputStream inputStream) throws IOException {
		return builder(inputStream).build();
	}

	/**
	 * 获取音频时长
	 *
	 * @return 音频时长，可能为 null
	 * @since 2.1.0
	 */
	public Duration getDuration() {
		return duration;
	}

	/**
	 * 获取采样率（Hz）
	 *
	 * @return 采样率，如 44100、48000
	 * @since 2.1.0
	 */
	public int getSampleRate() {
		return sampleRate;
	}

	/**
	 * 获取声道数
	 *
	 * @return 声道数，1 为单声道，2 为立体声
	 * @since 2.1.0
	 */
	public int getChannels() {
		return channels;
	}

	/**
	 * 获取比特率（bps）
	 *
	 * @return 比特率，如 64000（64kbps）
	 * @since 2.1.0
	 */
	public int getBitrate() {
		return bitrate;
	}

	/**
	 * 判断是否为单声道音频
	 *
	 * @return true 表示单声道，false 表示非单声道
	 * @since 2.1.0
	 */
	public boolean isMono() {
		return this.channels == 1;
	}

	/**
	 * 判断是否为立体声音频
	 *
	 * @return true 表示立体声，false 表示非立体声
	 * @since 2.1.0
	 */
	public boolean isStereo() {
		return this.channels == 2;
	}

	/**
	 * 判断是否有时长信息
	 * <p>检查 duration 是否不为 null、不为零时长且不为负数</p>
	 *
	 * @return true 表示有有效时长，false 表示无有效时长
	 * @since 2.1.0
	 */
	public boolean hasDuration() {
		return duration != null && !duration.isZero() && !duration.isNegative();
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
	 *     <li>{@link #cd()} / {@link #film()}：快速设置采样率</li>
	 *     <li>{@link #wav()} / {@link #flac()} / {@link #mp3()} / {@link #opus()} / {@link #aac()}：快捷设置容器和编码器</li>
	 *     <li>{@link #sampleRate(int)}：设置采样率</li>
	 *     <li>{@link #bitrate(int)}：设置比特率</li>
	 *     <li>{@link #channels(int)}：设置声道数</li>
	 * </ul>
	 * <h3>使用示例</h3>
	 * <pre>{@code
	 * // 新建标准音频
	 * Audio audio = new Audio.Builder()
	 *     .format("mp3")
	 *     .codecId(avcodec.AV_CODEC_ID_MP3)
	 *     .channels(2)
	 *     .sampleRate(44100)
	 *     .bitrate(128000)
	 *     .build();
	 *
	 * // 使用快捷格式方法
	 * Audio wavAudio = new Audio.Builder()
	 *     .wav()
	 *     .stereo()
	 *     .cd()
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
	 * @since 2.1.0
	 */
	public static class Builder extends Media.Builder<Audio.Builder, Audio> {
		/**
		 * 音频时长
		 *
		 * @since 2.1.0
		 */
		protected Duration duration;

		/**
		 * 采样率（Hz）
		 *
		 * @since 2.1.0
		 */
		protected int sampleRate;

		/**
		 * 声道数
		 *
		 * @since 2.1.0
		 */
		protected int channels;

		/**
		 * 比特率（bps）
		 *
		 * @since 2.1.0
		 */
		protected int bitrate;

		/**
		 * 创建空的音频构建器
		 * <p>使用默认值：采样率 44100Hz，立体声，比特率 0</p>
		 *
		 * @since 2.1.0
		 */
		public Builder() {
			super();
			this.sampleRate = FFmpegConstants.AUDIO_STANDARD_SAMPLE_RATE;
			this.channels = FFmpegConstants.DEFAULT_AUDIO_CHANNELS;
			this.bitrate = 0;
		}

		/**
		 * 基于现有音频对象创建构建器
		 * <p>新构建器会复制现有音频的所有属性，方便进行修改
		 *
		 * @param audio 源音频对象，不可为 null
		 * @throws IllegalArgumentException 当 audio 为 null 时抛出
		 * @since 2.1.0
		 */
		public Builder(Audio audio) {
			super(audio);
			this.duration = Duration.ofNanos(audio.getDuration().toNanos());
			this.sampleRate = audio.sampleRate;
			this.channels = audio.channels;
			this.bitrate = audio.bitrate;
		}

		/**
		 * 设置为 WAV 格式
		 * <p>使用 PCM 16-bit LE 编码，适合无损音频处理</p>
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 2.1.0
		 */
		public Builder wav() {
			this.format = FFmpegConstants.AUDIO_WAV_FORMAT;
			this.codecId = avcodec.AV_CODEC_ID_PCM_S16LE;
			return this;
		}

		/**
		 * 设置为 FLAC 格式
		 * <p>使用 FLAC 无损压缩编码</p>
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 2.1.0
		 */
		public Builder flac() {
			this.format = FFmpegConstants.AUDIO_FLAC_FORMAT;
			this.codecId = avcodec.AV_CODEC_ID_FLAC;
			return this;
		}

		/**
		 * 设置为 MP3 格式
		 * <p>使用 MP3 有损压缩编码</p>
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 2.1.0
		 */
		public Builder mp3() {
			this.format = FFmpegConstants.AUDIO_MP3_FORMAT;
			this.codecId = avcodec.AV_CODEC_ID_MP3;
			return this;
		}

		/**
		 * 设置为 OPUS 格式
		 * <p>使用 OPUS 编码，适合网络传输</p>
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 2.1.0
		 */
		public Builder opus() {
			this.format = FFmpegConstants.AUDIO_OPUS_FORMAT;
			this.codecId = avcodec.AV_CODEC_ID_OPUS;
			return this;
		}

		/**
		 * 设置为 AAC 格式
		 * <p>使用 AAC 编码，适合移动设备</p>
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 2.1.0
		 */
		public Builder aac() {
			this.format = FFmpegConstants.AUDIO_AAC_FORMAT;
			this.codecId = avcodec.AV_CODEC_ID_AAC;
			return this;
		}

		/**
		 * 设置CD标准44100Hz采样率（通用音乐标准）
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 2.1.0
		 */
		public Builder cd() {
			this.sampleRate = FFmpegConstants.AUDIO_STANDARD_SAMPLE_RATE;
			return this;
		}

		/**
		 * 设置影视标准48000Hz音频采样率，适配视频、短视频、影视配乐场景
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 2.1.0
		 */
		public Builder film() {
			this.sampleRate = FFmpegConstants.VIDEO_STANDARD_SAMPLE_RATE;
			return this;
		}

		/**
		 * 设置为单声道
		 * <p>快捷方法，等效于调用 channels(1)</p>
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 2.1.0
		 */
		public Builder mono() {
			return channels(1);
		}

		/**
		 * 设置为立体声
		 * <p>快捷方法，等效于调用 channels(2)</p>
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 2.1.0
		 */
		public Builder stereo() {
			return channels(2);
		}

		/**
		 * 设置采样率
		 *
		 * @param sampleRate 采样率（Hz），必须大于 0
		 * @return 构建器自身，用于链式调用
		 * @throws IllegalArgumentException 当 sampleRate 小于等于 0 时抛出
		 * @since 2.1.0
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
		 * @since 2.1.0
		 */
		public Builder channels(int channels) {
			Validate.isTrue(channels > 0, "channels 必须大于 0");
			this.channels = channels;
			return this;
		}

		/**
		 * 设置比特率
		 *
		 * @param bitrate 比特率（bps），必须为非负数
		 * @return 构建器自身，用于链式调用
		 * @throws IllegalArgumentException 当 bitrate 为负数时抛出
		 * @since 2.1.0
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
		 * @since 2.1.0
		 */
		@Override
		public Audio build() {
			return new Audio(this.format, this.codecName, this.metadata, this.codecId, this.duration, this.sampleRate,
				this.channels, this.bitrate);
		}

		/**
		 * 从 {@link FFmpegFrameGrabber} 解析音频信息
		 * <p>如果 grabber 包含音频流，则解析音频特有属性</p>
		 *
		 * @param grabber FFmpeg 帧抓取器，不可为 null
		 * @return 构建器自身，用于链式调用
		 * @throws IllegalArgumentException 当 grabber 为 null 时抛出
		 * @since 2.1.0
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
