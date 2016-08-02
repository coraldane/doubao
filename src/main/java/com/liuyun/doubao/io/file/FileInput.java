package com.liuyun.doubao.io.file;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.liuyun.doubao.config.InputConfig;
import com.liuyun.doubao.config.file.FileInputConfig;
import com.liuyun.doubao.io.Input;

public class FileInput implements Input {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private FileInputConfig inputConfig = null;
	
	@Override
	public void init(InputConfig inputConfig) {
		if(inputConfig instanceof FileInputConfig){
			this.inputConfig = (FileInputConfig)inputConfig;
			
			this.registerPathWatcher();
		}
	}
	
	private void registerPathWatcher(){
		
	}
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<JSONObject> read() {
		// TODO Auto-generated method stub
		return null;
	}

}
