package io.github.pangju666.commons.pdf.model;

import java.util.ArrayList;
import java.util.List;

public class PDFDirectory {
	private String name;
	private Integer pageIndex;
	private List<PDFDirectory> children = new ArrayList<>();

	public PDFDirectory() {
	}

	public PDFDirectory(String name, Integer pageIndex) {
		this.name = name;
		this.pageIndex = pageIndex;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(Integer pageIndex) {
		this.pageIndex = pageIndex;
	}

	public List<PDFDirectory> getChildren() {
		return children;
	}

	public void setChildren(List<PDFDirectory> children) {
		this.children = children;
	}
}
