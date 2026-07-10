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

import io.github.pangju666.commons.ffmpeg.builder.FFmpegFiltersBuilder
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path

class FFmpegFiltersBuilderSpec extends Specification {
	@TempDir
	Path tempDir

	def "创建视频滤镜构建器"() {
		when:
		def builder = FFmpegFiltersBuilder.video()

		then:
		builder != null
	}

	def "创建音频滤镜构建器"() {
		when:
		def builder = FFmpegFiltersBuilder.audio()

		then:
		builder != null
	}

	def "添加输入源"() {
		when:
		def builder = FFmpegFiltersBuilder.video()
			.addInput()

		then:
		builder != null
	}

	def "添加带滤镜的输入源"() {
		when:
		def builder = FFmpegFiltersBuilder.video()
			.addInput("bgm", "aresample=44100")

		then:
		builder != null
	}

	def "添加带滤镜的输入源 - 使用filterName和args"() {
		when:
		def builder = FFmpegFiltersBuilder.video()
			.addInput("bgm", "aresample", "44100")

		then:
		builder != null
	}

	def "添加带滤镜的输入源 - alias为空抛异常"() {
		when:
		FFmpegFiltersBuilder.video()
			.addInput("", "filter")

		then:
		thrown(IllegalArgumentException)
	}

	def "添加带滤镜的输入源 - filter为空抛异常"() {
		when:
		FFmpegFiltersBuilder.video()
			.addInput("alias", "")

		then:
		thrown(IllegalArgumentException)
	}

	def "添加带滤镜的输入源 - filterName为空抛异常"() {
		when:
		FFmpegFiltersBuilder.video()
			.addInput("alias", "", "arg")

		then:
		thrown(IllegalArgumentException)
	}

	def "添加文件源"() {
		given:
		def testFile = tempDir.resolve("test.png")
		Files.write(testFile, "test".bytes)

		when:
		def builder = FFmpegFiltersBuilder.video()
			.addFileSource("wm", testFile.toFile())

		then:
		builder != null
	}

	def "添加文件源 - 使用filePath"() {
		when:
		def builder = FFmpegFiltersBuilder.video()
			.addFileSource("wm", "/path/to/file.png")

		then:
		builder != null
	}

	def "添加文件源带滤镜"() {
		given:
		def testFile = tempDir.resolve("test.png")
		Files.write(testFile, "test".bytes)

		when:
		def builder = FFmpegFiltersBuilder.video()
			.addFileSource("wm", testFile.toFile(), "scale=iw*0.2:-1")

		then:
		builder != null
	}

	def "添加文件源带滤镜 - 使用filterName和args"() {
		given:
		def testFile = tempDir.resolve("test.png")
		Files.write(testFile, "test".bytes)

		when:
		def builder = FFmpegFiltersBuilder.video()
			.addFileSource("wm", testFile.toFile(), "scale", "iw*0.2", "-1")

		then:
		builder != null
	}

	def "添加文件源带滤镜 - 使用filePath"() {
		when:
		def builder = FFmpegFiltersBuilder.video()
			.addFileSource("wm", "/path/to/file.png", "scale=iw*0.2:-1")

		then:
		builder != null
	}

	def "添加文件源带滤镜 - 使用filePath和filterName"() {
		when:
		def builder = FFmpegFiltersBuilder.video()
			.addFileSource("wm", "/path/to/file.png", "scale", "iw*0.2", "-1")

		then:
		builder != null
	}

	def "添加文件源 - file为null抛异常"() {
		when:
		FFmpegFiltersBuilder.video()
			.addFileSource("wm", (File) null)

		then:
		thrown(NullPointerException)
	}

	def "添加自定义源"() {
		when:
		def builder = FFmpegFiltersBuilder.video()
			.addSource("test", "color=red")

		then:
		builder != null
	}

	def "添加自定义源 - 使用filterName和args"() {
		when:
		def builder = FFmpegFiltersBuilder.video()
			.addSource("test", "color", "red")

		then:
		builder != null
	}

	def "添加自定义源 - alias为空抛异常"() {
		when:
		FFmpegFiltersBuilder.video()
			.addSource("", "filter")

		then:
		thrown(IllegalArgumentException)
	}

	def "添加自定义源 - filter为空抛异常"() {
		when:
		FFmpegFiltersBuilder.video()
			.addSource("alias", "")

		then:
		thrown(IllegalArgumentException)
	}

	def "为别名追加滤镜"() {
		when:
		def builder = FFmpegFiltersBuilder.video()
			.addInput("bgm", "aresample=44100")
			.appendAliasFilter("bgm", "volume=2.0")

		then:
		builder != null
	}

	def "为别名追加滤镜 - 使用filterName和args"() {
		when:
		def builder = FFmpegFiltersBuilder.video()
			.addInput("bgm", "aresample=44100")
			.appendAliasFilter("bgm", "volume", "2.0")

		then:
		builder != null
	}

	def "为别名追加滤镜 - alias为空抛异常"() {
		when:
		FFmpegFiltersBuilder.video()
			.appendAliasFilter("", "filter")

		then:
		thrown(IllegalArgumentException)
	}

	def "为别名追加滤镜 - filter为空抛异常"() {
		when:
		FFmpegFiltersBuilder.video()
			.appendAliasFilter("alias", "")

		then:
		thrown(IllegalArgumentException)
	}

	def "添加全局滤镜"() {
		when:
		def builder = FFmpegFiltersBuilder.video()
			.addGlobalFilter("scale=720:-1")

		then:
		builder != null
	}

	def "添加全局滤镜 - 使用filterName和args"() {
		when:
		def builder = FFmpegFiltersBuilder.video()
			.addGlobalFilter("scale", "720", "-1")

		then:
		builder != null
	}

	def "添加全局滤镜 - filterName为空抛异常"() {
		when:
		FFmpegFiltersBuilder.video()
			.addGlobalFilter("", "arg")

		then:
		thrown(IllegalArgumentException)
	}

	def "构建滤镜链 - 无全局滤镜返回null"() {
		when:
		def result = FFmpegFiltersBuilder.video()
			.addInput()
			.build()

		then:
		result == null
	}

	def "构建滤镜链 - 单个全局滤镜"() {
		when:
		def result = FFmpegFiltersBuilder.video()
			.addInput()
			.addGlobalFilter("scale=720:-1")
			.build()

		then:
		result == "scale=720:-1"
	}

	def "构建滤镜链 - 单个输入带标签"() {
		when:
		def result = FFmpegFiltersBuilder.video()
			.addInput()
			.addGlobalFilter("scale=720:-1")
			.addGlobalFilter("crop=640:480")
			.build()

		then:
		result == "[in]scale=720:-1,crop=640:480[out]"
	}

	def "构建滤镜链 - 多个输入"() {
		when:
		def result = FFmpegFiltersBuilder.video()
			.addInput()
			.addInput("bgm", "aresample=44100")
			.addGlobalFilter("overlay=W-w-20:H-h-20")
			.build()

		then:
		result != null
		result.contains("[0:v]")
		result.contains("[bgm]")
		result.contains("overlay")
	}

	def "构建滤镜链 - 带文件源"() {
		when:
		def result = FFmpegFiltersBuilder.video()
			.addInput()
			.addFileSource("wm", "/path/to/watermark.png")
			.appendAliasFilter("wm", "scale=iw*0.2:-1")
			.addGlobalFilter("overlay=W-w-20:H-h-20")
			.build()

		then:
		result != null
		result.contains("movie")
		result.contains("[wm]")
		result.contains("overlay")
	}

	def "构建滤镜链 - 音频滤镜"() {
		when:
		def result = FFmpegFiltersBuilder.audio()
			.addInput()
			.addGlobalFilter("volume=2.0")
			.addGlobalFilter("aresample=44100")
			.build()

		then:
		result == "[in]volume=2.0,aresample=44100[out]"
	}

	def "构建滤镜链 - 音频多个输入"() {
		when:
		def result = FFmpegFiltersBuilder.audio()
			.addInput()
			.addInput("bgm", "aresample=44100")
			.addGlobalFilter("amix=inputs=2:dropout_transition=0:duration=first")
			.build()

		then:
		result != null
		result.contains("[0:a]")
		result.contains("[bgm]")
		result.contains("amix")
	}

	def "构建滤镜链 - 视频添加图片水印"() {
		when:
		def result = FFmpegFiltersBuilder.video()
			.addInput()
			.addFileSource("wm", "/path/to/watermark.png")
			.appendAliasFilter("wm", "scale=iw*0.2:-1")
			.appendAliasFilter("wm", "format=rgba")
			.appendAliasFilter("wm", "colorchannelmixer=aa=0.8")
			.addGlobalFilter("overlay", "W-w-20", "H-h-20")
			.build()

		then:
		result != null
		result.contains("movie")
		result.contains("scale")
		result.contains("format")
		result.contains("colorchannelmixer")
		result.contains("overlay")
	}

	def "构建滤镜链 - 视频添加文字水印"() {
		when:
		def result = FFmpegFiltersBuilder.video()
			.addInput()
			.addGlobalFilter("drawtext", "text='Copyright 2024'", "fontsize=48", "fontcolor=white", "x=W-tw-20", "y=H-th-20")
			.build()

		then:
		result != null
		result.contains("drawtext")
		result.contains("text='Copyright 2024'")
	}

	def "构建滤镜链 - 音频混音"() {
		when:
		def result = FFmpegFiltersBuilder.audio()
			.addInput()
			.addInput("bgm", "aresample=44100")
			.addGlobalFilter("amix", "inputs=2", "dropout_transition=0", "duration=first", "weights=1 1")
			.build()

		then:
		result != null
		result.contains("amix")
		result.contains("inputs=2")
	}

	def "构建滤镜链 - 视频裁剪和缩放"() {
		when:
		def result = FFmpegFiltersBuilder.video()
			.addGlobalFilter("crop", "1000:1000:100:100")
			.addGlobalFilter("scale", "720:-1")
			.build()

		then:
		result == "[in]crop=1000:1000:100:100,scale=720:-1[out]"
	}

	def "构建滤镜链 - 音频处理"() {
		when:
		def result = FFmpegFiltersBuilder.audio()
			.addGlobalFilter("volume", "2.0")
			.addGlobalFilter("aresample", "44100")
			.build()

		then:
		result == "[in]volume=2.0,aresample=44100[out]"
	}

	def "构建滤镜链 - 视频变速处理"() {
		when:
		def result = FFmpegFiltersBuilder.video()
			.addGlobalFilter("setpts", "PTS*0.5")
			.addGlobalFilter("atempo", "2.0")
			.build()

		then:
		result == "[in]setpts=PTS*0.5,atempo=2.0[out]"
	}
}
