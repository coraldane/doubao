package com.liuyun.doubao.filter;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.liuyun.doubao.config.FilterConfig;
import com.liuyun.doubao.config.filter.RegexFilterConfig;

public class RegexFilter extends DefaultFilter {
	
	private RegexFilterConfig filterConfig = null;

	@Override
	public void init(FilterConfig filterConfig){
		super.init(filterConfig);
		if(filterConfig instanceof RegexFilterConfig){
			this.filterConfig = (RegexFilterConfig)filterConfig;
		}
	}
	
	@Override
	public boolean doFilter(JSONObject data) {
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

}
