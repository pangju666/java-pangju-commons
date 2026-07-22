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

package io.github.pangju666.commons.tesseract.io.resource;

import io.github.pangju666.commons.io.exception.UnsupportedResourceException;
import io.github.pangju666.commons.io.resource.IOResource;
import io.github.pangju666.commons.tesseract.lang.TesseractConstants;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.leptonica.global.leptonica;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Tesseract OCR 资源
 * <p>继承自 {@link IOResource}，专门用于 Tesseract OCR 识别的图像资源封装。</p>
 *
 * <p>该类提供以下功能：</p>
 * <ul>
 *     <li>支持多种输入源：文件路径、File对象、字节数组、输入流、IOResource</li>
 *     <li>自动验证图像类型是否为 Tesseract 支持的格式</li>
 *     <li>提供转换为 Leptonica PIX 格式的方法</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.1.0
 */
public class TesseractResource extends IOResource {
	/**
	 * 使用 IOResource 创建 TesseractResource
	 * <p>如果传入的资源不是 TesseractResource 实例，则验证其图像类型。</p>
	 *
	 * @param resource IOResource 资源对象
	 * @throws IOException                  当读取资源失败时抛出
	 * @throws UnsupportedResourceException 当资源不是支持的图像类型时抛出
	 * @since 1.1.0
	 */
	public TesseractResource(IOResource resource) throws IOException {
		super(resource);

		if (!(resource instanceof TesseractResource)) {
			validateImageType("resource 不是图像资源");
		}
	}

	/**
	 * 使用文件路径创建 TesseractResource
	 *
	 * @param filePath 图像文件路径
	 * @throws IOException                  当读取文件失败时抛出
	 * @throws UnsupportedResourceException 当文件不是支持的图像类型时抛出
	 * @since 1.1.0
	 */
	public TesseractResource(String filePath) throws IOException {
		super(filePath);

		validateImageType("filePath 不是图像文件路径");
	}

	/**
	 * 使用 File 对象创建 TesseractResource
	 *
	 * @param file 图像文件对象
	 * @throws IOException                  当读取文件失败时抛出
	 * @throws UnsupportedResourceException 当文件不是支持的图像类型时抛出
	 * @since 1.1.0
	 */
	public TesseractResource(File file) throws IOException {
		super(file);

		validateImageType("file 不是图像文件");
	}

	/**
	 * 使用字节数组创建 TesseractResource
	 *
	 * @param bytes 图像字节数组
	 * @throws IOException                  当读取字节数组失败时抛出
	 * @throws UnsupportedResourceException 当字节数组不是支持的图像类型时抛出
	 * @since 1.1.0
	 */
	public TesseractResource(byte[] bytes) throws IOException {
		super(bytes);

		validateImageType("bytes 不是图像数据");
	}

	/**
	 * 使用输入流创建 TesseractResource
	 *
	 * @param inputStream 图像输入流
	 * @throws IOException                  当读取输入流失败时抛出
	 * @throws UnsupportedResourceException 当输入流不是支持的图像类型时抛出
	 * @since 1.1.0
	 */
	public TesseractResource(InputStream inputStream) throws IOException {
		super(inputStream);

		validateImageType("inputStream 不是图像输入流");
	}

	/**
	 * 获取 Leptonica PIX 格式图像
	 * <p>根据资源类型选择合适的读取方式：</p>
	 * <ul>
	 *     <li>文件资源：使用 pixRead 从文件路径读取</li>
	 *     <li>内存资源：使用 pixReadMem 从字节数组读取</li>
	 * </ul>
	 *
	 * @return Leptonica PIX 格式图像对象
	 * @since 1.1.0
	 */
	public PIX getPix() {
		checkClosed();

		if (Objects.nonNull(file)) {
			return leptonica.pixRead(file.getAbsolutePath());
		} else {
			try (BytePointer bytePointer = new BytePointer(byteArrayOutputStream.toByteArray())) {
				return leptonica.pixReadMem(bytePointer, byteArrayOutputStream.size());
			}
		}
	}

	/**
	 * 验证图像类型
	 * <p>验证资源是否为 Tesseract 支持的图像类型。</p>
	 *
	 * <p>验证内容：</p>
	 * <ul>
	 *     <li>资源是否为图像类型</li>
	 *     <li>MIME类型是否为 Tesseract 支持的类型</li>
	 *     <li>图像格式是否为 Tesseract 支持的格式（当MIME类型不支持时）</li>
	 * </ul>
	 *
	 * @param message 验证失败时的错误消息
	 * @throws UnsupportedResourceException 当资源不是图像类型，或当前 MIME 类型/图像格式不支持 Tesseract 读取时抛出
	 * @since 1.1.0
	 */
	protected void validateImageType(String message) {
		if (!isImage()) {
			throw new UnsupportedResourceException(message);
		}
		if (!TesseractConstants.SUPPORTED_IMAGE_TYPES.contains(mimeType)) {
			if (!TesseractConstants.SUPPORTED_IMAGE_FILE_FORMATS.contains(format)) {
				throw new UnsupportedResourceException("不支持读取该图像", format, mimeType);
			}
		}
	}
}
