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

package io.github.pangju666.commons.media.lang;

/**
 * 媒体相关常量类，提供常用的媒体格式标识和默认参数值
 * <p>
 * 该类包含音频、视频常用格式的单个常量，以及媒体处理的默认参数值，
 * 方便在媒体处理操作中统一引用标准配置。
 * <p>
 * <h3>单个格式常量</h3>
 * <ul>
 *   <li><b>音频格式</b>：{@link #AUDIO_WAV_FORMAT}、{@link #AUDIO_FLAC_FORMAT}、{@link #AUDIO_MP3_FORMAT}、{@link #AUDIO_OPUS_FORMAT}、{@link #AUDIO_AAC_FORMAT}</li>
 *   <li><b>视频格式</b>：{@link #VIDEO_MP4_FORMAT}、{@link #VIDEO_WEBM_FORMAT}、{@link #VIDEO_MKV_FORMAT}</li>
 * </ul>
 * <p>
 * <h3>默认参数值</h3>
 * <ul>
 *   <li>{@link #DEFAULT_VIDEO_FRAME_RATE}：默认视频帧率</li>
 *   <li>{@link #DEFAULT_AUDIO_CHANNELS}：默认音频声道数</li>
 *   <li>{@link #AUDIO_STANDARD_SAMPLE_RATE}：音频标准采样率</li>
 *   <li>{@link #VIDEO_STANDARD_SAMPLE_RATE}：视频标准采样率</li>
 * </ul>
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 使用单个格式常量
 * String audioFormat = MediaConstants.AUDIO_MP3_FORMAT;
 * String videoFormat = MediaConstants.VIDEO_MP4_FORMAT;
 *
 * // 使用默认参数值
 * int frameRate = MediaConstants.DEFAULT_VIDEO_FRAME_RATE;
 * int channels = MediaConstants.DEFAULT_AUDIO_CHANNELS;
 * int sampleRate = MediaConstants.AUDIO_STANDARD_SAMPLE_RATE;
 * }</pre>
 *
 * @author pangju666
 * @see <a href="https://ffmpeg.org/ffmpeg-formats.html">FFmpeg Formats Official Doc</a>
 * @since 1.1.0
 */
public class MediaConstants {
	/**
	 * WAV 音频格式标识
	 * <p>无损音频格式，标准 PCM 波形音频文件</p>
	 *
	 * @since 1.1.0
	 */
	public static final String AUDIO_WAV_FORMAT = "wav";

	/**
	 * FLAC 音频格式标识
	 * <p>无损音频压缩格式，文件体积较小，音质无损</p>
	 *
	 * @since 1.1.0
	 */
	public static final String AUDIO_FLAC_FORMAT = "flac";

	/**
	 * MP3 音频格式标识
	 * <p>有损音频压缩格式，兼容性广泛，文件体积小</p>
	 *
	 * @since 1.1.0
	 */
	public static final String AUDIO_MP3_FORMAT = "mp3";

	/**
	 * Opus 音频格式标识
	 * <p>有损音频压缩格式，适合网络传输，音质优秀</p>
	 *
	 * @since 1.1.0
	 */
	public static final String AUDIO_OPUS_FORMAT = "opus";

	/**
	 * AAC 音频格式标识
	 * <p>有损音频压缩格式，广泛用于流媒体和移动设备</p>
	 *
	 * @since 1.1.0
	 */
	public static final String AUDIO_AAC_FORMAT = "aac";

	/**
	 * MP4 视频格式标识
	 * <p>通用视频容器格式，支持多种编码，兼容性广泛</p>
	 *
	 * @since 1.1.0
	 */
	public static final String VIDEO_MP4_FORMAT = "mp4";

	/**
	 * WebM 视频格式标识
	 * <p>开源视频容器格式，适合网络流媒体</p>
	 *
	 * @since 1.1.0
	 */
	public static final String VIDEO_WEBM_FORMAT = "webm";

	/**
	 * Matroska (MKV) 视频格式标识
	 * <p>开源视频容器格式，支持多种音视频编码</p>
	 *
	 * @since 1.1.0
	 */
	public static final String VIDEO_MKV_FORMAT = "matroska";

	/**
	 * 默认视频帧率
	 * <p>30 FPS，标准视频帧率</p>
	 *
	 * @since 1.1.0
	 */
	public static final int DEFAULT_VIDEO_FRAME_RATE = 30;

	/**
	 * 默认音频声道数
	 * <p>2 声道，立体声</p>
	 *
	 * @since 1.1.0
	 */
	public static final int DEFAULT_AUDIO_CHANNELS = 2;

	/**
	 * 音频标准采样率
	 * <p>44100 Hz，CD 音质标准</p>
	 *
	 * @since 1.1.0
	 */
	public static final int AUDIO_STANDARD_SAMPLE_RATE = 44100;

	/**
	 * 视频标准采样率
	 * <p>48000 Hz，专业视频音频标准</p>
	 *
	 * @since 1.1.0
	 */
	public static final int VIDEO_STANDARD_SAMPLE_RATE = 48000;
}
