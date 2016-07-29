package com.liuyun.doubao.handler;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
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

		this.input = loader.getExtension(inputConfig.getName());
		if (null != this.input) {
			this.input.init(inputConfig);
		}
	}

	@Override
	public boolean doTask(Context context) throws Exception {
		if (null == this.input) {
			return false;
		}
		List<JSONObject> dataList = this.input.read();
		for (JSONObject obj : dataList) {
			Context.putData2Queue(context.getFilterQueue(), obj);
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

}
