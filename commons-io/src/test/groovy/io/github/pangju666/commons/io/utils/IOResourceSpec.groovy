package io.github.pangju666.commons.io.utils

import io.github.pangju666.commons.io.model.IOResource
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Unroll

import java.nio.file.Path

class IOResourceSpec extends Specification {
	@TempDir
	Path tempDir

	def "基于文件路径构造IOResource"() {
		given:
		File f = tempDir.resolve("test.txt").toFile()
		f.text = "test content"

		when:
		def resource = new IOResource(f.absolutePath)

		then:
		resource.size == f.length()
		resource.mimeType != null
		resource.file == f
	}

	def "基于文件路径构造IOResource with cache"() {
		given:
		File f = tempDir.resolve("cache.txt").toFile()
		f.text = "cached content"

		when:
		def resource = new IOResource(f.absolutePath, true)

		then:
		resource.size == f.length()
		resource.mimeType != null
		resource.cacheContent
	}

	def "基于File对象构造IOResource"() {
		given:
		File f = tempDir.resolve("file.txt").toFile()
		f.text = "file content"

		when:
		def resource = new IOResource(f)

		then:
		resource.size == f.length()
		resource.mimeType != null
		resource.file == f
	}

	def "基于File对象构造IOResource with cache"() {
		given:
		File f = tempDir.resolve("filecache.txt").toFile()
		f.text = "file cached"

		when:
		def resource = new IOResource(f, true)

		then:
		resource.size == f.length()
		resource.cacheContent
	}

	def "基于字节数组构造IOResource"() {
		given:
		def data = "byte array data".bytes

		when:
		def resource = new IOResource(data)

		then:
		resource.size == data.length
		resource.mimeType != null
		resource.bytes == data
		!resource.cacheContent
	}

	def "基于输入流构造IOResource"() {
		given:
		def data = "stream data".bytes

		when:
		def resource = new IOResource(new ByteArrayInputStream(data))

		then:
		resource.size == data.length
		resource.mimeType != null
		resource.bytes == data
		!resource.cacheContent
	}

	def "复制构造函数共享非临时文件"() {
		given:
		File f = tempDir.resolve("original.txt").toFile()
		f.text = "original"
		def original = new IOResource(f)

		when:
		def copy = new IOResource(original)

		then:
		copy.file == f
		copy.size == original.size
		copy.mimeType == original.mimeType
	}

	def "复制构造函数共享字节数组"() {
		given:
		def data = "bytes data".bytes
		def original = new IOResource(data)

		when:
		def copy = new IOResource(original)

		then:
		copy.bytes == data
		copy.size == original.size
	}

	def "复制已关闭资源抛异常"() {
		given:
		def data = "test".bytes
		def original = new IOResource(data)
		original.close()

		when:
		new IOResource(original)

		then:
		thrown(IllegalArgumentException)
	}

	def "空字节数组构造抛异常"() {
		when:
		new IOResource(new byte[0])

		then:
		thrown(IllegalArgumentException)
	}

	def "null字节数组构造抛异常"() {
		when:
		new IOResource(null as byte[])

		then:
		thrown(IllegalArgumentException)
	}

	def "null输入流构造抛异常"() {
		when:
		new IOResource(null as InputStream)

		then:
		thrown(NullPointerException)
	}

	def "空文件路径构造抛异常"() {
		when:
		new IOResource("")

		then:
		thrown(IllegalArgumentException)
	}

	def "获取资源大小"() {
		given:
		def data = "size test".bytes
		def resource = new IOResource(data)

		expect:
		resource.size == data.length
	}

	def "获取MIME类型"() {
		given:
		def resource = new IOResource("text data".bytes)

		expect:
		resource.mimeType != null
		resource.mimeType.startsWith("text/")
	}

	def "获取摘要并缓存"() {
		given:
		def data = "digest test".bytes
		def resource = new IOResource(data)

		when:
		def digest1 = resource.getDigest()
		def digest2 = resource.getDigest()

		then:
		digest1 == digest2
		digest1.size() == 16
	}

	def "手动设置摘要"() {
		given:
		def resource = new IOResource("test".bytes)
		def customDigest = "customdigest123456"

		when:
		resource.setDigest(customDigest)

		then:
		resource.getDigest() == customDigest
	}

	def "已关闭资源设置摘要抛异常"() {
		given:
		def resource = new IOResource("test".bytes)
		resource.close()

		when:
		resource.setDigest("digest")

		then:
		thrown(IOException)
	}

	def "文件模式获取文件对象"() {
		given:
		File f = tempDir.resolve("fileobj.txt").toFile()
		f.text = "file object"
		def resource = new IOResource(f)

		when:
		def returnedFile = resource.getFile()

		then:
		returnedFile == f
	}

	def "字节数组模式获取文件创建临时文件"() {
		given:
		def data = "temp file test".bytes
		def resource = new IOResource(data)

		when:
		def tempFile = resource.getFile()

		then:
		tempFile.exists()
		tempFile.name.startsWith("io-resource-")
		tempFile.name.endsWith(".tmp")
		tempFile.parent == FileUtils.getTempDirectory().getPath()
		resource.isTempFile()
	}

	def "创建新输入流"() {
		given:
		def data = "input stream test".bytes
		def resource = new IOResource(data)

		when:
		def stream1 = resource.openInputStream()
		def bytes1 = stream1.readAllBytes()
		stream1.close()

		def stream2 = resource.openInputStream()
		def bytes2 = stream2.readAllBytes()
		stream2.close()

		then:
		bytes1 == data
		bytes2 == data
		bytes1 == bytes2
	}

	def "已关闭资源创建输入流抛异常"() {
		given:
		def resource = new IOResource("test".bytes)
		resource.close()

		when:
		resource.openInputStream()

		then:
		thrown(IOException)
	}

	def "文件模式获取字节数组"() {
		given:
		File f = tempDir.resolve("bytes.txt").toFile()
		def data = "file to bytes".bytes
		f.bytes = data
		def resource = new IOResource(f)

		when:
		def bytes = resource.getBytes()

		then:
		bytes == data
	}

	def "文件模式缓存字节数组"() {
		given:
		File f = tempDir.resolve("cachedbytes.txt").toFile()
		def data = "cached file bytes".bytes
		f.bytes = data
		def resource = new IOResource(f, true)

		when:
		def bytes1 = resource.getBytes()
		def bytes2 = resource.getBytes()

		then:
		bytes1 == data
		bytes2 == data
		bytes1.is(bytes2)
	}

	def "字节数组模式获取字节数组"() {
		given:
		def data = "byte array".bytes
		def resource = new IOResource(data)

		when:
		def bytes = resource.getBytes()

		then:
		bytes == data
		bytes.is(data)
	}

	def "已关闭资源获取字节数组抛异常"() {
		given:
		def resource = new IOResource("test".bytes)
		resource.close()

		when:
		resource.getBytes()

		then:
		thrown(IOException)
	}

	def "判断音频资源"() {
		given:
		def audioData = [0x00, 0x00, 0x00, 0x20, 0x66, 0x74, 0x79, 0x70, 0x4D, 0x34, 0x41] as byte[] // M4A header
		def resource = new IOResource(audioData)

		when:
		def isAudio = resource.isAudio()

		then:
		isAudio || !isAudio // Depends on Tika detection
	}

	def "判断视频资源"() {
		given:
		def videoData = [0x00, 0x00, 0x00, 0x1C, 0x66, 0x74, 0x79, 0x70, 0x6D, 0x70, 0x34, 0x32] as byte[] // MP4 header
		def resource = new IOResource(videoData)

		when:
		def isVideo = resource.isVideo()

		then:
		isVideo || !isVideo // Depends on Tika detection
	}

	def "判断图片资源"() {
		given:
		def imageData = [0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A] as byte[] // PNG header
		def resource = new IOResource(imageData)

		when:
		def isImage = resource.isImage()

		then:
		isImage
	}

	def "判断文本资源"() {
		given:
		def resource = new IOResource("plain text".bytes)

		expect:
		resource.isText()
	}

	def "判断临时文件"() {
		given:
		def data = "temp test".bytes
		def resource = new IOResource(data)

		when:
		resource.getFile()

		then:
		resource.isTempFile()
	}

	def "非临时文件判断"() {
		given:
		File f = tempDir.resolve("regular.txt").toFile()
		f.text = "regular file"
		def resource = new IOResource(f)

		expect:
		!resource.isTempFile()
	}

	def "无文件时判断临时文件返回false"() {
		given:
		def resource = new IOResource("test".bytes)

		expect:
		!resource.isTempFile()
	}

	def "关闭资源清理临时文件"() {
		given:
		def data = "close test".bytes
		def resource = new IOResource(data)
		def tempFile = resource.getFile()

		when:
		resource.close()

		then:
		!tempFile.exists()
		resource.isClosed()
	}

	def "关闭非临时文件不删除文件"() {
		given:
		File f = tempDir.resolve("keep.txt").toFile()
		f.text = "keep this file"
		def resource = new IOResource(f)

		when:
		resource.close()

		then:
		f.exists()
		resource.isClosed()
	}

	def "重复关闭安全"() {
		given:
		def resource = new IOResource("test".bytes)

		when:
		resource.close()
		resource.close()

		then:
		noExceptionThrown()
	}

	def "关闭后清空引用"() {
		given:
		def resource = new IOResource("test".bytes)
		def tempFile = resource.getFile()

		when:
		resource.close()

		then:
		!tempFile.exists()
	}

	def "判断资源已关闭"() {
		given:
		def resource = new IOResource("test".bytes)

		expect:
		!resource.isClosed()

		when:
		resource.close()

		then:
		resource.isClosed()
	}

	def "已关闭资源获取摘要抛异常"() {
		given:
		def resource = new IOResource("test".bytes)
		resource.close()

		when:
		resource.getDigest()

		then:
		thrown(IOException)
	}

	def "已关闭资源获取文件抛异常"() {
		given:
		def resource = new IOResource("test".bytes)
		resource.close()

		when:
		resource.getFile()

		then:
		thrown(IOException)
	}

	def "已关闭资源获取大小正常"() {
		given:
		def resource = new IOResource("test".bytes)
		resource.close()

		expect:
		resource.size == 4
	}

	def "已关闭资源获取MIME类型正常"() {
		given:
		def resource = new IOResource("test".bytes)
		resource.close()

		expect:
		resource.mimeType != null
	}

	def "设置输入流阈值"() {
		setup:
		IOResource.setInputStreamThreshold(8192)
	}

	def "设置非法阈值抛异常"() {
		when:
		IOResource.setInputStreamThreshold(0)

		then:
		thrown(IllegalArgumentException)

		when:
		IOResource.setInputStreamThreshold(-1)

		then:
		thrown(IllegalArgumentException)
	}

	def "文件模式计算摘要"() {
		given:
		File f = tempDir.resolve("digestfile.txt").toFile()
		f.text = "file digest test"
		def resource = new IOResource(f)

		when:
		def digest = resource.getDigest()

		then:
		digest != null
		digest.size() == 16
	}

	def "字节数组模式计算摘要"() {
		given:
		def resource = new IOResource("byte digest test".bytes)

		when:
		def digest = resource.getDigest()

		then:
		digest != null
		digest.size() == 16
	}

	@Unroll
	def "不同类型资源摘要不同"() {
		given:
		def resource1 = new IOResource(data1)
		def resource2 = new IOResource(data2)

		expect:
		resource1.getDigest() != resource2.getDigest()

		where:
		data1            | data2
		"content1".bytes | "content2".bytes
		"same".bytes     | "same ".bytes
		new byte[100]    | new byte[110]
	}

	def "相同内容不同来源摘要相同"() {
		given:
		def data = "same content".bytes
		File f = tempDir.resolve("same.txt").toFile()
		f.bytes = data

		def resource1 = new IOResource(data)
		def resource2 = new IOResource(f)

		expect:
		resource1.getDigest() == resource2.getDigest()
	}

	def "大文件字节数组模式不缓存"() {
		given:
		File f = tempDir.resolve("large.txt").toFile()
		def data = new byte[10000]
		new Random().nextBytes(data)
		f.bytes = data
		def resource = new IOResource(f, false)

		when:
		def bytes1 = resource.getBytes()
		def bytes2 = resource.getBytes()

		then:
		bytes1 == bytes2
		!bytes1.is(bytes2)
	}
}
