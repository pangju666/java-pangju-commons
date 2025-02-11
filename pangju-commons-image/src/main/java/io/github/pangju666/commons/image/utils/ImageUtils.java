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
import java.util.Set;

/**
 * 图片工具类
 *
 * @author pangju
 * @since 1.0.0
 */
public class ImageUtils {
	protected static final int DEFAULT_BUFFERED_BUFFER_SIZE = 8192;
	protected static final int DEFAULT_THRESHOLD = 200;
	protected static final Set<String> MIME_TYPES = Set.of(ImageIO.getReaderMIMETypes());

	protected ImageUtils() {
	}

	protected static UnsynchronizedByteArrayInputStream buildByteArrayInputStream(byte[] bytes) throws IOException {
		return UnsynchronizedByteArrayInputStream.builder()
			.setByteArray(bytes)
			.setOffset(0)
			.setLength(bytes.length)
			.get();
	}

	public BufferedImage binarizationImage(byte[] bytes) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}
		UnsynchronizedByteArrayInputStream inputStream = buildByteArrayInputStream(bytes);
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		return binarizationImage(bufferedImage, DEFAULT_THRESHOLD);
	}

	public BufferedImage binarizationImage(byte[] bytes, int Threshold) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}
		UnsynchronizedByteArrayInputStream inputStream = buildByteArrayInputStream(bytes);
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		return binarizationImage(bufferedImage, Threshold);
	}

	public BufferedImage binarizationImage(ImageInputStream imageInputStream) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(imageInputStream);
		return binarizationImage(bufferedImage, DEFAULT_THRESHOLD);
	}

	public BufferedImage binarizationImage(ImageInputStream imageInputStream, int Threshold) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(imageInputStream);
		return binarizationImage(bufferedImage, Threshold);
	}

	public BufferedImage binarizationImage(File file) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(file);
		return binarizationImage(bufferedImage, DEFAULT_THRESHOLD);
	}

	public BufferedImage binarizationImage(File file, int Threshold) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(file);
		return binarizationImage(bufferedImage, Threshold);
	}

	public BufferedImage binarizationImage(InputStream inputStream) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		return binarizationImage(bufferedImage, DEFAULT_THRESHOLD);
	}

	public BufferedImage binarizationImage(InputStream inputStream, int Threshold) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		return binarizationImage(bufferedImage, Threshold);
	}

	public BufferedImage binarizationImage(BufferedImage image) {
		return binarizationImage(image, DEFAULT_THRESHOLD);
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

		if (!MIME_TYPES.contains(mimeType)) {
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
		if (!MIME_TYPES.contains(mimeType)) {
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

		if (!MIME_TYPES.contains(mimeType)) {
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

	// 图像二值化
	public BufferedImage binarizationImage(BufferedImage image, int Threshold) {
		// 获取图像的宽度和高度
		int width = image.getWidth();
		int height = image.getHeight();

		// 创建一个相同尺寸的 BufferedImage 用于二值化图像
		BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);

		// 遍历图像的所有像素并应用二值化
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pixel = image.getRGB(x, y);
				//int alpha = (pixel >> 24) & 0xff;
				int red = (pixel >> 16) & 0xff;
				int green = (pixel >> 8) & 0xff;
				int blue = pixel & 0xff;

				// 计算灰度值
				int gray = (red + green + blue) / 3;

				// 应用阈值
				int newPixel = (gray > Threshold) ? 0xFFFFFFFF : 0xFF000000;
				binaryImage.setRGB(x, y, newPixel);
			}
		}
		return binaryImage;
	}
}