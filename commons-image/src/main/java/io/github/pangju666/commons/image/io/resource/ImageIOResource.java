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

package io.github.pangju666.commons.image.io.resource;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.twelvemonkeys.image.ImageUtil;
import io.github.pangju666.commons.image.lang.ImageConstants;
import io.github.pangju666.commons.image.model.ImageSize;
import io.github.pangju666.commons.image.utils.ImageUtils;
import io.github.pangju666.commons.io.exception.UnsupportedResourceException;
import io.github.pangju666.commons.io.resource.IOResource;
import io.github.pangju666.commons.io.utils.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
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
 * EXIF方向自动校正、BufferedImage缓存与深拷贝等功能。</p>
 *
 * <h3>核心特性：</h3>
 * <ul>
 *     <li><strong>图像尺寸获取</strong> - 支持从元数据或BufferedImage获取图像尺寸</li>
 *     <li><strong>元数据解析</strong> - 基于Metadata Extractor解析图像元数据</li>
 *     <li><strong>EXIF方向自动校正</strong> - 可选择在构造时根据 EXIF 方向校正图像，并在需要时缓存校正后的结果</li>
 *     <li><strong>BufferedImage缓存</strong> - 缓存解码后的图像，避免重复解码</li>
 *     <li><strong>BufferedImage深拷贝</strong> - 提供深拷贝方法，避免修改缓存的原始图像</li>
 *     <li><strong>图像格式识别</strong> - 支持从MIME类型获取ImageIO支持的format</li>
 *     <li><strong>ImageInputStream支持</strong> - 提供ImageInputStream接口</li>
 * </ul>
 *
 * <h3>使用场景：</h3>
 * <ul>
 *     <li>图像处理应用</li>
 *     <li>图像上传处理</li>
 *     <li>图像尺寸调整</li>
 *     <li>图像元数据解析</li>
 *     <li>图像方向自动校正</li>
 * </ul>
 *
 * <h3>注意事项：</h3>
 * <ul>
 *     <li>资源关闭后禁止执行任何操作</li>
 *     <li>BufferedImage在资源关闭时会被flush</li>
 *     <li>当启用方向校正且 EXIF 方向不为正常值时，会自动解码图像并缓存校正后的结果</li>
 *     <li>使用深拷贝方法会消耗更多内存，仅在需要修改图像时使用</li>
 *     <li>imageFormat 是从父类 format 推断的、且被 ImageIO 支持读取的图像格式，仅当该格式存在于 {@link ImageConstants#getSupportedReadImageFormats()} 中时才会被赋值</li>
 * </ul>
 *
 * @author pangju666
 * @since 2.1.0
 */
public class ImageIOResource extends IOResource {
	/**
	 * 图像格式
	 * <p>从父类 {@code format} 推断出的、被 ImageIO 支持读取的图像格式（如 JPEG、PNG）。</p>
	 * <p>仅当该格式存在于 {@link ImageConstants#getSupportedReadImageFormats()} 中时才会被赋值，否则为 {@code null}。</p>
	 *
	 * @since 2.1.0
	 */
	protected final String imageFormat;
	/**
	 * EXIF 方向是否已校正
	 * <p>标记当前资源是否按照 EXIF 方向处理逻辑构造。</p>
	 *
	 * <p>取值说明：</p>
	 * <ul>
	 *   <li>{@code true}：当前资源按 EXIF 方向校正语义处理；若 EXIF 方向不是正常值，缓存的图像和尺寸为校正后的结果</li>
	 *   <li>{@code false}：当前资源不按 EXIF 方向进行校正，后续读取到的是原始方向的数据</li>
	 * </ul>
	 *
	 * @since 2.1.0
	 */
	protected final boolean orientationCorrected;
	/**
	 * 图像尺寸
	 * <p>包含宽度和高度信息，当启用方向校正时为校正后的尺寸</p>
	 *
	 * @since 2.1.0
	 */
	protected volatile ImageSize imageSize;
	/**
	 * 图像元数据
	 * <p>基于Metadata Extractor解析的图像元数据</p>
	 *
	 * @since 2.1.0
	 */
	protected volatile Metadata metadata;
	/**
	 * 缓存的BufferedImage
	 * <p>用于避免重复解码图像，当启用方向校正时为校正后的图像</p>
	 *
	 * @since 2.1.0
	 */
	protected volatile BufferedImage image;

	/**
	 * 基于IOResource构造ImageIOResource（自动校正EXIF方向）
	 * <p>从现有 IOResource 创建 ImageIOResource，并在需要时根据 EXIF 方向信息校正图像方向。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>源资源必须未关闭</li>
	 *     <li>自动验证资源是否为图像类型</li>
	 *     <li>当 EXIF 方向不为正常值时，自动解码图像并缓存校正后的结果</li>
	 *     <li>orientationCorrected 字段将被设置为 true</li>
	 * </ul>
	 *
	 * @param resource 源资源（必须非null且未关闭）
	 * @throws IOException              当资源读取失败时抛出
	 * @throws UnsupportedResourceException 当 resource 不是受支持的图像资源时抛出
	 * @since 2.1.0
	 */
	public ImageIOResource(IOResource resource) throws IOException {
		this(resource, true);
	}

	/**
	 * 基于IOResource构造ImageIOResource（可选校正EXIF方向）
	 * <p>从现有 IOResource 创建 ImageIOResource，可选择是否根据 EXIF 方向信息校正图像方向。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>源资源必须未关闭</li>
	 *     <li>自动验证资源是否为图像类型</li>
	 *     <li>当启用校正且 EXIF 方向不为正常值时，自动解码图像并缓存校正后的结果</li>
	 *     <li>orientationCorrected 字段将与 {@code correctOrientation} 参数保持一致</li>
	 * </ul>
	 *
	 * @param resource           源资源（必须非null且未关闭）
	 * @param correctOrientation 是否根据 EXIF 方向信息校正图像方向
	 * @throws IOException              当资源读取失败时抛出
	 * @throws UnsupportedResourceException 当 resource 不是受支持的图像资源时抛出
	 * @since 2.1.0
	 */
	public ImageIOResource(IOResource resource, boolean correctOrientation) throws IOException {
		super(resource);

		this.orientationCorrected = correctOrientation;

		if (resource instanceof ImageIOResource imageIOResource) {
			this.imageFormat = imageIOResource.imageFormat;
		} else {
			validateImageType("resource 不是图像资源");

			if (StringUtils.isNotBlank(this.format)) {
				String imageFormat = this.format.toUpperCase();

				if (ImageConstants.getSupportedReadImageFormats().contains(imageFormat)) {
					this.imageFormat = imageFormat;
				} else {
					this.imageFormat = null;
				}
			} else {
				this.imageFormat = null;
			}
		}

		if (Objects.nonNull(file)) {
			int exifOrientation = ImageConstants.NORMAL_EXIF_ORIENTATION;
			if (correctOrientation) {
				try {
					this.metadata = ImageMetadataReader.readMetadata(file);
					exifOrientation = ImageUtils.getExifOrientation(this.metadata);
				} catch (ImageProcessingException ignored) {
					this.metadata = new Metadata();
				}
			}

			if (exifOrientation != ImageConstants.NORMAL_EXIF_ORIENTATION) {
				BufferedImage image = ImageIO.read(this.file);
				if (Objects.isNull(image)) {
					throw new IOException("图片读取失败，文件路径：" + this.file.getAbsolutePath());
				}

				this.imageSize = new ImageSize(image.getWidth(), image.getHeight(), exifOrientation).getVisualSize();
				this.image = ImageUtils.correctOrientation(image, exifOrientation);
			}
		} else {
			if (correctOrientation) {
				int exifOrientation = ImageConstants.NORMAL_EXIF_ORIENTATION;
				InputStream byteArrayInputStream = IOUtils.toUnsynchronizedByteArrayInputStream(getBytes());

				try {
					this.metadata = ImageMetadataReader.readMetadata(byteArrayInputStream, size.toBytes());
					exifOrientation = ImageUtils.getExifOrientation(this.metadata);
				} catch (ImageProcessingException ignored) {
					this.metadata = new Metadata();
				}

				if (exifOrientation != ImageConstants.NORMAL_EXIF_ORIENTATION) {
					BufferedImage image = ImageIO.read(byteArrayInputStream);
					if (Objects.isNull(image)) {
						throw new IOException("图片读取失败");
					}

					this.imageSize = new ImageSize(image.getWidth(), image.getHeight(), exifOrientation).getVisualSize();
					this.image = ImageUtils.correctOrientation(image, exifOrientation);
				}
			}
		}
	}

	/**
	 * 基于IOResource构造ImageIOResource（指定EXIF方向）
	 * <p>从现有IOResource创建ImageIOResource，使用指定的EXIF方向值进行校正。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>源资源必须未关闭</li>
	 *     <li>自动验证资源是否为图像类型</li>
	 *     <li>当 EXIF 方向不为正常值时，自动解码图像并缓存校正后的结果</li>
	 *     <li>orientationCorrected 字段将被设置为 true</li>
	 * </ul>
	 *
	 * @param resource        源资源（必须非null且未关闭）
	 * @param exifOrientation EXIF方向值（必须介于1-8之间）
	 * @throws IOException              当资源读取失败时抛出
	 * @throws IllegalArgumentException     当 exifOrientation 不在 1-8 范围内时抛出
	 * @throws UnsupportedResourceException 当 resource 不是受支持的图像资源时抛出
	 * @since 2.1.0
	 */
	public ImageIOResource(IOResource resource, int exifOrientation) throws IOException {
		super(resource);

		this.orientationCorrected = true;

		if (resource instanceof ImageIOResource imageIOResource) {
			this.imageFormat = imageIOResource.imageFormat;
		} else {
			validateImageType("resource 不是图像资源");

			if (StringUtils.isNotBlank(this.format)) {
				String imageFormat = this.format.toUpperCase();

				if (ImageConstants.getSupportedReadImageFormats().contains(imageFormat)) {
					this.imageFormat = imageFormat;
				} else {
					this.imageFormat = null;
				}
			} else {
				this.imageFormat = null;
			}
		}

		if (exifOrientation != ImageConstants.NORMAL_EXIF_ORIENTATION) {
			BufferedImage image;
			if (Objects.nonNull(this.file)) {
				image = ImageIO.read(this.file);
				if (Objects.isNull(image)) {
					throw new IOException("图片读取失败，文件路径：" + this.file.getAbsolutePath());
				}
			} else {
				try (InputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(getBytes())) {
					image = ImageIO.read(inputStream);
				}
				if (Objects.isNull(image)) {
					throw new IOException("图片读取失败");
				}
			}

			this.imageSize = new ImageSize(image.getWidth(), image.getHeight(), exifOrientation).getVisualSize();
			this.image = ImageUtils.correctOrientation(image, exifOrientation);
		}
	}

	/**
	 * 基于文件路径构造ImageIOResource（自动校正EXIF方向）
	 * <p>从文件路径创建 ImageIOResource，并在需要时根据 EXIF 方向信息校正图像方向。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>自动验证文件是否为图像类型</li>
	 *     <li>自动识别图像格式</li>
	 *     <li>当 EXIF 方向不为正常值时，自动解码图像并缓存校正后的结果</li>
	 * </ul>
	 *
	 * @param filePath 文件路径（必须非空）
	 * @throws IOException              当文件读取失败时抛出
	 * @throws UnsupportedResourceException 当 filePath 对应资源不是受支持的图像文件时抛出
	 * @since 2.1.0
	 */
	public ImageIOResource(String filePath) throws IOException {
		this(filePath, true);
	}

	/**
	 * 基于文件路径构造ImageIOResource（可选校正EXIF方向）
	 * <p>从文件路径创建 ImageIOResource，可选择是否根据 EXIF 方向信息校正图像方向。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>自动验证文件是否为图像类型</li>
	 *     <li>自动识别图像格式</li>
	 *     <li>当启用校正且EXIF方向不为正常值时，自动解码图像并缓存校正后的结果</li>
	 * </ul>
	 *
	 * @param filePath           文件路径（必须非空）
	 * @param correctOrientation 是否根据 EXIF 方向信息校正图像方向
	 * @throws IOException              当文件读取失败时抛出
	 * @throws UnsupportedResourceException 当 filePath 对应资源不是受支持的图像文件时抛出
	 * @since 2.1.0
	 */
	public ImageIOResource(String filePath, boolean correctOrientation) throws IOException {
		super(filePath, false);

		validateImageType("filePath 不是图像文件路径");

		if (StringUtils.isNotBlank(this.format)) {
			String imageFormat = this.format.toUpperCase();

			if (ImageConstants.getSupportedReadImageFormats().contains(imageFormat)) {
				this.imageFormat = imageFormat;
			} else {
				this.imageFormat = null;
			}
		} else {
			this.imageFormat = null;
		}

		this.orientationCorrected = correctOrientation;

		if (correctOrientation) {
			int exifOrientation = ImageConstants.NORMAL_EXIF_ORIENTATION;
			try {
				this.metadata = ImageMetadataReader.readMetadata(this.file);
				exifOrientation = ImageUtils.getExifOrientation(this.metadata);
			} catch (ImageProcessingException ignored) {
				this.metadata = new Metadata();
			}

			if (exifOrientation != ImageConstants.NORMAL_EXIF_ORIENTATION) {
				BufferedImage image = ImageIO.read(this.file);
				if (Objects.isNull(image)) {
					throw new IOException("图片读取失败，文件路径：" + this.file.getAbsolutePath());
				}

				this.imageSize = new ImageSize(image.getWidth(), image.getHeight(), exifOrientation).getVisualSize();
				this.image = ImageUtils.correctOrientation(image, exifOrientation);
			}
		}
	}

	/**
	 * 基于文件路径构造ImageIOResource（指定EXIF方向）
	 * <p>从文件路径创建ImageIOResource，使用指定的EXIF方向值进行校正。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>自动验证文件是否为图像类型</li>
	 *     <li>自动识别图像格式</li>
	 *     <li>当EXIF方向不为正常值时，自动解码图像并缓存校正后的结果</li>
	 * </ul>
	 *
	 * @param filePath        文件路径（必须非空）
	 * @param exifOrientation EXIF方向值（必须介于1-8之间）
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException     当 exifOrientation 不在 1-8 范围内时抛出
	 * @throws UnsupportedResourceException 当 filePath 对应资源不是受支持的图像文件时抛出
	 * @since 2.1.0
	 */
	public ImageIOResource(String filePath, int exifOrientation) throws IOException {
		super(filePath, false);

		Validate.inclusiveBetween(1, 8, exifOrientation, "exifOrientation 必须介于1-8之间");
		validateImageType("filePath 不是图像文件路径");

		if (StringUtils.isNotBlank(this.format)) {
			String imageFormat = this.format.toUpperCase();

			if (ImageConstants.getSupportedReadImageFormats().contains(imageFormat)) {
				this.imageFormat = imageFormat;
			} else {
				this.imageFormat = null;
			}
		} else {
			this.imageFormat = null;
		}

		this.orientationCorrected = true;

		if (exifOrientation != ImageConstants.NORMAL_EXIF_ORIENTATION) {
			BufferedImage image = ImageIO.read(this.file);
			if (Objects.isNull(image)) {
				throw new IOException("图片读取失败，文件路径：" + this.file.getAbsolutePath());
			}

			this.imageSize = new ImageSize(image.getWidth(), image.getHeight(), exifOrientation).getVisualSize();
			this.image = ImageUtils.correctOrientation(image, exifOrientation);
		}
	}


	/**
	 * 基于File对象构造ImageIOResource（自动校正EXIF方向）
	 * <p>从 File 对象创建 ImageIOResource，并在需要时根据 EXIF 方向信息校正图像方向。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>自动验证文件是否为图像类型</li>
	 *     <li>自动识别图像格式</li>
	 *     <li>当EXIF方向不为正常值时，自动解码图像并缓存校正后的结果</li>
	 * </ul>
	 *
	 * @param file 文件对象（必须非null）
	 * @throws IOException              当文件读取失败时抛出
	 * @throws UnsupportedResourceException 当 file 对应资源不是受支持的图像文件时抛出
	 * @since 2.1.0
	 */
	public ImageIOResource(File file) throws IOException {
		this(file, true);
	}

	/**
	 * 基于File对象构造ImageIOResource（可选校正EXIF方向）
	 * <p>从 File 对象创建 ImageIOResource，可选择是否根据 EXIF 方向信息校正图像方向。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>自动验证文件是否为图像类型</li>
	 *     <li>自动识别图像格式</li>
	 *     <li>当启用校正且 EXIF 方向不为正常值时，自动解码图像并缓存校正后的结果</li>
	 * </ul>
	 *
	 * @param file               文件对象（必须非null）
	 * @param correctOrientation 是否根据 EXIF 方向信息校正图像方向
	 * @throws IOException              当文件读取失败时抛出
	 * @throws UnsupportedResourceException 当 file 对应资源不是受支持的图像文件时抛出
	 * @since 2.1.0
	 */
	public ImageIOResource(File file, boolean correctOrientation) throws IOException {
		super(file, false);

		validateImageType("file 不是图像文件");

		if (StringUtils.isNotBlank(this.format)) {
			String imageFormat = this.format.toUpperCase();

			if (ImageConstants.getSupportedReadImageFormats().contains(imageFormat)) {
				this.imageFormat = imageFormat;
			} else {
				this.imageFormat = null;
			}
		} else {
			this.imageFormat = null;
		}

		this.orientationCorrected = correctOrientation;

		if (correctOrientation) {
			int exifOrientation = ImageConstants.NORMAL_EXIF_ORIENTATION;
			try {
				this.metadata = ImageMetadataReader.readMetadata(this.file);
				exifOrientation = ImageUtils.getExifOrientation(this.metadata);
			} catch (ImageProcessingException ignored) {
				this.metadata = new Metadata();
			}

			if (exifOrientation != ImageConstants.NORMAL_EXIF_ORIENTATION) {
				BufferedImage image = ImageIO.read(this.file);
				if (Objects.isNull(image)) {
					throw new IOException("图片读取失败，文件路径：" + this.file.getAbsolutePath());
				}

				this.imageSize = new ImageSize(image.getWidth(), image.getHeight(), exifOrientation).getVisualSize();
				this.image = ImageUtils.correctOrientation(image, exifOrientation);
			}
		}
	}

	/**
	 * 基于File对象构造ImageIOResource（指定EXIF方向）
	 * <p>从File对象创建ImageIOResource，使用指定的EXIF方向值进行校正。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>自动验证文件是否为图像类型</li>
	 *     <li>自动识别图像格式</li>
	 *     <li>当 EXIF 方向不为正常值时，自动解码图像并缓存校正后的结果</li>
	 * </ul>
	 *
	 * @param file            文件对象（必须非null）
	 * @param exifOrientation EXIF方向值（必须介于1-8之间）
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException     当 exifOrientation 不在 1-8 范围内时抛出
	 * @throws UnsupportedResourceException 当 file 对应资源不是受支持的图像文件时抛出
	 * @since 2.1.0
	 */
	public ImageIOResource(File file, int exifOrientation) throws IOException {
		super(file, false);

		Validate.inclusiveBetween(1, 8, exifOrientation, "exifOrientation 必须介于1-8之间");
		validateImageType("file 不是图像文件");

		if (StringUtils.isNotBlank(this.format)) {
			String imageFormat = this.format.toUpperCase();

			if (ImageConstants.getSupportedReadImageFormats().contains(imageFormat)) {
				this.imageFormat = imageFormat;
			} else {
				this.imageFormat = null;
			}
		} else {
			this.imageFormat = null;
		}

		this.orientationCorrected = true;

		if (exifOrientation != ImageConstants.NORMAL_EXIF_ORIENTATION) {
			BufferedImage image = ImageIO.read(this.file);
			if (Objects.isNull(image)) {
				throw new IOException("图片读取失败，文件路径：" + this.file.getAbsolutePath());
			}

			this.imageSize = new ImageSize(image.getWidth(), image.getHeight(), exifOrientation).getVisualSize();
			this.image = ImageUtils.correctOrientation(image, exifOrientation);
		}
	}

	/**
	 * 基于字节数组构造ImageIOResource（自动校正EXIF方向）
	 * <p>从字节数组创建 ImageIOResource，并在需要时根据 EXIF 方向信息校正图像方向。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>自动验证数据是否为图像类型</li>
	 *     <li>当EXIF方向不为正常值时，自动解码图像并缓存校正后的结果</li>
	 * </ul>
	 *
	 * @param bytes 字节数组（必须非空）
	 * @throws IOException              当数据读取失败时抛出
	 * @throws UnsupportedResourceException 当 bytes 不是受支持的图像数据时抛出
	 * @since 2.1.0
	 */
	public ImageIOResource(byte[] bytes) throws IOException {
		this(bytes, true);
	}

	/**
	 * 基于字节数组构造ImageIOResource（可选校正EXIF方向）
	 * <p>从字节数组创建 ImageIOResource，可选择是否根据 EXIF 方向信息校正图像方向。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>自动验证数据是否为图像类型</li>
	 *     <li>当启用校正且EXIF方向不为正常值时，自动解码图像并缓存校正后的结果</li>
	 * </ul>
	 *
	 * @param bytes              字节数组（必须非空）
	 * @param correctOrientation 是否根据 EXIF 方向信息校正图像方向
	 * @throws IOException              当数据读取失败时抛出
	 * @throws UnsupportedResourceException 当 bytes 不是受支持的图像数据时抛出
	 * @since 2.1.0
	 */
	public ImageIOResource(byte[] bytes, boolean correctOrientation) throws IOException {
		super(bytes);

		validateImageType("bytes 不是图像数据");

		if (StringUtils.isNotBlank(this.format)) {
			String imageFormat = this.format.toUpperCase();

			if (ImageConstants.getSupportedReadImageFormats().contains(imageFormat)) {
				this.imageFormat = imageFormat;
			} else {
				this.imageFormat = null;
			}
		} else {
			this.imageFormat = null;
		}

		this.orientationCorrected = correctOrientation;

		if (correctOrientation) {
			int exifOrientation = ImageConstants.NORMAL_EXIF_ORIENTATION;
			InputStream byteArrayInputStream = IOUtils.toUnsynchronizedByteArrayInputStream(getBytes());

			try {
				this.metadata = ImageMetadataReader.readMetadata(byteArrayInputStream, size.toBytes());
				exifOrientation = ImageUtils.getExifOrientation(this.metadata);
			} catch (ImageProcessingException ignored) {
				this.metadata = new Metadata();
			}

			if (exifOrientation != ImageConstants.NORMAL_EXIF_ORIENTATION) {
				BufferedImage image = ImageIO.read(byteArrayInputStream);
				if (Objects.isNull(image)) {
					throw new IOException("图片读取失败");
				}

				this.imageSize = new ImageSize(image.getWidth(), image.getHeight(), exifOrientation).getVisualSize();
				this.image = ImageUtils.correctOrientation(image, exifOrientation);
			}
		}
	}

	/**
	 * 基于字节数组构造ImageIOResource（指定EXIF方向）
	 * <p>从字节数组创建ImageIOResource，使用指定的EXIF方向值进行校正。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>自动验证数据是否为图像类型</li>
	 *     <li>当EXIF方向不为正常值时，自动解码图像并缓存校正后的结果</li>
	 * </ul>
	 *
	 * @param bytes           字节数组（必须非空）
	 * @param exifOrientation EXIF方向值（必须介于1-8之间）
	 * @throws IOException              当数据读取失败时抛出
	 * @throws IllegalArgumentException     当 exifOrientation 不在 1-8 范围内时抛出
	 * @throws UnsupportedResourceException 当 bytes 不是受支持的图像数据时抛出
	 * @since 2.1.0
	 */
	public ImageIOResource(byte[] bytes, int exifOrientation) throws IOException {
		super(bytes);

		Validate.inclusiveBetween(1, 8, exifOrientation, "exifOrientation 必须介于1-8之间");
		validateImageType("bytes 不是图像数据");

		if (StringUtils.isNotBlank(this.format)) {
			String imageFormat = this.format.toUpperCase();

			if (ImageConstants.getSupportedReadImageFormats().contains(imageFormat)) {
				this.imageFormat = imageFormat;
			} else {
				this.imageFormat = null;
			}
		} else {
			this.imageFormat = null;
		}

		this.orientationCorrected = true;

		if (exifOrientation != ImageConstants.NORMAL_EXIF_ORIENTATION) {
			try (InputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(getBytes())) {
				BufferedImage image = ImageIO.read(inputStream);
				if (Objects.isNull(image)) {
					throw new IOException("图片读取失败");
				}

				this.imageSize = new ImageSize(image.getWidth(), image.getHeight(), exifOrientation).getVisualSize();
				this.image = ImageUtils.correctOrientation(image, exifOrientation);
			}
		}
	}

	/**
	 * 基于输入流构造ImageIOResource（自动校正EXIF方向）
	 * <p>从输入流创建 ImageIOResource，并在需要时根据 EXIF 方向信息校正图像方向。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>自动验证数据是否为图像类型</li>
	 *     <li>当EXIF方向不为正常值时，自动解码图像并缓存校正后的结果</li>
	 * </ul>
	 *
	 * @param inputStream 输入流（必须非null）
	 * @throws IOException              当流读取失败时抛出
	 * @throws UnsupportedResourceException 当 inputStream 不是受支持的图像数据输入流时抛出
	 * @since 2.1.0
	 */
	public ImageIOResource(InputStream inputStream) throws IOException {
		this(inputStream, true);
	}

	/**
	 * 基于输入流构造ImageIOResource（可选校正EXIF方向）
	 * <p>从输入流创建 ImageIOResource，可选择是否根据 EXIF 方向信息校正图像方向。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>自动验证数据是否为图像类型</li>
	 *     <li>当启用校正且 EXIF 方向不为正常值时，自动解码图像并缓存校正后的结果</li>
	 * </ul>
	 *
	 * @param inputStream        输入流（必须非null）
	 * @param correctOrientation 是否根据 EXIF 方向信息校正图像方向
	 * @throws IOException              当流读取失败时抛出
	 * @throws UnsupportedResourceException 当 inputStream 不是受支持的图像数据输入流时抛出
	 * @since 2.1.0
	 */
	public ImageIOResource(InputStream inputStream, boolean correctOrientation) throws IOException {
		super(inputStream);

		validateImageType("inputStream 不是图像数据输入流");

		if (StringUtils.isNotBlank(this.format)) {
			String imageFormat = this.format.toUpperCase();

			if (ImageConstants.getSupportedReadImageFormats().contains(imageFormat)) {
				this.imageFormat = imageFormat;
			} else {
				this.imageFormat = null;
			}
		} else {
			this.imageFormat = null;
		}

		this.orientationCorrected = correctOrientation;

		if (correctOrientation) {
			int exifOrientation = ImageConstants.NORMAL_EXIF_ORIENTATION;
			InputStream byteArrayInputStream = IOUtils.toUnsynchronizedByteArrayInputStream(getBytes());

			try {
				this.metadata = ImageMetadataReader.readMetadata(byteArrayInputStream, size.toBytes());
				exifOrientation = ImageUtils.getExifOrientation(this.metadata);
			} catch (ImageProcessingException ignored) {
				this.metadata = new Metadata();
			}
			byteArrayInputStream.reset();

			if (exifOrientation != ImageConstants.NORMAL_EXIF_ORIENTATION) {
				BufferedImage image = ImageIO.read(byteArrayInputStream);
				if (Objects.isNull(image)) {
					throw new IOException("图片读取失败");
				}

				this.imageSize = new ImageSize(image.getWidth(), image.getHeight(), exifOrientation).getVisualSize();
				this.image = ImageUtils.correctOrientation(image, exifOrientation);
			}
		}
	}

	/**
	 * 基于输入流构造ImageIOResource（指定EXIF方向）
	 * <p>从输入流创建ImageIOResource，使用指定的EXIF方向值进行校正。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>自动验证数据是否为图像类型</li>
	 *     <li>当 EXIF 方向不为正常值时，自动解码图像并缓存校正后的结果</li>
	 * </ul>
	 *
	 * @param inputStream     输入流（必须非null）
	 * @param exifOrientation EXIF方向值（必须介于1-8之间）
	 * @throws IOException              当流读取失败时抛出
	 * @throws IllegalArgumentException     当 exifOrientation 不在 1-8 范围内时抛出
	 * @throws UnsupportedResourceException 当 inputStream 不是受支持的图像数据输入流时抛出
	 * @since 2.1.0
	 */
	public ImageIOResource(InputStream inputStream, int exifOrientation) throws IOException {
		super(inputStream);

		Validate.inclusiveBetween(1, 8, exifOrientation, "exifOrientation 必须介于1-8之间");
		validateImageType("inputStream 不是图像数据输入流");

		if (StringUtils.isNotBlank(this.format)) {
			String imageFormat = this.format.toUpperCase();

			if (ImageConstants.getSupportedReadImageFormats().contains(imageFormat)) {
				this.imageFormat = imageFormat;
			} else {
				this.imageFormat = null;
			}
		} else {
			this.imageFormat = null;
		}

		this.orientationCorrected = true;

		if (exifOrientation != ImageConstants.NORMAL_EXIF_ORIENTATION) {
			try (InputStream byteArrayInputStream = IOUtils.toUnsynchronizedByteArrayInputStream(getBytes())) {
				BufferedImage image = ImageIO.read(byteArrayInputStream);
				if (Objects.isNull(image)) {
					throw new IOException("图片读取失败");
				}

				this.imageSize = new ImageSize(image.getWidth(), image.getHeight(), exifOrientation).getVisualSize();
				this.image = ImageUtils.correctOrientation(image, exifOrientation);
			}
		}
	}

	/**
	 * 基于ImageInputStream构造ImageIOResource（自动校正EXIF方向）
	 * <p>从 ImageInputStream 创建 ImageIOResource，并在需要时根据 EXIF 方向信息校正图像方向。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>自动验证数据是否为图像类型</li>
	 *     <li>当EXIF方向不为正常值时，自动解码图像并缓存校正后的结果</li>
	 *     <li>输入流位置会被重置到原始位置</li>
	 * </ul>
	 *
	 * @param inputStream ImageInputStream（必须非null）
	 * @throws IOException              当流读取失败时抛出
	 * @throws UnsupportedResourceException 当 imageInputStream 不是受支持的图像数据流时抛出
	 * @since 2.1.0
	 */
	public ImageIOResource(ImageInputStream inputStream) throws IOException {
		this(inputStream, true);
	}

	/**
	 * 基于ImageInputStream构造ImageIOResource（可选校正EXIF方向）
	 * <p>从 ImageInputStream 创建 ImageIOResource，可选择是否根据 EXIF 方向信息校正图像方向。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>自动验证数据是否为图像类型</li>
	 *     <li>当启用校正且EXIF方向不为正常值时，自动解码图像并缓存校正后的结果</li>
	 *     <li>输入流位置会被重置到原始位置</li>
	 * </ul>
	 *
	 * @param imageInputStream   ImageInputStream（必须非null）
	 * @param correctOrientation 是否根据 EXIF 方向信息校正图像方向
	 * @throws IOException              当流读取失败时抛出
	 * @throws UnsupportedResourceException 当 imageInputStream 不是受支持的图像数据流时抛出
	 * @since 2.1.0
	 */
	public ImageIOResource(ImageInputStream imageInputStream, boolean correctOrientation) throws IOException {
		super(parse(imageInputStream), null);

		validateImageType("imageInputStream 不是图像输入流");

		if (StringUtils.isNotBlank(this.format)) {
			String imageFormat = this.format.toUpperCase();

			if (ImageConstants.getSupportedReadImageFormats().contains(imageFormat)) {
				this.imageFormat = imageFormat;
			} else {
				this.imageFormat = null;
			}
		} else {
			this.imageFormat = null;
		}

		this.orientationCorrected = correctOrientation;

		if (correctOrientation) {
			int exifOrientation = ImageConstants.NORMAL_EXIF_ORIENTATION;
			InputStream byteArrayInputStream = IOUtils.toUnsynchronizedByteArrayInputStream(getBytes());

			try {
				this.metadata = ImageMetadataReader.readMetadata(byteArrayInputStream, size.toBytes());
				exifOrientation = ImageUtils.getExifOrientation(this.metadata);
			} catch (ImageProcessingException ignored) {
				this.metadata = new Metadata();
			}
			byteArrayInputStream.reset();

			if (exifOrientation != ImageConstants.NORMAL_EXIF_ORIENTATION) {
				BufferedImage image = ImageIO.read(byteArrayInputStream);
				if (Objects.isNull(image)) {
					throw new IOException("图片读取失败");
				}

				this.imageSize = new ImageSize(image.getWidth(), image.getHeight(), exifOrientation).getVisualSize();
				this.image = ImageUtils.correctOrientation(image, exifOrientation);
			}
		}
	}

	/**
	 * 基于ImageInputStream构造ImageIOResource（指定EXIF方向）
	 * <p>从ImageInputStream创建ImageIOResource，使用指定的EXIF方向值进行校正。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>自动验证数据是否为图像类型</li>
	 *     <li>当EXIF方向不为正常值时，自动解码图像并缓存校正后的结果</li>
	 *     <li>输入流位置会被重置到原始位置</li>
	 * </ul>
	 *
	 * @param imageInputStream ImageInputStream（必须非null）
	 * @param exifOrientation  EXIF方向值（必须介于1-8之间）
	 * @throws IOException              当流读取失败时抛出
	 * @throws IllegalArgumentException     当 exifOrientation 不在 1-8 范围内时抛出
	 * @throws UnsupportedResourceException 当 imageInputStream 不是受支持的图像数据流时抛出
	 * @since 2.1.0
	 */
	public ImageIOResource(ImageInputStream imageInputStream, int exifOrientation) throws IOException {
		super(parse(imageInputStream), null);

		Validate.inclusiveBetween(1, 8, exifOrientation, "exifOrientation 必须介于1-8之间");
		validateImageType("imageInputStream 不是图像输入流");

		if (StringUtils.isNotBlank(this.format)) {
			String imageFormat = this.format.toUpperCase();

			if (ImageConstants.getSupportedReadImageFormats().contains(imageFormat)) {
				this.imageFormat = imageFormat;
			} else {
				this.imageFormat = null;
			}
		} else {
			this.imageFormat = null;
		}

		this.orientationCorrected = true;

		if (exifOrientation != ImageConstants.NORMAL_EXIF_ORIENTATION) {
			try (InputStream byteArrayInputStream = IOUtils.toUnsynchronizedByteArrayInputStream(getBytes())) {
				BufferedImage image = ImageIO.read(byteArrayInputStream);
				if (Objects.isNull(image)) {
					throw new IOException("图片读取失败");
				}

				this.imageSize = new ImageSize(image.getWidth(), image.getHeight(), exifOrientation).getVisualSize();
				this.image = ImageUtils.correctOrientation(image, exifOrientation);
			}
		}
	}

	/**
	 * 解析ImageInputStream为ByteArrayOutputStream
	 * <p>从ImageInputStream读取全部数据并转换为ByteArrayOutputStream。</p>
	 *
	 * <p>实现特性：</p>
	 * <ul>
	 *     <li>自动根据流大小计算合适的缓冲区大小</li>
	 *     <li>读取完成后会重置流位置到原始位置</li>
	 *     <li>使用try-finally确保流位置被正确恢复</li>
	 * </ul>
	 *
	 * @param imageInputStream ImageInputStream（必须非null）
	 * @return 包含流数据的ByteArrayOutputStream
	 * @throws IOException              当流读取失败时抛出
	 * @throws IllegalArgumentException 当imageInputStream为null时抛出
	 * @since 2.1.0
	 */
	protected static ByteArrayOutputStream parse(final ImageInputStream imageInputStream) throws IOException {
		Validate.notNull(imageInputStream, "imageInputStream 不可为 null");

		long oldPos = imageInputStream.getStreamPosition();
		imageInputStream.seek(0);

		try {
			int bufferSize = IOUtils.DEFAULT_BUFFER_SIZE;
			long totalSize = imageInputStream.length();
			if (totalSize != -1) {
				bufferSize = IOUtils.getBufferSize(totalSize);
			}
			ByteArrayOutputStream bos = new ByteArrayOutputStream(bufferSize);

			byte[] buffer = new byte[bufferSize];
			int length;
			while ((length = imageInputStream.read(buffer)) != -1) {
				bos.write(buffer, 0, length);
			}

			return bos;
		} finally {
			imageInputStream.seek(oldPos);
		}
	}

	/**
	 * 获取图像尺寸
	 * <p>优先从缓存获取尺寸，若未缓存则从元数据获取，若元数据中不存在则从BufferedImage获取。</p>
	 *
	 * <p>实现特性：</p>
	 * <ul>
	 *     <li>线程安全，使用synchronized保护</li>
	 *     <li>结果会被缓存，避免重复计算</li>
	 *     <li>当启用方向校正时，返回校正后的尺寸</li>
	 * </ul>
	 *
	 * @return 图像尺寸对象（包含宽度、高度和EXIF方向）
	 * @throws IOException 当资源已关闭或图像读取失败时抛出
	 * @since 2.1.0
	 */
	public ImageSize getImageSize() throws IOException {
		checkClosed();

		synchronized (this) {
			if (Objects.nonNull(imageSize)) {
				return imageSize;
			}

			Metadata metadata = getMetadata();
			imageSize = ImageUtils.getSize(metadata);

			if (Objects.isNull(imageSize)) {
				BufferedImage image = getBufferedImage();
				imageSize = new ImageSize(image.getWidth(), image.getHeight(), ImageUtils.getExifOrientation(metadata));
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
	 * @since 2.1.0
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
	 * @since 2.1.0
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
				try (InputStream byteArrayInputStream = IOUtils.toUnsynchronizedByteArrayInputStream(getBytes())) {
					metadata = ImageMetadataReader.readMetadata(byteArrayInputStream, size.toBytes());
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
	 * @since 2.1.0
	 */
	public void setMetadata(Metadata metadata) throws IOException {
		checkClosed();

		this.metadata = metadata;
	}

	/**
	 * 获取BufferedImage
	 * <p>使用ImageIO读取图像并返回BufferedImage对象。</p>
	 *
	 * <p>实现特性：</p>
	 * <ul>
	 *     <li>线程安全，使用synchronized保护</li>
	 *     <li>结果会被缓存，避免重复解码</li>
	 *     <li>当启用方向校正时，返回校正后的图像</li>
	 *     <li>读取失败时抛出IOException</li>
	 * </ul>
	 *
	 * @return BufferedImage对象（当启用方向校正时为校正后的图像）
	 * @throws IOException 当资源已关闭或图像读取失败时抛出
	 * @since 2.1.0
	 */
	public BufferedImage getBufferedImage() throws IOException {
		checkClosed();

		synchronized (this) {
			if (Objects.nonNull(image)) {
				return image;
			}

			if (Objects.nonNull(file)) {
				image = ImageIO.read(file);
				if (Objects.isNull(image)) {
					throw new IOException("图像读取失败，文件路径：" + file.getAbsolutePath());
				}
			} else {
				try (InputStream byteArrayInputStream = IOUtils.toUnsynchronizedByteArrayInputStream(getBytes())) {
					image = ImageIO.read(byteArrayInputStream);
					if (Objects.isNull(image)) {
						throw new IOException("图像读取失败");
					}
				}
			}
			return image;
		}
	}

	/**
	 * 获取图像格式
	 * <p>从父类format推断的、ImageIO支持的图像格式（如JPEG、PNG），
	 * 仅当该格式在{@link ImageConstants#getSupportedReadImageFormats}中时才有值。
	 *
	 * @return 图像格式字符串（大写），不支持该格式时返回null
	 * @since 2.1.0
	 */
	public String getImageFormat() {
		return imageFormat;
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
	 *   <li>当启用方向校正时，返回校正后图像的深拷贝</li>
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
	 * @return BufferedImage的深拷贝副本（当启用方向校正时为校正后图像的深拷贝）
	 * @throws IOException 当资源已关闭或图像读取失败时抛出
	 * @see #getBufferedImage()
	 * @see ImageUtil#createCopy(BufferedImage)
	 * @since 2.1.0
	 */
	public BufferedImage getBufferedImageCopy() throws IOException {
		checkClosed();

		BufferedImage src = this.image;
		if (Objects.isNull(src)) {
			synchronized (this) {
				src = this.image;
				if (Objects.isNull(src)) {
					src = getBufferedImage();
				}
			}
		}
		return ImageUtil.createCopy(src);
	}

	/**
	 * 判断 EXIF 方向是否已校正
	 * <p>返回当前资源是否按 EXIF 方向校正语义处理。</p>
	 *
	 * <p>返回值说明：</p>
	 * <ul>
	 *   <li>{@code true}：当前资源按 EXIF 方向校正语义处理；若 EXIF 方向不是正常值，缓存的图像和尺寸为校正后的结果</li>
	 *   <li>{@code false}：当前资源不按 EXIF 方向进行校正，后续读取到的是原始方向的数据</li>
	 * </ul>
	 *
	 * @return 如果 EXIF 方向已校正返回 true，否则返回 false
	 * @since 2.1.0
	 */
	public boolean isOrientationCorrected() {
		return orientationCorrected;
	}

	/**
	 * 创建ImageInputStream
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
	 * @since 2.1.0
	 */
	public ImageInputStream newImageInputStream() throws IOException {
		checkClosed();

		if (Objects.nonNull(file)) {
			return ImageIO.createImageInputStream(file);
		} else {
			return ImageIO.createImageInputStream(newBufferedInputStream());
		}
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
	 * @throws UnsupportedResourceException 当资源不是图像类型，或当前 MIME 类型不支持 ImageIO 读取时抛出
	 * @since 2.1.0
	 */
	protected void validateImageType(String message) {
		if (!isImage()) {
			throw new UnsupportedResourceException(message);
		}
		if (!ImageUtils.isSupportReadType(mimeType)) {
			throw new UnsupportedResourceException("不支持读取 " + mimeType + " 类型图像");
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
	 *     <li>清空image缓存</li>
	 *     <li>调用父类close方法</li>
	 * </ul>
	 *
	 * @throws IOException 当资源关闭失败时抛出
	 * @since 2.1.0
	 */
	@Override
	public synchronized void close() throws IOException {
		if (Objects.nonNull(this.image)) {
			this.image.flush();
		}

		this.metadata = null;
		this.image = null;
		this.imageSize = null;

		super.close();
	}
}
