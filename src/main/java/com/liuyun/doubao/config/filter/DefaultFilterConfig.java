package com.liuyun.doubao.config.filter;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.liuyun.doubao.common.Identified;
import com.liuyun.doubao.config.FilterConfig;
import com.liuyun.doubao.config.PluginConfig;

@Identified(name="default")
public class DefaultFilterConfig implements FilterConfig {
	
	private boolean drop = false;
	private PluginConfig plugin = new PluginConfig();
	
	@JSONField(name="add_field")
	private Map<String, Object> add_field = Maps.newConcurrentMap();
	
	@JSONField(name="remove_fields")
	private List<String> remove_fields = Lists.newArrayList();
	
	public Map<String, Object> getAddedFieldMap() {
		return this.add_field;
	}
	
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
	
	public List<String> getRemoveFields() {
		return remove_fields;
	}

	public void setRemove_fields(List<String> remove_fields) {
		this.remove_fields = remove_fields;
	}

	public void setAdd_field(Map<String, Object> add_field) {
		if(null != add_field){
			this.add_field = add_field;
		}
	}

	public String getPluginName(){
		if(null == this.plugin){
			return null;
		}
		return this.plugin.getName();
	}
}
