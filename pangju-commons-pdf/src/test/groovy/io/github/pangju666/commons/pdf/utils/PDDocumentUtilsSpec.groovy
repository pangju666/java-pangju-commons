package io.github.pangju666.commons.pdf.utils

import io.github.pangju666.commons.io.lang.IOConstants
import io.github.pangju666.commons.io.utils.FileUtils
import io.github.pangju666.commons.pdf.lang.PdfConstants
import org.apache.pdfbox.Loader
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.pdmodel.PDDocument
import spock.lang.Specification
import spock.lang.TempDir

import java.awt.image.BufferedImage
import java.nio.file.Path

class PDDocumentUtilsSpec extends Specification {
	@TempDir
	Path tempDir

	def "test computeMemoryUsageSetting with different file sizes"() {
		when:
		def result = PDDocumentUtils.computeMemoryUsageSetting(fileSize)

		then:
		result == expectedSetting

		where:
		fileSize          | expectedSetting
		49 * 1024 * 1024  | PDDocumentUtils.MAIN_MEMORY_ONLY_MEMORY_USAGE_SETTING
		100 * 1024 * 1024 | PDDocumentUtils.MIXED_PAGE_MEMORY_USAGE_SETTING
		501 * 1024 * 1024 | PDDocumentUtils.TEMP_FILE_ONLY_MEMORY_USAGE_SETTING
	}

	def "test isPDF with valid PDF file"() {
		given:
		def pdfFile = new File("src/test/resources/test.pdf")

		expect:
		PDDocumentUtils.isPDF(pdfFile)
	}

	def "test isPDF with valid PDF bytes"() {
		given:
		def pdfFile = new File("src/test/resources/test.pdf")

		expect:
		PDDocumentUtils.isPDF(FileUtils.readFileToByteArray(pdfFile))
	}

	def "test isPDF with valid PDF inputStream"() {
		given:
		def pdfFile = new File("src/test/resources/test.pdf")

		expect:
		PDDocumentUtils.isPDF(new FileInputStream(pdfFile))
	}

	def "test createDocument should copy metadata"() {
		given:
		def srcDoc = Loader.loadPDF(new File("src/test/resources/test.pdf"))
		srcDoc.documentInformation.author = "Test Author"

		when:
		def newDoc = PDDocumentUtils.createDocument(srcDoc)

		then:
		newDoc.documentInformation.author == "Test Author"
		newDoc.version == srcDoc.version

		cleanup:
		srcDoc.close()
		newDoc.close()
	}

	def "test getDocument with File"() {
		given:
		File file = new File("src/test/resources/test.pdf")
		file.exists() >> true
		file.isFile() >> true
		IOConstants.getDefaultTika().detect(file) >> PdfConstants.PDF_MIME_TYPE

		when:
		PDDocument document = PDDocumentUtils.getDocument(file)

		then:
		document != null
	}

	def "test getDocument with File and password"() {
		given:
		File file = new File("src/test/resources/test.pdf")
		file.exists() >> true
		file.isFile() >> true
		IOConstants.getDefaultTika().detect(file) >> PdfConstants.PDF_MIME_TYPE

		when:
		PDDocument document = PDDocumentUtils.getDocument(file, "123456")

		then:
		document != null
	}

	def "test addImage with byte[]"() {
		given:
		PDDocument document = Loader.loadPDF(new File("src/test/resources/test.pdf"))
		byte[] bytes = FileUtils.readFileToByteArray(new File("src/test/resources/large.png"))

		when:
		PDDocumentUtils.addImage(document, bytes, 1)

		then:
		noExceptionThrown()
	}

	def "test addImage with file"() {
		given:
		PDDocument document = Loader.loadPDF(new File("src/test/resources/test.pdf"))
		def file = new File("src/test/resources/split-1.pdf")

		when:
		PDDocumentUtils.addImage(document, file, 1)

		then:
		thrown(IllegalArgumentException)
	}

	def "test getPageImages with default scale"() {
		given:
		PDDocument document = Loader.loadPDF(new File("src/test/resources/test.pdf"))

		when:
		def images = PDDocumentUtils.getPageImages(document)

		then:
		images.every({ it instanceof BufferedImage })
		images.size() == document.getNumberOfPages()
	}

	def "test merge with File and MemoryUsageSetting"() {
		given:
		Collection<PDDocument> documents = Arrays.asList(
			Loader.loadPDF(new File("src/test/resources/test.pdf")),
			Loader.loadPDF(new File("src/test/resources/test.pdf")),
			Loader.loadPDF(new File("src/test/resources/test.pdf"))
		)
		File outputFile = new File("src/test/resources/merge.pdf")

		when:
		def outputDocument = PDDocumentUtils.merge(documents, MemoryUsageSetting.setupMainMemoryOnly())
		outputDocument.save(outputFile)

		then:
		noExceptionThrown()
	}

	def "test split with custom splitPage"() {
		given:
		PDDocument document = Loader.loadPDF(new File("src/test/resources/merge.pdf"))

		when:
		def splits = PDDocumentUtils.split(document, 2)
		for (i in 0..<splits.size()) {
			splits[i].save(new File("src/test/resources/split-${i + 1}.pdf"))
		}

		then:
		noExceptionThrown()
	}

	def "test copy all pages"() {
		given:
		PDDocument document = Loader.loadPDF(new File("src/test/resources/merge.pdf"))

		when:
		def copy = PDDocumentUtils.copy(document, 2, 3)

		then:
		copy.getNumberOfPages() == 2
	}

	def "test copy pages"() {
		given:
		PDDocument document = Loader.loadPDF(new File("src/test/resources/merge.pdf"))

		when:
		def copy = PDDocumentUtils.copy(document, Arrays.asList(1, 3))

		then:
		copy.getNumberOfPages() == 2
	}
}
