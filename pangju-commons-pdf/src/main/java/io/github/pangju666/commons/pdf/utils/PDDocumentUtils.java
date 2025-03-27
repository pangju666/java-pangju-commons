package io.github.pangju666.commons.pdf.utils;

import io.github.pangju666.commons.io.lang.IOConstants;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.pdf.lang.PdfConstants;
import io.github.pangju666.commons.pdf.model.PDFDirectory;
import org.apache.commons.lang3.ArrayUtils;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.ObjIntConsumer;

/**
 * PDF工具类
 *
 * @author pangju666
 * @since 1.0.0
 */
public class PDDocumentUtils {
	protected static final long MIN_PDF_BYTES = 50 * 1024 * 1024;
	protected static final long MAX_PDF_BYTES = 500 * 1024 * 1024;
	protected static final long MIXED_MAX_MAIN_MEMORY_BYTES = 100 * 1024 * 1024;

	protected PDDocumentUtils() {
	}

	public static MemoryUsageSetting computeMemoryUsageSetting(final long size) {
		if (size < MIN_PDF_BYTES) {
			return MemoryUsageSetting.setupMainMemoryOnly();
		} else if (size > MAX_PDF_BYTES) {
			return MemoryUsageSetting.setupTempFileOnly();
		} else {
			return MemoryUsageSetting.setupMixed(MIXED_MAX_MAIN_MEMORY_BYTES);
		}
	}

	/**
	 * 判断是否为PDF文件
	 *
	 * @param file 文件
	 * @return 是否为PDF文件
	 */
	public static boolean isPDF(final File file) throws IOException {
		return FileUtils.exist(file) && file.isFile() && PdfConstants.PDF_MIME_TYPE.equals(FileUtils.getMimeType(file));
	}

	public static boolean isPDF(final byte[] bytes) {
		return ArrayUtils.isNotEmpty(bytes) &&
			PdfConstants.PDF_MIME_TYPE.equals(IOConstants.getDefaultTika().detect(bytes));
	}

	public static boolean isPDF(final InputStream inputStream) throws IOException {
		return Objects.nonNull(inputStream) &&
			PdfConstants.PDF_MIME_TYPE.equals(IOConstants.getDefaultTika().detect(inputStream));
	}

	/**
	 * 创建新文档，并拷贝源文档的属性至新文档
	 *
	 * @param sourceDocument 源文档
	 * @return 创建的新文档
	 */
	public static PDDocument createDocument(final PDDocument sourceDocument) {
		PDDocument document = new PDDocument();
		// 复制文档属性
		document.getDocument().setVersion(sourceDocument.getVersion());
		document.setDocumentInformation(sourceDocument.getDocumentInformation());
		document.getDocumentCatalog().setViewerPreferences(sourceDocument.getDocumentCatalog().getViewerPreferences());
		return document;
	}

	/**
	 * 获取文档对象
	 *
	 * @param inputStream 文档输入流
	 */
	public static PDDocument getDocument(final InputStream inputStream) throws IOException {
		return Loader.loadPDF(inputStream.readAllBytes());
	}

	public static PDDocument getDocument(final byte[] bytes) throws IOException {
		if (!isPDF(bytes)) {
			return null;
		}
		return Loader.loadPDF(bytes);
	}

	/**
	 * 获取文档对象
	 *
	 * @param documentPath 文档路径
	 */
	public static PDDocument getDocument(final String documentPath) throws IOException {
		return getDocument(new File(documentPath));
	}

	/**
	 * 获取文档对象
	 *
	 * @param documentFile 文档文件
	 */
	public static PDDocument getDocument(final File documentFile) throws IOException {
		if (FileUtils.notExist(documentFile)) {
			return null;
		}
		if (!isPDF(documentFile)) {
			return null;
		}
		return Loader.loadPDF(documentFile, computeMemoryUsageSetting(documentFile.length()).streamCache);
	}

	public static void convertImageToPdf(final PDDocument document, final List<File> imageFiles) throws IOException {
		for (File imageFile : imageFiles) {
			convertImageToPdf(document, imageFile);
		}
	}

	public static void convertImageToPdf(final PDDocument document, final File imageFile) throws IOException {
		PDImageXObject imageObject = PDImageXObject.createFromFileByContent(imageFile, document);
		PDPage page = new PDPage(new PDRectangle(imageObject.getWidth(), imageObject.getHeight()));
		try (PDPageContentStream pageContentStream = new PDPageContentStream(document, page)) {
			pageContentStream.drawImage(imageObject, 0, 0);
		}
		document.addPage(page);
	}

	/**
	 * 以图片形式渲染文档页面
	 *
	 * @param document 文档
	 * @return 页面图像列表
	 */
	public static List<BufferedImage> renderDocumentPagesAsImage(final PDDocument document) throws IOException {
		return renderDocumentPagesAsImage(document, 72);
	}

	/**
	 * 以图片形式渲染文档页面
	 *
	 * @param document 文档
	 * @param dpi      DPI大小
	 * @return 页面图像列表
	 */
	public static List<BufferedImage> renderDocumentPagesAsImage(final PDDocument document, final float dpi) throws IOException {
		List<BufferedImage> pageImages = new ArrayList<>();
		renderDocumentPagesAsImage(document, dpi, ((bufferedImage, index) -> pageImages.add(bufferedImage)));
		return pageImages;
	}

	/**
	 * 以图片形式渲染文档页面
	 *
	 * @param document 文档
	 * @param action   渲染图像处理回调
	 */
	public static void renderDocumentPagesAsImage(final PDDocument document, final ObjIntConsumer<BufferedImage> action) throws IOException {
		renderDocumentPagesAsImage(document, 72, action);
	}

	/**
	 * 以图片形式渲染文档页面
	 *
	 * @param document 文档
	 * @param action   渲染图像处理回调
	 */
	public static void renderDocumentPagesAsImage(final PDDocument document, final float dpi, final ObjIntConsumer<BufferedImage> action) throws IOException {
		PDFRenderer renderer = new PDFRenderer(document);
		for (int i = 0; i < document.getNumberOfPages(); i++) {
			BufferedImage image = renderer.renderImageWithDPI(i, dpi);
			action.accept(image, i + 1);
		}
	}

	/**
	 * 以图片形式渲染文档页面
	 *
	 * @param document   文档
	 * @param pageNumber 页码
	 * @return 渲染后图像
	 */
	public static BufferedImage renderDocumentPageAsImage(final PDDocument document, final Integer pageNumber) throws IOException {
		return renderDocumentPageAsImage(document, pageNumber, 72);
	}

	/**
	 * 以图片形式渲染文档页面
	 *
	 * @param document   文档
	 * @param pageNumber 页码
	 * @param dpi        DPI大小
	 * @return 渲染后图像
	 */
	public static BufferedImage renderDocumentPageAsImage(final PDDocument document, final Integer pageNumber, final float dpi) throws IOException {
		PDFRenderer renderer = new PDFRenderer(document);
		return renderer.renderImageWithDPI(pageNumber - 1, dpi);
	}

	/**
	 * 以传统模式合并文档
	 *
	 * @param sourceFiles        待合并文件
	 * @param targetFile         目标输出文件
	 * @param memoryUsageSetting 内存使用设置
	 */
	public static void mergeDocumentByLegacyMode(final List<File> sourceFiles, final File targetFile,
												 final MemoryUsageSetting memoryUsageSetting) throws IOException {
		List<PDDocument> sourceDocuments = new ArrayList<>();
		for (File sourceFile : sourceFiles) {
			sourceDocuments.add(getDocument(sourceFile));
		}
		PDDocument result = mergeDocumentByLegacyMode(sourceDocuments, memoryUsageSetting);
		result.save(targetFile);
		result.close();
		for (PDDocument sourceDocument : sourceDocuments) {
			sourceDocument.close();
		}
	}

	/**
	 * 以传统模式合并文档
	 *
	 * @param documents          待合并文档列表
	 * @param memoryUsageSetting 内存使用设置
	 * @return 合并后的新文档
	 */
	public static PDDocument mergeDocumentByLegacyMode(final List<PDDocument> documents,
													   final MemoryUsageSetting memoryUsageSetting) throws IOException {
		PDFMergerUtility mergerUtility = new PDFMergerUtility();
		PDDocument result = new PDDocument(memoryUsageSetting.streamCache);
		for (PDDocument document : documents) {
			mergerUtility.appendDocument(result, document);
		}
		return result;
	}

	/**
	 * 根据页码切分文档
	 *
	 * @param sourceDocument 源文档
	 * @param splitPage      切分页数
	 * @param action         切割文件处理回调
	 */
	public static void splitDocumentByPages(final PDDocument sourceDocument, final int splitPage,
											final ObjIntConsumer<PDDocument> action) throws IOException {
		int totalPages = sourceDocument.getNumberOfPages();
		for (int pageNumber = 1; pageNumber <= totalPages; pageNumber += splitPage) {
			int endPage = pageNumber + splitPage - 1;
			try (PDDocument document = copyDocumentByPages(sourceDocument, pageNumber, endPage)) {
				action.accept(document, pageNumber);
			}
		}
	}

	/**
	 * 根据页码切分文档
	 *
	 * @param sourceDocument 源文档
	 * @param splitPage      切分页数
	 * @return 切割结果
	 */
	public static List<PDDocument> splitDocumentByPages(final PDDocument sourceDocument, final int splitPage) throws IOException {
		List<PDDocument> outputFileList = new ArrayList<>();
		int totalPages = sourceDocument.getNumberOfPages();
		for (int pageNumber = 1; pageNumber <= totalPages; pageNumber += splitPage) {
			outputFileList.add(copyDocumentByPages(sourceDocument, pageNumber, pageNumber + splitPage - 1));
		}
		return outputFileList;
	}

	/**
	 * 拷贝文档
	 *
	 * @param document 待拷贝文档
	 * @return 拷贝后的新文档
	 */
	public static PDDocument copyDocument(final PDDocument document) throws IOException {
		return copyDocumentByPages(document, 1, document.getNumberOfPages());
	}

	/**
	 * 拷贝源文档的指定页面
	 *
	 * @param sourceDocument 源文档
	 * @param startPage      起始页码
	 * @param endPage        结束页码
	 * @return 拷贝后的新文档
	 */
	public static PDDocument copyDocumentByPages(final PDDocument sourceDocument,
												 final int startPage, final int endPage) throws IOException {
		// 防止页码溢出
		int maxPage = Math.min(sourceDocument.getNumberOfPages(), endPage);
		PDDocument result = createDocument(sourceDocument);
		for (int i = startPage; i <= maxPage; i++) {
			PDPage page = sourceDocument.getPage(i - 1);
			result.importPage(page);
		}
		return result;
	}

	/**
	 * 拷贝源文档的指定页面
	 *
	 * @param sourceDocument 源文档
	 * @param pageList       页码列表
	 * @return 拷贝后的新文档
	 */
	public static PDDocument copyDocumentByPages(final PDDocument sourceDocument,
												 final List<Integer> pageList) throws IOException {
		int maxPageNumber = sourceDocument.getNumberOfPages();
		// 页码去重，过滤掉溢出的页码并排序
		List<Integer> pageNumberList = pageList.stream()
			.distinct()
			.filter(pageNumber -> pageNumber >= 1 && pageNumber <= maxPageNumber)
			.sorted(Integer::compareTo)
			.toList();

		PDDocument targetDocument = createDocument(sourceDocument);
		for (Integer pageNumber : pageNumberList) {
			PDPage sourcePage = sourceDocument.getPage(pageNumber - 1);
			targetDocument.importPage(sourcePage);
		}
		return targetDocument;
	}

	public static List<PDFDirectory> getDictionaries(final PDDocument document) throws IOException {
		PDDocumentOutline outline = document.getDocumentCatalog().getDocumentOutline();
		if (outline != null) {
			List<PDFDirectory> result = new ArrayList<>();
			parseOutline(outline, result);
			return result;
		} else {
			return Collections.emptyList();
		}
	}

	protected static void parseOutline(PDOutlineNode node, List<PDFDirectory> result) throws IOException {
		PDOutlineItem item = node.getFirstChild();
		while (item != null) {
			int pageIndex = getPageIndex(item);
			PDFDirectory PDFDirectory = new PDFDirectory(item.getTitle(), pageIndex);
			result.add(PDFDirectory);
			parseOutline(item, PDFDirectory.getChildren());
			item = item.getNextSibling();
		}
	}

	private static int getPageIndex(PDOutlineItem item) throws IOException {
		if (item.getDestination() instanceof PDPageDestination destination) {
			return destination.retrievePageNumber();
		} else if (item.getAction() instanceof PDActionGoTo action) {
			if (action.getDestination() instanceof PDPageDestination destination) {
				return destination.retrievePageNumber();
			}
		}
		return -1;
	}
}