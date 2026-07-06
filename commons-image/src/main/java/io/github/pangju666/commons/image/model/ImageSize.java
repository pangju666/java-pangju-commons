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

import io.github.pangju666.commons.image.lang.ImageConstants;
import org.apache.commons.lang3.Validate;

/**
 * 图像尺寸模型（不可变）。
 * <p>
 * 封装图像的宽度、高度及 EXIF 方向信息，提供物理尺寸与可视化尺寸的转换能力，以及多种尺寸计算工具。
 * </p>
 *
 * <p><b>核心概念：</b></p>
 * <ul>
 *   <li><b>物理尺寸：</b> 图像文件实际存储的像素宽高（不包含旋转信息）。</li>
 *   <li><b>可视化尺寸：</b> 结合 EXIF 方向信息（Orientation）校正后的显示宽高（通过 {@link #getVisualSize()} 获取）。</li>
 * </ul>
 *
 * <p><b>功能特性：</b></p>
 * <ul>
 *   <li><b>不可变性：</b> 对象创建后属性不可修改，保证线程安全。</li>
 *   <li><b>尺寸计算：</b>
 *     <ul>
 *       <li><b>等比缩放：</b> 支持按宽度、按高度、按比例或宽高（{@link #scale(int, int)}）进行等比缩放计算。</li>
 *       <li><b>强制调整：</b> 支持忽略原比例强制指定新尺寸（{@link #resize(int, int)}）。</li>
 *     </ul>
 *   </li>
 *   <li><b>安全计算：</b> 计算结果自动四舍五入，并确保最小尺寸为 1x1 像素，防止异常。</li>
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
	 * EXIF 方向标识
	 *
	 * @since 1.0.0
	 */
	private final int orientation;
	/**
	 * 是否为可视化尺寸
	 *
	 * @since 1.0.0
	 */
	private final boolean visual;

	/**
	 * 构造方法（正常方向）
	 * <p>
	 * 创建一个具有指定宽高、EXIF 方向为正常的图像尺寸对象。
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
		this.orientation = ImageConstants.NORMAL_EXIF_ORIENTATION;
		this.visual = false;
	}

	/**
	 * 构造方法（指定方向）
	 * <p>
	 * 创建一个具有指定宽高和 EXIF 方向的图像尺寸对象。
	 * </p>
	 *
	 * @param width       图像宽度（像素），必须大于 0
	 * @param height      图像高度（像素），必须大于 0
	 * @param orientation EXIF 方向标识，必须介于 1-8 之间
	 * @throws IllegalArgumentException 当参数不符合要求时抛出
	 * @since 1.0.0
	 */
	public ImageSize(int width, int height, int orientation) {
		this(width, height, orientation, false);
	}

	/**
	 * 全参数构造方法
	 * <p>
	 * 内部使用的全参数构造方法，支持设置 visual 属性。
	 * </p>
	 *
	 * @param width       图像宽度（像素），必须大于 0
	 * @param height      图像高度（像素），必须大于 0
	 * @param orientation EXIF 方向标识，必须介于 1-8 之间
	 * @param visual      是否为可视化尺寸
	 * @throws IllegalArgumentException 当参数不符合要求时抛出
	 * @since 1.0.0
	 */
	protected ImageSize(int width, int height, int orientation, boolean visual) {
		Validate.isTrue(width > 0, "width 必须大于0");
		Validate.isTrue(height > 0, "height 必须大于0");
		Validate.inclusiveBetween(1, 8, orientation, "orientation 必须介于1-8之间");

		this.width = width;
		this.height = height;
		this.orientation = orientation;
		this.visual = visual;
	}

	/**
	 * 获取符合视觉习惯的图像尺寸
	 * <p>
	 * 根据 EXIF 方向信息（Orientation）校正宽和高：
	 * </p>
	 * <ul>
	 *   <li><b>方向 5-8：</b> 图像被旋转了 90° 或 270°，此时<b>交换宽度和高度</b>。</li>
	 *   <li><b>其他方向：</b> 保持原始存储尺寸。</li>
	 * </ul>
	 * <p>
	 * 此方法常用于 UI 显示或需要按照图像“摆正”后的样子处理的场景。
	 * </p>
	 *
	 * @return 校正后的 {@link ImageSize} 对象。如果当前对象已经是可视化尺寸（{@code isVisual() == true}），则返回自身。
	 * @see <a href="https://www.exif.org/ExifTags/0x0112.php">EXIF Orientation Tag</a>
	 * @since 1.0.0
	 */
	public ImageSize getVisualSize() {
		if (this.visual) {
			return this;
		}
		return orientation >= 5 ? new ImageSize(height, width, orientation, true) :
			new ImageSize(width, height, orientation, true);
	}

	/**
	 * 获取 EXIF 方向标识
	 * <p>
	 * 对应 EXIF 标签 Orientation，值的范围为 1-8。
	 * </p>
	 *
	 * @return 方向标识值
	 * @since 1.0.0
	 */
	public int getOrientation() {
		return orientation;
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
			return new ImageSize(targetWidth, Math.max((int) Math.round(targetWidth / ratio), 1), orientation, visual);
		}

		double ratio = (double) height / width;
		return new ImageSize(targetWidth, Math.max((int) Math.round(targetWidth * ratio), 1), orientation, visual);
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
			return new ImageSize(Math.max((int) Math.round(targetHeight * ratio), 1), targetHeight, orientation, visual);
		}

		double ratio = (double) height / width;
		return new ImageSize(Math.max((int) Math.round(targetHeight / ratio), 1), targetHeight, orientation, visual);
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
	 * @param targetWidth  目标宽度，必须大于0
	 * @param targetHeight 目标高度，必须大于0
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
			return new ImageSize(targetWidth, heightByWidth, orientation, visual);
		}
		int widthByHeight = Math.max((int) Math.round(targetHeight * ratio), 1);
		return new ImageSize(widthByHeight, targetHeight, orientation, visual);
	}

	/**
	 * 按给定比例缩放尺寸，保持宽高比例不变。
	 *
	 * <p>该方法不会修改当前对象，而是返回一个新的尺寸实例。</p>
	 * <p>缩放结果采用四舍五入到最近像素。</p>
	 *
	 * @param scalingFactor 缩放比例，必须大于 0
	 * @return 缩放后的新尺寸
	 * @throws IllegalArgumentException 当 {@code scale} 小于或等于 0 时抛出
	 * @since 1.0.0
	 */
	public ImageSize scale(final double scalingFactor) {
		Validate.isTrue(scalingFactor > 0, "scale 必须大于0");

		return new ImageSize((int) Math.round(this.getWidth() * scalingFactor),
			(int) Math.round(this.height * scalingFactor), orientation, visual);
	}

	/**
	 * 强制调整尺寸（非等比）。
	 * <p>
	 * 直接返回一个具有指定宽度和高度的新 {@link ImageSize} 实例。
	 * <br><b>注意：</b> 此方法不保持原有的宽高比，而是强制使用目标尺寸。
	 * 原对象的 EXIF 方向信息（{@link #orientation}）和可视化状态（{@link #visual}）将被保留。
	 * </p>
	 *
	 * @param targetWidth  目标宽度，必须大于 0
	 * @param targetHeight 目标高度，必须大于 0
	 * @return 调整尺寸后的新实例
	 * @throws IllegalArgumentException 当宽或高小于等于 0 时抛出
	 * @since 1.0.0
	 */
	public ImageSize resize(final int targetWidth, final int targetHeight) {
		Validate.isTrue(targetWidth > 0, "targetWidth 必须大于0");
		Validate.isTrue(targetHeight > 0, "targetHeight 必须大于0");

		return new ImageSize(targetWidth, targetHeight, orientation, visual);
	}

	/**
	 * 检查图像是否为正常方向（无需旋转/翻转）。
	 * <p>
	 * 当 EXIF 等于 {@link ImageConstants#NORMAL_EXIF_ORIENTATION}（值为 1）时，
	 * 表示图像方向正常，显示时不需要进行任何旋转或翻转操作。
	 * </p>
	 *
	 * @return true 表示图像方向正常；false 表示需要根据 EXIF 方向进行校正
	 * @since 2.1.0
	 */
	public boolean isNormalOrientation() {
		return orientation == ImageConstants.NORMAL_EXIF_ORIENTATION;
	}
}