package com.liuyun.doubao.handler;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.liuyun.doubao.common.InitializingBean;
import com.liuyun.doubao.config.OutputConfig;
import com.liuyun.doubao.ctx.Context;
import com.liuyun.doubao.extension.ExtensionLoader;
import com.liuyun.doubao.io.Output;

public class OutputHolder implements InitializingBean {
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static final ExtensionLoader<Output> loader = ExtensionLoader.getExtensionLoader(Output.class);
	
	private List<OutputEventHandler> outputEventHandlers = Lists.newArrayList();
	private ExecutorService executor = null;
	
	@Override
	public void init(Context context) {
		List<OutputConfig> outputConfigs = context.getConfig().getOutputs();
		if(CollectionUtils.isEmpty(outputConfigs)){
			logger.error("output config is empty.");
			return;
		}
		
		executor = Executors.newFixedThreadPool(outputConfigs.size());
		for(OutputConfig outputConfig: outputConfigs){
			Output output = null;
			try {
				output = loader.getExtension(outputConfig.getName()).getClass().newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(null != output){
				output.init(outputConfig);
				OutputEventHandler thread = new OutputEventHandler(output, outputConfig.getBatchSize());
				this.outputEventHandlers.add(thread);
				executor.submit(thread);
			}
		}
	}

	@Override
	public void destroy(Context context) {
		for(OutputEventHandler thread: this.outputEventHandlers){
			thread.getOutput().destroy();
		}
	}

	public OutputEventHandler[] getOutputEventHandlers() {
		OutputEventHandler[] retArray = new OutputEventHandler[this.outputEventHandlers.size()];
		for(int index=0; index < this.outputEventHandlers.size(); index++){
			retArray[index] = this.outputEventHandlers.get(index);
		}
		return retArray;
	}

}

