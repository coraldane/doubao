package com.liuyun.doubao.config.std;

import com.liuyun.doubao.config.OutputConfig;

public class StdOutputConfig implements OutputConfig {

	private static final int BATCH_SIZE_DEFAULT = 10;

	private int batchSize = BATCH_SIZE_DEFAULT;
	
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
