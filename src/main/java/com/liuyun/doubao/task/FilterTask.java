package com.liuyun.doubao.task;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.liuyun.doubao.config.FilterConfig;
import com.liuyun.doubao.ctx.Context;
import com.liuyun.doubao.extension.ExtensionLoader;
import com.liuyun.doubao.io.Filter;

public class FilterTask extends TaskAdapter {
	private static final ExtensionLoader<Filter> loader = ExtensionLoader.getExtensionLoader(Filter.class);
	
	private List<Filter> filters = Lists.newArrayList();
	
	public FilterTask(Context context) {
		super(context);
	}
	
	@Override
	public void init(Context context) {
		List<FilterConfig> filterConfigs = context.getConfig().getFilters();
		for(FilterConfig filterConfig: filterConfigs){
			Filter filter = null;
			try {
				filter = loader.getExtension(filterConfig.getName()).getClass().newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(null != filter){
				filter.init(filterConfig);
				this.filters.add(filter);
			}
		}
	}

	@Override
	public boolean doTask(Context context) throws Exception {
		JSONObject data = this.context.getFilterQueue().poll(DEFAULT_POLL_TIMEOUT, TimeUnit.SECONDS);
		if(null == data){
			return false;
		}
		
		boolean denied = false;
		for(Filter filter: this.filters){
			if(!filter.doMatch(data)){
				denied = true;
				break;
			}
		}
		if(!denied){
			this.context.put2Output(data);
		}
		return !this.context.getFilterQueue().isEmpty();
	}

	@Override
	public void destroy(Context context) {
		super.waitForStoped();
		for(Filter filter: this.filters){
			filter.destroy();
		}
	}

}
