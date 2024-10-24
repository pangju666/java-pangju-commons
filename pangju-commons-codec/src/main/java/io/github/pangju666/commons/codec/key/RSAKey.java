package io.github.pangju666.commons.codec.key;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.jasypt.exceptions.AlreadyInitializedException;

import java.util.Objects;

public final class RSAKey {
	private byte[] publicKey;
	private byte[] privateKey;
	private boolean initialized = false;

	public synchronized void initialize() {
		if (this.initialized) {
			throw new AlreadyInitializedException();
		}
		this.initialized = true;
	}

	public byte[] getPublicKey() {
		return publicKey;
	}

	public synchronized void setPublicKey(byte[] publicKey) {
		Validate.isTrue(ArrayUtils.isNotEmpty(publicKey), "公钥不可为空");
		if (this.initialized) {
			throw new AlreadyInitializedException();
		}
		if (Objects.nonNull(this.publicKey)) {
			cleanPublicKey();
		}
		this.publicKey = new byte[publicKey.length];
		System.arraycopy(publicKey, 0, this.publicKey, 0, publicKey.length);
	}

	public byte[] getPrivateKey() {
		return privateKey;
	}

	public synchronized void setPrivateKey(byte[] privateKey) {
		Validate.isTrue(ArrayUtils.isNotEmpty(privateKey), "私钥不可为空");
		if (this.initialized) {
			throw new AlreadyInitializedException();
		}
		if (Objects.nonNull(this.privateKey)) {
			cleanPrivateKey();
		}
		this.privateKey = new byte[privateKey.length];
		System.arraycopy(privateKey, 0, this.privateKey, 0, privateKey.length);
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void cleanPublicKey() {
		if (Objects.nonNull(this.publicKey)) {
			byte[] key = this.publicKey;
			synchronized (key) {
				final int keyLength = key.length;
				for (int i = 0; i < keyLength; i++) {
					key[i] = (byte) 0;
				}
			}
		}
	}

	public void cleanPrivateKey() {
		if (Objects.nonNull(this.privateKey)) {
			byte[] key = this.privateKey;
			synchronized (key) {
				final int keyLength = key.length;
				for (int i = 0; i < keyLength; i++) {
					key[i] = (byte) 0;
				}
			}
		}
	}
}
