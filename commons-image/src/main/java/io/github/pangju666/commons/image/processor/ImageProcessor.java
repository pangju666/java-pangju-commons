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

package io.github.pangju666.commons.image.processor;

import com.twelvemonkeys.image.BrightnessContrastFilter;
import com.twelvemonkeys.image.GrayFilter;
import com.twelvemonkeys.image.ImageUtil;
import com.twelvemonkeys.image.ResampleOp;
import io.github.pangju666.commons.image.enums.FlipDirection;
import io.github.pangju666.commons.image.enums.RotateDirection;
import io.github.pangju666.commons.image.io.resource.ImageIOResource;
import io.github.pangju666.commons.image.lang.ImageConstants;
import io.github.pangju666.commons.image.model.ImageSize;
import io.github.pangju666.commons.image.model.ImageWatermarkOption;
import io.github.pangju666.commons.image.model.TextWatermarkOption;
import io.github.pangju666.commons.image.utils.ImageUtils;
import io.github.pangju666.commons.io.utils.FileUtils;
import net.coobird.thumbnailator.filters.Caption;
import net.coobird.thumbnailator.filters.Transparency;
import net.coobird.thumbnailator.filters.Watermark;
import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.ImageFilter;
import java.io.*;
import java.util.Objects;
import java.util.function.Function;

/**
 * 图像编辑器（链式调用风格）
 * <p>
 * 提供流式 API 以便对图像进行缩放、旋转、滤镜、亮度/对比度、灰度转换、透明度调整以及图片/文字水印等常见操作。<br />
 * 支持以 {@link ImageIOResource}、输入流、{@link ImageInputStream} 或 {@link BufferedImage} 作为输入源，并可输出为文件、输出流、{@link ImageOutputStream} 或 {@link BufferedImage}。<br />
 * 当使用 {@link ImageIOResource} 构建时，可选择在构造时自动校正 EXIF 方向并缓存校正后的图像。
 * </p>
 *
 * <p><b>核心特性：</b></p>
 * <ul>
 *   <li><b>链式调用：</b> API 设计简洁，配置与处理顺序清晰。</li>
 *   <li><b>智能格式：</b> 自动根据透明通道选择输出格式（含 Alpha 通道默认为 PNG，否则为 JPG）。</li>
 *   <li><b>状态重置：</b> 支持 {@link #reset()} 方法将图像恢复至初始状态，便于重复使用或撤销操作。</li>
 *   <li><b>资源释放：</b> 支持 {@link #release()} 方法释放图像资源，减少内存占用，释放后编辑器不可再使用。</li>
 *   <li><b>EXIF 支持：</b> 支持通过指定 EXIF 方向值进行图像方向校正，也可使用 {@link ImageIOResource} 已校正的图像避免重复处理。</li>
 *   <li><b>自定义扩展：</b> 通过 {@link #apply(Function)} 方法支持传入任意自定义图像转换函数，灵活扩展编辑功能。</li>
 *   <li><b>丰富操作：</b>
 *     <ul>
 *       <li>缩放：支持按宽/高、按比例、强制尺寸等多种模式。</li>
 *       <li>调整：旋转、翻转、裁剪。</li>
 *       <li>调色：亮度、对比度、灰度化、透明度调整。</li>
 *       <li>特效：模糊、锐化、自定义滤镜。</li>
 *       <li>水印：支持图片和文字水印，提供九宫格方向定位和自定义坐标两种方式。</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p><b>推荐使用方式：</b></p>
 * <ul>
 *   <li><b>推荐：</b> 使用 {@link ImageIOResource} 构建实例，提供统一的资源管理和更好的性能。ImageIOResource 可选择在构造时自动校正 EXIF 方向并缓存校正后的图像。</li>
 * </ul>
 *
 * <p><b>线程安全：</b></p>
 * <ul>
 *   <li>本类 <b>非线程安全</b>。实例包含可变的图像状态（{@code outputImage} 等）。</li>
 *   <li>请确保每个线程使用独立的实例，不要在多线程间共享同一个实例。</li>
 * </ul>
 *
 * <p><b>性能与内存：</b></p>
 * <ul>
 *   <li><b>处理顺序：</b> 建议先进行缩放或裁剪操作，再进行其他处理（如模糊、水印），以减少计算量和内存占用。</li>
 *   <li><b>资源管理：</b> 处理完成后建议调用 {@link #release()} 方法释放图像资源，减少内存占用。释放后编辑器不可再使用。</li>
 *   <li><b>流输入注意事项：</b> 当从 {@link InputStream} 创建并启用 EXIF 自动校正时：
 *     <ul>
 *       <li>推荐使用 {@link ByteArrayInputStream} 或 {@link UnsynchronizedByteArrayInputStream}，可实现零拷贝重复读取。</li>
 *       <li>对于其他类型的流，内部会将数据完全缓冲到内存中，处理大文件时需注意 OOM 风险。</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p><b>推荐方法调用顺序：</b></p>
 * <ol>
 *   <li>裁剪</li>
 *   <li>缩放</li>
 *   <li>旋转</li>
 *   <li>翻转</li>
 *   <li>灰度化</li>
 *   <li>修改亮度</li>
 *   <li>修改对比度</li>
 *   <li>调整透明度</li>
 *   <li>锐化或模糊（这两个效果互斥，一般不会同时用）</li>
 *   <li>滤镜</li>
 *   <li>添加水印</li>
 * </ol>
 *
 * <p><b>代码示例：</b></p>
 * <pre>{@code
 * // 1. 构建实例（推荐方式）
 * // 使用 ImageIOResource 构建（推荐）
 * ImageProcessor.of(new ImageIOResource(new File("input.jpg")));
 * ImageProcessor.of(new ImageIOResource(new File("input.jpg"), false));  // 不矫正 EXIF 方向
 * ImageProcessor.of(new ImageIOResource(new File("input.jpg"), 6));     // 指定 EXIF 方向
 *
 * // 从输入流构建
 * ImageProcessor.of(inputStream);
 * ImageProcessor.of(inputStream, 6);     // 指定 EXIF 方向
 *
 * // 从 ImageInputStream 构建
 * ImageProcessor.of(imageInputStream);
 * ImageProcessor.of(imageInputStream, 6);  // 指定 EXIF 方向
 *
 * // 从 BufferedImage 构建
 * ImageProcessor.of(bufferedImage);
 * ImageProcessor.of(bufferedImage, 6);      // 指定 EXIF 方向
 *
 * // 2. 缩放与调整大小
 * ImageProcessor.of(new ImageIOResource(new File("input.jpg")))
 *     .scaleByWidth(800)              // 按宽度等比缩放
 *     .scaleByHeight(600)             // 按高度等比缩放
 *     .scale(0.5)                     // 按比例缩放（50%）
 *     .resize(100, 100)               // 强制缩放到指定尺寸（不保持比例）
 *     .toFile(new File("out_scale.jpg"));
 *
 * // 3. 裁剪操作
 * ImageProcessor.of(new ImageIOResource(new File("input.jpg")))
 *     .cropByCenter(400, 400)         // 居中裁剪
 *     .cropByRect(0, 0, 200, 200)     // 指定矩形区域裁剪
 *     .cropByOffset(10, 10, 20, 20)   // 按边距裁剪（上、下、左、右）
 *     .toFile(new File("out_crop.jpg"));
 *
 * // 4. 旋转与翻转
 * ImageProcessor.of(new ImageIOResource(new File("input.jpg")))
 *     .rotate(RotateDirection.CW_90)  // 顺时针旋转 90 度
 *     .flip(FlipDirection.HORIZONTAL) // 水平翻转
 *     .toFile(new File("out_rotate.jpg"));
 *
 * // 5. 色彩与滤镜
 * ImageProcessor.of(new ImageIOResource(new File("input.jpg")))
 *     .grayscale()                    // 转为灰度图
 *     .blur(2.0f)                     // 高斯模糊
 *     .sharpen(0.3f)                  // 锐化
 *     .contrast(0.2f)                 // 增加对比度
 *     .brightness(0.1f)               // 增加亮度
 *     .transparency(0.5f)             // 调整透明度为 50%
 *     .filter(new GrayFilter())       // 应用自定义滤镜（支持 java.awt.image.ImageFilter）
 *     .toFile(new File("out_filter.jpg"));
 *
 * // 6. 水印添加（支持图片与文字）
 * ImageProcessor.of(new ImageIOResource(new File("input.jpg"), true))
 *     .addTextWatermark("CONFIDENTIAL", new TextWatermarkOption())
 *     .addImageWatermark(new File("logo.png"), new ImageWatermarkOption())
 *     .toFile(new File("out_watermark.jpg"));
 *
 * // 7. 格式转换与输出
 * ImageProcessor.of(new ImageIOResource(new File("input.png"))) // 输入 PNG
 *     .outputFormat("JPG")            // 强制输出为 JPG
 *     .toFile(new File("output.jpg"));
 *
 * // 8. 复杂操作链（链式调用）
 * ImageProcessor.of(new ImageIOResource(new File("input.jpg"), true))
 *     .cropByCenter(1000, 1000)       // 1. 先裁剪中心 1000x1000 区域
 *     .scaleByWidth(500)              // 2. 缩放到宽度 500px
 *     .blur(2.0f)                     // 3. 应用高斯模糊
 *     .addTextWatermark("PREVIEW", new TextWatermarkOption()) // 4. 添加水印
 *     .toFile(new File("processed.jpg"));
 *
 * // 9. 状态重置与多版本输出
 * ImageProcessor editor = ImageProcessor.of(new ImageIOResource(new File("original.png")));
 * // 输出缩略图
 * editor.scaleByWidth(200)
 *       .toFile(new File("thumbnail.png"));
 * // 重置并输出带水印的高清图
 * editor.reset()
 *       .addTextWatermark("CONFIDENTIAL", new TextWatermarkOption())
 *       .toFile(new File("watermarked_original.png"));
 * }</pre>
 *
 * @author pangju666
 * @see ImageSize
 * @see ImageUtil
 * @see ImageIOResource
 * @see ImageUtils
 * @since 2.1.0
 */
public class ImageProcessor {
	/**
	 * 默认的带透明通道图像输出格式
	 * <p>
	 * 当输入图像包含透明通道（Alpha通道）时，默认使用的输出格式。
	 * PNG格式支持透明度，适合保留原图像的透明效果。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected static final String DEFAULT_ALPHA_OUTPUT_FORMAT = "PNG";

	/**
	 * 默认的标准图像输出格式
	 * <p>
	 * 当输入图像不包含透明通道时，默认使用的输出格式。
	 * JPG格式具有较高的压缩率，适合不需要透明效果的图像。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected static final String DEFAULT_OUTPUT_FORMAT = "JPG";

	/**
	 * 原始图像尺寸
	 * <p>
	 * 存储输入图像的原始宽度和高度信息。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected final ImageSize inputImageSize;

	/**
	 * 原始输入图像
	 * <p>
	 * 存储从各种来源加载的原始图像数据。
	 * 当使用 {@link #of(ImageIOResource)} 构建时，此图像为 ImageIOResource 缓存的图像（可能已进行 EXIF 方向校正）。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected final BufferedImage inputImage;

	/**
	 * 输入图像格式
	 * <p>
	 * 通过 {@link #of(ImageIOResource)} 创建时，取自 ImageIOResource 的格式信息。
	 * 需为受支持的读取格式之一（参见 {@link ImageConstants#getSupportedReadImageFormats()}）。
	 * 该值用于默认初始化输出格式：构造后将把 {@code outputFormat} 设置为此扩展名；在 {@link #reset()} 时也会用它恢复输出格式。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected final String inputFormat;

	/**
	 * 处理后的输出图像
	 * <p>
	 * 存储经过缩放或其他处理后的图像数据，初始值为输入图像。
	 * 在调用各种处理方法后会被更新。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected BufferedImage outputImage;

	/**
	 * 输出图像尺寸
	 * <p>
	 * 存储当前输出图像的尺寸信息。
	 * 在调用缩放相关方法后会被更新。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected ImageSize outputImageSize;

	/**
	 * 输出图像格式
	 * <p>
	 * 指定输出图像的格式（如"png"、"jpeg"等）。
	 * 默认根据输入图像是否有透明通道自动选择（有透明通道用PNG，无透明通道用JPEG）。
	 * 可通过{@link #outputFormat(String)}方法修改。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	protected String outputFormat;

	/**
	 * 构造实例并初始化以下属性：
	 * <ul>
	 *   <li><b>输入/输出图像：</b> 初始时输出图像为输入图像的副本。<b>注意：</b>
	 *   若存在 EXIF 方向信息且方向不为正常值，会调用 {@link ImageUtils#correctOrientation}，输出图像可能会被旋转或翻转。</li>
	 *   <li><b>图像尺寸：</b> 记录输入图像的<b>可视化尺寸</b>（即 {@link ImageSize#getVisualSize()}，若存在 90°/270° 旋转，宽高会自动交换）。</li>
	 *   <li><b>输出格式：</b> 根据输入图像是否包含 Alpha 通道（透明度）自动设置默认值：
	 *     <ul>
	 *       <li>含透明度：默认为 PNG ({@link #DEFAULT_ALPHA_OUTPUT_FORMAT})。</li>
	 *       <li>无透明度：默认为 JPG ({@link #DEFAULT_OUTPUT_FORMAT})。</li>
	 *     </ul>
	 *   </li>
	 * </ul>
	 *
	 * @param inputImage     原始图像数据（BufferedImage），不可为 null
	 * @param inputImageSize 原始图像尺寸对象（包含宽、高及方向信息），不可为 null
	 * @throws NullPointerException 当 inputImage 或 inputImageSize 为 null 时抛出
	 * @since 2.1.0
	 */
	protected ImageProcessor(final BufferedImage inputImage, final ImageSize inputImageSize) {
		this(inputImage, inputImageSize, null);
	}

	/**
	 * 构造实例并初始化以下属性。
	 * <p>
	 * 与 {@link #ImageProcessor(BufferedImage, ImageSize)} 相比，此构造方法允许明确指定输入格式。
	 * 如果指定了 {@code inputFormat}，则输出格式也会直接使用该输入格式，而不根据图像是否含 Alpha 通道自动选择。
	 * 如果 {@code inputFormat} 为 null 或空白，则根据图像是否含 Alpha 通道自动选择默认格式。
	 * </p>
	 * <ul>
	 *   <li><b>输入/输出图像：</b> 初始时输出图像为输入图像的副本。<b>注意：</b>
	 *       若存在 EXIF 方向信息且方向不为正常值，会调用 {@link ImageUtils#correctOrientation}，输出图像可能会被旋转或翻转。</li>
	 *   <li><b>图像尺寸：</b> 记录输入图像的<b>可视化尺寸</b>（即 {@link ImageSize#getVisualSize()}，若存在 90°/270° 旋转，宽高会自动交换）。</li>
	 *   <li><b>输入/输出格式：</b> 如果 {@code inputFormat} 不为 null 或空白，则使用该格式；否则根据图像是否含 Alpha 通道自动选择。</li>
	 * </ul>
	 *
	 * @param inputImage     原始图像数据（BufferedImage），不可为 null
	 * @param inputImageSize 原始图像尺寸对象（包含宽、高及方向信息），不可为 null
	 * @param inputFormat    输入图像格式（如 "PNG"、"JPG"），可为 null 或空白
	 * @throws NullPointerException 当 inputImage 或 inputImageSize 为 null 时抛出
	 * @since 2.1.0
	 */
	protected ImageProcessor(final BufferedImage inputImage, final ImageSize inputImageSize, final String inputFormat) {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.notNull(inputImageSize, "inputImageSize 不可为 null");

		this.inputImage = inputImage;
		this.inputImageSize = inputImageSize;
		this.inputFormat = StringUtils.defaultIfBlank(inputFormat, null);

		this.outputImage = ImageUtil.createCopy(inputImage);
		if (!this.inputImageSize.isNormalOrientation()) {
			this.outputImage = ImageUtils.correctOrientation(this.outputImage, this.inputImageSize.getOrientation());
		}

		this.outputImageSize = inputImageSize.getVisualSize();

		this.outputFormat = StringUtils.defaultIfBlank(inputFormat,
			outputImage.getColorModel().hasAlpha() ? DEFAULT_ALPHA_OUTPUT_FORMAT : DEFAULT_OUTPUT_FORMAT);
	}

	/**
	 * 从输入流构建实例（使用默认 EXIF 方向）。
	 * <p>
	 * 此方法使用默认的 EXIF 方向值（正常方向）构建图像编辑器，不进行 EXIF 方向校正。
	 * </p>
	 *
	 * @param inputStream 输入流，不可为 null
	 * @return 图像编辑器实例
	 * @throws IOException 当读取图像失败时抛出
	 * @see #of(InputStream, int)
	 * @since 2.1.0
	 */
	public static ImageProcessor of(final InputStream inputStream) throws IOException {
		return of(inputStream, ImageConstants.NORMAL_EXIF_ORIENTATION);
	}

	/**
	 * 从输入流构建实例（手动指定 EXIF 方向）。
	 * <p>
	 * 适用于已知图像 EXIF 方向的场景。流仅会被读取一次，无需缓存或重置，性能最优。
	 * </p>
	 *
	 * @param inputStream     包含图像数据的输入流，不可为 null
	 * @param exifOrientation EXIF 方向值（1-8），用于校正图像
	 * @return 图像编辑器实例
	 * @throws IOException              当读取输入流出错时
	 * @throws NullPointerException     当 inputStream 为 null 时抛出
	 * @throws IllegalArgumentException 当 exifOrientation 不在1-8范围内时抛出
	 * @since 2.1.0
	 */
	public static ImageProcessor of(final InputStream inputStream, final int exifOrientation) throws IOException {
		Validate.notNull(inputStream, "inputStream不可为 null");
		Validate.inclusiveBetween(1, 8, exifOrientation, "exifOrientation 必须介于1-8之间");

		BufferedImage bufferedImage = ImageIO.read(inputStream);
		if (Objects.isNull(bufferedImage)) {
			throw new IOException("图片读取失败");
		}
		ImageSize imageSize = new ImageSize(bufferedImage.getWidth(), bufferedImage.getHeight(), exifOrientation);
		return new ImageProcessor(bufferedImage, imageSize);
	}

	/**
	 * 从 {@link ImageIOResource} 构建实例（推荐方式）。
	 * <p>
	 * 这是构建 {@link ImageProcessor} 实例的推荐方式，提供统一的资源管理和更好的性能。
	 * {@link ImageIOResource} 封装了图像数据、尺寸信息、EXIF 方向和格式等完整信息。
	 * </p>
	 * <p>
	 * <b>初始化行为：</b></p>
	 * <ul>
	 *   <li><b>图像数据：</b> 使用 ImageIOResource 缓存的 BufferedImage。如果 ImageIOResource 在构造时启用了 EXIF 方向校正，
	 *       则此图像为校正后的图像，无需再次校正。</li>
	 *   <li><b>图像尺寸：</b> 使用 ImageIOResource 缓存的 ImageSize。如果启用了方向校正，则为校正后的尺寸。</li>
	 *   <li><b>输出格式：</b> 使用 ImageIOResource 的格式信息（如果存在），否则根据图像是否含 Alpha 通道自动选择。</li>
	 * </ul>
	 *
	 * @param resource 图像 IO 资源对象，包含图像数据、尺寸、EXIF 方向和格式等信息，不可为 null
	 * @return 图像编辑器实例
	 * @throws IOException          当读取图像数据失败时抛出
	 * @throws NullPointerException 当 resource 为 null 时抛出
	 * @since 2.1.0
	 */
	public static ImageProcessor of(final ImageIOResource resource) throws IOException {
		Validate.notNull(resource, "resource不可为 null");

		ImageSize imageSize = resource.getImageSize();
		if (resource.isOrientationCorrected()) {
			imageSize = new ImageSize(imageSize.getWidth(), imageSize.getHeight());
		}
		return new ImageProcessor(resource.getBufferedImage(), imageSize, resource.getFormat());
	}

	/**
	 * 从 {@link ImageInputStream} 构建实例。
	 * <p>
	 * <b>注意：</b> {@link ImageInputStream} 不直接提供 EXIF 元数据，因此无法自动进行基于 EXIF 的视觉方向校正。
	 * 适用于不需要矫正图像视觉方向，或方向信息未知的场景。
	 * </p>
	 *
	 * @param imageInputStream 图像输入流，不可为 null
	 * @return 图像编辑器实例
	 * @throws NullPointerException 当 imageInputStream 为 null 时抛出
	 * @throws IOException          当读取图像失败时抛出
	 * @since 2.1.0
	 */
	public static ImageProcessor of(final ImageInputStream imageInputStream) throws IOException {
		Validate.notNull(imageInputStream, "imageInputStream不可为 null");

		BufferedImage bufferedImage = ImageIO.read(imageInputStream);
		if (Objects.isNull(bufferedImage)) {
			throw new IOException("图片读取失败");
		}
		ImageSize imageSize = new ImageSize(bufferedImage.getWidth(), bufferedImage.getHeight());
		return new ImageProcessor(bufferedImage, imageSize);
	}

	/**
	 * 从 {@link ImageInputStream} 构建实例（手动指定 EXIF 方向）。
	 * <p>
	 * 读取图像数据，并应用指定的 EXIF 方向进行视觉方向校正。
	 * 适用于已从外部获取了 EXIF 方向信息（例如数据库、文件名或独立元数据读取器）的场景。
	 * </p>
	 *
	 * @param imageInputStream 图像输入流，不可为 null
	 * @param exifOrientation  外部获取的 EXIF 方向值（1-8），用于校正图像
	 * @return 图像编辑器实例
	 * @throws NullPointerException     当 imageInputStream 为 null 时抛出
	 * @throws IllegalArgumentException 当 exifOrientation 不在1-8范围内时抛出
	 * @throws IOException              当读取图像失败时抛出
	 * @since 2.1.0
	 */
	public static ImageProcessor of(final ImageInputStream imageInputStream, final int exifOrientation) throws IOException {
		Validate.inclusiveBetween(1, 8, exifOrientation, "exifOrientation 必须介于1-8之间");
		Validate.notNull(imageInputStream, "imageInputStream不可为 null");

		BufferedImage bufferedImage = ImageIO.read(imageInputStream);
		if (Objects.isNull(bufferedImage)) {
			throw new IOException("图片读取失败");
		}
		ImageSize imageSize = new ImageSize(bufferedImage.getWidth(), bufferedImage.getHeight(), exifOrientation);
		return new ImageProcessor(bufferedImage, imageSize);
	}

	/**
	 * 从 {@link BufferedImage} 构建实例。
	 * <p>
	 * <b>注意：</b> {@link BufferedImage} 仅包含像素数据，不包含 EXIF 元数据，因此无法自动进行基于 EXIF 的视觉方向校正。
	 * 适用于图像已在内存中且不需要方向校正的场景（如生成的图像、已处理过的图像）。
	 * </p>
	 *
	 * @param bufferedImage BufferedImage 对象，不可为 null
	 * @return 图像编辑器实例
	 * @throws NullPointerException 当 bufferedImage 为 null 时抛出
	 * @since 2.1.0
	 */
	public static ImageProcessor of(final BufferedImage bufferedImage) {
		Validate.notNull(bufferedImage, "bufferedImage不可为 null");

		ImageSize imageSize = new ImageSize(bufferedImage.getWidth(), bufferedImage.getHeight());
		return new ImageProcessor(bufferedImage, imageSize);
	}

	/**
	 * 从 {@link BufferedImage} 构建实例（手动指定 EXIF 方向）。
	 * <p>
	 * 使用内存中的 {@link BufferedImage}，并应用指定的 EXIF 方向进行视觉方向校正。
	 * 适用于图像数据已在内存中，且方向信息已知（例如来自上传请求参数、数据库）的场景。
	 * </p>
	 *
	 * @param bufferedImage   BufferedImage 对象，不可为 null
	 * @param exifOrientation 外部获取的 EXIF 方向值（1-8），用于校正图像
	 * @return 图像编辑器实例
	 * @throws NullPointerException     当 bufferedImage 为 null 时抛出
	 * @throws IllegalArgumentException 当 exifOrientation 不在1-8范围内时抛出
	 * @since 2.1.0
	 */
	public static ImageProcessor of(final BufferedImage bufferedImage, final int exifOrientation) {
		Validate.notNull(bufferedImage, "bufferedImage不可为 null");
		Validate.inclusiveBetween(1, 8, exifOrientation, "exifOrientation 必须介于1-8之间");

		ImageSize imageSize = new ImageSize(bufferedImage.getWidth(), bufferedImage.getHeight(), exifOrientation);
		return new ImageProcessor(bufferedImage, imageSize);
	}

	/**
	 * 调整图像整体透明度。
	 * <p>
	 * 该方法会为整个图像应用统一的透明度值，可以用于创建半透明效果、水印叠加前的准备等场景。
	 * </p>
	 * <p><b>取值说明</b>：
	 * <ul>
	 *   <li>{@code opacity = 0.0}：完全透明（图像不可见）</li>
	 *   <li>{@code opacity = 0.5}：半透明（50% 透明度）</li>
	 *   <li>{@code opacity = 1.0}：完全不透明（原图保持不变）</li>
	 * </ul>
	 * </p>
	 *
	 * @param opacity 透明度值，范围 0.0（完全透明）到 1.0（完全不透明）
	 * @return 当前编辑器实例，用于链式调用
	 * @throws IllegalArgumentException 当 opacity 超出 [0.0, 1.0] 范围时抛出
	 * @since 2.1.0
	 */
	public ImageProcessor transparency(final float opacity) {
		Validate.isTrue(opacity >= 0 && opacity <= 1, "opacity 必须大于等于 0 且小于等于 1");

		this.outputImage = new Transparency(opacity).apply(this.outputImage);
		return this;
	}

	/**
	 * 按指定方向旋转图像。
	 *
	 * @param direction 旋转方向
	 * @return 当前编辑器实例，用于链式调用
	 * @since 2.1.0
	 */
	public ImageProcessor rotate(final RotateDirection direction) {
		Validate.notNull(direction, "direction 不可为 null");

		this.outputImage = ImageUtil.createRotated(this.outputImage, direction.getRadians());
		return this;
	}

	/**
	 * 按指定角度旋转图像。
	 *
	 * @param angle 旋转角度（单位：度），正值表示顺时针旋转
	 * @return 当前编辑器实例，用于链式调用
	 * @since 2.1.0
	 */
	public ImageProcessor rotate(final double angle) {
		this.outputImage = ImageUtil.createRotated(this.outputImage, Math.toRadians(angle));
		return this;
	}

	/**
	 * 对图像应用模糊效果，使用默认模糊半径1.5。
	 *
	 * <p><b>效果说明</b>：使用高斯模糊核，半径越大越模糊；1.5 像素提供轻微柔化，适合降噪或 UI 图标处理。</p>
	 *
	 * @return 当前编辑器实例，用于链式调用
	 * @since 2.1.0
	 */
	public ImageProcessor blur() {
		this.outputImage = ImageUtil.blur(this.outputImage, 1.5f);
		return this;
	}

	/**
	 * 对图像应用模糊效果，使用指定的模糊半径。
	 *
	 * <p><b>取值建议</b>：
	 * <ul>
	 *   <li>{@code radius ∈ (1, 2]}：轻微模糊，适用于轻微柔化或抗锯齿；</li>
	 *   <li>{@code radius ∈ (2, 4]}：中等模糊，适合背景虚化或隐私遮挡；</li>
	 *   <li>{@code radius > 4}：强烈模糊，计算开销显著增加，且可能显得“涂抹”；</li>
	 *   <li>注意：实际模糊核大小 ≈ {@code 2 * radius + 1}，过大会导致性能下降；</li>
	 *   <li>推荐范围：{@code 1.5 ~ 3.0}。</li>
	 * </ul></p>
	 *
	 * @param radius 模糊半径，值越大模糊效果越强
	 * @return 当前编辑器实例，用于链式调用
	 * @since 2.1.0
	 */
	public ImageProcessor blur(final float radius) {
		this.outputImage = ImageUtil.blur(this.outputImage, radius);
		return this;
	}

	/**
	 * 翻转图像。
	 *
	 * @param direction 翻转方向
	 * @return 当前编辑器实例，用于链式调用
	 * @since 2.1.0
	 */
	public ImageProcessor flip(final FlipDirection direction) {
		Validate.notNull(direction, "direction 不可为 null");

		this.outputImage = ImageUtil.createFlipped(this.outputImage, direction.getAxis());
		return this;
	}

	/**
	 * 对图像应用锐化效果，使用默认锐化强度0.3。
	 *
	 * <p><b>效果说明</b>：基于非锐化掩模（Unsharp Mask）原理，0.3 提供自然清晰度提升，无明显光晕。</p>
	 *
	 * @return 当前编辑器实例，用于链式调用
	 * @since 2.1.0
	 */
	public ImageProcessor sharpen() {
		this.outputImage = ImageUtil.sharpen(this.outputImage, 0.3f);
		return this;
	}

	/**
	 * 对图像应用锐化效果，使用指定的锐化强度。
	 *
	 * <p><b>取值建议</b>：
	 * <ul>
	 *   <li>{@code amount ∈ (0, 0.5]}：安全锐化，提升细节而不引入伪影；</li>
	 *   <li>{@code amount ∈ (0.5, 1.0]}：较强锐化，边缘可能出现轻微白边（halo）；</li>
	 *   <li>{@code amount > 1.0}：过度锐化，放大噪点，图像失真；</li>
	 *   <li>负值表示“反向锐化”（即额外模糊），但通常不推荐；</li>
	 *   <li>推荐范围：{@code 0.2 ~ 0.6}。</li>
	 * </ul></p>
	 *
	 * @param amount 锐化强度
	 * @return 当前编辑器实例，用于链式调用
	 * @since 2.1.0
	 */
	public ImageProcessor sharpen(final float amount) {
		this.outputImage = ImageUtil.sharpen(this.outputImage, amount);
		return this;
	}

	/**
	 * 将图像转换为灰度图。
	 *
	 * @return 当前编辑器实例，用于链式调用
	 * @since 2.1.0
	 */
	public ImageProcessor grayscale() {
		Image image = ImageUtil.filter(this.outputImage, new GrayFilter());
		this.outputImage = ImageUtil.toBuffered(image);
		return this;
	}

	/**
	 * 调整图像对比度，使用默认对比度值0.3。
	 *
	 * <p><b>效果说明</b>：0.3 表示对比度提升约 30%，使明暗更分明，适用于灰蒙图像。</p>
	 *
	 * @return 当前编辑器实例，用于链式调用
	 * @since 2.1.0
	 */
	public ImageProcessor contrast() {
		Image image = ImageUtil.filter(this.outputImage, new BrightnessContrastFilter(0, 0.3f));
		this.outputImage = ImageUtil.toBuffered(image);
		return this;
	}

	/**
	 * 调整图像对比度。
	 *
	 * <p><b>取值建议</b>：
	 * <ul>
	 *   <li>{@code amount ∈ (0, 0.5]}：适度增强对比度，自然观感；</li>
	 *   <li>{@code amount ∈ (0.5, 1.0]}：高对比度，适合艺术效果或低动态范围图像；</li>
	 *   <li>{@code amount ∈ [-0.5, 0)}：降低对比度，营造“朦胧”或“褪色”风格；</li>
	 *   <li>{@code amount = -1}：完全去对比度（灰度均一化）；</li>
	 *   <li>避免极端值（如 ±1），可能导致细节丢失；</li>
	 *   <li>推荐范围：{@code -0.3 ~ 0.6}。</li>
	 * </ul></p>
	 *
	 * @param amount 对比度调整值，范围为-1.0到1.0，0表示不变，正值增加对比度，负值降低对比度
	 * @return 当前编辑器实例，用于链式调用
	 * @since 2.1.0
	 */
	public ImageProcessor contrast(final float amount) {
		if (amount == 0f || amount > 1.0 || amount < -1.0) {
			return this;
		}

		BrightnessContrastFilter filter = new BrightnessContrastFilter(0f, amount);
		Image image = ImageUtil.filter(this.outputImage, filter);
		this.outputImage = ImageUtil.toBuffered(image);
		return this;
	}

	/**
	 * 调整图像亮度。
	 *
	 * <p><b>取值建议</b>：
	 * <ul>
	 *   <li>{@code amount ∈ (0, 1]}：提亮图像，1.0 表示亮度翻倍（可能过曝）；</li>
	 *   <li>{@code amount ∈ [-1, 0)}：降低亮度，-1.0 表示完全变黑；</li>
	 *   <li>{@code amount > 1}：极度提亮，高光区域严重溢出（纯白）；</li>
	 *   <li>{@code amount < -1}：极度压暗，阴影细节完全丢失；</li>
	 *   <li>日常调整建议：{@code -0.5 ~ 0.8}；</li>
	 *   <li>注意：亮度调整是线性加法（RGB += amount），非感知均匀。</li>
	 * </ul></p>
	 *
	 * @param amount 亮度调整值，范围为-2.0到2.0，0表示不变，正值增加亮度，负值降低亮度
	 * @return 当前编辑器实例，用于链式调用
	 * @since 2.1.0
	 */
	public ImageProcessor brightness(final float amount) {
		if (amount == 0f || amount > 2.0 || amount < -2.0) {
			return this;
		}

		BrightnessContrastFilter filter = new BrightnessContrastFilter(amount, 0f);
		Image image = ImageUtil.filter(this.outputImage, filter);
		this.outputImage = ImageUtil.toBuffered(image);
		return this;
	}

	/**
	 * 对图像应用自定义过滤器。
	 *
	 * @param filter 要应用的图像过滤器
	 * @return 当前编辑器实例，用于链式调用
	 * @throws NullPointerException 当过滤器为null时
	 * @since 2.1.0
	 */
	public ImageProcessor filter(final ImageFilter filter) {
		Validate.notNull(filter, "filter不可为 null");

		Image image = ImageUtil.filter(this.outputImage, filter);
		this.outputImage = ImageUtil.toBuffered(image);
		return this;
	}

	/**
	 * 强制将图像缩放到指定的宽度和高度，不保持原始宽高比。
	 *
	 * @param width  目标宽度（像素）
	 * @param height 目标高度（像素）
	 * @return 当前编辑器实例，用于链式调用
	 * @since 2.1.0
	 */
	public ImageProcessor resize(final int width, final int height) {
		return resize(width, height, ResampleOp.FILTER_LANCZOS);
	}

	/**
	 * 按指定宽度等比例缩放图像，保持原始宽高比。
	 *
	 * @param width              目标宽度（像素）
	 * @param height             目标高度（像素）
	 * @param resampleFilterType 插值滤波算法
	 * @return 当前编辑器实例，用于链式调用
	 * @see ResampleOp#FILTER_POINT
	 * @see ResampleOp#FILTER_BOX
	 * @see ResampleOp#FILTER_TRIANGLE
	 * @see ResampleOp#FILTER_HERMITE
	 * @see ResampleOp#FILTER_HANNING
	 * @see ResampleOp#FILTER_HAMMING
	 * @see ResampleOp#FILTER_BLACKMAN
	 * @see ResampleOp#FILTER_GAUSSIAN
	 * @see ResampleOp#FILTER_QUADRATIC
	 * @see ResampleOp#FILTER_CUBIC
	 * @see ResampleOp#FILTER_CATROM
	 * @see ResampleOp#FILTER_MITCHELL
	 * @see ResampleOp#FILTER_LANCZOS
	 * @see ResampleOp#FILTER_BLACKMAN_BESSEL
	 * @see ResampleOp#FILTER_BLACKMAN_SINC
	 * @since 2.1.0
	 */
	public ImageProcessor resize(final int width, final int height, final int resampleFilterType) {
		Validate.isTrue(resampleFilterType >= 0 && resampleFilterType <= 15, "resampleFilterType 取值范围在0-15之间");

		this.outputImageSize = this.outputImageSize.resize(width, height);
		this.outputImage = new ResampleOp(outputImageSize.getWidth(), outputImageSize.getHeight(), resampleFilterType)
			.filter(outputImage, null);
		return this;
	}

	/**
	 * 按指定宽度等比例缩放图像，保持原始宽高比。
	 *
	 * @param targetWidth 目标宽度（像素）
	 * @return 当前编辑器实例，用于链式调用
	 * @since 2.1.0
	 */
	public ImageProcessor scaleByWidth(final int targetWidth) {
		return scaleByWidth(targetWidth, ResampleOp.FILTER_LANCZOS);
	}

	/**
	 * 按指定宽度等比例缩放图像，保持原始宽高比。
	 *
	 * @param targetWidth        目标宽度（像素）
	 * @param resampleFilterType 插值滤波算法
	 * @return 当前编辑器实例，用于链式调用
	 * @see ResampleOp#FILTER_POINT
	 * @see ResampleOp#FILTER_BOX
	 * @see ResampleOp#FILTER_TRIANGLE
	 * @see ResampleOp#FILTER_HERMITE
	 * @see ResampleOp#FILTER_HANNING
	 * @see ResampleOp#FILTER_HAMMING
	 * @see ResampleOp#FILTER_BLACKMAN
	 * @see ResampleOp#FILTER_GAUSSIAN
	 * @see ResampleOp#FILTER_QUADRATIC
	 * @see ResampleOp#FILTER_CUBIC
	 * @see ResampleOp#FILTER_CATROM
	 * @see ResampleOp#FILTER_MITCHELL
	 * @see ResampleOp#FILTER_LANCZOS
	 * @see ResampleOp#FILTER_BLACKMAN_BESSEL
	 * @see ResampleOp#FILTER_BLACKMAN_SINC
	 * @since 2.1.0
	 */
	public ImageProcessor scaleByWidth(final int targetWidth, final int resampleFilterType) {
		Validate.isTrue(resampleFilterType >= 0 && resampleFilterType <= 15, "resampleFilterType 取值范围在0-15之间");

		this.outputImageSize = this.outputImageSize.scaleByWidth(targetWidth);
		this.outputImage = new ResampleOp(outputImageSize.getWidth(), outputImageSize.getHeight(), resampleFilterType)
			.filter(outputImage, null);
		return this;
	}

	/**
	 * 按指定高度等比例缩放图像，保持原始宽高比。
	 *
	 * @param targetHeight 目标高度（像素）
	 * @return 当前编辑器实例，用于链式调用
	 * @since 2.1.0
	 */
	public ImageProcessor scaleByHeight(final int targetHeight) {
		return scaleByHeight(targetHeight, ResampleOp.FILTER_LANCZOS);
	}

	/**
	 * 按指定高度等比例缩放图像，保持原始宽高比。
	 *
	 * @param targetHeight       目标高度（像素）
	 * @param resampleFilterType 插值滤波算法
	 * @return 当前编辑器实例，用于链式调用
	 * @see ResampleOp#FILTER_POINT
	 * @see ResampleOp#FILTER_BOX
	 * @see ResampleOp#FILTER_TRIANGLE
	 * @see ResampleOp#FILTER_HERMITE
	 * @see ResampleOp#FILTER_HANNING
	 * @see ResampleOp#FILTER_HAMMING
	 * @see ResampleOp#FILTER_BLACKMAN
	 * @see ResampleOp#FILTER_GAUSSIAN
	 * @see ResampleOp#FILTER_QUADRATIC
	 * @see ResampleOp#FILTER_CUBIC
	 * @see ResampleOp#FILTER_CATROM
	 * @see ResampleOp#FILTER_MITCHELL
	 * @see ResampleOp#FILTER_LANCZOS
	 * @see ResampleOp#FILTER_BLACKMAN_BESSEL
	 * @see ResampleOp#FILTER_BLACKMAN_SINC
	 * @since 2.1.0
	 */
	public ImageProcessor scaleByHeight(final int targetHeight, final int resampleFilterType) {
		Validate.isTrue(resampleFilterType >= 0 && resampleFilterType <= 15, "resampleFilterType 取值范围在0-15之间");

		this.outputImageSize = this.outputImageSize.scaleByHeight(targetHeight);
		this.outputImage = new ResampleOp(outputImageSize.getWidth(), outputImageSize.getHeight(), resampleFilterType)
			.filter(outputImage, null);
		return this;
	}

	/**
	 * 将图像缩放到指定的比例，保持原始宽高比。
	 *
	 * @param scalingFactor 缩放比例
	 * @return 当前编辑器实例，用于链式调用
	 * @since 2.1.0
	 */
	public ImageProcessor scale(final double scalingFactor) {
		return scale(scalingFactor, ResampleOp.FILTER_LANCZOS);
	}

	/**
	 * 将图像缩放到指定的比例，保持原始宽高比。
	 *
	 * @param scalingFactor      缩放比例
	 * @param resampleFilterType 插值滤波算法
	 * @return 当前编辑器实例，用于链式调用
	 * @see ResampleOp#FILTER_POINT
	 * @see ResampleOp#FILTER_BOX
	 * @see ResampleOp#FILTER_TRIANGLE
	 * @see ResampleOp#FILTER_HERMITE
	 * @see ResampleOp#FILTER_HANNING
	 * @see ResampleOp#FILTER_HAMMING
	 * @see ResampleOp#FILTER_BLACKMAN
	 * @see ResampleOp#FILTER_GAUSSIAN
	 * @see ResampleOp#FILTER_QUADRATIC
	 * @see ResampleOp#FILTER_CUBIC
	 * @see ResampleOp#FILTER_CATROM
	 * @see ResampleOp#FILTER_MITCHELL
	 * @see ResampleOp#FILTER_LANCZOS
	 * @see ResampleOp#FILTER_BLACKMAN_BESSEL
	 * @see ResampleOp#FILTER_BLACKMAN_SINC
	 * @since 2.1.0
	 */
	public ImageProcessor scale(final double scalingFactor, final int resampleFilterType) {
		Validate.isTrue(resampleFilterType >= 0 && resampleFilterType <= 15, "resampleFilterType 取值范围在0-15之间");

		this.outputImageSize = this.outputImageSize.scale(scalingFactor);
		this.outputImage = new ResampleOp(outputImageSize.getWidth(), outputImageSize.getHeight(), resampleFilterType)
			.filter(outputImage, null);
		return this;
	}

	/**
	 * 双约束等比缩放（基于目标宽高值）
	 * <p>
	 * 在不超过目标宽高的前提下保持宽高比：
	 * <ol>
	 *   <li>优先适配宽度计算</li>
	 *   <li>若高度超出则改为适配高度</li>
	 * </ol>
	 * </p>
	 *
	 * @param targetWidth  目标宽度（像素）
	 * @param targetHeight 目标高度（像素）
	 * @return 当前编辑器实例，用于链式调用
	 * @since 2.1.0
	 */
	public ImageProcessor scale(final int targetWidth, final int targetHeight) {
		return scale(targetWidth, targetHeight, ResampleOp.FILTER_LANCZOS);
	}

	/**
	 * 双约束等比缩放（基于目标宽高值）
	 * <p>
	 * 在不超过目标宽高的前提下保持宽高比：
	 * <ol>
	 *   <li>优先适配宽度计算</li>
	 *   <li>若高度超出则改为适配高度</li>
	 * </ol>
	 * </p>
	 *
	 * @param targetWidth        目标宽度（像素）
	 * @param targetHeight       目标高度（像素）
	 * @param resampleFilterType 插值滤波算法
	 * @return 当前编辑器实例，用于链式调用
	 * @see ResampleOp#FILTER_POINT
	 * @see ResampleOp#FILTER_BOX
	 * @see ResampleOp#FILTER_TRIANGLE
	 * @see ResampleOp#FILTER_HERMITE
	 * @see ResampleOp#FILTER_HANNING
	 * @see ResampleOp#FILTER_HAMMING
	 * @see ResampleOp#FILTER_BLACKMAN
	 * @see ResampleOp#FILTER_GAUSSIAN
	 * @see ResampleOp#FILTER_QUADRATIC
	 * @see ResampleOp#FILTER_CUBIC
	 * @see ResampleOp#FILTER_CATROM
	 * @see ResampleOp#FILTER_MITCHELL
	 * @see ResampleOp#FILTER_LANCZOS
	 * @see ResampleOp#FILTER_BLACKMAN_BESSEL
	 * @see ResampleOp#FILTER_BLACKMAN_SINC
	 * @since 2.1.0
	 */
	public ImageProcessor scale(final int targetWidth, final int targetHeight, final int resampleFilterType) {
		Validate.isTrue(resampleFilterType >= 0 && resampleFilterType <= 15, "resampleFilterType 取值范围在0-15之间");

		this.outputImageSize = this.outputImageSize.scale(targetWidth, targetHeight);
		this.outputImage = new ResampleOp(outputImageSize.getWidth(), outputImageSize.getHeight(), resampleFilterType)
			.filter(outputImage, null);
		return this;
	}

	/**
	 * 居中裁剪为指定尺寸。
	 * <p>
	 * 以当前输出图像的中心为基准，裁剪出宽度为 {@code width}、高度为 {@code height} 的区域。
	 * 若目标尺寸不小于原图尺寸（任一维度），则不进行裁剪并直接返回。
	 * </p>
	 *
	 * @param width  目标裁剪宽度，必须大于 0
	 * @param height 目标裁剪高度，必须大于 0
	 * @return 当前编辑器实例（便于链式调用）
	 * @throws IllegalArgumentException 当 {@code width} 或 {@code height} 小于等于 0 时抛出
	 * @since 2.1.0
	 */
	public ImageProcessor cropByCenter(int width, int height) {
		Validate.isTrue(width > 0, "width 不能小于0");
		Validate.isTrue(height > 0, "height 不能小于0");

		// 边界检测
		if (width > this.outputImageSize.getWidth() || height > this.outputImageSize.getHeight()) {
			return this;
		}

		// 先计算偏移量，再更新尺寸
		int x = (this.outputImageSize.getWidth() - width) / 2;
		int y = (this.outputImageSize.getHeight() - height) / 2;
		this.outputImageSize = this.outputImageSize.resize(width, height);
		return filter(new CropImageFilter(x, y, width, height));
	}

	/**
	 * 按边距偏移进行裁剪。
	 * <p>
	 * 从当前输出图像的四个边分别去除指定的偏移量，得到新的裁剪区域。
	 * 当任一偏移超出图像尺寸或左右/上下偏移之和超出对应维度时，不进行裁剪并返回。
	 * </p>
	 *
	 * @param topOffset    顶部偏移，必须大于等于 0
	 * @param bottomOffset 底部偏移，必须大于等于 0
	 * @param leftOffset   左侧偏移，必须大于等于 0
	 * @param rightOffset  右侧偏移，必须大于等于 0
	 * @return 当前编辑器实例（便于链式调用）
	 * @throws IllegalArgumentException 当任一偏移为负数时抛出
	 * @since 2.1.0
	 */
	public ImageProcessor cropByOffset(int topOffset, int bottomOffset, int leftOffset, int rightOffset) {
		Validate.isTrue(topOffset >= 0 && bottomOffset >= 0 && leftOffset >= 0 && rightOffset >= 0,
			"offset 不能小于0");

		// 边界检测
		if (leftOffset + rightOffset > this.outputImageSize.getWidth() ||
			topOffset + bottomOffset > this.outputImageSize.getHeight()) {
			return this;
		}

		int width = this.outputImageSize.getWidth() - leftOffset - rightOffset;
		int height = this.outputImageSize.getHeight() - topOffset - bottomOffset;
		this.outputImageSize = this.outputImageSize.resize(width, height);
		return filter(new CropImageFilter(leftOffset, topOffset, width, height));
	}

	/**
	 * 按矩形区域进行裁剪。
	 * <p>
	 * 使用左上角坐标 {@code (x, y)} 与尺寸 {@code (width, height)} 指定裁剪矩形。
	 * 当矩形超出图像边界时，不进行裁剪并返回。
	 * </p>
	 *
	 * @param x      裁剪矩形左上角 X 坐标，必须大于等于 0
	 * @param y      裁剪矩形左上角 Y 坐标，必须大于等于 0
	 * @param width  裁剪宽度，必须大于 0
	 * @param height 裁剪高度，必须大于 0
	 * @return 当前编辑器实例（便于链式调用）
	 * @throws IllegalArgumentException 当 {@code x} 或 {@code y} 为负数，或 {@code width}、{@code height} 小于等于 0 时抛出
	 * @since 2.1.0
	 */
	public ImageProcessor cropByRect(int x, int y, int width, int height) {
		Validate.isTrue(x >= 0, "x 不能小于0");
		Validate.isTrue(y >= 0, "y 不能小于0");
		Validate.isTrue(width > 0, "width 不能小于0");
		Validate.isTrue(height > 0, "height 不能小于0");

		// 边界检测
		if (x + width > this.outputImageSize.getWidth() || y + height > this.outputImageSize.getHeight()) {
			return this;
		}

		this.outputImageSize = this.outputImageSize.resize(width, height);
		return filter(new CropImageFilter(x, y, width, height));
	}

	/**
	 * 添加图片水印，使用默认水印配置。
	 * <p>
	 * 该方法会使用 {@link ImageWatermarkOption} 的默认配置，自动计算水印尺寸、位置等参数，
	 * 然后将水印应用到当前图像上。
	 * </p>
	 *
	 * @param watermarkImage 水印图片，不可为 null
	 * @return 当前编辑器实例，用于链式调用
	 * @see ImageWatermarkOption
	 * @since 2.1.0
	 */
	public ImageProcessor addImageWatermark(final BufferedImage watermarkImage) {
		this.outputImage = new ImageWatermarkOption().toWatermark(this.outputImageSize, watermarkImage)
			.apply(this.outputImage);
		return this;
	}

	/**
	 * 添加图片水印，使用指定的水印配置选项。
	 * <p>
	 * 该方法会根据 {@link ImageWatermarkOption} 中的配置，自动计算水印尺寸、位置等参数，
	 * 然后将水印应用到当前图像上。
	 * </p>
	 *
	 * @param watermarkImage 水印图片，不可为 null
	 * @param option         水印配置选项，包含缩放比例、透明度、位置方向、尺寸限制等，不可为 null
	 * @return 当前编辑器实例，用于链式调用
	 * @throws IllegalArgumentException 当 watermarkImage 或 option 为 null 时抛出
	 * @see ImageWatermarkOption
	 * @since 2.1.0
	 */
	public ImageProcessor addImageWatermark(final BufferedImage watermarkImage, final ImageWatermarkOption option) {
		Validate.notNull(option, "option 不可为 null");

		this.outputImage = option.toWatermark(this.outputImageSize, watermarkImage).apply(this.outputImage);
		return this;
	}

	/**
	 * 添加图片水印，直接使用预创建的 Watermark 对象。
	 * <p>
	 * 适用于需要对水印进行精确控制，或者需要复用同一水印配置的场景。
	 * </p>
	 *
	 * @param watermark 预创建的 Watermark 对象，不可为 null
	 * @return 当前编辑器实例，用于链式调用
	 * @throws IllegalArgumentException 当 watermark 为 null 时抛出
	 * @see Watermark
	 * @since 2.1.0
	 */
	public ImageProcessor addImageWatermark(final Watermark watermark) {
		Validate.notNull(watermark, "watermark 不可为 null");

		this.outputImage = watermark.apply(this.outputImage);
		return this;
	}

	/**
	 * 添加文字水印，使用默认水印配置。
	 * <p>
	 * 该方法会使用 {@link TextWatermarkOption} 的默认配置，自动计算文字大小、位置、颜色等参数，
	 * 然后将文字水印应用到当前图像上。
	 * </p>
	 *
	 * @param watermarkText 水印文字内容，不可为空字符串
	 * @return 当前编辑器实例，用于链式调用
	 * @see TextWatermarkOption
	 * @since 2.1.0
	 */
	public ImageProcessor addTextWatermark(final String watermarkText) {
		this.outputImage = new TextWatermarkOption().toCaption(watermarkText, this.outputImage)
			.apply(this.outputImage);
		return this;
	}

	/**
	 * 添加文字水印，使用指定的水印配置选项。
	 * <p>
	 * 该方法会根据 {@link TextWatermarkOption} 中的配置，自动计算文字大小、位置、颜色等参数，
	 * 然后将文字水印应用到当前图像上。
	 * </p>
	 *
	 * @param watermarkText 水印文字内容，不可为空字符串
	 * @param option        文字水印配置选项，包含字体、大小、颜色、透明度、位置方向等，不可为 null
	 * @return 当前编辑器实例，用于链式调用
	 * @throws IllegalArgumentException 当 watermarkText 为空或 option 为 null 时抛出
	 * @see TextWatermarkOption
	 * @since 2.1.0
	 */
	public ImageProcessor addTextWatermark(final String watermarkText, final TextWatermarkOption option) {
		Validate.notNull(option, "option 不可为 null");

		this.outputImage = option.toCaption(watermarkText, this.outputImage).apply(this.outputImage);
		return this;
	}

	/**
	 * 添加文字水印，直接使用预创建的 Caption 对象。
	 * <p>
	 * 适用于需要对文字水印进行精确控制，或者需要复用同一文字水印配置的场景。
	 * </p>
	 *
	 * @param caption 预创建的 Caption 对象，不可为 null
	 * @return 当前编辑器实例，用于链式调用
	 * @throws IllegalArgumentException 当 caption 为 null 时抛出
	 * @see Caption
	 * @since 2.1.0
	 */
	public ImageProcessor addTextWatermark(final Caption caption) {
		Validate.notNull(caption, "caption 不可为 null");

		this.outputImage = caption.apply(this.outputImage);
		return this;
	}

	/**
	 * 应用自定义图像操作。
	 * <p>
	 * 允许传入任意的图像转换函数，对当前图像进行处理。
	 * 函数接收当前图像，返回处理后的图像。
	 * 处理后会自动更新输出图像的尺寸信息。
	 * </p>
	 *
	 * @param operation 图像操作函数，接收当前图像，返回处理后的图像
	 * @return 当前编辑器实例，用于链式调用
	 * @throws NullPointerException 当 operation 为 null 时抛出
	 * @since 2.1.0
	 */
	public ImageProcessor apply(final Function<BufferedImage, BufferedImage> operation) {
		Validate.notNull(operation, "operation 不可为 null");

		this.outputImage = operation.apply(this.outputImage);
		this.outputImageSize = this.outputImageSize.resize(this.outputImage.getWidth(), this.outputImage.getHeight());

		return this;
	}

	/**
	 * 设置输出图像的格式（不区分大小写）。
	 *
	 * <p>指定调用 {@link #toFile(File)}、{@link #toOutputStream(OutputStream)} 或
	 * {@link #toImageOutputStream(ImageOutputStream)}时使用的图像编码格式。</p>
	 *
	 * <p><b>常见支持格式：</b></p>
	 * <ul>
	 *   <li><b>JPEG / JPG：</b> 适合照片，有损压缩，体积小。</li>
	 *   <li><b>PNG：</b> 适合图标/截图，无损压缩，支持透明度。</li>
	 *   <li><b>BMP：</b> 无压缩位图，体积大，解析快。</li>
	 * </ul>
	 * <p>完整支持列表请参考 {@link ImageConstants#getSupportedWriteImageFormats()}。</p>
	 *
	 * @param outputFormat 输出格式字符串（如 "jpg", "PNG"），不可为空
	 * @return 当前编辑器实例，用于链式调用
	 * @throws NullPointerException     当 outputFormat 为 null 时抛出
	 * @throws IllegalArgumentException 当 outputFormat 为空字符串或不支持该格式时抛出
	 * @see ImageConstants#getSupportedWriteImageFormats()
	 * @since 2.1.0
	 */
	public ImageProcessor outputFormat(final String outputFormat) {
		Validate.notBlank(outputFormat, "输出格式不可为空");
		String upperCaseOutputFormat = outputFormat.toUpperCase();
		Validate.isTrue(ImageConstants.getSupportedWriteImageFormats().contains(upperCaseOutputFormat),
			"不支持输出 " + outputFormat + " 图像格式");

		this.outputFormat = upperCaseOutputFormat;
		return this;
	}

	/**
	 * 将处理后的图像保存到文件。
	 *
	 * @param outputFile 输出文件
	 * @return 如果写入成功则返回true，否则返回false
	 * @throws IOException          当写入文件出错时
	 * @throws NullPointerException 当输出文件为null时
	 * @since 2.1.0
	 */
	public boolean toFile(final File outputFile) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		return ImageIO.write(toBufferedImage(), this.outputFormat, outputFile);
	}

	/**
	 * 将处理后的图像写入输出流。
	 *
	 * @param outputStream 输出流
	 * @return 如果写入成功则返回true，否则返回false
	 * @throws IOException          当写入输出流出错时
	 * @throws NullPointerException 当输出流为null时
	 * @since 2.1.0
	 */
	public boolean toOutputStream(final OutputStream outputStream) throws IOException {
		Validate.notNull(outputStream, "outputStream不可为 null");

		return ImageIO.write(toBufferedImage(), this.outputFormat, outputStream);
	}

	/**
	 * 将处理后的图像写入图像输出流。
	 *
	 * @param imageOutputStream 图像输出流
	 * @return 如果写入成功则返回true，否则返回false
	 * @throws IOException          当写入图像输出流出错时
	 * @throws NullPointerException 当图像输出流为null时
	 * @since 2.1.0
	 */
	public boolean toImageOutputStream(final ImageOutputStream imageOutputStream) throws IOException {
		Validate.notNull(imageOutputStream, "imageOutputStream不可为null");

		return ImageIO.write(toBufferedImage(), this.outputFormat, imageOutputStream);
	}

	/**
	 * 获取处理后的图像。
	 * <p>
	 * 此方法会根据输出格式的特性进行智能转换：
	 * <ul>
	 *   <li>如果输出格式是不支持透明通道的格式（如 JPG），且当前图像包含透明通道，
	 *       会自动转换为不透明的图像类型，避免透明区域显示异常。</li>
	 *   <li>对于灰度图像，会保持为灰度格式；对于彩色图像，会转换为 RGB/BGR 格式。</li>
	 *   <li>如果不需要格式转换，会直接返回原始的输出图像。</li>
	 * </ul>
	 * </p>
	 *
	 * @return 处理后图像的 BufferedImage，可能会根据输出格式进行类型转换
	 * @since 2.1.0
	 */
	public BufferedImage toBufferedImage() {
		int imageType = outputImage.getType();
		if (imageType != BufferedImage.TYPE_BYTE_BINARY && // 排除二值化图像类型
			imageType != BufferedImage.TYPE_USHORT_GRAY && // 排除灰度化图像类型
			imageType != BufferedImage.TYPE_BYTE_GRAY && // 排除灰度化图像类型
			ImageConstants.NON_TRANSPARENT_IMAGE_FORMATS.contains(outputFormat) &&
			outputImage.getColorModel().hasAlpha()) {
			if (outputImage.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_GRAY) {
				imageType = BufferedImage.TYPE_BYTE_GRAY;
			} else {
				switch (imageType) {
					case BufferedImage.TYPE_4BYTE_ABGR:
					case BufferedImage.TYPE_4BYTE_ABGR_PRE:
						imageType = BufferedImage.TYPE_INT_BGR;
						break;
					default:
						imageType = BufferedImage.TYPE_INT_RGB;
						break;
				}
				;
			}
			return ImageUtil.toBuffered(outputImage, imageType);
		}
		return this.outputImage;
	}

	/**
	 * 恢复图像到初始状态，重置所有处理效果。
	 * 此方法会将输出图像重置为输入图像，并恢复默认设置。
	 *
	 * @return 当前编辑器实例（便于链式调用）
	 * @since 2.1.0
	 */
	public ImageProcessor reset() {
		this.outputImage.flush();

		this.outputImageSize = inputImageSize.getVisualSize();

		this.outputImage = ImageUtil.createCopy(this.inputImage);
		if (!this.inputImageSize.isNormalOrientation()) {
			this.outputImage = ImageUtils.correctOrientation(this.outputImage, this.inputImageSize.getOrientation());
		}

		this.outputFormat = this.inputFormat;
		if (Objects.isNull(this.outputFormat)) {
			this.outputFormat = inputImage.getColorModel().hasAlpha() ? DEFAULT_ALPHA_OUTPUT_FORMAT : DEFAULT_OUTPUT_FORMAT;
		}

		return this;
	}

	/**
	 * 释放图像资源，清空所有内部图像数据。
	 * <p>
	 * 调用此方法后，编辑器将不再持有任何图像数据的引用，
	 * 有助于减少内存占用。调用后编辑器不可再使用。
	 * </p>
	 *
	 * @since 2.1.0
	 */
	public void release() {
		this.outputImage.flush();
		this.outputImage = null;
	}
}