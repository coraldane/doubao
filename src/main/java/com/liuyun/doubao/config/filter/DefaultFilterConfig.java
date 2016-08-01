package com.liuyun.doubao.config.filter;

import com.liuyun.doubao.common.Identified;
import com.liuyun.doubao.config.FilterConfig;
import com.liuyun.doubao.config.PluginConfig;

@Identified(name="default")
public class DefaultFilterConfig implements FilterConfig {

	private boolean drop = false;
	private PluginConfig plugin = new PluginConfig();
	public boolean isDrop() {
		return drop;
	}
	public void setDrop(boolean drop) {
		this.drop = drop;
	}
	public PluginConfig getPlugin() {
		return plugin;
	}
	public void setPlugin(PluginConfig plugin) {
		this.plugin = plugin;
	}
	
	public String getPluginName(){
		if(null == this.plugin){
			return null;
		}
		return this.plugin.getName();
	}
}
