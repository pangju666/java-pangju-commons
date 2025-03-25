/*
 *   Copyright 2025 pangju666
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.pangju666.commons.validation.annotation;

import io.github.pangju666.commons.lang.enums.RegexFlag;
import io.github.pangju666.commons.validation.validator.RegexValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 正则表达式校验注解
 * <p>支持功能：
 * <ul>
 *     <li>自定义正则表达式</li>
 *     <li>正则标志位配置</li>
 *     <li>边界匹配控制</li>
 * </ul></p>
 *
 * @author pangju666
 * @since 1.0.0
 */
@Documented
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = RegexValidator.class)
public @interface Regex {
	/**
	 * 正则表达式模式（必填）
	 *
	 * @since 1.0.0
	 */
	String regexp();

	/**
	 * 正则匹配标志位（默认无）
	 * @see io.github.pangju666.commons.lang.enums.RegexFlag
	 *
	 * @since 1.0.0
	 */
	RegexFlag[] flags() default {};

	/**
	 * 是否强制从字符串开头匹配（默认true）
	 *
	 * @since 1.0.0
	 */
	boolean matchStart() default true;

	/**
	 * 是否强制匹配到字符串结尾（默认true）
	 *
	 * @since 1.0.0
	 */
	boolean matchEnd() default true;

	/**
	 * 是否要求值不能为空白（仅空格等空白字符）
	 *
	 * @since 1.0.0
	 */
	boolean notBlank() default false;

	/**
	 * 是否要求值不能为空字符串
	 *
	 * @since 1.0.0
	 */
	boolean notEmpty() default false;

	/**
	 * 校验失败时的默认消息
	 */
	String message() default "格式不正确";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
