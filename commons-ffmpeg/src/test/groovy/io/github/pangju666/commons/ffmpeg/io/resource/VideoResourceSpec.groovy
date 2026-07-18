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

class VideoResourceSpec extends Specification {
	@TempDir
	Path tempDir

	def videoFiles = [
		"1416529-hd_1920_1080_30fps.webm",
		"1416529-uhd_2560_1440_30fps.mov",
		"1416529-uhd_2560_1440_30fps.wmv",
		"1416529-uhd_3840_2160_30fps.mp4"
	]

	def "从文件路径构造 - 有效视频文件"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()

		when:
		def resource = new VideoResource(videoFile.absolutePath)

		then:
		resource != null
		resource.isVideo()
	}

	def "从文件路径构造 - 无效视频文件"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", "file_example_MP3_5MG.mp3").toFile()

		when:
		new VideoResource(audioFile.absolutePath)

		then:
		thrown(UnsupportedResourceException)
	}

	def "从File对象构造 - 有效视频文件"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()

		when:
		def resource = new VideoResource(videoFile)

		then:
		resource != null
		resource.isVideo()
	}

	def "从File对象构造 - 无效视频文件"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", "file_example_MP3_5MG.mp3").toFile()

		when:
		new VideoResource(audioFile)

		then:
		thrown(UnsupportedResourceException)
	}

	def "从字节数组构造 - 有效视频数据"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def videoBytes = Files.readAllBytes(videoFile.toPath())

		when:
		def resource = new VideoResource(videoBytes)

		then:
		resource != null
		resource.isVideo()
	}

	def "从字节数组构造 - 无效视频数据"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", "file_example_MP3_5MG.mp3").toFile()
		def audioBytes = Files.readAllBytes(audioFile.toPath())

		when:
		new VideoResource(audioBytes)

		then:
		thrown(UnsupportedResourceException)
	}

	def "从IOResource构造 - 有效视频资源"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def ioResource = new IOResource(videoFile)

		when:
		def resource = new VideoResource(ioResource)

		then:
		resource != null
		resource.isVideo()
	}

	def "从IOResource构造 - VideoResource复用视频信息"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def videoResource1 = new VideoResource(videoFile)
		videoResource1.getVideo() // 触发延迟加载
		def ioResource = new IOResource(videoFile)

		when:
		def videoResource2 = new VideoResource(ioResource)

		then:
		videoResource2 != null
		videoResource2.isVideo()
	}

	def "从输入流构造 - 有效视频数据"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		try (def inputStream = Files.newInputStream(videoFile.toPath())) {

			when:
			def resource = new VideoResource(inputStream)

			then:
			resource != null
			resource.isVideo()
		}
	}

	def "从输入流构造 - 无效视频数据"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", "file_example_MP3_5MG.mp3").toFile()
		def inputStream = Files.newInputStream(audioFile.toPath())

		when:
		new VideoResource(inputStream)

		then:
		thrown(UnsupportedResourceException)
	}

	def "getVideo方法 - 延迟加载视频信息"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new VideoResource(videoFile)

		when:
		def video = resource.getVideo()

		then:
		video != null
		video.getImageWidth() > 0
		video.getImageHeight() > 0
		video.getFrameRate() > 0
	}

	def "getVideo方法 - 多次调用返回相同实例"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new VideoResource(videoFile)

		when:
		def video1 = resource.getVideo()
		def video2 = resource.getVideo()

		then:
		video1 == video2
		video1.is(video2)
	}

	def "openFrameGrabber方法 - 成功打开帧抓取器"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new VideoResource(videoFile)

		when:
		def grabber = resource.openFrameGrabber()

		then:
		grabber != null
		grabber.hasVideo()
		grabber.imageWidth > 0
		grabber.imageHeight > 0
		grabber.frameRate > 0

		cleanup:
		grabber?.close()
	}

	def "openFrameGrabber方法 - 触发视频信息延迟加载"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new VideoResource(videoFile)

		when:
		resource.openFrameGrabber()
		def video = resource.getVideo()

		then:
		video != null
		video.getImageWidth() > 0
		video.getImageHeight() > 0
	}

	def "isVideo方法 - 视频文件返回true"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new VideoResource(videoFile)

		expect:
		resource.isVideo()
	}

	def "close方法 - 关闭资源后无法获取视频信息"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new VideoResource(videoFile)
		resource.close()

		when:
		resource.getVideo()

		then:
		thrown(IllegalStateException)
	}

	def "close方法 - 关闭资源后无法打开帧抓取器"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new VideoResource(videoFile)
		resource.close()

		when:
		resource.openFrameGrabber()

		then:
		thrown(IllegalStateException)
	}
}
