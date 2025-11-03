package io.github.pangju666.commons.crypto.encryption.text

import io.github.pangju666.commons.crypto.encryption.binary.RSABinaryEncryptor
import io.github.pangju666.commons.crypto.key.RSAKey
import io.github.pangju666.commons.crypto.transformation.impl.RSAOEAPWithSHA256Transformation
import io.github.pangju666.commons.crypto.transformation.impl.RSAPKCS1PaddingTransformation
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.binary.Hex
import org.jasypt.exceptions.EncryptionOperationNotPossibleException
import spock.lang.Specification

class RSATextEncryptorSpec extends Specification {
	RSATextEncryptor rsaTextEncryptor
	RSAKey rsaKey

	def setup() {
		rsaKey = RSAKey.random() // 假设RSAKey可以生成密钥对
		rsaTextEncryptor = new RSATextEncryptor()
		rsaTextEncryptor.setKey(rsaKey)
		rsaTextEncryptor.setTransformation(new RSAPKCS1PaddingTransformation())
	}

	def "测试默认构造函数"() {
		when:
		def encryptor = new RSATextEncryptor()

		then:
		encryptor.binaryEncryptor != null
	}

	def "测试自定义二进制加密器构造函数"() {
		given:
		def binaryEncryptor = new RSABinaryEncryptor()

		when:
		def encryptor = new RSATextEncryptor(binaryEncryptor)

		then:
		encryptor.binaryEncryptor == binaryEncryptor
	}

	def "测试设置加密方案"() {
		given:
		def transformation = new RSAOEAPWithSHA256Transformation()

		when:
		rsaTextEncryptor.setTransformation(transformation)

		then:
		rsaTextEncryptor.binaryEncryptor.transformation == transformation
	}

	def "测试设置RSA密钥对"() {
		given:
		def newKey = RSAKey.random()

		when:
		rsaTextEncryptor.setKey(newKey)

		then:
		rsaTextEncryptor.binaryEncryptor.key == newKey
	}

	def "测试Base64加密解密流程"() {
		given:
		def originalText = "Hello, RSA Encryption! 你好，RSA加密！"

		when:
		def encrypted = rsaTextEncryptor.encrypt(originalText)
		def decrypted = rsaTextEncryptor.decrypt(encrypted)

		then:
		encrypted.size() > 0
		Base64.isBase64(encrypted)
		decrypted == originalText
	}

	def "测试Hex加密解密流程"() {
		given:
		def originalText = "Hex format encryption test"

		when:
		def encrypted = rsaTextEncryptor.encryptToHexString(originalText)
		def decrypted = rsaTextEncryptor.decryptFromHexString(encrypted)

		then:
		encrypted.size() > 0
		Hex.decodeHex(encrypted)
		decrypted == originalText
	}

	def "测试空文本加密"() {
		when:
		def base64Result = rsaTextEncryptor.encrypt("")
		def hexResult = rsaTextEncryptor.encryptToHexString("")

		then:
		base64Result == ""
		hexResult == ""
	}

	def "测试空文本解密"() {
		when:
		def base64Result = rsaTextEncryptor.decrypt("")
		def hexResult = rsaTextEncryptor.decryptFromHexString("")

		then:
		base64Result == ""
		hexResult == ""
	}

	def "测试长文本加密解密"() {
		given:
		def longText = "Long text test. " * 100

		when:
		def base64Encrypted = rsaTextEncryptor.encrypt(longText)
		def base64Decrypted = rsaTextEncryptor.decrypt(base64Encrypted)

		def hexEncrypted = rsaTextEncryptor.encryptToHexString(longText)
		def hexDecrypted = rsaTextEncryptor.decryptFromHexString(hexEncrypted)

		then:
		base64Decrypted == longText
		hexDecrypted == longText
	}

	def "测试特殊字符加密解密"() {
		given:
		def specialChars = "!@#\$%^&*()_+{}[]:;'\"\\|,.<>/?\uD83D\uDE00"

		when:
		def base64Result = rsaTextEncryptor.encrypt(specialChars)
		def hexResult = rsaTextEncryptor.encryptToHexString(specialChars)

		then:
		rsaTextEncryptor.decrypt(base64Result) == specialChars
		rsaTextEncryptor.decryptFromHexString(hexResult) == specialChars
	}

	def "测试无效Hex解密"() {
		when:
		rsaTextEncryptor.decryptFromHexString("invalidHexString")

		then:
		thrown(EncryptionOperationNotPossibleException)
	}

	def "测试不同密钥加解密失败"() {
		given:
		def newEncryptor = new RSATextEncryptor()
		newEncryptor.setKey(RSAKey.random()) // 使用新密钥
		def encrypted = newEncryptor.encrypt("test message")

		when:
		rsaTextEncryptor.decrypt(encrypted)

		then:
		thrown(EncryptionOperationNotPossibleException)
	}

	def "测试多次加密结果不同"() {
		given:
		def message = "Same message different encryption results"

		when:
		def encrypted1 = rsaTextEncryptor.encrypt(message)
		def encrypted2 = rsaTextEncryptor.encrypt(message)

		then:
		encrypted1 != encrypted2
		rsaTextEncryptor.decrypt(encrypted1) == message
		rsaTextEncryptor.decrypt(encrypted2) == message
	}
}

