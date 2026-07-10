/*
 *   Copyright 2026 pangju666
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

package io.github.pangju666.commons.tesseract.factory;

import io.github.pangju666.commons.io.utils.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.bytedeco.tesseract.TessBaseAPI;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * TessBaseAPI 对象池工厂
 * <p>
 * 基于 Apache Commons Pool2 的 BasePooledObjectFactory 实现，
 * 用于创建和管理 TessBaseAPI 对象池，提供 OCR 识别所需的 TessBaseAPI 实例。
 * </p>
 * <p>
 * 该工厂负责：
 * <ul>
 *   <li>创建并初始化 TessBaseAPI 实例</li>
 *   <li>包装对象以供对象池管理</li>
 *   <li>钝化对象（清理和释放资源）</li>
 *   <li>验证对象有效性</li>
 *   <li>销毁对象（释放资源）</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @since 2.1.0
 */
public class TessBaseAPIFactory extends BasePooledObjectFactory<TessBaseAPI> {
	/**
	 * Tesseract 语言数据包路径
	 * <p>指向包含 .traineddata 文件的目录</p>
	 *
	 * @since 2.1.0
	 */
	private final String dataPath;

	/**
	 * OCR 识别语言
	 * <p>语言代码，多个语言用 + 号连接，如 "chi_sim+eng"</p>
	 *
	 * @since 2.1.0
	 */
	private final String language;

	/**
	 * 使用默认配置创建 TessBaseAPI 工厂
	 * <p>
	 * 自动从类路径复制内置的语言数据包到临时目录，
	 * 包含英文(eng)、简体中文(chi_sim)和简体中文竖排(chi_sim_vert)三种语言包。
	 * 默认使用 "chi_sim+eng" 混合语言识别模式。
	 * </p>
	 *
	 * @throws IOException 当复制语言数据包文件失败时抛出
	 * @since 2.1.0
	 */
	public TessBaseAPIFactory() throws IOException {
		File dataDir = new File(FilenameUtils.separatorsToUnix(FileUtils.getTempDirectoryPath()), "tesseract_data");

		File englishFile = new File(dataDir, "eng.traineddata");
		if (!englishFile.exists() || !englishFile.isFile()) {
			try (InputStream inputStream = TessBaseAPIFactory.class.getResourceAsStream("/tesseract_data/eng.traineddata")) {
				if (Objects.nonNull(inputStream)) {
					FileUtils.copyInputStreamToFile(inputStream, englishFile);
				}
			}
		}

		File chineseDataFile = new File(dataDir, "chi_sim.traineddata");
		if (!chineseDataFile.exists() || !chineseDataFile.isFile()) {
			try (InputStream inputStream = TessBaseAPIFactory.class.getResourceAsStream("/tesseract_data/chi_sim.traineddata")) {
				if (Objects.nonNull(inputStream)) {
					FileUtils.copyInputStreamToFile(inputStream, chineseDataFile);
				}
			}
		}

		File chineseVertDataFile = new File(dataDir, "chi_sim_vert.traineddata");
		if (!chineseVertDataFile.exists() || !chineseVertDataFile.isFile()) {
			try (InputStream inputStream = TessBaseAPIFactory.class.getResourceAsStream("/tesseract_data/chi_sim_vert.traineddata")) {
				if (Objects.nonNull(inputStream)) {
					FileUtils.copyInputStreamToFile(inputStream, chineseVertDataFile);
				}
			}
		}

		this.dataPath = FilenameUtils.separatorsToUnix(dataDir.getAbsolutePath()) + "/";
		this.language = "chi_sim+eng";
	}

	/**
	 * 使用指定的数据路径和语言创建 {@link TessBaseAPI} 工厂
	 *
	 * @param dataPath Tesseract 语言数据包路径，不可为 null 或空，必须是存在的目录
	 * @param languages OCR 识别语言，不可为 null 或空
	 * @throws NullPointerException     当 dataPath 或 language 为 null 时抛出
	 * @throws IllegalArgumentException 当 dataPath 或 language 为空，或 dataPath 路径不存在/不是目录时抛出
	 * @see <a href="https://github.com/tesseract-ocr/tessdata_best">语言数据包<b>Github</b>仓库</a>
	 * @since 2.1.0
	 */
	public TessBaseAPIFactory(String dataPath, String... languages) {
		Validate.notEmpty(languages, "languages 不能为空");
		Validate.notBlank(dataPath, "dataPath 不能为空");

		File dataDir = new File(dataPath);
		Validate.isTrue(dataDir.exists(), "数据包路径不存在");
		Validate.isTrue(dataDir.isDirectory(), "数据包路径不是一个目录");

		this.dataPath = FilenameUtils.separatorsToUnix(dataPath);
		this.language = StringUtils.join(languages, "+");
	}

	/**
	 * 创建并初始化 TessBaseAPI 实例
	 * <p>创建新的 TessBaseAPI 对象并使用配置的 dataPath 和 language 进行初始化，
	 * 如果初始化失败则抛出异常。</p>
	 *
	 * @return 初始化完成的 TessBaseAPI 实例
	 * @throws IllegalArgumentException 当 Tesseract 初始化失败时抛出
	 * @since 2.1.0
	 */
	@Override
	public TessBaseAPI create() {
		TessBaseAPI tessBaseAPI = new TessBaseAPI();
		int initResult = tessBaseAPI.Init(dataPath, language);
		if (initResult != 0) {
			throw new IllegalArgumentException("Tesseract 初始化失败，请检查语言包");
		}
		return tessBaseAPI;
	}

	/**
	 * 将 TessBaseAPI 对象包装为 PooledObject
	 * <p>将原始对象包装为 {@link DefaultPooledObject}，以供对象池管理。</p>
	 *
	 * @param obj 要包装的 TessBaseAPI 对象
	 * @return 包装后的 PooledObject 对象
	 * @since 2.1.0
	 */
	@Override
	public PooledObject<TessBaseAPI> wrap(TessBaseAPI obj) {
		return new DefaultPooledObject<>(obj);
	}

	/**
	 * 钝化 TessBaseAPI 对象
	 * <p>在对象归还到池之前调用，用于清理对象状态并释放临时资源。
	 * 调用 {@link TessBaseAPI#Clear()} 清除识别数据。</p>
	 *
	 * @param object 要钝化的 PooledObject 对象
	 * @since 2.1.0
	 */
	@Override
	public void passivateObject(PooledObject<TessBaseAPI> object) {
		TessBaseAPI tessBaseAPI = object.getObject();
		tessBaseAPI.Clear();
	}

	/**
	 * 验证 TessBaseAPI 对象的有效性
	 * <p>在从池借出对象之前调用，用于检查对象是否仍然可用。
	 * 检查对象不为 null 且不是空指针。</p>
	 *
	 * @param object 要验证的 PooledObject 对象
	 * @return true 表示对象有效，false 表示对象无效
	 * @since 2.1.0
	 */
	@Override
	public boolean validateObject(PooledObject<TessBaseAPI> object) {
		TessBaseAPI tessBaseAPI = object.getObject();
		return Objects.nonNull(tessBaseAPI) && !tessBaseAPI.isNull();
	}

	/**
	 * 销毁 TessBaseAPI 对象
	 * <p>在对象从池中移除时调用，用于释放对象占用的资源。
	 * 调用 {@link TessBaseAPI#End()} 方法完全销毁 TessBaseAPI 实例。</p>
	 *
	 * @param object 要销毁的 PooledObject 对象
	 * @since 2.1.0
	 */
	@Override
	public void destroyObject(PooledObject<TessBaseAPI> object) {
		object.getObject().End();
	}
}
