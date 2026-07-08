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

package io.github.pangju666.commons.opencv.model;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.bmp.BmpHeaderDirectory;
import com.drew.metadata.eps.EpsDirectory;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.gif.GifImageDirectory;
import com.drew.metadata.heif.HeifDirectory;
import com.drew.metadata.ico.IcoDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.photoshop.PsdHeaderDirectory;
import com.drew.metadata.png.PngDirectory;
import com.drew.metadata.webp.WebpDirectory;
import io.github.pangju666.commons.io.model.IOResource;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.IOUtils;
import io.github.pangju666.commons.opencv.lang.OpencvConstants;
import io.github.pangju666.commons.opencv.utils.OpencvUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * OpenCV 图像资源类
 * <p>
 * 扩展 {@link IOResource}，提供 OpenCV {@link Mat} 图像数据的封装和管理。
 * 支持从文件、字节数组、输入流或 {@link IOResource} 构建图像资源，并提供图像尺寸、元数据、EXIF 方向等信息的访问。
 * </p>
 *
 * <p><b>核心特性：</b></p>
 * <ul>
 *   <li><b>Mat 管理：</b> 提供 {@link Mat} 图像数据的懒加载和缓存，支持获取原始 Mat 和深拷贝。</li>
 *   <li><b>尺寸获取：</b> 支持从元数据或 Mat 中获取图像尺寸，优先使用元数据以提高性能。</li>
 *   <li><b>EXIF 支持：</b> 支持自动解析 EXIF 方向信息，也支持手动指定方向，可选择在构造时自动校正方向。</li>
 *   <li><b>元数据解析：</b> 使用 Metadata Extractor 解析图像元数据，支持多种图像格式。</li>
 *   <li><b>资源管理：</b> 继承 {@link IOResource} 的资源管理能力，支持关闭和释放资源。</li>
 *   <li><b>读取标志：</b> 支持指定 OpenCV 读取标志（如 IMREAD_UNCHANGED、IMREAD_COLOR 等）控制图像读取方式。</li>
 *   <li><b>BytePointer 转换：</b> 支持将字节数据转换为 {@link BytePointer}，便于 OpenCV 直接解码。</li>
 * </ul>
 *
 * @author pangju666
 * @see IOResource
 * @see Mat
 * @see Size
 * @see OpencvUtils
 * @since 1.1.0
 */
public class OpencvImageResource extends IOResource {
	/**
	 * OpenCV 读取标志
	 * <p>控制图像读取方式的标志（如 IMREAD_UNCHANGED、IMREAD_COLOR 等）。</p>
	 *
	 * @since 1.1.0
	 */
	protected final int flags;
	/**
	 * EXIF 方向是否已校正
	 * <p>标记图像是否已进行 EXIF 方向校正。</p>
	 *
	 * <p>取值说明：</p>
	 * <ul>
	 *   <li>{@code true}：图像已进行 EXIF 方向校正，缓存的图像和尺寸为校正后的结果</li>
	 *   <li>{@code false}：图像未进行 EXIF 方向校正，缓存的图像和尺寸为原始数据</li>
	 * </ul>
	 *
	 * @since 1.1.0
	 */
	protected final boolean orientationCorrected;
	/**
	 * 图像尺寸
	 * <p>包含图像的宽度和高度信息，懒加载并缓存。</p>
	 *
	 * @since 1.1.0
	 */
	protected volatile Size imageSize;
	/**
	 * OpenCV Mat 图像对象
	 * <p>存储图像数据的 Mat 对象，懒加载并缓存。</p>
	 *
	 * @since 1.1.0
	 */
	protected volatile Mat imageMat;
	/**
	 * 图像元数据
	 * <p>使用 Metadata Extractor 解析的图像元数据，懒加载并缓存。</p>
	 *
	 * @since 1.1.0
	 */
	protected volatile Metadata metadata;
	/**
	 * EXIF 方向值
	 * <p>存储图像的 EXIF 方向信息，懒加载并缓存。</p>
	 *
	 * @since 1.1.0
	 */
	protected volatile Integer exifOrientation;

	/**
	 * 基于 IOResource 构造 OpencvImageResource（自动解析 EXIF 方向）
	 * <p>从现有 IOResource 创建 OpencvImageResource，自动解析 EXIF 方向信息。</p>
	 *
	 * @param resource IO 资源对象，不可为 null
	 * @throws IOException              当资源读取失败时抛出
	 * @throws IllegalArgumentException 当 resource 不是图像资源时抛出
	 * @since 1.1.0
	 */
	public OpencvImageResource(IOResource resource) throws IOException {
		this(resource, true);
	}

	/**
	 * 基于 IOResource 构造 OpencvImageResource（可选择是否解析 EXIF 方向）
	 * <p>从现有 IOResource 创建 OpencvImageResource，可选择是否自动解析 EXIF 方向信息。</p>
	 *
	 * @param resource           IO 资源对象，不可为 null
	 * @param correctOrientation 是否解析 EXIF 方向
	 * @throws IOException              当资源读取失败时抛出
	 * @throws IllegalArgumentException 当 resource 不是图像资源时抛出
	 * @since 1.1.0
	 */
	public OpencvImageResource(IOResource resource, boolean correctOrientation) throws IOException {
		super(resource);

		if (resource instanceof OpencvImageResource) {
			this.flags = ((OpencvImageResource) resource).flags;
		} else {
			validateImageType("resource 不是图像资源");

			this.flags = opencv_imgcodecs.IMREAD_UNCHANGED;
		}

		this.orientationCorrected = correctOrientation || (flags != opencv_imgcodecs.IMREAD_UNCHANGED &&
			flags != opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION);

		if (correctOrientation) {
			this.exifOrientation = OpencvConstants.NORMAL_EXIF_ORIENTATION;

			if (Objects.nonNull(this.file)) {
				try {
					this.metadata = ImageMetadataReader.readMetadata(this.file);
					this.exifOrientation = getExifOrientation(this.metadata);
				} catch (ImageProcessingException ignored) {
					this.metadata = new Metadata();
				}

				if ((flags == opencv_imgcodecs.IMREAD_UNCHANGED || flags == opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION) &&
					exifOrientation != OpencvConstants.NORMAL_EXIF_ORIENTATION) {
					try (Mat image = OpencvUtils.read(this.file)) {
						this.imageMat = OpencvUtils.correctOrientation(image, exifOrientation);
						this.imageSize = this.imageMat.size();
					}
				}
			} else {
				try (InputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(getBytes())) {
					this.metadata = ImageMetadataReader.readMetadata(inputStream);
					this.exifOrientation = getExifOrientation(this.metadata);
				} catch (ImageProcessingException ignored) {
					this.metadata = new Metadata();
				}

				if ((flags == opencv_imgcodecs.IMREAD_UNCHANGED || flags == opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION) &&
					exifOrientation != OpencvConstants.NORMAL_EXIF_ORIENTATION) {
					try (InputStream inputStream = newBufferedInputStream();
					     Mat image = OpencvUtils.read(inputStream)) {
						this.imageMat = OpencvUtils.correctOrientation(image, exifOrientation);
						this.imageSize = this.imageMat.size();
					}
				}
			}
		}
	}

	/**
	 * 基于 IOResource 构造 OpencvImageResource（指定读取标志）
	 * <p>从现有 IOResource 创建 OpencvImageResource，指定读取标志。</p>
	 *
	 * <p>方向校正规则：</p>
	 * <ul>
	 *     <li>当 flags 为 {@code IMREAD_UNCHANGED} 或 {@code IMREAD_IGNORE_ORIENTATION} 时，自动校正 EXIF 方向</li>
	 *     <li>其他 flags 不自动校正方向</li>
	 * </ul>
	 *
	 * @param resource IO 资源对象，不可为 null
	 * @param flags    OpenCV 读取标志
	 * @throws IOException              当资源读取失败时抛出
	 * @throws IllegalArgumentException 当 resource 不是图像资源时抛出
	 * @since 1.1.0
	 */
	public OpencvImageResource(IOResource resource, int flags) throws IOException {
		this(resource, flags, flags == opencv_imgcodecs.IMREAD_UNCHANGED ||
			flags == opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION);
	}

	/**
	 * 基于 IOResource 构造 OpencvImageResource（指定读取标志和是否解析 EXIF）
	 * <p>从现有 IOResource 创建 OpencvImageResource，指定读取标志和是否解析 EXIF 方向。</p>
	 *
	 * @param resource           IO 资源对象，不可为 null
	 * @param flags              OpenCV 读取标志
	 * @param correctOrientation 是否解析 EXIF 方向
	 * @throws IOException              当资源读取失败时抛出
	 * @throws IllegalArgumentException 当 resource 不是图像资源时抛出
	 * @since 1.1.0
	 */
	public OpencvImageResource(IOResource resource, int flags, boolean correctOrientation) throws IOException {
		super(resource);

		if (!(resource instanceof OpencvImageResource)) {
			validateImageType("resource 不是图像资源");
		}

		this.flags = flags;
		this.orientationCorrected = correctOrientation || (flags != opencv_imgcodecs.IMREAD_UNCHANGED &&
			flags != opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION);

		if (correctOrientation) {
			this.exifOrientation = OpencvConstants.NORMAL_EXIF_ORIENTATION;

			if (Objects.nonNull(this.file)) {
				try {
					this.metadata = ImageMetadataReader.readMetadata(this.file);
					this.exifOrientation = getExifOrientation(this.metadata);
				} catch (ImageProcessingException ignored) {
					this.metadata = new Metadata();
				}

				if ((flags == opencv_imgcodecs.IMREAD_UNCHANGED || flags == opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION) &&
					exifOrientation != OpencvConstants.NORMAL_EXIF_ORIENTATION) {
					try (Mat image = OpencvUtils.read(this.file)) {
						this.imageMat = OpencvUtils.correctOrientation(image, exifOrientation);
						this.imageSize = this.imageMat.size();
					}
				}
			} else {
				try (InputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(getBytes())) {
					this.metadata = ImageMetadataReader.readMetadata(inputStream);
					this.exifOrientation = getExifOrientation(this.metadata);
				} catch (ImageProcessingException ignored) {
					this.metadata = new Metadata();
				}

				if ((flags == opencv_imgcodecs.IMREAD_UNCHANGED || flags == opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION) &&
					exifOrientation != OpencvConstants.NORMAL_EXIF_ORIENTATION) {
					try (InputStream inputStream = newBufferedInputStream();
					     Mat image = OpencvUtils.read(inputStream)) {
						this.imageMat = OpencvUtils.correctOrientation(image, exifOrientation);
						this.imageSize = this.imageMat.size();
					}
				}
			}
		}
	}

	/**
	 * 基于 IOResource 构造 OpencvImageResource（指定读取标志和 EXIF 方向）
	 * <p>从现有 IOResource 创建 OpencvImageResource，指定读取标志和 EXIF 方向值。</p>
	 *
	 * @param resource        IO 资源对象，不可为 null
	 * @param flags           OpenCV 读取标志
	 * @param exifOrientation EXIF 方向值（必须介于 1-8 之间）
	 * @throws IOException              当资源读取失败时抛出
	 * @throws IllegalArgumentException 当 resource 不是图像资源或 exifOrientation 不在 1-8 范围内时抛出
	 * @since 1.1.0
	 */
	public OpencvImageResource(IOResource resource, int flags, int exifOrientation) throws IOException {
		super(resource);

		Validate.inclusiveBetween(1, 8, exifOrientation, "exifOrientation 必须介于1-8之间");
		if (!(resource instanceof OpencvImageResource)) {
			validateImageType("resource 不是图像资源");
		}

		this.flags = flags;
		this.orientationCorrected = true;
		this.exifOrientation = exifOrientation;

		if ((flags == opencv_imgcodecs.IMREAD_UNCHANGED || flags == opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION) &&
			exifOrientation != OpencvConstants.NORMAL_EXIF_ORIENTATION) {
			if (Objects.nonNull(this.file)) {
				try (Mat image = OpencvUtils.read(this.file)) {
					this.imageMat = OpencvUtils.correctOrientation(image, exifOrientation);
					this.imageSize = this.imageMat.size();
				}
			} else {
				try (InputStream inputStream = newBufferedInputStream();
				     Mat image = OpencvUtils.read(inputStream)) {
					this.imageMat = OpencvUtils.correctOrientation(image, exifOrientation);
					this.imageSize = this.imageMat.size();
				}
			}
		}
	}

	/**
	 * 基于文件路径构造 OpencvImageResource（自动解析 EXIF 方向）
	 * <p>从文件路径创建 OpencvImageResource，自动解析 EXIF 方向信息。</p>
	 *
	 * @param filePath 文件路径，不可为 null
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当文件不是图像文件时抛出
	 * @since 1.1.0
	 */
	public OpencvImageResource(String filePath) throws IOException {
		this(filePath, opencv_imgcodecs.IMREAD_UNCHANGED, true);
	}

	/**
	 * 基于文件路径构造 OpencvImageResource（可选择是否解析 EXIF 方向）
	 * <p>从文件路径创建 OpencvImageResource，可选择是否自动解析 EXIF 方向信息。</p>
	 *
	 * @param filePath           文件路径，不可为 null
	 * @param correctOrientation 是否解析 EXIF 方向
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当文件不是图像文件时抛出
	 * @since 1.1.0
	 */
	public OpencvImageResource(String filePath, boolean correctOrientation) throws IOException {
		this(filePath, opencv_imgcodecs.IMREAD_UNCHANGED, correctOrientation);
	}

	/**
	 * 基于文件路径构造 OpencvImageResource（指定读取标志）
	 * <p>从文件路径创建 OpencvImageResource，指定读取标志。</p>
	 *
	 * <p>方向校正规则：</p>
	 * <ul>
	 *     <li>当 flags 为 {@code IMREAD_UNCHANGED} 或 {@code IMREAD_IGNORE_ORIENTATION} 时，自动校正 EXIF 方向</li>
	 *     <li>其他 flags 不自动校正方向</li>
	 * </ul>
	 *
	 * @param filePath 文件路径，不可为 null
	 * @param flags    OpenCV 读取标志
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当文件不是图像文件时抛出
	 * @since 1.1.0
	 */
	public OpencvImageResource(String filePath, int flags) throws IOException {
		this(filePath, flags, flags == opencv_imgcodecs.IMREAD_UNCHANGED ||
			flags == opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION);
	}

	/**
	 * 基于文件路径构造 OpencvImageResource（指定读取标志和是否解析 EXIF）
	 * <p>从文件路径创建 OpencvImageResource，指定读取标志和是否解析 EXIF 方向。</p>
	 *
	 * @param filePath           文件路径，不可为 null
	 * @param flags              OpenCV 读取标志
	 * @param correctOrientation 是否解析 EXIF 方向
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当文件不是图像文件时抛出
	 * @since 1.1.0
	 */
	public OpencvImageResource(String filePath, int flags, boolean correctOrientation) throws IOException {
		super(filePath, false);

		validateImageType("file 不是图像文件");

		this.flags = flags;
		this.orientationCorrected = correctOrientation || (flags != opencv_imgcodecs.IMREAD_UNCHANGED &&
			flags != opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION);

		if (correctOrientation) {
			this.exifOrientation = OpencvConstants.NORMAL_EXIF_ORIENTATION;
			try {
				this.metadata = ImageMetadataReader.readMetadata(this.file);
				this.exifOrientation = getExifOrientation(this.metadata);
			} catch (ImageProcessingException ignored) {
				this.metadata = new Metadata();
			}
		}

		if ((flags == opencv_imgcodecs.IMREAD_UNCHANGED || flags == opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION) &&
			exifOrientation != OpencvConstants.NORMAL_EXIF_ORIENTATION) {
			try (Mat image = OpencvUtils.read(this.file)) {
				this.imageMat = OpencvUtils.correctOrientation(image, exifOrientation);
				this.imageSize = this.imageMat.size();
			}
		}
	}

	/**
	 * 基于文件路径构造 OpencvImageResource（指定读取标志和 EXIF 方向）
	 * <p>从文件路径创建 OpencvImageResource，指定读取标志和 EXIF 方向值。</p>
	 *
	 * @param filePath        文件路径，不可为 null
	 * @param flags           OpenCV 读取标志
	 * @param exifOrientation EXIF 方向值（必须介于 1-8 之间）
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当文件不是图像文件或 exifOrientation 不在 1-8 范围内时抛出
	 * @since 1.1.0
	 */
	public OpencvImageResource(String filePath, int flags, int exifOrientation) throws IOException {
		super(filePath, false);

		Validate.inclusiveBetween(1, 8, exifOrientation, "exifOrientation 必须介于1-8之间");
		validateImageType("file 不是图像文件");

		this.flags = flags;
		this.orientationCorrected = true;
		this.exifOrientation = exifOrientation;

		if ((flags == opencv_imgcodecs.IMREAD_UNCHANGED || flags == opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION) &&
			exifOrientation != OpencvConstants.NORMAL_EXIF_ORIENTATION) {
			try (Mat image = OpencvUtils.read(this.file)) {
				this.imageMat = OpencvUtils.correctOrientation(image, exifOrientation);
				this.imageSize = this.imageMat.size();
			}
		}
	}

	/**
	 * 基于文件构造 OpencvImageResource（自动解析 EXIF 方向）
	 * <p>从文件创建 OpencvImageResource，自动解析 EXIF 方向信息。</p>
	 *
	 * @param file 文件对象，不可为 null
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当文件不是图像文件时抛出
	 * @since 1.1.0
	 */
	public OpencvImageResource(File file) throws IOException {
		this(file, opencv_imgcodecs.IMREAD_UNCHANGED, true);
	}

	/**
	 * 基于文件构造 OpencvImageResource（可选择是否解析 EXIF 方向）
	 * <p>从文件创建 OpencvImageResource，可选择是否自动解析 EXIF 方向信息。</p>
	 *
	 * @param file               文件对象，不可为 null
	 * @param correctOrientation 是否解析 EXIF 方向
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当文件不是图像文件时抛出
	 * @since 1.1.0
	 */
	public OpencvImageResource(File file, boolean correctOrientation) throws IOException {
		this(file, opencv_imgcodecs.IMREAD_UNCHANGED, correctOrientation);
	}

	/**
	 * 基于文件构造 OpencvImageResource（指定读取标志）
	 * <p>从文件创建 OpencvImageResource，指定读取标志。</p>
	 *
	 * <p>方向校正规则：</p>
	 * <ul>
	 *     <li>当 flags 为 {@code IMREAD_UNCHANGED} 或 {@code IMREAD_IGNORE_ORIENTATION} 时，自动校正 EXIF 方向</li>
	 *     <li>其他 flags 不自动校正方向</li>
	 * </ul>
	 *
	 * @param file  文件对象，不可为 null
	 * @param flags OpenCV 读取标志
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当文件不是图像文件时抛出
	 * @since 1.1.0
	 */
	public OpencvImageResource(File file, int flags) throws IOException {
		this(file, flags, flags == opencv_imgcodecs.IMREAD_UNCHANGED ||
			flags == opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION);
	}

	/**
	 * 基于文件构造 OpencvImageResource（指定读取标志和是否解析 EXIF）
	 * <p>从文件创建 OpencvImageResource，指定读取标志和是否解析 EXIF 方向。</p>
	 *
	 * @param file               文件对象，不可为 null
	 * @param flags              OpenCV 读取标志
	 * @param correctOrientation 是否解析 EXIF 方向
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当文件不是图像文件时抛出
	 * @since 1.1.0
	 */
	public OpencvImageResource(File file, int flags, boolean correctOrientation) throws IOException {
		super(file, false);

		validateImageType("file 不是图像文件");

		this.flags = flags;
		this.orientationCorrected = correctOrientation || (flags != opencv_imgcodecs.IMREAD_UNCHANGED &&
			flags != opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION);


		if (correctOrientation) {
			this.exifOrientation = OpencvConstants.NORMAL_EXIF_ORIENTATION;
			try {
				this.metadata = ImageMetadataReader.readMetadata(file);
				this.exifOrientation = getExifOrientation(this.metadata);
			} catch (ImageProcessingException ignored) {
				this.metadata = new Metadata();
			}

			if ((flags == opencv_imgcodecs.IMREAD_UNCHANGED || flags == opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION) &&
				exifOrientation != OpencvConstants.NORMAL_EXIF_ORIENTATION) {
				try (Mat image = OpencvUtils.read(this.file)) {
					this.imageMat = OpencvUtils.correctOrientation(image, exifOrientation);
					this.imageSize = this.imageMat.size();
				}
			}
		}
	}

	/**
	 * 基于文件构造 OpencvImageResource（指定读取标志和 EXIF 方向）
	 * <p>从文件创建 OpencvImageResource，指定读取标志和 EXIF 方向值。</p>
	 *
	 * @param file            文件对象，不可为 null
	 * @param flags           OpenCV 读取标志
	 * @param exifOrientation EXIF 方向值（必须介于 1-8 之间）
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当文件不是图像文件或 exifOrientation 不在 1-8 范围内时抛出
	 * @since 1.1.0
	 */
	public OpencvImageResource(File file, int flags, int exifOrientation) throws IOException {
		super(file, false);

		Validate.inclusiveBetween(1, 8, exifOrientation, "exifOrientation 必须介于1-8之间");
		validateImageType("file 不是图像文件");

		this.flags = flags;
		this.orientationCorrected = true;
		this.exifOrientation = exifOrientation;

		if ((flags == opencv_imgcodecs.IMREAD_UNCHANGED || flags == opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION) &&
			exifOrientation != OpencvConstants.NORMAL_EXIF_ORIENTATION) {
			try (Mat image = OpencvUtils.read(this.file)) {
				this.imageMat = OpencvUtils.correctOrientation(image, exifOrientation);
				this.imageSize = this.imageMat.size();
			}
		}
	}

	/**
	 * 基于字节数组构造 OpencvImageResource（自动解析 EXIF 方向）
	 * <p>从字节数组创建 OpencvImageResource，自动解析 EXIF 方向信息。</p>
	 *
	 * @param bytes 字节数组，不可为 null
	 * @throws IOException              当数据读取失败时抛出
	 * @throws IllegalArgumentException 当数据不是图像数据时抛出
	 * @since 1.1.0
	 */
	public OpencvImageResource(byte[] bytes) throws IOException {
		this(bytes, opencv_imgcodecs.IMREAD_UNCHANGED, true);
	}

	/**
	 * 基于字节数组构造 OpencvImageResource（可选择是否解析 EXIF 方向）
	 * <p>从字节数组创建 OpencvImageResource，可选择是否自动解析 EXIF 方向信息。</p>
	 *
	 * @param bytes              字节数组，不可为 null
	 * @param correctOrientation 是否解析 EXIF 方向
	 * @throws IOException              当数据读取失败时抛出
	 * @throws IllegalArgumentException 当数据不是图像数据时抛出
	 * @since 1.1.0
	 */
	public OpencvImageResource(byte[] bytes, boolean correctOrientation) throws IOException {
		this(bytes, opencv_imgcodecs.IMREAD_UNCHANGED, correctOrientation);
	}

	/**
	 * 基于字节数组构造 OpencvImageResource（指定读取标志）
	 * <p>从字节数组创建 OpencvImageResource，指定读取标志。</p>
	 *
	 * <p>方向校正规则：</p>
	 * <ul>
	 *     <li>当 flags 为 {@code IMREAD_UNCHANGED} 或 {@code IMREAD_IGNORE_ORIENTATION} 时，自动校正 EXIF 方向</li>
	 *     <li>其他 flags 不自动校正方向</li>
	 * </ul>
	 *
	 * @param bytes 字节数组，不可为 null
	 * @param flags OpenCV 读取标志
	 * @throws IOException              当数据读取失败时抛出
	 * @throws IllegalArgumentException 当数据不是图像数据时抛出
	 * @since 1.1.0
	 */
	public OpencvImageResource(byte[] bytes, int flags) throws IOException {
		this(bytes, flags, flags == opencv_imgcodecs.IMREAD_UNCHANGED ||
			flags == opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION);
	}

	/**
	 * 基于字节数组构造 OpencvImageResource（指定读取标志和是否解析 EXIF）
	 * <p>从字节数组创建 OpencvImageResource，指定读取标志和是否解析 EXIF 方向。</p>
	 *
	 * @param bytes              字节数组，不可为 null
	 * @param flags              OpenCV 读取标志
	 * @param correctOrientation 是否解析 EXIF 方向
	 * @throws IOException              当数据读取失败时抛出
	 * @throws IllegalArgumentException 当数据不是图像数据时抛出
	 * @since 1.1.0
	 */
	public OpencvImageResource(byte[] bytes, int flags, boolean correctOrientation) throws IOException {
		super(bytes);

		validateImageType("bytes 不是图像数据");

		this.flags = flags;
		this.orientationCorrected = correctOrientation || (flags != opencv_imgcodecs.IMREAD_UNCHANGED &&
			flags != opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION);

		if (correctOrientation) {
			this.exifOrientation = OpencvConstants.NORMAL_EXIF_ORIENTATION;
			try (InputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(getBytes())) {
				this.metadata = ImageMetadataReader.readMetadata(inputStream);
				this.exifOrientation = getExifOrientation(this.metadata);
			} catch (ImageProcessingException ignored) {
				this.metadata = new Metadata();
			}

			if ((flags == opencv_imgcodecs.IMREAD_UNCHANGED || flags == opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION) &&
				exifOrientation != OpencvConstants.NORMAL_EXIF_ORIENTATION) {
				try (InputStream inputStream = newBufferedInputStream();
				     Mat image = OpencvUtils.read(inputStream)) {
					this.imageMat = OpencvUtils.correctOrientation(image, exifOrientation);
					this.imageSize = this.imageMat.size();
				}
			}
		}
	}

	/**
	 * 基于字节数组构造 OpencvImageResource（指定读取标志和 EXIF 方向）
	 * <p>从字节数组创建 OpencvImageResource，指定读取标志和 EXIF 方向值。</p>
	 *
	 * @param bytes           字节数组，不可为 null
	 * @param flags           OpenCV 读取标志
	 * @param exifOrientation EXIF 方向值（必须介于 1-8 之间）
	 * @throws IOException              当数据读取失败时抛出
	 * @throws IllegalArgumentException 当数据不是图像数据或 exifOrientation 不在 1-8 范围内时抛出
	 * @since 1.1.0
	 */
	public OpencvImageResource(byte[] bytes, int flags, int exifOrientation) throws IOException {
		super(bytes);

		Validate.inclusiveBetween(1, 8, exifOrientation, "exifOrientation 必须介于1-8之间");
		validateImageType("bytes 不是图像数据");

		this.flags = flags;
		this.orientationCorrected = true;
		this.exifOrientation = exifOrientation;

		if ((flags == opencv_imgcodecs.IMREAD_UNCHANGED || flags == opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION) &&
			exifOrientation != OpencvConstants.NORMAL_EXIF_ORIENTATION) {
			try (InputStream inputStream = newBufferedInputStream();
			     Mat image = OpencvUtils.read(inputStream)) {
				this.imageMat = OpencvUtils.correctOrientation(image, exifOrientation);
				this.imageSize = this.imageMat.size();
			}
		}
	}

	/**
	 * 基于输入流构造 OpencvImageResource（自动解析 EXIF 方向）
	 * <p>从输入流创建 OpencvImageResource，自动解析 EXIF 方向信息。</p>
	 *
	 * @param inputStream 输入流，不可为 null
	 * @throws IOException              当流读取失败时抛出
	 * @throws IllegalArgumentException 当流不是图像数据输入流时抛出
	 * @since 1.1.0
	 */
	public OpencvImageResource(InputStream inputStream) throws IOException {
		this(inputStream, opencv_imgcodecs.IMREAD_UNCHANGED, true);
	}

	/**
	 * 基于输入流构造 OpencvImageResource（可选择是否解析 EXIF 方向）
	 * <p>从输入流创建 OpencvImageResource，可选择是否自动解析 EXIF 方向信息。</p>
	 *
	 * @param inputStream        输入流，不可为 null
	 * @param correctOrientation 是否解析 EXIF 方向
	 * @throws IOException              当流读取失败时抛出
	 * @throws IllegalArgumentException 当流不是图像数据输入流时抛出
	 * @since 1.1.0
	 */
	public OpencvImageResource(InputStream inputStream, boolean correctOrientation) throws IOException {
		this(inputStream, opencv_imgcodecs.IMREAD_UNCHANGED, correctOrientation);
	}

	/**
	 * 基于输入流构造 OpencvImageResource（指定读取标志）
	 * <p>从输入流创建 OpencvImageResource，指定读取标志。</p>
	 *
	 * <p>方向校正规则：</p>
	 * <ul>
	 *     <li>当 flags 为 {@code IMREAD_UNCHANGED} 或 {@code IMREAD_IGNORE_ORIENTATION} 时，自动校正 EXIF 方向</li>
	 *     <li>其他 flags 不自动校正方向</li>
	 * </ul>
	 *
	 * @param inputStream 输入流，不可为 null
	 * @param flags       OpenCV 读取标志
	 * @throws IOException              当流读取失败时抛出
	 * @throws IllegalArgumentException 当流不是图像数据输入流时抛出
	 * @since 1.1.0
	 */
	public OpencvImageResource(InputStream inputStream, int flags) throws IOException {
		this(inputStream, flags, flags == opencv_imgcodecs.IMREAD_UNCHANGED ||
			flags == opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION);
	}

	/**
	 * 基于输入流构造 OpencvImageResource（指定读取标志和是否解析 EXIF）
	 * <p>从输入流创建 OpencvImageResource，指定读取标志和是否解析 EXIF 方向。</p>
	 *
	 * @param inputStream        输入流，不可为 null
	 * @param flags              OpenCV 读取标志
	 * @param correctOrientation 是否解析 EXIF 方向
	 * @throws IOException              当流读取失败时抛出
	 * @throws IllegalArgumentException 当流不是图像数据输入流时抛出
	 * @since 1.1.0
	 */
	public OpencvImageResource(InputStream inputStream, int flags, boolean correctOrientation) throws IOException {
		super(inputStream);

		validateImageType("inputStream 不是图像数据输入流");

		this.flags = flags;
		this.orientationCorrected = correctOrientation || (flags != opencv_imgcodecs.IMREAD_UNCHANGED &&
			flags != opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION);

		if (correctOrientation) {
			this.exifOrientation = OpencvConstants.NORMAL_EXIF_ORIENTATION;
			try (InputStream tmpInputStream = IOUtils.toUnsynchronizedByteArrayInputStream(getBytes())) {
				this.metadata = ImageMetadataReader.readMetadata(tmpInputStream);
				this.exifOrientation = getExifOrientation(this.metadata);
			} catch (ImageProcessingException ignored) {
				this.metadata = new Metadata();
			}

			if ((flags == opencv_imgcodecs.IMREAD_UNCHANGED || flags == opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION) &&
				exifOrientation != OpencvConstants.NORMAL_EXIF_ORIENTATION) {
				try (InputStream bufferedInputStream = newBufferedInputStream();
				     Mat image = OpencvUtils.read(bufferedInputStream)) {
					this.imageMat = OpencvUtils.correctOrientation(image, exifOrientation);
					this.imageSize = this.imageMat.size();
				}
			}
		}
	}

	/**
	 * 基于输入流构造 OpencvImageResource（指定读取标志和 EXIF 方向）
	 * <p>从输入流创建 OpencvImageResource，指定读取标志和 EXIF 方向值。</p>
	 *
	 * @param inputStream     输入流，不可为 null
	 * @param flags           OpenCV 读取标志
	 * @param exifOrientation EXIF 方向值（必须介于 1-8 之间）
	 * @throws IOException              当流读取失败时抛出
	 * @throws IllegalArgumentException 当流不是图像数据输入流或 exifOrientation 不在 1-8 范围内时抛出
	 * @since 1.1.0
	 */
	public OpencvImageResource(InputStream inputStream, int flags, int exifOrientation) throws IOException {
		super(inputStream);

		Validate.inclusiveBetween(1, 8, exifOrientation, "exifOrientation 必须介于1-8之间");
		validateImageType("inputStream 不是图像数据输入流");

		this.flags = flags;
		this.orientationCorrected = true;
		this.exifOrientation = exifOrientation;

		if ((flags == opencv_imgcodecs.IMREAD_UNCHANGED || flags == opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION) &&
			exifOrientation != OpencvConstants.NORMAL_EXIF_ORIENTATION) {
			try (InputStream bufferedInputStream = newBufferedInputStream();
			     Mat image = OpencvUtils.read(bufferedInputStream)) {
				this.imageMat = OpencvUtils.correctOrientation(image, exifOrientation);
				this.imageSize = this.imageMat.size();
			}
		}
	}

	/**
	 * 从元数据中获取 EXIF 方向信息
	 *
	 * @param metadata EXIF 元数据，不能为 null
	 * @return EXIF 方向值
	 * @throws IllegalArgumentException 如果 metadata 为 null
	 * @since 1.1.0
	 */
	protected static int getExifOrientation(final Metadata metadata) {
		Validate.notNull(metadata, "metadata 不可为 null");

		ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
		if (Objects.nonNull(exifIFD0Directory)) {
			Integer orientation = exifIFD0Directory.getInteger(ExifDirectoryBase.TAG_ORIENTATION);
			if (Objects.nonNull(orientation)) {
				return orientation;
			}
		}
		return OpencvConstants.NORMAL_EXIF_ORIENTATION;
	}

	/**
	 * 从元数据中获取图像尺寸
	 * <p>支持从多种图像格式的元数据中提取尺寸信息，包括 EXIF、BMP、EPS、GIF、HEIF、ICO、JPEG、PSD、PNG、WEBP 等。</p>
	 * <p>优先从 EXIF IFD0 目录中获取，如果未找到则遍历所有目录尝试提取。</p>
	 *
	 * @param metadata 图像元数据，不能为 null
	 * @return 图像尺寸对象，如果无法提取则返回 null
	 * @throws IllegalArgumentException 如果 metadata 为 null
	 * @since 1.1.0
	 */
	protected static Size getSize(final Metadata metadata) {
		Validate.notNull(metadata, "metadata 不可为 null");

		Integer imageWidth = null;
		Integer imageHeight = null;

		ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
		if (Objects.nonNull(exifIFD0Directory)) {
			if (exifIFD0Directory.containsTag(ExifDirectoryBase.TAG_IMAGE_WIDTH) &&
				exifIFD0Directory.containsTag(ExifDirectoryBase.TAG_IMAGE_HEIGHT)) {
				imageWidth = exifIFD0Directory.getInteger(ExifDirectoryBase.TAG_IMAGE_WIDTH);
				imageHeight = exifIFD0Directory.getInteger(ExifDirectoryBase.TAG_IMAGE_HEIGHT);
			}
		}

		if (ObjectUtils.anyNull(imageWidth, imageHeight)) {
			for (Directory directory : metadata.getDirectories()) {
				if (directory instanceof BmpHeaderDirectory) {
					BmpHeaderDirectory bmpHeaderDirectory = (BmpHeaderDirectory) directory;
					if (bmpHeaderDirectory.containsTag(BmpHeaderDirectory.TAG_IMAGE_WIDTH) &&
						bmpHeaderDirectory.containsTag(BmpHeaderDirectory.TAG_IMAGE_HEIGHT)) {
						imageWidth = bmpHeaderDirectory.getInteger(BmpHeaderDirectory.TAG_IMAGE_WIDTH);
						imageHeight = bmpHeaderDirectory.getInteger(BmpHeaderDirectory.TAG_IMAGE_HEIGHT);
					}
					break;
				} else if (directory instanceof EpsDirectory) {
					EpsDirectory epsDirectory = (EpsDirectory) directory;
					if (epsDirectory.containsTag(EpsDirectory.TAG_IMAGE_WIDTH) &&
						epsDirectory.containsTag(EpsDirectory.TAG_IMAGE_HEIGHT)) {
						imageWidth = epsDirectory.getInteger(EpsDirectory.TAG_IMAGE_WIDTH);
						imageHeight = epsDirectory.getInteger(EpsDirectory.TAG_IMAGE_HEIGHT);
					}
					break;
				} else if (directory instanceof GifImageDirectory) {
					GifImageDirectory gifImageDirectory = (GifImageDirectory) directory;
					if (gifImageDirectory.containsTag(GifImageDirectory.TAG_WIDTH) &&
						gifImageDirectory.containsTag(GifImageDirectory.TAG_HEIGHT)) {
						imageWidth = gifImageDirectory.getInteger(GifImageDirectory.TAG_WIDTH);
						imageHeight = gifImageDirectory.getInteger(GifImageDirectory.TAG_HEIGHT);
					}
					break;
				} else if (directory instanceof HeifDirectory) {
					HeifDirectory heifDirectory = (HeifDirectory) directory;
					if (heifDirectory.containsTag(HeifDirectory.TAG_IMAGE_WIDTH) &&
						heifDirectory.containsTag(HeifDirectory.TAG_IMAGE_HEIGHT)) {
						imageWidth = heifDirectory.getInteger(HeifDirectory.TAG_IMAGE_WIDTH);
						imageHeight = heifDirectory.getInteger(HeifDirectory.TAG_IMAGE_HEIGHT);
					}
					break;
				} else if (directory instanceof IcoDirectory) {
					IcoDirectory icoDirectory = (IcoDirectory) directory;
					if (icoDirectory.containsTag(IcoDirectory.TAG_IMAGE_WIDTH) &&
						icoDirectory.containsTag(IcoDirectory.TAG_IMAGE_HEIGHT)) {
						imageWidth = icoDirectory.getInteger(IcoDirectory.TAG_IMAGE_WIDTH);
						imageHeight = icoDirectory.getInteger(IcoDirectory.TAG_IMAGE_HEIGHT);
					}
					break;
				} else if (directory instanceof JpegDirectory) {
					JpegDirectory jpegDirectory = (JpegDirectory) directory;
					if (jpegDirectory.containsTag(JpegDirectory.TAG_IMAGE_WIDTH) &&
						jpegDirectory.containsTag(JpegDirectory.TAG_IMAGE_HEIGHT)) {
						imageWidth = jpegDirectory.getInteger(JpegDirectory.TAG_IMAGE_WIDTH);
						imageHeight = jpegDirectory.getInteger(JpegDirectory.TAG_IMAGE_HEIGHT);
					}
					break;
				} else if (directory instanceof PsdHeaderDirectory) {
					PsdHeaderDirectory exifDirectory = (PsdHeaderDirectory) directory;
					if (exifDirectory.containsTag(PsdHeaderDirectory.TAG_IMAGE_WIDTH) &&
						exifDirectory.containsTag(PsdHeaderDirectory.TAG_IMAGE_HEIGHT)) {
						imageWidth = exifDirectory.getInteger(PsdHeaderDirectory.TAG_IMAGE_WIDTH);
						imageHeight = exifDirectory.getInteger(PsdHeaderDirectory.TAG_IMAGE_HEIGHT);
					}
					break;
				} else if (directory instanceof PngDirectory) {
					PngDirectory pngDirectory = (PngDirectory) directory;
					if (pngDirectory.containsTag(PngDirectory.TAG_IMAGE_WIDTH) &&
						pngDirectory.containsTag(PngDirectory.TAG_IMAGE_HEIGHT)) {
						imageWidth = pngDirectory.getInteger(PngDirectory.TAG_IMAGE_WIDTH);
						imageHeight = pngDirectory.getInteger(PngDirectory.TAG_IMAGE_HEIGHT);
					}
					break;
				} else if (directory instanceof WebpDirectory) {
					WebpDirectory webpDirectory = (WebpDirectory) directory;
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

		return new Size(imageWidth, imageHeight);
	}

	/**
	 * 获取图像尺寸
	 * <p>优先从元数据中获取尺寸，如果元数据中没有则从 Mat 中获取。结果会被缓存。</p>
	 *
	 * @return 图像尺寸对象
	 * @throws IOException 当资源已关闭时抛出
	 * @since 1.1.0
	 */
	public Size getImageSize() throws IOException {
		checkClosed();

		synchronized (this) {
			if (Objects.nonNull(imageSize)) {
				return imageSize;
			}

			Metadata metadata = getMetadata();
			this.imageSize = getSize(metadata);

			if (Objects.isNull(this.imageSize)) {
				this.imageMat = getImageMat();
				this.imageSize = imageMat.size();
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
	public void setImageSize(Size imageSize) throws IOException {
		checkClosed();

		this.imageSize = imageSize;
	}

	/**
	 * 获取 EXIF 方向值
	 * <p>优先从缓存获取，若未缓存则从元数据中解析。结果会被缓存。</p>
	 *
	 * @return EXIF 方向值（1-8），如果无法解析则返回 1（正常方向）
	 * @throws IOException 当资源已关闭时抛出
	 * @since 1.1.0
	 */
	public int getExifOrientation() throws IOException {
		checkClosed();

		synchronized (this) {
			if (Objects.nonNull(exifOrientation)) {
				return exifOrientation;
			}

			Metadata metadata = getMetadata();
			this.exifOrientation = getExifOrientation(metadata);

			return exifOrientation;
		}
	}

	/**
	 * 设置 EXIF 方向值
	 * <p>用于手动设置 EXIF 方向值，覆盖自动解析的值。</p>
	 *
	 * @param exifOrientation EXIF 方向值（必须介于 1-8 之间），null 表示清除缓存
	 * @throws IOException              当资源已关闭时抛出
	 * @throws IllegalArgumentException 当 exifOrientation 不在 1-8 范围内时抛出
	 * @since 1.1.0
	 */
	public void setExifOrientation(Integer exifOrientation) throws IOException {
		checkClosed();

		if (Objects.nonNull(exifOrientation)) {
			Validate.inclusiveBetween(1, 8, exifOrientation, "exifOrientation 必须介于1-8之间");
		}

		this.exifOrientation = exifOrientation;
	}

	/**
	 * 获取图像元数据
	 * <p>使用 Metadata Extractor 解析图像元数据，如果解析失败返回空 Metadata 对象。结果会被缓存。</p>
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
				try (InputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(getBytes())) {
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
	 * 获取 OpenCV Mat 图像对象
	 * <p>从文件或字节数组中读取图像数据并转换为 Mat 对象。结果会被缓存。</p>
	 *
	 * <p>实现细节：</p>
	 * <ul>
	 *     <li>文件模式：使用 {@link OpencvUtils#read(File, int)} 直接读取文件</li>
	 *     <li>字节数组/输入流模式：通过 {@link #toBytePointer()} 转换为 BytePointer，然后使用 {@link opencv_imgcodecs#imdecode(Mat, int)} 解码</li>
	 * </ul>
	 *
	 * @return OpenCV Mat 图像对象
	 * @throws IOException 当资源已关闭或图像读取失败时抛出
	 * @since 1.1.0
	 */
	public Mat getImageMat() throws IOException {
		checkClosed();

		synchronized (this) {
			if (Objects.nonNull(imageMat)) {
				return imageMat;
			}

			if (Objects.nonNull(file)) {
				this.imageMat = OpencvUtils.read(file, flags);
				if (OpencvUtils.isEmpty(imageMat)) {
					throw new IOException("图片读取失败，文件路径：" + file.getAbsolutePath());
				}
			} else {
				try (BytePointer bytePointer = toBytePointer();
				     Mat bytesMat = new Mat(bytePointer)) {
					this.imageMat = opencv_imgcodecs.imdecode(bytesMat, flags);
					if (OpencvUtils.isEmpty(imageMat)) {
						throw new IOException("图片读取失败");
					}
				}
			}
			return imageMat;
		}
	}

	/**
	 * 获取 OpenCV Mat 图像对象的深拷贝
	 * <p>返回当前 Mat 对象的深拷贝，修改返回的 Mat 不会影响原始数据。</p>
	 *
	 * @return OpenCV Mat 图像对象的深拷贝
	 * @throws IOException 当资源已关闭时抛出
	 * @since 1.1.0
	 */
	public Mat getImageMatCopy() throws IOException {
		checkClosed();

		synchronized (this) {
			Mat src = imageMat;
			if (Objects.isNull(src)) {
				src = getImageMat();
			}
			return src.clone();
		}
	}

	/**
	 * 转换为 BytePointer
	 * <p>将缓存的字节数组转换为 OpenCV 的 BytePointer 对象。</p>
	 *
	 * <p>实现特性：</p>
	 * <ul>
	 *     <li>直接从内部缓存的字节数组创建 BytePointer</li>
	 *     <li>返回的 BytePointer 可直接用于 OpenCV 的 imdecode 等操作</li>
	 * </ul>
	 *
	 * @return BytePointer 对象
	 * @throws IOException 当资源已关闭时抛出
	 * @since 1.1.0
	 */
	public BytePointer toBytePointer() throws IOException {
		checkClosed();

		if (Objects.nonNull(file)) {
			return new BytePointer(FileUtils.readFileToByteArray(file));
		} else {
			return new BytePointer(byteArrayOutputStream.toByteArray());
		}
	}

	/**
	 * 关闭资源
	 * <p>释放图像相关资源，包括释放 Mat 和 Size 对象的引用、清空元数据和尺寸缓存。</p>
	 *
	 * @throws IOException 当关闭资源失败时抛出
	 * @since 1.1.0
	 */
	@Override
	public synchronized void close() throws IOException {
		if (Objects.nonNull(this.imageMat)) {
			this.imageMat.releaseReference();
		}
		if (Objects.nonNull(this.imageSize)) {
			this.imageSize.releaseReference();
		}

		this.metadata = null;
		this.imageMat = null;
		this.imageSize = null;

		super.close();
	}

	/**
	 * 验证图像类型
	 * <p>验证资源是否为图像类型，并检查 OpenCV 是否支持读取该格式。</p>
	 *
	 * @param message 验证失败时的错误消息
	 * @throws IOException              当资源读取失败时抛出
	 * @throws IllegalArgumentException 当资源不是图像类型或不支持读取时抛出
	 * @since 1.1.0
	 */
	protected void validateImageType(String message) throws IOException {
		Validate.isTrue(isImage(), message);

		if (Objects.nonNull(file) && !OpencvUtils.canRead(file)) {
			throw new IllegalArgumentException("不支持读取 " + mimeType + " 类型图像");
		}
	}

	/**
	 * 获取 OpenCV 读取标志
	 * <p>返回用于读取图像的 OpenCV 标志（如 IMREAD_UNCHANGED、IMREAD_COLOR 等）。</p>
	 *
	 * @return OpenCV 读取标志
	 * @since 1.1.0
	 */
	public int getFlags() {
		return flags;
	}

	/**
	 * 判断 EXIF 方向是否已校正
	 * <p>返回图像是否已进行 EXIF 方向校正。</p>
	 *
	 * <p>返回值说明：</p>
	 * <ul>
	 *   <li>{@code true}：图像已进行 EXIF 方向校正，缓存的图像和尺寸为校正后的结果</li>
	 *   <li>{@code false}：图像未进行 EXIF 方向校正，缓存的图像和尺寸为原始数据</li>
	 * </ul>
	 *
	 * @return 如果 EXIF 方向已校正返回 true，否则返回 false
	 * @since 1.1.0
	 */
	public boolean isOrientationCorrected() {
		return orientationCorrected;
	}
}
