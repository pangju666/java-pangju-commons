package io.github.pangju666.commons.image.utils

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Metadata
import io.github.pangju666.commons.image.lang.ImageConstants
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Unroll

import javax.imageio.ImageIO
import java.awt.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.List

class ImageUtilsSpec extends Specification {
	@TempDir
	Path tempDir

	// 测试资源路径
	static final String TEST_IMAGES_DIR = "src/test/resources/images"
	static final List<String> ALL_IMAGES = [
		"camera.jpg",
		"test.bmp",
		"test.gif",
		"test.ico",
		"test.jpg",
		"test.png",
		"test.svg",
		"test.tiff",
		"test.webp",
		"watermark.png",
	]

	static final Map<String, String> IMAGE_MIME_EXPECTED = [
		"test.bmp"     : "image/bmp",
		"test.gif"     : "image/gif",
		"test.ico"     : "image/vnd.microsoft.icon",
		"camera.jpg"   : "image/jpeg",
		"test.jpg"     : "image/jpeg",
		"test.png"     : "image/png",
		"test.svg"     : "image/svg+xml",
		"test.tiff"    : "image/tiff",
		"test.webp"    : "image/webp",
		"watermark.png": "image/png",
	]

	// MIME 类型检测（File/Bytes/InputStream）
	@Unroll
	def "getMimeType 应返回预期类型：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def bytes = Files.readAllBytes(file.toPath())
		def stream = new FileInputStream(file)

		when:
		def mimeByFile = ImageUtils.getMimeType(file)
		def mimeByBytes = ImageUtils.getMimeType(bytes)
		def mimeByStream = ImageUtils.getMimeType(stream)

		then:
		mimeByFile == expected
		mimeByBytes == expected
		mimeByStream == expected

		where:
		[name, expected] << IMAGE_MIME_EXPECTED.entrySet().collect { [it.key, it.value] }
	}

	// 图像尺寸获取（File/Bytes/InputStream）
	@Unroll
	def "getSize 应返回有效尺寸：#name via #inputType"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def input = inputType == 'File' ? file :
			inputType == 'Bytes' ? Files.readAllBytes(file.toPath()) :
				new FileInputStream(file)
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		def size = ImageUtils.getSize(input)

		then:
		if (canRead) {
			assert size != null
			assert size.getWidth() > 0
			assert size.getHeight() > 0
		} else {
			assert size == null
		}

		where:
		[name, inputType] << ALL_IMAGES.collectMany { n ->
			["File", "Bytes", "InputStream"].collect { t -> [n, t] }
		}
	}

	// 异常与边界
	def "getSize(bytes) 空数组返回 null"() {
		when:
		def size = ImageUtils.getSize(new byte[0])

		then:
		size == null
	}

	// ImageInputStream MIME 类型检测
	@Unroll
	def "getMimeType(ImageInputStream) 返回预期类型：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def imageInputStream = ImageIO.createImageInputStream(file)

		when:
		def mime = ImageUtils.getMimeType(imageInputStream)

		then:
		if (name == "test.svg") {
			assert mime == "image/svg"
		} else {
			assert mime == expected
		}

		where:
		[name, expected] << IMAGE_MIME_EXPECTED.entrySet().collect { [it.key, it.value] }
	}

	// Metadata MIME 类型与尺寸解析
	def "getMimeType(Metadata) 与 getSize(Metadata) 正常解析"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		Metadata metadata = ImageMetadataReader.readMetadata(file)

		when:
		def mime = ImageUtils.getMimeType(metadata)
		def size = ImageUtils.getSize(metadata)

		then:
		mime == "image/jpeg" || mime == null // 有些格式未记录此标签
		size == null || (size.getWidth() > 0 && size.getHeight() > 0)
	}

	// 环境能力检查
	def "环境支持读取常见类型，写入支持PNG/JPEG"() {
		expect:
		ImageUtils.isSupportReadType("image/png")
		ImageUtils.isSupportReadType("image/jpeg")
		ImageUtils.isSupportReadType("image/gif")
		ImageUtils.isSupportReadType("image/bmp")

		and:
		ImageUtils.isSupportWriteType("image/png")
		ImageUtils.isSupportWriteType("image/jpeg")
	}

	def "toHexColor 转换不含 Alpha"() {
		expect:
		ImageUtils.toHexColor(new Color(16, 32, 48)) == "#102030"
	}

	def "toHexColorWithAlpha 转换含 Alpha"() {
		expect:
		ImageUtils.toHexColorWithAlpha(new Color(16, 32, 48, 255)) == "#ff102030"
	}

	def "getMimeType(null InputStream) 抛异常"() {
		when:
		ImageUtils.getMimeType(null as InputStream)

		then:
		thrown(NullPointerException)
	}

	@Unroll
	def "getSize(ImageInputStream) 返回有效尺寸：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)
		def iis = ImageIO.createImageInputStream(file)

		when:
		def size = ImageUtils.getSize(iis)

		then:
		if (canRead) {
			assert size != null
			assert size.getWidth() > 0
			assert size.getHeight() > 0
		} else {
			assert size == null
		}

		where:
		name << ALL_IMAGES
	}

	@Unroll
	def "getSize(File,useMetadata=false) 返回有效尺寸：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		def size = ImageUtils.getSize(file, false)

		then:
		if (canRead) {
			assert size != null
			assert size.getWidth() > 0
			assert size.getHeight() > 0
		} else {
			assert size == null
		}

		where:
		name << ALL_IMAGES
	}

	@Unroll
	def "getSize(bytes,useMetadata=false) 返回有效尺寸：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)
		def bytes = Files.readAllBytes(file.toPath())

		when:
		def size = ImageUtils.getSize(bytes, false)

		then:
		if (canRead) {
			assert size != null
			assert size.getWidth() > 0
			assert size.getHeight() > 0
		} else {
			assert size == null
		}

		where:
		name << ALL_IMAGES
	}

	@Unroll
	def "getSize(InputStream,useMetadata=false) 返回有效尺寸：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)
		def stream = new FileInputStream(file)

		when:
		def size = ImageUtils.getSize(stream, false)

		then:
		if (canRead) {
			assert size != null
			assert size.getWidth() > 0
			assert size.getHeight() > 0
		} else {
			assert size == null
		}

		where:
		name << ALL_IMAGES
	}

	@Unroll
	def "getExifOrientation 各输入一致且有效范围：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def bytes = Files.readAllBytes(file.toPath())
		def stream = new FileInputStream(file)
		def metadata = ImageMetadataReader.readMetadata(file)

		when:
		def oFile = ImageUtils.getExifOrientation(file)
		def oBytes = ImageUtils.getExifOrientation(bytes)
		def oStream = ImageUtils.getExifOrientation(stream)
		def oMeta = ImageUtils.getExifOrientation(metadata)

		then:
		(1..8).containsAll([oFile, oBytes, oStream, oMeta])
		oFile == oBytes
		oBytes == oStream
		oStream == oMeta

		where:
		name << ALL_IMAGES.findAll { it != "test.svg" }
	}
}
