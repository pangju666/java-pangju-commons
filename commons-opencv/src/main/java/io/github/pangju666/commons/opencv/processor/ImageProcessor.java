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

package io.github.pangju666.commons.opencv.processor;

import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.opencv.enums.FlipDirection;
import io.github.pangju666.commons.opencv.enums.RotateDirection;
import io.github.pangju666.commons.opencv.io.resource.OpencvImageResource;
import io.github.pangju666.commons.opencv.lang.OpencvConstants;
import io.github.pangju666.commons.opencv.model.ImageWatermarkOption;
import io.github.pangju666.commons.opencv.model.TextWatermarkOption;
import io.github.pangju666.commons.opencv.utils.OpencvUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.indexer.DoubleIndexer;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.function.Function;

/**
 * 基于 OpenCV 的图像处理器（链式调用风格）
 * <p>
 * 提供流式 API 以便对图像进行缩放、旋转、滤镜、亮度/对比度、灰度转换、透明度调整以及图片/文字水印等常见操作。<br />
 * 支持以 {@link OpencvImageResource}、输入流、{@link BytePointer} 或 {@link Mat} 作为输入源，并可输出为文件、输出流、字节数组或 {@link Mat}。<br />
 * 可选地根据 EXIF 信息自动校正图像方向（当 EXIF 不存在或读取失败时不进行校正）。
 * </p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li><strong>链式调用：</strong> API 设计简洁，配置与处理顺序清晰。</li>
 *   <li><strong>状态重置：</strong> 支持 {@link #reset()} 方法将图像恢复至初始状态，便于重复使用或撤销操作。</li>
 *   <li><strong>资源释放：</strong> 支持 {@link #release()} 方法释放图像资源，减少内存占用，释放后处理器不可再使用。</li>
 *   <li><strong>EXIF 支持：</strong> 支持自动解析 EXIF 校正方向。</li>
 *   <li><strong>自定义扩展：</strong> 通过 {@link #apply(Function)} 方法支持传入任意自定义图像转换函数，灵活扩展编辑功能。</li>
 *   <li><strong>丰富操作：</strong>
 *     <ul>
 *       <li>缩放：支持按宽/高、按比例、强制尺寸等多种模式。</li>
 *       <li>调整：旋转（支持固定方向和任意角度）、翻转、平移。</li>
 *       <li>调色：亮度、对比度、灰度化、透明度调整。</li>
 *       <li>特效：均值模糊、高斯模糊、中值模糊、锐化、浮雕、阈值、自适应阈值。</li>
 *       <li>水印：支持图片和文字水印，提供九宫格方向定位和自定义坐标两种方式。</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h3>线程安全</h3>
 * <ul>
 *   <li>本类 <strong>非线程安全</strong>。实例包含可变的图像状态（{@code outputImage} 等）。</li>
 *   <li>请确保每个线程使用独立的实例，不要在多线程间共享同一个实例。</li>
 * </ul>
 *
 * <h3>性能与内存</h3>
 * <ul>
 *   <li><strong>处理顺序：</strong> 建议先进行缩放或裁剪操作，再进行其他处理（如模糊、水印），以减少计算量和内存占用。</li>
 *   <li><strong>资源管理：</strong> 处理完成后建议调用 {@link #release()} 方法释放图像资源，减少内存占用。释放后处理器不可再使用。</li>
 * </ul>
 *
 * <h3>推荐方法调用顺序</h3>
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
 *   <li>滤镜/阈值</li>
 *   <li>添加水印</li>
 * </ol>
 *
 * <h3>代码示例</h3>
 * <pre>{@code
 * // 1. 构建实例
 * // 从 OpencvImageResource 构建
 * ImageProcessor.of(new OpencvImageResource(new File("input.jpg")));
 * // 从输入流构建
 * ImageProcessor.of(inputStream);
 * // 从 BytePointer 构建
 * ImageProcessor.of(bytePointer);
 * // 从 Mat 构建
 * ImageProcessor.of(mat);
 *
 * // 2. 缩放与调整大小
 * ImageProcessor.of(new OpencvImageResource(new File("input.jpg")))
 *     .scaleByWidth(800)              // 按宽度等比缩放
 *     .scaleByHeight(600)             // 按高度等比缩放
 *     .scale(0.5)                     // 按比例缩放（50%）
 *     .scale(800, 600)                // 按目标尺寸等比缩放（不超出）
 *     .resize(100, 100)               // 强制缩放到指定尺寸（不保持比例）
 *     .toFile(new File("out_scale.jpg"));
 *
 * // 3. 裁剪操作
 * ImageProcessor.of(new OpencvImageResource(new File("input.jpg")))
 *     .cropByCenter(400, 400)         // 居中裁剪
 *     .cropByRect(0, 0, 200, 200)     // 指定矩形区域裁剪
 *     .cropByOffset(10, 10, 20, 20)   // 按边距裁剪（上、下、左、右）
 *     .toFile(new File("out_crop.jpg"));
 *
 * // 4. 旋转与翻转
 * ImageProcessor.of(new OpencvImageResource(new File("input.jpg")))
 *     .rotate(RotateDirection.CLOCKWISE_90)  // 顺时针旋转 90 度
 *     .rotate(45)                            // 任意角度旋转（45度）
 *     .flip(FlipDirection.HORIZONTAL)        // 水平翻转
 *     .toFile(new File("out_rotate.jpg"));
 *
 * // 5. 色彩与滤镜
 * ImageProcessor.of(new OpencvImageResource(new File("input.jpg")))
 *     .grayscale()                    // 转为灰度图
 *     .blur()                         // 均值模糊（默认尺寸）
 *     .gaussianBlur()                 // 高斯模糊（默认尺寸）
 *     .medianBlur(5)                  // 中值模糊
 *     .sharpen()                      // 锐化
 *     .emboss()                       // 浮雕效果
 *     .contrast(0.2f)                 // 增加对比度
 *     .brightness(20)                 // 增加亮度
 *     .transparency(0.5f)             // 调整透明度为 50%
 *     .threshold()                    // 自适应阈值
 *     .toFile(new File("out_filter.jpg"));
 *
 * // 6. 水印添加（支持图片与文字）
 * ImageProcessor.of(new OpencvImageResource(new File("input.jpg")))
 *     .addTextWatermark("CONFIDENTIAL", new TextWatermarkOption())
 *     .addImageWatermark(new File("logo.png"), new ImageWatermarkOption())
 *     .toFile(new File("out_watermark.jpg"));
 *
 * // 7. 复杂操作链（链式调用）
 * ImageProcessor.of(new OpencvImageResource(new File("input.jpg")))
 *     .cropByCenter(1000, 1000)       // 1. 先裁剪中心 1000x1000 区域
 *     .scaleByWidth(500)              // 2. 缩放到宽度 500px
 *     .gaussianBlur()                 // 3. 应用高斯模糊
 *     .addTextWatermark("PREVIEW", new TextWatermarkOption()) // 4. 添加水印
 *     .toFile(new File("processed.jpg"));
 *
 * // 8. 状态重置与多版本输出
 * ImageProcessor editor = ImageProcessor.of(new OpencvImageResource(new File("original.png")));
 * // 输出缩略图
 * editor.scaleByWidth(200)
 *       .toFile(new File("thumbnail.png"));
 * // 重置并输出带水印的高清图
 * editor.reset()
 *       .addTextWatermark("CONFIDENTIAL", new TextWatermarkOption())
 *       .toFile(new File("watermarked_original.png"));
 *
 * // 9. 使用后释放资源
 * editor.release();
 * }</pre>
 *
 * @author pangju666
 * @see RotateDirection
 * @see FlipDirection
 * @see io.github.pangju666.commons.opencv.model.ImageWatermarkOption
 * @see io.github.pangju666.commons.opencv.model.TextWatermarkOption
 * @see OpencvUtils
 * @since 1.1.0
 */
public class ImageProcessor {
	/**
	 * 默认的带透明通道图像输出格式
	 * <p>
	 * 当输入图像包含透明通道（Alpha通道）时，默认使用的输出格式。
	 * PNG格式支持透明度，适合保留原图像的透明效果。
	 * </p>
	 *
	 * @since 1.1.0
	 */
	protected static final String DEFAULT_ALPHA_OUTPUT_FORMAT = "png";

	/**
	 * 默认的标准图像输出格式
	 * <p>
	 * 当输入图像不包含透明通道时，默认使用的输出格式。
	 * JPG格式具有较高的压缩率，适合不需要透明效果的图像。
	 * </p>
	 *
	 * @since 1.1.0
	 */
	protected static final String DEFAULT_OUTPUT_FORMAT = "jpg";

	/**
	 * 输入图像格式
	 *
	 * @since 1.1.0
	 */
	protected final String inputFormat;
	/**
	 * 原始输入图像，用于重置操作
	 *
	 * @since 1.1.0
	 */
	protected final Mat inputImage;

	/**
	 * 输出图像格式
	 * <p>
	 * 图像编码输出时使用的格式，默认为 inputFormat，
	 * 如果 inputFormat 为空，则根据图像是否有透明通道选择 PNG 或 JPG
	 * </p>
	 *
	 * @since 1.1.0
	 */
	protected String outputFormat;

	/**
	 * 当前处理中的输出图像
	 *
	 * @since 1.1.0
	 */
	protected Mat outputImage;

	/**
	 * 内部构造函数，用于创建图像处理器
	 *
	 * <p>
	 * 会根据 flags 和 exifOrientation 决定是否自动校正图像方向，
	 * 并根据图像通道数设置默认的输出格式
	 * </p>
	 *
	 * @param inputImage      输入图像 Mat，不能为 null 或空
	 * @param exifOrientation EXIF 方向值（必须介于 1-8 之间）
	 * @param flags           图像读取标志（OpenCV 的 IMREAD_* 常量）
	 * @throws IllegalArgumentException 如果 inputImage 为 null 或空，或 exifOrientation 不在 1-8 范围内
	 * @since 1.1.0
	 */
	protected ImageProcessor(final Mat inputImage, final int exifOrientation, final int flags) {
		this(inputImage, exifOrientation, flags, null);
	}

	/**
	 * 内部构造函数，用于创建图像处理器
	 *
	 * <p>
	 * 会根据 flags 和 exifOrientation 决定是否自动校正图像方向，
	 * 并根据 inputFormat 或图像通道数设置默认的输出格式
	 * </p>
	 *
	 * @param inputImage      输入图像 Mat，不能为 null 或空
	 * @param exifOrientation EXIF 方向值
	 * @param flags           图像读取标志（OpenCV 的 IMREAD_* 常量）
	 * @param inputFormat     基于 MIME 类型推断的输入图像格式，可为 null
	 * @throws IllegalArgumentException 如果 inputImage 为 null 或空
	 * @since 1.1.0
	 */
	protected ImageProcessor(final Mat inputImage, final int exifOrientation, final int flags, final String inputFormat) {
		Validate.isTrue(OpencvUtils.isNotEmpty(inputImage), "inputImage 不存在图像数据");
		Validate.inclusiveBetween(1, 8, exifOrientation, "exifOrientation 必须介于1-8之间");

		this.inputImage = inputImage;
		this.inputFormat = StringUtils.defaultIfBlank(inputFormat, null);

		if ((flags == opencv_imgcodecs.IMREAD_UNCHANGED || flags == opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION) &&
			exifOrientation != OpencvConstants.NORMAL_EXIF_ORIENTATION) {
			this.outputImage = OpencvUtils.correctOrientation(this.inputImage, exifOrientation);
		} else {
			this.outputImage = inputImage.clone();
		}

		this.outputFormat = StringUtils.defaultIfBlank(inputFormat,
			outputImage.channels() == 4 ? DEFAULT_ALPHA_OUTPUT_FORMAT : DEFAULT_OUTPUT_FORMAT);
	}

	/**
	 * 从 OpencvImageResource 创建处理器
	 *
	 * <p>如果资源已校正 EXIF 方向，则使用校正后的图像；否则使用原始图像并根据 EXIF 方向值进行校正。</p>
	 *
	 * <p>会使用资源基于 MIME 类型推断的 format 作为输入图像格式</p>
	 *
	 * @param resource OpencvImageResource 对象，不能为 null
	 * @return 图像处理器实例
	 * @throws IOException              如果读取图像失败
	 * @throws IllegalArgumentException 如果 resource 为 null
	 * @since 1.1.0
	 */
	public static ImageProcessor of(final OpencvImageResource resource) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");

		if (resource.isOrientationCorrected()) {
			return new ImageProcessor(resource.getImageMat(),
				OpencvConstants.NORMAL_EXIF_ORIENTATION, resource.getFlags());
		}
		return new ImageProcessor(resource.getImageMat(), resource.getExifOrientation(), resource.getFlags(),
			resource.getFormat());
	}

	/**
	 * 从输入流创建处理器
	 * <p>使用 BGR 颜色模式读取图像，OpenCV 会在读取时自动进行 EXIF 方向校正。</p>
	 *
	 * <p><b>方向校正说明：</b></p>
	 * <ul>
	 *   <li>此方法使用 {@code IMREAD_COLOR_BGR} 标志读取图像，OpenCV 会自动进行 EXIF 方向校正</li>
	 *   <li>如需禁用自动校正，请使用 {@code IMREAD_UNCHANGED} 或 {@code IMREAD_IGNORE_ORIENTATION} 标志</li>
	 * </ul>
	 *
	 * @param inputStream 输入流，不能为 null
	 * @return 图像处理器实例
	 * @throws IOException 如果读取流失败
	 * @since 1.1.0
	 */
	public static ImageProcessor of(final InputStream inputStream) throws IOException {
		return of(inputStream, OpencvConstants.DEFAULT_IMAGE_COLOR_TYPE,
			OpencvConstants.NORMAL_EXIF_ORIENTATION);
	}

	/**
	 * 从输入流创建处理器，使用指定的读取标志
	 *
	 * <p><b>方向校正说明：</b></p>
	 * <ul>
	 *   <li>当 flags 为 {@code IMREAD_UNCHANGED} 或 {@code IMREAD_IGNORE_ORIENTATION} 时，OpenCV 不会自动校正方向，需要手动指定 EXIF 方向值</li>
	 *   <li>其他 flags（如 {@code IMREAD_COLOR_BGR}）时，OpenCV 会在读取时自动进行 EXIF 方向校正</li>
	 * </ul>
	 *
	 * @param inputStream 输入流，不能为 null
	 * @param flags       图像读取标志（OpenCV 的 IMREAD_* 常量）
	 * @return 图像处理器实例
	 * @throws IOException              如果读取流失败
	 * @throws IllegalArgumentException 如果 inputStream 为 null
	 * @since 1.1.0
	 */
	public static ImageProcessor of(final InputStream inputStream, final int flags) throws IOException {
		return of(inputStream, flags, OpencvConstants.NORMAL_EXIF_ORIENTATION);
	}

	/**
	 * 从输入流创建处理器，指定读取标志和 EXIF 方向值
	 *
	 * @param inputStream     输入流，不能为 null
	 * @param flags           图像读取标志（OpenCV 的 IMREAD_* 常量）
	 * @param exifOrientation EXIF 方向值（必须介于 1-8 之间）
	 * @return 图像处理器实例
	 * @throws IOException              如果读取流失败
	 * @throws IllegalArgumentException 如果 inputStream 为 null，或 exifOrientation 不在 1-8 范围内
	 * @since 1.1.0
	 */
	public static ImageProcessor of(final InputStream inputStream, final int flags, final int exifOrientation) throws IOException {
		Validate.inclusiveBetween(1, 8, exifOrientation, "exifOrientation 必须介于1-8之间");
		Validate.notNull(inputStream, "inputStream 不可为 null");

		Mat image = OpencvUtils.read(inputStream, flags);
		if (OpencvUtils.isEmpty(image)) {
			throw new IOException("图片读取失败");
		}
		return new ImageProcessor(image, exifOrientation, flags);
	}

	/**
	 * 从 BytePointer 创建处理器
	 * <p>使用 BGR 颜色模式读取图像，OpenCV 会在读取时自动进行 EXIF 方向校正。</p>
	 *
	 * <p><b>方向校正说明：</b></p>
	 * <ul>
	 *   <li>此方法使用 {@code IMREAD_COLOR_BGR} 标志读取图像，OpenCV 会自动进行 EXIF 方向校正</li>
	 *   <li>如需禁用自动校正，请使用 {@code IMREAD_UNCHANGED} 或 {@code IMREAD_IGNORE_ORIENTATION} 标志</li>
	 * </ul>
	 *
	 * @param bytePointer BytePointer 对象，不能为 null
	 * @return 图像处理器实例
	 * @throws IllegalArgumentException 如果 bytePointer 为 null 或解码失败
	 * @since 1.1.0
	 */
	public static ImageProcessor of(final BytePointer bytePointer) {
		return of(bytePointer, OpencvConstants.DEFAULT_IMAGE_COLOR_TYPE,
			OpencvConstants.NORMAL_EXIF_ORIENTATION);
	}

	/**
	 * 从 BytePointer 创建处理器，指定读取标志
	 *
	 * <p><b>方向校正说明：</b></p>
	 * <ul>
	 *   <li>当 flags 为 {@code IMREAD_UNCHANGED} 或 {@code IMREAD_IGNORE_ORIENTATION} 时，OpenCV 不会自动校正方向，需要手动指定 EXIF 方向值</li>
	 *   <li>其他 flags（如 {@code IMREAD_COLOR_BGR}）时，OpenCV 会在读取时自动进行 EXIF 方向校正</li>
	 * </ul>
	 *
	 * @param bytePointer BytePointer 对象，不能为 null
	 * @param flags       图像读取标志（OpenCV 的 IMREAD_* 常量）
	 * @return 图像处理器实例
	 * @throws IllegalArgumentException 如果 bytePointer 为 null 或解码失败
	 * @since 1.1.0
	 */
	public static ImageProcessor of(final BytePointer bytePointer, final int flags) {
		return of(bytePointer, flags, OpencvConstants.NORMAL_EXIF_ORIENTATION);
	}

	/**
	 * 从 BytePointer 创建处理器，指定读取标志和 EXIF 方向值
	 *
	 * @param bytePointer     BytePointer 对象，不能为 null 或空指针
	 * @param flags           图像读取标志（OpenCV 的 IMREAD_* 常量）
	 * @param exifOrientation EXIF 方向值（必须介于 1-8 之间）
	 * @return 图像处理器实例
	 * @throws IllegalArgumentException 如果 bytePointer 为 null 或空指针，或 exifOrientation 不在 1-8 范围内
	 * @since 1.1.0
	 */
	public static ImageProcessor of(final BytePointer bytePointer, final int flags, final int exifOrientation) {
		Validate.inclusiveBetween(1, 8, exifOrientation, "exifOrientation 必须介于1-8之间");
		Validate.notNull(bytePointer, "bytePointer不可为 null");
		Validate.isTrue(!bytePointer.isNull(), "bytePointer不可为 null");

		try (Mat bytesMat = new Mat(bytePointer)) {
			Mat imageMat = opencv_imgcodecs.imdecode(bytesMat, flags);
			return new ImageProcessor(imageMat, exifOrientation, flags);
		}
	}

	/**
	 * 从现有 Mat 图像创建处理器
	 *
	 * @param image 图像 Mat，不能为 null 或空
	 * @return 图像处理器实例
	 * @throws IllegalArgumentException 如果 image 为 null 或空
	 * @since 1.1.0
	 */
	public static ImageProcessor of(final Mat image) {
		return new ImageProcessor(image, OpencvConstants.NORMAL_EXIF_ORIENTATION,
			opencv_imgcodecs.IMREAD_UNCHANGED);
	}

	/**
	 * 从现有 Mat 图像创建处理器，指定 EXIF 方向值
	 *
	 * @param image           图像 Mat，不能为 null 或空
	 * @param exifOrientation EXIF 方向值（必须介于 1-8 之间）
	 * @return 图像处理器实例
	 * @throws IllegalArgumentException 如果 image 为 null 或空，或 exifOrientation 不在 1-8 范围内
	 * @since 1.1.0
	 */
	public static ImageProcessor of(final Mat image, final int exifOrientation) {
		return new ImageProcessor(image, exifOrientation, opencv_imgcodecs.IMREAD_UNCHANGED);
	}

	/**
	 * 设置图像的全局透明度
	 *
	 * <p>如果图像没有 Alpha 通道，会自动添加</p>
	 * <p><b>重要提示：</b>此方法只在内存中修改图像的透明度，最终效果取决于输出格式。
	 * 如果后续保存图像时使用不支持透明通道的格式（如 JPEG），则透明度效果会丢失。
	 * 建议使用支持透明通道的格式（如 PNG）保存图像。</p>
	 *
	 * @param alpha 透明度值，范围 0.0（完全透明）~ 1.0（完全不透明）
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果 alpha 超出 [0, 1] 范围
	 * @since 1.1.0
	 */
	public ImageProcessor transparency(final float alpha) {
		Validate.isTrue(alpha >= 0 && alpha <= 1, "alpha 必须大于等于 0 且小于等于 1");

		if (outputImage.channels() < 4) {
			Mat bgraMat = new Mat();
			int code;
			int type = outputImage.type();
			if (type == opencv_core.CV_8UC1 || type == opencv_core.CV_16UC1 || type == opencv_core.CV_32FC1) {
				code = opencv_imgproc.COLOR_GRAY2BGRA;
			} else {
				code = opencv_imgproc.COLOR_BGR2BGRA;
			}
			opencv_imgproc.cvtColor(outputImage, bgraMat, code);
			outputImage.releaseReference();
			outputImage = bgraMat;
		}

		MatVector channels = new MatVector(4);
		opencv_core.split(outputImage, channels);

		Mat alphaChannel = channels.get(3);
		alphaChannel.convertTo(alphaChannel, -1, alpha, 0); // scale=0.5, shift=0

		Mat image = new Mat();
		opencv_core.merge(channels, image);

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	/**
	 * 旋转图像（90度、180度、逆时针90度）
	 *
	 * @param direction 旋转方向枚举，不能为 null
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果 direction 为 null
	 * @since 1.1.0
	 */
	public ImageProcessor rotate(final RotateDirection direction) {
		Validate.notNull(direction, "direction 不可为 null");

		Mat image = new Mat();
		opencv_core.rotate(outputImage, image, direction.getCode());

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	/**
	 * 按任意角度旋转图像
	 *
	 * <p>旋转后的画布会自动调整大小，避免图像被裁切，图像会保持居中显示</p>
	 *
	 * @param angle 旋转角度（度），正数为顺时针，负数为逆时针
	 * @return 当前处理器实例，支持链式调用
	 * @since 1.1.0
	 */
	public ImageProcessor rotate(final double angle) {
		Size imageSize = outputImage.size();
		int width = imageSize.width();
		int height = imageSize.height();
		Point2f center = new Point2f((float) (width / 2.0), (float) (height / 2.0));
		Mat rotateMat = opencv_imgproc.getRotationMatrix2D(center, angle, 1.0);

		// 计算旋转后的画布宽高，避免图像被裁切
		double cos, sin;
		try (DoubleIndexer indexer = rotateMat.createIndexer()) {
			cos = Math.abs(indexer.get(0, 0));
			sin = Math.abs(indexer.get(0, 1));
		}

		int newWidth = (int) (height * sin + width * cos);
		int newHeight = (int) (height * cos + width * sin);

		// 偏移修正矩阵，保证内容居中
		try (DoubleIndexer indexer = rotateMat.createIndexer()) {
			indexer.put(0, 2, indexer.get(0, 2) + (newWidth / 2.0 - center.x()));
			indexer.put(1, 2, indexer.get(1, 2) + (newHeight / 2.0 - center.y()));
		}

		Mat newImage = new Mat(new Size(newWidth, newHeight), outputImage.type());
		Size newImageSize = new Size(newWidth, newHeight);
		// 仿射旋转，边界填充背景色
		opencv_imgproc.warpAffine(outputImage, newImage, rotateMat, newImageSize,
			opencv_imgproc.INTER_LINEAR, opencv_core.BORDER_CONSTANT,
			OpencvConstants.TRANSPARENT_COLOR);

		this.outputImage.releaseReference();
		this.outputImage = newImage;

		return this;
	}

	/**
	 * 翻转图像（水平或垂直）
	 *
	 * @param direction 翻转方向枚举，不能为 null
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果 direction 为 null
	 * @since 1.1.0
	 */
	public ImageProcessor flip(final FlipDirection direction) {
		Validate.notNull(direction, "direction 不可为 null");

		Mat image = new Mat();
		opencv_core.flip(outputImage, image, direction.getCode());

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	/**
	 * 平移图像
	 *
	 * @param dx 水平平移距离（像素）
	 * @param dy 垂直平移距离（像素）
	 * @return 当前处理器实例，支持链式调用
	 * @since 1.1.0
	 */
	public ImageProcessor warpAffine(final int dx, final int dy) {
		Mat image = new Mat();

		Mat matrixMat = OpencvUtils.getMatrixMat(dx, dy);
		opencv_imgproc.warpAffine(outputImage, image, matrixMat, outputImage.size(),
			opencv_imgproc.INTER_LINEAR, opencv_core.BORDER_CONSTANT,
			OpencvConstants.TRANSPARENT_COLOR);
		matrixMat.releaseReference();

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	/**
	 * 调整图像大小到指定尺寸
	 *
	 * @param width  目标宽度，必须大于 0
	 * @param height 目标高度，必须大于 0
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果 width 或 height 小于等于 0
	 * @since 1.1.0
	 */
	public ImageProcessor resize(final int width, final int height) {
		return resize(width, height, opencv_imgproc.INTER_LINEAR);
	}

	/**
	 * 调整图像大小到指定尺寸
	 *
	 * @param width         目标宽度，必须大于 0
	 * @param height        目标高度，必须大于 0
	 * @param interpolationFlag 插值滤波算法
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果 width 或 height 小于等于 0
	 * @see opencv_imgproc#INTER_NEAREST
	 * @see opencv_imgproc#INTER_LINEAR
	 * @see opencv_imgproc#INTER_CUBIC
	 * @see opencv_imgproc#INTER_AREA
	 * @see opencv_imgproc#INTER_LANCZOS4
	 * @see opencv_imgproc#INTER_LINEAR_EXACT
	 * @see opencv_imgproc#INTER_NEAREST_EXACT
	 * @see opencv_imgproc#INTER_MAX
	 * @since 1.1.0
	 */
	public ImageProcessor resize(final int width, final int height, final int interpolationFlag) {
		Validate.isTrue(width > 0, "width 必须大于 0");
		Validate.isTrue(height > 0, "height 必须大于 0");
		Validate.isTrue(interpolationFlag >= 0 && interpolationFlag <= 7, "interpolation 必须在0-7之间");

		Mat image = new Mat();
		opencv_imgproc.resize(outputImage, image, new Size(width, height), 0, 0, interpolationFlag);

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	/**
	 * 按目标宽度等比例缩放图像
	 *
	 * @param targetWidth 目标宽度，必须大于 0
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果 targetWidth 小于等于 0
	 * @since 1.1.0
	 */
	public ImageProcessor scaleByWidth(final int targetWidth) {
		Validate.isTrue(targetWidth > 0, "targetWidth 必须大于 0");

		Size size = OpencvUtils.scaleByWidth(outputImage.size(), targetWidth);
		return resize(size.width(), size.height());
	}

	/**
	 * 按目标宽度等比例缩放图像
	 *
	 * @param targetWidth   目标宽度，必须大于 0
	 * @param interpolationFlag 插值滤波算法
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果 targetWidth 小于等于 0
	 * @see opencv_imgproc#INTER_NEAREST
	 * @see opencv_imgproc#INTER_LINEAR
	 * @see opencv_imgproc#INTER_CUBIC
	 * @see opencv_imgproc#INTER_AREA
	 * @see opencv_imgproc#INTER_LANCZOS4
	 * @see opencv_imgproc#INTER_LINEAR_EXACT
	 * @see opencv_imgproc#INTER_NEAREST_EXACT
	 * @see opencv_imgproc#INTER_MAX
	 * @since 1.1.0
	 */
	public ImageProcessor scaleByWidth(final int targetWidth, final int interpolationFlag) {
		Validate.isTrue(targetWidth > 0, "targetWidth 必须大于 0");

		Size size = OpencvUtils.scaleByWidth(outputImage.size(), targetWidth);
		return resize(size.width(), size.height(), interpolationFlag);
	}

	/**
	 * 按目标高度等比例缩放图像
	 *
	 * @param targetHeight 目标高度，必须大于 0
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果 targetHeight 小于等于 0
	 * @since 1.1.0
	 */
	public ImageProcessor scaleByHeight(final int targetHeight) {
		Validate.isTrue(targetHeight > 0, "targetHeight 必须大于 0");

		Size size = OpencvUtils.scaleByHeight(outputImage.size(), targetHeight);
		return resize(size.width(), size.height());
	}

	/**
	 * 按目标高度等比例缩放图像
	 *
	 * @param targetHeight  目标高度，必须大于 0
	 * @param interpolationFlag 插值滤波算法
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果 targetHeight 小于等于 0
	 * @see opencv_imgproc#INTER_NEAREST
	 * @see opencv_imgproc#INTER_LINEAR
	 * @see opencv_imgproc#INTER_CUBIC
	 * @see opencv_imgproc#INTER_AREA
	 * @see opencv_imgproc#INTER_LANCZOS4
	 * @see opencv_imgproc#INTER_LINEAR_EXACT
	 * @see opencv_imgproc#INTER_NEAREST_EXACT
	 * @see opencv_imgproc#INTER_MAX
	 * @since 1.1.0
	 */
	public ImageProcessor scaleByHeight(final int targetHeight, final int interpolationFlag) {
		Validate.isTrue(targetHeight > 0, "targetHeight 必须大于 0");

		Size size = OpencvUtils.scaleByHeight(outputImage.size(), targetHeight);
		return resize(size.width(), size.height(), interpolationFlag);
	}


	/**
	 * 按比例因子缩放图像
	 *
	 * @param scalingFactor 缩放比例因子，必须大于 0（例如 0.5 为缩小 50%）
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果 scalingFactor 小于等于 0
	 * @since 1.1.0
	 */
	public ImageProcessor scale(final double scalingFactor) {
		Validate.isTrue(scalingFactor > 0, "scalingFactor 必须大于 0");

		Size size = OpencvUtils.scale(outputImage.size(), scalingFactor);
		return resize(size.width(), size.height());
	}

	/**
	 * 按比例因子缩放图像
	 *
	 * @param scalingFactor 缩放比例因子，必须大于 0（例如 0.5 为缩小 50%）
	 * @param interpolationFlag 插值滤波算法
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果 scalingFactor 小于等于 0
	 * @see opencv_imgproc#INTER_NEAREST
	 * @see opencv_imgproc#INTER_LINEAR
	 * @see opencv_imgproc#INTER_CUBIC
	 * @see opencv_imgproc#INTER_AREA
	 * @see opencv_imgproc#INTER_LANCZOS4
	 * @see opencv_imgproc#INTER_LINEAR_EXACT
	 * @see opencv_imgproc#INTER_NEAREST_EXACT
	 * @see opencv_imgproc#INTER_MAX
	 * @since 1.1.0
	 */
	public ImageProcessor scale(final double scalingFactor, final int interpolationFlag) {
		Validate.isTrue(scalingFactor > 0, "scalingFactor 必须大于 0");

		Size size = OpencvUtils.scale(outputImage.size(), scalingFactor);
		return resize(size.width(), size.height(), interpolationFlag);
	}

	/**
	 * 按目标尺寸等比例缩放图像（保持宽高比，不超出目标尺寸）
	 *
	 * @param targetWidth  目标宽度，必须大于 0
	 * @param targetHeight 目标高度，必须大于 0
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果 targetWidth 或 targetHeight 小于等于 0
	 * @since 1.1.0
	 */
	public ImageProcessor scale(final int targetWidth, final int targetHeight) {
		Validate.isTrue(targetWidth > 0, "targetWidth 必须大于 0");
		Validate.isTrue(targetHeight > 0, "targetHeight 必须大于 0");

		Size size = OpencvUtils.scale(outputImage.size(), targetWidth, targetHeight);
		return resize(size.width(), size.height());
	}

	/**
	 * 按目标尺寸等比例缩放图像（保持宽高比，不超出目标尺寸）
	 *
	 * @param targetWidth   目标宽度，必须大于 0
	 * @param targetHeight  目标高度，必须大于 0
	 * @param interpolationFlag 插值滤波算法
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果 targetWidth 或 targetHeight 小于等于 0
	 * @see opencv_imgproc#INTER_NEAREST
	 * @see opencv_imgproc#INTER_LINEAR
	 * @see opencv_imgproc#INTER_CUBIC
	 * @see opencv_imgproc#INTER_AREA
	 * @see opencv_imgproc#INTER_LANCZOS4
	 * @see opencv_imgproc#INTER_LINEAR_EXACT
	 * @see opencv_imgproc#INTER_NEAREST_EXACT
	 * @see opencv_imgproc#INTER_MAX
	 * @since 1.1.0
	 */
	public ImageProcessor scale(final int targetWidth, final int targetHeight, final int interpolationFlag) {
		Validate.isTrue(targetWidth > 0, "targetWidth 必须大于 0");
		Validate.isTrue(targetHeight > 0, "targetHeight 必须大于 0");

		Size size = OpencvUtils.scale(outputImage.size(), targetWidth, targetHeight);
		return resize(size.width(), size.height(), interpolationFlag);
	}

	/**
	 * 从图像中心裁剪指定大小的区域
	 *
	 * <p>如果目标尺寸大于图像尺寸，裁剪操作会被跳过</p>
	 *
	 * @param width  裁剪宽度，必须大于 0
	 * @param height 裁剪高度，必须大于 0
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果 width 或 height 小于等于 0
	 * @since 1.1.0
	 */
	public ImageProcessor cropByCenter(final int width, final int height) {
		Validate.isTrue(width > 0, "width 不能小于0");
		Validate.isTrue(height > 0, "height 不能小于0");

		Size imageSize = outputImage.size();
		// 边界检测
		if (width > imageSize.width() || height > imageSize.height()) {
			return this;
		}

		Rect rect = new Rect((imageSize.width() - width) / 2, (imageSize.height() - height) / 2, width, height);
		Mat image = outputImage.apply(rect).clone();

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	/**
	 * 从四周边缘按偏移量裁剪图像
	 *
	 * <p>如果目标区域超出图像边界，裁剪操作会被跳过</p>
	 *
	 * @param topOffset    顶部裁剪偏移（像素），必须 >= 0
	 * @param bottomOffset 底部裁剪偏移（像素），必须 >= 0
	 * @param leftOffset   左侧裁剪偏移（像素），必须 >= 0
	 * @param rightOffset  右侧裁剪偏移（像素），必须 >= 0
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果任一 offset 小于 0
	 * @since 1.1.0
	 */
	public ImageProcessor cropByOffset(final int topOffset, final int bottomOffset, final int leftOffset, final int rightOffset) {
		Validate.isTrue(topOffset >= 0 && bottomOffset >= 0 && leftOffset >= 0 && rightOffset >= 0,
			"offset 不能小于0");

		Size imageSize = outputImage.size();
		// 边界检测
		if (leftOffset + rightOffset > imageSize.width() ||
			topOffset + bottomOffset > imageSize.height()) {
			return this;
		}

		Rect rect = new Rect(leftOffset, topOffset, imageSize.width() - leftOffset - rightOffset,
			imageSize.height() - topOffset - bottomOffset);
		Mat image = outputImage.apply(rect).clone();

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	/**
	 * 按指定矩形区域裁剪图像
	 *
	 * <p>如果目标区域超出图像边界，裁剪操作会被跳过</p>
	 *
	 * @param x      矩形左上角 x 坐标，必须 >= 0
	 * @param y      矩形左上角 y 坐标，必须 >= 0
	 * @param width  矩形宽度，必须 > 0
	 * @param height 矩形高度，必须 > 0
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果任一参数无效
	 * @since 1.1.0
	 */
	public ImageProcessor cropByRect(final int x, final int y, final int width, final int height) {
		Validate.isTrue(x >= 0, "x 不能小于0");
		Validate.isTrue(y >= 0, "y 不能小于0");
		Validate.isTrue(width > 0, "width 不能小于0");
		Validate.isTrue(height > 0, "height 不能小于0");

		Size imageSize = outputImage.size();
		// 边界检测
		if (x + width > imageSize.width() || y + height > imageSize.height()) {
			return this;
		}

		Rect rect = new Rect(x, y, width, height);
		Mat image = outputImage.apply(rect).clone();

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	/**
	 * 将图像转换为灰度图
	 *
	 * <p>支持 BGR（3通道）或 BGRA（4通道）图像的转换</p>
	 *
	 * @return 当前处理器实例，支持链式调用
	 * @since 1.1.0
	 */
	public ImageProcessor grayscale() {
		Mat image = new Mat();
		int channels = this.outputImage.channels();

		if (channels == 3) {
			opencv_imgproc.cvtColor(this.outputImage, image, opencv_imgproc.COLOR_BGR2GRAY);
		} else if (channels == 4) {
			opencv_imgproc.cvtColor(this.outputImage, image, opencv_imgproc.COLOR_BGRA2GRAY);
		}

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	/**
	 * 对图像进行均值模糊处理（默认 5x5 卷积核）
	 *
	 * @return 当前处理器实例，支持链式调用
	 * @since 1.1.0
	 */
	public ImageProcessor blur() {
		return blur(new Size(5, 5));
	}

	/**
	 * 对图像进行均值模糊处理
	 *
	 * @param ksize 卷积核尺寸，不能为 null
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果 ksize 为 null
	 * @since 1.1.0
	 */
	public ImageProcessor blur(final Size ksize) {
		Validate.notNull(ksize, "ksize 不可为 null");

		Mat image = new Mat();

		opencv_imgproc.blur(this.outputImage, image, ksize);

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	/**
	 * 对图像进行高斯模糊处理（默认 5x5 卷积核，sigma=0）
	 *
	 * @return 当前处理器实例，支持链式调用
	 * @since 1.1.0
	 */
	public ImageProcessor gaussianBlur() {
		return gaussianBlur(new Size(5, 5), 0);
	}

	/**
	 * 对图像进行高斯模糊处理（sigma=0，自动计算）
	 *
	 * @param ksize 卷积核尺寸，不能为 null（宽高必须是奇数）
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果 ksize 为 null
	 * @since 1.1.0
	 */
	public ImageProcessor gaussianBlur(final Size ksize) {
		return gaussianBlur(ksize, 0);
	}

	/**
	 * 对图像进行高斯模糊处理
	 *
	 * @param ksize  卷积核尺寸，不能为 null（宽高必须是奇数）
	 * @param sigmaX X 方向的高斯核标准差，必须 >= 0
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果参数无效
	 * @since 1.1.0
	 */
	public ImageProcessor gaussianBlur(final Size ksize, final double sigmaX) {
		Validate.notNull(ksize, "ksize 不可为 null");
		Validate.isTrue(sigmaX >= 0, "sigmaX 必须大于等于 0");
		Validate.isTrue(ksize.width() % 2 != 0, "ksize 宽度必须为奇数");
		Validate.isTrue(ksize.height() % 2 != 0, "ksize 高度必须为奇数");

		Mat image = new Mat();

		opencv_imgproc.GaussianBlur(this.outputImage, image, ksize, sigmaX);

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	/**
	 * 对图像进行中值模糊处理（默认 5x5 卷积核）
	 *
	 * @return 当前处理器实例，支持链式调用
	 * @since 1.1.0
	 */
	public ImageProcessor medianBlur() {
		return medianBlur(5);
	}

	/**
	 * 对图像进行中值模糊处理
	 *
	 * @param ksize 卷积核尺寸，必须是大于 1 的奇数
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果 ksize 无效
	 * @since 1.1.0
	 */
	public ImageProcessor medianBlur(final int ksize) {
		Validate.isTrue(ksize > 1, "ksize 必须大于 1");
		Validate.isTrue(ksize % 2 != 0, "ksize 必须为奇数");

		Mat image = new Mat();

		opencv_imgproc.medianBlur(this.outputImage, image, ksize);

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	/**
	 * 对图像进行锐化处理（默认强度 weight=5）
	 *
	 * @return 当前处理器实例，支持链式调用
	 * @since 1.1.0
	 */
	public ImageProcessor sharpen() {
		return sharpen(5);
	}

	/**
	 * 对图像进行锐化处理
	 *
	 * @param weight 锐化强度，必须 > 4（值越大锐化效果越强）
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果 weight &lt;= 4
	 * @since 1.1.0
	 */
	public ImageProcessor sharpen(final float weight) {
		Validate.isTrue(weight > 4, "weight 必须大于4");

		float[] kernelData = {
			0, -1, 0,
			-1, weight, -1,
			0, -1, 0
		};
		Mat kernel = OpencvUtils.getKernel(kernelData);

		Mat image = new Mat();

		opencv_imgproc.filter2D(this.outputImage, image, -1, kernel);

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	/**
	 * 对图像进行浮雕效果处理（默认强度 strength=1.0）
	 *
	 * @return 当前处理器实例，支持链式调用
	 * @since 1.1.0
	 */
	public ImageProcessor emboss() {
		return emboss(1.0f);
	}

	/**
	 * 对图像进行浮雕效果处理
	 *
	 * @param strength 浮雕强度，必须 > 0
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果 strength &lt;= 0
	 * @since 1.1.0
	 */
	public ImageProcessor emboss(final float strength) {
		Validate.isTrue(strength > 0, "strength 必须大于0");

		float[] kernelData = {
			-2 * strength, -1 * strength, 0,
			-1 * strength, 1, 1 * strength,
			0, 1 * strength, 2 * strength
		};
		Mat kernel = OpencvUtils.getKernel(kernelData);

		Mat image = new Mat();

		opencv_imgproc.filter2D(this.outputImage, image, -1, kernel);

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	/**
	 * 对图像进行自适应二值化处理（使用 Otsu 算法自动计算阈值）
	 *
	 * @return 当前处理器实例，支持链式调用
	 * @since 1.1.0
	 */
	public ImageProcessor threshold() {
		return threshold(0, 255, opencv_imgproc.THRESH_BINARY + opencv_imgproc.THRESH_OTSU);
	}

	/**
	 * 对图像进行二值化处理
	 *
	 * @param thresh 阈值，范围 [0, 255]
	 * @param maxVal 最大值，范围 [0, 255]
	 * @param type   阈值处理类型（OpenCV 的 THRESH_* 常量）
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果参数无效
	 * @since 1.1.0
	 */
	public ImageProcessor threshold(final double thresh, final double maxVal, final int type) {
		Validate.isTrue(thresh >= 0 && thresh <= 255, "thresh 取值范围必须为 0~255");
		Validate.isTrue(maxVal >= 0 && maxVal <= 255, "maxVal 取值范围必须为 0~255");
		Validate.isTrue(thresh != maxVal, "thresh 不能与 maxVal 相同");

		Mat image = new Mat();

		opencv_imgproc.threshold(this.outputImage, image, thresh, maxVal, type);

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	/**
	 * 对图像进行自适应二值化处理（使用默认参数）
	 *
	 * @return 当前处理器实例，支持链式调用
	 * @since 1.1.0
	 */
	public ImageProcessor adaptiveThreshold() {
		return adaptiveThreshold(255, opencv_imgproc.ADAPTIVE_THRESH_MEAN_C,
			opencv_imgproc.THRESH_BINARY, 11, 2);
	}

	/**
	 * 对图像进行自适应二值化处理
	 *
	 * @param maxValue       最大值，范围 [0, 255]
	 * @param adaptiveMethod 自适应方法（OpenCV 的 ADAPTIVE_THRESH_* 常量）
	 * @param thresholdType  阈值类型（OpenCV 的 THRESH_* 常量）
	 * @param blockSize      邻域大小，必须是 >= 3 的奇数
	 * @param c              从均值或加权均值中减去的常量
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果参数无效
	 * @since 1.1.0
	 */
	public ImageProcessor adaptiveThreshold(final double maxValue, final int adaptiveMethod, final int thresholdType,
	                                        final int blockSize, final double c) {
		Validate.isTrue(maxValue >= 0 && maxValue <= 255, "maxValue 取值范围必须 0 ~ 255");
		Validate.isTrue(blockSize >= 3 && blockSize % 2 == 1, "blockSize 必须是大于等于3的奇数");

		Mat image = new Mat();

		opencv_imgproc.adaptiveThreshold(this.outputImage, image, maxValue, adaptiveMethod, thresholdType,
			blockSize, c);

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	/**
	 * 调整图像对比度（默认 alpha=0.3）
	 *
	 * @return 当前处理器实例，支持链式调用
	 * @since 1.1.0
	 */
	public ImageProcessor contrast() {
		return contrast(0.3f);
	}

	/**
	 * 调整图像对比度
	 *
	 * @param alpha 对比度缩放因子（1.0 为不改变，>1 增强，&lt;1 减弱）
	 * @return 当前处理器实例，支持链式调用
	 * @since 1.1.0
	 */
	public ImageProcessor contrast(final float alpha) {
		Validate.isTrue(alpha > 0, "alpha 必须大于 0");

		Mat image = new Mat();
		opencv_core.convertScaleAbs(outputImage, image, alpha, 0);

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	/**
	 * 调整图像亮度
	 *
	 * @param beta 亮度偏移量（0 为不改变，正值增加亮度，负值减少亮度）
	 * @return 当前处理器实例，支持链式调用
	 * @since 1.1.0
	 */
	public ImageProcessor brightness(final float beta) {
		Mat image = new Mat();
		opencv_core.convertScaleAbs(outputImage, image, 1, beta);

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	/**
	 * 添加图片水印（从文件加载），使用默认水印配置。
	 *
	 * <p>水印会根据默认配置自动调整大小、位置和透明度</p>
	 *
	 * @param watermarkImageFile 水印图片文件，不能为 null
	 * @return 当前处理器实例，支持链式调用
	 * @throws IOException              如果读取水印文件失败
	 * @throws IllegalArgumentException 如果 watermarkImageFile 为 null
	 * @since 1.1.0
	 */
	public ImageProcessor addImageWatermark(final File watermarkImageFile) throws IOException {
		try (Mat watermarkImageMat = OpencvUtils.read(watermarkImageFile, opencv_imgcodecs.IMREAD_UNCHANGED)) {
			return addImageWatermark(watermarkImageMat, new ImageWatermarkOption());
		}
	}

	/**
	 * 添加图片水印（从文件加载）
	 *
	 * @param watermarkImageFile 水印图片文件，不能为 null
	 * @param option             水印配置选项，不能为 null
	 * @return 当前处理器实例，支持链式调用
	 * @throws IOException              如果读取水印文件失败
	 * @throws IllegalArgumentException 如果任一参数为 null
	 * @since 1.1.0
	 */
	public ImageProcessor addImageWatermark(final File watermarkImageFile, final ImageWatermarkOption option) throws IOException {
		Validate.notNull(option, "option 不可为 null");

		try (Mat watermarkImageMat = OpencvUtils.read(watermarkImageFile, opencv_imgcodecs.IMREAD_UNCHANGED)) {
			return addImageWatermark(watermarkImageMat, option);
		}
	}

	/**
	 * 添加图片水印，使用默认水印配置。
	 *
	 * <p>水印会根据默认配置自动调整大小、位置和透明度</p>
	 *
	 * @param watermarkImage 水印图片 Mat，不能为 null
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果 watermarkImage 为 null
	 * @since 1.1.0
	 */
	public ImageProcessor addImageWatermark(final Mat watermarkImage) {
		return addImageWatermark(watermarkImage, new ImageWatermarkOption());
	}

	/**
	 * 添加图片水印
	 *
	 * <p>水印会根据配置自动调整大小、位置和透明度</p>
	 *
	 * @param watermarkImage 水印图片 Mat，不能为 null
	 * @param option         水印配置选项，不能为 null
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果任一参数为 null
	 * @since 1.1.0
	 */
	public ImageProcessor addImageWatermark(final Mat watermarkImage, final ImageWatermarkOption option) {
		Validate.notNull(option, "option 不可为 null");
		Validate.notNull(watermarkImage, "watermarkImage 不可为 null");
		Validate.isTrue(OpencvUtils.isNotEmpty(watermarkImage), "watermarkImage 不可为空");

		if (watermarkImage.channels() == 4) {
			OpencvUtils.cleanTransparency(watermarkImage);
		}

		Size outputImageSize = outputImage.size();
		Size originalWatermarkSize = watermarkImage.size();
		Pair<Size, Size> watermarkImageSizeRange = option.getSizeLimitStrategy().apply(outputImageSize);

		Mat targetWatermarkImage = new Mat();
		Size targetWatermarkImageSize = OpencvUtils.scale(outputImageSize, option.getRelativeScaleFactor());

		if (originalWatermarkSize.width() > originalWatermarkSize.height()) {
			int targetWidth = Math.min(watermarkImageSizeRange.getRight().width(),
				Math.max(watermarkImageSizeRange.getLeft().width(), targetWatermarkImageSize.width()));
			if (targetWidth != targetWatermarkImageSize.width()) {
				targetWatermarkImage = new Mat();
				targetWatermarkImageSize = OpencvUtils.scaleByWidth(originalWatermarkSize, targetWidth);
			}
		} else {
			int targetHeight = Math.min(watermarkImageSizeRange.getRight().height(),
				Math.max(watermarkImageSizeRange.getLeft().height(), targetWatermarkImageSize.height()));
			if (targetHeight != targetWatermarkImageSize.height()) {
				targetWatermarkImage = new Mat();
				targetWatermarkImageSize = OpencvUtils.scaleByHeight(originalWatermarkSize, targetHeight);
			}
		}

		opencv_imgproc.resize(watermarkImage, targetWatermarkImage, targetWatermarkImageSize);

		Rect watermarkRect;
		if (Objects.nonNull(option.getDirection())) {
			watermarkRect = option.getDirection().toImageWatermarkRect(outputImageSize, targetWatermarkImageSize, option.getMargin());
		} else {
			int x = Math.max(option.getMargin(), Math.min(outputImageSize.width() - targetWatermarkImageSize.width() -
				option.getMargin(), option.getX() + option.getMargin()));
			int y = Math.max(option.getMargin(), Math.min(outputImageSize.height() - targetWatermarkImageSize.height() -
				option.getMargin(), option.getY() + option.getMargin()));
			watermarkRect = new Rect(x, y, targetWatermarkImageSize.width(), targetWatermarkImageSize.height());
		}

		Mat roi = new Mat(outputImage, watermarkRect);

		Mat finalAlpha = new Mat();
		boolean hasAlpha = targetWatermarkImage.channels() == 4;
		if (hasAlpha) {
			// 提取水印自带的 Alpha 通道
			MatVector channels = new MatVector(4);
			opencv_core.split(targetWatermarkImage, channels);

			Mat originalAlpha = channels.get(3);
			originalAlpha.convertTo(finalAlpha, opencv_core.CV_32F, option.getOpacity() / 255.0, 0);

			for (Mat mat : channels.get()) {
				mat.releaseReference();
			}
		} else {
			finalAlpha.create(targetWatermarkImageSize.height(), targetWatermarkImageSize.width(), opencv_core.CV_32F);
			finalAlpha.put(new Scalar(option.getOpacity(), option.getOpacity(), option.getOpacity(), 0));
		}

		Mat watermarkBGR = new Mat();
		if (hasAlpha) {
			MatVector channels = new MatVector(4);
			opencv_core.split(targetWatermarkImage, channels);

			Mat alphatMat = channels.pop_back();
			alphatMat.releaseReference();

			opencv_core.merge(channels, watermarkBGR);

			for (Mat mat : channels.get()) {
				mat.releaseReference();
			}
		} else {
			targetWatermarkImage.copyTo(watermarkBGR);
		}

		Mat alpha3ch = new Mat();
		MatVector channels = new MatVector(3);
		channels.put(finalAlpha, finalAlpha, finalAlpha);
		opencv_core.merge(channels, alpha3ch);

		Mat oneMat = new Mat(targetWatermarkImageSize.height(), targetWatermarkImageSize.width(),
			opencv_core.CV_32FC3, new Scalar(1.0, 1.0, 1.0, 0));

		Mat alignedAlpha = new Mat();
		alpha3ch.convertTo(alignedAlpha, opencv_core.CV_32FC3);

		Mat invAlpha = new Mat();
		opencv_core.subtract(oneMat, alignedAlpha, invAlpha);

		Mat foreground = new Mat();
		opencv_core.multiply(watermarkBGR, alignedAlpha, foreground, 1.0, opencv_core.CV_32F);

		Mat background = new Mat();
		opencv_core.multiply(roi, invAlpha, background, 1.0, opencv_core.CV_32F);

		Mat blended = new Mat();
		opencv_core.add(foreground, background, blended, new Mat(), opencv_core.CV_32F);

		Mat blendedUint8 = new Mat();
		blended.convertTo(blendedUint8, opencv_core.CV_8U);
		blendedUint8.copyTo(roi);

		if (targetWatermarkImage != watermarkImage) {
			targetWatermarkImage.release();
		}
		roi.release();
		roi.release();
		finalAlpha.release();
		watermarkBGR.release();
		alignedAlpha.release();
		alpha3ch.release();
		oneMat.release();
		invAlpha.release();
		foreground.release();
		background.release();
		blended.release();
		blendedUint8.release();

		return this;
	}

	/**
	 * 添加文字水印，使用默认水印配置。
	 *
	 * <p><b>注意：OpenCV 默认不支持中文字符</p>
	 *
	 * @param watermarkText 水印文字，不能为 null 或空
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果 watermarkText 为 null 或空
	 * @since 1.1.0
	 */
	public ImageProcessor addTextWatermark(final String watermarkText) {
		return addTextWatermark(watermarkText, new TextWatermarkOption());
	}

	/**
	 * 添加文字水印
	 *
	 * <p><b>注意：OpenCV 默认不支持中文字符</p>
	 *
	 * @param watermarkText 水印文字，不能为 null 或空
	 * @param option        水印配置选项，不能为 null
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果任一参数无效
	 * @since 1.1.0
	 */
	public ImageProcessor addTextWatermark(final String watermarkText, final TextWatermarkOption option) {
		Validate.notNull(option, "option 不可为 null");
		Validate.notBlank(watermarkText, "watermarkText 不可为空");

		Size imageSize = outputImage.size();

		Scalar fillColor = OpencvUtils.toBGRColor(option.getFillColor());
		Scalar strokeColor = OpencvUtils.toBGRColor(option.getStrokeColor());
		double fontScale = option.getFontScaleStrategy().applyAsDouble(outputImage.size(), option.getFontFace());

		Size textSize = opencv_imgproc.getTextSize(watermarkText, option.getFontFace(), fontScale,
			option.getThickness(), (IntBuffer) null);
		int textW = textSize.width();
		int textH = textSize.height();

		Point point = null;
		if (Objects.nonNull(option.getDirection())) {
			switch (option.getDirection()) {
				case TOP:
					point = new Point((imageSize.width() - textW) / 2, textH + option.getMargin());
					break;
				case TOP_LEFT:
					point = new Point(option.getMargin(), textH + option.getMargin());
					break;
				case TOP_RIGHT:
					point = new Point(imageSize.width() - textW - option.getMargin(), textH + option.getMargin());
					break;
				case BOTTOM:
					point = new Point((imageSize.width() - textW) / 2, imageSize.height() - option.getMargin());
					break;
				case BOTTOM_LEFT:
					point = new Point(option.getMargin(), imageSize.height() - option.getMargin());
					break;
				case BOTTOM_RIGHT:
					point = new Point(imageSize.width() - textW - option.getMargin(),
						imageSize.height() - option.getMargin());
					break;
				case RIGHT:
					point = new Point(imageSize.width() - textW - option.getMargin(), (imageSize.height() + textH) / 2);
					break;
				case LEFT:
					point = new Point(option.getMargin(), (imageSize.height() + textH) / 2);
					break;
				case CENTER:
					point = new Point((imageSize.width() - textW) / 2, (imageSize.height() + textH) / 2);
					break;
				default:
					break;
			}
		} else {
			int x = Math.max(option.getMargin(), Math.min(imageSize.width() - textW - option.getMargin(),
				option.getX() + option.getMargin()));
			int y = Math.max(textH + option.getMargin(), Math.min(imageSize.height() - option.getMargin(),
				option.getY() + option.getMargin()));
			point = new Point(x, y);
		}

		Mat image;
		if (option.getOpacity() < 1) {
			Mat textLayer = new Mat(imageSize, outputImage.type());

			if (option.isStroke()) {
				opencv_imgproc.putText(textLayer, watermarkText, point, option.getFontFace(), fontScale,
					strokeColor, option.getThickness() + option.getStrokeSize(), opencv_imgproc.LINE_AA,
					false);
			}
			opencv_imgproc.putText(textLayer, watermarkText, point, option.getFontFace(), fontScale, fillColor,
				option.getThickness(), opencv_imgproc.LINE_AA, false);

			image = new Mat();
			opencv_core.addWeighted(outputImage, 1, textLayer, option.getOpacity(), 0, image);

			textLayer.releaseReference();
		} else {
			image = outputImage.clone();

			if (option.isStroke()) {
				opencv_imgproc.putText(image, watermarkText, point, option.getFontFace(), fontScale, strokeColor,
					option.getThickness() + option.getStrokeSize(), opencv_imgproc.LINE_AA,
					false);
			}
			opencv_imgproc.putText(image, watermarkText, point, option.getFontFace(), fontScale, fillColor,
				option.getThickness(), opencv_imgproc.LINE_AA, false);
		}

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	/**
	 * 应用自定义图像处理操作
	 *
	 * @param operation 自定义处理函数，不能为 null
	 * @return 当前处理器实例，支持链式调用
	 * @throws IllegalArgumentException 如果 operation 为 null
	 * @since 1.1.0
	 */
	public ImageProcessor apply(final Function<Mat, Mat> operation) {
		Validate.notNull(operation, "operation 不可为 null");

		Mat image = operation.apply(this.outputImage);

		this.outputImage.releaseReference();
		this.outputImage = image;

		return this;
	}

	/**
	 * 将处理后的图像保存到文件
	 *
	 * <p>根据文件扩展名自动确定输出格式</p>
	 *
	 * @param outputFile 输出文件，不能为 null
	 * @return 是否保存成功
	 * @throws IllegalArgumentException 如果 outputFile 为 null
	 * @since 1.1.0
	 */
	public boolean toFile(final File outputFile) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		return opencv_imgcodecs.imwrite(outputFile.getAbsolutePath(), outputImage);
	}

	/**
	 * 将处理后的图像保存到文件（带编码参数）
	 *
	 * <p>根据文件扩展名自动确定输出格式</p>
	 *
	 * @param outputFile 输出文件，不能为 null
	 * @param params     编码参数，不能为 null
	 * @return 是否保存成功
	 * @throws IllegalArgumentException 如果任一参数为 null
	 * @since 1.1.0
	 */
	public boolean toFile(final File outputFile, final int... params) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notNull(params, "params 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		return opencv_imgcodecs.imwrite(outputFile.getAbsolutePath(), outputImage, params);
	}

	/**
	 * 将处理后的图像写入输出流
	 *
	 * <p>使用默认的 outputFormat 编码图像</p>
	 *
	 * @param outputStream 输出流，不能为 null
	 * @return 是否写入成功
	 * @throws IOException              如果写入失败
	 * @throws IllegalArgumentException 如果任一参数无效
	 * @since 1.1.0
	 */
	public boolean toOutputStream(final OutputStream outputStream) throws IOException {
		return toOutputStream(outputStream, outputFormat);
	}

	/**
	 * 将处理后的图像写入输出流（带编码参数）
	 *
	 * <p>使用默认的 outputFormat 编码图像</p>
	 *
	 * @param outputStream 输出流，不能为 null
	 * @param params       编码参数，不能为 null
	 * @return 是否写入成功
	 * @throws IOException              如果写入失败
	 * @throws IllegalArgumentException 如果任一参数无效
	 * @since 1.1.0
	 */
	public boolean toOutputStream(final OutputStream outputStream, final int... params) throws IOException {
		return toOutputStream(outputStream, outputFormat, params);
	}

	/**
	 * 将处理后的图像写入输出流
	 *
	 * <p>使用指定的 outputFormat 编码图像</p>
	 *
	 * @param outputFormat 图像格式，不能为 null 或空
	 * @param outputStream 输出流，不能为 null
	 * @return 是否写入成功
	 * @throws IOException              如果写入失败
	 * @throws IllegalArgumentException 如果任一参数无效
	 * @since 1.1.0
	 */
	public boolean toOutputStream(final OutputStream outputStream, final String outputFormat) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		byte[] bytes = toBytes(outputFormat);
		if (Objects.isNull(bytes)) {
			return false;
		}
		outputStream.write(bytes);
		return true;
	}

	/**
	 * 将处理后的图像写入输出流（带编码参数）
	 *
	 * <p>使用指定的 outputFormat 编码图像</p>
	 *
	 * @param outputFormat       图像格式，不能为 null 或空
	 * @param outputStream 输出流，不能为 null
	 * @param params       编码参数，不能为 null
	 * @return 是否写入成功
	 * @throws IOException              如果写入失败
	 * @throws IllegalArgumentException 如果任一参数无效
	 * @since 1.1.0
	 */
	public boolean toOutputStream(final OutputStream outputStream, final String outputFormat, final int... params) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		byte[] bytes = toBytes(outputFormat, params);
		if (Objects.isNull(bytes)) {
			return false;
		}
		outputStream.write(bytes);
		return true;
	}

	/**
	 * 将处理后的图像转换为字节数组
	 *
	 * <p>使用默认的 outputFormat 编码图像</p>
	 *
	 * @return 图像字节数组，失败时返回 null
	 * @throws IllegalArgumentException 如果 format 无效
	 * @since 1.1.0
	 */
	public byte[] toBytes() {
		return toBytes(outputFormat);
	}

	/**
	 * 将处理后的图像转换为字节数组（带编码参数）
	 *
	 * <p>使用默认的 outputFormat 编码图像</p>
	 *
	 * @param params 编码参数，不能为 null
	 * @return 图像字节数组，失败时返回 null
	 * @throws IllegalArgumentException 如果任一参数无效
	 * @since 1.1.0
	 */
	public byte[] toBytes(final int... params) {
		return toBytes(outputFormat, params);
	}

	/**
	 * 将处理后的图像转换为字节数组
	 *
	 * <p>使用指定的 outputFormat 编码图像</p>
	 *
	 * @param outputFormat 图像格式，不能为 null 或空
	 * @return 图像字节数组，失败时返回 null
	 * @throws IllegalArgumentException 如果 format 无效
	 * @since 1.1.0
	 */
	public byte[] toBytes(final String outputFormat) {
		Validate.notBlank(outputFormat, "outputFormat 不可为空");
		Validate.isTrue(OpencvUtils.canWrite(outputFormat), "不支持输出 " + outputFormat + " 图像格式");

		byte[] bytes = new byte[(int) (outputImage.rows() * outputImage.step())];
		boolean result = opencv_imgcodecs.imencode(outputFormat, outputImage, bytes);

		if (!result) {
			return null;
		}
		return bytes;
	}

	/**
	 * 将处理后的图像转换为字节数组（带编码参数）
	 *
	 * <p>使用指定的 outputFormat 编码图像</p>
	 *
	 * @param outputFormat 图像格式，不能为 null 或空
	 * @param params 编码参数，不能为 null
	 * @return 图像字节数组，失败时返回 null
	 * @throws IllegalArgumentException 如果任一参数无效
	 * @since 1.1.0
	 */
	public byte[] toBytes(final String outputFormat, final int... params) {
		Validate.notNull(params, "params 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");
		Validate.isTrue(OpencvUtils.canWrite(outputFormat), "不支持输出 " + outputFormat + " 图像格式");

		byte[] bytes = new byte[(int) (outputImage.rows() * outputImage.step())];
		boolean result = opencv_imgcodecs.imencode(outputFormat, outputImage, bytes, params);

		if (!result) {
			return null;
		}
		return bytes;
	}

	/**
	 * 获取处理后的图像 Mat
	 *
	 * @return 图像 Mat 对象
	 * @since 1.1.0
	 */
	public Mat toMat() {
		return this.outputImage;
	}

	/**
	 * 重置到初始图像状态
	 *
	 * @return 当前处理器实例，支持链式调用
	 * @since 1.1.0
	 */
	public ImageProcessor reset() {
		this.outputImage.releaseReference();
		this.outputImage = this.inputImage.clone();

		return this;
	}

	/**
	 * 释放所有图像资源
	 *
	 * @since 1.1.0
	 */
	public void release() {
		this.outputImage.releaseReference();
		this.outputImage = null;
	}
}
