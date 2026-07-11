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

package io.github.pangju666.commons.ffmpeg.utils


import spock.lang.Specification

import java.time.Duration

class FFmpegUtilsSpec extends Specification {
	def "获取安全的文件源路径 - Windows路径"() {
		when:
		def result = FFmpegUtils.getSafeFileSourcePath("C:\\Users\\test\\file.png")

		then:
		result == "C\\:/Users/test/file.png"
	}

	def "获取安全的文件源路径 - Unix路径"() {
		when:
		def result = FFmpegUtils.getSafeFileSourcePath("/home/user/file.png")

		then:
		result == "/home/user/file.png"
	}

	def "获取安全的文件源路径 - 空字符串抛异常"() {
		when:
		FFmpegUtils.getSafeFileSourcePath("")

		then:
		thrown(IllegalArgumentException)
	}

	def "获取安全的文件源路径 - null抛异常"() {
		when:
		FFmpegUtils.getSafeFileSourcePath(null)

		then:
		thrown(NullPointerException)
	}

	def "将Duration转换为微秒时间戳"() {
		given:
		def duration = Duration.ofSeconds(1)

		when:
		def timestamp = FFmpegUtils.toTimestamp(duration)

		then:
		timestamp == 1000000L
	}

	def "将Duration转换为微秒时间戳 - null抛异常"() {
		when:
		FFmpegUtils.toTimestamp(null)

		then:
		thrown(NullPointerException)
	}

	def "将Duration转换为微秒时间戳 - 负数抛异常"() {
		given:
		def duration = Duration.ofSeconds(-1)

		when:
		FFmpegUtils.toTimestamp(duration)

		then:
		thrown(IllegalArgumentException)
	}

	def "获取音量滤镜 - 浮点精度"() {
		when:
		def filter = FFmpegUtils.getVolumeFilter(3.5f)

		then:
		filter == "volume=volume=+3.5000dB:precision=float"
	}

	def "获取音量滤镜 - 负分贝"() {
		when:
		def filter = FFmpegUtils.getVolumeFilter(-2.0f)

		then:
		filter == "volume=volume=-2.0000dB:precision=float"
	}

	def "获取音量滤镜 - 整数精度"() {
		when:
		def filter = FFmpegUtils.getVolumeFilter(3, FFmpegUtils.VolumePrecision.FIXED)

		then:
		filter == "volume=volume=+3dB:precision=fixed"
	}

	def "获取音量滤镜 - 双精度"() {
		when:
		def filter = FFmpegUtils.getVolumeFilter(3.5, FFmpegUtils.VolumePrecision.DOUBLE)

		then:
		filter == "volume=volume=+3.5000dB:precision=double"
	}

	def "获取音量滤镜 - null精度抛异常"() {
		when:
		FFmpegUtils.getVolumeFilter(3.5f, null)

		then:
		thrown(NullPointerException)
	}

	def "获取音量滤镜 - null分贝抛异常"() {
		when:
		FFmpegUtils.getVolumeFilter(null, FFmpegUtils.VolumePrecision.FLOAT)

		then:
		thrown(NullPointerException)
	}

	def "获取音频混合滤镜"() {
		when:
		def filter = FFmpegUtils.getAmixFilter(2, 0, 1.0f, 0.5f)

		then:
		filter.contains("amix=inputs=2:dropout_transition=0:duration=first")
		filter.contains("weights='1.0 0.5'")
	}

	def "获取音频混合滤镜 - 指定时长模式"() {
		when:
		def filter = FFmpegUtils.getAmixFilter(2, 0, FFmpegUtils.AmixDuration.LONGEST, 1.0f, 0.5f)

		then:
		filter.contains("amix=inputs=2:dropout_transition=0:duration=longest")
	}

	def "获取音频混合滤镜 - inputs小于2抛异常"() {
		when:
		FFmpegUtils.getAmixFilter(1, 0, 1.0f)

		then:
		thrown(IllegalArgumentException)
	}

	def "获取音频混合滤镜 - dropoutTransition为负数抛异常"() {
		when:
		FFmpegUtils.getAmixFilter(2, -1, 1.0f)

		then:
		thrown(IllegalArgumentException)
	}

	def "获取音频混合滤镜 - null时长模式抛异常"() {
		when:
		FFmpegUtils.getAmixFilter(2, 0, null, 1.0f)

		then:
		thrown(NullPointerException)
	}

	def "获取音频循环滤镜 - 无限循环"() {
		when:
		def filter = FFmpegUtils.getAloopFilter(44100, 1000000L)

		then:
		filter == "aloop=loop=-1:size=44100"
	}

	def "获取音频循环滤镜 - 指定循环次数"() {
		when:
		def filter = FFmpegUtils.getAloopFilter(3, 44100, 1000000L)

		then:
		filter == "aloop=loop=3:size=132300"
	}

	def "获取音频循环滤镜 - loop小于-1抛异常"() {
		when:
		FFmpegUtils.getAloopFilter(-2, 44100, 1000000L)

		then:
		thrown(IllegalArgumentException)
	}

	def "获取音频循环滤镜 - sampleRate小于等于0抛异常"() {
		when:
		FFmpegUtils.getAloopFilter(-1, 0, 1000000L)

		then:
		thrown(IllegalArgumentException)
	}

	def "获取音频循环滤镜 - lengthInTime小于等于0抛异常"() {
		when:
		FFmpegUtils.getAloopFilter(-1, 44100, 0L)

		then:
		thrown(IllegalArgumentException)
	}

	def "获取音频时长裁剪滤镜 - 指定时长"() {
		given:
		def duration = Duration.ofSeconds(10)

		when:
		def filter = FFmpegUtils.getAtrimFilter(duration)

		then:
		filter == "atrim=duration=10000000us"
	}

	def "获取音频时长裁剪滤镜 - 指定开始和结束时间"() {
		given:
		def start = Duration.ofSeconds(5)
		def end = Duration.ofSeconds(15)

		when:
		def filter = FFmpegUtils.getAtrimFilter(start, end)

		then:
		filter == "atrim=start=5000000us:end=15000000us"
	}

	def "获取音频时长裁剪滤镜 - 指定时间戳"() {
		when:
		def filter = FFmpegUtils.getAtrimFilter(10000000L)

		then:
		filter == "atrim=duration=10000000us"
	}

	def "获取音频时长裁剪滤镜 - 指定开始和结束时间戳"() {
		when:
		def filter = FFmpegUtils.getAtrimFilter(5000000L, 15000000L)

		then:
		filter == "atrim=start=5000000us:end=15000000us"
	}

	def "获取音频时长裁剪滤镜 - null时长抛异常"() {
		when:
		FFmpegUtils.getAtrimFilter((Duration) null)

		then:
		thrown(NullPointerException)
	}

	def "获取音频时长裁剪滤镜 - 零时长抛异常"() {
		given:
		def duration = Duration.ZERO

		when:
		FFmpegUtils.getAtrimFilter(duration)

		then:
		thrown(IllegalArgumentException)
	}

	def "获取音频时长裁剪滤镜 - 负数时长抛异常"() {
		given:
		def duration = Duration.ofSeconds(-1)

		when:
		FFmpegUtils.getAtrimFilter(duration)

		then:
		thrown(IllegalArgumentException)
	}

	def "获取视频时长裁剪滤镜 - 指定时长"() {
		given:
		def duration = Duration.ofSeconds(10)

		when:
		def filter = FFmpegUtils.getTrimFilter(duration)

		then:
		filter == "trim=duration=10000000us"
	}

	def "获取视频时长裁剪滤镜 - 指定开始和结束时间"() {
		given:
		def start = Duration.ofSeconds(5)
		def end = Duration.ofSeconds(15)

		when:
		def filter = FFmpegUtils.getTrimFilter(start, end)

		then:
		filter == "trim=start=5000000us:end=15000000us"
	}

	def "获取视频时长裁剪滤镜 - 指定时间戳"() {
		when:
		def filter = FFmpegUtils.getTrimFilter(10000000L)

		then:
		filter == "trim=duration=10000000us"
	}

	def "获取视频时长裁剪滤镜 - 指定开始和结束时间戳"() {
		when:
		def filter = FFmpegUtils.getTrimFilter(5000000L, 15000000L)

		then:
		filter == "trim=start=5000000us:end=15000000us"
	}

	def "获取视频时长裁剪滤镜 - null时长抛异常"() {
		when:
		FFmpegUtils.getTrimFilter((Duration) null)

		then:
		thrown(NullPointerException)
	}

	def "获取视频时长裁剪滤镜 - 零时长抛异常"() {
		given:
		def duration = Duration.ZERO

		when:
		FFmpegUtils.getTrimFilter(duration)

		then:
		thrown(IllegalArgumentException)
	}

	def "获取音频变速滤镜"() {
		when:
		def filter = FFmpegUtils.getAtempoFilter(2.0f)

		then:
		filter == "atempo=2.000"
	}

	def "获取音频变速滤镜 - 速度小于0.5抛异常"() {
		when:
		FFmpegUtils.getAtempoFilter(0.4f)

		then:
		thrown(IllegalArgumentException)
	}

	def "获取音频变速滤镜 - 速度大于100抛异常"() {
		when:
		FFmpegUtils.getAtempoFilter(101.0f)

		then:
		thrown(IllegalArgumentException)
	}

	def "获取视频画面裁剪滤镜"() {
		when:
		def filter = FFmpegUtils.getCropFilter(100, 100, 640, 480)

		then:
		filter == "crop=640:480:100:100"
	}

	def "获取视频画面裁剪滤镜 - x为负数抛异常"() {
		when:
		FFmpegUtils.getCropFilter(-1, 100, 640, 480)

		then:
		thrown(IllegalArgumentException)
	}

	def "获取视频画面裁剪滤镜 - y为负数抛异常"() {
		when:
		FFmpegUtils.getCropFilter(100, -1, 640, 480)

		then:
		thrown(IllegalArgumentException)
	}

	def "获取视频画面裁剪滤镜 - width小于等于0抛异常"() {
		when:
		FFmpegUtils.getCropFilter(100, 100, 0, 480)

		then:
		thrown(IllegalArgumentException)
	}

	def "获取视频画面裁剪滤镜 - height小于等于0抛异常"() {
		when:
		FFmpegUtils.getCropFilter(100, 100, 640, 0)

		then:
		thrown(IllegalArgumentException)
	}

	def "获取视频变速滤镜"() {
		when:
		def filter = FFmpegUtils.getSetptsFilter(2.0f)

		then:
		filter == "setpts=0.500000*PTS"
	}

	def "获取视频变速滤镜 - 速度小于等于0抛异常"() {
		when:
		FFmpegUtils.getSetptsFilter(0.0f)

		then:
		thrown(IllegalArgumentException)
	}

	def "获取视频帧率滤镜"() {
		when:
		def filter = FFmpegUtils.getFpsFilter(30.0)

		then:
		filter == "fps=30.00"
	}

	def "获取视频帧率滤镜 - 帧率小于等于0抛异常"() {
		when:
		FFmpegUtils.getFpsFilter(0.0)

		then:
		thrown(IllegalArgumentException)
	}

	def "AmixDuration枚举值"() {
		expect:
		FFmpegUtils.AmixDuration.FIRST.value == "first"
		FFmpegUtils.AmixDuration.LONGEST.value == "longest"
		FFmpegUtils.AmixDuration.SHORTEST.value == "shortest"
	}

	def "VolumePrecision枚举值"() {
		expect:
		FFmpegUtils.VolumePrecision.FIXED != null
		FFmpegUtils.VolumePrecision.FLOAT != null
		FFmpegUtils.VolumePrecision.DOUBLE != null
	}

	def "启用FFmpeg日志 - 默认级别"() {
		when:
		FFmpegUtils.enableLog()

		then:
		noExceptionThrown()
	}

	def "启用FFmpeg日志 - 指定级别"() {
		when:
		FFmpegUtils.enableLog(16)

		then:
		noExceptionThrown()
	}
}
