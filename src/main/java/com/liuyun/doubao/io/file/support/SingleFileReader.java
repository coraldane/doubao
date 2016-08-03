package com.liuyun.doubao.io.file.support;

import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

public class SingleFileReader {
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private volatile boolean ready = true;
	
	private Path path;
	private FileUniqueKey fileKey;
	private SincedbHandler sincedbHandler;
	
	public SingleFileReader(Path path, FileUniqueKey fileKey, SincedbHandler sincedbHandler) {
		this.path = path;
		this.fileKey = fileKey;
		this.sincedbHandler = sincedbHandler;
	}

	public List<JSONObject> read(){
		if(ready){
			logger.info("file unique key:{}, path: {}", this.fileKey, this.path);
		}
		return null;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}
	
}
