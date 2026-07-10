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


import io.github.pangju666.commons.ffmpeg.model.FFmpegResource
import io.github.pangju666.commons.ffmpeg.model.Video
import io.github.pangju666.commons.ffmpeg.utils.VideoUtils
import io.github.pangju666.commons.io.model.IOResource
import spock.lang.Specification
import spock.lang.TempDir

import java.awt.image.BufferedImage
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.function.Function
import java.util.function.ObjLongConsumer

class VideoUtilsSpec extends Specification {
	@TempDir
	Path tempDir

	def videoFiles = [
		"1416529-hd_1920_1080_30fps.webm",
		"1416529-uhd_2560_1440_30fps.mov",
		"1416529-uhd_2560_1440_30fps.wmv",
		"1416529-uhd_3840_2160_30fps.mp4"
	]

	def audioFiles = [
		"file_example_MP3_5MG.mp3",
		"suzume_no_tojimari.flac",
		"suzume_no_tojimari.ogg",
		"suzume_no_tojimari.wav"
	]

	def "转码到文件 - 指定输出配置"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def resource = new FFmpegResource(videoFile)
		def outputVideo = Video.MP4_1080P

		when:
		VideoUtils.transcode(resource, outputFile, outputVideo)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "转码到输出流 - 指定输出配置"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new FFmpegResource(videoFile)
		def outputStream = tempDir.resolve("output.mp4").toFile().newOutputStream()
		def outputVideo = Video.WEBM_1080P

		when:
		VideoUtils.transcode(resource, outputStream, outputVideo)

		then:
		noExceptionThrown()
	}

	def "提取视频流 - 使用源配置"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def resource = new FFmpegResource(videoFile)

		when:
		VideoUtils.extractVideo(resource, outputFile)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "提取视频流 - 指定输出配置"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def resource = new FFmpegResource(videoFile)
		def outputVideo = Video.MP4_1080P

		when:
		VideoUtils.extractVideo(resource, outputFile, outputVideo)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "提取视频流到输出流 - 使用源配置"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new FFmpegResource(videoFile)
		def outputStream = tempDir.resolve("output.mp4").toFile().newOutputStream()

		when:
		VideoUtils.extractVideo(resource, outputStream)
		outputStream.close()

		then:
		noExceptionThrown()
	}

	def "提取音频流 - 指定输出配置"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos/video_with_audio.mp4").toFile()
		def outputFile = tempDir.resolve("output.mp3").toFile()
		def resource = new FFmpegResource(videoFile)
		def outputAudio = Video.AUDIO_AAC_1080P

		when:
		VideoUtils.extractAudio(resource, outputFile, outputAudio)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "裁剪到文件 - 从开头裁剪指定时长"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def resource = new FFmpegResource(videoFile)
		def duration = Duration.ofSeconds(5)

		when:
		VideoUtils.cut(resource, outputFile, duration)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "裁剪到文件 - 指定时间段"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def resource = new FFmpegResource(videoFile)
		def start = Duration.ofSeconds(2)
		def end = Duration.ofSeconds(7)

		when:
		VideoUtils.cut(resource, outputFile, start, end)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "裁剪到输出流 - 从开头裁剪指定时长"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new FFmpegResource(videoFile)
		def outputStream = tempDir.resolve("output.mp4").toFile().newOutputStream()
		def duration = Duration.ofSeconds(5)

		when:
		VideoUtils.cut(resource, outputStream, duration)

		then:
		noExceptionThrown()
	}

	def "裁剪到输出流 - 指定时间段"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new FFmpegResource(videoFile)
		def outputStream = tempDir.resolve("output.mp4").toFile().newOutputStream()
		def start = Duration.ofSeconds(2)
		def end = Duration.ofSeconds(7)

		when:
		VideoUtils.cut(resource, outputStream, start, end)

		then:
		noExceptionThrown()
	}

	def "拼接 - 使用源配置"() {
		given:
		def videoFile1 = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def videoFile2 = Paths.get("src/test/resources/videos", videoFiles[1]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def resources = [new FFmpegResource(videoFile1), new FFmpegResource(videoFile2)]

		when:
		VideoUtils.concat(resources, outputFile)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "拼接 - 指定输出配置"() {
		given:
		def videoFile1 = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def videoFile2 = Paths.get("src/test/resources/videos", videoFiles[1]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def resources = [new FFmpegResource(videoFile1), new FFmpegResource(videoFile2)]
		def outputVideo = Video.MP4_1080P

		when:
		VideoUtils.concat(resources, outputFile, outputVideo)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "拼接到输出流 - 使用源配置"() {
		given:
		def videoFile1 = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def videoFile2 = Paths.get("src/test/resources/videos", videoFiles[1]).toFile()
		def outputStream = tempDir.resolve("output.mp4").toFile().newOutputStream()
		def resources = [new FFmpegResource(videoFile1), new FFmpegResource(videoFile2)]

		when:
		VideoUtils.concat(resources, outputStream)

		then:
		noExceptionThrown()
	}

	def "调整速度到文件 - 使用源配置"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def resource = new FFmpegResource(videoFile)

		when:
		VideoUtils.adjustSpeed(resource, outputFile, 2.0f)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "调整速度到文件 - 指定输出配置"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def resource = new FFmpegResource(videoFile)
		def outputVideo = Video.MP4_1080P

		when:
		VideoUtils.adjustSpeed(resource, outputFile, 1.5f, outputVideo)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "调整速度到输出流 - 使用源配置"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new FFmpegResource(videoFile)
		def outputStream = tempDir.resolve("output.mp4").toFile().newOutputStream()

		when:
		VideoUtils.adjustSpeed(resource, outputStream, 2.0f)

		then:
		noExceptionThrown()
	}

	def "抓取图像到文件 - 自动检测格式"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.jpg").toFile()
		def resource = new FFmpegResource(videoFile)
		def timestamp = Duration.ofSeconds(5)

		when:
		VideoUtils.grabImageAtTimestamp(resource, timestamp, outputFile)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "抓取图像到文件 - 指定格式"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.png").toFile()
		def resource = new FFmpegResource(videoFile)
		def timestamp = Duration.ofSeconds(5)

		when:
		VideoUtils.grabImageAtTimestamp(resource, timestamp, outputFile, "png")

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "抓取图像到OutputStream"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new FFmpegResource(videoFile)
		def outputStream = tempDir.resolve("output.jpg").toFile().newOutputStream()
		def timestamp = Duration.ofSeconds(5)

		when:
		VideoUtils.grabImageAtTimestamp(resource, timestamp, outputStream, "jpg")

		then:
		noExceptionThrown()
	}

	def "抓取图像返回BufferedImage"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new FFmpegResource(videoFile)
		def timestamp = Duration.ofSeconds(5)

		when:
		BufferedImage image = VideoUtils.grabImageAtTimestamp(resource, timestamp)

		then:
		image != null
		image.width > 0
		image.height > 0
	}

	def "按间隔抓取图像列表"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new FFmpegResource(videoFile)
		def timeUnit = TimeUnit.SECONDS

		when:
		List<BufferedImage> images = VideoUtils.grabImagePeriodically(resource, 5, timeUnit)

		then:
		images != null
		images.size() > 0
	}

	def "按间隔抓取图像回调"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new FFmpegResource(videoFile)
		def timeUnit = TimeUnit.SECONDS
		def count = 0
		def consumer = { BufferedImage image, long timestamp ->
			count++
		} as ObjLongConsumer<BufferedImage>

		when:
		VideoUtils.grabImagePeriodically(resource, 5, timeUnit, consumer)

		then:
		count > 0
	}

	def "按间隔抓取图像保存到目录 - 默认格式化器"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new FFmpegResource(videoFile)
		def timeUnit = TimeUnit.SECONDS
		def outputDir = tempDir.resolve("images").toFile()

		when:
		VideoUtils.grabImagePeriodically(resource, 5, timeUnit, "jpg", outputDir)

		then:
		outputDir.exists()
		outputDir.listFiles().length > 0
	}

	def "按间隔抓取图像保存到目录 - 自定义格式化器"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new FFmpegResource(videoFile)
		def timeUnit = TimeUnit.SECONDS
		def outputDir = tempDir.resolve("images").toFile()
		def formatter = { long timestamp -> "frame_${timestamp}" as String } as Function<Long, String>

		when:
		VideoUtils.grabImagePeriodically(resource, 5, timeUnit, "jpg", outputDir, formatter)

		then:
		outputDir.exists()
		outputDir.listFiles().length > 0
	}

	def "按矩形裁剪到文件 - 输出裁剪分辨率"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def resource = new FFmpegResource(videoFile)

		when:
		VideoUtils.cropByRect(resource, outputFile, 100, 100, 640, 480)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "按矩形裁剪到文件 - 指定输出配置"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def resource = new FFmpegResource(videoFile)
		def outputVideo = Video.MP4_1080P

		when:
		VideoUtils.cropByRect(resource, outputFile, 100, 100, 640, 480, outputVideo)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "按矩形裁剪到输出流"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new FFmpegResource(videoFile)
		def outputStream = tempDir.resolve("output.mp4").toFile().newOutputStream()

		when:
		VideoUtils.cropByRect(resource, outputStream, 100, 100, 640, 480)

		then:
		noExceptionThrown()
	}

	def "按偏移裁剪到文件 - 输出裁剪分辨率"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def resource = new FFmpegResource(videoFile)

		when:
		VideoUtils.cropByOffset(resource, outputFile, 10, 10, 10, 10)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "按偏移裁剪到文件 - 指定输出配置"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def resource = new FFmpegResource(videoFile)
		def outputVideo = Video.MP4_1080P

		when:
		VideoUtils.cropByOffset(resource, outputFile, 10, 10, 10, 10, outputVideo)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "按偏移裁剪到输出流"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new FFmpegResource(videoFile)
		def outputStream = tempDir.resolve("output.mp4").toFile().newOutputStream()

		when:
		VideoUtils.cropByOffset(resource, outputStream, 10, 10, 10, 10)

		then:
		noExceptionThrown()
	}

	def "按中心裁剪到文件 - 输出裁剪分辨率"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def resource = new FFmpegResource(videoFile)

		when:
		VideoUtils.cropByCenter(resource, outputFile, 640, 480)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "按中心裁剪到文件 - 指定输出配置"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def resource = new FFmpegResource(videoFile)
		def outputVideo = Video.MP4_1080P

		when:
		VideoUtils.cropByCenter(resource, outputFile, 640, 480, outputVideo)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "按中心裁剪到输出流"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new FFmpegResource(videoFile)
		def outputStream = tempDir.resolve("output.mp4").toFile().newOutputStream()

		when:
		VideoUtils.cropByCenter(resource, outputStream, 640, 480)

		then:
		noExceptionThrown()
	}

	def "替换音频 - 不循环填充"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[2]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def videoResource = new FFmpegResource(videoFile)
		def audioResource = new FFmpegResource(audioFile)

		when:
		VideoUtils.replaceAudio(videoResource, audioResource, outputFile)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "替换音频 - 循环填充"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[2]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def videoResource = new FFmpegResource(videoFile)
		def audioResource = new FFmpegResource(audioFile)

		when:
		VideoUtils.replaceAudio(videoResource, audioResource, outputFile, true)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "替换音频 - 指定输出配置"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def videoResource = new FFmpegResource(videoFile)
		def audioResource = new FFmpegResource(audioFile)
		def outputVideo = Video.MP4_1080P

		when:
		VideoUtils.replaceAudio(videoResource, audioResource, outputFile, outputVideo, false)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "替换音频到输出流"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[2]).toFile()
		def outputStream = tempDir.resolve("output.mp4").toFile().newOutputStream()
		def videoResource = new FFmpegResource(videoFile)
		def audioResource = new FFmpegResource(audioFile)

		when:
		VideoUtils.replaceAudio(videoResource, audioResource, outputStream)

		then:
		noExceptionThrown()
	}

	def "添加背景音乐 - 使用默认权重和源配置"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def bgmFile = Paths.get("src/test/resources/audios", audioFiles[2]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def videoResource = new FFmpegResource(videoFile)
		def bgmResource = new FFmpegResource(bgmFile)

		when:
		VideoUtils.addBgm(videoResource, bgmResource, outputFile)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "添加背景音乐 - 指定权重和输出配置"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def bgmFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def videoResource = new FFmpegResource(videoFile)
		def bgmResource = new FFmpegResource(bgmFile)
		def outputVideo = Video.MP4_1080P

		when:
		VideoUtils.addBgm(videoResource, bgmResource, outputFile, outputVideo, 0.3f)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "添加背景音乐到输出流 - 使用默认权重和源配置"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def bgmFile = Paths.get("src/test/resources/audios", audioFiles[2]).toFile()
		def outputStream = tempDir.resolve("output.mp4").toFile().newOutputStream()
		def videoResource = new FFmpegResource(videoFile)
		def bgmResource = new FFmpegResource(bgmFile)

		when:
		VideoUtils.addBgm(videoResource, bgmResource, outputStream)

		then:
		noExceptionThrown()
	}

	def "cut方法 - resource为null时抛出NullPointerException"() {
		given:
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def duration = Duration.ofSeconds(5)

		when:
		VideoUtils.cut(null, outputFile, duration)

		then:
		thrown(NullPointerException)
	}

	def "cut方法 - resource不是视频类型时抛出IllegalArgumentException"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def resource = new FFmpegResource(audioFile)
		def duration = Duration.ofSeconds(5)

		when:
		VideoUtils.cut(resource, outputFile, duration)

		then:
		thrown(IllegalArgumentException)
	}

	def "cut方法 - outputStream为null时抛出NullPointerException"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new FFmpegResource(videoFile)
		def duration = Duration.ofSeconds(5)

		when:
		VideoUtils.cut(resource, null as OutputStream, duration)

		then:
		thrown(NullPointerException)
	}

	def "concat方法 - resources为null时抛出NullPointerException"() {
		given:
		def outputFile = tempDir.resolve("output.mp4").toFile()

		when:
		VideoUtils.concat(null, outputFile)

		then:
		thrown(NullPointerException)
	}

	def "concat方法 - resources为空时抛出IllegalArgumentException"() {
		given:
		def outputFile = tempDir.resolve("output.mp4").toFile()

		when:
		VideoUtils.concat([], outputFile)

		then:
		thrown(IllegalArgumentException)
	}

	def "concat方法 - resources包含null元素时抛出NullPointerException"() {
		given:
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def resources = [null]

		when:
		VideoUtils.concat(resources, outputFile)

		then:
		thrown(IllegalArgumentException)
	}

	def "concat方法 - resources包含非视频类型时抛出IllegalArgumentException"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def resources = [new FFmpegResource(audioFile)]

		when:
		VideoUtils.concat(resources, outputFile)

		then:
		thrown(IllegalArgumentException)
	}

	def "concat方法 - outputStream为null时抛出NullPointerException"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resources = [new FFmpegResource(videoFile)]

		when:
		VideoUtils.concat(resources, null as OutputStream)

		then:
		thrown(NullPointerException)
	}

	def "adjustSpeed方法 - resource为null时抛出NullPointerException"() {
		given:
		def outputFile = tempDir.resolve("output.mp4").toFile()

		when:
		VideoUtils.adjustSpeed(null, outputFile, 2.0f)

		then:
		thrown(NullPointerException)
	}

	def "adjustSpeed方法 - resource不是视频类型时抛出IllegalArgumentException"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def resource = new FFmpegResource(audioFile)

		when:
		VideoUtils.adjustSpeed(resource, outputFile, 2.0f)

		then:
		thrown(IllegalArgumentException)
	}

	def "adjustSpeed方法 - outputStream为null时抛出NullPointerException"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new FFmpegResource(videoFile)

		when:
		VideoUtils.adjustSpeed(resource, null as OutputStream, 2.0f)

		then:
		thrown(NullPointerException)
	}

	def "grabImageAtTimestamp方法 - resource为null时抛出NullPointerException"() {
		given:
		def outputFile = tempDir.resolve("output.jpg").toFile()
		def timestamp = Duration.ofSeconds(5)

		when:
		VideoUtils.grabImageAtTimestamp(null, timestamp, outputFile)

		then:
		thrown(NullPointerException)
	}

	def "grabImageAtTimestamp方法 - resource不是视频类型时抛出IllegalArgumentException"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.jpg").toFile()
		def resource = new FFmpegResource(audioFile)
		def timestamp = Duration.ofSeconds(5)

		when:
		VideoUtils.grabImageAtTimestamp(resource, timestamp, outputFile)

		then:
		thrown(IllegalArgumentException)
	}

	def "grabImageAtTimestamp方法 - outputStream为null时抛出NullPointerException"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new FFmpegResource(videoFile)
		def timestamp = Duration.ofSeconds(5)

		when:
		VideoUtils.grabImageAtTimestamp(resource, timestamp, null, "jpg")

		then:
		thrown(NullPointerException)
	}

	def "cropByRect方法 - resource为null时抛出NullPointerException"() {
		given:
		def outputFile = tempDir.resolve("output.mp4").toFile()

		when:
		VideoUtils.cropByRect(null, outputFile, 100, 100, 640, 480)

		then:
		thrown(NullPointerException)
	}

	def "cropByRect方法 - resource不是视频类型时抛出IllegalArgumentException"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def resource = new FFmpegResource(audioFile)

		when:
		VideoUtils.cropByRect(resource, outputFile, 100, 100, 640, 480)

		then:
		thrown(IllegalArgumentException)
	}

	def "cropByRect方法 - outputStream为null时抛出NullPointerException"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new FFmpegResource(videoFile)

		when:
		VideoUtils.cropByRect(resource, null as OutputStream, 100, 100, 640, 480)

		then:
		thrown(NullPointerException)
	}

	def "cropByOffset方法 - resource为null时抛出NullPointerException"() {
		given:
		def outputFile = tempDir.resolve("output.mp4").toFile()

		when:
		VideoUtils.cropByOffset(null, outputFile, 10, 10, 10, 10)

		then:
		thrown(NullPointerException)
	}

	def "cropByOffset方法 - resource不是视频类型时抛出IllegalArgumentException"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def resource = new FFmpegResource(audioFile)

		when:
		VideoUtils.cropByOffset(resource, outputFile, 10, 10, 10, 10)

		then:
		thrown(IllegalArgumentException)
	}

	def "cropByOffset方法 - outputStream为null时抛出NullPointerException"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new FFmpegResource(videoFile)

		when:
		VideoUtils.cropByOffset(resource, null as OutputStream, 10, 10, 10, 10)

		then:
		thrown(NullPointerException)
	}

	def "cropByCenter方法 - resource为null时抛出NullPointerException"() {
		given:
		def outputFile = tempDir.resolve("output.mp4").toFile()

		when:
		VideoUtils.cropByCenter(null, outputFile, 640, 480)

		then:
		thrown(NullPointerException)
	}

	def "cropByCenter方法 - resource不是视频类型时抛出IllegalArgumentException"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def resource = new FFmpegResource(audioFile)

		when:
		VideoUtils.cropByCenter(resource, outputFile, 640, 480)

		then:
		thrown(IllegalArgumentException)
	}

	def "cropByCenter方法 - outputStream为null时抛出NullPointerException"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new FFmpegResource(videoFile)

		when:
		VideoUtils.cropByCenter(resource, null as OutputStream, 640, 480)

		then:
		thrown(NullPointerException)
	}

	def "replaceAudio方法 - videoResource为null时抛出NullPointerException"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def audioResource = new FFmpegResource(audioFile)

		when:
		VideoUtils.replaceAudio(null, audioResource, outputFile)

		then:
		thrown(NullPointerException)
	}

	def "replaceAudio方法 - audioResource为null时抛出NullPointerException"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def videoResource = new FFmpegResource(videoFile)

		when:
		VideoUtils.replaceAudio(videoResource, null, outputFile)

		then:
		thrown(NullPointerException)
	}

	def "replaceAudio方法 - videoResource不是视频类型时抛出IllegalArgumentException"() {
		given:
		def audioFile1 = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def audioFile2 = Paths.get("src/test/resources/audios", audioFiles[1]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def videoResource = new FFmpegResource(audioFile1)
		def audioResource = new FFmpegResource(audioFile2)

		when:
		VideoUtils.replaceAudio(videoResource, audioResource, outputFile)

		then:
		thrown(IllegalArgumentException)
	}

	def "replaceAudio方法 - audioResource不是音频类型时抛出IllegalArgumentException"() {
		given:
		def videoFile1 = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def videoFile2 = Paths.get("src/test/resources/videos", videoFiles[1]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def videoResource = new FFmpegResource(videoFile1)
		def audioResource = new FFmpegResource(videoFile2)

		when:
		VideoUtils.replaceAudio(videoResource, audioResource, outputFile)

		then:
		thrown(IllegalArgumentException)
	}

	def "replaceAudio方法 - outputStream为null时抛出NullPointerException"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def videoResource = new FFmpegResource(videoFile)
		def audioResource = new FFmpegResource(audioFile)

		when:
		VideoUtils.replaceAudio(videoResource, audioResource, null as OutputStream)

		then:
		thrown(NullPointerException)
	}

	def "addBgm方法 - videoResource为null时抛出NullPointerException"() {
		given:
		def bgmFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def bgmResource = new FFmpegResource(bgmFile)

		when:
		VideoUtils.addBgm(null, bgmResource, outputFile)

		then:
		thrown(NullPointerException)
	}

	def "addBgm方法 - bgmResource为null时抛出NullPointerException"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def videoResource = new FFmpegResource(videoFile)

		when:
		VideoUtils.addBgm(videoResource, null, outputFile)

		then:
		thrown(NullPointerException)
	}

	def "addBgm方法 - videoResource不是视频类型时抛出IllegalArgumentException"() {
		given:
		def audioFile1 = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def audioFile2 = Paths.get("src/test/resources/audios", audioFiles[1]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def videoResource = new FFmpegResource(audioFile1)
		def bgmResource = new FFmpegResource(audioFile2)

		when:
		VideoUtils.addBgm(videoResource, bgmResource, outputFile)

		then:
		thrown(IllegalArgumentException)
	}

	def "addBgm方法 - bgmResource不是音频类型时抛出IllegalArgumentException"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def videoFile2 = Paths.get("src/test/resources/videos", videoFiles[1]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def videoResource = new FFmpegResource(videoFile)
		def bgmResource = new FFmpegResource(videoFile2)

		when:
		VideoUtils.addBgm(videoResource, bgmResource, outputFile)

		then:
		thrown(IllegalArgumentException)
	}

	def "addBgm方法 - outputStream为null时抛出NullPointerException"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def bgmFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def videoResource = new FFmpegResource(videoFile)
		def bgmResource = new FFmpegResource(bgmFile)

		when:
		VideoUtils.addBgm(videoResource, bgmResource, null as OutputStream)

		then:
		thrown(NullPointerException)
	}

	def "addTextWatermark方法 - resource为null时抛出NullPointerException"() {
		given:
		def outputFile = tempDir.resolve("output.mp4").toFile()

		when:
		VideoUtils.addTextWatermark(null, outputFile, "test", "Arial")

		then:
		thrown(NullPointerException)
	}

	def "addTextWatermark方法 - resource不是视频类型时抛出IllegalArgumentException"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def resource = new FFmpegResource(audioFile)

		when:
		VideoUtils.addTextWatermark(resource, outputFile, "test", "Arial")

		then:
		thrown(IllegalArgumentException)
	}

	def "addTextWatermark方法 - outputStream为null时抛出NullPointerException"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new FFmpegResource(videoFile)

		when:
		VideoUtils.addTextWatermark(resource, null as OutputStream, "test", "Arial")

		then:
		thrown(NullPointerException)
	}

	def "addImageWatermark方法 - resource为null时抛出NullPointerException"() {
		given:
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def imageFile = Paths.get("src/test/resources/images", "watermark.png").toFile()
		def watermarkImage = new IOResource(imageFile)

		when:
		VideoUtils.addImageWatermark(null, outputFile, watermarkImage)

		then:
		thrown(NullPointerException)
	}

	def "addImageWatermark方法 - resource不是视频类型时抛出IllegalArgumentException"() {
		given:
		def audioFile = Paths.get("src/test/resources/audios", audioFiles[0]).toFile()
		def outputFile = tempDir.resolve("output.mp4").toFile()
		def resource = new FFmpegResource(audioFile)
		def imageFile = Paths.get("src/test/resources/images", "watermark.png").toFile()
		def watermarkImage = new IOResource(imageFile)

		when:
		VideoUtils.addImageWatermark(resource, outputFile, watermarkImage)

		then:
		thrown(IllegalArgumentException)
	}

	def "addImageWatermark方法 - outputStream为null时抛出NullPointerException"() {
		given:
		def videoFile = Paths.get("src/test/resources/videos", videoFiles[0]).toFile()
		def resource = new FFmpegResource(videoFile)
		def imageFile = Paths.get("src/test/resources/images", "watermark.png").toFile()
		def watermarkImage = new IOResource(imageFile)

		when:
		VideoUtils.addImageWatermark(resource, null as OutputStream, watermarkImage)

		then:
		thrown(NullPointerException)
	}
}
