package com.liuyun.doubao.config.parser;

import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.liuyun.doubao.config.OutputConfig;
import com.liuyun.doubao.extension.ExtensionLoader;

public class OutputConfigParser {
	private static final ExtensionLoader<OutputConfig> loader = ExtensionLoader.getExtensionLoader(OutputConfig.class);
	
	public static List<OutputConfig> doParse(JSONObject outputJson){
		List<OutputConfig> retList = Lists.newArrayList();
		Set<String> configExtensionNames = loader.getSupportedExtensions();
		for(String key: outputJson.keySet()){
			if(!configExtensionNames.contains(key)){
				continue;
			}
			String strJson = outputJson.getString(key);
			OutputConfig outputConfig = JSON.parseObject(strJson, loader.getExtension(key).getClass());
			outputConfig.setName(key);
			retList.add(outputConfig);
		}
		return retList;
	}
	
}
