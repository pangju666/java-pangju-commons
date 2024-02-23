import io.github.pangju666.commons.io.utils.image.ImageUtils
import spock.lang.Specification

import javax.imageio.ImageIO
import javax.imageio.stream.ImageInputStream

class TestSpec extends Specification {
	def "test"() {
		setup:
		try (FileInputStream fileInputStream = new FileInputStream("D:\\workspace\\resource\\图片\\avatar-ikun.png");
			 InputStream inputStream = ImageUtils.buildBufferInputStream(fileInputStream)) {
			ImageInputStream imageIO = ImageIO.createImageInputStream(inputStream)
			println ImageUtils.getSize(imageIO, "image/png")
		}
	}
}