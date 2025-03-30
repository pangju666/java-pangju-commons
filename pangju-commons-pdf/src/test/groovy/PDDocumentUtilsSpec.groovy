import io.github.pangju666.commons.pdf.utils.PDDocumentUtils
import org.apache.pdfbox.Loader
import spock.lang.Specification

class PDDocumentUtilsSpec extends Specification {
	def "test"() {
		setup:
		def inputFile = new File("src/test/resources/test-new.pdf")
		def outputFile = new File("src/test/resources/test-new.pdf")
		def document = Loader.loadPDF(inputFile)
		PDDocumentUtils.addImageToPdf(document, new File("src/test/resources/large.png"))
		document.save(outputFile)
	}
}
