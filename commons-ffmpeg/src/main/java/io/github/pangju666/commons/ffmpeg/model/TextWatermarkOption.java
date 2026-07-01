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

package io.github.pangju666.commons.ffmpeg.model;

import io.github.pangju666.commons.ffmpeg.lang.FFmpegConstants;
import io.github.pangju666.commons.ffmpeg.utils.FFmpegFiltersBuilder;
import io.github.pangju666.commons.ffmpeg.utils.FFmpegUtils;
import io.github.pangju666.commons.image.enums.WatermarkDirection;
import io.github.pangju666.commons.image.utils.ImageUtils;
import io.github.pangju666.commons.io.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.function.IntBinaryOperator;

public class TextWatermarkOption {
	protected final String fontName;
	protected final File fontFile;

	protected float opacity = 0.4f;
	protected int fontSize = 0;
	protected Color strokeColor = Color.BLACK;
	protected Color fillColor = Color.WHITE;
	protected float strokeWidth = 2.0f;
	protected boolean stroke = true;
	protected int x = 0;
	protected int y = 0;
	protected int margin = 20;
	protected WatermarkDirection direction;
	protected IntBinaryOperator fontSizeStrategy = (width, height) -> {
		int shorter = Math.min(width, height);
		if (shorter < 600) {
			// 低分辨率：强制 32pt
			return 32;
		} else if (shorter >= 1920) {
			// 大分辨率：48pt~80pt 缓慢增长
			double ratio = Math.min(1.0, (shorter - 1920.0) / 3000.0);
			return (int) Math.round(48 + ratio * (80 - 48));
		} else {
			// 中等分辨率：32pt~48pt 线性增长
			double ratio = (shorter - 600.0) / (1920.0 - 600.0);
			return (int) Math.round(32 + ratio * (48 - 32));
		}
	};

	public TextWatermarkOption(File fontFile) throws IOException {
		FileUtils.isMimeType(fontFile, FFmpegConstants.TTF_MIME_TYPE);

		this.fontFile = fontFile;
		this.fontName = null;
	}

	public TextWatermarkOption(String fontName) {
		Validate.notBlank(fontName, "fontName 不能为空");

		this.fontFile = null;
		this.fontName = fontName;
	}

	public float getOpacity() {
		return opacity;
	}

	public void setOpacity(float opacity) {
		if (opacity >= 0f && opacity <= 1) {
			this.opacity = opacity;
		}
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		if (fontSize > 0) {
			this.fontSize = fontSize;
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

	public float getStrokeWidth() {
		return strokeWidth;
	}

	public void setStrokeWidth(float strokeWidth) {
		if (strokeWidth > 0f) {
			this.strokeWidth = strokeWidth;
		}
	}

	public boolean isStroke() {
		return stroke;
	}

	public void setStroke(boolean stroke) {
		this.stroke = stroke;
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

	public int getMargin() {
		return margin;
	}

	public void setMargin(int margin) {
		if (margin >= 0) {
			this.margin = margin;
		}
	}

	public WatermarkDirection getDirection() {
		return direction;
	}

	public void setDirection(WatermarkDirection direction) {
		if (Objects.nonNull(direction)) {
			this.direction = direction;
		}
	}

	public IntBinaryOperator getFontSizeStrategy() {
		return fontSizeStrategy;
	}

	public void setFontSizeStrategy(IntBinaryOperator fontSizeStrategy) {
		if (Objects.nonNull(fontSizeStrategy)) {
			this.fontSizeStrategy = fontSizeStrategy;
		}
	}

	public String toFFmpegFilter(final String text, final FFmpegFrameGrabber grabber) throws IOException {
		Validate.notNull(grabber, "grabber 不能为 null");

		if (!FFmpegUtils.isNotStarted(grabber)) {
			grabber.start();
		}
		Validate.isTrue(grabber.hasVideo(), "grabber 不存在视频流");

		return toFFmpegFilter(text, grabber.getImageWidth(), grabber.getImageHeight());
	}

	public String toFFmpegFilter(final String text, final int videoWith, final int videoHeight) throws IOException {
		Validate.notBlank(text, "text 不能为空");
		Validate.isTrue(videoWith > 0, "videoWith 必须大于 0");
		Validate.isTrue(videoHeight > 0, "videoHeight 必须大于 0");

		return FFmpegFiltersBuilder.video()
			.addGlobalFilter("drawtext",
				String.format("text='%s'", text),
				Objects.nonNull(fontFile) ? String.format("fontfile='%s'", FFmpegUtils.getSafeFilePath(fontFile)) :
					String.format("font=%s", fontName),
				computePositionArgs(),
				"alpha=" + opacity,
				"fontsize=" + (fontSize > 0 ? fontSize : fontSizeStrategy.applyAsInt(videoWith, videoHeight)),
				"fontcolor=" + ImageUtils.toHexColor(fillColor),
				stroke ? "borderw=" + strokeWidth : StringUtils.EMPTY,
				stroke ? "bordercolor=" + ImageUtils.toHexColor(strokeColor) : StringUtils.EMPTY)
			.build();
	}

	protected String computePositionArgs() {
		if (Objects.isNull(direction)) {
			return String.format("x=%d:y=%d", x + margin, y + margin);
		}

		return switch (direction) {
			case TOP -> String.format("x=%s:y=%d", "(w-text_w)/2", margin);
			case TOP_LEFT -> String.format("x=%d:y=%d", margin, margin);
			case TOP_RIGHT -> String.format("x=%s:y=%d", "w-text_w-" + margin, margin);
			case BOTTOM -> String.format("x=%s:y=%s", "(w-text_w)/2", "h-text_h-" + margin);
			case BOTTOM_LEFT -> String.format("x=%d:y=%s", margin, "h-text_h-" + margin);
			case BOTTOM_RIGHT -> String.format("x=%s:y=%s", "w-text_w-" + margin, "h-text_h-" + margin);
			case LEFT -> String.format("x=%d:y=%s", margin, "(h-text_h)/2");
			case RIGHT -> String.format("x=%s:y=%s", "w-text_w-" + margin, "(h-text_h)/2");
			case CENTER -> String.format("x=%s:y=%s", "(w-text_w)/2", "(h-text_h)/2");
		};
	}
}