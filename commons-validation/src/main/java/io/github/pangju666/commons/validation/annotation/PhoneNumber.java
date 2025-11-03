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

import io.github.pangju666.commons.validation.enums.PhoneNumberType;
import io.github.pangju666.commons.validation.validator.PhoneNumberValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 电话号码校验注解
 * <p>支持类型：
 * <ul>
 *     <li>手机号码（type=MOBILE）</li>
 *     <li>固定电话（type=TEL）</li>
 *     <li>混合校验（type=MIX）</li>
 * </ul></p>
 *
 * @author pangju666
 * @since 1.0.0
 */
@Documented
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)
public @interface PhoneNumber {
	/**
	 * 校验失败时的默认消息
	 */
	String message() default "手机号格式不正确";

	/**
	 * 电话号码类型（默认混合校验）
	 *
	 * @since 1.0.0
	 */
	PhoneNumberType type() default PhoneNumberType.MIX;

	/**
	 * 是否校验运营商号段（默认false）
	 * <p>true时排除虚拟运营商号段</p>
	 *
	 * @since 1.0.0
	 */
	boolean strong() default false;

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

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
