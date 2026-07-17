package io.github.pangju666.commons.pdf.utils

import io.github.pangju666.commons.pdf.io.resource.PdfImageResource
import io.github.pangju666.commons.pdf.io.resource.PdfResource
import io.github.pangju666.commons.pdf.model.PdfRenderOptions
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import spock.lang.Specification
import spock.lang.Unroll

import javax.imageio.ImageIO
import java.awt.*
import java.awt.image.BufferedImage
import java.util.List

class PDDocumentUtilsSpec extends Specification {
	File pdfFile = new File("e:\\project\\pangju\\pangju-commons\\commons-pdf\\src\\test\\resources\\test.pdf")

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

	def "createDocument 保留版本与文档信息"() {
		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument src = pdfResource.getDocument()
		PDDocument dst = PDDocumentUtils.createDocument(src)

		then:
		dst != null
		dst.version == src.version
		dst.documentInformation?.title == src.documentInformation?.title

		cleanup:
		pdfResource?.close()
		dst?.close()
	}

	def "copy 全部页面"() {
		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		PDDocument copyAll = PDDocumentUtils.copy(doc)

		then:
		copyAll.numberOfPages == doc.numberOfPages

		cleanup:
		copyAll?.close()
		pdfResource?.close()
	}

	def "copy 指定结束页"() {
		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		int total = doc.numberOfPages
		PDDocument copy = PDDocumentUtils.copy(doc, Math.min(2, total))

		then:
		copy.numberOfPages == Math.min(2, total)

		cleanup:
		copy?.close()
		pdfResource?.close()
	}

	def "copy 指定页面范围"() {
		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		int total = doc.numberOfPages
		PDDocument copy = PDDocumentUtils.copy(doc, 1, Math.min(2, total))

		then:
		copy.numberOfPages == Math.min(2, total)

		cleanup:
		copy?.close()
		pdfResource?.close()
	}

	def "copy 指定页码集合"() {
		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		int total = doc.numberOfPages
		PDDocument copy = PDDocumentUtils.copy(doc, [1, 1, total + 5, 2])

		then:
		copy.numberOfPages == Math.min(2, total)

		cleanup:
		copy?.close()
		pdfResource?.close()
	}

	def "copy 空集合返回仅包含元数据的文档"() {
		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		PDDocument copy = PDDocumentUtils.copy(doc, null)

		then:
		copy != null
		copy.numberOfPages == 0
		copy.version == doc.version

		cleanup:
		copy?.close()
		pdfResource?.close()
	}

	def "merge 合并文档"() {
		when:
		PdfResource pdfResource1 = new PdfResource(pdfFile)
		PdfResource pdfResource2 = new PdfResource(pdfFile)
		PDDocument doc = pdfResource1.getDocument()
		PDDocument copyAll = PDDocumentUtils.copy(doc)
		PDDocument merged = PDDocumentUtils.merge([doc, copyAll], MemoryUsageSetting.setupMainMemoryOnly())

		then:
		merged.numberOfPages == doc.numberOfPages + copyAll.numberOfPages

		cleanup:
		merged?.close()
		copyAll?.close()
		pdfResource1?.close()
		pdfResource2?.close()
	}

	def "merge 过滤 null 文档"() {
		when:
		PdfResource pdfResource1 = new PdfResource(pdfFile)
		PdfResource pdfResource2 = new PdfResource(pdfFile)
		PDDocument doc = pdfResource1.getDocument()
		PDDocument copyAll = PDDocumentUtils.copy(doc)
		PDDocument merged = PDDocumentUtils.merge([doc, null, copyAll], MemoryUsageSetting.setupMainMemoryOnly())

		then:
		merged.numberOfPages == doc.numberOfPages + copyAll.numberOfPages

		cleanup:
		merged?.close()
		copyAll?.close()
		pdfResource1?.close()
		pdfResource2?.close()
	}

	def "split 拆分文档"() {
		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		def splitDocs = PDDocumentUtils.split(doc, 1)

		then:
		splitDocs.size() == doc.numberOfPages

		cleanup:
		splitDocs?.each { it?.close() }
		pdfResource?.close()
	}

	def "insertPage 插入页面到指定位置"() {
		when:
		PdfResource pdfResource1 = new PdfResource(pdfFile)
		PdfResource pdfResource2 = new PdfResource(pdfFile)
		PDDocument doc = pdfResource1.getDocument()
		int originalPages = doc.numberOfPages
		PDDocument src = pdfResource2.getDocument()
		PDPage page = src.getPage(0)

		PDDocumentUtils.insertPage(doc, page, 1)

		then:
		doc.numberOfPages == originalPages + 1

		cleanup:
		doc?.close()
		src?.close()
		pdfResource1?.close()
		pdfResource2?.close()
	}

	def "insertPage 插入位置超出页数添加到末尾"() {
		when:
		PdfResource pdfResource1 = new PdfResource(pdfFile)
		PdfResource pdfResource2 = new PdfResource(pdfFile)
		PDDocument doc = pdfResource1.getDocument()
		int originalPages = doc.numberOfPages
		PDDocument src = pdfResource2.getDocument()
		PDPage page = src.getPage(0)

		PDDocumentUtils.insertPage(doc, page, originalPages + 10)

		then:
		doc.numberOfPages == originalPages + 1

		cleanup:
		doc?.close()
		src?.close()
		pdfResource1?.close()
		pdfResource2?.close()
	}

	def "addImage 使用 PdfImageResource 添加图像"() {
		given:
		BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB)
		def g = bi.graphics
		g.color = Color.RED
		g.fillRect(0, 0, 16, 16)
		g.dispose()
		ByteArrayOutputStream out = new ByteArrayOutputStream()
		ImageIO.write(bi, "png", out)
		byte[] pngBytes = out.toByteArray()
		PdfImageResource imageResource = new PdfImageResource(pngBytes)

		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		int originalPages = doc.numberOfPages
		PDDocumentUtils.addImage(doc, imageResource)

		then:
		doc.numberOfPages == originalPages + 1

		cleanup:
		doc?.close()
		imageResource?.close()
		pdfResource?.close()
		out?.close()
	}

	def "addImage 使用 PdfImageResource 指定位置和尺寸"() {
		given:
		BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB)
		def g = bi.graphics
		g.color = Color.RED
		g.fillRect(0, 0, 16, 16)
		g.dispose()
		ByteArrayOutputStream out = new ByteArrayOutputStream()
		ImageIO.write(bi, "png", out)
		byte[] pngBytes = out.toByteArray()
		PdfImageResource imageResource = new PdfImageResource(pngBytes)

		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		int originalPages = doc.numberOfPages
		PDDocumentUtils.addImage(doc, imageResource, 0, 0, 16, 16)

		then:
		doc.numberOfPages == originalPages + 1

		cleanup:
		doc?.close()
		imageResource?.close()
		pdfResource?.close()
		out?.close()
	}

	def "insertImage 使用 PdfImageResource 插入图像"() {
		given:
		BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB)
		def g = bi.graphics
		g.color = Color.RED
		g.fillRect(0, 0, 16, 16)
		g.dispose()
		ByteArrayOutputStream out = new ByteArrayOutputStream()
		ImageIO.write(bi, "png", out)
		byte[] pngBytes = out.toByteArray()
		PdfImageResource imageResource = new PdfImageResource(pngBytes)

		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		int originalPages = doc.numberOfPages
		PDDocumentUtils.insertImage(doc, imageResource, 1)

		then:
		doc.numberOfPages == originalPages + 1

		cleanup:
		doc?.close()
		imageResource?.close()
		pdfResource?.close()
		out?.close()
	}

	def "insertImage 使用 PdfImageResource 指定位置和尺寸"() {
		given:
		BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB)
		def g = bi.graphics
		g.color = Color.RED
		g.fillRect(0, 0, 16, 16)
		g.dispose()
		ByteArrayOutputStream out = new ByteArrayOutputStream()
		ImageIO.write(bi, "png", out)
		byte[] pngBytes = out.toByteArray()
		PdfImageResource imageResource = new PdfImageResource(pngBytes)

		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		int originalPages = doc.numberOfPages
		PDDocumentUtils.insertImage(doc, imageResource, 1, 0, 0, 16, 16)

		then:
		doc.numberOfPages == originalPages + 1

		cleanup:
		doc?.close()
		imageResource?.close()
		pdfResource?.close()
		out?.close()
	}

	def "renderPagesAsImage 渲染所有页面(默认选项)"() {
		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		def images = PDDocumentUtils.renderPagesAsImage(doc)

		then:
		images.size() == doc.numberOfPages

		cleanup:
		pdfResource?.close()
	}

	def "renderPagesAsImage 渲染所有页面(自定义选项)"() {
		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		PdfRenderOptions options = new PdfRenderOptions()
		options.scale = 2.0f
		def images = PDDocumentUtils.renderPagesAsImage(doc, options)

		then:
		images.size() == doc.numberOfPages

		cleanup:
		pdfResource?.close()
	}

	def "renderPagesAsImage 渲染指定页面集合"() {
		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		int total = doc.numberOfPages
		def images = PDDocumentUtils.renderPagesAsImage(doc, [1, 2, null, total + 5] as Set)

		then:
		images.size() == Math.min(2, total)

		cleanup:
		pdfResource?.close()
	}

	def "renderPagesAsImage 渲染指定页面集合(自定义选项)"() {
		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		int total = doc.numberOfPages
		PdfRenderOptions options = new PdfRenderOptions()
		def images = PDDocumentUtils.renderPagesAsImage(doc, [1, 2] as Set, options)

		then:
		images.size() == Math.min(2, total)

		cleanup:
		pdfResource?.close()
	}

	def "renderPagesAsImage 渲染页面范围(默认选项)"() {
		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		int total = doc.numberOfPages
		def images = PDDocumentUtils.renderPagesAsImage(doc, Math.min(3, total))

		then:
		images.size() == Math.min(3, total)

		cleanup:
		pdfResource?.close()
	}

	def "renderPagesAsImage 渲染页面范围(自定义选项)"() {
		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		int total = doc.numberOfPages
		PdfRenderOptions options = new PdfRenderOptions()
		def images = PDDocumentUtils.renderPagesAsImage(doc, Math.min(3, total), options)

		then:
		images.size() == Math.min(3, total)

		cleanup:
		pdfResource?.close()
	}

	def "renderPagesAsImage 渲染指定起始和结束页"() {
		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		int total = doc.numberOfPages
		def images = PDDocumentUtils.renderPagesAsImage(doc, 1, Math.min(2, total))

		then:
		images.size() == Math.min(2, total)

		cleanup:
		pdfResource?.close()
	}

	def "renderPagesAsImage 渲染指定起始和结束页(自定义选项)"() {
		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		int total = doc.numberOfPages
		PdfRenderOptions options = new PdfRenderOptions()
		def images = PDDocumentUtils.renderPagesAsImage(doc, 1, Math.min(2, total), options)

		then:
		images.size() == Math.min(2, total)

		cleanup:
		pdfResource?.close()
	}

	def "renderPagesAsImage 使用消费者处理(默认选项)"() {
		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		List<Integer> processedPages = []
		PDDocumentUtils.renderPagesAsImage(doc) { image, page ->
			processedPages.add(page)
		}

		then:
		processedPages.size() == doc.numberOfPages

		cleanup:
		pdfResource?.close()
	}

	def "renderPagesAsImage 使用消费者处理(自定义选项)"() {
		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		List<Integer> processedPages = []
		PdfRenderOptions options = new PdfRenderOptions()
		PDDocumentUtils.renderPagesAsImage(doc, options) { image, page ->
			processedPages.add(page)
		}

		then:
		processedPages.size() == doc.numberOfPages

		cleanup:
		pdfResource?.close()
	}

	def "renderPagesAsImage 使用消费者处理指定页面集合"() {
		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		List<Integer> processedPages = []
		PDDocumentUtils.renderPagesAsImage(doc, [1, 2] as Set) { image, page ->
			processedPages.add(page)
		}

		then:
		processedPages.size() == 2

		cleanup:
		pdfResource?.close()
	}

	def "renderPageAsImage 渲染单个页面(默认选项)"() {
		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		def image = PDDocumentUtils.renderPageAsImage(doc, 1)

		then:
		image != null
		image.width > 0
		image.height > 0

		cleanup:
		pdfResource?.close()
	}

	def "renderPageAsImage 渲染单个页面(自定义选项)"() {
		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		PdfRenderOptions options = new PdfRenderOptions()
		options.scale = 2.0f
		def image = PDDocumentUtils.renderPageAsImage(doc, 1, options)

		then:
		image != null
		image.width > 0
		image.height > 0

		cleanup:
		pdfResource?.close()
	}

	def "getBookmarks 返回列表"() {
		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		def bookmarks = PDDocumentUtils.getBookmarks(doc)

		then:
		bookmarks != null

		cleanup:
		pdfResource?.close()
	}

	def "参数校验异常 - copy"() {
		when:
		PDDocumentUtils.copy(null)

		then:
		thrown(NullPointerException)

		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		PDDocumentUtils.copy(doc, 0)

		then:
		thrown(IllegalArgumentException)

		when:
		PDDocumentUtils.copy(doc, 1, 0)

		then:
		thrown(IllegalArgumentException)

		when:
		PDDocumentUtils.copy(doc, 2, 1)

		then:
		thrown(IllegalArgumentException)

		cleanup:
		pdfResource?.close()
	}

	def "参数校验异常 - merge"() {
		when:
		PDDocumentUtils.merge(null, MemoryUsageSetting.setupMainMemoryOnly())

		then:
		thrown(NullPointerException)

		when:
		PDDocumentUtils.merge([], MemoryUsageSetting.setupMainMemoryOnly())

		then:
		thrown(IllegalArgumentException)

		when:
		PDDocumentUtils.merge([null], null)

		then:
		thrown(NullPointerException)
	}

	def "参数校验异常 - split"() {
		when:
		PDDocumentUtils.split(null, 1)

		then:
		thrown(NullPointerException)

		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		PDDocumentUtils.split(doc, 0)

		then:
		thrown(IllegalArgumentException)

		cleanup:
		pdfResource?.close()
	}

	def "参数校验异常 - insertPage"() {
		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		PDDocumentUtils.insertPage(doc, null, 1)

		then:
		thrown(NullPointerException)

		when:
		PDDocumentUtils.insertPage(null, new PDPage(), 1)

		then:
		thrown(NullPointerException)

		when:
		PDDocumentUtils.insertPage(doc, new PDPage(), 0)

		then:
		thrown(IllegalArgumentException)

		cleanup:
		pdfResource?.close()
	}

	def "参数校验异常 - addImage(PdfImageResource)"() {
		when:
		PDDocumentUtils.addImage(null, new PdfImageResource(new byte[0]))

		then:
		thrown(IllegalArgumentException)

		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		PDDocumentUtils.addImage(doc, null as PdfImageResource)

		then:
		thrown(NullPointerException)

		cleanup:
		pdfResource?.close()
	}

	def "参数校验异常 - insertImage(PdfImageResource)"() {
		when:
		PDDocumentUtils.insertImage(null, new PdfImageResource(new byte[0]), 1)

		then:
		thrown(IllegalArgumentException)

		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		PDDocumentUtils.insertImage(doc, null, 1)

		then:
		thrown(NullPointerException)

		when:
		PDDocumentUtils.insertImage(doc, new PdfImageResource(new byte[0]), 0)

		then:
		thrown(IllegalArgumentException)

		cleanup:
		pdfResource?.close()
	}

	def "参数校验异常 - renderPagesAsImage"() {
		when:
		PDDocumentUtils.renderPagesAsImage(null)

		then:
		thrown(NullPointerException)

		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		PDDocumentUtils.renderPagesAsImage(doc, null, null)

		then:
		thrown(NullPointerException)

		when:
		PDDocumentUtils.renderPagesAsImage(doc, 0)

		then:
		thrown(IllegalArgumentException)

		when:
		PDDocumentUtils.renderPagesAsImage(doc, 1, 0)

		then:
		thrown(IllegalArgumentException)

		when:
		PDDocumentUtils.renderPagesAsImage(doc, 2, 1)

		then:
		thrown(IllegalArgumentException)

		cleanup:
		pdfResource?.close()
	}

	def "参数校验异常 - renderPageAsImage"() {
		when:
		PDDocumentUtils.renderPageAsImage(null, 1)

		then:
		thrown(NullPointerException)

		when:
		PdfResource pdfResource = new PdfResource(pdfFile)
		PDDocument doc = pdfResource.getDocument()
		PDDocumentUtils.renderPageAsImage(doc, 0)

		then:
		thrown(IllegalArgumentException)

		when:
		PDDocumentUtils.renderPageAsImage(doc, 0, null)

		then:
		thrown(NullPointerException)

		cleanup:
		pdfResource?.close()
	}
}
