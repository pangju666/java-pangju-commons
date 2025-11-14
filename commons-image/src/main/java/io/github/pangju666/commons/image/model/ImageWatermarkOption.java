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
	 * 水印最小宽度（像素）。默认：200
	 *
	 * @since 1.0.0
	 */
	private int minWidth = 200;
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
	 * 水印最大高度（像素）。默认：40
	 *
	 * @since 1.0.0
	 */
	private int maxHeight = 40;

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public float getOpacity() {
		return opacity;
	}

	public void setOpacity(float opacity) {
		this.opacity = opacity;
	}

	public int getMinWidth() {
		return minWidth;
	}

	public void setMinWidth(int minWidth) {
		this.minWidth = minWidth;
	}

	public int getMaxWidth() {
		return maxWidth;
	}

	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
	}

	public int getMinHeight() {
		return minHeight;
	}

	public void setMinHeight(int minHeight) {
		this.minHeight = minHeight;
	}

	public int getMaxHeight() {
		return maxHeight;
	}

	public void setMaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
	}
}
