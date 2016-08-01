package com.liuyun.doubao.config;

import java.util.Map;

import com.liuyun.doubao.extension.SPI;

@SPI
public interface InputConfig {

	void setAddedFieldMap(Map<String, Object> fieldsMap);
	
	Map<String, Object> getAddedFieldMap();
}
