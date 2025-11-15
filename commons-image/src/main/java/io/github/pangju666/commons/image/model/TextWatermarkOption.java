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
	 * <p>
	 * 注意：仅当颜色属性（如 {@code fillColor}、{@code strokeColor}）<b>不含透明通道</b>或为<b>完全不透明</b>（Alpha=255）时，
	 * {@code opacity} 才会生效；若提供的颜色已包含透明度（Alpha≠255），则以颜色自身的透明度为准。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private float opacity = 0.4f;
	/**
	 * 字体（族、样式、字号）。默认 {@code SansSerif} 12pt 常规。
	 *
	 * @since 1.0.0
	 */
	private Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
	/**
	 * 描边颜色，默认浅灰。仅当 {@code stroke=true} 时使用。
	 * <p>
	 * 若该颜色<b>不含透明通道</b>或为<b>完全不透明</b>（Alpha=255），并设置了 {@code opacity}（0&lt;opacity&lt;1），
	 * 将按不透明度调整透明通道；否则保留颜色自身的透明度。
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private Color strokeColor = Color.LIGHT_GRAY;
	/**
	 * 填充颜色，默认白色。
	 * <p>
	 * 若该颜色<b>不含透明通道</b>或为<b>完全不透明</b>（Alpha=255），并设置了 {@code opacity}（0&lt;opacity&lt;1），
	 * 将按不透明度调整透明通道；否则保留颜色自身的透明度。
	 * </p>
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

	/**
	 * 设置描边线宽（像素）。
	 * 仅当 {@code stroke=true} 时生效；必须为正数，非正数将被忽略并保持原值。
	 *
	 * @param strokeWidth 描边线宽（像素）
	 * @since 1.0.0
	 */
	public void setStrokeWidth(float strokeWidth) {
		if (strokeWidth > 0f) {
			this.strokeWidth = strokeWidth;
		}
	}

	public float getOpacity() {
		return opacity;
	}

	/**
	 * 设置透明度（Alpha）。
	 * 有效范围为 {@code [0.0f, 1.0f]}；越界值将被忽略并保持当前值。
	 * 其中 0 表示完全透明，1 表示完全不透明。
	 *
	 * <p>
	 * 注意：仅当颜色属性（如 {@code fillColor}、{@code strokeColor}）<b>不含透明通道</b>或为<b>完全不透明</b>（Alpha=255）时，
	 * {@code opacity} 才会在渲染时被应用；若提供的颜色已包含透明度（Alpha≠255），则优先使用颜色自身透明度。
	 * </p>
	 *
	 * @param opacity 透明度
	 * @since 1.0.0
	 */
	public void setOpacity(float opacity) {
		if (opacity >= 0f && opacity <= 1) {
			this.opacity = opacity;
		}
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
