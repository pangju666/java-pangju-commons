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

package io.github.pangju666.commons.ffmpeg.model;

import io.github.pangju666.commons.ffmpeg.builder.FFmpegFiltersBuilder;
import io.github.pangju666.commons.ffmpeg.enums.Direction;
import io.github.pangju666.commons.ffmpeg.lang.FFmpegConstants;
import io.github.pangju666.commons.ffmpeg.utils.FFmpegUtils;
import io.github.pangju666.commons.io.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.function.IntBinaryOperator;

/**
 * 文字水印配置选项类
 * <p>
 * 用于配置视频文字水印的各项参数，包括字体、颜色、位置、透明度等。
 * 支持通过系统字体或字体文件指定，提供多种预设位置和自定义位置选项。
 * </p>
 * <h3>核心特性</h3>
 * <ul>
 *     <li>支持系统字体和自定义字体文件（TTF 格式）</li>
 *     <li>可配置填充色和描边色</li>
 *     <li>支持多种预定义水印位置</li>
 *     <li>自适应字体大小策略</li>
 *     <li>支持自定义位置和边距</li>
 *     <li>可自定义字体大小策略</li>
 * </ul>
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 使用系统字体
 * TextWatermarkOption option = new TextWatermarkOption("Arial");
 * option.setFillColor(Color.WHITE);
 * option.setStrokeColor(Color.BLACK);
 * option.setDirection(Direction.BOTTOM_RIGHT);
 *
 * // 使用字体文件
 * TextWatermarkOption option = new TextWatermarkOption(new File("font.ttf"));
 * option.setOpacity(0.5f);
 *
 * // 转换为 FFmpeg 滤镜
 * String filter = option.toFFmpegFilter("水印文字", grabber);
 * }</pre>
 *
 * @author pangju666
 * @see ImageWatermarkOption
 * @see Direction
 * @since 2.1.0
 */
public class TextWatermarkOption {
	/**
	 * 颜色十六进制格式字符串
	 * <p>用于将 Color 对象转换为 FFmpeg drawtext 滤镜所需的十六进制颜色格式（如 #ffffff）</p>
	 *
	 * @since 2.1.0
	 */
	protected static final String COLOR_HEX_FORMAT = "#%02x%02x%02x";

	/**
	 * 系统字体名称
	 * <p>使用系统字体时设置此项，fontFile 为 null</p>
	 *
	 * @since 2.1.0
	 */
	protected final String fontName;

	/**
	 * 字体文件
	 * <p>使用自定义字体文件时设置此项，fontName 为 null</p>
	 *
	 * @since 2.1.0
	 */
	protected final File fontFile;

	/**
	 * 水印透明度，范围 0.0-1.0，默认 0.4
	 *
	 * @since 2.1.0
	 */
	protected float opacity = 0.4f;

	/**
	 * 描边颜色，默认黑色
	 *
	 * @since 2.1.0
	 */
	protected Color strokeColor = Color.BLACK;

	/**
	 * 填充颜色，默认白色
	 *
	 * @since 2.1.0
	 */
	protected Color fillColor = Color.WHITE;

	/**
	 * 描边宽度，默认 2.0
	 *
	 * @since 2.1.0
	 */
	protected float strokeWidth = 2.0f;

	/**
	 * 是否启用描边，默认 true
	 *
	 * @since 2.1.0
	 */
	protected boolean stroke = true;

	/**
	 * X 坐标位置，仅在未设置方向时生效
	 *
	 * @since 2.1.0
	 */
	protected int x = 0;

	/**
	 * Y 坐标位置，仅在未设置方向时生效
	 *
	 * @since 2.1.0
	 */
	protected int y = 0;

	/**
	 * 边距大小，默认 20
	 *
	 * @since 2.1.0
	 */
	protected int margin = 20;

	/**
	 * 水印位置方向，null 表示使用自定义坐标
	 *
	 * @since 2.1.0
	 */
	protected Direction direction;

	/**
	 * 自适应字体大小策略函数
	 * <p>根据视频宽高计算合适的字体大小。</p>
	 * <p><b>计算策略：</b></p>
	 * <ul>
	 *   <li>取视频宽度和高度的较小值作为基准</li>
	 *   <li>当较短边 &lt; 600 时，字体大小为 32</li>
	 *   <li>当 600 ≤ 较短边 &lt; 1920 时，从 32 线性增长到 48</li>
	 *   <li>当较短边 ≥ 1920 时，从 48 线性增长到 160（上限为 6000）</li>
	 * </ul>
	 *
	 * @since 2.1.0
	 */
	protected IntBinaryOperator fontSizeStrategy = (width, height) -> {
		int shorter = Math.min(width, height);
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
	 * 使用字体文件创建文字水印配置
	 *
	 * @param fontFile 字体文件，必须是 TTF 格式
	 * @throws IOException              文件读取失败时抛出
	 * @throws IllegalArgumentException 当文件不是 TTF 格式时抛出
	 * @since 2.1.0
	 */
	public TextWatermarkOption(File fontFile) throws IOException {
		Validate.isTrue(FileUtils.isMimeType(fontFile, FFmpegConstants.TTF_MIME_TYPE),
			"fontFile 不是字体文件");

		this.fontFile = fontFile;
		this.fontName = null;
	}

	/**
	 * 使用系统字体创建文字水印配置
	 *
	 * @param fontName 系统字体名称
	 * @throws IllegalArgumentException 当字体名称为空时抛出
	 * @since 2.1.0
	 */
	public TextWatermarkOption(String fontName) {
		Validate.notBlank(fontName, "fontName 不能为空");

		this.fontFile = null;
		this.fontName = fontName;
	}

	/**
	 * 获取水印透明度
	 *
	 * @return 透明度值，范围 0.0-1.0
	 * @since 2.1.0
	 */
	public float getOpacity() {
		return opacity;
	}

	/**
	 * 设置水印透明度
	 * <p>超出范围 0.0-1.0 的值将被忽略并保持当前值</p>
	 *
	 * @param opacity 透明度值，范围 0.0-1.0
	 * @since 2.1.0
	 */
	public void setOpacity(float opacity) {
		if (opacity >= 0f && opacity <= 1) {
			this.opacity = opacity;
		}
	}

	/**
	 * 获取描边颜色
	 *
	 * @return 描边颜色
	 * @since 2.1.0
	 */
	public Color getStrokeColor() {
		return strokeColor;
	}

	/**
	 * 设置描边颜色。
	 * 参数为 {@code null} 时忽略。
	 *
	 * @param strokeColor 描边颜色
	 * @since 2.1.0
	 */
	public void setStrokeColor(Color strokeColor) {
		if (Objects.nonNull(strokeColor)) {
			this.strokeColor = strokeColor;
		}
	}

	/**
	 * 获取填充颜色
	 *
	 * @return 填充颜色
	 * @since 2.1.0
	 */
	public Color getFillColor() {
		return fillColor;
	}

	/**
	 * 设置填充颜色。
	 * 参数为 {@code null} 时忽略。
	 *
	 * @param fillColor 填充颜色
	 * @since 2.1.0
	 */
	public void setFillColor(Color fillColor) {
		if (Objects.nonNull(fillColor)) {
			this.fillColor = fillColor;
		}
	}

	/**
	 * 获取描边宽度
	 *
	 * @return 描边宽度
	 * @since 2.1.0
	 */
	public float getStrokeWidth() {
		return strokeWidth;
	}

	/**
	 * 设置描边宽度
	 * <p>非正数将被忽略并保持当前值</p>
	 *
	 * @param strokeWidth 描边宽度，必须大于 0
	 * @since 2.1.0
	 */
	public void setStrokeWidth(float strokeWidth) {
		if (strokeWidth > 0f) {
			this.strokeWidth = strokeWidth;
		}
	}

	/**
	 * 判断是否启用描边
	 *
	 * @return true 表示启用描边，false 表示不启用
	 * @since 2.1.0
	 */
	public boolean isStroke() {
		return stroke;
	}

	/**
	 * 设置是否启用描边
	 *
	 * @param stroke 是否启用描边
	 * @since 2.1.0
	 */
	public void setStroke(boolean stroke) {
		this.stroke = stroke;
	}

	/**
	 * 获取 X 坐标
	 *
	 * @return X 坐标值
	 * @since 2.1.0
	 */
	public int getX() {
		return x;
	}

	/**
	 * 设置 X 坐标
	 * <p>负值将被忽略并保持当前值</p>
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
	 * 获取 Y 坐标
	 *
	 * @return Y 坐标值
	 * @since 2.1.0
	 */
	public int getY() {
		return y;
	}

	/**
	 * 设置 Y 坐标
	 * <p>负值将被忽略并保持当前值</p>
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
	 * 获取边距大小
	 *
	 * @return 边距值
	 * @since 2.1.0
	 */
	public int getMargin() {
		return margin;
	}

	/**
	 * 设置边距大小
	 * <p>负值将被忽略并保持当前值</p>
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
	 * 获取水印位置方向
	 *
	 * @return 位置方向枚举，null 表示使用自定义坐标
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
	 * 获取字体大小策略
	 *
	 * @return 字体大小计算策略函数
	 * @since 2.1.0
	 */
	public IntBinaryOperator getFontSizeStrategy() {
		return fontSizeStrategy;
	}

	/**
	 * 设置字体大小策略
	 * <p>null 值将被忽略并保持原策略</p>
	 *
	 * @param fontSizeStrategy 字体大小计算策略函数
	 * @since 2.1.0
	 */
	public void setFontSizeStrategy(IntBinaryOperator fontSizeStrategy) {
		if (Objects.nonNull(fontSizeStrategy)) {
			this.fontSizeStrategy = fontSizeStrategy;
		}
	}

	/**
	 * 将文字水印配置转换为 FFmpeg 滤镜字符串
	 * <p>
	 * 从 FFmpegFrameGrabber 中获取视频尺寸信息，然后调用另一个重载方法生成滤镜字符串。
	 * 如果 grabber 未启动，会自动启动它。
	 * </p>
	 *
	 * @param text    水印文字内容
	 * @param grabber FFmpeg 帧抓取器，用于获取视频尺寸
	 * @return FFmpeg drawtext 滤镜字符串
	 * @throws IOException              操作失败时抛出
	 * @throws IllegalArgumentException 当参数无效时抛出
	 * @since 2.1.0
	 */
	public String toFFmpegFilter(final String text, final FFmpegFrameGrabber grabber) throws IOException {
		Validate.notNull(grabber, "grabber 不能为 null");

		if (FFmpegUtils.isNotStarted(grabber)) {
			grabber.start();
		}
		Validate.isTrue(grabber.hasVideo(), "grabber 不存在视频流");

		return toFFmpegFilter(text, grabber.getImageWidth(), grabber.getImageHeight());
	}

	/**
	 * 将文字水印配置转换为 FFmpeg 滤镜字符串
	 * <p>
	 * 根据视频尺寸和水印配置生成 FFmpeg drawtext 滤镜字符串。
	 * 滤镜包含以下处理步骤：
	 * </p>
	 * <ul>
	 *   <li>设置水印文字内容</li>
	 *   <li>根据字体文件或系统字体名称设置字体</li>
	 *   <li>根据字体大小策略计算字体大小</li>
	 *   <li>设置填充颜色和描边颜色</li>
	 *   <li>应用透明度设置</li>
	 *   <li>根据方向或自定义坐标计算水印位置</li>
	 * </ul>
	 *
	 * @param text        水印文字内容
	 * @param videoWidth  视频宽度，必须大于 0
	 * @param videoHeight 视频高度，必须大于 0
	 * @return FFmpeg drawtext 滤镜字符串
	 * @since 2.1.0
	 */
	public String toFFmpegFilter(final String text, final int videoWidth, final int videoHeight) {
		Validate.notBlank(text, "text 不能为空");
		Validate.isTrue(videoWidth > 0, "videoWidth 必须大于 0");
		Validate.isTrue(videoHeight > 0, "videoHeight 必须大于 0");

		return FFmpegFiltersBuilder.video()
			.addGlobalFilter("drawtext",
				String.format("text='%s'", text),
				Objects.nonNull(fontFile) ? String.format("fontfile='%s'", FFmpegUtils.getSafeFileSourcePath(
					fontFile.getAbsolutePath())) : String.format("font=%s", fontName),
				computePositionArgs(),
				"alpha=" + opacity,
				"fontsize=" + fontSizeStrategy.applyAsInt(videoWidth, videoHeight),
				"fontcolor=" + String.format(COLOR_HEX_FORMAT, fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue()),
				stroke ? "borderw=" + strokeWidth : StringUtils.EMPTY,
				stroke ? "bordercolor=" + String.format(COLOR_HEX_FORMAT, strokeColor.getRed(), strokeColor.getGreen(),
					strokeColor.getBlue()) : StringUtils.EMPTY)
			.build();
	}

	/**
	 * 计算位置参数
	 * <p>根据方向或自定义坐标计算 FFmpeg drawtext 滤镜的位置参数</p>
	 *
	 * @return FFmpeg drawtext 滤镜的位置参数字符串
	 * @since 2.1.0
	 */
	protected String computePositionArgs() {
		if (Objects.isNull(direction)) {
			String exprX = String.format("max(%d,min(W-text_w-%d,%d))", margin, margin, x + margin);
			String exprY = String.format("max(%d,min(H-text_h-%d,%d))", margin, margin, y + margin);
			return String.format("x='%s':y='%s'", exprX, exprY);
		}

		return switch (direction) {
			case TOP -> String.format("x=(W-text_w)/2:y=text_h+%d", margin);
			case TOP_LEFT -> String.format("x=%d:y=text_h+%d", margin, margin);
			case TOP_RIGHT -> String.format("x=W-text_w-%d:y=text_h+%d", margin, margin);
			case BOTTOM -> String.format("x=(W-text_w)/2:y=H-text_h-%d", margin);
			case BOTTOM_LEFT -> String.format("x=%d:y=H-text_h-%d", margin, margin);
			case BOTTOM_RIGHT -> String.format("x=W-text_w-%d:y=H-text_h-%d", margin, margin);
			case LEFT -> String.format("x=%d:y=(H+text_h)/2", margin);
			case RIGHT -> String.format("x=W-text_w-%d:y=(H+text_h)/2", margin);
			default -> "x=(W-text_w)/2:y=(H+text_h)/2";
		};
	}
}