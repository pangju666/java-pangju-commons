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
import org.bytedeco.javacpp.indexer.UByteIndexer;
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
import java.io.*;

public class OpencvUtils {
	protected OpencvUtils() {}

	public static boolean canRead(final File file) throws IOException {
		Validate.isTrue(FileUtils.isImageType(file), "file 不是一个图像文件");

		return opencv_imgcodecs.haveImageReader(file.getAbsolutePath());
	}

	public static boolean canWrite(final String format) {
		Validate.notBlank(format, "format 不可为空");

		return opencv_imgcodecs.haveImageWriter(format.startsWith(FilenameUtils.EXTENSION_SEPARATOR_STR) ?
			StringUtils.EMPTY : FilenameUtils.EXTENSION_SEPARATOR + format);
	}

	public static Size getSize(final File file) throws IOException {
		Validate.isTrue(FileUtils.isImageType(file), "file 不是一个图像文件");

		try (Mat mat = opencv_imgcodecs.imread(file.getAbsolutePath())) {
			if (mat.isNull() || mat.empty()) {
				return null;
			}
			return mat.size();
		}
	}

	public static Mat read(final File file) throws IOException {
		return read(file, OpencvConstants.DEFAULT_IMAGE_COLOR_TYPE);
	}

	public static Mat read(final File file, final int flags) throws IOException {
		Validate.isTrue(FileUtils.isImageType(file), "file 不是一个图像文件");

		return opencv_imgcodecs.imread(file.getAbsolutePath(), flags);
	}

	public static Mat read(final InputStream inputStream) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		return read(inputStream.readAllBytes(), OpencvConstants.DEFAULT_IMAGE_COLOR_TYPE);
	}

	public static Mat read(final InputStream inputStream, final int flags) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		return read(inputStream.readAllBytes(), flags);
	}

	public static Mat read(final byte[] bytes) {
		return read(bytes, OpencvConstants.DEFAULT_IMAGE_COLOR_TYPE);
	}

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

	public static Mat read(final BufferedImage image) {
		return read(image, opencv_core.CV_8UC4);
	}

	public static Mat read(final BufferedImage image, final int type) {
		Mat mat = new Mat(image.getWidth(), image.getHeight(), type);
		byte[] data = ((DataBufferByte) image.getData().getDataBuffer()).getData();
		mat.data().put(data);
		return mat;
	}

	public static Scalar toBGRAColor(final Color color) {
		Validate.notNull(color, "color 不可为 null");

		return new Scalar(color.getBlue(), color.getGreen(), color.getRed(), color.getAlpha());
	}

	public static Scalar toBGRAColor(final String colorStr) {
		Validate.notBlank(colorStr, "colorStr 不可为空");

		return toBGRAColor(Color.decode(colorStr));
	}

	public static Scalar toBGRColor(final Color color) {
		Validate.notNull(color, "color 不可为 null");

		return new Scalar(color.getBlue(), color.getGreen(), color.getRed(), 255);
	}

	public static Scalar toBGRColor(final String colorStr) {
		Validate.notBlank(colorStr, "colorStr 不可为空");

		return toBGRColor(Color.decode(colorStr));
	}

	public static Scalar toRGBAColor(final Color color) {
		Validate.notNull(color, "color 不可为 null");

		return new Scalar(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}

	public static Scalar toRGBAColor(final String colorStr) {
		Validate.notBlank(colorStr, "colorStr 不可为空");

		return toRGBAColor(Color.decode(colorStr));
	}

	public static Scalar toRGBColor(final Color color) {
		Validate.notNull(color, "color 不可为 null");

		return new Scalar(color.getRed(), color.getGreen(), color.getBlue(), 255);
	}

	public static Scalar toRGBColor(final String colorStr) {
		Validate.notBlank(colorStr, "colorStr 不可为空");

		return toRGBColor(Color.decode(colorStr));
	}

	public static Mat getKernel(final float[] kernelData) {
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

	public static Size scaleByWidth(final Size size, final int targetWidth) {
		Validate.isTrue(targetWidth > 0, "targetWidth 必须大于 0");

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

	public static Size scaleByHeight(final Size size, final int targetHeight) {
		Validate.isTrue(targetHeight > 0, "targetHeight 必须大于 0");

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

	public static Size scale(final Size size, final double scalingFactor) {
		Validate.isTrue(scalingFactor > 0, "scalingFactor 必须大于 0");

		return new Size((int) Math.round(size.width() * scalingFactor),
			(int) Math.round(size.height() * scalingFactor));
	}

	public static Size scale(final Size size, final int targetWidth, final int targetHeight) {
		Validate.isTrue(targetWidth > 0, "targetWidth 必须大于 0");
		Validate.isTrue(targetHeight > 0, "targetHeight 必须大于 0");

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

	public static Mat transparency(final Mat image, final float alpha) {
		Validate.isTrue(alpha >= 0 && alpha <= 1, "alpha 必须大于等于 0 且小于等于 1");

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
}
