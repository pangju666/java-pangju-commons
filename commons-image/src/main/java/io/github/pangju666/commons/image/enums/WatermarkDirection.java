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

package io.github.pangju666.commons.image.enums;

import io.github.pangju666.commons.image.model.ImageSize;
import net.coobird.thumbnailator.geometry.Coordinate;

/**
 * 水印方向枚举，定义水印在图像中的九宫格位置。
 *
 * <p>用于在图像处理时确定水印绘制的参考位置，例如左上、居中、右下等。</p>
 * <p>提供 toCoordinate 方法，可根据目标图像尺寸和水印尺寸计算水印的具体坐标位置。</p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public enum WatermarkDirection {
	/**
	 * 左上角，紧贴上边与左边。
	 *
	 * @since 1.0.0
	 */
	TOP_LEFT,
	/**
	 * 顶部居中，紧贴上边并水平居中。
	 *
	 * @since 1.0.0
	 */
	TOP,
	/**
	 * 右上角，紧贴上边与右边。
	 *
	 * @since 1.0.0
	 */
	TOP_RIGHT,
	/**
	 * 右侧居中，紧贴右边并垂直居中。
	 *
	 * @since 1.0.0
	 */
	RIGHT,
	/**
	 * 中心位置，水平与垂直居中。
	 *
	 * @since 1.0.0
	 */
	CENTER,
	/**
	 * 左侧居中，紧贴左边并垂直居中。
	 *
	 * @since 1.0.0
	 */
	LEFT,
	/**
	 * 底部居中，紧贴下边并水平居中。
	 *
	 * @since 1.0.0
	 */
	BOTTOM,
	/**
	 * 右下角，紧贴下边与右边。
	 *
	 * @since 1.0.0
	 */
	BOTTOM_RIGHT,
	/**
	 * 左下角，紧贴下边与左边。
	 *
	 * @since 1.0.0
	 */
	BOTTOM_LEFT;

	/**
	 * 根据图像尺寸和水印尺寸计算水印坐标位置。
	 *
	 * @param imageSize          目标图像尺寸
	 * @param watermarkImageSize 水印图像尺寸
	 * @return 水印的坐标位置
	 * @throws IllegalArgumentException 如果任一参数为 null
	 * @since 1.1.0
	 */
	public Coordinate toCoordinate(ImageSize imageSize, ImageSize watermarkImageSize) {
		return toCoordinate(imageSize, watermarkImageSize.getWidth(), watermarkImageSize.getHeight());
	}

	/**
	 * 根据图像尺寸和水印宽高计算水印坐标位置。
	 *
	 * @param imageSize       目标图像尺寸
	 * @param watermarkWidth  水印宽度
	 * @param watermarkHeight 水印高度
	 * @return 水印的坐标位置
	 * @throws IllegalArgumentException 如果 imageSize 为 null
	 * @since 1.1.0
	 */
	public Coordinate toCoordinate(ImageSize imageSize, int watermarkWidth, int watermarkHeight) {
		return switch (this) {
			case TOP -> new Coordinate((imageSize.getWidth() - watermarkWidth) / 2, 0);
			case TOP_LEFT -> new Coordinate(0, 0);
			case TOP_RIGHT -> new Coordinate(imageSize.getWidth() - watermarkWidth, 0);
			case BOTTOM -> new Coordinate((imageSize.getWidth() - watermarkWidth) / 2,
				imageSize.getHeight() - watermarkHeight);
			case BOTTOM_LEFT -> new Coordinate(0, imageSize.getHeight() - watermarkHeight);
			case BOTTOM_RIGHT -> new Coordinate(imageSize.getWidth() - watermarkWidth,
				imageSize.getHeight() - watermarkHeight);
			case LEFT -> new Coordinate(0, (imageSize.getHeight() - watermarkHeight) / 2);
			case RIGHT -> new Coordinate(imageSize.getWidth() - watermarkWidth,
				(imageSize.getHeight() - watermarkHeight) / 2);
			case CENTER -> new Coordinate((imageSize.getWidth() - watermarkWidth) / 2,
				(imageSize.getHeight() - watermarkHeight) / 2);
		};
	}
}