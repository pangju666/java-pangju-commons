package io.github.pangju666.commons.io.utils


import spock.lang.Specification

class FileUtilsSpec extends Specification {
	def "encryptFile"() {
		setup:
		FileUtils.encryptFileByCtr(new File("C:\\Users\\OriginAI-21041703\\Downloads\\stopwords.txt"),
			new File("C:\\Users\\OriginAI-21041703\\Downloads\\stopwords-encrypted.txt"),
			"1111111111111111")
	}

	def "decryptFile"() {
		setup:
		FileUtils.decryptFileByCtr(new File("C:\\Users\\OriginAI-21041703\\Downloads\\stopwords-encrypted.txt"),
			new File("C:\\Users\\OriginAI-21041703\\Downloads\\stopwords-decrypted.txt"),
			"1111111111111111")
	}
}
