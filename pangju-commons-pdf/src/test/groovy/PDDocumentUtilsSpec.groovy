import io.github.pangju666.commons.io.utils.FileUtils
import io.github.pangju666.commons.pdf.utils.PDDocumentUtils
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Unroll

import java.nio.file.Files
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
		def srcDoc = Loader.loadPDF(new File("src/test/resources/password.pdf"))
		srcDoc.documentInformation.author = "Test Author"

		when:
		def newDoc = PDDocumentUtils.createDocument(srcDoc)

		then:
		newDoc.documentInformation.author == "Test Author"
		newDoc.version == srcDoc.version
		newDoc.isEncrypted()

		cleanup:
		srcDoc.close()
		newDoc.close()
	}

	def "test addImageToDocument with byte array"() {
		given:
		def doc = new PDDocument()
		def imageBytes = Files.readAllBytes(Path.of("src/test/resources/test.png"))

		when:
		PDDocumentUtils.addImageToDocument(doc, imageBytes)

		then:
		doc.numberOfPages == 1

		cleanup:
		doc.close()
	}

	def "test merge documents should combine pages"() {
		given:
		def doc1 = new PDDocument()
		doc1.addPage(new PDPage())
		def doc2 = new PDDocument()
		doc2.addPage(new PDPage())

		def outputFile = tempDir.resolve("merged.pdf").toFile()

		when:
		PDDocumentUtils.merge([doc1, doc2], outputFile)

		then:
		outputFile.exists()
		Loader.loadPDF(outputFile).withCloseable { mergedDoc ->
			mergedDoc.numberOfPages == 2
		}

		cleanup:
		doc1.close()
		doc2.close()
	}

	def "test split document with 2 pages split by 1"() {
		given:
		def doc = new PDDocument()
		doc.addPage(new PDPage())
		doc.addPage(new PDPage())

		when:
		def splitDocs = PDDocumentUtils.split(doc, 1)

		then:
		splitDocs.size() == 2
		splitDocs.eachWithIndex { splitDoc, i ->
			assert splitDoc.numberOfPages == 1
			splitDoc.close()
		}

		cleanup:
		doc.close()
	}

	def "test getDictionaries with outline items"() {
		given:
		def doc = new PDDocument()
		def outline = new PDDocumentOutline()
		def item1 = new PDOutlineItem()
		item1.title = "Chapter 1"
		outline.addLast(item1)

		doc.documentCatalog.documentOutline = outline

		when:
		def directories = PDDocumentUtils.getDictionaries(doc)

		then:
		directories.size() == 1
		directories[0].title == "Chapter 1"

		cleanup:
		doc.close()
	}

	def "test getDocument with invalid file should throw exception"() {
		when:
		PDDocumentUtils.getDocument(new File("invalid_path.pdf"))

		then:
		thrown(FileNotFoundException)
	}

	@Unroll
	def "test copy pages with #desc"() {
		given:
		def doc = new PDDocument()
		3.times { doc.addPage(new PDPage()) }

		when:
		def copiedDoc = PDDocumentUtils.copy(doc, pages)

		then:
		copiedDoc.numberOfPages == expectedPages

		cleanup:
		doc.close()
		copiedDoc.close()

		where:
		desc                 | pages  | expectedPages
		"page range"         | 1..3   | 3
		"specific pages"     | [1, 3] | 2
		"out of range pages" | [1, 5] | 1
	}
}
