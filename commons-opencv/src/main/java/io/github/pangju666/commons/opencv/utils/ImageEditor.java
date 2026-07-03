package io.github.pangju666.commons.opencv.utils;

import org.apache.commons.lang3.Validate;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Size;

public class ImageEditor {
	protected Size inputImageSize;

	protected Mat inputImage;

	protected Mat outputImage;

	protected Size outputImageSize;

	protected MatVector metadata;

	protected ImageEditor(final Mat inputImage, final MatVector metadata) {
		Validate.notNull(inputImage, "inputImage 不可为 null");

		this.inputImage = inputImage;
		this.inputImageSize = inputImage.size();

		this.outputImage = inputImage;
		this.outputImageSize = inputImageSize;
	}

	/*public static ImageEditor of(final File file) throws IOException {
		MatVector metadata = new MatVector();
		Mat mat = ImageUtils.
		ImageSize imageSize = new ImageSize(bufferedImage.getWidth(), bufferedImage.getHeight(), exifOrientation);
		return new ImageEditor(bufferedImage, imageSize, inputFormat);
	}

	public ImageEditor transparency(final float alpha) {
		Validate.isTrue(alpha >= 0 && alpha <= 1, "alpha 必须大于等于 0 且小于等于 1");

		this.outputImage = new Transparency(alpha).apply(this.outputImage);
		return this;
	}

	public ImageEditor rotate(final RotateDirection direction) {
		Validate.notNull(direction, "direction 不可为 null");

		this.outputImage = ImageUtil.createRotated(this.outputImage, Math.toRadians(direction.getAngle()));
		return this;
	}

	public ImageEditor rotate(final double angle) {
		this.outputImage = ImageUtil.createRotated(this.outputImage, Math.toRadians(angle));
		return this;
	}

	public ImageEditor blur() {
		this.outputImage = ImageUtil.blur(this.outputImage, 1.5f);
		return this;
	}

	public ImageEditor blur(final float radius) {
		this.outputImage = ImageUtil.blur(this.outputImage, radius);
		return this;
	}

	public ImageEditor flip(final FlipDirection direction) {
		Validate.notNull(direction, "direction 不可为 null");

		this.outputImage = ImageUtil.createFlipped(this.outputImage, direction.getAxis());
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

	public ImageEditor resize(final int width, final int height) {
		this.outputImageSize = this.outputImageSize.resize(width, height);
		this.outputImage = resample();
		return this;
	}

	public ImageEditor scaleByWidth(final int width) {
		this.outputImageSize = this.outputImageSize.scaleByWidth(width);
		this.outputImage = resample();
		return this;
	}

	public ImageEditor scaleByHeight(final int height) {
		this.outputImageSize = this.outputImageSize.scaleByHeight(height);
		this.outputImage = resample();
		return this;
	}

	public ImageEditor scale(final double scale) {
		this.outputImageSize = this.outputImageSize.scale(scale);
		this.outputImage = resample();
		return this;
	}

	public ImageEditor scale(final int width, final int height) {
		this.outputImageSize = this.outputImageSize.scale(width, height);
		this.outputImage = resample();
		return this;
	}

	public ImageEditor cropByCenter(int width, int height) {
		Validate.isTrue(width > 0, "width 不能小于0");
		Validate.isTrue(height > 0, "height 不能小于0");

		// 边界检测
		if (width >= this.outputImage.getWidth() || height >= this.outputImage.getHeight()) {
			return this;
		}

		this.outputImage = this.outputImage.getSubimage((this.outputImage.getWidth() - width) / 2,
			(this.outputImage.getHeight() - height) / 2, width, height);
		this.outputImageSize = new ImageSize(width, height);
		return this;
	}

	public ImageEditor cropByOffset(int topOffset, int bottomOffset, int leftOffset, int rightOffset) {
		Validate.isTrue(topOffset >= 0 && bottomOffset >= 0 && leftOffset >= 0 && rightOffset >= 0,
			"offset 不能小于0");

		// 边界检测
		if (rightOffset >= this.outputImage.getWidth() ||
			leftOffset >= this.outputImage.getWidth() ||
			leftOffset + rightOffset >= this.outputImage.getWidth() ||
			topOffset >= this.outputImage.getHeight() ||
			bottomOffset >= this.outputImage.getHeight() ||
			topOffset + bottomOffset >= this.outputImage.getHeight()) {
			return this;
		}

		int width = this.outputImage.getWidth() - leftOffset - rightOffset;
		int height = this.outputImage.getHeight() - topOffset - bottomOffset;
		this.outputImage = this.outputImage.getSubimage(leftOffset, topOffset, width, height);
		this.outputImageSize = new ImageSize(width, height);
		return this;
	}

	public ImageEditor cropByRect(int x, int y, int width, int height) {
		Validate.isTrue(x >= 0, "x 不能小于0");
		Validate.isTrue(y >= 0, "y 不能小于0");
		Validate.isTrue(width > 0, "width 不能小于0");
		Validate.isTrue(height > 0, "height 不能小于0");

		// 边界检测
		if (x >= this.outputImage.getWidth() ||
			width >= this.outputImage.getWidth() ||
			x + width >= this.outputImage.getWidth() ||
			y >= this.outputImage.getHeight() ||
			height >= this.outputImage.getHeight() ||
			y + height >= this.outputImage.getHeight()) {
			return this;
		}

		this.outputImage = this.outputImage.getSubimage(x, y, width, height);
		this.outputImageSize = new ImageSize(width, height);
		return this;
	}

	public ImageEditor addImageWatermark(final BufferedImage watermarkImage, final ImageWatermarkOption option) {
		Validate.notNull(option, "option 不可为 null");

		this.outputImage = option.toWatermark(this.outputImageSize, watermarkImage).apply(this.outputImage);
		return this;
	}
	public ImageEditor addTextWatermark(final String watermarkText, final TextWatermarkOption option) {
		Validate.notNull(option, "option 不可为 null");

		this.outputImage = option.toCaption(watermarkText, this.outputImage).apply(this.outputImage);
		return this;
	}

	public ImageEditor outputFormat(final String outputFormat) {
		Validate.notBlank(outputFormat, "输出格式不可为空");
		String upperCaseOutputFormat = outputFormat.toUpperCase();
		Validate.isTrue(ImageConstants.getSupportedWriteImageFormats().contains(upperCaseOutputFormat),
			"不支持输出该图像格式");

		this.outputFormat = upperCaseOutputFormat;
		return this;
	}

	public boolean toFile(final File outputFile) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		return ImageIO.write(toBufferedImage(), this.outputFormat, outputFile);
	}*/

	/*public ImageEditor reset() {
		this.outputImage = this.inputImage;
		this.outputImageSize = this.inputImageSize;
		this.outputFormat = this.inputFormat;
		if (StringUtils.isBlank(this.outputFormat)) {
			this.outputFormat = inputImage.getColorModel().hasAlpha() ? DEFAULT_ALPHA_OUTPUT_FORMAT : DEFAULT_OUTPUT_FORMAT;
		}
		this.resampleFilterType = ResampleOp.FILTER_LANCZOS;
		return this;
	}*/
}
