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

import io.github.pangju666.commons.validation.validator.NumberValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 数字格式校验注解
 * <p>支持校验：
 * <ul>
 *     <li>整数（decimal=false）</li>
 *     <li>小数（decimal=true）</li>
 *     <li>正数（positive=true）</li>
 * </ul></p>
 *
 * <p>
 * 支持的类型是 {@code CharSequence}。 {@code null} 视为有效，空白字符串视为无效。
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 * @see io.github.pangju666.commons.lang.pool.RegExPool#NUMBER
 * @see io.github.pangju666.commons.lang.pool.RegExPool#POSITIVE_NUMBER
 * @see io.github.pangju666.commons.lang.pool.RegExPool#FLOAT_NUMBER
 * @see io.github.pangju666.commons.lang.pool.RegExPool#POSITIVE_FLOAT_NUMBER
 */
@Documented
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = NumberValidator.class)
public @interface Number {
	/**
	 * 校验失败时的默认消息
	 */
	String message() default "数字格式不正确";

	/**
	 * 是否必须为正数（默认false）
	 *
	 * @since 1.0.0
	 */
	boolean positive() default false;

	/**
	 * 是否允许小数（默认false）
	 *
	 * @since 1.0.0
	 */
	boolean decimal() default false;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
