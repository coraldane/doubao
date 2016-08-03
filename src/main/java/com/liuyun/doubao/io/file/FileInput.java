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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.liuyun.doubao.config.InputConfig;
import com.liuyun.doubao.config.file.FileInputConfig;
import com.liuyun.doubao.io.Input;
import com.liuyun.doubao.io.file.support.FilePathWatcher;
import com.liuyun.doubao.io.file.support.FileUniqueKey;
import com.liuyun.doubao.io.file.support.PathChangeEventListener;
import com.liuyun.doubao.io.file.support.PathResolver;
import com.liuyun.doubao.io.file.support.SincedbHandler;
import com.liuyun.doubao.io.file.support.SingleFileReader;
import com.liuyun.doubao.utils.StringUtils;

public class FileInput implements Input {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private ExecutorService pathWatchExecutor = Executors.newCachedThreadPool();
	
	private List<FilePathWatcher> pathWatcherList = Lists.newArrayList();
	private Map<String, SincedbHandler> sincedbHandlerMap = Maps.newConcurrentMap();
	private Map<FileUniqueKey, SingleFileReader> fileReaderMap = Maps.newConcurrentMap();
	
	private FileInputConfig inputConfig = null;
	
	@Override
	public void init(InputConfig inputConfig) {
		if(inputConfig instanceof FileInputConfig){
			this.inputConfig = (FileInputConfig)inputConfig;
			
			try {
				this.registerPathWatcher();
			} catch (IOException e) {
				logger.error("register watcher error", e);
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
			
			String hashKey = DigestUtils.md2Hex(filepath);
			
			SincedbHandler sincedbHandler = new SincedbHandler(hashKey, inputConfig);
			PathChangeEventListener eventListener = new PathChangeEventListener(sincedbHandler, this.fileReaderMap);
			FilePathWatcher pathWatcher = new FilePathWatcher(start, resolver.getGlob(), this.inputConfig, eventListener);
			
			this.pathWatcherList.add(pathWatcher);
			this.sincedbHandlerMap.put(hashKey, sincedbHandler);
			
			this.pathWatchExecutor.submit(pathWatcher);
		}
	}
	
	@Override
	public void destroy() {
		for(FilePathWatcher pathWatcher: this.pathWatcherList){
			pathWatcher.destroy();
		}
		this.pathWatchExecutor.shutdown();
		
		//close file channel
		for(SingleFileReader fileReader: this.fileReaderMap.values()){
			fileReader.destroy();
		}
		
		for(SincedbHandler handler: this.sincedbHandlerMap.values()){
			handler.flush();
		}
	}

	@Override
	public List<JSONObject> read() {
		List<JSONObject> dataList = Lists.newArrayList();
		for(SingleFileReader fileReader: this.fileReaderMap.values()){
			try {
				List<JSONObject> itemList = fileReader.read();
				if(null != itemList){
					dataList.addAll(itemList);
				}
			} catch (IOException e) {
//				e.printStackTrace();
			}
		}
		return dataList;
	}

}
