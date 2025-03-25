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
 * 数据脱敏处理工具类，符合阿里脱敏规则
 * <p>提供常见敏感数据的脱敏处理方法集合，包括但不限于：
 * <ul>
 *  <li>身份证/军官证/护照号脱敏</li>
 *  <li>社保卡/医疗卡号脱敏</li>
 *  <li>手机/固话号码脱敏</li>
 *  <li>邮箱/地址脱敏</li>
 *  <li>车辆信息脱敏</li>
 *  <li>银行卡/密码脱敏等</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class DesensitizationUtils {
	protected DesensitizationUtils() {
	}

	/**
	 * 身份证号脱敏处理（保留前1位，后1位）
	 *
	 * @param idCard 原始身份证号码
	 * @return 脱敏后的身份证号（如：1***************1），脱敏失败则返回输入参数
	 * @since 1.0.0
	 */
	public static String hideIdCardNumber(final String idCard) {
		return hideRound(idCard, 1, 1);
	}

	/**
	 * 军官证号脱敏处理（保留前1位，后1位）
	 *
	 * @param militaryIdNumber 原始军官证号码
	 * @return 脱敏后的军官证号（如：军*************1），脱敏失败则返回输入参数
	 * @since 1.0.0
	 */
	public static String hideMilitaryIdNumber(final String militaryIdNumber) {
		return hideRound(militaryIdNumber, 1, 1);
	}

	/**
	 * 护照号脱敏处理（保留前1位，后1位）
	 *
	 * @param passportNumber 原始护照号码
	 * @return 脱敏后的护照号（如：E***************1），脱敏失败则返回输入参数
	 * @since 1.0.0
	 */
	public static String hidePassportNumber(final String passportNumber) {
		return hideRound(passportNumber, 1, 1);
	}

	/**
	 * 社保卡号脱敏处理（动态保留首尾各1/3长度，长度不能被3整除的，首部长度+1）
	 *
	 * @param socialSecurityCardNumber 原始社保卡号码
	 * @return 脱敏后的社保卡号（如：123******789），脱敏失败则返回输入参数
	 * @since 1.0.0
	 */
	public static String hideSocialSecurityCardNumber(final String socialSecurityCardNumber) {
		int length = StringUtils.length(socialSecurityCardNumber);
		int prefixLength = length / 3;
		int suffixLength = prefixLength;
		if (length % 3 != 0) {
			++prefixLength;
		}
		return hideRound(socialSecurityCardNumber, prefixLength, suffixLength);
	}

	/**
	 * 医保卡号脱敏处理（动态保留首尾各1/3长度，长度不能被3整除的，首部长度+1）
	 *
	 * @param medicalCardNumber 原始医疗卡号码
	 * @return 脱敏后的医疗卡号（如：C12******456），脱敏失败则返回输入参数
	 * @since 1.0.0
	 */
	public static String hideMedicalCardNumber(final String medicalCardNumber) {
		int length = StringUtils.length(medicalCardNumber);
		int prefixLength = length / 3;
		int suffixLength = prefixLength;
		if (length % 3 != 0) {
			++prefixLength;
		}
		return hideRound(medicalCardNumber, prefixLength, suffixLength);
	}

	/**
	 * 手机号脱敏处理（保留前3位，后2位）
	 *
	 * @param phoneNumber 原始手机号码
	 * @return 脱敏后的手机号（如：138******12），脱敏失败则返回输入参数
	 * @since 1.0.0
	 */
	public static String hidePhoneNumber(final String phoneNumber) {
		return hideRound(phoneNumber, 3, 2);
	}

	/**
	 * 固定电话脱敏处理（保留区号和后4位）
	 *
	 * @param telPhone 原始固定电话号码
	 * @return 脱敏后的固话号码（如：010****1234），脱敏失败则返回输入参数
	 * @since 1.0.0
	 */
	public static String hideTelPhone(final String telPhone) {
		return hideRound(telPhone, 3, 4);
	}

	/**
	 * 邮箱地址脱敏处理（保留用户名前3位，域名全保留）
	 *
	 * @param email 原始邮箱地址
	 * @return 脱敏后的邮箱（如：tes********@example.com），脱敏失败则返回输入参数
	 * @since 1.0.0
	 */
	public static String hideEmail(final String email) {
		int index = StringUtils.indexOf(email, '@');
		if (index == -1) {
			return email;
		}
		return hideRound(email, 3, email.length() - index);
	}

	/**
	 * 地址脱敏处理（隐藏区/县以下部分的地址）
	 *
	 * @param address 原始地址信息
	 * @return 脱敏后的地址（如：北京市朝阳区******），脱敏失败则返回输入参数
	 * @since 1.0.0
	 */
	public static String hideAddress(final String address) {
		if (StringUtils.isBlank(address)) {
			return address;
		}
		int index = StringUtils.lastIndexOfAny(address, "区", "县");
		if (index == -1) {
			return address;
		}
		return hideRight(address, index + 1);
	}

	/**
	 * 昵称脱敏处理（保留首尾各1位）
	 * <p>适用于普通用户昵称的脱敏展示</p>
	 *
	 * @param nickName 原始昵称
	 * @return 脱敏后的昵称（如：张*三），脱敏失败则返回输入参数
	 * @since 1.0.0
	 */
	public static String hideNickName(final String nickName) {
		return hideRound(nickName, 1, 1);
	}

	/**
	 * 中文姓名脱敏处理
	 * <p>根据姓名长度采用不同脱敏策略：
	 * <ul>
	 *  <li>3字及以下：隐藏第一个字（如：*三）</li>
	 *  <li>4-6字：显示最后两个字（如：**不败）</li>
	 *  <li>6字以上：保留首1位+尾2位（如：欧***莫非）</li>
	 * </ul>
	 *
	 * @param name 原始中文姓名
	 * @return 脱敏后的姓名，脱敏失败则返回输入参数
	 * @since 1.0.0
	 */
	public static String hideChineseName(final String name) {
		if (StringUtils.isBlank(name)) {
			return name;
		}
		int len = name.length();
		// 根据姓名长度选择脱敏策略
		if (len <= 3) {
			return hideLeft(name, len - 1);
		} else if (len <= 6) {
			return hideLeft(name, 2);
		} else {
			return hideRound(name, 1, 2);
		}
	}

	/**
	 * 车辆发动机号脱敏处理（保留前1位，后2位）
	 *
	 * @param engineNumber 原始发动机号（通常包含字母数字组合）
	 * @return 脱敏后的发动机号（如：A**12），脱敏失败则返回输入参数
	 * @since 1.0.0
	 */
	public static String hideVehicleEngineNumber(final String engineNumber) {
		return hideRound(engineNumber, 1, 2);
	}

	/**
	 * 车辆车架号脱敏处理（保留前3位，后3位）
	 * <p>适用于17位VIN码的标准脱敏</p>
	 *
	 * @param frameNumber 原始车架号
	 * @return 脱敏后的车架号（如：ABC***XYZ），脱敏失败则返回输入参数
	 * @since 1.0.0
	 */
	public static String hideVehicleFrameNumber(final String frameNumber) {
		return hideRound(frameNumber, 3, 3);
	}

	/**
	 * 车牌号脱敏处理（保留前2位，后3位）
	 * <p>同时支持新能源车牌和普通车牌格式</p>
	 *
	 * @param plateNumber 原始车牌号
	 * @return 脱敏后的车牌号（如：京A***189），脱敏失败则返回输入参数
	 * @since 1.0.0
	 */
	public static String hidePlateNumber(final String plateNumber) {
		return hideRound(plateNumber, 2, 3);
	}

	/**
	 * 银行卡号脱敏处理（保留前后各4位）
	 *
	 * @param bankCard 原始银行卡号
	 * @return 脱敏后的卡号（如：6228******8888），脱敏失败则返回输入参数
	 * @since 1.0.0
	 */
	public static String hideBankCard(final String bankCard) {
		return hideRound(bankCard, 4, 4);
	}

	/**
	 * 密码脱敏处理（全部替换为星号）
	 *
	 * @param password 原始密码
	 * @return 全星号字符串（如：********），脱敏失败则返回空字符串
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
	 * 右侧内容脱敏（保留左侧指定位数）
	 *
	 * @param content      待脱敏的原始字符串，允许为空
	 * @param prefixLength 保留的左侧字符数（需满足 0 ≤ prefixLength < 总长度）
	 * @return 右侧用星号填充的字符串（如：ABC****），
	 * 当参数非法时直接返回原始输入内容，
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
	 * @param content      待脱敏的原始字符串，允许为空
	 * @param suffixLength 保留的右侧字符数（需满足 0 ≤ suffixLength < 总长度）
	 * @return 左侧用星号填充的字符串（如：****XYZ），
	 * 当参数非法时直接返回原始输入内容，
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
	 * 环形脱敏处理（保留字符串首尾指定长度内容，中间用星号替代）
	 *
	 * @param content      待脱敏的原始字符串，允许为空
	 * @param prefixLength 头部保留字符数（需满足 0 ≤ prefixLength < 总长度）
	 * @param suffixLength 尾部保留字符数（需满足 0 ≤ suffixLength < 总长度）
	 * @return 符合首尾保留规则的脱敏字符串（格式示例："AB***EF"），
	 * 当参数非法时直接返回原始输入内容，
	 * 当首尾保留长度之和大于等于字符串总长度时返回原始输入内容
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
		if (prefixLength + suffixLength >= content.length()) {
			return content;
		}
		// 构造首部保留+星号+尾部保留的格式
		int length = StringUtils.length(content);
		String leftContent = StringUtils.left(content, prefixLength);
		String rightContent = StringUtils.right(content, suffixLength);
		rightContent = StringUtils.leftPad(rightContent, length - leftContent.length(), "*");
		return leftContent + rightContent;
	}
}
