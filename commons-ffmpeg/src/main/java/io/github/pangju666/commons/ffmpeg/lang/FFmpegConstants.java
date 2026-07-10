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

import java.util.Set;

/**
 * FFmpeg 相关常量类
 * <p>
 * 包含 FFmpeg 处理中常用的常量，包括：
 * </p>
 * <ul>
 * <li>音频格式标识（WAV、FLAC、MP3、Opus、AAC、OGG、WMV）</li>
 * <li>视频格式标识（MP4、WebM、MKV、MOV、AVI、WMV、RM、OGG）</li>
 * <li>FFmpeg 滤镜标签（输入/输出流标签、视频/音频流标签）</li>
 * <li>滤镜分隔符（参数连接符、分支分隔符、参数分隔符）</li>
 * <li>音频/视频标准参数（采样率、帧率、声道数）</li>
 * <li>特殊格式集合（需要定位操作的输出格式）</li>
 * </ul>
 *
 * @author pangju666
 * @see <a href="https://ffmpeg.org/ffmpeg-formats.html">FFmpeg Formats Official Doc</a>
 * @see <a href="https://ffmpeg.org/ffmpeg-filters.html">FFmpeg Filters Official Doc</a>
 * @since 1.1.0
 */
public class FFmpegConstants {
	/**
	 * TTF 字体文件的 MIME 类型
	 * <p>用于文字水印功能中的字体文件识别</p>
	 *
	 * @since 1.1.0
	 */
	public static final String TTF_MIME_TYPE = "application/x-font-ttf";

	/**
	 * 单个输入流的滤镜标签
	 * <p>用于标识滤镜链的输入流，格式为 [in]</p>
	 *
	 * @since 1.1.0
	 */
	public static final String FILTER_SINGLE_INPUT_TAG = "[in]";

	/**
	 * 单个输出流的滤镜标签
	 * <p>用于标识滤镜链的输出流，格式为 [out]</p>
	 *
	 * @since 1.1.0
	 */
	public static final String FILTER_SINGLE_OUTPUT_TAG = "[out]";

	/**
	 * 视频输出流的滤镜标签
	 * <p>用于标识滤镜链中的视频输出流，格式为 [v]</p>
	 *
	 * @since 1.1.0
	 */
	public static final String FILTER_OUTPUT_VIDEO_TAG = "[v]";

	/**
	 * 音频输出流的滤镜标签
	 * <p>用于标识滤镜链中的音频输出流，格式为 [a]</p>
	 *
	 * @since 1.1.0
	 */
	public static final String FILTER_OUTPUT_AUDIO_TAG = "[a]";

	/**
	 * 滤镜参数之间的连接分隔符
	 * <p>用于连接同一滤镜内的多个参数，如 "param1=value1,param2=value2"</p>
	 *
	 * @since 1.1.0
	 */
	public static final String FILTER_CONCAT_SEPARATOR = ",";

	/**
	 * 滤镜分支之间的分隔符
	 * <p>用于分隔滤镜链中的不同滤镜分支，如 "filter1;filter2"</p>
	 *
	 * @since 1.1.0
	 */
	public static final String FILTER_BRANCH_SEPARATOR = ";";

	/**
	 * 滤镜内部参数的分隔符
	 * <p>用于分隔滤镜名称和参数，或参数名和参数值，如 "filter:param1=value1:param2=value2"</p>
	 *
	 * @since 1.1.0
	 */
	public static final String FILTER_ARG_SEPARATOR = ":";

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
	 * OGG 音频格式标识
	 * <p>开源音频容器格式，支持多种编码</p>
	 *
	 * @since 1.1.0
	 */
	public static final String AUDIO_OGG_FORMAT = "ogg";

	/**
	 * WMV 音频格式标识
	 * <p>Windows Media 音频格式，ASF 容器</p>
	 *
	 * @since 1.1.0
	 */
	public static final String AUDIO_WMV_FORMAT = "asf";

	/**
	 * AAC 音频格式标识
	 * <p>有损音频压缩格式，广泛用于流媒体和移动设备</p>
	 *
	 * @since 1.1.0
	 */
	public static final String AUDIO_AAC_FORMAT = "adts";

	/**
	 * OGG 视频格式标识
	 * <p>开源视频容器格式，支持多种编码</p>
	 *
	 * @since 1.1.0
	 */
	public static final String VIDEO_OGG_FORMAT = "ogg";

	/**
	 * MOV 视频格式标识
	 * <p>QuickTime 视频容器格式，Apple 系统标准</p>
	 *
	 * @since 1.1.0
	 */
	public static final String VIDEO_MOV_FORMAT = "mov";

	/**
	 * AVI 视频格式标识
	 * <p>经典视频容器格式，兼容性广泛</p>
	 *
	 * @since 1.1.0
	 */
	public static final String VIDEO_AVI_FORMAT = "avi";

	/**
	 * WMV 视频格式标识
	 * <p>Windows Media 视频格式，ASF 容器</p>
	 *
	 * @since 1.1.0
	 */
	public static final String VIDEO_WMV_FORMAT = "asf";

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
	 * RM 视频格式标识
	 * <p>RealMedia 视频格式，RealNetworks 开发</p>
	 *
	 * @since 1.1.0
	 */
	public static final String VIDEO_RM_FORMAT = "rm";

	/**
	 * Matroska (MKV) 视频格式标识
	 * <p>开源视频容器格式，支持多种音视频编码</p>
	 *
	 * @since 1.1.0
	 */
	public static final String VIDEO_MKV_FORMAT = "matroska";

	/**
	 * 需要定位操作的输出格式集合
	 * <p>这些格式作为输出时，在随机访问时需要先进行定位操作</p>
	 *
	 * @since 1.1.0
	 */
	public static final Set<String> REQUIRE_SEEK_OUTPUT_FORMATS = Set.of(
		VIDEO_MP4_FORMAT, VIDEO_MOV_FORMAT, VIDEO_AVI_FORMAT, VIDEO_RM_FORMAT,
		AUDIO_WAV_FORMAT, AUDIO_FLAC_FORMAT, AUDIO_MP3_FORMAT);

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

	/**
	 * 私有构造函数，防止实例化
	 *
	 * @since 1.1.0
	 */
	protected FFmpegConstants() {
	}
}
