package com.liuyun.doubao.io;

import com.alibaba.fastjson.JSONArray;
import com.liuyun.doubao.config.OutputConfig;
import com.liuyun.doubao.extension.SPI;

@SPI
public interface Output extends Closable {
	
	void init(OutputConfig outputConfig);
	
	/**
	 * 
	 * @param dataArray
	 * @return 成功的记录数
	 */
	int write(JSONArray dataArray);
}
