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

import io.github.pangju666.commons.ffmpeg.utils.FFmpegFiltersBuilder;
import io.github.pangju666.commons.ffmpeg.utils.FFmpegUtils;
import io.github.pangju666.commons.image.enums.WatermarkDirection;
import org.apache.commons.lang3.Validate;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class ImageWatermarkOption {
	protected double relativeScale = 0.15;
	protected float opacity = 0.4f;
	protected int x = 0;
	protected int y = 0;
	protected int margin = 10;
	protected WatermarkDirection direction;

	public double getRelativeScale() {
		return relativeScale;
	}

	public void setRelativeScale(double relativeScale) {
		if (relativeScale > 0) {
			this.relativeScale = relativeScale;
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

	public String toFFmpegFilter(File watermarkImageFile, FFmpegFrameGrabber grabber) throws IOException {
		Validate.notNull(grabber, "grabber 不能为 null");

		if (FFmpegUtils.isNotStarted(grabber)) {
			grabber.start();
		}
		Validate.isTrue(grabber.hasVideo(), "grabber 不存在视频流");

		return toFFmpegFilter(watermarkImageFile, grabber.getImageWidth(), grabber.getImageHeight());
	}

	public String toFFmpegFilter(File watermarkImageFile, int videoWith, int videoHeight) throws IOException {
		Validate.isTrue(videoWith > 0, "videoWith 必须大于0");
		Validate.isTrue(videoHeight > 0, "videoHeight 必须大于0");

		return FFmpegFiltersBuilder.video()
			.addFileSource("wm", watermarkImageFile)
			.appendAliasFilter("wm", "scale=" + (videoWith > videoHeight ?
				String.format("iw*%.2f:-1", relativeScale) : String.format("-1:ih*%.2f", relativeScale)))
			.appendAliasFilter("wm", "format=rgba")
			.appendAliasFilter("wm", "colorchannelmixer", "aa=" + opacity)
			.addGlobalFilter("overlay", computePositionArgs(), "format=auto")
			.build();
	}

	protected String computePositionArgs() {
		if (Objects.isNull(direction)) {
			return String.format("x=%d:y=%d", x + margin, y + margin);
		}

		return switch (direction) {
			case TOP -> String.format("x=%s:y=%d", "(W-w)/2", margin);
			case TOP_LEFT -> String.format("x=%d:y=%d", margin, margin);
			case TOP_RIGHT -> String.format("x=%s:y=%d", "W-w-" + margin, margin);
			case BOTTOM -> String.format("x=%s:y=%s", "(W-w)/2", "H-h-" + margin);
			case BOTTOM_LEFT -> String.format("x=%d:y=%s", margin, "H-h-" + margin);
			case BOTTOM_RIGHT -> String.format("x=%s:y=%s", "W-w-" + margin, "H-h-" + margin);
			case LEFT -> String.format("x=%d:y=%s", margin, "(H-h)/2");
			case RIGHT -> String.format("x=%s:y=%s", "W-w-" + margin, "(H-h)/2");
			case CENTER -> String.format("x=%s:y=%s", "(W-w)/2", "(H-h)/2");
		};
	}
}