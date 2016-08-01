package com.liuyun.doubao.config.parser;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.liuyun.doubao.config.InputConfig;
import com.liuyun.doubao.extension.ExtensionLoader;

public class InputConfigParser {
	private static Logger logger = LoggerFactory.getLogger(InputConfigParser.class);
	
	private static final ExtensionLoader<InputConfig> loader = ExtensionLoader.getExtensionLoader(InputConfig.class);
	
	public static InputConfig doParse(JSONObject inputJson){
		if(1 < inputJson.keySet().size()){
			logger.warn("can't use more than one input source.");
			return null;
		}
		Set<String> configExtensionNames = loader.getSupportedExtensions();
		for(String key: inputJson.keySet()){
			if(!configExtensionNames.contains(key)){
				continue;
			}
			String strJson = inputJson.getString(key);
			InputConfig inputConfig = JSON.parseObject(strJson, loader.getExtension(key).getClass());
			return inputConfig;
		}
		return null;
	}
	
}
