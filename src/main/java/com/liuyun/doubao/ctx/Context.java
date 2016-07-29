package com.liuyun.doubao.ctx;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.alibaba.fastjson.JSONObject;
import com.liuyun.doubao.config.DoubaoConfig;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

public class Context {

	// Specify the size of the ring buffer, must be power of 2.
	private static final int DEFAULT_RING_BUFFER_SIZE = 1024;
	
	private DoubaoConfig config = null;
	
	private RingBuffer<JsonEvent> filterQueue = null;
	private RingBuffer<JsonEvent> outputQueue = null;

	public Context(DoubaoConfig config){
		this.config = config;
		init();
	}
	
	private void init(){
		ThreadFactory threadFactory = Executors.defaultThreadFactory();
		
        JsonEventFactory factory = new JsonEventFactory();
		Disruptor<JsonEvent> disruptor = new Disruptor<JsonEvent>(factory, DEFAULT_RING_BUFFER_SIZE, threadFactory);
		disruptor.handleEventsWith(new JsonEventHandler());
		
	}

	public DoubaoConfig getConfig() {
		return config;
	}

	public BlockingQueue<JSONObject> getFilterQueue() {
		return filterQueue;
	}

	public BlockingQueue<JSONObject> getOutputQueue() {
		return outputQueue;
	}
	
	public boolean isFilterQueueEmpty() {
		
	}
	
	public void put2Filter(JSONObject data){
		this.filterQueue.add(data);
	}
	
	public void put2Output(JSONObject data){
		this.outputQueue.add(data);
	}
}
