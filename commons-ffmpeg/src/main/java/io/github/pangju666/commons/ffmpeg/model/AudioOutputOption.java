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

import io.github.pangju666.commons.ffmpeg.builder.AudioOutputOptionBuilder;
import io.github.pangju666.commons.ffmpeg.enums.FrameType;
import io.github.pangju666.commons.ffmpeg.lang.FFmpegConstants;
import io.github.pangju666.commons.ffmpeg.utils.FFmpegUtils;
import io.github.pangju666.commons.io.exception.UnsupportedResourceException;
import org.apache.commons.lang3.Validate;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;

import java.util.Map;

/**
 * 音频输出选项类，封装音频文件的输出配置
 * <p>
 * 该类继承自 {@link OutputOption}，专门用于配置音频文件的输出参数，
 * 包含音频特有的属性如采样率、声道数、比特率等。
 * </p>
 * <h3>核心特性</h3>
 * <ul>
 *     <li>基于 JavaCV/FFmpeg 自动验证编码器支持</li>
 *     <li>提供预定义标准音频配置静态方法</li>
 *     <li>支持从 Audio 对象解析配置</li>
 *     <li>支持从 FFmpegFrameGrabber 解析配置</li>
 *     <li>提供录制器配置方法</li>
 * </ul>
 * <h3>音频特有属性</h3>
 * <ul>
 *     <li>{@link #sampleRate} - 采样率（Hz），如 44100、48000</li>
 *     <li>{@link #channels} - 声道数，如 1（单声道）、2（立体声）</li>
 *     <li>{@link #bitrate} - 比特率（bps），如 128000（128kbps）</li>
 *     <li>{@link #sampleFormat} - 采样格式，如 AV_SAMPLE_FMT_S16</li>
 * </ul>
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 从 Audio 对象解析配置
 * AudioOutputOption option = new AudioOutputOption(audio);
 *
 * // 从 FFmpegFrameGrabber 解析配置
 * AudioOutputOption option = new AudioOutputOption(grabber);
 *
 * // 使用预定义配置
 * AudioOutputOption option = AudioOutputOption.mp3();
 *
 * // 为视频配置音频
 * AudioOutputOption option = AudioOutputOption.aacForVideo(128000);
 * }</pre>
 *
 * @author pangju666
 * @apiNote 无损编码设置比特率无效，一般情况下不要设置采样格式
 * @see OutputOption
 * @see Audio
 * @see AudioOutputOptionBuilder
 * @since 2.1.0
 */
public class AudioOutputOption extends OutputOption {
	/**
	 * 采样率（Hz），如 44100、48000 等
	 * <p>常见采样率：44100Hz（CD音质标准）、48000Hz（专业视频音频标准）</p>
	 *
	 * @since 2.1.0
	 */
	protected int sampleRate = FFmpegConstants.CD_STANDARD_SAMPLE_RATE;

	/**
	 * 声道数，1 为单声道，2 为立体声，大于 2 为多声道
	 * <p>常见配置：1（单声道）、2（立体声）、5.1（环绕声）、7.1（全景声）</p>
	 *
	 * @since 2.1.0
	 */
	protected int channels = FFmpegConstants.DEFAULT_AUDIO_CHANNELS;

	/**
	 * 比特率（bps），表示音频数据传输速率
	 * <p>比特率越高音质越好、体积越大，需根据编码格式和用途合理设置</p>
	 * <p>常见比特率：128kbps（标准MP3）、192kbps（高音质MP3）、256kbps（AAC高音质）、320kbps（MP3最高）</p>
	 *
	 * @since 2.1.0
	 */
	protected int bitrate = 64000;

	/**
	 * 采样格式
	 * <p>表示音频样本的数据格式，如 AV_SAMPLE_FMT_S16（16位有符号整数）、AV_SAMPLE_FMT_FLT（32位浮点数）</p>
	 *
	 * @since 2.1.0
	 */
	protected int sampleFormat = avutil.AV_SAMPLE_FMT_NONE;

	/**
	 * 根据格式构造音频输出选项
	 *
	 * @param format 音频格式
	 * @throws IllegalArgumentException     当 format 为空时抛出
	 * @throws UnsupportedResourceException 当格式不被支持时抛出
	 * @since 2.1.0
	 */
	public AudioOutputOption(String format) {
		super(format);
	}

	/**
	 * 从 Audio 对象构造音频输出选项
	 *
	 * @param audio 音频信息对象，不可为 null
	 * @throws IllegalArgumentException 当 audio 为 null 时抛出
	 * @since 2.1.0
	 */
	public AudioOutputOption(Audio audio) {
		super();

		Validate.notNull(audio, "audio 不可为 null");

		setFormat(FFmpegUtils.parseFormat(audio.format()));
		Validate.notBlank(format, "format 不可为空");

		setCodecId(audio.codecId());
		setSampleRate(audio.sampleRate());
		setChannels(audio.channels());
		setBitrate(audio.bitrate());
		setMetadata(audio.metadata());
		// 不提取采样格式
		//setSampleFormat(audio.sampleFormat());
	}

	/**
	 * 从 FFmpeg 帧抓取器构造音频输出选项
	 *
	 * @param grabber FFmpeg 帧抓取器
	 * @since 2.1.0
	 */
	public AudioOutputOption(FFmpegFrameGrabber grabber) throws FFmpegFrameGrabber.Exception {
		this(Audio.parse(grabber));
	}

	/**
	 * 创建 WAV 格式音频输出选项
	 * <p>使用 CD 标准采样率和立体声配置</p>
	 *
	 * @return WAV 音频输出选项
	 * @since 2.1.0
	 */
	public static AudioOutputOption wav() {
		return AudioOutputOptionBuilder.wav()
			.sampleRate(FFmpegConstants.CD_STANDARD_SAMPLE_RATE)
			.channels(FFmpegConstants.DEFAULT_AUDIO_CHANNELS)
			.bitrate(0)
			.build();
	}

	/**
	 * 创建 FLAC 格式音频输出选项
	 * <p>使用 CD 标准采样率和立体声配置</p>
	 *
	 * @return FLAC 音频输出选项
	 * @since 2.1.0
	 */
	public static AudioOutputOption flac() {
		return AudioOutputOptionBuilder.flac()
			.sampleRate(FFmpegConstants.CD_STANDARD_SAMPLE_RATE)
			.channels(FFmpegConstants.DEFAULT_AUDIO_CHANNELS)
			.bitrate(0)
			.build();
	}

	/**
	 * 创建高比特率 MP3 格式音频输出选项
	 * <p>使用 192kbps 比特率</p>
	 *
	 * @return MP3 音频输出选项
	 * @since 2.1.0
	 */
	public static AudioOutputOption mp3HighBitrate() {
		return AudioOutputOptionBuilder.mp3()
			.sampleRate(FFmpegConstants.CD_STANDARD_SAMPLE_RATE)
			.channels(FFmpegConstants.DEFAULT_AUDIO_CHANNELS)
			.bitrate(192_000)
			.build();
	}

	/**
	 * 创建标准比特率 MP3 格式音频输出选项
	 * <p>使用 128kbps 比特率</p>
	 *
	 * @return MP3 音频输出选项
	 * @since 2.1.0
	 */
	public static AudioOutputOption mp3() {
		return AudioOutputOptionBuilder.mp3()
			.sampleRate(FFmpegConstants.CD_STANDARD_SAMPLE_RATE)
			.channels(FFmpegConstants.DEFAULT_AUDIO_CHANNELS)
			.bitrate(128_000)
			.build();
	}

	/**
	 * 创建高比特率 Opus 格式音频输出选项
	 * <p>使用 192kbps 比特率</p>
	 *
	 * @return Opus 音频输出选项
	 * @since 2.1.0
	 */
	public static AudioOutputOption opusHighBitrate() {
		return AudioOutputOptionBuilder.opus()
			.sampleRate(FFmpegConstants.CD_STANDARD_SAMPLE_RATE)
			.channels(FFmpegConstants.DEFAULT_AUDIO_CHANNELS)
			.bitrate(192_000)
			.build();
	}

	/**
	 * 创建标准比特率 Opus 格式音频输出选项
	 * <p>使用 96kbps 比特率</p>
	 *
	 * @return Opus 音频输出选项
	 * @since 2.1.0
	 */
	public static AudioOutputOption opus() {
		return AudioOutputOptionBuilder.opus()
			.sampleRate(FFmpegConstants.CD_STANDARD_SAMPLE_RATE)
			.channels(FFmpegConstants.DEFAULT_AUDIO_CHANNELS)
			.bitrate(96_000)
			.build();
	}

	/**
	 * 创建高比特率 AAC 格式音频输出选项
	 * <p>使用 256kbps 比特率</p>
	 *
	 * @return AAC 音频输出选项
	 * @since 2.1.0
	 */
	public static AudioOutputOption aacHighBitrate() {
		return AudioOutputOptionBuilder.aac()
			.sampleRate(FFmpegConstants.CD_STANDARD_SAMPLE_RATE)
			.channels(FFmpegConstants.DEFAULT_AUDIO_CHANNELS)
			.bitrate(256_000)
			.build();
	}

	/**
	 * 创建标准比特率 AAC 格式音频输出选项
	 * <p>使用 128kbps 比特率</p>
	 *
	 * @return AAC 音频输出选项
	 * @since 2.1.0
	 */
	public static AudioOutputOption aac() {
		return AudioOutputOptionBuilder.aac()
			.sampleRate(FFmpegConstants.CD_STANDARD_SAMPLE_RATE)
			.channels(FFmpegConstants.DEFAULT_AUDIO_CHANNELS)
			.bitrate(128_000)
			.build();
	}

	/**
	 * 为视频创建 Opus 音频输出选项
	 * <p>使用广播标准采样率和指定比特率</p>
	 *
	 * @param bitrate 音频比特率
	 * @return Opus 音频输出选项
	 * @since 2.1.0
	 */
	public static AudioOutputOption opusForVideo(int bitrate) {
		return AudioOutputOptionBuilder.opus()
			.sampleRate(FFmpegConstants.BROADCAST_STANDARD_SAMPLE_RATE)
			.channels(FFmpegConstants.DEFAULT_AUDIO_CHANNELS)
			.bitrate(bitrate)
			.build();
	}

	/**
	 * 为视频创建 AAC 音频输出选项
	 * <p>使用广播标准采样率和指定比特率</p>
	 *
	 * @param bitrate 音频比特率
	 * @return AAC 音频输出选项
	 * @since 2.1.0
	 */
	public static AudioOutputOption aacForVideo(int bitrate) {
		return AudioOutputOptionBuilder.aac()
			.sampleRate(FFmpegConstants.BROADCAST_STANDARD_SAMPLE_RATE)
			.channels(FFmpegConstants.DEFAULT_AUDIO_CHANNELS)
			.bitrate(bitrate)
			.build();
	}

	/**
	 * 获取采样率
	 *
	 * @return 采样率（Hz）
	 * @since 2.1.0
	 */
	public int getSampleRate() {
		return sampleRate;
	}

	/**
	 * 设置采样率
	 *
	 * @param sampleRate 采样率（Hz），必须大于 0
	 * @since 2.1.0
	 */
	public void setSampleRate(int sampleRate) {
		if (sampleRate > 0) {
			this.sampleRate = sampleRate;
		}
	}

	/**
	 * 获取声道数
	 *
	 * @return 声道数
	 * @since 2.1.0
	 */
	public int getChannels() {
		return channels;
	}

	/**
	 * 设置声道数
	 *
	 * @param channels 声道数，必须大于 0
	 * @since 2.1.0
	 */
	public void setChannels(int channels) {
		if (channels > 0) {
			this.channels = channels;
		}
	}

	/**
	 * 获取比特率
	 *
	 * @return 比特率（bps）
	 * @since 2.1.0
	 */
	public int getBitrate() {
		return bitrate;
	}

	/**
	 * 设置比特率
	 *
	 * @param bitrate 比特率（bps），必须大于等于 0
	 * @apiNote 无损编码设置该值无效
	 * @since 2.1.0
	 */
	public void setBitrate(int bitrate) {
		if (bitrate >= 0) {
			this.bitrate = bitrate;
		}
	}

	/**
	 * 获取采样格式
	 *
	 * @return 采样格式
	 * @since 2.1.0
	 */
	public int getSampleFormat() {
		return sampleFormat;
	}

	/**
	 * 设置采样格式
	 *
	 * @param sampleFormat 采样格式
	 * @apiNote 一般不需要设置，让FFmpeg自行决定
	 * @since 2.1.0
	 */
	public void setSampleFormat(int sampleFormat) {
		this.sampleFormat = sampleFormat;
	}

	/**
	 * 配置 FFmpeg 帧录制器
	 * <p>
	 * 根据帧类型配置音频相关的录制器参数。
	 * </p>
	 *
	 * @param recorder  FFmpeg 帧录制器
	 * @param frameType 帧类型
	 * @throws IllegalArgumentException 当参数为 null 时抛出
	 * @since 2.1.0
	 */
	@Override
	public void configure(FFmpegFrameRecorder recorder, FrameType frameType) {
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.notNull(frameType, "frameType 不可为 null");

		if (frameType != FrameType.VIDEO) {
			recorder.setFormat(format);
			recorder.setAudioCodec(codecId);
			recorder.setSampleRate(sampleRate);
			recorder.setAudioChannels(channels);
			if (bitrate > 0) {
				recorder.setAudioBitrate(bitrate);
			}
			recorder.setSampleFormat(sampleFormat);
			if (!stripMetadata) {
				recorder.setMetadata(Map.copyOf(metadata));
			}
		}
	}
}
