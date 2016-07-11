package com.liuyun.doubao.io;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.liuyun.doubao.config.InputConfig;
import com.liuyun.doubao.extension.SPI;

@SPI
public interface Input extends Closable {
	
	void init(InputConfig inputConfig);
	
	List<JSONObject> read();

}
