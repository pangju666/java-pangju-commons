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

package io.github.pangju666.commons.ffmpeg.utils;

import io.github.pangju666.commons.ffmpeg.enums.FrameType;
import io.github.pangju666.commons.ffmpeg.io.FFmpegOutputStreamAdapter;
import io.github.pangju666.commons.ffmpeg.io.resource.AudioResource;
import io.github.pangju666.commons.ffmpeg.model.AudioOutputOption;
import io.github.pangju666.commons.io.utils.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 音频处理工具类
 * <p>
 * 提供音频文件的常用处理功能，包括：
 * </p>
 * <h3>核心功能</h3>
 * <ul>
 *     <li>转码 - 将音频转换为不同格式或编码</li>
 *     <li>裁剪 - 截取音频片段（支持从开头裁剪或指定时间段）</li>
 *     <li>拼接 - 将多个音频文件合并</li>
 *     <li>添加背景音乐 - 混合主音频和背景音乐（支持自定义权重）</li>
 *     <li>速度调整 - 改变音频播放速度（0.5-100倍）</li>
 *     <li>音量调整 - 调整音频音量（分贝值）</li>
 * </ul>
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 转码音频文件
 * AudioResource resource = AudioResource.of(new File("input.mp3"));
 * AudioOutputOption option = AudioOutputOptionBuilder.aac().bitrate(128000).build();
 * AudioUtils.transcode(resource, new File("output.aac"), option);
 *
 * // 裁剪音频（从开头裁剪 30 秒）
 * AudioUtils.cut(resource, new File("cut.mp3"), Duration.ofSeconds(30));
 *
 * // 拼接多个音频文件
 * List<AudioResource> resources = List.of(
 *     AudioResource.of(new File("part1.mp3")),
 *     AudioResource.of(new File("part2.mp3"))
 * );
 * AudioUtils.concat(resources, new File("merged.mp3"), option);
 *
 * // 添加背景音乐
 * AudioResource bgm = AudioResource.of(new File("bgm.mp3"));
 * AudioUtils.addBgm(resource, bgm, new File("with-bgm.mp3"), 0.3f);
 *
 * // 调整播放速度（2倍速）
 * AudioUtils.adjustSpeed(resource, new File("fast.mp3"), 2.0f);
 *
 * // 调整音量（增加 5 分贝）
 * AudioUtils.adjustVolume(resource, new File("loud.mp3"), 5.0f);
 * }</pre>
 *
 * @author pangju666
 * @see FFmpegUtils
 * @see AudioOutputOption
 * @since 2.1.0
 */
public class AudioUtils {
	/**
	 * 默认背景音乐权重
	 *
	 * @since 2.1.0
	 */
	public static final float DEFAULT_BGM_WEIGHT = 0.4f;

	protected AudioUtils() {
	}

	/**
	 * 将音频资源转码并输出到文件
	 *
	 * @param resource     输入音频资源
	 * @param outputFile   输出文件
	 * @param outputOption 输出音频配置
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 resource、outputOption 或 outputFile 为 null 时
	 * @since 2.1.0
	 */
	public static void transcode(final AudioResource resource, final File outputFile, final AudioOutputOption outputOption) throws IOException {
		Validate.notNull(outputOption, "outputOption 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			FFmpegUtils.transcode(grabber, recorder, outputOption, FrameType.AUDIO,
				false);
		}
	}

	/**
	 * 将音频资源转码并输出到输出流
	 *
	 * @param resource     输入音频资源
	 * @param outputStream 输出流
	 * @param outputOption 输出音频配置
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 resource、outputStream 或 outputOption 为 null 时
	 * @since 2.1.0
	 */
	public static void transcode(final AudioResource resource, final OutputStream outputStream,
	                             final AudioOutputOption outputOption) throws IOException {
		Validate.notNull(outputOption, "outputOption 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegOutputStreamAdapter adapter = new FFmpegOutputStreamAdapter(outputStream, outputOption, grabber);
		     FFmpegFrameRecorder recorder = adapter.openFFmpegFrameRecorder()) {
			FFmpegUtils.transcode(grabber, recorder, outputOption, FrameType.AUDIO,
				false);
		}
	}

	/**
	 * 从开头裁剪音频到指定时长并输出到文件（使用源音频配置）
	 *
	 * @param resource   输入音频资源
	 * @param outputFile 输出文件
	 * @param duration   裁剪时长
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 resource 或 outputFile 为 null 时
	 * @since 2.1.0
	 */
	public static void cut(final AudioResource resource, final File outputFile, final Duration duration) throws IOException {
		cut(resource, outputFile, (AudioOutputOption) null, Duration.ZERO, duration);
	}

	/**
	 * 从开头裁剪音频到指定时长并输出到输出流（使用源音频配置）
	 *
	 * @param resource     输入音频资源
	 * @param outputStream 输出流
	 * @param duration     裁剪时长
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 resource 或 outputStream 为 null 时
	 * @since 2.1.0
	 */
	public static void cut(final AudioResource resource, final OutputStream outputStream, final Duration duration) throws IOException {
		cut(resource, outputStream, (AudioOutputOption) null, Duration.ZERO, duration);
	}

	/**
	 * 裁剪指定时间段的音频并输出到文件（使用源音频配置）
	 *
	 * @param resource   输入音频资源
	 * @param outputFile 输出文件
	 * @param start      开始时间
	 * @param end        结束时间
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 resource 或 outputFile 为 null 时
	 * @since 2.1.0
	 */
	public static void cut(final AudioResource resource, final File outputFile, final Duration start, final Duration end) throws IOException {
		cut(resource, outputFile, null, start, end);
	}

	/**
	 * 裁剪指定时间段的音频并输出到输出流（使用源音频配置）
	 *
	 * @param resource     输入音频资源
	 * @param outputStream 输出流
	 * @param start        开始时间
	 * @param end          结束时间
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 resource 或 outputStream 为 null 时
	 * @since 2.1.0
	 */
	public static void cut(final AudioResource resource, final OutputStream outputStream, final Duration start,
	                       final Duration end) throws IOException {
		cut(resource, outputStream, null, start, end);
	}

	/**
	 * 从开头裁剪音频到指定时长并输出到文件（指定输出配置）
	 *
	 * @param resource     输入音频资源
	 * @param outputFile   输出文件
	 * @param outputOption 输出音频配置
	 * @param duration     裁剪时长
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 resource、outputOption 或 outputFile 为 null 时
	 * @since 2.1.0
	 */
	public static void cut(final AudioResource resource, final File outputFile, final AudioOutputOption outputOption,
	                       final Duration duration) throws IOException {
		cut(resource, outputFile, outputOption, Duration.ZERO, duration);
	}

	/**
	 * 从开头裁剪音频到指定时长并输出到输出流（指定输出配置）
	 *
	 * @param resource     输入音频资源
	 * @param outputStream 输出流
	 * @param outputOption 输出音频配置
	 * @param duration     裁剪时长
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 resource、outputOption 或 outputStream 为 null 时
	 * @since 2.1.0
	 */
	public static void cut(final AudioResource resource, final OutputStream outputStream, final AudioOutputOption outputOption,
	                       final Duration duration) throws IOException {
		cut(resource, outputStream, outputOption, Duration.ZERO, duration);
	}

	/**
	 * 裁剪指定时间段的音频并输出到文件（指定输出配置）
	 *
	 * @param resource     输入音频资源
	 * @param outputFile   输出文件
	 * @param outputOption 输出音频配置
	 * @param start        开始时间
	 * @param end          结束时间
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 resource、outputOption 或 outputFile 为 null 时
	 * @since 2.1.0
	 */
	public static void cut(final AudioResource resource, final File outputFile, final AudioOutputOption outputOption,
	                       final Duration start, final Duration end) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			FFmpegUtils.cut(grabber, recorder, outputOption, start, end, FrameType.AUDIO,
				false);
		}
	}

	/**
	 * 裁剪指定时间段的音频并输出到输出流（指定输出配置）
	 *
	 * @param resource     输入音频资源
	 * @param outputStream 输出流
	 * @param outputOption 输出音频配置
	 * @param start        开始时间
	 * @param end          结束时间
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 resource、outputOption 或 outputStream 为 null 时
	 * @since 2.1.0
	 */
	public static void cut(final AudioResource resource, final OutputStream outputStream, final AudioOutputOption outputOption,
	                       final Duration start, final Duration end) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegOutputStreamAdapter adapter = new FFmpegOutputStreamAdapter(outputStream, outputOption, grabber);
		     FFmpegFrameRecorder recorder = adapter.openFFmpegFrameRecorder()) {
			FFmpegUtils.cut(grabber, recorder, outputOption, start, end, FrameType.AUDIO,
				false);
		}
	}

	/**
	 * 拼接多个音频资源并输出到文件
	 *
	 * @param resources    音频资源集合
	 * @param outputFile   输出文件
	 * @param outputOption 输出音频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 outputOption 或 outputFile 为 null 时
	 * @throws IllegalArgumentException 当 resources 为空或包含 null 元素 时
	 * @since 2.1.0
	 */
	public static void concat(final Collection<AudioResource> resources, final File outputFile,
	                          final AudioOutputOption outputOption) throws IOException {
		Validate.notEmpty(resources, "resources 不可为空");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notNull(outputOption, "outputOption 不可为 null");
		Validate.isTrue(resources.stream().allMatch(Objects::nonNull),
			"resources 中存在为 null 的 AudioResource");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doConcat(resources, outputOption, recorder);
		}
	}

	/**
	 * 拼接多个音频资源并输出到输出流
	 *
	 * @param resources    音频资源集合
	 * @param outputStream 输出流
	 * @param outputOption 输出音频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 outputStream 或 outputOption 为 null 时
	 * @throws IllegalArgumentException 当 resources 为空或包含 null 元素 时
	 * @since 2.1.0
	 */
	public static void concat(final Collection<AudioResource> resources, final OutputStream outputStream,
	                          final AudioOutputOption outputOption) throws IOException {
		Validate.notEmpty(resources, "resources 不可为空");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(outputOption, "outputOption 不可为 null");
		Validate.isTrue(resources.stream().allMatch(Objects::nonNull),
			"resources 中存在为 null 的 AudioResource");

		try (FFmpegOutputStreamAdapter outputStreamAdapter = new FFmpegOutputStreamAdapter(outputStream, outputOption.getFormat());
		     FFmpegFrameRecorder recorder = outputStreamAdapter.openFFmpegFrameRecorder()) {
			doConcat(resources, outputOption, recorder);
		}
	}

	/**
	 * 为音频添加背景音乐并输出到文件（使用默认权重和源音频配置）
	 *
	 * @param mainResource 主音频资源
	 * @param bgmResource  背景音乐资源
	 * @param outputFile   输出文件
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 mainResource、bgmResource 或 outputFile 为 null 时
	 * @since 2.1.0
	 */
	public static void addBgm(final AudioResource mainResource, final AudioResource bgmResource,
	                          final File outputFile) throws IOException {
		addBgm(mainResource, bgmResource, outputFile, null, DEFAULT_BGM_WEIGHT);
	}

	/**
	 * 为音频添加背景音乐并输出到输出流（使用默认权重和源音频配置）
	 *
	 * @param mainResource 主音频资源
	 * @param bgmResource  背景音乐资源
	 * @param outputStream 输出流
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 mainResource、bgmResource 或 outputStream 为 null 时
	 * @since 2.1.0
	 */
	public static void addBgm(final AudioResource mainResource, final AudioResource bgmResource,
	                          final OutputStream outputStream) throws IOException {
		addBgm(mainResource, bgmResource, outputStream, null, DEFAULT_BGM_WEIGHT);
	}

	/**
	 * 为音频添加背景音乐并输出到文件（使用默认权重，指定输出配置）
	 *
	 * @param mainResource 主音频资源
	 * @param bgmResource  背景音乐资源
	 * @param outputFile   输出文件
	 * @param outputOption 输出音频配置
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 mainResource、bgmResource、outputOption 或 outputFile 为 null 时
	 * @since 2.1.0
	 */
	public static void addBgm(final AudioResource mainResource, final AudioResource bgmResource,
	                          final File outputFile, final AudioOutputOption outputOption) throws IOException {
		addBgm(mainResource, bgmResource, outputFile, outputOption, DEFAULT_BGM_WEIGHT);
	}

	/**
	 * 为音频添加背景音乐并输出到输出流（使用默认权重，指定输出配置）
	 *
	 * @param mainResource 主音频资源
	 * @param bgmResource  背景音乐资源
	 * @param outputStream 输出流
	 * @param outputOption 输出音频配置
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 mainResource、bgmResource、outputOption 或 outputStream 为 null 时
	 * @since 2.1.0
	 */
	public static void addBgm(final AudioResource mainResource, final AudioResource bgmResource,
	                          final OutputStream outputStream, final AudioOutputOption outputOption) throws IOException {
		addBgm(mainResource, bgmResource, outputStream, outputOption, DEFAULT_BGM_WEIGHT);
	}

	/**
	 * 为音频添加背景音乐并输出到文件（指定权重，使用源音频配置）
	 *
	 * @param mainResource 主音频资源
	 * @param bgmResource  背景音乐资源
	 * @param outputFile   输出文件
	 * @param bgmWeight    背景音乐权重
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 mainResource、bgmResource 或 outputFile 为 null 时
	 * @throws IllegalArgumentException 当 bgmWeight 小于等于 0 时
	 * @since 2.1.0
	 */
	public static void addBgm(final AudioResource mainResource, final AudioResource bgmResource,
	                          final File outputFile, final float bgmWeight) throws IOException {
		addBgm(mainResource, bgmResource, outputFile, null, bgmWeight);
	}

	/**
	 * 为音频添加背景音乐并输出到输出流（指定权重，使用源音频配置）
	 *
	 * @param mainResource 主音频资源
	 * @param bgmResource  背景音乐资源
	 * @param outputStream 输出流
	 * @param bgmWeight    背景音乐权重
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 mainResource、bgmResource 或 outputStream 为 null 时
	 * @throws IllegalArgumentException 当 bgmWeight 小于等于 0 时
	 * @since 2.1.0
	 */
	public static void addBgm(final AudioResource mainResource, final AudioResource bgmResource,
	                          final OutputStream outputStream, final float bgmWeight) throws IOException {
		addBgm(mainResource, bgmResource, outputStream, null, bgmWeight);
	}

	/**
	 * 为音频添加背景音乐并输出到文件（指定权重和输出配置）
	 *
	 * @param mainResource 主音频资源
	 * @param bgmResource  背景音乐资源
	 * @param outputFile   输出文件
	 * @param outputOption 输出音频配置
	 * @param bgmWeight    背景音乐权重
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 mainResource、bgmResource、outputOption 或 outputFile 为 null 时
	 * @throws IllegalArgumentException 当 bgmWeight 小于等于 0 时
	 * @since 2.1.0
	 */
	public static void addBgm(final AudioResource mainResource, final AudioResource bgmResource,
	                          final File outputFile, final AudioOutputOption outputOption, final float bgmWeight) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notNull(mainResource, "mainResource 不可为 null");
		Validate.notNull(bgmResource, "bgmResource 不可为 null");
		Validate.isTrue(bgmWeight > 0, "bgmWeight 必须大于0");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber mainGrabber = mainResource.openFrameGrabber();
		     FFmpegFrameGrabber bgmGrabber = bgmResource.openFrameGrabber();
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			AudioOutputOption defaultOutputOption = new AudioOutputOption(mainGrabber);

			FFmpegUtils.applyFilter(List.of(mainGrabber, bgmGrabber), recorder,
				ObjectUtils.getIfNull(outputOption, defaultOutputOption),
				null, FFmpegUtils.getAddBgmFilter(mainGrabber, bgmGrabber, bgmWeight),
				FrameType.AUDIO, false);
		}
	}

	/**
	 * 为音频添加背景音乐并输出到输出流（指定权重和输出配置）
	 *
	 * @param mainResource 主音频资源
	 * @param bgmResource  背景音乐资源
	 * @param outputStream 输出流
	 * @param outputOption 输出音频配置
	 * @param bgmWeight    背景音乐权重
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 mainResource、bgmResource、outputOption 或 outputStream 为 null 时
	 * @throws IllegalArgumentException 当 bgmWeight 小于等于 0 时
	 * @since 2.1.0
	 */
	public static void addBgm(final AudioResource mainResource, final AudioResource bgmResource,
	                          final OutputStream outputStream, final AudioOutputOption outputOption, final float bgmWeight) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(mainResource, "mainResource 不可为 null");
		Validate.notNull(bgmResource, "bgmResource 不可为 null");
		Validate.isTrue(bgmWeight > 0, "bgmWeight 必须大于0");

		try (FFmpegFrameGrabber mainGrabber = mainResource.openFrameGrabber();
		     FFmpegFrameGrabber bgmGrabber = bgmResource.openFrameGrabber();
		     FFmpegOutputStreamAdapter adapter = new FFmpegOutputStreamAdapter(outputStream, outputOption, mainGrabber);
		     FFmpegFrameRecorder recorder = adapter.openFFmpegFrameRecorder()) {
			AudioOutputOption defaultOutputOption = new AudioOutputOption(mainGrabber);

			FFmpegUtils.applyFilter(List.of(mainGrabber, bgmGrabber), recorder,
				ObjectUtils.getIfNull(outputOption, defaultOutputOption),
				null, FFmpegUtils.getAddBgmFilter(mainGrabber, bgmGrabber, bgmWeight),
				FrameType.AUDIO, false);
		}
	}

	/**
	 * 调整音频播放速度并输出到文件（使用源音频配置）
	 *
	 * @param resource   输入音频资源
	 * @param outputFile 输出文件
	 * @param speed      播放速度（0.5-100）
	 * @throws IOException 当 I/O 错误发生时
	 * @since 2.1.0
	 */
	public static void adjustSpeed(final AudioResource resource, final File outputFile, final float speed) throws IOException {
		adjustSpeed(resource, outputFile, speed, null);
	}

	/**
	 * 调整音频播放速度并输出到输出流（使用源音频配置）
	 *
	 * @param resource     输入音频资源
	 * @param outputStream 输出流
	 * @param speed        播放速度（0.5-100）
	 * @throws IOException 当 I/O 错误发生时
	 * @since 2.1.0
	 */
	public static void adjustSpeed(final AudioResource resource, final OutputStream outputStream, final float speed) throws IOException {
		adjustSpeed(resource, outputStream, speed, null);
	}

	/**
	 * 调整音频播放速度并输出到文件（指定输出配置）
	 *
	 * @param resource     输入音频资源
	 * @param outputFile   输出文件
	 * @param speed        播放速度（0.5-100）
	 * @param outputOption 输出音频配置
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 resource、outputOption 或 outputFile 为 null 时
	 * @since 2.1.0
	 */
	public static void adjustSpeed(final AudioResource resource, final File outputFile, final float speed,
	                               final AudioOutputOption outputOption) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			FFmpegUtils.applyAudioFilter(grabber, recorder, outputOption,
				FFmpegUtils.getAtempoFilter(speed), FrameType.AUDIO, false);
		}
	}

	/**
	 * 调整音频播放速度并输出到输出流（指定输出配置）
	 *
	 * @param resource     输入音频资源
	 * @param outputStream 输出流
	 * @param speed        播放速度（0.5-100）
	 * @param outputOption 输出音频配置
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 resource、outputOption 或 outputStream 为 null 时
	 * @since 2.1.0
	 */
	public static void adjustSpeed(final AudioResource resource, final OutputStream outputStream, final float speed,
	                               final AudioOutputOption outputOption) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegOutputStreamAdapter adapter = new FFmpegOutputStreamAdapter(outputStream, outputOption, grabber);
		     FFmpegFrameRecorder recorder = adapter.openFFmpegFrameRecorder()) {
			FFmpegUtils.applyAudioFilter(grabber, recorder, outputOption,
				FFmpegUtils.getAtempoFilter(speed), FrameType.AUDIO, false);
		}
	}

	/**
	 * 调整音频音量并输出到文件（使用源音频配置）
	 *
	 * @param resource   输入音频资源
	 * @param outputFile 输出文件
	 * @param db         音量变化的分贝值
	 * @throws IOException 当 I/O 错误发生时
	 * @since 2.1.0
	 */
	public static void adjustVolume(final AudioResource resource, final File outputFile, final float db) throws IOException {
		adjustVolume(resource, outputFile, db, null);
	}

	/**
	 * 调整音频音量并输出到输出流（使用源音频配置）
	 *
	 * @param resource     输入音频资源
	 * @param outputStream 输出流
	 * @param db           音量变化的分贝值
	 * @throws IOException 当 I/O 错误发生时
	 * @since 2.1.0
	 */
	public static void adjustVolume(final AudioResource resource, final OutputStream outputStream, final float db) throws IOException {
		adjustVolume(resource, outputStream, db, null);
	}

	/**
	 * 调整音频音量并输出到文件（指定输出配置）
	 *
	 * @param resource     输入音频资源
	 * @param outputFile   输出文件
	 * @param db           音量变化的分贝值
	 * @param outputOption 输出音频配置
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 resource、outputOption 或 outputFile 为 null 时
	 * @since 2.1.0
	 */
	public static void adjustVolume(final AudioResource resource, final File outputFile, final float db,
	                                final AudioOutputOption outputOption) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			FFmpegUtils.applyAudioFilter(grabber, recorder, outputOption,
				FFmpegUtils.getVolumeFilter(db), FrameType.AUDIO, false);
		}
	}

	/**
	 * 调整音频音量并输出到输出流（指定输出配置）
	 *
	 * @param resource     输入音频资源
	 * @param outputStream 输出流
	 * @param db           音量变化的分贝值
	 * @param outputOption 输出音频配置
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 resource、outputOption 或 outputStream 为 null 时
	 * @since 2.1.0
	 */
	public static void adjustVolume(final AudioResource resource, final OutputStream outputStream, final float db,
	                                final AudioOutputOption outputOption) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegOutputStreamAdapter adapter = new FFmpegOutputStreamAdapter(outputStream, outputOption, grabber);
		     FFmpegFrameRecorder recorder = adapter.openFFmpegFrameRecorder()) {
			FFmpegUtils.applyAudioFilter(grabber, recorder, outputOption,
				FFmpegUtils.getVolumeFilter(db), FrameType.AUDIO, false);
		}
	}

	/**
	 * 执行音频拼接操作
	 * <p>
	 * 初始化录制器并依次将多个音频资源的帧录制到输出中。
	 * </p>
	 *
	 * @param resources    音频资源集合
	 * @param outputOption 输出音频配置
	 * @param recorder     帧录制器
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 recorder 为 null 时
	 * @since 2.1.0
	 */
	protected static void doConcat(Collection<AudioResource> resources, AudioOutputOption outputOption,
	                               FFmpegFrameRecorder recorder) throws IOException {
		Validate.notNull(recorder, "recorder 不可为 null");

		FFmpegUtils.initRecorder(recorder, null, outputOption, FrameType.AUDIO);
		recorder.start();

		for (AudioResource resource : resources) {
			try (FFmpegFrameGrabber grabber = resource.openFrameGrabber()) {
				if (FFmpegUtils.isNotStarted(grabber)) {
					grabber.start();
				}

				FFmpegUtils.recordFrames(recorder, grabber, FrameType.AUDIO, false);
			}
		}
	}
}
