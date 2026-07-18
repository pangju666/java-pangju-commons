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

package io.github.pangju666.commons.pdf.io.resource;

import io.github.pangju666.commons.io.exception.UnsupportedResourceException;
import io.github.pangju666.commons.io.resource.IOResource;
import io.github.pangju666.commons.pdf.lang.PdfConstants;
import org.apache.commons.lang3.Validate;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * PDF 图像资源类
 * <p>
 * 该类用于处理图像资源，支持将图像转换为 PDF 页面 或 {@link PDImageXObject} 对象。
 * 基于 Apache PDFBox 实现，支持从文件路径、File 对象、字节数组、输入流或 IOResource 创建图像资源。
 * 支持的图像格式包括：JPEG、TIFF、GIF、BMP、PNG。
 * </p>
 *
 * @author pangju666
 * @since 2.1.0
 */
public class PdfImageResource extends IOResource {
	/**
	 * 使用 IOResource 创建 PdfImageResource
	 * <p>如果传入的资源不是 PdfImageResource 实例，则验证其 MIME 类型。</p>
	 *
	 * @param resource IOResource 资源对象
	 * @throws IOException                  当读取资源失败时抛出
	 * @throws UnsupportedResourceException 当资源不是支持的图像类型时抛出
	 * @since 2.1.0
	 */
	public PdfImageResource(IOResource resource) throws IOException {
		super(resource);

		if (!(resource instanceof PdfImageResource)) {
			validateType("resource 不是图像资源");
		}
	}

	/**
	 * 使用文件路径创建 PdfImageResource
	 *
	 * @param filePath 图像文件路径
	 * @throws IOException                  当读取文件失败时抛出
	 * @throws UnsupportedResourceException 当文件不是支持的图像类型时抛出
	 * @since 2.1.0
	 */
	public PdfImageResource(String filePath) throws IOException {
		super(filePath);

		validateType("filePath 不是图像文件路径");
	}

	/**
	 * 使用 File 对象创建 PdfImageResource
	 *
	 * @param file 图像文件对象
	 * @throws IOException                  当读取文件失败时抛出
	 * @throws UnsupportedResourceException 当文件不是支持的图像类型时抛出
	 * @since 2.1.0
	 */
	public PdfImageResource(File file) throws IOException {
		super(file);

		validateType("file 不是图像文件");
	}

	/**
	 * 使用字节数组创建 PdfImageResource
	 *
	 * @param bytes 图像字节数组
	 * @throws IOException                  当读取字节数组失败时抛出
	 * @throws UnsupportedResourceException 当字节数组不是支持的图像类型时抛出
	 * @since 2.1.0
	 */
	public PdfImageResource(byte[] bytes) throws IOException {
		super(bytes);

		validateType("bytes 不是图像数据");
	}

	/**
	 * 使用输入流创建 PdfImageResource
	 *
	 * @param inputStream 图像输入流
	 * @throws IOException                  当读取输入流失败时抛出
	 * @throws UnsupportedResourceException 当输入流不是支持的图像类型时抛出
	 * @since 2.1.0
	 */
	public PdfImageResource(InputStream inputStream) throws IOException {
		super(inputStream);

		validateType("inputStream 不是图像输入流");
	}

	/**
	 * 获取 PDF 图像对象
	 * <p>
	 * 将图像资源转换为 PDFBox 的 PDImageXObject 对象。
	 * 如果资源来源于文件，则从文件创建；否则从字节数组创建。
	 * </p>
	 *
	 * @param document PDF 文档对象，不可为 null
	 * @return PDImageXObject 图像对象
	 * @throws IOException          当读取图像失败时抛出
	 * @throws NullPointerException 当 document 为 null 时抛出
	 * @since 2.1.0
	 */
	public PDImageXObject getImageXObject(PDDocument document) throws IOException {
		checkClosed();

		Validate.notNull(document, "document 不可为 null");

		if (Objects.nonNull(file)) {
			return PDImageXObject.createFromFileByContent(file, document);
		} else {
			byte[] bytes = byteArrayOutputStream.toByteArray();
			return PDImageXObject.createFromByteArray(document, bytes, format);
		}
	}

	/**
	 * 创建包含图像的 PDF 页面
	 * <p>
	 * 创建一个与图像尺寸相同的 PDF 页面，并将图像绘制在页面左上角 (0, 0) 位置。
	 * </p>
	 *
	 * @param document PDF 文档对象，不可为 null
	 * @return PDPage 包含图像的 PDF 页面
	 * @throws IOException           当创建页面或绘制图像失败时抛出
	 * @throws NullPointerException  当 document 为 null 时抛出
	 * @throws IllegalStateException 当资源已关闭时抛出
	 * @since 2.1.0
	 */
	public PDPage createPage(PDDocument document) throws IOException {
		checkClosed();

		Validate.notNull(document, "document 不可为 null");

		PDImageXObject imageXObject = getImageXObject(document);
		PDPage page = new PDPage(new PDRectangle(imageXObject.getWidth(), imageXObject.getHeight()));
		try (PDPageContentStream pageContentStream = new PDPageContentStream(document, page)) {
			pageContentStream.drawImage(imageXObject, 0, 0);
		}
		return page;
	}

	/**
	 * 创建指定尺寸的 PDF 页面并绘制图像
	 * <p>
	 * 创建一个指定尺寸的 PDF 页面，并将图像绘制在指定位置。
	 * </p>
	 *
	 * @param document PDF 文档对象，不可为 null
	 * @param x        图像绘制位置的 X 坐标，必须大于等于 0
	 * @param y        图像绘制位置的 Y 坐标，必须大于等于 0
	 * @param width    页面宽度，必须大于 0
	 * @param height   页面高度，必须大于 0
	 * @return PDPage 包含图像的 PDF 页面
	 * @throws IOException              当创建页面或绘制图像失败时抛出
	 * @throws IllegalArgumentException 当 x、y 小于 0 或 width、height 小于等于 0 时抛出
	 * @throws NullPointerException     当 document 为 null 时抛出
	 * @throws IllegalStateException    当资源已关闭时抛出
	 * @since 2.1.0
	 */
	public PDPage createPage(PDDocument document, int x, int y, int width, int height) throws IOException {
		checkClosed();

		Validate.notNull(document, "document 不可为 null");
		Validate.isTrue(x >= 0, "x 不可为小于等于0");
		Validate.isTrue(y >= 0, "y 不可为小于等于0");
		Validate.isTrue(width > 0, "width 不可为小于等于0");
		Validate.isTrue(height > 0, "height 不可为小于等于0");

		PDImageXObject imageXObject = getImageXObject(document);
		PDPage page = new PDPage(new PDRectangle(width, height));
		try (PDPageContentStream pageContentStream = new PDPageContentStream(document, page)) {
			pageContentStream.drawImage(imageXObject, x, y);
		}
		return page;
	}

	/**
	 * 验证资源类型是否为支持的图像格式
	 * <p>
	 * 验证资源是否为图像类型，并检查其 MIME 类型或文件扩展名是否在 PDF 支持的图像格式列表中。
	 * </p>
	 *
	 * @param message 验证失败时的错误消息
	 * @throws UnsupportedResourceException 当资源不是图像类型或不支持的图像格式时抛出
	 * @since 2.1.0
	 */
	protected void validateType(String message) {
		if (!isImage()) {
			throw new UnsupportedResourceException(message);
		}
		if (!PdfConstants.PDF_IMAGE_SUPPORTED_IMAGE_TYPES.contains(mimeType)) {
			if (!PdfConstants.PDF_IMAGE_SUPPORTED_IMAGE_FORMATS.contains(format)) {
				throw new UnsupportedResourceException("不支持读取该图像", format, mimeType);
			}
		}
	}
}
