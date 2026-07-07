/*
 *   Copyright 2026 pangju666
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

package io.github.pangju666.commons.image.model;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.twelvemonkeys.image.ImageUtil;
import io.github.pangju666.commons.image.lang.ImageConstants;
import io.github.pangju666.commons.image.utils.ImageUtils;
import io.github.pangju666.commons.io.model.IOResource;
import io.github.pangju666.commons.io.utils.FilenameUtils;
import org.apache.commons.lang3.Validate;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * 图像IO资源封装类
 * <p>继承自 {@link IOResource}，提供图像特定的功能封装，包括图像尺寸获取、元数据解析、
 * EXIF方向信息处理、BufferedImage缓存与深拷贝等功能。</p>
 *
 * <h3>核心特性：</h3>
 * <ul>
 *     <li><strong>图像尺寸获取</strong> - 支持从元数据或BufferedImage获取图像尺寸</li>
 *     <li><strong>元数据解析</strong> - 基于Metadata Extractor解析图像元数据</li>
 *     <li><strong>EXIF方向处理</strong> - 支持EXIF方向信息的读取和设置</li>
 *     <li><strong>BufferedImage缓存</strong> - 可选的BufferedImage缓存机制</li>
 *     <li><strong>BufferedImage深拷贝</strong> - 提供深拷贝方法，避免修改缓存的原始图像</li>
 *     <li><strong>格式识别</strong> - 自动识别图像格式（文件模式）</li>
 *     <li><strong>ImageInputStream支持</strong> - 提供ImageInputStream接口</li>
 * </ul>
 *
 * <h3>使用场景：</h3>
 * <ul>
 *     <li>图像文件处理</li>
 *     <li>图像元数据提取</li>
 *     <li>图像尺寸获取</li>
 *     <li>EXIF方向校正</li>
 *     <li>图像编辑前的资源准备（配合ImageEditor使用）</li>
 * </ul>
 *
 * <h3>注意事项：</h3>
 * <ul>
 *     <li>资源关闭后禁止执行任何操作</li>
 *     <li>BufferedImage在资源关闭时会被flush</li>
 *     <li>字节数组和输入流模式无法自动识别格式</li>
 *     <li>EXIF方向值必须介于1-8之间</li>
 *     <li>使用深拷贝方法会消耗更多内存，仅在需要修改图像时使用</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.1.0
 */
public class ImageIOResource extends IOResource {
	/**
	 * EXIF方向值
	 * <p>取值范围1-8，表示图像的旋转和翻转信息</p>
	 *
	 * @since 1.1.0
	 */
	protected final Integer exifOrientation;
	/**
	 * 图像格式
	 * <p>文件模式自动识别（如JPEG、PNG），字节数组/输入流模式为null</p>
	 *
	 * @since 1.1.0
	 */
	protected final String format;
	/**
	 * 图像尺寸
	 * <p>包含宽度、高度和EXIF方向信息</p>
	 *
	 * @since 1.1.0
	 */
	protected volatile ImageSize imageSize;
	/**
	 * 缓存的BufferedImage
	 * <p>用于避免重复解码图像</p>
	 *
	 * @since 1.1.0
	 */
	protected volatile BufferedImage bufferedImage;
	/**
	 * 图像元数据
	 * <p>基于Metadata Extractor解析的图像元数据</p>
	 *
	 * @since 1.1.0
	 */
	protected volatile Metadata metadata;

	/**
	 * 基于IOResource构造ImageIOResource（自动解析EXIF方向）
	 * <p>从现有IOResource创建ImageIOResource，自动解析EXIF方向信息。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>源资源必须未关闭</li>
	 *     <li>自动验证资源是否为图像类型</li>
	 *     <li>文件模式自动识别格式</li>
	 *     <li>自动解析EXIF方向信息</li>
	 * </ul>
	 *
	 * @param resource 源资源（必须非null且未关闭）
	 * @throws IOException              当资源读取失败时抛出
	 * @throws IllegalArgumentException 当resource已关闭或不是图像资源时抛出
	 * @since 1.1.0
	 */
	public ImageIOResource(IOResource resource) throws IOException {
		super(resource);

		if (resource instanceof ImageIOResource) {
			this.format = ((ImageIOResource) resource).format;
			this.exifOrientation = ((ImageIOResource) resource).exifOrientation;
		} else {
			if (Objects.nonNull(file)) {
				validateImageType("resource 不是图像资源");

				this.format = FilenameUtils.getExtension(file.getName()).toUpperCase();

				int exifOrientation = ImageConstants.NORMAL_EXIF_ORIENTATION;
				try {
					this.metadata = ImageMetadataReader.readMetadata(file);
					exifOrientation = ImageUtils.getExifOrientation(this.metadata);
				} catch (ImageProcessingException ignored) {
				}
				this.exifOrientation = exifOrientation;
			} else {
				this.format = null;

				validateImageType("resource 不是图像资源");

				int exifOrientation = ImageConstants.NORMAL_EXIF_ORIENTATION;
				try (InputStream inputStream = toInputStream(this.bytes)) {
					this.metadata = ImageMetadataReader.readMetadata(inputStream);
					exifOrientation = ImageUtils.getExifOrientation(this.metadata);
				} catch (ImageProcessingException ignored) {
				}
				this.exifOrientation = exifOrientation;
			}
		}
	}

	/**
	 * 基于IOResource构造ImageIOResource（可选解析EXIF方向）
	 * <p>从现有IOResource创建ImageIOResource，可选择是否解析EXIF方向信息。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>源资源必须未关闭</li>
	 *     <li>自动验证资源是否为图像类型</li>
	 *     <li>文件模式自动识别格式</li>
	 *     <li>根据参数决定是否解析EXIF方向</li>
	 * </ul>
	 *
	 * @param resource             源资源（必须非null且未关闭）
	 * @param parseExifOrientation 是否解析EXIF方向信息
	 * @throws IOException              当资源读取失败时抛出
	 * @throws IllegalArgumentException 当resource已关闭或不是图像资源时抛出
	 * @since 1.1.0
	 */
	public ImageIOResource(IOResource resource, boolean parseExifOrientation) throws IOException {
		super(resource);

		if (Objects.nonNull(file)) {
			if (!(resource instanceof ImageIOResource)) {
				validateImageType("resource 不是图像资源");
			}

			this.format = FilenameUtils.getExtension(file.getName()).toUpperCase();

			int exifOrientation = ImageConstants.NORMAL_EXIF_ORIENTATION;
			if (parseExifOrientation) {
				try {
					this.metadata = ImageMetadataReader.readMetadata(file);
					exifOrientation = ImageUtils.getExifOrientation(this.metadata);
				} catch (ImageProcessingException ignored) {
				}
			}
			this.exifOrientation = exifOrientation;
		} else {
			this.format = null;

			if (!(resource instanceof ImageIOResource)) {
				validateImageType("resource 不是图像资源");
			}

			int exifOrientation = ImageConstants.NORMAL_EXIF_ORIENTATION;
			if (parseExifOrientation) {
				try (InputStream inputStream = toInputStream(this.bytes)) {
					this.metadata = ImageMetadataReader.readMetadata(inputStream);
					exifOrientation = ImageUtils.getExifOrientation(this.metadata);
				} catch (ImageProcessingException ignored) {
				}
			}
			this.exifOrientation = exifOrientation;
		}
	}

	/**
	 * 基于IOResource构造ImageIOResource（指定EXIF方向）
	 * <p>从现有IOResource创建ImageIOResource，使用指定的EXIF方向值。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>源资源必须未关闭</li>
	 *     <li>自动验证资源是否为图像类型</li>
	 *     <li>文件模式自动识别格式</li>
	 *     <li>不自动解析EXIF方向，使用指定值</li>
	 * </ul>
	 *
	 * @param resource        源资源（必须非null且未关闭）
	 * @param exifOrientation EXIF方向值（必须介于1-8之间）
	 * @throws IllegalArgumentException 当resource已关闭、不是图像资源或exifOrientation不在1-8范围内时抛出
	 * @since 1.1.0
	 */
	public ImageIOResource(IOResource resource, int exifOrientation) {
		super(resource);

		if (Objects.nonNull(file)) {
			if (!(resource instanceof ImageIOResource)) {
				validateImageType("resource 不是图像资源");
			}

			this.format = FilenameUtils.getExtension(file.getName()).toUpperCase();
		} else {
			if (!(resource instanceof ImageIOResource)) {
				validateImageType("resource 不是图像资源");
			}

			this.format = null;
		}

		this.exifOrientation = exifOrientation;
	}

	/**
	 * 基于文件路径构造ImageIOResource（自动解析EXIF方向）
	 * <p>从文件路径创建ImageIOResource，自动解析EXIF方向信息。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>自动验证文件是否为图像类型</li>
	 *     <li>自动识别图像格式</li>
	 *     <li>自动解析EXIF方向信息</li>
	 * </ul>
	 *
	 * @param filePath 文件路径（必须非空）
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当filePath为空或文件不是图像文件时抛出
	 * @since 1.1.0
	 */
	public ImageIOResource(String filePath) throws IOException {
		this(filePath, true);
	}

	/**
	 * 基于文件路径构造ImageIOResource（可选解析EXIF方向）
	 * <p>从文件路径创建ImageIOResource，可选择是否解析EXIF方向信息。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>自动验证文件是否为图像类型</li>
	 *     <li>自动识别图像格式</li>
	 *     <li>根据参数决定是否解析EXIF方向</li>
	 * </ul>
	 *
	 * @param filePath             文件路径（必须非空）
	 * @param parseExifOrientation 是否解析EXIF方向信息
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当filePath为空或文件不是图像文件时抛出
	 * @since 1.1.0
	 */
	public ImageIOResource(String filePath, boolean parseExifOrientation) throws IOException {
		super(filePath, false);

		validateImageType("file 不是图像文件");

		this.format = FilenameUtils.getExtension(file.getName()).toUpperCase();

		int exifOrientation = ImageConstants.NORMAL_EXIF_ORIENTATION;
		if (parseExifOrientation) {
			try {
				this.metadata = ImageMetadataReader.readMetadata(file);
				exifOrientation = ImageUtils.getExifOrientation(this.metadata);
			} catch (ImageProcessingException ignored) {
			}
		}
		this.exifOrientation = exifOrientation;
	}

	/**
	 * 基于文件路径构造ImageIOResource（指定EXIF方向）
	 * <p>从文件路径创建ImageIOResource，使用指定的EXIF方向值。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>自动验证文件是否为图像类型</li>
	 *     <li>自动识别图像格式</li>
	 *     <li>不自动解析EXIF方向，使用指定值</li>
	 * </ul>
	 *
	 * @param filePath        文件路径（必须非空）
	 * @param exifOrientation EXIF方向值（必须介于1-8之间）
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当filePath为空、文件不是图像文件或exifOrientation不在1-8范围内时抛出
	 * @since 1.1.0
	 */
	public ImageIOResource(String filePath, int exifOrientation) throws IOException {
		super(filePath, false);

		Validate.inclusiveBetween(1, 8, exifOrientation, "exifOrientation 必须介于1-8之间");
		validateImageType("file 不是图像文件");

		this.format = FilenameUtils.getExtension(file.getName()).toUpperCase();
		this.exifOrientation = exifOrientation;
	}


	/**
	 * 基于File对象构造ImageIOResource（自动解析EXIF方向）
	 * <p>从File对象创建ImageIOResource，自动解析EXIF方向信息。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>自动验证文件是否为图像类型</li>
	 *     <li>自动识别图像格式</li>
	 *     <li>自动解析EXIF方向信息</li>
	 * </ul>
	 *
	 * @param file 文件对象（必须非null）
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当file为null或文件不是图像文件时抛出
	 * @since 1.1.0
	 */
	public ImageIOResource(File file) throws IOException {
		this(file, true);
	}

	/**
	 * 基于File对象构造ImageIOResource（可选解析EXIF方向）
	 * <p>从File对象创建ImageIOResource，可选择是否解析EXIF方向信息。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>自动验证文件是否为图像类型</li>
	 *     <li>自动识别图像格式</li>
	 *     <li>根据参数决定是否解析EXIF方向</li>
	 * </ul>
	 *
	 * @param file                 文件对象（必须非null）
	 * @param parseExifOrientation 是否解析EXIF方向信息
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当file为null或文件不是图像文件时抛出
	 * @since 1.1.0
	 */
	public ImageIOResource(File file, boolean parseExifOrientation) throws IOException {
		super(file, false);

		validateImageType("file 不是图像文件");

		this.format = FilenameUtils.getExtension(file.getName()).toUpperCase();

		int exifOrientation = ImageConstants.NORMAL_EXIF_ORIENTATION;
		if (parseExifOrientation) {
			try {
				this.metadata = ImageMetadataReader.readMetadata(file);
				exifOrientation = ImageUtils.getExifOrientation(this.metadata);
			} catch (ImageProcessingException ignored) {
			}
		}
		this.exifOrientation = exifOrientation;
	}

	/**
	 * 基于File对象构造ImageIOResource（指定EXIF方向）
	 * <p>从File对象创建ImageIOResource，使用指定的EXIF方向值。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>自动验证文件是否为图像类型</li>
	 *     <li>自动识别图像格式</li>
	 *     <li>不自动解析EXIF方向，使用指定值</li>
	 * </ul>
	 *
	 * @param file            文件对象（必须非null）
	 * @param exifOrientation EXIF方向值（必须介于1-8之间）
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当file为null、文件不是图像文件或exifOrientation不在1-8范围内时抛出
	 * @since 1.1.0
	 */
	public ImageIOResource(File file, int exifOrientation) throws IOException {
		super(file, false);

		Validate.inclusiveBetween(1, 8, exifOrientation, "exifOrientation 必须介于1-8之间");
		validateImageType("file 不是图像文件");

		this.format = FilenameUtils.getExtension(file.getName()).toUpperCase();
		this.exifOrientation = exifOrientation;
	}

	/**
	 * 基于字节数组构造ImageIOResource（自动解析EXIF方向）
	 * <p>从字节数组创建ImageIOResource，自动解析EXIF方向信息。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>自动验证数据是否为图像类型</li>
	 *     <li>无法自动识别格式，format为null</li>
	 *     <li>自动解析EXIF方向信息</li>
	 * </ul>
	 *
	 * @param bytes 字节数组（必须非空）
	 * @throws IOException              当数据读取失败时抛出
	 * @throws IllegalArgumentException 当bytes为空或数据不是图像数据时抛出
	 * @since 1.1.0
	 */
	public ImageIOResource(byte[] bytes) throws IOException {
		this(bytes, true);
	}

	/**
	 * 基于字节数组构造ImageIOResource（可选解析EXIF方向）
	 * <p>从字节数组创建ImageIOResource，可选择是否解析EXIF方向信息。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>自动验证数据是否为图像类型</li>
	 *     <li>无法自动识别格式，format为null</li>
	 *     <li>根据参数决定是否解析EXIF方向</li>
	 * </ul>
	 *
	 * @param bytes                字节数组（必须非空）
	 * @param parseExifOrientation 是否解析EXIF方向信息
	 * @throws IOException              当数据读取失败时抛出
	 * @throws IllegalArgumentException 当bytes为空或数据不是图像数据时抛出
	 * @since 1.1.0
	 */
	public ImageIOResource(byte[] bytes, boolean parseExifOrientation) throws IOException {
		super(bytes);

		validateImageType("bytes 不是图像数据");

		this.format = null;

		int exifOrientation = ImageConstants.NORMAL_EXIF_ORIENTATION;
		if (parseExifOrientation) {
			try (InputStream inputStream = toInputStream(this.bytes)) {
				this.metadata = ImageMetadataReader.readMetadata(inputStream);
				exifOrientation = ImageUtils.getExifOrientation(this.metadata);
			} catch (ImageProcessingException ignored) {
			}
		}
		this.exifOrientation = exifOrientation;
	}

	/**
	 * 基于字节数组构造ImageIOResource（指定EXIF方向）
	 * <p>从字节数组创建ImageIOResource，使用指定的EXIF方向值。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>自动验证数据是否为图像类型</li>
	 *     <li>无法自动识别格式，format为null</li>
	 *     <li>不自动解析EXIF方向，使用指定值</li>
	 * </ul>
	 *
	 * @param bytes           字节数组（必须非空）
	 * @param exifOrientation EXIF方向值（必须介于1-8之间）
	 * @throws IllegalArgumentException 当bytes为空、数据不是图像数据或exifOrientation不在1-8范围内时抛出
	 * @since 1.1.0
	 */
	public ImageIOResource(byte[] bytes, int exifOrientation) {
		super(bytes);

		Validate.inclusiveBetween(1, 8, exifOrientation, "exifOrientation 必须介于1-8之间");
		validateImageType("bytes 不是图像数据");

		this.format = null;
		this.exifOrientation = exifOrientation;
	}

	/**
	 * 基于输入流构造ImageIOResource（自动解析EXIF方向）
	 * <p>从输入流创建ImageIOResource，自动解析EXIF方向信息。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>自动验证数据是否为图像类型</li>
	 *     <li>无法自动识别格式，format为null</li>
	 *     <li>自动解析EXIF方向信息</li>
	 * </ul>
	 *
	 * @param inputStream 输入流（必须非null）
	 * @throws IOException              当流读取失败时抛出
	 * @throws IllegalArgumentException 当inputStream为null或数据不是图像数据时抛出
	 * @since 1.1.0
	 */
	public ImageIOResource(InputStream inputStream) throws IOException {
		this(inputStream, true);
	}

	/**
	 * 基于输入流构造ImageIOResource（可选解析EXIF方向）
	 * <p>从输入流创建ImageIOResource，可选择是否解析EXIF方向信息。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>自动验证数据是否为图像类型</li>
	 *     <li>无法自动识别格式，format为null</li>
	 *     <li>根据参数决定是否解析EXIF方向</li>
	 * </ul>
	 *
	 * @param inputStream          输入流（必须非null）
	 * @param parseExifOrientation 是否解析EXIF方向信息
	 * @throws IOException              当流读取失败时抛出
	 * @throws IllegalArgumentException 当inputStream为null或数据不是图像数据时抛出
	 * @since 1.1.0
	 */
	public ImageIOResource(InputStream inputStream, boolean parseExifOrientation) throws IOException {
		super(inputStream);

		validateImageType("inputStream 不是图像数据输入流");

		this.format = null;

		int exifOrientation = ImageConstants.NORMAL_EXIF_ORIENTATION;
		if (parseExifOrientation) {
			try (InputStream tmpInputStream = toInputStream(this.bytes)) {
				this.metadata = ImageMetadataReader.readMetadata(tmpInputStream);
				exifOrientation = ImageUtils.getExifOrientation(this.metadata);
			} catch (ImageProcessingException ignored) {
			}
		}
		this.exifOrientation = exifOrientation;
	}

	/**
	 * 基于输入流构造ImageIOResource（指定EXIF方向）
	 * <p>从输入流创建ImageIOResource，使用指定的EXIF方向值。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>自动验证数据是否为图像类型</li>
	 *     <li>无法自动识别格式，format为null</li>
	 *     <li>不自动解析EXIF方向，使用指定值</li>
	 * </ul>
	 *
	 * @param inputStream     输入流（必须非null）
	 * @param exifOrientation EXIF方向值（必须介于1-8之间）
	 * @throws IOException              当流读取失败时抛出
	 * @throws IllegalArgumentException 当inputStream为null、数据不是图像数据或exifOrientation不在1-8范围内时抛出
	 * @since 1.1.0
	 */
	public ImageIOResource(InputStream inputStream, int exifOrientation) throws IOException {
		super(inputStream);

		Validate.inclusiveBetween(1, 8, exifOrientation, "exifOrientation 必须介于1-8之间");
		validateImageType("inputStream 不是图像数据输入流");

		this.format = null;
		this.exifOrientation = exifOrientation;
	}

	/**
	 * 获取图像尺寸
	 * <p>优先从元数据获取尺寸，若元数据中不存在则从BufferedImage获取。</p>
	 *
	 * <p>实现特性：</p>
	 * <ul>
	 *     <li>线程安全，使用synchronized保护</li>
	 *     <li>结果会被缓存，避免重复计算</li>
	 *     <li>包含EXIF方向信息</li>
	 * </ul>
	 *
	 * @return 图像尺寸对象（包含宽度、高度和EXIF方向）
	 * @throws IOException 当资源已关闭或图像读取失败时抛出
	 * @since 1.1.0
	 */
	public ImageSize getImageSize() throws IOException {
		checkClosed();

		synchronized (this) {
			if (Objects.nonNull(imageSize)) {
				return imageSize;
			}

			if (Objects.nonNull(metadata)) {
				imageSize = ImageUtils.getSize(metadata);
			}

			if (Objects.isNull(imageSize)) {
				BufferedImage image = getBufferedImage();
				imageSize = new ImageSize(image.getWidth(), image.getHeight(), exifOrientation);
			}

			return imageSize;
		}
	}

	/**
	 * 设置图像尺寸
	 * <p>用于手动设置图像尺寸，覆盖自动计算的值。</p>
	 *
	 * @param imageSize 图像尺寸对象
	 * @throws IOException 当资源已关闭时抛出
	 * @since 1.1.0
	 */
	public void setImageSize(ImageSize imageSize) throws IOException {
		checkClosed();

		this.imageSize = imageSize;
	}

	/**
	 * 获取图像元数据
	 * <p>使用Metadata Extractor解析图像元数据，若解析失败返回空Metadata对象。</p>
	 *
	 * <p>实现特性：</p>
	 * <ul>
	 *     <li>线程安全，使用synchronized保护</li>
	 *     <li>结果会被缓存，避免重复解析</li>
	 *     <li>解析失败时返回空Metadata而非null</li>
	 * </ul>
	 *
	 * @return 图像元数据对象
	 * @throws IOException 当资源已关闭时抛出
	 * @since 1.1.0
	 */
	public Metadata getMetadata() throws IOException {
		checkClosed();

		synchronized (this) {
			if (Objects.nonNull(metadata)) {
				return metadata;
			}

			if (Objects.nonNull(file)) {
				try {
					metadata = ImageMetadataReader.readMetadata(file);
				} catch (ImageProcessingException ignored) {
					metadata = new Metadata();
				}
			} else {
				try (InputStream inputStream = toInputStream(bytes)) {
					metadata = ImageMetadataReader.readMetadata(inputStream);
				} catch (ImageProcessingException ignored) {
					metadata = new Metadata();
				}
			}
			return metadata;
		}
	}

	/**
	 * 设置图像元数据
	 * <p>用于手动设置图像元数据，覆盖自动解析的值。</p>
	 *
	 * @param metadata 图像元数据对象
	 * @throws IOException 当资源已关闭时抛出
	 * @since 1.1.0
	 */
	public void setMetadata(Metadata metadata) throws IOException {
		checkClosed();

		this.metadata = metadata;
	}

	/**
	 * 获取图像格式
	 * <p>文件模式自动识别格式（如JPEG、PNG），字节数组/输入流模式返回null。</p>
	 *
	 * @return 图像格式字符串（大写），字节数组/输入流模式返回null
	 * @since 1.1.0
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * 获取BufferedImage
	 * <p>使用ImageIO读取图像并返回BufferedImage对象。</p>
	 *
	 * <p>实现特性：</p>
	 * <ul>
	 *     <li>线程安全，使用synchronized保护</li>
	 *     <li>结果会被缓存，避免重复解码</li>
	 *     <li>读取失败时抛出IOException</li>
	 * </ul>
	 *
	 * @return BufferedImage对象
	 * @throws IOException 当资源已关闭或图像读取失败时抛出
	 * @since 1.1.0
	 */
	public BufferedImage getBufferedImage() throws IOException {
		checkClosed();

		synchronized (this) {
			if (Objects.nonNull(bufferedImage)) {
				return bufferedImage;
			}

			if (Objects.nonNull(file)) {
				bufferedImage = ImageIO.read(file);
				if (Objects.isNull(bufferedImage)) {
					throw new IOException("图片读取失败，文件路径：" + file.getAbsolutePath());
				}
			} else {
				try (InputStream inputStream = toInputStream(this.bytes)) {
					bufferedImage = ImageIO.read(inputStream);
					if (Objects.isNull(bufferedImage)) {
						throw new IOException("图片读取失败");
					}
				}
			}
			return bufferedImage;
		}
	}

	/**
	 * 获取BufferedImage的深拷贝副本
	 * <p>
	 * 返回缓存的BufferedImage的深拷贝，避免直接修改缓存的图像。
	 * 如果缓存中没有图像，会先加载图像并创建副本。
	 * </p>
	 *
	 * <p><b>实现特性：</b></p>
	 * <ul>
	 *   <li>使用 {@link ImageUtil#createCopy(BufferedImage)} 创建深拷贝</li>
	 *   <li>线程安全 - 使用 synchronized 保护</li>
	 *   <li>先检查资源关闭状态，再获取锁（提高性能）</li>
	 *   <li>如果缓存为空，先调用 {@link #getBufferedImage()} 加载图像</li>
	 *   <li>返回的副本与缓存完全独立，修改副本不影响缓存</li>
	 * </ul>
	 *
	 * <p><b>使用场景：</b></p>
	 * <ul>
	 *   <li>需要对图像进行修改但不影响原始缓存时</li>
	 *   <li>需要将图像传递给外部代码时</li>
	 *   <li>需要多次处理同一图像时</li>
	 * </ul>
	 *
	 * <p><b>注意事项：</b></p>
	 * <ul>
	 *   <li>此方法会创建新的图像对象，消耗更多内存</li>
	 *   <li>如果只需要读取图像，建议使用 {@link #getBufferedImage()}</li>
	 *   <li>资源关闭后调用会抛出异常</li>
	 * </ul>
	 *
	 * @return BufferedImage的深拷贝副本
	 * @throws IOException 当资源已关闭或图像读取失败时抛出
	 * @see #getBufferedImage()
	 * @see ImageUtil#createCopy(BufferedImage)
	 * @since 1.1.0
	 */
	public BufferedImage getBufferedImageCopy() throws IOException {
		checkClosed();

		synchronized (this) {
			BufferedImage src = bufferedImage;
			if (Objects.isNull(src)) {
				src = getBufferedImage();
			}
			return ImageUtil.createCopy(src);
		}
	}

	/**
	 * 打开ImageInputStream
	 * <p>创建ImageInputStream用于图像处理，每次调用创建新实例。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>调用者负责关闭返回的流</li>
	 *     <li>每次调用返回新实例</li>
	 * </ul>
	 *
	 * @return ImageInputStream对象
	 * @throws IOException 当资源已关闭或流创建失败时抛出
	 * @since 1.1.0
	 */
	public ImageInputStream openImageInputStream() throws IOException {
		checkClosed();

		if (Objects.nonNull(file)) {
			return ImageIO.createImageInputStream(file);
		} else {
			return ImageIO.createImageInputStream(toInputStream(bytes));
		}
	}

	/**
	 * 关闭资源
	 * <p>释放图像相关资源，包括flush BufferedImage、清空元数据和尺寸缓存。</p>
	 *
	 * <p>清理操作：</p>
	 * <ul>
	 *     <li>flush BufferedImage（释放图像内存）</li>
	 *     <li>清空metadata缓存</li>
	 *     <li>清空imageSize缓存</li>
	 *     <li>清空bufferedImage缓存</li>
	 *     <li>调用父类close方法</li>
	 * </ul>
	 *
	 * @throws IOException 当资源关闭失败时抛出
	 * @since 1.1.0
	 */
	@Override
	public synchronized void close() throws IOException {
		if (Objects.nonNull(this.bufferedImage)) {
			this.bufferedImage.flush();
		}

		this.metadata = null;
		this.bufferedImage = null;
		this.imageSize = null;

		super.close();
	}

	/**
	 * 验证图像类型
	 * <p>验证资源是否为支持的图像类型。</p>
	 *
	 * <p>验证内容：</p>
	 * <ul>
	 *     <li>MIME类型是否为图像类型</li>
	 *     <li>图像类型是否支持读取</li>
	 * </ul>
	 *
	 * @param message 验证失败时的错误消息
	 * @throws IllegalArgumentException 当资源不是图像类型或不支持读取时抛出
	 * @since 1.1.0
	 */
	protected void validateImageType(String message) {
		Validate.isTrue(isImage(), message);
		Validate.isTrue(ImageUtils.isSupportReadType(mimeType), "不支持读取 " + mimeType + " 类型图像");
	}
}
