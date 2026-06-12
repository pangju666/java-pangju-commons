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
import org.apache.commons.lang3.StringUtils;
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
 * 音频处理工具类（基于 JavaCV + FFmpeg 实现）
 * <p>
 * 封装常用音频处理能力，统一资源管理、参数校验与异常处理，支持文件/输出流两种输出形式；
 * 底层复用 FFmpeg 原生滤镜与帧处理逻辑，兼顾兼容性与执行效率。
 * </p>
 * <h3>核心功能列表</h3>
 * <ul>
 *     <li>音频转码：支持自定义输出格式、音频采样/声道/码率参数</li>
 *     <li>音频裁剪：支持按时长、起止时间截取片段，兼容多种输出配置</li>
 *     <li>音频拼接：多段音频按顺序合并为单个音频文件/流</li>
 *     <li>音频混音：主音频叠加背景音乐，自动对齐音频参数，支持音量调节</li>
 *     <li>音频变速：基于 atempo 滤镜实现倍速播放（变速同步改变音调），支持大范围倍率</li>
 *     <li>通用滤镜：统一封装 FFmpeg 音频滤镜调用入口，可扩展各类音频特效</li>
 * </ul>
 * <h3>通用说明</h3>
 * <ul>
 *     <li>所有入参均做非空、类型合法性校验，非法入参将抛出 {@link IllegalArgumentException}</li>
 *     <li>统一使用 try-with-resources 自动关闭流、抓取器、录制器，避免资源泄漏</li>
 *     <li>音频变速底层自动多级拼接 atempo 滤镜，突破单级 0.5 ~ 2.0 速度限制</li>
 *     <li>混音功能自动将背景音乐标准化为 WAV 格式并对齐主音频参数，保证混音效果</li>
 * </ul>
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 示例1：音频转码为 MP3 格式
 * MediaResource resource = new MediaResource(new File("input.wav"));
 * AudioUtils.transcode(resource, new File("output.mp3"), "mp3");
 *
 * // 示例2：裁剪音频前10秒
 * AudioUtils.cut(resource, new File("output.mp3"), Duration.ofSeconds(10));
 *
 * // 示例3：音频混音（背景音乐音量0.4）
 * MediaResource bgm = new MediaResource(new File("bgm.mp3"));
 * AudioUtils.remix(resource, bgm, new File("remix.mp3"), 0.4f);
 *
 * // 示例4：音频变速（2倍速）
 * AudioUtils.adjustSpeed(resource, new File("output_speed.mp3"), 2.0f);
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
	public static final String BGM_FORMAT = "wav";
	/**
	 * 音频变速 - 单级 atempo 最小速度（FFmpeg 原生限制）
	 *
	 * @since 1.1.0
	 */
	public static final float MIN_SPEED = 0.5f;
	/**
	 * 音频变速 - 单级 atempo 最大速度（FFmpeg 原生限制）
	 *
	 * @since 1.1.0
	 */
	public static final float MAX_SPEED = 2.0f;
	/**
	 * 16位音频采样值缩放比例（Short.MAX_VALUE = 32768）
	 *
	 * @since 1.1.0
	 */
	protected static final float S16_SAMPLE_SCALE = 32768.0f;

	/**
	 * 音频转码（指定输出文件和格式）
	 * <p>
	 * 将源音频转码为指定格式，保留原音频的采样率、声道数和码率。
	 * </p>
	 *
	 * @param resource     音频资源（需为 audio/* 类型 MediaResource，不可为 null）
	 * @param outputFile   输出文件（不可为 null，将自动覆盖已有文件）
	 * @param outputFormat 输出格式（如 mp3、wav、flac、aac，不可为空）
	 * @throws IOException              IO异常/音频处理异常（文件不存在、权限不足、解析失败等）
	 * @throws IllegalArgumentException 入参校验失败时抛出（参数为 null、格式为空、非音频类型）
	 * @since 1.1.0
	 */
	public static void transcode(MediaResource resource, File outputFile, String outputFormat) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (OutputStream outputStream = FileUtils.openOutputStream(outputFile)) {
			transcode(resource, outputStream, null, outputFormat);
		}
	}

	/**
	 * 音频转码（指定输出流和格式）
	 * <p>
	 * 将源音频转码为指定格式，保留原音频的采样率、声道数和码率，直接写入输出流。
	 * 输出流不会被自动关闭，需要调用方管理。
	 * </p>
	 *
	 * @param resource     音频资源（需为 audio/* 类型 MediaResource，不可为 null）
	 * @param outputStream 输出流（不可为 null，不会被自动关闭）
	 * @param outputFormat 输出格式（如 mp3、wav、flac、aac，不可为空）
	 * @throws IOException              IO异常/音频处理异常（文件解析、写入失败等）
	 * @throws IllegalArgumentException 入参校验失败时抛出（参数为 null、格式为空、非音频类型）
	 * @since 1.1.0
	 */
	public static void transcode(MediaResource resource, OutputStream outputStream, String outputFormat) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");

		transcode(resource, outputStream, null, outputFormat);
	}

	/**
	 * 音频转码（指定输出文件和音频参数）
	 * <p>
	 * 将源音频转码为指定格式和参数，可自定义采样率、声道数和码率。
	 * 参数值小于等于0时将沿用原音频配置。
	 * </p>
	 *
	 * @param resource    音频资源（需为 audio/* 类型 MediaResource，不可为 null）
	 * @param outputFile  输出文件（不可为 null，将自动覆盖已有文件）
	 * @param outputAudio 输出音频参数（采样率、声道数、比特率等，不可为 null）
	 * @throws IOException              IO异常/音频处理异常（文件不存在、权限不足、解析失败等）
	 * @throws IllegalArgumentException 入参校验失败时抛出（参数为 null、非音频类型）
	 * @since 1.1.0
	 */
	public static void transcode(MediaResource resource, File outputFile, Audio outputAudio) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (OutputStream outputStream = FileUtils.openOutputStream(outputFile)) {
			transcode(resource, outputStream, outputAudio, null);
		}
	}

	/**
	 * 音频转码（指定输出流和音频参数）
	 * <p>
	 * 将源音频转码为指定格式和参数，可自定义采样率、声道数和码率，直接写入输出流。
	 * 参数值小于等于0时将沿用原音频配置，输出流不会被自动关闭，需要调用方管理。
	 * </p>
	 *
	 * @param resource     音频资源（需为 audio/* 类型 MediaResource，不可为 null）
	 * @param outputStream 输出流（不可为 null，不会被自动关闭）
	 * @param outputAudio  输出音频参数（采样率、声道数、比特率等，不可为 null）
	 * @throws IOException              IO异常/音频处理异常（文件解析、写入失败等）
	 * @throws IllegalArgumentException 入参校验失败时抛出（参数为 null、非音频类型）
	 * @since 1.1.0
	 */
	public static void transcode(MediaResource resource, OutputStream outputStream, Audio outputAudio) throws IOException {
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");

		transcode(resource, outputStream, outputAudio, null);
	}

	/**
	 * 音频裁剪（从起始位置[0]裁剪指定时长）
	 * <p>
	 * 从音频开头截取指定时长的片段，保留原音频格式。
	 * </p>
	 *
	 * @param resource   音频资源（需为 audio/* 类型 MediaResource，不可为 null）
	 * @param outputFile 输出文件（不可为 null，将自动覆盖已有文件）
	 * @param duration   裁剪时长（不可为 null，需大于0，超过音频总时长则截取到音频末尾）
	 * @throws IOException              IO异常/音频处理异常（文件不存在、权限不足、解析失败等）
	 * @throws IllegalArgumentException 入参校验失败时抛出（参数为 null、时长无效、非音频类型）
	 * @since 1.1.0
	 */
	public static void cut(MediaResource resource, File outputFile, Duration duration) throws IOException {
		cut(resource, outputFile, Duration.ZERO, duration);
	}

	/**
	 * 音频裁剪（从起始位置[0]裁剪指定时长）
	 * <p>
	 * 从音频开头截取指定时长的片段，保留原音频格式，直接写入输出流。
	 * 输出流不会被自动关闭，需要调用方管理。
	 * </p>
	 *
	 * @param resource     音频资源（需为 audio/* 类型 MediaResource，不可为 null）
	 * @param outputStream 输出流（不可为 null，不会被自动关闭）
	 * @param duration     裁剪时长（不可为 null，需大于0，超过音频总时长则截取到音频末尾）
	 * @throws IOException              IO异常/音频处理异常（文件解析、写入失败等）
	 * @throws IllegalArgumentException 入参校验失败时抛出（参数为 null、时长无效、非音频类型）
	 * @since 1.1.0
	 */
	public static void cut(MediaResource resource, OutputStream outputStream, Duration duration) throws IOException {
		Validate.notNull(duration, "duration 不可为 null");

		cut(resource, outputStream, Duration.ZERO, duration);
	}

	/**
	 * 音频裁剪（指定起始和结束时间）
	 * <p>
	 * 截取音频指定时间段的片段，结束时间为 null 时截取到音频末尾。
	 * 保留原音频格式。
	 * </p>
	 *
	 * @param resource   音频资源（需为 audio/* 类型 MediaResource，不可为 null）
	 * @param outputFile 输出文件（不可为 null，将自动覆盖已有文件）
	 * @param start      起始时间（不可为 null，必须小于音频总时长）
	 * @param end        裁剪结束时间，可为 null（表示截取到末尾）；非空时必须大于起始时间
	 * @throws IOException              IO异常/音频处理异常（文件不存在、权限不足、解析失败等）
	 * @throws IllegalArgumentException 入参校验失败时抛出（参数为 null、时间无效、非音频类型）
	 * @since 1.1.0
	 */
	public static void cut(MediaResource resource, File outputFile, Duration start, Duration end) throws IOException {
		Validate.notNull(start, "start 不可为 null");
		if (Objects.nonNull(end)) {
			Validate.isTrue(end.compareTo(start) > 0, "end 必须大于 start");
		}
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (OutputStream outputStream = FileUtils.openOutputStream(outputFile)) {
			cut(resource, outputStream, null, null, start, end);
		}
	}

	/**
	 * 音频裁剪（指定起始和结束时间）
	 * <p>
	 * 截取音频指定时间段的片段，结束时间为 null 时截取到音频末尾，
	 * 直接写入输出流。输出流不会被自动关闭，需要调用方管理。
	 * </p>
	 *
	 * @param resource     音频资源（需为 audio/* 类型 MediaResource，不可为 null）
	 * @param outputStream 输出流（不可为 null，不会被自动关闭）
	 * @param start        起始时间（不可为 null，必须小于音频总时长）
	 * @param end          裁剪结束时间，可为 null（表示截取到末尾）；非空时必须大于起始时间
	 * @throws IOException              IO异常/音频处理异常（文件解析、写入失败等）
	 * @throws IllegalArgumentException 入参校验失败时抛出（参数为 null、时间无效、非音频类型）
	 * @since 1.1.0
	 */
	public static void cut(MediaResource resource, OutputStream outputStream, Duration start, Duration end) throws IOException {
		Validate.notNull(start, "start 不可为 null");
		if (Objects.nonNull(end)) {
			Validate.isTrue(end.compareTo(start) > 0, "end 必须大于 start");
		}
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");

		cut(resource, outputStream, null, null, start, end);
	}

	/**
	 * 音频裁剪（指定输出格式+从起始位置[0]裁剪指定时长）
	 * <p>
	 * 从音频开头截取指定时长的片段，同时转码为指定格式。
	 * </p>
	 *
	 * @param resource     音频资源（需为 audio/* 类型 MediaResource，不可为 null）
	 * @param outputFile   输出文件（不可为 null，将自动覆盖已有文件）
	 * @param outputFormat 输出格式（如 mp3、wav、flac，不可为空）
	 * @param duration     裁剪时长（不可为 null，需大于0，超过音频总时长则截取到音频末尾）
	 * @throws IOException              IO异常/音频处理异常（文件不存在、权限不足、解析失败等）
	 * @throws IllegalArgumentException 入参校验失败时抛出（参数为 null、格式为空、时长无效、非音频类型）
	 * @since 1.1.0
	 */
	public static void cut(MediaResource resource, File outputFile, String outputFormat, Duration duration) throws IOException {
		cut(resource, outputFile, outputFormat, Duration.ZERO, duration);
	}

	/**
	 * 音频裁剪（指定输出格式+从起始位置[0]裁剪指定时长）
	 * <p>
	 * 从音频开头截取指定时长的片段，同时转码为指定格式，直接写入输出流。
	 * 输出流不会被自动关闭，需要调用方管理。
	 * </p>
	 *
	 * @param resource     音频资源（需为 audio/* 类型 MediaResource，不可为 null）
	 * @param outputStream 输出流（不可为 null，不会被自动关闭）
	 * @param outputFormat 输出格式（如 mp3、wav、flac，不可为空）
	 * @param duration     裁剪时长（不可为 null，需大于0，超过音频总时长则截取到音频末尾）
	 * @throws IOException              IO异常/音频处理异常（文件解析、写入失败等）
	 * @throws IllegalArgumentException 入参校验失败时抛出（参数为 null、格式为空、时长无效、非音频类型）
	 * @since 1.1.0
	 */
	public static void cut(MediaResource resource, OutputStream outputStream, String outputFormat, Duration duration) throws IOException {
		Validate.notNull(duration, "duration 不可为 null");

		cut(resource, outputStream, outputFormat, Duration.ZERO, duration);
	}

	/**
	 * 音频裁剪（指定输出格式+起始/结束时间）
	 * <p>
	 * 截取音频指定时间段的片段，结束时间为 null 时截取到音频末尾，
	 * 同时转码为指定格式。
	 * </p>
	 *
	 * @param resource     音频资源（需为 audio/* 类型 MediaResource，不可为 null）
	 * @param outputFile   输出文件（不可为 null，将自动覆盖已有文件）
	 * @param outputFormat 输出格式（如 mp3、wav、flac，不可为空）
	 * @param start        起始时间（不可为 null，必须小于音频总时长）
	 * @param end          裁剪结束时间，可为 null（表示截取到末尾）；非空时必须大于起始时间
	 * @throws IOException              IO异常/音频处理异常（文件不存在、权限不足、解析失败等）
	 * @throws IllegalArgumentException 入参校验失败时抛出（参数为 null、格式为空、时间无效、非音频类型）
	 * @since 1.1.0
	 */
	public static void cut(MediaResource resource, File outputFile, String outputFormat, Duration start, Duration end) throws IOException {
		Validate.notNull(start, "start 不可为 null");
		if (Objects.nonNull(end)) {
			Validate.isTrue(end.compareTo(start) > 0, "end 必须大于 start");
		}
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (OutputStream outputStream = FileUtils.openOutputStream(outputFile)) {
			cut(resource, outputStream, null, outputFormat, start, end);
		}
	}

	/**
	 * 音频裁剪（指定输出格式+起始/结束时间）
	 * <p>
	 * 截取音频指定时间段的片段，结束时间为 null 时截取到音频末尾，
	 * 同时转码为指定格式，直接写入输出流。输出流不会被自动关闭，需要调用方管理。
	 * </p>
	 *
	 * @param resource     音频资源（需为 audio/* 类型 MediaResource，不可为 null）
	 * @param outputStream 输出流（不可为 null，不会被自动关闭）
	 * @param outputFormat 输出格式（如 mp3、wav、flac，不可为空）
	 * @param start        起始时间（不可为 null，必须小于音频总时长）
	 * @param end          裁剪结束时间，可为 null（表示截取到末尾）；非空时必须大于起始时间
	 * @throws IOException              IO异常/音频处理异常（文件解析、写入失败等）
	 * @throws IllegalArgumentException 入参校验失败时抛出（参数为 null、格式为空、时间无效、非音频类型）
	 * @since 1.1.0
	 */
	public static void cut(MediaResource resource, OutputStream outputStream, String outputFormat, Duration start, Duration end) throws IOException {
		Validate.notNull(start, "start 不可为 null");
		if (Objects.nonNull(end)) {
			Validate.isTrue(end.compareTo(start) > 0, "end 必须大于 start");
		}
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");

		cut(resource, outputStream, null, outputFormat, start, end);
	}

	/**
	 * 音频裁剪（指定输出音频参数+从起始位置[0]裁剪指定时长）
	 * <p>
	 * 从音频开头截取指定时长的片段，同时按指定音频参数进行转码。
	 * 参数值小于等于0时将沿用原音频配置。
	 * </p>
	 *
	 * @param resource    音频资源（需为 audio/* 类型 MediaResource，不可为 null）
	 * @param outputFile  输出文件（不可为 null，将自动覆盖已有文件）
	 * @param outputAudio 输出音频参数（采样率、声道数等，不可为 null）
	 * @param duration    裁剪时长（不可为 null，需大于0，超过音频总时长则截取到音频末尾）
	 * @throws IOException              IO异常/音频处理异常（文件不存在、权限不足、解析失败等）
	 * @throws IllegalArgumentException 入参校验失败时抛出（参数为 null、时长无效、非音频类型）
	 * @since 1.1.0
	 */
	public static void cut(MediaResource resource, File outputFile, Audio outputAudio, Duration duration) throws IOException {
		Validate.notNull(duration, "duration 不可为 null");

		cut(resource, outputFile, outputAudio, Duration.ZERO, duration);
	}

	/**
	 * 音频裁剪（指定输出音频参数+从起始位置[0]裁剪指定时长）
	 * <p>
	 * 从音频开头截取指定时长的片段，同时按指定音频参数进行转码，直接写入输出流。
	 * 参数值小于等于0时将沿用原音频配置，输出流不会被自动关闭，需要调用方管理。
	 * </p>
	 *
	 * @param resource     音频资源（需为 audio/* 类型 MediaResource，不可为 null）
	 * @param outputStream 输出流（不可为 null，不会被自动关闭）
	 * @param outputAudio  输出音频参数（采样率、声道数等，不可为 null）
	 * @param duration     裁剪时长（不可为 null，需大于0，超过音频总时长则截取到音频末尾）
	 * @throws IOException              IO异常/音频处理异常（文件解析、写入失败等）
	 * @throws IllegalArgumentException 入参校验失败时抛出（参数为 null、时长无效、非音频类型）
	 * @since 1.1.0
	 */
	public static void cut(MediaResource resource, OutputStream outputStream, Audio outputAudio, Duration duration) throws IOException {
		Validate.notNull(duration, "duration 不可为 null");

		cut(resource, outputStream, outputAudio, Duration.ZERO, duration);
	}

	/**
	 * 音频裁剪（指定输出音频参数+起始/结束时间）
	 * <p>
	 * 截取音频指定时间段的片段，结束时间为 null 时截取到音频末尾，
	 * 同时按指定音频参数进行转码。参数值小于等于0时将沿用原音频配置。
	 * </p>
	 *
	 * @param resource    音频资源（需为 audio/* 类型 MediaResource，不可为 null）
	 * @param outputFile  输出文件（不可为 null，将自动覆盖已有文件）
	 * @param outputAudio 输出音频参数（采样率、声道数等，不可为 null）
	 * @param start       起始时间（不可为 null，必须小于音频总时长）
	 * @param end         裁剪结束时间，可为 null（表示截取到末尾）；非空时必须大于起始时间
	 * @throws IOException              IO异常/音频处理异常（文件不存在、权限不足、解析失败等）
	 * @throws IllegalArgumentException 入参校验失败时抛出（参数为 null、时间无效、非音频类型）
	 * @since 1.1.0
	 */
	public static void cut(MediaResource resource, File outputFile, Audio outputAudio, Duration start, Duration end) throws IOException {
		Validate.notNull(start, "start 不可为 null");
		if (Objects.nonNull(end)) {
			Validate.isTrue(end.compareTo(start) > 0, "end 必须大于 start");
		}
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (OutputStream outputStream = FileUtils.openOutputStream(outputFile)) {
			cut(resource, outputStream, outputAudio, null, start, end);
		}
	}

	/**
	 * 音频裁剪（指定输出音频参数+起始/结束时间）
	 * <p>
	 * 截取音频指定时间段的片段，结束时间为 null 时截取到音频末尾，
	 * 同时按指定音频参数进行转码，直接写入输出流。
	 * 参数值小于等于0时将沿用原音频配置，输出流不会被自动关闭，需要调用方管理。
	 * </p>
	 *
	 * @param resource     音频资源（需为 audio/* 类型 MediaResource，不可为 null）
	 * @param outputStream 输出流（不可为 null，不会被自动关闭）
	 * @param outputAudio  输出音频参数（采样率、声道数等，不可为 null）
	 * @param start        起始时间（不可为 null，必须小于音频总时长）
	 * @param end          裁剪结束时间，可为 null（表示截取到末尾）；非空时必须大于起始时间
	 * @throws IOException              IO异常/音频处理异常（文件解析、写入失败等）
	 * @throws IllegalArgumentException 入参校验失败时抛出（参数为 null、时间无效、非音频类型）
	 * @since 1.1.0
	 */
	public static void cut(MediaResource resource, OutputStream outputStream, Audio outputAudio, Duration start, Duration end) throws IOException {
		Validate.notNull(start, "start 不可为 null");
		if (Objects.nonNull(end)) {
			Validate.isTrue(end.compareTo(start) > 0, "end 必须大于 start");
		}
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");

		cut(resource, outputStream, outputAudio, null, start, end);
	}

	/**
	 * 音频拼接（多音频合并，指定输出文件和格式）
	 * <p>
	 * 将多个音频资源按顺序合并为单个音频，使用第一个音频的采样率和声道数作为基准。
	 * 非音频类型的资源将被自动跳过。
	 * </p>
	 *
	 * @param resources    音频资源列表（不可为空，元素需为 audio/* 类型 MediaResource）
	 * @param outputFile   输出文件（不可为 null，将自动覆盖已有文件）
	 * @param outputFormat 输出格式（如 mp3、wav，不可为空）
	 * @throws IOException              IO异常/音频处理异常（文件不存在、权限不足、解析失败等）
	 * @throws IllegalArgumentException 入参校验失败时抛出（参数为 null、格式为空、资源列表为空）
	 * @since 1.1.0
	 */
	public static void concat(List<MediaResource> resources, File outputFile, String outputFormat) throws IOException {
		Validate.notEmpty(resources, "resources 不可为空");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (OutputStream outputStream = FileUtils.openOutputStream(outputFile)) {
			concat(resources, outputStream, Audio.builder().format(outputFormat).build());
		}
	}

	/**
	 * 音频拼接（多音频合并，指定输出流和格式）
	 * <p>
	 * 将多个音频资源按顺序合并为单个音频，使用第一个音频的采样率和声道数作为基准，
	 * 同时转码为指定格式，直接写入输出流。非音频类型的资源将被自动跳过。
	 * 输出流不会被自动关闭，需要调用方管理。
	 * </p>
	 *
	 * @param resources    音频资源列表（不可为空，元素需为 audio/* 类型 MediaResource）
	 * @param outputStream 输出流（不可为 null，不会被自动关闭）
	 * @param outputFormat 输出格式（如 mp3、wav，不可为空）
	 * @throws IOException              IO异常/音频处理异常（文件解析、写入失败等）
	 * @throws IllegalArgumentException 入参校验失败时抛出（参数为 null、格式为空、资源列表为空）
	 * @since 1.1.0
	 */
	public static void concat(List<MediaResource> resources, OutputStream outputStream, String outputFormat) throws IOException {
		Validate.notBlank(outputFormat, "outputFormat 不可为空");

		concat(resources, outputStream, Audio.builder().format(outputFormat).build());
	}

	/**
	 * 音频拼接（多音频合并，指定输出文件和音频参数）
	 * <p>
	 * 将多个音频资源按顺序合并为单个音频，按指定音频参数进行转码。
	 * 非音频类型的资源将被自动跳过。参数值小于等于0时将沿用原音频配置。
	 * </p>
	 *
	 * @param resources   音频资源列表（不可为空，元素需为 audio/* 类型 MediaResource）
	 * @param outputFile  输出文件（不可为 null，将自动覆盖已有文件）
	 * @param outputAudio 输出音频参数（采样率、声道数等，不可为 null）
	 * @throws IOException              IO异常/音频处理异常（文件不存在、权限不足、解析失败等）
	 * @throws IllegalArgumentException 入参校验失败时抛出（参数为 null、资源列表为空）
	 * @since 1.1.0
	 */
	public static void concat(List<MediaResource> resources, File outputFile, Audio outputAudio) throws IOException {
		Validate.notEmpty(resources, "resources 不可为空");
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (OutputStream outputStream = FileUtils.openOutputStream(outputFile)) {
			concat(resources, outputStream, outputAudio);
		}
	}

	/**
	 * 音频拼接（多音频合并，指定输出流和音频参数）
	 * <p>
	 * 将多个音频资源按顺序合并为单个音频，按指定音频参数进行转码，直接写入输出流。
	 * 非音频类型的资源将被自动跳过。参数值小于等于0时将沿用原音频配置。
	 * 输出流不会被自动关闭，需要调用方管理。
	 * </p>
	 *
	 * @param resources    音频资源列表（不可为空，元素需为 audio/* 类型 MediaResource）
	 * @param outputStream 输出流（不可为 null，不会被自动关闭）
	 * @param outputAudio  输出音频参数（采样率、声道数等，不可为 null）
	 * @throws IOException              IO异常/音频处理异常（文件解析、写入失败等）
	 * @throws IllegalArgumentException 入参校验失败时抛出（参数为 null、资源列表为空）
	 * @since 1.1.0
	 */
	public static void concat(List<MediaResource> resources, OutputStream outputStream, Audio outputAudio) throws IOException {
		Validate.notEmpty(resources, "resources 不可为空");
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0, 0)) {
			initRecorder(recorder, null, outputAudio, null);

			for (MediaResource resource : resources) {
				if (Objects.isNull(resource) || !resource.isAudio()) {
					continue;
				}

				try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(resource.getInputStream())) {
					grabber.start();

					recordSamples(recorder, grabber, null);
				}
			}
		}
	}

	/**
	 * 音频混音（主音频+背景音乐，默认音量0.4，输出到文件）
	 * <p>
	 * 自动标准化背景音乐格式为 WAV，匹配主音频的采样率/声道数
	 *
	 * @param mainResource 主音频资源（需为 audio/* 类型 MediaResource，不可为 null）
	 * @param bgmResource  背景音乐资源（需为 audio/* 类型 MediaResource，不可为 null）
	 * @param outputFile   输出文件（不可为 null）
	 * @throws IOException              IO异常/音频处理异常
	 * @throws IllegalArgumentException 入参校验失败时抛出
	 * @since 1.1.0
	 */
	public static void remix(MediaResource mainResource, MediaResource bgmResource, File outputFile) throws IOException {
		Validate.notNull(mainResource, "mainResource 不可为 null");
		Validate.notNull(bgmResource, "bgmResource 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (OutputStream outputStream = FileUtils.openOutputStream(outputFile)) {
			remix(mainResource, bgmResource, outputStream, 0.4f);
		}
	}

	/**
	 * 音频混音（主音频+背景音乐，指定音量，输出到文件）
	 * <p>
	 * 自动标准化背景音乐格式为 WAV，匹配主音频的采样率/声道数
	 *
	 * @param mainResource 主音频资源（需为 audio/* 类型 MediaResource，不可为 null）
	 * @param bgmResource  背景音乐资源（需为 audio/* 类型 MediaResource，不可为 null）
	 * @param outputFile   输出文件（不可为 null）
	 * @param bgmVolume    背景音乐线性音量系数：最小值 0，上限参考 {@link #MAX_BGM_VOLUME}；
	 *                     解说/旁白推荐 0.3~0.5，K歌演唱推荐 0.5~0.7，原始音量为 1.0
	 * @throws IOException              IO异常/音频处理异常
	 * @throws IllegalArgumentException 入参校验失败时抛出
	 * @since 1.1.0
	 */
	public static void remix(MediaResource mainResource, MediaResource bgmResource, File outputFile, float bgmVolume) throws IOException {
		Validate.notNull(mainResource, "mainResource 不可为 null");
		Validate.notNull(bgmResource, "bgmResource 不可为 null");
		Validate.isTrue(bgmVolume > 0, "bgmVolume 必须大于0");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (OutputStream outputStream = FileUtils.openOutputStream(outputFile)) {
			remix(mainResource, bgmResource, outputStream, bgmVolume);
		}
	}

	/**
	 * 音频混音（主音频+背景音乐，默认音量{@value MAX_BGM_VOLUME}，输出到流）
	 * <p>
	 * 自动标准化背景音乐格式为 WAV，匹配主音频的采样率/声道数，直接写入输出流。
	 * 输出流不会被自动关闭，需要调用方管理。
	 * </p>
	 *
	 * @param mainResource 主音频资源（需为 audio/* 类型 MediaResource，不可为 null）
	 * @param bgmResource  背景音乐资源（需为 audio/* 类型 MediaResource，不可为 null）
	 * @param outputStream 输出流（不可为 null，不会被自动关闭）
	 * @throws IOException              IO异常/音频处理异常（文件解析、写入失败等）
	 * @throws IllegalArgumentException 入参校验失败时抛出（参数为 null、非音频类型）
	 * @since 1.1.0
	 */
	public static void remix(MediaResource mainResource, MediaResource bgmResource, OutputStream outputStream) throws IOException {
		remix(mainResource, bgmResource, outputStream, MAX_BGM_VOLUME);
	}

	/**
	 * 音频混音（主音频+背景音乐，指定音量，输出到流）
	 * <p>
	 * 核心混音逻辑：
	 * <ol>
	 *   <li>标准化背景音乐格式/采样率/声道数为与主音频一致</li>
	 *   <li>缓存背景音乐PCM数据（若背景音乐时长 < 主音频则循环使用）</li>
	 *   <li>逐采样点混合主音频与背景音乐（浮点运算避免溢出）</li>
	 *   <li>限制混合后采样值在合法范围（Short.MIN~MAX 或 -1.0~1.0）</li>
	 * </ol>
	 * 输出流不会被自动关闭，需要调用方管理。
	 * </p>
	 *
	 * @param mainResource 主音频资源（需为 audio/* 类型 MediaResource，不可为 null）
	 * @param bgmResource  背景音乐资源（需为 audio/* 类型 MediaResource，不可为 null）
	 * @param outputStream 输出流（不可为 null，不会被自动关闭）
	 * @param bgmVolume    背景音乐线性音量系数：最小值 0，上限参考 {@link #MAX_BGM_VOLUME}；
	 *                     解说/旁白推荐 0.3~0.5，K歌演唱推荐 0.5~0.7，原始音量为 1.0
	 * @throws IOException              IO异常/音频处理异常（文件解析、写入失败等）
	 * @throws IllegalArgumentException 入参校验失败时抛出（参数为 null、音量无效、非音频类型）
	 * @since 1.1.0
	 */
	public static void remix(MediaResource mainResource, MediaResource bgmResource, OutputStream outputStream, float bgmVolume) throws IOException {
		Validate.notNull(mainResource, "mainResource 不可为 null");
		Validate.notNull(bgmResource, "bgmResource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.isTrue(bgmVolume > 0, "bgmVolume 必须大于0");

		File tmpFile = null;
		FFmpegFrameGrabber bgmGrabber;
		Frame[] liveBgmHolder = new Frame[1];

		try (FFmpegFrameGrabber mainGrabber = new FFmpegFrameGrabber(mainResource.getInputStream())) {
			mainGrabber.start();
			Audio mainAudio = Audio.builder().parse(mainGrabber).build();

			bgmGrabber = new FFmpegFrameGrabber(bgmResource.getInputStream());
			bgmGrabber.start();

			// 判断是否需要转临时标准化wav
			if (bgmGrabber.getAudioChannels() != mainAudio.getChannels() ||
				bgmGrabber.getSampleRate() != mainAudio.getSampleRate() ||
				!BGM_FORMAT.equals(bgmGrabber.getFormat())) {
				tmpFile = File.createTempFile(UUID.randomUUID().toString(), null);

				try (FFmpegFrameRecorder bgmRecorder = new FFmpegFrameRecorder(tmpFile, 0, 0)) {
					initRecorder(bgmRecorder, bgmGrabber, mainAudio, BGM_FORMAT);
					recordSamples(bgmRecorder, bgmGrabber, null);
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

			try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0, 0)) {
				initRecorder(recorder, mainGrabber, mainAudio, null);

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
									float bgmSample = getBgmSample(isCacheBgm, bgmPcmCache, bgmGrabber,
										liveBgmHolder, bgmPageIndex, bgmSampleOffset, i);
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
									float bgmSample = getBgmSample(isCacheBgm, bgmPcmCache, bgmGrabber,
										liveBgmHolder, bgmPageIndex, bgmSampleOffset, i);
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
	 * 音频变速（速度改变，音调同步改变）
	 * <p>
	 * 基于 FFmpeg atempo 滤镜实现；单级滤镜有效区间 0.5 ~ 2.0，超出区间会自动多级叠加滤镜。
	 * 速度小于 1.0 为慢放，等于 1.0 为原速，大于 1.0 为快放。
	 * </p>
	 *
	 * @param resource   源音频资源，不可为 null，必须为 audio 类型媒体
	 * @param outputFile 输出文件，不可为 null
	 * @param speed      变速倍率，合法范围：0.1 ~ 10.0
	 * @throws IOException              音频读取、滤镜处理、写入发生IO异常时抛出
	 * @throws IllegalArgumentException 入参为空、非音频类型、变速倍率非法时抛出
	 * @since 1.1.0
	 */
	public static void adjustSpeed(MediaResource resource, File outputFile, float speed) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(speed > 0, "变速倍率必须大于 0");
		Validate.isTrue(speed <= 10.0f, "变速倍率最大支持 10.0");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (OutputStream outputStream = FileUtils.openOutputStream(outputFile)) {
			adjustSpeed(resource, outputStream, speed);
		}
	}

	/**
	 * 音频变速（速度改变，音调同步改变）
	 * <p>
	 * 基于 FFmpeg atempo 滤镜实现；单级滤镜有效区间 0.5 ~ 2.0，超出区间会自动多级叠加滤镜。
	 * 速度小于 1.0 为慢放，等于 1.0 为原速，大于 1.0 为快放。
	 * 输出流不会被自动关闭，需要调用方管理。
	 * </p>
	 *
	 * @param resource     源音频资源，不可为 null，必须为 audio 类型媒体
	 * @param outputStream 音频输出流，不可为 null，不会被自动关闭
	 * @param speed        变速倍率，合法范围：0.1 ~ 10.0
	 * @throws IOException              音频读取、滤镜处理、写入发生IO异常时抛出
	 * @throws IllegalArgumentException 入参为空、非音频类型、变速倍率非法时抛出
	 * @since 1.1.0
	 */
	public static void adjustSpeed(MediaResource resource, OutputStream outputStream, float speed) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputStream, "outputStream");
		Validate.isTrue(speed > 0, "变速倍率必须大于 0");
		Validate.isTrue(speed <= 10.0f, "变速倍率最大支持 10.0");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");

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
		String audioFilters = String.join(",", filterParts);

		applyFilter(resource, outputStream, audioFilters);
	}

	/**
	 * 通用音频滤镜处理核心方法
	 * <p>
	 * 通用链路：读取音频帧 → 经过 FFmpeg 音频滤镜处理 → 写入输出流；
	 * 适用于音频变速、音效处理等各类音频滤镜场景。
	 * <br><b>注意</b>：代码内部先通过抓取器获取真实声道/采样率再赋值给滤镜，规避构造传 0 导致滤镜失效问题。
	 * 输出流不会被自动关闭，需要调用方管理。
	 * </p>
	 *
	 * @param resource     源音频资源，不可为 null，必须为音频类型
	 * @param outputStream 音频输出流，不可为 null，不会被自动关闭
	 * @param audioFilters FFmpeg 音频滤镜表达式，不可为空字符串
	 * @throws IOException              音频读取、滤镜处理、流读写过程中发生IO异常时抛出
	 * @throws IllegalArgumentException 入参为空、非音频类型、滤镜表达式为空时抛出
	 * @since 1.1.0
	 */
	public static void applyFilter(MediaResource resource, OutputStream outputStream, String audioFilters) throws IOException {
		try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(resource.getInputStream());
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0, 0);
		     FFmpegFrameFilter filter = new FFmpegFrameFilter(audioFilters, 0)) {
			grabber.start();

			filter.setAudioChannels(grabber.getAudioChannels());
			filter.setSampleRate(grabber.getSampleRate());
			filter.start();

			initRecorder(recorder, grabber, null, null);

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
	 * 读取音频帧并写入录制器
	 * <p>循环从帧抓取器读取音频采样帧，写入帧录制器；
	 * 支持指定结束时间戳，到达指定位置后提前终止录制。</p>
	 *
	 * @param recorder     音频帧录制器，不可为 null
	 * @param grabber      音频帧抓取器，不可为 null
	 * @param endTimestamp 结束时间戳，单位：微秒；传 {@code null} 则录制至音频末尾
	 * @throws FFmpegFrameRecorder.Exception 帧录制过程发生异常时抛出
	 * @throws FrameGrabber.Exception        帧抓取过程发生异常时抛出
	 * @since 1.1.0
	 */
	protected static void recordSamples(FFmpegFrameRecorder recorder, FFmpegFrameGrabber grabber, Long endTimestamp)
		throws FFmpegFrameRecorder.Exception, FrameGrabber.Exception {
		if (Objects.nonNull(endTimestamp) && grabber.getTimestamp() > endTimestamp) {
			return;
		}

		while (true) {
			try (Frame frame = grabber.grabSamples()) {
				if (Objects.isNull(frame)) {
					break;
				}
				if (frame.samples != null) {
					recorder.record(frame);
				}
			}
		}
	}

	/**
	 * 初始化音频帧录制器参数
	 * <p>
	 * 优先级规则：
	 * <ol>
	 *  <li>优先使用 {@code outputFormat} 指定输出格式；</li>
	 *  <li>若传入 {@code outputAudio}，优先使用其内参数，参数值小于等于0时沿用原音频配置；</li>
	 *  <li>无自定义参数时，完全沿用帧抓取器的音频格式、采样率、声道、码率、元数据。</li>
	 * </ol>
	 * 调用该方法后会自动执行 {@code recorder.start()} 启动录制器。
	 * </p>
	 *
	 * @param recorder     待初始化的帧录制器，不可为 null
	 * @param grabber      源帧抓取器，用于读取原始音频参数，不可为 null
	 * @param outputAudio  自定义音频参数，可为 {@code null}；为 null 时沿用原音频配置
	 * @param outputFormat 自定义输出格式，可为 {@code null}；为 null 时沿用原音频格式
	 * @throws FFmpegFrameRecorder.Exception 录制器初始化、参数设置失败时抛出
	 * @since 1.1.0
	 */
	protected static void initRecorder(FFmpegFrameRecorder recorder, FFmpegFrameGrabber grabber, Audio outputAudio,
	                                   String outputFormat) throws FFmpegFrameRecorder.Exception {
		if (StringUtils.isNotBlank(outputFormat)) {
			recorder.setFormat(outputFormat);
		}

		if (Objects.nonNull(outputAudio)) {
			recorder.setFormat(StringUtils.isNotBlank(outputAudio.getFormat()) ? outputAudio.getFormat() : grabber.getFormat());
			recorder.setSampleRate(outputAudio.getSampleRate() <= 0 ? grabber.getSampleRate() : outputAudio.getSampleRate());
			recorder.setAudioBitrate(outputAudio.getBitrate() <= 0 ? grabber.getAudioBitrate() : outputAudio.getBitrate());
			recorder.setAudioChannels(outputAudio.getChannels() <= 0 ? grabber.getAudioChannels() : outputAudio.getChannels());
			recorder.setAudioMetadata(Objects.nonNull(outputAudio.getMetadata()) ? outputAudio.getMetadata() : grabber.getAudioMetadata());
		} else {
			recorder.setFormat(grabber.getFormat());
			recorder.setSampleRate(grabber.getSampleRate());
			recorder.setAudioBitrate(grabber.getAudioBitrate());
			recorder.setAudioChannels(grabber.getAudioChannels());
			recorder.setAudioMetadata(grabber.getAudioMetadata());
		}

		recorder.start();
	}

	/**
	 * 音频裁剪核心实现
	 * <p>
	 * 内部方法，用于实现音频裁剪的具体逻辑。通过 FFmpegFrameGrabber 读取源音频，
	 * 支持指定起始和结束时间截取音频片段，可以自定义输出格式和音频参数。
	 * </p>
	 *
	 * @param resource     音频资源，不可为 null，必须为音频类型
	 * @param outputStream 输出流，不可为 null，不会被自动关闭
	 * @param outputAudio  输出音频参数（可为 null，null 则完全沿用原参数）
	 * @param outputFormat 输出格式（可为 null，null 则沿用原格式）
	 * @param start        起始时间，不可为 null
	 * @param end          裁剪结束时间，可为 null；为 null 时裁剪至音频末尾
	 * @throws IOException              IO异常/音频处理异常（文件解析、写入失败等）
	 * @throws IllegalArgumentException 时间参数无效时抛出
	 * @since 1.1.0
	 */
	protected static void cut(MediaResource resource, OutputStream outputStream, Audio outputAudio, String outputFormat,
	                          Duration start, Duration end) throws IOException {
		try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(resource.getInputStream())) {
			long startTimestamp = start.toNanos() / 1000;
			long endTimestamp = Objects.isNull(end) ? grabber.getLengthInTime() : Math.min(end.toNanos() / 1000, grabber.getLengthInTime());

			Validate.isTrue(startTimestamp < grabber.getLengthInTime(), "start 必须小于音频总时长");

			grabber.setTimestamp(startTimestamp);
			grabber.start();

			try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0, 0)) {
				initRecorder(recorder, grabber, outputAudio, outputFormat);
				recordSamples(recorder, grabber, endTimestamp);
			}
		}
	}

	/**
	 * 音频转码核心实现
	 * <p>
	 * 内部方法，用于实现音频转码的具体逻辑。通过 FFmpegFrameGrabber 读取源音频，
	 * 再通过 FFmpegFrameRecorder 写入到输出流，期间可以指定输出格式和音频参数。
	 * </p>
	 *
	 * @param resource     音频资源，不可为 null，必须为音频类型
	 * @param outputStream 输出流，不可为 null
	 * @param outputAudio  输出音频参数（可为 null，null 则完全沿用原参数）
	 * @param outputFormat 输出格式（可为 null，null 则沿用原格式）
	 * @throws IOException IO异常/音频处理异常（文件解析、写入失败等）
	 * @since 1.1.0
	 */
	protected static void transcode(MediaResource resource, OutputStream outputStream, Audio outputAudio, String outputFormat) throws IOException {
		try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(resource.getInputStream());
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0, 0)) {
			grabber.start();

			initRecorder(recorder, grabber, outputAudio, outputFormat);
			recordSamples(recorder, grabber, null);
		}
	}

	/**
	 * 获取背景音乐采样值（支持缓存/实时读取）
	 * <p>
	 * 辅助方法，用于混音功能中获取背景音乐的采样值。
	 * 支持两种模式：缓存模式（适用于背景音乐较短，循环播放）和实时模式（适用于长背景音乐）。
	 * 返回值为归一化后的采样值，范围在 -1.0 到 1.0 之间。
	 * </p>
	 *
	 * @param useCache          是否使用缓存的PCM数据（true表示缓存模式，false表示实时读取）
	 * @param pageCache         PCM缓存页列表（缓存模式下使用）
	 * @param grabber           背景音乐帧抓取器（不可为 null）
	 * @param liveHolder        实时帧持有者（用于存储当前读取的帧，数组长度为1）
	 * @param pageIndex         缓存页索引（缓存模式下使用）
	 * @param cacheSampleOffset 缓存采样偏移量（缓存模式下使用）
	 * @param channelIndex      声道索引（从0开始）
	 * @return 归一化后的采样值（-1.0 ~ 1.0），当音频读取完毕时返回 0.0
	 * @throws IOException 读取帧异常
	 * @since 1.1.0
	 */
	private static float getBgmSample(boolean useCache,
	                                  List<short[]> pageCache,
	                                  FFmpegFrameGrabber grabber,
	                                  Frame[] liveHolder,
	                                  int pageIndex,
	                                  int cacheSampleOffset,
	                                  int channelIndex) throws IOException {
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