package com.liuyun.doubao.io.file.support;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.collect.Maps;
import com.liuyun.doubao.input.AbstractStopableDataReader;
import com.liuyun.doubao.input.DataReaderFactory;
import com.liuyun.doubao.utils.SysUtils;

public class FileReaderFactory implements DataReaderFactory {

	private ExecutorService executor = Executors.newCachedThreadPool();
	private Map<String, AbstractStopableDataReader> fileReaderMap = Maps.newConcurrentMap();
	private Map<String, DataReadConsumer> consumerMap = Maps.newConcurrentMap();
	private BlockingQueue<String> readyQueue = new LinkedBlockingQueue<String>();
	
	@Override
	public void addReader(String key, AbstractStopableDataReader dataReader) {
		this.fileReaderMap.put(key, dataReader);
		DataReadConsumer consumer = new DataReadConsumer(dataReader);
		this.consumerMap.put(key, consumer);
		this.executor.submit(consumer);
	}

	@Override
	public boolean containsReader(String key) {
		return this.fileReaderMap.containsKey(key);
	}
	
	@Override
	public void stop(boolean waitCompleted) {
		for(String key: this.fileReaderMap.keySet()){
			final AbstractStopableDataReader dataReader = this.fileReaderMap.get(key);
			dataReader.stop(waitCompleted);
		}
	}

	@Override
	public void destroy() {
		for(String key: this.fileReaderMap.keySet()){
			destroy(key);
		}
	}

	@Override
	public void notifyForRead(String key) {
		try {
			this.readyQueue.put(key);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void destroy(String key) {
		AbstractStopableDataReader dataReader = this.fileReaderMap.get(key);
		if(null == dataReader){
			return;
		}
		dataReader.destroy();
	}
	
	@Override
	public void resetReader(String oldKey, String newKey, Object param) {
		Path path = (Path)param;
		AbstractStopableDataReader reader = this.fileReaderMap.get(oldKey);
		SingleFileReader fileReader = (SingleFileReader)reader;
		this.fileReaderMap.put(newKey, fileReader);
		try {
			fileReader.reset(newKey, path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while(true){
			try {
				String key = this.readyQueue.take();
				
				DataReadConsumer consumer = this.consumerMap.get(key);
				if(null == consumer){
					return;
				}
				consumer.setReady(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}

class DataReadConsumer implements Runnable {
	private volatile boolean ready = false;
	
	private AbstractStopableDataReader dataReader = null;
	
	public DataReadConsumer(AbstractStopableDataReader dataReader){
		this.dataReader = dataReader;
	}
	
	@Override
	public void run(){
		while(true){
			if(ready){
				try {
					this.dataReader.readData();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				SysUtils.sleep(100);
			}
		}
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}
	
}
