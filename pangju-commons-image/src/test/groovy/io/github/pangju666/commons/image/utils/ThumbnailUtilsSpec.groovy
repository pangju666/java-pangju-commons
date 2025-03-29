package io.github.pangju666.commons.image.utils


import io.github.pangju666.commons.image.model.ImageSize
import spock.lang.Specification

import javax.imageio.ImageIO

class ThumbnailUtilsSpec extends Specification {

	def "asThumbnail"() {
		setup:
		def inputFile = new File("E:\\project\\java\\commons\\pangju-commons\\pangju-commons-image\\src\\test\\resources\\test.png")
		def outputFile = new File("E:\\project\\java\\commons\\pangju-commons\\pangju-commons-image\\src\\test\\resources\\thumbnail.svg")
		ThumbnailUtils.scale(ImageIO.read(inputFile), new ImageSize(100, 100), outputFile)
	}
}