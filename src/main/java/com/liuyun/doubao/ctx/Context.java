package com.liuyun.doubao.ctx;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.alibaba.fastjson.JSONObject;
import com.liuyun.doubao.config.DoubaoConfig;

public class Context {

	private DoubaoConfig config = null;
	
	private BlockingQueue<JSONObject> filterQueue = new LinkedBlockingQueue<JSONObject>();
	private BlockingQueue<JSONObject> outputQueue = new LinkedBlockingQueue<JSONObject>();

	public Context(DoubaoConfig config){
		this.config = config;
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
	
	public void put2Filter(JSONObject data){
		this.filterQueue.add(data);
	}
	
	public void put2Output(JSONObject data){
		this.outputQueue.add(data);
	}
}
