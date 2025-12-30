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
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
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
import java.util.stream.Collectors;

/**
 * PDF文档高级操作工具类
 * <p>
 * 基于Apache PDFBox 3.x封装的高阶PDF文档处理工具，提供线程安全的静态方法集。
 * 本工具类针对常见PDF操作场景进行了优化封装，简化了原生API的使用复杂度。
 * </p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li><strong>智能内存管理</strong> - 根据文件大小自动选择最优处理模式（内存/混合/临时文件）</li>
 *   <li><strong>全面的I/O支持</strong> - 支持文件、字节数组、输入流等多种数据源，统一异常处理</li>
 *   <li><strong>线程安全</strong> - 所有方法均为无状态静态方法，可安全用于多线程环境</li>
 *   <li><strong>严格的参数校验</strong> - 使用Validate进行前置条件检查，提供清晰的错误信息</li>
 *   <li><strong>元数据保留</strong> - 所有操作自动保留原始文档属性和元数据</li>
 * </ul>
 *
 * <h3>内存管理策略</h3>
 * <table border="1">
 *   <caption>根据文件大小自动选择处理模式</caption>
 *   <tr><th>文件大小</th><th>处理模式</th><th>内存限制</th><th>适用场景</th></tr>
 *   <tr><td>&lt; 50MB</td><td>纯内存</td><td>无限制</td><td>小文件快速处理</td></tr>
 *   <tr><td>50MB-500MB</td><td>混合模式</td><td>100MB</td><td>中等文件平衡处理</td></tr>
 *   <tr><td>&gt; 500MB</td><td>临时文件</td><td>无内存限制</td><td>大文件稳定处理</td></tr>
 * </table>
 *
 * <h3>典型使用场景</h3>
 * <ol>
 *   <li>PDF文档合并与拆分</li>
 *   <li>页面提取与图像转换</li>
 *   <li>文档书签管理</li>
 *   <li>PDF与图像互转</li>
 * </ol>
 *
 * @author pangju666
 * @version 1.0.0
 * @see org.apache.pdfbox.pdmodel.PDDocument
 * @see org.apache.pdfbox.multipdf.PDFMergerUtility
 * @see <a href="https://pdfbox.apache.org/">Apache PDFBox官网</a>
 * @since 1.0.0
 */
public class PDDocumentUtils {
	/**
	 * 纯内存处理阈值（50MB）
	 * <p>小于此大小的PDF文件将完全在内存中处理</p>
	 *
	 * @since 1.0.0
	 */
	public static final long MIN_PDF_BYTES = 50 * 1024 * 1024;
	/**
	 * 临时文件处理阈值（500MB）
	 * <p>大于此大小的PDF文件将使用临时文件处理</p>
	 *
	 * @since 1.0.0
	 */
	public static final long MAX_PDF_BYTES = 500 * 1024 * 1024;
	/**
	 * 混合模式内存上限（100MB）
	 * <p>50MB-500MB文件使用的内存缓冲区大小</p>
	 *
	 * @since 1.0.0
	 */
	public static final long MIXED_MAX_MAIN_MEMORY_BYTES = 100 * 1024 * 1024;

	/**
	 * 纯内存处理模式的内存使用配置
	 * <p>
	 * 此模式将所有PDF内容完全加载到内存中处理，适用于小文件(&lt;50MB)的高性能操作。
	 * 特点：
	 * <ul>
	 *   <li>处理速度最快</li>
	 *   <li>内存占用与文件大小成正比</li>
	 *   <li>不产生临时文件</li>
	 * </ul>
	 *
	 * @see MemoryUsageSetting#setupMainMemoryOnly()
	 * @see #MIN_PDF_BYTES
	 * @since 1.0.0
	 */
	public static final MemoryUsageSetting MAIN_MEMORY_ONLY_MEMORY_USAGE_SETTING = MemoryUsageSetting.setupMainMemoryOnly();
	/**
	 * 纯临时文件处理模式的内存使用配置
	 * <p>
	 * 此模式使用磁盘临时文件处理PDF内容，适用于大文件(&gt;500MB)的稳定处理。
	 * 特点：
	 * <ul>
	 *   <li>内存占用最低</li>
	 *   <li>处理速度较慢</li>
	 *   <li>适合内存受限环境</li>
	 * </ul>
	 *
	 * @see MemoryUsageSetting#setupTempFileOnly()
	 * @see #MAX_PDF_BYTES
	 * @since 1.0.0
	 */
	public static final MemoryUsageSetting TEMP_FILE_ONLY_MEMORY_USAGE_SETTING = MemoryUsageSetting.setupTempFileOnly();
	/**
	 * 混合内存处理模式的内存使用配置
	 * <p>
	 * 此模式结合内存和临时文件处理，适用于中等大小文件(50MB-500MB)的平衡处理。
	 * 特点：
	 * <ul>
	 *   <li>内存使用限制为100MB</li>
	 *   <li>自动溢出到临时文件</li>
	 *   <li>性能与内存使用的折中方案</li>
	 * </ul>
	 *
	 * @see MemoryUsageSetting#setupMixed(long)
	 * @see #MIXED_MAX_MAIN_MEMORY_BYTES
	 * @since 1.0.0
	 */
	public static final MemoryUsageSetting MIXED_PAGE_MEMORY_USAGE_SETTING = MemoryUsageSetting.setupMixed(MIXED_MAX_MAIN_MEMORY_BYTES);

	protected PDDocumentUtils() {
	}

	/**
	 * 根据PDF文件大小自动选择最优内存处理策略
	 * <p>
	 * 基于预设的阈值({@link #MIN_PDF_BYTES}和{@link #MAX_PDF_BYTES})，
	 * 智能选择以下三种处理模式之一：
	 * <ul>
	 *   <li><b>纯内存模式</b> - 文件小于50MB时使用，性能最佳</li>
	 *   <li><b>混合模式</b> - 文件50MB-500MB时使用，平衡性能与内存</li>
	 *   <li><b>临时文件模式</b> - 文件大于500MB时使用，内存占用最低</li>
	 * </ul>
	 * </p>
	 *
	 * @param fileSize PDF文件大小(字节)，必须为非负数
	 * @return 最适合的内存使用策略配置
	 * @throws IllegalArgumentException 如果fileSize为负数
	 * @see #MAIN_MEMORY_ONLY_MEMORY_USAGE_SETTING
	 * @see #MIXED_PAGE_MEMORY_USAGE_SETTING
	 * @see #TEMP_FILE_ONLY_MEMORY_USAGE_SETTING
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
	 * 验证指定文件是否为有效的PDF文档
	 * <p>
	 * 通过检测文件内容的MIME类型判断是否为PDF文档，支持各种PDF版本(包括PDF/A等变体)。
	 * 此方法会读取文件头部内容进行验证，不会加载整个文件。
	 * </p>
	 *
	 * @param file 待验证的文件对象，不可为null
	 * @return 如果是有效的PDF文档返回true，否则返回false
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当file参数为null时抛出
	 * @see PdfConstants#PDF_MIME_TYPE
	 * @see FileUtils#isMimeType(File, String)
	 * @since 1.0.0
	 */
	public static boolean isPDF(final File file) throws IOException {
		return FileUtils.isMimeType(file, PdfConstants.PDF_MIME_TYPE);
	}

	/**
	 * 验证字节数组是否为有效的PDF文档内容
	 * <p>
	 * 通过检测字节数组的MIME类型判断是否为PDF文档，支持各种PDF版本。
	 * 此方法仅检查字节数组头部特征，不会解析整个内容。
	 * </p>
	 *
	 * @param bytes 待验证的字节数组，可为null或空数组
	 * @return 如果是有效的PDF文档内容返回true，否则返回false
	 * @see PdfConstants#PDF_MIME_TYPE
	 * @see IOConstants#getDefaultTika()
	 * @since 1.0.0
	 */
	public static boolean isPDF(final byte[] bytes) {
		return ArrayUtils.isNotEmpty(bytes) &&
			PdfConstants.PDF_MIME_TYPE.equals(IOConstants.getDefaultTika().detect(bytes));
	}

	/**
	 * 验证输入流是否为有效的PDF文档
	 * <p>
	 * 通过检测输入流内容的MIME类型判断是否为PDF文档，支持各种PDF版本。
	 * 此方法会读取流头部内容进行验证，不会消耗整个流。
	 * 注意：调用后流的位置可能会发生变化。
	 * </p>
	 *
	 * @param inputStream 待验证的输入流，不可为null
	 * @return 如果是有效的PDF文档返回true，否则返回false
	 * @throws IOException              当流读取失败时抛出
	 * @throws IllegalArgumentException 当inputStream参数为null时抛出
	 * @see PdfConstants#PDF_MIME_TYPE
	 * @see IOConstants#getDefaultTika()
	 * @since 1.0.0
	 */
	public static boolean isPDF(final InputStream inputStream) throws IOException {
		return Objects.nonNull(inputStream) &&
			PdfConstants.PDF_MIME_TYPE.equals(IOConstants.getDefaultTika().detect(inputStream));
	}

	/**
	 * 创建新的PDF文档并完整复制源文档元数据
	 * <p>
	 * 复制内容包括：
	 * <ul>
	 *   <li>PDF版本信息</li>
	 *   <li>文档信息(作者、标题等)</li>
	 *   <li>XMP元数据(如果存在)</li>
	 * </ul>
	 * 注意：此方法不会复制文档内容(页面、书签等)。
	 * </p>
	 *
	 * @param source 源PDF文档对象，不可为null
	 * @return 包含源文档元数据的新PDDocument实例
	 * @throws IllegalArgumentException 如果source参数为null
	 * @see PDDocument#setVersion(float)
	 * @see PDDocument#setDocumentInformation(org.apache.pdfbox.pdmodel.PDDocumentInformation)
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
	 * 从文件系统加载PDF文档
	 * <p>
	 * 此方法会：
	 * <ol>
	 *   <li>验证文件存在且可读</li>
	 *   <li>检测文件MIME类型确保是有效的PDF文档</li>
	 *   <li>根据文件大小自动选择最优内存处理策略</li>
	 *   <li>加载并返回PDDocument实例</li>
	 * </ol>
	 * 注意：调用方负责关闭返回的PDDocument对象。
	 * </p>
	 *
	 * @param file 要加载的PDF文件，不可为null
	 * @return 加载成功的PDDocument实例，不会返回null
	 * @throws FileNotFoundException    当文件不存在或不可读时抛出
	 * @throws IllegalArgumentException 当文件不是有效的PDF文档时抛出
	 * @throws IOException              当文件读取失败或PDF解析错误时抛出
	 * @see #computeMemoryUsageSetting(long)
	 * @see Loader#loadPDF(File)
	 * @since 1.0.0
	 */
	public static PDDocument getDocument(final File file) throws IOException {
		if (!FileUtils.isMimeType(file, PdfConstants.PDF_MIME_TYPE)) {
			throw new IllegalArgumentException("不是一个 PDF 文件");
		}
		return Loader.loadPDF(file, computeMemoryUsageSetting(file.length()).streamCache);
	}

	/**
	 * 从加密的PDF文件加载文档
	 * <p>
	 * 此方法会：
	 * <ol>
	 *   <li>验证文件存在且可读</li>
	 *   <li>检测文件MIME类型确保是有效的PDF文档</li>
	 *   <li>根据文件大小自动选择最优内存处理策略</li>
	 *   <li>使用提供的密码解密并加载文档</li>
	 * </ol>
	 * 注意：调用方负责关闭返回的PDDocument对象。
	 * </p>
	 *
	 * @param file     要加载的加密PDF文件，不可为null
	 * @param password 文档解密密码，不可为null或空字符串
	 * @return 解密并加载成功的PDDocument实例，不会返回null
	 * @throws FileNotFoundException    当文件不存在或不可读时抛出
	 * @throws IllegalArgumentException 当文件不是有效的PDF文档或密码无效时抛出
	 * @throws IOException              当文件读取失败、PDF解析错误或密码错误时抛出
	 * @see #computeMemoryUsageSetting(long)
	 * @see Loader#loadPDF(File, String)
	 * @since 1.0.0
	 */
	public static PDDocument getDocument(final File file, final String password) throws IOException {
		if (!FileUtils.isMimeType(file, PdfConstants.PDF_MIME_TYPE)) {
			throw new IllegalArgumentException("不是一个 PDF 文件");
		}
		return Loader.loadPDF(file, password, computeMemoryUsageSetting(file.length()).streamCache);
	}

	/**
	 * 将图像添加到PDF文档的最后一页
	 * <p>
	 * 此方法会自动：
	 * <ul>
	 *   <li>创建新页面(如果文档为空)</li>
	 *   <li>保持图像原始尺寸</li>
	 *   <li>将图像放置在页面左上角(0,0)坐标</li>
	 * </ul>
	 * 支持常见图像格式：JPEG, PNG, BMP, GIF等。
	 * </p>
	 *
	 * @param document 目标PDF文档对象，不可为null
	 * @param bytes    图像字节数组，不可为null或空
	 * @throws IOException              当图像处理失败或格式不支持时抛出
	 * @throws IllegalArgumentException 当document为null或bytes为空时抛出
	 * @see #addImage(PDDocument, byte[], int)
	 * @since 1.0.0
	 */
	public static void addImage(final PDDocument document, final byte[] bytes) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		addImage(document, bytes, document.getNumberOfPages() + 1);
	}

	/**
	 * 将图像添加到PDF文档的最后一页(自定义尺寸和位置)
	 * <p>
	 * 此方法会自动创建新页面(如果文档为空)，并按照指定参数绘制图像。
	 * 坐标系统说明：
	 * <ul>
	 *   <li>原点(0,0)位于页面左下角</li>
	 *   <li>x轴向右延伸，y轴向上延伸</li>
	 *   <li>单位：PDF点(1点=1/72英寸)</li>
	 * </ul>
	 * </p>
	 *
	 * @param document 目标PDF文档对象，不可为null
	 * @param bytes    图像字节数组，不可为null或空
	 * @param x        图像左下角x坐标(点)
	 * @param y        图像左下角y坐标(点)
	 * @param width    绘制宽度(点)
	 * @param height   绘制高度(点)
	 * @throws IOException              当图像处理失败或格式不支持时抛出
	 * @throws IllegalArgumentException 当document为null或bytes为空时抛出
	 * @see #addImage(PDDocument, byte[], int, int, int, int, int)
	 * @since 1.0.0
	 */
	public static void addImage(final PDDocument document, final byte[] bytes, final int x, final int y,
								final int width, final int height) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		addImage(document, bytes, document.getNumberOfPages() + 1, x, y, width, height);
	}

	/**
	 * 将图像添加到PDF文档的指定页码
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>验证文档和图像数据有效性</li>
	 *   <li>自动创建指定页码的页面(如果不存在)</li>
	 *   <li>保持图像原始尺寸</li>
	 *   <li>将图像放置在页面左上角(0,0)坐标</li>
	 * </ul>
	 * 支持常见图像格式：JPEG, PNG, BMP, GIF等。
	 * </p>
	 *
	 * @param document 目标PDF文档对象，不可为null
	 * @param bytes    图像字节数组，不可为null或空
	 * @param page     目标页码(从1开始)
	 * @throws IOException              当图像处理失败或格式不支持时抛出
	 * @throws IllegalArgumentException 当document为null、bytes为空或页码无效时抛出
	 * @see #addImageToDocument(PDDocument, PDImageXObject, int, int, int, int, int)
	 * @since 1.0.0
	 */
	public static void addImage(final PDDocument document, final byte[] bytes, final int page) throws IOException {
		Validate.notNull(document, "document 不可为 null");
		Validate.isTrue(ArrayUtils.isNotEmpty(bytes), "bytes 不可为空");

		PDImageXObject imageObject = PDImageXObject.createFromByteArray(document, bytes, null);
		addImageToDocument(document, imageObject, page, 0, 0, imageObject.getWidth(), imageObject.getHeight());
	}

	/**
	 * 将图像添加到PDF文档的指定页码(自定义尺寸和位置)
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>验证文档和图像数据有效性</li>
	 *   <li>自动创建指定页码的页面(如果不存在)</li>
	 *   <li>按照指定参数绘制图像</li>
	 * </ul>
	 * 坐标系统说明：
	 * <ul>
	 *   <li>原点(0,0)位于页面左下角</li>
	 *   <li>x轴向右延伸，y轴向上延伸</li>
	 *   <li>单位：PDF点(1点=1/72英寸)</li>
	 * </ul>
	 * </p>
	 *
	 * @param document 目标PDF文档对象，不可为null
	 * @param bytes    图像字节数组，不可为null或空
	 * @param page     目标页码(从1开始)
	 * @param x        图像左下角x坐标(点)
	 * @param y        图像左下角y坐标(点)
	 * @param width    绘制宽度(点)
	 * @param height   绘制高度(点)
	 * @throws IOException              当图像处理失败或格式不支持时抛出
	 * @throws IllegalArgumentException 当document为null、bytes为空或页码无效时抛出
	 * @see #addImageToDocument(PDDocument, PDImageXObject, int, int, int, int, int)
	 * @since 1.0.0
	 */
	public static void addImage(final PDDocument document, final byte[] bytes, final int page,
								final int x, final int y, final int width, final int height) throws IOException {
		Validate.notNull(document, "document 不可为 null");
		Validate.isTrue(ArrayUtils.isNotEmpty(bytes), "bytes 不可为空");

		PDImageXObject imageObject = PDImageXObject.createFromByteArray(document, bytes, null);
		addImageToDocument(document, imageObject, page, x, y, width, height);
	}

	/**
	 * 将图像文件添加到PDF文档的最后一页
	 * <p>
	 * 此方法会自动：
	 * <ul>
	 *   <li>创建新页面(如果文档为空)</li>
	 *   <li>保持图像原始尺寸</li>
	 *   <li>将图像放置在页面左上角(0,0)坐标</li>
	 * </ul>
	 * 支持常见图像格式：JPEG, PNG, BMP, GIF等。
	 * </p>
	 *
	 * @param document  目标PDF文档对象，不可为null
	 * @param imageFile 图像文件对象，不可为null且必须存在
	 * @throws IOException              当图像处理失败、文件不存在或格式不支持时抛出
	 * @throws IllegalArgumentException 当document或imageFile为null时抛出
	 * @see #addImage(PDDocument, File, int)
	 * @since 1.0.0
	 */
	public static void addImage(final PDDocument document, final File imageFile) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		addImage(document, imageFile, document.getNumberOfPages() + 1);
	}

	/**
	 * 将图像文件添加到PDF文档的最后一页(自定义尺寸和位置)
	 * <p>
	 * 此方法会自动创建新页面(如果文档为空)，并按照指定参数绘制图像。
	 * 坐标系统说明：
	 * <ul>
	 *   <li>原点(0,0)位于页面左下角</li>
	 *   <li>x轴向右延伸，y轴向上延伸</li>
	 *   <li>单位：PDF点(1点=1/72英寸)</li>
	 * </ul>
	 * </p>
	 *
	 * @param document  目标PDF文档对象，不可为null
	 * @param imageFile 图像文件对象，不可为null且必须存在
	 * @param x         图像左下角x坐标(点)
	 * @param y         图像左下角y坐标(点)
	 * @param width     绘制宽度(点)
	 * @param height    绘制高度(点)
	 * @throws IOException              当图像处理失败、文件不存在或格式不支持时抛出
	 * @throws IllegalArgumentException 当document或imageFile为null时抛出
	 * @see #addImage(PDDocument, File, int, int, int, int, int)
	 * @since 1.0.0
	 */
	public static void addImage(final PDDocument document, final File imageFile, final int x, final int y,
								final int width, final int height) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		addImage(document, imageFile, document.getNumberOfPages() + 1, x, y, width, height);
	}

	/**
	 * 将图像文件添加到PDF文档的指定页码
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>验证文档和图像文件有效性</li>
	 *   <li>自动创建指定页码的页面(如果不存在)</li>
	 *   <li>保持图像原始尺寸</li>
	 *   <li>将图像放置在页面左上角(0,0)坐标</li>
	 * </ul>
	 * 支持常见图像格式：JPEG, PNG, BMP, GIF等。
	 * </p>
	 *
	 * @param document  目标PDF文档对象，不可为null
	 * @param imageFile 图像文件对象，不可为null且必须存在
	 * @param page      目标页码(从1开始)
	 * @throws IOException              当图像处理失败、文件不存在或格式不支持时抛出
	 * @throws IllegalArgumentException 当document或imageFile为null，或页码无效时抛出
	 * @see #addImageToDocument(PDDocument, PDImageXObject, int, int, int, int, int)
	 * @since 1.0.0
	 */
	public static void addImage(final PDDocument document, final File imageFile, final int page) throws IOException {
		Validate.isTrue(page > 0, "page 不可为小于等于0");
		FileUtils.checkFile(imageFile, "imageFile 不可为 null");

		PDImageXObject imageObject = PDImageXObject.createFromFileByContent(imageFile, document);
		addImageToDocument(document, imageObject, page, 0, 0, imageObject.getWidth(), imageObject.getHeight());
	}

	/**
	 * 将图像文件添加到PDF文档的指定页码(自定义尺寸和位置)
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>验证文档和图像文件有效性</li>
	 *   <li>自动创建指定页码的页面(如果不存在)</li>
	 *   <li>按照指定参数绘制图像</li>
	 * </ul>
	 * 坐标系统说明：
	 * <ul>
	 *   <li>原点(0,0)位于页面左下角</li>
	 *   <li>x轴向右延伸，y轴向上延伸</li>
	 *   <li>单位：PDF点(1点=1/72英寸)</li>
	 * </ul>
	 * </p>
	 *
	 * @param document  目标PDF文档对象，不可为null
	 * @param imageFile 图像文件对象，不可为null且必须存在
	 * @param page      目标页码(从1开始)
	 * @param x         图像左下角x坐标(点)
	 * @param y         图像左下角y坐标(点)
	 * @param width     绘制宽度(点)
	 * @param height    绘制高度(点)
	 * @throws IOException              当图像处理失败、文件不存在或格式不支持时抛出
	 * @throws IllegalArgumentException 当document或imageFile为null，或页码无效时抛出
	 * @see #addImageToDocument(PDDocument, PDImageXObject, int, int, int, int, int)
	 * @since 1.0.0
	 */
	public static void addImage(final PDDocument document, final File imageFile, final int page,
								final int x, final int y, final int width, final int height) throws IOException {
		FileUtils.checkFile(imageFile, "imageFile 不可为 null");

		PDImageXObject imageObject = PDImageXObject.createFromFileByContent(imageFile, document);
		addImageToDocument(document, imageObject, page, x, y, width, height);
	}

	/**
	 * 将PDF文档指定页面渲染为图像并返回(使用默认缩放比例1)
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>自动处理null输入，返回空列表</li>
	 *   <li>使用原始尺寸(100%缩放比例)渲染页面</li>
	 *   <li>支持批量获取多个页面的图像</li>
	 * </ul>
	 * 注意：页码从1开始，超出范围的页码会被自动过滤。
	 * </p>
	 *
	 * @param document PDF文档对象，允许为null，null则返回空列表
	 * @param pages    要获取的页码集合(从1开始)
	 * @return 包含指定页面图像的列表，不会返回null
	 * @throws IOException 当页面渲染失败时抛出
	 * @see #getPagesAsImage(PDDocument, int, Collection)
	 * @since 1.0.0
	 */
	public static List<BufferedImage> getPagesAsImage(final PDDocument document, final Collection<Integer> pages) throws IOException {
		return getPagesAsImage(document, 1, pages);
	}

	/**
	 * 将PDF文档指定页面渲染为图像并返回(使用指定缩放比例)
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>自动处理null输入，返回空列表</li>
	 *   <li>按照指定缩放比例渲染页面</li>
	 *   <li>支持批量获取多个页面的图像</li>
	 *   <li>自动过滤无效页码</li>
	 * </ul>
	 * 缩放比例说明：
	 * <ul>
	 *   <li>1.0表示原始尺寸(100%)</li>
	 *   <li>0.5表示缩小一半(50%)</li>
	 *   <li>2.0表示放大一倍(200%)</li>
	 * </ul>
	 * </p>
	 *
	 * @param document PDF文档对象，允许为null，null则返回空列表
	 * @param scale    图像缩放比例，必须大于0
	 * @param pages    要获取的页码集合(从1开始)，允许为null或空，则返回空列表
	 * @return 包含指定页面图像的列表，不会返回null
	 * @throws IOException              当页面渲染失败时抛出
	 * @throws IllegalArgumentException 当scale小于等于0时抛出
	 * @see #getPagesAsImage(PDDocument, Collection)
	 * @since 1.0.0
	 */
	public static List<BufferedImage> getPagesAsImage(final PDDocument document, final int scale,
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
	 * 将PDF文档指定页面渲染为图像并返回(使用指定DPI)
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>自动处理null输入，返回空列表</li>
	 *   <li>按照指定DPI值渲染页面</li>
	 *   <li>支持批量获取多个页面的图像</li>
	 *   <li>自动过滤无效页码</li>
	 * </ul>
	 * DPI说明：
	 * <ul>
	 *   <li>72 DPI 为标准屏幕分辨率</li>
	 *   <li>300 DPI 为打印质量</li>
	 *   <li>值越高图像质量越好，但内存消耗越大</li>
	 * </ul>
	 * </p>
	 *
	 * @param document PDF文档对象，允许为null，null则返回空列表
	 * @param dpi      图像DPI值，必须大于0
	 * @param pages    要获取的页码集合(从1开始)，允许为null或空，则返回空列表
	 * @return 包含指定页面图像的列表，不会返回null
	 * @throws IOException              当页面渲染失败时抛出
	 * @throws IllegalArgumentException 当dpi小于等于0时抛出
	 * @see #getPagesAsImage(PDDocument, Collection)
	 * @see #getPagesAsImage(PDDocument, int, Collection)
	 * @since 1.0.0
	 */
	public static List<BufferedImage> getPagesAsImageWithDPI(final PDDocument document, final float dpi,
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
	 * 将PDF文档指定页面渲染为图像并返回(使用默认缩放比例1)
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>自动处理null输入，返回空列表</li>
	 *   <li>使用原始尺寸(100%缩放比例)渲染页面</li>
	 *   <li>自动调整超出文档范围的结束页码</li>
	 * </ul>
	 * 注意：页码从1开始，结束页码必须大于等于起始页码。
	 * </p>
	 *
	 * @param document  PDF文档对象，允许为null，null则返回空列表
	 * @param startPage 起始页码(从1开始)
	 * @param endPage   结束页码(从1开始)
	 * @return 包含指定页面范围图像的列表，不会返回null
	 * @throws IOException              当页面渲染失败时抛出
	 * @throws IllegalArgumentException 当页码无效时抛出
	 * @see #getPagesAsImage(PDDocument, int, int, int)
	 * @since 1.0.0
	 */
	public static List<BufferedImage> getPagesAsImage(final PDDocument document, final int startPage, final int endPage) throws IOException {
		return getPagesAsImage(document, 1, startPage, endPage);
	}

	/**
	 * 将PDF文档指定页面渲染为图像并返回(使用指定缩放比例)
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>自动处理null输入，返回空列表</li>
	 *   <li>按照指定缩放比例渲染页面</li>
	 *   <li>自动调整超出文档范围的结束页码</li>
	 * </ul>
	 * 缩放比例说明：
	 * <ul>
	 *   <li>1.0表示原始尺寸(100%)</li>
	 *   <li>0.5表示缩小一半(50%)</li>
	 *   <li>2.0表示放大一倍(200%)</li>
	 * </ul>
	 * </p>
	 *
	 * @param document  PDF文档对象，允许为null，null则返回空列表
	 * @param scale     图像缩放比例，必须大于0
	 * @param startPage 起始页码(从1开始)
	 * @param endPage   结束页码(从1开始)
	 * @return 包含指定页面范围图像的列表，不会返回null
	 * @throws IOException              当页面渲染失败时抛出
	 * @throws IllegalArgumentException 当scale小于等于0或页码无效时抛出
	 * @see #getPagesAsImage(PDDocument, int, int)
	 * @since 1.0.0
	 */
	public static List<BufferedImage> getPagesAsImage(final PDDocument document, final int scale, final int startPage, final int endPage) throws IOException {
		if (Objects.isNull(document)) {
			return Collections.emptyList();
		}

		Validate.isTrue(scale > 0, "scale 不可为小于等于0");
		checkArgs(startPage, endPage);

		int maxPage = Math.min(document.getNumberOfPages(), endPage);
		PDFRenderer renderer = new PDFRenderer(document);
		List<BufferedImage> images = new ArrayList<>(endPage - startPage);
		for (int i = startPage; i <= maxPage; i++) {
			BufferedImage image = renderer.renderImage(i - 1, scale);
			images.add(image);
		}
		return images;
	}

	/**
	 * 将PDF文档指定页面渲染为图像并返回(使用指定DPI)
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>自动处理null输入，返回空列表</li>
	 *   <li>按照指定DPI值渲染页面</li>
	 *   <li>自动调整超出文档范围的结束页码</li>
	 * </ul>
	 * DPI说明：
	 * <ul>
	 *   <li>72 DPI 为标准屏幕分辨率</li>
	 *   <li>300 DPI 为打印质量</li>
	 *   <li>值越高图像质量越好，但内存消耗越大</li>
	 * </ul>
	 * 页码规则：
	 * <ul>
	 *   <li>起始页码必须小于等于结束页码</li>
	 *   <li>页码从1开始</li>
	 *   <li>超出文档范围的页码会自动调整</li>
	 * </ul>
	 * </p>
	 *
	 * @param document  PDF文档对象，允许为null，null则返回空列表
	 * @param dpi       图像DPI值，必须大于0
	 * @param startPage 起始页码(从1开始)
	 * @param endPage   结束页码(从1开始)
	 * @return 包含指定页面范围图像的列表，不会返回null
	 * @throws IOException              当页面渲染失败时抛出
	 * @throws IllegalArgumentException 当dpi小于等于0或页码无效时抛出
	 * @see #getPagesAsImageWithDPI(PDDocument, float, Collection)
	 * @see #getPagesAsImage(PDDocument, int, int)
	 * @since 1.0.0
	 */
	public static List<BufferedImage> getPagesAsImageWithDPI(final PDDocument document, final float dpi,
															 final int startPage, final int endPage) throws IOException {
		if (Objects.isNull(document)) {
			return Collections.emptyList();
		}

		Validate.isTrue(dpi > 0, "dpi 不可为小于等于0");
		checkArgs(startPage, endPage);

		int maxPage = Math.min(document.getNumberOfPages(), endPage);
		PDFRenderer renderer = new PDFRenderer(document);
		List<BufferedImage> images = new ArrayList<>(endPage - startPage);
		for (int i = startPage; i <= maxPage; i++) {
			BufferedImage image = renderer.renderImageWithDPI(i - 1, dpi);
			images.add(image);
		}
		return images;
	}

	/**
	 * 将PDF文档所有页面渲染为图像并返回(使用默认缩放比例1)
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>自动处理null输入，返回空列表</li>
	 *   <li>使用原始尺寸(100%缩放比例)渲染所有页面</li>
	 *   <li>按文档顺序返回所有页面的图像</li>
	 * </ul>
	 * 这是{@link #getPageAsImage(PDDocument, int)}方法的便捷版本，使用默认缩放比例1。
	 * </p>
	 *
	 * @param document PDF文档对象，允许为null，null则返回空列表
	 * @return 包含所有页面图像的列表，不会返回null
	 * @throws IOException 当页面渲染失败时抛出
	 * @see #getPageAsImage(PDDocument, int)
	 * @since 1.0.0
	 */
	public static List<BufferedImage> getPagesAsImage(final PDDocument document) throws IOException {
		return getPagesAsImage(document, 1);
	}

	/**
	 * 将PDF文档所有页面渲染为图像并返回(使用指定缩放比例)
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>自动处理null输入，返回空列表</li>
	 *   <li>按照指定缩放比例渲染所有页面</li>
	 *   <li>按文档顺序返回所有页面的图像</li>
	 * </ul>
	 * 缩放比例说明：
	 * <ul>
	 *   <li>1.0表示原始尺寸(100%)</li>
	 *   <li>0.5表示缩小一半(50%)</li>
	 *   <li>2.0表示放大一倍(200%)</li>
	 * </ul>
	 * </p>
	 *
	 * @param document PDF文档对象，允许为null，null则返回空列表
	 * @param scale    图像缩放比例，必须大于0
	 * @return 包含所有页面图像的列表，不会返回null
	 * @throws IOException              当页面渲染失败时抛出
	 * @throws IllegalArgumentException 当scale小于等于0时抛出
	 * @see #getPagesAsImage(PDDocument)
	 * @since 1.0.0
	 */
	public static List<BufferedImage> getPagesAsImage(final PDDocument document, final int scale) throws IOException {
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
	 * 将PDF文档所有页面渲染为图像并返回(使用指定DPI)
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>自动处理null输入，返回空列表</li>
	 *   <li>按照指定DPI值渲染所有页面</li>
	 *   <li>按文档顺序返回所有页面的图像</li>
	 * </ul>
	 * DPI说明：
	 * <ul>
	 *   <li>72 DPI 为标准屏幕分辨率</li>
	 *   <li>300 DPI 为打印质量</li>
	 *   <li>值越高图像质量越好，但内存消耗越大</li>
	 * </ul>
	 * </p>
	 *
	 * @param document PDF文档对象，允许为null，null则返回空列表
	 * @param dpi      图像DPI值，必须大于0
	 * @return 包含所有页面图像的列表，不会返回null
	 * @throws IOException              当页面渲染失败时抛出
	 * @throws IllegalArgumentException 当dpi小于等于0时抛出
	 * @see #getPagesAsImage(PDDocument)
	 * @see #getPagesAsImage(PDDocument, int)
	 * @since 1.0.0
	 */
	public static List<BufferedImage> getPagesAsImageWithDPI(final PDDocument document, final float dpi) throws IOException {
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
	 * 将PDF文档指定页面渲染为图像并返回(使用默认缩放比例1)
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>使用原始尺寸(100%缩放比例)渲染指定页面</li>
	 *   <li>严格校验输入参数</li>
	 * </ul>
	 * 这是{@link #getPageAsImage(PDDocument, int, int)}方法的便捷版本，使用默认缩放比例1。
	 * </p>
	 *
	 * @param document PDF文档对象，不允许为null
	 * @param page     页码(从1开始)，必须大于0
	 * @return 指定页面的图像
	 * @throws IOException              当页面渲染失败时抛出
	 * @throws IllegalArgumentException 当document为null或页码无效时抛出
	 * @see #getPageAsImage(PDDocument, int, int)
	 * @since 1.0.0
	 */
	public static BufferedImage getPageAsImage(final PDDocument document, final int page) throws IOException {
		Validate.notNull(document, "document 不可为 null");
		Validate.isTrue(page > 0, "page 不可为小于等于0");

		PDFRenderer renderer = new PDFRenderer(document);
		return renderer.renderImage(page - 1);
	}

	/**
	 * 将PDF文档指定页面渲染为图像并返回(使用指定缩放比例)
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>按照指定缩放比例渲染指定页面</li>
	 *   <li>严格校验输入参数</li>
	 * </ul>
	 * 缩放比例说明：
	 * <ul>
	 *   <li>1.0表示原始尺寸(100%)</li>
	 *   <li>0.5表示缩小一半(50%)</li>
	 *   <li>2.0表示放大一倍(200%)</li>
	 * </ul>
	 * </p>
	 *
	 * @param document PDF文档对象，不允许为null
	 * @param page     页码(从1开始)，必须大于0
	 * @param scale    图像缩放比例，必须大于0
	 * @return 指定页面的图像
	 * @throws IOException              当页面渲染失败时抛出
	 * @throws IllegalArgumentException 当document为null或页码无效或scale小于等于0时抛出
	 * @see #getPageAsImage(PDDocument, int)
	 * @since 1.0.0
	 */
	public static BufferedImage getPageAsImage(final PDDocument document, final int page, final int scale) throws IOException {
		Validate.notNull(document, "document 不可为 null");
		Validate.isTrue(page > 0, "page 不可为小于等于0");
		Validate.isTrue(scale > 0, "scale 不可为小于等于0");

		PDFRenderer renderer = new PDFRenderer(document);
		return renderer.renderImage(page - 1, scale);
	}

	/**
	 * 将PDF文档指定页面渲染为图像并返回(使用指定DPI)
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>按照指定DPI值渲染单个页面</li>
	 *   <li>严格校验输入参数</li>
	 * </ul>
	 * DPI说明：
	 * <ul>
	 *   <li>72 DPI 为标准屏幕分辨率</li>
	 *   <li>300 DPI 为打印质量</li>
	 *   <li>值越高图像质量越好，但内存消耗越大</li>
	 * </ul>
	 * 页码规则：
	 * <ul>
	 *   <li>页码从1开始</li>
	 *   <li>必须小于等于文档总页数</li>
	 * </ul>
	 * </p>
	 *
	 * @param document PDF文档对象，不允许为null
	 * @param page     页码(从1开始)，必须大于0
	 * @param dpi      图像DPI值，必须大于0
	 * @return 指定页面的图像
	 * @throws IOException              当页面渲染失败时抛出
	 * @throws IllegalArgumentException 当document为null或页码无效或dpi小于等于0时抛出
	 * @see #getPageAsImage(PDDocument, int)
	 * @see #getPageAsImage(PDDocument, int, int)
	 * @since 1.0.0
	 */
	public static BufferedImage getPageAsImageWithDPI(final PDDocument document, final int page, final float dpi) throws IOException {
		Validate.notNull(document, "document 不可为 null");
		Validate.isTrue(page > 0, "page 不可为小于等于0");
		Validate.isTrue(dpi > 0, "dpi 不可为小于等于0");

		PDFRenderer renderer = new PDFRenderer(document);
		return renderer.renderImageWithDPI(page - 1, dpi);
	}

	/**
	 * 合并多个PDF文档并返回合并后的文档对象
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>按照输入文档的顺序合并所有页面</li>
	 *   <li>使用指定的内存策略优化内存使用</li>
	 *   <li>严格校验输入参数</li>
	 * </ul>
	 * 注意：
	 * <ul>
	 *   <li>合并后的文档将保留所有输入文档的页面内容</li>
	 *   <li>不会修改原始文档对象</li>
	 *   <li>合并顺序与输入集合的迭代顺序一致</li>
	 * </ul>
	 * </p>
	 *
	 * @param documents          要合并的PDF文档集合，不允许为null或空
	 * @param memoryUsageSetting 内存使用策略，不允许为null
	 * @return 合并后的PDDocument对象
	 * @throws IOException              当合并操作失败时抛出
	 * @throws IllegalArgumentException 当参数为null或documents为空时抛出
	 * @see MemoryUsageSetting
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
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>自动处理null输入，返回空列表</li>
	 *   <li>按照指定页数拆分文档</li>
	 *   <li>自动处理最后一组不足拆分页数的情况</li>
	 * </ul>
	 * 拆分规则：
	 * <ul>
	 *   <li>每n页拆分为一个新文档</li>
	 *   <li>最后一组可能少于n页</li>
	 *   <li>拆分后的文档按原始顺序排列</li>
	 * </ul>
	 * </p>
	 *
	 * @param document  要拆分的PDF文档，允许为null，null则返回空列表
	 * @param splitPage 每n页拆分一次，必须大于0
	 * @return 拆分后的文档列表，不会返回null
	 * @throws IOException              当拆分操作失败时抛出
	 * @throws IllegalArgumentException 当splitPage小于等于0时抛出
	 * @see #copy(PDDocument, int, int)
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
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>创建包含源文档所有页面的新文档</li>
	 *   <li>保留源文档的元数据和属性</li>
	 *   <li>严格校验输入参数</li>
	 * </ul>
	 * 这是{@link #copy(PDDocument, int, int)}方法的便捷版本，自动复制全部页面。
	 * </p>
	 *
	 * @param document 源PDF文档对象，不允许为null
	 * @return 包含所有页面的新文档对象
	 * @throws IOException              当复制操作失败时抛出
	 * @throws IllegalArgumentException 当document为null时抛出
	 * @see #copy(PDDocument, int, int)
	 * @since 1.0.0
	 */
	public static PDDocument copy(final PDDocument document) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		return copy(document, 1, document.getNumberOfPages());
	}

	/**
	 * 复制PDF文档的指定页面范围
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>创建包含指定页面范围的新文档</li>
	 *   <li>保留源文档的元数据和属性</li>
	 *   <li>自动调整超出文档范围的结束页码</li>
	 *   <li>严格校验输入参数</li>
	 * </ul>
	 * 页码规则：
	 * <ul>
	 *   <li>页码从1开始</li>
	 *   <li>起始页码必须小于等于结束页码</li>
	 *   <li>超出文档范围的页码会自动调整</li>
	 * </ul>
	 * </p>
	 *
	 * @param document  源PDF文档对象，不允许为null
	 * @param startPage 起始页码(从1开始)，必须大于0
	 * @param endPage   结束页码(从1开始)，必须大于等于起始页码
	 * @return 包含指定页面范围的新文档对象
	 * @throws IOException              当复制操作失败时抛出
	 * @throws IllegalArgumentException 当document为null或页码无效时抛出
	 * @see #copy(PDDocument)
	 * @see #copy(PDDocument, Collection)
	 * @since 1.0.0
	 */
	public static PDDocument copy(final PDDocument document, final int startPage, final int endPage) throws IOException {
		Validate.notNull(document, "document 不可为 null");
		checkArgs(startPage, endPage);

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
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>创建包含指定页码的新文档</li>
	 *   <li>保留源文档的元数据和属性</li>
	 *   <li>自动过滤无效页码</li>
	 *   <li>按页码顺序复制页面</li>
	 * </ul>
	 * 页码规则：
	 * <ul>
	 *   <li>页码从1开始</li>
	 *   <li>超出文档范围的页码会被自动过滤</li>
	 *   <li>重复页码会被自动去重</li>
	 * </ul>
	 * </p>
	 *
	 * @param document 源PDF文档对象，不允许为null
	 * @param pages    要复制的页码集合(从1开始)，允许为null或空，null/空则返回空文档
	 * @return 包含指定页面的新文档对象
	 * @throws IOException              当复制操作失败时抛出
	 * @throws IllegalArgumentException 当document为null时抛出
	 * @see #copy(PDDocument)
	 * @see #copy(PDDocument, int, int)
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
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>递归解析文档大纲结构</li>
	 *   <li>自动处理null输入，返回空列表</li>
	 *   <li>自动处理无大纲的情况</li>
	 *   <li>保留书签的层级结构</li>
	 * </ul>
	 * 书签说明：
	 * <ul>
	 *   <li>书签可能没有关联的页码</li>
	 *   <li>书签按文档中的顺序返回</li>
	 *   <li>子书签会嵌套在父书签中</li>
	 * </ul>
	 * </p>
	 *
	 * @param document PDF文档对象，允许为null，null则返回空列表
	 * @return 包含所有书签的列表，不会返回null
	 * @throws IOException 当读取书签失败时抛出
	 * @see Bookmark
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
		parseOutline(null, outline, bookmarks);
		return bookmarks;
	}

	/**
	 * 递归解析PDF文档大纲结构并构建层级书签列表
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>深度优先遍历大纲节点树</li>
	 *   <li>自动处理所有子节点</li>
	 *   <li>保留书签的完整层级结构</li>
	 *   <li>自动转换页码为1-based格式</li>
	 *   <li>支持父子书签关联</li>
	 * </ul>
	 * 书签特性：
	 * <ul>
	 *   <li>书签可能没有关联的页码(null表示无关联页面)</li>
	 *   <li>子书签会嵌套在父书签的children集合中</li>
	 *   <li>书签按文档中的原始顺序构建</li>
	 * </ul>
	 * </p>
	 *
	 * @param parentId  父书签节点ID，顶层书签时为null
	 * @param node      当前要解析的大纲节点，不允许为null
	 * @param bookmarks 要填充的书签集合，不允许为null
	 * @throws IOException              当解析大纲结构失败时抛出
	 * @throws IllegalArgumentException 当node或bookmarks为null时抛出
	 * @see Bookmark
	 * @see #getPageIndex(PDOutlineItem)
	 * @since 1.0.0
	 */
	protected static void parseOutline(final String parentId, final PDOutlineNode node,
									   final Collection<Bookmark> bookmarks) throws IOException {
		PDOutlineItem item = node.getFirstChild();
		while (Objects.nonNull(item)) {
			int pageIndex = getPageIndex(item);
			Bookmark bookmark = new Bookmark(parentId, item.getTitle(), pageIndex == -1 ? null : pageIndex);
			bookmarks.add(bookmark);
			parseOutline(bookmark.getNodeKey(), item, bookmark.getChildren());
			item = item.getNextSibling();
		}
	}

	/**
	 * 获取大纲项对应的实际页码
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>处理直接关联页面的目的地</li>
	 *   <li>处理通过跳转动作关联的目的地</li>
	 *   <li>自动转换PDFBox的0-based页码为1-based</li>
	 * </ul>
	 * 返回值说明：
	 * <ul>
	 *   <li>返回1-based页码</li>
	 *   <li>返回-1表示无法确定页码</li>
	 *   <li>返回值可直接用于文档操作</li>
	 * </ul>
	 * </p>
	 *
	 * @param item 大纲项对象，不允许为null
	 * @return 对应的1-based页码，无法确定时返回-1
	 * @throws IOException 当解析目的地失败时抛出
	 * @see PDPageDestination
	 * @see PDActionGoTo
	 * @since 1.0.0
	 */
	protected static int getPageIndex(final PDOutlineItem item) throws IOException {
		PDDestination destination = item.getDestination();
		if (destination instanceof PDPageDestination) {
			return ((PDPageDestination) destination).retrievePageNumber();
		}

		PDAction action = item.getAction();
		if (action instanceof PDActionGoTo) {
			destination = ((PDActionGoTo) action).getDestination();
			if (destination instanceof PDPageDestination) {
				return ((PDPageDestination) destination).retrievePageNumber();
			}
		}
		return -1;
	}

	/**
	 * 将图像添加到PDF文档的指定位置
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>创建新页面并插入到指定位置</li>
	 *   <li>按指定坐标和尺寸绘制图像</li>
	 *   <li>严格校验所有输入参数</li>
	 * </ul>
	 * 坐标说明：
	 * <ul>
	 *   <li>坐标系原点(0,0)位于页面左下角</li>
	 *   <li>x轴向右为正方向</li>
	 *   <li>y轴向上为正方向</li>
	 * </ul>
	 * </p>
	 *
	 * @param document     目标PDF文档对象，不允许为null
	 * @param imageXObject 要添加的图像对象，不允许为null
	 * @param page         要插入的1-based页码，必须大于0
	 * @param x            图像左下角的x坐标，必须大于0
	 * @param y            图像左下角的y坐标，必须大于0
	 * @param width        图像宽度，必须大于0
	 * @param height       图像高度，必须大于0
	 * @throws IOException              当图像处理失败时抛出
	 * @throws IllegalArgumentException 当任何参数无效时抛出
	 * @see PDImageXObject
	 * @since 1.0.0
	 */
	protected static void addImageToDocument(final PDDocument document, final PDImageXObject imageXObject, final int page,
											 final int x, final int y, final int width, final int height) throws IOException {
		checkArgs(page, x, y, width, height);

		PDPage newPage = new PDPage(new PDRectangle(width, height));
		try (PDPageContentStream pageContentStream = new PDPageContentStream(document, newPage)) {
			pageContentStream.drawImage(imageXObject, x, y);
		}
		int numberOfPages = document.getNumberOfPages();
		if (page <= numberOfPages) {
			PDPage beforePage = document.getPage(page - 1);
			document.getPages().insertBefore(newPage, beforePage);
		} else {
			document.addPage(newPage);
		}
	}

	/**
	 * 检查并过滤有效的页码集合
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>自动去除重复页码</li>
	 *   <li>过滤null值和无效页码</li>
	 *   <li>按升序排序页码</li>
	 * </ul>
	 * 页码规则：
	 * <ul>
	 *   <li>页码从1开始</li>
	 *   <li>必须小于等于文档总页数</li>
	 *   <li>无效页码会被自动过滤</li>
	 * </ul>
	 * </p>
	 *
	 * @param document PDF文档对象，不允许为null
	 * @param pages    要检查的页码集合，允许为null
	 * @return 有效的页码列表(已排序)，不会返回null
	 * @throws IllegalArgumentException 当document为null时抛出
	 * @since 1.0.0
	 */
	protected static List<Integer> checkPages(final PDDocument document, final Collection<Integer> pages) {
		int maxPageNumber = document.getNumberOfPages();
		return pages.stream()
			.distinct()
			.filter(pageNumber -> Objects.nonNull(pageNumber) && pageNumber >= 1 && pageNumber <= maxPageNumber)
			.sorted(Integer::compareTo)
			.collect(Collectors.toList());
	}

	/**
	 * 校验页码范围参数的有效性
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>校验起始页码必须大于0</li>
	 *   <li>校验结束页码必须大于0</li>
	 *   <li>校验结束页码必须大于等于起始页码</li>
	 * </ul>
	 * 页码规则：
	 * <ul>
	 *   <li>页码从1开始</li>
	 *   <li>必须符合逻辑顺序</li>
	 * </ul>
	 * </p>
	 *
	 * @param startPage 起始页码(从1开始)
	 * @param endPage   结束页码(从1开始)
	 * @throws IllegalArgumentException 当任何参数无效时抛出
	 * @since 1.0.0
	 */
	protected static void checkArgs(final int startPage, final int endPage) {
		Validate.isTrue(startPage > 0, "startPage 不可为小于等于0");
		Validate.isTrue(endPage > 0, "endPage 不可为小于等于0");
		Validate.isTrue(startPage <= endPage, "endPage 必须大于等于 startPage");
	}

	/**
	 * 校验图像位置和尺寸参数的有效性
	 * <p>
	 * 此方法会：
	 * <ul>
	 *   <li>校验页码必须大于0</li>
	 *   <li>校验坐标值必须大于0</li>
	 *   <li>校验尺寸值必须大于0</li>
	 * </ul>
	 * 坐标规则：
	 * <ul>
	 *   <li>坐标系原点(0,0)位于页面左下角</li>
	 *   <li>所有值必须为正数</li>
	 * </ul>
	 * </p>
	 *
	 * @param page   页码(从1开始)
	 * @param x      图像左下角的x坐标
	 * @param y      图像左下角的y坐标
	 * @param width  图像宽度
	 * @param height 图像高度
	 * @throws IllegalArgumentException 当任何参数无效时抛出
	 * @since 1.0.0
	 */
	protected static void checkArgs(final int page, final int x, final int y, final int width, final int height) {
		Validate.isTrue(page > 0, "page 不可为小于等于0");
		Validate.isTrue(x > 0, "x 不可为小于等于0");
		Validate.isTrue(y > 0, "y 不可为小于等于0");
		Validate.isTrue(width > 0, "width 不可为小于等于0");
		Validate.isTrue(height > 0, "height 不可为小于等于0");
	}
}
