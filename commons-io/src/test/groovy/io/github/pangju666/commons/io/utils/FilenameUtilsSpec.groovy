package io.github.pangju666.commons.io.utils

import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Title("FilenameUtils 单元测试")
class FilenameUtilsSpec extends Specification {

	@Unroll
	def "getMimeType 返回预期类型: #name -> #expected"() {
		expect:
		FilenameUtils.getMimeType(name) == expected

		where:
		name            | expected
		null            | null
		""              | null
		"image.png"     | "image/png"
		"readme.txt"    | "text/plain"
		"document.pdf"  | "application/pdf"
		"unknown.zzzzz" | "application/octet-stream"
	}

	@Unroll
	def "图片/文本/视频/音频类型判断"() {
		expect:
		FilenameUtils.isImageType("photo.png")
		!FilenameUtils.isImageType("")
		FilenameUtils.isTextType("readme.txt")
		!FilenameUtils.isTextType(null)
		FilenameUtils.isVideoType("movie.mp4")
		FilenameUtils.isVideoType("playlist.m3u8")
		!FilenameUtils.isVideoType("photo.png")
		FilenameUtils.isAudioType("music.mp3")
		!FilenameUtils.isAudioType("image.jpg")
	}

	def "模型类型判断对非模型返回false"() {
		expect:
		!FilenameUtils.isModelType("document.pdf")
	}

	@Unroll
	def "isMimeType 精确匹配与不匹配"() {
		expect:
		FilenameUtils.isMimeType("image.png", "image/png")
		!FilenameUtils.isMimeType("image.png", "image/jpeg")
		!FilenameUtils.isMimeType("image.png", "")
		!FilenameUtils.isMimeType("", "image/png")
	}

	def "isAnyMimeType(数组) 任一匹配与空数组"() {
		expect:
		FilenameUtils.isAnyMimeType("image.png", "application/pdf", "image/jpeg", "image/png")
		!FilenameUtils.isAnyMimeType("image.png" as String, new String[0])
		// 忽略大小写
		FilenameUtils.isAnyMimeType("image.png", "IMAGE/PNG")
	}

	def "isAnyMimeType(集合) 任一匹配与空集合"() {
		given:
		def set = ["application/pdf", "image/png"] as Set
		def empty = [] as Set

		expect:
		FilenameUtils.isAnyMimeType("image.png", set)
		!FilenameUtils.isAnyMimeType("image.png", empty)
	}

	@Unroll
	def "rename 完全替换文件名"() {
		expect:
		FilenameUtils.rename(input, newName) == output

		where:
		input              | newName          | output
		"data.csv"         | "backup"         | "backup"
		"/var/log/app.log" | "error_2023.log" | "/var/log/error_2023.log"
		"a/b/file.txt"     | "new.bin"        | "a/b/new.bin"
	}

	@Unroll
	def "replaceBaseName 仅替换基名保留扩展名与路径"() {
		expect:
		FilenameUtils.replaceBaseName(input, newBase) == output

		where:
		input              | newBase | output
		"file.txt"         | "new"   | "new.txt"
		"/path/to/old.jpg" | "photo" | "/path/to/photo.jpg"
		"config.bak"       | "set"   | "set.bak"
		"/dir/noext"       | "base"  | "/dir/base"
	}

	@Unroll
	def "replaceExtension 替换或移除扩展名"() {
		expect:
		FilenameUtils.replaceExtension(input, ext) == output

		where:
		input               | ext    | output
		"file.txt"          | "csv"  | "file.csv"
		"/path/to/data.old" | "json" | "/path/to/data.json"
		"config"            | ".xml" | "config.xml"
		"/dir/file.ext"     | ""     | "/dir/file"
	}
}
