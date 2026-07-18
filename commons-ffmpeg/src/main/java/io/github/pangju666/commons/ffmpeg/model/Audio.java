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

import io.github.pangju666.commons.ffmpeg.utils.FFmpegUtils;
import org.apache.commons.lang3.Validate;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.time.Duration;
import java.util.Map;

/**
 * 音频信息记录类
 * <p>
 * 用于封装音频文件的元数据信息，包括格式、编解码器、采样率、声道数、比特率等。
 * 通过 {@link #parse(FFmpegFrameGrabber)} 静态方法从 FFmpeg 帧抓取器中解析音频信息。
 * </p>
 * <p>
 * <b>主要字段说明：</b>
 * </p>
 * <ul>
 *   <li>{@code format} - 音频格式（如 mp3、aac、flac 等）</li>
 *   <li>{@code metadata} - 音频元数据映射（如标题、艺术家、专辑等）</li>
 *   <li>{@code codecName} - 编解码器名称</li>
 *   <li>{@code codecId} - 编解码器 ID</li>
 *   <li>{@code duration} - 音频时长</li>
 *   <li>{@code sampleRate} - 采样率（Hz）</li>
 *   <li>{@code channels} - 声道数</li>
 *   <li>{@code bitrate} - 比特率（bps）</li>
 *   <li>{@code sampleFormat} - 采样格式</li>
 *   <li>{@code audioMetadata} - 音频专用元数据映射</li>
 * </ul>
 * <p>
 * <b>使用示例：</b>
 * </p>
 * <pre>{@code
 * FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("audio.mp3");
 * grabber.start();
 * Audio audio = Audio.parse(grabber);
 * System.out.println("采样率: " + audio.sampleRate());
 * System.out.println("声道数: " + audio.channels());
 * System.out.println("是否立体声: " + audio.isStereo());
 * grabber.close();
 * }</pre>
 *
 * @param format 音频格式
 * @param metadata 音频元数据映射
 * @param codecName 编解码器名称
 * @param codecId 编解码器 ID
 * @param duration 音频时长
 * @param sampleRate 采样率（Hz）
 * @param channels 声道数
 * @param bitrate 比特率（bps）
 * @param sampleFormat 采样格式
 * @param audioMetadata 音频专用元数据映射
 * @author pangju666
 * @since 2.1.0
 */
public record Audio(String format, Map<String, String> metadata, String codecName, int codecId, Duration duration,
                    int sampleRate, int channels, int bitrate, int sampleFormat, Map<String, String> audioMetadata) {
	/**
	 * 从 FFmpeg 帧抓取器解析音频信息
	 * <p>
	 * 如果抓取器未启动，会自动启动它。
	 * 如果资源不包含音频流，返回 null。
	 * </p>
	 *
	 * @param grabber FFmpeg 帧抓取器，不可为 null
	 * @return 音频信息对象，如果资源不包含音频流则返回 null
	 * @throws IllegalArgumentException 当 grabber 为 null 时抛出
	 * @throws FFmpegFrameGrabber.Exception 当抓取器启动失败时抛出
	 * @since 2.1.0
	 */
	public static Audio parse(FFmpegFrameGrabber grabber) throws FFmpegFrameGrabber.Exception {
		Validate.notNull(grabber, "grabber 不可为 null");

		if (FFmpegUtils.isNotStarted(grabber)) {
			grabber.start();
		}

		if (grabber.hasAudio()) {
			return new Audio(grabber.getFormat(), Map.copyOf(grabber.getMetadata()),
				grabber.getAudioCodecName(), grabber.getAudioCodec(),
				Duration.ofNanos(grabber.getLengthInTime() * 1000), grabber.getSampleRate(),
				grabber.getAudioChannels(), grabber.getAudioBitrate(), grabber.getSampleFormat(),
				Map.copyOf(grabber.getAudioMetadata()));
		} else {
			return null;
		}
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
}
