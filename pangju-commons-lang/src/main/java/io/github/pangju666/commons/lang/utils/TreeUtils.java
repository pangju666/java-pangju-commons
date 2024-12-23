package io.github.pangju666.commons.lang.utils;

import io.github.pangju666.commons.lang.model.TreeNode;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class TreeUtils {
	protected TreeUtils() {
	}

	public static <K, T extends TreeNode<K, T>> List<T> toTree(final Collection<T> collection, final K rootNodeKey) {
		if (CollectionUtils.isEmpty(collection)) {
			return Collections.emptyList();
		}
		List<T> treeNodes = collection.stream()
			.filter(node -> Objects.equals(node.getParentNodeKey(), rootNodeKey))
			.toList();
		setChildNodes(collection, treeNodes, null);
		return treeNodes;
	}

	public static <K, T extends TreeNode<K, T>> List<T> toTree(final Collection<T> collection,
															   final K rootNodeKey,
															   final Consumer<T> convertFunc) {
		if (CollectionUtils.isEmpty(collection)) {
			return Collections.emptyList();
		}
		List<T> treeNodes = collection.stream()
			.filter(node -> Objects.equals(node.getParentNodeKey(), rootNodeKey))
			.toList();
		setChildNodes(collection, treeNodes, convertFunc);
		return treeNodes;
	}

	protected static <K, T extends TreeNode<K, T>> void setChildNodes(final Collection<T> collection,
																	  final Collection<T> parentNodes,
																	  final Consumer<T> convertFunc) {
		if (CollectionUtils.isNotEmpty(parentNodes)) {
			parentNodes.forEach(parentNode -> {
				List<T> childNodes = collection.stream()
					.filter(node -> Objects.equals(node.getParentNodeKey(), parentNode.getNodeKey()))
					.toList();
				parentNode.setChildNodes(childNodes);
				if (Objects.nonNull(convertFunc)) {
					convertFunc.accept(parentNode);
				}
				setChildNodes(collection, childNodes, convertFunc);
			});
		}
	}
}
