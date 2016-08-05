package com.liuyun.doubao.plugin.resourceInfo;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.liuyun.doubao.config.PluginConfig;
import com.liuyun.doubao.io.FilterResult;
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
	public FilterResult filter(JSONObject data) {
		JSONObject taskInfo = data.getJSONObject("task_info");
		if(null == taskInfo){
			return FilterResult.newDrop();
		}
		
		JSONObject paramsObj = taskInfo.getJSONObject("params");
		if(null == paramsObj){
			return FilterResult.newDrop();
		}
		
		JSONArray urlArray = paramsObj.getJSONArray("resource_urls");
		if(null == urlArray){
			return FilterResult.newDrop();
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
		paramsObj.remove("resource_urls");
		taskInfo.put("params", paramsObj);
		data.put("task_info", taskInfo);
		return FilterResult.newMatched(true);
	}
	
	private String getDomainFromUrl(String strUrl){
		int start = strUrl.indexOf("//");
		if(0 > start){
			start = 0;
		} else {
			start += 2;
		}
		int end = strUrl.indexOf("/", start+1);
		if(start > end){
			end = strUrl.indexOf("?", start+1);
		}
		if(start > end){
			end = strUrl.length();
		}
		String domain = strUrl.substring(start, end);
		return domain;
	}
	
}
