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
public class XWPFDocumentUtils {
	protected XWPFDocumentUtils() {
	}

	public static boolean isDocx(final File file) throws IOException {
		return FileUtils.isMimeType(file, PoiConstants.DOCX_MIME_TYPE);
	}

	public static boolean isDocx(final byte[] bytes) {
		if (ArrayUtils.isEmpty(bytes)) {
			return false;
		}
		String mimeType = IOConstants.getDefaultTika().detect(bytes);
		return PoiConstants.DOCX_MIME_TYPE.equals(mimeType);
	}

	public static boolean isDocx(final InputStream inputStream) throws IOException {
		if (Objects.isNull(inputStream)) {
			return false;
		}
		String mimeType = IOConstants.getDefaultTika().detect(inputStream);
		return PoiConstants.DOCX_MIME_TYPE.equals(mimeType);
	}

	public static Document getDocument(final File file) throws IOException {
		FileUtils.checkFile(file, "file 不可为 null");

		String mimeType = IOConstants.getDefaultTika().detect(file);
		if (!PoiConstants.DOCX_MIME_TYPE.equals(mimeType)) {
			throw new IllegalArgumentException("不是docx文件");
		}
		try (FileInputStream inputStream = FileUtils.openInputStream(file)) {
			return new XWPFDocument(inputStream);
		}
	}

	public static Document getDocument(final byte[] bytes) throws IOException {
		Validate.isTrue(ArrayUtils.isNotEmpty(bytes), "bytes 不可为空");

		String mimeType = IOConstants.getDefaultTika().detect(bytes);
		if (!PoiConstants.DOCX_MIME_TYPE.equals(mimeType)) {
			throw new IllegalArgumentException("不是docx文件");
		}
		InputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
		return new XWPFDocument(inputStream);
	}
}