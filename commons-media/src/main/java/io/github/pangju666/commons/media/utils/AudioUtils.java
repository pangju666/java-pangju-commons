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

package io.github.pangju666.commons.media.utils;

import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.media.lang.MediaConstants;
import io.github.pangju666.commons.media.model.Audio;
import io.github.pangju666.commons.media.model.MediaResource;
import org.apache.commons.lang3.Validate;
import org.bytedeco.javacv.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 音频处理工具类
 * <p>
 * 基于 JavaCV + FFmpeg 实现的专业音频处理工具，提供丰富的音频操作功能，包括转码、裁剪、拼接、混音、变速和滤镜应用等。
 * 采用统一的资源管理和参数校验机制，支持 File 和 OutputStream 两种输出方式，兼顾易用性和执行效率。
 * </p>
 * <h2>主要特性</h2>
 * <ul>
 *     <li><strong>音频转码</strong>：支持多种音频格式转换，可自定义采样率、声道数、码率等编码参数</li>
 *     <li><strong>音频裁剪</strong>：支持按时间区间截取音频片段，可自定义输出格式</li>
 *     <li><strong>音频拼接</strong>：将多个音频文件按顺序合并为一个音频，支持统一输出格式</li>
 *     <li><strong>音频混音</strong>：主音频叠加背景音乐，自动对齐采样率和声道，支持音量调节</li>
 *     <li><strong>音频变速</strong>：基于 FFmpeg atempo 滤镜，支持 0.01 到 100 倍速范围，自动处理倍率限制</li>
 *     <li><strong>滤镜应用</strong>：开放 FFmpeg 滤镜接口，可自定义复杂的音频处理效果</li>
 * </ul>
 * <h2>使用说明</h2>
 * <ul>
 *     <li>所有方法均进行严格的参数校验，非法参数将抛出 {@link IllegalArgumentException}</li>
 *     <li>内部使用 try-with-resources 自动管理 FFmpegFrameGrabber 和 FFmpegFrameRecorder，避免资源泄漏</li>
 *     <li>音频变速功能采用多级 atempo 滤镜拼接，突破单级 0.5-2.0 倍速限制</li>
 *     <li>混音功能自动将背景音乐标准化为 WAV 格式并对齐主音频参数，确保混音质量</li>
 *     <li>支持 {@link MediaResource} 作为音频输入源，支持文件和流两种输入方式</li>
 * </ul>
 * <h2>典型用例</h2>
 * <pre>{@code
 * // 1. 音频转码
 * MediaResource resource = new MediaResource(new File("input.wav"));
 * AudioUtils.transcode(resource, new File("output.mp3"), Audio.MP3);
 *
 * // 2. 音频裁剪
 * AudioUtils.cut(resource, new File("clip.mp3"), Duration.ofSeconds(5), Duration.ofSeconds(15));
 *
 * // 3. 音频混音
 * MediaResource bgm = new MediaResource(new File("background.mp3"));
 * AudioUtils.remix(resource, bgm, new File("remix.mp3"), 0.5f);
 *
 * // 4. 音频变速
 * AudioUtils.adjustSpeed(resource, new File("faster.mp3"), 1.5f);
 *
 * // 5. 应用自定义滤镜
 * AudioUtils.applyFilter(resource, new File("effect.mp3"), "aecho=0.8:0.88:60:0.4");
 * }</pre>
 *
 * @author pangju666
 * @see org.bytedeco.javacv.FFmpegFrameGrabber
 * @see org.bytedeco.javacv.FFmpegFrameRecorder
 * @see org.bytedeco.javacv.FFmpegFrameFilter
 * @see io.github.pangju666.commons.media.model.MediaResource
 * @see io.github.pangju666.commons.media.model.Audio
 * @since 1.1.0
 */
public class AudioUtils {
	/**
	 * 背景音乐音量安全上限（超过此值极易出现爆音）
	 *
	 * @since 1.1.0
	 */
	public static final float MAX_BGM_VOLUME = 1.5f;

	/**
	 * 背景音乐标准化格式（WAV，用于混音时的格式统一）
	 *
	 * @since 1.1.0
	 */
	protected static final String BGM_FORMAT = MediaConstants.AUDIO_WAV_FORMAT;

	/**
	 * 音频变速 - 单级 atempo 最小速度（FFmpeg 原生限制）
	 *
	 * @since 1.1.0
	 */
	protected static final float MIN_SPEED = 0.5f;

	/**
	 * 音频变速 - 单级 atempo 最大速度（FFmpeg 原生限制）
	 *
	 * @since 1.1.0
	 */
	protected static final float MAX_SPEED = 2.0f;

	/**
	 * 16位音频采样值缩放比例
	 *
	 * @since 1.1.0
	 */
	private static final float S16_SAMPLE_SCALE = 32768.0f;

	/**
	 * 受保护的构造函数，防止实例化
	 *
	 * @since 1.1.0
	 */
	protected AudioUtils() {
	}

	/**
	 * 音频转码（输出到文件）
	 * <p>根据指定的 Audio 配置，将输入音频转码为目标格式，支持自定义编码器、采样率、声道数、码率等参数</p>
	 *
	 * @param resource    输入音频资源，不可为 null，必须是音频类型
	 * @param outputFile  输出文件，不可为 null
	 * @param outputAudio 输出音频配置，不可为 null
	 * @throws IllegalArgumentException 当 resource 为空、不是音频类型，或 outputFile/outputAudio 为空时抛出
	 * @throws IOException              当文件读写或 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	public static void transcode(final MediaResource resource, final File outputFile, final Audio outputAudio) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doTranscode(resource, recorder, outputAudio);
		}
	}

	/**
	 * 音频转码（输出到输出流）
	 * <p>根据指定的 Audio 配置，将输入音频转码为目标格式，支持自定义编码器、采样率、声道数、码率等参数</p>
	 *
	 * @param resource     输入音频资源，不可为 null，必须是音频类型
	 * @param outputStream 输出流，不可为 null
	 * @param outputAudio  输出音频配置，不可为 null
	 * @throws IllegalArgumentException 当 resource 为空、不是音频类型，或 outputStream/outputAudio 为空时抛出
	 * @throws IOException              当流操作或 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	public static void transcode(final MediaResource resource, final OutputStream outputStream, final Audio outputAudio) throws IOException {
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doTranscode(resource, recorder, outputAudio);
		}
	}

	/**
	 * 音频裁剪（输出到文件，从开头截取指定时长）
	 * <p>从音频开头开始，截取指定时长的片段，输出格式保持与输入一致</p>
	 *
	 * @param resource   输入音频资源，不可为 null，必须是音频类型
	 * @param outputFile 输出文件，不可为 null
	 * @param duration   截取时长，不可为 null，必须大于 0
	 * @throws IllegalArgumentException 当 resource 为空、不是音频类型，或 outputFile/duration 为空时抛出
	 * @throws IOException              当文件读写或 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	public static void cut(final MediaResource resource, final File outputFile, final Duration duration) throws IOException {
		cut(resource, outputFile, Duration.ZERO, duration);
	}

	/**
	 * 音频裁剪（输出到输出流，从开头截取指定时长）
	 * <p>从音频开头开始，截取指定时长的片段，输出格式保持与输入一致</p>
	 *
	 * @param resource     输入音频资源，不可为 null，必须是音频类型
	 * @param outputStream 输出流，不可为 null
	 * @param duration     截取时长，不可为 null，必须大于 0
	 * @throws IllegalArgumentException 当 resource 为空、不是音频类型，或 outputStream/duration 为空时抛出
	 * @throws IOException              当流操作或 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	public static void cut(final MediaResource resource, final OutputStream outputStream, final Duration duration) throws IOException {
		Validate.notNull(duration, "duration 不可为 null");

		cut(resource, outputStream, Duration.ZERO, duration);
	}

	/**
	 * 音频裁剪（输出到文件，指定起止时间）
	 * <p>从指定的开始时间截取到结束时间，输出格式保持与输入一致</p>
	 *
	 * @param resource   输入音频资源，不可为 null，必须是音频类型
	 * @param outputFile 输出文件，不可为 null
	 * @param start      开始时间，不可为 null，必须小于音频总时长
	 * @param end        结束时间，可为 null（表示截取到结尾），不为 null 时必须大于 start
	 * @throws IllegalArgumentException 当 resource 为空、不是音频类型，或 outputFile/start 为空，或 start 大于等于音频总时长，或 end 小于等于 start 时抛出
	 * @throws IOException              当文件读写或 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	public static void cut(final MediaResource resource, final File outputFile, final Duration start, final Duration end) throws IOException {
		Validate.notNull(start, "start 不可为 null");
		if (Objects.nonNull(end)) {
			Validate.isTrue(end.compareTo(start) > 0, "end 必须大于 start");
		}
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doCut(resource, recorder, null, start, end);
		}
	}

	/**
	 * 音频裁剪（输出到输出流，指定起止时间）
	 * <p>从指定的开始时间截取到结束时间，输出格式保持与输入一致</p>
	 *
	 * @param resource     输入音频资源，不可为 null，必须是音频类型
	 * @param outputStream 输出流，不可为 null
	 * @param start        开始时间，不可为 null，必须小于音频总时长
	 * @param end          结束时间，可为 null（表示截取到结尾），不为 null 时必须大于 start
	 * @throws IllegalArgumentException 当 resource 为空、不是音频类型，或 outputStream/start 为空，或 start 大于等于音频总时长，或 end 小于等于 start 时抛出
	 * @throws IOException              当流操作或 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	public static void cut(final MediaResource resource, final OutputStream outputStream, final Duration start,
						   final Duration end) throws IOException {
		Validate.notNull(start, "start 不可为 null");
		if (Objects.nonNull(end)) {
			Validate.isTrue(end.compareTo(start) > 0, "end 必须大于 start");
		}
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doCut(resource, recorder, null, start, end);
		}
	}

	/**
	 * 音频裁剪（输出到文件，从开头截取指定时长，自定义输出配置）
	 * <p>从音频开头开始，截取指定时长的片段，支持自定义输出格式和参数</p>
	 *
	 * @param resource    输入音频资源，不可为 null，必须是音频类型
	 * @param outputFile  输出文件，不可为 null
	 * @param outputAudio 输出音频配置，不可为 null
	 * @param duration    截取时长，不可为 null，必须大于 0
	 * @throws IllegalArgumentException 当 resource 为空、不是音频类型，或 outputFile/outputAudio/duration 为空时抛出
	 * @throws IOException              当文件读写或 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	public static void cut(final MediaResource resource, final File outputFile, final Audio outputAudio,
						   final Duration duration) throws IOException {
		Validate.notNull(duration, "duration 不可为 null");

		cut(resource, outputFile, outputAudio, Duration.ZERO, duration);
	}

	/**
	 * 音频裁剪（输出到输出流，从开头截取指定时长，自定义输出配置）
	 * <p>从音频开头开始，截取指定时长的片段，支持自定义输出格式和参数</p>
	 *
	 * @param resource     输入音频资源，不可为 null，必须是音频类型
	 * @param outputStream 输出流，不可为 null
	 * @param outputAudio  输出音频配置，不可为 null
	 * @param duration     截取时长，不可为 null，必须大于 0
	 * @throws IllegalArgumentException 当 resource 为空、不是音频类型，或 outputStream/outputAudio/duration 为空时抛出
	 * @throws IOException              当流操作或 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	public static void cut(final MediaResource resource, final OutputStream outputStream, final Audio outputAudio,
	                       final Duration duration) throws IOException {
		Validate.notNull(duration, "duration 不可为 null");

		cut(resource, outputStream, outputAudio, Duration.ZERO, duration);
	}

	/**
	 * 音频裁剪（输出到文件，指定起止时间，自定义输出配置）
	 * <p>从指定的开始时间截取到结束时间，支持自定义输出格式和参数</p>
	 *
	 * @param resource    输入音频资源，不可为 null，必须是音频类型
	 * @param outputFile  输出文件，不可为 null
	 * @param outputAudio 输出音频配置，不可为 null
	 * @param start       开始时间，不可为 null，必须小于音频总时长
	 * @param end         结束时间，可为 null（表示截取到结尾），不为 null 时必须大于 start
	 * @throws IllegalArgumentException 当 resource 为空、不是音频类型，或 outputFile/outputAudio/start 为空，或 start 大于等于音频总时长，或 end 小于等于 start 时抛出
	 * @throws IOException              当文件读写或 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	public static void cut(final MediaResource resource, final File outputFile, final Audio outputAudio,
						   final Duration start, final Duration end) throws IOException {
		Validate.notNull(start, "start 不可为 null");
		if (Objects.nonNull(end)) {
			Validate.isTrue(end.compareTo(start) > 0, "end 必须大于 start");
		}
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doCut(resource, recorder, outputAudio, start, end);
		}
	}

	/**
	 * 音频裁剪（输出到输出流，指定起止时间，自定义输出配置）
	 * <p>从指定的开始时间截取到结束时间，支持自定义输出格式和参数</p>
	 *
	 * @param resource     输入音频资源，不可为 null，必须是音频类型
	 * @param outputStream 输出流，不可为 null
	 * @param outputAudio  输出音频配置，不可为 null
	 * @param start        开始时间，不可为 null，必须小于音频总时长
	 * @param end          结束时间，可为 null（表示截取到结尾），不为 null 时必须大于 start
	 * @throws IllegalArgumentException 当 resource 为空、不是音频类型，或 outputStream/outputAudio/start 为空，或 start 大于等于音频总时长，或 end 小于等于 start 时抛出
	 * @throws IOException              当流操作或 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	public static void cut(final MediaResource resource, final OutputStream outputStream, final Audio outputAudio,
						   final Duration start, final Duration end) throws IOException {
		Validate.notNull(start, "start 不可为 null");
		if (Objects.nonNull(end)) {
			Validate.isTrue(end.compareTo(start) > 0, "end 必须大于 start");
		}
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doCut(resource, recorder, outputAudio, start, end);
		}
	}

	/**
	 * 音频拼接（输出到文件）
	 * <p>将多个音频资源按顺序拼接为一个完整的音频文件，自动对齐输出参数</p>
	 *
	 * @param resources   音频资源列表，不可为空，非音频类型的资源会被自动跳过
	 * @param outputFile  输出文件，不可为 null
	 * @param outputAudio 输出音频配置，不可为 null
	 * @throws IllegalArgumentException 当 resources 为空、或 outputFile/outputAudio 为空时抛出
	 * @throws IOException              当文件读写或 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	public static void concat(final List<MediaResource> resources, final File outputFile, final Audio outputAudio) throws IOException {
		Validate.notEmpty(resources, "resources 不可为空");
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doConcat(resources, recorder, outputAudio);
		}
	}

	/**
	 * 音频拼接（输出到输出流）
	 * <p>将多个音频资源按顺序拼接为一个完整的音频流，自动对齐输出参数</p>
	 *
	 * @param resources    音频资源列表，不可为空，非音频类型的资源会被自动跳过
	 * @param outputStream 输出流，不可为 null
	 * @param outputAudio  输出音频配置，不可为 null
	 * @throws IllegalArgumentException 当 resources 为空、或 outputStream/outputAudio 为空时抛出
	 * @throws IOException              当流操作或 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	public static void concat(final List<MediaResource> resources, final OutputStream outputStream, final Audio outputAudio) throws IOException {
		Validate.notEmpty(resources, "resources 不可为空");
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doConcat(resources, recorder, outputAudio);
		}
	}

	/**
	 * 音频混音（输出到文件，使用默认背景音乐音量）
	 * <p>将主音频与背景音乐混合，背景音乐音量默认为安全上限 {@link #MAX_BGM_VOLUME}，自动对齐音频参数</p>
	 *
	 * @param mainResource 主音频资源，不可为 null，必须是音频类型
	 * @param bgmResource  背景音乐资源，不可为 null，必须是音频类型
	 * @param outputFile   输出文件，不可为 null
	 * @throws IllegalArgumentException 当 mainResource/bgmResource/outputFile 为空，或不是音频类型时抛出
	 * @throws IOException              当文件读写或 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	public static void remix(final MediaResource mainResource, final MediaResource bgmResource, final File outputFile) throws IOException {
		remix(mainResource, bgmResource, outputFile, MAX_BGM_VOLUME);
	}

	/**
	 * 音频混音（输出到文件，自定义背景音乐音量）
	 * <p>将主音频与背景音乐混合，支持自定义背景音乐音量，自动对齐音频参数</p>
	 *
	 * @param mainResource 主音频资源，不可为 null，必须是音频类型
	 * @param bgmResource  背景音乐资源，不可为 null，必须是音频类型
	 * @param outputFile   输出文件，不可为 null
	 * @param bgmVolume    背景音乐音量，必须大于 0，建议不超过 {@link #MAX_BGM_VOLUME} 以避免爆音
	 * @throws IllegalArgumentException 当 mainResource/bgmResource/outputFile 为空、不是音频类型，或 bgmVolume 小于等于 0 时抛出
	 * @throws IOException              当文件读写或 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	public static void remix(final MediaResource mainResource, final MediaResource bgmResource, final File outputFile,
							 final float bgmVolume) throws IOException {
		Validate.notNull(mainResource, "mainResource 不可为 null");
		Validate.notNull(bgmResource, "bgmResource 不可为 null");
		Validate.isTrue(bgmVolume > 0, "bgmVolume 必须大于0");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doRemix(mainResource, bgmResource, recorder, bgmVolume);
		}
	}

	/**
	 * 音频混音（输出到输出流，使用默认背景音乐音量）
	 * <p>将主音频与背景音乐混合，背景音乐音量默认为安全上限 {@link #MAX_BGM_VOLUME}，自动对齐音频参数</p>
	 *
	 * @param mainResource 主音频资源，不可为 null，必须是音频类型
	 * @param bgmResource  背景音乐资源，不可为 null，必须是音频类型
	 * @param outputStream 输出流，不可为 null
	 * @throws IllegalArgumentException 当 mainResource/bgmResource/outputStream 为空，或不是音频类型时抛出
	 * @throws IOException              当流操作或 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	public static void remix(final MediaResource mainResource, final MediaResource bgmResource, final OutputStream outputStream) throws IOException {
		remix(mainResource, bgmResource, outputStream, MAX_BGM_VOLUME);
	}

	/**
	 * 音频混音（输出到输出流，自定义背景音乐音量）
	 * <p>将主音频与背景音乐混合，支持自定义背景音乐音量，自动对齐音频参数</p>
	 *
	 * @param mainResource 主音频资源，不可为 null，必须是音频类型
	 * @param bgmResource  背景音乐资源，不可为 null，必须是音频类型
	 * @param outputStream 输出流，不可为 null
	 * @param bgmVolume    背景音乐音量，必须大于 0，建议不超过 {@link #MAX_BGM_VOLUME} 以避免爆音
	 * @throws IllegalArgumentException 当 mainResource/bgmResource/outputStream 为空、不是音频类型，或 bgmVolume 小于等于 0 时抛出
	 * @throws IOException              当流操作或 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	public static void remix(final MediaResource mainResource, final MediaResource bgmResource, final OutputStream outputStream, final float bgmVolume) throws IOException {
		Validate.notNull(mainResource, "mainResource 不可为 null");
		Validate.notNull(bgmResource, "bgmResource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.isTrue(bgmVolume > 0, "bgmVolume 必须大于0");
		Validate.isTrue(mainResource.isAudio(), "不是音频类型 MediaResource");
		Validate.isTrue(bgmResource.isAudio(), "不是音频类型 MediaResource");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doRemix(mainResource, bgmResource, recorder, bgmVolume);
		}
	}

	/**
	 * 音频变速（输出到输出流，保持原始格式）
	 * <p>调整音频播放速度，同步改变音调，支持 0 到 10.0 的倍率范围，自动使用多级 atempo 滤镜突破单级限制</p>
	 *
	 * @param resource     输入音频资源，不可为 null，必须是音频类型
	 * @param outputStream 输出流，不可为 null
	 * @param speed        变速倍率，必须大于 0，最大支持 10.0
	 * @throws IllegalArgumentException 当 resource 为空、不是音频类型，或 outputStream 为空，或 speed 不在有效范围时抛出
	 * @throws IOException              当流操作或 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	public static void adjustSpeed(final MediaResource resource, final OutputStream outputStream, final float speed) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.isTrue(speed > 0, "变速倍率必须大于 0");
		Validate.isTrue(speed <= 10.0f, "变速倍率最大支持 10.0");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doAdjustSpeed(resource, recorder, speed, null);
		}
	}

	/**
	 * 音频变速（输出到文件，保持原始格式）
	 * <p>调整音频播放速度，同步改变音调，支持 0 到 10.0 的倍率范围，自动使用多级 atempo 滤镜突破单级限制</p>
	 *
	 * @param resource   输入音频资源，不可为 null，必须是音频类型
	 * @param outputFile 输出文件，不可为 null
	 * @param speed      变速倍率，必须大于 0，最大支持 10.0
	 * @throws IllegalArgumentException 当 resource 为空、不是音频类型，或 outputFile 为空，或 speed 不在有效范围时抛出
	 * @throws IOException              当文件读写或 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	public static void adjustSpeed(final MediaResource resource, final File outputFile, final float speed) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(speed > 0, "变速倍率必须大于 0");
		Validate.isTrue(speed <= 10.0f, "变速倍率最大支持 10.0");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doAdjustSpeed(resource, recorder, speed, null);
		}
	}

	/**
	 * 音频变速（输出到输出流，自定义输出配置）
	 * <p>调整音频播放速度，同步改变音调，支持 0 到 10.0 的倍率范围，自动使用多级 atempo 滤镜突破单级限制</p>
	 *
	 * @param resource     输入音频资源，不可为 null，必须是音频类型
	 * @param outputStream 输出流，不可为 null
	 * @param speed        变速倍率，必须大于 0，最大支持 10.0
	 * @param outputAudio  输出音频配置，不可为 null
	 * @throws IllegalArgumentException 当 resource 为空、不是音频类型，或 outputStream/outputAudio 为空，或 speed 不在有效范围时抛出
	 * @throws IOException              当流操作或 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	public static void adjustSpeed(final MediaResource resource, final OutputStream outputStream, final float speed,
								   final Audio outputAudio) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.isTrue(speed > 0, "变速倍率必须大于 0");
		Validate.isTrue(speed <= 10.0f, "变速倍率最大支持 10.0");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		Validate.notNull(outputAudio, "outputAudio 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doAdjustSpeed(resource, recorder, speed, outputAudio);
		}
	}

	/**
	 * 音频变速（输出到文件，自定义输出配置）
	 * <p>调整音频播放速度，同步改变音调，支持 0 到 10.0 的倍率范围，自动使用多级 atempo 滤镜突破单级限制</p>
	 *
	 * @param resource    输入音频资源，不可为 null，必须是音频类型
	 * @param outputFile  输出文件，不可为 null
	 * @param speed       变速倍率，必须大于 0，最大支持 10.0
	 * @param outputAudio 输出音频配置，不可为 null
	 * @throws IllegalArgumentException 当 resource 为空、不是音频类型，或 outputFile/outputAudio 为空，或 speed 不在有效范围时抛出
	 * @throws IOException              当文件读写或 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	public static void adjustSpeed(final MediaResource resource, final File outputFile, final float speed,
								   final Audio outputAudio) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(speed > 0, "变速倍率必须大于 0");
		Validate.isTrue(speed <= 10.0f, "变速倍率最大支持 10.0");
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doAdjustSpeed(resource, recorder, speed, outputAudio);
		}
	}

	/**
	 * 应用音频滤镜（输出到文件，保持原始格式）
	 * <p>对音频应用自定义 FFmpeg 音频滤镜，支持多种音频特效处理</p>
	 *
	 * @param resource     输入音频资源，不可为 null，必须是音频类型
	 * @param outputFile   输出文件，不可为 null
	 * @param audioFilters FFmpeg 音频滤镜字符串，不可为空
	 * @throws IllegalArgumentException 当 resource 为空、不是音频类型，或 outputFile/audioFilters 为空时抛出
	 * @throws IOException              当文件读写或 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	public static void applyFilter(final MediaResource resource, final File outputFile, final String audioFilters) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notBlank(audioFilters, "audioFilters 不可为空");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doApplyFilter(resource, recorder, audioFilters, null);
		}
	}

	/**
	 * 应用音频滤镜（输出到输出流，保持原始格式）
	 * <p>对音频应用自定义 FFmpeg 音频滤镜，支持多种音频特效处理</p>
	 *
	 * @param resource     输入音频资源，不可为 null，必须是音频类型
	 * @param outputStream 输出流，不可为 null
	 * @param audioFilters FFmpeg 音频滤镜字符串，不可为空
	 * @throws IllegalArgumentException 当 resource 为空、不是音频类型，或 outputStream/audioFilters 为空时抛出
	 * @throws IOException              当流操作或 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	public static void applyFilter(final MediaResource resource, final OutputStream outputStream, final String audioFilters) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notBlank(audioFilters, "audioFilters 不可为空");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doApplyFilter(resource, recorder, audioFilters, null);
		}
	}

	/**
	 * 应用音频滤镜（输出到文件，自定义输出配置）
	 * <p>对音频应用自定义 FFmpeg 音频滤镜，支持多种音频特效处理</p>
	 *
	 * @param resource     输入音频资源，不可为 null，必须是音频类型
	 * @param outputFile   输出文件，不可为 null
	 * @param audioFilters FFmpeg 音频滤镜字符串，不可为空
	 * @param outputAudio  输出音频配置，不可为 null
	 * @throws IllegalArgumentException 当 resource 为空、不是音频类型，或 outputFile/audioFilters/outputAudio 为空时抛出
	 * @throws IOException              当文件读写或 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	public static void applyFilter(final MediaResource resource, final File outputFile, final String audioFilters,
								   final Audio outputAudio) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notBlank(audioFilters, "audioFilters 不可为空");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doApplyFilter(resource, recorder, audioFilters, outputAudio);
		}
	}

	/**
	 * 应用音频滤镜（输出到输出流，自定义输出配置）
	 * <p>对音频应用自定义 FFmpeg 音频滤镜，支持多种音频特效处理</p>
	 *
	 * @param resource     输入音频资源，不可为 null，必须是音频类型
	 * @param outputStream 输出流，不可为 null
	 * @param audioFilters FFmpeg 音频滤镜字符串，不可为空
	 * @param outputAudio  输出音频配置，不可为 null
	 * @throws IllegalArgumentException 当 resource 为空、不是音频类型，或 outputStream/audioFilters/outputAudio 为空时抛出
	 * @throws IOException              当流操作或 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	public static void applyFilter(final MediaResource resource, final OutputStream outputStream,
								   final String audioFilters, final Audio outputAudio) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notBlank(audioFilters, "audioFilters 不可为空");
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doApplyFilter(resource, recorder, audioFilters, outputAudio);
		}
	}

	/**
	 * 执行音频拼接操作（内部方法）
	 *
	 * @param resources   音频资源列表
	 * @param recorder    FFmpeg 帧录制器
	 * @param outputAudio 输出音频配置
	 * @throws IOException 当 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	protected static void doConcat(final List<MediaResource> resources, final FFmpegFrameRecorder recorder,
								   final Audio outputAudio) throws IOException {
		initRecorder(recorder, null, outputAudio);

		for (MediaResource resource : resources) {
			if (Objects.isNull(resource) || !resource.isAudio()) {
				continue;
			}

			try (FFmpegFrameGrabber grabber = (resource.isFile() ? new FFmpegFrameGrabber(resource.getFile()) :
				new FFmpegFrameGrabber(resource.getInputStream()))) {
				grabber.start();

				recordFrames(recorder, grabber, null);
			}
		}
	}

	/**
	 * 执行音频混音操作（内部方法）
	 *
	 * @param mainResource 主音频资源
	 * @param bgmResource  背景音乐资源
	 * @param recorder     FFmpeg 帧录制器
	 * @param bgmVolume    背景音乐音量
	 * @throws IOException 当 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	protected static void doRemix(final MediaResource mainResource, final MediaResource bgmResource,
								  final FFmpegFrameRecorder recorder, final float bgmVolume) throws IOException {
		File tmpFile = null;
		FFmpegFrameGrabber bgmGrabber;
		Frame[] liveBgmHolder = new Frame[1];

		try (FFmpegFrameGrabber mainGrabber = (mainResource.isFile() ? new FFmpegFrameGrabber(mainResource.getFile()) :
			new FFmpegFrameGrabber(mainResource.getInputStream()))) {
			mainGrabber.start();
			Audio mainAudio = Audio.builder(mainGrabber).build();

			bgmGrabber = bgmResource.isFile() ? new FFmpegFrameGrabber(bgmResource.getFile()) :
				new FFmpegFrameGrabber(bgmResource.getInputStream());
			bgmGrabber.start();

			// 判断是否需要转临时标准化wav
			if (bgmGrabber.getAudioChannels() != mainAudio.getChannels() ||
				bgmGrabber.getSampleRate() != mainAudio.getSampleRate() ||
				!BGM_FORMAT.equals(bgmGrabber.getFormat())) {
				tmpFile = File.createTempFile(UUID.randomUUID().toString(), null);

				try (FFmpegFrameRecorder bgmRecorder = new FFmpegFrameRecorder(tmpFile, 0)) {
					Audio outputAudio = Audio.builder(mainAudio).format(BGM_FORMAT).build();
					initRecorder(bgmRecorder, bgmGrabber, outputAudio);
					recordFrames(bgmRecorder, bgmGrabber, null);
				} catch (IOException e) {
					FileUtils.forceDeleteIfExist(tmpFile);
					throw e;
				} finally {
					bgmGrabber.close();
				}

				bgmGrabber = new FFmpegFrameGrabber(tmpFile);
				bgmGrabber.start();
			}

			List<short[]> bgmPcmCache = new ArrayList<>();
			boolean isCacheBgm = bgmGrabber.getLengthInTime() < mainGrabber.getLengthInTime();

			if (isCacheBgm) {
				while (true) {
					try (Frame frame = bgmGrabber.grabSamples()) {
						if (Objects.isNull(frame)) {
							break;
						}

						if (frame.samples != null && frame.samples.length > 0) {
							ShortBuffer sb = (ShortBuffer) frame.samples[0];
							short[] arr = new short[sb.remaining()];
							sb.get(arr);
							bgmPcmCache.add(arr);
						}
					} catch (IOException e) {
						bgmGrabber.close();
						FileUtils.forceDeleteIfExist(tmpFile);
						throw e;
					}
				}
			}

			try {
				initRecorder(recorder, mainGrabber, mainAudio);

				int bgmPageIndex = 0;
				int bgmSampleOffset = 0;

				while (true) {
					try (Frame mainFrame = mainGrabber.grabSamples()) {
						if (Objects.isNull(mainFrame)) {
							break;
						}
						if (mainFrame.samples == null) {
							continue;
						}

						Buffer[] sampleBuffers = mainFrame.samples;
						int sampleCount = sampleBuffers[0].remaining();

						// 逐声道混音
						for (int i = 0; i < sampleBuffers.length; i++) {
							Buffer buffer = sampleBuffers[i];
							// 区分浮点 / 16位整型
							if (buffer instanceof FloatBuffer floatBuffer) {
								float[] mainArr = new float[sampleCount];
								floatBuffer.get(mainArr);
								floatBuffer.rewind();

								for (int j = 0; j < sampleCount; j++) {
									float mainSample = mainArr[j];
									float bgmSample = computeBgmSample(isCacheBgm, bgmPcmCache,
										bgmGrabber, liveBgmHolder, bgmPageIndex,
										bgmSampleOffset, i);
									float mixSample = mainSample + bgmSample * bgmVolume;
									floatBuffer.put(j, Math.max(-1f, Math.min(1f, mixSample)));
									if (isCacheBgm) {
										bgmSampleOffset++;
									}
								}
							} else if (buffer instanceof ShortBuffer shortBuffer) {
								short[] mainArr = new short[sampleCount];
								shortBuffer.get(mainArr);
								shortBuffer.rewind();

								for (int j = 0; j < sampleCount; j++) {
									float mainSample = mainArr[j] / S16_SAMPLE_SCALE;
									float bgmSample = computeBgmSample(isCacheBgm, bgmPcmCache,
										bgmGrabber, liveBgmHolder, bgmPageIndex,
										bgmSampleOffset, i);
									int mixSample = (int) ((mainSample + bgmSample * bgmVolume) * S16_SAMPLE_SCALE);
									shortBuffer.put(j, (short) Math.max(Short.MIN_VALUE,
										Math.min(Short.MAX_VALUE, mixSample)));
									if (isCacheBgm) {
										bgmSampleOffset++;
									}
								}
							}
						}

						if (isCacheBgm) {
							short[] page = bgmPcmCache.get(bgmPageIndex);
							if (bgmSampleOffset >= page.length) {
								bgmSampleOffset = 0;
								bgmPageIndex = (bgmPageIndex + 1) % bgmPcmCache.size();
							}
						}

						recorder.record(mainFrame);
					}
				}
			} finally {
				if (liveBgmHolder[0] != null) {
					liveBgmHolder[0].close();
				}
				bgmGrabber.close();

				FileUtils.forceDeleteIfExist(tmpFile);
			}
		}
	}

	/**
	 * 执行音频变速操作（内部方法）
	 * <p>自动构建多级 atempo 滤镜链，突破单级 0.5~2.0 的限制</p>
	 *
	 * @param resource    输入音频资源
	 * @param recorder    FFmpeg 帧录制器
	 * @param speed       变速倍率
	 * @param outputAudio 输出音频配置，可为 null
	 * @throws IOException 当 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	protected static void doAdjustSpeed(final MediaResource resource, final FFmpegFrameRecorder recorder, final float speed,
										final Audio outputAudio) throws IOException {
		List<String> filterParts = new ArrayList<>();
		float current = speed;
		// 大于2.0：不断除以 2.0 叠加 atempo=2.0
		while (current > MAX_SPEED) {
			filterParts.add("atempo=2.0");
			current /= MAX_SPEED;
		}
		// 小于0.5：不断乘以 2.0 叠加 atempo=0.5
		while (current < MIN_SPEED) {
			filterParts.add("atempo=0.5");
			current *= MIN_SPEED;
		}
		// 最后补上剩余倍率
		filterParts.add(String.format("atempo=%.2f", current));

		doApplyFilter(resource, recorder, String.join(",", filterParts), outputAudio);
	}

	/**
	 * 执行音频滤镜应用操作（内部方法）
	 *
	 * @param resource     输入音频资源
	 * @param recorder     FFmpeg 帧录制器
	 * @param audioFilters FFmpeg 音频滤镜字符串
	 * @param outputAudio  输出音频配置，可为 null
	 * @throws IOException 当 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	protected static void doApplyFilter(final MediaResource resource, final FFmpegFrameRecorder recorder, final String audioFilters,
										final Audio outputAudio) throws IOException {
		try (FFmpegFrameGrabber grabber = (resource.isFile() ? new FFmpegFrameGrabber(resource.getFile()) :
			new FFmpegFrameGrabber(resource.getInputStream()));
			 FFmpegFrameFilter filter = new FFmpegFrameFilter(audioFilters, 0)) {
			grabber.start();

			filter.setAudioChannels(grabber.getAudioChannels());
			filter.setSampleRate(grabber.getSampleRate());
			filter.start();

			initRecorder(recorder, grabber, outputAudio);

			while (true) {
				try (Frame grabberFrame = grabber.grabSamples()) {
					if (Objects.isNull(grabberFrame)) {
						break;
					}

					filter.push(grabberFrame);

					while (true) {
						try (Frame filterFrame = filter.pull()) {
							if (Objects.isNull(filterFrame)) {
								break;
							}

							recorder.record(filterFrame);
						}
					}
				}
			}

			while (true) {
				try (Frame filterFrame = filter.pull()) {
					if (Objects.isNull(filterFrame)) {
						break;
					}

					recorder.record(filterFrame);
				}
			}
		}
	}

	/**
	 * 执行音频裁剪操作（内部方法）
	 *
	 * @param resource    输入音频资源
	 * @param recorder    FFmpeg 帧录制器
	 * @param outputAudio 输出音频配置，可为 null
	 * @param start       开始时间
	 * @param end         结束时间，可为 null
	 * @throws IOException 当 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	protected static void doCut(final MediaResource resource, final FFmpegFrameRecorder recorder, final Audio outputAudio,
								final Duration start, final Duration end) throws IOException {
		try (FFmpegFrameGrabber grabber = (resource.isFile() ? new FFmpegFrameGrabber(resource.getFile()) :
			new FFmpegFrameGrabber(resource.getInputStream()))) {
			long startTimestamp = start.toNanos() / 1000;
			long endTimestamp = Objects.isNull(end) ? grabber.getLengthInTime() : Math.min(end.toNanos() / 1000, grabber.getLengthInTime());

			Validate.isTrue(startTimestamp < grabber.getLengthInTime(), "start 必须小于音频总时长");

			grabber.setTimestamp(startTimestamp);
			grabber.start();

			initRecorder(recorder, grabber, outputAudio);
			recordFrames(recorder, grabber, endTimestamp);
		}
	}

	/**
	 * 执行音频转码操作（内部方法）
	 *
	 * @param resource    输入音频资源
	 * @param recorder    FFmpeg 帧录制器
	 * @param outputAudio 输出音频配置
	 * @throws IOException 当 FFmpeg 操作失败时抛出
	 * @since 1.1.0
	 */
	protected static void doTranscode(final MediaResource resource, final FFmpegFrameRecorder recorder, final Audio outputAudio) throws IOException {
		try (FFmpegFrameGrabber grabber = (resource.isFile() ? new FFmpegFrameGrabber(resource.getFile()) :
			new FFmpegFrameGrabber(resource.getInputStream()))) {
			grabber.start();

			initRecorder(recorder, grabber, outputAudio);
			recordFrames(recorder, grabber, null);
		}
	}

	/**
	 * 录制音频帧（内部方法）
	 * <p>从 grabber 中抓取音频帧并写入 recorder，可选截止时间</p>
	 *
	 * @param recorder     FFmpeg 帧录制器
	 * @param grabber      FFmpeg 帧抓取器
	 * @param endTimestamp 截止时间戳（微秒），可为 null
	 * @throws FFmpegFrameRecorder.Exception 当录制器操作失败时抛出
	 * @throws FrameGrabber.Exception        当抓取器操作失败时抛出
	 * @since 1.1.0
	 */
	protected static void recordFrames(final FFmpegFrameRecorder recorder, final FFmpegFrameGrabber grabber,
									   final Long endTimestamp) throws FFmpegFrameRecorder.Exception, FrameGrabber.Exception {
		if (Objects.nonNull(endTimestamp) && grabber.getTimestamp() > endTimestamp) {
			return;
		}

		while (true) {
			try (Frame frame = grabber.grabSamples()) {
				if (Objects.isNull(frame)) {
					break;
				}
				recorder.record(frame);
			}
		}
	}

	/**
	 * 初始化 FFmpeg 帧录制器（内部方法）
	 * <p>根据指定的 Audio 配置或从 grabber 中自动获取参数，初始化录制器</p>
	 *
	 * @param recorder     FFmpeg 帧录制器，不可为 null
	 * @param grabber      FFmpeg 帧抓取器，可为 null
	 * @param outputAudio  输出音频配置，可为 null
	 * @throws FFmpegFrameRecorder.Exception 当录制器启动失败时抛出
	 * @since 1.1.0
	 */
	protected static void initRecorder(final FFmpegFrameRecorder recorder, final FFmpegFrameGrabber grabber,
	                                   final Audio outputAudio) throws FFmpegFrameRecorder.Exception {
		if (Objects.nonNull(outputAudio)) {
			outputAudio.initRecorder(recorder);
		} else {
			recorder.setFormat(grabber.getFormat());

			recorder.setAudioCodec(grabber.getAudioCodec());
			recorder.setAudioCodecName(grabber.getAudioCodecName());
			recorder.setSampleRate(grabber.getSampleRate());
			recorder.setAudioBitrate(grabber.getAudioBitrate());
			recorder.setAudioChannels(grabber.getAudioChannels());
			recorder.setAudioMetadata(grabber.getAudioMetadata());
		}

		recorder.start();
	}

	/**
	 * 计算背景音乐采样值（内部方法）
	 * <p>根据是否使用缓存模式，从缓存或实时抓取中获取背景音乐采样值</p>
	 *
	 * @param useCache          是否使用缓存模式
	 * @param pageCache         缓存的背景音乐 PCM 数据
	 * @param grabber           背景音乐抓取器
	 * @param liveHolder        实时帧持有者
	 * @param pageIndex         当前缓存页索引
	 * @param cacheSampleOffset 当前缓存页内的采样偏移
	 * @param channelIndex      声道索引
	 * @return 归一化后的背景音乐采样值（-1.0 到 1.0）
	 * @throws IOException 当抓取器操作失败时抛出
	 * @since 1.1.0
	 */
	private static float computeBgmSample(final boolean useCache, final List<short[]> pageCache, final FFmpegFrameGrabber grabber,
	                                      final Frame[] liveHolder, final int pageIndex, final int cacheSampleOffset,
	                                      final int channelIndex) throws IOException {
		if (useCache) {
			short[] page = pageCache.get(pageIndex);
			int safeOff = Math.min(cacheSampleOffset, page.length - 1);
			return page[safeOff] / S16_SAMPLE_SCALE;
		}

		Frame frame = liveHolder[0];
		if (frame == null || !frame.samples[0].hasRemaining()) {
			if (frame != null) {
				frame.close();
			}
			frame = grabber.grabSamples();
			liveHolder[0] = frame;
			if (frame == null) {
				return 0f;
			}
		}

		Buffer buffer = frame.samples[Math.min(channelIndex, frame.samples.length - 1)];
		return ((ShortBuffer) buffer).get() / S16_SAMPLE_SCALE;
	}
}
