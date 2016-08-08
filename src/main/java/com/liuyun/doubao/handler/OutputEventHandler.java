package com.liuyun.doubao.handler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.liuyun.doubao.ctx.JsonEvent;
import com.liuyun.doubao.io.Output;
import com.liuyun.doubao.utils.SysUtils;
import com.lmax.disruptor.EventHandler;

public class OutputEventHandler implements EventHandler<JsonEvent>, Runnable {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Output output = null;
	private int batchSize = 0;
	
	private long lastWriteTime = System.currentTimeMillis();
	
	private List<JSONObject> dataBuffer = Lists.newArrayList();
	
	public OutputEventHandler(Output output, int batchSize){
		this.output = output;
		this.batchSize = batchSize;
	}
	
	public Output getOutput() {
		return output;
	}

	public void setOutput(Output output) {
		this.output = output;
	}

	@Override
	public void onEvent(JsonEvent event, long sequence, boolean endOfBatch) throws Exception {
		while(this.dataBuffer.size() >= this.batchSize){
			SysUtils.sleep(100);
		}
		
		this.dataBuffer.add(event.get());
	}
	
	@Override
	public void run(){
		while(true){
			if(!this.dataBuffer.isEmpty() && 
					(this.dataBuffer.size() >= this.batchSize || lastWriteTime +1000 < System.currentTimeMillis())){
				this.output.write(this.dataBuffer);
				this.dataBuffer.clear();
				
				lastWriteTime = System.currentTimeMillis();
			} else {
				SysUtils.sleep(100);
			}
		}
	}
	
}