package com.liuyun.doubao.plugin.resourceInfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.liuyun.doubao.config.PluginConfig;
import com.liuyun.doubao.plugin.DefaultPlugin;

public class ResourceInfoPlugin extends DefaultPlugin {
	
	@Override
	public void init(PluginConfig pluginConfig) {
		super.init(pluginConfig);
	}

	@Override
	public void destroy() {
		
	}

	@Override
	public boolean filter(JSONObject data) {
		JSONObject taskInfo = data.getJSONObject("task_info");
		if(null == taskInfo){
			return false;
		}
		JSONObject paramsObj = taskInfo.getJSONObject("params");
		if(null == paramsObj){
			return false;
		}
		JSONArray urlArray = paramsObj.getJSONArray("resource_urls");
		if(null == urlArray){
			return false;
		}
		data.put("resource_size", urlArray.size());
		
		return true;
	}
	
}
