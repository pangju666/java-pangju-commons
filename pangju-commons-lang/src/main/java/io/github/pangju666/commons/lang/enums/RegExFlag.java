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

package io.github.pangju666.commons.lang.enums;

import java.util.regex.Pattern;

/**
 * 正则模式标志位（文档是机翻的）
 *
 * @author pangju666
 * @see java.util.regex.Pattern
 * @since 1.0.0
 */
public enum RegExFlag {
	/**
	 * 启用 Unix 行模式。
	 *
	 * <p> 在该模式下，只有{@code '\n'} 行结束符被识别。
	 * 在{@code .}、{@code ^}和{@code $}的行为中。
	 *
	 * <p> Unix 行模式也可以通过嵌入式标志启用
	 * 表达式&nbsp;{@code (?d)}.
	 */
	UNIX_LINES(Pattern.UNIX_LINES),
	/**
	 * 启用对案例不敏感的匹配。
	 *
	 * <p>默认情况下，对案例不敏感的匹配假设只有us-ascii charset中的字符才能匹配。可以通过与此标志结合指定{@link #UNICODE_CASE}
	 * 标志来启用Unicode-Aware-nicode-case-Insimentiment匹配。
	 *
	 * <p>也可以通过嵌入式标志表达式{@code (?i)}启用情况不敏感的匹配。
	 *
	 * <p>指定此标志可能会施加轻微的性能罚款。<p>
	 */
	CASE_INSENSITIVE(Pattern.CASE_INSENSITIVE),
	/**
	 * 允许在模式中使用空白和注释。
	 *
	 * <p> 在这种模式下，空白将被忽略，以 {@code #} 开头的嵌入式注释将被忽略。
	 * 以 {@code #} 开头的嵌入式注释将被忽略，直至行尾。
	 *
	 * <p> 注释模式也可以通过嵌入式标记启用
	 * expression&nbsp;{@code (?x)}.
	 */
	COMMENTS(Pattern.COMMENTS),
	/**
	 * 启用多行模式。
	 *
	 * <p> 在多行模式下，表达式 {@code ^} 和 {@code $} 匹配
	 * 分别在行结束符或输入序列结束符之后或之前匹配。
	 * 在多行模式下，表达式 {@code ^} 和 {@code $}  默认情况下，这些表达式只匹配
	 * 默认情况下，这些表达式只匹配整个输入序列的开头和结尾。
	 *
	 * <p> 多行模式也可以通过嵌入式标志启用
	 * 表达式&nbsp;{@code (?m)}.  </p>
	 */
	MULTILINE(Pattern.MULTILINE),
	/**
	 * 启用 dotall 模式。
	 *
	 * <p> 在 dotall 模式下，表达式 {@code .} 匹配任何字符、
	 * 包括行结束符。  默认情况下，该表达式不匹配
	 * 行结束符。
	 *
	 * <p> 还可以通过嵌入式标志启用 dotall 模式
	 * 表达式&nbsp;{@code (?s)} 。  {@code s} 是 "单行 "模式的助记符。
	 * 单行 "模式的助记符，这在 Perl 中被称为 "单行 "模式）。  </p>
	 */
	DOTALL(Pattern.DOTALL),
	/**
	 * 启用 Unicode 识别大小写折叠。
	 *
	 * <p> 如果指定了这个标记，那么在使用{@link #CASE_INSENSITIVE} 标记启用大小写不敏感匹配时
	 * 当使用 {@link #CASE_INSENSITIVE} 标志时，大小写不敏感匹配将以符合统一字符编码标准的方式进行。
	 * 与 Unicode 标准一致。  默认情况下，大小写不敏感
	 * 匹配假定只匹配 US-ASCII 字符集中的字符。
	 * 匹配。
	 *
	 * <p> Unicode 感知大小写折叠也可通过嵌入式标志启用
	 * 表达式&nbsp;{@code (?u)}.
	 *
	 * <p> 指定此标志可能会带来性能损失。  </p>
	 */
	UNICODE_CASE(Pattern.UNICODE_CASE),
	/**
	 * 启用规范等价。
	 *
	 * <p> 指定此标志后，两个字符将被视为
	 * 当且仅当它们的全规范分解相匹配时，才会被视为匹配。
	 * 例如，表达式 <code>"a&#92;u030A"</code> 将与字符串 <code>"&#92;u030A"</code> 匹配。
	 * 字符串 <code>"&#92;u00E5"</code> 。  默认情况下
	 * 匹配不考虑规范等价性。
	 *
	 * <p> 没有用于启用规范等价的嵌入式标志字符。
	 * 等价。
	 *
	 * <p> 指定此标志可能会影响性能。  </p>
	 */
	CANON_EQ(Pattern.CANON_EQ);

	private final int value;

	RegExFlag(final int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}