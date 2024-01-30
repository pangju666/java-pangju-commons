package io.github.pangju666.commons.codec.utils;

import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Objects;

public class RsaUtils {
	public static final String ALGORITHM = "RSA";
	private static KeyPairGenerator DEFAULT_KEY_PAIR_GENERATOR;
	private static KeyFactory keyFactory;

	protected RsaUtils() {
	}

	public static KeyPair generateKey() {
		try {
			if (Objects.isNull(DEFAULT_KEY_PAIR_GENERATOR)) {
				DEFAULT_KEY_PAIR_GENERATOR = KeyPairGenerator.getInstance(ALGORITHM);
			}
			return DEFAULT_KEY_PAIR_GENERATOR.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			return ExceptionUtils.rethrow(e);
		}
	}

	public static String decryptToString(final InputStream inputStream, final PublicKey key) throws IOException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return new String(decrypt(inputStream.readAllBytes(), key, ALGORITHM));
	}

	public static String decryptToString(final InputStream inputStream, final PrivateKey key) throws IOException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return new String(decrypt(inputStream.readAllBytes(), key, ALGORITHM));
	}

	public static String decryptToString(final InputStream inputStream, final PublicKey key, final String transformation) throws IOException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return new String(decrypt(inputStream.readAllBytes(), key, transformation));
	}

	public static String decryptToString(final InputStream inputStream, final PrivateKey key, final String transformation) throws IOException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return new String(decrypt(inputStream.readAllBytes(), key, transformation));
	}

	public static String decryptToString(final InputStream inputStream, final PublicKey key, final Charset charset) throws IOException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return new String(decrypt(inputStream.readAllBytes(), key, ALGORITHM), charset);
	}

	public static String decryptToString(final InputStream inputStream, final PrivateKey key, final Charset charset) throws IOException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return new String(decrypt(inputStream.readAllBytes(), key, ALGORITHM), charset);
	}

	public static String decryptToString(final InputStream inputStream, final PublicKey key, final String transformation, final Charset charset) throws IOException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return new String(decrypt(inputStream.readAllBytes(), key, transformation), charset);
	}

	public static String decryptToString(final InputStream inputStream, final PrivateKey key, final String transformation, final Charset charset) throws IOException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return new String(decrypt(inputStream.readAllBytes(), key, transformation), charset);
	}

	public static String decryptToString(final ByteBuffer byteBuffer, final PublicKey key) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return new String(decrypt(byteBuffer.array(), key, ALGORITHM));
	}

	public static String decryptToString(final ByteBuffer byteBuffer, final PrivateKey key) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return new String(decrypt(byteBuffer.array(), key, ALGORITHM));
	}

	public static String decryptToString(final ByteBuffer byteBuffer, final PublicKey key, final String transformation) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return new String(decrypt(byteBuffer.array(), key, transformation));
	}

	public static String decryptToString(final ByteBuffer byteBuffer, final PrivateKey key, final String transformation) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return new String(decrypt(byteBuffer.array(), key, transformation));
	}

	public static String decryptToString(final ByteBuffer byteBuffer, final PublicKey key, final Charset charset) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return new String(decrypt(byteBuffer.array(), key, ALGORITHM), charset);
	}

	public static String decryptToString(final ByteBuffer byteBuffer, final PrivateKey key, final Charset charset) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return new String(decrypt(byteBuffer.array(), key, ALGORITHM), charset);
	}

	public static String decryptToString(final ByteBuffer byteBuffer, final PublicKey key, final String transformation, final Charset charset) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return new String(decrypt(byteBuffer.array(), key, transformation), charset);
	}

	public static String decryptToString(final ByteBuffer byteBuffer, final PrivateKey key, final String transformation, final Charset charset) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return new String(decrypt(byteBuffer.array(), key, transformation), charset);
	}

	public static String decryptToString(final byte[] input, final PublicKey key) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return new String(decrypt(input, key, ALGORITHM));
	}

	public static String decryptToString(final byte[] input, final PrivateKey key) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return new String(decrypt(input, key, ALGORITHM));
	}

	public static String decryptToString(final byte[] input, final PublicKey key, final String transformation) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return new String(decrypt(input, key, transformation));
	}

	public static String decryptToString(final byte[] input, final PrivateKey key, final String transformation) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return new String(decrypt(input, key, transformation));
	}

	public static String decryptToString(final byte[] input, final PublicKey key, final Charset charset) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return new String(decrypt(input, key, ALGORITHM), charset);
	}

	public static String decryptToString(final byte[] input, final PrivateKey key, final Charset charset) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return new String(decrypt(input, key, ALGORITHM), charset);
	}

	public static String decryptToString(final byte[] input, final PublicKey key, final String transformation, final Charset charset) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return new String(decrypt(input, key, transformation), charset);
	}

	public static String decryptToString(final byte[] input, final PrivateKey key, final String transformation, final Charset charset) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return new String(decrypt(input, key, transformation), charset);
	}

	public static byte[] decrypt(final InputStream inputStream, final PublicKey key) throws IOException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return decrypt(inputStream.readAllBytes(), key, ALGORITHM);
	}

	public static byte[] decrypt(final InputStream inputStream, final PrivateKey key) throws IOException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return decrypt(inputStream.readAllBytes(), key, ALGORITHM);
	}

	public static byte[] decrypt(final InputStream inputStream, final PublicKey key, final String transformation) throws IOException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return decrypt(inputStream.readAllBytes(), key, transformation);
	}

	public static byte[] decrypt(final InputStream inputStream, final PrivateKey key, final String transformation) throws IOException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return decrypt(inputStream.readAllBytes(), key, transformation);
	}

	public static byte[] decrypt(final ByteBuffer byteBuffer, final PublicKey key) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return decrypt(byteBuffer.array(), key, ALGORITHM);
	}

	public static byte[] decrypt(final ByteBuffer byteBuffer, final PrivateKey key) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return decrypt(byteBuffer.array(), key, ALGORITHM);
	}

	public static byte[] decrypt(final ByteBuffer byteBuffer, final PublicKey key, final String transformation) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return decrypt(byteBuffer.array(), key, transformation);
	}

	public static byte[] decrypt(final ByteBuffer byteBuffer, final PrivateKey key, final String transformation) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return decrypt(byteBuffer.array(), key, transformation);
	}

	public static byte[] decrypt(final byte[] input, final PublicKey key) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return decrypt(input, key, ALGORITHM);
	}

	public static byte[] decrypt(final byte[] input, final PrivateKey key) throws IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return decrypt(input, key, ALGORITHM);
	}

	public static byte[] decrypt(final byte[] input, final PublicKey key, final String transformation) throws IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return doFinal(input, key, transformation, Cipher.DECRYPT_MODE);
	}

	public static byte[] decrypt(final byte[] input, final PrivateKey key, final String transformation) throws IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		return doFinal(input, key, transformation, Cipher.DECRYPT_MODE);
	}

	public static byte[] encrypt(final InputStream inputStream, final PublicKey key) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
		return encrypt(inputStream.readAllBytes(), key, ALGORITHM);
	}

	public static byte[] encrypt(final InputStream inputStream, final PrivateKey key) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
		return encrypt(inputStream.readAllBytes(), key, ALGORITHM);
	}

	public static byte[] encrypt(final InputStream inputStream, final PublicKey key, final String transformation) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
		return encrypt(inputStream.readAllBytes(), key, transformation);
	}

	public static byte[] encrypt(final InputStream inputStream, final PrivateKey key, final String transformation) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
		return encrypt(inputStream.readAllBytes(), key, transformation);
	}

	public static byte[] encrypt(final String string, final PublicKey key) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
		return encrypt(string.getBytes(), key, ALGORITHM);
	}

	public static byte[] encrypt(final String string, final PrivateKey key) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
		return encrypt(string.getBytes(), key, ALGORITHM);
	}

	public static byte[] encrypt(final String string, final PublicKey key, final Charset charset) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
		return encrypt(string.getBytes(charset), key, ALGORITHM);
	}

	public static byte[] encrypt(final String string, final PrivateKey key, final Charset charset) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
		return encrypt(string.getBytes(charset), key, ALGORITHM);
	}

	public static byte[] encrypt(final String string, final PublicKey key, final String transformation) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
		return encrypt(string.getBytes(), key, transformation);
	}

	public static byte[] encrypt(final String string, final PrivateKey key, final String transformation) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
		return encrypt(string.getBytes(), key, transformation);
	}

	public static byte[] encrypt(final String string, final PublicKey key, final String transformation, final Charset charset) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
		return encrypt(string.getBytes(charset), key, transformation);
	}

	public static byte[] encrypt(final String string, final PrivateKey key, final String transformation, final Charset charset) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
		return encrypt(string.getBytes(charset), key, transformation);
	}

	public static byte[] encrypt(final ByteBuffer byteBuffer, final PublicKey key) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
		return encrypt(byteBuffer.array(), key, ALGORITHM);
	}

	public static byte[] encrypt(final ByteBuffer byteBuffer, final PrivateKey key) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
		return encrypt(byteBuffer.array(), key, ALGORITHM);
	}

	public static byte[] encrypt(final ByteBuffer byteBuffer, final PublicKey key, final String transformation) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
		return encrypt(byteBuffer.array(), key, transformation);
	}

	public static byte[] encrypt(final ByteBuffer byteBuffer, final PrivateKey key, final String transformation) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
		return encrypt(byteBuffer.array(), key, transformation);
	}

	public static byte[] encrypt(final byte[] input, final PublicKey key) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
		return encrypt(input, key, ALGORITHM);
	}

	public static byte[] encrypt(final byte[] input, final PrivateKey key) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
		return encrypt(input, key, ALGORITHM);
	}

	public static byte[] encrypt(final byte[] input, final PublicKey key, final String transformation) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
		return doFinal(input, key, transformation, Cipher.ENCRYPT_MODE);
	}

	public static byte[] encrypt(final byte[] input, final PrivateKey key, final String transformation) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
		return doFinal(input, key, transformation, Cipher.ENCRYPT_MODE);
	}

	public static KeyFactory getKeyFactory() throws NoSuchAlgorithmException {
		if (Objects.isNull(keyFactory)) {
			keyFactory = KeyFactory.getInstance(RsaUtils.ALGORITHM);
		}
		return keyFactory;
	}

	private static byte[] doFinal(final byte[] input, final Key key, final String transformation, int mode) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
		Cipher cipher = Cipher.getInstance(transformation);
		cipher.init(mode, key);

		int size;
		if (key instanceof PublicKey publicKey) {
			RSAPublicKeySpec keySpec = getKeyFactory().getKeySpec(publicKey, RSAPublicKeySpec.class);
			size = keySpec.getModulus().bitLength() / 8;
		} else if (key instanceof PrivateKey privateKey) {
			RSAPrivateKeySpec keySpec = getKeyFactory().getKeySpec(privateKey, RSAPrivateKeySpec.class);
			size = keySpec.getModulus().bitLength() / 8;
		} else {
			throw new InvalidKeyException();
		}
		if (mode == Cipher.ENCRYPT_MODE) {
			size -= 11;
		}
		if (input.length <= size) {
			return cipher.doFinal(input);
		}

		int inputLength = input.length;
		int offsetLength = 0;
		int i = 0;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		while (inputLength - offsetLength > 0) {
			byte[] bytes;
			if (inputLength - offsetLength > size) {
				bytes = cipher.doFinal(input, offsetLength, size);
			} else {
				bytes = cipher.doFinal(input, offsetLength, inputLength - offsetLength);
			}
			outputStream.writeBytes(bytes);
			++i;
			offsetLength = size * i;
		}
		return outputStream.toByteArray();
	}
}
