import io.github.pangju666.commons.io.utils.image.ImageUtils
import spock.lang.Specification

class TestSpec extends Specification {
	def "test"() {
		setup:
		println ImageUtils.scaleSizeByHeight(1920, 1080, 800)
	}
}