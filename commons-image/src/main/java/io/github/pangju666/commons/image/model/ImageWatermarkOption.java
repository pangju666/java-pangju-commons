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

/**
 * 图像水印尺寸与透明度配置。
 *
 * <p>用于控制图片水印绘制时的缩放比例（相对原图尺寸）、透明度，以及水印尺寸的最小/最大限制。</p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class ImageWatermarkOption {
	/**
	 * 水印的相对缩放比例（相对原图尺寸）。
	 * 默认值：0.15f；建议范围：[0.0f, 1.0f]
	 *
	 * @since 1.0.0
	 */
	private float scale = 0.15f;
	/**
	 * 水印透明度（Alpha）。
	 * 默认值：0.4f；范围：[0.0f（完全透明）, 1.0f（完全不透明）]
	 *
	 * @since 1.0.0
	 */
	private float opacity = 0.4f;
	/**
	 * 水印最小宽度（像素）。默认：40
	 *
	 * @since 1.0.0
	 */
	private int minWidth = 40;
	/**
	 * 水印最大宽度（像素）。默认：200
	 *
	 * @since 1.0.0
	 */
	private int maxWidth = 200;
	/**
	 * 水印最小高度（像素）。默认：40
	 *
	 * @since 1.0.0
	 */
	private int minHeight = 40;
	/**
	 * 水印最大高度（像素）。默认：200
	 *
	 * @since 1.0.0
	 */
	private int maxHeight = 200;

	public float getScale() {
		return scale;
	}

	/**
	 * 设置水印的相对缩放比例（相对原图尺寸）。
	 * 必须为正数；非正数将被忽略并保持当前值。
	 * 该缩放与宽高范围共同作用，最终绘制尺寸会被限制在设定区间内。
	 *
	 * @param scale 缩放比例（> 0）
	 * @since 1.0.0
	 */
	public void setScale(float scale) {
		if (scale > 0) {
			this.scale = scale;
		}
	}

	public float getOpacity() {
		return opacity;
	}

	/**
	 * 设置水印透明度（Alpha）。
	 * 有效范围为 {@code (0.0f, 1.0f)}；越界值将被忽略并保持当前值。
	 * 其中 0 表示完全透明，1 表示完全不透明。
	 *
	 * @param opacity 透明度
	 * @since 1.0.0
	 */
	public void setOpacity(float opacity) {
		if (opacity > 0f && opacity < 1) {
			this.opacity = opacity;
		}
	}

	public int getMinWidth() {
		return minWidth;
	}

	/**
	 * 设置水印宽度的有效范围（像素）。
	 * 两个参数都必须为正数，且 {@code maxWidth >= minWidth}；
	 * 无效参数将被忽略并保持当前范围。
	 *
	 * @param minWidth 最小宽度（像素）
	 * @param maxWidth 最大宽度（像素）
	 * @since 1.0.0
	 */
	public void setWidthRange(int minWidth, int maxWidth) {
		if (minWidth > 0 && maxWidth > 0 && maxWidth >= minWidth) {
			this.minWidth = minWidth;
			this.maxWidth = maxWidth;
		}
	}

	public int getMaxWidth() {
		return maxWidth;
	}

	public int getMinHeight() {
		return minHeight;
	}

	/**
	 * 设置水印高度的有效范围（像素）。
	 * 两个参数都必须为正数，且 {@code maxHeight >= minHeight}；
	 * 无效参数将被忽略并保持当前范围。
	 *
	 * @param minHeight 最小高度（像素）
	 * @param maxHeight 最大高度（像素）
	 * @since 1.0.0
	 */
	public void setHeightRange(int minHeight, int maxHeight) {
		if (minHeight > 0 && maxHeight > 0 && maxHeight >= minHeight) {
			this.minHeight = minHeight;
			this.maxHeight = maxHeight;
		}
	}

	public int getMaxHeight() {
		return maxHeight;
	}
}
