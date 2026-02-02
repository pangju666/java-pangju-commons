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
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataReader;
import com.drew.metadata.bmp.BmpHeaderDirectory;
import com.drew.metadata.eps.EpsDirectory;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.file.FileTypeDirectory;
import com.drew.metadata.gif.GifImageDirectory;
import com.drew.metadata.heif.HeifDirectory;
import com.drew.metadata.ico.IcoDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.photoshop.PsdHeaderDirectory;
import com.drew.metadata.png.PngDirectory;
import com.drew.metadata.webp.WebpDirectory;
import com.twelvemonkeys.image.ImageUtil;
import io.github.pangju666.commons.image.lang.ImageConstants;
import io.github.pangju666.commons.image.model.ImageSize;
import io.github.pangju666.commons.io.lang.IOConstants;
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
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Kernel;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.Iterator;
import java.util.Objects;

/**
 * 图像处理工具类
 * <p>
 * 提供图像信息获取功能，包括但不限于以下方面：
 * <ul>
 *   <li><b>图像元数据读取</b> - 读取EXIF信息</li>
 *   <li><b>图像格式检测</b> - 基于文件内容检测 MIME 类型（支持 JPEG、PNG、GIF、BMP、WebP 等）</li>
 *   <li><b>图像尺寸获取</b> - 解析原始尺寸并提取 EXIF 方向信息</li>
 *   <li><b>环境支持检查</b> - 检查当前环境是否支持读写特定格式的图像</li>
 *   <li><b>颜色格式转换</b> - 颜色对象（Color）与十六进制字符串互转</li>
 * </ul>
 * </p>
 *
 * <p><b>典型使用场景：</b></p>
 * <ol>
 *   <li>图像上传时的格式验证和尺寸获取</li>
 *   <li>图像处理前的 EXIF 方向校正</li>
 *   <li>图像格式转换时的 MIME 类型检查</li>
 * </ol>
 *
 * <p><b>关于图像尺寸：</b></p>
 * <p>
 * 本工具类获取的图像尺寸默认为<b>物理存储尺寸</b>。如需获取符合视觉习惯的尺寸（即应用了 EXIF 旋转后的尺寸），
 * 请结合 {@link ImageSize#getVisualSize()} 使用。
 * </p>
 *
 * <p><b>注意事项：</b></p>
 * <ul>
 *   <li>所有方法均为静态方法，不可实例化</li>
 *   <li>线程安全 - 无共享状态</li>
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
	 * 带 Alpha 的颜色十六进制格式模板
	 * <p>
	 * 格式：{@code #AARRGGBB}，每个通道使用两位十六进制表示。
	 * 生成示例：Alpha=255, R=16, G=32, B=48 → {@code "#ff102030"}
	 * </p>
	 *
	 * @since 1.0.0
	 */
	protected static final String ALPHA_COLOR_HEX_FORMAT = "#%02x%02x%02x%02x";

	/**
	 * 不带 Alpha 的颜色十六进制格式模板
	 * <p>
	 * 格式：{@code #RRGGBB}，每个通道使用两位十六进制表示。
	 * 生成示例：R=16, G=32, B=48 → {@code "#102030"}
	 * </p>
	 *
	 * @since 1.0.0
	 */
	protected static final String COLOR_HEX_FORMAT = "#%02x%02x%02x";

	/**
	 * 将 Color 转换为 #AARRGGBB 格式的十六进制字符串
	 *
	 * @since 1.0.0
	 */
	public static String toHexColorWithAlpha(final Color color) {
		Validate.notNull(color, "color 不可为 null");

		return String.format(ALPHA_COLOR_HEX_FORMAT, color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
	}

	/**
	 * 将 Color 转换为 #RRGGBB 格式的十六进制字符串（忽略 Alpha）
	 *
	 * @since 1.0.0
	 */
	public static String toHexColor(final Color color) {
		Validate.notNull(color, "color 不可为 null");

		return String.format(COLOR_HEX_FORMAT, color.getRed(), color.getGreen(), color.getBlue());
	}

	/**
	 * 检查MIME类型是否支持读取
	 * <p>
	 * 基于ImageIO SPI机制检查当前运行环境是否支持读取指定的MIME类型。
	 * 支持的类型取决于classpath中注册的{@link ImageReader}插件。
	 * </p>
	 *
	 * <p><b>常见支持类型（取决于环境）：</b></p>
	 * <ul>
	 *   <li>image/jpeg (JPEG)</li>
	 *   <li>image/png (PNG)</li>
	 *   <li>image/gif (GIF)</li>
	 *   <li>image/bmp (BMP)</li>
	 *   <li>image/webp (WebP - 需对应插件)</li>
	 * </ul>
	 *
	 * @param imageMimeType 待检查的MIME类型，支持null或空字符串（直接返回false）
	 * @return 当前环境有可用的{@link ImageReader}支持该MIME类型时返回 true
	 * @see ImageReaderSpi
	 * @see ImageConstants#getSupportedReadImageTypes()
	 * @since 1.0.0
	 */
	public static boolean isSupportReadType(final String imageMimeType) {
		if (StringUtils.isBlank(imageMimeType)) {
			return false;
		}
		return ImageConstants.getSupportedReadImageTypes().contains(imageMimeType);
	}

	/**
	 * 检查MIME类型是否支持写入
	 * <p>
	 * 基于ImageIO SPI机制检查当前运行环境是否支持写入指定的MIME类型。
	 * 支持的类型取决于classpath中注册的{@link ImageWriter}插件。
	 * </p>
	 *
	 * <p><b>常见支持类型（取决于环境）：</b></p>
	 * <ul>
	 *   <li>image/jpeg (JPEG)</li>
	 *   <li>image/png (PNG)</li>
	 *   <li>image/bmp (BMP)</li>
	 *   <li>image/webp (WebP - 需对应插件)</li>
	 * </ul>
	 *
	 * @param imageMimeType 待检查的MIME类型，支持null或空字符串（直接返回false）
	 * @return 当前环境有可用的{@link ImageWriter}支持该MIME类型时返回 true
	 * @see ImageWriterSpi
	 * @see ImageConstants#getSupportedWriteImageTypes()
	 * @since 1.0.0
	 */
	public static boolean isSupportWriteType(final String imageMimeType) {
		if (StringUtils.isBlank(imageMimeType)) {
			return false;
		}
		return ImageConstants.getSupportedWriteImageTypes().contains(imageMimeType);
	}

	/**
	 * 获取文件的MIME类型（使用Apache Tika获取）
	 * <p>
	 * 通过Apache Tika内容检测引擎检测文件的实际MIME类型，
	 * 返回最匹配的MIME类型（遵循IANA标准）。
	 * </p>
	 *
	 * <p><b>性能考虑：</b></p>
	 * <ul>
	 *   <li>基于内容检测，无需依赖扩展名</li>
	 *   <li>底层调用{@link FileUtils#getMimeType(File)}</li>
	 * </ul>
	 *
	 * @param file 要检查的文件对象，必须满足：
	 *             <ul>
	 *               <li>非null</li>
	 *               <li>存在且可读</li>
	 *             </ul>
	 * @return 文件的MIME类型，无法获取时返回null
	 * @throws IOException              当文件不存在或读取失败时抛出
	 * @throws IllegalArgumentException 当file为null时抛出
	 * @apiNote 此方法用于获取图像文件的MIME类型
	 * @see FileUtils#getMimeType(File)
	 * @since 1.0.0
	 */
	public static String getMimeType(final File file) throws IOException {
		return FileUtils.getMimeType(file);
	}

	/**
	 * 获取字节数组数据的MIME类型（使用Apache Tika获取）
	 * <p>
	 * 通过Apache Tika内容检测引擎检测字节数组的实际MIME类型，
	 * 适用于内存中图像数据的类型检测。
	 * </p>
	 *
	 * <p><b>注意事项：</b></p>
	 * <ul>
	 *   <li>基于内容魔数检测</li>
	 *   <li>对于大字节数组，建议使用流式处理以节省内存</li>
	 * </ul>
	 *
	 * @param bytes 要检查的字节数组，允许为null
	 * @return 数据的MIME类型，无法获取时返回null
	 * @apiNote 此方法用于获取图像数据的MIME类型
	 * @see org.apache.tika.Tika#detect(byte[])
	 * @since 1.0.0
	 */
	public static String getMimeType(final byte[] bytes) {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}
		return IOConstants.getDefaultTika().detect(bytes);
	}

	/**
	 * 获取输入流的MIME类型（使用Apache Tika获取）
	 * <p>
	 * 通过Apache Tika内容检测引擎检测输入流的实际MIME类型，
	 * 会自动处理缓冲以提高性能。
	 * </p>
	 *
	 * <p><b>流处理规则：</b></p>
	 * <ul>
	 *   <li>非BufferedInputStream会自动包装</li>
	 *   <li>流不会被关闭（调用者负责）</li>
	 *   <li>流位置会被重置（前提是流支持mark/reset）</li>
	 * </ul>
	 *
	 * @param inputStream 要检查的输入流，必须满足：
	 *                    <ul>
	 *                      <li>非null</li>
	 *                      <li>建议支持mark/reset操作</li>
	 *                    </ul>
	 * @return 流的MIME类型，无法获取时返回null
	 * @throws IOException              当读取流失败时抛出
	 * @throws IllegalArgumentException 当inputStream为null时抛出
	 * @apiNote 此方法用于获取图像流的MIME类型
	 * @see org.apache.tika.Tika#detect(InputStream)
	 * @since 1.0.0
	 */
	public static String getMimeType(final InputStream inputStream) throws IOException {
		Validate.notNull(inputStream, "imageInputStream 不可为 null");

		if (inputStream instanceof BufferedInputStream ||
			inputStream instanceof UnsynchronizedBufferedInputStream) {
			return IOConstants.getDefaultTika().detect(inputStream);
		} else {
			try (UnsynchronizedBufferedInputStream bufferedInputStream = IOUtils.unsynchronizedBuffer(inputStream)) {
				return IOConstants.getDefaultTika().detect(bufferedInputStream);
			}
		}
	}

	/**
	 * 获取图像输入流的 MIME 类型
	 * <p>
	 * 基于 ImageIO SPI 机制检测图像格式，返回最匹配的一个 MIME 类型。
	 * 此方法直接操作 {@link ImageInputStream}，适用于已创建流的场景。
	 * </p>
	 *
	 * <p><b>实现细节：</b></p>
	 * <ul>
	 *   <li>使用 {@link ImageIO#getImageReaders(Object)} 获取图像读取器</li>
	 *   <li>通过读取器SPI获取支持的 MIME 类型列表</li>
	 *   <li>返回列表中的第一个元素（通常是标准 MIME 类型）</li>
	 *   <li>操作过程不会关闭输入流，但会读取部分数据</li>
	 * </ul>
	 *
	 * @param imageInputStream 图像输入流，必须满足：
	 *                         <ul>
	 *                           <li>非 null</li>
	 *                           <li>已定位到图像数据起始位置</li>
	 *                         </ul>
	 * @return 图像的 MIME 类型（如 "image/jpeg"），无法识别时返回 null
	 * @throws IllegalArgumentException 当 imageInputStream 为 null 时抛出
	 * @see ImageReaderSpi#getMIMETypes()
	 * @since 1.0.0
	 */
	public static String getMimeType(final ImageInputStream imageInputStream) {
		Validate.notNull(imageInputStream, "imageInputStream 不可为 null");

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
		return ArrayUtils.get(mimeTypes, 0, null);
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
	 *   <li>获取{@link FileTypeDirectory}类型的元数据</li>
	 *   <li>检查是否包含{@link FileTypeDirectory#TAG_DETECTED_FILE_MIME_TYPE}标签</li>
	 *   <li>返回找到的MIME类型</li>
	 * </ul>
	 *
	 * <p><b>注意事项：</b></p>
	 * <ul>
	 *   <li>不是所有图像格式的元数据都包含MIME类型信息</li>
	 *   <li>metadata-extractor支持的格式有限</li>
	 *   <li>对于更可靠的MIME类型检测，建议使用{@link FileUtils#getMimeType}</li>
	 * </ul>
	 *
	 * @param metadata 图像元数据对象，不可为 null
	 * @return 检测到的MIME类型字符串，格式如"image/jpeg"，未找到时返回null
	 * @throws IllegalArgumentException 当metadata为null时抛出
	 * @see FileTypeDirectory#TAG_DETECTED_FILE_MIME_TYPE
	 * @see FileTypeDirectory
	 * @since 1.0.0
	 */
	public static String getMimeType(final Metadata metadata) {
		Validate.notNull(metadata, "metadata 不可为 null");

		FileTypeDirectory fileTypeDirectory = metadata.getFirstDirectoryOfType(FileTypeDirectory.class);
		if (Objects.nonNull(fileTypeDirectory)) {
			if (fileTypeDirectory.containsTag(FileTypeDirectory.TAG_DETECTED_FILE_MIME_TYPE)) {
				return fileTypeDirectory.getString(FileTypeDirectory.TAG_DETECTED_FILE_MIME_TYPE);
			}
		}
		return null;
	}

	/**
	 * 获取图像文件的尺寸信息
	 * <p>
	 * 这是一个便捷方法，等效于调用 {@code getSize(file, true)}。
	 * 默认优先从元数据中解析尺寸信息。
	 * </p>
	 * <p><b>注意：</b></p>
	 * <ul>
	 *   <li>此方法返回的是图像的<b>原始存储尺寸</b>。</li>
	 *   <li><b>不会</b>根据 EXIF 方向信息自动交换宽度和高度。</li>
	 *   <li>如需获取视觉方向一致的尺寸，请自行处理 EXIF 方向。</li>
	 * </ul>
	 *
	 * @param file 图像文件对象，必须存在且可读
	 * @return 包含宽度和高度的 {@link ImageSize} 对象，如果无法解析则返回 null
	 * @throws IOException              当文件读取发生错误时抛出
	 * @throws IllegalArgumentException 当 file 为 null 时抛出
	 * @see #getSize(File, boolean)
	 * @since 1.0.0
	 */
	public static ImageSize getSize(final File file) throws IOException {
		return getSize(file, true);
	}

	/**
	 * 获取图像文件的尺寸信息（可配置策略）
	 * <p>
	 * 根据 {@code useMetadata} 参数决定获取尺寸的策略：
	 * </p>
	 * <ul>
	 *   <li><b>true (推荐)：</b> 优先读取元数据（Metadata）。
	 *       <ul>
	 *           <li>如果元数据包含尺寸信息，直接返回（返回的是<b>原始存储尺寸</b>，不交换宽高）。</li>
	 *           <li>同时解析 EXIF 方向信息并存储在返回的 {@link ImageSize} 对象中。</li>
	 *           <li>如需获取视觉方向一致的尺寸，请调用 {@link ImageSize#getVisualSize()}。</li>
	 *       </ul>
	 *   </li>
	 *   <li><b>false (高性能)：</b> 仅通过 ImageIO 读取图像原始尺寸。
	 *       <ul>
	 *           <li>忽略任何 EXIF 方向信息。</li>
	 *           <li>返回图像存储的物理像素尺寸。</li>
	 *       </ul>
	 *   </li>
	 * </ul>
	 *
	 * @param file        图像文件对象，必须非 null 且可读
	 * @param useMetadata 是否优先使用元数据获取尺寸
	 * @return 包含宽度和高度的 {@link ImageSize} 对象，如果无法解析则返回 null
	 * @throws IOException              当文件读取发生错误时抛出
	 * @throws IllegalArgumentException 当 file 为 null 时抛出
	 * @see MetadataReader
	 * @see ImageReader
	 * @since 1.0.0
	 */
	public static ImageSize getSize(final File file, final boolean useMetadata) throws IOException {
		FileUtils.checkFile(file, "file 不可为 null");

		if (!useMetadata) {
			try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(file)) {
				if (Objects.isNull(imageInputStream)) {
					return null;
				}
				return parseSizeByImageInputStream(imageInputStream, null);
			}
		}

		Integer orientation = null;
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(file);

			ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
			if (Objects.nonNull(exifIFD0Directory)) {
				orientation = exifIFD0Directory.getInteger(ExifDirectoryBase.TAG_ORIENTATION);
			}

			ImageSize imageSize = parseSizeByMetadata(metadata, exifIFD0Directory);
			if (Objects.nonNull(imageSize)) {
				return imageSize;
			}
		} catch (ImageProcessingException | IOException ignored) {
		}
		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(file)) {
			if (Objects.isNull(imageInputStream)) {
				return null;
			}
			return parseSizeByImageInputStream(imageInputStream, orientation);
		}
	}

	/**
	 * 获取字节数组数据的图像尺寸
	 * <p>
	 * 这是一个便捷方法，等效于调用 {@code getSize(bytes, true)}。
	 * 默认优先从元数据中解析尺寸信息。
	 * </p>
	 * <p><b>注意：</b></p>
	 * <ul>
	 *   <li>返回的是<b>原始存储尺寸</b>，<b>不会</b>根据 EXIF 方向交换宽高。</li>
	 * </ul>
	 *
	 * @param bytes 要检查的字节数组，允许为 null 或空（此时返回 null）
	 * @return 包含宽度和高度的 {@link ImageSize} 对象，如果无法解析则返回 null
	 * @throws IOException 当读取数据发生错误时抛出
	 * @see #getSize(byte[], boolean)
	 * @since 1.0.0
	 */
	public static ImageSize getSize(final byte[] bytes) throws IOException {
		return getSize(bytes, true);
	}

	/**
	 * 获取字节数组数据的图像尺寸（可配置策略）
	 * <p>
	 * 根据 {@code useMetadata} 参数决定获取尺寸的策略：
	 * </p>
	 * <ul>
	 *   <li><b>true (推荐)：</b> 优先读取元数据（Metadata）。
	 *       <ul>
	 *           <li>如果元数据包含尺寸信息，直接返回（返回的是<b>原始存储尺寸</b>，不交换宽高）。</li>
	 *           <li>同时解析 EXIF 方向信息并存储在返回的 {@link ImageSize} 对象中。</li>
	 *           <li>如需获取视觉方向一致的尺寸，请调用 {@link ImageSize#getVisualSize()}。</li>
	 *       </ul>
	 *   </li>
	 *   <li><b>false (高性能)：</b> 仅通过 ImageIO 读取图像原始尺寸。
	 *       <ul>
	 *           <li>忽略任何 EXIF 方向信息。</li>
	 *           <li>返回图像存储的物理像素尺寸。</li>
	 *       </ul>
	 *   </li>
	 * </ul>
	 *
	 * @param bytes       要检查的字节数组，允许为 null 或空
	 * @param useMetadata 是否优先使用元数据获取尺寸
	 * @return 包含宽度和高度的 {@link ImageSize} 对象，如果无法解析则返回 null
	 * @throws IOException 当读取数据发生错误时抛出
	 * @see MetadataReader
	 * @see ImageReader
	 * @since 1.0.0
	 */
	public static ImageSize getSize(final byte[] bytes, final boolean useMetadata) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}

		UnsynchronizedByteArrayInputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
		return parseSizeByByteArrayInputStream(inputStream, bytes.length, useMetadata);
	}

	/**
	 * 获取输入流的图像尺寸
	 * <p>
	 * 这是一个便捷方法，等效于调用 {@code getSize(inputStream, true)}。
	 * 默认优先从元数据中解析尺寸信息。
	 * </p>
	 * <p><b>注意：</b></p>
	 * <ul>
	 *   <li>返回的是<b>原始存储尺寸</b>，<b>不会</b>根据 EXIF 方向交换宽高。</li>
	 * </ul>
	 *
	 * @param inputStream 输入流对象，必须非 null
	 * @return 包含宽度和高度的 {@link ImageSize} 对象，如果无法解析则返回 null
	 * @throws IOException              当流读取发生错误时抛出
	 * @throws IllegalArgumentException 当 inputStream 为 null 时抛出
	 * @see #getSize(InputStream, boolean)
	 * @since 1.0.0
	 */
	public static ImageSize getSize(final InputStream inputStream) throws IOException {
		return getSize(inputStream, true);
	}

	/**
	 * 获取输入流的图像尺寸（可配置策略）
	 * <p>
	 * 与 {@link #getSize(File, boolean)} 逻辑类似，但增加了对输入流的处理机制。
	 * </p>
	 * <p><b>注意：</b></p>
	 * <ul>
	 *   <li>无论是否使用元数据，返回的均是<b>原始存储尺寸</b>。</li>
	 *   <li>如果元数据包含方向信息，会被封装在返回的 {@link ImageSize} 对象中。</li>
	 *   <li>如需获取视觉方向一致的尺寸，请调用 {@link ImageSize#getVisualSize()}。</li>
	 * </ul>
	 * <p><b>流处理机制：</b></p>
	 * <ul>
	 *   <li>如果流支持 {@link InputStream#reset()}：直接在流上操作并在需要时重置。</li>
	 *   <li>如果流不支持 mark 且 {@code useMetadata} 为 true：
	 *       会将流内容缓冲到内存中（可能消耗较大内存），以便多次读取（一次读元数据，一次可能读 ImageIO）。
	 *   </li>
	 *   <li>如果流不支持 mark 且 {@code useMetadata} 为 false：
	 *       直接包装为 ImageInputStream 读取，不进行额外缓冲。
	 *   </li>
	 * </ul>
	 *
	 * @param inputStream 输入流对象，必须非 null
	 * @param useMetadata 是否优先使用元数据获取尺寸
	 * @return 包含宽度和高度的 {@link ImageSize} 对象，如果无法解析则返回 null
	 * @throws IOException              当流读取发生错误时抛出
	 * @throws IllegalArgumentException 当 inputStream 为 null 时抛出
	 * @see #getSize(File, boolean)
	 * @since 1.0.0
	 */
	public static ImageSize getSize(final InputStream inputStream, final boolean useMetadata) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		if (inputStream instanceof ByteArrayInputStream || inputStream instanceof UnsynchronizedByteArrayInputStream) {
			return parseSizeByByteArrayInputStream(inputStream, inputStream.available(), useMetadata);
		}

		if (!useMetadata) {
			try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
				if (Objects.isNull(imageInputStream)) {
					return null;
				}
				return parseSizeByImageInputStream(imageInputStream, null);
			}
		}

		Integer orientation = null;
		UnsynchronizedByteArrayOutputStream outputStream = IOUtils.toUnsynchronizedByteArrayOutputStream(inputStream);
		try (InputStream tmpInputStream = outputStream.toInputStream()) {
			Metadata metadata = ImageMetadataReader.readMetadata(tmpInputStream, outputStream.size());

			ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
			if (Objects.nonNull(exifIFD0Directory)) {
				orientation = exifIFD0Directory.getInteger(ExifDirectoryBase.TAG_ORIENTATION);
			}

			ImageSize imageSize = parseSizeByMetadata(metadata, exifIFD0Directory);
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
			return parseSizeByImageInputStream(imageInputStream, orientation);
		}
	}

	/**
	 * 从 ImageInputStream 获取图像原始尺寸
	 * <p>
	 * 直接利用 {@link ImageReader} 读取图像的物理宽、高。
	 * </p>
	 * <p><b>注意：</b></p>
	 * <ul>
	 *   <li><b>不处理方向：</b> 此方法忽略 EXIF Orientation 标签，返回的是图像存储的原始像素尺寸。</li>
	 *   <li><b>资源管理：</b> 方法内部会创建并销毁 ImageReader，但<b>不会关闭</b>传入的 {@code imageInputStream}。</li>
	 * </ul>
	 *
	 * @param imageInputStream 已创建的图像输入流，必须非 null
	 * @return 包含宽度和高度的 {@link ImageSize} 对象，如果无法解析则返回 null
	 * @throws IOException              当流读取发生错误时抛出
	 * @throws IllegalArgumentException 当 imageInputStream 为 null 时抛出
	 * @see ImageReader#getWidth(int)
	 * @see ImageReader#getHeight(int)
	 * @since 1.0.0
	 */
	public static ImageSize getSize(final ImageInputStream imageInputStream) throws IOException {
		return parseSizeByImageInputStream(imageInputStream, null);
	}

	/**
	 * 从元数据中获取图像尺寸
	 * <p>
	 * 解析图像元数据以提取宽度和高度信息。
	 * </p>
	 *
	 * <p><b>支持的数据源：</b></p>
	 * <ul>
	 *   <li><b>EXIF：</b> {@link ExifIFD0Directory}（最高优先级）</li>
	 *   <li><b>特定格式头：</b>
	 *     <ul>
	 *       <li>JPEG ({@link JpegDirectory})</li>
	 *       <li>PNG ({@link PngDirectory})</li>
	 *       <li>GIF ({@link GifImageDirectory})</li>
	 *       <li>BMP ({@link BmpHeaderDirectory})</li>
	 *       <li>WebP ({@link WebpDirectory})</li>
	 *       <li>PSD ({@link PsdHeaderDirectory})</li>
	 *       <li>ICO ({@link IcoDirectory})</li>
	 *       <li>HEIF ({@link HeifDirectory})</li>
	 *       <li>EPS ({@link EpsDirectory})</li>
	 *     </ul>
	 *   </li>
	 * </ul>
	 *
	 * @param metadata 包含图像信息的元数据对象，必须非null
	 * @return 解析出的 {@link ImageSize}（原始尺寸），如果无法从元数据中提取有效尺寸则返回 null
	 * @throws IllegalArgumentException 当 metadata 为 null 时抛出
	 * @see #parseSizeByMetadata(Metadata, ExifIFD0Directory)
	 * @since 1.0.0
	 */
	public static ImageSize getSize(final Metadata metadata) {
		Validate.notNull(metadata, "metadata 不可为 null");

		ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
		return parseSizeByMetadata(metadata, exifIFD0Directory);
	}

	/**
	 * 从文件中获取EXIF方向信息
	 * <p>
	 * 自动检测并处理EXIF方向信息，返回标准化的方向值。
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
	 * @param file 图像文件，不可为 null 且文件存在并可读取
	 * @return EXIF方向值（1-8），无法获取时返回{@link ImageConstants#NORMAL_EXIF_ORIENTATION}
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当file为null时抛出
	 * @see #getExifOrientation(Metadata)
	 * @since 1.0.0
	 */
	public static int getExifOrientation(final File file) throws IOException, ImageProcessingException {
		FileUtils.checkFile(file, "file 不可为 null");
		Metadata metadata = ImageMetadataReader.readMetadata(file);
		return getExifOrientation(metadata);
	}

	/**
	 * 从字节数组中获取EXIF方向信息
	 * <p>
	 * 自动检测并处理EXIF方向信息，返回标准化的方向值。
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
	 * @param bytes 图像字节数组，不可为 null/空数组
	 * @return EXIF方向值（1-8），未找到时返回{@link ImageConstants#NORMAL_EXIF_ORIENTATION}
	 * @throws IOException              当读取数据失败时抛出
	 * @throws ImageProcessingException 当图像处理异常时抛出
	 * @throws IllegalArgumentException 当bytes为null或空时抛出
	 * @see #getExifOrientation(Metadata)
	 * @since 1.0.0
	 */
	public static int getExifOrientation(final byte[] bytes) throws IOException, ImageProcessingException {
		Validate.isTrue(ArrayUtils.isNotEmpty(bytes), "bytes 不可为空");

		UnsynchronizedByteArrayInputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
		Metadata metadata = ImageMetadataReader.readMetadata(inputStream, bytes.length);
		return getExifOrientation(metadata);
	}

	/**
	 * 从输入流中获取EXIF方向信息
	 * <p>
	 * 自动检测并处理EXIF方向信息，返回标准化的方向值。
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
	 * @param inputStream 图像输入流，不可为 null
	 * @return EXIF方向值（1-8），未找到时返回{@link ImageConstants#NORMAL_EXIF_ORIENTATION}
	 * @throws IOException              当流读取失败时抛出
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
	 *   <li>获取Exif元数据</li>
	 *   <li>查找{@link ExifDirectoryBase#TAG_ORIENTATION}标签</li>
	 *   <li>返回找到的方向值</li>
	 * </ol>
	 *
	 * @param metadata 图像元数据对象，不可为 null
	 * @return EXIF方向值（1-8），未找到时返回{@link ImageConstants#NORMAL_EXIF_ORIENTATION}
	 * @throws IllegalArgumentException 当元数据为null时抛出
	 * @see ExifIFD0Directory
	 * @see ExifDirectoryBase#TAG_ORIENTATION
	 * @since 1.0.0
	 */
	public static int getExifOrientation(final Metadata metadata) {
		Validate.notNull(metadata, "metadata 不可为 null");

		ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
		if (Objects.nonNull(exifIFD0Directory)) {
			Integer orientation = exifIFD0Directory.getInteger(ExifDirectoryBase.TAG_ORIENTATION);
			if (Objects.nonNull(orientation)) {
				return orientation;
			}
		}
		return ImageConstants.NORMAL_EXIF_ORIENTATION;
	}

	/**
	 * 通过字节数组输入流解析图像尺寸（内部辅助方法）
	 * <p>
	 * 内部辅助方法，专门处理字节数组输入流。
	 * 根据 {@code useMetadata} 参数决定解析策略：
	 * <ul>
	 *   <li>{@code true}：优先尝试解析元数据获取尺寸和方向，失败后降级为直接读取</li>
	 *   <li>{@code false}：直接通过 ImageIO 读取图像尺寸（返回原始尺寸，不含方向信息）</li>
	 * </ul>
	 * </p>
	 *
	 * <p><b>实现细节：</b></p>
	 * <ul>
	 *   <li>利用 {@link InputStream#reset()} 实现流的重复读取</li>
	 *   <li>元数据解析失败时会自动捕获异常并尝试直接读取</li>
	 *   <li>最终解析会将 EXIF 方向信息存储在 {@link ImageSize} 中（不自动修正宽高）</li>
	 * </ul>
	 *
	 * <p><b>性能提示：</b></p>
	 * <ul>
	 *   <li>启用元数据解析可能需要读取更多数据</li>
	 *   <li>对于超大文件或确定无 EXIF 信息的图像，禁用元数据可提高性能</li>
	 * </ul>
	 *
	 * @param inputStream 输入流，必须满足：
	 *                    <ul>
	 *                      <li>非 null</li>
	 *                      <li>是{@link ByteArrayInputStream} 或 {@link UnsynchronizedByteArrayInputStream} 中的一种</li>
	 *                    </ul>
	 * @param streamLength 输入流内容长度
	 * @param useMetadata 是否优先尝试从元数据获取尺寸
	 * @return 图像尺寸对象，解析失败或无法识别格式时返回 null
	 * @throws IOException 当发生 I/O 错误或流重置失败时抛出
	 * @see InputStream#markSupported()
	 * @see InputStream#reset()
	 * @since 1.0.0
	 */
	protected static ImageSize parseSizeByByteArrayInputStream(final InputStream inputStream, final long streamLength,
															   final boolean useMetadata) throws IOException {
		if (!useMetadata) {
			try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
				if (Objects.isNull(imageInputStream)) {
					return null;
				}
				return parseSizeByImageInputStream(imageInputStream, null);
			}
		}

		Integer orientation = null;
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(inputStream, streamLength);

			ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
			if (Objects.nonNull(exifIFD0Directory)) {
				Integer orientationValue = exifIFD0Directory.getInteger(ExifDirectoryBase.TAG_ORIENTATION);
				if (Objects.nonNull(orientationValue)) {
					orientation = orientationValue;
				}
			}

			ImageSize imageSize = parseSizeByMetadata(metadata, exifIFD0Directory);
			if (Objects.nonNull(imageSize)) {
				return imageSize;
			}
		} catch (ImageProcessingException | IOException ignored) {
		}
		inputStream.reset();
		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
			if (Objects.isNull(imageInputStream)) {
				return null;
			}
			return parseSizeByImageInputStream(imageInputStream, orientation);
		}
	}

	/**
	 * 通过 ImageInputStream 解析图像尺寸（内部辅助方法）
	 * <p>
	 * 使用 ImageIO 读取图像的基本尺寸（宽、高），并将传入的 EXIF 方向信息存储在 {@link ImageSize} 对象中（不调整宽高）。
	 * </p>
	 *
	 * <p><b>实现细节：</b></p>
	 * <ul>
	 *   <li>使用 {@link ImageIO#getImageReaders(Object)} 获取合适的 ImageReader</li>
	 *   <li>读取第一帧的宽高（index 0）</li>
	 *   <li>将 orientation 参数存储到 ImageSize 中</li>
	 *   <li>自动释放 ImageReader 资源</li>
	 * </ul>
	 *
	 * @param imageInputStream 图像输入流，必须满足：
	 *                         <ul>
	 *                           <li>非null</li>
	 *                           <li>已定位到图像数据起始位置</li>
	 *                         </ul>
	 * @param orientation      EXIF 方向标识（1-8），用于构造 ImageSize
	 * @return 图像尺寸对象，如果有错误或无法找到 Reader 则返回 null
	 * @throws IOException              当读取发生 I/O 错误时抛出
	 * @throws IllegalArgumentException 当 imageInputStream 为 null 时抛出
	 * @see ImageReader
	 * @see ImageSize
	 * @since 1.0.0
	 */
	protected static ImageSize parseSizeByImageInputStream(final ImageInputStream imageInputStream,
														   final Integer orientation) throws IOException {
		Validate.notNull(imageInputStream, "imageInputStream 不可为 null");

		int width;
		int height;
		Iterator<ImageReader> iterator = ImageIO.getImageReaders(imageInputStream);
		if (!iterator.hasNext()) {
			return null;
		}
		ImageReader reader = iterator.next();
		reader.setInput(imageInputStream);
		width = reader.getWidth(0);
		height = reader.getHeight(0);
		reader.dispose();

		if (Objects.isNull(orientation)) {
			return new ImageSize(width, height);
		} else {
			return new ImageSize(width, height, orientation);
		}
	}

	/**
	 * 从元数据中解析图像尺寸（内部辅助方法）
	 * <p>
	 * 封装通用的元数据解析逻辑，从各类图像格式的元数据目录中提取宽、高及方向信息。
	 * </p>
	 *
	 * <p><b>解析策略：</b></p>
	 * <ol>
	 *   <li><b>EXIF优先：</b> 如果提供了 {@link ExifIFD0Directory}，优先尝试从中获取尺寸和方向</li>
	 *   <li><b>回退机制：</b> 若EXIF中无尺寸信息，遍历 {@link Metadata} 中的所有目录，
	 *       查找支持的格式目录（如BMP, PNG, JPEG等）并提取尺寸</li>
	 *   <li><b>方向存储：</b> 将解析到的 EXIF 方向信息存储在 {@link ImageSize} 对象中，如需获取视觉尺寸请调用 {@link ImageSize#getVisualSize()}</li>
	 * </ol>
	 *
	 * @param metadata          完整的元数据对象，不可为null
	 * @param exifIFD0Directory 预获取的EXIF IFD0目录，允许为null（为null时跳过EXIF优先步骤）
	 * @return 解析得到的 {@link ImageSize} 对象，如果无法提取有效宽高则返回null
	 * @throws IllegalArgumentException 当metadata为null时抛出
	 * @since 1.0.0
	 */
	protected static ImageSize parseSizeByMetadata(final Metadata metadata, final ExifIFD0Directory exifIFD0Directory) {
		Validate.notNull(metadata, "metadata 不可为 null");

		Integer imageOrientation = null;
		Integer imageWidth = null;
		Integer imageHeight = null;

		if (Objects.nonNull(exifIFD0Directory)) {
			imageOrientation = exifIFD0Directory.getInteger(ExifDirectoryBase.TAG_ORIENTATION);
			if (exifIFD0Directory.containsTag(ExifDirectoryBase.TAG_IMAGE_WIDTH) &&
				exifIFD0Directory.containsTag(ExifDirectoryBase.TAG_IMAGE_HEIGHT)) {
				imageWidth = exifIFD0Directory.getInteger(ExifDirectoryBase.TAG_IMAGE_WIDTH);
				imageHeight = exifIFD0Directory.getInteger(ExifDirectoryBase.TAG_IMAGE_HEIGHT);
			}
		}

		if (ObjectUtils.anyNull(imageWidth, imageHeight)) {
			for (Directory directory : metadata.getDirectories()) {
				if (directory instanceof BmpHeaderDirectory bmpHeaderDirectory) {
					if (bmpHeaderDirectory.containsTag(BmpHeaderDirectory.TAG_IMAGE_WIDTH) &&
						bmpHeaderDirectory.containsTag(BmpHeaderDirectory.TAG_IMAGE_HEIGHT)) {
						imageWidth = bmpHeaderDirectory.getInteger(BmpHeaderDirectory.TAG_IMAGE_WIDTH);
						imageHeight = bmpHeaderDirectory.getInteger(BmpHeaderDirectory.TAG_IMAGE_HEIGHT);
					}
					break;
				} else if (directory instanceof EpsDirectory epsDirectory) {
					if (epsDirectory.containsTag(EpsDirectory.TAG_IMAGE_WIDTH) &&
						epsDirectory.containsTag(EpsDirectory.TAG_IMAGE_HEIGHT)) {
						imageWidth = epsDirectory.getInteger(EpsDirectory.TAG_IMAGE_WIDTH);
						imageHeight = epsDirectory.getInteger(EpsDirectory.TAG_IMAGE_HEIGHT);
					}
					break;
				} else if (directory instanceof GifImageDirectory gifImageDirectory) {
					if (gifImageDirectory.containsTag(GifImageDirectory.TAG_WIDTH) &&
						gifImageDirectory.containsTag(GifImageDirectory.TAG_HEIGHT)) {
						imageWidth = gifImageDirectory.getInteger(GifImageDirectory.TAG_WIDTH);
						imageHeight = gifImageDirectory.getInteger(GifImageDirectory.TAG_HEIGHT);
					}
					break;
				} else if (directory instanceof HeifDirectory heifDirectory) {
					if (heifDirectory.containsTag(HeifDirectory.TAG_IMAGE_WIDTH) &&
						heifDirectory.containsTag(HeifDirectory.TAG_IMAGE_HEIGHT)) {
						imageWidth = heifDirectory.getInteger(HeifDirectory.TAG_IMAGE_WIDTH);
						imageHeight = heifDirectory.getInteger(HeifDirectory.TAG_IMAGE_HEIGHT);
					}
					break;
				} else if (directory instanceof IcoDirectory icoDirectory) {
					if (icoDirectory.containsTag(IcoDirectory.TAG_IMAGE_WIDTH) &&
						icoDirectory.containsTag(IcoDirectory.TAG_IMAGE_HEIGHT)) {
						imageWidth = icoDirectory.getInteger(IcoDirectory.TAG_IMAGE_WIDTH);
						imageHeight = icoDirectory.getInteger(IcoDirectory.TAG_IMAGE_HEIGHT);
					}
					break;
				} else if (directory instanceof JpegDirectory jpegDirectory) {
					if (jpegDirectory.containsTag(JpegDirectory.TAG_IMAGE_WIDTH) &&
						jpegDirectory.containsTag(JpegDirectory.TAG_IMAGE_HEIGHT)) {
						imageWidth = jpegDirectory.getInteger(JpegDirectory.TAG_IMAGE_WIDTH);
						imageHeight = jpegDirectory.getInteger(JpegDirectory.TAG_IMAGE_HEIGHT);
					}
					break;
				} else if (directory instanceof PsdHeaderDirectory exifDirectory) {
					if (exifDirectory.containsTag(PsdHeaderDirectory.TAG_IMAGE_WIDTH) &&
						exifDirectory.containsTag(PsdHeaderDirectory.TAG_IMAGE_HEIGHT)) {
						imageWidth = exifDirectory.getInteger(PsdHeaderDirectory.TAG_IMAGE_WIDTH);
						imageHeight = exifDirectory.getInteger(PsdHeaderDirectory.TAG_IMAGE_HEIGHT);
					}
					break;
				} else if (directory instanceof PngDirectory pngDirectory) {
					if (pngDirectory.containsTag(PngDirectory.TAG_IMAGE_WIDTH) &&
						pngDirectory.containsTag(PngDirectory.TAG_IMAGE_HEIGHT)) {
						imageWidth = pngDirectory.getInteger(PngDirectory.TAG_IMAGE_WIDTH);
						imageHeight = pngDirectory.getInteger(PngDirectory.TAG_IMAGE_HEIGHT);
					}
					break;
				} else if (directory instanceof WebpDirectory webpDirectory) {
					if (webpDirectory.containsTag(WebpDirectory.TAG_IMAGE_WIDTH) &&
						webpDirectory.containsTag(WebpDirectory.TAG_IMAGE_HEIGHT)) {
						imageWidth = webpDirectory.getInteger(WebpDirectory.TAG_IMAGE_WIDTH);
						imageHeight = webpDirectory.getInteger(WebpDirectory.TAG_IMAGE_HEIGHT);
					}
					break;
				}
			}
			if (ObjectUtils.anyNull(imageWidth, imageHeight)) {
				return null;
			}
		}

		if (Objects.isNull(imageOrientation)) {
			return new ImageSize(imageWidth, imageHeight);
		} else {
			return new ImageSize(imageWidth, imageHeight, imageOrientation);
		}
	}
}