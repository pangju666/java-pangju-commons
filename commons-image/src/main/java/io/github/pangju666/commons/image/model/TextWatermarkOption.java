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

package io.github.pangju666.commons.image.model;

import java.awt.*;

/**
 * 文字水印样式配置。
 *
 * <p>用于控制文字水印的透明度、字体、描边颜色与宽度、填充颜色，以及是否启用描边效果。</p>
 *
 * @since 1.0.0
 */
public class TextWatermarkOption {
	/**
	 * 透明度（Alpha）。范围：[0.0f, 1.0f]；默认 0.4f。
	 * 控制文本整体透明度，0 表示完全透明，1 表示完全不透明。
	 *
	 * @since 1.0.0
	 */
	private float opacity = 0.4f;
	/**
	 * 字体（族、样式、字号）。默认 {@code Dialog} 12pt 常规。
	 *
	 * @since 1.0.0
	 */
	private Font font = new Font(Font.DIALOG, Font.PLAIN, 12);
	/**
	 * 描边颜色，默认浅灰。仅当 {@code stroke=true} 时使用。
	 *
	 * @since 1.0.0
	 */
	private Color strokeColor = Color.LIGHT_GRAY;
	/**
	 * 填充颜色，默认白色。
	 *
	 * @since 1.0.0
	 */
	private Color fillColor = Color.WHITE;
	/**
	 * 描边线宽（像素），默认 3.0f。仅当 {@code stroke=true} 时生效。
	 *
	 * @since 1.0.0
	 */
	private float strokeWidth = 3.0f;
	/**
	 * 是否启用文字描边。默认 {@code true}；在复杂背景下提升可读性。
	 *
	 * @since 1.0.0
	 */
	private boolean stroke = true;

	public boolean isStroke() {
		return stroke;
	}

	public void setStroke(boolean stroke) {
		this.stroke = stroke;
	}

	public float getStrokeWidth() {
		return strokeWidth;
	}

	public void setStrokeWidth(float strokeWidth) {
		this.strokeWidth = strokeWidth;
	}

	public float getOpacity() {
		return opacity;
	}

	public void setOpacity(float opacity) {
		this.opacity = opacity;
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public Color getStrokeColor() {
		return strokeColor;
	}

	public void setStrokeColor(Color strokeColor) {
		this.strokeColor = strokeColor;
	}

	public Color getFillColor() {
		return fillColor;
	}

	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}
}
