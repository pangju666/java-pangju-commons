package io.github.pangju666.commons.crypto.digest;

import io.github.pangju666.commons.crypto.key.RSAKey;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.commons.CommonUtils;
import org.jasypt.digest.StringDigester;

import java.nio.charset.StandardCharsets;

public final class RSAStringDigester implements StringDigester {
	private final RSAByteDigester byteDigester;

	public RSAStringDigester() {
		this.byteDigester = new RSAByteDigester();
	}

	public RSAStringDigester(RSAKey key) {
		this.byteDigester = new RSAByteDigester(key);
	}

	public void setKey(RSAKey key) {
		byteDigester.setKey(key);
	}

	public void setAlgorithm(String algorithm) {
		byteDigester.setAlgorithm(algorithm);
	}

	@Override
	public String digest(String message) {
		if (StringUtils.isBlank(message)) {
			return StringUtils.EMPTY;
		}
		return Base64.encodeBase64String(byteDigester.digest(message.getBytes(StandardCharsets.UTF_8)));
	}

	@Override
	public boolean matches(String message, String digest) {
		if (StringUtils.isBlank(message)) {
			return StringUtils.isBlank(digest);
		} else if (StringUtils.isBlank(digest)) {
			return false;
		}
		return byteDigester.matches(message.getBytes(StandardCharsets.UTF_8),
			Base64.decodeBase64(digest));
	}

	public String digestToHexadecimal(String message, boolean outputHex) {
		if (StringUtils.isBlank(message)) {
			return StringUtils.EMPTY;
		}
		return CommonUtils.toHexadecimal(byteDigester.digest(message.getBytes(StandardCharsets.UTF_8)));
	}

	public boolean matchesFromHexadecimal(String message, String digest, boolean inputHex) {
		if (StringUtils.isBlank(message)) {
			return StringUtils.isBlank(digest);
		} else if (StringUtils.isBlank(digest)) {
			return false;
		}
		return byteDigester.matches(message.getBytes(StandardCharsets.UTF_8),
			CommonUtils.fromHexadecimal(digest));
	}
}
