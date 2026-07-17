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
import io.github.pangju666.commons.pdf.utils.PDDocumentUtils;
import org.apache.commons.lang3.Validate;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * PDF 文档资源类
 * <p>
 * 该类用于处理 PDF 文档，基于 Apache PDFBox 的 PDDocument 实现。
 * 支持从文件路径、File 对象、字节数组、输入流或 IOResource 创建 PDF 文档资源。
 * 支持密码保护的 PDF 文档加载和自定义内存使用设置。
 * </p>
 *
 * @author pangju666
 * @since 1.1.0
 */
public class PdfResource extends IOResource {
	/**
	 * PDDocument 文档对象
	 *
	 * @since 1.1.0
	 */
	protected volatile PDDocument document;

	/**
	 * 使用 IOResource 创建 PdfResource
	 * <p>如果传入的资源不是 PdfResource 实例，则验证其 MIME 类型。</p>
	 *
	 * @param resource IOResource 资源对象
	 * @throws IOException                  当读取资源失败时抛出
	 * @throws UnsupportedResourceException 当资源不是 PDF 文档类型时抛出
	 * @since 1.1.0
	 */
	public PdfResource(IOResource resource) throws IOException {
		super(resource);

		if (!(resource instanceof PdfResource)) {
			validateType("resource 不是 PDF 文档资源");
		}
	}

	/**
	 * 使用文件路径创建 PdfResource
	 *
	 * @param filePath PDF 文档文件路径
	 * @throws IOException                  当读取文件失败时抛出
	 * @throws UnsupportedResourceException 当文件不是 PDF 文档类型时抛出
	 * @since 1.1.0
	 */
	public PdfResource(String filePath) throws IOException {
		super(filePath);

		validateType("filePath 不是 PDF 文档文件路径");
	}

	/**
	 * 使用 File 对象创建 PdfResource
	 *
	 * @param file PDF 文档文件对象
	 * @throws IOException                  当读取文件失败时抛出
	 * @throws UnsupportedResourceException 当文件不是 PDF 文档类型时抛出
	 * @since 1.1.0
	 */
	public PdfResource(File file) throws IOException {
		super(file);

		validateType("file 不是 PDF 文档文件");
	}

	/**
	 * 使用字节数组创建 PdfResource
	 *
	 * @param bytes PDF 文档字节数组
	 * @throws IOException                  当读取字节数组失败时抛出
	 * @throws UnsupportedResourceException 当字节数组不是 PDF 文档类型时抛出
	 * @since 1.1.0
	 */
	public PdfResource(byte[] bytes) throws IOException {
		super(bytes);

		validateType("bytes 不是 PDF 文档数据");
	}

	/**
	 * 使用输入流创建 PdfResource
	 *
	 * @param inputStream PDF 文档输入流
	 * @throws IOException                  当读取输入流失败时抛出
	 * @throws UnsupportedResourceException 当输入流不是 PDF 文档类型时抛出
	 * @since 1.1.0
	 */
	public PdfResource(InputStream inputStream) throws IOException {
		super(inputStream);

		validateType("inputStream 不是 PDF 文档输入流");
	}

	/**
	 * 获取 PDDocument 文档对象
	 * <p>
	 * 该方法采用懒加载模式，首次调用时创建文档对象，后续调用返回缓存的实例。
	 * 根据文件大小自动计算内存使用设置。
	 * 如果资源来源于文件，则从文件系统加载；否则从字节数组加载。
	 * </p>
	 *
	 * @return PDDocument 文档对象
	 * @throws IOException 当读取文档失败时抛出
	 * @since 1.1.0
	 */
	public synchronized PDDocument getDocument() throws IOException {
		checkClosed();

		synchronized (this) {
			if (Objects.nonNull(document)) {
				return document;
			}

			if (Objects.nonNull(file)) {
				document = Loader.loadPDF(file, PDDocumentUtils.computeMemoryUsageSetting(file.length()).streamCache);
			} else {
				byte[] bytes = byteArrayOutputStream.toByteArray();
				document = Loader.loadPDF(bytes, "", null, null,
					PDDocumentUtils.computeMemoryUsageSetting(bytes.length).streamCache);
			}
			return document;
		}
	}

	/**
	 * 使用密码获取 PDDocument 文档对象
	 * <p>
	 * 该方法采用懒加载模式，首次调用时创建文档对象，后续调用返回缓存的实例。
	 * 用于加载密码保护的 PDF 文档。
	 * 根据文件大小自动计算内存使用设置。
	 * </p>
	 *
	 * @param password PDF 文档密码，不可为空
	 * @return PDDocument 文档对象
	 * @throws IOException              当读取文档失败时抛出
	 * @throws IllegalArgumentException 当 password 为空时抛出
	 * @since 1.1.0
	 */
	public synchronized PDDocument getDocument(String password) throws IOException {
		checkClosed();

		Validate.notBlank(password, "password 不可为空");

		synchronized (this) {
			if (Objects.nonNull(document)) {
				return document;
			}

			if (Objects.nonNull(file)) {
				document = Loader.loadPDF(file, password, PDDocumentUtils.computeMemoryUsageSetting(file.length()).streamCache);
			} else {
				byte[] bytes = byteArrayOutputStream.toByteArray();
				document = Loader.loadPDF(bytes, password, null, null,
					PDDocumentUtils.computeMemoryUsageSetting(bytes.length).streamCache);
			}
			return document;
		}
	}

	/**
	 * 使用自定义内存设置获取 PDDocument 文档对象
	 * <p>
	 * 该方法采用懒加载模式，首次调用时创建文档对象，后续调用返回缓存的实例。
	 * 使用指定的内存使用设置来控制 PDF 加载时的内存占用。
	 * </p>
	 *
	 * @param memoryUsageSetting 内存使用设置，不可为 null
	 * @return PDDocument 文档对象
	 * @throws IOException          当读取文档失败时抛出
	 * @throws NullPointerException 当 memoryUsageSetting 为 null 时抛出
	 * @since 1.1.0
	 */
	public synchronized PDDocument getDocument(MemoryUsageSetting memoryUsageSetting) throws IOException {
		checkClosed();

		Validate.notNull(memoryUsageSetting, "memoryUsageSetting 不可为 null");

		synchronized (this) {
			if (Objects.nonNull(document)) {
				return document;
			}

			if (Objects.nonNull(file)) {
				document = Loader.loadPDF(file, memoryUsageSetting.streamCache);
			} else {
				byte[] bytes = byteArrayOutputStream.toByteArray();
				document = Loader.loadPDF(bytes, "", null, null, memoryUsageSetting.streamCache);
			}
			return document;
		}
	}

	/**
	 * 使用密码和自定义内存设置获取 PDDocument 文档对象
	 * <p>
	 * 该方法采用懒加载模式，首次调用时创建文档对象，后续调用返回缓存的实例。
	 * 用于加载密码保护的 PDF 文档，并使用指定的内存使用设置。
	 * </p>
	 *
	 * @param password           PDF 文档密码，不可为空
	 * @param memoryUsageSetting 内存使用设置，不可为 null
	 * @return PDDocument 文档对象
	 * @throws IOException              当读取文档失败时抛出
	 * @throws IllegalArgumentException 当 password 为空时抛出
	 * @throws NullPointerException     当 memoryUsageSetting 为 null 时抛出
	 * @since 1.1.0
	 */
	public synchronized PDDocument getDocument(String password, MemoryUsageSetting memoryUsageSetting) throws IOException {
		checkClosed();

		Validate.notBlank(password, "password 不可为空");
		Validate.notNull(memoryUsageSetting, "memoryUsageSetting 不可为 null");

		synchronized (this) {
			if (Objects.nonNull(document)) {
				return document;
			}

			if (Objects.nonNull(file)) {
				document = Loader.loadPDF(file, password, memoryUsageSetting.streamCache);
			} else {
				byte[] bytes = byteArrayOutputStream.toByteArray();
				document = Loader.loadPDF(bytes, password, null, null, memoryUsageSetting.streamCache);
			}
			return document;
		}
	}

	/**
	 * 验证资源类型是否为 PDF 文档
	 *
	 * @param message 验证失败时的错误消息
	 * @throws UnsupportedResourceException 当 MIME 类型不是 PDF 文档类型时抛出
	 * @since 1.1.0
	 */
	protected void validateType(String message) {
		if (!PdfConstants.PDF_MIME_TYPE.equals(mimeType)) {
			throw new UnsupportedResourceException(message);
		}
	}

	/**
	 * 关闭资源并释放文档对象
	 * <p>
	 * 先关闭 PDDocument 文档对象并将引用置为 null，然后调用父类关闭方法。
	 * </p>
	 *
	 * @throws IOException 当关闭文档失败时抛出
	 * @since 1.1.0
	 */
	@Override
	public synchronized void close() throws IOException {
		if (Objects.nonNull(document)) {
			document.close();
		}
		this.document = null;

		super.close();
	}
}
