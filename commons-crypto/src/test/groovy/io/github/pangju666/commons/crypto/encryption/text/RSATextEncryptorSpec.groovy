package io.github.pangju666.commons.crypto.encryption.text

import io.github.pangju666.commons.crypto.key.RSAKeyPair
import io.github.pangju666.commons.crypto.lang.CryptoConstants
import io.github.pangju666.commons.crypto.transformation.impl.RSAOEAPWithSHA256Transformation
import io.github.pangju666.commons.crypto.transformation.impl.RSAPKCS1PaddingTransformation
import io.github.pangju666.commons.crypto.utils.KeyPairUtils
import org.jasypt.exceptions.AlreadyInitializedException
import org.jasypt.exceptions.EncryptionOperationNotPossibleException
import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

import java.security.KeyPair
import java.security.SecureRandom

@Title("RSATextEncryptor 单元测试")
class RSATextEncryptorSpec extends Specification {

	def "默认配置加密/解密成功"() {
		given:
		def encryptor = new RSATextEncryptor()
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair = RSAKeyPair.fromKeyPair(kp)
		encryptor.setKeyPair(pair)
		def input = "hello world"

		expect:
		encryptor.decrypt(encryptor.encrypt(input)) == input
	}

	def "指定方案构造为非空"() {
		expect:
		new RSATextEncryptor(new RSAOEAPWithSHA256Transformation()) != null
		new RSATextEncryptor(new RSAPKCS1PaddingTransformation()) != null
	}

	def "指定方案构造传入null抛出异常"() {
		when:
		new RSATextEncryptor(null)

		then:
		thrown(NullPointerException)
	}

	@Unroll
	def "encrypt 空输入返回空字符串: #label"() {
		given:
		def encryptor = new RSATextEncryptor()

		expect:
		encryptor.encrypt(input) != null && encryptor.encrypt(input).length() == 0

		where:
		label  | input
		"null" | null
		"空"   | ""
	}

	def "encrypt 未设置公钥抛出异常"() {
		given:
		def encryptor = new RSATextEncryptor()

		when:
		encryptor.encrypt("a")

		then:
		thrown(EncryptionOperationNotPossibleException)
	}

	@Unroll
	def "decrypt 空输入返回空字符串: #label"() {
		given:
		def encryptor = new RSATextEncryptor()

		expect:
		encryptor.decrypt(input) != null && encryptor.decrypt(input).length() == 0

		where:
		label  | input
		"null" | null
		"空"   | ""
	}

	def "decrypt 未设置私钥抛出异常"() {
		given:
		def encryptor = new RSATextEncryptor()

		when:
		encryptor.decrypt("SGVsbG8=")

		then:
		thrown(EncryptionOperationNotPossibleException)
	}

	def "initialize 后不可再设置密钥"() {
		given:
		def encryptor = new RSATextEncryptor()
		encryptor.initialize()
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair = RSAKeyPair.fromKeyPair(kp)

		when:
		encryptor.setKeyPair(pair)

		then:
		thrown(AlreadyInitializedException)
	}

	def "加密触发惰性初始化，之后再设置密钥抛异常"() {
		given:
		def encryptor = new RSATextEncryptor()
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair = RSAKeyPair.fromKeyPair(kp)
		encryptor.setKeyPair(pair)

		when:
		def encrypted = encryptor.encrypt("lazy-init")
		encryptor.setKeyPair(pair)

		then:
		encrypted.length() > 0
		thrown(AlreadyInitializedException)
	}

	def "分段加密与解密保持一致（OAEP）"() {
		given:
		def encryptor = new RSATextEncryptor(new RSAOEAPWithSHA256Transformation())
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair = RSAKeyPair.fromKeyPair(kp)
		encryptor.setKeyPair(pair)
		def data = randomAscii(1500)

		expect:
		encryptor.decrypt(encryptor.encrypt(data)) == data
	}

	def "分段加密与解密保持一致（PKCS1）"() {
		given:
		def encryptor = new RSATextEncryptor(new RSAPKCS1PaddingTransformation())
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair = RSAKeyPair.fromKeyPair(kp)
		encryptor.setKeyPair(pair)
		def data = randomAscii(1500)

		expect:
		encryptor.decrypt(encryptor.encrypt(data)) == data
	}

	def "跨密钥解密失败抛异常"() {
		given:
		KeyPair kp1 = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		KeyPair kp2 = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair1 = RSAKeyPair.fromKeyPair(kp1)
		def pair2 = RSAKeyPair.fromKeyPair(kp2)

		def encryptor1 = new RSATextEncryptor()
		encryptor1.setKeyPair(pair1)
		def encrypted = encryptor1.encrypt("mismatch")

		def decryptor2 = new RSATextEncryptor()
		decryptor2.setPrivateKey(pair2.getPrivateKey())

		when:
		decryptor2.decrypt(encrypted)

		then:
		thrown(EncryptionOperationNotPossibleException)
	}

	private static String randomAscii(int len) {
		def chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
		def r = new SecureRandom()
		def sb = new StringBuilder(len)
		for (int i = 0; i < len; i++) {
			sb.append(chars[r.nextInt(chars.size())])
		}
		return sb.toString()
	}
}
