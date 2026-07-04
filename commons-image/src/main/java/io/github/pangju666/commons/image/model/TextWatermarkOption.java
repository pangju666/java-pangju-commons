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

import com.twelvemonkeys.image.BrightnessContrastFilter;
import com.twelvemonkeys.image.GrayFilter;
import net.coobird.thumbnailator.filters.Caption;
import net.coobird.thumbnailator.geometry.Coordinate;
import net.coobird.thumbnailator.geometry.Position;
import net.coobird.thumbnailator.geometry.Positions;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.function.ToIntFunction;

/**
 * 文字水印样式配置。
 *
 * <p>用于控制文字水印的透明度、字体名称、字体大小策略、字体样式、填充颜色、边距、位置（方向或自定义坐标）。</p>
 *
 * <p>水印位置支持两种方式：</p>
 * <ul>
 *   <li>通过 {@link Positions} 设置九宫格方向位置</li>
 *   <li>通过自定义坐标 x/y 精确设置位置</li>
 * </ul>
 *
 * <p>字体大小计算说明：</p>
 * <ul>
 *   <li>使用 {@code fontSizeStrategy} 函数计算基础字体大小（单位：pt）</li>
 *   <li>最终字体大小 = 基础大小 × {@link #FONT_SCALE}（2.45）</li>
 *   <li>缩放系数是为了匹配 Thumbnailator 的渲染需求</li>
 * </ul>
 *
 * @author pangju666
 * @see #toCaption(String, BufferedImage)
 * @see Positions
 * @since 1.0.0
 */
public class TextWatermarkOption {
	/**
	 * 字体大小缩放系数。
	 *
	 * <p>用于将字体大小策略计算出的基础大小转换为 Thumbnailator 所需的像素尺寸。</p>
	 *
	 * @since 1.1.0
	 */
	public static final float FONT_SCALE = 2.45f;

	/**
	 * 透明度（百分比）。范围：[0.0, 1.0]；默认 40%。
	 * 控制文本整体透明度，0 表示完全透明，1 表示完全不透明。
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
	 * 字体颜色，默认白色。
	 *
	 * @since 1.1.0
	 */
	private Color color = Color.WHITE;

	/**
	 * 边距大小，默认 20
	 *
	 * @since 1.1.0
	 */
	private int margin = 20;

	/**
	 * X 坐标位置，仅在未设置方向时生效
	 *
	 * @since 1.1.0
	 */
	private int x = 0;

	/**
	 * Y 坐标位置，仅在未设置方向时生效
	 *
	 * @since 1.1.0
	 */
	private int y = 0;

	/**
	 * 水印位置方向，null 表示使用自定义坐标
	 *
	 * @since 1.1.0
	 */
	private Positions direction;

	/**
	 * 描边颜色，默认黑色。仅当 {@code stroke=true} 时使用。
	 * <p>
	 * 若该颜色<b>不含透明通道</b>或为<b>完全不透明</b>（Alpha=255），并设置了 {@code opacity}（0&lt;opacity&lt;1），
	 * 将按不透明度调整透明通道；否则保留颜色自身的透明度。
	 * </p>
	 *
	 * @since 1.0.0
	 * @deprecated
	 */
	@Deprecated(forRemoval = true, since = "1.1.0")
	private Color strokeColor = Color.BLACK;

	/**
	 * 填充颜色，默认白色。
	 * <p>
	 * 若该颜色<b>不含透明通道</b>或为<b>完全不透明</b>（Alpha=255），并设置了 {@code opacity}（0&lt;opacity&lt;1），
	 * 将按不透明度调整透明通道；否则保留颜色自身的透明度。
	 * </p>
	 *
	 * @since 1.0.0
	 * @deprecated
	 */
	@Deprecated(forRemoval = true, since = "1.1.0")
	private Color fillColor = Color.WHITE;

	/**
	 * 描边线宽（像素），默认 2.0。仅当 {@code stroke=true} 时生效。
	 *
	 * @since 1.0.0
	 * @deprecated
	 */
	@Deprecated(forRemoval = true, since = "1.1.0")
	private float strokeWidth = 2.0f;

	/**
	 * 是否启用文字描边。默认 {@code true}；在复杂背景下提升可读性。
	 *
	 * @since 1.0.0
	 * @deprecated
	 */
	@Deprecated(forRemoval = true, since = "1.1.0")
	private boolean stroke = true;

	/**
	 * 字体大小计算策略函数。
	 * <p>
	 * 根据目标图像的尺寸（{@link ImageSize}），计算出合适的水印字体大小（单位：pt）。
	 * 默认策略基于图像短边长度进行动态计算：
	 * <ul>
	 *   <li>小图（短边 &lt; 600px）：固定为 32pt</li>
	 *   <li>中等图（600px &le; 短边 &lt; 1920px）：在 32pt 至 48pt 之间线性增长</li>
	 *   <li>大图（短边 &ge; 1920px）：在 48pt 至 160pt 之间（随尺寸从 1920px 增长到 6000px 逐渐变缓）</li>
	 * </ul>
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private ToIntFunction<ImageSize> fontSizeStrategy = imageSize -> {
		int shorter = Math.min(imageSize.getWidth(), imageSize.getHeight());
		if (shorter < 600) {
			return 32;
		} else if (shorter >= 1920) {
			double ratio = Math.min(1.0, (shorter - 1920.0) / (6000 - 1920.0));
			return (int) Math.round(48 + ratio * (160 - 48));
		} else {
			double ratio = (shorter - 600.0) / (1920.0 - 600.0);
			return (int) Math.round(32 + ratio * (48 - 32));
		}
	};

	/**
	 * 获取字体名称。
	 *
	 * @return 字体名称（如 "SansSerif"）
	 * @since 1.0.0
	 */
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

	/**
	 * 获取字体样式。
	 *
	 * @return 字体样式整数值（如 {@link Font#BOLD}）
	 * @since 1.0.0
	 */
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

	/**
	 * 获取当前字体大小计算策略。
	 *
	 * @return 计算字体大小的策略函数
	 * @since 1.0.0
	 */
	public ToIntFunction<ImageSize> getFontSizeStrategy() {
		return fontSizeStrategy;
	}

	/**
	 * 设置字体大小计算策略。
	 * <p>
	 * 允许自定义策略，根据目标图像尺寸动态计算字体大小。
	 * </p>
	 *
	 * @param fontSizeStrategy 字体大小计算策略，不能为 null；如果为 null 则忽略并保持原策略
	 * @since 1.0.0
	 */
	public void setFontSizeStrategy(ToIntFunction<ImageSize> fontSizeStrategy) {
		if (Objects.nonNull(fontSizeStrategy)) {
			this.fontSizeStrategy = fontSizeStrategy;
		}
	}

	/**
	 * 获取水印透明度。
	 *
	 * @return 透明度值（0.0 - 1.0）
	 * @since 1.0.0
	 */
	public float getOpacity() {
		return opacity;
	}

	/**
	 * 设置透明度（百分比）。
	 * 有效范围为 {@code [0.0, 1.0]}；越界值将被忽略并保持当前值。
	 * 其中 0 表示完全透明，1 表示完全不透明。
	 *
	 * @param opacity 透明度
	 * @since 1.0.0
	 */
	public void setOpacity(float opacity) {
		if (opacity >= 0f && opacity <= 1) {
			this.opacity = opacity;
		}
	}

	/**
	 * 获取填充颜色。
	 *
	 * @return 填充颜色对象
	 * @since 1.1.0
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * 设置字体颜色。
	 * 参数为 {@code null} 时忽略。
	 *
	 * @param color 字体颜色
	 * @since 1.1.0
	 */
	public void setColor(Color color) {
		if (Objects.nonNull(color)) {
			this.color = color;
		}
	}

	/**
	 * 获取边距大小
	 *
	 * @return 边距值
	 * @since 1.1.0
	 */
	public int getMargin() {
		return margin;
	}

	/**
	 * 设置边距大小
	 *
	 * @param margin 边距值，必须大于等于 0
	 * @since 1.1.0
	 */
	public void setMargin(int margin) {
		if (margin >= 0) {
			this.margin = margin;
		}
	}

	/**
	 * 获取 X 坐标位置
	 *
	 * @return X 坐标值
	 * @since 1.1.0
	 */
	public int getX() {
		return x;
	}

	/**
	 * 设置 X 坐标位置，仅在未设置方向时生效
	 *
	 * @param x X 坐标值，必须大于等于 0
	 * @since 1.1.0
	 */
	public void setX(int x) {
		if (x >= 0) {
			this.x = x;
		}
	}

	/**
	 * 获取 Y 坐标位置
	 *
	 * @return Y 坐标值
	 * @since 1.1.0
	 */
	public int getY() {
		return y;
	}

	/**
	 * 设置 Y 坐标位置，仅在未设置方向时生效
	 *
	 * @param y Y 坐标值，必须大于等于 0
	 * @since 1.1.0
	 */
	public void setY(int y) {
		if (y >= 0) {
			this.y = y;
		}
	}

	/**
	 * 获取水印位置方向
	 *
	 * @return 水印方向，null 表示使用自定义坐标
	 * @since 1.1.0
	 */
	public Positions getDirection() {
		return direction;
	}

	/**
	 * 设置水印位置方向，设置为 null 表示使用自定义坐标
	 *
	 * @param direction 水印方向
	 * @since 1.1.0
	 */
	public void setDirection(Positions direction) {
		this.direction = direction;
	}

	/**
	 * 获取是否启用描边。
	 *
	 * @return true 表示启用描边，false 表示禁用
	 * @since 1.0.0
	 * @deprecated
	 */
	@Deprecated(forRemoval = true, since = "1.1.0")
	public boolean isStroke() {
		return stroke;
	}

	/**
	 * 设置是否启用文字描边。
	 * 当为 {@code false} 时，渲染将不进行描边；此时 {@code strokeColor} 与 {@code strokeWidth} 配置被忽略。
	 *
	 * @param stroke 是否启用描边
	 * @since 1.0.0
	 * @deprecated
	 */
	@Deprecated(forRemoval = true, since = "1.1.0")
	public void setStroke(boolean stroke) {
		this.stroke = stroke;
	}

	/**
	 * 获取描边线宽。
	 *
	 * @return 线宽（像素）
	 * @since 1.0.0
	 * @deprecated
	 */
	@Deprecated(forRemoval = true, since = "1.1.0")
	public float getStrokeWidth() {
		return strokeWidth;
	}

	/**
	 * 设置描边线宽（像素）。
	 * 仅当 {@code stroke=true} 时生效；必须为正数，非正数将被忽略并保持原值。
	 *
	 * @param strokeWidth 描边线宽（像素）
	 * @since 1.0.0
	 * @deprecated
	 */
	@Deprecated(forRemoval = true, since = "1.1.0")
	public void setStrokeWidth(float strokeWidth) {
		if (strokeWidth > 0f) {
			this.strokeWidth = strokeWidth;
		}
	}

	/**
	 * 获取描边颜色。
	 *
	 * @return 描边颜色对象
	 * @since 1.0.0
	 * @deprecated
	 */
	@Deprecated(forRemoval = true, since = "1.1.0")
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
	 * @deprecated
	 */
	@Deprecated(forRemoval = true, since = "1.1.0")
	public void setStrokeColor(Color strokeColor) {
		if (Objects.nonNull(strokeColor)) {
			this.strokeColor = strokeColor;
		}
	}

	/**
	 * 获取填充颜色。
	 *
	 * @return 填充颜色对象
	 * @since 1.0.0
	 * @deprecated
	 */
	@Deprecated(forRemoval = true, since = "1.1.0")
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
	 * @deprecated
	 */
	@Deprecated(forRemoval = true, since = "1.1.0")
	public void setFillColor(Color fillColor) {
		if (Objects.nonNull(fillColor)) {
			this.fillColor = fillColor;
		}
	}

	/**
	 * 根据目标图像和文字创建 Caption 对象
	 * <p>
	 * 字体会根据字体大小策略进行动态计算。
	 * 如果设置了方向，则直接使用该方向；否则使用自定义坐标。
	 * </p>
	 *
	 * <p>字体大小计算流程：</p>
	 * <ol>
	 *   <li>使用 {@code fontSizeStrategy} 函数基于目标图像尺寸计算基础字体大小（单位：pt）</li>
	 *   <li>将基础大小乘以 {@link #FONT_SCALE}（2.45）得到最终像素尺寸</li>
	 * </ol>
	 *
	 * <p>坐标计算说明：</p>
	 * <ul>
	 *   <li>使用自定义坐标时，Y 坐标会自动添加边距值以补偿 Thumbnailator 的渲染特点</li>
	 *   <li>使用方向坐标时，使用 {@link Direction} 内部类，在九宫格位置基础上添加边距</li>
	 * </ul>
	 *
	 * @param text        水印文字
	 * @param targetImage 目标图像
	 * @return 配置好的 Caption 对象
	 * @throws IllegalArgumentException 如果任一参数为 null 或文字为空
	 * @since 1.1.0
	 */
	public Caption toCaption(String text, BufferedImage targetImage) {
		Validate.notNull(targetImage, "targetImage 不可为 null");
		Validate.notBlank(text, "text 不可为空");

		ImageSize targetImageSize = new ImageSize(targetImage.getWidth(), targetImage.getHeight());
		Font font = new Font(fontName, fontStyle, Math.round((fontSizeStrategy.applyAsInt(targetImageSize) *
			FONT_SCALE)));

		// Caption 计算y坐标不使用insets，需要手动添加
		Position coordinate;
		if (Objects.nonNull(direction)) {
			coordinate = new Direction(direction, margin);
		} else {
			Graphics graphics = targetImage.getGraphics();
			try {
				graphics.setFont(font);

				FontMetrics fontMetrics = graphics.getFontMetrics();
				int textWidth = fontMetrics.stringWidth(text);

				int x = Math.max(0, Math.min(targetImageSize.getWidth() - textWidth - margin, this.x));
				int y = Math.max(margin, Math.min(targetImageSize.getHeight() - margin, this.y + margin));
				coordinate = new Coordinate(x, y);
			} finally {
				if (Objects.nonNull(graphics)) {
					graphics.dispose();
				}
			}
		}
		return new Caption(text, font, color, opacity, coordinate, margin);
	}

	/**
	 * 扩展的位置计算类，在九宫格位置基础上添加 Y 轴边距
	 *
	 * <p>这是一个内部辅助类，解决 Thumbnailator Caption 在计算 Y 坐标时不使用边距的问题。</p>
	 * <p>工作原理：使用 {@link Positions} 计算出基础位置后，再在 Y 轴上添加指定的边距值。</p>
	 *
	 * @author pangju666
	 * @since 1.1.0
	 */
	protected static final class Direction implements Position {
		/**
		 * 基础九宫格位置
		 */
		private final Positions positions;

		/**
		 * Y 轴边距值（单位：像素）
		 */
		private final int margin;

		/**
		 * 创建带有边距的位置计算器
		 *
		 * @param positions 基础九宫格位置，不能为空
		 * @param margin    Y 轴边距值
		 * @since 1.1.0
		 */
		public Direction(Positions positions, int margin) {
			this.positions = positions;
			this.margin = margin;
		}

		/**
		 * 计算最终位置坐标
		 *
		 * <p>先使用基础 {@link Positions} 计算位置，然后在 Y 轴上添加边距进行补偿</p>
		 *
		 * @param enclosingWidth  容器宽度
		 * @param enclosingHeight 容器高度
		 * @param width           元素宽度
		 * @param height          元素高度
		 * @param insetLeft       左边距
		 * @param insetRight      右边距
		 * @param insetTop        上边距
		 * @param insetBottom     下边距
		 * @return 计算出的最终位置点
		 * @since 1.1.0
		 */
		@Override
		public Point calculate(int enclosingWidth, int enclosingHeight, int width, int height, int insetLeft,
							   int insetRight, int insetTop, int insetBottom) {
			Point point = positions.calculate(enclosingWidth, enclosingHeight, width,  height, insetLeft, insetRight, insetTop,
				insetBottom);
			point.y += margin;
			return point;
		}
	}
}