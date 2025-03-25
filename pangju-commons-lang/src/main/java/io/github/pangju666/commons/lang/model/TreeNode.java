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
 * 树形结构节点通用接口，定义树形数据结构的基本操作
 *
 * @param <K> 节点唯一标识类型（如Long、String等）
 * @param <T> 节点携带的业务数据类型
 *
 * @author pangju666
 * @since 1.0.0
 */
public interface TreeNode<K, T> {
	/**
	 * 获取当前节点的唯一标识键
	 *
	 * @return 节点的唯一标识键值，用于构建树形结构关系
	 * @since 1.0.0
	 */
	K getNodeKey();

	/**
	 * 获取父节点的唯一标识键
	 *
	 * @return 父节点的唯一标识键值，根节点应返回null或特定标识值
	 * @since 1.0.0
	 */
	K getParentNodeKey();

	/**
	 * 设置子节点集合
	 *
	 * @param childNodes 子节点集合，集合元素需实现TreeNode接口
	 * @since 1.0.0
	 */
	void setChildNodes(final Collection<T> childNodes);
}
