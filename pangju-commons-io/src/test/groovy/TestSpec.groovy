import io.github.pangju666.commons.io.utils.compress.ZipUtils
import io.github.pangju666.commons.io.utils.file.FilenameUtils
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

	def "testZip"() {
		setup:
		ZipUtils.compressFiles(new FileOutputStream(new File("D:\\workspace\\工具.zip")), new File("D:\\workspace\\Roaming"))
	}

	def "sadsadasd"() {
		setup:
		println FilenameUtils.getFullPathNoEndSeparator("C:\\asdasdasd\\asdasdad")
	}
}