package io.github.pangju666.commons.codec.encryption.password

import io.github.pangju666.commons.crypto.encryption.password.BcryptPasswordEncryptor
import spock.lang.Specification

class BcryptPasswordEncryptorTest extends Specification {
	def "test"() {
		given:
		def encryptor = new BcryptPasswordEncryptor()

		when:
		String password = encryptor.encryptPassword(message)
		def result = encryptor.checkPassword(message, password)

		then:
		result

		where:
		message                 | a
		"Hello World"           | 1
		"Spock单元测试快速入门" | 2
		"1234564789"            | 3
	}
}
