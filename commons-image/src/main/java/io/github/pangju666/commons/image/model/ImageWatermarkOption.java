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

import org.apache.commons.lang3.tuple.Pair;

import java.util.Objects;
import java.util.function.Function;

/**
 * 图像水印尺寸与透明度配置。
 *
 * <p>用于控制图片水印绘制时的缩放比例（相对原图尺寸）、透明度，以及水印尺寸的最小/最大限制策略。</p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class ImageWatermarkOption {
	/**
	 * 水印的相对缩放比例（相对原图尺寸）。
	 * 默认值：0.15；建议范围：[0.0, 1.0]
	 *
	 * @since 1.0.0
	 */
	private double relativeScale = 0.15;
	/**
	 * 水印透明度（百分比）。
	 * 默认值：0.4；范围：[0.0（完全透明）, 1.0（完全不透明）]
	 *
	 * @since 1.0.0
	 */
	private float opacity = 0.4f;

	/**
	 * 水印尺寸限制策略。
	 * <p>
	 * 根据目标图像的尺寸（{@link ImageSize}），计算出水印允许的最小尺寸和最大尺寸（{@link Pair}，左为最小，右为最大）。
	 * 默认策略根据图像短边长度分为三档：
	 * <ul>
	 *   <li>小图（短边 &lt; 600px）：最小 120x120，最大 150x150</li>
	 *   <li>大图（短边 &ge; 1920px）：最小 250x250，最大 400x400</li>
	 *   <li>中等图（其他）：最小 150x150，最大 250x250</li>
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
	 * @since 1.0.0
	 */
	public double getRelativeScale() {
		return relativeScale;
	}

	/**
	 * 设置水印的相对缩放比例（相对原图尺寸）。
	 * 必须为正数；非正数将被忽略并保持当前值。
	 * 该缩放与宽高范围共同作用，最终绘制尺寸会被限制在设定区间内。
	 *
	 * @param relativeScale 相对原图尺寸的缩放比例（&gt; 0）
	 * @since 1.0.0
	 */
	public void setRelativeScale(double relativeScale) {
		if (relativeScale > 0) {
			this.relativeScale = relativeScale;
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
}
