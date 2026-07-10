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

import io.github.pangju666.commons.ffmpeg.model.Audio
import io.github.pangju666.commons.ffmpeg.model.FFmpegResource
import io.github.pangju666.commons.ffmpeg.utils.AudioUtils
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration

class AudioUtilsSpec extends Specification {
	@TempDir
	Path tempDir

	def audioFiles = [
		"file_example_MP3_5MG.mp3",
		"suzume_no_tojimari.flac",
		"suzume_no_tojimari.ogg",
		"suzume_no_tojimari.wav"
	]

	def "DEFAULT_BGM_WEIGHT常量值"() {
		expect:
		AudioUtils.DEFAULT_BGM_WEIGHT == 0.4f
	}

	def "转码到文件 - 指定输出配置"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp3").toFile()
		def resource = new FFmpegResource(audioFile)

		when:
		AudioUtils.transcode(resource, outputFile, Audio.MP3)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "转码到输出流 - 指定输出配置"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def resource = new FFmpegResource(audioFile)
		def outputStream = tempDir.resolve("output.mp3").toFile().newOutputStream()
		def outputAudio = Audio.MP3

		when:
		AudioUtils.transcode(resource, outputStream, outputAudio)

		then:
		noExceptionThrown()
	}

	def "裁剪到文件 - 从开头裁剪指定时长"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp3").toFile()
		def resource = new FFmpegResource(audioFile)
		def duration = Duration.ofSeconds(5)

		when:
		AudioUtils.cut(resource, outputFile, duration)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "裁剪到文件 - 指定时间段"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp3").toFile()
		def resource = new FFmpegResource(audioFile)
		def start = Duration.ofSeconds(2)
		def end = Duration.ofSeconds(7)

		when:
		AudioUtils.cut(resource, outputFile, start, end)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "裁剪到输出流 - 从开头裁剪指定时长"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def resource = new FFmpegResource(audioFile)
		def outputStream = tempDir.resolve("output.mp3").toFile().newOutputStream()
		def duration = Duration.ofSeconds(5)

		when:
		AudioUtils.cut(resource, outputStream, duration)

		then:
		noExceptionThrown()
	}

	def "裁剪到输出流 - 指定时间段"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def resource = new FFmpegResource(audioFile)
		def outputStream = tempDir.resolve("output.mp3").toFile().newOutputStream()
		def start = Duration.ofSeconds(2)
		def end = Duration.ofSeconds(7)

		when:
		AudioUtils.cut(resource, outputStream, start, end)

		then:
		noExceptionThrown()
	}

	def "拼接 - 使用源配置"() {
		given:
		def audioFile1 = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def audioFile2 = Paths.get("src/test/resources/audios", audioFiles[1]).toFile()
		def outputFile = tempDir.resolve("output.mp3").toFile()
		def resources = [new FFmpegResource(audioFile1), new FFmpegResource(audioFile2)]

		when:
		AudioUtils.concat(resources, outputFile)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "拼接 - 指定输出配置"() {
		given:
		def audioFile1 = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def audioFile2 = Paths.get("src/test/resources/audios", audioFiles[1]).toFile()
		def outputFile = tempDir.resolve("output.mp3").toFile()
		def resources = [new FFmpegResource(audioFile1), new FFmpegResource(audioFile2)]
		def outputAudio = Audio.MP3

		when:
		AudioUtils.concat(resources, outputFile, outputAudio)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "拼接到输出流 - 使用源配置"() {
		given:
		def audioFile1 = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def audioFile2 = Paths.get("src/test/resources/audios", audioFiles[1]).toFile()
		def outputStream = tempDir.resolve("output.mp3").toFile().newOutputStream()
		def resources = [new FFmpegResource(audioFile1), new FFmpegResource(audioFile2)]

		when:
		AudioUtils.concat(resources, outputStream)

		then:
		noExceptionThrown()
	}

	def "拼接到输出流 - 指定输出配置"() {
		given:
		def audioFile1 = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def audioFile2 = Paths.get("src/test/resources/audios", audioFiles[1]).toFile()
		def outputStream = tempDir.resolve("output.mp3").toFile().newOutputStream()
		def resources = [new FFmpegResource(audioFile1), new FFmpegResource(audioFile2)]
		def outputAudio = Audio.MP3

		when:
		AudioUtils.concat(resources, outputStream, outputAudio)

		then:
		noExceptionThrown()
	}

	def "添加背景音乐 - 使用默认权重和源配置"() {
		given:
		def mainFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def bgmFile = Paths.get("src/test/resources/audios", audioFiles[1]).toFile()
		def outputFile = tempDir.resolve("output.mp3").toFile()
		def mainResource = new FFmpegResource(mainFile)
		def bgmResource = new FFmpegResource(bgmFile)

		when:
		AudioUtils.addBgm(mainResource, bgmResource, outputFile)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "添加背景音乐 - 指定权重和输出配置"() {
		given:
		def mainFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def bgmFile = Paths.get("src/test/resources/audios", audioFiles[1]).toFile()
		def outputFile = tempDir.resolve("output.mp3").toFile()
		def mainResource = new FFmpegResource(mainFile)
		def bgmResource = new FFmpegResource(bgmFile)
		def outputAudio = Audio.MP3

		when:
		AudioUtils.addBgm(mainResource, bgmResource, outputFile, outputAudio, 0.3f)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "添加背景音乐到输出流 - 使用默认权重和源配置"() {
		given:
		def mainFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def bgmFile = Paths.get("src/test/resources/audios", audioFiles[1]).toFile()
		def outputStream = tempDir.resolve("output.mp3").toFile().newOutputStream()
		def mainResource = new FFmpegResource(mainFile)
		def bgmResource = new FFmpegResource(bgmFile)

		when:
		AudioUtils.addBgm(mainResource, bgmResource, outputStream)

		then:
		noExceptionThrown()
	}

	def "调整速度 - 使用源配置"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp3").toFile()
		def resource = new FFmpegResource(audioFile)

		when:
		AudioUtils.adjustSpeed(resource, outputFile, 2.0f)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "调整速度 - 指定输出配置"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp3").toFile()
		def resource = new FFmpegResource(audioFile)
		def outputAudio = Audio.MP3

		when:
		AudioUtils.adjustSpeed(resource, outputFile, 1.5f, outputAudio)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "调整速度到输出流 - 使用源配置"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def resource = new FFmpegResource(audioFile)
		def outputStream = tempDir.resolve("output.mp3").toFile().newOutputStream()

		when:
		AudioUtils.adjustSpeed(resource, outputStream, 2.0f)

		then:
		noExceptionThrown()
	}

	def "调整音量 - 使用源配置"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp3").toFile()
		def resource = new FFmpegResource(audioFile)

		when:
		AudioUtils.adjustVolume(resource, outputFile, 3.0f)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "调整音量 - 指定输出配置"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp3").toFile()
		def resource = new FFmpegResource(audioFile)
		def outputAudio = Audio.MP3

		when:
		AudioUtils.adjustVolume(resource, outputFile, -2.0f, outputAudio)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "调整音量到输出流 - 使用源配置"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def resource = new FFmpegResource(audioFile)
		def outputStream = tempDir.resolve("output.mp3").toFile().newOutputStream()

		when:
		AudioUtils.adjustVolume(resource, outputStream, 3.0f)

		then:
		noExceptionThrown()
	}

	def "cut方法 - resource为null时抛出NullPointerException"() {
		given:
		def outputFile = tempDir.resolve("output.mp3").toFile()
		def duration = Duration.ofSeconds(5)

		when:
		AudioUtils.cut(null, outputFile, duration)

		then:
		thrown(NullPointerException)
	}

	def "cut方法 - resource不是音频类型时抛出IllegalArgumentException"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", "1416529-hd_1920_1080_30fps.webm").toFile()
		def outputFile = tempDir.resolve("output.mp3").toFile()
		def resource = new FFmpegResource(videoFile)
		def duration = Duration.ofSeconds(5)

		when:
		AudioUtils.cut(resource, outputFile, duration)

		then:
		thrown(IllegalArgumentException)
	}

	def "cut方法 - outputStream为null时抛出NullPointerException"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def resource = new FFmpegResource(audioFile)
		def duration = Duration.ofSeconds(5)

		when:
		AudioUtils.cut(resource, null as File, duration)

		then:
		thrown(NullPointerException)
	}

	def "concat方法 - resources为null时抛出NullPointerException"() {
		given:
		def outputFile = tempDir.resolve("output.mp3").toFile()

		when:
		AudioUtils.concat(null, outputFile)

		then:
		thrown(NullPointerException)
	}

	def "concat方法 - resources为空时抛出IllegalArgumentException"() {
		given:
		def outputFile = tempDir.resolve("output.mp3").toFile()

		when:
		AudioUtils.concat([], outputFile)

		then:
		thrown(IllegalArgumentException)
	}

	def "concat方法 - resources包含null元素时抛出IllegalArgumentException"() {
		given:
		def outputFile = tempDir.resolve("output.mp3").toFile()
		def resources = [null]

		when:
		AudioUtils.concat(resources, outputFile)

		then:
		thrown(IllegalArgumentException)
	}

	def "concat方法 - resources包含非音频类型时抛出IllegalArgumentException"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", "1416529-hd_1920_1080_30fps.webm").toFile()
		def outputFile = tempDir.resolve("output.mp3").toFile()
		def resources = [new FFmpegResource(videoFile)]

		when:
		AudioUtils.concat(resources, outputFile)

		then:
		thrown(IllegalArgumentException)
	}

	def "concat方法 - outputStream为null时抛出NullPointerException"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def resources = [new FFmpegResource(audioFile)]

		when:
		AudioUtils.concat(resources, null as File)

		then:
		thrown(NullPointerException)
	}

	def "addBgm方法 - mainResource为null时抛出NullPointerException"() {
		given:
		def bgmFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp3").toFile()
		def bgmResource = new FFmpegResource(bgmFile)

		when:
		AudioUtils.addBgm(null, bgmResource, outputFile)

		then:
		thrown(NullPointerException)
	}

	def "addBgm方法 - bgmResource为null时抛出NullPointerException"() {
		given:
		def mainFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp3").toFile()
		def mainResource = new FFmpegResource(mainFile)

		when:
		AudioUtils.addBgm(mainResource, null, outputFile)

		then:
		thrown(NullPointerException)
	}

	def "addBgm方法 - mainResource不是音频类型时抛出IllegalArgumentException"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", "1416529-hd_1920_1080_30fps.webm").toFile()
		def bgmFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp3").toFile()
		def mainResource = new FFmpegResource(videoFile)
		def bgmResource = new FFmpegResource(bgmFile)

		when:
		AudioUtils.addBgm(mainResource, bgmResource, outputFile)

		then:
		thrown(IllegalArgumentException)
	}

	def "addBgm方法 - bgmResource不是音频类型时抛出IllegalArgumentException"() {
		given:
		def mainFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def videoFile = Paths.get("src/test/resources/videos", "1416529-hd_1920_1080_30fps.webm").toFile()
		def outputFile = tempDir.resolve("output.mp3").toFile()
		def mainResource = new FFmpegResource(mainFile)
		def bgmResource = new FFmpegResource(videoFile)

		when:
		AudioUtils.addBgm(mainResource, bgmResource, outputFile)

		then:
		thrown(IllegalArgumentException)
	}

	def "addBgm方法 - outputStream为null时抛出NullPointerException"() {
		given:
		def mainFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def bgmFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def mainResource = new FFmpegResource(mainFile)
		def bgmResource = new FFmpegResource(bgmFile)

		when:
		AudioUtils.addBgm(mainResource, bgmResource, null as File)

		then:
		thrown(NullPointerException)
	}
}
