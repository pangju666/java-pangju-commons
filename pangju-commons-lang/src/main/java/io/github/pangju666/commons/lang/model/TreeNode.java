package io.github.pangju666.commons.lang.model;

import java.util.Collection;

/**
 * 树形结构
 *
 * @param <K> 键值类型
 * @param <T> 数据类型
 */
public interface TreeNode<K, T> {
	/**
	 * 获取节点键值
	 *
	 * @return 节点键值
	 */
	K getNodeKey();

	/**
	 * 获取父级节点键值
	 *
	 * @return 父级节点键值
	 */
	K getParentNodeKey();

	/**
	 * 设置子级节点
	 *
	 * @param childNodes 子级节点集合
	 */
	void setChildNodes(final Collection<? extends TreeNode<K, T>> childNodes);
}
