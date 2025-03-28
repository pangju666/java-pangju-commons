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

	/**
	 * 检查指定文件是否是ImageIO支持的图像类型
	 *
	 * @param file 要检查的文件对象，允许为null，null将返回false
	 * @return 如果文件是支持的图像类型返回true，否则返回false
	 * @throws IOException 当文件不存在或读取失败时抛出
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
	public static boolean isSupportType(final File file) throws IOException {
		if (Objects.isNull(file)) {
			return false;
		}
		checkFile(file);

		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(file)) {
			return Objects.nonNull(getMimeType(imageInputStream));
		}
	}

	/**
	 * 检查指定路径对应的文件是否是ImageIO支持的图像类型
	 *
	 * @param path 要检查的文件路径，允许为null，null将返回false
	 * @return 如果文件是支持的图像类型返回true，否则返回false
	 * @throws IOException 当路径不存在或读取失败时抛出
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
	public static boolean isSupportType(final Path path) throws IOException {
		if (Objects.isNull(path)) {
			return false;
		}
		checkPath(path);

		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(path.toFile())) {
			return Objects.nonNull(getMimeType(imageInputStream));
		}
	}

	/**
	 * 检查字节数组数据是否是ImageIO支持的图像类型
	 *
	 * @param bytes 要检查的字节数组，允许为空，空将返回false
	 * @return 如果数据是支持的图像类型返回true，否则返回false
	 * @throws IOException 当读取数据失败时抛出
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
	public static boolean isSupportType(final byte[] bytes) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return false;
		}

		InputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
			return Objects.nonNull(getMimeType(imageInputStream));
		}
	}

	/**
	 * 检查输入流是否是ImageIO支持的图像类型
	 *
	 * @param inputStream 要检查的输入流，不可为null
	 * @return 如果流数据是支持的图像类型返回true，否则返回false
	 * @throws IOException              当读取流失败时抛出
	 * @throws IllegalArgumentException 当输入流为null时抛出
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
	public static boolean isSupportType(final InputStream inputStream) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
			return Objects.nonNull(getMimeType(imageInputStream));
		}
	}

	/**
	 * 判断两个MIME类型是否属于同一图像类型（有些图像属于多个MIME类型）
	 *
	 * @param mimeType1 第一个MIME类型，允许为空，空将返回false
	 * @param mimeType2 第二个MIME类型，允许为空，空将返回false
	 * @return 如果属于同一类型返回true，否则返回false
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
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

	/**
	 * 判断文件是否与指定MIME类型匹配（有些图像属于多个MIME类型）
	 *
	 * @param file     要检查的文件对象，允许为null，null将返回false
	 * @param mimeType 要匹配的MIME类型，允许为空，空将返回false
	 * @return 如果匹配返回true，否则返回false
	 * @throws IOException 当文件不存在或读取失败时抛出
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
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

	/**
	 * 判断路径对应的文件是否与指定MIME类型匹配（有些图像属于多个MIME类型）
	 *
	 * @param path     要检查的文件路径，允许为null，null将返回false
	 * @param mimeType 要匹配的MIME类型，允许为空，空将返回false
	 * @return 如果匹配返回true，否则返回false
	 * @throws IOException              当路径不存在或读取失败时抛出
	 * @throws IllegalArgumentException 当输入流为null时抛出
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
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

	/**
	 * 判断字节数组数据是否与指定MIME类型匹配（有些图像属于多个MIME类型）
	 *
	 * @param bytes    要检查的字节数组，允许为空，空将返回false
	 * @param mimeType 要匹配的MIME类型，允许为空，空将返回false
	 * @return 如果匹配返回true，否则返回false
	 * @throws IOException 当读取数据失败时抛出
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
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

	/**
	 * 判断输入流是否与指定MIME类型匹配（有些图像属于多个MIME类型）
	 *
	 * @param inputStream 要检查的输入流，不可为null
	 * @param mimeType    要匹配的MIME类型，允许为空，空将返回false
	 * @return 如果匹配返回true，否则返回false
	 * @throws IOException              当读取流失败时抛出
	 * @throws IllegalArgumentException 当输入流为null时抛出
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
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

	/**
	 * 判断图像输入流是否与指定MIME类型匹配（有些图像属于多个MIME类型）
	 *
	 * @param imageInputStream 要检查的图像输入流，不可为null
	 * @param mimeType         要匹配的MIME类型，允许为空，空将返回false
	 * @return 如果匹配返回true，否则返回false
	 * @throws IllegalArgumentException 当图像输入流为null时抛出
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
	public static boolean isSameType(final ImageInputStream imageInputStream, final String mimeType) {
		Validate.notNull(imageInputStream, "imageInputStream 不可为 null");
		if (StringUtils.isBlank(mimeType)) {
			return false;
		}

		return ArrayUtils.contains(parseMimeTypes(imageInputStream), mimeType);
	}

	/**
	 * 获取文件的MIME类型（使用ImageIO获取）
	 *
	 * @param file 要检查的文件对象，允许为null，null将返回false
	 * @return 文件的MIME类型，无法获取时返回null
	 * @throws IOException 当文件不存在或读取失败时抛出
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
	public static String getMimeType(final File file) throws IOException {
		if (Objects.isNull(file)) {
			return null;
		}
		checkFile(file);

		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(file)) {
			return getMimeType(imageInputStream);
		}
	}

	/**
	 * 获取路径对应文件的MIME类型（使用ImageIO获取）
	 *
	 * @param path 要检查的文件路径，允许为null，null将返回false
	 * @return 文件的MIME类型，无法获取时返回null
	 * @throws IOException 当路径不存在或读取失败时抛出
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
	public static String getMimeType(final Path path) throws IOException {
		if (Objects.isNull(path)) {
			return null;
		}
		checkPath(path);

		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(path.toFile())) {
			return getMimeType(imageInputStream);
		}
	}

	/**
	 * 获取字节数组数据的MIME类型（使用ImageIO获取）
	 *
	 * @param bytes 要检查的字节数组，允许为空，空将返回false
	 * @return 数据的MIME类型，无法获取时返回null
	 * @throws IOException 当读取数据失败时抛出
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
	public static String getMimeType(final byte[] bytes) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}

		UnsynchronizedByteArrayInputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
			return getMimeType(imageInputStream);
		}
	}

	/**
	 * 获取输入流的MIME类型（使用ImageIO获取）
	 *
	 * @param inputStream 要检查的输入流，不可为null
	 * @return 流的MIME类型，无法获取时返回null
	 * @throws IOException              当读取流失败时抛出
	 * @throws IllegalArgumentException 当输入流为null时抛出
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
	public static String getMimeType(final InputStream inputStream) throws IOException {
		Validate.notNull(inputStream, "imageInputStream 不可为 null");

		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
			return getMimeType(imageInputStream);
		}
	}

	/**
	 * 获取图像输入流的MIME类型（使用ImageIO获取）
	 *
	 * @param imageInputStream 要检查的图像输入流，不可为null
	 * @return 流的MIME类型，无法获取时返回null
	 * @throws IllegalArgumentException 当图像输入流为null时抛出
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
	public static String getMimeType(final ImageInputStream imageInputStream) {
		Validate.notNull(imageInputStream, "imageInputStream 不可为 null");

		return ArrayUtils.get(parseMimeTypes(imageInputStream), 0, null);
	}

	/**
	 * 从元数据中获取MIME类型
	 *
	 * @param metadata 图像元数据对象，不可为null
	 * @return MIME类型，无法获取时返回null
	 * @throws IllegalArgumentException 当元数据为null时抛出
	 * @see ImageReaderSpi
	 * @since 1.0.0
	 */
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

	/**
	 * 获取文件的图像尺寸（自动处理EXIF方向）
	 *
	 * <p>注意事项：
	 * <ul>
	 *     <li>文件对象允许为null，null将返回null</li>
	 *     <li>文件必须存在且为常规文件</li>
	 * </ul></p>
	 *
	 * @param file 要检查的文件对象，允许为null
	 * @return 包含宽度和高度的ImageSize对象，无法获取时返回null
	 * @throws IOException 当文件不存在或读取失败时抛出
	 * @see com.drew.metadata.MetadataReader
	 * @see ImageReader
	 * @since 1.0.0
	 */
	public static ImageSize getSize(final File file) throws IOException {
		return getSize(file, true);
	}

	/**
	 * 获取文件的图像尺寸（可选择是否优先使用元数据）
	 *
	 * <p>注意事项：
	 * <ul>
	 *     <li>文件对象允许为null，null将返回null</li>
	 *     <li>文件必须存在且为常规文件</li>
	 *     <li>超过100MB且不考虑自动修正图像方向时，useMetadata建议为false</li>
	 * </ul></p>
	 *
	 * @param file        要检查的文件对象，允许为null
	 * @param useMetadata 是否优先使用元数据获取尺寸（为true则会自动处理EXIF方向）
	 * @return 包含宽度和高度的ImageSize对象，无法获取时返回null
	 * @throws IOException 当文件不存在或读取失败时抛出
	 * @see com.drew.metadata.MetadataReader
	 * @see ImageReader
	 * @since 1.0.0
	 */
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

	/**
	 * 获取路径对应文件的图像尺寸（自动处理EXIF方向）
	 *
	 * <p>注意事项：
	 * <ul>
	 *     <li>路径对象允许为null，null将返回null</li>
	 *     <li>路径必须指向存在的常规文件</li>
	 * </ul></p>
	 *
	 * @param path 要检查的文件路径，允许为null
	 * @return 包含宽度和高度的ImageSize对象，无法获取时返回null
	 * @throws IOException 当路径不存在或读取失败时抛出
	 * @see com.drew.metadata.MetadataReader
	 * @see ImageReader
	 * @since 1.0.0
	 */
	public static ImageSize getSize(final Path path) throws IOException {
		return getSize(path, true);
	}

	/**
	 * 获取路径对应文件的图像尺寸（可选择是否优先使用元数据）
	 *
	 * <p>注意事项：
	 * <ul>
	 *     <li>路径对象允许为null，null将返回null</li>
	 *     <li>路径必须指向存在的常规文件</li>
	 *     <li>超过100MB且不考虑自动修正图像方向时，useMetadata建议为false</li>
	 * </ul></p>
	 *
	 * @param path        要检查的文件路径，允许为null
	 * @param useMetadata 是否优先使用元数据获取尺寸（为true则会自动处理EXIF方向）
	 * @return 包含宽度和高度的ImageSize对象，无法获取时返回null
	 * @throws IOException 当路径不存在或读取失败时抛出
	 * @see com.drew.metadata.MetadataReader
	 * @see ImageReader
	 * @since 1.0.0
	 */
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

	/**
	 * 获取字节数组数据的图像尺寸（自动处理EXIF方向）
	 *
	 * <p>注意事项：
	 * <ul>
	 *     <li>字节数组允许为空，空将返回null</li>
	 * </ul></p>
	 *
	 * @param bytes 要检查的字节数组，允许为空
	 * @return 包含宽度和高度的ImageSize对象，无法获取时返回null
	 * @throws IOException 当读取数据失败时抛出
	 * @see com.drew.metadata.MetadataReader
	 * @see ImageReader
	 * @since 1.0.0
	 */
	public static ImageSize getSize(final byte[] bytes) throws IOException {
		return getSize(bytes, true);
	}

	/**
	 * 获取字节数组数据的图像尺寸（可选择是否优先使用元数据）
	 *
	 * <p>注意事项：
	 * <ul>
	 *     <li>字节数组允许为空，空将返回null</li>
	 *     <li>超过100MB且不考虑自动修正图像方向时，useMetadata建议为false</li>
	 * </ul>
	 * </p>
	 *
	 * @param bytes       要检查的字节数组，允许为空
	 * @param useMetadata 是否优先使用元数据获取尺寸（为true则会自动处理EXIF方向）
	 * @return 包含宽度和高度的ImageSize对象，无法获取时返回null
	 * @throws IOException 当读取数据失败时抛出
	 * @see com.drew.metadata.MetadataReader
	 * @see ImageReader
	 * @since 1.0.0
	 */
	public static ImageSize getSize(final byte[] bytes, final boolean useMetadata) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}

		UnsynchronizedByteArrayInputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
		return parseSizeByByteArrayInputStream(inputStream, useMetadata);
	}

	/**
	 * 获取输入流的图像尺寸（自动处理EXIF方向）
	 *
	 * <p>注意事项：
	 * <ul>
	 *     <li>输入流不可为null</li>
	 *     <li>流读取后不会被关闭</li>
	 * </ul></p>
	 *
	 * @param inputStream 要检查的输入流，不可为null
	 * @return 包含宽度和高度的ImageSize对象，无法获取时返回null
	 * @throws IOException 当读取流失败时抛出
	 * @throws IllegalArgumentException 当输入流为null时抛出
	 * @since 1.0.0
	 * @see com.drew.metadata.MetadataReader
	 * @see ImageReader
	 */
	public static ImageSize getSize(final InputStream inputStream) throws IOException {
		return getSize(inputStream, true);
	}

	/**
	 * 获取输入流的图像尺寸（可选择是否优先使用元数据）
	 *
	 * <p>注意事项：
	 * <ul>
	 *     <li>输入流不可为null</li>
	 *     <li>流读取后不会被关闭</li>
	 *     <li>超过100MB且不考虑自动修正图像方向时，useMetadata建议为false</li>
	 * </ul></p>
	 *
	 * @param inputStream 要检查的输入流，不可为null
	 * @param useMetadata 是否优先使用元数据获取尺寸（为true则会自动处理EXIF方向）
	 * @return 包含宽度和高度的ImageSize对象，无法获取时返回null
	 * @throws IOException 当读取流失败时抛出
	 * @throws IllegalArgumentException 当输入流为null时抛出
	 * @since 1.0.0
	 * @see com.drew.metadata.MetadataReader
	 * @see ImageReader
	 */
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

	/**
	 * 获取图像输入流的图像尺寸（不修正图像方向）
	 *
	 * @param imageInputStream 要检查的图像输入流，不可为null
	 * @return 包含宽度和高度的ImageSize对象，无法获取时返回null
	 * @throws IOException 当读取流失败时抛出
	 * @throws IllegalArgumentException 当图像输入流为null时抛出
	 * @since 1.0.0
	 * @see ImageReader
	 */
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

	/**
	 * 从元数据中获取图像尺寸（自动处理EXIF方向）
	 *
	 * <p>注意事项：
	 * <ul>
	 *     <li>元数据对象不可为null</li>
	 *     <li>当方向值为5-8时（需要90度旋转的情况），自动交换宽高值</li>
	 * </ul></p>
	 *
	 * @param metadata 图像元数据对象，不可为null
	 * @return 包含宽度和高度的ImageSize对象，无法获取时返回null
	 * @throws IllegalArgumentException 当元数据为null时抛出
	 * @since 1.0.0
	 * @see com.drew.metadata.MetadataReader
	 */
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

	/**
	 * 获取文件的EXIF方向信息
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
	 * @param file 要检查的文件对象，允许为null，null将返回false
	 * @return EXIF方向值，未找到时返回{@link #NORMAL_ORIENTATION}
	 * @throws IOException              当文件读取失败时抛出
	 * @throws ImageProcessingException 当图像处理异常时抛出
	 * @see com.drew.metadata.MetadataReader
	 * @since 1.0.0
	 */
	public static Integer getExifOrientation(final File file) throws IOException, ImageProcessingException {
		if (Objects.isNull(file)) {
			return null;
		}
		checkFile(file);

		Metadata metadata = ImageMetadataReader.readMetadata(file);
		return getExifOrientation(metadata);
	}

	/**
	 * 获取路径对应文件的EXIF方向信息
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
	 * @param path 要检查的文件路径，允许为null，null将返回false
	 * @return EXIF方向值，未找到时返回{@link #NORMAL_ORIENTATION}
	 * @throws IOException              当路径不存在或读取失败时抛出
	 * @throws ImageProcessingException 当图像处理异常时抛出
	 * @see com.drew.metadata.MetadataReader
	 * @since 1.0.0
	 */
	public static Integer getExifOrientation(final Path path) throws IOException, ImageProcessingException {
		if (Objects.isNull(path)) {
			return null;
		}
		checkPath(path);

		Metadata metadata = ImageMetadataReader.readMetadata(path.toFile());
		return getExifOrientation(metadata);
	}

	/**
	 * 获取字节数组数据的EXIF方向信息
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
	 * @param bytes 要检查的字节数组，允许为空，空将返回false
	 * @return EXIF方向值，未找到时返回{@link #NORMAL_ORIENTATION}
	 * @throws IOException              当读取数据失败时抛出
	 * @throws ImageProcessingException 当图像处理异常时抛出
	 * @see com.drew.metadata.MetadataReader
	 * @since 1.0.0
	 */
	public static Integer getExifOrientation(final byte[] bytes) throws IOException, ImageProcessingException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}

		Metadata metadata = ImageMetadataReader.readMetadata(IOUtils.toUnsynchronizedByteArrayInputStream(bytes));
		return getExifOrientation(metadata);
	}

	/**
	 * 获取输入流的EXIF方向信息
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
	 * @param inputStream 要检查的输入流，不可为null
	 * @return EXIF方向值，未找到时返回{@link #NORMAL_ORIENTATION}
	 * @throws IOException              当读取流失败时抛出
	 * @throws ImageProcessingException 当图像处理异常时抛出
	 * @throws IllegalArgumentException 当输入流为null时抛出
	 * @see com.drew.metadata.MetadataReader
	 * @since 1.0.0
	 */
	public static Integer getExifOrientation(final InputStream inputStream) throws IOException, ImageProcessingException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
		return getExifOrientation(metadata);
	}

	/**
	 * 从元数据中获取EXIF方向信息
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
	 * @param metadata 图像元数据对象，不可为null
	 * @return EXIF方向值，未找到时返回{@link #NORMAL_ORIENTATION}
	 * @throws IllegalArgumentException 当元数据为null时抛出
	 * @see com.drew.metadata.MetadataReader
	 * @since 1.0.0
	 */
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

	/**
	 * 从字节数组输入流解析图像尺寸
	 *
	 * @param inputStream 字节数组输入流，不可为null
	 * @param useMetadata 是否使用元数据获取尺寸
	 * @return 包含宽度和高度的ImageSize对象，无法获取时返回null
	 * @throws IOException 当流读取失败时抛出
	 * @since 1.0.0
	 */
	protected static ImageSize parseSizeByByteArrayInputStream(final InputStream inputStream, final boolean useMetadata) throws IOException {
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

	/**
	 * 从图像输入流解析MIME类型
	 *
	 * @param imageInputStream 图像输入流，不可为null
	 * @return MIME类型数组，无法获取时返回空数组
	 * @since 1.0.0
	 */
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

	/**
	 * 校验文件路径有效性
	 *
	 * @param path 要检查的文件路径，不可为null
	 * @throws NoSuchFileException 当路径不存在或不是常规文件时抛出
	 * @since 1.0.0
	 */
	protected static void checkPath(final Path path) throws NoSuchFileException {
		if (!Files.exists(path) || !Files.isRegularFile(path)) {
			throw new NoSuchFileException(path.toString());
		}
	}

	/**
	 * 校验文件有效性
	 *
	 * @param file 要检查的文件对象，不可为null
	 * @throws FileNotFoundException 当文件不存在或不是常规文件时抛出
	 * @since 1.0.0
	 */
	protected static void checkFile(final File file) throws FileNotFoundException {
		if (!file.exists() || !file.isFile()) {
			throw new FileNotFoundException(file.toString());
		}
	}
}