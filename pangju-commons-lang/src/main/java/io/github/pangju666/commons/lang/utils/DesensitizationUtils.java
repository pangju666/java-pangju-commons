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

package io.github.pangju666.commons.lang.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * 数据脱敏处理工具类
 *
 * <p>提供常见敏感数据的脱敏处理方法集合，包括但不限于：
 * <ul>
 *  <li>手机号/电话号码脱敏</li>
 *  <li>身份证号/护照号脱敏</li>
 *  <li>银行卡号脱敏</li>
 *  <li>邮箱地址脱敏</li>
 *  <li>住址脱敏等</li>
 * </ul>
 * </p>
 *
 * <p>
 *     借鉴自：<a href="https://github.com/chinabugotech/hutool/blob/5.8.36/hutool-core/src/main/java/cn/hutool/core/util/DesensitizedUtil.java">cn.hutool.core.util.DesensitizedUtil 5.8.36</a>
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class DesensitizationUtils {
	protected static final String EMAIL_REGEX = "(\\w{3})\\w+@(\\w+)";
	protected static final String GENERAL_FORMAT = "$1***********$2";

	protected DesensitizationUtils() {
	}

	/**
	 * 身份证号脱敏处理（保留前0位，后4位）
	 *
	 * @param idCard 原始身份证号码
	 * @return 脱敏后的身份证号（如：***************1234）
	 * @since 1.0.0
	 */
	public static String hideIdCard(final String idCard) {
		return hideRound(idCard, 0, 4);
	}

	/**
	 * 手机号脱敏处理（保留前3位，后4位）
	 *
	 * @param phoneNumber 原始手机号码
	 * @return 脱敏后的手机号（如：138****5678）
	 * @since 1.0.0
	 */
	public static String hidePhoneNumber(final String phoneNumber) {
		return hideRound(phoneNumber, 3, 4);
	}

	/**
	 * 邮箱脱敏处理（使用正则表达式匹配替换）
	 *
	 * @param email 原始邮箱地址
	 * @return 脱敏后的邮箱（如：t***@example.com）
	 * @since 1.0.0
	 */
	public static String hideEmail(final String email) {
		return hide(email, EMAIL_REGEX, GENERAL_FORMAT);
	}

	/**
	 * 车辆发动机号脱敏处理（保留前1位，后2位）
	 *
	 * @param engineNumber 原始发动机号
	 * @return 脱敏后的发动机号（如：A**12）
	 * @since 1.0.0
	 */
	public static String hideVehicleEngineNumber(final String engineNumber) {
		return hideRound(engineNumber, 1, 2);
	}

	/**
	 * 车辆车架号脱敏处理（保留前3位，后3位）
	 *
	 * @param frameNumber 原始车架号
	 * @return 脱敏后的车架号（如：ABC***XYZ）
	 * @since 1.0.0
	 */
	public static String hideVehicleFrameNumber(final String frameNumber) {
		return hideRound(frameNumber, 3, 3);
	}

	/**
	 * 车牌号脱敏处理（保留前2位，后3位）
	 *
	 * @param plateNumber 原始车牌号
	 * @return 脱敏后的车牌号（如：京A***89）
	 * @since 1.0.0
	 */
	public static String hidePlateNumber(final String plateNumber) {
		return hideRound(plateNumber, 2, 3);
	}

	/**
	 * 昵称脱敏处理（保留首尾各1位）
	 *
	 * @param nickName 原始昵称
	 * @return 脱敏后的昵称（如：张*三）
	 * @since 1.0.0
	 */
	public static String hideNickName(final String nickName) {
		return hideRound(nickName, 1, 1);
	}

	/**
	 * 银行卡号脱敏处理（保留前后各4位）
	 *
	 * @param bankCard 原始银行卡号
	 * @return 脱敏后的卡号（如：6228******8888）
	 * @since 1.0.0
	 */
	public static String hideBankCard(final String bankCard) {
		return hideRound(bankCard, 4, 4);
	}

	/**
	 * 中文姓名脱敏处理
	 *
	 * @param name 原始中文姓名
	 * @return 脱敏后的姓名（2字姓名：*三；3字及以上：**芳）
	 * @since 1.0.0
	 */
	public static String hideChineseName(final String name) {
		if (StringUtils.isBlank(name)) {
			return name;
		}
		int len = name.length();
		// 根据姓名长度选择脱敏策略
		if (len < 3) {
			return hideLeft(name, 1);
		} else {
			return hideLeft(name, 2);
		}
	}

	/**
	 * 密码脱敏处理（全部替换为星号）
	 *
	 * @param password 原始密码
	 * @return 全星号字符串（如：********）
	 * @since 1.0.0
	 */
	public static String hidePassword(final String password) {
		if (StringUtils.isBlank(password)) {
			return StringUtils.EMPTY;
		}
		// 创建等长的星号字符串
		char[] chs = new char[password.length()];
		Arrays.fill(chs, '*');
		return StringUtils.valueOf(chs);
	}

	/**
	 * 地址脱敏处理
	 *
	 * @param address 原始地址
	 * @return 脱敏后的地址（长地址保留前6位，短地址保留前半部分）
	 * @since 1.0.0
	 */
	public static String hideAddress(final String address) {
		if (StringUtils.isBlank(address)) {
			return address;
		}
		// 根据地址长度选择脱敏策略
		if (address.length() >= 12) {
			return hideRight(address, 6);
		}
		return hideRight(address, address.length() / 2 - 1);
	}

	/**
	 * 固定电话脱敏处理
	 *
	 * @param telPhone 原始电话号码（要求包含区号分隔符）
	 * @return 脱敏后的号码（如：010-****1234）
	 * @since 1.0.0
	 */
	public static String hideTelPhone(final String telPhone) {
		if (StringUtils.isBlank(telPhone)) {
			return telPhone;
		}
		// 分割区号和号码部分处理
		String[] temp = telPhone.split("-");
		temp[1] = hideLeft(temp[1], 4);
		return temp[0] + "-" + temp[1];
	}

	/**
	 * 通用正则替换方法
	 *
	 * @param content 原始内容
	 * @param regex 匹配模式正则表达式
	 * @param format 替换格式
	 * @return 正则替换后的字符串
	 * @since 1.0.0
	 */
	public static String hide(final String content, final String regex, final String format) {
		if (StringUtils.isBlank(content)) {
			return content;
		}
		return content.replaceAll(regex, format);
	}

	/**
	 * 右侧内容脱敏（保留左侧指定位数）
	 *
	 * @param content 原始内容
	 * @param prefixLength 保留的左侧字符数
	 * @return 右侧用星号填充的字符串（如：ABC****）
	 * @since 1.0.0
	 */
	public static String hideRight(final String content, final int prefixLength) {
		if (StringUtils.isBlank(content)) {
			return content;
		}
		if (prefixLength < 0 || prefixLength >= content.length()) {
			return content;
		}
		// 构造左侧保留+右侧星号的字符串
		String hideContent = StringUtils.left(content, prefixLength);
		return StringUtils.rightPad(hideContent, StringUtils.length(content), "*");
	}

	/**
	 * 左侧内容脱敏（保留右侧指定位数）
	 *
	 * @param content 原始内容
	 * @param suffixLength 保留的右侧字符数
	 * @return 左侧用星号填充的字符串（如：****XYZ）
	 * @since 1.0.0
	 */
	public static String hideLeft(final String content, final int suffixLength) {
		if (StringUtils.isBlank(content)) {
			return content;
		}
		if (suffixLength < 0 || suffixLength >= content.length()) {
			return content;
		}
		// 构造左侧星号+右侧保留的字符串
		String hideContent = StringUtils.right(content, suffixLength);
		return StringUtils.leftPad(hideContent, StringUtils.length(content), "*");
	}

	/**
	 * 环形脱敏（同时保留首尾部分）
	 *
	 * @param content 原始内容
	 * @param prefixLength 头部保留长度
	 * @param suffixLength 尾部保留长度
	 * @return 首尾保留+中间星号的格式（如：AB***EF）
	 * @since 1.0.0
	 */
	public static String hideRound(final String content, int prefixLength, int suffixLength) {
		if (StringUtils.isBlank(content)) {
			return content;
		}
		// 参数有效性校验
		if (prefixLength < 0 || prefixLength >= content.length()) {
			return content;
		}
		if (suffixLength < 0 || suffixLength >= content.length()) {
			return content;
		}
		// 保证首尾保留长度合理性
		if (prefixLength > suffixLength) {
			prefixLength = suffixLength;
		}
		// 构造首部保留+星号+尾部保留的格式
		int length = StringUtils.length(content);
		String leftContent = StringUtils.left(content, prefixLength);
		String rightContent = StringUtils.right(content, suffixLength);
		rightContent = StringUtils.leftPad(rightContent, length, "*");
		rightContent = StringUtils.removeStart(rightContent, "***");
		return leftContent.concat(rightContent);
	}
}
