package com.liuyun.doubao.filter;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.liuyun.doubao.config.FilterConfig;
import com.liuyun.doubao.config.filter.MatchFilterConfig;

public class MatchFilter extends DefaultFilter {
	
	private MatchFilterConfig filterConfig = null;

	@Override
	public void init(FilterConfig filterConfig){
		super.init(filterConfig);
		if(filterConfig instanceof MatchFilterConfig){
			this.filterConfig = (MatchFilterConfig)filterConfig;
		}
	}
	
	@Override
	public boolean doFilter(JSONObject data) {
		Map<String, Object> patternMap = this.filterConfig.getPattern();
		for(String key: patternMap.keySet()){
			if(!patternMap.get(key).equals(data.get(key))){
				return false;
			}
		}
		return true;
	}

}
