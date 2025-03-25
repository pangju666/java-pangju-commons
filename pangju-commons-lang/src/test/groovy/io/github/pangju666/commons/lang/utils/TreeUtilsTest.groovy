package io.github.pangju666.commons.lang.utils

import io.github.pangju666.commons.lang.model.TreeNode
import spock.lang.Specification
import spock.lang.Unroll

class TreeUtilsTest extends Specification {
	static class TestNode implements TreeNode<Long, TestNode> {
		String name
		Long id
		Long parentId
		Collection<TestNode> childNodes = []

		TestNode(Long id, Long parentId, String name) {
			this.id = id
			this.parentId = parentId
			this.name = name
		}

		@Override
		Long getNodeKey() {
			return id
		}

		@Override
		Long getParentNodeKey() {
			return parentId
		}

		@Override
		void setChildNodes(Collection<TestNode> childNodes) {
			this.childNodes = childNodes
		}
	}

	@Unroll
	def "构建树形结构测试 - 场景: #scenario"() {
		when:
		def result = TreeUtils.toTree(input, rootKey, converter)

		then:
		verifyResult(result, expectedStructure)

		where:
		scenario         | input                                      | rootKey | converter                 | expectedStructure
		// 基础功能测试
		"多根节点树"     | [
			new TestNode(1L, null, "根节点1"),
			new TestNode(2L, null, "根节点2"),
			new TestNode(3L, 1L, "子节点1-1"),
			new TestNode(4L, 1L, "子节点1-2"),
			new TestNode(5L, 2L, "子节点2-1"),
			new TestNode(6L, 3L, "孙子节点1-1-1"),
			new TestNode(7L, 99L, "孤立节点") // 不存在的父节点
		]                                                             | null    | null                      | [[id: 1, children: [[id: 3, children: [[id: 6]]], [id: 4]]], [id: 2, children: [[id: 5]]]]
		"单根节点树"     | [
			new TestNode(1L, null, "根节点1"),
			new TestNode(2L, null, "根节点2"),
			new TestNode(3L, 1L, "子节点1-1"),
			new TestNode(4L, 1L, "子节点1-2"),
			new TestNode(5L, 2L, "子节点2-1"),
			new TestNode(6L, 3L, "孙子节点1-1-1"),
			new TestNode(7L, 99L, "孤立节点") // 不存在的父节点
		]                                                             | 1L      | null                      | [[id: 3, children: [[id: 6]]], [id: 4]]
		"空集合输入"     | []                                         | null    | null                      | []
		// 转换函数测试
		"带节点转换处理" | [
			new TestNode(1L, null, "根节点1"),
			new TestNode(2L, null, "根节点2"),
			new TestNode(3L, 1L, "子节点1-1"),
			new TestNode(4L, 1L, "子节点1-2"),
			new TestNode(5L, 2L, "子节点2-1"),
			new TestNode(6L, 3L, "孙子节点1-1-1"),
			new TestNode(7L, 99L, "孤立节点") // 不存在的父节点
		]                                                             | null    | { it.name += "[已处理]" } | [[id: 1, name: "根节点1[已处理]", children: [[id: 3, name: "子节点1-1[已处理]"]]]] // 简化的验证结构
		// 边界条件测试
		"孤立节点处理"   | [[
								new TestNode(1L, null, "根节点1"),
								new TestNode(2L, null, "根节点2"),
								new TestNode(3L, 1L, "子节点1-1"),
								new TestNode(4L, 1L, "子节点1-2"),
								new TestNode(5L, 2L, "子节点2-1"),
								new TestNode(6L, 3L, "孙子节点1-1-1"),
								new TestNode(7L, 99L, "孤立节点") // 不存在的父节点
							].get(6)]                                 | null    | null                      | []
	}

	private void verifyResult(List<TestNode> actual, List<Map> expected) {
		assert actual*.getNodeKey() == expected*.id
		actual.eachWithIndex { node, index ->
			if (expected[index].containsKey("name")) {
				assert node.name == expected[index].name
			}
			if (expected[index].containsKey("children")) {
				verifyResult(node.childNodes, expected[index].children)
			}
		}
	}

	def "测试节点转换函数应用"() {
		given:
		def nodes = [new TestNode(1L, null, "原始节点")]
		def converter = { it.name = "转换后的节点" }

		when:
		def result = TreeUtils.toTree(nodes, null, converter)

		then:
		result[0].name == "转换后的节点"
	}

	def "测试多层级嵌套结构"() {
		given:
		def nodes = [
			new TestNode(1L, null, "根节点"),
			new TestNode(2L, 1L, "子节点"),
			new TestNode(3L, 2L, "孙子节点"),
			new TestNode(4L, 3L, "曾孙节点")
		]

		when:
		def result = TreeUtils.toTree(nodes, null)

		then:
		result[0].childNodes[0].childNodes[0].childNodes[0].nodeKey == 4L
	}
}
