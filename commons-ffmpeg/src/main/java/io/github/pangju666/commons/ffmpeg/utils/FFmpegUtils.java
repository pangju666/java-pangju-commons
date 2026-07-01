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
import io.github.pangju666.commons.ffmpeg.lang.FFmpegConstants;
import io.github.pangju666.commons.ffmpeg.model.Audio;
import io.github.pangju666.commons.ffmpeg.model.Media;
import io.github.pangju666.commons.ffmpeg.model.MediaResource;
import io.github.pangju666.commons.ffmpeg.model.Video;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.ObjLongConsumer;

/**
 * FFmpeg 工具类，提供媒体处理的常用方法和过滤器生成
 * <p>
 * 该工具类封装了 FFmpeg 相关的常见操作，包括：
 * <ul>
 *     <li>文件路径处理和时间转换</li>
 *     <li>音频和视频过滤器生成</li>
 *     <li>媒体剪辑、拼接、转码等操作</li>
 *     <li>资源加载和释放</li>
 *     <li>图像抓取和帧提取</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @since 1.1.0
 */
public class FFmpegUtils {
	protected FFmpegUtils() {
	}

	/**
	 * 获取安全的文件源路径，用于 FFmpeg 滤镜参数
	 * <p>
	 * 将 Windows 路径分隔符替换为 Unix 风格，并对冒号进行转义，
	 * 确保路径在 FFmpeg 滤镜字符串中可以正确解析
	 * </p>
	 *
	 * @param filePath 原始文件路径
	 * @return 安全的文件路径字符串
	 * @throws IllegalArgumentException 当 filePath 为空或空白时
	 * @since 1.1.0
	 */
	public static String getSafeFileSourcePath(final String filePath) {
		Validate.notBlank(filePath, "filePath 不可为空");

		return FilenameUtils.separatorsToUnix(filePath).replace(":", "\\:");
	}

	/**
	 * 将 Duration 转换为 FFmpeg 使用的微秒时间戳
	 *
	 * @param duration 持续时间
	 * @return 微秒级时间戳
	 * @throws NullPointerException     当 duration 为 null 时
	 * @throws IllegalArgumentException 当 duration 为负数时
	 * @since 1.1.0
	 */
	public static long toTimestamp(final Duration duration) {
		Validate.notNull(duration, "duration 不可为null");
		Validate.isTrue(!duration.isNegative(), "duration 必须大于 0");

		return TimeUnit.NANOSECONDS.toMicros(duration.toNanos());
	}

	/**
	 * 获取添加背景音乐的滤镜字符串
	 * <p>
	 * 自动处理采样率转换和循环，确保背景音乐与主音频流匹配
	 * </p>
	 *
	 * @param mainGrabber 主音频/视频抓取器
	 * @param bgmGrabber  背景音乐抓取器
	 * @param bgmWeight   背景音乐的权重
	 * @return 完整的音频混合滤镜字符串
	 * @throws NullPointerException         当 mainGrabber 或 bgmGrabber 为 null 时
	 * @throws IllegalArgumentException     当 mainGrabber 或 bgmGrabber 无音频流时
	 * @throws FFmpegFrameGrabber.Exception 当抓取器启动失败时
	 * @since 1.1.0
	 */
	public static String getAddBgmFilter(final FFmpegFrameGrabber mainGrabber, final FFmpegFrameGrabber bgmGrabber,
	                                     final float bgmWeight) throws FFmpegFrameGrabber.Exception {
		Validate.notNull(mainGrabber, "mainGrabber 不可为 null");
		Validate.notNull(bgmGrabber, "bgmGrabber 不可为 null");

		if (isNotStarted(mainGrabber)) {
			mainGrabber.start();
		}
		if (isNotStarted(bgmGrabber)) {
			bgmGrabber.start();
		}
		Validate.isTrue(mainGrabber.hasAudio(), "mainGrabber 不存在音频流");
		Validate.isTrue(bgmGrabber.hasAudio(), "bgmGrabber 不存在音频流");

		long bgmLengthInTime = bgmGrabber.getLengthInTime();
		int mainSampleRate = mainGrabber.getSampleRate();

		FFmpegFiltersBuilder builder = FFmpegFiltersBuilder.audio()
			.addInput();
		if (mainSampleRate != bgmGrabber.getSampleRate()) {
			builder.addInput("bgm", String.format("aresample=%d", mainSampleRate));
		}
		if (mainGrabber.getLengthInTime() > bgmLengthInTime) {
			builder.addInput("bgm", FFmpegUtils.getAloopFilter(mainSampleRate, bgmLengthInTime));
		} else {
			builder.addInput();
		}
		return builder
			.addGlobalFilter(FFmpegUtils.getAmixFilter(2, 0, 1.0f, bgmWeight))
			.build();
	}

	/**
	 * 获取音频音量滤镜（使用浮点精度）
	 *
	 * @param db 音量变化的分贝值（正数表示增加的分贝值，负数表示减少的分贝值）
	 * @return 音量滤镜字符串
	 * @since 1.1.0
	 */
	public static String getVolumeFilter(final float db) {
		return getVolumeFilter(db, VolumePrecision.FLOAT);
	}

	/**
	 * 获取音频音量滤镜
	 *
	 * @param db        音量变化的分贝值（正数表示增加的分贝值，负数表示减少的分贝值）
	 * @param precision 数值精度
	 * @return 音量滤镜字符串
	 * @throws NullPointerException 当 precision 或 db 为 null 时
	 * @since 1.1.0
	 */
	public static String getVolumeFilter(final Number db, final VolumePrecision precision) {
		Validate.notNull(precision, "duration 不可为 null");
		Validate.notNull(db, "db 不可为 null");

		switch (precision) {
			case FIXED:
				int volumeVal = db.intValue();
				return String.format("volume=volume=%s%ddB:precision=fixed", volumeVal > 0 ? "+" : "", volumeVal);
			case DOUBLE:
				double dVolumeVal = db.doubleValue();
				return String.format("volume=volume=%s%.4fdB:precision=double", dVolumeVal > 0 ? "+" : "", dVolumeVal);
			case FLOAT:
			default:
				float fVolumeVal = db.floatValue();
				return String.format("volume=volume=%s%.4fdB:precision=float", fVolumeVal > 0 ? "+" : "", fVolumeVal);
		}
	}

	/**
	 * 获取音频混合滤镜（指定过渡时长）
	 *
	 * @param inputs            输入流数量（至少 2）
	 * @param dropoutTransition 淡出过渡时长（毫秒）
	 * @param weights           各输入流的权重数组
	 * @return amix 滤镜字符串
	 * @throws IllegalArgumentException 当 inputs 小于 2 或 dropoutTransition 为负数时
	 * @since 1.1.0
	 */
	public static String getAmixFilter(final int inputs, final int dropoutTransition, final float... weights) {
		return getAmixFilter(inputs, dropoutTransition, AmixDuration.FIRST, weights);
	}

	/**
	 * 获取音频混合滤镜（完整参数）
	 *
	 * @param inputs            输入流数量（至少 2）
	 * @param dropoutTransition 淡出过渡时长（毫秒）
	 * @param duration          输出时长模式
	 * @param weights           各输入流的权重数组
	 * @return amix 滤镜字符串
	 * @throws NullPointerException     当 duration 为 null 时
	 * @throws IllegalArgumentException 当 inputs 小于 2 或 dropoutTransition 为负数时
	 * @since 1.1.0
	 */
	public static String getAmixFilter(final int inputs, final int dropoutTransition, final AmixDuration duration,
	                                   final float... weights) {
		Validate.isTrue(inputs >= 2, "inputs 必须大于等于 2");
		Validate.isTrue(dropoutTransition >= 0, "dropoutTransition 必须大于等于 0");
		Validate.notNull(duration, "duration 不可为 null");

		String audioFilters = String.format("amix=inputs=%d:dropout_transition=%d:duration=%s",
			inputs, dropoutTransition, duration.value);

		List<Float> weightList = new ArrayList<>(inputs);
		for (int i = 0; i < weights.length; i++) {
			if (i == inputs) {
				break;
			}
			weightList.add(Math.max(weights[i], 0));
		}

		if (!weightList.isEmpty()) {
			audioFilters += (FFmpegConstants.FILTER_ARG_SEPARATOR + "weights='" + StringUtils.join(
				weightList, StringUtils.SPACE) + "'");
		}

		return audioFilters;
	}

	/**
	 * 获取音频循环滤镜（无限循环）
	 *
	 * @param sampleRate   采样率
	 * @param lengthInTime 音频时长（微秒）
	 * @return aloop 滤镜字符串
	 * @throws IllegalArgumentException 当 sampleRate 或 lengthInTime 小于等于 0 时
	 * @since 1.1.0
	 */
	public static String getAloopFilter(final int sampleRate, final long lengthInTime) {
		return getAloopFilter(-1, sampleRate, lengthInTime);
	}

	/**
	 * 获取音频循环滤镜
	 *
	 * @param loop         循环次数，-1 表示无限循环
	 * @param sampleRate   采样率
	 * @param lengthInTime 音频时长（微秒）
	 * @return aloop 滤镜字符串
	 * @throws IllegalArgumentException 当 loop 小于 -1，或 sampleRate/lengthInTime 小于等于 0 时
	 * @since 1.1.0
	 */
	public static String getAloopFilter(final int loop, final int sampleRate, final long lengthInTime) {
		Validate.isTrue(loop >= -1, "loop 必须大于等于 -1");
		Validate.isTrue(sampleRate > 0, "sampleRate 必须大于 0");
		Validate.isTrue(lengthInTime > 0, "lengthInTime 必须大于 0");

		long size = TimeUnit.MICROSECONDS.toSeconds(lengthInTime) * sampleRate * Math.max(1, loop);
		return String.format("aloop=loop=%d:size=%d", loop, size);
	}

	/**
	 * 获取音频时长裁剪滤镜（指定时长）
	 *
	 * @param duration 裁剪时长
	 * @return atrim 滤镜字符串
	 * @throws NullPointerException     当 duration 为 null 时
	 * @throws IllegalArgumentException 当 duration 为 0 或负数时
	 * @since 1.1.0
	 */
	public static String getAtrimFilter(final Duration duration) {
		Validate.notNull(duration, "duration 不可为null");
		Validate.isTrue(!duration.isZero() && !duration.isNegative(), "duration 必须大于 0");

		return getAtrimFilter(toTimestamp(duration));
	}

	/**
	 * 获取音频时长裁剪滤镜（指定开始和结束时间）
	 *
	 * @param start 开始时间
	 * @param end   结束时间
	 * @return atrim 滤镜字符串
	 * @throws NullPointerException     当 start 或 end 为 null 时
	 * @throws IllegalArgumentException 当 start 为负数，或 end 为 0/负数时
	 * @since 1.1.0
	 */
	public static String getAtrimFilter(final Duration start, final Duration end) {
		Validate.notNull(start, "start 不可为null");
		Validate.isTrue(!start.isZero() && !start.isNegative(), "start 必须大于等于 0");
		Validate.notNull(end, "end 不可为null");
		Validate.isTrue(!end.isZero() && end.isNegative(), "end 必须大于等于 0");

		return getAtrimFilter(toTimestamp(start), toTimestamp(end));
	}

	/**
	 * 获取音频时长裁剪滤镜（指定时间戳）
	 *
	 * @param lengthInTime 裁剪时间戳（微秒）
	 * @return atrim 滤镜字符串
	 * @throws IllegalArgumentException 当 lengthInTime 小于等于 0 时
	 * @since 1.1.0
	 */
	public static String getAtrimFilter(final long lengthInTime) {
		Validate.isTrue(lengthInTime > 0, "lengthInTime 必须大于 0");

		return String.format("atrim=duration=%dus", lengthInTime);
	}

	/**
	 * 获取音频时长裁剪滤镜（指定开始和结束时间戳）
	 *
	 * @param startTimestamp 开始时间戳（微秒）
	 * @param endTimestamp   结束时间戳（微秒）
	 * @return atrim 滤镜字符串
	 * @throws IllegalArgumentException 当 startTimestamp/endTimestamp 小于等于 0 或 endTimestamp 小于等于 startTimestamp 时
	 * @since 1.1.0
	 */
	public static String getAtrimFilter(final long startTimestamp, final long endTimestamp) {
		Validate.isTrue(startTimestamp > 0, "startTimestamp 必须大于 0");
		Validate.isTrue(endTimestamp > 0, "endTimestamp 必须大于 0");
		Validate.isTrue(endTimestamp > startTimestamp, "endTimestamp 必须大于 startTimestamp");

		return String.format("atrim=start=%dus:end=%dus", startTimestamp, endTimestamp);
	}

	/**
	 * 获取视频时长裁剪滤镜（指定时长）
	 *
	 * @param duration 裁剪时长
	 * @return trim 滤镜字符串
	 * @throws NullPointerException     当 duration 为 null 时
	 * @throws IllegalArgumentException 当 duration 为 0 或负数时
	 * @since 1.1.0
	 */
	public static String getTrimFilter(final Duration duration) {
		Validate.notNull(duration, "duration 不可为null");
		Validate.isTrue(!duration.isZero() && !duration.isNegative(), "duration 必须大于 0");

		return String.format("trim=duration=%dus", toTimestamp(duration));
	}

	/**
	 * 获取视频时长裁剪滤镜（指定开始和结束时间）
	 *
	 * @param start 开始时间
	 * @param end   结束时间
	 * @return trim 滤镜字符串
	 * @throws NullPointerException     当 start 或 end 为 null 时
	 * @throws IllegalArgumentException 当 start 为负数，或 end 为 0/负数时
	 * @since 1.1.0
	 */
	public static String getTrimFilter(final Duration start, final Duration end) {
		Validate.notNull(start, "start 不可为null");
		Validate.isTrue(!start.isZero() && !start.isNegative(), "start 必须大于等于 0");
		Validate.notNull(end, "end 不可为null");
		Validate.isTrue(!end.isZero() && end.isNegative(), "end 必须大于等于 0");

		return String.format("trim=start=%dus:end=%dus", toTimestamp(start), toTimestamp(end));
	}

	/**
	 * 获取视频时长裁剪滤镜（指定时间戳）
	 *
	 * @param lengthInTime 裁剪时间戳（微秒）
	 * @return trim 滤镜字符串
	 * @throws IllegalArgumentException 当 lengthInTime 小于等于 0 时
	 * @since 1.1.0
	 */
	public static String getTrimFilter(final long lengthInTime) {
		Validate.isTrue(lengthInTime > 0, "lengthInTime 必须大于 0");

		return String.format("trim=duration=%dus", lengthInTime);
	}

	/**
	 * 获取视频时长裁剪滤镜（指定开始和结束时间戳）
	 *
	 * @param startTimestamp 开始时间戳（微秒）
	 * @param endTimestamp   结束时间戳（微秒）
	 * @return trim 滤镜字符串
	 * @throws IllegalArgumentException 当 startTimestamp/endTimestamp 小于等于 0 或 endTimestamp 小于等于 startTimestamp 时
	 * @since 1.1.0
	 */
	public static String getTrimFilter(final long startTimestamp, final long endTimestamp) {
		Validate.isTrue(startTimestamp > 0, "startTimestamp 必须大于 0");
		Validate.isTrue(endTimestamp > 0, "endTimestamp 必须大于 0");
		Validate.isTrue(endTimestamp > startTimestamp, "endTimestamp 必须大于 startTimestamp");

		return String.format("trim=start=%dus:end=%dus", startTimestamp, endTimestamp);
	}

	/**
	 * 获取音频变速滤镜
	 *
	 * @param speed 播放速度（0.5 - 100）
	 * @return atempo 滤镜字符串
	 * @throws IllegalArgumentException 当 speed 超出 [0.5, 100] 范围时
	 * @since 1.1.0
	 */
	public static String getAtempoFilter(final float speed) {
		Validate.isTrue(speed >= 0.5, "speed 必须大于等于 0.5");
		Validate.isTrue(speed <= 100, "speed 必须小于等于 100");

		return String.format("atempo=%.3f", speed);
	}

	/**
	 * 获取视频画面裁剪滤镜
	 *
	 * @param x      裁剪区域左上角 x 坐标
	 * @param y      裁剪区域左上角 y 坐标
	 * @param width  裁剪宽度
	 * @param height 裁剪高度
	 * @return crop 滤镜字符串
	 * @throws IllegalArgumentException 当 x/y 为负数，或 width/height 小于等于 0 时
	 * @since 1.1.0
	 */
	public static String getCropFilter(final int x, final int y, final int width, final int height) {
		Validate.isTrue(x >= 0, "x 必须大于等于 0");
		Validate.isTrue(y >= 0, "y 必须大于等于 0");
		Validate.isTrue(width > 0, "width 必须大于 0");
		Validate.isTrue(height > 0, "height 必须大于 0");

		return String.format("crop=%d:%d:%d:%d", width, height, x, y);
	}

	/**
	 * 获取视频变速滤镜（用于变速）
	 * <p>
	 * 通过调整时间戳实现视频变速，需要与 fps 配合使用防止画面撕裂、与 atempo 配合使用以保持音视频同步
	 * </p>
	 *
	 * @param speed 播放速度（大于 0）
	 * @return setpts 滤镜字符串
	 * @throws IllegalArgumentException 当 speed 小于等于 0 时
	 * @since 1.1.0
	 */
	public static String getSetptsFilter(final float speed) {
		Validate.isTrue(speed > 0, "speed 必须大于 0");

		// setpts 系数 = 1 / 速度
		float ptsScale = 1.0f / speed;
		return String.format("setpts=%.6f*PTS", ptsScale);
	}

	/**
	 * 获取视频帧率滤镜
	 *
	 * @param frameRate 目标帧率
	 * @return fps 滤镜字符串
	 * @throws IllegalArgumentException 当 frameRate 小于等于 0 时
	 * @since 1.1.0
	 */
	public static String getFpsFilter(final double frameRate) {
		Validate.isTrue(frameRate > 0, "frameRate 必须大于 0");

		return String.format("fps=%.2f", frameRate);
	}

	/**
	 * 仅应用音频滤镜
	 *
	 * @param grabber         帧抓取器
	 * @param recorder        帧录制器
	 * @param outputMedia     输出媒体配置
	 * @param audioFilters    音频滤镜字符串
	 * @param frameType       处理的帧类型
	 * @param recorderStarted 录制器是否已启动
	 * @param <T>             媒体类型
	 * @throws IOException 当 I/O 错误发生时
	 * @since 1.1.0
	 */
	public static <T extends Media> void applyAudioFilter(final FFmpegFrameGrabber grabber, final FFmpegFrameRecorder recorder,
	                                                      final T outputMedia, final String audioFilters, final FrameType frameType,
	                                                      final boolean recorderStarted) throws IOException {
		applyFilter(grabber, recorder, null, outputMedia, null, audioFilters, frameType, recorderStarted);
	}

	/**
	 * 仅应用视频滤镜
	 *
	 * @param grabber         帧抓取器
	 * @param recorder        帧录制器
	 * @param outputMedia     输出媒体配置
	 * @param videoFilters    视频滤镜字符串
	 * @param frameType       处理的帧类型
	 * @param recorderStarted 录制器是否已启动
	 * @param <T>             媒体类型
	 * @throws IOException 当 I/O 错误发生时
	 * @since 1.1.0
	 */
	public static <T extends Media> void applyVideoFilter(final FFmpegFrameGrabber grabber, final FFmpegFrameRecorder recorder,
	                                                      final T outputMedia, final String videoFilters, final FrameType frameType,
	                                                      final boolean recorderStarted) throws IOException {
		applyFilter(grabber, recorder, null, outputMedia, videoFilters, null, frameType, recorderStarted);
	}

	/**
	 * 应用滤镜（单个输入源）
	 *
	 * @param grabber         帧抓取器
	 * @param recorder        帧录制器
	 * @param outputMedia     输出媒体配置
	 * @param videoFilters    视频滤镜字符串
	 * @param audioFilters    音频滤镜字符串
	 * @param frameType       处理的帧类型
	 * @param recorderStarted 录制器是否已启动
	 * @param <T>             媒体类型
	 * @throws IOException 当 I/O 错误发生时
	 * @since 1.1.0
	 */
	public static <T extends Media> void applyFilter(final FFmpegFrameGrabber grabber, final FFmpegFrameRecorder recorder,
	                                                 final T outputMedia, final String videoFilters,
	                                                 final String audioFilters, final FrameType frameType,
	                                                 final boolean recorderStarted) throws IOException {
		applyFilter(grabber, recorder, null, outputMedia, videoFilters, audioFilters, frameType, recorderStarted);
	}

	/**
	 * 应用滤镜（单个输入源，带滤镜媒体配置）
	 *
	 * @param grabber         帧抓取器
	 * @param recorder        帧录制器
	 * @param filterMedia     滤镜媒体配置
	 * @param outputMedia     输出媒体配置
	 * @param videoFilters    视频滤镜字符串
	 * @param audioFilters    音频滤镜字符串
	 * @param frameType       处理的帧类型
	 * @param recorderStarted 录制器是否已启动
	 * @param <T>             媒体类型
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 grabber、recorder 或 frameType 为 null，或 videoFilters 和 audioFilters 同时为空时
	 * @since 1.1.0
	 */
	public static <T extends Media> void applyFilter(final FFmpegFrameGrabber grabber,
	                                                 final FFmpegFrameRecorder recorder, final T filterMedia,
	                                                 final T outputMedia, final String videoFilters,
	                                                 final String audioFilters, final FrameType frameType,
	                                                 final boolean recorderStarted) throws IOException {
		Validate.notNull(grabber, "grabber 不可为 null");
		Validate.notNull(frameType, "frameMode 不可为 null");
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.isTrue(!StringUtils.isAllBlank(audioFilters, videoFilters),
			"audioFilters 或 videoFilters 至少需要一个不为空");

		if (isNotStarted(grabber)) {
			grabber.start();
		}

		int audioInputs = grabber.hasAudio() && StringUtils.isNotBlank(audioFilters) ? 1 : 0;
		int videoInputs = grabber.hasVideo() && StringUtils.isNotBlank(videoFilters) ? 1 : 0;

		try (FFmpegFrameFilter filter = openFrameFilter(videoFilters, audioFilters, grabber, filterMedia, audioInputs,
			videoInputs)) {
			if (!recorderStarted) {
				startRecorder(recorder, grabber, outputMedia, frameType);
			}

			while (true) {
				try (Frame frame = frameType.grabFrame(grabber)) {
					if (Objects.isNull(frame)) {
						break;
					}
					filter.push(frame);
				}

				recordFrames(recorder, filter, frameType);
			}

			recordFrames(recorder, filter, frameType);
		}
	}

	/**
	 * 应用滤镜（多个输入源）
	 *
	 * @param grabbers        帧抓取器列表
	 * @param recorder        帧录制器
	 * @param filterMedia     滤镜媒体配置
	 * @param outputMedia     输出媒体配置
	 * @param videoFilters    视频滤镜字符串
	 * @param audioFilters    音频滤镜字符串
	 * @param frameType       处理的帧类型
	 * @param recorderStarted 录制器是否已启动
	 * @param <T>             媒体类型
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 grabbers、frameType、filterMedia、outputMedia 或 recorder 为 null，
	 *                              或 grabbers 包含 null 元素，或 videoFilters 和 audioFilters 同时为空时
	 * @since 1.1.0
	 */
	public static <T extends Media> void applyFilter(final List<FFmpegFrameGrabber> grabbers,
	                                                 final FFmpegFrameRecorder recorder, final T filterMedia,
	                                                 final T outputMedia, final String videoFilters,
	                                                 final String audioFilters, final FrameType frameType,
	                                                 final boolean recorderStarted) throws IOException {
		Validate.notEmpty(grabbers, "grabbers 不可为空");
		Validate.isTrue(grabbers.stream().allMatch(Objects::nonNull), "集合中存在为 null的 grabber");
		Validate.notNull(frameType, "frameMode 不可为 null");
		Validate.notNull(filterMedia, "filterMedia 不可为 null");
		Validate.notNull(outputMedia, "outputMedia 不可为 null");
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.isTrue(!StringUtils.isAllBlank(audioFilters, videoFilters),
			"audioFilters 或 videoFilters 至少需要一个不为空");

		int audioInputs = 0;
		int videoInputs = 0;
		for (FFmpegFrameGrabber grabber : grabbers) {
			if (isNotStarted(grabber)) {
				grabber.start();
			}

			if (grabber.hasAudio() && StringUtils.isNotBlank(audioFilters)) {
				++audioInputs;
			}
			if (grabber.hasVideo() && StringUtils.isNotBlank(videoFilters)) {
				++videoInputs;
			}
		}

		try (FFmpegFrameFilter filter = openFrameFilter(videoFilters, audioFilters, null, filterMedia,
			audioInputs, videoInputs)) {
			boolean hasFrame;

			if (!recorderStarted) {
				startRecorder(recorder, null, outputMedia, frameType);
			}

			do {
				hasFrame = false;

				for (int i = 0; i < grabbers.size(); i++) {
					FFmpegFrameGrabber grabber = grabbers.get(i);

					try (Frame frame = frameType.grabFrame(grabber)) {
						if (Objects.nonNull(frame)) {
							filter.push(i, frame);
							hasFrame = true;
						}
					}
				}

				recordFrames(recorder, filter, frameType);
			} while (hasFrame);

			recordFrames(recorder, filter, frameType);
		}
	}

	/**
	 * 裁剪媒体（从开头裁剪指定时长）
	 *
	 * @param grabber         帧抓取器
	 * @param recorder        帧录制器
	 * @param outputMedia     输出媒体配置
	 * @param duration        裁剪时长
	 * @param frameType       处理的帧类型
	 * @param recorderStarted 录制器是否已启动
	 * @param <T>             媒体类型
	 * @throws IOException 当 I/O 错误发生时
	 * @since 1.1.0
	 */
	public static <T extends Media> void cut(final FFmpegFrameGrabber grabber, final FFmpegFrameRecorder recorder,
	                                         final T outputMedia, final Duration duration, final FrameType frameType,
	                                         final boolean recorderStarted) throws IOException {
		cut(grabber, recorder, outputMedia, 0, toTimestamp(duration), frameType, recorderStarted);
	}

	/**
	 * 裁剪媒体（指定开始和结束时间）
	 *
	 * @param grabber         帧抓取器
	 * @param recorder        帧录制器
	 * @param outputMedia     输出媒体配置
	 * @param start           开始时间
	 * @param end             结束时间
	 * @param frameType       处理的帧类型
	 * @param recorderStarted 录制器是否已启动
	 * @param <T>             媒体类型
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 start 或 end 为 null 时
	 * @throws IllegalArgumentException 当 start 为负数，或 end 为 0/负数时
	 * @since 1.1.0
	 */
	public static <T extends Media> void cut(final FFmpegFrameGrabber grabber, final FFmpegFrameRecorder recorder,
	                                         final T outputMedia, final Duration start, final Duration end,
	                                         final FrameType frameType, final boolean recorderStarted) throws IOException {
		Validate.notNull(start, "start 不可为null");
		Validate.isTrue(!start.isNegative(), "start 必须大于等于 0");
		Validate.notNull(end, "end 不可为null");
		Validate.isTrue(!end.isZero() && !end.isNegative(), "end 必须大于 0");

		cut(grabber, recorder, outputMedia, toTimestamp(start), toTimestamp(end),
			frameType, recorderStarted);
	}

	/**
	 * 裁剪媒体（指定开始和结束时间微秒）
	 *
	 * @param grabber         帧抓取器
	 * @param recorder        帧录制器
	 * @param outputMedia     输出媒体配置
	 * @param startTimestamp  开始时间（微秒）
	 * @param endTimestamp    结束时间（微秒）
	 * @param frameType       处理的帧类型
	 * @param recorderStarted 录制器是否已启动
	 * @param <T>             媒体类型
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 grabber 或 recorder 为 null 时
	 * @since 1.1.0
	 */
	public static <T extends Media> void cut(final FFmpegFrameGrabber grabber, final FFmpegFrameRecorder recorder,
	                                         final T outputMedia, long startTimestamp, long endTimestamp,
	                                         final FrameType frameType, final boolean recorderStarted) throws IOException {
		Validate.notNull(grabber, "resource 不可为 null");
		Validate.notNull(recorder, "recorder 不可为 null");

		if (isNotStarted(grabber)) {
			grabber.start();
		}

		long lengthInTime = grabber.getLengthInTime();
		startTimestamp = Math.min(startTimestamp, lengthInTime);
		endTimestamp = Math.min(endTimestamp, lengthInTime);
		Validate.isTrue(startTimestamp <= endTimestamp, "startTimestamp 不能大于 endTimestamp");

		if (!recorderStarted) {
			startRecorder(recorder, grabber, outputMedia, frameType);
		}

		if (startTimestamp != endTimestamp) {
			grabber.setTimestamp(startTimestamp);

			while (true) {
				try (Frame frame = frameType.grabFrame(grabber)) {
					if (grabber.getTimestamp() >= endTimestamp) {
						break;
					}

					recorder.record(frame);
				}
			}
		}
	}

	/**
	 * 拼接媒体（从媒体资源）
	 *
	 * @param resources       媒体资源集合
	 * @param recorder        帧录制器
	 * @param outputMedia     输出媒体配置
	 * @param frameType       处理的帧类型
	 * @param recorderStarted 录制器是否已启动
	 * @param <T>             媒体类型
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 resources、frameType 或 recorder 为 null，或 resources 包含 null 元素时
	 * @throws IllegalArgumentException 当 resources 为空时
	 * @since 1.1.0
	 */
	public static <T extends Media> void concatByResource(final Collection<MediaResource> resources,
	                                                      final FFmpegFrameRecorder recorder, final T outputMedia,
	                                                      final FrameType frameType, final boolean recorderStarted) throws IOException {
		Validate.notEmpty(resources, "resources 不可为空");
		Validate.isTrue(resources.stream().allMatch(Objects::nonNull), "集合中存在为 null的 grabber");
		Validate.notNull(frameType, "frameMode 不可为 null");
		Validate.notNull(recorder, "recorder 不可为 null");

		boolean started = recorderStarted;
		for (MediaResource resource : resources) {
			try (FFmpegFrameGrabber grabber = openFrameGrabber(resource)) {
				grabber.start();

				if (!started) {
					startRecorder(recorder, grabber, outputMedia, frameType);

					started = true;
				}

				recordFrames(recorder, grabber, frameType);
			}
		}
	}

	/**
	 * 拼接媒体（从帧抓取器）
	 *
	 * @param grabbers        帧抓取器集合
	 * @param recorder        帧录制器
	 * @param outputMedia     输出媒体配置
	 * @param frameType       处理的帧类型
	 * @param recorderStarted 录制器是否已启动
	 * @param <T>             媒体类型
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 grabbers、frameType 或 recorder 为 null，或 grabbers 包含 null 元素时
	 * @throws IllegalArgumentException 当 grabbers 为空时
	 * @since 1.1.0
	 */
	public static <T extends Media> void concat(final Collection<FFmpegFrameGrabber> grabbers,
	                                            final FFmpegFrameRecorder recorder, final T outputMedia,
	                                            final FrameType frameType, final boolean recorderStarted) throws IOException {
		Validate.notEmpty(grabbers, "resources 不可为空");
		Validate.isTrue(grabbers.stream().allMatch(Objects::nonNull), "集合中存在为 null的 grabber");
		Validate.notNull(frameType, "frameMode 不可为 null");
		Validate.notNull(recorder, "recorder 不可为 null");

		boolean started = recorderStarted;
		for (FFmpegFrameGrabber grabber : grabbers) {
			if (isNotStarted(grabber)) {
				grabber.start();
			}

			if (!started) {
				startRecorder(recorder, grabber, outputMedia, frameType);

				started = true;
			}

			recordFrames(recorder, grabber, frameType);
		}
	}

	/**
	 * 转码媒体
	 * <p>
	 * 从输入媒体读取帧并直接写入输出，格式和编码由输出媒体配置决定
	 * </p>
	 *
	 * @param grabber         帧抓取器
	 * @param recorder        帧录制器
	 * @param outputMedia     输出媒体配置
	 * @param frameType       处理的帧类型
	 * @param recorderStarted 录制器是否已启动
	 * @param <T>             媒体类型
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 grabber、recorder 或 frameType 为 null 时
	 * @since 1.1.0
	 */
	public static <T extends Media> void transcode(final FFmpegFrameGrabber grabber, final FFmpegFrameRecorder recorder,
	                                               final T outputMedia, final FrameType frameType,
	                                               final boolean recorderStarted) throws IOException {
		Validate.notNull(grabber, "grabber 不可为 null");
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.notNull(frameType, "frameMode 不可为 null");

		if (isNotStarted(grabber)) {
			grabber.start();
		}

		if (!recorderStarted) {
			startRecorder(recorder, grabber, outputMedia, frameType);
		}
		recordFrames(recorder, grabber, frameType);
	}

	/**
	 * 打开帧抓取器
	 *
	 * @param resource 媒体资源
	 * @return 已打开的帧抓取器
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 resource 为 null 时
	 * @since 1.1.0
	 */
	public static FFmpegFrameGrabber openFrameGrabber(final MediaResource resource) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");

		if (resource.isFile()) {
			return new FFmpegFrameGrabber(resource.getFile());
		} else {
			return new FFmpegFrameGrabber(resource.getInputStream());
		}
	}

	/**
	 * 打开并启动帧滤镜
	 *
	 * @param videoFilters 视频滤镜字符串
	 * @param audioFilters 音频滤镜字符串
	 * @param grabber      源帧抓取器（用于获取媒体信息，可为 null）
	 * @param filterMedia  滤镜媒体配置（用于获取媒体信息，可为 null）
	 * @param audioInputs  音频输入数量
	 * @param videoInputs  视频输入数量
	 * @param <T>          媒体类型
	 * @return 已启动的帧滤镜
	 * @throws FFmpegFrameFilter.Exception  当滤镜启动失败时
	 * @throws FFmpegFrameGrabber.Exception 当抓取器操作失败时
	 * @throws IllegalArgumentException     当 videoFilters 和 audioFilters 同时为空，
	 *                                      或 grabber 和 filterMedia 同时为 null，
	 *                                      或 audioInputs/videoInputs 为负数时
	 * @since 1.1.0
	 */
	public static <T extends Media> FFmpegFrameFilter openFrameFilter(final String videoFilters, final String audioFilters,
	                                                                  final FFmpegFrameGrabber grabber, final T filterMedia,
	                                                                  final int audioInputs, final int videoInputs)
		throws FFmpegFrameFilter.Exception, FFmpegFrameGrabber.Exception {
		Validate.isTrue(!StringUtils.isAllBlank(audioFilters, videoFilters),
			"audioFilters 或 videoFilters 至少需要一个不为空");

		FFmpegFrameFilter filter = new FFmpegFrameFilter(videoFilters, audioFilters, 0, 0, 0);

		startFilter(filter, grabber, filterMedia, audioInputs, videoInputs);

		return filter;
	}

	/**
	 * 录制帧（从两个独立的视频和音频抓取器）
	 *
	 * @param recorder     帧录制器
	 * @param videoGrabber 视频抓取器
	 * @param audioGrabber 音频抓取器
	 * @throws FFmpegFrameRecorder.Exception 当录制失败时
	 * @throws FrameGrabber.Exception        当抓取失败时
	 * @throws NullPointerException          当 recorder、videoGrabber 或 audioGrabber 为 null 时
	 * @throws IllegalArgumentException      当 videoGrabber 无视频流或 audioGrabber 无音频流时
	 * @since 1.1.0
	 */
	public static void recordFrames(final FFmpegFrameRecorder recorder, final FFmpegFrameGrabber videoGrabber,
	                                final FFmpegFrameGrabber audioGrabber) throws FFmpegFrameRecorder.Exception, FrameGrabber.Exception {
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.notNull(videoGrabber, "videoGrabber 不可为 null");
		Validate.notNull(audioGrabber, "audioGrabber 不可为 null");

		if (isNotStarted(videoGrabber)) {
			videoGrabber.start();
		}
		Validate.isTrue(videoGrabber.hasVideo(), "videoGrabber 不存在视频流");

		if (isNotStarted(audioGrabber)) {
			audioGrabber.start();
		}
		Validate.isTrue(audioGrabber.hasAudio(), "audioGrabber 不存在音频流");

		while (true) {
			try (Frame videoFrame = FrameType.VIDEO.grabFrame(videoGrabber)) {
				if (Objects.isNull(videoFrame)) {
					break;
				}
				recorder.record(videoFrame);
			}

			try (Frame audioFrame = FrameType.AUDIO.grabFrame(audioGrabber)) {
				if (Objects.nonNull(audioFrame)) {
					recorder.record(audioFrame);
				}
			}
		}
	}

	/**
	 * 录制帧（从单个抓取器）
	 *
	 * @param recorder  帧录制器
	 * @param grabber   帧抓取器
	 * @param frameType 处理的帧类型
	 * @throws FFmpegFrameRecorder.Exception 当录制失败时
	 * @throws FrameGrabber.Exception        当抓取失败时
	 * @throws NullPointerException          当 recorder、grabber 或 frameType 为 null 时
	 * @since 1.1.0
	 */
	public static void recordFrames(final FFmpegFrameRecorder recorder, final FFmpegFrameGrabber grabber,
	                                final FrameType frameType) throws FFmpegFrameRecorder.Exception, FrameGrabber.Exception {
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.notNull(grabber, "grabber 不可为 null");
		Validate.notNull(frameType, "frameMode 不可为 null");

		if (isNotStarted(grabber)) {
			grabber.start();
		}

		while (true) {
			try (Frame frame = frameType.grabFrame(grabber)) {
				if (Objects.isNull(frame)) {
					break;
				}
				recorder.record(frame);
			}
		}
	}

	/**
	 * 录制帧（从帧滤镜）
	 *
	 * @param recorder  帧录制器
	 * @param filter    帧滤镜
	 * @param frameType 处理的帧类型
	 * @throws FFmpegFrameRecorder.Exception 当录制失败时
	 * @throws FFmpegFrameFilter.Exception   当滤镜操作失败时
	 * @throws NullPointerException          当 recorder、filter 或 frameType 为 null 时
	 * @since 1.1.0
	 */
	public static void recordFrames(final FFmpegFrameRecorder recorder, final FFmpegFrameFilter filter,
	                                final FrameType frameType) throws FFmpegFrameRecorder.Exception, FFmpegFrameFilter.Exception {
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.notNull(filter, "filter 不可为 null");
		Validate.notNull(frameType, "frameMode 不可为 null");

		while (true) {
			try (Frame frame = frameType.pullFrame(filter)) {
				if (Objects.isNull(frame)) {
					break;
				}
				recorder.record(frame);
			}
		}
	}

	/**
	 * 启动录制器（单个抓取器）
	 *
	 * @param recorder    帧录制器
	 * @param grabber     源帧抓取器
	 * @param outputMedia 输出媒体配置
	 * @param frameType   处理的帧类型
	 * @param <T>         媒体类型
	 * @throws FFmpegFrameRecorder.Exception 当录制器启动失败时
	 * @throws FFmpegFrameGrabber.Exception  当抓取器操作失败时
	 * @since 1.1.0
	 */
	public static <T extends Media> void startRecorder(final FFmpegFrameRecorder recorder, final FFmpegFrameGrabber grabber,
	                                                   final T outputMedia, final FrameType frameType) throws FFmpegFrameRecorder.Exception, FFmpegFrameGrabber.Exception {
		startRecorder(recorder, grabber, grabber, outputMedia, frameType);
	}

	/**
	 * 启动录制器（视频和音频抓取器分离）
	 *
	 * @param recorder     帧录制器
	 * @param videoGrabber 视频抓取器
	 * @param audioGrabber 音频抓取器
	 * @param outputMedia  输出媒体配置
	 * @param frameType    处理的帧类型
	 * @param <T>          媒体类型
	 * @throws FFmpegFrameRecorder.Exception 当录制器启动失败时
	 * @throws FFmpegFrameGrabber.Exception  当抓取器操作失败时
	 * @throws NullPointerException          当 recorder 为 null 时
	 * @throws IllegalArgumentException      当 outputMedia 与 (videoGrabber 和 audioGrabber) 同时为 null 时
	 * @since 1.1.0
	 */
	public static <T extends Media> void startRecorder(final FFmpegFrameRecorder recorder,
	                                                   final FFmpegFrameGrabber videoGrabber,
	                                                   final FFmpegFrameGrabber audioGrabber, final T outputMedia,
	                                                   final FrameType frameType) throws FFmpegFrameRecorder.Exception, FFmpegFrameGrabber.Exception {
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.isTrue(Objects.nonNull(outputMedia) || ObjectUtils.allNotNull(videoGrabber, audioGrabber),
			"outputMedia 或 （videoGrabber 和 audioGrabber） 不可同时为 null");

		if (Objects.nonNull(outputMedia)) {
			if (outputMedia instanceof Audio && frameType != FrameType.VIDEO) {
				Audio audio = (Audio) outputMedia;

				recorder.setFormat(audio.getFormat());
				recorder.setSampleRate(audio.getSampleRate());
				recorder.setAudioCodec(audio.getCodecId());
				recorder.setAudioCodecName(audio.getCodecName());
				recorder.setAudioBitrate(audio.getBitrate());
				recorder.setAudioChannels(audio.getChannels());
				recorder.setAudioMetadata(new HashMap<>(audio.getMetadata()));
			} else if (outputMedia instanceof Video) {
				Video video = (Video) outputMedia;

				if (frameType != FrameType.AUDIO) {
					recorder.setFrameRate(video.getFrameRate());
					recorder.setVideoBitrate(video.getBitrate());
					recorder.setVideoCodec(video.getCodecId());
					recorder.setVideoCodecName(video.getCodecName());
					recorder.setImageWidth(video.getWidth());
					recorder.setImageHeight(video.getHeight());
					recorder.setVideoMetadata(new HashMap<>(video.getMetadata()));
				}

				if (Objects.nonNull(video.getAudio()) && frameType != FrameType.VIDEO) {
					recorder.setSampleRate(video.getAudio().getSampleRate());
					recorder.setAudioCodec(video.getAudio().getCodecId());
					recorder.setAudioCodecName(video.getAudio().getCodecName());
					recorder.setAudioBitrate(video.getAudio().getBitrate());
					recorder.setAudioChannels(video.getAudio().getChannels());
					recorder.setAudioMetadata(new HashMap<>(video.getAudio().getMetadata()));
				}
			}
		} else {
			if (isNotStarted(audioGrabber)) {
				audioGrabber.start();
			}

			recorder.setFormat(audioGrabber.getFormat());
			if (frameType != FrameType.VIDEO && audioGrabber.hasAudio()) {
				recorder.setAudioCodec(audioGrabber.getAudioCodec());
				recorder.setAudioCodecName(audioGrabber.getAudioCodecName());
				recorder.setSampleRate(audioGrabber.getSampleRate());
				recorder.setAudioBitrate(audioGrabber.getAudioBitrate());
				recorder.setAudioChannels(audioGrabber.getAudioChannels());
				recorder.setAudioMetadata(audioGrabber.getAudioMetadata());
			}

			if (isNotStarted(videoGrabber)) {
				videoGrabber.start();
			}

			recorder.setFormat(videoGrabber.getFormat());
			if (frameType != FrameType.AUDIO && videoGrabber.hasVideo()) {
				recorder.setVideoCodec(videoGrabber.getVideoCodec());
				recorder.setVideoCodecName(videoGrabber.getVideoCodecName());
				recorder.setFrameRate(videoGrabber.getFrameRate());
				recorder.setVideoBitrate(videoGrabber.getVideoBitrate());
				recorder.setImageWidth(videoGrabber.getImageWidth());
				recorder.setImageHeight(videoGrabber.getImageHeight());
				recorder.setVideoMetadata(videoGrabber.getVideoMetadata());
			}
		}

		recorder.start();
	}

	/**
	 * 启动帧滤镜
	 *
	 * @param filter      帧滤镜
	 * @param grabber     源帧抓取器（用于获取媒体信息，可为 null）
	 * @param filterMedia 滤镜媒体配置（用于获取媒体信息，可为 null）
	 * @param audioInputs 音频输入数量
	 * @param videoInputs 视频输入数量
	 * @param <T>         媒体类型
	 * @throws FFmpegFrameFilter.Exception  当滤镜启动失败时
	 * @throws FFmpegFrameGrabber.Exception 当抓取器操作失败时
	 * @throws NullPointerException         当 filter 为 null 时
	 * @throws IllegalArgumentException     当 audioInputs/videoInputs 为负数，
	 *                                      或 grabber 和 filterMedia 同时为 null 时
	 * @since 1.1.0
	 */
	public static <T extends Media> void startFilter(final FFmpegFrameFilter filter, final FFmpegFrameGrabber grabber,
	                                                 final T filterMedia, final int audioInputs, final int videoInputs)
		throws FFmpegFrameFilter.Exception, FFmpegFrameGrabber.Exception {
		Validate.notNull(filter, "filter 不可为 null");
		Validate.isTrue(audioInputs >= 0, "audioInputs 必须大于等于0");
		Validate.isTrue(videoInputs >= 0, "videoInputs 必须大于等于0");
		Validate.isTrue(ObjectUtils.anyNotNull(grabber, filterMedia),
			"grabber 或 filterMedia 不可同时为 null");

		filter.setAudioInputs(audioInputs);
		filter.setVideoInputs(videoInputs);

		if (Objects.nonNull(filterMedia)) {
			if (filterMedia instanceof Audio && audioInputs > 0) {
				Audio audio = (Audio) filterMedia;
				filter.setSampleRate(audio.getSampleRate());
				filter.setAudioChannels(audio.getChannels());
			} else if (filterMedia instanceof Video) {
				Video video = (Video) filterMedia;

				if (videoInputs > 0) {
					filter.setFrameRate(video.getFrameRate());
					filter.setImageWidth(video.getWidth());
					filter.setImageHeight(video.getHeight());
				}

				if (Objects.nonNull(video.getAudio()) && audioInputs > 0) {
					filter.setSampleRate(video.getAudio().getSampleRate());
					filter.setAudioChannels(video.getAudio().getChannels());
				}
			}
		} else {
			if (isNotStarted(grabber)) {
				grabber.start();
			}

			if (grabber.hasVideo() && videoInputs > 0) {
				filter.setFrameRate(grabber.getFrameRate());
				filter.setImageWidth(grabber.getImageWidth());
				filter.setImageHeight(grabber.getImageHeight());
			}

			if (grabber.hasAudio() && audioInputs > 0) {
				filter.setSampleRate(grabber.getSampleRate());
				filter.setAudioChannels(grabber.getAudioChannels());
			}
		}

		filter.start();
	}

	/**
	 * 判断抓取器是否未启动
	 *
	 * @param grabber 帧抓取器
	 * @return 如果未启动返回 true，否则返回 false
	 * @since 1.1.0
	 */
	public static boolean isNotStarted(final FFmpegFrameGrabber grabber) {
		return Objects.isNull(grabber.getFormatContext()) || grabber.getFormatContext().isNull();
	}

	/**
	 * 在指定时间点抓取单帧图像
	 *
	 * @param grabber   帧抓取器
	 * @param timestamp 时间点
	 * @return 抓取到的图像
	 * @throws FrameGrabber.Exception   当抓取失败时
	 * @throws NullPointerException     当 grabber 或 timestamp 为 null 时
	 * @throws IllegalArgumentException 当 timestamp 超过媒体总时长时
	 * @since 1.1.0
	 */
	public static BufferedImage grabImageAtTimestamp(final FFmpegFrameGrabber grabber, final Duration timestamp) throws FrameGrabber.Exception {
		Validate.notNull(grabber, "grabber 不可为 null");
		Validate.notNull(timestamp, "timestamp 不可为 null");

		if (isNotStarted(grabber)) {
			grabber.start();
		}

		long timestampMicros = toTimestamp(timestamp);
		Validate.isTrue(timestampMicros <= grabber.getLengthInTime(), "timestamp 必须小于等于总时长");
		grabber.setTimestamp(timestampMicros);

		try (Frame frame = grabber.grabImage();
		     Java2DFrameConverter converter = new Java2DFrameConverter()) {
			return converter.convert(frame);
		}
	}

	/**
	 * 按固定间隔抓取关键帧图像（返回列表）
	 *
	 * @param grabber  帧抓取器
	 * @param interval 间隔时间
	 * @param timeUnit 时间单位
	 * @return 抓取到的图像列表
	 * @throws FrameGrabber.Exception   当抓取失败时
	 * @throws NullPointerException     当 grabber 或 timeUnit 为 null 时
	 * @throws IllegalArgumentException 当 interval 小于等于 0 时
	 * @since 1.1.0
	 */
	public static List<BufferedImage> grabImagePeriodically(final FFmpegFrameGrabber grabber, final long interval,
	                                                        final TimeUnit timeUnit) throws FrameGrabber.Exception {
		Validate.notNull(grabber, "grabber 不可为 null");
		Validate.isTrue(interval > 0, "interval 必须大于 0");
		Validate.notNull(timeUnit, "timeUnit 不可为 null");

		if (isNotStarted(grabber)) {
			grabber.start();
		}

		long currentTimestamp = 0;
		long endTimestamp = grabber.getLengthInTime();
		long intervalMicros = timeUnit.toMicros(interval);
		List<BufferedImage> images = new ArrayList<>(Math.min((int) (endTimestamp / intervalMicros), Integer.MAX_VALUE));

		try (Java2DFrameConverter converter = new Java2DFrameConverter()) {
			while (currentTimestamp < endTimestamp) {
				grabber.setTimestamp(currentTimestamp);

				try (Frame frame = grabber.grabKeyFrame()) {
					if (Objects.nonNull(frame) && !Objects.isNull(frame.image)) {
						BufferedImage image = converter.convert(frame);
						images.add(image);
					}

					currentTimestamp = Math.min(currentTimestamp + intervalMicros, endTimestamp);
				}
			}
		}

		return images;
	}

	/**
	 * 按固定间隔抓取关键帧图像（使用消费者回调）
	 *
	 * @param grabber  帧抓取器
	 * @param interval 间隔时间
	 * @param timeUnit 时间单位
	 * @param consumer 图像消费者，接收图像和对应的时间戳（微秒）
	 * @throws FrameGrabber.Exception   当抓取失败时
	 * @throws NullPointerException     当 grabber、timeUnit 或 consumer 为 null 时
	 * @throws IllegalArgumentException 当 interval 小于等于 0 时
	 * @since 1.1.0
	 */
	public static void grabImagePeriodically(final FFmpegFrameGrabber grabber, final long interval, final TimeUnit timeUnit,
	                                         final ObjLongConsumer<BufferedImage> consumer) throws FrameGrabber.Exception {
		Validate.notNull(grabber, "grabber 不可为 null");
		Validate.isTrue(interval > 0, "interval 必须大于 0");
		Validate.notNull(consumer, "consumer 不可为 null");
		Validate.notNull(timeUnit, "timeUnit 不可为 null");

		if (isNotStarted(grabber)) {
			grabber.start();
		}

		long currentTimestamp = 0;
		long endTimestamp = grabber.getLengthInTime();
		long intervalMicros = timeUnit.toMicros(interval);

		try (Java2DFrameConverter converter = new Java2DFrameConverter()) {
			while (currentTimestamp < endTimestamp) {
				grabber.setTimestamp(currentTimestamp);

				try (Frame frame = grabber.grabKeyFrame()) {
					if (Objects.nonNull(frame) && !Objects.isNull(frame.image)) {
						BufferedImage image = converter.convert(frame);
						consumer.accept(image, currentTimestamp);
						image.flush();
					}

					currentTimestamp = Math.min(currentTimestamp + intervalMicros, endTimestamp);
				}
			}
		}
	}

	/**
	 * 启用 FFmpeg 日志（默认警告级别）
	 *
	 * @since 1.1.0
	 */
	public static void enableLog() {
		enableLog(avutil.AV_LOG_WARNING);
	}

	/**
	 * 启用 FFmpeg 日志（指定级别）
	 *
	 * @param level 日志级别（使用 FFmpeg 的 AV_LOG_* 常量）
	 * @since 1.1.0
	 */
	public static void enableLog(int level) {
		FFmpegLogCallback.set();
		avutil.av_log_set_level(level);
	}

	/**
	 * 音频混合时长模式枚举
	 *
	 * @author pangju666
	 * @since 1.1.0
	 */
	public enum AmixDuration {
		/**
		 * 使用第一个输入流的时长
		 *
		 * @since 1.1.0
		 */
		FIRST("first"),
		/**
		 * 使用最长输入流的时长
		 *
		 * @since 1.1.0
		 */
		LONGEST("longest"),
		/**
		 * 使用最短输入流的时长
		 *
		 * @since 1.1.0
		 */
		SHORTEST("shortest");

		/**
		 * FFmpeg 滤镜参数值
		 *
		 * @since 1.1.0
		 */
		public final String value;

		AmixDuration(String value) {
			this.value = value;
		}
	}

	/**
	 * 音量滤镜精度枚举
	 *
	 * @author pangju666
	 * @since 1.1.0
	 */
	public enum VolumePrecision {
		/**
		 * 整数精度
		 *
		 * @since 1.1.0
		 */
		FIXED,
		/**
		 * 单精度浮点
		 *
		 * @since 1.1.0
		 */
		FLOAT,
		/**
		 * 双精度浮点
		 *
		 * @since 1.1.0
		 */
		DOUBLE
	}
}
