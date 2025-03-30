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

/**
 * 缩略图生成工具类，提供多种缩放策略和输出方式
 * <p>注意事项：</p>
 * <ul>
 *   <li>默认使用{@link ResampleOp#FILTER_TRIANGLE 三角形滤波器}进行重采样</li>
 *   <li>输出{@link BufferedImage#TYPE_INT_RGB 非透明格式}(如JPG)时会自动转换为{@link BufferedImage#TYPE_INT_ARGB 透明格式}</li>
 * </ul>
 *
 * @author pangju666
 * @see ResampleOp
 * @since 1.0.0
 */
public class ThumbnailUtils {
	protected ThumbnailUtils() {
	}

	/**
	 * 强制缩放（不考虑长宽比）图像到指定尺寸并输出到流
	 *
	 * @param inputImage   原始图像（非null）
	 * @param imageSize    目标尺寸（非null）
	 * @param outputStream 输出流（非null）
	 * @param outputFormat 输出图片格式（非空，如PNG/JPG）
	 * @return 是否成功写入
	 * @throws IOException              当发生I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @since 1.0.0
	 */
	public static boolean forceScale(final BufferedImage inputImage, final OutputStream outputStream,
									 final ImageSize imageSize, final String outputFormat) throws IOException {
		return scale(inputImage, outputStream, imageSize, outputFormat, ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 强制缩放（不考虑长宽比）图像到指定尺寸并输出到流
	 *
	 * @param inputImage   原始图像（非null）
	 * @param imageSize    目标尺寸（非null）
	 * @param outputStream 输出流（非null）
	 * @param outputFormat 输出图片格式（非null，如PNG/JPG）
	 * @param filterType   重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 是否成功写入
	 * @throws IOException              当发生I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @since 1.0.0
	 */
	public static boolean forceScale(final BufferedImage inputImage, final OutputStream outputStream,
									 final ImageSize imageSize, final String outputFormat, final int filterType) throws IOException {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.notNull(imageSize, "imageSize 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");

		BufferedImage outputImage = resample(inputImage, imageSize, outputFormat, filterType);
		return ImageIO.write(outputImage, outputFormat.toLowerCase(), outputStream);
	}

	/**
	 * 强制缩放（不考虑长宽比）图像并输出到文件（自动获取文件扩展名作为格式）
	 *
	 * @param inputImage 原始图像（非null）
	 * @param imageSize  目标尺寸（非null）
	 * @param outputFile 输出文件（非null）
	 * @return 是否成功写入
	 * @throws IOException              当outputFile是目录或I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @since 1.0.0
	 */
	public static boolean forceScale(final BufferedImage inputImage, final File outputFile,
									 final ImageSize imageSize) throws IOException {
		return scale(inputImage, outputFile, imageSize,
			FilenameUtils.getExtension(outputFile.getName()), ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 强制缩放（不考虑长宽比）图像并输出到文件（自动获取文件扩展名作为格式）
	 *
	 * @param inputImage 原始图像（非null）
	 * @param imageSize  目标尺寸（非null）
	 * @param outputFile 输出文件（非null）
	 * @param filterType 重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 是否成功写入
	 * @throws IOException              当outputFile是目录或I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @since 1.0.0
	 */
	public static boolean forceScale(final BufferedImage inputImage, final File outputFile, final ImageSize imageSize,
									 final int filterType) throws IOException {
		return scale(inputImage, outputFile, imageSize,
			FilenameUtils.getExtension(outputFile.getName()), filterType);
	}

	/**
	 * 强制缩放（不考虑长宽比）图像并输出到文件
	 *
	 * @param inputImage   原始图像（非null）
	 * @param imageSize    目标尺寸（非null）
	 * @param outputFile   输出文件（非null）
	 * @param outputFormat 输出图片格式（非空，如PNG/JPG）
	 * @return 是否成功写入
	 * @throws IOException              当outputFile是目录或I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @since 1.0.0
	 */
	public static boolean forceScale(final BufferedImage inputImage, final File outputFile, final ImageSize imageSize,
									 final String outputFormat) throws IOException {
		return scale(inputImage, outputFile, imageSize, outputFormat, ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 强制缩放（不考虑长宽比）图像并输出到文件
	 *
	 * @param inputImage   原始图像（非null）
	 * @param imageSize    目标尺寸（非null）
	 * @param outputFile   输出文件（非null）
	 * @param outputFormat 输出图片格式（非空，如PNG/JPG）
	 * @param filterType   重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 是否成功写入
	 * @throws IOException              当outputFile是目录或I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @since 1.0.0
	 */
	public static boolean forceScale(final BufferedImage inputImage, final File outputFile, final ImageSize imageSize,
									 final String outputFormat, final int filterType) throws IOException {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.notNull(imageSize, "imageSize 不可为 null");
		Validate.notNull(outputFile, "outputFile 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");
		checkFile(outputFile);

		BufferedImage outputImage = resample(inputImage, imageSize, outputFormat, filterType);
		return ImageIO.write(outputImage, outputFormat.toLowerCase(), outputFile);
	}

	/**
	 * 强制缩放（不考虑长宽比）图像
	 *
	 * @param image     原始图像（非null）
	 * @param imageSize 目标尺寸（非null）
	 * @return 缩放后的图像
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @since 1.0.0
	 */
	public static BufferedImage forceScale(final BufferedImage image, final ImageSize imageSize) {
		return scale(image, imageSize, ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 强制缩放（不考虑长宽比）图像
	 *
	 * @param image      原始图像（非null）
	 * @param imageSize  目标尺寸（非null）
	 * @param filterType 重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 缩放后的图像
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @since 1.0.0
	 */
	public static BufferedImage forceScale(final BufferedImage image, final ImageSize imageSize, final int filterType) {
		Validate.notNull(image, "image 不可为 null");
		Validate.notNull(imageSize, "imageSize 不可为 null");

		return resample(image, imageSize, filterType);
	}

	/**
	 * 根据高度等比缩放图像到输出流
	 *
	 * @param inputImage   原始图像（非null）
	 * @param height       目标高度（>0）
	 * @param outputStream 输出流（非null）
	 * @param outputFormat 输出图片格式（非空，如PNG/JPG）
	 * @return 是否成功写入
	 * @throws IOException              当发生I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空或高度<=0时抛出
	 * @since 1.0.0
	 */
	public static boolean scaleByHeight(final BufferedImage inputImage, final OutputStream outputStream, final int height,
										final String outputFormat) throws IOException {
		return scaleByHeight(inputImage, outputStream, height, outputFormat, ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 根据宽度等比缩放图像到输出流
	 *
	 * @param inputImage   原始图像（非null）
	 * @param width        目标宽度（>0）
	 * @param outputStream 输出流（非null）
	 * @param outputFormat 输出图片格式（非空，如PNG/JPG）
	 * @return 是否成功写入
	 * @throws IOException              当发生I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空或宽度<=0时抛出
	 * @since 1.0.0
	 */
	public static boolean scaleByWidth(final BufferedImage inputImage, final OutputStream outputStream, final int width,
									   final String outputFormat) throws IOException {
		return scaleByWidth(inputImage, outputStream, width, outputFormat, ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 根据高度或宽度等比缩放图像到输出流
	 *
	 * @param inputImage   原始图像（非null）
	 * @param imageSize    目标尺寸（非null）
	 * @param outputStream 输出流（非null）
	 * @param outputFormat 输出图片格式（非空，如PNG/JPG）
	 * @return 是否成功写入
	 * @throws IOException              当发生I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @see ImageSize#scale(ImageSize)
	 * @since 1.0.0
	 */
	public static boolean scale(final BufferedImage inputImage, final OutputStream outputStream, final ImageSize imageSize,
								final String outputFormat) throws IOException {
		return scale(inputImage, outputStream, imageSize, outputFormat, ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 根据高度等比缩放图像到输出流
	 *
	 * @param inputImage   原始图像（非null）
	 * @param height       目标高度（>0）
	 * @param outputStream 输出流（非null）
	 * @param outputFormat 输出图片格式（非空，如PNG/JPG）
	 * @param filterType   重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 是否成功写入
	 * @throws IOException              当发生I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空或高度<=0时抛出
	 * @since 1.0.0
	 */
	public static boolean scaleByHeight(final BufferedImage inputImage, final OutputStream outputStream, final int height,
										final String outputFormat, final int filterType) throws IOException {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.isTrue(height > 0, "height 必须大于0");
		Validate.notNull(outputStream, "outputFile 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");

		ImageSize outputImageSize = new ImageSize(inputImage.getWidth(), inputImage.getHeight()).scaleByHeight(height);
		BufferedImage outputImage = resample(inputImage, outputImageSize, outputFormat, filterType);
		return ImageIO.write(outputImage, outputFormat.toLowerCase(), outputStream);
	}

	/**
	 * 根据宽度等比缩放图像到输出流
	 *
	 * @param inputImage   原始图像（非null）
	 * @param width        目标宽度（>0）
	 * @param outputStream 输出流（非null）
	 * @param outputFormat 输出图片格式（非空，如PNG/JPG）
	 * @param filterType   重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 是否成功写入
	 * @throws IOException              当发生I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空或宽度<=0时抛出
	 * @since 1.0.0
	 */
	public static boolean scaleByWidth(final BufferedImage inputImage, final OutputStream outputStream, final int width,
									   final String outputFormat, final int filterType) throws IOException {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.isTrue(width > 0, "width 必须大于0");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");

		ImageSize outputImageSize = new ImageSize(inputImage.getWidth(), inputImage.getHeight()).scaleByWidth(width);
		BufferedImage outputImage = resample(inputImage, outputImageSize, outputFormat, filterType);
		return ImageIO.write(outputImage, outputFormat.toLowerCase(), outputStream);
	}

	/**
	 * 根据高度或宽度等比缩放图像到输出流
	 *
	 * @param inputImage   原始图像（非null）
	 * @param imageSize    目标尺寸（非null）
	 * @param outputStream 输出流（非null）
	 * @param outputFormat 输出图片格式（非空，如PNG/JPG）
	 * @param filterType   重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 是否成功写入
	 * @throws IOException              当发生I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @see ImageSize#scale(ImageSize)
	 * @since 1.0.0
	 */
	public static boolean scale(final BufferedImage inputImage, final OutputStream outputStream, final ImageSize imageSize,
								final String outputFormat, final int filterType) throws IOException {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.notNull(imageSize, "imageSize 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");

		ImageSize outputImageSize = new ImageSize(inputImage.getWidth(), inputImage.getHeight()).scale(imageSize);
		BufferedImage outputImage = resample(inputImage, outputImageSize, outputFormat, filterType);
		return ImageIO.write(outputImage, outputFormat.toLowerCase(), outputStream);
	}

	/**
	 * 根据高度等比缩放图像并输出到文件（自动获取文件扩展名作为格式）
	 *
	 * @param inputImage 原始图像（非null）
	 * @param height     目标高度（>0）
	 * @param outputFile 输出文件（非null）
	 * @return 是否成功写入
	 * @throws IOException              当outputFile是目录或I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空或高度<=0时抛出
	 * @since 1.0.0
	 */
	public static boolean scaleByHeight(final BufferedImage inputImage, final File outputFile, final int height) throws IOException {
		return scaleByHeight(inputImage, outputFile, height,
			FilenameUtils.getExtension(outputFile.getName()), ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 根据宽度等比缩放图像并输出到文件（自动获取文件扩展名作为格式）
	 *
	 * @param inputImage 原始图像（非null）
	 * @param width      目标宽度（>0）
	 * @param outputFile 输出文件（非null）
	 * @return 是否成功写入
	 * @throws IOException              当outputFile是目录或I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空或宽度<=0时抛出
	 * @since 1.0.0
	 */
	public static boolean scaleByWidth(final BufferedImage inputImage, final File outputFile, final int width) throws IOException {
		return scaleByWidth(inputImage, outputFile, width,
			FilenameUtils.getExtension(outputFile.getName()), ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 根据高度或宽度等比缩放图像并输出到文件（自动获取文件扩展名作为格式）
	 *
	 * @param inputImage 原始图像（非null）
	 * @param imageSize  目标尺寸（非null）
	 * @param outputFile 输出文件（非null）
	 * @return 是否成功写入
	 * @throws IOException              当outputFile是目录或I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @see ImageSize#scale(ImageSize)
	 * @since 1.0.0
	 */
	public static boolean scale(final BufferedImage inputImage, final File outputFile, final ImageSize imageSize) throws IOException {
		return scale(inputImage, outputFile, imageSize,
			FilenameUtils.getExtension(outputFile.getName()), ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 根据高度等比缩放图像并输出到文件（自动获取文件扩展名作为格式）
	 *
	 * @param inputImage 原始图像（非null）
	 * @param height     目标高度（>0）
	 * @param outputFile 输出文件（非null）
	 * @param filterType 重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 是否成功写入
	 * @throws IOException              当outputFile是目录或I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空或高度<=0时抛出
	 * @since 1.0.0
	 */
	public static boolean scaleByHeight(final BufferedImage inputImage, final File outputFile, final int height,
										final int filterType) throws IOException {
		return scaleByHeight(inputImage, outputFile, height,
			FilenameUtils.getExtension(outputFile.getName()), filterType);
	}

	/**
	 * 根据宽度等比缩放图像并输出到文件（自动获取文件扩展名作为格式）
	 *
	 * @param inputImage 原始图像（非null）
	 * @param width      目标宽度（>0）
	 * @param outputFile 输出文件（非null）
	 * @param filterType 重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 是否成功写入
	 * @throws IOException              当outputFile是目录或I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空或宽度<=0时抛出
	 * @since 1.0.0
	 */
	public static boolean scaleByWidth(final BufferedImage inputImage, final File outputFile, final int width,
									   final int filterType) throws IOException {
		return scaleByWidth(inputImage, outputFile, width,
			FilenameUtils.getExtension(outputFile.getName()), filterType);
	}

	/**
	 * 根据高度或宽度等比缩放图像并输出到文件（自动获取文件扩展名作为格式）
	 *
	 * @param inputImage 原始图像（非null）
	 * @param imageSize  目标尺寸（非null）
	 * @param outputFile 输出文件（非null）
	 * @param filterType 重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 是否成功写入
	 * @throws IOException              当outputFile是目录或I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @see ImageSize#scale(ImageSize)
	 * @since 1.0.0
	 */
	public static boolean scale(final BufferedImage inputImage, final File outputFile, final ImageSize imageSize,
								final int filterType) throws IOException {
		return scale(inputImage, outputFile, imageSize,
			FilenameUtils.getExtension(outputFile.getName()), filterType);
	}

	/**
	 * 根据高度等比缩放图像并输出到文件
	 *
	 * @param inputImage   原始图像（非null）
	 * @param height       目标高度（>0）
	 * @param outputFile   输出文件（非null）
	 * @param outputFormat 输出图片格式（非空，如PNG/JPG）
	 * @return 是否成功写入
	 * @throws IOException              当outputFile是目录或I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空或高度<=0时抛出
	 * @since 1.0.0
	 */
	public static boolean scaleByHeight(final BufferedImage inputImage, final File outputFile, final int height,
										final String outputFormat) throws IOException {
		return scaleByHeight(inputImage, outputFile, height, outputFormat, ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 根据宽度等比缩放图像并输出到文件
	 *
	 * @param inputImage   原始图像（非null）
	 * @param width        目标宽度（>0）
	 * @param outputFile   输出文件（非null）
	 * @param outputFormat 输出图片格式（非空，如PNG/JPG）
	 * @return 是否成功写入
	 * @throws IOException              当outputFile是目录或I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空或宽度<=0时抛出
	 * @since 1.0.0
	 */
	public static boolean scaleByWidth(final BufferedImage inputImage, final File outputFile, final int width,
									   final String outputFormat) throws IOException {
		return scaleByWidth(inputImage, outputFile, width, outputFormat, ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 根据高度或宽度等比缩放图像并输出到文件
	 *
	 * @param inputImage   原始图像（非null）
	 * @param imageSize    目标尺寸（非null）
	 * @param outputFile   输出文件（非null）
	 * @param outputFormat 输出图片格式（非空，如PNG/JPG）
	 * @return 是否成功写入
	 * @throws IOException              当outputFile是目录或I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @see ImageSize#scale(ImageSize)
	 * @since 1.0.0
	 */
	public static boolean scale(final BufferedImage inputImage, final File outputFile, final ImageSize imageSize,
								final String outputFormat) throws IOException {
		return scale(inputImage, outputFile, imageSize, outputFormat, ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 根据高度等比缩放图像并输出到文件
	 *
	 * @param inputImage   原始图像（非null）
	 * @param height       目标高度（>0）
	 * @param outputFile   输出文件（非null）
	 * @param outputFormat 输出图片格式（非空，如PNG/JPG）
	 * @param filterType   重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 是否成功写入
	 * @throws IOException              当outputFile是目录或I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空或高度<=0时抛出
	 * @since 1.0.0
	 */
	public static boolean scaleByHeight(final BufferedImage inputImage, final File outputFile, final int height,
										final String outputFormat, final int filterType) throws IOException {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.isTrue(height > 0, "height 必须大于0");
		Validate.notNull(outputFile, "outputFile 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");
		checkFile(outputFile);

		ImageSize outputImageSize = new ImageSize(inputImage.getWidth(), inputImage.getHeight()).scaleByHeight(height);
		BufferedImage outputImage = resample(inputImage, outputImageSize, outputFormat, filterType);
		return ImageIO.write(outputImage, outputFormat.toLowerCase(), outputFile);
	}

	/**
	 * 根据宽度等比缩放图像并输出到文件
	 *
	 * @param inputImage   原始图像（非null）
	 * @param width        目标宽度（>0）
	 * @param outputFile   输出文件（非null）
	 * @param outputFormat 输出图片格式（非空，如PNG/JPG）
	 * @param filterType   重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 是否成功写入
	 * @throws IOException              当outputFile是目录或I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空或宽度<=0时抛出
	 * @since 1.0.0
	 */
	public static boolean scaleByWidth(final BufferedImage inputImage, final File outputFile, final int width,
									   final String outputFormat, final int filterType) throws IOException {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.isTrue(width > 0, "width 必须大于0");
		Validate.notNull(outputFile, "outputFile 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");
		checkFile(outputFile);

		ImageSize outputImageSize = new ImageSize(inputImage.getWidth(), inputImage.getHeight()).scaleByWidth(width);
		BufferedImage outputImage = resample(inputImage, outputImageSize, outputFormat, filterType);
		return ImageIO.write(outputImage, outputFormat.toLowerCase(), outputFile);
	}

	/**
	 * 根据高度或宽度等比缩放图像并输出到文件
	 *
	 * @param inputImage   原始图像（非null）
	 * @param imageSize    目标尺寸（非null）
	 * @param outputFile   输出文件（非null）
	 * @param outputFormat 输出图片格式（非空，如PNG/JPG）
	 * @param filterType   重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 是否成功写入
	 * @throws IOException              当outputFile是目录或I/O错误时抛出
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @see ImageSize#scale(ImageSize)
	 * @since 1.0.0
	 */
	public static boolean scale(final BufferedImage inputImage, final File outputFile, final ImageSize imageSize,
								final String outputFormat, final int filterType) throws IOException {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.notNull(imageSize, "imageSize 不可为 null");
		Validate.notNull(outputFile, "outputFile 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");
		checkFile(outputFile);

		ImageSize outputImageSize = new ImageSize(inputImage.getWidth(), inputImage.getHeight()).scale(imageSize);
		BufferedImage outputImage = resample(inputImage, outputImageSize, outputFormat, filterType);
		return ImageIO.write(outputImage, outputFormat.toLowerCase(), outputFile);
	}

	/**
	 * 缩放图像
	 * <p>示例：
	 * <blockquote><pre>
	 * // 根据宽高定义不透明通道图像
	 * BufferedImage outputImage1 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	 * ThumbnailUtils.scale(inputImage, outputImage1)
	 *
	 * // 根据宽高定义不透明通道图像
	 * BufferedImage outputImage2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	 * ThumbnailUtils.scale(inputImage, outputImage2)
	 * </pre></blockquote></p>
	 *
	 * @param inputImage  原始图像（非null）
	 * @param outputImage 缩放后的图像
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @since 1.0.0
	 */
	public static void scale(final BufferedImage inputImage, final BufferedImage outputImage) {
		scale(inputImage, outputImage, ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 缩放图像
	 * <p>示例：
	 * <blockquote><pre>
	 * // 根据宽高定义不透明通道图像
	 * BufferedImage outputImage1 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	 * ThumbnailUtils.scale(inputImage, outputImage1, ResampleOp.FILTER_TRIANGLE)
	 *
	 * // 根据宽高定义不透明通道图像
	 * BufferedImage outputImage2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	 * ThumbnailUtils.scale(inputImage, outputImage2, ResampleOp.FILTER_TRIANGLE)
	 * </pre></blockquote></p>
	 *
	 * @param inputImage  原始图像（非null）
	 * @param outputImage 缩放后的图像（非null）
	 * @param filterType  重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @since 1.0.0
	 */
	public static void scale(final BufferedImage inputImage, final BufferedImage outputImage, final int filterType) {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.notNull(outputImage, "outputImage 不可为 null");

		ResampleOp resampleOp = new ResampleOp(outputImage.getWidth(), outputImage.getHeight(), filterType);
		resampleOp.filter(inputImage, outputImage);
	}

	/**
	 * 根据高度等比缩放图像
	 *
	 * @param image  原始图像（非null）
	 * @param height 目标高度（>0）
	 * @return 缩放后的图像
	 * @throws IllegalArgumentException 当任一参数为空或高度<=0时抛出
	 * @since 1.0.0
	 */
	public static BufferedImage scaleByHeight(final BufferedImage image, final int height) {
		return scaleByHeight(image, height, ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 根据宽度等比缩放图像
	 *
	 * @param image 原始图像（非null）
	 * @param width 目标宽度（>0）
	 * @return 缩放后的图像
	 * @throws IllegalArgumentException 当任一参数为空或宽度<=0时抛出
	 * @since 1.0.0
	 */
	public static BufferedImage scaleByWidth(final BufferedImage image, final int width) {
		return scaleByWidth(image, width, ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 根据高度或宽度等比缩放图像并输出到文件（自动获取文件扩展名作为格式）
	 *
	 * @param image     原始图像（非null）
	 * @param imageSize 目标尺寸（非null）
	 * @return 是否成功写入
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @see ImageSize#scale(ImageSize)
	 * @since 1.0.0
	 */
	public static BufferedImage scale(final BufferedImage image, final ImageSize imageSize) {
		return scale(image, imageSize, ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 根据高度等比缩放图像
	 *
	 * @param image      原始图像（非null）
	 * @param height     目标高度（>0）
	 * @param filterType 重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 缩放后的图像
	 * @throws IllegalArgumentException 当任一参数为空或高度<=0时抛出
	 * @since 1.0.0
	 */
	public static BufferedImage scaleByHeight(final BufferedImage image, final int height, final int filterType) {
		Validate.notNull(image, "image 不可为 null");
		Validate.isTrue(height > 0, "height 必须大于0");

		ImageSize outputImageSize = new ImageSize(image.getWidth(), image.getHeight()).scaleByHeight(height);
		return resample(image, outputImageSize, filterType);
	}

	/**
	 * 根据宽度等比缩放图像
	 *
	 * @param image      原始图像（非null）
	 * @param width      目标宽度（>0）
	 * @param filterType 重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 缩放后的图像
	 * @throws IllegalArgumentException 当任一参数为空或宽度<=0时抛出
	 * @since 1.0.0
	 */
	public static BufferedImage scaleByWidth(final BufferedImage image, final int width, final int filterType) {
		Validate.notNull(image, "image 不可为 null");
		Validate.isTrue(width > 0, "width 必须大于0");

		ImageSize outputImageSize = new ImageSize(image.getWidth(), image.getHeight()).scaleByWidth(width);
		return resample(image, outputImageSize, filterType);
	}

	/**
	 * 根据高度或宽度等比缩放图像
	 *
	 * @param image      原始图像（非null）
	 * @param imageSize  目标尺寸（非null）
	 * @param filterType 重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 缩放后的图像
	 * @throws IllegalArgumentException 当任一参数为空时抛出
	 * @see ImageSize#scale(ImageSize)
	 * @since 1.0.0
	 */
	public static BufferedImage scale(final BufferedImage image, final ImageSize imageSize, final int filterType) {
		Validate.notNull(image, "image 不可为 null");
		Validate.notNull(imageSize, "imageSize 不可为 null");

		ImageSize outputImageSize = new ImageSize(image.getWidth(), image.getHeight()).scale(imageSize);
		return resample(image, outputImageSize, filterType);
	}

	/**
	 * 图像重采样方法（处理透明通道）
	 * <p>注意事项：</p>
	 * <ul>
	 *   <li>当输出格式为JPG等不支持透明格式时，自动转换为RGB模式</li>
	 *   <li>透明格式（如PNG）保留Alpha通道</li>
	 * </ul>
	 *
	 * @param inputImage      原始图像
	 * @param outputImageSize 输出尺寸
	 * @param outputFormat    输出图片格式（非空，如PNG/JPG）
	 * @param filterType      滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 处理后的图像
	 * @since 1.0.0
	 */
	protected static BufferedImage resample(final BufferedImage inputImage, final ImageSize outputImageSize,
											final String outputFormat, final int filterType) {
		if (ImageConstants.NON_TRANSPARENT_IMAGE_FORMATS.contains(outputFormat.toLowerCase())) {
			BufferedImage outputImage = new BufferedImage(outputImageSize.width(), outputImageSize.height(),
				BufferedImage.TYPE_INT_RGB);
			ResampleOp resampleOp = new ResampleOp(outputImageSize.width(), outputImageSize.height(), filterType);
			resampleOp.filter(inputImage, outputImage);
			return outputImage;
		} else {
			return resample(inputImage, outputImageSize, filterType);
		}
	}

	/**
	 * 图像重采样方法
	 *
	 * @param inputImage      原始图像
	 * @param outputImageSize 输出尺寸
	 * @param filterType      滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 处理后的图像
	 * @since 1.0.0
	 */
	protected static BufferedImage resample(final BufferedImage inputImage, final ImageSize outputImageSize,
											final int filterType) {
		ResampleOp resampleOp = new ResampleOp(outputImageSize.width(), outputImageSize.height(), filterType);
		return resampleOp.filter(inputImage, null);
	}

	/**
	 * 文件校验
	 *
	 * @param file 文件对象
	 * @throws FileNotFoundException 当文件是目录时抛出
	 * @since 1.0.0
	 */
	protected static void checkFile(final File file) throws IOException {
		if (file.exists() && file.isDirectory()) {
			throw new IOException(file.getAbsolutePath() + " 不是一个文件路径");
		}
	}
}
