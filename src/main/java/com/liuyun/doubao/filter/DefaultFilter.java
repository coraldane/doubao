package com.liuyun.doubao.filter;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.liuyun.doubao.common.Plugin;
import com.liuyun.doubao.config.FilterConfig;
import com.liuyun.doubao.config.filter.DefaultFilterConfig;
import com.liuyun.doubao.extension.ExtensionLoader;
import com.liuyun.doubao.io.Filter;
import com.liuyun.doubao.io.FilterResult;

public abstract class DefaultFilter implements Filter {
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static final ExtensionLoader<Plugin> loader = ExtensionLoader.getExtensionLoader(Plugin.class);
	
	private Plugin plugin = null;
	
	@Override
	public void init(FilterConfig filterConfig) {
		if(filterConfig instanceof DefaultFilterConfig){
			setFilterConfig((DefaultFilterConfig)filterConfig);
		}
		
		if(StringUtils.isNotEmpty(this.getFilterConfig().getPluginName())){
			this.plugin = loader.getExtension(this.getFilterConfig().getPluginName());
			if(null == this.plugin){
				logger.error("plugin " + this.getFilterConfig().getPluginName() + " not found!");
			} else {
				this.plugin.init(this.getFilterConfig().getPlugin());
			}
		}
	}

	@Override
	public void destroy() {
		if(null != this.plugin){
			this.plugin.destroy();
		}
	}

	@Override
	public FilterResult doMatch(JSONObject data) {
		if(false == this.doFilter(data)){//如果不符合匹配规则，直接进入下一过滤器
			return FilterResult.newNotMatch();
		}
		if(this.getFilterConfig().isDrop()){//直接丢弃
			return FilterResult.newDrop();
		}
		
		Map<String, Object> addedFieldMap = this.getFilterConfig().getAddedFieldMap();
		for(String field: addedFieldMap.keySet()){
			data.put(field, addedFieldMap.get(field));
		}
		
		for(String fieldName: this.getFilterConfig().getRemoveFields()){
			data.remove(fieldName);
		}
		
		if(StringUtils.isNotEmpty(this.getFilterConfig().getPluginName()) && null != this.plugin){
			return this.plugin.filter(data);
		}
		return FilterResult.newMatched(true);
	}
	
	public abstract void setFilterConfig(DefaultFilterConfig filterConfig);
	
	public abstract DefaultFilterConfig getFilterConfig();

	public abstract boolean doFilter(JSONObject data);
}
