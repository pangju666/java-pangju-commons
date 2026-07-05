package io.github.pangju666.commons.opencv.test

import io.github.pangju666.commons.io.utils.FileUtils
import io.github.pangju666.commons.opencv.model.ImageWatermarkOption
import io.github.pangju666.commons.opencv.model.TextWatermarkOption
import io.github.pangju666.commons.opencv.utils.ImageEditor
import org.bytedeco.opencv.global.opencv_imgcodecs
import spock.lang.Specification

class OpencvUtilsSpec extends Specification {
	def test() throws IOException {
		setup:
		File inputFile = new File("E:\\Roaming\\camera.jpg")
		File outputFile = new File("E:\\Roaming\\output.png")
		File watermarkFile = new File("E:\\Roaming\\output.jpg")

		def textOption = new TextWatermarkOption()
		//textOption.opacity = 1
		def imageOption = new ImageWatermarkOption()
		//imageOption.opacity = 1

		ImageEditor.of(FileUtils.readFileToByteArray(inputFile), opencv_imgcodecs.IMREAD_ANYCOLOR)
		//.transparency(0.3)
		//.flip(FlipDirection.HORIZONTAL)
		//.rotate(27)
		//.scale(500, 500)
			.addTextWatermark("DEMO", textOption)
		//.addImageWatermark(watermarkFile, imageOption)
		//.cropByCenter(100, 100)
		//.grayscale()
			.toFile(outputFile)
	}
}
