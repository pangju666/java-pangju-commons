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
 *    limitations under the Code.
 */

package io.github.pangju666.commons.ffmpeg.builder

import io.github.pangju666.commons.ffmpeg.lang.FFmpegConstants
import io.github.pangju666.commons.ffmpeg.model.AudioOutputOption
import org.bytedeco.ffmpeg.global.avcodec
import spock.lang.Specification

class AudioOutputOptionBuilderSpec extends Specification {
	def "构造器 - 根据格式创建"() {
		when:
		def builder = new AudioOutputOptionBuilder("mp3")

		then:
		builder != null
		builder.outputOption.format == "mp3"
	}

	def "静态工厂方法 - wav"() {
		when:
		def builder = AudioOutputOptionBuilder.wav()

		then:
		builder != null
		builder.outputOption.format == "wav"
		builder.outputOption.codecId == avcodec.AV_CODEC_ID_PCM_S16LE
	}

	def "静态工厂方法 - flac"() {
		when:
		def builder = AudioOutputOptionBuilder.flac()

		then:
		builder != null
		builder.outputOption.format == "flac"
		builder.outputOption.codecId == avcodec.AV_CODEC_ID_FLAC
	}

	def "静态工厂方法 - mp3"() {
		when:
		def builder = AudioOutputOptionBuilder.mp3()

		then:
		builder != null
		builder.outputOption.format == "mp3"
		builder.outputOption.codecId == avcodec.AV_CODEC_ID_MP3
	}

	def "静态工厂方法 - opus"() {
		when:
		def builder = AudioOutputOptionBuilder.opus()

		then:
		builder != null
		builder.outputOption.format == "opus"
		builder.outputOption.codecId == avcodec.AV_CODEC_ID_OPUS
	}

	def "静态工厂方法 - aac"() {
		when:
		def builder = AudioOutputOptionBuilder.aac()

		then:
		builder != null
		builder.outputOption.format == "adts"
		builder.outputOption.codecId == avcodec.AV_CODEC_ID_AAC
	}

	def "codec方法 - 设置编码器"() {
		given:
		def builder = new AudioOutputOptionBuilder("mp3")
		def codec = avcodec.avcodec_find_encoder(avcodec.AV_CODEC_ID_MP3)

		when:
		def result = builder.codec(codec)

		then:
		result.is(builder)
		builder.outputOption.getCodecId() == codec.id()
	}

	def "codecId方法 - 设置编码器ID"() {
		given:
		def builder = new AudioOutputOptionBuilder(FFmpegConstants.AUDIO_MP3_FORMAT)

		when:
		def result = builder.codecId(avcodec.AV_CODEC_ID_MP3).build()

		then:
		result.codecId == avcodec.AV_CODEC_ID_MP3
	}

	def "cd方法 - 设置CD标准采样率"() {
		given:
		def builder = new AudioOutputOptionBuilder("wav")

		when:
		def result = builder.cd()

		then:
		result.is(builder)
		builder.outputOption.sampleRate == 44100
	}

	def "broadcast方法 - 设置广播标准采样率"() {
		given:
		def builder = new AudioOutputOptionBuilder("wav")

		when:
		def result = builder.broadcast()

		then:
		result.is(builder)
		builder.outputOption.sampleRate == 48000
	}

	def "sampleRate方法 - 设置采样率"() {
		given:
		def builder = new AudioOutputOptionBuilder("wav")

		when:
		def result = builder.sampleRate(48000)

		then:
		result.is(builder)
		builder.outputOption.sampleRate == 48000
	}

	def "mono方法 - 设置为单声道"() {
		given:
		def builder = new AudioOutputOptionBuilder("wav")

		when:
		def result = builder.mono()

		then:
		result.is(builder)
		builder.outputOption.channels == 1
	}

	def "stereo方法 - 设置为立体声"() {
		given:
		def builder = new AudioOutputOptionBuilder("wav")

		when:
		def result = builder.stereo()

		then:
		result.is(builder)
		builder.outputOption.channels == 2
	}

	def "channels方法 - 设置声道数"() {
		given:
		def builder = new AudioOutputOptionBuilder("wav")

		when:
		def result = builder.channels(4)

		then:
		result.is(builder)
		builder.outputOption.channels == 4
	}

	def "bitrate方法 - 设置比特率"() {
		given:
		def builder = new AudioOutputOptionBuilder("mp3")

		when:
		def result = builder.bitrate(192000)

		then:
		result.is(builder)
		builder.outputOption.bitrate == 192000
	}

	def "build方法 - 构建音频输出选项"() {
		given:
		def builder = new AudioOutputOptionBuilder("mp3")

		when:
		def option = builder.build()

		then:
		option != null
		option instanceof AudioOutputOption
		option.format == "mp3"
	}

	def "链式调用 - 完整构建流程"() {
		when:
		def option = AudioOutputOptionBuilder.mp3()
			.cd()
			.stereo()
			.bitrate(192000)
			.build()

		then:
		option != null
		option.format == "mp3"
		option.codecId == avcodec.AV_CODEC_ID_MP3
		option.sampleRate == 44100
		option.channels == 2
		option.bitrate == 192000
	}

	def "链式调用 - 使用所有方法"() {
		when:
		def option = AudioOutputOptionBuilder.wav()
			.broadcast()
			.stereo()
			.bitrate(1411200)
			.build()

		then:
		option != null
		option.format == "wav"
		option.codecId == avcodec.AV_CODEC_ID_PCM_S16LE
		option.sampleRate == 48000
		option.channels == 2
		option.bitrate == 1411200
	}
}
