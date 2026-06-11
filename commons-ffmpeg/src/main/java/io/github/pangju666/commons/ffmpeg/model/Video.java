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

import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.time.Duration;

public class Video extends Media {
	private Duration duration;       // 时长
	private double frameRate;        // 帧数/秒
	private int frames;              // 总帧数
	private int width;                 // 视频宽度
	private int height;             // 视频高度
	private long bitrate;             // 视频码率(bps)
	private Audio audio;             // 音频信息

	public Video() {
		super();
	}

	protected void init(FFmpegFrameGrabber grabber) {
		//super.init(grabber);

		if (grabber.hasVideo()) {
			this.metadata = grabber.getVideoMetadata();
			this.codecName = grabber.getVideoCodecName();
			this.codecId = grabber.getVideoCodec();

			this.duration = Duration.ofNanos(grabber.getLengthInTime() * 1000);
			this.frameRate = grabber.getVideoFrameRate();
			this.frames = grabber.getLengthInFrames();
			this.width = grabber.getImageWidth();
			this.height = grabber.getImageHeight();
			this.frameRate = grabber.getVideoFrameRate();
			this.bitrate = grabber.getVideoBitrate();
		}

		if (grabber.hasAudio()) {
			//this.audio = new Audio();
			//this.audio.init(grabber);
		}
	}

	public Duration getDuration() {
		return duration;
	}

	public void setDuration(Duration duration) {
		this.duration = duration;
	}

	public double getFrameRate() {
		return frameRate;
	}

	public void setFrameRate(double frameRate) {
		this.frameRate = frameRate;
	}

	public int getFrames() {
		return frames;
	}

	public void setFrames(int frames) {
		this.frames = frames;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public long getBitrate() {
		return bitrate;
	}

	public void setBitrate(long bitrate) {
		this.bitrate = bitrate;
	}

	public Audio getAudio() {
		return audio;
	}

	public void setAudio(Audio audio) {
		this.audio = audio;
	}
}
