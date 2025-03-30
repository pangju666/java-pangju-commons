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

package io.github.pangju666.commons.image.utils;

import com.twelvemonkeys.image.BrightnessContrastFilter;
import com.twelvemonkeys.image.GrayFilter;
import com.twelvemonkeys.image.ImageUtil;
import io.github.pangju666.commons.image.lang.ImageConstants;
import io.github.pangju666.commons.io.utils.FilenameUtils;
import org.apache.commons.lang3.Validate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 图像滤镜工具类
 * <p>提供灰度化、亮度/对比度调整等图像滤波处理功能，支持多种输出格式和输出方式</p>
 *
 * @since 1.0.0
 */
public class ImageFilterUtils {
	/**
	 * 默认亮度对比度滤镜
	 *
	 * @since 1.0.0
	 */
	public static final BrightnessContrastFilter DEFAULT_BRIGHTNESS_CONTRAST_FILTER = new BrightnessContrastFilter();
	/**
	 * 灰度化滤镜
	 *
	 * @since 1.0.0
	 */
	protected static final GrayFilter GRAY_FILTER = new GrayFilter();

	protected ImageFilterUtils() {
	}

	/**
	 * 将图像灰度化并输出到流
	 *
	 * @param inputImage 原始图像（不可为null）
	 * @param outputStream 输出流（不可为null）
	 * @param outputFormat 输出图片格式（非空，如PNG/JPG）
	 * @throws IOException 当写入失败时抛出
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @since 1.0.0
	 * @see ImageUtil#grayscale(Image)
	 */
	public static void grayscale(final BufferedImage inputImage, final OutputStream outputStream,
								 final String outputFormat) throws IOException {
		filter(inputImage, GRAY_FILTER, outputStream, outputFormat);
	}

	/**
	 * 将图像灰度化并输出到文件（自动获取文件扩展名作为格式）
	 *
	 * @param inputImage 原始图像（不可为null）
	 * @param outputFile 输出文件（不可为null）
	 * @throws IOException 当outputFile是目录或I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @since 1.0.0
	 * @see ImageUtil#grayscale(Image)
	 */
	public static void grayscale(final BufferedImage inputImage, final File outputFile) throws IOException {
		filter(inputImage, GRAY_FILTER, outputFile);
	}

	/**
	 * 将图像灰度化并保存为指定格式
	 *
	 * @param inputImage 原始图像（不可为null）
	 * @param outputFile 输出文件（不可为null）
	 * @param outputFormat 输出图片格式（非空，如PNG/JPG）
	 * @throws IOException 当outputFile是目录或I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @since 1.0.0
	 * @see ImageUtil#grayscale(Image)
	 */
	public static void grayscale(final BufferedImage inputImage, final File outputFile,
								 final String outputFormat) throws IOException {
		filter(inputImage, GRAY_FILTER, outputFile, outputFormat);
	}

	/**
	 * 对图像进行灰度化处理
	 *
	 * @param image 原始图像（不可为null）
	 * @return 灰度图像（原图不受影响）
	 * @since 1.0.0
	 * @see ImageUtil#grayscale(Image)
	 */
	public static BufferedImage grayscale(final BufferedImage image) {
		return filter(image, GRAY_FILTER);
	}

	/**
	 * 对图像进行灰度化处理
	 *
	 * @param image 原始图像（不可为null）
	 * @param imageType 目标图像类型（如{@link BufferedImage#TYPE_INT_ARGB}）
	 * @return 灰度图像（原图不受影响）
	 * @since 1.0.0
	 * @see ImageUtil#grayscale(Image)
	 */
	public static BufferedImage grayscale(final BufferedImage image, int imageType) {
		return filter(image, GRAY_FILTER, imageType);
	}

	/**
	 * 增加图像对比度并输出到流
	 *
	 * @param inputImage 原始图像（不可为null）
	 * @param amount 对比度的大小，范围 [-1.0.1.0]
	 * @param outputStream 输出流（不可为null）
	 * @param outputFormat 输出图片格式（非空，如PNG/JPG）
	 * @throws IOException 当写入失败时抛出
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @since 1.0.0
	 * @see ImageUtil#contrast(Image, float)
	 */
	public static void contrast(final BufferedImage inputImage, final float amount,
								final OutputStream outputStream, final String outputFormat) throws IOException {
		if (amount != 0f) {
			Validate.inclusiveBetween(-1.0, 1.0, amount);
			BrightnessContrastFilter filter = new BrightnessContrastFilter(0f, amount);
			filter(inputImage, filter, outputStream, outputFormat);
		}
	}

	/**
	 * 增加图像对比度并输出到文件（自动获取文件扩展名作为格式）
	 *
	 * @param inputImage 原始图像（不可为null）
	 * @param amount 对比度的大小，范围 [-1.0.1.0]
	 * @param outputFile 输出文件（不可为null）
	 * @throws IOException 当outputFile是目录或I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @since 1.0.0
	 * @see ImageUtil#contrast(Image, float)
	 */
	public static void contrast(final BufferedImage inputImage, final float amount, final File outputFile) throws IOException {
		if (amount != 0f) {
			Validate.inclusiveBetween(-1.0, 1.0, amount);
			BrightnessContrastFilter filter = new BrightnessContrastFilter(0f, amount);
			filter(inputImage, filter, outputFile);
		}
	}

	/**
	 * 增加图像对比度并输出到文件
	 *
	 * @param inputImage 原始图像（不可为null）
	 * @param amount 对比度的大小，范围 [-1.0.1.0]
	 * @param outputFile 输出文件（不可为null）
	 * @param outputFormat 输出图片格式（非空，如PNG/JPG）
	 * @throws IOException 当outputFile是目录或I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @since 1.0.0
	 * @see ImageUtil#contrast(Image, float)
	 */
	public static void contrast(final BufferedImage inputImage, final float amount, final File outputFile,
								final String outputFormat) throws IOException {
		if (amount != 0f) {
			Validate.inclusiveBetween(-1.0, 1.0, amount);
			BrightnessContrastFilter filter = new BrightnessContrastFilter(0f, amount);
			filter(inputImage, filter, outputFile, outputFormat);
		}
	}

	/**
	 * 增加图像对比度
	 *
	 * @param image 原始图像（不可为null）
	 * @param amount 对比度的大小，范围 [-1.0.1.0]
	 * @return 处理后的新图像（原图不受影响）
	 * @throws IllegalArgumentException 当任一参数为null时抛出
	 * @since 1.0.0
	 * @see ImageUtil#contrast(Image, float)
	 */
	public static BufferedImage contrast(final BufferedImage image, final float amount) {
		if (amount == 0f) {
			return image;
		}
		Validate.inclusiveBetween(-1.0, 1.0, amount);
		RGBImageFilter filter = new BrightnessContrastFilter(0f, amount);
		return filter(image, filter);
	}

	/**
	 * 增加图像对比度
	 *
	 * @param image 原始图像（不可为null）
	 * @param amount 对比度的大小，范围 [-1.0.1.0]
	 * @param imageType 目标图像类型（如{@link BufferedImage#TYPE_INT_ARGB}）
	 * @return 处理后的新图像（原图不受影响）
	 * @throws IllegalArgumentException 当任一参数为null时抛出
	 * @since 1.0.0
	 * @see ImageUtil#contrast(Image, float)
	 */
	public static BufferedImage contrast(final BufferedImage image, final float amount, int imageType) {
		if (amount == 0f) {
			return image;
		}
		Validate.inclusiveBetween(-1.0, 1.0, amount);
		BrightnessContrastFilter filter = new BrightnessContrastFilter(0f, amount);
		return filter(image, filter, imageType);
	}

	/**
	 * 增加图像亮度并输出到流
	 *
	 * @param inputImage 原始图像（不可为null）
	 * @param amount 亮度值，范围 [-2.0.2.0]
	 * @param outputStream 输出流（不可为null）
	 * @param outputFormat 输出图片格式（非空，如PNG/JPG）
	 * @throws IOException 当写入失败时抛出
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @since 1.0.0
	 * @see ImageUtil#brightness(Image, float)
	 */
	public static void brightness(final BufferedImage inputImage, final float amount,
								  final OutputStream outputStream, final String outputFormat) throws IOException {
		if (amount != 0f) {
			Validate.inclusiveBetween(-2.0, 2.0, amount);
			BrightnessContrastFilter filter = new BrightnessContrastFilter(amount, 0f);
			filter(inputImage, filter, outputStream, outputFormat);
		}
	}

	/**
	 * 增加图像亮度并输出到文件（自动获取文件扩展名作为格式）
	 *
	 * @param inputImage 原始图像（不可为null）
	 * @param amount 亮度值，范围 [-2.0.2.0]
	 * @param outputFile 输出文件（不可为null）
	 * @throws IOException 当outputFile是目录或I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @since 1.0.0
	 * @see ImageUtil#brightness(Image, float)
	 */
	public static void brightness(final BufferedImage inputImage, final float amount, final File outputFile) throws IOException {
		if (amount != 0f) {
			Validate.inclusiveBetween(-2.0, 2.0, amount);
			BrightnessContrastFilter filter = new BrightnessContrastFilter(amount, 0f);
			filter(inputImage, filter, outputFile);
		}
	}

	/**
	 * 增加图像亮度并输出到文件
	 *
	 * @param inputImage 原始图像（不可为null）
	 * @param amount 亮度值，范围 [-2.0.2.0]
	 * @param outputFile 输出文件（不可为null）
	 * @param outputFormat 输出图片格式（非空，如PNG/JPG）
	 * @throws IOException 当outputFile是目录或I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @since 1.0.0
	 * @see ImageUtil#brightness(Image, float)
	 */
	public static void brightness(final BufferedImage inputImage, final float amount, final File outputFile,
								  final String outputFormat) throws IOException {
		if (amount != 0f) {
			Validate.inclusiveBetween(-2.0, 2.0, amount);
			BrightnessContrastFilter filter = new BrightnessContrastFilter(amount, 0f);
			filter(inputImage, filter, outputFile, outputFormat);
		}
	}

	/**
	 * 增加图像亮度
	 *
	 * @param image 原始图像（不可为null）
	 * @param amount 亮度值，范围 [-2.0.2.0]
	 * @return 处理后的新图像（原图不受影响）
	 * @throws IllegalArgumentException 当任一参数为null时抛出
	 * @since 1.0.0
	 * @see ImageUtil#brightness(Image, float)
	 */
	public static BufferedImage brightness(final BufferedImage image, final float amount) {
		if (amount == 0f) {
			return image;
		}
		Validate.inclusiveBetween(-2.0, 2.0, amount);
		BrightnessContrastFilter filter = new BrightnessContrastFilter(amount, 0f);
		return filter(image, filter);
	}

	/**
	 * 增加图像亮度
	 *
	 * @param image 原始图像（不可为null）
	 * @param amount 亮度值，范围 [-2.0.2.0]
	 * @param imageType 目标图像类型（如{@link BufferedImage#TYPE_INT_ARGB}）
	 * @return 处理后的新图像（原图不受影响）
	 * @throws IllegalArgumentException 当任一参数为null时抛出
	 * @since 1.0.0
	 * @see ImageUtil#brightness(Image, float)
	 */
	public static BufferedImage brightness(final BufferedImage image, final float amount, int imageType) {
		if (amount == 0f) {
			return image;
		}
		Validate.inclusiveBetween(-2.0, 2.0, amount);
		BrightnessContrastFilter filter = new BrightnessContrastFilter(amount, 0f);
		return filter(image, filter, imageType);
	}

	/**
	 * 应用图像滤镜处理并输出到流
	 *
	 * @param inputImage 原始图像（不可为null）
	 * @param filter 图像滤镜（不可为null）
	 * @param outputStream 输出流（不可为null）
	 * @param outputFormat 输出图片格式（非空，如PNG/JPG）
	 * @throws IOException 当写入失败时抛出
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @since 1.0.0
	 * @see ImageUtil#filter(Image, ImageFilter)
	 */
	public static void filter(final BufferedImage inputImage, final ImageFilter filter,
							  final OutputStream outputStream, final String outputFormat) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");

		BufferedImage outputImage = filter(inputImage, filter);
		ImageIO.write(outputImage, outputFormat, outputStream);
	}

	/**
	 * 应用图像滤镜并输出到文件（自动获取文件扩展名作为格式）
	 *
	 * @param inputImage 原始图像（不可为null）
	 * @param filter 图像滤镜（不可为null）
	 * @param outputFile 输出文件（不可为null）
	 * @throws IOException 当outputFile是目录或I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @since 1.0.0
	 * @see ImageUtil#filter(Image, ImageFilter)
	 */
	public static void filter(final BufferedImage inputImage, final ImageFilter filter, final File outputFile) throws IOException {
		Validate.notNull(outputFile, "outputFile 不可为 null");
		checkFile(outputFile);

		BufferedImage outputImage = filter(inputImage, filter);
		ImageIO.write(outputImage, FilenameUtils.getExtension(outputFile.getName()), outputFile);
	}

	/**
	 * 应用图像滤镜并输出到文件
	 *
	 * @param inputImage 原始图像（不可为null）
	 * @param filter 图像滤镜（不可为null）
	 * @param outputFile 输出文件（不可为null）
	 * @param outputFormat 输出图片格式（非空，如PNG/JPG）
	 * @throws IOException 当outputFile是目录或I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @since 1.0.0
	 * @see ImageUtil#filter(Image, ImageFilter)
	 */
	public static void filter(final BufferedImage inputImage, final ImageFilter filter, final File outputFile,
							  final String outputFormat) throws IOException {
		Validate.notNull(outputFile, "outputFile 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");
		checkFile(outputFile);

		BufferedImage outputImage;
		if (ImageConstants.NON_TRANSPARENT_IMAGE_FORMATS.contains(outputFormat.toLowerCase())) {
			outputImage = filter(inputImage, filter, BufferedImage.TYPE_INT_RGB);
		} else {
			outputImage = filter(inputImage, filter);
		}
		ImageIO.write(outputImage, outputFormat.toLowerCase(), outputFile);
	}

	/**
	 * 应用图像滤镜
	 *
	 * @param image 原始图像（不可为null）
	 * @param filter 图像滤镜（不可为null）
	 * @return 处理后的新图像（原图不受影响）
	 * @throws IllegalArgumentException 当任一参数为null时抛出
	 * @since 1.0.0
	 * @see ImageUtil#filter(Image, ImageFilter)
	 */
	public static BufferedImage filter(final BufferedImage image, final ImageFilter filter) {
		Validate.notNull(image, "image 不可为 null");
		Validate.notNull(filter, "filter 不可为 null");

		Image filterImage = ImageUtil.filter(image, filter);
		return ImageUtil.toBuffered(filterImage);
	}

	/**
	 * 应用图像滤镜
	 *
	 * @param image 原始图像（不可为null）
	 * @param filter 图像滤镜（不可为null）
	 * @param imageType 目标图像类型（如{@link BufferedImage#TYPE_INT_ARGB}）
	 * @return 处理后的新图像（原图不受影响）
	 * @throws IllegalArgumentException 当任一参数为null时抛出
	 * @since 1.0.0
	 * @see ImageUtil#filter(Image, ImageFilter)
	 */
	public static BufferedImage filter(final BufferedImage image, final ImageFilter filter, int imageType) {
		Validate.notNull(image, "image 不可为 null");
		Validate.notNull(filter, "filter 不可为 null");

		Image filterImage = ImageUtil.filter(image, filter);
		return ImageUtil.toBuffered(filterImage, imageType);
	}

	/**
	 * 文件校验
	 *
	 * @param file 文件对象
	 * @throws FileNotFoundException 当文件是目录时抛出
	 * @since 1.0.0
	 */
	protected static void checkFile(final File file) throws IOException {
		if (!file.exists()) {
			throw new FileNotFoundException(file.getAbsolutePath());
		}
		if (!file.isFile()) {
			throw new IOException(file.getAbsolutePath() + " 不是一个文件路径");
		}
	}
}