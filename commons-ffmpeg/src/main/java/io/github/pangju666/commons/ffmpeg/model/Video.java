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
import java.util.Objects;

/**
 * 视频媒体对象，封装了视频文件的核心信息
 * <p>
 * 该类继承自{@link Media}，专门用于表示视频文件，包含视频特有属性如帧率、分辨率、码率、时长，以及内置音频轨道信息。
 * 采用不可变对象设计，所有属性为 final，确保线程安全。
 * </p>
 * <h3>核心特性</h3>
 * <ul>
 *   <li>支持从多种来源解析视频信息（File、byte[]、InputStream、FFmpegFrameGrabber）</li>
 *   <li>封装视频特有属性：分辨率、帧率、码率、时长</li>
 *   <li>内置音频轨道信息，支持音视频一体化处理</li>
 *   <li>不可变对象设计，线程安全</li>
 *   <li>提供 Fluent Builder 模式，支持链式调用</li>
 *   <li>提供预定义标准视频配置（MP4、WEBM、MKV多种分辨率和码率等级）</li>
 * </ul>
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 从文件解析视频
 * Video video = Video.builder(new File("video.mp4")).build();
 *
 * // 使用预定义配置
 * Video standardVideo = Video.MP4_1080P;
 *
 * // 手动构建视频
 * Video customVideo = Video.builder()
 *     .mp4WithH264()
 *     .resolution1080p()
 *     .frameRate(30)
 *     .bitrate(6000000)
 *     .audio(Video.AUDIO_AAC_1080P)
 *     .build();
 *
 * // 基于现有视频修改
 * Video modifiedVideo = Video.builder(existingVideo)
 *     .bitrate(8000000)
 *     .build();
 * }</pre>
 *
 * @author pangju666
 * @see Media
 * @see Audio
 * @see Video.Builder
 * @see FFmpegFrameGrabber
 * @since 1.1.0
 */
public class Video extends Media {
	/**
	 * 标准AAC音频配置 - 适用于480P视频
	 * <p>AAC编码，96kbps码率，默认采样率44.1kHz，立体声</p>
	 *
	 * @since 1.1.0
	 */
	public static final Audio AUDIO_AAC_480P = Audio.builder()
		.aac()
		.bitrate(96_000)
		.build();

	/**
	 * 标准AAC音频配置 - 适用于720P视频
	 * <p>AAC编码，128kbps码率，默认采样率44.1kHz，立体声</p>
	 *
	 * @since 1.1.0
	 */
	public static final Audio AUDIO_AAC_720P = Audio.builder()
		.aac()
		.bitrate(128_000)
		.build();

	/**
	 * 标准AAC音频配置 - 适用于1080P视频
	 * <p>AAC编码，192kbps码率，48kHz采样率，立体声</p>
	 *
	 * @since 1.1.0
	 */
	public static final Audio AUDIO_AAC_1080P = Audio.builder()
		.aac()
		.sampleRate(FFmpegConstants.VIDEO_STANDARD_SAMPLE_RATE)
		.bitrate(192_000)
		.build();

	/**
	 * 标准AAC音频配置 - 适用于2K视频
	 * <p>AAC编码，256kbps码率，48kHz采样率，立体声</p>
	 *
	 * @since 1.1.0
	 */
	public static final Audio AUDIO_AAC_2K = Audio.builder()
		.aac()
		.sampleRate(FFmpegConstants.VIDEO_STANDARD_SAMPLE_RATE)
		.bitrate(256_000)
		.build();

	/**
	 * 标准OPUS音频配置 - 适用于480P视频
	 * <p>OPUS编码，80kbps码率，默认采样率44.1kHz，立体声</p>
	 *
	 * @since 1.1.0
	 */
	public static final Audio AUDIO_OPUS_480P = Audio.builder()
		.opus()
		.bitrate(80_000)
		.build();

	/**
	 * 标准OPUS音频配置 - 适用于720P视频
	 * <p>OPUS编码，96kbps码率，默认采样率44.1kHz，立体声</p>
	 *
	 * @since 1.1.0
	 */
	public static final Audio AUDIO_OPUS_720P = Audio.builder()
		.opus()
		.bitrate(96_000)
		.build();

	/**
	 * 标准OPUS音频配置 - 适用于1080P视频
	 * <p>OPUS编码，128kbps码率，48kHz采样率，立体声</p>
	 *
	 * @since 1.1.0
	 */
	public static final Audio AUDIO_OPUS_1080P = Audio.builder()
		.opus()
		.sampleRate(FFmpegConstants.VIDEO_STANDARD_SAMPLE_RATE)
		.bitrate(128_000)
		.build();

	/**
	 * 标准OPUS音频配置 - 适用于2K视频
	 * <p>OPUS编码，192kbps码率，48kHz采样率，立体声</p>
	 *
	 * @since 1.1.0
	 */
	public static final Audio AUDIO_OPUS_2K = Audio.builder()
		.opus()
		.sampleRate(FFmpegConstants.VIDEO_STANDARD_SAMPLE_RATE)
		.bitrate(192_000)
		.build();

	/**
	 * 标准FLAC无损音频配置 - 适用于视频
	 * <p>FLAC编码，48kHz采样率，立体声</p>
	 *
	 * @since 1.1.0
	 */
	public static final Audio AUDIO_FLAC_VIDEO = Audio.builder()
		.flac()
		.sampleRate(FFmpegConstants.VIDEO_STANDARD_SAMPLE_RATE)
		.build();

	/**
	 * 标准MP4 480P视频配置（标准码率）
	 * <p>MP4容器+H264编码，640×480，1.5Mbps，30fps，配AAC 480P音频
	 *
	 * @since 1.1.0
	 */
	public static final Video MP4_480P = Video.builder()
		.mp4WithH264()
		.resolution480p()
		.bitrate(1500_000)
		.audio(AUDIO_AAC_480P)
		.build();

	/**
	 * 标准MP4 720P视频配置（标准码率）
	 * <p>MP4容器+H264编码，1280×720，3Mbps，30fps，配AAC 720P音频
	 *
	 * @since 1.1.0
	 */
	public static final Video MP4_720P = Video.builder()
		.mp4WithH264()
		.resolution720p()
		.bitrate(3000_000)
		.audio(AUDIO_AAC_720P)
		.build();

	/**
	 * 标准MP4 1080P视频配置（标准码率）
	 * <p>MP4容器+H264编码，1920×1080，6Mbps，30fps，配AAC 1080P音频
	 *
	 * @since 1.1.0
	 */
	public static final Video MP4_1080P = Video.builder()
		.mp4WithH264()
		.resolution1080p()
		.bitrate(6000_000)
		.audio(AUDIO_AAC_1080P)
		.build();

	/**
	 * 标准MP4 2K视频配置（标准码率）
	 * <p>MP4容器+H264编码，2560×1440，12Mbps，30fps，配AAC 2K音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MP4_2K = Video.builder()
		.mp4WithH264()
		.resolution2k()
		.bitrate(12000_000)
		.audio(AUDIO_AAC_2K)
		.build();

	/**
	 * 标准MP4 480P竖屏视频配置（标准码率）
	 * <p>MP4容器+H264编码，480×640，1.5Mbps，30fps，配AAC 480P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MP4_480P_VERTICAL = Video.builder()
		.mp4WithH264()
		.resolution480pVertical()
		.bitrate(1500_000)
		.audio(AUDIO_AAC_480P)
		.build();

	/**
	 * 标准MP4 720P竖屏视频配置（标准码率）
	 * <p>MP4容器+H264编码，720×1280，3Mbps，30fps，配AAC 720P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MP4_720P_VERTICAL = Video.builder()
		.mp4WithH264()
		.resolution720pVertical()
		.bitrate(3000_000)
		.audio(AUDIO_AAC_720P)
		.build();

	/**
	 * 标准MP4 1080P竖屏视频配置（标准码率）
	 * <p>MP4容器+H264编码，1080×1920，6Mbps，30fps，配AAC 1080P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MP4_1080P_VERTICAL = Video.builder()
		.mp4WithH264()
		.resolution1080pVertical()
		.bitrate(6000_000)
		.audio(AUDIO_AAC_1080P)
		.build();

	/**
	 * 标准MP4 2K竖屏视频配置（标准码率）
	 * <p>MP4容器+H264编码，1440×2560，12Mbps，30fps，配AAC 2K音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MP4_2K_VERTICAL = Video.builder()
		.mp4WithH264()
		.resolution2kVertical()
		.bitrate(12000_000)
		.audio(AUDIO_AAC_2K)
		.build();

	/**
	 * 标准MP4 480P视频配置（低码率）
	 * <p>MP4容器+H264编码，640×480，1Mbps，30fps，配AAC 480P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MP4_480P_LOW = Video.builder()
		.mp4WithH264()
		.resolution480p()
		.bitrate(1000_000)
		.audio(AUDIO_AAC_480P)
		.build();

	/**
	 * 标准MP4 720P视频配置（低码率）
	 * <p>MP4容器+H264编码，1280×720，2Mbps，30fps，配AAC 720P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MP4_720P_LOW = Video.builder()
		.mp4WithH264()
		.resolution720p()
		.bitrate(2000_000)
		.audio(AUDIO_AAC_720P)
		.build();

	/**
	 * 标准MP4 1080P视频配置（低码率）
	 * <p>MP4容器+H264编码，1920×1080，4Mbps，30fps，配AAC 1080P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MP4_1080P_LOW = Video.builder()
		.mp4WithH264()
		.resolution1080p()
		.bitrate(4000_000)
		.audio(AUDIO_AAC_1080P)
		.build();

	/**
	 * 标准MP4 2K视频配置（低码率）
	 * <p>MP4容器+H264编码，2560×1440，8Mbps，30fps，配AAC 2K音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MP4_2K_LOW = Video.builder()
		.mp4WithH264()
		.resolution2k()
		.bitrate(8000_000)
		.audio(AUDIO_AAC_2K)
		.build();

	/**
	 * 标准MP4 480P竖屏视频配置（低码率）
	 * <p>MP4容器+H264编码，480×640，1Mbps，30fps，配AAC 480P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MP4_480P_LOW_VERTICAL = Video.builder()
		.mp4WithH264()
		.resolution480pVertical()
		.bitrate(1000_000)
		.audio(AUDIO_AAC_480P)
		.build();

	/**
	 * 标准MP4 720P竖屏视频配置（低码率）
	 * <p>MP4容器+H264编码，720×1280，2Mbps，30fps，配AAC 720P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MP4_720P_LOW_VERTICAL = Video.builder()
		.mp4WithH264()
		.resolution720pVertical()
		.bitrate(2000_000)
		.audio(AUDIO_AAC_720P)
		.build();

	/**
	 * 标准MP4 1080P竖屏视频配置（低码率）
	 * <p>MP4容器+H264编码，1080×1920，4Mbps，30fps，配AAC 1080P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MP4_1080P_LOW_VERTICAL = Video.builder()
		.mp4WithH264()
		.resolution1080pVertical()
		.bitrate(4000_000)
		.audio(AUDIO_AAC_1080P)
		.build();

	/**
	 * 标准MP4 2K竖屏视频配置（低码率）
	 * <p>MP4容器+H264编码，1440×2560，8Mbps，30fps，配AAC 2K音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MP4_2K_LOW_VERTICAL = Video.builder()
		.mp4WithH264()
		.resolution2kVertical()
		.bitrate(8000_000)
		.audio(AUDIO_AAC_2K)
		.build();

	/**
	 * 标准MP4 480P视频配置（高码率）
	 * <p>MP4容器+H264编码，640×480，2Mbps，30fps，配AAC 480P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MP4_480P_HIGH = Video.builder()
		.mp4WithH264()
		.resolution480p()
		.bitrate(2000_000)
		.audio(AUDIO_AAC_480P)
		.build();

	/**
	 * 标准MP4 720P视频配置（高码率）
	 * <p>MP4容器+H264编码，1280×720，4Mbps，30fps，配AAC 720P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MP4_720P_HIGH = Video.builder()
		.mp4WithH264()
		.resolution720p()
		.bitrate(4000_000)
		.audio(AUDIO_AAC_720P)
		.build();

	/**
	 * 标准MP4 1080P视频配置（高码率）
	 * <p>MP4容器+H264编码，1920×1080，8Mbps，30fps，配AAC 1080P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MP4_1080P_HIGH = Video.builder()
		.mp4WithH264()
		.resolution1080p()
		.bitrate(8000_000)
		.audio(AUDIO_AAC_1080P)
		.build();

	/**
	 * 标准MP4 2K视频配置（高码率）
	 * <p>MP4容器+H264编码，2560×1440，16Mbps，30fps，配AAC 2K音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MP4_2K_HIGH = Video.builder()
		.mp4WithH264()
		.resolution2k()
		.bitrate(16000_000)
		.audio(AUDIO_AAC_2K)
		.build();

	/**
	 * 标准MP4 480P竖屏视频配置（高码率）
	 * <p>MP4容器+H264编码，480×640，2Mbps，30fps，配AAC 480P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MP4_480P_HIGH_VERTICAL = Video.builder()
		.mp4WithH264()
		.resolution480pVertical()
		.bitrate(2000_000)
		.audio(AUDIO_AAC_480P)
		.build();

	/**
	 * 标准MP4 720P竖屏视频配置（高码率）
	 * <p>MP4容器+H264编码，720×1280，4Mbps，30fps，配AAC 720P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MP4_720P_HIGH_VERTICAL = Video.builder()
		.mp4WithH264()
		.resolution720pVertical()
		.bitrate(4000_000)
		.audio(AUDIO_AAC_720P)
		.build();

	/**
	 * 标准MP4 1080P竖屏视频配置（高码率）
	 * <p>MP4容器+H264编码，1080×1920，8Mbps，30fps，配AAC 1080P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MP4_1080P_HIGH_VERTICAL = Video.builder()
		.mp4WithH264()
		.resolution1080pVertical()
		.bitrate(8000_000)
		.audio(AUDIO_AAC_1080P)
		.build();

	/**
	 * 标准MP4 2K竖屏视频配置（高码率）
	 * <p>MP4容器+H264编码，1440×2560，16Mbps，30fps，配AAC 2K音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MP4_2K_HIGH_VERTICAL = Video.builder()
		.mp4WithH264()
		.resolution2kVertical()
		.bitrate(16000_000)
		.audio(AUDIO_AAC_2K)
		.build();

	/**
	 * 标准WEBM 480P视频配置（标准码率）
	 * <p>WEBM容器+VP9编码，640×480，1.3Mbps，30fps，配OPUS 480P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video WEBM_480P = Video.builder()
		.webmWithVP9()
		.resolution480p()
		.bitrate(1300_000)
		.audio(AUDIO_OPUS_480P)
		.build();

	/**
	 * 标准WEBM 720P视频配置（标准码率）
	 * <p>WEBM容器+VP9编码，1280×720，2.6Mbps，30fps，配OPUS 720P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video WEBM_720P = Video.builder()
		.webmWithVP9()
		.resolution720p()
		.bitrate(2600_000)
		.audio(AUDIO_OPUS_720P)
		.build();

	/**
	 * 标准WEBM 1080P视频配置（标准码率）
	 * <p>WEBM容器+VP9编码，1920×1080，5.2Mbps，30fps，配OPUS 1080P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video WEBM_1080P = Video.builder()
		.webmWithVP9()
		.resolution1080p()
		.bitrate(5200_000)
		.audio(AUDIO_OPUS_1080P)
		.build();

	/**
	 * 标准WEBM 2K视频配置（标准码率）
	 * <p>WEBM容器+VP9编码，2560×1440，10.5Mbps，30fps，配OPUS 2K音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video WEBM_2K = Video.builder()
		.webmWithVP9()
		.resolution2k()
		.bitrate(10500_000)
		.audio(AUDIO_OPUS_2K)
		.build();

	/**
	 * 标准WEBM 480P竖屏视频配置（标准码率）
	 * <p>WEBM容器+VP9编码，480×640，1.3Mbps，30fps，配OPUS 480P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video WEBM_480P_VERTICAL = Video.builder()
		.webmWithVP9()
		.resolution480pVertical()
		.bitrate(1300_000)
		.audio(AUDIO_OPUS_480P)
		.build();

	/**
	 * 标准WEBM 720P竖屏视频配置（标准码率）
	 * <p>WEBM容器+VP9编码，720×1280，2.6Mbps，30fps，配OPUS 720P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video WEBM_720P_VERTICAL = Video.builder()
		.webmWithVP9()
		.resolution720pVertical()
		.bitrate(2600_000)
		.audio(AUDIO_OPUS_720P)
		.build();

	/**
	 * 标准WEBM 1080P竖屏视频配置（标准码率）
	 * <p>WEBM容器+VP9编码，1080×1920，5.2Mbps，30fps，配OPUS 1080P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video WEBM_1080P_VERTICAL = Video.builder()
		.webmWithVP9()
		.resolution1080pVertical()
		.bitrate(5200_000)
		.audio(AUDIO_OPUS_1080P)
		.build();

	/**
	 * 标准WEBM 2K竖屏视频配置（标准码率）
	 * <p>WEBM容器+VP9编码，1440×2560，10.5Mbps，30fps，配OPUS 2K音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video WEBM_2K_VERTICAL = Video.builder()
		.webmWithVP9()
		.resolution2kVertical()
		.bitrate(10500_000)
		.audio(AUDIO_OPUS_2K)
		.build();

	/**
	 * 标准WEBM 480P视频配置（低码率）
	 * <p>WEBM容器+VP9编码，640×480，750kbps，30fps，配OPUS 480P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video WEBM_480P_LOW = Video.builder()
		.webmWithVP9()
		.resolution480p()
		.bitrate(750_000)
		.audio(AUDIO_OPUS_480P)
		.build();

	/**
	 * 标准WEBM 720P视频配置（低码率）
	 * <p>WEBM容器+VP9编码，1280×720，1.5Mbps，30fps，配OPUS 720P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video WEBM_720P_LOW = Video.builder()
		.webmWithVP9()
		.resolution720p()
		.bitrate(1500_000)
		.audio(AUDIO_OPUS_720P)
		.build();

	/**
	 * 标准WEBM 1080P视频配置（低码率）
	 * <p>WEBM容器+VP9编码，1920×1080，3Mbps，30fps，配OPUS 1080P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video WEBM_1080P_LOW = Video.builder()
		.webmWithVP9()
		.resolution1080p()
		.bitrate(3000_000)
		.audio(AUDIO_OPUS_1080P)
		.build();

	/**
	 * 标准WEBM 2K视频配置（低码率）
	 * <p>WEBM容器+VP9编码，2560×1440，6Mbps，30fps，配OPUS 2K音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video WEBM_2K_LOW = Video.builder()
		.webmWithVP9()
		.resolution2k()
		.bitrate(6000_000)
		.audio(AUDIO_OPUS_2K)
		.build();

	/**
	 * 标准WEBM 480P竖屏视频配置（低码率）
	 * <p>WEBM容器+VP9编码，480×640，750kbps，30fps，配OPUS 480P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video WEBM_480P_LOW_VERTICAL = Video.builder()
		.webmWithVP9()
		.resolution480pVertical()
		.bitrate(750_000)
		.audio(AUDIO_OPUS_480P)
		.build();

	/**
	 * 标准WEBM 720P竖屏视频配置（低码率）
	 * <p>WEBM容器+VP9编码，720×1280，1.5Mbps，30fps，配OPUS 720P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video WEBM_720P_LOW_VERTICAL = Video.builder()
		.webmWithVP9()
		.resolution720pVertical()
		.bitrate(1500_000)
		.audio(AUDIO_OPUS_720P)
		.build();

	/**
	 * 标准WEBM 1080P竖屏视频配置（低码率）
	 * <p>WEBM容器+VP9编码，1080×1920，3Mbps，30fps，配OPUS 1080P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video WEBM_1080P_LOW_VERTICAL = Video.builder()
		.webmWithVP9()
		.resolution1080pVertical()
		.bitrate(3000_000)
		.audio(AUDIO_OPUS_1080P)
		.build();

	/**
	 * 标准WEBM 2K竖屏视频配置（低码率）
	 * <p>WEBM容器+VP9编码，1440×2560，6Mbps，30fps，配OPUS 2K音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video WEBM_2K_LOW_VERTICAL = Video.builder()
		.webmWithVP9()
		.resolution2kVertical()
		.bitrate(6000_000)
		.audio(AUDIO_OPUS_2K)
		.build();

	/**
	 * 标准WEBM 480P视频配置（高码率）
	 * <p>WEBM容器+VP9编码，640×480，1.8Mbps，30fps，配OPUS 480P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video WEBM_480P_HIGH = Video.builder()
		.webmWithVP9()
		.resolution480p()
		.bitrate(1800_000)
		.audio(AUDIO_OPUS_480P)
		.build();

	/**
	 * 标准WEBM 720P视频配置（高码率）
	 * <p>WEBM容器+VP9编码，1280×720，3.5Mbps，30fps，配OPUS 720P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video WEBM_720P_HIGH = Video.builder()
		.webmWithVP9()
		.resolution720p()
		.bitrate(3500_000)
		.audio(AUDIO_OPUS_720P)
		.build();

	/**
	 * 标准WEBM 1080P视频配置（高码率）
	 * <p>WEBM容器+VP9编码，1920×1080，7Mbps，30fps，配OPUS 1080P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video WEBM_1080P_HIGH = Video.builder()
		.webmWithVP9()
		.resolution1080p()
		.bitrate(7000_000)
		.audio(AUDIO_OPUS_1080P)
		.build();

	/**
	 * 标准WEBM 2K视频配置（高码率）
	 * <p>WEBM容器+VP9编码，2560×1440，14Mbps，30fps，配OPUS 2K音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video WEBM_2K_HIGH = Video.builder()
		.webmWithVP9()
		.resolution2k()
		.bitrate(14000_000)
		.audio(AUDIO_OPUS_2K)
		.build();

	/**
	 * 标准WEBM 480P竖屏视频配置（高码率）
	 * <p>WEBM容器+VP9编码，480×640，1.8Mbps，30fps，配OPUS 480P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video WEBM_480P_HIGH_VERTICAL = Video.builder()
		.webmWithVP9()
		.resolution480pVertical()
		.bitrate(1800_000)
		.audio(AUDIO_OPUS_480P)
		.build();

	/**
	 * 标准WEBM 720P竖屏视频配置（高码率）
	 * <p>WEBM容器+VP9编码，720×1280，3.5Mbps，30fps，配OPUS 720P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video WEBM_720P_HIGH_VERTICAL = Video.builder()
		.webmWithVP9()
		.resolution720pVertical()
		.bitrate(3500_000)
		.audio(AUDIO_OPUS_720P)
		.build();

	/**
	 * 标准WEBM 1080P竖屏视频配置（高码率）
	 * <p>WEBM容器+VP9编码，1080×1920，7Mbps，30fps，配OPUS 1080P音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video WEBM_1080P_HIGH_VERTICAL = Video.builder()
		.webmWithVP9()
		.resolution1080pVertical()
		.bitrate(7000_000)
		.audio(AUDIO_OPUS_1080P)
		.build();

	/**
	 * 标准WEBM 2K竖屏视频配置（高码率）
	 * <p>WEBM容器+VP9编码，1440×2560，14Mbps，30fps，配OPUS 2K音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video WEBM_2K_HIGH_VERTICAL = Video.builder()
		.webmWithVP9()
		.resolution2kVertical()
		.bitrate(14000_000)
		.audio(AUDIO_OPUS_2K)
		.build();

	/**
	 * 标准MKV 480P视频配置
	 * <p>MKV容器+H265编码，640×480，900kbps，30fps，配FLAC无损音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MKV_480P = Video.builder()
		.mkvWithH265()
		.resolution480p()
		.bitrate(900_000)
		.audio(Audio.FLAC)
		.build();

	/**
	 * 标准MKV 720P视频配置
	 * <p>MKV容器+H265编码，1280×720，1.8Mbps，30fps，配FLAC无损音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MKV_720P = Video.builder()
		.mkvWithH265()
		.resolution720p()
		.bitrate(1800_000)
		.audio(Audio.FLAC)
		.build();

	/**
	 * 标准MKV 1080P视频配置
	 * <p>MKV容器+H265编码，1920×1080，3.6Mbps，30fps，配FLAC无损音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MKV_1080P = Video.builder()
		.mkvWithH265()
		.resolution1080p()
		.bitrate(3600_000)
		.audio(AUDIO_FLAC_VIDEO)
		.build();

	/**
	 * 标准MKV 2K视频配置
	 * <p>MKV容器+H265编码，2560×1440，7.2Mbps，30fps，配FLAC无损音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MKV_2K = Video.builder()
		.mkvWithH265()
		.resolution2k()
		.bitrate(7200_000)
		.audio(AUDIO_FLAC_VIDEO)
		.build();

	/**
	 * 标准MKV 480P竖屏视频配置
	 * <p>MKV容器+H265编码，480×640，900kbps，30fps，配FLAC无损音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MKV_480P_VERTICAL = Video.builder()
		.mkvWithH265()
		.resolution480pVertical()
		.bitrate(900_000)
		.audio(Audio.FLAC)
		.build();

	/**
	 * 标准MKV 720P竖屏视频配置
	 * <p>MKV容器+H265编码，720×1280，1.8Mbps，30fps，配FLAC无损音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MKV_720P_VERTICAL = Video.builder()
		.mkvWithH265()
		.resolution720pVertical()
		.bitrate(1800_000)
		.audio(Audio.FLAC)
		.build();

	/**
	 * 标准MKV 1080P竖屏视频配置
	 * <p>MKV容器+H265编码，1080×1920，3.6Mbps，30fps，配FLAC无损音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MKV_1080P_VERTICAL = Video.builder()
		.mkvWithH265()
		.resolution1080pVertical()
		.bitrate(3600_000)
		.audio(AUDIO_FLAC_VIDEO)
		.build();

	/**
	 * 标准MKV 2K竖屏视频配置
	 * <p>MKV容器+H265编码，1440×2560，7.2Mbps，30fps，配FLAC无损音频</p>
	 *
	 * @since 1.1.0
	 */
	public static final Video MKV_2K_VERTICAL = Video.builder()
		.mkvWithH265()
		.resolution2kVertical()
		.bitrate(7200_000)
		.audio(AUDIO_FLAC_VIDEO)
		.build();

	/**
	 * 视频总播放时长
	 * <p>null / {@link Duration#ZERO} 代表无有效时长（如实时流、直播流）</p>
	 *
	 * @since 1.1.0
	 */
	protected final Duration duration;

	/**
	 * 视频帧率，单位 fps（帧/秒）
	 * <p>常见取值：24、25、30、60，数值越高画面越流畅、体积越大</p>
	 *
	 * @since 1.1.0
	 */
	protected final double frameRate;

	/**
	 * 视频宽度，单位像素
	 * <p>常见分辨率：1920（1080p）、1280（720p）、3840（4K）</p>
	 *
	 * @since 1.1.0
	 */
	protected final int width;

	/**
	 * 视频高度，单位像素
	 * <p>常见分辨率：1080（1080p）、720（720p）、2160（4K）</p>
	 *
	 * @since 1.1.0
	 */
	protected final int height;

	/**
	 * 视频码率，单位 bps
	 * <p>码率越高画质越好、体积越大，需根据分辨率和帧率合理设置</p>
	 *
	 * @since 1.1.0
	 */
	protected final int bitrate;

	/**
	 * 视频中的音频信息
	 * <p>null 表示视频无音频轨道</p>
	 *
	 * @since 1.1.0
	 */
	protected final Audio audio;

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
	 * 创建新的视频构建器
	 *
	 * @return 空的Video.Builder实例
	 * @since 1.1.0
	 */
	public static Video.Builder builder() {
		return new Video.Builder();
	}

	/**
	 * 基于现有视频对象创建构建器
	 * <p>新构建器会复制现有视频的所有属性，方便进行修改</p>
	 *
	 * @param video 源视频对象，不可为null
	 * @return 基于现有视频的Video.Builder实例
	 * @throws IllegalArgumentException 当video为null时抛出
	 * @since 1.1.0
	 */
	public static Video.Builder builder(Video video) {
		return new Video.Builder(video);
	}

	/**
	 * 从FFmpegFrameGrabber创建构建器
	 * <p>会自动解析grabber中的视频信息</p>
	 *
	 * @param grabber FFmpeg帧抓取器，不可为null
	 * @return 已解析的Video.Builder实例
	 * @throws IllegalArgumentException     当grabber为null时抛出
	 * @throws FFmpegFrameGrabber.Exception 当解析失败时抛出
	 * @since 1.1.0
	 */
	public static Video.Builder builder(FFmpegFrameGrabber grabber) throws FFmpegFrameGrabber.Exception {
		return new Video.Builder().parse(grabber);
	}

	/**
	 * 从文件创建构建器
	 * <p>会自动解析文件中的视频信息</p>
	 *
	 * @param file 视频文件，不可为null且必须是有效文件
	 * @return 已解析的Video.Builder实例
	 * @throws IllegalArgumentException 当file为null或无效时抛出
	 * @throws IOException              当文件读取失败时抛出
	 * @since 1.1.0
	 */
	public static Video.Builder builder(File file) throws IOException {
		FileUtils.checkFile(file, "file 不可为 null");

		try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file)) {
			grabber.start();
			return new Video.Builder().parse(grabber);
		}
	}

	/**
	 * 从字节数组创建构建器
	 * <p>会自动解析字节数组中的视频信息</p>
	 *
	 * @param bytes 视频字节数组，不可为null
	 * @return 已解析的Video.Builder实例
	 * @throws IllegalArgumentException 当bytes为null时抛出
	 * @throws IOException              当解析失败时抛出
	 * @since 1.1.0
	 */
	public static Video.Builder builder(byte[] bytes) throws IOException {
		Validate.notNull(bytes, "bytes 不可为 null");

		try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(IOUtils.toUnsynchronizedByteArrayInputStream(bytes))) {
			grabber.start();
			return new Video.Builder().parse(grabber);
		}
	}

	/**
	 * 从输入流创建构建器
	 * <p>会自动解析输入流中的视频信息</p>
	 *
	 * @param inputStream 视频输入流，不可为null
	 * @return 已解析的Video.Builder实例
	 * @throws IllegalArgumentException 当inputStream为null时抛出
	 * @throws IOException              当读取失败时抛出
	 * @since 1.1.0
	 */
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
	 * <p>检查 duration 是否不为 null、不为零时长且不为负数</p>
	 *
	 * @return true = 非空且时长不为 0 且不为负数；false = 无有效时长
	 * @since 1.1.0
	 */
	public boolean hasDuration() {
		return duration != null && !duration.isZero() && !duration.isNegative();
	}

	/**
	 * 判断是否为竖屏视频
	 * <p>当高度大于宽度时判定为竖屏</p>
	 *
	 * @return true表示竖屏，false表示横屏或正方形
	 * @since 1.1.0
	 */
	public boolean isVertical() {
		return height > width;
	}

	/**
	 * 判断是否为正方形视频
	 * <p>当宽度等于高度时判定为正方形</p>
	 *
	 * @return true表示正方形，false表示非正方形
	 * @since 1.1.0
	 */
	public boolean isSquare() {
		return width == height;
	}

	/**
	 * Video对象的构建器，提供Fluent API链式调用
	 * <p>
	 * 继承自{@link Media.Builder}，增加了视频特有的属性设置方法，以及快捷格式和分辨率设置。
	 * </p>
	 * <h3>使用方式</h3>
	 * <ul>
	 *   <li><b>新建视频对象</b>：使用{@link #Builder()}创建空白构建器，设置属性后调用{@link #build()}</li>
	 *   <li><b>修改现有视频</b>：使用{@link #Builder(Video)}基于现有视频创建构建器，修改属性后调用{@link #build()}</li>
	 * </ul>
	 * <h3>主要方法</h3>
	 * <ul>
	 *   <li>{@link #resolution(int, int)}：设置视频分辨率</li>
	 *   <li>{@link #resolution480p()} / {@link #resolution720p()} / {@link #resolution1080p()} / {@link #resolution2k()}：快捷设置横屏分辨率</li>
	 *   <li>{@link #resolution480pVertical()} / {@link #resolution720pVertical()} / {@link #resolution1080pVertical()} / {@link #resolution2kVertical()}：快捷设置竖屏分辨率</li>
	 *   <li>{@link #frameRate24()} / {@link #frameRate25()} / {@link #frameRate30()} / {@link #frameRate60()}：快捷设置标准帧率</li>
	 *   <li>{@link #mp4WithH264()} / {@link #mp4WithH265()} / {@link #webmWithVP9()} / {@link #mkvWithH264()} / {@link #mkvWithH265()}：快捷设置格式和编码</li>
	 *   <li>{@link #frameRate(double)}：设置帧率</li>
	 *   <li>{@link #bitrate(int)}：设置码率</li>
	 *   <li>{@link #audio(Audio)}：设置音频</li>
	 * </ul>
	 * <h3>使用示例</h3>
	 * <pre>{@code
	 * // 新建标准视频
	 * Video video = new Video.Builder()
	 *     .mp4WithH264()
	 *     .resolution1080p()
	 *     .frameRate30()
	 *     .bitrate(6000000)
	 *     .audio(Video.AUDIO_AAC_1080P)
	 *     .build();
	 *
	 * // 修改现有视频
	 * Video modified = new Video.Builder(existingVideo)
	 *     .bitrate(8000000)
	 *     .build();
	 * }</pre>
	 *
	 * @author pangju666
	 * @see Media.Builder
	 * @see Video
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
			this.frameRate = FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE;
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

		/**
		 * 设置为MP4格式+H264编码
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public Builder mp4WithH264() {
			this.format = FFmpegConstants.VIDEO_MP4_FORMAT;
			this.codecId = avcodec.AV_CODEC_ID_H264;
			return this;
		}

		/**
		 * 设置为MP4格式+H265编码
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public Builder mp4WithH265() {
			this.format = FFmpegConstants.VIDEO_MP4_FORMAT;
			this.codecId = avcodec.AV_CODEC_ID_H265;
			return this;
		}

		/**
		 * 设置为WEBM格式+VP9编码
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public Builder webmWithVP9() {
			this.format = FFmpegConstants.VIDEO_WEBM_FORMAT;
			this.codecId = avcodec.AV_CODEC_ID_VP9;
			return this;
		}

		/**
		 * 设置为MKV格式+H264编码
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public Builder mkvWithH264() {
			this.format = FFmpegConstants.VIDEO_MKV_FORMAT;
			this.codecId = avcodec.AV_CODEC_ID_H264;
			return this;
		}

		/**
		 * 设置为MKV格式+H265编码
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public Builder mkvWithH265() {
			this.format = FFmpegConstants.VIDEO_MKV_FORMAT;
			this.codecId = avcodec.AV_CODEC_ID_H265;
			return this;
		}

		/**
		 * 设置为2K横屏分辨率（2560×1440）
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public Builder resolution2k() {
			return resolution(2560, 1440);
		}

		/**
		 * 设置为1080P横屏分辨率（1920×1080）
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public Builder resolution1080p() {
			return resolution(1920, 1080);
		}

		/**
		 * 设置为720P横屏分辨率（1280×720）
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public Builder resolution720p() {
			return resolution(1280, 720);
		}

		/**
		 * 设置为480P横屏分辨率（640×480）
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public Builder resolution480p() {
			return resolution(640, 480);
		}

		/**
		 * 设置为2K竖屏分辨率（1440×2560）
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public Builder resolution2kVertical() {
			return resolution(1440, 2560);
		}

		/**
		 * 设置为1080P竖屏分辨率（1080×1920）
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public Builder resolution1080pVertical() {
			return resolution(1080, 1920);
		}

		/**
		 * 设置为720P竖屏分辨率（720×1280）
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public Builder resolution720pVertical() {
			return resolution(720, 1280);
		}

		/**
		 * 设置为480P竖屏分辨率（480×640）
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public Builder resolution480pVertical() {
			return resolution(480, 640);
		}

		/**
		 * 设置视频分辨率
		 *
		 * @param width  视频宽度，必须大于等于 0
		 * @param height 视频高度，必须大于等于 0
		 * @return 当前构建器，支持链式调用
		 * @throws IllegalArgumentException 当 width 或 height 小于 0 时抛出
		 * @since 1.1.0
		 */
		public Builder resolution(int width, int height) {
			Validate.isTrue(width >= 0, "width 必须为非负数");
			Validate.isTrue(height >= 0, "height 必须为非负数");
			this.height = height;
			this.width = width;
			return this;
		}

		/**
		 * 设置电影标准帧率 24fps（影视胶片制式）
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public Builder frameRate24() {
			return frameRate(24);
		}

		/**
		 * 设置PAL电视标准帧率 25fps（国内广电、短视频通用）
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public Builder frameRate25() {
			return frameRate(25);
		}

		/**
		 * 设置通用短视频/直播标准帧率 30fps（互联网视频主流）
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public Builder frameRate30() {
			return frameRate(30);
		}

		/**
		 * 设置高刷高清帧率 60fps（游戏、高清短视频、慢动作）
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public Builder frameRate60() {
			return frameRate(60);
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
		 * @param bitrate 码率（bps），<b>必须为非负数</b>
		 * @return 当前构建器，支持链式调用
		 * @throws IllegalArgumentException 码率为负数时抛出
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
			this.audio = audio;
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
		protected Video.Builder parse(FFmpegFrameGrabber grabber) throws FFmpegFrameGrabber.Exception {
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
