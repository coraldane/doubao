package com.liuyun.doubao.config.parser;

import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.liuyun.doubao.config.FilterConfig;
import com.liuyun.doubao.extension.ExtensionLoader;

public class FilterConfigParser {
	private static final ExtensionLoader<FilterConfig> loader = ExtensionLoader.getExtensionLoader(FilterConfig.class);
	
	public static List<FilterConfig> doParse(JSONArray filterJson){
		List<FilterConfig> retList = Lists.newArrayList();
		Set<String> configExtensionNames = loader.getSupportedExtensions();
		for(int index=0; index < filterJson.size(); index++){
			JSONObject json = filterJson.getJSONObject(index);
			for(String key: json.keySet()){
				if(!configExtensionNames.contains(key)){
					continue;
				}
				String strJson = json.getString(key);
				FilterConfig filterConfig = JSON.parseObject(strJson, loader.getExtension(key).getClass());
				retList.add(filterConfig);
			}
		}
		return retList;
	}
	
}
