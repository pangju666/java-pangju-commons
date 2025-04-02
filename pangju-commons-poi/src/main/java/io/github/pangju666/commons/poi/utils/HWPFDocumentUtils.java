package io.github.pangju666.commons.poi.utils;

import io.github.pangju666.commons.io.lang.IOConstants;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.IOUtils;
import io.github.pangju666.commons.poi.lang.PoiConstants;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * 文档渲染工具类，仅支持docx格式文档
 *
 * @author pangju666
 * @since 1.0.0
 */
public class HWPFDocumentUtils {
	protected HWPFDocumentUtils() {
	}

	public static boolean isDoc(final File file) throws IOException {
		return FileUtils.isMimeType(file, PoiConstants.DOC_MIME_TYPE);
	}

	public static boolean isDoc(final byte[] bytes) {
		if (ArrayUtils.isEmpty(bytes)) {
			return false;
		}
		String mimeType = IOConstants.getDefaultTika().detect(bytes);
		return PoiConstants.DOC_MIME_TYPE.equals(mimeType);
	}

	public static boolean isDoc(final InputStream inputStream) throws IOException {
		if (Objects.isNull(inputStream)) {
			return false;
		}
		String mimeType = IOConstants.getDefaultTika().detect(inputStream);
		return PoiConstants.DOC_MIME_TYPE.equals(mimeType);
	}

	public static Document getDocument(final File file) throws IOException {
		FileUtils.checkFile(file, "file 不可为 null");

		String mimeType = IOConstants.getDefaultTika().detect(file);
		if (!PoiConstants.DOC_MIME_TYPE.equals(mimeType)) {
			throw new IllegalArgumentException("不是doc文件");
		}
		try (FileInputStream inputStream = FileUtils.openInputStream(file)) {
			return new XWPFDocument(inputStream);
		}
	}

	public static Document getDocument(final byte[] bytes) throws IOException {
		Validate.isTrue(ArrayUtils.isNotEmpty(bytes), "bytes 不可为空");

		String mimeType = IOConstants.getDefaultTika().detect(bytes);
		if (!PoiConstants.DOC_MIME_TYPE.equals(mimeType)) {
			throw new IllegalArgumentException("不是doc文件");
		}
		InputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
		return new XWPFDocument(inputStream);
	}
}