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


import io.github.pangju666.commons.io.resource.IOResource
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path

class FFmpegResourceSpec extends Specification {
	@TempDir
	Path tempDir

	static final String TEST_AUDIOS_DIR = "src/test/resources/audios"
	static final String TEST_VIDEOS_DIR = "src/test/resources/videos"
	static final List<String> TEST_AUDIO_FILES = [
		"file_example_MP3_5MG.mp3",
		"suzume_no_tojimari.flac",
		"suzume_no_tojimari.ogg",
		"suzume_no_tojimari.wav"
	]
	static final List<String> TEST_VIDEO_FILES = [
		"1416529-hd_1920_1080_30fps.webm",
		"1416529-uhd_2560_1440_30fps.mov",
		"1416529-uhd_2560_1440_30fps.wmv",
		"1416529-uhd_3840_2160_30fps.mp4"
	]

	def "从音频文件创建FFmpegResource"() {
		given:
		def testFile = new File("${TEST_AUDIOS_DIR}/${fileName}")

		when:
		def resource = new FFmpegResource(testFile)

		then:
		resource != null
		resource.getFile() != null
		resource.isAudio()

		where:
		fileName << TEST_AUDIO_FILES
	}

	def "从视频文件创建FFmpegResource"() {
		given:
		def testFile = new File("${TEST_VIDEOS_DIR}/${fileName}")

		when:
		def resource = new FFmpegResource(testFile)

		then:
		resource != null
		resource.getFile() != null
		resource.isVideo()

		where:
		fileName << TEST_VIDEO_FILES
	}

	def "从字节数组创建FFmpegResource"() {
		given:
		def bytes = "test content".bytes

		when:
		def resource = new FFmpegResource(bytes)

		then:
		resource != null
		resource.getBytes() != null
	}

	def "从输入流创建FFmpegResource"() {
		given:
		def bytes = "test content".bytes
		def inputStream = new ByteArrayInputStream(bytes)

		when:
		def resource = new FFmpegResource(inputStream)

		then:
		resource != null
		resource.openInputStream() != null
	}

	def "从IOResource创建FFmpegResource"() {
		given:
		def testFile = tempDir.resolve("test.txt")
		Files.write(testFile, "test content".bytes)
		def ioResource = new IOResource(testFile.toFile())

		when:
		def resource = new FFmpegResource(ioResource)

		then:
		resource != null
		resource.getFile() != null
	}

	def "从文件路径创建FFmpegResource - 带缓存参数"() {
		given:
		def testFile = tempDir.resolve("test.txt")
		Files.write(testFile, "test content".bytes)

		when:
		def resource = new FFmpegResource(testFile.toString(), true)

		then:
		resource != null
		resource.getFile() != null
	}

	def "从文件对象创建FFmpegResource - 带缓存参数"() {
		given:
		def testFile = tempDir.resolve("test.txt")
		Files.write(testFile, "test content".bytes)

		when:
		def resource = new FFmpegResource(testFile.toFile(), true)

		then:
		resource != null
		resource.getFile() != null
	}

	def "从IOResource创建FFmpegResource - 带缓存参数"() {
		given:
		def testFile = tempDir.resolve("test.txt")
		Files.write(testFile, "test content".bytes)
		def ioResource = new IOResource(testFile.toFile())

		when:
		def resource = new FFmpegResource(ioResource, true)

		then:
		resource != null
		resource.getFile() != null
	}

	def "openFrameGrabber - 文件模式"() {
		given:
		def testFile = tempDir.resolve("test.txt")
		Files.write(testFile, "test content".bytes)
		def resource = new FFmpegResource(testFile.toFile())

		when:
		def grabber = resource.openFrameGrabber()

		then:
		grabber != null
	}

	def "openFrameGrabber - 字节数组模式"() {
		given:
		def bytes = "test content".bytes
		def resource = new FFmpegResource(bytes)

		when:
		def grabber = resource.openFrameGrabber()

		then:
		grabber != null
	}

	def "openFrameGrabber - 输入流模式"() {
		given:
		def bytes = "test content".bytes
		def inputStream = new ByteArrayInputStream(bytes)
		def resource = new FFmpegResource(inputStream)

		when:
		def grabber = resource.openFrameGrabber()

		then:
		grabber != null
	}

	def "从文件路径创建FFmpegResource - null抛异常"() {
		when:
		new FFmpegResource((String) null)

		then:
		thrown(NullPointerException)
	}

	def "从文件对象创建FFmpegResource - null抛异常"() {
		when:
		new FFmpegResource((File) null)

		then:
		thrown(NullPointerException)
	}

	def "从字节数组创建FFmpegResource - null抛异常"() {
		when:
		new FFmpegResource((byte[]) null)

		then:
		thrown(IllegalArgumentException)
	}

	def "从输入流创建FFmpegResource - null抛异常"() {
		when:
		new FFmpegResource((InputStream) null)

		then:
		thrown(NullPointerException)
	}

	def "从IOResource创建FFmpegResource - null抛异常"() {
		when:
		new FFmpegResource((IOResource) null)

		then:
		thrown(NullPointerException)
	}
}
