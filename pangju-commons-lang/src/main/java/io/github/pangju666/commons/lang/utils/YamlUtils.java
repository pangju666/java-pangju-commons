package io.github.pangju666.commons.lang.utils;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

public class YamlUtils {
	public static final DumperOptions DEFAULT_OPTIONS;

	static {
		DEFAULT_OPTIONS = new DumperOptions();
		DEFAULT_OPTIONS.setIndent(2);
		DEFAULT_OPTIONS.setPrettyFlow(true);
		DEFAULT_OPTIONS.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
	}

	protected YamlUtils() {
	}

	public static Map<String, Object> parseYamlAsMap(final String yamlStr) {
		Yaml yaml = new Yaml();
		return yaml.load(yamlStr);
	}

	public static Map<String, Object> parseYamlAsMap(final File yamlFile) throws FileNotFoundException {
		Yaml yaml = new Yaml();
		return yaml.load(new FileInputStream(yamlFile));
	}

	public static Map<String, Object> parseYamlAsMap(final InputStream inputStream) {
		Yaml yaml = new Yaml();
		return yaml.load(inputStream);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getValue(final String key, final Map<String, Object> yamlMap) {
		String[] keys = key.split("\\.");
		// 初始化配置映射对象
		Object value = yamlMap;
		// 遍历配置名称
		for (String s : keys) {
			// 初始化配置映射
			Map<String, Object> values = (Map<String, Object>) value;
			// 获取对应的值
			value = values.get(s);
			// 判断是否存在此项
			if (Objects.isNull(value)) {
				return null;
			}
		}
		// 返回配置项对应的值
		return (T) value;
	}

	public static <T> T getValue(final String key, final String yamlStr) {
		return getValue(key, parseYamlAsMap(yamlStr));
	}

	public static <T> T getValue(final String key, final File yamlFile) throws FileNotFoundException {
		return getValue(key, parseYamlAsMap(yamlFile));
	}

	public static <T> T getValue(final String key, final InputStream stream) {
		return getValue(key, parseYamlAsMap(stream));
	}

	public static boolean existKey(final String key, final Map<String, Object> yamlMap) {
		return getValue(key, yamlMap) != null;
	}

	public static boolean existKey(final String key, final String yamlStr) {
		return getValue(key, yamlStr) != null;
	}

	public static boolean existKey(final String key, final File yamlFile) throws FileNotFoundException {
		return getValue(key, yamlFile) != null;
	}

	public static boolean existKey(final String key, final InputStream stream) {
		return getValue(key, stream) != null;
	}

	public static String toString(final Map<String, Object> yamlMap) {
		Yaml yaml = new Yaml(DEFAULT_OPTIONS);
		return yaml.dumpAsMap(yamlMap);
	}

	public static String format(final String yamlStr) {
		Yaml yaml = new Yaml(DEFAULT_OPTIONS);
		return yaml.dumpAsMap(yaml.load(yamlStr));
	}

	public static Yaml newPrettyFlowYaml() {
		return new Yaml(DEFAULT_OPTIONS);
	}
}