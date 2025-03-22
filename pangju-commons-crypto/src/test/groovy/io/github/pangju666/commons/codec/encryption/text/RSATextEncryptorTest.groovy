package io.github.pangju666.commons.codec.encryption.text

import io.github.pangju666.commons.crypto.encryption.text.RSATextEncryptor
import io.github.pangju666.commons.crypto.key.RSAKey
import org.apache.commons.codec.binary.Base64
import spock.lang.Specification
import spock.lang.Unroll

class RSATextEncryptorTest extends Specification {
	RSAKey key

	def setup() {
		key = new RSAKey()
		key.setPublicKey(Base64.decodeBase64(
			"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhD5nkHgXMROVlV2pVaZw" +
				"TyFn0Qnj/NdlOBmamuNgp+fpqWNeZ01nXYK/T05+FF/cfZ3w2mJjX9GVBPIEyOCB" +
				"IwkIhSu9KACzYqLpyqwCHVNLNmnuBpGYzJV2OpKtkqgCaVi2KGhvTcQMW84k52MO" +
				"b9xho15iXss0lhktEB1dkRjZZ1mI2d6iahpXMNooz/Ctuy09e0zHDy0RQTrMOtwf" +
				"0hSspJEaxJKeb4xGsocOLXS9CJ+AZ3ctHUTwOSaYFr81RpUIIo5zd6ePdZaHIi/Z" +
				"zlnu5KAszyfQ7jtgu1ejQDyjkPHFtmKJuM8lCwX/YVnPeqyS0qC0gFyM9V9Z5fDD" +
				"xQIDAQAB"
		))
		key.setPrivateKey(Base64.decodeBase64(
			"MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCEPmeQeBcxE5WV" +
				"XalVpnBPIWfRCeP812U4GZqa42Cn5+mpY15nTWddgr9PTn4UX9x9nfDaYmNf0ZUE" +
				"8gTI4IEjCQiFK70oALNiounKrAIdU0s2ae4GkZjMlXY6kq2SqAJpWLYoaG9NxAxb" +
				"ziTnYw5v3GGjXmJeyzSWGS0QHV2RGNlnWYjZ3qJqGlcw2ijP8K27LT17TMcPLRFB" +
				"Osw63B/SFKykkRrEkp5vjEayhw4tdL0In4Bndy0dRPA5JpgWvzVGlQgijnN3p491" +
				"lociL9nOWe7koCzPJ9DuO2C7V6NAPKOQ8cW2Yom4zyULBf9hWc96rJLSoLSAXIz1" +
				"X1nl8MPFAgMBAAECggEAca33dxBU12I/dwvxsegGqW9NADxhWQuMPmJ8LeOCRjQs" +
				"/+ik0WaPkaoAbNtLtJYHQ5DO59wmEstmL78gb9HWENkGEvxbeZTIn+Tt1sRweOcO" +
				"Q7Nh4xGr6UoePRGz1dtJX2fcBvZnn9EE0rJttN7JHU20PSy4oUZLaTAxNaYYtc4E" +
				"QSrvofX3u4ZyKuSe0HmIUxeEcoNqr1ud3pWnNlRpF2RFBUG2hrx8aSjzb6LJEO7/" +
				"PDDL3FMtJuRkSM89YXrEnYu4BZdLUsumP4CyWAkQn0ExyApR/RD9PnEpHtUlsTab" +
				"XCnBFEogUGTq/PxAdrEZTzZmj79Ep4NygYtRSL1+AQKBgQDjReOlBHL5sPkeByWx" +
				"DRJGl7VPF4i51WXDW1Eb+7fYt3/PVlQcaYzjZs6cw6tPCXTEK1NpcTb8ViVCM0Tu" +
				"Ci/gdddNnEX2XYmW842xBO/mY1JHr9wz0OoG7YlBObEiZblvffsAGgjOeQQO66j3" +
				"v5wYdOOzVCfpBA297mCSxuk2RQKBgQCU9Y4BnuAMTfPFLZ5NmKJtK5W+E654jayw" +
				"RaQCqtQ//276hGMCpvezzTPhRKnN0bzyNgeGF2nNltqhstFdH6F3YFLTlEcKHRR2" +
				"3VR22UlhoiQwmGvc2+SKuEizzE734S+4sbYCMA4ULK4J9VyBVOllrp3o5SMYn1Qh" +
				"Ni6JctrvgQKBgCIyUOdyfO5PD5zSDHzQb7CJgTFuZBc6Ib68Tb79KBOGwMdswOkp" +
				"hJZu0KXL10nkLVqa/kj+TPy45ZBJcJS0mbGXaZb27Zv9RQeei/JXwNUUmrvInUR6" +
				"qcvzD9TtnlaDodxBw3Ondy3CDbdFBD6K6SzQ4bYI8pxgDMVISeWr4klVAoGAdCLf" +
				"DLRpyi4cmObwPV97g9IvdXqy1JrgwK4LKWk3Ao6MYBVHfJHhHfYnNMLsAOQ9hDpL" +
				"s2gdvYSYAOoCAbDfmssmyH8aw+/YPLRjXiYa6FwaCylLL27hyKXVSRlJmEmhg0ZK" +
				"uXnuABy1tF3wOYWSUwzJYQMFUfUnCp3Luq4ptAECgYAH7HztYxy+ytrWDyr0qHEK" +
				"oXhfK2xocuVAG76TK5/EGko6RhetCOTK9ZJEPskTeJHuZvzyIPAopRNcztwYSYMr" +
				"ju7S1+8BLWnWvzpVBO2M20AtNajOMJV/dTOavCIK/OLZy0CCIf9mP6HV768hAPVA" +
				"vFe94LZZkZ4mIcVjbA78Cw=="
		))

	}

	@Unroll
	def "test"() {
		given:
		def encryptor = new RSATextEncryptor()
		encryptor.setKey(key)

		when:
		String encryptStr = encryptor.encrypt(message)
		def result = encryptor.decrypt(encryptStr)

		then:
		message == result

		where:
		message                 | a
		"Hello World"           | 1
		"Spock单元测试快速入门" | 2
		"1234564789"            | 3
	}
}
