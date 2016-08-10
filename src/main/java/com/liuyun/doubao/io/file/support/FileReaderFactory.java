package com.liuyun.doubao.io.file.support;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.liuyun.doubao.input.AbstractStopableDataReader;
import com.liuyun.doubao.input.DataReaderFactory;
import com.liuyun.doubao.utils.ThreadUtils;

public class FileReaderFactory implements DataReaderFactory {
	
	protected volatile boolean running = true;

	private Map<String, AbstractStopableDataReader> fileReaderMap = Maps.newConcurrentMap();
	private BlockingQueue<String> readyQueue = new LinkedBlockingQueue<String>();
	
	@Override
	public void addReader(String key, AbstractStopableDataReader dataReader) {
		this.fileReaderMap.put(key, dataReader);
	}

	@Override
	public boolean containsReader(String key) {
		return this.fileReaderMap.containsKey(key);
	}
	
	@Override
	public void stop(boolean waitCompleted) {
		this.running = false;
		for(String key: this.fileReaderMap.keySet()){
			AbstractStopableDataReader dataReader = this.fileReaderMap.get(key);
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
		while(this.running){
			try {
				String key = this.readyQueue.take();
				
				final AbstractStopableDataReader dataReader = this.fileReaderMap.get(key);
				ThreadUtils.newThread(dataReader, new Function<AbstractStopableDataReader, Void>(){
					@Override
					public Void apply(AbstractStopableDataReader input) {
						try {
							dataReader.readData();
						} catch (IOException e) {
							e.printStackTrace();
						}
						return null;
					}
					
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
