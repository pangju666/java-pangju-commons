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

package io.github.pangju666.commons.ffmpeg.builder;

import io.github.pangju666.commons.ffmpeg.enums.VideoPreset;
import io.github.pangju666.commons.ffmpeg.lang.FFmpegConstants;
import io.github.pangju666.commons.ffmpeg.model.AudioOutputOption;
import io.github.pangju666.commons.ffmpeg.model.Video;
import io.github.pangju666.commons.ffmpeg.model.VideoOutputOption;
import io.github.pangju666.commons.io.exception.UnsupportedResourceException;
import org.apache.commons.lang3.Validate;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.IOException;

/**
 * 视频输出选项构建器
 * <p>
 * 提供流式 API 用于构建 {@link VideoOutputOption} 实例，支持链式调用。
 * </p>
 * <h3>核心特性</h3>
 * <ul>
 *     <li>支持从格式和分辨率、Video 对象或 FFmpegFrameGrabber 创建构建器</li>
 *     <li>提供预定义的视频格式和编码器组合工厂方法</li>
 *     <li>支持智能缩放（按宽度、按高度、按比例、按缩放因子）</li>
 *     <li>支持设置帧率、比特率等视频参数</li>
 *     <li>支持嵌入音频输出选项</li>
 * </ul>
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 使用预定义格式和编码器
 * VideoOutputOption option = VideoOutputOptionBuilder.mp4WithH264(VideoPreset.FHD_1080P)
 *     .frameRate(30)
 *     .bitrate(5000000)
 *     .audio(AudioOutputOptionBuilder.aac().bitrate(128000).build())
 *     .build();
 *
 * // 自定义格式和分辨率
 * VideoOutputOption option = new VideoOutputOptionBuilder("mp4", 1920, 1080)
 *     .codecId(avcodec.AV_CODEC_ID_H264)
 *     .frameRate(60)
 *     .bitrate(8000000)
 *     .build();
 *
 * // 使用智能缩放
 * VideoOutputOption option = new VideoOutputOptionBuilder("mkv", 3840, 2160)
 *     .scaleByWidth(1920)
 *     .frameRate(30)
 *     .build();
 *
 * // 从 Video 对象创建
 * VideoOutputOption option = new VideoOutputOptionBuilder(video)
 *     .scale(0.5)
 *     .bitrate(3000000)
 *     .build();
 *
 * // 添加元数据
 * VideoOutputOption option = VideoOutputOptionBuilder.mp4WithH264(VideoPreset.FHD_1080P)
 *     .addMetadata("title", "My Video")
 *     .addMetadata("author", "pangju666")
 *     .build();
 * }</pre>
 *
 * @author pangju666
 * @see VideoOutputOption
 * @since 1.1.0
 */
public class VideoOutputOptionBuilder extends OutputOptionBuilder<VideoOutputOptionBuilder, VideoOutputOption> {
	/**
	 * 原始视频宽度，用于缩放计算
	 *
	 * @since 1.1.0
	 */
	protected int originalImageWidth;

	/**
	 * 原始视频高度，用于缩放计算
	 *
	 * @since 1.1.0
	 */
	protected int originalImageHeight;

	/**
	 * 根据视频格式和分辨率创建构建器
	 *
	 * @param format      视频格式
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @throws IllegalArgumentException     当 format 为空时抛出
	 * @throws UnsupportedResourceException 当格式不被支持时抛出
	 * @since 1.1.0
	 */
	public VideoOutputOptionBuilder(String format, int imageWidth, int imageHeight) {
		super(new VideoOutputOption(format, imageWidth, imageHeight));
		this.originalImageWidth = outputOption.getImageWidth();
		this.originalImageHeight = outputOption.getImageHeight();
	}

	/**
	 * 从 Video 对象创建构建器
	 *
	 * @param video 视频信息对象
	 * @since 1.1.0
	 */
	public VideoOutputOptionBuilder(Video video) {
		super(new VideoOutputOption(video));
	}

	/**
	 * 从 FFmpeg 帧抓取器创建构建器
	 *
	 * @param grabber FFmpeg 帧抓取器
	 * @throws IOException 读取视频信息时发生 I/O 错误
	 * @since 1.1.0
	 */
	public VideoOutputOptionBuilder(FFmpegFrameGrabber grabber) throws IOException {
		super(new VideoOutputOption(grabber));
	}

	/**
	 * 创建 MP4 格式 H264 编码视频输出选项构建器
	 *
	 * @param preset 视频预设
	 * @return MP4 H264 格式构建器
	 * @since 1.1.0
	 */
	public static VideoOutputOptionBuilder mp4WithH264(VideoPreset preset) {
		return new VideoOutputOptionBuilder(FFmpegConstants.VIDEO_MP4_FORMAT, preset.width, preset.height)
			.codecId(avcodec.AV_CODEC_ID_H264);
	}

	/**
	 * 创建 MP4 格式 H265 编码视频输出选项构建器
	 *
	 * @param preset 视频预设
	 * @return MP4 H265 格式构建器
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOptionBuilder mp4WithH265(VideoPreset preset) {
		return new VideoOutputOptionBuilder(FFmpegConstants.VIDEO_MP4_FORMAT, preset.width, preset.height)
			.codecId(avcodec.AV_CODEC_ID_H265);
	}

	/**
	 * 创建 WebM 格式 VP9 编码视频输出选项构建器
	 *
	 * @param preset 视频预设
	 * @return WebM VP9 格式构建器
	 * @since 1.1.0
	 */
	public static VideoOutputOptionBuilder webmWithVP9(VideoPreset preset) {
		return new VideoOutputOptionBuilder(FFmpegConstants.VIDEO_WEBM_FORMAT, preset.width, preset.height)
			.codecId(avcodec.AV_CODEC_ID_VP9);
	}

	/**
	 * 创建 MKV 格式 H264 编码视频输出选项构建器
	 *
	 * @param preset 视频预设
	 * @return MKV H264 格式构建器
	 * @since 1.1.0
	 */
	public static VideoOutputOptionBuilder mkvWithH264(VideoPreset preset) {
		return new VideoOutputOptionBuilder(FFmpegConstants.VIDEO_MKV_FORMAT, preset.width, preset.height)
			.codecId(avcodec.AV_CODEC_ID_H264);
	}

	/**
	 * 创建 MKV 格式 H265 编码视频输出选项构建器
	 *
	 * @param preset 视频预设
	 * @return MKV H265 格式构建器
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOptionBuilder mkvWithH265(VideoPreset preset) {
		return new VideoOutputOptionBuilder(FFmpegConstants.VIDEO_MKV_FORMAT, preset.width, preset.height)
			.codecId(avcodec.AV_CODEC_ID_H265);
	}

	/**
	 * 创建 MP4 格式 H264 编码视频输出选项构建器
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @return MP4 H264 格式构建器
	 * @since 1.1.0
	 */
	public static VideoOutputOptionBuilder mp4WithH264(int imageWidth, int imageHeight) {
		return new VideoOutputOptionBuilder(FFmpegConstants.VIDEO_MP4_FORMAT, imageWidth, imageHeight)
			.codecId(avcodec.AV_CODEC_ID_H264);
	}

	/**
	 * 创建 MP4 格式 H265 编码视频输出选项构建器
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @return MP4 H265 格式构建器
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOptionBuilder mp4WithH265(int imageWidth, int imageHeight) {
		return new VideoOutputOptionBuilder(FFmpegConstants.VIDEO_MP4_FORMAT, imageWidth, imageHeight)
			.codecId(avcodec.AV_CODEC_ID_H265);
	}

	/**
	 * 创建 WebM 格式 VP9 编码视频输出选项构建器
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @return WebM VP9 格式构建器
	 * @since 1.1.0
	 */
	public static VideoOutputOptionBuilder webmWithVP9(int imageWidth, int imageHeight) {
		return new VideoOutputOptionBuilder(FFmpegConstants.VIDEO_WEBM_FORMAT, imageWidth, imageHeight)
			.codecId(avcodec.AV_CODEC_ID_VP9);
	}

	/**
	 * 创建 MKV 格式 H264 编码视频输出选项构建器
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @return MKV H264 格式构建器
	 * @since 1.1.0
	 */
	public static VideoOutputOptionBuilder mkvWithH264(int imageWidth, int imageHeight) {
		return new VideoOutputOptionBuilder(FFmpegConstants.VIDEO_MKV_FORMAT, imageWidth, imageHeight)
			.codecId(avcodec.AV_CODEC_ID_H264);
	}

	/**
	 * 创建 MKV 格式 H265 编码视频输出选项构建器
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @return MKV H265 格式构建器
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOptionBuilder mkvWithH265(int imageWidth, int imageHeight) {
		return new VideoOutputOptionBuilder(FFmpegConstants.VIDEO_MKV_FORMAT, imageWidth, imageHeight)
			.codecId(avcodec.AV_CODEC_ID_H265);
	}

	/**
	 * 按目标宽度缩放视频
	 * <p>根据原始宽高比自动计算高度，保持宽高比</p>
	 *
	 * @param targetWidth 目标宽度（像素）
	 * @return 构建器自身，用于链式调用
	 * @throws IllegalArgumentException 当 targetWidth 小于等于 0 时抛出
	 * @since 1.1.0
	 */
	public VideoOutputOptionBuilder scaleByWidth(int targetWidth) {
		Validate.isTrue(targetWidth > 0, "targetWidth 必须大于0");

		if (originalImageWidth > originalImageHeight) {
			double ratio = (double) originalImageWidth / originalImageHeight;

			this.outputOption.setImageWidth(targetWidth);
			this.outputOption.setImageHeight(Math.max((int) Math.round(targetWidth / ratio), 1));
		} else {
			double ratio = (double) originalImageWidth / originalImageHeight;

			this.outputOption.setImageWidth(targetWidth);
			this.outputOption.setImageHeight(Math.max((int) Math.round(targetWidth * ratio), 1));
		}

		return this;
	}

	/**
	 * 按目标高度缩放视频
	 * <p>根据原始宽高比自动计算宽度，保持宽高比</p>
	 *
	 * @param targetHeight 目标高度（像素）
	 * @return 构建器自身，用于链式调用
	 * @throws IllegalArgumentException 当 targetHeight 小于等于 0 时抛出
	 * @since 1.1.0
	 */
	public VideoOutputOptionBuilder scaleByHeight(int targetHeight) {
		Validate.isTrue(targetHeight > 0, "targetHeight 必须大于0");

		if (originalImageWidth > originalImageHeight) {
			double ratio = (double) originalImageWidth / originalImageHeight;

			this.outputOption.setImageWidth(Math.max((int) Math.round(targetHeight * ratio), 1));
			this.outputOption.setImageHeight(targetHeight);
		} else {
			double ratio = (double) originalImageWidth / originalImageHeight;

			this.outputOption.setImageWidth(Math.max((int) Math.round(targetHeight / ratio), 1));
			this.outputOption.setImageHeight(targetHeight);
		}

		return this;
	}

	/**
	 * 按目标分辨率缩放视频
	 * <p>在保持宽高比的前提下，选择不超过目标分辨率的最大尺寸</p>
	 *
	 * @param targetWidth  目标宽度（像素）
	 * @param targetHeight 目标高度（像素）
	 * @return 构建器自身，用于链式调用
	 * @throws IllegalArgumentException 当参数小于等于 0 时抛出
	 * @since 1.1.0
	 */
	public VideoOutputOptionBuilder scale(int targetWidth, int targetHeight) {
		Validate.isTrue(targetWidth > 0, "targetWidth 必须大于0");
		Validate.isTrue(targetHeight > 0, "targetHeight 必须大于0");

		double ratio = (double) originalImageWidth / originalImageHeight;
		int heightByWidth = Math.max((int) Math.round(targetWidth / ratio), 1);
		if (heightByWidth <= targetHeight) {
			this.outputOption.setImageWidth(targetWidth);
			this.outputOption.setImageHeight(heightByWidth);
		} else {
			this.outputOption.setImageWidth(Math.max((int) Math.round(targetHeight * ratio), 1));
			this.outputOption.setImageHeight(targetHeight);
		}

		return this;
	}

	/**
	 * 按缩放因子缩放视频
	 * <p>根据原始分辨率乘以缩放因子计算新分辨率</p>
	 *
	 * @param scalingFactor 缩放因子，必须大于 0
	 * @return 构建器自身，用于链式调用
	 * @throws IllegalArgumentException 当 scalingFactor 小于等于 0 时抛出
	 * @since 1.1.0
	 */
	public VideoOutputOptionBuilder scale(double scalingFactor) {
		Validate.isTrue(scalingFactor > 0, "scale 必须大于0");

		this.outputOption.setImageWidth((int) Math.round(originalImageWidth * scalingFactor));
		this.outputOption.setImageHeight((int) Math.round(originalImageHeight * scalingFactor));

		return this;
	}

	/**
	 * 设置帧率
	 *
	 * @param frameRate 帧率（fps）
	 * @return 构建器自身，用于链式调用
	 * @since 1.1.0
	 */
	public VideoOutputOptionBuilder frameRate(double frameRate) {
		this.outputOption.setFrameRate(frameRate);

		return this;
	}

	/**
	 * 设置比特率
	 *
	 * @param bitrate 比特率（bps）
	 * @return 构建器自身，用于链式调用
	 * @since 1.1.0
	 */
	public VideoOutputOptionBuilder bitrate(int bitrate) {
		this.outputOption.setBitrate(bitrate);

		return this;
	}

	/**
	 * 设置分辨率
	 * <p>同时更新原始分辨率，用于后续缩放计算</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @return 构建器自身，用于链式调用
	 * @since 1.1.0
	 */
	public VideoOutputOptionBuilder resolution(int imageWidth, int imageHeight) {
		this.outputOption.setImageWidth(imageWidth);
		this.outputOption.setImageHeight(imageHeight);

		this.originalImageWidth = imageWidth;
		this.originalImageHeight = imageHeight;

		return this;
	}

	/**
	 * 设置音频轨道配置
	 *
	 * @param audio 音频输出选项
	 * @return 构建器自身，用于链式调用
	 * @since 1.1.0
	 */
	public VideoOutputOptionBuilder audio(AudioOutputOption audio) {
		this.outputOption.setAudio(audio);

		return this;
	}
}
