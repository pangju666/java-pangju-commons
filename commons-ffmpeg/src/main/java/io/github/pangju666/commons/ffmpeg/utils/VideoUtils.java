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
import io.github.pangju666.commons.ffmpeg.model.*;
import io.github.pangju666.commons.image.lang.ImageConstants;
import io.github.pangju666.commons.io.utils.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.ObjLongConsumer;

/**
 *
 * @since 1.1.0
 */
public class VideoUtils {
	/**
	 * 受保护的构造函数，防止实例化
	 *
	 * @since 1.1.0
	 */
	protected VideoUtils() {
	}

	public static void transcode(final MediaResource resource, final File outputFile, final Video outputVideo) throws IOException {
		Validate.notNull(outputVideo, "outputAudio 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 MediaResource");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			FFmpegUtils.transcode(grabber, recorder, outputVideo, FrameType.ALL,
				false);
		}
	}

	public static void transcode(final MediaResource resource, final OutputStream outputStream, final Video outputVideo) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(outputVideo, "outputAudio 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 MediaResource");

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			FFmpegUtils.transcode(grabber, recorder, outputVideo, FrameType.ALL,
				false);
		}
	}

	public static void extractVideo(final MediaResource resource, final File outputFile) throws IOException {
		extractVideo(resource, outputFile, null);
	}

	public static void extractVideo(final MediaResource resource, final OutputStream outputStream) throws IOException {
		extractVideo(resource, outputStream, null);
	}

	public static void extractVideo(final MediaResource resource, final File outputFile, final Video outputVideo) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 MediaResource");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			FFmpegUtils.transcode(grabber, recorder, outputVideo, FrameType.VIDEO,
				false);
		}
	}

	public static void extractVideo(final MediaResource resource, final OutputStream outputStream, final Video outputVideo) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 MediaResource");

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			FFmpegUtils.transcode(grabber, recorder, outputVideo, FrameType.VIDEO,
				false);
		}
	}

	public static void extractAudio(final MediaResource resource, final File outputFile) throws IOException {
		extractAudio(resource, outputFile, null);
	}

	public static void extractAudio(final MediaResource resource, final OutputStream outputStream) throws IOException {
		extractAudio(resource, outputStream, null);
	}

	public static void extractAudio(final MediaResource resource, final File outputFile, final Audio outputAudio) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 MediaResource");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			FFmpegUtils.transcode(grabber, recorder, outputAudio, FrameType.AUDIO,
				false);
		}
	}

	public static void extractAudio(final MediaResource resource, final OutputStream outputStream, final Audio outputAudio) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 MediaResource");

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			FFmpegUtils.transcode(grabber, recorder, outputAudio, FrameType.AUDIO,
				false);
		}
	}

	public static void cut(final MediaResource resource, final File outputFile, final Duration duration) throws IOException {
		cut(resource, outputFile, (Audio) null, Duration.ZERO, duration);
	}

	public static void cut(final MediaResource resource, final OutputStream outputStream, final Duration duration) throws IOException {
		cut(resource, outputStream, (Audio) null, Duration.ZERO, duration);
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
		cut(resource, outputFile, outputAudio, Duration.ZERO, duration);
	}

	public static void cut(final MediaResource resource, final OutputStream outputStream, final Audio outputAudio,
	                       final Duration duration) throws IOException {
		cut(resource, outputStream, outputAudio, Duration.ZERO, duration);
	}

	public static void cut(final MediaResource resource, final File outputFile, final Audio outputAudio,
	                       final Duration start, final Duration end) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 MediaResource");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			FFmpegUtils.cut(grabber, recorder, outputAudio, start, end, FrameType.ALL,
				false);
		}
	}

	public static void cut(final MediaResource resource, final OutputStream outputStream, final Audio outputAudio,
	                       final Duration start, final Duration end) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 MediaResource");

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			FFmpegUtils.cut(grabber, recorder, outputAudio, start, end, FrameType.ALL,
				false);
		}
	}

	public static void concat(final Collection<MediaResource> resources, final File outputFile) throws IOException {
		concat(resources, outputFile, null);
	}

	public static void concat(final Collection<MediaResource> resources, final OutputStream outputStream) throws IOException {
		concat(resources, outputStream, null);
	}

	public static void concat(final Collection<MediaResource> resources, final File outputFile, final Video outputVideo) throws IOException {
		Validate.notEmpty(resources, "resources 不可为空");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.isTrue(resources.stream().allMatch(resource ->
			Objects.nonNull(resource) && resource.isVideo()), "存在非视频类型 MediaResource");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			FFmpegUtils.concatByResource(resources, recorder, outputVideo, FrameType.ALL,
				false);
		}
	}

	public static void concat(final Collection<MediaResource> resources, final OutputStream outputStream,
	                          final Video outputVideo) throws IOException {
		Validate.notEmpty(resources, "resources 不可为空");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.isTrue(resources.stream().allMatch(resource ->
			Objects.nonNull(resource) && resource.isVideo()), "存在非视频类型 MediaResource");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			FFmpegUtils.concatByResource(resources, recorder, outputVideo, FrameType.ALL,
				false);
		}
	}

	public static void adjustSpeed(final MediaResource resource, final File outputFile, final float speed) throws IOException {
		adjustSpeed(resource, outputFile, speed, null);
	}

	public static void adjustSpeed(final MediaResource resource, final OutputStream outputStream, final float speed) throws IOException {
		adjustSpeed(resource, outputStream, speed, null);
	}

	public static void adjustSpeed(final MediaResource resource, final File outputFile, final float speed,
	                               final Video outputVideo) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doAdjustSpeed(resource, recorder, speed, outputVideo);
		}
	}

	public static void adjustSpeed(final MediaResource resource, final OutputStream outputStream, final float speed,
	                               final Video outputVideo) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doAdjustSpeed(resource, recorder, speed, outputVideo);
		}
	}

	public static void grabImageAtTimestamp(final MediaResource resource, final Duration timestamp,
	                                        final File outputFile) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		String outputFormat = FilenameUtils.getExtension(outputFile.getName());
		Validate.isTrue(ImageConstants.getSupportedWriteImageFormats().contains(outputFormat),
			"不支持输出为 " + outputFormat + " 格式");

		ImageIO.write(grabImageAtTimestamp(resource, timestamp), outputFormat, outputFile);
	}

	public static void grabImageAtTimestamp(final MediaResource resource, final Duration timestamp,
	                                        final File outputFile, final String outputFormat) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");
		Validate.isTrue(ImageConstants.getSupportedWriteImageFormats().contains(outputFormat),
			"不支持输出为 " + outputFormat + " 格式");

		FileUtils.forceMkdirParent(outputFile);

		ImageIO.write(grabImageAtTimestamp(resource, timestamp), outputFormat, outputFile);
	}

	public static void grabImageAtTimestamp(final MediaResource resource, final Duration timestamp,
	                                        final ImageOutputStream outputStream, final String outputFormat) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");
		Validate.isTrue(ImageConstants.getSupportedWriteImageFormats().contains(outputFormat),
			"不支持输出为 " + outputFormat + " 格式");

		ImageIO.write(grabImageAtTimestamp(resource, timestamp), outputFormat, outputStream);
	}

	public static void grabImageAtTimestamp(final MediaResource resource, final Duration timestamp,
	                                        final OutputStream outputStream, final String outputFormat) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notBlank(outputFormat, "outputFormat 不可为空");
		Validate.isTrue(ImageConstants.getSupportedWriteImageFormats().contains(outputFormat),
			"不支持输出为 " + outputFormat + " 格式");

		ImageIO.write(grabImageAtTimestamp(resource, timestamp), outputFormat, outputStream);
	}

	public static BufferedImage grabImageAtTimestamp(final MediaResource resource, final Duration timestamp) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 MediaResource");

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource)) {
			return FFmpegUtils.grabImageAtTimestamp(grabber, timestamp);
		}
	}

	public static List<BufferedImage> grabImagePeriodically(final MediaResource resource, final long interval,
	                                                        final TimeUnit timeUnit) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 MediaResource");

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource)) {
			return FFmpegUtils.grabImagePeriodically(grabber, interval, timeUnit);
		}
	}

	public static void grabImagePeriodically(final MediaResource resource, final long interval,
	                                         final TimeUnit timeUnit, final ObjLongConsumer<BufferedImage> consumer) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 MediaResource");

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource)) {
			FFmpegUtils.grabImagePeriodically(grabber, interval, timeUnit, consumer);
		}
	}

	public static void grabImagePeriodically(final MediaResource resource, final long interval, final TimeUnit timeUnit,
	                                         final String outputFormat, final File outputDir) throws IOException {
		grabImagePeriodically(resource, interval, timeUnit, outputFormat, outputDir, Object::toString);
	}

	public static void grabImagePeriodically(final MediaResource resource, final long interval, final TimeUnit timeUnit,
	                                         final String outputFormat, final File outputDir,
	                                         final Function<Long, String> filenameFormatter) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 MediaResource");
		Validate.isTrue(ImageConstants.getSupportedWriteImageFormats().contains(outputFormat),
			"不支持输出为 " + outputFormat + " 格式");
		Validate.notNull(outputDir, "outputDir 不可为 null");

		FileUtils.forceMkdir(outputDir);

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource)) {
			FFmpegUtils.grabImagePeriodically(grabber, interval, timeUnit, (image, timestamp) -> {
				String filename = filenameFormatter.apply(timestamp);
				File outputFile = new File(outputDir, filename + "." + outputFormat);
				try {
					ImageIO.write(image, outputFormat, outputFile);
				} catch (IOException e) {
					throw ExceptionUtils.asRuntimeException(e);
				}
			});
		}
	}

	public static void cropByRect(final MediaResource resource, final File outputFile, final int x, final int y,
	                              final int width, final int height) throws IOException {
		cropByRect(resource, outputFile, x, y, width, height, true);
	}

	public static void cropByRect(final MediaResource resource, final OutputStream outputStream, final int x, final int y,
	                              final int width, final int height) throws IOException {
		cropByRect(resource, outputStream, x, y, width, height, true);
	}

	public static void cropByRect(final MediaResource resource, final File outputFile, final int x, final int y,
	                              final int width, final int height, final boolean outputCropResolution) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doCropByRect(resource, recorder, x, y, width, height, null, outputCropResolution);
		}
	}

	public static void cropByRect(final MediaResource resource, final OutputStream outputStream, final int x, final int y,
	                              final int width, final int height, final boolean outputCropResolution) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doCropByRect(resource, recorder, x, y, width, height, null, outputCropResolution);
		}
	}

	public static void cropByRect(final MediaResource resource, final File outputFile, final int x, final int y,
	                              final int width, final int height, final Video outputVideo) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doCropByRect(resource, recorder, x, y, width, height, outputVideo, false);
		}
	}

	public static void cropByRect(final MediaResource resource, final OutputStream outputStream, final int x, final int y,
	                              final int width, final int height, final Video outputVideo) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doCropByRect(resource, recorder, x, y, width, height, outputVideo, false);
		}
	}

	public static void cropByOffset(final MediaResource resource, final File outputFile, final int topOffset,
	                                final int bottomOffset, final int leftOffset, final int rightOffset) throws IOException {
		cropByOffset(resource, outputFile, topOffset, bottomOffset, leftOffset, rightOffset, true);
	}

	public static void cropByOffset(final MediaResource resource, final OutputStream outputStream, final int topOffset,
	                                final int bottomOffset, final int leftOffset, final int rightOffset) throws IOException {
		cropByOffset(resource, outputStream, topOffset, bottomOffset, leftOffset, rightOffset, true);
	}

	public static void cropByOffset(final MediaResource resource, final File outputFile, final int topOffset,
	                                final int bottomOffset, final int leftOffset, final int rightOffset,
	                                final boolean outputCropResolution) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doCropByOffset(resource, recorder, topOffset, bottomOffset, leftOffset, rightOffset, null,
				outputCropResolution);
		}
	}

	public static void cropByOffset(final MediaResource resource, final OutputStream outputStream, final int topOffset,
	                                final int bottomOffset, final int leftOffset, final int rightOffset,
	                                final boolean outputCropResolution) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doCropByOffset(resource, recorder, topOffset, bottomOffset, leftOffset, rightOffset, null,
				outputCropResolution);
		}
	}

	public static void cropByOffset(final MediaResource resource, final File outputFile, final int topOffset,
	                                final int bottomOffset, final int leftOffset, final int rightOffset,
	                                final Video outputVideo) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doCropByOffset(resource, recorder, topOffset, bottomOffset, leftOffset, rightOffset, outputVideo,
				false);
		}
	}

	public static void cropByOffset(final MediaResource resource, final OutputStream outputStream,
	                                final int topOffset, final int bottomOffset, final int leftOffset,
	                                final int rightOffset, final Video outputVideo) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doCropByOffset(resource, recorder, topOffset, bottomOffset, leftOffset, rightOffset, outputVideo,
				false);
		}
	}

	public static void cropByCenter(final MediaResource resource, final File outputFile, final int width,
	                                final int height) throws IOException {
		cropByCenter(resource, outputFile, width, height, true);
	}

	public static void cropByCenter(final MediaResource resource, final OutputStream outputStream, final int width,
	                                final int height) throws IOException {
		cropByCenter(resource, outputStream, width, height, true);
	}

	public static void cropByCenter(final MediaResource resource, final File outputFile, final int width,
	                                final int height, final boolean outputCropResolution) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doCropByCenter(resource, recorder, width, height, null, outputCropResolution);
		}
	}

	public static void cropByCenter(final MediaResource resource, final OutputStream outputStream, final int width,
	                                final int height, final boolean outputCropResolution) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doCropByCenter(resource, recorder, width, height, null, outputCropResolution);
		}
	}

	public static void cropByCenter(final MediaResource resource, final File outputFile, final int width,
	                                final int height, final Video outputVideo) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doCropByCenter(resource, recorder, width, height, outputVideo, false);
		}
	}

	public static void cropByCenter(final MediaResource resource, final OutputStream outputStream, final int width,
	                                final int height, final Video outputVideo) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doCropByCenter(resource, recorder, width, height, outputVideo, false);
		}
	}

	public static void replaceAudio(final MediaResource videoResource, final MediaResource audioResource,
	                                final File outputFile) throws IOException {
		replaceAudio(videoResource, audioResource, outputFile, null, false);
	}

	public static void replaceAudio(final MediaResource videoResource, final MediaResource audioResource,
	                                final OutputStream outputStream) throws IOException {
		replaceAudio(videoResource, audioResource, outputStream, null, false);
	}

	public static void replaceAudio(final MediaResource videoResource, final MediaResource audioResource,
	                                final File outputFile, final boolean loopFillAudio) throws IOException {
		replaceAudio(videoResource, audioResource, outputFile, null, loopFillAudio);
	}

	public static void replaceAudio(final MediaResource videoResource, final MediaResource audioResource,
	                                final OutputStream outputStream, final boolean loopFillAudio) throws IOException {
		replaceAudio(videoResource, audioResource, outputStream, null, loopFillAudio);
	}

	public static void replaceAudio(final MediaResource videoResource, final MediaResource audioResource,
	                                final File outputFile, final Video outputVide) throws IOException {
		replaceAudio(videoResource, audioResource, outputFile, outputVide, false);
	}

	public static void replaceAudio(final MediaResource videoResource, final MediaResource audioResource,
	                                final OutputStream outputStream, final Video outputVide) throws IOException {
		replaceAudio(videoResource, audioResource, outputStream, outputVide, false);
	}

	public static void replaceAudio(final MediaResource videoResource, final MediaResource audioResource,
	                                final File outputFile, final Video outputVideo, final boolean loopFillAudio) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
		Validate.notNull(videoResource, "resource 不可为 null");
		Validate.isTrue(videoResource.isVideo(), "不是视频类型 MediaResource");
		Validate.notNull(audioResource, "resource 不可为 null");
		Validate.isTrue(audioResource.isAudio(), "不是音频类型 MediaResource");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameGrabber videoGrabber = FFmpegUtils.openFrameGrabber(videoResource);
		     FFmpegFrameGrabber audioGrabber = FFmpegUtils.openFrameGrabber(audioResource);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doReplaceAudio(videoGrabber, audioGrabber, recorder, outputVideo, loopFillAudio);
		}
	}

	public static void replaceAudio(final MediaResource videoResource, final MediaResource audioResource,
	                                final OutputStream outputStream, final Video outputVideo, final boolean loopFillAudio) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(videoResource, "resource 不可为 null");
		Validate.isTrue(videoResource.isVideo(), "不是视频类型 MediaResource");
		Validate.notNull(audioResource, "resource 不可为 null");
		Validate.isTrue(audioResource.isAudio(), "不是音频类型 MediaResource");

		try (FFmpegFrameGrabber videoGrabber = FFmpegUtils.openFrameGrabber(videoResource);
		     FFmpegFrameGrabber audioGrabber = FFmpegUtils.openFrameGrabber(audioResource);
		     FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doReplaceAudio(videoGrabber, audioGrabber, recorder, outputVideo, loopFillAudio);
		}
	}

	public static void addBgm(final MediaResource videoResource, final MediaResource bgmResource,
	                          final File outputFile) throws IOException {
		addBgm(videoResource, bgmResource, outputFile, null, AudioUtils.DEFAULT_BGM_WEIGHT);
	}

	public static void addBgm(final MediaResource videoResource, final MediaResource bgmResource,
	                          final OutputStream outputStream) throws IOException {
		addBgm(videoResource, bgmResource, outputStream, null, AudioUtils.DEFAULT_BGM_WEIGHT);
	}

	public static void addBgm(final MediaResource videoResource, final MediaResource bgmResource,
	                          final File outputFile, final Video outputVideo) throws IOException {
		addBgm(videoResource, bgmResource, outputFile, outputVideo, AudioUtils.DEFAULT_BGM_WEIGHT);
	}

	public static void addBgm(final MediaResource videoResource, final MediaResource bgmResource,
	                          final OutputStream outputStream, final Video outputVideo) throws IOException {
		addBgm(videoResource, bgmResource, outputStream, outputVideo, AudioUtils.DEFAULT_BGM_WEIGHT);
	}

	public static void addBgm(final MediaResource videoResource, final MediaResource bgmResource,
	                          final File outputFile, final float bgmWeight) throws IOException {
		addBgm(videoResource, bgmResource, outputFile, null, bgmWeight);
	}

	public static void addBgm(final MediaResource videoResource, final MediaResource bgmResource,
	                          final OutputStream outputStream, final float bgmWeight) throws IOException {
		addBgm(videoResource, bgmResource, outputStream, null, bgmWeight);
	}

	public static void addBgm(final MediaResource videoResource, final MediaResource bgmResource,
	                          final File outputFile, final Video outputVideo, final float bgmWeight) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doAddBgm(videoResource, bgmResource, recorder, outputVideo, bgmWeight);
		}
	}

	public static void addBgm(final MediaResource videoResource, final MediaResource bgmResource,
	                          final OutputStream outputStream, final Video outputVideo, final float bgmWeight) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doAddBgm(videoResource, bgmResource, recorder, outputVideo, bgmWeight);
		}
	}

	public static void addTextWatermark(final MediaResource resource, final File outputFile, final String watermarkText,
	                                    final File fontFile) throws IOException {
		addTextWatermark(resource, outputFile, null, watermarkText, new TextWatermarkOption(fontFile));
	}

	public static void addTextWatermark(final MediaResource resource, final OutputStream outputStream, final String watermarkText,
	                                    final File fontFile) throws IOException {
		addTextWatermark(resource, outputStream, null, watermarkText, new TextWatermarkOption(fontFile));
	}

	public static void addTextWatermark(final MediaResource resource, final File outputFile, final String watermarkText,
	                                    final String fontName) throws IOException {
		addTextWatermark(resource, outputFile, null, watermarkText, new TextWatermarkOption(fontName));
	}

	public static void addTextWatermark(final MediaResource resource, final OutputStream outputStream, final String watermarkText,
	                                    final String fontName) throws IOException {
		addTextWatermark(resource, outputStream, null, watermarkText, new TextWatermarkOption(fontName));
	}

	public static void addTextWatermark(final MediaResource resource, final File outputFile, final String watermarkText,
	                                    final TextWatermarkOption option) throws IOException {
		addTextWatermark(resource, outputFile, null, watermarkText, option);
	}

	public static void addTextWatermark(final MediaResource resource, final OutputStream outputStream, final String watermarkText,
	                                    final TextWatermarkOption option) throws IOException {
		addTextWatermark(resource, outputStream, null, watermarkText, option);
	}

	public static void addTextWatermark(final MediaResource resource, final File outputFile, final Video outputVideo,
	                                    final String watermarkText, final File fontFile) throws IOException {
		addTextWatermark(resource, outputFile, outputVideo, watermarkText, new TextWatermarkOption(fontFile));
	}

	public static void addTextWatermark(final MediaResource resource, final OutputStream outputStream,
	                                    final Video outputVideo, final String watermarkText, final File fontFile) throws IOException {
		addTextWatermark(resource, outputStream, outputVideo, watermarkText, new TextWatermarkOption(fontFile));
	}

	public static void addTextWatermark(final MediaResource resource, final File outputFile, final Video outputVideo,
	                                    final String watermarkText, final String fontName) throws IOException {
		addTextWatermark(resource, outputFile, outputVideo, watermarkText, new TextWatermarkOption(fontName));
	}

	public static void addTextWatermark(final MediaResource resource, final OutputStream outputStream,
	                                    final Video outputVideo, final String watermarkText, final String fontName) throws IOException {
		addTextWatermark(resource, outputStream, outputVideo, watermarkText, new TextWatermarkOption(fontName));
	}

	public static void addTextWatermark(final MediaResource resource, final File outputFile, final Video outputVideo,
	                                    final String watermarkText, final TextWatermarkOption option) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doAddTextWatermark(resource, recorder, outputVideo, watermarkText, option);
		}
	}

	public static void addTextWatermark(final MediaResource resource, final OutputStream outputStream,
	                                    final Video outputVideo, final String watermarkText,
	                                    final TextWatermarkOption option) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doAddTextWatermark(resource, recorder, outputVideo, watermarkText, option);
		}
	}

	public static void addImageWatermark(final MediaResource resource, final File outputFile, final File watermarkImage) throws IOException {
		addImageWatermark(resource, outputFile, null, watermarkImage, new ImageWatermarkOption());
	}

	public static void addImageWatermark(final MediaResource resource, final OutputStream outputStream,
	                                     final File watermarkImage) throws IOException {
		addImageWatermark(resource, outputStream, null, watermarkImage, new ImageWatermarkOption());
	}

	public static void addImageWatermark(final MediaResource resource, final File outputFile, final File watermarkImage,
	                                     final ImageWatermarkOption option) throws IOException {
		addImageWatermark(resource, outputFile, null, watermarkImage, option);
	}

	public static void addImageWatermark(final MediaResource resource, final OutputStream outputStream,
	                                     final File watermarkImage, final ImageWatermarkOption option) throws IOException {
		addImageWatermark(resource, outputStream, null, watermarkImage, option);
	}

	public static void addImageWatermark(final MediaResource resource, final File outputFile, final Video outputVideo,
	                                     final File watermarkImage) throws IOException {
		addImageWatermark(resource, outputFile, outputVideo, watermarkImage, new ImageWatermarkOption());
	}

	public static void addImageWatermark(final MediaResource resource, final OutputStream outputStream,
	                                     final File watermarkImage, final Video outputVideo) throws IOException {
		addImageWatermark(resource, outputStream, outputVideo, watermarkImage, new ImageWatermarkOption());
	}

	public static void addImageWatermark(final MediaResource resource, final File outputFile, final Video outputVideo,
	                                     final File watermarkImage, final ImageWatermarkOption option) throws IOException {
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doAddImageWatermark(resource, recorder, outputVideo, watermarkImage, option);
		}
	}

	public static void addImageWatermark(final MediaResource resource, final OutputStream outputStream,
	                                     final Video outputVideo, final File watermarkImage,
	                                     final ImageWatermarkOption option) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doAddImageWatermark(resource, recorder, outputVideo, watermarkImage, option);
		}
	}

	protected static void doAdjustSpeed(final MediaResource resource, final FFmpegFrameRecorder recorder, final float speed,
	                                    final Video outputVideo) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 MediaResource");
		Validate.notNull(recorder, "recorder 不可为 null");

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource)) {
			grabber.start();

			String videoFilters = FFmpegFiltersBuilder.video()
				.addGlobalFilter(FFmpegUtils.getSetptsFilter(speed))
				.addGlobalFilter(FFmpegUtils.getFpsFilter(grabber.getFrameRate()))
				.build();

			FFmpegUtils.applyFilter(grabber, recorder, outputVideo, videoFilters,
				grabber.hasAudio() ? FFmpegUtils.getAtempoFilter(speed) : null, FrameType.ALL,
				false);
		}
	}

	protected static void doCropByOffset(final MediaResource resource, final FFmpegFrameRecorder recorder,
	                                     final int topOffset, final int bottomOffset, final int leftOffset,
	                                     final int rightOffset, final Video outputVideo,
	                                     final boolean outputCropResolution) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 MediaResource");
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.isTrue(topOffset >= 0 && bottomOffset >= 0 && leftOffset >= 0 && rightOffset >= 0,
			"offset 不能小于0");

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource)) {
			grabber.start();

			int videoWidth = grabber.getImageWidth();
			int videoHeight = grabber.getImageHeight();
			// 边界检测
			if (rightOffset >= videoWidth || leftOffset >= videoWidth || leftOffset + rightOffset >= videoWidth ||
				topOffset >= videoHeight || bottomOffset >= videoHeight || topOffset + bottomOffset >= videoHeight) {
				throw new IllegalArgumentException(String.format("偏移裁剪 坐标越界，原视频：%dx%d，裁剪区域：顶部偏移标 %d，" +
						"底部偏移 %d 左侧偏移：%d 右侧偏移：%d",
					videoWidth, videoHeight, topOffset, bottomOffset, leftOffset, rightOffset));
			}

			Video cropOutputVideo = outputVideo;
			if (outputCropResolution) {
				cropOutputVideo = Video.builder(grabber)
					.resolution(videoWidth - leftOffset - rightOffset, videoHeight - topOffset - bottomOffset)
					.build();
			}

			FFmpegUtils.applyFilter(grabber, recorder, outputVideo, cropOutputVideo,
				String.format("crop=%d:%d:%s:%s", leftOffset, topOffset, "iw-" + (leftOffset + rightOffset),
					"ih-" + (topOffset + bottomOffset)), null, FrameType.ALL, false);
		}
	}

	protected static void doCropByRect(final MediaResource resource, final FFmpegFrameRecorder recorder,
	                                   final int x, final int y, final int width, final int height,
	                                   final Video outputVideo, final boolean outputCropResolution) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 MediaResource");
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.isTrue(x >= 0, "x 不能小于0");
		Validate.isTrue(y >= 0, "y 不能小于0");
		Validate.isTrue(width > 0, "width 不能小于0");
		Validate.isTrue(height > 0, "height 不能小于0");

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource)) {
			grabber.start();

			int videoHeight = grabber.getImageHeight();
			int videoWidth = grabber.getImageWidth();
			// 边界检测
			if (x >= videoWidth || width >= videoWidth || x + width >= videoWidth ||
				y >= videoHeight || height >= videoHeight || y + height >= videoHeight) {
				throw new IllegalArgumentException(String.format("区域裁剪 坐标越界，原视频：%dx%d，裁剪区域：x坐标 %d，" +
						"y坐标 %d 宽高：%d 高度：%d",
					videoWidth, videoHeight, x, y, width, height));
			}

			Video cropOutputVideo = outputVideo;
			if (outputCropResolution) {
				cropOutputVideo = Video.builder(grabber).resolution(width, height).build();
			}

			FFmpegUtils.applyFilter(grabber, recorder, outputVideo, cropOutputVideo,
				FFmpegUtils.getCropFilter(x, y, width, height), null, FrameType.ALL,
				false);
		}
	}

	protected static void doCropByCenter(final MediaResource resource, final FFmpegFrameRecorder recorder,
	                                     final int width, final int height, final Video outputVideo,
	                                     final boolean outputCropResolution) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 MediaResource");
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.isTrue(width > 0, "width 不能小于0");
		Validate.isTrue(height > 0, "height 不能小于0");

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource)) {
			grabber.start();

			int videoHeight = grabber.getImageHeight();
			int videoWidth = grabber.getImageWidth();
			// 边界检测
			if (width >= videoWidth || height >= videoHeight) {
				throw new IllegalArgumentException(String.format("中心裁剪 坐标越界，原视频：%dx%d，裁剪宽度：%d，裁剪高度：%d",
					videoWidth, videoHeight, width, height));
			}

			Video cropOutputVideo = outputVideo;
			if (outputCropResolution) {
				cropOutputVideo = Video.builder(grabber).resolution(width, height).build();
			}

			FFmpegUtils.applyFilter(grabber, recorder, outputVideo, cropOutputVideo,
				FFmpegUtils.getCropFilter((videoWidth - width) / 2,
					(videoHeight - height) / 2, width, height), null, FrameType.ALL,
				false);
		}
	}

	protected static void doAddBgm(final MediaResource videoResource, final MediaResource bgmResource,
	                               final FFmpegFrameRecorder recorder, final Video outputVideo, final float bgmWeight) throws IOException {
		Validate.notNull(videoResource, "resource 不可为 null");
		Validate.isTrue(videoResource.isVideo(), "不是视频类型 MediaResource");
		Validate.notNull(bgmResource, "resource 不可为 null");
		Validate.isTrue(bgmResource.isAudio(), "不是音频类型 MediaResource");
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.isTrue(bgmWeight > 0, "bgmWeight 必须大于0");

		try (FFmpegFrameGrabber videoGrabber = FFmpegUtils.openFrameGrabber(videoResource);
		     FFmpegFrameGrabber bgmGrabber = FFmpegUtils.openFrameGrabber(bgmResource)) {
			videoGrabber.start();

			if (!videoGrabber.hasAudio()) {
				doReplaceAudio(videoGrabber, bgmGrabber, recorder, outputVideo, true);
			} else {
				Video video = Video.parse(videoGrabber);

				FFmpegUtils.applyFilter(List.of(videoGrabber, bgmGrabber), recorder, video,
					ObjectUtils.getIfNull(outputVideo, video), null,
					FFmpegUtils.getAddBgmFilter(videoGrabber, bgmGrabber, bgmWeight),
					FrameType.ALL, false);
			}
		}
	}

	protected static void doAddTextWatermark(final MediaResource resource, final FFmpegFrameRecorder recorder,
	                                         final Video outputVideo, final String watermarkText,
	                                         final TextWatermarkOption option) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 MediaResource");
		Validate.notNull(option, "option 不可为 null");

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource)) {
			String videoFilters = option.toFFmpegFilter(watermarkText, grabber);
			FFmpegUtils.applyVideoFilter(grabber, recorder, outputVideo, videoFilters,
				FrameType.ALL, false);
		}
	}

	protected static void doAddImageWatermark(final MediaResource resource, final FFmpegFrameRecorder recorder,
	                                          final Video outputVideo, final File watermarkImage,
	                                          final ImageWatermarkOption option) throws IOException {
		Validate.notNull(recorder, "recorder 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isVideo(), "不是视频类型 MediaResource");
		Validate.notNull(option, "option 不可为 null");

		try (FFmpegFrameGrabber grabber = FFmpegUtils.openFrameGrabber(resource)) {
			grabber.start();

			String videoFilters = option.toFFmpegFilter(watermarkImage, grabber);
			FFmpegUtils.applyVideoFilter(grabber, recorder, outputVideo, videoFilters,
				FrameType.ALL, false);
		}
	}

	protected static void doReplaceAudio(final FFmpegFrameGrabber videoGrabber, final FFmpegFrameGrabber audioGrabber,
	                                     final FFmpegFrameRecorder recorder, final Video outputVideo,
	                                     final boolean loopFillAudio) throws IOException {
		Validate.notNull(videoGrabber, "videoGrabber 不可为 null");
		Validate.notNull(audioGrabber, "audioGrabber 不可为 null");
		Validate.notNull(recorder, "recorder 不可为 null");

		if (FFmpegUtils.isNotStarted(videoGrabber)) {
			videoGrabber.start();
		}
		if (FFmpegUtils.isNotStarted(audioGrabber)) {
			audioGrabber.start();
		}

		FFmpegUtils.startRecorder(recorder, videoGrabber, audioGrabber, outputVideo, FrameType.ALL);

		long videoLengthInTime = videoGrabber.getLengthInTime();
		long audioLengthInTime = audioGrabber.getLengthInTime();

		if (!loopFillAudio || videoLengthInTime <= audioLengthInTime) {
			FFmpegUtils.recordFrames(recorder, videoGrabber, audioGrabber);
		} else {
			FFmpegUtils.recordFrames(recorder, videoGrabber, FrameType.VIDEO);

			recorder.setTimestamp(0);
			String audioFilters = FFmpegFiltersBuilder.audio()
				.addGlobalFilter(FFmpegUtils.getAloopFilter(audioGrabber.getSampleRate(), audioLengthInTime))
				.addGlobalFilter(FFmpegUtils.getAtrimFilter(videoLengthInTime))
				.build();
			FFmpegUtils.applyAudioFilter(audioGrabber, recorder, outputVideo, audioFilters,
				FrameType.AUDIO, true);
		}
	}
}