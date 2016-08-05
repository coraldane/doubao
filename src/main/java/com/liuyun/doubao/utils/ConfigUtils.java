package com.liuyun.doubao.utils;

import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.liuyun.doubao.config.DoubaoConfig;
import com.liuyun.doubao.config.parser.FilterConfigParser;
import com.liuyun.doubao.config.parser.InputConfigParser;
import com.liuyun.doubao.config.parser.OutputConfigParser;

public class ConfigUtils {
	private static Logger logger = LoggerFactory.getLogger(ConfigUtils.class);
	
	private static volatile DoubaoConfig config = null;
	
	private static void init(String configFile) {
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(configFile);
			if (!fileReader.ready()) {
				logger.error("The Configuration file is not found:" + configFile);
				return;
			}
			String strJson = IOUtils.toString(fileReader);
			JSONObject configJson = JSON.parseObject(strJson, Feature.OrderedField);
			parseConfig(configJson);
		} catch (Exception e) {
			logger.error("read config file error.", e);
			throw new RuntimeException(e);
		} finally {
			if(null != fileReader){
				try {
					fileReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void parseConfig(JSONObject configJson){
		config = new DoubaoConfig();
		String[] keys = new String[]{"input", "filter", "output"};
		for(String key: keys){
			if(!configJson.containsKey(key)){
				continue;
			}
			if("input".equals(key)){
				JSONObject inputJson = configJson.getJSONObject(key);
				config.setInput(InputConfigParser.doParse(inputJson));
			} else if("output".equals(key)){
				JSONObject outputJson = configJson.getJSONObject(key);
				config.setOutputs(OutputConfigParser.doParse(outputJson));
			} else if("filter".equals(key)){
				JSONArray filterJson = configJson.getJSONArray(key);
				config.setFilters(FilterConfigParser.doParse(filterJson));
			}
		}
	}
	
	public static DoubaoConfig getConfig(String configFile){
		if(null == config){
			init(configFile);
		}
		return config;
	}

}
