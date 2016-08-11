package com.liuyun.doubao.processor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.liuyun.doubao.common.Identified;
import com.liuyun.doubao.common.InitializingBean;
import com.liuyun.doubao.config.OutputConfig;
import com.liuyun.doubao.ctx.Context;
import com.liuyun.doubao.extension.ExtensionLoader;
import com.liuyun.doubao.io.Output;

public class OutputHolder implements InitializingBean {
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static final ExtensionLoader<Output> loader = ExtensionLoader.getExtensionLoader(Output.class);
	
	private List<OutputProcessor> outputProcessors = Lists.newArrayList();
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
			Output output = loader.createExtensionByIdentified(outputConfig.getClass().getAnnotation(Identified.class));
			
			if(null != output){
				output.init(outputConfig, context);
				OutputProcessor thread = new OutputProcessor(output, outputConfig);
				this.outputProcessors.add(thread);
				executor.submit(thread);
			}
		}
	}

	@Override
	public void destroy(Context context) {
		for(OutputProcessor thread: this.outputProcessors){
			thread.getOutput().destroy();
		}
	}

	public OutputProcessor[] getOutputEventHandlers() {
		OutputProcessor[] retArray = new OutputProcessor[this.outputProcessors.size()];
		for(int index=0; index < this.outputProcessors.size(); index++){
			retArray[index] = this.outputProcessors.get(index);
		}
		return retArray;
	}

}


