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

import javax.imageio.ImageIO;
import java.util.Objects;
import java.util.Set;

public class OpencvConstants {

	/**
	 * 私有构造函数，防止实例化
	 *
	 * @since 1.1.0
	 */
	protected OpencvConstants() {
	}

	public static int DEFAULT_IMAGE_COLOR_TYPE = opencv_imgcodecs.IMREAD_COLOR_BGR;

	public static final Set<String> SUPPORTED_IMAGE_FILE_FORMATS = Set.of("bmp", "dib", "gif", "jpeg", "jpg",
		"jpe", "jp2", "png", "webp", "avif", "pbm", "pgm", "ppm", "pxm", "pnm", "pfm", "sr", "ras", "tiff", "tif",
		"exr", "hdr", "pic");

	public static final Set<String> SUPPORTED_IMAGE_TYPES = Set.of("image/bmp", "image/gif", "image/png",
		"image/webp", "image/avif", "image/tiff", "image/jpeg", "image/jp2", "image/vnd.radiance", "image/x-pict",
		"image/x-cmu-raster", "image/x-sun-raster", "image/x-exr", "image/x-portable-bitmap", "image/x-portable-graymap",
		"image/x-portable-pixmap", "image/x-portable-anymap", "image/x-portable-floatmap");
}
