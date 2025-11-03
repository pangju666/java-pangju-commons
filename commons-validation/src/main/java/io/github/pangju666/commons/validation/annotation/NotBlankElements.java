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

import io.github.pangju666.commons.validation.validator.NotBlankElementsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 集合元素非空校验注解
 * <p>验证集合中元素是否符合：
 * <ul>
 *     <li>当allMatch=true时，所有元素必须非空</li>
 *     <li>当allMatch=false时，至少一个元素非空</li>
 * </ul></p>
 *
 * @author pangju666
 * @since 1.0.0
 */
@Documented
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = NotBlankElementsValidator.class)
public @interface NotBlankElements {
	/**
	 * 校验失败时的默认消息
	 */
	String message() default "集合中存在空白的值";

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

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
