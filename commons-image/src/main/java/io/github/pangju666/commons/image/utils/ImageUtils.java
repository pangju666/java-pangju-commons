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
import com.twelvemonkeys.image.ImageUtil;
import io.github.pangju666.commons.image.lang.ImageConstants;
import io.github.pangju666.commons.image.model.ImageSize;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.IOUtils;
import org.apache.commons.io.input.UnsynchronizedBufferedInputStream;
import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Kernel;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * 图像处理工具类
 * <p>
 * 提供全面的图像处理功能，包括但不限于以下方面：
 * <ul>
 *   <li><b>图像元数据处理</b> - 读取EXIF信息、方向校正等</li>
 *   <li><b>图像格式检测</b> - 支持JPEG、PNG、GIF等常见格式</li>
 *   <li><b>图像尺寸处理</b> - 自动处理EXIF方向信息</li>
 *   <li><b>MIME类型转换</b> - 图像格式与MIME类型互转</li>
 * </ul>
 * </p>
 *
 * <p><b>典型使用场景：</b></p>
 * <ol>
 *   <li>图像上传时的格式验证和尺寸获取</li>
 *   <li>图像处理前的EXIF方向校正</li>
 *   <li>图像格式转换时的MIME类型检查</li>
 * </ol>
 *
 * <p><b>注意事项：</b></p>
 * <ul>
 *   <li>所有方法均为静态方法，不可实例化</li>
 *   <li>线程安全 - 无共享状态</li>
 *   <li>大文件处理 - 对于超过100MB的文件建议关闭元数据读取</li>
 * </ul>
 *
 * <p>推荐以下方法：
 * <ul>
 *     <li>{@link ImageUtil#createCopy(BufferedImage)} 深拷贝图像</li>
 *     <li>{@link ImageUtil#toBuffered(BufferedImage, int)} 修改图像颜色类型</li>
 *     <li>{@link ImageUtil#createFlipped(Image, int)} 翻转图像</li>
 *     <li>{@link ImageUtil#createRotated(Image, int)} 旋转图像</li>
 *     <li>{@link ImageUtil#createRotated(Image, double)} 旋转图像</li>
 *     <li>{@link ImageUtil#hasTransparentPixels(RenderedImage, boolean)} 测试图像是否有透明或半透明像素</li>
 *     <li>{@link ImageUtil#waitForImage(Image, long)} 等待图像完全加载</li>
 *     <li>{@link ImageUtil#waitForImages(Image[], long)} 等待大量图像完全加载</li>
 *     <li>{@link ImageUtil#convolve(BufferedImage, Kernel, int)} 使用卷积矩阵卷积图像</li>
 *     <li>{@link ImageUtil#sharpen(BufferedImage, float)} 使用卷积矩阵锐化图像</li>
 *     <li>{@link ImageUtil#blur(BufferedImage, float)} 创建给定图像的模糊版本</li>
 *     <li>{@link ImageUtil#createResampled(Image, int, int, int)} 缩放图像</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see ImageReader
 * @see ImageMetadataReader
 * @see ImageConstants
 * @since 1.0.0
 */
public class ImageUtils {
	protected ImageUtils() {
	}

	/**
	 * 检查MIME类型是否支持读取
	 *
	 * <p>
	 * 支持的MIME类型包括但不限于：
	 * <table border="1">
	 *   <tr><th>MIME类型</th><th>描述</th></tr>
	 *   <tr><td>image/jpeg</td><td>JPEG图像</td></tr>
	 *   <tr><td>image/png</td><td>PNG图像</td></tr>
	 *   <tr><td>image/gif</td><td>GIF图像</td></tr>
	 *   <tr><td>image/bmp</td><td>BMP图像</td></tr>
	 *   <tr><td>image/webp</td><td>WebP图像</td></tr>
	 * </table>
	 * 完整列表见{@link ImageConstants#getSupportReadImageTypes()}
	 * </p>
	 *
	 * @param imageMimeType 待检查的MIME类型，允许为null或空
	 * @return 如果支持读取返回true，否则返回false
	 * @see ImageReaderSpi
	 * @see ImageConstants#getSupportReadImageTypes()
	 * @since 1.0.0
	 */
	public static boolean isSupportReadType(final String imageMimeType) {
		if (StringUtils.isBlank(imageMimeType)) {
			return false;
		}
		return ImageConstants.getSupportReadImageTypes().contains(imageMimeType);
	}

	/**
	 * 检查MIME类型是否支持写入
	 * <p>
	 * 支持的MIME类型包括但不限于：
	 * <table border="1">
	 *   <tr><th>MIME类型</th><th>描述</th></tr>
	 *   <tr><td>image/jpeg</td><td>JPEG图像</td></tr>
	 *   <tr><td>image/png</td><td>PNG图像</td></tr>
	 *   <tr><td>image/bmp</td><td>BMP图像</td></tr>
	 *   <tr><td>image/webp</td><td>WebP图像</td></tr>
	 * </table>
	 * 完整列表见{@link ImageConstants#getSupportWriteImageTypes()}
	 * </p>
	 *
	 * @param imageMimeType 待检查的MIME类型，允许为null或空
	 * @return 如果支持写入返回true，否则返回false
	 * @see ImageWriterSpi
	 * @see ImageConstants#getSupportWriteImageTypes()
	 * @since 1.0.0
	 */
	public static boolean isSupportWriteType(final String imageMimeType) {
		if (StringUtils.isBlank(imageMimeType)) {
			return false;
		}
		return ImageConstants.getSupportWriteImageTypes().contains(imageMimeType);
	}

	/**
	 * 判断两个MIME类型是否属于同一图像类型
	 * <p>
	 * 有些图像格式具有多个MIME类型（如JPEG可能有image/jpeg和image/jpg），
	 * 此方法通过ImageIO SPI机制检查两个MIME类型是否属于同一种图像格式。
	 * </p>
	 *
	 * <p><b>处理流程：</b></p>
	 * <ol>
	 *   <li>检查任一参数为空则返回false</li>
	 *   <li>通过ImageIO获取第一个MIME类型对应的ImageReader</li>
	 *   <li>获取该Reader支持的所有MIME类型</li>
	 *   <li>检查第二个MIME类型是否在支持列表中</li>
	 * </ol>
	 *
	 * @param mimeType1 第一个MIME类型，允许为空
	 * @param mimeType2 第二个MIME类型，允许为空
	 * @return 如果属于同一类型返回true，否则返回false
	 * @see ImageReaderSpi#getMIMETypes()
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
	 * 判断文件是否与指定MIME类型匹配
	 * <p>
	 * 有些图像格式具有多个MIME类型（如JPEG可能有image/jpeg和image/jpg），
	 * 此方法通过解析文件实际MIME类型来判断是否匹配。
	 * </p>
	 *
	 * <p><b>处理流程：</b></p>
	 * <ol>
	 *   <li>检查参数有效性</li>
	 *   <li>创建图像输入流</li>
	 *   <li>解析文件实际MIME类型</li>
	 *   <li>检查是否包含指定类型</li>
	 * </ol>
	 *
	 * @param file     要检查的文件对象，必须满足：
	 *               <ul>
	 *                 <li>非null</li>
	 *                 <li>存在且可读</li>
	 *               </ul>
	 * @param mimeType 要匹配的MIME类型，允许为空（将返回false）
	 * @return 如果匹配返回true，否则返回false
	 * @throws IOException 当文件不存在或读取失败时抛出
	 * @throws IllegalArgumentException 当file为null时抛出
	 * @see #parseMimeTypes(ImageInputStream)
	 * @since 1.0.0
	 */
	public static boolean isSameType(final File file, final String mimeType) throws IOException {
		if (StringUtils.isBlank(mimeType)) {
			return false;
		}
		FileUtils.checkFile(file, "file 不可为 null");
		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(file)) {
			return ArrayUtils.contains(parseMimeTypes(imageInputStream), mimeType);
		}
	}

	/**
	 * 判断字节数组是否与指定MIME类型匹配
	 * <p>
	 * 通过解析字节数组实际MIME类型来判断是否匹配，
	 * 适用于内存中图像数据的类型检查。
	 * </p>
	 *
	 * @param bytes    要检查的字节数组，必须满足：
	 *               <ul>
	 *                 <li>非null</li>
	 *                 <li>非空</li>
	 *               </ul>
	 * @param mimeType 要匹配的MIME类型，允许为空（将返回false）
	 * @return 如果匹配返回true，否则返回false
	 * @throws IOException 当读取数据失败时抛出
	 * @throws IllegalArgumentException 当bytes为null或空时抛出
	 * @see #parseMimeTypes(ImageInputStream)
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
	 * 判断输入流是否与指定MIME类型匹配
	 * <p>
	 * 通过解析输入流实际MIME类型来判断是否匹配，
	 * 会自动处理缓冲以提高性能。
	 * </p>
	 *
	 * @param inputStream 要检查的输入流，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>支持mark/reset操作</li>
	 *                  </ul>
	 * @param mimeType    要匹配的MIME类型，允许为空（将返回false）
	 * @return 如果匹配返回true，否则返回false
	 * @throws IOException 当读取流失败时抛出
	 * @throws IllegalArgumentException 当inputStream为null时抛出
	 * @see #parseMimeTypes(ImageInputStream)
	 * @since 1.0.0
	 */
	public static boolean isSameType(final InputStream inputStream, final String mimeType) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		if (StringUtils.isBlank(mimeType)) {
			return false;
		}

		Validate.notBlank(mimeType, "mimeType 不可为空");
		if (inputStream instanceof BufferedInputStream ||
			inputStream instanceof UnsynchronizedBufferedInputStream) {
			try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
				return ArrayUtils.contains(parseMimeTypes(imageInputStream), mimeType);
			}
		} else {
			try (UnsynchronizedBufferedInputStream bufferedInputStream = IOUtils.unsynchronizedBuffer(inputStream);
				 ImageInputStream imageInputStream = ImageIO.createImageInputStream(bufferedInputStream)) {
				return ArrayUtils.contains(parseMimeTypes(imageInputStream), mimeType);
			}
		}
	}

	/**
	 * 判断图像输入流是否与指定MIME类型匹配
	 * <p>
	 * 直接使用已解析的MIME类型进行匹配，
	 * 适用于已经创建图像输入流的场景。
	 * </p>
	 *
	 * @param imageInputStream 要检查的图像输入流，必须满足：
	 *                      <ul>
	 *                        <li>非null</li>
	 *                        <li>已定位到图像数据起始位置</li>
	 *                      </ul>
	 * @param mimeType        要匹配的MIME类型，允许为空（将返回false）
	 * @return 如果匹配返回true，否则返回false
	 * @throws IllegalArgumentException 当imageInputStream为null时抛出
	 * @see #parseMimeTypes(ImageInputStream)
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
	 * <p>
	 * 通过ImageIO SPI机制检测文件的实际MIME类型，
	 * 返回最匹配的MIME类型（按ImageReader优先级排序）。
	 * </p>
	 *
	 * <p><b>性能考虑：</b></p>
	 * <ul>
	 *   <li>会创建临时ImageInputStream</li>
	 *   <li>对于大文件，建议使用{@link FileUtils#getMimeType}</li>
	 * </ul>
	 *
	 * @param file 要检查的文件对象，必须满足：
	 *           <ul>
	 *             <li>非null</li>
	 *             <li>存在且可读</li>
	 *           </ul>
	 * @return 文件的MIME类型，无法获取时返回null
	 * @throws IOException 当文件不存在或读取失败时抛出
	 * @throws IllegalArgumentException 当file为null时抛出
	 * @apiNote 此方法专门用于获取图像文件的MIME类型
	 * @see ImageReaderSpi#getMIMETypes()
	 * @since 1.0.0
	 */
	public static String getMimeType(final File file) throws IOException {
		FileUtils.checkFile(file, "file 不可为 null");
		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(file)) {
			return getMimeType(imageInputStream);
		}
	}

	/**
	 * 获取字节数组数据的MIME类型（使用ImageIO获取）
	 * <p>
	 * 通过ImageIO SPI机制检测字节数组的实际MIME类型，
	 * 适用于内存中图像数据的类型检测。
	 * </p>
	 *
	 * <p><b>注意事项：</b></p>
	 * <ul>
	 *   <li>会创建临时ByteArrayInputStream</li>
	 *   <li>对于大字节数组(&gt;10MB)，建议使用其他方法</li>
	 * </ul>
	 *
	 * @param bytes 要检查的字节数组，允许为null
	 * @return 数据的MIME类型，无法获取时返回null
	 * @throws IOException 当读取数据失败时抛出
	 * @apiNote 此方法专门用于获取图像数据的MIME类型
	 * @see ImageReaderSpi#getMIMETypes()
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
	 * <p>
	 * 通过ImageIO SPI机制检测输入流的实际MIME类型，
	 * 会自动处理缓冲以提高性能。
	 * </p>
	 *
	 * <p><b>流处理规则：</b></p>
	 * <ul>
	 *   <li>非BufferedInputStream会自动包装</li>
	 *   <li>流不会被关闭（调用者负责）</li>
	 *   <li>流位置会被重置</li>
	 * </ul>
	 *
	 * @param inputStream 要检查的输入流，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>支持mark/reset操作</li>
	 *                  </ul>
	 * @return 流的MIME类型，无法获取时返回null
	 * @throws IOException 当读取流失败时抛出
	 * @throws IllegalArgumentException 当inputStream为null时抛出
	 * @apiNote 此方法专门用于获取图像流的MIME类型
	 * @see ImageReaderSpi#getMIMETypes()
	 * @since 1.0.0
	 */
	public static String getMimeType(final InputStream inputStream) throws IOException {
		Validate.notNull(inputStream, "imageInputStream 不可为 null");

		if (inputStream instanceof BufferedInputStream ||
			inputStream instanceof UnsynchronizedBufferedInputStream) {
			try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
				return getMimeType(imageInputStream);
			}
		} else {
			try (UnsynchronizedBufferedInputStream bufferedInputStream = IOUtils.unsynchronizedBuffer(inputStream);
				 ImageInputStream imageInputStream = ImageIO.createImageInputStream(bufferedInputStream)) {
				return getMimeType(imageInputStream);
			}
		}
	}

	/**
	 * 获取图像输入流的MIME类型（使用ImageIO获取）
	 * <p>
	 * 直接使用已创建的ImageInputStream检测MIME类型，
	 * 适用于已经创建图像输入流的场景。
	 * </p>
	 *
	 * <p><b>实现细节：</b></p>
	 * <ul>
	 *   <li>使用ImageIO.getImageReaders()获取匹配的ImageReader</li>
	 *   <li>返回第一个ImageReader支持的最高优先级MIME类型</li>
	 *   <li>不会修改输入流状态</li>
	 * </ul>
	 *
	 * @param imageInputStream 要检查的图像输入流，必须满足：
	 *                      <ul>
	 *                        <li>非null</li>
	 *                        <li>已定位到图像数据起始位置</li>
	 *                      </ul>
	 * @return 流的MIME类型，无法获取时返回null
	 * @throws IllegalArgumentException 当imageInputStream为null时抛出
	 * @apiNote 此方法专门用于获取图像流的MIME类型
	 * @see ImageReaderSpi#getMIMETypes()
	 * @since 1.0.0
	 */
	public static String getMimeType(final ImageInputStream imageInputStream) {
		Validate.notNull(imageInputStream, "imageInputStream 不可为 null");

		return ArrayUtils.get(parseMimeTypes(imageInputStream), 0, null);
	}

	/**
	 * 从图像元数据中提取MIME类型信息
	 * <p>
	 * 使用metadata-extractor库从图像文件的元数据中获取MIME类型，
	 * 主要从{@link FileTypeDirectory}中读取{@link FileTypeDirectory#TAG_DETECTED_FILE_MIME_TYPE}标签值。
	 * </p>
	 *
	 * <p><b>实现细节：</b></p>
	 * <ul>
	 *   <li>遍历所有{@link FileTypeDirectory}类型的元数据目录</li>
	 *   <li>检查是否包含{@link FileTypeDirectory#TAG_DETECTED_FILE_MIME_TYPE}标签</li>
	 *   <li>返回找到的第一个有效MIME类型</li>
	 * </ul>
	 *
	 * <p><b>注意事项：</b></p>
	 * <ul>
	 *   <li>不是所有图像格式的元数据都包含MIME类型信息</li>
	 *   <li>metadata-extractor支持的格式有限</li>
	 *   <li>对于更可靠的MIME类型检测，建议使用{@link FileUtils#getMimeType}</li>
	 * </ul>
	 *
	 * @param metadata 图像元数据对象，必须满足：
	 *               <ul>
	 *                 <li>非null</li>
	 *                 <li>包含有效的图像元数据</li>
	 *               </ul>
	 * @return 检测到的MIME类型字符串，格式如"image/jpeg"，
	 *         未找到时返回null
	 * @throws IllegalArgumentException 当metadata为null时抛出
	 * @apiNote 此方法依赖于metadata-extractor库的实现，
	 *          对于不支持的格式将返回null
	 * @see FileTypeDirectory#TAG_DETECTED_FILE_MIME_TYPE
	 * @see FileUtils#getMimeType(File)
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
	 * 获取图像文件的尺寸信息（自动处理EXIF方向）
	 * <p>
	 * 自动检测并处理EXIF方向信息，确保返回的尺寸与实际显示尺寸一致。
	 * 对于大文件（&gt;100MB），建议使用{@link #getSize(File, boolean)}并设置useMetadata为false。
	 * </p>
	 *
	 * <p><b>实现细节：</b></p>
	 * <ul>
	 *   <li>默认优先使用元数据获取尺寸信息</li>
	 *   <li>自动处理EXIF方向标签（1-8）</li>
	 *   <li>对于方向值5-8会自动交换宽高</li>
	 *   <li>元数据获取失败时回退到直接读取图像</li>
	 * </ul>
	 *
	 * @param file 图像文件对象，必须满足：
	 *            <ul>
	 *              <li>非null</li>
	 *              <li>存在且可读</li>
	 *              <li>有效的图像文件</li>
	 *            </ul>
	 * @return 包含宽度和高度的ImageSize对象，无法获取时返回null
	 * @throws IOException 当文件不存在或读取失败时抛出
	 * @throws IllegalArgumentException 当file为null时抛出
	 * @see #getSize(File, boolean)
	 * @see #getSize(Metadata)
	 * @since 1.0.0
	 */
	public static ImageSize getSize(final File file) throws IOException {
		return getSize(file, true);
	}

	/**
	 * 获取图像文件的尺寸信息（可选择是否优先使用元数据）
	 * <p>
	 * 提供更灵活的方式获取图像尺寸，可选择是否优先使用元数据获取尺寸。
	 * 当useMetadata为true时会自动处理EXIF方向信息。
	 * </p>
	 *
	 * <p><b>性能考虑：</b></p>
	 * <ul>
	 *   <li>useMetadata=true：会尝试读取完整文件元数据</li>
	 *   <li>useMetadata=false：直接读取图像尺寸，性能更好</li>
	 *   <li>对于大文件(&gt;100MB)，建议设置useMetadata=false</li>
	 * </ul>
	 *
	 * @param file 图像文件对象，必须满足：
	 *            <ul>
	 *              <li>非null</li>
	 *              <li>存在且可读</li>
	 *              <li>有效的图像文件</li>
	 *            </ul>
	 * @param useMetadata 是否优先使用元数据获取尺寸：
	 *                  <ul>
	 *                    <li>true：自动处理EXIF方向</li>
	 *                    <li>false：直接读取图像尺寸</li>
	 *                  </ul>
	 * @return 包含宽度和高度的ImageSize对象，无法获取时返回null
	 * @throws IOException 当文件不存在或读取失败时抛出
	 * @throws IllegalArgumentException 当file为null时抛出
	 * @apiNote 此方法适用于需要平衡准确性和性能的场景
	 * @see MetadataReader
	 * @see ImageReader
	 * @since 1.0.0
	 */
	public static ImageSize getSize(final File file, final boolean useMetadata) throws IOException {
		FileUtils.checkFile(file, "file 不可为 null");
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
	 * 获取字节数组数据的图像尺寸（自动处理EXIF方向）
	 * <p>
	 * 自动检测并处理EXIF方向信息，确保返回的尺寸与实际显示尺寸一致。
	 * 对于大字节数组(&gt;100MB)，建议使用{@link #getSize(byte[], boolean)}并设置useMetadata为false。
	 * </p>
	 *
	 * <p><b>实现细节：</b></p>
	 * <ul>
	 *   <li>默认优先使用元数据获取尺寸信息</li>
	 *   <li>自动处理EXIF方向标签（1-8）</li>
	 *   <li>对于方向值5-8会自动交换宽高</li>
	 *   <li>元数据获取失败时回退到直接读取图像</li>
	 * </ul>
	 *
	 * @param bytes 要检查的字节数组，允许为null或空
	 * @return 包含宽度和高度的ImageSize对象，无法获取时返回null
	 * @throws IOException 当读取数据失败时抛出
	 * @see #getSize(byte[], boolean)
	 * @see #getSize(Metadata)
	 * @since 1.0.0
	 */
	public static ImageSize getSize(final byte[] bytes) throws IOException {
		return getSize(bytes, true);
	}

	/**
	 * 获取字节数组数据的图像尺寸（可选择是否优先使用元数据）
	 * <p>
	 * 提供更灵活的方式获取图像尺寸，可选择是否优先使用元数据获取尺寸。
	 * 当useMetadata为true时会自动处理EXIF方向信息。
	 * </p>
	 *
	 * <p><b>性能考虑：</b></p>
	 * <ul>
	 *   <li>useMetadata=true：会尝试读取完整元数据</li>
	 *   <li>useMetadata=false：直接读取图像尺寸，性能更好</li>
	 *   <li>对于大字节数组(&gt;100MB)，建议设置useMetadata=false</li>
	 * </ul>
	 *
	 * @param bytes 要检查的字节数组，必须满足：
	 *            <ul>
	 *              <li>非null</li>
	 *              <li>非空</li>
	 *            </ul>
	 * @param useMetadata 是否优先使用元数据获取尺寸：
	 *                  <ul>
	 *                    <li>true：自动处理EXIF方向</li>
	 *                    <li>false：直接读取图像尺寸</li>
	 *                  </ul>
	 * @return 包含宽度和高度的ImageSize对象，无法获取时返回null
	 * @throws IOException 当读取数据失败时抛出
	 * @throws IllegalArgumentException 当bytes为null或空时抛出
	 * @apiNote 此方法适用于需要平衡准确性和性能的场景
	 * @see MetadataReader
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
	 * <p>
	 * 自动检测并处理EXIF方向信息，确保返回的尺寸与实际显示尺寸一致。
	 * 对于大流数据(&gt;100MB)，建议使用{@link #getSize(InputStream, boolean)}并设置useMetadata为false。
	 * </p>
	 *
	 * <p><b>实现细节：</b></p>
	 * <ul>
	 *   <li>默认优先使用元数据获取尺寸信息</li>
	 *   <li>自动处理EXIF方向标签（1-8）</li>
	 *   <li>对于方向值5-8会自动交换宽高</li>
	 *   <li>元数据获取失败时回退到直接读取图像</li>
	 * </ul>
	 *
	 * @param inputStream 输入流对象，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>支持mark/reset操作</li>
	 *                  </ul>
	 * @return 包含宽度和高度的ImageSize对象，无法获取时返回null
	 * @throws IOException 当流读取失败时抛出
	 * @throws IllegalArgumentException 当inputStream为null时抛出
	 * @see #getSize(InputStream, boolean)
	 * @see #getSize(Metadata)
	 * @since 1.0.0
	 */
	public static ImageSize getSize(final InputStream inputStream) throws IOException {
		return getSize(inputStream, true);
	}

	/**
	 * 获取输入流的图像尺寸（可选择是否优先使用元数据）
	 * <p>
	 * 提供更灵活的方式获取图像尺寸，可选择是否优先使用元数据获取尺寸。
	 * 当useMetadata为true时会自动处理EXIF方向信息。
	 * </p>
	 *
	 * <p><b>性能考虑：</b></p>
	 * <ul>
	 *   <li>useMetadata=true：会尝试读取完整流数据</li>
	 *   <li>useMetadata=false：直接读取图像尺寸，性能更好</li>
	 *   <li>对于大流数据(&gt;100MB)，建议设置useMetadata=false</li>
	 * </ul>
	 *
	 * @param inputStream 输入流对象，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>支持mark/reset操作</li>
	 *                  </ul>
	 * @param useMetadata 是否优先使用元数据获取尺寸：
	 *                  <ul>
	 *                    <li>true：自动处理EXIF方向</li>
	 *                    <li>false：直接读取图像尺寸</li>
	 *                  </ul>
	 * @return 包含宽度和高度的ImageSize对象，无法获取时返回null
	 * @throws IOException 当流读取失败时抛出
	 * @throws IllegalArgumentException 当inputStream为null时抛出
	 * @apiNote 此方法适用于需要平衡准确性和性能的场景
	 * @see MetadataReader
	 * @see ImageReader
	 * @since 1.0.0
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

		try (InputStream tmpInputStream = outputStream.toInputStream()) {
			Metadata metadata = ImageMetadataReader.readMetadata(tmpInputStream);
			ImageSize imageSize = getSize(metadata);
			if (Objects.nonNull(imageSize)) {
				return imageSize;
			}
		} catch (ImageProcessingException | IOException ignored) {
		}

		try (InputStream tmpInputStream = outputStream.toInputStream();
			 ImageInputStream imageInputStream = ImageIO.createImageInputStream(tmpInputStream)) {
			if (Objects.isNull(imageInputStream)) {
				return null;
			}
			return getSize(imageInputStream);
		}
	}

	/**
	 * 获取图像输入流的图像尺寸（不处理图像EXIF方向）
	 * <p>
	 * 直接通过ImageReader获取图像尺寸，不处理EXIF方向信息，
	 * 适用于已经确定不需要处理方向的场景。
	 * </p>
	 *
	 * <p><b>注意事项：</b></p>
	 * <ul>
	 *   <li>不会自动处理EXIF方向信息</li>
	 *   <li>调用者需确保输入流已正确定位</li>
	 *   <li>使用后会自动释放ImageReader资源</li>
	 * </ul>
	 *
	 * @param imageInputStream 要检查的图像输入流，不可为null
	 * @return 包含宽度和高度的ImageSize对象，无法获取时返回null
	 * @throws IOException              当读取流失败时抛出
	 * @throws IllegalArgumentException 当图像输入流为null时抛出
	 * @see ImageReader
	 * @since 1.0.0
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
	 * <p>
	 * 自动处理EXIF方向信息，当方向值为5-8时（需要90度旋转的情况），
	 * 会自动交换宽高值以确保返回的尺寸与实际显示尺寸一致。
	 * </p>
	 *
	 * <p><b>EXIF处理规则：</b></p>
	 * <ul>
	 *   <li>方向值1-4：保持原始宽高</li>
	 *   <li>方向值5-8：交换宽高值</li>
	 * </ul>
	 *
	 * @param metadata 图像元数据对象，不可为null
	 * @return 包含宽度和高度的ImageSize对象，无法获取时返回null
	 * @throws IllegalArgumentException 当元数据为null时抛出
	 * @see MetadataReader
	 * @since 1.0.0
	 */
	public static ImageSize getSize(final Metadata metadata) {
		Validate.notNull(metadata, "metadata 不可为 null");

		Collection<ExifDirectoryBase> exifDirectories = metadata.getDirectoriesOfType(ExifDirectoryBase.class);
		Integer imageWidth = null;
		Integer imageHeight = null;
		int imageOrientation = ImageConstants.NORMAL_EXIF_ORIENTATION;
		for (ExifDirectoryBase exifDirectoryBase : exifDirectories) {
			if (exifDirectoryBase instanceof ExifIFD0Directory) {
				ExifIFD0Directory exifIFD0Directory = (ExifIFD0Directory) exifDirectoryBase;
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
			Integer orientation = exifDirectoryBase.getInteger(ExifDirectoryBase.TAG_ORIENTATION);
			if (Objects.nonNull(orientation)) {
				imageOrientation = orientation;
			}
		}
		if (ObjectUtils.anyNull(imageWidth, imageHeight)) {
			return null;
		}
		return (imageOrientation >= 5 && imageOrientation <= 8) ? new ImageSize(imageHeight, imageWidth) :
			new ImageSize(imageWidth, imageHeight);
	}

	/**
	 * 获取文件的EXIF方向信息
	 * <p>
	 * 标准EXIF方向值定义：
	 * <table border="1">
	 *   <tr><th>值</th><th>描述</th><th>旋转角度</th></tr>
	 *   <tr><td>1</td><td>正常方向</td><td>0°</td></tr>
	 *   <tr><td>2</td><td>水平翻转</td><td>镜像</td></tr>
	 *   <tr><td>3</td><td>旋转180度</td><td>180°</td></tr>
	 *   <tr><td>4</td><td>垂直翻转</td><td>镜像</td></tr>
	 *   <tr><td>5</td><td>旋转90度+水平翻转</td><td>90°+镜像</td></tr>
	 *   <tr><td>6</td><td>顺时针旋转90度</td><td>90°</td></tr>
	 *   <tr><td>7</td><td>旋转270度+水平翻转</td><td>270°+镜像</td></tr>
	 *   <tr><td>8</td><td>逆时针旋转90度</td><td>270°</td></tr>
	 * </table>
	 * </p>
	 *
	 * @param file 图像文件，必须满足：
	 *            <ul>
	 *              <li>非null</li>
	 *              <li>存在且可读</li>
	 *              <li>包含EXIF信息</li>
	 *            </ul>
	 * @return EXIF方向值（1-8），无法获取时返回{@link ImageConstants#NORMAL_EXIF_ORIENTATION}
	 * @throws IOException 当文件读取失败时抛出
	 * @throws IllegalArgumentException 当file为null时抛出
	 * @see ExifDirectoryBase#TAG_ORIENTATION
	 * @since 1.0.0
	 */
	public static Integer getExifOrientation(final File file) throws IOException, ImageProcessingException {
		FileUtils.checkFile(file, "file 不可为 null");
		Metadata metadata = ImageMetadataReader.readMetadata(file);
		return getExifOrientation(metadata);
	}

	/**
	 * 获取字节数组数据的EXIF方向信息
	 * <p>
	 * 自动检测并处理EXIF方向信息，返回标准化的方向值。
	 * 完整方向说明：
	 * <table border="1">
	 *   <caption>EXIF方向值说明</caption>
	 *   <tr><th>值</th><th>描述</th><th>旋转角度</th></tr>
	 *   <tr><td>1</td><td>正常方向</td><td>0°</td></tr>
	 *   <tr><td>2</td><td>水平翻转</td><td>镜像</td></tr>
	 *   <tr><td>3</td><td>旋转180度</td><td>180°</td></tr>
	 *   <tr><td>4</td><td>垂直翻转</td><td>镜像</td></tr>
	 *   <tr><td>5</td><td>旋转90度+水平翻转</td><td>90°+镜像</td></tr>
	 *   <tr><td>6</td><td>顺时针旋转90度</td><td>90°</td></tr>
	 *   <tr><td>7</td><td>旋转270度+水平翻转</td><td>270°+镜像</td></tr>
	 *   <tr><td>8</td><td>逆时针旋转90度</td><td>270°</td></tr>
	 * </table>
	 * </p>
	 *
	 * @param bytes 图像字节数组，必须满足：
	 *            <ul>
	 *              <li>非null</li>
	 *              <li>非空</li>
	 *              <li>包含有效的EXIF信息</li>
	 *            </ul>
	 * @return EXIF方向值（1-8），未找到时返回{@link ImageConstants#NORMAL_EXIF_ORIENTATION}
	 * @throws IOException 当读取数据失败时抛出
	 * @throws ImageProcessingException 当图像处理异常时抛出
	 * @throws IllegalArgumentException 当bytes为null或空时抛出
	 * @see #getExifOrientation(Metadata)
	 * @since 1.0.0
	 */
	public static int getExifOrientation(final byte[] bytes) throws IOException, ImageProcessingException {
		Validate.isTrue(ArrayUtils.isNotEmpty(bytes), "bytes 不可为空");

		Metadata metadata = ImageMetadataReader.readMetadata(IOUtils.toUnsynchronizedByteArrayInputStream(bytes));
		return getExifOrientation(metadata);
	}

	/**
	 * 获取输入流的EXIF方向信息
	 * <p>
	 * 自动检测并处理EXIF方向信息，返回标准化的方向值。
	 * 完整方向说明：
	 * <table border="1">
	 *   <caption>EXIF方向值说明</caption>
	 *   <tr><th>值</th><th>描述</th><th>旋转角度</th></tr>
	 *   <tr><td>1</td><td>正常方向</td><td>0°</td></tr>
	 *   <tr><td>2</td><td>水平翻转</td><td>镜像</td></tr>
	 *   <tr><td>3</td><td>旋转180度</td><td>180°</td></tr>
	 *   <tr><td>4</td><td>垂直翻转</td><td>镜像</td></tr>
	 *   <tr><td>5</td><td>旋转90度+水平翻转</td><td>90°+镜像</td></tr>
	 *   <tr><td>6</td><td>顺时针旋转90度</td><td>90°</td></tr>
	 *   <tr><td>7</td><td>旋转270度+水平翻转</td><td>270°+镜像</td></tr>
	 *   <tr><td>8</td><td>逆时针旋转90度</td><td>270°</td></tr>
	 * </table>
	 * </p>
	 *
	 * @param inputStream 图像输入流，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>支持mark/reset操作</li>
	 *                    <li>包含有效的EXIF信息</li>
	 *                  </ul>
	 * @return EXIF方向值（1-8），未找到时返回{@link ImageConstants#NORMAL_EXIF_ORIENTATION}
	 * @throws IOException 当流读取失败时抛出
	 * @throws ImageProcessingException 当图像处理异常时抛出
	 * @throws IllegalArgumentException 当inputStream为null时抛出
	 * @see #getExifOrientation(Metadata)
	 * @since 1.0.0
	 */
	public static int getExifOrientation(final InputStream inputStream) throws IOException, ImageProcessingException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
		return getExifOrientation(metadata);
	}

	/**
	 * 从元数据中获取EXIF方向信息
	 * <p>
	 * 自动检测并处理EXIF方向标签，返回标准化的方向值。
	 * 完整方向说明：
	 * <table border="1">
	 *   <caption>EXIF方向值说明</caption>
	 *   <tr><th>值</th><th>描述</th><th>旋转角度</th></tr>
	 *   <tr><td>1</td><td>正常方向</td><td>0°</td></tr>
	 *   <tr><td>2</td><td>水平翻转</td><td>镜像</td></tr>
	 *   <tr><td>3</td><td>旋转180度</td><td>180°</td></tr>
	 *   <tr><td>4</td><td>垂直翻转</td><td>镜像</td></tr>
	 *   <tr><td>5</td><td>旋转90度+水平翻转</td><td>90°+镜像</td></tr>
	 *   <tr><td>6</td><td>顺时针旋转90度</td><td>90°</td></tr>
	 *   <tr><td>7</td><td>旋转270度+水平翻转</td><td>270°+镜像</td></tr>
	 *   <tr><td>8</td><td>逆时针旋转90度</td><td>270°</td></tr>
	 * </table>
	 * </p>
	 *
	 * <p><b>处理流程：</b></p>
	 * <ol>
	 *   <li>检查元数据是否为null</li>
	 *   <li>遍历所有EXIF目录</li>
	 *   <li>查找TAG_ORIENTATION标签</li>
	 *   <li>返回找到的第一个有效方向值</li>
	 * </ol>
	 *
	 * @param metadata 图像元数据对象，必须满足：
	 *                <ul>
	 *                  <li>非null</li>
	 *                  <li>包含有效的EXIF信息</li>
	 *                </ul>
	 * @return EXIF方向值（1-8），未找到时返回{@link ImageConstants#NORMAL_EXIF_ORIENTATION}
	 * @throws IllegalArgumentException 当元数据为null时抛出
	 * @see ExifIFD0Directory
	 * @see ExifDirectoryBase#TAG_ORIENTATION
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
		return ImageConstants.NORMAL_EXIF_ORIENTATION;
	}

	/**
	 * 通过输入流解析图像尺寸
	 * <p>
	 * 内部方法，用于处理入流形式的图像数据，
	 * 支持通过元数据或直接读取两种方式获取尺寸。
	 * </p>
	 *
	 * <p><b>性能考虑：</b></p>
	 * <ul>
	 *   <li>优先使用元数据时(useMetadata=true)，会尝试读取完整流</li>
	 *   <li>对于大文件，建议设置useMetadata=false</li>
	 *   <li>输入流必须支持mark/reset操作</li>
	 * </ul>
	 *
	 * @param inputStream  输入流，必须满足：
	 *                    <ul>
	 *                      <li>非null</li>
	 *                      <li>支持mark/reset操作</li>
	 *                    </ul>
	 * @param useMetadata 是否优先使用元数据
	 * @return 图像尺寸对象，解析失败返回null
	 * @throws IOException 当I/O错误发生时抛出
	 * @see #getSize(Metadata)
	 * @see #getSize(ImageInputStream)
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
	 * 解析图像输入流的MIME类型集合
	 * <p>
	 * 内部方法，通过ImageIO SPI机制获取图像输入流支持的所有MIME类型，
	 * 返回结果按优先级排序（第一个为最匹配的类型）。
	 * </p>
	 *
	 * <p><b>实现细节：</b></p>
	 * <ul>
	 *   <li>使用ImageIO.getImageReaders()获取匹配的ImageReader</li>
	 *   <li>通过ImageReaderSpi获取支持的所有MIME类型</li>
	 *   <li>自动处理null值，确保返回非null数组</li>
	 * </ul>
	 *
	 * @param imageInputStream 图像输入流，必须满足：
	 *                       <ul>
	 *                         <li>非null</li>
	 *                         <li>已定位到图像数据起始位置</li>
	 *                       </ul>
	 * @return MIME类型数组，可能为空数组但不会为null
	 * @throws IllegalArgumentException 当imageInputStream为null时抛出
	 * @see ImageReaderSpi#getMIMETypes()
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
}