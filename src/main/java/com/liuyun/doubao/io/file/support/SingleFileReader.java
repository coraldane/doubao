package com.liuyun.doubao.io.file.support;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.liuyun.doubao.config.file.FileInputConfig;
import com.liuyun.doubao.utils.DateUtils;
import com.liuyun.doubao.utils.StringUtils;
import com.liuyun.doubao.utils.SysUtils;

public class SingleFileReader {
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final int MAX_READ_LINES_PER_TIME = 10000;
	
	private volatile boolean ready = true;
	private volatile boolean waitForReading = true;
	private volatile long lastOffset = 0L;
	
	private RandomAccessFile fileHandler = null;
	private FileUniqueKey fileKey;
	private SincedbHandler sincedbHandler;
	
	private MessageBean messageBean = new MessageBean();
	
	public SingleFileReader(Path path, FileUniqueKey fileKey, SincedbHandler sincedbHandler) throws IOException {
		this.fileKey = fileKey;
		this.sincedbHandler = sincedbHandler;
		this.fileHandler = new RandomAccessFile(path.toFile(), "r");
		this.messageBean.setPath(path.toString());
		
		this.init();
	}
	
	private void init(){
		this.messageBean.setHost(SysUtils.getHostName());
		
		FileInputConfig inputConfig = this.sincedbHandler.getFileInputConfig();
		try {
			long offset = 0;
			if("end".equals(inputConfig.getStartPosition())){
				if(0 == this.sincedbHandler.getOffset(this.fileKey)){
					offset = this.fileHandler.length();
				} else {
					offset = this.sincedbHandler.getOffset(this.fileKey);
				}
			}
			this.sincedbHandler.setOffset(this.fileKey, offset);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<JSONObject> read() throws IOException{
		List<JSONObject> retList = Lists.newArrayList();
		List<String> strLineList = null;
		if(ready){
			this.setReady(false);
			long lastOffset = this.sincedbHandler.getOffset(this.fileKey);
			long newOffset = this.fileHandler.length();
			
			strLineList = this.readLines(lastOffset, newOffset);
		} else if(waitForReading){
			long newOffset = this.sincedbHandler.getOffset(this.fileKey);
			strLineList = this.readLines(this.lastOffset, newOffset);
		} else {
			return null;
		}
		
		if(CollectionUtils.isNotEmpty(strLineList)){
			FileInputConfig fileInputConfig = this.sincedbHandler.getFileInputConfig();
			for(String strLine: strLineList){
				JSONObject json = new JSONObject();
				json.put("@timestamp", DateUtils.formatNow(DateUtils.DATE_FORMAT_ISO8601));
				json.put("host", this.messageBean.getHost());
				json.put("path", this.messageBean.getPath());
				if(StringUtils.isNotEmpty(fileInputConfig.getType())){
					json.put("type", fileInputConfig.getType());
				}
				if(!fileInputConfig.getTags().isEmpty()){
					json.put("tags", fileInputConfig.getTags());
				}
				json.put("message", strLine);
				retList.add(json);
			}
		}
		return retList;
	}
	
	private List<String> readLines(long lastOffset, long newOffset) throws IOException{
		List<String> retList = Lists.newArrayList();
		
		int lineCount = 0;
		while(lastOffset < newOffset && lineCount < MAX_READ_LINES_PER_TIME){
			this.fileHandler.seek(lastOffset);
			String newLine = this.fileHandler.readLine();
			if(StringUtils.isEmpty(newLine)){
				break;
			}
			lastOffset += newLine.length() + 1;
			retList.add(newLine);
			lineCount ++;
		}
		
		if(lineCount == MAX_READ_LINES_PER_TIME && lastOffset < newOffset){
			this.lastOffset = lastOffset;
			this.sincedbHandler.setOffset(this.fileKey, newOffset);
			this.waitForReading = true;
		} else {
			this.sincedbHandler.setOffset(this.fileKey, lastOffset);
			this.waitForReading = false;
		}
		
		return retList;
	}
	
	public void setReady(boolean ready) {
		this.ready = ready;
	}
	
	public void destroy(){
		try {
			if(null != this.fileHandler){
				this.fileHandler.close();
				this.fileHandler = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
