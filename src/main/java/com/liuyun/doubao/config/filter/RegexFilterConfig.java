package com.liuyun.doubao.config.filter;

import java.util.Map;

import com.google.common.collect.Maps;

public class RegexFilterConfig extends DefaultFilterConfig {

	private Map<String, Object> pattern = Maps.newConcurrentMap();
	public Map<String, Object> getPattern() {
		return pattern;
	}
	public void setPattern(Map<String, Object> pattern) {
		this.pattern = pattern;
	}
	
}
