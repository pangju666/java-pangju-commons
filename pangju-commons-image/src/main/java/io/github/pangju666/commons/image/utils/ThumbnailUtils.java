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
import io.github.pangju666.commons.image.model.ImageSize;
import org.apache.commons.lang3.Validate;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @see com.twelvemonkeys.image.ResampleOp
 */
public class ThumbnailUtils {
	/*public static boolean asThumbnail(final File file) throws IOException {
		if (Objects.isNull(file)) {
			return false;
		}
		checkFile(file);

		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(file)) {
			return Objects.nonNull(getMimeType(imageInputStream));
		}
	}

	public static boolean asThumbnail(final Path path) throws IOException {
		if (Objects.isNull(path)) {
			return false;
		}
		checkPath(path);

		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(path.toFile())) {
			return Objects.nonNull(getMimeType(imageInputStream));
		}
	}

	public static boolean asThumbnail(final byte[] bytes) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return false;
		}

		InputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
			return Objects.nonNull(getMimeType(imageInputStream));
		}
	}

	public static boolean asThumbnail(final InputStream inputStream) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
			return Objects.nonNull(getMimeType(imageInputStream));
		}
	}*/

	public static BufferedImage asThumbnail(final BufferedImage image, final ImageSize imageSize, final int filterType) throws IOException {
		Validate.notNull(image, "image 不可为 null");
		ResampleOp resampleOp = new ResampleOp(imageSize.width(), imageSize.height(), filterType);
		return resampleOp.filter(image, null);
	}
}
