package io.github.pangju666.commons.opencv.test;

import io.github.pangju666.commons.opencv.utils.ImageUtils
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Size
import spock.lang.Specification

import java.nio.ByteBuffer;

class ImageUtilsSpec extends Specification {
	def test() throws IOException {
		setup:
		File file = new File("E:\\Roaming\\camera.jpg")

		/*file.withInputStream {it -> {
			Mat mat = ImageUtils.read(it.readAllBytes())
			Size size = mat.size()
			println size.width()
			println size.height()
		}}*/

		Mat mat = ImageUtils.read(file)
		Size size = mat.size()
		println size.width()
		println size.height()
	}
}
