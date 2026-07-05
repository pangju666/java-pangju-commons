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

package io.github.pangju666.commons.image.model;

import com.twelvemonkeys.image.ImageUtil;
import net.coobird.thumbnailator.filters.Watermark;
import net.coobird.thumbnailator.geometry.Coordinate;
import net.coobird.thumbnailator.geometry.Position;
import net.coobird.thumbnailator.geometry.Positions;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.function.Function;

/**
 * 图像水印配置。
 *
 * <p>用于控制图片水印绘制时的缩放比例（相对原图尺寸）、透明度、边距、位置（方向或自定义坐标），以及水印尺寸的最小/最大限制策略。</p>
 *
 * <p>水印位置支持两种方式：</p>
 * <ul>
 *   <li>通过 {@link Positions} 设置九宫格方向位置</li>
 *   <li>通过自定义坐标 x/y 精确设置位置</li>
 * </ul>
 *
 * <p>水印尺寸计算流程：</p>
 * <ol>
 *   <li>根据 {@code relativeScaleFactor} 计算初始目标尺寸</li>
 *   <li>根据 {@code sizeLimitStrategy} 计算允许的最小/最大尺寸</li>
 *   <li>将初始目标尺寸限制在允许的范围内</li>
 *   <li>根据水印宽高比选择以宽度或高度为基准进行缩放</li>
 * </ol>
 *
 * @author pangju666
 * @see #toWatermark(ImageSize, BufferedImage)
 * @see Positions
 * @since 1.0.0
 */
public class ImageWatermarkOption {
	/**
	 * 水印的相对缩放比例（相对原图尺寸）。
	 * 默认值：0.15；建议范围：[0.0, 1.0]
	 *
	 * @since 1.1.0
	 */
	private double relativeScaleFactor = 0.15;

	/**
	 * 水印透明度（百分比）。
	 * 默认值：0.4；范围：[0.0（完全透明）, 1.0（完全不透明）]
	 *
	 * @since 1.0.0
	 */
	private float opacity = 0.4f;

	/**
	 * 边距大小，默认 10
	 *
	 * @since 1.1.0
	 */
	private int margin = 10;

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
	 * 水印位置方向，null 表示使用自定义坐标
	 *
	 * @since 1.1.0
	 */
	private Positions direction;

	/**
	 * 水印尺寸限制策略。
	 * <p>
	 * 根据目标图像的尺寸（{@link ImageSize}），计算出水印允许的最小尺寸和最大尺寸（{@link Pair}，左为最小，右为最大）。
	 * 默认策略根据图像短边长度分为三档：
	 * <ul>
	 *   <li>小图（短边 &lt; 600px）：最小 120x120，最大 150x150</li>
	 *   <li>中等图（600px &le; 短边 &lt; 1920px）：最小 150x150，最大 250x250</li>
	 *   <li>大图（短边 &ge; 1920px）：最小 250x250，最大 400x400</li>
	 * </ul>
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private Function<ImageSize, Pair<ImageSize, ImageSize>> sizeLimitStrategy = imageSize -> {
		int shorter = Math.min(imageSize.getWidth(), imageSize.getHeight());
		if (shorter < 600) { // 小图
			return Pair.of(new ImageSize(120, 120), new ImageSize(150, 150));
		} else if (shorter >= 1920) { // 大图（注意：>=1920）
			return Pair.of(new ImageSize(250, 250), new ImageSize(400, 400));
		} else { // 中等图
			return Pair.of(new ImageSize(150, 150), new ImageSize(250, 250));
		}
	};

	/**
	 * 获取水印的相对缩放比例。
	 *
	 * @return 相对缩放比例（如 0.15 表示水印大小约为原图的 15%）
	 * @since 1.1.0
	 */
	public double getRelativeScaleFactor() {
		return relativeScaleFactor;
	}

	/**
	 * 设置水印的相对缩放比例（相对原图尺寸）。
	 * 必须为正数；非正数将被忽略并保持当前值。
	 * 该缩放与宽高范围共同作用，最终绘制尺寸会被限制在设定区间内。
	 *
	 * @param relativeScaleFactor 相对原图尺寸的缩放比例（&gt; 0）
	 * @since 1.1.0
	 */
	public void setRelativeScaleFactor(double relativeScaleFactor) {
		if (relativeScaleFactor > 0) {
			this.relativeScaleFactor = relativeScaleFactor;
		}
	}

	/**
	 * 获取水印的相对缩放比例。
	 *
	 * @return 相对缩放比例（如 0.15 表示水印大小约为原图的 15%）
	 * @since 1.0.0
	 * @deprecated 请使用 {@link #getRelativeScaleFactor} 替代
	 */
	@Deprecated(forRemoval = true, since = "1.1.0")
	public double getRelativeScale() {
		return relativeScaleFactor;
	}

	/**
	 * 设置水印的相对缩放比例（相对原图尺寸）。
	 * 必须为正数；非正数将被忽略并保持当前值。
	 * 该缩放与宽高范围共同作用，最终绘制尺寸会被限制在设定区间内。
	 *
	 * @param relativeScale 相对原图尺寸的缩放比例（&gt; 0）
	 * @since 1.0.0
	 * @deprecated 请使用 {@link #setRelativeScaleFactor} 替代
	 */
	@Deprecated(forRemoval = true, since = "1.1.0")
	public void setRelativeScale(double relativeScale) {
		if (relativeScale > 0) {
			this.relativeScaleFactor = relativeScale;
		}
	}

	/**
	 * 获取水印透明度。
	 *
	 * @return 透明度值（0.0 - 1.0）
	 * @since 1.0.0
	 */
	public float getOpacity() {
		return opacity;
	}

	/**
	 * 设置水印透明度（百分比）。
	 * 有效范围为 {@code [0.0, 1.0]}；越界值将被忽略并保持当前值。
	 * 其中 0 表示完全透明，1 表示完全不透明。
	 *
	 * @param opacity 透明度
	 * @since 1.0.0
	 */
	public void setOpacity(float opacity) {
		if (opacity >= 0f && opacity <= 1) {
			this.opacity = opacity;
		}
	}

	/**
	 * 获取当前的水印尺寸限制策略。
	 *
	 * @return 计算最小/最大尺寸的策略函数
	 * @since 1.0.0
	 */
	public Function<ImageSize, Pair<ImageSize, ImageSize>> getSizeLimitStrategy() {
		return sizeLimitStrategy;
	}

	/**
	 * 设置水印尺寸限制策略。
	 * <p>
	 * 允许自定义策略，根据目标图像尺寸动态决定水印的尺寸上下限。
	 * </p>
	 *
	 * @param sizeLimitStrategy 水印尺寸限制策略，不能为 null；如果为 null 则忽略并保持原策略
	 * @since 1.0.0
	 */
	public void setSizeLimitStrategy(Function<ImageSize, Pair<ImageSize, ImageSize>> sizeLimitStrategy) {
		if (Objects.nonNull(sizeLimitStrategy)) {
			this.sizeLimitStrategy = sizeLimitStrategy;
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
	 * 获取 X 坐标位置
	 *
	 * @return X 坐标值
	 * @since 1.1.0
	 */
	public int getX() {
		return x;
	}

	/**
	 * 设置 X 坐标位置，仅在未设置方向时生效
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
	 * 获取 Y 坐标位置
	 *
	 * @return Y 坐标值
	 * @since 1.1.0
	 */
	public int getY() {
		return y;
	}

	/**
	 * 设置 Y 坐标位置，仅在未设置方向时生效
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
	 * 获取水印位置方向
	 *
	 * @return 水印方向，null 表示使用自定义坐标
	 * @since 1.1.0
	 */
	public Positions getDirection() {
		return direction;
	}

	/**
	 * 设置水印位置方向，设置为 null 表示使用自定义坐标
	 *
	 * @param direction 水印方向
	 * @since 1.1.0
	 */
	public void setDirection(Positions direction) {
		this.direction = direction;
	}

	/**
	 * 根据目标图像尺寸和水印图像创建 Watermark 对象
	 * <p>
	 * 水印尺寸会根据相对缩放比例和尺寸限制策略进行调整。
	 * 如果设置了方向，则直接使用该方向；否则使用自定义坐标。
	 * </p>
	 *
	 * <p>尺寸计算逻辑：</p>
	 * <ol>
	 *   <li>首先根据 {@code relativeScaleFactor} 计算初始目标尺寸</li>
	 *   <li>使用 {@code sizeLimitStrategy} 获取允许的最小和最大尺寸</li>
	 *   <li>根据水印宽高比选择主维度：宽&gt;高时以宽度为基准，否则以高度为基准</li>
	 *   <li>将目标尺寸限制在最小和最大尺寸范围内</li>
	 *   <li>如果需要调整尺寸，则重新采样水印图像</li>
	 * </ol>
	 *
	 * @param targetImageSize 目标图像尺寸
	 * @param watermarkImage  水印图像
	 * @return 配置好的 Watermark 对象
	 * @throws IllegalArgumentException 如果任一参数为 null
	 * @since 1.1.0
	 */
	public Watermark toWatermark(ImageSize targetImageSize, BufferedImage watermarkImage) {
		Validate.notNull(watermarkImage, "watermarkImage 不可为 null");
		Validate.notNull(targetImageSize, "targetImageSize 不可为 null");

		Pair<ImageSize, ImageSize> watermarkImageSizeRange = sizeLimitStrategy.apply(targetImageSize);
		ImageSize originalWatermarkSize = new ImageSize(watermarkImage.getWidth(), watermarkImage.getHeight());

		BufferedImage targetWatermarkImage = watermarkImage;
		ImageSize targetWatermarkImageSize = targetImageSize.scale(relativeScaleFactor);

		if (originalWatermarkSize.getWidth() > originalWatermarkSize.getHeight()) {
			int targetWidth = Math.min(watermarkImageSizeRange.getRight().getWidth(),
				Math.max(watermarkImageSizeRange.getLeft().getWidth(), targetWatermarkImageSize.getWidth()));
			if (targetWidth != targetWatermarkImageSize.getWidth()) {
				targetWatermarkImageSize = originalWatermarkSize.scaleByWidth(targetWidth);
				targetWatermarkImage = ImageUtil.createResampled(watermarkImage,
					targetWatermarkImageSize.getWidth(), targetWatermarkImageSize.getHeight(),
					Image.SCALE_DEFAULT);
			}
		} else {
			int targetHeight = Math.min(watermarkImageSizeRange.getRight().getHeight(),
				Math.max(watermarkImageSizeRange.getLeft().getHeight(), targetWatermarkImageSize.getHeight()));
			if (targetHeight != targetWatermarkImageSize.getHeight()) {
				targetWatermarkImageSize = originalWatermarkSize.scaleByHeight(targetHeight);
				targetWatermarkImage = ImageUtil.createResampled(watermarkImage,
					targetWatermarkImageSize.getWidth(), targetWatermarkImageSize.getHeight(),
					Image.SCALE_DEFAULT);
			}
		}

		Position coordinate;
		if (Objects.nonNull(direction)) {
			coordinate = direction;
		} else {
			int x = Math.max(0, Math.min(targetImageSize.getWidth() - targetWatermarkImage.getWidth(), this.x));
			int y = Math.max(0, Math.min(targetImageSize.getHeight() - targetWatermarkImage.getHeight(), this.y));
			coordinate = new Coordinate(x, y);
		}

		return new Watermark(coordinate, targetWatermarkImage, opacity, margin);
	}
}