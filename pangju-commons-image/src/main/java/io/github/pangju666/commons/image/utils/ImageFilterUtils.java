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

import com.twelvemonkeys.image.BrightnessContrastFilter;
import com.twelvemonkeys.image.GrayFilter;
import com.twelvemonkeys.image.ImageUtil;
import io.github.pangju666.commons.image.lang.ImageConstants;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.FilenameUtils;
import org.apache.commons.lang3.Validate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 图像滤镜处理工具类
 * <p>
 * 提供专业的图像滤镜处理功能，包括灰度化、亮度/对比度调整等核心操作，
 * 支持多种输出格式和输出方式，所有操作均保持线程安全。
 * </p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li><strong>多格式支持</strong> - 支持PNG/JPG/BMP等常见图像格式</li>
 *   <li><strong>无损处理</strong> - 所有操作均不影响原始图像</li>
 *   <li><strong>高性能</strong> - 基于高效图像处理算法实现</li>
 * </ul>
 *
 * <h3>典型应用场景</h3>
 * <ul>
 *   <li>图像预处理</li>
 *   <li>照片效果增强</li>
 *   <li>医学影像处理</li>
 *   <li>计算机视觉预处理</li>
 * </ul>
 *
 * @since 1.0.0
 */
public class ImageFilterUtils {
	/**
	 * 灰度化滤镜实例
	 * <p>
	 * 使用标准灰度转换算法(30%红 + 59%绿 + 11%蓝)，
	 * 适用于黑白图像生成场景。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	protected static final GrayFilter GRAY_FILTER = new GrayFilter();

	protected ImageFilterUtils() {
	}

	/**
	 * 将图像灰度化并输出到流
	 * <p>
	 * 使用标准灰度转换算法处理图像，并将结果写入输出流，
	 * 适用于需要直接输出灰度图像的场景。
	 * </p>
	 *
	 * @param inputImage 原始图像对象，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>已加载的有效图像</li>
	 *                  </ul>
	 * @param outputStream 输出流对象，必须满足：
	 *                    <ul>
	 *                      <li>非null</li>
	 *                      <li>已打开的可写流</li>
	 *                    </ul>
	 * @param outputFormat 输出图像格式，必须满足：
	 *                    <ul>
	 *                      <li>非空字符串</li>
	 *                      <li>ImageIO支持的格式(如"png","jpg")</li>
	 *                    </ul>
	 * @throws IOException 当发生以下情况时抛出：
	 *                    <ul>
	 *                      <li>输出流写入失败</li>
	 *                      <li>图像编码失败</li>
	 *                    </ul>
	 * @throws IllegalArgumentException 当参数验证失败时抛出
	 * @since 1.0.0
	 */
	public static void grayscale(final BufferedImage inputImage, final OutputStream outputStream,
								 final String outputFormat) throws IOException {
		filter(inputImage, GRAY_FILTER, outputStream, outputFormat);
	}

	/**
	 * 将图像灰度化并输出到文件（自动获取文件扩展名作为格式）
	 * <p>
	 * 自动从文件名提取格式扩展名，支持常见图片格式，
	 * 根据格式自动处理透明通道。
	 * </p>
	 *
	 * @param inputImage 原始图像对象，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>已加载的有效图像</li>
	 *                  </ul>
	 * @param outputFile 输出文件对象，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>具有写入权限的有效路径</li>
	 *                    <li>包含有效的图片格式扩展名</li>
	 *                  </ul>
	 * @throws IOException 当发生以下情况时抛出：
	 *                    <ul>
	 *                      <li>文件写入失败</li>
	 *                      <li>路径不可写</li>
	 *                      <li>图像编码失败</li>
	 *                    </ul>
	 * @throws IllegalArgumentException 当参数验证失败时抛出
	 * @since 1.0.0
	 */
	public static void grayscale(final BufferedImage inputImage, final File outputFile) throws IOException {
		filter(inputImage, GRAY_FILTER, outputFile);
	}

	/**
	 * 将图像灰度化并保存为指定格式
	 * <p>
	 * 支持指定输出格式，根据格式自动选择最佳图像类型，
	 * 确保输出文件质量最优。
	 * </p>
	 *
	 * @param inputImage 原始图像对象，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>已加载的有效图像</li>
	 *                  </ul>
	 * @param outputFile 输出文件对象，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>具有写入权限的有效路径</li>
	 *                  </ul>
	 * @param outputFormat 输出图像格式，必须满足：
	 *                    <ul>
	 *                      <li>非空字符串</li>
	 *                      <li>ImageIO支持的格式(如"png","jpg")</li>
	 *                    </ul>
	 * @throws IOException 当发生以下情况时抛出：
	 *                    <ul>
	 *                      <li>文件写入失败</li>
	 *                      <li>路径不可写</li>
	 *                      <li>图像编码失败</li>
	 *                    </ul>
	 * @throws IllegalArgumentException 当参数验证失败时抛出
	 * @since 1.0.0
	 */
	public static void grayscale(final BufferedImage inputImage, final File outputFile,
								 final String outputFormat) throws IOException {
		filter(inputImage, GRAY_FILTER, outputFile, outputFormat);
	}

	/**
	 * 对图像进行灰度化处理
	 * <p>
	 * 使用标准灰度转换算法处理图像，
	 * 返回新的BufferedImage对象，原始图像不受影响。
	 * </p>
	 *
	 * @param image 原始图像对象，必须满足：
	 *             <ul>
	 *               <li>非null</li>
	 *               <li>已加载的有效图像</li>
	 *             </ul>
	 * @return 处理后的新BufferedImage对象
	 * @throws IllegalArgumentException 当参数验证失败时抛出
	 * @since 1.0.0
	 */
	public static BufferedImage grayscale(final BufferedImage image) {
		return filter(image, GRAY_FILTER);
	}

	/**
	 * 对图像进行灰度化处理（指定输出类型）
	 * <p>
	 * 支持指定输出图像类型，适用于需要精确控制输出格式的场景，
	 * 返回新的BufferedImage对象，原始图像不受影响。
	 * </p>
	 *
	 * @param image 原始图像对象，必须满足：
	 *             <ul>
	 *               <li>非null</li>
	 *               <li>已加载的有效图像</li>
	 *             </ul>
	 * @param imageType 目标图像类型，常用值：
	 *                 <ul>
	 *                   <li>BufferedImage.TYPE_INT_RGB</li>
	 *                   <li>BufferedImage.TYPE_INT_ARGB</li>
	 *                   <li>BufferedImage.TYPE_3BYTE_BGR</li>
	 *                 </ul>
	 * @return 处理后的新BufferedImage对象
	 * @throws IllegalArgumentException 当参数验证失败时抛出
	 * @since 1.0.0
	 */
	public static BufferedImage grayscale(final BufferedImage image, int imageType) {
		return filter(image, GRAY_FILTER, imageType);
	}

	/**
	 * 增加图像对比度并输出到流
	 * <p>
	 * 调整图像对比度并将结果写入输出流，支持PNG/JPG等常见格式，
	 * 对比度调整范围为[-1.0, 1.0]，0表示不调整。
	 * </p>
	 *
	 * @param inputImage 原始图像对象，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>已加载的有效图像</li>
	 *                  </ul>
	 * @param amount 对比度调整值，必须满足：
	 *              <ul>
	 *                <li>范围在[-1.0, 1.0]之间</li>
	 *                <li>0表示不调整</li>
	 *              </ul>
	 * @param outputStream 输出流对象，必须满足：
	 *                    <ul>
	 *                      <li>非null</li>
	 *                      <li>已打开的可写流</li>
	 *                    </ul>
	 * @param outputFormat 输出图像格式，必须满足：
	 *                    <ul>
	 *                      <li>非空字符串</li>
	 *                      <li>ImageIO支持的格式(如"png","jpg")</li>
	 *                    </ul>
	 * @throws IOException 当发生以下情况时抛出：
	 *                    <ul>
	 *                      <li>输出流写入失败</li>
	 *                      <li>图像编码失败</li>
	 *                    </ul>
	 * @throws IllegalArgumentException 当参数验证失败时抛出
	 * @since 1.0.0
	 */
	public static void contrast(final BufferedImage inputImage, final float amount,
								final OutputStream outputStream, final String outputFormat) throws IOException {
		if (amount != 0f) {
			Validate.inclusiveBetween(-1.0, 1.0, amount);
			BrightnessContrastFilter filter = new BrightnessContrastFilter(0f, amount);
			filter(inputImage, filter, outputStream, outputFormat);
		}
	}

	/**
	 * 增加图像对比度并输出到文件（自动获取文件扩展名作为格式）
	 * <p>
	 * 自动从文件名提取格式扩展名，支持常见图片格式，
	 * 根据格式自动处理透明通道。
	 * </p>
	 *
	 * @param inputImage 原始图像对象，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>已加载的有效图像</li>
	 *                  </ul>
	 * @param amount 对比度调整值，必须满足：
	 *              <ul>
	 *                <li>范围在[-1.0, 1.0]之间</li>
	 *                <li>0表示不调整</li>
	 *              </ul>
	 * @param outputFile 输出文件对象，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>具有写入权限的有效路径</li>
	 *                    <li>包含有效的图片格式扩展名</li>
	 *                  </ul>
	 * @throws IOException 当发生以下情况时抛出：
	 *                    <ul>
	 *                      <li>文件写入失败</li>
	 *                      <li>路径不可写</li>
	 *                      <li>图像编码失败</li>
	 *                    </ul>
	 * @throws IllegalArgumentException 当参数验证失败时抛出
	 * @since 1.0.0
	 */
	public static void contrast(final BufferedImage inputImage, final float amount, final File outputFile) throws IOException {
		if (amount != 0f) {
			Validate.inclusiveBetween(-1.0, 1.0, amount);
			BrightnessContrastFilter filter = new BrightnessContrastFilter(0f, amount);
			filter(inputImage, filter, outputFile);
		}
	}

	/**
	 * 增加图像对比度并输出到文件
	 * <p>
	 * 支持指定输出格式，根据格式自动选择最佳图像类型，
	 * 确保输出文件质量最优。
	 * </p>
	 *
	 * @param inputImage 原始图像对象，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>已加载的有效图像</li>
	 *                  </ul>
	 * @param amount 对比度调整值，必须满足：
	 *              <ul>
	 *                <li>范围在[-1.0, 1.0]之间</li>
	 *                <li>0表示不调整</li>
	 *              </ul>
	 * @param outputFile 输出文件对象，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>具有写入权限的有效路径</li>
	 *                  </ul>
	 * @param outputFormat 输出图像格式，必须满足：
	 *                    <ul>
	 *                      <li>非空字符串</li>
	 *                      <li>ImageIO支持的格式(如"png","jpg")</li>
	 *                    </ul>
	 * @throws IOException 当发生以下情况时抛出：
	 *                    <ul>
	 *                      <li>文件写入失败</li>
	 *                      <li>路径不可写</li>
	 *                      <li>图像编码失败</li>
	 *                    </ul>
	 * @throws IllegalArgumentException 当参数验证失败时抛出
	 * @since 1.0.0
	 */
	public static void contrast(final BufferedImage inputImage, final float amount, final File outputFile,
								final String outputFormat) throws IOException {
		if (amount != 0f) {
			Validate.inclusiveBetween(-1.0, 1.0, amount);
			BrightnessContrastFilter filter = new BrightnessContrastFilter(0f, amount);
			filter(inputImage, filter, outputFile, outputFormat);
		}
	}

	/**
	 * 增加图像对比度
	 * <p>
	 * 使用标准对比度调整算法处理图像，
	 * 返回新的BufferedImage对象，原始图像不受影响。
	 * </p>
	 *
	 * @param image 原始图像对象，必须满足：
	 *             <ul>
	 *               <li>非null</li>
	 *               <li>已加载的有效图像</li>
	 *             </ul>
	 * @param amount 对比度调整值，必须满足：
	 *              <ul>
	 *                <li>范围在[-1.0, 1.0]之间</li>
	 *                <li>0表示不调整</li>
	 *              </ul>
	 * @return 处理后的新BufferedImage对象
	 * @throws IllegalArgumentException 当参数验证失败时抛出
	 * @since 1.0.0
	 */
	public static BufferedImage contrast(final BufferedImage image, final float amount) {
		if (amount == 0f) {
			return image;
		}
		Validate.inclusiveBetween(-1.0, 1.0, amount);
		RGBImageFilter filter = new BrightnessContrastFilter(0f, amount);
		return filter(image, filter);
	}

	/**
	 * 增加图像对比度（指定输出类型）
	 * <p>
	 * 支持指定输出图像类型，适用于需要精确控制输出格式的场景，
	 * 返回新的BufferedImage对象，原始图像不受影响。
	 * </p>
	 *
	 * @param image 原始图像对象，必须满足：
	 *             <ul>
	 *               <li>非null</li>
	 *               <li>已加载的有效图像</li>
	 *             </ul>
	 * @param amount 对比度调整值，必须满足：
	 *              <ul>
	 *                <li>范围在[-1.0, 1.0]之间</li>
	 *                <li>0表示不调整</li>
	 *              </ul>
	 * @param imageType 目标图像类型，常用值：
	 *                 <ul>
	 *                   <li>BufferedImage.TYPE_INT_RGB</li>
	 *                   <li>BufferedImage.TYPE_INT_ARGB</li>
	 *                   <li>BufferedImage.TYPE_3BYTE_BGR</li>
	 *                 </ul>
	 * @return 处理后的新BufferedImage对象
	 * @throws IllegalArgumentException 当参数验证失败时抛出
	 * @since 1.0.0
	 */
	public static BufferedImage contrast(final BufferedImage image, final float amount, int imageType) {
		if (amount == 0f) {
			return image;
		}
		Validate.inclusiveBetween(-1.0, 1.0, amount);
		BrightnessContrastFilter filter = new BrightnessContrastFilter(0f, amount);
		return filter(image, filter, imageType);
	}

	/**
	 * 增加图像亮度并输出到流
	 * <p>
	 * 调整图像亮度并将结果写入输出流，支持PNG/JPG等常见格式，
	 * 亮度调整范围为[-2.0, 2.0]，0表示不调整。
	 * </p>
	 *
	 * @param inputImage 原始图像对象，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>已加载的有效图像</li>
	 *                  </ul>
	 * @param amount 亮度调整值，必须满足：
	 *              <ul>
	 *                <li>范围在[-2.0, 2.0]之间</li>
	 *                <li>0表示不调整</li>
	 *              </ul>
	 * @param outputStream 输出流对象，必须满足：
	 *                    <ul>
	 *                      <li>非null</li>
	 *                      <li>已打开的可写流</li>
	 *                    </ul>
	 * @param outputFormat 输出图像格式，必须满足：
	 *                    <ul>
	 *                      <li>非空字符串</li>
	 *                      <li>ImageIO支持的格式(如"png","jpg")</li>
	 *                    </ul>
	 * @throws IOException 当发生以下情况时抛出：
	 *                    <ul>
	 *                      <li>输出流写入失败</li>
	 *                      <li>图像编码失败</li>
	 *                    </ul>
	 * @throws IllegalArgumentException 当参数验证失败时抛出
	 * @since 1.0.0
	 */
	public static void brightness(final BufferedImage inputImage, final float amount,
								  final OutputStream outputStream, final String outputFormat) throws IOException {
		if (amount != 0f) {
			Validate.inclusiveBetween(-2.0, 2.0, amount);
			BrightnessContrastFilter filter = new BrightnessContrastFilter(amount, 0f);
			filter(inputImage, filter, outputStream, outputFormat);
		}
	}

	/**
	 * 增加图像亮度并输出到文件（自动获取文件扩展名作为格式）
	 * <p>
	 * 自动从文件名提取格式扩展名，支持常见图片格式，
	 * 根据格式自动处理透明通道。
	 * </p>
	 *
	 * @param inputImage 原始图像对象，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>已加载的有效图像</li>
	 *                  </ul>
	 * @param amount 亮度调整值，必须满足：
	 *              <ul>
	 *                <li>范围在[-2.0, 2.0]之间</li>
	 *                <li>0表示不调整</li>
	 *              </ul>
	 * @param outputFile 输出文件对象，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>具有写入权限的有效路径</li>
	 *                    <li>包含有效的图片格式扩展名</li>
	 *                  </ul>
	 * @throws IOException 当发生以下情况时抛出：
	 *                    <ul>
	 *                      <li>文件写入失败</li>
	 *                      <li>路径不可写</li>
	 *                      <li>图像编码失败</li>
	 *                    </ul>
	 * @throws IllegalArgumentException 当参数验证失败时抛出
	 * @since 1.0.0
	 */
	public static void brightness(final BufferedImage inputImage, final float amount, final File outputFile) throws IOException {
		if (amount != 0f) {
			Validate.inclusiveBetween(-2.0, 2.0, amount);
			BrightnessContrastFilter filter = new BrightnessContrastFilter(amount, 0f);
			filter(inputImage, filter, outputFile);
		}
	}

	/**
	 * 增加图像亮度并输出到文件
	 * <p>
	 * 支持指定输出格式，根据格式自动选择最佳图像类型，
	 * 确保输出文件质量最优。
	 * </p>
	 *
	 * @param inputImage 原始图像对象，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>已加载的有效图像</li>
	 *                  </ul>
	 * @param amount 亮度调整值，必须满足：
	 *              <ul>
	 *                <li>范围在[-2.0, 2.0]之间</li>
	 *                <li>0表示不调整</li>
	 *              </ul>
	 * @param outputFile 输出文件对象，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>具有写入权限的有效路径</li>
	 *                  </ul>
	 * @param outputFormat 输出图像格式，必须满足：
	 *                    <ul>
	 *                      <li>非空字符串</li>
	 *                      <li>ImageIO支持的格式(如"png","jpg")</li>
	 *                    </ul>
	 * @throws IOException 当发生以下情况时抛出：
	 *                    <ul>
	 *                      <li>文件写入失败</li>
	 *                      <li>路径不可写</li>
	 *                      <li>图像编码失败</li>
	 *                    </ul>
	 * @throws IllegalArgumentException 当参数验证失败时抛出
	 * @since 1.0.0
	 */
	public static void brightness(final BufferedImage inputImage, final float amount, final File outputFile,
								  final String outputFormat) throws IOException {
		if (amount != 0f) {
			Validate.inclusiveBetween(-2.0, 2.0, amount);
			BrightnessContrastFilter filter = new BrightnessContrastFilter(amount, 0f);
			filter(inputImage, filter, outputFile, outputFormat);
		}
	}

	/**
	 * 增加图像亮度
	 * <p>
	 * 使用标准亮度调整算法处理图像，
	 * 返回新的BufferedImage对象，原始图像不受影响。
	 * </p>
	 *
	 * @param image 原始图像对象，必须满足：
	 *             <ul>
	 *               <li>非null</li>
	 *               <li>已加载的有效图像</li>
	 *             </ul>
	 * @param amount 亮度调整值，必须满足：
	 *              <ul>
	 *                <li>范围在[-2.0, 2.0]之间</li>
	 *                <li>0表示不调整</li>
	 *              </ul>
	 * @return 处理后的新BufferedImage对象
	 * @throws IllegalArgumentException 当参数验证失败时抛出
	 * @since 1.0.0
	 */
	public static BufferedImage brightness(final BufferedImage image, final float amount) {
		if (amount == 0f) {
			return image;
		}
		Validate.inclusiveBetween(-2.0, 2.0, amount);
		BrightnessContrastFilter filter = new BrightnessContrastFilter(amount, 0f);
		return filter(image, filter);
	}

	/**
	 * 增加图像亮度（指定输出类型）
	 * <p>
	 * 支持指定输出图像类型，适用于需要精确控制输出格式的场景，
	 * 返回新的BufferedImage对象，原始图像不受影响。
	 * </p>
	 *
	 * @param image 原始图像对象，必须满足：
	 *             <ul>
	 *               <li>非null</li>
	 *               <li>已加载的有效图像</li>
	 *             </ul>
	 * @param amount 亮度调整值，必须满足：
	 *              <ul>
	 *                <li>范围在[-2.0, 2.0]之间</li>
	 *                <li>0表示不调整</li>
	 *              </ul>
	 * @param imageType 目标图像类型，常用值：
	 *                 <ul>
	 *                   <li>BufferedImage.TYPE_INT_RGB</li>
	 *                   <li>BufferedImage.TYPE_INT_ARGB</li>
	 *                   <li>BufferedImage.TYPE_3BYTE_BGR</li>
	 *                 </ul>
	 * @return 处理后的新BufferedImage对象
	 * @throws IllegalArgumentException 当参数验证失败时抛出
	 * @since 1.0.0
	 */
	public static BufferedImage brightness(final BufferedImage image, final float amount, int imageType) {
		if (amount == 0f) {
			return image;
		}
		Validate.inclusiveBetween(-2.0, 2.0, amount);
		BrightnessContrastFilter filter = new BrightnessContrastFilter(amount, 0f);
		return filter(image, filter, imageType);
	}

	/**
	 * 应用图像滤镜处理并输出到流
	 * <p>
	 * 核心滤镜处理方法，支持将处理结果直接写入输出流，
	 * 适用于网络传输或内存处理场景。
	 * </p>
	 *
	 * @param inputImage 原始图像对象，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>已加载的有效图像</li>
	 *                  </ul>
	 * @param filter 图像滤镜对象，必须满足：
	 *              <ul>
	 *                <li>非null</li>
	 *                <li>实现正确的滤镜逻辑</li>
	 *              </ul>
	 * @param outputStream 输出流对象，必须满足：
	 *                    <ul>
	 *                      <li>非null</li>
	 *                      <li>已打开的可写流</li>
	 *                    </ul>
	 * @param outputFormat 输出图像格式，必须满足：
	 *                    <ul>
	 *                      <li>非空字符串</li>
	 *                      <li>ImageIO支持的格式(如"png","jpg")</li>
	 *                    </ul>
	 * @throws IOException 当发生以下情况时抛出：
	 *                    <ul>
	 *                      <li>输出流写入失败</li>
	 *                      <li>图像编码失败</li>
	 *                    </ul>
	 * @throws IllegalArgumentException 当参数验证失败时抛出
	 * @since 1.0.0
	 */
	public static void filter(final BufferedImage inputImage, final ImageFilter filter, final OutputStream outputStream,
							  final String outputFormat) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");

		BufferedImage outputImage = filter(inputImage, filter);
		ImageIO.write(outputImage, outputFormat, outputStream);
	}

	/**
	 * 应用图像滤镜并输出到文件（自动获取文件扩展名作为格式）
	 * <p>
	 * 自动从输出文件名提取格式扩展名，支持常见图片格式，
	 * 根据格式自动处理透明通道。
	 * </p>
	 *
	 * @param inputImage 原始图像对象，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>已加载的有效图像</li>
	 *                  </ul>
	 * @param filter 图像滤镜对象，必须满足：
	 *              <ul>
	 *                <li>非null</li>
	 *                <li>实现正确的滤镜逻辑</li>
	 *              </ul>
	 * @param outputFile 输出文件对象，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>具有写入权限的有效路径</li>
	 *                    <li>包含有效的图片格式扩展名</li>
	 *                  </ul>
	 * @throws IOException 当发生以下情况时抛出：
	 *                    <ul>
	 *                      <li>文件写入失败</li>
	 *                      <li>路径不可写</li>
	 *                      <li>图像编码失败</li>
	 *                    </ul>
	 * @throws IllegalArgumentException 当参数验证失败时抛出
	 * @since 1.0.0
	 */
	public static void filter(final BufferedImage inputImage, final ImageFilter filter, final File outputFile) throws IOException {
		FileUtils.checkFile(outputFile, "outputFile 不可为 null");

		BufferedImage outputImage = filter(inputImage, filter);
		ImageIO.write(outputImage, FilenameUtils.getExtension(outputFile.getName()), outputFile);
	}

	/**
	 * 应用图像滤镜并输出到文件
	 * <p>
	 * 支持自动处理透明通道，根据输出格式自动选择最佳图像类型，
	 * 确保输出文件质量最优。
	 * </p>
	 *
	 * @param inputImage 原始图像对象，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>已加载的有效图像</li>
	 *                  </ul>
	 * @param filter 图像滤镜对象，必须满足：
	 *              <ul>
	 *                <li>非null</li>
	 *                <li>实现正确的滤镜逻辑</li>
	 *              </ul>
	 * @param outputFile 输出文件对象，必须满足：
	 *                  <ul>
	 *                    <li>非null</li>
	 *                    <li>具有写入权限的有效路径</li>
	 *                  </ul>
	 * @param outputFormat 输出图像格式，必须满足：
	 *                    <ul>
	 *                      <li>非空字符串</li>
	 *                      <li>ImageIO支持的格式(如"png","jpg")</li>
	 *                    </ul>
	 * @throws IOException 当发生以下情况时抛出：
	 *                    <ul>
	 *                      <li>文件写入失败</li>
	 *                      <li>路径不可写</li>
	 *                      <li>图像编码失败</li>
	 *                    </ul>
	 * @throws IllegalArgumentException 当参数验证失败时抛出
	 * @since 1.0.0
	 */
	public static void filter(final BufferedImage inputImage, final ImageFilter filter, final File outputFile,
							  final String outputFormat) throws IOException {
		Validate.notBlank(outputFormat, "outputFormat 不可为空");
		FileUtils.checkFile(outputFile, "outputFile 不可为 null");

		BufferedImage outputImage;
		if (ImageConstants.NON_TRANSPARENT_IMAGE_FORMATS.contains(outputFormat.toLowerCase())) {
			outputImage = filter(inputImage, filter, BufferedImage.TYPE_INT_RGB);
		} else {
			outputImage = filter(inputImage, filter);
		}
		ImageIO.write(outputImage, outputFormat.toLowerCase(), outputFile);
	}

	/**
	 * 应用图像滤镜处理
	 * <p>
	 * 核心滤镜处理方法，生成新的BufferedImage对象，
	 * 保持原始图像不变。
	 * </p>
	 *
	 * @param image 原始图像对象，必须满足：
	 *             <ul>
	 *               <li>非null</li>
	 *               <li>已加载的有效图像</li>
	 *             </ul>
	 * @param filter 图像滤镜对象，必须满足：
	 *              <ul>
	 *                <li>非null</li>
	 *                <li>实现正确的滤镜逻辑</li>
	 *              </ul>
	 * @return 处理后的新BufferedImage对象
	 * @throws IllegalArgumentException 当参数验证失败时抛出
	 * @since 1.0.0
	 */
	public static BufferedImage filter(final BufferedImage image, final ImageFilter filter) {
		Validate.notNull(image, "image 不可为 null");
		Validate.notNull(filter, "filter 不可为 null");

		Image filterImage = ImageUtil.filter(image, filter);
		return ImageUtil.toBuffered(filterImage);
	}

	/**
	 * 应用图像滤镜处理（指定输出类型）
	 * <p>
	 * 支持指定输出图像类型，适用于需要精确控制输出格式的场景，
	 * 如需要强制RGB或ARGB格式的情况。
	 * </p>
	 *
	 * @param image 原始图像对象，必须满足：
	 *             <ul>
	 *               <li>非null</li>
	 *               <li>已加载的有效图像</li>
	 *             </ul>
	 * @param filter 图像滤镜对象，必须满足：
	 *              <ul>
	 *                <li>非null</li>
	 *                <li>实现正确的滤镜逻辑</li>
	 *              </ul>
	 * @param imageType 目标图像类型，常用值：
	 *                 <ul>
	 *                   <li>BufferedImage.TYPE_INT_RGB</li>
	 *                   <li>BufferedImage.TYPE_INT_ARGB</li>
	 *                   <li>BufferedImage.TYPE_3BYTE_BGR</li>
	 *                 </ul>
	 * @return 处理后的新BufferedImage对象
	 * @throws IllegalArgumentException 当参数验证失败时抛出
	 * @since 1.0.0
	 */
	public static BufferedImage filter(final BufferedImage image, final ImageFilter filter, int imageType) {
		Validate.notNull(image, "image 不可为 null");
		Validate.notNull(filter, "filter 不可为 null");

		Image filterImage = ImageUtil.filter(image, filter);
		return ImageUtil.toBuffered(filterImage, imageType);
	}
}