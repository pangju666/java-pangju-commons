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
 * 缩略图生成器（链式调用风格）
 * <p>
 * 本类提供了流式API来创建和处理图像缩略图，支持链式方法调用以配置各种参数。
 * </p>
 *
 * <p><b>核心特性：</b></p>
 * <ul>
 *   <li>链式调用API，支持流畅的方法调用</li>
 *   <li>支持多种缩放模式：强制缩放、按宽度缩放、按高度缩放等</li>
 *   <li>自动处理透明通道和图像格式转换</li>
 *   <li>支持多种输入源：URL、文件、流、BufferedImage</li>
 *   <li>支持多种输出目标：文件、流、BufferedImage</li>
 *   <li>可自定义重采样滤波器</li>
 *   <li>支持图像旋转、翻转、模糊、锐化等处理</li>
 *   <li>支持亮度、对比度调整和灰度转换</li>
 *   <li>自动根据EXIF信息校正图像方向</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>{@code
 * // 基本用法：按宽度等比缩放
 * Thumbnails.of(new File("input.jpg"))
 *     .scaleByWidth(200)
 *     .toFile(new File("output.jpg"));
 *
 * // 强制缩放到指定尺寸
 * Thumbnails.of(bufferedImage)
 *     .forceScale(300, 200)
 *     .outputFormat("png")
 *     .toOutputStream(outputStream);
 *
 * // 图像处理链
 * Thumbnails.of(url)
 *     .scaleByWidth(400)
 *     .scaleFilterType(ResampleOp.FILTER_LANCZOS)
 *     .rotate(90)
 *     .grayscale()
 *     .contrast(0.2f)
 *     .toBufferedImage();
 * }</pre>
 *
 * <p><b>方法调用顺序：</b></p>
 * <ol>
 *   <li>使用静态工厂方法创建实例：{@code Thumbnails.of(...)}</li>
 *   <li>配置缩放操作：{@link #scaleByWidth(int)}、{@link #scaleByHeight(int)} 或 {@link #forceScale(int, int)}</li>
 *   <li>（可选）配置输出格式：{@link #outputFormat(String)}</li>
 *   <li>（可选）配置滤波器：{@link #scaleFilterType(int)} 或 {@link #scaleHints(int)}</li>
 *   <li>（可选）应用图像处理：{@link #rotate(int)}、{@link #grayscale()}、{@link #blur()} 等</li>
 *   <li>输出结果：{@link #toFile(File)}、{@link #toOutputStream(OutputStream)} 等</li>
 * </ol>
 *
 * <p><b>注意事项：</b></p>
 * <ul>
 *   <li>默认使用{@link ResampleOp#FILTER_LANCZOS Lanczos 插值（高质量）滤波器}</li>
 *   <li>默认输出格式根据输入图像是否有透明通道自动选择（PNG或JPEG）</li>
 *   <li>不支持透明的格式（如JPEG）会自动转换为RGB模式</li>
 *   <li>可以使用{@link #restore()}方法恢复到原始图像状态</li>
 * </ul>
 *
 * @author pangju666
 * @see ResampleOp
 * @see ImageSize
 * @see ImageUtil
 * @since 1.0.0
 */
public class Thumbnails {
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
	 * 构造缩略图生成器（指定输出格式）
	 *
	 * @param inputImage     原始图像，不可为null
	 * @param inputImageSize 原始图像尺寸，不可为null
	 * @throws IllegalArgumentException 当参数无效时抛出
	 * @since 1.0.0
	 */
	protected Thumbnails(final BufferedImage inputImage, final ImageSize inputImageSize) {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.notNull(inputImageSize, "inputImageSize 不可为 null");

		this.inputImage = inputImage;
		this.inputImageSize = inputImageSize;

		this.outputImage = inputImage;
		this.outputImageSize = inputImageSize;
		this.outputFormat = inputImage.getColorModel().hasAlpha() ? DEFAULT_ALPHA_OUTPUT_FORMAT : DEFAULT_OUTPUT_FORMAT;
	}

	/**
	 * 从URL创建缩略图生成器
	 *
	 * @param url 图像URL，不可为null
	 * @return 缩略图生成器实例
	 * @throws IOException 当读取图像失败时抛出
	 * @since 1.0.0
	 */
	public static Thumbnails of(final URL url) throws IOException {
		Validate.notNull(url, "url不可为空");

		BufferedImage bufferedImage = ImageIO.read(url);
		return new Thumbnails(bufferedImage, new ImageSize(bufferedImage.getWidth(), bufferedImage.getHeight()));
	}

	/**
	 * 从文件创建缩略图生成器
	 * <p>
	 * 自动从文件扩展名推断输出格式。
	 * </p>
	 *
	 * @param file 图像文件，不可为null
	 * @return 缩略图生成器实例
	 * @throws IOException 当读取图像失败时抛出
	 * @since 1.0.0
	 */
	public static Thumbnails of(final File file) throws IOException, ImageProcessingException {
		return of(file, true);
	}

	/**
	 * 从文件创建缩略图生成器
	 * <p>
	 * 自动从文件扩展名推断输出格式。
	 * </p>
	 *
	 * @param file 图像文件，不可为null
	 * @return 缩略图生成器实例
	 * @throws IOException 当读取图像失败时抛出
	 * @since 1.0.0
	 */
	public static Thumbnails of(final File file, final boolean autoCorrectOrientation) throws IOException, ImageProcessingException {
		FileUtils.checkFile(file, "file 不可为 null");

		BufferedImage bufferedImage = ImageIO.read(file);
		Thumbnails thumbnails = new Thumbnails(bufferedImage, new ImageSize(bufferedImage.getWidth(),
			bufferedImage.getHeight()));
		thumbnails.outputFormat(FilenameUtils.getExtension(file.getName()));
		if (autoCorrectOrientation) {
			thumbnails.correctOrientation(ImageUtils.getExifOrientation(file));
		}
		return thumbnails;
	}

	/**
	 * 从输入流创建缩略图生成器
	 *
	 * @param inputStream 输入流，不可为null
	 * @return 缩略图生成器实例
	 * @throws IOException 当读取图像失败时抛出
	 * @since 1.0.0
	 */
	public static Thumbnails of(final InputStream inputStream) throws IOException, ImageProcessingException {
		return of(inputStream, true);
	}

	/**
	 * 从输入流创建缩略图生成器
	 *
	 * @param inputStream 输入流，不可为null
	 * @return 缩略图生成器实例
	 * @throws IOException 当读取图像失败时抛出
	 * @since 1.0.0
	 */
	public static Thumbnails of(final InputStream inputStream, final boolean autoCorrectOrientation) throws IOException,
		ImageProcessingException {
		Validate.notNull(inputStream, "inputStream不可为空");

		Thumbnails thumbnails;
		if (inputStream instanceof ByteArrayInputStream || inputStream instanceof UnsynchronizedByteArrayInputStream) {
			BufferedImage bufferedImage = ImageIO.read(inputStream);
			thumbnails = new Thumbnails(bufferedImage, new ImageSize(
				bufferedImage.getWidth(), bufferedImage.getHeight()));

			if (autoCorrectOrientation) {
				inputStream.reset();
				thumbnails.correctOrientation(ImageUtils.getExifOrientation(inputStream));
			}
		} else {
			UnsynchronizedByteArrayOutputStream outputStream = IOUtils.toUnsynchronizedByteArrayOutputStream(inputStream);

			try (InputStream tmpInputStream = outputStream.toInputStream()) {
				BufferedImage bufferedImage = ImageIO.read(tmpInputStream);
				thumbnails = new Thumbnails(bufferedImage, new ImageSize(
					bufferedImage.getWidth(), bufferedImage.getHeight()));
			}

			if (autoCorrectOrientation) {
				try (InputStream tmpInputStream = outputStream.toInputStream()) {
					thumbnails.correctOrientation(ImageUtils.getExifOrientation(tmpInputStream));
				}
			}
		}
		return thumbnails;
	}

	/**
	 * 从图像输入流创建缩略图生成器
	 *
	 * @param imageInputStream 图像输入流，不可为null
	 * @return 缩略图生成器实例
	 * @throws IOException 当读取图像失败时抛出
	 * @since 1.0.0
	 */
	public static Thumbnails of(final ImageInputStream imageInputStream) throws IOException {
		Validate.notNull(imageInputStream, "imageInputStream不可为空");

		BufferedImage bufferedImage = ImageIO.read(imageInputStream);
		return new Thumbnails(bufferedImage, new ImageSize(bufferedImage.getWidth(), bufferedImage.getHeight()));
	}

	/**
	 * 从BufferedImage创建缩略图生成器
	 *
	 * @param bufferedImage BufferedImage对象，不可为null
	 * @return 缩略图生成器实例
	 * @since 1.0.0
	 */
	public static Thumbnails of(final BufferedImage bufferedImage) {
		Validate.notNull(bufferedImage, "bufferedImage不可为空");

		return new Thumbnails(bufferedImage, new ImageSize(bufferedImage.getWidth(), bufferedImage.getHeight()));
	}

	/**
	 * 设置重采样滤波器类型
	 * <p>
	 * 可选的滤波器类型参见{@link ResampleOp}常量。
	 * </p>
	 *
	 * @param filterType 滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
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
	public Thumbnails scaleFilterType(final int filterType) {
		if (filterType > 15) {
			this.resampleFilterType = ResampleOp.FILTER_LANCZOS;
		} else {
			this.resampleFilterType = filterType;
		}
		return this;
	}

	public Thumbnails scaleHints(final int hints) {
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
	 * 设置输出图片格式
	 *
	 * @param outputFormat 输出格式（如"png"、"jpeg"等），必须是支持的格式
	 * @return 当前实例，支持链式调用
	 * @throws IllegalArgumentException 当格式不支持时抛出
	 * @since 1.0.0
	 */
	public Thumbnails outputFormat(final String outputFormat) {
		Validate.isTrue(ImageConstants.getSupportWriteImageFormats().contains(outputFormat), "不支持输出该图像格式");
		this.outputFormat = outputFormat;
		return this;
	}

	/**
	 * 根据EXIF方向信息校正图像方向
	 * <p>
	 * 根据EXIF元数据中的方向信息自动旋转或翻转图像，使其显示正确的方向。
	 * </p>
	 *
	 * @param orientation EXIF方向值（1-8）
	 * @return 当前实例，支持链式调用
	 * @since 1.0.0
	 */
	public Thumbnails correctOrientation(int orientation) {
		if (orientation < 1 || orientation > 8) {
			return this;
		}

		switch (orientation) {
			case 2:
				flip(ImageUtil.FLIP_VERTICAL);
				break;
			case 3:
				rotate(ImageUtil.ROTATE_180);
				break;
			case 4:
				flip(ImageUtil.FLIP_HORIZONTAL);
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
	 * 旋转图像（按预定义方向）
	 * <p>
	 * 将图像按指定的预定义方向旋转。
	 * </p>
	 *
	 * @param direction 旋转方向，使用{@link ImageUtil}中的常量：
	 *                 {@link ImageUtil#ROTATE_90_CW}、
	 *                 {@link ImageUtil#ROTATE_90_CCW}或
	 *                 {@link ImageUtil#ROTATE_180}
	 * @return 当前实例，支持链式调用
	 * @since 1.0.0
	 */
	public Thumbnails rotate(final int direction) {
		if (direction == ImageUtil.ROTATE_90_CW || direction == ImageUtil.ROTATE_90_CCW || direction == ImageUtil.ROTATE_180) {
			this.outputImage = ImageUtil.createRotated(this.outputImage, direction);
		}
		return this;
	}

	/**
	 * 旋转图像（按指定角度）
	 * <p>
	 * 将图像按指定的角度旋转。
	 * </p>
	 *
	 * @param angle 旋转角度（度），正值表示顺时针旋转，负值表示逆时针旋转
	 * @return 当前实例，支持链式调用
	 * @since 1.0.0
	 */
	public Thumbnails rotate(final double angle) {
		this.outputImage = ImageUtil.createRotated(this.outputImage, angle);
		return this;
	}

	/**
	 * 模糊图像（使用默认半径）
	 * <p>
	 * 对图像应用高斯模糊效果，使用默认模糊半径1.5。
	 * </p>
	 *
	 * @return 当前实例，支持链式调用
	 * @since 1.0.0
	 */
	public Thumbnails blur() {
		this.outputImage = ImageUtil.blur(this.outputImage, 1.5f);
		return this;
	}

	/**
	 * 模糊图像（指定模糊半径）
	 * <p>
	 * 对图像应用高斯模糊效果，使用指定的模糊半径。
	 * 半径越大，模糊效果越强。
	 * </p>
	 *
	 * @param radius 模糊半径，值越大模糊效果越强
	 * @return 当前实例，支持链式调用
	 * @since 1.0.0
	 */
	public Thumbnails blur(final float radius) {
		this.outputImage = ImageUtil.blur(this.outputImage, radius);
		return this;
	}

	/**
	 * 翻转图像
	 * <p>
	 * 沿指定轴翻转图像（水平或垂直）。
	 * </p>
	 *
	 * @param axis 翻转轴，使用{@link ImageUtil}中的常量：
	 *            {@link ImageUtil#FLIP_HORIZONTAL}或
	 *            {@link ImageUtil#FLIP_VERTICAL}
	 * @return 当前实例，支持链式调用
	 * @since 1.0.0
	 */
	public Thumbnails flip(final int axis) {
		if (axis == ImageUtil.FLIP_HORIZONTAL || axis == ImageUtil.FLIP_VERTICAL) {
			this.outputImage = ImageUtil.createFlipped(this.outputImage, axis);
		}
		return this;
	}

	/**
	 * 锐化图像（使用默认强度）
	 * <p>
	 * 对图像应用锐化效果，使用默认锐化强度。
	 * </p>
	 *
	 * @return 当前实例，支持链式调用
	 * @since 1.0.0
	 */
	public Thumbnails sharpen() {
		this.outputImage = ImageUtil.sharpen(this.outputImage);
		return this;
	}

	/**
	 * 锐化图像（指定锐化强度）
	 * <p>
	 * 对图像应用锐化效果，使用指定的锐化强度。
	 * 强度越大，锐化效果越明显。
	 * </p>
	 *
	 * @param amount 锐化强度，值越大锐化效果越强
	 * @return 当前实例，支持链式调用
	 * @since 1.0.0
	 */
	public Thumbnails sharpen(final float amount) {
		this.outputImage = ImageUtil.sharpen(this.outputImage, amount);
		return this;
	}

	/**
	 * 将图像转换为灰度
	 * <p>
	 * 将彩色图像转换为灰度图像。
	 * </p>
	 *
	 * @return 当前实例，支持链式调用
	 * @since 1.0.0
	 */
	public Thumbnails grayscale() {
		Image image = ImageUtil.filter(this.outputImage, GRAY_FILTER);
		this.outputImage = ImageUtil.toBuffered(image, this.outputImage.getType());
		return this;
	}

	/**
	 * 调整图像对比度（使用默认值）
	 * <p>
	 * 使用默认对比度值0.3调整图像对比度。
	 * </p>
	 *
	 * @return 当前实例，支持链式调用
	 * @since 1.0.0
	 */
	public Thumbnails contrast() {
		return contrast(0.3f);
	}

	/**
	 * 调整图像对比度（指定对比度值）
	 * <p>
	 * 使用指定的对比度值调整图像对比度。
	 * 值范围为-1.0到1.0，正值增加对比度，负值降低对比度。
	 * </p>
	 *
	 * @param amount 对比度调整值，范围为-1.0到1.0
	 * @return 当前实例，支持链式调用
	 * @since 1.0.0
	 */
	public Thumbnails contrast(final float amount) {
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
	 * 调整图像亮度
	 * <p>
	 * 使用指定的亮度值调整图像亮度。
	 * 值范围为-2.0到2.0，正值增加亮度，负值降低亮度。
	 * </p>
	 *
	 * @param amount 亮度调整值，范围为-2.0到2.0
	 * @return 当前实例，支持链式调用
	 * @since 1.0.0
	 */
	public Thumbnails brightness(final float amount) {
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

	public Thumbnails filter(final ImageFilter filter) {
		Validate.notNull(filter, "filter不可为空");

		Image image = ImageUtil.filter(this.outputImage, filter);
		this.outputImage = ImageUtil.toBuffered(image, this.outputImage.getType());
		return this;
	}

	public Thumbnails forceScale(final int width, final int height) {
		this.outputImageSize = new ImageSize(width, height);
		this.outputImage = resample();
		return this;
	}

	public Thumbnails forceScale(final ImageSize size) {
		Validate.notNull(size, "size不可为空");

		this.outputImageSize = size;
		this.outputImage = resample();
		return this;
	}

	public Thumbnails scaleByWidth(final int width) {
		this.outputImageSize = this.outputImageSize.scaleByWidth(width);
		this.outputImage = resample();
		return this;
	}

	public Thumbnails scaleByHeight(final int height) {
		this.outputImageSize = this.outputImageSize.scaleByHeight(height);
		this.outputImage = resample();
		return this;
	}

	public Thumbnails scale(final int width, final int height) {
		this.outputImageSize = this.outputImageSize.scale(width, height);
		this.outputImage = resample();
		return this;
	}

	/**
	 * 恢复到原始图像
	 * <p>
	 * 将当前处理的图像恢复到原始输入图像状态，
	 * 清除所有已应用的处理效果和参数设置。
	 * </p>
	 *
	 * @return 当前实例，支持链式调用
	 * @since 1.0.0
	 */
	public Thumbnails restore() {
		this.outputImage = this.inputImage;
		this.outputImageSize = this.inputImageSize;
		this.outputFormat = inputImage.getColorModel().hasAlpha() ? DEFAULT_ALPHA_OUTPUT_FORMAT : DEFAULT_OUTPUT_FORMAT;
		this.resampleFilterType = ResampleOp.FILTER_LANCZOS;
		return this;
	}

	/**
	 * 将缩放后的图像写入文件
	 *
	 * @param outputFile 输出文件，不可为null
	 * @return 是否成功写入
	 * @throws IOException 当写入失败时抛出
	 * @since 1.0.0
	 */
	public boolean toFile(final File outputFile) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		return ImageIO.write(this.outputImage, this.outputFormat, outputFile);
	}

	/**
	 * 将缩放后的图像写入输出流
	 *
	 * @param outputStream 输出流，不可为null
	 * @return 是否成功写入
	 * @throws IOException 当写入失败时抛出
	 * @since 1.0.0
	 */
	public boolean toOutputStream(final OutputStream outputStream) throws IOException {
		Validate.notNull(outputStream, "outputStream不可为空");

		return ImageIO.write(this.outputImage, this.outputFormat, outputStream);
	}

	/**
	 * 将缩放后的图像写入图像输出流
	 *
	 * @param imageOutputStream 图像输出流，不可为null
	 * @return 是否成功写入
	 * @throws IOException 当写入失败时抛出
	 * @since 1.0.0
	 */
	public boolean toImageOutputStream(final ImageOutputStream imageOutputStream) throws IOException {
		Validate.notNull(imageOutputStream, "imageOutputStream不可为空");

		return ImageIO.write(this.outputImage, this.outputFormat, imageOutputStream);
	}

	/**
	 * 获取缩放后的BufferedImage对象
	 * <p>
	 * 可用于进一步的图像处理或内存操作。
	 * </p>
	 *
	 * @return 缩放后的图像对象
	 * @since 1.0.0
	 */
	public BufferedImage toBufferedImage() {
		return ImageUtil.createCopy(this.outputImage);
	}

	/**
	 * 图像重采样方法（内部使用）
	 * <p>
	 * 根据输出格式自动处理透明通道：
	 * <ul>
	 *   <li>不支持透明的格式 → 转换为RGB模式</li>
	 *   <li>支持透明的格式 → 保留Alpha通道</li>
	 * </ul>
	 * </p>
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