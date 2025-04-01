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

package io.github.pangju666.commons.pdf.utils;

import io.github.pangju666.commons.io.lang.IOConstants;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.pdf.lang.PdfConstants;
import io.github.pangju666.commons.pdf.model.PDFDirectory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.function.ObjIntConsumer;

/**
 * PDF文档操作工具类，提供PDF文档的创建、合并、拆分、复制、图像处理等常用功能。
 * <p>
 * 本工具类基于Apache PDFBox实现，提供了对PDF文档的高级操作封装。
 * </p>
 * <p><b>注意事项：</b></p>
 * <ul>
 *   <li>所有方法都针对{@link PDDocument}对象操作，使用后需要手动关闭文档</li>
 *   <li>内存管理策略根据文档大小自动选择，大文档处理会使用临时文件</li>
 *   <li>线程安全性：非线程安全，多个线程操作同一文档需要自行同步</li>
 *   <li>图像处理：添加图像时会自动创建新页面，支持多种图像格式</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class PDDocumentUtils {
	/**
	 * PDF文件处理的最小内存阈值（50MB），小于此值将完全在内存中处理
	 *
	 * @see #computeMemoryUsageSetting(long)
	 * @since 1.0.0
	 */
	public static final long MIN_PDF_BYTES = 50 * 1024 * 1024;
	/**
	 * PDF文件处理的最大内存阈值（500MB），超过此大小将使用临时文件处理
	 *
	 * @see #computeMemoryUsageSetting(long)
	 * @since 1.0.0
	 */
	public static final long MAX_PDF_BYTES = 500 * 1024 * 1024;
	/**
	 * 混合内存模式下最大内存使用量（100MB），超过此值将使用临时文件
	 *
	 * @since 1.0.0
	 */
	public static final long MIXED_MAX_MAIN_MEMORY_BYTES = 100 * 1024 * 1024;

	/**
	 * 纯内存处理模式的内存使用设置
	 *
	 * @see MemoryUsageSetting
	 * @since 1.0.0
	 */
	public static final MemoryUsageSetting MAIN_MEMORY_ONLY_MEMORY_USAGE_SETTING = MemoryUsageSetting.setupMainMemoryOnly();
	/**
	 * 纯临时文件处理模式的内存使用设置
	 *
	 * @see MemoryUsageSetting
	 * @since 1.0.0
	 */
	public static final MemoryUsageSetting TEMP_FILE_ONLY_MEMORY_USAGE_SETTING = MemoryUsageSetting.setupTempFileOnly();
	/**
	 * 混合内存处理模式的内存使用设置
	 *
	 * @see MemoryUsageSetting
	 * @since 1.0.0
	 */
	public static final MemoryUsageSetting MIXED_PAGE_MEMORY_USAGE_SETTING = MemoryUsageSetting.setupMixed(MIXED_MAX_MAIN_MEMORY_BYTES);

	protected PDDocumentUtils() {
	}

	/**
	 * 根据文件大小计算合适的内存使用策略
	 *
	 * @param fileSize 文件大小(字节)
	 * @return 内存使用策略
	 * @throws IllegalArgumentException 如果fileSize为负数
	 * @see #MIN_PDF_BYTES
	 * @see #MAX_PDF_BYTES
	 * @since 1.0.0
	 */
	public static MemoryUsageSetting computeMemoryUsageSetting(final long fileSize) {
		if (fileSize < MIN_PDF_BYTES) {
			return MAIN_MEMORY_ONLY_MEMORY_USAGE_SETTING;
		} else if (fileSize > MAX_PDF_BYTES) {
			return TEMP_FILE_ONLY_MEMORY_USAGE_SETTING;
		} else {
			return MIXED_PAGE_MEMORY_USAGE_SETTING;
		}
	}

	/**
	 * 检查文件是否为有效的PDF文件
	 *
	 * @param file 要检查的文件
	 * @return 如果是有效的PDF文件返回true，否则返回false
	 * @throws IOException              如果文件读取失败
	 * @throws IllegalArgumentException 如果file为null
	 * @see PdfConstants#PDF_MIME_TYPE
	 * @since 1.0.0
	 */
	public static boolean isPDF(final File file) throws IOException {
		return FileUtils.exist(file, true) &&
			IOConstants.getDefaultTika().detect(file).equals(PdfConstants.PDF_MIME_TYPE);
	}

	/**
	 * 检查字节数组是否为有效的PDF文件
	 *
	 * @param bytes 要检查的字节数组
	 * @return 如果是有效的PDF文件返回true，否则返回false
	 * @see PdfConstants#PDF_MIME_TYPE
	 * @since 1.0.0
     */
	public static boolean isPDF(final byte[] bytes) {
		return ArrayUtils.isNotEmpty(bytes) &&
			PdfConstants.PDF_MIME_TYPE.equals(IOConstants.getDefaultTika().detect(bytes));
	}

	/**
	 * 检查输入流是否为有效的PDF文件
	 *
	 * @param inputStream 要检查的输入流
	 * @return 如果是有效的PDF文件返回true，否则返回false
	 * @throws IOException 如果流读取失败
	 * @throws IllegalArgumentException 如果inputStream为null
	 * @see PdfConstants#PDF_MIME_TYPE
	 * @since 1.0.0
     */
	public static boolean isPDF(final InputStream inputStream) throws IOException {
		return Objects.nonNull(inputStream) &&
			PdfConstants.PDF_MIME_TYPE.equals(IOConstants.getDefaultTika().detect(inputStream));
	}

	/**
	 * 创建新的PDF文档并复制源文档的元数据
	 *
	 * @param source 源PDF文档
	 * @return 新创建的PDDocument对象
	 * @throws IOException 如果复制元数据失败
	 * @throws IllegalArgumentException 如果source为null
	 * @since 1.0.0
     */
	public static PDDocument createDocument(final PDDocument source) throws IOException {
		Validate.notNull(source, "source 不可为 null");

		PDDocument newDocument = new PDDocument();
		newDocument.setVersion(source.getVersion());
		newDocument.setDocumentInformation(source.getDocumentInformation());
		if (Objects.nonNull(source.getDocumentCatalog().getMetadata())) {
			newDocument.getDocumentCatalog().setMetadata(source.getDocumentCatalog().getMetadata());
		}
		for (PDSignature signatureDictionary : source.getSignatureDictionaries()) {
			newDocument.addSignature(signatureDictionary);
		}
		return newDocument;
	}

	/**
	 * 从文件加载PDF文档
	 *
	 * @param file PDF文件
	 * @return 加载的PDDocument对象，如果不是PDF文件返回null
	 * @throws IOException 如果文件读取失败或不是有效的PDF文件
	 * @throws IllegalArgumentException 如果file为null
	 * @throws FileNotFoundException 如果文件不存在
	 * @see #computeMemoryUsageSetting(long)
	 * @since 1.0.0
     */
	public static PDDocument getDocument(final File file) throws IOException {
		Validate.notNull(file, "file 不可为 null");
		checkFile(file);

		if (!PdfConstants.PDF_MIME_TYPE.equals(IOConstants.getDefaultTika().detect(file))) {
			return null;
		}
		return Loader.loadPDF(file, computeMemoryUsageSetting(file.length()).streamCache);
	}

	/**
	 * 从加密的PDF文件加载文档
	 *
	 * @param file PDF文件
	 * @param password 文档密码
	 * @return 加载的PDDocument对象，如果不是PDF文件返回null
	 * @throws IOException 如果文件读取失败或密码错误
	 * @throws IllegalArgumentException 如果file为null
	 * @throws FileNotFoundException 如果文件不存在
	 * @see #computeMemoryUsageSetting(long)
	 * @since 1.0.0
     */
	public static PDDocument getDocument(final File file, final String password) throws IOException {
		Validate.notNull(file, "file 不可为 null");
		checkFile(file);

		if (!PdfConstants.PDF_MIME_TYPE.equals(IOConstants.getDefaultTika().detect(file))) {
			return null;
		}
		return Loader.loadPDF(file, password, computeMemoryUsageSetting(file.length()).streamCache);
	}

	/**
	 * 将图像添加到PDF文档的指定位置
	 *
	 * @param document 目标PDF文档
	 * @param bytes 图像字节数组
	 * @param page 要插入的页码(1-based)
	 * @throws IOException 如果图像处理失败
	 * @throws IllegalArgumentException 如果document为null
	 * @since 1.0.0
     */
	public static void addImageToDocument(final PDDocument document, final byte[] bytes, int page) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		if (ArrayUtils.isNotEmpty(bytes)) {
			PDImageXObject imageObject = PDImageXObject.createFromByteArray(document, bytes, null);
			PDPage PDPage = new PDPage(new PDRectangle(imageObject.getWidth(), imageObject.getHeight()));
			try (PDPageContentStream pageContentStream = new PDPageContentStream(document, PDPage)) {
				pageContentStream.drawImage(imageObject, 0, 0);
			}
			PDPage beforePage = document.getPage(page);
			document.getPages().insertBefore(beforePage, beforePage);
		}
	}

	/**
	 * 将图像添加到PDF文档的指定位置(自定义尺寸)
	 *
	 * @param document 目标PDF文档
	 * @param bytes 图像字节数组
	 * @param width 图像宽度
	 * @param height 图像高度
	 * @param page 要插入的页码(1-based)
	 * @throws IOException 如果图像处理失败
	 * @throws IllegalArgumentException 如果document为null
	 * @since 1.0.0
     */
	public static void addImageToDocument(final PDDocument document, final byte[] bytes, int width, int height, int page) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		if (ArrayUtils.isNotEmpty(bytes)) {
			PDImageXObject imageObject = PDImageXObject.createFromByteArray(document, bytes, null);
			PDPage PDPage = new PDPage(new PDRectangle(width, height));
			try (PDPageContentStream pageContentStream = new PDPageContentStream(document, PDPage)) {
				pageContentStream.drawImage(imageObject, 0, 0);
			}
			PDPage beforePage = document.getPage(page);
			document.getPages().insertBefore(beforePage, beforePage);
		}
	}

	/**
	 * 将图像文件添加到PDF文档的指定位置
	 *
	 * @param document 目标PDF文档
	 * @param imageFile 图像文件
	 * @param page 要插入的页码(1-based)
	 * @throws IOException 如果图像处理失败或文件无效
	 * @throws IllegalArgumentException 如果document或imageFile为null
	 * @since 1.0.0
     */
	public static void addImageToDocument(final PDDocument document, final File imageFile, int page) throws IOException {
		Validate.notNull(imageFile, "file 不可为 null");
		checkFile(imageFile);

		if (IOConstants.getDefaultTika().detect(imageFile).startsWith(IOConstants.IMAGE_MIME_TYPE_PREFIX)) {
			PDImageXObject imageObject = PDImageXObject.createFromFileByContent(imageFile, document);
			PDPage PDPage = new PDPage(new PDRectangle(imageObject.getWidth(), imageObject.getHeight()));
			try (PDPageContentStream pageContentStream = new PDPageContentStream(document, PDPage)) {
				pageContentStream.drawImage(imageObject, 0, 0);
			}
			PDPage beforePage = document.getPage(page);
			document.getPages().insertBefore(beforePage, beforePage);
		}
	}

	/**
	 * 将图像文件添加到PDF文档的指定位置(自定义尺寸)
	 *
	 * @param document 目标PDF文档
	 * @param imageFile 图像文件
	 * @param width 图像宽度
	 * @param height 图像高度
	 * @param page 要插入的页码(1-based)
	 * @throws IOException 如果图像处理失败或文件无效
	 * @throws IllegalArgumentException 如果document或imageFile为null
	 * @since 1.0.0
     */
	public static void addImageToDocument(final PDDocument document, final File imageFile, int width, int height, int page) throws IOException {
		Validate.notNull(imageFile, "file 不可为 null");
		checkFile(imageFile);

		if (IOConstants.getDefaultTika().detect(imageFile).startsWith(IOConstants.IMAGE_MIME_TYPE_PREFIX)) {
			PDImageXObject imageObject = PDImageXObject.createFromFileByContent(imageFile, document);
			PDPage PDPage = new PDPage(new PDRectangle(width, height));
			try (PDPageContentStream pageContentStream = new PDPageContentStream(document, PDPage)) {
				pageContentStream.drawImage(imageObject, 0, 0);
			}
			PDPage beforePage = document.getPage(page);
			document.getPages().insertBefore(beforePage, beforePage);
		}
	}

	/**
	 * 将图像添加到PDF文档末尾
	 *
	 * @param document 目标PDF文档
	 * @param bytes 图像字节数组
	 * @throws IOException 如果图像处理失败
	 * @throws IllegalArgumentException 如果document为null
	 * @since 1.0.0
     */
	public static void addImageToDocument(final PDDocument document, final byte[] bytes) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		if (ArrayUtils.isNotEmpty(bytes)) {
			PDImageXObject imageObject = PDImageXObject.createFromByteArray(document, bytes, null);
			PDPage page = new PDPage(new PDRectangle(imageObject.getWidth(), imageObject.getHeight()));
			try (PDPageContentStream pageContentStream = new PDPageContentStream(document, page)) {
				pageContentStream.drawImage(imageObject, 0, 0);
			}
			document.addPage(page);
		}
	}

	/**
	 * 将图像添加到PDF文档末尾(自定义尺寸)
	 *
	 * @param document 目标PDF文档
	 * @param bytes 图像字节数组
	 * @param width 图像宽度
	 * @param height 图像高度
	 * @throws IOException 如果图像处理失败
	 * @throws IllegalArgumentException 如果document为null
	 * @since 1.0.0
     */
	public static void addImageToDocument(final PDDocument document, final byte[] bytes, int width, int height) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		if (ArrayUtils.isNotEmpty(bytes)) {
			PDImageXObject imageObject = PDImageXObject.createFromByteArray(document, bytes, null);
			PDPage page = new PDPage(new PDRectangle(width, height));
			try (PDPageContentStream pageContentStream = new PDPageContentStream(document, page)) {
				pageContentStream.drawImage(imageObject, 0, 0);
			}
			document.addPage(page);
		}
	}

	/**
	 * 将图像文件添加到PDF文档末尾
	 *
	 * @param document 目标PDF文档
	 * @param imageFile 图像文件
	 * @throws IOException 如果图像处理失败或文件无效
	 * @throws IllegalArgumentException 如果document或imageFile为null
	 * @since 1.0.0
     */
	public static void addImageToDocument(final PDDocument document, final File imageFile) throws IOException {
		Validate.notNull(imageFile, "file 不可为 null");
		checkFile(imageFile);

		if (IOConstants.getDefaultTika().detect(imageFile).startsWith(IOConstants.IMAGE_MIME_TYPE_PREFIX)) {
			PDImageXObject imageObject = PDImageXObject.createFromFileByContent(imageFile, document);
			PDPage page = new PDPage(new PDRectangle(imageObject.getWidth(), imageObject.getHeight()));
			try (PDPageContentStream pageContentStream = new PDPageContentStream(document, page)) {
				pageContentStream.drawImage(imageObject, 0, 0);
			}
			document.addPage(page);
		}
	}

	/**
	 * 将图像文件添加到PDF文档末尾(自定义尺寸)
	 *
	 * @param document 目标PDF文档
	 * @param imageFile 图像文件
	 * @param width 图像宽度
	 * @param height 图像高度
	 * @throws IOException 如果图像处理失败或文件无效
	 * @throws IllegalArgumentException 如果document或imageFile为null
	 * @since 1.0.0
     */
	public static void addImageToDocument(final PDDocument document, final File imageFile, int width, int height) throws IOException {
		Validate.notNull(imageFile, "file 不可为 null");
		checkFile(imageFile);

		if (IOConstants.getDefaultTika().detect(imageFile).startsWith(IOConstants.IMAGE_MIME_TYPE_PREFIX)) {
			PDImageXObject imageObject = PDImageXObject.createFromFileByContent(imageFile, document);
			PDPage page = new PDPage(new PDRectangle(width, height));
			try (PDPageContentStream pageContentStream = new PDPageContentStream(document, page)) {
				pageContentStream.drawImage(imageObject, 0, 0);
			}
			document.addPage(page);
		}
	}

	/**
	 * 遍历PDF文档中的所有图像并执行操作(使用默认缩放比例1)
	 *
	 * @param document PDF文档
	 * @param action 对每页图像执行的操作(参数为图像和页码)
	 * @throws IOException 如果渲染失败
	 * @throws IllegalArgumentException 如果document为null
	 * @since 1.0.0
     */
	public static void getImages(final PDDocument document, final ObjIntConsumer<BufferedImage> action) throws IOException {
		getImages(document, 1, action);
	}

	/**
	 * 遍历PDF文档中的所有图像并执行操作(使用指定缩放比例)
	 *
	 * @param document PDF文档
	 * @param scale 图像缩放比例
	 * @param action 对每页图像执行的操作(参数为图像和页码)
	 * @throws IOException 如果渲染失败
	 * @throws IllegalArgumentException 如果document为null或scale小于等于0
	 * @since 1.0.0
     */
	public static void getImages(final PDDocument document, final int scale, final ObjIntConsumer<BufferedImage> action) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		if (Objects.nonNull(action)) {
			PDFRenderer renderer = new PDFRenderer(document);
			for (int i = 0; i < document.getNumberOfPages(); i++) {
				BufferedImage image = renderer.renderImage(i, scale);
				action.accept(image, i + 1);
			}
		}
	}

	/**
	 * 遍历PDF文档中的所有图像并执行操作(使用指定DPI)
	 *
	 * @param document PDF文档
	 * @param dpi 图像DPI值
	 * @param action 对每页图像执行的操作(参数为图像和页码)
	 * @throws IOException 如果渲染失败
	 * @throws IllegalArgumentException 如果document为null或dpi小于等于0
	 * @since 1.0.0
     */
	public static void getImages(final PDDocument document, final float dpi, final ObjIntConsumer<BufferedImage> action) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		if (Objects.nonNull(action)) {
			PDFRenderer renderer = new PDFRenderer(document);
			for (int i = 0; i < document.getNumberOfPages(); i++) {
				BufferedImage image = renderer.renderImageWithDPI(i, dpi);
				action.accept(image, i + 1);
			}
		}
	}

	/**
	 * 提取PDF文档中的所有图像(使用默认缩放比例1)
	 *
	 * @param document PDF文档
	 * @return 包含所有页面图像的列表
	 * @throws IOException 如果渲染失败
	 * @throws IllegalArgumentException 如果document为null
	 * @since 1.0.0
     */
	public static List<BufferedImage> getImages(final PDDocument document) throws IOException {
		return getImages(document, 1);
	}

	/**
	 * 提取PDF文档中的所有图像(使用指定缩放比例)
	 *
	 * @param document PDF文档
	 * @param scale 图像缩放比例
	 * @return 包含所有页面图像的列表
	 * @throws IOException 如果渲染失败
	 * @throws IllegalArgumentException 如果document为null或scale小于等于0
	 * @since 1.0.0
     */
	public static List<BufferedImage> getImages(final PDDocument document, final int scale) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		PDFRenderer renderer = new PDFRenderer(document);
		List<BufferedImage> images = new ArrayList<>(document.getNumberOfPages());
		for (int i = 0; i < document.getNumberOfPages(); i++) {
			BufferedImage image = renderer.renderImage(i, scale);
			images.add(image);
		}
		return images;
	}

	/**
	 * 提取PDF文档中的所有图像(使用指定DPI)
	 *
	 * @param document PDF文档
	 * @param dpi 图像DPI值
	 * @return 包含所有页面图像的列表
	 * @throws IOException 如果渲染失败
	 * @throws IllegalArgumentException 如果document为null或dpi小于等于0
	 * @since 1.0.0
     */
	public static List<BufferedImage> getImages(final PDDocument document, final float dpi) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		PDFRenderer renderer = new PDFRenderer(document);
		List<BufferedImage> images = new ArrayList<>(document.getNumberOfPages());
		for (int i = 0; i < document.getNumberOfPages(); i++) {
			BufferedImage image = renderer.renderImageWithDPI(i, dpi);
			images.add(image);
		}
		return images;
	}

	/**
	 * 提取PDF文档的指定页面为图像
	 *
	 * @param document PDF文档
	 * @param page 页码(1-based)
	 * @return 指定页面的图像
	 * @throws IOException 如果渲染失败
	 * @throws IllegalArgumentException 如果document为null或页码无效
	 * @since 1.0.0
     */
	public static BufferedImage getImage(final PDDocument document, final int page) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		PDFRenderer renderer = new PDFRenderer(document);
		return renderer.renderImage(page - 1);
	}

	/**
	 * 提取PDF文档的指定页面为图像(使用指定缩放比例)
	 *
	 * @param document PDF文档
	 * @param page 页码(1-based)
	 * @param scale 图像缩放比例
	 * @return 指定页面的图像
	 * @throws IOException 如果渲染失败
	 * @throws IllegalArgumentException 如果document为null或页码无效或scale小于等于0
	 * @since 1.0.0
     */
	public static BufferedImage getImage(final PDDocument document, final int page, final int scale) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		PDFRenderer renderer = new PDFRenderer(document);
		return renderer.renderImage(page - 1, scale);
	}

	/**
	 * 提取PDF文档的指定页面为图像(使用指定DPI)
	 *
	 * @param document PDF文档
	 * @param page 页码(1-based)
	 * @param dpi 图像DPI值
	 * @return 指定页面的图像
	 * @throws IOException 如果渲染失败
	 * @throws IllegalArgumentException 如果document为null或页码无效或dpi小于等于0
	 * @since 1.0.0
     */
	public static BufferedImage getImage(final PDDocument document, final int page, final float dpi) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		PDFRenderer renderer = new PDFRenderer(document);
		return renderer.renderImageWithDPI(page - 1, dpi);
	}

	/**
	 * 合并多个PDF文档到一个输出流
	 *
	 * @param documents 要合并的文档集合
	 * @param outputStream 合并后的输出流
	 * @param memoryUsageSetting 内存使用策略
	 * @throws IOException 如果合并或保存失败
	 * @throws IllegalArgumentException 如果参数为null或documents为空
	 * @see #merge(Collection, MemoryUsageSetting)
	 * @since 1.0.0
     */
	public static void merge(final Collection<PDDocument> documents, final OutputStream outputStream, final MemoryUsageSetting memoryUsageSetting) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (PDDocument outputDocument = merge(documents, memoryUsageSetting)) {
			outputDocument.save(outputStream);
		}
	}

	/**
	 * 合并多个PDF文档到输出文件(自动计算内存使用策略)
	 *
	 * @param documents 要合并的文档集合
	 * @param outputFile 合并后的输出文件
	 * @throws IOException 如果合并或保存失败
	 * @throws IllegalArgumentException 如果参数为null或documents为空
	 * @see #computeMemoryUsageSetting(long)
	 * @since 1.0.0
     */
	public static void merge(final Collection<PDDocument> documents, final File outputFile) throws IOException {
		Validate.notNull(outputFile, "outputFile 不可为 null");
		if (outputFile.exists() && !outputFile.isFile()) {
			throw new IOException(outputFile.getAbsolutePath() + " 不是一个文件路径");
		}

		try (PDDocument outputDocument = merge(documents, computeMemoryUsageSetting(outputFile.length()))) {
			outputDocument.save(outputFile);
		}
	}

	/**
	 * 合并多个PDF文档到输出文件(使用指定内存策略)
	 *
	 * @param documents 要合并的文档集合
	 * @param outputFile 合并后的输出文件
	 * @param memoryUsageSetting 内存使用策略
	 * @throws IOException 如果合并或保存失败
	 * @throws IllegalArgumentException 如果参数为null或documents为空
	 * @since 1.0.0
     */
	public static void merge(final Collection<PDDocument> documents, final File outputFile, final MemoryUsageSetting memoryUsageSetting) throws IOException {
		Validate.notNull(outputFile, "outputFile 不可为 null");
		if (outputFile.exists() && !outputFile.isFile()) {
			throw new IOException(outputFile.getAbsolutePath() + " 不是一个文件路径");
		}

		try (PDDocument outputDocument = merge(documents, memoryUsageSetting)) {
			outputDocument.save(outputFile);
		}
	}

	/**
	 * 合并多个PDF文档并返回合并后的文档对象
	 *
	 * @param documents 要合并的文档集合
	 * @param memoryUsageSetting 内存使用策略
	 * @return 合并后的PDDocument对象
	 * @throws IOException 如果合并失败
	 * @throws IllegalArgumentException 如果参数为null或documents为空
	 * @since 1.0.0
     */
	public static PDDocument merge(final Collection<PDDocument> documents, final MemoryUsageSetting memoryUsageSetting) throws IOException {
		Validate.notNull(memoryUsageSetting, "memoryUsageSetting 不可为 null");
		Validate.notEmpty(documents, "documents 不可为空");

		PDFMergerUtility mergerUtility = new PDFMergerUtility();
		PDDocument outputDocument = new PDDocument(memoryUsageSetting.streamCache);
		for (PDDocument document : documents) {
			mergerUtility.appendDocument(outputDocument, document);
		}
		return outputDocument;
	}

	/**
	 * 按页拆分PDF文档并对每个拆分文档执行操作(默认每页拆分)
	 *
	 * @param document 要拆分的PDF文档
	 * @param action 对每个拆分文档执行的操作(参数为文档和起始页码)
	 * @throws IOException 如果拆分失败
	 * @throws IllegalArgumentException 如果document为null
	 * @since 1.0.0
     */
	public static void split(final PDDocument document, final ObjIntConsumer<PDDocument> action) throws IOException {
		split(document, 1, action);
	}

	/**
	 * 按页拆分PDF文档并对每个拆分文档执行操作(指定每n页拆分)
	 *
	 * @param document 要拆分的PDF文档
	 * @param splitPage 每n页拆分一次
	 * @param action 对每个拆分文档执行的操作(参数为文档和起始页码)
	 * @throws IOException 如果拆分失败
	 * @throws IllegalArgumentException 如果document为null或splitPage小于等于0
	 * @since 1.0.0
     */
	public static void split(final PDDocument document, final int splitPage, final ObjIntConsumer<PDDocument> action) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		if (Objects.nonNull(action)) {
			int totalPages = document.getNumberOfPages();
			for (int pageNumber = 1; pageNumber <= totalPages; pageNumber += splitPage) {
				int endPage = pageNumber + splitPage - 1;
				try (PDDocument splitDocument = copy(document, pageNumber, endPage)) {
					action.accept(splitDocument, pageNumber);
				}
			}
		}
	}

	/**
	 * 按页拆分PDF文档(默认每页拆分)
	 *
	 * @param document 要拆分的PDF文档
	 * @return 拆分后的文档列表
	 * @throws IOException 如果拆分失败
	 * @throws IllegalArgumentException 如果document为null
	 * @since 1.0.0
     */
	public static List<PDDocument> split(final PDDocument document) throws IOException {
		return split(document, 1);
	}

	/**
	 * 按页拆分PDF文档(指定每n页拆分)
	 *
	 * @param document 要拆分的PDF文档
	 * @param splitPage 每n页拆分一次
	 * @return 拆分后的文档列表
	 * @throws IOException 如果拆分失败
	 * @throws IllegalArgumentException 如果document为null或splitPage小于等于0
	 * @since 1.0.0
     */
	public static List<PDDocument> split(final PDDocument document, final int splitPage) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		List<PDDocument> outputFileList = new ArrayList<>(document.getNumberOfPages() / splitPage);
		int totalPages = document.getNumberOfPages();
		for (int pageNumber = 1; pageNumber <= totalPages; pageNumber += splitPage) {
			outputFileList.add(copy(document, pageNumber, pageNumber + splitPage - 1));
		}
		return outputFileList;
	}

	/**
	 * 复制PDF文档的全部页面
	 *
	 * @param document 源PDF文档
	 * @return 复制后的文档对象
	 * @throws IOException 如果复制失败
	 * @throws IllegalArgumentException 如果document为null
	 * @since 1.0.0
     */
	public static PDDocument copy(final PDDocument document) throws IOException {
		return copy(document, 1, document.getNumberOfPages());
	}

	/**
	 * 复制PDF文档的指定页面范围
	 *
	 * @param document 源PDF文档
	 * @param startPage 起始页码(1-based)
	 * @param endPage 结束页码(1-based)
	 * @return 复制后的文档对象
	 * @throws IOException 如果复制失败
	 * @throws IllegalArgumentException 如果document为null或页码无效
	 * @since 1.0.0
     */
	public static PDDocument copy(final PDDocument document, final int startPage, final int endPage) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		// 防止页码溢出
		int maxPage = Math.min(document.getNumberOfPages(), endPage);
		PDDocument result = createDocument(document);
		for (int i = startPage; i <= maxPage; i++) {
			PDPage page = document.getPage(i - 1);
			result.importPage(page);
		}
		return result;
	}

	/**
	 * 复制PDF文档的指定页码数组
	 *
	 * @param document 源PDF文档
	 * @param pages 要复制的页码数组(1-based)
	 * @return 复制后的文档对象
	 * @throws IOException 如果复制失败
	 * @throws IllegalArgumentException 如果document为null或pages为空
	 * @since 1.0.0
     */
	public static PDDocument copy(final PDDocument document, final int... pages) throws IOException {
		return copy(document, Arrays.stream(pages).boxed().toList());
	}

	/**
	 * 复制PDF文档的指定页码数组
	 *
	 * @param document 源PDF文档
	 * @param pages 要复制的页码数组(1-based)
	 * @return 复制后的文档对象
	 * @throws IOException 如果复制失败
	 * @throws IllegalArgumentException 如果document为null或pages为空
	 * @since 1.0.0
     */
	public static PDDocument copy(final PDDocument document, final Integer... pages) throws IOException {
		return copy(document, Arrays.asList(pages));
	}

	/**
	 * 复制PDF文档的指定页码集合
	 *
	 * @param document 源PDF文档
	 * @param pages 要复制的页码集合(1-based)
	 * @return 复制后的文档对象
	 * @throws IOException 如果复制失败
	 * @throws IllegalArgumentException 如果document为null或pages为空
	 * @since 1.0.0
     */
	public static PDDocument copy(final PDDocument document, final Collection<Integer> pages) throws IOException {
		Validate.notNull(document, "document 不可为 null");
		Validate.notEmpty(pages, "pages 不可为空");

		int maxPageNumber = document.getNumberOfPages();
		// 页码去重，过滤掉溢出的页码并排序
		List<Integer> pageNumberList = pages.stream()
			.distinct()
			.filter(pageNumber -> pageNumber >= 1 && pageNumber <= maxPageNumber)
			.sorted(Integer::compareTo)
			.toList();

		PDDocument copyDocument = createDocument(document);
		for (Integer pageNumber : pageNumberList) {
			PDPage sourcePage = document.getPage(pageNumber - 1);
			copyDocument.importPage(sourcePage);
		}
		return copyDocument;
	}

	/**
	 * 获取PDF文档的目录结构
	 *
	 * @param document PDF文档
	 * @return 目录结构列表
	 * @throws IOException 如果读取目录失败
	 * @throws IllegalArgumentException 如果document为null
	 * @since 1.0.0
     */
	public static List<PDFDirectory> getDictionaries(final PDDocument document) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		PDDocumentOutline outline = document.getDocumentCatalog().getDocumentOutline();
		if (outline != null) {
			List<PDFDirectory> result = new ArrayList<>();
			parseOutline(outline, result);
			return result;
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * 解析PDF文档大纲节点并构建目录结构
	 *
	 * @param node 大纲节点
	 * @param result 目录结构结果列表
	 * @throws IOException 如果解析失败
	 * @since 1.0.0
     */
	protected static void parseOutline(final PDOutlineNode node, final List<PDFDirectory> result) throws IOException {
		PDOutlineItem item = node.getFirstChild();
		while (item != null) {
			int pageIndex = getPageIndex(item);
			PDFDirectory PDFDirectory = new PDFDirectory(item.getTitle(), pageIndex);
			result.add(PDFDirectory);
			parseOutline(item, PDFDirectory.children());
			item = item.getNextSibling();
		}
	}

	/**
	 * 获取大纲项对应的页码
	 *
	 * @param item 大纲项
	 * @return 对应的页码(1 - based)，如果无法确定返回-1
	 * @throws IOException 如果获取页码失败
	 * @since 1.0.0
	 */
	protected static int getPageIndex(final PDOutlineItem item) throws IOException {
		if (item.getDestination() instanceof PDPageDestination destination) {
			return destination.retrievePageNumber();
		} else if (item.getAction() instanceof PDActionGoTo action) {
			if (action.getDestination() instanceof PDPageDestination destination) {
				return destination.retrievePageNumber();
			}
		}
		return -1;
	}

	/**
	 * 检查文件有效性
	 *
	 * @param file 要检查的文件
	 * @throws FileNotFoundException 如果文件不存在
	 * @throws IOException 如果路径不是文件
	 * @since 1.0.0
	 */
	protected static void checkFile(final File file) throws IOException {
		if (!file.exists()) {
			throw new FileNotFoundException(file.getAbsolutePath());
		}
		if (!file.isFile()) {
			throw new IOException(file.getAbsolutePath() + " 不是一个文件路径");
		}
	}
}