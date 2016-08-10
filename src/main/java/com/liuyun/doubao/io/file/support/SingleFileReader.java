package com.liuyun.doubao.io.file.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.liuyun.doubao.config.file.FileInputConfig;
import com.liuyun.doubao.ctx.Context;
import com.liuyun.doubao.input.AbstractStopableDataReader;
import com.liuyun.doubao.utils.DateUtils;
import com.liuyun.doubao.utils.FileUtils;
import com.liuyun.doubao.utils.StringUtils;
import com.liuyun.doubao.utils.SysUtils;

public class SingleFileReader extends AbstractStopableDataReader {
	
	private static final int MAX_READ_LINES_PER_TIME = 100;
	
	protected volatile boolean waitForReading = true;
	private long lastOffset = 0L;
	
	private SeekableByteChannel fileHandler = null;
	private String fileKey;
	private SincedbHandler sincedbHandler;
	
	private MessageBean messageBean = new MessageBean();
	
	public SingleFileReader(Context context, Path path, SincedbHandler sincedbHandler) throws IOException {
		this.setContext(context);
		this.fileKey = FileUtils.getInodeAndDevice(path);
		this.sincedbHandler = sincedbHandler;
		this.fileHandler = Files.newByteChannel(path, StandardOpenOption.READ);
		this.messageBean.setPath(path.toString());
		
		this.init();
	}
	
	private void init(){
		this.messageBean.setHost(SysUtils.getHostName());
		FileInputConfig inputConfig = (FileInputConfig)this.context.getConfig().getInput();
		try {
			long offset = 0;
			if("end".equals(inputConfig.getStartPosition())){
				if(0 == this.sincedbHandler.getOffset(this.fileKey)){
					offset = this.fileHandler.size();
				} else {
					offset = this.sincedbHandler.getOffset(this.fileKey);
				}
			}
			this.sincedbHandler.setOffset(this.fileKey, offset);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void readData() throws IOException {
		this.setReadyForStop(false);
		
		this.lastOffset = this.sincedbHandler.getOffset(this.fileKey);
		long newOffset = this.fileHandler.size();
		
		List<String> strLineList = this.readLines(newOffset);
		this.writeData(strLineList);
		
		while(waitForReading){
			if(stopImmediately){
				this.sincedbHandler.setOffset(this.fileKey, this.lastOffset);
				break;
			}
			
			newOffset = this.sincedbHandler.getOffset(this.fileKey);
			strLineList = this.readLines(newOffset);
			this.writeData(strLineList);
		}
		this.setReadyForStop(true);
	}
	
	private void writeData(List<String> strLineList){
		if(CollectionUtils.isNotEmpty(strLineList)){
			FileInputConfig fileInputConfig = (FileInputConfig)this.context.getConfig().getInput();
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
				Context.readData2Queue(context, json);
			}
		}
	}
	
	private List<String> readLines(long newOffset) throws IOException{
		List<String> retList = Lists.newArrayList();
		
		int lineCount = 0;
		while(this.lastOffset < newOffset && lineCount < MAX_READ_LINES_PER_TIME){
			this.fileHandler.position(this.lastOffset);
			String newLine = this.readLine();
			if(StringUtils.isEmpty(newLine)){
				continue;
			}
			retList.add(newLine);
			lineCount ++;
		}
		
		if(lineCount == MAX_READ_LINES_PER_TIME && lastOffset < newOffset){
			this.sincedbHandler.setOffset(this.fileKey, newOffset);
			this.waitForReading = true;
		} else {
			this.sincedbHandler.setOffset(this.fileKey, lastOffset);
			this.waitForReading = false;
		}
		
		return retList;
	}
	
	private String readLine() throws IOException{
		ByteBuffer dst = ByteBuffer.allocate(1024);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
        while(this.fileHandler.read(dst) > 0){
        	dst.rewind();
        	for(int i=0; i < dst.limit(); i++){
        		byte ch = dst.get();
        		this.lastOffset ++;
        		if('\r' == ch || '\n' == ch){
        			return baos.toString();
        		} else {
        			baos.write(ch);
        		}
        	}
        	dst.flip();
        }
        return baos.toString();
	}
	
	public void reset(String newFileKey, Path path) throws IOException {
		this.fileKey = newFileKey;
		this.lastOffset = 0;
		this.destroy();
		
		this.fileHandler = Files.newByteChannel(path, StandardOpenOption.READ);
	}
	
	@Override
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
