package io.github.pangju666.commons.image.utils


import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ImageUtilsSpec extends Specification {
	@TempDir
	Path tempDir

	// 测试资源路径
	static final String TEST_IMAGES_DIR = "src/test/resources/"

	static final String[] TEST_IMAGES = [
		"${TEST_IMAGES_DIR}test.bmp",
		"${TEST_IMAGES_DIR}test.gif",
		"${TEST_IMAGES_DIR}test.ico",
		"${TEST_IMAGES_DIR}test.iff",
		"${TEST_IMAGES_DIR}test.jpg",
		"${TEST_IMAGES_DIR}test.pcx",
		"${TEST_IMAGES_DIR}test.png",
		"${TEST_IMAGES_DIR}test.svg",
		"${TEST_IMAGES_DIR}test.tga",
		"${TEST_IMAGES_DIR}test.tif",
		"${TEST_IMAGES_DIR}test.webp",
		"${TEST_IMAGES_DIR}test.xwd"
	]

	def "test"() {
		setup:
		/*for (final def mimeType in Set.of(ImageIO.getReaderMIMETypes())) {
			println mimeType
		}*/
		println ImageUtils.getImageSize(new FileInputStream(new File("D:\\workspace\\resource\\图片\\wac_nearside.tif")))
	}

	// 测试 isSupportImageType 系列方法
	@Unroll
	def "isSupportImageType应该正确识别文件"() {
		given:
		def file = new File(TEST_IMAGES[index])

		when:
		def result = ImageUtils.isSupportImageType(inputType == 'File' ? file :
			inputType == 'Path' ? file.toPath() :
				inputType == 'Bytes' ? Files.readAllBytes(file.toPath()) :
					new FileInputStream(file))

		then:
		result == expected

		where:
		index | inputType     || expected
		0     | 'File'        || true
		1     | 'Path'        || true
		2     | 'Bytes'       || true
		3     | 'InputStream' || true
		4     | 'File'        || true
		5     | 'File'        || true
	}

	// 测试 isSameImageType 系列方法
	@Unroll
	def "isSameImageType应该正确比较"() {
		given:
		def testData = TEST_IMAGES[index]
		def input = inputType == 'File' ? new File(testData) :
			inputType == 'Path' ? Paths.get(testData) :
				inputType == 'Bytes' ? Files.readAllBytes(Paths.get(testData)) :
					new FileInputStream(testData)

		when:
		def result = ImageUtils.isSameImageType(input, mimeType)

		then:
		result == expected

		where:
		index | inputType     | mimeType                   || expected
		0     | 'File'        | 'image/x-bmp'              || true
		10    | 'Path'        | 'image/x-webp'             || true
		2     | 'Bytes'       | 'image/vnd.microsoft.icon' || true
		9     | 'InputStream' | 'image/x-tiff'             || true
		6     | 'File'        | 'image/jpeg'               || false
		5     | 'File'        | 'image/x-pcx'              || true
		3     | 'File'        | 'image/x-iff'              || true
		8     | 'File'        | 'image/x-tga'              || true
	}

	// 测试 getImageType 系列方法
	@Unroll
	def "getImageType应该正确识别的类型"() {
		given:
		def testData = TEST_IMAGES[index]
		def input = inputType == 'File' ? new File(testData) :
			inputType == 'Path' ? Paths.get(testData) :
				inputType == 'Bytes' ? Files.readAllBytes(Paths.get(testData)) :
					new FileInputStream(testData)

		when:
		def result = ImageUtils.getImageType(input)

		then:
		result == expectedType

		where:
		index | inputType     | expectedType
		0     | 'File'        | 'image/bmp'
		10    | 'Path'        | 'image/webp'
		2     | 'Bytes'       | 'image/x-icon'
		9     | 'InputStream' | 'image/tiff'
		6     | 'File'        | 'image/png'
		5     | 'File'        | 'image/x-pcx'
		3     | 'File'        | 'image/iff'
		8     | 'File'        | 'image/tga'
	}

	// 测试 getImageSize 系列方法
	@Unroll
	def "getImageSize应该正确获取的尺寸"() {
		given:
		def testData = TEST_IMAGES[index]
		def input = inputType == 'File' ? new File(testData) :
			inputType == 'Path' ? Paths.get(testData) :
				inputType == 'Bytes' ? Files.readAllBytes(Paths.get(testData)) :
					new FileInputStream(testData)

		when:
		def size = ImageUtils.getImageSize(input)

		then:
		size?.width() == expectedWidth
		size?.height() == expectedHeight

		where:
		index | inputType     | expectedType   | expectedWidth | expectedHeight
		0     | 'File'        | 'image/bmp'    | 600           | 600
		10    | 'Path'        | 'image/webp'   | 1170          | 1428
		2     | 'Bytes'       | 'image/x-icon' | 32            | 32
		9     | 'InputStream' | 'image/tiff'   | 600           | 600
		6     | 'File'        | 'image/png'    | 860           | 540
		5     | 'File'        | 'image/x-pcx'  | 260           | 450
		3     | 'File'        | 'image/iff'    | 320           | 200
		8     | 'File'        | 'image/tga'    | 600           | 600
	}

	// 测试异常情况
	def "getImageSize应该正确处理无效输入"() {
		when:
		def size = ImageUtils.getImageSize(new byte[0])

		then:
		size == null
	}

	// 添加性能测试
	def "性能测试：处理大文件"() {
		given:
		def largeFile = new File("${TEST_IMAGES_DIR}/large.png")

		when:
		def result = ImageUtils.getImageSize(largeFile)

		then:
		result != null
	}

	// 添加并发测试
	def "并发安全测试"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/large.png")

		when:
		def results = (1..100).parallelStream().map {
			ImageUtils.getImageSize(file)
		}.toList()

		then:
		results.every { it?.width() == 4095 }
	}
}
