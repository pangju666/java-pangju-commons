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
