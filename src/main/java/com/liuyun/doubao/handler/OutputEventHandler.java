package com.liuyun.doubao.handler;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.liuyun.doubao.config.OutputConfig;
import com.liuyun.doubao.ctx.Context;
import com.liuyun.doubao.ctx.JsonEvent;
import com.liuyun.doubao.extension.ExtensionLoader;
import com.liuyun.doubao.io.Output;

public class OutputEventHandler implements ClosableEventHandler {
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static final ExtensionLoader<Output> loader = ExtensionLoader.getExtensionLoader(Output.class);
	
	private List<OutputThread> outputThreads = Lists.newArrayList();
	
	private ExecutorService executor = null;
	
	@Override
	public void init(Context context) {
		List<OutputConfig> outputConfigs = context.getConfig().getOutputs();
		if(CollectionUtils.isEmpty(outputConfigs)){
			logger.error("output config is empty.");
			return;
		}
		
		this.executor = Executors.newFixedThreadPool(outputConfigs.size());
		
		for(OutputConfig outputConfig: outputConfigs){
			Output output = null;
			try {
				output = loader.getExtension(outputConfig.getName()).getClass().newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(null != output){
				output.init(outputConfig);
				OutputThread thread = new OutputThread(output, outputConfig.getBatchSize());
				this.outputThreads.add(thread);
				executor.submit(thread);
			}
		}
	}

	@Override
	public void destroy(Context context) {
		this.executor.shutdown();
		for(OutputThread thread: this.outputThreads){
			thread.getOutput().destroy();
		}
	}

	@Override
	public void onEvent(JsonEvent event, long arg1, boolean arg2) throws Exception {
		// TODO Auto-generated method stub
		JSONObject data = event.get();
		for(OutputThread thread: this.outputThreads){
			thread.pushData(data);
		}
	}
}

class OutputThread implements Runnable {
	private static final int DEFAULT_POLL_TIMEOUT = 3;
	
	private Output output = null;
	private int batchSize = 0;
	
	private BlockingQueue<JSONObject> dataQueue = new LinkedBlockingQueue<JSONObject>();
	
	public OutputThread(Output output, int batchSize){
		this.output = output;
		this.batchSize = batchSize;
	}
	
	@Override
	public void run(){
		while(true){
			writeData();
		}
	}
	
	private void writeData(){
		JSONArray dataArray = new JSONArray();
		for(int index=0; index < batchSize; index++){
			try {
				JSONObject data = this.popData();
				if(null == data){
					break;
				} else {
					dataArray.add(data);
				}
			} catch (InterruptedException e) {
//				e.printStackTrace();
			}
		}
		this.output.write(dataArray);
	}
	
	private JSONObject popData() throws InterruptedException{
		return this.dataQueue.poll(DEFAULT_POLL_TIMEOUT, TimeUnit.SECONDS);
	}

	public void pushData(JSONObject data) {
		this.dataQueue.add(data);
	}

	public Output getOutput() {
		return output;
	}

	public void setOutput(Output output) {
		this.output = output;
	}
	
}
