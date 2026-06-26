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
import io.github.pangju666.commons.media.model.Audio;
import io.github.pangju666.commons.media.model.MediaResource;
import org.apache.commons.lang3.Validate;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
	 * 背景音乐音量安全上限（超过此值极易出现爆音）
	 *
	 * @since 1.1.0
	 */
	public static final float MAX_BGM_VOLUME = 1.5f;

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
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		FFmpegUtils.transcode(resource, outputFile, outputAudio, true);
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
		Validate.notNull(outputStream, "outputStream 不可为 null");
		FFmpegUtils.transcode(resource, outputStream, outputAudio, true);
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
		Validate.notNull(duration, "duration 不可为 null");
		cut(resource, outputFile, null, Duration.ZERO, duration);
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
		cut(resource, outputStream, null, Duration.ZERO, duration);
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
		cut(resource, outputFile, null, start, end);
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
	public static void cut(final MediaResource resource, final OutputStream outputStream, final Duration start, final Duration end) throws IOException {
		cut(resource, outputStream, null, start, end);
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
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		FFmpegUtils.cut(resource, outputFile, outputAudio, start, end, true);
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
		Validate.notNull(outputStream, "outputStream 不可为 null");
		FFmpegUtils.cut(resource, outputStream, outputAudio, start, end, true);
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
	public static void concat(final Collection<MediaResource> resources, final File outputFile, final Audio outputAudio) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		FFmpegUtils.concat(resources, outputFile, outputAudio, true);
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
	public static void concat(final Collection<MediaResource> resources, final OutputStream outputStream, final Audio outputAudio) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		FFmpegUtils.concat(resources, outputStream, outputAudio, true);
	}

	public static void remix(final MediaResource mainResource, final MediaResource bgmResource, final File outputFile) throws IOException {
		remix(mainResource, bgmResource, outputFile, MAX_BGM_VOLUME);
	}

	public static void remix(final MediaResource mainResource, final MediaResource bgmResource,
	                         final OutputStream outputStream) throws IOException {
		remix(mainResource, bgmResource, outputStream, MAX_BGM_VOLUME);
	}

	public static void remix(final MediaResource mainResource, final MediaResource bgmResource, final File outputFile,
	                         final Audio outputAudio) throws IOException {
		remix(mainResource, bgmResource, outputFile, outputAudio, MAX_BGM_VOLUME);
	}

	public static void remix(final MediaResource mainResource, final MediaResource bgmResource,
	                         final OutputStream outputStream, final Audio outputAudio) throws IOException {
		remix(mainResource, bgmResource, outputStream, outputAudio, MAX_BGM_VOLUME);
	}

	public static void remix(final MediaResource mainResource, final MediaResource bgmResource,
	                         final OutputStream outputStream, final float bgmVolume) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		doRemix(mainResource, bgmResource, outputStream, null, bgmVolume);
	}

	public static void remix(final MediaResource mainResource, final MediaResource bgmResource, final File outputFile,
	                         final float bgmVolume) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		doRemix(mainResource, bgmResource, outputFile, null, bgmVolume);
	}

	public static void remix(final MediaResource mainResource, final MediaResource bgmResource, final File outputFile,
	                         final Audio outputAudio, final float bgmVolume) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		doRemix(mainResource, bgmResource, outputFile, outputAudio, bgmVolume);
	}

	public static void remix(final MediaResource mainResource, final MediaResource bgmResource, final OutputStream outputStream,
	                         final Audio outputAudio, final float bgmVolume) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		doRemix(mainResource, bgmResource, outputStream, outputAudio, bgmVolume);
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
		adjustSpeed(resource, outputStream, speed, null);
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
		adjustSpeed(resource, outputFile, speed, null);
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
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		Validate.notNull(outputStream, "outputStream 不可为 null");

		FFmpegUtils.applyFilter(Collections.singletonList(resource), outputStream, outputAudio,
			null, FFmpegUtils.getAtempoFilter(speed), true);
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
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FFmpegUtils.applyFilter(Collections.singletonList(resource), outputFile, outputAudio,
			null, FFmpegUtils.getAtempoFilter(speed), true);
	}

	public static void applyFilter(final Collection<MediaResource> resources, final File outputFile,
	                               final String audioFilters) throws IOException {
		applyFilter(resources, outputFile, audioFilters, null);
	}

	public static void applyFilter(final Collection<MediaResource> resources, final OutputStream outputStream,
	                               final String audioFilters) throws IOException {
		applyFilter(resources, outputStream, audioFilters, null);
	}

	public static void applyFilter(final Collection<MediaResource> resources, final File outputFile, final String audioFilters,
								   final Audio outputAudio) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		FFmpegUtils.applyFilter(resources, outputFile, outputAudio, null, audioFilters, true);
	}

	public static void applyFilter(final Collection<MediaResource> resources, final OutputStream outputStream,
								   final String audioFilters, final Audio outputAudio) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		FFmpegUtils.applyFilter(resources, outputStream, outputAudio, null, audioFilters, true);
	}

	protected static void doRemix(final MediaResource mainResource, final MediaResource bgmResource, final Object output,
	                              final Audio outputAudio, final float bgmVolume) throws IOException {
		Validate.notNull(mainResource, "mainResource 不可为 null");
		Validate.notNull(bgmResource, "bgmResource 不可为 null");
		Validate.isTrue(mainResource.isAudio(), "不是音频类型 MediaResource");
		Validate.isTrue(bgmResource.isAudio(), "不是音频类型 MediaResource");

		try (FFmpegFrameGrabber mainGrabber = FFmpegUtils.openFrameGrabber(mainResource);
		     FFmpegFrameGrabber bgmGrabber = FFmpegUtils.openFrameGrabber(bgmResource)) {
			mainGrabber.start();
			bgmGrabber.start();

			String audioFilters = FFmpegUtils.getAmixFilter(bgmVolume, mainGrabber.getLengthInTime() > bgmGrabber.getLengthInTime());
			FFmpegUtils.applyFilter(List.of(mainGrabber, bgmGrabber), output, outputAudio,
				null, audioFilters, true);
		}
	}
}
