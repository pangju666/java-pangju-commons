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

package io.github.pangju666.commons.ffmpeg.model;

import io.github.pangju666.commons.ffmpeg.builder.VideoOutputOptionBuilder;
import io.github.pangju666.commons.ffmpeg.enums.FrameType;
import io.github.pangju666.commons.ffmpeg.enums.VideoPreset;
import io.github.pangju666.commons.ffmpeg.lang.FFmpegConstants;
import io.github.pangju666.commons.ffmpeg.utils.FFmpegUtils;
import io.github.pangju666.commons.io.exception.UnsupportedResourceException;
import org.apache.commons.lang3.Validate;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;

import java.util.Map;
import java.util.Objects;

/**
 * 视频输出选项类，封装视频文件的输出配置
 * <p>
 * 该类继承自 {@link OutputOption}，专门用于配置视频文件的输出参数，
 * 包含视频特有属性如帧率、分辨率、码率等，以及内置音频轨道配置。
 * </p>
 * <h3>核心特性</h3>
 * <ul>
 *   <li>基于 JavaCV/FFmpeg 自动验证编码器支持</li>
 *   <li>提供预定义标准视频配置静态方法（MP4、WEBM、MKV 多种分辨率和码率等级）</li>
 *   <li>支持从 Video 对象解析配置</li>
 *   <li>支持从 FFmpegFrameGrabber 解析配置</li>
 *   <li>内置音频轨道配置支持</li>
 *   <li>提供录制器配置方法</li>
 * </ul>
 * <h3>视频特有属性</h3>
 * <ul>
 *   <li>{@link #frameRate} - 帧率（fps），如 24、30、60</li>
 *   <li>{@link #imageWidth} - 视频宽度（像素），如 1920</li>
 *   <li>{@link #imageHeight} - 视频高度（像素），如 1080</li>
 *   <li>{@link #bitrate} - 视频码率（bps），如 6000000（6Mbps）</li>
 *   <li>{@link #pixelFormat} - 像素格式，如 AV_PIX_FMT_YUV420P</li>
 *   <li>{@link #audio} - 音频轨道配置</li>
 * </ul>
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 从 Video 对象解析配置
 * VideoOutputOption option = new VideoOutputOption(video);
 *
 * // 从 FFmpegFrameGrabber 解析配置
 * VideoOutputOption option = new VideoOutputOption(grabber);
 *
 * // 使用预定义配置（H264 + AAC）
 * VideoOutputOption option = VideoOutputOption.mp4WithH264(VideoPreset.FHD_1080P);
 *
 * // 使用预定义配置（H265 + Opus）
 * VideoOutputOption option = VideoOutputOption.mkvWithH265(VideoPreset.UHD_4K);
 * }</pre>
 *
 * @author pangju666
 * @apiNote 无损编码设置码率无效，一般情况下不要设置像素格式
 * @see OutputOption
 * @see AudioOutputOption
 * @see Video
 * @see VideoPreset
 * @see VideoOutputOptionBuilder
 * @since 1.1.0
 */
public class VideoOutputOption extends OutputOption {
	/**
	 * 帧率（fps），如 24、30、60
	 * <p>常见帧率：24fps（电影）、30fps（标准视频）、60fps（高帧率视频）</p>
	 *
	 * @since 1.1.0
	 */
	protected double frameRate = FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE;

	/**
	 * 视频宽度（像素）
	 *
	 * @since 1.1.0
	 */
	protected int imageWidth;

	/**
	 * 视频高度（像素）
	 *
	 * @since 1.1.0
	 */
	protected int imageHeight;

	/**
	 * 视频码率（bps）
	 * <p>码率越高画质越好、体积越大，需根据编码格式和用途合理设置</p>
	 *
	 * @since 1.1.0
	 */
	protected int bitrate = 400000;

	/**
	 * 像素格式
	 * <p>表示视频像素的数据格式，如 AV_PIX_FMT_YUV420P（YUV420P）</p>
	 *
	 * @since 1.1.0
	 */
	protected int pixelFormat = avutil.AV_PIX_FMT_NONE;

	/**
	 * 音频轨道配置
	 * <p>如果视频包含音频，此字段保存音频输出配置</p>
	 *
	 * @since 1.1.0
	 */
	protected AudioOutputOption audio;

	/**
	 * 根据格式和分辨率构造视频输出选项
	 *
	 * @param format      视频格式
	 * @param imageWidth  视频宽度（像素），必须大于 0
	 * @param imageHeight 视频高度（像素），必须大于 0
	 * @throws IllegalArgumentException     当分辨率参数无效或 format 为空时抛出
	 * @throws UnsupportedResourceException 当格式不被支持时抛出
	 * @since 1.1.0
	 */
	public VideoOutputOption(String format, int imageWidth, int imageHeight) {
		super(format);

		Validate.isTrue(imageWidth > 0, "imageWidth 必须大于0");
		Validate.isTrue(imageHeight > 0, "imageHeight 必须大于0");

		setImageWidth(imageWidth);
		setImageHeight(imageHeight);
	}

	/**
	 * 从 Video 对象构造视频输出选项
	 *
	 * @param video 视频信息对象，不可为 null
	 * @throws IllegalArgumentException 当 video 为 null 时抛出
	 * @since 1.1.0
	 */
	public VideoOutputOption(Video video) {
		super();

		Validate.notNull(video, "video 不可为 null");

		setFormat(FFmpegUtils.parseFormat(video.getFormat()));
		Validate.notBlank(format, "format 不可为空");

		setCodecId(video.getCodecId());
		setFrameRate(video.getFrameRate());
		setImageWidth(video.getImageWidth());
		setImageHeight(video.getImageHeight());
		setBitrate(video.getBitrate());
		// 不提取像素格式
		//setPixelFormat(video.pixelFormat());
		if (Objects.nonNull(video.getAudio())) {
			setAudio(new AudioOutputOption(video.getAudio()));
		}
	}

	/**
	 * 从 FFmpeg 帧抓取器构造视频输出选项
	 *
	 * @param grabber FFmpeg 帧抓取器
	 * @throws FFmpegFrameGrabber.Exception 解析失败时抛出
	 * @since 1.1.0
	 */
	public VideoOutputOption(FFmpegFrameGrabber grabber) throws FFmpegFrameGrabber.Exception {
		this(Video.parse(grabber));
	}

	/**
	 * 创建 MP4 格式 H264 编码视频输出选项
	 * <p>使用默认帧率</p>
	 *
	 * @param preset 视频预设
	 * @return MP4 H264 视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption mp4WithH264(VideoPreset preset) {
		return mp4WithH264(preset, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 MP4 格式 H264 编码视频输出选项
	 *
	 * @param preset    视频预设
	 * @param frameRate 帧率
	 * @return MP4 H264 视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption mp4WithH264(VideoPreset preset, double frameRate) {
		return VideoOutputOptionBuilder.mp4WithH264(preset)
			.frameRate(frameRate)
			.bitrate(preset.h264Bitrate)
			.audio(AudioOutputOption.aacForVideo(preset.aacBitrate))
			.build();
	}

	/**
	 * 创建 MP4 格式 H265 编码视频输出选项
	 * <p>使用默认帧率</p>
	 *
	 * @param preset 视频预设
	 * @return MP4 H265 视频输出选项
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOption mp4WithH265(VideoPreset preset) {
		return mp4WithH265(preset, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 MP4 格式 H265 编码视频输出选项
	 *
	 * @param preset    视频预设
	 * @param frameRate 帧率
	 * @return MP4 H265 视频输出选项
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOption mp4WithH265(VideoPreset preset, double frameRate) {
		return VideoOutputOptionBuilder.mp4WithH265(preset)
			.frameRate(frameRate)
			.bitrate(preset.h265Bitrate)
			.audio(AudioOutputOption.aacForVideo(preset.aacBitrate))
			.build();
	}

	/**
	 * 创建 MKV 格式 H264 编码视频输出选项
	 * <p>使用默认帧率</p>
	 *
	 * @param preset 视频预设
	 * @return MKV H264 视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption mkvWithH264(VideoPreset preset) {
		return mkvWithH264(preset, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 MKV 格式 H264 编码视频输出选项
	 *
	 * @param preset    视频预设
	 * @param frameRate 帧率
	 * @return MKV H264 视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption mkvWithH264(VideoPreset preset, double frameRate) {
		return VideoOutputOptionBuilder.mkvWithH264(preset)
			.frameRate(frameRate)
			.bitrate(preset.h264Bitrate)
			.audio(AudioOutputOption.aacForVideo(preset.aacBitrate))
			.build();
	}

	/**
	 * 创建 MKV 格式 H265 编码视频输出选项
	 * <p>使用默认帧率</p>
	 *
	 * @param preset 视频预设
	 * @return MKV H265 视频输出选项
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOption mkvWithH265(VideoPreset preset) {
		return mkvWithH265(preset, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 MKV 格式 H265 编码视频输出选项
	 *
	 * @param preset    视频预设
	 * @param frameRate 帧率
	 * @return MKV H265 视频输出选项
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOption mkvWithH265(VideoPreset preset, double frameRate) {
		return VideoOutputOptionBuilder.mkvWithH265(preset)
			.frameRate(frameRate)
			.bitrate(preset.h265Bitrate)
			.audio(AudioOutputOption.aacForVideo(preset.aacBitrate))
			.build();
	}

	/**
	 * 创建 WebM 格式 VP9 编码视频输出选项
	 * <p>使用默认帧率</p>
	 *
	 * @param preset 视频预设
	 * @return WebM VP9 视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption webmWithVP9(VideoPreset preset) {
		return webmWithVP9(preset, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 WebM 格式 VP9 编码视频输出选项
	 *
	 * @param preset    视频预设
	 * @param frameRate 帧率
	 * @return WebM VP9 视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption webmWithVP9(VideoPreset preset, double frameRate) {
		return VideoOutputOptionBuilder.webmWithVP9(preset)
			.frameRate(frameRate)
			.bitrate(preset.vp9Bitrate)
			.audio(AudioOutputOption.opusForVideo(preset.opusBitrate))
			.build();
	}

	/**
	 * 创建 MP4 格式 H264 高比特率编码视频输出选项
	 * <p>使用默认帧率</p>
	 *
	 * @param preset 视频预设
	 * @return MP4 H264 高比特率视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption mp4WithH264HighBitrate(VideoPreset preset) {
		return mp4WithH264HighBitrate(preset, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 MP4 格式 H264 高比特率编码视频输出选项
	 *
	 * @param preset    视频预设
	 * @param frameRate 帧率
	 * @return MP4 H264 高比特率视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption mp4WithH264HighBitrate(VideoPreset preset, double frameRate) {
		return VideoOutputOptionBuilder.mp4WithH264(preset)
			.frameRate(frameRate)
			.bitrate(preset.h264HighBitrate)
			.audio(AudioOutputOption.aacForVideo(preset.aacBitrate))
			.build();
	}

	/**
	 * 创建 MP4 格式 H265 高比特率编码视频输出选项
	 * <p>使用默认帧率</p>
	 *
	 * @param preset 视频预设
	 * @return MP4 H265 高比特率视频输出选项
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOption mp4WithH265HighBitrate(VideoPreset preset) {
		return mp4WithH265HighBitrate(preset, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 MP4 格式 H265 高比特率编码视频输出选项
	 *
	 * @param preset    视频预设
	 * @param frameRate 帧率
	 * @return MP4 H265 高比特率视频输出选项
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOption mp4WithH265HighBitrate(VideoPreset preset, double frameRate) {
		return VideoOutputOptionBuilder.mp4WithH265(preset)
			.frameRate(frameRate)
			.bitrate(preset.h265HighBitrate)
			.audio(AudioOutputOption.aacForVideo(preset.aacBitrate))
			.build();
	}

	/**
	 * 创建 MKV 格式 H264 高比特率编码视频输出选项
	 * <p>使用默认帧率</p>
	 *
	 * @param preset 视频预设
	 * @return MKV H264 高比特率视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption mkvWithH264HighBitrate(VideoPreset preset) {
		return mkvWithH264HighBitrate(preset, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 MKV 格式 H264 高比特率编码视频输出选项
	 *
	 * @param preset    视频预设
	 * @param frameRate 帧率
	 * @return MKV H264 高比特率视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption mkvWithH264HighBitrate(VideoPreset preset, double frameRate) {
		return VideoOutputOptionBuilder.mkvWithH264(preset)
			.frameRate(frameRate)
			.bitrate(preset.h264HighBitrate)
			.audio(AudioOutputOption.aacForVideo(preset.aacBitrate))
			.build();
	}

	/**
	 * 创建 MKV 格式 H265 高比特率编码视频输出选项
	 * <p>使用默认帧率</p>
	 *
	 * @param preset 视频预设
	 * @return MKV H265 高比特率视频输出选项
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOption mkvWithH265HighBitrate(VideoPreset preset) {
		return mkvWithH265HighBitrate(preset, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 MKV 格式 H265 高比特率编码视频输出选项
	 *
	 * @param preset    视频预设
	 * @param frameRate 帧率
	 * @return MKV H265 高比特率视频输出选项
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOption mkvWithH265HighBitrate(VideoPreset preset, double frameRate) {
		return VideoOutputOptionBuilder.mkvWithH265(preset)
			.frameRate(frameRate)
			.bitrate(preset.h265HighBitrate)
			.audio(AudioOutputOption.aacForVideo(preset.aacBitrate))
			.build();
	}

	/**
	 * 创建 WebM 格式 VP9 高比特率编码视频输出选项
	 * <p>使用默认帧率</p>
	 *
	 * @param preset 视频预设
	 * @return WebM VP9 高比特率视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption webmWithVP9HighBitrate(VideoPreset preset) {
		return webmWithVP9HighBitrate(preset, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 WebM 格式 VP9 高比特率编码视频输出选项
	 *
	 * @param preset    视频预设
	 * @param frameRate 帧率
	 * @return WebM VP9 高比特率视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption webmWithVP9HighBitrate(VideoPreset preset, double frameRate) {
		return VideoOutputOptionBuilder.webmWithVP9(preset)
			.frameRate(frameRate)
			.bitrate(preset.vp9HighBitrate)
			.audio(AudioOutputOption.opusForVideo(preset.opusBitrate))
			.build();
	}

	/**
	 * 创建 MP4 格式 H264 低比特率编码视频输出选项
	 * <p>使用默认帧率</p>
	 *
	 * @param preset 视频预设
	 * @return MP4 H264 低比特率视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption mp4WithH264LowBitrate(VideoPreset preset) {
		return mp4WithH264LowBitrate(preset, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 MP4 格式 H264 低比特率编码视频输出选项
	 *
	 * @param preset    视频预设
	 * @param frameRate 帧率
	 * @return MP4 H264 低比特率视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption mp4WithH264LowBitrate(VideoPreset preset, double frameRate) {
		return VideoOutputOptionBuilder.mp4WithH264(preset)
			.frameRate(frameRate)
			.bitrate(preset.h264LowBitrate)
			.audio(AudioOutputOption.aacForVideo(preset.aacBitrate))
			.build();
	}

	/**
	 * 创建 MP4 格式 H265 低比特率编码视频输出选项
	 * <p>使用默认帧率</p>
	 *
	 * @param preset 视频预设
	 * @return MP4 H265 低比特率视频输出选项
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOption mp4WithH265LowBitrate(VideoPreset preset) {
		return mp4WithH265LowBitrate(preset, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 MP4 格式 H265 低比特率编码视频输出选项
	 *
	 * @param preset    视频预设
	 * @param frameRate 帧率
	 * @return MP4 H265 低比特率视频输出选项
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOption mp4WithH265LowBitrate(VideoPreset preset, double frameRate) {
		return VideoOutputOptionBuilder.mp4WithH265(preset)
			.frameRate(frameRate)
			.bitrate(preset.h265LowBitrate)
			.audio(AudioOutputOption.aacForVideo(preset.aacBitrate))
			.build();
	}

	/**
	 * 创建 MKV 格式 H264 低比特率编码视频输出选项
	 * <p>使用默认帧率</p>
	 *
	 * @param preset 视频预设
	 * @return MKV H264 低比特率视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption mkvWithH264LowBitrate(VideoPreset preset) {
		return mkvWithH264LowBitrate(preset, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 MKV 格式 H264 低比特率编码视频输出选项
	 *
	 * @param preset    视频预设
	 * @param frameRate 帧率
	 * @return MKV H264 低比特率视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption mkvWithH264LowBitrate(VideoPreset preset, double frameRate) {
		return VideoOutputOptionBuilder.mkvWithH264(preset)
			.frameRate(frameRate)
			.bitrate(preset.h264LowBitrate)
			.audio(AudioOutputOption.aacForVideo(preset.aacBitrate))
			.build();
	}

	/**
	 * 创建 MKV 格式 H265 低比特率编码视频输出选项
	 * <p>使用默认帧率</p>
	 *
	 * @param preset 视频预设
	 * @return MKV H265 低比特率视频输出选项
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOption mkvWithH265LowBitrate(VideoPreset preset) {
		return mkvWithH265LowBitrate(preset, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 MKV 格式 H265 低比特率编码视频输出选项
	 *
	 * @param preset    视频预设
	 * @param frameRate 帧率
	 * @return MKV H265 低比特率视频输出选项
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOption mkvWithH265LowBitrate(VideoPreset preset, double frameRate) {
		return VideoOutputOptionBuilder.mkvWithH265(preset)
			.frameRate(frameRate)
			.bitrate(preset.h265LowBitrate)
			.audio(AudioOutputOption.aacForVideo(preset.aacBitrate))
			.build();
	}

	/**
	 * 创建 WebM 格式 VP9 低比特率编码视频输出选项
	 * <p>使用默认帧率</p>
	 *
	 * @param preset 视频预设
	 * @return WebM VP9 低比特率视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption webmWithVP9LowBitrate(VideoPreset preset) {
		return webmWithVP9LowBitrate(preset, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 WebM 格式 VP9 低比特率编码视频输出选项
	 *
	 * @param preset    视频预设
	 * @param frameRate 帧率
	 * @return WebM VP9 低比特率视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption webmWithVP9LowBitrate(VideoPreset preset, double frameRate) {
		return VideoOutputOptionBuilder.webmWithVP9(preset)
			.frameRate(frameRate)
			.bitrate(preset.vp9LowBitrate)
			.audio(AudioOutputOption.opusForVideo(preset.opusBitrate))
			.build();
	}

	/**
	 * 创建 MP4 格式 H264 编码视频输出选项
	 * <p>根据分辨率自动匹配预设，使用默认帧率</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @return MP4 H264 视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption mp4WithH264(int imageWidth, int imageHeight) {
		return mp4WithH264(imageWidth, imageHeight, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 MP4 格式 H264 编码视频输出选项
	 * <p>根据分辨率自动匹配预设</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @param frameRate   帧率
	 * @return MP4 H264 视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption mp4WithH264(int imageWidth, int imageHeight, double frameRate) {
		VideoPreset preset = VideoPreset.match(imageWidth, imageHeight);

		return VideoOutputOptionBuilder.mp4WithH264(imageWidth, imageHeight)
			.frameRate(frameRate)
			.bitrate(preset.h264Bitrate)
			.audio(AudioOutputOption.aacForVideo(preset.aacBitrate))
			.build();
	}

	/**
	 * 创建 MP4 格式 H265 编码视频输出选项
	 * <p>根据分辨率自动匹配预设，使用默认帧率</p>
	 *
	 * @param imageWidth  视品宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @return MP4 H265 视频输出选项
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOption mp4WithH265(int imageWidth, int imageHeight) {
		return mp4WithH265(imageWidth, imageHeight, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 MP4 格式 H265 编码视频输出选项
	 * <p>根据分辨率自动匹配预设</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @param frameRate   帧率
	 * @return MP4 H265 视频输出选项
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOption mp4WithH265(int imageWidth, int imageHeight, double frameRate) {
		VideoPreset preset = VideoPreset.match(imageWidth, imageHeight);

		return VideoOutputOptionBuilder.mp4WithH265(imageWidth, imageHeight)
			.frameRate(frameRate)
			.bitrate(preset.h265Bitrate)
			.audio(AudioOutputOption.aacForVideo(preset.aacBitrate))
			.build();
	}

	/**
	 * 创建 MKV 格式 H264 编码视频输出选项
	 * <p>根据分辨率自动匹配预设，使用默认帧率</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @return MKV H264 视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption mkvWithH264(int imageWidth, int imageHeight) {
		return mkvWithH264(imageWidth, imageHeight, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 MKV 格式 H264 编码视频输出选项
	 * <p>根据分辨率自动匹配预设</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @param frameRate   帧率
	 * @return MKV H264 视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption mkvWithH264(int imageWidth, int imageHeight, double frameRate) {
		VideoPreset preset = VideoPreset.match(imageWidth, imageHeight);

		return VideoOutputOptionBuilder.mkvWithH264(imageWidth, imageHeight)
			.frameRate(frameRate)
			.bitrate(preset.h264Bitrate)
			.audio(AudioOutputOption.aacForVideo(preset.aacBitrate))
			.build();
	}

	/**
	 * 创建 MKV 格式 H265 编码视频输出选项
	 * <p>根据分辨率自动匹配预设，使用默认帧率</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @return MKV H265 视频输出选项
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOption mkvWithH265(int imageWidth, int imageHeight) {
		return mkvWithH265(imageWidth, imageHeight, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 MKV 格式 H265 编码视频输出选项
	 * <p>根据分辨率自动匹配预设</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @param frameRate   帧率
	 * @return MKV H265 视频输出选项
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOption mkvWithH265(int imageWidth, int imageHeight, double frameRate) {
		VideoPreset preset = VideoPreset.match(imageWidth, imageHeight);

		return VideoOutputOptionBuilder.mkvWithH265(imageWidth, imageHeight)
			.frameRate(frameRate)
			.bitrate(preset.h265Bitrate)
			.audio(AudioOutputOption.aacForVideo(preset.aacBitrate))
			.build();
	}

	/**
	 * 创建 WebM 格式 VP9 编码视频输出选项
	 * <p>根据分辨率自动匹配预设，使用默认帧率</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @return WebM VP9 视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption webmWithVP9(int imageWidth, int imageHeight) {
		return webmWithVP9(imageWidth, imageHeight, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 WebM 格式 VP9 编码视频输出选项
	 * <p>根据分辨率自动匹配预设</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @param frameRate   帧率
	 * @return WebM VP9 视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption webmWithVP9(int imageWidth, int imageHeight, double frameRate) {
		VideoPreset preset = VideoPreset.match(imageWidth, imageHeight);

		return VideoOutputOptionBuilder.webmWithVP9(imageWidth, imageHeight)
			.frameRate(frameRate)
			.bitrate(preset.vp9Bitrate)
			.audio(AudioOutputOption.opusForVideo(preset.opusBitrate))
			.build();
	}

	/**
	 * 创建 MP4 格式 H264 高比特率编码视频输出选项
	 * <p>根据分辨率自动匹配预设，使用默认帧率</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @return MP4 H264 高比特率视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption mp4WithH264HighBitrate(int imageWidth, int imageHeight) {
		return mp4WithH264HighBitrate(imageWidth, imageHeight, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 MP4 格式 H264 高比特率编码视频输出选项
	 * <p>根据分辨率自动匹配预设</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @param frameRate   帧率
	 * @return MP4 H264 高比特率视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption mp4WithH264HighBitrate(int imageWidth, int imageHeight, double frameRate) {
		VideoPreset preset = VideoPreset.match(imageWidth, imageHeight);

		return VideoOutputOptionBuilder.mp4WithH264(imageWidth, imageHeight)
			.frameRate(frameRate)
			.bitrate(preset.h264HighBitrate)
			.audio(AudioOutputOption.aacForVideo(preset.aacBitrate))
			.build();
	}

	/**
	 * 创建 MP4 格式 H265 高比特率编码视频输出选项
	 * <p>根据分辨率自动匹配预设，使用默认帧率</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @return MP4 H265 高比特率视频输出选项
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOption mp4WithH265HighBitrate(int imageWidth, int imageHeight) {
		return mp4WithH265HighBitrate(imageWidth, imageHeight, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 MP4 格式 H265 高比特率编码视频输出选项
	 * <p>根据分辨率自动匹配预设</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @param frameRate   帧率
	 * @return MP4 H265 高比特率视频输出选项
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOption mp4WithH265HighBitrate(int imageWidth, int imageHeight, double frameRate) {
		VideoPreset preset = VideoPreset.match(imageWidth, imageHeight);

		return VideoOutputOptionBuilder.mp4WithH265(imageWidth, imageHeight)
			.frameRate(frameRate)
			.bitrate(preset.h265HighBitrate)
			.audio(AudioOutputOption.aacForVideo(preset.aacBitrate))
			.build();
	}

	/**
	 * 创建 MKV 格式 H264 高比特率编码视频输出选项
	 * <p>根据分辨率自动匹配预设，使用默认帧率</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @return MKV H264 高比特率视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption mkvWithH264HighBitrate(int imageWidth, int imageHeight) {
		return mkvWithH264HighBitrate(imageWidth, imageHeight, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 MKV 格式 H264 高比特率编码视频输出选项
	 * <p>根据分辨率自动匹配预设</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @param frameRate   帧率
	 * @return MKV H264 高比特率视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption mkvWithH264HighBitrate(int imageWidth, int imageHeight, double frameRate) {
		VideoPreset preset = VideoPreset.match(imageWidth, imageHeight);

		return VideoOutputOptionBuilder.mkvWithH264(imageWidth, imageHeight)
			.frameRate(frameRate)
			.bitrate(preset.h264HighBitrate)
			.audio(AudioOutputOption.aacForVideo(preset.aacBitrate))
			.build();
	}

	/**
	 * 创建 MKV 格式 H265 高比特率编码视频输出选项
	 * <p>根据分辨率自动匹配预设，使用默认帧率</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @return MKV H265 高比特率视频输出选项
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOption mkvWithH265HighBitrate(int imageWidth, int imageHeight) {
		return mkvWithH265HighBitrate(imageWidth, imageHeight, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 MKV 格式 H265 高比特率编码视频输出选项
	 * <p>根据分辨率自动匹配预设</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @param frameRate   帧率
	 * @return MKV H265 高比特率视频输出选项
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOption mkvWithH265HighBitrate(int imageWidth, int imageHeight, double frameRate) {
		VideoPreset preset = VideoPreset.match(imageWidth, imageHeight);

		return VideoOutputOptionBuilder.mkvWithH265(imageWidth, imageHeight)
			.frameRate(frameRate)
			.bitrate(preset.h265HighBitrate)
			.audio(AudioOutputOption.aacForVideo(preset.aacBitrate))
			.build();
	}

	/**
	 * 创建 WebM 格式 VP9 高比特率编码视频输出选项
	 * <p>根据分辨率自动匹配预设，使用默认帧率</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @return WebM VP9 高比特率视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption webmWithVP9HighBitrate(int imageWidth, int imageHeight) {
		return webmWithVP9HighBitrate(imageWidth, imageHeight, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 WebM 格式 VP9 高比特率编码视频输出选项
	 * <p>根据分辨率自动匹配预设</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @param frameRate   帧率
	 * @return WebM VP9 高比特率视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption webmWithVP9HighBitrate(int imageWidth, int imageHeight, double frameRate) {
		VideoPreset preset = VideoPreset.match(imageWidth, imageHeight);

		return VideoOutputOptionBuilder.webmWithVP9(imageWidth, imageHeight)
			.frameRate(frameRate)
			.bitrate(preset.vp9HighBitrate)
			.audio(AudioOutputOption.opusForVideo(preset.opusBitrate))
			.build();
	}

	/**
	 * 创建 MP4 格式 H264 低比特率编码视频输出选项
	 * <p>根据分辨率自动匹配预设，使用默认帧率</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @return MP4 H264 低比特率视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption mp4WithH264LowBitrate(int imageWidth, int imageHeight) {
		return mp4WithH264LowBitrate(imageWidth, imageHeight, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 MP4 格式 H264 低比特率编码视频输出选项
	 * <p>根据分辨率自动匹配预设</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @param frameRate   帧率
	 * @return MP4 H264 低比特率视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption mp4WithH264LowBitrate(int imageWidth, int imageHeight, double frameRate) {
		VideoPreset preset = VideoPreset.match(imageWidth, imageHeight);

		return VideoOutputOptionBuilder.mp4WithH264(imageWidth, imageHeight)
			.frameRate(frameRate)
			.bitrate(preset.h264LowBitrate)
			.audio(AudioOutputOption.aacForVideo(preset.aacBitrate))
			.build();
	}

	/**
	 * 创建 MP4 格式 H265 低比特率编码视频输出选项
	 * <p>根据分辨率自动匹配预设，使用默认帧率</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @return MP4 H265 低比特率视频输出选项
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOption mp4WithH265LowBitrate(int imageWidth, int imageHeight) {
		return mp4WithH265LowBitrate(imageWidth, imageHeight, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 MP4 格式 H265 低比特率编码视频输出选项
	 * <p>根据分辨率自动匹配预设</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @param frameRate   帧率
	 * @return MP4 H265 低比特率视频输出选项
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOption mp4WithH265LowBitrate(int imageWidth, int imageHeight, double frameRate) {
		VideoPreset preset = VideoPreset.match(imageWidth, imageHeight);

		return VideoOutputOptionBuilder.mp4WithH265(imageWidth, imageHeight)
			.frameRate(frameRate)
			.bitrate(preset.h265LowBitrate)
			.audio(AudioOutputOption.aacForVideo(preset.aacBitrate))
			.build();
	}

	/**
	 * 创建 MKV 格式 H264 低比特率编码视频输出选项
	 * <p>根据分辨率自动匹配预设，使用默认帧率</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @return MKV H264 低比特率视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption mkvWithH264LowBitrate(int imageWidth, int imageHeight) {
		return mkvWithH264LowBitrate(imageWidth, imageHeight, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 MKV 格式 H264 低比特率编码视频输出选项
	 * <p>根据分辨率自动匹配预设</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @param frameRate   帧率
	 * @return MKV H264 低比特率视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption mkvWithH264LowBitrate(int imageWidth, int imageHeight, double frameRate) {
		VideoPreset preset = VideoPreset.match(imageWidth, imageHeight);

		return VideoOutputOptionBuilder.mkvWithH264(imageWidth, imageHeight)
			.frameRate(frameRate)
			.bitrate(preset.h264LowBitrate)
			.audio(AudioOutputOption.aacForVideo(preset.aacBitrate))
			.build();
	}

	/**
	 * 创建 MKV 格式 H265 低比特率编码视频输出选项
	 * <p>根据分辨率自动匹配预设，使用默认帧率</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @return MKV H265 低比特率视频输出选项
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOption mkvWithH265LowBitrate(int imageWidth, int imageHeight) {
		return mkvWithH265LowBitrate(imageWidth, imageHeight, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 MKV 格式 H265 低比特率编码视频输出选项
	 * <p>根据分辨率自动匹配预设</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @param frameRate   帧率
	 * @return MKV H265 低比特率视频输出选项
	 * @apiNote 需要导入<b>ffmpeg-platform-gpl</b>包
	 * @since 1.1.0
	 */
	public static VideoOutputOption mkvWithH265LowBitrate(int imageWidth, int imageHeight, double frameRate) {
		VideoPreset preset = VideoPreset.match(imageWidth, imageHeight);

		return VideoOutputOptionBuilder.mkvWithH265(imageWidth, imageHeight)
			.frameRate(frameRate)
			.bitrate(preset.h265LowBitrate)
			.audio(AudioOutputOption.aacForVideo(preset.aacBitrate))
			.build();
	}

	/**
	 * 创建 WebM 格式 VP9 低比特率编码视频输出选项
	 * <p>根据分辨率自动匹配预设，使用默认帧率</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @return WebM VP9 低比特率视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption webmWithVP9LowBitrate(int imageWidth, int imageHeight) {
		return webmWithVP9LowBitrate(imageWidth, imageHeight, FFmpegConstants.DEFAULT_VIDEO_FRAME_RATE);
	}

	/**
	 * 创建 WebM 格式 VP9 低比特率编码视频输出选项
	 * <p>根据分辨率自动匹配预设</p>
	 *
	 * @param imageWidth  视频宽度（像素）
	 * @param imageHeight 视频高度（像素）
	 * @param frameRate   帧率
	 * @return WebM VP9 低比特率视频输出选项
	 * @since 1.1.0
	 */
	public static VideoOutputOption webmWithVP9LowBitrate(int imageWidth, int imageHeight, double frameRate) {
		VideoPreset preset = VideoPreset.match(imageWidth, imageHeight);

		return VideoOutputOptionBuilder.webmWithVP9(imageWidth, imageHeight)
			.frameRate(frameRate)
			.bitrate(preset.vp9LowBitrate)
			.audio(AudioOutputOption.opusForVideo(preset.opusBitrate))
			.build();
	}

	/**
	 * 获取帧率
	 *
	 * @return 帧率（fps）
	 * @since 1.1.0
	 */
	public double getFrameRate() {
		return frameRate;
	}

	/**
	 * 设置帧率
	 *
	 * @param frameRate 帧率（fps），必须大于 0
	 * @since 1.1.0
	 */
	public void setFrameRate(double frameRate) {
		if (frameRate > 0) {
			this.frameRate = frameRate;
		}
	}

	/**
	 * 获取视频宽度
	 *
	 * @return 视频宽度（像素）
	 * @since 1.1.0
	 */
	public int getImageWidth() {
		return imageWidth;
	}

	/**
	 * 设置视频宽度
	 *
	 * @param imageWidth 视频宽度（像素），必须大于 0
	 * @since 1.1.0
	 */
	public void setImageWidth(int imageWidth) {
		if (imageWidth > 0) {
			this.imageWidth = imageWidth;
		}
	}

	/**
	 * 获取视频高度
	 *
	 * @return 视频高度（像素）
	 * @since 1.1.0
	 */
	public int getImageHeight() {
		return imageHeight;
	}

	/**
	 * 设置视频高度
	 *
	 * @param imageHeight 视频高度（像素），必须大于 0
	 * @since 1.1.0
	 */
	public void setImageHeight(int imageHeight) {
		if (imageHeight > 0) {
			this.imageHeight = imageHeight;
		}
	}

	/**
	 * 获取视频码率
	 *
	 * @return 视频码率（bps）
	 * @since 1.1.0
	 */
	public int getBitrate() {
		return bitrate;
	}

	/**
	 * 设置视频码率
	 *
	 * @param bitrate 视频码率（bps），必须大于等于 0
	 * @apiNote 无损编码设置该值无效
	 * @since 1.1.0
	 */
	public void setBitrate(int bitrate) {
		if (bitrate >= 0) {
			this.bitrate = bitrate;
		}
	}

	/**
	 * 获取像素格式
	 *
	 * @return 像素格式
	 * @since 1.1.0
	 */
	public int getPixelFormat() {
		return pixelFormat;
	}

	/**
	 * 设置像素格式
	 *
	 * @param pixelFormat 像素格式
	 * @apiNote 一般不需要设置，让FFmpeg自行决定
	 * @since 1.1.0
	 */
	public void setPixelFormat(int pixelFormat) {
		this.pixelFormat = pixelFormat;
	}

	/**
	 * 获取音频轨道配置
	 *
	 * @return 音频输出选项，可能为 null
	 * @since 1.1.0
	 */
	public AudioOutputOption getAudio() {
		return audio;
	}

	/**
	 * 设置音频轨道配置
	 *
	 * @param audio 音频输出选项
	 * @since 1.1.0
	 */
	public void setAudio(AudioOutputOption audio) {
		this.audio = audio;
	}

	/**
	 * 配置 FFmpeg 帧录制器
	 * <p>
	 * 根据帧类型配置视频和音频相关的录制器参数。
	 * </p>
	 *
	 * @param recorder  FFmpeg 帧录制器
	 * @param frameType 帧类型
	 * @throws IllegalArgumentException 当参数为 null 时抛出
	 * @since 1.1.0
	 */
	@Override
	public void configure(FFmpegFrameRecorder recorder, FrameType frameType) {
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.notNull(frameType, "frameType 不可为 null");

		if (frameType != FrameType.VIDEO && Objects.nonNull(audio)) {
			audio.configure(recorder, frameType);
		}
		if (frameType != FrameType.AUDIO) {
			recorder.setFormat(format);
			recorder.setVideoCodec(codecId);
			recorder.setFrameRate(frameRate);
			recorder.setImageWidth(imageWidth);
			recorder.setImageHeight(imageHeight);
			if (bitrate > 0) {
				recorder.setVideoBitrate(bitrate);
			}
			recorder.setPixelFormat(pixelFormat);
			if (!stripMetadata) {
				recorder.setMetadata(Map.copyOf(metadata));
			}
		}
	}
}
