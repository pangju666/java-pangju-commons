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

import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.Objects;

/**
 * 文字水印样式配置。
 *
 * <p>用于控制文字水印的透明度、字体名称、字体大小、字体样式、描边颜色与宽度、填充颜色，以及是否启用描边效果。</p>
 *
 * @since 1.0.0
 */
public class TextWatermarkOption {
	/**
	 * 透明度（百分比）。范围：[0.0, 1.0]；默认 0.4。
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
	 * 字体名称，默认 {@code Font.SANS_SERIF}。
	 * 仅当值非空且非纯空白时有效；否则保持当前值。
	 * 常见取值示例：{@code Font.SANS_SERIF}、{@code Font.SERIF}、{@code Font.MONOSPACED}。
	 *
	 * @since 1.0.0
	 */
	private String fontName = Font.SANS_SERIF;
	/**
	 * 字体样式，默认 {@code Font.PLAIN}。
	 * 有效取值：
	 * {@code Font.PLAIN}(0)、{@code Font.BOLD}(1)、{@code Font.ITALIC}(2)、{@code Font.BOLD | Font.ITALIC}(3)。
	 * 越界值将被忽略并保持当前值。
	 *
	 * @since 1.0.0
	 */
	private int fontStyle = Font.PLAIN;
	/**
	 * 字体大小比例，默认 {@code 0.04}。
	 * 必须为非负数（{@code >= 0}）；负数将被忽略并保持当前值。
	 * 实际字号由渲染器结合图像尺寸与该比例计算；比例越小文字越小。
	 *
	 * @since 1.0.0
	 */
	private double fontSizeRatio = 0.04;
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
	 * 描边线宽（像素），默认 3.0。仅当 {@code stroke=true} 时生效。
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

	public String getFontName() {
		return fontName;
	}

	/**
	 * 设置字体名称。
	 * 仅当参数非空且非纯空白时生效；否则忽略并保持当前值。
	 * 常见取值示例：{@code Font.SANS_SERIF}、{@code Font.SERIF}、{@code Font.MONOSPACED}。
	 *
	 * @param fontName 字体名称
	 * @since 1.0.0
	 */
	public void setFontName(String fontName) {
		if (StringUtils.isNotBlank(fontName)) {
			this.fontName = fontName;
		}
	}

	public int getFontStyle() {
		return fontStyle;
	}

	/**
	 * 设置字体样式。
	 * 有效取值：
	 * <ul>
	 *   <li>{@code Font.PLAIN}（0）</li>
	 *   <li>{@code Font.BOLD}（1）</li>
	 *   <li>{@code Font.ITALIC}（2）</li>
	 *   <li>{@code Font.BOLD | Font.ITALIC}（3）</li>
	 * </ul>
	 * 非法值将被忽略并保持当前值。
	 *
	 * @param fontStyle 字体样式
	 * @since 1.0.0
	 */
	public void setFontStyle(int fontStyle) {
		if (fontStyle >= 0 && fontStyle <= 3) {
			this.fontStyle = fontStyle;
		}
	}

	public double getFontSizeRatio() {
		return fontSizeRatio;
	}

	/**
	 * 设置字体大小比例。
	 * 必须为非负数（{@code > 0}）；负数将被忽略并保持当前值。
	 * 实际字号由渲染器结合图像尺寸与该比例计算；比例越小文字越小。
	 *
	 * @param fontSizeRatio 字体大小比例（非负）
	 * @since 1.0.0
	 */
	public void setFontSizeRatio(double fontSizeRatio) {
		if (fontSizeRatio > 0) {
			this.fontSizeRatio = fontSizeRatio;
		}
	}

	public boolean isStroke() {
		return stroke;
	}

	/**
	 * 设置是否启用文字描边。
	 * 当为 {@code false} 时，渲染将不进行描边；此时 {@code strokeColor} 与 {@code strokeWidth} 配置被忽略。
	 *
	 * @param stroke 是否启用描边
	 * @since 1.0.0
	 */
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
	 * 设置透明度（百分比）。
	 * 有效范围为 {@code [0.0, 1.0]}；越界值将被忽略并保持当前值。
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

	public Color getStrokeColor() {
		return strokeColor;
	}

	/**
	 * 设置描边颜色。
	 * 仅当 {@code stroke=true} 时在渲染中使用；参数为 {@code null} 时忽略。
	 * 若颜色 Alpha 为 255（不透明），渲染时结合 {@code opacity} 调整透明度；若颜色已包含透明度（Alpha≠255），优先使用颜色自身透明度。
	 *
	 * @param strokeColor 描边颜色
	 * @since 1.0.0
	 */
	public void setStrokeColor(Color strokeColor) {
		if (Objects.nonNull(strokeColor)) {
			this.strokeColor = strokeColor;
		}
	}

	public Color getFillColor() {
		return fillColor;
	}

	/**
	 * 设置填充颜色。
	 * 参数为 {@code null} 时忽略。
	 * 若颜色 Alpha 为 255（不透明），渲染时结合 {@code opacity} 调整透明度；若颜色已包含透明度（Alpha≠255），优先使用颜色自身透明度。
	 *
	 * @param fillColor 填充颜色
	 * @since 1.0.0
	 */
	public void setFillColor(Color fillColor) {
		if (Objects.nonNull(fillColor)) {
			this.fillColor = fillColor;
		}
	}
}