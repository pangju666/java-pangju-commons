/*
 *   Copyright 2025 pangju666
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

import io.github.pangju666.commons.ffmpeg.utils.FFmpegFiltersBuilder;
import io.github.pangju666.commons.ffmpeg.utils.FFmpegUtils;
import io.github.pangju666.commons.image.enums.WatermarkDirection;
import io.github.pangju666.commons.io.utils.FileUtils;
import org.apache.commons.lang3.Validate;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * 图片水印配置选项类
 * <p>
 * 用于配置视频图片水印的各项参数，包括相对尺寸、透明度、位置等。
 * 支持多种预设位置和自定义位置选项，水印尺寸按视频比例自适应调整。
 * </p>
 * <h3>核心特性</h3>
 * <ul>
 *     <li>支持相对视频尺寸自动缩放</li>
 *     <li>可配置水印透明度</li>
 *     <li>支持多种预定义水印位置</li>
 *     <li>支持自定义位置和边距</li>
 * </ul>
 * <h3>使用示例</h3>
 * <pre>{@code
 * ImageWatermarkOption option = new ImageWatermarkOption();
 * option.setOpacity(0.5f);
 * option.setRelativeScale(0.2);
 * option.setDirection(WatermarkDirection.BOTTOM_RIGHT);
 * }</pre>
 *
 * @author pangju666
 * @see TextWatermarkOption
 * @see io.github.pangju666.commons.image.enums.WatermarkDirection
 * @since 1.1.0
 */
public class ImageWatermarkOption {
	/**
	 * 水印相对于视频的相对尺寸，默认 0.15
	 * <p>表示水印宽度或高度占视频相应边的比例</p>
	 *
	 * @since 1.1.0
	 */
	protected double relativeScale = 0.15;

	/**
	 * 水印透明度，范围 0.0-1.0，默认 0.4
	 *
	 * @since 1.1.0
	 */
	protected float opacity = 0.4f;

	/**
	 * X 坐标位置，仅在未设置方向时生效
	 *
	 * @since 1.1.0
	 */
	protected int x = 0;

	/**
	 * Y 坐标位置，仅在未设置方向时生效
	 *
	 * @since 1.1.0
	 */
	protected int y = 0;

	/**
	 * 边距大小，默认 10
	 *
	 * @since 1.1.0
	 */
	protected int margin = 10;

	/**
	 * 水印位置方向，null 表示使用自定义坐标
	 *
	 * @since 1.1.0
	 */
	protected WatermarkDirection direction;

	/**
	 * 获取水印相对尺寸
	 *
	 * @return 相对尺寸比例值
	 * @since 1.1.0
	 */
	public double getRelativeScale() {
		return relativeScale;
	}

	/**
	 * 设置水印相对尺寸
	 *
	 * @param relativeScale 相对尺寸比例，必须大于 0
	 * @since 1.1.0
	 */
	public void setRelativeScale(double relativeScale) {
		if (relativeScale > 0) {
			this.relativeScale = relativeScale;
		}
	}

	/**
	 * 获取水印透明度
	 *
	 * @return 透明度值，范围 0.0-1.0
	 * @since 1.1.0
	 */
	public float getOpacity() {
		return opacity;
	}

	/**
	 * 设置水印透明度
	 *
	 * @param opacity 透明度值，范围 0.0-1.0
	 * @since 1.1.0
	 */
	public void setOpacity(float opacity) {
		if (opacity >= 0f && opacity <= 1) {
			this.opacity = opacity;
		}
	}

	/**
	 * 获取 X 坐标
	 *
	 * @return X 坐标值
	 * @since 1.1.0
	 */
	public int getX() {
		return x;
	}

	/**
	 * 设置 X 坐标
	 *
	 * @param x X 坐标值，必须大于等于 0
	 * @since 1.1.0
	 */
	public void setX(int x) {
		if (x >= 0) {
			this.x = x;
		}
	}

	/**
	 * 获取 Y 坐标
	 *
	 * @return Y 坐标值
	 * @since 1.1.0
	 */
	public int getY() {
		return y;
	}

	/**
	 * 设置 Y 坐标
	 *
	 * @param y Y 坐标值，必须大于等于 0
	 * @since 1.1.0
	 */
	public void setY(int y) {
		if (y >= 0) {
			this.y = y;
		}
	}

	/**
	 * 获取边距大小
	 *
	 * @return 边距值
	 * @since 1.1.0
	 */
	public int getMargin() {
		return margin;
	}

	/**
	 * 设置边距大小
	 *
	 * @param margin 边距值，必须大于等于 0
	 * @since 1.1.0
	 */
	public void setMargin(int margin) {
		if (margin >= 0) {
			this.margin = margin;
		}
	}

	/**
	 * 获取水印位置方向
	 *
	 * @return 位置方向枚举，null 表示使用自定义坐标
	 * @since 1.1.0
	 */
	public WatermarkDirection getDirection() {
		return direction;
	}

	/**
	 * 设置水印位置方向
	 *
	 * @param direction 位置方向枚举
	 * @since 1.1.0
	 */
	public void setDirection(WatermarkDirection direction) {
		if (Objects.nonNull(direction)) {
			this.direction = direction;
		}
	}

	/**
	 * 将图片水印配置转换为 FFmpeg 滤镜字符串
	 *
	 * @param imageFile 水印图片文件
	 * @param grabber   FFmpeg 帧抓取器，用于获取视频尺寸
	 * @return FFmpeg overlay 滤镜字符串
	 * @throws IOException              操作失败时抛出
	 * @throws IllegalArgumentException 当参数无效时抛出
	 * @since 1.1.0
	 */
	public String toFFmpegFilter(File imageFile, FFmpegFrameGrabber grabber) throws IOException {
		Validate.notNull(grabber, "grabber 不能为 null");

		if (FFmpegUtils.isNotStarted(grabber)) {
			grabber.start();
		}
		Validate.isTrue(grabber.hasVideo(), "grabber 不存在视频流");

		return toFFmpegFilter(imageFile, grabber.getImageWidth(), grabber.getImageHeight());
	}

	/**
	 * 将图片水印配置转换为 FFmpeg 滤镜字符串
	 *
	 * @param imageFile   水印图片文件
	 * @param videoWith   视频宽度
	 * @param videoHeight 视频高度
	 * @return FFmpeg overlay 滤镜字符串
	 * @throws IOException              操作失败时抛出
	 * @throws IllegalArgumentException 当参数无效时抛出
	 * @since 1.1.0
	 */
	public String toFFmpegFilter(File imageFile, int videoWith, int videoHeight) throws IOException {
		Validate.isTrue(videoWith > 0, "videoWith 必须大于0");
		Validate.isTrue(videoHeight > 0, "videoHeight 必须大于0");
		Validate.isTrue(FileUtils.isImageType(imageFile), "imageFile 不是图片文件");

		return FFmpegFiltersBuilder.video()
			.addFileSource("wm", imageFile)
			.appendAliasFilter("wm", "scale=" + (videoWith > videoHeight ?
				String.format("iw*%.2f:-1", relativeScale) : String.format("-1:ih*%.2f", relativeScale)))
			.appendAliasFilter("wm", "format=rgba")
			.appendAliasFilter("wm", "colorchannelmixer", "aa=" + opacity)
			.addGlobalFilter("overlay", computePositionArgs(), "format=auto")
			.build();
	}

	/**
	 * 计算位置参数
	 *
	 * @return FFmpeg overlay 滤镜的位置参数字符串
	 * @since 1.1.0
	 */
	protected String computePositionArgs() {
		if (Objects.isNull(direction)) {
			return String.format("x=%d:y=%d", x + margin, y + margin);
		}

		return switch (direction) {
			case TOP -> String.format("x=%s:y=%d", "(W-w)/2", margin);
			case TOP_LEFT -> String.format("x=%d:y=%d", margin, margin);
			case TOP_RIGHT -> String.format("x=%s:y=%d", "W-w-" + margin, margin);
			case BOTTOM -> String.format("x=%s:y=%s", "(W-w)/2", "H-h-" + margin);
			case BOTTOM_LEFT -> String.format("x=%d:y=%s", margin, "H-h-" + margin);
			case BOTTOM_RIGHT -> String.format("x=%s:y=%s", "W-w-" + margin, "H-h-" + margin);
			case LEFT -> String.format("x=%d:y=%s", margin, "(H-h)/2");
			case RIGHT -> String.format("x=%s:y=%s", "W-w-" + margin, "(H-h)/2");
			case CENTER -> String.format("x=%s:y=%s", "(W-w)/2", "(H-h)/2");
		};
	}
}