package com.liuyun.doubao.io.file.support;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.liuyun.doubao.ctx.Context;
import com.liuyun.doubao.io.Closable;
import com.liuyun.doubao.io.Stopable;
import com.liuyun.doubao.utils.FileUtils;

public class PathChangedEventProcessor implements Closable, Stopable {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Context context = null;
	private Map<String, SingleFileReader> fileReaderMap = Maps.newConcurrentMap();
	private Map<String, String> fileKeyMap = Maps.newConcurrentMap();
	private Map<String, String> hashKeyMap = Maps.newConcurrentMap();
	private Map<String, SincedbHandler> sincedbHandlerMap = Maps.newConcurrentMap();
	
	public PathChangedEventProcessor(Context context){
		this.context = context;
	}
	
	public void addSincedbHandler(String hashKey, SincedbHandler handler){
		this.sincedbHandlerMap.put(hashKey, handler);
	}
	
	public void addFileReader(String hashKey, Path path) throws IOException {
		logger.debug("register file: {}", path.toString());
		this.hashKeyMap.put(path.toString(), hashKey);
		
		String fileKey = FileUtils.getInodeAndDevice(path);
		if(!this.fileReaderMap.containsKey(fileKey)){
			SingleFileReader fileReader = new SingleFileReader(context, path, sincedbHandler);
			this.fileReaderMap.put(fileKey, fileReader);
			this.fileKeyMap.put(path.toString(), fileKey);
		}
	}
	
	public void handleEvent(Kind<?> kind, Path child) {
		logger.debug("kind:{}, path: {}", kind.name(), child.toString());
		if(!this.fileKeyMap.containsKey(child.toString())){
			return;
		}
		
		String fileKey = FileUtils.getInodeAndDevice(child);
		if("ENTRY_CREATE".equals(kind.name())){
			this.fileReaderMap.get(fileKey).notifyForRead();
		} else if("ENTRY_MODIFY".equals(kind.name())){
			String oldFileKey = this.fileKeyMap.get(child.toString());
			if(fileKey.equals(oldFileKey)){
				this.fileReaderMap.get(fileKey).notifyForRead();
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
	
	private SincedbHandler getSincedbHandler(Path child) {
		if(this.hashKeyMap.containsKey(child.toString())){
			
		}
		return null;
	}

	@Override
	public void stop(boolean waitCompleted) {
		for(SingleFileReader fileReader: this.fileReaderMap.values()){
			fileReader.stop(true);
		}
	}

	@Override
	public void destroy() {
		//close file channel
		for(SingleFileReader fileReader: this.fileReaderMap.values()){
			fileReader.destroy();
		}
		
		for(SincedbHandler handler: this.sincedbHandlerMap.values()){
			handler.flush();
		}
	}
	
}
