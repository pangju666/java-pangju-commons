package io.github.pangju666.commons.poi.utils;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.data.DocxRenderData;
import com.deepoove.poi.data.RenderData;
import com.deepoove.poi.data.TextRenderData;
import com.deepoove.poi.template.ElementTemplate;
import com.deepoove.poi.template.MetaTemplate;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文档渲染工具类，仅支持docx格式文档
 *
 * @author 胖橘
 * @version 1.0
 * @since 1.0
 */
public class XWPFTemplateUtils {
	protected XWPFTemplateUtils() {
	}

	/**
	 * 根据模板文件合并多个文件
	 *
	 * @param outputStream 渲染结果输出流
	 * @param templateFile 模板文件
	 * @param destFiles    待合并文件
	 * @throws IOException 写入错误时触发
	 */
	public static void mergeFiles(OutputStream outputStream, File templateFile,
								  File... destFiles) throws IOException {
		XWPFTemplate template = XWPFTemplate.compile(templateFile);
		Map<String, RenderData> placeholderMap = getTagNameMap(template, getRenderDataList(destFiles));
		writeToStream(template, placeholderMap, outputStream);
	}

	/**
	 * 根据模板文件合并多个文件
	 *
	 * @param outputStream        渲染结果输出流
	 * @param templateInputStream 模板文件输入流
	 * @param destFiles           待合并文件
	 * @throws IOException 写入错误时触发
	 */
	public static void mergeFiles(OutputStream outputStream, InputStream templateInputStream,
								  File... destFiles) throws IOException {
		XWPFTemplate template = XWPFTemplate.compile(templateInputStream);
		Map<String, RenderData> placeholderMap = getTagNameMap(template, getRenderDataList(destFiles));
		writeToStream(template, placeholderMap, outputStream);
	}

	/**
	 * 根据模板文件合并多个文件
	 *
	 * @param outputStream     渲染结果输出流
	 * @param templateFilePath 模板文件路径
	 * @param destFiles        待合并文件
	 * @throws IOException 写入错误时触发
	 */
	public static void mergeFiles(OutputStream outputStream, String templateFilePath,
								  File... destFiles) throws IOException {
		XWPFTemplate template = XWPFTemplate.compile(templateFilePath);
		Map<String, RenderData> placeholderMap = getTagNameMap(template, getRenderDataList(destFiles));
		writeToStream(template, placeholderMap, outputStream);
	}

	/**
	 * 根据模板文件合并多个文件
	 *
	 * @param outputFile   渲染结果输出文件
	 * @param templateFile 模板文件
	 * @param destFiles    待合并文件
	 * @throws IOException 写入错误时触发
	 */
	public static void mergeFiles(File outputFile, File templateFile,
								  File... destFiles) throws IOException {
		XWPFTemplate template = XWPFTemplate.compile(templateFile);
		Map<String, RenderData> placeholderMap = getTagNameMap(template, getRenderDataList(destFiles));
		writeToFile(template, placeholderMap, outputFile);
	}

	/**
	 * 根据模板文件合并多个文件
	 *
	 * @param outputFile          渲染结果输出文件
	 * @param templateInputStream 模板文件输入流
	 * @param destFiles           待合并文件
	 * @throws IOException 写入错误时触发
	 */
	public static void mergeFiles(File outputFile, InputStream templateInputStream,
								  File... destFiles) throws IOException {
		XWPFTemplate template = XWPFTemplate.compile(templateInputStream);
		Map<String, RenderData> placeholderMap = getTagNameMap(template, getRenderDataList(destFiles));
		writeToFile(template, placeholderMap, outputFile);
	}

	/**
	 * 根据模板文件合并多个文件
	 *
	 * @param outputFile       渲染结果输出文件
	 * @param templateFilePath 模板文件路径
	 * @param destFiles        待合并文件
	 * @throws IOException 写入错误时触发
	 */
	public static void mergeFiles(File outputFile, String templateFilePath,
								  File... destFiles) throws IOException {
		XWPFTemplate template = XWPFTemplate.compile(templateFilePath);
		Map<String, RenderData> placeholderMap = getTagNameMap(template, getRenderDataList(destFiles));
		writeToFile(template, placeholderMap, outputFile);
	}

	/**
	 * 替换模板文件中的标签，并渲染结果至流中
	 *
	 * @param outputStream        渲染结果输出流
	 * @param templateInputStream 模板文件输入流
	 * @param tagNameMap          占位符映射
	 * @throws IOException 写入错误时触发
	 */
	public static void renderTags(OutputStream outputStream, InputStream templateInputStream,
								  Map<String, RenderData> tagNameMap) throws IOException {
		XWPFTemplate template = XWPFTemplate.compile(templateInputStream);
		writeToStream(template, tagNameMap, outputStream);
	}

	/**
	 * 替换模板文件中的标签，并渲染结果至流中
	 *
	 * @param outputStream 渲染结果输出流
	 * @param templateFile 模板文件
	 * @param tagNameMap   占位符映射
	 * @throws IOException 写入错误时触发
	 */
	public static void renderTags(OutputStream outputStream, File templateFile,
								  Map<String, RenderData> tagNameMap) throws IOException {
		XWPFTemplate template = XWPFTemplate.compile(templateFile);
		writeToStream(template, tagNameMap, outputStream);
	}

	/**
	 * 替换模板文件中的标签，并渲染结果至流中
	 *
	 * @param outputStream     渲染结果输出流
	 * @param templateFilePath 模板文件路径
	 * @param tagNameMap       占位符映射
	 * @throws IOException 写入错误时触发
	 */
	public static void renderTags(OutputStream outputStream, String templateFilePath,
								  Map<String, RenderData> tagNameMap) throws IOException {
		XWPFTemplate template = XWPFTemplate.compile(templateFilePath);
		writeToStream(template, tagNameMap, outputStream);
	}

	/**
	 * 替换模板文件中的标签，并渲染结果至文件中
	 *
	 * @param outputFile   渲染结果输出文件
	 * @param templateFile 模板文件
	 * @param tagNameMap   待渲染占位符映射
	 * @throws IOException 写入错误时触发
	 */
	public static void renderTags(File outputFile, File templateFile,
								  Map<String, RenderData> tagNameMap) throws IOException {
		XWPFTemplate template = XWPFTemplate.compile(templateFile);
		writeToFile(template, tagNameMap, outputFile);
	}

	/**
	 * 替换模板文件中的标签，并渲染结果至文件中
	 *
	 * @param outputFile          渲染结果输出文件
	 * @param templateInputStream 模板文件输入流
	 * @param tagNameMap          待渲染占位符映射
	 * @throws IOException 写入错误时触发
	 */
	public static void renderTags(File outputFile, InputStream templateInputStream,
								  Map<String, RenderData> tagNameMap) throws IOException {
		XWPFTemplate template = XWPFTemplate.compile(templateInputStream);
		writeToFile(template, tagNameMap, outputFile);
	}

	/**
	 * 替换模板文件中的标签，并渲染结果至文件中
	 *
	 * @param outputFile       渲染结果输出文件
	 * @param templateFilePath 模板文件路径
	 * @param tagNameMap       待渲染占位符映射
	 * @throws IOException 写入错误时触发
	 */
	public static void renderTags(File outputFile, String templateFilePath,
								  Map<String, RenderData> tagNameMap) throws IOException {
		XWPFTemplate template = XWPFTemplate.compile(templateFilePath);
		writeToFile(template, tagNameMap, outputFile);
	}

	/**
	 * 获取模板文件中的占位符标签名列表
	 *
	 * @param template 模板文件
	 * @return 占位符标签名列表
	 */
	public static List<String> getTagNameList(XWPFTemplate template) {
		List<MetaTemplate> metaTemplateList = template.getElementTemplates();
		List<String> tagNameList = new ArrayList<>(metaTemplateList.size());
		metaTemplateList.forEach(metaTemplate -> {
			ElementTemplate elementTemplate = (ElementTemplate) metaTemplate;
			tagNameList.add(elementTemplate.getTagName());
		});
		return tagNameList;
	}

	/**
	 * 获取docx渲染数据
	 *
	 * @param docxFile 待渲染docx文件
	 * @return docx渲染数据
	 * @see DocxRenderData
	 */
	public static DocxRenderData createRenderData(File docxFile) {
		return new DocxRenderData(docxFile);
	}

	/**
	 * 获取文本渲染数据
	 *
	 * @param text 待渲染文本数据
	 * @return 文本渲染数据
	 * @see TextRenderData
	 */
	public static TextRenderData createRenderData(String text) {
		return new TextRenderData(StringUtils.stripToEmpty(text));
	}

	private static List<RenderData> getRenderDataList(File... docxRenderFiles) {
		List<RenderData> docxRenderDataList = new ArrayList<>();
		for (File destFile : docxRenderFiles) {
			docxRenderDataList.add(createRenderData(destFile));
		}
		return docxRenderDataList;
	}

	private static Map<String, RenderData> getTagNameMap(XWPFTemplate template, List<RenderData> renderDataList) {
		// 获取文件中全部占位符
		List<String> tagNameList = getTagNameList(template);
		Map<String, RenderData> tagNameMap = new HashMap<>(tagNameList.size());
		// 遍历占位符名称和渲染数据
		for (int i = 0; i < tagNameList.size(); i++) {
			// 判断下标是否越界, 越界则退出循环
			if (i > renderDataList.size()) {
				break;
			}
			tagNameMap.put(tagNameList.get(i), renderDataList.get(i));
		}
		return tagNameMap;
	}

	private static void writeToStream(XWPFTemplate template, Map<String, RenderData> tagNameMap,
									  OutputStream outputStream) throws IOException {
		template.render(tagNameMap);
		template.write(outputStream);
		outputStream.flush();
		template.close();
	}

	private static void writeToFile(XWPFTemplate template, Map<String, RenderData> tagNameMap,
									File outputFile) throws IOException {
		template.render(tagNameMap);
		template.writeToFile(outputFile.getAbsolutePath());
		template.close();
	}
}