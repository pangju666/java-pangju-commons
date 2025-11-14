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
import io.github.pangju666.commons.image.lang.ImageConstants;
import io.github.pangju666.commons.image.model.ImageSize;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.FilenameUtils;
import io.github.pangju666.commons.io.utils.IOUtils;
import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
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
import java.util.concurrent.ConcurrentHashMap;

/**
 * 图像编辑器（链式调用风格）
 * <p>
 * 本类提供了流式API来处理图像，支持链式方法调用以配置各种参数。
 * 可以轻松实现图像的缩放、旋转、滤镜效果等多种处理操作。
 * </p>
 *
 * <p><b>核心特性：</b></p>
 * <ul>
 *   <li>链式调用API，支持流畅的方法调用</li>
 *   <li>支持多种缩放模式：强制缩放、按宽度缩放、按高度缩放、按比例缩放等</li>
 *   <li>自动处理透明通道和图像格式转换</li>
 *   <li>支持多种输入源：URL、文件、流、BufferedImage</li>
 *   <li>支持多种输出目标：文件、流、BufferedImage</li>
 *   <li>可自定义重采样滤波器</li>
 *   <li>支持图像旋转、翻转、模糊、锐化等处理</li>
 *   <li>支持亮度、对比度调整和灰度转换</li>
 *   <li>自动根据EXIF信息校正图像方向</li>
 *   <li>支持图像处理效果的叠加应用</li>
 *   <li>提供恢复原始图像状态的功能</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>{@code
 * // 基本用法：按宽度等比缩放
 * ImageEditor.of(new File("input.jpg"))
 *     .scaleByWidth(200)
 *     .toFile(new File("output.jpg"));
 *
 * // 强制缩放到指定尺寸
 * ImageEditor.of(bufferedImage)
 *     .resize(300, 200)
 *     .outputFormat("png")
 *     .toOutputStream(outputStream);
 *
 * // 图像处理链
 * ImageEditor.of(url)
 *     .scaleByWidth(400)
 *     .scaleFilterType(ResampleOp.FILTER_LANCZOS)
 *     .rotate(90)
 *     .grayscale()
 *     .contrast(0.2f)
 *     .toBufferedImage();
 *
 * // 高级图像处理
 * ImageEditor.of(new File("input.jpg"))
 *     .scaleByWidth(500)
 *     .blur(2.0f)
 *     .brightness(0.1f)
 *     .contrast(0.2f)
 *     .toFile(new File("output.jpg"));
 * }</pre>
 *
 * <p><b>方法调用顺序：</b></p>
 * <ol>
 *   <li>使用静态工厂方法创建实例：{@code ImageEditor.of(...)}</li>
 *   <li>配置缩放操作：{@link #scaleByWidth(int)}、{@link #scaleByHeight(int)} 或 {@link #resize(int, int)}</li>
 *   <li>（可选）配置输出格式：{@link #outputFormat(String)}</li>
 *   <li>（可选）配置滤波器：{@link #scaleFilterType(int)} 或 {@link #scaleHints(int)}</li>
 *   <li>（可选）应用图像处理：{@link #rotate(int)}、{@link #grayscale()}、{@link #blur()}、{@link #brightness(float)}、{@link #contrast(float)} 等</li>
 *   <li>输出结果：{@link #toFile(File)}、{@link #toOutputStream(OutputStream)}、{@link #toBufferedImage()} 等</li>
 * </ol>
 *
 * <p><b>注意事项：</b></p>
 * <ul>
 *   <li>默认使用{@link ResampleOp#FILTER_LANCZOS Lanczos 插值（高质量）滤波器}</li>
 *   <li>默认输出格式根据输入图像是否有透明通道自动选择（PNG或JPEG）</li>
 *   <li>不支持透明的格式（如JPEG）会自动转换为RGB模式</li>
 *   <li>可以使用{@link #restore()}方法恢复到原始图像状态</li>
 *   <li>处理大量图像时，建议在使用完毕后显式关闭相关资源</li>
 *   <li>图像处理操作会按照调用顺序依次应用</li>
 * </ul>
 *
 * @author pangju666
 * @see ResampleOp
 * @see ImageSize
 * @see ImageUtil
 * @see BrightnessContrastFilter
 * @see GrayFilter
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
	 * 可通过{@link #scaleFilterType(int)}或{@link #scaleHints(int)}方法修改。
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
		Validate.notNull(url, "url不可为空");

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
	 * @since 1.0.0
	 * @see io.github.pangju666.commons.image.utils.ImageUtils#getExifOrientation(java.io.File)
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
		Validate.notNull(inputStream, "inputStream不可为空");

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
		Validate.notNull(imageInputStream, "imageInputStream不可为空");

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
		Validate.notNull(bufferedImage, "bufferedImage不可为空");

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
	public ImageEditor scaleFilterType(final int filterType) {
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
		Validate.notNull(filter, "filter不可为空");

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
		Validate.notNull(size, "size不可为空");

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
	 * @param factor 缩放比例
	 * @return 当前缩略图处理器实例，用于链式调用
	 * @since 1.0.0
	 */
	public ImageEditor scale(final double factor) {
		this.outputImageSize = this.outputImageSize.scale(factor);
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
		Validate.notNull(outputStream, "outputStream不可为空");

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
		Validate.notNull(imageOutputStream, "imageOutputStream不可为空");

		return ImageIO.write(this.outputImage, this.outputFormat, imageOutputStream);
	}

	/**
	 * 获取处理后图像的副本。
	 *
	 * @return 处理后图像的BufferedImage副本
	 * @since 1.0.0
	 */
	public BufferedImage toBufferedImage() {
		return ImageUtil.createCopy(this.outputImage);
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
}