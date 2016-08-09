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
	
	private Map<String, SingleFileReader> fileReaderMap = null;
	private Map<String, String> fileKeyMap = Maps.newConcurrentMap();
	private SincedbHandler sincedbHandler = null;
	
	public PathChangeEventListener(SincedbHandler handler, Map<String, SingleFileReader> fileReaderMap){
		this.sincedbHandler = handler;
		this.fileReaderMap = fileReaderMap;
	}
	
	public void addFileReader(Path path) throws IOException {
		logger.debug("register file: {}", path.toString());
		if(null == this.fileReaderMap){
			this.fileReaderMap = Maps.newConcurrentMap();
		}
		String fileKey = FileUtils.getInodeAndDevice(path);
		if(!this.fileReaderMap.containsKey(fileKey)){
			SingleFileReader fileReader = new SingleFileReader(path, fileKey, sincedbHandler);
			this.fileReaderMap.put(fileKey, fileReader);
			this.fileKeyMap.put(path.toString(), fileKey);
		}
	}
	
	public void handleEvent(Kind<?> kind, Path child) {
		if(!this.fileKeyMap.containsKey(child.toString())){
			return;
		}
		logger.debug("kind:{}, path: {}", kind.name(), child.toString());
		
		String fileKey = FileUtils.getInodeAndDevice(child);
		if("ENTRY_CREATE".equals(kind.name())){
			this.fileReaderMap.get(fileKey).setReady(true);
		} else if("ENTRY_MODIFY".equals(kind.name())){
			String oldFileKey = this.fileKeyMap.get(child.toString());
			if(fileKey.equals(oldFileKey)){
				this.fileReaderMap.get(fileKey).setReady(true);
			} else {
				try {
					this.fileKeyMap.put(child.toString(), fileKey);
					logger.debug("file has changed, path: {}", child.toString());
					SingleFileReader fileReader = this.fileReaderMap.get(oldFileKey);
					this.fileReaderMap.put(fileKey, fileReader);
					fileReader.reset(fileKey, child);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if("ENTRY_DELETE".equals(kind.name())){
			this.sincedbHandler.removeFile(fileKey);
			this.fileReaderMap.get(fileKey).destroy();
		}
	}
	
}
