package com.liuyun.doubao.processor;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.liuyun.doubao.common.Identified;
import com.liuyun.doubao.config.FilterConfig;
import com.liuyun.doubao.ctx.Context;
import com.liuyun.doubao.ctx.JsonEvent;
import com.liuyun.doubao.extension.ExtensionLoader;
import com.liuyun.doubao.io.Filter;
import com.liuyun.doubao.io.FilterResult;

public class FilterProcessor implements ClosableProcessor {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static final ExtensionLoader<Filter> loader = ExtensionLoader.getExtensionLoader(Filter.class);
	
	private List<List<Filter>> filters = Lists.newArrayList();
	
	private Context context;
	
	@Override
	public void init(Context context) {
		this.context = context;
		List<List<FilterConfig>> filterConfigs = context.getConfig().getFilters();
		for(List<FilterConfig> configs: filterConfigs){
			List<Filter> filterList = Lists.newArrayList();
			for(FilterConfig filterConfig: configs){
				Filter filter = loader.createExtensionByIdentified(filterConfig.getClass().getAnnotation(Identified.class));
				if(null != filter){
					filter.init(filterConfig);
					filterList.add(filter);
				}
			}
			if(CollectionUtils.isNotEmpty(filterList)){
				this.filters.add(filterList);
			}
		}
	}
	
	@Override
	public void onEvent(JsonEvent event, long sequence, boolean endOfBatch) throws Exception {
		JSONObject data = event.get();
		boolean denied = false;
		for(List<Filter> filterList: this.filters){
			for(Filter filter: filterList){
				FilterResult result = filter.doMatch(data);
				if(false == result.isContinued()){
					denied = true;
					break;
				}
			}
			if(denied){
				break;
			}
		}
		if(false == denied){
			Context.putData2Queue(this.context.getOutputQueue(), data);
		}
	}

	@Override
	public void destroy(Context context) {
		for(List<Filter> filterList: this.filters){
			for(Filter filter: filterList){
				filter.destroy();
			}
		}
	}

}
