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

/**
 * DOCX模板工具类
 * <p>
 * 提供对DOCX模板文档的操作支持，包括：
 * <ul>
 *   <li>模板编译</li>
 *   <li>模板标签处理</li>
 *   <li>数据模型构建</li>
 * </ul>
 * 注意事项：
 * <ul>
 *   <li>基于poi-tl实现模板功能</li>
 *   <li>仅支持.docx格式模板</li>
 *   <li>所有方法均为静态方法</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class XWPFTemplateUtils {
	protected XWPFTemplateUtils() {
	}

	/**
	 * 使用默认配置编译DOCX模板
	 *
	 * @param templateFile 模板文件，不允许为null
	 * @return 编译后的模板对象
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当文件不是DOCX格式时抛出
	 * @since 1.0.0
	 */
	public static XWPFTemplate compile(File templateFile) throws IOException {
		return compile(templateFile, Configure.createDefault());
	}

	/**
	 * 使用指定配置编译DOCX模板
	 *
	 * @param templateFile 模板文件，不允许为null
	 * @param configure 模板配置，不允许为null
	 * @return 编译后的模板对象
	 * @throws IOException 当文件读取失败时抛出
	 * @throws IllegalArgumentException 当文件不是DOCX格式时抛出
	 * @since 1.0.0
	 */
	public static XWPFTemplate compile(File templateFile, Configure configure) throws IOException {
		if (!XWPFDocumentUtils.isDocx(templateFile)) {
			throw new IllegalArgumentException("templateFile 不是一个docx文件");
		}
		return XWPFTemplate.compile(templateFile, configure);
	}

	/**
	 * 使用默认配置编译DOCX模板字节数组
	 *
	 * @param bytes 模板字节数组，不允许为null或空
	 * @return 编译后的模板对象
	 * @throws IOException 当字节数组解析失败时抛出
	 * @throws IllegalArgumentException 当字节数组不是DOCX格式时抛出
	 * @since 1.0.0
	 */
	public static XWPFTemplate compile(byte[] bytes) throws IOException {
		return compile(bytes, Configure.createDefault());
	}

	/**
	 * 使用指定配置编译DOCX模板字节数组
	 *
	 * @param bytes 模板字节数组，不允许为null或空
	 * @param configure 模板配置，不允许为null
	 * @return 编译后的模板对象
	 * @throws IOException 当字节数组解析失败时抛出
	 * @throws IllegalArgumentException 当字节数组不是DOCX格式时抛出
	 * @since 1.0.0
	 */
	public static XWPFTemplate compile(byte[] bytes, Configure configure) throws IOException {
		if (!XWPFDocumentUtils.isDocx(bytes)) {
			throw new IllegalArgumentException("bytes 不是一个docx文档字节数组");
		}
		return XWPFTemplate.compile(IOUtils.toUnsynchronizedByteArrayInputStream(bytes), configure);
	}

	/**
	 * 获取模板中的所有标签名称
	 * <p>
	 * 从模板中提取所有有效的标签名称，过滤掉非ElementTemplate类型的模板元素
	 * </p>
	 *
	 * @param template 模板对象，不允许为null
	 * @return 标签名称列表，不会返回null。如果模板中没有标签，返回空列表
	 * @throws IllegalArgumentException 当template参数为null时抛出
	 * @since 1.0.0
	 */
	public static List<String> getTagNames(final XWPFTemplate template) {
		Validate.notNull(template, "template 不可为 null");

		return CollectionUtils.emptyIfNull(template.getElementTemplates())
			.stream()
			.filter(metaTemplate -> metaTemplate instanceof ElementTemplate)
			.map(metaTemplate -> ((ElementTemplate) metaTemplate).getTagName())
			.toList();
	}

	/**
	 * 构建模板数据模型
	 * <p>
	 * 将渲染数据列表与模板标签名称按顺序映射，构建可用于模板渲染的数据模型。
	 * 如果渲染数据数量少于标签数量，多余的标签将不会被映射。
	 * </p>
	 *
	 * @param template 模板对象，不允许为null
	 * @param renderDataList 渲染数据列表，可以为null或空
	 * @return 标签名与渲染数据的映射，不会返回null。如果renderDataList为null或空，返回空Map
	 * @throws IllegalArgumentException 当template参数为null时抛出
	 * @since 1.0.0
	 */
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