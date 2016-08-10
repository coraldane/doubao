package com.liuyun.doubao.io.file.support;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.liuyun.doubao.ctx.Context;
import com.liuyun.doubao.input.DataReaderFactory;
import com.liuyun.doubao.io.Closable;
import com.liuyun.doubao.io.Stopable;
import com.liuyun.doubao.utils.FileUtils;

public class PathChangedEventProcessor implements Closable, Stopable {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private DataReaderFactory dataReaderFactory = new FileReaderFactory();
	private Context context = null;
	
	private Map<String, String> fileKeyMap = Maps.newConcurrentMap();
	private Map<String, String> hashKeyMap = Maps.newConcurrentMap();
	private Map<String, SincedbHandler> sincedbHandlerMap = Maps.newConcurrentMap();
	
	public PathChangedEventProcessor(Context context){
		this.context = context;
		new Thread(this.dataReaderFactory).start();
	}
	
	public void addSincedbHandler(String hashKey) throws IOException{
		SincedbHandler sincedbHandler = new SincedbHandler(hashKey);
		this.sincedbHandlerMap.put(hashKey, sincedbHandler);
	}
	
	public void addFileReader(String hashKey, Path path) throws IOException {
		logger.debug("register file: {}", path.toString());
		this.hashKeyMap.put(path.toString(), hashKey);
		SincedbHandler sincedbHandler = this.sincedbHandlerMap.get(hashKey);
		
		String fileKey = FileUtils.getInodeAndDevice(path);
		if(!this.dataReaderFactory.containsReader(fileKey)){
			SingleFileReader fileReader = new SingleFileReader(context, path, sincedbHandler);
			this.fileKeyMap.put(path.toString(), fileKey);
			this.dataReaderFactory.addReader(fileKey, fileReader);
		}
	}
	
	public void handleEvent(Kind<?> kind, Path child) {
		if(!this.fileKeyMap.containsKey(child.toString())){
			return;
		}
		logger.debug("kind:{}, path: {}", kind.name(), child.toString());
		
		SincedbHandler sincedbHandler = this.getSincedbHandler(child);
		String fileKey = FileUtils.getInodeAndDevice(child);
		if("ENTRY_CREATE".equals(kind.name())){
			this.dataReaderFactory.notifyForRead(fileKey);
		} else if("ENTRY_MODIFY".equals(kind.name())){
			String oldFileKey = this.fileKeyMap.get(child.toString());
			if(fileKey.equals(oldFileKey)){
				this.dataReaderFactory.notifyForRead(fileKey);
			} else {
				this.fileKeyMap.put(child.toString(), fileKey);
				logger.debug("file has changed, path: {}", child.toString());
				this.dataReaderFactory.resetReader(oldFileKey, fileKey, child);
			}
		} else if("ENTRY_DELETE".equals(kind.name())){
			sincedbHandler.removeFile(fileKey);
			this.dataReaderFactory.destroy(fileKey);
		}
	}
	
	private SincedbHandler getSincedbHandler(Path child) {
		if(this.hashKeyMap.containsKey(child.toString())){
			String hashKey = this.hashKeyMap.get(child.toString());
			return this.sincedbHandlerMap.get(hashKey);
		}
		return null;
	}

	@Override
	public void stop(boolean waitCompleted) {
		this.dataReaderFactory.stop(waitCompleted);
	}
	
	@Override
	public void destroy() {
		this.dataReaderFactory.destroy();
		
		for(SincedbHandler handler: this.sincedbHandlerMap.values()){
			handler.flush();
		}
	}
	
}
