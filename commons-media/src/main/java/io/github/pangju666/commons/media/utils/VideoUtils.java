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
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Objects;

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
	public static void transcode(MediaResource resource, File outputFile, Video outputVideo) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputVideo, "outputVideo 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 MediaResource");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doTranscode(resource, recorder, outputVideo);
		}
	}

	public static void transcode(MediaResource resource, OutputStream outputStream, Video outputVideo) throws IOException {
		Validate.notNull(outputVideo, "outputVideo 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 MediaResource");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doTranscode(resource, recorder, outputVideo);
		}
	}

	protected static void doCut(MediaResource resource, OutputStream outputStream, Video outputVideo, Duration start, Duration end) throws IOException {
		try (FFmpegFrameGrabber grabber = (resource.isFile() ? new FFmpegFrameGrabber(resource.getFile()) :
			new FFmpegFrameGrabber(resource.getInputStream()))) {
			long startTimestamp = start.toNanos() / 1000;
			long endTimestamp = Objects.isNull(end) ? grabber.getLengthInTime() : Math.min(end.toNanos() / 1000, grabber.getLengthInTime());

			Validate.isTrue(startTimestamp < grabber.getLengthInTime(), "start 必须小于音频总时长");

			grabber.setTimestamp(startTimestamp);
			grabber.start();

			try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0, 0, 0)) {
				initRecorder(recorder, grabber, outputVideo);
				recordFrames(recorder, grabber, endTimestamp);
			}
		}
	}

	protected static void doTranscode(MediaResource resource, FFmpegFrameRecorder recorder, Video outputVideo) throws IOException {
		try (FFmpegFrameGrabber grabber = (resource.isFile() ? new FFmpegFrameGrabber(resource.getFile()) :
			new FFmpegFrameGrabber(resource.getInputStream()))) {
			grabber.start();

			initRecorder(recorder, grabber, outputVideo);
			recordFrames(recorder, grabber, null);
		}
	}

	protected static void recordFrames(FFmpegFrameRecorder recorder, FFmpegFrameGrabber grabber, Long endTimestamp)
		throws FFmpegFrameRecorder.Exception, FrameGrabber.Exception {
		if (Objects.nonNull(endTimestamp) && grabber.getTimestamp() > endTimestamp) {
			return;
		}

		while (true) {
			try (Frame frame = grabber.grabFrame()) {
				if (Objects.isNull(frame)) {
					break;
				}
				recorder.record(frame);
			}
		}
	}

	protected static void initRecorder(FFmpegFrameRecorder recorder, FFmpegFrameGrabber grabber, Video outputVideo) throws FFmpegFrameRecorder.Exception {
		if (Objects.nonNull(outputVideo)) {
			outputVideo.initRecoder(recorder);
		} else {
			recorder.setFormat(grabber.getFormat());

			if (grabber.hasVideo()) {
				recorder.setVideoCodec(grabber.getVideoCodec());
				recorder.setVideoCodecName(grabber.getVideoCodecName());
				recorder.setFrameRate(grabber.getFrameRate());
				recorder.setVideoBitrate(grabber.getVideoBitrate());
				recorder.setImageWidth(grabber.getImageWidth());
				recorder.setImageHeight(grabber.getImageHeight());
				recorder.setVideoMetadata(grabber.getVideoMetadata());
			}

			if (grabber.hasAudio()) {
				recorder.setAudioCodec(grabber.getAudioCodec());
				recorder.setAudioCodecName(grabber.getAudioCodecName());
				recorder.setSampleRate(grabber.getSampleRate());
				recorder.setAudioBitrate(grabber.getAudioBitrate());
				recorder.setAudioChannels(grabber.getAudioChannels());
				recorder.setAudioMetadata(grabber.getAudioMetadata());
			}
		}

		recorder.start();
	}
}