package com.liuyun.doubao.filter;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.liuyun.doubao.config.filter.DefaultFilterConfig;
import com.liuyun.doubao.config.filter.GrokFilterConfig;
import com.liuyun.doubao.ctx.Context;
import com.nflabs.grok.Grok;
import com.nflabs.grok.GrokException;
import com.nflabs.grok.Match;

public class GrokFilter extends DefaultFilter {
	
	private static final String DEFAULT_GROK_PATTERN_FILE_PATH = "conf/patterns/grok-patterns";
	
	private GrokFilterConfig filterConfig = new GrokFilterConfig();
	
	@Override
	public boolean doFilter(JSONObject data) {
		String fieldName = this.filterConfig.getMatchBean().getFieldName();
		if(StringUtils.isBlank(fieldName)){
			return false;
		}
		String strMsg = data.getString(fieldName);
		if(StringUtils.isBlank(strMsg)){
			return false;
		}
		Grok grok = new Grok();
		try {
			grok.addPatternFromFile(DEFAULT_GROK_PATTERN_FILE_PATH);
			grok.compile(this.filterConfig.getMatchBean().getPattern());
			Match gm = grok.match(strMsg);
			gm.captures();
			
			Map<String, Object> rowMap = gm.toMap();
			if(rowMap.isEmpty()){
				Context.addTag2Data(data, "_grokparsefailure");
				return false;
			} else {
				data.putAll(gm.toMap());
			}
		} catch (GrokException e) {
			logger.error("grokparsefailure", e);
		}
		return true;
	}
	
	@Override
	public void setFilterConfig(DefaultFilterConfig filterConfig) {
		if(filterConfig instanceof GrokFilterConfig){
			this.filterConfig = (GrokFilterConfig)filterConfig;
		}
	}

	@Override
	public DefaultFilterConfig getFilterConfig() {
		return this.filterConfig;
	}

}
