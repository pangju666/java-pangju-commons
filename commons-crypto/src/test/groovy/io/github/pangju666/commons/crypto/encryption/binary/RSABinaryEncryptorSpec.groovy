package io.github.pangju666.commons.crypto.encryption.binary

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
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

@Title("RSABinaryEncryptor 单元测试")
class RSABinaryEncryptorSpec extends Specification {

	def "默认配置加密/解密成功"() {
		given:
		def encryptor = new RSABinaryEncryptor()
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair = RSAKeyPair.fromKeyPair(kp)
		encryptor.setKeyPair(pair)
		byte[] input = "hello world".bytes

		when:
		byte[] encrypted = encryptor.encrypt(input)
		byte[] decrypted = encryptor.decrypt(encrypted)

		then:
		encrypted != null && encrypted.length > 0
		new String(decrypted) == "hello world"
	}

	def "指定方案构造为非空"() {
		expect:
		new RSABinaryEncryptor(new RSAOEAPWithSHA256Transformation()) != null
		new RSABinaryEncryptor(new RSAPKCS1PaddingTransformation()) != null
	}

	def "指定方案构造传入null抛出异常"() {
		when:
		new RSABinaryEncryptor(null)

		then:
		thrown(NullPointerException)
	}

	@Unroll
	def "encrypt 空输入返回空数组: #label"() {
		given:
		def encryptor = new RSABinaryEncryptor()

		expect:
		encryptor.encrypt(input) != null && encryptor.encrypt(input).length == 0

		where:
		label  | input
		"null" | null
		"空"   | new byte[0]
	}

	def "encrypt 未设置公钥抛出异常"() {
		given:
		def encryptor = new RSABinaryEncryptor()

		when:
		encryptor.encrypt("a".bytes)

		then:
		thrown(EncryptionOperationNotPossibleException)
	}

	@Unroll
	def "decrypt 空输入返回空数组: #label"() {
		given:
		def encryptor = new RSABinaryEncryptor()

		expect:
		encryptor.decrypt(input) != null && encryptor.decrypt(input).length == 0

		where:
		label  | input
		"null" | null
		"空"   | new byte[0]
	}

	def "decrypt 未设置私钥抛出异常"() {
		given:
		def encryptor = new RSABinaryEncryptor()
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		encryptor.setPublicKey((RSAPublicKey) kp.public)

		when:
		encryptor.decrypt("a".bytes)

		then:
		thrown(EncryptionOperationNotPossibleException)
	}

	def "initialize 后不可再设置密钥"() {
		given:
		def encryptor = new RSABinaryEncryptor()
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
		def encryptor = new RSABinaryEncryptor()
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair = RSAKeyPair.fromKeyPair(kp)
		encryptor.setKeyPair(pair)
		byte[] data = "lazy-init".bytes

		when:
		byte[] encrypted = encryptor.encrypt(data)
		encryptor.setKeyPair(pair)

		then:
		encrypted.length > 0
		thrown(AlreadyInitializedException)
	}

	def "分段加密与解密保持一致（OAEP）"() {
		given:
		def encryptor = new RSABinaryEncryptor(new RSAOEAPWithSHA256Transformation())
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair = RSAKeyPair.fromKeyPair(kp)
		encryptor.setKeyPair(pair)
		byte[] data = new byte[500]
		new SecureRandom().nextBytes(data)

		expect:
		new String(encryptor.decrypt(encryptor.encrypt(data))) == new String(data)
	}

	def "分段加密与解密保持一致（PKCS1）"() {
		given:
		def encryptor = new RSABinaryEncryptor(new RSAPKCS1PaddingTransformation())
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair = RSAKeyPair.fromKeyPair(kp)
		encryptor.setKeyPair(pair)
		byte[] data = new byte[500]
		new SecureRandom().nextBytes(data)

		expect:
		new String(encryptor.decrypt(encryptor.encrypt(data))) == new String(data)
	}

	def "跨密钥解密失败抛异常"() {
		given:
		KeyPair kp1 = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		KeyPair kp2 = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair1 = RSAKeyPair.fromKeyPair(kp1)
		def pair2 = RSAKeyPair.fromKeyPair(kp2)

		def encryptor1 = new RSABinaryEncryptor()
		encryptor1.setKeyPair(pair1)
		byte[] data = "mismatch".bytes
		byte[] encrypted = encryptor1.encrypt(data)

		def decryptor2 = new RSABinaryEncryptor()
		decryptor2.setPrivateKey((RSAPrivateKey) kp2.private)

		when:
		decryptor2.decrypt(encrypted)

		then:
		thrown(EncryptionOperationNotPossibleException)
	}
}
