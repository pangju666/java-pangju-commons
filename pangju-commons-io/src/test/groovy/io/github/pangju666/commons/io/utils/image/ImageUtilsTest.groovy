package io.github.pangju666.commons.io.utils.image


import com.drew.imaging.ImageMetadataReader
import io.github.pangju666.commons.io.utils.file.FileUtils
import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream
import spock.lang.Specification
import spock.lang.Unroll

import javax.imageio.ImageIO

class ImageUtilsTest extends Specification {
	def "ComputeNewSizeByWidth"() {
		given:
		def targetWidth = 600

		when:
		def newSize = ImageUtils.computeNewSizeByWidth(width, height, targetWidth)

		then:
		newSize.width() == expectWidth && newSize.height() == expectHeight

		where:
		width | height | expectWidth | expectHeight
		1920  | 1080   | 600         | 337
		1080  | 1920   | 600         | 1066
	}

	def "ComputeNewSizeByHeight"() {
		given:
		def targetWidth = 600

		when:
		def newSize = ImageUtils.computeNewSizeByHeight(width, height, targetWidth)

		then:
		newSize.width() == expectWidth && newSize.height() == expectHeight

		where:
		width | height | expectWidth | expectHeight
		1920  | 1080   | 1066        | 600
		1080  | 1920   | 337         | 600
	}

	def "ComputeNewSize"() {
		given:
		def targetWidth = 400
		def targetHeight = 300

		when:
		def newSize = ImageUtils.computeNewSize(width, height, targetWidth, targetHeight)

		then:
		newSize.width() == expectWidth && newSize.height() == expectHeight

		where:
		width | height | expectWidth | expectHeight
		1920  | 1080   | 400         | 225
		1080  | 1920   | 168         | 300
	}

	@Unroll
	def "IsImage"() {
		given:
		def file = new File("C:\\Users\\OriginAI-21041703\\Pictures\\019a81568e1b3132f8754c80184506.jpg")
		def bytes = FileUtils.readFileToByteArray(file)
		def inputStream = UnsynchronizedByteArrayInputStream.builder()
			.setByteArray(bytes)
			.get()
		def metaData = ImageMetadataReader.readMetadata(file)

		def isImage1 = ImageUtils.isImage(file)
		def isImage2 = ImageUtils.isImage(bytes)
		def isImage3 = ImageUtils.isImage(inputStream)
		def isImage7 = ImageUtils.isImage(metaData)

		def file2 = new File("C:\\Users\\OriginAI-21041703\\Downloads\\library_directory_tree.sql")
		def bytes2 = FileUtils.readFileToByteArray(file2)
		def inputStream2 = UnsynchronizedByteArrayInputStream.builder()
			.setByteArray(bytes2)
			.get()

		def isImage4 = ImageUtils.isImage(file2)
		def isImage5 = ImageUtils.isImage(bytes2)
		def isImage6 = ImageUtils.isImage(inputStream2)

		expect:
		isImage1
		isImage2
		isImage3
		isImage7
		!isImage4
		!isImage5
		!isImage6
	}

	@Unroll
	def "GetMimeType"() {
		def file = new File("D:\\workspace\\resource\\图片\\wac_nearside.tif")
		def bytes = FileUtils.openBufferedFileChannelInputStream(file).readAllBytes()
		def inputStream = UnsynchronizedByteArrayInputStream.builder()
			.setByteArray(bytes)
			.get()
		def metaData = ImageMetadataReader.readMetadata(file)

		def mimeType1 = ImageUtils.getImageType(file)
		def mimeType2 = ImageUtils.getImageType(bytes)
		def mimeType3 = ImageUtils.getImageType(inputStream)
		def mimeType4 = ImageUtils.getImageType(metaData)

		expect:
		mimeType1 == "image/tiff"
		mimeType2 == "image/tiff"
		mimeType3 == "image/tiff"
		mimeType4 == null
	}

	@Unroll
	def "GetSize"() {
		def file = new File("D:\\workspace\\resource\\图片\\50.jpg")
		def bytes = FileUtils.openBufferedFileChannelInputStream(file).readAllBytes()
		def inputStream = UnsynchronizedByteArrayInputStream.builder()
			.setByteArray(bytes)
			.get()
		def metaData = ImageMetadataReader.readMetadata(file)
		def imageInputStream = ImageIO.createImageInputStream(file)

		def imageSize1 = ImageUtils.getImageSize(file)
		def imageSize2 = ImageUtils.getImageSize(bytes)
		def imageSize3 = ImageUtils.getImageSize(inputStream)
		def imageSize4 = ImageUtils.getImageSize(metaData)
		def imageSize5 = ImageUtils.getImageSize(imageInputStream)

		expect:
		imageSize1.width() == 3072 && imageSize1.height() == 4096
		imageSize2.width() == 3072 && imageSize2.height() == 4096
		imageSize3.width() == 3072 && imageSize3.height() == 4096
		imageSize4.width() == 3072 && imageSize4.height() == 4096
		imageSize5.width() == 4096 && imageSize5.height() == 3072
	}
}
