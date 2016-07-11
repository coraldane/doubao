package com.liuyun.doubao.config.redis;

import com.liuyun.doubao.bean.RedisConfig;
import com.liuyun.doubao.config.InputConfig;

public class RedisInputConfig extends RedisConfig implements InputConfig {
	
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
}
