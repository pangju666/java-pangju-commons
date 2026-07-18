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
import io.github.pangju666.commons.ffmpeg.lang.FFmpegConstants;
import io.github.pangju666.commons.ffmpeg.model.AudioOutputOption;
import io.github.pangju666.commons.ffmpeg.model.OutputOption;
import io.github.pangju666.commons.ffmpeg.model.VideoOutputOption;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.bytedeco.ffmpeg.avcodec.AVCodec;
import org.bytedeco.ffmpeg.avformat.AVOutputFormat;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avformat;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.ObjLongConsumer;

/**
 * FFmpeg 工具类，提供媒体处理的常用方法和过滤器生成
 * <p>
 * 该工具类封装了 FFmpeg 相关的常见操作，包括：
 * </p>
 * <h3>路径和时间处理</h3>
 * <ul>
 *     <li>文件路径安全转换（适配 FFmpeg 滤镜参数格式）</li>
 *     <li>Duration 与微秒时间戳转换</li>
 *     <li>格式字符串解析（从逗号分隔的格式中提取格式）</li>
 * </ul>
 * <h3>格式和编码器验证</h3>
 * <ul>
 *     <li>支持的格式验证（使用缓存避免重复验证）</li>
 *     <li>支持的编码器验证（使用缓存避免重复验证）</li>
 * </ul>
 * <h3>音频滤镜生成</h3>
 * <ul>
 *     <li>音量调整滤镜（支持多种精度）</li>
 *     <li>音频混合滤镜（支持多输入混音）</li>
 *     <li>音频循环滤镜（支持无限循环和指定次数）</li>
 *     <li>音频时长裁剪滤镜</li>
 *     <li>音频变速滤镜</li>
 *     <li>背景音乐添加滤镜</li>
 * </ul>
 * <h3>视频滤镜生成</h3>
 * <ul>
 *     <li>视频时长裁剪滤镜</li>
 *     <li>视频画面裁剪滤镜</li>
 *     <li>视频变速滤镜（通过调整时间戳）</li>
 *     <li>视频帧率滤镜</li>
 * </ul>
 * <h3>媒体处理操作</h3>
 * <ul>
 *     <li>应用滤镜（支持单输入和多输入）</li>
 *     <li>媒体裁剪（支持时长和时间范围）</li>
 *     <li>媒体转码</li>
 * </ul>
 * <h3>帧处理</h3>
 * <ul>
 *     <li>帧录制（从抓取器、滤镜、分离的视频/音频抓取器）</li>
 *     <li>录制器启动（支持多种配置方式）</li>
 *     <li>帧滤镜启动和配置</li>
 *     <li>抓取器状态判断</li>
 * </ul>
 * <h3>图像抓取</h3>
 * <ul>
 *     <li>指定时间点抓取单帧图像</li>
 *     <li>按固定间隔抓取关键帧图像（支持列表和回调两种方式）</li>
 * </ul>
 * <h3>其他</h3>
 * <ul>
 *     <li>FFmpeg 日志控制</li>
 *     <li>枚举类型：音频混合时长模式、音量滤镜精度</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.1.0
 */
public class FFmpegUtils {
	/**
	 * 支持的格式集合
	 * <p>使用并发集合缓存已验证支持的格式，避免重复验证</p>
	 *
	 * @since 1.1.0
	 */
	protected static final ConcurrentHashMap.KeySetView<String, Boolean> SUPPORTED_MUXER_FORMAT_SET = ConcurrentHashMap.newKeySet();

	/**
	 * 支持的编码器 ID 集合
	 * <p>使用并发集合缓存已验证支持的编码器 ID，避免重复验证</p>
	 *
	 * @since 1.1.0
	 */
	protected static final ConcurrentHashMap.KeySetView<Integer, Boolean> SUPPORTED_ENCODER_CODEC_ID_SET = ConcurrentHashMap.newKeySet();

	/**
	 * 私有构造函数，防止实例化
	 *
	 * @since 1.1.0
	 */
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
		Validate.isTrue(!duration.isNegative(), "duration 必须大于等于 0");

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
		Validate.notNull(precision, "precision 不可为 null");
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
		Validate.isTrue(!start.isNegative(), "start 必须大于等于 0");
		Validate.notNull(end, "end 不可为null");
		Validate.isTrue(!end.isZero() && !end.isNegative(), "end 必须大于 0");

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
	 * @throws IllegalArgumentException 当 startTimestamp 小于 0 或 endTimestamp 小于等于 0 或 endTimestamp 小于等于 startTimestamp 时
	 * @since 1.1.0
	 */
	public static String getAtrimFilter(final long startTimestamp, final long endTimestamp) {
		Validate.isTrue(startTimestamp >= 0, "startTimestamp 必须大于等于 0");
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
		Validate.isTrue(!start.isNegative(), "start 必须大于等于 0");
		Validate.notNull(end, "end 不可为null");
		Validate.isTrue(!end.isZero() && !end.isNegative(), "end 必须大于 0");

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
	 * @throws IllegalArgumentException 当 startTimestamp 小于 0 或 endTimestamp 小于等于 0 或 endTimestamp 小于等于 startTimestamp 时
	 * @since 1.1.0
	 */
	public static String getTrimFilter(final long startTimestamp, final long endTimestamp) {
		Validate.isTrue(startTimestamp >= 0, "startTimestamp 必须大于等于 0");
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
	 * @param outputOption    输出媒体配置
	 * @param audioFilters    音频滤镜字符串
	 * @param frameType       处理的帧类型
	 * @param recorderStarted 录制器是否已启动
	 * @throws IOException 当 I/O 错误发生时
	 * @since 1.1.0
	 */
	public static void applyAudioFilter(final FFmpegFrameGrabber grabber, final FFmpegFrameRecorder recorder,
	                                    final OutputOption outputOption, final String audioFilters,
	                                    final FrameType frameType, final boolean recorderStarted) throws IOException {
		applyFilter(grabber, recorder, outputOption, null, audioFilters, frameType, recorderStarted);
	}

	/**
	 * 仅应用视频滤镜
	 *
	 * @param grabber         帧抓取器
	 * @param recorder        帧录制器
	 * @param outputOption    输出媒体配置
	 * @param videoFilters    视频滤镜字符串
	 * @param frameType       处理的帧类型
	 * @param recorderStarted 录制器是否已启动
	 * @throws IOException 当 I/O 错误发生时
	 * @since 1.1.0
	 */
	public static void applyVideoFilter(final FFmpegFrameGrabber grabber, final FFmpegFrameRecorder recorder,
	                                    final OutputOption outputOption, final String videoFilters,
	                                    final FrameType frameType, final boolean recorderStarted) throws IOException {
		applyFilter(grabber, recorder, outputOption, videoFilters, null, frameType, recorderStarted);
	}

	/**
	 * 应用滤镜（单个输入源）
	 *
	 * @param grabber         帧抓取器
	 * @param recorder        帧录制器
	 * @param outputOption    输出媒体配置
	 * @param videoFilters    视频滤镜字符串
	 * @param audioFilters    音频滤镜字符串
	 * @param frameType       处理的帧类型
	 * @param recorderStarted 录制器是否已启动
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 grabber、recorder 或 frameType 为 null，或 videoFilters 和 audioFilters 同时为空时
	 * @since 1.1.0
	 */
	public static void applyFilter(final FFmpegFrameGrabber grabber, final FFmpegFrameRecorder recorder,
	                               final OutputOption outputOption, final String videoFilters, final String audioFilters,
	                               final FrameType frameType, final boolean recorderStarted) throws IOException {
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

		try (FFmpegFrameFilter filter = newFrameFilter(videoFilters, audioFilters, grabber, audioInputs, videoInputs)) {
			filter.start();

			if (!recorderStarted) {
				initRecorder(recorder, grabber, outputOption, frameType);
				recorder.start();
			}

			while (true) {
				try (Frame frame = frameType.grabFrame(grabber)) {
					if (Objects.isNull(frame)) {
						break;
					}
					filter.push(frame);
				}

				recordFrames(recorder, filter, frameType, false);
			}

			recordFrames(recorder, filter, frameType);
		}
	}

	/**
	 * 应用滤镜（多个输入源）
	 *
	 * @param grabbers        帧抓取器列表
	 * @param recorder        帧录制器
	 * @param videoFilters    视频滤镜字符串
	 * @param audioFilters    音频滤镜字符串
	 * @param frameType       处理的帧类型
	 * @param recorderStarted 录制器是否已启动
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 grabbers、frameType 或 recorder 为 null，
	 *                              或 grabbers 包含 null 元素，或 videoFilters 和 audioFilters 同时为空时
	 * @throws IllegalArgumentException 当 primaryInputIndex 无效时
	 * @since 1.1.0
	 */
	public static void applyFilter(final List<FFmpegFrameGrabber> grabbers, final FFmpegFrameRecorder recorder,
	                               final String videoFilters, final String audioFilters,
	                               final FrameType frameType, final boolean recorderStarted) throws IOException {
		applyFilter(grabbers, recorder, 0, null, videoFilters, audioFilters, frameType,
			recorderStarted);
	}

	/**
	 * 应用滤镜（多个输入源，指定输出配置）
	 *
	 * @param grabbers        帧抓取器列表
	 * @param recorder        帧录制器
	 * @param outputOption    输出媒体配置
	 * @param videoFilters    视频滤镜字符串
	 * @param audioFilters    音频滤镜字符串
	 * @param frameType       处理的帧类型
	 * @param recorderStarted 录制器是否已启动
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 grabbers、frameType 或 recorder 为 null，
	 *                                  或 grabbers 包含 null 元素，或 videoFilters 和 audioFilters 同时为空时
	 * @throws IllegalArgumentException 当 primaryInputIndex 无效时
	 * @since 1.1.0
	 */
	public static void applyFilter(final List<FFmpegFrameGrabber> grabbers, final FFmpegFrameRecorder recorder,
	                               final OutputOption outputOption, final String videoFilters, final String audioFilters,
	                               final FrameType frameType, final boolean recorderStarted) throws IOException {
		applyFilter(grabbers, recorder, 0, outputOption, videoFilters, audioFilters, frameType,
			recorderStarted);
	}

	/**
	 * 应用滤镜（多个输入源，指定主输入索引）
	 *
	 * @param grabbers          帧抓取器列表
	 * @param recorder          帧录制器
	 * @param primaryInputIndex 主输入索引（用于初始化录制器和获取媒体信息）
	 * @param videoFilters      视频滤镜字符串
	 * @param audioFilters      音频滤镜字符串
	 * @param frameType         处理的帧类型
	 * @param recorderStarted   录制器是否已启动
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 grabbers、frameType 或 recorder 为 null，
	 *                                  或 grabbers 包含 null 元素，或 videoFilters 和 audioFilters 同时为空时
	 * @throws IllegalArgumentException 当 primaryInputIndex 无效时
	 * @since 1.1.0
	 */
	public static void applyFilter(final List<FFmpegFrameGrabber> grabbers, final FFmpegFrameRecorder recorder,
	                               final int primaryInputIndex, final String videoFilters, final String audioFilters,
	                               final FrameType frameType, final boolean recorderStarted) throws IOException {
		applyFilter(grabbers, recorder, primaryInputIndex, null, videoFilters, audioFilters, frameType,
			recorderStarted);
	}

	/**
	 * 应用滤镜（多个输入源，完整参数）
	 * <p>
	 * 从多个输入源抓取帧，应用指定的音频和视频滤镜，然后录制到输出。
	 * 主输入索引用于初始化录制器和获取媒体信息配置滤镜。
	 * </p>
	 *
	 * @param grabbers          帧抓取器列表
	 * @param recorder          帧录制器
	 * @param primaryInputIndex 主输入索引（用于初始化录制器和获取媒体信息）
	 * @param outputOption      输出媒体配置（可为 null）
	 * @param videoFilters      视频滤镜字符串
	 * @param audioFilters      音频滤镜字符串
	 * @param frameType         处理的帧类型
	 * @param recorderStarted   录制器是否已启动
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 grabbers、frameType 或 recorder 为 null，
	 *                                  或 grabbers 包含 null 元素，或 videoFilters 和 audioFilters 同时为空时
	 * @throws IllegalArgumentException 当 primaryInputIndex 无效时
	 * @since 1.1.0
	 */
	public static void applyFilter(final List<FFmpegFrameGrabber> grabbers, final FFmpegFrameRecorder recorder,
	                               final int primaryInputIndex, final OutputOption outputOption,
	                               final String videoFilters, final String audioFilters,
	                               final FrameType frameType, final boolean recorderStarted) throws IOException {
		Validate.notEmpty(grabbers, "grabbers 不可为空");
		Validate.isTrue(grabbers.stream().allMatch(Objects::nonNull), "集合中存在为 null的 grabber");
		Validate.isTrue(primaryInputIndex >= 0 && primaryInputIndex < grabbers.size(), "primaryInputIndex 不是有效的索引");
		Validate.notNull(frameType, "frameMode 不可为 null");
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

		FFmpegFrameGrabber primaryGrabber = grabbers.get(primaryInputIndex);
		try (FFmpegFrameFilter filter = newFrameFilter(videoFilters, audioFilters, primaryGrabber, audioInputs, videoInputs)) {
			filter.start();

			if (!recorderStarted) {
				initRecorder(recorder, primaryGrabber, outputOption, frameType);
				recorder.start();
			}

			boolean hasFrame;
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

				recordFrames(recorder, filter, frameType, false);
			} while (hasFrame);

			recordFrames(recorder, filter, frameType);
		}
	}

	/**
	 * 裁剪媒体（从开头裁剪指定时长）
	 *
	 * @param grabber         帧抓取器
	 * @param recorder        帧录制器
	 * @param outputOption    输出媒体配置
	 * @param duration        裁剪时长
	 * @param frameType       处理的帧类型
	 * @param recorderStarted 录制器是否已启动
	 * @throws IOException 当 I/O 错误发生时
	 * @since 1.1.0
	 */
	public static void cut(final FFmpegFrameGrabber grabber, final FFmpegFrameRecorder recorder,
	                       final OutputOption outputOption, final Duration duration, final FrameType frameType,
	                       final boolean recorderStarted) throws IOException {
		cut(grabber, recorder, outputOption, 0, toTimestamp(duration), frameType, recorderStarted);
	}

	/**
	 * 裁剪媒体（指定开始和结束时间）
	 *
	 * @param grabber         帧抓取器
	 * @param recorder        帧录制器
	 * @param outputOption    输出媒体配置
	 * @param start           开始时间
	 * @param end             结束时间
	 * @param frameType       处理的帧类型
	 * @param recorderStarted 录制器是否已启动
	 * @throws IOException              当 I/O 错误发生时
	 * @throws NullPointerException     当 start 或 end 为 null 时
	 * @throws IllegalArgumentException 当 start 为负数，或 end 为 0/负数时
	 * @since 1.1.0
	 */
	public static void cut(final FFmpegFrameGrabber grabber, final FFmpegFrameRecorder recorder,
	                       final OutputOption outputOption, final Duration start, final Duration end,
	                       final FrameType frameType, final boolean recorderStarted) throws IOException {
		Validate.notNull(start, "start 不可为null");
		Validate.isTrue(!start.isNegative(), "start 必须大于等于 0");
		Validate.notNull(end, "end 不可为null");
		Validate.isTrue(!end.isZero() && !end.isNegative(), "end 必须大于 0");

		cut(grabber, recorder, outputOption, toTimestamp(start), toTimestamp(end),
			frameType, recorderStarted);
	}

	/**
	 * 裁剪媒体（指定开始和结束时间微秒）
	 *
	 * @param grabber         帧抓取器
	 * @param recorder        帧录制器
	 * @param outputOption    输出媒体配置
	 * @param startTimestamp  开始时间（微秒）
	 * @param endTimestamp    结束时间（微秒）
	 * @param frameType       处理的帧类型
	 * @param recorderStarted 录制器是否已启动
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 grabber 或 recorder 为 null 时
	 * @since 1.1.0
	 */
	public static void cut(final FFmpegFrameGrabber grabber, final FFmpegFrameRecorder recorder,
	                       final OutputOption outputOption, long startTimestamp, long endTimestamp,
	                       final FrameType frameType, final boolean recorderStarted) throws IOException {
		Validate.notNull(grabber, "resource 不可为 null");
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.isTrue(startTimestamp >= 0, "startTimestamp 必须大于等于 0");
		Validate.isTrue(endTimestamp > 0, "endTimestamp 必须大于 0");

		if (isNotStarted(grabber)) {
			grabber.start();
		}

		long lengthInTime = grabber.getLengthInTime();
		startTimestamp = Math.min(startTimestamp, lengthInTime);
		endTimestamp = Math.min(endTimestamp, lengthInTime);
		Validate.isTrue(endTimestamp > startTimestamp, "endTimestamp 必须大于 startTimestamp");

		if (!recorderStarted) {
			initRecorder(recorder, grabber, outputOption, frameType);
			recorder.start();
		}

		grabber.setTimestamp(startTimestamp);

		while (true) {
			try (Frame frame = frameType.grabFrame(grabber)) {
				long currentTimestamp = grabber.getTimestamp();

				if (currentTimestamp >= endTimestamp) {
					break;
				}

				if (currentTimestamp >= startTimestamp) {
					recorder.record(frame);
				}
			}
		}
		recorder.flush();
	}

	/**
	 * 转码媒体
	 * <p>
	 * 从输入媒体读取帧并直接写入输出，格式和编码由输出媒体配置决定
	 * </p>
	 *
	 * @param grabber         帧抓取器
	 * @param recorder        帧录制器
	 * @param outputOption    输出媒体配置
	 * @param frameType       处理的帧类型
	 * @param recorderStarted 录制器是否已启动
	 * @throws IOException          当 I/O 错误发生时
	 * @throws NullPointerException 当 grabber、recorder 或 frameType 为 null 时
	 * @since 1.1.0
	 */
	public static void transcode(final FFmpegFrameGrabber grabber, final FFmpegFrameRecorder recorder,
	                             final OutputOption outputOption, final FrameType frameType,
	                             final boolean recorderStarted) throws IOException {
		Validate.notNull(grabber, "grabber 不可为 null");
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.notNull(frameType, "frameMode 不可为 null");

		if (isNotStarted(grabber)) {
			grabber.start();
		}

		if (!recorderStarted) {
			initRecorder(recorder, grabber, outputOption, frameType);
			recorder.start();
		}
		recordFrames(recorder, grabber, frameType);
	}

	/**
	 * 新建帧滤镜
	 * <p>
	 * 创建并配置一个新的 FFmpeg 帧滤镜，设置音频和视频输入数量，
	 * 并从源帧抓取器获取媒体信息进行初始化。
	 * </p>
	 *
	 * @param videoFilters 视频滤镜字符串
	 * @param audioFilters 音频滤镜字符串
	 * @param grabber      源帧抓取器（用于获取媒体信息）
	 * @param audioInputs  音频输入数量
	 * @param videoInputs  视频输入数量
	 * @return 已配置的帧滤镜（未启动）
	 * @throws NullPointerException         当 grabber 为 null 时
	 * @throws FFmpegFrameGrabber.Exception 当抓取器操作失败时
	 * @throws IllegalArgumentException     当 videoFilters 和 audioFilters 同时为空，
	 *                                      或 audioInputs/videoInputs 为负数时
	 * @since 1.1.0
	 */
	public static FFmpegFrameFilter newFrameFilter(final String videoFilters, final String audioFilters,
	                                               final FFmpegFrameGrabber grabber, final int audioInputs,
												   final int videoInputs) throws FFmpegFrameGrabber.Exception {
		Validate.isTrue(!StringUtils.isAllBlank(audioFilters, videoFilters),
			"audioFilters 或 videoFilters 至少需要一个不为空");

		FFmpegFrameFilter filter = new FFmpegFrameFilter(videoFilters, audioFilters, 0, 0, 0);

		initFilter(filter, grabber, audioInputs, videoInputs);

		return filter;
	}

	/**
	 * 录制帧（从两个独立的视频和音频抓取器）
	 * <p>默认刷新录制器</p>
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
		recordFrames(recorder, videoGrabber, audioGrabber, true);
	}

	/**
	 * 录制帧（从两个独立的视频和音频抓取器）
	 *
	 * @param recorder      帧录制器
	 * @param videoGrabber  视频抓取器
	 * @param audioGrabber  音频抓取器
	 * @param flushRecorder 是否刷新录制器
	 * @throws FFmpegFrameRecorder.Exception 当录制失败时
	 * @throws FrameGrabber.Exception        当抓取失败时
	 * @throws NullPointerException          当 recorder、videoGrabber 或 audioGrabber 为 null 时
	 * @throws IllegalArgumentException      当 videoGrabber 无视频流或 audioGrabber 无音频流时
	 * @since 1.1.0
	 */
	public static void recordFrames(final FFmpegFrameRecorder recorder, final FFmpegFrameGrabber videoGrabber,
	                                final FFmpegFrameGrabber audioGrabber, final boolean flushRecorder) throws FFmpegFrameRecorder.Exception, FrameGrabber.Exception {
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

		if (flushRecorder) {
			recorder.flush();
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
		recordFrames(recorder, grabber, frameType, true);
	}

	/**
	 * 录制帧（从单个抓取器）
	 *
	 * @param recorder      帧录制器
	 * @param grabber       帧抓取器
	 * @param frameType     处理的帧类型
	 * @param flushRecorder 是否刷新录制器
	 * @throws FFmpegFrameRecorder.Exception 当录制失败时
	 * @throws FrameGrabber.Exception        当抓取失败时
	 * @throws NullPointerException          当 recorder、grabber 或 frameType 为 null 时
	 * @since 1.1.0
	 */
	public static void recordFrames(final FFmpegFrameRecorder recorder, final FFmpegFrameGrabber grabber,
	                                final FrameType frameType, final boolean flushRecorder) throws FFmpegFrameRecorder.Exception, FrameGrabber.Exception {
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

		if (flushRecorder) {
			recorder.flush();
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
		recordFrames(recorder, filter, frameType, true);
	}

	/**
	 * 录制帧（从帧滤镜）
	 *
	 * @param recorder      帧录制器
	 * @param filter        帧滤镜
	 * @param frameType     处理的帧类型
	 * @param flushRecorder 是否刷新录制器
	 * @throws FFmpegFrameRecorder.Exception 当录制失败时
	 * @throws FFmpegFrameFilter.Exception   当滤镜操作失败时
	 * @throws NullPointerException          当 recorder、filter 或 frameType 为 null 时
	 * @since 1.1.0
	 */
	public static void recordFrames(final FFmpegFrameRecorder recorder, final FFmpegFrameFilter filter,
	                                final FrameType frameType, final boolean flushRecorder) throws FFmpegFrameRecorder.Exception, FFmpegFrameFilter.Exception {
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

		if (flushRecorder) {
			recorder.flush();
		}
	}

	/**
	 * 初始化录制器（单个抓取器）
	 *
	 * @param recorder     帧录制器
	 * @param grabber      源帧抓取器
	 * @param outputOption 输出媒体配置
	 * @param frameType    处理的帧类型
	 * @throws FFmpegFrameGrabber.Exception 当抓取器操作失败时
	 * @since 1.1.0
	 */
	public static void initRecorder(final FFmpegFrameRecorder recorder, final FFmpegFrameGrabber grabber,
	                                final OutputOption outputOption, final FrameType frameType) throws FFmpegFrameGrabber.Exception {
		initRecorder(recorder, grabber, grabber, outputOption, frameType);
	}

	/**
	 * 初始化录制器（视频和音频抓取器分离）
	 *
	 * @param recorder     帧录制器
	 * @param videoGrabber 视频抓取器
	 * @param audioGrabber 音频抓取器
	 * @param outputOption 输出配置
	 * @param frameType    处理的帧类型
	 * @throws FFmpegFrameGrabber.Exception 当抓取器操作失败时
	 * @throws NullPointerException         当 recorder 为 null 时
	 * @throws IllegalArgumentException     当 outputOption 与 (videoGrabber 和 audioGrabber) 同时为 null 时
	 * @since 1.1.0
	 */
	public static void initRecorder(final FFmpegFrameRecorder recorder, final FFmpegFrameGrabber videoGrabber,
	                                final FFmpegFrameGrabber audioGrabber, final OutputOption outputOption,
	                                final FrameType frameType) throws FFmpegFrameGrabber.Exception {
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.isTrue(Objects.nonNull(outputOption) || ObjectUtils.allNotNull(videoGrabber, audioGrabber),
			"outputOption 或 （videoGrabber 和 audioGrabber） 不可同时为 null");

		if (Objects.nonNull(outputOption)) {
			outputOption.configure(recorder, frameType);
		} else {
			if (isNotStarted(audioGrabber)) {
				audioGrabber.start();
			}

			if (frameType != FrameType.VIDEO && audioGrabber.hasAudio()) {
				AudioOutputOption audioOutputOption = new AudioOutputOption(audioGrabber);
				audioOutputOption.configure(recorder, frameType);
			}

			if (isNotStarted(videoGrabber)) {
				videoGrabber.start();
			}

			if (frameType != FrameType.AUDIO && videoGrabber.hasVideo()) {
				VideoOutputOption videoOutputOption = new VideoOutputOption(videoGrabber);
				videoOutputOption.configure(recorder, frameType);
			}
		}
	}

	/**
	 * 初始化帧滤镜
	 * <p>
	 * 从源帧抓取器获取媒体信息并配置帧滤镜的音频和视频输入参数。
	 * </p>
	 *
	 * @param filter       帧滤镜
	 * @param grabber      源帧抓取器（用于获取媒体信息）
	 * @param audioInputs  音频输入数量
	 * @param videoInputs  视频输入数量
	 * @throws FFmpegFrameGrabber.Exception 当抓取器操作失败时
	 * @throws NullPointerException         当 filter 或 grabber 为 null 时
	 * @throws IllegalArgumentException     当 audioInputs/videoInputs 为负数时
	 * @since 1.1.0
	 */
	public static void initFilter(final FFmpegFrameFilter filter, final FFmpegFrameGrabber grabber,
	                              final int audioInputs, final int videoInputs) throws FFmpegFrameGrabber.Exception {
		Validate.notNull(filter, "filter 不可为 null");
		Validate.notNull(grabber, "grabber 不可为 null");
		Validate.isTrue(audioInputs >= 0, "audioInputs 必须大于等于0");
		Validate.isTrue(videoInputs >= 0, "videoInputs 必须大于等于0");

		filter.setAudioInputs(audioInputs);
		filter.setVideoInputs(videoInputs);

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
			while (currentTimestamp <= endTimestamp) {
				grabber.setTimestamp(currentTimestamp);

				try (Frame frame = grabber.grabKeyFrame()) {
					if (Objects.nonNull(frame) && !Objects.isNull(frame.image)) {
						BufferedImage image = converter.convert(frame);
						images.add(image);
					}

					if (currentTimestamp == endTimestamp) {
						break;
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
			while (currentTimestamp <= endTimestamp) {
				grabber.setTimestamp(currentTimestamp);

				try (Frame frame = grabber.grabKeyFrame()) {
					if (Objects.nonNull(frame) && !Objects.isNull(frame.image)) {
						BufferedImage image = converter.convert(frame);
						consumer.accept(image, currentTimestamp);
						image.flush();
					}

					if (currentTimestamp == endTimestamp) {
						break;
					}
					currentTimestamp = Math.min(currentTimestamp + intervalMicros, endTimestamp);
				}
			}
		}
	}

	/**
	 * 解析格式字符串
	 * <p>
	 * 从逗号分隔的格式字符串中提取格式。
	 * 如果包含 webm 或 mp4，优先返回这些格式；否则返回第一个格式。
	 * 例如 "mp4,h264" 返回 "mp4"，"webm,vp9" 返回 "webm"，"mp4" 返回 "mp4"
	 * </p>
	 *
	 * @param format 格式字符串，可为 null 或空
	 * @return 解析后的格式，如果输入为 null、空或无效则返回 null
	 * @since 1.1.0
	 */
	public static String parseFormat(String format) {
		return parseFormat(format, null);
	}

	/**
	 * 解析格式字符串（带默认值）
	 * <p>
	 * 从逗号分隔的格式字符串中提取格式。
	 * 如果包含 webm 或 mp4，优先返回这些格式；否则返回第一个格式。
	 * 如果输入为 null、空或无效，返回指定的默认值。
	 * </p>
	 *
	 * @param format        格式字符串，可为 null 或空
	 * @param defaultFormat 默认格式，当解析失败时返回
	 * @return 解析后的格式，如果输入为 null、空或无效则返回 defaultFormat
	 * @since 1.1.0
	 */
	public static String parseFormat(String format, String defaultFormat) {
		String[] formats = StringUtils.split(format, ",");
		if (ArrayUtils.isEmpty(formats)) {
			return defaultFormat;
		} else if (formats.length == 1) {
			return ArrayUtils.get(formats, 0, defaultFormat);
		}

		if (ArrayUtils.contains(formats, FFmpegConstants.VIDEO_WEBM_FORMAT)) {
			return FFmpegConstants.VIDEO_WEBM_FORMAT;
		} else if (ArrayUtils.contains(formats, FFmpegConstants.VIDEO_MP4_FORMAT)) {
			return FFmpegConstants.VIDEO_MP4_FORMAT;
		}
		return ArrayUtils.get(formats, 0, defaultFormat);
	}

	/**
	 * 检查格式是否为支持的输出格式（封装器）
	 * <p>
	 * 使用 FFmpeg 的 av_guess_format 函数验证格式是否支持。
	 * 支持的格式会被缓存，避免重复验证。
	 * </p>
	 *
	 * @param format 格式名称，不可为空
	 * @return 如果格式支持返回 true，否则返回 false
	 * @throws IllegalArgumentException 当 format 为空或空白时
	 * @since 1.1.0
	 */
	public static boolean isSupportedMuxer(String format) {
		Validate.notBlank(format, "format 不可为空");

		if (SUPPORTED_MUXER_FORMAT_SET.contains(format)) {
			return true;
		}

		try (AVOutputFormat outputFormat = avformat.av_guess_format(format, null, null)) {
			if (Objects.nonNull(outputFormat) && !outputFormat.isNull()) {
				SUPPORTED_MUXER_FORMAT_SET.add(format);

				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * 检查编码器 ID 是否为支持的编码器
	 * <p>
	 * 使用 FFmpeg 的 avcodec_find_encoder 函数验证编码器是否支持。
	 * 支持的编码器 ID 会被缓存，避免重复验证。
	 * </p>
	 *
	 * @param codecId 编码器 ID
	 * @return 如果编码器支持返回 true，否则返回 false
	 * @since 1.1.0
	 */
	public static boolean isSupportedEncoder(int codecId) {
		if (codecId <= avcodec.AV_CODEC_ID_NONE) {
			return false;
		}

		if (SUPPORTED_ENCODER_CODEC_ID_SET.contains(codecId)) {
			return true;
		}

		try (AVCodec codec = avcodec.avcodec_find_encoder(codecId)) {
			if (Objects.nonNull(codec) && !codec.isNull()) {
				SUPPORTED_ENCODER_CODEC_ID_SET.add(codec.id());

				return true;
			} else {
				return false;
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