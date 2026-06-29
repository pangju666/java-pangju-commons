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

package io.github.pangju666.commons.ffmpeg.utils;

import io.github.pangju666.commons.ffmpeg.enums.FrameType;
import io.github.pangju666.commons.ffmpeg.lang.FFmpegConstants;
import io.github.pangju666.commons.ffmpeg.model.Audio;
import io.github.pangju666.commons.ffmpeg.model.Media;
import io.github.pangju666.commons.ffmpeg.model.MediaResource;
import io.github.pangju666.commons.ffmpeg.model.Video;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.bytedeco.javacv.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.ObjLongConsumer;

public class FFmpegUtils {
	protected FFmpegUtils() {
	}

	public static String getVolumeFilter(final Number db) {
		return getVolumeFilter(db, VolumePrecision.FLOAT);
	}

	public static String getVolumeFilter(final Number db, final VolumePrecision precision) {
		Validate.notNull(precision, "duration 不可为 null");
		Validate.notNull(db, "db 不可为 null");

		return switch (precision) {
			case FIXED -> {
				int volumeVal = db.intValue();
				yield String.format("volume=volume=%s%ddB:precision=fixed", volumeVal > 0 ? "+" : "", volumeVal);
			}
			case FLOAT -> {
				float volumeVal = db.floatValue();
				yield String.format("volume=volume=%s%.4fdB:precision=float", volumeVal > 0 ? "+" : "", volumeVal);
			}
			case DOUBLE -> {
				double volumeVal = db.doubleValue();
				yield String.format("volume=volume=%s%.4fdB:precision=double", volumeVal > 0 ? "+" : "", volumeVal);
			}
		};
	}

	public static String getAmixFilter(final int inputs, final float... weights) {
		return getAmixFilter(inputs, 0, AmixDuration.FIRST, weights);
	}

	public static String getAmixFilter(final int inputs, final int dropoutTransition, final float... weights) {
		return getAmixFilter(inputs, dropoutTransition, AmixDuration.FIRST, weights);
	}

	public static String getAmixFilter(final int inputs, final int dropoutTransition, final AmixDuration duration,
	                                   final float... weights) {
		Validate.isTrue(inputs >= 2, "inputs 必须大于等于 2");
		Validate.isTrue(dropoutTransition >= 0, "dropoutTransition 必须大于等于 0");
		Validate.notNull(duration, "duration 不可为 null");

		String audioFilters = String.format("amix=inputs=%d:dropout_transition=%d:duration=%s",
			inputs, dropoutTransition, duration.value);

		List<Float> weightList = new ArrayList<>(inputs);
		for (int i = 0; i < weights.length; i++) {
			if (i == inputs) {
				break;
			}
			weightList.add(Math.max(weights[i], 0));
		}
		for (int i = 0; i < inputs - weights.length; i++) {
			weightList.add(FFmpegConstants.FILTER_AMIX_DEFAULT_WEIGHT);
		}

		if (!weightList.isEmpty()) {
			audioFilters += (FFmpegConstants.FILTER_ARG_SEPARATOR + "weights='" + StringUtils.join(
				weightList, StringUtils.SPACE) + "'");
		}

		return audioFilters;
	}

	public static String getAloopInfiniteFilter(final int sampleRate, final long lengthInTime) {
		Validate.isTrue(sampleRate > 0, "sampleRate 必须大于 0");
		Validate.isTrue(lengthInTime > 0, "lengthInTime 必须大于 0");

		return getAloopFilter(-1, sampleRate * (TimeUnit.MICROSECONDS.toSeconds(lengthInTime)));
	}

	public static String getAloopFilter(final int loop, final long size) {
		Validate.isTrue(loop >= -1, "loop 必须大于等于 -1");
		Validate.isTrue(size >= 0, "size 必须大于等于 0");

		return String.format("aloop=loop=%d:size=%d", loop, size);
	}

	public static String getAtrimFilter(final long lengthInTime) {
		Validate.isTrue(lengthInTime > 0, "lengthInTime 必须大于 0");

		return String.format("atrim=duration=%dus", lengthInTime);
	}

	public static String getAtrimFilter(final long startTimestamp, final long endTimestamp) {
		Validate.isTrue(startTimestamp > 0, "startTimestamp 必须大于 0");
		Validate.isTrue(endTimestamp > 0, "endTimestamp 必须大于 0");
		Validate.isTrue(endTimestamp > startTimestamp, "endTimestamp 必须大于 startTimestamp");

		return String.format("atrim=start=%dus:end=%dus", startTimestamp, endTimestamp);
	}

	public static String getTrimFilter(final long lengthInTime) {
		Validate.isTrue(lengthInTime > 0, "lengthInTime 必须大于 0");

		return String.format("trim=duration=%dus", lengthInTime);
	}

	public static String getTrimFilter(final long startTimestamp, final long endTimestamp) {
		Validate.isTrue(startTimestamp > 0, "startTimestamp 必须大于 0");
		Validate.isTrue(endTimestamp > 0, "endTimestamp 必须大于 0");
		Validate.isTrue(endTimestamp > startTimestamp, "endTimestamp 必须大于 startTimestamp");

		return String.format("trim=start=%dus:end=%dus", startTimestamp, endTimestamp);
	}

	public static String getAtempoFilter(final float speed) {
		Validate.isTrue(speed >= 0.5, "speed 必须大于等于 0.5");
		Validate.isTrue(speed <= 100, "speed 必须小于等于 100");

		return String.format("atempo=%.3f", speed);
	}

	public static String getCropFilter(final int x, final int y, final int width, final int height) {
		Validate.isTrue(x >= 0, "x 必须大于等于 0");
		Validate.isTrue(y >= 0, "y 必须大于等于 0");
		Validate.isTrue(width > 0, "width 必须大于 0");
		Validate.isTrue(height > 0, "height 必须大于 0");

		return String.format("crop=%d:%d:%d:%d", width, height, x, y);
	}

	public static String getCropFilter(final String x, final String y, final String width, final String height) {
		Validate.notBlank(width, "width 不可为空");
		Validate.notBlank(height, "height 不可为空");

		return String.format("crop=%s:%s:%s:%s", width, height, StringUtils.defaultString(x),
			StringUtils.defaultString(y));
	}

	public static String getSetptsWithFpsFilter(final float speed, final double frameRate) {
		Validate.isTrue(speed > 0, "speed 必须大于 0");

		// setpts 系数 = 1 / 速度
		float ptsScale = 1.0f / speed;
		return String.format("setpts=%.6f*PTS,fps=%.2f", ptsScale, frameRate);
	}

	public static <T extends Media> void applyFilter(final FFmpegFrameGrabber grabber, final FFmpegFrameRecorder recorder,
	                                                 final T outputMedia, final String videoFilters,
	                                                 final String audioFilters, final FrameType frameType,
	                                                 final boolean recorderStarted) throws IOException {
		applyFilter(Collections.singletonList(grabber), recorder, null, outputMedia, videoFilters,
			audioFilters, frameType, recorderStarted);
	}

	public static <T extends Media> void applyFilter(final FFmpegFrameGrabber grabber,
	                                                 final FFmpegFrameRecorder recorder, final T filterMedia,
	                                                 final T outputMedia, final String videoFilters,
	                                                 final String audioFilters, final FrameType frameType,
	                                                 final boolean recorderStarted) throws IOException {
		applyFilter(Collections.singletonList(grabber), recorder, filterMedia, outputMedia, videoFilters,
			audioFilters, frameType, recorderStarted);
	}

	public static <T extends Media> void applyFilter(final List<FFmpegFrameGrabber> grabbers,
	                                                 final FFmpegFrameRecorder recorder, final T outputMedia,
	                                                 final String videoFilters, final String audioFilters,
	                                                 final FrameType frameType, final boolean recorderStarted) throws IOException {
		applyFilter(grabbers, recorder, null, outputMedia, videoFilters, audioFilters, frameType, recorderStarted);
	}

	public static <T extends Media> void applyFilter(final List<FFmpegFrameGrabber> grabbers,
	                                                 final FFmpegFrameRecorder recorder, final T filterMedia,
	                                                 final T outputMedia, final String videoFilters,
	                                                 final String audioFilters, final FrameType frameType,
	                                                 final boolean recorderStarted) throws IOException {
		Validate.notEmpty(grabbers, "grabbers 不可为空");
		Validate.isTrue(grabbers.stream().allMatch(Objects::nonNull), "集合中存在为 null的 grabber");
		Validate.notNull(frameType, "frameMode 不可为 null");
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.isTrue(!StringUtils.isAllBlank(audioFilters, videoFilters),
			"audioFilters 或 videoFilters 至少需要一个不为空");

		int audioInputs = 0;
		int videoInputs = 0;
		for (FFmpegFrameGrabber grabber : grabbers) {
			if (isNotStarted(grabber)) {
				grabber.start();
			}

			if (grabber.hasAudio() && StringUtils.isNotBlank(audioFilters)) {
				++audioInputs;
			}
			if (grabber.hasVideo() && StringUtils.isNotBlank(videoFilters)) {
				++videoInputs;
			}
		}

		try (FFmpegFrameFilter filter = new FFmpegFrameFilter(videoFilters, audioFilters, 0, 0, 0)) {
			boolean hasFrame;

			startFilter(filter, grabbers.get(0), filterMedia, audioInputs, videoInputs);
			if (!recorderStarted) {
				startRecorder(recorder, grabbers.get(0), outputMedia, frameType);
			}

			if (StringUtils.isNoneBlank(videoFilters, audioFilters)) {
				do {
					hasFrame = false;

					for (int i = 0; i < grabbers.size(); i++) {
						FFmpegFrameGrabber grabber = grabbers.get(i);

						try (Frame frame = FrameType.ALL.grabFrame(grabber)) {
							if (Objects.nonNull(frame)) {
								filter.push(i, frame);
								hasFrame = true;
							}
						}
					}
				} while (hasFrame);
			} else if (StringUtils.isBlank(videoFilters)) {
				do {
					hasFrame = false;

					for (int i = 0; i < grabbers.size(); i++) {
						FFmpegFrameGrabber grabber = grabbers.get(i);

						if (grabber.hasVideo() && frameType != FrameType.AUDIO) {
							try (Frame videoFrame = FrameType.VIDEO.grabFrame(grabber)) {
								if (Objects.nonNull(videoFrame)) {
									recorder.record(videoFrame);
									hasFrame = true;
								}
							}
						}

						if (grabber.hasAudio()) {
							try (Frame audioFrame = FrameType.AUDIO.grabFrame(grabber)) {
								if (Objects.nonNull(audioFrame)) {
									filter.push(i, audioFrame);
									hasFrame = true;
								}
							}
						}
					}
				} while (hasFrame);
			} else if (StringUtils.isBlank(audioFilters)) {
				do {
					hasFrame = false;

					for (int i = 0; i < grabbers.size(); i++) {
						FFmpegFrameGrabber grabber = grabbers.get(i);

						if (grabber.hasVideo()) {
							try (Frame videoFrame = FrameType.VIDEO.grabFrame(grabber)) {
								if (Objects.nonNull(videoFrame)) {
									filter.push(i, videoFrame);
									hasFrame = true;
								}
							}
						}

						if (grabber.hasAudio() && frameType == FrameType.ALL) {
							try (Frame audioFrame = FrameType.AUDIO.grabFrame(grabber)) {
								if (Objects.nonNull(audioFrame)) {
									recorder.record(audioFrame);
									hasFrame = true;
								}
							}
						}
					}
				} while (hasFrame);
			}

			while (true) {
				try (Frame filterFrame = frameType.pullFrame(filter)) {
					if (Objects.isNull(filterFrame)) {
						break;
					}

					recorder.record(filterFrame);
				}
			}
		}
	}

	public static <T extends Media> void concatByResource(final Collection<MediaResource> resources,
	                                                      final FFmpegFrameRecorder recorder, final T outputMedia,
	                                                      final FrameType frameType, final boolean recorderStarted) throws IOException {
		Validate.notEmpty(resources, "resources 不可为空");
		Validate.isTrue(resources.stream().allMatch(Objects::nonNull), "集合中存在为 null的 grabber");
		Validate.notNull(frameType, "frameMode 不可为 null");
		Validate.notNull(recorder, "recorder 不可为 null");

		boolean started = recorderStarted;
		for (MediaResource resource : resources) {
			try (FFmpegFrameGrabber grabber = openFrameGrabber(resource)) {
				grabber.start();

				if (!started) {
					startRecorder(recorder, grabber, outputMedia, frameType);

					started = true;
				}

				recordFrames(recorder, grabber, frameType);
			}
		}
	}

	public static <T extends Media> void concat(final Collection<FFmpegFrameGrabber> grabbers,
	                                            final FFmpegFrameRecorder recorder, final T outputMedia,
	                                            final FrameType frameType, final boolean recorderStarted) throws IOException {
		Validate.notEmpty(grabbers, "resources 不可为空");
		Validate.isTrue(grabbers.stream().allMatch(Objects::nonNull), "集合中存在为 null的 grabber");
		Validate.notNull(frameType, "frameMode 不可为 null");
		Validate.notNull(recorder, "recorder 不可为 null");

		boolean started = recorderStarted;
		for (FFmpegFrameGrabber grabber : grabbers) {
			if (isNotStarted(grabber)) {
				grabber.start();
			}

			if (!started) {
				startRecorder(recorder, grabber, outputMedia, frameType);

				started = true;
			}

			recordFrames(recorder, grabber, frameType);
		}
	}

	public static <T extends Media> void transcode(final FFmpegFrameGrabber grabber, final FFmpegFrameRecorder recorder,
	                                               final T outputMedia, final FrameType frameType,
	                                               final boolean recorderStarted) throws IOException {
		Validate.notNull(grabber, "grabber 不可为 null");
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.notNull(frameType, "frameMode 不可为 null");

		if (isNotStarted(grabber)) {
			grabber.start();
		}

		if (!recorderStarted) {
			startRecorder(recorder, grabber, outputMedia, frameType);
		}
		recordFrames(recorder, grabber, frameType);
	}

	public static FFmpegFrameGrabber openFrameGrabber(final MediaResource resource) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");

		if (resource.isFile()) {
			return new FFmpegFrameGrabber(resource.getFile());
		} else {
			return new FFmpegFrameGrabber(resource.getInputStream());
		}
	}

	public static void recordFrames(final FFmpegFrameRecorder recorder, final FFmpegFrameGrabber grabber,
	                                final FrameType frameType) throws FFmpegFrameRecorder.Exception, FrameGrabber.Exception {
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.notNull(grabber, "grabber 不可为 null");
		Validate.notNull(frameType, "frameMode 不可为 null");

		while (true) {
			try (Frame frame = frameType.grabFrame(grabber)) {
				if (Objects.isNull(frame)) {
					break;
				}
				recorder.record(frame);
			}
		}
	}

	public static <T extends Media> void startRecorder(final FFmpegFrameRecorder recorder, final FFmpegFrameGrabber grabber,
	                                                   final T outputMedia, final FrameType frameType) throws FFmpegFrameRecorder.Exception {
		startRecorder(recorder, grabber, grabber, outputMedia, frameType);
	}

	public static <T extends Media> void startRecorder(final FFmpegFrameRecorder recorder, final FFmpegFrameGrabber videoGrabber,
	                                                   final FFmpegFrameGrabber audioGrabber, final T outputMedia,
	                                                   final FrameType frameType) throws FFmpegFrameRecorder.Exception {
		Validate.notNull(recorder, "recorder 不可为 null");

		if (Objects.nonNull(outputMedia)) {
			if (outputMedia instanceof Audio audio && (frameType == FrameType.AUDIO || frameType == FrameType.ALL)) {
				recorder.setFormat(audio.getFormat());
				recorder.setSampleRate(audio.getSampleRate());
				recorder.setAudioCodec(audio.getCodecId());
				recorder.setAudioCodecName(audio.getCodecName());
				recorder.setAudioBitrate(audio.getBitrate());
				recorder.setAudioChannels(audio.getChannels());
				recorder.setAudioMetadata(new HashMap<>(audio.getMetadata()));
			} else if (outputMedia instanceof Video video) {
				if (frameType != FrameType.AUDIO) {
					recorder.setFrameRate(video.getFrameRate());
					recorder.setVideoBitrate(video.getBitrate());
					recorder.setVideoCodec(video.getCodecId());
					recorder.setVideoCodecName(video.getCodecName());
					recorder.setImageWidth(video.getWidth());
					recorder.setImageHeight(video.getHeight());
					recorder.setVideoMetadata(new HashMap<>(video.getMetadata()));
				}

				if (Objects.nonNull(video.getAudio()) && (frameType == FrameType.AUDIO || frameType == FrameType.ALL)) {
					recorder.setSampleRate(video.getAudio().getSampleRate());
					recorder.setAudioCodec(video.getAudio().getCodecId());
					recorder.setAudioCodecName(video.getAudio().getCodecName());
					recorder.setAudioBitrate(video.getAudio().getBitrate());
					recorder.setAudioChannels(video.getAudio().getChannels());
					recorder.setAudioMetadata(new HashMap<>(video.getAudio().getMetadata()));
				}
			}
		} else {
			if (Objects.nonNull(audioGrabber)) {
				recorder.setFormat(audioGrabber.getFormat());

				if ((frameType == FrameType.AUDIO || frameType == FrameType.ALL) && audioGrabber.hasAudio()) {
					recorder.setAudioCodec(audioGrabber.getAudioCodec());
					recorder.setAudioCodecName(audioGrabber.getAudioCodecName());
					recorder.setSampleRate(audioGrabber.getSampleRate());
					recorder.setAudioBitrate(audioGrabber.getAudioBitrate());
					recorder.setAudioChannels(audioGrabber.getAudioChannels());
					recorder.setAudioMetadata(audioGrabber.getAudioMetadata());
				}
			}

			if (Objects.nonNull(videoGrabber)) {
				recorder.setFormat(videoGrabber.getFormat());

				if (frameType != FrameType.AUDIO && videoGrabber.hasVideo()) {
					recorder.setVideoCodec(videoGrabber.getVideoCodec());
					recorder.setVideoCodecName(videoGrabber.getVideoCodecName());
					recorder.setFrameRate(videoGrabber.getFrameRate());
					recorder.setVideoBitrate(videoGrabber.getVideoBitrate());
					recorder.setImageWidth(videoGrabber.getImageWidth());
					recorder.setImageHeight(videoGrabber.getImageHeight());
					recorder.setVideoMetadata(videoGrabber.getVideoMetadata());
				}
			}
		}

		recorder.start();
	}

	public static <T extends Media> void startFilter(final FFmpegFrameFilter filter, final FFmpegFrameGrabber grabber,
	                                                 final T filterMedia, final int audioInputs, final int videoInputs) throws FFmpegFrameFilter.Exception {
		Validate.notNull(filter, "filter 不可为 null");
		Validate.isTrue(audioInputs >= 0, "audioInputs 必须大于等于0");
		Validate.isTrue(videoInputs >= 0, "videoInputs 必须大于等于0");

		filter.setAudioInputs(audioInputs);
		filter.setVideoInputs(videoInputs);

		if (Objects.nonNull(filterMedia)) {
			if (filterMedia instanceof Audio audio && audioInputs > 0) {
				filter.setSampleRate(audio.getSampleRate());
				filter.setAudioChannels(audio.getChannels());
			} else if (filterMedia instanceof Video video) {
				if (videoInputs > 0) {
					filter.setFrameRate(video.getFrameRate());
					filter.setImageWidth(video.getWidth());
					filter.setImageHeight(video.getHeight());
				}

				if (Objects.nonNull(video.getAudio()) && audioInputs > 0) {
					filter.setSampleRate(video.getAudio().getSampleRate());
					filter.setAudioChannels(video.getAudio().getChannels());
				}
			}
		} else if (Objects.nonNull(grabber)) {
			if (grabber.hasVideo() && videoInputs > 0) {
				filter.setFrameRate(grabber.getFrameRate());
				filter.setImageWidth(grabber.getImageWidth());
				filter.setImageHeight(grabber.getImageHeight());
			}

			if (grabber.hasAudio() && audioInputs > 0) {
				filter.setSampleRate(grabber.getSampleRate());
				filter.setAudioChannels(grabber.getAudioChannels());
			}
		}

		filter.start();
	}

	public static boolean isNotStarted(final FFmpegFrameGrabber grabber) {
		return Objects.isNull(grabber.getFormatContext()) || grabber.getFormatContext().isNull();
	}

	public static BufferedImage grabImageAtTimestamp(final FFmpegFrameGrabber grabber, final Duration timestamp) throws FrameGrabber.Exception {
		Validate.notNull(grabber, "grabber 不可为 null");
		Validate.notNull(timestamp, "timestamp 不可为 null");

		if (FFmpegUtils.isNotStarted(grabber)) {
			grabber.start();
		}

		long timestampMicros = timestamp.toNanos() / 1000;
		Validate.isTrue(timestampMicros <= grabber.getLengthInTime(), "timestamp 必须小于等于总时长");
		grabber.setTimestamp(timestampMicros);

		try (Frame frame = FrameType.IMAGE.grabFrame(grabber)) {
			return FFmpegConstants.JAVA_2D_FRAME_CONVERTER.convert(frame);
		}
	}

	public static List<BufferedImage> grabImagePeriodically(final FFmpegFrameGrabber grabber, final long interval,
	                                                        final TimeUnit timeUnit) throws FrameGrabber.Exception {
		Validate.notNull(grabber, "grabber 不可为 null");
		Validate.isTrue(interval > 0, "interval 必须大于 0");
		Validate.notNull(timeUnit, "timeUnit 不可为 null");

		if (FFmpegUtils.isNotStarted(grabber)) {
			grabber.start();
		}

		long currentTimestamp = 0;
		long endTimestamp = grabber.getLengthInTime();
		long intervalMicros = timeUnit.toMicros(interval);
		List<BufferedImage> images = new ArrayList<>(Math.min((int) (endTimestamp / intervalMicros), Integer.MAX_VALUE));
		while (currentTimestamp <= endTimestamp) {
			grabber.setTimestamp(currentTimestamp);

			try (Frame frame = FrameType.KEY_FRAME.grabFrame(grabber)) {
				if (Objects.nonNull(frame) && !Objects.isNull(frame.image)) {
					images.add(FFmpegConstants.JAVA_2D_FRAME_CONVERTER.convert(frame));
				}
			}

			currentTimestamp += intervalMicros;
		}

		return images;
	}

	public static void grabImagePeriodically(final FFmpegFrameGrabber grabber, final long interval, final TimeUnit timeUnit,
	                                         final ObjLongConsumer<BufferedImage> consumer) throws FrameGrabber.Exception {
		Validate.notNull(grabber, "grabber 不可为 null");
		Validate.isTrue(interval > 0, "interval 必须大于 0");
		Validate.notNull(consumer, "consumer 不可为 null");
		Validate.notNull(timeUnit, "timeUnit 不可为 null");

		if (FFmpegUtils.isNotStarted(grabber)) {
			grabber.start();
		}

		long currentTimestamp = 0;
		long endTimestamp = grabber.getLengthInTime();
		long intervalMicros = timeUnit.toMicros(interval);
		while (currentTimestamp <= endTimestamp) {
			grabber.setTimestamp(currentTimestamp);

			try (Frame frame = FrameType.KEY_FRAME.grabFrame(grabber)) {
				if (Objects.nonNull(frame) && !Objects.isNull(frame.image)) {
					BufferedImage image = FFmpegConstants.JAVA_2D_FRAME_CONVERTER.convert(frame);
					consumer.accept(image, currentTimestamp);
					image.flush();
				}
			}

			currentTimestamp += intervalMicros;
		}
	}

	public enum AmixDuration {
		FIRST("first"),
		LONGEST("longest"),
		SHORTEST("shortest");

		public final String value;

		AmixDuration(String value) {
			this.value = value;
		}
	}

	public enum VolumePrecision {
		FIXED,
		FLOAT,
		DOUBLE
	}
}
