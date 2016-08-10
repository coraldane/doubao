package com.liuyun.doubao.io.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.liuyun.doubao.config.InputConfig;
import com.liuyun.doubao.config.file.FileInputConfig;
import com.liuyun.doubao.ctx.Context;
import com.liuyun.doubao.io.Input;
import com.liuyun.doubao.io.file.support.FilePathWatcher;
import com.liuyun.doubao.io.file.support.PathChangedEventProcessor;
import com.liuyun.doubao.io.file.support.PathResolver;
import com.liuyun.doubao.utils.StringUtils;

public class FileInput implements Input {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private ExecutorService pathWatchExecutor = Executors.newCachedThreadPool();
	
	private Map<String, FilePathWatcher> pathWatcherMap = Maps.newConcurrentMap();
	
	private PathChangedEventProcessor pathChangedProcessor;
	private FileInputConfig inputConfig = null;
	private Context context = null;
	
	@Override
	public void init(InputConfig inputConfig, Context context) {
		this.context = context;
		this.pathChangedProcessor = new PathChangedEventProcessor(this.context);
		if(inputConfig instanceof FileInputConfig){
			this.inputConfig = (FileInputConfig)inputConfig;
		}
	}
	
	@Override
	public void destroy() {
		for(FilePathWatcher pathWatcher: this.pathWatcherMap.values()){
			pathWatcher.destroy();
		}
		this.pathWatchExecutor.shutdown();
		
		this.pathChangedProcessor.destroy();
	}
	
	@Override
	public void stop(boolean waitCompleted){
		this.pathChangedProcessor.stop(waitCompleted);
	}

	@Override
	public void startup() {
		try {
			this.registerPathWatcher();
		} catch (IOException e) {
			logger.error("register watcher error", e);
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
			pathChangedProcessor.addSincedbHandler(hashKey);
			
			FilePathWatcher pathWatcher = null;
			if(this.pathWatcherMap.containsKey(start.toString())){
				pathWatcher = this.pathWatcherMap.get(start.toString());
			} else {
				pathWatcher = new FilePathWatcher(start, this.inputConfig, pathChangedProcessor);
			}
			pathWatcher.addFileMatcher(hashKey, filepath);
			this.pathWatcherMap.put(start.toString(), pathWatcher);
		}
		
		for(FilePathWatcher pathWatcher: this.pathWatcherMap.values()){
			pathWatcher.startup();
			this.pathWatchExecutor.submit(pathWatcher);
		}
	}

}
