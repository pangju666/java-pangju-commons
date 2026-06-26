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

package io.github.pangju666.commons.media.utils;

import io.github.pangju666.commons.media.model.Media;
import io.github.pangju666.commons.media.model.MediaResource;
import org.apache.commons.lang3.Validate;
import org.bytedeco.javacv.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

class FFmpegUtils {
	/**
	 * 音频变速 - 单级 atempo 最小速度（FFmpeg 原生限制）
	 *
	 * @since 1.1.0
	 */
	private static final float ATEMPO_MIN_SPEED = 0.5f;

	/**
	 * 音频变速 - 单级 atempo 最大速度（FFmpeg 原生限制）
	 *
	 * @since 1.1.0
	 */
	private static final float ATEMPO_MAX_SPEED = 2.0f;
	private static final float ATEMPO_SPEED_LIMIT = 10f;

	protected FFmpegUtils() {
	}

	public static String getAtempoFilter(final float speed) {
		Validate.isTrue(speed > 0, "变速倍率必须大于 0");

		List<String> audioFilterParts = new ArrayList<>();
		float current = Math.min(speed, ATEMPO_SPEED_LIMIT);
		// 大于2.0：不断除以 2.0 叠加 atempo=2.0
		while (current > ATEMPO_MAX_SPEED) {
			audioFilterParts.add("atempo=2.0");
			current /= ATEMPO_MAX_SPEED;
		}
		// 小于0.5：不断乘以 2.0 叠加 atempo=0.5
		while (current < ATEMPO_MIN_SPEED) {
			audioFilterParts.add("atempo=0.5");
			current *= ATEMPO_MIN_SPEED;
		}
		// 最后补上剩余倍率
		audioFilterParts.add(String.format("atempo=%.2f", current));

		return String.join(",", audioFilterParts);
	}

	public static String getAmixFilter(final float bgmVolume, final boolean loop) {
		Validate.isTrue(bgmVolume > 0, "bgmVolume 必须大于0");

		return String.format("[1:a]volume=%.2f[bg];[0:a][bg]amix=inputs=2:duration=%s[a]", bgmVolume, loop ? "loop" : "first");
	}

	public static <T extends Media> void concat(final Collection<MediaResource> resources, final Object output, final T outputMedia,
	                                            final boolean onlyAudio) throws IOException {
		Validate.notEmpty(resources, "resources 不可为空");
		Validate.notNull(outputMedia, "outputMedia 不可为 null");
		if (onlyAudio) {
			Validate.isTrue(resources.stream().allMatch(MediaResource::isAudio), "存在非音频类型 MediaResource");
		} else {
			Validate.isTrue(resources.stream().allMatch(MediaResource::isVideo), "存在非视频类型 MediaResource");
		}

		try (FFmpegFrameRecorder recorder = openFrameRecorder(output)) {
			initRecorder(recorder, null, outputMedia);

			for (MediaResource resource : resources) {
				if (Objects.isNull(resource) || !resource.isAudio()) {
					continue;
				}

				try (FFmpegFrameGrabber grabber = openFrameGrabber(resource)) {
					grabber.start();

					recordFrames(recorder, grabber, null, onlyAudio);
				}
			}
		}
	}

	public static <T extends Media> void applyFilter(final Collection<MediaResource> resources, final Object output,
	                                                 final T outputMedia, final String videoFilters, final String audioFilters,
	                                                 final boolean onlyAudio) throws IOException {
		Validate.notEmpty(resources, "resources 不可为空");
		if (onlyAudio) {
			Validate.notBlank(audioFilters, "audioFilters 不可为空");
			Validate.isTrue(resources.stream().allMatch(MediaResource::isAudio), "存在非音频类型 MediaResource");
		} else {
			Validate.notBlank(videoFilters, "videoFilters 不可为空");
			Validate.isTrue(resources.stream().allMatch(MediaResource::isVideo), "存在非视频类型 MediaResource");
		}

		List<FFmpegFrameGrabber> grabbers = new ArrayList<>(resources.size());
		for (MediaResource resource : resources) {
			FFmpegFrameGrabber grabber = openFrameGrabber(resource);
			grabber.start();

			grabbers.add(grabber);
		}

		applyFilter(grabbers, output, outputMedia, videoFilters, audioFilters, onlyAudio);

		for (FFmpegFrameGrabber grabber : grabbers) {
			grabber.close();
		}
	}

	public static <T extends Media> void applyFilter(final List<FFmpegFrameGrabber> grabbers, final Object output,
	                                                 final T outputMedia, final String videoFilters, final String audioFilters,
	                                                 final boolean onlyAudio) throws IOException {
		Validate.notEmpty(grabbers, "grabbers 不可为空");

		try (FFmpegFrameFilter filter = new FFmpegFrameFilter(videoFilters, audioFilters, 0, 0, 0);
		     FFmpegFrameRecorder recorder = openFrameRecorder(output)) {

			boolean isInit = false;
			boolean hasFrame;
			do {
				hasFrame = false;

				for (int i = 0; i < grabbers.size(); i++) {
					FFmpegFrameGrabber grabber = grabbers.get(i);
					if (Objects.isNull(grabber.getFormatContext()) || grabber.getFormatContext().isNull()) {
						grabber.start();
					}

					if (!isInit) {
						initFilter(filter, grabber, outputMedia, grabbers.size());
						initRecorder(recorder, grabber, outputMedia);

						isInit = true;
					}

					try (Frame frame = onlyAudio ? grabber.grabSamples() : grabber.grabFrame()) {
						if (Objects.nonNull(frame)) {
							filter.push(i, frame);
							hasFrame = true;
						}
					}
				}
			} while (hasFrame);

			while (true) {
				try (Frame filterFrame = onlyAudio ? filter.pullSamples() : filter.pull()) {
					if (Objects.isNull(filterFrame)) {
						break;
					}

					recorder.record(filterFrame);
				}
			}
		}
	}

	public static <T extends Media> void cut(final MediaResource resource, final Object output, final T outputMedia,
	                                         final Duration start, final Duration end, final boolean onlyAudio) throws IOException {
		Validate.notNull(start, "start 不可为 null");
		if (Objects.nonNull(end)) {
			Validate.isTrue(end.compareTo(start) > 0, "end 必须大于 start");
		}
		Validate.notNull(resource, "resource 不可为 null");
		if (onlyAudio) {
			Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		} else {
			Validate.isTrue(resource.isVideo(), "不是视频类型 MediaResource");
		}

		try (FFmpegFrameGrabber grabber = openFrameGrabber(resource);
		     FFmpegFrameRecorder recorder = openFrameRecorder(output)) {
			grabber.start();

			long lengthInTime = grabber.getLengthInTime();

			long startTimestamp = start.toNanos() / 1000;
			grabber.setTimestamp(startTimestamp);

			Validate.isTrue(startTimestamp < lengthInTime, "start 必须小于音频总时长");
			long endTimestamp = Objects.isNull(end) ? lengthInTime : Math.min(end.toNanos() / 1000, lengthInTime);

			initRecorder(recorder, grabber, outputMedia);
			recordFrames(recorder, grabber, endTimestamp, onlyAudio);
		}
	}

	public static <T extends Media> void transcode(final MediaResource resource, final Object output,
	                                               final T outputMedia, final boolean onlyAudio) throws IOException {
		Validate.notNull(outputMedia, "outputMedia 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		if (onlyAudio) {
			Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		} else {
			Validate.isTrue(resource.isVideo(), "不是视频类型 MediaResource");
		}

		try (FFmpegFrameGrabber grabber = openFrameGrabber(resource);
		     FFmpegFrameRecorder recorder = openFrameRecorder(output)) {
			grabber.start();

			initRecorder(recorder, grabber, outputMedia);
			recordFrames(recorder, grabber, null, onlyAudio);
		}
	}

	public static FFmpegFrameGrabber openFrameGrabber(final MediaResource resource) throws IOException {
		if (resource.isFile()) {
			return new FFmpegFrameGrabber(resource.getFile());
		} else {
			return new FFmpegFrameGrabber(resource.getInputStream());
		}
	}

	private static void recordFrames(final FFmpegFrameRecorder recorder, final FFmpegFrameGrabber grabber,
	                                 final Long endTimestamp, final boolean onlyAudio) throws FFmpegFrameRecorder.Exception, FrameGrabber.Exception {
		while (true) {
			try (Frame frame = onlyAudio ? grabber.grabSamples() : grabber.grabFrame()) {
				if (Objects.isNull(frame)) {
					break;
				}
				if (Objects.nonNull(endTimestamp) && grabber.getTimestamp() > endTimestamp) {
					break;
				}
				recorder.record(frame);
			}
		}
	}

	private static <T extends Media> void initRecorder(final FFmpegFrameRecorder recorder, final FFmpegFrameGrabber grabber,
	                                                   final T outputMedia) throws FFmpegFrameRecorder.Exception {
		if (Objects.nonNull(outputMedia)) {
			outputMedia.initRecorder(recorder);
		} else if (Objects.nonNull(grabber)) {
			recorder.setFormat(grabber.getFormat());

			if (grabber.hasVideo()) {
				recorder.setVideoCodec(grabber.getVideoCodec());
				recorder.setVideoCodecName(grabber.getVideoCodecName());
				recorder.setFrameRate(grabber.getFrameRate());
				recorder.setVideoBitrate(grabber.getVideoBitrate());
				recorder.setImageWidth(grabber.getImageWidth());
				recorder.setImageHeight(grabber.getImageHeight());
				recorder.setVideoMetadata(grabber.getVideoMetadata());
			}

			if (grabber.hasAudio()) {
				recorder.setAudioCodec(grabber.getAudioCodec());
				recorder.setAudioCodecName(grabber.getAudioCodecName());
				recorder.setSampleRate(grabber.getSampleRate());
				recorder.setAudioBitrate(grabber.getAudioBitrate());
				recorder.setAudioChannels(grabber.getAudioChannels());
				recorder.setAudioMetadata(grabber.getAudioMetadata());
			}
		}

		recorder.start();
	}

	private static <T extends Media> void initFilter(final FFmpegFrameFilter filter, final FFmpegFrameGrabber grabber,
	                                                 final T outputMedia, final int inputs) throws FFmpegFrameFilter.Exception {
		filter.setAudioInputs(inputs);
		filter.setVideoInputs(inputs);

		if (Objects.nonNull(outputMedia)) {
			outputMedia.initFilter(filter);
		} else if (Objects.nonNull(grabber)) {
			if (grabber.hasVideo()) {
				filter.setFrameRate(grabber.getFrameRate());
				filter.setImageWidth(grabber.getImageWidth());
				filter.setImageHeight(grabber.getImageHeight());
			}

			if (grabber.hasAudio()) {
				filter.setSampleRate(grabber.getSampleRate());
				filter.setAudioChannels(grabber.getAudioChannels());
			}
		}

		filter.start();
	}

	private static FFmpegFrameRecorder openFrameRecorder(final Object output) {
		if (output instanceof File outputFile) {
			return new FFmpegFrameRecorder(outputFile, 0);
		} else if (output instanceof OutputStream outputStream) {
			return new FFmpegFrameRecorder(outputStream, 0);
		}
		return null;
	}
}
