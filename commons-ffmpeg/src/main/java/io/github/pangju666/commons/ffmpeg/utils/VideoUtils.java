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

import io.github.pangju666.commons.ffmpeg.builder.FFmpegFiltersBuilder;
import io.github.pangju666.commons.ffmpeg.enums.FrameType;
import io.github.pangju666.commons.ffmpeg.io.FFmpegOutputStream;
import io.github.pangju666.commons.ffmpeg.io.resource.FFmpegResource;
import io.github.pangju666.commons.ffmpeg.model.Audio;
import io.github.pangju666.commons.ffmpeg.model.ImageWatermarkOption;
import io.github.pangju666.commons.ffmpeg.model.TextWatermarkOption;
import io.github.pangju666.commons.ffmpeg.model.Video;
import io.github.pangju666.commons.io.resource.IOResource;
import io.github.pangju666.commons.io.utils.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.ObjLongConsumer;

/**
 * 视频处理工具类
 * <p>
 * 提供视频文件的常用处理功能，包括：
 * <ul>
 *     <li>转码 - 将视频转换为不同格式或编码</li>
 *     <li>提取 - 提取视频流或音频流</li>
 *     <li>裁剪 - 截取视频片段</li>
 *     <li>拼接 - 将多个视频文件合并</li>
 *     <li>裁剪画面 - 通过矩形、偏移或中心方式裁剪视频画面</li>
 *     <li>速度调整 - 改变视频播放速度</li>
 *     <li>音频替换 - 替换视频的音频轨</li>
 *     <li>添加背景音乐 - 为视频添加背景音乐</li>
 *     <li>添加水印 - 添加文字水印或图片水印</li>
 *     <li>图像抓取 - 在指定时间点抓取帧图像或按间隔抓取关键帧</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @since 1.1.0
 */
public class VideoUtils {
	private static volatile Set<String> SUPPORTED_WRITE_IMAGE_FORMATS;

	/**
	 * 受保护的构造函数，防止实例化
	 *
	 * @since 1.1.0
	 */
	protected VideoUtils() {
	}

	/**
	 * 将视频资源转码并输出到文件
	 *
	 * @param resource    输入视频资源
	 * @param outputFile  输出文件
	 * @param outputVideo 输出视频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 或 outputVideo 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void transcode(final FFmpegResource resource, final File outputFile, final Video outputVideo) throws IOException {
		Validate.notNull(outputVideo, "outputVideo 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			FFmpegUtils.transcode(grabber, recorder, outputVideo, FrameType.ALL,
				false);
		}
	}

	/**
	 * 将视频资源转码并输出到输出流
	 *
	 * @param resource     输入视频资源
	 * @param outputStream 输出流
	 * @param outputVideo  输出视频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource、outputStream 或 outputVideo 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void transcode(final FFmpegResource resource, final OutputStream outputStream, final Video outputVideo) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(outputVideo, "outputVideo 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegOutputStream ffmpegOutputStream = new FFmpegOutputStream(outputStream, outputVideo.getFormat());
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(ffmpegOutputStream, 0)) {
			FFmpegUtils.transcode(grabber, recorder, outputVideo, FrameType.ALL,
				false);
		}
	}

	/**
	 * 从视频资源中提取视频流并输出到文件（使用源视频配置）
	 *
	 * @param resource   输入视频资源
	 * @param outputFile 输出文件
	 * @throws IOException 当 I/O 错误发生时
	 * @since 1.1.0
	 */
	public static void extractVideo(final FFmpegResource resource, final File outputFile) throws IOException {
		extractVideo(resource, outputFile, null);
	}

	/**
	 * 从视频资源中提取视频流并输出到输出流（使用源视频配置）
	 *
	 * @param resource     输入视频资源
	 * @param outputStream 输出流
	 * @throws IOException 当 I/O 错误发生时
	 * @since 1.1.0
	 */
	public static void extractVideo(final FFmpegResource resource, final OutputStream outputStream) throws IOException {
		extractVideo(resource, outputStream, null);
	}

	/**
	 * 从视频资源中提取视频流并输出到文件（指定输出配置）
	 *
	 * @param resource    输入视频资源
	 * @param outputFile  输出文件
	 * @param outputVideo 输出视频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void extractVideo(final FFmpegResource resource, final File outputFile, final Video outputVideo) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			FFmpegUtils.transcode(grabber, recorder, outputVideo, FrameType.VIDEO,
				false);
		}
	}

	/**
	 * 从视频资源中提取视频流并输出到输出流（指定输出配置）
	 *
	 * @param resource     输入视频资源
	 * @param outputStream 输出流
	 * @param outputVideo  输出视频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 outputStream 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void extractVideo(final FFmpegResource resource, final OutputStream outputStream, final Video outputVideo) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegOutputStream ffmpegOutputStream = new FFmpegOutputStream(outputStream, outputVideo, grabber);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(ffmpegOutputStream, 0)) {
			FFmpegUtils.transcode(grabber, recorder, outputVideo, FrameType.VIDEO,
				false);
		}
	}

	/**
	 * 从视频资源中提取音频流并输出到文件（指定输出配置）
	 *
	 * @param resource    输入视频资源
	 * @param outputFile  输出文件
	 * @param outputAudio 输出音频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void extractAudio(final FFmpegResource resource, final File outputFile, final Audio outputAudio) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			FFmpegUtils.transcode(grabber, recorder, outputAudio, FrameType.AUDIO,
				false);
		}
	}

	/**
	 * 从视频资源中提取音频流并输出到输出流（指定输出配置）
	 *
	 * @param resource     输入视频资源
	 * @param outputStream 输出流
	 * @param outputAudio  输出音频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 outputStream 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void extractAudio(final FFmpegResource resource, final OutputStream outputStream, final Audio outputAudio) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegOutputStream ffmpegOutputStream = new FFmpegOutputStream(outputStream, outputAudio, grabber);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(ffmpegOutputStream, 0)) {
			FFmpegUtils.transcode(grabber, recorder, outputAudio, FrameType.AUDIO,
				false);
		}
	}

	/**
	 * 从开头裁剪视频到指定时长并输出到文件（使用源视频配置）
	 *
	 * @param resource   输入视频资源
	 * @param outputFile 输出文件
	 * @param duration   裁剪时长
	 * @throws IOException 当 I/O 错误发生时
	 * @since 1.1.0
	 */
	public static void cut(final FFmpegResource resource, final File outputFile, final Duration duration) throws IOException {
		cut(resource, outputFile, null, Duration.ZERO, duration);
	}

	/**
	 * 从开头裁剪视频到指定时长并输出到输出流（使用源视频配置）
	 *
	 * @param resource     输入视频资源
	 * @param outputStream 输出流
	 * @param duration     裁剪时长
	 * @throws IOException 当 I/O 错误发生时
	 * @since 1.1.0
	 */
	public static void cut(final FFmpegResource resource, final OutputStream outputStream, final Duration duration) throws IOException {
		cut(resource, outputStream, null, Duration.ZERO, duration);
	}

	/**
	 * 裁剪指定时间段的视频并输出到文件（使用源视频配置）
	 *
	 * @param resource   输入视频资源
	 * @param outputFile 输出文件
	 * @param start      开始时间
	 * @param end        结束时间
	 * @throws IOException 当 I/O 错误发生时
	 * @since 1.1.0
	 */
	public static void cut(final FFmpegResource resource, final File outputFile, final Duration start, final Duration end) throws IOException {
		cut(resource, outputFile, null, start, end);
	}

	/**
	 * 裁剪指定时间段的视频并输出到输出流（使用源视频配置）
	 *
	 * @param resource     输入视频资源
	 * @param outputStream 输出流
	 * @param start        开始时间
	 * @param end          结束时间
	 * @throws IOException 当 I/O 错误发生时
	 * @since 1.1.0
	 */
	public static void cut(final FFmpegResource resource, final OutputStream outputStream, final Duration start,
	                       final Duration end) throws IOException {
		cut(resource, outputStream, null, start, end);
	}

	/**
	 * 从开头裁剪视频到指定时长并输出到文件（指定输出视频配置）
	 *
	 * @param resource    输入视频资源
	 * @param outputFile  输出文件
	 * @param outputVideo 输出视频配置
	 * @param duration    裁剪时长
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void cut(final FFmpegResource resource, final File outputFile, final Video outputVideo,
	                       final Duration duration) throws IOException {
		cut(resource, outputFile, outputVideo, Duration.ZERO, duration);
	}

	/**
	 * 从开头裁剪视频到指定时长并输出到输出流（指定输出视频配置）
	 *
	 * @param resource     输入视频资源
	 * @param outputStream 输出流
	 * @param outputVideo  输出视频配置
	 * @param duration     裁剪时长
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 或 outputStream 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void cut(final FFmpegResource resource, final OutputStream outputStream, final Video outputVideo,
	                       final Duration duration) throws IOException {
		cut(resource, outputStream, outputVideo, Duration.ZERO, duration);
	}

	/**
	 * 裁剪指定时间段的视频并输出到文件（指定输出视频配置）
	 *
	 * @param resource    输入视频资源
	 * @param outputFile  输出文件
	 * @param outputVideo 输出视频配置
	 * @param start       开始时间
	 * @param end         结束时间
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void cut(final FFmpegResource resource, final File outputFile, final Video outputVideo,
	                       final Duration start, final Duration end) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			FFmpegUtils.cut(grabber, recorder, outputVideo, start, end, FrameType.ALL,
				false);
		}
	}

	/**
	 * 裁剪指定时间段的视频并输出到输出流（指定输出音频配置）
	 *
	 * @param resource     输入视频资源
	 * @param outputStream 输出流
	 * @param outputVideo  输出视频配置
	 * @param start        开始时间
	 * @param end          结束时间
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 outputStream 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void cut(final FFmpegResource resource, final OutputStream outputStream, final Video outputVideo,
	                       final Duration start, final Duration end) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegOutputStream ffmpegOutputStream = new FFmpegOutputStream(outputStream, outputVideo, grabber);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(ffmpegOutputStream, 0)) {
			FFmpegUtils.cut(grabber, recorder, outputVideo, start, end, FrameType.ALL,
				false);
		}
	}

	/**
	 * 拼接多个视频资源并输出到文件（使用源视频配置）
	 *
	 * @param resources  视频资源集合
	 * @param outputFile 输出文件
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resources 为 null 或包含 null 元素时
	 * @throws IllegalArgumentException 当 resources 为空或包含非视频类型资源时
	 * @since 1.1.0
	 */
	public static void concat(final Collection<FFmpegResource> resources, final File outputFile) throws IOException {
		concat(resources, outputFile, null);
	}

	/**
	 * 拼接多个视频资源并输出到输出流（使用源视频配置）
	 *
	 * @param resources    视频资源集合
	 * @param outputStream 输出流
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resources、outputStream 为 null 或 resources 包含 null 元素时
	 * @throws IllegalArgumentException 当 resources 为空或包含非视频类型资源时
	 * @since 1.1.0
	 */
	public static void concat(final Collection<FFmpegResource> resources, final OutputStream outputStream) throws IOException {
		concat(resources, outputStream, null);
	}

	/**
	 * 拼接多个视频资源并输出到文件（指定输出配置）
	 *
	 * @param resources   视频资源集合
	 * @param outputFile  输出文件
	 * @param outputVideo 输出视频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resources 为 null 或包含 null 元素时
	 * @throws IllegalArgumentException 当 resources 为空或包含非视频类型资源时
	 * @since 1.1.0
	 */
	public static void concat(final Collection<FFmpegResource> resources, final File outputFile, final Video outputVideo) throws IOException {
		Validate.notEmpty(resources, "resources 不可为空");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.isTrue(resources.stream().allMatch(resource ->
			Objects.nonNull(resource) && resource.isVideo()), "resources 中存在为 null 或非视频类型的 FFmpegResource");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			boolean started = false;
			for (FFmpegResource resource : resources) {
				try (FFmpegFrameGrabber grabber = resource.openFrameGrabber()) {
					grabber.start();

					if (!started) {
						FFmpegUtils.initRecorder(recorder, grabber, outputVideo, FrameType.ALL);
						recorder.start();
						started = true;
					}

					FFmpegUtils.recordFrames(recorder, grabber, FrameType.ALL, false);
				}
			}
		}
	}

	/**
	 * 拼接多个视频资源并输出到输出流（指定输出配置）
	 *
	 * @param resources    视频资源集合
	 * @param outputStream 输出流
	 * @param outputVideo  输出视频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resources 或 outputStream 为 null 或 resources 包含 null 元素时
	 * @throws IllegalArgumentException 当 resources 为空或包含非视频类型资源时
	 * @since 1.1.0
	 */
	public static void concat(final Collection<FFmpegResource> resources, final OutputStream outputStream,
	                          final Video outputVideo) throws IOException {
		Validate.notEmpty(resources, "resources 不可为空");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.isTrue(resources.stream().allMatch(resource ->
			Objects.nonNull(resource) && resource.isVideo()), "resources 中存在为 null 或非视频类型的 FFmpegResource");

		FFmpegFrameRecorder recorder = null;
		FFmpegOutputStream fFmpegOutputStream = null;

		if (Objects.nonNull(outputVideo) && StringUtils.isNotBlank(outputVideo.getFormat())) {
			fFmpegOutputStream = new FFmpegOutputStream(outputStream, outputVideo);
			recorder = new FFmpegFrameRecorder(fFmpegOutputStream, 0);
		}

		boolean started = false;
		for (FFmpegResource resource : resources) {
			try (FFmpegFrameGrabber grabber = resource.openFrameGrabber()) {
				grabber.start();

				if (Objects.isNull(fFmpegOutputStream)) {
					fFmpegOutputStream = new FFmpegOutputStream(outputStream, grabber.getFormat());
					recorder = new FFmpegFrameRecorder(fFmpegOutputStream, 0);
				}

				if (!started) {
					recorder = new FFmpegFrameRecorder(fFmpegOutputStream, 0);
					FFmpegUtils.initRecorder(recorder, grabber, outputVideo, FrameType.ALL);
					recorder.start();
					started = true;
				}

				FFmpegUtils.recordFrames(recorder, grabber, FrameType.ALL, false);
			}
		}

		if (Objects.nonNull(recorder)) {
			recorder.close();
		}
		if (Objects.nonNull(fFmpegOutputStream)) {
			fFmpegOutputStream.close();
		}
	}

	/**
	 * 调整视频播放速度并输出到文件（使用源视频配置）
	 *
	 * @param resource   输入视频资源
	 * @param outputFile 输出文件
	 * @param speed      播放速度（0.5-100）
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void adjustSpeed(final FFmpegResource resource, final File outputFile, final float speed) throws IOException {
		adjustSpeed(resource, outputFile, speed, null);
	}

	/**
	 * 调整视频播放速度并输出到输出流（使用源视频配置）
	 *
	 * @param resource     输入视频资源
	 * @param outputStream 输出流
	 * @param speed        播放速度（0.5-100）
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 或 outputStream 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void adjustSpeed(final FFmpegResource resource, final OutputStream outputStream, final float speed) throws IOException {
		adjustSpeed(resource, outputStream, speed, null);
	}

	/**
	 * 调整视频播放速度并输出到文件（指定输出配置）
	 *
	 * @param resource    输入视频资源
	 * @param outputFile  输出文件
	 * @param speed       播放速度（0.5-100）
	 * @param outputVideo 输出视频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void adjustSpeed(final FFmpegResource resource, final File outputFile, final float speed,
	                               final Video outputVideo) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doAdjustSpeed(grabber, recorder, speed, outputVideo);
		}
	}

	/**
	 * 调整视频播放速度并输出到输出流（指定输出配置）
	 *
	 * @param resource     输入视频资源
	 * @param outputStream 输出流
	 * @param speed        播放速度（0.5-100）
	 * @param outputVideo  输出视频配置
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 outputStream 为 null 时
	 * @since 1.1.0
	 */
	public static void adjustSpeed(final FFmpegResource resource, final OutputStream outputStream, final float speed,
	                               final Video outputVideo) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegOutputStream ffmpegOutputStream = new FFmpegOutputStream(outputStream, outputVideo, grabber);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(ffmpegOutputStream, 0)) {
			doAdjustSpeed(grabber, recorder, speed, outputVideo);
		}
	}

	/**
	 * 在指定时间点抓取视频帧并输出到文件（自动检测输出格式）
	 *
	 * @param resource   输入视频资源
	 * @param timestamp  时间点
	 * @param outputFile 输出文件
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void grabImageAtTimestamp(final FFmpegResource resource, final Duration timestamp,
	                                        final File outputFile) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		String outputFormat = FilenameUtils.getExtension(outputFile.getName());
		Validate.isTrue(getSupportedWriteImageFormats().contains(outputFormat),
			"不支持输出为 " + outputFormat + " 格式");

		ImageIO.write(grabImageAtTimestamp(resource, timestamp), outputFormat, outputFile);
	}

	/**
	 * 在指定时间点抓取视频帧并输出到文件（指定输出格式）
	 *
	 * @param resource     输入视频资源
	 * @param timestamp    时间点
	 * @param outputFile   输出文件
	 * @param outputFormat 输出图片格式
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void grabImageAtTimestamp(final FFmpegResource resource, final Duration timestamp,
	                                        final File outputFile, final String outputFormat) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");
		Validate.isTrue(getSupportedWriteImageFormats().contains(outputFormat),
			"不支持输出为 " + outputFormat + " 格式");

		FileUtils.forceMkdirParent(outputFile);

		ImageIO.write(grabImageAtTimestamp(resource, timestamp), outputFormat, outputFile);
	}

	/**
	 * 在指定时间点抓取视频帧并输出到图像输出流（指定输出格式）
	 *
	 * @param resource     输入视频资源
	 * @param timestamp    时间点
	 * @param outputStream 图像输出流
	 * @param outputFormat 输出图片格式
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 或 outputStream 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void grabImageAtTimestamp(final FFmpegResource resource, final Duration timestamp,
	                                        final ImageOutputStream outputStream, final String outputFormat) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");
		Validate.isTrue(getSupportedWriteImageFormats().contains(outputFormat),
			"不支持输出为 " + outputFormat + " 格式");

		ImageIO.write(grabImageAtTimestamp(resource, timestamp), outputFormat, outputStream);
	}

	/**
	 * 在指定时间点抓取视频帧并输出到输出流（指定输出格式）
	 *
	 * @param resource     输入视频资源
	 * @param timestamp    时间点
	 * @param outputStream 输出流
	 * @param outputFormat 输出图片格式
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 或 outputStream 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void grabImageAtTimestamp(final FFmpegResource resource, final Duration timestamp,
	                                        final OutputStream outputStream, final String outputFormat) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");
		Validate.isTrue(getSupportedWriteImageFormats().contains(outputFormat),
			"不支持输出为 " + outputFormat + " 格式");

		ImageIO.write(grabImageAtTimestamp(resource, timestamp), outputFormat, outputStream);
	}

	/**
	 * 在指定时间点抓取视频帧并返回 BufferedImage
	 *
	 * @param resource  输入视频资源
	 * @param timestamp 时间点
	 * @return 抓取的图像
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static BufferedImage grabImageAtTimestamp(final FFmpegResource resource, final Duration timestamp) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber()) {
			return FFmpegUtils.grabImageAtTimestamp(grabber, timestamp);
		}
	}

	/**
	 * 按指定间隔抓取视频关键帧并返回 BufferedImage 列表
	 *
	 * @param resource 输入视频资源
	 * @param interval 间隔时间
	 * @param timeUnit 时间单位
	 * @return 抓取的图像列表
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static List<BufferedImage> grabImagePeriodically(final FFmpegResource resource, final long interval,
	                                                        final TimeUnit timeUnit) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber()) {
			return FFmpegUtils.grabImagePeriodically(grabber, interval, timeUnit);
		}
	}

	/**
	 * 按指定间隔抓取视频关键帧并通过消费者回调处理
	 *
	 * @param resource 输入视频资源
	 * @param interval 间隔时间
	 * @param timeUnit 时间单位
	 * @param consumer 图像消费者，接收图像和对应的时间戳（微秒）
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 或 consumer 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void grabImagePeriodically(final FFmpegResource resource, final long interval, final TimeUnit timeUnit,
	                                         final ObjLongConsumer<BufferedImage> consumer) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");
		Validate.notNull(consumer, "consumer 不可为 null");
		Validate.notNull(timeUnit, "timeUnit 不可为 null");

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber()) {
			FFmpegUtils.grabImagePeriodically(grabber, interval, timeUnit, consumer);
		}
	}

	/**
	 * 按指定间隔抓取视频关键帧并保存到指定目录（使用默认文件名格式化器）
	 *
	 * @param resource     输入视频资源
	 * @param interval     间隔时间
	 * @param timeUnit     时间单位
	 * @param outputFormat 输出图片格式
	 * @param outputDir    输出目录
	 * @throws IOException 当 I/O 错误发生时
	 * @since 1.1.0
	 */
	public static void grabImagePeriodically(final FFmpegResource resource, final long interval, final TimeUnit timeUnit,
	                                         final String outputFormat, final File outputDir) throws IOException {
		grabImagePeriodically(resource, interval, timeUnit, outputFormat, outputDir, Object::toString);
	}

	/**
	 * 按指定间隔抓取视频关键帧并保存到指定目录（自定义文件名格式化器）
	 *
	 * @param resource          输入视频资源
	 * @param interval          间隔时间
	 * @param timeUnit          时间单位
	 * @param outputFormat      输出图片格式
	 * @param outputDir         输出目录
	 * @param filenameFormatter 文件名格式化器，接收时间戳返回文件名（不含扩展名）
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void grabImagePeriodically(final FFmpegResource resource, final long interval, final TimeUnit timeUnit,
	                                         final String outputFormat, final File outputDir,
	                                         final Function<Long, String> filenameFormatter) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");
		Validate.isTrue(getSupportedWriteImageFormats().contains(outputFormat),
			"不支持输出为 " + outputFormat + " 格式");
		Validate.notNull(outputDir, "outputDir 不可为 null");

		FileUtils.forceMkdir(outputDir);

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber()) {
			FFmpegUtils.grabImagePeriodically(grabber, interval, timeUnit, (image, timestamp) -> {
				String filename = filenameFormatter.apply(timestamp);
				File outputFile = new File(outputDir, filename + "." + outputFormat);
				try {
					ImageIO.write(image, outputFormat, outputFile);
				} catch (IOException e) {
					throw ExceptionUtils.asRuntimeException(e);
				}
			});
		}
	}

	/**
	 * 通过矩形区域裁剪视频画面并输出到文件（输出裁剪分辨率）
	 *
	 * @param resource   输入视频资源
	 * @param outputFile 输出文件
	 * @param x          裁剪区域左上角 x 坐标
	 * @param y          裁剪区域左上角 y 坐标
	 * @param width      裁剪宽度
	 * @param height     裁剪高度
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void cropByRect(final FFmpegResource resource, final File outputFile, final int x, final int y,
	                              final int width, final int height) throws IOException {
		cropByRect(resource, outputFile, x, y, width, height, true);
	}

	/**
	 * 通过矩形区域裁剪视频画面并输出到输出流（输出裁剪分辨率）
	 *
	 * @param resource     输入视频资源
	 * @param outputStream 输出流
	 * @param x            裁剪区域左上角 x 坐标
	 * @param y            裁剪区域左上角 y 坐标
	 * @param width        裁剪宽度
	 * @param height       裁剪高度
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 或 outputStream 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void cropByRect(final FFmpegResource resource, final OutputStream outputStream, final int x, final int y,
	                              final int width, final int height) throws IOException {
		cropByRect(resource, outputStream, x, y, width, height, true);
	}

	/**
	 * 通过矩形区域裁剪视频画面并输出到文件（指定是否输出裁剪分辨率）
	 *
	 * @param resource             输入视频资源
	 * @param outputFile           输出文件
	 * @param x                    裁剪区域左上角 x 坐标
	 * @param y                    裁剪区域左上角 y 坐标
	 * @param width                裁剪宽度
	 * @param height               裁剪高度
	 * @param outputCropResolution 是否输出裁剪后的分辨率
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void cropByRect(final FFmpegResource resource, final File outputFile, final int x, final int y,
	                              final int width, final int height, final boolean outputCropResolution) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");
		Validate.isTrue(x >= 0, "x 不能小于0");
		Validate.isTrue(y >= 0, "y 不能小于0");
		Validate.isTrue(width > 0, "width 不能小于0");
		Validate.isTrue(height > 0, "height 不能小于0");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doCropByRect(grabber, recorder, x, y, width, height, null, outputCropResolution);
		}
	}

	/**
	 * 通过矩形区域裁剪视频画面并输出到输出流（指定是否输出裁剪分辨率）
	 *
	 * @param resource             输入视频资源
	 * @param outputStream         输出流
	 * @param x                    裁剪区域左上角 x 坐标
	 * @param y                    裁剪区域左上角 y 坐标
	 * @param width                裁剪宽度
	 * @param height               裁剪高度
	 * @param outputCropResolution 是否输出裁剪后的分辨率
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 outputStream 为 null 时
	 * @since 1.1.0
	 */
	public static void cropByRect(final FFmpegResource resource, final OutputStream outputStream, final int x, final int y,
	                              final int width, final int height, final boolean outputCropResolution) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");
		Validate.isTrue(x >= 0, "x 不能小于0");
		Validate.isTrue(y >= 0, "y 不能小于0");
		Validate.isTrue(width > 0, "width 不能小于0");
		Validate.isTrue(height > 0, "height 不能小于0");

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegOutputStream ffmpegOutputStream = new FFmpegOutputStream(outputStream, grabber);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(ffmpegOutputStream, 0)) {
			doCropByRect(grabber, recorder, x, y, width, height, null, outputCropResolution);
		}
	}

	/**
	 * 通过矩形区域裁剪视频画面并输出到文件（指定输出视频配置）
	 *
	 * @param resource    输入视频资源
	 * @param outputFile  输出文件
	 * @param x           裁剪区域左上角 x 坐标
	 * @param y           裁剪区域左上角 y 坐标
	 * @param width       裁剪宽度
	 * @param height      裁剪高度
	 * @param outputVideo 输出视频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void cropByRect(final FFmpegResource resource, final File outputFile, final int x, final int y,
	                              final int width, final int height, final Video outputVideo) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputVideo, "outputVideo 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");
		Validate.isTrue(x >= 0, "x 不能小于0");
		Validate.isTrue(y >= 0, "y 不能小于0");
		Validate.isTrue(width > 0, "width 不能小于0");
		Validate.isTrue(height > 0, "height 不能小于0");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doCropByRect(grabber, recorder, x, y, width, height, outputVideo, false);
		}
	}

	/**
	 * 通过矩形区域裁剪视频画面并输出到输出流（指定输出视频配置）
	 *
	 * @param resource     输入视频资源
	 * @param outputStream 输出流
	 * @param x            裁剪区域左上角 x 坐标
	 * @param y            裁剪区域左上角 y 坐标
	 * @param width        裁剪宽度
	 * @param height       裁剪高度
	 * @param outputVideo  输出视频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 或 outputStream 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void cropByRect(final FFmpegResource resource, final OutputStream outputStream, final int x, final int y,
	                              final int width, final int height, final Video outputVideo) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(outputVideo, "outputVideo 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(x >= 0, "x 不能小于0");
		Validate.isTrue(y >= 0, "y 不能小于0");
		Validate.isTrue(width > 0, "width 不能小于0");
		Validate.isTrue(height > 0, "height 不能小于0");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegOutputStream ffmpegOutputStream = new FFmpegOutputStream(outputStream, outputVideo);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(ffmpegOutputStream, 0)) {
			doCropByRect(grabber, recorder, x, y, width, height, outputVideo, false);
		}
	}

	/**
	 * 通过边距偏移裁剪视频画面并输出到文件（输出裁剪分辨率）
	 *
	 * @param resource     输入视频资源
	 * @param outputFile   输出文件
	 * @param topOffset    顶部偏移
	 * @param bottomOffset 底部偏移
	 * @param leftOffset   左侧偏移
	 * @param rightOffset  右侧偏移
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void cropByOffset(final FFmpegResource resource, final File outputFile, final int topOffset,
	                                final int bottomOffset, final int leftOffset, final int rightOffset) throws IOException {
		cropByOffset(resource, outputFile, topOffset, bottomOffset, leftOffset, rightOffset, true);
	}

	/**
	 * 通过边距偏移裁剪视频画面并输出到输出流（输出裁剪分辨率）
	 *
	 * @param resource     输入视频资源
	 * @param outputStream 输出流
	 * @param topOffset    顶部偏移
	 * @param bottomOffset 底部偏移
	 * @param leftOffset   左侧偏移
	 * @param rightOffset  右侧偏移
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 或 outputStream 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void cropByOffset(final FFmpegResource resource, final OutputStream outputStream, final int topOffset,
	                                final int bottomOffset, final int leftOffset, final int rightOffset) throws IOException {
		cropByOffset(resource, outputStream, topOffset, bottomOffset, leftOffset, rightOffset, true);
	}

	/**
	 * 通过边距偏移裁剪视频画面并输出到文件（指定是否输出裁剪分辨率）
	 *
	 * @param resource             输入视频资源
	 * @param outputFile           输出文件
	 * @param topOffset            顶部偏移
	 * @param bottomOffset         底部偏移
	 * @param leftOffset           左侧偏移
	 * @param rightOffset          右侧偏移
	 * @param outputCropResolution 是否输出裁剪后的分辨率
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void cropByOffset(final FFmpegResource resource, final File outputFile, final int topOffset,
	                                final int bottomOffset, final int leftOffset, final int rightOffset,
	                                final boolean outputCropResolution) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");
		Validate.isTrue(topOffset >= 0 && bottomOffset >= 0 && leftOffset >= 0 && rightOffset >= 0,
			"offset 不能小于0");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doCropByOffset(grabber, recorder, topOffset, bottomOffset, leftOffset, rightOffset, null,
				outputCropResolution);
		}
	}

	/**
	 * 通过边距偏移裁剪视频画面并输出到输出流（指定是否输出裁剪分辨率）
	 *
	 * @param resource             输入视频资源
	 * @param outputStream         输出流
	 * @param topOffset            顶部偏移
	 * @param bottomOffset         底部偏移
	 * @param leftOffset           左侧偏移
	 * @param rightOffset          右侧偏移
	 * @param outputCropResolution 是否输出裁剪后的分辨率
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 outputStream 为 null 时
	 * @since 1.1.0
	 */
	public static void cropByOffset(final FFmpegResource resource, final OutputStream outputStream, final int topOffset,
	                                final int bottomOffset, final int leftOffset, final int rightOffset,
	                                final boolean outputCropResolution) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");
		Validate.isTrue(topOffset >= 0 && bottomOffset >= 0 && leftOffset >= 0 && rightOffset >= 0,
			"offset 不能小于0");

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegOutputStream ffmpegOutputStream = new FFmpegOutputStream(outputStream, grabber);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(ffmpegOutputStream, 0)) {
			doCropByOffset(grabber, recorder, topOffset, bottomOffset, leftOffset, rightOffset, null,
				outputCropResolution);
		}
	}

	/**
	 * 通过边距偏移裁剪视频画面并输出到文件（指定输出视频配置）
	 *
	 * @param resource     输入视频资源
	 * @param outputFile   输出文件
	 * @param topOffset    顶部偏移
	 * @param bottomOffset 底部偏移
	 * @param leftOffset   左侧偏移
	 * @param rightOffset  右侧偏移
	 * @param outputVideo  输出视频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void cropByOffset(final FFmpegResource resource, final File outputFile, final int topOffset,
	                                final int bottomOffset, final int leftOffset, final int rightOffset,
	                                final Video outputVideo) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputVideo, "outputVideo 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");
		Validate.isTrue(topOffset >= 0 && bottomOffset >= 0 && leftOffset >= 0 && rightOffset >= 0,
			"offset 不能小于0");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doCropByOffset(grabber, recorder, topOffset, bottomOffset, leftOffset, rightOffset, outputVideo,
				false);
		}
	}

	/**
	 * 通过边距偏移裁剪视频画面并输出到输出流（指定输出视频配置）
	 *
	 * @param resource     输入视频资源
	 * @param outputStream 输出流
	 * @param topOffset    顶部偏移
	 * @param bottomOffset 底部偏移
	 * @param leftOffset   左侧偏移
	 * @param rightOffset  右侧偏移
	 * @param outputVideo  输出视频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 或 outputStream 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void cropByOffset(final FFmpegResource resource, final OutputStream outputStream,
	                                final int topOffset, final int bottomOffset, final int leftOffset,
	                                final int rightOffset, final Video outputVideo) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputVideo, "outputVideo 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");
		Validate.isTrue(topOffset >= 0 && bottomOffset >= 0 && leftOffset >= 0 && rightOffset >= 0,
			"offset 不能小于0");

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegOutputStream ffmpegOutputStream = new FFmpegOutputStream(outputStream, outputVideo);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(ffmpegOutputStream, 0)) {
			doCropByOffset(grabber, recorder, topOffset, bottomOffset, leftOffset, rightOffset, outputVideo,
				false);
		}
	}

	/**
	 * 通过中心裁剪视频画面并输出到文件（输出裁剪分辨率）
	 *
	 * @param resource   输入视频资源
	 * @param outputFile 输出文件
	 * @param width      裁剪宽度
	 * @param height     裁剪高度
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void cropByCenter(final FFmpegResource resource, final File outputFile, final int width,
	                                final int height) throws IOException {
		cropByCenter(resource, outputFile, width, height, true);
	}

	/**
	 * 通过中心裁剪视频画面并输出到输出流（输出裁剪分辨率）
	 *
	 * @param resource     输入视频资源
	 * @param outputStream 输出流
	 * @param width        裁剪宽度
	 * @param height       裁剪高度
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 或 outputStream 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void cropByCenter(final FFmpegResource resource, final OutputStream outputStream, final int width,
	                                final int height) throws IOException {
		cropByCenter(resource, outputStream, width, height, true);
	}

	/**
	 * 通过中心裁剪视频画面并输出到文件（指定是否输出裁剪分辨率）
	 *
	 * @param resource             输入视频资源
	 * @param outputFile           输出文件
	 * @param width                裁剪宽度
	 * @param height               裁剪高度
	 * @param outputCropResolution 是否输出裁剪后的分辨率
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void cropByCenter(final FFmpegResource resource, final File outputFile, final int width,
	                                final int height, final boolean outputCropResolution) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");
		Validate.isTrue(width > 0, "width 不能小于0");
		Validate.isTrue(height > 0, "height 不能小于0");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doCropByCenter(grabber, recorder, width, height, null, outputCropResolution);
		}
	}

	/**
	 * 通过中心裁剪视频画面并输出到输出流（指定是否输出裁剪分辨率）
	 *
	 * @param resource             输入视频资源
	 * @param outputStream         输出流
	 * @param width                裁剪宽度
	 * @param height               裁剪高度
	 * @param outputCropResolution 是否输出裁剪后的分辨率
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 outputStream 为 null 时
	 * @since 1.1.0
	 */
	public static void cropByCenter(final FFmpegResource resource, final OutputStream outputStream, final int width,
	                                final int height, final boolean outputCropResolution) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");
		Validate.isTrue(width > 0, "width 不能小于0");
		Validate.isTrue(height > 0, "height 不能小于0");

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegOutputStream ffmpegOutputStream = new FFmpegOutputStream(outputStream, grabber);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(ffmpegOutputStream, 0)) {
			doCropByCenter(grabber, recorder, width, height, null, outputCropResolution);
		}
	}

	/**
	 * 通过中心裁剪视频画面并输出到文件（指定输出视频配置）
	 *
	 * @param resource    输入视频资源
	 * @param outputFile  输出文件
	 * @param width       裁剪宽度
	 * @param height      裁剪高度
	 * @param outputVideo 输出视频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void cropByCenter(final FFmpegResource resource, final File outputFile, final int width,
	                                final int height, final Video outputVideo) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputVideo, "outputVideo 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");
		Validate.isTrue(width > 0, "width 不能小于0");
		Validate.isTrue(height > 0, "height 不能小于0");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doCropByCenter(grabber, recorder, width, height, outputVideo, false);
		}
	}

	/**
	 * 通过中心裁剪视频画面并输出到输出流（指定输出视频配置）
	 *
	 * @param resource     输入视频资源
	 * @param outputStream 输出流
	 * @param width        裁剪宽度
	 * @param height       裁剪高度
	 * @param outputVideo  输出视频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 或 outputStream 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void cropByCenter(final FFmpegResource resource, final OutputStream outputStream, final int width,
	                                final int height, final Video outputVideo) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputVideo, "outputVideo 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");
		Validate.isTrue(width > 0, "width 不能小于0");
		Validate.isTrue(height > 0, "height 不能小于0");

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegOutputStream ffmpegOutputStream = new FFmpegOutputStream(outputStream, outputVideo);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(ffmpegOutputStream, 0)) {
			doCropByCenter(grabber, recorder, width, height, outputVideo, false);
		}
	}

	/**
	 * 替换视频的音频轨并输出到文件（不循环填充音频）
	 *
	 * @param videoResource 视频资源
	 * @param audioResource 音频资源
	 * @param outputFile    输出文件
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 videoResource 或 audioResource 为 null 时
	 * @throws IllegalArgumentException 当 videoResource 不是视频类型或 audioResource 不是音频类型时
	 * @since 1.1.0
	 */
	public static void replaceAudio(final FFmpegResource videoResource, final FFmpegResource audioResource,
	                                final File outputFile) throws IOException {
		replaceAudio(videoResource, audioResource, outputFile, null, false);
	}

	/**
	 * 替换视频的音频轨并输出到输出流（不循环填充音频）
	 *
	 * @param videoResource 视频资源
	 * @param audioResource 音频资源
	 * @param outputStream  输出流
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 outputStream、videoResource 或 audioResource 为 null 时
	 * @throws IllegalArgumentException 当 videoResource 不是视频类型或 audioResource 不是音频类型时
	 * @since 1.1.0
	 */
	public static void replaceAudio(final FFmpegResource videoResource, final FFmpegResource audioResource,
	                                final OutputStream outputStream) throws IOException {
		replaceAudio(videoResource, audioResource, outputStream, null, false);
	}

	/**
	 * 替换视频的音频轨并输出到文件（指定是否循环填充音频）
	 *
	 * @param videoResource 视频资源
	 * @param audioResource 音频资源
	 * @param outputFile    输出文件
	 * @param loopFillAudio 是否循环填充音频以匹配视频时长
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 videoResource 或 audioResource 为 null 时
	 * @throws IllegalArgumentException 当 videoResource 不是视频类型或 audioResource 不是音频类型时
	 * @since 1.1.0
	 */
	public static void replaceAudio(final FFmpegResource videoResource, final FFmpegResource audioResource,
	                                final File outputFile, final boolean loopFillAudio) throws IOException {
		replaceAudio(videoResource, audioResource, outputFile, null, loopFillAudio);
	}

	/**
	 * 替换视频的音频轨并输出到输出流（指定是否循环填充音频）
	 *
	 * @param videoResource 视频资源
	 * @param audioResource 音频资源
	 * @param outputStream  输出流
	 * @param loopFillAudio 是否循环填充音频以匹配视频时长
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 outputStream、videoResource 或 audioResource 为 null 时
	 * @throws IllegalArgumentException 当 videoResource 不是视频类型或 audioResource 不是音频类型时
	 * @since 1.1.0
	 */
	public static void replaceAudio(final FFmpegResource videoResource, final FFmpegResource audioResource,
	                                final OutputStream outputStream, final boolean loopFillAudio) throws IOException {
		replaceAudio(videoResource, audioResource, outputStream, null, loopFillAudio);
	}

	/**
	 * 替换视频的音频轨并输出到文件（指定输出视频配置，不循环填充音频）
	 *
	 * @param videoResource 视频资源
	 * @param audioResource 音频资源
	 * @param outputFile    输出文件
	 * @param outputVideo   输出视频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 videoResource 或 audioResource 为 null 时
	 * @throws IllegalArgumentException 当 videoResource 不是视频类型或 audioResource 不是音频类型时
	 * @since 1.1.0
	 */
	public static void replaceAudio(final FFmpegResource videoResource, final FFmpegResource audioResource,
	                                final File outputFile, final Video outputVideo) throws IOException {
		replaceAudio(videoResource, audioResource, outputFile, outputVideo, false);
	}

	/**
	 * 替换视频的音频轨并输出到输出流（指定输出视频配置，不循环填充音频）
	 *
	 * @param videoResource 视频资源
	 * @param audioResource 音频资源
	 * @param outputStream  输出流
	 * @param outputVideo   输出视频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 outputStream、videoResource 或 audioResource 为 null 时
	 * @throws IllegalArgumentException 当 videoResource 不是视频类型或 audioResource 不是音频类型时
	 * @since 1.1.0
	 */
	public static void replaceAudio(final FFmpegResource videoResource, final FFmpegResource audioResource,
	                                final OutputStream outputStream, final Video outputVideo) throws IOException {
		replaceAudio(videoResource, audioResource, outputStream, outputVideo, false);
	}

	/**
	 * 替换视频的音频轨并输出到文件（指定输出视频配置和是否循环填充音频）
	 *
	 * @param videoResource 视频资源
	 * @param audioResource 音频资源
	 * @param outputFile    输出文件
	 * @param outputVideo   输出视频配置
	 * @param loopFillAudio 是否循环填充音频以匹配视频时长
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 videoResource 或 audioResource 为 null 时
	 * @throws IllegalArgumentException 当 videoResource 不是视频类型或 audioResource 不是音频类型时
	 * @since 1.1.0
	 */
	public static void replaceAudio(final FFmpegResource videoResource, final FFmpegResource audioResource,
	                                final File outputFile, final Video outputVideo, final boolean loopFillAudio) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notNull(videoResource, "resource 不可为 null");
		Validate.isTrue(videoResource.isVideo(), "不是视频类型 FFmpegResource");
		Validate.notNull(audioResource, "resource 不可为 null");
		Validate.isTrue(audioResource.isAudio(), "不是音频类型 FFmpegResource");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber videoGrabber = videoResource.openFrameGrabber();
		     FFmpegFrameGrabber audioGrabber = audioResource.openFrameGrabber();
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doReplaceAudio(videoGrabber, audioGrabber, recorder, outputVideo, loopFillAudio);
		}
	}

	/**
	 * 替换视频的音频轨并输出到输出流（指定输出视频配置和是否循环填充音频）
	 *
	 * @param videoResource 视频资源
	 * @param audioResource 音频资源
	 * @param outputStream  输出流
	 * @param outputVideo   输出视频配置
	 * @param loopFillAudio 是否循环填充音频以匹配视频时长
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 outputStream、videoResource 或 audioResource 为 null 时
	 * @throws IllegalArgumentException 当 videoResource 不是视频类型或 audioResource 不是音频类型时
	 * @since 1.1.0
	 */
	public static void replaceAudio(final FFmpegResource videoResource, final FFmpegResource audioResource,
	                                final OutputStream outputStream, final Video outputVideo, final boolean loopFillAudio) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(videoResource, "resource 不可为 null");
		Validate.isTrue(videoResource.isVideo(), "不是视频类型 FFmpegResource");
		Validate.notNull(audioResource, "resource 不可为 null");
		Validate.isTrue(audioResource.isAudio(), "不是音频类型 FFmpegResource");

		try (FFmpegFrameGrabber videoGrabber = videoResource.openFrameGrabber();
		     FFmpegFrameGrabber audioGrabber = audioResource.openFrameGrabber();
		     FFmpegOutputStream ffmpegOutputStream = new FFmpegOutputStream(outputStream, outputVideo, videoGrabber);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(ffmpegOutputStream, 0)) {
			doReplaceAudio(videoGrabber, audioGrabber, recorder, outputVideo, loopFillAudio);
		}
	}

	/**
	 * 为视频添加背景音乐并输出到文件（使用默认权重和源视频配置）
	 *
	 * @param videoResource 视频资源
	 * @param bgmResource   背景音乐资源
	 * @param outputFile    输出文件
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 videoResource 或 bgmResource 为 null 时
	 * @throws IllegalArgumentException 当 videoResource 不是视频类型或 bgmResource 不是音频类型时
	 * @since 1.1.0
	 */
	public static void addBgm(final FFmpegResource videoResource, final FFmpegResource bgmResource,
	                          final File outputFile) throws IOException {
		addBgm(videoResource, bgmResource, outputFile, null, AudioUtils.DEFAULT_BGM_WEIGHT);
	}

	/**
	 * 为视频添加背景音乐并输出到输出流（使用默认权重和源视频配置）
	 *
	 * @param videoResource 视频资源
	 * @param bgmResource   背景音乐资源
	 * @param outputStream  输出流
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 outputStream、videoResource 或 bgmResource 为 null 时
	 * @throws IllegalArgumentException 当 videoResource 不是视频类型或 bgmResource 不是音频类型时
	 * @since 1.1.0
	 */
	public static void addBgm(final FFmpegResource videoResource, final FFmpegResource bgmResource,
	                          final OutputStream outputStream) throws IOException {
		addBgm(videoResource, bgmResource, outputStream, null, AudioUtils.DEFAULT_BGM_WEIGHT);
	}

	/**
	 * 为视频添加背景音乐并输出到文件（使用默认权重，指定输出配置）
	 *
	 * @param videoResource 视频资源
	 * @param bgmResource   背景音乐资源
	 * @param outputFile    输出文件
	 * @param outputVideo   输出视频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 videoResource 或 bgmResource 为 null 时
	 * @throws IllegalArgumentException 当 videoResource 不是视频类型或 bgmResource 不是音频类型时
	 * @since 1.1.0
	 */
	public static void addBgm(final FFmpegResource videoResource, final FFmpegResource bgmResource,
	                          final File outputFile, final Video outputVideo) throws IOException {
		addBgm(videoResource, bgmResource, outputFile, outputVideo, AudioUtils.DEFAULT_BGM_WEIGHT);
	}

	/**
	 * 为视频添加背景音乐并输出到输出流（使用默认权重，指定输出配置）
	 *
	 * @param videoResource 视频资源
	 * @param bgmResource   背景音乐资源
	 * @param outputStream  输出流
	 * @param outputVideo   输出视频配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 outputStream、videoResource 或 bgmResource 为 null 时
	 * @throws IllegalArgumentException 当 videoResource 不是视频类型或 bgmResource 不是音频类型时
	 * @since 1.1.0
	 */
	public static void addBgm(final FFmpegResource videoResource, final FFmpegResource bgmResource,
	                          final OutputStream outputStream, final Video outputVideo) throws IOException {
		addBgm(videoResource, bgmResource, outputStream, outputVideo, AudioUtils.DEFAULT_BGM_WEIGHT);
	}

	/**
	 * 为视频添加背景音乐并输出到文件（指定权重，使用源视频配置）
	 *
	 * @param videoResource 视频资源
	 * @param bgmResource   背景音乐资源
	 * @param outputFile    输出文件
	 * @param bgmWeight     背景音乐权重
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 videoResource 或 bgmResource 为 null 时
	 * @throws IllegalArgumentException 当 videoResource 不是视频类型或 bgmResource 不是音频类型时
	 * @since 1.1.0
	 */
	public static void addBgm(final FFmpegResource videoResource, final FFmpegResource bgmResource,
	                          final File outputFile, final float bgmWeight) throws IOException {
		addBgm(videoResource, bgmResource, outputFile, null, bgmWeight);
	}

	/**
	 * 为视频添加背景音乐并输出到输出流（指定权重，使用源视频配置）
	 *
	 * @param videoResource 视频资源
	 * @param bgmResource   背景音乐资源
	 * @param outputStream  输出流
	 * @param bgmWeight     背景音乐权重
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 outputStream、videoResource 或 bgmResource 为 null 时
	 * @throws IllegalArgumentException 当 videoResource 不是视频类型或 bgmResource 不是音频类型时
	 * @since 1.1.0
	 */
	public static void addBgm(final FFmpegResource videoResource, final FFmpegResource bgmResource,
	                          final OutputStream outputStream, final float bgmWeight) throws IOException {
		addBgm(videoResource, bgmResource, outputStream, null, bgmWeight);
	}

	/**
	 * 为视频添加背景音乐并输出到文件（指定权重和输出配置）
	 *
	 * @param videoResource 视频资源
	 * @param bgmResource   背景音乐资源
	 * @param outputFile    输出文件
	 * @param outputVideo   输出视频配置
	 * @param bgmWeight     背景音乐权重
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 videoResource 或 bgmResource 为 null 时
	 * @throws IllegalArgumentException 当 videoResource 不是视频类型或 bgmResource 不是音频类型时
	 * @since 1.1.0
	 */
	public static void addBgm(final FFmpegResource videoResource, final FFmpegResource bgmResource,
	                          final File outputFile, final Video outputVideo, final float bgmWeight) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notNull(videoResource, "videoResource 不可为 null");
		Validate.notNull(bgmResource, "bgmResource 不可为 null");
		Validate.isTrue(videoResource.isVideo(), "不是视频类型 FFmpegResource");
		Validate.isTrue(bgmResource.isAudio(), "不是音频类型 FFmpegResource");
		Validate.isTrue(bgmWeight > 0, "bgmWeight 必须大于0");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber videoGrabber = videoResource.openFrameGrabber();
		     FFmpegFrameGrabber bgmGrabber = bgmResource.openFrameGrabber();
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doAddBgm(videoGrabber, bgmGrabber, recorder, outputVideo, bgmWeight);
		}
	}

	/**
	 * 为视频添加背景音乐并输出到输出流（指定权重和输出配置）
	 *
	 * @param videoResource 视频资源
	 * @param bgmResource   背景音乐资源
	 * @param outputStream  输出流
	 * @param outputVideo   输出视频配置
	 * @param bgmWeight     背景音乐权重
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 outputStream、videoResource 或 bgmResource 为 null 时
	 * @throws IllegalArgumentException 当 videoResource 不是视频类型或 bgmResource 不是音频类型时
	 * @since 1.1.0
	 */
	public static void addBgm(final FFmpegResource videoResource, final FFmpegResource bgmResource,
	                          final OutputStream outputStream, final Video outputVideo, final float bgmWeight) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(videoResource, "videoResource 不可为 null");
		Validate.notNull(bgmResource, "bgmResource 不可为 null");
		Validate.isTrue(videoResource.isVideo(), "不是视频类型 FFmpegResource");
		Validate.isTrue(bgmResource.isAudio(), "不是音频类型 FFmpegResource");
		Validate.isTrue(bgmWeight > 0, "bgmWeight 必须大于0");

		try (FFmpegFrameGrabber videoGrabber = videoResource.openFrameGrabber();
		     FFmpegFrameGrabber bgmGrabber = bgmResource.openFrameGrabber();
		     FFmpegOutputStream ffmpegOutputStream = new FFmpegOutputStream(outputStream, outputVideo, videoGrabber);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(ffmpegOutputStream, 0)) {
			doAddBgm(videoGrabber, bgmGrabber, recorder, outputVideo, bgmWeight);
		}
	}

	/**
	 * 为视频添加文字水印并输出到文件（通过字体文件）
	 *
	 * @param resource      输入视频资源
	 * @param outputFile    输出文件
	 * @param watermarkText 水印文字
	 * @param fontFile      字体文件
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void addTextWatermark(final FFmpegResource resource, final File outputFile, final String watermarkText,
	                                    final File fontFile) throws IOException {
		addTextWatermark(resource, outputFile, null, watermarkText, new TextWatermarkOption(fontFile));
	}

	/**
	 * 为视频添加文字水印并输出到输出流（通过字体文件）
	 *
	 * @param resource      输入视频资源
	 * @param outputStream  输出流
	 * @param watermarkText 水印文字
	 * @param fontFile      字体文件
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 或 outputStream 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void addTextWatermark(final FFmpegResource resource, final OutputStream outputStream, final String watermarkText,
	                                    final File fontFile) throws IOException {
		addTextWatermark(resource, outputStream, null, watermarkText, new TextWatermarkOption(fontFile));
	}

	/**
	 * 为视频添加文字水印并输出到文件（通过字体名称）
	 *
	 * @param resource      输入视频资源
	 * @param outputFile    输出文件
	 * @param watermarkText 水印文字
	 * @param fontName      字体名称
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void addTextWatermark(final FFmpegResource resource, final File outputFile, final String watermarkText,
	                                    final String fontName) throws IOException {
		addTextWatermark(resource, outputFile, null, watermarkText, new TextWatermarkOption(fontName));
	}

	/**
	 * 为视频添加文字水印并输出到输出流（通过字体名称）
	 *
	 * @param resource      输入视频资源
	 * @param outputStream  输出流
	 * @param watermarkText 水印文字
	 * @param fontName      字体名称
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 或 outputStream 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void addTextWatermark(final FFmpegResource resource, final OutputStream outputStream, final String watermarkText,
	                                    final String fontName) throws IOException {
		addTextWatermark(resource, outputStream, null, watermarkText, new TextWatermarkOption(fontName));
	}

	/**
	 * 为视频添加文字水印并输出到文件（指定水印配置）
	 *
	 * @param resource      输入视频资源
	 * @param outputFile    输出文件
	 * @param watermarkText 水印文字
	 * @param option        水印配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void addTextWatermark(final FFmpegResource resource, final File outputFile, final String watermarkText,
	                                    final TextWatermarkOption option) throws IOException {
		addTextWatermark(resource, outputFile, null, watermarkText, option);
	}

	/**
	 * 为视频添加文字水印并输出到输出流（指定水印配置）
	 *
	 * @param resource      输入视频资源
	 * @param outputStream  输出流
	 * @param watermarkText 水印文字
	 * @param option        水印配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 或 outputStream 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void addTextWatermark(final FFmpegResource resource, final OutputStream outputStream, final String watermarkText,
	                                    final TextWatermarkOption option) throws IOException {
		addTextWatermark(resource, outputStream, null, watermarkText, option);
	}

	/**
	 * 为视频添加文字水印并输出到文件（指定输出配置，通过字体文件）
	 *
	 * @param resource      输入视频资源
	 * @param outputFile    输出文件
	 * @param outputVideo   输出视频配置
	 * @param watermarkText 水印文字
	 * @param fontFile      字体文件
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void addTextWatermark(final FFmpegResource resource, final File outputFile, final Video outputVideo,
	                                    final String watermarkText, final File fontFile) throws IOException {
		addTextWatermark(resource, outputFile, outputVideo, watermarkText, new TextWatermarkOption(fontFile));
	}

	/**
	 * 为视频添加文字水印并输出到输出流（指定输出配置，通过字体文件）
	 *
	 * @param resource      输入视频资源
	 * @param outputStream  输出流
	 * @param outputVideo   输出视频配置
	 * @param watermarkText 水印文字
	 * @param fontFile      字体文件
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 或 outputStream 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void addTextWatermark(final FFmpegResource resource, final OutputStream outputStream,
	                                    final Video outputVideo, final String watermarkText, final File fontFile) throws IOException {
		addTextWatermark(resource, outputStream, outputVideo, watermarkText, new TextWatermarkOption(fontFile));
	}

	/**
	 * 为视频添加文字水印并输出到文件（指定输出配置，通过字体名称）
	 *
	 * @param resource      输入视频资源
	 * @param outputFile    输出文件
	 * @param outputVideo   输出视频配置
	 * @param watermarkText 水印文字
	 * @param fontName      字体名称
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void addTextWatermark(final FFmpegResource resource, final File outputFile, final Video outputVideo,
	                                    final String watermarkText, final String fontName) throws IOException {
		addTextWatermark(resource, outputFile, outputVideo, watermarkText, new TextWatermarkOption(fontName));
	}

	/**
	 * 为视频添加文字水印并输出到输出流（指定输出配置，通过字体名称）
	 *
	 * @param resource      输入视频资源
	 * @param outputStream  输出流
	 * @param outputVideo   输出视频配置
	 * @param watermarkText 水印文字
	 * @param fontName      字体名称
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 或 outputStream 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void addTextWatermark(final FFmpegResource resource, final OutputStream outputStream,
	                                    final Video outputVideo, final String watermarkText, final String fontName) throws IOException {
		addTextWatermark(resource, outputStream, outputVideo, watermarkText, new TextWatermarkOption(fontName));
	}

	/**
	 * 为视频添加文字水印并输出到文件（指定输出配置和水印配置）
	 *
	 * @param resource      输入视频资源
	 * @param outputFile    输出文件
	 * @param outputVideo   输出视频配置
	 * @param watermarkText 水印文字
	 * @param option        水印配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void addTextWatermark(final FFmpegResource resource, final File outputFile, final Video outputVideo,
	                                    final String watermarkText, final TextWatermarkOption option) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");
		Validate.notNull(option, "option 不可为 null");
		Validate.notBlank(watermarkText, "watermarkText 不能为空");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doAddTextWatermark(grabber, recorder, outputVideo, watermarkText, option);
		}
	}

	/**
	 * 为视频添加文字水印并输出到输出流（指定输出配置和水印配置）
	 *
	 * @param resource      输入视频资源
	 * @param outputStream  输出流
	 * @param outputVideo   输出视频配置
	 * @param watermarkText 水印文字
	 * @param option        水印配置
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 outputStream 为 null 时
	 * @since 1.1.0
	 */
	public static void addTextWatermark(final FFmpegResource resource, final OutputStream outputStream,
	                                    final Video outputVideo, final String watermarkText,
	                                    final TextWatermarkOption option) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");
		Validate.notNull(option, "option 不可为 null");
		Validate.notBlank(watermarkText, "watermarkText 不能为空");

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegOutputStream ffmpegOutputStream = new FFmpegOutputStream(outputStream, outputVideo, grabber);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(ffmpegOutputStream, 0)) {
			doAddTextWatermark(grabber, recorder, outputVideo, watermarkText, option);
		}
	}

	/**
	 * 为视频添加图片水印并输出到文件（使用默认配置）
	 *
	 * @param resource       输入视频资源
	 * @param outputFile     输出文件
	 * @param watermarkImage 水印图片文件
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void addImageWatermark(final FFmpegResource resource, final File outputFile, final IOResource watermarkImage) throws IOException {
		addImageWatermark(resource, outputFile, null, watermarkImage, new ImageWatermarkOption());
	}

	/**
	 * 为视频添加图片水印并输出到输出流（使用默认配置）
	 *
	 * @param resource       输入视频资源
	 * @param outputStream   输出流
	 * @param watermarkImage 水印图片文件
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 或 outputStream 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void addImageWatermark(final FFmpegResource resource, final OutputStream outputStream,
	                                     final IOResource watermarkImage) throws IOException {
		addImageWatermark(resource, outputStream, null, watermarkImage, new ImageWatermarkOption());
	}

	/**
	 * 为视频添加图片水印并输出到文件（指定水印配置）
	 *
	 * @param resource       输入视频资源
	 * @param outputFile     输出文件
	 * @param watermarkImage 水印图片文件
	 * @param option         水印配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void addImageWatermark(final FFmpegResource resource, final File outputFile, final IOResource watermarkImage,
	                                     final ImageWatermarkOption option) throws IOException {
		addImageWatermark(resource, outputFile, null, watermarkImage, option);
	}

	/**
	 * 为视频添加图片水印并输出到输出流（指定水印配置）
	 *
	 * @param resource       输入视频资源
	 * @param outputStream   输出流
	 * @param watermarkImage 水印图片文件
	 * @param option         水印配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 或 outputStream 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void addImageWatermark(final FFmpegResource resource, final OutputStream outputStream,
	                                     final IOResource watermarkImage, final ImageWatermarkOption option) throws IOException {
		addImageWatermark(resource, outputStream, null, watermarkImage, option);
	}

	/**
	 * 为视频添加图片水印并输出到文件（指定输出配置）
	 *
	 * @param resource       输入视频资源
	 * @param outputFile     输出文件
	 * @param outputVideo    输出视频配置
	 * @param watermarkImage 水印图片文件
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void addImageWatermark(final FFmpegResource resource, final File outputFile, final Video outputVideo,
	                                     final IOResource watermarkImage) throws IOException {
		addImageWatermark(resource, outputFile, outputVideo, watermarkImage, new ImageWatermarkOption());
	}

	/**
	 * 为视频添加图片水印并输出到输出流（指定输出配置）
	 *
	 * @param resource       输入视频资源
	 * @param outputStream   输出流
	 * @param outputVideo    输出视频配置
	 * @param watermarkImage 水印图片文件
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resource 或 outputStream 为 null 时
	 * @throws IllegalArgumentException 当 resource 不是视频类型时
	 * @since 1.1.0
	 */
	public static void addImageWatermark(final FFmpegResource resource, final OutputStream outputStream,
	                                     final Video outputVideo, final IOResource watermarkImage) throws IOException {
		addImageWatermark(resource, outputStream, outputVideo, watermarkImage, new ImageWatermarkOption());
	}

	/**
	 * 为视频添加图片水印并输出到文件（指定输出配置和水印配置）
	 *
	 * @param resource       输入视频资源
	 * @param outputFile     输出文件
	 * @param outputVideo    输出视频配置
	 * @param watermarkImage 水印图片文件
	 * @param option         水印配置
	 * @throws IOException 当 I/O 错误发生时
	 * @since 1.1.0
	 */
	public static void addImageWatermark(final FFmpegResource resource, final File outputFile, final Video outputVideo,
	                                     final IOResource watermarkImage, final ImageWatermarkOption option) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");
		Validate.notNull(option, "option 不可为 null");
		Validate.notNull(watermarkImage, "watermarkImage 不可为 null");
		Validate.isTrue(watermarkImage.isImage(), "watermarkImage 不是图片资源");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doAddImageWatermark(grabber, recorder, outputVideo, watermarkImage, option);
		}
	}

	/**
	 * 为视频添加图片水印并输出到输出流（指定输出配置和水印配置）
	 *
	 * @param resource       输入视频资源
	 * @param outputStream   输出流
	 * @param outputVideo    输出视频配置
	 * @param watermarkImage 水印图片文件
	 * @param option         水印配置
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 outputStream 为 null 时
	 * @since 1.1.0
	 */
	public static void addImageWatermark(final FFmpegResource resource, final OutputStream outputStream,
	                                     final Video outputVideo, final IOResource watermarkImage,
	                                     final ImageWatermarkOption option) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 FFmpegResource");
		Validate.notNull(option, "option 不可为 null");
		Validate.notNull(watermarkImage, "watermarkImage 不可为 null");
		Validate.isTrue(watermarkImage.isImage(), "watermarkImage 不是图片资源");

		try (FFmpegFrameGrabber grabber = resource.openFrameGrabber();
		     FFmpegOutputStream ffmpegOutputStream = new FFmpegOutputStream(outputStream, outputVideo, grabber);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(ffmpegOutputStream, 0)) {
			doAddImageWatermark(grabber, recorder, outputVideo, watermarkImage, option);
		}
	}

	/**
	 * 内部方法：执行调整视频播放速度
	 *
	 * @param grabber     视频抓取器
	 * @param recorder    帧录制器
	 * @param speed       播放速度（0.5-100）
	 * @param outputVideo 输出视频配置
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 grabber 或 recorder 为 null 时
	 * @since 1.1.0
	 */
	protected static void doAdjustSpeed(final FFmpegFrameGrabber grabber, final FFmpegFrameRecorder recorder, final float speed,
	                                    final Video outputVideo) throws IOException {
		Validate.notNull(grabber, "grabber 不可为 null");
		Validate.notNull(recorder, "recorder 不可为 null");

		if (FFmpegUtils.isNotStarted(grabber)) {
			grabber.start();
		}

		String videoFilters = FFmpegFiltersBuilder.video()
			.addGlobalFilter(FFmpegUtils.getSetptsFilter(speed))
			.addGlobalFilter(FFmpegUtils.getFpsFilter(grabber.getFrameRate()))
			.build();

		FFmpegUtils.applyFilter(grabber, recorder, outputVideo, videoFilters,
			grabber.hasAudio() ? FFmpegUtils.getAtempoFilter(speed) : null, FrameType.ALL,
			false);
	}

	/**
	 * 内部方法：执行通过边距偏移裁剪视频画面
	 *
	 * @param grabber              视频抓取器
	 * @param recorder             帧录制器
	 * @param topOffset            顶部偏移
	 * @param bottomOffset         底部偏移
	 * @param leftOffset           左侧偏移
	 * @param rightOffset          右侧偏移
	 * @param outputVideo          输出视频配置
	 * @param outputCropResolution 是否输出裁剪后的分辨率
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 grabber 或 recorder 为 null 时
	 * @throws IllegalArgumentException 当 offset 参数小于0时
	 * @since 1.1.0
	 */
	protected static void doCropByOffset(final FFmpegFrameGrabber grabber, final FFmpegFrameRecorder recorder,
	                                     final int topOffset, final int bottomOffset, final int leftOffset,
	                                     final int rightOffset, final Video outputVideo,
	                                     final boolean outputCropResolution) throws IOException {
		Validate.notNull(grabber, "grabber 不可为 null");
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.isTrue(topOffset >= 0 && bottomOffset >= 0 && leftOffset >= 0 && rightOffset >= 0,
			"offset 不能小于0");

		if (FFmpegUtils.isNotStarted(grabber)) {
			grabber.start();
		}

		int videoWidth = grabber.getImageWidth();
		int videoHeight = grabber.getImageHeight();
		// 边界检测
		if (leftOffset + rightOffset > videoWidth || topOffset + bottomOffset > videoHeight) {
			throw new IllegalArgumentException(String.format("偏移裁剪 坐标越界，原视频：%dx%d，裁剪区域：顶部偏移标 %d，" +
					"底部偏移 %d 左侧偏移：%d 右侧偏移：%d",
				videoWidth, videoHeight, topOffset, bottomOffset, leftOffset, rightOffset));
		}

		Video cropOutputVideo = outputVideo;
		if (outputCropResolution) {
			cropOutputVideo = Video.builder(grabber)
				.resolution(videoWidth - leftOffset - rightOffset, videoHeight - topOffset - bottomOffset)
				.build();
		}

		FFmpegUtils.applyFilter(grabber, recorder, outputVideo, cropOutputVideo,
			String.format("crop=%d:%d:%s:%s", leftOffset, topOffset, "iw-" + (leftOffset + rightOffset),
				"ih-" + (topOffset + bottomOffset)), null, FrameType.ALL, false);
	}

	/**
	 * 内部方法：执行通过矩形区域裁剪视频画面
	 *
	 * @param grabber              视频抓取器
	 * @param recorder             帧录制器
	 * @param x                    裁剪区域左上角 x 坐标
	 * @param y                    裁剪区域左上角 y 坐标
	 * @param width                裁剪宽度
	 * @param height               裁剪高度
	 * @param outputVideo          输出视频配置
	 * @param outputCropResolution 是否输出裁剪后的分辨率
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 grabber 或 recorder 为 null 时
	 * @throws IllegalArgumentException 当坐标参数小于0或越界时
	 * @since 1.1.0
	 */
	protected static void doCropByRect(final FFmpegFrameGrabber grabber, final FFmpegFrameRecorder recorder,
	                                   final int x, final int y, final int width, final int height,
	                                   final Video outputVideo, final boolean outputCropResolution) throws IOException {
		Validate.notNull(grabber, "resource 不可为 null");
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.isTrue(x >= 0, "x 不能小于0");
		Validate.isTrue(y >= 0, "y 不能小于0");
		Validate.isTrue(width > 0, "width 不能小于0");
		Validate.isTrue(height > 0, "height 不能小于0");

		if (FFmpegUtils.isNotStarted(grabber)) {
			grabber.start();
		}

		int videoHeight = grabber.getImageHeight();
		int videoWidth = grabber.getImageWidth();
		// 边界检测
		if (x + width > videoWidth || y + height > videoHeight) {
			throw new IllegalArgumentException(String.format("区域裁剪 坐标越界，原视频：%dx%d，裁剪区域：x坐标 %d，" +
					"y坐标 %d 宽高：%d 高度：%d",
				videoWidth, videoHeight, x, y, width, height));
		}

		Video cropOutputVideo = outputVideo;
		if (outputCropResolution) {
			cropOutputVideo = Video.builder(grabber).resolution(width, height).build();
		}

		FFmpegUtils.applyFilter(grabber, recorder, outputVideo, cropOutputVideo,
			FFmpegUtils.getCropFilter(x, y, width, height), null, FrameType.ALL,
			false);
	}

	/**
	 * 内部方法：执行通过中心裁剪视频画面
	 *
	 * @param grabber              视频抓取器
	 * @param recorder             帧录制器
	 * @param width                裁剪宽度
	 * @param height               裁剪高度
	 * @param outputVideo          输出视频配置
	 * @param outputCropResolution 是否输出裁剪后的分辨率
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 grabber 或 recorder 为 null 时
	 * @throws IllegalArgumentException 当宽高参数小于0或越界时
	 * @since 1.1.0
	 */
	protected static void doCropByCenter(final FFmpegFrameGrabber grabber, final FFmpegFrameRecorder recorder,
	                                     final int width, final int height, final Video outputVideo,
	                                     final boolean outputCropResolution) throws IOException {
		Validate.notNull(grabber, "grabber 不可为 null");
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.isTrue(width > 0, "width 不能小于0");
		Validate.isTrue(height > 0, "height 不能小于0");

		if (FFmpegUtils.isNotStarted(grabber)) {
			grabber.start();
		}

		int videoHeight = grabber.getImageHeight();
		int videoWidth = grabber.getImageWidth();
		// 边界检测
		if (width > videoWidth || height > videoHeight) {
			throw new IllegalArgumentException(String.format("中心裁剪 坐标越界，原视频：%dx%d，裁剪宽度：%d，裁剪高度：%d",
				videoWidth, videoHeight, width, height));
		}

		Video cropOutputVideo = outputVideo;
		if (outputCropResolution) {
			cropOutputVideo = Video.builder(grabber).resolution(width, height).build();
		}

		FFmpegUtils.applyFilter(grabber, recorder, outputVideo, cropOutputVideo,
			FFmpegUtils.getCropFilter((videoWidth - width) / 2,
				(videoHeight - height) / 2, width, height), null, FrameType.ALL,
			false);
	}

	/**
	 * 内部方法：执行为视频添加背景音乐
	 *
	 * @param videoGrabber 视频抓取器
	 * @param bgmGrabber   背景音乐抓取器
	 * @param recorder     帧录制器
	 * @param outputVideo  输出视频配置
	 * @param bgmWeight    背景音乐权重
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 videoGrabber、bgmGrabber 或 recorder 为 null 时
	 * @throws IllegalArgumentException 当 bgmWeight 不大于0时
	 * @since 1.1.0
	 */
	protected static void doAddBgm(final FFmpegFrameGrabber videoGrabber, final FFmpegFrameGrabber bgmGrabber,
	                               final FFmpegFrameRecorder recorder, final Video outputVideo, final float bgmWeight) throws IOException {
		Validate.notNull(videoGrabber, "videoGrabber 不可为 null");
		Validate.notNull(bgmGrabber, "bgmGrabber 不可为 null");
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.isTrue(bgmWeight > 0, "bgmWeight 必须大于0");

		if (FFmpegUtils.isNotStarted(videoGrabber)) {
			videoGrabber.start();
		}
		if (FFmpegUtils.isNotStarted(bgmGrabber)) {
			bgmGrabber.start();
		}

		if (!videoGrabber.hasAudio()) {
			doReplaceAudio(videoGrabber, bgmGrabber, recorder, outputVideo, true);
		} else {
			Video video = Video.parse(videoGrabber);

			FFmpegUtils.applyFilter(List.of(videoGrabber, bgmGrabber), recorder, video,
				ObjectUtils.defaultIfNull(outputVideo, video), null,
				FFmpegUtils.getAddBgmFilter(videoGrabber, bgmGrabber, bgmWeight),
				FrameType.ALL, false);
		}
	}

	/**
	 * 内部方法：执行为视频添加文字水印
	 *
	 * @param grabber       视频抓取器
	 * @param recorder      帧录制器
	 * @param outputVideo   输出视频配置
	 * @param watermarkText 水印文字
	 * @param option        水印配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 grabber、recorder、option 或 watermarkText 为 null 时
	 * @throws IllegalArgumentException 当 watermarkText 为空时
	 * @since 1.1.0
	 */
	protected static void doAddTextWatermark(final FFmpegFrameGrabber grabber, final FFmpegFrameRecorder recorder,
	                                         final Video outputVideo, final String watermarkText,
	                                         final TextWatermarkOption option) throws IOException {
		Validate.notNull(grabber, "grabber 不可为 null");
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.notNull(option, "option 不可为 null");
		Validate.notBlank(watermarkText, "watermarkText 不能为空");

		if (FFmpegUtils.isNotStarted(grabber)) {
			grabber.start();
		}

		String videoFilters = option.toFFmpegFilter(watermarkText, grabber);
		FFmpegUtils.applyVideoFilter(grabber, recorder, outputVideo, videoFilters,
			FrameType.ALL, false);
	}

	/**
	 * 内部方法：执行为视频添加图片水印
	 *
	 * @param grabber        视频抓取器
	 * @param recorder       帧录制器
	 * @param outputVideo    输出视频配置
	 * @param watermarkImage 水印图片文件
	 * @param option         水印配置
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 grabber、recorder、option 或 watermarkImage 为 null 时
	 * @throws IllegalArgumentException 当 watermarkImage 不是图片资源时
	 * @since 1.1.0
	 */
	protected static void doAddImageWatermark(final FFmpegFrameGrabber grabber, final FFmpegFrameRecorder recorder,
	                                          final Video outputVideo, final IOResource watermarkImage,
	                                          final ImageWatermarkOption option) throws IOException {
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.notNull(grabber, "grabber 不可为 null");
		Validate.notNull(option, "option 不可为 null");
		Validate.notNull(watermarkImage, "watermarkImage 不可为 null");
		Validate.isTrue(watermarkImage.isImage(), "watermarkImage 不是图片资源");

		if (FFmpegUtils.isNotStarted(grabber)) {
			grabber.start();
		}

		String videoFilters = option.toFFmpegFilter(watermarkImage, grabber);
		FFmpegUtils.applyVideoFilter(grabber, recorder, outputVideo, videoFilters,
			FrameType.ALL, false);
	}

	/**
	 * 内部方法：执行替换视频的音频轨
	 *
	 * @param videoGrabber  视频抓取器
	 * @param audioGrabber  音频抓取器
	 * @param recorder      帧录制器
	 * @param outputVideo   输出视频配置
	 * @param loopFillAudio 是否循环填充音频以匹配视频时长
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 videoGrabber、audioGrabber 或 recorder 为 null 时
	 * @since 1.1.0
	 */
	protected static void doReplaceAudio(final FFmpegFrameGrabber videoGrabber, final FFmpegFrameGrabber audioGrabber,
	                                     final FFmpegFrameRecorder recorder, final Video outputVideo,
	                                     final boolean loopFillAudio) throws IOException {
		Validate.notNull(videoGrabber, "videoGrabber 不可为 null");
		Validate.notNull(audioGrabber, "audioGrabber 不可为 null");
		Validate.notNull(recorder, "recorder 不可为 null");

		if (FFmpegUtils.isNotStarted(videoGrabber)) {
			videoGrabber.start();
		}
		if (FFmpegUtils.isNotStarted(audioGrabber)) {
			audioGrabber.start();
		}

		FFmpegUtils.initRecorder(recorder, videoGrabber, audioGrabber, outputVideo, FrameType.ALL);
		recorder.start();

		long videoLengthInTime = videoGrabber.getLengthInTime();
		long audioLengthInTime = audioGrabber.getLengthInTime();

		if (!loopFillAudio || videoLengthInTime <= audioLengthInTime) {
			FFmpegUtils.recordFrames(recorder, videoGrabber, audioGrabber);
		} else {
			FFmpegUtils.recordFrames(recorder, videoGrabber, FrameType.VIDEO, false);

			recorder.setTimestamp(0);
			String audioFilters = FFmpegFiltersBuilder.audio()
				.addGlobalFilter(FFmpegUtils.getAloopFilter(audioGrabber.getSampleRate(), audioLengthInTime))
				.addGlobalFilter(FFmpegUtils.getAtrimFilter(videoLengthInTime))
				.build();
			FFmpegUtils.applyAudioFilter(audioGrabber, recorder, outputVideo, audioFilters,
				FrameType.AUDIO, true);
		}
	}

	protected static Set<String> getSupportedWriteImageFormats() {
		if (Objects.isNull(SUPPORTED_WRITE_IMAGE_FORMATS)) {
			synchronized (VideoUtils.class) {
				if (Objects.isNull(SUPPORTED_WRITE_IMAGE_FORMATS)) {
					SUPPORTED_WRITE_IMAGE_FORMATS = Set.of(ImageIO.getWriterFormatNames());
				}
			}
		}
		return SUPPORTED_WRITE_IMAGE_FORMATS;
	}
}
