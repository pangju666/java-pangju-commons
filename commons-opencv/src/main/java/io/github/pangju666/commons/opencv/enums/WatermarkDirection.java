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

package io.github.pangju666.commons.opencv.enums;

import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Size;

public enum WatermarkDirection {
	TOP_LEFT,
	TOP,
	TOP_RIGHT,
	RIGHT,
	CENTER,
	LEFT,
	BOTTOM,
	BOTTOM_RIGHT,
	BOTTOM_LEFT;

	public Rect toImageWatermarkRect(Size imageSize, Size watermarkImageSize, int margin) {
		return switch (this) {
			case TOP -> new Rect((imageSize.width() - watermarkImageSize.width()) / 2,
				watermarkImageSize.height() + margin, watermarkImageSize.width(), watermarkImageSize.height());
			case TOP_LEFT -> new Rect(0, watermarkImageSize.height(), watermarkImageSize.width(), watermarkImageSize.height());
			case TOP_RIGHT -> new Rect(imageSize.width() - watermarkImageSize.width(),
				watermarkImageSize.height(), watermarkImageSize.width(), watermarkImageSize.height());
			case BOTTOM -> new Rect((imageSize.width() - watermarkImageSize.width()) / 2,
				imageSize.height(), watermarkImageSize.width(), watermarkImageSize.height());
			case BOTTOM_LEFT -> new Rect(0, imageSize.height(), watermarkImageSize.width(), watermarkImageSize.height());
			case BOTTOM_RIGHT -> new Rect(imageSize.width() - watermarkImageSize.width(),
				imageSize.height(), watermarkImageSize.width(), watermarkImageSize.height());
			case LEFT -> new Rect(0, (imageSize.height() - watermarkImageSize.height()) / 2, watermarkImageSize.width(), watermarkImageSize.height());
			case RIGHT -> new Rect(imageSize.width() - watermarkImageSize.width(),
				(imageSize.height() - watermarkImageSize.height()) / 2, watermarkImageSize.width(), watermarkImageSize.height());
			default -> new Rect((imageSize.width() - watermarkImageSize.width()) / 2,
				(imageSize.height() - watermarkImageSize.height()) / 2, watermarkImageSize.width(), watermarkImageSize.height());
		};
	}
}