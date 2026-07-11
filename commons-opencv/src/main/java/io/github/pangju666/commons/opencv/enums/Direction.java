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

package io.github.pangju666.commons.opencv.enums;

import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Size;

/**
 * 位置方向枚举
 *
 * <p>提供九宫格位置定位。</p>
 *
 * @author pangju666
 * @since 1.1.0
 */
public enum Direction {
	/**
	 * 左上角
	 *
	 * @since 1.1.0
	 */
	TOP_LEFT,

	/**
	 * 顶部居中
	 *
	 * @since 1.1.0
	 */
	TOP,

	/**
	 * 右上角
	 *
	 * @since 1.1.0
	 */
	TOP_RIGHT,

	/**
	 * 右侧居中
	 *
	 * @since 1.1.0
	 */
	RIGHT,

	/**
	 * 中心
	 *
	 * @since 1.1.0
	 */
	CENTER,

	/**
	 * 左侧居中
	 *
	 * @since 1.1.0
	 */
	LEFT,

	/**
	 * 底部居中
	 *
	 * @since 1.1.0
	 */
	BOTTOM,

	/**
	 * 右下角
	 *
	 * @since 1.1.0
	 */
	BOTTOM_RIGHT,

	/**
	 * 左下角
	 *
	 * @since 1.1.0
	 */
	BOTTOM_LEFT;

	/**
	 * 根据位置方向计算图像水印的矩形区域
	 *
	 * <p>该方法返回水印在目标图像中的放置位置（Rect），考虑了指定的边距。</p>
	 * <p>返回的矩形包含水印的起始坐标（x, y）和尺寸（width, height）。</p>
	 *
	 * @param imageSize          目标图像尺寸
	 * @param watermarkImageSize 水印图像尺寸
	 * @param margin             水印与图像边缘的距离（单位：像素）
	 * @return 水印放置位置的矩形
	 * @since 1.1.0
	 */
	public Rect toImageWatermarkRect(Size imageSize, Size watermarkImageSize, int margin) {
		switch (this) {
			case TOP:
				return new Rect((imageSize.width() - watermarkImageSize.width()) / 2, margin,
					watermarkImageSize.width(), watermarkImageSize.height());
			case TOP_LEFT:
				return new Rect(margin, margin, watermarkImageSize.width(), watermarkImageSize.height());
			case TOP_RIGHT:
				return new Rect(imageSize.width() - watermarkImageSize.width() - margin, margin,
					watermarkImageSize.width(), watermarkImageSize.height());
			case BOTTOM:
				return new Rect((imageSize.width() - watermarkImageSize.width()) / 2,
					imageSize.height() - watermarkImageSize.height() - margin,
					watermarkImageSize.width(), watermarkImageSize.height());
			case BOTTOM_LEFT:
				return new Rect(margin, imageSize.height() - watermarkImageSize.height() - margin,
					watermarkImageSize.width(), watermarkImageSize.height());
			case BOTTOM_RIGHT:
				return new Rect(imageSize.width() - watermarkImageSize.width() - margin,
					imageSize.height() - watermarkImageSize.height() - margin, watermarkImageSize.width(),
					watermarkImageSize.height());
			case LEFT:
				return new Rect(margin, (imageSize.height() - watermarkImageSize.height()) / 2,
					watermarkImageSize.width(), watermarkImageSize.height());
			case RIGHT:
				return new Rect(imageSize.width() - watermarkImageSize.width() - margin,
					(imageSize.height() - watermarkImageSize.height()) / 2, watermarkImageSize.width(),
					watermarkImageSize.height());
			default:
				return new Rect((imageSize.width() - watermarkImageSize.width()) / 2,
					(imageSize.height() - watermarkImageSize.height()) / 2, watermarkImageSize.width(),
					watermarkImageSize.height());
		}
	}
}