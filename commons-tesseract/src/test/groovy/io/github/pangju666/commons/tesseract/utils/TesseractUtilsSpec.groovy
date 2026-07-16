package io.github.pangju666.commons.tesseract.utils

import io.github.pangju666.commons.tesseract.enums.PageSegmentationMode
import io.github.pangju666.commons.tesseract.factory.TessBaseAPIFactory
import io.github.pangju666.commons.tesseract.io.resource.TesseractResource
import io.github.pangju666.commons.tesseract.model.TessBaseAPIOptions
import org.bytedeco.leptonica.PIX
import spock.lang.Specification

class TesseractUtilsSpec extends Specification {
	static final String TEST_IMAGES_DIR = "src/test/resources"

	def "从TesseractResource识别 - 正常情况"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")
		def resource = new TesseractResource(file)
		def options = new TessBaseAPIOptions()
		def factory = new TessBaseAPIFactory()
		def tessBaseAPI = factory.create()

		when:
		def text = TesseractUtils.ocrImage(tessBaseAPI, resource, options)

		then:
		text != null
		text instanceof String

		cleanup:
		tessBaseAPI.End()
		tessBaseAPI.releaseReference()
	}

	def "从TesseractResource识别 - null tessBaseAPI抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")
		def resource = new TesseractResource(file)
		def options = new TessBaseAPIOptions()

		when:
		TesseractUtils.ocrImage(null, resource, options)

		then:
		thrown(NullPointerException)
	}

	def "从TesseractResource识别 - null resource抛异常"() {
		given:
		def options = new TessBaseAPIOptions()
		def factory = new TessBaseAPIFactory()
		def tessBaseAPI = factory.create()

		when:
		TesseractUtils.ocrImage(tessBaseAPI, null as TesseractResource, options)

		then:
		thrown(NullPointerException)

		cleanup:
		tessBaseAPI.End()
		tessBaseAPI.releaseReference()
	}

	def "从TesseractResource识别 - null options抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")
		def resource = new TesseractResource(file)
		def factory = new TessBaseAPIFactory()
		def tessBaseAPI = factory.create()

		when:
		TesseractUtils.ocrImage(tessBaseAPI, resource, null)

		then:
		thrown(NullPointerException)

		cleanup:
		tessBaseAPI.End()
		tessBaseAPI.releaseReference()
	}

	def "从PIX识别 - 正常情况"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")
		def resource = new TesseractResource(file)
		def options = new TessBaseAPIOptions()
		def factory = new TessBaseAPIFactory()
		def tessBaseAPI = factory.create()
		PIX pix = resource.getPix()

		when:
		def text = TesseractUtils.ocrImage(tessBaseAPI, pix, options)

		then:
		text != null
		text instanceof String

		cleanup:
		pix.close()
		tessBaseAPI.End()
		tessBaseAPI.releaseReference()
	}

	def "从PIX识别 - null tessBaseAPI抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")
		def resource = new TesseractResource(file)
		def options = new TessBaseAPIOptions()
		PIX pix = resource.getPix()

		when:
		TesseractUtils.ocrImage(null, pix, options)

		then:
		thrown(NullPointerException)

		cleanup:
		pix.close()
	}

	def "从PIX识别 - null image抛异常"() {
		given:
		def options = new TessBaseAPIOptions()
		def factory = new TessBaseAPIFactory()
		def tessBaseAPI = factory.create()

		when:
		TesseractUtils.ocrImage(tessBaseAPI, (PIX) null, options)

		then:
		thrown(NullPointerException)

		cleanup:
		tessBaseAPI.End()
		tessBaseAPI.releaseReference()
	}

	def "从PIX识别 - null options抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")
		def resource = new TesseractResource(file)
		def factory = new TessBaseAPIFactory()
		def tessBaseAPI = factory.create()
		PIX pix = resource.getPix()

		when:
		TesseractUtils.ocrImage(tessBaseAPI, pix, null)

		then:
		thrown(NullPointerException)

		cleanup:
		pix.close()
		tessBaseAPI.End()
		tessBaseAPI.releaseReference()
	}

	def "从PIX识别 - 使用页面分割模式"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")
		def resource = new TesseractResource(file)
		def options = new TessBaseAPIOptions()
		options.setPsm(PageSegmentationMode.AUTO_NO_OSD)
		def factory = new TessBaseAPIFactory()
		def tessBaseAPI = factory.create()
		PIX pix = resource.getPix()

		when:
		def text = TesseractUtils.ocrImage(tessBaseAPI, pix, options)

		then:
		text != null
		text instanceof String

		cleanup:
		pix.close()
		tessBaseAPI.End()
		tessBaseAPI.releaseReference()
	}

	def "从PIX识别 - 使用图像分辨率"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")
		def resource = new TesseractResource(file)
		def options = new TessBaseAPIOptions()
		options.setPpi(300)
		def factory = new TessBaseAPIFactory()
		def tessBaseAPI = factory.create()
		PIX pix = resource.getPix()

		when:
		def text = TesseractUtils.ocrImage(tessBaseAPI, pix, options)

		then:
		text != null
		text instanceof String

		cleanup:
		pix.close()
		tessBaseAPI.End()
		tessBaseAPI.releaseReference()
	}

	def "从PIX识别 - 使用识别区域"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")
		def resource = new TesseractResource(file)
		def options = new TessBaseAPIOptions()
		options.setRectangle(0, 0, 100, 100)
		def factory = new TessBaseAPIFactory()
		def tessBaseAPI = factory.create()
		PIX pix = resource.getPix()

		when:
		def text = TesseractUtils.ocrImage(tessBaseAPI, pix, options)

		then:
		text != null
		text instanceof String

		cleanup:
		pix.close()
		tessBaseAPI.End()
		tessBaseAPI.releaseReference()
	}

	def "批量处理 - 使用同一个TessBaseAPI"() {
		given:
		def file1 = new File("${TEST_IMAGES_DIR}/test.png")
		def file2 = new File("${TEST_IMAGES_DIR}/test.png")
		def resource1 = new TesseractResource(file1)
		def resource2 = new TesseractResource(file2)
		def options = new TessBaseAPIOptions()
		def factory = new TessBaseAPIFactory()
		def tessBaseAPI = factory.create()

		when:
		def text1 = TesseractUtils.ocrImage(tessBaseAPI, resource1, options)
		def text2 = TesseractUtils.ocrImage(tessBaseAPI, resource2, options)

		then:
		text1 != null
		text2 != null

		cleanup:
		tessBaseAPI.End()
		tessBaseAPI.releaseReference()
	}
}