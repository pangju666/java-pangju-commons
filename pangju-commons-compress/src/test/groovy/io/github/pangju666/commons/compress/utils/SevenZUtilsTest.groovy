package io.github.pangju666.commons.compress.utils

import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import spock.lang.Specification

class SevenZUtilsTest extends Specification {
	def "UnCompress1"() {
		setup:
		SevenZUtils.unCompress(new File("C:\\Users\\OriginAI-21041703\\Downloads\\Downloads.7z"))
	}

	def "UnCompress2"() {
		setup:
		SevenZUtils.unCompress(new File("C:\\Users\\OriginAI-21041703\\Downloads\\Downloads.7z"),
			new File("C:\\Users\\OriginAI-21041703\\Downloads\\test"))
	}

	def "UnCompress3"() {
		setup:
		def file = new File("C:\\Users\\OriginAI-21041703\\Downloads\\Downloads.7z")
		def sevenZFile = SevenZFile.builder().setFile(file).get()
		SevenZUtils.unCompress(sevenZFile, new File("C:\\Users\\OriginAI-21041703\\Downloads\\test2"))
	}

	def "Compress1"() {
		setup:
		SevenZUtils.compress(new File("C:\\Users\\OriginAI-21041703\\Downloads\\Downloads"))
	}

	def "Compress2"() {
		setup:
		SevenZUtils.compress(new File("C:\\Users\\OriginAI-21041703\\Downloads\\test"),
			new File("C:\\Users\\OriginAI-21041703\\Downloads\\test.7z"))
	}

	def "Compress3"() {
		setup:
		def file = new File("C:\\Users\\OriginAI-21041703\\Downloads\\test2.7z")
		def sevenZFile = new SevenZOutputFile(file)
		SevenZUtils.compress(new File("C:\\Users\\OriginAI-21041703\\Downloads\\test2"), sevenZFile)
	}

	def "Compress4"() {
		setup:
		SevenZUtils.compress(Arrays.asList(
			new File("C:\\Users\\OriginAI-21041703\\Downloads\\library_book.sql")),
			new File("C:\\Users\\OriginAI-21041703\\Downloads\\test3.7z"))
	}

	def "Compress5"() {
		setup:
		def file = new File("C:\\Users\\OriginAI-21041703\\Downloads\\test4.7z")
		def sevenZFile = new SevenZOutputFile(file)
		SevenZUtils.compress(Arrays.asList(
			new File("C:\\Users\\OriginAI-21041703\\Downloads\\library_book.sql")), sevenZFile)
	}
}
