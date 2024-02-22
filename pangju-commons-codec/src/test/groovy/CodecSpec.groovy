import io.github.pangju666.commons.codec.digest.RSAStringDigester
import io.github.pangju666.commons.codec.encryption.binary.RSABinaryEncryptor
import io.github.pangju666.commons.codec.encryption.text.RSATextEncryptor
import io.github.pangju666.commons.codec.utils.RsaUtils
import spock.lang.Specification

import java.security.Security

class CodecSpec extends Specification {
	def "testAesUtils"() {
		setup:
		def key = RsaUtils.generateKeyPair()
		def publicKey = key.getPublic()
		def privateKey = key.getPrivate()

		def encryptor = new RSABinaryEncryptor()
		encryptor.setPublicKey(publicKey)
		encryptor.setPrivateKey(privateKey)

		def plainText = "测试文本"
		def encryptMessage = encryptor.encrypt(plainText.getBytes())
		println encryptMessage
		println new String(encryptor.decrypt(encryptMessage))
	}

	def "sadasdasd"() {
		setup:
		final List algos = new ArrayList(Security.getAlgorithms("Cipher"));
		Collections.sort(algos);
		final LinkedHashSet pbeAlgos = new LinkedHashSet();
		final Iterator algosIter = algos.iterator();
		while (algosIter.hasNext()) {
			final String algo = (String) algosIter.next();
			if (algo != null && algo.startsWith("RSA")) {
				pbeAlgos.add(algo);
			}
		}
		println Collections.unmodifiableSet(pbeAlgos);
	}

	def "asdasdsa"() {
		setup:
		def key = RsaUtils.generateKeyPair()
		def publicKey = key.getPublic()
		def a = new RSATextEncryptor()
		a.setPublicKey(publicKey.encoded)
		a.setPrivateKey(key.getPrivate().encoded)
		def result = a.encrypt("测试")
		println result
		println a.decrypt(result)
	}

	def "sadadasdasdadasdadadasd"() {
		setup:
		def key = RsaUtils.generateKeyPair()
		RSAStringDigester rSAStringDigester = new RSAStringDigester()
		rSAStringDigester.setPublicKey(key.getPublic().encoded)
		rSAStringDigester.setPrivateKey(key.getPrivate().encoded)
		rSAStringDigester.initialize()
		def encoded = rSAStringDigester.digest("测试文本")
		println encoded
		println rSAStringDigester.matches("测试文本", encoded)
	}
}