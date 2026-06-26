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
import io.github.pangju666.commons.media.model.MediaResource;
import io.github.pangju666.commons.media.model.Video;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Collection;

/*
	一、优先必加（生产环境强烈建议补上，解决现有短板）
	1. 裁剪视频（截取片段）
	业务场景：短视频剪辑、去掉片头片尾、分段导出
	需求：指定开始时间 + 时长 / 结束时间，截取片段输出。
	核心思路：利用 setTimestamp 跳转起始帧，控制读取帧数 / 时长停止。
	2. 去除音频 / 保留音频 开关
	现有代码默认保留原音频，很多场景需要静音视频。
	3. 异常细化 + 日志 + 文件校验
	现有只简单打印异常，生产需要：
	提前判断源文件是否存在、输出目录是否创建
	区分「文件不存在」「格式不支持」「读写失败」等异常
	替换 e.printStackTrace() 为日志框架
	4. 水印多位置选择（左上 / 右上 / 左下 / 右下 / 居中）
	目前固定右下角，灵活性不足。
	二、进阶实用功能（通用业务高频）
	1. 视频合并（拼接多个视频）
	场景：多段视频合成一个、轮播视频。
	前提：建议待合并视频分辨率 / 帧率 / 编码统一，避免花屏。
	2. 批量处理（批量转码 / 加水印）
	遍历文件夹下所有视频，统一处理，不用逐个调用。
	3. 生成缩略图组（多张预览图）
	场景：视频预览、素材列表，每隔 N 秒截一张图。
	4. 旋转视频（90°/180°/270°）
	解决手机竖拍视频方向颠倒问题。
	三、场景化增值功能（按需选用）
	1. 动态水印（滚动文字）
	场景：防盗录、版权播报。
	2. 帧率调整（降帧压缩体积）
	如 30 帧 → 24/15 帧，进一步减小文件。
	3. 抽帧压缩（间隔取帧）
	极致压缩，适合预览视频。
	4. 单独提取音频
	把视频中的音频导出为 mp3/wav。
*/

/**
 *
 * @since 1.1.0
 */
public class VideoUtils {
	/**
	 * 受保护的构造函数，防止实例化
	 *
	 * @since 1.1.0
	 */
	protected VideoUtils() {
	}

	public static void transcode(final MediaResource resource, final File outputFile, final Video outputVideo) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		FFmpegUtils.transcode(resource, outputFile, outputVideo, false);
	}

	public static void transcode(final MediaResource resource, final OutputStream outputStream, final Video outputVideo) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		FFmpegUtils.transcode(resource, outputStream, outputVideo, false);
	}

	public static void cut(final MediaResource resource, final File outputFile, final Duration duration) throws IOException {
		Validate.notNull(duration, "duration 不可为 null");
		cut(resource, outputFile, null, Duration.ZERO, duration);
	}

	public static void cut(final MediaResource resource, final OutputStream outputStream, final Duration duration) throws IOException {
		Validate.notNull(duration, "duration 不可为 null");
		cut(resource, outputStream, null, Duration.ZERO, duration);
	}

	public static void cut(final MediaResource resource, final File outputFile, final Duration start, final Duration end) throws IOException {
		cut(resource, outputFile, null, start, end);
	}

	public static void cut(final MediaResource resource, final OutputStream outputStream, final Duration start,
	                       final Duration end) throws IOException {
		cut(resource, outputStream, null, start, end);
	}

	public static void cut(final MediaResource resource, final File outputFile, final Video outputVideo,
	                       final Duration duration) throws IOException {
		Validate.notNull(duration, "duration 不可为 null");
		cut(resource, outputFile, outputVideo, Duration.ZERO, duration);
	}

	public static void cut(final MediaResource resource, final OutputStream outputStream, final Video outputVideo,
	                       final Duration duration) throws IOException {
		Validate.notNull(duration, "duration 不可为 null");
		cut(resource, outputStream, outputVideo, Duration.ZERO, duration);
	}

	public static void cut(final MediaResource resource, final File outputFile, final Video outputVideo,
	                       final Duration start, final Duration end) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		FFmpegUtils.cut(resource, outputFile, outputVideo, start, end, false);
	}

	public static void cut(final MediaResource resource, final OutputStream outputStream, final Video outputVideo,
	                       final Duration start, final Duration end) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		FFmpegUtils.cut(resource, outputStream, outputVideo, start, end, false);
	}

	public static void concat(final Collection<MediaResource> resources, final File outputFile, final Video outputVideo) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		FFmpegUtils.concat(resources, outputFile, outputVideo, false);
	}

	public static void concat(final Collection<MediaResource> resources, final OutputStream outputStream, final Video outputVideo) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		FFmpegUtils.concat(resources, outputStream, outputVideo, false);
	}

	public static void applyFilter(final Collection<MediaResource> resources, final File outputFile,
	                               final String videoFilters, final String audioFilters) throws IOException {
		applyFilter(resources, outputFile, videoFilters, audioFilters, null);
	}

	public static void applyFilter(final Collection<MediaResource> resources, final OutputStream outputStream,
	                               final String videoFilters, final String audioFilters) throws IOException {
		applyFilter(resources, outputStream, videoFilters, audioFilters, null);
	}

	public static void applyFilter(final Collection<MediaResource> resources, final File outputFile, final String videoFilters,
	                               final String audioFilters, final Video outputVideo) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		FFmpegUtils.applyFilter(resources, outputFile, outputVideo, videoFilters, audioFilters, false);
	}

	public static void applyFilter(final Collection<MediaResource> resources, final OutputStream outputStream,
	                               final String videoFilters, final String audioFilters, final Video outputVideo) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		FFmpegUtils.applyFilter(resources, outputStream, outputVideo, videoFilters, audioFilters, false);
	}
}