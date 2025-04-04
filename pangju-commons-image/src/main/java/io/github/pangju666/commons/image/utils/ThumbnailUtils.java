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
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.FilenameUtils;
import org.apache.commons.lang3.Validate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 缩略图生成工具类，提供多种图像缩放策略和输出方式
 * <p>
 * 本工具类支持两种缩放模式：
 * <ol>
 *   <li><b>强制缩放</b> - 不考虑原始图像宽高比，直接缩放到指定尺寸</li>
 *   <li><b>等比缩放</b> - 保持原始图像宽高比，根据高度、宽度或最大边进行缩放</li>
 * </ol>
 * </p>
 *
 * <p><b>核心特性：</b></p>
 * <ul>
 *   <li>支持多种输出目标：文件、输出流、BufferedImage对象</li>
 *   <li>自动处理图像格式转换（如JPG转PNG时的透明通道处理）</li>
 *   <li>提供多种重采样滤波器选择（默认使用三角形滤波器）</li>
 *   <li>完善的参数校验和异常处理</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>{@code
 * // 强制缩放图像到指定尺寸并保存为文件
 * ThumbnailUtils.forceScale(inputImage, outputFile, new ImageSize(200, 200));
 *
 * // 根据高度等比缩放图像并输出到流
 * ThumbnailUtils.scaleByHeight(inputImage, outputStream, 300, "PNG");
 * }</pre>
 *
 * <p><b>注意事项：</b></p>
 * <ul>
 *   <li>默认使用{@link ResampleOp#FILTER_TRIANGLE 三角形滤波器}进行重采样</li>
 *   <li>输出{@link BufferedImage#TYPE_INT_RGB 非透明格式}(如JPG)时会自动转换为{@link BufferedImage#TYPE_INT_ARGB 透明格式}</li>
 *   <li>所有方法都是线程安全的</li>
 *   <li>输入流必须支持mark/reset操作</li>
 * </ul>
 *
 * @author pangju666
 * @see ResampleOp
 * @see ImageSize
 * @see BufferedImage
 * @since 1.0.0
 */
public class ThumbnailUtils {
	protected ThumbnailUtils() {
	}

	/**
	 * 强制缩放图像到指定尺寸并输出到流（不考虑原始宽高比）
	 * <p>
	 * 此方法会将图像强制缩放到指定尺寸，不保持原始宽高比，
	 * 可能导致图像变形。适用于需要精确控制输出尺寸的场景。
	 * </p>
	 *
	 * <p><b>处理流程：</b></p>
	 * <ol>
	 *   <li>校验输入参数有效性</li>
	 *   <li>使用指定滤波器进行图像重采样</li>
	 *   <li>自动处理图像格式转换</li>
	 *   <li>将结果写入输出流</li>
	 * </ol>
	 *
	 * @param inputImage   原始图像，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>已加载的有效图像</li>
	 *                   </ul>
	 * @param imageSize    目标尺寸，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>宽度和高度都大于0</li>
	 *                   </ul>
	 * @param outputStream 输出流，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>已打开的可写流</li>
	 *                   </ul>
	 * @param outputFormat 输出图片格式，必须满足：
	 *                   <ul>
	 *                     <li>非空</li>
	 *                     <li>ImageIO支持的格式（如"PNG","JPEG"）</li>
	 *                   </ul>
	 * @return 是否成功写入输出流
	 * @throws IOException 当发生I/O错误时抛出，包括：
	 *                   <ul>
	 *                     <li>输出流写入失败</li>
	 *                     <li>图像编码失败</li>
	 *                   </ul>
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #resample(BufferedImage, ImageSize, String, int)
	 * @since 1.0.0
	 */
	public static boolean forceScale(final BufferedImage inputImage, final OutputStream outputStream,
									 final ImageSize imageSize, final String outputFormat) throws IOException {
		return forceScale(inputImage, outputStream, imageSize, outputFormat, ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 强制缩放图像到指定尺寸并输出到流（不考虑原始宽高比）
	 * <p>
	 * 此方法会将图像强制缩放到指定尺寸，不保持原始宽高比，
	 * 可能导致图像变形。适用于需要精确控制输出尺寸的场景。
	 * </p>
	 *
	 * <p><b>处理流程：</b></p>
	 * <ol>
	 *   <li>校验输入参数有效性</li>
	 *   <li>使用指定滤波器进行图像重采样</li>
	 *   <li>自动处理图像格式转换</li>
	 *   <li>将结果写入输出流</li>
	 * </ol>
	 *
	 * @param inputImage   原始图像，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>已加载的有效图像</li>
	 *                   </ul>
	 * @param imageSize    目标尺寸，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>宽度和高度都大于0</li>
	 *                   </ul>
	 * @param outputStream 输出流，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>已打开的可写流</li>
	 *                   </ul>
	 * @param outputFormat 输出图片格式，必须满足：
	 *                   <ul>
	 *                     <li>非空</li>
	 *                     <li>ImageIO支持的格式（如"PNG","JPEG"）</li>
	 *                   </ul>
	 * @param filterType   重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 是否成功写入输出流
	 * @throws IOException 当发生I/O错误时抛出，包括：
	 *                   <ul>
	 *                     <li>输出流写入失败</li>
	 *                     <li>图像编码失败</li>
	 *                   </ul>
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #resample(BufferedImage, ImageSize, String, int)
	 * @since 1.0.0
	 */
	public static boolean forceScale(final BufferedImage inputImage, final OutputStream outputStream,
									 final ImageSize imageSize, final String outputFormat, final int filterType) throws IOException {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.notNull(imageSize, "imageSize 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");

		BufferedImage outputImage = resample(inputImage, imageSize, outputFormat, filterType);
		return ImageIO.write(outputImage, outputFormat.toLowerCase(), outputStream);
	}

	/**
	 * 强制缩放图像并输出到文件（自动获取文件扩展名作为格式）
	 * <p>
	 * 此方法会根据文件扩展名自动确定输出格式，
	 * 适用于需要简化参数传递的场景。
	 * </p>
	 *
	 * <p><b>处理流程：</b></p>
	 * <ol>
	 *   <li>从文件名提取扩展名作为输出格式</li>
	 *   <li>使用默认三角形滤波器进行图像重采样</li>
	 *   <li>将结果写入文件</li>
	 * </ol>
	 *
	 * @param inputImage 原始图像，必须满足：
	 *                 <ul>
	 *                   <li>非null</li>
	 *                   <li>已加载的有效图像</li>
	 *                 </ul>
	 * @param imageSize  目标尺寸，必须满足：
	 *                 <ul>
	 *                   <li>非null</li>
	 *                   <li>宽度和高度都大于0</li>
	 *                 </ul>
	 * @param outputFile 输出文件，必须满足：
	 *                 <ul>
	 *                   <li>非null</li>
	 *                   <li>可写的文件路径</li>
	 *                   <li>不是目录</li>
	 *                 </ul>
	 * @return 是否成功写入文件
	 * @throws IOException 当发生I/O错误时抛出，包括：
	 *                   <ul>
	 *                     <li>文件写入失败</li>
	 *                     <li>图像编码失败</li>
	 *                   </ul>
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #forceScale(BufferedImage, File, ImageSize, String, int)
	 * @since 1.0.0
	 */
	public static boolean forceScale(final BufferedImage inputImage, final File outputFile, final ImageSize imageSize) throws IOException {
		return forceScale(inputImage, outputFile, imageSize,
			FilenameUtils.getExtension(outputFile.getName()), ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 强制缩放图像并输出到文件（自动获取文件扩展名作为格式）
	 * <p>
	 * 此方法会根据文件扩展名自动确定输出格式，
	 * 适用于需要简化参数传递的场景。
	 * </p>
	 *
	 * <p><b>处理流程：</b></p>
	 * <ol>
	 *   <li>从文件名提取扩展名作为输出格式</li>
	 *   <li>使用指定滤波器进行图像重采样</li>
	 *   <li>将结果写入文件</li>
	 * </ol>
	 *
	 * @param inputImage 原始图像，必须满足：
	 *                 <ul>
	 *                   <li>非null</li>
	 *                   <li>已加载的有效图像</li>
	 *                 </ul>
	 * @param imageSize  目标尺寸，必须满足：
	 *                 <ul>
	 *                   <li>非null</li>
	 *                   <li>宽度和高度都大于0</li>
	 *                 </ul>
	 * @param outputFile 输出文件，必须满足：
	 *                 <ul>
	 *                   <li>非null</li>
	 *                   <li>可写的文件路径</li>
	 *                   <li>不是目录</li>
	 *                 </ul>
	 * @param filterType 重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 是否成功写入文件
	 * @throws IOException 当发生I/O错误时抛出，包括：
	 *                   <ul>
	 *                     <li>文件写入失败</li>
	 *                     <li>图像编码失败</li>
	 *                   </ul>
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #forceScale(BufferedImage, File, ImageSize, String, int)
	 * @since 1.0.0
	 */
	public static boolean forceScale(final BufferedImage inputImage, final File outputFile, final ImageSize imageSize,
									 final int filterType) throws IOException {
		return forceScale(inputImage, outputFile, imageSize,
			FilenameUtils.getExtension(outputFile.getName()), filterType);
	}

	/**
	 * 强制缩放图像并输出到文件
	 * <p>
	 * 此方法会将图像强制缩放到指定尺寸，不保持原始宽高比，
	 * 可能导致图像变形。适用于需要精确控制输出尺寸的场景。
	 * </p>
	 *
	 * <p><b>处理流程：</b></p>
	 * <ol>
	 *   <li>校验输入参数有效性</li>
	 *   <li>使用默认三角形滤波器进行图像重采样</li>
	 *   <li>将结果写入文件</li>
	 * </ol>
	 *
	 * @param inputImage   原始图像，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>已加载的有效图像</li>
	 *                   </ul>
	 * @param imageSize    目标尺寸，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>宽度和高度都大于0</li>
	 *                   </ul>
	 * @param outputFile   输出文件，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>可写的文件路径</li>
	 *                     <li>不是目录</li>
	 *                   </ul>
	 * @param outputFormat 输出图片格式，必须满足：
	 *                   <ul>
	 *                     <li>非空</li>
	 *                     <li>ImageIO支持的格式（如"PNG","JPEG"）</li>
	 *                   </ul>
	 * @return 是否成功写入文件
	 * @throws IOException 当发生I/O错误时抛出，包括：
	 *                   <ul>
	 *                     <li>文件写入失败</li>
	 *                     <li>图像编码失败</li>
	 *                   </ul>
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #forceScale(BufferedImage, File, ImageSize, String, int)
	 * @since 1.0.0
	 */
	public static boolean forceScale(final BufferedImage inputImage, final File outputFile, final ImageSize imageSize,
									 final String outputFormat) throws IOException {
		return forceScale(inputImage, outputFile, imageSize, outputFormat, ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 强制缩放图像并输出到文件（不考虑原始宽高比）
	 * <p>
	 * 此方法会将图像强制缩放到指定尺寸，不保持原始宽高比，
	 * 可能导致图像变形。适用于需要精确控制输出尺寸的场景。
	 * </p>
	 *
	 * <p><b>处理流程：</b></p>
	 * <ol>
	 *   <li>校验输入参数有效性</li>
	 *   <li>使用指定滤波器进行图像重采样</li>
	 *   <li>自动处理图像格式转换</li>
	 *   <li>将结果写入文件</li>
	 * </ol>
	 *
	 * @param inputImage   原始图像，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>已加载的有效图像</li>
	 *                   </ul>
	 * @param imageSize    目标尺寸，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>宽度和高度都大于0</li>
	 *                   </ul>
	 * @param outputFile   输出文件，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>可写的文件路径</li>
	 *                     <li>不是目录</li>
	 *                   </ul>
	 * @param outputFormat 输出图片格式，必须满足：
	 *                   <ul>
	 *                     <li>非空</li>
	 *                     <li>ImageIO支持的格式（如"PNG","JPEG"）</li>
	 *                   </ul>
	 * @param filterType   重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 是否成功写入文件
	 * @throws IOException 当发生I/O错误时抛出，包括：
	 *                   <ul>
	 *                     <li>文件写入失败</li>
	 *                     <li>图像编码失败</li>
	 *                   </ul>
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #resample(BufferedImage, ImageSize, String, int)
	 * @since 1.0.0
	 */
	public static boolean forceScale(final BufferedImage inputImage, final File outputFile, final ImageSize imageSize,
									 final String outputFormat, final int filterType) throws IOException {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.notNull(imageSize, "imageSize 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		BufferedImage outputImage = resample(inputImage, imageSize, outputFormat, filterType);
		return ImageIO.write(outputImage, outputFormat.toLowerCase(), outputFile);
	}

	/**
	 * 强制缩放图像（不考虑原始宽高比）
	 * <p>
	 * 此方法会将图像强制缩放到指定尺寸，不保持原始宽高比，
	 * 可能导致图像变形。适用于需要精确控制输出尺寸的场景。
	 * </p>
	 *
	 * <p><b>处理流程：</b></p>
	 * <ol>
	 *   <li>使用默认三角形滤波器进行图像重采样</li>
	 *   <li>返回缩放后的图像对象</li>
	 * </ol>
	 *
	 * @param image     原始图像，必须满足：
	 *                <ul>
	 *                  <li>非null</li>
	 *                  <li>已加载的有效图像</li>
	 *                </ul>
	 * @param imageSize 目标尺寸，必须满足：
	 *                <ul>
	 *                  <li>非null</li>
	 *                  <li>宽度和高度都大于0</li>
	 *                </ul>
	 * @return 缩放后的图像对象
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #forceScale(BufferedImage, ImageSize, int)
	 * @since 1.0.0
	 */
	public static BufferedImage forceScale(final BufferedImage image, final ImageSize imageSize) {
		return scale(image, imageSize, ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 强制缩放图像（不考虑原始宽高比）
	 * <p>
	 * 此方法会将图像强制缩放到指定尺寸，不保持原始宽高比，
	 * 可能导致图像变形。适用于需要精确控制输出尺寸的场景。
	 * </p>
	 *
	 * <p><b>处理流程：</b></p>
	 * <ol>
	 *   <li>校验输入参数有效性</li>
	 *   <li>使用指定滤波器进行图像重采样</li>
	 *   <li>返回缩放后的图像对象</li>
	 * </ol>
	 *
	 * @param image      原始图像，必须满足：
	 *                 <ul>
	 *                   <li>非null</li>
	 *                   <li>已加载的有效图像</li>
	 *                 </ul>
	 * @param imageSize  目标尺寸，必须满足：
	 *                 <ul>
	 *                   <li>非null</li>
	 *                   <li>宽度和高度都大于0</li>
	 *                 </ul>
	 * @param filterType 重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 缩放后的图像对象
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #resample(BufferedImage, ImageSize, int)
	 * @since 1.0.0
	 */
	public static BufferedImage forceScale(final BufferedImage image, final ImageSize imageSize, final int filterType) {
		Validate.notNull(image, "image 不可为 null");
		Validate.notNull(imageSize, "imageSize 不可为 null");

		return resample(image, imageSize, filterType);
	}

	/**
	 * 根据高度等比缩放图像到输出流
	 * <p>
	 * 保持原始宽高比，根据指定高度计算新宽度并缩放图像。
	 * 适用于需要保持图像比例的场景。
	 * </p>
	 *
	 * <p><b>处理流程：</b></p>
	 * <ol>
	 *   <li>校验输入参数有效性</li>
	 *   <li>计算保持比例的目标尺寸</li>
	 *   <li>使用默认滤波器进行图像重采样</li>
	 *   <li>将结果写入输出流</li>
	 * </ol>
	 *
	 * @param inputImage   原始图像，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>已加载的有效图像</li>
	 *                   </ul>
	 * @param height       目标高度，必须满足：
	 *                   <ul>
	 *                     <li>大于0</li>
	 *                   </ul>
	 * @param outputStream 输出流，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>已打开的可写流</li>
	 *                   </ul>
	 * @param outputFormat 输出图片格式，必须满足：
	 *                   <ul>
	 *                     <li>非空</li>
	 *                     <li>ImageIO支持的格式（如"PNG","JPEG"）</li>
	 *                   </ul>
	 * @return 是否成功写入输出流
	 * @throws IOException 当发生I/O错误时抛出，包括：
	 *                   <ul>
	 *                     <li>输出流写入失败</li>
	 *                     <li>图像编码失败</li>
	 *                   </ul>
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #scaleByHeight(BufferedImage, OutputStream, int, String, int)
	 * @since 1.0.0
	 */
	public static boolean scaleByHeight(final BufferedImage inputImage, final OutputStream outputStream, final int height,
										final String outputFormat) throws IOException {
		return scaleByHeight(inputImage, outputStream, height, outputFormat, ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 根据宽度等比缩放图像到输出流
	 * <p>
	 * 保持原始宽高比，根据指定宽度计算新高度并缩放图像。
	 * 适用于需要保持图像比例的场景。
	 * </p>
	 *
	 * <p><b>处理流程：</b></p>
	 * <ol>
	 *   <li>校验输入参数有效性</li>
	 *   <li>计算保持比例的目标尺寸</li>
	 *   <li>使用默认滤波器进行图像重采样</li>
	 *   <li>将结果写入输出流</li>
	 * </ol>
	 *
	 * @param inputImage   原始图像，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>已加载的有效图像</li>
	 *                   </ul>
	 * @param width        目标宽度，必须满足：
	 *                   <ul>
	 *                     <li>大于0</li>
	 *                   </ul>
	 * @param outputStream 输出流，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>已打开的可写流</li>
	 *                   </ul>
	 * @param outputFormat 输出图片格式，必须满足：
	 *                   <ul>
	 *                     <li>非空</li>
	 *                     <li>ImageIO支持的格式（如"PNG","JPEG"）</li>
	 *                   </ul>
	 * @return 是否成功写入输出流
	 * @throws IOException 当发生I/O错误时抛出，包括：
	 *                   <ul>
	 *                     <li>输出流写入失败</li>
	 *                     <li>图像编码失败</li>
	 *                   </ul>
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #scaleByWidth(BufferedImage, OutputStream, int, String, int)
	 * @since 1.0.0
	 */
	public static boolean scaleByWidth(final BufferedImage inputImage, final OutputStream outputStream, final int width,
									   final String outputFormat) throws IOException {
		return scaleByWidth(inputImage, outputStream, width, outputFormat, ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 根据高度或宽度等比缩放图像到输出流
	 * <p>
	 * 保持原始宽高比，根据指定尺寸计算新尺寸并缩放图像。
	 * 适用于需要保持图像比例的输出流场景。
	 * </p>
	 *
	 * <p><b>处理流程：</b></p>
	 * <ol>
	 *   <li>校验输入参数有效性</li>
	 *   <li>计算保持比例的目标尺寸</li>
	 *   <li>使用默认滤波器进行图像重采样</li>
	 *   <li>将结果写入输出流</li>
	 * </ol>
	 *
	 * @param inputImage   原始图像，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>已加载的有效图像</li>
	 *                   </ul>
	 * @param imageSize    目标尺寸，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>宽度和高度都大于0</li>
	 *                   </ul>
	 * @param outputStream 输出流，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>已打开的可写流</li>
	 *                   </ul>
	 * @param outputFormat 输出图片格式，必须满足：
	 *                   <ul>
	 *                     <li>非空</li>
	 *                     <li>ImageIO支持的格式（如"PNG","JPEG"）</li>
	 *                   </ul>
	 * @return 是否成功写入输出流
	 * @throws IOException 当发生I/O错误时抛出，包括：
	 *                   <ul>
	 *                     <li>输出流写入失败</li>
	 *                     <li>图像编码失败</li>
	 *                   </ul>
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #scale(BufferedImage, OutputStream, ImageSize, String, int)
	 * @since 1.0.0
	 */
	public static boolean scale(final BufferedImage inputImage, final OutputStream outputStream, final ImageSize imageSize,
								final String outputFormat) throws IOException {
		return scale(inputImage, outputStream, imageSize, outputFormat, ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 根据高度等比缩放图像到输出流
	 * <p>
	 * 保持原始宽高比，根据指定高度计算新宽度并缩放图像。
	 * 适用于需要保持图像比例的输出流场景。
	 * </p>
	 *
	 * <p><b>处理流程：</b></p>
	 * <ol>
	 *   <li>校验输入参数有效性</li>
	 *   <li>计算保持比例的目标尺寸</li>
	 *   <li>使用指定滤波器进行图像重采样</li>
	 *   <li>将结果写入输出流</li>
	 * </ol>
	 *
	 * @param inputImage   原始图像，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>已加载的有效图像</li>
	 *                   </ul>
	 * @param height       目标高度，必须满足：
	 *                   <ul>
	 *                     <li>大于0</li>
	 *                   </ul>
	 * @param outputStream 输出流，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>已打开的可写流</li>
	 *                   </ul>
	 * @param outputFormat 输出图片格式，必须满足：
	 *                   <ul>
	 *                     <li>非空</li>
	 *                     <li>ImageIO支持的格式（如"PNG","JPEG"）</li>
	 *                   </ul>
	 * @param filterType   重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 是否成功写入输出流
	 * @throws IOException 当发生I/O错误时抛出，包括：
	 *                   <ul>
	 *                     <li>输出流写入失败</li>
	 *                     <li>图像编码失败</li>
	 *                   </ul>
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #scaleByHeight(BufferedImage, int)
	 * @since 1.0.0
	 */
	public static boolean scaleByHeight(final BufferedImage inputImage, final OutputStream outputStream, final int height,
										final String outputFormat, final int filterType) throws IOException {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.isTrue(height > 0, "height 必须大于0");
		Validate.notNull(outputStream, "outputFile 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");

		ImageSize outputImageSize = new ImageSize(inputImage.getWidth(), inputImage.getHeight()).scaleByHeight(height);
		BufferedImage outputImage = resample(inputImage, outputImageSize, outputFormat, filterType);
		return ImageIO.write(outputImage, outputFormat.toLowerCase(), outputStream);
	}

	/**
	 * 根据宽度等比缩放图像到输出流
	 * <p>
	 * 保持原始宽高比，根据指定宽度计算新高度并缩放图像。
	 * 适用于需要保持图像比例的输出流场景。
	 * </p>
	 *
	 * <p><b>处理流程：</b></p>
	 * <ol>
	 *   <li>校验输入参数有效性</li>
	 *   <li>计算保持比例的目标尺寸</li>
	 *   <li>使用指定滤波器进行图像重采样</li>
	 *   <li>将结果写入输出流</li>
	 * </ol>
	 *
	 * @param inputImage   原始图像，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>已加载的有效图像</li>
	 *                   </ul>
	 * @param width        目标宽度，必须满足：
	 *                   <ul>
	 *                     <li>大于0</li>
	 *                   </ul>
	 * @param outputStream 输出流，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>已打开的可写流</li>
	 *                   </ul>
	 * @param outputFormat 输出图片格式，必须满足：
	 *                   <ul>
	 *                     <li>非空</li>
	 *                     <li>ImageIO支持的格式（如"PNG","JPEG"）</li>
	 *                   </ul>
	 * @param filterType   重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 是否成功写入输出流
	 * @throws IOException 当发生I/O错误时抛出，包括：
	 *                   <ul>
	 *                     <li>输出流写入失败</li>
	 *                     <li>图像编码失败</li>
	 *                   </ul>
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #scaleByWidth(BufferedImage, OutputStream, int, String)
	 * @since 1.0.0
	 */
	public static boolean scaleByWidth(final BufferedImage inputImage, final OutputStream outputStream, final int width,
									   final String outputFormat, final int filterType) throws IOException {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.isTrue(width > 0, "width 必须大于0");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");

		ImageSize outputImageSize = new ImageSize(inputImage.getWidth(), inputImage.getHeight()).scaleByWidth(width);
		BufferedImage outputImage = resample(inputImage, outputImageSize, outputFormat, filterType);
		return ImageIO.write(outputImage, outputFormat.toLowerCase(), outputStream);
	}

	/**
	 * 根据高度或宽度等比缩放图像到输出流
	 * <p>
	 * 保持原始宽高比，根据指定尺寸计算新尺寸并缩放图像。
	 * 适用于需要保持图像比例的输出流场景。
	 * </p>
	 *
	 * <p><b>处理流程：</b></p>
	 * <ol>
	 *   <li>校验输入参数有效性</li>
	 *   <li>计算保持比例的目标尺寸</li>
	 *   <li>使用指定滤波器进行图像重采样</li>
	 *   <li>将结果写入输出流</li>
	 * </ol>
	 *
	 * @param inputImage   原始图像，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>已加载的有效图像</li>
	 *                   </ul>
	 * @param imageSize    目标尺寸，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>宽度和高度都大于0</li>
	 *                   </ul>
	 * @param outputStream 输出流，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>已打开的可写流</li>
	 *                   </ul>
	 * @param outputFormat 输出图片格式，必须满足：
	 *                   <ul>
	 *                     <li>非空</li>
	 *                     <li>ImageIO支持的格式（如"PNG","JPEG"）</li>
	 *                   </ul>
	 * @param filterType   重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 是否成功写入输出流
	 * @throws IOException 当发生I/O错误时抛出，包括：
	 *                   <ul>
	 *                     <li>输出流写入失败</li>
	 *                     <li>图像编码失败</li>
	 *                   </ul>
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see ImageSize#scale(ImageSize)
	 * @since 1.0.0
	 */
	public static boolean scale(final BufferedImage inputImage, final OutputStream outputStream, final ImageSize imageSize,
								final String outputFormat, final int filterType) throws IOException {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.notNull(imageSize, "imageSize 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");

		ImageSize outputImageSize = new ImageSize(inputImage.getWidth(), inputImage.getHeight()).scale(imageSize);
		BufferedImage outputImage = resample(inputImage, outputImageSize, outputFormat, filterType);
		return ImageIO.write(outputImage, outputFormat.toLowerCase(), outputStream);
	}

	/**
	 * 根据高度等比缩放图像并输出到文件
	 * <p>
	 * 保持原始宽高比，根据指定高度计算新宽度并缩放图像。
	 * 自动从文件名获取输出格式。
	 * </p>
	 *
	 * <p><b>文件处理：</b></p>
	 * <ul>
	 *   <li>自动创建父目录（如果不存在）</li>
	 *   <li>自动从文件名获取输出格式</li>
	 *   <li>自动处理文件系统权限问题</li>
	 * </ul>
	 *
	 * @param inputImage 原始图像，必须满足：
	 *                 <ul>
	 *                   <li>非null</li>
	 *                   <li>已加载的有效图像</li>
	 *                 </ul>
	 * @param height     目标高度，必须满足：
	 *                 <ul>
	 *                   <li>大于0</li>
	 *                 </ul>
	 * @param outputFile 输出文件，必须满足：
	 *                 <ul>
	 *                   <li>非null</li>
	 *                   <li>有效的文件路径</li>
	 *                   <li>有写入权限</li>
	 *                 </ul>
	 * @return 是否成功写入文件
	 * @throws IOException 当发生I/O错误时抛出，包括：
	 *                   <ul>
	 *                     <li>文件系统权限不足</li>
	 *                     <li>磁盘空间不足</li>
	 *                     <li>图像编码失败</li>
	 *                   </ul>
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #scaleByHeight(BufferedImage, File, int, int)
	 * @since 1.0.0
	 */
	public static boolean scaleByHeight(final BufferedImage inputImage, final File outputFile, final int height) throws IOException {
		return scaleByHeight(inputImage, outputFile, height,
			FilenameUtils.getExtension(outputFile.getName()), ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 根据宽度等比缩放图像并输出到文件
	 * <p>
	 * 保持原始宽高比，根据指定宽度计算新高度并缩放图像。
	 * 自动从文件名获取输出格式。
	 * </p>
	 *
	 * <p><b>文件处理：</b></p>
	 * <ul>
	 *   <li>自动创建父目录（如果不存在）</li>
	 *   <li>自动从文件名获取输出格式</li>
	 *   <li>自动处理文件系统权限问题</li>
	 * </ul>
	 *
	 * @param inputImage 原始图像，必须满足：
	 *                 <ul>
	 *                   <li>非null</li>
	 *                   <li>已加载的有效图像</li>
	 *                 </ul>
	 * @param width     目标宽度，必须满足：
	 *                 <ul>
	 *                   <li>大于0</li>
	 *                 </ul>
	 * @param outputFile 输出文件，必须满足：
	 *                 <ul>
	 *                   <li>非null</li>
	 *                   <li>有效的文件路径</li>
	 *                   <li>有写入权限</li>
	 *                 </ul>
	 * @return 是否成功写入文件
	 * @throws IOException 当发生I/O错误时抛出，包括：
	 *                   <ul>
	 *                     <li>文件系统权限不足</li>
	 *                     <li>磁盘空间不足</li>
	 *                     <li>图像编码失败</li>
	 *                   </ul>
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #scaleByWidth(BufferedImage, File, int, int)
	 * @since 1.0.0
	 */
	public static boolean scaleByWidth(final BufferedImage inputImage, final File outputFile, final int width) throws IOException {
		return scaleByWidth(inputImage, outputFile, width,
			FilenameUtils.getExtension(outputFile.getName()), ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 根据高度或宽度等比缩放图像并输出到文件（自动获取文件扩展名作为格式）
	 * <p>
	 * 保持原始宽高比，根据目标尺寸自动计算缩放比例。
	 * 自动从文件名获取输出格式并使用默认滤波器。
	 * </p>
	 *
	 * <p><b>特性：</b></p>
	 * <ul>
	 *   <li>自动从文件名获取输出格式</li>
	 *   <li>使用默认三角形滤波器({@link ResampleOp#FILTER_TRIANGLE})</li>
	 *   <li>自动创建父目录（如果不存在）</li>
	 * </ul>
	 *
	 * @param inputImage 原始图像，必须满足：
	 *                 <ul>
	 *                   <li>非null</li>
	 *                   <li>已加载的有效图像</li>
	 *                 </ul>
	 * @param imageSize  目标尺寸，必须满足：
	 *                 <ul>
	 *                   <li>非null</li>
	 *                   <li>宽度和高度都大于0</li>
	 *                 </ul>
	 * @param outputFile 输出文件，必须满足：
	 *                 <ul>
	 *                   <li>非null</li>
	 *                   <li>有效的文件路径</li>
	 *                   <li>有写入权限</li>
	 *                 </ul>
	 * @return 是否成功写入文件
	 * @throws IOException 当发生I/O错误时抛出，包括：
	 *                   <ul>
	 *                     <li>文件系统权限不足</li>
	 *                     <li>磁盘空间不足</li>
	 *                     <li>图像编码失败</li>
	 *                   </ul>
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #scale(BufferedImage, File, ImageSize, String, int)
	 * @since 1.0.0
	 */
	public static boolean scale(final BufferedImage inputImage, final File outputFile, final ImageSize imageSize) throws IOException {
		return scale(inputImage, outputFile, imageSize,
			FilenameUtils.getExtension(outputFile.getName()), ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 根据高度等比缩放图像并输出到文件（自动获取文件扩展名作为格式）
	 * <p>
	 * 保持原始宽高比，根据指定高度计算新宽度并缩放图像。
	 * 自动从文件名获取输出格式。
	 * </p>
	 *
	 * <p><b>特性：</b></p>
	 * <ul>
	 *   <li>自动从文件名获取输出格式</li>
	 *   <li>自动创建父目录（如果不存在）</li>
	 *   <li>支持自定义重采样滤波器</li>
	 * </ul>
	 *
	 * @param inputImage 原始图像，必须满足：
	 *                 <ul>
	 *                   <li>非null</li>
	 *                   <li>已加载的有效图像</li>
	 *                 </ul>
	 * @param height     目标高度，必须满足：
	 *                 <ul>
	 *                   <li>大于0</li>
	 *                 </ul>
	 * @param outputFile 输出文件，必须满足：
	 *                 <ul>
	 *                   <li>非null</li>
	 *                   <li>有效的文件路径</li>
	 *                   <li>有写入权限</li>
	 *                 </ul>
	 * @param filterType 重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 是否成功写入文件
	 * @throws IOException 当发生I/O错误时抛出，包括：
	 *                   <ul>
	 *                     <li>文件系统权限不足</li>
	 *                     <li>磁盘空间不足</li>
	 *                     <li>图像编码失败</li>
	 *                   </ul>
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #scaleByHeight(BufferedImage, File, int, String, int)
	 * @since 1.0.0
	 */
	public static boolean scaleByHeight(final BufferedImage inputImage, final File outputFile, final int height,
										final int filterType) throws IOException {
		return scaleByHeight(inputImage, outputFile, height,
			FilenameUtils.getExtension(outputFile.getName()), filterType);
	}

	/**
	 * 根据宽度等比缩放图像并输出到文件（自动获取文件扩展名作为格式）
	 * <p>
	 * 保持原始宽高比，根据指定宽度计算新高度并缩放图像。
	 * 自动从文件名获取输出格式。
	 * </p>
	 *
	 * <p><b>特性：</b></p>
	 * <ul>
	 *   <li>自动从文件名获取输出格式</li>
	 *   <li>自动创建父目录（如果不存在）</li>
	 *   <li>支持自定义重采样滤波器</li>
	 * </ul>
	 *
	 * @param inputImage 原始图像，必须满足：
	 *                 <ul>
	 *                   <li>非null</li>
	 *                   <li>已加载的有效图像</li>
	 *                 </ul>
	 * @param width      目标宽度，必须满足：
	 *                 <ul>
	 *                   <li>大于0</li>
	 *                 </ul>
	 * @param outputFile 输出文件，必须满足：
	 *                 <ul>
	 *                   <li>非null</li>
	 *                   <li>有效的文件路径</li>
	 *                   <li>有写入权限</li>
	 *                 </ul>
	 * @param filterType 重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 是否成功写入文件
	 * @throws IOException 当发生I/O错误时抛出，包括：
	 *                   <ul>
	 *                     <li>文件系统权限不足</li>
	 *                     <li>磁盘空间不足</li>
	 *                     <li>图像编码失败</li>
	 *                   </ul>
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #scaleByWidth(BufferedImage, File, int, String, int)
	 * @since 1.0.0
	 */
	public static boolean scaleByWidth(final BufferedImage inputImage, final File outputFile, final int width,
									   final int filterType) throws IOException {
		return scaleByWidth(inputImage, outputFile, width,
			FilenameUtils.getExtension(outputFile.getName()), filterType);
	}

	/**
	 * 根据高度或宽度等比缩放图像并输出到文件（自动获取文件扩展名作为格式）
	 * <p>
	 * 保持原始宽高比，根据目标尺寸自动计算缩放比例。
	 * 自动从文件名获取输出格式并使用指定滤波器。
	 * </p>
	 *
	 * <p><b>特性：</b></p>
	 * <ul>
	 *   <li>自动从文件名获取输出格式</li>
	 *   <li>自动创建父目录（如果不存在）</li>
	 *   <li>支持自定义重采样滤波器</li>
	 * </ul>
	 *
	 * @param inputImage 原始图像，必须满足：
	 *                 <ul>
	 *                   <li>非null</li>
	 *                   <li>已加载的有效图像</li>
	 *                 </ul>
	 * @param imageSize  目标尺寸，必须满足：
	 *                 <ul>
	 *                   <li>非null</li>
	 *                   <li>宽度和高度都大于0</li>
	 *                 </ul>
	 * @param outputFile 输出文件，必须满足：
	 *                 <ul>
	 *                   <li>非null</li>
	 *                   <li>有效的文件路径</li>
	 *                   <li>有写入权限</li>
	 *                 </ul>
	 * @param filterType 重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 是否成功写入文件
	 * @throws IOException 当发生I/O错误时抛出，包括：
	 *                   <ul>
	 *                     <li>文件系统权限不足</li>
	 *                     <li>磁盘空间不足</li>
	 *                     <li>图像编码失败</li>
	 *                   </ul>
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #scale(BufferedImage, File, ImageSize, String, int)
	 * @since 1.0.0
	 */
	public static boolean scale(final BufferedImage inputImage, final File outputFile, final ImageSize imageSize,
								final int filterType) throws IOException {
		return scale(inputImage, outputFile, imageSize,
			FilenameUtils.getExtension(outputFile.getName()), filterType);
	}

	/**
	 * 根据高度等比缩放图像并输出到文件
	 * <p>
	 * 保持原始宽高比，根据指定高度计算新宽度并缩放图像。
	 * 适用于需要保持图像比例的文件输出场景。
	 * </p>
	 *
	 * <p><b>文件处理：</b></p>
	 * <ul>
	 *   <li>自动创建父目录（如果不存在）</li>
	 *   <li>自动处理文件系统权限问题</li>
	 *   <li>自动转换图像格式（如JPG转PNG时的透明通道处理）</li>
	 * </ul>
	 *
	 * @param inputImage   原始图像，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>已加载的有效图像</li>
	 *                   </ul>
	 * @param height       目标高度，必须满足：
	 *                   <ul>
	 *                     <li>大于0</li>
	 *                   </ul>
	 * @param outputFile   输出文件，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>有效的文件路径</li>
	 *                     <li>有写入权限</li>
	 *                   </ul>
	 * @param outputFormat 输出图片格式，必须满足：
	 *                   <ul>
	 *                     <li>非空</li>
	 *                     <li>ImageIO支持的格式（如"PNG","JPEG"）</li>
	 *                   </ul>
	 * @return 是否成功写入文件
	 * @throws IOException 当发生I/O错误时抛出，包括：
	 *                   <ul>
	 *                     <li>文件系统权限不足</li>
	 *                     <li>磁盘空间不足</li>
	 *                     <li>图像编码失败</li>
	 *                   </ul>
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #scaleByHeight(BufferedImage, File, int, String, int)
	 * @since 1.0.0
	 */
	public static boolean scaleByHeight(final BufferedImage inputImage, final File outputFile, final int height,
										final String outputFormat) throws IOException {
		return scaleByHeight(inputImage, outputFile, height, outputFormat, ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 根据宽度等比缩放图像并输出到文件
	 * <p>
	 * 保持原始宽高比，根据指定宽度计算新高度并缩放图像。
	 * 适用于需要保持图像比例的文件输出场景。
	 * </p>
	 *
	 * <p><b>文件处理：</b></p>
	 * <ul>
	 *   <li>自动创建父目录（如果不存在）</li>
	 *   <li>自动处理文件系统权限问题</li>
	 *   <li>自动转换图像格式（如JPG转PNG时的透明通道处理）</li>
	 * </ul>
	 *
	 * @param inputImage   原始图像，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>已加载的有效图像</li>
	 *                   </ul>
	 * @param width        目标宽度，必须满足：
	 *                   <ul>
	 *                     <li>大于0</li>
	 *                   </ul>
	 * @param outputFile   输出文件，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>有效的文件路径</li>
	 *                     <li>有写入权限</li>
	 *                   </ul>
	 * @param outputFormat 输出图片格式，必须满足：
	 *                   <ul>
	 *                     <li>非空</li>
	 *                     <li>ImageIO支持的格式（如"PNG","JPEG"）</li>
	 *                   </ul>
	 * @return 是否成功写入文件
	 * @throws IOException 当发生I/O错误时抛出，包括：
	 *                   <ul>
	 *                     <li>文件系统权限不足</li>
	 *                     <li>磁盘空间不足</li>
	 *                     <li>图像编码失败</li>
	 *                   </ul>
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #scaleByWidth(BufferedImage, File, int, String, int)
	 * @since 1.0.0
	 */
	public static boolean scaleByWidth(final BufferedImage inputImage, final File outputFile, final int width,
									   final String outputFormat) throws IOException {
		return scaleByWidth(inputImage, outputFile, width, outputFormat, ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 根据高度或宽度等比缩放图像并输出到文件
	 * <p>
	 * 保持原始宽高比，根据目标尺寸自动计算缩放比例。
	 * 适用于需要保持图像比例的文件输出场景。
	 * </p>
	 *
	 * <p><b>文件处理：</b></p>
	 * <ul>
	 *   <li>自动创建父目录（如果不存在）</li>
	 *   <li>自动处理文件系统权限问题</li>
	 *   <li>自动转换图像格式（如JPG转PNG时的透明通道处理）</li>
	 * </ul>
	 *
	 * @param inputImage   原始图像，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>已加载的有效图像</li>
	 *                   </ul>
	 * @param imageSize    目标尺寸，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>宽度和高度都大于0</li>
	 *                   </ul>
	 * @param outputFile   输出文件，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>有效的文件路径</li>
	 *                     <li>有写入权限</li>
	 *                   </ul>
	 * @param outputFormat 输出图片格式，必须满足：
	 *                   <ul>
	 *                     <li>非空</li>
	 *                     <li>ImageIO支持的格式（如"PNG","JPEG"")</li>
	 *                   </ul>
	 * @return 是否成功写入文件
	 * @throws IOException 当发生I/O错误时抛出，包括：
	 *                   <ul>
	 *                     <li>文件系统权限不足</li>
	 *                     <li>磁盘空间不足</li>
	 *                     <li>图像编码失败</li>
	 *                   </ul>
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see ImageSize#scale(ImageSize)
	 * @since 1.0.0
	 */
	public static boolean scale(final BufferedImage inputImage, final File outputFile, final ImageSize imageSize,
								final String outputFormat) throws IOException {
		return scale(inputImage, outputFile, imageSize, outputFormat, ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 根据高度等比缩放图像并输出到文件
	 * <p>
	 * 保持原始宽高比，根据指定高度计算新宽度并缩放图像。
	 * 适用于需要保持图像比例的文件输出场景。
	 * </p>
	 *
	 * <p><b>处理流程：</b></p>
	 * <ol>
	 *   <li>校验输入参数有效性</li>
	 *   <li>计算保持比例的目标尺寸</li>
	 *   <li>使用指定滤波器进行图像重采样</li>
	 *   <li>将结果写入输出文件</li>
	 * </ol>
	 *
	 * @param inputImage   原始图像，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>已加载的有效图像</li>
	 *                   </ul>
	 * @param height       目标高度，必须满足：
	 *                   <ul>
	 *                     <li>大于0</li>
	 *                   </ul>
	 * @param outputFile   输出文件，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>有效的文件路径</li>
	 *                     <li>有写入权限</li>
	 *                   </ul>
	 * @param outputFormat 输出图片格式，必须满足：
	 *                   <ul>
	 *                     <li>非空</li>
	 *                     <li>ImageIO支持的格式（如"PNG","JPEG"）</li>
	 *                   </ul>
	 * @param filterType   重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 是否成功写入文件
	 * @throws IOException 当发生I/O错误时抛出，包括：
	 *                   <ul>
	 *                     <li>文件系统权限不足</li>
	 *                     <li>磁盘空间不足</li>
	 *                     <li>图像编码失败</li>
	 *                   </ul>
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #scaleByHeight(BufferedImage, File, int, String)
	 * @since 1.0.0
	 */
	public static boolean scaleByHeight(final BufferedImage inputImage, final File outputFile, final int height,
										final String outputFormat, final int filterType) throws IOException {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.isTrue(height > 0, "height 必须大于0");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		ImageSize outputImageSize = new ImageSize(inputImage.getWidth(), inputImage.getHeight()).scaleByHeight(height);
		BufferedImage outputImage = resample(inputImage, outputImageSize, outputFormat, filterType);
		return ImageIO.write(outputImage, outputFormat.toLowerCase(), outputFile);
	}

	/**
	 * 根据宽度等比缩放图像并输出到文件
	 * <p>
	 * 保持原始宽高比，根据指定宽度计算新高度并缩放图像。
	 * 适用于需要保持图像比例的文件输出场景。
	 * </p>
	 *
	 * <p><b>处理流程：</b></p>
	 * <ol>
	 *   <li>校验输入参数有效性</li>
	 *   <li>计算保持比例的目标尺寸</li>
	 *   <li>使用指定滤波器进行图像重采样</li>
	 *   <li>将结果写入输出文件</li>
	 * </ol>
	 *
	 * @param inputImage   原始图像，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>已加载的有效图像</li>
	 *                   </ul>
	 * @param width        目标宽度，必须满足：
	 *                   <ul>
	 *                     <li>大于0</li>
	 *                   </ul>
	 * @param outputFile   输出文件，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>有效的文件路径</li>
	 *                     <li>有写入权限</li>
	 *                   </ul>
	 * @param outputFormat 输出图片格式，必须满足：
	 *                   <ul>
	 *                     <li>非空</li>
	 *                     <li>ImageIO支持的格式（如"PNG","JPEG"）</li>
	 *                   </ul>
	 * @param filterType   重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 是否成功写入文件
	 * @throws IOException 当发生I/O错误时抛出，包括：
	 *                   <ul>
	 *                     <li>文件系统权限不足</li>
	 *                     <li>磁盘空间不足</li>
	 *                     <li>图像编码失败</li>
	 *                   </ul>
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #scaleByWidth(BufferedImage, File, int, String)
	 * @since 1.0.0
	 */
	public static boolean scaleByWidth(final BufferedImage inputImage, final File outputFile, final int width,
									   final String outputFormat, final int filterType) throws IOException {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.isTrue(width > 0, "width 必须大于0");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		ImageSize outputImageSize = new ImageSize(inputImage.getWidth(), inputImage.getHeight()).scaleByWidth(width);
		BufferedImage outputImage = resample(inputImage, outputImageSize, outputFormat, filterType);
		return ImageIO.write(outputImage, outputFormat.toLowerCase(), outputFile);
	}

	/**
	 * 根据高度或宽度等比缩放图像并输出到文件
	 * <p>
	 * 保持原始宽高比，根据目标尺寸自动计算缩放比例。
	 * 适用于需要保持图像比例的文件输出场景。
	 * </p>
	 *
	 * <p><b>处理流程：</b></p>
	 * <ol>
	 *   <li>校验输入参数有效性</li>
	 *   <li>计算保持比例的目标尺寸</li>
	 *   <li>使用指定滤波器进行图像重采样</li>
	 *   <li>将结果写入输出文件</li>
	 * </ol>
	 *
	 * @param inputImage   原始图像，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>已加载的有效图像</li>
	 *                   </ul>
	 * @param imageSize    目标尺寸，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>宽度和高度都大于0</li>
	 *                   </ul>
	 * @param outputFile   输出文件，必须满足：
	 *                   <ul>
	 *                     <li>非null</li>
	 *                     <li>有效的文件路径</li>
	 *                     <li>有写入权限</li>
	 *                   </ul>
	 * @param outputFormat 输出图片格式，必须满足：
	 *                   <ul>
	 *                     <li>非空</li>
	 *                     <li>ImageIO支持的格式（如"PNG","JPEG"）</li>
	 *                   </ul>
	 * @param filterType   重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 是否成功写入文件
	 * @throws IOException 当发生I/O错误时抛出，包括：
	 *                   <ul>
	 *                     <li>文件系统权限不足</li>
	 *                     <li>磁盘空间不足</li>
	 *                     <li>图像编码失败</li>
	 *                   </ul>
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see ImageSize#scale(ImageSize)
	 * @see #resample(BufferedImage, ImageSize, String, int)
	 * @since 1.0.0
	 */
	public static boolean scale(final BufferedImage inputImage, final File outputFile, final ImageSize imageSize,
								final String outputFormat, final int filterType) throws IOException {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.notNull(imageSize, "imageSize 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		ImageSize outputImageSize = new ImageSize(inputImage.getWidth(), inputImage.getHeight()).scale(imageSize);
		BufferedImage outputImage = resample(inputImage, outputImageSize, outputFormat, filterType);
		return ImageIO.write(outputImage, outputFormat.toLowerCase(), outputFile);
	}

	/**
	 * 缩放图像到指定输出图像尺寸
	 * <p>
	 * 使用默认三角形滤波器({@link ResampleOp#FILTER_TRIANGLE})进行图像重采样，
	 * 适用于需要直接操作图像对象的场景。
	 * </p>
	 *
	 * <p><b>示例：</b></p>
	 * <blockquote><pre>
	 * // 创建RGB格式输出图像
	 * BufferedImage rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	 * ThumbnailUtils.scale(sourceImage, rgbImage);
	 *
	 * // 创建ARGB格式输出图像(支持透明通道)
	 * BufferedImage argbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	 * ThumbnailUtils.scale(sourceImage, argbImage);
	 * </pre></blockquote>
	 *
	 * @param inputImage  原始图像，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>已加载的有效图像</li>
	 *                  </ul>
	 * @param outputImage 输出图像，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>已初始化的有效图像对象</li>
	 *                    <li>尺寸与预期输出尺寸一致</li>
	 *                  </ul>
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #scale(BufferedImage, BufferedImage, int)
	 * @since 1.0.0
	 */
	public static void scale(final BufferedImage inputImage, final BufferedImage outputImage) {
		scale(inputImage, outputImage, ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 缩放图像到指定输出图像尺寸
	 * <p>
	 * 使用指定滤波器进行图像重采样，适用于需要自定义重采样质量的场景。
	 * </p>
	 *
	 * <p><b>示例：</b></p>
	 * <blockquote><pre>
	 * // 使用三角形滤波器创建RGB格式输出图像
	 * BufferedImage rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	 * ThumbnailUtils.scale(sourceImage, rgbImage, ResampleOp.FILTER_TRIANGLE);
	 *
	 * // 使用双三次滤波器创建ARGB格式输出图像
	 * BufferedImage argbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	 * ThumbnailUtils.scale(sourceImage, argbImage, ResampleOp.FILTER_BICUBIC);
	 * </pre></blockquote>
	 *
	 * @param inputImage  原始图像，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>已加载的有效图像</li>
	 *                  </ul>
	 * @param outputImage 输出图像，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>已初始化的有效图像对象</li>
	 *                    <li>尺寸与预期输出尺寸一致</li>
	 *                  </ul>
	 * @param filterType  重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see ResampleOp
	 * @since 1.0.0
	 */
	public static void scale(final BufferedImage inputImage, final BufferedImage outputImage, final int filterType) {
		Validate.notNull(inputImage, "inputImage 不可为 null");
		Validate.notNull(outputImage, "outputImage 不可为 null");

		ResampleOp resampleOp = new ResampleOp(outputImage.getWidth(), outputImage.getHeight(), filterType);
		resampleOp.filter(inputImage, outputImage);
	}

	/**
	 * 根据高度等比缩放图像并返回BufferedImage
	 * <p>
	 * 保持原始宽高比，根据指定高度计算新宽度并缩放图像。
	 * 适用于需要获取缩放后图像对象进行进一步处理的场景。
	 * </p>
	 *
	 * @param image   原始图像，必须满足：
	 *              <ul>
	 *                <li>非null</li>
	 *                <li>已加载的有效图像</li>
	 *              </ul>
	 * @param height  目标高度，必须满足：
	 *              <ul>
	 *                <li>大于0</li>
	 *              </ul>
	 * @return 缩放后的图像对象
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #scaleByHeight(BufferedImage, int, int)
	 * @since 1.0.0
	 */
	public static BufferedImage scaleByHeight(final BufferedImage image, final int height) {
		return scaleByHeight(image, height, ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 根据宽度等比缩放图像并返回BufferedImage
	 * <p>
	 * 保持原始宽高比，根据指定宽度计算新高度并缩放图像。
	 * 适用于需要获取缩放后图像对象进行进一步处理的场景。
	 * </p>
	 *
	 * @param image   原始图像，必须满足：
	 *              <ul>
	 *                <li>非null</li>
	 *                <li>已加载的有效图像</li>
	 *              </ul>
	 * @param width   目标宽度，必须满足：
	 *              <ul>
	 *                <li>大于0</li>
	 *              </ul>
	 * @return 缩放后的图像对象
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #scaleByWidth(BufferedImage, int, int)
	 * @since 1.0.0
	 */
	public static BufferedImage scaleByWidth(final BufferedImage image, final int width) {
		return scaleByWidth(image, width, ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 根据高度或宽度等比缩放图像并返回BufferedImage
	 * <p>
	 * 保持原始宽高比，根据目标尺寸自动计算缩放比例。
	 * 适用于需要获取缩放后图像对象进行进一步处理的场景。
	 * </p>
	 *
	 * <p><b>处理流程：</b></p>
	 * <ol>
	 *   <li>校验输入参数有效性</li>
	 *   <li>计算保持比例的目标尺寸</li>
	 *   <li>使用默认三角形滤波器进行图像重采样</li>
	 * </ol>
	 *
	 * @param image     原始图像，必须满足：
	 *                <ul>
	 *                  <li>非null</li>
	 *                  <li>已加载的有效图像</li>
	 *                </ul>
	 * @param imageSize 目标尺寸，必须满足：
	 *                <ul>
	 *                  <li>非null</li>
	 *                  <li>宽度和高度都大于0</li>
	 *                </ul>
	 * @return 缩放后的图像对象
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #scale(BufferedImage, ImageSize, int)
	 * @see ImageSize#scale(ImageSize)
	 * @since 1.0.0
	 */
	public static BufferedImage scale(final BufferedImage image, final ImageSize imageSize) {
		return scale(image, imageSize, ResampleOp.FILTER_TRIANGLE);
	}

	/**
	 * 根据高度等比缩放图像并返回BufferedImage
	 * <p>
	 * 保持原始宽高比，根据指定高度计算新宽度并缩放图像。
	 * 适用于需要精确控制输出高度的场景。
	 * </p>
	 *
	 * <p><b>处理流程：</b></p>
	 * <ol>
	 *   <li>校验输入参数有效性</li>
	 *   <li>计算保持比例的目标尺寸</li>
	 *   <li>使用指定滤波器进行图像重采样</li>
	 * </ol>
	 *
	 * @param image      原始图像，必须满足：
	 *                 <ul>
	 *                   <li>非null</li>
	 *                   <li>已加载的有效图像</li>
	 *                 </ul>
	 * @param height     目标高度，必须满足：
	 *                 <ul>
	 *                   <li>大于0</li>
	 *                 </ul>
	 * @param filterType 重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 缩放后的图像对象
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #scaleByHeight(BufferedImage, int)
	 * @since 1.0.0
	 */
	public static BufferedImage scaleByHeight(final BufferedImage image, final int height, final int filterType) {
		Validate.notNull(image, "image 不可为 null");
		Validate.isTrue(height > 0, "height 必须大于0");

		ImageSize outputImageSize = new ImageSize(image.getWidth(), image.getHeight()).scaleByHeight(height);
		return resample(image, outputImageSize, filterType);
	}

	/**
	 * 根据宽度等比缩放图像并返回BufferedImage
	 * <p>
	 * 保持原始宽高比，根据指定宽度计算新高度并缩放图像。
	 * 适用于需要精确控制输出宽度的场景。
	 * </p>
	 *
	 * <p><b>处理流程：</b></p>
	 * <ol>
	 *   <li>校验输入参数有效性</li>
	 *   <li>计算保持比例的目标尺寸</li>
	 *   <li>使用指定滤波器进行图像重采样</li>
	 * </ol>
	 *
	 * @param image      原始图像，必须满足：
	 *                 <ul>
	 *                   <li>非null</li>
	 *                   <li>已加载的有效图像</li>
	 *                 </ul>
	 * @param width      目标宽度，必须满足：
	 *                 <ul>
	 *                   <li>大于0</li>
	 *                 </ul>
	 * @param filterType 重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 缩放后的图像对象
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see #scaleByWidth(BufferedImage, int)
	 * @since 1.0.0
	 */
	public static BufferedImage scaleByWidth(final BufferedImage image, final int width, final int filterType) {
		Validate.notNull(image, "image 不可为 null");
		Validate.isTrue(width > 0, "width 必须大于0");

		ImageSize outputImageSize = new ImageSize(image.getWidth(), image.getHeight()).scaleByWidth(width);
		return resample(image, outputImageSize, filterType);
	}

	/**
	 * 根据高度或宽度等比缩放图像并返回BufferedImage
	 * <p>
	 * 保持原始宽高比，根据目标尺寸自动计算缩放比例。
	 * 适用于需要自定义重采样滤波器的场景。
	 * </p>
	 *
	 * <p><b>处理流程：</b></p>
	 * <ol>
	 *   <li>校验输入参数有效性</li>
	 *   <li>计算保持比例的目标尺寸</li>
	 *   <li>使用指定滤波器进行图像重采样</li>
	 * </ol>
	 *
	 * @param image      原始图像，必须满足：
	 *                 <ul>
	 *                   <li>非null</li>
	 *                   <li>已加载的有效图像</li>
	 *                 </ul>
	 * @param imageSize  目标尺寸，必须满足：
	 *                 <ul>
	 *                   <li>非null</li>
	 *                   <li>宽度和高度都大于0</li>
	 *                 </ul>
	 * @param filterType 重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 缩放后的图像对象
	 * @throws IllegalArgumentException 当任一参数无效时抛出
	 * @see ImageSize#scale(ImageSize)
	 * @see #scale(BufferedImage, ImageSize)
	 * @since 1.0.0
	 */
	public static BufferedImage scale(final BufferedImage image, final ImageSize imageSize, final int filterType) {
		Validate.notNull(image, "image 不可为 null");
		Validate.notNull(imageSize, "imageSize 不可为 null");

		ImageSize outputImageSize = new ImageSize(image.getWidth(), image.getHeight()).scale(imageSize);
		return resample(image, outputImageSize, filterType);
	}

	/**
	 * 图像重采样方法（自动处理透明通道）
	 * <p>
	 * 根据输出格式自动处理图像色彩模式，确保格式兼容性：
	 * <ul>
	 *   <li>对于不支持透明的格式（如JPG/JPEG），自动转换为RGB模式</li>
	 *   <li>对于支持透明的格式（如PNG），保留原始Alpha通道</li>
	 * </ul>
	 * </p>
	 *
	 * <p><b>处理流程：</b></p>
	 * <ol>
	 *   <li>检查输出格式是否支持透明通道</li>
	 *   <li>创建适当色彩模式的输出图像</li>
	 *   <li>应用指定滤波器进行重采样</li>
	 * </ol>
	 *
	 * @param inputImage      原始图像，必须满足：
	 *                      <ul>
	 *                        <li>非null</li>
	 *                        <li>已加载的有效图像</li>
	 *                      </ul>
	 * @param outputImageSize 输出尺寸，必须满足：
	 *                      <ul>
	 *                        <li>非null</li>
	 *                        <li>宽度和高度都大于0</li>
	 *                      </ul>
	 * @param outputFormat    输出图片格式，必须满足：
	 *                      <ul>
	 *                        <li>非空</li>
	 *                        <li>ImageIO支持的格式（如"PNG","JPEG"）</li>
	 *                      </ul>
	 * @param filterType      重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 重采样后的图像对象
	 * @see ImageConstants#NON_TRANSPARENT_IMAGE_FORMATS
	 * @since 1.0.0
	 */
	protected static BufferedImage resample(final BufferedImage inputImage, final ImageSize outputImageSize,
											final String outputFormat, final int filterType) {
		if (ImageConstants.NON_TRANSPARENT_IMAGE_FORMATS.contains(outputFormat.toLowerCase())) {
			BufferedImage outputImage = new BufferedImage(outputImageSize.width(), outputImageSize.height(),
				BufferedImage.TYPE_INT_RGB);
			ResampleOp resampleOp = new ResampleOp(outputImageSize.width(), outputImageSize.height(), filterType);
			resampleOp.filter(inputImage, outputImage);
			return outputImage;
		} else {
			return resample(inputImage, outputImageSize, filterType);
		}
	}

	/**
	 * 基础图像重采样方法
	 * <p>
	 * 执行图像尺寸调整的核心方法，保留原始图像的色彩模式。
	 * 适用于需要保持原始图像特性的场景。
	 * </p>
	 *
	 * @param inputImage      原始图像，必须满足：
	 *                      <ul>
	 *                        <li>非null</li>
	 *                        <li>已加载的有效图像</li>
	 *                      </ul>
	 * @param outputImageSize 输出尺寸，必须满足：
	 *                      <ul>
	 *                        <li>非null</li>
	 *                        <li>宽度和高度都大于0</li>
	 *                      </ul>
	 * @param filterType      重采样滤波器类型，建议使用{@link ResampleOp#FILTER_TRIANGLE}
	 * @return 重采样后的图像对象
	 * @see ResampleOp
	 * @since 1.0.0
	 */
	protected static BufferedImage resample(final BufferedImage inputImage, final ImageSize outputImageSize,
											final int filterType) {
		ResampleOp resampleOp = new ResampleOp(outputImageSize.width(), outputImageSize.height(), filterType);
		return resampleOp.filter(inputImage, null);
	}
}