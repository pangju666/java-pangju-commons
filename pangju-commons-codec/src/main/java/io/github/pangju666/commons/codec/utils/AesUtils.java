package io.github.pangju666.commons.codec.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class AesUtils {
	public static final String ALGORITHM = "AES";
	private static KeyGenerator DEFAULT_KEY_GENERATOR;

	protected AesUtils() {
	}

	public static SecretKey generateKey() {
		try {
			if (Objects.isNull(DEFAULT_KEY_GENERATOR)) {
				DEFAULT_KEY_GENERATOR = KeyGenerator.getInstance(ALGORITHM);
			}
			return DEFAULT_KEY_GENERATOR.generateKey();
		} catch (NoSuchAlgorithmException e) {
			return ExceptionUtils.rethrow(e);
		}
	}

	public static String decryptToString(final InputStream inputStream, final SecretKey key) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return new String(decrypt(inputStream.readAllBytes(), key, ALGORITHM, null));
	}

	public static String decryptToString(final InputStream inputStream, final SecretKey key, final String transformation) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return new String(decrypt(inputStream.readAllBytes(), key, transformation, null));
	}

	public static String decryptToString(final InputStream inputStream, final SecretKey key, final String transformation, final byte[] iv) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return new String(decrypt(inputStream.readAllBytes(), key, transformation, iv));
	}

	public static String decryptToString(final InputStream inputStream, final SecretKey key, final Charset charset) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return new String(decrypt(inputStream.readAllBytes(), key, ALGORITHM, null), charset);
	}

	public static String decryptToString(final InputStream inputStream, final SecretKey key, final String transformation, final Charset charset) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return new String(decrypt(inputStream.readAllBytes(), key, transformation, null), charset);
	}

	public static String decryptToString(final InputStream inputStream, final SecretKey key, final String transformation, final Charset charset, final byte[] iv) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return new String(decrypt(inputStream.readAllBytes(), key, transformation, iv), charset);
	}

	public static String decryptToString(final ByteBuffer byteBuffer, final SecretKey key) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return new String(decrypt(byteBuffer.array(), key, ALGORITHM, null));
	}

	public static String decryptToString(final ByteBuffer byteBuffer, final SecretKey key, final String transformation) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return new String(decrypt(byteBuffer.array(), key, transformation, null));
	}

	public static String decryptToString(final ByteBuffer byteBuffer, final SecretKey key, final String transformation, final byte[] iv) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return new String(decrypt(byteBuffer.array(), key, transformation, iv));
	}

	public static String decryptToString(final ByteBuffer byteBuffer, final Charset charset, final SecretKey key) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return new String(decrypt(byteBuffer.array(), key, ALGORITHM, null), charset);
	}

	public static String decryptToString(final ByteBuffer byteBuffer, final Charset charset, final SecretKey key, final String transformation) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return new String(decrypt(byteBuffer.array(), key, transformation, null), charset);
	}

	public static String decryptToString(final ByteBuffer byteBuffer, final Charset charset, final SecretKey key, final String transformation, final byte[] iv) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return new String(decrypt(byteBuffer.array(), key, transformation, iv), charset);
	}

	public static String decryptToString(final byte[] bytes, final SecretKey key) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return new String(decrypt(bytes, key, ALGORITHM, null));
	}

	public static String decryptToString(final byte[] bytes, final SecretKey key, final String transformation) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return new String(decrypt(bytes, key, transformation, null));
	}

	public static String decryptToString(final byte[] bytes, final SecretKey key, final String transformation, final byte[] iv) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return new String(decrypt(bytes, key, transformation, iv));
	}

	public static String decryptToString(final byte[] bytes, final Charset charset, final SecretKey key) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return new String(decrypt(bytes, key, ALGORITHM, null), charset);
	}

	public static String decryptToString(final byte[] bytes, final Charset charset, final SecretKey key, final String transformation) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return new String(decrypt(bytes, key, transformation, null), charset);
	}

	public static String decryptToString(final byte[] bytes, final Charset charset, final SecretKey key, final String transformation, final byte[] iv) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return new String(decrypt(bytes, key, transformation, iv), charset);
	}

	public static byte[] decrypt(final InputStream inputStream, final SecretKey key) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return decrypt(inputStream.readAllBytes(), key, ALGORITHM, null);
	}

	public static byte[] decrypt(final InputStream inputStream, final SecretKey key, final String transformation) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return decrypt(inputStream.readAllBytes(), key, transformation, null);
	}

	public static byte[] decrypt(final InputStream inputStream, final SecretKey key, final String transformation, final byte[] iv) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return decrypt(inputStream.readAllBytes(), key, transformation, iv);
	}

	public static byte[] decrypt(final ByteBuffer byteBuffer, final SecretKey key) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return decrypt(byteBuffer.array(), key, ALGORITHM, null);
	}

	public static byte[] decrypt(final ByteBuffer byteBuffer, final SecretKey key, final String transformation) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return decrypt(byteBuffer.array(), key, transformation, null);
	}

	public static byte[] decrypt(final ByteBuffer byteBuffer, final SecretKey key, final String transformation, final byte[] iv) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return decrypt(byteBuffer.array(), key, transformation, iv);
	}

	public static byte[] decrypt(final byte[] input, final SecretKey key) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return decrypt(input, key, ALGORITHM, null);
	}

	public static byte[] decrypt(final byte[] input, final SecretKey key, final String transformation) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return decrypt(input, key, transformation, null);
	}

	public static byte[] decrypt(final byte[] input, final SecretKey key, final String transformation, final byte[] iv) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		Cipher cipher = Cipher.getInstance(transformation);
		if (ArrayUtils.isEmpty(iv)) {
			cipher.init(Cipher.DECRYPT_MODE, key);
		} else {
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
		}
		return cipher.doFinal(input);
	}

	public static byte[] encrypt(final InputStream inputStream, final SecretKey key) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return encrypt(inputStream.readAllBytes(), key, ALGORITHM, null);
	}

	public static byte[] encrypt(final InputStream inputStream, final SecretKey key, final String transformation) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return encrypt(inputStream.readAllBytes(), key, transformation, null);
	}

	public static byte[] encrypt(final InputStream inputStream, final SecretKey key, final String transformation, final byte[] iv) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return encrypt(inputStream.readAllBytes(), key, transformation, iv);
	}

	public static byte[] encrypt(final String string, final SecretKey key) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return encrypt(string.getBytes(), key, ALGORITHM, null);
	}

	public static byte[] encrypt(final String string, final SecretKey key, final String transformation) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return encrypt(string.getBytes(), key, transformation, null);
	}

	public static byte[] encrypt(final String string, final SecretKey key, final String transformation, final byte[] iv) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return encrypt(string.getBytes(), key, transformation, iv);
	}

	public static byte[] encrypt(final String string, final Charset charset, final SecretKey key) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return encrypt(string.getBytes(charset), key, ALGORITHM, null);
	}

	public static byte[] encrypt(final String string, final Charset charset, final SecretKey key, final String transformation) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return encrypt(string.getBytes(charset), key, transformation, null);
	}

	public static byte[] encrypt(final String string, final Charset charset, final SecretKey key, final String transformation, final byte[] iv) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return encrypt(string.getBytes(charset), key, transformation, iv);
	}

	public static byte[] encrypt(final ByteBuffer byteBuffer, final SecretKey key) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return encrypt(byteBuffer.array(), key, ALGORITHM, null);
	}

	public static byte[] encrypt(final ByteBuffer byteBuffer, final SecretKey key, final String transformation) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return encrypt(byteBuffer.array(), key, transformation, null);
	}

	public static byte[] encrypt(final ByteBuffer byteBuffer, final SecretKey key, final String transformation, final byte[] iv) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return encrypt(byteBuffer.array(), key, transformation, iv);
	}

	public static byte[] encrypt(final byte[] input, final SecretKey key) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return encrypt(input, key, ALGORITHM, null);
	}

	public static byte[] encrypt(final byte[] input, final SecretKey key, final String transformation) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return encrypt(input, key, transformation, null);
	}

	public static byte[] encrypt(final byte[] input, final SecretKey key, final String transformation, final byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		Cipher cipher = Cipher.getInstance(transformation);
		if (ArrayUtils.isEmpty(iv)) {
			cipher.init(Cipher.ENCRYPT_MODE, key);
		} else {
			cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
		}
		return cipher.doFinal(input);
	}
}