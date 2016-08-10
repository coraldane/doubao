package com.liuyun.doubao.filter;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.liuyun.doubao.config.filter.DefaultFilterConfig;
import com.liuyun.doubao.config.filter.GrokFilterConfig;
import com.liuyun.doubao.ctx.Context;

import oi.thekraken.grok.api.Grok;
import oi.thekraken.grok.api.Match;
import oi.thekraken.grok.api.exception.GrokException;

public class GrokFilter extends DefaultFilter {
	
	private static final String DEFAULT_GROK_PATTERN_FILE_PATH = "conf/patterns/grok-patterns";
	private static String[] NOT_USED_ARGS = new String[]{"BASE10NUM", "YEAR", "MONTHNUM", "MONTHDAY", "HOUR", "MINUTE", "SECOND", "ISO8601_TIMEZONE"};
	
	private GrokFilterConfig filterConfig = new GrokFilterConfig();
	private Grok grok = null;
	
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
		
		Match gm = grok.match(strMsg);
		gm.captures();
		
		Map<String, Object> rowMap = gm.toMap();
		if(rowMap.isEmpty()){
			Context.addTag2Data(data, "_grokparsefailure");
			return false;
		} else {
			for(String key: NOT_USED_ARGS){
				rowMap.remove(key);
			}
			data.putAll(rowMap);
		}
		return true;
	}
	
	@Override
	public void setFilterConfig(DefaultFilterConfig filterConfig) {
		if(filterConfig instanceof GrokFilterConfig){
			this.filterConfig = (GrokFilterConfig)filterConfig;
			
			try {
				grok = Grok.create(DEFAULT_GROK_PATTERN_FILE_PATH);
				grok.compile(this.filterConfig.getMatchBean().getPattern());
			} catch (GrokException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public DefaultFilterConfig getFilterConfig() {
		return this.filterConfig;
	}

}
