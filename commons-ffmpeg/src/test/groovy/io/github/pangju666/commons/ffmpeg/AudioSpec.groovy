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

package io.github.pangju666.commons.ffmpeg

import io.github.pangju666.commons.ffmpeg.io.resource.FFmpegResource
import io.github.pangju666.commons.ffmpeg.model.Audio
import org.bytedeco.javacv.FFmpegFrameGrabber
import spock.lang.Specification

class AudioSpec extends Specification {
	static final String TEST_AUDIOS_DIR = "src/test/resources/audios"
	static final List<String> TEST_AUDIO_FILES = [
		"file_example_MP3_5MG.mp3",
		"suzume_no_tojimari.flac",
		"suzume_no_tojimari.ogg",
		"suzume_no_tojimari.wav"
	]

	def "基于现有Audio创建Builder"() {
		given:
		def audio = Audio.wav()
			.stereo()
			.cd()
			.build()

		when:
		def builder = Audio.builder(audio)

		then:
		builder != null
	}

	def "基于现有Audio创建Builder - null抛异常"() {
		when:
		Audio.builder((Audio) null)

		then:
		thrown(NullPointerException)
	}

	def "从FFmpegFrameGrabber创建Builder"() {
		given:
		def grabber = new FFmpegFrameGrabber(new File("${TEST_AUDIOS_DIR}/${TEST_AUDIO_FILES.get(3)}"))

		when:
		def builder = Audio.builder(grabber)

		then:
		builder != null
	}

	def "从FFmpegFrameGrabber创建Builder - null抛异常"() {
		when:
		Audio.builder((FFmpegFrameGrabber) null)

		then:
		thrown(NullPointerException)
	}

	def "从FFmpegResource创建Builder"() {
		given:
		def testFile = new File("${TEST_AUDIOS_DIR}/${fileName}")
		def resource = new FFmpegResource(testFile)

		when:
		def builder = Audio.builder(resource)

		then:
		builder != null

		where:
		fileName << TEST_AUDIO_FILES
	}

	def "从FFmpegResource创建Builder - null抛异常"() {
		when:
		Audio.builder((FFmpegResource) null)

		then:
		thrown(NullPointerException)
	}

	def "从InputStream创建Builder"() {
		given:
		def testFile = new File("${TEST_AUDIOS_DIR}/suzume_no_tojimari.wav")
		def inputStream = new FileInputStream(testFile)

		when:
		def builder = Audio.builder(inputStream)

		then:
		builder != null
	}

	def "从InputStream创建Builder - null抛异常"() {
		when:
		Audio.builder((InputStream) null)

		then:
		thrown(NullPointerException)
	}

	def "从FFmpegFrameGrabber解析Audio"() {
		given:
		def grabber = new FFmpegFrameGrabber(new File("${TEST_AUDIOS_DIR}/${TEST_AUDIO_FILES.get(3)}"))

		when:
		def audio = Audio.parse(grabber)

		then:
		audio != null
	}

	def "从FFmpegResource解析Audio"() {
		given:
		def testFile = new File("${TEST_AUDIOS_DIR}/${fileName}")
		def resource = new FFmpegResource(testFile)

		when:
		def audio = Audio.parse(resource)

		then:
		audio != null
		audio.getDuration() != null
		audio.getSampleRate() > 0
		audio.getChannels() > 0

		where:
		fileName << TEST_AUDIO_FILES
	}

	def "从FFmpegResource解析Audio - null抛异常"() {
		when:
		Audio.parse((FFmpegResource) null)

		then:
		thrown(NullPointerException)
	}

	def "从InputStream解析Audio"() {
		given:
		def testFile = new File("${TEST_AUDIOS_DIR}/suzume_no_tojimari.wav")
		def inputStream = new FileInputStream(testFile)

		when:
		def audio = Audio.parse(inputStream)

		then:
		audio != null
	}

	def "从InputStream解析Audio - null抛异常"() {
		when:
		Audio.parse((InputStream) null)

		then:
		thrown(NullPointerException)
	}

	def "获取音频时长"() {
		given:
		def audio = Audio.wav()
			.stereo()
			.cd()
			.build()

		when:
		def duration = audio.getDuration()

		then:
		duration == null || duration.isZero()
	}

	def "获取采样率"() {
		given:
		def audio = Audio.wav()
			.stereo()
			.cd()
			.build()

		when:
		def sampleRate = audio.getSampleRate()

		then:
		sampleRate > 0
	}

	def "获取声道数"() {
		given:
		def audio = Audio.wav()
			.stereo()
			.cd()
			.build()

		when:
		def channels = audio.getChannels()

		then:
		channels > 0
	}

	def "获取比特率"() {
		given:
		def audio = Audio.wav()
			.stereo()
			.cd()
			.bitrate(128000)
			.build()

		when:
		def bitrate = audio.getBitrate()

		then:
		bitrate == 128000
	}

	def "判断是否为单声道"() {
		given:
		def audio = Audio.wav()
			.mono()
			.cd()
			.build()

		when:
		def isMono = audio.isMono()

		then:
		isMono
	}

	def "判断是否为立体声"() {
		given:
		def audio = Audio.wav()
			.stereo()
			.cd()
			.build()

		when:
		def isStereo = audio.isStereo()

		then:
		isStereo
	}

	def "判断是否有时长信息"() {
		given:
		def audio = Audio.wav()
			.stereo()
			.cd()
			.build()

		when:
		def hasDuration = audio.hasDuration()

		then:
		!hasDuration
	}

	def "使用预定义WAV配置"() {
		expect:
		Audio.WAV != null
	}

	def "使用预定义FLAC配置"() {
		expect:
		Audio.FLAC != null
	}

	def "使用预定义MP3配置"() {
		expect:
		Audio.MP3 != null
	}

	def "使用预定义MP3_HIGH配置"() {
		expect:
		Audio.MP3_HIGH != null
	}

	def "使用预定义OPUS配置"() {
		expect:
		Audio.OPUS != null
	}

	def "使用预定义OPUS_HIGH配置"() {
		expect:
		Audio.OPUS_HIGH != null
	}

	def "使用预定义AAC配置"() {
		expect:
		Audio.AAC != null
	}

	def "使用预定义AAC_HIGH配置"() {
		expect:
		Audio.AAC_HIGH != null
	}

	def "Builder设置WAV格式"() {
		when:
		def audio = Audio.wav()
			.build()

		then:
		audio != null
	}

	def "Builder设置FLAC格式"() {
		when:
		def audio = Audio.flac()
			.build()

		then:
		audio != null
	}

	def "Builder设置MP3格式"() {
		when:
		def audio = Audio.mp3()
			.build()

		then:
		audio != null
	}

	def "Builder设置OPUS格式"() {
		when:
		def audio = Audio.opus()
			.build()

		then:
		audio != null
	}

	def "Builder设置AAC格式"() {
		when:
		def audio = Audio.aac()
			.build()

		then:
		audio != null
	}

	def "Builder设置CD采样率"() {
		when:
		def audio = Audio.mp3()
			.cd()
			.build()

		then:
		audio.getSampleRate() == 44100
	}

	def "Builder设置Film采样率"() {
		when:
		def audio = Audio.mp3()
			.film()
			.build()

		then:
		audio.getSampleRate() == 48000
	}

	def "Builder设置单声道"() {
		when:
		def audio = Audio.mp3()
			.mono()
			.build()

		then:
		audio.getChannels() == 1
	}

	def "Builder设置立体声"() {
		when:
		def audio = Audio.mp3()
			.stereo()
			.build()

		then:
		audio.getChannels() == 2
	}

	def "Builder设置采样率 - 负数抛异常"() {
		when:
		Audio.mp3()
			.sampleRate(-1)

		then:
		thrown(IllegalArgumentException)
	}

	def "Builder设置采样率 - 零抛异常"() {
		when:
		Audio.mp3()
			.sampleRate(0)

		then:
		thrown(IllegalArgumentException)
	}

	def "Builder设置声道数 - 负数抛异常"() {
		when:
		Audio.mp3()
			.channels(-1)

		then:
		thrown(IllegalArgumentException)
	}

	def "Builder设置声道数 - 零抛异常"() {
		when:
		Audio.mp3()
			.channels(0)

		then:
		thrown(IllegalArgumentException)
	}

	def "Builder设置比特率 - 负数抛异常"() {
		when:
		Audio.mp3()
			.bitrate(-1)

		then:
		thrown(IllegalArgumentException)
	}

	def "Builder设置比特率 - 零合法"() {
		when:
		def audio = Audio.mp3()
			.bitrate(0)
			.build()

		then:
		audio.getBitrate() == 0
	}

	def "获取采样格式"() {
		given:
		def audio = Audio.wav()
			.stereo()
			.cd()
			.build()

		when:
		def sampleFormat = audio.getSampleFormat()

		then:
		sampleFormat >= -1
	}

	def "Builder设置采样格式"() {
		when:
		def audio = Audio.mp3()
			.sampleFormat(1)
			.build()

		then:
		audio.getSampleFormat() == 1
	}

	def "Builder设置采样格式 - 小于-1抛异常"() {
		when:
		Audio.mp3()
			.sampleFormat(-2)

		then:
		thrown(IllegalArgumentException)
	}

	def "Builder设置采样格式 - -1合法"() {
		when:
		def audio = Audio.mp3()
			.sampleFormat(-1)
			.build()

		then:
		audio.getSampleFormat() == -1
	}
}
