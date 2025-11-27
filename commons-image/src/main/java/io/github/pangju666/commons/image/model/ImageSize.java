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
 * 图像尺寸模型类
 * <p>
 * 表示图像的显示尺寸，封装宽度和高度两个不可变属性，
 * 提供多种保持宽高比的尺寸缩放计算方法。
 * </p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li><strong>不可变性</strong> - 线程安全，适合并发场景</li>
 *   <li><strong>宽高比保持</strong> - 所有缩放操作保持原始比例</li>
 *   <li><strong>像素保护</strong> - 结果像素值最小为 1</li>
 *   <li><strong>取整策略</strong> - 所有尺寸计算采用四舍五入到最近像素</li>
 * </ul>
 *
 * <h3>典型应用</h3>
 * <ul>
 *   <li>图像缩略图生成</li>
 *   <li>响应式图片尺寸计算</li>
 *   <li>图片裁剪预处理</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class ImageSize {
	/**
	 * 图像宽度（像素）
	 *
	 * @since 1.0.0
	 */
	private final int width;
	/**
	 * 图像高度（像素）
	 *
	 * @since 1.0.0
	 */
	private final int height;

	/**
	 * 规范构造方法
	 * <p>
	 * 对宽度和高度进行有效性验证：
	 * <ul>
	 *   <li>必须为正整数</li>
	 * </ul>
	 * </p>
	 *
	 * @param width  图像宽度（像素），必须大于 0
	 * @param height 图像高度（像素），必须大于 0
	 * @throws IllegalArgumentException 当参数不符合要求时抛出
	 * @since 1.0.0
	 */
	public ImageSize(int width, int height) {
		Validate.isTrue(width > 0, "width 必须大于0");
		Validate.isTrue(height > 0, "height 必须大于0");

		this.width = width;
		this.height = height;
	}

	/**
	 * 获取图像宽度（像素）
	 *
	 * @return 正整数宽度
	 * @since 1.0.0
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * 获取图像高度（像素）
	 *
	 * @return 正整数高度
	 * @since 1.0.0
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * 基于目标宽度的等比缩放
	 * <p>
	 * 保持原始宽高比，按目标宽度计算新尺寸：
	 * <ul>
	 *   <li>当宽 &gt; 高时：高度按比例缩小</li>
	 *   <li>当宽 &le; 高时：高度按比例放大</li>
	 *   <li>所有计算结果采用四舍五入到最近像素，且最小为 1 像素</li>
	 * </ul>
	 * </p>
	 *
	 * <p>该方法不会修改当前对象，而是返回一个新的实例。</p>
	 *
	 * @param targetWidth 目标宽度，必须大于 0
	 * @return 缩放后的新尺寸对象
	 * @throws IllegalArgumentException 当参数不符合要求时抛出
	 * @since 1.0.0
	 */
	public ImageSize scaleByWidth(final int targetWidth) {
		Validate.isTrue(targetWidth > 0, "targetWidth 必须大于0");

		if (width > height) {
			double ratio = (double) width / height;
			return new ImageSize(targetWidth, Math.max((int) Math.round(targetWidth / ratio), 1));
		}

		double ratio = (double) height / width;
		return new ImageSize(targetWidth, Math.max((int) Math.round(targetWidth * ratio), 1));
	}

	/**
	 * 基于目标高度的等比缩放
	 * <p>
	 * 保持原始宽高比，按目标高度计算新尺寸：
	 * <ul>
	 *   <li>当宽 &gt; 高时：宽度按比例缩小</li>
	 *   <li>当宽 &le; 高时：宽度按比例放大</li>
	 *   <li>所有计算结果采用四舍五入到最近像素，且最小为 1 像素</li>
	 * </ul>
	 * </p>
	 *
	 * <p>该方法不会修改当前对象，而是返回一个新的实例。</p>
	 *
	 * @param targetHeight 目标高度，必须大于 0
	 * @return 缩放后的新尺寸对象
	 * @throws IllegalArgumentException 当参数不符合要求时抛出
	 * @since 1.0.0
	 */
	public ImageSize scaleByHeight(final int targetHeight) {
		Validate.isTrue(targetHeight > 0, "targetHeight 必须大于0");

		if (width > height) {
			double ratio = (double) width / height;
			return new ImageSize(Math.max((int) Math.round(targetHeight * ratio), 1), targetHeight);
		}

		double ratio = (double) height / width;
		return new ImageSize(Math.max((int) Math.round(targetHeight / ratio), 1), targetHeight);
	}

	/**
	 * 双约束等比缩放（基于尺寸对象）
	 * <p>
	 * 在不超过目标尺寸的前提下保持宽高比：
	 * <ol>
	 *   <li>优先适配宽度计算</li>
	 *   <li>若高度超出则改为适配高度</li>
	 *   <li>所有结果采用四舍五入到最近像素，且最小为 1 像素</li>
	 * </ol>
	 * </p>
	 *
	 * <p>该方法不会修改当前对象，而是返回一个新的实例。</p>
	 *
	 * @param targetSize 目标尺寸对象，非 null，宽高需为正数
	 * @return 满足约束的缩放尺寸
	 * @throws IllegalArgumentException 当参数不符合要求时抛出
	 * @since 1.0.0
	 */
	public ImageSize scale(final ImageSize targetSize) {
		Validate.notNull(targetSize, "targetSize 不可为 null");
		return scale(targetSize.getWidth(), targetSize.getHeight());
	}

	/**
	 * 双约束等比缩放（基于目标宽高值）
	 * <p>
	 * 在不超过目标宽高的前提下保持宽高比：
	 * <ol>
	 *   <li>优先适配宽度计算</li>
	 *   <li>若高度超出则改为适配高度</li>
	 *   <li>所有结果采用四舍五入到最近像素，且最小为 1 像素</li>
	 * </ol>
	 * </p>
	 *
	 * <p>该方法不会修改当前对象，而是返回一个新的实例。</p>
	 *
	 * @param targetWidth  目标宽度，必须满足：
	 *                     <ul>
	 *                       <li>大于0</li>
	 *                       <li>不超过Integer.MAX_VALUE</li>
	 *                     </ul>
	 * @param targetHeight 目标高度，必须满足：
	 *                     <ul>
	 *                       <li>大于0</li>
	 *                       <li>不超过Integer.MAX_VALUE</li>
	 *                     </ul>
	 * @return 满足约束的缩放尺寸
	 * @throws IllegalArgumentException 当参数不符合要求时抛出
	 * @since 1.0.0
	 */
	public ImageSize scale(final int targetWidth, final int targetHeight) {
		Validate.isTrue(targetWidth > 0, "targetWidth 必须大于0");
		Validate.isTrue(targetHeight > 0, "targetHeight 必须大于0");

		double ratio = (double) width / height;
		int heightByWidth = Math.max((int) Math.round(targetWidth / ratio), 1);
		if (heightByWidth <= targetHeight) {
			return new ImageSize(targetWidth, heightByWidth);
		}
		int widthByHeight = Math.max((int) Math.round(targetHeight * ratio), 1);
		return new ImageSize(widthByHeight, targetHeight);
	}

	/**
	 * 按给定比例缩放尺寸，保持宽高比例不变。
	 *
	 * <p>该方法不会修改当前对象，而是返回一个新的尺寸实例。</p>
	 * <p>缩放结果采用四舍五入到最近像素。</p>
	 *
	 * @param scale 缩放比例，必须大于 0
	 * @return 缩放后的新尺寸
	 * @throws IllegalArgumentException 当 {@code scale} 小于或等于 0 时抛出
	 * @since 1.0.0
	 */
	public ImageSize scale(final double scale) {
		Validate.isTrue(scale > 0, "scale 必须大于0");
		return new ImageSize((int) Math.round(this.getWidth() * scale), (int) Math.round(this.height * scale));
	}
}