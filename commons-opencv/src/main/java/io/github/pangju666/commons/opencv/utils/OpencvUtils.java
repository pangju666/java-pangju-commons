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

package io.github.pangju666.commons.opencv.utils;

import io.github.pangju666.commons.io.lang.IOConstants;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.FilenameUtils;
import io.github.pangju666.commons.io.utils.IOUtils;
import io.github.pangju666.commons.opencv.enums.FlipDirection;
import io.github.pangju666.commons.opencv.enums.RotateDirection;
import io.github.pangju666.commons.opencv.lang.OpencvConstants;
import io.github.pangju666.commons.opencv.processor.ImageProcessor;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.Validate;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.indexer.DoubleIndexer;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * OpenCV 工具类
 *
 * <p>提供 OpenCV 图像处理的常用工具方法，包括图像读写、颜色转换、尺寸缩放、亮度对比度调整、透明度处理等功能。</p>
 *
 * <h2>主要功能</h2>
 * <ul>
 *   <li><strong>空值判断</strong>：支持判断 Mat 对象的空值检查</li>
 *   <li><strong>图像读写</strong>：支持从文件、输入流、字节数组读取图像</li>
 *   <li><strong>格式支持检查</strong>：支持检查 OpenCV 是否可以读取或写入指定格式的图像</li>
 *   <li><strong>颜色转换</strong>：支持 AWT Color 与 OpenCV Scalar 之间的 BGR/RGBA 格式转换</li>
 *   <li><strong>尺寸缩放</strong>：支持按宽度、高度、比例、目标尺寸等多种缩放方式</li>
 *   <li><strong>图像增强</strong>：支持透明区域清理、EXIF 方向校正</li>
 *   <li><strong>工具方法</strong>：支持矩阵创建、卷积核创建、平移矩阵等</li>
 * </ul>
 *
 * @author pangju666
 * @see OpencvConstants
 * @see ImageProcessor
 * @since 2.1.0
 */
public class OpencvUtils {
	/**
	 * 私有构造函数，防止实例化
	 *
	 * @since 2.1.0
	 */
	protected OpencvUtils() {
	}

	/**
	 * 判断图像 Mat 是否为空
	 *
	 * @param mat 图像 Mat 对象，允许为 null
	 * @return 如果 mat 为 null、为空对象或没有数据，返回 true；否则返回 false
	 * @since 2.1.0
	 */
	public static boolean isEmpty(final Mat mat) {
		return Objects.isNull(mat) || mat.isNull() || mat.empty();
	}

	/**
	 * 判断图像 Mat 是否不为空
	 *
	 * @param mat 图像 Mat 对象，允许为 null
	 * @return 如果 mat 不为 null、不为空对象且有数据，返回 true；否则返回 false
	 * @since 2.1.0
	 */
	public static boolean isNotEmpty(final Mat mat) {
		return !isEmpty(mat);
	}

	/**
	 * 检查 OpenCV 是否可以读取指定格式的图像文件
	 *
	 * @param file 图像文件，不能为 null 且必须是图像文件
	 * @return 如果可以读取返回 true，否则返回 false
	 * @throws IOException              如果文件操作失败时抛出
	 * @throws IllegalArgumentException 如果 file 不是图像文件时抛出
	 * @since 2.1.0
	 */
	public static boolean canRead(final File file) throws IOException {
		Validate.isTrue(FileUtils.isImageType(file), "file 不是一个图像文件");

		return opencv_imgcodecs.haveImageReader(file.getAbsolutePath());
	}

	/**
	 * 检查 OpenCV 是否可以写入指定格式的图像文件
	 *
	 * @param format 图像格式扩展名（如 "jpg"、"png"），不能为空白
	 * @return 如果可以写入返回 true，否则返回 false
	 * @throws IllegalArgumentException 如果 format 为空白时抛出
	 * @since 2.1.0
	 */
	public static boolean canWrite(final String format) {
		Validate.notBlank(format, "format 不可为空");

		return opencv_imgcodecs.haveImageWriter((format.toLowerCase().startsWith(
			FilenameUtils.EXTENSION_SEPARATOR_STR) ? StringUtils.EMPTY : FilenameUtils.EXTENSION_SEPARATOR) + format);
	}

	/**
	 * 获取图像文件的尺寸（宽度和高度）
	 *
	 * @param file 图像文件，不能为 null 且必须是图像文件
	 * @return 图像尺寸对象，如果读取失败返回 null
	 * @throws IOException              如果文件操作失败时抛出
	 * @throws IllegalArgumentException 如果 file 不是图像文件时抛出
	 * @since 2.1.0
	 */
	public static Size getSize(final File file) throws IOException {
		Validate.isTrue(FileUtils.isImageType(file), "file 不是一个图像文件");

		try (Mat mat = opencv_imgcodecs.imread(file.getAbsolutePath())) {
			if (mat.isNull() || mat.empty()) {
				return null;
			}
			return mat.size();
		}
	}

	/**
	 * 从文件读取图像，使用 BGR 颜色类型
	 *
	 * @param file 图像文件，不能为 null 且必须是图像文件
	 * @return 图像 Mat 对象
	 * @throws IOException              如果文件操作失败时抛出
	 * @throws IllegalArgumentException 如果 file 不是图像文件时抛出
	 * @since 2.1.0
	 */
	public static Mat read(final File file) throws IOException {
		return read(file, OpencvConstants.DEFAULT_IMAGE_COLOR_TYPE);
	}

	/**
	 * 从文件读取图像，使用指定的颜色类型
	 *
	 * @param file  图像文件，不能为 null 且必须是图像文件
	 * @param flags 读取标志（如 IMREAD_COLOR、IMREAD_GRAYSCALE 等）
	 * @return 图像 Mat 对象
	 * @throws IOException              如果文件操作失败时抛出
	 * @throws IllegalArgumentException 如果 file 不是图像文件时抛出
	 * @since 2.1.0
	 */
	public static Mat read(final File file, final int flags) throws IOException {
		Validate.isTrue(FileUtils.isImageType(file), "file 不是一个图像文件");

		return opencv_imgcodecs.imread(file.getAbsolutePath(), flags);
	}

	/**
	 * 从输入流读取图像，使用 BGR 颜色类型
	 *
	 * @param inputStream 输入流，不能为 null
	 * @return 图像 Mat 对象
	 * @throws IOException              如果流读取失败时抛出
	 * @throws IllegalArgumentException 如果 inputStream 为 null 时抛出
	 * @since 2.1.0
	 */
	public static Mat read(final InputStream inputStream) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		return read(inputStream, OpencvConstants.DEFAULT_IMAGE_COLOR_TYPE);
	}

	/**
	 * 从输入流读取图像，使用指定的颜色类型
	 *
	 * @param inputStream 输入流，不能为 null
	 * @param flags       读取标志（如 IMREAD_COLOR、IMREAD_GRAYSCALE 等）
	 * @return 图像 Mat 对象
	 * @throws IOException              如果流读取失败时抛出
	 * @throws IllegalArgumentException 如果 inputStream 为 null 时抛出
	 * @since 2.1.0
	 */
	public static Mat read(final InputStream inputStream, final int flags) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		UnsynchronizedByteArrayOutputStream outputStream = IOUtils.toUnsynchronizedByteArrayOutputStream(inputStream);
		try (BytePointer bytePointer = new BytePointer(outputStream.toByteArray());
		     Mat bytesMat = new Mat(bytePointer)) {
			return opencv_imgcodecs.imdecode(bytesMat, flags);
		}
	}

	/**
	 * 从字节数组读取图像，使用 BGR 颜色类型
	 *
	 * @param bytes 图像字节数组，不能为 null 或空
	 * @return 图像 Mat 对象
	 * @throws IllegalArgumentException 如果 bytes 为 null 或空，或者不是图像数据时抛出
	 * @since 2.1.0
	 */
	public static Mat read(final byte[] bytes) {
		return read(bytes, OpencvConstants.DEFAULT_IMAGE_COLOR_TYPE);
	}

	/**
	 * 从字节数组读取图像，使用指定的颜色类型
	 *
	 * @param bytes 图像字节数组，不能为 null 或空
	 * @param flags 读取标志（如 IMREAD_COLOR、IMREAD_GRAYSCALE 等）
	 * @return 图像 Mat 对象
	 * @throws IllegalArgumentException 如果 bytes 为 null 或空，或者不是图像数据时抛出
	 * @since 2.1.0
	 */
	public static Mat read(final byte[] bytes, final int flags) {
		Validate.isTrue(ArrayUtils.isNotEmpty(bytes), "bytes 不可为空");

		String mimeType = IOConstants.getDefaultTika().detect(bytes);
		Validate.isTrue(Strings.CS.startsWith(mimeType, IOConstants.IMAGE_MIME_TYPE_PREFIX),
			"bytes 不是一个图像数据");

		try (BytePointer bytePointer = new BytePointer(bytes);
		     Mat bytesMat = new Mat(bytePointer)) {
			return opencv_imgcodecs.imdecode(bytesMat, flags);
		}
	}

	/**
	 * 将 AWT Color 转换为 OpenCV BGRA 格式的 Scalar
	 *
	 * @param color AWT 颜色对象，不能为 null
	 * @return BGRA 格式的 Scalar 对象（蓝、绿、红、Alpha）
	 * @throws IllegalArgumentException 如果 color 为 null 时抛出
	 * @since 2.1.0
	 */
	public static Scalar toBGRAColor(final Color color) {
		Validate.notNull(color, "color 不可为 null");

		return new Scalar(color.getBlue(), color.getGreen(), color.getRed(), color.getAlpha());
	}

	/**
	 * 将颜色字符串转换为 OpenCV BGRA 格式的 Scalar
	 *
	 * @param colorStr 颜色字符串（如 "#FF0000"、"red"），不能为空白
	 * @return BGRA 格式的 Scalar 对象
	 * @throws IllegalArgumentException 如果 colorStr 为空白时抛出
	 * @see Color#decode(String)
	 * @since 2.1.0
	 */
	public static Scalar toBGRAColor(final String colorStr) {
		Validate.notBlank(colorStr, "colorStr 不可为空");

		return toBGRAColor(Color.decode(colorStr));
	}

	/**
	 * 将 AWT Color 转换为 OpenCV BGR 格式的 Scalar（不透明）
	 *
	 * @param color AWT 颜色对象，不能为 null
	 * @return BGR 格式的 Scalar 对象（蓝、绿、红，Alpha=255）
	 * @throws IllegalArgumentException 如果 color 为 null 时抛出
	 * @since 2.1.0
	 */
	public static Scalar toBGRColor(final Color color) {
		Validate.notNull(color, "color 不可为 null");

		return new Scalar(color.getBlue(), color.getGreen(), color.getRed(), 255);
	}

	/**
	 * 将颜色字符串转换为 OpenCV BGR 格式的 Scalar（不透明）
	 *
	 * @param colorStr 颜色字符串（如 "#FF0000"、"red"），不能为空白
	 * @return BGR 格式的 Scalar 对象
	 * @throws IllegalArgumentException 如果 colorStr 为空白时抛出
	 * @see Color#decode(String)
	 * @since 2.1.0
	 */
	public static Scalar toBGRColor(final String colorStr) {
		Validate.notBlank(colorStr, "colorStr 不可为空");

		return toBGRColor(Color.decode(colorStr));
	}

	/**
	 * 将 AWT Color 转换为 OpenCV RGBA 格式的 Scalar
	 *
	 * @param color AWT 颜色对象，不能为 null
	 * @return RGBA 格式的 Scalar 对象（红、绿、蓝、Alpha）
	 * @throws IllegalArgumentException 如果 color 为 null 时抛出
	 * @since 2.1.0
	 */
	public static Scalar toRGBAColor(final Color color) {
		Validate.notNull(color, "color 不可为 null");

		return new Scalar(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}

	/**
	 * 将颜色字符串转换为 OpenCV RGBA 格式的 Scalar
	 *
	 * @param colorStr 颜色字符串（如 "#FF0000"、"red"），不能为空白
	 * @return RGBA 格式的 Scalar 对象
	 * @throws IllegalArgumentException 如果 colorStr 为空白时抛出
	 * @see Color#decode(String)
	 * @since 2.1.0
	 */
	public static Scalar toRGBAColor(final String colorStr) {
		Validate.notBlank(colorStr, "colorStr 不可为空");

		return toRGBAColor(Color.decode(colorStr));
	}

	/**
	 * 将 AWT Color 转换为 OpenCV RGB 格式的 Scalar（不透明）
	 *
	 * @param color AWT 颜色对象，不能为 null
	 * @return RGB 格式的 Scalar 对象（红、绿、蓝，Alpha=255）
	 * @throws IllegalArgumentException 如果 color 为 null 时抛出
	 * @since 2.1.0
	 */
	public static Scalar toRGBColor(final Color color) {
		Validate.notNull(color, "color 不可为 null");

		return new Scalar(color.getRed(), color.getGreen(), color.getBlue(), 255);
	}

	/**
	 * 将颜色字符串转换为 OpenCV RGB 格式的 Scalar（不透明）
	 *
	 * @param colorStr 颜色字符串（如 "#FF0000"、"red"），不能为空白
	 * @return RGB 格式的 Scalar 对象
	 * @throws IllegalArgumentException 如果 colorStr 为空白时抛出
	 * @see Color#decode(String)
	 * @since 2.1.0
	 */
	public static Scalar toRGBColor(final String colorStr) {
		Validate.notBlank(colorStr, "colorStr 不可为空");

		return toRGBColor(Color.decode(colorStr));
	}

	/**
	 * 创建 3x3 的卷积核矩阵
	 *
	 * @param kernelData 卷积核数据数组，不能为 null 或空，必须包含 9 个元素
	 * @return 3x3 的 Mat 卷积核
	 * @throws IllegalArgumentException 如果 kernelData 为 null 或空时抛出
	 * @since 2.1.0
	 */
	public static Mat getKernel(final float[] kernelData) {
		Validate.isTrue(ArrayUtils.isNotEmpty(kernelData), "kernelData 不可为空");

		Mat kernel = new Mat(3, 3, opencv_core.CV_32F);
		try (FloatIndexer idx = kernel.createIndexer()) {
			int pos = 0;
			for (int r = 0; r < 3; r++) {
				for (int c = 0; c < 3; c++) {
					idx.put(r, c, kernelData[pos++]);
				}
			}
		}
		return kernel;
	}

	/**
	 * 按目标宽度等比例缩放图像尺寸，保持宽高比
	 *
	 * @param size        原始图像尺寸，不能为 null
	 * @param targetWidth 目标宽度，必须大于 0
	 * @return 缩放后的尺寸对象
	 * @throws IllegalArgumentException 如果 size 为 null 或 targetWidth 小于等于 0 时抛出
	 * @since 2.1.0
	 */
	public static Size scaleByWidth(final Size size, final int targetWidth) {
		Validate.isTrue(targetWidth > 0, "targetWidth 必须大于 0");
		Validate.notNull(size, "size 不可为 null");

		int width = size.width();
		int height = size.height();

		if (width > height) {
			double ratio = (double) width / height;
			return new Size(targetWidth, Math.max((int) Math.round(targetWidth / ratio), 1));
		} else {
			double ratio = (double) height / width;
			return new Size(targetWidth, Math.max((int) Math.round(targetWidth * ratio), 1));
		}
	}

	/**
	 * 按目标高度等比例缩放图像尺寸，保持宽高比
	 *
	 * @param size         原始图像尺寸，不能为 null
	 * @param targetHeight 目标高度，必须大于 0
	 * @return 缩放后的尺寸对象
	 * @throws IllegalArgumentException 如果 size 为 null 或 targetHeight 小于等于 0 时抛出
	 * @since 2.1.0
	 */
	public static Size scaleByHeight(final Size size, final int targetHeight) {
		Validate.isTrue(targetHeight > 0, "targetHeight 必须大于 0");
		Validate.notNull(size, "size 不可为 null");

		int width = size.width();
		int height = size.height();

		if (width > height) {
			double ratio = (double) width / height;
			return new Size(Math.max((int) Math.round(targetHeight * ratio), 1), targetHeight);
		} else {
			double ratio = (double) height / width;
			return new Size(Math.max((int) Math.round(targetHeight / ratio), 1), targetHeight);
		}
	}

	/**
	 * 按比例因子缩放图像尺寸
	 *
	 * @param size          原始图像尺寸，不能为 null
	 * @param scalingFactor 缩放比例因子，必须大于 0
	 * @return 缩放后的尺寸对象
	 * @throws IllegalArgumentException 如果 size 为 null 或 scalingFactor 小于等于 0 时抛出
	 * @since 2.1.0
	 */
	public static Size scale(final Size size, final double scalingFactor) {
		Validate.isTrue(scalingFactor > 0, "scalingFactor 必须大于 0");
		Validate.notNull(size, "size 不可为 null");

		return new Size((int) Math.round(size.width() * scalingFactor),
			(int) Math.round(size.height() * scalingFactor));
	}

	/**
	 * 按目标宽度和高度缩放图像尺寸，保持宽高比且不超出目标尺寸
	 *
	 * <p>缩放后的图像将完全容纳在目标尺寸内，可能有一边小于目标尺寸</p>
	 *
	 * @param size         原始图像尺寸，不能为 null
	 * @param targetWidth  目标宽度，必须大于 0
	 * @param targetHeight 目标高度，必须大于 0
	 * @return 缩放后的尺寸对象
	 * @throws IllegalArgumentException 如果 size 为 null 或 targetWidth/targetHeight 小于等于 0 时抛出
	 * @since 2.1.0
	 */
	public static Size scale(final Size size, final int targetWidth, final int targetHeight) {
		Validate.isTrue(targetWidth > 0, "targetWidth 必须大于 0");
		Validate.isTrue(targetHeight > 0, "targetHeight 必须大于 0");
		Validate.notNull(size, "size 不可为 null");

		int width = size.width();
		int height = size.height();

		double ratio = (double) width / height;
		int heightByWidth = Math.max((int) Math.round(targetWidth / ratio), 1);
		if (heightByWidth <= targetHeight) {
			return new Size(targetWidth, heightByWidth);
		} else {
			int widthByHeight = Math.max((int) Math.round(targetHeight * ratio), 1);
			return new Size(widthByHeight, targetHeight);
		}
	}

	/**
	 * 创建 2D 平移变换矩阵
	 *
	 * <p>矩阵格式为 [[1, 0, dx], [0, 1, dy]]</p>
	 *
	 * @param dx X 轴平移量
	 * @param dy Y 轴平移量
	 * @return 2x3 的平移变换矩阵
	 * @since 2.1.0
	 */
	public static Mat getMatrixMat(final int dx, final int dy) {
		Mat matrixMat = new Mat(2, 3, opencv_core.CV_64F);
		try (DoubleIndexer indexer = matrixMat.createIndexer()) {
			// 第一行 [1, 0, dx]
			indexer.put(0, 0, 1.0);
			indexer.put(0, 1, 0.0);
			indexer.put(0, 2, dx);
			// 第二行 [0, 1, dy]
			indexer.put(1, 0, 0.0);
			indexer.put(1, 1, 1.0);
			indexer.put(1, 2, dy);
		}
		return matrixMat;
	}

	/**
	 * 清理图像的透明区域，解决白边和黑底问题
	 *
	 * <p>主要功能：
	 * <ul>
	 *   <li>将极低透明度的像素（Alpha &lt; 10）强制设为完全透明，解决白边问题</li>
	 *   <li>将完全透明区域（Alpha = 0）的 RGB 通道也强制设为 0，解决黑底问题</li>
	 * </ul>
	 *
	 * <p>此方法直接修改输入的图像，不会创建新图像</p>
	 * <p><b>注意：</b>如果图像不是 4 通道（不包含透明通道），则该方法不执行任何操作，直接返回</p>
	 *
	 * @param image 输入图像，不能为 null，建议使用 4 通道图像（BGRA/RGBA）
	 * @throws IllegalArgumentException 如果 image 为 null 或为空时抛出
	 * @since 2.1.0
	 */
	public static void cleanTransparency(final Mat image) {
		Validate.notNull(image, "image 不可为 null");
		Validate.isTrue(isNotEmpty(image), "image 不可为空");

		if (image.channels() != 4) {
			return;
		}

		MatVector channels = new MatVector(4);
		opencv_core.split(image, channels);
		Mat alpha = channels.get(3);

		// 1. 解决白边：将极低透明度的像素（如 < 10）强制设为完全透明
		// 这能清除 PS 导出时边缘残留的半透明白色像素
		Mat thresholdMask = new Mat();
		opencv_imgproc.threshold(alpha, thresholdMask, 10, 255, opencv_imgproc.THRESH_BINARY);

		// 将低于阈值的 Alpha 强制置 0
		Mat zeroMat = new Mat(new Scalar(0, 0, 0, 0));
		opencv_core.bitwise_and(alpha, thresholdMask, alpha); // 低于阈值的变0

		// 2. 解决黑底：确保完全透明的区域，其 RGB 通道也被强制置为 0
		// 避免半透明混合时透出黑色底色
		Mat rgbMask = new Mat();
		opencv_imgproc.threshold(alpha, rgbMask, 0, 255, opencv_imgproc.THRESH_BINARY); // 找出 Alpha > 0 的区域

		// 将 Alpha=0 的区域的 RGB 强制设为 0
		MatVector rgbChannels = new MatVector(channels.get(0), channels.get(1), channels.get(2));
		Mat blackRgb = new Mat(new Scalar(0, 0, 0, 0));

		// 这里使用简单的逐通道处理，确保透明区无残留颜色
		for (int i = 0; i < 3; i++) {
			opencv_core.bitwise_and(rgbChannels.get(i), rgbMask, rgbChannels.get(i));
		}

		MatVector rgbaChannels = new MatVector(channels.get(0), channels.get(1), channels.get(2), alpha);
		// 重新合并通道
		opencv_core.merge(rgbaChannels, image);

		// 释放临时矩阵
		for (Mat ch : channels.get()) {
			ch.release();
		}
		thresholdMask.release();
		zeroMat.release();
		rgbMask.release();
		blackRgb.release();
	}

	/**
	 * 根据 EXIF 方向值校正图像方向
	 * <p>根据 EXIF 方向值（1-8）对图像进行相应的旋转和翻转操作。</p>
	 *
	 * <p>EXIF 方向值说明：</p>
	 * <ul>
	 *   <li>1：正常方向，无需处理</li>
	 *   <li>2：水平翻转</li>
	 *   <li>3：旋转 180 度</li>
	 *   <li>4：垂直翻转</li>
	 *   <li>5：顺时针旋转 90 度后水平翻转</li>
	 *   <li>6：顺时针旋转 90 度</li>
	 *   <li>7：逆时针旋转 90 度后水平翻转</li>
	 *   <li>8：逆时针旋转 90 度</li>
	 * </ul>
	 *
	 * @param image           图像 Mat 对象，不能为 null
	 * @param exifOrientation EXIF 方向值（必须介于 1-8 之间）
	 * @return 校正后的图像 Mat 对象
	 * @throws IllegalArgumentException 如果 image 为 null 或 exifOrientation 不在 1-8 范围内时抛出
	 * @since 2.1.0
	 */
	public static Mat correctOrientation(final Mat image, final int exifOrientation) {
		Validate.notNull(image, "image 不可为 null");
		Validate.inclusiveBetween(1, 8, exifOrientation, "exifOrientation 必须介于1-8之间");

		Mat outputImage = image;
		switch (exifOrientation) {
			case 2:
				opencv_core.flip(image, outputImage, FlipDirection.HORIZONTAL.getCode());
				break;
			case 3:
				opencv_core.rotate(image, outputImage, RotateDirection.UPSIDE_DOWN.getCode());
				break;
			case 4:
				opencv_core.flip(image, outputImage, FlipDirection.VERTICAL.getCode());
				break;
			case 5:
				opencv_core.rotate(image, outputImage, RotateDirection.CLOCKWISE_90.getCode());

				Mat clockwise90Mat = new Mat();
				opencv_core.flip(outputImage, clockwise90Mat, FlipDirection.HORIZONTAL.getCode());

				outputImage = clockwise90Mat;
				break;
			case 6:
				opencv_core.rotate(image, outputImage, RotateDirection.CLOCKWISE_90.getCode());
				break;
			case 7:
				opencv_core.rotate(image, outputImage, RotateDirection.COUNTER_CLOCKWISE_90.getCode());

				Mat counterClockwise90Mat = new Mat();
				opencv_core.flip(outputImage, counterClockwise90Mat, FlipDirection.HORIZONTAL.getCode());

				outputImage = counterClockwise90Mat;
				break;
			case 8:
				opencv_core.rotate(image, outputImage, RotateDirection.COUNTER_CLOCKWISE_90.getCode());
				break;
			default:
				break;
		}
		return outputImage;
	}
}
