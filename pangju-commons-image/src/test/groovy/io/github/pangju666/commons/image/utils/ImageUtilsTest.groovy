package io.github.pangju666.commons.image.utils


import spock.lang.Specification

import javax.imageio.ImageIO

class ImageUtilsTest extends Specification {
	def "test"() {
		setup:
		for (final def mimeType in Set.of(ImageIO.getReaderMIMETypes())) {
			println mimeType
		}
	}

	def "test2"() {
		setup:
		def file = new File("E:\\旅游照\\广州\\IMG_20180116_101900.jpg")
		println ImageUtils.getImageSize(ImageIO.createImageInputStream(file))
	}
}
