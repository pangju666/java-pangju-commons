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

package io.github.pangju666.commons.pdf.model;

import org.apache.commons.lang3.Validate;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.RenderDestination;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

/**
 * PDF 渲染选项类
 * <p>
 * 该类用于配置 PDF 页面渲染为图像时的各种参数，包括缩放比例、图像类型和渲染目标。
 * 支持通过 DPI 或缩放比例来控制渲染质量。
 * </p>
 *
 * @author pangju666
 * @since 1.1.0
 */
public class PdfRenderOptions {
	/**
	 * 缩放比例，默认为 1.0
	 * <p>
	 * 缩放比例用于控制渲染图像的大小，1.0 表示原始大小。
	 * 可以通过 setScale() 或 setDpi() 方法设置。
	 * </p>
	 *
	 * @since 1.1.0
	 */
	protected float scale = 1;

	/**
	 * 图像类型，默认为 RGB
	 * <p>
	 * 指定渲染图像的颜色类型，如 RGB、GRAY、BINARY 等。
	 * </p>
	 *
	 * @since 1.1.0
	 */
	protected ImageType imageType = ImageType.RGB;

	/**
	 * 渲染目标，默认为 EXPORT
	 * <p>
	 * 指定渲染图像的目标用途，如 EXPORT（导出）、VIEW（查看）等。
	 * </p>
	 *
	 * @since 1.1.0
	 */
	protected RenderDestination destination = RenderDestination.EXPORT;

	/**
	 * 获取缩放比例
	 *
	 * @return 缩放比例
	 * @since 1.1.0
	 */
	public float getScale() {
		return scale;
	}

	/**
	 * 设置缩放比例
	 * <p>
	 * 只有当 scale 大于 0 时才会更新缩放比例。
	 * </p>
	 *
	 * @param scale 缩放比例，必须大于 0
	 * @since 1.1.0
	 */
	public void setScale(float scale) {
		if (scale > 0) {
			this.scale = scale;
		}
	}

	/**
	 * 获取 DPI（每英寸点数）
	 * <p>
	 * DPI 通过缩放比例计算得出，公式为：DPI = scale * 72。
	 * </p>
	 *
	 * @return DPI 值
	 * @since 1.1.0
	 */
	public float getDpi() {
		return scale * 72f;
	}

	/**
	 * 设置 DPI（每英寸点数）
	 * <p>
	 * 通过 DPI 自动计算缩放比例，公式为：scale = DPI / 72。
	 * 只有当 dpi 大于 0 时才会更新缩放比例。
	 * </p>
	 *
	 * @param dpi DPI 值，必须大于 0
	 * @since 1.1.0
	 */
	public void setDpi(float dpi) {
		if (dpi > 0) {
			this.scale = dpi / 72f;
		}
	}

	/**
	 * 获取图像类型
	 *
	 * @return 图像类型
	 * @since 1.1.0
	 */
	public ImageType getImageType() {
		return imageType;
	}

	/**
	 * 设置图像类型
	 * <p>
	 * 只有当 imageType 不为 null 时才会更新图像类型。
	 * </p>
	 *
	 * @param imageType 图像类型，不可为 null
	 * @since 1.1.0
	 */
	public void setImageType(ImageType imageType) {
		if (Objects.nonNull(imageType)) {
			this.imageType = imageType;
		}
	}

	/**
	 * 获取渲染目标
	 *
	 * @return 渲染目标
	 * @since 1.1.0
	 */
	public RenderDestination getDestination() {
		return destination;
	}

	/**
	 * 设置渲染目标
	 * <p>
	 * 只有当 destination 不为 null 时才会更新渲染目标。
	 * </p>
	 *
	 * @param destination 渲染目标，不可为 null
	 * @since 1.1.0
	 */
	public void setDestination(RenderDestination destination) {
		if (Objects.nonNull(destination)) {
			this.destination = destination;
		}
	}

	/**
	 * 使用 PDFRenderer 渲染指定页面为图像
	 * <p>
	 * 使用当前的渲染选项（缩放比例、图像类型、渲染目标）将 PDF 指定页面渲染为 BufferedImage。
	 * </p>
	 *
	 * @param renderer   PDF 渲染器，不可为 null
	 * @param pageNumber 页码（从1开始），必须大于等于 1
	 * @return 渲染后的图像
	 * @throws IOException              当渲染失败时抛出
	 * @throws IllegalArgumentException 当 renderer 为 null 或 pageNumber 小于 1 时抛出
	 * @since 1.1.0
	 */
	public BufferedImage renderImage(PDFRenderer renderer, int pageNumber) throws IOException {
		Validate.notNull(renderer, "renderer 不可为 null");
		Validate.isTrue(pageNumber >= 1, "pageNumber 不可为小于等于1");

		return renderer.renderImage(pageNumber - 1, scale, imageType, destination);
	}

	/**
	 * 使用 PDDocument 渲染指定页面为图像
	 * <p>
	 * 从 PDDocument 创建 PDFRenderer，然后使用当前的渲染选项将 PDF 指定页面渲染为 BufferedImage。
	 * </p>
	 *
	 * @param document   PDF 文档对象，不可为 null
	 * @param pageNumber 页码（从1开始），必须大于等于 1
	 * @return 渲染后的图像
	 * @throws IOException              当渲染失败时抛出
	 * @throws IllegalArgumentException 当 document 为 null 或 pageNumber 小于 1 时抛出
	 * @since 1.1.0
	 */
	public BufferedImage renderImage(PDDocument document, int pageNumber) throws IOException {
		Validate.notNull(document, "document 不可为 null");
		Validate.isTrue(pageNumber >= 1, "pageNumber 不可为小于等于0");

		PDFRenderer renderer = new PDFRenderer(document);
		return renderer.renderImage(pageNumber - 1, scale, imageType, destination);
	}
}
