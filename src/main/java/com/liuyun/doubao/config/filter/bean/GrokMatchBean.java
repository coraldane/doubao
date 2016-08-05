package com.liuyun.doubao.config.filter.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class GrokMatchBean {

	@JSONField(name="field_name")
	private String fieldName;
	private String pattern;
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
}
