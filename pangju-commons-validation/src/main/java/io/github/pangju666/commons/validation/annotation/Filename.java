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

import io.github.pangju666.commons.validation.validator.FilenameValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 文件名格式校验注解
 * <p>验证字符串是否符合合法文件名规范：
 * <ul>
 *     <li>禁止包含 \ / : * ? " < > | 等特殊字符</li>
 *     <li>支持文件扩展名校验（根据extension参数）</li>
 * </ul></p>
 *
 * @author pangju666
 * @since 1.0.0
 */
@Documented
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = FilenameValidator.class)
public @interface Filename {
	/**
	 * 校验失败时的默认消息
	 */
	String message() default "文件名禁止包含特殊字符";

	/**
	 * 是否允许包含文件扩展名
	 *
	 * @since 1.0.0
	 */
	boolean extension() default true;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
