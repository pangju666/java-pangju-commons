package io.github.pangju666.commons.opencv.utils


import org.bytedeco.opencv.global.opencv_imgcodecs
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Size
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Unroll

import java.awt.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.List

class OpenCvUtilsSpec extends Specification {
	@TempDir
	Path tempDir

	static final String TEST_IMAGES_DIR = "src/test/resources/images"
	static final List<String> SUPPORTED_IMAGES = [
		"test.jpg",
		"test.png",
		"test.bmp",
		"test.tiff",
	]

	def "判断 Mat 为空 - null"() {
		expect:
		OpenCvUtils.isEmpty(null)
	}

	def "判断 Mat 为空 - 空对象"() {
		given:
		def mat = new Mat()

		expect:
		OpenCvUtils.isEmpty(mat)
	}

	def "判断 Mat 为空 - 有数据"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def mat = OpenCvUtils.read(file)

		expect:
		!OpenCvUtils.isEmpty(mat)

		cleanup:
		mat.release()
	}

	def "判断 Mat 不为空 - null"() {
		expect:
		!OpenCvUtils.isNotEmpty(null)
	}

	def "判断 Mat 不为空 - 有数据"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def mat = OpenCvUtils.read(file)

		expect:
		OpenCvUtils.isNotEmpty(mat)

		cleanup:
		mat.release()
	}

	@Unroll
	def "检查是否可以读取图像文件：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")

		expect:
		OpenCvUtils.canRead(file)

		where:
		name << SUPPORTED_IMAGES
	}

	def "检查是否可以读取非图像文件抛异常"() {
		given:
		def nonImageFile = new File(tempDir.toFile(), "test.txt")
		nonImageFile.write("test content")

		when:
		OpenCvUtils.canRead(nonImageFile)

		then:
		thrown(IllegalArgumentException)
	}

	def "检查是否可以写入图像格式"() {
		expect:
		OpenCvUtils.canWrite("jpg")
		OpenCvUtils.canWrite("png")
		OpenCvUtils.canWrite(".jpg")
		OpenCvUtils.canWrite(".png")
	}

	def "检查是否可以写入空格式抛异常"() {
		when:
		OpenCvUtils.canWrite("")

		then:
		thrown(IllegalArgumentException)
	}

	def "检查是否可以写入null格式抛异常"() {
		when:
		OpenCvUtils.canWrite(null)

		then:
		thrown(NullPointerException)
	}

	@Unroll
	def "获取图像尺寸：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")

		when:
		def size = OpenCvUtils.getSize(file)

		then:
		size != null
		size.width() > 0
		size.height() > 0

		where:
		name << SUPPORTED_IMAGES
	}

	def "获取非图像文件尺寸抛异常"() {
		given:
		def nonImageFile = new File(tempDir.toFile(), "test.txt")
		nonImageFile.write("test content")

		when:
		OpenCvUtils.getSize(nonImageFile)

		then:
		thrown(IllegalArgumentException)
	}

	@Unroll
	def "从文件读取图像：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")

		when:
		def mat = OpenCvUtils.read(file)

		then:
		mat != null
		!OpenCvUtils.isEmpty(mat)
		mat.cols() > 0
		mat.rows() > 0

		cleanup:
		mat.release()

		where:
		name << SUPPORTED_IMAGES
	}

	@Unroll
	def "从文件读取图像（指定flags）：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")

		when:
		def mat = OpenCvUtils.read(file, opencv_imgcodecs.IMREAD_GRAYSCALE)

		then:
		mat != null
		!OpenCvUtils.isEmpty(mat)
		mat.cols() > 0
		mat.rows() > 0

		cleanup:
		mat.release()

		where:
		name << SUPPORTED_IMAGES
	}

	def "从文件读取非图像文件抛异常"() {
		given:
		def nonImageFile = new File(tempDir.toFile(), "test.txt")
		nonImageFile.write("test content")

		when:
		OpenCvUtils.read(nonImageFile)

		then:
		thrown(IllegalArgumentException)
	}

	@Unroll
	def "从输入流读取图像：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def inputStream = new FileInputStream(file)

		when:
		def mat = OpenCvUtils.read(inputStream)

		then:
		mat != null
		OpenCvUtils.isNotEmpty(mat)
		mat.cols() > 0
		mat.rows() > 0

		cleanup:
		mat.release()
		inputStream.close()

		where:
		name << SUPPORTED_IMAGES
	}

	@Unroll
	def "从输入流读取图像（指定flags）：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def inputStream = new FileInputStream(file)

		when:
		def mat = OpenCvUtils.read(inputStream, opencv_imgcodecs.IMREAD_GRAYSCALE)

		then:
		mat != null
		!OpenCvUtils.isEmpty(mat)
		mat.cols() > 0
		mat.rows() > 0

		cleanup:
		mat.release()
		inputStream.close()

		where:
		name << SUPPORTED_IMAGES
	}

	def "从null输入流读取抛异常"() {
		when:
		OpenCvUtils.read((InputStream) null)

		then:
		thrown(NullPointerException)
	}

	@Unroll
	def "从字节数组读取图像：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def bytes = Files.readAllBytes(file.toPath())

		when:
		def mat = OpenCvUtils.read(bytes)

		then:
		mat != null
		!OpenCvUtils.isEmpty(mat)
		mat.cols() > 0
		mat.rows() > 0

		cleanup:
		mat.release()

		where:
		name << SUPPORTED_IMAGES
	}

	@Unroll
	def "从字节数组读取图像（指定flags）：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def bytes = Files.readAllBytes(file.toPath())

		when:
		def mat = OpenCvUtils.read(bytes, opencv_imgcodecs.IMREAD_GRAYSCALE)

		then:
		mat != null
		!OpenCvUtils.isEmpty(mat)
		mat.cols() > 0
		mat.rows() > 0

		cleanup:
		mat.release()

		where:
		name << SUPPORTED_IMAGES
	}

	def "从空字节数组读取抛异常"() {
		when:
		OpenCvUtils.read(new byte[0])

		then:
		thrown(IllegalArgumentException)
	}

	def "从null字节数组读取抛异常"() {
		when:
		OpenCvUtils.read((byte[]) null)

		then:
		thrown(IllegalArgumentException)
	}

	def "从非图像字节数组读取抛异常"() {
		given:
		def bytes = "not an image".getBytes()

		when:
		OpenCvUtils.read(bytes)

		then:
		thrown(IllegalArgumentException)
	}

	def "将AWT Color转换为BGRA Scalar"() {
		given:
		def color = new Color(255, 0, 0, 128)

		when:
		def scalar = OpenCvUtils.toBGRAColor(color)

		then:
		scalar != null
		scalar.get(0) == 0  // Blue
		scalar.get(1) == 0  // Green
		scalar.get(2) == 255 // Red
		scalar.get(3) == 128 // Alpha
	}

	def "将null Color转换为BGRA Scalar抛异常"() {
		when:
		OpenCvUtils.toBGRAColor((Color) null)

		then:
		thrown(NullPointerException)
	}

	def "将颜色字符串转换为BGRA Scalar"() {
		when:
		def scalar = OpenCvUtils.toBGRAColor("#FF0000")

		then:
		scalar != null
		scalar.get(0) == 0  // Blue
		scalar.get(1) == 0  // Green
		scalar.get(2) == 255 // Red
		scalar.get(3) == 255 // Alpha
	}

	def "将空字符串转换为BGRA Scalar抛异常"() {
		when:
		OpenCvUtils.toBGRAColor("")

		then:
		thrown(IllegalArgumentException)
	}

	def "将AWT Color转换为BGR Scalar"() {
		given:
		def color = new Color(255, 0, 0, 128)

		when:
		def scalar = OpenCvUtils.toBGRColor(color)

		then:
		scalar != null
		scalar.get(0) == 0  // Blue
		scalar.get(1) == 0  // Green
		scalar.get(2) == 255 // Red
		scalar.get(3) == 255 // Alpha (always 255)
	}

	def "将null Color转换为BGR Scalar抛异常"() {
		when:
		OpenCvUtils.toBGRColor((Color) null)

		then:
		thrown(NullPointerException)
	}

	def "将颜色字符串转换为BGR Scalar"() {
		when:
		def scalar = OpenCvUtils.toBGRColor("#FF0000")

		then:
		scalar != null
		scalar.get(0) == 0  // Blue
		scalar.get(1) == 0  // Green
		scalar.get(2) == 255 // Red
		scalar.get(3) == 255 // Alpha
	}

	def "将AWT Color转换为RGBA Scalar"() {
		given:
		def color = new Color(255, 0, 0, 128)

		when:
		def scalar = OpenCvUtils.toRGBAColor(color)

		then:
		scalar != null
		scalar.get(0) == 255 // Red
		scalar.get(1) == 0  // Green
		scalar.get(2) == 0  // Blue
		scalar.get(3) == 128 // Alpha
	}

	def "将null Color转换为RGBA Scalar抛异常"() {
		when:
		OpenCvUtils.toRGBAColor((Color) null)

		then:
		thrown(NullPointerException)
	}

	def "将颜色字符串转换为RGBA Scalar"() {
		when:
		def scalar = OpenCvUtils.toRGBAColor("#FF0000")

		then:
		scalar != null
		scalar.get(0) == 255 // Red
		scalar.get(1) == 0  // Green
		scalar.get(2) == 0  // Blue
		scalar.get(3) == 255 // Alpha
	}

	def "将AWT Color转换为RGB Scalar"() {
		given:
		def color = new Color(255, 0, 0, 128)

		when:
		def scalar = OpenCvUtils.toRGBColor(color)

		then:
		scalar != null
		scalar.get(0) == 255 // Red
		scalar.get(1) == 0  // Green
		scalar.get(2) == 0  // Blue
		scalar.get(3) == 255 // Alpha (always 255)
	}

	def "将null Color转换为RGB Scalar抛异常"() {
		when:
		OpenCvUtils.toRGBColor((Color) null)

		then:
		thrown(NullPointerException)
	}

	def "将颜色字符串转换为RGB Scalar"() {
		when:
		def scalar = OpenCvUtils.toRGBColor("#FF0000")

		then:
		scalar != null
		scalar.get(0) == 255 // Red
		scalar.get(1) == 0  // Green
		scalar.get(2) == 0  // Blue
		scalar.get(3) == 255 // Alpha
	}

	def "创建3x3卷积核"() {
		given:
		def kernelData = [1.0f, 0.0f, -1.0f, 2.0f, 0.0f, -2.0f, 1.0f, 0.0f, -1.0f] as float[]

		when:
		def kernel = OpenCvUtils.create3x3FloatKernel(kernelData)

		then:
		kernel != null
		kernel.rows() == 3
		kernel.cols() == 3

		cleanup:
		kernel.release()
	}

	def "创建空卷积核抛异常"() {
		when:
		OpenCvUtils.getKernel(new float[0])

		then:
		thrown(IllegalArgumentException)
	}

	def "创建null卷积核抛异常"() {
		when:
		OpenCvUtils.getKernel((float[]) null)

		then:
		thrown(IllegalArgumentException)
	}

	def "按宽度缩放尺寸"() {
		given:
		def size = new Size(800, 600)

		when:
		def scaledSize = OpenCvUtils.scaleByWidth(size, 400)

		then:
		scaledSize.width() == 400
		scaledSize.height() > 0
	}

	def "按宽度缩放尺寸 - 宽大于高"() {
		given:
		def size = new Size(800, 600)

		when:
		def scaledSize = OpenCvUtils.scaleByWidth(size, 400)

		then:
		scaledSize.width() == 400
		scaledSize.height() == 300
	}

	def "按宽度缩放尺寸 - 高大于宽"() {
		given:
		def size = new Size(600, 800)

		when:
		def scaledSize = OpenCvUtils.scaleByWidth(size, 300)

		then:
		scaledSize.width() == 300
		scaledSize.height() == 400
	}

	def "按宽度缩放尺寸 - 无效宽度抛异常"() {
		given:
		def size = new Size(800, 600)

		when:
		OpenCvUtils.scaleByWidth(size, 0)

		then:
		thrown(IllegalArgumentException)
	}

	def "按宽度缩放尺寸 - null尺寸抛异常"() {
		when:
		OpenCvUtils.scaleByWidth(null, 400)

		then:
		thrown(NullPointerException)
	}

	def "按高度缩放尺寸"() {
		given:
		def size = new Size(800, 600)

		when:
		def scaledSize = OpenCvUtils.scaleByHeight(size, 300)

		then:
		scaledSize.height() == 300
		scaledSize.width() > 0
	}

	def "按高度缩放尺寸 - 宽大于高"() {
		given:
		def size = new Size(800, 600)

		when:
		def scaledSize = OpenCvUtils.scaleByHeight(size, 300)

		then:
		scaledSize.height() == 300
		scaledSize.width() == 400
	}

	def "按高度缩放尺寸 - 高大于宽"() {
		given:
		def size = new Size(600, 800)

		when:
		def scaledSize = OpenCvUtils.scaleByHeight(size, 400)

		then:
		scaledSize.height() == 400
		scaledSize.width() == 300
	}

	def "按高度缩放尺寸 - 无效高度抛异常"() {
		given:
		def size = new Size(800, 600)

		when:
		OpenCvUtils.scaleByHeight(size, 0)

		then:
		thrown(IllegalArgumentException)
	}

	def "按高度缩放尺寸 - null尺寸抛异常"() {
		when:
		OpenCvUtils.scaleByHeight(null, 300)

		then:
		thrown(NullPointerException)
	}

	def "按比例缩放尺寸"() {
		given:
		def size = new Size(800, 600)

		when:
		def scaledSize = OpenCvUtils.scale(size, 0.5)

		then:
		scaledSize.width() == 400
		scaledSize.height() == 300
	}

	def "按比例缩放尺寸 - 放大"() {
		given:
		def size = new Size(800, 600)

		when:
		def scaledSize = OpenCvUtils.scale(size, 2.0)

		then:
		scaledSize.width() == 1600
		scaledSize.height() == 1200
	}

	def "按比例缩放尺寸 - 无效比例抛异常"() {
		given:
		def size = new Size(800, 600)

		when:
		OpenCvUtils.scale(size, 0)

		then:
		thrown(IllegalArgumentException)
	}

	def "按比例缩放尺寸 - null尺寸抛异常"() {
		when:
		OpenCvUtils.scale(null, 0.5)

		then:
		thrown(NullPointerException)
	}

	def "按目标尺寸缩放尺寸"() {
		given:
		def size = new Size(800, 600)

		when:
		def scaledSize = OpenCvUtils.scale(size, 400, 300)

		then:
		scaledSize.width() == 400
		scaledSize.height() == 300
	}

	def "按目标尺寸缩放尺寸 - 保持宽高比"() {
		given:
		def size = new Size(800, 600)

		when:
		def scaledSize = OpenCvUtils.scale(size, 400, 400)

		then:
		scaledSize.width() <= 400
		scaledSize.height() <= 400
	}

	def "按目标尺寸缩放尺寸 - 无效宽度抛异常"() {
		given:
		def size = new Size(800, 600)

		when:
		OpenCvUtils.scale(size, 0, 300)

		then:
		thrown(IllegalArgumentException)
	}

	def "按目标尺寸缩放尺寸 - 无效高度抛异常"() {
		given:
		def size = new Size(800, 600)

		when:
		OpenCvUtils.scale(size, 400, 0)

		then:
		thrown(IllegalArgumentException)
	}

	def "按目标尺寸缩放尺寸 - null尺寸抛异常"() {
		when:
		OpenCvUtils.scale(null, 400, 300)

		then:
		thrown(NullPointerException)
	}

	def "创建平移变换矩阵"() {
		when:
		def matrix = OpenCvUtils.create3x3FloatKernel(10, 20)

		then:
		matrix != null
		matrix.rows() == 2
		matrix.cols() == 3

		cleanup:
		matrix.release()
	}

	def "清理透明区域 - 4通道图像"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.png")
		def mat = OpenCvUtils.read(file, opencv_imgcodecs.IMREAD_UNCHANGED)

		when:
		OpenCvUtils.cleanTransparency(mat)

		then:
		mat != null
		!OpenCvUtils.isEmpty(mat)

		cleanup:
		mat.release()
	}

	def "清理透明区域 - 非4通道图像不处理"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def mat = OpenCvUtils.read(file)

		when:
		OpenCvUtils.cleanTransparency(mat)

		then:
		mat != null
		!OpenCvUtils.isEmpty(mat)

		cleanup:
		mat.release()
	}

	def "清理透明区域 - null图像抛异常"() {
		when:
		OpenCvUtils.cleanTransparency(null)

		then:
		thrown(NullPointerException)
	}

	def "清理透明区域 - 空图像抛异常"() {
		given:
		def mat = new Mat()

		when:
		OpenCvUtils.cleanTransparency(mat)

		then:
		thrown(IllegalArgumentException)

		cleanup:
		mat.release()
	}

	@Unroll
	def "校正EXIF方向 - 方向#orientation"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def mat = OpenCvUtils.read(file)

		when:
		def correctedMat = OpenCvUtils.correctOrientation(mat, orientation)

		then:
		correctedMat != null
		!OpenCvUtils.isEmpty(correctedMat)

		cleanup:
		mat.release()
		correctedMat.release()

		where:
		orientation << (1..8)
	}

	def "校正EXIF方向 - 正常方向不处理"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def mat = OpenCvUtils.read(file)

		when:
		def correctedMat = OpenCvUtils.correctOrientation(mat, 1)

		then:
		correctedMat != null
		OpenCvUtils.isNotEmpty(correctedMat)

		cleanup:
		mat.release()
		correctedMat.release()
	}

	def "校正EXIF方向 - null图像抛异常"() {
		when:
		OpenCvUtils.correctOrientation(null, 1)

		then:
		thrown(NullPointerException)
	}

	def "校正EXIF方向 - 无效方向抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def mat = OpenCvUtils.read(file)

		when:
		OpenCvUtils.correctOrientation(mat, 9)

		then:
		thrown(IllegalArgumentException)

		cleanup:
		mat.release()
	}

	def "校正EXIF方向 - 方向0抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def mat = OpenCvUtils.read(file)

		when:
		OpenCvUtils.correctOrientation(mat, 0)

		then:
		thrown(IllegalArgumentException)

		cleanup:
		mat.release()
	}
}
