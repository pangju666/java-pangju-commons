package io.github.pangju666.commons.io.utils;

import io.github.pangju666.commons.io.model.ImageSize;
import io.github.pangju666.commons.lang.pool.ConstantPool;
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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

public class ImageUtils {
	protected ImageUtils() {
	}

	public static boolean isImage(final File file) throws IOException {
		String mimeType = FileUtils.getMimeType(file);
		return StringUtils.startsWith(mimeType, ConstantPool.IMAGE_MIME_TYPE_PREFIX);
	}

	public static boolean isImage(final InputStream inputStream) throws IOException {
		String mimeType = FileUtils.getMimeType(inputStream);
		return StringUtils.startsWith(mimeType, ConstantPool.IMAGE_MIME_TYPE_PREFIX);
	}

	public static boolean isImage(final Metadata metadata) {
		String mimeType = getMimeType(metadata);
		return StringUtils.startsWith(mimeType, ConstantPool.IMAGE_MIME_TYPE_PREFIX);
	}

	public static String getMimeType(final Metadata metadata) {
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
		return fileTypeDirectory.getString(FileTypeDirectory.TAG_DETECTED_FILE_MIME_TYPE);
	}

	public static ImageSize getSize(final File file) throws IOException {
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(file);
			ImageSize size = getSizeByMetadata(metadata);
			if (Objects.nonNull(size)) {
				return size;
			}

			ImageReader imageReader = readImageByFormat(file);
			if (Objects.isNull(imageReader)) {
				return null;
			}
			return getSizeByImageReader(imageReader);
		} catch (ImageProcessingException e) {
			ImageReader imageReader = readImageByFormat(file);
			if (Objects.isNull(imageReader)) {
				return null;
			}
			return getSizeByImageReader(imageReader);
		}
	}

	public static ImageSize getSize(final File file, final String mimeType) throws IOException {
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(file);
			ImageSize size = getSizeByMetadata(metadata);
			if (Objects.nonNull(size)) {
				return size;
			}

			ImageReader imageReader = readImageByMimeType(file, mimeType);
			if (Objects.isNull(imageReader)) {
				return null;
			}
			ImageSize imageSize = new ImageSize(imageReader.getWidth(0), imageReader.getHeight(0));
			((ImageInputStream) imageReader.getInput()).close();
			return imageSize;
		} catch (ImageProcessingException e) {
			ImageReader imageReader = readImageByMimeType(file, mimeType);
			if (Objects.isNull(imageReader)) {
				return null;
			}
			return getSizeByImageReader(imageReader);
		}
	}

	public static ImageSize getSize(final InputStream inputStream, final String mimeType) throws IOException {
		ByteArrayInputStream contentArrayInputStream;
		if (inputStream instanceof ByteArrayInputStream byteArrayInputStream) {
			contentArrayInputStream = byteArrayInputStream;
		} else {
			contentArrayInputStream = new ByteArrayInputStream(inputStream.readAllBytes());
		}
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(contentArrayInputStream);
			ImageSize size = getSizeByMetadata(metadata);
			if (Objects.nonNull(size)) {
				return size;
			}

			ImageReader imageReader = readImageByMimeType(inputStream, mimeType);
			if (Objects.isNull(imageReader)) {
				return null;
			}
			return getSizeByImageReader(imageReader);
		} catch (ImageProcessingException e) {
			ImageReader imageReader = readImageByMimeType(inputStream, mimeType);
			if (Objects.isNull(imageReader)) {
				return null;
			}
			return getSizeByImageReader(imageReader);
		}
	}

	public static ImageSize getSize(final Metadata metadata) throws MetadataException {
		ImageSize size = getSizeByMetadata(metadata);
		if (Objects.isNull(size)) {
			throw new MetadataException("无法读取图片尺寸信息");
		}
		return size;
	}

	public static ImageSize scaleSizeByWidth(final Integer imageWidth, final Integer imageHeight, final Integer targetWidth) {
		if (imageWidth > imageHeight) {
			double ratio = imageWidth.doubleValue() / imageHeight.doubleValue();
			return new ImageSize(targetWidth, (int) Math.max(targetWidth / ratio, 1));
		}
		double ratio = imageHeight.doubleValue() / imageWidth.doubleValue();
		return new ImageSize(targetWidth, (int) Math.max(targetWidth * ratio, 1));
	}

	public static ImageSize scaleSizeByHeight(final Integer imageWidth, final Integer imageHeight, final Integer targetHeight) {
		if (imageWidth > imageHeight) {
			double ratio = imageWidth.doubleValue() / imageHeight.doubleValue();
			return new ImageSize((int) Math.max(targetHeight * ratio, 1), targetHeight);
		}
		double ratio = imageHeight.doubleValue() / imageWidth.doubleValue();
		return new ImageSize((int) Math.max(targetHeight / ratio, 1), targetHeight);
	}

	public static ImageSize scaleSize(final Integer imageWidth, final Integer imageHeight,
									  final Integer targetWidth, final Integer targetHeight) {
		double ratio = imageWidth.doubleValue() / imageHeight.doubleValue();
		if (imageWidth > imageHeight) {
			double actualHeight = Math.max(targetWidth / ratio, 1);
			if (actualHeight > targetHeight.doubleValue()) {
				return new ImageSize((int) Math.max(targetHeight * ratio, 1), targetHeight);
			}
			return new ImageSize(targetWidth, (int) actualHeight);
		} else {
			double actualWidth = Math.max(targetHeight / ratio, 1);
			if (actualWidth > targetWidth.doubleValue()) {
				return new ImageSize((int) Math.max(targetHeight * ratio, 1), targetHeight);
			}
			return new ImageSize((int) actualWidth, targetHeight);
		}
	}

	public static ImageReader readImageByFormat(final File file) throws IOException {
		String formatName = FilenameUtils.getExtension(file.getName());
		if (StringUtils.isEmpty(formatName)) {
			return null;
		}
		// try with resource 会报错
		ImageInputStream imageInputStream = ImageIO.createImageInputStream(file);
		return readImageByFormat(imageInputStream, formatName);
	}

	public static ImageReader readImageByMimeType(final File file, final String mimeType) throws IOException {
		// try with resource 会报错
		ImageInputStream imageInputStream = ImageIO.createImageInputStream(file);
		return readImageByMimeType(imageInputStream, mimeType);
	}

	public static ImageReader readImageByMimeType(final InputStream inputStream, final String mimeType) throws IOException {
		// try with resource 会报错
		ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
		return readImageByMimeType(imageInputStream, mimeType);
	}

	public static ImageReader readImageByFormat(final InputStream inputStream, final String formatName) throws IOException {
		// try with resource 会报错
		ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
		return readImageByFormat(imageInputStream, formatName);
	}

	public static ImageReader readImageByMimeType(final ImageInputStream imageInputStream, final String mimeType) {
		Iterator<ImageReader> iterator = ImageIO.getImageReadersByMIMEType(mimeType);
		if (!iterator.hasNext()) {
			return null;
		}
		ImageReader reader = iterator.next();
		reader.setInput(imageInputStream, true);
		return reader;
	}

	public static ImageReader readImageByFormat(final ImageInputStream imageInputStream, final String formatName) {
		Iterator<ImageReader> iterator = ImageIO.getImageReadersByFormatName(formatName);
		if (!iterator.hasNext()) {
			return null;
		}
		ImageReader reader = iterator.next();
		reader.setInput(imageInputStream, true);
		return reader;
	}

	private static ImageSize getSizeByMetadata(Metadata metadata) {
		Collection<ExifDirectoryBase> fileTypeDirectories = metadata.getDirectoriesOfType(ExifDirectoryBase.class);
		var iterator = fileTypeDirectories.iterator();
		if (!iterator.hasNext()) {
			return getSizeByDirectory(metadata);
		}
		ExifDirectoryBase exifDirectory = iterator.next();
		Integer orientation = exifDirectory.getInteger(ExifDirectoryBase.TAG_ORIENTATION);
		Integer width;
		Integer height;
		if (Objects.nonNull(orientation) && (orientation >= 5 && orientation <= 8)) {
			width = exifDirectory.getInteger(ExifDirectoryBase.TAG_IMAGE_HEIGHT);
			height = exifDirectory.getInteger(ExifDirectoryBase.TAG_IMAGE_WIDTH);
		} else {
			width = exifDirectory.getInteger(ExifDirectoryBase.TAG_IMAGE_WIDTH);
			height = exifDirectory.getInteger(ExifDirectoryBase.TAG_IMAGE_HEIGHT);
		}
		if (ObjectUtils.anyNull(width, height)) {
			return getSizeByDirectory(metadata);
		}
		return new ImageSize(width, height);
	}

	private static ImageSize getSizeByDirectory(Metadata metadata) {
		try {
			for (Directory directory : metadata.getDirectories()) {
				if (directory instanceof JpegDirectory jpegDirectory) {
					return new ImageSize(jpegDirectory.getImageWidth(), jpegDirectory.getImageHeight());
				} else if (directory instanceof PngDirectory pngDirectory) {
					return new ImageSize(pngDirectory.getInt(PngDirectory.TAG_IMAGE_WIDTH), pngDirectory.getInt(PngDirectory.TAG_IMAGE_HEIGHT));
				} else if (directory instanceof WebpDirectory webpDirectory) {
					return new ImageSize(webpDirectory.getInt(WebpDirectory.TAG_IMAGE_WIDTH), webpDirectory.getInt(WebpDirectory.TAG_IMAGE_HEIGHT));
				} else if (directory instanceof BmpHeaderDirectory bmpHeaderDirectory) {
					return new ImageSize(bmpHeaderDirectory.getInt(BmpHeaderDirectory.TAG_IMAGE_WIDTH), bmpHeaderDirectory.getInt(BmpHeaderDirectory.TAG_IMAGE_HEIGHT));
				}
			}
			return null;
		} catch (MetadataException e) {
			return null;
		}
	}

	private static ImageSize getSizeByImageReader(ImageReader imageReader) throws IOException {
		ImageSize imageSize = new ImageSize(imageReader.getWidth(0), imageReader.getHeight(0));
		((ImageInputStream) imageReader.getInput()).close();
		return imageSize;
	}
}
