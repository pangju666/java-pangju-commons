package io.github.pangju666.commons.tesseract.utils

import io.github.pangju666.commons.tesseract.lang.TesseractConstants
import org.bytedeco.tesseract.TessBaseAPI
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Unroll

import javax.imageio.ImageIO
import javax.imageio.stream.ImageInputStream
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path

class OcrUtilsSpec extends Specification {
	@TempDir
	Path tempDir

	static final String TEST_IMAGES_DIR = "src/test/resources/images"

	def "从RenderedImage识别 - 便捷方式"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")
		def image = ImageIO.read(file)

		when:
		def text = OcrUtils.ocrImage(image)

		then:
		text != null
		text instanceof String
	}

	def "从RenderedImage识别 - null抛异常"() {
		when:
		OcrUtils.ocrImage((BufferedImage) null)

		then:
		thrown(NullPointerException)
	}

	def "从ImageInputStream识别 - 便捷方式"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")
		def imageInputStream = ImageIO.createImageInputStream(file)

		when:
		def text = OcrUtils.ocrImage(imageInputStream)

		then:
		text != null
		text instanceof String

		cleanup:
		imageInputStream.close()
	}

	def "从ImageInputStream识别 - null抛异常"() {
		when:
		OcrUtils.ocrImage((ImageInputStream) null)

		then:
		thrown(NullPointerException)
	}

	@Unroll
	def "从字节数组识别 - 便捷方式：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")
		def bytes = Files.readAllBytes(file.toPath())

		when:
		def text = OcrUtils.ocrImage(bytes)

		then:
		text != null
		text instanceof String
	}

	def "从字节数组识别 - null抛异常"() {
		when:
		OcrUtils.ocrImage((byte[]) null)

		then:
		thrown(IllegalArgumentException)
	}

	def "从字节数组识别 - 空数组抛异常"() {
		when:
		OcrUtils.ocrImage(new byte[0])

		then:
		thrown(IllegalArgumentException)
	}

	def "从字节数组识别 - 不支持的格式抛异常"() {
		given:
		def bytes = "not an image".getBytes()

		when:
		OcrUtils.ocrImage(bytes)

		then:
		thrown(IllegalArgumentException)
	}

	@Unroll
	def "从文件识别 - 便捷方式：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")

		when:
		def text = OcrUtils.ocrImage(file)

		then:
		text != null
		text instanceof String
	}

	def "从文件识别 - null抛异常"() {
		when:
		OcrUtils.ocrImage((File) null)

		then:
		thrown(NullPointerException)
	}

	def "从文件识别 - 不存在的文件抛异常"() {
		given:
		def file = new File(tempDir.toFile(), "nonexistent.jpg")

		when:
		OcrUtils.ocrImage(file)

		then:
		thrown(Exception)
	}

	@Unroll
	def "从输入流识别 - 便捷方式：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")
		def inputStream = new FileInputStream(file)

		when:
		def text = OcrUtils.ocrImage(inputStream)

		then:
		text != null
		text instanceof String
	}

	def "从输入流识别 - null抛异常"() {
		when:
		OcrUtils.ocrImage((InputStream) null)

		then:
		thrown(NullPointerException)
	}

	def "从RenderedImage识别 - 手动管理"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")
		def image = ImageIO.read(file)
		def tessBaseAPI = TesseractConstants.getDefaultTessBaseApiPool().borrowObject()

		when:
		def text = OcrUtils.ocrImage(tessBaseAPI, image)

		then:
		text != null
		text instanceof String

		cleanup:
		TesseractConstants.getDefaultTessBaseApiPool().returnObject(tessBaseAPI)
	}

	def "从RenderedImage识别 - 手动管理 - null tessBaseAPI抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")
		def image = ImageIO.read(file)

		when:
		OcrUtils.ocrImage((TessBaseAPI) null, image)

		then:
		thrown(NullPointerException)
	}

	def "从RenderedImage识别 - 手动管理 - null image抛异常"() {
		given:
		def tessBaseAPI = TesseractConstants.getDefaultTessBaseApiPool().borrowObject()

		when:
		OcrUtils.ocrImage(tessBaseAPI, (BufferedImage) null)

		then:
		thrown(NullPointerException)

		cleanup:
		TesseractConstants.getDefaultTessBaseApiPool().returnObject(tessBaseAPI)
	}

	def "从ImageInputStream识别 - 手动管理"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")
		def imageInputStream = ImageIO.createImageInputStream(file)
		def tessBaseAPI = TesseractConstants.getDefaultTessBaseApiPool().borrowObject()

		when:
		def text = OcrUtils.ocrImage(tessBaseAPI, imageInputStream)

		then:
		text != null
		text instanceof String

		cleanup:
		imageInputStream.close()
		TesseractConstants.getDefaultTessBaseApiPool().returnObject(tessBaseAPI)
	}

	def "从ImageInputStream识别 - 手动管理 - null tessBaseAPI抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")
		def imageInputStream = ImageIO.createImageInputStream(file)

		when:
		OcrUtils.ocrImage((TessBaseAPI) null, imageInputStream)

		then:
		thrown(NullPointerException)

		cleanup:
		imageInputStream.close()
	}

	def "从ImageInputStream识别 - 手动管理 - null imageInputStream抛异常"() {
		given:
		def tessBaseAPI = TesseractConstants.getDefaultTessBaseApiPool().borrowObject()

		when:
		OcrUtils.ocrImage(tessBaseAPI, (ImageInputStream) null)

		then:
		thrown(NullPointerException)

		cleanup:
		TesseractConstants.getDefaultTessBaseApiPool().returnObject(tessBaseAPI)
	}

	@Unroll
	def "从输入流识别 - 手动管理：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")
		def inputStream = new FileInputStream(file)
		def tessBaseAPI = TesseractConstants.getDefaultTessBaseApiPool().borrowObject()

		when:
		def text = OcrUtils.ocrImage(tessBaseAPI, inputStream)

		then:
		text != null
		text instanceof String

		cleanup:
		inputStream.close()
		TesseractConstants.getDefaultTessBaseApiPool().returnObject(tessBaseAPI)
	}

	def "从输入流识别 - 手动管理 - null tessBaseAPI抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")
		def inputStream = new FileInputStream(file)

		when:
		OcrUtils.ocrImage((TessBaseAPI) null, inputStream)

		then:
		thrown(NullPointerException)

		cleanup:
		inputStream.close()
	}

	def "从输入流识别 - 手动管理 - null inputStream抛异常"() {
		given:
		def tessBaseAPI = TesseractConstants.getDefaultTessBaseApiPool().borrowObject()

		when:
		OcrUtils.ocrImage(tessBaseAPI, (InputStream) null)

		then:
		thrown(NullPointerException)

		cleanup:
		TesseractConstants.getDefaultTessBaseApiPool().returnObject(tessBaseAPI)
	}

	@Unroll
	def "从字节数组识别 - 手动管理：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")
		def bytes = Files.readAllBytes(file.toPath())
		def tessBaseAPI = TesseractConstants.getDefaultTessBaseApiPool().borrowObject()

		when:
		def text = OcrUtils.ocrImage(tessBaseAPI, bytes)

		then:
		text != null
		text instanceof String

		cleanup:
		TesseractConstants.getDefaultTessBaseApiPool().returnObject(tessBaseAPI)
	}

	def "从字节数组识别 - 手动管理 - null tessBaseAPI抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")
		def bytes = Files.readAllBytes(file.toPath())

		when:
		OcrUtils.ocrImage((TessBaseAPI) null, bytes)

		then:
		thrown(NullPointerException)
	}

	def "从字节数组识别 - 手动管理 - null bytes抛异常"() {
		given:
		def tessBaseAPI = TesseractConstants.getDefaultTessBaseApiPool().borrowObject()

		when:
		OcrUtils.ocrImage(tessBaseAPI, (byte[]) null)

		then:
		thrown(IllegalArgumentException)

		cleanup:
		TesseractConstants.getDefaultTessBaseApiPool().returnObject(tessBaseAPI)
	}

	def "从字节数组识别 - 手动管理 - 空数组抛异常"() {
		given:
		def tessBaseAPI = TesseractConstants.getDefaultTessBaseApiPool().borrowObject()

		when:
		OcrUtils.ocrImage(tessBaseAPI, new byte[0])

		then:
		thrown(IllegalArgumentException)

		cleanup:
		TesseractConstants.getDefaultTessBaseApiPool().returnObject(tessBaseAPI)
	}

	def "从字节数组识别 - 手动管理 - 不支持的格式抛异常"() {
		given:
		def bytes = "not an image".getBytes()
		def tessBaseAPI = TesseractConstants.getDefaultTessBaseApiPool().borrowObject()

		when:
		OcrUtils.ocrImage(tessBaseAPI, bytes)

		then:
		thrown(IllegalArgumentException)

		cleanup:
		TesseractConstants.getDefaultTessBaseApiPool().returnObject(tessBaseAPI)
	}

	@Unroll
	def "从文件识别 - 手动管理：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")
		def tessBaseAPI = TesseractConstants.getDefaultTessBaseApiPool().borrowObject()

		when:
		def text = OcrUtils.ocrImage(tessBaseAPI, file)

		then:
		text != null
		text instanceof String

		cleanup:
		TesseractConstants.getDefaultTessBaseApiPool().returnObject(tessBaseAPI)
	}

	def "从文件识别 - 手动管理 - null tessBaseAPI抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")

		when:
		OcrUtils.ocrImage((TessBaseAPI) null, file)

		then:
		thrown(NullPointerException)
	}

	def "从文件识别 - 手动管理 - null file抛异常"() {
		given:
		def tessBaseAPI = TesseractConstants.getDefaultTessBaseApiPool().borrowObject()

		when:
		OcrUtils.ocrImage(tessBaseAPI, (File) null)

		then:
		thrown(NullPointerException)

		cleanup:
		TesseractConstants.getDefaultTessBaseApiPool().returnObject(tessBaseAPI)
	}

	def "从文件识别 - 手动管理 - 不存在的文件抛异常"() {
		given:
		def file = new File(tempDir.toFile(), "nonexistent.jpg")
		def tessBaseAPI = TesseractConstants.getDefaultTessBaseApiPool().borrowObject()

		when:
		OcrUtils.ocrImage(tessBaseAPI, file)

		then:
		thrown(Exception)

		cleanup:
		TesseractConstants.getDefaultTessBaseApiPool().returnObject(tessBaseAPI)
	}

	def "批量处理 - 手动管理对象池"() {
		given:
		def file1 = new File("${TEST_IMAGES_DIR}/test.png")
		def file2 = new File("${TEST_IMAGES_DIR}/test.png")
		def tessBaseAPI = TesseractConstants.getDefaultTessBaseApiPool().borrowObject()

		when:
		def text1 = OcrUtils.ocrImage(tessBaseAPI, file1)
		def text2 = OcrUtils.ocrImage(tessBaseAPI, file2)

		then:
		text1 != null
		text2 != null

		cleanup:
		TesseractConstants.getDefaultTessBaseApiPool().returnObject(tessBaseAPI)
	}
}
