package io.github.pangju666.commons.image.utils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.bmp.BmpHeaderDirectory;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.file.FileTypeDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;
import com.drew.metadata.webp.WebpDirectory;
import io.github.pangju666.commons.image.lang.ImageConstants;
import io.github.pangju666.commons.image.model.ImageSize;
import io.github.pangju666.commons.io.lang.IOConstants;
import io.github.pangju666.commons.io.utils.FileUtils;
import org.apache.commons.io.input.UnsynchronizedBufferedInputStream;
import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * 图片工具类
 *
 * @author pangju
 * @since 1.0.0
 */
public class ImageUtils {
	public static final int DEFAULT_MEDIAN_FILTER_SIZE = 3;
	public static final int DEFAULT_GAUSSIAN_KERNEL_SIZE = 5;
	public static final double DEFAULT_GAUSSIAN_SIGMA = 1.4;
	protected static final int DEFAULT_BUFFERED_BUFFER_SIZE = 8192;

	protected ImageUtils() {
	}

	public static boolean isSupportImageType(final File file) throws IOException {
		String mimeType = getImageType(file);
		return ImageConstants.SUPPORT_IMAGE_TYPES.contains(mimeType);
	}

	public static boolean isSupportImageType(final byte[] bytes) {
		String mimeType = getImageType(bytes);
		return ImageConstants.SUPPORT_IMAGE_TYPES.contains(mimeType);
	}

	public static boolean isSupportImageType(final InputStream inputStream) throws IOException {
		String mimeType = getImageType(inputStream);
		return ImageConstants.SUPPORT_IMAGE_TYPES.contains(mimeType);
	}

	public static ImageSize computeNewSizeByWidth(final int imageWidth, final int imageHeight, final int targetWidth) {
		if (imageWidth > imageHeight) {
			double ratio = (double) imageWidth / imageHeight;
			return new ImageSize(targetWidth, (int) Math.max(targetWidth / ratio, 1));
		}
		double ratio = (double) imageHeight / imageWidth;
		return new ImageSize(targetWidth, (int) Math.max(targetWidth * ratio, 1));
	}

	public static ImageSize computeNewSizeByHeight(final int imageWidth, final int imageHeight, final int targetHeight) {
		if (imageWidth > imageHeight) {
			double ratio = (double) imageWidth / imageHeight;
			return new ImageSize((int) Math.max(targetHeight * ratio, 1), targetHeight);
		}
		double ratio = (double) imageHeight / imageWidth;
		return new ImageSize((int) Math.max(targetHeight / ratio, 1), targetHeight);
	}

	public static ImageSize computeNewSize(final int imageWidth, final int imageHeight,
										   final int targetWidth, final int targetHeight) {
		double ratio = (double) imageWidth / imageHeight;
		if (imageWidth > imageHeight) {
			double actualHeight = Math.max(targetWidth / ratio, 1);
			if (actualHeight > targetHeight) {
				return new ImageSize((int) Math.max(targetHeight * ratio, 1), targetHeight);
			}
			return new ImageSize(targetWidth, (int) actualHeight);
		} else {
			double actualWidth = Math.max(targetHeight / ratio, 1);
			if (actualWidth > targetWidth) {
				return new ImageSize((int) Math.max(targetHeight * ratio, 1), targetHeight);
			}
			return new ImageSize((int) actualWidth, targetHeight);
		}
	}

	public static boolean isImage(final File file) throws IOException {
		return FileUtils.isImageType(file);
	}

	public static boolean isImage(final byte[] bytes) {
		if (ArrayUtils.isEmpty(bytes)) {
			return false;
		}
		String mimeType = IOConstants.getDefaultTika().detect(bytes);
		return StringUtils.startsWith(mimeType, IOConstants.IMAGE_MIME_TYPE_PREFIX);
	}

	public static boolean isImage(final InputStream inputStream) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		String mimeType = IOConstants.getDefaultTika().detect(inputStream);
		return StringUtils.startsWith(mimeType, IOConstants.IMAGE_MIME_TYPE_PREFIX);
	}

	public static boolean isImage(final Metadata metadata) {
		Validate.notNull(metadata, "metadata 不可为 null");
		String mimeType = getImageType(metadata);
		return StringUtils.startsWith(mimeType, IOConstants.IMAGE_MIME_TYPE_PREFIX);
	}

	public static String getImageType(final File file) throws IOException {
		String mimeType = FileUtils.getMimeType(file);
		return StringUtils.startsWith(mimeType, IOConstants.IMAGE_MIME_TYPE_PREFIX) ? mimeType : null;
	}

	public static String getImageType(final byte[] bytes) {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}
		String mimeType = IOConstants.getDefaultTika().detect(bytes);
		return StringUtils.startsWith(mimeType, IOConstants.IMAGE_MIME_TYPE_PREFIX) ? mimeType : null;
	}

	public static String getImageType(final InputStream inputStream) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		String mimeType = IOConstants.getDefaultTika().detect(inputStream);
		return StringUtils.startsWith(mimeType, IOConstants.IMAGE_MIME_TYPE_PREFIX) ? mimeType : null;
	}

	public static String getImageType(final Metadata metadata) {
		Validate.notNull(metadata, "metadata 不可为 null");

		Collection<FileTypeDirectory> fileTypeDirectories = metadata.getDirectoriesOfType(FileTypeDirectory.class);
		var iterator = fileTypeDirectories.iterator();
		if (!iterator.hasNext()) {
			for (Directory directory : metadata.getDirectories()) {
				if (directory instanceof JpegDirectory) {
					return "image/jpeg";
				} else if (directory instanceof PngDirectory) {
					return "image/png";
				} else if (directory instanceof WebpDirectory) {
					return "image/webp";
				} else if (directory instanceof BmpHeaderDirectory) {
					return "image/bmp";
				}
			}
			return null;
		}
		FileTypeDirectory fileTypeDirectory = iterator.next();
		String mimeType = fileTypeDirectory.getString(FileTypeDirectory.TAG_DETECTED_FILE_MIME_TYPE);
		return StringUtils.startsWith(mimeType, IOConstants.IMAGE_MIME_TYPE_PREFIX) ? mimeType : null;
	}

	public static ImageSize getImageSize(final File file) throws IOException {
		String mimeType = getImageType(file);
		return getImageSize(file, mimeType);
	}

	public static ImageSize getImageSize(final File file, final String mimeType) throws IOException {
		FileUtils.validateFile(file, "file 不可为 null");
		Validate.notBlank(mimeType, "mimeType 不可为空");

		try {
			Metadata metadata = ImageMetadataReader.readMetadata(file);
			ImageSize size = getImageSizeByMetadata(metadata);
			if (Objects.nonNull(size)) {
				return size;
			}
		} catch (ImageProcessingException ignored) {
		}

		if (!ImageConstants.SUPPORT_IMAGE_TYPES.contains(mimeType)) {
			return null;
		}
		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(file)) {
			if (Objects.isNull(imageInputStream)) {
				return null;
			}
			return getImageSize(imageInputStream, mimeType);
		}
	}

	public static ImageSize getImageSize(final byte[] bytes) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}
		UnsynchronizedByteArrayOutputStream outputStream = buildByteArrayOutputStream(bytes);
		String mimeType = IOConstants.getDefaultTika().detect(outputStream.toInputStream());
		return getImageSizeByOutputStream(outputStream, mimeType);
	}

	public static ImageSize getImageSize(final byte[] bytes, final String mimeType) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}
		Validate.notBlank(mimeType, "mimeType 不可为空");
		UnsynchronizedByteArrayOutputStream outputStream = buildByteArrayOutputStream(bytes);
		return getImageSizeByOutputStream(outputStream, mimeType);
	}

	public static ImageSize getImageSize(final InputStream inputStream) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		UnsynchronizedByteArrayOutputStream outputStream = buildByteArrayOutputStream(inputStream);
		String mimeType = IOConstants.getDefaultTika().detect(outputStream.toInputStream());
		return getImageSizeByOutputStream(outputStream, mimeType);
	}

	public static ImageSize getImageSize(final InputStream inputStream, final String mimeType) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		Validate.notBlank(mimeType, "mimeType 不可为空");
		UnsynchronizedByteArrayOutputStream outputStream = buildByteArrayOutputStream(inputStream);
		return getImageSizeByOutputStream(outputStream, mimeType);
	}

	public static ImageSize getImageSize(ImageInputStream imageInputStream) throws IOException {
		Validate.notNull(imageInputStream, "imageInputStream 不可为 null");
		Iterator<ImageReader> iterator = ImageIO.getImageReaders(imageInputStream);
		if (!iterator.hasNext()) {
			return null;
		}
		ImageReader reader = iterator.next();
		reader.setInput(imageInputStream, true);
		return new ImageSize(reader.getWidth(0), reader.getHeight(0));
	}

	public static ImageSize getImageSize(ImageInputStream imageInputStream, String mimeType) throws IOException {
		Validate.notNull(imageInputStream, "imageInputStream 不可为 null");
		Validate.notBlank(mimeType, "mimeType 不可为空");
		if (!ImageConstants.SUPPORT_IMAGE_TYPES.contains(mimeType)) {
			return null;
		}
		return getImageSizeByImageInputStream(imageInputStream, mimeType);
	}

	public static ImageSize getImageSize(final Metadata metadata) throws MetadataException {
		Validate.notNull(metadata, "metadata 不可为 null");
		ImageSize size = getImageSizeByMetadata(metadata);
		if (Objects.isNull(size)) {
			throw new MetadataException("无法读取图片尺寸信息");
		}
		return size;
	}

	protected static ImageSize getImageSizeByOutputStream(final UnsynchronizedByteArrayOutputStream outputStream,
														  final String mimeType) throws IOException {
		try (InputStream inputStream = outputStream.toInputStream();
			 InputStream bufferedInputStream = buildBufferedInputStream(inputStream)) {
			Metadata metadata = ImageMetadataReader.readMetadata(bufferedInputStream);
			ImageSize size = getImageSizeByMetadata(metadata);
			if (Objects.nonNull(size)) {
				return size;
			}
		} catch (ImageProcessingException ignored) {
		}

		if (!ImageConstants.SUPPORT_IMAGE_TYPES.contains(mimeType)) {
			return null;
		}
		try (InputStream inputStream = outputStream.toInputStream();
			 InputStream bufferedInputStream = buildBufferedInputStream(inputStream);
			 ImageInputStream imageInputStream = ImageIO.createImageInputStream(bufferedInputStream)) {
			if (Objects.isNull(imageInputStream)) {
				return null;
			}
			return getImageSizeByImageInputStream(imageInputStream, mimeType);
		}
	}

	protected static ImageSize getImageSizeByImageInputStream(ImageInputStream imageInputStream, String mimeType) throws IOException {
		Iterator<ImageReader> iterator = ImageIO.getImageReadersByMIMEType(mimeType);
		if (!iterator.hasNext()) {
			return null;
		}
		ImageReader reader = iterator.next();
		reader.setInput(imageInputStream, true);
		return new ImageSize(reader.getWidth(0), reader.getHeight(0));
	}

	protected static ImageSize getImageSizeByMetadata(Metadata metadata) {
		Collection<ExifDirectoryBase> fileTypeDirectories = metadata.getDirectoriesOfType(ExifDirectoryBase.class);
		var iterator = fileTypeDirectories.iterator();
		if (!iterator.hasNext()) {
			return getImageSizeByDirectories(metadata.getDirectories());
		}

		ExifDirectoryBase exifDirectory = iterator.next();
		Integer width = exifDirectory.getInteger(ExifDirectoryBase.TAG_IMAGE_WIDTH);
		Integer height = exifDirectory.getInteger(ExifDirectoryBase.TAG_IMAGE_HEIGHT);
		if (ObjectUtils.anyNull(width, height)) {
			ImageSize size = getImageSizeByDirectories(metadata.getDirectories());
			if (Objects.isNull(size)) {
				return null;
			}
			width = size.width();
			height = size.height();
		}
		Integer orientation = exifDirectory.getInteger(ExifDirectoryBase.TAG_ORIENTATION);
		if (Objects.nonNull(orientation) && (orientation >= 5 && orientation <= 8)) {
			int tmp = width;
			width = height;
			height = tmp;
		}
		return new ImageSize(width, height);
	}

	protected static ImageSize getImageSizeByDirectories(Iterable<Directory> directories) {
		try {
			for (Directory directory : directories) {
				if (directory instanceof JpegDirectory jpegDirectory) {
					return new ImageSize(jpegDirectory.getImageWidth(), jpegDirectory.getImageHeight());
				} else if (directory instanceof PngDirectory pngDirectory) {
					return new ImageSize(
						pngDirectory.getInt(PngDirectory.TAG_IMAGE_WIDTH),
						pngDirectory.getInt(PngDirectory.TAG_IMAGE_HEIGHT)
					);
				} else if (directory instanceof WebpDirectory webpDirectory) {
					return new ImageSize(
						webpDirectory.getInt(WebpDirectory.TAG_IMAGE_WIDTH),
						webpDirectory.getInt(WebpDirectory.TAG_IMAGE_HEIGHT)
					);
				} else if (directory instanceof BmpHeaderDirectory bmpHeaderDirectory) {
					return new ImageSize(
						bmpHeaderDirectory.getInt(BmpHeaderDirectory.TAG_IMAGE_WIDTH),
						bmpHeaderDirectory.getInt(BmpHeaderDirectory.TAG_IMAGE_HEIGHT)
					);
				}
			}
			return null;
		} catch (MetadataException e) {
			return null;
		}
	}

	protected static UnsynchronizedBufferedInputStream buildBufferedInputStream(InputStream inputStream) throws IOException {
		return new UnsynchronizedBufferedInputStream.Builder()
			.setBufferSize(DEFAULT_BUFFERED_BUFFER_SIZE)
			.setInputStream(inputStream)
			.get();
	}

	protected static UnsynchronizedByteArrayOutputStream buildByteArrayOutputStream(byte[] bytes) throws IOException {
		UnsynchronizedByteArrayOutputStream outputStream = UnsynchronizedByteArrayOutputStream.builder()
			.setBufferSize(DEFAULT_BUFFERED_BUFFER_SIZE)
			.get();
		outputStream.write(bytes);
		return outputStream;
	}

	protected static UnsynchronizedByteArrayOutputStream buildByteArrayOutputStream(InputStream inputStream) throws IOException {
		UnsynchronizedByteArrayOutputStream outputStream = UnsynchronizedByteArrayOutputStream.builder()
			.setBufferSize(DEFAULT_BUFFERED_BUFFER_SIZE)
			.get();
		outputStream.write(inputStream);
		return outputStream;
	}

	protected static UnsynchronizedByteArrayInputStream buildByteArrayInputStream(byte[] bytes) throws IOException {
		return UnsynchronizedByteArrayInputStream.builder()
			.setByteArray(bytes)
			.setOffset(0)
			.setLength(bytes.length)
			.get();
	}

	public static BufferedImage binaryImage(byte[] bytes) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}
		UnsynchronizedByteArrayInputStream inputStream = buildByteArrayInputStream(bytes);
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		return binaryImage(bytes, computeBinaryImageThreshold(bufferedImage));
	}

	public static BufferedImage binaryImage(byte[] bytes, int Threshold) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}
		UnsynchronizedByteArrayInputStream inputStream = buildByteArrayInputStream(bytes);
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		return binaryImage(bufferedImage, Threshold);
	}

	public static BufferedImage binaryImage(ImageInputStream imageInputStream) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(imageInputStream);
		return binaryImage(imageInputStream, computeBinaryImageThreshold(bufferedImage));
	}

	public static BufferedImage binaryImage(ImageInputStream imageInputStream, int threshold) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(imageInputStream);
		return binaryImage(bufferedImage, threshold);
	}

	public static BufferedImage binaryImage(File file) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(file);
		return binaryImage(bufferedImage, computeBinaryImageThreshold(bufferedImage));
	}

	public static BufferedImage binaryImage(File file, int threshold) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(file);
		return binaryImage(bufferedImage, threshold);
	}

	public static BufferedImage binaryImage(InputStream inputStream) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		return binaryImage(bufferedImage, computeBinaryImageThreshold(bufferedImage));
	}

	public static BufferedImage binaryImage(InputStream inputStream, int threshold) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		return binaryImage(bufferedImage, threshold);
	}

	public static BufferedImage binaryImage(BufferedImage image) {
		return binaryImage(image, computeBinaryImageThreshold(image));
	}

	/**
	 * 图像二值化
	 *
	 * @param image 输入图像
	 * @param threshold 阈值
	 * @return 输出图像
	 */
	public static BufferedImage binaryImage(BufferedImage image, int threshold) {
		// 获取图像宽度和高度
		int width = image.getWidth();
		int height = image.getHeight();
		// 创建输出图像（TYPE_BYTE_BINARY表示创建二值化图像）
		BufferedImage binarizationImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);

		// 遍历每个像素并进行二值化处理
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// 获取该点的颜色
				int color = image.getRGB(x, y);

				// 提取灰度值
				int grayLevel = color & 0xff;

				// 根据阈值决定是黑色还是白色
				if (grayLevel > threshold) {
					binarizationImage.setRGB(x, y, ImageConstants.WHITE_HEX_RGB); // 白色
				} else {
					binarizationImage.setRGB(x, y, ImageConstants.BLACK_HEX_RGB); // 黑色
				}
			}
		}
		return binarizationImage;
	}

	public static BufferedImage grayscaleImage(byte[] bytes) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}
		UnsynchronizedByteArrayInputStream inputStream = buildByteArrayInputStream(bytes);
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		return grayscaleImage(bufferedImage);
	}

	public static BufferedImage grayscaleImage(ImageInputStream imageInputStream) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(imageInputStream);
		return grayscaleImage(bufferedImage);
	}

	public static BufferedImage grayscaleImage(File file) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(file);
		return grayscaleImage(bufferedImage);
	}

	public static BufferedImage grayscaleImage(InputStream inputStream) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		return grayscaleImage(bufferedImage);
	}

	/**
	 * 图像灰度化
	 */
	public static BufferedImage grayscaleImage(BufferedImage image) {
		// 获取图像宽度和高度
		int width = image.getWidth();
		int height = image.getHeight();
		// 创建输出图像（TYPE_BYTE_GRAY表示创建灰度图像）
		BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

		// 遍历每个像素并进行灰度化处理
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// 获取该点的颜色
				int color = image.getRGB(x, y);

				// 提取RGB分量
				int red = (color >> 16) & 0xff;
				int green = (color >> 8) & 0xff;
				int blue = color & 0xff;

				// 计算灰度值（这里使用了加权平均法，权重反映了人眼对不同颜色的敏感度）
				int grayLevel = (int) (0.3 * red + 0.59 * green + 0.11 * blue);

				// 将灰度值设置回像素（灰度图像是单通道，因此只需要一个值）
				int grayColor = (grayLevel << 16) + (grayLevel << 8) + grayLevel;
				grayImage.setRGB(x, y, grayColor);
			}
		}
		return grayImage;
	}

	protected static int computeBinaryImageThreshold(BufferedImage image) {
		int[] histogram = new int[256];
		int total = image.getWidth() * image.getHeight();

		// 计算直方图
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int color = image.getRGB(x, y);
				int grayLevel = color & 0xff;
				histogram[grayLevel]++;
			}
		}

		double sum = 0;
		for (int i = 0; i < 256; i++) sum += i * histogram[i];

		double sumB = 0;
		int wB = 0;
		int wF;

		double varMax = 0;
		int threshold = 0;

		for (int t = 0; t < 256; t++) {
			wB += histogram[t];               // Weight Background
			if (wB == 0) continue;

			wF = total - wB;                  // Weight Foreground
			if (wF == 0) break;

			sumB += t * histogram[t];

			double mB = sumB / wB;            // Mean Background
			double mF = (sum - sumB) / wF;    // Mean Foreground

			// Calculate Between Class Variance
			double varBetween = (double) wB * (double) wF * (mB - mF) * (mB - mF);

			// Check if new maximum found
			if (varBetween > varMax) {
				varMax = varBetween;
				threshold = t;
			}
		}
		return threshold;
	}

	public static BufferedImage medianFilterImage(byte[] bytes) throws IOException {
		return medianFilterImage(bytes, DEFAULT_MEDIAN_FILTER_SIZE);
	}

	public static BufferedImage medianFilterImage(byte[] bytes, int filterSize) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}
		UnsynchronizedByteArrayInputStream inputStream = buildByteArrayInputStream(bytes);
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		return medianFilterImage(bufferedImage, filterSize);
	}

	public static BufferedImage medianFilterImage(ImageInputStream imageInputStream) throws IOException {
		return medianFilterImage(imageInputStream, DEFAULT_MEDIAN_FILTER_SIZE);
	}

	public static BufferedImage medianFilterImage(ImageInputStream imageInputStream, int filterSize) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(imageInputStream);
		return medianFilterImage(bufferedImage, filterSize);
	}

	public static BufferedImage medianFilterImage(File file) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(file);
		return medianFilterImage(bufferedImage, DEFAULT_MEDIAN_FILTER_SIZE);
	}

	public static BufferedImage medianFilterImage(File file, int filterSize) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(file);
		return medianFilterImage(bufferedImage, filterSize);
	}

	public static BufferedImage medianFilterImage(InputStream inputStream) throws IOException {
		return medianFilterImage(inputStream, DEFAULT_MEDIAN_FILTER_SIZE);
	}

	public static BufferedImage medianFilterImage(InputStream inputStream, int filterSize) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		return medianFilterImage(bufferedImage, filterSize);
	}

	public static BufferedImage medianFilterImage(BufferedImage image) {
		return medianFilterImage(image, DEFAULT_MEDIAN_FILTER_SIZE);
	}

	/**
	 * 中值滤波去噪
	 *
	 * @param image      输入图像
	 * @param filterSize 滤波器大小
	 * @return 输出图像
	 */
	public static BufferedImage medianFilterImage(BufferedImage image, int filterSize) {
		// 获取图像宽度和高度
		int width = image.getWidth();
		int height = image.getHeight();
		// 创建输出图像
		BufferedImage medianFilteringImage = new BufferedImage(width, height, image.getType());
		// 定义滤波器大小（这里使用3x3滤波器）
		int radius = filterSize / 2;

		// 遍历每个像素并进行中值滤波
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int[] pixels = new int[filterSize * filterSize];
				int index = 0;

				// 收集邻域像素
				for (int fy = -radius; fy <= radius; fy++) {
					for (int fx = -radius; fx <= radius; fx++) {
						int nx = x + fx;
						int ny = y + fy;
						if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
							pixels[index++] = image.getRGB(nx, ny) & 0xff;
						}
					}
				}

				// 计算中值
				int medianValue = findMedian(pixels, index);

				// 设置新的像素值
				int rgb = (medianValue << 16) + (medianValue << 8) + medianValue;
				medianFilteringImage.setRGB(x, y, rgb);
			}
		}
		return medianFilteringImage;
	}

	protected static int findMedian(int[] array, int length) {
		int[] sortedArray = new int[length];
		System.arraycopy(array, 0, sortedArray, 0, length);
		java.util.Arrays.sort(sortedArray);
		return sortedArray[length / 2];
	}

	public static BufferedImage gaussianFilterImage(byte[] bytes) throws IOException {
		return gaussianFilterImage(bytes, DEFAULT_GAUSSIAN_KERNEL_SIZE, DEFAULT_GAUSSIAN_SIGMA);
	}

	public static BufferedImage gaussianFilterImage(byte[] bytes, int kernelSize, double sigma) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}
		UnsynchronizedByteArrayInputStream inputStream = buildByteArrayInputStream(bytes);
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		return gaussianFilterImage(bufferedImage, kernelSize, sigma);
	}

	public static BufferedImage gaussianFilterImage(ImageInputStream imageInputStream) throws IOException {
		return gaussianFilterImage(imageInputStream, DEFAULT_GAUSSIAN_KERNEL_SIZE, DEFAULT_GAUSSIAN_SIGMA);
	}

	public static BufferedImage gaussianFilterImage(ImageInputStream imageInputStream, int kernelSize, double sigma) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(imageInputStream);
		return gaussianFilterImage(bufferedImage, kernelSize, sigma);
	}

	public static BufferedImage gaussianFilterImage(File file) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(file);
		return gaussianFilterImage(bufferedImage, DEFAULT_GAUSSIAN_KERNEL_SIZE, DEFAULT_GAUSSIAN_SIGMA);
	}

	public static BufferedImage gaussianFilterImage(File file, int kernelSize, double sigma) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(file);
		return gaussianFilterImage(bufferedImage, kernelSize, sigma);
	}

	public static BufferedImage gaussianFilterImage(InputStream inputStream) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		return gaussianFilterImage(bufferedImage, DEFAULT_GAUSSIAN_KERNEL_SIZE, DEFAULT_GAUSSIAN_SIGMA);
	}

	public static BufferedImage gaussianFilterImage(InputStream inputStream, int kernelSize, double sigma) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		return gaussianFilterImage(bufferedImage, kernelSize, sigma);
	}

	public static BufferedImage gaussianFilterImage(BufferedImage image) {
		return gaussianFilterImage(image, DEFAULT_GAUSSIAN_KERNEL_SIZE, DEFAULT_GAUSSIAN_SIGMA);
	}

	/**
	 * 高斯滤波去噪
	 *
	 * @param image      输入图像
	 * @param kernelSize 高斯核大小
	 * @param sigma      标准差
	 * @return 输出图像
	 */
	public static BufferedImage gaussianFilterImage(BufferedImage image, int kernelSize, double sigma) {
		// 获取图像宽度和高度
		int width = image.getWidth();
		int height = image.getHeight();
		// 创建输出图像
		BufferedImage gaussianFilteringImage = new BufferedImage(width, height, image.getType());

		// 生成高斯核
		double[][] kernel = generateGaussianKernel(kernelSize, sigma);
		int radius = kernel.length / 2;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				double rSum = 0, gSum = 0, bSum = 0;

				// 应用卷积
				for (int ky = -radius; ky <= radius; ky++) {
					for (int kx = -radius; kx <= radius; kx++) {
						int nx = x + kx;
						int ny = y + ky;
						if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
							int rgb = image.getRGB(nx, ny);
							int r = (rgb >> 16) & 0xff;
							int g = (rgb >> 8) & 0xff;
							int b = rgb & 0xff;

							double weight = kernel[ky + radius][kx + radius];

							rSum += r * weight;
							gSum += g * weight;
							bSum += b * weight;
						}
					}
				}

				// 设置新的像素值
				int rgb = ((int) rSum << 16) | ((int) gSum << 8) | (int) bSum;
				gaussianFilteringImage.setRGB(x, y, rgb);
			}
		}
		return gaussianFilteringImage;
	}

	protected static double[][] generateGaussianKernel(int size, double sigma) {
		double[][] kernel = new double[size][size];
		int radius = size / 2;
		double sum = 0.0;

		for (int y = -radius; y <= radius; y++) {
			for (int x = -radius; x <= radius; x++) {
				kernel[y + radius][x + radius] = Math.exp(-(x * x + y * y) / (2 * sigma * sigma));
				sum += kernel[y + radius][x + radius];
			}
		}

		// 归一化
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				kernel[y][x] /= sum;
			}
		}

		return kernel;
	}
}