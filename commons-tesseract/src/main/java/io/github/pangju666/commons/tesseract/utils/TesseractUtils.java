/*
 *   Copyright 2026 pangju666
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

package io.github.pangju666.commons.tesseract.utils;

import io.github.pangju666.commons.tesseract.io.resource.TesseractResource;
import io.github.pangju666.commons.tesseract.lang.TesseractConstants;
import io.github.pangju666.commons.tesseract.model.TessBaseAPIOptions;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.tesseract.TessBaseAPI;

import java.util.Objects;

/**
 * Tesseract OCR 工具类
 * <p>基于 Tesseract OCR 引擎提供图片文字识别功能的工具类。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *     <li>支持从 {@link TesseractResource} 进行 OCR 识别</li>
 *     <li>支持从 Leptonica PIX 格式图像进行 OCR 识别</li>
 *     <li>支持配置页面分割模式、图像分辨率、识别区域等参数</li>
 * </ul>
 *
 * <h2>支持的图像格式</h2>
 * <p>支持的图像格式由 Tesseract 引擎决定，常见的支持格式包括：
 * BMP、JPEG、PNG、TIFF、GIF、WEBP 等。</p>
 *
 * <h2>示例代码</h2>
 * <pre>{@code
 * TessBaseAPI tessBaseAPI = TesseractConstants.getDefaultTessBaseApiPool().borrowObject();
 * try {
 *     TesseractResource resource = new TesseractResource(imageFile);
 *     TessBaseAPIOptions options = new TessBaseAPIOptions();
 *     options.setPsm(PageSegmentationMode.AUTO_NO_OSD);
 *     String text = TesseractUtils.ocrImage(tessBaseAPI, resource, options);
 * } finally {
 *     TesseractConstants.getDefaultTessBaseApiPool().returnObject(tessBaseAPI);
 * }
 * }</pre>
 *
 * @author pangju666
 * @see TessBaseAPI
 * @see TesseractResource
 * @see TessBaseAPIOptions
 * @see TesseractConstants
 * @since 2.1.0
 */
public class TesseractUtils {
	/**
	 * 受保护的构造函数，防止实例化
	 *
	 * @since 2.1.0
	 */
	protected TesseractUtils() {
	}

	/**
	 * 从 TesseractResource 进行 OCR 识别
	 * <p>将 TesseractResource 转换为 Leptonica PIX 格式，然后调用 TessBaseAPI 进行文字识别。</p>
	 *
	 * @param tessBaseAPI 已初始化的 TessBaseAPI 实例，不可为 null
	 * @param resource Tesseract 资源对象，不可为 null
	 * @param options TessBaseAPI 配置选项，不可为 null
	 * @return 识别出的文字内容（UTF-8 编码）
	 * @throws NullPointerException 当 tessBaseAPI、resource 或 options 为 null 时抛出
	 * @since 2.1.0
	 */
	public static String ocrImage(final TessBaseAPI tessBaseAPI, final TesseractResource resource,
	                              final TessBaseAPIOptions options) {
		Validate.notNull(resource, "resource 不可为 null");

		try (PIX image = resource.getPix()) {
			return ocrImage(tessBaseAPI, image, options);
		}
	}

	/**
	 * 从 PIX 图片对象进行 OCR 识别
	 * <p>这是所有 OCR 识别方法的核心实现。
	 * 将 PIX 图片对象设置到 TessBaseAPI 中，应用配置选项，然后调用 GetUTF8Text 获取识别结果。</p>
	 *
	 * <p>实现特性：</p>
	 * <ul>
	 *     <li>验证 TessBaseAPI 实例的有效性（非 null 且非 isNull）</li>
	 *     <li>验证 PIX 图片对象的有效性（非 null 且非 isNull）</li>
	 *     <li>验证配置选项的有效性（非 null）</li>
	 *     <li>自动管理 BytePointer 资源</li>
	 *     <li>当识别结果为 null 时返回空字符串</li>
	 * </ul>
	 *
	 * @param tessBaseAPI 已初始化的 TessBaseAPI 实例，不可为 null
	 * @param image Leptonica PIX 图片对象，不可为 null
	 * @param options TessBaseAPI 配置选项，不可为 null
	 * @return 识别出的文字内容（UTF-8 编码），如果识别失败返回空字符串
	 * @throws NullPointerException 当 tessBaseAPI、image 或 options 为 null 时抛出
	 * @throws IllegalArgumentException 当 tessBaseAPI 或 image 为空指针时抛出
	 * @since 2.1.0
	 */
	public static String ocrImage(final TessBaseAPI tessBaseAPI, final PIX image, final TessBaseAPIOptions options) {
		Validate.notNull(tessBaseAPI, "tessBaseAPI 不可为 null");
		Validate.isTrue(!tessBaseAPI.isNull(), "tessBaseAPI 不可为 null");
		Validate.notNull(image, "image 不可为 null");
		Validate.isTrue(!tessBaseAPI.isNull(), "image 不可为 null");
		Validate.notNull(options, "options 不可为 null");

		tessBaseAPI.SetImage(image);
		options.configure(tessBaseAPI);

		try (BytePointer result = tessBaseAPI.GetUTF8Text()) {
			if (Objects.isNull(result) || result.isNull()) {
				return StringUtils.EMPTY;
			}
			return result.getString();
		}
	}
}
