package com.liuyun.doubao.config;

import java.util.Map;

import com.google.common.collect.Maps;

public abstract class AbstractInputConfig implements InputConfig {
	
	private Map<String, Object> add_field = Maps.newConcurrentMap();
	
	@Override
	public void setAdd_field(Map<String, Object> fieldsMap) {
		if(null != fieldsMap){
			this.add_field = fieldsMap;
		}
	}

	@Override
	public Map<String, Object> getAdd_field() {
		return this.add_field;
	}

}
