package com.liuyun.doubao.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liuyun.doubao.common.Identified;
import com.liuyun.doubao.common.InitializingBean;
import com.liuyun.doubao.config.InputConfig;
import com.liuyun.doubao.ctx.Context;
import com.liuyun.doubao.extension.ExtensionLoader;
import com.liuyun.doubao.io.Input;
import com.liuyun.doubao.io.Stopable;

public class InputProcessor implements InitializingBean, Stopable {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static final ExtensionLoader<Input> loader = ExtensionLoader.getExtensionLoader(Input.class);

	private Input input = null;
	protected Context context = null;
	
	public InputProcessor(){
		
	}

	@Override
	public void init(Context context) {
		this.context = context;
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
	public void destroy(Context context) {
		if (null != this.input) {
			this.input.destroy();
		}
	}

	@Override
	public void stop(boolean waitCompleted) {
		if (null != this.input) {
			this.input.stop(false);
		}
	}

}
