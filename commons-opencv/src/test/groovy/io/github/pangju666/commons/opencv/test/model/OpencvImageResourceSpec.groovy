package io.github.pangju666.commons.opencv.test.model

import com.drew.metadata.Metadata
import io.github.pangju666.commons.io.model.IOResource
import io.github.pangju666.commons.opencv.model.OpencvImageResource
import io.github.pangju666.commons.opencv.utils.OpencvUtils
import org.bytedeco.opencv.global.opencv_imgcodecs
import org.bytedeco.opencv.opencv_core.Size
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.Path

class OpencvImageResourceSpec extends Specification {
	@TempDir
	Path tempDir

	static final String TEST_IMAGES_DIR = "src/test/resources/images"
	static final List<String> SUPPORTED_IMAGES = [
		"test.jpg",
		"test.png",
		"test.bmp",
		"test.tiff",
	]

	@Unroll
	def "基于文件路径构造（自动解析 EXIF）：#name"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/${name}")

		when:
		"构造 OpencvImageResource"
		def resource = new OpencvImageResource(file.absolutePath)

		then:
		"验证构造成功"
		resource != null
		resource.getFlags() == opencv_imgcodecs.IMREAD_UNCHANGED
		resource.getExifOrientation() in (1..8)

		where:
		name << SUPPORTED_IMAGES
	}

	@Unroll
	def "基于文件路径构造（指定 EXIF 方向）：#name"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/${name}")

		when:
		"构造 OpencvImageResource 并指定 EXIF 方向"
		def resource = new OpencvImageResource(file.absolutePath, opencv_imgcodecs.IMREAD_UNCHANGED, 6)

		then:
		"验证构造成功"
		resource != null
		resource.getExifOrientation() == 6

		where:
		name << SUPPORTED_IMAGES
	}

	@Unroll
	def "基于文件构造（自动解析 EXIF）：#name"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/${name}")

		when:
		"构造 OpencvImageResource"
		def resource = new OpencvImageResource(file)

		then:
		"验证构造成功"
		resource != null
		resource.getFlags() == opencv_imgcodecs.IMREAD_UNCHANGED
		resource.getExifOrientation() in (1..8)

		where:
		name << SUPPORTED_IMAGES
	}

	@Unroll
	def "基于文件构造（指定 EXIF 方向）：#name"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/${name}")

		when:
		"构造 OpencvImageResource 并指定 EXIF 方向"
		def resource = new OpencvImageResource(file, opencv_imgcodecs.IMREAD_UNCHANGED, 6)

		then:
		"验证构造成功"
		resource != null
		resource.getExifOrientation() == 6

		where:
		name << SUPPORTED_IMAGES
	}

	@Unroll
	def "基于字节数组构造（自动解析 EXIF）：#name"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def bytes = Files.readAllBytes(file.toPath())

		when:
		"构造 OpencvImageResource"
		def resource = new OpencvImageResource(bytes)

		then:
		"验证构造成功"
		resource != null
		resource.getFlags() == opencv_imgcodecs.IMREAD_UNCHANGED
		resource.getExifOrientation() in (1..8)

		where:
		name << SUPPORTED_IMAGES
	}

	@Unroll
	def "基于字节数组构造（指定 EXIF 方向）：#name"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def bytes = Files.readAllBytes(file.toPath())

		when:
		"构造 OpencvImageResource 并指定 EXIF 方向"
		def resource = new OpencvImageResource(bytes, opencv_imgcodecs.IMREAD_UNCHANGED, 6)

		then:
		"验证构造成功"
		resource != null
		resource.getExifOrientation() == 6

		where:
		name << SUPPORTED_IMAGES
	}

	@Unroll
	def "基于输入流构造（自动解析 EXIF）：#name"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def inputStream = new FileInputStream(file)

		when:
		"构造 OpencvImageResource"
		def resource = new OpencvImageResource(inputStream)

		then:
		"验证构造成功"
		resource != null
		resource.getFlags() == opencv_imgcodecs.IMREAD_UNCHANGED
		resource.getExifOrientation() in (1..8)

		where:
		name << SUPPORTED_IMAGES
	}

	@Unroll
	def "基于输入流构造（指定 EXIF 方向）：#name"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def inputStream = new FileInputStream(file)

		when:
		"构造 OpencvImageResource 并指定 EXIF 方向"
		def resource = new OpencvImageResource(inputStream, opencv_imgcodecs.IMREAD_UNCHANGED, 6)

		then:
		"验证构造成功"
		resource != null
		resource.getExifOrientation() == 6

		where:
		name << SUPPORTED_IMAGES
	}

	@Unroll
	def "基于 IOResource 构造（自动解析 EXIF）：#name"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ioResource = new IOResource(file)

		when:
		"构造 OpencvImageResource"
		def resource = new OpencvImageResource(ioResource)

		then:
		"验证构造成功"
		resource != null
		resource.getFlags() == opencv_imgcodecs.IMREAD_UNCHANGED
		resource.getExifOrientation() in (1..8)

		where:
		name << SUPPORTED_IMAGES
	}

	@Unroll
	def "基于 IOResource 构造（指定 EXIF 方向）：#name"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ioResource = new IOResource(file)

		when:
		"构造 OpencvImageResource 并指定 EXIF 方向"
		def resource = new OpencvImageResource(ioResource, opencv_imgcodecs.IMREAD_UNCHANGED, 6)

		then:
		"验证构造成功"
		resource != null
		resource.getExifOrientation() == 6

		where:
		name << SUPPORTED_IMAGES
	}

	@Unroll
	def "基于 OpencvImageResource 构造（保留 flags）：#name"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def originalResource = new OpencvImageResource(file, opencv_imgcodecs.IMREAD_COLOR, 5)

		when:
		"基于 OpencvImageResource 构造"
		def resource = new OpencvImageResource(originalResource)

		then:
		"验证 flags 和 exifOrientation 被保留"
		resource.getFlags() == opencv_imgcodecs.IMREAD_COLOR

		where:
		name << SUPPORTED_IMAGES
	}

	def "EXIF 方向超出范围抛异常"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")

		when:
		"构造 OpencvImageResource 并指定超出范围的 EXIF 方向"
		new OpencvImageResource(file, opencv_imgcodecs.IMREAD_UNCHANGED, 9)

		then:
		"抛出 IllegalArgumentException"
		thrown(IllegalArgumentException)
	}

	@Unroll
	def "获取图像尺寸：#name"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def resource = new OpencvImageResource(file)

		when:
		"获取图像尺寸"
		def size = resource.getImageSize()

		then:
		"验证尺寸获取成功"
		size != null
		size.width() > 0
		size.height() > 0

		where:
		name << SUPPORTED_IMAGES
	}

	@Unroll
	def "获取 Mat 图像：#name"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def resource = new OpencvImageResource(file)

		when:
		"获取 Mat 图像"
		def mat = resource.getImageMat()

		then:
		"验证 Mat 获取成功"
		mat != null
		!OpencvUtils.isEmpty(mat)
		mat.cols() > 0
		mat.rows() > 0

		where:
		name << SUPPORTED_IMAGES
	}

	@Unroll
	def "获取 Mat 深拷贝：#name"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def resource = new OpencvImageResource(file)

		when:
		"获取 Mat 深拷贝"
		def originalMat = resource.getImageMat()
		def copyMat = resource.getImageMatCopy()

		then:
		"验证深拷贝成功"
		copyMat != null
		!OpencvUtils.isEmpty(copyMat)
		copyMat.cols() == originalMat.cols()
		copyMat.rows() == originalMat.rows()
		copyMat != originalMat

		where:
		name << SUPPORTED_IMAGES
	}

	@Unroll
	def "获取图像元数据：#name"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def resource = new OpencvImageResource(file)

		when:
		"获取图像元数据"
		def metadata = resource.getMetadata()

		then:
		"验证元数据获取成功"
		metadata != null

		where:
		name << SUPPORTED_IMAGES
	}

	def "设置图像尺寸"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new OpencvImageResource(file)
		def newSize = new Size(100, 100)

		when:
		"设置图像尺寸"
		resource.setImageSize(newSize)

		then:
		"验证尺寸设置成功"
		resource.getImageSize().width() == 100
		resource.getImageSize().height() == 100
	}

	def "设置图像元数据"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new OpencvImageResource(file)
		def newMetadata = new Metadata()

		when:
		"设置图像元数据"
		resource.setMetadata(newMetadata)

		then:
		"验证元数据设置成功"
		resource.getMetadata() == newMetadata
	}

	def "获取 flags"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new OpencvImageResource(file, opencv_imgcodecs.IMREAD_COLOR, true)

		when:
		"获取 flags"
		def flags = resource.getFlags()

		then:
		"验证 flags 获取成功"
		flags == opencv_imgcodecs.IMREAD_COLOR
	}

	def "获取 EXIF 方向"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new OpencvImageResource(file, opencv_imgcodecs.IMREAD_UNCHANGED, 6)

		when:
		"获取 EXIF 方向"
		def orientation = resource.getExifOrientation()

		then:
		"验证 EXIF 方向获取成功"
		orientation == 6
	}

	def "关闭资源"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new OpencvImageResource(file)
		resource.getImageMat()

		when:
		"关闭资源"
		resource.close()
		resource.getImageMat()

		then:
		"验证资源已关闭"
		thrown(IOException)
	}

	def "关闭后访问方法抛异常"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new OpencvImageResource(file)
		resource.close()

		when:
		"访问已关闭资源的方法"
		resource.getImageSize()

		then:
		"抛出 IOException"
		thrown(IOException)
	}

	def "设置已关闭资源的尺寸抛异常"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new OpencvImageResource(file)
		resource.close()

		when:
		"设置已关闭资源的尺寸"
		resource.setImageSize(new Size(100, 100))

		then:
		"抛出 IOException"
		thrown(IOException)
	}

	def "构造非图像文件抛异常"() {
		given:
		"准备非图像文件"
		def nonImageFile = new File(tempDir.toFile(), "test.txt")
		nonImageFile.write("test content")

		when:
		"构造 OpencvImageResource"
		new OpencvImageResource(nonImageFile)

		then:
		"抛出 IllegalArgumentException"
		thrown(IllegalArgumentException)
	}

	def "构造空字节数组抛异常"() {
		when:
		"构造 OpencvImageResource"
		new OpencvImageResource(new byte[0])

		then:
		"抛出 IllegalArgumentException"
		thrown(IllegalArgumentException)
	}

	def "构造 null 文件抛异常"() {
		when:
		"构造 OpencvImageResource"
		new OpencvImageResource((File) null)

		then:
		"抛出 NullPointerException"
		thrown(NullPointerException)
	}

	def "构造 null 字节数组抛异常"() {
		when:
		"构造 OpencvImageResource"
		new OpencvImageResource((byte[]) null)

		then:
		"抛出 NullPointerException"
		thrown(IllegalArgumentException)
	}

	def "构造 null 输入流抛异常"() {
		when:
		"构造 OpencvImageResource"
		new OpencvImageResource((InputStream) null)

		then:
		"抛出 NullPointerException"
		thrown(NullPointerException)
	}

	def "构造 null IOResource 抛异常"() {
		when:
		"构造 OpencvImageResource"
		new OpencvImageResource((IOResource) null)

		then:
		"抛出 NullPointerException"
		thrown(NullPointerException)
	}

	@Unroll
	def "指定读取标志构造：#name"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/${name}")

		when:
		"构造 OpencvImageResource 并指定读取标志"
		def resource = new OpencvImageResource(file, opencv_imgcodecs.IMREAD_GRAYSCALE, true)

		then:
		"验证读取标志设置成功"
		resource.getFlags() == opencv_imgcodecs.IMREAD_GRAYSCALE

		where:
		name << SUPPORTED_IMAGES
	}

	@Unroll
	def "可选择是否解析 EXIF：#name - #parseExif"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/${name}")

		when:
		"构造 OpencvImageResource"
		def resource = new OpencvImageResource(file, parseExif)

		then:
		"验证构造成功"
		resource != null
		resource.getExifOrientation() in (1..8)

		where:
		name       | parseExif
		"test.jpg" | true
		"test.jpg" | false
		"test.png" | true
		"test.png" | false
	}

	@Unroll
	def "转换为BytePointer：#name"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def resource = new OpencvImageResource(file)

		when:
		"转换为 BytePointer"
		def bytePointer = resource.toBytePointer()

		then:
		"验证转换成功"
		bytePointer != null
		!bytePointer.isNull()

		cleanup:
		bytePointer.close()

		where:
		name << SUPPORTED_IMAGES
	}

	def "转换为BytePointer - 已关闭资源抛异常"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new OpencvImageResource(file)
		resource.close()

		when:
		"转换为 BytePointer"
		resource.toBytePointer()

		then:
		"抛出 IOException"
		thrown(IOException)
	}

	def "判断EXIF方向是否已校正 - 已校正"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new OpencvImageResource(file, opencv_imgcodecs.IMREAD_UNCHANGED, true)

		when:
		"判断是否已校正"
		def isCorrected = resource.isOrientationCorrected()

		then:
		"验证已校正"
		isCorrected
	}

	def "判断EXIF方向是否已校正 - 未校正"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")
		def resource = new OpencvImageResource(file, opencv_imgcodecs.IMREAD_UNCHANGED, false)

		when:
		"判断是否已校正"
		def isCorrected = resource.isOrientationCorrected()

		then:
		"验证未校正"
		!isCorrected
	}

	@Unroll
	def "基于flags构造自动判断方向校正：#name - #flags"() {
		given:
		"准备测试文件"
		def file = new File("${TEST_IMAGES_DIR}/${name}")

		when:
		"构造 OpencvImageResource 并指定 flags"
		def resource = new OpencvImageResource(file, flags, false)

		then:
		"验证方向校正状态"
		resource.isOrientationCorrected() == (flags != opencv_imgcodecs.IMREAD_UNCHANGED &&
			flags != opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION)

		where:
		name       | flags
		"test.jpg" | opencv_imgcodecs.IMREAD_UNCHANGED
		"test.jpg" | opencv_imgcodecs.IMREAD_IGNORE_ORIENTATION
		"test.jpg" | opencv_imgcodecs.IMREAD_COLOR_BGR
		"test.jpg" | opencv_imgcodecs.IMREAD_GRAYSCALE
	}
}
