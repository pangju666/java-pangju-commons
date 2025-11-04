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

import io.github.pangju666.commons.validation.validator.UUIDSValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.hibernate.validator.constraints.UUID;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * UUID集合校验注解
 * <p>
 * 用于校验UUID集合的有效性。
 * </p>
 *
 * <p>
 *     代码参考自{@link org.hibernate.validator.constraints.UUID}
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
@Documented
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = UUIDSValidator.class)
public @interface UUIDS {
	/**
	 * 是否要求所有元素匹配（默认true）
	 * <p>false表示只要任一元素匹配即通过</p>
	 *
	 * @since 1.0.0
	 */
	boolean allMatch() default true;

	/**
	 * @return 允许集合中存在null或空白字符串。
	 * 默认情况下不允许集合中存在null或空白字符串。
	 */
	boolean allowEmpty() default false;

	/**
	 * @return {@code true} 如果 nil UUID {@code 00000000-0000-0000-0000-000000000000} 有效。
	 * 默认情况下，零 UUID 有效。
	 */
	boolean allowNil() default true;

	/**
	 * 接受 {@code [1; 15]} 范围内的值，与十六进制 {@code [1; f]} 范围相对应。
	 *
	 * @return 可接受的 UUID 版本号。
	 * 默认情况下，版本 1 至 5 是允许的。
	 */
	int[] version() default {1, 2, 3, 4, 5};

	/**
	 * 接受 {@code [0; 2]} 范围内的数值。
	 * <p>
	 *     UUID 的变体由第 17 个十六进制数字的二进制表示决定({@code xxxxxxxx-xxxxxx-xxxxxx-Vxxxxx-xxxxxxxxxxxx} 其中 {@code V} 是变体数字)。
	 * <p>
	 * 目前，验证器只支持 {@code [0, 1, 2]} 变体：
	 * <table>
	 *     <caption>表格 1</caption>
	 *     <thead>
	 *         <tr>
	 *             <th>变体 #</th>
	 *             <th>二进制表示法</th>
	 *             <th>十六进制数字</th>
	 *             <th>注释</th>
	 *         </tr>
	 *     </thead>
	 *     <tbody>
	 *         <tr>
	 *             <td>0</td>
	 *             <td>0xxx</td>
	 *             <td>0 - 7</td>
	 *             <td></td>
	 *         </tr>
	 *         <tr>
	 *             <td>1</td>
	 *             <td>10xx</td>
	 *             <td>8 - b</td>
	 *             <td></td>
	 *         </tr>
	 *         <tr>
	 *             <td>2</td>
	 *             <td>110x</td>
	 *             <td>c - d</td>
	 *             <td></td>
	 *         </tr>
	 *         <tr>
	 *             <td>-</td>
	 *             <td>1110</td>
	 *             <td>e</td>
	 *             <td>不支持，带有此类变体的 UUID 将被视为无效。</td>
	 *         </tr>
	 *         <tr>
	 *             <td>-</td>
	 *             <td>1111</td>
	 *             <td>f</td>
	 *             <td>不支持，带有此类变体的 UUID 将被视为无效。</td>
	 *         </tr>
	 *     </tbody>
	 * </table>
	 *
	 * @return 允许的 UUID 变体编号
	 * 默认情况下，允许所有变体 0 至 2
	 */
	int[] variant() default {0, 1, 2};

	/**
	 * @return 所需的字母大小写
	 * 默认情况下只有小写字母有效
	 *
	 * @see UUID.LetterCase
	 */
	UUID.LetterCase letterCase() default UUID.LetterCase.LOWER_CASE;

	String message() default "集合中存在无效的UUID";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}