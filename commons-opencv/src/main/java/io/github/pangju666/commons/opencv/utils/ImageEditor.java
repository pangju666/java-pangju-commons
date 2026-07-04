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

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.github.jaiimageio.impl.common.ImageUtil;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.FilenameUtils;
import io.github.pangju666.commons.io.utils.IOUtils;
import io.github.pangju666.commons.opencv.enums.FlipDirection;
import io.github.pangju666.commons.opencv.enums.RotateDirection;
import io.github.pangju666.commons.opencv.lang.OpencvConstants;
import io.github.pangju666.commons.opencv.model.ImageWatermarkOption;
import io.github.pangju666.commons.opencv.model.TextWatermarkOption;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.bytedeco.javacpp.indexer.DoubleIndexer;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_core.Point;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ImageEditor {
	protected static final int NORMAL_EXIF_ORIENTATION = 1;

	protected static final Scalar TRANSPARENT_COLOR = new Scalar(0, 0, 0, 0);

	protected Mat inputImage;

	protected Mat outputImage;

	protected ImageEditor(final Mat inputImage, int exifOrientation, int flags) {
		Validate.notNull(inputImage, "inputImage 不可为 null");

		this.inputImage = inputImage;
		Validate.isTrue(!inputImage.isNull() && !inputImage.empty(), "inputImage 不存在图像数据");

		this.outputImage = inputImage;

		if (flags == opencv_imgcodecs.IMREAD_UNCHANGED || flags == opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION) {
			switch (exifOrientation) {
				case 2:
					flip(FlipDirection.HORIZONTAL);
					break;
				case 3:
					rotate(RotateDirection.UPSIDE_DOWN);
					break;
				case 4:
					flip(FlipDirection.VERTICAL);
					break;
				case 5:
					rotate(RotateDirection.CLOCKWISE_90);
					flip(FlipDirection.HORIZONTAL);
					break;
				case 6:
					rotate(RotateDirection.CLOCKWISE_90);
					break;
				case 7:
					rotate(RotateDirection.COUNTER_CLOCKWISE_90);
					flip(FlipDirection.HORIZONTAL);
					break;
				case 8:
					rotate(RotateDirection.COUNTER_CLOCKWISE_90);
					break;
				default:
					break;
			}

			this.inputImage = this.outputImage;
		}
	}

	public static ImageEditor of(final File file) throws IOException {
		return of(file, OpencvConstants.DEFAULT_IMAGE_COLOR_TYPE);
	}

	public static ImageEditor of(final File file, final int flags) throws IOException {
		int exifOrientation = NORMAL_EXIF_ORIENTATION;
		Mat mat = OpencvUtils.read(file, flags);

		if (flags == opencv_imgcodecs.IMREAD_UNCHANGED || flags == opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION) {
			try {
				Metadata metadata = ImageMetadataReader.readMetadata(file);
				exifOrientation = getExifOrientation(metadata);
			} catch (ImageProcessingException ignored) {
			}
		}

		return new ImageEditor(mat, exifOrientation, flags);
	}

	public static ImageEditor of(final InputStream inputStream) throws IOException {
		return of(inputStream.readAllBytes(), OpencvConstants.DEFAULT_IMAGE_COLOR_TYPE);
	}

	public static ImageEditor of(final InputStream inputStream, final int flags) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		return of(inputStream.readAllBytes(), flags);
	}

	public static ImageEditor of(final byte[] bytes) throws IOException {
		return of(bytes, OpencvConstants.DEFAULT_IMAGE_COLOR_TYPE);
	}

	public static ImageEditor of(final byte[] bytes, final int flags) throws IOException {
		int exifOrientation = NORMAL_EXIF_ORIENTATION;
		Mat mat = OpencvUtils.read(bytes, flags);

		if (flags == opencv_imgcodecs.IMREAD_UNCHANGED || flags == opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION) {
			try {
				Metadata metadata = ImageMetadataReader.readMetadata(IOUtils.toUnsynchronizedByteArrayInputStream(bytes));
				exifOrientation = getExifOrientation(metadata);
			} catch (ImageProcessingException ignored) {
			}
		}

		return new ImageEditor(mat, exifOrientation, flags);
	}

	public ImageEditor transparency(final float alpha) {
		Validate.isTrue(alpha >= 0 && alpha <= 1, "alpha 必须大于等于 0 且小于等于 1");

		Mat image = OpencvUtils.transparency(this.outputImage, alpha);

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	public ImageEditor rotate(final RotateDirection direction) {
		Validate.notNull(direction, "direction 不可为 null");

		Mat image = new Mat();
		opencv_core.rotate(outputImage, image, direction.getCode());

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	public ImageEditor rotate(final double angle) {
		Size imageSize = outputImage.size();
		int width = imageSize.width();
		int height = imageSize.height();
		Point2f center = new Point2f((float) (width / 2.0), (float) (height / 2.0));
		Mat rotateMat = opencv_imgproc.getRotationMatrix2D(center, angle, 1.0);

		// 计算旋转后的画布宽高，避免图像被裁切
		double cos, sin;
		try (DoubleIndexer indexer = rotateMat.createIndexer()) {
			cos = Math.abs(indexer.get(0, 0));
			sin = Math.abs(indexer.get(0, 1));
		}

		int newWidth = (int) (height * sin + width * cos);
		int newHeight = (int) (height * cos + width * sin);

		// 偏移修正矩阵，保证内容居中
		try (DoubleIndexer indexer = rotateMat.createIndexer()) {
			indexer.put(0, 2, indexer.get(0, 2) + (newWidth / 2.0 - center.x()));
			indexer.put(1, 2, indexer.get(1, 2) + (newHeight / 2.0 - center.y()));
		}

		Mat newImage = new Mat(new Size(newWidth, newHeight), outputImage.type());
		Size newImageSize = new Size(newWidth, newHeight);
		// 仿射旋转，边界填充背景色
		opencv_imgproc.warpAffine(outputImage, newImage, rotateMat, newImageSize,
			opencv_imgproc.INTER_LINEAR, opencv_core.BORDER_CONSTANT, TRANSPARENT_COLOR);

		this.outputImage.releaseReference();
		this.outputImage = newImage;

		return this;
	}

	public ImageEditor flip(final FlipDirection direction) {
		Validate.notNull(direction, "direction 不可为 null");

		Mat image = new Mat();
		opencv_core.flip(outputImage, image, direction.getCode());

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	public ImageEditor warpAffine(final int dx, final int dy) {
		Mat image = new Mat();

		Mat matrixMat = OpencvUtils.getMatrixMat(dx, dy);
		opencv_imgproc.warpAffine(outputImage, image, matrixMat, outputImage.size(),
			opencv_imgproc.INTER_LINEAR, opencv_core.BORDER_CONSTANT, TRANSPARENT_COLOR);
		matrixMat.releaseReference();

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	public ImageEditor resize(final int width, final int height) {
		Validate.isTrue(width > 0, "width 必须大于 0");
		Validate.isTrue(height > 0, "height 必须大于 0");

		Mat image = new Mat();
		opencv_imgproc.resize(outputImage, image, new Size(width, height));

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	public ImageEditor scaleByWidth(final int targetWidth) {
		Validate.isTrue(targetWidth > 0, "targetWidth 必须大于 0");

		Size size = OpencvUtils.scaleByWidth(outputImage.size(), targetWidth);
		return resize(size.width(), size.height());
	}

	public ImageEditor scaleByHeight(final int targetHeight) {
		Validate.isTrue(targetHeight > 0, "targetHeight 必须大于 0");

		Size size = OpencvUtils.scaleByHeight(outputImage.size(), targetHeight);
		return resize(size.width(), size.height());
	}

	public ImageEditor scale(final double scalingFactor) {
		Validate.isTrue(scalingFactor > 0, "scalingFactor 必须大于 0");

		Size size = OpencvUtils.scale(outputImage.size(), scalingFactor);
		return resize(size.width(), size.height());
	}

	public ImageEditor scale(final int targetWidth, final int targetHeight) {
		Validate.isTrue(targetWidth > 0, "targetWidth 必须大于 0");
		Validate.isTrue(targetHeight > 0, "targetHeight 必须大于 0");

		Size size = OpencvUtils.scale(outputImage.size(), targetWidth, targetHeight);
		return resize(size.width(), size.height());
	}

	public ImageEditor cropByCenter(int width, int height) {
		Validate.isTrue(width > 0, "width 不能小于0");
		Validate.isTrue(height > 0, "height 不能小于0");

		Size imageSize = outputImage.size();
		// 边界检测
		if (width > imageSize.width() || height > imageSize.height()) {
			return this;
		}

		Rect rect = new Rect((imageSize.width() - width) / 2, (imageSize.height() - height) / 2, width, height);
		Mat image = outputImage.apply(rect).clone();

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	public ImageEditor cropByOffset(int topOffset, int bottomOffset, int leftOffset, int rightOffset) {
		Validate.isTrue(topOffset >= 0 && bottomOffset >= 0 && leftOffset >= 0 && rightOffset >= 0,
			"offset 不能小于0");

		Size imageSize = outputImage.size();
		// 边界检测
		if (leftOffset + rightOffset > imageSize.width() ||
			topOffset + bottomOffset > imageSize.height()) {
			return this;
		}

		Rect rect = new Rect(leftOffset, topOffset, imageSize.width() - leftOffset - rightOffset,
			imageSize.height() - topOffset - bottomOffset);
		Mat image = outputImage.apply(rect).clone();

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	public ImageEditor cropByRect(int x, int y, int width, int height) {
		Validate.isTrue(x >= 0, "x 不能小于0");
		Validate.isTrue(y >= 0, "y 不能小于0");
		Validate.isTrue(width > 0, "width 不能小于0");
		Validate.isTrue(height > 0, "height 不能小于0");

		Size imageSize = outputImage.size();
		// 边界检测
		if (x + width > imageSize.width() || y + height > imageSize.height()) {
			return this;
		}

		Rect rect = new Rect(x, y, width, height);
		Mat image = outputImage.apply(rect).clone();

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	public ImageEditor grayscale() {
		Mat image = new Mat();
		int channels = this.outputImage.channels();

		if (channels == 3) {
			opencv_imgproc.cvtColor(this.outputImage, image, opencv_imgproc.COLOR_BGR2GRAY);
		} else if (channels == 4) {
			opencv_imgproc.cvtColor(this.outputImage, image, opencv_imgproc.COLOR_BGRA2GRAY);
		}

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	public ImageEditor blur() {
		return blur(new Size(5, 5));
	}

	public ImageEditor blur(final Size ksize) {
		Validate.notNull(ksize, "ksize 不可为 null");

		Mat image = new Mat();

		opencv_imgproc.blur(this.outputImage, image, ksize);

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	public ImageEditor gaussianBlur() {
		return gaussianBlur(new Size(5, 5), 0);
	}

	public ImageEditor gaussianBlur(final Size ksize) {
		return gaussianBlur(ksize, 0);
	}

	public ImageEditor gaussianBlur(final Size ksize, final double sigmaX) {
		Validate.notNull(ksize, "ksize 不可为 null");
		Validate.isTrue(sigmaX >= 0, "sigmaX 必须大于等于 0");

		Mat image = new Mat();

		opencv_imgproc.GaussianBlur(this.outputImage, image, ksize, sigmaX);

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	public ImageEditor medianBlur() {
		return medianBlur(5);
	}

	public ImageEditor medianBlur(final int ksize) {
		Validate.isTrue(ksize > 1, "ksize 必须大于 1");
		Validate.isTrue(ksize % 2 != 0, "ksize 必须为奇数");

		Mat image = new Mat();

		opencv_imgproc.medianBlur(this.outputImage, image, ksize);

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	public ImageEditor sharpen() {
		return sharpen(5);
	}

	public ImageEditor sharpen(final float weight) {
		Validate.isTrue(weight > 4, "weight 必须大于4");

		float[] kernelData = {
			0, -1, 0,
			-1, weight, -1,
			0, -1, 0
		};
		Mat kernel = OpencvUtils.getKernel(kernelData);

		Mat image = new Mat();

		opencv_imgproc.filter2D(this.outputImage, image, -1, kernel);

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	public ImageEditor emboss() {
		return emboss(1.0f);
	}

	public ImageEditor emboss(final float strength) {
		Validate.isTrue(strength > 0, "strength 必须大于0");

		float[] kernelData = {
			-2 * strength, -1 * strength, 0,
			-1 * strength, 1, 1 * strength,
			0, 1 * strength, 2 * strength
		};
		Mat kernel = OpencvUtils.getKernel(kernelData);

		Mat image = new Mat();

		opencv_imgproc.filter2D(this.outputImage, image, -1, kernel);

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	public ImageEditor threshold() {
		return threshold(0, 255, opencv_imgproc.THRESH_BINARY + opencv_imgproc.THRESH_OTSU);
	}

	public ImageEditor threshold(final double thresh, final double maxVal, final int type) {
		Validate.isTrue(thresh >= 0 && thresh <= 255, "thresh 取值范围必须为 0~255");
		Validate.isTrue(maxVal >= 0 && maxVal <= 255, "maxVal 取值范围必须为 0~255");
		Validate.isTrue(thresh != maxVal, "thresh 不能与 maxVal 相同");

		Mat image = new Mat();

		opencv_imgproc.threshold(this.outputImage, image, thresh, maxVal, type);

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	public ImageEditor adaptiveThreshold() {
		return adaptiveThreshold(255, opencv_imgproc.ADAPTIVE_THRESH_MEAN_C,
			opencv_imgproc.THRESH_BINARY, 11, 2);
	}

	public ImageEditor adaptiveThreshold(final double maxValue, final int adaptiveMethod, final int thresholdType,
										 final int blockSize, final double c) {
		Validate.isTrue(maxValue >= 0 && maxValue <= 255, "maxValue 取值范围必须 0 ~ 255");
		Validate.isTrue(blockSize >= 3 && blockSize % 2 == 1, "blockSize 必须是大于等于3的奇数");

		Mat image = new Mat();

		opencv_imgproc.adaptiveThreshold(this.outputImage, image, maxValue, adaptiveMethod, thresholdType,
			blockSize, c);

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	public ImageEditor contrast() {
		return contrast(0.3f);
	}

	public ImageEditor contrast(final float alpha) {
		Mat image = OpencvUtils.adjustBrightnessContrast(this.outputImage, alpha, 0);

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	public ImageEditor brightness(final float beta) {
		Mat image = OpencvUtils.adjustBrightnessContrast(this.outputImage, 1f, beta);

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	public ImageEditor addImageWatermark(final File watermarkImage, final ImageWatermarkOption option) throws IOException {
		Validate.notNull(option, "option 不可为 null");

		return addImageWatermark(OpencvUtils.read(watermarkImage), option);
	}

	public ImageEditor addImageWatermark(final Mat watermarkImage, final ImageWatermarkOption option) {
		Validate.notNull(option, "option 不可为 null");
		Validate.notNull(watermarkImage, "watermarkImage 不可为 null");

		Size outputImageSize = outputImage.size();
		Size originalWatermarkSize = watermarkImage.size();
		Pair<Size, Size> watermarkImageSizeRange = option.getSizeLimitStrategy().apply(outputImageSize);

		Mat targetWatermarkImage;
		Size targetWatermarkImageSize = OpencvUtils.scale(outputImageSize, option.getRelativeScaleFactor());

		if (originalWatermarkSize.width() > originalWatermarkSize.height()) {
			int targetWidth = Math.min(watermarkImageSizeRange.getRight().width(),
				Math.max(watermarkImageSizeRange.getLeft().width(), targetWatermarkImageSize.width()));
			if (targetWidth != targetWatermarkImageSize.width()) {
				targetWatermarkImage = new Mat();
				targetWatermarkImageSize = OpencvUtils.scaleByWidth(originalWatermarkSize, targetWidth);

				opencv_imgproc.resize(watermarkImage, targetWatermarkImage, targetWatermarkImageSize);
			} else {
				targetWatermarkImage = watermarkImage;
			}
		} else {
			int targetHeight = Math.min(watermarkImageSizeRange.getRight().height(),
				Math.max(watermarkImageSizeRange.getLeft().height(), targetWatermarkImageSize.height()));
			if (targetHeight != targetWatermarkImageSize.height()) {
				targetWatermarkImage = new Mat();
				targetWatermarkImageSize = OpencvUtils.scaleByHeight(originalWatermarkSize, targetHeight);

				opencv_imgproc.resize(watermarkImage, targetWatermarkImage, targetWatermarkImageSize);
			} else {
				targetWatermarkImage = watermarkImage;
			}
		}

		Rect roiRect;
		if (Objects.nonNull(option.getDirection())) {
			roiRect = option.getDirection().toImageWatermarkRect(outputImageSize, targetWatermarkImageSize, option.getMargin());
		} else {
			roiRect = new Rect(option.getX() + option.getMargin(), option.getY() + option.getMargin(),
				targetWatermarkImageSize.width(), targetWatermarkImageSize.height());
		}

		Mat newOutputImage = outputImage.clone();
		Mat roiMat = newOutputImage.apply(roiRect);

		if (option.getOpacity() < 1) {
			Mat transparencyWatarmarkImageMat = OpencvUtils.transparency(targetWatermarkImage, option.getOpacity());
			Mat mixRoi = new Mat();
			opencv_core.addWeighted(roiMat, 1.0, transparencyWatarmarkImageMat, option.getOpacity(),
				0, mixRoi);
			mixRoi.copyTo(roiMat);

			transparencyWatarmarkImageMat.releaseReference();
			mixRoi.releaseReference();
		} else {
			opencv_core.addWeighted(roiMat, 1, targetWatermarkImage, 1, 0, roiMat);
		}

		if (targetWatermarkImage != watermarkImage) {
			targetWatermarkImage.releaseReference();
		}
		roiMat.releaseReference();

		this.outputImage.releaseReference();
		this.outputImage = newOutputImage;

		return this;
	}

	/* 不支持中文水印 */
	public ImageEditor addTextWatermark(final String watermarkText, final TextWatermarkOption option) {
		Validate.notNull(option, "option 不可为 null");
		Validate.notBlank(watermarkText, "watermarkText 不可为空");

		Mat image = new Mat();
		Size imageSize = outputImage.size();

		Scalar fillColor = OpencvUtils.toBGRColor(option.getFillColor());
		Scalar strokeColor = OpencvUtils.toBGRColor(option.getStrokeColor());
		double fontScale = option.getFontScaleStrategy().applyAsDouble(outputImage.size(), option.getFontFace());

		Size textSize = opencv_imgproc.getTextSize(watermarkText, option.getFontFace(), fontScale,
			option.getThickness(), (IntBuffer) null);
		int textW = textSize.width();
		int textH = textSize.height();

		Point point = new Point(option.getX() + option.getMargin(), option.getY() + option.getMargin() + textH);

		if (Objects.nonNull(option.getDirection())) {
			point = switch (option.getDirection()) {
				case TOP -> new Point((imageSize.width() - textW) / 2, textH + option.getMargin());
				case TOP_LEFT -> new Point(option.getMargin(), textH + option.getMargin());
				case TOP_RIGHT -> new Point(imageSize.width() - textW - option.getMargin(), textH + option.getMargin());
				case BOTTOM -> new Point((imageSize.width() - textW) / 2, imageSize.height() - option.getMargin());
				case BOTTOM_LEFT -> new Point(option.getMargin(), imageSize.height() - option.getMargin());
				case BOTTOM_RIGHT -> new Point(imageSize.width() - textW - option.getMargin(),
					imageSize.height() - option.getMargin());
				case CENTER -> new Point((imageSize.width() - textW) / 2, (imageSize.height() + textH) / 2);
				default -> point;
			};
		}

		if (option.getOpacity() < 1) {
			Mat textLayer = new Mat();
			textLayer.put(Mat.zeros(imageSize, outputImage.type()));

			if (option.isStroke()) {
				opencv_imgproc.putText(textLayer, watermarkText, point, opencv_imgproc.FONT_HERSHEY_SIMPLEX,
					fontScale, strokeColor, option.getThickness() + option.getStrokeSize(),
					opencv_imgproc.LINE_AA, false);
			}
			opencv_imgproc.putText(textLayer, watermarkText, point, opencv_imgproc.FONT_HERSHEY_SIMPLEX,
				fontScale, fillColor, option.getThickness(), opencv_imgproc.LINE_AA, false);

			opencv_core.addWeighted(outputImage, 1, textLayer, option.getOpacity(), 0, image);

			textLayer.releaseReference();
		} else {
			image = outputImage.clone();

			if (option.isStroke()) {
				opencv_imgproc.putText(image, watermarkText, point, opencv_imgproc.FONT_HERSHEY_SIMPLEX,
					fontScale, strokeColor, option.getThickness() + option.getStrokeSize(),
					opencv_imgproc.LINE_AA, false);
			}
			opencv_imgproc.putText(image, watermarkText, point, opencv_imgproc.FONT_HERSHEY_SIMPLEX,
				fontScale, fillColor, option.getThickness(), opencv_imgproc.LINE_AA, false);
		}

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	public ImageEditor apply(final Function<Mat, Mat> operation) {
		Validate.notNull(operation, "operation 不可为 null");

		Mat image = operation.apply(this.outputImage);

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	public boolean toFile(final File outputFile) {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		boolean result = opencv_imgcodecs.imwrite(outputFile.getAbsolutePath(), outputImage);
		release();
		return result;
	}

	public boolean toFile(final File outputFile, int... params) {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notNull(params, "params 不可为 null");

		boolean result = opencv_imgcodecs.imwrite(outputFile.getAbsolutePath(), outputImage, params);
		release();
		return result;
	}

	public boolean toOutputStream(final String format, final OutputStream outputStream) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		byte[] bytes = toBytes(format);
		if (Objects.isNull(bytes)) {
			return false;
		}
		outputStream.write(bytes);
		return true;
	}

	public boolean toOutputStream(final String format, final OutputStream outputStream, int... params) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		byte[] bytes = toBytes(format, params);
		if (Objects.isNull(bytes)) {
			return false;
		}
		outputStream.write(bytes);
		return true;
	}

	public byte[] toBytes(final String format) {
		Validate.notBlank(format, "format 不可为空");
		Validate.isTrue(OpencvConstants.SUPPORTED_IMAGE_FILE_FORMATS.contains(format),
			"不支持输出 " + format + " 图像格式");

		byte[] bytes = new byte[(int) (outputImage.rows() * outputImage.step())];
		boolean result = opencv_imgcodecs.imencode(format.startsWith(FilenameUtils.EXTENSION_SEPARATOR_STR) ?
			StringUtils.EMPTY : FilenameUtils.EXTENSION_SEPARATOR + format, outputImage, bytes);

		release();

		if (!result) {
			return null;
		}
		return bytes;
	}

	public byte[] toBytes(final String format, final int... params) {
		Validate.notNull(params, "params 不可为 null");
		Validate.notBlank(format, "format 不可为空");
		Validate.isTrue(OpencvConstants.SUPPORTED_IMAGE_FILE_FORMATS.contains(format),
			"不支持输出 " + format + " 图像格式");

		byte[] bytes = new byte[(int) (outputImage.rows() * outputImage.step())];
		boolean result = opencv_imgcodecs.imencode(format.startsWith(FilenameUtils.EXTENSION_SEPARATOR_STR) ?
			StringUtils.EMPTY : FilenameUtils.EXTENSION_SEPARATOR + format, outputImage, bytes, params);

		release();

		if (!result) {
			return null;
		}
		return bytes;
	}

	public Mat toMat() {
		if (this.outputImage != this.inputImage) {
			this.inputImage.releaseReference();
		}
		return this.outputImage;
	}

	public ImageEditor reset() {
		if (this.outputImage != this.inputImage) {
			this.outputImage.releaseReference();

			this.outputImage = this.inputImage;
		}

		return this;
	}

	public void release() {
		this.outputImage.releaseReference();
		this.inputImage.releaseReference();
	}

	protected static int getExifOrientation(final Metadata metadata) {
		Validate.notNull(metadata, "metadata 不可为 null");

		ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
		if (Objects.nonNull(exifIFD0Directory)) {
			Integer orientation = exifIFD0Directory.getInteger(ExifDirectoryBase.TAG_ORIENTATION);
			if (Objects.nonNull(orientation)) {
				return orientation;
			}
		}
		return NORMAL_EXIF_ORIENTATION;
	}
}
