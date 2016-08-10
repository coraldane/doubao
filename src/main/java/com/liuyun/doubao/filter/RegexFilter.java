package com.liuyun.doubao.filter;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.liuyun.doubao.config.filter.DefaultFilterConfig;
import com.liuyun.doubao.config.filter.RegexFilterConfig;

public class RegexFilter extends DefaultFilter {
	
	private RegexFilterConfig filterConfig = new RegexFilterConfig();

	@Override
	public boolean doFilter(JSONObject data) throws Exception {
		Map<String, Object> patternMap = this.filterConfig.getPattern();
		for(String key: patternMap.keySet()){
			String patternValue = String.valueOf(patternMap.get(key));
			Object realValue = data.get(key);
			if(null == realValue){
				return false;
			}
			if(realValue instanceof String){
				if(!String.valueOf(realValue).matches(patternValue)){
					return false;
				}
			} else {
				if(!patternValue.equals(String.valueOf(realValue))){
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void setFilterConfig(DefaultFilterConfig filterConfig) {
		if(filterConfig instanceof RegexFilterConfig){
			this.filterConfig = (RegexFilterConfig)filterConfig;
		}
	}

	@Override
	public DefaultFilterConfig getFilterConfig() {
		return this.filterConfig;
	}

}
