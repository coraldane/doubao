package com.liuyun.doubao.config;

import com.alibaba.fastjson.JSONObject;

public class PluginConfig {

	private String name;
	
	private JSONObject params = new JSONObject();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public JSONObject getParams() {
		return params;
	}

	public void setParams(JSONObject params) {
		this.params = params;
	}
	
}
