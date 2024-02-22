import io.github.pangju666.commons.io.utils.file.FileUtils
import spock.lang.Specification


class TestSpec extends Specification {
	def "test"() {
		setup:
		println FileUtils.getMimeType(new File("D:\\workspace\\resource\\图片\\01241f5d63368fa8012187f4f38801.jpg@1280w_1l_2o_100sh.jpg"))
	}
}