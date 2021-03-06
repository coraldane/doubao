package com.liuyun.doubao.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.liuyun.doubao.common.Plugin;
import com.liuyun.doubao.config.PluginConfig;

public class DefaultPlugin implements Plugin {
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	protected PluginConfig config = null;
	
	@Override
	public void init(PluginConfig pluginConfig) {
		this.config = pluginConfig;
	}

	@Override
	public void destroy() {
		
	}

	@Override
	public JSONObject doFilter(JSONObject data) throws Exception{
		return data;
	}
	
}
