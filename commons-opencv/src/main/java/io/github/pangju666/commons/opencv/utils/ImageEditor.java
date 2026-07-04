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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.indexer.DoubleIndexer;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Objects;

public class ImageEditor {
	protected static final int NORMAL_EXIF_ORIENTATION = 1;
	protected static final Scalar TRANSPARENT_SCALAR = new Scalar(0,0,0,0);

	protected Mat inputImage;

	protected Mat outputImage;

	protected Size outputImageSize;

	protected ImageEditor(final Mat inputImage, int exifOrientation, int flags) {
		Validate.notNull(inputImage, "inputImage 不可为 null");

		this.inputImage = inputImage;
		Validate.isTrue(!inputImage.isNull() && !inputImage.empty(), "inputImage 不存在图像数据");

		this.outputImage = inputImage;
		this.outputImageSize = inputImage.size();
		Validate.isTrue(!outputImageSize.isNull() && !outputImageSize.empty(), "inputImage 不存在图像尺寸");

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

			if (exifOrientation >= 5) {
				this.outputImageSize = this.outputImage.size();
			}

			this.inputImage = this.outputImage;
		}
	}

	public static ImageEditor of(final File file) throws IOException {
		return of(file, OpencvConstants.DEFAULT_IMAGE_COLOR_TYPE);
	}

	public static ImageEditor of(final File file, final int flags) throws IOException {
		int exifOrientation = NORMAL_EXIF_ORIENTATION;
		Mat mat = ImageUtils.read(file, flags);

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
		Mat mat = ImageUtils.read(bytes, flags);

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

		Mat image = this.outputImage;

		if (image.channels() < 4) {
			Mat bgraMat = new Mat();
			int code = switch (image.type()) {
				case opencv_imgcodecs.IMREAD_COLOR_RGB -> opencv_imgproc.COLOR_RGB2RGBA;
				case opencv_imgcodecs.IMREAD_GRAYSCALE,
					 opencv_imgcodecs.IMREAD_REDUCED_GRAYSCALE_2,
					 opencv_imgcodecs.IMREAD_REDUCED_GRAYSCALE_4,
					 opencv_imgcodecs.IMREAD_REDUCED_GRAYSCALE_8 -> opencv_imgproc.COLOR_GRAY2BGRA;
				default -> opencv_imgproc.COLOR_BGR2BGRA;
			};
			opencv_imgproc.cvtColor(image, bgraMat, code);
			image.close();
			image = bgraMat;
		}

		try (MatVector channels = new MatVector();
		     Scalar alphaScalar = new Scalar(Math.floor(alpha * 255))) {
			opencv_core.split(image, channels);

			try (Mat alphaChannel = channels.get(3)) {
				alphaChannel.put(alphaScalar);
				opencv_core.merge(channels, image);
			}
		}

		this.outputImage.close();
		this.outputImage = image;

		return this;
	}

	public ImageEditor rotate(final RotateDirection direction) {
		Validate.notNull(direction, "direction 不可为 null");

		Mat image = new Mat();
		opencv_core.rotate(outputImage, image, direction.getCode());

		this.outputImage.close();
		this.outputImage = image;

		return this;
	}

	public ImageEditor rotate(final double angle) {
		int width = outputImageSize.width();
		int height = outputImageSize.height();
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

		Mat image = new Mat(new Size(newWidth, newHeight), outputImage.type());
		Size size = new Size(newWidth, newHeight);
		// 仿射旋转，边界填充背景色
		opencv_imgproc.warpAffine(outputImage, image, rotateMat, size, opencv_imgproc.INTER_LINEAR,
			opencv_core.BORDER_CONSTANT, TRANSPARENT_SCALAR);

		this.outputImage.close();
		this.outputImage = image;

		return this;
	}

	public ImageEditor flip(final FlipDirection direction) {
		Validate.notNull(direction, "direction 不可为 null");

		Mat image = new Mat();
		opencv_core.flip(outputImage, image, direction.getCode());

		this.outputImage.close();
		this.outputImage = image;

		return this;
	}

	public ImageEditor resize(final int width, final int height) {
		Validate.isTrue(width > 0, "width 必须大于 0");
		Validate.isTrue(height > 0, "height 必须大于 0");

		Mat image = new Mat();
		opencv_imgproc.resize(outputImage, image, new Size(width, height));

		this.outputImage.close();
		this.outputImage = image;

		this.outputImageSize.close();
		this.outputImageSize = new Size(width, height);

		return this;
	}

	public ImageEditor scaleByWidth(final int targetWidth) {
		Validate.isTrue(targetWidth > 0, "targetWidth 必须大于 0");

		int width = outputImageSize.width();
		int height = outputImageSize.height();
		Size size;

		if (width > height) {
			double ratio = (double) width / height;
			size = new Size(targetWidth, Math.max((int) Math.round(targetWidth / ratio), 1));
		} else {
			double ratio = (double) height / width;
			size = new Size(targetWidth, Math.max((int) Math.round(targetWidth * ratio), 1));
		}

		return resize(size.width(), size.height());
	}

	public ImageEditor scaleByHeight(final int targetHeight) {
		Validate.isTrue(targetHeight > 0, "targetHeight 必须大于 0");

		int width = outputImageSize.width();
		int height = outputImageSize.height();
		Size size;

		if (width > height) {
			double ratio = (double) width / height;
			size = new Size(Math.max((int) Math.round(targetHeight * ratio), 1), targetHeight);
		} else {
			double ratio = (double) height / width;
			size = new Size(Math.max((int) Math.round(targetHeight / ratio), 1), targetHeight);
		}

		return resize(size.width(), size.height());
	}

	public ImageEditor scale(final double scalingFactor) {
		Validate.isTrue(scalingFactor > 0, "scalingFactor 必须大于 0");

		return resize((int) Math.round(outputImageSize.width() * scalingFactor),
			(int) Math.round(outputImageSize.height() * scalingFactor));
	}

	public ImageEditor scale(final int targetWidth, final int targetHeight) {
		Validate.isTrue(targetWidth > 0, "targetWidth 必须大于 0");
		Validate.isTrue(targetHeight > 0, "targetHeight 必须大于 0");

		int width = outputImageSize.width();
		int height = outputImageSize.height();
		Size size;

		double ratio = (double) width / height;
		int heightByWidth = Math.max((int) Math.round(targetWidth / ratio), 1);
		if (heightByWidth <= targetHeight) {
			size = new Size(targetWidth, heightByWidth);
		} else {
			int widthByHeight = Math.max((int) Math.round(targetHeight * ratio), 1);
			size = new Size(widthByHeight, targetHeight);
		}

		return resize(size.width(), size.height());
	}

	public ImageEditor cropByCenter(int width, int height) {
		Validate.isTrue(width > 0, "width 不能小于0");
		Validate.isTrue(height > 0, "height 不能小于0");

		// 边界检测
		if (width >= this.outputImageSize.width() || height >= this.outputImageSize.height()) {
			return this;
		}

		return cropByRect((this.outputImageSize.width() - width) / 2, (this.outputImageSize.height() - height) / 2,
			this.outputImageSize.width(), this.outputImageSize.height());
	}

	public ImageEditor cropByOffset(int topOffset, int bottomOffset, int leftOffset, int rightOffset) {
		Validate.isTrue(topOffset >= 0 && bottomOffset >= 0 && leftOffset >= 0 && rightOffset >= 0,
			"offset 不能小于0");

		// 边界检测
		if (rightOffset >= this.outputImageSize.width() ||
			leftOffset >= this.outputImageSize.width() ||
			leftOffset + rightOffset >= this.outputImageSize.width() ||
			topOffset >= this.outputImageSize.height() ||
			bottomOffset >= this.outputImageSize.height() ||
			topOffset + bottomOffset >= this.outputImageSize.height()) {
			return this;
		}

		return cropByRect(leftOffset, topOffset, this.outputImageSize.width() - leftOffset - rightOffset,
			this.outputImageSize.height() - topOffset - bottomOffset);
	}

	public ImageEditor cropByRect(int x, int y, int width, int height) {
		Validate.isTrue(x >= 0, "x 不能小于0");
		Validate.isTrue(y >= 0, "y 不能小于0");
		Validate.isTrue(width > 0, "width 不能小于0");
		Validate.isTrue(height > 0, "height 不能小于0");

		// 边界检测
		if (x >= this.outputImageSize.width() || width >= this.outputImageSize.width() ||
			x + width >= this.outputImageSize.width() || y >= this.outputImageSize.height() ||
			height >= this.outputImageSize.height() || y + height >= this.outputImageSize.height()) {
			return this;
		}

		Rect rect = new Rect(x, y, width, height);
		Mat image = new Mat(this.outputImage, rect);

		this.outputImage.close();
		this.outputImage = image;

		this.outputImageSize.close();
		this.outputImageSize = new Size(width, height);

		return this;
	}

	/*public ImageEditor addImageWatermark(final BufferedImage watermarkImage, final ImageWatermarkOption option) {
		Validate.notNull(option, "option 不可为 null");

		this.outputImage = option.toWatermark(this.outputImageSize, watermarkImage).apply(this.outputImage);
		return this;
	}
	public ImageEditor addTextWatermark(final String watermarkText, final TextWatermarkOption option) {
		Validate.notNull(option, "option 不可为 null");

		this.outputImage = option.toCaption(watermarkText, this.outputImage).apply(this.outputImage);
		return this;
	}*/

	/*public ImageEditor blur() {
		this.outputImage = ImageUtil.blur(this.outputImage, 1.5f);
		return this;
	}

	public ImageEditor blur(final float radius) {
		this.outputImage = ImageUtil.blur(this.outputImage, radius);
		return this;
	}

	public ImageEditor sharpen() {
		this.outputImage = ImageUtil.sharpen(this.outputImage, 0.3f);
		return this;
	}

	public ImageEditor sharpen(final float amount) {
		this.outputImage = ImageUtil.sharpen(this.outputImage, amount);
		return this;
	}

	public ImageEditor grayscale() {
		Image image = ImageUtil.filter(this.outputImage, GRAY_FILTER);
		this.outputImage = ImageUtil.toBuffered(image);
		return this;
	}

	public ImageEditor contrast() {
		Image image = ImageUtil.filter(this.outputImage, DEFAULT_CONTRAST_FILTER);
		this.outputImage = ImageUtil.toBuffered(image);
		return this;
	}

	public ImageEditor contrast(final float amount) {
		if (amount == 0f || amount > 1.0 || amount < -1.0) {
			return this;
		}

		BrightnessContrastFilter filter = new BrightnessContrastFilter(0f, amount);
		Image image = ImageUtil.filter(this.outputImage, filter);
		this.outputImage = ImageUtil.toBuffered(image);
		return this;
	}

	public ImageEditor brightness(final float amount) {
		if (amount == 0f || amount > 2.0 || amount < -2.0) {
			return this;
		}

		BrightnessContrastFilter filter = new BrightnessContrastFilter(amount, 0f);
		Image image = ImageUtil.filter(this.outputImage, filter);
		this.outputImage = ImageUtil.toBuffered(image);
		return this;
	}

	public ImageEditor filter(final ImageFilter filter) {
		Validate.notNull(filter, "filter不可为 null");

		Image image = ImageUtil.filter(this.outputImage, filter);
		this.outputImage = ImageUtil.toBuffered(image);
		return this;
	}
	 */

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
			this.inputImage.close();
		}
		return this.outputImage;
	}

	public ImageEditor reset() {
		if (this.outputImage != this.inputImage) {
			this.outputImage.close();
			this.outputImageSize.close();

			this.outputImage = this.inputImage;
			this.outputImageSize = this.inputImage.size();
		}

		return this;
	}

	public void release() {
		if (this.outputImage != this.inputImage) {
			this.outputImage.close();
			this.outputImageSize.close();
		}
		this.inputImage.close();
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
