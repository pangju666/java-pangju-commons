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

package io.github.pangju666.commons.opencv.lang;

import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Scalar;

import java.util.Set;

/**
 * OpenCV 相关常量类
 *
 * <p>提供 OpenCV 图像处理常用的常量配置，包括透明颜色、默认图像读取模式、支持的图像格式和 MIME 类型等。</p>
 *
 * @author pangju666
 * @since 1.1.0
 */
public class OpencvConstants {
	/**
	 * 私有构造函数，防止实例化
	 *
	 * @since 1.1.0
	 */
	protected OpencvConstants() {
	}

	/**
	 * 完全透明颜色常量
	 *
	 * <p>RGBA 格式的完全透明色：(0, 0, 0, 0)</p>
	 *
	 * @since 1.1.0
	 */
	public static final Scalar TRANSPARENT_COLOR = new Scalar(0, 0, 0, 0);

	/**
	 * 默认图像颜色读取模式
	 *
	 * <p>默认使用 BGR 彩色模式读取图像（opencv_imgcodecs.IMREAD_COLOR_BGR）</p>
	 *
	 * @since 1.1.0
	 */
	public static int DEFAULT_IMAGE_COLOR_TYPE = opencv_imgcodecs.IMREAD_COLOR_BGR;

	/**
	 * 支持的图像文件格式集合（文件扩展名）
	 *
	 * <p>包含所有 OpenCV 支持读写的图像文件扩展名，不区分大小写。</p>
	 * <p>常见格式包括：bmp, jpg, png, webp, tiff, gif 等。</p>
	 *
	 * @since 1.1.0
	 */
	public static final Set<String> SUPPORTED_IMAGE_FILE_FORMATS = Set.of("bmp", "dib", "gif", "jpeg", "jpg",
		"jpe", "jp2", "png", "webp", "avif", "pbm", "pgm", "ppm", "pxm", "pnm", "pfm", "sr", "ras", "tiff", "tif",
		"exr", "hdr", "pic");

	/**
	 * 支持的图像 MIME 类型集合
	 *
	 * <p>包含所有 OpenCV 支持读写的图像 MIME 类型，用于内容类型检查。</p>
	 *
	 * @since 1.1.0
	 */
	public static final Set<String> SUPPORTED_IMAGE_TYPES = Set.of("image/bmp", "image/gif", "image/png",
		"image/webp", "image/avif", "image/tiff", "image/jpeg", "image/jp2", "image/vnd.radiance", "image/x-pict",
		"image/x-cmu-raster", "image/x-sun-raster", "image/x-exr", "image/x-portable-bitmap", "image/x-portable-graymap",
		"image/x-portable-pixmap", "image/x-portable-anymap", "image/x-portable-floatmap");
}
