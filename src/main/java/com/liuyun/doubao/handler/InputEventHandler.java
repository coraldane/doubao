package com.liuyun.doubao.handler;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.alibaba.fastjson.JSONObject;
import com.liuyun.doubao.common.Identified;
import com.liuyun.doubao.config.InputConfig;
import com.liuyun.doubao.ctx.Context;
import com.liuyun.doubao.extension.ExtensionLoader;
import com.liuyun.doubao.io.Input;

public class InputEventHandler extends StopableThread {
	private static final ExtensionLoader<Input> loader = ExtensionLoader.getExtensionLoader(Input.class);

	private Input input = null;

	public InputEventHandler(Context context) {
		super(context);
	}

	@Override
	public void init(Context context) {
		InputConfig inputConfig = context.getConfig().getInput();
		if (null == inputConfig) {
			logger.error("input config is null.");
			return;
		}
		
		this.input = loader.createExtensionByIdentified(inputConfig.getClass().getAnnotation(Identified.class));
		if (null != this.input) {
			this.input.init(inputConfig, context);
		}
	}

	@Override
	public boolean doTask(Context context) throws Exception {
		if (null == this.input) {
			return false;
		}
		List<JSONObject> dataList = this.input.read();
		if(CollectionUtils.isEmpty(dataList)){
			return false;
		}
		for (JSONObject json : dataList) {
			JSONObject data = this.assemableData(json);
			Context.putData2Queue(context.getFilterQueue(), data);
		}
		return true;
	}

	@Override
	public void destroy(Context context) {
		super.waitForStoped();
		if (null != this.input) {
			this.input.destroy();
		}
	}

	private JSONObject assemableData(JSONObject json){
		InputConfig inputConfig = this.context.getConfig().getInput();
		Map<String, Object> addedFieldMap = inputConfig.getAddedFieldMap();
		if(null != addedFieldMap && !addedFieldMap.isEmpty()){
			for(String key: addedFieldMap.keySet()){
				json.put(key, addedFieldMap.get(key));
			}
		}
		return json;
	}
}
