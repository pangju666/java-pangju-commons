package io.github.pangju666.commons.codec.encryption.password;

import org.apache.commons.lang3.StringUtils;
import org.jasypt.util.password.PasswordEncryptor;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.security.SecureRandom;

/**
 * Bcrypt算法密码加密器
 * <p>
 * 使用步骤：
 *    <ol>
 *        <li>创建一个实例（使用new）</li>
 *        <li>执行加密（使用{@link #encryptPassword(String)}）或检查密码是否匹配（使用{@link #checkPassword(String, String)}）操作</li>
 *    </ol>
 * </p>
 * <br/>这个类是<i>线程安全的</i>
 *
 * @author pangju
 * @since 1.0.0
 */
public final class BcryptPasswordEncryptor implements PasswordEncryptor {
	private final SecureRandom random;

	public BcryptPasswordEncryptor() {
		this.random = new SecureRandom();
	}

	public BcryptPasswordEncryptor(SecureRandom random) {
		this.random = random;
	}

	@Override
	public String encryptPassword(String password) {
		if (StringUtils.isBlank(password)) {
			return StringUtils.EMPTY;
		}
		String salt = BCrypt.gensalt(BCrypt.gensalt("$2a", 10, random));
		return BCrypt.hashpw(password, salt);
	}

	@Override
	public boolean checkPassword(String plainPassword, String encryptedPassword) {
		if (StringUtils.isBlank(plainPassword)) {
			return StringUtils.isBlank(encryptedPassword);
		} else if (StringUtils.isBlank(encryptedPassword)) {
			return false;
		}
		return BCrypt.checkpw(plainPassword, encryptedPassword);
	}
}
