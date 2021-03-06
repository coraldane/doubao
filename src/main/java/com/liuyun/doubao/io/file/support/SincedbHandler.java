package com.liuyun.doubao.io.file.support;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.liuyun.doubao.common.Configuration;
import com.liuyun.doubao.utils.StringUtils;

public class SincedbHandler implements Runnable {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static final int DEFAULT_INTERVAL = 10;
	
	private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
	
	private static final String SINCEDB_FILE_PREFIX = ".sincedb_";
	private static final String DEFAULT_DATA_FOLDER_NAME = "data";
	private static final String SINCEDB_DATA_DELIMITER = ",";
	
	private String hashKey = null;
	
	private Path dataFolder = null;
	private Map<String, Long> offsetMap = Maps.newConcurrentMap();

	public SincedbHandler(String hash) throws IOException{
		this.hashKey = hash;
		this.initialFromDisk();
		
		executorService.scheduleAtFixedRate(this, 3, DEFAULT_INTERVAL, TimeUnit.SECONDS);
	}
	
	public long getOffset(String fileKey){
		if(this.offsetMap.containsKey(fileKey)){
			return this.offsetMap.get(fileKey);
		}
		return 0;
	}
	
	public void setOffset(String fileKey, long offset){
		this.offsetMap.put(fileKey, offset);
	}
	
	public void removeFile(String fileKey){
		this.offsetMap.remove(fileKey);
	}
	
	private void initialFromDisk() throws IOException{
		String dataFolderName = DEFAULT_DATA_FOLDER_NAME;
		String userDefinedDataFolder = Configuration.getInstance().getProperty("sincedb.path");
		if(StringUtils.isNotBlank(userDefinedDataFolder)){
			dataFolderName = userDefinedDataFolder;
		}
		this.dataFolder = Paths.get(dataFolderName);
		if(Files.exists(dataFolder)){
			if(Files.isRegularFile(dataFolder)){
				throw new RuntimeException("sincedb folder is not a valid directory!");
			}
		} else {
			Files.createDirectory(dataFolder);
		}
		
		Path dataPath = this.joinPath(this.dataFolder, SINCEDB_FILE_PREFIX + this.hashKey);
		if(Files.exists(dataPath) && Files.isRegularFile(dataPath)){
			this.readSincedbFile(dataPath);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void readSincedbFile(Path sincedbFilepath) throws IOException {
		FileReader fileReader = null;
		
		try{
			fileReader = new FileReader(sincedbFilepath.toFile());
			List<String> strLineList = IOUtils.readLines(fileReader);
			for(String strLine: strLineList){
				if(StringUtils.isBlank(strLine)){
					continue;
				}
				
				String[] strArray = strLine.trim().split(SINCEDB_DATA_DELIMITER);
				if(null == strArray || 3 != strArray.length || !StringUtils.isInteger(strArray[2])){
					continue;
				}
				
				this.offsetMap.put(strArray[0] + "," + strArray[1], Long.parseLong(strArray[2]));
			}
		} catch(IOException e){
			logger.error("read sincedb file error", e);
		} finally {
			if(null != fileReader){
				fileReader.close();
				fileReader = null;
			}
		}
		
	}
	
	@Override
	public void run(){
		flush();
	}
	
	public void flush(){
		Path tmpPath = this.joinPath(this.dataFolder, System.currentTimeMillis() + ".tmp");
		Path dataPath = this.joinPath(this.dataFolder, SINCEDB_FILE_PREFIX + this.hashKey);
		StringBuffer buffer = new StringBuffer();
		for(String key: this.offsetMap.keySet()){
			buffer.append(key).append(",").append(this.offsetMap.get(key)).append("\n");
		}
		try {
			Files.write(tmpPath, buffer.toString().getBytes());
			Files.deleteIfExists(dataPath);
			tmpPath.toFile().renameTo(dataPath.toFile());
		} catch (IOException e) {
			logger.error("flush sincedb data error", e);
		}
	}
	
	private Path joinPath(Path folder, String fileName){
		String folderName = folder.toString();
		if(!folderName.endsWith(File.separator)){
			folderName += File.separator;
		}
		return Paths.get(folderName + fileName);
	}
}
