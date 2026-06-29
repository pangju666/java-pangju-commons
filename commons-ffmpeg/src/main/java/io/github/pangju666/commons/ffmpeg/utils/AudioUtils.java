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
import io.github.pangju666.commons.ffmpeg.model.Audio;
import io.github.pangju666.commons.ffmpeg.model.Media;
import io.github.pangju666.commons.ffmpeg.model.MediaResource;
import io.github.pangju666.commons.io.utils.FileUtils;
import org.apache.commons.lang3.Validate;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class AudioUtils {
	static final float DEFAULT_BGM_WEIGHT = 0.4f;

	protected AudioUtils() {
	}

	public static void transcode(final MediaResource resource, final File outputFile, final Audio outputAudio) throws IOException {
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			FFmpegUtils.transcode(grabber, recorder, outputAudio, FrameType.AUDIO,
				false);
		}
	}

	public static void transcode(final MediaResource resource, final OutputStream outputStream, final Audio outputAudio) throws IOException {
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			FFmpegUtils.transcode(grabber, recorder, outputAudio, FrameType.AUDIO,
				false);
		}
	}

	public static void cut(final MediaResource resource, final File outputFile, final Duration duration) throws IOException {
		cut(resource, outputFile, (Audio) null, duration);
	}

	public static void cut(final MediaResource resource, final OutputStream outputStream, final Duration duration) throws IOException {
		cut(resource, outputStream, (Audio) null, duration);
	}

	public static void cut(final MediaResource resource, final File outputFile, final Duration start, final Duration end) throws IOException {
		cut(resource, outputFile, null, start, end);
	}

	public static void cut(final MediaResource resource, final OutputStream outputStream, final Duration start,
	                       final Duration end) throws IOException {
		cut(resource, outputStream, null, start, end);
	}

	public static void cut(final MediaResource resource, final File outputFile, final Audio outputAudio,
	                       final Duration duration) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doCut(resource, recorder, outputAudio, duration);
		}
	}

	public static void cut(final MediaResource resource, final OutputStream outputStream, final Audio outputAudio,
	                       final Duration duration) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doCut(resource, recorder, outputAudio, duration);
		}
	}

	public static void cut(final MediaResource resource, final File outputFile, final Audio outputAudio,
	                       final Duration start, final Duration end) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doCut(resource, recorder, outputAudio, start, end);
		}
	}

	public static void cut(final MediaResource resource, final OutputStream outputStream, final Audio outputAudio,
	                       final Duration start, final Duration end) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doCut(resource, recorder, outputAudio, start, end);
		}
	}

	public static void concat(final Collection<MediaResource> resources, final File outputFile) throws IOException {
		concat(resources, outputFile, null);
	}

	public static void concat(final Collection<MediaResource> resources, final OutputStream outputStream) throws IOException {
		concat(resources, outputStream, null);
	}

	public static void concat(final Collection<MediaResource> resources, final File outputFile, final Audio outputAudio) throws IOException {
		Validate.notEmpty(resources, "resources 不可为空");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.isTrue(resources.stream().allMatch(resource ->
			Objects.nonNull(resource) && resource.isAudio()), "存在非音频类型 MediaResource");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			FFmpegUtils.concatByResource(resources, recorder, outputAudio, FrameType.AUDIO,
				false);
		}
	}

	public static void concat(final Collection<MediaResource> resources, final OutputStream outputStream,
	                          final Audio outputAudio) throws IOException {
		Validate.notEmpty(resources, "resources 不可为空");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.isTrue(resources.stream().allMatch(resource ->
			Objects.nonNull(resource) && resource.isAudio()), "存在非音频类型 MediaResource");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			FFmpegUtils.concatByResource(resources, recorder, outputAudio, FrameType.AUDIO,
				false);
		}
	}

	public static void addBgm(final MediaResource mainResource, final MediaResource bgmResource,
	                          final File outputFile) throws IOException {
		addBgm(mainResource, bgmResource, outputFile, null, DEFAULT_BGM_WEIGHT);
	}

	public static void addBgm(final MediaResource mainResource, final MediaResource bgmResource,
	                          final OutputStream outputStream) throws IOException {
		addBgm(mainResource, bgmResource, outputStream, null, DEFAULT_BGM_WEIGHT);
	}

	public static void addBgm(final MediaResource mainResource, final MediaResource bgmResource,
	                          final File outputFile, final Audio outputAudio) throws IOException {
		addBgm(mainResource, bgmResource, outputFile, outputAudio, DEFAULT_BGM_WEIGHT);
	}

	public static void addBgm(final MediaResource mainResource, final MediaResource bgmResource,
	                          final OutputStream outputStream, final Audio outputAudio) throws IOException {
		addBgm(mainResource, bgmResource, outputStream, outputAudio, DEFAULT_BGM_WEIGHT);
	}

	public static void addBgm(final MediaResource mainResource, final MediaResource bgmResource,
	                          final File outputFile, final float bgmWeight) throws IOException {
		addBgm(mainResource, bgmResource, outputFile, null, bgmWeight);
	}

	public static void addBgm(final MediaResource mainResource, final MediaResource bgmResource,
	                          final OutputStream outputStream, final float bgmWeight) throws IOException {
		addBgm(mainResource, bgmResource, outputStream, null, bgmWeight);
	}

	public static void addBgm(final MediaResource mainResource, final MediaResource bgmResource,
	                          final File outputFile, final Audio outputAudio, final float bgmWeight) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doAddBgm(mainResource, bgmResource, recorder, outputAudio, bgmWeight);
		}
	}

	public static void addBgm(final MediaResource mainResource, final MediaResource bgmResource,
	                          final OutputStream outputStream, final Audio outputAudio, final float bgmWeight) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doAddBgm(mainResource, bgmResource, recorder, outputAudio, bgmWeight);
		}
	}

	static <T extends Media> void addBgm(final FFmpegFrameGrabber mainGrabber, final FFmpegFrameGrabber bgmGrabber,
	                                     final FFmpegFrameRecorder recorder, final T outputMedia, final float bgmWeight,
	                                     final boolean recorderStarted) throws IOException {
		Validate.notNull(mainGrabber, "mainGrabber 不可为 null");
		Validate.notNull(bgmGrabber, "bgmGrabber 不可为 null");
		Validate.isTrue(bgmWeight > 0, "bgmWeight 必须大于0");
		Validate.notNull(recorder, "recorder 不可为 null");

		if (FFmpegUtils.isNotStarted(mainGrabber)) {
			mainGrabber.start();
		}
		Validate.isTrue(mainGrabber.hasAudio(), "mainGrabber 不存在音频流");

		if (FFmpegUtils.isNotStarted(bgmGrabber)) {
			bgmGrabber.start();
		}
		Validate.isTrue(bgmGrabber.hasAudio(), "bgmGrabber 不存在音频流");

		long mainLengthInTime = mainGrabber.getLengthInTime();
		long bgmLengthInTime = bgmGrabber.getLengthInTime();

		FilterBuilder builder = FilterBuilder.audio()
			.addInput();
		if (mainLengthInTime > bgmLengthInTime) {
			builder.addInput("bgm", FFmpegUtils.getAloopInfiniteFilter(bgmGrabber.getSampleRate(),
				bgmGrabber.getLengthInTime()));
		} else {
			builder.addInput();
		}
		builder.addFilter(FFmpegUtils.getAmixFilter(2, 1.0f, bgmWeight));

		FFmpegUtils.applyFilter(List.of(mainGrabber, bgmGrabber), recorder, outputMedia,
			null, builder.build(), FrameType.AUDIO, recorderStarted);
	}

	public static void adjustSpeed(final MediaResource resource, final File outputFile, final float speed) throws IOException {
		adjustSpeed(resource, outputFile, speed, null);
	}

	public static void adjustSpeed(final MediaResource resource, final OutputStream outputStream, final float speed) throws IOException {
		adjustSpeed(resource, outputStream, speed, null);
	}

	public static void adjustSpeed(final MediaResource resource, final File outputFile, final float speed,
	                               final Audio outputAudio) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doAdjustSpeed(resource, recorder, speed, outputAudio);
		}
	}


	public static void adjustSpeed(final MediaResource resource, final OutputStream outputStream, final float speed,
	                               final Audio outputAudio) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doAdjustSpeed(resource, recorder, speed, outputAudio);
		}
	}

	public static void adjustVolume(final MediaResource resource, final File outputFile, final double db) throws IOException {
		adjustVolume(resource, outputFile, db, null);
	}

	public static void adjustVolume(final MediaResource resource, final OutputStream outputStream, final double db) throws IOException {
		adjustVolume(resource, outputStream, db, null);
	}

	public static void adjustVolume(final MediaResource resource, final File outputFile, final double db,
	                                final Audio outputAudio) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doAdjustVolume(resource, recorder, db, outputAudio);
		}
	}


	public static void adjustVolume(final MediaResource resource, final OutputStream outputStream, final double db,
	                                final Audio outputAudio) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doAdjustVolume(resource, recorder, db, outputAudio);
		}
	}

	protected static void doAdjustSpeed(final MediaResource resource, final FFmpegFrameRecorder recorder,
	                                    final float speed, final Audio outputAudio) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		Validate.notNull(recorder, "recorder 不可为 null");

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource)) {
			FFmpegUtils.applyFilter(grabber, recorder, outputAudio, null,
				FFmpegUtils.getAtempoFilter(speed), FrameType.AUDIO, false);
		}
	}

	protected static void doCut(final MediaResource resource, final FFmpegFrameRecorder recorder,
	                            final Audio outputAudio, final Duration duration) throws IOException {
		Validate.notNull(duration, "duration 不可为null");
		Validate.isTrue(!duration.isZero() && !duration.isNegative(), "duration 必须大于 0");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		Validate.notNull(recorder, "recorder 不可为 null");

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource)) {
			grabber.start();

			long srcLengthInTime = grabber.getLengthInTime();
			long targetLengthInTime = Math.min(duration.toNanos() / 1000, srcLengthInTime);
			if (targetLengthInTime == srcLengthInTime) {
				FFmpegUtils.startRecorder(recorder, grabber, outputAudio, FrameType.AUDIO);
				FFmpegUtils.recordFrames(recorder, grabber, FrameType.AUDIO);
			} else {
				String audioFilters = FilterBuilder.single()
					.addFilter(FFmpegUtils.getAtrimFilter(targetLengthInTime))
					.build();
				FFmpegUtils.applyFilter(grabber, recorder, outputAudio, null, audioFilters,
					FrameType.AUDIO, false);
			}
		}
	}

	protected static void doCut(final MediaResource resource, final FFmpegFrameRecorder recorder,
	                            final Audio outputAudio, final Duration start, final Duration end) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		Validate.notNull(start, "start 不可为null");
		Validate.isTrue(!start.isZero() && !start.isNegative(), "start 必须大于 0");
		Validate.notNull(end, "end 不可为null");
		Validate.isTrue(!end.isZero() && !end.isNegative(), "end 必须大于 0");

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource)) {
			grabber.start();

			long lengthInTime = grabber.getLengthInTime();
			long startTimestamp = Math.min(start.toNanos() / 1000, lengthInTime);
			long endTimestamp = Math.min(end.toNanos() / 1000, lengthInTime);
			if (startTimestamp == endTimestamp) {
				FFmpegUtils.startRecorder(recorder, grabber, outputAudio, FrameType.AUDIO);
				FFmpegUtils.recordFrames(recorder, grabber, FrameType.AUDIO);
			} else {
				String audioFilters = FilterBuilder.single()
					.addFilter(FFmpegUtils.getAtrimFilter(startTimestamp, endTimestamp))
					.build();
				FFmpegUtils.applyFilter(grabber, recorder, outputAudio, null, audioFilters,
					FrameType.AUDIO, false);
			}
		}
	}

	protected static void doAdjustVolume(final MediaResource resource, final FFmpegFrameRecorder recorder,
	                                     final double db, final Audio outputAudio) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		Validate.notNull(recorder, "recorder 不可为 null");

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource)) {
			FFmpegUtils.applyFilter(grabber, recorder, outputAudio, null,
				FFmpegUtils.getVolumeFilter(db), FrameType.AUDIO, false);
		}
	}

	protected static void doAddBgm(final MediaResource mainResource, final MediaResource bgmResource,
	                               final FFmpegFrameRecorder recorder, final Audio outputAudio, final float bgmWeight) throws IOException {
		Validate.notNull(mainResource, "mainResource 不可为 null");
		Validate.isTrue(mainResource.isAudio(), "不是音频类型 MediaResource");
		Validate.notNull(bgmResource, "bgmResource 不可为 null");
		Validate.isTrue(bgmResource.isAudio(), "不是音频类型 MediaResource");

		try (FFmpegFrameGrabber mainGrabber = FFmpegUtils.openFrameGrabber(mainResource);
		     FFmpegFrameGrabber bgmGrabber = FFmpegUtils.openFrameGrabber(bgmResource)) {
			addBgm(mainGrabber, bgmGrabber, recorder, outputAudio, bgmWeight, false);
		}
	}
}
