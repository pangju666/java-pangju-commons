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

import com.twelvemonkeys.image.ResampleOp;
import io.github.pangju666.commons.image.lang.ImageConstants;
import io.github.pangju666.commons.image.model.ImageSize;
import io.github.pangju666.commons.io.utils.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * 缩略图生成器（链式调用风格）
 * <p>
 * 本类提供了流式API来创建图像缩略图，支持链式方法调用以配置各种参数。
 * </p>
 *
 * <p><b>核心特性：</b></p>
 * <ul>
 *   <li>链式调用API，支持流畅的方法调用</li>
 *   <li>支持强制缩放和等比缩放两种模式</li>
 *   <li>自动处理透明通道和图像格式转换</li>
 *   <li>支持多种输入源：URL、文件、流、BufferedImage</li>
 *   <li>支持多种输出目标：文件、流、BufferedImage</li>
 *   <li>可自定义重采样滤波器</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>{@code
 * // 基本用法：等比缩放到宽度200px
 * Thumbnails.of(new File("input.jpg"))
 *     .width(200)
 *     .scale()
 *     .toFile(new File("output.jpg"));
 *
 * // 强制缩放到指定尺寸
 * Thumbnails.of(bufferedImage)
 *     .imageSize(new ImageSize(300, 200))
 *     .outputFormat("png")
 *     .forceScale()
 *     .toOutputStream(outputStream);
 *
 * // 自定义滤波器
 * Thumbnails.of(url)
 *     .width(400)
 *     .filterType(ResampleOp.FILTER_BICUBIC)
 *     .scale()
 *     .toBufferedImage();
 * }</pre>
 *
 * <p><b>方法调用顺序：</b></p>
 * <ol>
 *   <li>使用静态工厂方法创建实例：{@code Thumbnails.of(...)}</li>
 *   <li>配置目标尺寸：{@link #width(int)}、{@link #height(int)} 或 {@link #imageSize(ImageSize)}</li>
 *   <li>（可选）配置输出格式：{@link #outputFormat(String)}</li>
 *   <li>（可选）配置滤波器：{@link #filterType(int)}</li>
 *   <li>执行缩放：{@link #forceScale()} 或 {@link #scale()}</li>
 *   <li>输出结果：{@link #toFile(File)}、{@link #toOutputStream(OutputStream)} 等</li>
 * </ol>
 *
 * <p><b>注意事项：</b></p>
 * <ul>
 *   <li>默认使用{@link ResampleOp#FILTER_TRIANGLE 三角形滤波器}</li>
 *   <li>默认输出格式根据输入图像是否有透明通道自动选择（PNG或JPEG）</li>
 *   <li>必须在输出前调用{@link #forceScale()}或{@link #scale()}</li>
 *   <li>不支持透明的格式（如JPEG）会自动转换为RGB模式</li>
 * </ul>
 *
 * @author pangju666
 * @see ResampleOp
 * @see ImageSize
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
	 * 原始输入图像
	 * <p>
	 * 存储从各种来源（文件、URL、流等）加载的原始图像数据。
	 * 该属性为final，在对象创建后不可更改。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private final BufferedImage inputImage;

	/**
	 * 原始图像尺寸
	 * <p>
	 * 存储输入图像的原始宽度和高度信息。
	 * 该属性为final，在对象创建后不可更改。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private final ImageSize inputImageSize;

	/**
	 * 处理后的输出图像
	 * <p>
	 * 存储经过缩放处理后的图像数据，初始值为输入图像。
	 * 在调用{@link #scale()}或{@link #forceScale()}方法后被更新。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private BufferedImage outputImage;

	/**
	 * 等比缩放后的输出尺寸
	 * <p>
	 * 存储保持原始宽高比的情况下计算出的输出尺寸。
	 * 在调用{@link #width(int)}、{@link #height(int)}或{@link #imageSize(ImageSize)}方法后被更新。
	 * 用于{@link #scale()}方法中的等比缩放。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private ImageSize outputImageSize;

	/**
	 * 目标图像尺寸
	 * <p>
	 * 存储用户指定的目标尺寸，可能不保持原始宽高比。
	 * 在调用{@link #width(int)}、{@link #height(int)}或{@link #imageSize(ImageSize)}方法后被更新。
	 * 用于{@link #forceScale()}方法中的强制缩放。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private ImageSize targetImageSize;

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
	 * 默认使用{@link ResampleOp#FILTER_TRIANGLE 三角形滤波器}。
	 * 可通过{@link #filterType(int)}方法修改。
	 * </p>
	 * @see ResampleOp 支持的滤波器类型常量
	 *
	 * @since 1.0.0
	 */
	private int filterType = ResampleOp.FILTER_TRIANGLE;

	/**
	 * 构造缩略图生成器（自动选择输出格式）
	 * <p>
	 * 根据输入图像是否包含Alpha通道自动选择输出格式：
	 * <ul>
	 *   <li>有透明通道 → PNG格式</li>
	 *   <li>无透明通道 → JPEG格式</li>
	 * </ul>
	 * </p>
	 *
	 * @param inputImage     原始图像
	 * @param inputImageSize 原始图像尺寸
	 * @since 1.0.0
	 */
	protected Thumbnails(BufferedImage inputImage, ImageSize inputImageSize) {
		this(inputImage, inputImageSize, inputImage.getColorModel().hasAlpha() ?
			DEFAULT_ALPHA_OUTPUT_FORMAT : DEFAULT_OUTPUT_FORMAT);
	}

	/**
	 * 构造缩略图生成器（指定输出格式）
	 *
	 * @param inputImage     原始图像，不可为null
	 * @param inputImageSize 原始图像尺寸，不可为null
	 * @param outputFormat   输出图片格式，必须是支持的格式
	 * @throws IllegalArgumentException 当参数无效时抛出
	 * @since 1.0.0
	 */
	protected Thumbnails(BufferedImage inputImage, ImageSize inputImageSize, String outputFormat) {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.notNull(inputImageSize, "inputImageSize 不可为 null");
		Validate.isTrue(ImageConstants.getSupportWriteImageFormats().contains(outputFormat), "不支持输出该图像格式");

		this.inputImage = inputImage;
		this.inputImageSize = inputImageSize;

		this.targetImageSize = inputImageSize;
		this.outputImageSize = inputImageSize;
		this.outputImage = inputImage;
		this.outputFormat = StringUtils.isBlank(outputFormat) ? "jpeg" : outputFormat.toLowerCase();
	}

	/**
	 * 从URL创建缩略图生成器
	 *
	 * @param url 图像URL，不可为null
	 * @return 缩略图生成器实例
	 * @throws IOException 当读取图像失败时抛出
	 * @since 1.0.0
	 */
	public static Thumbnails of(URL url) throws IOException {
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
	public static Thumbnails of(File file) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(file);
		return new Thumbnails(bufferedImage, new ImageSize(bufferedImage.getWidth(),
			bufferedImage.getHeight()), FilenameUtils.getExtension(file.getName()));
	}

	/**
	 * 从输入流创建缩略图生成器
	 *
	 * @param inputStream 输入流，不可为null
	 * @return 缩略图生成器实例
	 * @throws IOException 当读取图像失败时抛出
	 * @since 1.0.0
	 */
	public static Thumbnails of(InputStream inputStream) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		return new Thumbnails(bufferedImage, new ImageSize(bufferedImage.getWidth(), bufferedImage.getHeight()));
	}

	/**
	 * 从图像输入流创建缩略图生成器
	 *
	 * @param imageInputStream 图像输入流，不可为null
	 * @return 缩略图生成器实例
	 * @throws IOException 当读取图像失败时抛出
	 * @since 1.0.0
	 */
	public static Thumbnails of(ImageInputStream imageInputStream) throws IOException {
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
	public static Thumbnails of(BufferedImage bufferedImage) {
		return new Thumbnails(bufferedImage, new ImageSize(bufferedImage.getWidth(), bufferedImage.getHeight()));
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
	 * @param bufferedImage 原始图像
	 * @param imageSize     目标尺寸
	 * @param outputFormat  输出格式
	 * @param filterType    滤波器类型
	 * @return 重采样后的图像
	 * @since 1.0.0
	 */
	protected static BufferedImage resample(BufferedImage bufferedImage, ImageSize imageSize, String outputFormat, int filterType) {
		if (ImageConstants.NON_TRANSPARENT_IMAGE_FORMATS.contains(outputFormat)) {
			BufferedImage outputImage = new BufferedImage(imageSize.getWidth(), imageSize.getHeight(),
				BufferedImage.TYPE_INT_RGB);
			ResampleOp resampleOp = new ResampleOp(imageSize.getWidth(), imageSize.getHeight(), filterType);
			resampleOp.filter(bufferedImage, outputImage);
			return outputImage;
		} else {
			ResampleOp resampleOp = new ResampleOp(imageSize.getWidth(), imageSize.getHeight(), filterType);
			return resampleOp.filter(bufferedImage, null);
		}
	}

	/**
	 * 设置目标宽度（保持高度不变）
	 * <p>
	 * 调用此方法后，实际输出尺寸将根据宽度等比缩放计算。
	 * 仅在调用{@link #scale()}时生效。
	 * </p>
	 *
	 * @param width 目标宽度，必须大于0
	 * @return 当前实例，支持链式调用
	 * @throws IllegalArgumentException 当宽度不大于0时抛出
	 * @since 1.0.0
	 */
	public Thumbnails width(int width) {
		Validate.isTrue(width > 0, "width 必须大于0");
		this.targetImageSize = new ImageSize(width, this.targetImageSize.getHeight());
		this.outputImageSize = this.inputImageSize.scaleByWidth(width);
		return this;
	}

	/**
	 * 设置目标高度（保持宽度不变）
	 * <p>
	 * 调用此方法后，实际输出尺寸将根据高度等比缩放计算。
	 * 仅在调用{@link #scale()}时生效。
	 * </p>
	 *
	 * @param height 目标高度，必须大于0
	 * @return 当前实例，支持链式调用
	 * @throws IllegalArgumentException 当高度不大于0时抛出
	 * @since 1.0.0
	 */
	public Thumbnails height(int height) {
		Validate.isTrue(height > 0, "height 必须大于0");
		this.targetImageSize = new ImageSize(this.targetImageSize.getWidth(), height);
		this.outputImageSize = this.inputImageSize.scaleByHeight(height);
		return this;
	}

	/**
	 * 设置目标尺寸
	 * <p>
	 * 调用此方法后，实际输出尺寸将根据目标尺寸等比缩放计算。
	 * 仅在调用{@link #scale()}时生效。
	 * </p>
	 *
	 * @param imageSize 目标尺寸，不可为null
	 * @return 当前实例，支持链式调用
	 * @throws IllegalArgumentException 当imageSize为null时抛出
	 * @since 1.0.0
	 */
	public Thumbnails imageSize(ImageSize imageSize) {
		Validate.notNull(imageSize, "imageSize 不可为 null");
		this.targetImageSize = imageSize;
		this.outputImageSize = this.inputImageSize.scale(imageSize);
		return this;
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
	public Thumbnails filterType(int filterType) {
		this.filterType = filterType;
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
	public Thumbnails outputFormat(String outputFormat) {
		Validate.isTrue(ImageConstants.getSupportWriteImageFormats().contains(outputFormat), "不支持输出该图像格式");
		this.outputFormat = outputFormat;
		return this;
	}

	/**
	 * 执行强制缩放（不保持宽高比）
	 * <p>
	 * 将图像强制缩放到{@link #targetImageSize}指定的尺寸，
	 * 不考虑原始宽高比，可能导致图像变形。
	 * </p>
	 *
	 * <p><b>使用场景：</b></p>
	 * <ul>
	 *   <li>需要精确的输出尺寸</li>
	 *   <li>不关心图像比例失真</li>
	 *   <li>填充固定尺寸的容器</li>
	 * </ul>
	 *
	 * @return 当前实例，支持链式调用
	 * @since 1.0.0
	 */
	public Thumbnails forceScale() {
		this.outputImage = resample(this.inputImage, this.targetImageSize, this.outputFormat, this.filterType);
		return this;
	}

	/**
	 * 执行等比缩放（保持宽高比）
	 * <p>
	 * 将图像等比缩放到{@link #outputImageSize}指定的尺寸，
	 * 保持原始宽高比，不会导致图像变形。
	 * </p>
	 *
	 * <p><b>使用场景：</b></p>
	 * <ul>
	 *   <li>需要保持图像原始比例</li>
	 *   <li>避免图像失真</li>
	 *   <li>自适应缩放</li>
	 * </ul>
	 *
	 * @return 当前实例，支持链式调用
	 * @since 1.0.0
	 */
	public Thumbnails scale() {
		this.outputImage = resample(this.inputImage, this.outputImageSize, this.outputFormat, this.filterType);
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
	public boolean toFile(File outputFile) throws IOException {
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
	public boolean toOutputStream(OutputStream outputStream) throws IOException {
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
	public boolean toImageOutputStream(ImageOutputStream imageOutputStream) throws IOException {
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
		return outputImage;
	}
}