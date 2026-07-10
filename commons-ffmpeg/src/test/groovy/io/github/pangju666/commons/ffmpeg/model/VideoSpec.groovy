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

package io.github.pangju666.commons.ffmpeg.model

import org.bytedeco.javacv.FFmpegFrameGrabber
import spock.lang.Specification

class VideoSpec extends Specification {
	static final String TEST_VIDEOS_DIR = "src/test/resources/videos"
	static final List<String> TEST_VIDEO_FILES = [
		"1416529-hd_1920_1080_30fps.webm",
		"1416529-uhd_2560_1440_30fps.mov",
		"1416529-uhd_2560_1440_30fps.wmv",
		"1416529-uhd_3840_2160_30fps.mp4"
	]

	def "基于现有Video创建Builder"() {
		given:
		def video = Video.mp4WithH264()
			.resolution1080p()
			.frameRate30()
			.bitrate(6000000)
			.build()

		when:
		def builder = Video.builder(video)

		then:
		builder != null
	}

	def "基于现有Video创建Builder - null抛异常"() {
		when:
		Video.builder((Video) null)

		then:
		thrown(NullPointerException)
	}

	def "从FFmpegFrameGrabber创建Builder"() {
		given:
		def grabber = new FFmpegFrameGrabber(new File("${TEST_VIDEOS_DIR}/${TEST_VIDEO_FILES.get(3)}"))

		when:
		def builder = Video.builder(grabber)

		then:
		builder != null
	}

	def "从FFmpegFrameGrabber创建Builder - null抛异常"() {
		when:
		Video.builder((FFmpegFrameGrabber) null)

		then:
		thrown(NullPointerException)
	}

	def "从FFmpegResource创建Builder"() {
		given:
		def testFile = new File("${TEST_VIDEOS_DIR}/${fileName}")
		def resource = new FFmpegResource(testFile)

		when:
		def builder = Video.builder(resource)

		then:
		builder != null

		where:
		fileName << TEST_VIDEO_FILES
	}

	def "从FFmpegResource创建Builder - null抛异常"() {
		when:
		Video.builder((FFmpegResource) null)

		then:
		thrown(NullPointerException)
	}

	def "从InputStream创建Builder"() {
		given:
		def testFile = new File("${TEST_VIDEOS_DIR}/1416529-uhd_3840_2160_30fps.mp4")
		def inputStream = new FileInputStream(testFile)

		when:
		def builder = Video.builder(inputStream)

		then:
		builder != null
	}

	def "从InputStream创建Builder - null抛异常"() {
		when:
		Video.builder((InputStream) null)

		then:
		thrown(NullPointerException)
	}

	def "从FFmpegFrameGrabber解析Video"() {
		given:
		def grabber = new FFmpegFrameGrabber(new File("${TEST_VIDEOS_DIR}/${TEST_VIDEO_FILES[3]}"))

		when:
		def video = Video.parse(grabber)

		then:
		video != null
	}

	def "从FFmpegResource解析Video"() {
		given:
		def testFile = new File("${TEST_VIDEOS_DIR}/${fileName}")
		def resource = new FFmpegResource(testFile)

		when:
		def video = Video.parse(resource)

		then:
		video != null
		video.getDuration() != null
		video.getFrameRate() > 0
		video.getWidth() > 0
		video.getHeight() > 0

		where:
		fileName << TEST_VIDEO_FILES
	}

	def "从FFmpegResource解析Video - null抛异常"() {
		when:
		Video.parse((FFmpegResource) null)

		then:
		thrown(NullPointerException)
	}

	def "从InputStream解析Video"() {
		given:
		def testFile = new File("${TEST_VIDEOS_DIR}/1416529-uhd_3840_2160_30fps.mp4")
		def inputStream = new FileInputStream(testFile)

		when:
		def video = Video.parse(inputStream)

		then:
		video != null
	}

	def "从InputStream解析Video - null抛异常"() {
		when:
		Video.parse((InputStream) null)

		then:
		thrown(NullPointerException)
	}

	def "获取视频时长"() {
		given:
		def video = Video.mp4WithH264()
			.resolution1080p()
			.frameRate30()
			.bitrate(6000000)
			.build()

		when:
		def duration = video.getDuration()

		then:
		duration != null
	}

	def "获取视频帧率"() {
		given:
		def video = Video.mp4WithH264()
			.resolution1080p()
			.frameRate30()
			.bitrate(6000000)
			.build()

		when:
		def frameRate = video.getFrameRate()

		then:
		frameRate > 0
	}

	def "获取视频宽度"() {
		given:
		def video = Video.mp4WithH264()
			.resolution1080p()
			.frameRate30()
			.bitrate(6000000)
			.build()

		when:
		def width = video.getWidth()

		then:
		width == 1920
	}

	def "获取视频高度"() {
		given:
		def video = Video.mp4WithH264()
			.resolution1080p()
			.frameRate30()
			.bitrate(6000000)
			.build()

		when:
		def height = video.getHeight()

		then:
		height == 1080
	}

	def "获取视频码率"() {
		given:
		def video = Video.mp4WithH264()
			.resolution1080p()
			.frameRate30()
			.bitrate(6000000)
			.build()

		when:
		def bitrate = video.getBitrate()

		then:
		bitrate == 6000000
	}

	def "获取视频音频信息"() {
		given:
		def video = Video.mp4WithH264()
			.resolution1080p()
			.frameRate30()
			.bitrate(6000000)
			.audio(Video.AUDIO_AAC_1080P)
			.build()

		when:
		def audio = video.getAudio()

		then:
		audio != null
	}

	def "判断是否包含音频轨道"() {
		given:
		def video = Video.mp4WithH264()
			.resolution1080p()
			.frameRate30()
			.bitrate(6000000)
			.audio(Video.AUDIO_AAC_1080P)
			.build()

		when:
		def hasAudio = video.hasAudio()

		then:
		hasAudio
	}

	def "判断是否有时长信息"() {
		given:
		def video = Video.mp4WithH264()
			.resolution1080p()
			.frameRate30()
			.bitrate(6000000)
			.build()

		when:
		def hasDuration = video.hasDuration()

		then:
		!hasDuration
	}

	def "判断是否为竖屏视频"() {
		given:
		def video = Video.mp4WithH264()
			.resolution1080pVertical()
			.frameRate30()
			.bitrate(6000000)
			.build()

		when:
		def isVertical = video.isVertical()

		then:
		isVertical
	}

	def "判断是否为正方形视频"() {
		given:
		def video = Video.mp4WithH264()
			.resolution(1080, 1080)
			.frameRate30()
			.bitrate(6000000)
			.build()

		when:
		def isSquare = video.isSquare()

		then:
		isSquare
	}

	def "使用预定义MP4_480P配置"() {
		expect:
		Video.MP4_480P != null
	}

	def "使用预定义MP4_720P配置"() {
		expect:
		Video.MP4_720P != null
	}

	def "使用预定义MP4_1080P配置"() {
		expect:
		Video.MP4_1080P != null
	}

	def "使用预定义MP4_2K配置"() {
		expect:
		Video.MP4_2K != null
	}

	def "使用预定义WEBM_480P配置"() {
		expect:
		Video.WEBM_480P != null
	}

	def "使用预定义WEBM_720P配置"() {
		expect:
		Video.WEBM_720P != null
	}

	def "使用预定义WEBM_1080P配置"() {
		expect:
		Video.WEBM_1080P != null
	}

	def "使用预定义WEBM_2K配置"() {
		expect:
		Video.WEBM_2K != null
	}

	def "使用预定义MKV_480P配置"() {
		expect:
		Video.MKV_480P != null
	}

	def "使用预定义MKV_720P配置"() {
		expect:
		Video.MKV_720P != null
	}

	def "使用预定义MKV_1080P配置"() {
		expect:
		Video.MKV_1080P != null
	}

	def "使用预定义MKV_2K配置"() {
		expect:
		Video.MKV_2K != null
	}

	def "Builder设置MP4+H264格式"() {
		when:
		def video = Video.mp4WithH264()
			.build()

		then:
		video != null
	}

	def "Builder设置MP4+H265格式"() {
		when:
		def video = Video.mp4WithH265()
			.build()

		then:
		video != null
	}

	def "Builder设置WEBM+VP9格式"() {
		when:
		def video = Video.webmWithVP9()
			.build()

		then:
		video != null
	}

	def "Builder设置MKV+H264格式"() {
		when:
		def video = Video.mkvWithH264()
			.build()

		then:
		video != null
	}

	def "Builder设置MKV+H265格式"() {
		when:
		def video = Video.mkvWithH265()
			.build()

		then:
		video != null
	}

	def "Builder设置2K横屏分辨率"() {
		when:
		def video = Video.mkvWithH264()
			.resolution2k()
			.build()

		then:
		video.getWidth() == 2560
		video.getHeight() == 1440
	}

	def "Builder设置1080P横屏分辨率"() {
		when:
		def video = Video.mkvWithH264()
			.resolution1080p()
			.build()

		then:
		video.getWidth() == 1920
		video.getHeight() == 1080
	}

	def "Builder设置720P横屏分辨率"() {
		when:
		def video = Video.mkvWithH264()
			.resolution720p()
			.build()

		then:
		video.getWidth() == 1280
		video.getHeight() == 720
	}

	def "Builder设置480P横屏分辨率"() {
		when:
		def video = Video.mkvWithH264()
			.resolution480p()
			.build()

		then:
		video.getWidth() == 640
		video.getHeight() == 480
	}

	def "Builder设置2K竖屏分辨率"() {
		when:
		def video = Video.mkvWithH264()
			.resolution2kVertical()
			.build()

		then:
		video.getWidth() == 1440
		video.getHeight() == 2560
	}

	def "Builder设置1080P竖屏分辨率"() {
		when:
		def video = Video.mkvWithH264()
			.resolution1080pVertical()
			.build()

		then:
		video.getWidth() == 1080
		video.getHeight() == 1920
	}

	def "Builder设置720P竖屏分辨率"() {
		when:
		def video = Video.mkvWithH264()
			.resolution720pVertical()
			.build()

		then:
		video.getWidth() == 720
		video.getHeight() == 1280
	}

	def "Builder设置480P竖屏分辨率"() {
		when:
		def video = Video.mkvWithH264()
			.resolution480pVertical()
			.build()

		then:
		video.getWidth() == 480
		video.getHeight() == 640
	}

	def "Builder设置帧率24"() {
		when:
		def video = Video.mkvWithH264()
			.frameRate24()
			.build()

		then:
		video.getFrameRate() == 24
	}

	def "Builder设置帧率25"() {
		when:
		def video = Video.mkvWithH264()
			.frameRate25()
			.build()

		then:
		video.getFrameRate() == 25
	}

	def "Builder设置帧率30"() {
		when:
		def video = Video.mkvWithH264()
			.frameRate30()
			.build()

		then:
		video.getFrameRate() == 30
	}

	def "Builder设置帧率60"() {
		when:
		def video = Video.mkvWithH264()
			.frameRate60()
			.build()

		then:
		video.getFrameRate() == 60
	}

	def "Builder设置分辨率 - 负数抛异常"() {
		when:
		Video.mkvWithH264()
			.resolution(-1, 1080)

		then:
		thrown(IllegalArgumentException)
	}

	def "Builder设置分辨率 - 零合法"() {
		when:
		def video = Video.mkvWithH264()
			.resolution(0, 0)
			.build()

		then:
		video.getWidth() == 0
		video.getHeight() == 0
	}

	def "Builder设置帧率 - 负数抛异常"() {
		when:
		Video.mkvWithH264()
			.frameRate(-1)

		then:
		thrown(IllegalArgumentException)
	}

	def "Builder设置帧率 - 零抛异常"() {
		when:
		Video.mkvWithH264()
			.frameRate(0)

		then:
		thrown(IllegalArgumentException)
	}

	def "Builder设置码率 - 负数抛异常"() {
		when:
		Video.mkvWithH264()
			.bitrate(-1)

		then:
		thrown(IllegalArgumentException)
	}

	def "Builder设置码率 - 零合法"() {
		when:
		def video = Video.mkvWithH264()
			.bitrate(0)
			.build()

		then:
		video.getBitrate() == 0
	}

	def "Builder按宽度缩放分辨率"() {
		given:
		def video = Video.mkvWithH264()
			.resolution1080p()
			.scaleByWidth(1280)
			.build()

		expect:
		video.getWidth() == 1280
		video.getHeight() == 720
	}

	def "Builder按高度缩放分辨率"() {
		given:
		def video = Video.mkvWithH264()
			.resolution1080p()
			.scaleByHeight(720)
			.build()

		expect:
		video.getWidth() == 1280
		video.getHeight() == 720
	}

	def "Builder按目标宽高缩放分辨率"() {
		given:
		def video = Video.mkvWithH264()
			.resolution1080p()
			.scale(1280, 720)
			.build()

		expect:
		video.getWidth() == 1280
		video.getHeight() == 720
	}

	def "Builder按比例缩放分辨率"() {
		given:
		def video = Video.mkvWithH264()
			.resolution1080p()
			.scale(0.5)
			.build()

		expect:
		video.getWidth() == 960
		video.getHeight() == 540
	}

	def "获取像素格式"() {
		given:
		def video = Video.mp4WithH264()
			.resolution1080p()
			.frameRate30()
			.bitrate(6000000)
			.build()

		when:
		def pixelFormat = video.getPixelFormat()

		then:
		pixelFormat >= -1
	}

	def "Builder设置像素格式"() {
		when:
		def video = Video.mkvWithH264()
			.pixelFormat(1)
			.build()

		then:
		video.getPixelFormat() == 1
	}

	def "Builder设置像素格式 - 小于-1抛异常"() {
		when:
		Video.mkvWithH264()
			.pixelFormat(-2)

		then:
		thrown(IllegalArgumentException)
	}

	def "Builder设置像素格式 - -1合法"() {
		when:
		def video = Video.mkvWithH264()
			.pixelFormat(-1)
			.build()

		then:
		video.getPixelFormat() == -1
	}
}
