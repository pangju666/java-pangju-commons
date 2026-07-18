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

package io.github.pangju666.commons.ffmpeg.io.resource

import io.github.pangju666.commons.io.exception.UnsupportedResourceException
import io.github.pangju666.commons.io.resource.IOResource
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class AudioResourceSpec extends Specification {
	@TempDir
	Path tempDir

	def audioFiles = [
		"file_example_MP3_5MG.mp3",
		"suzume_no_tojimari.flac",
		"suzume_no_tojimari.ogg",
		"suzume_no_tojimari.wav"
	]

	def "从文件路径构造 - 有效音频文件"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()

		when:
		def resource = new AudioResource(audioFile.absolutePath)

		then:
		resource != null
		resource.isAudio()
	}

	def "从文件路径构造 - 无效音频文件"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", "1416529-hd_1920_1080_30fps.webm").toFile()

		when:
		new AudioResource(videoFile.absolutePath)

		then:
		thrown(UnsupportedResourceException)
	}

	def "从File对象构造 - 有效音频文件"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()

		when:
		def resource = new AudioResource(audioFile)

		then:
		resource != null
		resource.isAudio()
	}

	def "从File对象构造 - 无效音频文件"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", "1416529-hd_1920_1080_30fps.webm").toFile()

		when:
		new AudioResource(videoFile)

		then:
		thrown(UnsupportedResourceException)
	}

	def "从字节数组构造 - 有效音频数据"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def audioBytes = Files.readAllBytes(audioFile.toPath())

		when:
		def resource = new AudioResource(audioBytes)

		then:
		resource != null
		resource.isAudio()
	}

	def "从字节数组构造 - 无效音频数据"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", "1416529-hd_1920_1080_30fps.webm").toFile()
		def videoBytes = Files.readAllBytes(videoFile.toPath())

		when:
		new AudioResource(videoBytes)

		then:
		thrown(UnsupportedResourceException)
	}

	def "从IOResource构造 - 有效音频资源"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def ioResource = new IOResource(audioFile)

		when:
		def resource = new AudioResource(ioResource)

		then:
		resource != null
		resource.isAudio()
	}

	def "从IOResource构造 - AudioResource复用音频信息"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def audioResource1 = new AudioResource(audioFile)
		audioResource1.getAudio() // 触发延迟加载
		def ioResource = new IOResource(audioFile)

		when:
		def audioResource2 = new AudioResource(ioResource)

		then:
		audioResource2 != null
		audioResource2.isAudio()
	}

	def "从输入流构造 - 有效音频数据"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		try (def inputStream = Files.newInputStream(audioFile.toPath())) {

			when:
			def resource = new AudioResource(inputStream)

			then:
			resource != null
			resource.isAudio()
		}
	}

	def "从输入流构造 - 无效音频数据"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", "1416529-hd_1920_1080_30fps.webm").toFile()
		def inputStream = Files.newInputStream(videoFile.toPath())

		when:
		new AudioResource(inputStream)

		then:
		thrown(UnsupportedResourceException)
	}

	def "getAudio方法 - 延迟加载音频信息"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def resource = new AudioResource(audioFile)

		when:
		def audio = resource.getAudio()

		then:
		audio != null
		audio.getSampleRate() > 0
		audio.getChannels() > 0
	}

	def "getAudio方法 - 多次调用返回相同实例"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def resource = new AudioResource(audioFile)

		when:
		def audio1 = resource.getAudio()
		def audio2 = resource.getAudio()

		then:
		audio1 == audio2
		audio1.is(audio2)
	}

	def "openFrameGrabber方法 - 成功打开帧抓取器"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def resource = new AudioResource(audioFile)

		when:
		def grabber = resource.openFrameGrabber()

		then:
		grabber != null
		grabber.hasAudio()
		grabber.audioChannels > 0
		grabber.sampleRate > 0

		cleanup:
		grabber?.close()
	}

	def "openFrameGrabber方法 - 触发音频信息延迟加载"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def resource = new AudioResource(audioFile)

		when:
		resource.openFrameGrabber()
		def audio = resource.getAudio()

		then:
		audio != null
		audio.getSampleRate() > 0
	}

	def "isAudio方法 - 音频文件返回true"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def resource = new AudioResource(audioFile)

		expect:
		resource.isAudio()
	}

	def "close方法 - 关闭资源后无法获取音频信息"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def resource = new AudioResource(audioFile)
		resource.close()

		when:
		resource.getAudio()

		then:
		thrown(IllegalStateException)
	}

	def "close方法 - 关闭资源后无法打开帧抓取器"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def resource = new AudioResource(audioFile)
		resource.close()

		when:
		resource.openFrameGrabber()

		then:
		thrown(IllegalStateException)
	}
}
