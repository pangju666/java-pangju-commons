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

package io.github.pangju666.commons.ffmpeg.builder

import io.github.pangju666.commons.ffmpeg.enums.VideoPreset
import io.github.pangju666.commons.ffmpeg.lang.FFmpegConstants
import io.github.pangju666.commons.ffmpeg.model.AudioOutputOption
import io.github.pangju666.commons.ffmpeg.model.VideoOutputOption
import org.bytedeco.ffmpeg.global.avcodec
import spock.lang.Specification

class VideoOutputOptionBuilderSpec extends Specification {
	def "构造器 - 根据格式和分辨率创建"() {
		when:
		def builder = new VideoOutputOptionBuilder("mp4", 1920, 1080)

		then:
		builder != null
		builder.outputOption.format == "mp4"
		builder.outputOption.imageWidth == 1920
		builder.outputOption.imageHeight == 1080
		builder.originalImageWidth == 1920
		builder.originalImageHeight == 1080
	}

	def "静态工厂方法 - mp4WithH264使用预设"() {
		when:
		def builder = VideoOutputOptionBuilder.mp4WithH264(VideoPreset.FHD_1080P)

		then:
		builder != null
		builder.outputOption.format == "mp4"
		builder.outputOption.imageWidth == 1920
		builder.outputOption.imageHeight == 1080
		builder.outputOption.codecId == avcodec.AV_CODEC_ID_H264
	}

	def "静态工厂方法 - mp4WithH265使用预设"() {
		when:
		def builder = VideoOutputOptionBuilder.mp4WithH265(VideoPreset.FHD_1080P)

		then:
		builder != null
		builder.outputOption.format == "mp4"
		builder.outputOption.imageWidth == 1920
		builder.outputOption.imageHeight == 1080
		builder.outputOption.codecId == avcodec.AV_CODEC_ID_H265
	}

	def "静态工厂方法 - webmWithVP9使用预设"() {
		when:
		def builder = VideoOutputOptionBuilder.webmWithVP9(VideoPreset.FHD_1080P)

		then:
		builder != null
		builder.outputOption.format == "webm"
		builder.outputOption.imageWidth == 1920
		builder.outputOption.imageHeight == 1080
		builder.outputOption.codecId == avcodec.AV_CODEC_ID_VP9
	}

	def "静态工厂方法 - mkvWithH264使用预设"() {
		when:
		def builder = VideoOutputOptionBuilder.mkvWithH264(VideoPreset.FHD_1080P)

		then:
		builder != null
		builder.outputOption.format == FFmpegConstants.VIDEO_MKV_FORMAT
		builder.outputOption.imageWidth == 1920
		builder.outputOption.imageHeight == 1080
		builder.outputOption.codecId == avcodec.AV_CODEC_ID_H264
	}

	def "静态工厂方法 - mkvWithH265使用预设"() {
		when:
		def builder = VideoOutputOptionBuilder.mkvWithH265(VideoPreset.FHD_1080P)

		then:
		builder != null
		builder.outputOption.format == FFmpegConstants.VIDEO_MKV_FORMAT
		builder.outputOption.imageWidth == 1920
		builder.outputOption.imageHeight == 1080
		builder.outputOption.codecId == avcodec.AV_CODEC_ID_H265
	}

	def "静态工厂方法 - mp4WithH264使用自定义分辨率"() {
		when:
		def builder = VideoOutputOptionBuilder.mp4WithH264(1280, 720)

		then:
		builder != null
		builder.outputOption.format == FFmpegConstants.VIDEO_MP4_FORMAT
		builder.outputOption.imageWidth == 1280
		builder.outputOption.imageHeight == 720
		builder.outputOption.codecId == avcodec.AV_CODEC_ID_H264
	}

	def "静态工厂方法 - mp4WithH265使用自定义分辨率"() {
		when:
		def builder = VideoOutputOptionBuilder.mp4WithH265(1280, 720)

		then:
		builder != null
		builder.outputOption.format == FFmpegConstants.VIDEO_MP4_FORMAT
		builder.outputOption.imageWidth == 1280
		builder.outputOption.imageHeight == 720
		builder.outputOption.codecId == avcodec.AV_CODEC_ID_H265
	}

	def "静态工厂方法 - webmWithVP9使用自定义分辨率"() {
		when:
		def builder = VideoOutputOptionBuilder.webmWithVP9(1280, 720)

		then:
		builder != null
		builder.outputOption.format == FFmpegConstants.VIDEO_WEBM_FORMAT
		builder.outputOption.imageWidth == 1280
		builder.outputOption.imageHeight == 720
		builder.outputOption.codecId == avcodec.AV_CODEC_ID_VP9
	}

	def "静态工厂方法 - mkvWithH264使用自定义分辨率"() {
		when:
		def builder = VideoOutputOptionBuilder.mkvWithH264(1280, 720)

		then:
		builder != null
		builder.outputOption.format == FFmpegConstants.VIDEO_MKV_FORMAT
		builder.outputOption.imageWidth == 1280
		builder.outputOption.imageHeight == 720
		builder.outputOption.codecId == avcodec.AV_CODEC_ID_H264
	}

	def "静态工厂方法 - mkvWithH265使用自定义分辨率"() {
		when:
		def builder = VideoOutputOptionBuilder.mkvWithH265(1280, 720)

		then:
		builder != null
		builder.outputOption.format == FFmpegConstants.VIDEO_MKV_FORMAT
		builder.outputOption.imageWidth == 1280
		builder.outputOption.imageHeight == 720
		builder.outputOption.codecId == avcodec.AV_CODEC_ID_H265
	}

	def "codec方法 - 设置编码器"() {
		given:
		def builder = new VideoOutputOptionBuilder("mp4", 1920, 1080)
		def codec = avcodec.avcodec_find_encoder(avcodec.AV_CODEC_ID_H264)

		when:
		def result = builder.codec(codec)

		then:
		result.is(builder)
		builder.outputOption.codecId == codec.id()
	}

	def "codecId方法 - 设置编码器ID"() {
		given:
		def builder = new VideoOutputOptionBuilder("mp4", 1920, 1080)

		when:
		def result = builder.codecId(avcodec.AV_CODEC_ID_H264)

		then:
		result.is(builder)
		builder.outputOption.codecId == avcodec.AV_CODEC_ID_H264
	}

	def "scaleByWidth方法 - 按宽度缩放保持宽高比"() {
		given:
		def builder = new VideoOutputOptionBuilder("mp4", 1920, 1080)

		when:
		def result = builder.scaleByWidth(1280)

		then:
		result.is(builder)
		builder.outputOption.imageWidth == 1280
		builder.outputOption.imageHeight == 720
	}

	def "scaleByWidth方法 - 宽度小于等于0抛出异常"() {
		given:
		def builder = new VideoOutputOptionBuilder("mp4", 1920, 1080)

		when:
		builder.scaleByWidth(0)

		then:
		thrown(IllegalArgumentException)
	}

	def "scaleByHeight方法 - 按高度缩放保持宽高比"() {
		given:
		def builder = new VideoOutputOptionBuilder("mp4", 1920, 1080)

		when:
		def result = builder.scaleByHeight(720)

		then:
		result.is(builder)
		builder.outputOption.imageWidth == 1280
		builder.outputOption.imageHeight == 720
	}

	def "scaleByHeight方法 - 高度小于等于0抛出异常"() {
		given:
		def builder = new VideoOutputOptionBuilder("mp4", 1920, 1080)

		when:
		builder.scaleByHeight(0)

		then:
		thrown(IllegalArgumentException)
	}

	def "scale方法 - 按目标分辨率缩放保持宽高比"() {
		given:
		def builder = new VideoOutputOptionBuilder("mp4", 1920, 1080)

		when:
		def result = builder.scale(1280, 720)

		then:
		result.is(builder)
		builder.outputOption.imageWidth == 1280
		builder.outputOption.imageHeight == 720
	}

	def "scale方法 - 参数小于等于0抛出异常"() {
		given:
		def builder = new VideoOutputOptionBuilder("mp4", 1920, 1080)

		when:
		builder.scale(0, 720)

		then:
		thrown(IllegalArgumentException)
	}

	def "scale方法 - 使用缩放因子缩放"() {
		given:
		def builder = new VideoOutputOptionBuilder("mp4", 1920, 1080)

		when:
		def result = builder.scale(0.5)

		then:
		result.is(builder)
		builder.outputOption.imageWidth == 960
		builder.outputOption.imageHeight == 540
	}

	def "scale方法 - 缩放因子小于等于0抛出异常"() {
		given:
		def builder = new VideoOutputOptionBuilder("mp4", 1920, 1080)

		when:
		builder.scale(0)

		then:
		thrown(IllegalArgumentException)
	}

	def "frameRate方法 - 设置帧率"() {
		given:
		def builder = new VideoOutputOptionBuilder("mp4", 1920, 1080)

		when:
		def result = builder.frameRate(30.0)

		then:
		result.is(builder)
		builder.outputOption.frameRate == 30.0 as double
	}

	def "bitrate方法 - 设置比特率"() {
		given:
		def builder = new VideoOutputOptionBuilder("mp4", 1920, 1080)

		when:
		def result = builder.bitrate(5000000)

		then:
		result.is(builder)
		builder.outputOption.bitrate == 5000000
	}

	def "resolution方法 - 设置分辨率并更新原始分辨率"() {
		given:
		def builder = new VideoOutputOptionBuilder("mp4", 1920, 1080)

		when:
		def result = builder.resolution(1280, 720)

		then:
		result.is(builder)
		builder.outputOption.imageWidth == 1280
		builder.outputOption.imageHeight == 720
		builder.originalImageWidth == 1280
		builder.originalImageHeight == 720
	}

	def "audio方法 - 设置音频配置"() {
		given:
		def builder = new VideoOutputOptionBuilder("mp4", 1920, 1080)
		def audioOption = AudioOutputOption.aacForVideo(12000)

		when:
		def result = builder.audio(audioOption)

		then:
		result.is(builder)
		builder.outputOption.audio == audioOption
	}

	def "build方法 - 构建视频输出选项"() {
		given:
		def builder = new VideoOutputOptionBuilder("mp4", 1920, 1080)

		when:
		def option = builder.build()

		then:
		option != null
		option instanceof VideoOutputOption
		option.format == "mp4"
		option.imageWidth == 1920
		option.imageHeight == 1080
	}

	def "链式调用 - 完整构建流程"() {
		when:
		def option = VideoOutputOptionBuilder.mp4WithH264(VideoPreset.FHD_1080P)
			.frameRate(30)
			.bitrate(5000000)
			.build()

		then:
		option != null
		option.format == "mp4"
		option.imageWidth == 1920
		option.imageHeight == 1080
		option.codecId == avcodec.AV_CODEC_ID_H264
		option.frameRate == 30
		option.bitrate == 5000000
	}
}
