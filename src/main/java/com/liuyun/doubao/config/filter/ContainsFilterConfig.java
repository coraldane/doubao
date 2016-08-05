package com.liuyun.doubao.config.filter;

import com.liuyun.doubao.common.Identified;

@Identified(name="contains")
public class ContainsFilterConfig extends DefaultFilterConfig {

	private String field_name;
	private String contains_value;
	
	public String getField_name() {
		return field_name;
	}
	public void setField_name(String field_name) {
		this.field_name = field_name;
	}
	public String getContains_value() {
		return contains_value;
	}
	public void setContains_value(String contains_value) {
		this.contains_value = contains_value;
	}
	
}
