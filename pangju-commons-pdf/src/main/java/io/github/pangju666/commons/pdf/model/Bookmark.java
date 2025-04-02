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

package io.github.pangju666.commons.pdf.model;

import java.util.ArrayList;
import java.util.List;

/**
 * PDF文档书签
 * <p>
 * 表示PDF文档中的书签项，包含书签名称、页码和子书签列表
 * </p>
 *
 * @param name      书签名称
 * @param pageNumber 书签对应的页码(从1开始)，可为null表示无页码
 * @param children  子书签列表，默认为空列表
 * @author pangju666
 * @since 1.0.0
 */
public record Bookmark(String name, Integer pageNumber, List<Bookmark> children) {
	public Bookmark {
		children = new ArrayList<>();
	}

	public Bookmark(String name, Integer pageIndex) {
		this(name, pageIndex, new ArrayList<>());
	}
}