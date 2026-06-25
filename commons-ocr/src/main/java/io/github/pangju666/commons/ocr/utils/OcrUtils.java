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

package io.github.pangju666.commons.ocr.utils;

import io.github.pangju666.commons.io.lang.IOConstants;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.IOUtils;
import io.github.pangju666.commons.ocr.lang.OcrConstants;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.commons.lang3.Validate;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.leptonica.global.leptonica;
import org.bytedeco.tesseract.TessBaseAPI;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * OCR 工具类
 * <p>
 * 基于 Tesseract OCR 引擎提供图片文字识别功能的工具类。
 * 支持从多种来源（输入流、字节数组、文件、RenderedImage）读取图片并进行文字识别。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *   <li>从 InputStream 读取图片并进行 OCR 识别</li>
 *   <li>从字节数组读取图片并进行 OCR 识别</li>
 *   <li>从 File 读取图片并进行 OCR 识别</li>
 *   <li>从 RenderedImage 读取图片并进行 OCR 识别</li>
 * </ul>
 * </p>
 * <p>
 * 该工具类提供两种使用方式：
 * <ul>
 *   <li>便捷方式：直接调用不需要 tessBaseAPI 参数的方法，会自动从对象池获取和归还对象</li>
 *   <li>手动管理：调用需要 tessBaseAPI 参数的方法，自行管理对象生命周期</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @since 1.1.0
 */
public class OcrUtils {
	/**
	 * 受保护的构造函数，防止实例化
	 *
	 * @since 1.1.0
	 */
	protected OcrUtils() {
	}

	/**
	 * 从字节数组读取图片并进行 OCR 识别
	 * <p>
	 * 自动从对象池获取 TessBaseAPI 实例，识别完成后自动归还到对象池。
	 * 首先检测图片的 MIME 类型，确认是受支持的图片格式后，
	 * 使用 Leptonica 库将字节数组转换为 PIX 图片对象，
	 * 然后调用 TessBaseAPI 进行文字识别。
	 * </p>
	 *
	 * @param imageData 图片字节数组，不可为 null 或空
	 * @return 识别出的文字内容（UTF-8 编码）
	 * @throws Exception              当解析图片、识别失败或对象池操作失败时抛出
	 * @throws IllegalArgumentException 当参数为 null、为空或图片类型不支持时抛出
	 * @since 1.1.0
	 */
	public static String ocrImage(byte[] imageData) throws Exception {
		TessBaseAPI tessBaseAPI = OcrConstants.getDefaultTessBaseApiPool().borrowObject();
		try {
			return ocrImage(tessBaseAPI, imageData);
		} finally {
			OcrConstants.getDefaultTessBaseApiPool().returnObject(tessBaseAPI);
		}
	}

	/**
	 * 从文件读取图片并进行 OCR 识别
	 * <p>
	 * 自动从对象池获取 TessBaseAPI 实例，识别完成后自动归还到对象池。
	 * 首先检测图片文件的 MIME 类型，确认是受支持的图片格式后，
	 * 使用 Leptonica 库将文件读取为 PIX 图片对象，
	 * 然后调用 TessBaseAPI 进行文字识别。
	 * </p>
	 *
	 * @param imageFile 图片文件，不可为 null，必须存在且可读
	 * @return 识别出的文字内容（UTF-8 编码）
	 * @throws Exception              当读取文件、解析图片、识别失败或对象池操作失败时抛出
	 * @throws IllegalArgumentException 当参数为 null 或图片类型不支持时抛出
	 * @since 1.1.0
	 */
	public static String ocrImage(File imageFile) throws Exception {
		TessBaseAPI tessBaseAPI = OcrConstants.getDefaultTessBaseApiPool().borrowObject();
		try {
			return ocrImage(tessBaseAPI, imageFile);
		} finally {
			OcrConstants.getDefaultTessBaseApiPool().returnObject(tessBaseAPI);
		}
	}

	/**
	 * 从 RenderedImage 读取图片并进行 OCR 识别
	 * <p>
	 * 自动从对象池获取 TessBaseAPI 实例，识别完成后自动归还到对象池。
	 * 将 RenderedImage 转换为 PNG 格式的字节数组，
	 * 然后调用字节数组版本的 OCR 方法进行识别。
	 * 使用 PNG 格式可以保持图片质量的同时控制文件大小。
	 * </p>
	 *
	 * @param image 图片 RenderedImage 对象，不可为 null
	 * @return 识别出的文字内容（UTF-8 编码）
	 * @throws Exception              当转换 RenderedImage、识别失败或对象池操作失败时抛出
	 * @throws IllegalArgumentException 当参数为 null 时抛出
	 * @since 1.1.0
	 */
	public static String ocrImage(RenderedImage image) throws Exception {
		TessBaseAPI tessBaseAPI = OcrConstants.getDefaultTessBaseApiPool().borrowObject();
		try {
			return ocrImage(tessBaseAPI, image);
		} finally {
			OcrConstants.getDefaultTessBaseApiPool().returnObject(tessBaseAPI);
		}
	}

	/**
	 * 从输入流读取图片并进行 OCR 识别
	 * <p>
	 * 自动从对象池获取 TessBaseAPI 实例，识别完成后自动归还到对象池。
	 * 将输入流中的所有字节读取到内存中，然后调用字节数组版本的 OCR 方法进行识别。
	 * 该方法会关闭输入流。
	 * </p>
	 *
	 * @param inputStream 图片输入流，不可为 null
	 * @return 识别出的文字内容（UTF-8 编码）
	 * @throws Exception              当读取输入流、解析图片、识别失败或对象池操作失败时抛出
	 * @throws IllegalArgumentException 当参数为 null 或不合法时抛出
	 * @since 1.1.0
	 */
	public static String ocrImage(InputStream inputStream) throws Exception {
		TessBaseAPI tessBaseAPI = OcrConstants.getDefaultTessBaseApiPool().borrowObject();
		try {
			return ocrImage(tessBaseAPI, inputStream);
		} finally {
			OcrConstants.getDefaultTessBaseApiPool().returnObject(tessBaseAPI);
		}
	}

	/**
	 * 从 RenderedImage 读取图片并进行 OCR 识别
	 * <p>
	 * 将 RenderedImage 转换为 PNG 格式的字节数组，
	 * 然后调用字节数组版本的 OCR 方法进行识别。
	 * 使用 PNG 格式可以保持图片质量的同时控制文件大小。
	 * </p>
	 *
	 * @param tessBaseAPI 已初始化的 TessBaseAPI 实例，不可为 null
	 * @param image       图片 RenderedImage 对象，不可为 null
	 * @return 识别出的文字内容（UTF-8 编码）
	 * @throws IOException              当转换 RenderedImage 或识别失败时抛出
	 * @throws IllegalArgumentException 当参数为 null 时抛出
	 * @since 1.1.0
	 */
	public static String ocrImage(TessBaseAPI tessBaseAPI, RenderedImage image) throws IOException {
		Validate.notNull(image, "image 不可为 null");
		Validate.notNull(tessBaseAPI, "tessBaseAPI 不可为 null");

		UnsynchronizedByteArrayOutputStream outputStream = IOUtils.toUnsynchronizedByteArrayOutputStream(IOUtils.DEFAULT_BUFFER_SIZE);
		try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)) {
			ImageIO.write(image, "png", bufferedOutputStream);
		}

		return ocrImage(tessBaseAPI, outputStream.toByteArray());
	}

	/**
	 * 从输入流读取图片并进行 OCR 识别
	 * <p>
	 * 将输入流中的所有字节读取到内存中，然后调用字节数组版本的 OCR 方法进行识别。
	 * 该方法会关闭输入流。
	 * </p>
	 *
	 * @param tessBaseAPI 已初始化的 TessBaseAPI 实例，不可为 null
	 * @param inputStream 图片输入流，不可为 null
	 * @return 识别出的文字内容（UTF-8 编码）
	 * @throws IOException              当读取输入流或解析图片失败时抛出
	 * @throws IllegalArgumentException 当参数为 null 或不合法时抛出
	 * @since 1.1.0
	 */
	public static String ocrImage(TessBaseAPI tessBaseAPI, InputStream inputStream) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		return ocrImage(tessBaseAPI, inputStream.readAllBytes());
	}

	/**
	 * 从字节数组读取图片并进行 OCR 识别
	 * <p>
	 * 首先检测图片的 MIME 类型，确认是受支持的图片格式后，
	 * 使用 Leptonica 库将字节数组转换为 PIX 图片对象，
	 * 然后调用 TessBaseAPI 进行文字识别。
	 * </p>
	 *
	 * @param tessBaseAPI 已初始化的 TessBaseAPI 实例，不可为 null
	 * @param imageData   图片字节数组，不可为 null 或空
	 * @return 识别出的文字内容（UTF-8 编码）
	 * @throws IOException              当解析图片或识别失败时抛出
	 * @throws IllegalArgumentException 当参数为 null、为空或图片类型不支持时抛出
	 * @since 1.1.0
	 */
	public static String ocrImage(TessBaseAPI tessBaseAPI, byte[] imageData) throws IOException {
		Validate.notNull(imageData, "imageData 不可为 null");
		Validate.isTrue(imageData.length > 0, "imageData 不可为空");
		Validate.notNull(tessBaseAPI, "tessBaseAPI 不可为 null");
		String mimeType = IOConstants.getDefaultTika().detect(imageData);
		Validate.isTrue(OcrConstants.SUPPORTED_IMAGE_TYPES.contains(mimeType), "不是受支持的图像类型");

		try (PIX image = leptonica.pixReadMem(new BytePointer(imageData), imageData.length)) {
			if (Objects.isNull(image) || image.isNull()) {
				throw new IOException("图片读取失败");
			}
			tessBaseAPI.SetImage(image);
			return tessBaseAPI.GetUTF8Text().getString();
		}
	}

	/**
	 * 从文件读取图片并进行 OCR 识别
	 * <p>
	 * 首先检测图片文件的 MIME 类型，确认是受支持的图片格式后，
	 * 使用 Leptonica 库将文件读取为 PIX 图片对象，
	 * 然后调用 TessBaseAPI 进行文字识别。
	 * </p>
	 *
	 * @param tessBaseAPI 已初始化的 TessBaseAPI 实例，不可为 null
	 * @param imageFile   图片文件，不可为 null，必须存在且可读
	 * @return 识别出的文字内容（UTF-8 编码）
	 * @throws IOException              当读取文件或解析图片失败时抛出
	 * @throws IllegalArgumentException 当参数为 null 或图片类型不支持时抛出
	 * @since 1.1.0
	 */
	public static String ocrImage(TessBaseAPI tessBaseAPI, File imageFile) throws IOException {
		Validate.notNull(tessBaseAPI, "tessBaseAPI 不可为 null");
		String mimeType = FileUtils.getMimeType(imageFile);
		Validate.isTrue(OcrConstants.SUPPORTED_IMAGE_TYPES.contains(mimeType), "不是受支持的图像类型");

		try (PIX image = leptonica.pixRead(imageFile.getAbsolutePath())) {
			if (Objects.isNull(image) || image.isNull()) {
				throw new IOException("图片读取失败");
			}
			tessBaseAPI.SetInputImage(image);

			return tessBaseAPI.GetUTF8Text().getString();
		}
	}
}
