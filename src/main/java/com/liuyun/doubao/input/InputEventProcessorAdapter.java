package com.liuyun.doubao.input;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.liuyun.doubao.config.InputConfig;
import com.liuyun.doubao.ctx.Context;
import com.liuyun.doubao.processor.StopableThread;

public abstract class InputEventProcessorAdapter extends StopableThread implements InputListener {

	protected volatile boolean ready = true;
	protected volatile boolean waitForReading = true;
	
	@Override
	public void notifyForRead() {
		this.ready = true;
	}
	
	protected void write(JSONObject json){
		InputConfig inputConfig = this.context.getConfig().getInput();
		Map<String, Object> addedFieldMap = inputConfig.getAddedFieldMap();
		if(null != addedFieldMap && !addedFieldMap.isEmpty()){
			for(String key: addedFieldMap.keySet()){
				json.put(key, addedFieldMap.get(key));
			}
		}
		Context.putData2Queue(context.getFilterQueue(), json);
	}
	
}
