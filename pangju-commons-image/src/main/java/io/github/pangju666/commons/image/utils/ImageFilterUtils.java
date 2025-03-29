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

public class ImageFilterUtils {
	protected static RGBImageFilter GRAY_FILTER = new GrayFilter();
	protected static RGBImageFilter DEFAULT_BRIGHTNESS_CONTRAST_FILTER = new BrightnessContrastFilter(0f, 0.3f);

	protected ImageFilterUtils() {
	}

	public static void grayscale(final BufferedImage inputImage, final OutputStream outputStream,
								 final String outputFormat) throws IOException {
		filter(inputImage, GRAY_FILTER, outputStream, outputFormat);
	}

	public static void grayscale(final BufferedImage inputImage, final File outputFile) throws IOException {
		filter(inputImage, GRAY_FILTER, outputFile);
	}

	public static void grayscale(final BufferedImage inputImage, final File outputFile,
								 final String outputFormat) throws IOException {
		filter(inputImage, GRAY_FILTER, outputFile, outputFormat);
	}

	public static BufferedImage grayscale(final BufferedImage image) {
		return filter(image, GRAY_FILTER);
	}

	public static BufferedImage grayscale(final BufferedImage image, int imageType) {
		return filter(image, GRAY_FILTER, imageType);
	}

	public static void contrast(final BufferedImage inputImage, final OutputStream outputStream,
								final String outputFormat) throws IOException {
		filter(inputImage, DEFAULT_BRIGHTNESS_CONTRAST_FILTER, outputStream, outputFormat);
	}

	public static void contrast(final BufferedImage inputImage, final File outputFile) throws IOException {
		filter(inputImage, DEFAULT_BRIGHTNESS_CONTRAST_FILTER, outputFile);
	}

	public static void contrast(final BufferedImage inputImage, final File outputFile,
								final String outputFormat) throws IOException {
		filter(inputImage, DEFAULT_BRIGHTNESS_CONTRAST_FILTER, outputFile, outputFormat);
	}

	public static BufferedImage contrast(final BufferedImage image) {
		return filter(image, DEFAULT_BRIGHTNESS_CONTRAST_FILTER);
	}

	public static BufferedImage contrast(final BufferedImage image, int imageType) {
		return filter(image, DEFAULT_BRIGHTNESS_CONTRAST_FILTER, imageType);
	}

	public static void contrast(final BufferedImage inputImage, final float amount,
								final OutputStream outputStream, final String outputFormat) throws IOException {
		if (amount != 0f) {
			BrightnessContrastFilter filter = new BrightnessContrastFilter(0f, amount);
			filter(inputImage, filter, outputStream, outputFormat);
		}
	}

	public static void contrast(final BufferedImage inputImage, final float amount, final File outputFile) throws IOException {
		if (amount != 0f) {
			BrightnessContrastFilter filter = new BrightnessContrastFilter(0f, amount);
			filter(inputImage, filter, outputFile);
		}
	}

	public static void contrast(final BufferedImage inputImage, final float amount, final File outputFile,
								final String outputFormat) throws IOException {
		if (amount != 0f) {
			BrightnessContrastFilter filter = new BrightnessContrastFilter(0f, amount);
			filter(inputImage, filter, outputFile, outputFormat);
		}
	}

	public static BufferedImage contrast(final BufferedImage image, final float amount) {
		if (amount == 0f) {
			return image;
		}
		RGBImageFilter filter = new BrightnessContrastFilter(0f, amount);
		return filter(image, filter);
	}

	public static BufferedImage contrast(final BufferedImage image, final float amount, int imageType) {
		if (amount == 0f) {
			return image;
		}
		BrightnessContrastFilter filter = new BrightnessContrastFilter(0f, amount);
		return filter(image, filter, imageType);
	}

	public static void brightness(final BufferedImage inputImage, final float amount,
								  final OutputStream outputStream, final String outputFormat) throws IOException {
		if (amount != 0f) {
			BrightnessContrastFilter filter = new BrightnessContrastFilter(amount, 0f);
			filter(inputImage, filter, outputStream, outputFormat);
		}
	}

	public static void brightness(final BufferedImage inputImage, final float amount, final File outputFile) throws IOException {
		if (amount != 0f) {
			BrightnessContrastFilter filter = new BrightnessContrastFilter(amount, 0f);
			filter(inputImage, filter, outputFile);
		}
	}

	public static void brightness(final BufferedImage inputImage, final float amount, final File outputFile,
								  final String outputFormat) throws IOException {
		if (amount != 0f) {
			BrightnessContrastFilter filter = new BrightnessContrastFilter(amount, 0f);
			filter(inputImage, filter, outputFile, outputFormat);
		}
	}

	public static BufferedImage brightness(final BufferedImage image, final float amount) {
		if (amount == 0f) {
			return image;
		}
		BrightnessContrastFilter filter = new BrightnessContrastFilter(amount, 0f);
		return filter(image, filter);
	}

	public static BufferedImage brightness(final BufferedImage image, final float amount, int imageType) {
		if (amount == 0f) {
			return image;
		}
		BrightnessContrastFilter filter = new BrightnessContrastFilter(amount, 0f);
		return filter(image, filter, imageType);
	}

	public static void filter(final BufferedImage inputImage, final ImageFilter filter,
							  final OutputStream outputStream, final String outputFormat) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		BufferedImage outputImage = filter(inputImage, filter);
		ImageIO.write(outputImage, outputFormat, outputStream);
	}

	public static void filter(final BufferedImage inputImage, final ImageFilter filter, final File outputFile) throws IOException {
		Validate.notNull(outputFile, "outputFile 不可为 null");
		checkFile(outputFile);

		BufferedImage outputImage = filter(inputImage, filter);
		ImageIO.write(outputImage, FilenameUtils.getExtension(outputFile.getName()), outputFile);
	}

	public static void filter(final BufferedImage inputImage, final ImageFilter filter, final File outputFile,
							  final String outputFormat) throws IOException {
		Validate.notNull(outputFile, "outputFile 不可为 null");
		checkFile(outputFile);

		BufferedImage outputImage;
		if (ImageConstants.NON_TRANSPARENT_IMAGE_FORMATS.contains(outputFormat.toLowerCase())) {
			outputImage = filter(inputImage, filter, BufferedImage.TYPE_INT_RGB);
		} else {
			outputImage = filter(inputImage, filter);
		}
		ImageIO.write(outputImage, outputFormat.toLowerCase(), outputFile);
	}

	public static BufferedImage filter(final BufferedImage image, final ImageFilter filter) {
		Validate.notNull(image, "image 不可为 null");
		Validate.notNull(filter, "filter 不可为 null");

		Image filterImage = ImageUtil.filter(image, filter);
		return ImageUtil.toBuffered(filterImage);
	}

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
	protected static void checkFile(final File file) throws FileNotFoundException {
		if (file.exists() && file.isDirectory()) {
			throw new FileNotFoundException(file.getAbsolutePath());
		}
	}
}