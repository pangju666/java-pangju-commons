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

import io.github.pangju666.commons.validation.validator.EnumNameValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 枚举值校验注解
 * <p>验证字段值是否在指定枚举类的枚举值范围内</p>
 *
 * @author pangju666
 * @since 1.0.0
 */
@Documented
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = EnumNameValidator.class)
public @interface EnumName {
	/**
	 * 校验失败时的默认消息
	 */
	String message() default "不是有效的枚举变量名称";

	/**
	 * 目标枚举类
	 *
	 * @since 1.0.0
	 */
	Class<? extends java.lang.Enum> enumClass();

	/**
	 * 是否忽略大小写匹配
	 *
	 * @since 1.0.0
	 */
	boolean ignoreCase() default true;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
