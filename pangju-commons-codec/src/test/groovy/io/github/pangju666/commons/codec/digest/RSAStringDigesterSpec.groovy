package io.github.pangju666.commons.codec.digest

import io.github.pangju666.commons.codec.key.RSAKey
import org.apache.commons.codec.binary.Base64
import spock.lang.Specification
import spock.lang.Unroll

class RSAStringDigesterSpec extends Specification {
	RSAKey key

	def setup() {
		key = new RSAKey()
		key.setPublicKey(Base64.decodeBase64(
			"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAu1vxQXamu75lZRmeeKM5+mt3wRnXQfBMhuPVklegrihCnF5Ao5vW3/zdxkZSqp+s+LWQxMwebDpc1+P4b+7mCkkG2Lq6qIIlkhj8+c5hBYKJ4GvA0H7pGxdBnQ22GDoAa/8iFkHQsVAga59bXh9sIB6/OTlXeGJWmFc2lHAfZWMdAMtTN41gvWgI5knqBiGi+neW/GGszm3tyRaPRJkgNCZS1ATSDo5MlM3+f3fv6dkLR7kQ3uq4aeEUHJGbE0SgaRIrK2jmn4wD5XlHC/Q0nkrnIb+/WN2wd+e4P8dDGVj86NfysWHLvRVTeZ+RHNh9WxjyedDNhj9UkrcrEoS0JwIDAQAB"))
		key.setPrivateKey(Base64.decodeBase64(
			"MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC7W/FBdqa7vmVlGZ54ozn6a3fBGddB8EyG49WSV6CuKEKcXkCjm9bf/N3GRlKqn6z4tZDEzB5sOlzX4/hv7uYKSQbYurqogiWSGPz5zmEFgonga8DQfukbF0GdDbYYOgBr/yIWQdCxUCBrn1teH2wgHr85OVd4YlaYVzaUcB9lYx0Ay1M3jWC9aAjmSeoGIaL6d5b8YazObe3JFo9EmSA0JlLUBNIOjkyUzf5/d+/p2QtHuRDe6rhp4RQckZsTRKBpEisraOafjAPleUcL9DSeSuchv79Y3bB357g/x0MZWPzo1/KxYcu9FVN5n5Ec2H1bGPJ50M2GP1SStysShLQnAgMBAAECggEAI1EJ8XaNqE8T6tJA8mmGOMOAL9iQoF/M9RJGRE8bPSjeoX4MvipWfyM5pLHFzF7L6pfDAa2eQHVh4doZjvPfzemFDl5oZ/IcZeUq7mWuGS1JkxGMp2B8zTv9hL68Z+WrBbByevKOBZZefhrsTJostgNFtWIvMESNISszYRifems5OOSUN7NSKsgLyFrmVbliOynO/5flM2YRUUzmjMvNy0mt4Txyuw4oisjI+zDqGug6fShVAZUXTIaz1wacKjlNSA4kEB2QhSBM6MOP34yHQr3ro+MBv/tzCUGvRCnro6CJspuxLYtXDsfc4RCqeMQOafAfvCZi3SLT/K3kMfSsBQKBgQDlhR/5H9vQg4k2cWzWjfxwy+Y3hXozNqD+B+nKLKATEpllNTNM57NOIGe+VawfbYAIv9iLS7MMqQ4elfTkIuFnMAKOPNCsfWVIa4r+RWHloG86aRKxUTvkvuImgedlXXxYDLnB8rEPtyuSCT5bRJPQShPfn4Z9vqHMXU3IwXmkbQKBgQDQ+ZiTIGAtUvN1858hpsxeTMfu0STm3nMZMVU5WvEc0P2q4lZNeCdQdM6dqlT5Ez6nDVC47ZknXNtdb5m3WLezQKMF0MWThyXfIO/giY4YbDbJBcBtiAmDbT52plGuJIzYBYZGhQygv9HT55D/G1OvoDl1HWi+Li3ikhDuWsPWYwKBgQCfnDdC4LPVSlO2R6R/hBfkPPdo7uqvCLNmVQxw6x2ahdmktx1owsw/bWuXwi5DYLuj2f31yHvINxw5iuDVcag8QPfWAFbBTSaM+TScZJWSwUH4Za95Hdu/1Dqiur23EQ7ykE7xoBsfqaqkEMeLYqJEO++cGGzHrzUHUG9SR7GW9QKBgFZ1DUTBfkkCKqvAETQw+BiDl6wcZzIFEoV3vZSvJVYQuQI8BgyjC1DkIp9kWyd5aNBgV+dKlbSNXEx54ZTTZZ0JvuinCTcSEP8Rd2zkPB+qlbfxYz1trNHtHoykHuL3SDYPgxAl96WB9uO+yGRi/+qHVX44np8gJ+e3Gr4iWyeVAoGAbWzAX+iljO46ADdHg6gbMCXbiU/GEfYLlzeOeoI38Wb587irxIcZQrDvM0u7P58e/JDHA/eFyusBvC2dTCNEms3tfn8T1U2kPJR51qtH1feoVjEZTjAxApmUbLIZgVKAeNVWOEQbPloYP6VAVeMt/Zf5Le+/wbIfdQpgDCUu2dw="))

	}

	@Unroll
	def "Digest"() {
		given:
		def digester = new RSAStringDigester()
		digester.setKey(key)

		when:
		def digest = digester.digest(message)

		then:
		digest == expectDigest

		where:
		message                 | expectDigest
		"Hello World"           | "INm6Hp2XLeT/4dvy28ibd35TSbwxEQqvCGu8JaeUDt1PyObKxUSGEEbmtD+TpKXPAUzRepXSeSKzkDLDkqH9AUzBHEt08opc88PuivEgtkn+5R2a6AF4Do2Rb5o7jeCotyYdaEh1oy9eCMFWFhI8k5IyBFrD7h+CfG2j9MImIJGRksJCZ53YL0xULuJeAqK5elBqyspNlXrr6b1w1eJPNgqzFzX8X5nfwsLva1HxTEG467XBjYWM64qQVU5ULDTCGo2ycadLK4W8iuagGHo7KRgrAiuRlP28sAhAJfxOmls59ebeyoMbhkRepCM7OHL1Kwz7n1ssRXXX/3hs2q9ZSw=="
		"Spock单元测试快速入门" | "FUA6LgD0Y+LowCmmI4MLnKv2r/4in0bNTUeRZ1lPSYI1nkIUtSJsMMjg5TK8oK5PR9EMReehqKUtgShC3pkvHkaQwcJ9p+1DdEBnVeHNlvuHozTAfVMiuNH4iawnxViybTusNRIYAXGkfMOgYM6f5aRDgIprYgS6T7lZ4QYI0Us57YD5ynZ7SO920+qZHZ5zByUwsrn2LvvexHgMTt6uHOFihjlQFoYPkJevEkHoNXvZ4xuuR75m/5sS1X2psL6AjvpGhTJifUGa8o+2ogxxQRH54FqogoJTFTQwN8YVmiK6BU1bDs6xqg1SDInPU7ACVIStYR2zvSnc2sO8e4DzgA=="
		"1234564789"            | "Kkq9FWX0cnHyYEV+NL6xfFcZZm3d/aFy1fL11RUoW3PtIyEF0YO2zPvUOM39DFfRaW877O+KTTDeA8S4GhXYZ+8b2ZlsAwTAY+v+gT9gp/X+5NKwLikCSWui+VHZsHXCUuONf/ZeMb0/KrlRpxFa+Tx8kdXvprkHI9DNEHH3meypu9owM0qGNQ1D3TrDVAUDo1sVkrDRTeJcghRUhX32pymsnsfydiUbmpR0NeblIjfxSfOcx6d3SLGR/Lbb3+4hH854ZybCBJI3y8eOoLIVpAgXKeef8uv/SkR7cDWFJGvzEmzjUb6WvB1Tyrae6WNvhB11FycHN1gR4FrNp30dZw=="
	}

	@Unroll
	def "Matches"() {
		given:
		def digester = new RSAStringDigester()
		digester.setKey(key)

		when:
		def result = digester.matches(message, digest)

		then:
		result

		where:
		message                 | digest
		"Hello World"           | "INm6Hp2XLeT/4dvy28ibd35TSbwxEQqvCGu8JaeUDt1PyObKxUSGEEbmtD+TpKXPAUzRepXSeSKzkDLDkqH9AUzBHEt08opc88PuivEgtkn+5R2a6AF4Do2Rb5o7jeCotyYdaEh1oy9eCMFWFhI8k5IyBFrD7h+CfG2j9MImIJGRksJCZ53YL0xULuJeAqK5elBqyspNlXrr6b1w1eJPNgqzFzX8X5nfwsLva1HxTEG467XBjYWM64qQVU5ULDTCGo2ycadLK4W8iuagGHo7KRgrAiuRlP28sAhAJfxOmls59ebeyoMbhkRepCM7OHL1Kwz7n1ssRXXX/3hs2q9ZSw=="
		"Spock单元测试快速入门" | "FUA6LgD0Y+LowCmmI4MLnKv2r/4in0bNTUeRZ1lPSYI1nkIUtSJsMMjg5TK8oK5PR9EMReehqKUtgShC3pkvHkaQwcJ9p+1DdEBnVeHNlvuHozTAfVMiuNH4iawnxViybTusNRIYAXGkfMOgYM6f5aRDgIprYgS6T7lZ4QYI0Us57YD5ynZ7SO920+qZHZ5zByUwsrn2LvvexHgMTt6uHOFihjlQFoYPkJevEkHoNXvZ4xuuR75m/5sS1X2psL6AjvpGhTJifUGa8o+2ogxxQRH54FqogoJTFTQwN8YVmiK6BU1bDs6xqg1SDInPU7ACVIStYR2zvSnc2sO8e4DzgA=="
		"1234564789"            | "Kkq9FWX0cnHyYEV+NL6xfFcZZm3d/aFy1fL11RUoW3PtIyEF0YO2zPvUOM39DFfRaW877O+KTTDeA8S4GhXYZ+8b2ZlsAwTAY+v+gT9gp/X+5NKwLikCSWui+VHZsHXCUuONf/ZeMb0/KrlRpxFa+Tx8kdXvprkHI9DNEHH3meypu9owM0qGNQ1D3TrDVAUDo1sVkrDRTeJcghRUhX32pymsnsfydiUbmpR0NeblIjfxSfOcx6d3SLGR/Lbb3+4hH854ZybCBJI3y8eOoLIVpAgXKeef8uv/SkR7cDWFJGvzEmzjUb6WvB1Tyrae6WNvhB11FycHN1gR4FrNp30dZw=="
	}
}
