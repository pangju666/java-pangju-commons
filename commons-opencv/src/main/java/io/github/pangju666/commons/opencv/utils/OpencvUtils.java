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

package io.github.pangju666.commons.opencv.utils;

import io.github.pangju666.commons.io.lang.IOConstants;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.FilenameUtils;
import io.github.pangju666.commons.opencv.lang.OpencvConstants;
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
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * OpenCV 工具类
 *
 * <p>提供 OpenCV 图像处理的常用工具方法，包括图像读写、颜色转换、尺寸缩放、亮度对比度调整、透明度处理等功能。</p>
 *
 * <h2>主要功能</h2>
 * <ul>
 *   <li><strong>图像读写</strong>：支持从文件、输入流、字节数组、BufferedImage 读取图像</li>
 *   <li><strong>颜色转换</strong>：支持 AWT Color 与 OpenCV Scalar 之间的 BGR/RGBA 格式转换</li>
 *   <li><strong>尺寸缩放</strong>：支持按宽度、高度、比例、目标尺寸等多种缩放方式</li>
 *   <li><strong>图像增强</strong>：支持亮度对比度调整、透明度设置、透明区域清理</li>
 *   <li><strong>工具方法</strong>：支持矩阵创建、卷积核创建等</li>
 * </ul>
 *
 * @author pangju666
 * @see OpencvConstants
 * @since 1.1.0
 */
public class OpencvUtils {
	/**
	 * 私有构造函数，防止实例化
	 *
	 * @since 1.1.0
	 */
	protected OpencvUtils() {
	}

	/**
	 * 检查 OpenCV 是否可以读取指定格式的图像文件
	 *
	 * @param file 图像文件，不能为 null 且必须是图像文件
	 * @return 如果可以读取返回 true，否则返回 false
	 * @throws IOException              如果文件操作失败时抛出
	 * @throws IllegalArgumentException 如果 file 不是图像文件时抛出
	 * @since 1.1.0
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
	 * @since 1.1.0
	 */
	public static boolean canWrite(final String format) {
		Validate.notBlank(format, "format 不可为空");

		return opencv_imgcodecs.haveImageWriter(format.toLowerCase().startsWith(
			FilenameUtils.EXTENSION_SEPARATOR_STR) ? StringUtils.EMPTY : FilenameUtils.EXTENSION_SEPARATOR + format);
	}

	/**
	 * 获取图像文件的尺寸（宽度和高度）
	 *
	 * @param file 图像文件，不能为 null 且必须是图像文件
	 * @return 图像尺寸对象，如果读取失败返回 null
	 * @throws IOException              如果文件操作失败时抛出
	 * @throws IllegalArgumentException 如果 file 不是图像文件时抛出
	 * @since 1.1.0
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
	 * @since 1.1.0
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
	 * @since 1.1.0
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
	 * @since 1.1.0
	 */
	public static Mat read(final InputStream inputStream) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		return read(inputStream.readAllBytes(), OpencvConstants.DEFAULT_IMAGE_COLOR_TYPE);
	}

	/**
	 * 从输入流读取图像，使用指定的颜色类型
	 *
	 * @param inputStream 输入流，不能为 null
	 * @param flags       读取标志（如 IMREAD_COLOR、IMREAD_GRAYSCALE 等）
	 * @return 图像 Mat 对象
	 * @throws IOException              如果流读取失败时抛出
	 * @throws IllegalArgumentException 如果 inputStream 为 null 时抛出
	 * @since 1.1.0
	 */
	public static Mat read(final InputStream inputStream, final int flags) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		return read(inputStream.readAllBytes(), flags);
	}

	/**
	 * 从字节数组读取图像，使用 BGR 颜色类型
	 *
	 * @param bytes 图像字节数组，不能为 null 或空
	 * @return 图像 Mat 对象
	 * @throws IllegalArgumentException 如果 bytes 为 null 或空，或者不是图像数据时抛出
	 * @since 1.1.0
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
	 * @since 1.1.0
	 */
	public static Mat read(final byte[] bytes, final int flags) {
		Validate.isTrue(ArrayUtils.isNotEmpty(bytes), "bytes 不可为空");

		String mimeType = IOConstants.getDefaultTika().detect(bytes);
		Validate.isTrue(Strings.CS.startsWith(mimeType, IOConstants.IMAGE_MIME_TYPE_PREFIX),
			"file 不是一个图像文件");

		try (BytePointer bytePointer = new BytePointer(bytes);
		     Mat bufMat = new Mat(bytePointer)) {
			return opencv_imgcodecs.imdecode(bufMat, flags);
		}
	}

	/**
	 * 从 BufferedImage 读取图像，使用默认的 4 通道类型
	 *
	 * @param image BufferedImage 对象，不能为 null
	 * @return 图像 Mat 对象
	 * @throws IllegalArgumentException 如果 image 为 null 时抛出
	 * @since 1.1.0
	 */
	public static Mat read(final BufferedImage image) {
		Validate.notNull(image, "image 不可为 null");

		Mat mat = new Mat(image.getWidth(), image.getHeight(), opencv_core.CV_8UC4);
		byte[] data = ((DataBufferByte) image.getData().getDataBuffer()).getData();
		mat.data().put(data);
		return mat;
	}

	/**
	 * 将 AWT Color 转换为 OpenCV BGRA 格式的 Scalar
	 *
	 * @param color AWT 颜色对象，不能为 null
	 * @return BGRA 格式的 Scalar 对象（蓝、绿、红、Alpha）
	 * @throws IllegalArgumentException 如果 color 为 null 时抛出
	 * @since 1.1.0
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
	 * @since 1.1.0
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
	 * @since 1.1.0
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
	 * @since 1.1.0
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
	 * @since 1.1.0
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
	 * @since 1.1.0
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
	 * @since 1.1.0
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
	 * @since 1.1.0
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
	 * @since 1.1.0
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
	 * @since 1.1.0
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
	 * @since 1.1.0
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
	 * @since 1.1.0
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
	 * @since 1.1.0
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
	 * @since 1.1.0
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
	 * 调整图像的亮度和对比度
	 *
	 * <p>使用公式：g(i,j) = alpha * f(i,j) + beta</p>
	 * <p>alpha 控制对比度（> 1 增加对比度，&lt; 1 减小对比度）</p>
	 * <p>beta 控制亮度（正值增加亮度，负值减小亮度）</p>
	 *
	 * @param image 输入图像，不能为 null
	 * @param alpha 对比度增益因子，必须大于 0
	 * @param beta  亮度偏移值
	 * @return 调整后的图像
	 * @throws IllegalArgumentException 如果 image 为 null 或 alpha 小于等于 0 时抛出
	 * @since 1.1.0
	 */
	public static Mat adjustBrightnessContrast(final Mat image, final float alpha, final float beta) {
		Validate.isTrue(alpha > 0, "alpha 必须大于 0");
		Validate.notNull(image, "image 不可为 null");

		Mat outputImage = new Mat();

		try (Mat thresholdImage = new Mat()) {
			Mat alphaMat = new Mat(Scalar.all(alpha));
			opencv_core.multiply(image, alphaMat, thresholdImage);
			alphaMat.releaseReference();

			Mat betaMat = new Mat(Scalar.all(beta));
			opencv_core.add(thresholdImage, betaMat, thresholdImage);
			betaMat.releaseReference();

			opencv_imgproc.threshold(thresholdImage, thresholdImage, 0, 255, opencv_imgproc.THRESH_TRUNC);

			Mat subtractMat = new Mat(Scalar.all(0));
			opencv_core.subtract(thresholdImage, subtractMat, outputImage);
			subtractMat.releaseReference();

			return outputImage;
		}
	}

	/**
	 * 设置图像的全局透明度
	 *
	 * <p>如果图像没有 Alpha 通道，会自动添加</p>
	 *
	 * @param image 输入图像，不能为 null
	 * @param alpha 透明度值，范围 0.0（完全透明）- 1.0（完全不透明）
	 * @return 设置透明度后的图像（可能是新创建的 Mat）
	 * @throws IllegalArgumentException 如果 image 为 null 或 alpha 超出 [0, 1] 范围时抛出
	 * @since 1.1.0
	 */
	public static Mat transparency(final Mat image, final float alpha) {
		Validate.isTrue(alpha >= 0 && alpha <= 1, "alpha 必须大于等于 0 且小于等于 1");
		Validate.notNull(image, "image 不可为 null");

		Mat outputImage = image;

		if (outputImage.channels() < 4) {
			Mat bgraMat = new Mat();
			int type = image.type();
			int code = opencv_imgproc.COLOR_BGR2BGRA;
			if (type == opencv_core.CV_8UC1 || type == opencv_core.CV_16UC1 || type == opencv_core.CV_32FC1) {
				code = opencv_imgproc.COLOR_GRAY2BGRA;
			}
			opencv_imgproc.cvtColor(outputImage, bgraMat, code);
			image.releaseReference();
			outputImage = bgraMat;
		}

		try (MatVector channels = new MatVector();
		     Scalar alphaScalar = new Scalar(Math.floor(alpha * 255))) {
			opencv_core.split(outputImage, channels);

			try (Mat alphaChannel = channels.get(3)) {
				alphaChannel.put(alphaScalar);
				opencv_core.merge(channels, outputImage);
			}
		}

		return outputImage;
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
	 *
	 * @param image 输入图像，不能为 null，必须是 4 通道图像（BGRA/RGBA）
	 * @throws IllegalArgumentException 如果 image 为 null 时抛出
	 * @since 1.1.0
	 */
	public static void cleanTransparency(final Mat image) {
		Validate.notNull(image, "image 不可为 null");

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
}
