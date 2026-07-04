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

package io.github.pangju666.commons.opencv.model;

import com.github.jaiimageio.impl.common.ImageUtil;
import io.github.pangju666.commons.opencv.enums.WatermarkDirection;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.function.Function;

public class ImageWatermarkOption {
	private double relativeScaleFactor = 0.15;

	private float opacity = 0.4f;

	private int inset = 10;

	private int x = 0;

	private int y = 0;

	private WatermarkDirection direction;

	/*private Function<ImageSize, Pair<ImageSize, ImageSize>> sizeLimitStrategy = imageSize -> {
		int shorter = Math.min(imageSize.getWidth(), imageSize.getHeight());
		if (shorter < 600) { // 小图
			return Pair.of(new ImageSize(120, 120), new ImageSize(150, 150));
		} else if (shorter >= 1920) { // 大图（注意：>=1920）
			return Pair.of(new ImageSize(250, 250), new ImageSize(400, 400));
		} else { // 中等图
			return Pair.of(new ImageSize(150, 150), new ImageSize(250, 250));
		}
	};*/

	public double getRelativeScaleFactor() {
		return relativeScaleFactor;
	}

	public void setRelativeScaleFactor(double relativeScaleFactor) {
		if (relativeScaleFactor > 0) {
			this.relativeScaleFactor = relativeScaleFactor;
		}
	}

	public float getOpacity() {
		return opacity;
	}

	public void setOpacity(float opacity) {
		if (opacity >= 0f && opacity <= 1) {
			this.opacity = opacity;
		}
	}

	public int getInset() {
		return inset;
	}

	public void setInset(int inset) {
		if (inset >= 0) {
			this.inset = inset;
		}
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		if (x >= 0) {
			this.x = x;
		}
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		if (y >= 0) {
			this.y = y;
		}
	}

	public WatermarkDirection getDirection() {
		return direction;
	}

	public void setDirection(WatermarkDirection direction) {
		this.direction = direction;
	}
}
