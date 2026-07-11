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

class OpencvUtilsSpec extends Specification {
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
		OpencvUtils.isEmpty(null)
	}

	def "判断 Mat 为空 - 空对象"() {
		given:
		def mat = new Mat()

		expect:
		OpencvUtils.isEmpty(mat)
	}

	def "判断 Mat 为空 - 有数据"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def mat = OpencvUtils.read(file)

		expect:
		!OpencvUtils.isEmpty(mat)

		cleanup:
		mat.release()
	}

	def "判断 Mat 不为空 - null"() {
		expect:
		!OpencvUtils.isNotEmpty(null)
	}

	def "判断 Mat 不为空 - 有数据"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def mat = OpencvUtils.read(file)

		expect:
		OpencvUtils.isNotEmpty(mat)

		cleanup:
		mat.release()
	}

	@Unroll
	def "检查是否可以读取图像文件：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")

		expect:
		OpencvUtils.canRead(file)

		where:
		name << SUPPORTED_IMAGES
	}

	def "检查是否可以读取非图像文件抛异常"() {
		given:
		def nonImageFile = new File(tempDir.toFile(), "test.txt")
		nonImageFile.write("test content")

		when:
		OpencvUtils.canRead(nonImageFile)

		then:
		thrown(IllegalArgumentException)
	}

	def "检查是否可以写入图像格式"() {
		expect:
		OpencvUtils.canWrite("jpg")
		OpencvUtils.canWrite("png")
		OpencvUtils.canWrite(".jpg")
		OpencvUtils.canWrite(".png")
	}

	def "检查是否可以写入空格式抛异常"() {
		when:
		OpencvUtils.canWrite("")

		then:
		thrown(IllegalArgumentException)
	}

	def "检查是否可以写入null格式抛异常"() {
		when:
		OpencvUtils.canWrite(null)

		then:
		thrown(NullPointerException)
	}

	@Unroll
	def "获取图像尺寸：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")

		when:
		def size = OpencvUtils.getSize(file)

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
		OpencvUtils.getSize(nonImageFile)

		then:
		thrown(IllegalArgumentException)
	}

	@Unroll
	def "从文件读取图像：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")

		when:
		def mat = OpencvUtils.read(file)

		then:
		mat != null
		!OpencvUtils.isEmpty(mat)
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
		def mat = OpencvUtils.read(file, opencv_imgcodecs.IMREAD_GRAYSCALE)

		then:
		mat != null
		!OpencvUtils.isEmpty(mat)
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
		OpencvUtils.read(nonImageFile)

		then:
		thrown(IllegalArgumentException)
	}

	@Unroll
	def "从输入流读取图像：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def inputStream = new FileInputStream(file)

		when:
		def mat = OpencvUtils.read(inputStream)

		then:
		mat != null
		OpencvUtils.isNotEmpty(mat)
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
		def mat = OpencvUtils.read(inputStream, opencv_imgcodecs.IMREAD_GRAYSCALE)

		then:
		mat != null
		!OpencvUtils.isEmpty(mat)
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
		OpencvUtils.read((InputStream) null)

		then:
		thrown(NullPointerException)
	}

	@Unroll
	def "从字节数组读取图像：#name"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def bytes = Files.readAllBytes(file.toPath())

		when:
		def mat = OpencvUtils.read(bytes)

		then:
		mat != null
		!OpencvUtils.isEmpty(mat)
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
		def mat = OpencvUtils.read(bytes, opencv_imgcodecs.IMREAD_GRAYSCALE)

		then:
		mat != null
		!OpencvUtils.isEmpty(mat)
		mat.cols() > 0
		mat.rows() > 0

		cleanup:
		mat.release()

		where:
		name << SUPPORTED_IMAGES
	}

	def "从空字节数组读取抛异常"() {
		when:
		OpencvUtils.read(new byte[0])

		then:
		thrown(IllegalArgumentException)
	}

	def "从null字节数组读取抛异常"() {
		when:
		OpencvUtils.read((byte[]) null)

		then:
		thrown(IllegalArgumentException)
	}

	def "从非图像字节数组读取抛异常"() {
		given:
		def bytes = "not an image".getBytes()

		when:
		OpencvUtils.read(bytes)

		then:
		thrown(IllegalArgumentException)
	}

	def "将AWT Color转换为BGRA Scalar"() {
		given:
		def color = new Color(255, 0, 0, 128)

		when:
		def scalar = OpencvUtils.toBGRAColor(color)

		then:
		scalar != null
		scalar.get(0) == 0  // Blue
		scalar.get(1) == 0  // Green
		scalar.get(2) == 255 // Red
		scalar.get(3) == 128 // Alpha
	}

	def "将null Color转换为BGRA Scalar抛异常"() {
		when:
		OpencvUtils.toBGRAColor((Color) null)

		then:
		thrown(NullPointerException)
	}

	def "将颜色字符串转换为BGRA Scalar"() {
		when:
		def scalar = OpencvUtils.toBGRAColor("#FF0000")

		then:
		scalar != null
		scalar.get(0) == 0  // Blue
		scalar.get(1) == 0  // Green
		scalar.get(2) == 255 // Red
		scalar.get(3) == 255 // Alpha
	}

	def "将空字符串转换为BGRA Scalar抛异常"() {
		when:
		OpencvUtils.toBGRAColor("")

		then:
		thrown(IllegalArgumentException)
	}

	def "将AWT Color转换为BGR Scalar"() {
		given:
		def color = new Color(255, 0, 0, 128)

		when:
		def scalar = OpencvUtils.toBGRColor(color)

		then:
		scalar != null
		scalar.get(0) == 0  // Blue
		scalar.get(1) == 0  // Green
		scalar.get(2) == 255 // Red
		scalar.get(3) == 255 // Alpha (always 255)
	}

	def "将null Color转换为BGR Scalar抛异常"() {
		when:
		OpencvUtils.toBGRColor((Color) null)

		then:
		thrown(NullPointerException)
	}

	def "将颜色字符串转换为BGR Scalar"() {
		when:
		def scalar = OpencvUtils.toBGRColor("#FF0000")

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
		def scalar = OpencvUtils.toRGBAColor(color)

		then:
		scalar != null
		scalar.get(0) == 255 // Red
		scalar.get(1) == 0  // Green
		scalar.get(2) == 0  // Blue
		scalar.get(3) == 128 // Alpha
	}

	def "将null Color转换为RGBA Scalar抛异常"() {
		when:
		OpencvUtils.toRGBAColor((Color) null)

		then:
		thrown(NullPointerException)
	}

	def "将颜色字符串转换为RGBA Scalar"() {
		when:
		def scalar = OpencvUtils.toRGBAColor("#FF0000")

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
		def scalar = OpencvUtils.toRGBColor(color)

		then:
		scalar != null
		scalar.get(0) == 255 // Red
		scalar.get(1) == 0  // Green
		scalar.get(2) == 0  // Blue
		scalar.get(3) == 255 // Alpha (always 255)
	}

	def "将null Color转换为RGB Scalar抛异常"() {
		when:
		OpencvUtils.toRGBColor((Color) null)

		then:
		thrown(NullPointerException)
	}

	def "将颜色字符串转换为RGB Scalar"() {
		when:
		def scalar = OpencvUtils.toRGBColor("#FF0000")

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
		def kernel = OpencvUtils.getKernel(kernelData)

		then:
		kernel != null
		kernel.rows() == 3
		kernel.cols() == 3

		cleanup:
		kernel.release()
	}

	def "创建空卷积核抛异常"() {
		when:
		OpencvUtils.getKernel(new float[0])

		then:
		thrown(IllegalArgumentException)
	}

	def "创建null卷积核抛异常"() {
		when:
		OpencvUtils.getKernel((float[]) null)

		then:
		thrown(IllegalArgumentException)
	}

	def "按宽度缩放尺寸"() {
		given:
		def size = new Size(800, 600)

		when:
		def scaledSize = OpencvUtils.scaleByWidth(size, 400)

		then:
		scaledSize.width() == 400
		scaledSize.height() > 0
	}

	def "按宽度缩放尺寸 - 宽大于高"() {
		given:
		def size = new Size(800, 600)

		when:
		def scaledSize = OpencvUtils.scaleByWidth(size, 400)

		then:
		scaledSize.width() == 400
		scaledSize.height() == 300
	}

	def "按宽度缩放尺寸 - 高大于宽"() {
		given:
		def size = new Size(600, 800)

		when:
		def scaledSize = OpencvUtils.scaleByWidth(size, 300)

		then:
		scaledSize.width() == 300
		scaledSize.height() == 400
	}

	def "按宽度缩放尺寸 - 无效宽度抛异常"() {
		given:
		def size = new Size(800, 600)

		when:
		OpencvUtils.scaleByWidth(size, 0)

		then:
		thrown(IllegalArgumentException)
	}

	def "按宽度缩放尺寸 - null尺寸抛异常"() {
		when:
		OpencvUtils.scaleByWidth(null, 400)

		then:
		thrown(NullPointerException)
	}

	def "按高度缩放尺寸"() {
		given:
		def size = new Size(800, 600)

		when:
		def scaledSize = OpencvUtils.scaleByHeight(size, 300)

		then:
		scaledSize.height() == 300
		scaledSize.width() > 0
	}

	def "按高度缩放尺寸 - 宽大于高"() {
		given:
		def size = new Size(800, 600)

		when:
		def scaledSize = OpencvUtils.scaleByHeight(size, 300)

		then:
		scaledSize.height() == 300
		scaledSize.width() == 400
	}

	def "按高度缩放尺寸 - 高大于宽"() {
		given:
		def size = new Size(600, 800)

		when:
		def scaledSize = OpencvUtils.scaleByHeight(size, 400)

		then:
		scaledSize.height() == 400
		scaledSize.width() == 300
	}

	def "按高度缩放尺寸 - 无效高度抛异常"() {
		given:
		def size = new Size(800, 600)

		when:
		OpencvUtils.scaleByHeight(size, 0)

		then:
		thrown(IllegalArgumentException)
	}

	def "按高度缩放尺寸 - null尺寸抛异常"() {
		when:
		OpencvUtils.scaleByHeight(null, 300)

		then:
		thrown(NullPointerException)
	}

	def "按比例缩放尺寸"() {
		given:
		def size = new Size(800, 600)

		when:
		def scaledSize = OpencvUtils.scale(size, 0.5)

		then:
		scaledSize.width() == 400
		scaledSize.height() == 300
	}

	def "按比例缩放尺寸 - 放大"() {
		given:
		def size = new Size(800, 600)

		when:
		def scaledSize = OpencvUtils.scale(size, 2.0)

		then:
		scaledSize.width() == 1600
		scaledSize.height() == 1200
	}

	def "按比例缩放尺寸 - 无效比例抛异常"() {
		given:
		def size = new Size(800, 600)

		when:
		OpencvUtils.scale(size, 0)

		then:
		thrown(IllegalArgumentException)
	}

	def "按比例缩放尺寸 - null尺寸抛异常"() {
		when:
		OpencvUtils.scale(null, 0.5)

		then:
		thrown(NullPointerException)
	}

	def "按目标尺寸缩放尺寸"() {
		given:
		def size = new Size(800, 600)

		when:
		def scaledSize = OpencvUtils.scale(size, 400, 300)

		then:
		scaledSize.width() == 400
		scaledSize.height() == 300
	}

	def "按目标尺寸缩放尺寸 - 保持宽高比"() {
		given:
		def size = new Size(800, 600)

		when:
		def scaledSize = OpencvUtils.scale(size, 400, 400)

		then:
		scaledSize.width() <= 400
		scaledSize.height() <= 400
	}

	def "按目标尺寸缩放尺寸 - 无效宽度抛异常"() {
		given:
		def size = new Size(800, 600)

		when:
		OpencvUtils.scale(size, 0, 300)

		then:
		thrown(IllegalArgumentException)
	}

	def "按目标尺寸缩放尺寸 - 无效高度抛异常"() {
		given:
		def size = new Size(800, 600)

		when:
		OpencvUtils.scale(size, 400, 0)

		then:
		thrown(IllegalArgumentException)
	}

	def "按目标尺寸缩放尺寸 - null尺寸抛异常"() {
		when:
		OpencvUtils.scale(null, 400, 300)

		then:
		thrown(NullPointerException)
	}

	def "创建平移变换矩阵"() {
		when:
		def matrix = OpencvUtils.getMatrixMat(10, 20)

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
		def mat = OpencvUtils.read(file, opencv_imgcodecs.IMREAD_UNCHANGED)

		when:
		OpencvUtils.cleanTransparency(mat)

		then:
		mat != null
		!OpencvUtils.isEmpty(mat)

		cleanup:
		mat.release()
	}

	def "清理透明区域 - 非4通道图像不处理"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def mat = OpencvUtils.read(file)

		when:
		OpencvUtils.cleanTransparency(mat)

		then:
		mat != null
		!OpencvUtils.isEmpty(mat)

		cleanup:
		mat.release()
	}

	def "清理透明区域 - null图像抛异常"() {
		when:
		OpencvUtils.cleanTransparency(null)

		then:
		thrown(NullPointerException)
	}

	def "清理透明区域 - 空图像抛异常"() {
		given:
		def mat = new Mat()

		when:
		OpencvUtils.cleanTransparency(mat)

		then:
		thrown(IllegalArgumentException)

		cleanup:
		mat.release()
	}

	@Unroll
	def "校正EXIF方向 - 方向#orientation"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def mat = OpencvUtils.read(file)

		when:
		def correctedMat = OpencvUtils.correctOrientation(mat, orientation)

		then:
		correctedMat != null
		!OpencvUtils.isEmpty(correctedMat)

		cleanup:
		mat.release()
		correctedMat.release()

		where:
		orientation << (1..8)
	}

	def "校正EXIF方向 - 正常方向不处理"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def mat = OpencvUtils.read(file)

		when:
		def correctedMat = OpencvUtils.correctOrientation(mat, 1)

		then:
		correctedMat != null
		OpencvUtils.isNotEmpty(correctedMat)

		cleanup:
		mat.release()
		correctedMat.release()
	}

	def "校正EXIF方向 - null图像抛异常"() {
		when:
		OpencvUtils.correctOrientation(null, 1)

		then:
		thrown(NullPointerException)
	}

	def "校正EXIF方向 - 无效方向抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def mat = OpencvUtils.read(file)

		when:
		OpencvUtils.correctOrientation(mat, 9)

		then:
		thrown(IllegalArgumentException)

		cleanup:
		mat.release()
	}

	def "校正EXIF方向 - 方向0抛异常"() {
		given:
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def mat = OpencvUtils.read(file)

		when:
		OpencvUtils.correctOrientation(mat, 0)

		then:
		thrown(IllegalArgumentException)

		cleanup:
		mat.release()
	}
}
