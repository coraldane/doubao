package com.liuyun.doubao.config.std;

import com.liuyun.doubao.common.Identified;
import com.liuyun.doubao.config.OutputConfig;

@Identified(name="stdout")
public class StdOutputConfig implements OutputConfig {

	private static final int BATCH_SIZE_DEFAULT = 10;

	private int batch_size = BATCH_SIZE_DEFAULT;

	public int getBatch_size() {
		return batch_size;
	}

	public void setBatch_size(int batch_size) {
		this.batch_size = batch_size;
	}
	
}
