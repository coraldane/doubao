package com.liuyun.doubao.config.redis;

import java.util.Map;

import com.google.common.collect.Maps;
import com.liuyun.doubao.common.Identified;
import com.liuyun.doubao.config.InputConfig;

@Identified(name="redis")
public class RedisInputConfig extends RedisConfig implements InputConfig {
	
	private Map<String, Object> add_field = Maps.newConcurrentMap();
	
	private static final int BATCH_SIZE_DEFAULT = 100;

	private int batch_size = BATCH_SIZE_DEFAULT;
	private String key;
	
	/** one of ["none", "gzip"] */
	private String compression_type = "none";
	
	public int getBatch_size() {
		return batch_size;
	}
	public void setBatch_size(int batch_size) {
		this.batch_size = batch_size;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}

	public String getCompression_type() {
		return compression_type;
	}
	public void setCompression_type(String compression_type) {
		this.compression_type = compression_type;
	}
	public Map<String, Object> getAdd_field() {
		return add_field;
	}
	public void setAdd_field(Map<String, Object> add_field) {
		this.add_field = add_field;
	}

}
