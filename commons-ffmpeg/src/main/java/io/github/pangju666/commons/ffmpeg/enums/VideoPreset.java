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

package io.github.pangju666.commons.ffmpeg.enums;

import org.apache.commons.lang3.Validate;

/**
 * 视频预设枚举
 * <p>
 * 定义了常见视频分辨率的预设配置，包括分辨率和不同编码器的推荐比特率。
 * 支持横屏和竖屏两种方向，可根据视频尺寸自动匹配对应的预设。
 * </p>
 * <p>
 * <b>支持的分辨率：</b>
 * </p>
 * <ul>
 *   <li>UHD 4K (3840x2160) - 超高清，适用于高端视频制作</li>
 *   <li>QHD 2K (2560x1440) - 2K 高清，适用于中高端视频</li>
 *   <li>FHD 1080p (1920x1080) - 全高清，适用于标准高清视频</li>
 *   <li>HD 720p (1280x720) - 高清，适用于网络视频</li>
 *   <li>SD 480p (640x480) - 标清，适用于低带宽场景</li>
 *   <li>SD 360p (640x360) - 低标清，适用于移动设备或低带宽场景</li>
 * </ul>
 * <p>
 * <b>支持的编码器：</b>
 * </p>
 * <ul>
 *   <li>音频编码：AAC、Opus</li>
 *   <li>视频编码：H.264 (AVC)、H.265 (HEVC)、VP9</li>
 * </ul>
 * <p>
 * 每个预设为不同编码器提供了低、标准、高三种比特率选项，以适应不同的质量和带宽需求。
 * </p>
 * <p>
 * <b>使用示例：</b>
 * </p>
 * <pre>{@code
 * // 根据视频尺寸自动匹配预设
 * VideoPreset preset = VideoPreset.match(1920, 1080);
 * System.out.println(preset.name()); // 输出: FHD_1080P
 *
 * // 获取特定编码器的比特率
 * int h264Bitrate = preset.h264Bitrate;
 * int aacBitrate = preset.aacBitrate;
 * }</pre>
 *
 * @author pangju666
 * @since 1.1.0
 */
public enum VideoPreset {
	/**
	 * UHD 4K 横屏 (3840x2160) - 超高清分辨率
	 *
	 * @since 1.1.0
	 */
	UHD_4K(3840, 2160, 320_000, 256_000,
		12_000_000, 24_000_000, 32_000_000,
		8_000_000, 14_000_000, 20_000_000,
		10_000_000, 18_000_000, 24_000_000),
	/**
	 * QHD 2K 横屏 (2560x1440) - 2K 高清分辨率
	 *
	 * @since 1.1.0
	 */
	QHD_2K(2560, 1440, 256_000, 192_000,
		6_000_000, 12_000_000, 16_000_000,
		4_000_000, 7_200_000, 10_000_000,
		6_000_000, 10_500_000, 14_000_000),
	/**
	 * FHD 1080p 横屏 (1920x1080) - 全高清分辨率
	 *
	 * @since 1.1.0
	 */
	FHD_1080P(1920, 1080, 192_000, 128_000,
		3_000_000, 6_000_000, 8_000_000,
		2_000_000, 3_600_000, 7_000_000,
		3_000_000, 5_200_000, 7_000_000),
	/**
	 * HD 720p 横屏 (1280x720) - 高清分辨率
	 *
	 * @since 1.1.0
	 */
	HD_720P(1280, 720, 128_000, 96_000,
		1_500_000, 3_000_000, 4_000_000,
		1_000_000, 1_800_000, 3_500_000,
		1_500_000, 2_600_000, 3_500_000),
	/**
	 * SD 480p 横屏 (640x480) - 标清分辨率
	 *
	 * @since 1.1.0
	 */
	SD_480P(640, 480, 96_000, 80_000,
		750_000, 1_500_000, 2_000_000,
		500_000, 900_000, 1_800_000,
		800_000, 1_300_000, 1_800_000),
	/**
	 * SD 360p 横屏 (640x360) - 低标清分辨率
	 *
	 * @since 1.1.0
	 */
	SD_360P(640, 360, 64_000, 48_000,
		400_000, 750_000, 1_000_000,
		250_000, 450_000, 800_000,
		400_000, 650_000, 900_000),
	/**
	 * UHD 4K 竖屏 (2160x3840) - 超高清竖屏分辨率
	 *
	 * @since 1.1.0
	 */
	UHD_4K_VERTICAL(2160, 3840, 320_000, 256_000,
		12_000_000, 24_000_000, 32_000_000,
		8_000_000, 14_000_000, 20_000_000,
		10_000_000, 18_000_000, 24_000_000),
	/**
	 * QHD 2K 竖屏 (1440x2560) - 2K 高清竖屏分辨率
	 *
	 * @since 1.1.0
	 */
	QHD_2K_VERTICAL(1440, 2560, 256_000, 192_000,
		6_000_000, 12_000_000, 16_000_000,
		4_000_000, 7_200_000, 10_000_000,
		6_000_000, 10_500_000, 14_000_000),
	/**
	 * FHD 1080p 竖屏 (1080x1920) - 全高清竖屏分辨率
	 *
	 * @since 1.1.0
	 */
	FHD_1080P_VERTICAL(1080, 1920, 192_000, 128_000,
		3_000_000, 6_000_000, 8_000_000,
		2_000_000, 3_600_000, 7_000_000,
		3_000_000, 5_200_000, 7_000_000),
	/**
	 * HD 720p 竖屏 (720x1280) - 高清竖屏分辨率
	 *
	 * @since 1.1.0
	 */
	HD_720P_VERTICAL(720, 1280, 128_000, 96_000,
		1_500_000, 3_000_000, 4_000_000,
		1_000_000, 1_800_000, 3_500_000,
		1_500_000, 2_600_000, 3_500_000),
	/**
	 * SD 480p 竖屏 (480x640) - 标清竖屏分辨率
	 *
	 * @since 1.1.0
	 */
	SD_480P_VERTICAL(480, 640, 96_000, 80_000,
		750_000, 1_500_000, 2_000_000,
		500_000, 900_000, 1_800_000,
		800_000, 1_300_000, 1_800_000),
	/**
	 * SD 360p 竖屏 (360x640) - 低标清竖屏分辨率
	 *
	 * @since 1.1.0
	 */
	SD_360P_VERTICAL(360, 640, 64_000, 48_000,
		400_000, 750_000, 1_000_000,
		250_000, 450_000, 800_000,
		400_000, 650_000, 900_000);

	/**
	 * 视频宽度（像素）
	 *
	 * @since 1.1.0
	 */
	public final int width;
	/**
	 * 视频高度（像素）
	 *
	 * @since 1.1.0
	 */
	public final int height;

	/**
	 * AAC 音频编码比特率（bps）
	 *
	 * @since 1.1.0
	 */
	public final int aacBitrate;
	/**
	 * Opus 音频编码比特率（bps）
	 *
	 * @since 1.1.0
	 */
	public final int opusBitrate;

	/**
	 * H.264 视频编码标准比特率（bps）
	 *
	 * @since 1.1.0
	 */
	public final int h264Bitrate;
	/**
	 * H.264 视频编码低比特率（bps）
	 *
	 * @since 1.1.0
	 */
	public final int h264LowBitrate;
	/**
	 * H.264 视频编码高比特率（bps）
	 *
	 * @since 1.1.0
	 */
	public final int h264HighBitrate;

	/**
	 * H.265 视频编码标准比特率（bps）
	 *
	 * @since 1.1.0
	 */
	public final int h265Bitrate;
	/**
	 * H.265 视频编码低比特率（bps）
	 *
	 * @since 1.1.0
	 */
	public final int h265LowBitrate;
	/**
	 * H.265 视频编码高比特率（bps）
	 *
	 * @since 1.1.0
	 */
	public final int h265HighBitrate;

	/**
	 * VP9 视频编码标准比特率（bps）
	 *
	 * @since 1.1.0
	 */
	public final int vp9Bitrate;
	/**
	 * VP9 视频编码高比特率（bps）
	 *
	 * @since 1.1.0
	 */
	public final int vp9HighBitrate;
	/**
	 * VP9 视频编码低比特率（bps）
	 *
	 * @since 1.1.0
	 */
	public final int vp9LowBitrate;

	/**
	 * 构造函数
	 *
	 * @param width           视频宽度（像素）
	 * @param height          视频高度（像素）
	 * @param aacBitrate      AAC 音频编码比特率（bps）
	 * @param opusBitrate     Opus 音频编码比特率（bps）
	 * @param h264LowBitrate  H.264 视频编码低比特率（bps）
	 * @param h264Bitrate     H.264 视频编码标准比特率（bps）
	 * @param h264HighBitrate H.264 视频编码高比特率（bps）
	 * @param h265LowBitrate  H.265 视频编码低比特率（bps）
	 * @param h265Bitrate     H.265 视频编码标准比特率（bps）
	 * @param h265HighBitrate H.265 视频编码高比特率（bps）
	 * @param vp9LowBitrate   VP9 视频编码低比特率（bps）
	 * @param vp9Bitrate      VP9 视频编码标准比特率（bps）
	 * @param vp9HighBitrate  VP9 视频编码高比特率（bps）
	 * @since 1.1.0
	 */
	VideoPreset(int width, int height, int aacBitrate, int opusBitrate,
	            int h264LowBitrate, int h264Bitrate, int h264HighBitrate,
	            int h265LowBitrate, int h265Bitrate, int h265HighBitrate,
	            int vp9LowBitrate, int vp9Bitrate, int vp9HighBitrate) {
		this.width = width;
		this.height = height;

		this.aacBitrate = aacBitrate;
		this.opusBitrate = opusBitrate;

		this.h264LowBitrate = h264LowBitrate;
		this.h264Bitrate = h264Bitrate;
		this.h264HighBitrate = h264HighBitrate;

		this.h265LowBitrate = h265LowBitrate;
		this.h265Bitrate = h265Bitrate;
		this.h265HighBitrate = h265HighBitrate;

		this.vp9LowBitrate = vp9LowBitrate;
		this.vp9Bitrate = vp9Bitrate;
		this.vp9HighBitrate = vp9HighBitrate;
	}

	/**
	 * 根据视频尺寸匹配对应的预设
	 * <p>
	 * 根据输入的宽度和高度，自动匹配最接近的视频预设。
	 * 匹配规则基于视频的最大边和最小边，同时考虑横屏和竖屏方向。
	 * </p>
	 *
	 * @param width  视频宽度（像素），必须大于 0
	 * @param height 视频高度（像素），必须大于 0
	 * @return 匹配的视频预设
	 * @throws IllegalArgumentException 当 width 或 height 小于等于 0 时抛出
	 * @since 1.1.0
	 */
	public static VideoPreset match(int width, int height) {
		Validate.isTrue(width > 0 && height > 0, "width 和 height 必须大于 0");

		int maxSide = Math.max(width, height);
		int minSide = Math.min(width, height);
		boolean isVertical = height > width;

		if (maxSide >= 3840 && minSide >= 2160) {
			return isVertical ? UHD_4K_VERTICAL : UHD_4K;
		} else if (maxSide >= 2560 && minSide >= 1440) {
			return isVertical ? QHD_2K_VERTICAL : QHD_2K;
		} else if (maxSide >= 1920 && minSide >= 1080) {
			return isVertical ? FHD_1080P_VERTICAL : FHD_1080P;
		} else if (maxSide >= 1280 && minSide >= 720) {
			return isVertical ? HD_720P_VERTICAL : HD_720P;
		} else if (maxSide >= 854 && minSide >= 480) {
			return isVertical ? SD_480P_VERTICAL : SD_480P;
		} else {
			return isVertical ? SD_360P_VERTICAL : SD_360P;
		}
	}
}
