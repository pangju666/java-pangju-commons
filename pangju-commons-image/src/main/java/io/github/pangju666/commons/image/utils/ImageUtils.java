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

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.file.FileTypeDirectory;
import io.github.pangju666.commons.image.model.ImageSize;
import io.github.pangju666.commons.io.utils.IOUtils;
import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * 图像处理工具类
 * <p>提供全面的图像处理能力，包括格式检测、尺寸获取、EXIF方向处理等，支持多种输入源（文件、路径、字节数组、流）</p>
 *
 * @author pangju666
 * @see ImageReader
 * @see ImageMetadataReader
 * @since 1.0.0
 */
public class ImageUtils {
	/**
	 * EXIF正常方向标识值
	 * <p>对应未旋转图像的默认方向值，当orientation标签不存在时默认使用该值</p>
	 *
	 * @since 1.0.0
	 */
	protected static final int NORMAL_ORIENTATION = 1;

	protected ImageUtils() {
	}

	public static boolean isSupportType(final File file) throws IOException {
		if (Objects.isNull(file)) {
			return false;
		}
		checkFile(file);

		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(file)) {
			return Objects.nonNull(getMimeType(imageInputStream));
		}
	}

	public static boolean isSupportType(final Path path) throws IOException {
		if (Objects.isNull(path)) {
			return false;
		}
		checkPath(path);

		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(path.toFile())) {
			return Objects.nonNull(getMimeType(imageInputStream));
		}
	}

	public static boolean isSupportType(final byte[] bytes) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return false;
		}

		InputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
			return Objects.nonNull(getMimeType(imageInputStream));
		}
	}

	public static boolean isSupportType(final InputStream inputStream) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
			return Objects.nonNull(getMimeType(imageInputStream));
		}
	}

	public static boolean isSameType(final String mimeType1, final String mimeType2) {
		if (StringUtils.isAnyBlank(mimeType1, mimeType2)) {
			return false;
		}

		Iterator<ImageReader> readers = ImageIO.getImageReadersByMIMEType(mimeType1);
		if (!readers.hasNext()) {
			return false;
		}
		ImageReader reader = readers.next();
		ImageReaderSpi readerSpi = reader.getOriginatingProvider();
		if (Objects.isNull(readerSpi)) {
			reader.dispose();
			return false;
		}
		String[] mimeTypes = readerSpi.getMIMETypes();
		reader.dispose();
		return ArrayUtils.contains(mimeTypes, mimeType2);
	}

	public static boolean isSameType(final File file, final String mimeType) throws IOException {
		if (Objects.isNull(file)) {
			return false;
		}
		if (StringUtils.isBlank(mimeType)) {
			return false;
		}
		checkFile(file);

		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(file)) {
			return ArrayUtils.contains(parseMimeTypes(imageInputStream), mimeType);
		}
	}

	public static boolean isSameType(final Path path, final String mimeType) throws IOException {
		if (Objects.isNull(path)) {
			return false;
		}
		if (StringUtils.isBlank(mimeType)) {
			return false;
		}
		checkPath(path);

		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(path.toFile())) {
			return ArrayUtils.contains(parseMimeTypes(imageInputStream), mimeType);
		}
	}

	public static boolean isSameType(final byte[] bytes, final String mimeType) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return false;
		}
		if (StringUtils.isBlank(mimeType)) {
			return false;
		}

		InputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
			return ArrayUtils.contains(parseMimeTypes(imageInputStream), mimeType);
		}
	}

	public static boolean isSameType(final InputStream inputStream, final String mimeType) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		if (StringUtils.isBlank(mimeType)) {
			return false;
		}

		Validate.notBlank(mimeType, "mimeType 不可为空");
		if (inputStream instanceof BufferedInputStream bufferedInputStream) {
			try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(bufferedInputStream)) {
				return ArrayUtils.contains(parseMimeTypes(imageInputStream), mimeType);
			}
		} else {
			try (BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
				 ImageInputStream imageInputStream = ImageIO.createImageInputStream(bufferedInputStream)) {
				return ArrayUtils.contains(parseMimeTypes(imageInputStream), mimeType);
			}
		}
	}

	public static boolean isSameType(final ImageInputStream imageInputStream, final String mimeType) {
		Validate.notNull(imageInputStream, "imageInputStream 不可为 null");
		if (StringUtils.isBlank(mimeType)) {
			return false;
		}

		return ArrayUtils.contains(parseMimeTypes(imageInputStream), mimeType);
	}

	public static String getMimeType(final File file) throws IOException {
		if (Objects.isNull(file)) {
			return null;
		}
		checkFile(file);

		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(file)) {
			return getMimeType(imageInputStream);
		}
	}

	public static String getMimeType(final Path path) throws IOException {
		if (Objects.isNull(path)) {
			return null;
		}
		checkPath(path);

		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(path.toFile())) {
			return getMimeType(imageInputStream);
		}
	}

	public static String getMimeType(final byte[] bytes) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}

		UnsynchronizedByteArrayInputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
			return getMimeType(imageInputStream);
		}
	}

	public static String getMimeType(final InputStream inputStream) throws IOException {
		Validate.notNull(inputStream, "imageInputStream 不可为 null");

		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
			return getMimeType(imageInputStream);
		}
	}

	public static String getMimeType(final ImageInputStream imageInputStream) {
		Validate.notNull(imageInputStream, "imageInputStream 不可为 null");

		return ArrayUtils.get(parseMimeTypes(imageInputStream), 0, null);
	}

	public static String getMimeType(final Metadata metadata) {
		Validate.notNull(metadata, "metadata 不可为 null");

		Collection<FileTypeDirectory> fileTypeDirectories = metadata.getDirectoriesOfType(FileTypeDirectory.class);
		for (FileTypeDirectory fileTypeDirectory : fileTypeDirectories) {
			if (fileTypeDirectory.containsTag(FileTypeDirectory.TAG_DETECTED_FILE_MIME_TYPE)) {
				return fileTypeDirectory.getString(FileTypeDirectory.TAG_DETECTED_FILE_MIME_TYPE);
			}
		}
		return null;
	}

	public static ImageSize getSize(final File file) throws IOException {
		return getSize(file, true);
	}

	public static ImageSize getSize(final File file, final boolean useMetadata) throws IOException {
		if (Objects.isNull(file)) {
			return null;
		}
		checkFile(file);

		if (useMetadata) {
			try {
				Metadata metadata = ImageMetadataReader.readMetadata(file);
				ImageSize imageSize = getSize(metadata);
				if (Objects.nonNull(imageSize)) {
					return imageSize;
				}
			} catch (ImageProcessingException | IOException ignored) {
			}
		}
		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(file)) {
			if (Objects.isNull(imageInputStream)) {
				return null;
			}
			return getSize(imageInputStream);
		}
	}

	public static ImageSize getSize(final Path path) throws IOException {
		return getSize(path, true);
	}

	public static ImageSize getSize(final Path path, final boolean useMetadata) throws IOException {
		if (Objects.isNull(path)) {
			return null;
		}
		checkPath(path);

		if (useMetadata) {
			try {
				Metadata metadata = ImageMetadataReader.readMetadata(path.toFile());
				ImageSize imageSize = getSize(metadata);
				if (Objects.nonNull(imageSize)) {
					return imageSize;
				}
			} catch (ImageProcessingException | IOException ignored) {
			}
		}
		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(path.toFile())) {
			if (Objects.isNull(imageInputStream)) {
				return null;
			}
			return getSize(imageInputStream);
		}
	}

	public static ImageSize getSize(final byte[] bytes) throws IOException {
		return getSize(bytes, true);
	}

	public static ImageSize getSize(final byte[] bytes, final boolean useMetadata) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}

		UnsynchronizedByteArrayInputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
		return parseSizeByByteArrayInputStream(inputStream, useMetadata);
	}

	public static ImageSize getSize(final InputStream inputStream) throws IOException {
		return getSize(inputStream, true);
	}

	public static ImageSize getSize(final InputStream inputStream, final boolean useMetadata) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		if (inputStream instanceof ByteArrayInputStream || inputStream instanceof UnsynchronizedByteArrayInputStream) {
			return parseSizeByByteArrayInputStream(inputStream, useMetadata);
		}

		if (!useMetadata) {
			try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
				if (Objects.isNull(imageInputStream)) {
					return null;
				}
				return getSize(imageInputStream);
			}
		}

		UnsynchronizedByteArrayOutputStream outputStream = IOUtils.toUnsynchronizedByteArrayOutputStream(inputStream);
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(outputStream.toInputStream());
			ImageSize imageSize = getSize(metadata);
			if (Objects.nonNull(imageSize)) {
				return imageSize;
			}
		} catch (ImageProcessingException | IOException ignored) {
		}
		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(outputStream.toInputStream())) {
			if (Objects.isNull(imageInputStream)) {
				return null;
			}
			return getSize(imageInputStream);
		}
	}

	public static ImageSize getSize(final ImageInputStream imageInputStream) throws IOException {
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

	public static ImageSize getSize(final Metadata metadata) {
		Validate.notNull(metadata, "metadata 不可为 null");

		Collection<ExifDirectoryBase> exifDirectories = metadata.getDirectoriesOfType(ExifDirectoryBase.class);
		Integer imageWidth = null;
		Integer imageHeight = null;
		int imageOrientation = NORMAL_ORIENTATION;
		for (ExifDirectoryBase exifDirectory : exifDirectories) {
			if (exifDirectory instanceof ExifIFD0Directory exifIFD0Directory) {
				if (exifIFD0Directory.containsTag(ExifDirectoryBase.TAG_IMAGE_WIDTH)) {
					Integer width = exifIFD0Directory.getInteger(ExifDirectoryBase.TAG_IMAGE_WIDTH);
					if (Objects.nonNull(width)) {
						imageWidth = width;
					}
				}
				if (exifIFD0Directory.containsTag(ExifDirectoryBase.TAG_IMAGE_HEIGHT)) {
					Integer height = exifIFD0Directory.getInteger(ExifDirectoryBase.TAG_IMAGE_HEIGHT);
					if (Objects.nonNull(height)) {
						imageHeight = height;
					}
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

	public static Integer getExifOrientation(final File file) throws IOException, ImageProcessingException {
		if (Objects.isNull(file)) {
			return null;
		}
		checkFile(file);

		Metadata metadata = ImageMetadataReader.readMetadata(file);
		return getExifOrientation(metadata);
	}

	public static Integer getExifOrientation(final Path path) throws IOException, ImageProcessingException {
		if (Objects.isNull(path)) {
			return null;
		}
		checkPath(path);

		Metadata metadata = ImageMetadataReader.readMetadata(path.toFile());
		return getExifOrientation(metadata);
	}

	public static Integer getExifOrientation(final byte[] bytes) throws IOException, ImageProcessingException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}

		Metadata metadata = ImageMetadataReader.readMetadata(IOUtils.toUnsynchronizedByteArrayInputStream(bytes));
		return getExifOrientation(metadata);
	}

	public static Integer getExifOrientation(final InputStream inputStream) throws IOException, ImageProcessingException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
		return getExifOrientation(metadata);
	}

	public static int getExifOrientation(final Metadata metadata) {
		Validate.notNull(metadata, "metadata 不可为 null");

		Collection<ExifDirectoryBase> exifDirectories = metadata.getDirectoriesOfType(ExifDirectoryBase.class);
		for (ExifDirectoryBase exifDirectory : exifDirectories) {
			Integer orientation = exifDirectory.getInteger(ExifDirectoryBase.TAG_ORIENTATION);
			if (Objects.nonNull(orientation)) {
				return orientation;
			}
		}
		return NORMAL_ORIENTATION;
	}

	protected static ImageSize parseSizeByByteArrayInputStream(InputStream inputStream, boolean useMetadata) throws IOException {
		if (useMetadata) {
			try {
				Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
				ImageSize imageSize = getSize(metadata);
				if (Objects.nonNull(imageSize)) {
					return imageSize;
				}
				inputStream.reset();
			} catch (ImageProcessingException | IOException ignored) {
			}
		}
		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
			if (Objects.isNull(imageInputStream)) {
				return null;
			}
			return getSize(imageInputStream);
		}
	}

	protected static String[] parseMimeTypes(final ImageInputStream imageInputStream) {
		String[] mimeTypes = ArrayUtils.EMPTY_STRING_ARRAY;
		Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
		if (readers.hasNext()) {
			ImageReader reader = readers.next();
			ImageReaderSpi readerSpi = reader.getOriginatingProvider();
			if (Objects.nonNull(readerSpi)) {
				mimeTypes = ArrayUtils.nullToEmpty(readerSpi.getMIMETypes());
			}
			reader.dispose();
		}
		return mimeTypes;
	}

	protected static void checkPath(final Path path) throws NoSuchFileException {
		if (!Files.exists(path) || !Files.isRegularFile(path)) {
			throw new NoSuchFileException(path.toString());
		}
	}

	protected static void checkFile(File file) throws FileNotFoundException {
		if (!file.exists() || !file.isFile()) {
			throw new FileNotFoundException(file.toString());
		}
	}
}