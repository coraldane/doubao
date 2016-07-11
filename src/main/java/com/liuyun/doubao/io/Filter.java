package com.liuyun.doubao.io;

import com.alibaba.fastjson.JSONObject;
import com.liuyun.doubao.config.FilterConfig;
import com.liuyun.doubao.extension.SPI;

@SPI
public interface Filter extends Closable {
	
	void init(FilterConfig FilterConfig);

	boolean doMatch(JSONObject data);
	
}
