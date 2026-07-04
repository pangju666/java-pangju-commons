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
 *
 * <p>提供两种坐标计算方法：</p>
 * <ul>
 *   <li>{@link #toCaptionCoordinate(ImageSize, int)}：为文字水印计算坐标</li>
 *   <li>{@link #toWatermarkCoordinate(ImageSize, ImageSize)}：为图像水印计算坐标</li>
 * </ul>
 *
 * <p>注意：所有返回的坐标都基于 Thumbnailator 坐标系（Y 轴向下为正）。</p>
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
	 * 根据图像尺寸计算文字水印（Caption）的坐标位置。
	 * <p>
	 * 注意：返回的坐标是相对于 Thumbnailator 坐标系的，Y 轴向下为正。
	 * 对于文字水印，坐标会根据边距进行调整。
	 * </p>
	 *
	 * @param imageSize 目标图像尺寸，不能为 null
	 * @param margin    水印边距
	 * @return 文字水印的坐标位置
	 * @since 1.1.0
	 */
	public Coordinate toCaptionCoordinate(ImageSize imageSize, int margin) {
		switch (this) {
			case TOP:
				return new Coordinate((imageSize.getWidth()) / 2, margin);
			case TOP_LEFT:
				return new Coordinate(0, margin);
			case TOP_RIGHT:
				return new Coordinate(imageSize.getWidth(), margin);
			case BOTTOM:
				return new Coordinate((imageSize.getWidth()) / 2, imageSize.getHeight() - margin);
			case BOTTOM_LEFT:
				return new Coordinate(0, imageSize.getHeight() - margin);
			case BOTTOM_RIGHT:
				return new Coordinate(imageSize.getWidth(), imageSize.getHeight() - margin);
			case LEFT:
				return new Coordinate(0, (imageSize.getHeight()) / 2);
			case RIGHT:
				return new Coordinate(imageSize.getWidth(),
					(imageSize.getHeight()) / 2);
			case CENTER:
			default:
				return new Coordinate((imageSize.getWidth()) / 2,
					(imageSize.getHeight()) / 2);
		}
	}

	/**
	 * 根据图像尺寸和水印宽高计算图像水印的坐标位置。
	 * <p>
	 * 注意：返回的坐标是相对于 Thumbnailator 坐标系的，Y 轴向下为正。
	 * 对于图像水印，坐标会根据水印尺寸进行调整，确保水印正确定位。
	 * </p>
	 *
	 * @param imageSize          目标图像尺寸，不能为 null
	 * @param watermarkImageSize 水印图像尺寸，不能为 null
	 * @return 图像水印的坐标位置
	 * @since 1.1.0
	 */
	public Coordinate toWatermarkCoordinate(ImageSize imageSize, ImageSize watermarkImageSize) {
		switch (this) {
			case TOP:
				return new Coordinate((imageSize.getWidth() - watermarkImageSize.getWidth()) / 2,
					watermarkImageSize.getHeight());
			case TOP_LEFT:
				return new Coordinate(0, watermarkImageSize.getHeight());
			case TOP_RIGHT:
				return new Coordinate(imageSize.getWidth() - watermarkImageSize.getWidth(),
					watermarkImageSize.getHeight());
			case BOTTOM:
				return new Coordinate((imageSize.getWidth() - watermarkImageSize.getWidth()) / 2,
					imageSize.getHeight());
			case BOTTOM_LEFT:
				return new Coordinate(0, imageSize.getHeight());
			case BOTTOM_RIGHT:
				return new Coordinate(imageSize.getWidth() - watermarkImageSize.getWidth(),
					imageSize.getHeight());
			case LEFT:
				return new Coordinate(0, (imageSize.getHeight() - watermarkImageSize.getHeight()) / 2);
			case RIGHT:
				return new Coordinate(imageSize.getWidth() - watermarkImageSize.getWidth(),
					(imageSize.getHeight() - watermarkImageSize.getHeight()) / 2);
			case CENTER:
			default:
				return new Coordinate((imageSize.getWidth() - watermarkImageSize.getWidth()) / 2,
					(imageSize.getHeight() - watermarkImageSize.getHeight()) / 2);
		}
	}
}