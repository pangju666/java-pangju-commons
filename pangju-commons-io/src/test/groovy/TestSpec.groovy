import io.github.pangju666.commons.io.utils.image.ImageUtils
import spock.lang.Specification

class TestSpec extends Specification {
	def "test"() {
		setup:
		println ImageUtils.getSize(new File("D:\\workspace\\resource\\图片\\avatar-ikun.png"))
	}
}