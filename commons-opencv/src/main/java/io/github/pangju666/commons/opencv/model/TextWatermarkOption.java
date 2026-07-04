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

import io.github.pangju666.commons.opencv.enums.Direction;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Size;

import java.awt.*;
import java.util.Objects;
import java.util.function.ToDoubleBiFunction;

/**
 * 文本水印配置类
 *
 * <p>提供文本水印的完整配置，包括字体、颜色、描边、透明度、位置和字体缩放策略等。</p>
 * <p>支持九宫格方向定位和自定义坐标定位两种方式，支持文字描边效果。</p>
 *
 * <h2>核心配置</h2>
 * <ul>
 *   <li><strong>字体</strong>：OpenCV 内置字体（默认 FONT_HERSHEY_SIMPLEX）</li>
 *   <li><strong>颜色</strong>：填充色（默认白色）和描边色（默认黑色）</li>
 *   <li><strong>描边</strong>：可选的文字描边效果，支持设置描边大小</li>
 *   <li><strong>透明度</strong>：水印不透明度，范围 0.0 - 1.0（默认 0.4）</li>
 *   <li><strong>字体缩放策略</strong>：根据目标图像尺寸和字体类型动态计算合适的字体大小</li>
 * </ul>
 *
 * <h2>默认字体缩放策略</h2>
 * <ul>
 *   <li>小图（短边 &lt; 600px）：32pt</li>
 *   <li>中等图（600 ≤ 短边 &lt; 1920）：32-48pt 线性增长</li>
 *   <li>大图（短边 ≥ 1920）：48-160pt（随尺寸增长逐渐放缓）</li>
 * </ul>
 *
 * @author pangju666
 * @see Direction
 * @since 1.1.0
 */
public class TextWatermarkOption {
	/**
	 * 标准字体的缩放除数
	 *
	 * <p>用于将磅值（pt）转换为 OpenCV 的 fontScale</p>
	 *
	 * @since 1.1.0
	 */
	public static final double STD_FONT_DIV = 14.0;

	/**
	 * 手写体字体的缩放除数
	 *
	 * @since 1.1.0
	 */
	public static final double SCRIPT_FONT_DIV = 15.0;

	/**
	 * 小号字体的缩放除数
	 *
	 * @since 1.1.0
	 */
	public static final double SMALL_FONT_DIV = 12.0;

	/**
	 * 水印透明度（不透明度）
	 *
	 * <p>范围：0.0（完全透明）- 1.0（完全不透明），默认值 0.4</p>
	 *
	 * @since 1.1.0
	 */
	private float opacity = 0.4f;

	/**
	 * 水印边距（与图像边缘的距离）
	 *
	 * <p>单位：像素，默认值 20</p>
	 *
	 * @since 1.1.0
	 */
	private int margin = 20;

	/**
	 * 自定义 X 坐标
	 *
	 * <p>仅在未设置方向时使用，默认值 0</p>
	 *
	 * @since 1.1.0
	 */
	private int x = 0;

	/**
	 * 自定义 Y 坐标
	 *
	 * <p>仅在未设置方向时使用，默认值 0</p>
	 *
	 * @since 1.1.0
	 */
	private int y = 0;

	/**
	 * 水印位置方向（九宫格）
	 *
	 * <p>优先使用此参数设置水印位置，未设置时使用自定义坐标</p>
	 *
	 * @since 1.1.0
	 */
	private Direction direction;

	/**
	 * 描边颜色
	 *
	 * <p>默认值：黑色</p>
	 *
	 * @since 1.1.0
	 */
	private Color strokeColor = Color.BLACK;

	/**
	 * 填充颜色（文字颜色）
	 *
	 * <p>默认值：白色</p>
	 *
	 * @since 1.1.0
	 */
	private Color fillColor = Color.WHITE;

	/**
	 * 描边大小
	 *
	 * <p>单位：像素，默认值 2</p>
	 *
	 * @since 1.1.0
	 */
	private int strokeSize = 2;

	/**
	 * 是否启用描边
	 *
	 * <p>默认值：true（启用）</p>
	 *
	 * @since 1.1.0
	 */
	private boolean stroke = true;

	/**
	 * 文字线条粗细
	 *
	 * <p>单位：像素，默认值 3</p>
	 *
	 * @since 1.1.0
	 */
	private int thickness = 3;

	/**
	 * OpenCV 字体类型
	 *
	 * <p>默认值：FONT_HERSHEY_SIMPLEX</p>
	 *
	 * @since 1.1.0
	 */
	private int fontFace = opencv_imgproc.FONT_HERSHEY_SIMPLEX;

	/**
	 * 字体缩放策略
	 *
	 * <p>根据目标图像尺寸和字体类型计算合适的 fontScale 值。</p>
	 * <p>输入参数：(目标图像尺寸, 字体类型)，返回值：fontScale</p>
	 * <p>默认策略会根据图像尺寸动态计算，并根据字体类型使用不同的缩放除数。</p>
	 *
	 * @since 1.1.0
	 */
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

	/**
	 * 获取水印透明度
	 *
	 * @return 透明度值，范围 0.0 - 1.0
	 * @since 1.1.0
	 */
	public float getOpacity() {
		return opacity;
	}

	/**
	 * 设置水印透明度
	 *
	 * @param opacity 透明度值，范围 0.0 - 1.0
	 * @since 1.1.0
	 */
	public void setOpacity(float opacity) {
		if (opacity >= 0f && opacity <= 1) {
			this.opacity = opacity;
		}
	}

	/**
	 * 获取水印边距
	 *
	 * @return 边距值（单位：像素）
	 * @since 1.1.0
	 */
	public int getMargin() {
		return margin;
	}

	/**
	 * 设置水印边距
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
	 * 获取自定义 X 坐标
	 *
	 * @return X 坐标值
	 * @since 1.1.0
	 */
	public int getX() {
		return x;
	}

	/**
	 * 设置自定义 X 坐标
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
	 * 获取自定义 Y 坐标
	 *
	 * @return Y 坐标值
	 * @since 1.1.0
	 */
	public int getY() {
		return y;
	}

	/**
	 * 设置自定义 Y 坐标
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
	 * @return 位置方向枚举，可能为 null
	 * @since 1.1.0
	 */
	public Direction getDirection() {
		return direction;
	}

	/**
	 * 设置水印位置方向
	 *
	 * @param direction 位置方向枚举
	 * @since 1.1.0
	 */
	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	/**
	 * 是否启用描边
	 *
	 * @return true 表示启用描边，false 表示禁用
	 * @since 1.1.0
	 */
	public boolean isStroke() {
		return stroke;
	}

	/**
	 * 设置是否启用描边
	 *
	 * @param stroke 是否启用描边
	 * @since 1.1.0
	 */
	public void setStroke(boolean stroke) {
		this.stroke = stroke;
	}

	/**
	 * 获取描边大小
	 *
	 * @return 描边大小（单位：像素）
	 * @since 1.1.0
	 */
	public int getStrokeSize() {
		return strokeSize;
	}

	/**
	 * 设置描边大小
	 *
	 * @param strokeSize 描边大小，必须大于 0
	 * @since 1.1.0
	 */
	public void setStrokeSize(int strokeSize) {
		if (strokeSize > 0) {
			this.strokeSize = strokeSize;
		}
	}

	/**
	 * 获取描边颜色
	 *
	 * @return 描边颜色对象
	 * @since 1.1.0
	 */
	public Color getStrokeColor() {
		return strokeColor;
	}

	/**
	 * 设置描边颜色
	 *
	 * @param strokeColor 描边颜色对象，不能为 null
	 * @since 1.1.0
	 */
	public void setStrokeColor(Color strokeColor) {
		if (Objects.nonNull(strokeColor)) {
			this.strokeColor = strokeColor;
		}
	}

	/**
	 * 获取填充颜色（文字颜色）
	 *
	 * @return 填充颜色对象
	 * @since 1.1.0
	 */
	public Color getFillColor() {
		return fillColor;
	}

	/**
	 * 设置填充颜色（文字颜色）
	 *
	 * @param fillColor 填充颜色对象，不能为 null
	 * @since 1.1.0
	 */
	public void setFillColor(Color fillColor) {
		if (Objects.nonNull(fillColor)) {
			this.fillColor = fillColor;
		}
	}

	/**
	 * 获取文字线条粗细
	 *
	 * @return 线条粗细值（单位：像素）
	 * @since 1.1.0
	 */
	public int getThickness() {
		return thickness;
	}

	/**
	 * 设置文字线条粗细
	 *
	 * @param thickness 线条粗细值，必须大于 0
	 * @since 1.1.0
	 */
	public void setThickness(int thickness) {
		if (thickness > 0) {
			this.thickness = thickness;
		}
	}

	/**
	 * 获取字体缩放策略
	 *
	 * @return 字体缩放策略函数，输入参数：(目标图像尺寸, 字体类型)，返回值：fontScale
	 * @since 1.1.0
	 */
	public ToDoubleBiFunction<Size, Integer> getFontScaleStrategy() {
		return fontScaleStrategy;
	}

	/**
	 * 设置字体缩放策略
	 *
	 * @param fontScaleStrategy 字体缩放策略函数
	 * @since 1.1.0
	 */
	public void setFontScaleStrategy(ToDoubleBiFunction<Size, Integer> fontScaleStrategy) {
		this.fontScaleStrategy = fontScaleStrategy;
	}

	/**
	 * 获取 OpenCV 字体类型
	 *
	 * @return 字体类型常量（来自 opencv_imgproc）
	 * @since 1.1.0
	 */
	public int getFontFace() {
		return fontFace;
	}

	/**
	 * 设置 OpenCV 字体类型
	 *
	 * @param fontFace 字体类型常量（来自 opencv_imgproc）
	 * @since 1.1.0
	 */
	public void setFontFace(int fontFace) {
		this.fontFace = fontFace;
	}
}