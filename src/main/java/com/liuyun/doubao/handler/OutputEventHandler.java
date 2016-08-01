package com.liuyun.doubao.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.liuyun.doubao.ctx.JsonEvent;
import com.liuyun.doubao.io.Output;
import com.lmax.disruptor.EventHandler;

public class OutputEventHandler implements EventHandler<JsonEvent>, Runnable {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Output output = null;
	private int batchSize = 0;
	
	private long lastWriteTime = System.currentTimeMillis();
	
	private JSONArray dataBuffer = new JSONArray();
	
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
		if(this.dataBuffer.size() >= this.batchSize){
			throw new RuntimeException("wait for write data...");
		}
		
		this.dataBuffer.add(event.get());
	}
	
	@Override
	public void run(){
		while(true){
			if(this.dataBuffer.size() == this.batchSize || lastWriteTime +1000 < System.currentTimeMillis()){
				this.output.write(this.dataBuffer);
				this.dataBuffer.clear();
				
				lastWriteTime = System.currentTimeMillis();
			} else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
		}
	}
	
}