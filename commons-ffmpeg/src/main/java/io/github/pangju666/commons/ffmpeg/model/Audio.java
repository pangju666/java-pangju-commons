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

import io.github.pangju666.commons.ffmpeg.io.resource.FFmpegResource;
import io.github.pangju666.commons.ffmpeg.lang.FFmpegConstants;
import org.apache.commons.lang3.Validate;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;

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
 *     <li>基于 JavaCV/FFmpeg 自动解析音频元数据</li>
 *     <li>采用 Fluent Builder 模式，支持链式调用</li>
 *     <li>不可变对象设计，线程安全</li>
 *     <li>内置音频格式判断（单声道/立体声等）</li>
 *     <li>提供预定义标准音频配置常量（{@link #WAV}、{@link #FLAC}、{@link #MP3}、{@link #MP3_HIGH}、
 *     {@link #OPUS}、{@link #OPUS_HIGH}、{@link #AAC}、{@link #AAC_HIGH}）</li>
 * </ul>
 * <h3>音频特有属性</h3>
 * <ul>
 *     <li>{@link #duration} - 音频播放时长</li>
 *     <li>{@link #sampleRate} - 采样率（Hz），如 44100、48000</li>
 *     <li>{@link #channels} - 声道数，如 1（单声道）、2（立体声）</li>
 *     <li>{@link #bitrate} - 比特率（bps），如 128000（128kbps）</li>
 *     <li>{@link #sampleFormat} - 采样格式，如 AV_SAMPLE_FMT_S16</li>
 * </ul>
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 从 FFmpegResource 解析音频
 * FFmpegResource resource = new FFmpegResource(new File("music.wav"));
 * Audio audio = Audio.builder(resource).build();
 *
 * // 使用预定义配置
 * Audio standardAudio = Audio.MP3;
 *
 * // 手动构建标准音频
 * Audio customAudio = Audio.mp3()
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
 * @see Builder
 * @since 2.1.0
 */
public class Audio extends Media {
	/**
	 * 标准 WAV 音频配置
	 * <p>使用 PCM 16-bit LE 编码，默认采样率 44100Hz，立体声</p>
	 *
	 * @since 2.1.0
	 */
	public static final Audio WAV = Audio.wav()
		.cd()
		.stereo()
		.build();

	/**
	 * 标准 FLAC 音频配置
	 * <p>无损压缩格式，默认采样率 44100Hz，立体声</p>
	 *
	 * @since 2.1.0
	 */
	public static final Audio FLAC = Audio.flac()
		.cd()
		.stereo()
		.build();

	/**
	 * 标准 MP3 音频配置（128kbps）
	 * <p>适用于一般音质需求，采样率 44100Hz，立体声</p>
	 *
	 * @since 2.1.0
	 */
	public static final Audio MP3 = Audio.mp3()
		.cd()
		.stereo()
		.bitrate(128_000)
		.build();

	/**
	 * 高音质 MP3 音频配置（192kbps）
	 * <p>适用于较高音质需求，采样率 44100Hz，立体声</p>
	 *
	 * @since 2.1.0
	 */
	public static final Audio MP3_HIGH = Audio.mp3()
		.cd()
		.stereo()
		.bitrate(192_000)
		.build();

	/**
	 * 标准 OPUS 音频配置（96kbps）
	 * <p>适用于网络音频传输，采样率 44100Hz，立体声</p>
	 *
	 * @since 2.1.0
	 */
	public static final Audio OPUS = Audio.opus()
		.cd()
		.stereo()
		.bitrate(96_000)
		.build();

	/**
	 * 高音质 OPUS 音频配置（192kbps）
	 * <p>适用于高音质网络传输，采样率 44100Hz，立体声</p>
	 *
	 * @since 2.1.0
	 */
	public static final Audio OPUS_HIGH = Audio.opus()
		.cd()
		.stereo()
		.bitrate(192_000)
		.build();

	/**
	 * 标准 AAC 音频配置（128kbps）
	 * <p>适用于一般音质需求，采样率 44100Hz，立体声</p>
	 *
	 * @since 2.1.0
	 */
	public static final Audio AAC = Audio.aac()
		.cd()
		.stereo()
		.bitrate(128_000)
		.build();

	/**
	 * 高音质 AAC 音频配置（256kbps）
	 * <p>适用于较高音质需求，采样率 44100Hz，立体声</p>
	 *
	 * @since 2.1.0
	 */
	public static final Audio AAC_HIGH = Audio.aac()
		.cd()
		.stereo()
		.bitrate(256_000)
		.build();

	/**
	 * 音频时长
	 * <p>表示音频的总播放时长，null 或 {@link Duration#ZERO} 表示无有效时长（如实时流）</p>
	 *
	 * @since 2.1.0
	 */
	protected final Duration duration;

	/**
	 * 采样率（Hz），如 44100、48000 等
	 * <p>常见采样率：44100Hz（CD音质标准）、48000Hz（专业视频音频标准）</p>
	 *
	 * @since 2.1.0
	 */
	protected final int sampleRate;

	/**
	 * 声道数，1 为单声道，2 为立体声，大于 2 为多声道
	 * <p>常见配置：1（单声道）、2（立体声）、5.1（环绕声）、7.1（全景声）</p>
	 *
	 * @since 2.1.0
	 */
	protected final int channels;

	/**
	 * 比特率（bps），表示音频数据传输速率
	 * <p>比特率越高音质越好、体积越大，需根据编码格式和用途合理设置</p>
	 * <p>常见比特率：128kbps（标准MP3）、192kbps（高音质MP3）、256kbps（AAC高音质）、320kbps（MP3最高）</p>
	 *
	 * @since 2.1.0
	 */
	protected final int bitrate;

	/**
	 * 采样格式
	 * <p>表示音频样本的数据格式，如 AV_SAMPLE_FMT_S16（16位有符号整数）、AV_SAMPLE_FMT_FLT（32位浮点数）</p>
	 *
	 * @since 2.1.0
	 */
	protected final int sampleFormat;

	/**
	 * 受保护的构造函数，仅用于 Builder 内部实例化
	 *
	 * @param format      音频格式
	 * @param codecName   编码器名称
	 * @param metadata    音频元数据
	 * @param codecId     编码器ID
	 * @param duration    音频时长
	 * @param sampleRate  采样率
	 * @param channels    声道数
	 * @param bitrate     比特率
	 * @param sampleFormat 采样格式
	 * @since 2.1.0
	 */
	protected Audio(String format, String codecName, Map<String, String> metadata, int codecId, Duration duration,
	                int sampleRate, int channels, int bitrate, int sampleFormat) {
		super(format, codecName, metadata, codecId);
		this.duration = duration;
		this.sampleRate = sampleRate;
		this.channels = channels;
		this.bitrate = bitrate;
		this.sampleFormat = sampleFormat;
	}

	/**
	 * 设置为 WAV 格式
	 * <p>使用 PCM 16-bit LE 编码，适合无损音频处理</p>
	 *
	 * @return 构建器自身，用于链式调用
	 * @since 2.1.0
	 */
	public static Builder wav() {
		return new Builder(FFmpegConstants.AUDIO_WAV_FORMAT)
			.codecId(avcodec.AV_CODEC_ID_PCM_S16LE);
	}

	/**
	 * 设置为 FLAC 格式
	 * <p>使用 FLAC 无损压缩编码</p>
	 *
	 * @return 构建器自身，用于链式调用
	 * @since 2.1.0
	 */
	public static Builder flac() {
		return new Builder(FFmpegConstants.AUDIO_FLAC_FORMAT)
			.codecId(avcodec.AV_CODEC_ID_FLAC);
	}

	/**
	 * 设置为 MP3 格式
	 * <p>使用 MP3 有损压缩编码</p>
	 *
	 * @return 构建器自身，用于链式调用
	 * @since 2.1.0
	 */
	public static Builder mp3() {
		return new Builder(FFmpegConstants.AUDIO_MP3_FORMAT)
			.codecId(avcodec.AV_CODEC_ID_MP3);
	}

	/**
	 * 设置为 OPUS 格式
	 * <p>使用 OPUS 编码，适合网络传输</p>
	 *
	 * @return 构建器自身，用于链式调用
	 * @since 2.1.0
	 */
	public static Builder opus() {
		return new Builder(FFmpegConstants.AUDIO_OPUS_FORMAT)
			.codecId(avcodec.AV_CODEC_ID_OPUS);
	}

	/**
	 * 设置为 AAC 格式
	 * <p>使用 AAC 编码，适合移动设备</p>
	 *
	 * @return 构建器自身，用于链式调用
	 * @since 2.1.0
	 */
	public static Builder aac() {
		return new Builder(FFmpegConstants.AUDIO_AAC_FORMAT)
			.codecId(avcodec.AV_CODEC_ID_AAC);
	}

	/**
	 * 创建新的音频构建器
	 *
	 * @param format 媒体格式，不可为空白
	 * @return 空的 Audio.Builder 实例
	 * @throws IllegalArgumentException 当 format 为空白时抛出
	 * @since 2.1.0
	 */
	public static Builder builder(String format) {
		return new Builder(format);
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
	public static Builder builder(Audio audio) {
		return new Builder(audio);
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
	public static Builder builder(FFmpegFrameGrabber grabber) throws FFmpegFrameGrabber.Exception {
		return new Builder(grabber);
	}

	/**
	 * 从 FFmpegResource 创建构建器
	 * <p>会自动解析资源中的音频信息</p>
	 *
	 * @param resource FFmpeg 资源，不可为 null
	 * @return 已解析的 Audio.Builder 实例
	 * @throws NullPointerException 当 resource 为 null 时抛出
	 * @throws IOException          当读取失败时抛出
	 * @since 2.1.0
	 */
	public static Builder builder(FFmpegResource resource) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber()) {
			grabber.start();
			return new Builder(grabber);
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
	public static Builder builder(InputStream inputStream) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputStream)) {
			grabber.start();
			return new Builder(grabber);
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
	 * 从 FFmpegResource 解析音频对象
	 * <p>会自动解析资源中的音频信息并构建 Audio 对象
	 *
	 * @param resource FFmpeg 资源，不可为 null
	 * @return 解析得到的 Audio 对象
	 * @throws NullPointerException 当 resource 为 null 时抛出
	 * @throws IOException          当读取失败时抛出
	 * @since 2.1.0
	 */
	public static Audio parse(FFmpegResource resource) throws IOException {
		return builder(resource).build();
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
	 * 获取采样格式
	 *
	 * @return 采样格式，如 AV_SAMPLE_FMT_S16、AV_SAMPLE_FMT_FLT
	 * @since 2.1.0
	 */
	public int getSampleFormat() {
		return sampleFormat;
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
	 * <h3>创建构建器的方式</h3>
	 * <ul>
	 *     <li><b>空构建器</b>：使用 {@code Audio.builder(format)} 创建空白构建器，用于全新构建</li>
	 *     <li><b>解析构建器</b>：使用 {@code Audio.builder(grabber)} 从 FFmpegFrameGrabber 解析</li>
	 *     <li><b>复制构建器</b>：使用 {@code Audio.builder(audio)} 基于现有音频对象创建，用于修改现有对象</li>
	 * </ul>
	 * <h3>音频特有构建方法</h3>
	 * <ul>
	 *     <li>{@link #mono()} / {@link #stereo()}：快速设置声道数</li>
	 *     <li>{@link #cd()} / {@link #film()}：快速设置采样率</li>
	 *     <li>{@link #sampleRate(int)}：设置采样率</li>
	 *     <li>{@link #channels(int)}：设置声道数</li>
	 *     <li>{@link #bitrate(int)}：设置比特率</li>
	 *     <li>{@link #sampleFormat(int)}：设置采样格式（一般不需要手动设置，由 FFmpeg 自动解析）</li>
	 * </ul>
	 * <h3>快捷格式方法（静态工厂）</h3>
	 * <ul>
	 *     <li>{@link Audio#wav()} / {@link Audio#flac()} / {@link Audio#mp3()} / {@link Audio#opus()} / {@link Audio#aac()}：快捷设置容器和编码器</li>
	 * </ul>
	 * <h3>使用示例</h3>
	 * <pre>{@code
	 * // 使用快捷格式方法
	 * Audio wavAudio = Audio.wav()
	 *     .stereo()
	 *     .cd()
	 *     .build();
	 *
	 * // 修改现有音频
	 * Audio modified = Audio.builder(existingAudio)
	 *     .bitrate(320000)
	 *     .build();
	 * }</pre>
	 *
	 * @author pangju666
	 * @see Media.Builder
	 * @see Audio
	 * @since 2.1.0
	 */
	public static class Builder extends Media.Builder<Builder, Audio> {
		/**
		 * 音频时长
		 * <p>表示音频的总播放时长，null 或 {@link Duration#ZERO} 表示无有效时长（如实时流）</p>
		 *
		 * @since 2.1.0
		 */
		protected Duration duration;

		/**
		 * 采样率（Hz）
		 * <p>常见采样率：44100Hz（CD音质标准）、48000Hz（专业视频音频标准）</p>
		 *
		 * @since 2.1.0
		 */
		protected int sampleRate;

		/**
		 * 声道数
		 * <p>常见配置：1（单声道）、2（立体声）、5.1（环绕声）、7.1（全景声）</p>
		 *
		 * @since 2.1.0
		 */
		protected int channels;

		/**
		 * 比特率（bps）
		 * <p>比特率越高音质越好、体积越大，需根据编码格式和用途合理设置</p>
		 * <p>常见比特率：128kbps（标准MP3）、192kbps（高音质MP3）、256kbps（AAC高音质）、320kbps（MP3最高）</p>
		 *
		 * @since 2.1.0
		 */
		protected int bitrate;

		/**
		 * 采样格式
		 * <p>表示音频样本的数据格式</p>
		 *
		 * @since 2.1.0
		 */
		protected int sampleFormat;

		/**
		 * 空构建器构造函数
		 * <p>
		 * 创建一个空白的构建器，使用默认值：采样率 44100Hz，立体声，比特率 64kbps，时长 ZERO，采样格式 NONE。
		 * </p>
		 *
		 * @param format 媒体格式，不可为空白
		 * @throws IllegalArgumentException 当 format 为空白时抛出
		 * @since 2.1.0
		 */
		public Builder(String format) {
			super(format);

			this.sampleRate = FFmpegConstants.AUDIO_STANDARD_SAMPLE_RATE;
			this.channels = FFmpegConstants.DEFAULT_AUDIO_CHANNELS;
			this.bitrate = 64000;
			this.duration = Duration.ZERO;
			this.sampleFormat = avutil.AV_SAMPLE_FMT_NONE;
		}

		/**
		 * 从 FFmpegFrameGrabber 创建构建器
		 * <p>
		 * 自动解析 grabber 中的音频信息（格式、元数据、编码器、时长、采样率、声道数、比特率、采样格式）。
		 * </p>
		 *
		 * @param grabber FFmpeg 帧抓取器，不可为 null
		 * @throws FFmpegFrameGrabber.Exception 当 grabber 启动失败时抛出
		 * @throws IllegalArgumentException     当 grabber 为 null 时抛出
		 * @since 2.1.0
		 */
		public Builder(FFmpegFrameGrabber grabber) throws FFmpegFrameGrabber.Exception {
			super(grabber);

			if (grabber.hasAudio()) {
				this.metadata = Collections.unmodifiableMap(grabber.getAudioMetadata());
				this.codecName = grabber.getAudioCodecName();
				this.codecId = grabber.getAudioCodec();
				// 微秒 → 纳秒：1 微秒 = 1000 纳秒
				this.duration = Duration.ofNanos(grabber.getLengthInTime() * 1000);
				this.sampleRate = grabber.getSampleRate();
				this.channels = grabber.getAudioChannels();
				this.bitrate = grabber.getAudioBitrate();
				this.sampleFormat = grabber.getSampleFormat();
			}
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
			this.sampleFormat = audio.sampleFormat;
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
		 * 设置采样格式
		 * <p>采样格式表示音频样本的数据格式，如 AV_SAMPLE_FMT_S16、AV_SAMPLE_FMT_FLT</p>
		 *
		 * @param sampleFormat 采样格式，必须大于等于 -1（-1 表示 AV_SAMPLE_FMT_NONE）
		 * @return 构建器自身，用于链式调用
		 * @throws IllegalArgumentException 当 sampleFormat 小于 -1 时抛出
		 * @since 2.1.0
		 */
		public Builder sampleFormat(int sampleFormat) {
			Validate.isTrue(sampleFormat >= -1, "sampleFormat 必须大于等于 -1");
			this.sampleFormat = sampleFormat;
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
				this.channels, this.bitrate, this.sampleFormat);
		}
	}
}
