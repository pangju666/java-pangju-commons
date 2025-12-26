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

import com.drew.imaging.ImageProcessingException;
import com.twelvemonkeys.image.BrightnessContrastFilter;
import com.twelvemonkeys.image.GrayFilter;
import com.twelvemonkeys.image.ImageUtil;
import com.twelvemonkeys.image.ResampleOp;
import io.github.pangju666.commons.image.enums.WatermarkDirection;
import io.github.pangju666.commons.image.lang.ImageConstants;
import io.github.pangju666.commons.image.model.ImageSize;
import io.github.pangju666.commons.image.model.ImageWatermarkOption;
import io.github.pangju666.commons.image.model.TextWatermarkOption;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.FilenameUtils;
import io.github.pangju666.commons.io.utils.IOUtils;
import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageFilter;
import java.io.*;
import java.util.Objects;

/**
 * 图像编辑器（链式调用风格）
 * <p>
 * 提供流式 API 以便对图像进行缩放、旋转、滤镜、亮度/对比度、灰度转换、以及图片/文字水印等常见操作。<br />
 * 支持以文件、输入流、{@link ImageInputStream}与 {@link BufferedImage} 作为输入源，并可输出为文件、输出流、{@link ImageOutputStream}或 {@link BufferedImage}。<br />
 * 可选地根据 EXIF 信息自动校正图像方向（当 EXIF 不存在或读取失败时不进行校正）。
 * </p>
 *
 * <p><b>核心特性：</b></p>
 * <ul>
 *   <li><b>链式调用：</b> API 设计简洁，配置与处理顺序清晰。</li>
 *   <li><b>智能格式：</b> 自动根据透明通道选择输出格式（含 Alpha 通道默认为 PNG，否则为 JPG）。</li>
 *   <li><b>状态重置：</b> 支持 {@link #reset()} 方法将图像恢复至初始状态，便于重复使用或撤销操作。</li>
 *   <li><b>EXIF 支持：</b> 支持自动解析 EXIF 校正方向，也支持手动指定方向进行校正。</li>
 *   <li><b>丰富操作：</b>
 *     <ul>
 *       <li>缩放：支持按宽/高、按比例、强制尺寸等多种模式，默认使用高质量 Lanczos 滤波。</li>
 *       <li>调整：旋转、翻转、裁剪。</li>
 *       <li>调色：亮度、对比度、灰度化、饱和度（通过滤镜）。</li>
 *       <li>特效：模糊、锐化、自定义滤镜。</li>
 *       <li>水印：支持图片和文字水印，提供九宫格定位与精细坐标控制。</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p><b>坐标与定位：</b></p>
 * <ul>
 *   <li>提供两套定位 API：显式坐标（{@code x}, {@code y}）与九宫格方向（{@link WatermarkDirection}）。</li>
 *   <li>文字水印以“文本基线”为基准；图片水印以“左上角”为基准。</li>
 *   <li>九宫格定位会自动应用适当的边距（图片约 10px，文字约 20px）。</li>
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
 *   <li><b>流输入注意事项：</b> 当从 {@link InputStream} 创建并启用 EXIF 自动校正时：
 *     <ul>
 *       <li>推荐使用 {@link ByteArrayInputStream} 或 {@link UnsynchronizedByteArrayInputStream}，可实现零拷贝重复读取。</li>
 *       <li>对于其他类型的流，内部会将数据完全缓冲到内存中，处理大文件时需注意 OOM 风险。</li>
 *     </ul>
 *   </li>
 *   <li><b>滤波器权衡：</b> 默认的 {@link ResampleOp#FILTER_LANCZOS} 质量最好但计算最慢；对性能要求高时可选择 {@link ResampleOp#FILTER_BOX}。</li>
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
 *   <li>锐化或模糊（这两个效果互斥，一般不会同时用）</li>
 *   <li>滤镜</li>
 *   <li>添加水印</li>
 * </ol>
 *
 * <p><b>代码示例：</b></p>
 * <pre>{@code
 * // 1. 构建实例
 * // 从文件构建
 * ImageEditor.of(new File("input.jpg"));
 * // 从输入流构建
 * ImageEditor.of(inputStream);
 * // 自动校正 EXIF 方向（默认为 false）
 * ImageEditor.of(new File("input.jpg"), true);
 * // 指定 EXIF 方向（如：6 表示顺时针旋转 90 度）
 * ImageEditor.of(new File("input.jpg"), 6);
 *
 * // 2. 缩放与调整大小
 * ImageEditor.of(new File("input.jpg"))
 *     .scaleByWidth(800)              // 按宽度等比缩放
 *     .scaleByHeight(600)             // 按高度等比缩放
 *     .scale(0.5)                     // 按比例缩放（50%）
 *     .resize(100, 100)               // 强制缩放到指定尺寸（不保持比例）
 *     .toFile(new File("out_scale.jpg"));
 *
 * // 3. 裁剪操作
 * ImageEditor.of(new File("input.jpg"))
 *     .cropByCenter(400, 400)         // 居中裁剪
 *     .cropByRect(0, 0, 200, 200)     // 指定矩形区域裁剪
 *     .cropByOffset(10, 10, 20, 20)   // 按边距裁剪（上、下、左、右）
 *     .toFile(new File("out_crop.jpg"));
 *
 * // 4. 旋转与翻转
 * ImageEditor.of(new File("input.jpg"))
 *     .rotate(ImageUtil.ROTATE_90_CW) // 顺时针旋转 90 度
 *     .flip(ImageUtil.FLIP_HORIZONTAL)// 水平翻转
 *     .toFile(new File("out_rotate.jpg"));
 *
 * // 5. 色彩与滤镜
 * ImageEditor.of(new File("input.jpg"))
 *     .grayscale()                    // 转为灰度图
 *     .blur(2.0f)                     // 高斯模糊
 *     .sharpen(0.3f)                  // 锐化
 *     .contrast(0.2f)                 // 增加对比度
 *     .brightness(0.1f)               // 增加亮度
 *     .filter(new GrayFilter())       // 应用自定义滤镜（支持 java.awt.image.ImageFilter）
 *     .toFile(new File("out_filter.jpg"));
 *
 * // 6. 水印添加（支持图片与文字）
 * ImageEditor.of(new File("input.jpg"), true)
 *     .addTextWatermark("CONFIDENTIAL", new TextWatermarkOption(), WatermarkDirection.CENTER)
 *     .addImageWatermark(new File("logo.png"), new ImageWatermarkOption(), WatermarkDirection.BOTTOM_RIGHT)
 *     .toFile(new File("out_watermark.jpg"));
 *
 * // 7. 格式转换与输出
 * ImageEditor.of(new File("input.png")) // 输入 PNG
 *     .outputFormat("JPG")            // 强制输出为 JPG
 *     .toFile(new File("output.jpg"));
 *
 * // 8. 复杂操作链（链式调用）
 * ImageEditor.of(new File("input.jpg"), true)
 *     .cropByCenter(1000, 1000)       // 1. 先裁剪中心 1000x1000 区域
 *     .scaleByWidth(500)              // 2. 缩放到宽度 500px
 *     .blur(2.0f)                     // 3. 应用高斯模糊
 *     .addTextWatermark("PREVIEW", new TextWatermarkOption(), WatermarkDirection.CENTER) // 4. 添加水印
 *     .toFile(new File("processed.jpg"));
 *
 * // 9. 状态重置与多版本输出
 * ImageEditor editor = ImageEditor.of(new File("original.png"));
 * // 输出缩略图
 * editor.scaleByWidth(200)
 * 		.toFile(new File("thumbnail.png"));
 * // 重置并输出带水印的高清图
 * editor.reset()
 *       .addTextWatermark("CONFIDENTIAL", new TextWatermarkOption(), WatermarkDirection.CENTER)
 *       .toFile(new File("watermarked_original.png"));
 * }</pre>
 *
 * @author pangju666
 * @see ResampleOp
 * @see ImageSize
 * @see ImageUtil
 * @see BrightnessContrastFilter
 * @see GrayFilter
 * @see WatermarkDirection
 * @see ImageWatermarkOption
 * @see TextWatermarkOption
 * @since 1.0.0
 */
public class ImageEditor {
	/**
	 * 默认的带透明通道图像输出格式
	 * <p>
	 * 当输入图像包含透明通道（Alpha通道）时，默认使用的输出格式。
	 * PNG格式支持透明度，适合保留原图像的透明效果。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	protected static final String DEFAULT_ALPHA_OUTPUT_FORMAT = "PNG";

	/**
	 * 默认的标准图像输出格式
	 * <p>
	 * 当输入图像不包含透明通道时，默认使用的输出格式。
	 * JPG格式具有较高的压缩率，适合不需要透明效果的图像。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	protected static final String DEFAULT_OUTPUT_FORMAT = "JPG";

	/**
	 * 灰度滤镜
	 * <p>
	 * 用于将彩色图像转换为灰度图像的滤镜实例。
	 * 该滤镜在{@link #grayscale()}方法中使用。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	protected static final GrayFilter GRAY_FILTER = new GrayFilter();
	/**
	 * 默认对比度过滤器
	 * <p>
	 * 用于调整图像对比度的滤镜实例。
	 * 该滤镜在{@link #contrast()}方法中使用。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	protected static final BrightnessContrastFilter DEFAULT_CONTRAST_FILTER = new BrightnessContrastFilter(0, 0.3f);

	/**
	 * 原始输入图像
	 * <p>
	 * 存储从各种来源（文件、URL、流等）加载的原始图像数据。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	protected BufferedImage inputImage;

	/**
	 * 原始图像尺寸
	 * <p>
	 * 存储输入图像的原始宽度和高度信息。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	protected ImageSize inputImageSize;

	/**
	 * 处理后的输出图像
	 * <p>
	 * 存储经过缩放或其他处理后的图像数据，初始值为输入图像。
	 * 在调用各种处理方法后会被更新。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	protected BufferedImage outputImage;

	/**
	 * 输出图像尺寸
	 * <p>
	 * 存储当前输出图像的尺寸信息。
	 * 在调用缩放相关方法后会被更新。
	 * </p>
	 *
	 * @since 1.0.0
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
	 * @since 1.0.0
	 */
	protected String outputFormat;

	/**
	 * 重采样滤波器类型
	 * <p>
	 * 指定图像缩放时使用的重采样算法。
	 * 默认使用{@link ResampleOp#FILTER_LANCZOS Lanczos 插值（高质量）滤波器}。
	 * 可通过{@link #resampleFilterType(int)}方法修改。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	protected int resampleFilterType = ResampleOp.FILTER_LANCZOS;

	/**
	 * 输入图像格式
	 * <p>
	 * 通过 {@link #of(File)} 或 {@link #of(File, boolean)} 从文件创建时，取自文件扩展名的大写格式（如 "PNG"、"JPG"或"JPEG"）；
	 * 需为受支持的读取格式之一（参见 {@link ImageConstants#getSupportedReadImageFormats()}）。
	 * 该值用于默认初始化输出格式：构造后将把 {@code outputFormat} 设置为此扩展名；在 {@link #reset()} 时也会用它恢复输出格式。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	protected String inputFormat;

	/**
	 * 构造实例并初始化以下属性：
	 * <ul>
	 *   <li><b>输入/输出图像：</b> 初始时输出图像引用指向输入图像。</li>
	 *   <li><b>图像尺寸：</b> 记录输入图像的尺寸信息（包含可能的 EXIF 方向）。</li>
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
	 * @since 1.0.0
	 */
	protected ImageEditor(final BufferedImage inputImage, final ImageSize inputImageSize) {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.notNull(inputImageSize, "inputImageSize 不可为 null");

		this.inputImage = inputImage;
		this.inputImageSize = inputImageSize;

		this.outputImage = inputImage;
		this.outputImageSize = inputImageSize;
		this.outputFormat = inputImage.getColorModel().hasAlpha() ? DEFAULT_ALPHA_OUTPUT_FORMAT : DEFAULT_OUTPUT_FORMAT;
	}

	/**
	 * 从文件创建
	 * <p>
	 * 这是{@link #of(File, boolean)}方法的便捷重载，不矫正图像视觉方向。
	 * </p>
	 *
	 * @param file 图像文件，不可为null
	 * @return 图像编辑器实例
	 * @throws IOException 当读取图像失败时抛出
	 * @see #of(File, boolean)
	 * @since 1.0.0
	 */
	public static ImageEditor of(final File file) throws IOException {
		return of(file, false);
	}

	/**
	 * 从文件创建
	 *
	 * <p>读取图像文件数据，并根据参数决定是否处理 EXIF 方向信息。</p>
	 *
	 * <p><b>方向处理策略：</b></p>
	 * <ul>
	 *   <li><b>correctOrientation = true：</b>
	 *     <ul>
	 *       <li>优先解析 EXIF 元数据获取方向信息。</li>
	 *       <li>调用 {@link #correctOrientation(ImageEditor)} 将图像矫正为视觉方向。</li>
	 *       <li>内部 {@link ImageSize} 将更新为可视化尺寸。</li>
	 *     </ul>
	 *   </li>
	 *   <li><b>correctOrientation = false：</b>
	 *     <ul>
	 *       <li>仅读取图像的物理像素数据，忽略 EXIF 信息。</li>
	 *       <li>内部 {@link ImageSize} 不包含方向信息（orientation = null）。</li>
	 *     </ul>
	 *   </li>
	 * </ul>
	 *
	 * <p><b>文件处理说明：</b></p>
	 * <ul>
	 *   <li><b>correctOrientation = false：</b> 文件仅被读取一次（解码像素数据）。</li>
	 *   <li><b>correctOrientation = true：</b> 需两次读取文件（解析 EXIF 元数据 + 解码像素数据），会有额外的 I/O 开销。</li>
	 * </ul>
	 *
	 * @param file               图像文件，不可为 null
	 * @param correctOrientation 是否根据 EXIF 信息自动校正图像方向
	 * @return 图像编辑器实例
	 * @throws NullPointerException      当 file 为 null 时抛出
	 * @throws IllegalArgumentException  当 file 为目录或扩展名不在支持的读取格式列表时抛出
	 * @throws IOException               当读取图像失败时抛出
	 * @see #correctOrientation(ImageEditor)
	 * @see ImageUtils#getSize(File)
	 * @since 1.0.0
	 */
	public static ImageEditor of(final File file, final boolean correctOrientation) throws IOException {
		FileUtils.checkFile(file, "file 不可为 null");

		String inputFormat = FilenameUtils.getExtension(file.getName()).toUpperCase();
		Validate.isTrue(ImageConstants.getSupportedReadImageFormats().contains(inputFormat), "不支持读取该图像格式");

		Integer exifOrientation = null;
		if (correctOrientation) {
			try {
				exifOrientation = ImageUtils.getExifOrientation(file);
			} catch (ImageProcessingException | IOException ignored) {
			}
		}

		BufferedImage bufferedImage = ImageIO.read(file);
		ImageSize imageSize;
		if (Objects.nonNull(exifOrientation)) {
			imageSize = new ImageSize(bufferedImage.getWidth(), bufferedImage.getHeight(), exifOrientation);
		} else {
			imageSize = new ImageSize(bufferedImage.getWidth(), bufferedImage.getHeight());
		}
		ImageEditor imageEditor = new ImageEditor(bufferedImage, imageSize);
		imageEditor.inputFormat = inputFormat;
		imageEditor.outputFormat = inputFormat;
		correctOrientation(imageEditor);
		return imageEditor;
	}

	/**
	 * 从文件创建（手动指定 EXIF 方向）。
	 *
	 * <p>读取图像并应用指定的 EXIF 方向进行自动校正。</p>
	 * <p>适用于已从外部获取了 EXIF 方向信息（例如数据库、文件名或独立元数据读取器）的场景，
	 * 此时无需再次解析文件中的 EXIF 元数据。</p>
	 *
	 *
	 * @param file            图像文件，不可为 null
	 * @param exifOrientation 外部获取的 EXIF 方向值（1-8），用于校正图像
	 * @return 图像编辑器实例（已完成方向校正）
	 * @throws IOException          当读取图像失败时抛出
	 * @throws NullPointerException 当 file 为 null 时抛出
	 * @see #correctOrientation(ImageEditor)
	 * @since 1.0.0
	 */
	public static ImageEditor of(final File file, final int exifOrientation) throws IOException {
		FileUtils.checkFile(file, "file 不可为 null");

		String inputFormat = FilenameUtils.getExtension(file.getName()).toUpperCase();
		Validate.isTrue(ImageConstants.getSupportedReadImageFormats().contains(inputFormat), "不支持读取该图像格式");

		BufferedImage bufferedImage = ImageIO.read(file);
		ImageSize imageSize = new ImageSize(bufferedImage.getWidth(), bufferedImage.getHeight(), exifOrientation);
		ImageEditor imageEditor = new ImageEditor(bufferedImage, imageSize);
		imageEditor.inputFormat = inputFormat;
		imageEditor.outputFormat = inputFormat;
		correctOrientation(imageEditor);
		return imageEditor;
	}

	/**
	 * 从输入流创建
	 * <p>
	 * 这是{@link #of(InputStream, boolean)}方法的便捷重载，不矫正图像视觉方向。
	 * </p>
	 *
	 * @param inputStream 输入流，不可为null
	 * @return 图像编辑器实例
	 * @throws IOException 当读取图像失败时抛出
	 * @see #of(InputStream, boolean)
	 * @since 1.0.0
	 */
	public static ImageEditor of(final InputStream inputStream) throws IOException {
		return of(inputStream, false);
	}

	/**
	 * 从输入流创建
	 *
	 * <p>读取输入流中的图像数据，并根据参数决定是否处理 EXIF 方向信息。</p>
	 *
	 * <p><b>方向处理策略：</b></p>
	 * <ul>
	 *   <li><b>correctOrientation = true：</b>
	 *     <ul>
	 *       <li>优先解析 EXIF 元数据获取方向信息。</li>
	 *       <li>调用 {@link #correctOrientation(ImageEditor)} 将图像矫正为视觉方向。</li>
	 *       <li>内部 {@link ImageSize} 将更新为可视化尺寸。</li>
	 *     </ul>
	 *   </li>
	 *   <li><b>correctOrientation = false：</b>
	 *     <ul>
	 *       <li>仅读取图像的物理像素数据，忽略 EXIF 信息。</li>
	 *       <li>内部 {@link ImageSize} 不包含方向信息（orientation = null）。</li>
	 *     </ul>
	 *   </li>
	 * </ul>
	 *
	 * <p><b>流处理说明：</b></p>
	 * <ul>
	 *   <li><b>correctOrientation = false：</b> 流仅被读取一次，无特殊内存要求。</li>
	 *   <li><b>correctOrientation = true：</b> 需两次读取流（获取 EXIF + 读取像素），对流类型敏感：
	 *     <ul>
	 *       <li><b>{@link ByteArrayInputStream} / {@link UnsynchronizedByteArrayInputStream}：</b> 直接利用 reset 特性重复读取，无额外内存开销。</li>
	 *       <li><b>其他输入流：</b> 需将数据完全读取到内存中（{@link UnsynchronizedByteArrayOutputStream}）以支持重读，处理大文件时请注意内存占用。</li>
	 *     </ul>
	 *   </li>
	 * </ul>
	 *
	 * @param inputStream            包含图像数据的输入流，不可为 null
	 * @param correctOrientation 是否根据 EXIF 信息自动校正图像方向
	 * @return 图像编辑器实例
	 * @throws IOException 当读取输入流出错时
	 * @see #correctOrientation(ImageEditor)
	 * @see ImageUtils#getSize(InputStream)
	 * @since 1.0.0
	 */
	public static ImageEditor of(final InputStream inputStream, final boolean correctOrientation) throws IOException {
		Validate.notNull(inputStream, "inputStream不可为 null");

		if (!correctOrientation) {
			BufferedImage bufferedImage = ImageIO.read(inputStream);
			ImageSize imageSize = new ImageSize(bufferedImage.getWidth(), bufferedImage.getHeight());
			return new ImageEditor(bufferedImage, imageSize);
		}

		Integer exifOrientation = null;
		BufferedImage bufferedImage;
		if (inputStream instanceof ByteArrayInputStream || inputStream instanceof UnsynchronizedByteArrayInputStream) {
			try {
				exifOrientation = ImageUtils.getExifOrientation(inputStream);
			} catch (ImageProcessingException | IOException ignored) {
			}
			inputStream.reset();
			bufferedImage = ImageIO.read(inputStream);
		} else {
			UnsynchronizedByteArrayOutputStream outputStream = IOUtils.toUnsynchronizedByteArrayOutputStream(inputStream);
			try (InputStream tmpInputStream = outputStream.toInputStream()) {
				exifOrientation = ImageUtils.getExifOrientation(tmpInputStream);
			} catch (ImageProcessingException | IOException ignored) {
			}
			try (InputStream tmpInputStream = outputStream.toInputStream()) {
				bufferedImage = ImageIO.read(tmpInputStream);
			}
		}
		ImageSize imageSize;
		if (Objects.nonNull(exifOrientation)) {
			imageSize = new ImageSize(bufferedImage.getWidth(), bufferedImage.getHeight(), exifOrientation);
		} else {
			imageSize = new ImageSize(bufferedImage.getWidth(), bufferedImage.getHeight());
		}
		ImageEditor imageEditor = new ImageEditor(bufferedImage, imageSize);
		correctOrientation(imageEditor);
		return imageEditor;
	}

	/**
	 * 从输入流创建（手动指定 EXIF 方向）。
	 *
	 * <p>读取图像并应用指定的 EXIF 方向进行自动校正。</p>
	 * <p>适用于已从外部获取了 EXIF 方向信息（例如数据库或独立元数据读取器）的场景。</p>
	 *
	 * @param inputStream     包含图像数据的输入流，不可为 null
	 * @param exifOrientation 外部获取的 EXIF 方向值（1-8），用于校正图像
	 * @return 图像编辑器实例（已完成方向校正）
	 * @throws IOException          当读取输入流出错时
	 * @throws NullPointerException 当 inputStream 为 null 时抛出
	 * @see #correctOrientation(ImageEditor)
	 * @since 1.0.0
	 */
	public static ImageEditor of(final InputStream inputStream, final int exifOrientation) throws IOException {
		Validate.notNull(inputStream, "inputStream不可为 null");

		BufferedImage bufferedImage = ImageIO.read(inputStream);
		ImageSize imageSize = new ImageSize(bufferedImage.getWidth(), bufferedImage.getHeight(), exifOrientation);
		ImageEditor imageEditor = new ImageEditor(bufferedImage, imageSize);
		correctOrientation(imageEditor);
		return imageEditor;
	}

	/**
	 * 从{@link ImageInputStream}创建
	 *
	 * <p>注意：由于 {@link ImageInputStream} 不包含 EXIF 元数据，此方法创建的实例 <b>无法</b> 进行基于 EXIF 的视觉方向校正。</p>
	 * <p>适用于不需要矫正图像视觉方向的场景。</p>
	 *
	 * @param imageInputStream 图像输入流，不可为 null
	 * @return 图像编辑器实例
	 * @throws NullPointerException 当 imageInputStream 为 null 时抛出
	 * @throws IOException          当读取图像失败时抛出
	 * @since 1.0.0
	 */
	public static ImageEditor of(final ImageInputStream imageInputStream) throws IOException {
		Validate.notNull(imageInputStream, "imageInputStream不可为 null");

		BufferedImage bufferedImage = ImageIO.read(imageInputStream);
		return new ImageEditor(bufferedImage, new ImageSize(bufferedImage.getWidth(),
			bufferedImage.getHeight()));
	}

	/**
	 * 从{@link ImageInputStream}创建（手动指定 EXIF 方向）。
	 *
	 * <p>读取图像并应用指定的 EXIF 方向进行视觉方向校正。</p>
	 * <p>适用于已从外部获取了 EXIF 方向信息（例如数据库或独立元数据读取器）的场景。</p>
	 *
	 * @param imageInputStream 图像输入流，不可为 null
	 * @param exifOrientation  外部获取的 EXIF 方向值（1-8），用于校正图像
	 * @return 图像编辑器实例（已完成方向校正）
	 * @throws NullPointerException 当 imageInputStream 为 null 时抛出
	 * @throws IOException          当读取图像失败时抛出
	 * @see #correctOrientation(ImageEditor)
	 * @since 1.0.0
	 */
	public static ImageEditor of(final ImageInputStream imageInputStream, final int exifOrientation) throws IOException {
		Validate.notNull(imageInputStream, "imageInputStream不可为 null");

		BufferedImage bufferedImage = ImageIO.read(imageInputStream);
		ImageEditor imageEditor = new ImageEditor(bufferedImage, new ImageSize(
			bufferedImage.getWidth(), bufferedImage.getHeight(), exifOrientation));
		correctOrientation(imageEditor);
		return imageEditor;
	}

	/**
	 * 从 {@link BufferedImage} 创建
	 *
	 * <p>注意：由于 {@link BufferedImage} 不包含 EXIF 元数据，此方法创建的实例 <b>无法</b> 进行基于 EXIF 的视觉方向校正。</p>
	 * <p>适用于不需要矫正图像视觉方向的场景。</p>
	 *
	 * @param bufferedImage BufferedImage 对象，不可为 null
	 * @return 图像编辑器实例
	 * @throws NullPointerException 当 bufferedImage 为 null 时抛出
	 * @since 1.0.0
	 */
	public static ImageEditor of(final BufferedImage bufferedImage) {
		Validate.notNull(bufferedImage, "bufferedImage不可为 null");

		return new ImageEditor(bufferedImage, new ImageSize(bufferedImage.getWidth(),
			bufferedImage.getHeight()));
	}

	/**
	 * 从 {@link BufferedImage} 创建（手动指定 EXIF 方向）。
	 *
	 * <p>使用内存中的 {@link BufferedImage} 并应用指定的 EXIF 方向进行视觉方向校正。</p>
	 * <p>适用于图像数据已在内存中，且方向信息已知（例如来自上传请求参数）的场景。</p>
	 *
	 * @param bufferedImage   BufferedImage 对象，不可为 null
	 * @param exifOrientation 外部获取的 EXIF 方向值（1-8），用于校正图像
	 * @return 图像编辑器实例（已完成方向校正）
	 * @throws NullPointerException 当 bufferedImage 为 null 时抛出
	 * @see #correctOrientation(ImageEditor)
	 * @since 1.0.0
	 */
	public static ImageEditor of(final BufferedImage bufferedImage, final int exifOrientation) {
		Validate.notNull(bufferedImage, "bufferedImage不可为 null");

		ImageEditor imageEditor = new ImageEditor(bufferedImage, new ImageSize(
			bufferedImage.getWidth(), bufferedImage.getHeight(), exifOrientation));
		correctOrientation(imageEditor);
		return imageEditor;
	}

	/**
	 * 设置重采样滤波器类型
	 * <p>
	 * 可选的滤波器类型参见{@link ResampleOp}常量。
	 * </p>
	 *
	 * <p>默认设置为{@link ResampleOp#FILTER_LANCZOS}，一般情况下不需要设置这个。</p>
	 *
	 * @param filterType 滤波器类型，建议使用{@link ResampleOp#FILTER_LANCZOS}
	 * @return 当前实例，支持链式调用
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
	 * @since 1.0.0
	 */
	public ImageEditor resampleFilterType(final int filterType) {
		if (filterType < 0 || filterType > 15) {
			this.resampleFilterType = ResampleOp.FILTER_LANCZOS;
		} else {
			this.resampleFilterType = filterType;
		}
		return this;
	}

	/**
	 * 根据 EXIF 方向信息校正图像方向（将物理尺寸转换为可视化尺寸）。
	 * <p>
	 * 解析 {@link ImageSize} 中存储的 EXIF 方向信息，对图像数据应用相应的旋转或翻转操作，
	 * 使其在视觉上呈现为正确的方向。
	 * </p>
	 * <p><b>处理逻辑：</b></p>
	 * <ul>
	 *   <li>若未包含方向信息或方向为 1（正常），则不进行任何操作。</li>
	 *   <li>根据 EXIF 标准（1-8）自动应用旋转/翻转变换。</li>
	 *   <li>对于方向值 5-8，图像宽高会发生交换。</li>
	 *   <li>操作完成后，内部维护的 {@link ImageSize} 将更新为 {@link ImageSize#getVisualSize()}（即可视化尺寸）。</li>
	 * </ul>
	 *
	 * <p><b>不同方法处理逻辑：</b></p>
	 * <ul>
	 *   <li>1: 正常方向 (不需要校正)</li>
	 *   <li>2: 水平翻转</li>
	 *   <li>3: 旋转 180 度</li>
	 *   <li>4: 垂直翻转</li>
	 *   <li>5: 顺时针旋转 90 度后水平翻转</li>
	 *   <li>6: 顺时针旋转 90 度</li>
	 *   <li>7: 逆时针旋转 90 度后水平翻转</li>
	 *   <li>8: 逆时针旋转 90 度</li>
	 * </ul>
	 *
	 * @since 1.0.0
	 */
	protected static void correctOrientation(ImageEditor imageEditor) {
		if (Objects.isNull(imageEditor.outputImageSize.getOrientation())) {
			return;
		}

		switch (imageEditor.outputImageSize.getOrientation()) {
			case 2:
				imageEditor.flip(ImageUtil.FLIP_HORIZONTAL);
				break;
			case 3:
				imageEditor.rotate(ImageUtil.ROTATE_180);
				break;
			case 4:
				imageEditor.flip(ImageUtil.FLIP_VERTICAL);
				break;
			case 5:
				imageEditor.rotate(ImageUtil.ROTATE_90_CW);
				imageEditor.flip(ImageUtil.FLIP_HORIZONTAL);
				break;
			case 6:
				imageEditor.rotate(ImageUtil.ROTATE_90_CW);
				break;
			case 7:
				imageEditor.rotate(ImageUtil.ROTATE_90_CCW);
				imageEditor.flip(ImageUtil.FLIP_HORIZONTAL);
				break;
			case 8:
				imageEditor.rotate(ImageUtil.ROTATE_90_CCW);
				break;
			default:
				break;
		}
		imageEditor.outputImageSize = imageEditor.outputImageSize.getVisualSize();

		// 防止 reset 后丢失方向矫正效果
		imageEditor.inputImage = imageEditor.outputImage;
		imageEditor.inputImageSize = imageEditor.outputImageSize;
	}

	/**
	 * 按指定方向旋转图像。
	 *
	 * @param direction 旋转方向，可以是 {@link ImageUtil#ROTATE_90_CW}、{@link ImageUtil#ROTATE_90_CCW} 或 {@link ImageUtil#ROTATE_180}
	 * @return 当前编辑器实例，用于链式调用
	 * @since 1.0.0
	 */
	public ImageEditor rotate(final int direction) {
		if (direction == ImageUtil.ROTATE_90_CW || direction == ImageUtil.ROTATE_90_CCW || direction == ImageUtil.ROTATE_180) {
			this.outputImage = ImageUtil.createRotated(this.outputImage, Math.toRadians(direction));
		}
		return this;
	}

	/**
	 * 按指定角度旋转图像。
	 *
	 * @param angleDegrees 旋转角度（度数），正值表示顺时针旋转
	 * @return 当前编辑器实例，用于链式调用
	 * @since 1.0.0
	 */
	public ImageEditor rotate(final double angleDegrees) {
		this.outputImage = ImageUtil.createRotated(this.outputImage, Math.toRadians(angleDegrees));
		return this;
	}

	/**
	 * 对图像应用模糊效果，使用默认模糊半径1.5。
	 *
	 * <p><b>效果说明</b>：使用高斯模糊核，半径越大越模糊；1.5 像素提供轻微柔化，适合降噪或 UI 图标处理。</p>
	 *
	 * @return 当前编辑器实例，用于链式调用
	 * @since 1.0.0
	 */
	public ImageEditor blur() {
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
	 * @since 1.0.0
	 */
	public ImageEditor blur(final float radius) {
		this.outputImage = ImageUtil.blur(this.outputImage, radius);
		return this;
	}

	/**
	 * 翻转图像。
	 *
	 * @param axis 翻转轴，可以是 {@link ImageUtil#FLIP_HORIZONTAL} 或 {@link ImageUtil#FLIP_VERTICAL}
	 * @return 当前编辑器实例，用于链式调用
	 * @since 1.0.0
	 */
	public ImageEditor flip(final int axis) {
		if (axis == ImageUtil.FLIP_HORIZONTAL || axis == ImageUtil.FLIP_VERTICAL) {
			this.outputImage = ImageUtil.createFlipped(this.outputImage, axis);
		}
		return this;
	}

	/**
	 * 对图像应用锐化效果，使用默认锐化强度0.3。
	 *
	 * <p><b>效果说明</b>：基于非锐化掩模（Unsharp Mask）原理，0.3 提供自然清晰度提升，无明显光晕。</p>
	 *
	 * @return 当前编辑器实例，用于链式调用
	 * @since 1.0.0
	 */
	public ImageEditor sharpen() {
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
	 * @since 1.0.0
	 */
	public ImageEditor sharpen(final float amount) {
		this.outputImage = ImageUtil.sharpen(this.outputImage, amount);
		return this;
	}

	/**
	 * 将图像转换为灰度图。
	 *
	 * @return 当前编辑器实例，用于链式调用
	 * @since 1.0.0
	 */
	public ImageEditor grayscale() {
		Image image = ImageUtil.filter(this.outputImage, GRAY_FILTER);
		this.outputImage = ImageUtil.toBuffered(image, this.outputImage.getType());
		return this;
	}

	/**
	 * 调整图像对比度，使用默认对比度值0.3。
	 *
	 * <p><b>效果说明</b>：0.3 表示对比度提升约 30%，使明暗更分明，适用于灰蒙图像。</p>
	 *
	 * @return 当前编辑器实例，用于链式调用
	 * @since 1.0.0
	 */
	public ImageEditor contrast() {
		Image image = ImageUtil.filter(this.outputImage, DEFAULT_CONTRAST_FILTER);
		this.outputImage = ImageUtil.toBuffered(image, this.outputImage.getType());
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
	 * @since 1.0.0
	 */
	public ImageEditor contrast(final float amount) {
		if (amount == 0f || amount > 1.0 || amount < -1.0) {
			return this;
		}

		BrightnessContrastFilter filter = new BrightnessContrastFilter(0f, amount);
		Image image = ImageUtil.filter(this.outputImage, filter);
		this.outputImage = ImageUtil.toBuffered(image, this.outputImage.getType());
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
	 * @since 1.0.0
	 */
	public ImageEditor brightness(final float amount) {
		if (amount == 0f || amount > 2.0 || amount < -2.0) {
			return this;
		}

		BrightnessContrastFilter filter = new BrightnessContrastFilter(amount, 0f);
		Image image = ImageUtil.filter(this.outputImage, filter);
		this.outputImage = ImageUtil.toBuffered(image, this.outputImage.getType());
		return this;
	}

	/**
	 * 对图像应用自定义过滤器。
	 *
	 * @param filter 要应用的图像过滤器
	 * @return 当前编辑器实例，用于链式调用
	 * @throws NullPointerException 当过滤器为null时
	 * @since 1.0.0
	 */
	public ImageEditor filter(final ImageFilter filter) {
		Validate.notNull(filter, "filter不可为 null");

		Image image = ImageUtil.filter(this.outputImage, filter);
		this.outputImage = ImageUtil.toBuffered(image, this.outputImage.getType());
		return this;
	}

	/**
	 * 强制将图像缩放到指定的宽度和高度，不保持原始宽高比。
	 *
	 * @param width  目标宽度（像素）
	 * @param height 目标高度（像素）
	 * @return 当前编辑器实例，用于链式调用
	 * @since 1.0.0
	 */
	public ImageEditor resize(final int width, final int height) {
		this.outputImageSize = new ImageSize(width, height);
		this.outputImage = resample();
		return this;
	}

	/**
	 * 强制将图像缩放到指定的尺寸，不保持原始宽高比。
	 *
	 * @param size 目标尺寸
	 * @return 当前编辑器实例，用于链式调用
	 * @throws NullPointerException 当尺寸参数为null时
	 * @since 1.0.0
	 */
	public ImageEditor resize(final ImageSize size) {
		Validate.notNull(size, "size不可为 null");

		this.outputImageSize = size;
		this.outputImage = resample();
		return this;
	}

	/**
	 * 按指定宽度等比例缩放图像，保持原始宽高比。
	 *
	 * @param width 目标宽度（像素）
	 * @return 当前编辑器实例，用于链式调用
	 * @since 1.0.0
	 */
	public ImageEditor scaleByWidth(final int width) {
		this.outputImageSize = this.outputImageSize.scaleByWidth(width);
		this.outputImage = resample();
		return this;
	}

	/**
	 * 按指定高度等比例缩放图像，保持原始宽高比。
	 *
	 * @param height 目标高度（像素）
	 * @return 当前编辑器实例，用于链式调用
	 * @since 1.0.0
	 */
	public ImageEditor scaleByHeight(final int height) {
		this.outputImageSize = this.outputImageSize.scaleByHeight(height);
		this.outputImage = resample();
		return this;
	}

	/**
	 * 将图像缩放到指定的比例，保持原始宽高比。
	 *
	 * @param scale 缩放比例
	 * @return 当前编辑器实例，用于链式调用
	 * @since 1.0.0
	 */
	public ImageEditor scale(final double scale) {
		this.outputImageSize = this.outputImageSize.scale(scale);
		this.outputImage = resample();
		return this;
	}

	/**
	 * 将图像缩放到指定的最大宽度和高度范围内，保持原始宽高比。
	 *
	 * @param width  最大宽度（像素）
	 * @param height 最大高度（像素）
	 * @return 当前编辑器实例，用于链式调用
	 * @see ImageSize#scale(int, int)
	 * @since 1.0.0
	 */
	public ImageEditor scale(final int width, final int height) {
		this.outputImageSize = this.outputImageSize.scale(width, height);
		this.outputImage = resample();
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
	 * @since 1.0.0
	 */
	public ImageEditor cropByCenter(int width, int height) {
		Validate.isTrue(width > 0, "width 不能小于0");
		Validate.isTrue(height > 0, "height 不能小于0");

		// 边界检测
		if (width >= this.outputImage.getWidth() || height >= this.outputImage.getHeight()) {
			return this;
		}

		this.outputImage = this.outputImage.getSubimage((this.outputImage.getWidth() - width) / 2,
			(this.outputImage.getHeight() - height) / 2, width, height);
		this.outputImageSize = new ImageSize(this.outputImage.getWidth(), this.outputImage.getHeight());
		return this;
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
	 * @since 1.0.0
	 */
	public ImageEditor cropByOffset(int topOffset, int bottomOffset, int leftOffset, int rightOffset) {
		Validate.isTrue(topOffset >= 0 && bottomOffset >= 0 && leftOffset >= 0 && rightOffset >= 0,
			"offset 不能小于0");

		// 边界检测
		if (rightOffset >= this.outputImage.getWidth() ||
			leftOffset >= this.outputImage.getWidth() ||
			leftOffset + rightOffset >= this.outputImage.getWidth() ||
			topOffset >= this.outputImage.getHeight() ||
			bottomOffset >= this.outputImage.getHeight() ||
			topOffset + bottomOffset >= this.outputImage.getHeight()) {
			return this;
		}

		this.outputImage = this.outputImage.getSubimage(leftOffset, topOffset,
			this.outputImage.getWidth() - leftOffset - rightOffset,
			this.outputImage.getHeight() - topOffset - bottomOffset);
		this.outputImageSize = new ImageSize(this.outputImage.getWidth(), this.outputImage.getHeight());
		return this;
	}

	/**
	 * 按矩形区域进行裁剪。
	 * <p>
	 * 使用左上角坐标 {@code (x, y)} 与尺寸 {@code (width, height)} 指定裁剪矩形。
	 * 当矩形超出图像边界（包含等于边界的情况）时，不进行裁剪并返回。
	 * </p>
	 *
	 * @param x      裁剪矩形左上角 X 坐标，必须大于等于 0
	 * @param y      裁剪矩形左上角 Y 坐标，必须大于等于 0
	 * @param width  裁剪宽度，必须大于 0
	 * @param height 裁剪高度，必须大于 0
	 * @return 当前编辑器实例（便于链式调用）
	 * @throws IllegalArgumentException 当 {@code x} 或 {@code y} 为负数，或 {@code width}、{@code height} 小于等于 0 时抛出
	 * @since 1.0.0
	 */
	public ImageEditor cropByRect(int x, int y, int width, int height) {
		Validate.isTrue(x >= 0, "x 不能小于0");
		Validate.isTrue(y >= 0, "y 不能小于0");
		Validate.isTrue(width > 0, "width 不能小于0");
		Validate.isTrue(height > 0, "height 不能小于0");

		// 边界检测
		if (x >= this.outputImage.getWidth() ||
			width >= this.outputImage.getWidth() ||
			x + width >= this.outputImage.getWidth() ||
			y >= this.outputImage.getHeight() ||
			height >= this.outputImage.getHeight() ||
			y + height >= this.outputImage.getHeight()) {
			return this;
		}

		this.outputImage = this.outputImage.getSubimage(x, y, width, height);
		this.outputImageSize = new ImageSize(this.outputImage.getWidth(), this.outputImage.getHeight());
		return this;
	}

	/**
	 * 添加图片水印（显式坐标定位）。
	 * <p>
	 * 直接使用传入的左上角坐标 {@code x}/{@code y} 进行定位，不使用九宫格方向。
	 * 其缩放、透明度与尺寸约束等行为与受保护方法一致。
	 *
	 * @param watermarkImage 图片水印源，不能为空
	 * @param option         水印配置（缩放、透明度、尺寸约束等）
	 * @param x              绘制起点 X（左上角）
	 * @param y              绘制起点 Y（左上角）
	 * @return 当前编辑器实例（便于链式调用）
	 * @throws IllegalArgumentException 当 x 或者 y &lt; 0 时抛出
	 * @since 1.0.0
	 * @see #addImageWatermark(BufferedImage, ImageWatermarkOption, WatermarkDirection, int, int)
	 */
	public ImageEditor addImageWatermark(BufferedImage watermarkImage, ImageWatermarkOption option, int x, int y) {
		Validate.isTrue(x >= 0 && y >= 0, "水印位置必须大于0");
		return addImageWatermark(watermarkImage, option, null, x, y);
	}

	/**
	 * 添加图片水印（按九宫格方向自动定位）。
	 * <p>
	 * 坐标固定为 (0, 0)，实际绘制位置由 {@code direction} 决定。
	 * 缩放、透明度与尺寸约束等行为与受保护方法一致。
	 *
	 * @param watermarkImage 图片水印源，不能为空
	 * @param option         水印配置（缩放、透明度、尺寸约束等）
	 * @param direction      九宫格方向，用于自动计算水印位置
	 * @return 当前编辑器实例（便于链式调用）
	 * @throws IllegalArgumentException 当 {@code direction} 为 {@code null} 时抛出
	 * @since 1.0.0
	 * @see #addImageWatermark(BufferedImage, ImageWatermarkOption, WatermarkDirection, int, int)
	 */
	public ImageEditor addImageWatermark(BufferedImage watermarkImage, ImageWatermarkOption option, WatermarkDirection direction) {
		Validate.notNull(direction, "direction 不可为 null");
		return addImageWatermark(watermarkImage, option, direction, 0, 0);
	}

	/**
	 * 从文件加载并添加图片水印（按九宫格方向自动定位）。
	 * <p>
	 * 会先校验文件非空且存在，然后通过 {@code ImageIO.read} 读取为 {@code BufferedImage}。
	 * 其他缩放、透明度与尺寸约束等行为与受保护方法一致。
	 *
	 * @param watermarkFile 水印图片文件，不能为空且存在
	 * @param option        水印配置（缩放、透明度、尺寸约束等）
	 * @param direction     九宫格方向，用于自动计算水印位置
	 * @return 当前编辑器实例（便于链式调用）
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当 {@code direction} 为 {@code null} 时抛出
	 * @since 1.0.0
	 * @see #addImageWatermark(BufferedImage, ImageWatermarkOption, WatermarkDirection, int, int)
	 */
	public ImageEditor addImageWatermark(File watermarkFile, ImageWatermarkOption option, WatermarkDirection direction) throws IOException {
		Validate.notNull(direction, "direction 不可为 null");
		FileUtils.checkFile(watermarkFile, "file 不可为 null");
		return addImageWatermark(ImageIO.read(watermarkFile), option, direction, 0, 0);
	}

	/**
	 * 从文件加载并添加图片水印（显式坐标定位）。
	 * <p>
	 * 会先校验文件非空且存在，然后通过 {@code ImageIO.read} 读取为 {@code BufferedImage}。
	 * 直接使用传入的左上角坐标 {@code x}/{@code y} 进行定位。
	 *
	 * @param watermarkFile 水印图片文件，不能为空且存在
	 * @param option        水印配置（缩放、透明度、尺寸约束等）
	 * @param x             绘制起点 X（左上角）
	 * @param y             绘制起点 Y（左上角）
	 * @return 当前编辑器实例（便于链式调用）
	 * @throws IOException 当文件读取失败时抛出
	 * @since 1.0.0
	 * @see #addImageWatermark(BufferedImage, ImageWatermarkOption, WatermarkDirection, int, int)
	 */
	public ImageEditor addImageWatermark(File watermarkFile, ImageWatermarkOption option, int x, int y) throws IOException {
		FileUtils.checkFile(watermarkFile, "file 不可为 null");
		return addImageWatermark(ImageIO.read(watermarkFile), option, null, x, y);
	}

	/**
	 * 添加文字水印（按九宫格方向自动定位）。
	 * <p>
	 * 坐标固定为 (0, 0)（文本基线），实际绘制位置由 {@code direction} 决定。
	 * 字体来源于 {@code option.fontName}/{@code option.fontStyle}；字号按输出图像较长边结合 {@code option.fontSizeRatio} 计算并四舍五入。
	 * 文本抗锯齿开启；透明度与描边效果由 {@code option} 控制。
	 * </p>
	 *
	 * @param watermarkText 非空的水印文本内容
	 * @param option        文本水印配置（字体、透明度、颜色、描边开关与线宽等）
	 * @param direction     九宫格方向，用于自动计算水印位置
	 * @return 当前编辑器实例（便于链式调用）
	 * @throws IllegalArgumentException 当 {@code direction} 为 {@code null}、或 {@code watermarkText} 为空、或 {@code option} 为 {@code null} 时抛出
	 * @since 1.0.0
	 * @see #addTextWatermark(String, TextWatermarkOption, WatermarkDirection, int, int)
	 */
	public ImageEditor addTextWatermark(String watermarkText, TextWatermarkOption option, WatermarkDirection direction) {
		Validate.notNull(direction, "direction 不可为 null");
		return addTextWatermark(watermarkText, option, direction, 0, 0);
	}

	/**
	 * 添加文字水印（显式坐标定位）。
	 * <p>
	 * 直接使用传入坐标 {@code x}/{@code y}（文本基线）进行定位，不使用九宫格方向。
	 * 字体来源于 {@code option.fontName}/{@code option.fontStyle}；字号按输出图像较长边结合 {@code option.fontSizeRatio} 计算并四舍五入。
	 * 文本抗锯齿开启；透明度与描边效果由 {@code option} 控制。
	 * </p>
	 *
	 * @param watermarkText 非空的水印文本内容
	 * @param option        文本水印配置（字体、透明度、颜色、描边开关与线宽等）
	 * @param x             绘制起点 X（文本基线）
	 * @param y             绘制起点 Y（文本基线）
	 * @return 当前编辑器实例（便于链式调用）
	 * @throws IllegalArgumentException 当 {@code x < 0} 或 {@code y < 0}，或 {@code watermarkText} 为空，或 {@code option} 为 {@code null} 时抛出
	 * @since 1.0.0
	 * @see #addTextWatermark(String, TextWatermarkOption, WatermarkDirection, int, int)
	 */
	public ImageEditor addTextWatermark(String watermarkText, TextWatermarkOption option, int x, int y) {
		Validate.isTrue(x >= 0 && y >= 0, "水印位置必须大于0");
		return addTextWatermark(watermarkText, option, null, x, y);
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
	 * @since 1.0.0
	 */
	public ImageEditor outputFormat(final String outputFormat) {
		Validate.notBlank(outputFormat, "输出格式不可为空");
		String upperCaseOutputFormat = outputFormat.toUpperCase();
		Validate.isTrue(ImageConstants.getSupportedWriteImageFormats().contains(upperCaseOutputFormat),
			"不支持输出该图像格式");

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
	 * @since 1.0.0
	 */
	public boolean toFile(final File outputFile) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		return ImageIO.write(toBufferedImage(), this.outputFormat, outputFile);
	}

	/**
	 * 将处理后的图像写入输出流。
	 *
	 * @param outputStream 输出流
	 * @return 如果写入成功则返回true，否则返回false
	 * @throws IOException          当写入输出流出错时
	 * @throws NullPointerException 当输出流为null时
	 * @since 1.0.0
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
	 * @since 1.0.0
	 */
	public boolean toImageOutputStream(final ImageOutputStream imageOutputStream) throws IOException {
		Validate.notNull(imageOutputStream, "imageOutputStream不可为null");

		return ImageIO.write(toBufferedImage(), this.outputFormat, imageOutputStream);
	}

	/**
	 * 获取处理后的图像。
	 *
	 * @return 处理后图像的BufferedImage
	 * @since 1.0.0
	 */
	public BufferedImage toBufferedImage() {
		if (outputImage.getColorModel().hasAlpha() && ImageConstants.NON_TRANSPARENT_IMAGE_FORMATS.contains(outputFormat)) {
			int imageType;
			switch (outputImage.getType()) {
				case BufferedImage.TYPE_4BYTE_ABGR:
				case BufferedImage.TYPE_4BYTE_ABGR_PRE:
					imageType = BufferedImage.TYPE_3BYTE_BGR;
					break;
				default:
					imageType = BufferedImage.TYPE_INT_RGB;
					break;
			}
			return ImageUtil.toBuffered(outputImage, imageType);
		}
		return this.outputImage;
	}

	/**
	 * 对图像进行重采样处理，根据当前设置的输出尺寸和重采样过滤器类型。
	 *
	 * @return 重采样后的图像
	 * @since 1.0.0
	 */
	protected BufferedImage resample() {
		return new ResampleOp(outputImageSize.getWidth(), outputImageSize.getHeight(), resampleFilterType)
			.filter(outputImage, null);
	}

	/**
	 * 在输出图像上绘制图片水印。
	 * <p>
	 * 行为说明：
	 * <ul>
	 *   <li>根据 {@code option.relativeScale} 按输出图像尺寸等比计算目标水印大小，随后再受
	 *   {@code minWidth}/{@code minHeight}/{@code maxWidth}/{@code maxHeight} 的下限与上限约束。</li>
	 *   <li>当 {@code option.opacity} 在 (0, 1) 之间时，使用相应透明度进行叠加绘制。</li>
	 *   <li>若提供 {@code direction}，按照九宫格方向自动计算绘制位置；为 {@code null} 时使用传入的左上角坐标 {@code x}/{@code y}。</li>
	 * </ul>
	 *
	 * @param watermarkImage 非空的图片水印源（将被绘制到输出图像上）
	 * @param option         水印配置（缩放、透明度、尺寸约束等）
	 * @param direction      九宫格方向；为 {@code null} 时直接使用 {@code x}/{@code y}
	 * @param x              当 {@code direction} 为 {@code null} 时的绘制起点 X（左上角）
	 * @param y              当 {@code direction} 为 {@code null} 时的绘制起点 Y（左上角）
	 * @return 当前编辑器实例（便于链式调用）
	 * @throws IllegalArgumentException 当 {@code watermarkImage} 或 {@code option} 为 {@code null} 时抛出
	 * @see io.github.pangju666.commons.image.model.ImageWatermarkOption
	 * @see io.github.pangju666.commons.image.enums.WatermarkDirection
	 * @since 1.0.0
	 */
	protected ImageEditor addImageWatermark(BufferedImage watermarkImage, ImageWatermarkOption option, WatermarkDirection direction,
											int x, int y) {
		Validate.notNull(watermarkImage, "watermarkImage 不可为 null");
		Validate.notNull(option, "option 不可为 null");

		Graphics2D graphics = this.outputImage.createGraphics();

		// 设置抗锯齿
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// 设置不透明度
		if (option.getOpacity() > 0f && option.getOpacity() < 1) {
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, option.getOpacity()));
		}

		ImageSize originalWatermarkImageSize = new ImageSize(watermarkImage.getWidth(), watermarkImage.getHeight());
		ImageSize watermarkImageSize = originalWatermarkImageSize.scale(this.outputImageSize.scale(option.getRelativeScale()));
		if (watermarkImageSize.getWidth() > watermarkImageSize.getHeight()) {
			if (watermarkImageSize.getWidth() > option.getMaxWidth()) {
				watermarkImageSize = originalWatermarkImageSize.scaleByWidth(option.getMaxWidth());
			} else if (watermarkImageSize.getWidth() < option.getMinWidth()) {
				watermarkImageSize = originalWatermarkImageSize.scaleByWidth(option.getMinWidth());
			}
		} else {
			if (watermarkImageSize.getHeight() > option.getMaxHeight()) {
				watermarkImageSize = originalWatermarkImageSize.scaleByHeight(option.getMaxHeight());
			} else if (watermarkImageSize.getHeight() < option.getMinHeight()) {
				watermarkImageSize = originalWatermarkImageSize.scaleByHeight(option.getMinHeight());
			}
		}

		int waterX = x;
		int waterY = y;
		if (Objects.nonNull(direction)) {
			switch (direction) {
				case TOP:
					waterX = (outputImageSize.getWidth() - watermarkImageSize.getWidth()) / 2;
					waterY = 10;
					break;
				case BOTTOM:
					waterX = (outputImageSize.getHeight() - watermarkImageSize.getHeight()) / 2;
					waterY = outputImageSize.getHeight() - watermarkImageSize.getHeight() - 10;
					break;
				case TOP_LEFT:
					waterX = 10;
					waterY = 10;
					break;
				case TOP_RIGHT:
					waterX = outputImageSize.getWidth() - watermarkImageSize.getWidth() - 10;
					waterY = 10;
					break;
				case BOTTOM_LEFT:
					waterX = 10;
					waterY = outputImageSize.getHeight() - watermarkImageSize.getHeight() - 10;
					break;
				case BOTTOM_RIGHT:
					waterX = outputImageSize.getWidth() - watermarkImageSize.getWidth() - 10;
					waterY = outputImageSize.getHeight() - watermarkImageSize.getHeight() - 10;
					break;
				case LEFT:
					waterX = 10;
					waterY = (outputImageSize.getHeight() - watermarkImageSize.getHeight()) / 2;
					break;
				case RIGHT:
					waterX = outputImageSize.getWidth() - watermarkImageSize.getWidth() - 10;
					waterY = (outputImageSize.getHeight() - watermarkImageSize.getHeight()) / 2;
					break;
				case CENTER:
					waterX = (outputImageSize.getWidth() - watermarkImageSize.getWidth()) / 2;
					waterY = (outputImageSize.getHeight() - watermarkImageSize.getHeight()) / 2;
					break;
			}
		}

		graphics.drawImage(watermarkImage, waterX, waterY, watermarkImageSize.getWidth(),
			watermarkImageSize.getHeight(), null);
		graphics.dispose();

		return this;
	}

	/**
	 * 在输出图像上绘制文字水印。
	 * <p>
	 * 行为说明：
	 * <ul>
	 *   <li>启用文本抗锯齿；字体来源于 {@code option.fontName}/{@code option.fontStyle}，字号按较长边与 {@code option.fontSizeRatio} 计算并四舍五入。</li>
	 *   <li>文本位置以基线为准；内部使用字体度量（{@code FontMetrics}）计算文本宽度与高度。</li>
	 *   <li>当 {@code direction} 非空时按九宫格方向自动计算位置；为 {@code null} 时使用传入坐标 {@code x}/{@code y}（基线）。</li>
	 *   <li>当 {@code option.stroke} 为 {@code true} 时，先按描边色与线宽绘制描边，再按填充色绘制文本。</li>
	 *   <li>颜色透明度受 {@code option.opacity} 或颜色自身 Alpha 影响；内部按 {@code TextWatermarkOption} 配置进行转换。</li>
	 * </ul>
	 *
	 * @param text      非空的水印文本内容
	 * @param option    文本水印配置（字体、透明度、颜色、描边开关与线宽等）
	 * @param direction 九宫格方向；为 {@code null} 时使用传入坐标 {@code x}/{@code y}
	 * @param x         当 {@code direction} 为 {@code null} 时的绘制起点 X（文本基线）
	 * @param y         当 {@code direction} 为 {@code null} 时的绘制起点 Y（文本基线）
	 * @return 当前编辑器实例（便于链式调用）
	 * @throws IllegalArgumentException 当 {@code text} 为空或 {@code option} 为 {@code null} 时抛出
	 * @see io.github.pangju666.commons.image.model.TextWatermarkOption
	 * @see io.github.pangju666.commons.image.enums.WatermarkDirection
	 * @since 1.0.0
	 */
	protected ImageEditor addTextWatermark(String text, TextWatermarkOption option, WatermarkDirection direction,
										   int x, int y) {
		Validate.notBlank(text, "text 不可为空");
		Validate.notNull(option, "option 不可为 null");

		Graphics2D graphics = this.outputImage.createGraphics();

		int fontSize;
		if (outputImageSize.getWidth() > outputImageSize.getHeight()) {
			fontSize = (int) Math.round(outputImageSize.getWidth() * option.getFontSizeRatio());
		} else {
			fontSize = (int) Math.round(outputImageSize.getHeight() * option.getFontSizeRatio());
		}

		Font font = new Font(option.getFontName(), option.getFontStyle(), fontSize);

		// 设置抗锯齿
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		// 设置字体
		graphics.setFont(font);

		FontMetrics fontMetrics = graphics.getFontMetrics();
		int textWidth = fontMetrics.stringWidth(text);
		int textHeight = fontMetrics.getAscent() - fontMetrics.getDescent();

		int waterX = x;
		int waterY = y;
		if (Objects.nonNull(direction)) {
			switch (direction) {
				case TOP:
					waterX = (outputImageSize.getWidth() - textWidth) / 2;
					waterY = 20 + textHeight;
					break;
				case BOTTOM:
					waterX = (outputImageSize.getHeight() + textHeight) / 2;
					waterY = outputImageSize.getHeight() - 20;
					break;
				case TOP_LEFT:
					waterX = 20;
					waterY = 20 + textHeight;
					break;
				case TOP_RIGHT:
					waterX = outputImageSize.getWidth() - textWidth - 20;
					waterY = 20 + textHeight;
					break;
				case BOTTOM_LEFT:
					waterX = 20;
					waterY = outputImageSize.getHeight() - 20;
					break;
				case BOTTOM_RIGHT:
					waterX = outputImageSize.getWidth() - textWidth - 20;
					waterY = outputImageSize.getHeight() - 20;
					break;
				case LEFT:
					waterX = 20;
					waterY = (outputImageSize.getHeight() + textHeight) / 2;
					break;
				case RIGHT:
					waterX = outputImageSize.getWidth() - textWidth - 20;
					waterY = (outputImageSize.getHeight() + textHeight) / 2;
					break;
				case CENTER:
					waterX = (outputImageSize.getWidth() - textWidth) / 2;
					waterY = (outputImageSize.getHeight() + textHeight) / 2;
					break;
			}
		}

		if (option.isStroke()) {
			// 绘制描边
			graphics.setColor(getStrokeColor(option));
			graphics.setStroke(new BasicStroke(option.getStrokeWidth()));
			graphics.drawString(text, waterX, waterY);

			// 绘制填充
			graphics.setColor(getFillColor(option));
			graphics.setStroke(new BasicStroke(0.5f));
			graphics.drawString(text, waterX, waterY);
		} else {
			graphics.setColor(getFillColor(option));
			graphics.drawString(text, waterX, waterY);
		}
		graphics.dispose();

		return this;
	}

	/**
	 * 获取文本水印的填充颜色（应用不透明度）
	 * <p>
	 * 逻辑：
	 * <ol>
	 *   <li>若 {@code option.getFillColor()} 为空，使用 {@link Color#WHITE}</li>
	 *   <li>当颜色为完全不透明（Alpha=255）且 {@code 0 < option.getOpacity() < 1}，
	 *   按给定不透明度调整 Alpha 并返回新颜色</li>
	 *   <li>否则返回原始颜色</li>
	 * </ol>
	 * </p>
	 *
	 * @param option 文本水印配置，需非 null，使用其中的填充色与不透明度
	 * @return 应用不透明度后的填充颜色
	 * @since 1.0.0
	 */
	protected Color getFillColor(TextWatermarkOption option) {
		Color color = ObjectUtils.getIfNull(option.getFillColor(), Color.WHITE);
		if (color.getAlpha() == 255) {
			// 设置不透明度
			if (option.getOpacity() > 0f && option.getOpacity() < 1) {
				return new Color(color.getRed(), color.getGreen(), color.getBlue(), 255 * option.getOpacity());
			}
		}
		return color;
	}

	/**
	 * 获取文本水印的描边颜色（应用不透明度）
	 * <p>
	 * 逻辑：
	 * <ol>
	 *   <li>若 {@code option.getStrokeColor()} 为空，使用 {@link Color#LIGHT_GRAY}</li>
	 *   <li>当颜色为完全不透明（Alpha=255）且 {@code 0 < option.getOpacity() < 1}，
	 *   按给定不透明度调整 Alpha 并返回新颜色</li>
	 *   <li>否则返回原始颜色</li>
	 * </ol>
	 * </p>
	 *
	 * @param option 文本水印配置，需非 null，使用其中的描边色与不透明度
	 * @return 应用不透明度后的描边颜色
	 * @since 1.0.0
	 */
	protected Color getStrokeColor(TextWatermarkOption option) {
		Color color = ObjectUtils.getIfNull(option.getStrokeColor(), Color.LIGHT_GRAY);
		if (color.getAlpha() == 255) {
			// 设置不透明度
			if (option.getOpacity() > 0f && option.getOpacity() < 1) {
				return new Color(color.getRed(), color.getGreen(), color.getBlue(), 255 * option.getOpacity());
			}
		}
		return color;
	}

	/**
	 * 恢复图像到初始状态，重置所有处理效果。
	 * 此方法会将输出图像重置为输入图像，并恢复默认设置。
	 *
	 * @return 当前编辑器实例（便于链式调用）
	 * @since 1.0.0
	 */
	public ImageEditor reset() {
		this.outputImage = this.inputImage;
		this.outputImageSize = this.inputImageSize;
		this.outputFormat = this.inputFormat;
		if (StringUtils.isBlank(this.outputFormat)) {
			this.outputFormat = inputImage.getColorModel().hasAlpha() ? DEFAULT_ALPHA_OUTPUT_FORMAT : DEFAULT_OUTPUT_FORMAT;
		}
		this.resampleFilterType = ResampleOp.FILTER_LANCZOS;
		return this;
	}
}