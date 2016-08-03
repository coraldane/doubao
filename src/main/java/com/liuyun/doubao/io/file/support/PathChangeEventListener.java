package com.liuyun.doubao.io.file.support;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.liuyun.doubao.utils.FileUtils;

public class PathChangeEventListener {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Map<FileUniqueKey, SingleFileReader> fileReaderMap = null;
	private SincedbHandler sincedbHandler = null;
	
	public PathChangeEventListener(SincedbHandler handler, Map<FileUniqueKey, SingleFileReader> fileReaderMap){
		this.sincedbHandler = handler;
		this.fileReaderMap = fileReaderMap;
	}
	
	public void addFileReader(Path path) throws IOException {
		if(null == this.fileReaderMap){
			this.fileReaderMap = Maps.newConcurrentMap();
		}
		FileUniqueKey fileKey = FileUtils.getInodeAndDevice(path);
		SingleFileReader fileReader = new SingleFileReader(path, fileKey, sincedbHandler);
		this.fileReaderMap.put(fileKey, fileReader);
	}
	
	public void handleEvent(Kind<?> kind, Path child) {
		logger.info("{}: {}", kind.name(), child);
	}
	
}
