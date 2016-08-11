package com.liuyun.doubao.config.redis;

import com.liuyun.doubao.common.Identified;
import com.liuyun.doubao.config.OutputConfig;

@Identified(name="redis")
public class RedisOutputConfig extends RedisConfig implements OutputConfig {

	private static final int BATCH_SIZE_DEFAULT = 100;

	private String key;
	private int batch_size = BATCH_SIZE_DEFAULT;
	
	/** one of ["none", "gzip"] */
	private String compression_type = "none";
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public int getBatch_size() {
		return batch_size;
	}
	public void setBatch_size(int batch_size) {
		this.batch_size = batch_size;
	}
	public String getCompression_type() {
		return compression_type;
	}
	public void setCompression_type(String compression_type) {
		this.compression_type = compression_type;
	}
	
}
