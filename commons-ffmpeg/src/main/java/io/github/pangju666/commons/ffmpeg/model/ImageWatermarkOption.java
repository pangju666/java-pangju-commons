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
import io.github.pangju666.commons.image.model.ImageSize;
import io.github.pangju666.commons.io.utils.FileUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.function.BiFunction;

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
 * option.setRelativeScaleFactor(0.2);
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
	private double relativeScaleFactor = 0.15;

	/**
	 * 水印透明度，范围 0.0-1.0，默认 0.4
	 *
	 * @since 1.1.0
	 */
	private float opacity = 0.4f;

	/**
	 * X 坐标位置，仅在未设置方向时生效
	 *
	 * @since 1.1.0
	 */
	private int x = 0;

	/**
	 * Y 坐标位置，仅在未设置方向时生效
	 *
	 * @since 1.1.0
	 */
	private int y = 0;

	/**
	 * 边距大小，默认 10
	 *
	 * @since 1.1.0
	 */
	private int inset = 10;

	/**
	 * 水印位置方向，null 表示使用自定义坐标
	 *
	 * @since 1.1.0
	 */
	private WatermarkDirection direction;

	/**
	 * 水印尺寸限制策略。
	 * <p>
	 * 根据目标视频的画面尺寸，计算出水印允许的最小尺寸和最大尺寸（{@link Pair}，左为最小，右为最大）。
	 * 默认策略根据视频画面尺寸短边长度分为三档：
	 * <ul>
	 *   <li>小尺寸视频（短边 &lt; 600px）：最小 120x120，最大 150x150</li>
	 *   <li>大尺寸视频（短边 &ge; 1920px）：最小 250x250，最大 400x400</li>
	 *   <li>中等尺寸视频（其他）：最小 150x150，最大 250x250</li>
	 * </ul>
	 * </p>
	 *
	 * @since 1.1.0
	 */
	private BiFunction<Integer, Integer, Pair<ImageSize, ImageSize>> sizeLimitStrategy = (width, height) -> {
		int shorter = Math.min(width, height);
		if (shorter < 600) { // 小图
			return Pair.of(new ImageSize(120, 120), new ImageSize(150, 150));
		} else if (shorter >= 1920) { // 大图（注意：>=1920）
			return Pair.of(new ImageSize(250, 250), new ImageSize(400, 400));
		} else { // 中等图
			return Pair.of(new ImageSize(150, 150), new ImageSize(250, 250));
		}
	};

	/**
	 * 获取水印相对尺寸
	 *
	 * @return 相对尺寸比例值
	 * @since 1.1.0
	 */
	public double getRelativeScaleFactor() {
		return relativeScaleFactor;
	}

	/**
	 * 设置水印相对尺寸
	 *
	 * @param relativeScaleFactor 相对尺寸比例，必须大于 0
	 * @since 1.1.0
	 */
	public void setRelativeScaleFactor(double relativeScaleFactor) {
		if (relativeScaleFactor > 0) {
			this.relativeScaleFactor = relativeScaleFactor;
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
	public int getInset() {
		return inset;
	}

	/**
	 * 设置边距大小
	 *
	 * @param inset 边距值，必须大于等于 0
	 * @since 1.1.0
	 */
	public void setInset(int inset) {
		if (inset >= 0) {
			this.inset = inset;
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
	 * 获取当前的水印尺寸限制策略。
	 *
	 * @return 计算最小/最大尺寸的策略函数
	 * @since 1.1.0
	 */
	public BiFunction<Integer, Integer, Pair<ImageSize, ImageSize>> getSizeLimitStrategy() {
		return sizeLimitStrategy;
	}

	/**
	 * 设置水印尺寸限制策略。
	 * <p>
	 * 允许自定义策略，根据目标图像尺寸动态决定水印的尺寸上下限。
	 * </p>
	 *
	 * @param sizeLimitStrategy 水印尺寸限制策略，不能为 null；如果为 null 则忽略并保持原策略
	 * @since 1.1.0
	 */
	public void setSizeLimitStrategy(BiFunction<Integer, Integer, Pair<ImageSize, ImageSize>> sizeLimitStrategy) {
		if (Objects.nonNull(sizeLimitStrategy)) {
			this.sizeLimitStrategy = sizeLimitStrategy;
		}
	}

	/**
	 * 获取当前的水印尺寸限制策略。
	 *
	 * @return 计算最小/最大尺寸的策略函数
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
	 * @param watermarkImage   水印图片文件
	 * @param videoWith   视频宽度
	 * @param videoHeight 视频高度
	 * @return FFmpeg overlay 滤镜字符串
	 * @throws IOException              操作失败时抛出
	 * @throws IllegalArgumentException 当参数无效时抛出
	 * @since 1.1.0
	 */
	public String toFFmpegFilter(File watermarkImage, int videoWith, int videoHeight) throws IOException {
		Validate.isTrue(videoWith > 0, "videoWith 必须大于0");
		Validate.isTrue(videoHeight > 0, "videoHeight 必须大于0");
		Validate.isTrue(FileUtils.isImageType(watermarkImage), "imageFile 不是图片文件");

		Pair<ImageSize, ImageSize> watermarkImageSizeRange = sizeLimitStrategy.apply(videoWith, videoHeight);

		return FFmpegFiltersBuilder.video()
			.addFileSource("wm", watermarkImage)
			.appendAliasFilter("wm", "scale", String.format(
				"w='if(gt(iw,ih),min(%d\\,max(%d\\,iw*%.2f)),-1)':h='if(gt(ih,iw),min(%d\\,max(%d\\,ih*%.2f)),-1)'",
				watermarkImageSizeRange.getRight().getWidth(),
				watermarkImageSizeRange.getLeft().getWidth(),
				relativeScaleFactor,
				watermarkImageSizeRange.getRight().getHeight(),
				watermarkImageSizeRange.getLeft().getHeight(),
				relativeScaleFactor))
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
			return String.format("x=%d:y=%d", x + inset, y + inset);
		}

		return switch (direction) {
			case TOP -> String.format("x=%s:y=%d", "(W-w)/2", inset);
			case TOP_LEFT -> String.format("x=%d:y=%d", inset, inset);
			case TOP_RIGHT -> String.format("x=%s:y=%d", "W-w-" + inset, inset);
			case BOTTOM -> String.format("x=%s:y=%s", "(W-w)/2", "H-h-" + inset);
			case BOTTOM_LEFT -> String.format("x=%d:y=%s", inset, "H-h-" + inset);
			case BOTTOM_RIGHT -> String.format("x=%s:y=%s", "W-w-" + inset, "H-h-" + inset);
			case LEFT -> String.format("x=%d:y=%s", inset, "(H-h)/2");
			case RIGHT -> String.format("x=%s:y=%s", "W-w-" + inset, "(H-h)/2");
			case CENTER -> String.format("x=%s:y=%s", "(W-w)/2", "(H-h)/2");
		};
	}
}