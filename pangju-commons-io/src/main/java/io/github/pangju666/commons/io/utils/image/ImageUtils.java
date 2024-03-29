package io.github.pangju666.commons.io.utils.image;

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
import io.github.pangju666.commons.io.lang.Constants;
import io.github.pangju666.commons.io.model.ImageSize;
import io.github.pangju666.commons.io.utils.file.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
	protected static final Set<String> MIME_TYPES = Set.of(ImageIO.getReaderMIMETypes());
	protected static final Set<String> FILE_SUFFIXES = Set.of(ImageIO.getReaderFileSuffixes());

	protected ImageUtils() {
	}

	/**
	 * 根据预期宽度计算新的实际宽高
	 *
	 * @param imageWidth  图片宽度
	 * @param imageHeight 图片高度
	 * @param targetWidth 预期宽度
	 * @return 实际缩放后的宽高
	 * @since 1.0.0
	 */
	public static ImageSize computeNewSizeByWidth(final int imageWidth, final int imageHeight, final int targetWidth) {
		if (imageWidth > imageHeight) {
			double ratio = (double) imageWidth / imageHeight;
			return new ImageSize(targetWidth, (int) Math.max(targetWidth / ratio, 1));
		}
		double ratio = (double) imageHeight / imageWidth;
		return new ImageSize(targetWidth, (int) Math.max(targetWidth * ratio, 1));
	}

	/**
	 * 根据预期高度计算实际缩放后的宽高
	 *
	 * @param imageWidth   图片宽度
	 * @param imageHeight  图片高度
	 * @param targetHeight 预期高度
	 * @return 实际缩放后的宽高
	 * @since 1.0.0
	 */
	public static ImageSize computeNewSizeByHeight(final int imageWidth, final int imageHeight, final int targetHeight) {
		if (imageWidth > imageHeight) {
			double ratio = (double) imageWidth / imageHeight;
			return new ImageSize((int) Math.max(targetHeight * ratio, 1), targetHeight);
		}
		double ratio = (double) imageHeight / imageWidth;
		return new ImageSize((int) Math.max(targetHeight / ratio, 1), targetHeight);
	}

	/**
	 * 根据预期宽高计算实际缩放后的宽高
	 * <p>如果宽大于高则根据宽计算缩放尺寸，否则按高计算缩放尺寸</p>
	 *
	 * @param imageWidth   图片宽度
	 * @param imageHeight  图片高度
	 * @param targetWidth  预期宽度
	 * @param targetHeight 预期高度
	 * @return 实际缩放后的宽高
	 * @since 1.0.0
	 */
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

	/**
	 * 判断是否为图片文件
	 *
	 * @param file 要解析的文件
	 * @return 是图片文件则返回true，否则为false
	 * @throws IOException 图片读取失败
	 * @see FileUtils#isImageType(File)
	 * @since 1.0.0
	 */
	public static boolean isImage(final File file) throws IOException {
		return FileUtils.isImageType(file);
	}

	/**
	 * 判断是否为图片
	 *
	 * @param bytes 要解析的字节数组
	 * @return 是图片则返回true，否则为false
	 * @since 1.0.0
	 */
	public static boolean isImage(final byte[] bytes) {
		if (ArrayUtils.isEmpty(bytes)) {
			return false;
		}
		String mimeType = Constants.DEFAULT_TIKA.detect(bytes);
		return StringUtils.startsWith(mimeType, Constants.IMAGE_MIME_TYPE_PREFIX);
	}

	/**
	 * 判断是否为图片
	 *
	 * @param inputStream 要解析的输入流
	 * @return 是图片则返回true，否则为false
	 * @since 1.0.0
	 */
	public static boolean isImage(final InputStream inputStream) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		String mimeType = Constants.DEFAULT_TIKA.detect(inputStream);
		return StringUtils.startsWith(mimeType, Constants.IMAGE_MIME_TYPE_PREFIX);
	}

	/**
	 * 判断是否为图片
	 *
	 * @param metadata 图片元数据
	 * @return 是图片则返回true，否则为false
	 * @see Metadata
	 * @since 1.0.0
	 */
	public static boolean isImage(final Metadata metadata) {
		Validate.notNull(metadata, "metadata 不可为 null");
		String mimeType = getMimeType(metadata);
		return StringUtils.startsWith(mimeType, Constants.IMAGE_MIME_TYPE_PREFIX);
	}

	/**
	 * 根据图片元数据获取MIME类型
	 *
	 * @param metadata 图片元数据
	 * @return 图片MIME类型，如果不是支持的类型则返回 null
	 * @since 1.0.0
	 */
	public static String getMimeType(final Metadata metadata) {
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
		return StringUtils.startsWith(mimeType, Constants.IMAGE_MIME_TYPE_PREFIX) ? mimeType : null;
	}

	/**
	 * 获取图片尺寸
	 *
	 * @param file 图片文件
	 * @throws IOException 图片文件读取失败时
	 * @return 图片尺寸，读取失败或不支持该格式则返回 null
	 * @since 1.0.0
	 */
	public static ImageSize getSize(final File file) throws IOException {
		FileUtils.validateFile(file, "file 不可为 null");
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(file);
			ImageSize size = getSizeByMetadata(metadata);
			if (Objects.nonNull(size)) {
				return size;
			}
		} catch (ImageProcessingException ignored) {
		}
		String suffix = getSuffix(file);
		if (Objects.isNull(suffix)) {
			return null;
		}
		Iterator<ImageReader> iterator = ImageIO.getImageReadersBySuffix(suffix);
		ImageInputStream imageInputStream = ImageIO.createImageInputStream(file);
		if (Objects.isNull(imageInputStream)) {
			return null;
		}
		ImageReader reader = getImageReader(iterator, imageInputStream);
		ImageSize imageSize = null;
		if (Objects.nonNull(reader)) {
			imageSize = getSize(reader);
		}
		imageInputStream.close();
		return imageSize;
	}

	/**
	 * 根据图片类型获取图片尺寸
	 *
	 * @param file 图片文件
	 * @param mimeType 图片MIME类型，如果不是图片类型则返回 null
	 * @throws IOException 图片文件读取失败时
	 * @return 图片尺寸，读取失败则返回 null
	 * @since 1.0.0
	 */
	public static ImageSize getSize(final File file, final String mimeType) throws IOException {
		FileUtils.validateFile(file, "file 不可为 null");
		Validate.notBlank(mimeType, "mimeType 不可为空");
		if (!MIME_TYPES.contains(mimeType)) {
			return null;
		}
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(file);
			ImageSize size = getSizeByMetadata(metadata);
			if (Objects.nonNull(size)) {
				return size;
			}
		} catch (ImageProcessingException ignored) {
		}
		ImageInputStream imageInputStream = ImageIO.createImageInputStream(file);
		if (Objects.isNull(imageInputStream)) {
			return null;
		}
		ImageSize imageSize = getSizeByImageInputStreamAndMimeType(imageInputStream, mimeType);
		imageInputStream.close();
		return imageSize;
	}

	/**
	 * 获取图片尺寸
	 *
	 * @param bytes    字节数组
	 * @param mimeType 图片MIME类型
	 * @return 图片尺寸，如果不是支持的类型则返回 null
	 * @throws IOException 图片文件读取失败时
	 * @since 1.0.0
	 */
	public static ImageSize getSize(final byte[] bytes, final String mimeType) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}
		Validate.notBlank(mimeType, "mimeType 不可为空");
		if (!MIME_TYPES.contains(mimeType)) {
			return null;
		}
		UnsynchronizedByteArrayOutputStream outputStream = UnsynchronizedByteArrayOutputStream.builder().get();
		outputStream.write(bytes);
		return getSizeByOutputStreamAndMimeType(outputStream, mimeType);
	}

	/**
	 * 获取图片尺寸
	 *
	 * @param inputStream 图片输入流
	 * @param mimeType 图片MIME类型
	 * @return 图片尺寸，如果不是支持的类型则返回 null
	 * @throws IOException 图片读取失败时
	 * @since 1.0.0
	 */
	public static ImageSize getSize(final InputStream inputStream, final String mimeType) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		Validate.notBlank(mimeType, "mimeType 不可为空");
		if (!MIME_TYPES.contains(mimeType)) {
			return null;
		}
		UnsynchronizedByteArrayOutputStream outputStream = UnsynchronizedByteArrayOutputStream.builder().get();
		inputStream.transferTo(outputStream);
		return getSizeByOutputStreamAndMimeType(outputStream, mimeType);
	}

	/**
	 * 获取图片尺寸
	 *
	 * @param imageInputStream 图片输入流
	 * @param mimeType         图片MIME类型
	 * @return 图片尺寸，如果不是支持的类型则返回 null
	 * @throws IOException 图片读取失败时
	 * @since 1.0.0
	 */
	public static ImageSize getSize(ImageInputStream imageInputStream, String mimeType) throws IOException {
		Validate.notNull(imageInputStream, "imageInputStream 不可为 null");
		Validate.notBlank(mimeType, "mimeType 不可为空");
		if (!MIME_TYPES.contains(mimeType)) {
			return null;
		}
		return getSizeByImageInputStreamAndMimeType(imageInputStream, mimeType);
	}

	/**
	 * 获取图片尺寸
	 *
	 * @param imageReader 图片读取器
	 * @return 图片尺寸
	 * @throws IOException 图片读取失败时
	 * @since 1.0.0
	 */
	public static ImageSize getSize(ImageReader imageReader) throws IOException {
		Validate.notNull(imageReader, "imageReader 不可为 null");
		return new ImageSize(imageReader.getWidth(0), imageReader.getHeight(0));
	}

	/**
	 * 根据图片元数据获取图片尺寸
	 *
	 * @param metadata 图片元数据
	 * @return 图片尺寸，读取失败则返回 null
	 * @throws MetadataException 不包含图片尺寸元信息时
	 * @since 1.0.0
	 */
	public static ImageSize getSize(final Metadata metadata) throws MetadataException {
		Validate.notNull(metadata, "metadata 不可为 null");
		ImageSize size = getSizeByMetadata(metadata);
		if (Objects.isNull(size)) {
			throw new MetadataException("无法读取图片尺寸信息");
		}
		return size;
	}

	/**
	 * 读取图片
	 *
	 * @param file 图片文件
	 * @return 图片读取器，如果不是支持的类型则返回 null
	 * @throws IOException 图片读取失败时
	 * @since 1.0.0
	 */
	public static ImageReader readImage(final File file) throws IOException {
		FileUtils.validateFile(file, "file 不可为 null");
		String suffix = getSuffix(file);
		if (Objects.isNull(suffix)) {
			return null;
		}
		// try with resource 会报错
		ImageInputStream imageInputStream = ImageIO.createImageInputStream(file);
		if (Objects.isNull(imageInputStream)) {
			return null;
		}
		Iterator<ImageReader> iterator = ImageIO.getImageReadersBySuffix(suffix);
		return getImageReader(iterator, imageInputStream);
	}

	/**
	 * 读取图片
	 *
	 * @param file     图片文件
	 * @param mimeType 图片MIME类型
	 * @return 图片读取器，如果不是支持的类型则返回 null
	 * @throws IOException 图片读取失败时
	 * @since 1.0.0
	 */
	public static ImageReader readImage(final File file, final String mimeType) throws IOException {
		FileUtils.validateFile(file, "file 不可为 null");
		Validate.notBlank(mimeType, "mimeType 不可为空");
		if (!MIME_TYPES.contains(mimeType)) {
			return null;
		}
		// try with resource 会报错
		ImageInputStream imageInputStream = ImageIO.createImageInputStream(file);
		if (Objects.isNull(imageInputStream)) {
			return null;
		}
		Iterator<ImageReader> iterator = ImageIO.getImageReadersByMIMEType(mimeType);
		return getImageReader(iterator, imageInputStream);
	}

	/**
	 * 读取图片
	 *
	 * @param bytes    图片字节流
	 * @param mimeType 图片MIME类型
	 * @return 图片读取器，如果不是支持的类型则返回 null
	 * @throws IOException 图片读取失败时
	 * @since 1.0.0
	 */
	public static ImageReader readImage(final byte[] bytes, final String mimeType) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}
		Validate.notBlank(mimeType, "mimeType 不可为空");
		InputStream inputStream = UnsynchronizedByteArrayInputStream
			.builder()
			.setByteArray(bytes)
			.setOffset(0)
			.setLength(bytes.length)
			.get();
		try (UnsynchronizedBufferedInputStream bufferedInputStream = buildBufferInputStream(inputStream)) {
			return readImage(bufferedInputStream, mimeType);
		}
	}

	/**
	 * 读取图片
	 *
	 * @param inputStream 图片输入流
	 * @param mimeType    图片MIME类型
	 * @return 图片读取器，如果不是支持的类型则返回 null
	 * @throws IOException 图片读取失败时
	 * @since 1.0.0
	 */
	public static ImageReader readImage(final InputStream inputStream, final String mimeType) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		Validate.notBlank(mimeType, "mimeType 不可为空");
		if (!MIME_TYPES.contains(mimeType)) {
			return null;
		}
		// try with resource 会报错
		ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
		if (Objects.isNull(imageInputStream)) {
			return null;
		}
		Iterator<ImageReader> iterator = ImageIO.getImageReadersByMIMEType(mimeType);
		return getImageReader(iterator, imageInputStream);
	}

	/**
	 * 读取图片
	 *
	 * @param imageInputStream 图片输入流
	 * @param mimeType 图片MIME类型
	 * @return 图片读取器，如果不是支持的类型则返回 null
	 * @since 1.0.0
	 */
	public static ImageReader readImage(final ImageInputStream imageInputStream, final String mimeType) {
		Validate.notNull(imageInputStream, "imageInputStream 不可为 null");
		Validate.notBlank(mimeType, "mimeType 不可为空");
		if (!MIME_TYPES.contains(mimeType)) {
			return null;
		}
		Iterator<ImageReader> iterator = ImageIO.getImageReadersByMIMEType(mimeType);
		return getImageReader(iterator, imageInputStream);
	}

	protected static ImageSize getSizeByOutputStreamAndMimeType(final UnsynchronizedByteArrayOutputStream outputStream,
																final String mimeType) throws IOException {
		try (InputStream tmpInputStream = outputStream.toInputStream();
			 InputStream bufferedInputStream = buildBufferInputStream(tmpInputStream)) {
			Metadata metadata = ImageMetadataReader.readMetadata(bufferedInputStream);
			ImageSize size = getSizeByMetadata(metadata);
			if (Objects.nonNull(size)) {
				return size;
			}
		} catch (ImageProcessingException ignored) {
		}
		try (InputStream tmpInputStream = outputStream.toInputStream();
			 InputStream bufferedInputStream = buildBufferInputStream(tmpInputStream)) {
			ImageInputStream imageInputStream = ImageIO.createImageInputStream(bufferedInputStream);
			if (Objects.isNull(imageInputStream)) {
				return null;
			}
			ImageSize imageSize = getSizeByImageInputStreamAndMimeType(imageInputStream, mimeType);
			imageInputStream.close();
			return imageSize;
		}
	}

	protected static ImageSize getSizeByImageInputStreamAndMimeType(ImageInputStream imageInputStream, String mimeType) throws IOException {
		Iterator<ImageReader> iterator = ImageIO.getImageReadersByMIMEType(mimeType);
		ImageReader reader = getImageReader(iterator, imageInputStream);
		ImageSize imageSize = null;
		if (Objects.nonNull(reader)) {
			imageSize = getSize(reader);
		}
		return imageSize;
	}

	protected static ImageSize getSizeByMetadata(Metadata metadata) {
		Collection<ExifDirectoryBase> fileTypeDirectories = metadata.getDirectoriesOfType(ExifDirectoryBase.class);
		var iterator = fileTypeDirectories.iterator();
		if (!iterator.hasNext()) {
			return getSizeByDirectories(metadata.getDirectories());
		}

		ExifDirectoryBase exifDirectory = iterator.next();
		Integer width = exifDirectory.getInteger(ExifDirectoryBase.TAG_IMAGE_WIDTH);
		Integer height = exifDirectory.getInteger(ExifDirectoryBase.TAG_IMAGE_HEIGHT);
		if (ObjectUtils.anyNull(width, height)) {
			ImageSize size = getSizeByDirectories(metadata.getDirectories());
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

	protected static ImageSize getSizeByDirectories(Iterable<Directory> directories) {
		try {
			for (Directory directory : directories) {
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

	public static UnsynchronizedBufferedInputStream buildBufferInputStream(InputStream inputStream) throws IOException {
		return new UnsynchronizedBufferedInputStream.Builder()
			.setInputStream(inputStream)
			.get();
	}

	protected static ImageReader getImageReader(Iterator<ImageReader> imageReaders,
												ImageInputStream inputStream) {
		if (!imageReaders.hasNext()) {
			return null;
		}
		ImageReader reader = imageReaders.next();
		reader.setInput(inputStream, true);
		return reader;
	}

	protected static String getSuffix(File file) {
		String extension = FilenameUtils.getExtension(file.getName());
		String suffix = null;
		for (String fileSuffix : FILE_SUFFIXES) {
			if (StringUtils.equalsIgnoreCase(fileSuffix, extension)) {
				suffix = fileSuffix;
			}
		}
		return suffix;
	}
}
