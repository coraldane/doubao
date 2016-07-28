package com.liuyun.doubao.plugin.resourceInfo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.liuyun.doubao.config.PluginConfig;
import com.liuyun.doubao.plugin.DefaultPlugin;
import com.liuyun.doubao.utils.StringUtils;

public class ResourceInfoPlugin extends DefaultPlugin {
	
	@Override
	public void init(PluginConfig pluginConfig) {
		super.init(pluginConfig);
	}

	@Override
	public void destroy() {
		
	}

	@Override
	public boolean filter(JSONObject data) {
		JSONObject taskInfo = data.getJSONObject("task_info");
		if(null == taskInfo){
			return false;
		}
		JSONObject paramsObj = taskInfo.getJSONObject("params");
		if(null == paramsObj){
			return false;
		}
		JSONArray urlArray = paramsObj.getJSONArray("resource_urls");
		if(null == urlArray){
			return false;
		}
		data.put("resource_size", urlArray.size());
		
		Map<String, Integer> domainMap = Maps.newConcurrentMap();
		for(int index=0; index < urlArray.size(); index++){
			JSONObject urlObj = urlArray.getJSONObject(index);
			if(null == urlObj){
				continue;
			}
			String seedUrl = urlObj.getString("seed_url");
			if(StringUtils.isBlank(seedUrl)){
				continue;
			}
			String domain = this.getDomainFromUrl(seedUrl);
			int lastCount = 1;
			if(domainMap.containsKey(domain)){
				lastCount = domainMap.get(domain);
				lastCount ++;
			}
			domainMap.put(domain, lastCount);
		}
		
		List<DomainStat> domainStatList = Lists.newArrayList();
		for(Entry<String, Integer> entry: domainMap.entrySet()){
			domainStatList.add(new DomainStat(entry.getKey(), entry.getValue()));
		}
		data.put("domain_stats", domainStatList);
		
		return true;
	}
	
	private String getDomainFromUrl(String strUrl){
		try {
			URL url = new URL(strUrl);
			String domain = url.getHost();
			return domain;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return "";
	}
	
}
