package io.github.pangju666.commons.opencv.test

import io.github.pangju666.commons.opencv.enums.FlipDirection
import io.github.pangju666.commons.opencv.enums.RotateDirection
import io.github.pangju666.commons.opencv.utils.ImageEditor
import org.bytedeco.opencv.global.opencv_imgcodecs
import spock.lang.Specification

class ImageUtilsSpec extends Specification {
	def test() throws IOException {
		setup:
		File inputFile = new File("E:\\Roaming\\camera.jpg")
		File outputFile = new File("E:\\Roaming\\output.png")

		ImageEditor.of(inputFile, opencv_imgcodecs.IMREAD_ANYCOLOR)
			//.transparency(0.3)
			//.flip(FlipDirection.HORIZONTAL)
			//.rotate(27)
			//.scale(500, 500)
			//.cropByCenter(100, 100)
			.grayscale()
			.toFile(outputFile)
	}
}
