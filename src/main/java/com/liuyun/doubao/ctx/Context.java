package com.liuyun.doubao.ctx;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.liuyun.doubao.config.DoubaoConfig;
import com.liuyun.doubao.config.InputConfig;
import com.liuyun.doubao.extension.ExtensionLoader;
import com.liuyun.doubao.io.Compression;
import com.liuyun.doubao.stat.DataStatisticsRepository;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;

public class Context {
	private static final ExtensionLoader<Compression> compressionLoader = ExtensionLoader.getExtensionLoader(Compression.class);
	
	private DataStatisticsRepository dataStatRepository = null;
	private DoubaoConfig config = null;
	
	private RingBuffer<JsonEvent> filterQueue = null;
	private RingBuffer<JsonEvent> outputQueue = null;
	
	public Context(DoubaoConfig config, DataStatisticsRepository dataStatRepository){
		this.config = config;
		this.dataStatRepository = dataStatRepository;
	}
	
	public DoubaoConfig getConfig() {
		return config;
	}

	public void setFilterQueue(RingBuffer<JsonEvent> filterQueue) {
		this.filterQueue = filterQueue;
	}

	public void setOutputQueue(RingBuffer<JsonEvent> outputQueue) {
		this.outputQueue = outputQueue;
	}
	
	public DataStatisticsRepository getDataStatRepository() {
		return dataStatRepository;
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
	
	public void putData2OutQueue(JSONObject json) {
		this.outputQueue.publishEvent(TRANSLATOR, json);
		this.dataStatRepository.incrementDataOutputOrigin(1, json.toString().getBytes().length);
	}
	
	public static void addTag2Data(JSONObject data, String tag){
		JSONArray tags = data.getJSONArray("tags");
		if(null == tags){
			tags = new JSONArray();
		}
		List<String> tagList = Lists.newArrayList();
		for(int index=0; index < tags.size(); index++){
			tagList.add(tags.getString(index));
		}
		if(!tagList.contains(tag)){
			tagList.add(tag);
		}
		data.put("tags", tagList);
	}
	
	public void readData2Queue(JSONObject json){
		InputConfig inputConfig = this.getConfig().getInput();
		Map<String, Object> addedFieldMap = inputConfig.getAdd_field();
		if(null != addedFieldMap && !addedFieldMap.isEmpty()){
			for(String key: addedFieldMap.keySet()){
				json.put(key, addedFieldMap.get(key));
			}
		}
		this.filterQueue.publishEvent(TRANSLATOR, json);
	}
	
	public static Compression getCompression(Object configInstance){
		Object compressTypeObj = getProperty(configInstance, "compression_type");
		if(null == compressTypeObj || "none".equals(compressTypeObj)){
			
		} else {
			Set<String> extensionNameSet = compressionLoader.getSupportedExtensions();
			if(extensionNameSet.contains(compressTypeObj)){
				return compressionLoader.getExtension(compressTypeObj.toString());
			}
		}
		return null;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Object getProperty(Object instance, String fieldName) {
		if(null == instance){
			return null;
		}
		try{
			Class clazz = instance.getClass();
			String methodName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
			Method method = clazz.getMethod(methodName);
			return method.invoke(instance);
		}	catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
}
