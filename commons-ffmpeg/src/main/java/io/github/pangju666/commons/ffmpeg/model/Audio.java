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
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * 音频媒体模型类，封装音频文件的核心属性和构建逻辑
 *
 * @author pangju666
 * @since 1.1.0
 */
public class Audio extends Media {
	/**
	 * 默认音频格式：wav
	 *
	 * @since 1.1.0
	 */
	public static final String DEFAULT_FORMAT = "wav";
	/**
	 * 默认采样率：44100Hz（标准音频采样率）
	 *
	 * @since 1.1.0
	 */
	public static final int DEFAULT_SAMPLE_RATE = 44100;
	/**
	 * 默认声道数：2（立体声）
	 *
	 * @since 1.1.0
	 */
	public static final int DEFAULT_CHANNELS = 2;
	/**
	 * 默认码率：64000bps（64kbps）
	 *
	 * @since 1.1.0
	 */
	public static final int DEFAULT_BITRATE = 64000;

	/**
	 * 音频时长
	 *
	 * @since 1.1.0
	 */
	protected Duration duration;
	/**
	 * 采样率（Hz），如44100、48000
	 *
	 * @since 1.1.0
	 */
	protected int sampleRate;
	/**
	 * 声道数，1=单声道，2=立体声，>2=多声道
	 *
	 * @since 1.1.0
	 */
	protected int channels;
	/**
	 * 音频码率（bps），如64000、128000
	 *
	 * @since 1.1.0
	 */
	protected int bitrate;

	protected Audio() {
	}

	/**
	 * 创建音频构建器（默认配置）
	 * <p>默认配置如下：
	 * <ul>
	 *     <li>音频格式：wav</li>
	 *     <li>采样率：44100 Hz（标准通用采样率）</li>
	 *     <li>声道数：2（立体声）</li>
	 *     <li>音频码率：64000 bps（64kbps）</li>
	 *     <li>音频时长：0</li>
	 * </ul>
	 * </p>
	 *
	 * @return 新的AudioBuilder实例
	 * @since 1.1.0
	 */
	public static AudioBuilder builder() {
		return new AudioBuilder();
	}

	/**
	 * 基于已有音频对象创建构建器（复制已有属性）
	 *
	 * @param audio 已有音频对象，不可为null
	 * @return 新的AudioBuilder实例
	 * @throws IllegalArgumentException 当audio为null时抛出
	 * @since 1.1.0
	 */
	public static AudioBuilder builder(Audio audio) {
		return new AudioBuilder(audio);
	}

	/**
	 * 获取音频时长
	 *
	 * @return 时长对象，null表示未设置
	 * @since 1.1.0
	 */
	public Duration getDuration() {
		return duration;
	}

	/**
	 * 获取采样率（Hz）
	 *
	 * @return 采样率数值，默认44100
	 * @since 1.1.0
	 */
	public int getSampleRate() {
		return sampleRate;
	}

	/**
	 * 获取声道数
	 *
	 * @return 声道数，默认2（立体声）
	 * @since 1.1.0
	 */
	public int getChannels() {
		return channels;
	}

	/**
	 * 获取音频码率（bps）
	 *
	 * @return 码率数值，默认64000
	 * @since 1.1.0
	 */
	public int getBitrate() {
		return bitrate;
	}

	/**
	 * 判断是否为单声道
	 *
	 * @return true=单声道（声道数=1），false=非单声道
	 * @since 1.1.0
	 */
	public boolean isMono() {
		return this.channels == 1;
	}

	/**
	 * 判断是否为立体声（双声道）
	 *
	 * @return true=立体声（声道数=2），false=非立体声
	 * @since 1.1.0
	 */
	public boolean isStereo() {
		return this.channels == 2;
	}

	/**
	 * 判断是否为无损音频格式（wav/flac）
	 *
	 * @return true=无损格式，false=有损格式
	 * @since 1.1.0
	 */
	public boolean isLossless() {
		return "wav".equalsIgnoreCase(format) || "flac".equalsIgnoreCase(format);
	}

	/**
	 * 获取音频总秒数
	 *
	 * @return 总秒数，无时长时返回0
	 * @since 1.1.0
	 */
	public long getTotalSeconds() {
		if (duration == null) return 0;
		return duration.toSeconds();
	}

	/**
	 * 判断是否有有效时长（非零、非null）
	 *
	 * @return true=有有效时长，false=无时长/时长为0
	 * @since 1.1.0
	 */
	public boolean hasDuration() {
		return duration != null && !duration.isZero();
	}

	/**
	 * 音频对象构建器，实现音频特有属性的链式构建
	 *
	 * @author pangju666
	 * @since 1.1.0
	 */
	public static class AudioBuilder extends Media.Builder<AudioBuilder, Audio> {
		/**
		 * 构造默认配置的音频构建器
		 * <p>默认配置如下：
		 * <ul>
		 *     <li>音频格式：wav</li>
		 *     <li>采样率：44100 Hz（标准通用采样率）</li>
		 *     <li>声道数：2（立体声）</li>
		 *     <li>音频码率：64000 bps（64kbps）</li>
		 *     <li>音频时长：0</li>
		 * </ul>
		 * </p>
		 *
		 * @since 1.1.0
		 */
		public AudioBuilder() {
			super(new Audio());

			super.media.format = DEFAULT_FORMAT;
			super.media.sampleRate = DEFAULT_SAMPLE_RATE;
			super.media.channels = DEFAULT_CHANNELS;
			super.media.bitrate = DEFAULT_BITRATE;
			super.media.duration = Duration.ZERO;
		}

		/**
		 * 基于已有音频对象构造构建器（复制属性）
		 *
		 * @param audio 已有音频对象，不可为null
		 * @since 1.1.0
		 */
		public AudioBuilder(Audio audio) {
			super(audio);

			super.media.format = audio.getFormat();
			super.media.codecId = audio.getCodecId();
			super.media.codecName = audio.getCodecName();
			super.media.metadata = Map.copyOf(audio.getMetadata());
			super.media.duration = audio.getDuration();
			super.media.sampleRate = audio.getSampleRate();
			super.media.channels = audio.getChannels();
			super.media.bitrate = audio.getBitrate();
		}

		/**
		 * 快速设置为单声道（声道数=1）
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public AudioBuilder mono() {
			return channels(1);
		}

		/**
		 * 快速设置为双声道立体声（声道数=2）
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public AudioBuilder stereo() {
			return channels(2);
		}

		/**
		 * 设置音频时长
		 *
		 * @param duration 时长对象，null会重置为Duration.ZERO
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public AudioBuilder duration(Duration duration) {
			if (Objects.isNull(duration)) {
				super.media.duration = Duration.ZERO;
			} else {
				super.media.duration = duration;
			}
			return this;
		}

		/**
		 * 设置采样率（仅正数生效）
		 *
		 * @param sampleRate 采样率（Hz），如44100、48000
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public AudioBuilder sampleRate(int sampleRate) {
			if (sampleRate > 0) {
				super.media.sampleRate = sampleRate;
			}
			return this;
		}

		/**
		 * 设置声道数（仅正数生效）
		 *
		 * @param channels 声道数，1=单声道，2=立体声，>2=多声道
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public AudioBuilder channels(int channels) {
			if (channels > 0) {
				super.media.channels = channels;
			}
			return this;
		}

		/**
		 * 设置音频码率（仅正数生效）
		 *
		 * @param bitrate 码率（bps），如64000、128000
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		public AudioBuilder bitrate(int bitrate) {
			if (bitrate > 0) {
				super.media.bitrate = bitrate;
			}
			return this;
		}

		/**
		 * 复制当前构建器配置，生成新构建器
		 *
		 * @return 新的AudioBuilder实例（属性与当前一致）
		 * @since 1.1.0
		 */
		public AudioBuilder copy() {
			return new Audio.AudioBuilder(this.build());
		}

		/**
		 * 重置构建器为默认配置
		 *
		 * @return 构建器自身，用于链式调用
		 * @since 1.1.0
		 */
		@Override
		public AudioBuilder reset() {
			super.media.format = DEFAULT_FORMAT;
			super.media.sampleRate = DEFAULT_SAMPLE_RATE;
			super.media.channels = DEFAULT_CHANNELS;
			super.media.bitrate = DEFAULT_BITRATE;
			super.media.duration = Duration.ZERO;
			super.media.metadata = Collections.emptyMap();
			super.media.codecId = 0;
			super.media.codecName = null;
			return this;
		}

		/**
		 * 从FFmpegFrameGrabber初始化音频特有属性
		 *
		 * @param grabber FFmpeg帧抓取器（已启动且包含音频流）
		 * @since 1.1.0
		 */
		@Override
		protected void init(FFmpegFrameGrabber grabber) {
			super.init(grabber);

			if (grabber.hasAudio()) {
				super.media.metadata = Collections.unmodifiableMap(grabber.getAudioMetadata());
				super.media.codecName = grabber.getAudioCodecName();
				super.media.codecId = grabber.getAudioCodec();

				// 音频时长转换：grabber单位微秒
				super.media.duration = Duration.ofNanos(grabber.getLengthInTime() * 1000);
				super.media.sampleRate = grabber.getSampleRate();
				super.media.channels = grabber.getAudioChannels();
				super.media.bitrate = grabber.getAudioBitrate();
			}
		}
	}
}
