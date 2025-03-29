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

import org.apache.commons.lang3.Validate;

/**
 * 图像尺寸记录类
 * <p>表示经过方向校正后的图像实际显示尺寸，包含宽度和高度两个不可变属性</p>
 * <p>本类提供多种尺寸缩放计算方法，所有计算结果均保持原始宽高比</p>
 *
 * @param width  实际显示宽度（像素，必须 > 0）
 * @param height 实际显示高度（像素，必须 > 0）
 * @author pangju666
 * @since 1.0.0
 */
public record ImageSize(int width, int height) {
	/**
	 * 根据目标宽度等比缩放
	 * <p>算法说明：</p>
	 * <ol>
	 *   <li>当原始宽>高时：保持宽高比，高度按比例缩小</li>
	 *   <li>当原始宽≤高时：保持宽高比，高度按比例放大</li>
	 *   <li>计算结果像素值不小于1</li>
	 * </ol>
	 *
	 * @param targetWidth 目标宽度（必须 > 0）
	 * @return 等比缩放后的新尺寸对象
	 * @throws IllegalArgumentException 当targetWidth ≤ 0时抛出
	 * @since 1.0.0
	 */
	public ImageSize scaleByWidth(final int targetWidth) {
		Validate.isTrue(targetWidth > 0, "targetWidth 必须大于0");

		if (width > height) {
			double ratio = (double) width / height;
			return new ImageSize(targetWidth, (int) Math.max(targetWidth / ratio, 1));
		}

		double ratio = (double) height / width;
		return new ImageSize(targetWidth, (int) Math.max(targetWidth * ratio, 1));
	}

	/**
	 * 根据目标高度等比缩放
	 * <p>算法说明：</p>
	 * <ol>
	 *   <li>当原始宽>高时：保持宽高比，宽度按比例缩小</li>
	 *   <li>当原始宽≤高时：保持宽高比，宽度按比例放大</li>
	 *   <li>计算结果像素值不小于1</li>
	 * </ol>
	 *
	 * @param targetHeight 目标高度（必须 > 0）
	 * @return 等比缩放后的新尺寸对象
	 * @throws IllegalArgumentException 当targetHeight ≤ 0时抛出
	 * @since 1.0.0
	 */
	public ImageSize scaleByHeight(final int targetHeight) {
		Validate.isTrue(targetHeight > 0, "inputImage 必须大于0");

		if (width > height) {
			double ratio = (double) width / height;
			return new ImageSize((int) Math.max(targetHeight * ratio, 1), targetHeight);
		}

		double ratio = (double) height / width;
		return new ImageSize((int) Math.max(targetHeight / ratio, 1), targetHeight);
	}

	/**
	 * 双约束等比缩放
	 * <p>算法说明：</p>
	 * <ol>
	 *   <li>优先保持宽高比适配目标宽度</li>
	 *   <li>若计算后的高度超过目标高度，则改为适配目标高度</li>
	 *   <li>最终尺寸不超过任一目标维度</li>
	 *   <li>计算结果像素值不小于1</li>
	 * </ol>
	 *
	 * @param targetSize 最大允许尺寸（宽度必须 > 0，高度必须大于0）
	 * @return 满足双约束的等比缩放尺寸
	 * @throws IllegalArgumentException 当任一参数 ≤ 0时抛出
	 * @since 1.0.0
	 */
	public ImageSize scale(final ImageSize targetSize) {
		return scale(targetSize.width(), targetSize.height());
	}

	/**
	 * 双约束等比缩放
	 * <p>算法说明：</p>
	 * <ol>
	 *   <li>优先保持宽高比适配目标宽度</li>
	 *   <li>若计算后的高度超过目标高度，则改为适配目标高度</li>
	 *   <li>最终尺寸不超过任一目标维度</li>
	 *   <li>计算结果像素值不小于1</li>
	 * </ol>
	 *
	 * @param targetWidth  最大允许宽度（必须 > 0）
	 * @param targetHeight 最大允许高度（必须 > 0）
	 * @return 满足双约束的等比缩放尺寸
	 * @throws IllegalArgumentException 当任一参数 ≤ 0时抛出
	 * @since 1.0.0
	 */
	public ImageSize scale(final int targetWidth, final int targetHeight) {
		Validate.isTrue(targetWidth > 0, "targetWidth 必须大于0");
		Validate.isTrue(targetHeight > 0, "inputImage 必须大于0");

		double ratio = (double) width / height;
		if (width > height) {
			double actualHeight = Math.max(targetWidth / ratio, 1);
			if (actualHeight > targetHeight) {
				return new ImageSize((int) Math.max(targetHeight * ratio, 1), targetHeight);
			}
			return new ImageSize(targetWidth, (int) actualHeight);
		} else {
			double actualWidth = Math.max(targetHeight / ratio, 1);
			if (actualWidth > targetWidth) {
				return new ImageSize((int) Math.max(targetHeight * ratio, 1), targetHeight);
			}
			return new ImageSize((int) actualWidth, targetHeight);
		}
	}
}