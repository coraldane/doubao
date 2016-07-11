package com.liuyun.doubao.filter;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.liuyun.doubao.common.Plugin;
import com.liuyun.doubao.config.FilterConfig;
import com.liuyun.doubao.config.filter.DefaultFilterConfig;
import com.liuyun.doubao.extension.ExtensionLoader;
import com.liuyun.doubao.io.Filter;

public abstract class DefaultFilter implements Filter {
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static final ExtensionLoader<Plugin> loader = ExtensionLoader.getExtensionLoader(Plugin.class);
	
	private DefaultFilterConfig filterConfig = null;
	
	private Plugin plugin = null;
	
	@Override
	public void init(FilterConfig filterConfig) {
		if(filterConfig instanceof DefaultFilterConfig){
			this.filterConfig = (DefaultFilterConfig)filterConfig;
		}
		
		if(StringUtils.isNotEmpty(this.filterConfig.getPluginName())){
			this.plugin = loader.getExtension(this.filterConfig.getPluginName());
			if(null == this.plugin){
				logger.error("plugin " + this.filterConfig.getPluginName() + " not found!");
			} else {
				this.plugin.init(this.filterConfig.getPlugin());
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
	public boolean doMatch(JSONObject data) {
		if(false == this.doFilter(data)){//如果不符合匹配规则，直接进入下一过滤器
			return true;
		}
		if(this.filterConfig.isDrop()){//直接丢弃
			return false;
		}
		if(StringUtils.isNotEmpty(this.filterConfig.getPluginName()) && null != this.plugin){
			return this.plugin.filter(data);
		}
		return true;
	}

	public abstract boolean doFilter(JSONObject data);
}
