package io.github.pangju666.commons.pdf.utils;

import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.FilenameUtils;
import org.apache.commons.lang3.Validate;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.ObjIntConsumer;

/**
 * PDF工具类
 *
 * @author 胖橘
 * @version 1.0
 * @since 1.0
 */
public class PDDocumentUtils {
	public static final String PDF_MIME_TYPE = "application/pdf";
	private static final long MIN_PDF_BYTES = 50 * 1024 * 1024;
	private static final long MAX_PDF_BYTES = 500 * 1024 * 1024;
	private static final long MIXED_MAX_MAIN_MEMORY_BYTES = 100 * 1024 * 1024;

	protected PDDocumentUtils() {
	}

	public static MemoryUsageSetting computeMemoryUsageSetting(long size) {
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
	public static boolean isPdfFile(File file) {
		Validate.isTrue(FileUtils.exist(file), "文件不存在");
		return isPdfFile(file.getName());
	}

	/**
	 * 判断是否为PDF文件
	 *
	 * @param filePath 文件路径
	 * @return 是否为PDF文件
	 */
	public static boolean isPdfFile(String filePath) {
		return PDF_MIME_TYPE.equals(FilenameUtils.getMimeType(filePath));
	}

	/**
	 * 创建新文档，并拷贝源文档的属性至新文档
	 *
	 * @param sourceDocument 源文档
	 * @return 创建的新文档
	 */
	public static PDDocument createDocument(PDDocument sourceDocument) {
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
	public static PDDocument getDocument(InputStream inputStream) throws IOException {
		return Loader.loadPDF(inputStream.readAllBytes());
	}

	/**
	 * 获取文档对象
	 *
	 * @param documentPath 文档路径
	 */
	public static PDDocument getDocument(String documentPath) throws IOException {
		if (!isPdfFile(documentPath)) {
			return null;
		}
		File file = new File(documentPath);
		if (FileUtils.notExist(file)) {
			return null;
		}
		return Loader.loadPDF(file);
	}

	/**
	 * 获取文档对象
	 *
	 * @param documentFile 文档文件
	 */
	public static PDDocument getDocument(File documentFile) throws IOException {
		if (FileUtils.notExist(documentFile)) {
			return null;
		}
		if (!isPdfFile(documentFile.getName())) {
			return null;
		}
		return Loader.loadPDF(documentFile, computeMemoryUsageSetting(documentFile.length()).streamCache);
	}

	/**
	 * 获取总页数
	 *
	 * @param documentInputStream 文档输入流
	 * @return 总页数
	 */
	public static int getPageCount(InputStream documentInputStream) throws IOException {
		try (PDDocument document = getDocument(documentInputStream)) {
			return document.getNumberOfPages();
		}
	}

	/**
	 * 获取总页数
	 *
	 * @param documentFile 文档
	 * @return 总页数
	 */
	public static int getPageCount(File documentFile) throws IOException {
		try (PDDocument document = getDocument(documentFile)) {
			if (Objects.isNull(document)) {
				return 0;
			}
			return document.getNumberOfPages();
		}
	}

	public static void convertImageToPdf(PDDocument document, List<File> imageFiles) throws IOException {
		for (File imageFile : imageFiles) {
			convertImageToPdf(document, imageFile);
		}
	}

	public static void convertImageToPdf(PDDocument document, File imageFile) throws IOException {
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
	public static List<BufferedImage> renderDocumentPagesAsImage(PDDocument document) throws IOException {
		return renderDocumentPagesAsImage(document, 72);
	}

	/**
	 * 以图片形式渲染文档页面
	 *
	 * @param document 文档
	 * @param dpi      DPI大小
	 * @return 页面图像列表
	 */
	public static List<BufferedImage> renderDocumentPagesAsImage(PDDocument document, float dpi) throws IOException {
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
	public static void renderDocumentPagesAsImage(PDDocument document, ObjIntConsumer<BufferedImage> action) throws IOException {
		renderDocumentPagesAsImage(document, 72, action);
	}

	/**
	 * 以图片形式渲染文档页面
	 *
	 * @param document 文档
	 * @param action   渲染图像处理回调
	 */
	public static void renderDocumentPagesAsImage(PDDocument document, float dpi, ObjIntConsumer<BufferedImage> action) throws IOException {
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
	public static BufferedImage renderDocumentPageAsImage(PDDocument document, Integer pageNumber) throws IOException {
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
	public static BufferedImage renderDocumentPageAsImage(PDDocument document, Integer pageNumber, float dpi) throws IOException {
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
	public static void mergeDocumentByLegacyMode(List<File> sourceFiles, File targetFile,
												 MemoryUsageSetting memoryUsageSetting) throws IOException {
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
	public static PDDocument mergeDocumentByLegacyMode(List<PDDocument> documents,
													   MemoryUsageSetting memoryUsageSetting) throws IOException {
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
	public static void splitDocumentByPages(PDDocument sourceDocument, int splitPage,
											ObjIntConsumer<PDDocument> action) throws IOException {
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
	public static List<PDDocument> splitDocumentByPages(PDDocument sourceDocument, int splitPage) throws IOException {
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
	public static PDDocument copyDocument(PDDocument document) throws IOException {
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
	public static PDDocument copyDocumentByPages(PDDocument sourceDocument,
												 int startPage, int endPage) throws IOException {
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
	public static PDDocument copyDocumentByPages(PDDocument sourceDocument,
												 List<Integer> pageList) throws IOException {
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
}