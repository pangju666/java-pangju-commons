import io.github.pangju666.commons.poi.utils.XWPFDocumentUtils
import spock.lang.Specification

class XWPFDocumentUtilsSpec extends Specification {

	def "Feature method"() {
		setup:
		XWPFDocumentUtils.merge(Arrays.asList(
			new File("src/test/resources/test1.docx"),
			new File("src/test/resources/test2.docx")
		), new File("src/test/resources/output.docx"))
	}
}
