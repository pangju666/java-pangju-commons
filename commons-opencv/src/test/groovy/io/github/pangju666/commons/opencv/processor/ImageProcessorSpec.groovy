package io.github.pangju666.commons.opencv.processor

import io.github.pangju666.commons.opencv.enums.FlipDirection
import io.github.pangju666.commons.opencv.enums.RotateDirection
import io.github.pangju666.commons.opencv.io.resource.OpenCvResource
import io.github.pangju666.commons.opencv.model.ImageWatermarkOption
import io.github.pangju666.commons.opencv.model.TextWatermarkOption
import io.github.pangju666.commons.opencv.utils.OpenCvUtils
import org.bytedeco.javacpp.BytePointer
import org.bytedeco.opencv.global.opencv_imgcodecs
import org.bytedeco.opencv.opencv_core.Mat
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.Path

class ImageProcessorSpec extends Specification {
	@TempDir
	Path tempDir

	@Shared
	def TEST_IMAGES_DIR = "src/test/resources/images"
	@Shared
	def SUPPORTED_IMAGES = [
		"test.jpg",
		"test.png",
		"test.bmp",
		"test.tiff",
	]

	def "从OpenCvResource创建编辑器"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new OpenCvResource(file)

		when:
		def editor = ImageProcessor.of(resource)

		then:
		editor != null

		cleanup:
		editor.release()
		resource.close()
	}

	def "从OpenCvResource创建编辑器 - null资源抛异常"() {
		when:
		ImageProcessor.of((OpenCvResource) null)

		then:
		thrown(NullPointerException)
	}

	@Unroll
	def "从输入流创建编辑器：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def inputStream = new FileInputStream(file)

		when:
		def editor = ImageProcessor.of(inputStream)

		then:
		editor != null

		cleanup:
		editor.release()
		inputStream.close()

		where:
		name << SUPPORTED_IMAGES
	}

	def "从输入流创建编辑器 - null输入流抛异常"() {
		when:
		ImageProcessor.of((InputStream) null)

		then:
		thrown(NullPointerException)
	}

	@Unroll
	def "从输入流创建编辑器（指定flags）：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def inputStream = new FileInputStream(file)

		when:
		def editor = ImageProcessor.of(inputStream, opencv_imgcodecs.IMREAD_GRAYSCALE)

		then:
		editor != null

		cleanup:
		editor.release()
		inputStream.close()

		where:
		name << SUPPORTED_IMAGES
	}

	def "从输入流创建编辑器（指定flags和exifOrientation）"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def inputStream = new FileInputStream(file)

		when:
		def editor = ImageProcessor.of(inputStream, opencv_imgcodecs.IMREAD_UNCHANGED, 6)

		then:
		editor != null

		cleanup:
		editor.release()
		inputStream.close()
	}

	def "从输入流创建编辑器 - 无效exifOrientation抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def inputStream = new FileInputStream(file)

		when:
		ImageProcessor.of(inputStream, opencv_imgcodecs.IMREAD_UNCHANGED, 9)

		then:
		thrown(IllegalArgumentException)

		cleanup:
		inputStream.close()
	}

	@Unroll
	def "从BytePointer创建编辑器：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def bytes = Files.readAllBytes(file.toPath())
		def bytePointer = new BytePointer(bytes)

		when:
		def editor = ImageProcessor.of(bytePointer)

		then:
		editor != null

		cleanup:
		editor.release()
		bytePointer.close()

		where:
		name << SUPPORTED_IMAGES
	}

	def "从BytePointer创建编辑器 - null抛异常"() {
		when:
		ImageProcessor.of((BytePointer) null)

		then:
		thrown(NullPointerException)
	}

	@Unroll
	def "从BytePointer创建编辑器（指定flags）：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def bytes = Files.readAllBytes(file.toPath())
		def bytePointer = new BytePointer(bytes)

		when:
		def editor = ImageProcessor.of(bytePointer, opencv_imgcodecs.IMREAD_GRAYSCALE)

		then:
		editor != null

		cleanup:
		editor.release()
		bytePointer.close()

		where:
		name << SUPPORTED_IMAGES
	}

	def "从BytePointer创建编辑器（指定flags和exifOrientation）"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def bytes = Files.readAllBytes(file.toPath())
		def bytePointer = new BytePointer(bytes)

		when:
		def editor = ImageProcessor.of(bytePointer, opencv_imgcodecs.IMREAD_UNCHANGED, 6)

		then:
		editor != null

		cleanup:
		editor.release()
		bytePointer.close()
	}

	def "从BytePointer创建编辑器 - 无效exifOrientation抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def bytes = Files.readAllBytes(file.toPath())
		def bytePointer = new BytePointer(bytes)

		when:
		ImageProcessor.of(bytePointer, opencv_imgcodecs.IMREAD_UNCHANGED, 9)

		then:
		thrown(IllegalArgumentException)

		cleanup:
		bytePointer.close()
	}

	def "从Mat创建编辑器"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def mat = OpenCvUtils.read(file)

		when:
		def editor = ImageProcessor.of(mat)

		then:
		editor != null

		cleanup:
		editor.release()
		mat.release()
	}

	def "从Mat创建编辑器 - null抛异常"() {
		when:
		ImageProcessor.of((Mat) null)

		then:
		thrown(IllegalArgumentException)
	}

	def "从Mat创建编辑器（指定exifOrientation）"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def mat = OpenCvUtils.read(file)

		when:
		def editor = ImageProcessor.of(mat, 6)

		then:
		editor != null

		cleanup:
		editor.release()
		mat.releaseReference()
	}

	def "从Mat创建编辑器 - 无效exifOrientation抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def mat = OpenCvUtils.read(file)

		when:
		ImageProcessor.of(mat, 9)

		then:
		thrown(IllegalArgumentException)

		cleanup:
		mat.release()
	}

	def "按宽度缩放"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))

		when:
		editor.scaleByWidth(400)

		then:
		editor.toMat().cols() == 400

		cleanup:
		editor.release()
	}

	def "按高度缩放"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))

		when:
		editor.scaleByHeight(300)

		then:
		editor.toMat().rows() == 300

		cleanup:
		editor.release()
	}

	def "按比例缩放"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))
		def originalWidth = editor.toMat().size().width()

		when:
		editor.scale(0.5)

		then:
		editor.toMat().size().width() == (int) Math.round(originalWidth / 2)

		cleanup:
		editor.release()
	}

	def "按目标尺寸缩放"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))

		when:
		editor.scale(400, 300)

		then:
		editor.toMat().cols() <= 400
		editor.toMat().rows() <= 300

		cleanup:
		editor.release()
	}

	def "强制缩放到指定尺寸"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))

		when:
		editor.resize(100, 100)

		then:
		editor.toMat().cols() == 100
		editor.toMat().rows() == 100

		cleanup:
		editor.release()
	}

	def "居中裁剪"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))

		when:
		editor.cropByCenter(400, 400)

		then:
		editor.toMat().cols() == 400
		editor.toMat().rows() == 400

		cleanup:
		editor.release()
	}

	def "按矩形区域裁剪"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))

		when:
		editor.cropByRect(0, 0, 200, 200)

		then:
		editor.toMat().cols() == 200
		editor.toMat().rows() == 200

		cleanup:
		editor.release()
	}

	def "按边距裁剪"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))
		def originalWidth = editor.toMat().size().width()
		def originalHeight = editor.toMat().size().height()

		when:
		editor.cropByOffset(10, 10, 20, 20)

		then:
		editor.toMat().size().width() == originalWidth - 40
		editor.toMat().size().height() == originalHeight - 20

		cleanup:
		editor.release()
	}

	def "旋转固定方向"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))
		def originalWidth = editor.toMat().cols()
		def originalHeight = editor.toMat().rows()

		when:
		editor.rotate(RotateDirection.CLOCKWISE_90)

		then:
		editor.toMat().cols() == originalHeight
		editor.toMat().rows() == originalWidth

		cleanup:
		editor.release()
	}

	def "旋转任意角度"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))

		when:
		editor.rotate(45)

		then:
		editor.toMat() != null

		cleanup:
		editor.release()
	}

	def "翻转图像"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))

		when:
		editor.flip(FlipDirection.HORIZONTAL)

		then:
		editor.toMat() != null

		cleanup:
		editor.release()
	}

	def "平移图像"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))

		when:
		editor.translate(10, 20)

		then:
		editor.toMat() != null

		cleanup:
		editor.release()
	}

	def "灰度化"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))

		when:
		editor.grayscale()

		then:
		editor.toMat() != null
		editor.toMat().channels() == 1

		cleanup:
		editor.release()
	}

	def "调整透明度"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")
		def editor = ImageProcessor.of(new OpenCvResource(file))

		when:
		editor.opacity(0.5f)

		then:
		editor.toMat() != null

		cleanup:
		editor.release()
	}

	def "均值模糊"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))

		when:
		editor.blur()

		then:
		editor.toMat() != null

		cleanup:
		editor.release()
	}

	def "高斯模糊"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))

		when:
		editor.gaussianBlur()

		then:
		editor.toMat() != null

		cleanup:
		editor.release()
	}

	def "中值模糊"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))

		when:
		editor.medianBlur(5)

		then:
		editor.toMat() != null

		cleanup:
		editor.release()
	}

	def "锐化"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))

		when:
		editor.sharpen()

		then:
		editor.toMat() != null

		cleanup:
		editor.release()
	}

	def "浮雕效果"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))

		when:
		editor.emboss()

		then:
		editor.toMat() != null

		cleanup:
		editor.release()
	}

	def "调整对比度"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))

		when:
		editor.contrast(0.3f)

		then:
		editor.toMat() != null

		cleanup:
		editor.release()
	}

	def "调整亮度"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))

		when:
		editor.brightness(20)

		then:
		editor.toMat() != null

		cleanup:
		editor.release()
	}

	def "阈值处理"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))
		editor.grayscale()

		when:
		editor.threshold()

		then:
		editor.toMat() != null

		cleanup:
		editor.release()
	}

	def "自适应阈值"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))
		editor.grayscale()

		when:
		editor.adaptiveThreshold()

		then:
		editor.toMat() != null

		cleanup:
		editor.release()
	}

	def "添加文字水印"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))
		def option = new TextWatermarkOption()

		when:
		editor.addTextWatermark("TEST", option)

		then:
		editor.toMat() != null

		cleanup:
		editor.release()
	}

	def "添加文字水印 - 空文本抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))
		def option = new TextWatermarkOption()

		when:
		editor.addTextWatermark("", option)

		then:
		thrown(IllegalArgumentException)

		cleanup:
		editor.release()
	}

	def "添加图片水印"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def watermarkFile = new File("${TEST_IMAGES_DIR}/test.png")
		def editor = ImageProcessor.of(new OpenCvResource(file))
		def option = new ImageWatermarkOption()

		when:
		editor.addImageWatermark(watermarkFile, option)

		then:
		editor.toMat() != null

		cleanup:
		editor.release()
	}

	def "添加图片水印 - null文件抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))
		def option = new ImageWatermarkOption()

		when:
		editor.addImageWatermark((File) null, option)

		then:
		thrown(NullPointerException)

		cleanup:
		editor.release()
	}

	def "应用自定义操作"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))

		when:
		editor.apply({ mat -> mat.clone() })

		then:
		editor.toMat() != null

		cleanup:
		editor.release()
	}

	def "应用自定义操作 - null函数抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))

		when:
		editor.apply(null)

		then:
		thrown(NullPointerException)

		cleanup:
		editor.release()
	}

	@Unroll
	def "保存到文件：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def editor = ImageProcessor.of(new OpenCvResource(file))
		def outputFile = new File(tempDir.toFile(), "output_${name}")

		when:
		def result = editor.toFile(outputFile)

		then:
		result
		outputFile.exists()
		outputFile.length() > 0

		cleanup:
		editor.release()

		where:
		name << SUPPORTED_IMAGES
	}

	def "保存到文件 - null文件抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))

		when:
		editor.toFile(null)

		then:
		thrown(NullPointerException)

		cleanup:
		editor.release()
	}

	@Unroll
	def "转换为字节数组：#format"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))

		when:
		def bytes = editor.toBytes(format)

		then:
		bytes != null
		bytes.length > 0

		cleanup:
		editor.release()

		where:
		format << ["jpg", "png", "bmp"]
	}

	def "转换为字节数组 - 不支持的格式抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))

		when:
		editor.toBytes("unsupported")

		then:
		thrown(IllegalArgumentException)

		cleanup:
		editor.release()
	}

	def "获取Mat"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))

		when:
		def mat = editor.toMat()

		then:
		mat != null
		!OpenCvUtils.isEmpty(mat)

		cleanup:
		editor.release()
	}

	def "重置到初始状态"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))
		def originalMat = editor.toMat().clone()

		when:
		editor.scaleByWidth(400)
		editor.reset()

		then:
		editor.toMat().cols() == originalMat.cols()
		editor.toMat().rows() == originalMat.rows()

		cleanup:
		editor.release()
		originalMat.release()
	}

	def "释放资源"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def editor = ImageProcessor.of(new OpenCvResource(file))

		when:
		editor.release()

		then:
		editor.toMat() == null
	}

	def "链式调用"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def outputFile = new File(tempDir.toFile(), "output_chain.jpg")

		when:
		def result = ImageProcessor.of(new OpenCvResource(file))
			.scaleByWidth(400)
			.grayscale()
			.toFile(outputFile)

		then:
		result
		outputFile.exists()
		outputFile.length() > 0
	}
}
