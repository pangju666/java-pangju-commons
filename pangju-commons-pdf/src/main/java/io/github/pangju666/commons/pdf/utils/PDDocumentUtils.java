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

public class PDDocumentUtils {
	/**
	 * PDF文件处理的最小内存阈值（50MB）
	 *
	 * @since 1.0.0
	 */
	public static final long MIN_PDF_BYTES = 50 * 1024 * 1024;
	/**
	 * PDF文件处理的最大内存阈值（500MB），超过此大小将使用临时文件处理
	 *
	 * @since 1.0.0
	 */
	public static final long MAX_PDF_BYTES = 500 * 1024 * 1024;
	/**
	 * 混合内存模式下最大内存使用量（100MB）
	 *
	 * @since 1.0.0
	 */
	public static final long MIXED_MAX_MAIN_MEMORY_BYTES = 100 * 1024 * 1024;

	public static final MemoryUsageSetting MAIN_MEMORY_ONLY_MEMORY_USAGE_SETTING = MemoryUsageSetting.setupMainMemoryOnly();
	public static final MemoryUsageSetting TEMP_FILE_ONLY_MEMORY_USAGE_SETTING = MemoryUsageSetting.setupTempFileOnly();
	public static final MemoryUsageSetting MIXED_PAGE_MEMORY_USAGE_SETTING = MemoryUsageSetting.setupMixed(MIXED_MAX_MAIN_MEMORY_BYTES);

	protected PDDocumentUtils() {
	}

	public static MemoryUsageSetting computeMemoryUsageSetting(final long fileSize) {
		if (fileSize < MIN_PDF_BYTES) {
			return MAIN_MEMORY_ONLY_MEMORY_USAGE_SETTING;
		} else if (fileSize > MAX_PDF_BYTES) {
			return TEMP_FILE_ONLY_MEMORY_USAGE_SETTING;
		} else {
			return MIXED_PAGE_MEMORY_USAGE_SETTING;
		}
	}

	public static boolean isPDF(final File file) throws IOException {
		return FileUtils.exist(file, true) &&
			IOConstants.getDefaultTika().detect(file).equals(PdfConstants.PDF_MIME_TYPE);
	}

	public static boolean isPDF(final byte[] bytes) {
		return ArrayUtils.isNotEmpty(bytes) &&
			PdfConstants.PDF_MIME_TYPE.equals(IOConstants.getDefaultTika().detect(bytes));
	}

	public static boolean isPDF(final InputStream inputStream) throws IOException {
		return Objects.nonNull(inputStream) &&
			PdfConstants.PDF_MIME_TYPE.equals(IOConstants.getDefaultTika().detect(inputStream));
	}

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

	public static PDDocument getDocument(final File file) throws IOException {
		Validate.notNull(file, "file 不可为 null");
		checkFile(file);

		if (!PdfConstants.PDF_MIME_TYPE.equals(IOConstants.getDefaultTika().detect(file))) {
			return null;
		}
		return Loader.loadPDF(file, computeMemoryUsageSetting(file.length()).streamCache);
	}

	public static PDDocument getDocument(final File file, final String password) throws IOException {
		Validate.notNull(file, "file 不可为 null");
		checkFile(file);

		if (!PdfConstants.PDF_MIME_TYPE.equals(IOConstants.getDefaultTika().detect(file))) {
			return null;
		}
		return Loader.loadPDF(file, password, computeMemoryUsageSetting(file.length()).streamCache);
	}

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

	public static void getImages(final PDDocument document, final ObjIntConsumer<BufferedImage> action) throws IOException {
		getImages(document, 1, action);
	}

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

	public static List<BufferedImage> getImages(final PDDocument document) throws IOException {
		return getImages(document, 1);
	}

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

	public static BufferedImage getImage(final PDDocument document, final int page) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		PDFRenderer renderer = new PDFRenderer(document);
		return renderer.renderImage(page - 1);
	}

	public static BufferedImage getImage(final PDDocument document, final int page, final int scale) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		PDFRenderer renderer = new PDFRenderer(document);
		return renderer.renderImage(page - 1, scale);
	}

	public static BufferedImage getImage(final PDDocument document, final int page, final float dpi) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		PDFRenderer renderer = new PDFRenderer(document);
		return renderer.renderImageWithDPI(page - 1, dpi);
	}

	public static void merge(final Collection<PDDocument> documents, final OutputStream outputStream, final MemoryUsageSetting memoryUsageSetting) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (PDDocument outputDocument = merge(documents, memoryUsageSetting)) {
			outputDocument.save(outputStream);
		}
	}

	public static void merge(final Collection<PDDocument> documents, final File outputFile) throws IOException {
		Validate.notNull(outputFile, "outputFile 不可为 null");
		if (outputFile.exists() && !outputFile.isFile()) {
			throw new IOException(outputFile.getAbsolutePath() + " 不是一个文件路径");
		}

		try (PDDocument outputDocument = merge(documents, computeMemoryUsageSetting(outputFile.length()))) {
			outputDocument.save(outputFile);
		}
	}

	public static void merge(final Collection<PDDocument> documents, final File outputFile, final MemoryUsageSetting memoryUsageSetting) throws IOException {
		Validate.notNull(outputFile, "outputFile 不可为 null");
		if (outputFile.exists() && !outputFile.isFile()) {
			throw new IOException(outputFile.getAbsolutePath() + " 不是一个文件路径");
		}

		try (PDDocument outputDocument = merge(documents, memoryUsageSetting)) {
			outputDocument.save(outputFile);
		}
	}

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

	public static void split(final PDDocument document, final ObjIntConsumer<PDDocument> action) throws IOException {
		split(document, 1, action);
	}

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

	public static List<PDDocument> split(final PDDocument document) throws IOException {
		return split(document, 1);
	}

	public static List<PDDocument> split(final PDDocument document, final int splitPage) throws IOException {
		Validate.notNull(document, "document 不可为 null");

		List<PDDocument> outputFileList = new ArrayList<>(document.getNumberOfPages() / splitPage);
		int totalPages = document.getNumberOfPages();
		for (int pageNumber = 1; pageNumber <= totalPages; pageNumber += splitPage) {
			outputFileList.add(copy(document, pageNumber, pageNumber + splitPage - 1));
		}
		return outputFileList;
	}

	public static PDDocument copy(final PDDocument document) throws IOException {
		return copy(document, 1, document.getNumberOfPages());
	}

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

	public static PDDocument copy(final PDDocument document, final int... pages) throws IOException {
		return copy(document, Arrays.stream(pages).boxed().toList());
	}

	public static PDDocument copy(final PDDocument document, final Integer... pages) throws IOException {
		return copy(document, Arrays.asList(pages));
	}

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

	protected static void parseOutline(final PDOutlineNode node, final List<PDFDirectory> result) throws IOException {
		PDOutlineItem item = node.getFirstChild();
		while (item != null) {
			int pageIndex = getPageIndex(item);
			PDFDirectory PDFDirectory = new PDFDirectory(item.getTitle(), pageIndex);
			result.add(PDFDirectory);
			parseOutline(item, PDFDirectory.getChildren());
			item = item.getNextSibling();
		}
	}

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

	protected static void checkFile(final File file) throws IOException {
		if (!file.exists()) {
			throw new FileNotFoundException(file.getAbsolutePath());
		}
		if (!file.isFile()) {
			throw new IOException(file.getAbsolutePath() + " 不是一个文件路径");
		}
	}
}