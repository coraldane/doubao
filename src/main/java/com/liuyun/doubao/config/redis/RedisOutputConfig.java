package com.liuyun.doubao.config.redis;

import com.liuyun.doubao.bean.RedisConfig;
import com.liuyun.doubao.common.Identified;
import com.liuyun.doubao.config.OutputConfig;

@Identified(name="redis")
public class RedisOutputConfig extends RedisConfig implements OutputConfig {

	private static final int BATCH_SIZE_DEFAULT = 100;

	private String key;
	private int batchSize = BATCH_SIZE_DEFAULT;
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	@Override
	public int getBatchSize() {
		return batchSize;
	}
	
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
