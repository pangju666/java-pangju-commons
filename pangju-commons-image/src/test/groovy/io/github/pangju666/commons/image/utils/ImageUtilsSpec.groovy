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
		def result = ImageUtils.isSameType(input, mimeType)

		then:
		result == expected

		where:
		index | inputType     | mimeType                   || expected
		0     | 'File'        | 'image/x-bmp'              || true
		1  | 'Bytes'       | 'image/gif'                || true
		2  | 'InputStream' | 'image/vnd.microsoft.icon' || true
		3     | 'File'        | 'image/x-iff'              || true
		4  | 'File'        | 'image/jpeg'               || true
		5  | 'File'        | 'image/pcx'                || true
		6  | 'File'        | 'image/png'                || true
		7  | 'File'        | 'image/svg+xml'            || true
		8  | 'File'        | 'image/tga'                || true
		9  | 'File'        | 'image/tiff'               || true
		10 | 'File'        | 'image/webp'               || true
		11 | 'File'        | 'image/xwd'                || true
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
		def result = ImageUtils.getMimeType(input)

		then:
		result == expectedType

		where:
		index | inputType     | expectedType
		0     | 'File'        | 'image/bmp'
		1  | 'Bytes'       | 'image/gif'
		2  | 'InputStream' | 'image/vnd.microsoft.icon'
		3     | 'File'        | 'image/iff'
		4  | 'File'        | 'image/jpeg'
		5  | 'File'        | 'image/pcx'
		6  | 'File'        | 'image/png'
		7  | 'File'        | 'image/svg'
		8     | 'File'        | 'image/tga'
		9  | 'File'        | 'image/tiff'
		10 | 'File'        | 'image/webp'
		11 | 'File'        | 'image/xwd'
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
		def size = ImageUtils.getSize(input)

		then:
		size?.width() == expectedWidth
		size?.height() == expectedHeight

		where:
		index | inputType     | expectedType   | expectedWidth | expectedHeight
		0     | 'File'        | 'image/bmp'    | 600           | 600
		2     | 'Bytes'       | 'image/x-icon' | 32            | 32
		9 | 'InputStream' | 'image/tiff' | 4095 | 2559
		6     | 'File'        | 'image/png'    | 860           | 540
		5     | 'File'        | 'image/x-pcx'  | 260           | 450
		3     | 'File'        | 'image/iff'    | 320           | 200
		8     | 'File'        | 'image/tga'    | 600           | 600
	}

	// 测试异常情况
	def "getImageSize应该正确处理无效输入"() {
		when:
		def size = ImageUtils.getSize(new byte[0])

		then:
		size == null
	}

	// 添加性能测试
	def "性能测试：处理大文件"() {
		given:
		def largeFile = new File("${TEST_IMAGES_DIR}/large.png")

		when:
		def result = ImageUtils.getSize(largeFile)

		then:
		result != null
	}

	// 添加并发测试
	def "并发安全测试"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/large.png")

		when:
		def results = (1..100).parallelStream().map {
			ImageUtils.getSize(file)
		}.toList()

		then:
		results.every { it?.width() == 4095 }
	}
}
