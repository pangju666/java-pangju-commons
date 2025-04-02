package io.github.pangju666.commons.poi.utils;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.data.RenderData;
import com.deepoove.poi.template.ElementTemplate;
import io.github.pangju666.commons.io.utils.IOUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XWPFTemplateUtils {
	protected XWPFTemplateUtils() {
	}

	public static XWPFTemplate compile(File templateFile) throws IOException {
		return compile(templateFile, Configure.createDefault());
	}

	public static XWPFTemplate compile(File templateFile, Configure configure) throws IOException {
		if (!XWPFDocumentUtils.isDocx(templateFile)) {
			throw new IllegalArgumentException("templateFile 不是一个docx文件");
		}
		return XWPFTemplate.compile(templateFile, configure);
	}

	public static XWPFTemplate compile(byte[] bytes) throws IOException {
		return compile(bytes, Configure.createDefault());
	}

	public static XWPFTemplate compile(byte[] bytes, Configure configure) throws IOException {
		if (!XWPFDocumentUtils.isDocx(bytes)) {
			throw new IllegalArgumentException("bytes 不是一个docx文档字节数组");
		}
		return XWPFTemplate.compile(IOUtils.toUnsynchronizedByteArrayInputStream(bytes), configure);
	}

	public static List<String> getTagNames(final XWPFTemplate template) {
		Validate.notNull(template, "template 不可为 null");

		return CollectionUtils.emptyIfNull(template.getElementTemplates())
			.stream()
			.filter(metaTemplate -> metaTemplate instanceof ElementTemplate)
			.map(metaTemplate -> ((ElementTemplate) metaTemplate).getTagName())
			.toList();
	}

	public static Map<String, RenderData> getDataModel(final XWPFTemplate template, final List<RenderData> renderDataList) {
		Validate.notNull(template, "template 不可为 null");

		if (CollectionUtils.isEmpty(renderDataList)) {
			return Collections.emptyMap();
		}

		List<String> tagNames = getTagNames(template);
		Map<String, RenderData> tagNameMap = new HashMap<>(tagNames.size());
		// 遍历占位符名称和渲染数据
		for (int i = 0; i < tagNames.size(); i++) {
			// 判断下标是否越界, 越界则退出循环
			if (i > renderDataList.size()) {
				break;
			}
			tagNameMap.put(tagNames.get(i), renderDataList.get(i));
		}
		return tagNameMap;
	}
}