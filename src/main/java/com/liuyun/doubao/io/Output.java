package com.liuyun.doubao.io;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.liuyun.doubao.config.OutputConfig;
import com.liuyun.doubao.ctx.Context;
import com.liuyun.doubao.extension.SPI;

@SPI
public interface Output extends Closable {
	
	void init(OutputConfig outputConfig, Context context);
	
	/**
	 * 
	 * @param dataArray
	 * @return 成功的记录数
	 */
	int write(List<JSONObject> dataArray);
	
	/**
	 * 写入压缩后的数据
	 * @param compressed
	 * @return
	 */
	int writeCompressedData(String compressed);
	
}
