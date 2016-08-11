package com.liuyun.doubao.config;

import java.util.Map;

import com.liuyun.doubao.extension.SPI;

@SPI
public interface InputConfig {

	void setAdd_field(Map<String, Object> fieldMap);
	
	Map<String, Object> getAdd_field();
	
	
}
