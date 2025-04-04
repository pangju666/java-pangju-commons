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
import org.apache.commons.io.input.UnsynchronizedBufferedInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * DOC文档工具类
 * <p>
 * 提供对Microsoft Word 97-2003格式(.doc)文档的操作支持，包括：
 * <ul>
 *   <li>文档格式验证</li>
 *   <li>文档内容读取</li>
 * </ul>
 * 注意事项：
 * <ul>
 *   <li>仅支持.doc格式文档</li>
 *   <li>所有方法均为静态方法</li>
 *   <li>线程安全</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class HWPFDocumentUtils {
	protected HWPFDocumentUtils() {
	}

	/**
	 * 检查文件是否为DOC格式
	 *
	 * @param file 待检查的文件，不允许为null
	 * @return true-是DOC格式，false-不是DOC格式或文件不存在
	 * @throws IOException 当文件读取失败时抛出
	 * @since 1.0.0
	 */
	public static boolean isDoc(final File file) throws IOException {
		return FileUtils.isMimeType(file, PoiConstants.DOC_MIME_TYPE);
	}

	/**
	 * 检查字节数组是否为DOC格式
	 *
	 * @param bytes 待检查的字节数组
	 * @return true-是DOC格式，false-不是DOC格式或字节数组为空
	 * @since 1.0.0
	 */
	public static boolean isDoc(final byte[] bytes) {
		if (ArrayUtils.isEmpty(bytes)) {
			return false;
		}
		String mimeType = IOConstants.getDefaultTika().detect(bytes);
		return PoiConstants.DOC_MIME_TYPE.equals(mimeType);
	}

	/**
	 * 检查输入流是否为DOC格式
	 *
	 * @param inputStream 待检查的输入流
	 * @return true-是DOC格式，false-不是DOC格式或输入流为null
	 * @throws IOException 当流读取失败时抛出
	 * @since 1.0.0
	 */
	public static boolean isDoc(final InputStream inputStream) throws IOException {
		if (Objects.isNull(inputStream)) {
			return false;
		}
		String mimeType = IOConstants.getDefaultTika().detect(inputStream);
		return PoiConstants.DOC_MIME_TYPE.equals(mimeType);
	}

	/**
	 * 从文件加载DOC文档
	 *
	 * @param file DOC文件，不允许为null
	 * @return 加载的文档对象
	 * @throws IOException 当文件读取失败时抛出
	 * @throws IllegalArgumentException 当文件不是DOC格式时抛出
	 * @since 1.0.0
	 */
	public static Document getDocument(final File file) throws IOException {
		FileUtils.checkFile(file, "file 不可为 null");

		String mimeType = IOConstants.getDefaultTika().detect(file);
		if (!PoiConstants.DOC_MIME_TYPE.equals(mimeType)) {
			throw new IllegalArgumentException("不是doc文件");
		}
		try (UnsynchronizedBufferedInputStream inputStream = FileUtils.openUnsynchronizedBufferedInputStream(file)) {
			return new XWPFDocument(inputStream);
		}
	}

	/**
	 * 从字节数组加载DOC文档
	 *
	 * @param bytes DOC文档字节数组，不允许为空
	 * @return 加载的文档对象
	 * @throws IOException 当字节数组解析失败时抛出
	 * @throws IllegalArgumentException 当字节数组不是DOC格式时抛出
	 * @since 1.0.0
	 */
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