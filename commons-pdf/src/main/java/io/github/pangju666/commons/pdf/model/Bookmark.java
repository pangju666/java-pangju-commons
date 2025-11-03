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

import io.github.pangju666.commons.lang.model.TreeNode;
import io.github.pangju666.commons.lang.utils.IdUtils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * PDF文档书签模型类
 * <p>
 * 表示PDF文档中的书签节点，支持层级结构。实现{@link TreeNode}接口，提供树形结构操作能力。
 * </p>
 * <p>
 * 特性：
 * <ul>
 *   <li>自动生成唯一ID</li>
 *   <li>支持父子层级关系</li>
 *   <li>关联PDF文档页码</li>
 *   <li>支持子书签集合</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class Bookmark implements TreeNode<String, Bookmark> {
	/**
	 * 父书签ID
	 * <p>
	 * 表示当前书签的父节点ID，为null时表示是顶层书签
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private String parentId;
	/**
	 * 书签唯一标识
	 * <p>
	 * 自动生成的UUID，用于唯一标识书签节点
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private String id;
	/**
	 * 书签名称
	 * <p>
	 * 显示在PDF文档中的书签标题
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private String name;
	/**
	 * 关联的页码
	 * <p>
	 * 1-based页码，表示点击书签时跳转到的页面
	 * 为null时表示书签不关联具体页面
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private Integer pageNumber;
	/**
	 * 子书签集合
	 * <p>
	 * 当前书签下的所有子书签，保持层级结构
	 * </p>
	 *
	 * @since 1.0.0
	 */
	private Collection<Bookmark> children;

	/**
	 * 构造书签对象
	 *
	 * @param name 书签名称，不允许为null
	 * @param pageIndex 关联的0-based页码，允许为null表示无关联页面
	 */
	public Bookmark(String name, Integer pageIndex) {
		this.id = IdUtils.fastUUID();
		this.name = name;
		this.pageNumber = pageIndex + 1;
		this.children = new ArrayList<>();
	}

	/**
	 * 构造带父ID的书签对象
	 *
	 * @param parentId  父书签ID，允许为null表示顶层书签
	 * @param name      书签名称，不允许为null
	 * @param pageIndex 关联的0-based页码，允许为null表示无关联页面
	 */
	public Bookmark(String parentId, String name, Integer pageIndex) {
		this(name, pageIndex);
		this.parentId = parentId;
	}

	@Override
	public String getNodeKey() {
		return id;
	}

	@Override
	public String getParentNodeKey() {
		return parentId;
	}

	@Override
	public void setChildNodes(Collection<Bookmark> childNodes) {
		this.children = childNodes;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}

	public Collection<Bookmark> getChildren() {
		return children;
	}

	public void setChildren(Collection<Bookmark> children) {
		this.children = children;
	}
}