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

package io.github.pangju666.commons.pdf.lang;

import java.util.Set;

/**
 * PDF相关常量
 *
 * @author pangju666
 * @since 1.0.0
 */
public class PdfConstants {
	/**
	 * PDF MIME类型
	 *
	 * @since 1.0.0
	 */
	public static final String PDF_MIME_TYPE = "application/pdf";

	/**
	 * PDF支持的图片MIME类型集合
	 * <p>
	 * 包含PDF可以嵌入的图片格式对应的MIME类型：
	 * JPEG、TIFF、GIF、BMP、PNG
	 * </p>
	 *
	 * @since 1.1.0
	 */
	public static final Set<String> PDF_IMAGE_SUPPORTED_IMAGE_TYPES = Set.of(
		"image/jpeg", "image/tiff", "image/gif", "image/bmp", "image/png");

	/**
	 * PDF支持的图片文件扩展名集合
	 * <p>
	 * 包含PDF可以嵌入的图片格式对应的文件扩展名：
	 * jpg、jpeg、tif、tiff、gif、bmp、png
	 * </p>
	 *
	 * @since 1.1.0
	 */
	public static final Set<String> PDF_IMAGE_SUPPORTED_IMAGE_FORMATS = Set.of(
		"jpg", "jpeg", "tif", "tiff", "gif", "bmp", "png");
}
