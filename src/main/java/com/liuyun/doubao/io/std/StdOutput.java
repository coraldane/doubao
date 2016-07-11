package com.liuyun.doubao.io.std;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.liuyun.doubao.config.OutputConfig;
import com.liuyun.doubao.io.Output;

public class StdOutput implements Output {
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public void init(OutputConfig outputConfig) {
		
	}

	@Override
	public void destroy() {
		
	}
	
	@Override
	public int write(JSONArray dataArray) {
		int success = 0;
		
		for(int index=0; index < dataArray.size(); index++){
			JSONObject data = dataArray.getJSONObject(index);
			if(null == data){
				continue;
			}
			System.out.println(data.toJSONString());
			success ++;
		}
		
		return success;
	}

}
