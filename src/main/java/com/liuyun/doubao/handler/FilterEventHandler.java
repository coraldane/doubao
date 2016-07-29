package com.liuyun.doubao.handler;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.liuyun.doubao.config.FilterConfig;
import com.liuyun.doubao.ctx.Context;
import com.liuyun.doubao.ctx.JsonEvent;
import com.liuyun.doubao.extension.ExtensionLoader;
import com.liuyun.doubao.io.Filter;

public class FilterEventHandler implements ClosableEventHandler {
	private static final ExtensionLoader<Filter> loader = ExtensionLoader.getExtensionLoader(Filter.class);
	
	private List<Filter> filters = Lists.newArrayList();
	
	private Context context;
	
	@Override
	public void init(Context context) {
		this.context = context;
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
	public void onEvent(JsonEvent event, long sequence, boolean endOfBatch) throws Exception {
		JSONObject data = event.get();
		boolean denied = false;
		for(Filter filter: this.filters){
			if(!filter.doMatch(data)){
				denied = true;
				break;
			}
		}
		if(!denied){
			Context.putData2Queue(this.context.getOutputQueue(), data);
		}
	}

	@Override
	public void destroy(Context context) {
//		super.waitForStoped();
		for(Filter filter: this.filters){
			filter.destroy();
		}
	}

}
