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
import org.apache.commons.lang3.Validate;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageFilter;
import java.io.*;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 图像编辑器（链式调用风格）
 * <p>
 * 提供流式 API 以便对图像进行缩放、旋转、滤镜、亮度/对比度、灰度转换、以及图片/文字水印等常见操作，
 * 支持 URL、文件、输入流与 {@code BufferedImage} 作为输入源，并可输出为文件、输出流或 {@code BufferedImage}。
 * 可选地根据 EXIF 信息自动校正图像方向（当 EXIF 不存在或读取失败时不进行校正）。
 * </p>
 *
 * <p><b>核心特性：</b></p>
 * <ul>
 *   <li>链式 API，配置与处理顺序清晰</li>
 *   <li>多种缩放模式：强制缩放、按宽度/高度缩放、按比例缩放</li>
 *   <li>可自定义重采样滤波器，默认使用 {@link ResampleOp#FILTER_LANCZOS}（高质量）</li>
 *   <li>旋转、翻转、模糊、锐化、灰度、亮度与对比度调整</li>
 *   <li>图片水印与文字水印，支持 {@link io.github.pangju666.commons.image.enums.WatermarkDirection 九宫格定位}</li>
 *   <li>根据 EXIF 自动方向校正（可选）</li>
 *   <li>自动根据透明通道选择输出格式（PNG 或 JPEG）</li>
 * </ul>
 *
 * <p><b>坐标与定位：</b></p>
 * <ul>
 *   <li>提供两套定位 API：显式坐标方法签名包含 {@code x}/{@code y}；九宫格方法签名包含 {@code direction}，位置由方向计算。</li>
 *   <li>文字水印的坐标以“文本基线”为基准；图片水印以“左上角”为基准。</li>
 *   <li>内部实现对边缘位置使用固定边距进行微调：图片约 10px、文字约 20px。</li>
 * </ul>
 *
 * <p><b>代码示例（水印）：</b></p>
 * <pre>{@code
 * // 图片水印：按九宫格方向定位（右下角）
 * ImageEditor.of(new File("input.jpg"))
 *     .addImageWatermark(ImageIO.read(new File("wm.png")), new ImageWatermarkOption(), WatermarkDirection.BOTTOM_RIGHT)
 *     .toFile(new File("output_dir.jpg"));
 *
 * // 图片水印：显式坐标定位（左上角 20,20）
 * ImageEditor.of(new File("input.jpg"))
 *     .addImageWatermark(ImageIO.read(new File("wm.png")), new ImageWatermarkOption(), 20, 20)
 *     .toFile(new File("output_xy.jpg"));
 *
 * // 文字水印：按九宫格方向定位（左上角）
 * TextWatermarkOption textOpt = new TextWatermarkOption();
 * textOpt.setOpacity(0.6f);
 * textOpt.setStroke(true);
 * ImageEditor.of(new File("input.jpg"))
 *     .addTextWatermark("© Company", textOpt, WatermarkDirection.TOP_LEFT)
 *     .toFile(new File("output_text_dir.jpg"));
 *
 * // 文字水印：显式坐标定位（基线 50,50）
 * ImageEditor.of(new File("input.jpg"))
 *     .addTextWatermark("CONFIDENTIAL", new TextWatermarkOption(), 50, 50)
 *     .toFile(new File("output_text_xy.jpg"));
 * }</pre>
 *
 * <p><b>代码示例（常用操作）：</b></p>
 * <pre>{@code
 * // 1) 缩放
 * ImageEditor.of(new File("input.jpg"))
 *     .scaleByWidth(400)              // 按宽度等比缩放
 *     .toFile(new File("out_scale_by_width.jpg"));
 *
 * ImageEditor.of(new File("input.jpg"))
 *     .resize(640, 360)               // 强制缩放到指定尺寸
 *     .outputFormat("png")            // 指定输出格式
 *     .toFile(new File("out_resize.png"));
 *
 * ImageEditor.of(new File("input.jpg"))
 *     .scale(0.5)                     // 按比例缩放（50%）
 *     .scaleFilterType(ResampleOp.FILTER_LANCZOS) // 高质量滤波
 *     .toFile(new File("out_scale_ratio.jpg"));
 *
 * // 2) 旋转与翻转
 * ImageEditor.of(new File("input.jpg"))
 *     .rotate(ImageUtil.ROTATE_90_CW) // 顺时针 90°
 *     .toFile(new File("out_rotate_90.jpg"));
 *
 * ImageEditor.of(new File("input.jpg"))
 *     .flip(ImageUtil.FLIP_HORIZONTAL) // 水平翻转
 *     .toFile(new File("out_flip_h.jpg"));
 *
 * // 3) 滤镜、模糊与锐化
 * ImageEditor.of(new File("input.jpg"))
 *     .blur(2.0f)                     // 高斯模糊半径 2.0
 *     .toFile(new File("out_blur.jpg"));
 *
 * ImageEditor.of(new File("input.jpg"))
 *     .sharpen(0.25f)                 // 锐化强度 0.25
 *     .toFile(new File("out_sharpen.jpg"));
 *
 * ImageEditor.of(new File("input.jpg"))
 *     .grayscale()                    // 转灰度
 *     .toFile(new File("out_gray.jpg"));
 *
 * // 4) 亮度与对比度
 * ImageEditor.of(new File("input.jpg"))
 *     .brightness(0.1f)               // 提升亮度
 *     .contrast(0.2f)                 // 提升对比度
 *     .toFile(new File("out_light_contrast.jpg"));
 *
 * // 5) EXIF 方向校正（自动）
 * ImageEditor.of(new File("input.jpg"), true) // 启用自动校正，EXIF 缺失或读取失败时不校正
 *     .toFile(new File("out_exif_auto.jpg"));
 *
 * // 5') EXIF 方向校正（手动）
 * try {
 *     File in = new File("input.jpg");
 *     int exif = ImageUtils.getExifOrientation(in); // 可能抛出异常
 *     ImageEditor.of(in)                            // 不启用自动校正
 *         .correctOrientation(exif)                 // 手动矫正 EXIF 方向
 *         .toFile(new File("out_exif_manual.jpg"));
 * } catch (IOException | ImageProcessingException e) {
 *     // 无法获取 EXIF 时可保持原始方向或记录日志
 * }
 *
 * // 6) 输出到不同目标
 * ByteArrayOutputStream os = new ByteArrayOutputStream();
 * ImageEditor.of(new File("input.jpg"))
 *     .scaleByHeight(300)
 *     .toOutputStream(os);            // 输出到流
 * byte[] data = os.toByteArray();
 *
 * BufferedImage bi = ImageEditor.of(new File("input.jpg"))
 *     .scale(0.75)
 *     .toBufferedImage();             // 输出为 BufferedImage
 *
 * // 7) 恢复初始状态并继续处理
 * ImageEditor editor = ImageEditor.of(new File("input.jpg"));
 * editor.blur(1.5f).contrast(0.2f);
 * editor.restore();                   // 恢复到原始图像
 * editor.scaleByWidth(500).toFile(new File("out_after_restore.jpg"));
 * }
 * </pre>
 *
 * <p><b>使用建议：</b></p>
 * <ul>
 *   <li>处理链按照方法调用顺序依次应用，可通过 {@link #restore()} 恢复到初始状态。</li>
 *   <li>大图或批量处理场景建议合理复用实例并注意资源释放。</li>
 * </ul>
 *
 * <p><b>线程安全：</b></p>
 * <ul>
 *   <li>本类为<strong>非线程安全</strong>，实例包含可变状态（如 {@code outputImage}、{@code outputFormat}）。</li>
 *   <li>不要在多个线程间共享同一实例并发调用；请为每个线程创建独立实例或在外部做同步。</li>
 *   <li>方法内部会创建并释放 {@code Graphics2D}，并发访问可能导致资源竞争或不可预期的呈现结果。</li>
 * </ul>
 *
 * <p><b>性能与内存：</b></p>
 * <ul>
 *   <li>处理大图会占用较多内存，建议先进行缩放再应用其他效果，以降低后续计算量。</li>
 *   <li>滤波器选择存在质量与速度权衡：{@link ResampleOp#FILTER_LANCZOS} 质量高但较慢；{@link ResampleOp#FILTER_BOX} 较快且质量中等；{@link ResampleOp#FILTER_POINT} 最快但质量较低。</li>
 *   <li>亮度/对比度滤镜使用简单缓存以复用常用参数实例，重复调用可减少对象创建。</li>
 *   <li>合成与水印绘制会创建临时 {@code Graphics2D} 并在结束后释放；避免在热点路径中频繁创建与销毁编辑器实例。</li>
 * </ul>
 *
 * <p><b>异常与容错：</b></p>
 * <ul>
 *   <li>EXIF 读取失败或不存在时不会抛出异常，保持原始方向并继续处理。</li>
 *   <li>参数校验使用 {@link org.apache.commons.lang3.Validate}；常见异常为 {@link IllegalArgumentException}（如参数为 {@code null}、文本为空、不支持的输出格式）。</li>
 *   <li>读取与写入操作（如 {@link #of(File)}、{@link #toFile(File)}）在失败时抛出 {@link IOException}。</li>
 * </ul>
 *
 * @author pangju666
 * @see ResampleOp
 * @see ImageSize
 * @see ImageUtil
 * @see BrightnessContrastFilter
 * @see GrayFilter
 * @see io.github.pangju666.commons.image.enums.WatermarkDirection
 * @see io.github.pangju666.commons.image.model.ImageWatermarkOption
 * @see io.github.pangju666.commons.image.model.TextWatermarkOption
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
	protected static final String DEFAULT_ALPHA_OUTPUT_FORMAT = "png";

	/**
	 * 文本水印的默认字体。
	 * <p>
	 * 当 {@code TextWatermarkOption#font} 未设置时，绘制文字水印将使用该字体。
	 * 采用字体 {@code Font.SANS_SERIF}、常规样式 {@code Font.PLAIN}、字号 12。
	 *
	 * @since 1.0.0
	 */
	protected static final Font DEFAULT_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

	/**
	 * 默认的标准图像输出格式
	 * <p>
	 * 当输入图像不包含透明通道时，默认使用的输出格式。
	 * JPG格式具有较高的压缩率，适合不需要透明效果的图像。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	protected static final String DEFAULT_OUTPUT_FORMAT = "jpg";

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
	 * 亮度滤镜缓存
	 * <p>
	 * 存储不同亮度值对应的滤镜实例，避免重复创建相同参数的滤镜对象。
	 * 键为亮度调整值，值为对应的亮度对比度滤镜实例。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	protected static final Map<Float, BrightnessContrastFilter> BRIGHTNESS_FILTERS_MAP = new ConcurrentHashMap<>(10);

	/**
	 * 对比度滤镜缓存
	 * <p>
	 * 存储不同对比度值对应的滤镜实例，避免重复创建相同参数的滤镜对象。
	 * 键为对比度调整值，值为对应的亮度对比度滤镜实例。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	protected static final Map<Float, BrightnessContrastFilter> CONTRAST_FILTERS_MAP = new ConcurrentHashMap<>(10);

	static {
		CONTRAST_FILTERS_MAP.put(0.3f, new BrightnessContrastFilter(0f, 0.3f));
	}

	/**
	 * 原始输入图像
	 * <p>
	 * 存储从各种来源（文件、URL、流等）加载的原始图像数据。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private final BufferedImage inputImage;

	/**
	 * 原始图像尺寸
	 * <p>
	 * 存储输入图像的原始宽度和高度信息。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private final ImageSize inputImageSize;

	/**
	 * 处理后的输出图像
	 * <p>
	 * 存储经过缩放或其他处理后的图像数据，初始值为输入图像。
	 * 在调用各种处理方法后会被更新。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private BufferedImage outputImage;

	/**
	 * 输出图像尺寸
	 * <p>
	 * 存储当前输出图像的尺寸信息。
	 * 在调用缩放相关方法后会被更新。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private ImageSize outputImageSize;

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
	private String outputFormat;

	/**
	 * 重采样滤波器类型
	 * <p>
	 * 指定图像缩放时使用的重采样算法。
	 * 默认使用{@link ResampleOp#FILTER_LANCZOS Lanczos 插值（高质量）滤波器}。
	 * 可通过{@link #resampleFilterType(int)}或{@link #scaleHints(int)}方法修改。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private int resampleFilterType = ResampleOp.FILTER_LANCZOS;

	/**
	 * 构造图像编辑器（指定输出格式）
	 *
	 * @param inputImage     原始图像，不可为null
	 * @param inputImageSize 原始图像尺寸，不可为null
	 * @throws NullPointerException 当参数为null时抛出
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
	 * 从URL创建图像编辑器
	 *
	 * @param url 图像URL，不可为null
	 * @return 图像编辑器实例
	 * @throws IOException 当读取图像失败时抛出
	 * @since 1.0.0
	 */
	public static ImageEditor of(final URL url) throws IOException {
		Validate.notNull(url, "url不可为 null");

		BufferedImage bufferedImage = ImageIO.read(url);
		return new ImageEditor(bufferedImage, new ImageSize(bufferedImage.getWidth(), bufferedImage.getHeight()));
	}

	/**
	 * 从文件创建图像编辑器
	 * <p>
	 * 自动从文件扩展名推断输出格式
	 * 这是{@link #of(File, boolean)}方法的便捷重载，自动校正参数设为false。
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
	 * 从文件创建图像编辑器。
	 *
	 * <p>自动从文件扩展名推断输出格式。可选择是否根据 EXIF 信息自动校正图像方向。
	 * 当 EXIF 不存在或读取失败时，将保持原始方向并忽略相关异常。</p>
	 *
	 * <p>处理流程：</p>
	 * <ol>
	 *   <li>验证文件有效性</li>
	 *   <li>读取图像数据到 BufferedImage</li>
	 *   <li>创建 ImageEditor 实例</li>
	 *   <li>设置输出格式为文件扩展名</li>
	 *   <li>若启用自动校正：尝试读取 EXIF 方向并应用校正；若 EXIF 不存在或读取失败则保持原始方向</li>
	 * </ol>
	 *
	 * @param file                   图像文件，不可为 null
	 * @param autoCorrectOrientation 是否自动校正图像方向（根据 EXIF 信息）
	 * @return 图像编辑器实例
	 * @throws IOException 当读取图像失败时抛出
	 * @see io.github.pangju666.commons.image.utils.ImageUtils#getExifOrientation(java.io.File)
	 * @since 1.0.0
	 */
	public static ImageEditor of(final File file, final boolean autoCorrectOrientation) throws IOException {
		FileUtils.checkFile(file, "file 不可为 null");

		BufferedImage bufferedImage = ImageIO.read(file);
		ImageEditor imageEditor = new ImageEditor(bufferedImage, new ImageSize(bufferedImage.getWidth(),
			bufferedImage.getHeight()));
		imageEditor.outputFormat(FilenameUtils.getExtension(file.getName()));
		if (autoCorrectOrientation) {
			int exif = ImageConstants.NORMAL_EXIF_ORIENTATION;
			try {
				Metadata metadata = ImageMetadataReader.readMetadata(file);
				exif = ImageUtils.getExifOrientation(metadata);
			} catch (ImageProcessingException | IOException ignored) {
			}
			imageEditor.correctOrientation(exif);
		}
		return imageEditor;
	}

	/**
	 * 从输入流创建图像编辑器
	 * <p>
	 * 这是{@link #of(InputStream, boolean)}方法的便捷重载，
	 * 自动校正参数设为false。
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
	 * 从输入流创建图像编辑器，并可选择是否自动校正图像方向。
	 *
	 * <p>该方法会从输入流读取图像数据并创建一个新的 {@code ImageEditor} 实例。
	 * 当 {@code autoCorrectOrientation} 为 {@code true} 时，将尝试读取 EXIF 方向并进行校正；
	 * 若 EXIF 不存在或读取失败，则保持原始方向，不进行校正且忽略相关异常。</p>
	 *
	 * <p>针对不同类型的输入流：</p>
	 * <ul>
	 *   <li>可重置流（如 {@link ByteArrayInputStream}）：先读取图像数据，重置流后再读取 EXIF。</li>
	 *   <li>不可重置流：先将内容复制到内存，再分别读取图像数据与 EXIF。</li>
	 * </ul>
	 *
	 * @param inputStream            包含图像数据的输入流，不可为 null
	 * @param autoCorrectOrientation 是否根据 EXIF 信息自动校正图像方向
	 * @return 新创建的图像编辑器实例
	 * @throws IOException 当读取输入流出错时
	 * @see #of(File, boolean)
	 * @see #correctOrientation(int)
	 * @see io.github.pangju666.commons.image.utils.ImageUtils#getExifOrientation(java.io.InputStream)
	 * @since 1.0.0
	 */
	public static ImageEditor of(final InputStream inputStream, final boolean autoCorrectOrientation) throws IOException {
		Validate.notNull(inputStream, "inputStream不可为 null");

		if (!autoCorrectOrientation) {
			BufferedImage bufferedImage = ImageIO.read(inputStream);
			return new ImageEditor(bufferedImage, new ImageSize(
				bufferedImage.getWidth(), bufferedImage.getHeight()));
		}

		ImageEditor imageEditor;
		if (inputStream instanceof ByteArrayInputStream || inputStream instanceof UnsynchronizedByteArrayInputStream) {
			BufferedImage bufferedImage = ImageIO.read(inputStream);
			imageEditor = new ImageEditor(bufferedImage, new ImageSize(bufferedImage.getWidth(),
				bufferedImage.getHeight()));

			inputStream.reset();
			int exif = ImageConstants.NORMAL_EXIF_ORIENTATION;
			try {
				Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
				exif = ImageUtils.getExifOrientation(metadata);
			} catch (ImageProcessingException | IOException ignored) {
			}
			imageEditor.correctOrientation(exif);
		} else {
			UnsynchronizedByteArrayOutputStream outputStream = IOUtils.toUnsynchronizedByteArrayOutputStream(inputStream);

			try (InputStream tmpInputStream = outputStream.toInputStream()) {
				BufferedImage bufferedImage = ImageIO.read(tmpInputStream);
				imageEditor = new ImageEditor(bufferedImage, new ImageSize(
					bufferedImage.getWidth(), bufferedImage.getHeight()));
			}

			int exif = ImageConstants.NORMAL_EXIF_ORIENTATION;
			try (InputStream tmpInputStream = outputStream.toInputStream()) {
				Metadata metadata = ImageMetadataReader.readMetadata(tmpInputStream);
				exif = ImageUtils.getExifOrientation(metadata);
			} catch (ImageProcessingException | IOException ignored) {
			}
			imageEditor.correctOrientation(exif);
		}
		return imageEditor;
	}

	/**
	 * 从图像输入流创建图像编辑器
	 *
	 * @param imageInputStream 图像输入流，不可为null
	 * @return 图像编辑器实例
	 * @throws IOException 当读取图像失败时抛出
	 * @since 1.0.0
	 */
	public static ImageEditor of(final ImageInputStream imageInputStream) throws IOException {
		Validate.notNull(imageInputStream, "imageInputStream不可为 null");

		BufferedImage bufferedImage = ImageIO.read(imageInputStream);
		return new ImageEditor(bufferedImage, new ImageSize(bufferedImage.getWidth(), bufferedImage.getHeight()));
	}

	/**
	 * 从BufferedImage创建图像编辑器
	 *
	 * @param bufferedImage BufferedImage对象，不可为null
	 * @return 图像编辑器实例
	 * @since 1.0.0
	 */
	public static ImageEditor of(final BufferedImage bufferedImage) {
		Validate.notNull(bufferedImage, "bufferedImage不可为 null");

		return new ImageEditor(bufferedImage, new ImageSize(bufferedImage.getWidth(), bufferedImage.getHeight()));
	}

	/**
	 * 设置重采样滤波器类型
	 * <p>
	 * 可选的滤波器类型参见{@link ResampleOp}常量。
	 * </p>
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
	 * 设置图像缩放的提示类型，影响缩放算法的选择。
	 * <p>
	 * 根据不同的提示类型，会选择不同的重采样过滤器：
	 * <ul>
	 *   <li>{@link Image#SCALE_FAST} 或 {@link Image#SCALE_REPLICATE}: 使用最近邻插值 (FILTER_POINT)</li>
	 *   <li>{@link Image#SCALE_AREA_AVERAGING}: 使用盒式过滤 (FILTER_BOX)</li>
	 *   <li>{@link Image#SCALE_SMOOTH}: 使用Lanczos算法 (FILTER_LANCZOS)</li>
	 *   <li>其他值: 使用二次插值 (FILTER_QUADRATIC)</li>
	 * </ul>
	 *
	 * @param hints 缩放提示类型，来自 {@link Image} 类的常量
	 * @return 当前缩略图处理器实例，用于链式调用
	 * @since 1.0.0
	 */
	public ImageEditor scaleHints(final int hints) {
		switch (hints) {
			case Image.SCALE_FAST:
			case Image.SCALE_REPLICATE:
				this.resampleFilterType = ResampleOp.FILTER_POINT;
				break;
			case Image.SCALE_AREA_AVERAGING:
				this.resampleFilterType = ResampleOp.FILTER_BOX;
				break;
			case Image.SCALE_SMOOTH:
				this.resampleFilterType = ResampleOp.FILTER_LANCZOS;
				break;
			default:
				this.resampleFilterType = ResampleOp.FILTER_QUADRATIC;
				break;
		}
		return this;
	}

	/**
	 * 设置输出图像的格式。
	 *
	 * @param outputFormat 输出格式，如 "jpg"、"png" 等
	 * @return 当前缩略图处理器实例，用于链式调用
	 * @throws IllegalArgumentException 当指定的格式不被支持时
	 * @since 1.0.0
	 */
	public ImageEditor outputFormat(final String outputFormat) {
		Validate.isTrue(ImageConstants.getSupportWriteImageFormats().contains(outputFormat), "不支持输出该图像格式");

		this.outputFormat = outputFormat;
		return this;
	}

	/**
	 * 根据EXIF方向信息校正图像方向。
	 * <p>
	 * 根据EXIF标准，方向值范围为1-8：
	 * <ul>
	 *   <li>1: 正常方向 (不需要校正)</li>
	 *   <li>2: 水平翻转</li>
	 *   <li>3: 旋转180度</li>
	 *   <li>4: 垂直翻转</li>
	 *   <li>5: 顺时针旋转90度后水平翻转</li>
	 *   <li>6: 顺时针旋转90度</li>
	 *   <li>7: 逆时针旋转90度后水平翻转</li>
	 *   <li>8: 逆时针旋转90度</li>
	 * </ul>
	 * 对于方向值5-8，会同时调整输出图像的宽高比例。
	 *
	 * @param orientation EXIF方向值(1-8)
	 * @return 当前缩略图处理器实例，用于链式调用
	 * @since 1.0.0
	 */
	public ImageEditor correctOrientation(int orientation) {
		if (orientation < 1 || orientation > 8) {
			return this;
		}

		switch (orientation) {
			case 2:
				flip(ImageUtil.FLIP_HORIZONTAL);
				break;
			case 3:
				rotate(ImageUtil.ROTATE_180);
				break;
			case 4:
				flip(ImageUtil.FLIP_VERTICAL);
				break;
			case 5:
				rotate(ImageUtil.ROTATE_90_CW);
				flip(ImageUtil.FLIP_HORIZONTAL);
				break;
			case 6:
				rotate(ImageUtil.ROTATE_90_CW);
				break;
			case 7:
				rotate(ImageUtil.ROTATE_90_CCW);
				flip(ImageUtil.FLIP_HORIZONTAL);
				break;
			case 8:
				rotate(ImageUtil.ROTATE_90_CCW);
				break;
			default:
				break;
		}
		if (orientation >= 5) {
			this.outputImageSize = new ImageSize(this.outputImageSize.getHeight(), this.outputImageSize.getWidth());
		}
		return this;
	}

	/**
	 * 按指定方向旋转图像。
	 *
	 * @param direction 旋转方向，可以是 {@link ImageUtil#ROTATE_90_CW}、{@link ImageUtil#ROTATE_90_CCW} 或 {@link ImageUtil#ROTATE_180}
	 * @return 当前缩略图处理器实例，用于链式调用
	 * @since 1.0.0
	 */
	public ImageEditor rotate(final int direction) {
		if (direction == ImageUtil.ROTATE_90_CW || direction == ImageUtil.ROTATE_90_CCW || direction == ImageUtil.ROTATE_180) {
			this.outputImage = ImageUtil.createRotated(this.outputImage, direction);
		}
		return this;
	}

	/**
	 * 按指定角度旋转图像。
	 *
	 * @param angle 旋转角度（度数），正值表示顺时针旋转
	 * @return 当前缩略图处理器实例，用于链式调用
	 * @since 1.0.0
	 */
	public ImageEditor rotate(final double angle) {
		this.outputImage = ImageUtil.createRotated(this.outputImage, angle);
		return this;
	}

	/**
	 * 对图像应用模糊效果，使用默认模糊半径1.5。
	 *
	 * @return 当前缩略图处理器实例，用于链式调用
	 * @since 1.0.0
	 */
	public ImageEditor blur() {
		this.outputImage = ImageUtil.blur(this.outputImage, 1.5f);
		return this;
	}

	/**
	 * 对图像应用模糊效果，使用指定的模糊半径。
	 *
	 * @param radius 模糊半径，值越大模糊效果越强
	 * @return 当前缩略图处理器实例，用于链式调用
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
	 * @return 当前缩略图处理器实例，用于链式调用
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
	 * @return 当前缩略图处理器实例，用于链式调用
	 * @since 1.0.0
	 */
	public ImageEditor sharpen() {
		this.outputImage = ImageUtil.sharpen(this.outputImage, 0.3f);
		return this;
	}

	/**
	 * 对图像应用锐化效果，使用指定的锐化强度。
	 *
	 * @param amount 锐化强度
	 * @return 当前缩略图处理器实例，用于链式调用
	 * @since 1.0.0
	 */
	public ImageEditor sharpen(final float amount) {
		this.outputImage = ImageUtil.sharpen(this.outputImage, amount);
		return this;
	}

	/**
	 * 将图像转换为灰度图。
	 *
	 * @return 当前缩略图处理器实例，用于链式调用
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
	 * @return 当前缩略图处理器实例，用于链式调用
	 * @since 1.0.0
	 */
	public ImageEditor contrast() {
		return contrast(0.3f);
	}

	/**
	 * 调整图像对比度。
	 *
	 * @param amount 对比度调整值，范围为-1.0到1.0，0表示不变，正值增加对比度，负值降低对比度
	 * @return 当前缩略图处理器实例，用于链式调用
	 * @since 1.0.0
	 */
	public ImageEditor contrast(final float amount) {
		if (amount == 0f || amount > 1.0 || amount < -1.0) {
			return this;
		}

		BrightnessContrastFilter filter;
		if (CONTRAST_FILTERS_MAP.containsKey(amount)) {
			filter = BRIGHTNESS_FILTERS_MAP.get(amount);
		} else {
			filter = new BrightnessContrastFilter(0f, amount);
			BRIGHTNESS_FILTERS_MAP.put(amount, filter);
		}

		Image image = ImageUtil.filter(this.outputImage, filter);
		this.outputImage = ImageUtil.toBuffered(image, this.outputImage.getType());
		return this;
	}

	/**
	 * 调整图像亮度。
	 *
	 * @param amount 亮度调整值，范围为-2.0到2.0，0表示不变，正值增加亮度，负值降低亮度
	 * @return 当前缩略图处理器实例，用于链式调用
	 * @since 1.0.0
	 */
	public ImageEditor brightness(final float amount) {
		if (amount == 0f || amount > 2.0 || amount < -2.0) {
			return this;
		}

		BrightnessContrastFilter filter;
		if (BRIGHTNESS_FILTERS_MAP.containsKey(amount)) {
			filter = BRIGHTNESS_FILTERS_MAP.get(amount);
		} else {
			filter = new BrightnessContrastFilter(amount, 0f);
			BRIGHTNESS_FILTERS_MAP.put(amount, filter);
		}

		Image image = ImageUtil.filter(this.outputImage, filter);
		this.outputImage = ImageUtil.toBuffered(image, this.outputImage.getType());
		return this;
	}

	/**
	 * 对图像应用自定义过滤器。
	 *
	 * @param filter 要应用的图像过滤器
	 * @return 当前缩略图处理器实例，用于链式调用
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
	 * @return 当前缩略图处理器实例，用于链式调用
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
	 * @return 当前缩略图处理器实例，用于链式调用
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
	 * @return 当前缩略图处理器实例，用于链式调用
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
	 * @return 当前缩略图处理器实例，用于链式调用
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
	 * @return 当前缩略图处理器实例，用于链式调用
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
	 * @return 当前缩略图处理器实例，用于链式调用
	 * @see ImageSize#scale(int, int)
	 * @since 1.0.0
	 */
	public ImageEditor scale(final int width, final int height) {
		this.outputImageSize = this.outputImageSize.scale(width, height);
		this.outputImage = resample();
		return this;
	}

	/**
	 * 设置输出图像整体不透明度并进行覆盖绘制。
	 * <p>
	 * 使用 {@code AlphaComposite.SRC_OVER} 以指定透明度将输入图像绘制到当前输出图像上。
	 * 当 {@code opacity} &lt; 0 或 &gt; 1 时不进行任何处理直接返回。
	 *
	 * @param opacity 不透明度，取值范围 [0, 1]，超出范围不生效
	 * @return 当前编辑器实例（便于链式调用）
	 * @since 1.0.0
	 */
	public ImageEditor opacity(float opacity) {
		if (opacity < 0f || opacity > 1.0) {
			return this;
		}

		Graphics2D graphics2D = this.outputImage.createGraphics();
		graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
		graphics2D.drawImage(inputImage, 0, 0, null);
		graphics2D.dispose();
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
	 */
	public ImageEditor addImageWatermark(File watermarkFile, ImageWatermarkOption option, int x, int y) throws IOException {
		FileUtils.checkFile(watermarkFile, "file 不可为 null");
		return addImageWatermark(ImageIO.read(watermarkFile), option, null, x, y);
	}

	/**
	 * 添加文字水印（按九宫格方向自动定位）。
	 * <p>
	 * 坐标固定为 (0, 0)，实际绘制位置由 {@code direction} 决定。
	 * 字体来源于 {@code option.font}，为空则使用默认字体 {@code DEFAULT_FONT}；
	 * 透明度与描边效果由 {@code option} 控制。
	 *
	 * @param watermarkText 非空的水印文本内容
	 * @param option        文本水印配置（字体、透明度、颜色、描边开关与线宽等）
	 * @param direction     九宫格方向，用于自动计算水印位置
	 * @return 当前编辑器实例（便于链式调用）
	 * @throws IllegalArgumentException 当 {@code direction} 为 {@code null} 时抛出
	 * @since 1.0.0
	 */
	public ImageEditor addTextWatermark(String watermarkText, TextWatermarkOption option, WatermarkDirection direction) {
		Validate.notNull(direction, "direction 不可为 null");
		return addTextWatermark(watermarkText, option, direction, 0, 0);
	}

	/**
	 * 添加文字水印（显式坐标定位）。
	 * <p>
	 * 直接使用传入坐标 {@code x}/{@code y}（文本基线）进行定位，不使用九宫格方向。
	 * 字体来源于 {@code option.font}，为空则使用默认字体 {@code DEFAULT_FONT}；
	 * 透明度与描边效果由 {@code option} 控制。
	 *
	 * @param watermarkText 非空的水印文本内容
	 * @param option        文本水印配置（字体、透明度、颜色、描边开关与线宽等）
	 * @param x             绘制起点 X（文本基线）
	 * @param y             绘制起点 Y（文本基线）
	 * @return 当前编辑器实例（便于链式调用）
	 * @throws IllegalArgumentException 当 x 或者 y &lt; 0 时抛出
	 * @since 1.0.0
	 */
	public ImageEditor addTextWatermark(String watermarkText, TextWatermarkOption option, int x, int y) {
		Validate.isTrue(x >= 0 && y >= 0, "水印位置必须大于0");
		return addTextWatermark(watermarkText, option, null, x, y);
	}

	/**
	 * 恢复图像到初始状态，重置所有处理效果。
	 * 此方法会将输出图像重置为输入图像，并恢复默认设置。
	 *
	 * @return 当前缩略图处理器实例，用于链式调用
	 * @since 1.0.0
	 */
	public ImageEditor restore() {
		this.outputImage = this.inputImage;
		this.outputImageSize = this.inputImageSize;
		this.outputFormat = inputImage.getColorModel().hasAlpha() ? DEFAULT_ALPHA_OUTPUT_FORMAT : DEFAULT_OUTPUT_FORMAT;
		this.resampleFilterType = ResampleOp.FILTER_LANCZOS;
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

		return ImageIO.write(this.outputImage, this.outputFormat, outputFile);
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

		return ImageIO.write(this.outputImage, this.outputFormat, outputStream);
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

		return ImageIO.write(this.outputImage, this.outputFormat, imageOutputStream);
	}

	/**
	 * 获取处理后的图像。
	 *
	 * @return 处理后图像的BufferedImage
	 * @since 1.0.0
	 */
	public BufferedImage toBufferedImage() {
		return this.outputImage;
	}

	/**
	 * 对图像进行重采样处理，根据当前设置的输出尺寸和重采样过滤器类型。
	 * 如果输出格式不支持透明度但原图有透明通道，会自动转换为不透明图像。
	 *
	 * @return 重采样后的图像
	 * @since 1.0.0
	 */
	protected BufferedImage resample() {
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
			BufferedImage noAlphaOutputImage = new BufferedImage(outputImageSize.getWidth(), outputImageSize.getHeight(), imageType);
			ResampleOp resampleOp = new ResampleOp(outputImageSize.getWidth(), outputImageSize.getHeight(), resampleFilterType);
			return resampleOp.filter(this.outputImage, noAlphaOutputImage);
		} else {
			return new ResampleOp(outputImageSize.getWidth(), outputImageSize.getHeight(), resampleFilterType)
				.filter(outputImage, null);
		}
	}

	/**
	 * 在输出图像上绘制图片水印。
	 * <p>
	 * 行为说明：
	 * <ul>
	 *   <li>根据 {@code option.scale} 按输出图像尺寸等比计算目标水印大小，随后再受
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
		graphics.drawImage(this.outputImage, 0, 0, null);

		// 设置抗锯齿
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// 设置不透明度
		if (option.getOpacity() > 0f && option.getOpacity() < 1) {
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, option.getOpacity()));
		}

		ImageSize originalWatermarkImageSize = new ImageSize(watermarkImage.getWidth(), watermarkImage.getHeight());
		ImageSize watermarkImageSize = originalWatermarkImageSize.scale(this.outputImageSize.scale(option.getScale()));
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
	 *   <li>启用抗锯齿与文字抗锯齿；字体来源于 {@code option.font}，为空则使用默认字体。</li>
	 *   <li>根据 {@code direction} 自动计算九宫格位置；为 {@code null} 时使用传入坐标 {@code x}/{@code y}。</li>
	 *   <li>文本位置以基线为准；内部使用字体度量（{@code FontMetrics}）计算文本宽度与高度。</li>
	 *   <li>当 {@code option.stroke} 为 {@code true} 时，先按描边色与线宽绘制描边，再按填充色绘制文本。</li>
	 * </ul>
	 *
	 * @param text      非空的水印文本内容
	 * @param option    文本水印配置（字体、透明度、颜色、描边开关与线宽等）
	 * @param direction 九宫格方向；为 {@code null} 时直接使用 {@code x}/{@code y}
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
		graphics.drawImage(this.outputImage, 0, 0, null);

		// 设置抗锯齿
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		// 设置字体
		graphics.setFont(ObjectUtils.getIfNull(option.getFont(), DEFAULT_FONT));

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
}