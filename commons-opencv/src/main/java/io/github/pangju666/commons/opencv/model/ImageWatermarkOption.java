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

package io.github.pangju666.commons.opencv.model;

import io.github.pangju666.commons.opencv.enums.Direction;
import org.apache.commons.lang3.tuple.Pair;
import org.bytedeco.opencv.opencv_core.Size;

import java.util.function.Function;

/**
 * 图像水印配置类
 *
 * <p>提供图像水印的完整配置，包括缩放比例、透明度、边距、位置和尺寸限制策略等。</p>
 * <p>支持九宫格方向定位和自定义坐标定位两种方式。</p>
 *
 * <h2>核心配置</h2>
 * <ul>
 *   <li><strong>相对缩放因子</strong>：水印相对于目标图像短边的比例（默认 0.15）</li>
 *   <li><strong>透明度</strong>：水印的不透明度，范围 0.0 - 1.0（默认 0.4）</li>
 *   <li><strong>边距</strong>：水印与图像边缘的距离（默认 10 像素）</li>
 *   <li><strong>尺寸限制策略</strong>：根据目标图像尺寸动态计算水印的最小和最大尺寸</li>
 * </ul>
 *
 * <h2>定位方式</h2>
 * <ul>
 *   <li>优先使用 {@link #direction}（九宫格方向）</li>
 *   <li>未设置方向时使用自定义坐标 {@link #x}, {@link #y}</li>
 * </ul>
 *
 * @author pangju666
 * @see Direction
 * @since 2.1.0
 */
public class ImageWatermarkOption {
	/**
	 * 相对缩放因子（相对于目标图像短边）
	 *
	 * <p>默认值 0.15，表示水印尺寸为目标图像短边的 15%</p>
	 *
	 * @since 2.1.0
	 */
	private double relativeScaleFactor = 0.15;

	/**
	 * 水印透明度（不透明度）
	 *
	 * <p>范围：0.0（完全透明）- 1.0（完全不透明），默认值 0.4</p>
	 *
	 * @since 2.1.0
	 */
	private float opacity = 0.4f;

	/**
	 * 水印边距（与图像边缘的距离）
	 *
	 * <p>单位：像素，默认值 10</p>
	 *
	 * @since 2.1.0
	 */
	private int margin = 10;

	/**
	 * 自定义 X 坐标
	 *
	 * <p>仅在未设置方向时使用，默认值 0</p>
	 *
	 * @since 2.1.0
	 */
	private int x = 0;

	/**
	 * 自定义 Y 坐标
	 *
	 * <p>仅在未设置方向时使用，默认值 0</p>
	 *
	 * @since 2.1.0
	 */
	private int y = 0;

	/**
	 * 水印位置方向（九宫格）
	 *
	 * <p>优先使用此参数设置水印位置，未设置时使用自定义坐标</p>
	 *
	 * @since 2.1.0
	 */
	private Direction direction;

	/**
	 * 尺寸限制策略
	 *
	 * <p>根据目标图像尺寸返回水印的最小尺寸和最大尺寸（Pair&lt;最小尺寸, 最大尺寸&gt;）。</p>
	 * <p>默认策略：</p>
	 * <ul>
	 *   <li>小图（短边 &lt; 600px）：120-150px</li>
	 *   <li>中等图（600 ≤ 短边 &lt; 1920）：150-250px</li>
	 *   <li>大图（短边 ≥ 1920）：250-400px</li>
	 * </ul>
	 *
	 * @since 2.1.0
	 */
	private Function<Size, Pair<Size, Size>> sizeLimitStrategy = imageSize -> {
		int shorter = Math.min(imageSize.width(), imageSize.height());
		if (shorter < 600) { // 小图
			return Pair.of(new Size(120, 120), new Size(150, 150));
		} else if (shorter >= 1920) { // 大图（注意：>=1920）
			return Pair.of(new Size(250, 250), new Size(400, 400));
		} else { // 中等图
			return Pair.of(new Size(150, 150), new Size(250, 250));
		}
	};

	/**
	 * 获取相对缩放因子
	 *
	 * @return 相对缩放因子（相对于目标图像短边的比例）
	 * @since 2.1.0
	 */
	public double getRelativeScaleFactor() {
		return relativeScaleFactor;
	}

	/**
	 * 设置相对缩放因子
	 *
	 * @param relativeScaleFactor 相对缩放因子，必须大于 0
	 * @since 2.1.0
	 */
	public void setRelativeScaleFactor(double relativeScaleFactor) {
		if (relativeScaleFactor > 0) {
			this.relativeScaleFactor = relativeScaleFactor;
		}
	}

	/**
	 * 获取水印透明度
	 *
	 * @return 透明度值，范围 0.0 - 1.0
	 * @since 2.1.0
	 */
	public float getOpacity() {
		return opacity;
	}

	/**
	 * 设置水印透明度
	 *
	 * @param opacity 透明度值，范围 0.0 - 1.0
	 * @since 2.1.0
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
	 * @since 2.1.0
	 */
	public int getMargin() {
		return margin;
	}

	/**
	 * 设置水印边距
	 *
	 * @param margin 边距值，必须大于等于 0
	 * @since 2.1.0
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
	 * @since 2.1.0
	 */
	public int getX() {
		return x;
	}

	/**
	 * 设置自定义 X 坐标
	 *
	 * @param x X 坐标值，必须大于等于 0
	 * @since 2.1.0
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
	 * @since 2.1.0
	 */
	public int getY() {
		return y;
	}

	/**
	 * 设置自定义 Y 坐标
	 *
	 * @param y Y 坐标值，必须大于等于 0
	 * @since 2.1.0
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
	 * @since 2.1.0
	 */
	public Direction getDirection() {
		return direction;
	}

	/**
	 * 设置水印位置方向
	 *
	 * @param direction 位置方向枚举
	 * @since 2.1.0
	 */
	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	/**
	 * 获取尺寸限制策略
	 *
	 * @return 尺寸限制策略函数，输入目标图像尺寸，返回 Pair&lt;最小尺寸, 最大尺寸&gt;
	 * @since 2.1.0
	 */
	public Function<Size, Pair<Size, Size>> getSizeLimitStrategy() {
		return sizeLimitStrategy;
	}

	/**
	 * 设置尺寸限制策略
	 *
	 * @param sizeLimitStrategy 尺寸限制策略函数
	 * @since 2.1.0
	 */
	public void setSizeLimitStrategy(Function<Size, Pair<Size, Size>> sizeLimitStrategy) {
		this.sizeLimitStrategy = sizeLimitStrategy;
	}
}
