package com.liuyun.doubao.config;

import java.util.Map;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Maps;

public abstract class AbstractInputConfig implements InputConfig {
	
	@JSONField(name="add_field")
	private Map<String, Object> addedFieldMap = Maps.newConcurrentMap();
	
	@Override
	public void setAddedFieldMap(Map<String, Object> fieldsMap) {
		if(null != fieldsMap){
			this.addedFieldMap = fieldsMap;
		}
	}

	@Override
	public Map<String, Object> getAddedFieldMap() {
		return this.addedFieldMap;
	}

}
