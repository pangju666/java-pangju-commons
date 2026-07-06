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

package io.github.pangju666.commons.ffmpeg.lang;

/**
 * FFmpeg 相关常量类
 * <p>
 * 包含 FFmpeg 处理中常用的常量，如格式标识、滤镜标签、分隔符等。
 * </p>
 *
 * @author pangju666
 * @see <a href="https://ffmpeg.org/ffmpeg-formats.html">FFmpeg Formats Official Doc</a>
 * @since 2.1.0
 */
public class FFmpegConstants {
	/**
	 * TTF 字体文件的 MIME 类型
	 *
	 * @since 2.1.0
	 */
	public static final String TTF_MIME_TYPE = "application/x-font-ttf";

	/**
	 * 单个输入流的滤镜标签
	 *
	 * @since 2.1.0
	 */
	public static final String FILTER_SINGLE_INPUT_TAG = "[in]";

	/**
	 * 单个输出流的滤镜标签
	 *
	 * @since 2.1.0
	 */
	public static final String FILTER_SINGLE_OUTPUT_TAG = "[out]";

	/**
	 * 视频输出流的滤镜标签
	 *
	 * @since 2.1.0
	 */
	public static final String FILTER_OUTPUT_VIDEO_TAG = "[v]";

	/**
	 * 音频输出流的滤镜标签
	 *
	 * @since 2.1.0
	 */
	public static final String FILTER_OUTPUT_AUDIO_TAG = "[a]";

	/**
	 * 滤镜参数之间的连接分隔符
	 *
	 * @since 2.1.0
	 */
	public static final String FILTER_CONCAT_SEPARATOR = ",";

	/**
	 * 滤镜分支之间的分隔符
	 *
	 * @since 2.1.0
	 */
	public static final String FILTER_BRANCH_SEPARATOR = ";";

	/**
	 * 滤镜内部参数的分隔符
	 *
	 * @since 2.1.0
	 */
	public static final String FILTER_ARG_SEPARATOR = ":";

	/**
	 * WAV 音频格式标识
	 * <p>无损音频格式，标准 PCM 波形音频文件</p>
	 *
	 * @since 2.1.0
	 */
	public static final String AUDIO_WAV_FORMAT = "wav";

	/**
	 * FLAC 音频格式标识
	 * <p>无损音频压缩格式，文件体积较小，音质无损</p>
	 *
	 * @since 2.1.0
	 */
	public static final String AUDIO_FLAC_FORMAT = "flac";

	/**
	 * MP3 音频格式标识
	 * <p>有损音频压缩格式，兼容性广泛，文件体积小</p>
	 *
	 * @since 2.1.0
	 */
	public static final String AUDIO_MP3_FORMAT = "mp3";

	/**
	 * Opus 音频格式标识
	 * <p>有损音频压缩格式，适合网络传输，音质优秀</p>
	 *
	 * @since 2.1.0
	 */
	public static final String AUDIO_OPUS_FORMAT = "opus";

	/**
	 * AAC 音频格式标识
	 * <p>有损音频压缩格式，广泛用于流媒体和移动设备</p>
	 *
	 * @since 2.1.0
	 */
	public static final String AUDIO_AAC_FORMAT = "aac";

	/**
	 * MP4 视频格式标识
	 * <p>通用视频容器格式，支持多种编码，兼容性广泛</p>
	 *
	 * @since 2.1.0
	 */
	public static final String VIDEO_MP4_FORMAT = "mp4";

	/**
	 * WebM 视频格式标识
	 * <p>开源视频容器格式，适合网络流媒体</p>
	 *
	 * @since 2.1.0
	 */
	public static final String VIDEO_WEBM_FORMAT = "webm";

	/**
	 * Matroska (MKV) 视频格式标识
	 * <p>开源视频容器格式，支持多种音视频编码</p>
	 *
	 * @since 2.1.0
	 */
	public static final String VIDEO_MKV_FORMAT = "matroska";

	/**
	 * 默认视频帧率
	 * <p>30 FPS，标准视频帧率</p>
	 *
	 * @since 2.1.0
	 */
	public static final int DEFAULT_VIDEO_FRAME_RATE = 30;

	/**
	 * 默认音频声道数
	 * <p>2 声道，立体声</p>
	 *
	 * @since 2.1.0
	 */
	public static final int DEFAULT_AUDIO_CHANNELS = 2;

	/**
	 * 音频标准采样率
	 * <p>44100 Hz，CD 音质标准</p>
	 *
	 * @since 2.1.0
	 */
	public static final int AUDIO_STANDARD_SAMPLE_RATE = 44100;

	/**
	 * 视频标准采样率
	 * <p>48000 Hz，专业视频音频标准</p>
	 *
	 * @since 2.1.0
	 */
	public static final int VIDEO_STANDARD_SAMPLE_RATE = 48000;

	/**
	 * 私有构造函数，防止实例化
	 *
	 * @since 2.1.0
	 */
	protected FFmpegConstants() {
	}
}
