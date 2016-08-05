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
	
	public static List<List<FilterConfig>> doParse(JSONArray filterJson){
		List<List<FilterConfig>> retList = Lists.newArrayList();
		Set<String> configExtensionNames = loader.getSupportedExtensions();
		for(int index=0; index < filterJson.size(); index++){
			JSONObject json = filterJson.getJSONObject(index);
			List<FilterConfig> filterConfigList = Lists.newArrayList();
			for(String key: json.keySet()){
				if(!configExtensionNames.contains(key)){
					continue;
				}
				String strJson = json.getString(key);
				FilterConfig filterConfig = JSON.parseObject(strJson, loader.getExtension(key).getClass());
				filterConfigList.add(filterConfig);
			}
			if(!filterConfigList.isEmpty()){
				retList.add(filterConfigList);
			}
		}
		return retList;
	}
	
}
