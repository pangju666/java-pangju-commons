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

import com.twelvemonkeys.image.ResampleOp;
import io.github.pangju666.commons.image.lang.ImageConstants;
import io.github.pangju666.commons.image.model.ImageSize;
import io.github.pangju666.commons.io.utils.FilenameUtils;
import org.apache.commons.lang3.Validate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public class ThumbnailUtils {
	protected ThumbnailUtils() {
	}

	public static boolean forceScale(final BufferedImage inputImage, final ImageSize imageSize, final OutputStream outputStream,
									 final String outputFormat) throws IOException {
		return scale(inputImage, imageSize, outputStream, outputFormat, ResampleOp.FILTER_TRIANGLE);
	}

	public static boolean forceScale(final BufferedImage inputImage, final ImageSize imageSize, final OutputStream outputStream,
									 final String outputFormat, final int filterType) throws IOException {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.notNull(imageSize, "imageSize 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");

		BufferedImage outputImage = resample(inputImage, imageSize, outputFormat, filterType);
		return ImageIO.write(outputImage, outputFormat, outputStream);
	}

	public static boolean forceScale(final BufferedImage inputImage, final ImageSize imageSize, final File outputFile) throws IOException {
		return scale(inputImage, imageSize, outputFile,
			FilenameUtils.getExtension(outputFile.getName()), ResampleOp.FILTER_TRIANGLE);
	}

	public static boolean forceScale(final BufferedImage inputImage, final ImageSize imageSize, final File outputFile,
									 final int filterType) throws IOException {
		return scale(inputImage, imageSize, outputFile,
			FilenameUtils.getExtension(outputFile.getName()), filterType);
	}

	public static boolean forceScale(final BufferedImage inputImage, final ImageSize imageSize, final File outputFile,
									 final String outputFormat) throws IOException {
		return scale(inputImage, imageSize, outputFile, outputFormat, ResampleOp.FILTER_TRIANGLE);
	}

	public static boolean forceScale(final BufferedImage inputImage, final ImageSize imageSize, final File outputFile,
									 final String outputFormat, final int filterType) throws IOException {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.notNull(imageSize, "imageSize 不可为 null");
		Validate.notNull(outputFile, "outputFile 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");
		checkFile(outputFile);

		BufferedImage outputImage = resample(inputImage, imageSize, outputFormat, filterType);
		return ImageIO.write(outputImage, outputFormat, outputFile);
	}

	public static BufferedImage forceScale(final BufferedImage image, final ImageSize imageSize) {
		return scale(image, imageSize, ResampleOp.FILTER_TRIANGLE);
	}

	public static BufferedImage forceScale(final BufferedImage image, final ImageSize imageSize, final int filterType) {
		Validate.notNull(image, "image 不可为 null");
		Validate.notNull(imageSize, "imageSize 不可为 null");

		return resample(image, imageSize, filterType);
	}

	public static boolean scaleByHeight(final BufferedImage inputImage, final int height, final OutputStream outputStream,
										final String outputFormat) throws IOException {
		return scaleByHeight(inputImage, height, outputStream, outputFormat, ResampleOp.FILTER_TRIANGLE);
	}

	public static boolean scaleByWidth(final BufferedImage inputImage, final int width, final OutputStream outputStream,
									   final String outputFormat) throws IOException {
		return scaleByWidth(inputImage, width, outputStream, outputFormat, ResampleOp.FILTER_TRIANGLE);
	}

	public static boolean scale(final BufferedImage inputImage, final ImageSize imageSize, final OutputStream outputStream,
								final String outputFormat) throws IOException {
		return scale(inputImage, imageSize, outputStream, outputFormat, ResampleOp.FILTER_TRIANGLE);
	}

	public static boolean scaleByHeight(final BufferedImage inputImage, final int height, final OutputStream outputStream,
										final String outputFormat, final int filterType) throws IOException {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.isTrue(height > 0, "height 必须大于0");
		Validate.notNull(outputStream, "outputFile 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");

		ImageSize outputImageSize = new ImageSize(inputImage.getWidth(), inputImage.getHeight()).scaleByHeight(height);
		BufferedImage outputImage = resample(inputImage, outputImageSize, outputFormat, filterType);
		return ImageIO.write(outputImage, outputFormat, outputStream);
	}

	public static boolean scaleByWidth(final BufferedImage inputImage, final int width, final OutputStream outputStream,
									   final String outputFormat, final int filterType) throws IOException {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.isTrue(width > 0, "width 必须大于0");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");

		ImageSize outputImageSize = new ImageSize(inputImage.getWidth(), inputImage.getHeight()).scaleByWidth(width);
		BufferedImage outputImage = resample(inputImage, outputImageSize, outputFormat, filterType);
		return ImageIO.write(outputImage, outputFormat, outputStream);
	}

	public static boolean scale(final BufferedImage inputImage, final ImageSize imageSize, final OutputStream outputStream,
								final String outputFormat, final int filterType) throws IOException {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.notNull(imageSize, "imageSize 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");

		ImageSize outputImageSize = new ImageSize(inputImage.getWidth(), inputImage.getHeight()).scale(imageSize);
		BufferedImage outputImage = resample(inputImage, outputImageSize, outputFormat, filterType);
		return ImageIO.write(outputImage, outputFormat, outputStream);
	}

	public static boolean scaleByHeight(final BufferedImage inputImage, final int height, final File outputFile) throws IOException {
		return scaleByHeight(inputImage, height, outputFile,
			FilenameUtils.getExtension(outputFile.getName()), ResampleOp.FILTER_TRIANGLE);
	}

	public static boolean scaleByWidth(final BufferedImage inputImage, final int width, final File outputFile) throws IOException {
		return scaleByWidth(inputImage, width, outputFile,
			FilenameUtils.getExtension(outputFile.getName()), ResampleOp.FILTER_TRIANGLE);
	}

	public static boolean scale(final BufferedImage inputImage, final ImageSize imageSize, final File outputFile) throws IOException {
		return scale(inputImage, imageSize, outputFile,
			FilenameUtils.getExtension(outputFile.getName()), ResampleOp.FILTER_TRIANGLE);
	}

	public static boolean scaleByHeight(final BufferedImage inputImage, final int height, final File outputFile,
										final int filterType) throws IOException {
		return scaleByHeight(inputImage, height, outputFile,
			FilenameUtils.getExtension(outputFile.getName()), filterType);
	}

	public static boolean scaleByWidth(final BufferedImage inputImage, final int width, final File outputFile,
									   final int filterType) throws IOException {
		return scaleByWidth(inputImage, width, outputFile,
			FilenameUtils.getExtension(outputFile.getName()), filterType);
	}

	public static boolean scale(final BufferedImage inputImage, final ImageSize imageSize, final File outputFile,
								final int filterType) throws IOException {
		return scale(inputImage, imageSize, outputFile,
			FilenameUtils.getExtension(outputFile.getName()), filterType);
	}

	public static boolean scaleByHeight(final BufferedImage inputImage, final int height, final File outputFile,
										final String outputFormat) throws IOException {
		return scaleByHeight(inputImage, height, outputFile, outputFormat, ResampleOp.FILTER_TRIANGLE);
	}

	public static boolean scaleByWidth(final BufferedImage inputImage, final int width, final File outputFile,
									   final String outputFormat) throws IOException {
		return scaleByWidth(inputImage, width, outputFile, outputFormat, ResampleOp.FILTER_TRIANGLE);
	}

	public static boolean scale(final BufferedImage inputImage, final ImageSize imageSize, final File outputFile,
								final String outputFormat) throws IOException {
		return scale(inputImage, imageSize, outputFile, outputFormat, ResampleOp.FILTER_TRIANGLE);
	}

	public static boolean scaleByHeight(final BufferedImage inputImage, final int height, final File outputFile,
										final String outputFormat, final int filterType) throws IOException {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.isTrue(height > 0, "height 必须大于0");
		Validate.notNull(outputFile, "outputFile 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");
		checkFile(outputFile);

		ImageSize outputImageSize = new ImageSize(inputImage.getWidth(), inputImage.getHeight()).scaleByHeight(height);
		BufferedImage outputImage = resample(inputImage, outputImageSize, outputFormat, filterType);
		return ImageIO.write(outputImage, outputFormat, outputFile);
	}

	public static boolean scaleByWidth(final BufferedImage inputImage, final int width, final File outputFile,
									   final String outputFormat, final int filterType) throws IOException {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.isTrue(width > 0, "width 必须大于0");
		Validate.notNull(outputFile, "outputFile 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");
		checkFile(outputFile);

		ImageSize outputImageSize = new ImageSize(inputImage.getWidth(), inputImage.getHeight()).scaleByWidth(width);
		BufferedImage outputImage = resample(inputImage, outputImageSize, outputFormat, filterType);
		return ImageIO.write(outputImage, outputFormat, outputFile);
	}

	public static boolean scale(final BufferedImage inputImage, final ImageSize imageSize, final File outputFile,
								final String outputFormat, final int filterType) throws IOException {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.notNull(imageSize, "imageSize 不可为 null");
		Validate.notNull(outputFile, "outputFile 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");
		checkFile(outputFile);

		ImageSize outputImageSize = new ImageSize(inputImage.getWidth(), inputImage.getHeight()).scale(imageSize);
		BufferedImage outputImage = resample(inputImage, outputImageSize, outputFormat, filterType);
		return ImageIO.write(outputImage, outputFormat, outputFile);
	}

	public static void scale(final BufferedImage inputImage, final BufferedImage outputImage) {
		scale(inputImage, outputImage, ResampleOp.FILTER_TRIANGLE);
	}

	public static void scale(final BufferedImage inputImage, final BufferedImage outputImage, final int filterType) {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.notNull(outputImage, "outputImage 不可为 null");

		ResampleOp resampleOp = new ResampleOp(outputImage.getWidth(), outputImage.getHeight(), filterType);
		resampleOp.filter(inputImage, outputImage);
	}

	public static BufferedImage scaleByHeight(final BufferedImage image, final int height) {
		return scaleByHeight(image, height, ResampleOp.FILTER_TRIANGLE);
	}

	public static BufferedImage scaleByWidth(final BufferedImage image, final int width) {
		return scaleByWidth(image, width, ResampleOp.FILTER_TRIANGLE);
	}

	public static BufferedImage scale(final BufferedImage image, final ImageSize imageSize) {
		return scale(image, imageSize, ResampleOp.FILTER_TRIANGLE);
	}

	public static BufferedImage scaleByHeight(final BufferedImage image, final int height, final int filterType) {
		Validate.notNull(image, "image 不可为 null");
		Validate.isTrue(height > 0, "height 必须大于0");

		ImageSize outputImageSize = new ImageSize(image.getWidth(), image.getHeight()).scaleByHeight(height);
		return resample(image, outputImageSize, filterType);
	}

	public static BufferedImage scaleByWidth(final BufferedImage image, final int width, final int filterType) {
		Validate.notNull(image, "image 不可为 null");
		Validate.isTrue(width > 0, "width 必须大于0");

		ImageSize outputImageSize = new ImageSize(image.getWidth(), image.getHeight()).scaleByWidth(width);
		return resample(image, outputImageSize, filterType);
	}

	public static BufferedImage scale(final BufferedImage image, final ImageSize imageSize, final int filterType) {
		Validate.notNull(image, "image 不可为 null");
		Validate.notNull(imageSize, "imageSize 不可为 null");

		ImageSize outputImageSize = new ImageSize(image.getWidth(), image.getHeight()).scale(imageSize);
		return resample(image, outputImageSize, filterType);
	}

	protected static BufferedImage resample(BufferedImage inputImage, ImageSize outputImageSize, String outputFormat, int filterType) {
		if (ImageConstants.NON_TRANSPARENT_IMAGE_FORMATS.contains(outputFormat)) {
			BufferedImage outputImage = new BufferedImage(outputImageSize.width(), outputImageSize.height(), BufferedImage.TYPE_INT_RGB);
			ResampleOp resampleOp = new ResampleOp(outputImageSize.width(), outputImageSize.height(), filterType);
			resampleOp.filter(inputImage, outputImage);
			return outputImage;
		} else {
			return resample(inputImage, outputImageSize, filterType);
		}
	}

	protected static BufferedImage resample(BufferedImage inputImage, ImageSize outputImageSize, int filterType) {
		ResampleOp resampleOp = new ResampleOp(outputImageSize.width(), outputImageSize.height(), filterType);
		return resampleOp.filter(inputImage, null);
	}

	protected static void checkFile(final File file) throws FileNotFoundException {
		if (file.exists() && file.isDirectory()) {
			throw new FileNotFoundException(file.getAbsolutePath());
		}
	}
}
