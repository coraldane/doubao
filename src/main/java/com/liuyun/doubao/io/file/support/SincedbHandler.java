package com.liuyun.doubao.io.file.support;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.google.common.collect.Maps;
import com.liuyun.doubao.common.Configuration;
import com.liuyun.doubao.utils.StringUtils;

public class SincedbHandler {
	
	private static final String SINCEDB_FILE_PREFIX = ".sincedb_";
	private static final String DEFAULT_DATA_FOLDER_NAME = "data";
	private static final String SINCEDB_DATA_DELIMITER = ",";
	
	private String hashKey = null;
	
	private Path dataFolder = null;
	private Map<FileUniqueKey, Long> offsetMap = Maps.newConcurrentMap();

	public SincedbHandler(String hash) throws IOException{
		this.hashKey = hash;
		this.initialFromDisk();
	}
	
	public Long getOffset(FileUniqueKey fileKey){
		return this.offsetMap.get(fileKey);
	}
	
	public void setOffset(FileUniqueKey fileKey, long offset){
		this.offsetMap.put(fileKey, offset);
	}
	
	private void initialFromDisk() throws IOException{
		String dataFolderName = DEFAULT_DATA_FOLDER_NAME;
		String userDefinedDataFolder = Configuration.getInstance().getProperty("sincedb.path");
		if(StringUtils.isNotBlank(userDefinedDataFolder)){
			dataFolderName = userDefinedDataFolder;
		}
		this.dataFolder = Paths.get(dataFolderName);
		if(Files.exists(dataFolder)){
			if(!Files.isDirectory(dataFolder)){
				throw new RuntimeException("sincedb folder is not a valid directory!");
			}
		} else {
			Files.createDirectory(dataFolder);
		}
		
		Optional<Path> dbOpt = Files.list(dataFolder).filter(new Predicate<Path>(){
			@Override
			public boolean test(Path input) {
				return input.getFileName().equals(SINCEDB_FILE_PREFIX + hashKey);
			}}).findFirst();
		if(null == dbOpt || !dbOpt.isPresent()){
			return;
		}
		Path sincedbFilepath = dbOpt.get();
		if(null == sincedbFilepath){
			return;
		}
		if(Files.exists(sincedbFilepath) && Files.isRegularFile(sincedbFilepath)){
			this.readSincedbFile(sincedbFilepath);
		}
	}
	
	private void readSincedbFile(Path sincedbFilepath) throws IOException {
		List<String> strLineList = Files.readAllLines(sincedbFilepath);
		for(String strLine: strLineList){
			if(StringUtils.isBlank(strLine)){
				continue;
			}
			
			String[] strArray = strLine.trim().split(SINCEDB_DATA_DELIMITER);
			if(null == strArray || 3 != strArray.length || !StringUtils.isInteger(strArray[2])){
				continue;
			}
			
			this.offsetMap.put(new FileUniqueKey(strArray[0], strArray[1]), Long.parseLong(strArray[2]));
		}
	}
}
