package io.github.pangju666.commons.compress.function;

import org.apache.commons.compress.archivers.ArchiveEntry;

import java.io.IOException;
import java.io.InputStream;

/**
 * 压缩条目提取器函数式接口。
 * <p>用于从压缩条目中提取输入流，以便进行解压操作。</p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 使用 SevenZFile 作为提取器
 * try (SevenZFile zf = SevenZFile.builder().setFile(file).get()) {
 *     ArchiveUtils.extract(zf.getEntries().iterator(), outputDir, zf::getInputStream);
 * }
 * }</pre>
 *
 * @param <T> 压缩条目类型，必须继承自 {@link ArchiveEntry}
 * @since 1.1.0
 */
@FunctionalInterface
public interface ArchiveEntryExtractor<T extends ArchiveEntry> {
	/**
	 * 从压缩条目提取输入流。
	 *
	 * @param entry 压缩条目对象
	 * @return 条目内容的输入流
	 * @throws IOException 当提取输入流失败时抛出
	 * @since 1.1.0
	 */
	InputStream extractor(T entry) throws IOException;
}
