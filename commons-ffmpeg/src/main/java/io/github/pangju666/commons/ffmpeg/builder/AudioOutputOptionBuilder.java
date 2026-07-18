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

import io.github.pangju666.commons.ffmpeg.lang.FFmpegConstants;
import io.github.pangju666.commons.ffmpeg.model.Audio;
import io.github.pangju666.commons.ffmpeg.model.AudioOutputOption;
import io.github.pangju666.commons.io.exception.UnsupportedResourceException;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.IOException;

/**
 * 音频输出选项构建器
 * <p>
 * 提供流式 API 用于构建 {@link AudioOutputOption} 实例，支持链式调用。
 * </p>
 * <h3>核心特性</h3>
 * <ul>
 *     <li>支持从格式、Audio 对象或 FFmpegFrameGrabber 创建构建器</li>
 *     <li>提供预定义的音频格式工厂方法（wav、flac、mp3、opus、aac）</li>
 *     <li>支持设置采样率、声道数、比特率等音频参数</li>
 *     <li>提供常用音频标准快捷方法（cd、mono、stereo）</li>
 * </ul>
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 使用预定义格式
 * AudioOutputOption option = AudioOutputOptionBuilder.mp3()
 *     .cd()
 *     .stereo()
 *     .bitrate(192000)
 *     .build();
 *
 * // 自定义格式
 * AudioOutputOption option = new AudioOutputOptionBuilder("wav")
 *     .sampleRate(48000)
 *     .channels(2)
 *     .bitrate(1411200)
 *     .build();
 *
 * // 从 Audio 对象创建
 * AudioOutputOption option = new AudioOutputOptionBuilder(audio)
 *     .bitrate(320000)
 *     .build();
 *
 * // 添加元数据
 * AudioOutputOption option = AudioOutputOptionBuilder.mp3()
 *     .addMetadata("title", "My Song")
 *     .addMetadata("artist", "Artist Name")
 *     .build();
 * }</pre>
 *
 * @author pangju666
 * @see AudioOutputOption
 * @since 1.1.0
 */
public class AudioOutputOptionBuilder extends OutputOptionBuilder<AudioOutputOptionBuilder, AudioOutputOption> {
	/**
	 * 根据音频格式创建构建器
	 *
	 * @param format 音频格式
	 * @throws IllegalArgumentException     当 format 为空时抛出
	 * @throws UnsupportedResourceException 当格式不被支持时抛出
	 * @since 1.1.0
	 */
	public AudioOutputOptionBuilder(String format) {
		super(new AudioOutputOption(format));
	}

	/**
	 * 从 Audio 对象创建构建器
	 *
	 * @param audio 音频信息对象
	 * @since 1.1.0
	 */
	public AudioOutputOptionBuilder(Audio audio) {
		super(new AudioOutputOption(audio));
	}

	/**
	 * 从 FFmpeg 帧抓取器创建构建器
	 *
	 * @param grabber FFmpeg 帧抓取器
	 * @throws IOException 读取音频信息时发生 I/O 错误
	 * @since 1.1.0
	 */
	public AudioOutputOptionBuilder(FFmpegFrameGrabber grabber) throws IOException {
		super(new AudioOutputOption(grabber));
	}

	/**
	 * 创建 WAV 格式音频输出选项构建器
	 * <p>使用 PCM_S16LE 编码器</p>
	 *
	 * @return WAV 格式构建器
	 * @since 1.1.0
	 */
	public static AudioOutputOptionBuilder wav() {
		return new AudioOutputOptionBuilder(FFmpegConstants.AUDIO_WAV_FORMAT)
			.codecId(avcodec.AV_CODEC_ID_PCM_S16LE);
	}

	/**
	 * 创建 FLAC 格式音频输出选项构建器
	 * <p>使用 FLAC 编码器</p>
	 *
	 * @return FLAC 格式构建器
	 * @since 1.1.0
	 */
	public static AudioOutputOptionBuilder flac() {
		return new AudioOutputOptionBuilder(FFmpegConstants.AUDIO_FLAC_FORMAT)
			.codecId(avcodec.AV_CODEC_ID_FLAC);
	}

	/**
	 * 创建 MP3 格式音频输出选项构建器
	 * <p>使用 MP3 编码器</p>
	 *
	 * @return MP3 格式构建器
	 * @since 1.1.0
	 */
	public static AudioOutputOptionBuilder mp3() {
		return new AudioOutputOptionBuilder(FFmpegConstants.AUDIO_MP3_FORMAT)
			.codecId(avcodec.AV_CODEC_ID_MP3);
	}

	/**
	 * 创建 Opus 格式音频输出选项构建器
	 * <p>使用 Opus 编码器</p>
	 *
	 * @return Opus 格式构建器
	 * @since 1.1.0
	 */
	public static AudioOutputOptionBuilder opus() {
		return new AudioOutputOptionBuilder(FFmpegConstants.AUDIO_OPUS_FORMAT)
			.codecId(avcodec.AV_CODEC_ID_OPUS);
	}

	/**
	 * 创建 AAC 格式音频输出选项构建器
	 * <p>使用 AAC 编码器</p>
	 *
	 * @return AAC 格式构建器
	 * @since 1.1.0
	 */
	public static AudioOutputOptionBuilder aac() {
		return new AudioOutputOptionBuilder(FFmpegConstants.AUDIO_AAC_FORMAT)
			.codecId(avcodec.AV_CODEC_ID_AAC);
	}

	/**
	 * 设置为 CD 标准采样率（44100 Hz）
	 *
	 * @return 构建器自身，用于链式调用
	 * @since 1.1.0
	 */
	public AudioOutputOptionBuilder cd() {
		this.outputOption.setSampleRate(FFmpegConstants.CD_STANDARD_SAMPLE_RATE);
		return this;
	}

	/**
	 * 设置为广播标准采样率（48000 Hz）
	 *
	 * @return 构建器自身，用于链式调用
	 * @since 1.1.0
	 */
	public AudioOutputOptionBuilder broadcast() {
		this.outputOption.setSampleRate(FFmpegConstants.BROADCAST_STANDARD_SAMPLE_RATE);
		return this;
	}

	/**
	 * 设置采样率
	 *
	 * @param sampleRate 采样率（Hz）
	 * @return 构建器自身，用于链式调用
	 * @since 1.1.0
	 */
	public AudioOutputOptionBuilder sampleRate(int sampleRate) {
		this.outputOption.setSampleRate(sampleRate);

		return this;
	}

	/**
	 * 设置为单声道
	 *
	 * @return 构建器自身，用于链式调用
	 * @since 1.1.0
	 */
	public AudioOutputOptionBuilder mono() {
		return channels(1);
	}

	/**
	 * 设置为立体声
	 *
	 * @return 构建器自身，用于链式调用
	 * @since 1.1.0
	 */
	public AudioOutputOptionBuilder stereo() {
		return channels(2);
	}

	/**
	 * 设置声道数
	 *
	 * @param channels 声道数
	 * @return 构建器自身，用于链式调用
	 * @since 1.1.0
	 */
	public AudioOutputOptionBuilder channels(int channels) {
		this.outputOption.setChannels(channels);

		return this;
	}

	/**
	 * 设置比特率
	 *
	 * @param bitrate 比特率（bps）
	 * @return 构建器自身，用于链式调用
	 * @since 1.1.0
	 */
	public AudioOutputOptionBuilder bitrate(int bitrate) {
		this.outputOption.setBitrate(bitrate);

		return this;
	}
}
