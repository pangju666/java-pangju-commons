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

package io.github.pangju666.commons.validation.enums;

/**
 * 电话号码类型枚举
 * <p>定义常用的电话号码校验类型</p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public enum PhoneNumberType {
	/**
	 * 手机号码类型（仅支持中国大陆手机号格式校验）
	 *
	 * @since 1.0.0
	 */
	MOBILE,
	/**
	 * 固定电话号码类型（包含区号、分机号等格式校验）
	 *
	 * @since 1.0.0
	 */
	TEL,
	/**
	 * 混合类型（同时支持手机号和固定电话格式校验）
	 *
	 * @since 1.0.0
	 */
	MIX
}