package com.liuyun.doubao.io.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.liuyun.doubao.config.InputConfig;
import com.liuyun.doubao.config.file.FileInputConfig;
import com.liuyun.doubao.io.Input;
import com.liuyun.doubao.utils.StringUtils;

public class FileInput implements Input {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private ExecutorService executor = Executors.newCachedThreadPool();
	private Map<String, FilePathWatcher> pathWatcherMap = Maps.newConcurrentMap();
	
	private FileInputConfig inputConfig = null;
	
	@Override
	public void init(InputConfig inputConfig) {
		if(inputConfig instanceof FileInputConfig){
			this.inputConfig = (FileInputConfig)inputConfig;
			
			try {
				this.registerPathWatcher();
			} catch (IOException e) {
				logger.error("register path watcher error", e);
			}
		}
	}
	
	private void registerPathWatcher() throws IOException{
		for(String filepath: this.inputConfig.getPath()){
			if(StringUtils.isBlank(filepath)){
				logger.error("path canot be empty!");
				continue;
			}
			
			PathResolver resolver = new PathResolver(filepath);
			Path start = Paths.get(resolver.getDir());
			if(!Files.isDirectory(start) || Files.notExists(start)){
				logger.error("file path [" + resolver.getDir() + "] is not directory or not exists!");
				continue;
			}
			
			String pathHash = DigestUtils.md2Hex(filepath);
			FilePathWatcher pathWatcher = new FilePathWatcher(start, resolver.getGlob(), this.inputConfig.isRecursive());
			this.pathWatcherMap.put(pathHash, pathWatcher);
			executor.submit(pathWatcher);
		}
	}
	
	@Override
	public void destroy() {
		this.executor.shutdown();
		for(String key: this.pathWatcherMap.keySet()){
			FilePathWatcher pathWatcher = this.pathWatcherMap.get(key);
			pathWatcher.destroy();
		}
	}

	@Override
	public List<JSONObject> read() {
		// TODO Auto-generated method stub
		return null;
	}

}
