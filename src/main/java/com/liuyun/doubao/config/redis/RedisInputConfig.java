package com.liuyun.doubao.config.redis;

import java.util.Map;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Maps;
import com.liuyun.doubao.common.Identified;
import com.liuyun.doubao.config.InputConfig;

@Identified(name="redis")
public class RedisInputConfig extends RedisConfig implements InputConfig {
	
	@JSONField(name="add_field")
	private Map<String, Object> addedFieldMap = Maps.newConcurrentMap();
	
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private static final int BATCH_SIZE_DEFAULT = 100;

	private int batchSize = BATCH_SIZE_DEFAULT;
	private String key;
	
	public int getBatchSize() {
		return batchSize;
	}
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}

	public Map<String, Object> getAddedFieldMap() {
		return addedFieldMap;
	}

	public void setAddedFieldMap(Map<String, Object> addedFieldMap) {
		this.addedFieldMap = addedFieldMap;
	}

}
