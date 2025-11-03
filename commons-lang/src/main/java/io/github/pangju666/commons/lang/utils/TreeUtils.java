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

package io.github.pangju666.commons.lang.utils;

import io.github.pangju666.commons.lang.model.TreeNode;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 树形结构构建工具类，提供将扁平数据转换为树形结构的能力
 * <p>支持单根/多根树构建，适用于菜单、目录等树形数据结构处理</p>
 *
 * @author pangju666
 * @since 1.0.0
 * @see TreeNode
 */
public class TreeUtils {
	protected TreeUtils() {
	}

	/**
	 * 构建树形结构（基础版本）
	 *
	 * @param collection 扁平节点集合
	 * @param <K>        节点键类型
	 * @param <T>        树节点类型
	 * @return 树形结构节点列表
	 * @since 1.0.0
	 */
	public static <K, T extends TreeNode<K, T>> List<T> toTree(final Collection<T> collection) {
		return toTree(collection, null, null);
	}

	/**
	 * 构建树形结构（基础版本）
	 *
	 * @param collection  扁平节点集合
	 * @param rootNodeKey 根节点标识键值（通常为null或特定值）
	 * @param <K>         节点键类型
	 * @param <T>         树节点类型
	 * @return 树形结构节点列表
	 * @since 1.0.0
	 */
	public static <K, T extends TreeNode<K, T>> List<T> toTree(final Collection<T> collection, final K rootNodeKey) {
		if (Objects.isNull(collection) || collection.isEmpty()) {
			return Collections.emptyList();
		}
		List<T> treeNodes = collection.stream()
			.filter(node -> Objects.equals(node.getParentNodeKey(), rootNodeKey))
			.collect(Collectors.toList());
		setChildNodes(collection, treeNodes, null);
		return treeNodes;
	}

	/**
	 * 构建树形结构（支持节点转换处理）
	 *
	 * @param collection  扁平节点集合
	 * @param convertFunc 节点转换函数
	 * @param <K>         节点键类型
	 * @param <T>         树节点类型
	 * @return 树形结构节点列表
	 * @since 1.0.0
	 */
	public static <K, T extends TreeNode<K, T>> List<T> toTree(final Collection<T> collection, final Consumer<T> convertFunc) {
		return toTree(collection, null, convertFunc);
	}

	/**
	 * 构建树形结构（支持节点转换处理）
	 *
	 * @param collection   扁平节点集合
	 * @param rootNodeKey  根节点标识键值
	 * @param convertFunc  节点转换函数
	 * @return 树形结构节点列表
	 * @param <K> 节点键类型
	 * @param <T> 树节点类型
	 * @since 1.0.0
	 */
	public static <K, T extends TreeNode<K, T>> List<T> toTree(final Collection<T> collection,
															   final K rootNodeKey,
															   final Consumer<T> convertFunc) {
		if (Objects.isNull(collection) || collection.isEmpty()) {
			return Collections.emptyList();
		}
		List<T> treeNodes = collection.stream()
			.filter(node -> Objects.equals(node.getParentNodeKey(), rootNodeKey))
			.collect(Collectors.toList());
		setChildNodes(collection, treeNodes, convertFunc);
		return treeNodes;
	}

	/**
	 * 递归设置子节点关系
	 *
	 * @param collection   原始节点集合
	 * @param parentNodes  当前层父节点集合
	 * @param convertFunc  节点转换处理函数
	 * @param <K> 节点键类型
	 * @param <T> 树节点类型
	 * @since 1.0.0
	 */
	protected static <K, T extends TreeNode<K, T>> void setChildNodes(final Collection<T> collection,
																	  final Collection<T> parentNodes,
																	  final Consumer<T> convertFunc) {
		if (Objects.nonNull(parentNodes) && !parentNodes.isEmpty()) {
			parentNodes.forEach(parentNode -> {
				List<T> childNodes = collection.stream()
					.filter(node -> Objects.equals(node.getParentNodeKey(), parentNode.getNodeKey()))
					.collect(Collectors.toList());
				parentNode.setChildNodes(childNodes);
				if (Objects.nonNull(convertFunc)) {
					convertFunc.accept(parentNode);
				}
				setChildNodes(collection, childNodes, convertFunc);
			});
		}
	}
}
