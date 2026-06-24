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

import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.media.model.Audio;
import io.github.pangju666.commons.media.model.MediaResource;
import org.apache.commons.lang3.Validate;
import org.bytedeco.javacv.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 音频处理工具类（基于 JavaCV + FFmpeg 实现）
 * <p>
 * 封装常用音频处理能力，统一资源管理、参数校验与异常处理，支持文件/输出流两种输出形式；
 * 底层复用 FFmpeg 原生滤镜与帧处理逻辑，兼顾兼容性与执行效率。
 * </p>
 * <h3>核心功能列表</h3>
 * <ul>
 *     <li>音频转码：支持自定义输出格式、音频采样/声道/码率参数</li>
 *     <li>音频裁剪：支持按时长、起止时间截取片段，兼容多种输出配置</li>
 *     <li>音频拼接：多段音频按顺序合并为单个音频文件/流</li>
 *     <li>音频混音：主音频叠加背景音乐，自动对齐音频参数，支持音量调节</li>
 *     <li>音频变速：基于 atempo 滤镜实现倍速播放（变速同步改变音调），支持大范围倍率</li>
 *     <li>通用滤镜：统一封装 FFmpeg 音频滤镜调用入口，可扩展各类音频特效</li>
 * </ul>
 * <h3>通用说明</h3>
 * <ul>
 *     <li>所有入参均做非空、类型合法性校验，非法入参将抛出 {@link IllegalArgumentException}</li>
 *     <li>统一使用 try-with-resources 自动关闭流、抓取器、录制器，避免资源泄漏</li>
 *     <li>音频变速底层自动多级拼接 atempo 滤镜，突破单级 0.5 ~ 2.0 速度限制</li>
 *     <li>混音功能自动将背景音乐标准化为 WAV 格式并对齐主音频参数，保证混音效果</li>
 * </ul>
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 示例1：音频转码为 MP3 格式
 * MediaResource resource = new MediaResource(new File("input.wav"));
 * AudioUtils.transcode(resource, new File("output.mp3"), "mp3");
 *
 * // 示例2：裁剪音频前10秒
 * AudioUtils.cut(resource, new File("output.mp3"), Duration.ofSeconds(10));
 *
 * // 示例3：音频混音（背景音乐音量0.4）
 * MediaResource bgm = new MediaResource(new File("bgm.mp3"));
 * AudioUtils.remix(resource, bgm, new File("remix.mp3"), 0.4f);
 *
 * // 示例4：音频变速（2倍速）
 * AudioUtils.adjustSpeed(resource, new File("output_speed.mp3"), 2.0f);
 * }</pre>
 *
 * @author pangju666
 * @see org.bytedeco.javacv.FFmpegFrameGrabber
 * @see org.bytedeco.javacv.FFmpegFrameRecorder
 * @see org.bytedeco.javacv.FFmpegFrameFilter
 * @see io.github.pangju666.commons.media.model.MediaResource
 * @see io.github.pangju666.commons.media.model.Audio
 * @since 1.1.0
 */
public class AudioUtils {
	/**
	 * 背景音乐音量安全上限（超过此值极易出现爆音）
	 *
	 * @since 1.1.0
	 */
	public static final float MAX_BGM_VOLUME = 1.5f;
	/**
	 * 背景音乐标准化格式（WAV，用于混音时的格式统一）
	 *
	 * @since 1.1.0
	 */
	protected static final String BGM_FORMAT = "wav";
	/**
	 * 音频变速 - 单级 atempo 最小速度（FFmpeg 原生限制）
	 *
	 * @since 1.1.0
	 */
	protected static final float MIN_SPEED = 0.5f;
	/**
	 * 音频变速 - 单级 atempo 最大速度（FFmpeg 原生限制）
	 *
	 * @since 1.1.0
	 */
	protected static final float MAX_SPEED = 2.0f;
	/**
	 * 16位音频采样值缩放比例（Short.MAX_VALUE = 32768）
	 *
	 * @since 1.1.0
	 */
	private static final float S16_SAMPLE_SCALE = 32768.0f;

	public static void transcode(MediaResource resource, File outputFile, Audio outputAudio) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doTranscode(resource, recorder, outputAudio);
		}
	}

	public static void transcode(MediaResource resource, OutputStream outputStream, Audio outputAudio) throws IOException {
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doTranscode(resource, recorder, outputAudio);
		}
	}

	public static void cut(MediaResource resource, File outputFile, Duration duration) throws IOException {
		cut(resource, outputFile, Duration.ZERO, duration);
	}

	public static void cut(MediaResource resource, OutputStream outputStream, Duration duration) throws IOException {
		Validate.notNull(duration, "duration 不可为 null");

		cut(resource, outputStream, Duration.ZERO, duration);
	}

	public static void cut(MediaResource resource, File outputFile, Duration start, Duration end) throws IOException {
		Validate.notNull(start, "start 不可为 null");
		if (Objects.nonNull(end)) {
			Validate.isTrue(end.compareTo(start) > 0, "end 必须大于 start");
		}
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doCut(resource, recorder, null, start, end);
		}
	}

	public static void cut(MediaResource resource, OutputStream outputStream, Duration start, Duration end) throws IOException {
		Validate.notNull(start, "start 不可为 null");
		if (Objects.nonNull(end)) {
			Validate.isTrue(end.compareTo(start) > 0, "end 必须大于 start");
		}
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doCut(resource, recorder, null, start, end);
		}
	}

	public static void cut(MediaResource resource, File outputFile, Audio outputAudio, Duration duration) throws IOException {
		Validate.notNull(duration, "duration 不可为 null");

		cut(resource, outputFile, outputAudio, Duration.ZERO, duration);
	}

	public static void cut(MediaResource resource, OutputStream outputStream, Audio outputAudio, Duration duration) throws IOException {
		Validate.notNull(duration, "duration 不可为 null");

		cut(resource, outputStream, outputAudio, Duration.ZERO, duration);
	}

	public static void cut(MediaResource resource, File outputFile, Audio outputAudio, Duration start, Duration end) throws IOException {
		Validate.notNull(start, "start 不可为 null");
		if (Objects.nonNull(end)) {
			Validate.isTrue(end.compareTo(start) > 0, "end 必须大于 start");
		}
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doCut(resource, recorder, outputAudio, start, end);
		}
	}

	public static void cut(MediaResource resource, OutputStream outputStream, Audio outputAudio, Duration start, Duration end) throws IOException {
		Validate.notNull(start, "start 不可为 null");
		if (Objects.nonNull(end)) {
			Validate.isTrue(end.compareTo(start) > 0, "end 必须大于 start");
		}
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doCut(resource, recorder, outputAudio, start, end);
		}
	}

	public static void concat(List<MediaResource> resources, File outputFile, Audio outputAudio) throws IOException {
		Validate.notEmpty(resources, "resources 不可为空");
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doConcat(resources, recorder, outputAudio);
		}
	}

	public static void concat(List<MediaResource> resources, OutputStream outputStream, Audio outputAudio) throws IOException {
		Validate.notEmpty(resources, "resources 不可为空");
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doConcat(resources, recorder, outputAudio);
		}
	}

	public static void remix(MediaResource mainResource, MediaResource bgmResource, File outputFile) throws IOException {
		remix(mainResource, bgmResource, outputFile, MAX_BGM_VOLUME);
	}

	public static void remix(MediaResource mainResource, MediaResource bgmResource, File outputFile, float bgmVolume) throws IOException {
		Validate.notNull(mainResource, "mainResource 不可为 null");
		Validate.notNull(bgmResource, "bgmResource 不可为 null");
		Validate.isTrue(bgmVolume > 0, "bgmVolume 必须大于0");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doRemix(mainResource, bgmResource, recorder, bgmVolume);
		}
	}

	public static void remix(MediaResource mainResource, MediaResource bgmResource, OutputStream outputStream) throws IOException {
		remix(mainResource, bgmResource, outputStream, MAX_BGM_VOLUME);
	}

	public static void remix(MediaResource mainResource, MediaResource bgmResource, OutputStream outputStream, float bgmVolume) throws IOException {
		Validate.notNull(mainResource, "mainResource 不可为 null");
		Validate.notNull(bgmResource, "bgmResource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.isTrue(bgmVolume > 0, "bgmVolume 必须大于0");
		Validate.isTrue(mainResource.isAudio(), "不是音频类型 MediaResource");
		Validate.isTrue(bgmResource.isAudio(), "不是音频类型 MediaResource");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doRemix(mainResource, bgmResource, recorder, bgmVolume);
		}
	}

	public static void adjustSpeed(MediaResource resource, OutputStream outputStream, float speed) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.isTrue(speed > 0, "变速倍率必须大于 0");
		Validate.isTrue(speed <= 10.0f, "变速倍率最大支持 10.0");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doAdjustSpeed(resource, recorder, speed, null);
		}
	}

	public static void adjustSpeed(MediaResource resource, File outputFile, float speed) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(speed > 0, "变速倍率必须大于 0");
		Validate.isTrue(speed <= 10.0f, "变速倍率最大支持 10.0");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doAdjustSpeed(resource, recorder, speed, null);
		}
	}

	public static void adjustSpeed(MediaResource resource, OutputStream outputStream, float speed, Audio outputAudio) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.isTrue(speed > 0, "变速倍率必须大于 0");
		Validate.isTrue(speed <= 10.0f, "变速倍率最大支持 10.0");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		Validate.notNull(outputAudio, "outputAudio 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doAdjustSpeed(resource, recorder, speed, outputAudio);
		}
	}

	public static void adjustSpeed(MediaResource resource, File outputFile, float speed, Audio outputAudio) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.isTrue(speed > 0, "变速倍率必须大于 0");
		Validate.isTrue(speed <= 10.0f, "变速倍率最大支持 10.0");
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doAdjustSpeed(resource, recorder, speed, outputAudio);
		}
	}

	public static void applyFilter(MediaResource resource, File outputFile, String audioFilters) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notBlank(audioFilters, "audioFilters 不可为空");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doApplyFilter(resource, recorder, audioFilters, null);
		}
	}

	public static void applyFilter(MediaResource resource, OutputStream outputStream, String audioFilters) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notBlank(audioFilters, "audioFilters 不可为空");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doApplyFilter(resource, recorder, audioFilters, null);
		}
	}

	public static void applyFilter(MediaResource resource, File outputFile, String audioFilters, Audio outputAudio) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notBlank(audioFilters, "audioFilters 不可为空");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 0)) {
			doApplyFilter(resource, recorder, audioFilters, outputAudio);
		}
	}

	public static void applyFilter(MediaResource resource, OutputStream outputStream, String audioFilters, Audio outputAudio) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notBlank(audioFilters, "audioFilters 不可为空");
		Validate.notNull(outputAudio, "outputAudio 不可为 null");
		Validate.isTrue(resource.isAudio(), "不是音频类型 MediaResource");

		try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, 0)) {
			doApplyFilter(resource, recorder, audioFilters, outputAudio);
		}
	}

	protected static void doConcat(List<MediaResource> resources, FFmpegFrameRecorder recorder, Audio outputAudio) throws IOException {
		initRecorder(recorder, null, outputAudio);

		for (MediaResource resource : resources) {
			if (Objects.isNull(resource) || !resource.isAudio()) {
				continue;
			}

			try (FFmpegFrameGrabber grabber = (resource.isFile() ? new FFmpegFrameGrabber(resource.getFile()) :
				new FFmpegFrameGrabber(resource.getInputStream()))) {
				grabber.start();

				recordFrames(recorder, grabber, null);
			}
		}
	}

	protected static void doRemix(MediaResource mainResource, MediaResource bgmResource, FFmpegFrameRecorder recorder,
	                              float bgmVolume) throws IOException {
		File tmpFile = null;
		FFmpegFrameGrabber bgmGrabber;
		Frame[] liveBgmHolder = new Frame[1];

		try (FFmpegFrameGrabber mainGrabber = (mainResource.isFile() ? new FFmpegFrameGrabber(mainResource.getFile()) :
			new FFmpegFrameGrabber(mainResource.getInputStream()))) {
			mainGrabber.start();
			Audio mainAudio = Audio.builder(mainGrabber).build();

			bgmGrabber = bgmResource.isFile() ? new FFmpegFrameGrabber(bgmResource.getFile()) :
				new FFmpegFrameGrabber(bgmResource.getInputStream());
			bgmGrabber.start();

			// 判断是否需要转临时标准化wav
			if (bgmGrabber.getAudioChannels() != mainAudio.getChannels() ||
				bgmGrabber.getSampleRate() != mainAudio.getSampleRate() ||
				!BGM_FORMAT.equals(bgmGrabber.getFormat())) {
				tmpFile = File.createTempFile(UUID.randomUUID().toString(), null);

				try (FFmpegFrameRecorder bgmRecorder = new FFmpegFrameRecorder(tmpFile, 0)) {
					Audio outputAudio = Audio.builder(mainAudio).format(BGM_FORMAT).build();
					initRecorder(bgmRecorder, bgmGrabber, outputAudio);
					recordFrames(bgmRecorder, bgmGrabber, null);
				} catch (IOException e) {
					FileUtils.forceDeleteIfExist(tmpFile);
					throw e;
				} finally {
					bgmGrabber.close();
				}

				bgmGrabber = new FFmpegFrameGrabber(tmpFile);
				bgmGrabber.start();
			}

			List<short[]> bgmPcmCache = new ArrayList<>();
			boolean isCacheBgm = bgmGrabber.getLengthInTime() < mainGrabber.getLengthInTime();

			if (isCacheBgm) {
				while (true) {
					try (Frame frame = bgmGrabber.grabSamples()) {
						if (Objects.isNull(frame)) {
							break;
						}

						if (frame.samples != null && frame.samples.length > 0) {
							ShortBuffer sb = (ShortBuffer) frame.samples[0];
							short[] arr = new short[sb.remaining()];
							sb.get(arr);
							bgmPcmCache.add(arr);
						}
					} catch (IOException e) {
						bgmGrabber.close();
						FileUtils.forceDeleteIfExist(tmpFile);
						throw e;
					}
				}
			}

			try {
				initRecorder(recorder, mainGrabber, mainAudio);

				int bgmPageIndex = 0;
				int bgmSampleOffset = 0;

				while (true) {
					try (Frame mainFrame = mainGrabber.grabSamples()) {
						if (Objects.isNull(mainFrame)) {
							break;
						}
						if (mainFrame.samples == null) {
							continue;
						}

						Buffer[] sampleBuffers = mainFrame.samples;
						int sampleCount = sampleBuffers[0].remaining();

						// 逐声道混音
						for (int i = 0; i < sampleBuffers.length; i++) {
							Buffer buffer = sampleBuffers[i];
							// 区分浮点 / 16位整型
							if (buffer instanceof FloatBuffer floatBuffer) {
								float[] mainArr = new float[sampleCount];
								floatBuffer.get(mainArr);
								floatBuffer.rewind();

								for (int j = 0; j < sampleCount; j++) {
									float mainSample = mainArr[j];
									float bgmSample = computeBgmSample(isCacheBgm, bgmPcmCache,
										bgmGrabber, liveBgmHolder, bgmPageIndex,
										bgmSampleOffset, i);
									float mixSample = mainSample + bgmSample * bgmVolume;
									floatBuffer.put(j, Math.max(-1f, Math.min(1f, mixSample)));
									if (isCacheBgm) {
										bgmSampleOffset++;
									}
								}
							} else if (buffer instanceof ShortBuffer shortBuffer) {
								short[] mainArr = new short[sampleCount];
								shortBuffer.get(mainArr);
								shortBuffer.rewind();

								for (int j = 0; j < sampleCount; j++) {
									float mainSample = mainArr[j] / S16_SAMPLE_SCALE;
									float bgmSample = computeBgmSample(isCacheBgm, bgmPcmCache,
										bgmGrabber, liveBgmHolder, bgmPageIndex,
										bgmSampleOffset, i);
									int mixSample = (int) ((mainSample + bgmSample * bgmVolume) * S16_SAMPLE_SCALE);
									shortBuffer.put(j, (short) Math.max(Short.MIN_VALUE,
										Math.min(Short.MAX_VALUE, mixSample)));
									if (isCacheBgm) {
										bgmSampleOffset++;
									}
								}
							}
						}

						if (isCacheBgm) {
							short[] page = bgmPcmCache.get(bgmPageIndex);
							if (bgmSampleOffset >= page.length) {
								bgmSampleOffset = 0;
								bgmPageIndex = (bgmPageIndex + 1) % bgmPcmCache.size();
							}
						}

						recorder.record(mainFrame);
					}
				}
			} finally {
				if (liveBgmHolder[0] != null) {
					liveBgmHolder[0].close();
				}
				bgmGrabber.close();

				FileUtils.forceDeleteIfExist(tmpFile);
			}
		}
	}

	protected static void doAdjustSpeed(MediaResource resource, FFmpegFrameRecorder recorder, float speed,
	                                    Audio outputAudio) throws IOException {
		List<String> filterParts = new ArrayList<>();
		float current = speed;
		// 大于2.0：不断除以 2.0 叠加 atempo=2.0
		while (current > MAX_SPEED) {
			filterParts.add("atempo=2.0");
			current /= MAX_SPEED;
		}
		// 小于0.5：不断乘以 2.0 叠加 atempo=0.5
		while (current < MIN_SPEED) {
			filterParts.add("atempo=0.5");
			current *= MIN_SPEED;
		}
		// 最后补上剩余倍率
		filterParts.add(String.format("atempo=%.2f", current));
		String audioFilters = String.join(",", filterParts);

		doApplyFilter(resource, recorder, audioFilters, outputAudio);
	}

	protected static void doApplyFilter(MediaResource resource, FFmpegFrameRecorder recorder, String audioFilters,
	                                    Audio outputAudio) throws IOException {
		try (FFmpegFrameGrabber grabber = (resource.isFile() ? new FFmpegFrameGrabber(resource.getFile()) :
			new FFmpegFrameGrabber(resource.getInputStream()));
		     FFmpegFrameFilter filter = new FFmpegFrameFilter(audioFilters, 0)) {
			grabber.start();

			filter.setAudioChannels(grabber.getAudioChannels());
			filter.setSampleRate(grabber.getSampleRate());
			filter.start();

			initRecorder(recorder, grabber, outputAudio);

			while (true) {
				try (Frame grabberFrame = grabber.grabSamples()) {
					if (Objects.isNull(grabberFrame)) {
						break;
					}

					filter.push(grabberFrame);

					while (true) {
						try (Frame filterFrame = filter.pull()) {
							if (Objects.isNull(filterFrame)) {
								break;
							}

							recorder.record(filterFrame);
						}
					}
				}
			}

			while (true) {
				try (Frame filterFrame = filter.pull()) {
					if (Objects.isNull(filterFrame)) {
						break;
					}

					recorder.record(filterFrame);
				}
			}
		}
	}

	protected static void doCut(MediaResource resource, FFmpegFrameRecorder recorder, Audio outputAudio,
	                            Duration start, Duration end) throws IOException {
		try (FFmpegFrameGrabber grabber = (resource.isFile() ? new FFmpegFrameGrabber(resource.getFile()) :
			new FFmpegFrameGrabber(resource.getInputStream()))) {
			long startTimestamp = start.toNanos() / 1000;
			long endTimestamp = Objects.isNull(end) ? grabber.getLengthInTime() : Math.min(end.toNanos() / 1000, grabber.getLengthInTime());

			Validate.isTrue(startTimestamp < grabber.getLengthInTime(), "start 必须小于音频总时长");

			grabber.setTimestamp(startTimestamp);
			grabber.start();

			initRecorder(recorder, grabber, outputAudio);
			recordFrames(recorder, grabber, endTimestamp);
		}
	}

	protected static void doTranscode(MediaResource resource, FFmpegFrameRecorder recorder, Audio outputAudio) throws IOException {
		try (FFmpegFrameGrabber grabber = (resource.isFile() ? new FFmpegFrameGrabber(resource.getFile()) :
			new FFmpegFrameGrabber(resource.getInputStream()))) {
			grabber.start();

			initRecorder(recorder, grabber, outputAudio);
			recordFrames(recorder, grabber, null);
		}
	}

	protected static void recordFrames(FFmpegFrameRecorder recorder, FFmpegFrameGrabber grabber, Long endTimestamp)
		throws FFmpegFrameRecorder.Exception, FrameGrabber.Exception {
		if (Objects.nonNull(endTimestamp) && grabber.getTimestamp() > endTimestamp) {
			return;
		}

		while (true) {
			try (Frame frame = grabber.grabSamples()) {
				if (Objects.isNull(frame)) {
					break;
				}
				recorder.record(frame);
			}
		}
	}

	protected static void initRecorder(FFmpegFrameRecorder recorder, FFmpegFrameGrabber grabber, Audio outputAudio) throws FFmpegFrameRecorder.Exception {
		if (Objects.nonNull(outputAudio)) {
			outputAudio.initRecoder(recorder);
		} else {
			recorder.setFormat(grabber.getFormat());

			recorder.setAudioCodec(grabber.getAudioCodec());
			recorder.setAudioCodecName(grabber.getAudioCodecName());
			recorder.setSampleRate(grabber.getSampleRate());
			recorder.setAudioBitrate(grabber.getAudioBitrate());
			recorder.setAudioChannels(grabber.getAudioChannels());
			recorder.setAudioMetadata(grabber.getAudioMetadata());
		}

		recorder.start();
	}

	private static float computeBgmSample(boolean useCache, List<short[]> pageCache, FFmpegFrameGrabber grabber,
	                                      Frame[] liveHolder, int pageIndex, int cacheSampleOffset, int channelIndex) throws IOException {
		if (useCache) {
			short[] page = pageCache.get(pageIndex);
			int safeOff = Math.min(cacheSampleOffset, page.length - 1);
			return page[safeOff] / S16_SAMPLE_SCALE;
		}

		Frame frame = liveHolder[0];
		if (frame == null || !frame.samples[0].hasRemaining()) {
			if (frame != null) {
				frame.close();
			}
			frame = grabber.grabSamples();
			liveHolder[0] = frame;
			if (frame == null) {
				return 0f;
			}
		}

		Buffer buffer = frame.samples[Math.min(channelIndex, frame.samples.length - 1)];
		return ((ShortBuffer) buffer).get() / S16_SAMPLE_SCALE;
	}
}