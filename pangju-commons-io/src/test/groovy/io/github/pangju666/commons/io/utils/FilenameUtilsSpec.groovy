package io.github.pangju666.commons.io.utils

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class FilenameUtilsSpec extends Specification {
	@Shared
	def imageFiles = ["test.jpg", "photo.png", "image.webp"]
	@Shared
	def textFiles = ["readme.md", "data.csv", "notes.txt"]
	@Shared
	def videoFiles = ["movie.mp4", "clip.avi", "video.mov"]

	// getMimeType 测试
	def "测试获取MIME类型"() {
		when:
		def result = FilenameUtils.getMimeType(filename)

		then:
		result == expectedMimeType

		where:
		filename       | expectedMimeType
		"image.png"    | "image/png"
		"document.pdf" | "application/pdf"
		"data.json"    | "application/json" // 注意：MimetypesFileTypeMap的默认行为
	}

	// 类型检测方法测试
	@Unroll
	def "测试文件类型检测：#method -> #filename 应返回 #expected"() {
		expect:
		FilenameUtils."$method"(filename) == expected

		where:
		method              | filename       | expected
		"isImageType"       | "photo.jpg"    | true
		"isImageType"       | "data.txt"     | false
		"isTextType"        | "readme.md"    | true
		"isTextType"        | "movie.mp4"    | false
		"isVideoType"       | "clip.avi"     | true
		"isVideoType"       | "image.png"    | false
		"isAudioType"       | "music.mp3"    | true
		"isAudioType"       | "document.pdf" | false
		"isApplicationType" | "app.exe"      | true
		"isApplicationType" | "video.mp4"    | false
	}

	// 路径类型判断测试
	@Unroll
	def "测试路径类型判断：路径 '#path' 是目录路径应返回 #isDir，是文件路径应返回 #isFile"() {
		expect:
		FilenameUtils.isDirectoryPath(path) == isDir
		FilenameUtils.isFilePath(path) == isFile

		where:
		path                 | isDir | isFile
		"C:\\logs\\"         | true  | false
		"/var/log/"          | true  | false
		"file.txt"           | false | true
		"D:/data/report.pdf" | false | true
		""                   | false | false
	}

	// 文件名操作测试
	@Unroll
	def "测试文件名重构：原始文件 '#original' 使用参数 '#param' 调用方法 '#method' 应得到 '#expected'"() {
		expect:
		FilenameUtils."$method"(original, param) == expected

		where:
		method             | original           | param        | expected
		"rename"           | "/path/to/old.txt" | "newfile"    | "/path/to/newfile"
		"rename"           | "data.csv"         | "backup.zip" | "backup.zip"
		"replaceBaseName"  | "document.pdf"     | "report"     | "report.pdf"
		"replaceBaseName"  | "/tmp/image.jpg"   | "thumbnail"  | "/tmp/thumbnail.jpg"
		"replaceExtension" | "file"             | "txt"        | "file.txt"
		"replaceExtension" | "photo.png"        | "jpg"        | "photo.jpg"
		"replaceExtension" | "config"           | ".xml"       | "config.xml"
	}

	// 异常测试
	def "测试参数校验异常"() {
		when:
		FilenameUtils.getMimeType(null)

		then:
		thrown(NullPointerException)

		when:
		FilenameUtils.rename("valid.txt", "")

		then:
		thrown(IllegalArgumentException)
	}

	// 批量MIME类型匹配测试
	def "测试批量MIME类型匹配"() {
		setup:
		def allowedTypes = ["image/png", "image/jpeg", "application/pdf"]

		expect:
		FilenameUtils.isAnyMimeType(filename, allowedTypes as String[]) == expected

		where:
		filename       | expected
		"test.png"     | true
		"photo.jpg"    | true
		"document.pdf" | true
		"video.mp4"    | false
	}
}
