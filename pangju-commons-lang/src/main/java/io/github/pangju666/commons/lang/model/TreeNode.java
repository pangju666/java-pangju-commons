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

package io.github.pangju666.commons.lang.model;

import java.util.Collection;

/**
 * 树数据结构接口
 *
 * @param <K> 键值类型
 * @param <T> 数据类型
 *
 * @author pangju666
 * @since 1.0.0
 */
public interface TreeNode<K, T> {
	/**
	 * 获取节点键值
	 *
	 * @return 节点键值
	 * @since 1.0.0
	 */
	K getNodeKey();

	/**
	 * 获取父级节点键值
	 *
	 * @return 父级节点键值
	 * @since 1.0.0
	 */
	K getParentNodeKey();

	/**
	 * 设置子级节点
	 *
	 * @param childNodes 子级节点集合
	 * @since 1.0.0
	 */
	void setChildNodes(final Collection<? extends TreeNode<K, T>> childNodes);
}
