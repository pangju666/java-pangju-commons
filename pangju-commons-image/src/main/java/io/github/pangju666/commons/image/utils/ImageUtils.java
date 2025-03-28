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
import com.drew.metadata.MetadataReader;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.file.FileTypeDirectory;
import io.github.pangju666.commons.image.model.ImageSize;
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
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
	 * <p>完整方向说明：
	 * <ul>
	 *   <li>1: 正常方向</li>
	 *   <li>2: 水平翻转</li>
	 *   <li>3: 旋转180度</li>
	 *   <li>4: 垂直翻转</li>
	 *   <li>5: 旋转90度+水平翻转</li>
	 *   <li>6: 顺时针旋转90度</li>
	 *   <li>7: 旋转270度+水平翻转</li>
	 *   <li>8: 逆时针旋转90度</li>
	 * </ul>
	 *
	 * @since 1.0.0
	 */
	protected static final int NORMAL_ORIENTATION = 1;

	protected ImageUtils() {
	}

	/**
	 * 检测文件是否为支持的图像格式（判断javax.imageio是否支持）
	 *
	 * @param file 待检测的文件对象
	 * @return true表示支持，false表示不支持或文件不存在
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当file为null时抛出
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
	public static boolean isSupportImageType(final File file) throws IOException {
		FileUtils.checkExists(file, "file 不可为 null", true);
		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(file)) {
			return Objects.nonNull(getImageType(imageInputStream));
		}
	}

	/**
	 * 检测路径指向的文件是否为支持的图像格式（判断javax.imageio是否支持）
	 *
	 * @param path 待检测的文件路径
	 * @return true表示支持，false表示不支持或文件不存在
	 * @throws IOException         当文件读取失败时抛出
	 * @throws NoSuchFileException 当路径不存在时抛出
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
	public static boolean isSupportImageType(final Path path) throws IOException {
		checkPath(path, "path 不可为 null");
		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(path.toFile())) {
			return Objects.nonNull(getImageType(imageInputStream));
		}
	}

	/**
	 * 检测字节数组是否为支持的图像格式（判断javax.imageio是否支持）
	 *
	 * @param bytes 图像字节数据
	 * @return true表示支持，false表示不支持或数据为空
	 * @throws IOException 当数据解析失败时抛出
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
	public static boolean isSupportImageType(final byte[] bytes) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return false;
		}

		InputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
			return Objects.nonNull(getImageType(imageInputStream));
		}
	}

	/**
	 * 检测输入流是否为支持的图像格式（判断javax.imageio是否支持）
	 *
	 * @param inputStream 图像输入流
	 * @return true表示支持，false表示不支持
	 * @throws IOException              当流读取失败时抛出
	 * @throws IllegalArgumentException 当inputStream为null时抛出
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
	public static boolean isSupportImageType(final InputStream inputStream) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		if (inputStream instanceof BufferedInputStream bufferedInputStream) {
			try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(bufferedInputStream)) {
				return Objects.nonNull(getImageType(imageInputStream));
			}
		} else {
			try (BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
				 ImageInputStream imageInputStream = ImageIO.createImageInputStream(bufferedInputStream)) {
				return Objects.nonNull(getImageType(imageInputStream));
			}
		}
	}

	/**
	 * 判断两个MIME类型是否对应相同图像格式（基于javax.imageio判断，有些图像有多个MIME类型）
	 *
	 * @param mimeType1 第一个MIME类型
	 * @param mimeType2 第二个MIME类型
	 * @return true表示类型相同，false不同
	 * @throws IllegalArgumentException 任一参数为空时抛出
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
	public static boolean isSameImageType(final String mimeType1, final String mimeType2) {
		Validate.notBlank(mimeType1, "mimeType1 不可为空");
		Validate.notBlank(mimeType2, "mimeType2 不可为空");

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

	/**
	 * 判断文件是否为指定图像格式（基于javax.imageio判断，有些图像有多个MIME类型）
	 *
	 * @param file     图像文件
	 * @param mimeType 目标MIME类型
	 * @return true表示匹配，false不匹配
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 参数不合法时抛出
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
	public static boolean isSameImageType(final File file, final String mimeType) throws IOException {
		FileUtils.checkExists(file, "file 不可为 null", true);
		Validate.notBlank(mimeType, "mimeType 不可为空");
		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(file)) {
			return ArrayUtils.contains(parseImageTypes(imageInputStream), mimeType);
		}
	}

	/**
	 * 判断路径指向文件是否为指定图像格式（基于javax.imageio判断，有些图像有多个MIME类型）
	 *
	 * @param path     文件路径
	 * @param mimeType 目标MIME类型
	 * @return true表示匹配，false不匹配
	 * @throws IOException         当文件读取失败时抛出
	 * @throws NoSuchFileException 路径不存在时抛出
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
	public static boolean isSameImageType(final Path path, final String mimeType) throws IOException {
		checkPath(path, "path 不可为 null");
		Validate.notBlank(mimeType, "mimeType 不可为空");
		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(path.toFile())) {
			return ArrayUtils.contains(parseImageTypes(imageInputStream), mimeType);
		}
	}

	/**
	 * 判断字节数组是否为指定图像格式（基于javax.imageio判断，有些图像有多个MIME类型）
	 *
	 * @param bytes    图像数据
	 * @param mimeType 目标MIME类型
	 * @return true表示匹配，false不匹配
	 * @throws IOException              当数据解析失败时抛出
	 * @throws IllegalArgumentException 参数不合法时抛出
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
	public static boolean isSameImageType(final byte[] bytes, final String mimeType) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return false;
		}
		Validate.notBlank(mimeType, "mimeType 不可为空");

		InputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
			return ArrayUtils.contains(parseImageTypes(imageInputStream), mimeType);
		}
	}

	/**
	 * 判断输入流是否为指定图像格式（基于javax.imageio判断，有些图像有多个MIME类型）
	 *
	 * @param inputStream 图像输入流
	 * @param mimeType    目标MIME类型
	 * @return true表示匹配，false不匹配
	 * @throws IOException              当流读取失败时抛出
	 * @throws IllegalArgumentException 参数不合法时抛出
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
	public static boolean isSameImageType(final InputStream inputStream, final String mimeType) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		Validate.notBlank(mimeType, "mimeType 不可为空");
		if (inputStream instanceof BufferedInputStream bufferedInputStream) {
			try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(bufferedInputStream)) {
				return ArrayUtils.contains(parseImageTypes(imageInputStream), mimeType);
			}
		} else {
			try (BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
				 ImageInputStream imageInputStream = ImageIO.createImageInputStream(bufferedInputStream)) {
				return ArrayUtils.contains(parseImageTypes(imageInputStream), mimeType);
			}
		}
	}

	/**
	 * 判断图像输入流是否为指定图像格式（基于javax.imageio判断，有些图像有多个MIME类型）
	 *
	 * @param imageInputStream 图像输入流
	 * @param mimeType         目标MIME类型
	 * @return true表示匹配，false不匹配
	 * @throws IllegalArgumentException 参数不合法时抛出
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
	public static boolean isSameImageType(final ImageInputStream imageInputStream, final String mimeType) {
		Validate.notNull(imageInputStream, "imageInputStream 不可为 null");
		return ArrayUtils.contains(parseImageTypes(imageInputStream), mimeType);
	}

	/**
	 * 获取文件图像类型（双检测机制）
	 * <p>检测策略：</p>
	 * <ol>
	 *   <li>优先通过元数据解析</li>
	 *   <li>失败后使用ImageIO检测</li>
	 * </ol>
	 *
	 * @param file 图像文件
	 * @return 检测到的MIME类型，未识别返回null
	 * @throws IOException 文件读取失败时抛出
	 * @see ImageReaderSpi
	 * @see MetadataReader
	 * @since 1.0.0
	 */
	public static String getImageType(final File file) throws IOException {
		FileUtils.checkExists(file, "file 不可为 null", true);

		try {
			Metadata metadata = ImageMetadataReader.readMetadata(file);
			String mimeType = getImageType(metadata);
			if (StringUtils.isNotBlank(mimeType)) {
				return mimeType;
			}
		} catch (ImageProcessingException | IOException ignored) {
		}

		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(file)) {
			return getImageType(imageInputStream);
		}
	}

	/**
	 * 获取路径指向文件的图像类型（双检测机制）
	 * <p>检测策略：</p>
	 * <ol>
	 *   <li>优先通过元数据解析</li>
	 *   <li>失败后使用ImageIO检测</li>
	 * </ol>
	 *
	 * @param path 文件路径
	 * @return 检测到的MIME类型，未识别返回null
	 * @throws IOException 文件读取失败时抛出
	 * @see ImageReaderSpi
	 * @see MetadataReader
	 * @since 1.0.0
	 */
	public static String getImageType(final Path path) throws IOException {
		checkPath(path, "path 不可为 null");

		try {
			Metadata metadata = ImageMetadataReader.readMetadata(path.toFile());
			String mimeType = getImageType(metadata);
			if (StringUtils.isNotBlank(mimeType)) {
				return mimeType;
			}
		} catch (ImageProcessingException | IOException ignored) {
		}

		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(path.toFile())) {
			return getImageType(imageInputStream);
		}
	}

	/**
	 * 获取字节数组图像类型（双检测机制）
	 * <p>检测策略：</p>
	 * <ol>
	 *   <li>优先通过元数据解析</li>
	 *   <li>失败后使用ImageIO检测</li>
	 * </ol>
	 *
	 * @param bytes 图像数据
	 * @return 检测到的MIME类型，未识别返回null
	 * @throws IOException 数据解析失败时抛出
	 * @see ImageReaderSpi
	 * @see MetadataReader
	 * @since 1.0.0
	 */
	public static String getImageType(final byte[] bytes) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}
		UnsynchronizedByteArrayInputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);

		try {
			Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
			String mimeType = getImageType(metadata);
			if (StringUtils.isNotBlank(mimeType)) {
				return mimeType;
			}
			inputStream.reset();
		} catch (ImageProcessingException | IOException ignored) {
		}

		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
			return getImageType(imageInputStream);
		}
	}

	/**
	 * 获取输入流图像类型（双检测机制）
	 * <p>检测策略：</p>
	 * <ol>
	 *   <li>优先通过元数据解析</li>
	 *   <li>失败后使用ImageIO检测</li>
	 * </ol>
	 *
	 * @param inputStream 图像输入流
	 * @return 检测到的MIME类型，未识别返回null
	 * @throws IOException 流读取失败时抛出
	 * @see ImageReaderSpi
	 * @see MetadataReader
	 * @since 1.0.0
	 */
	public static String getImageType(final InputStream inputStream) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		if (inputStream instanceof BufferedInputStream bufferedInputStream) {
			try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(bufferedInputStream)) {
				return getImageType(imageInputStream);
			}
		} else {
			try (BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
				 ImageInputStream imageInputStream = ImageIO.createImageInputStream(bufferedInputStream)) {
				return getImageType(imageInputStream);
			}
		}
	}

	/**
	 * 获取图像输入流图像类型（使用ImageIO检测）
	 *
	 * @param imageInputStream 图像输入流
	 * @return 检测到的MIME类型，未识别返回null
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
	public static String getImageType(final ImageInputStream imageInputStream) {
		return ArrayUtils.get(parseImageTypes(imageInputStream), 0, null);
	}

	/**
	 * 从元数据获取图像MIME类型
	 *
	 * @param metadata 图像元数据对象
	 * @return 检测到的MIME类型，未识别返回null
	 * @see Metadata
	 * @since 1.0.0
	 */
	public static String getImageType(final Metadata metadata) {
		Collection<FileTypeDirectory> fileTypeDirectories = metadata.getDirectoriesOfType(FileTypeDirectory.class);
		for (FileTypeDirectory fileTypeDirectory : fileTypeDirectories) {
			if (fileTypeDirectory.containsTag(FileTypeDirectory.TAG_DETECTED_FILE_MIME_TYPE)) {
				return fileTypeDirectory.getString(FileTypeDirectory.TAG_DETECTED_FILE_MIME_TYPE);
			}
		}
		return null;
	}

	/**
	 * 获取文件图像尺寸（自动方向校正、双获取机制）
	 * <p>获取策略：</p>
	 * <ol>
	 *   <li>优先通过元数据解析</li>
	 *   <li>失败后使用ImageIO获取</li>
	 * </ol>
	 *
	 * @param file 图像文件
	 * @return 图像尺寸，未识别返回null
	 * @throws IOException 文件读取失败时抛出
	 * @see ImageReader
	 * @see MetadataReader
	 * @since 1.0.0
	 */
	public static ImageSize getImageSize(final File file) throws IOException {
		FileUtils.checkExists(file, "file 不可为 null", true);

		try {
			Metadata metadata = ImageMetadataReader.readMetadata(file);
			ImageSize imageSize = getImageSize(metadata);
			if (Objects.nonNull(imageSize)) {
				return imageSize;
			}
		} catch (ImageProcessingException | IOException ignored) {
		}

		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(file)) {
			if (Objects.isNull(imageInputStream)) {
				return null;
			}
			return getImageSize(imageInputStream);
		}
	}

	/**
	 * 获取路径指向文件的图像尺寸（自动方向校正、双获取机制）
	 * <p>获取策略：</p>
	 * <ol>
	 *   <li>优先通过元数据解析</li>
	 *   <li>失败后使用ImageIO获取</li>
	 * </ol>
	 *
	 * @param path 文件路径
	 * @return 图像尺寸，未识别返回null
	 * @throws IOException 文件读取失败时抛出
	 * @see ImageReader
	 * @see MetadataReader
	 * @since 1.0.0
	 */
	public static ImageSize getImageSize(final Path path) throws IOException {
		checkPath(path, "path 不可为 null");

		try {
			Metadata metadata = ImageMetadataReader.readMetadata(path.toFile());
			ImageSize imageSize = getImageSize(metadata);
			if (Objects.nonNull(imageSize)) {
				return imageSize;
			}
		} catch (ImageProcessingException | IOException ignored) {
		}

		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(path.toFile())) {
			if (Objects.isNull(imageInputStream)) {
				return null;
			}
			return getImageSize(imageInputStream);
		}
	}

	/**
	 * 获取字节数组图像尺寸（自动方向校正、双获取机制）
	 * <p>获取策略：</p>
	 * <ol>
	 *   <li>优先通过元数据解析</li>
	 *   <li>失败后使用ImageIO获取</li>
	 * </ol>
	 *
	 * @param bytes 图像数据
	 * @return 图像尺寸，未识别返回null
	 * @throws IOException 数据解析失败时抛出
	 * @see ImageReader
	 * @see MetadataReader
	 * @since 1.0.0
	 */
	public static ImageSize getImageSize(final byte[] bytes) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}
		UnsynchronizedByteArrayInputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);

		try {
			Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
			ImageSize imageSize = getImageSize(metadata);
			if (Objects.nonNull(imageSize)) {
				return imageSize;
			}
			inputStream.reset();
		} catch (ImageProcessingException | IOException ignored) {
		}

		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
			if (Objects.isNull(imageInputStream)) {
				return null;
			}
			return getImageSize(imageInputStream);
		}
	}

	/**
	 * 获取输入流图像尺寸（自动方向校正、双获取机制）
	 * <p>获取策略：</p>
	 * <ol>
	 *   <li>优先通过元数据解析</li>
	 *   <li>失败后使用ImageIO获取</li>
	 * </ol>
	 *
	 * @param inputStream 图像输入流
	 * @return 图像尺寸，未识别返回null
	 * @throws IOException 流读取失败时抛出
	 * @see ImageReader
	 * @see MetadataReader
	 * @since 1.0.0
	 */
	public static ImageSize getImageSize(InputStream inputStream) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		UnsynchronizedByteArrayOutputStream outputStream = IOUtils.toUnsynchronizedByteArrayOutputStream(inputStream);
		//TODO 解决大图片解析慢问题
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(outputStream.toInputStream());
			ImageSize imageSize = getImageSize(metadata);
			if (Objects.nonNull(imageSize)) {
				return imageSize;
			}
		} catch (ImageProcessingException | IOException ignored) {
		}

		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(outputStream.toInputStream())) {
			if (Objects.isNull(imageInputStream)) {
				return null;
			}
			return getImageSize(imageInputStream);
		}
	}

	/**
	 * 从图像输入流获取图像尺寸（不支持自动方向校正）
	 *
	 * @param imageInputStream 图像输入流
	 * @return 图像尺寸
	 * @throws IOException 流操作失败时抛出
	 * @see ImageReader
	 * @since 1.0.0
	 */
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

	/**
	 * 获取图像尺寸（自动方向校正）
	 *
	 * @param metadata 图像元数据
	 * @return 图像尺寸，无有效数据返回null
	 * @see Metadata
	 * @since 1.0.0
	 */
	public static ImageSize getImageSize(final Metadata metadata) {
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

	/**
	 * 解析图像MIME类型列表（有些图像有多个MIME类型）
	 *
	 * @param imageInputStream 图像输入流
	 * @return 图像MIME类型列表
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
	protected static String[] parseImageTypes(final ImageInputStream imageInputStream) {
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

	/**
	 * 校验路径有效性
	 *
	 * @param path    待校验路径
	 * @param message 异常信息
	 * @throws NoSuchFileException 路径不存在时抛出
	 * @since 1.0.0
	 */
	protected static void checkPath(final Path path, final String message) throws NoSuchFileException {
		Objects.requireNonNull(path, message);
		if (!Files.exists(path) || !Files.isRegularFile(path)) {
			throw new NoSuchFileException(path.toString());
		}
	}
}