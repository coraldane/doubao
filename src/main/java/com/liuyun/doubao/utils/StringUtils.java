package com.liuyun.doubao.utils;

import java.lang.reflect.Array;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;

/**
 * 
 * @Date: Sep 20, 2014 9:41:35 AM<br>
 * @Copyright (c) 2014 udai.com <br>
 *            *
 * @since 1.0
 * @author tianlg
 */
public class StringUtils extends org.apache.commons.lang.StringUtils {

	public static final String CHARSET_UTF8 = "UTF-8";

	public static boolean isEmpty(Long value) {
		return null == value || 0 == value;
	}

	/**
	 * 判断一个字符串是否为整型数字
	 * 
	 * @param value
	 * @return
	 * @author coraldane
	 */
	public static boolean isInteger(String value) {
		if (StringUtils.isEmpty(value)) {
			return false;
		}
		Pattern pattern = Pattern.compile("[+|-]?[0-9]+");
		return pattern.matcher(value).matches();
	}
	
	/**
	 * 判断是否为手机号
	 * @param value
	 * @return
	 */
	public static boolean isMobile(String value){
		if (StringUtils.isEmpty(value)) {
			return false;
		}
		Pattern pattern = Pattern.compile("^0?1(([3578][0-9]{1})|(59)){1}[0-9]{8}$");
		return pattern.matcher(value).matches();
	}

	/**
	 * Trim leading and trailing whitespace from the given String.
	 * 
	 * @param str
	 *            the String to check
	 * @return the trimmed String
	 * @see java.lang.Character#isWhitespace
	 */
	public static String trimWhitespace(String str) {
		if (null == str || "".equals(str)) {
			return str;
		}
		StringBuffer buf = new StringBuffer(str);
		while (buf.length() > 0 && Character.isWhitespace(buf.charAt(0))) {
			buf.deleteCharAt(0);
		}
		while (buf.length() > 0 && Character.isWhitespace(buf.charAt(buf.length() - 1))) {
			buf.deleteCharAt(buf.length() - 1);
		}
		return buf.toString();
	}

	/**
	 * 判断字符串是否符合邮件地址的格式
	 * 
	 * @param value
	 * @return
	 * @author tianlg
	 */
	public static boolean isEmail(String value) {
		String[] array;
		Pattern pattern = Pattern.compile("[\\w[.-]]+");
		if (value.indexOf("@") < 0) {
			return false;
		}

		if (value.contains("..")) {
			return false;
		}

		array = value.split("@");
		if (array.length != 2) {
			return false;
		}

		for (String name : array) {
			name = array[0];
			if (!pattern.matcher(name).matches()) {
				return false;
			}
			if (name.indexOf(".") == 0 || name.endsWith(".")) {
				return false;
			}
		}

		String name = array[1];
		if (!name.contains(".")) {
			return false;
		}

		int suffixLength = name.length() - name.lastIndexOf(".");
		if (suffixLength != 4 && suffixLength != 3) {
			return false;
		}
		return true;
	}

	/**
	 * 判断是否为数字型字符串
	 * 
	 * @param value
	 * @return
	 * @author tianlg
	 */
	public static boolean isNumeric(String value) {
		Pattern pattern = Pattern.compile("[+|-]?[0-9]+(.[0-9]+)?");
		return pattern.matcher(value).matches();
	}

	/**
	 * 将对象toString().欢迎扩展
	 * 
	 * @param o
	 * @return
	 * @date 2011-11-2 下午12:23:43
	 * @author jiaxiao
	 */
	public static String toString(Object o) {
		return toString(o, ",");
	}

	public static String toString(Object o, String spliter) {
		if (null == o) {
			return "";
		} else {
			if (o instanceof Integer || o instanceof Double || o instanceof Boolean || o instanceof Float
					|| o instanceof Long || o instanceof Short || o instanceof StringBuffer
					|| o instanceof StringBuilder) {
				return o.toString();
			} else if (o.getClass().isArray()) {
				StringBuffer buffer = new StringBuffer();
				int len = Array.getLength(o);
				for (int index = 0; index < len; index++) {
					buffer.append(Array.get(o, index));
					if (index < len - 1) {
						buffer.append(spliter);
					}
				}
				return buffer.toString();
			} else if (o instanceof Collection) {
				StringBuffer buffer = new StringBuffer();
				int len = CollectionUtils.size(o);
				for (int index = 0; index < len; index++) {
					buffer.append(CollectionUtils.get(o, index));
					if (index < len - 1) {
						buffer.append(spliter);
					}
				}
				return buffer.toString();
			} else {
				return o.toString();
			}
		}
	}

	/**
	 * 百分比格式化
	 * 
	 * @param number
	 * @return
	 */
	public static String formatPercent(double number) {
		NumberFormat percentFormat = NumberFormat.getPercentInstance();
		percentFormat.setMaximumFractionDigits(2);
		return percentFormat.format(number);
	}

	public static String getUUID() {
		String uuid = UUID.randomUUID().toString();
		return uuid.replaceAll("-", "");
	}

	public static boolean isLongValid(Long id) {
		if (id == null || id == 0) {
			return false;
		}
		return true;
	}

	public static boolean contains(String[] array, String dest) {
		for (String strValue : array) {
			if (strValue.equals(dest)) {
				return true;
			}
		}
		return false;
	}

	public static List<Long> split4LongList(String str) {
		return split4LongList(str, ",");
	}

	public static List<Long> split4LongList(String str, String spliter) {
		if (isEmail(str)) {
			return Collections.emptyList();
		}
		String[] ss = str.split(spliter);
		List<Long> list = new ArrayList<Long>();
		for (String s : ss) {
			list.add(Long.parseLong(s));
		}
		return list;
	}

	public static boolean isAllChinese(String str) {
		char start='\u4e00';
		char end = '\u9fa5';
		char[] arr = str.toCharArray();
		for(int i=0;i<arr.length;i++){
			if(arr[i] < start || arr[i]> end){
				return false;
			}
		}
		return true;
	}
}
