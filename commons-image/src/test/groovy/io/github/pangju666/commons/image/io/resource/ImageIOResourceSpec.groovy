package io.github.pangju666.commons.image.io.resource

import com.drew.metadata.Metadata
import io.github.pangju666.commons.image.model.ImageSize
import io.github.pangju666.commons.io.resource.IOResource
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.Path

class ImageIOResourceSpec extends Specification {
	@TempDir
	Path tempDir

	static final String TEST_IMAGES_DIR = "src/test/resources/images"
	static final List<String> TEST_IMAGES = ["test.jpg", "test.png", "test.gif", "test.bmp", "test.webp"]

	// 基于文件路径构造
	@Unroll
	def "基于文件路径构造ImageIOResource：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")

		when:
		def resource = new ImageIOResource(file.absolutePath)

		then:
		resource.size.toBytes() == file.length()
		resource.format == name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		resource.isImage()

		where:
		name << TEST_IMAGES
	}

	def "基于文件路径构造ImageIOResource with parseExifOrientation=false"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")

		when:
		def resource = new ImageIOResource(file.absolutePath, false)

		then:
		resource.size.toBytes() == file.length()
		resource.format == "JPG"
	}

	def "基于文件路径构造ImageIOResource with 自定义exifOrientation"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")

		when:
		def resource = new ImageIOResource(file.absolutePath, 6)

		then:
		resource.size.toBytes() == file.length()
		resource.format == "JPG"
	}

	def "基于文件路径构造ImageIOResource 非法exifOrientation抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")

		when:
		new ImageIOResource(file.absolutePath, 9)

		then:
		thrown(IllegalArgumentException)
	}

	def "基于文件路径构造ImageIOResource 空路径抛异常"() {
		when:
		new ImageIOResource("")

		then:
		thrown(IllegalArgumentException)
	}

	// 基于File对象构造
	@Unroll
	def "基于File对象构造ImageIOResource：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")

		when:
		def resource = new ImageIOResource(file)

		then:
		resource.size.toBytes() == file.length()
		resource.format == name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		resource.isImage()

		where:
		name << TEST_IMAGES
	}

	def "基于File对象构造ImageIOResource with parseExifOrientation=false"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")

		when:
		def resource = new ImageIOResource(file, false)

		then:
		resource.size.toBytes() == file.length()
		resource.format == "PNG"
	}

	def "基于File对象构造ImageIOResource with 自定义exifOrientation"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")

		when:
		def resource = new ImageIOResource(file, 3)

		then:
		resource.size.toBytes() == file.length()
		resource.format == "PNG"
	}

	def "基于File对象构造ImageIOResource null文件抛异常"() {
		when:
		new ImageIOResource(null as File)

		then:
		thrown(NullPointerException)
	}

	// 基于字节数组构造
	@Unroll
	def "基于字节数组构造ImageIOResource：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def bytes = Files.readAllBytes(file.toPath())

		when:
		def resource = new ImageIOResource(bytes)

		then:
		resource.size.toBytes() == bytes.length
		resource.format == null
		resource.isImage()

		where:
		name << TEST_IMAGES
	}

	def "基于字节数组构造ImageIOResource with parseExifOrientation=false"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def bytes = Files.readAllBytes(file.toPath())

		when:
		def resource = new ImageIOResource(bytes, false)

		then:
		resource.size.toBytes() == bytes.length
		resource.format == null
	}

	def "基于字节数组构造ImageIOResource with 自定义exifOrientation"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def bytes = Files.readAllBytes(file.toPath())

		when:
		def resource = new ImageIOResource(bytes, 5)

		then:
		resource.size.toBytes() == bytes.length
		resource.format == null
	}

	def "基于字节数组构造ImageIOResource 空数组抛异常"() {
		when:
		new ImageIOResource(new byte[0])

		then:
		thrown(IllegalArgumentException)
	}

	def "基于字节数组构造ImageIOResource null数组抛异常"() {
		when:
		new ImageIOResource(null as byte[])

		then:
		thrown(IllegalArgumentException)
	}

	// 基于输入流构造
	@Unroll
	def "基于输入流构造ImageIOResource：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def inputStream = new FileInputStream(file)

		when:
		def resource = new ImageIOResource(inputStream)

		then:
		resource.size.toBytes() == file.length()
		resource.format == null
		resource.isImage()

		where:
		name << TEST_IMAGES
	}

	def "基于输入流构造ImageIOResource with parseExifOrientation=false"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def inputStream = new FileInputStream(file)

		when:
		def resource = new ImageIOResource(inputStream, false)

		then:
		resource.size.toBytes() == file.length()
		resource.format == null
	}

	def "基于输入流构造ImageIOResource with 自定义exifOrientation"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def inputStream = new FileInputStream(file)

		when:
		def resource = new ImageIOResource(inputStream, 8)

		then:
		resource.size.toBytes() == file.length()
		resource.format == null
	}

	def "基于输入流构造ImageIOResource null流抛异常"() {
		when:
		new ImageIOResource(null as InputStream)

		then:
		thrown(NullPointerException)
	}

	// 基于IOResource构造
	def "基于IOResource构造ImageIOResource"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def ioResource = new IOResource(file)

		when:
		def imageResource = new ImageIOResource(ioResource)

		then:
		imageResource.size == ioResource.size
		imageResource.format == "JPG"
		imageResource.isImage()
	}

	def "基于IOResource构造ImageIOResource with parseExifOrientation=false"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def ioResource = new IOResource(file)

		when:
		def imageResource = new ImageIOResource(ioResource, false)

		then:
		imageResource.size == ioResource.size
		imageResource.format == "JPG"
	}

	def "基于IOResource构造ImageIOResource with 自定义exifOrientation"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def ioResource = new IOResource(file)

		when:
		def imageResource = new ImageIOResource(ioResource, 2)

		then:
		imageResource.size == ioResource.size
		imageResource.format == "JPG"
	}

	def "基于ImageIOResource构造ImageIOResource 共享format和exifOrientation"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def original = new ImageIOResource(file, 6)

		when:
		def copy = new ImageIOResource(original)

		then:
		copy.format == original.format
	}

	def "基于已关闭IOResource构造ImageIOResource抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def ioResource = new IOResource(file)
		ioResource.close()

		when:
		new ImageIOResource(ioResource)

		then:
		thrown(IllegalArgumentException)
	}

	// 非图像文件构造抛异常
	def "非图像文件构造ImageIOResource抛异常"() {
		given:
		def textFile = tempDir.resolve("test.txt").toFile()
		textFile.text = "not an image"

		when:
		new ImageIOResource(textFile)

		then:
		thrown(IllegalArgumentException)
	}

	def "非图像字节数组构造ImageIOResource抛异常"() {
		when:
		new ImageIOResource("not image data".bytes)

		then:
		thrown(IllegalArgumentException)
	}

	def "非图像输入流构造ImageIOResource抛异常"() {
		given:
		def inputStream = new ByteArrayInputStream("not image".bytes)

		when:
		new ImageIOResource(inputStream)

		then:
		thrown(IllegalArgumentException)
	}

	// getImageSize 测试
	@Unroll
	def "getImageSize 返回有效尺寸：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def resource = new ImageIOResource(file)

		when:
		def imageSize = resource.getImageSize()

		then:
		imageSize != null
		imageSize.width > 0
		imageSize.height > 0

		where:
		name << TEST_IMAGES
	}

	def "getImageSize 缓存结果"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)

		when:
		def size1 = resource.getImageSize()
		def size2 = resource.getImageSize()

		then:
		size1.is(size2)
	}

	def "getImageSize 已关闭资源抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)
		resource.close()

		when:
		resource.getImageSize()

		then:
		thrown(IllegalStateException)
	}

	// getMetadata 测试
	def "getMetadata 返回元数据"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)

		when:
		def metadata = resource.getMetadata()

		then:
		metadata != null
		metadata.directoryCount >= 0
	}

	def "getMetadata 缓存结果"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)

		when:
		def metadata1 = resource.getMetadata()
		def metadata2 = resource.getMetadata()

		then:
		metadata1.is(metadata2)
	}

	def "getMetadata 已关闭资源抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)
		resource.close()

		when:
		resource.getMetadata()

		then:
		thrown(IllegalStateException)
	}

	// getFormat 测试
	@Unroll
	def "getFormat 返回正确格式：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def resource = new ImageIOResource(file)

		expect:
		resource.format == expected

		where:
		name        | expected
		"test.jpg"  | "JPG"
		"test.png"  | "PNG"
		"test.gif"  | "GIF"
		"test.bmp"  | "BMP"
		"test.webp" | "WEBP"
	}

	def "getFormat 字节数组模式返回null"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def bytes = Files.readAllBytes(file.toPath())
		def resource = new ImageIOResource(bytes)

		expect:
		resource.format == null
	}

	def "getFormat 输入流模式返回null"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def inputStream = new FileInputStream(file)
		def resource = new ImageIOResource(inputStream)

		expect:
		resource.format == null
	}

	// getBufferedImage 测试
	@Unroll
	def "getBufferedImage 返回有效图像：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def resource = new ImageIOResource(file)

		when:
		def image = resource.getBufferedImage()

		then:
		image != null
		image.width > 0
		image.height > 0

		where:
		name << TEST_IMAGES
	}

	def "getBufferedImage 缓存结果"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)

		when:
		def image1 = resource.getBufferedImage()
		def image2 = resource.getBufferedImage()

		then:
		image1.is(image2)
	}

	def "getBufferedImage 已关闭资源抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)
		resource.close()

		when:
		resource.getBufferedImage()

		then:
		thrown(IllegalStateException)
	}

	// getBufferedImageCopy 测试
	@Unroll
	def "getBufferedImageCopy 返回有效副本：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def resource = new ImageIOResource(file)

		when:
		def copy = resource.getBufferedImageCopy()

		then:
		copy != null
		copy.width > 0
		copy.height > 0

		where:
		name << TEST_IMAGES
	}

	def "getBufferedImageCopy 返回独立副本"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)
		def original = resource.getBufferedImage()

		when:
		def copy = resource.getBufferedImageCopy()

		then:
		copy != null
		!copy.is(original)
		copy.width == original.width
		copy.height == original.height
	}

	def "getBufferedImageCopy 缓存为空时先加载"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)

		when:
		def copy = resource.getBufferedImageCopy()

		then:
		copy != null
		resource.getBufferedImage().is(resource.getBufferedImage())
	}

	def "getBufferedImageCopy 修改副本不影响缓存"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)
		def original = resource.getBufferedImage()
		def originalRgb = original.getRGB(0, 0)

		when:
		def copy = resource.getBufferedImageCopy()
		copy.setRGB(0, 0, 0xFFFFFF)
		def cached = resource.getBufferedImage()

		then:
		cached.getRGB(0, 0) == originalRgb
		copy.getRGB(0, 0) != originalRgb
	}

	def "getBufferedImageCopy 已关闭资源抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)
		resource.close()

		when:
		resource.getBufferedImageCopy()

		then:
		thrown(IllegalStateException)
	}

	// openImageInputStream 测试
	def "openImageInputStream 返回有效流"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)

		when:
		def imageInputStream = resource.newImageInputStream()

		then:
		imageInputStream != null
		imageInputStream.read() >= 0

		cleanup:
		imageInputStream?.close()
	}

	def "openImageInputStream 字节数组模式"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def bytes = Files.readAllBytes(file.toPath())
		def resource = new ImageIOResource(bytes)

		when:
		def imageInputStream = resource.newImageInputStream()

		then:
		imageInputStream != null
		imageInputStream.read() >= 0

		cleanup:
		imageInputStream?.close()
	}

	def "openImageInputStream 已关闭资源抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)
		resource.close()

		when:
		resource.newImageInputStream()

		then:
		thrown(IllegalStateException)
	}

	// setImageSize 测试
	def "setImageSize 设置图像尺寸"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)
		def customSize = new ImageSize(100, 200, 1)

		when:
		resource.setImageSize(customSize)

		then:
		resource.getImageSize() == customSize
	}

	def "setImageSize 已关闭资源抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)
		resource.close()

		when:
		resource.setImageSize(new ImageSize(100, 100))

		then:
		thrown(IllegalStateException)
	}

	// setMetadata 测试
	def "setMetadata 设置元数据"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)
		def customMetadata = new Metadata()

		when:
		resource.setMetadata(customMetadata)

		then:
		resource.getMetadata() == customMetadata
	}

	def "setMetadata 已关闭资源抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)
		resource.close()

		when:
		resource.setMetadata(new Metadata())

		then:
		thrown(IllegalStateException)
	}

	// close 测试
	def "close 清理资源"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)
		resource.getBufferedImage()
		resource.getMetadata()
		resource.getImageSize()

		when:
		resource.close()

		then:
		resource.isClosed()
	}

	def "close 重复关闭安全"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)

		when:
		resource.close()
		resource.close()

		then:
		noExceptionThrown()
	}

	def "close 后清空缓存"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)
		resource.getBufferedImage()

		when:
		resource.close()

		then:
		resource.isClosed()
	}

	// isClosed 测试
	def "isClosed 未关闭返回false"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)

		expect:
		!resource.isClosed()
	}

	def "isClosed 关闭后返回true"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)

		when:
		resource.close()

		then:
		resource.isClosed()
	}

	// 继承自IOResource的方法测试
	def "getSize 继承方法正常工作"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)

		expect:
		resource.size.toBytes() == file.length()
	}

	def "getMimeType 继承方法正常工作"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)

		expect:
		resource.mimeType != null
		resource.mimeType.startsWith("image/")
	}

	def "isImage 继承方法返回true"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)

		expect:
		resource.isImage()
	}

	def "isText 继承方法返回false"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)

		expect:
		!resource.isText()
	}

	def "isVideo 继承方法返回false"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)

		expect:
		!resource.isVideo()
	}

	def "isAudio 继承方法返回false"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new ImageIOResource(file)

		expect:
		!resource.isAudio()
	}
}
