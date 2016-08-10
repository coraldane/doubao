package com.liuyun.doubao.filter;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.liuyun.doubao.config.filter.ContainsFilterConfig;
import com.liuyun.doubao.config.filter.DefaultFilterConfig;

public class ContainsFilter extends DefaultFilter {
	
	private ContainsFilterConfig filterConfig = new ContainsFilterConfig();
	
	@Override
	public boolean doFilter(JSONObject data) throws Exception {
		if(StringUtils.isBlank(this.filterConfig.getField_name())){
			return false;
		}
		Object obj = data.get(this.filterConfig.getField_name());
		if(obj instanceof List){
			JSONArray jsonArray = data.getJSONArray(this.filterConfig.getField_name());
			for(int index=0; index < jsonArray.size(); index++){
				if(this.filterConfig.getContains_value().equals(jsonArray.getString(index))){
					return true;
				}
			}
		} else {
			return this.filterConfig.getContains_value().equals(obj);
		}
		return false;
	}
	
	@Override
	public void setFilterConfig(DefaultFilterConfig filterConfig) {
		if(filterConfig instanceof ContainsFilterConfig){
			this.filterConfig = (ContainsFilterConfig)filterConfig;
		}
	}

	@Override
	public DefaultFilterConfig getFilterConfig() {
		return this.filterConfig;
	}

}
