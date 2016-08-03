package com.liuyun.doubao.io.std;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public int write(List<JSONObject> dataArray) {
		int success = 0;
		
		for(int index=0; index < dataArray.size(); index++){
			JSONObject data = dataArray.get(index);
			if(null == data){
				continue;
			}
			System.out.println(data.toJSONString());
			success ++;
		}
		
		return success;
	}

}
