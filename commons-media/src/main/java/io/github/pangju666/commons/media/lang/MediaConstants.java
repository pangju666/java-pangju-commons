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
 * 媒体相关常量类，定义FFmpeg支持的音频、视频、字幕文件格式集合
 * <p>
 * 该类包含音频、视频、字幕三类媒体的完整格式支持，涵盖读取和写入两个维度，
 * 同时提供两种格式集合：FFmpeg格式标识和文件扩展名后缀。
 * <p>
 * <h3>FFmpeg格式标识集合</h3>
 * <ul>
 *   <li><b>音频格式</b>：{@link #SUPPORTED_READ_AUDIO_FORMATS}、{@link #SUPPORTED_WRITE_AUDIO_FORMATS}</li>
 *   <li><b>视频格式</b>：{@link #SUPPORTED_READ_VIDEO_FORMATS}、{@link #SUPPORTED_WRITE_VIDEO_FORMATS}</li>
 *   <li><b>字幕格式</b>：{@link #SUPPORTED_READ_SUBTITLE_FORMATS}、{@link #SUPPORTED_WRITE_SUBTITLE_FORMATS}</li>
 * </ul>
 * <p>
 * <h3>文件扩展名后缀集合</h3>
 * <ul>
 *   <li><b>音频后缀</b>：{@link #SUPPORTED_READ_AUDIO_FILE_FORMATS}、{@link #SUPPORTED_WRITE_AUDIO_FILE_FORMATS}</li>
 *   <li><b>视频后缀</b>：{@link #SUPPORTED_READ_VIDEO_FILE_FORMATS}、{@link #SUPPORTED_WRITE_VIDEO_FILE_FORMATS}</li>
 *   <li><b>字幕后缀</b>：{@link #SUPPORTED_READ_SUBTITLE_FILE_FORMATS}、{@link #SUPPORTED_WRITE_SUBTITLE_FILE_FORMATS}</li>
 * </ul>
 * <p>
 * <h3>格式命名规范</h3>
 * <ul>
 *   <li>FFmpeg格式标识采用原生短名称（如"mp3"、"mp4"、"srt"）</li>
 *   <li>文件扩展名后缀用于匹配文件名，均为小写</li>
 *   <li>裸码流格式区分字节序（大端be/小端le）、位深、编码类型</li>
 *   <li>游戏/专业格式针对特定场景设计，名称与FFmpeg保持一致</li>
 * </ul>
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 检查是否支持读取某FFmpeg音频格式
 * if (MediaConstants.SUPPORTED_READ_AUDIO_FORMATS.contains("mp3")) {
 *     // 可以读取
 * }
 *
 * // 检查文件扩展名是否为支持的视频格式
 * if (MediaConstants.SUPPORTED_READ_VIDEO_FILE_FORMATS.contains("mp4")) {
 *     // 可以读取
 * }
 *
 * // 检查是否支持写入某字幕格式
 * if (MediaConstants.SUPPORTED_WRITE_SUBTITLE_FILE_FORMATS.contains("srt")) {
 *     // 可以写入
 * }
 * }</pre>
 *
 * @author pangju666
 * @see <a href="https://ffmpeg.org/ffmpeg-formats.html">FFmpeg Formats Official Doc</a>
 * @since 1.1.0
 */
public class MediaConstants {
	public static final String AUDIO_WAV_FORMAT = "wav";

	public static final String AUDIO_FLAC_FORMAT = "flac";

	public static final String AUDIO_MP3_FORMAT = "mp3";

	public static final String AUDIO_OPUS_FORMAT = "opus";

	public static final String AUDIO_AAC_FORMAT = "aac";

	public static final String VIDEO_MP4_FORMAT = "mp4";

	public static final String VIDEO_WEBM_FORMAT = "webm";

	public static final String VIDEO_MKV_FORMAT = "matroska";

	public static final int DEFAULT_VIDEO_FRAME_RATE = 30;

	public static final int DEFAULT_AUDIO_CHANNELS = 2;

	public static final int AUDIO_STANDARD_SAMPLE_RATE = 44100;

	public static final int VIDEO_STANDARD_SAMPLE_RATE = 48000;
}
