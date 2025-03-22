import com.hankcs.hanlp.HanLP
import spock.lang.Specification

class TestSpec extends Specification {

	def "test"() {
		setup:
		println HanLP.convertToPinyinString("你好", " ", true);
	}
}
