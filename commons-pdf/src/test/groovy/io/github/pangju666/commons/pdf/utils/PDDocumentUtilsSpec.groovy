package io.github.pangju666.commons.pdf.utils


import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.pdmodel.PDDocument
import spock.lang.Specification
import spock.lang.Unroll

import javax.imageio.ImageIO
import java.awt.*
import java.awt.image.BufferedImage

class PDDocumentUtilsSpec extends Specification {
	File pdfFile = new File("e:\\project\\pangju\\pangju-commons\\commons-pdf\\src\\test\\resources\\test.pdf")

	def "isPDF(File) 与 isPDF(byte[]) 检测"() {
		given:
		byte[] pdfBytes = pdfFile.bytes
		File txt = File.createTempFile("not-pdf", ".txt")
		txt.text = "abc"

		expect:
		PDDocumentUtils.isPDF(pdfFile)
		PDDocumentUtils.isPDF(pdfBytes)
		!PDDocumentUtils.isPDF(txt)
		!PDDocumentUtils.isPDF("xyz".bytes)
		!PDDocumentUtils.isPDF(new byte[0])

		cleanup:
		txt?.delete()
	}

	@Unroll
	def "computeMemoryUsageSetting 返回预期策略: size=#size"() {
		expect:
		def mus = PDDocumentUtils.computeMemoryUsageSetting(size)
		mus.is(PDDocumentUtils.MAIN_MEMORY_ONLY_MEMORY_USAGE_SETTING) == (size < PDDocumentUtils.MIN_PDF_BYTES)
		mus.is(PDDocumentUtils.MIXED_PAGE_MEMORY_USAGE_SETTING) == (size >= PDDocumentUtils.MIN_PDF_BYTES && size <= PDDocumentUtils.MAX_PDF_BYTES)
		mus.is(PDDocumentUtils.TEMP_FILE_ONLY_MEMORY_USAGE_SETTING) == (size > PDDocumentUtils.MAX_PDF_BYTES)

		where:
		size << [
			10 * 1024 * 1024,
			50 * 1024 * 1024,
			200 * 1024 * 1024,
			600 * 1024 * 1024
		]
	}

	def "getDocument 加载 PDF 与非法文件抛异常"() {
		given:
		File txt = File.createTempFile("not-pdf", ".txt")
		txt.text = "abc"

		when:
		PDDocument doc = PDDocumentUtils.getDocument(pdfFile)

		then:
		doc != null
		doc.numberOfPages > 0

		when:
		PDDocumentUtils.getDocument(txt)

		then:
		thrown(IllegalArgumentException)

		cleanup:
		doc?.close()
		txt?.delete()
	}

	def "getDocument(含密码) 加载未加密 PDF"() {
		when:
		PDDocument doc = PDDocumentUtils.getDocument(pdfFile, "password")

		then:
		doc != null
		doc.numberOfPages > 0

		cleanup:
		doc?.close()
	}

	def "渲染所有页面为图像(默认与指定缩放)"() {
		when:
		PDDocument doc = PDDocumentUtils.getDocument(pdfFile)
		def images1 = PDDocumentUtils.getPagesAsImage(doc)
		def images2 = PDDocumentUtils.getPagesAsImage(doc, 2)

		then:
		images1.size() == doc.numberOfPages
		images2.size() == doc.numberOfPages

		cleanup:
		doc?.close()
	}

	def "渲染指定页面集合为图像(过滤无效页码)"() {
		when:
		PDDocument doc = PDDocumentUtils.getDocument(pdfFile)
		int total = doc.numberOfPages
		def images = PDDocumentUtils.getPagesAsImage(doc, [1, 2, null, total + 5] as Set)

		then:
		images.size() == Math.min(2, total)

		cleanup:
		doc?.close()
	}

	def "渲染页面范围为图像(缩放与DPI)"() {
		when:
		PDDocument doc = PDDocumentUtils.getDocument(pdfFile)
		int total = doc.numberOfPages
		def rangeImages1 = PDDocumentUtils.getPagesAsImage(doc, 2, 1, Math.min(3, total))
		def rangeImages2 = PDDocumentUtils.getPagesAsImageWithDPI(doc, 72f, 1, Math.min(3, total))

		then:
		rangeImages1.size() == rangeImages2.size()
		rangeImages1.size() <= Math.min(3, total)

		cleanup:
		doc?.close()
	}

	def "copy/merge/split 操作"() {
		when:
		PDDocument doc = PDDocumentUtils.getDocument(pdfFile)
		PDDocument copyAll = PDDocumentUtils.copy(doc)
		PDDocument merged = PDDocumentUtils.merge([doc, copyAll], MemoryUsageSetting.setupMainMemoryOnly())
		def splitDocs = PDDocumentUtils.split(doc, 1)

		then:
		copyAll.numberOfPages == doc.numberOfPages
		merged.numberOfPages == doc.numberOfPages + copyAll.numberOfPages
		splitDocs.size() == doc.numberOfPages

		cleanup:
		merged?.close()
		copyAll?.close()
		splitDocs?.each { it?.close() }
		doc?.close()
	}

	def "addImage 添加字节图像到文档不抛异常"() {
		given:
		BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB)
		def g = bi.graphics
		g.color = Color.RED
		g.fillRect(0, 0, 16, 16)
		g.dispose()
		ByteArrayOutputStream out = new ByteArrayOutputStream()
		ImageIO.write(bi, "png", out)
		byte[] pngBytes = out.toByteArray()

		when:
		PDDocument doc = PDDocumentUtils.getDocument(pdfFile)
		PDDocumentUtils.addImage(doc, pngBytes, 1, 1, 16, 16)

		then:
		doc.numberOfPages >= 1

		cleanup:
		doc?.close()
		out?.close()
	}

	def "addImage(byte[]) 默认坐标抛异常"() {
		given:
		BufferedImage bi = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB)
		ByteArrayOutputStream out = new ByteArrayOutputStream()
		ImageIO.write(bi, "png", out)
		byte[] pngBytes = out.toByteArray()

		when:
		PDDocument doc = PDDocumentUtils.getDocument(pdfFile)
		PDDocumentUtils.addImage(doc, pngBytes)

		then:
		thrown(IllegalArgumentException)

		cleanup:
		doc?.close()
		out?.close()
	}

	def "addImage(File) 坐标添加与默认抛异常"() {
		given:
		BufferedImage bi = new BufferedImage(12, 12, BufferedImage.TYPE_INT_RGB)
		File imgFile = File.createTempFile("img", ".png")
		ImageIO.write(bi, "png", imgFile)

		when:
		PDDocument doc1 = PDDocumentUtils.getDocument(pdfFile)
		PDDocumentUtils.addImage(doc1, imgFile, 1, 1, 12, 12)

		then:
		doc1.numberOfPages >= 1

		when:
		PDDocument doc2 = PDDocumentUtils.getDocument(pdfFile)
		PDDocumentUtils.addImage(doc2, imgFile)

		then:
		thrown(IllegalArgumentException)

		cleanup:
		doc1?.close()
		doc2?.close()
		imgFile?.delete()
	}

	def "getPagesAsImageWithDPI(集合) 返回尺寸集合"() {
		when:
		PDDocument doc = PDDocumentUtils.getDocument(pdfFile)
		int total = doc.numberOfPages
		def images = PDDocumentUtils.getPagesAsImageWithDPI(doc, 72f, [1, 2, total + 10])

		then:
		images.size() == Math.min(2, total)

		cleanup:
		doc?.close()
	}

	def "createDocument 保留版本与文档信息"() {
		when:
		PDDocument src = PDDocumentUtils.getDocument(pdfFile)
		PDDocument dst = PDDocumentUtils.createDocument(src)

		then:
		dst != null
		dst.version == src.version
		dst.documentInformation?.title == src.documentInformation?.title

		cleanup:
		src?.close()
		dst?.close()
	}

	def "getBookmarks 返回列表"() {
		when:
		PDDocument doc = PDDocumentUtils.getDocument(pdfFile)
		def bookmarks = PDDocumentUtils.getBookmarks(doc)

		then:
		bookmarks != null

		cleanup:
		doc?.close()
	}

	def "copy 指定范围与集合"() {
		when:
		PDDocument doc = PDDocumentUtils.getDocument(pdfFile)
		int total = doc.numberOfPages
		PDDocument c1 = PDDocumentUtils.copy(doc, 1, Math.min(2, total))
		PDDocument c2 = PDDocumentUtils.copy(doc, [1, 1, total + 5, 2])

		then:
		c1.numberOfPages == Math.min(2, total)
		c2.numberOfPages == Math.min(2, total)

		cleanup:
		c1?.close()
		c2?.close()
		doc?.close()
	}

	def "getDocument(byte[]) 加载 PDF 与非法内容抛异常"() {
		given:
		byte[] pdfBytes = pdfFile.bytes
		byte[] invalidBytes = "invalid".bytes

		when:
		PDDocument doc = PDDocumentUtils.getDocument(pdfBytes)

		then:
		doc != null
		doc.numberOfPages > 0

		when:
		PDDocumentUtils.getDocument(invalidBytes)

		then:
		thrown(IllegalArgumentException)

		cleanup:
		doc?.close()
	}

	def "getDocument(byte[], String) 加载 PDF"() {
		given:
		byte[] pdfBytes = pdfFile.bytes

		when:
		PDDocument doc = PDDocumentUtils.getDocument(pdfBytes, "password")

		then:
		doc != null
		doc.numberOfPages > 0

		cleanup:
		doc?.close()
	}

	def "getDocument(InputStream) 加载 PDF"() {
		given:
		InputStream inputStream = new ByteArrayInputStream(pdfFile.bytes)
		InputStream invalidStream = new ByteArrayInputStream("invalid".bytes)

		when:
		PDDocument doc = PDDocumentUtils.getDocument(inputStream)

		then:
		doc != null
		doc.numberOfPages > 0

		when:
		PDDocumentUtils.getDocument(invalidStream)

		then:
		thrown(IllegalArgumentException)

		cleanup:
		doc?.close()
		inputStream?.close()
		invalidStream?.close()
	}

	def "getDocument(InputStream, String) 加载 PDF"() {
		given:
		InputStream inputStream = new ByteArrayInputStream(pdfFile.bytes)

		when:
		PDDocument doc = PDDocumentUtils.getDocument(inputStream, "password")

		then:
		doc != null
		doc.numberOfPages > 0

		cleanup:
		doc?.close()
		inputStream?.close()
	}

	def "getPageAsImage 单页渲染"() {
		when:
		PDDocument doc = PDDocumentUtils.getDocument(pdfFile)
		def image1 = PDDocumentUtils.getPageAsImage(doc, 1)
		def image2 = PDDocumentUtils.getPageAsImage(doc, 1, 2)
		def image3 = PDDocumentUtils.getPageAsImageWithDPI(doc, 1, 72f)

		then:
		image1 != null
		image2 != null
		image3 != null
		image1.width > 0
		image2.width > image1.width

		cleanup:
		doc?.close()
	}

	def "参数校验异常"() {
		when:
		PDDocument doc = PDDocumentUtils.getDocument(pdfFile)
		PDDocumentUtils.getPagesAsImage(doc, 0, 1, 1)

		then:
		thrown(IllegalArgumentException)

		when:
		PDDocumentUtils.getPagesAsImageWithDPI(doc, 0f, [1])

		then:
		thrown(IllegalArgumentException)

		when:
		PDDocumentUtils.getPagesAsImage(doc, 1, 2, 1)

		then:
		thrown(IllegalArgumentException)

		when:
		PDDocumentUtils.getPageAsImage(doc, 0)

		then:
		thrown(IllegalArgumentException)

		when:
		PDDocumentUtils.getPageAsImage(doc, 1, 0)

		then:
		thrown(IllegalArgumentException)

		when:
		PDDocumentUtils.getPageAsImageWithDPI(doc, 1, 0f)

		then:
		thrown(IllegalArgumentException)

		cleanup:
		doc?.close()
	}
}
