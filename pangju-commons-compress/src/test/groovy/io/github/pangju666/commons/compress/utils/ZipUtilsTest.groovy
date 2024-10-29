package io.github.pangju666.commons.compress.utils

import io.github.pangju666.commons.io.utils.file.FileUtils
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipFile
import spock.lang.Specification

import java.util.zip.Deflater

class ZipUtilsTest extends Specification {
	def "UnCompress1"() {
		setup:
		ZipUtils.unCompress(new File("C:\\Users\\OriginAI-21041703\\Downloads\\Downloads.zip"))
	}

	def "UnCompress2"() {
		setup:
		ZipUtils.unCompress(new File("C:\\Users\\OriginAI-21041703\\Downloads\\Downloads.zip"),
			new File("C:\\Users\\OriginAI-21041703\\Downloads\\test1"))
	}

	def "UnCompress3"() {
		setup:
		def file = new File("C:\\Users\\OriginAI-21041703\\Downloads\\Downloads.zip")
		def zipFile = ZipFile.builder().setFile(file).get()
		ZipUtils.unCompress(zipFile, new File("C:\\Users\\OriginAI-21041703\\Downloads\\test2"))
	}

	def "UnCompress4"() {
		setup:
		def file = new File("C:\\Users\\OriginAI-21041703\\Downloads\\Downloads.zip")
		def inputStream = new FileInputStream(file)
		ZipUtils.unCompress(inputStream, new File("C:\\Users\\OriginAI-21041703\\Downloads\\test3"))
	}

	def "UnCompress5"() {
		setup:
		def file = new File("C:\\Users\\OriginAI-21041703\\Downloads\\Downloads.zip")
		def inputStream = new ZipArchiveInputStream(FileUtils.openInputStream(file))
		ZipUtils.unCompress(inputStream, new File("C:\\Users\\OriginAI-21041703\\Downloads\\test4"))
	}

	def "Compress1"() {
		setup:
		ZipUtils.compress(new File("C:\\Users\\OriginAI-21041703\\Downloads\\Downloads"))
	}

	def "Compress2"() {
		setup:
		ZipUtils.compress(new File("C:\\Users\\OriginAI-21041703\\Downloads\\test1"),
			new File("C:\\Users\\OriginAI-21041703\\Downloads\\test1.zip"))
	}

	def "Compress3"() {
		setup:
		def file = new File("C:\\Users\\OriginAI-21041703\\Downloads\\test2.zip")
		def outputStream = new FileOutputStream(file)
		ZipUtils.compress(new File("C:\\Users\\OriginAI-21041703\\Downloads\\test2"), outputStream)
	}

	def "Compress4"() {
		setup:
		def file = new File("C:\\Users\\OriginAI-21041703\\Downloads\\test3.zip")
		def outputStream = new ZipArchiveOutputStream(new FileOutputStream(file))
		ZipUtils.compress(new File("C:\\Users\\OriginAI-21041703\\Downloads\\test3"), outputStream)
	}

	def "Compress5"() {
		setup:
		ZipUtils.compress(Arrays.asList(
			new File("C:\\Users\\OriginAI-21041703\\Downloads\\library_book.sql")),
			new File("C:\\Users\\OriginAI-21041703\\Downloads\\test3.zip"))
	}

	def "Compress6"() {
		setup:
		def file = new File("C:\\Users\\OriginAI-21041703\\Downloads\\test4.zip")
		def outputStream = new FileOutputStream(file)
		ZipUtils.compress(Arrays.asList(
			new File("C:\\Users\\OriginAI-21041703\\Downloads\\library_book.sql")), outputStream)
	}

	def "Compress7"() {
		setup:
		def file = new File("C:\\Users\\OriginAI-21041703\\Downloads\\test2.zip")
		def outputStream = new ZipArchiveOutputStream(new FileOutputStream(file))
		outputStream.setLevel(Deflater.BEST_SPEED)
		ZipUtils.compress(Arrays.asList(
			new File("C:\\Users\\OriginAI-21041703\\Downloads\\library_book.sql"),
			new File("C:\\Users\\OriginAI-21041703\\Downloads\\RuoYi-master")), outputStream)
	}
}
