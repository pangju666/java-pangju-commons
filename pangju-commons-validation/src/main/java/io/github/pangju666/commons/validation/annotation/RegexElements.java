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
import io.github.pangju666.commons.validation.validator.RegexElementsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 集合元素正则校验注解
 * <p>验证集合中所有/部分元素是否符合指定正则表达式，支持：
 * <ul>
 *     <li>通过allMatch控制全部匹配或任一匹配</li>
 *     <li>正则标志位设置（如忽略大小写等）</li>
 *     <li>集合空值策略控制</li>
 * </ul></p>
 *
 * @author pangju666
 * @since 1.0.0
 */
@Documented
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = RegexElementsValidator.class)
public @interface RegexElements {
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
	 * 是否要求所有元素匹配（默认true）
	 * <p>false表示只要任一元素匹配即通过</p>
	 *
	 * @since 1.0.0
	 */
	boolean allMatch() default true;

	/**
	 * 是否要求集合不能为空
	 * <p>包括null和空集合两种情况</p>
	 *
	 * @since 1.0.0
	 */
	boolean notEmpty() default false;

	/**
	 * 校验失败时的默认消息
	 */
	String message() default "集合中存在格式不正确的值";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
