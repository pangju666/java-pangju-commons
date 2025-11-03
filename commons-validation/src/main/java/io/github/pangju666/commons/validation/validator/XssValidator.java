package io.github.pangju666.commons.validation.validator;

import io.github.pangju666.commons.validation.annotation.Xss;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * 验证字符串是否包含HTML内容
 *
 * @author pangju666
 * @see Xss
 * @see Jsoup
 * @since 1.0.0
 */
public class XssValidator implements ConstraintValidator<Xss, String> {
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (StringUtils.isBlank(value)) {
			return true;
		}
		Document document = Jsoup.parse(value);
		// 如果解析后的文本与原始文本不同，则可能是 HTML
		return !document.text().equals(value.trim());
	}
}