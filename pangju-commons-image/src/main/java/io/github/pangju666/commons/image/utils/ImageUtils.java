package io.github.pangju666.commons.image.utils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import io.github.pangju666.commons.image.lang.ImageConstants;
import io.github.pangju666.commons.image.model.ImageSize;
import io.github.pangju666.commons.io.lang.IOConstants;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.IOUtils;
import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

public class ImageUtils {
	private static final int NORMAL_ORIENTATION = 1;

	protected ImageUtils() {
	}

	public static boolean isSupportImageType(final File file) throws IOException {
		String mimeType = FileUtils.getMimeType(file);
		return ImageConstants.getSupportImageTypes().contains(mimeType);
	}

	public static boolean isSupportImageType(final Path path) throws IOException {
		checkPath(path, "path 不可为 null");
		String mimeType = IOConstants.getDefaultTika().detect(path);
		return ImageConstants.getSupportImageTypes().contains(mimeType);
	}

	public static boolean isSupportImageType(final byte[] bytes) {
		if (ArrayUtils.isEmpty(bytes)) {
			return false;
		}
		String mimeType = IOConstants.getDefaultTika().detect(bytes);
		return ImageConstants.getSupportImageTypes().contains(mimeType);
	}

	public static boolean isSupportImageType(final InputStream inputStream) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		String mimeType = IOConstants.getDefaultTika().detect(inputStream);
		return ImageConstants.getSupportImageTypes().contains(mimeType);
	}

	public static boolean isImage(final File file) throws IOException {
		return FileUtils.isImageType(file);
	}

	public static boolean isImage(final Path path) throws IOException {
		checkPath(path, "path 不可为 null");
		String mimeType = IOConstants.getDefaultTika().detect(path);
		return StringUtils.startsWith(mimeType, IOConstants.IMAGE_MIME_TYPE_PREFIX);
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

	public static String getImageType(final File file) throws IOException {
		String mimeType = FileUtils.getMimeType(file);
		return StringUtils.startsWith(mimeType, IOConstants.IMAGE_MIME_TYPE_PREFIX) ? mimeType : null;
	}

	public static String getImageType(final Path path) throws IOException {
		checkPath(path, "path 不可为 null");
		String mimeType = IOConstants.getDefaultTika().detect(path);
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

	public static ImageSize getImageSize(final File file) throws IOException, ImageProcessingException {
		FileUtils.checkExists(file, "file 不可为 null", true);

		Metadata metadata = ImageMetadataReader.readMetadata(file);
		ImageSize imageSize = parseSizeByMetadata(metadata);
		if (Objects.nonNull(imageSize)) {
			return imageSize;
		}

		String mimeType = getImageType(file);
		if (!ImageConstants.getSupportImageTypes().contains(mimeType)) {
			return null;
		}
		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(file)) {
			return getImageSize(imageInputStream);
		}
	}

	public static ImageSize getImageSize(final File file, final String mimeType) throws IOException, ImageProcessingException {
		FileUtils.checkExists(file, "file 不可为 null", true);
		Validate.notBlank(mimeType, "mimeType 不可为空");

		Metadata metadata = ImageMetadataReader.readMetadata(file);
		ImageSize imageSize = parseSizeByMetadata(metadata);
		if (Objects.nonNull(imageSize)) {
			return imageSize;
		}

		if (!ImageConstants.getSupportImageTypes().contains(mimeType)) {
			return null;
		}
		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(file)) {
			return getImageSize(imageInputStream);
		}
	}

	public static ImageSize getImageSize(final Path path) throws IOException, ImageProcessingException {
		checkPath(path, "path 不可为 null");
		return getImageSize(path.toFile());
	}

	public static ImageSize getImageSize(final Path path, final String mimeType) throws IOException, ImageProcessingException {
		checkPath(path, "path 不可为 null");
		return getImageSize(path.toFile(), mimeType);
	}

	public static ImageSize getImageSize(final byte[] bytes) throws IOException, ImageProcessingException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}

		UnsynchronizedByteArrayInputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
		Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
		ImageSize imageSize = parseSizeByMetadata(metadata);
		if (Objects.nonNull(imageSize)) {
			return imageSize;
		}

		String mimeType = IOConstants.getDefaultTika().detect(bytes);
		return parseImageSize(IOUtils.toUnsynchronizedByteArrayOutputStream(bytes), mimeType);
	}

	public static ImageSize getImageSize(final byte[] bytes, final String mimeType) throws IOException, ImageProcessingException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}
		Validate.notBlank(mimeType, "mimeType 不可为空");

		UnsynchronizedByteArrayInputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
		Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
		ImageSize imageSize = parseSizeByMetadata(metadata);
		if (Objects.nonNull(imageSize)) {
			return imageSize;
		}

		return parseImageSize(IOUtils.toUnsynchronizedByteArrayOutputStream(bytes), mimeType);
	}

	public static ImageSize getImageSize(final InputStream inputStream) throws IOException, ImageProcessingException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		UnsynchronizedByteArrayOutputStream outputStream = IOUtils.toUnsynchronizedByteArrayOutputStream(inputStream);

		Metadata metadata = ImageMetadataReader.readMetadata(outputStream.toInputStream());
		ImageSize imageSize = parseSizeByMetadata(metadata);
		if (Objects.nonNull(imageSize)) {
			return imageSize;
		}

		String mimeType = IOConstants.getDefaultTika().detect(outputStream.toInputStream());
		return parseImageSize(outputStream, mimeType);
	}

	public static ImageSize getImageSize(final InputStream inputStream, final String mimeType) throws IOException, ImageProcessingException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		Validate.notBlank(mimeType, "mimeType 不可为空");
		UnsynchronizedByteArrayOutputStream outputStream = IOUtils.toUnsynchronizedByteArrayOutputStream(inputStream);

		Metadata metadata = ImageMetadataReader.readMetadata(outputStream.toInputStream());
		ImageSize imageSize = parseSizeByMetadata(metadata);
		if (Objects.nonNull(imageSize)) {
			return imageSize;
		}

		return parseImageSize(outputStream, mimeType);
	}

	public static ImageSize getImageSize(final ImageInputStream imageInputStream) throws IOException {
		Validate.notNull(imageInputStream, "imageInputStream 不可为 null");
		Iterator<ImageReader> iterator = ImageIO.getImageReaders(imageInputStream);
		if (!iterator.hasNext()) {
			return null;
		}
		ImageReader reader = iterator.next();
		reader.setInput(imageInputStream);
		ImageSize imageSize = new ImageSize(reader.getWidth(0), reader.getHeight(0));
		reader.dispose();
		return imageSize;
	}

	protected static ImageSize parseSizeByMetadata(final Metadata metadata) {
		Collection<ExifDirectoryBase> exifDirectories = metadata.getDirectoriesOfType(ExifDirectoryBase.class);
		Integer imageWidth = null;
		Integer imageHeight = null;
		/*
		 * 1 图像没有旋转，正常显示。
		 * 2: 图像水平翻转（镜像反转）。
		 * 3: 图像旋转180度。
		 * 4: 图像垂直翻转（上下颠倒）。
		 * 5: 图像先旋转90度再水平翻转。
		 * 6: 图像顺时针旋转90度。
		 * 7: 图像先旋转270度再水平翻转。
		 * 8: 图像逆时针旋转90度（即顺时针旋转270度）
		 */
		int imageOrientation = NORMAL_ORIENTATION;
		for (ExifDirectoryBase exifDirectory : exifDirectories) {
			if (exifDirectory.containsTag(ExifDirectoryBase.TAG_EXIF_IMAGE_WIDTH)) {
				Integer width = exifDirectory.getInteger(ExifDirectoryBase.TAG_EXIF_IMAGE_WIDTH);
				if (Objects.nonNull(width)) {
					imageWidth = width;
				}
			}
			if (exifDirectory.containsTag(ExifDirectoryBase.TAG_EXIF_IMAGE_HEIGHT)) {
				Integer height = exifDirectory.getInteger(ExifDirectoryBase.TAG_EXIF_IMAGE_HEIGHT);
				if (Objects.nonNull(height)) {
					imageHeight = height;
				}
			}
			Integer orientation = exifDirectory.getInteger(ExifDirectoryBase.TAG_ORIENTATION);
			if (Objects.nonNull(orientation)) {
				imageOrientation = orientation;
			}
		}
		if (ObjectUtils.anyNull(imageWidth, imageHeight)) {
			return null;
		}
		return (imageOrientation >= 5 && imageOrientation <= 8) ?
			new ImageSize(imageHeight, imageWidth) : new ImageSize(imageWidth, imageHeight);
	}

	protected static ImageSize parseImageSize(final UnsynchronizedByteArrayOutputStream outputStream,
											  final String mimeType) throws IOException {
		if (!ImageConstants.getSupportImageTypes().contains(mimeType)) {
			return null;
		}
		try (InputStream inputStream = outputStream.toInputStream();
			 InputStream bufferedInputStream = IOUtils.toUnsynchronizedBufferedInputStream(inputStream);
			 ImageInputStream imageInputStream = ImageIO.createImageInputStream(bufferedInputStream)) {
			if (Objects.isNull(imageInputStream)) {
				return null;
			}
			return getImageSize(imageInputStream);
		}
	}

	protected static void checkPath(final Path path, final String message) throws NoSuchFileException {
		Objects.requireNonNull(path, message);
		if (!Files.exists(path) || !Files.isRegularFile(path)) {
			throw new NoSuchFileException(path.toString());
		}
	}
}