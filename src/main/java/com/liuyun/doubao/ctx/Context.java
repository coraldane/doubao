package com.liuyun.doubao.ctx;

import com.alibaba.fastjson.JSONObject;
import com.liuyun.doubao.config.DoubaoConfig;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;

public class Context {
	private DoubaoConfig config = null;
	
	private RingBuffer<JsonEvent> filterQueue = null;
	private RingBuffer<JsonEvent> outputQueue = null;
	
	public Context(DoubaoConfig config){
		this.config = config;
	}
	
	public DoubaoConfig getConfig() {
		return config;
	}

	public RingBuffer<JsonEvent> getFilterQueue() {
		return filterQueue;
	}

	public void setFilterQueue(RingBuffer<JsonEvent> filterQueue) {
		this.filterQueue = filterQueue;
	}

	public RingBuffer<JsonEvent> getOutputQueue() {
		return outputQueue;
	}

	public void setOutputQueue(RingBuffer<JsonEvent> outputQueue) {
		this.outputQueue = outputQueue;
	}
	
	private void waitForStoped(RingBuffer<JsonEvent> ringBuffer){
		while(ringBuffer.remainingCapacity() < ringBuffer.getBufferSize()){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				
			}
		}
	}
	
	public void stop(){
		if(null != this.filterQueue){
			waitForStoped(this.filterQueue);
		}
		if(null != this.outputQueue){
			waitForStoped(this.outputQueue);
		}
	}
	
	private static final EventTranslatorOneArg<JsonEvent, JSONObject> TRANSLATOR = new EventTranslatorOneArg<JsonEvent, JSONObject>(){
		@Override
		public void translateTo(JsonEvent event, long sequence, JSONObject obj) {
			event.set(obj);
		}
	};
	
	public static void putData2Queue(RingBuffer<JsonEvent> ringBuffer, JSONObject obj) {
		if (null != ringBuffer) {
			ringBuffer.publishEvent(TRANSLATOR, obj);
		}
	}
}
