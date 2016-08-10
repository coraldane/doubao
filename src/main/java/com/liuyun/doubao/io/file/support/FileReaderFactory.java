package com.liuyun.doubao.io.file.support;

import java.util.Map;

import com.google.common.collect.Maps;
import com.liuyun.doubao.input.AbstractStopableDataReader;
import com.liuyun.doubao.input.DataReaderFactory;

public class FileReaderFactory implements DataReaderFactory {

	private Map<String, AbstractStopableDataReader> fileReaderMap = Maps.newConcurrentMap();

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
		AbstractStopableDataReader dataReader = this.fileReaderMap.get(key);
		if(null == dataReader){
			return;
		}
		dataReader.notifyForRead();
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
		SingleFileReader fileReader = this.fileReaderMap.get(oldFileKey);
		this.fileReaderMap.put(fileKey, fileReader);
		fileReader.reset(fileKey, child);
	}
	
}
