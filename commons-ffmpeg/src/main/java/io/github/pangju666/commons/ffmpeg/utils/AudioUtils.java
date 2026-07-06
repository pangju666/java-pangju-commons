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
import io.github.pangju666.commons.ffmpeg.model.Audio;
import io.github.pangju666.commons.ffmpeg.model.MediaResource;
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
 * <ul>
 *     <li>转码 - 将音频转换为不同格式或编码</li>
 *     <li>裁剪 - 截取音频片段</li>
 *     <li>拼接 - 将多个音频文件合并</li>
 *     <li>添加背景音乐 - 混合主音频和背景音乐</li>
 *     <li>速度调整 - 改变音频播放速度</li>
 *     <li>音量调整 - 调整音频音量</li>
 * </ul>
 * </p>
 *
 * @author pangju666
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
	 * @param resource    输入音频资源
	 * @param outputFile  输出文件
	 * @param outputAudio 输出音频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 或 outputAudio 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是音频类型时
	 * @since 2.1.0
	 */
	public static void transcode(final MediaResource resource, final File outputFile, final Audio outputAudio) throws IOException {
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			FFmpegUtils.transcode(grabber, recorder, outputAudio, FrameType.AUDIO,
				false);
		}
	}

	/**
	 * 将音频资源转码并输出到输出流
	 *
	 * @param resource     输入音频资源
	 * @param outputStream 输出流
	 * @param outputAudio  输出音频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource、outputStream 或 outputAudio 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是音频类型时
	 * @since 2.1.0
	 */
	public static void transcode(final MediaResource resource, final OutputStream outputStream, final Audio outputAudio) throws IOException {
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			FFmpegUtils.transcode(grabber, recorder, outputAudio, FrameType.AUDIO,
				false);
		}
	}

	/**
	 * 从开头裁剪音频到指定时长并输出到文件（使用源音频配置）
	 *
	 * @param resource   输入音频资源
	 * @param outputFile 输出文件
	 * @param duration   裁剪时长
	 * @throws IOException 当 I/O 错误发生时
	 * @since 2.1.0
	 */
	public static void cut(final MediaResource resource, final File outputFile, final Duration duration) throws IOException {
		cut(resource, outputFile, (Audio) null, Duration.ZERO, duration);
	}

	/**
	 * 从开头裁剪音频到指定时长并输出到输出流（使用源音频配置）
	 *
	 * @param resource     输入音频资源
	 * @param outputStream 输出流
	 * @param duration     裁剪时长
	 * @throws IOException 当 I/O 错误发生时
	 * @since 2.1.0
	 */
	public static void cut(final MediaResource resource, final OutputStream outputStream, final Duration duration) throws IOException {
		cut(resource, outputStream, (Audio) null, Duration.ZERO, duration);
	}

	/**
	 * 裁剪指定时间段的音频并输出到文件（使用源音频配置）
	 *
	 * @param resource   输入音频资源
	 * @param outputFile 输出文件
	 * @param start      开始时间
	 * @param end        结束时间
	 * @throws IOException 当 I/O 错误发生时
	 * @since 2.1.0
	 */
	public static void cut(final MediaResource resource, final File outputFile, final Duration start, final Duration end) throws IOException {
		cut(resource, outputFile, null, start, end);
	}

	/**
	 * 裁剪指定时间段的音频并输出到输出流（使用源音频配置）
	 *
	 * @param resource     输入音频资源
	 * @param outputStream 输出流
	 * @param start        开始时间
	 * @param end          结束时间
	 * @throws IOException 当 I/O 错误发生时
	 * @since 2.1.0
	 */
	public static void cut(final MediaResource resource, final OutputStream outputStream, final Duration start,
	                       final Duration end) throws IOException {
		cut(resource, outputStream, null, start, end);
	}

	/**
	 * 从开头裁剪音频到指定时长并输出到文件（指定输出配置）
	 *
	 * @param resource    输入音频资源
	 * @param outputFile  输出文件
	 * @param outputAudio 输出音频配置
	 * @param duration    裁剪时长
	 * @throws IOException 当 I/O 错误发生时
	 * @since 2.1.0
	 */
	public static void cut(final MediaResource resource, final File outputFile, final Audio outputAudio,
	                       final Duration duration) throws IOException {
		cut(resource, outputFile, outputAudio, Duration.ZERO, duration);
	}

	/**
	 * 从开头裁剪音频到指定时长并输出到输出流（指定输出配置）
	 *
	 * @param resource     输入音频资源
	 * @param outputStream 输出流
	 * @param outputAudio  输出音频配置
	 * @param duration     裁剪时长
	 * @throws IOException 当 I/O 错误发生时
	 * @since 2.1.0
	 */
	public static void cut(final MediaResource resource, final OutputStream outputStream, final Audio outputAudio,
	                       final Duration duration) throws IOException {
		cut(resource, outputStream, outputAudio, Duration.ZERO, duration);
	}

	/**
	 * 裁剪指定时间段的音频并输出到文件（指定输出配置）
	 *
	 * @param resource    输入音频资源
	 * @param outputFile  输出文件
	 * @param outputAudio 输出音频配置
	 * @param start       开始时间
	 * @param end         结束时间
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是音频类型时
	 * @since 2.1.0
	 */
	public static void cut(final MediaResource resource, final File outputFile, final Audio outputAudio,
	                       final Duration start, final Duration end) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			FFmpegUtils.cut(grabber, recorder, outputAudio, start, end, FrameType.AUDIO,
				false);
		}
	}

	/**
	 * 裁剪指定时间段的音频并输出到输出流（指定输出配置）
	 *
	 * @param resource     输入音频资源
	 * @param outputStream 输出流
	 * @param outputAudio  输出音频配置
	 * @param start        开始时间
	 * @param end          结束时间
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 或 outputStream 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是音频类型时
	 * @since 2.1.0
	 */
	public static void cut(final MediaResource resource, final OutputStream outputStream, final Audio outputAudio,
	                       final Duration start, final Duration end) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			FFmpegUtils.cut(grabber, recorder, outputAudio, start, end, FrameType.AUDIO,
				false);
		}
	}

	/**
	 * 拼接多个音频资源并输出到文件（使用源音频配置）
	 *
	 * @param resources  音频资源集合
	 * @param outputFile 输出文件
	 * @throws IOException 当 I/O 错误发生时
	 * @since 2.1.0
	 */
	public static void concat(final Collection<MediaResource> resources, final File outputFile) throws IOException {
		concat(resources, outputFile, null);
	}

	/**
	 * 拼接多个音频资源并输出到输出流（使用源音频配置）
	 *
	 * @param resources    音频资源集合
	 * @param outputStream 输出流
	 * @throws IOException 当 I/O 错误发生时
	 * @since 2.1.0
	 */
	public static void concat(final Collection<MediaResource> resources, final OutputStream outputStream) throws IOException {
		concat(resources, outputStream, null);
	}

	/**
	 * 拼接多个音频资源并输出到文件（指定输出配置）
	 *
	 * @param resources   音频资源集合
	 * @param outputFile  输出文件
	 * @param outputAudio 输出音频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resources 为 null 或包含 null 元素时
	 * @throws IllegalArgumentException 当 resources 为空或包含非音频类型资源时
	 * @since 2.1.0
	 */
	public static void concat(final Collection<MediaResource> resources, final File outputFile, final Audio outputAudio) throws IOException {
		Validate.notEmpty(resources, "resources 不可为空");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.isTrue(resources.stream().allMatch(resource ->
			Objects.nonNull(resource) && resource.isAudio()), "存在非音频类型 MediaResource");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			FFmpegUtils.concatByResource(resources, recorder, outputAudio, FrameType.AUDIO,
				false);
		}
	}

	/**
	 * 拼接多个音频资源并输出到输出流（指定输出配置）
	 *
	 * @param resources    音频资源集合
	 * @param outputStream 输出流
	 * @param outputAudio  输出音频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resources、outputStream 为 null 或 resources 包含 null 元素时
	 * @throws IllegalArgumentException 当 resources 为空或包含非音频类型资源时
	 * @since 2.1.0
	 */
	public static void concat(final Collection<MediaResource> resources, final OutputStream outputStream,
	                          final Audio outputAudio) throws IOException {
		Validate.notEmpty(resources, "resources 不可为空");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.isTrue(resources.stream().allMatch(resource ->
			Objects.nonNull(resource) && resource.isAudio()), "存在非音频类型 MediaResource");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			FFmpegUtils.concatByResource(resources, recorder, outputAudio, FrameType.AUDIO,
				false);
		}
	}

	/**
	 * 为音频添加背景音乐并输出到文件（使用默认权重和源音频配置）
	 *
	 * @param mainResource 主音频资源
	 * @param bgmResource  背景音乐资源
	 * @param outputFile   输出文件
	 * @throws IOException 当 I/O 错误发生时
	 * @since 2.1.0
	 */
	public static void addBgm(final MediaResource mainResource, final MediaResource bgmResource,
	                          final File outputFile) throws IOException {
		addBgm(mainResource, bgmResource, outputFile, null, DEFAULT_BGM_WEIGHT);
	}

	/**
	 * 为音频添加背景音乐并输出到输出流（使用默认权重和源音频配置）
	 *
	 * @param mainResource 主音频资源
	 * @param bgmResource  背景音乐资源
	 * @param outputStream 输出流
	 * @throws IOException 当 I/O 错误发生时
	 * @since 2.1.0
	 */
	public static void addBgm(final MediaResource mainResource, final MediaResource bgmResource,
	                          final OutputStream outputStream) throws IOException {
		addBgm(mainResource, bgmResource, outputStream, null, DEFAULT_BGM_WEIGHT);
	}

	/**
	 * 为音频添加背景音乐并输出到文件（使用默认权重，指定输出配置）
	 *
	 * @param mainResource 主音频资源
	 * @param bgmResource  背景音乐资源
	 * @param outputFile   输出文件
	 * @param outputAudio  输出音频配置
	 * @throws IOException 当 I/O 错误发生时
	 * @since 2.1.0
	 */
	public static void addBgm(final MediaResource mainResource, final MediaResource bgmResource,
	                          final File outputFile, final Audio outputAudio) throws IOException {
		addBgm(mainResource, bgmResource, outputFile, outputAudio, DEFAULT_BGM_WEIGHT);
	}

	/**
	 * 为音频添加背景音乐并输出到输出流（使用默认权重，指定输出配置）
	 *
	 * @param mainResource 主音频资源
	 * @param bgmResource  背景音乐资源
	 * @param outputStream 输出流
	 * @param outputAudio  输出音频配置
	 * @throws IOException 当 I/O 错误发生时
	 * @since 2.1.0
	 */
	public static void addBgm(final MediaResource mainResource, final MediaResource bgmResource,
	                          final OutputStream outputStream, final Audio outputAudio) throws IOException {
		addBgm(mainResource, bgmResource, outputStream, outputAudio, DEFAULT_BGM_WEIGHT);
	}

	/**
	 * 为音频添加背景音乐并输出到文件（指定权重，使用源音频配置）
	 *
	 * @param mainResource 主音频资源
	 * @param bgmResource  背景音乐资源
	 * @param outputFile   输出文件
	 * @param bgmWeight    背景音乐权重
	 * @throws IOException 当 I/O 错误发生时
	 * @since 2.1.0
	 */
	public static void addBgm(final MediaResource mainResource, final MediaResource bgmResource,
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
	 * @throws IOException 当 I/O 错误发生时
	 * @since 2.1.0
	 */
	public static void addBgm(final MediaResource mainResource, final MediaResource bgmResource,
	                          final OutputStream outputStream, final float bgmWeight) throws IOException {
		addBgm(mainResource, bgmResource, outputStream, null, bgmWeight);
	}

	/**
	 * 为音频添加背景音乐并输出到文件（指定权重和输出配置）
	 *
	 * @param mainResource 主音频资源
	 * @param bgmResource  背景音乐资源
	 * @param outputFile   输出文件
	 * @param outputAudio  输出音频配置
	 * @param bgmWeight    背景音乐权重
	 * @throws IOException 当 I/O 错误发生时
	 * @since 2.1.0
	 */
	public static void addBgm(final MediaResource mainResource, final MediaResource bgmResource,
	                          final File outputFile, final Audio outputAudio, final float bgmWeight) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doAddBgm(mainResource, bgmResource, recorder, outputAudio, bgmWeight);
		}
	}

	/**
	 * 为音频添加背景音乐并输出到输出流（指定权重和输出配置）
	 *
	 * @param mainResource 主音频资源
	 * @param bgmResource  背景音乐资源
	 * @param outputStream 输出流
	 * @param outputAudio  输出音频配置
	 * @param bgmWeight    背景音乐权重
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 outputStream 为 null 时
	 * @since 2.1.0
	 */
	public static void addBgm(final MediaResource mainResource, final MediaResource bgmResource,
	                          final OutputStream outputStream, final Audio outputAudio, final float bgmWeight) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doAddBgm(mainResource, bgmResource, recorder, outputAudio, bgmWeight);
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
	public static void adjustSpeed(final MediaResource resource, final File outputFile, final float speed) throws IOException {
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
	public static void adjustSpeed(final MediaResource resource, final OutputStream outputStream, final float speed) throws IOException {
		adjustSpeed(resource, outputStream, speed, null);
	}

	/**
	 * 调整音频播放速度并输出到文件（指定输出配置）
	 *
	 * @param resource    输入音频资源
	 * @param outputFile  输出文件
	 * @param speed       播放速度（0.5-100）
	 * @param outputAudio 输出音频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是音频类型时
	 * @since 2.1.0
	 */
	public static void adjustSpeed(final MediaResource resource, final File outputFile, final float speed,
	                               final Audio outputAudio) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			FFmpegUtils.applyAudioFilter(grabber, recorder, outputAudio,
				FFmpegUtils.getAtempoFilter(speed), FrameType.AUDIO, false);
		}
	}

	/**
	 * 调整音频播放速度并输出到输出流（指定输出配置）
	 *
	 * @param resource     输入音频资源
	 * @param outputStream 输出流
	 * @param speed        播放速度（0.5-100）
	 * @param outputAudio  输出音频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 或 outputStream 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是音频类型时
	 * @since 2.1.0
	 */
	public static void adjustSpeed(final MediaResource resource, final OutputStream outputStream, final float speed,
	                               final Audio outputAudio) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			FFmpegUtils.applyAudioFilter(grabber, recorder, outputAudio,
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
	public static void adjustVolume(final MediaResource resource, final File outputFile, final float db) throws IOException {
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
	public static void adjustVolume(final MediaResource resource, final OutputStream outputStream, final float db) throws IOException {
		adjustVolume(resource, outputStream, db, null);
	}

	/**
	 * 调整音频音量并输出到文件（指定输出配置）
	 *
	 * @param resource    输入音频资源
	 * @param outputFile  输出文件
	 * @param db          音量变化的分贝值
	 * @param outputAudio 输出音频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是音频类型时
	 * @since 2.1.0
	 */
	public static void adjustVolume(final MediaResource resource, final File outputFile, final float db,
	                                final Audio outputAudio) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			FFmpegUtils.applyAudioFilter(grabber, recorder, outputAudio,
				FFmpegUtils.getVolumeFilter(db), FrameType.AUDIO, false);
		}
	}

	/**
	 * 调整音频音量并输出到输出流（指定输出配置）
	 *
	 * @param resource     输入音频资源
	 * @param outputStream 输出流
	 * @param db           音量变化的分贝值
	 * @param outputAudio  输出音频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 或 outputStream 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是音频类型时
	 * @since 2.1.0
	 */
	public static void adjustVolume(final MediaResource resource, final OutputStream outputStream, final float db,
	                                final Audio outputAudio) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			FFmpegUtils.applyAudioFilter(grabber, recorder, outputAudio,
				FFmpegUtils.getVolumeFilter(db), FrameType.AUDIO, false);
		}
	}

	/**
	 * 内部方法：执行添加背景音乐的操作
	 *
	 * @param mainResource 主音频资源
	 * @param bgmResource  背景音乐资源
	 * @param recorder     帧录制器
	 * @param outputAudio  输出音频配置
	 * @param bgmWeight    背景音乐权重
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 mainResource 或 bgmResource 为 null 时
	 * @throws IllegalArgumentException 当 mainResource 或 bgmResource 不是音频类型，或 bgmWeight 小于等于 0 时
	 * @since 2.1.0
	 */
	protected static void doAddBgm(final MediaResource mainResource, final MediaResource bgmResource,
	                               final FFmpegFrameRecorder recorder, final Audio outputAudio, final float bgmWeight) throws IOException {
		Validate.notNull(mainResource, "mainResource 不可为 null");
		Validate.isTrue(mainResource.isAudio(), "不是音频类型 MediaResource");
		Validate.notNull(bgmResource, "bgmResource 不可为 null");
		Validate.isTrue(bgmResource.isAudio(), "不是音频类型 MediaResource");
		Validate.isTrue(bgmWeight > 0, "bgmWeight 必须大于0");

		try (FFmpegFrameGrabber mainGrabber = FFmpegUtils.openFrameGrabber(mainResource);
		     FFmpegFrameGrabber bgmGrabber = FFmpegUtils.openFrameGrabber(bgmResource)) {
			Audio mainAudio = Audio.parse(mainGrabber);

			FFmpegUtils.applyFilter(List.of(mainGrabber, bgmGrabber), recorder, mainAudio,
				ObjectUtils.getIfNull(outputAudio, mainAudio),
				null, FFmpegUtils.getAddBgmFilter(mainGrabber, bgmGrabber, bgmWeight),
				FrameType.AUDIO, false);
		}
	}
}
