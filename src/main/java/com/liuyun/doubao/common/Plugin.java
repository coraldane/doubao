package com.liuyun.doubao.common;

import com.alibaba.fastjson.JSONObject;
import com.liuyun.doubao.config.PluginConfig;
import com.liuyun.doubao.extension.SPI;
import com.liuyun.doubao.io.FilterResult;

@SPI("default")
public interface Plugin {
	
	void init(PluginConfig pluginConfig);
	
	void destroy();

	/**
	 * 
	 * @param data
	 * @return 消息是否继续传递, true 是, false 否
	 */
	FilterResult filter(JSONObject data);
	
}
