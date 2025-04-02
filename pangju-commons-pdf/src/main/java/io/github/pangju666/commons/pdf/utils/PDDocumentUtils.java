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
import io.github.pangju666.commons.pdf.model.Bookmark;
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
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * PDF文档操作工具类，提供了一系列静态方法来处理PDF文档的常见操作。
 * <p>
 * 主要功能包括：
 * <ul>
 *   <li>PDF文档的加载和验证</li>
 *   <li>PDF文档的合并与拆分</li>
 *   <li>PDF页面的复制与提取</li>
 *   <li>图像与PDF的相互转换</li>
 *   <li>PDF目录结构的解析</li>
 *   <li>内存使用优化策略</li>
 * </ul>
 * <p>
 * 本工具类基于Apache PDFBox库实现，提供了更简洁易用的API封装。
 * <p>
 * 内存使用策略：
 * <ul>
 *   <li>小于50MB的PDF文件使用纯内存模式处理</li>
 *   <li>50MB-500MB之间的PDF文件使用混合内存模式处理</li>
 *   <li>大于500MB的PDF文件使用临时文件模式处理</li>
 * </ul>
 *
 * @author pangju666
 * @version 1.0.0
 * @see org.apache.pdfbox.pdmodel.PDDocument
 * @see org.apache.pdfbox.multipdf.PDFMergerUtility
 * @see org.apache.pdfbox.rendering.PDFRenderer
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
		return FileUtils.isMimeType(file, PdfConstants.PDF_MIME_TYPE);
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
	 * @since 1.0.0
     */
	public static PDDocument createDocument(final PDDocument source) {
		Validate.notNull(source, "source 不可为 null");

		PDDocument newDocument = new PDDocument();
		newDocument.setVersion(source.getVersion());
		newDocument.setDocumentInformation(source.getDocumentInformation());
		if (Objects.nonNull(source.getDocumentCatalog().getMetadata())) {
			newDocument.getDocumentCatalog().setMetadata(source.getDocumentCatalog().getMetadata());
		}
		return newDocument;
	}

	/**
	 * 从文件加载PDF文档
	 *
	 * @param file PDF文件
	 * @return 加载的PDDocument对象，如果不是PDF文件返回null
	 * @throws IOException 如果文件读取失败或不是有效的PDF文件
	 * @throws IllegalArgumentException 如果file为null或不为pdf文件
	 * @throws FileNotFoundException 如果文件不存在
	 * @see #computeMemoryUsageSetting(long)
	 * @since 1.0.0
     */
	public static PDDocument getDocument(final File file) throws IOException {
		FileUtils.checkFile(file, "file 不可为 null");

		if (!PdfConstants.PDF_MIME_TYPE.equals(IOConstants.getDefaultTika().detect(file))) {
			throw new IllegalArgumentException("不是一个pdf文件");
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
	 * @throws IllegalArgumentException 如果file为null或不为pdf文件
	 * @throws FileNotFoundException 如果文件不存在
	 * @see #computeMemoryUsageSetting(long)
	 * @since 1.0.0
     */
	public static PDDocument getDocument(final File file, final String password) throws IOException {
		FileUtils.checkFile(file, "file 不可为 null");

		if (!PdfConstants.PDF_MIME_TYPE.equals(IOConstants.getDefaultTika().detect(file))) {
			throw new IllegalArgumentException("不是一个pdf文件");
		}
		return Loader.loadPDF(file, password, computeMemoryUsageSetting(file.length()).streamCache);
	}

	/**
	 * 将图像添加到PDF文档的最后一页
	 *
	 * @param document 目标PDF文档
	 * @param bytes    图像字节数组
	 * @throws IOException              如果图像处理失败
	 * @throws IllegalArgumentException 如果document为null
	 * @since 1.0.0
	 */
	public static void addImage(final PDDocument document, final byte[] bytes) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		addImage(document, bytes, document.getNumberOfPages() + 1);
	}

	/**
	 * 将图像添加到PDF文档的最后一页(自定义尺寸)
	 *
	 * @param document 目标PDF文档
	 * @param bytes    图像字节数组
	 * @param width    图像宽度
	 * @param height   图像高度
	 * @param x        绘制图像的 x 坐标
	 * @param y        绘制图像的 y 坐标
	 * @throws IOException              如果图像处理失败
	 * @throws IllegalArgumentException 如果document为null
	 * @since 1.0.0
	 */
	public static void addImage(final PDDocument document, final byte[] bytes, final int x, final int y,
								final int width, final int height) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		addImage(document, bytes, document.getNumberOfPages() + 1, x, y, width, height);
	}

	/**
	 * 将图像添加到PDF文档的指定页码
	 *
	 * @param document 目标PDF文档
	 * @param bytes 图像字节数组
	 * @param page 要插入的页码(从1开始)
	 * @throws IOException 如果图像处理失败
	 * @throws IllegalArgumentException 如果document为null
	 * @since 1.0.0
	 */
	public static void addImage(final PDDocument document, final byte[] bytes, final int page) throws IOException {
		Validate.notNull(document, "document 不可为 null");
		Validate.isTrue(page > 0, "page 不可为小于等于0");
		if (ArrayUtils.isEmpty(bytes)) {
			return;
		}

		PDImageXObject imageObject = PDImageXObject.createFromByteArray(document, bytes, null);
		addImageToDocument(document, imageObject, page, 0, 0, imageObject.getWidth(), imageObject.getHeight());
	}

	/**
	 * 将图像添加到PDF文档的指定页码(自定义尺寸)
	 *
	 * @param document 目标PDF文档
	 * @param bytes 图像字节数组
	 * @param width 图像宽度
	 * @param height 图像高度
	 * @param x 绘制图像的 x 坐标
	 * @param y 绘制图像的 y 坐标
	 * @param page 要插入的页码(从1开始)
	 * @throws IOException 如果图像处理失败
	 * @throws IllegalArgumentException 如果document为null
	 * @since 1.0.0
	 */
	public static void addImage(final PDDocument document, final byte[] bytes, final int page,
								final int x, final int y, final int width, final int height) throws IOException {
		Validate.notNull(document, "document 不可为 null");
		Validate.isTrue(page > 0, "page 不可为小于等于0");
		Validate.isTrue(x > 0, "x 不可为小于等于0");
		Validate.isTrue(y > 0, "y 不可为小于等于0");
		Validate.isTrue(width > 0, "width 不可为小于等于0");
		Validate.isTrue(height > 0, "height 不可为小于等于0");
		if (ArrayUtils.isEmpty(bytes)) {
			return;
		}

		PDImageXObject imageObject = PDImageXObject.createFromByteArray(document, bytes, null);
		addImageToDocument(document, imageObject, page, x, y, width, height);
	}

	/**
	 * 将图像文件添加到PDF文档的最后一页
	 *
	 * @param document  目标PDF文档
	 * @param imageFile 图像文件
	 * @throws IOException              如果图像处理失败或文件无效
	 * @throws IllegalArgumentException 如果document或imageFile为null
	 * @since 1.0.0
	 */
	public static void addImage(final PDDocument document, final File imageFile) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		addImage(document, imageFile, document.getNumberOfPages() + 1);
	}

	/**
	 * 将图像文件添加到PDF文档的最后一页(自定义尺寸)
	 *
	 * @param document  目标PDF文档
	 * @param imageFile 图像文件
	 * @param width     图像宽度
	 * @param height    图像高度
	 * @param x         绘制图像的 x 坐标
	 * @param y         绘制图像的 y 坐标
	 * @throws IOException              如果图像处理失败或文件无效
	 * @throws IllegalArgumentException 如果document或imageFile为null
	 * @since 1.0.0
	 */
	public static void addImage(final PDDocument document, final File imageFile, final int x, final int y,
								final int width, final int height) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		addImage(document, imageFile, document.getNumberOfPages() + 1, x, y, width, height);
	}

	/**
	 * 将图像文件添加到PDF文档的指定页码
	 *
	 * @param document 目标PDF文档
	 * @param imageFile 图像文件
	 * @param page 要插入的页码(从1开始)
	 * @throws IOException 如果图像处理失败或文件无效
	 * @throws IllegalArgumentException 如果document或imageFile为null
	 * @since 1.0.0
     */
	public static void addImage(final PDDocument document, final File imageFile, final int page) throws IOException {
		Validate.isTrue(page > 0, "page 不可为小于等于0");
		FileUtils.checkFile(imageFile, "imageFile 不可为 null");

		PDImageXObject imageObject = PDImageXObject.createFromFileByContent(imageFile, document);
		addImageToDocument(document, imageObject, page, 0, 0, imageObject.getWidth(), imageObject.getHeight());
	}

	/**
	 * 将图像文件添加到PDF文档的指定页码(自定义尺寸)
	 *
	 * @param document 目标PDF文档
	 * @param imageFile 图像文件
	 * @param width 图像宽度
	 * @param height 图像高度
	 * @param page 要插入的页码(从1开始)
	 * @param x 绘制图像的 x 坐标
	 * @param y 绘制图像的 y 坐标
	 * @throws IOException 如果图像处理失败或文件无效
	 * @throws IllegalArgumentException 如果document或imageFile为null
	 * @since 1.0.0
	 */
	public static void addImage(final PDDocument document, final File imageFile, final int page,
								final int x, final int y, final int width, final int height) throws IOException {
		Validate.isTrue(page > 0, "page 不可为小于等于0");
		Validate.isTrue(x > 0, "x 不可为小于等于0");
		Validate.isTrue(y > 0, "y 不可为小于等于0");
		Validate.isTrue(width > 0, "width 不可为小于等于0");
		Validate.isTrue(height > 0, "height 不可为小于等于0");
		FileUtils.checkFile(imageFile, "imageFile 不可为 null");

		PDImageXObject imageObject = PDImageXObject.createFromFileByContent(imageFile, document);
		addImageToDocument(document, imageObject, page, x, y, width, height);
	}

	/**
	 * 获取PDF文档指定页面的图像列表(使用默认缩放比例1)
	 *
	 * @param document PDF文档，允许为null，nul则返回空列表
	 * @param pages    要获取的页码集合(从1开始)
	 * @return 包含指定页面图像的列表
	 * @throws IOException              如果渲染失败
	 * @since 1.0.0
	 */
	public static List<BufferedImage> getPageImages(final PDDocument document,
													final Collection<Integer> pages) throws IOException {
		return getPageImages(document, 1, pages);
	}

	/**
	 * 获取PDF文档指定页面的图像列表(使用指定缩放比例)
	 *
	 * @param document PDF文档，允许为null，nul则返回空列表
	 * @param scale 图像缩放比例
	 * @param pages 要获取的页码集合(从1开始)，允许为空，空则返回空列表
	 * @return 包含指定页面图像的列表
	 * @throws IOException 如果渲染失败
	 * @throws IllegalArgumentException 如果scale小于等于0
	 * @since 1.0.0
	 */
	public static List<BufferedImage> getPageImages(final PDDocument document, final int scale,
													final Collection<Integer> pages) throws IOException {
		Validate.isTrue(scale > 0, "dpi 不可为小于等于0");
		if (Objects.isNull(document) || Objects.isNull(pages) || pages.isEmpty()) {
			return Collections.emptyList();
		}

		List<Integer> validPages = checkPages(document, pages);
		PDFRenderer renderer = new PDFRenderer(document);
		List<BufferedImage> images = new ArrayList<>(validPages.size());
		for (Integer pageNumber : validPages) {
			BufferedImage image = renderer.renderImage(pageNumber - 1, scale);
			images.add(image);
		}
		return images;
	}

	/**
	 * 获取PDF文档指定页面的图像列表(使用指定DPI)
	 *
	 * @param document PDF文档，允许为null，nul则返回空列表
	 * @param dpi 图像DPI值
	 * @param pages 要获取的页码集合(从1开始)，允许为空，空则返回空列表
	 * @return 包含指定页面图像的列表
	 * @throws IOException 如果渲染失败
	 * @throws IllegalArgumentException 如果dpi小于等于0
	 * @since 1.0.0
	 */
	public static List<BufferedImage> getPageImages(final PDDocument document, final float dpi,
													final Collection<Integer> pages) throws IOException {
		Validate.isTrue(dpi > 0, "dpi 不可为小于等于0");
		if (Objects.isNull(document) || Objects.isNull(pages) || pages.isEmpty()) {
			return Collections.emptyList();
		}

		List<Integer> validPages = checkPages(document, pages);
		PDFRenderer renderer = new PDFRenderer(document);
		List<BufferedImage> images = new ArrayList<>(validPages.size());
		for (Integer pageNumber : validPages) {
			BufferedImage image = renderer.renderImageWithDPI(pageNumber - 1, dpi);
			images.add(image);
		}
		return images;
	}

	/**
	 * 获取PDF文档指定页面范围的图像列表(使用默认缩放比例1)
	 *
	 * @param document PDF文档，允许为null，nul则返回空列表
	 * @param startPage 起始页码(从1开始)
	 * @param endPage 结束页码(从1开始)
	 * @return 包含指定页面范围图像的列表
	 * @throws IOException 如果渲染失败
	 * @throws IllegalArgumentException 如果页码无效
	 * @since 1.0.0
	 */
	public static List<BufferedImage> getPageImages(final PDDocument document,
													final int startPage, final int endPage) throws IOException {
		return getPageImages(document, 1, startPage, endPage);
	}

	/**
	 * 获取PDF文档指定页面范围的图像列表(使用指定缩放比例)
	 *
	 * @param document PDF文档，允许为null，nul则返回空列表
	 * @param scale 图像缩放比例
	 * @param startPage 起始页码(从1开始)
	 * @param endPage 结束页码(从1开始)
	 * @return 包含指定页面范围图像的列表
	 * @throws IOException 如果渲染失败
	 * @throws IllegalArgumentException 如果scale小于等于0或页码无效
     * @since 1.0.0
	 */
	public static List<BufferedImage> getPageImages(final PDDocument document, final int scale,
													final int startPage, final int endPage) throws IOException {
		if (Objects.isNull(document)) {
			return Collections.emptyList();
		}
		Validate.isTrue(scale > 0, "scale 不可为小于等于0");
		Validate.isTrue(startPage > 0, "startPage 不可为小于等于0");
		Validate.isTrue(endPage > 0, "endPage 不可为小于等于0");
		Validate.isTrue(startPage <= endPage, "endPage 必须大于等于 startPage");

		int maxPage = Math.min(document.getNumberOfPages(), endPage);
		PDFRenderer renderer = new PDFRenderer(document);
		List<BufferedImage> images = new ArrayList<>(endPage - startPage + 1);
		for (int i = startPage; i <= maxPage; i++) {
			BufferedImage image = renderer.renderImage(i + 1, scale);
			images.add(image);
		}
		return images;
	}

	/**
	 * 获取PDF文档指定页面范围的图像列表(使用指定DPI)
	 *
	 * @param document PDF文档，允许为null，nul则返回空列表
	 * @param dpi 图像DPI值
	 * @param startPage 起始页码(从1开始)
	 * @param endPage 结束页码(从1开始)
	 * @return 包含指定页面范围图像的列表
	 * @throws IOException 如果渲染失败
	 * @throws IllegalArgumentException 如果dpi小于等于0或页码无效
     * @since 1.0.0
	 */
	public static List<BufferedImage> getPageImages(final PDDocument document, final float dpi,
													final int startPage, final int endPage) throws IOException {
		if (Objects.isNull(document)) {
			return Collections.emptyList();
		}
		Validate.isTrue(dpi > 0, "dpi 不可为小于等于0");
		Validate.isTrue(startPage > 0, "startPage 不可为小于等于0");
		Validate.isTrue(endPage > 0, "endPage 不可为小于等于0");
		Validate.isTrue(startPage <= endPage, "endPage 必须大于等于 startPage");

		int maxPage = Math.min(document.getNumberOfPages(), endPage);
		PDFRenderer renderer = new PDFRenderer(document);
		List<BufferedImage> images = new ArrayList<>(endPage - startPage + 1);
		for (int i = startPage; i <= maxPage; i++) {
			BufferedImage image = renderer.renderImageWithDPI(i + 1, dpi);
			images.add(image);
		}
		return images;
	}

	/**
	 * 提取PDF文档中的所有图像(使用默认缩放比例1)
	 *
	 * @param document PDF文档，允许为null，nul则返回空列表
	 * @return 包含所有页面图像的列表
	 * @throws IOException 如果渲染失败
	 * @since 1.0.0
     */
	public static List<BufferedImage> getPageImages(final PDDocument document) throws IOException {
		return getPageImages(document, 1);
	}

	/**
	 * 提取PDF文档中的所有图像(使用指定缩放比例)
	 *
	 * @param document PDF文档，允许为null，nul则返回空列表
	 * @param scale 图像缩放比例
	 * @return 包含所有页面图像的列表
	 * @throws IOException 如果渲染失败
	 * @throws IllegalArgumentException 如果scale小于等于0
	 * @since 1.0.0
     */
	public static List<BufferedImage> getPageImages(final PDDocument document, final int scale) throws IOException {
		if (Objects.isNull(document)) {
			return Collections.emptyList();
		}
		Validate.isTrue(scale > 0, "scale 不可为小于等于0");

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
	 * @param document PDF文档，允许为null，nul则返回空列表
	 * @param dpi 图像DPI值
	 * @return 包含所有页面图像的列表
	 * @throws IOException 如果渲染失败
	 * @throws IllegalArgumentException 如果dpi小于等于0
	 * @since 1.0.0
     */
	public static List<BufferedImage> getPageImages(final PDDocument document, final float dpi) throws IOException {
		if (Objects.isNull(document)) {
			return Collections.emptyList();
		}
		Validate.isTrue(dpi > 0, "dpi 不可为小于等于0");

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
	 * @param page 页码(从1开始)
	 * @return 指定页面的图像
	 * @throws IOException 如果渲染失败
	 * @throws IllegalArgumentException 如果document为null或页码无效
	 * @since 1.0.0
     */
	public static BufferedImage getPageImage(final PDDocument document, final int page) throws IOException {
		Validate.notNull(document, "document 不可为 null");
		Validate.isTrue(page > 0, "page 不可为小于等于0");

		PDFRenderer renderer = new PDFRenderer(document);
		return renderer.renderImage(page - 1);
	}

	/**
	 * 提取PDF文档的指定页面为图像(使用指定缩放比例)
	 *
	 * @param document PDF文档
	 * @param page 页码(从1开始)
	 * @param scale 图像缩放比例
	 * @return 指定页面的图像
	 * @throws IOException 如果渲染失败
	 * @throws IllegalArgumentException 如果document为null或页码无效或scale小于等于0
	 * @since 1.0.0
	 */
	public static BufferedImage getPageImage(final PDDocument document, final int page, final int scale) throws IOException {
		Validate.notNull(document, "document 不可为 null");
		Validate.isTrue(page > 0, "page 不可为小于等于0");
		Validate.isTrue(scale > 0, "scale 不可为小于等于0");

		PDFRenderer renderer = new PDFRenderer(document);
		return renderer.renderImage(page - 1, scale);
	}

	/**
	 * 提取PDF文档的指定页面为图像(使用指定DPI)
	 *
	 * @param document PDF文档
	 * @param page 页码(从1开始)
	 * @param dpi 图像DPI值
	 * @return 指定页面的图像
	 * @throws IOException 如果渲染失败
	 * @throws IllegalArgumentException 如果document为null或页码无效或dpi小于等于0
	 * @since 1.0.0
	 */
	public static BufferedImage getPageImage(final PDDocument document, final int page, final float dpi) throws IOException {
		Validate.notNull(document, "document 不可为 null");
		Validate.isTrue(page > 0, "page 不可为小于等于0");
		Validate.isTrue(dpi > 0, "dpi 不可为小于等于0");

		PDFRenderer renderer = new PDFRenderer(document);
		return renderer.renderImageWithDPI(page - 1, dpi);
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
	 * 按页拆分PDF文档(指定每n页拆分)
	 *
	 * @param document 要拆分的PDF文档，允许为null，nul则返回空列表
	 * @param splitPage 每n页拆分一次
	 * @return 拆分后的文档列表
	 * @throws IOException 如果拆分失败
	 * @throws IllegalArgumentException 如果splitPage小于等于0
	 * @since 1.0.0
     */
	public static List<PDDocument> split(final PDDocument document, final int splitPage) throws IOException {
		if (Objects.isNull(document)) {
			return Collections.emptyList();
		}
		Validate.isTrue(splitPage > 0, "splitPage 必须大于0");

		int totalPages = document.getNumberOfPages();
		List<PDDocument> outputFileList = new ArrayList<>((totalPages / splitPage) + (totalPages % splitPage));
		for (int pageNumber = 1; pageNumber <= totalPages; pageNumber += splitPage) {
			PDDocument copyDocument = copy(document, pageNumber, Math.min(pageNumber + splitPage - 1, totalPages));
			outputFileList.add(copyDocument);
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
		Validate.notNull(document, "document 不可为 null");

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
		Validate.isTrue(startPage > 0, "startPage 不可为小于等于0");
		Validate.isTrue(endPage > 0, "endPage 不可为小于等于0");
		Validate.isTrue(startPage <= endPage, "endPage 必须大于等于 startPage");

		int maxPage = Math.min(document.getNumberOfPages(), endPage);
		PDDocument copyDocument = createDocument(document);
		for (int i = startPage; i <= maxPage; i++) {
			PDPage page = document.getPage(i - 1);
			copyDocument.importPage(page);
		}
		return copyDocument;
	}

	/**
	 * 复制PDF文档的指定页码集合
	 *
	 * @param document 源PDF文档
	 * @param pages 要复制的页码集合(1-based)，允许为空，空则返回空文档
	 * @return 复制后的文档对象
	 * @throws IOException 如果复制失败
	 * @throws IllegalArgumentException 如果document为null
	 * @since 1.0.0
	 */
	public static PDDocument copy(final PDDocument document, final Collection<Integer> pages) throws IOException {
		Validate.notNull(document, "document 不可为 null");
		if (Objects.isNull(pages) || pages.isEmpty()) {
			return createDocument(document);
		}

		List<Integer> validPages = checkPages(document, pages);
		PDDocument copyDocument = createDocument(document);
		for (Integer pageNumber : validPages) {
			PDPage sourcePage = document.getPage(pageNumber - 1);
			copyDocument.importPage(sourcePage);
		}
		return copyDocument;
	}

	/**
	 * 获取PDF文档的书签列表
	 *
	 * @param document PDF文档，允许为null，nul则返回空列表
	 * @return 书签列表
	 * @throws IOException 如果读取书签失败
	 * @since 1.0.0
     */
	public static List<Bookmark> getBookmarks(final PDDocument document) throws IOException {
		if (Objects.isNull(document)) {
			return Collections.emptyList();
		}
		PDDocumentOutline outline = document.getDocumentCatalog().getDocumentOutline();
		if (Objects.isNull(outline)) {
			return Collections.emptyList();
		}
		List<Bookmark> bookmarks = new ArrayList<>();
		parseOutline(outline, bookmarks);
		return bookmarks;
	}

	/**
	 * 解析PDF文档并构建书签列表
	 *
	 * @param node 大纲节点
	 * @param bookmarks 书签列表
	 * @throws IOException 如果解析失败
	 * @since 1.0.0
     */
	protected static void parseOutline(final PDOutlineNode node, final Collection<Bookmark> bookmarks) throws IOException {
		PDOutlineItem item = node.getFirstChild();
		while (Objects.nonNull(item)) {
			int pageIndex = getPageIndex(item);
			Bookmark bookmark = new Bookmark(item.getTitle(), pageIndex == -1 ? null : pageIndex + 1);
			bookmarks.add(bookmark);
			parseOutline(item, bookmark.children());
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
	 * 将图像添加到PDF文档
	 *
	 * @param document     目标PDF文档
	 * @param imageXObject 要添加的图像对象
	 * @param page         要插入的页码(从1开始)
	 * @param x            绘制图像的x坐标
	 * @param y            绘制图像的y坐标
	 * @param width        图像宽度
	 * @param height       图像高度
	 * @throws IOException 如果图像处理失败
	 * @since 1.0.0
	 */
	protected static void addImageToDocument(final PDDocument document, final PDImageXObject imageXObject, final int page,
											 final int x, final int y, final int width, final int height) throws IOException {
		PDPage PDPage = new PDPage(new PDRectangle(width, height));
		try (PDPageContentStream pageContentStream = new PDPageContentStream(document, PDPage)) {
			pageContentStream.drawImage(imageXObject, x, y);
		}
		PDPage beforePage = document.getPage(page);
		document.getPages().insertBefore(beforePage, beforePage);
	}

	/**
	 * 检查页码集合的有效性并返回有效的页码列表
	 *
	 * @param document PDF文档
	 * @param pages    要检查的页码集合
	 * @return 有效的页码列表(已排序)
	 * @throws IllegalArgumentException 如果页码无效
	 * @since 1.0.0
	 */
	protected static List<Integer> checkPages(PDDocument document, Collection<Integer> pages) {
		int maxPageNumber = document.getNumberOfPages();
		return pages.stream()
			.distinct()
			.filter(pageNumber -> Objects.nonNull(pageNumber) && pageNumber >= 1 && pageNumber <= maxPageNumber)
			.sorted(Integer::compareTo)
			.toList();
	}
}