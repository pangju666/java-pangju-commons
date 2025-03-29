package io.github.pangju666.commons.image.utils


import spock.lang.Specification

import javax.imageio.ImageIO
import javax.imageio.stream.ImageInputStream

class ThumbnailUtilsSpec extends Specification {

	def "asThumbnail"() {
		setup:
		def inputFile = new File("E:\\project\\java\\commons\\pangju-commons\\pangju-commons-image\\src\\test\\resources\\test.svg")
		def outputFile = new File("E:\\project\\java\\commons\\pangju-commons\\pangju-commons-image\\src\\test\\resources\\thumbnail.jpg")
		def inputStream = new FileInputStream(inputFile)
		ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)
		println ImageIO.read(imageInputStream)


		//println ImageUtils.getMimeType(new FileInputStream(inputFile))

		//println ImageUtils.getMimeType(new FileInputStream(new File("E:\\数据文件\\测试用文件\\wac_nearside.tif")))
		/*InputStream imageInputStream = new FileInputStream(new File("E:\\数据文件\\测试用文件\\wac_nearside.tif"))
		println ImageUtils.getMimeType(imageInputStream);*/


		/*def inputImageStream = ImageIO.createImageInputStream(inputFile)
		def readers = ImageIO.getImageReadersByFormatName("svg")
		if (readers.hasNext()) {
			def reader = readers.next()
			reader.setInput(inputImageStream)
			def inputImage = reader.read(0)
			def image = ThumbnailUtils.asThumbnail(inputImage, new ImageSize(100, 100), ResampleOp.FILTER_TRIANGLE)
			println ImageIO.write(image, "png", outputFile)
		}*/
	}
}
