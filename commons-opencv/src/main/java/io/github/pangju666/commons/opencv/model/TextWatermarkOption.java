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

import io.github.pangju666.commons.opencv.enums.WatermarkDirection;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Size;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

public class TextWatermarkOption {
	public static final double STD_FONT_DIV = 14.0;
	public static final double SCRIPT_FONT_DIV = 15.0;
	public static final double SMALL_FONT_DIV = 12.0;

	private float opacity = 0.4f;

	private int margin = 20;

	private int x = 0;

	private int y = 0;

	private WatermarkDirection direction;

	private Color strokeColor = Color.BLACK;

	private Color fillColor = Color.WHITE;

	private int strokeSize = 2;

	private boolean stroke = true;

	private int thickness = 3;

	private int fontFace = opencv_imgproc.FONT_HERSHEY_SIMPLEX;

	private ToDoubleBiFunction<Size, Integer> fontScaleStrategy = (size, fontFace) -> {
		int shorter = Math.min(size.width(), size.height());

		int fontPt;
		if (shorter < 600) {
			fontPt = 32;
		} else if (shorter >= 1920) {
			double ratio = Math.min(1.0, (shorter - 1920.0) / (6000 - 1920.0));
			fontPt = (int) Math.round(48 + ratio * (160 - 48));
		} else {
			double ratio = (shorter - 600.0) / (1920.0 - 600.0);
			fontPt = (int) Math.round(32 + ratio * (48 - 32));
		}

		int baseFontFace = fontFace & ~opencv_imgproc.FONT_ITALIC;
		if (baseFontFace == opencv_imgproc.FONT_HERSHEY_SCRIPT_SIMPLEX || baseFontFace == opencv_imgproc.FONT_HERSHEY_SCRIPT_COMPLEX) {
			return (double) fontPt / SCRIPT_FONT_DIV;
		} else if (baseFontFace == opencv_imgproc.FONT_HERSHEY_PLAIN || baseFontFace == opencv_imgproc.FONT_HERSHEY_COMPLEX_SMALL) {
			return (double) fontPt / SMALL_FONT_DIV;
		} else {
			return (double) fontPt / STD_FONT_DIV;
		}
	};

	public float getOpacity() {
		return opacity;
	}

	public void setOpacity(float opacity) {
		if (opacity >= 0f && opacity <= 1) {
			this.opacity = opacity;
		}
	}

	public int getMargin() {
		return margin;
	}

	public void setMargin(int margin) {
		if (margin >= 0) {
			this.margin = margin;
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

	public boolean isStroke() {
		return stroke;
	}

	public void setStroke(boolean stroke) {
		this.stroke = stroke;
	}

	public int getStrokeSize() {
		return strokeSize;
	}

	public void setStrokeSize(int strokeSize) {
		if (strokeSize > 0) {
			this.strokeSize = strokeSize;
		}
	}

	public Color getStrokeColor() {
		return strokeColor;
	}

	public void setStrokeColor(Color strokeColor) {
		if (Objects.nonNull(strokeColor)) {
			this.strokeColor = strokeColor;
		}
	}

	public Color getFillColor() {
		return fillColor;
	}

	public void setFillColor(Color fillColor) {
		if (Objects.nonNull(fillColor)) {
			this.fillColor = fillColor;
		}
	}

	public int getThickness() {
		return thickness;
	}

	public void setThickness(int thickness) {
		if (thickness > 0) {
			this.thickness = thickness;
		}
 	}

	public ToDoubleBiFunction<Size, Integer> getFontScaleStrategy() {
		return fontScaleStrategy;
	}

	public void setFontScaleStrategy(ToDoubleBiFunction<Size, Integer> fontScaleStrategy) {
		this.fontScaleStrategy = fontScaleStrategy;
	}

	public int getFontFace() {
		return fontFace;
	}

	public void setFontFace(int fontFace) {
		this.fontFace = fontFace;
	}
}