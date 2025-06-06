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

import io.github.pangju666.commons.validation.validator.IPValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * IP地址校验注解
 * <p>支持格式：
 * <ul>
 *     <li>IPv4标准格式（如192.168.1.1）</li>
 *     <li>IPv6标准格式（需开启ipv6参数）</li>
 * </ul></p>
 *
 * @author pangju666
 * @since 1.0.0
 */
@Documented
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = IPValidator.class)
public @interface IP {
	/**
	 * 校验失败时的默认消息
	 */
	String message() default "ip地址格式不正确";

	/**
	 * 是否启用IPv6校验（默认false）
	 * <p>false时仅校验IPv4格式</p>
	 *
	 * @since 1.0.0
	 */
	boolean ipv6() default false;

	/**
	 * 是否不允许空白字符串（仅空格等）
	 *
	 * @since 1.0.0
	 */
	boolean notBlank() default false;

	/**
	 * 是否不允许空字符串
	 *
	 * @since 1.0.0
	 */
	boolean notEmpty() default false;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}