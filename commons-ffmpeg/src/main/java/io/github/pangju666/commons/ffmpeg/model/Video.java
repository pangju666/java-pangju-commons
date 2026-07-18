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

import io.github.pangju666.commons.ffmpeg.utils.FFmpegUtils;
import org.apache.commons.lang3.Validate;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * 视频信息类
 * <p>
 * 用于封装视频文件的元数据信息，包括格式、编解码器、分辨率、帧率、比特率等。
 * 通过 {@link #parse(FFmpegFrameGrabber)} 静态方法从 FFmpeg 帧抓取器中解析视频信息。
 * 如果视频包含音频轨道，也会同时解析音频信息。
 * </p>
 * <p>
 * <b>主要字段说明：</b>
 * </p>
 * <ul>
 *   <li>{@code format} - 视频格式（如 mp4、avi、mkv 等）</li>
 *   <li>{@code metadata} - 视频元数据映射（如标题、创建时间等）</li>
 *   <li>{@code codecName} - 视频编解码器名称</li>
 *   <li>{@code codecId} - 视频编解码器 ID</li>
 *   <li>{@code duration} - 视频时长</li>
 *   <li>{@code frameRate} - 帧率（fps）</li>
 *   <li>{@code imageWidth} - 视频宽度（像素）</li>
 *   <li>{@code imageHeight} - 视频高度（像素）</li>
 *   <li>{@code bitrate} - 视频比特率（bps）</li>
 *   <li>{@code pixelFormat} - 像素格式</li>
 *   <li>{@code videoMetadata} - 视频专用元数据映射</li>
 *   <li>{@code audio} - 音频信息（如果视频包含音频）</li>
 * </ul>
 * <p>
 * <b>使用示例：</b>
 * </p>
 * <pre>{@code
 * FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("video.mp4");
 * grabber.start();
 * Video video = Video.parse(grabber);
 * System.out.println("分辨率: " + video.getImageWidth() + "x" + video.getImageHeight());
 * System.out.println("帧率: " + video.getFrameRate());
 * System.out.println("是否竖屏: " + video.isVertical());
 * System.out.println("包含音频: " + video.hasAudio());
 * grabber.close();
 * }</pre>
 *
 * @author pangju666
 * @since 1.1.0
 */
public class Video {
	private String format;
	private Map<String, String> metadata;
	private String codecName;
	private int codecId;
	private Duration duration;
	private double frameRate;
	private int imageWidth;
	private int imageHeight;
	private int bitrate;
	private int pixelFormat;
	private Map<String, String> videoMetadata;
	private Audio audio;

	public Video() {
	}

	protected Video(String format, Map<String, String> metadata, String codecName, int codecId, Duration duration,
	                double frameRate, int imageWidth, int imageHeight, int bitrate, int pixelFormat,
	                Map<String, String> videoMetadata, Audio audio) {
		this.format = format;
		this.metadata = metadata;
		this.codecName = codecName;
		this.codecId = codecId;
		this.duration = duration;
		this.frameRate = frameRate;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.bitrate = bitrate;
		this.pixelFormat = pixelFormat;
		this.videoMetadata = videoMetadata;
		this.audio = audio;
	}

	/**
	 * 从 FFmpeg 帧抓取器解析视频信息
	 * <p>
	 * 如果抓取器未启动，会自动启动它。
	 * 如果资源不包含视频流，返回 null。
	 * 如果视频包含音频轨道，会同时解析音频信息。
	 * </p>
	 *
	 * @param grabber FFmpeg 帧抓取器，不可为 null
	 * @return 视频信息对象，如果资源不包含视频流则返回 null
	 * @throws IllegalArgumentException     当 grabber 为 null 时抛出
	 * @throws FFmpegFrameGrabber.Exception 当抓取器启动失败时抛出
	 * @since 1.1.0
	 */
	public static Video parse(FFmpegFrameGrabber grabber) throws FFmpegFrameGrabber.Exception {
		Validate.notNull(grabber, "grabber 不可为 null");

		if (FFmpegUtils.isNotStarted(grabber)) {
			grabber.start();
		}

		if (grabber.hasVideo()) {
			return new Video(grabber.getFormat(), Map.copyOf(grabber.getMetadata()),
				grabber.getVideoCodecName(), grabber.getVideoCodec(),
				Duration.ofNanos(grabber.getLengthInTime() * 1000), grabber.getFrameRate(),
				grabber.getImageWidth(), grabber.getImageHeight(), grabber.getVideoBitrate(), grabber.getPixelFormat(),
				Map.copyOf(grabber.getVideoMetadata()), Audio.parse(grabber));
		} else {
			return null;
		}
	}

	/**
	 * 判断是否为竖屏视频
	 * <p>当高度大于宽度时判定为竖屏</p>
	 *
	 * @return true表示竖屏，false表示横屏或正方形
	 * @since 1.1.0
	 */
	public boolean isVertical() {
		return imageHeight > imageWidth;
	}

	/**
	 * 判断是否为正方形视频
	 * <p>当宽度等于高度时判定为正方形</p>
	 *
	 * @return true表示正方形，false表示非正方形
	 * @since 1.1.0
	 */
	public boolean isSquare() {
		return imageWidth == imageHeight;
	}

	/**
	 * 判断视频是否包含音频轨道
	 *
	 * @return true = 有音频，false = 无音频
	 * @since 1.1.0
	 */
	public boolean hasAudio() {
		return Objects.nonNull(audio);
	}

	/**
	 * 判断是否存在有效播放时长
	 * <p>检查 duration 是否不为 null、不为零时长且不为负数</p>
	 *
	 * @return true = 非空且时长不为 0 且不为负数；false = 无有效时长
	 * @since 1.1.0
	 */
	public boolean hasDuration() {
		return duration != null && !duration.isZero() && !duration.isNegative();
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

	public String getCodecName() {
		return codecName;
	}

	public void setCodecName(String codecName) {
		this.codecName = codecName;
	}

	public int getCodecId() {
		return codecId;
	}

	public void setCodecId(int codecId) {
		this.codecId = codecId;
	}

	public Duration getDuration() {
		return duration;
	}

	public void setDuration(Duration duration) {
		this.duration = duration;
	}

	public double getFrameRate() {
		return frameRate;
	}

	public void setFrameRate(double frameRate) {
		this.frameRate = frameRate;
	}

	public int getImageWidth() {
		return imageWidth;
	}

	public void setImageWidth(int imageWidth) {
		this.imageWidth = imageWidth;
	}

	public int getImageHeight() {
		return imageHeight;
	}

	public void setImageHeight(int imageHeight) {
		this.imageHeight = imageHeight;
	}

	public int getBitrate() {
		return bitrate;
	}

	public void setBitrate(int bitrate) {
		this.bitrate = bitrate;
	}

	public int getPixelFormat() {
		return pixelFormat;
	}

	public void setPixelFormat(int pixelFormat) {
		this.pixelFormat = pixelFormat;
	}

	public Map<String, String> getVideoMetadata() {
		return videoMetadata;
	}

	public void setVideoMetadata(Map<String, String> videoMetadata) {
		this.videoMetadata = videoMetadata;
	}

	public Audio getAudio() {
		return audio;
	}

	public void setAudio(Audio audio) {
		this.audio = audio;
	}
}
